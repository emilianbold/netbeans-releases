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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.PredefinedSymbols;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.elements.ParameterElementImpl;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.nodes.MagicMethodDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.MethodDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;

/**
 * @author Radek Matous
 */
final class MethodScopeImpl extends FunctionScopeImpl implements MethodScope, VariableNameFactory {
    private String classNormName;

    //new contructors
    MethodScopeImpl(Scope inScope, String returnType, MethodDeclarationInfo nodeInfo) {
        super(inScope, nodeInfo, returnType);
        assert inScope instanceof TypeScope;
        classNormName = inScope.getNormalizedName();
    }
    MethodScopeImpl(Scope inScope, MagicMethodDeclarationInfo nodeInfo) {
        super(inScope, nodeInfo);
        assert inScope instanceof TypeScope;
        classNormName = inScope.getNormalizedName();
    }

    MethodScopeImpl(Scope inScope, BaseFunctionElement element) {
        super(inScope, element, PhpElementKind.METHOD);
        assert inScope instanceof TypeScope : inScope.getClass().toString();
        classNormName = inScope.getNormalizedName();
    }

    @Override
    public VariableNameImpl createElement(Variable node) {
        VariableNameImpl retval = new VariableNameImpl(this, node, false);
        addElement(retval);
        return retval;
    }

    @Override
    public Collection<? extends VariableName> getDeclaredVariables() {
        final Scope inScope = getInScope();
        if (inScope instanceof ClassScope) {
            ClassScope classScope = (ClassScope) inScope;
            return ModelUtils.merge(classScope.getDeclaredVariables(), super.getDeclaredVariables());
        }
        return Collections.emptyList();
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getPhpModifiers().toString()).append(" ");//NOI18N
        sb.append(super.toString());
        return sb.toString();
    }

    public boolean isMagic() {
        return PredefinedSymbols.MAGIC_METHODS.contains(getName().toLowerCase());
    }

    public boolean isConstructor() {
        return isMagic() ? getName().contains("__construct") : false;
    }

    public TypeScope getTypeScope() {
        return (ClassScope) getInScope();
    }

    @Override
    public String getNormalizedName() {
        return classNormName+super.getNormalizedName();
    }

    public String getClassSkeleton() {
        StringBuilder sb = new StringBuilder();
        sb.append(getPhpModifiers().toString()).append(" ");//NOI18N
        sb.append("function").append(" ").append(getName());//NOI18N
        sb.append("(");//NOI18N
        List<? extends ParameterElement> parameterList = getParameters();
        if (parameterList.size() > 0) {
            for (int i = 0, n = parameterList.size(); i < n; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                final ParameterElement param = parameterList.get(i);
                    if (param.hasDeclaredType()) {
                        Set<TypeResolver> types = param.getTypes();
                        if (types.size() == 1) {
                            for (TypeResolver typeResolver : types) {
                                if (typeResolver.isResolved()) {
                                    sb.append(typeResolver.getTypeName(false)).append(' ');//NOI18N
                                }
                            }
                        }
                    }

                sb.append(param.getName());
                if (!param.isMandatory()) {
                    String defaultValue = param.getDefaultValue();
                    if (defaultValue != null) {
                        sb.append(" = ").append(defaultValue); //NOI18N
                    }
                }
            }
        }

        sb.append(")");
        sb.append("{\n}");//NOI18N
        return sb.toString();
    }

    public String getInterfaceSkeleton() {
        StringBuilder sb = new StringBuilder();
        sb.append(getPhpModifiers().toString()).append(" ");//NOI18N
        sb.append("function").append(" ").append(getName());//NOI18N
        sb.append("(");//NOI18N
        List<? extends String> parameterNames = getParameterNames();
        for (int i = 0; i < parameterNames.size(); i++) {
            String param = parameterNames.get(i);
            if (i > 0) {
                sb.append(", ");//NOI18N
            }
            sb.append(param);
        }
        sb.append(")");
        sb.append(";\n");//NOI18N
        return sb.toString();
    }

    @Override
    public String getIndexSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(";");//NOI18N
        sb.append(getName()).append(";");//NOI18N
        sb.append(getSignatureLastPart());
        return sb.toString();
    }

    @Override
    public String getConstructorIndexSignature() {
        StringBuilder sb = new StringBuilder();
        final String typeName = getInScope().getName();
        sb.append(typeName.toLowerCase()).append(";");//NOI18N
        sb.append(typeName).append(";");//NOI18N
        sb.append(getSignatureLastPart());
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(this);
        QualifiedName qualifiedName = namespaceScope.getQualifiedName();
        sb.append(qualifiedName.toString()).append(";");//NOI18N
        return sb.toString();
    }

    private String getSignatureLastPart() {
        StringBuilder sb = new StringBuilder();
        sb.append(getOffset()).append(";"); //NOI18N
        List<? extends ParameterElement> parameters = getParameters();
        for (int idx = 0; idx < parameters.size(); idx++) {
            ParameterElementImpl parameter = (ParameterElementImpl) parameters.get(idx);
            if (idx > 0) {
                sb.append(','); //NOI18N
            }
            sb.append(parameter.getSignature());
        }
        sb.append(";"); //NOI18N
        if (returnType != null && !PredefinedSymbols.MIXED_TYPE.equalsIgnoreCase(returnType)) {
            sb.append(returnType);
        }
        sb.append(";"); //NOI18N
        sb.append(getPhpModifiers().toFlags()).append(";");
        return sb.toString();
    }
}
