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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package ElsaResultAnalyser;

import java.io.BufferedReader;
import java.io.FileInputStream;

/**
 *
 * @author nk220367
 */
public class Parser {

    Lexer l;

    Parser(BufferedReader s) {
        l = new Lexer(s);
        l.getNextToken();
    }

    public AstNode parse() {
        return node();
    }

    void matchId() {
        if (l.getCurrentToken().type != Token.TT.TOKEN_ID) {
            System.out.println("id expected");
        }
    }

    void match(String s) {
        if (!l.getCurrentToken().name.equals(s)) {
            System.out.println(s + " expected");
        }
    }

    AstNode node() {
        AstNode node = new AstNode();

        node.line = l.getCurrentToken().line;
        
        // id
        //matchId();
        node.name = l.getCurrentToken().name;
        l.getNextToken();

        // id[1]
        if (l.getCurrentToken().name.equals("[")) {
            l.getNextToken();
            if (l.getCurrentToken().type == Token.TT.TOKEN_DIGIT) {
                l.getNextToken();
                if (l.getCurrentToken().name.equals("]")) {
                    l.getNextToken();
                }
            }
        }

        // id->id
        if (l.getCurrentToken().name.equals("->")) {
            l.getNextToken();
            if (l.getCurrentToken().type == Token.TT.TOKEN_ID) {
                l.getNextToken();
            }
        }

        if (l.getCurrentToken().name.equals("=")) { // id =

            if (l.isEndOfLine()) {
                l.getNextToken();
                return node;
            }
            l.getNextToken();

            node.value = l.getCurrentToken().name;
            if (l.isEndOfLine()) {

                int indent = l.getIndent();
                l.getNextToken();
                if (indent < l.getIndent()) {
                    indent = l.getIndent();
                    while (indent == l.getIndent() && !l.getCurrentToken().name.equals("")) {
                        node.children.add(node());
                    }
                }                
                return node;
            }
            l.getNextToken();

            if (l.getCurrentToken().name.equals(":")) { // id = id:

                l.getNextToken();
                int indent = l.getIndent();
                while (indent == l.getIndent() && !l.getCurrentToken().name.equals("")) {
                    node.children.add(node());
                }
            } else { // id = string

                node.value += l.getCurrentToken().name;
                node.value += l.getEndOfString().name;

                int indent = l.getIndent();
                l.getNextToken();
                if (indent < l.getIndent()) {
                    indent = l.getIndent();
                    while (indent == l.getIndent() && !l.getCurrentToken().name.equals("")) {
                        node.children.add(node());
                    }
                }
            }
        } else if (l.getCurrentToken().name.equals(":")) { // id:

            if (l.isEndOfLine()) { // id:\n

                int indent = l.getIndent();
                l.getNextToken();
                if (indent < l.getIndent()) {
                    indent = l.getIndent();
                    while (indent == l.getIndent() && !l.getCurrentToken().name.equals("")) {
                        node.children.add(node());
                    }
                }
            } else { // id: string
                l.getNextToken();
                node.value = l.getCurrentToken().name + l.getEndOfString().name;
                int indent = l.getIndent();
                l.getNextToken();
                if (indent < l.getIndent()) {
                    indent = l.getIndent();
                    while (indent == l.getIndent() && !l.getCurrentToken().name.equals("")) {
                        node.children.add(node());
                    }
                }
            }
        } else if (l.getCurrentToken().name.equals("is")) { // id is string

            l.getNextToken();
            node.value = l.getCurrentToken().name + l.getEndOfString().name;
            l.getNextToken();
        }

        return node;
    }
}
