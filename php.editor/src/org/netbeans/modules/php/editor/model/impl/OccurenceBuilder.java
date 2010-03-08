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
import java.util.Map.Entry;
import java.util.Set;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.NamespaceIndexFilter;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.FieldElement;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.ConstantElement;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.IncludeElement;
import org.netbeans.modules.php.editor.model.IndexScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.Occurence;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.model.nodes.ElementInfo;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind;
import org.netbeans.modules.php.editor.model.nodes.ClassConstantDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.ClassDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.ConstantDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.FunctionDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.IncludeInfo;
import org.netbeans.modules.php.editor.model.nodes.InterfaceDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.MagicMethodDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.MethodDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.PhpDocTypeTagInfo;
import org.netbeans.modules.php.editor.model.nodes.SingleFieldDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ClassName;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.GotoLabel;
import org.netbeans.modules.php.editor.parser.astnodes.GotoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.Include;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.openide.util.Union2;

/**
 * @author Radek Matous
 */
class OccurenceBuilder {
    private Map<ASTNodeInfo<Scalar>, ConstantElement> constDeclarations;
    private Map<ConstantDeclarationInfo, ConstantElement> constDeclarations53;
    private Map<ASTNodeInfo<Scalar>, Scope> constInvocations;
    private Map<ASTNodeInfo<Expression>, Scope> nsConstInvocations;
    private Map<ASTNodeInfo<FunctionDeclaration>, FunctionScope> fncDeclarations;
    private Map<ASTNodeInfo<MethodDeclaration>, MethodScope> methodDeclarations;
    private Map<MagicMethodDeclarationInfo, MethodScope> magicMethodDeclarations;
    private Map<ASTNodeInfo<MethodInvocation>, Scope> methodInvocations;
    private Map<ASTNodeInfo<Identifier>, ClassConstantElement> classConstantDeclarations;
    private Map<ASTNodeInfo<FunctionInvocation>, Scope> fncInvocations;
    private Map<ASTNodeInfo<StaticMethodInvocation>, Scope> staticMethodInvocations;
    private Map<ASTNodeInfo<StaticFieldAccess>, Scope> staticFieldInvocations;
    private Map<ASTNodeInfo<StaticConstantAccess>, Scope> staticConstantInvocations;
    private Map<ClassDeclarationInfo, ClassScope> clasDeclarations;
    private Map<InterfaceDeclarationInfo, InterfaceScope> ifaceDeclarations;
    private HashMap<PhpDocTypeTagInfo, Scope> docTags;
    private Map<ASTNodeInfo<ClassName>, Scope> clasNames;
    private Map<ASTNodeInfo<ClassInstanceCreation>, Scope> clasInstanceCreations;
    private Map<ASTNodeInfo<Expression>, Scope> clasIDs;
    private Map<ASTNodeInfo<Expression>, Scope> ifaceIDs;
    private Map<ASTNodeInfo<Variable>, Scope> variables;
    private HashMap<IncludeInfo, IncludeElement> includes;
    private HashMap<SingleFieldDeclarationInfo, FieldElementImpl> fldDeclarations;
    private HashMap<ASTNodeInfo<FieldAccess>, Scope> fieldInvocations;
    private int offset;
    private ElementInfo currentContextInfo;
    private ModelElement element;
    private Collection<ModelElement> declarations = new HashSet<ModelElement>();
    private Map<ASTNodeInfo<GotoLabel>, Scope> gotoLabel;
    private Map<ASTNodeInfo<GotoStatement>, Scope> gotoStatement;

    OccurenceBuilder(ModelElement element) {
        this(-1);
        this.element = element;
    }
    OccurenceBuilder(int offset) {
        this.offset = offset;
        this.constInvocations = new HashMap<ASTNodeInfo<Scalar>, Scope>();
        this.nsConstInvocations = new HashMap<ASTNodeInfo<Expression>, Scope>();
        this.constDeclarations = new HashMap<ASTNodeInfo<Scalar>, ConstantElement>();
        this.constDeclarations53 = new HashMap<ConstantDeclarationInfo, ConstantElement>();
        this.includes = new HashMap<IncludeInfo, IncludeElement>();
        this.fncInvocations = new HashMap<ASTNodeInfo<FunctionInvocation>, Scope>();
        this.fncDeclarations = new HashMap<ASTNodeInfo<FunctionDeclaration>, FunctionScope>();
        this.staticMethodInvocations = new HashMap<ASTNodeInfo<StaticMethodInvocation>, Scope>();
        this.methodDeclarations = new HashMap<ASTNodeInfo<MethodDeclaration>, MethodScope>();
        this.magicMethodDeclarations = new HashMap<MagicMethodDeclarationInfo, MethodScope>();
        this.methodInvocations = new HashMap<ASTNodeInfo<MethodInvocation>, Scope>();
        this.fieldInvocations = new HashMap<ASTNodeInfo<FieldAccess>, Scope>();
        this.staticFieldInvocations = new HashMap<ASTNodeInfo<StaticFieldAccess>, Scope>();
        this.staticConstantInvocations = new HashMap<ASTNodeInfo<StaticConstantAccess>, Scope>();
        this.clasDeclarations = new HashMap<ClassDeclarationInfo, ClassScope>();
        this.ifaceDeclarations = new HashMap<InterfaceDeclarationInfo, InterfaceScope>();
        this.clasNames = new HashMap<ASTNodeInfo<ClassName>, Scope>();
        this.clasInstanceCreations = new HashMap<ASTNodeInfo<ClassInstanceCreation>, Scope>();
        this.clasIDs = new HashMap<ASTNodeInfo<Expression>, Scope>();
        this.ifaceIDs = new HashMap<ASTNodeInfo<Expression>, Scope>();
        this.classConstantDeclarations = new HashMap<ASTNodeInfo<Identifier>, ClassConstantElement>();
        this.variables = new HashMap<ASTNodeInfo<Variable>, Scope>();
        this.fldDeclarations = new HashMap<SingleFieldDeclarationInfo, FieldElementImpl>();
        this.docTags = new HashMap<PhpDocTypeTagInfo, Scope>();
        this.gotoStatement =  new HashMap<ASTNodeInfo<GotoStatement>, Scope>();
        this.gotoLabel = new HashMap<ASTNodeInfo<GotoLabel>, Scope>();
    }

    void prepare(GotoStatement statement, ScopeImpl scope) {
        if (canBePrepared(statement, scope)) {
            ASTNodeInfo<GotoStatement> node = ASTNodeInfo.create(statement);
            gotoStatement.put(node, scope);
            setOccurenceAsCurrent(new ElementInfo(node, scope));
        }
    }

    void prepare(GotoLabel label, ScopeImpl scope) {
        if (canBePrepared(label, scope)) {
            ASTNodeInfo<GotoLabel> node = ASTNodeInfo.create(label);
            gotoLabel.put(node, scope);
            setOccurenceAsCurrent(new ElementInfo(node, scope));
        }
    }

