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
    
}
