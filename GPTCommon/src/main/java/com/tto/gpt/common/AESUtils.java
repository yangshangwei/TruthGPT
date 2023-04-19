package com.tto.gpt.common;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

public class AESUtils {

    private static final String ALGORITHM = "AES";
    private static final String KEY = "_YourGPT_isPoweredByTTO_";

    /**
     * 加密
     *
     * @param data
     *            要加密的数据
     * @return 加密后的数据
     * @throws Exception
     */
    public static String encrypt(String data) throws Exception {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedValue = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedValue);
    }

    /**
     * 解密
     *
     * @param data
     *            要解密的数据
     * @return 解密后的数据
     * @throws Exception
     */
    public static String decrypt(String data) throws Exception {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedValue = Base64.getDecoder().decode(data);
        byte[] decValue = cipher.doFinal(decryptedValue);
        return new String(decValue);
    }

    /**
     * 生成密钥
     *
     * @return 密钥
     * @throws Exception
     */
    private static Key generateKey() throws Exception {
        return new SecretKeySpec(KEY.getBytes(), ALGORITHM);
    }

}
