/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.ruby.lexer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.gsf.CompilationInfo;

import org.netbeans.api.gsf.GsfTokenId;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.gsf.annotations.NonNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
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

    static {
        END_PAIRS.add(RubyTokenId.BEGIN);
        END_PAIRS.add(RubyTokenId.FOR);
        END_PAIRS.add(RubyTokenId.CLASS);
        END_PAIRS.add(RubyTokenId.DEF);
        END_PAIRS.add(RubyTokenId.DO);
        END_PAIRS.add(RubyTokenId.WHILE);
        END_PAIRS.add(RubyTokenId.IF);
        END_PAIRS.add(RubyTokenId.CLASS);
        END_PAIRS.add(RubyTokenId.MODULE);
        END_PAIRS.add(RubyTokenId.CASE);
        END_PAIRS.add(RubyTokenId.LOOP);
        END_PAIRS.add(RubyTokenId.UNTIL);
        END_PAIRS.add(RubyTokenId.UNLESS);

        INDENT_WORDS.addAll(END_PAIRS);
        // Add words that are not matched themselves with an "end",
        // but which also provide block structure to indented content
        // (usually part of a multi-keyword structure such as if-then-elsif-else-end
        // where only the "if" is considered an end-pair.)
        INDENT_WORDS.add(RubyTokenId.ELSE);
        INDENT_WORDS.add(RubyTokenId.ELSIF);
        INDENT_WORDS.add(RubyTokenId.ENSURE);
        INDENT_WORDS.add(RubyTokenId.WHEN);
        INDENT_WORDS.add(RubyTokenId.RESCUE);

        // XXX What about BEGIN{} and END{} ?
    }

    /** For a possibly generated offset in an AST, return the corresponding lexing/true document offset */
    public static int getLexerOffset(CompilationInfo info, int astOffset) {
        if (info.getEmbeddingModel() != null) {
            return info.getEmbeddingModel().generatedToSourcePos(info.getFileObject(), astOffset);
        }
        
        return astOffset;
    }

    private LexUtilities() {
    }

    /** Find the ruby token sequence (in case it's embedded in something else at the top level */
    @SuppressWarnings("unchecked")
    public static TokenSequence<?extends GsfTokenId> getRubyTokenSequence(BaseDocument doc, int offset) {
        TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
        return getRubyTokenSequence(th, offset);
    }
    
    @SuppressWarnings("unchecked")
    public static TokenSequence<?extends GsfTokenId> getRubyTokenSequence(TokenHierarchy<Document> th, int offset) {
        TokenSequence<?extends GsfTokenId> ts = th.tokenSequence(RubyTokenId.language());

        if (ts == null) {
            // Possibly an embedding scenario such as an RHTML file
            // First try with backward bias true
            List<TokenSequence<?extends TokenId>> list = th.embeddedTokenSequences(offset, true);

            for (TokenSequence t : list) {
                if (t.language() == RubyTokenId.language()) {
                    ts = t;

                    break;
                }
            }

            if (ts == null) {
                list = th.embeddedTokenSequences(offset, false);

                for (TokenSequence t : list) {
                    if (t.language() == RubyTokenId.language()) {
                        ts = t;

                        break;
                    }
                }

            }
        }

        return ts;
    }

    public static Token<?extends GsfTokenId> getToken(BaseDocument doc, int offset) {
        TokenSequence<?extends GsfTokenId> ts = getRubyTokenSequence(doc, offset);

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

            Token<?extends GsfTokenId> token = ts.token();

            return token;
        }

        return null;
    }

    public static char getTokenChar(BaseDocument doc, int offset) {
        Token<?extends GsfTokenId> token = getToken(doc, offset);

        if (token != null) {
            String text = token.text().toString();

            if (text.length() > 0) { // Usually true, but I could have gotten EOF right?

                return text.charAt(0);
            }
        }

        return 0;
    }

    /** Search forwards in the token sequence until a token of type <code>down</code> is found */
    public static OffsetRange findHeredocEnd(TokenSequence<?extends GsfTokenId> ts,  Token<?extends GsfTokenId> startToken) {
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
            Token<?extends GsfTokenId> token = ts.token();
            TokenId id = token.id();

            if (id == RubyTokenId.STRING_END || id == RubyTokenId.QUOTED_STRING_END) {
                String t = token.text().toString();
                if (text.equals(t) || textn.equals(t)) {
                    return new OffsetRange(ts.offset(), ts.offset() + token.length());
                }
            }
        }

        return OffsetRange.NONE;
    }

    /** Search forwards in the token sequence until a token of type <code>down</code> is found */
    public static OffsetRange findHeredocBegin(TokenSequence<?extends GsfTokenId> ts,  Token<?extends GsfTokenId> endToken) {
        // Look for the end of the given heredoc
        String text = endToken.text().toString();
        if (text.endsWith("\n")) {
            text = text.substring(0, text.length()-1);
        }
        String textQuotes = "\"" + text + "\"";
        String textSQuotes = "'" + text + "'";

        while (ts.movePrevious()) {
            Token<?extends GsfTokenId> token = ts.token();
            TokenId id = token.id();

            if (id == RubyTokenId.STRING_BEGIN || id == RubyTokenId.QUOTED_STRING_BEGIN) {
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
    public static OffsetRange findFwd(BaseDocument doc, TokenSequence<?extends GsfTokenId> ts, TokenId up,
        TokenId down) {
        int balance = 0;

        while (ts.moveNext()) {
            Token<?extends GsfTokenId> token = ts.token();
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
    public static OffsetRange findBwd(BaseDocument doc, TokenSequence<?extends GsfTokenId> ts, TokenId up,
        TokenId down) {
        int balance = 0;

        while (ts.movePrevious()) {
            Token<?extends GsfTokenId> token = ts.token();
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
    public static OffsetRange findBegin(BaseDocument doc, TokenSequence<?extends GsfTokenId> ts) {
        int balance = 0;

        while (ts.movePrevious()) {
            Token<?extends GsfTokenId> token = ts.token();
            TokenId id = token.id();

            if (isBeginToken(id, doc, ts)) {
                // No matching dot for "do" used in conditionals etc.)) {
                if (balance == 0) {
                    return new OffsetRange(ts.offset(), ts.offset() + token.length());
                }

                balance--;
            } else if (id == RubyTokenId.END) {
                balance++;
            }
        }

        return OffsetRange.NONE;
    }

    public static OffsetRange findEnd(BaseDocument doc, TokenSequence<?extends GsfTokenId> ts) {
        int balance = 0;

        while (ts.moveNext()) {
            Token<?extends GsfTokenId> token = ts.token();
            TokenId id = token.id();

            if (isBeginToken(id, doc, ts)) {
                balance--;
            } else if (id == RubyTokenId.END) {
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
                Token<? extends GsfTokenId> token = getToken(doc, first);
                if (token != null) {
                    TokenId id = token.id();
                    if (id == RubyTokenId.WHILE || id == RubyTokenId.UNTIL || id == RubyTokenId.FOR) {
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
        if (id == RubyTokenId.DO) {
            return isEndmatchingDo(doc, offset);
        }
        return END_PAIRS.contains(id);
    }

    /**
     * Return true iff the given token is a token that should be matched
     * with a corresponding "end" token, such as "begin", "def", "module",
     * etc.
     */
    public static boolean isBeginToken(TokenId id, BaseDocument doc, TokenSequence<?extends GsfTokenId> ts) {
        if (id == RubyTokenId.DO) {
            return isEndmatchingDo(doc, ts.offset());
        }
        return END_PAIRS.contains(id);
    }
    
    /**
     * Return true iff the given token is a token that indents its content,
     * such as the various begin tokens as well as "else", "when", etc.
     */
    public static boolean isIndentToken(TokenId id) {
        return INDENT_WORDS.contains(id);
    }

    /** Compute the balance of begin/end tokens on the line */
    public static int getBeginEndLineBalance(BaseDocument doc, int offset) {
        try {
            int begin = Utilities.getRowStart(doc, offset);
            int end = Utilities.getRowEnd(doc, offset);

            TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, begin);
            if (ts == null) {
                return 0;
            }

            ts.move(begin);

            if (!ts.moveNext()) {
                return 0;
            }

            int balance = 0;

            do {
                Token<?extends GsfTokenId> token = ts.token();
                TokenId id = token.id();

                if (isBeginToken(id, doc, ts)) {
                    balance++;
                } else if (id == RubyTokenId.END) {
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

            TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, begin);
            if (ts == null) {
                return 0;
            }

            ts.move(begin);

            if (!ts.moveNext()) {
                return 0;
            }

            int balance = 0;

            do {
                Token<?extends GsfTokenId> token = ts.token();
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
        TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, 0);
        if (ts == null) {
            return 0;
        }

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

    public static int getLineIndent(BaseDocument doc, int offset) {
        try {
            int start = Utilities.getRowStart(doc, offset);
            int end;

            if (Utilities.isRowWhite(doc, start)) {
                end = Utilities.getRowEnd(doc, offset);
            } else {
                end = Utilities.getRowFirstNonWhite(doc, start);
            }

            int indent = Utilities.getVisualColumn(doc, end);

            return indent;
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);

            return 0;
        }
    }

    public static void indent(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append(' ');
        }
    }

    public static String getIndentString(int indent) {
        StringBuilder sb = new StringBuilder(indent);
        indent(sb, indent);

        return sb.toString();
    }

    /**
     * Return true iff the line for the given offset is a Ruby comment line.
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

        return doc.getText(begin, 1).equals("#");
    }

    public static void adjustLineIndentation(BaseDocument doc, int offset, int adjustment) {
        try {
            int lineBegin = Utilities.getRowStart(doc, offset);

            if (adjustment > 0) {
                doc.remove(lineBegin, adjustment);
            } else if (adjustment < 0) {
                doc.insertString(adjustment, LexUtilities.getIndentString(adjustment), null);
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
    }

    /** Adjust the indentation of the line containing the given offset to the provided
     * indentation, and return the new indent.
     */
    public static int setLineIndentation(BaseDocument doc, int offset, int indent) {
        int currentIndent = getLineIndent(doc, offset);

        try {
            int lineBegin = Utilities.getRowStart(doc, offset);

            if (lineBegin == -1) {
                return currentIndent;
            }

            int adjust = currentIndent - indent;

            if (adjust > 0) {
                // Make sure that we are only removing spaces here
                String text = doc.getText(lineBegin, adjust);

                for (int i = 0; i < text.length(); i++) {
                    if (!Character.isWhitespace(text.charAt(i))) {
                        throw new RuntimeException(
                            "Illegal indentation adjustment: Deleting non-whitespace chars: " +
                            text);
                    }
                }

                doc.remove(lineBegin, adjust);
            } else if (adjust < 0) {
                adjust = -adjust;
                doc.insertString(lineBegin, getIndentString(adjust), null);
            }

            return indent;
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);

            return currentIndent;
        }
    }

    /**
     * Return the string at the given position, or null if none
     */
    @SuppressWarnings("unchecked")
    public static String getStringAt(int caretOffset, TokenHierarchy<Document> th) {
        TokenSequence<?extends GsfTokenId> ts = getRubyTokenSequence(th, caretOffset);

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

        Token<?extends GsfTokenId> token = ts.token();

        if (token != null) {
            TokenId id = token.id();

            // We're within a String that has embedded Ruby. Drop into the
            // embedded language and see if we're within a literal string there.
            if (id == RubyTokenId.EMBEDDED_RUBY) {
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

            // Skip over embedded Ruby segments and literal strings until you find the beginning
            int segments = 0;

            while ((id == RubyTokenId.ERROR) || (id == RubyTokenId.STRING_LITERAL) ||
                    (id == RubyTokenId.QUOTED_STRING_LITERAL) || (id == RubyTokenId.EMBEDDED_RUBY)) {
                string = token.text().toString();
                segments++;
                ts.movePrevious();
                token = ts.token();
                id = token.id();
            }

            if ((id == RubyTokenId.STRING_BEGIN) || (id == RubyTokenId.QUOTED_STRING_BEGIN)) {
                if (segments == 1) {
                    return string;
                } else {
                    // Build up the String from the sequence
                    StringBuilder sb = new StringBuilder();

                    while (ts.moveNext()) {
                        token = ts.token();
                        id = token.id();

                        if ((id == RubyTokenId.ERROR) || (id == RubyTokenId.STRING_LITERAL) ||
                                (id == RubyTokenId.QUOTED_STRING_LITERAL) ||
                                (id == RubyTokenId.EMBEDDED_RUBY)) {
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

    /**
     * Check if the caret is inside a literal string that is associated with
     * a require statement.
     *
     * @return The offset of the beginning of the require string, or -1
     *     if the offset is not inside a require string.
     */
    public static int getRequireStringOffset(int caretOffset, TokenHierarchy<Document> th) {
        TokenSequence<?extends GsfTokenId> ts = getRubyTokenSequence(th, caretOffset);

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

        Token<?extends GsfTokenId> token = ts.token();

        if (token != null) {
            TokenId id = token.id();

            // Skip over embedded Ruby segments and literal strings until you find the beginning
            while ((id == RubyTokenId.ERROR) || (id == RubyTokenId.STRING_LITERAL) ||
                    (id == RubyTokenId.QUOTED_STRING_LITERAL) || (id == RubyTokenId.EMBEDDED_RUBY)) {
                ts.movePrevious();
                token = ts.token();
                id = token.id();
            }

            int stringStart = ts.offset() + token.length();

            if ((id == RubyTokenId.STRING_BEGIN) || (id == RubyTokenId.QUOTED_STRING_BEGIN)) {
                // Completion of literal strings within require calls
                while (ts.movePrevious()) {
                    token = ts.token();

                    id = token.id();

                    if ((id == RubyTokenId.WHITESPACE) || (id == RubyTokenId.LPAREN) ||
                            (id == RubyTokenId.STRING_LITERAL) ||
                            (id == RubyTokenId.QUOTED_STRING_LITERAL)) {
                        continue;
                    }

                    if (id == RubyTokenId.IDENTIFIER) {
                        String text = token.text().toString();

                        if (text.equals("require") || text.equals("load")) {
                            return stringStart;
                        } else {
                            return -1;
                        }
                    } else {
                        return -1;
                    }
                }
            }
        }

        return -1;
    }

    public static int getSingleQuotedStringOffset(int caretOffset, TokenHierarchy<Document> th) {
        return getLiteralStringOffset(caretOffset, th, RubyTokenId.STRING_BEGIN);
    }

    public static int getDoubleQuotedStringOffset(int caretOffset, TokenHierarchy<Document> th) {
        return getLiteralStringOffset(caretOffset, th, RubyTokenId.QUOTED_STRING_BEGIN);
    }

    public static int getRegexpOffset(int caretOffset, TokenHierarchy<Document> th) {
        return getLiteralStringOffset(caretOffset, th, RubyTokenId.REGEXP_BEGIN);
    }

    /**
     * Determine if the caret is inside a literal string, and if so, return its starting
     * offset. Return -1 otherwise.
     */
    @SuppressWarnings("unchecked")
    private static int getLiteralStringOffset(int caretOffset, TokenHierarchy<Document> th,
        GsfTokenId begin) {
        TokenSequence<?extends GsfTokenId> ts = getRubyTokenSequence(th, caretOffset);

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

        Token<?extends GsfTokenId> token = ts.token();

        if (token != null) {
            TokenId id = token.id();

            // We're within a String that has embedded Ruby. Drop into the
            // embedded language and see if we're within a literal string there.
            if (id == RubyTokenId.EMBEDDED_RUBY) {
                ts = (TokenSequence)ts.embedded();
                assert ts != null;
                ts.move(caretOffset);

                if (!ts.moveNext() && !ts.movePrevious()) {
                    return -1;
                }

                token = ts.token();
                id = token.id();
            }

            // Skip over embedded Ruby segments and literal strings until you find the beginning
            while ((id == RubyTokenId.ERROR) || (id == RubyTokenId.STRING_LITERAL) ||
                    (id == RubyTokenId.QUOTED_STRING_LITERAL) ||
                    (id == RubyTokenId.REGEXP_LITERAL) || (id == RubyTokenId.EMBEDDED_RUBY)) {
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

    /**
     * Determine whether the given offset corresponds to a method call on another
     * object. This would happen in these cases:
     *    Foo::|, Foo::Bar::|, Foo.|, Foo.x|, foo.|, foo.x|
     * and not here:
     *   |, Foo|, foo|
     * The method returns the left hand side token, if any, such as "Foo", Foo::Bar",
     * and "foo". If not, it will return null.
     * Note that "self" and "super" are possible return values for the lhs, which mean
     * that you don't have a call on another object. Clients of this method should
     * handle that return value properly (I could return null here, but clients probably
     * want to distinguish self and super in this case so it's useful to return the info.)
     *
     * This method will also try to be smart such that if you have a block or array
     * call, it will return the relevant classnames (e.g. for [1,2].x| it returns "Array").
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public static Call getCallType(BaseDocument doc, TokenHierarchy<Document> th, int offset) {
        TokenSequence<?extends GsfTokenId> ts = getRubyTokenSequence(th, offset);

        if (ts == null) {
            return Call.NONE;
        }

        ts.move(offset);

        boolean methodExpected = false;

        if (!ts.moveNext() && !ts.movePrevious()) {
            return Call.NONE;
        }

        if (ts.offset() == offset) {
            // We're looking at the offset to the RIGHT of the caret
            // position, which could be whitespace, e.g.
            //  "foo.x| " <-- looking at the whitespace
            ts.movePrevious();
        }

        Token<?extends GsfTokenId> token = ts.token();

        if (token != null) {
            TokenId id = token.id();

            if (id == RubyTokenId.WHITESPACE) {
                return Call.LOCAL;
            }

            // We're within a String that has embedded Ruby. Drop into the
            // embedded language and iterate the ruby tokens there.
            if (id == RubyTokenId.EMBEDDED_RUBY) {
                ts = (TokenSequence)ts.embedded();
                assert ts != null;
                ts.move(offset);

                if (!ts.moveNext() && !ts.movePrevious()) {
                    return Call.NONE;
                }

                token = ts.token();
                id = token.id();
            }

            // See if we're in the identifier - "x" in "foo.x"
            // I could also be a keyword in case the prefix happens to currently
            // match a keyword, such as "next"
            // However, if we're at the end of the document, x. will lex . as an
            // identifier of text ".", so handle this case specially
            if ((id == RubyTokenId.IDENTIFIER) || (id == RubyTokenId.CONSTANT) ||
                    id.primaryCategory().equals("keyword")) {
                String tokenText = token.text().toString();

                if (".".equals(tokenText)) {
                    // Special case - continue - we'll handle this part next
                    methodExpected = true;
                } else if ("::".equals(tokenText)) {
                    // Special case - continue - we'll handle this part next
                } else {
                    methodExpected = true;

                    if (Character.isUpperCase(tokenText.charAt(0))) {
                        methodExpected = false;
                    }

                    if (!ts.movePrevious()) {
                        return Call.LOCAL;
                    }
                }

                token = ts.token();
                id = token.id();
            }

            // If we're not in the identifier we need to be in the dot (in "foo.x").
            // I can't just check for tokens DOT and COLON3 because for unparseable source
            // (like "File.|") the lexer will return the "." as an identifier.
            if (id == RubyTokenId.DOT) {
                methodExpected = true;
            } else if (id == RubyTokenId.COLON3) {
            } else if (id == RubyTokenId.IDENTIFIER) {
                String t = token.text().toString();

                if (t.equals(".")) {
                    methodExpected = true;
                } else if (!t.equals("::")) {
                    return Call.LOCAL;
                }
            } else {
                return Call.LOCAL;
            }

            int lastSeparatorOffset = ts.offset();
            int beginOffset = lastSeparatorOffset;
            int lineStart = 0;

            try {
                if (offset > doc.getLength()) {
                    offset = doc.getLength();
                }

                lineStart = Utilities.getRowStart(doc, offset);
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            }

            // Find the beginning of the expression. We'll go past keywords, identifiers
            // and dots or double-colons
            while (ts.movePrevious()) {
                // If we get to the previous line we're done
                if (ts.offset() < lineStart) {
                    break;
                }

                token = ts.token();
                id = token.id();

                if (id == RubyTokenId.WHITESPACE) {
                    break;
                } else if (id == RubyTokenId.RBRACKET) {
                    // Looks like we're operating on an array, e.g.
                    //  [1,2,3].each|
                    return new Call("Array", null, false, methodExpected);
                } else if (id == RubyTokenId.RBRACE) { // XXX uh oh, what about blocks?  {|x|printx}.| ? type="Proc"
                                                       // Looks like we're operating on a hash, e.g.
                                                       //  {1=>foo,2=>bar}.each|

                    return new Call("Hash", null, false, methodExpected);
                } else if ((id == RubyTokenId.STRING_END) || (id == RubyTokenId.QUOTED_STRING_END)) {
                    return new Call("String", null, false, methodExpected);
                } else if (id == RubyTokenId.REGEXP_END) {
                    return new Call("Regexp", null, false, methodExpected);
                } else if (id == RubyTokenId.INT_LITERAL) {
                    return new Call("Fixnum", null, false, methodExpected); // Or Bignum?
                } else if (id == RubyTokenId.FLOAT_LITERAL) {
                    return new Call("Float", null, false, methodExpected);
                } else if (id == RubyTokenId.TYPE_SYMBOL) {
                    return new Call("Symbol", null, false, methodExpected);
                } else if (id == RubyTokenId.RANGE) {
                    return new Call("Range", null, false, methodExpected);
                } else if ((id == RubyTokenId.ANY_KEYWORD) && (token.text().equals("nil"))) {
                    return new Call("NilClass", null, false, methodExpected);
                } else if ((id == RubyTokenId.ANY_KEYWORD) && (token.text().equals("true"))) {
                    return new Call("TrueClass", null, false, methodExpected);
                } else if ((id == RubyTokenId.ANY_KEYWORD) && (token.text().equals("false"))) {
                    return new Call("FalseClass", null, false, methodExpected);
                } else if (((id == RubyTokenId.GLOBAL_VAR) || (id == RubyTokenId.INSTANCE_VAR) ||
                        (id == RubyTokenId.CLASS_VAR) || (id == RubyTokenId.IDENTIFIER)) ||
                        id.primaryCategory().equals("keyword") || (id == RubyTokenId.DOT) ||
                        (id == RubyTokenId.COLON3) || (id == RubyTokenId.CONSTANT) ||
                        (id == RubyTokenId.SUPER) || (id == RubyTokenId.SELF)) {
                    // We're building up a potential expression such as "Test::Unit" so continue looking
                    beginOffset = ts.offset();

                    continue;
                } else if ((id == RubyTokenId.LPAREN) || (id == RubyTokenId.LBRACE) ||
                        (id == RubyTokenId.LBRACKET)) {
                    // It's an expression for example within a parenthesis, e.g.
                    // yield(^File.join())
                    // in this case we can do top level completion
                    // TODO: There are probably more valid contexts here
                    break;
                } else {
                    // Something else - such as "getFoo().x|" - at this point we don't know the type
                    // so we'll just return unknown
                    return Call.UNKNOWN;
                }
            }

            if (beginOffset < lastSeparatorOffset) {
                try {
                    String lhs = doc.getText(beginOffset, lastSeparatorOffset - beginOffset);

                    if (lhs.equals("super") || lhs.equals("self")) { // NOI18N

                        return new Call(lhs, lhs, false, true);
                    } else if (Character.isUpperCase(lhs.charAt(0))) {
                        return new Call(lhs, lhs, true, methodExpected);
                    } else {
                        return new Call(null, lhs, false, methodExpected);
                    }
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                }
            } else {
                return Call.UNKNOWN;
            }
        }

        return Call.LOCAL;
    }

    public static class Call {
        /**
         * The call is local: there is no left hand side expression; it's
         * either a local call or an inherited or mixed-in call
         */
        public static final Call LOCAL = new Call(null, null, false, false);

        /**
         * Doesn't look like a call
         */
        public static final Call NONE = new Call(null, null, false, false);

        /**
         * The call has a complicated LHS (involving for example parentheses).
         * The full LHS is not computed.
         */
        public static final Call UNKNOWN = new Call(null, null, false, false);
        private final String type;
        private final String lhs;
        private final boolean isStatic;
        private final boolean methodExpected;

        public Call(String type, String lhs, boolean isStatic, boolean methodExpected) {
            this.type = type;
            this.lhs = lhs;
            this.methodExpected = methodExpected;

            if (lhs == null) {
                lhs = type;
            }

            this.isStatic = isStatic;
        }

        public String getType() {
            return type;
        }

        public String getLhs() {
            return lhs;
        }

        public boolean isStatic() {
            return isStatic;
        }

        /** Return true if the lhs is a simple identifier */
        public boolean isSimpleIdentifier() {
            if (lhs == null) {
                return false;
            }

            for (int i = 0, n = lhs.length(); i < n; i++) {
                char c = lhs.charAt(i);

                if (Character.isJavaIdentifierPart(c)) {
                    continue;
                }

                if ((c == '@') || (c == '$')) {
                    continue;
                }

                return false;
            }

            return true;
        }

        public String toString() {
            if (this == LOCAL) {
                return "LOCAL";
            } else if (this == NONE) {
                return "NONE";
            } else if (this == UNKNOWN) {
                return "UNKNOWN";
            } else {
                return "Call(" + type + "," + lhs + "," + isStatic + ")";
            }
        }

        /** Return true iff we believe this is going to call a method. If we don't know,
         * e.g. Test::, then return false */
        public boolean isMethodExpected() {
            return this.methodExpected;
        }
    }

    public static boolean isInsideQuotedString(BaseDocument doc, int offset) {
        TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, offset);

        if (ts == null) {
            return false;
        }

        ts.move(offset);

        if (ts.moveNext()) {
            Token<?extends GsfTokenId> token = ts.token();
            TokenId id = token.id();
            if (id == RubyTokenId.QUOTED_STRING_LITERAL || id == RubyTokenId.QUOTED_STRING_END) {
                return true;
            }
        }
        if (ts.movePrevious()) {
            Token<?extends GsfTokenId> token = ts.token();
            TokenId id = token.id();
            if (id == RubyTokenId.QUOTED_STRING_LITERAL || id == RubyTokenId.QUOTED_STRING_BEGIN) {
                return true;
            }
        }
        
        return false;
    }
}
