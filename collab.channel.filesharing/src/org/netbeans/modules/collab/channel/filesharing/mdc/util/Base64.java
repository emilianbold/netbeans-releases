/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.channel.filesharing.mdc.util;

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
