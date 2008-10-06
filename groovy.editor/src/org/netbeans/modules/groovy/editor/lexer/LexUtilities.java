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
package org.netbeans.modules.groovy.editor.lexer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;


/**
 * Utilities associated with lexing or analyzing the document at the
 * lexical level, unlike AstUtilities which is contains utilities
 * to analyze parsed information about a document.
 *
 * @author Tor Norbye
 */
public class LexUtilities {
    /** Tokens that match a corresponding END statement. Even though while, unless etc.
     * can be statement modifiers, those luckily have different token ids so are not a problem
     * here.
     */
    private static final Set<TokenId> END_PAIRS = new HashSet<TokenId>();

    /**
     * Tokens that should cause indentation of the next line. This is true for all {@link #END_PAIRS},
     * but also includes tokens like "else" that are not themselves matched with end but also contribute
     * structure for indentation.
     *
     */
    private static final Set<TokenId> INDENT_WORDS = new HashSet<TokenId>();

    private static final List<GroovyTokenId> WHITESPACES_AND_COMMENTS = Arrays.asList(
            GroovyTokenId.WHITESPACE,
            GroovyTokenId.NLS,
            GroovyTokenId.EOL,
            GroovyTokenId.LINE_COMMENT,
            GroovyTokenId.BLOCK_COMMENT);

    static {
        END_PAIRS.add(GroovyTokenId.LBRACE);
        INDENT_WORDS.addAll(END_PAIRS);
        INDENT_WORDS.add(GroovyTokenId.COLON);
    }

    private LexUtilities() {
    }

    public static OffsetRange getLexerOffsets(CompilationInfo info, OffsetRange astRange) {
        ParserResult result = info.getEmbeddedResult(GroovyTokenId.GROOVY_MIME_TYPE, 0);
        if (result != null) {
            TranslatedSource ts = result.getTranslatedSource();
            if (ts != null) {
                int rangeStart = astRange.getStart();
                int start = ts.getLexicalOffset(rangeStart);
                if (start == rangeStart) {
                    return astRange;
                } else if (start == -1) {
                    return OffsetRange.NONE;
                } else {
                    // Assumes the translated range maintains size
                    return new OffsetRange(start, start+astRange.getLength());
                }
            }
        }

        return astRange;
    }
    
    /** Find the Groovy token sequence (in case it's embedded in something else at the top level */
    @SuppressWarnings("unchecked")
    public static TokenSequence<?extends GroovyTokenId> getGroovyTokenSequence(BaseDocument doc, int offset) {
        TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
        return getGroovyTokenSequence(th, offset);
    }
    
