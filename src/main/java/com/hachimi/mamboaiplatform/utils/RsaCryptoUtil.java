package com.hachimi.mamboaiplatform.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * RSA 加解密工具类（公钥加密 / 私钥解密）。
 *
 * <p>用于登录/注册时前端加密密码传输，后端使用私钥解密出明文后，
 * 再走原有的 MD5 加盐哈希逻辑，避免密码在传输链路中以明文出现。</p>
 *
 * <p>安全说明：私钥仅存于后端服务端环境，禁止提交到代码仓库。</p>
 *
 * @author <a href="https://github.com/Marisalice114">Marisalice114</a>
 */
public final class RsaCryptoUtil {

  private RsaCryptoUtil() {
  }

  /**
   * 使用 Base64 编码的 PKCS#8 私钥，解密 Base64 编码的密文。
   *
   * @param base64PrivateKey Base64 编码的 PKCS#8 私钥（不含 PEM 头尾）
   * @param base64CipherText Base64 编码的 RSA 密文
   * @return 解密后的明文字符串
   * @throws Exception 密钥或密文非法时抛出
   */
  public static String decryptByPrivateKey(String base64PrivateKey, String base64CipherText) throws Exception {
    byte[] keyBytes = Base64.getDecoder().decode(base64PrivateKey);
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    PrivateKey privateKey = keyFactory.generatePrivate(spec);

    Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
    // 关键：显式指定 MGF1 使用 SHA-256，与前端 Web Crypto (RSA-OAEP, SHA-256) 对齐
    OAEPParameterSpec oaepParams = new OAEPParameterSpec(
        "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
    cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams);
    byte[] plainBytes = cipher.doFinal(Base64.getDecoder().decode(base64CipherText));
    return new String(plainBytes, StandardCharsets.UTF_8);
  }
}