    void prepare(FieldAccess fieldAccess, Scope scope) {
        if (canBePrepared(fieldAccess, scope)) {
            ASTNodeInfo<FieldAccess> node = ASTNodeInfo.create(fieldAccess);
            fieldInvocations.put(node, scope);
            setOccurenceAsCurrent(new ElementInfo(node, scope));
        }
    }

    void prepare(Include incl, IncludeElementImpl inclImpl) {
        if (canBePrepared(incl, inclImpl)) {
            IncludeInfo node = IncludeInfo.create(incl);
            includes.put(node, inclImpl);
            setOccurenceAsCurrent(new ElementInfo(node, ModelUtils.getNamespaceScope(inclImpl)));
        }
    }

    void prepare(MethodInvocation methodInvocation, Scope scope) {
        if (canBePrepared(methodInvocation, scope)) {
            ASTNodeInfo<MethodInvocation> node = ASTNodeInfo.create(methodInvocation);
            methodInvocations.put(node, scope);
            setOccurenceAsCurrent(new ElementInfo(node, scope));
        }
    }

    void prepare(SingleFieldDeclarationInfo info, FieldElementImpl fei) {
        SingleFieldDeclaration node = info.getOriginalNode();
        if (canBePrepared(node, fei)) {
            fldDeclarations.put(info, fei);
            setOccurenceAsCurrent(new ElementInfo(info, fei));
        }
    }

    void prepare(Variable variable, Scope scope) {
        if (canBePrepared(variable, scope)) {
            ASTNodeInfo<Variable> node = ASTNodeInfo.create(variable);
            variables.put(node, scope);
            setOccurenceAsCurrent(new ElementInfo(node, scope));
        }
    }

    void prepare(FunctionInvocation functionInvocation, Scope scope) {
        if (canBePrepared(functionInvocation, scope)) {
            ASTNodeInfo<FunctionInvocation> node = ASTNodeInfo.create(functionInvocation);
            this.fncInvocations.put(node, scope);
            setOccurenceAsCurrent(new ElementInfo(node, scope));
        }
    }

    void prepare(StaticMethodInvocation staticMethodInvocation, Scope scope) {
        if (canBePrepared(staticMethodInvocation, scope)) {
            ASTNodeInfo<StaticMethodInvocation> node = ASTNodeInfo.create(staticMethodInvocation);
            this.staticMethodInvocations.put(node, scope);
            setOccurenceAsCurrent(new ElementInfo(node, scope));
        }
    }

    void prepare(StaticFieldAccess staticFieldAccess, Scope scope) {
        if (canBePrepared(staticFieldAccess, scope)) {
            ASTNodeInfo<StaticFieldAccess> node = ASTNodeInfo.create(staticFieldAccess);
            staticFieldInvocations.put(node, scope);
            setOccurenceAsCurrent(new ElementInfo(node, scope));
        }
    }

    void prepare(StaticConstantAccess staticConstantAccess, Scope scope) {
        if (canBePrepared(staticConstantAccess, scope)) {
            ASTNodeInfo<StaticConstantAccess> node = ASTNodeInfo.create(staticConstantAccess);
            staticConstantInvocations.put(node, scope);
            setOccurenceAsCurrent(new ElementInfo(node, scope));
        }
    }

    void prepare(ClassName clsName, Scope scope) {
        if (canBePrepared(clsName, scope)) {
            ASTNodeInfo<ClassName> node = ASTNodeInfo.create(clsName);
            clasNames.put(node, scope);
            setOccurenceAsCurrent(new ElementInfo(node, scope));
        }
    }

    void prepare(Kind kind, Expression node, Scope scope) {
        ASTNodeInfo<Expression> nodeInfo = null;
        if (node instanceof Identifier) {
            nodeInfo = ASTNodeInfo.create(kind, (Identifier)node);
        } else if (node instanceof NamespaceName) {
            nodeInfo = ASTNodeInfo.create(kind, (NamespaceName)node);
        }
        if (nodeInfo != null && canBePrepared(node, scope)) {
            switch (nodeInfo.getKind()) {
                case CLASS:
                    clasIDs.put(nodeInfo, scope);
                    break;
                case IFACE:
                    ifaceIDs.put(nodeInfo, scope);
                    break;
                case CONSTANT:
                    if (node instanceof NamespaceName) {
                        nsConstInvocations.put(nodeInfo, scope);
                        if (currentContextInfo != null) {
                            return;
                        }
                    }
                    break;
                default:
                    throw new IllegalStateException();
            }
            setOccurenceAsCurrent(new ElementInfo(nodeInfo, scope));
        }
    }

    void prepare(ClassInstanceCreation node, Scope scope) {
        ASTNodeInfo<ClassInstanceCreation> nodeInfo = ASTNodeInfo.create(node);
        if (canBePrepared(node, scope)) {
            clasInstanceCreations.put(nodeInfo, scope);
            setOccurenceAsCurrent(new ElementInfo(nodeInfo, scope));
        }
    }

    void prepare(Kind kind, Scalar scalar, Scope scope) {
        ASTNodeInfo<Scalar> nodeInfo = ASTNodeInfo.create(kind, scalar);
        if (canBePrepared(scalar, scope)) {
            constInvocations.put(nodeInfo, scope);
            setOccurenceAsCurrent(new ElementInfo(nodeInfo, scope));
        }
    }

    void prepare(ASTNodeInfo<Scalar> nodeInfo, ConstantElement constantElement) {
        Scalar scalar = nodeInfo.getOriginalNode();
        if (canBePrepared(scalar, constantElement)) {
            constDeclarations.put(nodeInfo, constantElement);
            setOccurenceAsCurrent(new ElementInfo(nodeInfo, constantElement));
        }
    }

    void prepare(ConstantDeclarationInfo constantNodeInfo, ConstantElement scope) {
        if (constantNodeInfo != null && canBePrepared(constantNodeInfo.getOriginalNode(), scope)) {
            constDeclarations53.put(constantNodeInfo, scope);
            setOccurenceAsCurrent(new ElementInfo(constantNodeInfo, scope));
        }
    }


    void prepare(PHPDocTypeTag pHPDocTag, Scope scope) {
        if (canBePrepared(pHPDocTag, scope)) {
            List<? extends PhpDocTypeTagInfo> infos = PhpDocTypeTagInfo.create(pHPDocTag, scope);
            for (PhpDocTypeTagInfo typeTagInfo : infos) {
                docTags.put(typeTagInfo, scope);
                setOccurenceAsCurrent(new ElementInfo(typeTagInfo, scope));
            }
        }
    }

    void prepare(ClassDeclaration classDeclaration, ClassScope scope) {
        if (canBePrepared(classDeclaration, scope)) {
            ClassDeclarationInfo node = ClassDeclarationInfo.create(classDeclaration);
            clasDeclarations.put(node, scope);
            prepare(Kind.CLASS, classDeclaration.getSuperClass(), scope);
            List<Expression> interfaes = classDeclaration.getInterfaes();
            for (Expression iface : interfaes) {
                prepare(Kind.IFACE, iface, scope);
            }
            setOccurenceAsCurrent(new ElementInfo(node, scope));
        }
    }

