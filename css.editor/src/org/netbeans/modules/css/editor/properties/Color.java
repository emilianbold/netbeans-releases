/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor.properties;

import java.util.StringTokenizer;

/**
 *  I18N note - some messages here are not localized since the code is not used so far
 * 
 * 
 * @author marekfukala
 */
public class Color implements CssPropertyValueAcceptor, CustomErrorMessageProvider {

    private static final String[] COLORS = new String[]{"aqua", "black", "blue", "fuchsia", "gray", "green", "lime", "maroon", "navy", "olive", "orange", "purple", "red", "silver", "teal", "white", "yellow]", "inherit"}; //NOI18N

    private String errorMsg = null;
    
    public String id() {
        return "color"; //NOI18N
    }

    public boolean accepts(String token) {
        if (token.length() == 0) {
            return true;
        }

        //text hexa values
        if (token.startsWith("#")) { //NOI18N
            String number = token.substring(1);
            if (number.length() == 3 || number.length() == 6) {
                for (int i = 0; i < number.length(); i++) {
                    char c = number.charAt(i);
                    if (!(Character.isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) {
                        //error
                        errorMsg = "Unexpected character '" + c + "' in haxadecimal color value";
                        return false;
                    }
                }
            } else {
                //error
                errorMsg = "Unexpected length of hexadecimal color definition";
                return false;
            }
        } /*else if (token.startsWith("rgb") || token.startsWith("RGB")) {
            // rgb(255,0,0) 
            int braceIndex = token.indexOf('(');
            int closeBraceIndex = token.indexOf(')');

            if (braceIndex < 0 || closeBraceIndex < 0) {
                errorMsg = "Incorrect rgb definition format";
                return false;
            }

            String content = token.substring(braceIndex + 1, closeBraceIndex);
            StringTokenizer st = new StringTokenizer(content, ",");
            int tokens = 0;
            while (st.hasMoreTokens()) {
                tokens++;
                String value = st.nextToken().trim();
                if (value.endsWith("%")) {
                    try {
                        float f = Float.parseFloat(value.substring(0, value.length() - 2));
                        if (f < 0 || f > 100) {
                            errorMsg = "Value " + f + " out of range";
                            return false;
                        }
                    } catch (NumberFormatException nfe) {
                        errorMsg = "Incorrect number format";
                        return false;
                    }
                } else {
                    try {
                        int i = Integer.parseInt(value);
                        if (i < 0 || i > 0xff) {
                            errorMsg = "Value " + i + " out of range";
                            return false;
                        }
                    } catch (NumberFormatException nfe) {
                        errorMsg = "Incorrect number format";
                        return false;
                    }
                }

            }
            
            if(tokens != 3) {
                errorMsg = "Incorrect number of rgb parameters";
                return false;
            }


        }*/ else {
            //test predefined text values
            for (int i = 0; i < COLORS.length; i++) {
                if (COLORS[i].equalsIgnoreCase(token)) {
                    return true;
                }
            }

            errorMsg = "Invalid color name";
            return false;
        }

        return true;
    }

    public String customErrorMessage() {
        return errorMsg;
    }
}

