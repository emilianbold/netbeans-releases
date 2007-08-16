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

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import junit.framework.TestCase;

/**
 *
 * @author Jindrich Sedek
 */
public class PasswdEncryptionTest extends TestCase {

    private String inputText = "THIS IS text \\$.-=/;[]1!/*-56";

    public PasswdEncryptionTest(String testName) {
        super(testName);
    }

    public void testEncryptAndDecryptStrings() throws Exception {
        KeyPair pair = generateKeys();
        String encoded = PasswdEncryption.encrypt(inputText, pair.getPublic());
        String decoded = PasswdEncryption.decrypt(encoded, pair.getPrivate());
        assertEquals("DECRIPTED", inputText, decoded);
    }

    public void testEncryptAndDecryptBytes() throws Exception {
        KeyPair pair = generateKeys();
        byte[] encoded = PasswdEncryption.encrypt(inputText.getBytes(), pair.getPublic());
        byte[] decoded = PasswdEncryption.decrypt(encoded, pair.getPrivate());
        assertEquals("DECRIPTED", inputText, new String(decoded));
    }

    public void testEncryptDefault() throws Exception {
        String encoded = PasswdEncryption.encrypt(inputText);
        assertFalse("IS ENCODED", encoded.equals(inputText));
    }

    public void testConvert() throws Exception {
        byte[] pole = inputText.getBytes();
        assertEquals(inputText, new String(pole));
    }

    private KeyPair generateKeys() throws Exception {
        /* Generate a RSA key pair */
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyGen.initialize(1024, random);
        return keyGen.generateKeyPair();
    }
}
