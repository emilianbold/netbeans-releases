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
 * Contributor(s): The Original Software is NetBeans. The Initial
 * Developer of the Original Software is Sun Microsystems, Inc. Portions
 * Copyright 1997-2006 Sun Microsystems, Inc. All Rights Reserved.
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

package org.netbeans.modules.php.editor;

import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;

import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;

/**
 * This static class groups the whole aspect of bracket completion. It is
 * defined to clearly separate the functionality and keep actions clean. The
 * methods of the class are called from different actions as KeyTyped,
 * DeletePreviousChar.
 * 
 * @author ads ( BraceCompletion from org.netbeans.modules.editor.java 
 * package is resued ).
 */
class BracketCompletion {
    
    public static OffsetRange findMatching( Document doc, int caretOffset ) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Returns true if bracket completion is enabled in options.
     */
    static boolean completionSettingEnabled() {
        // TODO 
        /*return ((Boolean) Settings.getValue(PhpKit.class,
                ExtSettingsNames.PAIR_CHARACTERS_COMPLETION)).booleanValue();*/
        return true;
    }
    
    /**
     * Returns true if it is selected in Options
     * to add "\".\n\"" on line break
     */
    static boolean useDotConnectorInStringSetting() {
        // TODO
        return false;
    }
    
    /**
     * A hook method called after a character was inserted into the document.
     * The function checks for special characters for completion ()[]'"{} and
     * other conditions and optionally performs changes to the doc and or caret
     * (complets braces, moves caret, etc.)
     * 
     * @param doc
     *            the document where the change occurred
     * @param dotPos
     *            position of the character insertion
     * @param caret
     *            caret
     * @param ch
     *            the character that was inserted
     * @throws BadLocationException
     *             if dotPos is not correct
     */
    static void charInserted( BaseDocument doc, int dotPos, Caret caret, char ch )
            throws BadLocationException
    {
        if (!completionSettingEnabled())
        {
            return;
        }
        if (ch == ')' || ch == ']' || ch == '(' || ch == '[') {
            Token tokenAtDot = TokenUtils.getPhpToken(doc, dotPos);

            // tokenAtDot is inserted char, not the next one.
            if (    isTokenTextEquals( tokenAtDot, TokenUtils.RBRACKET) ||
                    isTokenTextEquals( tokenAtDot, TokenUtils.RPAREN) ) 
            {
                skipClosingBracket(doc, caret, ch);
            }
            else if ( isTokenTextEquals( tokenAtDot, TokenUtils.LBRACKET ) ||
                      isTokenTextEquals( tokenAtDot, TokenUtils.LPAREN))
            {
                completeOpeningBracket(doc, dotPos, caret, ch);
            }
        }
        else if (ch == ';') {
            processSemicolon(doc, dotPos, caret);
        }
    }

    /**
     * Hook called after a character *ch* was backspace-deleted from *doc*. The
     * function possibly removes bracket or quote pair if appropriate.
     * 
     * @param doc
     *            the document
     * @param dotPos
     *            position of the change
     * @param caret
     *            caret
     * @param ch
     *            the character that was deleted
     */
    static void charBackspaced( BaseDocument doc, int dotPos, Caret caret,
            char ch ) throws BadLocationException
    {
        if (completionSettingEnabled()) {
            if (ch == '(' || ch == '[') {
                if (isRemovePairedBracket(doc, dotPos, ch)){
                    doc.remove(dotPos, 1);
                }
            }
            else if (ch == '\"' || ch == '\'' || ch == '`') {
                if (!isEscapeSequence(doc, dotPos)) {
                    char match[] = doc.getChars(dotPos, 1);
                    if (match != null && match[0] == ch) {
                        doc.remove(dotPos, 1);
                    }
                }
            }
        }
    }
    
