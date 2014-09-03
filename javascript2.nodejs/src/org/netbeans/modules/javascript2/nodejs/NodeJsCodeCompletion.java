/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.nodejs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.api.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.api.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.spi.CompletionContext;
import org.netbeans.modules.javascript2.editor.spi.CompletionProvider;

/**
 *
 * @author Petr Pisl
 */
@CompletionProvider.Registration(priority = 41)
public class NodeJsCodeCompletion implements CompletionProvider {

    private static final String REQUIRE = "require";
    private static final String EXPORTS = "exports";

    @Override
    public List<CompletionProposal> complete(CodeCompletionContext ccContext, CompletionContext jsCompletionContext, String prefix) {
        List<CompletionProposal> result = new ArrayList<CompletionProposal>();

        if (jsCompletionContext == CompletionContext.STRING || jsCompletionContext == CompletionContext.EXPRESSION
                || jsCompletionContext == CompletionContext.GLOBAL) {
            TokenHierarchy<?> th = ccContext.getParserResult().getSnapshot().getTokenHierarchy();
            if (th == null) {
                return Collections.EMPTY_LIST;
            }
            int carretOffset = ccContext.getCaretOffset();
            int eOffset = ccContext.getParserResult().getSnapshot().getEmbeddedOffset(carretOffset);
            TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(th, eOffset);
            if (ts == null) {
                return Collections.EMPTY_LIST;
            }
            ts.move(eOffset);

            if (!ts.movePrevious()) {
                return Collections.EMPTY_LIST;
            }

            Token<? extends JsTokenId> token = null;
            JsTokenId tokenId;
            if (jsCompletionContext == CompletionContext.STRING || jsCompletionContext == CompletionContext.EXPRESSION) {
                token = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.BLOCK_COMMENT, JsTokenId.STRING_BEGIN, JsTokenId.STRING));
                tokenId = token.id();
                if (tokenId == JsTokenId.BRACKET_LEFT_PAREN && ts.movePrevious()) {
                    token = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.BLOCK_COMMENT));
                    tokenId = token.id();
                    if (tokenId == JsTokenId.IDENTIFIER && REQUIRE.equals(token.text().toString())) {
//                        System.out.println("nabidnout seznam knihoven");
                        result.addAll((new NodeJsCompletionItem.FilenameSupport()).getItems(ccContext.getParserResult().getSnapshot().getSource().getFileObject(), eOffset, ".." + prefix));
                    }
                } else {
                    if (tokenId == JsTokenId.IDENTIFIER && ts.movePrevious()) {
                        token = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.BLOCK_COMMENT));
                        tokenId = token.id();
                    }
                    if (tokenId == JsTokenId.OPERATOR_ASSIGNMENT) {
//                        System.out.println("nabidnout require()");
                        if (prefix.isEmpty() || REQUIRE.startsWith(prefix)) {
                            result.add(NodeJsCompletionItem.createNodeJsItem(new NodeJsCompletionDataItem(REQUIRE, null, "require('${cursor}')"), ElementKind.METHOD, eOffset - prefix.length()));
                        }
                    }
                }
            } else {
                if (prefix.isEmpty() || EXPORTS.startsWith(prefix)) {
                    result.add(NodeJsCompletionItem.createNodeJsItem(new NodeJsCompletionDataItem(EXPORTS, null, "exports."), ElementKind.PROPERTY, eOffset - prefix.length()));
                }
            }
        }

        return result.isEmpty() ? Collections.EMPTY_LIST : result;
    }

    @Override
    public String getHelpDocumentation(ParserResult info, ElementHandle element) {
        return null;
    }

}
