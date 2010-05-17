/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
                case '\\': // NOI18N
                case '"': // NOI18N
                case '\'': // NOI18N
                    sb.append ('\\'); // NOI18N
                    sb.append (c);
                    break;
                case '\r': // NOI18N
                    sb.append ("\\r"); // NOI18N
                    break;
                case '\n': // NOI18N
                    sb.append ("\\n"); // NOI18N
                    break;
                case '\t': // NOI18N
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
            sb.append ('0'); // NOI18N
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
            if (c != '\\') { // NOI18N
                sb.append (c);
                continue;
            }
            c = value.charAt (i);
            i++;
            switch (c) {
                case 'r': // NOI18N
                    sb.append ('\r'); // NOI18N
                    break;
                case 'n': // NOI18N
                    sb.append ('\n'); // NOI18N
                    break;
                case 't': // NOI18N
                    sb.append ('\t'); // NOI18N
                    break;
                case 'u': // NOI18N
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
                case '"': // NOI18N
                case '\'': // NOI18N
                case '\\': // NOI18N
                    sb.append(c);
                    break;
                default:
                    if (c < '0' || c > '9') { // NOI18N
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
        return string != null  &&  string.length() > 0;
    }

}
