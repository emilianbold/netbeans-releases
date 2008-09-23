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
package org.netbeans.modules.php.editor.lexer;

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
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.php.editor.indent.PHPBracketCompleter.LineBalance;
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
    private static final Set<PHPTokenId> INDENT_BEGIN_TOKENS = new HashSet<PHPTokenId>();
    private static final Set<PHPTokenId> INDENT_END_TOKENS = new HashSet<PHPTokenId>();

//    /**
//     * Tokens that should cause indentation of the next line. This is true for all {@link #END_PAIRS},
//     * but also includes tokens like "else" that are not themselves matched with end but also contribute
//     * structure for indentation.
//     *
//     */
//    private static final Set<TokenId> INDENT_WORDS = new HashSet<TokenId>();

    static {
//        INDENT_BEGIN_TOKENS.add(PHPTokenId.PHP_OPENTAG);
//        INDENT_BEGIN_TOKENS.add(PHPTokenId.PHP_DECLARE);
//        INDENT_BEGIN_TOKENS.add(PHPTokenId.PHP_FOR);
//        INDENT_BEGIN_TOKENS.add(PHPTokenId.PHP_FOREACH);
//        INDENT_BEGIN_TOKENS.add(PHPTokenId.PHP_IF);
//        INDENT_BEGIN_TOKENS.add(PHPTokenId.PHP_SWITCH);
//        INDENT_BEGIN_TOKENS.add(PHPTokenId.PHP_WHILE);

//        INDENT_END_TOKENS.add(PHPTokenId.PHP_CLOSETAG);
//        INDENT_END_TOKENS.add(PHPTokenId.PHP_ENDDECLARE);
//        INDENT_END_TOKENS.add(PHPTokenId.PHP_ENDFOR);
//        INDENT_END_TOKENS.add(PHPTokenId.PHP_ENDFOREACH);
//        INDENT_END_TOKENS.add(PHPTokenId.PHP_ENDIF);
//        INDENT_END_TOKENS.add(PHPTokenId.PHP_ENDSWITCH);
//        INDENT_END_TOKENS.add(PHPTokenId.PHP_ENDWHILE);

//        // Add words that are not matched themselves with an "end",
//        // but which also provide block structure to indented content
//        // (usually part of a multi-keyword structure such as if-then-elsif-else-end
//        // where only the "if" is considered an end-pair.)
//        INDENT_WORDS.add(PHPTokenId.PHP_FOR);
//        INDENT_WORDS.add(PHPTokenId.PHP_IF);
//        INDENT_WORDS.add(PHPTokenId.PHP_ELSE);
//        INDENT_WORDS.add(PHPTokenId.PHP_WHILE);
    }

    private LexUtilities() {
    }
    
