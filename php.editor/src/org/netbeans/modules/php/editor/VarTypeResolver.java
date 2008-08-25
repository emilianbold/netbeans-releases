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

package org.netbeans.modules.php.editor;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.nav.NavUtils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.util.Union2;

/**
 * @author Radek Matous
 */
public final class VarTypeResolver {
    private final PHPCompletionItem.CompletionRequest request;
    private final String varName;
    private final List<ASTNode> pathUnderCaret;
    private final ASTNode blockOfCaret;
    private VarTypeResolver(final PHPCompletionItem.CompletionRequest request,
            final String varName) {
        this.request = request;
        this.varName = varName;
        pathUnderCaret = NavUtils.underCaret(request.info, request.anchor);
        blockOfCaret = findNearestBlock(pathUnderCaret);

    }

    public static VarTypeResolver getInstance(final PHPCompletionItem.CompletionRequest request,
            final String varName)  {

        return new VarTypeResolver(request, varName);
    }

    public String resolveType() {
        final Map<String, Union2<Variable, String>> assignments = new HashMap<String, Union2<Variable, String>>();
        final List<ASTNode> path = new LinkedList<ASTNode>();
        new DefaultVisitor() {
            @Override
            public void scan(ASTNode node) {
                path.add(node);
                super.scan(node);
                path.remove(node);
            }

            public void visit(Assignment node) {
                int offset = request.anchor;
                if ((offset != (-1) && offset >= node.getStartOffset())) {
                    VariableBase leftHandSide = node.getLeftHandSide();
                    Expression rightHandSide = node.getRightHandSide();
                    if (leftHandSide instanceof Variable) {
                        String leftVarName = CodeUtils.extractVariableName((Variable) leftHandSide);
                        if (isValidBlock(path)) {
                            if (rightHandSide instanceof Variable) {
                                String rightVarName = CodeUtils.extractVariableName((Variable) rightHandSide);
                                Union2<Variable, String> rAssignment = assignments.get(rightVarName);
                                if (rAssignment != null) {
                                    assignments.put(leftVarName, rAssignment);
                                } else {
                                    assignments.put(leftVarName, Union2.<Variable, String>createFirst((Variable) rightHandSide));
                                }
                            } else if (rightHandSide instanceof ClassInstanceCreation) {
                                assignments.put(leftVarName, Union2.<Variable, String>createSecond(CodeUtils.extractClassName((ClassInstanceCreation) rightHandSide)));
                            } else {
                                String typeName = null;
                                if (rightHandSide instanceof VariableBase) {
                                    Stack<VariableBase> stack = new Stack<VariableBase>();
                                    createVariableBaseChain((VariableBase) rightHandSide, stack);
                                    while (!stack.isEmpty() && stack.peek() != null) {
                                        VariableBase varBase = stack.pop();
                                        if (typeName == null) {
                                            if (varBase instanceof FunctionInvocation) {
                                                typeName = getReturnType((FunctionInvocation) varBase, request);
                                            } else if (varBase instanceof Variable) {
                                                typeName = findPrecedingType((Variable) varBase, assignments);
                                            } else if (varBase instanceof StaticFieldAccess) {
                                                typeName = getReturnType((StaticFieldAccess)varBase, request);
                                            } else if (varBase instanceof StaticMethodInvocation) {
                                                typeName = getReturnType((StaticMethodInvocation)varBase, request);
                                            }
                                            if (typeName == null) {
                                                break;
                                            }
                                        } else {
                                            if (varBase instanceof MethodInvocation) {
                                                typeName = getReturnType(typeName, (MethodInvocation) varBase, request);
                                            } else {
                                                typeName = null;
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (typeName == null) {
                                    assignments.put(leftVarName, null);
                                } else {
                                    assignments.put(leftVarName, Union2.<Variable, String>createSecond(typeName));
                                }
                            }
                        } else {
                            assignments.put(leftVarName, null);
                        }
                    }
                    super.visit(node);
                }
            }
        }.scan(Utils.getRoot(request.info));
        return findPrecedingType(varName, assignments);
    }
    private static void createVariableBaseChain(VariableBase node, Stack<VariableBase> stack) {
        stack.push(node);
        if (node instanceof MethodInvocation) {
            createVariableBaseChain(((MethodInvocation)node).getDispatcher(), stack);
        }
    }
    private static String getReturnType(FunctionInvocation node, final PHPCompletionItem.CompletionRequest request) {
        PHPIndex index = request.index;
        Collection<IndexedFunction> functions = index.getFunctions(request.result, CodeUtils.extractFunctionName(node), NameKind.EXACT_NAME);
        if (!functions.isEmpty()) {
            IndexedFunction fnc = functions.iterator().next();
            return fnc.getReturnType();
        }
        return null;
    }
    private static String getReturnType(StaticMethodInvocation node, final PHPCompletionItem.CompletionRequest request) {
        PHPIndex index = request.index;
        StaticMethodInvocation staticMethodInvocation = (StaticMethodInvocation) node;
        String clsName = staticMethodInvocation.getClassName().getName();
        FunctionInvocation method = staticMethodInvocation.getMethod();
        String fncName = CodeUtils.extractFunctionName(method);
        Collection<IndexedFunction> functions =
                index.getAllMethods(request.result, clsName, fncName, NameKind.EXACT_NAME, PHPIndex.ANY_ATTR);
        if (!functions.isEmpty()) {
            IndexedFunction fnc = functions.iterator().next();
            return fnc.getReturnType();
        }
        return null;
    }
    private static String getReturnType(StaticFieldAccess node, final PHPCompletionItem.CompletionRequest request) {
        StaticFieldAccess staticFieldAccess = (StaticFieldAccess) node;
        String clsName = staticFieldAccess.getClassName().getName();
        Variable var = staticFieldAccess.getField();
        String varName = CodeUtils.extractVariableName(var);
        varName = (varName.startsWith("$")) //NOI18N
                ? varName.substring(1) : varName;

        PHPIndex index = request.index;
        Collection<IndexedConstant> constants =
                index.getAllProperties(request.result, clsName, varName, NameKind.EXACT_NAME, PHPIndex.ANY_ATTR);

        if (!constants.isEmpty()) {
            IndexedConstant con = constants.iterator().next();
            return con.getTypeName();
        }
        return null;
    }
    private static String getReturnType(String className, VariableBase v, final PHPCompletionItem.CompletionRequest request) {
        return null;
    }

    private static String getReturnType(String className, MethodInvocation methodInvocation, final PHPCompletionItem.CompletionRequest request) {
        PHPIndex index = request.index;
        FunctionInvocation method = methodInvocation.getMethod();
        String fncName = CodeUtils.extractFunctionName(method);
        Collection<IndexedFunction> functions =
                index.getAllMethods(request.result, className, fncName, NameKind.EXACT_NAME, PHPIndex.ANY_ATTR);
        if (!functions.isEmpty()) {
            IndexedFunction fnc = functions.iterator().next();
            return fnc.getReturnType();
        }
        return null;
    }
    private static String findPrecedingType(Variable node, final Map<String, Union2<Variable, String>> assignments) {
        String varName = CodeUtils.extractVariableName(node);
        return findPrecedingType(varName, assignments);
    }

    private static String findPrecedingType(String varName, final Map<String, Union2<Variable, String>> assignments) {
        String retval = null;
        Union2<Variable, String> rAssignment = assignments.get(varName);
        if (rAssignment != null && rAssignment.hasSecond()) {
            retval = rAssignment.second();
        }
        return retval;
    }

    private ASTNode findNearestBlock(final List<ASTNode> path) {
        ASTNode retval = null;
        int size = path.size();
        for (int i = size - 1; i >= 0; i--) {
            ASTNode node = path.get(i);
            if (node instanceof Program || node instanceof Block) {
                retval = node;
                break;
            }
        }
        return retval;
    }

    private boolean isValidBlock(final List<ASTNode> path) {
        ASTNode nearestBlock = findNearestBlock(path);
        if (nearestBlock == null) {
            return false;
        } else if (blockOfCaret == nearestBlock) {
            return true;
        }
        int size = pathUnderCaret.size();
        for (int i = size - 1; i >= 0; i--) {
            ASTNode node = pathUnderCaret.get(i);
            if (node == nearestBlock) {
                return true;
            }
        }
        return false;

    }


}
