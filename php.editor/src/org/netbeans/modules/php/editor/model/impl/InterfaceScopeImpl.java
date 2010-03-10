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

import org.netbeans.modules.php.editor.api.QualifiedName;
import java.util.Collection;
import java.util.HashSet;
import org.netbeans.modules.php.editor.model.*;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.model.nodes.InterfaceDeclarationInfo;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;


/**
 *
 * @author Radek Matous
 */
class InterfaceScopeImpl extends TypeScopeImpl implements InterfaceScope {
    InterfaceScopeImpl(Scope inScope, InterfaceDeclarationInfo nodeInfo) {
        super(inScope, nodeInfo);
    }

    InterfaceScopeImpl(IndexScope inScope, InterfaceElement indexedIface) {
        //TODO: in idx is no info about ifaces
        super(inScope, indexedIface);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        List<? extends InterfaceScope> implementedInterfaces = getSuperInterfaceScopes();
        if (implementedInterfaces.size() > 0) {
            sb.append(" implements ");
            for (InterfaceScope interfaceScope : implementedInterfaces) {
                sb.append(interfaceScope.getName()).append(" ");
            }
        }
        return sb.toString();
    }
    @Override
    public Collection<? extends MethodScope> getInheritedMethods() {
        Set<MethodScope> allMethods = new HashSet<MethodScope>();
        IndexScope indexScope = ModelUtils.getIndexScope(this);
        ElementQuery.Index index = indexScope.getIndex();
        Set<InterfaceScope> interfaceScopes = new HashSet<InterfaceScope>();
        interfaceScopes.addAll(getSuperInterfaceScopes());
        for (InterfaceScope iface : interfaceScopes) {
            Collection<MethodElement> indexedFunctions = index.getInheritedMethods(iface);
            for (MethodElement classMember : indexedFunctions) {
                MethodElement indexedFunction = classMember;
                allMethods.add(new MethodScopeImpl(iface, indexedFunction));
            }
        }
        return allMethods;
    }

    @Override
    public final Collection<? extends ClassConstantElement> getInheritedConstants() {
        Set<ClassConstantElement> allConstants = new HashSet<ClassConstantElement>();
        IndexScope indexScope = ModelUtils.getIndexScope(this);
        ElementQuery.Index index = indexScope.getIndex();
        Set<InterfaceScope> interfaceScopes = new HashSet<InterfaceScope>();
        interfaceScopes.addAll(getSuperInterfaceScopes());
        for (InterfaceScope iface : interfaceScopes) {
            Collection<TypeConstantElement> indexedConstants = index.getInheritedTypeConstants(iface);
            for (TypeConstantElement classMember : indexedConstants) {
                TypeConstantElement indexedFunction = classMember;
                allConstants.add(new ClassConstantElementImpl(iface, indexedFunction));
            }
        }
        return allConstants;
    }

    public final Collection<? extends MethodScope> getMethods() {
        Set<MethodScope> allMethods = new HashSet<MethodScope>();
        allMethods.addAll(getDeclaredMethods());
        allMethods.addAll(getInheritedMethods());
        return allMethods;
    }

    @Override
    public String getIndexSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(";");//NOI18N
        sb.append(getName()).append(";");//NOI18N
        sb.append(getOffset()).append(";");//NOI18N
        List<? extends String> superInterfaces = getSuperInterfaceNames();
        for (int i = 0; i < superInterfaces.size(); i++) {
            String iface = superInterfaces.get(i);
            if (i > 0) {
                sb.append(",");
            }
            sb.append(iface);
        }
        sb.append(";");//NOI18N
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(this);
        QualifiedName qualifiedName = namespaceScope.getQualifiedName();
        sb.append(qualifiedName.toString()).append(";");//NOI18N
        return sb.toString();
    }

    @Override
    public QualifiedName getNamespaceName() {
        if (indexedElement instanceof InterfaceElement) {
            InterfaceElement indexedInterface = (InterfaceElement)indexedElement;
            return indexedInterface.getNamespaceName();
        }
        return super.getNamespaceName();
    }
}
