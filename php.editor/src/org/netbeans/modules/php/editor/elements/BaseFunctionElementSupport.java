/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.elements;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement.PrintAs;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.ParameterElement.OutputType;
import org.netbeans.modules.php.editor.api.elements.TypeNameResolver;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;

/**
 * @author Radek Matous
 */
public class BaseFunctionElementSupport  {
    private final List<ParameterElement> parameters;
    private final Set<TypeResolver> returnTypes;

    protected BaseFunctionElementSupport(
            final List<ParameterElement> parameters,
            final Set<TypeResolver> returnTypes) {

        this.parameters = parameters;
        this.returnTypes = returnTypes;
    }

    public final List<ParameterElement> getParameters() {
        return parameters;
    }

    public final Collection<TypeResolver> getReturnTypes() {
        return returnTypes;
    }


    public final String asString(PrintAs as, BaseFunctionElement element, TypeNameResolver typeNameResolver) {
        StringBuilder template = new StringBuilder();
        switch (as) {
            case NameAndParamsDeclaration:
                template.append(" ").append(element.getName()).append("("); //NOI18N
                template.append(parameters2String(getParameters(), OutputType.SHORTEN_DECLARATION, typeNameResolver));
                template.append(")"); //NOI18N
                break;
            case NameAndParamsInvocation:
                template.append(" ").append(element.getName()).append("("); //NOI18N
                template.append(parameters2String(getParameters(), OutputType.SIMPLE_NAME, typeNameResolver));
                template.append(")"); //NOI18N
                break;
            case DeclarationWithoutBody:
                final String modifiers = element.getPhpModifiers().toString();
                if (modifiers.length() > 0) {
                    template.append(modifiers).append(" ");//NOI18N
                }
                template.append("function");//NOI18N
                template.append(asString(PrintAs.NameAndParamsDeclaration, element, typeNameResolver));
                break;
            case DeclarationWithEmptyBody:
                template.append(asString(PrintAs.DeclarationWithoutBody, element, typeNameResolver));
                template.append("{\n}");//NOI18N
                break;
            case DeclarationWithParentCallInBody:
                template.append(asString(PrintAs.DeclarationWithoutBody, element, typeNameResolver));
                Collection<TypeResolver> returns = getReturnTypes();
                String methdodInvocation = asString(PrintAs.NameAndParamsInvocation, element, typeNameResolver);
                if (methdodInvocation.startsWith(" ")) {
                    methdodInvocation = methdodInvocation.substring(1);
                }
                if (returns.size() > 0) {
                    template.append(String.format("{%nreturn parent::%s;%n}", methdodInvocation));//NOI18N
                } else {
                    template.append(String.format("{%nparent::%s;%n}", methdodInvocation));//NOI18N
                }
                break;
            case ReturnSemiTypes:
                for (TypeResolver typeResolver : getReturnTypes()) {
                    if (typeResolver.isResolved()) {
                        QualifiedName typeName = typeResolver.getTypeName(false);
                        if (typeName != null) {
                            if (template.length() > 0) {
                                template.append("|"); //NOI18N
                            }
                            template.append(typeNameResolver.resolve(typeName).toString());
                        }
                    } else {
                        String typeName = typeResolver.getRawTypeName();
                        if (typeName != null) {
                            if (template.length() > 0) {
                                template.append("|"); //NOI18N
                            }
                            template.append(typeName);
                        }
                    }
                }
                break;
            case ReturnTypes:
                for (TypeResolver typeResolver : getReturnTypes()) {
                    if (typeResolver.isResolved()) {
                        QualifiedName typeName = typeResolver.getTypeName(false);
                        if (typeName != null) {
                            if (template.length() > 0) {
                                template.append("|"); //NOI18N
                            }
                            template.append(typeNameResolver.resolve(typeName).toString());
                        }
                    }
                }
                break;
        }
        return template.toString();
    }

    private static String parameters2String(final List<ParameterElement> parameterList, OutputType stringOutputType, TypeNameResolver typeNameResolver) {
        StringBuilder template = new StringBuilder();
        if (parameterList.size() > 0) {
            for (int i = 0, n = parameterList.size(); i < n; i++) {
                StringBuilder paramSb = new StringBuilder();
                if (i > 0) {
                    paramSb.append(", "); //NOI18N
                }
                final ParameterElement param = parameterList.get(i);
                paramSb.append(param.asString(stringOutputType, typeNameResolver));
                template.append(paramSb);
            }
        }
        return template.toString();
    }

}
