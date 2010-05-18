/*
 * BEGIN_HEADER - DO NOT EDIT
 * 
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */

package org.netbeans.modules.edm.editor.utils;

public class ScEncrypt {

    public static class ScSCrypt {
        private char chain;

        private int currKey;

        private int endSpace;
        private char[] keySpace;

        public ScSCrypt(String key) {
            String keyPtr;

            /* Force a valid key. */
            if ((key == null) || (key.length() == 0)) {
                keyPtr = new String(SC_V_CRYPT_DFT_KEY);
            } else {
                keyPtr = key;
            }

            keySpace = new char[(keyPtr.length() * 2)];

            /* Prepare KeySpace. */
            currKey = 0;

            int lookupIndex;

            for (int i = 0; i < keyPtr.length(); i++) {
                /* Left side. */
                lookupIndex = keyPtr.charAt(i) >> 4;
                keySpace[currKey] = (char) SC_V_CRYPT_PIN[lookupIndex];
                currKey++;

                /* Right side. */
                lookupIndex = keyPtr.charAt(i) & 0x0F;
                keySpace[currKey] = (char) SC_V_CRYPT_PIN[lookupIndex];
                currKey++;
            }

            endSpace = currKey;
            currKey = 0;
            chain = SC_M_CRYPT_CHAIN_INIT;
        }

        void extend() {
            /* Repermute the key space. */
            for (int i = 0; i < keySpace.length; i++) {
                keySpace[i] = (char) SC_V_CRYPT_PIN[keySpace[i]];
            }

            /* Reset the current key */
            currKey = 0;
        }

        char getChain() {
            return chain;
        }

        int getCurrKey() {
            return currKey;
        }

        int getEndSpace() {
            return endSpace;
        }

        char[] getKeySpace() {
            return keySpace;
        }
    }

    private static final char SC_M_CRYPT_CHAIN_INIT = (char) 0x72;

    private static final char[] SC_V_CRYPT_DFT_KEY = { (char) 0x83, (char) 0x3A, (char) 0xFC, (char) 0xE9, (char) 0x47, (char) 0x95, (char) 0x21,
            (char) 0x6D, (char) 0xBE, (char) 0x7F, (char) 0xA2, (char) 0x54, (char) 0xC6, (char) 0xDB, (char) 0x18, (char) 0x00};

    private static final short[] SC_V_CRYPT_PIN = { 3, 5, 15, 6, 12, 9, 2, 0, 13, 10, 7, 4, 1, 14, 11, 8};

    private static final short[] SC_V_CRYPT_POU = { 7, 12, 6, 0, 11, 1, 3, 10, 15, 5, 9, 14, 4, 8, 13, 2};


    public static String decrypt(String key, String data) {
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

        /* Prepare to decrypt data. */
        dataPtr = 0;
        codePtr = 0;

        /* Pop data length. */
        length = code[codePtr];
        codePtr++;

        /* Decrypt the data. */
        out = new char[length];

        for (count = 0; count < length; count++) {
            /* Unchain */
            abyte = code[codePtr];

            // we need to keep this to 8 bits
            crypt.chain = (char) ((crypt.chain << 2) & 0x00FF);
            abyte ^= crypt.chain;
            crypt.chain = code[codePtr];

            /* Split */
            lbyte = (char) (abyte >> 4);
            rbyte = (char) (abyte & 0x0F);

            /* Xor */
            lbyte ^= crypt.keySpace[crypt.currKey];
            crypt.currKey++;
            rbyte ^= crypt.keySpace[crypt.currKey];
            crypt.currKey++;

            /* Permute */
            lbyte = (char) SC_V_CRYPT_POU[lbyte];
            rbyte = (char) SC_V_CRYPT_POU[rbyte];

            /* Join */
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

        /* Prepare to encrypt data. */
        dataPtr = 0;
        codePtr = 0;

        /* Push input data length on output. */
        code[codePtr] = (char) length;
        codePtr++;

        /* Increment length to account for pushed value. */
        length++;

        /* Encrypt the data. */
        for (int i = 0; i < data.length(); i++) {
            /* Split */
            lbyte = (char) (data.charAt(dataPtr) >> 4);
            rbyte = (char) (data.charAt(dataPtr) & 0x0F);

            /* Permute */
            lbyte = (char) SC_V_CRYPT_PIN[lbyte];
            rbyte = (char) SC_V_CRYPT_PIN[rbyte];

            /* Xor */
            lbyte ^= crypt.keySpace[crypt.currKey];
            crypt.currKey++;
            rbyte ^= crypt.keySpace[crypt.currKey];
            crypt.currKey++;

            /* Join */
            code[codePtr] = (char) (0x00FF & ((lbyte << 4) | rbyte));

            /* Chain */
            crypt.chain = (char) ((crypt.chain << 2) & 0x00FF);
            code[codePtr] ^= crypt.chain;
            crypt.chain = code[codePtr];

            codePtr++;
            dataPtr++;

            if (crypt.getCurrKey() == crypt.getEndSpace()) {
                crypt.extend();
            }
        }

        /* Convert integers to a hex string. */
        String out = "";

        for (count = 0; count < length; count++) {
            String digits = Integer.toHexString(code[count]).toUpperCase();

            if (digits.length() == 1) {
                digits = "0" + digits;
            }

            out += digits;
        }

        return out;
    }
}
