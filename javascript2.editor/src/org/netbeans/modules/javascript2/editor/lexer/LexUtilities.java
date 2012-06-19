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
package org.netbeans.modules.javascript2.editor.lexer;

import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.util.Exceptions;


/**
 * Utilities associated with lexing or analyzing the document at the
 * lexical level, unlike AstUtilities which is contains utilities
 * to analyze parsed information about a document.
 *
 * @author Petr Hejl
 * @author Tor Norbye
 */
public final class LexUtilities {

    private LexUtilities() {
        super();
    }

    /** Find the JavaScript token sequence (in case it's embedded in something else at the top level */
    public static TokenSequence<? extends JsTokenId> getJsTokenSequence(Document doc, int offset) {
        TokenHierarchy<Document> th = TokenHierarchy.get(doc);
        return getJsTokenSequence(th, offset);
    }

    public static TokenSequence<?extends JsTokenId> getJsTokenSequence(Snapshot snapshot, int offset) {
        TokenHierarchy<?> th = snapshot.getTokenHierarchy();
        return getJsTokenSequence(th, offset);
    }

    /** Find the JavaScript token sequence (in case it's embedded in something else at the top level */
    public static TokenSequence<?extends JsTokenId> getJsTokenSequence(TokenHierarchy<?> th, int offset) {
        TokenSequence<?extends JsTokenId> ts = th.tokenSequence(JsTokenId.javascriptLanguage());

        if (ts == null) {
            // Possibly an embedding scenario such as an HTML file
            // First try with backward bias true
            List<TokenSequence<?>> list = th.embeddedTokenSequences(offset, true);

            for (TokenSequence t : list) {
                if (t.language() == JsTokenId.javascriptLanguage()) {
                    ts = t;

                    break;
                }
            }

            if (ts == null) {
                list = th.embeddedTokenSequences(offset, false);

                for (TokenSequence t : list) {
                    if (t.language() == JsTokenId.javascriptLanguage()) {
                        ts = t;

                        break;
                    }
                }
            }
        }

        return ts;
    }
    
    /** Search forwards in the token sequence until a token of type <code>down</code> is found */
    public static OffsetRange findFwd(Document doc, TokenSequence<?extends JsTokenId> ts, TokenId up,
        TokenId down) {
        int balance = 0;

        while (ts.moveNext()) {
            Token<?extends JsTokenId> token = ts.token();
            TokenId id = token.id();
            
            if (id == up) {
                balance++;
            } else if (id == down) {
                if (balance == 0) {
                    return new OffsetRange(ts.offset(), ts.offset() + token.length());
                }

                balance--;
            }
        }

        return OffsetRange.NONE;
    }

    public static OffsetRange findBwd(Document doc, TokenSequence<?extends JsTokenId> ts, TokenId up,
        TokenId down) {
        int balance = 0;

        while (ts.movePrevious()) {
            Token<?extends JsTokenId> token = ts.token();
            TokenId id = token.id();

            if (id == up) {
                if (balance == 0) {
                    return new OffsetRange(ts.offset(), ts.offset() + token.length());
                }

                balance++;
            } else if (id == down) {
                balance--;
            }
        }

        return OffsetRange.NONE;
    }

    public static Token<?extends JsTokenId> getToken(Document doc, int offset) {
        TokenSequence<?extends JsTokenId> ts = getPositionedSequence(doc, offset);

        if (ts != null) {
            return ts.token();
        }

        return null;
    }

    public static char getTokenChar(Document doc, int offset) {
        Token<?extends JsTokenId> token = getToken(doc, offset);

        if (token != null) {
            String text = token.text().toString();

            if (text.length() > 0) { // Usually true, but I could have gotten EOF right?

                return text.charAt(0);
            }
        }

        return 0;
    }

    /**
     * The same as braceBalance but generalized to any pair of matching
     * tokens.
     * @param open the token that increses the count
     * @param close the token that decreses the count
     */
    public static int getTokenBalance(Document doc, TokenId open, TokenId close, int offset)
        throws BadLocationException {
        TokenSequence<?extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, 0);
        if (ts == null) {
            return 0;
        }

        // XXX Why 0? Why not offset?
        ts.moveIndex(0);

        if (!ts.moveNext()) {
            return 0;
        }

        int balance = 0;

        do {
            Token t = ts.token();

            if (t.id() == open) {
                balance++;
            } else if (t.id() == close) {
                balance--;
            }
        } while (ts.moveNext());

