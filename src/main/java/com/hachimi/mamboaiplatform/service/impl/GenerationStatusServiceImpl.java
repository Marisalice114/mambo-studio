package com.hachimi.mamboaiplatform.service.impl;

import cn.hutool.json.JSONUtil;
import com.hachimi.mamboaiplatform.service.GenerationStatusService;
import com.hachimi.mamboaiplatform.service.model.GenerationStatus;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
public class GenerationStatusServiceImpl implements GenerationStatusService {

  private static final String KEY_PREFIX = "code_gen_status:";
  private static final Duration TTL = Duration.ofHours(2);

  @Resource
  private RedissonClient redissonClient;

  private String key(Long appId) {
    return KEY_PREFIX + appId;
  }

  private void save(Long appId, GenerationStatus status) {
    try {
      RBucket<String> bucket = redissonClient.getBucket(key(appId));
      bucket.set(JSONUtil.toJsonStr(status), TTL);
    } catch (Exception e) {
      log.error("保存生成状态失败 appId={} status={} error={}", appId, status, e.getMessage());
    }
  }

  private GenerationStatus load(Long appId) {
    try {
      RBucket<String> bucket = redissonClient.getBucket(key(appId));
      String json = bucket.get();
      if (json == null)
        return null;
      return JSONUtil.toBean(json, GenerationStatus.class);
    } catch (Exception e) {
      log.error("读取生成状态失败 appId={} error={}", appId, e.getMessage());
      return null;
    }
  }

  @Override
  public void markRunning(Long appId, String sessionId) {
    GenerationStatus s = new GenerationStatus();
    s.setStatus("running");
    s.setSessionId(sessionId);
    s.setUpdatedAt(System.currentTimeMillis());
    save(appId, s);
  }

  @Override
  public void markBuilt(Long appId, String sessionId, String message) {
    GenerationStatus s = load(appId);
    if (s == null)
      s = new GenerationStatus();
    s.setStatus("built");
    s.setSessionId(sessionId);
    s.setMessage(message);
    s.setUpdatedAt(System.currentTimeMillis());
    save(appId, s);
  }

  @Override
  public void markFailed(Long appId, String sessionId, String message) {
    GenerationStatus s = load(appId);
    if (s == null)
      s = new GenerationStatus();
    s.setStatus("failed");
    s.setSessionId(sessionId);
    s.setMessage(message);
    s.setUpdatedAt(System.currentTimeMillis());
    save(appId, s);
  }

  @Override
  public void markStopped(Long appId, String sessionId, String message) {
    GenerationStatus s = load(appId);
    if (s == null)
      s = new GenerationStatus();
    s.setStatus("stopped");
    s.setSessionId(sessionId);
    s.setMessage(message);
    s.setUpdatedAt(System.currentTimeMillis());
    save(appId, s);
  }

  @Override
  public GenerationStatus getStatus(Long appId) {
    return load(appId);
  }
}