    void prepare(InterfaceDeclaration interfaceDeclaration, InterfaceScope scope) {
        if (canBePrepared(interfaceDeclaration, scope)) {
            InterfaceDeclarationInfo node = InterfaceDeclarationInfo.create(interfaceDeclaration);
            ifaceDeclarations.put(node, scope);
            List<Expression> interfaes = interfaceDeclaration.getInterfaes();
            for (Expression iface : interfaes) {
                prepare(Kind.IFACE, iface, scope);
            }
            setOccurenceAsCurrent(new ElementInfo(node, scope));
        }
    }

    void prepare(FunctionDeclaration functionDeclaration, FunctionScope scope) {
        if (canBePrepared(functionDeclaration, scope)) {
            FunctionDeclarationInfo node = FunctionDeclarationInfo.create(functionDeclaration);
            fncDeclarations.put(node, scope);
            setOccurenceAsCurrent(new ElementInfo(node, scope));
        }
    }

    void prepare(MethodDeclaration methodDeclaration, MethodScope scope) {
        if (canBePrepared(methodDeclaration, scope)) {
            MethodDeclarationInfo node = MethodDeclarationInfo.create(methodDeclaration);
            methodDeclarations.put(node, scope);
            setOccurenceAsCurrent(new ElementInfo(node, scope));
        }
    }

    void prepare(final MagicMethodDeclarationInfo node, MethodScope scope) {
        if (canBePrepared(node.getOriginalNode(), scope)) {
            if (node.getKind().equals(Kind.METHOD)) {
                magicMethodDeclarations.put(node, scope);
                ElementInfo elementInfo = new ElementInfo(node, scope);
                setOccurenceAsCurrent(elementInfo);
            }
        }
    }

    void prepare(ClassConstantDeclarationInfo constantNodeInfo, ClassConstantElement scope) {
        if (constantNodeInfo != null && canBePrepared(constantNodeInfo.getOriginalNode(), scope)) {
            classConstantDeclarations.put(constantNodeInfo, scope);
            setOccurenceAsCurrent(new ElementInfo(constantNodeInfo, scope));
        }
    }

    private void buildFieldDeclarations(ElementInfo nodeCtxInfo, FileScopeImpl fileScope) {
        for (Entry<SingleFieldDeclarationInfo, FieldElementImpl> entry : fldDeclarations.entrySet()) {
            SingleFieldDeclarationInfo nodeInfo = entry.getKey();
            if (isNameEquality(nodeCtxInfo, nodeInfo, entry.getValue())) {
                final OccurenceImpl occurenceImpl = new OccurenceImpl(entry.getValue(), nodeInfo.getRange(), fileScope);
                fileScope.addOccurence(occurenceImpl);
            }
        }
    }
    
    private void buildFieldInvocations(ElementInfo nodeCtxInfo, FileScopeImpl fileScope) {
        String idName = nodeCtxInfo.getName();
        Map<String, List<FieldElementImpl>> unknownFieldNameCache = new HashMap<String, List<FieldElementImpl>>();
        for (Entry<ASTNodeInfo<FieldAccess>, Scope> entry : fieldInvocations.entrySet()) {
            ASTNodeInfo<FieldAccess> nodeInfo = entry.getKey();
            if (idName.equalsIgnoreCase(nodeInfo.getName())) {
                //List<? extends ModelElement> elems = CachedModelSupport.getInheritedFields(queryName, fileScope);
                Collection<? extends TypeScope> types = getClassName((VariableScope) entry.getValue(), nodeInfo.getOriginalNode());
                List<ClassScope> classes = new ArrayList<ClassScope>();
                for (TypeScope type : types) {
                    if (type instanceof ClassScope) {
                        classes.add((ClassScope) type);
                    }
                }
                Collection<ModelElement> allFields = new HashSet<ModelElement>();
                if (!classes.isEmpty()) {
                    for (ClassScope clz : classes) {
                        Collection<? extends ModelElement> fields = CachingSupport.getInheritedFields(
                                clz, idName, fileScope);
                        //TODO: if not found, then lookup inherited
                        //use ClassScope.getTopInheritedMethods(final String queryName, final int... modifiers)
                        allFields.addAll(fields);
                        if (allFields.isEmpty()) {
                            fileScope.addOccurence(new OccurenceImpl(clz, nodeInfo.getRange(), fileScope));
                        }
                    }
                } else {
                    List<FieldElementImpl> name2Fields = unknownFieldNameCache.get(idName);
                    if (name2Fields == null) {
                        name2Fields = name2Fields(fileScope, idName);
                        if (!name2Fields.isEmpty()) {
                            unknownFieldNameCache.put(idName, name2Fields);
                        }
                    }                    
                    if (!name2Fields.isEmpty()) {
                        @SuppressWarnings("unchecked")
                        Occurence occurenceImpl = new OccurenceImpl(name2Fields, nodeInfo.getRange(), fileScope);
                        fileScope.addOccurence(occurenceImpl);
                    }
                }
                if (!allFields.isEmpty()) {
                    fileScope.addOccurence(new OccurenceImpl(allFields, nodeInfo.getRange(), fileScope));
                }
            }
        }
    }

    private void buildMethodDeclarations(ElementInfo nodeCtxInfo, FileScopeImpl fileScope) {
        for (Entry<ASTNodeInfo<MethodDeclaration>, MethodScope> entry : methodDeclarations.entrySet()) {
            ASTNodeInfo<MethodDeclaration> nodeInfo = entry.getKey();
            if (isNameEquality(nodeCtxInfo, nodeInfo, entry.getValue())) {
                fileScope.addOccurence(new OccurenceImpl(entry.getValue(), nodeInfo.getRange(), fileScope));
            }
        }
    }
    private void buildMagicMethodDeclarations(ElementInfo nodeCtxInfo, FileScopeImpl fileScope) {
        for (Entry<MagicMethodDeclarationInfo, MethodScope> entry : magicMethodDeclarations.entrySet()) {
            MagicMethodDeclarationInfo nodeInfo = entry.getKey();
            if (isNameEquality(nodeCtxInfo, nodeInfo, entry.getValue())) {
                fileScope.addOccurence(new OccurenceImpl(entry.getValue(), nodeInfo.getRange(), fileScope));
            }
        }
    }

    private void buildMethodInvocations(ElementInfo nodeCtxInfo, FileScopeImpl fileScope) {
        String idName = nodeCtxInfo.getName();
        Map<String, List<MethodScope>> unknownMethodNameCache = new HashMap<String, List<MethodScope>>();
        for (Entry<ASTNodeInfo<MethodInvocation>, Scope> entry : methodInvocations.entrySet()) {
            ASTNodeInfo<MethodInvocation> nodeInfo = entry.getKey();
            if (idName.equalsIgnoreCase(nodeInfo.getName())) {
                Collection<? extends TypeScope> types = getClassName((VariableScope) entry.getValue(), nodeInfo.getOriginalNode());
                Collection<ModelElement> allMethods = new HashSet<ModelElement>();
                if (!types.isEmpty()) {
                    for (TypeScope type : types) {
                        Collection<? extends ModelElement> methods = CachingSupport.getInheritedMethods(
                                type, idName, fileScope);
                        //TODO: if not found, then lookup inherited
                        //use ClassScope.getTopInheritedMethods(final String queryName, final int... modifiers)
                        allMethods.addAll(methods);
                    }
                } else {
                    List<MethodScope> name2Methods = unknownMethodNameCache.get(idName);
                    if (name2Methods == null) {
                        name2Methods = name2Methods(fileScope, idName, nodeInfo);
                        if (!name2Methods.isEmpty()) {
                            unknownMethodNameCache.put(idName, name2Methods);
                        }
                    }
                    if (!name2Methods.isEmpty()) {
                        @SuppressWarnings("unchecked")
                        Occurence occurenceImpl = new OccurenceImpl(name2Methods, nodeInfo.getRange(), fileScope);
                        fileScope.addOccurence(occurenceImpl);
                    }
                }
                if (!allMethods.isEmpty()) {
                    fileScope.addOccurence(new OccurenceImpl(allMethods, nodeInfo.getRange(), fileScope));
                }
            }
        }
    }

