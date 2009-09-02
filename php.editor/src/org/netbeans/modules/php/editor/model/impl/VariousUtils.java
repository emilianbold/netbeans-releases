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
import org.netbeans.modules.php.editor.NamespaceIndexFilter;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.QualifiedName;
import org.netbeans.modules.php.editor.model.QualifiedNameKind;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TypeScope;
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
import org.netbeans.modules.php.editor.parser.astnodes.Scalar.Type;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;
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
                    String parts[] = tag.getValue().split("\\s+", 3); //NOI18N

                    if (parts.length > 1) {
                        String[] typeNames = parts[0].split("\\|", 2);
                        List<QualifiedName> types = new ArrayList<QualifiedName>();
                        for (String tName : typeNames) {
                            types.add(QualifiedName.create(tName));
                        }
                        String name = parts[1].split("\\s+", 2)[0];
                        retval.put(name, types);
                    }
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
                    String parts[] = tag.getValue().split("\\s+", 2); //NOI18N

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
            return extractTypeFroVariableBase((VariableBase) expression, allAssignments);//extractVariableTypeFromVariableBase(varBase);
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
    public static Collection<? extends TypeScope> getType( VariableScope varScope, String semiTypeName, int offset, boolean justDispatcher) throws IllegalStateException {
        Collection<? extends TypeScope> recentTypes = Collections.emptyList();
        Collection<? extends TypeScope> oldRecentTypes = Collections.emptyList();
        Stack<VariableName> fldVarStack = new Stack<VariableName>();
        
        if (semiTypeName != null && semiTypeName.contains("@")) {
            String operation = null;
            String[] fragments = semiTypeName.split("[@:]");
            int len = (justDispatcher) ? fragments.length - 1 : fragments.length;
            for (int i = 0; i < len; i++) {
                oldRecentTypes = recentTypes;                
                String frag = fragments[i];
                if (frag.trim().length() == 0) {
                    continue;
                }
                if (VariousUtils.METHOD_TYPE_PREFIX.startsWith(frag)) {
                    operation = VariousUtils.METHOD_TYPE_PREFIX;
                } else if (VariousUtils.FUNCTION_TYPE_PREFIX.startsWith(frag)) {
                    operation = VariousUtils.FUNCTION_TYPE_PREFIX;
                } else if (VariousUtils.STATIC_METHOD_TYPE_PREFIX.startsWith(frag)) {
                    operation = VariousUtils.STATIC_METHOD_TYPE_PREFIX;
                } else if (VariousUtils.STATIC_FIELD__TYPE_PREFIX.startsWith(frag)) {
                    operation = VariousUtils.FIELD_TYPE_PREFIX;
                } else if (VariousUtils.VAR_TYPE_PREFIX.startsWith(frag)) {
                    operation = VariousUtils.VAR_TYPE_PREFIX;
                } else if (VariousUtils.FIELD_TYPE_PREFIX.startsWith(frag)) {
                    operation = VariousUtils.FIELD_TYPE_PREFIX;
                } else {
                    if (operation == null) {
                        assert i == 0;
                        NamespaceIndexFilter filter = new NamespaceIndexFilter(frag);
                        QualifiedNameKind kind = filter.getKind();
                        String query = kind.isUnqualified() ? frag : filter.getName();
                        recentTypes = CachingSupport.getClasses(query,varScope);
                        if (!kind.isUnqualified()) {
                            recentTypes = filter.filterModelElements(recentTypes, true);
                        }
                    } else if (operation.startsWith(VariousUtils.METHOD_TYPE_PREFIX)) {
                        List<TypeScope> newRecentTypes = new ArrayList<TypeScope>();
                        for (TypeScope tScope : oldRecentTypes) {
                            Collection<? extends MethodScope> inheritedMethods = CachingSupport.getInheritedMethods(tScope, frag, varScope, PHPIndex.ANY_ATTR);
                            for (MethodScope meth : inheritedMethods) {
                                newRecentTypes.addAll(meth.getReturnTypes(true));
                            }
                        }
                        recentTypes = newRecentTypes;
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.FUNCTION_TYPE_PREFIX)) {
                        List<TypeScope> newRecentTypes = new ArrayList<TypeScope>();
                        FunctionScope fnc = ModelUtils.getFirst(CachingSupport.getFunctions(frag, varScope));
                        if (fnc != null) {
                            newRecentTypes.addAll(fnc.getReturnTypes(true));
                        }
                        recentTypes = newRecentTypes;
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.STATIC_METHOD_TYPE_PREFIX)) {
                        List<TypeScope> newRecentTypes = new ArrayList<TypeScope>();
                        String[] frgs = frag.split("\\.");
                        assert frgs.length == 2;
                        String clsName = frgs[0];
                        if (clsName != null) {
                            boolean parent = false;
                            if (varScope instanceof MethodScope) {//NOI18N
                                if ("self".equals(clsName)) {
                                    Scope inScope = varScope.getInScope();
                                    clsName = inScope.getName();
                                } else if ("parent".equals(clsName)) {
                                    Scope inScope = varScope.getInScope();
                                    clsName = inScope.getName();
                                    parent = true;
                                }
                            }
                            NamespaceIndexFilter filter = new NamespaceIndexFilter(frag);
                            QualifiedNameKind kind = filter.getKind();
                            String query = kind.isUnqualified() ? frag : filter.getName();
                            recentTypes = CachingSupport.getClasses(query, varScope);
                            Collection<? extends ClassScope> classes = CachingSupport.getClasses(clsName, varScope);
                            if (!kind.isUnqualified()) {
                                classes = filter.filterModelElements(classes, true);
                            }
                            for (ClassScope cls : classes) {
                                if (parent) {
                                    cls = ModelUtils.getFirst(cls.getSuperClasses());
                                    if (cls == null) continue;
                                }
                                Collection<? extends MethodScope> inheritedMethods = CachingSupport.getInheritedMethods(cls, frgs[1], varScope, PHPIndex.ANY_ATTR);
                                for (MethodScope meth : inheritedMethods) {
                                   newRecentTypes.addAll(meth.getReturnTypes(true));
                                }
                            }
                        }
                        recentTypes = newRecentTypes;
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.VAR_TYPE_PREFIX)) {
                        List<TypeScope> newRecentTypes = new ArrayList<TypeScope>();
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
                                    newRecentTypes.addAll(var.getTypes(offset));
                                } 
                            } finally {
                                recursionDetection.remove(checkName);
                            }
                        }

                        if (newRecentTypes.isEmpty()) {
                            if (varScope instanceof MethodScope) {//NOI18N
                                MethodScope mScope = (MethodScope) varScope;
                                if ((frag.equals("this") || frag.equals("$this"))) {//NOI18N
                                    String clsName = ((ClassScope) mScope.getInScope()).getName();
                                    newRecentTypes.addAll(CachingSupport.getClasses(clsName, varScope));
                                }
                            }
                        }
                        recentTypes = newRecentTypes;
                        operation = null;
                        
                    } else if (operation.startsWith(VariousUtils.FIELD_TYPE_PREFIX)) {
                        VariableName var = fldVarStack.isEmpty() ? null : fldVarStack.pop();
                        List<TypeScope> newRecentTypes = new ArrayList<TypeScope>();
                        String fldName = frag;
                        if (!fldName.startsWith("$")) {//NOI18N
                            fldName = "$" + fldName;//NOI18N
                        }
                        for (TypeScope type : oldRecentTypes) {
                            if (type instanceof ClassScope) {
                                ClassScope cls = (ClassScope) type;
                                Collection<? extends FieldElement> inheritedFields = CachingSupport.getInheritedFields(cls, fldName, varScope, PHPIndex.ANY_ATTR);
                                for (FieldElement fieldElement : inheritedFields) {
                                    if (var != null) {
                                        newRecentTypes.addAll(var.getFieldTypes(fieldElement, offset));
                                    } else {
                                        newRecentTypes.addAll(fieldElement.getTypes(offset));
                                    }
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
            NamespaceIndexFilter filter = new NamespaceIndexFilter(semiTypeName);
            QualifiedName qn = QualifiedName.create(semiTypeName);
            final QualifiedNameKind kind = qn.getKind();
            String query = kind.isUnqualified() ? semiTypeName : filter.getName();
            Collection<? extends TypeScope> retval = new ArrayList<TypeScope>(CachingSupport.getTypes( query, varScope));
            if (retval.isEmpty() && varScope instanceof  MethodScope) {
                query = translateSpecialClassName(varScope, query);
                retval = new ArrayList<TypeScope>(CachingSupport.getTypes( query, varScope));
            }

            if (!kind.isUnqualified()) {
                retval = filter.filterModelElements(retval, true);
            }
            return retval;
        }
       
        return recentTypes;
    }

    public static Stack<? extends ModelElement> getElemenst(FileScope topScope, final VariableScope varScope, String semiTypeName, int offset) throws IllegalStateException {
        Stack<ModelElement> emptyStack = new Stack<ModelElement>();
        Stack<ModelElement> retval = new Stack<ModelElement>();
        Stack<String> stack = new Stack<String>();
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
                if (VariousUtils.METHOD_TYPE_PREFIX.startsWith(frag)) {
                    operation = VariousUtils.METHOD_TYPE_PREFIX;
                } else if (VariousUtils.FUNCTION_TYPE_PREFIX.startsWith(frag)) {
                    assert operation == null;
                    operation = VariousUtils.FUNCTION_TYPE_PREFIX;
                } else if (VariousUtils.STATIC_METHOD_TYPE_PREFIX.startsWith(frag)) {
                    assert operation == null;
                    operation = VariousUtils.STATIC_METHOD_TYPE_PREFIX;
                } else if (VariousUtils.VAR_TYPE_PREFIX.startsWith(frag)) {
                    assert operation == null;
                    operation = VariousUtils.VAR_TYPE_PREFIX;
                } else if (VariousUtils.FIELD_TYPE_PREFIX.startsWith(frag)) {
                    assert operation == null;
                    operation = VariousUtils.FIELD_TYPE_PREFIX;
                } else if (VariousUtils.CONSTRUCTOR_TYPE_PREFIX.startsWith(frag)) {
                    assert operation == null;
                    operation = VariousUtils.CONSTRUCTOR_TYPE_PREFIX;
                } else {
                    if (operation == null) {
                        assert i == 0;
                        stack.push(frag);
                    } else if (operation.startsWith(VariousUtils.METHOD_TYPE_PREFIX)) {
                        String clsName = stack.isEmpty() ? null : stack.pop();
                        if (clsName == null) {
                            return emptyStack;
                        }
                        ClassScope cls = ModelUtils.getFirst(CachingSupport.getClasses(clsName,topScope));
                        if (cls == null) {
                            return emptyStack;
                        }
                        MethodScope meth = ModelUtils.getFirst(CachingSupport.getInheritedMethods(cls, frag, topScope, PHPIndex.ANY_ATTR));
                        if (meth == null) {
                            return emptyStack;
                        } else {
                            retval.push(meth);
                        }
                        type = ModelUtils.getFirst(meth.getReturnTypes(true));
                        if (type == null) {
                            semiTypeName = null;
                            break;
                        }
                        stack.push(type.getName());
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.FUNCTION_TYPE_PREFIX)) {
                        FunctionScope fnc = ModelUtils.getFirst(CachingSupport.getFunctions(frag, topScope));
                        if (fnc == null) {
                            semiTypeName = null;
                            break;
                        } else {
                            retval.push(fnc);
                        }
                        type = ModelUtils.getFirst(fnc.getReturnTypes(true));
                        if (type == null) {
                            semiTypeName = null;
                            break;
                        }
                        stack.push(type.getName());
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.CONSTRUCTOR_TYPE_PREFIX)) {
                        ClassScope cls = ModelUtils.getFirst(CachingSupport.getClasses(frag, topScope));
                        if (cls == null) {
                            semiTypeName = null;
                            break;
                        } else {
                            MethodScope meth = ModelUtils.getFirst(CachingSupport.getMethods(cls, "__construct",topScope, PHPIndex.ANY_ATTR));//NOI18N
                            if (meth != null) {
                                retval.push(meth);
                            } else {
                                return emptyStack;
                            }
                        }
                        stack.push(cls.getName());
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.STATIC_METHOD_TYPE_PREFIX)) {
                        String[] frgs = frag.split("\\.");
                        assert frgs.length == 2;
                        String clsName = frgs[0];
                        if (clsName == null) {
                            return emptyStack;
                        }
                        ClassScope cls = ModelUtils.getFirst(CachingSupport.getClasses(clsName, topScope));
                        if (cls == null) {
                            return emptyStack;
                        }
                        MethodScope meth = ModelUtils.getFirst(CachingSupport.getMethods(cls, frgs[1],topScope, PHPIndex.ANY_ATTR));
                                //ModelUtils.getFirst(cls.getMethods(frgs[1], PhpModifiers.STATIC));
                        if (meth == null) {
                            return emptyStack;
                        } else {
                            retval.push(meth);
                        }
                        type = ModelUtils.getFirst(meth.getReturnTypes(true));
                        if (type == null) {
                            semiTypeName = null;
                            break;
                        }
                        stack.push(type.getName());
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.VAR_TYPE_PREFIX)) {
                        type = null;
                        if (varScope instanceof MethodScope) {//NOI18N
                            MethodScope mScope = (MethodScope) varScope;
                            if ((frag.equals("this") || frag.equals("$this"))) {//NOI18N
                                type = (ClassScope) mScope.getInScope();
                            }
                            if (type != null) {
                                stack.push(type.getName());
                                operation = null;
                            }
                        } else if (varScope instanceof NamespaceScope) {
                            NamespaceScope nScope = (NamespaceScope) varScope;
                            VariableName varName = ModelUtils.getFirst(nScope.getDeclaredVariables(), frag);
                            if (varName != null) {
                                type = ModelUtils.getFirst(varName.getTypes(offset));
                                if (type != null) {
                                    stack.push(type.getName());
                                    operation = null;
                                }
                            }
                        }
                        if (type == null) {
                            List<? extends VariableName> variables = ModelUtils.filter(varScope.getDeclaredVariables(), frag);
                            if (!variables.isEmpty()) {
                                VariableName varName = ModelUtils.getFirst(variables);
                                type = varName != null ? ModelUtils.getFirst(varName.getTypes(offset)) : null;
                                if (varName != null) {
                                    retval.push(varName);
                                }
                                if (type != null) {
                                    stack.push(type.getName());
                                } else {
                                    semiTypeName = null;
                                    break;
                                }
                                operation = null;
                            }
                        }
                    } else if (operation.startsWith(VariousUtils.FIELD_TYPE_PREFIX)) {
                        String clsName = stack.isEmpty() ? null : stack.pop();
                        if (clsName == null) {
                            return emptyStack;
                        }
                        ClassScope cls = ModelUtils.getFirst(CachingSupport.getClasses(clsName,topScope));
                        if (cls == null) {
                            return emptyStack;
                        }
                        FieldElement fieldElement = ModelUtils.getFirst(CachingSupport.getInheritedFields(cls, 
                                !frag.startsWith("$") ? String.format("%s%s", "$",frag) : frag, topScope, PHPIndex.ANY_ATTR));//NOI18N
                        if (fieldElement == null) {
                            return emptyStack;
                        } else {
                            retval.push(fieldElement);
                        }
                        type = ModelUtils.getFirst(fieldElement.getTypes(offset));
                        if (type == null) {
                            semiTypeName = null;
                            break;
                        }
                        stack.push(type.getName());
                        operation = null;
                    } else {
                        throw new UnsupportedOperationException(operation);
                    }
                }
            }
            if (stack.size() == 1) {
                semiTypeName = stack.pop();
            }
        //throw new UnsupportedOperationException("Not supported yet.");
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
        FileObject retval = null;
        if (sourceFile.isFolder()) {
            throw new IllegalArgumentException(FileUtil.getFileDisplayName(sourceFile));
        }
        String fileName = resolveFileName(include);        
        if (fileName != null) {
            FileObject parent = sourceFile.getParent();
            assert parent != null : FileUtil.getFileDisplayName(sourceFile);
            retval = PhpSourcePath.resolveFile(parent, fileName);
        }
        return  retval;
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
        START, METHOD, INVALID, VARBASE, DOLAR, PARAMS, REFERENCE, STATIC_REFERENCE, FUNCTION, FIELD, VARIABLE, CLASSNAME, STOP
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
                            metaAll.insert(0, "@" + VariousUtils.FIELD_TYPE_PREFIX);
                            metaAll.insert(0, token.text().toString());
                            state = State.CLASSNAME;
                        } else if (isSelf(token) || isParent(token)) {
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
        if (scp instanceof MethodScope) {
            MethodScope msi = (MethodScope) scp;
            ClassScope csi = (ClassScope) msi.getInScope();
            if ("self".equals(clsName) || "this".equals(clsName)) {//NOI18N
                clsName = csi.getName();
            } else if ("parent".equals(clsName)) {
                ClassScope clzScope = ModelUtils.getFirst(csi.getSuperClasses());
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

    private static boolean isParent(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_PARENT);
    }

    private static boolean isString(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_STRING);
    }

}
