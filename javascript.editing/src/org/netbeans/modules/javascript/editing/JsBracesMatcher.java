/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.editing;

import java.util.List;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.*;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript.editing.lexer.JsTokenId;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 * Implementation of BracesMatcher interface for Javascript. It is based on original code
 * from JsKeystrokeHandler.findMatching
 *
 * @author Marek Slama
 */
public final class JsBracesMatcher implements BracesMatcher {

    MatcherContext context;

    public JsBracesMatcher (MatcherContext context) {
        this.context = context;
    }

    @Override
    public int [] findOrigin() throws InterruptedException, BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            BaseDocument doc = (BaseDocument) context.getDocument();
            int offset = context.getSearchOffset();

            TokenSequence<?extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, offset);

            if (ts != null) {
                ts.move(offset);

                if (!ts.moveNext()) {
                    return null;
                }

                Token<?extends JsTokenId> token = ts.token();

                if (token == null) {
                    return null;
                }
                
                TokenId id = token.id();
                
                if (id == JsTokenId.STRING_BEGIN) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id == JsTokenId.STRING_END) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id == JsTokenId.REGEXP_BEGIN) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id == JsTokenId.REGEXP_END) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id == JsTokenId.LPAREN) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id == JsTokenId.RPAREN) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id == JsTokenId.LBRACE) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id == JsTokenId.RBRACE) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id == JsTokenId.LBRACKET) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id == JsTokenId.RBRACKET) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                }
            }
            return null;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }
    
    @Override
    public int [] findMatches() throws InterruptedException, BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            BaseDocument doc = (BaseDocument) context.getDocument();
            int offset = context.getSearchOffset();

            TokenSequence<?extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, offset);

            if (ts != null) {
                ts.move(offset);

                if (!ts.moveNext()) {
                    return null;
                }

                Token<?extends JsTokenId> token = ts.token();

                if (token == null) {
                    return null;
                }

                TokenId id = token.id();
                
                OffsetRange r;
                if (id == JsTokenId.STRING_BEGIN) {
                    r = findPair(ts.languagePath(), ts.offset(), false, JsTokenId.STRING_BEGIN, JsTokenId.STRING_END);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == JsTokenId.STRING_END) {
                    r = findPair(ts.languagePath(), ts.offset(), true, JsTokenId.STRING_END, JsTokenId.STRING_BEGIN);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == JsTokenId.REGEXP_BEGIN) {
                    r = findPair(ts.languagePath(), ts.offset(), false, JsTokenId.REGEXP_BEGIN, JsTokenId.REGEXP_END);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == JsTokenId.REGEXP_END) {
                    r = findPair(ts.languagePath(), ts.offset(), true, JsTokenId.REGEXP_END, JsTokenId.REGEXP_BEGIN);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == JsTokenId.LPAREN) {
                    r = findPair(ts.languagePath(), ts.offset(), false, JsTokenId.LPAREN, JsTokenId.RPAREN);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == JsTokenId.RPAREN) {
                    r = findPair(ts.languagePath(), ts.offset(), true, JsTokenId.RPAREN, JsTokenId.LPAREN);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == JsTokenId.LBRACE) {
                    r = findPair(ts.languagePath(), ts.offset(), false, JsTokenId.LBRACE, JsTokenId.RBRACE);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == JsTokenId.RBRACE) {
                    r = findPair(ts.languagePath(), ts.offset(), true, JsTokenId.RBRACE, JsTokenId.LBRACE);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == JsTokenId.LBRACKET) {
                    r = findPair(ts.languagePath(), ts.offset(), false, JsTokenId.LBRACKET, JsTokenId.RBRACKET);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == JsTokenId.RBRACKET) {
                    r = findPair(ts.languagePath(), ts.offset(), true, JsTokenId.RBRACKET, JsTokenId.LBRACKET);
                    return new int [] {r.getStart(), r.getEnd() };
                }
            }
            return null;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }
    
    private OffsetRange findPair(LanguagePath lPath, int originOffset, boolean backward, TokenId originalId, TokenId pairId) {
        TokenHierarchy<Document> th = TokenHierarchy.get(context.getDocument());
        List<TokenSequence<?>> list;
        if (backward) {
            list = th.tokenSequenceList(lPath, 0, originOffset);
        } else {
            list = th.tokenSequenceList(lPath, originOffset + 1, context.getDocument().getLength());
        }
        
        int counter = 0;
        TokenId tokenID;
        
        for (TokenSequenceIterator tsi = new TokenSequenceIterator(list, backward); tsi.hasMore();) {
            TokenSequence<?> sq = tsi.getSequence();
            tokenID = sq.token().id();
            if (originalId == tokenID) {
                counter++;
            } else if (pairId == tokenID) {
                if (counter == 0) {
                    return new OffsetRange(sq.offset(), sq.offset() + sq.token().length());
                } else {
                    counter--;
                }
            }
        }
        return OffsetRange.NONE;
    }

    private static final class TokenSequenceIterator {

        private final List<TokenSequence<?>> list;
        private final boolean backward;

        private int index;

        public TokenSequenceIterator(List<TokenSequence<?>> list, boolean backward) {
            this.list = list;
            this.backward = backward;
            this.index = -1;
        }

        public boolean hasMore() {
            return backward ? hasPrevious() : hasNext();
        }

        public TokenSequence<?> getSequence() {
            assert index >= 0 && index < list.size() : "No sequence available, call hasMore() first."; //NOI18N
            return list.get(index);
        }

        private boolean hasPrevious() {
            boolean anotherSeq = false;

            if (index == -1) {
                index = list.size() - 1;
                anotherSeq = true;
            }

            for( ; index >= 0; index--) {
                TokenSequence<?> seq = list.get(index);
                if (anotherSeq) {
                    seq.moveEnd();
                }

                if (seq.movePrevious()) {
                    return true;
                }

                anotherSeq = true;
            }

            return false;
        }

        private boolean hasNext() {
            boolean anotherSeq = false;

            if (index == -1) {
                index = 0;
                anotherSeq = true;
            }

            for( ; index < list.size(); index++) {
                TokenSequence<?> seq = list.get(index);
                if (anotherSeq) {
                    seq.moveStart();
                }

                if (seq.moveNext()) {
                    return true;
                }

                anotherSeq = true;
            }

            return false;
        }
    }

}

