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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TypeDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public abstract class PSR1Hint extends HintRule {

    @Override
    public void invoke(PHPRuleContext context, List<Hint> result) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() != null) {
            FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
            if (fileObject != null) {
                CheckVisitor checkVisitor = createVisitor(fileObject, context.doc);
                phpParseResult.getProgram().accept(checkVisitor);
                result.addAll(checkVisitor.getHints());
            }
        }
    }

    abstract CheckVisitor createVisitor(FileObject fileObject, BaseDocument baseDocument);

    public static class ConstantDeclarationHint extends PSR1Hint {
        private static final String HINT_ID = "PSR1.Hint.Constant"; //NOI18N
        private static final Pattern CONSTANT_PATTERN = Pattern.compile("[A-Z0-9]+[A-Z0-9_]*[A-Z0-9]+"); //NOI18N

        @Override
        CheckVisitor createVisitor(FileObject fileObject, BaseDocument baseDocument) {
            return new ConstantsVisitor(this, fileObject, baseDocument);
        }

        private static final class ConstantsVisitor extends CheckVisitor {

            public ConstantsVisitor(PSR1Hint psr1hint, FileObject fileObject, BaseDocument baseDocument) {
                super(psr1hint, fileObject, baseDocument);
            }

            @Override
            @NbBundle.Messages("PSR1ConstantDeclarationHintText=Class constants MUST be declared in all upper case with underscore separators.")
            public void visit(ConstantDeclaration node) {
                for (Identifier constantNameNode : node.getNames()) {
                    String constantName = constantNameNode.getName();
                    if (constantName != null && !CONSTANT_PATTERN.matcher(constantName).matches()) {
                        createHint(constantNameNode, Bundle.PSR1ConstantDeclarationHintText());
                    }
                }
            }

        }

        @Override
        public String getId() {
            return HINT_ID;
        }

        @Override
        @NbBundle.Messages("PSR1ConstantHintDesc=Class constants MUST be declared in all upper case with underscore separators.")
        public String getDescription() {
            return Bundle.PSR1ConstantHintDesc();
        }

        @Override
        @NbBundle.Messages("PSR1ConstantHintDisp=Class Constant Declaration")
        public String getDisplayName() {
            return Bundle.PSR1ConstantHintDisp();
        }

    }

    public static class MethodDeclarationHint extends PSR1Hint {
        private static final String HINT_ID = "PSR1.Hint.Method"; //NOI18N
        private static final Pattern CONSTANT_PATTERN = Pattern.compile("([a-z]|__)[a-zA-Z0-9]*"); //NOI18N

        @Override
        CheckVisitor createVisitor(FileObject fileObject, BaseDocument baseDocument) {
            return new MethodDeclarationVisitor(this, fileObject, baseDocument);
        }

        private static final class MethodDeclarationVisitor extends CheckVisitor {

            public MethodDeclarationVisitor(PSR1Hint psr1hint, FileObject fileObject, BaseDocument baseDocument) {
                super(psr1hint, fileObject, baseDocument);
            }

            @Override
            @NbBundle.Messages("PSR1MethodDeclarationHintText=Method names MUST be declared in camelCase().")
            public void visit(MethodDeclaration node) {
                Identifier functionNameNode = node.getFunction().getFunctionName();
                String methodName = functionNameNode.getName();
                if (methodName != null && !CONSTANT_PATTERN.matcher(methodName).matches()) {
                    createHint(functionNameNode, Bundle.PSR1MethodDeclarationHintText());
                }
            }

        }

        @Override
        public String getId() {
            return HINT_ID;
        }

        @Override
        @NbBundle.Messages("PSR1MethodDeclarationHintDesc=Method names MUST be declared in camelCase().")
        public String getDescription() {
            return Bundle.PSR1MethodDeclarationHintDesc();
        }

        @Override
        @NbBundle.Messages("PSR1MethodDeclarationHintDisp=Method Declaration")
        public String getDisplayName() {
            return Bundle.PSR1MethodDeclarationHintDisp();
        }

    }

    public static class TypeDeclarationHint extends PSR1Hint {
        private static final String HINT_ID = "PSR1.Hint.Type"; //NOI18N
        private FileObject fileObject;

        @Override
        CheckVisitor createVisitor(FileObject fileObject, BaseDocument baseDocument) {
            assert fileObject != null;
            this.fileObject = fileObject;
            return new TypeDeclarationVisitor(this, fileObject, baseDocument);
        }

        protected boolean isPhp52() {
            return CodeUtils.isPhp52(fileObject);
        }

        private static final class TypeDeclarationVisitor extends CheckVisitor {
            private static final Pattern PHP52_NAME_PATTERN = Pattern.compile("([A-Z][a-zA-Z0-9]*_)+[A-Z][a-zA-Z0-9]+"); //NOI18N
            private static final Pattern PHP53_NAME_PATTERN = Pattern.compile("[A-Z][a-zA-Z0-9]+"); //NOI18N
            private final boolean isPhp52;
            private boolean isInNamedNamespaceDeclaration = false;

            public TypeDeclarationVisitor(TypeDeclarationHint typeDeclarationHint, FileObject fileObject, BaseDocument baseDocument) {
                super(typeDeclarationHint, fileObject, baseDocument);
                isPhp52 = typeDeclarationHint.isPhp52();
            }

            @Override
            public void visit(NamespaceDeclaration node) {
                isInNamedNamespaceDeclaration = node.getName() != null;
                super.visit(node);
            }

            @Override
            public void visit(ClassDeclaration node) {
                processTypeDeclaration(node);
            }

            @Override
            public void visit(InterfaceDeclaration node) {
                processTypeDeclaration(node);
            }

            @Override
            public void visit(TraitDeclaration node) {
                processTypeDeclaration(node);
            }

            @NbBundle.Messages({
                "PSR1TypeDeclaration53HintText=Type names MUST be declared in StudlyCaps.",
                "PSR1TypeDeclaration52HintText=Type names SHOULD use the pseudo-namespacing convention of Vendor_ prefixes on type names.",
                "PSR1TypeDeclaration53NoNsHintText=Each type MUST be in a namespace of at least one level: a top-level vendor name."
            })
            private void processTypeDeclaration(TypeDeclaration node) {
                Identifier typeNameNode = node.getName();
                String typeName = typeNameNode.getName();
                if (isPhp52) {
                    if (typeName != null && !PHP52_NAME_PATTERN.matcher(typeName).matches()) {
                        createHint(typeNameNode, Bundle.PSR1TypeDeclaration52HintText());
                    }
                } else {
                    if (!isInNamedNamespaceDeclaration) {
                        createHint(typeNameNode, Bundle.PSR1TypeDeclaration53NoNsHintText());
                    } else {
                        if (typeName != null && !PHP53_NAME_PATTERN.matcher(typeName).matches()) {
                            createHint(typeNameNode, Bundle.PSR1TypeDeclaration53HintText());
                        }
                    }
                }
            }

        }

        @Override
        public String getId() {
            return HINT_ID;
        }

        @Override
        @NbBundle.Messages("PSR1TypeDeclarationHintDesc=Type names MUST be declared in StudlyCaps (Code written for 5.2.x and before SHOULD use the pseudo-namespacing convention of Vendor_ prefixes on type names). Each type is in a file by itself, and is in a namespace of at least one level: a top-level vendor name.")
        public String getDescription() {
            return Bundle.PSR1TypeDeclarationHintDesc();
        }

        @Override
        @NbBundle.Messages("PSR1TypeDeclarationHintDisp=Type Declaration")
        public String getDisplayName() {
            return Bundle.PSR1TypeDeclarationHintDisp();
        }
    }

    private abstract static class CheckVisitor extends DefaultVisitor {
        private final PSR1Hint psr1hint;
        private final FileObject fileObject;
        private final BaseDocument baseDocument;
        private final List<Hint> hints;

        public CheckVisitor(PSR1Hint psr1hint, FileObject fileObject, BaseDocument baseDocument) {
            this.psr1hint = psr1hint;
            this.fileObject = fileObject;
            this.baseDocument = baseDocument;
            this.hints = new ArrayList<>();
        }

        public List<Hint> getHints() {
            return hints;
        }

        @NbBundle.Messages({
            "# {0} - Text which describes the violation",
            "PSR1ViolationHintText=PSR-1 Violation:\n{0}"
        })
        protected void createHint(ASTNode node, String message) {
            OffsetRange offsetRange = new OffsetRange(node.getStartOffset(), node.getEndOffset());
            if (psr1hint.showHint(offsetRange, baseDocument)) {
                hints.add(new Hint(
                        psr1hint,
                        Bundle.PSR1ViolationHintText(message),
                        fileObject,
                        offsetRange,
                        null,
                        500));
            }
        }

    }

    @Override
    public boolean getDefaultEnabled() {
        return false;
    }

}
