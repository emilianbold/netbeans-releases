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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.jca.base;

import java.io.BufferedReader;
import java.io.InputStreamReader;


/**
 * <p>
 * Performs encryption / decription using a single key
 * </p>
 *
 * <p></p>
 *
 * @author Robert Frank-Thompson (ported to Java by Andrea Joy Spilholtz)
 */
public class ScEncrypt {
    /* ********************************************************* */
    /* CONSTANTS                                                 */
    /* ********************************************************* */

    // public final static String RCS_ID= StcCorp.COPYRIGHT +
    //  "$Id: ScEncrypt.java,v 1.3 2007/11/18 02:43:05 echou Exp $";

    /* ********************************************************* */
    /* PRIVATE CONSTANTS                                         */
    /* ********************************************************* */

    /** keys */
    private static final char SC_M_CRYPT_CHAIN_INIT = (char) 0x72;

    /** keys */
    private static final short[] SC_V_CRYPT_PIN = {
        3, 5, 15, 6, 12, 9, 2, 0, 13, 10, 7, 4, 1, 14, 11, 8
    };

    /** keys */
    private static final short[] SC_V_CRYPT_POU = {
        7, 12, 6, 0, 11, 1, 3, 10, 15, 5, 9, 14, 4, 8, 13, 2
    };

    /** keys */
    private static final char[] SC_V_CRYPT_DFT_KEY = {
        (char) 0x83, (char) 0x3A, (char) 0xFC, (char) 0xE9, (char) 0x47,
        (char) 0x95, (char) 0x21, (char) 0x6D, (char) 0xBE, (char) 0x7F,
        (char) 0xA2, (char) 0x54, (char) 0xC6, (char) 0xDB, (char) 0x18,
        (char) 0x00
    };

    /* ********************************************************* */
    /* CONSTRUCTORS                                              */
    /* ********************************************************* */
    /* ********************************************************* */
    /* UNIT TEST                                                 */
    /* ********************************************************* */

