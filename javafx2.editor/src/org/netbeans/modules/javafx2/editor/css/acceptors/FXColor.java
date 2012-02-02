/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.javafx2.editor.css.acceptors;

//import org.netbeans.modules.css.editor.properties.CssPropertyValueAcceptor;
//import org.netbeans.modules.css.editor.properties.CustomErrorMessageProvider;

// XXX with the new version of css.lib I won't probably need it

/**
 *
 * @author Anton Chechel <anton.chechel@oracle.com>
 */
// TODO other color definition:
// color=named-color | <looked-up-color> | <rgb-color> | <hsb-color> | <color-function>
// color-stop=[!color [ !percentage | !fx-length]?]
// looked-up-color=+
// rgb-color=#[!digit][!digit][!digit] | #[!digit][!digit][!digit][!digit][!digit][!digit] |\
// rgb(!integer, !integer, !integer) | rgb(!percentage, !percentage, !percentage) |\
// rgba(!integer, !integer, !integer, !number) | rgba(!percentage, !percentage, !percentage, !number)
// hsb-color=hsb(!number, !percentage, !percentage) | hsba(!number, !percentage, !percentage, !number)
//public class FXColor implements CssPropertyValueAcceptor, CustomErrorMessageProvider {
public class FXColor {

    private static final String[] COLORS = new String[]{"aliceblue", "antiquewhite", "aqua", //NOI18N
        "aquamarine", "azure", "beige", "bisque", "black", "blanchedalmond", "blue", "blueviolet", //NOI18N
        "brown", "burlywood", "cadetblue", "chartreuse", "chocolate", "coral", "cornflowerblue", //NOI18N
        "cornsilk", "crimson", "cyan", "darkblue", "darkcyan", "darkgoldenrod", "darkgray", //NOI18N
        "darkgreen", "darkgrey", "darkkhaki", "darkmagenta", "darkolivegreen", "darkorange", //NOI18N
        "darkorchid", "darkred", "darksalmon", "darkseagreen", "darkslateblue", "darkslategray", //NOI18N
        "darkslategrey", "darkturquoise", "darkviolet", "deeppink", "deepskyblue", "dimgray", //NOI18N
        "dimgrey", "dodgerblue", "firebrick", "floralwhite", "forestgreen", "fuchsia", "gainsboro", //NOI18N
        "ghostwhite", "gold", "goldenrod", "gray", "green", "greenyellow", "grey", "honeydew", "hotpink", //NOI18N
        "indianred", "indigo", "ivory", "khaki", "lavender", "lavenderblush", "lawngreen", "lemonchiffon", //NOI18N
        "lightblue", "lightcoral", "lightcyan", "lightgoldenrodyellow", "lightgray", "lightgreen", //NOI18N
        "lightgrey", "lightpink", "lightsalmon", "lightseagreen", "lightskyblue", "lightslategray", //NOI18N
        "lightslategrey", "lightsteelblue", "lightyellow", "lime", "limegreen", "linen", "magenta", //NOI18N
        "maroon", "mediumaquamarine", "mediumblue", "mediumorchid", "mediumpurple", "mediumseagreen", //NOI18N
        "mediumslateblue", "mediumspringgreen", "mediumturquoise", "mediumvioletred", "midnightblue", //NOI18N
        "mintcream", "mistyrose", "moccasin", "navajowhite", "navy", "oldlace", "olive", "olivedrab", //NOI18N
        "orange", "orangered", "orchid", "palegoldenrod", "palegreen", "paleturquoise", "palevioletred", //NOI18N
        "papayawhip", "peachpuff", "peru", "pink", "plum", "powderblue", "purple", "red", "rosybrown", //NOI18N
        "royalblue", "saddlebrown", "salmon", "sandybrown", "seagreen", "seashell", "sienna", "silver", //NOI18N
        "skyblue", "slateblue", "slategray", "slategrey", "snow", "springgreen", "steelblue", "tan", //NOI18N
        "teal", "thistle", "tomato", "turquoise", "violet", "wheat", "white", "whitesmoke", "yellow", //NOI18N
        "yellowgreen", "transparent"}; //NOI18N
    
    private String errorMsg = null;

//    @Override
    public String id() {
        return "fx-color"; //NOI18N
    }

//    @Override
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
                    if (!(Character.isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) { //NOI18N
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
        } else {
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

//    @Override
    public String customErrorMessage() {
        return errorMsg;
    }
}
