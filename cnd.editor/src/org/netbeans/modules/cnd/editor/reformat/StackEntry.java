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

package org.netbeans.modules.cnd.editor.reformat;

import org.netbeans.api.lexer.Token;
import org.netbeans.cnd.api.lexer.CppTokenId;
import static org.netbeans.cnd.api.lexer.CppTokenId.*;

/**
 *
 * @author Alexander Simon
 */
class StackEntry {

    private int index;
    private CppTokenId kind;
    private CppTokenId importantKind;
    private boolean likeToFunction = false;
    private boolean likeToArrayInitialization = false;
    private String text;
    private int indent;
    private int selfIndent;

    StackEntry(ExtendedTokenSequence ts) {
        super();
        index = ts.index();
        kind = ts.token().id();
        text = ts.token().text().toString();
        switch (kind) {
            case IF: //("if", "keyword-directive"),
            case ELSE: //("else", "keyword-directive"),
            case TRY: //("try", "keyword-directive"), // C++
            case CATCH: //("catch", "keyword-directive"), //C++
            case WHILE: //("while", "keyword-directive"),
            case FOR: //("for", "keyword-directive"),
            case DO: //("do", "keyword-directive"),
            case ASM: //("asm", "keyword-directive"), // gcc and C++
            case SWITCH: //("switch", "keyword-directive"),
                importantKind = kind;
                break;
            default:
                initImportant(ts);
        }
    }

    private void initImportant(ExtendedTokenSequence ts) {
        int i = ts.index();
        try {
            int paren = 0;
            int curly = 0;
            int triangle = 0;
            while (true) {
                if (!ts.movePrevious()) {
                    return;
                }
                Token<CppTokenId> current = ts.token();
                switch (current.id()) {
                    case RPAREN: //(")", "separator"),
                    {
                        if (paren == 0 && curly == 0 && triangle == 0) {
                            likeToFunction = true;
                        }
                        paren++;
                        break;
                    }
                    case LPAREN: //("(", "separator"),
                    {
                        if (paren == 0) {
                            Token<CppTokenId> prev = ts.lookPreviousImportant();
                            if (prev != null && prev.id() == OPERATOR) {
                                likeToArrayInitialization = false;
                                likeToFunction = true;
                                return;
                            }
                            likeToArrayInitialization = true;
                            return;
                        }
                        paren--;
                        break;
                    }
                    case CASE:
                    case DEFAULT:
                    {
                        if (paren == 0 && curly == 0 && triangle == 0) {
                            likeToArrayInitialization = false;
                            likeToFunction = false;
                            return;
                        }
                        break;
                    }
                    case RBRACE: //("}", "separator"),
                    case LBRACE: //("{", "separator"),
                    case SEMICOLON: //(";", "separator"),
                    {
                        if (paren == 0 && curly == 0 && triangle == 0) {
                            // undefined
                            return;
                        }
                        break;
                    }
                    case EQ: //("=", "operator"),
                    {
                        if (paren == 0) {
                            Token<CppTokenId> prev = ts.lookPreviousImportant();
                            if (prev != null && prev.id() == OPERATOR) {
                                likeToArrayInitialization = false;
                                likeToFunction = true;
                                return;
                            }
                            likeToArrayInitialization = true;
                            likeToFunction = false;
                            return;
                        }
                        break;
                    }
                    case GT: //(">", "operator"),
                    {
                        if (paren == 0 && curly == 0) {
                            Token<CppTokenId> prev = ts.lookPreviousImportant();
                            if (prev != null && prev.id() == OPERATOR) {
                                likeToArrayInitialization = false;
                                likeToFunction = true;
                                return;
                            }
                            triangle++;
                        }
                        break;
                    }
                    case LT: //("<", "operator"),
                    {
                        if (paren == 0 && curly == 0) {
                            if (triangle == 0) {
                            Token<CppTokenId> prev = ts.lookPreviousImportant();
                                if (prev != null && prev.id() == OPERATOR) {
                                    likeToArrayInitialization = false;
                                    likeToFunction = true;
                                    return;
                                }
                                // undefined
                                return;
                            }
                            triangle--;
                        }
                        break;
                    }
                    case NAMESPACE: //("namespace", "keyword"), //C++
                    case CLASS: //("class", "keyword"), //C++
                    {
                        if (paren == 0 && curly == 0 && triangle == 0) {
                            importantKind = current.id();
                            likeToFunction = false;
                            return;
                        }
                        break;
                    }
                    case STRUCT: //("struct", "keyword"),
                    case ENUM: //("enum", "keyword"),
                    case UNION: //("union", "keyword"),
                    {
                        if (paren == 0 && curly == 0 && triangle == 0) {
                            if (!likeToFunction) {
                                importantKind = current.id();
                                return;
                            }
                        }
                        break;
                    }
                    case EXTERN: //EXTERN("extern", "keyword"),
                    {
                        if (paren == 0 && curly == 0 && triangle == 0) {
                            if (!likeToFunction) {
                                importantKind = CppTokenId.NAMESPACE;
                                return;
                            }
                        }
                        break;
                    }
                    case IF: //("if", "keyword-directive"),
                    case ELSE: //("else", "keyword-directive"),
                    case SWITCH: //("switch", "keyword-directive"),
                    case WHILE: //("while", "keyword-directive"),
                    case DO: //("do", "keyword-directive"),
                    case FOR: //("for", "keyword-directive"),
                    case TRY: //("try", "keyword-directive"), // C++
                    case CATCH: //("catch", "keyword-directive"), //C++
                    {
                        if (paren == 0 && curly == 0 && triangle == 0) {
                            importantKind = current.id();
                            likeToFunction = false;
                            return;
                        }
                        break;
                    }
                }
            }
        } finally {
            ts.moveIndex(i);
            ts.moveNext();
        }
    }

    public int getIndent(){
        return indent;
    }

    public void setIndent(int indent){
        this.indent = indent;
    }

    public int getSelfIndent(){
        return selfIndent;
    }

    public void setSelfIndent(int selfIndent){
        this.selfIndent = selfIndent;
    }
    
    public int getIndex() {
        return index;
    }
    
    public String getText() {
        return text;
    }

    public CppTokenId getKind() {
        return kind;
    }

    public CppTokenId getImportantKind() {
        return importantKind;
    }

    public boolean isLikeToFunction() {
        return likeToFunction;
    }

    public void setLikeToFunction(boolean likeToFunction) {
        this.likeToFunction = likeToFunction;
    }

    public boolean isLikeToArrayInitialization() {
        return likeToArrayInitialization;
    }

    public void setLikeToArrayInitialization(boolean likeToArrayInitialization) {
        this.likeToArrayInitialization = likeToArrayInitialization;
    }

    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder(kind.name());
        if (importantKind != null && kind != importantKind){
            buf.append("(").append(importantKind.name()).append(")"); // NOI18N
        } else if (likeToFunction) {
            buf.append("(FUNCTION)"); // NOI18N
        } else if (likeToArrayInitialization) {
            buf.append("(ARRAY_INITIALIZATION)"); // NOI18N
        }
        return buf.toString();
    }
}
