/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.support;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class Encrypter {

    private final EncryptionAlgorythm algo;

    public Encrypter(String passPhrase) {
        if (passPhrase == null) {
            throw new NullPointerException(
                    "passPhrase cannot be NULL"); // NOI18N
        }

        if (passPhrase.length() < 8) {
            throw new RuntimeException(
                    "passPhrase cannot be less than 8 characters"); // NOI18N
        }

        EncryptionAlgorythm algorithm = null;
        
        try {
            algorithm = new DESEncrypter(passPhrase);
        } catch (NoSuchAlgorithmException ex) {
            // fall-back to simple mangling...
            algorithm = new XOREncrypter(passPhrase);
        }

        algo = algorithm;
    }

    public String encrypt(String str) {
        return algo.encrypt(str);
    }

    public char[] encrypt(char[] chars) {
        return algo.encrypt(String.valueOf(chars)).toCharArray();
    }

    public String decrypt(String str) {
        return algo.decrypt(str);
    }

    public char[] decrypt(char[] chars) {
        return algo.decrypt(String.valueOf(chars)).toCharArray();
    }

    public static long getFileChecksum(String fname) {
        File file = new File(fname);
        if (file == null || !file.exists()) {
            return -1;
        }

        Checksum checksum = new CRC32();

        BufferedInputStream is = null;

        try {
            is = new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (is == null) {
            return -1;
        }

        byte[] bytes = new byte[1024];
        int len = 0;

        try {
            while ((len = is.read(bytes)) >= 0) {
                checksum.update(bytes, 0, len);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return checksum.getValue();
    }

    public static boolean checkCRC32(String fname, long checksum) {
        return checksum == getFileChecksum(fname);
    }

    private static interface EncryptionAlgorythm {

        public String encrypt(String str);

        public String decrypt(String str);
    }

    private static class DESEncrypter implements EncryptionAlgorythm {

        private SecretKey key;
        private sun.misc.BASE64Encoder base64encoder =
                new sun.misc.BASE64Encoder();
        private sun.misc.BASE64Decoder base64decoder =
                new sun.misc.BASE64Decoder();

        public DESEncrypter(final String passPhrase) throws NoSuchAlgorithmException {
            try {
                DESKeySpec keySpec =
                        new DESKeySpec(passPhrase.getBytes("UTF8")); // NOI18N
                SecretKeyFactory keyFactory =
                        SecretKeyFactory.getInstance("DES"); // NOI18N
                key = keyFactory.generateSecret(keySpec);
            } catch (UnsupportedEncodingException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvalidKeySpecException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvalidKeyException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public synchronized String encrypt(String str) {
            String result = null;

            try {
                byte[] cleartext = str.getBytes("UTF8"); // NOI18N
                Cipher cipher = Cipher.getInstance("DES"); // NOI18N
                cipher.init(Cipher.ENCRYPT_MODE, key);
                result = base64encoder.encode(cipher.doFinal(cleartext));
            } catch (IllegalBlockSizeException ex) {
                Exceptions.printStackTrace(ex);
            } catch (BadPaddingException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvalidKeyException ex) {
                Exceptions.printStackTrace(ex);
            } catch (NoSuchAlgorithmException ex) {
                Exceptions.printStackTrace(ex);
            } catch (NoSuchPaddingException ex) {
                Exceptions.printStackTrace(ex);
            } catch (UnsupportedEncodingException ex) {
                Exceptions.printStackTrace(ex);
            }

            return result;
        }

        @Override
        public synchronized String decrypt(String str) {
            String result = ""; // NOI18N
            try {
                byte[] encrypedPwdBytes = base64decoder.decodeBuffer(str);
                Cipher cipher = Cipher.getInstance("DES"); // NOI18N
                cipher.init(Cipher.DECRYPT_MODE, key);
                byte[] plainTextPwdBytes = cipher.doFinal(encrypedPwdBytes);
                StringBuilder sb = new StringBuilder();

                for (byte b : plainTextPwdBytes) {
                    sb.append((char) b);
                }

                result = sb.toString();
            } catch (IllegalBlockSizeException ex) {
                Exceptions.printStackTrace(ex);
            } catch (BadPaddingException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvalidKeyException ex) {
                Exceptions.printStackTrace(ex);
            } catch (NoSuchAlgorithmException ex) {
                Exceptions.printStackTrace(ex);
            } catch (NoSuchPaddingException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            return result;
        }
    }

    private static class XOREncrypter implements EncryptionAlgorythm {

        private final static String emptyPwd = "$$$EmptyPassword$$$"; // NOI18N
        private final static char delimeter = '\000'; // NOI18N
        private final byte[] passPhrase;

        public XOREncrypter(String passPhrase) {
            this.passPhrase = passPhrase.getBytes();
        }

        @Override
        public String encrypt(String str) {
            if (str != null && str.length() == 0) {
                str = emptyPwd;
            }

            return xor(str + delimeter);
        }

        @Override
        public String decrypt(String str) {
            String out = xor(str);
            int delimIndex = out == null ? -1 : out.indexOf(delimeter);
            if (delimIndex > -1) {
                out = out.substring(0, delimIndex);
            }
            if (emptyPwd.equals(out)) {
                out = ""; // NOI18N
            }
            return out;
        }

        private String xor(String str) {
            if (str == null) {
                return null;
            }
            byte[] bytes = str.getBytes();
            int len = Math.max(passPhrase.length, bytes.length);
            byte[] out = new byte[len];
            for (int i = 0; i < len; i++) {
                out[i] = (byte) (bytes[i % bytes.length] ^
                        passPhrase[i % passPhrase.length]);
            }
            return new String(out);
        }
    }
}
