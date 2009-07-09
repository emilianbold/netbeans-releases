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

package org.netbeans.modules.php.editor.model.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.openide.util.Union2;

/**
 * @author Radek Matous
 */
class VariableBaseType {
    public static final String FUNCTION_TYPE_PREFIX = "fn:";
    public static final String METHOD_TYPE_PREFIX = "mtd:";
    public static final String STATIC_METHOD_TYPE_PREFIX = "static.mtd:";
    public static final String FIELD_TYPE_PREFIX = "fld:";
    public static final String STATIC_FIELD__TYPE_PREFIX = "static.fld:";
    public static final String VAR_TYPE_PREFIX = "var:";

    private Union2<String, TypeScopeImpl> type;
    private VariableBaseType(Union2<String, TypeScopeImpl> type){
        this.type = type;
    }

    static VariableBaseType create(MethodInvocation mi) {
        return createImpl(mi);
    }

    static VariableBaseType create(Variable variable) {
        return createImpl(variable);
    }

    static VariableBaseType create(StaticMethodInvocation smi) {
        return createImpl(smi);
    }

    static VariableBaseType create(FunctionInvocation fi) {
        return createImpl(fi);
    }

    static VariableBaseType create(Assignment assignment) {
        return new VariableBaseType(Union2.<String, TypeScopeImpl>createFirst(
                extractVariableTypeFromAssignment(assignment, new HashMap<String, VariableNameImpl>())));
    }
    
    private static VariableBaseType createImpl(VariableBase varBase) {
        return new VariableBaseType(Union2.<String, TypeScopeImpl>createFirst(
                extractTypeFroVariableBase(varBase, new HashMap<String, VariableNameImpl>())));
    }


    private static String extractTypeFroVariableBase(VariableBase varBase, Map<String, VariableNameImpl> allAssignments) {
        Stack<VariableBase> stack = new Stack<VariableBase>();
        String typeName = null;
        createVariableBaseChain(varBase, stack);
        while (!stack.isEmpty() && stack.peek() != null) {
            varBase = stack.pop();
            String tmpType = extractVariableTypeFromVariableBase(varBase, allAssignments);
            if (tmpType == null) {
                typeName = tmpType;
                break;
            }
            if (typeName == null) {
                typeName = tmpType;
            } else {
                typeName += tmpType;
            }
        }
        return typeName; 
    }

    private static void createVariableBaseChain(VariableBase node, Stack<VariableBase> stack) {
        stack.push(node);
        if (node instanceof MethodInvocation) {
            createVariableBaseChain(((MethodInvocation) node).getDispatcher(), stack);
        }
    }

    private static String extractVariableTypeFromVariableBase(VariableBase varBase, Map<String, VariableNameImpl> allAssignments) {
        if (varBase instanceof Variable) {
            String varName = CodeUtils.extractVariableName((Variable) varBase);
            VariableNameImpl nameImpl = allAssignments.get(varName);
            if (nameImpl != null) {
                VarAssignmentImpl last = ModelUtils.getLast(nameImpl.getAssignments());
                if (last != null) {
                    String semiTypeName = last.typeNameFromUnion();
                    if (semiTypeName != null) {
                        return semiTypeName;
                    }
                }
            }
            return "@" + VAR_TYPE_PREFIX + varName;
        } else if (varBase instanceof FunctionInvocation) {
            FunctionInvocation functionInvocation = (FunctionInvocation) varBase;
            String fname = CodeUtils.extractFunctionName(functionInvocation);
            return "@" + FUNCTION_TYPE_PREFIX + fname;
        } else if (varBase instanceof StaticMethodInvocation) {
            StaticMethodInvocation staticMethodInvocation = (StaticMethodInvocation) varBase;
            String className = CodeUtils.extractUnqualifiedClassName(staticMethodInvocation);
            String methodName = CodeUtils.extractFunctionName(staticMethodInvocation.getMethod());

            if (className != null && methodName != null) {
                return "@" + STATIC_METHOD_TYPE_PREFIX + className + '.' + methodName;
            }
        } else if (varBase instanceof MethodInvocation) {
            MethodInvocation methodInvocation = (MethodInvocation) varBase;
            String methodName = CodeUtils.extractFunctionName(methodInvocation.getMethod());
            if (methodName != null) {
                return "@" + METHOD_TYPE_PREFIX + methodName;
            }
        }

        return null;
    }

    static String extractVariableTypeFromAssignment(Assignment assignment, Map<String, VariableNameImpl> allAssignments) {
        Expression expression = assignment.getRightHandSide();
        if (expression instanceof Assignment) {
            // handle nested assignments, e.g. $l = $m = new ObjectName;
            return extractVariableTypeFromAssignment((Assignment) expression, allAssignments);
        } else if (expression instanceof Reference) {
            Reference ref = (Reference) expression;
            expression = ref.getExpression();
        }
        if (expression instanceof ClassInstanceCreation) {
            ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
            Expression className = classInstanceCreation.getClassName().getName();

            if (className instanceof Identifier) {
                Identifier identifier = (Identifier) className;
                return identifier.getName();
            }
        } else if (expression instanceof ArrayCreation) {
            return "array"; //NOI18N
        } else if (expression instanceof VariableBase) {
            return extractTypeFroVariableBase((VariableBase) expression, allAssignments);//extractVariableTypeFromVariableBase(varBase);
        }

        return null;
    }
}
