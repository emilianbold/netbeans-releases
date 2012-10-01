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
package org.netbeans.modules.php.editor.verification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.QualifiedNameKind;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.GotoLabel;
import org.netbeans.modules.php.editor.parser.astnodes.GotoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.TypeDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultTreePathVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class CheckPHPVersionVisitor extends DefaultTreePathVisitor {
    private FileObject fobj;
    private ArrayList<PHPVerificationError> errors = new ArrayList<PHPVerificationError>();

    public CheckPHPVersionVisitor(FileObject fobj) {
        this.fobj = fobj;
    }

    public static  boolean appliesTo(FileObject fobj) {
        return CodeUtils.isPhp_52(fobj);
    }

    @Override
    public void visit(NamespaceDeclaration declaration) {
        final NamespaceName name = declaration.getName();
        if (name != null) {
            createError(declaration.getStartOffset(), name.getEndOffset());
        } else {
            createError(declaration);
        }
    }

    @Override
    public void visit(LambdaFunctionDeclaration declaration) {
        createError(declaration);
    }

    @Override
    public void visit(GotoLabel label) {
        createError(label);
    }

    @Override
    public void visit(ConstantDeclaration statement) {
        for (ASTNode node : getPath()) {
            if (node instanceof TypeDeclaration) {
                return;
            }
        }
        createError(statement);
    }

    @Override
    public void visit(GotoStatement statement) {
        createError(statement);
    }

    @Override
    public void visit(UseStatement statement) {
        createError(statement);
    }

    @Override
    public void visit(StaticMethodInvocation node) {
        if (node.getClassName() instanceof Variable) {
            createError(node.getClassName());
        } else {
            super.visit(node);
        }
    }

    @Override
    public void visit(NamespaceName namespaceName) {

        QualifiedName qname = QualifiedName.create(namespaceName);
        if (qname.getKind() != QualifiedNameKind.UNQUALIFIED){
            createError(namespaceName);
        }
    }

    public Collection<PHPVerificationError> getErrors(){
        return Collections.unmodifiableCollection(errors);
    }

    private  void createError(int startOffset, int endOffset){

        PHPVerificationError error = new PHP53VersionError(fobj, startOffset, endOffset);
        errors.add(error);
    }

    private void createError(ASTNode node){
        createError(node.getStartOffset(), node.getEndOffset());
        super.visit(node);
    }

    private static class PHP53VersionError extends PHPVerificationError {

        private static final String KEY = "php.ver"; //NOI18N

        public PHP53VersionError(FileObject fileObject, int startOffset, int endOffset) {
            super(fileObject, startOffset, endOffset);
        }

        @Override
        @Messages("CheckPHPVerDesc=Detect language features not compatible with PHP version indicated in project settings")
        public String getDescription() {
            return Bundle.CheckPHPVerDesc();
        }

        @Override
        @Messages("CheckPHPVerDispName=Language feature not compatible with PHP version indicated in project settings")
        public String getDisplayName() {
            return Bundle.CheckPHPVerDispName();
        }

        @Override
        public String getKey() {
            return KEY;
        }

    }

}
