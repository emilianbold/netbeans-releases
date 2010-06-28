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
package org.netbeans.modules.web.jsf.editor.hints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.el.lexer.api.ELTokenId;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.editor.el.JsfElParser;
import org.netbeans.modules.web.jsf.editor.el.JsfElParser.ELParseResult;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Hints provider for checking syntax of EL expressions.
 *
 * @author Erno Mononen
 */
final class ELSyntaxChecker extends HintsProvider {

    @Override
    public List<Hint> compute(RuleContext context) {
        final Document doc = context.doc;
        final FileObject file =
                context.parserResult.getSnapshot().getSource().getFileObject();
        final WebModule webModule = WebModule.getWebModule(file);
        if (webModule == null) {
            return Collections.emptyList();
        }

        ExpressionCollector collector = new ExpressionCollector(doc, webModule);
        doc.render(collector);
        List<Hint> result = new ArrayList<Hint>();
        for (ELParseResult each : collector.getResult()) {
            Hint error = checkSyntax(each);
            if (error != null) {
                result.add(error);
            }
        }

        return result;
    }

    private Hint checkSyntax(ELParseResult parseResult) {

        if (parseResult.isValid()) {
            return null;
        }

        Hint hint = new Hint(HintsProvider.DEFAULT_ERROR_RULE,
                    parseResult.getError().getLocalizedMessage(),
                    parseResult.getFileObject(),
                    parseResult.getOffset(),
                    Collections.<HintFix>emptyList(),
                    HintsProvider.DEFAULT_ERROR_HINT_PRIORITY);
            return hint;
    }

    private static class ExpressionCollector implements Runnable {

        private final Document doc;
        private final WebModule webModule;
        private final List<ELParseResult> result = new ArrayList<ELParseResult>();

        public ExpressionCollector(Document doc, WebModule webModule) {
            this.doc = doc;
            this.webModule = webModule;
        }

        @Override
        public void run() {
            TokenHierarchy<?> th = TokenHierarchy.get(doc);
            TokenSequence<?> topLevel = th.tokenSequence();
            topLevel.moveStart();
            while (topLevel.moveNext()) {
                TokenSequence<ELTokenId> elTokenSequence =
                        topLevel.embedded(ELTokenId.language());
                if (elTokenSequence != null) {
                    elTokenSequence.moveEnd();
                    if (elTokenSequence.moveNext() || elTokenSequence.movePrevious()) {
                        Token<ELTokenId> token = elTokenSequence.token();
                        int offset = elTokenSequence.offset() + token.length();
                        collectExpressions(offset, elTokenSequence);
                    }
                }
            }
        }

        private void collectExpressions(int offset, TokenSequence<ELTokenId> tokenSequence) {
            try {
                JsfElParser parser = JsfElParser.create(doc, offset);
                ELParseResult parseResult = parser.parse();
                if (parseResult == null) {
                    tokenSequence.move(offset);
                    if (!tokenSequence.movePrevious()) {
                        return;
                    }
                    collectExpressions(tokenSequence.offset(), tokenSequence);
                } else {
                    result.add(parseResult);
//                    int startOffset = parseResult.getOffset().getStart();
//                    collectExpressions(startOffset, tokenSequence);
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        List<ELParseResult> getResult() {
            return result;
        }
    }
}