//    /** 
//     * Return the comment sequence (if any) for the comment prior to the given offset.
//     */
//    public static TokenSequence<? extends JsCommentTokenId> getCommentFor(BaseDocument doc, int offset) {
//        TokenSequence<?extends JsTokenId> jts = getJsTokenSequence(doc, offset);
//        if (jts == null) {
//            return null;
//        }
//        jts.move(offset);
//        
//        while (jts.movePrevious()) {
//            TokenId id = jts.token().id();
//            if (id == JsTokenId.BLOCK_COMMENT) {
//                return jts.embedded(JsCommentTokenId.language());
//            } else if (id != JsTokenId.WHITESPACE && id != JsTokenId.EOL) {
//                return null;
//            }
//        }
//        
//        return null;
//    }
//
//    /** For a possibly generated offset in an AST, return the corresponding lexing/true document offset */
//    public static int getLexerOffset(CompilationInfo info, int astOffset) {
//        ParserResult result = info.getEmbeddedResult(JsMimeResolver.JAVASCRIPT_MIME_TYPE, 0);
//        if (result != null) {
//            TranslatedSource ts = result.getTranslatedSource();
//            if (ts != null) {
//                return ts.getLexicalOffset(astOffset);
//            }
//        }
//        
//        return astOffset;
//    }
//    
//    public static OffsetRange getLexerOffsets(CompilationInfo info, OffsetRange astRange) {
//        ParserResult result = info.getEmbeddedResult(JsMimeResolver.JAVASCRIPT_MIME_TYPE, 0);
//        if (result != null) {
//            TranslatedSource ts = result.getTranslatedSource();
//            if (ts != null) {
//                int rangeStart = astRange.getStart();
//                int start = ts.getLexicalOffset(rangeStart);
//                if (start == rangeStart) {
//                    return astRange;
//                } else if (start == -1) {
//                    return OffsetRange.NONE;
//                } else {
//                    // Assumes the translated range maintains size
//                    return new OffsetRange(start, start+astRange.getLength());
//                }
//            }
//        }
//
//        return astRange;
//    }
//    
//    /** Find the ruby token sequence (in case it's embedded in something else at the top level */
//    @SuppressWarnings("unchecked")
//    public static TokenSequence<?extends JsTokenId> getJsTokenSequence(BaseDocument doc, int offset) {
//        TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
//        return getJsTokenSequence(th, offset);
//    }
//    
    @SuppressWarnings("unchecked")
    public static TokenSequence<PHPTokenId> getPHPTokenSequence(Document doc, int offset) {
        TokenHierarchy<Document> th = TokenHierarchy.get(doc);
        TokenSequence<PHPTokenId> ts = th == null ? null : th.tokenSequence(PHPTokenId.language());

        if (ts == null) {
            // Possibly an embedding scenario such as an RHTML file
            // First try with backward bias true
            List<TokenSequence<?>> list = th.embeddedTokenSequences(offset, true);

            for (TokenSequence t : list) {
                if (t.language() == PHPTokenId.language()) {
                    ts = t;

                    break;
                }
            }

            if (ts == null) {
                list = th.embeddedTokenSequences(offset, false);

                for (TokenSequence t : list) {
                    if (t.language() == PHPTokenId.language()) {
                        ts = t;

                        break;
                    }
                }
            }
        }

        return ts;
    }

    public static TokenSequence<?extends PHPTokenId> getPositionedSequence(BaseDocument doc, int offset) {
        TokenSequence<?extends PHPTokenId> ts = getPHPTokenSequence(doc, offset);

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
            
            return ts;
        }

        return null;
    }

    
    public static Token<?extends PHPTokenId> getToken(BaseDocument doc, int offset) {
        TokenSequence<?extends PHPTokenId> ts = getPositionedSequence(doc, offset);
        
        if (ts != null) {
            return ts.token();
        }

        return null;
    }

    public static char getTokenChar(BaseDocument doc, int offset) {
        Token<?extends PHPTokenId> token = getToken(doc, offset);

        if (token != null) {
            if (token.text().length() > 0) { // Usually true, but I could have gotten EOF right?
                return token.text().charAt(0);
            }
        }

        return 0;
    }