    private void buildIncludes(ElementInfo nodeCtxInfo, FileScopeImpl fileScope) {
        String idName = nodeCtxInfo.getName();
        for (Entry<IncludeInfo, IncludeElement> entry : includes.entrySet()) {
            IncludeInfo nodeInfo = entry.getKey();
            if (idName.equalsIgnoreCase(nodeInfo.getName())) {
                fileScope.addOccurence(new OccurenceImpl(entry.getValue(), nodeInfo.getRange(), fileScope));
            }
        }
    }

    private void buildConstantInvocations(ElementInfo nodeCtxInfo, FileScopeImpl fileScope) {
        String idName = nodeCtxInfo.getName();
        for (Entry<ASTNodeInfo<Scalar>, Scope> entry : constInvocations.entrySet()) {
            ASTNodeInfo<Scalar> nodeInfo = entry.getKey();
            if (idName.equalsIgnoreCase(nodeInfo.getName())) {
                List<? extends ModelElement> elems = CachingSupport.getConstants(idName, fileScope);
                if (!elems.isEmpty()) {
                    fileScope.addOccurence(new OccurenceImpl(elems, nodeInfo.getRange(), fileScope));
                }
            }
        }
        for (Entry<ASTNodeInfo<Expression>, Scope> entry : nsConstInvocations.entrySet()) {
            ASTNodeInfo<Expression> nodeInfo = entry.getKey();
            Expression originalNode = nodeInfo.getOriginalNode();
            if (originalNode instanceof  NamespaceName && idName.equalsIgnoreCase(nodeInfo.getName())) {
                List<? extends ModelElement> elems = CachingSupport.getConstants(idName, fileScope);
                if (!elems.isEmpty()) {
                    fileScope.addOccurence(new OccurenceImpl(elems, nodeInfo.getRange(), fileScope));
                }
            }
        }

    }

    private void buildConstantDeclarations(ElementInfo nodeCtxInfo, FileScopeImpl fileScope) {
        String idName = nodeCtxInfo.getName();
        for (Entry<ASTNodeInfo<Scalar>, ConstantElement> entry : constDeclarations.entrySet()) {
            ASTNodeInfo<Scalar> nodeInfo = entry.getKey();
            if (idName.equalsIgnoreCase(nodeInfo.getName())) {
                fileScope.addOccurence(new OccurenceImpl(entry.getValue(), nodeInfo.getRange(), fileScope));
            }
        }
        for (Entry<ConstantDeclarationInfo, ConstantElement> entry : constDeclarations53.entrySet()) {
            ClassConstantDeclarationInfo nodeInfo = entry.getKey();
            if (idName.equalsIgnoreCase(nodeInfo.getName())) {
                fileScope.addOccurence(new OccurenceImpl(entry.getValue(), nodeInfo.getRange(), fileScope));
            }
        }
    }

    private void buildStaticConstantDeclarations(ElementInfo nodeCtxInfo, FileScopeImpl fileScope) {
        for (Entry<ASTNodeInfo<Identifier>, ClassConstantElement> entry : classConstantDeclarations.entrySet()) {
            ASTNodeInfo<Identifier> nodeInfo = entry.getKey();
            if (isNameEquality(nodeCtxInfo, nodeInfo, entry.getValue())) {
                Collection<ClassConstantElement> cnstants = Collections.singleton(entry.getValue());
                fileScope.addOccurence(new OccurenceImpl(cnstants,nodeInfo.getRange(), fileScope));
            }

        }
    }

    private void buildStaticMethodInvocations(ElementInfo nodeCtxInfo, FileScopeImpl fileScope) {
        for (Entry<ASTNodeInfo<StaticMethodInvocation>, Scope> entry : staticMethodInvocations.entrySet()) {
            ASTNodeInfo<StaticMethodInvocation> nodeInfo = entry.getKey();
            String methodName = nodeInfo.getName();
            final Scope scope = entry.getValue();
            Collection<MethodElement> allMethods = Collections.emptyList();
            if (isNameEquality(nodeCtxInfo, nodeInfo,scope)) {
                QualifiedName queryTypeName = nodeCtxInfo.getTypeQualifiedName();
                QualifiedName currentTypeName = ASTNodeInfo.toQualifiedName(nodeInfo.getOriginalNode(), true);

                Collection<ModelElement> methods = new HashSet<ModelElement>();
                final String clzName = currentTypeName.toName().toString();
                if (clzName == null) return;
                boolean isParent = clzName.equalsIgnoreCase("parent");//NOI18N
                boolean isSelf = clzName.equalsIgnoreCase("self");//NOI18N
                
                NamespaceIndexFilter filterQuery = new NamespaceIndexFilter(queryTypeName.toString());
                if (isParent || isSelf) {
                    TypeScope typeScope = ModelUtils.getFirst(VariousUtils.getStaticTypeName(scope, clzName));
                    if (typeScope != null) {
                        currentTypeName = typeScope.getNamespaceName().append(QualifiedName.create(typeScope.getName()));
                    }
                }
                IndexScope indexScope = ModelUtils.getIndexScope(fileScope);
                Index index = indexScope.getIndex();
                allMethods = index.getAllMethods(NameKind.exact(currentTypeName), NameKind.exact(methodName));
                //allMethods = indexScope.findMethods(null, methodName, modifiers)
                if (!isParent && !isSelf) {
                    allMethods = filterQuery.filter(allMethods, true);
                }
                for (MethodElement method : allMethods) {
                    TypeElement type = method.getType();
                    TypeScopeImpl csi = null;
                    if (type instanceof ClassElement) {
                        csi = new ClassScopeImpl(indexScope, (ClassElement) type);
                    } else if (type instanceof InterfaceElement) {
                        csi = new InterfaceScopeImpl(indexScope, (InterfaceElement) type);
                    }
                    methods.add(new MethodScopeImpl(csi, method));
                }
                
                if (methods != null && !methods.isEmpty()) {
                    fileScope.addOccurence(new OccurenceImpl(methods, nodeInfo.getRange(), fileScope));
                }

            }
        }
    }