        return balance;
    }

    /**
     * Return true iff the line for the given offset is a JavaScript comment line.
     * This will return false for lines that contain comments (even when the
     * offset is within the comment portion) but also contain code.
     */
    public static boolean isCommentOnlyLine(BaseDocument doc, int offset)
            throws BadLocationException {

        int begin = Utilities.getRowFirstNonWhite(doc, offset);

        if (begin == -1) {
            return false; // whitespace only
        }

        Token<? extends JsTokenId> token = LexUtilities.getToken(doc, begin);
        if (token != null) {
            return token.id() == JsTokenId.LINE_COMMENT;
        }

        return false;
    }

    /** Compute the balance of begin/end tokens on the line */
    public static int getLineBalance(BaseDocument doc, int offset, TokenId up, TokenId down) {
        try {
            int begin = Utilities.getRowStart(doc, offset);
            int end = Utilities.getRowEnd(doc, offset);

            TokenSequence<?extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, begin);
            if (ts == null) {
                return 0;
            }

            ts.move(begin);

            if (!ts.moveNext()) {
                return 0;
            }

            int balance = 0;

            do {
                Token<?extends JsTokenId> token = ts.token();
                TokenId id = token.id();

                if (id == up) {
                    balance++;
                } else if (id == down) {
                    balance--;
                }
            } while (ts.moveNext() && (ts.offset() <= end));

            return balance;
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);

            return 0;
        }
    }

    public static TokenSequence<?extends JsTokenId> getPositionedSequence(Document doc, int offset) {
        return getPositionedSequence(doc, offset, true);
    }

    public static TokenSequence<?extends JsTokenId> getPositionedSequence(Snapshot snapshot, int offset) {
        return getPositionedSequence(snapshot, offset, true);
    }

    public static TokenSequence<?extends JsTokenId> getPositionedSequence(Document doc, int offset, boolean lookBack) {
        return _getPosSeq(getJsTokenSequence(doc, offset), offset, lookBack);
    }

    public static TokenSequence<?extends JsTokenId> getPositionedSequence(Snapshot snapshot, int offset, boolean lookBack) {
        return _getPosSeq(getJsTokenSequence(snapshot, offset), offset, lookBack);
    }

    private static TokenSequence<?extends JsTokenId> _getPosSeq(TokenSequence<? extends JsTokenId> ts, int offset, boolean lookBack) {
        if (ts != null) {
            ts.move(offset);

            if (!lookBack && !ts.moveNext()) {
                return null;
            } else if (lookBack && !ts.moveNext() && !ts.movePrevious()) {
                return null;
            }

            return ts;
        }

        return null;
    }
    
    public static int getLexerOffset(JsParserResult info, int astOffset) {
        return info.getSnapshot().getOriginalOffset(astOffset);
    }
    
    public static OffsetRange getLexerOffsets(JsParserResult info, OffsetRange astRange) {
        int rangeStart = astRange.getStart();
        int start = info.getSnapshot().getOriginalOffset(rangeStart);
        if (start == rangeStart) {
            return astRange;
        } else if (start == -1) {
            return OffsetRange.NONE;
        } else {
            // Assumes the translated range maintains size
            return new OffsetRange(start, start + astRange.getLength());
        }
    }
    
    /**
     * Finds the first next token that is not in the ignore list
     * @param ts
     * @param ignores list of ignored tokens
     * @return 
     */
    public static Token<?extends JsTokenId> findNext(TokenSequence<?extends JsTokenId> ts, List<JsTokenId> ignores) {
        if (ignores.contains(ts.token().id())) {
            while (ts.moveNext() && ignores.contains(ts.token().id())) {}
        }
        return ts.token();
    }

    /**
     * Finds the first previous token that is not in the ignore list
     * @param ts
     * @param ignores
     * @return 
     */
    public static Token<?extends JsTokenId> findPrevious(TokenSequence<?extends JsTokenId> ts, List<JsTokenId> ignores) {
        if (ignores.contains(ts.token().id())) {
            while (ts.movePrevious() && ignores.contains(ts.token().id())) {}
        }
        return ts.token();
    }

    /**
     * Finds the first next token from the input list.
     * @param ts
     * @param lookfor
     * @return 
     */
    public static Token<?extends JsTokenId> findNextToken(TokenSequence<?extends JsTokenId> ts, List<JsTokenId> lookfor) {
        if (!lookfor.contains(ts.token().id())) {
            while (ts.moveNext() && !lookfor.contains(ts.token().id())) {}
        }
        return ts.token();
    }

    /**
     * Finds the first previous toke from the input list.
     * @param ts
     * @param lookfor
     * @return 
     */
    public static Token<?extends JsTokenId> findPreviousToken(TokenSequence<?extends JsTokenId> ts, List<JsTokenId> lookfor) {
        if (!lookfor.contains(ts.token().id())) {
            while (ts.movePrevious() && !lookfor.contains(ts.token().id())) {}
        }
        return ts.token();
    }

}
