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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.model.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.QualifiedNameKind;
import org.netbeans.modules.php.editor.model.IndexScope;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.UseElement;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ClassName;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Include;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression.OperatorType;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar.Type;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.ParenthesisExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 *
 * @author Radek Matous
 */
public class VariousUtils {

    public static final String CONSTRUCTOR_TYPE_PREFIX = "constuct:";
    public static final String FUNCTION_TYPE_PREFIX = "fn:";
    public static final String METHOD_TYPE_PREFIX = "mtd:";
    public static final String STATIC_METHOD_TYPE_PREFIX = "static.mtd:";
    public static final String FIELD_TYPE_PREFIX = "fld:";
    public static final String STATIC_FIELD__TYPE_PREFIX = "static.fld:";
    public static final String VAR_TYPE_PREFIX = "var:";
    public static final String ARRAY_TYPE_PREFIX = "array:";

    public static enum Kind {
        CONSTRUCTOR,
        FUNCTION,
        METHOD,
        STATIC_METHOD,
        FIELD,
        STATIC_FIELD,
        VAR;
        @Override
        public String toString() {
            switch(this) {
                case CONSTRUCTOR:
                    return VariousUtils.CONSTRUCTOR_TYPE_PREFIX;
                case FUNCTION:
                    return VariousUtils.FUNCTION_TYPE_PREFIX;
                case METHOD:
                    return VariousUtils.METHOD_TYPE_PREFIX;
                case STATIC_METHOD:
                    return VariousUtils.STATIC_METHOD_TYPE_PREFIX;
                case FIELD:
                    return VariousUtils.FIELD_TYPE_PREFIX;
                case STATIC_FIELD:
                    return VariousUtils.STATIC_FIELD__TYPE_PREFIX;
                case VAR:
                    return VariousUtils.VAR_TYPE_PREFIX;
            }
            return super.toString();
        }
    };

    public static String extractTypeFroVariableBase(VariableBase varBase) {
        return extractTypeFroVariableBase(varBase, Collections.<String, AssignmentImpl>emptyMap());
    }

    static String extractTypeFroVariableBase(VariableBase varBase, Map<String, AssignmentImpl> allAssignments) {
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
        return typeName; //extractVariableTypeFromVariableBase(varBase);
    }

    private VariousUtils() {
    }

    public static String getReturnTypeFromPHPDoc(Program root, FunctionDeclaration functionDeclaration) {
        return getTypeFromPHPDoc(root, functionDeclaration, PHPDocTag.Type.RETURN);
    }

    public static String getFieldTypeFromPHPDoc(Program root, SingleFieldDeclaration field) {
        return getTypeFromPHPDoc(root, field, PHPDocTag.Type.VAR);
    }


    public static Map<String,List<QualifiedName>> getParamTypesFromPHPDoc(Program root, ASTNode node) {
        Map<String,List<QualifiedName>> retval = new HashMap<String, List<QualifiedName>>();
        Comment comment = Utils.getCommentForNode(root, node);

        if (comment instanceof PHPDocBlock) {
            PHPDocBlock phpDoc = (PHPDocBlock) comment;

            for (PHPDocTag tag : phpDoc.getTags()) {
                if (tag.getKind() == PHPDocTag.Type.PARAM) {
                    List<QualifiedName> types = new ArrayList<QualifiedName>();
                    PHPDocVarTypeTag paramTag = (PHPDocVarTypeTag)tag;
                    for(PHPDocTypeNode type : paramTag.getTypes()) {
                        types.add(QualifiedName.create(type.getValue()));
                    }
                    retval.put(paramTag.getVariable().getValue(), types);
                }
            }
        }
        return retval;
    }

    public static String getTypeFromPHPDoc(Program root, ASTNode node, PHPDocTag.Type tagType) {
        Comment comment = Utils.getCommentForNode(root, node);

        if (comment instanceof PHPDocBlock) {
            PHPDocBlock phpDoc = (PHPDocBlock) comment;

            for (PHPDocTag tag : phpDoc.getTags()) {
                if (tag.getKind() == tagType) {
                    String parts[] = tag.getValue().trim().split("\\s+", 2); //NOI18N

                    if (parts.length > 0) {
                        String type = parts[0].split("\\;", 2)[0];
                        return type;
                    }

                    break;
                }
            }
        }
        return null;
    }


    @CheckForNull
    static String extractVariableTypeFromAssignment(Assignment assignment, Map<String, AssignmentImpl> allAssignments) {
        Expression expression = assignment.getRightHandSide();
        return extractVariableTypeFromExpression(expression, allAssignments);
    }

