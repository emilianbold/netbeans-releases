/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.lib.uihandler;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

/**
 *
 * @author Jindrich Sedek
 */
public class PasswdEncryption {

    private static final String delimiter = ":"; //NOI18N

    public static String encrypt(String text) throws IOException, GeneralSecurityException {
        return encrypt(text, getPublicKey());
    }

    public static byte[] encrypt(byte[] text) throws IOException, GeneralSecurityException {
        return encrypt(text, getPublicKey());
    }

    public static byte[] encrypt(byte[] text, PublicKey key) throws IOException, GeneralSecurityException {
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // NOI18N
        rsaCipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encoded = null;
        encoded = rsaCipher.doFinal(text);
        return encoded;
    }

    public static byte[] decrypt(byte[] text, PrivateKey key) throws IOException, GeneralSecurityException {
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // NOI18N
        rsaCipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decoded = null;
        decoded = rsaCipher.doFinal(text);
        return decoded;
    }

    public static String encrypt(String text, PublicKey key) throws IOException, GeneralSecurityException {
        byte[] encrypted = encrypt(text.getBytes(), key);
        return arrayToString(encrypted);
    }

    public static String decrypt(String text, PrivateKey key) throws IOException, GeneralSecurityException {
        byte[] decrypted = decrypt(stringToArray(text), key);
        return new String(decrypted);
    }

    private static String arrayToString(byte[] array) {
        String result = new String();
        for (int i = 0; i < array.length; i++) {
            byte b = array[i];
            result = result.concat(Byte.toString(b) + delimiter);
        }
        return result;
    }

    private static byte[] stringToArray(String str) {
        String[] numbers = str.split(delimiter);
        byte[] result = new byte[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            result[i] = Byte.parseByte(numbers[i]);
        }
        return result;
    }

    private static PublicKey getPublicKey() throws IOException, GeneralSecurityException {
        InputStream inputStr = PasswdEncryption.class.getResourceAsStream("pubKey"); // NOI18N
        byte[] encodedKey = new byte[inputStr.available()];
        inputStr.read(encodedKey);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedKey);
        KeyFactory kf = KeyFactory.getInstance("RSA"); // NOI18N
        PublicKey publicKey = kf.generatePublic(publicKeySpec);
        return publicKey;
    }
}
