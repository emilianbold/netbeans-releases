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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.impl.ModelVisitor;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;

/**
 * @author Radek Matous
 */
public class ParameterInfoSupport {

    private ModelVisitor modelVisitor;
    private Document document;
    private int offset;

    ParameterInfoSupport(ModelVisitor modelVisitor, Document document, int offset) {
        this.modelVisitor = modelVisitor;
        this.document = document;
        this.offset = offset;
    }
    private static final Collection<PHPTokenId> CTX_DELIMITERS = Arrays.asList(
            PHPTokenId.PHP_OPENTAG, PHPTokenId.PHP_SEMICOLON, PHPTokenId.PHP_CURLY_OPEN, PHPTokenId.PHP_CURLY_CLOSE,
            PHPTokenId.PHP_RETURN, PHPTokenId.PHP_OPERATOR, PHPTokenId.PHP_ECHO,
            PHPTokenId.PHP_EVAL, PHPTokenId.PHP_NEW, PHPTokenId.PHP_NOT, PHPTokenId.PHP_CASE,
            PHPTokenId.PHP_IF, PHPTokenId.PHP_ELSE, PHPTokenId.PHP_ELSEIF, PHPTokenId.PHP_PRINT,
            PHPTokenId.PHP_FOR, PHPTokenId.PHP_FOREACH, PHPTokenId.PHP_WHILE,
            PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_LINE_COMMENT,
            PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING, PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE);


    private enum State {

        START, METHOD, INVALID, VARBASE, DOLAR, PARAMS, REFERENCE, STATIC_REFERENCE, FUNCTION, FIELD, VARIABLE, CLASSNAME, STOP
    };

