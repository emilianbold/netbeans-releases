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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.groovy.editor.api.elements.ast;

import groovy.lang.MetaMethod;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.groovy.editor.api.elements.common.IMethodElement;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;

public class ASTMethod extends ASTElement implements IMethodElement {

    private List<String> parameters;
    private Class clz;
    private MetaMethod method;
    private boolean GDK;
    private String methodSignature;

    
    public ASTMethod(GroovyParserResult info, ASTNode node) {
        super(info, node);
    }
    
    // We need this variant to drag the Class to which this Method belongs with us.
    // This is used in the CodeCompleter complete/document pair.
    
    public ASTMethod(GroovyParserResult info, ASTNode node, Class clz, MetaMethod method, boolean GDK) {

        super(info, node);
        this.clz = clz;
        this.method = method;
        this.GDK = GDK;
    }

    public boolean isGDK() {
        return GDK;
    }

    public MetaMethod getMethod() {
        return method;
    }
    
    public Class getClz() {
        return clz;
    }

    @Override
    public List<String> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<String>();
            for (Parameter parameter : ((MethodNode) node).getParameters()) {
                parameters.add(parameter.getName());
            }
        }
        return parameters;
    }

    @Override
    public String getSignature() {
        if (methodSignature == null) {
            StringBuilder builder = new StringBuilder(super.getSignature());
            List<String> params = getParameters();
            if (params.size() > 0) {
                builder.append("("); // NOI18N
                for (String parameter : params) {
                    builder.append(parameter);
                    builder.append(","); // NOI18N
                }
                builder.setLength(builder.length() - 1);
                builder.append(")"); // NOI18N
            }
            methodSignature = builder.toString();
        }
        return methodSignature;
    }

    @Override
    public String getName() {
        if (name == null) {
            if (node instanceof ConstructorNode) {
                name = ((ConstructorNode) node).getDeclaringClass().getNameWithoutPackage();
            } else if (node instanceof MethodNode) {
                name = ((MethodNode) node).getName();
            }

            if (name == null) {
                name = node.toString();
            }
        }
        return name;
    }

    public void setModifiers(Set<Modifier> modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public ElementKind getKind() {
        if (node instanceof ConstructorNode) {
            return ElementKind.CONSTRUCTOR;
        } else if (node instanceof MethodNode) {
            return ElementKind.METHOD;
        } else {
            return ElementKind.OTHER;
        }
    }

    @Override
    public boolean isTopLevel() {
        return false;
    }

    @Override
    public boolean isInherited() {
        return false;
    }

    @Override
    public boolean isDeprecated() {
        if (node instanceof MethodNode) {
            for (AnnotationNode annotation : ((MethodNode) node).getAnnotations()) {
                if (Deprecated.class.getName().equals(annotation.getClassNode().getName())) { // NOI18N
                    return true;
                }
            }
        }
        return false;
    }
}
