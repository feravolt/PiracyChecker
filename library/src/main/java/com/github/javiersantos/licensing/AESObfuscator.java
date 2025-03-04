/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.javiersantos.licensing;

import com.github.javiersantos.licensing.util.Base64;
import com.github.javiersantos.licensing.util.Base64DecoderException;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * An Obfuscator that uses AES to encrypt data.
 */
public class AESObfuscator implements Obfuscator {
    private static final String KEYGEN_ALGORITHM = "PBEWITHSHAAND256BITAES-CBC-BC";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final byte[] IV =
            {16, 74, 71, -80, 32, 101, -47, 72, 117, -14, 0, -29, 70, 65, -12, 74};
    private static final String header = "com.github.javiersantos.licensing.AESObfuscator-1|";

    private final Cipher mEncryptor;
    private final Cipher mDecryptor;

    /**
     * @param salt          an array of random bytes to use for each (un)obfuscation
     * @param applicationId application identifier, e.g. the package name
     * @param deviceId      device identifier. Use as many sources as possible to create this unique identifier.
     */
    public AESObfuscator(byte[] salt, String applicationId, String deviceId) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(KEYGEN_ALGORITHM);
            KeySpec keySpec =
                    new PBEKeySpec((applicationId + deviceId).toCharArray(), salt, 1024, 256);
            SecretKey tmp = factory.generateSecret(keySpec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
            mEncryptor = Cipher.getInstance(CIPHER_ALGORITHM);
            mEncryptor.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(IV));
            mDecryptor = Cipher.getInstance(CIPHER_ALGORITHM);
            mDecryptor.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(IV));
        } catch (GeneralSecurityException e) {
            // This can't happen on a compatible Android device.
            throw new RuntimeException("Invalid environment", e);
        }
    }

    public String obfuscate(String original, String key) {
        if (original == null) {
            return null;
        }
        try {
            // Header is appended as an integrity check
            return Base64.encode(mEncryptor.doFinal((header + key + original).getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Invalid environment", e);
        }
    }

    public String unobfuscate(String obfuscated, String key) throws ValidationException {
        if (obfuscated == null) {
            return null;
        }
        try {
            String result = new String(mDecryptor.doFinal(Base64.decode(obfuscated)), StandardCharsets.UTF_8);
            // Check for presence of header. This serves as a final integrity check, for cases
            // where the block size is correct during decryption.
            int headerIndex = result.indexOf(header + key);
            if (headerIndex != 0) {
                throw new ValidationException("Header not found (invalid data or key)" + ":" +
                        obfuscated);
            }
            return result.substring(header.length() + key.length());
        } catch (Base64DecoderException | IllegalBlockSizeException | BadPaddingException e) {
            throw new ValidationException(e.getMessage() + ":" + obfuscated);
        }
    }
}
