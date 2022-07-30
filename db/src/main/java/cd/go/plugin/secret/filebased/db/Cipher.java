/*
 * Copyright 2022 Thoughtworks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cd.go.plugin.secret.filebased.db;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static cd.go.plugin.secret.filebased.db.Util.isBlank;

public class Cipher {
    public static String decrypt(String cipherKey, String encryptedValue) throws BadSecretException, GeneralSecurityException {
        byte[] keyBytes = Base64.getDecoder().decode(cipherKey);

        if (!canDecrypt(encryptedValue)) {
            throw new BadSecretException("Bad cipher text");
        }

        String[] splits = encryptedValue.split(":");

        String encodedIV = splits[1];
        String encodedCipherText = splits[2];

        byte[] initializationVector = Base64.getDecoder().decode(encodedIV);
        javax.crypto.Cipher decryptCipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
        decryptCipher.init(javax.crypto.Cipher.DECRYPT_MODE, createSecretKeySpec(keyBytes), new IvParameterSpec(initializationVector));

        byte[] decryptedBytes = decryptCipher.doFinal(Base64.getDecoder().decode(encodedCipherText));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public static byte[] generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        keygen.init(128);
        return keygen.generateKey().getEncoded();
    }

    public static String encrypt(String cipherKey, String value) throws GeneralSecurityException {
        byte[] keyBytes = Base64.getDecoder().decode(cipherKey);
        byte[] initializationVector = generateKey();
        byte[] bytesToEncrypt = value.getBytes(StandardCharsets.UTF_8);

        javax.crypto.Cipher encryptCipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
        encryptCipher.init(javax.crypto.Cipher.ENCRYPT_MODE, createSecretKeySpec(keyBytes), new IvParameterSpec(initializationVector));

        byte[] encryptedBytes = encryptCipher.doFinal(bytesToEncrypt);

        return String.join(":", "AES", encode(initializationVector), encode(encryptedBytes));
    }

    private static String encode(byte[] initializationVector) {
        return Base64.getEncoder().encodeToString(initializationVector);
    }

    private static SecretKeySpec createSecretKeySpec(byte[] key) {
        return new SecretKeySpec(key, "AES");
    }

    private static boolean canDecrypt(String cipherText) {
        if (isBlank(cipherText)) {
            return false;
        }
        String[] splits = cipherText.split(":");
        return splits.length == 3 && "AES".equals(splits[0]) && (!isBlank(splits[1])) && (!isBlank(splits[2]));
    }
}
