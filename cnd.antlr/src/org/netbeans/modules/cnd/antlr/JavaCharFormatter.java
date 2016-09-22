/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.antlr;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

class JavaCharFormatter implements CharFormatter {


    /** Given a character value, return a string representing the character
     * that can be embedded inside a string literal or character literal
     * This works for Java/C/C++ code-generation and languages with compatible
     * special-character-escapment.
     * Code-generators for languages should override this method.
     * @param c   The character of interest.
     * @param forCharLiteral  true to escape for char literal, false for string literal
     */
    public String escapeChar(int c, boolean forCharLiteral) {
        switch (c) {
            //		case GrammarAnalyzer.EPSILON_TYPE : return "<end-of-token>";
            case '\n':
                return "\\n";
            case '\t':
                return "\\t";
            case '\r':
                return "\\r";
            case '\\':
                return "\\\\";
            case '\'':
                return forCharLiteral ? "\\'" : "'";
            case '"':
                return forCharLiteral ? "\"" : "\\\"";
            default :
                if (c < ' ' || c > 126) {
                    if ((0x0000 <= c) && (c <= 0x000F)) {
                        return "\\u000" + Integer.toString(c, 16);
                    }
                    else if ((0x0010 <= c) && (c <= 0x00FF)) {
                        return "\\u00" + Integer.toString(c, 16);
                    }
                    else if ((0x0100 <= c) && (c <= 0x0FFF)) {
                        return "\\u0" + Integer.toString(c, 16);
                    }
                    else {
                        return "\\u" + Integer.toString(c, 16);
                    }
                }
                else {
                    return String.valueOf((char)c);
                }
        }
    }

    /** Converts a String into a representation that can be use as a literal
     * when surrounded by double-quotes.
     * @param s The String to be changed into a literal
     */
    public String escapeString(String s) {
        String retval = new String();
        for (int i = 0; i < s.length(); i++) {
            retval += escapeChar(s.charAt(i), false);
        }
        return retval;
    }

    /** Given a character value, return a string representing the character
     * literal that can be recognized by the target language compiler.
     * This works for languages that use single-quotes for character literals.
     * Code-generators for languages should override this method.
     * @param c   The character of interest.
     */
    public String literalChar(int c) {
        return "'" + escapeChar(c, true) + "'";
    }

    /** Converts a String into a string literal
     * This works for languages that use double-quotes for string literals.
     * Code-generators for languages should override this method.
     * @param s The String to be changed into a literal
     */
    public String literalString(String s) {
        return "\"" + escapeString(s) + "\"";
    }
}