    private static boolean isRemovePairedBracket(BaseDocument doc, int dotPos,
            char ch) {
        Token tokenAtDot = TokenUtils.getPhpToken(doc, dotPos);

        if (TokenUtils.LBRACKET.equals(""+ch)) {
            if ( isTokenTextEquals(tokenAtDot, TokenUtils.RBRACKET) 
                 && !isBalanced(doc, TokenUtils.LBRACKET, TokenUtils.RBRACKET)) 
            {
                return true;
            }
        } 
        else if (TokenUtils.LPAREN.equals(""+ch)) {
            if ( isTokenTextEquals(tokenAtDot, TokenUtils.RPAREN) 
                 && !isBalanced(doc, TokenUtils.LPAREN, TokenUtils.RPAREN)) 
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolve whether pairing right curly should be added automatically at the
     * caret position or not. <br>
     * There must be only whitespace or line comment or block comment between
     * the caret position and the left brace and the left brace must be on the
     * same line where the caret is located. <br>
     * The caret must not be "contained" in the opened block comment token.
     * 
     * @param doc
     *            document in which to operate.
     * @param caretOffset
     *            offset of the caret.
     * @return true if a right brace '}' should be added or false if not.
     */
    static boolean isAddRightBrace( BaseDocument doc, int caretOffset )
            throws BadLocationException
    {
        boolean addRightBrace = false;
        if (completionSettingEnabled() && caretOffset > 0) {

            TokenSequence sequence = TokenUtils.getTokenSequence(doc);
            int tokenOffset = TokenUtils.getTokenOffset(sequence, caretOffset);

            // suppose that right brace should be added
            addRightBrace = true;

            /* 
             *  Disable right brace adding if caret not positioned within
             *  whitespace or line comment
             */
             addRightBrace = caretInsideAllowedTokens(
                     sequence, caretOffset, tokenOffset);
            
            if (addRightBrace) {// still candidate for adding
                /*
                 *  Check whether there are only whitespace or comment tokens
                 *  between caret and left brace and check only on the line
                 *  with the caret
                 */
                addRightBrace = checkAllowedTokensOnTheLeft(
                        doc, sequence, caretOffset, tokenOffset);
            }

            if (addRightBrace) { 
                /*
                 *  Finally check the brace balance
                 *  whether there are any missing right
                 *  braces
                 */
                addRightBrace = (braceBalance(doc) > 0);
            }
        }
        return addRightBrace;
    }

    static void addRightBrace(BaseDocument doc, Caret caret, int dotPos) 
            throws BadLocationException 
    {
        int end = getRowOrBlockEnd(doc, dotPos);
        doc.insertString(end, "}", null); // NOI18N
        caret.setDot(dotPos);
    }
    
    /**
     * Returns position of the first unpaired closing paren/brace/bracket from
     * the caretOffset till the end of caret row. If there is no such element,
     * position after the last non-white character on the caret row is returned.
     */
    static int getRowOrBlockEnd( BaseDocument doc, int caretOffset )
            throws BadLocationException
    {
        int rowEnd = Utilities.getRowLastNonWhite(doc, caretOffset);
        if (rowEnd == -1 || caretOffset >= rowEnd) {
            return caretOffset;
        }
        rowEnd += 1;
        int parenBalance = 0;
        int braceBalance = 0;
        int bracketBalance = 0;
        TokenSequence sequence = TokenUtils.getTokenSequence(doc).subSequence( 
                caretOffset , rowEnd );
        while ( sequence.moveNext() && sequence.offset() < rowEnd) {
            Token token = sequence.token();
            if ( isTokenTextEquals( token, TokenUtils.LPAREN)){
                parenBalance++;
            }
            else if (isTokenTextEquals( token, TokenUtils.RPAREN)){
                if (parenBalance-- == 0){
                    return sequence.offset();
                }
            }
            else if (isTokenTextEquals( token, TokenUtils.LBRACE)){
                braceBalance++;
            }
            else if (isTokenTextEquals( token, TokenUtils.RBRACE)){
                if (braceBalance-- == 0) {
                    return sequence.offset();
                }
            }
            else if (isTokenTextEquals( token, TokenUtils.LBRACKET)){
                bracketBalance++;
            }
            else if (isTokenTextEquals( token, TokenUtils.RBRACKET)){
                if (bracketBalance-- == 0) {
                    return sequence.offset();
                }

            }
        }
        return rowEnd;
    }

    /**
     * Check whether the typed bracket should stay in the document or be
     * removed. <br>
     * This method is called by <code>skipClosingBracket()</code>.
     * 
     * @param doc
     *            document into which typing was done.
     * @param caretOffset
     *             position of the caret in document
     * @param tokenText
     *            inserted bracket token text
     * @param leftBracket
     *            paired left bracket token text
     */
    static boolean isSkipClosingBracket( BaseDocument doc, int caretOffset,
            String bracketText , String leftBracketText) throws BadLocationException
    {
        // First check whether the caret is not after the last char in the document
        // because no bracket would follow then so it could not be skipped.
        if (caretOffset == doc.getLength()) {
            return false; // no skip in this case
        }

        boolean skipClosingBracket = false; // by default do not remove
        TokenSequence sequence = TokenUtils.getTokenSequence(doc);
        Token token = TokenUtils.getToken(sequence, caretOffset);
        
        if (token == null || !isTokenTextEquals(token, bracketText)) {
            return skipClosingBracket;
        }

        int balance = balanceBetweenBraces(doc, caretOffset, 
                leftBracketText, bracketText);
        if (balance <= 0 ){
            // this means amount of bracketText is more or equals to 
            // amount of leftBracketText. 
            skipClosingBracket = true;
        }
        return skipClosingBracket;
    }
    
    /**
     * Check for conditions and possibly complete an already inserted quote .
     * 
     * @param doc
     *            the document
     * @param dotPos
     *            position of the opening bracket (already in the doc)
     * @param caret
     *            caret
     * @param bracket
     *            the character that was inserted
     */
    static boolean completeQuote( BaseDocument doc, int dotPos, Caret caret,
            char bracket ) throws BadLocationException
    {

        if (!completionSettingEnabled()) {
            return false;
        }

        if (isEscapeSequence(doc, dotPos)) { // \" or \' typed
            return false;
        }

        Token tokenAtDot = TokenUtils.getPhpToken(doc, dotPos);
        
        // dotPos is inside comment
        if (isCommentToken(tokenAtDot)){
            return false;
        }
        
        if (isEODStringToken(tokenAtDot)){
            return false;
        }
        // dotPos is inside String
        boolean insideString = isStringToken(tokenAtDot);
        boolean unpairedQuoteToken = hasUnpairedQuoteToken(doc, bracket);

        if (insideString) {
            if (    unpairedQuoteToken
                    && retypedEnclosingBracket(doc, dotPos, bracket)){
                return true;
            }
        }
        
        // if quotes are not still balanced and we are not inside string, 
        // will add paired quote.
        if (!insideString && unpairedQuoteToken ) {
            String toInsert = "" + bracket + bracket;
            
            if (isSemicolonExpected(doc, dotPos)){
                toInsert += TokenUtils.SEMICOLON;
            }
            else if (isColonExpected(doc, dotPos)){
                toInsert += TokenUtils.COLON;
            }
            doc.insertString(dotPos, toInsert, null); // NOI18N
            return true;
        }
        return false;
    }

    /**
     * returns true if php_string token is placed 
     * on dotPos offset in doc document
     */
    static boolean posWithinString( BaseDocument doc, int dotPos ) {
        Token token = TokenUtils.getPhpToken(doc, dotPos);
        return isStringToken(token);
    }

    /**
     * returns true if there is ''
     */
    static boolean isNotClosedEOD(BaseDocument doc, int dotPos) {
        try {
            TokenSequence sequence = getTokenSequenceForRow(doc, dotPos);
            Token token = TokenUtils.getToken(sequence, dotPos);
            Token prevToken = getFirstNonWhiteTokenBwd(sequence, dotPos);
            
            if (prevToken == null || token == null){
                return false;
            }
            
            if (isEODStringToken(token)){
                return false;
            }
            
            if (TokenUtils.getTokenType(prevToken).equals(TokenUtils.EOD_OPERATOR)) {
                return true;
            }
        } catch (BadLocationException ex) {
        }
        return false;
    }

    static String getEODStringLabel( BaseDocument doc, int dotPos ){
        try {
            int firstNonWhite = TokenUtils.getTokenOffset(doc, dotPos);
            int lastNonWhite = Utilities.getFirstNonWhiteBwd(doc, dotPos);
            if (firstNonWhite >= 0 && lastNonWhite >= firstNonWhite) {
                return doc.getText(firstNonWhite, lastNonWhite - firstNonWhite + 1);
            }
        } catch (BadLocationException ex) {
        }
        return "";
    }

    private static boolean isColonExpected(BaseDocument doc, int dotPos) 
            throws BadLocationException
    {
        Token nextToken = getFirstNonWhiteTokenFwd(doc, dotPos);
        Token prevToken = getFirstNonWhiteTokenBwd(doc, dotPos);
        if (isEndOfLine(doc, dotPos)) {
            if (nextToken == null || prevToken == null ){
                return false;
            }
            if (    isTokenTextEquals(prevToken, TokenUtils.CASE)
                    && !isTokenTextEquals(nextToken, TokenUtils.COLON)
            ){
                return true;
            }
        }
        return false;
    }
    
    /**
     * if cursor is on the end of line and there are no
     * ), ], . or ; symbols after it, add ; after added paired quote.
     * 
     */
    private static boolean isSemicolonExpected(BaseDocument doc, int dotPos) 
            throws BadLocationException
    {
        Token nextToken = getFirstNonWhiteTokenFwd(doc, dotPos);
        Token prevToken = getFirstNonWhiteTokenBwd(doc, dotPos);
        if (!isEndOfLine(doc, dotPos)) {
            return false;
        }
        // next is allowed to be null
        boolean nextIncorrect = ( nextToken != null
                && (
                isStringToken(nextToken) 
                || isTokenTextEquals(nextToken, TokenUtils.DOT)
                || isTokenTextEquals(nextToken, TokenUtils.SEMICOLON)
                || isTokenTextEquals(nextToken, TokenUtils.RBRACKET)
                || isTokenTextEquals(nextToken, TokenUtils.RPAREN)
                || isTokenTextEquals(nextToken, TokenUtils.COLON) )
                );
        // next is NOT allowed to be null
        boolean prevIncorrect = ( prevToken != null
                && (
                isTokenTextEquals(prevToken, TokenUtils.CASE)
                || isTokenTextEquals(prevToken, TokenUtils.ARRAY_PAIR_MAPPER)
                || isTokenTextEquals(prevToken, TokenUtils.COMMA)
                || isTokenTextEquals(prevToken, TokenUtils.LPAREN) )
                );
        
            if (nextIncorrect || prevIncorrect){
                return false;
            }
        
            return true;
    }
    
    /**
     * returns token sequence retrieved from full document's tokenSequence 
     * using subSequence(rowOffset, dotPos);
     * rowOffset was calculated using Utilities.getRowStart(doc, dotPos)
     */
    private static TokenSequence getTokenSequenceForRow(BaseDocument doc, 
            int dotPos) throws BadLocationException
    {
            TokenSequence seq = TokenUtils.getTokenSequence(doc);
            int rowOffset = Utilities.getRowStart(doc, dotPos);
            return seq.subSequence(rowOffset, dotPos);
    }
    
    private static Token getFirstNonWhiteTokenFwd(BaseDocument doc, int dotPos) 
            throws BadLocationException
    {
        TokenSequence sequence = TokenUtils.getTokenSequence(doc);
        return getFirstNonWhiteTokenFwd(sequence, dotPos);
    }
    
    private static Token getFirstNonWhiteTokenFwd(TokenSequence sequence, 
            int dotPos) throws BadLocationException
    {
        sequence.move(dotPos);
        while (sequence.moveNext()){
            Token token = sequence.token();
            if (!TokenUtils.getTokenType(token).equals(TokenUtils.WHITESPACE)) {
                return sequence.token();
            }
        }
        return null;
    }

    private static Token getFirstNonWhiteTokenBwd(BaseDocument doc, int dotPos) 
            throws BadLocationException
    {
        TokenSequence sequence = TokenUtils.getTokenSequence(doc);
        return getFirstNonWhiteTokenBwd(sequence, dotPos);
    }
    
    private static Token getFirstNonWhiteTokenBwd(TokenSequence sequence, 
            int dotPos) throws BadLocationException
    {
        sequence.move(dotPos);
        while (sequence.movePrevious()){
            Token token = sequence.token();
            if (!TokenUtils.getTokenType(token).equals(TokenUtils.WHITESPACE)) {
                return sequence.token();
            }
        }
        return null;
    }
    
    /**
     * Counts the number of braces starting at dotPos to the end of the
     * document. Every occurence of { increses the count by 1, every occurrence
     * of } decreses the count by 1. The result is returned.
     * 
     * @return The number of { - number of } (>0 more { than } ,<0 more } than {)
     */
    private static int braceBalance( BaseDocument doc )
            throws BadLocationException
    {
        return tokenBalance(doc, TokenUtils.LBRACE, TokenUtils.RBRACE);
    }

    /**
     * The same as braceBalance but generalized to any pair of matching tokens.
     * 
     * @param open
     *            the token that increses the count
     * @param close
     *            the token that decreses the count
     */
    private static int tokenBalance( BaseDocument doc, String open,
            String close ) throws BadLocationException
    {
        BalanceTokenProcessor processor = new BalanceTokenProcessor(open, close);
        return processor.checkBalance(doc);
    }

    private static int tokenBalance( BaseDocument doc, String open,
            String close, int startOffset, int endOffset ) throws BadLocationException
    {
        BalanceTokenProcessor processor = new BalanceTokenProcessor(open, close);
        return processor.checkBalance(doc, startOffset, endOffset);
    }

    /**
     * A hook to be called after closing bracket ) or ] was inserted into the
     * document. The method checks if the bracket should stay there or be
     * removed and some exisitng bracket just skipped.
     * 
     * @param doc
     *            the document
     * @param dotPos
     *            position of the inserted bracket
     * @param caret
     *            caret
     * @param bracket
     *            the bracket character ']' or ')'
     */
    private static void skipClosingBracket( BaseDocument doc, Caret caret,
            char bracket ) throws BadLocationException
    {

        String bracketText = (bracket == ')') ? TokenUtils.RPAREN
                : TokenUtils.RBRACKET;
        String left = (bracket == ')') ? TokenUtils.LPAREN
                : TokenUtils.LBRACKET;

        int caretOffset = caret.getDot();
        if (isSkipClosingBracket(doc, caretOffset, bracketText , left)) {
            doc.remove(caretOffset - 1, 1);
            caret.setDot(caretOffset); // skip closing bracket
        }
    }

    /**
     * finds { or } brace on the left. The same on the right.
     * Checks balance of leftTokenText and rightTokenText in this period.
     * @returns number or leftTokenText occurances minus rightTokenText occurances
     */
    private static int balanceBetweenBraces(BaseDocument doc, int caretOffset,
            String leftTokenText, String rightTokenText) throws BadLocationException
    {
        int prevBraceOffset = findFirstTokenBwd(doc, caretOffset, 
                TokenUtils.LBRACE, TokenUtils.RBRACE);
        int nextBraceOffset = findFirstTokenFwd(doc, caretOffset, 
                TokenUtils.LBRACE, TokenUtils.RBRACE);

        // token balance 
        int balance = tokenBalance(doc, leftTokenText, rightTokenText, 
                prevBraceOffset, nextBraceOffset);
        return balance;
    }
    
    /** 
     * Searches for the first appearance of any token text specified in tokenText.
     * Looks in forward direction (increasing offset).
     * Starts from token placed on caretOffset offsed.
     */
    private static int findFirstTokenFwd(BaseDocument doc, int caretOffset, 
            CharSequence... tokenText)
    {
        TokenSequence sequence = TokenUtils.getTokenSequence(doc);
        sequence.move(caretOffset);
        int offset = -1;
        while (sequence.moveNext()){ 
            
            if (isTokenTextEquals(sequence.token(), tokenText)){
                offset = sequence.offset();
                break;
            }
        }
        return offset;
    }
    
    /** 
     * Searches for the first appearance of any token text specified in tokenText.
     * Looks in backward direction (decreasing offset).
     * Starts from token placed on caretOffset offsed.
     */
    private static int findFirstTokenBwd(BaseDocument doc, int caretOffset, 
            CharSequence... tokenText)
    {
        TokenSequence sequence = TokenUtils.getTokenSequence(doc);
        sequence.move(caretOffset);
        int offset = -1;
        while (sequence.movePrevious()){ 
            if (isTokenTextEquals(sequence.token(), tokenText)){
                offset = sequence.offset();
                break;
            }
        }
        return offset;
    }
    
    /** 
     * checks if tokenText is equals to any element specified in toCompare.
     */
    private static boolean isTokenTextEquals(Token token, 
            CharSequence... toCompare)
    {
        boolean result  = false;
        if (token != null){
            for (CharSequence test : toCompare){
                if(TokenUtilities.textEquals(token.text(), test)){
                    result = true;
                    break;
                }
            }
        }
        return result;
        
    }
    
    /**
     * Check for various conditions and possibly add a pairing bracket to the
     * already inserted.
     * 
     * @param doc
     *            the document
     * @param dotPos
     *            position of the opening bracket (already in the doc)
     * @param caret
     *            caret
     * @param bracket
     *            the bracket that was inserted
     */
    private static void completeOpeningBracket( BaseDocument doc, int dotPos,
            Caret caret, char bracket ) throws BadLocationException
    {
        String leftBracket = ""+bracket;
        String rightBracket = ""+matching(bracket);
        
        int balance = 
                balanceBetweenBraces(doc, dotPos, leftBracket, rightBracket);
        if (isCompletablePosition(doc, dotPos + 1) && balance >0) {
            doc.insertString(dotPos + 1, rightBracket, null);
            caret.setDot(dotPos + 1);
        }
    }

    private static boolean isEscapeSequence( BaseDocument doc, int dotPos )
            throws BadLocationException
    {
        if (dotPos <= 0)
            return false;
        char previousChar = doc.getChars(dotPos - 1, 1)[0];
        return previousChar == '\\';
    }

    /**
     * Checks if caret is positioned within whitespace or line comment.
     * @return false if caret is inside any token except WHITESPACE or LINE_COMMENT. 
     * Otherwise true.
     */
    private static boolean caretInsideAllowedTokens(TokenSequence sequence, 
            int caretOffset, int tokenOffset) 
    {
        Token tokenX = TokenUtils.getToken(sequence, caretOffset);
        int off = caretOffset - tokenOffset;
        if (off > 0 && off < tokenX.length()) {
            // caret contained in token
            // these tokens are OK
            return  TokenUtils.getTokenType(tokenX).equals(TokenUtils.WHITESPACE)
                    || 
                    TokenUtils.getTokenType(tokenX).equals(TokenUtils.LINE_COMMENT);
        }
        // caret is not inside token
        return true;
    }
    
    /**
     *  Check whether there are only whitespace or comment tokens
     *  between caret and left brace and check only on the line
     *  with the caret
     */
    private static boolean checkAllowedTokensOnTheLeft(BaseDocument doc, 
            TokenSequence sequence, int caretOffset, int tokenOffset) 
    {
        boolean isAllowed = false;
        try {
            sequence.move(tokenOffset);
            int caretRowStartOffset = Utilities.getRowStart(doc, caretOffset);

            while (sequence.movePrevious() 
                    && sequence.offset() >= caretRowStartOffset) 
            {
                Token token = sequence.token();
                // Assuming java token context here
                boolean allowedTokens = 
                        TokenUtils.getTokenType(token).equals(TokenUtils.WHITESPACE)
                        || 
                        isCommentToken(token);

                if (allowedTokens) {
                    continue;
                }
                /* the only success case is when we meet LBRACE before we 
                 meet any another token 
                 */
                if (isTokenTextEquals(token, TokenUtils.LBRACE)) {
                    isAllowed = true;
                }
                break;
            }
        } catch (BadLocationException ex) {
        }
        /* we are here means:
         - we have got exception
         - achieved line beginning
         - have no tokens on the left
         - have met not allowed token which was not LBRACE
         */
        return isAllowed;
    }
    
    private static boolean isEndOfLine(BaseDocument doc, int dotPos) 
            throws BadLocationException 
    {
        int lastNonWhite = Utilities.getRowLastNonWhite(doc, dotPos);
        // eol - true if the caret is at the end of line (ignoring whitespaces)
        boolean eol = lastNonWhite < dotPos;
        return eol;
    }
    
    /**
     * if we retype the first or the last quote of the string,
     * overwrites existing quote and returns true.
     * Otherwise returns false.
     */
    // fix for #69524
    private static boolean retypedEnclosingBracket(BaseDocument doc, int dotPos, 
            char bracket) throws BadLocationException
    {
        char chr = doc.getChars(dotPos, 1)[0];
        if (chr == bracket) {
            TokenSequence seq = TokenUtils.getTokenSequence(doc);
            int tokenStart = TokenUtils.getTokenOffset(seq, dotPos);
            int tokenLength = TokenUtils.getToken(seq, dotPos).length();
            int tokenEnd = tokenStart + tokenLength - 1;

            if ( dotPos == tokenStart || dotPos == tokenEnd){
                doc.insertString(dotPos, "" + bracket, null); // NOI18N
                doc.remove(dotPos, 1);
                return true;
            }
        }
        return false;
    }
    
    private static boolean hasUnpairedQuoteToken(BaseDocument doc, char bracket) {
        int count = 0;
        String quote = "" + bracket;
        TokenSequence sequence = TokenUtils.getTokenSequence(doc);
        sequence.moveStart();
        while (sequence.moveNext()) {
            CharSequence tokenText = sequence.token().text();
            if (TokenUtilities.textEquals(tokenText, quote)) {
                count++;
            }
        }
        return ((count % 2) == 0);
    }

    private static boolean isCommentToken(Token token){
        return (token != null 
                && 
                ( TokenUtils.getTokenType(token).equals( TokenUtils.BLOCK_COMMENT )
                || TokenUtils.getTokenType(token).equals(TokenUtils.LINE_COMMENT)) );
    }
    
    private static boolean isStringToken(Token token){
        return (token != null 
                && 
                ( TokenUtils.getTokenType(token).equals( TokenUtils.STRING )) );
    }
    
    private static boolean isEODStringToken(Token token){
        return (token != null 
                && 
                ( TokenUtils.getTokenType(token).equals( TokenUtils.EOD_STRING )) );
    }
    
    /**
     * Checks whether dotPos is a position at which bracket and quote completion
     * is performed. Brackets and quotes are not completed everywhere but just
     * at suitable places .
     * 
     * @param doc
     *            the document
     * @param dotPos
     *            position to be tested
     */
    private static boolean isCompletablePosition( BaseDocument doc, int dotPos )
            throws BadLocationException
    {
        if (dotPos == doc.getLength()) // there's no other character to test
            return true;
        else {
            // test that we are in front of ) , " or '
            char chr = doc.getChars(dotPos, 1)[0];
            return (chr == ')'  || 
                    chr == ','  || 
                    chr == '\"' || 
                    chr == '\'' || 
                    chr == ' '  || 
                    chr == ']'  || 
                    chr == '}'  || 
                    chr == '{'  || 
                    chr == '\n' || 
                    chr == '\t' || 
                    chr == ';'  );
        }
        /*
         * have adde { in addition to suggested by java editor logic.
         * to support case -> if (|{}.
         * java will not add paired ) in this case.
         */
    }

    /**
     * Precesses new typed semicolon (;).
     * @param doc
     *          the document
     * @param dotPos
     *          int position on which new ; was added
     * @param caret
     *          Caret. is on the new position, after new typed ;
     */
    private static void processSemicolon( BaseDocument doc, int dotPos, Caret caret )
            throws BadLocationException
    {
        if (isRetypedSemicolon(doc, dotPos, caret)){
            return;
        }
        moveSemicolon(doc, dotPos, caret);
    }
        
    /**
     * processes case of retyped ; at the end of line.
     * if ; already existed and was the last token on the line, 
     * removes newly adde ;, to have only one of them.
     * @param doc
     *          the document
     * @param dotPos
     *          int position on which new ; was added
     * @param caret
     *          Caret. is on the new position, after new typed ;
     */
    private static boolean isRetypedSemicolon( BaseDocument doc, int dotPos, 
            Caret caret ) throws BadLocationException
    {
        
        int caretPos = caret.getDot();
        // check if symbol on the caret is the last in the line
        if (!isEndOfLine(doc, caretPos+1)) {
            return false;
        }

        Token nextToken = TokenUtils.getPhpToken(doc, dotPos+1);
        
        if (isTokenTextEquals(nextToken, TokenUtils.SEMICOLON)){
            // remove new semicolon
            doc.remove(dotPos, 1); 
            // set caret pos after semicolon
            caret.setDot(caretPos); 
            return true;
        }
        return false;
    }
        
    
    private static void moveSemicolon( BaseDocument doc, int dotPos, Caret caret )
            throws BadLocationException
    {
        
        int lastParenPos = findLastRParenWithoutOthers(doc, dotPos);
        if (lastParenPos < 0 ){
            return;
        }
        
        Token tokenAfterSColon = TokenUtils.getPhpToken(doc, dotPos);
        if ( isForLoopSemicolon( doc, dotPos) 
                || isStringToken(tokenAfterSColon)
                || isEODStringToken(tokenAfterSColon))
        {
            return;
        }
        
        doc.remove(dotPos, 1);
        doc.insertString(lastParenPos, ";", null); // NOI18N
        caret.setDot(lastParenPos + 1);
    }

    /**
     * looks for the last right Parenthesis in current line.
     * Starts from dotPos offset and goes in forward direction.
     * <br/>
     * Doesn't permit any symbols other than 
     * TokenUtils.RPAREN or TokenUtils.WHITESPACE.
     * @return last right paren position in current row. returns -1 If there is any symbol 
     * other than TokenUtils.RPAREN or TokenUtils.WHITESPACE.
     * 
     * <br/>
     * is invoked from moveSemicolon( BaseDocument, int, Caret )
     */
    private static int findLastRParenWithoutOthers(BaseDocument doc , int dotPos) 
            throws BadLocationException
    {
        int lastParenPos = -1;
        int eolPos = Utilities.getRowEnd(doc, dotPos);
        TokenSequence sequence= TokenUtils.getTokenSequence(doc).subSequence(
                dotPos, eolPos);
        if ( sequence == null ){
            return -1;
        }
        
        sequence.moveNext(); // skip current ;
        while ( sequence.moveNext() ){
            Token token = sequence.token();
            if (isTokenTextEquals(token, TokenUtils.RPAREN)){
                lastParenPos = sequence.offset();
            }
            else if ( !isTokenTextEquals(token, TokenUtils.WHITESPACE)){
                return -1;
            }
        }
        
        return lastParenPos;
   }
    
    /*
     * logic was taken from java editor
     */
    private static boolean isForLoopSemicolon( BaseDocument doc , int dotPos) {
        TokenSequence sequence = TokenUtils.getTokenSequence(doc);
        Token token = TokenUtils.getToken(sequence, dotPos);
        
        if (!isTokenTextEquals(token, TokenUtils.SEMICOLON)){
            return false;
        }

        sequence.move(dotPos);
        
        int parDepth = 0; // parenthesis depth
        int braceDepth = 0; // brace depth
        boolean semicolonFound = false; // next semicolon
        
        
        while (sequence.movePrevious()){
            
            token = sequence.token();
            if (isTokenTextEquals(token, TokenUtils.LPAREN)){
                if (parDepth == 0) { // could be a 'for ('
                    do {
                        sequence.movePrevious();
                        token = sequence.token();
                    } while ( isCommentToken(token) ||
                            TokenUtils.getTokenType(token).equals(TokenUtils.WHITESPACE));
                    
                    if ( isTokenTextEquals(token, TokenUtils.FOR)) {
                        return true;
                    }
                    return false;
                }
                else { // non-zero depth
                    parDepth--;
                }
            }
            else if (isTokenTextEquals(token, TokenUtils.RPAREN)) {
                parDepth++;
            }
            else if (isTokenTextEquals(token, TokenUtils.LBRACE)) {
                if (braceDepth == 0) { // unclosed left brace
                    return false;
                }
                braceDepth--;
            }
            else if (isTokenTextEquals(token, TokenUtils.RBRACE)) {
                braceDepth++;

            }
            else if (isTokenTextEquals(token, TokenUtils.SEMICOLON)) {
                if (semicolonFound) { // one semicolon already found
                    return false;
                }
                semicolonFound = true;
            }
        }
        return false;
    }

    private static boolean isBalanced( BaseDocument doc, String left, 
            String right )
    {
        try {
            return tokenBalance(doc, left, right) == 0;
        } catch (BadLocationException ex) {
            return false;
        }
    }


    /**
     * Returns for an opening bracket or quote the appropriate closing
     * character.
     */
    private static char matching( char bracket ) {
        switch (bracket) {
            case '(':
                return ')';
            case '[':
                return ']';
            case '\"':
                return '\"'; // NOI18N
            case '\'':
                return '\'';
            case '`':
                return '`';
            default:
                return ' ';
        }
    }

  /** 
   * Counts the number of specified left and right paired tokens 
   * Every occurence of leftToken increses the count by 1, every
   * occurrence of rightToken decreses the count by 1. 
   */
    private static class BalanceTokenProcessor {

        private static final int BALANCED = 0;
        
        public BalanceTokenProcessor(String leftToken, String rightToken) {
            myLeft = leftToken;
            myRight = rightToken;
        }
        
        /**
         * @return The number of leftToken minus number of rightToken occurances
         */
        public int getBalance(){
            return myBalance;
        }

        /**
         * @return true if number of leftToken and rightToken are equal
         */
        public boolean isBalanced(){
            return (getBalance() == BALANCED );
        }
        
        /**
         * @returns result of getBalance()
         * @see getBalance()
         */
        public int checkBalance(BaseDocument doc) {
            return checkBalance(doc, -1, -1);
        }
        
        /**
         * @returns result of getBalance()
         * @see getBalance()
         */
        public int checkBalance(BaseDocument doc, int startOffset, int endOffset) {
            refresh();
            TokenSequence sequence = getToketSequence(doc, startOffset, endOffset);
            sequence.moveStart();
            while (sequence.moveNext()) {
                checkToken(sequence.token());
            }
            return getBalance();
        }

        private TokenSequence getToketSequence(BaseDocument doc, 
                int startOffset, int endOffset)
        {
            TokenSequence sequence = TokenUtils.getTokenSequence(doc);
            if (startOffset < 0 && endOffset < 0){
                return sequence;
            }
            // if one of offsets was specified ( >=0)
            if (startOffset < 0){
                startOffset = 0;
            }
            if (endOffset < 0){
                endOffset = doc.getLength();
            }
            return sequence.subSequence(startOffset, endOffset);
        }
        
        private void checkToken(Token token) {
            CharSequence tokenText = token.text();
            if (TokenUtilities.textEquals(tokenText, myLeft)) {
                myBalance++;
            } else if (TokenUtilities.textEquals(tokenText, myRight)) {
                myBalance--;
            }
        }

        private void refresh(){
            myBalance = 0;
        }
        
        private String myLeft;
        private String myRight;

        private int myBalance = 0;
    }
    
}
