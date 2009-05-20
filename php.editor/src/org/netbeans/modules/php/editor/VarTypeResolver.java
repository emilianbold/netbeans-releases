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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.model.ModelFactory;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.nav.NavUtils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.CatchClause;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ClassName;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.InstanceOfExpression;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
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
    private final String varName;
    private final List<ASTNode> pathUnderCaret;
    private final ASTNode blockOfCaret;
    private int anchor;
    private PHPIndex index;
    private ParserResult info;
    private PHPParseResult result;
    private VarTypeResolver(final PHPCompletionItem.CompletionRequest request,
            final String varName) {
        this(varName,request.anchor,request.index, request.info, request.result);
    }

    private VarTypeResolver(final String varName,int anchor,PHPIndex index,ParserResult info,PHPParseResult result) {
        this.result = result;
        this.anchor = anchor;
        this.index = index;
        this.info = info;
        this.varName = varName;
        pathUnderCaret = NavUtils.underCaret(info, anchor);
        blockOfCaret = findNearestBlock(pathUnderCaret);
    }

    private VarTypeResolver(final ParserResult info, final int offset, final String varName)  {
        this(varName,offset,
                PHPIndex.get(info),
                info,
                (PHPParseResult)info);
    }
    public static VarTypeResolver getInstance(final PHPCompletionItem.CompletionRequest request,
            final String varName)  {

        return new VarTypeResolver(request, varName);
    }

    public static VarTypeResolver getInstance(final ParserResult info, final int offset, final String varName)  {
        return new VarTypeResolver(info, offset, varName);
    }

    public String resolveType() {
        final Map<String, Union2<? extends ASTNode, String>> assignments = new HashMap<String, Union2<? extends ASTNode, String>>();
        final Map<String, ElementKind> memberNames = new HashMap<String, ElementKind>();
        final List<ASTNode> path = new LinkedList<ASTNode>();
        new DefaultVisitor() {
            @Override
            public void scan(ASTNode node) {
                path.add(node);
                super.scan(node);
                path.remove(node);
            }

            @Override
            public void visit(ClassDeclaration node) {
                assignments.put("$this", Union2.<Variable, String>createSecond(CodeUtils.extractClassName(node)));//NOI18N
                assignments.put("self", Union2.<Variable, String>createSecond(CodeUtils.extractClassName(node)));//NOI18N
                assignments.put("parent", Union2.<Variable, String>createSecond(CodeUtils.extractSuperClassName(node)));//NOI18N
                super.visit(node);
            }


            @Override
            public void visit(FunctionDeclaration node) {
                int offset = anchor;
                if ((offset != (-1) && offset >= node.getStartOffset())) {
                    if (isValidBlock(path)) {
                        List<FormalParameter> formalParameters = node.getFormalParameters();
                        for (FormalParameter param : formalParameters) {
                            Identifier parameterType = param.getParameterType();
                            if (parameterType == null) continue;
                            String typeName = parameterType.getName();
                            String paramName = null;
                            Expression parameterName = param.getParameterName();
                            Variable var = null;
                            if (parameterName instanceof Reference) {
                                Reference ref = (Reference) parameterName;
                                parameterName = ref.getExpression();
                                if (parameterName instanceof Variable) {
                                    var = (Variable) parameterName;
                                }
                            } else if (parameterName instanceof Variable) {
                                var = (Variable) parameterName;
                            }
                            if (var != null) {
                                paramName = CodeUtils.extractVariableName((Variable) parameterName);
                            }
                            if (paramName != null && typeName != null && typeName.length() > 0) {
                                assignments.put(paramName, Union2.<Variable, String>createSecond(typeName));
                            }
                        }
                    }
                }

                super.visit(node);
            }

            @Override
            public void visit(InstanceOfExpression node) {
                //TODO: doesn't work properly yet, commented out because offers type also in wrong contexts
                /*int offset = anchor;
                if ((offset != (-1) && offset >= node.getStartOffset())) {
                    if (isValidBlock(path)) {
                        Expression expression = node.getExpression();
                        if (expression instanceof Variable) {
                            String typeName = CodeUtils.extractClassName(node.getClassName());
                            String vName = CodeUtils.extractVariableName((Variable) expression);
                            if (vName != null && typeName != null && typeName.length() > 0 && assignments.get(vName) == null) {
                                assignments.put(vName, Union2.<Variable, String>createSecond(typeName));
                            }
                        }
                    }
                }*/
                super.visit(node);
            }

            @Override
            public void visit(CatchClause node) {
                int offset = anchor;
                if ((offset != (-1) && offset >= node.getStartOffset()
                        && offset <= node.getEndOffset())) {
                    if (isValidBlock(path)) {
                        String excName = CodeUtils.extractVariableName(node.getVariable());
                        String typeName = node.getClassName().getName();
                        if (excName != null && typeName != null && typeName.length() > 0) {
                            assignments.put(excName, Union2.<Variable, String>createSecond(typeName));
                        }
                    }
                }
                super.visit(node);
            }

            @Override
            public void visit(FieldAccess node) {
                if ((blockOfCaret.getStartOffset() <= node.getStartOffset() &&
                        blockOfCaret.getEndOffset() >= node.getEndOffset())) {
                    if (isValidBlock(path)) {
                        Variable field = node.getField();
                        VariableBase dispatcher = node.getDispatcher();
                        if (dispatcher instanceof Variable) {
                            Variable var = (Variable) dispatcher;
                            String name = CodeUtils.extractVariableName(var);
                            if (name != null && name.equals(varName)) {
                                String fldNames = CodeUtils.extractVariableName(field);
                                if (fldNames != null) {
                                    memberNames.put(fldNames, ElementKind.FIELD);
                                }
                            }
                        }
                    }
                }
                super.visit(node);
            }

            @Override
            public void visit(MethodInvocation node) {
                int offset = anchor;
                if ((offset != (-1) && offset >= node.getStartOffset())) {
                    if (isValidBlock(path)) {

                        VariableBase dispatcher = node.getDispatcher();
                        if (dispatcher instanceof Variable) {
                            Variable var = (Variable) dispatcher;
                            String name = CodeUtils.extractVariableName(var);
                            if (name != null && name.equals(varName)) {
                                String methName = CodeUtils.extractFunctionName(node.getMethod());
                                if (methName != null) {
                                    memberNames.put(methName, ElementKind.METHOD);
                                }
                            }
                        }
                    }
                }
                super.visit(node);
            }


            public void visit(Assignment node) {
                int offset = anchor;                
                if ((offset != (-1) && offset >= node.getStartOffset())) {
                    VariableBase leftHandSide = node.getLeftHandSide();
                    Expression rightHandSide = node.getRightHandSide();
                    if (leftHandSide instanceof Variable || leftHandSide instanceof FieldAccess) {
                        String leftVarName = null;
                        if (leftHandSide instanceof Variable) {
                            leftVarName = CodeUtils.extractVariableName((Variable) leftHandSide);
                        } else if (leftHandSide instanceof FieldAccess) {
                            leftVarName = getInternalName((FieldAccess) leftHandSide, assignments);
                        } else {
                            assert false : leftHandSide.getClass().getName();
                        }
                        if (leftVarName != null) {
                            if (isValidBlock(path)) {
                                if (rightHandSide instanceof Reference) {
                                    while(rightHandSide instanceof Reference) {
                                        rightHandSide = ((Reference)rightHandSide).getExpression();
                                    }
                                }
                                if (rightHandSide instanceof Variable || rightHandSide instanceof FieldAccess) {
                                    String rightVarName = null;
                                    if (rightHandSide instanceof Variable) {
                                        rightVarName = CodeUtils.extractVariableName((Variable) rightHandSide);
                                    } else if (rightHandSide instanceof FieldAccess) {
                                        rightVarName = getInternalName((FieldAccess) rightHandSide, assignments);
                                    } else {
                                        assert false : rightHandSide.getClass().getName();
                                    }

                                    Union2<? extends ASTNode, String> rAssignment = assignments.get(rightVarName);
                                    if (rAssignment != null) {
                                        assignments.put(leftVarName, rAssignment);
                                    } else {
                                        if (rightHandSide instanceof Variable) {
                                            assignments.put(leftVarName, Union2.<Variable, String>createFirst((Variable) rightHandSide));
                                        } else if (rightHandSide instanceof FieldAccess) {
                                            assignments.put(leftVarName, Union2.<FieldAccess, String>createFirst((FieldAccess) rightHandSide));
                                        } else {
                                            assert false : rightHandSide.getClass().getName();
                                        }
                                    }
                                } else if (rightHandSide instanceof ClassInstanceCreation) {
                                    ClassInstanceCreation clsInstanceCreation = (ClassInstanceCreation) rightHandSide;
                                    ClassName className = clsInstanceCreation.getClassName();
                                    Expression expr = className.getName();
                                    if (expr instanceof Identifier) {
                                        assignments.put(leftVarName, Union2.<Variable, String>createSecond(((Identifier) expr).getName()));
                                    } else {
                                        assignments.put(leftVarName, null);
                                    }
                                } else {
                                    String typeName = null;
                                    if (rightHandSide instanceof VariableBase) {
                                        typeName = evaluateVariableBase((VariableBase)rightHandSide, assignments);
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
                    }
                    super.visit(node);
                }
            }
        }.scan(Utils.getRoot(info));
        String retval = findPrecedingType(varName, assignments);
        if (retval != null && retval.length() == 0) {
            retval = null;
        }
        // next attempt to resolve type
        if (retval == null) {
            Model model = ModelFactory.getModel(info);
            VariableScope varScope = model.getVariableScope(anchor);
            if (varScope != null) {
                List<? extends VariableName> variables = ModelUtils.filter(varScope.getDeclaredVariables(), varName);
                VariableName var = ModelUtils.getFirst(variables);
                if (var != null) {
                    TypeScope typeScope = ModelUtils.getFirst(var.getTypes(anchor));
                    if (typeScope != null) {
                        retval = typeScope.getName();
                    }
                }
            }
        }
        if (retval == null && !memberNames.isEmpty()) {
            Set<String> typeNames = null;
            for (Entry<String, ElementKind> entrySet : memberNames.entrySet()) {
                if (typeNames == null) {
                    typeNames = new HashSet<String>(index.typeNamesForIdentifier(entrySet.getKey(), entrySet.getValue(),QuerySupport.Kind.CASE_INSENSITIVE_PREFIX));
                } else {
                    Set<String> names4MethName = index.typeNamesForIdentifier(entrySet.getKey(), entrySet.getValue(),QuerySupport.Kind.CASE_INSENSITIVE_PREFIX);
                    typeNames.retainAll(names4MethName);
                }
                if (!(typeNames.size() > 1)) {
                    break;
                }
            }
            if (typeNames.size() == 1) {
                retval = typeNames.iterator().next();
            }
        }

        return retval;
    }

    private String getInternalName(FieldAccess fieldAccess,  Map<String, Union2<? extends ASTNode, String>> assignments) {
        StringBuffer sb = new StringBuffer();
        VariableBase dispatcher = fieldAccess.getDispatcher();
        String dispType = evaluateVariableBase((VariableBase) dispatcher, assignments);
        if (dispType != null) {
            Variable field = fieldAccess.getField();
            String fldName = CodeUtils.extractVariableName(field);
            if (fldName != null) {
                if (fldName.startsWith("$")) {//NOI18N
                    fldName.substring(1);
                }
                sb.append(dispType).append("::").append(fldName);
                return sb.toString();
            }
        }
        return null;
    }

    private String evaluateVariableBase(VariableBase rightHandSide, Map<String, Union2<? extends ASTNode, String>> assignments) {
        String typeName = null;
        Stack<VariableBase> stack = new Stack<VariableBase>();
        createVariableBaseChain((VariableBase) rightHandSide, stack);
        while (!stack.isEmpty() && stack.peek() != null) {
            VariableBase varBase = stack.pop();
            if (typeName == null) {
                if (varBase instanceof FunctionInvocation) {
                    typeName = getReturnType((FunctionInvocation) varBase, result, index);
                } else if (varBase instanceof Variable) {
                    typeName = findPrecedingType((Variable) varBase, assignments);
                } else if (varBase instanceof StaticFieldAccess) {
                    typeName = getReturnType((StaticFieldAccess) varBase, result, index);
                } else if (varBase instanceof StaticMethodInvocation) {
                    typeName = getReturnType((StaticMethodInvocation) varBase, result, index);
                }
                if (typeName == null) {
                    break;
                }
            } else {
                if (varBase instanceof MethodInvocation) {
                    typeName = getReturnType(typeName, (MethodInvocation) varBase, result, index);
                } else {
                    typeName = null;
                    break;
                }
            }
        }
        return typeName;
    }
    private static void createVariableBaseChain(VariableBase node, Stack<VariableBase> stack) {
        stack.push(node);
        if (node instanceof MethodInvocation) {
            createVariableBaseChain(((MethodInvocation)node).getDispatcher(), stack);
        }
    }
    private static String getReturnType(FunctionInvocation node,PHPParseResult result,PHPIndex index) {
        Collection<IndexedFunction> functions = index.getFunctions(result, CodeUtils.extractFunctionName(node), QuerySupport.Kind.EXACT);
        if (!functions.isEmpty()) {
            IndexedFunction fnc = functions.iterator().next();
            return fnc.getReturnType();
        }
        return null;
    }
    private static String getReturnType(StaticMethodInvocation node,PHPParseResult result,PHPIndex index) {
        StaticMethodInvocation staticMethodInvocation = (StaticMethodInvocation) node;
        String clsName = staticMethodInvocation.getClassName().getName();
        FunctionInvocation method = staticMethodInvocation.getMethod();
        String fncName = CodeUtils.extractFunctionName(method);
        Collection<IndexedFunction> functions =
                index.getAllMethods(result, clsName, fncName, QuerySupport.Kind.EXACT, PHPIndex.ANY_ATTR);
        if (!functions.isEmpty()) {
            IndexedFunction fnc = functions.iterator().next();
            return fnc.getReturnType();
        }
        return null;
    }
    private static String getReturnType(StaticFieldAccess node,PHPParseResult result,PHPIndex index) {
        StaticFieldAccess staticFieldAccess = (StaticFieldAccess) node;
        String clsName = staticFieldAccess.getClassName().getName();
        Variable var = staticFieldAccess.getField();
        String varName = CodeUtils.extractVariableName(var);
        if (varName != null) {
            varName = (varName.startsWith("$")) //NOI18N
                    ? varName.substring(1) : varName;

            Collection<IndexedConstant> constants =
                    index.getAllFields(result, clsName, varName, QuerySupport.Kind.EXACT, PHPIndex.ANY_ATTR);

            if (!constants.isEmpty()) {
                IndexedConstant con = constants.iterator().next();
                return con.getTypeName();
            }
        }
        return null;
    }
    private static String getReturnType(String className, VariableBase v,PHPParseResult result, PHPIndex index) {
        return null;
    }

    private static String getReturnType(String className, MethodInvocation methodInvocation,PHPParseResult result,PHPIndex index) {
        FunctionInvocation method = methodInvocation.getMethod();
        String fncName = CodeUtils.extractFunctionName(method);
        Collection<IndexedFunction> functions =
                index.getAllMethods(result, className, fncName, QuerySupport.Kind.EXACT, PHPIndex.ANY_ATTR);
        if (!functions.isEmpty()) {
            IndexedFunction fnc = functions.iterator().next();
            return fnc.getReturnType();
        }
        return null;
    }
    private static String findPrecedingType(Variable node, final Map<String, Union2<? extends ASTNode, String>> assignments) {
        String varName = CodeUtils.extractVariableName(node);

        if (varName == null){
            return null;
        }

        return findPrecedingType(varName, assignments);
    }

    private static String findPrecedingType(String varName, final Map<String, Union2<? extends ASTNode, String>> assignments) {
        String retval = null;
        Union2<? extends ASTNode, String> rAssignment = assignments.get(varName);
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
            if (node instanceof Program || node instanceof Block || node instanceof FunctionDeclaration) {
                retval = node;
                break;
            }
        }
        return retval;
    }

    private boolean isValidBlock(final List<ASTNode> path) {
        if (varName != null && varName.contains("::")) {//NOI18N
            return true;
        }
        ASTNode nearestBlock = findNearestBlock(path);
        if (nearestBlock == null) {
            return false;
        } else if (blockOfCaret == nearestBlock) {
            return true;
        }
        int size = pathUnderCaret.size();
        for (int i = size - 1; i >= 0; i--) {
            ASTNode node = pathUnderCaret.get(i);
            if (node instanceof FunctionDeclaration) {
                if (nearestBlock instanceof FunctionDeclaration) {
                    String fncName = CodeUtils.extractFunctionName((FunctionDeclaration) node);
                    String fncName2 = CodeUtils.extractFunctionName((FunctionDeclaration) nearestBlock);
                    return (fncName.equals(fncName2));
                } else {
                    return false;
                }
            }
            if (node == nearestBlock) {
                return true;
            }
        }
        return false;

    }


}
