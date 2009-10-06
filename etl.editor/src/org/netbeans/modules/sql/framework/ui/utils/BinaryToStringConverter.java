/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.sql.framework.ui.utils;

public class BinaryToStringConverter {

    static class ConversionConstants {
        int radix; // the base radix
        int width; // number of chars used to represent byte

        ConversionConstants(int w, int r) {
            width = w;
            radix = r;
        }
    }
    public static final int BINARY = 2;
    public static final int DECIMAL = 10;
    public static final int HEX = 16;

    public static final int OCTAL = 8;

    static ConversionConstants decimal = new ConversionConstants(3, 10);
    static ConversionConstants hex = new ConversionConstants(2, 16);
    private static ConversionConstants binary = new ConversionConstants(8, 2);
    private static ConversionConstants octal = new ConversionConstants(3, 8);

    /**
     * List of characters considered "printable".
     */
    private static String printable = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ`~!@#$%^&*()-_=+[{]}\\|;:'\",<.>/?";

    /**
     * Convert from an array of Bytes into a string.
     */
    public static String convertToString(Byte[] data, int base, boolean showAscii) {

        if (data == null) {
            return null;
        }

        StringBuilder buf = new StringBuilder(20);
        ConversionConstants convConst = getConstants(base);

        // Convert each byte and put into string buffer
        for (int i = 0; i < data.length; i++) {
            int value = data[i].byteValue();
            String s = null;

            // if user wants to see ASCII chars as characters,
            // see if this is one that should be displayed that way
            if (showAscii) {
                if (printable.indexOf((char) value) > -1) {
                    s = new Character((char) value) + "          ".substring(10 - (convConst.width - 1));
                }
            }

            // if user is not looking for ASCII chars, or if this one is one that
            // is not printable, then convert it into numeric form
            if (s == null) {
                switch (base) {
                    case DECIMAL:
                        // convert signed to unsigned
                        if (value < 0) {
                            value = 256 + value;
                        }
                        s = Integer.toString(value);
                        break;
                    case OCTAL:
                        s = Integer.toOctalString(value);
                        break;
                    case BINARY:
                        s = Integer.toBinaryString(value);
                        break;
                    case HEX: // fall through to default
                    default:
                        s = Integer.toHexString(value);
                }
                // some formats (e.g. hex & octal) extend a negative number to multiple
                // places (e.g. FC becomes FFFC), so chop off extra stuff in front
                if (s.length() > convConst.width)
                    s = s.substring(s.length() - convConst.width);

                // front pad with zeros and add to output
                if (s.length() < convConst.width) {
                    buf.append("00000000".substring(8 - (convConst.width - s.length())));
                }
            }
            buf.append(s);
            buf.append("  "); // always add spaces at end for consistancy
        }
        return buf.toString();
    }

    /**
     * Get the constants to use for the given base.
     */
    private static ConversionConstants getConstants(int base) {
        switch (base) {
            case DECIMAL:
                return decimal;
            case OCTAL:
                return octal;
            case BINARY:
                return binary;
            case HEX: // default to hex if unknown base passed in
            default:
                return hex;
        }
    }

    /**
     * Do not allow any instances to be created.
     */
    private BinaryToStringConverter() {
    }
}

