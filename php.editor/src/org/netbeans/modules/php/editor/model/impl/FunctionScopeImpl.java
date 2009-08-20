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
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.model.*;
import java.util.Collections;
import java.util.List;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.PredefinedSymbols;
import org.netbeans.modules.php.editor.model.nodes.FunctionDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.MethodDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;

/**
 *
 * @author Radek Matous
 */
class FunctionScopeImpl extends ScopeImpl implements FunctionScope, VariableContainerImpl {
    private List<? extends Parameter> paremeters;
    String returnType;

    //new contructors
    FunctionScopeImpl(Scope inScope, FunctionDeclarationInfo info, String returnType) {
        super(inScope, info, new PhpModifiers(PhpModifiers.PUBLIC), info.getOriginalNode().getBody());
        this.paremeters = info.getParameters();
        this.returnType = returnType;
    }
    protected FunctionScopeImpl(Scope inScope, MethodDeclarationInfo info, String returnType) {
        super(inScope, info, info.getAccessModifiers(), info.getOriginalNode().getFunction().getBody());
        this.paremeters = info.getParameters();
        this.returnType = returnType;
    }

    FunctionScopeImpl(Scope inScope, IndexedFunction indexedFunction) {
        this(inScope, indexedFunction, PhpKind.FUNCTION);
    }

    protected FunctionScopeImpl(Scope inScope, final IndexedFunction element, PhpKind kind) {
        super(inScope, element, kind);
        List<Parameter> parameters = new ArrayList<Parameter>();

        final List<String> pams = element.getParameters();
        final int  mandatoryArgSize = pams.size() - element.getOptionalArgs().length;

        for (int i = 0; i < pams.size(); i++) {
            final String paramName = pams.get(i);
            final int idx = i;
            parameters.add(new Parameter() {
                public String getName() {
                    return paramName;
                }

                public String getDefaultValue() {
                    //TODO: evaluate def.values not in index
                    return "";//NOI18N
                }

                public boolean isMandatory() {
                    return idx < mandatoryArgSize;
                }

                //TODO: not implemented yet
                public List<QualifiedName> getTypes() {
                    return Collections.emptyList();
                }

                public OffsetRange getOffsetRange() {
                    return new OffsetRange(element.getOffset(), element.getOffset()+paramName.length());
                }
            });

        }
        this.paremeters = parameters;
        this.returnType =  element.getReturnType();
    }

    //old contructors
    

    public final Collection<? extends TypeScope> getReturnTypes() {
        Collection<TypeScope> retval = Collections.<TypeScope>emptyList();
        if (returnType != null && returnType.length() > 0) {
            retval = new ArrayList<TypeScope>();
            for (String typeName : returnType.split("\\|")) {
                retval.addAll(CachingSupport.getTypes(typeName, this));
            }
        }
        return retval;
    }

    @NonNull
    public List<? extends String> getParameterNames() {
        assert paremeters != null;
        List<String> parameterNames = new ArrayList<String>();
        for (Parameter parameter : paremeters) {
            parameterNames.add(parameter.getName());
        }
        return parameterNames;
    }

    @NonNull
    public List<? extends Parameter> getParameters() {
        return paremeters;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Collection<? extends TypeScope> returnTypes = getReturnTypes();
        sb.append('[');
        for (TypeScope typeScope : returnTypes) {
            if (sb.length() == 1) {
                sb.append("|");
            }
            sb.append(typeScope.getName());
        }
        sb.append("] ");
        sb.append(super.toString()).append("(");
        List<? extends String> parameters = getParameterNames();
        for (int i = 0; i < parameters.size(); i++) {
            String param = parameters.get(i);
            if (i > 0) sb.append(",");
            sb.append(param);
        }
        sb.append(")");

        return sb.toString();
    }

    public Collection<? extends VariableName> getDeclaredVariables() {
        return getAllVariablesImpl();
    }

    public Collection<? extends VariableName> findDeclaredVariables(String... queryName) {
        return getVariablesImpl(queryName);
    }

    public Collection<? extends VariableName> findDeclaredVariables(QuerySupport.Kind nameKind, String... queryName) {
        return findDeclaredVariables(nameKind, queryName);
    }

    public Collection<? extends VariableName> getAllVariablesImpl() {
        return getVariablesImpl();
    }

    public Collection<? extends VariableName> getVariablesImpl(String... queryName) {
        return getVariablesImpl(QuerySupport.Kind.EXACT, queryName);
    }

    public Collection<? extends VariableName> getVariablesImpl(final QuerySupport.Kind nameKind, final String... queryName) {
        return filter(getElements(), new ElementFilter() {
            public boolean isAccepted(ModelElement element) {
                return element.getPhpKind().equals(PhpKind.VARIABLE)  &&
                        (queryName.length == 0 || nameKindMatch(element.getName(), nameKind, queryName));
            }
        });
    }
    public VariableNameImpl createElement(Variable node) {
        VariableNameImpl retval = new VariableNameImpl(this, node, false);
        addElement(retval);
        return retval;
    }
        
    @Override
    public String getIndexSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(";");//NOI18N
        sb.append(getName()).append(";");//NOI18N
        StringBuilder defaultArgs = new StringBuilder();
        List<? extends Parameter> parameters = getParameters();
        for (int paramIdx = 0; paramIdx < parameters.size(); paramIdx++) {
            Parameter parameter = parameters.get(paramIdx);
            if (paramIdx > 0) { sb.append(","); }//NOI18N
            sb.append(parameter.getName());
            if (parameter.getDefaultValue() != null) {
                if (defaultArgs.length() > 0) { defaultArgs.append(","); }//NOI18N
                defaultArgs.append(paramIdx);
            }
        }
        sb.append(";");//NOI18N
        sb.append(getOffset()).append(";");//NOI18N
        sb.append(defaultArgs).append(";");//NOI18N
        if (returnType != null && !PredefinedSymbols.MIXED_TYPE.equalsIgnoreCase(returnType)) {
            sb.append(returnType);
        }
        sb.append(";");//NOI18N
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(this);
        QualifiedName qualifiedName = namespaceScope.getQualifiedName();
        sb.append(qualifiedName.toString()).append(";");//NOI18N
        return sb.toString();
    }

    @Override
    public QualifiedName getNamespaceName() {
        if (indexedElement instanceof IndexedFunction) {
            IndexedFunction indexedFunction = (IndexedFunction)indexedElement;
            return QualifiedName.create(indexedFunction.getNamespaceName());
        }
        return super.getNamespaceName();
    }
}
