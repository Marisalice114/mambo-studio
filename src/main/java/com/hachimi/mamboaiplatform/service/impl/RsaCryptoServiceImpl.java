package com.hachimi.mamboaiplatform.service.impl;

import com.hachimi.mamboaiplatform.exception.BusinessException;
import com.hachimi.mamboaiplatform.exception.ErrorCode;
import com.hachimi.mamboaiplatform.service.RsaCryptoService;
import com.hachimi.mamboaiplatform.utils.RsaCryptoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * RSA 加密服务实现。
 *
 * <p>私钥通过配置项 {@code app.rsa.private-key-base64} 注入，
 * 该配置必须放在不入库的本地/环境配置中（如 application-local.yml），
 * 禁止将私钥提交到公共仓库。</p>
 *
 * @author <a href="https://github.com/Marisalice114">Marisalice114</a>
 */
@Service
@Slf4j
public class RsaCryptoServiceImpl implements RsaCryptoService {

  /**
   * Base64 编码的 PKCS#8 私钥，来自不入库的配置文件或环境变量。
   */
  private final String privateKeyBase64;

  public RsaCryptoServiceImpl(
      @Value("${app.rsa.private-key-base64:}") String privateKeyBase64) {
    this.privateKeyBase64 = privateKeyBase64;
  }

  @Override
  public String decryptPassword(String cipherPassword) {
    if (!StringUtils.hasText(privateKeyBase64)) {
      log.error("RSA 私钥未配置，请检查 app.rsa.private-key-base64 配置项");
      throw new BusinessException(ErrorCode.SYSTEM_ERROR, "服务端加密配置缺失");
    }
    if (!StringUtils.hasText(cipherPassword)) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
    }
    try {
      String plain = RsaCryptoUtil.decryptByPrivateKey(privateKeyBase64, cipherPassword);
      if (!StringUtils.hasText(plain)) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码解密失败");
      }
      return plain;
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("RSA 密码解密失败", e);
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码解密失败");
    }
  }
}