    /**
     * Main driver.
     *
     * @param args arguments.
     *
     * @throws Exception on error.
     */
    public static void main(String[] args) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(
                    System.in));
        System.out.print("Password:  ");

        String password = in.readLine();
        String data = "foo";

        while (data.length() > 0) {
            System.out.print("> ");
            data = in.readLine();

            if (data.length() > 0) {
                System.out.println("   (" + data.length() + ")");

                String encrypt = ScEncrypt.encrypt(password, data);
                System.out.println("   " + encrypt + " (" + encrypt.length()
                    + ")");

                String decrypt = ScEncrypt.decrypt(password, encrypt);
                System.out.println("   " + decrypt + " (" + decrypt.length()
                    + ")");
            }
        }
    }

    /* ********************************************************* */
    /* PUBLIC STATIC METHODS                                     */
    /* ********************************************************* */

    /**
     * Decrypt.
     *
     * @param key key.
     * @param data data
     *
     * @return decrypted string.
     *
     * @throws Exception on error.
     */
    public static String decrypt(String key, String data)
        throws Exception {
        ScSCrypt crypt;
        char[] code = new char[(data.length() / 2)];
        char[] out;
        int codePtr;
        int dataPtr;
        char abyte;
        char lbyte;
        char rbyte;
        int length;
        int count;
        int num;

        crypt = new ScSCrypt(key);
        dataPtr = 0;

        char[] lenTmp = new char[1];
        lenTmp[0] = data.charAt(dataPtr);

        char[] ctmp = new char[2];

        /* Convert hex string data to integers */
        for (count = 0; count < (data.length() / 2); count++) {
            ctmp[0] = data.charAt(dataPtr);
            ctmp[1] = data.charAt(dataPtr + 1);
            num = Integer.parseInt(new String(ctmp), 16);
            code[count] = (char) num;
            dataPtr += 2;
        }

        /*  Prepare to decrypt data.                                */
        dataPtr = 0;
        codePtr = 0;

        /*  Pop data length.                                        */
        length = (int) code[codePtr];
        codePtr++;

        /*  Decrypt the data.                                       */
        out = new char[length];

        for (count = 0; count < length; count++) {
            /*  Unchain */
            abyte = code[codePtr];

            // we need to keep this to 8 bits
            crypt.chain = (char) ((crypt.chain << 2) & 0x00FF);
            abyte ^= crypt.chain;
            crypt.chain = code[codePtr];

            /*  Split   */
            lbyte = (char) (abyte >> 4);
            rbyte = (char) (abyte & 0x0F);

            /*  Xor     */
            lbyte ^= crypt.keySpace[crypt.currKey];
            crypt.currKey++;
            rbyte ^= crypt.keySpace[crypt.currKey];
            crypt.currKey++;

            /*  Permute */
            lbyte = (char) SC_V_CRYPT_POU[(int) lbyte];
            rbyte = (char) SC_V_CRYPT_POU[(int) rbyte];

            /*  Join    */
            out[count] = (char) (0x00FF & ((lbyte << 4) | rbyte));

            codePtr++;
            dataPtr++;

            if (crypt.getCurrKey() == crypt.getEndSpace()) {
                crypt.extend();
            }
        }

        String decrypt = new String(out);

        return decrypt;
    }

    /**
     * Encrypt.
     *
     * @param key key
     * @param data data
     *
     * @return encrypted string.
     */
    public static String encrypt(String key, String data) {
        ScSCrypt crypt;

        // need 2 chars for the length
        char[] code = new char[(data.length() * 2) + 2];
        int codePtr;
        int dataPtr;
        char lbyte;
        char rbyte;
        int length;
        int count;

        crypt = new ScSCrypt(key);
        length = data.length();

        /*  Prepare to encrypt data.                                */
        dataPtr = 0;
        codePtr = 0;

        /*  Push input data length on output.                       */
        code[codePtr] = (char) length;
        codePtr++;

        /*  Increment length to account for pushed value.           */
        length++;

        /*  Encrypt the data.                                       */
        for (int i = 0; i < data.length(); i++) {
            /*  Split   */
            lbyte = (char) (data.charAt(dataPtr) >> 4);
            rbyte = (char) (data.charAt(dataPtr) & 0x0F);

            /*  Permute */
            lbyte = (char) SC_V_CRYPT_PIN[lbyte];
            rbyte = (char) SC_V_CRYPT_PIN[rbyte];

            /*  Xor     */
            lbyte ^= crypt.keySpace[crypt.currKey];
            crypt.currKey++;
            rbyte ^= crypt.keySpace[crypt.currKey];
            crypt.currKey++;

            /*  Join    */
            code[codePtr] = (char) (0x00FF & ((lbyte << 4) | rbyte));

            /*  Chain  */
            crypt.chain = (char) ((crypt.chain << 2) & 0x00FF);
            code[codePtr] ^= crypt.chain;
            crypt.chain = code[codePtr];

            codePtr++;
            dataPtr++;

            if (crypt.getCurrKey() == crypt.getEndSpace()) {
                crypt.extend();
            }
        }

        /*  Convert integers to a hex string.                       */
        String out = "";
        dataPtr = 0;

        for (count = 0; count < length; count++) {
            String digits = Integer.toHexString(code[count]).toUpperCase();

            if (digits.length() == 1) {
                digits = "0" + digits;
            }

            out += digits;
        }

        return out;
    }

    /**
     * INNER CLASS
     */
    public static class ScSCrypt {
        /** key space */
        private char[] keySpace;

        /** end space. */
        private int endSpace;

        /** current key */
        private int currKey;

        /** chain */
        private char chain;

        /**
         * Constructor.
         *
         * @param key key.
         */
        public ScSCrypt(String key) {
            String keyPtr;

            /*  Force a valid key.                                          */
            if ((key == null) || (key.length() == 0)) {
                keyPtr = new String(SC_V_CRYPT_DFT_KEY);
            } else {
                keyPtr = key;
            }

            keySpace = new char[(keyPtr.length() * 2)];

            /*  Prepare KeySpace.                                           */
            currKey = 0;

            int lookupIndex;

            for (int i = 0; i < keyPtr.length(); i++) {
                /*  Left side.                                              */
                lookupIndex = keyPtr.charAt(i) >> 4;
                keySpace[currKey] = (char) SC_V_CRYPT_PIN[lookupIndex];
                currKey++;

                /*  Right side.                                             */
                lookupIndex = keyPtr.charAt(i) & 0x0F;
                keySpace[currKey] = (char) SC_V_CRYPT_PIN[lookupIndex];
                currKey++;
            }

            endSpace = currKey;
            currKey = 0;
            chain = SC_M_CRYPT_CHAIN_INIT;
        }

        /**
         * Gets chain.
         *
         * @return chain.
         */
        char getChain() {
            return chain;
        }

        /**
         * Gets end space.
         *
         * @return end space
         */
        int getEndSpace() {
            return endSpace;
        }

        /**
         * Gets current key.
         *
         * @return current key.
         */
        int getCurrKey() {
            return currKey;
        }

        /**
         * Gets key space.
         *
         * @return key space array.
         */
        char[] getKeySpace() {
            return keySpace;
        }

        /**
         * Repermute the key space and resets the current key
         */
        void extend() {
            /* Repermute the key space.          */
            for (int i = 0; i < keySpace.length; i++) {
                keySpace[i] = (char) SC_V_CRYPT_PIN[keySpace[i]];
            }

            /* Reset the current key */
            currKey = 0;
        }
    }
}


/*STC_LOG*
 *******************************************************************************
 *$Log: ScEncrypt.java,v $
 *Revision 1.3  2007/11/18 02:43:05  echou
 *6628188 n global rar wizard for jmsjca
 *
 *Revision 1.1.2.1  2007/11/08 03:40:01  echou
 *global rar inbound enhancement misc
 *
 *Revision 1.1  2003/01/02 20:40:06  omontoya
 *Included code for encrypted strings.
 *
 *Revision 1.3  2002/04/26 18:58:22  dseifert
 *Package change
 *
 *Revision 1.2  2002/03/28 01:29:30  svenkate
 *Merge dev-egate-branch onto the trunk.
 *
 *Revision 1.1.2.1  2002/03/14 18:36:02  rdamir
 *add egate http files
 *
 *Revision 1.3  1999/09/11 00:11:08  pberkman
 *merge to trunk
 *
 *Revision 1.2  1999/08/15 02:45:44  pberkman
 *merge to trunk
 *
 *Revision 1.1.2.1  1999/04/06 23:23:53  andrea
 *encryption code
 *
 *Revision 1.1  1998/11/14 00:54:32  andrea
 *Initial revision
 *
 *
 *******************************************************************************
 *STC_LOG*/