    private void buildStaticFieldInvocations(ElementInfo nodeCtxInfo, FileScopeImpl fileScope) {
        for (Entry<ASTNodeInfo<StaticFieldAccess>, Scope> entry : staticFieldInvocations.entrySet()) {
            ASTNodeInfo<StaticFieldAccess> nodeInfo = entry.getKey();
            String fieldName = nodeInfo.getName();
            if (fieldName == null) continue;
            if (fieldName.startsWith("$")) {
                fieldName = fieldName.substring(1);
            }
            final Scope scope = entry.getValue();
            if (isNameEquality(nodeCtxInfo, nodeInfo, entry.getValue())) {
                QualifiedName queryQN = nodeCtxInfo.getTypeQualifiedName();
                QualifiedName nodeQN = ASTNodeInfo.toQualifiedName(nodeInfo.getOriginalNode(), true);
                 Collection<ModelElement> methods = new HashSet<ModelElement>();
                final String clzName = nodeQN.toName().toString();
                if (clzName == null) return;
                boolean isParent = clzName.equalsIgnoreCase("parent");//NOI18N
                boolean isSelf = clzName.equalsIgnoreCase("self");//NOI18N

                NamespaceIndexFilter filterQuery = new NamespaceIndexFilter(queryQN.toString());
                if (isParent || isSelf) {
                    TypeScope typeScope = ModelUtils.getFirst(VariousUtils.getStaticTypeName(scope, clzName));
                    if (typeScope != null) {
                        nodeQN = typeScope.getNamespaceName().append(QualifiedName.create(typeScope.getName()));
                    }
                }
                IndexScope indexScope = ModelUtils.getIndexScope(fileScope);
                Index index = indexScope.getIndex();
                Collection<FieldElement> allFields = Collections.emptyList();
                allFields = index.getAlllFields(NameKind.exact(nodeQN), NameKind.exact(fieldName));
                if (!isParent && !isSelf) {
                    allFields = filterQuery.filter(allFields, true);
                }
                for (FieldElement field : allFields) {
                    TypeElement type = field.getType();
                    TypeScopeImpl csi = null;
                    if (type instanceof ClassElement) {
                        csi = new ClassScopeImpl(indexScope, (ClassElement) type);
                    } else if (type instanceof InterfaceElement) {
                        csi = new InterfaceScopeImpl(indexScope, (InterfaceElement) type);
                    }
                    methods.add(new FieldElementImpl(csi, field));
                }

                if (methods != null && !methods.isEmpty()) {
                    fileScope.addOccurence(new OccurenceImpl(methods, nodeInfo.getRange(), fileScope));
                }
            }
        }
    }

    private void buildStaticConstantInvocations(ElementInfo nodeCtxInfo, FileScopeImpl fileScope) {
        String constName = nodeCtxInfo.getName();
        for (Entry<ASTNodeInfo<StaticConstantAccess>, Scope> entry : staticConstantInvocations.entrySet()) {
            ASTNodeInfo<StaticConstantAccess> nodeInfo = entry.getKey();
            final Scope scope = entry.getValue();
            if (isNameEquality(nodeCtxInfo, nodeInfo, entry.getValue())) {
                QualifiedName queryQN = nodeCtxInfo.getTypeQualifiedName();
                QualifiedName nodeQN = ASTNodeInfo.toQualifiedName(nodeInfo.getOriginalNode(), true);
                 Collection<ModelElement> constants = new HashSet<ModelElement>();
                final String clzName = nodeQN.toName().toString();
                if (clzName == null) return;
                boolean isParent = clzName.equalsIgnoreCase("parent");//NOI18N
                boolean isSelf = clzName.equalsIgnoreCase("self");//NOI18N

                NamespaceIndexFilter filterQuery = new NamespaceIndexFilter(queryQN.toString());
                if (isParent || isSelf) {
                    TypeScope typeScope = ModelUtils.getFirst(VariousUtils.getStaticTypeName(scope, clzName));
                    if (typeScope != null) {
                        nodeQN = typeScope.getNamespaceName().append(QualifiedName.create(typeScope.getName()));
                    }
                }
                IndexScope indexScope = ModelUtils.getIndexScope(fileScope);
                Index index = indexScope.getIndex();
                Collection<TypeConstantElement> allConstants = Collections.emptyList();
                allConstants = index.getAllTypeConstants(NameKind.exact(nodeQN), NameKind.exact(constName));
                if (!isParent && !isSelf) {
                    allConstants = filterQuery.filter(allConstants, true);
                }
                for (TypeConstantElement constant : allConstants) {
                    TypeElement type = constant.getType();
                    TypeScopeImpl csi = null;
                    if (type instanceof ClassElement) {
                        csi = new ClassScopeImpl(indexScope, (ClassElement) type);
                    } else if (type instanceof InterfaceElement) {
                        csi = new InterfaceScopeImpl(indexScope, (InterfaceElement) type);
                    }
                    constants.add(new ClassConstantElementImpl(csi, constant));
                }

                if (constants != null && !constants.isEmpty()) {
                    fileScope.addOccurence(new OccurenceImpl(constants, nodeInfo.getRange(), fileScope));
                }
            }
        }
    }

    private void buildDocTagsForClasses(ElementInfo nodeCtxInfo, FileScopeImpl fileScope) {
        for (Entry<PhpDocTypeTagInfo, Scope> entry : docTags.entrySet()) {
            PhpDocTypeTagInfo nodeInfo = entry.getKey();
            if (Kind.CLASS.equals(nodeInfo.getKind()) && isNameEquality(nodeCtxInfo, nodeInfo, entry.getValue())) {
                collectTypeDeclarations(fileScope, PhpElementKind.CLASS);
                collectTypeDeclarations(fileScope, PhpElementKind.IFACE);
                if (!declarations.isEmpty()) {
                    fileScope.addOccurence(new OccurenceImpl(declarations, nodeInfo.getRange(), fileScope));
                }
            }
        }
    }
    
    private void buildClassInstanceCreation(ElementInfo query, FileScopeImpl fileScope) {
        for (Entry<ASTNodeInfo<ClassInstanceCreation>, Scope> entry : clasInstanceCreations.entrySet()) {
            ASTNodeInfo<ClassInstanceCreation> nodeInfo = entry.getKey();
            if (isNameEquality(query, nodeInfo, entry.getValue())) {
                collectTypeDeclarations(fileScope, PhpElementKind.CONSTRUCTOR);
                if (!declarations.isEmpty()) {
                    fileScope.addOccurence(new OccurenceImpl(declarations, nodeInfo.getRange(), fileScope));
                }
            }
        }
    }

    private void buildClassNames(ElementInfo nodeCtxInfo, FileScopeImpl fileScope) {
        for (Entry<ASTNodeInfo<ClassName>, Scope> entry : clasNames.entrySet()) {
            ASTNodeInfo<ClassName> nodeInfo = entry.getKey();
            if (isNameEquality(nodeCtxInfo, nodeInfo, entry.getValue())) {
                collectTypeDeclarations(fileScope, PhpElementKind.CLASS);
                if (!declarations.isEmpty()) {
                    fileScope.addOccurence(new OccurenceImpl(declarations, nodeInfo.getRange(), fileScope));
                }
            }
        }
    }

