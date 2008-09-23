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
package org.netbeans.modules.javascript.editing.lexer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.gsf.api.CompilationInfo;

import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.annotations.CheckForNull;
import org.netbeans.modules.javascript.editing.NbUtilities;
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
    /**
     * Tokens that should cause indentation of the next line. This is true for all {@link #END_PAIRS},
     * but also includes tokens like "else" that are not themselves matched with end but also contribute
     * structure for indentation.
     *
     */
    private static final Set<TokenId> INDENT_WORDS = new HashSet<TokenId>();

    static {
        // END_PAIRS.add(JsTokenId.BEGIN);

        // Add words that are not matched themselves with an "end",
        // but which also provide block structure to indented content
        // (usually part of a multi-keyword structure such as if-then-elsif-else-end
        // where only the "if" is considered an end-pair.)
        INDENT_WORDS.add(JsTokenId.FOR);
        INDENT_WORDS.add(JsTokenId.IF);
        INDENT_WORDS.add(JsTokenId.ELSE);
        INDENT_WORDS.add(JsTokenId.WHILE);
    }

    @CheckForNull
    public static BaseDocument getDocument(CompilationInfo info, boolean forceOpen) {
        try {
            if (info == null) {
                return null;
            }
            BaseDocument doc = (BaseDocument) info.getDocument();
            if (doc == null && forceOpen) {
                doc = NbUtilities.getBaseDocument(info.getFileObject(), true);
            }
            
            return doc;
        } catch (ClassCastException ex) {
            // TESTS! If data object for GSF isn't found it will be a FilterDocument
            // rather than a BaseDocument
            return NbUtilities.getBaseDocument(info.getFileObject(), true);
        }
    }

    private LexUtilities() {
    }
    
    /** 
     * Return the comment sequence (if any) for the comment prior to the given offset.
     */
    public static TokenSequence<? extends JsCommentTokenId> getCommentFor(BaseDocument doc, int offset) {
        TokenSequence<?extends JsTokenId> jts = getJsTokenSequence(doc, offset);
        if (jts == null) {
            return null;
        }
        jts.move(offset);
        
        while (jts.movePrevious()) {
            TokenId id = jts.token().id();
            if (id == JsTokenId.BLOCK_COMMENT) {
                return jts.embedded(JsCommentTokenId.language());
            } else if (id != JsTokenId.WHITESPACE && id != JsTokenId.EOL) {
                return null;
            }
        }
        
        return null;
    }

    /** For a possibly generated offset in an AST, return the corresponding lexing/true document offset */
    public static int getLexerOffset(CompilationInfo info, int astOffset) {
        ParserResult result = info.getEmbeddedResult(JsTokenId.JAVASCRIPT_MIME_TYPE, 0);
        if (result == null) {
            result = info.getEmbeddedResult(JsTokenId.JSON_MIME_TYPE, 0);
        }
        if (result != null) {
            TranslatedSource ts = result.getTranslatedSource();
            if (ts != null) {
                return ts.getLexicalOffset(astOffset);
            }
        }
        
        return astOffset;
    }
    
    public static OffsetRange getLexerOffsets(CompilationInfo info, OffsetRange astRange) {
        ParserResult result = info.getEmbeddedResult(JsTokenId.JAVASCRIPT_MIME_TYPE, 0);
        if (result == null) {
            result = info.getEmbeddedResult(JsTokenId.JSON_MIME_TYPE, 0);
        }
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
    
    /** Find the ruby token sequence (in case it's embedded in something else at the top level */
    @SuppressWarnings("unchecked")
    public static TokenSequence<?extends JsTokenId> getJsTokenSequence(BaseDocument doc, int offset) {
        TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
        return getJsTokenSequence(th, offset);
    }
    
    @SuppressWarnings("unchecked")
    public static TokenSequence<?extends JsTokenId> getJsTokenSequence(TokenHierarchy<Document> th, int offset) {
        TokenSequence<?extends JsTokenId> ts = th.tokenSequence(JsTokenId.language());

        if (ts == null) {
            // Possibly an embedding scenario such as an RHTML file
            // First try with backward bias true
            List<TokenSequence<?>> list = th.embeddedTokenSequences(offset, true);

            for (TokenSequence t : list) {
                if (t.language() == JsTokenId.language()) {
                    ts = t;

                    break;
                }
            }

            if (ts == null) {
                list = th.embeddedTokenSequences(offset, false);

                for (TokenSequence t : list) {
                    if (t.language() == JsTokenId.language()) {
                        ts = t;

                        break;
                    }
                }
            }
        }

        return ts;
    }

    public static TokenSequence<?extends JsTokenId> getPositionedSequence(BaseDocument doc, int offset) {
        return getPositionedSequence(doc, offset, true);
    }
    
    public static TokenSequence<?extends JsTokenId> getPositionedSequence(BaseDocument doc, int offset, boolean lookBack) {
        TokenSequence<?extends JsTokenId> ts = getJsTokenSequence(doc, offset);

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

    
    public static Token<?extends JsTokenId> getToken(BaseDocument doc, int offset) {
        TokenSequence<?extends JsTokenId> ts = getPositionedSequence(doc, offset);
        
        if (ts != null) {
            return ts.token();
        }

        return null;
    }

    public static char getTokenChar(BaseDocument doc, int offset) {
        Token<?extends JsTokenId> token = getToken(doc, offset);

        if (token != null) {
            String text = token.text().toString();

            if (text.length() > 0) { // Usually true, but I could have gotten EOF right?

                return text.charAt(0);
            }
        }

        return 0;
    }

    
    public static Token<?extends JsTokenId> findNextNonWsNonComment(TokenSequence<?extends JsTokenId> ts) {
        return findNext(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.EOL, JsTokenId.LINE_COMMENT, JsTokenId.BLOCK_COMMENT));
    }

    public static Token<?extends JsTokenId> findPreviousNonWsNonComment(TokenSequence<?extends JsTokenId> ts) {
        return findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.EOL, JsTokenId.LINE_COMMENT, JsTokenId.BLOCK_COMMENT));
    }
    
    public static Token<?extends JsTokenId> findNext(TokenSequence<?extends JsTokenId> ts, List<JsTokenId> ignores) {
        if (ignores.contains(ts.token().id())) {
            while (ts.moveNext() && ignores.contains(ts.token().id())) {}
        }
        return ts.token();
    }
    
    public static Token<?extends JsTokenId> findNextIncluding(TokenSequence<?extends JsTokenId> ts, List<JsTokenId> includes) {
        while (ts.moveNext() && !includes.contains(ts.token().id())) {}
        return ts.token();
    }
    
    public static Token<?extends JsTokenId> findPreviousIncluding(TokenSequence<?extends JsTokenId> ts, List<JsTokenId> includes) {
            while (ts.movePrevious() && !includes.contains(ts.token().id())) {}
        return ts.token();
    }
    
    public static Token<?extends JsTokenId> findPrevious(TokenSequence<?extends JsTokenId> ts, List<JsTokenId> ignores) {
        if (ignores.contains(ts.token().id())) {
            while (ts.movePrevious() && ignores.contains(ts.token().id())) {}
        }
        return ts.token();
    }
    
    static boolean skipParenthesis(TokenSequence<?extends JsTokenId> ts) {
        return skipParenthesis(ts, false);
    }
    
    /**
     * Tries to skip parenthesis 
     */
    public static boolean skipParenthesis(TokenSequence<?extends JsTokenId> ts, boolean back) {
        int balance = 0;

        Token<?extends JsTokenId> token = ts.token();
        if (token == null) {
            return false;
        }

        TokenId id = token.id();
            
//        // skip whitespaces
//        if (id == JsTokenId.WHITESPACE) {
//            while (ts.moveNext() && ts.token().id() == JsTokenId.WHITESPACE) {}
//        }
        if (id == JsTokenId.WHITESPACE || id == JsTokenId.EOL) {
            while ((back ? ts.movePrevious() : ts.moveNext()) && (ts.token().id() == JsTokenId.WHITESPACE || ts.token().id() == JsTokenId.EOL)) {}
        }

        // if current token is not left parenthesis
        if (ts.token().id() != (back ? JsTokenId.RPAREN : JsTokenId.LPAREN)) {
            return false;
        }

        do {
            token = ts.token();
            id = token.id();

            if (id == (back ? JsTokenId.RPAREN : JsTokenId.LPAREN)) {
                balance++;
            } else if (id == (back ? JsTokenId.LPAREN : JsTokenId.RPAREN)) {
                if (balance == 0) {
                    return false;
                } else if (balance == 1) {
                    //int length = ts.offset() + token.length();
                    if (back) {
                        ts.movePrevious();
                    } else {
                        ts.moveNext();
                    }
                    return true;
                }

                balance--;
            }
        } while (back ? ts.movePrevious() : ts.moveNext());

        return false;
    }
    
    /** Search forwards in the token sequence until a token of type <code>down</code> is found */
    public static OffsetRange findFwd(BaseDocument doc, TokenSequence<?extends JsTokenId> ts, TokenId up,
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

    /** Search backwards in the token sequence until a token of type <code>up</code> is found */
    public static OffsetRange findBwd(BaseDocument doc, TokenSequence<?extends JsTokenId> ts, TokenId up,
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

    private static OffsetRange findMultilineRange(TokenSequence<? extends JsTokenId> ts) {
        int startOffset = ts.offset();
        JsTokenId id = ts.token().id();
        switch (id) {
            case ELSE:
                ts.moveNext();
                id = ts.token().id();
                break;
            case IF:
            case FOR:
            case WHILE:
                ts.moveNext();
                if (!skipParenthesis(ts, false)) {
                    return OffsetRange.NONE;
                }
                id = ts.token().id();
                break;
            default:
                return OffsetRange.NONE;
        }
        
        boolean eolFound = false;
        int lastEolOffset = ts.offset();
        
        // skip whitespaces and comments
        if (id == JsTokenId.WHITESPACE || id == JsTokenId.LINE_COMMENT || id == JsTokenId.BLOCK_COMMENT || id == JsTokenId.EOL) {
            if (ts.token().id() == JsTokenId.EOL) {
                lastEolOffset = ts.offset();
                eolFound = true;
            }
            while (ts.moveNext() && (
                    ts.token().id() == JsTokenId.WHITESPACE ||
                    ts.token().id() == JsTokenId.LINE_COMMENT ||
                    ts.token().id() == JsTokenId.EOL ||
                    ts.token().id() == JsTokenId.BLOCK_COMMENT)) {
                if (ts.token().id() == JsTokenId.EOL) {
                    lastEolOffset = ts.offset();
                    eolFound = true;
                }
            }
        }
        // if we found end of sequence or end of line
        if (ts.token() == null || (ts.token().id() != JsTokenId.LBRACE && eolFound)) {
            return new OffsetRange(startOffset, lastEolOffset);
        }
        return  OffsetRange.NONE;
    }
    
    public static OffsetRange getMultilineRange(BaseDocument doc, TokenSequence<? extends JsTokenId> ts) {
        int index = ts.index();
        OffsetRange offsetRange = findMultilineRange(ts);
        ts.moveIndex(index);
        ts.moveNext();
        return offsetRange;
    }
    
    /**
     * Return true iff the given token is a token that indents its content,
     * such as the various begin tokens as well as "else", "when", etc.
     */
    public static boolean isIndentToken(TokenId id) {
        return INDENT_WORDS.contains(id);
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

    /**
     * The same as braceBalance but generalized to any pair of matching
     * tokens.
     * @param open the token that increses the count
     * @param close the token that decreses the count
     */
    public static int getTokenBalance(BaseDocument doc, TokenId open, TokenId close, int offset)
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

    /**
     * Get the comment block for the given offset. The offset may be either within the comment
     * block, or the comment corresponding to a code node, depending on isAfter.
     * 
     * @param doc The document
     * @param caretOffset The offset in the document
     * @param isAfter If true, the offset is pointing to some code AFTER the code block
     *   such as a method node. In this case it needs to back up to find the comment.
     * @return
     */
    public static OffsetRange getCommentBlock(BaseDocument doc, int caretOffset, boolean isAfter) {
        // Check if the caret is within a comment, and if so insert a new
        // leaf "node" which contains the comment line and then comment block
        try {
            TokenSequence<? extends TokenId> ts = LexUtilities.getJsTokenSequence(doc, caretOffset);
            if (ts == null) {
                return OffsetRange.NONE;
            }
            ts.move(caretOffset);
            if (isAfter) {
                while (ts.movePrevious()) {
                    TokenId id = ts.token().id();
                    if (id == JsTokenId.BLOCK_COMMENT || id == JsTokenId.LINE_COMMENT) {
                        return getCommentBlock(doc, ts.offset(), false);
                    } else if (!((id == JsTokenId.WHITESPACE) || (id == JsTokenId.EOL))) {
                        return OffsetRange.NONE;
                    }
                }
                return OffsetRange.NONE;
            }
            
            if (!ts.moveNext() && !ts.movePrevious()) {
                return null;
            }
            Token<?extends TokenId> token = ts.token();
            
            if (token != null && token.id() == JsTokenId.BLOCK_COMMENT) {
                return new OffsetRange(ts.offset(), ts.offset()+token.length());
            }

            if ((token != null) && (token.id() == JsTokenId.LINE_COMMENT)) {
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
     * @todo Rewrite this now that I have a separate newline token, EOL, that I can
     *   break on - no need to call Utilities.getRowStart.
     */
    public static int findSpaceBegin(BaseDocument doc, int lexOffset) {
        TokenSequence ts = LexUtilities.getJsTokenSequence(doc, lexOffset);
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
                return Math.max((ts.token().id() == JsTokenId.WHITESPACE) ?
                    ts.offset() : lexOffset, lineStart);
            }
            while (ts.movePrevious()) {
                Token token = ts.token();
                if (token.id() != JsTokenId.WHITESPACE) {
                    return Math.max(ts.offset() + token.length(), lineStart);
                }
            }
        }
        
        return lexOffset;
    }

    /**
     * Get the documentation associated with the given node in the given document.
     * TODO: handle proper block comments
     */
    public static List<String> gatherDocumentation(CompilationInfo info, BaseDocument baseDoc, int nodeOffset) {
        LinkedList<String> comments = new LinkedList<String>();
        int elementBegin = nodeOffset;
        if (info != null && info.getDocument() == baseDoc) {
            elementBegin = LexUtilities.getLexerOffset(info, elementBegin);
            if (elementBegin == -1) {
                return null;
            }
        }
        
        try {
            if (elementBegin >= baseDoc.getLength()) {
                return null;
            }

            // Search to previous lines, locate comments. Once we have a non-whitespace line that isn't
            // a comment, we're done

            int offset = Utilities.getRowStart(baseDoc, elementBegin);
            offset--;

            // Skip empty and whitespace lines
            while (offset >= 0) {
                // Find beginning of line
                offset = Utilities.getRowStart(baseDoc, offset);

                if (!Utilities.isRowEmpty(baseDoc, offset) &&
                        !Utilities.isRowWhite(baseDoc, offset)) {
                    break;
                }

                offset--;
            }

            if (offset < 0) {
                return null;
            }

            while (offset >= 0) {
                // Find beginning of line
                offset = Utilities.getRowStart(baseDoc, offset);

                if (Utilities.isRowEmpty(baseDoc, offset) || Utilities.isRowWhite(baseDoc, offset)) {
                    // Empty lines not allowed within an rdoc
                    break;
                }

                // This is a comment line we should include
                int lineBegin = Utilities.getRowFirstNonWhite(baseDoc, offset);
                int lineEnd = Utilities.getRowLastNonWhite(baseDoc, offset) + 1;
                String line = baseDoc.getText(lineBegin, lineEnd - lineBegin);

                // Tolerate "public", "private" and "protected" here --
                // Test::Unit::Assertions likes to put these in front of each
                // method.
                if (line.startsWith("*")) {
                    // ignore end of block comment: "*/"
                    if (line.length() == 1 || (line.length() > 1 && line.charAt(1) != '/')) {
                        comments.addFirst(line.substring(1).trim());
                    }
                } else {
                    // No longer in a comment
                    break;
                }

                // Previous line
                offset--;
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }

        return comments;
    }

    
}