    static String extractVariableTypeFromExpression(Expression expression, Map<String, AssignmentImpl> allAssignments) {
        if (expression instanceof Assignment) {
            // handle nested assignments, e.g. $l = $m = new ObjectName;
            return extractVariableTypeFromAssignment((Assignment) expression, allAssignments);
        } else if (expression instanceof Reference) {
            Reference ref = (Reference) expression;
            expression = ref.getExpression();
        }
        if (expression instanceof ClassInstanceCreation) {
            ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
            final ClassName className = classInstanceCreation.getClassName();
            Expression name = className.getName();
            if (name instanceof NamespaceName) {
                QualifiedName qn = QualifiedName.create(name);
                return qn.toString();
            }
            return CodeUtils.extractClassName(className);
        } else if (expression instanceof ArrayCreation) {
            return "array"; //NOI18N
        } else if (expression instanceof VariableBase) {
            return extractTypeFroVariableBase((VariableBase) expression, allAssignments); //extractVariableTypeFromVariableBase(varBase);
        } else if (expression instanceof Scalar) {
            Scalar scalar = (Scalar) expression;
            Type scalarType = scalar.getScalarType();
            if (scalarType.equals(Scalar.Type.STRING)) {
                // #174333 - TODO: probably would be better to fix it in parser
                String stringValue = scalar.getStringValue().toLowerCase();
                if (stringValue.equals("false") || stringValue.equals("true")) { //NOI18N
                    return "boolean";//NOI18N
                }
                if (stringValue.equals("null")) { //NOI18N
                    return "null"; //NOI18N
                }
            }
            return scalarType.toString().toLowerCase();
        } else if (expression instanceof InfixExpression) {
            InfixExpression infixExpression = (InfixExpression) expression;
            OperatorType operator = infixExpression.getOperator();
            if (operator.equals(OperatorType.CONCAT)) {
                return Type.STRING.toString().toLowerCase();
            }
        }
        return null;
    }

    public static String replaceVarNames(String semiTypeName, Map<String,String> var2Type)  {
        StringBuilder retval = new StringBuilder();
        String[] fragments = semiTypeName.split("[@:]");
        for (int i = 0; i < fragments.length; i++) {
            String frag = fragments[i];
            if (frag.trim().length() == 0) continue;
            if (VariousUtils.VAR_TYPE_PREFIX.startsWith(frag)) {
                if (i+1 < fragments.length) {
                    String varName = fragments[++i];
                    String type = var2Type.get(varName);
                    if (type != null) {
                        retval.append(type);
                        continue;
                    }
                }
                return null;
            }
            Kind[] values = VariousUtils.Kind.values();
            boolean isPrefix = false;
            for (Kind kind : values) {
                if (kind.toString().startsWith(frag)) {
                    isPrefix = true;
                    break;
                }
            }
            if (isPrefix) {
                retval.append("@");//NOI18N
                retval.append(frag);
                retval.append(":");//NOI18N
                isPrefix = true;
            } else {
                retval.append(frag);
            }
        }
        return retval.toString();
    }
    public static Collection<? extends VariableName> getAllVariables(VariableScope varScope, String semiTypeName)  {
        List<VariableName> retval = new ArrayList<VariableName>();
        String[] fragments = semiTypeName.split("[@:]");
        for (int i = 0; i < fragments.length; i++) {
            String frag = fragments[i];
            if (frag.trim().length() == 0) continue;
            if (VariousUtils.VAR_TYPE_PREFIX.startsWith(frag)) {
                if (i+1 < fragments.length) {
                    String varName = fragments[++i];
                    VariableName var = varName != null ? ModelUtils.getFirst(varScope.getDeclaredVariables(), varName) : null;
                    if (var != null) {
                        retval.add(var);
                    } else {
                        return Collections.emptyList();
                    }
                }
            }
        }
        return retval;
    }