    private void buildInterfaceIDs(ElementInfo nodeCtxInfo, FileScopeImpl fileScope) {
        for (Entry<ASTNodeInfo<Expression>, Scope> entry : ifaceIDs.entrySet()) {
            ASTNodeInfo<Expression> nodeInfo = entry.getKey();
            if (isNameEquality(nodeCtxInfo, nodeInfo, entry.getValue())) {
                collectTypeDeclarations(fileScope, PhpElementKind.IFACE);
                if (!declarations.isEmpty()) {
                    fileScope.addOccurence(new OccurenceImpl(declarations, nodeInfo.getRange(), fileScope));
                }
            }
        }
    }

    private void buildClassIDs(ElementInfo nodeCtxInfo, FileScopeImpl fileScope) {
        for (Entry<ASTNodeInfo<Expression>, Scope> entry : clasIDs.entrySet()) {
            ASTNodeInfo<Expression> nodeInfo = entry.getKey();
            if (isNameEquality(nodeCtxInfo, nodeInfo, entry.getValue())) {
                collectTypeDeclarations(fileScope, PhpElementKind.CLASS);
                if (!declarations.isEmpty()) {
                    fileScope.addOccurence(new OccurenceImpl(declarations, nodeInfo.getRange(), fileScope));
                }
            }
        }
    }

    private void buildInterfaceDeclarations(ElementInfo nodeCtxInfo, FileScopeImpl fileScope) {
        for (Entry<InterfaceDeclarationInfo, InterfaceScope> entry : ifaceDeclarations.entrySet()) {
            InterfaceDeclarationInfo nodeInfo = entry.getKey();
            if (isNameEquality(nodeCtxInfo, nodeInfo, entry.getValue())) {
                collectTypeDeclarations(fileScope, PhpElementKind.IFACE);
                if (!declarations.isEmpty()) {
                    fileScope.addOccurence(new OccurenceImpl(declarations, nodeInfo.getRange(), fileScope));
                }
            }

        }
    }

    private void buildClassDeclarations(ElementInfo query, FileScopeImpl fileScope) {
        for (Entry<ClassDeclarationInfo, ClassScope> entry : clasDeclarations.entrySet()) {
            ClassDeclarationInfo nodeInfo = entry.getKey();
            if (isNameEquality(query, nodeInfo, entry.getValue())) {
                collectTypeDeclarations(fileScope, PhpElementKind.CLASS);
                if (!declarations.isEmpty()) {
                    fileScope.addOccurence(new OccurenceImpl(declarations, nodeInfo.getRange(), fileScope));
                }
            }
        }
    }

    private void buildFunctionDeclarations(ElementInfo nodeCtxInfo, FileScopeImpl fileScope) {
        for (Entry<ASTNodeInfo<FunctionDeclaration>, FunctionScope> entry : fncDeclarations.entrySet()) {
            ASTNodeInfo<FunctionDeclaration> nodeInfo = entry.getKey();
            if (isNameEquality(nodeCtxInfo, nodeInfo, entry.getValue())) {
                fileScope.addOccurence(new OccurenceImpl(entry.getValue(), nodeInfo.getRange(), fileScope));
            }

        }
    }

    private void buildFunctionInvocations(final ElementInfo nodeCtxInfo, FileScopeImpl fileScope) {
        String idName = nodeCtxInfo.getName();
        for (Entry<ASTNodeInfo<FunctionInvocation>, Scope> entry : fncInvocations.entrySet()) {
            ASTNodeInfo<FunctionInvocation> nodeInfo = entry.getKey();
            if (isNameEquality(nodeCtxInfo, nodeInfo, entry.getValue())) {
                Collection<? extends FunctionScope> elems = CachingSupport.getFunctions(idName, fileScope);
                /*elems = ModelUtils.filter(elems, new ModelUtils.ElementFilter<FunctionScope>() {
                    public boolean isAccepted(FunctionScope element) {
                        return ModelUtils.nameKindMatch(element.getNamespaceName() , QuerySupport.Kind.EXACT, namespaceName);
                    }
                });*/
                if (!elems.isEmpty()) {
                    fileScope.addOccurence(new OccurenceImpl(elems, nodeInfo.getRange(), fileScope));
                }

            }
        }
    }

    private void buildDocTagsForVars(ElementInfo nodeCtxInfo, FileScopeImpl fileScope) {
        String idName = nodeCtxInfo.getName();
        for (Entry<PhpDocTypeTagInfo, Scope> entry : docTags.entrySet()) {
            PhpDocTypeTagInfo nodeInfo = entry.getKey();
            String name = nodeInfo.getName();
            Scope scope = entry.getValue();
            if (Kind.VARIABLE.equals(nodeInfo.getKind()) && scope instanceof VariableScope && idName.equalsIgnoreCase(name)) {
                VariableScope varScope = (VariableScope) entry.getValue();
                List<? extends ModelElement> elems = ModelUtils.filter(varScope.getDeclaredVariables(), name);
                if (elems.isEmpty()) {
                    elems = ModelUtils.filter(ModelUtils.getDeclaredVariables(fileScope), name);
                    }

                if (!elems.isEmpty()) {
                    fileScope.addOccurence(new OccurenceImpl(elems, nodeInfo.getRange(), fileScope));
                }

            }
        }
    }
    private void buildDocTagsForFields(ElementInfo nodeCtxInfo, FileScopeImpl fileScope) {
        String idName = nodeCtxInfo.getName();
        for (Entry<PhpDocTypeTagInfo, Scope> entry : docTags.entrySet()) {
            PhpDocTypeTagInfo nodeInfo = entry.getKey();
            String name = nodeInfo.getName();
            Scope scope = entry.getValue();
            if (Kind.FIELD.equals(nodeInfo.getKind()) && scope instanceof ClassScope && idName.equalsIgnoreCase(name)) {
                Collection<? extends ClassScope> classes = CachingSupport.getClasses(scope.getName(), scope);

                Collection<ModelElement> allFields = new HashSet<ModelElement>();
                if (!classes.isEmpty()) {
                    for (ClassScope clz : classes) {
                        Collection<? extends ModelElement> fields = CachingSupport.getInheritedFields(
                                clz, idName, fileScope);
                        //TODO: if not found, then lookup inherited
                        //use ClassScope.getTopInheritedMethods(final String queryName, final int... modifiers)
                        allFields.addAll(fields);
                        if (allFields.isEmpty()) {
                            fileScope.addOccurence(new OccurenceImpl(clz, nodeInfo.getRange(), fileScope));
                        }
                    }
                } 
                if (!allFields.isEmpty()) {
                    fileScope.addOccurence(new OccurenceImpl(allFields, nodeInfo.getRange(), fileScope));
                }
            }
        }
    }
    private void buildGotoStatements(ElementInfo nodeCtxInfo, FileScopeImpl fileScope) {
        buildGoto(nodeCtxInfo, gotoStatement, fileScope);
    }
    private void buildGotoLabels(ElementInfo nodeCtxInfo, FileScopeImpl fileScope) {
        buildGoto(nodeCtxInfo, gotoLabel, fileScope);
    }
    private static <T extends ASTNode> void buildGoto(ElementInfo nodeCtxInfo, Map<ASTNodeInfo<T>, Scope> entries, FileScopeImpl fileScope) {
        String currentName = nodeCtxInfo.getName();
        Scope currentScope = nodeCtxInfo.getScope();
        for (Entry<ASTNodeInfo<T>, Scope> entry : entries.entrySet()) {
            ASTNodeInfo<T> nodeInfo = entry.getKey();
            String name = nodeInfo.getName();
            Scope scope = entry.getValue();
            if (currentName.equalsIgnoreCase(name) && currentScope == scope) {
                fileScope.addOccurence(new OccurenceImpl(entry.getValue(), nodeInfo.getRange(), fileScope) {
                    @Override
                    public boolean gotoDeclarationEnabled() {
                        return false;
                    }
                });
            }
        }
    }

