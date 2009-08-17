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
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.nodes.ClassDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.IncludeInfo;
import org.netbeans.modules.php.editor.model.nodes.InterfaceDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.MethodDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.NamespaceDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.SingleFieldDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Include;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Program;

/**
 *
 * @author Radek Matous
 */
class ModelBuilder {
    private final FileScopeImpl fileScope;
    private NamespaceScopeImpl namespaceScope;
    private NamespaceScopeImpl defaultNamespaceScope;
    private Stack<ScopeImpl> currentScope;
    private Program program;
    private final Map<VariableContainerImpl, Map<String, VariableNameImpl>> vars;

    ModelBuilder(FileScopeImpl fileScope) {
        this.fileScope = fileScope;
        this.currentScope = new Stack<ScopeImpl>();
        this.vars = new HashMap<VariableContainerImpl, Map<String, VariableNameImpl>>();
        setCurrentScope(fileScope);
        setCurrentScope(namespaceScope = defaultNamespaceScope = new NamespaceScopeImpl(fileScope));
    }

    NamespaceScope build(NamespaceDeclaration node, OccurenceBuilder occurencesBuilder) {
        final NamespaceDeclarationInfo info = NamespaceDeclarationInfo.create(node);

        NamespaceScopeImpl nScope = (info.isDefaultNamespace())? defaultNamespaceScope://NOI18N
            ModelElementFactory.create( info, this);
        if (!nScope.isDefaultNamespace()) {
            setCurrentScope(nScope);
        }
        nScope.setBlockRange(node);
        return nScope;
    }

    ClassScope build(ClassDeclaration node, OccurenceBuilder occurencesBuilder) {
        ClassScopeImpl classScope = ModelElementFactory.create(ClassDeclarationInfo.create(node), this);
        setCurrentScope(classScope);
        occurencesBuilder.prepare(node,classScope);
        return classScope;
    }

    void build(FieldsDeclaration node, OccurenceBuilder occurencesBuilder) {
        List<? extends SingleFieldDeclarationInfo> infos = SingleFieldDeclarationInfo.create(node);
        for (SingleFieldDeclarationInfo sfdi : infos) {
            FieldElementImpl fei = ModelElementFactory.create(sfdi, this);
            occurencesBuilder.prepare(sfdi,fei);
        }
    }

    void build(Include node, OccurenceBuilder occurencesBuilder) {
        IncludeElementImpl inclImpl = ModelElementFactory.create(IncludeInfo.create(node), this);
        if (inclImpl != null) {
            occurencesBuilder.prepare(node,inclImpl);
        }
    }

    InterfaceScope build(InterfaceDeclaration node, OccurenceBuilder occurencesBuilder) {
        InterfaceScopeImpl classScope = ModelElementFactory.create(InterfaceDeclarationInfo.create(node), this);
        setCurrentScope(classScope);
        occurencesBuilder.prepare(node,classScope);
        return classScope;
    }

     MethodScope build(MethodDeclaration node, OccurenceBuilder occurencesBuilder) {
        MethodScopeImpl methodScope = ModelElementFactory.create(MethodDeclarationInfo.create(getProgram(),node), this);
        setCurrentScope(methodScope);
        occurencesBuilder.prepare(node, methodScope);
        return methodScope;
    }

    void reset() {
        if (!currentScope.empty()) {
            ScopeImpl createdScope = currentScope.pop();
            if (createdScope instanceof NamespaceScopeImpl) {
                namespaceScope = defaultNamespaceScope;
            } 
        }
    }

    /**
     * @return the fileScope
     */
    FileScopeImpl getFileScope() {
        return fileScope;
    }

    NamespaceScopeImpl getCurrentNameSpace() {
        return namespaceScope;
    }

    /**
     * @return the currentScope or null
     */
    ScopeImpl getCurrentScope() {
        return currentScope.isEmpty() ? null : currentScope.peek();
    }

    /**
     * @param currentScope the currentScope to set
     */
    void setCurrentScope(ScopeImpl scope) {
        if (scope instanceof NamespaceScopeImpl) {
            namespaceScope = (NamespaceScopeImpl) scope;
        }
        this.currentScope.push(scope);
    }

    /**
     * @return the program
     */
    Program getProgram() {
        assert program != null;
        return program;
    }

    void setProgram(Program program) {
        this.program = program;
        this.defaultNamespaceScope.setBlockRange(program);
    }

    /**
     * @return the vars
     */
    Map<VariableContainerImpl, Map<String, VariableNameImpl>> getVars() {
        return vars;
    }
}
