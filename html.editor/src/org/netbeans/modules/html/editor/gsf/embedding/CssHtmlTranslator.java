/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.html.editor.gsf.embedding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * Creates CSS virtual source for html sources.
 * 
 * @author Marek Fukala
 */
public class CssHtmlTranslator implements CssEmbeddingProvider.Translator {

    private static final Logger LOGGER = Logger.getLogger(CssHtmlTranslator.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);

    public static final String CSS_MIME_TYPE = "text/x-css"; //NOI18N
    public static final String HTML_MIME_TYPE = "text/html"; //NOI18N

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        TokenHierarchy th = TokenHierarchy.create(snapshot.getText(), HTMLTokenId.language());
        TokenSequence ts = th.tokenSequence();
        HashMap<String, Object> state = new HashMap<String, Object>(6);
        List<Embedding> embeddings = new ArrayList<Embedding>();
        extractCssFromHTML(snapshot, ts, state, embeddings);
        return embeddings;
    }

    //internal state names for the html code analyzer
    protected static final String END_OF_LAST_SEQUENCE = "end_of_last_sequence";
    protected static final String IN_STYLE = "in_style";
    protected static final String IN_INLINED_STYLE = "in_inlined_style";
    private static final String QUTE_CUT = "quote_cut";

    /** @param ts An HTML token sequence always positioned at the beginning. */
    protected void extractCssFromHTML(Snapshot snapshot, TokenSequence<HTMLTokenId> ts, HashMap<String, Object> state, List<Embedding> embeddings) {
        while (ts.moveNext()) {
            Token<HTMLTokenId> htmlToken = ts.token();
            TokenId htmlId = htmlToken.id();
            if (htmlId == HTMLTokenId.STYLE) {
                state.put(IN_STYLE, Boolean.TRUE);
                //jumped into style
                int sourceStart = ts.offset();
                int sourceEnd = sourceStart + htmlToken.length();
                embeddings.add(snapshot.create(sourceStart, sourceEnd - sourceStart, CSS_MIME_TYPE ));
            } else {
                //jumped out of the style
                state.remove(IN_STYLE);

                if (state.get(IN_INLINED_STYLE) != null) {
                    if (htmlId == HTMLTokenId.VALUE) {
                        //continuation of the html style attribute value after templating
                        int sourceStart = ts.offset();
                        CharSequence text = htmlToken.text();
                        int tokenLength = htmlToken.length();
                        if (CharSequenceUtilities.endsWith(text, "\"") || CharSequenceUtilities.endsWith(text, "'")) {
                            tokenLength--;
                        }
                        embeddings.add(snapshot.create(sourceStart, tokenLength, CSS_MIME_TYPE));

                    } else {
                        state.remove(IN_INLINED_STYLE);


                        //xxx check if the following code has still meaning
                        //using the new embeddings

                        //generate the end of the virtual selector
                        int sourceStart = ts.offset();

                        if (state.get(QUTE_CUT) != null) {
                            sourceStart--;
                        }
                        // <<< eof xxx

                        state.remove(QUTE_CUT);
                        embeddings.add(snapshot.create("\n}\n", CSS_MIME_TYPE));


                    }
                }

                if (htmlId == HTMLTokenId.TAG_OPEN) {
                    //look at the tag and try to find the style="xx" attribute
                    //TODO make it work at the border of embedded sections

                    //TokenSequence<? extends HTMLTokenId> ts = ts.subSequence(ts.offset());
                    //ts.moveStart();
                    boolean style = false;
                    while (ts.moveNext()) {
                        Token<? extends HTMLTokenId> t = ts.token();
                        TokenId id = t.id();

                        if (id == HTMLTokenId.TAG_CLOSE_SYMBOL) {
                            break;
                        } else if (id == HTMLTokenId.ARGUMENT) {
                            if ("style".equalsIgnoreCase(t.text().toString())) {
                                style = true;
                            }
                        } else if (id == HTMLTokenId.VALUE && style) {
                            //found inlined css
                            int sourceStart = ts.offset();
                            String text = t.text().toString();

                            if (text.startsWith("\"") || text.startsWith("'")) {
                                sourceStart++;
                                text = text.substring(1);
                            }

                            int sourceEnd = sourceStart + text.length();
                            if (text.endsWith("\"") || text.endsWith("'")) {
                                sourceEnd--;
                                state.put(QUTE_CUT, Boolean.TRUE);
                                text = text.substring(0, text.length() - 1);
                            }
                            //encapsulate by rule name so the parser can parse it
//                            int generatedStart = buffer.length();
//                            buffer.append();
//                            int generatedEnd = buffer.length();
//                            CodeBlockData blockData = new CodeBlockData(sourceStart, sourceStart, generatedStart,
//                                    generatedEnd);
//                            codeBlocks.add(blockData);

                            embeddings.add(snapshot.create("\n SELECTOR {\n\t", CSS_MIME_TYPE));

//                            generatedStart = buffer.length();
//                            buffer.append(text);
//                            generatedEnd = buffer.length();
//                            blockData = new CodeBlockData(sourceStart, sourceEnd, generatedStart,
//                                    generatedEnd);
//                            codeBlocks.add(blockData);

                            embeddings.add(snapshot.create(sourceStart, sourceEnd - sourceStart, CSS_MIME_TYPE));

                            state.put(IN_INLINED_STYLE, Boolean.TRUE);

                            break;
                        }

                    }
                }
            }
        }

    }

