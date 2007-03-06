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

package org.netbeans.modules.iep.editor.tcg.util;

/**
 * This class allows the creation and retrieval of a byte array from a String.
 * Instances of this class are immutable.
 *
 * @author    Bing Lu
 * @created   May 14, 2003
 * @version   1.0
 * @since     1.0
 */
public class BlobUtil {

    /** Encoding. */
    private final static String ENCODING = "ASCII";

    /**
     * Returns the encoded string based on the data provided in a byte arry
     *
     * @param aBytes  - the byte array to encode
     * @return        the encoded string or empty string if the the bye array is
     *      null or empty
     * @since         1.0
     */
    public static String getBase64StringFromBytes(byte[] aBytes) {
        String encodedString = null;

        // if the input is null or empty return empty string
        if ((null == aBytes) || (aBytes.length == 0)) {
            return "";
        }

        try {
            encodedString = Base64.byteArrayToBase64(aBytes);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to encode.");
        }

        return encodedString;
    }


    /**
     * Returns the decoded byte array from the provided string. String is
     * assumed to be base64 encoded
     *
     * @param aBase64String  a base 64 string to be converted to byte array.
     * @return               the byte array or null if aBase64String is null or
     *      empty byte array if aBase64String length is 0
     * @since                1.0
     */
    public static byte[] getBytesFromBase64String(String aBase64String) {
        byte[] decodedBytes = null;

        if (null == aBase64String) {
            return decodedBytes;
        }

        if (aBase64String.length() == 0) {
            return new byte[0];
        }

        try {
            decodedBytes = Base64.base64ToByteArray(aBase64String);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to decode.");
        }

        return decodedBytes;
    }


    /**
     * Description of the Method
     *
     * @param str  Description of the Parameter
     * @return     Description of the Return Value
     */
    public static String encodeBase64(String str) {
        return getBase64StringFromBytes(str.getBytes());
    }

    /**
     * Description of the Method
     *
     * @param str  Description of the Parameter
     * @return     Description of the Return Value
     */
    public static String decodeBase64(String str) {
        return new String(getBytesFromBase64String(str));
    }
}

