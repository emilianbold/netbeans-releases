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
package org.netbeans.modules.php.editor.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.impl.ModelVisitor;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;

/**
 * @author Radek Matous
 */
public class CompletionSupport {

    private ModelVisitor modelVisitor;
    private Document document;
    private int offset;

    CompletionSupport(ModelVisitor modelVisitor, Document document, int offset) {
        this.modelVisitor = modelVisitor;
        this.document = document;
        this.offset = offset;
    }
    private static final Collection<PHPTokenId> CTX_DELIMITERS = Arrays.asList(
            PHPTokenId.PHP_SEMICOLON, PHPTokenId.PHP_CURLY_OPEN, PHPTokenId.PHP_CURLY_CLOSE,
            PHPTokenId.PHP_RETURN, PHPTokenId.PHP_OPERATOR, PHPTokenId.PHP_ECHO,
            PHPTokenId.PHP_EVAL, PHPTokenId.PHP_NEW, PHPTokenId.PHP_NOT, PHPTokenId.PHP_CASE,
            PHPTokenId.PHP_IF, PHPTokenId.PHP_ELSE, PHPTokenId.PHP_ELSEIF, PHPTokenId.PHP_PRINT,
            PHPTokenId.PHP_FOR, PHPTokenId.PHP_FOREACH, PHPTokenId.PHP_WHILE,
            PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_LINE_COMMENT,
            PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING, PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE);

    private enum State {

        START, INVALID, VARBASE, DOLAR, PARAMS, REFERENCE, STATIC_REFERENCE, METHOD, FIELD, VARIABLE, CLASSNAME, STOP
    };


    //TODO: (just halfway) instead return ModelElements for code completion
    public  List<? extends TypeScope> getClassMemberType() {
        return getClassMemberTypegetType(offset);
    }

    private  List<? extends TypeScope> getClassMemberTypegetType(final int offset) {
        ModelScope modelScope = modelVisitor.getModelScope();
        VariableScope nearestVariableScope = modelVisitor.getNearestVariableScope(offset);

        List<? extends TypeScope> emptyRetval = Collections.emptyList();
        if (modelScope == null || nearestVariableScope == null) {
            return emptyRetval;
        }
        List<? extends TypeScope> retval = emptyRetval;
        TokenSequence<PHPTokenId> tokenSequence = LexUtilities.getPHPTokenSequence(document, offset);

        if (moveToOffset(tokenSequence, offset)) {
            return emptyRetval;
        }

        State state = State.START;
        int leftBraces = 0;
        int rightBraces = 0;
        StringBuilder sb = new StringBuilder();
        while (!state.equals(State.INVALID) && !state.equals(State.STOP) && canContinueAfterSkipingWhitespaces(tokenSequence)) {
            Token<PHPTokenId> token = tokenSequence.token();
            if (!CTX_DELIMITERS.contains(token.id())) {
                switch (state) {
                    case START:
                        state = State.INVALID;
                        if (isReference(token)) {
                            state = State.REFERENCE;
                        } else if (isStaticReference(token)) {
                            state = State.STATIC_REFERENCE;
                        }
                        break;
                    case REFERENCE:
                        state = State.INVALID;
                        if (isLeftBracket(token)) {
                            leftBraces++;
                            state = State.PARAMS;
                        } else if (isString(token)) {
                            sb.insert(0, token.text().toString());
                            state = State.FIELD;
                        } else if (isVariable(token)) {
                            sb.insert(0, token.text().toString());
                            state = State.VARBASE;
                        }
                        break;
                    case STATIC_REFERENCE:
                        state = State.INVALID;
                        if (isString(token)) {
                            sb.insert(0, token.text().toString());
                            state = State.CLASSNAME;
                        } else if (isSelf(token)) {
                            sb.insert(0, buildStaticClassName(nearestVariableScope, token.text().toString()));
                            //TODO: maybe rather introduce its own State
                            state = State.CLASSNAME;
                        }
                        break;
                    case PARAMS:
                        state = State.INVALID;
                        if (CTX_DELIMITERS.contains(token.id())) {
                            state = State.INVALID;
                        } else if (isLeftBracket(token)) {
                            leftBraces++;
                        } else if (isRightBracket(token)) {
                            rightBraces++;
                        }
                        if (leftBraces == rightBraces) {
                            state = State.METHOD;
                        }
                        break;
                    case METHOD:
                        state = State.INVALID;
                        if (isString(token)) {
                            sb.insert(0, token.text().toString());
                            sb.insert(0, "@" + VariousUtils.METHOD_TYPE_PREFIX);
                            state = State.START;
                        }
                        break;
                    case FIELD:
                        state = State.INVALID;
                        if (isReference(token)) {
                            sb.insert(0, "@" + VariousUtils.FIELD_TYPE_PREFIX);
                            state = State.REFERENCE;
                        }
                        break;
                    case VARBASE:
                        state = State.INVALID;
                        if (isStaticReference(token)) {
                            state = State.STATIC_REFERENCE;
                            break;
                        } else {
                            state = State.VARIABLE;
                        }

                    case VARIABLE:
                        sb.insert(0, "@" + VariousUtils.VAR_TYPE_PREFIX);
                    case CLASSNAME:
                        //TODO: self, parent not handled yet
                        //TODO: maybe rather introduce its own State for self, parent
                        state = State.STOP;
                        break;
                }
            }
        }
        if (state.equals(State.STOP)) {
            String typeName = sb.toString();
            return VariousUtils.getType(modelScope, nearestVariableScope, typeName, offset, false);
        }

        return retval;
    }

    private static String buildStaticClassName(Scope scp, String staticClzName) {
        if (scp instanceof MethodScope) {
            MethodScope msi = (MethodScope) scp;
            ClassScope csi = (ClassScope) msi.getInScope();
            if ("self".equals(staticClzName)) {
                staticClzName = csi.getName();
            } else if ("parent".equals(staticClzName)) {
                ClassScope clzScope = ModelUtils.getFirst(csi.getSuperClasses());
                if (clzScope != null) {
                    staticClzName = clzScope.getName();
                }
            }
        }
        return staticClzName;
    }

    private static boolean canContinueAfterSkipingWhitespaces(TokenSequence<PHPTokenId> tokenSequence) {
        Token<PHPTokenId> token = tokenSequence.token();
        while (token != null && token.id() == PHPTokenId.WHITESPACE) {
            boolean retval = tokenSequence.movePrevious();
            if (!retval) {
                return false;
            }
        }
        return tokenSequence.movePrevious();
    }

    private boolean moveToOffset(TokenSequence<PHPTokenId> tokenSequence, final int offset) {
        return tokenSequence == null || tokenSequence.move(offset) < 0;
    }

    private boolean isDolar(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_TOKEN) && "$".contentEquals(token.text());//NOI18N
    }

    private boolean isLeftBracket(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_TOKEN) && ")".contentEquals(token.text());//NOI18N
    }

    private boolean isRightBracket(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_TOKEN) && "(".contentEquals(token.text());//NOI18N
    }

    private boolean isReference(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_OBJECT_OPERATOR);
    }

    private boolean isStaticReference(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM);
    }

    private boolean isVariable(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_VARIABLE);
    }

    private boolean isSelf(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_SELF);
    }

    private boolean isParent(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_PARENT);
    }

    private boolean isString(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_STRING);
    }
}
