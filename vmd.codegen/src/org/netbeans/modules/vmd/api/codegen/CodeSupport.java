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
package org.netbeans.modules.vmd.api.codegen;

import org.openide.ErrorManager;

/**
 * @author David Kaspar
 */
public final class CodeSupport {

    private CodeSupport () {
    }

    public static String encryptStringToJavaCode (String value) {
        if (value == null)
            return null;
        StringBuffer sb = new StringBuffer ();
        for (int i = 0; i < value.length (); i++) {
            final char c = value.charAt (i);
            switch (c) {
                case '\\':
                case '"':
                case '\'':
                    sb.append ('\\');
                    sb.append (c);
                    break;
                case '\r':
                    sb.append ("\\r"); // NOI18N
                    break;
                case '\n':
                    sb.append ("\\n"); // NOI18N
                    break;
                case '\t':
                    sb.append ("\\t"); // NOI18N
                    break;
                default:
                    if (c < 32) {
                        sb.append ("\\"); // NOI18N
                        sb.append (alignWithZeros (Integer.toOctalString (c), 3));
                    } else if (c >= 128) {
                        sb.append ("\\u"); // NOI18N
                        sb.append (alignWithZeros (Integer.toHexString (c).toUpperCase (), 4));
                    } else
                        sb.append (c);
            }
        }
        return sb.toString ();
    }

    public static String alignWithZeros (String value, int positions) {
        if (value == null)
            return null;
        StringBuffer sb = new StringBuffer ();
        positions -= value.length ();
        while (positions > 0) {
            sb.append ('0');
            positions --;
        }
        if (positions < 0) // just to asure alignment to specified positions
            return value.substring (-positions);
        return sb.append (value).toString ();
    }

    public static String decryptStringFromJavaCode (String value) {
        if (value == null)
            return null;
        final int len = value.length ();
        StringBuffer sb = new StringBuffer ();
        int i = 0;
        while (i < len) {
            char c = value.charAt (i);
            i++;
            if (c != '\\') {
                sb.append (c);
                continue;
            }
            c = value.charAt (i);
            i++;
            switch (c) {
                case 'r':
                    sb.append ('\r');
                    break;
                case 'n':
                    sb.append ('\n');
                    break;
                case 't':
                    sb.append ('\t');
                    break;
                case 'u':
                    if (i + 4 > len) {
                        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "WARNING: Invalid hex number at the end: " + value.substring (i)); // NOI18N
                        break;
                    }
                    try {
                        sb.append ((char) Integer.parseInt (value.substring (i, i + 4), 16));
                    } catch (NumberFormatException e) {
                        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "WARNING: Invalid hex number format: " + value.substring (i, i + 4)); // NOI18N
                    }
                    i += 4;
                    break;
                case '"':
                case '\'':
                case '\\':
                    sb.append(c);
                    break;
                default:
                    if (c < '0' || c > '9') {
                        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "WARNING: Invalid character after slash: " + c); // NOI18N
                        break;
                    }
                    i--;
                    if (i + 3 > len) {
                        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "WARNING: Invalid octal number at the end: " + value.substring (i)); // NOI18N
                        break;
                    }
                    try {
                        sb.append ((char) Integer.parseInt (value.substring (i, i + 3), 8));
                    } catch (NumberFormatException e) {
                        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "WARNING: Invalid octal number format: " + value.substring (i, i + 3)); // NOI18N
                    }
                    i += 3;
            }
        }
        return sb.toString ();
    }

    public static int compareStrings (String s1, String s2) {
        if (s1 != null)
            return s2 != null ? s1.compareTo (s2) : 1;
        else
            return s2 != null ? -1 : 0;
    }

    public static boolean isNotEmpty (String string) {
        return string != null  &&  ! "".equals (string); // NOI18N
    }

}
