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
package org.netbeans.modules.html.editor.embedding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.web.common.api.LexerUtils;

/**
 *
 * @author marekfukala
 */
@EmbeddingProvider.Registration(
        mimeType = "text/html",
        targetMimeType = "text/javascript")
public class JsEmbeddingProvider extends EmbeddingProvider {

    private static final String JS_MIMETYPE = "text/javascript"; //NOI18N
    private static final String NETBEANS_IMPORT_FILE = "__netbeans_import__"; // NOI18N
    private boolean cancelled = true;

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        cancelled = false; //resume
        List<Embedding> embeddings = new ArrayList<>();
        TokenSequence<HTMLTokenId> tokenSequence = snapshot.getTokenHierarchy().tokenSequence(HTMLTokenId.language());
        JsAnalyzerState state = new JsAnalyzerState();
        process(snapshot, tokenSequence, state, embeddings);
        return embeddings.isEmpty() 
                ? Collections.<Embedding>emptyList() 
                : Collections.singletonList(Embedding.create(embeddings));
    }

    @Override
    public int getPriority() {
        return 50;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    private void process(Snapshot snapshot, TokenSequence<HTMLTokenId> ts, JsAnalyzerState state, List<Embedding> embeddings) {
        ts.moveStart();
        
        while (ts.moveNext()) {
            if(cancelled) {
                embeddings.clear();
                return ;
            }
            
            Token<HTMLTokenId> token = ts.token();
            
            //end of the inlined js section
            if (state.in_inlined_javascript && token.id() != HTMLTokenId.VALUE_JAVASCRIPT) {
                handleValueJavascriptSectionEnd(snapshot, ts, token, state, embeddings);
            }

            switch (token.id()) {
                case SCRIPT:
                    handleScript(snapshot, ts, token, state, embeddings);
                    break;
                case TAG_OPEN:
                    handleOpenTag(snapshot, ts, token, state, embeddings);
                    break;
                case TEXT:
                    if (state.in_javascript) {
                        embeddings.add(snapshot.create(ts.offset(), token.length(), JS_MIMETYPE));
                    }
                    break;
                case VALUE_JAVASCRIPT:
                    handleValueJavascript(snapshot, ts, token, state, embeddings);
                    break;
                case TAG_CLOSE:
                    if (LexerUtils.equals("script", token.text(), true, true)) {
                        embeddings.add(snapshot.create("\n", JS_MIMETYPE)); //NOI18N
                    }
                    break;
                case EL_OPEN_DELIMITER:
                    handleELOpenDelimiter(snapshot, ts, token, state, embeddings);
                    break;
                default:
                    state.in_javascript = false;
                    break;
            }
        }
    }

    private static void handleELOpenDelimiter(Snapshot snapshot, TokenSequence<HTMLTokenId> ts, Token<HTMLTokenId> token, JsAnalyzerState state, List<Embedding> embeddings) {
        //1.check if the next token represents javascript content
        String mimetype = (String) ts.token().getProperty("contentMimeType"); //NOT IN AN API, TBD
        if (mimetype != null && "text/javascript".equals(mimetype)) {
            embeddings.add(snapshot.create("(function(){\n", JS_MIMETYPE)); //NOI18N

            //2. check content
            if (ts.moveNext()) {
                if (token.id() == HTMLTokenId.EL_CONTENT) {
                    //not empty expression: {{sg}}
                    embeddings.add(snapshot.create(ts.offset(), ts.token().length(), JS_MIMETYPE));
                    embeddings.add(snapshot.create(";\n});\n", JS_MIMETYPE)); //NOI18N
                } else if (token.id() == HTMLTokenId.EL_CLOSE_DELIMITER) {
                    //empty expression: {{}}
                    embeddings.add(snapshot.create(ts.offset(), 0, JS_MIMETYPE));
                    embeddings.add(snapshot.create(";\n});\n", JS_MIMETYPE)); //NOI18N
                }
            }
        }
    }

    private static void handleScript(Snapshot snapshot, TokenSequence<HTMLTokenId> ts, Token<HTMLTokenId> token, JsAnalyzerState state, List<Embedding> embeddings) {
        String scriptType = (String) token.getProperty(HTMLTokenId.SCRIPT_TYPE_TOKEN_PROPERTY);
        if (scriptType == null || "text/javascript".equals(scriptType)) {
            state.in_javascript = true;
            // Emit the block verbatim
            int sourceStart = ts.offset();
            String text = token.text().toString();
            List<EmbeddingPosition> jsEmbeddings = extractJsEmbeddings(text, sourceStart);
            for (EmbeddingPosition embedding : jsEmbeddings) {
                embeddings.add(snapshot.create(embedding.getOffset(), embedding.getLength(), JS_MIMETYPE));
            }
        }
    }

    private static void handleValueJavascript(Snapshot snapshot, TokenSequence<HTMLTokenId> ts, Token<HTMLTokenId> token, JsAnalyzerState state, List<Embedding> embeddings) {
        int sourceStart = ts.offset();
        int sourceEnd = sourceStart + ts.token().length();

        if (!state.in_inlined_javascript) {
            //first inlined javascript token

            String value = token.text().toString();
            // Strip opening "'s
            if (value.length() > 0) {
                char fch = value.charAt(0); //get first char
                if (fch == '\'' || fch == '"') {
                    state.opening_quotation_stripped = true;
                    sourceStart++; //skip the quotation
                }
            }

            //first inlined JS section - add the prelude
            // Add a function context around the event handler
            // such that it gets proper function context (e.g.
            // it can return values, the way event handlers can)
            embeddings.add(snapshot.create("(function(){\n", JS_MIMETYPE)); //NOI18N
        }

        state.in_inlined_javascript = true;

        state.lastInlinedJavascriptToken = ts.token();
        state.lastInlinedJavscriptEmbedding = snapshot.create(sourceStart, sourceEnd - sourceStart, JS_MIMETYPE);

        //add the embedding
        embeddings.add(state.lastInlinedJavscriptEmbedding);

        state.inlined_javascript_pieces++;

    }

    private static void handleValueJavascriptSectionEnd(Snapshot snapshot, TokenSequence<HTMLTokenId> ts, Token<HTMLTokenId> token, JsAnalyzerState state, List<Embedding> embeddings) {
        //we left the inlined javascript section
        //need to check if the last inlined javascript section endded
        //with a quotation and if so, strip it from the virtual source

        assert state.lastInlinedJavscriptEmbedding != null;
        assert state.lastInlinedJavascriptToken != null;

        int sourceStart = state.lastInlinedJavascriptToken.offset(snapshot.getTokenHierarchy());
        int sourceLength = state.lastInlinedJavascriptToken.length();
        CharSequence value = state.lastInlinedJavascriptToken.text();

        //strip closing quotation
        if (state.opening_quotation_stripped) {
            if (value.length() > 0) {
                char fch = value.charAt(value.length() - 1);
                if (fch == '\'' || fch == '"') {
                    sourceLength--;

                    //if there is only one inlined javascript piece, and starting quotation has been stripped,
                    //we need to do that again in the reentered embedding
                    if (state.inlined_javascript_pieces == 1) {
                        sourceStart++;
                        sourceLength--;
                    }

                    //need to adjust the last embedding
                    //1. remove the embedding from the list
                    boolean removed = embeddings.remove(state.lastInlinedJavscriptEmbedding);
                    assert removed;

                    //2. create new embedding with the adjusted length
                    embeddings.add(snapshot.create(sourceStart, sourceLength, JS_MIMETYPE));
                }
            }
        }

        //end of inlined javascript section - add postlude
        state.in_inlined_javascript = false;
        state.opening_quotation_stripped = false;
        state.lastInlinedJavascriptToken = null;
        state.lastInlinedJavscriptEmbedding = null;
        state.inlined_javascript_pieces = 0;

        // Finish the surrounding function context
        embeddings.add(snapshot.create("\n});\n", JS_MIMETYPE)); //NOI18N

    }

    private static void handleOpenTag(Snapshot snapshot, TokenSequence<HTMLTokenId> ts, Token<HTMLTokenId> token, JsAnalyzerState state, List<Embedding> embeddings) {
        // TODO - if we see a <script src="someurl"> block that also
        // has a nonempty body, warn - the body will be ignored!!
        // (This should be a quickfix)
        if (LexerUtils.equals("script", token.text(), false, false)) {
            // Look for "<script src=" and if found, locate any includes.
            // Quit when I find TAG_CLOSE or run out of tokens
            // (for files with errors)
            TokenSequence<? extends HTMLTokenId> ets = ts.subSequence(ts.offset());
            ets.moveStart();
            boolean foundSrc = false;
            boolean foundType = false;
            String type = null;
            String src = null;
            while (ets.moveNext()) {
                Token<? extends HTMLTokenId> t = ets.token();
                HTMLTokenId id = t.id();
                // TODO - if we see a DEFER attribute here record that somehow
                // such that I can have a quickfix look to make sure you don't try
                // to mess with the document!
                if (id == HTMLTokenId.TAG_CLOSE_SYMBOL) {
                    break;
                } else if (foundSrc || foundType) {
                    if (id == HTMLTokenId.ARGUMENT) {
                        break;
                    } else if (id == HTMLTokenId.VALUE) {
                        // Found a script src
                        if (foundSrc) {
                            src = t.toString();
                        } else {
                            assert foundType;
                            type = t.toString();
                        }
                        foundSrc = false;
                        foundType = false;
                    }
                } else if (id == HTMLTokenId.ARGUMENT) {
                    String val = t.toString();
                    switch (val) {
                        case "src":
                            foundSrc = true;
                            break;
                        case "type":
                            foundType = true;
                            break;
                    }
                }
            }
            if (src != null) {
                if (type == null || type.toLowerCase().indexOf("javascript") != -1) {
                    if (src.length() > 2 && src.startsWith("\"") && src.endsWith("\"")) {
                        src = src.substring(1, src.length() - 1);
                    }
                    if (src.length() > 2 && src.startsWith("'") && src.endsWith("'")) {
                        src = src.substring(1, src.length() - 1);
                    }

                    // Insert a file link
                    String insertText = NETBEANS_IMPORT_FILE + "('" + src + "');\n"; // NOI18N
                    embeddings.add(snapshot.create(insertText, JS_MIMETYPE));
                }
            }
        }

    }

    protected static List<EmbeddingPosition> extractJsEmbeddings(String text, int sourceStart) {
        List<EmbeddingPosition> embeddings = new LinkedList<>();
        // beginning comment around the script
        int start = 0;
        for (; start < text.length(); start++) {
            char c = text.charAt(start);
            if (!Character.isWhitespace(c)) {
                break;
            }
        }
        if (start < text.length() && text.startsWith("<!--", start)) { //NOI18N
            int lineEnd = text.indexOf('\n', start); //NOI18N
            if (isHtmlCommentStartToSkip(text, start, lineEnd)) {
                if (start > 0) {
                    embeddings.add(new EmbeddingPosition(sourceStart, start));
                }
                lineEnd++; //skip the \n
                sourceStart += lineEnd;
                text = text.substring(lineEnd);
            }
        }

        // inline comments inside script
        Scanner scanner = new Scanner(text).useDelimiter("(<!--).*(-->)"); //NOI18N
        while (scanner.hasNext()) {
            scanner.next();
            MatchResult match = scanner.match();
            embeddings.add(new EmbeddingPosition(sourceStart + match.start(), match.group().length()));
        }
        return embeddings;
    }

    private static boolean isHtmlCommentStartToSkip(String text, int start, int lineEnd) {
        if (lineEnd != -1) {
            // issue #223883 - one of suggested constructs: http://lachy.id.au/log/2005/05/script-comments (Example 4)
            if (text.startsWith("<!--//-->", start)) { //NOI18N
                return true;
            } else {
                //    embedded delimiter - issue #217081 || one line comment - issue #223883
                return (text.indexOf("-->", start) == -1 || lineEnd < text.indexOf("-->", start)); //NOI18N
            }
        } else {
            return false;
        }
    }

    private static final class JsAnalyzerState {

        int inlined_javascript_pieces = 0;
        boolean in_javascript = false;
        boolean in_inlined_javascript = false;
        boolean opening_quotation_stripped = false;
        Token<?> lastInlinedJavascriptToken = null;
        Embedding lastInlinedJavscriptEmbedding = null;
    }

    protected static final class EmbeddingPosition {

        private final int offset;
        private final int length;

        public EmbeddingPosition(int offset, int length) {
            this.offset = offset;
            this.length = length;
        }

        public int getLength() {
            return length;
        }

        public int getOffset() {
            return offset;
        }
    }
}
