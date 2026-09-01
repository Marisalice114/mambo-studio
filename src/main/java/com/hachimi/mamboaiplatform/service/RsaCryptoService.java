package com.hachimi.mamboaiplatform.service;

/**
 * RSA 加密相关服务接口。
 *
 * <p>用于登录/注册时对前端提交的加密密码进行解密，解密出明文后
 * 由调用方继续走原有 MD5 加盐哈希逻辑。</p>
 *
 * @author <a href="https://github.com/Marisalice114">Marisalice114</a>
 */
public interface RsaCryptoService {

  /**
   * 使用私钥解密前端提交的 RSA 密文密码。
   *
   * @param cipherPassword 前端 Base64(RSA 加密后的密码)
   * @return 明文密码
   */
  String decryptPassword(String cipherPassword);
}