    public ParameterInfo getParameterInfo() {
        ParameterInfo retval = parametersNodeImpl(offset, modelVisitor.getCompilationInfo());
        if (retval == ParameterInfo.NONE) {
            retval = parametersTokenImpl();
        }
        return retval;
    }
    private ParameterInfo parametersTokenImpl() {
        FileScope modelScope = modelVisitor.getFileScope();
        VariableScope nearestVariableScope = modelVisitor.getNearestVariableScope(offset);

        if (modelScope == null || nearestVariableScope == null) {
            return ParameterInfo.NONE;
        }
        TokenSequence<PHPTokenId> tokenSequence = LexUtilities.getPHPTokenSequence(document, offset);

        if (moveToOffset(tokenSequence, offset)) {
            return ParameterInfo.NONE;
        }

        int commasCount = 0;
        int anchor = -1;
        State state = State.PARAMS;
        int leftBraces = 0;
        int rightBraces = 1;
        StringBuilder metaAll = new StringBuilder();
        while (!state.equals(State.INVALID) && !state.equals(State.STOP) && tokenSequence.movePrevious() && skipWhitespaces(tokenSequence)) {
            Token<PHPTokenId> token = tokenSequence.token();
            if (!CTX_DELIMITERS.contains(token.id())) {
                switch (state) {
                    case METHOD:
                    case START:
                        state = (state.equals(State.METHOD)) ? State.STOP : State.INVALID;
                        // state = State.INVALID;
                        if (isReference(token)) {
                            metaAll.insert(0, "@" + VariousUtils.METHOD_TYPE_PREFIX);
                            state = State.REFERENCE;
                        } else if (isStaticReference(token)) {
                            metaAll.insert(0, "@" + VariousUtils.METHOD_TYPE_PREFIX);
                            state = State.STATIC_REFERENCE;
                        } else if (state.equals(State.STOP)) {
                            metaAll.insert(0, "@" + VariousUtils.FUNCTION_TYPE_PREFIX);
                        }
                        break;
                    case REFERENCE:
                        state = State.INVALID;
                        if (isRightBracket(token)) {
                            rightBraces++;
                            state = State.PARAMS;
                        } else if (isString(token)) {
                            metaAll.insert(0, token.text().toString());
                            state = State.FIELD;
                        } else if (isVariable(token)) {
                            metaAll.insert(0, token.text().toString());
                            state = State.VARBASE;
                        }
                        break;
                    case STATIC_REFERENCE:
                        state = State.INVALID;
                        if (isString(token)) {
                            metaAll.insert(0, token.text().toString());
                            state = State.CLASSNAME;
                        } else if (isSelf(token)) {
                            metaAll.insert(0, buildStaticClassName(nearestVariableScope, token.text().toString()));
                            //TODO: maybe rather introduce its own State
                            state = State.CLASSNAME;
                        }
                        break;
                    case PARAMS:
                        state = State.INVALID;
                        if (isWhiteSpace(token)) {
                            state = State.PARAMS;
                        } else if (isComma(token)) {
                            if (metaAll.length() == 0) {
                                commasCount++;
                            }
                            state = State.PARAMS;
                        } else if (isVariable(token)) {
                            state = State.PARAMS;
                        } else if (CTX_DELIMITERS.contains(token.id())) {
                            state = State.INVALID;
                        } else if (isLeftBracket(token)) {
                            leftBraces++;
                        } else if (isRightBracket(token)) {
                            rightBraces++;
                        }
                        if (leftBraces == rightBraces) {
                            state = State.FUNCTION;
                        }
                        break;
                    case FUNCTION:
                        state = State.INVALID;
                        if (isString(token)) {
                            metaAll.insert(0, token.text().toString());
                            if (anchor == -1) {
                                anchor = tokenSequence.offset();
                            }
                            state = State.METHOD;
                        }
                        break;
                    case FIELD:
                        state = State.INVALID;
                        if (isReference(token)) {
                            metaAll.insert(0, "@" + VariousUtils.FIELD_TYPE_PREFIX);
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
                        metaAll.insert(0, "@" + VariousUtils.VAR_TYPE_PREFIX);
                    case CLASSNAME:
                        //TODO: self, parent not handled yet
                        //TODO: maybe rather introduce its own State for self, parent
                        state = State.STOP;
                        break;
                }
            } else {
                if (state.equals(State.METHOD)) {
                    state = State.STOP;
                    PHPTokenId id = token.id();
                    if (id != null && PHPTokenId.PHP_NEW.equals(id)) {
                        metaAll.insert(0, "@" + VariousUtils.CONSTRUCTOR_TYPE_PREFIX);
                    } else {
                        metaAll.insert(0, "@" + VariousUtils.FUNCTION_TYPE_PREFIX);
                    }
                    break;
                }
            }

        }
        if (state.equals(State.STOP)) {
            String typeName = metaAll.toString();
            Stack<? extends ModelElement> elemenst = VariousUtils.getElemenst(modelScope, nearestVariableScope, typeName, offset);
            if (!elemenst.isEmpty()) {
                ModelElement element = elemenst.peek();
                if (element instanceof FunctionScope) {
                    return new ParameterInfo(toParamNames((FunctionScope) element), commasCount, offset);
                }
            }
        }

        return ParameterInfo.NONE;
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

    private static boolean skipWhitespaces(TokenSequence<PHPTokenId> tokenSequence) {
        Token<PHPTokenId> token = tokenSequence.token();
        while (token != null && isWhiteSpace(token)) {
            boolean retval = tokenSequence.movePrevious();
            token = tokenSequence.token();
            if (!retval) {
                return false;
            }
        }
        return true;
    }

    private static boolean moveToOffset(TokenSequence<PHPTokenId> tokenSequence, final int offset) {
        return tokenSequence == null || tokenSequence.move(offset) < 0;
    }

    private static boolean isDolar(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_TOKEN) && "$".contentEquals(token.text());//NOI18N
    }

    private static boolean isLeftBracket(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_TOKEN) && "(".contentEquals(token.text());//NOI18N
    }

