/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.smarty.editor;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.smarty.editor.lexer.TplTokenId;
import org.netbeans.modules.php.smarty.editor.lexer.TplTopTokenId;
import org.netbeans.modules.php.smarty.editor.parser.TplParserResult;
import org.netbeans.modules.php.smarty.editor.parser.TplParserResult.Block;
import org.netbeans.modules.php.smarty.editor.utlis.LexerUtils;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;
import org.openide.util.Exceptions;

/**
 * TPL parser based implementation of BracesMatcher. Inspired by HtmlBracesMatching.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class TplBracesMatching implements BracesMatcher, BracesMatcherFactory {

    private MatcherContext context;
    protected static boolean testMode = false;

    public TplBracesMatching() {
        this(null);
    }

    private TplBracesMatching(MatcherContext context) {
        this.context = context;
    }

    @Override
    public int[] findOrigin() throws InterruptedException, BadLocationException {
        int searchOffset = context.getSearchOffset();
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            if (!testMode && MatcherContext.isTaskCanceled()) {
                return null;
            }
            TokenSequence<TplTopTokenId> ts = LexerUtils.getTplTopTokenSequence(context.getDocument(), searchOffset);
            TokenHierarchy<Document> th = TokenHierarchy.get(context.getDocument());

            if (ts != null && ts.language() == TplTopTokenId.language()) {
                while (searchOffset != context.getLimitOffset()) {
                    int diff = ts.move(searchOffset);
                    searchOffset = searchOffset + (context.isSearchingBackward() ? -1 : +1);
                    if (diff == 0 && context.isSearchingBackward()) {
                        //we are searching backward and the offset is at the token boundary
                        if (!ts.movePrevious()) {
                            continue;
                        }
                    } else {
                        if (!ts.moveNext()) {
                            continue;
                        }
                    }

                    Token<TplTopTokenId> t = ts.token();
                    if (tokenInTag(t)) {
                        //find the tag beginning
                        do {
                            Token<TplTopTokenId> t2 = ts.token();
                            int t2offs = ts.offset();
                            if (!tokenInTag(t2)) {
                                return null;
                            } else if (t2.id() == TplTopTokenId.T_SMARTY_OPEN_DELIMITER) {
                                //find end
                                int tagNameEnd = -1;
                                while (ts.moveNext()) {
                                    Token<TplTopTokenId> t3 = ts.token();
                                    int t3offs = ts.offset();
                                    int from = t2offs;
                                    int to = t3offs + t3.length();
                                    if (!tokenInTag(t3) || t3.id() == TplTopTokenId.T_SMARTY_OPEN_DELIMITER) {
                                        return null;
                                    } else if (t3.id() == TplTopTokenId.T_SMARTY) {
                                        TokenSequence<TplTokenId> tplTS = LexerUtils.getTplTokenSequence(th, t3offs);
                                        if (tplTS == null) {
                                            return null;
                                        } else {
                                            if (tplTS.token().id() == TplTokenId.FUNCTION) {
                                                tagNameEnd = tplTS.token().offset(th) + tplTS.token().length();
                                            }
                                        }
                                    } else if (t3.id() == TplTopTokenId.T_SMARTY_CLOSE_DELIMITER) {
                                        if (tagNameEnd != -1) {
                                            return new int[]{from, to,
                                                        from, tagNameEnd,
                                                        to - 1, to};
                                        } else {
                                            return new int[]{from, to};
                                        }
                                    }
                                }
                                break;
                            }
                        } while (ts.movePrevious());
                    }
                }
                return null;
            }
            return null;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }

    private boolean tokenInTag(Token t) {
        return t.id() == TplTopTokenId.T_SMARTY || t.id() == TplTopTokenId.T_SMARTY_CLOSE_DELIMITER
                || t.id() == TplTopTokenId.T_SMARTY_OPEN_DELIMITER || t.id() == TplTopTokenId.T_LITERAL_DEL
                || t.id() == TplTopTokenId.T_PHP_DEL || t.id() == TplTopTokenId.T_COMMENT;
    }

    @Override
    public int[] findMatches() throws InterruptedException, BadLocationException {
        if (!testMode && MatcherContext.isTaskCanceled()) {
            return null;
        }
        final int searchOffset = context.getSearchOffset();
        final Source source = Source.create(context.getDocument());
        if (source == null) {
            return null;
        }

        // comments - do not color them as errors
        TokenSequence<TplTopTokenId> ts = LexerUtils.getTplTopTokenSequence(context.getDocument(), searchOffset);
        if (ts != null && ts.language() == TplTopTokenId.language()) {
            ts.move(searchOffset);
            ts.moveNext(); ts.movePrevious();
            if (ts.token().id() == TplTopTokenId.T_COMMENT) {
                return new int[]{searchOffset, searchOffset};
            }
        }

        final int[][] ret = new int[1][];
        try {
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    if (!testMode && MatcherContext.isTaskCanceled()
                            || !source.getMimeType().equals(TplDataLoader.MIME_TYPE)) {
                        return;
                    }

                    if (resultIterator == null) {
                        ret[0] = new int[]{searchOffset, searchOffset};
                        return;
                    }

                    TplParserResult parserResult = (TplParserResult) resultIterator.getParserResult();
                    if (parserResult == null) {
                        return;
                    }

                    int searchOffsetLocal = searchOffset;
                    while (searchOffsetLocal != context.getLimitOffset()) {
                        int searched = parserResult.getSnapshot().getEmbeddedOffset(searchOffsetLocal);
                        Block block = getBlockForOffset(parserResult, searched, context.isSearchingBackward());
                        if (block == null) {
                            return;
                        }
                        if (block.getSections().size() == 1) {
                            //just simple tag - was found by findOrigin()
                            ret[0] = new int[]{searchOffset, searchOffset};
                            return;
                        }

                        List<Integer> result = new LinkedList<Integer>();
                        TplParserResult.Section lastSection = null;
                        for (TplParserResult.Section section : block.getSections()) {
                            OffsetRange or = section.getOffset();
                            or = new OffsetRange(or.getStart() - 1, or.getEnd() + 1);
                            if (!or.containsInclusive(searchOffset)) {
                                insertMatchingSection(result, section);
                            } else {
                                if (lastSection == null) {
                                    lastSection = section;
                                } else {
                                    if ((section.getOffset().getStart() < lastSection.getOffset().getStart() && context.isSearchingBackward())
                                            || section.getOffset().getStart() > lastSection.getOffset().getStart() && !context.isSearchingBackward()) {
                                        insertMatchingSection(result, lastSection);
                                        lastSection = section;
                                    } else {
                                        insertMatchingSection(result, section);
                                    }
                                }
                            }
                        }
                        ret[0] = convertToIntegers(result);
                        searchOffsetLocal = searchOffsetLocal + (context.isSearchingBackward() ? -1 : +1);
                    }
                }
            });

        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }

        return ret[0];
    }

    private static void insertMatchingSection(List<Integer> result, TplParserResult.Section section) {
        // XXX - keep in mind custom delimiters
        OffsetRange offset = section.getOffset();
        result.add(offset.getStart() - 1);
        result.add(offset.getStart() + section.getFunctionNameLength());
        result.add(offset.getEnd());
        result.add(offset.getEnd() + 1);
    }

    private static int[] convertToIntegers(List<Integer> list) {
        int[] integers = new int[list.size()];
        Iterator<Integer> iterator = list.iterator();
        for (int i = 0; i < integers.length; i++) {
            integers[i] = iterator.next().intValue();
        }
        return integers;
    }

    /**
     * Gets block of tags for given offset.
     *
     * @param parserResult tplParserResult
     * @param offset examined offset
     * @return {@code TplParserResult.Block} where one of sections contain the offset, {@code null} otherwise - if
     * no such block was found
     */
    private static TplParserResult.Block getBlockForOffset(TplParserResult parserResult, int offset, boolean backwardSearching) {
        // XXX - should think about the custom delimiters later
        Block lastBlock = null;
        int previousBlockOffset = -1;
        for (TplParserResult.Block block : parserResult.getBlocks()) {
            for (TplParserResult.Section section : block.getSections()) {
                OffsetRange or = section.getOffset();
                or = new OffsetRange(or.getStart() - 1, or.getEnd() + 1);
                if (or.containsInclusive(offset)) {
                    if (lastBlock != null) {
                        if ((section.getOffset().getStart() < previousBlockOffset && backwardSearching)
                                || section.getOffset().getStart() > previousBlockOffset && !backwardSearching){
                            return block;
                        } else {
                            return lastBlock;
                        }
                    } else {
                        lastBlock = block;
                        previousBlockOffset = section.getOffset().getStart();
                    }
                }
            }
        }
        return lastBlock;
    }

    @Override
    public BracesMatcher createMatcher(final MatcherContext context) {
        final TplBracesMatching[] ret = {null};
        context.getDocument().render(new Runnable() {
            @Override
            public void run() {
                TokenHierarchy<Document> hierarchy = TokenHierarchy.get(context.getDocument());

                //test if the tpl sequence is the top level one
                if (hierarchy.tokenSequence().language() == TplTopTokenId.language()) {
                    ret[0] = new TplBracesMatching(context);
                }
            }
        });
        return ret[0];
    }
}