//
//    /**
//     * Tries to skip parenthesis 
//     */
//    static boolean skipParenthesis(TokenSequence<?extends JsTokenId> ts) {
//        int balance = 0;
//
//        Token<?extends JsTokenId> token = ts.token();
//        if (token == null) {
//            return false;
//        }
//
//        TokenId id = token.id();
//            
////        // skip whitespaces
////        if (id == JsTokenId.WHITESPACE) {
////            while (ts.moveNext() && ts.token().id() == JsTokenId.WHITESPACE) {}
////        }
//        if (id == JsTokenId.WHITESPACE || id == JsTokenId.EOL) {
//            while (ts.moveNext() && (ts.token().id() == JsTokenId.WHITESPACE || ts.token().id() == JsTokenId.EOL)) {}
//        }
//
//        // if current token is not left parenthesis
//        if (ts.token().id() != JsTokenId.LPAREN) {
//            return false;
//        }
//
//        do {
//            token = ts.token();
//            id = token.id();
//
//            if (id == JsTokenId.LPAREN) {
//                balance++;
//            } else if (id == JsTokenId.RPAREN) {
//                if (balance == 0) {
//                    return false;
//                } else if (balance == 1) {
//                    int length = ts.offset() + token.length();
//                    ts.moveNext();
//                    return true;
//                }
//
//                balance--;
//            }
//        } while (ts.moveNext());
//
//        return false;
//    }
    
    /** Search forwards in the token sequence until a token of type <code>down</code> is found */
    public static OffsetRange findFwd(BaseDocument doc, TokenSequence<?extends PHPTokenId> ts, char up, char down) {
        int balance = 0;

        while (ts.moveNext()) {
            Token<?extends PHPTokenId> token = ts.token();
            
            if (textEquals(token.text(), up)) {
                balance++;
            } else if (textEquals(token.text(), down)) {
                if (balance == 0) {
                    return new OffsetRange(ts.offset(), ts.offset() + token.length());
                }

                balance--;
            }
        }

        return OffsetRange.NONE;
    }

    /** Search backwards in the token sequence until a token of type <code>up</code> is found */
    public static OffsetRange findBwd(BaseDocument doc, TokenSequence<?extends PHPTokenId> ts, char up, char down) {
        int balance = 0;

        while (ts.movePrevious()) {
            Token<?extends PHPTokenId> token = ts.token();
            TokenId id = token.id();

            if (textEquals(token.text(), up)) {
                if (balance == 0) {
                    return new OffsetRange(ts.offset(), ts.offset() + token.length());
                }

                balance++;
            } else if (textEquals(token.text(), down)) {
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
    public static OffsetRange findBegin(BaseDocument doc, TokenSequence<?extends PHPTokenId> ts) {
// XXX: ressurect
//        int balance = 0;
//
//        while (ts.movePrevious()) {
//            Token<?extends JsTokenId> token = ts.token();
//            TokenId id = token.id();
//
//            if (isBeginToken(id, doc, ts)) {
//                // No matching dot for "do" used in conditionals etc.)) {
//                if (balance == 0) {
//                    return new OffsetRange(ts.offset(), ts.offset() + token.length());
//                }
//
//                balance--;
//            } else if (isEndToken(id, doc, ts)) {
//                balance++;
//            }
//        }
//
        return OffsetRange.NONE;
    }

//    public static OffsetRange findEnd(BaseDocument doc, TokenSequence<?extends JsTokenId> ts) {
//        int balance = 0;
//
//        while (ts.moveNext()) {
//            Token<?extends JsTokenId> token = ts.token();
//            TokenId id = token.id();
//
//            if (isBeginToken(id, doc, ts)) {
//                balance--;
//            } else if (isEndToken(id, doc, ts)) {
//                if (balance == 0) {
//                    return new OffsetRange(ts.offset(), ts.offset() + token.length());
//                }
//
//                balance++;
//            }
//        }
//
//        return OffsetRange.NONE;
//    }
//    
////    /** Determine whether "do" is an indent-token (e.g. matches an end) or if
////     * it's simply a separator in while,until,for expressions)
////     */
////    public static boolean isEndmatchingDo(BaseDocument doc, int offset) {
////        // In the following case, do is dominant:
////        //     expression.do 
////        //        whatever
////        //     end
////        //
////        // However, not here:
////        //     while true do
////        //        whatever
////        //     end
////        //
////        // In the second case, the end matches the while, but in the first case
////        // the end matches the do
////        
////        // Look at the first token of the current line
////        try {
////            int first = Utilities.getRowFirstNonWhite(doc, offset);
////            if (first != -1) {
////                Token<? extends JsTokenId> token = getToken(doc, first);
////                if (token != null) {
////                    TokenId id = token.id();
////                    if (id == JsTokenId.WHILE || id == JsTokenId.UNTIL || id == JsTokenId.FOR) {
////                        return false;
////                    }
////                }
////            }
////        } catch (BadLocationException ble) {
////            Exceptions.printStackTrace(ble);
////        }
////        
////        return true;
////    }

    /**
     * Return true iff the given token is a token that should be matched
     * with a corresponding "end" token, such as "begin", "def", "module",
     * etc.
     */
    public static boolean isIndentBeginToken(PHPTokenId id) {
        return INDENT_BEGIN_TOKENS.contains(id);
    }

    /**
     * Return true iff the given token is a token that should be matched
     * with a corresponding "end" token, such as "begin", "def", "module",
     * etc.
     */
    public static boolean isIndentEndToken(PHPTokenId id) {
        return INDENT_END_TOKENS.contains(id);
    }

//    private static OffsetRange findMultilineRange(TokenSequence<? extends JsTokenId> ts) {
//        int startOffset = ts.offset();
//        JsTokenId id = ts.token().id();
//        switch (id) {
//            case ELSE:
//                ts.moveNext();
//                id = ts.token().id();
//                break;
//            case IF:
//            case FOR:
//            case WHILE:
//                ts.moveNext();
//                if (!skipParenthesis(ts)) {
//                    return OffsetRange.NONE;
//                }
//                id = ts.token().id();
//                break;
//            default:
//                return OffsetRange.NONE;
//        }
//        
//        boolean eolFound = false;
//        int lastEolOffset = ts.offset();
//        
//        // skip whitespaces and comments
//        if (id == JsTokenId.WHITESPACE || id == JsTokenId.LINE_COMMENT || id == JsTokenId.BLOCK_COMMENT || id == JsTokenId.EOL) {
//            if (ts.token().id() == JsTokenId.EOL) {
//                lastEolOffset = ts.offset();
//                eolFound = true;
//            }
//            while (ts.moveNext() && (
//                    ts.token().id() == JsTokenId.WHITESPACE ||
//                    ts.token().id() == JsTokenId.LINE_COMMENT ||
//                    ts.token().id() == JsTokenId.EOL ||
//                    ts.token().id() == JsTokenId.BLOCK_COMMENT)) {
//                if (ts.token().id() == JsTokenId.EOL) {
//                    lastEolOffset = ts.offset();
//                    eolFound = true;
//                }
//            }
//        }
//        // if we found end of sequence or end of line
//        if (ts.token() == null || (ts.token().id() != JsTokenId.LBRACE && eolFound)) {
//            return new OffsetRange(startOffset, lastEolOffset);
//        }
//        return  OffsetRange.NONE;
//    }
//    
//    public static OffsetRange getMultilineRange(BaseDocument doc, int offset) {
//        TokenSequence<? extends JsTokenId> ts = getPositionedSequence(doc, offset);
//        return findMultilineRange(ts);
//    }
//    
//    /**
//     * Return true iff the given token is a token that should be matched
//     * with a corresponding "end" token, such as "begin", "def", "module",
//     * etc.
//     */
//    public static boolean isBeginToken(PHPTokenId id, BaseDocument doc, TokenSequence<?extends PHPTokenId> ts) {
//        return false;
//    }
//
//    public static boolean isEndToken(PHPTokenId id, BaseDocument doc, TokenSequence<?extends PHPTokenId> ts) {
//        return false;
//    }
//    
//    /**
//     * Return true iff the given token is a token that indents its content,
//     * such as the various begin tokens as well as "else", "when", etc.
//     */
//    public static boolean isIndentToken(TokenId id) {
//        return INDENT_WORDS.contains(id);
//    }

    /** Compute the balance of begin/end tokens on the line.
     * @param doc the document
     * @param offset The offset somewhere on the line
     * @param upToOffset If true, only compute the line balance up to the given offset (inclusive),
     *   and if false compute the balance for the whole line
     */
    public static int getBeginEndLineBalance(BaseDocument doc, int offset, boolean upToOffset) {
// XXX: resurrect
//        try {
//            int begin = Utilities.getRowStart(doc, offset);
//            int end = upToOffset ? offset : Utilities.getRowEnd(doc, offset);
//
//            TokenSequence<?extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, begin);
//            if (ts == null) {
//                return 0;
//            }
//
//            ts.move(begin);
//
//            if (!ts.moveNext()) {
//                return 0;
//            }
//
//            int balance = 0;
//
//            do {
//                Token<?extends PHPTokenId> token = ts.token();
//                TokenId id = token.id();
//
//                if (isBeginToken(id, doc, ts)) {
//                    balance++;
//                } else if (isEndToken(id, doc, ts)) {
//                    balance--;
//                }
//            } while (ts.moveNext() && (ts.offset() <= end));
//
//            return balance;
//        } catch (BadLocationException ble) {
//            Exceptions.printStackTrace(ble);
//
//            return 0;
//        }
        return 0;
    }

    /** Compute the balance of begin/end tokens on the line */
    public static int getLineBalance(BaseDocument doc, int offset, TokenId up, TokenId down, LineBalance lineBalance) {
        try {
            int begin = Utilities.getRowStart(doc, offset);
            int end = Utilities.getRowEnd(doc, offset);

            TokenSequence<?extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, begin);
            if (ts == null) {
                return 0;
            }

            ts.move(begin);

            if (!ts.moveNext()) {
                return 0;
            }

            int upCount = 0;
            int downCount = 0;

            do {
                Token<?extends PHPTokenId> token = ts.token();
                TokenId id = token.id();

                if (id == up) {
                    upCount++;
                } else if (id == down) {
                    if (lineBalance.equals(LineBalance.UP_FIRST)) {
                        if (upCount > 0) {downCount++;}
                    } else {
                        downCount++;
                    }
                }
            } while (ts.moveNext() && (ts.offset() <= end));

            return (upCount-downCount);
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
    public static int getTokenBalance(BaseDocument doc, char open, char close, int offset) throws BadLocationException {
        TokenSequence<?extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, 0);
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

            if (textEquals(t.text(), open)) {
                balance++;
            } else if (textEquals(t.text(), close)) {
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

    private static String getIndentString(int indent) {
        StringBuilder sb = new StringBuilder(indent);
        indent(sb, indent);

        return sb.toString();
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

        Token<? extends PHPTokenId> token = LexUtilities.getToken(doc, begin);
        if (token != null) {
            return token.id() == PHPTokenId.PHP_LINE_COMMENT;
        }
        
        return false;
    }

//    public static void adjustLineIndentation(BaseDocument doc, int offset, int adjustment) {
//        try {
//            int lineBegin = Utilities.getRowStart(doc, offset);
//
//            if (adjustment > 0) {
//                doc.remove(lineBegin, adjustment);
//            } else if (adjustment < 0) {
//                doc.insertString(adjustment, LexUtilities.getIndentString(adjustment), null);
//            }
//        } catch (BadLocationException ble) {
//            Exceptions.printStackTrace(ble);
//        }
//    }

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

//    /**
//     * Return the string at the given position, or null if none
//     */
//    @SuppressWarnings("unchecked")
//    public static String getStringAt(int caretOffset, TokenHierarchy<Document> th) {
//        TokenSequence<?extends JsTokenId> ts = getJsTokenSequence(th, caretOffset);
//
//        if (ts == null) {
//            return null;
//        }
//
//        ts.move(caretOffset);
//
//        if (!ts.moveNext() && !ts.movePrevious()) {
//            return null;
//        }
//
//        if (ts.offset() == caretOffset) {
//            // We're looking at the offset to the RIGHT of the caret
//            // and here I care about what's on the left
//            ts.movePrevious();
//        }
//
//        Token<?extends JsTokenId> token = ts.token();
//
//        if (token != null) {
//            TokenId id = token.id();
//
////            // We're within a String that has embedded Js. Drop into the
////            // embedded language and see if we're within a literal string there.
////            if (id == JsTokenId.EMBEDDED_RUBY) {
////                ts = (TokenSequence)ts.embedded();
////                assert ts != null;
////                ts.move(caretOffset);
////
////                if (!ts.moveNext() && !ts.movePrevious()) {
////                    return null;
////                }
////
////                token = ts.token();
////                id = token.id();
////            }
////
//            String string = null;
//
//            // Skip over embedded Js segments and literal strings until you find the beginning
//            int segments = 0;
//
//            while ((id == JsTokenId.ERROR) || (id == JsTokenId.STRING_LITERAL)) {
//                string = token.text().toString();
//                segments++;
//                ts.movePrevious();
//                token = ts.token();
//                id = token.id();
//            }
//
//            if (id == JsTokenId.STRING_BEGIN) {
//                if (segments == 1) {
//                    return string;
//                } else {
//                    // Build up the String from the sequence
//                    StringBuilder sb = new StringBuilder();
//
//                    while (ts.moveNext()) {
//                        token = ts.token();
//                        id = token.id();
//
//                        if ((id == JsTokenId.ERROR) || (id == JsTokenId.STRING_LITERAL)) {
//                            sb.append(token.text());
//                        } else {
//                            break;
//                        }
//                    }
//
//                    return sb.toString();
//                }
//            }
//        }
//
//        return null;
//    }
//
////    /**
////     * Check if the caret is inside a literal string that is associated with
////     * a require statement.
////     *
////     * @return The offset of the beginning of the require string, or -1
////     *     if the offset is not inside a require string.
////     */
////    public static int getRequireStringOffset(int caretOffset, TokenHierarchy<Document> th) {
////        TokenSequence<?extends JsTokenId> ts = getJsTokenSequence(th, caretOffset);
////
////        if (ts == null) {
////            return -1;
////        }
////
////        ts.move(caretOffset);
////
////        if (!ts.moveNext() && !ts.movePrevious()) {
////            return -1;
////        }
////
////        if (ts.offset() == caretOffset) {
////            // We're looking at the offset to the RIGHT of the caret
////            // and here I care about what's on the left
////            ts.movePrevious();
////        }
////
////        Token<?extends JsTokenId> token = ts.token();
////
////        if (token != null) {
////            TokenId id = token.id();
////
////            // Skip over embedded Js segments and literal strings until you find the beginning
////            while ((id == JsTokenId.ERROR) || (id == JsTokenId.STRING_LITERAL)) {
////                ts.movePrevious();
////                token = ts.token();
////                id = token.id();
////            }
////
////            int stringStart = ts.offset() + token.length();
////
////            if (id == JsTokenId.STRING_BEGIN) {
////                // Completion of literal strings within require calls
////                while (ts.movePrevious()) {
////                    token = ts.token();
////
////                    id = token.id();
////
////                    if ((id == JsTokenId.WHITESPACE) || (id == JsTokenId.LPAREN) ||
////                            (id == JsTokenId.STRING_LITERAL)) {
////                        continue;
////                    }
////
////                    if (id == JsTokenId.IDENTIFIER) {
////                        String text = token.text().toString();
////
////                        if (text.equals("require") || text.equals("load")) {
////                            return stringStart;
////                        } else {
////                            return -1;
////                        }
////                    } else {
////                        return -1;
////                    }
////                }
////            }
////        }
////
////        return -1;
////    }
////
//
//    public static int getSingleQuotedStringOffset(int caretOffset, TokenHierarchy<Document> th) {
//        return getLiteralStringOffset(caretOffset, th, JsTokenId.STRING_BEGIN);
//    }
//
//    public static int getRegexpOffset(int caretOffset, TokenHierarchy<Document> th) {
//        return getLiteralStringOffset(caretOffset, th, JsTokenId.REGEXP_BEGIN);
//    }
//
//    /**
//     * Determine if the caret is inside a literal string, and if so, return its starting
//     * offset. Return -1 otherwise.
//     */
//    @SuppressWarnings("unchecked")
//    private static int getLiteralStringOffset(int caretOffset, TokenHierarchy<Document> th,
//        JsTokenId begin) {
//        TokenSequence<?extends JsTokenId> ts = getJsTokenSequence(th, caretOffset);
//
//        if (ts == null) {
//            return -1;
//        }
//
//        ts.move(caretOffset);
//
//        if (!ts.moveNext() && !ts.movePrevious()) {
//            return -1;
//        }
//
//        if (ts.offset() == caretOffset) {
//            // We're looking at the offset to the RIGHT of the caret
//            // and here I care about what's on the left
//            ts.movePrevious();
//        }
//
//        Token<?extends JsTokenId> token = ts.token();
//
//        if (token != null) {
//            TokenId id = token.id();
//
////            // We're within a String that has embedded Js. Drop into the
////            // embedded language and see if we're within a literal string there.
////            if (id == JsTokenId.EMBEDDED_RUBY) {
////                ts = (TokenSequence)ts.embedded();
////                assert ts != null;
////                ts.move(caretOffset);
////
////                if (!ts.moveNext() && !ts.movePrevious()) {
////                    return -1;
////                }
////
////                token = ts.token();
////                id = token.id();
////            }
//
//            // Skip over embedded Js segments and literal strings until you find the beginning
//            while ((id == JsTokenId.ERROR) || (id == JsTokenId.STRING_LITERAL) ||
//                    (id == JsTokenId.REGEXP_LITERAL)) {
//                ts.movePrevious();
//                token = ts.token();
//                id = token.id();
//            }
//
//            if (id == begin) {
//                if (!ts.moveNext()) {
//                    return -1;
//                }
//
//                return ts.offset();
//            }
//        }
//
//        return -1;
//    }
//
////    public static boolean isInsideQuotedString(BaseDocument doc, int offset) {
////        TokenSequence<?extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, offset);
////
////        if (ts == null) {
////            return false;
////        }
////
////        ts.move(offset);
////
////        if (ts.moveNext()) {
////            Token<?extends JsTokenId> token = ts.token();
////            TokenId id = token.id();
////            if (id == JsTokenId.QUOTED_STRING_LITERAL || id == JsTokenId.QUOTED_STRING_END) {
////                return true;
////            }
////        }
////        if (ts.movePrevious()) {
////            Token<?extends JsTokenId> token = ts.token();
////            TokenId id = token.id();
////            if (id == JsTokenId.QUOTED_STRING_LITERAL || id == JsTokenId.QUOTED_STRING_BEGIN) {
////                return true;
////            }
////        }
////        
////        return false;
////    }
////
//    public static boolean isInsideRegexp(BaseDocument doc, int offset) {
//        TokenSequence<?extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, offset);
//
//        if (ts == null) {
//            return false;
//        }
//
//        ts.move(offset);
//
//        if (ts.moveNext()) {
//            Token<?extends JsTokenId> token = ts.token();
//            TokenId id = token.id();
//            if (id == JsTokenId.REGEXP_LITERAL || id == JsTokenId.REGEXP_END) {
//                return true;
//            }
//        }
//        if (ts.movePrevious()) {
//            Token<?extends JsTokenId> token = ts.token();
//            TokenId id = token.id();
//            if (id == JsTokenId.REGEXP_LITERAL || id == JsTokenId.REGEXP_BEGIN) {
//                return true;
//            }
//        }
//        
//        return false;
//    }
//    
//    /**
//     * Get the comment block for the given offset. The offset may be either within the comment
//     * block, or the comment corresponding to a code node, depending on isAfter.
//     * 
//     * @param doc The document
//     * @param caretOffset The offset in the document
//     * @param isAfter If true, the offset is pointing to some code AFTER the code block
//     *   such as a method node. In this case it needs to back up to find the comment.
//     * @return
//     */
//    public static OffsetRange getCommentBlock(BaseDocument doc, int caretOffset, boolean isAfter) {
//        // Check if the caret is within a comment, and if so insert a new
//        // leaf "node" which contains the comment line and then comment block
//        try {
//            TokenSequence<? extends TokenId> ts = LexUtilities.getJsTokenSequence(doc, caretOffset);
//            if (ts == null) {
//                return OffsetRange.NONE;
//            }
//            ts.move(caretOffset);
//            if (isAfter) {
//                while (ts.movePrevious()) {
//                    TokenId id = ts.token().id();
//                    if (id == JsTokenId.BLOCK_COMMENT || id == JsTokenId.LINE_COMMENT) {
//                        return getCommentBlock(doc, ts.offset(), false);
//                    } else if (!((id == JsTokenId.WHITESPACE) || (id == JsTokenId.EOL))) {
//                        return OffsetRange.NONE;
//                    }
//                }
//                return OffsetRange.NONE;
//            }
//            
//            if (!ts.moveNext() && !ts.movePrevious()) {
//                return null;
//            }
//            Token<?extends TokenId> token = ts.token();
//            
//            if (token != null && token.id() == JsTokenId.BLOCK_COMMENT) {
//                return new OffsetRange(ts.offset(), ts.offset()+token.length());
//            }
//
//            if ((token != null) && (token.id() == JsTokenId.LINE_COMMENT)) {
//                // First add a range for the current line
//                int begin = Utilities.getRowStart(doc, caretOffset);
//                int end = Utilities.getRowEnd(doc, caretOffset);
//
//                if (LexUtilities.isCommentOnlyLine(doc, caretOffset)) {
//
//                    while (begin > 0) {
//                        int newBegin = Utilities.getRowStart(doc, begin - 1);
//
//                        if ((newBegin < 0) || !LexUtilities.isCommentOnlyLine(doc, newBegin)) {
//                            begin = Utilities.getRowFirstNonWhite(doc, begin);
//                            break;
//                        }
//
//                        begin = newBegin;
//                    }
//
//                    int length = doc.getLength();
//
//                    while (true) {
//                        int newEnd = Utilities.getRowEnd(doc, end + 1);
//
//                        if ((newEnd >= length) || !LexUtilities.isCommentOnlyLine(doc, newEnd)) {
//                            end = Utilities.getRowLastNonWhite(doc, end)+1;
//                            break;
//                        }
//
//                        end = newEnd;
//                    }
//
//                    if (begin < end) {
//                        return new OffsetRange(begin, end);
//                    }
//                } else {
//                    // It's just a line comment next to some code
//                    TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
//                    int offset = token.offset(th);
//                    return new OffsetRange(offset, offset + token.length());
//                }
//            }
//        } catch (BadLocationException ble) {
//            Exceptions.printStackTrace(ble);
//        }
//        
//        return OffsetRange.NONE;
//    }
//
//    /**
//     * Back up to the first space character prior to the given offset - as long as 
//     * it's on the same line!  If there's only leading whitespace on the line up
//     * to the lex offset, return the offset itself 
//     * @todo Rewrite this now that I have a separate newline token, EOL, that I can
//     *   break on - no need to call Utilities.getRowStart.
//     */
//    public static int findSpaceBegin(BaseDocument doc, int lexOffset) {
//        TokenSequence ts = LexUtilities.getJsTokenSequence(doc, lexOffset);
//        if (ts == null) {
//            return lexOffset;
//        }
//        boolean allowPrevLine = false;
//        int lineStart;
//        try {
//            lineStart = Utilities.getRowStart(doc, Math.min(lexOffset, doc.getLength()));
//            int prevLast = lineStart-1;
//            if (lineStart > 0) {
//                prevLast = Utilities.getRowLastNonWhite(doc, lineStart-1);
//                if (prevLast != -1) {
//                    char c = doc.getText(prevLast, 1).charAt(0);
//                    if (c == ',') {
//                        // Arglist continuation? // TODO : check lexing
//                        allowPrevLine = true;
//                    }
//                }
//            }
//            if (!allowPrevLine) {
//                int firstNonWhite = Utilities.getRowFirstNonWhite(doc, lineStart);
//                if (lexOffset <= firstNonWhite || firstNonWhite == -1) {
//                    return lexOffset;
//                }
//            } else {
//                // Make lineStart so small that Math.max won't cause any problems
//                int firstNonWhite = Utilities.getRowFirstNonWhite(doc, lineStart);
//                if (prevLast >= 0 && (lexOffset <= firstNonWhite || firstNonWhite == -1)) {
//                    return prevLast+1;
//                }
//                lineStart = 0;
//            }
//        } catch (BadLocationException ble) {
//            Exceptions.printStackTrace(ble);
//            return lexOffset;
//        }
//        ts.move(lexOffset);
//        if (ts.moveNext()) {
//            if (lexOffset > ts.offset()) {
//                // We're in the middle of a token
//                return Math.max((ts.token().id() == JsTokenId.WHITESPACE) ?
//                    ts.offset() : lexOffset, lineStart);
//            }
//            while (ts.movePrevious()) {
//                Token token = ts.token();
//                if (token.id() != JsTokenId.WHITESPACE) {
//                    return Math.max(ts.offset() + token.length(), lineStart);
//                }
//            }
//        }
//        
//        return lexOffset;
//    }
//
//    /**
//     * Get the documentation associated with the given node in the given document.
//     * TODO: handle proper block comments
//     */
//    public static List<String> gatherDocumentation(CompilationInfo info, BaseDocument baseDoc, int nodeOffset) {
//        LinkedList<String> comments = new LinkedList<String>();
//        int elementBegin = nodeOffset;
//        try {
//            if (info != null && info.getDocument() == baseDoc) {
//                elementBegin = LexUtilities.getLexerOffset(info, elementBegin);
//                if (elementBegin == -1) {
//                    return null;
//                }
//            }
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//        
//        try {
//            if (elementBegin >= baseDoc.getLength()) {
//                return null;
//            }
//
//            // Search to previous lines, locate comments. Once we have a non-whitespace line that isn't
//            // a comment, we're done
//
//            int offset = Utilities.getRowStart(baseDoc, elementBegin);
//            offset--;
//
//            // Skip empty and whitespace lines
//            while (offset >= 0) {
//                // Find beginning of line
//                offset = Utilities.getRowStart(baseDoc, offset);
//
//                if (!Utilities.isRowEmpty(baseDoc, offset) &&
//                        !Utilities.isRowWhite(baseDoc, offset)) {
//                    break;
//                }
//
//                offset--;
//            }
//
//            if (offset < 0) {
//                return null;
//            }
//
//            while (offset >= 0) {
//                // Find beginning of line
//                offset = Utilities.getRowStart(baseDoc, offset);
//
//                if (Utilities.isRowEmpty(baseDoc, offset) || Utilities.isRowWhite(baseDoc, offset)) {
//                    // Empty lines not allowed within an rdoc
//                    break;
//                }
//
//                // This is a comment line we should include
//                int lineBegin = Utilities.getRowFirstNonWhite(baseDoc, offset);
//                int lineEnd = Utilities.getRowLastNonWhite(baseDoc, offset) + 1;
//                String line = baseDoc.getText(lineBegin, lineEnd - lineBegin);
//
//                // Tolerate "public", "private" and "protected" here --
//                // Test::Unit::Assertions likes to put these in front of each
//                // method.
//                if (line.startsWith("*")) {
//                    // ignore end of block comment: "*/"
//                    if (line.length() == 1 || (line.length() > 1 && line.charAt(1) != '/')) {
//                        comments.addFirst(line.substring(1).trim());
//                    }
//                } else {
//                    // No longer in a comment
//                    break;
//                }
//
//                // Previous line
//                offset--;
//            }
//        } catch (BadLocationException ble) {
//            Exceptions.printStackTrace(ble);
//        }
//
//        return comments;
//    }


    public static boolean textEquals(CharSequence text1, char... text2) {
        int len = text1.length();
        if (len == text2.length) {
            for (int i = len - 1; i >= 0; i--) {
                if (text1.charAt(i) != text2[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
}