    private static Set<String> recursionDetection = new HashSet<String>();//#168868
    //TODO: needs to be improved to properly return more types
    public static Collection<? extends TypeScope> getType(final VariableScope varScope, String semiTypeName, int offset, boolean justDispatcher) throws IllegalStateException {
        Collection<? extends TypeScope> recentTypes = Collections.emptyList();
        Collection<? extends TypeScope> oldRecentTypes = Collections.emptyList();
        Stack<VariableName> fldVarStack = new Stack<VariableName>();

        if (semiTypeName != null && semiTypeName.contains("@")) {
            String operation = null;
            String[] fragments = semiTypeName.split("[@:]");
            int len = (justDispatcher) ? fragments.length - 1 : fragments.length;
            for (int i = 0; i < len; i++) {
                oldRecentTypes = recentTypes;
                String frag = fragments[i].trim();
                if (frag.length() == 0) {
                    continue;
                }
                String operationPrefix = (frag.endsWith(":")) ? frag : String.format("%s:", frag);
                if (VariousUtils.METHOD_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    operation = VariousUtils.METHOD_TYPE_PREFIX;
                } else if (VariousUtils.FUNCTION_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    operation = VariousUtils.FUNCTION_TYPE_PREFIX;
                } else if (VariousUtils.STATIC_METHOD_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    operation = VariousUtils.STATIC_METHOD_TYPE_PREFIX;
                } else if (VariousUtils.STATIC_FIELD__TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    operation = VariousUtils.FIELD_TYPE_PREFIX;
                } else if (VariousUtils.VAR_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    operation = VariousUtils.VAR_TYPE_PREFIX;
                } else if (VariousUtils.ARRAY_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    operation = VariousUtils.ARRAY_TYPE_PREFIX;
                } else if (VariousUtils.FIELD_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    operation = VariousUtils.FIELD_TYPE_PREFIX;
                } else if (VariousUtils.CONSTRUCTOR_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    operation = VariousUtils.CONSTRUCTOR_TYPE_PREFIX;
                } else {
                    if (operation == null) {
                        assert i == 0 : frag;
                        recentTypes = IndexScopeImpl.getTypes(QualifiedName.create(frag),varScope);
                    } else if (operation.startsWith(VariousUtils.CONSTRUCTOR_TYPE_PREFIX)) {
                        //new FooImpl()-> not allowed in php
                        return Collections.emptyList();
                    } else if (operation.startsWith(VariousUtils.METHOD_TYPE_PREFIX)) {
                        Set<TypeScope> newRecentTypes = new HashSet<TypeScope>();
                        for (TypeScope tScope : oldRecentTypes) {
                            Collection<? extends MethodScope> inheritedMethods = IndexScopeImpl.getMethods(tScope, frag, varScope, PhpModifiers.ALL_FLAGS);
                            for (MethodScope meth : inheritedMethods) {
                                newRecentTypes.addAll(meth.getReturnTypes(true));
                            }
                        }
                        recentTypes = newRecentTypes;
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.FUNCTION_TYPE_PREFIX)) {
                        Set<TypeScope> newRecentTypes = new HashSet<TypeScope>();
                        FunctionScope fnc = ModelUtils.getFirst(IndexScopeImpl.getFunctions(QualifiedName.create(frag), varScope));
                        if (fnc != null) {
                            newRecentTypes.addAll(fnc.getReturnTypes(true));
                        }
                        recentTypes = newRecentTypes;
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.STATIC_METHOD_TYPE_PREFIX)) {
                        Set<TypeScope> newRecentTypes = new HashSet<TypeScope>();
                        String[] frgs = frag.split("\\.");
                        assert frgs.length == 2;
                        String clsName = frgs[0];
                        if (clsName != null) {
                            Collection<? extends ClassScope> classes = IndexScopeImpl.getClasses(createQuery(clsName, varScope), varScope);
                            for (ClassScope cls : classes) {
                                Collection<? extends MethodScope> inheritedMethods = IndexScopeImpl.getMethods(cls, frgs[1], varScope, PhpModifiers.ALL_FLAGS);
                                for (MethodScope meth : inheritedMethods) {
                                    newRecentTypes.addAll(meth.getReturnTypes(true));
                                }
                            }
                        }
                        recentTypes = newRecentTypes;
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.VAR_TYPE_PREFIX)
                            || (operation.startsWith(VariousUtils.ARRAY_TYPE_PREFIX))) {
                        Set<TypeScope> newRecentTypes = new HashSet<TypeScope>();
                        String varName = frag;
                        VariableName var = ModelUtils.getFirst(varScope.getDeclaredVariables(), varName);
                        if (var != null) {
                           if (i+2 < len && VariousUtils.FIELD_TYPE_PREFIX.startsWith(fragments[i+1])) {
                            fldVarStack.push(var);
                           }
                            final String checkName = var.getName() + String.valueOf(offset);
                           boolean added = recursionDetection.add(checkName);
                            try {
                                if (added) {
                                    boolean isArray = operation.startsWith(VariousUtils.ARRAY_TYPE_PREFIX);
                                    if (isArray) {
                                        newRecentTypes.addAll(var.getArrayAccessTypes(offset));
                                    } else {
                                        newRecentTypes.addAll(var.getTypes(offset));
                                    }
                                }
                            } finally {
                                recursionDetection.remove(checkName);
                            }
                        }

                        if (newRecentTypes.isEmpty()) {
                            if (varScope instanceof MethodScope) {//NOI18N
                                MethodScope mScope = (MethodScope) varScope;
                                if ((frag.equals("this") || frag.equals("$this"))) {//NOI18N
                                    final Scope inScope = mScope.getInScope();
                                    if (inScope instanceof ClassScope) {
                                        String clsName = ((ClassScope) inScope).getName();
                                        newRecentTypes.addAll(IndexScopeImpl.getClasses(QualifiedName.create(clsName), varScope));
                                    }
                                }
                            }
                        }
                        recentTypes = newRecentTypes;
                        operation = null;

                    } else if (operation.startsWith(VariousUtils.FIELD_TYPE_PREFIX)) {
                        VariableName var = fldVarStack.isEmpty() ? null : fldVarStack.pop();
                        Set<TypeScope> newRecentTypes = new HashSet<TypeScope>();
                        String fldName = frag;
                        if (!fldName.startsWith("$")) {//NOI18N
                            fldName = "$" + fldName;//NOI18N
                        }
                        for (TypeScope type : oldRecentTypes) {
                            Collection<? extends FieldElement> inheritedFields = IndexScopeImpl.getFields(type, fldName, varScope, PhpModifiers.ALL_FLAGS);
                            for (FieldElement fieldElement : inheritedFields) {
                                if (var != null) {
                                    final Collection<? extends TypeScope> fieldTypes = var.getFieldTypes(fieldElement, offset);
                                    if (fieldTypes.isEmpty() && (fieldElement instanceof FieldElementImpl)) {
                                        newRecentTypes.addAll(((FieldElementImpl) fieldElement).getDefaultTypes());
                                    } else {
                                        newRecentTypes.addAll(fieldTypes);
                                    }
                                } else {
                                    newRecentTypes.addAll(fieldElement.getTypes(offset));
                                }
                            }
                        }
                        recentTypes = newRecentTypes;
                        operation = null;
                    } else {
                        throw new UnsupportedOperationException(operation);
                    }
                }
            }
        } else if (semiTypeName != null ) {
            QualifiedName qn = QualifiedName.create(semiTypeName);
            qn = qn.toNamespaceName().append(translateSpecialClassName(varScope, qn.getName()));
            if (semiTypeName.startsWith("\\")) { // NOI18N
                qn = qn.toFullyQualified();
            } else {
                NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(varScope);
                if (namespaceScope != null) {
                    Collection<QualifiedName> possibleFQN = getPossibleFQN(qn, offset, namespaceScope);
                    if (!possibleFQN.isEmpty()) {
                        qn = ModelUtils.getFirst(possibleFQN);
                    }
                }
            }
            final IndexScope indexScope = ModelUtils.getIndexScope(varScope);
            return indexScope.findTypes(qn);
        }

        return recentTypes;
    }

    private static QualifiedName createQuery(String semiTypeName, final Scope scope) {
        final QualifiedName query = QualifiedName.create(semiTypeName);
        return query.toNamespaceName().append(translateSpecialClassName(scope, query.getName()));
    }

    public static Stack<? extends ModelElement> getElemenst(FileScope topScope, final VariableScope varScope, String semiTypeName, int offset) throws IllegalStateException {
        Stack<ModelElement> emptyStack = new Stack<ModelElement>();
        Stack<ModelElement> retval = new Stack<ModelElement>();
        Stack<Collection<? extends TypeScope>> stack = new Stack<Collection<? extends TypeScope>>();

        TypeScope type = null;
        if (semiTypeName != null && semiTypeName.contains("@")) {
            String operation = null;
            String[] fragments = semiTypeName.split("[@:]");
            int len = fragments.length;
            for (int i = 0; i < len; i++) {
                String frag = fragments[i];
                if (frag.trim().length() == 0) {
                    continue;
                }
                String operationPrefix = (frag.endsWith(":")) ? frag : String.format("%s:", frag);
                if (VariousUtils.METHOD_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    operation = VariousUtils.METHOD_TYPE_PREFIX;
                } else if (VariousUtils.FUNCTION_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    assert operation == null;
                    operation = VariousUtils.FUNCTION_TYPE_PREFIX;
                } else if (VariousUtils.STATIC_METHOD_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    assert operation == null;
                    operation = VariousUtils.STATIC_METHOD_TYPE_PREFIX;
                } else if (VariousUtils.VAR_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    assert operation == null;
                    operation = VariousUtils.VAR_TYPE_PREFIX;
                } else if (VariousUtils.FIELD_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    assert operation == null;
                    operation = VariousUtils.FIELD_TYPE_PREFIX;
                } else if (VariousUtils.CONSTRUCTOR_TYPE_PREFIX.equalsIgnoreCase(operationPrefix)) {
                    assert operation == null;
                    operation = VariousUtils.CONSTRUCTOR_TYPE_PREFIX;
                } else {
                    if (operation == null) {
                        assert i == 0;

                        Collection<? extends TypeScope> types = IndexScopeImpl.getTypes(QualifiedName.create(frag), topScope);

                        if (!types.isEmpty()) {
                            stack.push(types);
                        }
                    } else if (operation.startsWith(VariousUtils.METHOD_TYPE_PREFIX)) {
                        Collection<? extends TypeScope> types = stack.isEmpty() ? null : stack.pop();
                        if (types == null || types.isEmpty()) {
                            return emptyStack;
                        }
                        TypeScope cls = ModelUtils.getFirst(types);
                        if (cls == null) {
                            return emptyStack;
                        }
                        final Collection<? extends MethodScope> methods = IndexScopeImpl.getMethods(cls, frag, topScope, PhpModifiers.ALL_FLAGS);
                        MethodScope meth = ModelUtils.getFirst(methods);
                        if (methods.isEmpty()) {
                            return emptyStack;
                        } else {
                            retval.push(meth);
                        }
                        types = meth.getReturnTypes(true);
                        if (types == null || types.isEmpty()) {
                            break;
                        }
                        stack.push(types);
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.FUNCTION_TYPE_PREFIX)) {
                        FunctionScope fnc = ModelUtils.getFirst(IndexScopeImpl.getFunctions(QualifiedName.create(frag), topScope));
                        if (fnc == null) {
                            break;
                        } else {
                            retval.push(fnc);
                        }
                        final Collection<? extends TypeScope> returnTypes = fnc.getReturnTypes(true);
                        type = ModelUtils.getFirst(returnTypes);
                        if (type == null) {
                            break;
                        }
                        stack.push(returnTypes);
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.CONSTRUCTOR_TYPE_PREFIX)) {
                        ClassScope cls = ModelUtils.getFirst(IndexScopeImpl.getClasses(QualifiedName.create(frag), topScope));
                        if (cls == null) {
                            break;
                        } else {
                            MethodScope meth = ModelUtils.getFirst(IndexScopeImpl.getMethods(cls, "__construct",topScope, PhpModifiers.ALL_FLAGS));//NOI18N
                            if (meth != null) {
                                retval.push(meth);
                            } else {
                                return emptyStack;
                            }
                        }
                        stack.push(Collections.singletonList(cls));
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.STATIC_METHOD_TYPE_PREFIX)) {
                        String[] frgs = frag.split("\\.");
                        assert frgs.length == 2;
                        String clsName = frgs[0];
                        if (clsName == null) {
                            return emptyStack;
                        }
                        ClassScope cls = ModelUtils.getFirst(IndexScopeImpl.getClasses(QualifiedName.create(clsName), topScope));
                        if (cls == null) {
                            return emptyStack;
                        }
                        MethodScope meth = ModelUtils.getFirst(IndexScopeImpl.getMethods(cls, frgs[1],topScope, PhpModifiers.ALL_FLAGS));
                                //ModelUtils.getFirst(cls.getMethods(frgs[1], PhpModifiers.STATIC));
                        if (meth == null) {
                            return emptyStack;
                        } else {
                            retval.push(meth);
                        }
                        final Collection<? extends TypeScope> returnTypes = meth.getReturnTypes(true);
                        type = ModelUtils.getFirst(returnTypes);
                        if (type == null) {
                            break;
                        }
                        stack.push(returnTypes);
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.VAR_TYPE_PREFIX)) {
                        type = null;
                        if (varScope instanceof MethodScope) {//NOI18N
                            MethodScope mScope = (MethodScope) varScope;
                            if ((frag.equals("this") || frag.equals("$this"))) {//NOI18N
                                type = (ClassScope) mScope.getInScope();
                            }
                            if (type != null) {
                                stack.push(Collections.singletonList(type));
                                operation = null;
                            }
                        } else if (varScope instanceof NamespaceScope) {
                            NamespaceScope nScope = (NamespaceScope) varScope;
                            VariableName varName = ModelUtils.getFirst(nScope.getDeclaredVariables(), frag);
                            if (varName != null) {
                                final Collection<? extends TypeScope> types = varName.getTypes(offset);
                                type = ModelUtils.getFirst(types);
                                if (type != null) {
                                    stack.push(types);
                                    operation = null;
                                }
                            }
                        }
                        if (type == null) {
                            List<? extends VariableName> variables = ModelUtils.filter(varScope.getDeclaredVariables(), frag);
                            if (!variables.isEmpty()) {
                                VariableName varName = ModelUtils.getFirst(variables);
                                final Collection<? extends TypeScope> types = varName != null ? varName.getTypes(offset) : null;
                                type = types != null ? ModelUtils.getFirst(types) : null;
                                if (varName != null) {
                                    retval.push(varName);
                                }
                                if (type != null) {
                                    stack.push(types);
                                } else {
                                    break;
                                }
                                operation = null;
                            }
                        }
                    } else if (operation.startsWith(VariousUtils.FIELD_TYPE_PREFIX)) {
                        Collection<? extends TypeScope> types = stack.isEmpty() ? null : stack.pop();
                        if (types == null || types.isEmpty()) {
                            return emptyStack;
                        }
                        TypeScope cls = ModelUtils.getFirst(types);
                        if (cls == null || !(cls instanceof ClassScope)) {
                            return emptyStack;
                        }
                        FieldElement fieldElement = ModelUtils.getFirst(IndexScopeImpl.getFields((ClassScope)cls,
                                !frag.startsWith("$") ? String.format("%s%s", "$",frag) : frag, topScope, PhpModifiers.ALL_FLAGS));//NOI18N
                        if (fieldElement == null) {
                            return emptyStack;
                        } else {
                            retval.push(fieldElement);
                        }
                        final Collection<? extends TypeScope> fieldTypes = fieldElement.getTypes(offset);
                        type = ModelUtils.getFirst(fieldTypes);
                        if (type == null) {
                            break;
                        }
                        stack.push(fieldTypes);
                        operation = null;
                    } else {
                        throw new UnsupportedOperationException(operation);
                    }
                }
            }
        }
        return retval;
    }

    private static void createVariableBaseChain(VariableBase node, Stack<VariableBase> stack) {
        stack.push(node);
        if (node instanceof MethodInvocation) {
            createVariableBaseChain(((MethodInvocation) node).getDispatcher(), stack);
        } else if (node instanceof FieldAccess) {
            createVariableBaseChain(((FieldAccess) node).getDispatcher(), stack);
        }
    }

    private static String extractVariableTypeFromVariableBase(VariableBase varBase, Map<String, AssignmentImpl> allAssignments) {
        if (varBase instanceof Variable) {
            String varName = CodeUtils.extractVariableName((Variable) varBase);
            AssignmentImpl assignmentImpl = allAssignments.get(varName);
            if (assignmentImpl != null) {
                String semiTypeName = assignmentImpl.typeNameFromUnion();
                if (semiTypeName != null) {
                    return semiTypeName;
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
        } else if (varBase instanceof FieldAccess) {
            FieldAccess fieldAccess = (FieldAccess) varBase;
            String filedName = CodeUtils.extractVariableName(fieldAccess.getField());
            if (filedName != null) {
                return "@" + FIELD_TYPE_PREFIX + filedName;
            }
        } else if (varBase instanceof StaticFieldAccess) {
            StaticFieldAccess fieldAccess = (StaticFieldAccess) varBase;
            String clsName = CodeUtils.extractUnqualifiedName(fieldAccess.getClassName());
            String fldName = CodeUtils.extractVariableName(fieldAccess.getField());
            if (clsName != null && fldName != null) {
                return clsName+"@" + STATIC_FIELD__TYPE_PREFIX + fldName;
            }
        }

        return null;
    }
    public static String resolveFileName(Include include) {
        Expression e = include.getExpression();

        if (e instanceof ParenthesisExpression) {
            e = ((ParenthesisExpression) e).getExpression();
        }

        if (e instanceof Scalar) {
            Scalar s = (Scalar) e;

            if (Type.STRING == s.getScalarType()) {
                String fileName = s.getStringValue();
                fileName = fileName.length() >= 2 ? fileName.substring(1, fileName.length() - 1) : fileName;//TODO: not nice
                return fileName;
            }
        }

        return null;
    }

    /**
     * @param sourceFile needs to be data file (not folder)
     */
    public static FileObject resolveInclude(FileObject sourceFile, Include include) {
        Parameters.notNull("sourceFile", sourceFile);
        if (sourceFile.isFolder()) {
            throw new IllegalArgumentException(FileUtil.getFileDisplayName(sourceFile));
        }
        return  resolveInclude(sourceFile, resolveFileName(include));
    }

    public static FileObject resolveInclude(FileObject sourceFile, String fileName) {
        FileObject retval = null;
        if (fileName != null) {
            File absoluteFile = new File(fileName);
            if (absoluteFile.exists()) {
                retval = FileUtil.toFileObject(FileUtil.normalizeFile(absoluteFile));
            } else {
                FileObject parent = sourceFile.getParent();
                if (parent != null) {
                    retval = PhpSourcePath.resolveFile(parent, fileName);
                }
            }
        }
        return retval;
    }

    private static final Collection<PHPTokenId> CTX_DELIMITERS = Arrays.asList(
            PHPTokenId.PHP_OPENTAG, PHPTokenId.PHP_SEMICOLON, PHPTokenId.PHP_CURLY_OPEN, PHPTokenId.PHP_CURLY_CLOSE,
            PHPTokenId.PHP_RETURN, PHPTokenId.PHP_OPERATOR, PHPTokenId.PHP_ECHO,
            PHPTokenId.PHP_EVAL, PHPTokenId.PHP_NEW, PHPTokenId.PHP_NOT, PHPTokenId.PHP_CASE,
            PHPTokenId.PHP_IF, PHPTokenId.PHP_ELSE, PHPTokenId.PHP_ELSEIF, PHPTokenId.PHP_PRINT,
            PHPTokenId.PHP_FOR, PHPTokenId.PHP_FOREACH, PHPTokenId.PHP_WHILE,
            PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_LINE_COMMENT,
            PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING, PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE);


    public enum State {
        START, METHOD, INVALID, VARBASE, DOLAR, PARAMS, ARRAYREFERENCE, REFERENCE,
        STATIC_REFERENCE, FUNCTION, FIELD, VARIABLE, ARRAY_FIELD, ARRAY_VARIABLE, CLASSNAME, STOP, IDX
    };

    public static String getSemiType(TokenSequence<PHPTokenId> tokenSequence, State state, VariableScope varScope) throws IllegalStateException {
        int commasCount = 0;
        int anchor = -1;
        int leftBraces = 0;
        int rightBraces = State.PARAMS.equals(state) ? 1 : 0;
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
                    case IDX:
                        if (isLeftArryBracket(token)) {
                            state = State.ARRAYREFERENCE;
                        } else if (CTX_DELIMITERS.contains(token.id())) {
                            state = State.INVALID;
                        }
                        break;
                    case ARRAYREFERENCE:
                    case REFERENCE:
                        boolean isArray = state.equals(State.ARRAYREFERENCE);
                        state = State.INVALID;
                        if (isRightBracket(token)) {
                            rightBraces++;
                            state = State.PARAMS;
                        } else if (isRightArryBracket(token)) {
                            state = State.IDX;
                        } else if (isString(token)) {
                            metaAll.insert(0, token.text().toString());
                            state = isArray ? State.ARRAY_FIELD : State.FIELD;
                        } else if (isVariable(token)) {
                            metaAll.insert(0, token.text().toString());
                            state = isArray? State.ARRAY_VARIABLE : State.VARBASE;
                        }
                        break;
                    case STATIC_REFERENCE:
                        state = State.INVALID;
                        if (isString(token)) {
                            metaAll.insert(0, "@" + VariousUtils.FIELD_TYPE_PREFIX);
                            metaAll.insert(0, token.text().toString());
                            state = State.CLASSNAME;
                        } else if (isSelf(token) || isParent(token) || isStatic(token)) {
                            metaAll.insert(0, "@" + VariousUtils.FIELD_TYPE_PREFIX);
                            metaAll.insert(0, translateSpecialClassName(varScope, token.text().toString()));
                            //TODO: maybe rather introduce its own State
                            state = State.CLASSNAME;
                        }
                        break;
                    case PARAMS:
                        if (isWhiteSpace(token)) {
                            state = State.PARAMS;
                        } else if (isComma(token)) {
                            if (metaAll.length() == 0) {
                                commasCount++;
                            }
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
                    case ARRAY_FIELD:
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
                    case ARRAY_VARIABLE:
                    case VARIABLE:
                        if (state.equals(State.ARRAY_VARIABLE)) {
                            metaAll.insert(0, "@" + VariousUtils.ARRAY_TYPE_PREFIX);
                        } else {
                            metaAll.insert(0, "@" + VariousUtils.VAR_TYPE_PREFIX);
                        }
                    case CLASSNAME:
                        //TODO: self, parent not handled yet
                        //TODO: maybe rather introduce its own State for self, parent
                        if (isNamespaceSeparator(token)) {
                            if (tokenSequence.movePrevious()) {
                                metaAll.insert(0, token.text().toString());
                                token = tokenSequence.token();
                                if (isString(token)) {
                                    metaAll.insert(0, token.text().toString());
                                    break;
                                }
                            }
                        }
                        state = State.STOP;
                        break;
                }
            } else {
                if (state.equals(State.CLASSNAME)) {
                    state = State.STOP;
                    break;
                } else if (state.equals(State.METHOD)) {
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
            String retval = metaAll.toString();
            if (retval != null) {
                return retval;
            }
        }
        return null;
    }

    // XXX
    public static String getVariableName(String semiType) {
        if (semiType != null) {
            String prefix = "@" + VariousUtils.VAR_TYPE_PREFIX; // NOI18N
            if (semiType.startsWith(prefix)) {
                return semiType.substring(prefix.length(), semiType.lastIndexOf("@")); // NOI18N
            }
        }
        return null;
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

    private static String translateSpecialClassName(Scope scp, String clsName) {
        ClassScope classScope = null;
        if (scp instanceof ClassScope) {
            classScope = (ClassScope)scp;
        } else if (scp instanceof MethodScope) {
            MethodScope msi = (MethodScope) scp;
            classScope = (ClassScope) msi.getInScope();
        }
        if (classScope != null) {
            if ("self".equals(clsName) || "this".equals(clsName) || "static".equals(clsName)) {//NOI18N
                clsName = classScope.getName();
            } else if ("parent".equals(clsName)) {
                ClassScope clzScope = ModelUtils.getFirst(classScope.getSuperClasses());
                if (clzScope != null) {
                    clsName = clzScope.getName();
                }
            }
        }
        return clsName;
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
    private static boolean isRightArryBracket(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_TOKEN) && "]".contentEquals(token.text());//NOI18N
    }
    private static boolean isLeftArryBracket(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_TOKEN) && "[".contentEquals(token.text());//NOI18N
    }
    private static boolean isComma(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_TOKEN) && ",".contentEquals(token.text());//NOI18N
    }

    private static boolean isReference(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_OBJECT_OPERATOR);
    }
    private static boolean isNamespaceSeparator(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_NS_SEPARATOR);
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

    private static boolean isStatic(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_STATIC);
    }

    private static boolean isParent(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_PARENT);
    }

    private static boolean isString(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_STRING);
    }
    public static Collection<? extends TypeScope> getStaticTypeName(Scope inScope, String staticTypeName) {
        TypeScope csi = null;
        if (inScope instanceof MethodScope) {
            MethodScope msi = (MethodScope) inScope;
            csi = (ClassScope) msi.getInScope();
        }
        if (inScope instanceof ClassScope || inScope instanceof InterfaceScope) {
            csi = (TypeScope)inScope;
        }
        if (csi != null) {
            if ("self".equals(staticTypeName)) {
                return Collections.singletonList(csi);
            } else if ( "parent".equals(staticTypeName) && (csi instanceof ClassScope)) {
                return ((ClassScope)csi).getSuperClasses();
            }
        }
        return IndexScopeImpl.getTypes(QualifiedName.create(staticTypeName), inScope);
    }

    public static QualifiedName getPreferredName(QualifiedName fullName, NamespaceScope contextNamespace) {
        Collection<QualifiedName> allNames = getAllNames(fullName, contextNamespace);
        int segmentCount = Integer.MAX_VALUE;
        QualifiedName retval = null;
        for (QualifiedName qualifiedName : allNames) {
            int size = qualifiedName.getSegments().size();
            if (size < segmentCount) {
                retval = qualifiedName;
                segmentCount = size;
            }
        }
        return retval;
    }
    public static Collection<QualifiedName> getAllNames(QualifiedName fullName, NamespaceScope contextNamespace) {
        Set<QualifiedName> namesProposals = new HashSet<QualifiedName>();
        namesProposals.addAll(getRelatives(contextNamespace, fullName));
        namesProposals.add(fullName.toFullyQualified());
        return namesProposals;
    }
    public static Collection<QualifiedName> getRelativesToUses(NamespaceScope contextNamespace, QualifiedName fullName) {
        Set<QualifiedName> namesProposals = new HashSet<QualifiedName>();
        Collection<? extends UseElement> declaredUses = contextNamespace.getDeclaredUses();
        for (UseElement useElement : declaredUses) {
            QualifiedName proposedName = QualifiedName.getSuffix(fullName, QualifiedName.create(useElement.getName()), true);
            if (proposedName != null) {
                namesProposals.add(proposedName);
            }
        }
        return namesProposals;
    }
    public static Collection<QualifiedName> getRelativesToNamespace( NamespaceScope contextNamespace, QualifiedName fullName) {
        Set<QualifiedName> namesProposals = new HashSet<QualifiedName>();
        QualifiedName proposedName = QualifiedName.getSuffix(fullName, QualifiedName.create(contextNamespace), false);
        if (proposedName != null) {
            namesProposals.add(proposedName);
        }
        return namesProposals;
    }
    public static Collection<QualifiedName> getRelatives( NamespaceScope contextNamespace, QualifiedName fullName) {
        Set<QualifiedName> namesProposals = new HashSet<QualifiedName>();
        namesProposals.addAll(getRelativesToNamespace(contextNamespace, fullName));
        namesProposals.addAll(getRelativesToUses(contextNamespace, fullName));
        return namesProposals;
    }

    public static Collection<QualifiedName> getComposedNames(QualifiedName name, NamespaceScope contextNamespace) {
        Collection<? extends UseElement> declaredUses = contextNamespace.getDeclaredUses();
        Set<QualifiedName> namesProposals = new HashSet<QualifiedName>();
        if (!name.getKind().isFullyQualified()) {
            QualifiedName proposedName = QualifiedName.create(contextNamespace).append(name).toFullyQualified();
            if (proposedName != null) {
                namesProposals.add(proposedName);
            }
            for (UseElement useElement : declaredUses) {
                final QualifiedName useQName = QualifiedName.create(useElement.getName());
                proposedName = useQName.toNamespaceName().append(name).toFullyQualified();
                if (proposedName != null) {
                    namesProposals.add(proposedName);
                }
                if (!useQName.getName().equalsIgnoreCase(name.getName())) {
                    proposedName = useQName.append(name).toFullyQualified();
                    if (proposedName != null) {
                        namesProposals.add(proposedName);
                    }
                }
            }
        }
        namesProposals.add(name);
        return namesProposals;
    }

    /**
     * This method is trying to guess the full qualified name  from a name.
     * Names are resolved following these resolution rules like in the php runtime:
     *
     * 1. Calls to fully qualified functions, classes or constants are resolved
     * at compile-time. For instance new \A\B resolves to class A\B.
     * 2. All unqualified and qualified names (not fully qualified names) are
     * translated during compilation according to current import rules.
     * For example, if the namespace A\B\C is imported as C, a call to C\D\e()
     * is translated to A\B\C\D\e().
     * 3. Inside a namespace, all qualified names not translated according to
     * import rules have the current namespace prepended. For example, if a call
     * to C\D\e() is performed within namespace A\B, it is translated to A\B\C\D\e().
     * 4. Unqualified class names are translated during compilation according
     * to current import rules (full name substituted for short imported name).
     * In example, if the namespace A\B\C is imported as C, new C() is translated
     * to new A\B\C().
     * 5. Inside namespace (say A\B), calls to unqualified functions are resolved
     * at run-time. Here is how a call to function foo() is resolved:
     *      1. It looks for a function from the current namespace: A\B\foo().
     *      2. It tries to find and call the global function foo().
     * 6. Inside namespace (say A\B), calls to unqualified or qualified class
     * names (not fully qualified class names) are resolved at run-time. Here is
     * how a call to new C() or new D\E() is resolved. For new C():
     *      1. It looks for a class from the current namespace: A\B\C.
     *      2. It attempts to autoload A\B\C.
     * For new D\E():
     *      1. It looks for a class by prepending the current namespace: A\B\D\E.
     *      2. It attempts to autoload A\B\D\E.
     * To reference any global class in the global namespace, its fully qualified name new \C() must be used.
     *
     * @param name the qualified name that should be resolved according the
     * mentioned rules.
     * @param nameOffset Offset of the name that should be resolved. The resolving
     * full qualified names depends on the location of imports (use declaration).
     * @param contextNamespace Namespace where is the qualified name located
     * @return collection of full qualified names that fits the input name in the
     * name space context. Usually the method returns just one, but it can return, if is not clear
     * whether the name belongs to the defined namespace or to the default one.
     */
    public static Collection<QualifiedName> getPossibleFQN(QualifiedName name, int nameOffset, NamespaceScope contextNamespace){
        Set<QualifiedName> namespaces = new HashSet<QualifiedName>();
        boolean resolved = false;
        if (name.getKind() == QualifiedNameKind.FULLYQUALIFIED) {
            namespaces.add(name);
            resolved = true;
        } else {
            Collection<? extends UseElement> uses = contextNamespace.getDeclaredUses();
            if (uses.size() > 0) {
                for(UseElement useDeclaration : contextNamespace.getDeclaredUses()) {
                    if (useDeclaration.getOffset() < nameOffset) {
                        String firstNameSegment = name.getSegments().getFirst();
                        QualifiedName returnName = null;
                        if ((useDeclaration.getAliasedName() != null
                                    && firstNameSegment.equals(useDeclaration.getAliasedName().getAliasName()))) {
                            returnName = useDeclaration.getAliasedName().getRealName();
                        } else {
                            returnName = QualifiedName.create(useDeclaration.getName());
                            if (!firstNameSegment.equals(returnName.getSegments().getLast())) {
                                returnName = null;
                            }
                        }
                        if (returnName != null) {
                            for (int i = 1; i < name.getSegments().size(); i++) {
                                returnName = returnName.append(name.getSegments().get(i));
                            }
                            namespaces.add(returnName.toFullyQualified());
                            resolved = true;
                        }
                    }
                }
            }
        }
        if (!resolved) {
            if (name.getKind() == QualifiedNameKind.UNQUALIFIED) {
                namespaces.add(contextNamespace.getNamespaceName().append(name).toFullyQualified());
            } else {
                // the name is qualified -> append the name to the namespace name
                namespaces.add(QualifiedName.create(contextNamespace).append(name).toFullyQualified());
            }
        }
        return namespaces;
    }

}
