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

import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.nodes.ClassConstantDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.ClassDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.IncludeInfo;
import org.netbeans.modules.php.editor.model.nodes.InterfaceDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.MethodDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.NamespaceDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.SingleFieldDeclarationInfo;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Radek Matous
 */
class ModelElementFactory {

    private ModelElementFactory(){};

    static NamespaceScopeImpl create(NamespaceDeclarationInfo nodeInfo, ModelBuilder context) {
        NamespaceScopeImpl namespaceScope = new NamespaceScopeImpl(context.getFileScope(), nodeInfo);
        return namespaceScope;
    }

    @CheckForNull
    static IncludeElementImpl create(IncludeInfo info, ModelBuilder context) {
        return new IncludeElementImpl(context.getCurrentScope(), info);
    }

    static ClassScopeImpl create(ClassDeclarationInfo nodeInfo, ModelBuilder context) {
        Scope currentScope = context.getCurrentScope();
        if (currentScope instanceof FunctionScope) {
            currentScope = currentScope.getInScope();
        }
        ClassScopeImpl clz = new ClassScopeImpl( currentScope, nodeInfo);
        return clz;
    }

    static InterfaceScopeImpl create(InterfaceDeclarationInfo nodeInfo, ModelBuilder context) {
        InterfaceScopeImpl iface = new InterfaceScopeImpl(context.getCurrentScope(), nodeInfo);
        return iface;
    }

    static MethodScopeImpl create(MethodDeclarationInfo nodeInfo, ModelBuilder context) {
        String returnType = VariousUtils.getReturnTypeFromPHPDoc(context.getProgram(), 
                nodeInfo.getOriginalNode().getFunction());

        MethodScopeImpl method = new MethodScopeImpl(context.getCurrentScope(), returnType, nodeInfo);
        return method;
    }

    static FieldElementImpl create(SingleFieldDeclarationInfo nodeInfo, ModelBuilder context) {
        String returnType = VariousUtils.getFieldTypeFromPHPDoc(context.getProgram(),nodeInfo.getOriginalNode());
        FieldElementImpl fei = new FieldElementImpl(context.getCurrentScope(), returnType, nodeInfo);
        return fei;
    }

    static ClassConstantElementImpl create(ClassConstantDeclarationInfo clsConst, ModelBuilder context) {
        //TODO: addElement(retval);
        return new ClassConstantElementImpl(context.getCurrentScope(), clsConst);
    }
}
