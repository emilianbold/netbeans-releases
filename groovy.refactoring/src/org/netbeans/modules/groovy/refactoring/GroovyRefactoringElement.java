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
package org.netbeans.modules.groovy.refactoring;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.netbeans.modules.groovy.editor.AstPath;
import org.netbeans.modules.groovy.editor.AstUtilities;
import org.netbeans.modules.groovy.editor.elements.AstElement;
import org.netbeans.modules.gsf.api.ElementKind;
import org.openide.filesystems.FileObject;

/**
 *
 * @author martin
 */
public class GroovyRefactoringElement extends AstElement {

    private final ModuleNode root;
    private final FileObject fileObject;
    private final AstPath path;

    public GroovyRefactoringElement(ModuleNode root, ASTNode node, FileObject fileObject) {
        super(node);
        this.root = root;
        this.fileObject = fileObject;
        this.path = new AstPath(root, node.getLineNumber(), node.getColumnNumber());
    }

    public String getFindName() {
        if (node instanceof FieldNode) {
            FieldNode field = (FieldNode) node;
            return field.getType().getNameWithoutPackage();
        } else if (node instanceof VariableExpression) {
            VariableExpression variable = (VariableExpression) node;
            return variable.getType().getNameWithoutPackage();
        } else {
            return this.getNode().getText();
        }
    }

    @Override
    public String getName() {
        if (node instanceof FieldNode) {
            FieldNode field = (FieldNode) node;
            return field.getName();
        } else if (node instanceof VariableExpression) {
            VariableExpression variable = (VariableExpression) node;
            return variable.getName();
        } else {
            return this.getNode().getText();
        }
    }

    @Override
    public FileObject getFileObject() {
        return fileObject;
    }

    public Object getSimpleName() {
        if (node instanceof FieldNode) {
            return ((FieldNode) node).getName();
        } else if (node instanceof MethodNode) {
            return ((MethodNode) node).getName();
        } else if (node instanceof ClassNode) {
            return ((ClassNode) node).getNameWithoutPackage();
        } else {
            return getName();
        }
    }

    public String getDefClass() {
        return AstUtilities.getFqnName(path);
    }

    @Override
    public ElementKind getKind() {
        if (node instanceof FieldNode) {
            return ElementKind.FIELD;
        } else if (node instanceof MethodNode) {
            return ElementKind.METHOD;
        } else if (node instanceof ClassNode) {
            return ElementKind.CLASS;
        }
        return super.getKind();
    }

}
