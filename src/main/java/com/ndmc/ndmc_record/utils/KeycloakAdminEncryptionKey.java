package com.ndmc.ndmc_record.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class KeycloakAdminEncryptionKey {

    public static final String ALGORITHM = "AES";
    @Value("${KEY}")
    private String keySecret;

//    String encriptValue = keycloakAdminDecrypt.encrypt("oKnQTc0e",key);
//    logger.info("ENCRYPTED__VALUE " +encriptValue);

    public Key generateKey() throws Exception {
        Key key = new SecretKeySpec(keySecret.getBytes(),ALGORITHM);
        return key;
    }

    public String encrypt(String valueToEnc) throws Exception {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encValue = cipher.doFinal(valueToEnc.getBytes());
        byte[] encryptedByteValue = new Base64().encode(encValue);
        System.out.println("Encrypted Value :: " + new String(encryptedByteValue));
        return new String(encryptedByteValue);
    }

    public String decrypt(String encryptedValue) throws Exception {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedBytes = new Base64().decode(encryptedValue.getBytes());
        byte[] enctVal = cipher.doFinal(decodedBytes);
        System.out.println("Decrypted Value :: " + new String(enctVal));
        return new String(enctVal);
    }
}