    private void buildVariables(ElementInfo nodeCtxInfo, FileScopeImpl fileScope) {
        String idName = nodeCtxInfo.getName();
        for (Entry<ASTNodeInfo<Variable>, Scope> entry : variables.entrySet()) {
            ASTNodeInfo<Variable> nodeInfo = entry.getKey();
            String name = nodeInfo.getName();
            if (idName.equalsIgnoreCase(nodeInfo.getName())) {
                VariableScope varScope = (VariableScope) entry.getValue();
                List<? extends ModelElement> elems = ModelUtils.filter(varScope.getDeclaredVariables(), name);
                if (elems.isEmpty()) {
                    elems = ModelUtils.filter(ModelUtils.getDeclaredVariables(fileScope), name);
                    }

                if (!elems.isEmpty()) {
                    fileScope.addOccurence(new OccurenceImpl(elems, nodeInfo.getRange(), fileScope));
                }

            }
        }
    }

    void build(FileScopeImpl fileScope) {
        if (currentContextInfo == null && element != null) {
            ElementInfo contextInfo = new ElementInfo(element);
            if (contextInfo.getName() != null && contextInfo.getName().trim().length() > 0) {
                currentContextInfo = contextInfo;
            }
        }
        ASTNodeInfo.Kind kind = currentContextInfo != null ? currentContextInfo.getKind() : null;
        if (currentContextInfo != null && kind != null) {
            switch (kind) {
                case GOTO:
                    buildGotoLabels(currentContextInfo, fileScope);
                    buildGotoStatements(currentContextInfo, fileScope);
                    break;
                case FUNCTION:
                    buildFunctionInvocations(currentContextInfo, fileScope);
                    buildFunctionDeclarations(currentContextInfo, fileScope);
                    break;

                case VARIABLE:
                    buildVariables(currentContextInfo, fileScope);
                    buildDocTagsForVars(currentContextInfo, fileScope);
                    break;

                case STATIC_METHOD:
                    buildStaticMethodInvocations(currentContextInfo, fileScope);
                    buildMethodDeclarations(currentContextInfo, fileScope);
                    break;

                case FIELD:
                case STATIC_FIELD:
                    buildFieldDeclarations(currentContextInfo, fileScope);
                    buildFieldInvocations(currentContextInfo, fileScope);
                    buildStaticFieldInvocations(currentContextInfo, fileScope);
                    buildDocTagsForFields(currentContextInfo, fileScope);
                    break;

                case CONSTANT:
                    buildConstantInvocations(currentContextInfo, fileScope);
                    buildConstantDeclarations(currentContextInfo, fileScope);
                    break;

                case CLASS_CONSTANT:
                case STATIC_CLASS_CONSTANT:
                    buildStaticConstantInvocations(currentContextInfo, fileScope);
                    buildStaticConstantDeclarations(currentContextInfo, fileScope);
                    break;

                case CLASS_INSTANCE_CREATION:
                case CLASS:
                case IFACE:
                    Union2<ASTNodeInfo, ModelElement> rawElement = currentContextInfo.getRawElement();
                    final boolean isClassInstanceCreation = rawElement.hasFirst() &&
                            rawElement.first().getOriginalNode() instanceof ClassInstanceCreation;
                    if (isClassInstanceCreation) {
                        buildClassInstanceCreation(currentContextInfo, fileScope);
                    }
                    buildClassNames(currentContextInfo, fileScope);
                    buildClassIDs(currentContextInfo, fileScope);
                    buildClassDeclarations(currentContextInfo, fileScope);
                    buildDocTagsForClasses(currentContextInfo, fileScope);
                    buildInterfaceIDs(currentContextInfo, fileScope);
                    buildInterfaceDeclarations(currentContextInfo, fileScope);
                    if (!isClassInstanceCreation) {
                        buildClassInstanceCreation(currentContextInfo, fileScope);
                    }
                    break;
                case METHOD:
                    buildMethodInvocations(currentContextInfo, fileScope);
                    buildMethodDeclarations(currentContextInfo, fileScope);
                    buildMagicMethodDeclarations(currentContextInfo, fileScope);
                    break;
                case INCLUDE:
                    buildIncludes(currentContextInfo, fileScope);
                    break;
                default:
                    throw new IllegalStateException();

            }
        }

    }

    private void collectTypeDeclarations(FileScopeImpl fileScope, PhpElementKind kind) {
        if (declarations.isEmpty() /*|| !declarations.iterator().next().getPhpElementKind().equals(kind)*/) {
            declarations.clear();
            IndexScope indexScope = ModelUtils.getIndexScope(fileScope);
            List<? extends ModelElement> foundElems = Collections.emptyList();
            Collection<QualifiedName> composedNames = currentContextInfo.getComposedNames();
            Index index = indexScope.getIndex();
            if (!composedNames.isEmpty()) {
                switch(kind) {
                    case CLASS:                        
                        foundElems = indexScope.findClasses(currentContextInfo.getName());
                        break;
                    case IFACE:
                        foundElems = indexScope.findInterfaces(currentContextInfo.getName());
                        break;
                    case CONSTRUCTOR:
                        Collection<ModelElement> elements = new HashSet<ModelElement>();
                        Collection<ClassElement> classes = new HashSet<ClassElement>(index.getClasses(NameKind.exact(currentContextInfo.getName())));
                        Collection<MethodElement> constructors = new HashSet<MethodElement>();
                        for (ClassElement cls : classes) {
                            final Collection<MethodElement> tmpConstruct = index.getDeclaredConstructors(cls);
                            if (!tmpConstruct.isEmpty()) {
                                constructors.addAll(tmpConstruct);
                            } else {
                                elements.add(new ClassScopeImpl(indexScope, cls));
                            }
                        }                        
                        for (MethodElement clsMember : constructors) {
                            ClassScopeImpl csi = new ClassScopeImpl(indexScope, (ClassElement) clsMember.getType());
                            elements.add(new MethodScopeImpl(csi, clsMember));

                        }
                        foundElems = new ArrayList<ModelElement>(elements);
                        break;
                }

                if (!foundElems.isEmpty()) {
                    for (QualifiedName qn : composedNames) {
                        NamespaceIndexFilter filter = new NamespaceIndexFilter(qn.toString());
                        final Collection filterModelElements = filter.filterModelElements(foundElems, true);
                        declarations.addAll(filterModelElements);
                    }
                } 
            }
        }
    }

