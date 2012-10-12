/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.verification;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.QualifiedNameKind;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.GotoLabel;
import org.netbeans.modules.php.editor.parser.astnodes.GotoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.TypeDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PHP53UnhandledError extends AbstractUnhandledError {

    @Override
    void compute(PHPRuleContext context, List<Error> errors) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileObject != null && appliesTo(fileObject)) {
            CheckVisitor checkVisitor = new CheckVisitor(fileObject);
            phpParseResult.getProgram().accept(checkVisitor);
            errors.addAll(checkVisitor.getErrors());
        }
    }

    private boolean appliesTo(FileObject fileObject) {
        return CodeUtils.isPhp_52(fileObject);
    }

    private static class CheckVisitor extends DefaultVisitor {
        private List<Error> errors = new LinkedList<Error>();
        private Stack<ASTNode> parent = new Stack<ASTNode>();
        private final FileObject fileObject;

        public CheckVisitor(FileObject fileObject) {
            this.fileObject = fileObject;
        }

        public List<Error> getErrors() {
            return errors;
        }

        @Override
        public void visit(ClassDeclaration node) {
            parent.push(node);
            super.visit(node);
            parent.pop();
        }

        @Override
        public void visit(InterfaceDeclaration node) {
            parent.push(node);
            super.visit(node);
            parent.pop();
        }

        @Override
        public void visit(NamespaceDeclaration node) {
            final NamespaceName name = node.getName();
            if (name != null) {
                createError(node.getStartOffset(), name.getEndOffset());
            } else {
                createError(node);
            }
            super.visit(node);
        }

        @Override
        public void visit(LambdaFunctionDeclaration node) {
            createError(node);
            super.visit(node);
        }

        @Override
        public void visit(GotoLabel node) {
            createError(node);
            super.visit(node);
        }

        @Override
        public void visit(ConstantDeclaration node) {
            if (!parent.empty() && parent.peek() instanceof TypeDeclaration) {
                return;
            }
            createError(node);
            super.visit(node);
        }

        @Override
        public void visit(GotoStatement node) {
            createError(node);
            super.visit(node);
        }

        @Override
        public void visit(UseStatement node) {
            createError(node);
            super.visit(node);
        }

        @Override
        public void visit(StaticMethodInvocation node) {
            if (node.getClassName() instanceof Variable) {
                createError(node.getClassName());
            }
            super.visit(node);
        }

        @Override
        public void visit(NamespaceName node) {
            QualifiedName qname = QualifiedName.create(node);
            if (qname.getKind() != QualifiedNameKind.UNQUALIFIED){
                createError(node);
            }
            super.visit(node);
        }

        private  void createError(int startOffset, int endOffset){
            errors.add(new PhpVersionError(fileObject, startOffset, endOffset));
        }

        private void createError(ASTNode node){
            createError(node.getStartOffset(), node.getEndOffset());
        }

    }

    private static class PhpVersionError extends PHPVerificationError {
        private static final String ERROR_KEY = "Php.Version.53"; //NOI18N

        public PhpVersionError(FileObject fileObject, int startOffset, int endOffset) {
            super(fileObject, startOffset, endOffset);
        }

        @Override
        @Messages("PhpVersionErrorDisp=Language feature not compatible with PHP version indicated in project settings")
        public String getDisplayName() {
            return Bundle.PhpVersionErrorDisp();
        }

        @Override
        @Messages("PhpVersionErrorDesc=Detect language features not compatible with PHP version indicated in project settings")
        public String getDescription() {
            return Bundle.PhpVersionErrorDesc();
        }

        @Override
        public String getKey() {
            return ERROR_KEY;
        }

    }

    @Override
    @Messages("PHP53VersionErrorHintDispName=Language feature not compatible with PHP version indicated in project settings")
    public String getDisplayName() {
        return Bundle.PHP53VersionErrorHintDispName();
    }

}
