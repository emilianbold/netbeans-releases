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

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.OffsetRange;
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
    @CheckForNull
    public static TokenSequence<PHPTokenId> getPHPTokenSequence(Document doc, int offset) {
        TokenHierarchy<Document> th = TokenHierarchy.get(doc);
        return getPHPTokenSequence(th, offset);
    }

    public static TokenSequence<PHPTokenId> getPHPTokenSequence(TokenHierarchy<?> th, int offset) {
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
                    if (lineBalance.equals(LineBalance.DOWN_FIRST)) {
                        if (upCount > 0) {upCount++;}
                    } else {
                        upCount++;
                    }
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
