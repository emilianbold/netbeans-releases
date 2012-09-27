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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.ParameterElement.OutputType;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.nodes.FunctionDeclarationInfo;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.netbeans.modules.php.editor.verification.PHPHintsProvider.Kind;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class WrongOrderOfArgsHint extends AbstractRule {

    private static final String HINT_ID = "Wrong.Order.Of.Args.Hint"; //NOI18N

    @Override
    void computeHintsImpl(PHPRuleContext context, List<Hint> hints, Kind kind) throws BadLocationException {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        TokenHierarchy<?> tokenHierarchy = phpParseResult.getSnapshot().getTokenHierarchy();
        CheckVisitor checkVisitor = new CheckVisitor(fileObject, context.doc, tokenHierarchy);
        phpParseResult.getProgram().accept(checkVisitor);
        hints.addAll(checkVisitor.getHints());
    }

    private class CheckVisitor extends DefaultVisitor {

        private final FileObject fileObject;
        private final List<FunctionDeclaration> wrongFunctions = new LinkedList<FunctionDeclaration>();
        private final List<Hint> hints = new LinkedList<Hint>();
        private final BaseDocument doc;
        private final TokenHierarchy<?> tokenHierarchy;

        public CheckVisitor(FileObject fileObject, BaseDocument doc, TokenHierarchy<?> tokenHierarchy) {
            this.fileObject = fileObject;
            this.doc = doc;
            this.tokenHierarchy = tokenHierarchy;
        }

        public List<Hint> getHints() {
            for (FunctionDeclaration wrongFunction : wrongFunctions) {
                processWrongFunction(wrongFunction);
            }
            return hints;
        }

        @Messages("WrongOrderOfArgsDesc=Wrong order of arguments")
        private void processWrongFunction(FunctionDeclaration node) {
            RearrangeParametersFix hintFix = new RearrangeParametersFix(doc, node, tokenHierarchy);
            hints.add(new Hint(WrongOrderOfArgsHint.this, Bundle.WrongOrderOfArgsDesc(), fileObject, hintFix.getOffsetRange(), Collections.<HintFix>singletonList(hintFix), 500));
        }

        @Override
        public void visit(FunctionDeclaration node) {
            boolean defaultValue = false;
            for (FormalParameter formalParameter : node.getFormalParameters()) {
                if (formalParameter.getDefaultValue() != null) {
                    defaultValue = true;
                } else if (defaultValue) {
                    wrongFunctions.add(node);
                    break;
                }
            }
        }

    }

    private class RearrangeParametersFix implements HintFix {

        private final FunctionDeclaration node;
        private final BaseDocument doc;
        private final FunctionDeclarationInfo functionDeclarationInfo;
        private final TokenHierarchy<?> tokenHierarchy;

        public RearrangeParametersFix(BaseDocument doc, FunctionDeclaration node, TokenHierarchy<?> tokenHierarchy) {
            this.doc = doc;
            this.node = node;
            this.tokenHierarchy = tokenHierarchy;
            functionDeclarationInfo = FunctionDeclarationInfo.create(new RearrangedFunctionDeclaration(node));
        }

        @Override
        @Messages({
            "# {0} - Method or function name",
            "RearrangeParamsDisp=Rearrange arguments of the method or function: {0}"
        })
        public String getDescription() {
            return Bundle.RearrangeParamsDisp(functionDeclarationInfo.getName());
        }

        @Override
        public void implement() throws Exception {
            EditList edits = new EditList(doc);
            OffsetRange offsetRange = getOffsetRange();
            StringBuilder sb = new StringBuilder();
            for (ParameterElement param : functionDeclarationInfo.getParameters()) {
                sb.append(param.asString(OutputType.COMPLETE_DECLARATION)).append(", "); //NOI18N
            }
            edits.replace(offsetRange.getStart(), offsetRange.getLength(), sb.toString().substring(0, sb.length() - 2), true, 0);
            edits.apply();
        }

        public OffsetRange getOffsetRange() {
            int start = 0;
            int end = 0;
            TokenSequence<PHPTokenId> ts = LexUtilities.getPHPTokenSequence(tokenHierarchy, node.getStartOffset());
            if (ts != null) {
                ts.move(node.getStartOffset());
                int braceMatch = 0;
                while (ts.moveNext()) {
                    Token t = ts.token();
                    if (t.id() == PHPTokenId.PHP_TOKEN) {
                        if (t.text().toString().equals("(")) { //NOI18N
                            if (braceMatch == 0) {
                                start = ts.offset() + 1;
                            }
                            braceMatch++;
                        } else if (t.text().toString().equals(")")) { //NOI18N
                            braceMatch--;
                        }
                        if (braceMatch == 0) {
                            end = ts.offset();
                            ts.moveNext();
                            break;
                        }
                    }
                }
            }
            return new OffsetRange(start, end);
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }

    }

    private static class RearrangedFunctionDeclaration extends FunctionDeclaration {

        public RearrangedFunctionDeclaration(FunctionDeclaration node) {
            super(node.getStartOffset(), node.getEndOffset(), node.getFunctionName(), node.getFormalParameters(), node.getBody(), node.isReference());
        }

        @Override
        public List<FormalParameter> getFormalParameters() {
            List<FormalParameter> rearrangedList = new LinkedList<FormalParameter>();
            List<FormalParameter> parametersWithDefault = new LinkedList<FormalParameter>();
            for (FormalParameter param : super.getFormalParameters()) {
                if (param.getDefaultValue() == null) {
                    rearrangedList.add(param);
                } else {
                    parametersWithDefault.add(param);
                }
            }
            rearrangedList.addAll(parametersWithDefault);
            return rearrangedList;
        }

    }

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @Messages("WrongOrderOfArgsHintDesc=Optional arguments should be grouped on the right side for better readability.<br><br>Example offending code:<br><code>function foo($optional=NULL, $required){}</code><br><br>Recommended code:<br><code>function foo($required, $optional=NULL){}</code>")
    public String getDescription() {
        return Bundle.WrongOrderOfArgsHintDesc();
    }

    @Override
    @Messages("WrongOrderOfArgsHintDispName=Order of Arguments")
    public String getDisplayName() {
        return Bundle.WrongOrderOfArgsHintDispName();
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

}
