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
package org.netbeans.modules.groovy.refactoring;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.groovy.editor.api.ElementUtils;
import org.netbeans.modules.groovy.editor.api.elements.ast.ASTElement;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Janicek <mjanicek@netbeans.org>
 */
public class GroovyRefactoringElement extends ASTElement {

    private final FileObject fileObject;
    private final ElementKind refactoringKind;

    
    public GroovyRefactoringElement(GroovyParserResult info, ASTNode node, FileObject fo, ElementKind kind) {
        super(info, node);
        this.fileObject = fo;
        this.refactoringKind = kind;
    }

    @Override
    public FileObject getFileObject() {
        return fileObject;
    }

    @Override
    public ElementKind getKind() {
        return refactoringKind;
    }

    /**
     * Returns the name of the refactoring element. (e.g. for field declaration
     * "private GalacticMaster master" the method return "master")
     *
     * @return name of the refactoring element
     */
    @Override
    public String getName() {
        return ElementUtils.getNameWithoutPackage(node);
    }

    /**
     * Returns type of the refactoring element. (e.g. for field declaration 
     * "private GalacticMaster master" the method return "GalacticMaster")
     * 
     * @return type of the refactoring element
     */
    public final String getTypeName() {
        return ElementUtils.getTypeNameWithoutPackage(node);
    }

    public final ClassNode getDeclaringClass() {
        return ElementUtils.getDeclaringClass(node);
    }

    public final String getDeclaringClassName() {
        return ElementUtils.getDeclaringClassName(node);
    }

    public final String getDeclaringClassNameWithoutPackage() {
        return ElementUtils.getDeclaringClassNameWithoutPackage(node);
    }

    @Override
    public String getSignature() {
        if (node instanceof MethodNode) {
            return getMethodSignature(((MethodNode) node));
        } else if (node instanceof ConstructorCallExpression) {
            return getConstructorSignature(((ConstructorCallExpression) node));
        }
        return super.getSignature();
    }

    private String getMethodSignature(MethodNode method) {
        StringBuilder builder = new StringBuilder(super.getSignature());
        Parameter[] params = method.getParameters();

        builder.append("("); // NOI18N
        if (params.length > 0) {
            for (Parameter param : params) {
                builder.append(ElementUtils.getType(param.getType()).getNameWithoutPackage());
                builder.append(" "); // NOI18N
                builder.append(param.getName());
                builder.append(","); // NOI18N
            }
            builder.setLength(builder.length() - 1);
        }
        builder.append(")"); // NOI18N

        // No return type for constructors
        if (!"<init>".equals(method.getName())) { // NOI18N
            String returnType = method.getReturnType().getNameWithoutPackage();
            builder.append(" : "); // NOI18N
            builder.append(returnType);
        }

        return builder.toString();
    }

    private String getConstructorSignature(ConstructorCallExpression constructorCall) {
        StringBuilder builder = new StringBuilder();
        ClassNode type = constructorCall.getType();
        Expression arguments = constructorCall.getArguments();

        builder.append(type.getNameWithoutPackage());
        builder.append("("); // NOI18N

        if (arguments instanceof ArgumentListExpression) {
            ArgumentListExpression argumentList = ((ArgumentListExpression) arguments);
            if (argumentList.getExpressions().size() > 0) {
                for (Expression argument : argumentList.getExpressions()) {
                    builder.append(ElementUtils.getTypeNameWithoutPackage(argument.getType()));
                    builder.append(" "); // NOI18N
                    builder.append(argument.getText());
                    builder.append(","); // NOI18N
                }
                builder.setLength(builder.length() - 1);
            }
        }
        builder.append(")"); // NOI18N

        return builder.toString();
    }
}
