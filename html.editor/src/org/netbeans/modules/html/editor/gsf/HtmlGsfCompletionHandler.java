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
package org.netbeans.modules.html.editor.gsf;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.CodeCompletionContext;
import org.netbeans.modules.gsf.api.CodeCompletionHandler;
import org.netbeans.modules.gsf.api.CodeCompletionResult;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ParameterInfo;
import org.netbeans.modules.gsf.spi.DefaultCompletionResult;

/**
 * A GSF-based code completion provider for HTML. 
 * NOTE: This code completion provider is mostly unused. There is a separate
 * editor code completion provider that does the actual code completion for
 * HTML.  This is an ADDITIONAL code completion provider whose only job
 * currently is to return a non empty code completion result when the caret
 * (|) is inside an empty attribute value either for a JavaScript or a
 * CSS style attribute, e.g.   onclick="|" or   style='|'.  In this case,
 * it will pass back an empty code completion result, but the result object
 * marks possible embedding regions for JavaScript or CSS which GSF will use
 * to consult those completion providers if they have anything to add.
 * (They do not normally get involved since code completion is only invoked
 * on languages found in the document, and there's no lexical tokens for
 * JavaScript or CSS in these cases.
 */
public class HtmlGsfCompletionHandler implements CodeCompletionHandler {

    public CodeCompletionResult complete(CodeCompletionContext context) {
        Document document = context.getInfo().getDocument();
        if (document != null && document instanceof BaseDocument) {
            final BaseDocument doc = (BaseDocument) document;

            doc.readLock(); // Read-lock due to Token hierarchy use
            try {
                TokenHierarchy<Document> tokenHierarchy = TokenHierarchy.get(document);
                TokenSequence ts = tokenHierarchy.tokenSequence();
                int offset = context.getCaretOffset();
                ts.move(offset);
                if (!(ts.moveNext() || ts.movePrevious())) {
                    return CodeCompletionResult.NONE;
                }
                if (ts.language() != HTMLTokenId.language()) {
                    ts = ts.embedded(HTMLTokenId.language());
                    if (ts == null) {
                        return CodeCompletionResult.NONE;
                    } else {
                        ts.move(offset);
                        if (!(ts.moveNext() || ts.movePrevious())) {
                            return CodeCompletionResult.NONE;
                        }
                    }
                }
                Token token = ts.token();
                if (token != null) {
                    TokenId id = token.id();
                    if (id == HTMLTokenId.VALUE_JAVASCRIPT && token.length() == 2 && ts.offset() == offset - 1) {
                        DefaultCompletionResult result = new DefaultCompletionResult(Collections.<CompletionProposal>emptyList(), false);
                        result.setEmbeddedTypes(Collections.singleton("text/javascript")); // NOI18N
                        return result;
                    } else if (id == HTMLTokenId.VALUE && token.length() == 2 && ts.offset() == offset - 1) {
                        // See if we're in style="".  I only want to check this if the caret is BETWEEN the
                        // carets, that's why we do the ts.offset() == offset-1 check.

                        // Let's see if it's a style attribute
                        while (ts.movePrevious()) {
                            token = ts.token();
                            id = token.id();
                            if (id == HTMLTokenId.ARGUMENT) {
                                if (TokenUtilities.equals("style", token.text())) { // NOI18N
                                    DefaultCompletionResult result = new DefaultCompletionResult(Collections.<CompletionProposal>emptyList(), false);
                                    result.setEmbeddedTypes(Collections.singleton("text/x-css")); // NOI18N
                                    return result;
                                }
                            } else if (!(id == HTMLTokenId.WS || id == HTMLTokenId.OPERATOR)) {
                                break;
                            }
                        }
                    }
                }
            } finally {
                doc.readUnlock();
            }
        }

        return CodeCompletionResult.NONE;
    }

    public String document(CompilationInfo info, ElementHandle element) {
        return null;
    }

    public ElementHandle resolveLink(String link, ElementHandle originalHandle) {
        return null;
    }

    public String getPrefix(CompilationInfo info, int caretOffset, boolean upToOffset) {
        return null;
    }

    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        return QueryType.NONE;
    }

    public String resolveTemplateVariable(String variable, CompilationInfo info, int caretOffset, String name, Map parameters) {
        return null;
    }

    public Set<String> getApplicableTemplates(CompilationInfo info, int selectionBegin, int selectionEnd) {
        return Collections.emptySet();
    }

    public ParameterInfo parameters(CompilationInfo info, int caretOffset, CompletionProposal proposal) {
        return ParameterInfo.NONE;
    }
}