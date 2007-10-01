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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.proxy;

import java.io.ByteArrayOutputStream;

/**
 * Bas64 encode utility class.
 *
 * @author Maros Sandor
 */
public class Base64Encoder {

    private static final char [] characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    private Base64Encoder() {
    }

    public static String encode(byte [] data) {
        return encode(data, false);
    }
    
    public static String encode(byte [] data, boolean useNewlines) {
        int length = data.length;
        StringBuffer sb = new StringBuffer(data.length * 3 / 2);

        int end = length - 3;
        int i = 0;
        int lineCount = 0;

        while (i <= end) {
            int d = ((((int) data[i]) & 0xFF) << 16) | ((((int) data[i + 1]) & 0xFF) << 8) | (((int) data[i + 2]) & 0xFF);
            sb.append(characters[(d >> 18) & 0x3F]);
            sb.append(characters[(d >> 12) & 0x3F]);
            sb.append(characters[(d >> 6) & 0x3F]);
            sb.append(characters[d & 0x3F]);
            i += 3;
            
            if (useNewlines && lineCount++ >= 14) {
                lineCount = 0;
                sb.append(System.getProperty("line.separator"));
            }
        }

        if (i == length - 2) {
            int d = ((((int) data[i]) & 0xFF) << 16) | ((((int) data[i + 1]) & 0xFF) << 8);
            sb.append(characters[(d >> 18) & 0x3F]);
            sb.append(characters[(d >> 12) & 0x3F]);
            sb.append(characters[(d >> 6) & 0x3F]);
            sb.append("=");
        } else if (i == length - 1) {
            int d = (((int) data[i]) & 0xFF) << 16;
            sb.append(characters[(d >> 18) & 0x3F]);
            sb.append(characters[(d >> 12) & 0x3F]);
            sb.append("==");
        }
        return sb.toString();
    }
    
    public static byte [] decode(String s) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        decode(s, bos);
        return bos.toByteArray();
}
  
    private static void decode(String s, ByteArrayOutputStream bos) {
        int i = 0;
        int len = s.length();
        for (;;) {
            while (i < len && s.charAt(i) <= ' ') i++;
            if (i == len) break;
            int tri = (decode(s.charAt(i)) << 18) + (decode(s.charAt(i+1)) << 12) + (decode(s.charAt(i+2)) << 6) + (decode(s.charAt(i+3)));
            bos.write((tri >> 16) & 255);
            if (s.charAt(i+2) == '=') break;
            bos.write((tri >> 8) & 255);
            if (s.charAt(i+3) == '=') break;
            bos.write(tri & 255);
            i += 4;
        }
    }

    private static int decode(char c) {
        if (c >= 'A' && c <= 'Z') {
            return ((int) c) - 65;
        } else if (c >= 'a' && c <= 'z') {
            return ((int) c) - 97 + 26;
        } else if (c >= '0' && c <= '9') {
            return ((int) c) - 48 + 26 + 26;
        } else switch (c) {
            case '+': 
                return 62;
            case '/': 
                return 63;
            case '=': 
                return 0;
            default:
                throw new RuntimeException("Base64: unexpected code: " + c);
        }
    }
}