//    IncrementalEmbeddingModel.UpdateState incrementalUpdate(EditHistory history) {
//        // Clear cache
//        // prevLexOffset = prevAstOffset = 0;
//        prevLexOffset = history.convertOriginalToEdited(prevLexOffset);
//
//        int offset = history.getStart();
//        int limit = history.getOriginalEnd();
//        int delta = history.getSizeDelta();
//
//        // True if the edits occur within (or overlapping) one or more CSS code blocks
//        boolean codeOverlaps = false;
//        // True if all edits were contained within the CSS codeblocks
//        boolean editsContained = false;
//        for (CodeBlockData codeBlock : codeBlocks) {
//            // Block not affected by move
//            if (codeBlock.sourceEnd < offset) {
//                continue;
//            }
//            if (codeBlock.sourceStart > limit) {
//                codeBlock.sourceStart += delta;
//                codeBlock.sourceEnd += delta;
//                continue;
//            }
//            if (codeBlock.sourceStart <= offset && codeBlock.sourceEnd >= limit) {
//                codeBlock.sourceEnd += delta;
//                if (history.getEditedEnd() <= codeBlock.sourceEnd) {
//                    editsContained = true;
//                }
//                codeOverlaps = true;
//                continue;
//            }
//            return IncrementalEmbeddingModel.UpdateState.FAILED;
//        }
//
//        if (codeOverlaps) {
//            if (editsContained) {
//                // All edits are inside our existing code blocks, so we
//                // know there aren't any new or removed code blocks to worry about.
//                return IncrementalEmbeddingModel.UpdateState.UPDATED;
//            } else {
//                // We MAY have new or removed separate sections, but one or
//                // more of these overlap with our blocks so we're not sure.
//                // Err on the safe side and recompute everything.
//                return IncrementalEmbeddingModel.UpdateState.FAILED;
//            }
//        } else {
//            // See if it looks like we have added or removed any CSS sections
//            initForeignTokens();
//
//            if (history.wasModified(HTMLTokenId.STYLE) ||
//                    // HACK: Embedded tokenid notification doesn't seem to work yet (bug in EditHistory).
//                    // Therefore, we have to detect if there is a relevant top-level change in PHP files,
//                    // JSP files or RHTML files that can correspond to an HTML section, and if so, recompute
//                    // everything.
//                    history.wasModified(phpHtml) || history.wasModified(jspHtml) || history.wasModified(erbHtml)) {
//                return IncrementalEmbeddingModel.UpdateState.FAILED;
//            } else if (history.wasModified(HTMLTokenId.VALUE)) {
//                // HACK for "VALUE" -- I really only want to do this when the token text is
//                // a "style" attribute -value-, however, that's not a separate TokenId
//                // from other values (the way the javascript attributes are separated out as VALUE_JAVASCRIPT)
//                // so we'll just treat ANY attribute change as a possible style attribute change.
//
//                // We might be able to do some extra optimizations to avoid recomputing the
//                // model here. For example, if you've only inserted text (history.getOriginalSize()==0)
//                // and look up the current token sequence, and see if the range (history.getStart() to
//                // history.getEditedEnd()) is completely within the same VALUE token. If so, we've
//                // only edited the token. Next see if this is a value token corresponding to a style
//                // attribute (which can be tricky outside of HTML in languages like JSP and ERB where
//                // we may have to worry about <% %>'s in the middle), and if not, return
//                // COMPLETED instead of FAILED.
//
//                return IncrementalEmbeddingModel.UpdateState.FAILED;
//            }
//
//            return IncrementalEmbeddingModel.UpdateState.COMPLETED;
//        }
//    }
//    /** Whether we have initialized phpHtml, jspHtml and erbHtml yet */
//    private static boolean foreignTokensInitialized;
//    /** PHPTokenId.T_INLINE_HTML token id, initialized lazily */
//    private static TokenId phpHtml;
//    /** JspTokenId.TEXT token id, initialized lazily */
//    private static TokenId jspHtml;
//    /** RhtmlTokenId.HTML token id, initialized lazily */
//    private static TokenId erbHtml;
//
//    @SuppressWarnings("unchecked")
//    private void initForeignTokens() {
//        if (foreignTokensInitialized) {
//            return;
//        }
//        foreignTokensInitialized = true;
//
//        Collection<LanguageProvider> providers = (Collection<LanguageProvider>) Lookup.getDefault().lookupAll(LanguageProvider.class);
//        for (LanguageProvider provider : providers) {
//            Language erbLanguage = (Language<? extends TokenId>) provider.findLanguage("application/x-httpd-eruby");
//            if (erbLanguage != null) {
//                // Sync with RhtmlTokenId.HTML!
//                erbHtml = erbLanguage.tokenId("HTML"); // NOI18N
//            }
//            Language jspLanguage = (Language<? extends TokenId>) provider.findLanguage("text/x-jsp");
//            if (jspLanguage != null) {
//                // Sync with JspTokenId.TEXT!
//                jspHtml = jspLanguage.tokenId("TEXT"); // NOI18N
//            }
//            Language phpLanguage = (Language<? extends TokenId>) provider.findLanguage("text/x-php5");
//            if (phpLanguage != null) {
//                // Sync with PHPTokenId.T_INLINE_HTML
//                phpHtml = phpLanguage.tokenId("T_INLINE_HTML");
//            }
//        }
//    }
}