    @SuppressWarnings("unchecked")
    private static TokenSequence<? extends GroovyTokenId> findRhtmlDelimited(TokenSequence t, int offset) {
        if (t.language().mimeType().equals("text/x-gsp")) {
            t.move(offset);
            if (t.moveNext() && t.token() != null && 
                    "groovy-delimiter".equals(t.token().id().primaryCategory())) { // NOI18N
                // It's a delimiter - move ahead and see if we find it
                if (t.moveNext() && t.token() != null &&
                        "groovy".equals(t.token().id().primaryCategory())) { // NOI18N
                    TokenSequence<?> ets = t.embedded();
                    if (ets != null) {
                        return (TokenSequence<? extends GroovyTokenId>)ets;
                    }
                }
            }
        }
        
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public static TokenSequence<?extends GroovyTokenId> getGroovyTokenSequence(TokenHierarchy<Document> th, int offset) {
        TokenSequence<?extends GroovyTokenId> ts = th.tokenSequence(GroovyTokenId.language());

        if (ts == null) {
            // Possibly an embedding scenario such as an RHTML file
            // First try with backward bias true
            List<TokenSequence<?>> list = th.embeddedTokenSequences(offset, true);

            for (TokenSequence t : list) {
                if (t.language() == GroovyTokenId.language()) {
                    ts = t;

                    break;
                } else {
                    TokenSequence<? extends GroovyTokenId> ets = findRhtmlDelimited(t, offset);
                    if (ets != null) {
                        return ets;
                    }
                }
            }

            if (ts == null) {
                list = th.embeddedTokenSequences(offset, false);

                for (TokenSequence t : list) {
                    if (t.language() == GroovyTokenId.language()) {
                        ts = t;

                        break;
                    } else {
                        TokenSequence<? extends GroovyTokenId> ets = findRhtmlDelimited(t, offset);
                        if (ets != null) {
                            return ets;
                        }
                    }
                }
            }
        }

        return ts;
    }

    public static TokenSequence<?extends GroovyTokenId> getPositionedSequence(BaseDocument doc, int offset) {
        return getPositionedSequence(doc, offset, true);
    }
    
    public static TokenSequence<?extends GroovyTokenId> getPositionedSequence(BaseDocument doc, int offset, boolean lookBack) {
        TokenSequence<?extends GroovyTokenId> ts = getGroovyTokenSequence(doc, offset);

        if (ts != null) {
            try {
                ts.move(offset);
            } catch (AssertionError e) {
                DataObject dobj = (DataObject)doc.getProperty(Document.StreamDescriptionProperty);

                if (dobj != null) {
                    Exceptions.attachMessage(e, FileUtil.getFileDisplayName(dobj.getPrimaryFile()));
                }

                throw e;
            }

            if (!lookBack && !ts.moveNext()) {
                return null;
            } else if (lookBack && !ts.moveNext() && !ts.movePrevious()) {
                return null;
            }
            
            return ts;
        }

        return null;
    }

    public static Token<?extends GroovyTokenId> getToken(BaseDocument doc, int offset) {
        TokenSequence<?extends GroovyTokenId> ts = getGroovyTokenSequence(doc, offset);

        if (ts != null) {
            try {
                ts.move(offset);
            } catch (AssertionError e) {
                DataObject dobj = (DataObject)doc.getProperty(Document.StreamDescriptionProperty);

                if (dobj != null) {
                    Exceptions.attachMessage(e, FileUtil.getFileDisplayName(dobj.getPrimaryFile()));
                }

                throw e;
            }

            if (!ts.moveNext() && !ts.movePrevious()) {
                return null;
            }

            Token<?extends GroovyTokenId> token = ts.token();

            return token;
        }

        return null;
    }

    public static char getTokenChar(BaseDocument doc, int offset) {
        Token<?extends GroovyTokenId> token = getToken(doc, offset);

        if (token != null) {
            String text = token.text().toString();

            if (text.length() > 0) { // Usually true, but I could have gotten EOF right?

                return text.charAt(0);
            }
        }

        return 0;
    }

    /** Search forwards in the token sequence until a token of type <code>down</code> is found */
    public static OffsetRange findHeredocEnd(TokenSequence<?extends GroovyTokenId> ts,  Token<?extends GroovyTokenId> startToken) {
        // Look for the end of the given heredoc
        String text = startToken.text().toString();
        assert text.startsWith("<<");
        text = text.substring(2);
        if (text.startsWith("-")) {
            text = text.substring(1);
        }
        if ((text.startsWith("\"") && text.endsWith("\"")) || (text.startsWith("'") && text.endsWith("'"))) {
            text = text.substring(0, text.length()-2);
        }
        String textn = text+"\n";

        while (ts.moveNext()) {
            Token<?extends GroovyTokenId> token = ts.token();
            TokenId id = token.id();

            if (id == GroovyTokenId.STRING_END || id == GroovyTokenId.QUOTED_STRING_END) {
                String t = token.text().toString();
                if (text.equals(t) || textn.equals(t)) {
                    return new OffsetRange(ts.offset(), ts.offset() + token.length());
                }
            }
        }

        return OffsetRange.NONE;
    }

    /** Search forwards in the token sequence until a token of type <code>down</code> is found */
    public static OffsetRange findHeredocBegin(TokenSequence<?extends GroovyTokenId> ts,  Token<?extends GroovyTokenId> endToken) {
        // Look for the end of the given heredoc
        String text = endToken.text().toString();
        if (text.endsWith("\n")) {
            text = text.substring(0, text.length()-1);
        }
        String textQuotes = "\"" + text + "\"";
        String textSQuotes = "'" + text + "'";

        while (ts.movePrevious()) {
            Token<?extends GroovyTokenId> token = ts.token();
            TokenId id = token.id();

            if (id == GroovyTokenId.STRING_BEGIN || id == GroovyTokenId.QUOTED_STRING_BEGIN) {
                String t = token.text().toString();
                String marker = null;
                if (t.startsWith("<<-")) {
                    marker = t.substring(3);
                } else if (t.startsWith("<<")) {
                    marker = t.substring(2);
                }
                if (marker != null && (text.equals(marker) || textQuotes.equals(marker) || textSQuotes.equals(marker))) {
                    return new OffsetRange(ts.offset(), ts.offset() + token.length());
                }
            }
        }

        return OffsetRange.NONE;
    }
    
    /** Search forwards in the token sequence until a token of type <code>down</code> is found */
    public static OffsetRange findFwd(BaseDocument doc, TokenSequence<?extends GroovyTokenId> ts, TokenId up,
        TokenId down) {
        int balance = 0;

        while (ts.moveNext()) {
            Token<?extends GroovyTokenId> token = ts.token();
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

    /** Search backwards in the token sequence until a token of type <code>up</code> is found */
    public static OffsetRange findBwd(BaseDocument doc, TokenSequence<?extends GroovyTokenId> ts, TokenId up,
        TokenId down) {
        int balance = 0;

        while (ts.movePrevious()) {
            Token<?extends GroovyTokenId> token = ts.token();
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

    /** Find the token that begins a block terminated by "end". This is a token
     * in the END_PAIRS array. Walk backwards and find the corresponding token.
     * It does not use indentation for clues since this could be wrong and be
     * precisely the reason why the user is using pair matching to see what's wrong.
     */
    public static OffsetRange findBegin(BaseDocument doc, TokenSequence<?extends GroovyTokenId> ts) {
        int balance = 0;

        while (ts.movePrevious()) {
            Token<?extends GroovyTokenId> token = ts.token();
            TokenId id = token.id();

            if (isBeginToken(id, doc, ts)) {
                // No matching dot for "do" used in conditionals etc.)) {
                if (balance == 0) {
                    return new OffsetRange(ts.offset(), ts.offset() + token.length());
                }

                balance--;
            } else if (id == GroovyTokenId.RBRACE) {
                balance++;
            }
        }

        return OffsetRange.NONE;
    }

    public static OffsetRange findEnd(BaseDocument doc, TokenSequence<?extends GroovyTokenId> ts) {
        int balance = 0;

        while (ts.moveNext()) {
            Token<?extends GroovyTokenId> token = ts.token();
            TokenId id = token.id();

            if (isBeginToken(id, doc, ts)) {
                balance--;
            } else if (id == GroovyTokenId.RBRACE) {
                if (balance == 0) {
                    return new OffsetRange(ts.offset(), ts.offset() + token.length());
                }

                balance++;
            }
        }

        return OffsetRange.NONE;
    }
    
    /** Determine whether "do" is an indent-token (e.g. matches an end) or if
     * it's simply a separator in while,until,for expressions)
     */
    public static boolean isEndmatchingDo(BaseDocument doc, int offset) {
        // In the following case, do is dominant:
        //     expression.do 
        //        whatever
        //     end
        //
        // However, not here:
        //     while true do
        //        whatever
        //     end
        //
        // In the second case, the end matches the while, but in the first case
        // the end matches the do
        
        // Look at the first token of the current line
        try {
            int first = Utilities.getRowFirstNonWhite(doc, offset);
            if (first != -1) {
                Token<? extends GroovyTokenId> token = getToken(doc, first);
                if (token != null) {
                    TokenId id = token.id();
                    if (id == GroovyTokenId.LITERAL_while || id == GroovyTokenId.LITERAL_for) {
                        return false;
                    }
                }
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
        
        return true;
    }

    /**
     * Return true iff the given token is a token that should be matched
     * with a corresponding "end" token, such as "begin", "def", "module",
     * etc.
     */
    public static boolean isBeginToken(TokenId id, BaseDocument doc, int offset) {
        return END_PAIRS.contains(id);
    }

    /**
     * Return true iff the given token is a token that should be matched
     * with a corresponding "end" token, such as "begin", "def", "module",
     * etc.
     */
    public static boolean isBeginToken(TokenId id, BaseDocument doc, TokenSequence<?extends GroovyTokenId> ts) {
        return END_PAIRS.contains(id);
    }
    
    /**
     * Return true iff the given token is a token that indents its content,
     * such as the various begin tokens as well as "else", "when", etc.
     */
    public static boolean isIndentToken(TokenId id) {
        return INDENT_WORDS.contains(id);
    }

    /** Compute the balance of begin/end tokens on the line.
     * @param doc the document
     * @param offset The offset somewhere on the line
     * @param upToOffset If true, only compute the line balance up to the given offset (inclusive),
     *   and if false compute the balance for the whole line
     */
    public static int getBeginEndLineBalance(BaseDocument doc, int offset, boolean upToOffset) {
        try {
            int begin = Utilities.getRowStart(doc, offset);
            int end = upToOffset ? offset : Utilities.getRowEnd(doc, offset);

            TokenSequence<?extends GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, begin);
            if (ts == null) {
                return 0;
            }

            ts.move(begin);

            if (!ts.moveNext()) {
                return 0;
            }

            int balance = 0;

            do {
                Token<?extends GroovyTokenId> token = ts.token();
                TokenId id = token.id();

                if (isBeginToken(id, doc, ts)) {
                    balance++;
                } else if (id == GroovyTokenId.RBRACE) {
                    balance--;
                }
            } while (ts.moveNext() && (ts.offset() <= end));

            return balance;
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);

            return 0;
        }
    }

    /** Compute the balance of begin/end tokens on the line */
    public static int getLineBalance(BaseDocument doc, int offset, TokenId up, TokenId down) {
        try {
            int begin = Utilities.getRowStart(doc, offset);
            int end = Utilities.getRowEnd(doc, offset);

            TokenSequence<?extends GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, begin);
            if (ts == null) {
                return 0;
            }

            ts.move(begin);

            if (!ts.moveNext()) {
                return 0;
            }

            int balance = 0;

            do {
                Token<?extends GroovyTokenId> token = ts.token();
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

    /**
     * The same as braceBalance but generalized to any pair of matching
     * tokens.
     * @param open the token that increses the count
     * @param close the token that decreses the count
     */
    public static int getTokenBalance(BaseDocument doc, TokenId open, TokenId close, int offset)
        throws BadLocationException {
        TokenSequence<?extends GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, 0);
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
     * Return true iff the line for the given offset is a comment line.
     * This will return false for lines that contain comments (even when the
     * offset is within the comment portion) but also contain code.
     */
    public static boolean isCommentOnlyLine(BaseDocument doc, int offset)
        throws BadLocationException {
        int begin = Utilities.getRowFirstNonWhite(doc, offset);

        if (begin == -1) {
            return false; // whitespace only
        }

        if (begin == doc.getLength()) {
            return false;
        }

        return false;//doc.getText(begin, 2).equals("//") || doc.getText(begin, 1).equals("*");
    }

    /**
     * Return the string at the given position, or null if none
     */
    @SuppressWarnings("unchecked")
    public static String getStringAt(int caretOffset, TokenHierarchy<Document> th) {
        TokenSequence<?extends GroovyTokenId> ts = getGroovyTokenSequence(th, caretOffset);

        if (ts == null) {
            return null;
        }

        ts.move(caretOffset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }

        if (ts.offset() == caretOffset) {
            // We're looking at the offset to the RIGHT of the caret
            // and here I care about what's on the left
            ts.movePrevious();
        }

        Token<?extends GroovyTokenId> token = ts.token();

        if (token != null) {
            TokenId id = token.id();

            // We're within a String that has embedded Groovy. Drop into the
            // embedded language and see if we're within a literal string there.
            if (id == GroovyTokenId.EMBEDDED_GROOVY) {
                ts = (TokenSequence)ts.embedded();
                assert ts != null;
                ts.move(caretOffset);

                if (!ts.moveNext() && !ts.movePrevious()) {
                    return null;
                }

                token = ts.token();
                id = token.id();
            }

            String string = null;

            // Skip over embedded Groovy segments and literal strings until you find the beginning
            int segments = 0;

            while ((id == GroovyTokenId.ERROR) || (id == GroovyTokenId.STRING_LITERAL) ||
                    (id == GroovyTokenId.QUOTED_STRING_LITERAL) || (id == GroovyTokenId.EMBEDDED_GROOVY)) {
                string = token.text().toString();
                segments++;
                ts.movePrevious();
                token = ts.token();
                id = token.id();
            }

            if ((id == GroovyTokenId.STRING_BEGIN) || (id == GroovyTokenId.QUOTED_STRING_BEGIN)) {
                if (segments == 1) {
                    return string;
                } else {
                    // Build up the String from the sequence
                    StringBuilder sb = new StringBuilder();

                    while (ts.moveNext()) {
                        token = ts.token();
                        id = token.id();

                        if ((id == GroovyTokenId.ERROR) || (id == GroovyTokenId.STRING_LITERAL) ||
                                (id == GroovyTokenId.QUOTED_STRING_LITERAL) ||
                                (id == GroovyTokenId.EMBEDDED_GROOVY)) {
                            sb.append(token.text());
                        } else {
                            break;
                        }
                    }

                    return sb.toString();
                }
            }
        }

        return null;
    }

    public static int getSingleQuotedStringOffset(int caretOffset, TokenHierarchy<Document> th) {
        return getLiteralStringOffset(caretOffset, th, GroovyTokenId.STRING_BEGIN);
    }

    public static int getDoubleQuotedStringOffset(int caretOffset, TokenHierarchy<Document> th) {
        return getLiteralStringOffset(caretOffset, th, GroovyTokenId.QUOTED_STRING_BEGIN);
    }

    public static int getRegexpOffset(int caretOffset, TokenHierarchy<Document> th) {
        return getLiteralStringOffset(caretOffset, th, GroovyTokenId.REGEXP_BEGIN);
    }

    /**
     * Determine if the caret is inside a literal string, and if so, return its starting
     * offset. Return -1 otherwise.
     */
    @SuppressWarnings("unchecked")
    private static int getLiteralStringOffset(int caretOffset, TokenHierarchy<Document> th,
        GroovyTokenId begin) {
        TokenSequence<?extends GroovyTokenId> ts = getGroovyTokenSequence(th, caretOffset);

        if (ts == null) {
            return -1;
        }

        ts.move(caretOffset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return -1;
        }

        if (ts.offset() == caretOffset) {
            // We're looking at the offset to the RIGHT of the caret
            // and here I care about what's on the left
            ts.movePrevious();
        }

        Token<?extends GroovyTokenId> token = ts.token();

        if (token != null) {
            TokenId id = token.id();

            // We're within a String that has embedded Groovy. Drop into the
            // embedded language and see if we're within a literal string there.
            if (id == GroovyTokenId.EMBEDDED_GROOVY) {
                ts = (TokenSequence)ts.embedded();
                assert ts != null;
                ts.move(caretOffset);

                if (!ts.moveNext() && !ts.movePrevious()) {
                    return -1;
                }

                token = ts.token();
                id = token.id();
            }

            // Skip over embedded Groovy segments and literal strings until you find the beginning
            while ((id == GroovyTokenId.ERROR) || (id == GroovyTokenId.STRING_LITERAL) ||
                    (id == GroovyTokenId.QUOTED_STRING_LITERAL) ||
                    (id == GroovyTokenId.REGEXP_LITERAL) || (id == GroovyTokenId.EMBEDDED_GROOVY)) {
                ts.movePrevious();
                token = ts.token();
                id = token.id();
            }

            if (id == begin) {
                if (!ts.moveNext()) {
                    return -1;
                }

                return ts.offset();
            }
        }

        return -1;
    }

    public static boolean isInsideQuotedString(BaseDocument doc, int offset) {
        TokenSequence<?extends GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, offset);

        if (ts == null) {
            return false;
        }

        ts.move(offset);

        if (ts.moveNext()) {
            Token<?extends GroovyTokenId> token = ts.token();
            TokenId id = token.id();
            if (id == GroovyTokenId.QUOTED_STRING_LITERAL || id == GroovyTokenId.QUOTED_STRING_END) {
                return true;
            }
        }
        if (ts.movePrevious()) {
            Token<?extends GroovyTokenId> token = ts.token();
            TokenId id = token.id();
            if (id == GroovyTokenId.QUOTED_STRING_LITERAL || id == GroovyTokenId.QUOTED_STRING_BEGIN) {
                return true;
            }
        }
        
        return false;
    }

    public static boolean isInsideRegexp(BaseDocument doc, int offset) {
        TokenSequence<?extends GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, offset);

        if (ts == null) {
            return false;
        }

        ts.move(offset);

        if (ts.moveNext()) {
            Token<?extends GroovyTokenId> token = ts.token();
            TokenId id = token.id();
            if (id == GroovyTokenId.REGEXP_LITERAL || id == GroovyTokenId.REGEXP_END) {
                return true;
            }
        }
        if (ts.movePrevious()) {
            Token<?extends GroovyTokenId> token = ts.token();
            TokenId id = token.id();
            if (id == GroovyTokenId.REGEXP_LITERAL || id == GroovyTokenId.REGEXP_BEGIN) {
                return true;
            }
        }
        
        return false;
    }
    
    public static OffsetRange getCommentBlock(BaseDocument doc, int caretOffset) {
        // Check if the caret is within a comment, and if so insert a new
        // leaf "node" which contains the comment line and then comment block
        try {
            Token<?extends GroovyTokenId> token = LexUtilities.getToken(doc, caretOffset);

            if ((token != null) && (token.id() == GroovyTokenId.LINE_COMMENT)) {
                // First add a range for the current line
                int begin = Utilities.getRowStart(doc, caretOffset);
                int end = Utilities.getRowEnd(doc, caretOffset);

                if (LexUtilities.isCommentOnlyLine(doc, caretOffset)) {

                    while (begin > 0) {
                        int newBegin = Utilities.getRowStart(doc, begin - 1);

                        if ((newBegin < 0) || !LexUtilities.isCommentOnlyLine(doc, newBegin)) {
                            begin = Utilities.getRowFirstNonWhite(doc, begin);
                            break;
                        }

                        begin = newBegin;
                    }

                    int length = doc.getLength();

                    while (true) {
                        int newEnd = Utilities.getRowEnd(doc, end + 1);

                        if ((newEnd >= length) || !LexUtilities.isCommentOnlyLine(doc, newEnd)) {
                            end = Utilities.getRowLastNonWhite(doc, end)+1;
                            break;
                        }

                        end = newEnd;
                    }

                    if (begin < end) {
                        return new OffsetRange(begin, end);
                    }
                } else {
                    // It's just a line comment next to some code
                    TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
                    int offset = token.offset(th);
                    return new OffsetRange(offset, offset + token.length());
                }
            } else if (token != null && token.id() == GroovyTokenId.DOCUMENTATION) {
                // Select the whole token block
                TokenHierarchy<BaseDocument> th = TokenHierarchy.get(doc);
                int begin = token.offset(th);
                int end = begin + token.length();
                return new OffsetRange(begin, end);
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
        
        return OffsetRange.NONE;
    }

    /**
     * Back up to the first space character prior to the given offset - as long as 
     * it's on the same line!  If there's only leading whitespace on the line up
     * to the lex offset, return the offset itself 
     */
    public static int findSpaceBegin(BaseDocument doc, int lexOffset) {
        TokenSequence ts = LexUtilities.getGroovyTokenSequence(doc, lexOffset);
        if (ts == null) {
            return lexOffset;
        }
        boolean allowPrevLine = false;
        int lineStart;
        try {
            lineStart = Utilities.getRowStart(doc, Math.min(lexOffset, doc.getLength()));
            int prevLast = lineStart-1;
            if (lineStart > 0) {
                prevLast = Utilities.getRowLastNonWhite(doc, lineStart-1);
                if (prevLast != -1) {
                    char c = doc.getText(prevLast, 1).charAt(0);
                    if (c == ',') {
                        // Arglist continuation? // TODO : check lexing
                        allowPrevLine = true;
                    }
                }
            }
            if (!allowPrevLine) {
                int firstNonWhite = Utilities.getRowFirstNonWhite(doc, lineStart);
                if (lexOffset <= firstNonWhite || firstNonWhite == -1) {
                    return lexOffset;
                }
            } else {
                // Make lineStart so small that Math.max won't cause any problems
                int firstNonWhite = Utilities.getRowFirstNonWhite(doc, lineStart);
                if (prevLast >= 0 && (lexOffset <= firstNonWhite || firstNonWhite == -1)) {
                    return prevLast+1;
                }
                lineStart = 0;
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
            return lexOffset;
        }
        ts.move(lexOffset);
        if (ts.moveNext()) {
            if (lexOffset > ts.offset()) {
                // We're in the middle of a token
                return Math.max((ts.token().id() == GroovyTokenId.WHITESPACE) ?
                    ts.offset() : lexOffset, lineStart);
            }
            while (ts.movePrevious()) {
                Token token = ts.token();
                if (token.id() != GroovyTokenId.WHITESPACE) {
                    return Math.max(ts.offset() + token.length(), lineStart);
                }
            }
        }
        
        return lexOffset;
    }
    
    public static Token<?extends GroovyTokenId> findPreviousNonWsNonComment(TokenSequence<?extends GroovyTokenId> ts) {
        return findPrevious(ts, WHITESPACES_AND_COMMENTS);
    }
    
    private static Token<?extends GroovyTokenId> findPrevious(TokenSequence<?extends GroovyTokenId> ts, List<GroovyTokenId> ignores) {
        ts.movePrevious();
        if (ignores.contains(ts.token().id())) {
            while (ts.movePrevious() && ignores.contains(ts.token().id())) {}
        }
        return ts.token();
    }

}
