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
package org.netbeans.modules.collab.channel.output;

import java.io.*;


/**
 * util for encode and decode filecontent
 *
 * @author  Todd Fast <todd.fast@sun.com>
 * @version 1.0
 */
public class Base64 {
    /**
     *
     *
     */
    private Base64() {
        super();
    }

    /**
     * Encodes an array of bytes into a Base64 string, without compression.
     * Resulting string is uniform with no carriage returns.
     *
     * @param bytes binary data to be encoded
     * @return Base64 encoded string
     */
    public static String encode(byte[] bytes) {
        sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
        String result = encoder.encodeBuffer(bytes);

        // Since the JavaSoft guys put in a carriage return at 57 bytes,
        // we need to remove them so we have one long encoded string
        result = result.replaceAll("\r", "");
        result = result.replaceAll("\n", "");

        return result;
    }

    /**
     * Decodes a string using Base64 decoding into an array of bytes
     * without compression.  Strings not previously Base64 encoded will
     * succeed.
     *
     * @param s string to be decoded (assumed to be Base64 encoded string)
     * @return decoded array of bytes or <code>s.getBytes()</code> on exception during execution
     */
    public static byte[] decode(String s) {
        try {
            sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();

            return decoder.decodeBuffer(s);
        } catch (IOException e) {
            e.printStackTrace();

            // Ignore, use the provided text directly
            return s.getBytes();
        }
    }
}
