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
package org.netbeans.modules.html.editor;

import org.netbeans.modules.html.editor.api.HtmlKit;
import java.util.Collections;
import java.util.List;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;
import org.openide.util.Exceptions;

/**
 * A HTML parser based implementation of BracesMatcher. 
 *
 * @author Marek Fukala
 */
public class HtmlBracesMatching implements BracesMatcher, BracesMatcherFactory {

    private MatcherContext context;
    private static final String BLOCK_COMMENT_START = "<!--"; //NOI18N
    private static final String BLOCK_COMMENT_END = "-->"; //NOI18N
    static boolean testMode = false;

    public HtmlBracesMatching() {
        this(null);
    }

    private HtmlBracesMatching(MatcherContext context) {
        this.context = context;
    }

    public int[] findOrigin() throws InterruptedException, BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            if (!testMode && MatcherContext.isTaskCanceled()) {
                return null;
            }
            TokenSequence ts = Utils.getJoinedHtmlSequence(context.getDocument());
            TokenHierarchy th = TokenHierarchy.get(context.getDocument());

            if (ts.language() == HTMLTokenId.language()) {
                ts.move(context.getSearchOffset());
                //if (context.isSearchingBackward() ? ts.movePrevious() : ts.moveNext()) {
                if (ts.moveNext()) {
                    if (context.isSearchingBackward() && ts.offset() + ts.token().length() < context.getSearchOffset()) {
                        //check whether the searched position doesn't overlap the token boundaries
                        return null;
                    }
                    Token t = ts.token();
                    if (tokenInTag(t)) {
                        //find the tag beginning
                        do {
                            Token t2 = ts.token();
                            if (!tokenInTag(t2)) {
                                return null;
                            } else if (t2.id() == HTMLTokenId.TAG_OPEN_SYMBOL) {
                                //find end
                                int tagNameEnd = -1;
                                while (ts.moveNext()) {
                                    Token t3 = ts.token();
                                    if (!tokenInTag(t3) || t3.id() == HTMLTokenId.TAG_OPEN_SYMBOL) {
                                        return null;
                                    } else if (t3.id() == HTMLTokenId.TAG_CLOSE_SYMBOL) {
                                        if ("/>".equals(t3.text().toString())) {
                                            //do no match empty tags
                                            return null;
                                        } else {
                                            int from = t2.offset(th);
                                            int to = t3.offset(th) + t3.length();
                                            if (tagNameEnd != -1) {
                                                return new int[]{from, to,
                                                            from, tagNameEnd,
                                                            to - 1, to};
                                            } else {
                                                return new int[]{from, to};
                                            }
                                        }
                                    } else if (t3.id() == HTMLTokenId.TAG_OPEN || t3.id() == HTMLTokenId.TAG_CLOSE) {
                                        tagNameEnd = t3.offset(th) + t3.length();
                                    }
                                }
                                break;
                            }
                        } while (ts.movePrevious());
                    } else if (t.id() == HTMLTokenId.BLOCK_COMMENT) {
                        String tokenImage = t.text().toString();
                        if (tokenImage.startsWith(BLOCK_COMMENT_START) && context.getSearchOffset() < (t.offset(th)) + BLOCK_COMMENT_START.length()) {
                            return new int[]{t.offset(th), t.offset(th) + BLOCK_COMMENT_START.length()};
                        } else if (tokenImage.endsWith(BLOCK_COMMENT_END) && (context.getSearchOffset() >= (t.offset(th)) + tokenImage.length() - BLOCK_COMMENT_END.length())) {
                            return new int[]{t.offset(th) + t.length() - BLOCK_COMMENT_END.length(), t.offset(th) + t.length()};
                        }
                    }
                }
            }
            return null;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }

    private boolean tokenInTag(Token t) {
        return t.id() == HTMLTokenId.TAG_CLOSE_SYMBOL || t.id() == HTMLTokenId.TAG_OPEN_SYMBOL || t.id() == HTMLTokenId.TAG_OPEN || t.id() == HTMLTokenId.TAG_CLOSE || t.id() == HTMLTokenId.WS || t.id() == HTMLTokenId.ARGUMENT || t.id() == HTMLTokenId.VALUE || t.id() == HTMLTokenId.VALUE_CSS || t.id() == HTMLTokenId.VALUE_JAVASCRIPT || t.id() == HTMLTokenId.OPERATOR || t.id() == HTMLTokenId.EOL;
    }

    public int[] findMatches() throws InterruptedException, BadLocationException {
        if (!testMode && MatcherContext.isTaskCanceled()) {
            return null;
        }

        final Source source = Source.create(context.getDocument());
        if (source == null) {
            return null;
        }

        final int[][] ret = new int[1][];
        try {
            ParserManager.parse(Collections.singleton(source), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    if (!testMode && MatcherContext.isTaskCanceled()) {
                        return;
                    }

                    if (!source.getMimeType().equals(HtmlKit.HTML_MIME_TYPE)) {
                        //find embedded result iterator
                        resultIterator = Utils.getResultIterator(resultIterator, HtmlKit.HTML_MIME_TYPE);
                    }

                    if (resultIterator == null) {
                        ret[0] = new int[]{context.getSearchOffset(), context.getSearchOffset()};
                        return;
                    }

                    HtmlParserResult result = (HtmlParserResult) resultIterator.getParserResult();
                    if (result == null) {
                        return;
                    }

                    int searched = result.getSnapshot().getEmbeddedOffset(context.getSearchOffset());
                    AstNode origin = result.findLeaf(searched);
                    if (origin != null) {
                        if (origin.type() == AstNode.NodeType.OPEN_TAG ||
                                origin.type() == AstNode.NodeType.ENDTAG) {

                            //adjust the tag node, we are interested in the tags itself, not in the tag ranges
                            origin = AstNodeUtils.getTagNode(origin, searched);

                            if (origin == null) {
                                //offset between tags, no match
                                ret[0] = null;
                            } else {

                                AstNode match = origin.getMatchingTag();
                                if (match == null) {
                                    if (origin.needsToHaveMatchingTag()) {
                                        //error
                                        ret[0] = null; //no match
                                    } else {
                                        //valid
                                        ret[0] = new int[]{context.getSearchOffset(), context.getSearchOffset()}; //match nothing, origin will be yellow  - workaround
                                    }
                                } else {
                                    //match

                                    if (match.type() == AstNode.NodeType.OPEN_TAG) {
                                        //match the '<tagname' part
                                        int f1 = match.startOffset();
                                        int t1 = f1 + match.name().length() + 1; /* +1 == open tag symbol '<' length */
                                        //match the closing '>' symbol
                                        int f2 = match.endOffset() - 1; // -1 == close tag symbol '>' length
                                        int t2 = match.endOffset();

                                        ret[0] = translate(new int[]{f1, t1, f2, t2}, result);
                                    } else {
                                        ret[0] = translate(new int[]{match.startOffset(), match.endOffset()}, result);
                                    }
                                }
                            }
                        } else if (origin.type() == AstNode.NodeType.COMMENT) {
                            if (searched >= origin.startOffset() && searched <= origin.startOffset() + BLOCK_COMMENT_START.length()) {
                                //complete end of comment
                                ret[0] = translate(new int[]{origin.endOffset() - BLOCK_COMMENT_END.length(), origin.endOffset()}, result);
                            } else if (searched >= origin.endOffset() - BLOCK_COMMENT_END.length() && searched <= origin.endOffset()) {
                                //complete start of comment
                                ret[0] = translate(new int[]{origin.startOffset(), origin.startOffset() + BLOCK_COMMENT_START.length()}, result);
                            }
                        }
                    }

                }
            });

        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }

        return ret[0];
    }

    private int[] translate(int[] match, Result source) {

        int[] translation = new int[match.length];
        for (int i = 0; i < match.length; i++) {
            translation[i] = source.getSnapshot().getOriginalOffset(match[i]);
        }
        return translation;
    }

    //BracesMatcherFactory implementation
    public BracesMatcher createMatcher(final MatcherContext context) {
        final HtmlBracesMatching[] ret = {null};
        context.getDocument().render(new Runnable() {

            public void run() {
                TokenHierarchy<Document> hierarchy = TokenHierarchy.get(context.getDocument());

                //test if the html sequence is the top level one
                if (hierarchy.tokenSequence().language() == HTMLTokenId.language()) {
                    ret[0] = new HtmlBracesMatching(context);
                    return;
                }

                //test for embeedded html 
                List<TokenSequence<?>> ets = hierarchy.embeddedTokenSequences(context.getSearchOffset(), context.isSearchingBackward());
                for (TokenSequence ts : ets) {
                    Language language = ts.language();
                    if (language == HTMLTokenId.language()) {
                        ret[0] = new HtmlBracesMatching(context);
                        return;
                    }
                }
                // We might be trying to search at the end or beginning of a document. In which
                // case there is nothing to find and/or search through, so don't create a matcher.
                //        throw new IllegalStateException("No text/html language found on the MatcherContext's search offset! This should never happen!");
            }
        });
        return ret[0];
    }
}
