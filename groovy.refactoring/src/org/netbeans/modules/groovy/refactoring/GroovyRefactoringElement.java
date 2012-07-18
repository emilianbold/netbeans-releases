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
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.AstUtilities;
import org.netbeans.modules.groovy.editor.api.elements.ast.ASTElement;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Janicek <mjanicek@netbeans.org>
 */
public class GroovyRefactoringElement extends ASTElement {

    private final FileObject fileObject;
    private final AstPath path;

    
    public GroovyRefactoringElement(GroovyParserResult info, ModuleNode root, ASTNode node, FileObject fileObject) {
        super(info, node);
        this.fileObject = fileObject;
        this.path = new AstPath(root, node.getLineNumber(), node.getColumnNumber());
    }

    @Override
    public FileObject getFileObject() {
        return fileObject;
    }

    @Override
    public String getName() {
        if (node instanceof ClassNode) {
            return ((ClassNode) node).getNameWithoutPackage();
        } else if (node instanceof MethodNode) {
            return ((MethodNode) node).getName();
        } else if (node instanceof FieldNode) {
            return ((FieldNode) node).getName();
        } else if (node instanceof PropertyNode) {
            return ((PropertyNode) node).getName();
        } else if (node instanceof VariableExpression) {
            return ((VariableExpression) node).getName();
        }
        return "Not implemented yet - GroovyRefactoringElement.getName() needs to be improve!\n";
    }

    @Override
    public ElementKind getKind() {
        if (node instanceof ClassNode) {
            return ElementKind.CLASS;
        } else if (node instanceof MethodNode) {
            return ElementKind.METHOD;
        } else if (node instanceof FieldNode) {
            return ElementKind.FIELD;
        } else if (node instanceof PropertyNode) {
            return ElementKind.PROPERTY;
        } else if (node instanceof VariableExpression) {
            return ElementKind.VARIABLE;
        }
        return super.getKind();
    }

    public ClassNode getDeclaringClass() {
        if (node instanceof ClassNode) {
            return (ClassNode) node;
        } else if (node instanceof MethodNode) {
            return ((MethodNode) node).getDeclaringClass();
        } else if (node instanceof FieldNode) {
            return ((FieldNode) node).getDeclaringClass();
        } else if (node instanceof PropertyNode) {
            return ((PropertyNode) node).getDeclaringClass();
        } else if (node instanceof VariableExpression) {
            return ((VariableExpression) node).getDeclaringClass();
        }
        throw new IllegalStateException("Something isn't implemented yet - see GroovyRefactoringElement.getDeclaringClass() ..looks like the type: " + node.getClass().getName() + "isn't handled at the moment!");
    }

    @Override
    public String getSignature() {
        if (node instanceof MethodNode) {
            MethodNode method = ((MethodNode) node);
            StringBuilder builder = new StringBuilder(super.getSignature());
            Parameter[] params = method.getParameters();
            if (params.length > 0) {
                builder.append("("); // NOI18N
                for (Parameter param : params) {
                    builder.append(getTypeName(param.getType()));
                    builder.append(" "); // NOI18N
                    builder.append(param.getName());
                    builder.append(","); // NOI18N
                }
                builder.setLength(builder.length() - 1);
                builder.append(")"); // NOI18N
            }
            String returnType = method.getReturnType().getNameWithoutPackage();
            builder.append(" : "); // NOI18N
            builder.append(returnType);
            
            return builder.toString();
        }
        return super.getSignature();
    }

    public String getDefClass() {
        try {
            return getDeclaringClass().getNameWithoutPackage();
        } catch (IllegalStateException ex) {
            return AstUtilities.getFqnName(path);
        }
    }

    public String getType() {
        ClassNode type;
        if (node instanceof ClassNode) {
            type = ((ClassNode) node);
        } else if (node instanceof FieldNode) {
            type = ((FieldNode) node).getType();
        } else if (node instanceof PropertyNode) {
            type = ((PropertyNode) node).getType();
        } else if (node instanceof VariableExpression) {
            type = ((VariableExpression) node).getType();
        } else {
            return "Not implemented yet - GroovyRefactoringElement.getType() needs to be improve!";
        }
        return getTypeName(type);
    }

    private String getTypeName(ClassNode type) {
        String typeName = type.getNameWithoutPackage();

        // This will happened with all primitive type arrays, e.g. 'double [] x'
        if (typeName.startsWith("[")) {
            typeName = type.getComponentType().getNameWithoutPackage() + "[]";
        }
        return typeName;
    }
}
