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

import org.netbeans.modules.groovy.refactoring.utils.ElementUtils;
import org.codehaus.groovy.ast.*;
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
    public ElementKind getKind() {
        return ElementUtils.getKind(node);
    }

    @Override
    public String getName() {
        return ElementUtils.getNameWithoutPackage(node);
    }

    public final String getType() {
        return ElementUtils.getTypeNameWithoutPackage(node);
    }

    public final String getFQN() {
        return AstUtilities.getFqnName(path);
    }

    public final ClassNode getDeclaringClass() {
        return ElementUtils.getDeclaringClass(node);
    }

    public final String getDeclaratingClassName() {
        return ElementUtils.getDeclaratingClassName(node);
    }

    @Override
    public String getSignature() {
        if (node instanceof MethodNode) {
            MethodNode method = ((MethodNode) node);
            StringBuilder builder = new StringBuilder(super.getSignature());
            Parameter[] params = method.getParameters();

            builder.append("("); // NOI18N
            if (params.length > 0) {
                for (Parameter param : params) {
                    builder.append(ElementUtils.getTypeNameWithoutPackage(param.getType()));
                    builder.append(" "); // NOI18N
                    builder.append(param.getName());
                    builder.append(","); // NOI18N
                }
                builder.setLength(builder.length() - 1);
            }
            builder.append(")"); // NOI18N

            String returnType = method.getReturnType().getNameWithoutPackage();
            builder.append(" : "); // NOI18N
            builder.append(returnType);
            
            return builder.toString();
        }
        return super.getSignature();
    }
}