    private static boolean isRightBracket(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_TOKEN) && ")".contentEquals(token.text());//NOI18N
    }

    private static boolean isComma(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_TOKEN) && ",".contentEquals(token.text());//NOI18N
    }

    private static boolean isReference(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_OBJECT_OPERATOR);
    }

    private static boolean isWhiteSpace(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.WHITESPACE);
    }

    private static boolean isStaticReference(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM);
    }

    private static boolean isVariable(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_VARIABLE);
    }

    private static boolean isSelf(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_SELF);
    }

    private static boolean isParent(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_PARENT);
    }

    private static boolean isString(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_STRING);
    }

    private static ParameterInfo parametersNodeImpl(final int caretOffset, final ParserResult info) {
        final ParameterInfo[] retval = new ParameterInfo[1];
        DefaultVisitor visitor = new DefaultVisitor() {

            @Override
            public void scan(ASTNode node) {
                if (node != null && retval[0] == null) {
                    OffsetRange range = new OffsetRange(node.getStartOffset(), node.getEndOffset());
                    if (range.containsInclusive(caretOffset)) {
                        super.scan(node);
                    }
                }
            }

            @Override
            public void visit(ClassInstanceCreation node) {
                if (retval[0] == null) {
                    ASTNodeInfo<ClassInstanceCreation> nodeInfo = ASTNodeInfo.create(node);
                    retval[0] = createParameterInfo(nodeInfo,node.ctorParams());
                    super.visit(node);
                }
            }


            @Override
            public void visit(FunctionInvocation node) {
                if (retval[0] == null) {
                    ASTNodeInfo<FunctionInvocation> nodeInfo = ASTNodeInfo.create(node);
                    retval[0] = createParameterInfo(nodeInfo,node.getParameters());
                    super.visit(node);
                }
            }

            private ParameterInfo createParameterInfo(ASTNodeInfo nodeInfo, List<Expression> parameters) {
                int idx = -1;
                ASTNode node = nodeInfo.getOriginalNode();
                int anchor  = nodeInfo.getRange().getEnd();
                OffsetRange offsetRange = new OffsetRange(anchor, node.getEndOffset());
                if (offsetRange.containsInclusive(caretOffset)) {
                    idx = 0;
                    for (int i = 0; i < parameters.size(); i++) {
                        Expression expression = parameters.get(i);
                        offsetRange = new OffsetRange(expression.getStartOffset(), expression.getEndOffset());
                        if (offsetRange.containsInclusive(caretOffset)) {
                            idx = i;
                        } else {
                            offsetRange = new OffsetRange(expression.getEndOffset(), node.getEndOffset());
                            if (offsetRange.containsInclusive(caretOffset)) {
                                idx = i + 1;
                            }
                        }
                    }
                    final Model model = ModelFactory.getModel(info);
                    OccurencesSupport occurencesSupport = model.getOccurencesSupport((nodeInfo.getRange().getStart() + anchor) / 2);
                    Occurence occurence = occurencesSupport.getOccurence();
                    if (occurence != null) {
                        ModelElement declaration = occurence.getDeclaration();
                        if (declaration instanceof FunctionScope && occurence.getAllDeclarations().size() == 1) {
                            FunctionScope functionScope = (FunctionScope) declaration;
                            return new ParameterInfo(toParamNames(functionScope), idx, anchor);
                        }
                    }
                }
                return null;
            }
        };
        Program root = Utils.getRoot(info);
        if (root != null) {
            visitor.scan(root);
            if (retval[0] != null) {
                return retval[0];
            }
        }
        return ParameterInfo.NONE;
    }

    @CheckForNull
    private static List<String> toParamNames(FunctionScope functionScope) {
        List<String> paramNames = new ArrayList<String>();
        List<? extends Parameter> parameters = functionScope.getParameters();
        for (Parameter parameter : parameters) {
            if (parameter.isMandatory()) {
                paramNames.add(parameter.getName());
            } else {
                paramNames.add(parameter.getName() + "=" + parameter.getDefaultValue() != null ? parameter.getDefaultValue() : "");//NOI18N
            }
        }
        return paramNames;
    }
}
