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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.api.editor.PhpBaseElement;
import org.netbeans.modules.php.api.editor.PhpVariable;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.model.*;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.elements.TypeResolverImpl;
import org.netbeans.modules.php.editor.elements.VariableElementImpl;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind;
import org.netbeans.modules.php.editor.model.nodes.ClassConstantDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.ConstantDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.PhpDocTypeTagInfo;
import org.netbeans.modules.php.editor.nav.NavUtils;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayElement;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.CatchClause;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ClassName;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.DoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionName;
import org.netbeans.modules.php.editor.parser.astnodes.GlobalStatement;
import org.netbeans.modules.php.editor.parser.astnodes.GotoLabel;
import org.netbeans.modules.php.editor.parser.astnodes.GotoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Include;
import org.netbeans.modules.php.editor.parser.astnodes.InstanceOfExpression;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPVarComment;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.ReflectionVariable;
import org.netbeans.modules.php.editor.parser.astnodes.ReturnStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar.Type;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.SwitchStatement;
import org.netbeans.modules.php.editor.parser.astnodes.TryStatement;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.netbeans.modules.php.editor.parser.astnodes.WhileStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultTreePathVisitor;
import org.netbeans.modules.php.project.api.PhpEditorExtender;
import org.netbeans.modules.php.spi.editor.EditorExtender;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Radek Matous
 */
public final class ModelVisitor extends DefaultTreePathVisitor {

    private final FileScopeImpl fileScope;
    private Map<VariableNameFactory, Map<String, VariableNameImpl>> vars;
    private final Map<String, List<PhpDocTypeTagInfo>> varTypeComments;
    private volatile OccurenceBuilder occurencesBuilder;
    private volatile CodeMarkerBuilder markerBuilder;
    private final ModelBuilder modelBuilder;
    private final ParserResult info;
    private boolean  askForEditorExtensions = true;

    public ModelVisitor(ParserResult info) {
        this(info, -1);
    }

    public ModelVisitor(final ParserResult info, final int offset) {
        this.fileScope = new FileScopeImpl(info);
        varTypeComments = new HashMap<String, List<PhpDocTypeTagInfo>>();
        //var2TypeName = new HashMap<String, String>();
        occurencesBuilder = new OccurenceBuilder(offset);
        markerBuilder = new CodeMarkerBuilder(offset);
        this.modelBuilder = new ModelBuilder(this.fileScope);
        this.info = info;
    }

    public ModelVisitor(final ParserResult info, final ModelElement elemnt) {
        this.fileScope = new FileScopeImpl(info);
        varTypeComments = new HashMap<String, List<PhpDocTypeTagInfo>>();
        //var2TypeName = new HashMap<String, String>();
        occurencesBuilder = new OccurenceBuilder(elemnt);
        markerBuilder = new CodeMarkerBuilder(-1);
        this.modelBuilder = new ModelBuilder(this.fileScope);
        this.info = info;
    }

    public ParserResult getCompilationInfo() {
        return this.info;
    }

    @Override
    public void scan(ASTNode node) {
        super.scan(node);
        //just temporarily for testing purposes
//        if (node instanceof Program && fileScope != null) {
//            extendModel();
//        }
    }

