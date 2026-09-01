/**
 * RSA 密码加密工具
 *
 * 用于登录/注册时对密码进行 RSA 公钥加密后再提交，避免密码以明文传输。
 * 对应后端私钥位于不入库的 application-local.yml（app.rsa.private-key-base64）。
 *
 * 注意：这里的公钥是公开信息（仅用于加密），私钥仅存于服务端。
 */

// RSA 2048 公钥（PKCS#8 / SPKI PEM，单行）
// 若服务端私钥轮换，需同步更新此公钥
export const PUBLIC_KEY = `-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApOYokieWoA3ilZ5r+mOiuhuk7N8cDBSW6yXffFBsiBiRQoes1f1ucATIah6yMHxu+4kDeG0skxprty1b3n1ghmYgavJh5lb0o8gvRUToTj2j70kf5nktnfzVgNM1g+sUD6X+Ast65kJzH5uvsTD5/u+IjYqhObaimnqxILA25ZErP9M4Rj/NJQzKSxy4vtxW076XXuFPk6/XfSwsuqgHVTv4/Kb5An4AYXh1D7HrfF9A6xazKpYrprvhVmDI1l4TNuYg3FzXXlWrxMBeAdfwey0ljp6X+eFrW5Fs67PGIJuHKDTUhWd1/XCwVpXmj0uIAYnTEcZQac2qRvadrKCOeQIDAQAB
-----END PUBLIC KEY-----`

/**
 * 使用 Web Crypto API 对密码进行 RSA-OAEP 加密
 * @param plainPassword 明文密码
 * @returns Base64 编码的密文
 */
export async function encryptPassword(plainPassword: string): Promise<string> {
  const encoder = new TextEncoder()
  const keyData = encoder.encode(PUBLIC_KEY)

  // 导入公钥（SPKI 格式）
  const publicKey = await crypto.subtle.importKey(
    'spki',
    keyData,
    {
      name: 'RSA-OAEP',
      hash: 'SHA-256',
    },
    false,
    ['encrypt'],
  )

  // 加密密码（RSA-OAEP 加密）
  const encrypted = await crypto.subtle.encrypt(
    {
      name: 'RSA-OAEP',
    },
    publicKey,
    encoder.encode(plainPassword),
  )

  // 转 Base64
  const bytes = new Uint8Array(encrypted)
  let binary = ''
  bytes.forEach((b) => {
    binary += String.fromCharCode(b)
  })
  return btoa(binary)
}