    private boolean canBePrepared(ASTNode node, ModelElement scope) {
        return (getOffset() >= 0 || element != null) && scope != null && node != null;
    }

    private void setOccurenceAsCurrent(ElementInfo contextInfo) {
        if (element == null && contextInfo != null) {
            if (contextInfo.getName() != null && contextInfo.getName().trim().length() > 0) {
                OffsetRange range = contextInfo.getRange();
                if (range.containsInclusive(getOffset())) {
                    currentContextInfo = contextInfo;
                }
            }
        }
    }

    private static Collection<? extends TypeScope> getClassName(VariableScope scp, VariableBase varBase) {
        String vartype = VariousUtils.extractTypeFroVariableBase(varBase, 
                Collections.<String,AssignmentImpl>emptyMap());
        return VariousUtils.getType(scp, vartype, varBase.getStartOffset(), true);
    }

    @SuppressWarnings("unchecked")
    private static List<MethodScope> methods4TypeNames(FileScopeImpl fileScope, Set<String> typeNamesForIdentifier, final String name) {
        List<ClassScope> classes = new ArrayList<ClassScope>();
        for (Iterator<String> it = typeNamesForIdentifier.iterator(); it.hasNext();) {
            String type = it.next();
            classes.addAll(CachingSupport.getClasses(type, fileScope));
        }
        final Set<MethodScope> methodSet = new HashSet<MethodScope>();
        for (ClassScope classScope : classes) {
            methodSet.addAll((List<MethodScope>) CachingSupport.getInheritedMethods(classScope, name, fileScope));
        }

        final List<MethodScope> methods = new ArrayList<MethodScope>(methodSet);
        return methods;
    }

    @SuppressWarnings("unchecked")
    private static List<FieldElementImpl> flds4TypeNames(FileScopeImpl fileScope, Set<String> typeNamesForIdentifier, final String name) {
        List<ClassScope> classes = new ArrayList<ClassScope>();
        for (Iterator<String> it = typeNamesForIdentifier.iterator(); it.hasNext();) {
            String type = it.next();
            classes.addAll(CachingSupport.getClasses(type, fileScope));
        }

        final Set<FieldElementImpl> fldSet = new HashSet<FieldElementImpl>();
        for (ClassScope classScope : classes) {
            fldSet.addAll((List<FieldElementImpl>) CachingSupport.getInheritedFields(classScope, name, fileScope, PhpModifiers.ALL_FLAGS));
        }

        final List<FieldElementImpl> fields = new ArrayList<FieldElementImpl>(fldSet);
        return fields;
    }

    private static List<MethodScope> name2Methods(FileScopeImpl fileScope, final String name, ASTNodeInfo<MethodInvocation> nodeInfo ) {
        IndexScope indexScope = fileScope.getIndexScope();
        Index index = indexScope.getIndex();
        List<MethodScope> retval = new ArrayList<MethodScope>();
        FunctionInvocation functionInvocation = nodeInfo.getOriginalNode().getMethod();
        int paramCount = functionInvocation.getParameters().size();
        Collection<MethodElement> methods = index.getMethods(NameKind.prefix(name));
        for (MethodElement meth : methods) {
            List<? extends ParameterElement> parameters = meth.getParameters();
            if (ModelElementImpl.nameKindMatch(name, QuerySupport.Kind.EXACT, meth.getName()) && paramCount >= numberOfMandatoryParams(parameters) && paramCount <= parameters.size()) {            
                String in = meth.getIn();
                if (in != null) {
                    ClassScope clz = ModelUtils.getFirst(CachingSupport.getClasses(in, fileScope));
                    if (clz != null) {
                        retval.add(new MethodScopeImpl(clz, meth));
                    } else {
                        InterfaceScope iface = ModelUtils.getFirst(CachingSupport.getInterfaces(in, fileScope));
                        if (iface != null) {
                            retval.add(new MethodScopeImpl(iface, meth));
                        }
                    }
                }
            }
        }
        return retval;
    }

    private static List<FieldElementImpl> name2Fields(FileScopeImpl fileScope, String name) {
        IndexScope indexScope = fileScope.getIndexScope();
        Index index = indexScope.getIndex();
        List<FieldElementImpl> retval = new ArrayList<FieldElementImpl>();
        Collection<FieldElement> fields = index.getFields(NameKind.prefix(name.startsWith("$") ? name.substring(1) : name));
        for (FieldElement fld : fields) {
            if (ModelElementImpl.nameKindMatch(name, QuerySupport.Kind.EXACT, fld.getName())) {
                String in = fld.getIn();
                if (in != null) {
                    ClassScope clz = ModelUtils.getFirst(CachingSupport.getClasses(in, fileScope));
                    if (clz != null) {
                        retval.add(new FieldElementImpl(clz, fld));
                    } else {
                        InterfaceScope iface = ModelUtils.getFirst(CachingSupport.getInterfaces(in, fileScope));
                        if (iface != null) {
                            retval.add(new FieldElementImpl(iface, fld));
                        }
                    }
                }
            }
        }
        return retval;
    }

    /**
     * @return the offset
     */
    int getOffset() {
        return offset;
    }
    private static int numberOfMandatoryParams(List<? extends ParameterElement> params) {
            int mandatory = 0;
            for (ParameterElement parameter : params) {
                if (parameter.isMandatory()) {
                    mandatory++;
                }
            }
            return mandatory;
    }


    private static boolean isNameEquality(ElementInfo query, PhpDocTypeTagInfo node, ModelElement nodeScope) {
        String idName = query.getName();
        QualifiedName nodeQN = QualifiedName.create(node.getTypeName());
        if (idName.equalsIgnoreCase(nodeQN.toName().toString())) {
            final QualifiedName queryQN = query.getQualifiedName();
            final Collection<QualifiedName> queryComposedNames = QualifiedName.getComposedNames(queryQN,query.getNamespaceScope());
            final Collection<QualifiedName> nodeQomposedNames = QualifiedName.getComposedNames(nodeQN,ModelUtils.getNamespaceScope(nodeScope));
            queryComposedNames.retainAll(nodeQomposedNames);
            return !queryComposedNames.isEmpty();
        }
        return false;
    }
    private static boolean isNameEquality(ElementInfo query, ASTNodeInfo node, ModelElement nodeScope) {
        String idName = query.getName();
        if (idName.equalsIgnoreCase(node.getName())) {            
            QualifiedName queryQN = query.getQualifiedName();
            QualifiedName nodeQN = node.getQualifiedName();
            if (queryQN.equals(nodeQN)) return true;
            final Collection<QualifiedName> queryComposedNames = QualifiedName.getComposedNames(queryQN,query.getNamespaceScope());
            final Collection<QualifiedName> nodeQomposedNames = QualifiedName.getComposedNames(nodeQN,ModelUtils.getNamespaceScope(nodeScope));
            queryComposedNames.retainAll(nodeQomposedNames);
            return !queryComposedNames.isEmpty();
        }
        return false;
    }
}
