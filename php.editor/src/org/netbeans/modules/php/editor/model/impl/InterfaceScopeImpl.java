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

import org.netbeans.modules.php.editor.index.IndexedInterface;
import org.netbeans.modules.php.editor.model.*;
import java.util.List;
import org.netbeans.modules.php.editor.model.nodes.InterfaceDeclarationInfo;

/**
 *
 * @author Radek Matous
 */
final class InterfaceScopeImpl extends TypeScopeImpl implements InterfaceScope {
    InterfaceScopeImpl(ScopeImpl inScope, InterfaceDeclarationInfo nodeInfo) {
        super(inScope, nodeInfo);
    }

    InterfaceScopeImpl(IndexScopeImpl inScope, IndexedInterface indexedIface) {
        //TODO: in idx is no info about ifaces
        super(inScope, indexedIface);
    }

    @Override
    void checkModifiersAssert() {
        assert getPhpModifiers() != null;
        assert getPhpModifiers().isPublic();
        assert !getPhpModifiers().isFinal();
    }

    @Override
    void checkScopeAssert() {
        assert getInScope() != null;
        assert getInScope() instanceof FileScope;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        List<? extends InterfaceScopeImpl> implementedInterfaces = getInterfaces();
        if (implementedInterfaces.size() > 0) {
            sb.append(" implements ");
            for (InterfaceScopeImpl interfaceScope : implementedInterfaces) {
                sb.append(interfaceScope.getName()).append(" ");
            }
        }
        return sb.toString();
    }

    public List<? extends MethodScope> getAllInheritedMethods() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<? extends MethodScope> getInheritedMethods(String queryName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
