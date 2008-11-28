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

import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.model.*;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.openide.filesystems.FileObject;

import org.netbeans.modules.gsf.api.annotations.NonNull;
import org.netbeans.modules.php.editor.model.nodes.FunctionDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.MethodDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.openide.util.Union2;

/**
 *
 * @author Radek Matous
 */
class FunctionScopeImpl extends ScopeImpl implements FunctionScope, VariableContainerImpl {
    private List<? extends String> paremeters;
    private String returnType;

    //new contructors
    FunctionScopeImpl(ScopeImpl inScope, FunctionDeclarationInfo info, String returnType) {
        super(inScope, info, new PhpModifiers(PhpModifiers.PUBLIC), info.getOriginalNode().getBody());
        this.paremeters = info.getParameters();
        this.returnType = returnType;
    }
    protected FunctionScopeImpl(ScopeImpl inScope, MethodDeclarationInfo info, String returnType) {
        super(inScope, info, info.getAccessModifiers(), info.getOriginalNode().getFunction().getBody());
        this.paremeters = info.getParameters();
        this.returnType = returnType;
    }

    FunctionScopeImpl(ScopeImpl inScope, IndexedFunction indexedFunction) {
        this(inScope, indexedFunction, PhpKind.FUNCTION);
    }

    protected FunctionScopeImpl(ScopeImpl inScope, IndexedFunction element, PhpKind kind) {
        super(inScope, element, kind);
        this.paremeters = element.getParameters();
        this.returnType =  element.getReturnType();
    }

    //old contructors

    public VariableNameImpl createElement(Program program, Variable node) {
        VariableNameImpl retval = new VariableNameImpl(this, program, node);
        addElement(retval);
        return retval;
    }
    
    FunctionScopeImpl(ScopeImpl inScope, String name, Union2<String/*url*/,FileObject> file,
            OffsetRange offsetRange, PhpKind kind, List<? extends String> paremeters, PhpModifiers modifiers, String returnType) {
        super(inScope, name, file, offsetRange, kind, modifiers);
        this.paremeters = paremeters;
        this.returnType = returnType;
        assert paremeters != null;
    }

    public final List<? extends TypeScope> getReturnTypes() {
        return (returnType != null && returnType.length() > 0) ?
            CachedModelSupport.getTypes(returnType.split("\\|")[0], this) :
            Collections.<TypeScopeImpl>emptyList();
    }

    @NonNull
    public List<? extends String> getParameters() {
        assert paremeters != null;
        return paremeters;
    }

    @Override
    void checkModifiersAssert() {
        assert getPhpModifiers() != null;
        assert getPhpModifiers().isPublic();
    }

    @Override
    void checkScopeAssert() {
        assert getInScope() != null;
        assert getInScope() instanceof FileScope;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        List<? extends TypeScope> returnTypes = getReturnTypes();
        sb.append('[');
        for (int i = 0; i < returnTypes.size(); i++) {
            TypeScope typeScope = returnTypes.get(i);
            sb.append(typeScope.getName());
            if (i+1 < returnTypes.size()) sb.append("|");
        }
        sb.append("] ");
        sb.append(super.toString()).append("(");
        List<? extends String> parameters = getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            String param = parameters.get(i);
            if (i > 0) sb.append(",");
            sb.append(param);
        }
        sb.append(")");

        return sb.toString();
    }

    public List<? extends VariableName> getAllVariables() {
        return getAllVariablesImpl();
    }

    public List<? extends VariableName> getVariables(String... queryName) {
        return getVariablesImpl(queryName);
    }

    public List<? extends VariableName> getVariables(NameKind nameKind, String... queryName) {
        return getVariables(nameKind, queryName);
    }

    public List<? extends VariableNameImpl> getAllVariablesImpl() {
        return getVariablesImpl();
    }

    public List<? extends VariableNameImpl> getVariablesImpl(String... queryName) {
        return getVariablesImpl(NameKind.EXACT_NAME, queryName);
    }

    public List<? extends VariableNameImpl> getVariablesImpl(final NameKind nameKind, final String... queryName) {
        return filter(getElements(), new ElementFilter() {
            public boolean isAccepted(ModelElementImpl element) {
                return element.getPhpKind().equals(PhpKind.VARIABLE)  &&
                        (queryName.length == 0 || nameKindMatch(element.getName(), nameKind, queryName));
            }
        });
    }
}
