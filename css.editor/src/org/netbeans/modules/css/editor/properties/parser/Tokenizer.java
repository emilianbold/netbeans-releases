/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor.properties.parser;

import java.util.Stack;

/**
 *
 * @author marekfukala
 */
public class Tokenizer {
    
    public static Stack<String> tokenize(String input) {
        //this semi-lexer started as three lines code and evolved to this
        //ugly beast. Should be recoded to normal state lexing.
        Stack<String> stack = new Stack<String>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i
                < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '\'' || c == '"') {
                //quoted values needs to be one token
                sb.append(c); //add the quotation mark into the value
                for (i++; i
                        < input.length(); i++) {
                    c = input.charAt(i);

                    if (c == '\'' || c == '"') {
                        break;
                    } else {
                        sb.append(c);
                    }

                }
                sb.append(c); //add the quotation mark into the value
                stack.add(0, sb.toString());
                sb =
                        new StringBuilder();

            } else if (sb.toString().equalsIgnoreCase("url") && c == '(') { //NOI18N 
                //store separate tokens: URL + ( + ..... + )
                stack.add(0, sb.toString());
                stack.add(0, "" + c); //NOI18N

                sb = new StringBuilder();
                //make one token until ) found
                for (i++; i
                        < input.length(); i++) {
                    c = input.charAt(i);

                    if (c == ')') {
                        break;
                    } else {
                        sb.append(c);
                    }

                }

                stack.add(0, sb.toString());
                stack.add(0, "" + c); //add the quotation mark into the value  //NOI18N
                sb = new StringBuilder();

            } else if (c == ' ' || c == '\t' || c == '\n') {
                if (sb.length() > 0) {
                    stack.add(0, sb.toString());
                    sb =
                            new StringBuilder();
                }
//skip other potential whitespaces
                for (; i
                        < input.length(); i++) {
                    c = input.charAt(i);
                    if (c != ' ' || c != '\t') {
                        break;
                    }

                }

            } else {
                //handling of chars which are both delimiters and values
                if (c == ',' || c == '/' || c == '(' || c == ')') {
                    if (sb.length() > 0) {
                        stack.add(0, sb.toString());
                    }

                    stack.add(0, "" + c); //NOI18N

                    sb =
                            new StringBuilder();
                } else {
                    sb.append(c);
                }

            }
        }

        //value before eof
        if (sb.length() > 0) {
            stack.add(0, sb.toString());
        }
        
        return stack;

    }
}