    public void extendModel() {
        synchronized(this) {
            if (!askForEditorExtensions) {
                return;
            }
            askForEditorExtensions = false;
        }

        final FileObject fileObject = fileScope.getFileObject();
        EditorExtender editorExtender = PhpEditorExtender.forFileObject(fileObject);
        final List<PhpBaseElement> elements = editorExtender.getElementsForCodeCompletion(fileObject);
        if (elements.size() > 0) {
            for (PhpBaseElement element : elements) {
                if (element instanceof PhpVariable) {
                    PhpVariable phpVariable = (PhpVariable) element;
                    Collection<? extends NamespaceScope> declaredNamespaces = fileScope.getDeclaredNamespaces();
                    for (NamespaceScope namespace : declaredNamespaces) {
                        NamespaceScopeImpl namespaceScope = (NamespaceScopeImpl) namespace;
                        if (namespaceScope != null) {
                            final String varName = phpVariable.getName();
                            VariableNameImpl variable = findVariable(namespace, varName);
                            if (variable != null) {
                                variable.indexedElement = new VariableElementImpl(
                                         varName, variable.getOffset(), fileScope.getFilenameUrl(),
                                        null, TypeResolverImpl.parseTypes(phpVariable.getFullyQualifiedName()));
                            } else {
                                int offset = namespaceScope.getOffset();
                                VariableElementImpl var = new VariableElementImpl(
                                         varName, offset, fileScope.getFilenameUrl(),
                                        null, TypeResolverImpl.parseTypes(phpVariable.getFullyQualifiedName()));
                                namespaceScope.createElement(var);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void visit(PHPDocTag node) {
        super.visit(node);
        PHPDocTag.Type kind = node.getKind();
        ScopeImpl currentScope = modelBuilder.getCurrentScope();
        if (currentScope instanceof TypeScope && kind.equals(PHPDocTag.Type.METHOD)) {
            modelBuilder.buildMagicMethod(node, occurencesBuilder);
        } 
    }

    @Override
    public void visit(ReturnStatement node) {
        final ScopeImpl currentScope = modelBuilder.getCurrentScope();
        markerBuilder.prepare(node,currentScope);        
        String typeName = null;

        if (currentScope instanceof FunctionScope) {
            FunctionScopeImpl functionScope = (FunctionScopeImpl) currentScope;
            Expression expression = node.getExpression();
            if (expression instanceof ClassInstanceCreation) {
                ClassInstanceCreation instanceCreation = (ClassInstanceCreation) expression;
                ASTNodeInfo<ClassInstanceCreation> inf = ASTNodeInfo.create(instanceCreation);
                typeName = inf.getQualifiedName().toString();
            } else if (expression instanceof VariableBase) {
                typeName = VariousUtils.extractTypeFroVariableBase((VariableBase) expression);
                if (typeName != null) {
                    Collection<? extends VariableName> allVariables = VariousUtils.getAllVariables(functionScope, typeName);
                    Map<String,String> var2Type = new HashMap<String, String>();
                    for (VariableName variable : allVariables) {
                        String name = variable.getName();
                        String type = resolveVariableType(name, functionScope, node);
                        if (type == null) {
                            var2Type = Collections.emptyMap();
                            break;
                        }
                        var2Type.put(name, type);
                    }
                    if (!var2Type.isEmpty()) {
                        typeName = VariousUtils.replaceVarNames(typeName, var2Type);
                    }
                }
            }
            
            if (typeName != null) {
                Set<String> types = new HashSet<String>();
                if (functionScope.returnType != null) {
                    String[] split = functionScope.returnType.split("\\|");//NOI18N
                    for (String tp : split) {
                        types.add(tp);
                    }
                }                
                String tp = QualifiedName.create(typeName).toString();
                if (types.isEmpty()) {
                    functionScope.returnType = tp;
                } else if (types.add(tp)){
                    functionScope.returnType += "|" + tp;//NOI18N
                }
            }
        }
        super.visit(node);
    }

    private static Set<String> recursionDetection = new HashSet<String>();//#168868

    private String resolveVariableType(String varName, FunctionScopeImpl varScope, ReturnStatement node) {
        try {
            if (varName != null && recursionDetection.add(varName)) {
                if (varName.equalsIgnoreCase("$this") && varScope instanceof MethodScope) {//NOI18N
                    return varScope.getInScope().getName();
                }
                VariableNameImpl var = (VariableNameImpl) ModelUtils.getFirst(varScope.getDeclaredVariables(), varName);
                if (var != null) {
                    AssignmentImpl assignment = var.findVarAssignment(node.getStartOffset());
                    if (assignment != null) {
                        String typeName = assignment.typeNameFromUnion();
                        if (typeName != null) {
                            if (!typeName.contains("@")) {//NOI18N
                                return typeName;
                            } else {
                                String variableName = getName(typeName, VariousUtils.Kind.VAR, true);
                                if (variableName != null && !variableName.equalsIgnoreCase(varName)) {
                                    return resolveVariableType(variableName, varScope, node);
                                }
                                return typeName;
                            }
                        }
                    }
                }
            }
        } finally {
            if (varName != null) {
                recursionDetection.remove(varName);
            }
        }
        return null;
    }

    @Override
    public void visit(GotoLabel label) {
        super.visit(label);
        occurencesBuilder.prepare(label, modelBuilder.getCurrentScope());
    }
    @Override
    public void visit(GotoStatement statement) {
        super.visit(statement);
        occurencesBuilder.prepare(statement, modelBuilder.getCurrentScope());
    }


    public static String getName(String semiType, VariousUtils.Kind kind, boolean strict) {
        if (semiType != null) {
            String prefix = "@" + kind.toString(); // NOI18N
            if (semiType.startsWith(prefix)) {
                String[] split = semiType.split(prefix, 2);
                if (split.length > 1) {

                    if (split[1].contains("@")) {
                        if (strict) {
                            return null;
                        } else {
                            split = split[1].split("@");
                            if (split.length < 1) {
                                return null;
                            }
                            return split[0];
                        }
                    } else {
                        return split[1];
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void visit(Program program) {
        modelBuilder.setProgram(program);
        fileScope.setBlockRange(program);
        this.vars = new HashMap<VariableNameFactory, Map<String, VariableNameImpl>>();
        try {
            prepareVarComments(program);
            super.visit(program);
            handleVarComments();
        } finally {
            program = null;
            vars = null;
            buildOccurences();
            buildCodeMarks();
        }
    }

    @Override
    public void visit(Include node) {
        modelBuilder.build(node, occurencesBuilder);
        super.visit(node);
    }

    @Override
    public void visit(NamespaceDeclaration node) {
        modelBuilder.build(node, occurencesBuilder);
        try {
            super.visit(node);
        } finally {
            modelBuilder.reset();
        }
    }

    @Override
    public void visit(NamespaceName namespaceName) {
        super.visit(namespaceName);
        occurencesBuilder.prepare(Kind.CONSTANT, namespaceName, fileScope);
    }


    @Override
    public void visit(UseStatementPart statementPart) {
        ASTNodeInfo<UseStatementPart> info = ASTNodeInfo.create(statementPart);
        modelBuilder.getCurrentNameSpace().createUseStatementPart(info);
        super.visit(statementPart);
    }

    @Override
    public void visit(ClassDeclaration node) {
        modelBuilder.build(node, occurencesBuilder);
        checkComments(node);
        try {
            super.visit(node);
        } finally {
            modelBuilder.reset();
        }
    }

    @Override
    public void visit(InterfaceDeclaration node) {
        modelBuilder.build(node, occurencesBuilder);
        try {
            super.visit(node);
        } finally {
            modelBuilder.reset();
        }
    }

    @Override
    public void visit(MethodDeclaration node) {
        modelBuilder.build(node, occurencesBuilder);
        markerBuilder.prepare(node, modelBuilder.getCurrentScope());
        checkComments(node);

        try {
            //super.visit(node);
            scan(node.getFunction().getFormalParameters());
            scan(node.getFunction().getBody());
        } finally {
            modelBuilder.reset();
        }
    }

    @Override
    public void visit(FieldsDeclaration node) {
        modelBuilder.build(node, occurencesBuilder);
        checkComments(node);
        super.visit(node);
    }

    @Override
    public void visit(ClassInstanceCreation node) {
        occurencesBuilder.prepare(node, modelBuilder.getCurrentScope());
        scan(node.ctorParams());
    }

    @Override
    public void visit(InstanceOfExpression node) {
        occurencesBuilder.prepare(node.getClassName(), modelBuilder.getCurrentScope());
        String clsName = CodeUtils.extractClassName(node.getClassName());
        if (clsName != null) {
            Expression expression = node.getExpression();
            if (expression instanceof Variable) {
                Variable var = (Variable) expression;
                Scope currentScope = modelBuilder.getCurrentScope();
                VariableNameImpl varN = findVariable(currentScope, var);
                if (varN != null) {
                    varN.addElement(new VarAssignmentImpl(varN, currentScope, true,
                            getBlockRange(currentScope), ASTNodeInfo.create(var).getRange(), clsName));
                }
            }

        }
        super.visit(node);
    }

    @Override
    public void visit(MethodInvocation node) {
        occurencesBuilder.prepare(node, modelBuilder.getCurrentScope());
        scan(node.getDispatcher());
        scan(node.getMethod().getParameters());
    }

    @Override
    public void visit(Scalar scalar) {
        String stringValue = scalar.getStringValue();
        if (stringValue != null && stringValue.trim().length() > 0 &&
                scalar.getScalarType() == Type.STRING && !NavUtils.isQuoted(stringValue)) {
            occurencesBuilder.prepare(Kind.CONSTANT, scalar, fileScope);
        }
        super.visit(scalar);
    }

    @Override
    public void visit(StaticMethodInvocation node) {
        Scope scope = modelBuilder.getCurrentScope();
        occurencesBuilder.prepare(node, scope);
        occurencesBuilder.prepare(Kind.CLASS, node.getClassName(), scope);
        //scan(node.getClassName());
        scan(node.getMethod().getParameters());

    }

    @Override
    public void visit(ClassName node) {
        Scope scope = modelBuilder.getCurrentScope();
        occurencesBuilder.prepare(node, scope);
    }

    @Override
    public void visit(StaticConstantAccess node) {
        Scope scope = modelBuilder.getCurrentScope();
        occurencesBuilder.prepare(node, scope);
        occurencesBuilder.prepare(Kind.CLASS, node.getClassName(), scope);
        occurencesBuilder.prepare(Kind.IFACE, node.getClassName(), scope);
    }

    @Override
    public void visit(ConstantDeclaration node) {
        Scope scope = modelBuilder.getCurrentScope();
        if (scope instanceof NamespaceScope) {
            List<? extends ConstantDeclarationInfo> constantDeclarationInfos = ConstantDeclarationInfo.create(node);
            for (ConstantDeclarationInfo nodeInfo : constantDeclarationInfos) {
                ConstantElementImpl createElement = modelBuilder.getCurrentNameSpace().createElement(nodeInfo);
                occurencesBuilder.prepare(nodeInfo, createElement);
            }
        } else {
            List<? extends ClassConstantDeclarationInfo> constantDeclarationInfos = ClassConstantDeclarationInfo.create(node);
            for (ClassConstantDeclarationInfo nodeInfo : constantDeclarationInfos) {
                occurencesBuilder.prepare(nodeInfo, ModelElementFactory.create(nodeInfo, modelBuilder));
            }
        }
        super.visit(node);
    }

    @Override
    public void visit(SingleFieldDeclaration node) {
        //super.visit(node);
        scan(node.getValue());
    }

    @Override
    public void visit(ReflectionVariable node) {
        Expression name = node.getName();
        while (name instanceof ReflectionVariable) {
            ReflectionVariable refName = (ReflectionVariable) name;
            name = refName.getName();
        }
        if (name instanceof Variable) {
            scan(name);
        }
    }

    @Override
    public void visit(Variable node) {
        String varName = CodeUtils.extractVariableName(node);
        if (varName == null) {
            return;
        }
        Scope scope = modelBuilder.getCurrentScope();
        occurencesBuilder.prepare(node, scope);

        if (scope instanceof VariableNameFactory) {
            ASTNodeInfo<Variable> varInfo = ASTNodeInfo.create(node);
            if (scope instanceof MethodScope && varInfo.getName().equals("$this")) {//NOI18N
                scope = scope.getInScope();
            }
            if (scope instanceof VariableNameFactory) {
                createVariable((VariableNameFactory) scope, node);
            }
        } else {
            assert scope instanceof TypeScope : scope;
        }
        super.visit(node);
    }

    @Override
    public void visit(GlobalStatement node) {
        super.visit(node);
        List<Variable> variables = node.getVariables();
        for (Variable var : variables) {
            String varName = CodeUtils.extractVariableName(var);
            if (varName == null) {
                continue;
            }
            Scope scope = modelBuilder.getCurrentScope();
            if (scope instanceof VariableNameFactory) {
                VariableNameFactory vc = (VariableNameFactory) scope;
                Collection<? extends VariableName> variablesImpl = ModelUtils.filter(vc.getDeclaredVariables(), varName);
                VariableNameImpl varElem = (VariableNameImpl) ModelUtils.getFirst(variablesImpl);
                if (varElem != null) {
                    varElem.setGloballyVisible(true);
                } else {
                    vc = (VariableNameFactory) modelBuilder.getCurrentNameSpace();
                    variablesImpl = ModelUtils.filter(vc.getDeclaredVariables(), varName);
                    varElem = (VariableNameImpl) ModelUtils.getFirst(variablesImpl);
                    if (varElem != null) {
                        varElem.setGloballyVisible(true);
                    }
                }
            }
        }
    }

    @Override
    public void visit(FieldAccess node) {
        occurencesBuilder.prepare(node, modelBuilder.getCurrentScope());
        //super.visit(node);
        Variable field = node.getField();
        if (field instanceof ArrayAccess) {
            ArrayAccess access = (ArrayAccess) field;
            scan(access.getIndex());
            VariableBase name = access.getName();
            while (name instanceof ArrayAccess) {
                ArrayAccess access1 = (ArrayAccess) name;
                scan(access1.getIndex());
                name = access1.getName();
            }
        }
        scan(node.getDispatcher());
    }

    @Override
    public void visit(FunctionName node) {
        //intentionally ommited - if deleted, golden tests will fail and will show the reason 
        //super.visit(node);
    }

    private Map<String, AssignmentImpl> getAssignmentMap(Scope scope, final VariableBase leftHandSide) {
        Map<String, AssignmentImpl> allAssignments = new HashMap<String, AssignmentImpl>();
        if (scope instanceof VariableScope) {
            VariableScope variableScope = (VariableScope) scope;
            Collection<? extends VariableName> declaredVariables = variableScope.getDeclaredVariables();
            for (VariableName variableName : declaredVariables) {
                if (variableName instanceof VariableNameImpl) {
                    VariableNameImpl vni = (VariableNameImpl) variableName;
                    AssignmentImpl ai = vni.findVarAssignment(leftHandSide.getStartOffset());
                    if (ai != null) {
                        allAssignments.put(vni.getName(), ai);
                    }
                }
            }
        }
        return allAssignments;
    }

    @Override
    public void visit(Assignment node) {
        //Scope scope = currentScope.peek();
        Scope scope = modelBuilder.getCurrentScope();
        final VariableBase leftHandSide = node.getLeftHandSide();
        Expression rightHandSide = node.getRightHandSide();
        super.scan(leftHandSide);
        if (leftHandSide instanceof Variable) {
            VariableNameImpl varN = findVariable(scope, leftHandSide);
            //TODO: global variables or vars from other files
            //assert varN != null : CodeUtils.extractVariableName((Variable)leftHandSide);
            if (varN != null) {
                Map<String, AssignmentImpl> allAssignments = getAssignmentMap(scope, leftHandSide);
                Variable var = ((Variable) leftHandSide);
                if (rightHandSide instanceof ArrayCreation) {
                    ArrayCreation arrayCreation = (ArrayCreation) rightHandSide;
                    List<ArrayElement> elements = arrayCreation.getElements();
                    if (!elements.isEmpty()) {
                        for (ArrayElement arrayElement : elements) {
                            Expression value = arrayElement.getValue();
                            String typeName = VariousUtils.extractVariableTypeFromExpression(value, allAssignments);
                            ASTNode conditionalNode = findConditionalStatement(getPath());
                            VarAssignmentImpl vAssignment = new VarAssignmentImpl(varN, scope, conditionalNode != null,
                                    getBlockRange(conditionalNode, scope), new OffsetRange(var.getStartOffset(), var.getEndOffset()), typeName);
                            vAssignment.setAsArrayAccess(true);
                        }
                    } else {
                        String typeName = VariousUtils.extractVariableTypeFromExpression(rightHandSide, allAssignments);
                        ASTNode conditionalNode = findConditionalStatement(getPath());
                        new VarAssignmentImpl(varN, scope, conditionalNode != null,
                                    getBlockRange(conditionalNode, scope), new OffsetRange(var.getStartOffset(), var.getEndOffset()), typeName);
                    }
                } else {
                    ASTNode conditionalNode = findConditionalStatement(getPath());
                    varN.createAssignment(scope, conditionalNode != null, getBlockRange(conditionalNode, scope), new OffsetRange(var.getStartOffset(), var.getEndOffset()), node,
                            allAssignments);
                }
                occurencesBuilder.prepare((Variable) leftHandSide, scope);
            }
        } else if (leftHandSide instanceof FieldAccess) {
            FieldAccess fieldAccess = (FieldAccess) leftHandSide;
            VariableNameImpl varN = findVariable(modelBuilder.getCurrentScope(), fieldAccess.getDispatcher());
            if (varN != null) {
                varN.createLazyFieldAssignment(fieldAccess, node, scope);
            }

        } else if (leftHandSide instanceof StaticFieldAccess) {
            StaticFieldAccess sfa = (StaticFieldAccess) leftHandSide;
            //TODO:
            //CodeUtils.extractQualifiedName(sfa);
        }

        super.scan(rightHandSide);
    }

    @Override
    public void visit(ForEachStatement node) {
        Scope scope = modelBuilder.getCurrentScope();
        super.visit(node);
        Expression expression = node.getExpression();
        Expression value = node.getValue();
        if ((expression instanceof Variable) && (value instanceof Variable)) {
            VariableNameImpl varArray = findVariable(scope, (Variable)expression);
            VariableNameImpl varValue = findVariable(scope, (Variable)value);
            if (varArray != null && varValue != null) {
                varValue.setTypeResolutionKind(VariableNameImpl.TypeResolutionKind.MERGE_ASSIGNMENTS);
                Collection<? extends String> typeNames = varArray.getArrayAccessTypeNames(node.getStartOffset());
                for (String tpName : typeNames) {                    
                    new VarAssignmentImpl(varValue, scope, true, getBlockRange(scope),
                            new OffsetRange(value.getStartOffset(), value.getEndOffset()), tpName);
                }
            }
        }
    }


    @Override
    public void visit(FormalParameter node) {
        Expression parameterName = node.getParameterName();
        Expression parameterType = node.getParameterType();
        Scope scp = modelBuilder.getCurrentScope();
        FunctionScopeImpl fncScope = (FunctionScopeImpl) scp;
        while (parameterName instanceof Reference) {
            Reference ref = (Reference) parameterName;
            Expression expression = ref.getExpression();
            if (expression instanceof Variable || expression instanceof Reference) {
                parameterName = expression;
            }
        }
        List<? extends ParameterElement> parameters = fncScope.getParameters();
        if (parameterName instanceof Variable) {
            for (ParameterElement parameter : parameters) {
                Set<TypeResolver> types = parameter.getTypes();
                String typeName = null;
                for (TypeResolver typeResolver : types) {
                    if (typeResolver.isResolved()) {
                        QualifiedName typeQualifiedName = typeResolver.getTypeName(false);
                        if (typeQualifiedName != null) {
                            typeName = typeQualifiedName.toString();
                        }
                    }
                }
                VariableNameImpl var = createParameter(fncScope, parameter);
                if (!types.isEmpty() && var != null) {
                    var.addElement(new VarAssignmentImpl(var, fncScope, false, fncScope.getBlockRange(),
                            parameter.getOffsetRange(), typeName));
                }
            }
        }

        if (parameterName instanceof Variable) {
            //Identifier paramId = parameterType != null ? CodeUtils.extractUnqualifiedIdentifier(parameterType) : null;
            occurencesBuilder.prepare(Kind.CLASS, parameterType, fncScope);
            occurencesBuilder.prepare(Kind.IFACE, parameterType, fncScope);
            occurencesBuilder.prepare((Variable) parameterName, fncScope);
        }
        super.visit(node);
    }

    @Override
    public void visit(CatchClause node) {
        Variable variable = node.getVariable();
        Scope scope = modelBuilder.getCurrentScope();
        if (scope instanceof VariableNameFactory) {
            VariableNameImpl varNameImpl = createVariable((VariableNameFactory) scope, variable);
            if (varNameImpl != null) {
                varNameImpl.addElement(new VarAssignmentImpl(varNameImpl, scope, true, new OffsetRange(node.getStartOffset(), node.getEndOffset()),
                        VariableNameImpl.toOffsetRange(variable), CodeUtils.extractUnqualifiedTypeName(node)));
            }
        }
        occurencesBuilder.prepare(Kind.CLASS, node.getClassName(), scope);
        occurencesBuilder.prepare(variable, scope);


        scan(node.getBody());
    }

    @Override
    public void visit(LambdaFunctionDeclaration node) {
        ScopeImpl scope = modelBuilder.getCurrentScope();
        FunctionScopeImpl fncScope = FunctionScopeImpl.createElement(scope, node);
        modelBuilder.setCurrentScope(scope = fncScope);
        List<Expression> lexicalVariables = node.getLexicalVariables();
        for (Expression expression : lexicalVariables) {
            if (expression instanceof Variable) {
                Variable variable = (Variable) expression;
                VariableNameImpl varNameImpl= createVariable((VariableNameFactory) scope, variable);
                varNameImpl.setGloballyVisible(true);
            }
        }
        scan(lexicalVariables);
        scope.setBlockRange(node.getBody());
        scan(node.getFormalParameters());
        scan(node.getBody());
        modelBuilder.reset();
    }

    @Override
    public void visit(FunctionDeclaration node) {
        //Scope scope = currentScope.peek();
        ScopeImpl scope = modelBuilder.getCurrentScope();
        assert scope != null && ((scope instanceof FunctionScope) ||
                (scope instanceof MethodScope) || (scope instanceof NamespaceScopeImpl));
        if (scope instanceof NamespaceScopeImpl) {
            NamespaceScopeImpl ps = (NamespaceScopeImpl) scope;
            FunctionScopeImpl fncScope = ps.createElement(modelBuilder.getProgram(), node);
            //currentScope.push(scope = fncScope);
            modelBuilder.setCurrentScope(scope = fncScope);
            occurencesBuilder.prepare(node, fncScope);
            markerBuilder.prepare(node, modelBuilder.getCurrentScope());
            checkComments(node);
        } else if (!(scope instanceof NamespaceScope)) {
            Scope tmpScope = scope;
            while (!(tmpScope instanceof NamespaceScope)) {
                tmpScope = tmpScope.getInScope();
            }
            if (tmpScope instanceof NamespaceScopeImpl) {
                NamespaceScopeImpl ps = (NamespaceScopeImpl) tmpScope;
                FunctionScopeImpl fncScope = ps.createElement(modelBuilder.getProgram(), node);
                //currentScope.push(scope = fncScope);
                modelBuilder.setCurrentScope(scope = fncScope);
                occurencesBuilder.prepare(node, fncScope);
                markerBuilder.prepare(node, modelBuilder.getCurrentScope());
                checkComments(node);
            }
        }
        scope.setBlockRange(node.getBody());
        scan(node.getFormalParameters());
        scan(node.getBody());
        modelBuilder.reset();
    }

    @Override
    public void visit(FunctionInvocation node) {
        //Scope scope = currentScope.peek();
        Scope scope = modelBuilder.getCurrentScope();
        occurencesBuilder.prepare(node, scope);
        ASTNodeInfo<FunctionInvocation> nodeInfo = ASTNodeInfo.create(node);
        String name = nodeInfo.getName();
        if ("define".equals(name) && node.getParameters().size() == 2) {//NOI18N
            Expression d = node.getParameters().get(0);
            if (d instanceof Scalar && ((Scalar) d).getScalarType() == Type.STRING) {
                Scalar scalar = (Scalar) d;
                String value = scalar.getStringValue();
                if (NavUtils.isQuoted(value)) {
                    ASTNodeInfo<Scalar> scalarInfo = ASTNodeInfo.create(Kind.CONSTANT, scalar);
                    ConstantElementImpl constantImpl = modelBuilder.getCurrentNameSpace().createConstantElement(scalarInfo);
                    occurencesBuilder.prepare(scalarInfo, constantImpl);
                }
            }
        } else if ("constant".equals(name) && node.getParameters().size() == 1) {
            Expression d = node.getParameters().get(0);
            if (d instanceof Scalar) {
                Scalar scalar = (Scalar) d;
                if (scalar.getScalarType() == Type.STRING && NavUtils.isQuoted(scalar.getStringValue())) {
                    occurencesBuilder.prepare(Kind.CONSTANT, scalar, fileScope);
                }

            }

        }

        super.visit(node);
    }

    @Override
    public void visit(StaticFieldAccess node) {
        //Scope scope = currentScope.peek();
        Scope scope = modelBuilder.getCurrentScope();
        occurencesBuilder.prepare(node, scope);
        occurencesBuilder.prepare(Kind.CLASS, node.getClassName(), scope);
        Variable field = node.getField();
        if (field instanceof ArrayAccess) {
            ArrayAccess access = (ArrayAccess) field;
            scan(access.getIndex());
            VariableBase name = access.getName();
            while (name instanceof ArrayAccess) {
                ArrayAccess access1 = (ArrayAccess) name;
                scan(access1.getIndex());
                name = access1.getName();
            }
        }

        //super.visit(node);
    }

    @Override
    public void visit(PHPDocTypeTag node) {
        occurencesBuilder.prepare(node, modelBuilder.getCurrentScope());
        super.visit(node);
    }

    @Override
    public void visit(PHPDocVarTypeTag node) {
        Scope currentScope = modelBuilder.getCurrentScope();
        StringBuilder sb = new StringBuilder();
        List<? extends PhpDocTypeTagInfo> tagInfos = PhpDocTypeTagInfo.create(node, currentScope);
        for (Iterator<? extends PhpDocTypeTagInfo> it = tagInfos.iterator(); it.hasNext();) {
            PhpDocTypeTagInfo phpDocTypeTagInfo = it.next();
            if (phpDocTypeTagInfo.getKind().equals(Kind.FIELD)) {
                String typeName = phpDocTypeTagInfo.getTypeName();
                if (typeName != null) {
                    if (sb.length() > 0) {
                        sb.append("|");//NOI18N
                    }
                    sb.append(typeName);
                }
                if (currentScope instanceof ClassScope && !it.hasNext()) {
                    new FieldElementImpl(currentScope, sb.length() > 0 ? sb.toString() : null, phpDocTypeTagInfo);
                }
            } else if (node.getKind().equals(PHPDocTag.Type.GLOBAL) && phpDocTypeTagInfo.getKind().equals(Kind.VARIABLE)) {
                final String typeName = phpDocTypeTagInfo.getTypeName();
                final String varName = phpDocTypeTagInfo.getName();
                VariableScope variableScope = getVariableScope(node.getStartOffset());
                if (variableScope != null) {
                    VariableNameImpl varN = findVariable(variableScope, varName);
                    if (varN == null && variableScope instanceof VariableNameFactory) {
                        VariableNameFactory factory = (VariableNameFactory) variableScope;
                        final OffsetRange nameRange = new OffsetRange(node.getStartOffset(), node.getEndOffset());
                        varN = new VariableNameImpl(factory, varName, variableScope.getFile(), nameRange, true);
                    }
                    if (varN != null) {
                        varN.addElement(new VarAssignmentImpl(varN, variableScope,
                                false, variableScope.getBlockRange(), varN.getNameRange(), typeName));
                    }
                }
            } 
        }

        occurencesBuilder.prepare(node, currentScope);
        super.visit(node);
    }

    public FileScope getFileScope() {
        return fileScope;
    }

    @CheckForNull
    public CodeMarker getCodeMarker(int offset) {
        return findStrictCodeMarker((FileScopeImpl) getFileScope(), offset, null);
    }

    private void checkComments(ASTNode node) {
        Comment comment = node instanceof Comment ? (Comment) node : Utils.getCommentForNode(modelBuilder.getProgram(), node);
        if (comment instanceof PHPDocBlock) {
            PHPDocBlock phpDoc = (PHPDocBlock) comment;
            for (PHPDocTag tag : phpDoc.getTags()) {
                scan(tag);
            }
        } else if (comment instanceof PHPVarComment) {
            PHPDocVarTypeTag typeTag = ((PHPVarComment) comment).getVariable();
            List<? extends PhpDocTypeTagInfo> tagInfos = PhpDocTypeTagInfo.create(typeTag, fileScope);
            for (PhpDocTypeTagInfo tagInfo : tagInfos) {
                if (tagInfo.getKind().equals(ASTNodeInfo.Kind.VARIABLE)) {
                    String name = tagInfo.getName();
                    List<PhpDocTypeTagInfo> infos = varTypeComments.get(name);
                    if (infos == null) {
                        infos = new ArrayList<PhpDocTypeTagInfo>();
                        varTypeComments.put(name, infos);
                    }
                    infos.add(tagInfo);
                }
            }
        }
    }

    private VariableNameImpl findVariable(Scope scope, String varName) {
        VariableNameImpl retval = null;
        if (varName != null) {
            Map<String, VariableNameImpl> varnames = vars.get(scope);
            while (scope != null) {
                if (varnames != null) {
                    retval = varnames.get(varName);
                    if (retval != null) {
                        break;
                    }
                }
                scope = scope.getInScope();
                varnames = vars.get(scope);
            }
        }
        return retval;
    }

    private VariableNameImpl findVariable(Scope scope, final VariableBase leftHandSide) {
        String varName = null;
        if (leftHandSide instanceof Variable) {
            varName = VariableNameImpl.toName((Variable) leftHandSide);
        }
        return varName != null ? findVariable(scope, varName) : null;
    }

    private VariableNameImpl createParameter(FunctionScopeImpl fncScope, ParameterElement parameter) {
        VariableNameFactory varContainer = (VariableNameFactory) fncScope;
        Map<String, VariableNameImpl> map = vars.get(varContainer);
        if (map == null) {
            map = new HashMap<String, VariableNameImpl>();
            vars.put(varContainer, map);
        }
        String name = parameter.getName();
        VariableNameImpl varInstance = map.get(name);
        if (varInstance == null) {
            if (ModelUtils.filter(varContainer.getDeclaredVariables(), name).isEmpty()) {
                varInstance = new VariableNameImpl(fncScope, name, fncScope.getFile(), parameter.getOffsetRange(), false);
                fncScope.addElement(varInstance);
                map.put(name, varInstance);
            }
        }
        return varInstance;
    }

    private VariableNameImpl createVariable(VariableNameFactory varContainer, Variable node) {
        Map<String, VariableNameImpl> map = vars.get(varContainer);
        if (map == null) {
            map = new HashMap<String, VariableNameImpl>();
            vars.put(varContainer, map);
        }
        String name = VariableNameImpl.toName(node);
        VariableNameImpl retval = map.get(name);
        if (retval == null) {
            if (ModelUtils.filter(varContainer.getDeclaredVariables(), name).isEmpty()) {
                retval = varContainer.createElement(node);
                map.put(name, retval);
            }
        }
        return retval;
    }

    @CheckForNull
    private ASTNode findConditionalStatement(List<ASTNode> path) {
        for (ASTNode aSTNode : path) {
            if (aSTNode instanceof IfStatement) {
                return aSTNode;
            } else if (aSTNode instanceof WhileStatement) {
                return aSTNode;
            } else if (aSTNode instanceof DoStatement) {
                return aSTNode;
            } else if (aSTNode instanceof ForEachStatement) {
                return aSTNode;
            } else if (aSTNode instanceof ForStatement) {
                return aSTNode;
            } else if (aSTNode instanceof CatchClause) {
                return aSTNode;
            } else if (aSTNode instanceof SwitchStatement) {
                return aSTNode;
            } else if (aSTNode instanceof TryStatement) {
                return aSTNode;
            } else if (aSTNode instanceof InstanceOfExpression) {
                return aSTNode;
            }
        }
        return null;
    }

    private CodeMarker findStrictCodeMarker(FileScopeImpl scope, int offset, CodeMarker atOffset) {
        buildCodeMarks();
        List<? extends CodeMarker> markers = scope.getMarkers();
        for (CodeMarker codeMarker : markers) {
            assert codeMarker != null;
            if (codeMarker.getOffsetRange().containsInclusive(offset)) {
                atOffset = codeMarker;
            }
        }
        return atOffset;
    }

    @CheckForNull
    public Occurence getOccurence(int offset) {
        return findStrictOccurence((FileScopeImpl) getFileScope(), offset);
    }

    @CheckForNull
    public Occurence getOccurence(ModelElement element) {
        return findStrictOccurence((FileScopeImpl) getFileScope(), element);
    }

    public VariableScope getNearestVariableScope(int offset) {
        return findNearestVarScope((FileScopeImpl) getFileScope(), offset, null);
    }

    public VariableScope getVariableScope(int offset) {
        VariableScope retval = null;
        List<ModelElement> elements = new ArrayList<ModelElement>();
        elements.add(getFileScope());
        elements.addAll(ModelUtils.getElements(getFileScope(), true));
        for (ModelElement modelElement : elements) {
            if (modelElement instanceof VariableScope) {
                VariableScope varScope = (VariableScope) modelElement;
                final OffsetRange blockRange = varScope.getBlockRange();
                if (blockRange != null && blockRange.containsInclusive(offset)) {
                    if (retval == null ||
                            retval.getBlockRange().overlaps(varScope.getBlockRange())) {
                        retval = varScope;
                    }
                }
            } else if (modelElement instanceof ClassScope) {
                ClassScope clsScope = (ClassScope) modelElement;
                Collection<? extends MethodScope> allMethods = clsScope.getDeclaredMethods();
                for (MethodScope methodScope : allMethods) {
                    OffsetRange blockRange = methodScope.getBlockRange();
                    if (blockRange != null && blockRange.containsInclusive(offset)) {
                        if (retval == null ||
                                retval.getBlockRange().overlaps(methodScope.getBlockRange())) {
                            retval = methodScope;
                        }
                    }
                }
            }
        }
        return retval;
    }

    static List<Occurence> getAllOccurences(FileScope modelScope, Occurence occurence) {
        ModelElementImpl declaration = (ModelElementImpl) occurence.getDeclaration();
        if (declaration instanceof MethodScope) {
            MethodScope methodScope = (MethodScope) declaration;
            if (methodScope.isConstructor()) {
                declaration = (ModelElementImpl) methodScope.getInScope();
            }
        }
        if (declaration instanceof VarAssignmentImpl) {
            VarAssignmentImpl impl = (VarAssignmentImpl) declaration;
            declaration = impl.getContainer();
        }
        return ((FileScopeImpl) modelScope).getAllOccurences(declaration);
    }

    public static IndexScope getIndexScope(ParserResult info) {
        return new IndexScopeImpl(info);
    }

    public static IndexScope getIndexScope(ElementQuery.Index idx) {
        return new IndexScopeImpl(idx);
    }

    private void buildCodeMarks() {
        if (markerBuilder != null) {
            markerBuilder.build(fileScope);
            markerBuilder = null;
        }
    }

    private void buildOccurences() {
        if (occurencesBuilder != null) {
            occurencesBuilder.build(fileScope);
            occurencesBuilder = null;
        }
    }

    private Occurence findStrictOccurence(FileScopeImpl scope, int offset) {
        Occurence retval = null;
        buildOccurences();
        //FileObject fileObject = scope.getFileObject();
        List<Occurence> occurences = scope.getOccurences();
        for (Occurence occ : occurences) {
            assert occ != null;
            if (occ.getOccurenceRange().containsInclusive(offset)) {
                retval = occ;
            }
        }
        return retval;
    }

    private Occurence findStrictOccurence(FileScopeImpl scope, ModelElement element) {
        Occurence retval = null;
        buildOccurences();
        //FileObject fileObject = scope.getFileObject();
        List<Occurence> occurences = scope.getOccurences();
        for (Occurence occ : occurences) {
            assert occ != null;
            if (occ.getDeclaration().equals(element)) {
                retval = occ;
            }
        }
        return retval;
    }

    private VariableScope findNearestVarScope(Scope scope, int offset, VariableScope atOffset) {
        buildOccurences();
        Collection<? extends ModelElement> elements = scope.getElements();
        for (ModelElement varScope : elements) {
            if (varScope instanceof ClassScope || varScope instanceof NamespaceScope) {
                atOffset = findNearestVarScope((Scope) varScope, offset, atOffset);
            }
            if (varScope instanceof VariableScope) {
                if (varScope.getNameRange().getStart() <= offset) {
                    if (atOffset == null || atOffset.getOffset() < varScope.getOffset()) {
                        if (varScope instanceof VariableScope) {
                            FileObject fileObject = varScope.getFileObject();
                            if (fileObject == scope.getFileObject()) {
                                VariableScope variableScope = (VariableScope) varScope;
                                OffsetRange blockRange = variableScope.getBlockRange();
                                if (blockRange == null || blockRange.containsInclusive(offset)) {
                                    atOffset = variableScope;
                                }
                            }
                        }
                    }
                }
            }

        }
        if (atOffset == null) {
            while (scope != null && !(scope instanceof VariableScope)) {
                scope = scope.getInScope();
            }
            if (scope != null) {
                OffsetRange blockRange = scope.getBlockRange();
                if (blockRange == null || blockRange.containsInclusive(offset)) {
                    atOffset = (VariableScope) scope;
                }
            }
        }
        return atOffset;
    }
    private OffsetRange getBlockRange(Scope currentScope) {
        ASTNode conditionalNode = findConditionalStatement(getPath());
        return getBlockRange(conditionalNode, currentScope);
    }
    private OffsetRange getBlockRange(ASTNode conditionalNode, Scope currentScope) {
        OffsetRange scopeRange = (conditionalNode != null) ? new OffsetRange(conditionalNode.getStartOffset(), conditionalNode.getEndOffset()) : currentScope.getBlockRange();
        return scopeRange;
    }
    private void handleVarComments() {
        Set<String> varCommentNames = varTypeComments.keySet();
        for (String name : varCommentNames) {
            List<PhpDocTypeTagInfo> varComments = varTypeComments.get(name); //varComments.size() varTypeComments.size()
            if (varComments != null) {
                for (PhpDocTypeTagInfo phpDocTypeTagInfo : varComments) {
                    VariableScope varScope = getVariableScope(phpDocTypeTagInfo.getRange().getStart());
                    VariableNameImpl varInstance = null;
                    if (varScope instanceof Scope) {
                        Scope scp = (Scope) varScope;
                        varInstance = (VariableNameImpl) ModelUtils.getFirst(ModelUtils.filter(varScope.getDeclaredVariables(), name));
                        if (varInstance == null) {
                            varInstance = new VariableNameImpl(scp, name, scp.getFile(), phpDocTypeTagInfo.getRange(), scp instanceof NamespaceScopeImpl);
                        }
                    }
                    if (varInstance != null) {
                        ASTNode conditionalNode = findConditionalStatement(getPath());
                        VarAssignmentImpl vAssignment = new VarAssignmentImpl(varInstance, 
                                (Scope) varScope, conditionalNode != null, getBlockRange(varScope), phpDocTypeTagInfo.getRange(), phpDocTypeTagInfo.getTypeName());
                        varInstance.addElement(vAssignment);
                    }
                    //scan(phpDocTypeTagInfo.getTypeTag());
                    occurencesBuilder.prepare(phpDocTypeTagInfo.getTypeTag(), varScope);

                }
            }
        }
    }

    private void prepareVarComments(Program program) {
        List<Comment> comments = program.getComments();
        for (Comment comment : comments) {
            Comment.Type type = comment.getCommentType();
            if (type.equals(Comment.Type.TYPE_VARTYPE)) {
                checkComments(comment);
            }
        }
    }

    /*private String getVarTypeName(String name, Scope Scope) {
    String typeName = var2TypeName.get(name);
    if (typeName == null) {
    PhpDocTypeTagInfo typeTag = var2DefaultType.get(name);
    if (typeTag != null) {
    OffsetRange scopeRange = Scope.getBlockRange();
    if (scopeRange != null && scopeRange.overlaps(typeTag.getRange())) {
    typeName = typeTag.getTypeName();
    var2TypeName.put(name, typeName);
    scan(typeTag.getTypeTag());
    }
    }
    }
    return typeName;
    }*/
}
