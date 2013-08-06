/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.verification;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TypeDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PSR0Hint extends HintRule {
    private static final String HINT_ID = "PSR0.Hint"; //NOI18N
    private static final String PHP_FILE_EXTENSION = ".php"; //NOI18N

    @Override
    public void invoke(PHPRuleContext context, List<Hint> result) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() != null) {
            FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
            if (fileObject != null) {
                CheckVisitor checkVisitor = new CheckVisitor(fileObject, context.doc);
                phpParseResult.getProgram().accept(checkVisitor);
                result.addAll(checkVisitor.getHints());
            }
        }
    }

    private final class CheckVisitor extends DefaultVisitor {
        private final FileObject fileObject;
        private final BaseDocument baseDocument;
        private final File file;
        private final List<Hint> hints;

        public CheckVisitor(FileObject fileObject, BaseDocument baseDocument) {
            this.fileObject = fileObject;
            this.baseDocument = baseDocument;
            this.file = FileUtil.toFile(fileObject);
            this.hints = new ArrayList<>();
        }

        public List<Hint> getHints() {
            return hints;
        }

        @Override
        @NbBundle.Messages("PSR0WrongNamespaceNameHintText=PSR-0 Violation:\nNamespace declaration name doesn't correspond to current directory structure.")
        public void visit(NamespaceDeclaration node) {
            NamespaceName namespaceName = node.getName();
            String currentNamespaceName = CodeUtils.extractQualifiedName(namespaceName);
            String namespaceNameToPath = currentNamespaceName.replace('\\', File.separatorChar);
            String fileDirPath = file.getParent();
            if (!fileDirPath.contains(namespaceNameToPath)) {
                createHint(namespaceName, Bundle.PSR0WrongNamespaceNameHintText());
            }
            super.visit(node);
        }

        @Override
        public void visit(ClassDeclaration node) {
            processTypeDeclaration(node);
            super.visit(node);
        }

        @Override
        public void visit(InterfaceDeclaration node) {
            processTypeDeclaration(node);
            super.visit(node);
        }

        @Override
        public void visit(TraitDeclaration node) {
            processTypeDeclaration(node);
            super.visit(node);
        }

        @NbBundle.Messages("PSR0WrongTypeNameHintText=PSR-0 Violation:\nType declaration name doesn't correspond to current file path.")
        private void processTypeDeclaration(TypeDeclaration node) {
            String currentTypeName = CodeUtils.extractTypeName(node);
            String typeNameToPath = currentTypeName.replace('_', File.separatorChar);
            String filePath = file.getPath();
            if (!filePath.endsWith(typeNameToPath + PHP_FILE_EXTENSION)) {
                Identifier name = node.getName();
                createHint(name, Bundle.PSR0WrongTypeNameHintText());
            }
        }

        private void createHint(ASTNode node, String message) {
            OffsetRange offsetRange = new OffsetRange(node.getStartOffset(), node.getEndOffset());
            if (showHint(offsetRange, baseDocument)) {
                hints.add(new Hint(
                        PSR0Hint.this,
                        message,
                        fileObject,
                        offsetRange,
                        Collections.<HintFix>emptyList(),
                        500));
            }
        }

    }

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @NbBundle.Messages("PSR0HintDesc=The following describes the mandatory requirements that must be adhered to for autoloader interoperability.")
    public String getDescription() {
        return Bundle.PSR0HintDesc();
    }

    @Override
    @NbBundle.Messages("PSR0HintDisp=PSR-0: Autoloading Standard")
    public String getDisplayName() {
        return Bundle.PSR0HintDisp();
    }

    @Override
    public boolean getDefaultEnabled() {
        return false;
    }
}
