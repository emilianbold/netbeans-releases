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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.html.editor.lib.html4parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.NodeUtils;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.lib.api.ParseException;
import org.netbeans.modules.html.editor.lib.api.ProblemDescription;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult;
import org.netbeans.modules.html.editor.lib.api.elements.NodeVisitor;
import org.netbeans.modules.html.editor.lib.api.validation.ValidationContext;
import org.netbeans.modules.html.editor.lib.api.validation.ValidationException;
import org.netbeans.modules.html.editor.lib.api.validation.ValidationResult;
import org.netbeans.modules.html.editor.lib.api.validation.Validator;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author marekfukala
 */
@ServiceProvider(service=Validator.class, position=20)
public class Html4ValidatorImpl implements Validator {

    @Override
    public ValidationResult validate(ValidationContext context) throws ValidationException {
        assert canValidate(context.getVersion());
        List<ProblemDescription> problems = new ArrayList<ProblemDescription>();
        try {
            problems.addAll(extractErrorsFromAST(context.getSyntaxAnalyzerResult()));
            problems.addAll(findLexicalErrors(context.getSyntaxAnalyzerResult()));
            return new ValidationResult(this, context, problems, problems.isEmpty());

        } catch (ParseException ex) {
            throw new ValidationException(ex);
        }
    }

        private List<ProblemDescription> extractErrorsFromAST(SyntaxAnalyzerResult result) throws ParseException {
        final List<ProblemDescription> _errors = new ArrayList<ProblemDescription>();

        NodeVisitor errorsCollector = new NodeVisitor() {

            @Override
            public void visit(Node node) {
                if (node.type() == ElementType.OPEN_TAG
                        || node.type() == ElementType.END_TAG) {
//                        || node.type() == ElementType.UNKNOWN_TAG) { 

                    for (ProblemDescription desc : node.problems()) {
                        if (desc.getType() < ProblemDescription.WARNING) {
                            continue;
                        }
                        //some error in the node, report
                        ProblemDescription pd = ProblemDescription.create(
                                desc.getKey(),
                                desc.getText(),

                                desc.getType(),
                                desc.getFrom(),
                                desc.getTo());

                        _errors.add(pd);

                    }
                }
            }
        };

        NodeUtils.visitChildren(result.parseHtml().root(), errorsCollector);

        return _errors;
    }


    private List<ProblemDescription> findLexicalErrors(SyntaxAnalyzerResult result) {
        TokenHierarchy th = result.getSource().getSnapshot().getTokenHierarchy();
        TokenSequence<HTMLTokenId> ts = th.tokenSequence(HTMLTokenId.language());
        if (ts == null) {
            return Collections.emptyList();
        }

        final List<ProblemDescription> lexicalErrors = new ArrayList<ProblemDescription>();
        ts.moveStart();
        while (ts.moveNext()) {
            if (ts.token().id() == HTMLTokenId.ERROR) {
                //some error in the node, report
                String msg = NbBundle.getMessage(Html4ValidatorImpl.class, "MSG_UnexpectedToken", ts.token().text()); //NOI18N
                ProblemDescription pd = ProblemDescription.create(
                                "unexpected_token", //NOI18N
                                msg,
                                ProblemDescription.ERROR,
                                ts.offset(),
                                ts.offset() + ts.token().length());
                
                lexicalErrors.add(pd);
            }
        }
        return lexicalErrors;

    }

    @Override
    public String getValidatorName() {
        return "legacy html4 validator"; //NOI18N
    }

    @Override
    //XXX the validator can also validate html4, but for now such validation is done by the old SGML parser
    public boolean canValidate(HtmlVersion version) {
        switch(version) {
            case HTML32:
            case HTML40_FRAMESET:
            case HTML40_STRICT:
            case HTML40_TRANSATIONAL:
            case HTML41_FRAMESET:
            case HTML41_STRICT:
            case HTML41_TRANSATIONAL:
            case XHTML10_FRAMESET:
            case XHTML10_TRANSATIONAL:
            case XHTML10_STICT:
            case XHTML11:
                return true;
            default:
                return false;
        }
    }

}
