package com.hachimi.mamboaiplatform.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 记录 appId -> cancelFlag / sessionId，用于实现用户主动取消。
 */
public class GenerationSessionRegistry {
  private static final Map<Long, String> CURRENT_SESSION = new ConcurrentHashMap<>();
  private static final Map<Long, Boolean> CANCEL_FLAGS = new ConcurrentHashMap<>();

  public static void setSession(Long appId, String sessionId) {
    CURRENT_SESSION.put(appId, sessionId);
    CANCEL_FLAGS.remove(appId);
  }

  public static String getSession(Long appId) {
    return CURRENT_SESSION.get(appId);
  }

  public static void clearSession(Long appId) {
    CURRENT_SESSION.remove(appId);
    CANCEL_FLAGS.remove(appId);
  }

  public static void cancel(Long appId) {
    CANCEL_FLAGS.put(appId, true);
  }

  public static boolean isCancelled(Long appId) {
    return CANCEL_FLAGS.getOrDefault(appId, false);
  }
}
