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
 * Software is Sun Microsystems, Inc. Portions Copyright 2001-2006 Sun
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

package org.netbeans.modules.cnd.editor.cplusplus;

import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.ext.FormatTokenPosition;
import org.netbeans.editor.ext.ExtFormatSupport;
import org.netbeans.editor.ext.FormatWriter;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.openide.util.NbBundle;

/**
 * CC indentation services are located here
 *
 * duped from editor/libsrc/org/netbeans/editor/ext/java/JavaFormatSupport.java
 */
public class CCFormatSupport extends ExtFormatSupport {

    private TokenContextPath tokenContextPath;
    private CodeStyle codeStyle;

    public CCFormatSupport(FormatWriter formatWriter) {
        this(formatWriter, CCTokenContext.contextPath);
    }

    private CCFormatSupport(FormatWriter formatWriter, TokenContextPath tokenContextPath) {
        super(formatWriter);
        this.tokenContextPath = tokenContextPath;
        this.codeStyle = CodeStyle.getDefault(formatWriter.getDocument());
    }

    public TokenContextPath getTokenContextPath() {
        return tokenContextPath;
    }

    @Override
    public boolean expandTabs() {
        return codeStyle.expandTabToSpaces();
    }

    @Override
    public int getTabSize() {
        return codeStyle.getTabSize();
    }

    @Override
    public boolean isComment(TokenItem token, int offset) {
        TokenID tokenID = token.getTokenID();
        return (token.getTokenContextPath() == tokenContextPath
                && (tokenID == CCTokenContext.LINE_COMMENT
                    || tokenID == CCTokenContext.BLOCK_COMMENT));
    }

     /** Is given token a preprocessor **/
     public boolean isPreprocessorAtLineStart(TokenItem token){
         if(getFormatSpaceBeforeMethodCallParenthesis()) 
             return false;
         if (token != null){
            FormatTokenPosition ft = findLineFirstNonWhitespace(getPosition(token, 0));
            if (ft != null) {
                token = ft.getToken();
            }
         }
         return (token != null && token.getImage().startsWith("#")); // NOI18N
                 //&& getVisualColumnOffset(getPosition(token,0)) == 0);
     }

     /** Is given token a preprocessor **/
     public boolean isPreprocessorLine(TokenItem token){
         if (token != null){
            FormatTokenPosition ft = findLineFirstNonWhitespace(getPosition(token, 0));
            if (ft != null) {
                if (isEndBackSlashedLine(ft)) {
                    FormatTokenPosition eol = findPreviousEOL(ft);
                    ft = eol;
                    while(eol != null){
                        if (isBackSlashEndLine(eol)){
                            ft = eol;
                            eol = findPreviousEOL(eol);
                        } else {
                            break;
                        }
                    }
                    ft = findLineFirstNonWhitespace(ft);
                } else if (isBackSlashEndLine(ft)) {
                    FormatTokenPosition eol = ft;
                    eol = findPreviousEOL(eol);
                    while(eol != null){
                        if (isBackSlashEndLine(eol)){
                            ft = eol;
                            eol = findPreviousEOL(eol);
                        } else {
                            break;
                        }
                    }
                    ft = findLineFirstNonWhitespace(ft);
                }
                if (ft != null) {
                     token = ft.getToken();
                }
            }
         }
         return (token != null && token.getImage().startsWith("#")); // NOI18N
     }

     
     
    /** Return the ending non whitespace on the line or null
    * if there's no such token on the given line.
    */
    public boolean isEndBackSlashedLine(FormatTokenPosition pos) {
        FormatTokenPosition eol = findPreviousEOL(pos);
        if (eol != null){
            TokenItem token = eol.getToken();
            if (token != null) {
                eol =  findLineFirstNonWhitespace(getPosition(token, 0));
                if (eol != null) {
                    return isBackSlashEndLine(eol);
                }
            }
        }
        return false;
    }

    /** Return the ending non whitespace on the line or null
    * if there's no such token on the given line.
    */
    public boolean isBackSlashEndLine(FormatTokenPosition pos) {
        FormatTokenPosition eol = findLineEndNonImportant(pos);
        if (eol != null){
            TokenItem token = eol.getToken();
            if (token !=null) {
                if (token.getTokenID() == CCTokenContext.BACKSLASH || 
                    token.getTokenID() == CCTokenContext.INVALID_BACKSLASH){
                    return true;
                }
                token = token.getPrevious();
                return token !=null &&
                        (token.getTokenID() == CCTokenContext.BACKSLASH ||
                         token.getTokenID() == CCTokenContext.INVALID_BACKSLASH);
            }
        }
        return false;
    }

    public boolean isMultiLineComment(TokenItem token) {
        return (token.getTokenID() == CCTokenContext.BLOCK_COMMENT);
    }

    public boolean isMultiLineComment(FormatTokenPosition pos) {
        TokenItem token = pos.getToken();
        return (token == null) ? false : isMultiLineComment(token);
    }

    /** Check whether the given token is multi-line comment
     * that starts with a slash and an asterisk.
     */
    public boolean isCCDocComment(TokenItem token) {
        return isMultiLineComment(token)
            && token.getImage().startsWith("/*");
    }

    @Override
    public TokenID getWhitespaceTokenID() {
        return CCTokenContext.WHITESPACE;
    }

    @Override
    public TokenContextPath getWhitespaceTokenContextPath() {
        return tokenContextPath;
    }

    @Override
    public boolean canModifyWhitespace(TokenItem inToken) {
        if (inToken.getTokenContextPath() == CCTokenContext.contextPath) {
            switch (inToken.getTokenID().getNumericID()) {
                case CCTokenContext.BLOCK_COMMENT_ID:
                case CCTokenContext.WHITESPACE_ID:
                    return true;
            }
        }
        return false;
    }


    /** Find the starting token of the statement before
     * the given position and also return all the command
     * delimiters. It searches in the backward direction
     * for all the delimiters and statement starts and
     * return all the tokens that are either command starts
     * or delimiters. As the first step it uses
     * <code>getPreviousToken()</code> so it ignores the initial token.
     * @param token token before which the statement-start
     *  and delimiter is being searched.
     * @return token that is start of the given statement
     *  or command delimiter.
     *  If the start of the statement is not found, null is returned.
     */
    public TokenItem findStatement(TokenItem token) {
        TokenItem lit = null; // last important token
        boolean firstColon = true;
        TokenItem t = getPreviousToken(token);

        while (t != null) {
            if (t.getTokenContextPath() == tokenContextPath) {

                switch (t.getTokenID().getNumericID()) {
                    case CCTokenContext.SEMICOLON_ID:
                        if (!isForLoopSemicolon(t)) {
                            return (lit != null) ? lit : t;
                        }
                        break;

                    case CCTokenContext.LBRACE_ID:
                    case CCTokenContext.ELSE_ID:
                        return (lit != null) ? lit : t;

                    case CCTokenContext.RBRACE_ID:
                        // Check whether this is an array initialization block
                        if (!isArrayInitializationBraceBlock(t, null)) {
                            return (lit != null) ? lit : t;
                        } else { // skip the array initialization block
                            t = findMatchingToken(t, null, CCTokenContext.LBRACE, true);
                        }
                        break;

                    case CCTokenContext.COLON_ID:
                        TokenItem tt = findAnyToken(t, null, new TokenID[] {CCTokenContext.CASE, CCTokenContext.DEFAULT,
                        CCTokenContext.PUBLIC, CCTokenContext.PRIVATE, CCTokenContext.PROTECTED,
                        CCTokenContext.LBRACE, CCTokenContext.RBRACE, CCTokenContext.SEMICOLON,
                        CCTokenContext.QUESTION}, t.getTokenContextPath(), true);
                        if (tt != null) {
                            switch (tt.getTokenID().getNumericID()) {
                                case CCTokenContext.PUBLIC_ID:
                                case CCTokenContext.PRIVATE_ID:
                                case CCTokenContext.PROTECTED_ID:
                                case CCTokenContext.CASE_ID:
                                case CCTokenContext.DEFAULT_ID:
                                    return (lit != null) ? lit : t;
                            }
                        }
                        TokenItem prev = findImportantToken(t, null, true, true);
                        if (prev != null && prev.getTokenID().getNumericID()==CCTokenContext.RPAREN_ID) {
                            t = prev;
                            break;
                        }
                        if (lit != null && firstColon && tt == null){
                            return lit;
                        }
                        firstColon = false;
                        break;

                    case CCTokenContext.DO_ID:
                    case CCTokenContext.SWITCH_ID:
                    case CCTokenContext.CASE_ID:
                    case CCTokenContext.DEFAULT_ID:
                        return t;

                    case CCTokenContext.FOR_ID:
                    case CCTokenContext.IF_ID:
                    case CCTokenContext.WHILE_ID:
                        /* Try to find the statement after ( ... )
                         * If it exists, then the first important
                         * token after it is the stmt start. Otherwise
                         * it's this token.
                         */
                        if (lit != null && lit.getTokenID() == CCTokenContext.LPAREN) {
                            // Find matching right paren in fwd dir
                            TokenItem mt = findMatchingToken(lit, token,
                                    CCTokenContext.RPAREN, false);
                            if (mt != null && mt.getNext() != null) {
                                mt = findImportantToken(mt.getNext(), token, false, true);
                                if (mt != null) {
                                    return mt;
                                }
                            }
                        }
                        // No further stmt found, return this one
                        return t;
                }
                // Remember last important token (preprocessor token are not important (?) (4922370))
                if (!isPreprocessorLine(t) && isImportant(t, 0)) {
                    lit = t;
                }
            }
            t = t.getPrevious();
        }
        return lit;
    }


    /**
     * Find the 'if' when the 'else' is provided.
     * @param elseToken the token with the 'else' command
     *  for which the 'if' is being searched.
     * @return corresponding 'if' token or null if there's
     *  no corresponding 'if' statement.
     */
    public TokenItem findIf(TokenItem elseToken) {
        if (elseToken == null || !tokenEquals(elseToken, CCTokenContext.ELSE, tokenContextPath)) {
            throw new IllegalArgumentException(NbBundle.getBundle(CCFormatSupport.class).getString(
                    "MSG_AcceptOnlyElse")); //NOI18N
        }

        int braceDepth = 0; // depth of the braces
        int elseDepth = 0; // depth of multiple else stmts
        while (true) {
            elseToken = findStatement(elseToken);
            if (elseToken == null) {
                return null;
            }
            
            switch (elseToken.getTokenID().getNumericID()) {
                case CCTokenContext.LBRACE_ID:
                    if (--braceDepth < 0) {
                        return null; // no corresponding right brace
                    }
                    break;

                case CCTokenContext.RBRACE_ID:
                    braceDepth++;
                    break;

                case CCTokenContext.ELSE_ID:
                    if (braceDepth == 0) {
                        elseDepth++;
                    }
                    break;

                case CCTokenContext.SEMICOLON_ID:
                case CCTokenContext.COLON_ID:
                case CCTokenContext.DO_ID:
                case CCTokenContext.CASE_ID:
                case CCTokenContext.DEFAULT_ID:
                case CCTokenContext.FOR_ID:
                case CCTokenContext.WHILE_ID:
                    break;

                case CCTokenContext.IF_ID:
                    if (braceDepth == 0) {
                        if (elseDepth-- == 0) {
                            return elseToken; // successful search
                        }
                    }
                    break;
            }
        }
    }


    /** Find the 'switch' when the 'case' is provided.
     * @param caseToken the token with the 'case' command
     *  for which the 'switch' is being searched.
     * @return corresponding 'switch' token or null if there's
     *  no corresponding 'switch' statement.
     */
    public TokenItem findSwitch(TokenItem caseToken) {
        if (caseToken == null || (!tokenEquals(caseToken, CCTokenContext.CASE, tokenContextPath)
                   && !tokenEquals(caseToken, CCTokenContext.DEFAULT, tokenContextPath))) {
            throw new IllegalArgumentException(NbBundle.getBundle(CCFormatSupport.class).getString(
                                "MSG_AcceptOlyCaseDefault")); //NOI18N
        }

        int braceDepth = 1; // depth of the braces - need one more left
        while (true) {
            caseToken = findStatement(caseToken);
            if (caseToken == null) {
                return null;
            }
            
            switch (caseToken.getTokenID().getNumericID()) {
                case CCTokenContext.LBRACE_ID:
                    if (--braceDepth < 0) {
                        return null; // no corresponding right brace
                    }
                    break;

                case CCTokenContext.RBRACE_ID:
                    braceDepth++;
                    break;

                case CCTokenContext.SWITCH_ID:
                case CCTokenContext.DEFAULT_ID:
                    if (braceDepth == 0) {
                        return caseToken;
                    }
                    break;
            }
        }
    }

    /** Find the 'class' or 'struct'
     * @param visibilityToken the token.
     */
    private TokenItem findClassifier(TokenItem visibilityToken) {
        int braceDepth = 1; // depth of the braces - need one more left
        TokenItem classifierToken = visibilityToken;
        while (true) {
            classifierToken = findStatement(classifierToken);
            if (classifierToken == null) {
                return null;
            }
            
            switch (classifierToken.getTokenID().getNumericID()) {
                case CCTokenContext.LBRACE_ID:
                    if (--braceDepth < 0) {
                        return null; // no corresponding right brace
                    }
                    if (braceDepth == 0){
                        while ((classifierToken = classifierToken.getPrevious()) != null) {
                            switch (classifierToken.getTokenID().getNumericID()) {
                                case CCTokenContext.CLASS_ID:
                                case CCTokenContext.STRUCT_ID:
                                case CCTokenContext.ENUM_ID:
                                case CCTokenContext.UNION_ID:
                                    return classifierToken;
                            }
                        }
                        return null;
                    }
                    break;

                case CCTokenContext.RBRACE_ID:
                    braceDepth++;
                    break;

            }
        }
    }

    
    /** Find the 'try' when the 'catch' is provided.
     * @param catchToken the token with the 'catch' command
     *  for which the 'try' is being searched.
     * @return corresponding 'try' token or null if there's
     *  no corresponding 'try' statement.
     */
    public TokenItem findTry(TokenItem catchToken) {
        if (catchToken == null || (!tokenEquals(catchToken, CCTokenContext.CATCH, tokenContextPath))) {
            throw new IllegalArgumentException(NbBundle.getBundle(CCFormatSupport.class).getString(
					      "MSG_AcceptOnlyCatch")); //NOI18N
        }

        int braceDepth = 0; // depth of the braces
        while (true) {
            catchToken = findStatement(catchToken);
            if (catchToken == null) {
                return null;
            }
            
            switch (catchToken.getTokenID().getNumericID()) {
                case CCTokenContext.LBRACE_ID:
                    if (--braceDepth < 0) {
                        return null; // no corresponding right brace
                    }
                    break;

                case CCTokenContext.RBRACE_ID:
                    braceDepth++;
                    break;

                case CCTokenContext.TRY_ID:
                    if (braceDepth == 0) {
                        return catchToken;
                    }
                    break;
            }
        }
    }

    
    /** Find the start of the statement.
     * @param token token from which to start. It searches
     *  backward using <code>findStatement()</code> so it ignores
     *  the given token.
     * @return the statement start token (outer statement start for nested
     *  statements).
     *  It returns the same token if there is '{' before
     *  the given token.
     */
    public TokenItem findStatementStart(TokenItem token) {
        TokenItem t = findStatementStart(token, false);
        // preprocessor tokens are not important (bug#22570)
        while (t != null && isPreprocessorLine(t)) {
            TokenItem current = t.getPrevious();
            if (current == null) {
                return null;
            }
            t = findStatementStart(current, false);
        }
        return t;
    }

    public TokenItem findStatementStart(TokenItem token, boolean outermost) {
        TokenItem t = findStatement(token);
        if (t != null) {
            switch (t.getTokenID().getNumericID()) {
                case CCTokenContext.SEMICOLON_ID: // ';' found
                    TokenItem scss = findStatement(t);
                    if (scss == null) {
                        return token;
                    }
                    switch (scss.getTokenID().getNumericID()) {
                        case CCTokenContext.LBRACE_ID: // '{' then ';'
                        case CCTokenContext.RBRACE_ID: // '}' then ';'
                        case CCTokenContext.COLON_ID: // ':' then ';'
                        case CCTokenContext.CASE_ID: // 'case' then ';'
                        case CCTokenContext.DEFAULT_ID:
                        case CCTokenContext.SEMICOLON_ID: // ';' then ';'
                            return t; // return ';'

                        case CCTokenContext.PRIVATE_ID:
                        case CCTokenContext.PROTECTED_ID:
                        case CCTokenContext.PUBLIC_ID:

                        case CCTokenContext.DO_ID:
                        case CCTokenContext.FOR_ID:
                        case CCTokenContext.IF_ID:
                        case CCTokenContext.WHILE_ID:
                            return findStatementStart(t, outermost);

                        case CCTokenContext.ELSE_ID: // 'else' then ';'
                            // Find the corresponding 'if'
                            TokenItem ifss = findIf(scss);
                            if (ifss != null) { // 'if' ... 'else' then ';'
                                return findStatementStart(ifss, outermost);
                            } else { // no valid starting 'if'
                                return scss; // return 'else'
                            }

                        default: // something usual then ';'
                            TokenItem bscss = findStatement(scss);
                            if (bscss != null) {
                                switch (bscss.getTokenID().getNumericID()) {
                                case CCTokenContext.SEMICOLON_ID: // ';' then stmt ending with ';'
                                case CCTokenContext.LBRACE_ID:
                                case CCTokenContext.RBRACE_ID:
                                case CCTokenContext.COLON_ID:
                                    return scss; // 

                                case CCTokenContext.DO_ID:
                                case CCTokenContext.FOR_ID:
                                case CCTokenContext.IF_ID:
                                case CCTokenContext.WHILE_ID:
                                    return findStatementStart(bscss, outermost);

                                case CCTokenContext.ELSE_ID:
                                    // Find the corresponding 'if'
                                    ifss = findIf(bscss);
                                    if (ifss != null) { // 'if' ... 'else' ... ';'
                                        return findStatementStart(ifss, outermost);
                                    } else { // no valid starting 'if'
                                        return bscss; // return 'else'
                                    }
                                }
                            }

                            return scss;
                    } // semicolon servicing end

                case CCTokenContext.LBRACE_ID: // '{' found
                    return token; // return original token

                case CCTokenContext.RBRACE_ID: // '}' found
                    TokenItem lb = findMatchingToken(t, null, CCTokenContext.LBRACE, true);
                    if (lb != null) { // valid matching left-brace
                        // Find a stmt-start of the '{'
                        TokenItem lbss = findStatement(lb);
                        if (lbss != null) {
                            switch (lbss.getTokenID().getNumericID()) {
                                case CCTokenContext.ELSE_ID: // 'else {'
                                    // Find the corresponding 'if'
                                    TokenItem ifss = findIf(lbss);
                                    if (ifss != null) { // valid 'if'
                                        return findStatementStart(ifss, outermost);
                                    } else {
                                        return lbss; // return 'else'
                                    }

                                case CCTokenContext.CATCH_ID: // 'catch (...) {'
                                    // I'm not sure what to do if this isn't C++...
                                    // Find the corresponding 'try'
                                    TokenItem tryss = findTry(lbss);
                                    if (tryss != null) { // valid 'try'
                                        return findStatementStart(tryss, outermost);
                                    } else {
                                        return lbss; // return 'catch'
                                    }

                                case CCTokenContext.DO_ID:
                                case CCTokenContext.FOR_ID:
                                case CCTokenContext.IF_ID:
                                case CCTokenContext.WHILE_ID:
                                    return findStatementStart(lbss, outermost);
                            }
                            // I copied the next if from JavaFormatSupport. But I'm not 100% certain it
                            // applies...
                            if (lbss.getTokenID().getNumericID() == CCTokenContext.LBRACE_ID) {
                                return t; // return right brace
                            }
                            return lbss;
                        }
                    }
                    return t; // return right brace

                case CCTokenContext.COLON_ID:
                case CCTokenContext.CASE_ID:
                case CCTokenContext.DEFAULT_ID:
                    return token;

                case CCTokenContext.ELSE_ID:
                    // Find the corresponding 'if'
                    TokenItem ifss = findIf(t);
                    if (ifss != null) {
                        if (!outermost) {
                            return ifss;
                        } else {
                            return findStatementStart(ifss, outermost);
                        }
                    }
                    return t;

                case CCTokenContext.DO_ID:
                case CCTokenContext.FOR_ID:
                case CCTokenContext.IF_ID:
                case CCTokenContext.WHILE_ID:
                    if (!outermost) {
                        return t;
                    } else {
                        return findStatementStart(t, outermost);
                    }

                case CCTokenContext.IDENTIFIER_ID:
                    return t;
                default:
                    return t;
            }
        }
        return token; // return original token
    }

    /** 
     * Get the indentation for the given token. It first searches whether there's an 
     * non-whitespace and a non-leftbrace character on the line with the token and if so,
     * it takes indent of the non-ws char instead.
     *
     * @param token token for which the indent is being searched.
     *  The token itself is ignored and the previous token is used as a base for the search.
     * @param forceFirstNonWhitespace set true to ignore leftbrace and search 
     *  directly for first non-whitespace
     */
    public int getTokenIndent(TokenItem token, boolean forceFirstNonWhitespace) {
        FormatTokenPosition tp = getPosition(token, 0);
        // this is fix for bugs: 7980 and 9111
        // see the findLineFirstNonWhitespaceAndNonLeftBrace definition
        // for more info about the fix
        FormatTokenPosition fnw;
        if (forceFirstNonWhitespace)
            fnw = findLineFirstNonWhitespace(tp);
        else
            fnw = findLineFirstNonWhitespaceAndNonLeftBrace(tp);
        
        if (fnw != null) { // valid first non-whitespace
            tp = fnw;
        }
        return getVisualColumnOffset(tp);
    }

    public int getTokenIndent(TokenItem token) {
        return getTokenIndent(token, false);
    }   
    
    private int getRightIndent(){
        int i = getShiftWidth();
        if (isHalfIndentNewlineBeforeBrace()){
            return i/2;
        }
        return i;
    }

    private int getRightIndentSwitch(){
        int i = getShiftWidth();
        if (isHalfIndentNewlineBeforeBrace()){
            return i/2;
        }
        return i;
    }

    private int getRightIndentDeclaration(){
        int i = getShiftWidth();
        if (isHalfIndentNewlineBeforeBraceDeclaration()){
            return i/2;
        }
        return i;
    }
    
    /**
     * Find the indentation for the first token on the line.
     * The given token is also examined in some cases.
     */
    public int findIndent(TokenItem token) {
        int indent = -1; // assign invalid indent

        // First check the given token
        if (token != null) {
            switch (token.getTokenID().getNumericID()) {
                case CCTokenContext.ELSE_ID:
                    TokenItem ifss = findIf(token);
                    if (ifss != null) {
                        indent = getTokenIndent(ifss);
                    }
                    break;
                    
                case CCTokenContext.LBRACE_ID:
                    TokenItem stmt = findStatement(token);
                    if (stmt == null) {
                        indent = 0;
                    } else {
                        switch (stmt.getTokenID().getNumericID()) {
                            case CCTokenContext.DO_ID:
                            case CCTokenContext.FOR_ID:
                            case CCTokenContext.IF_ID:
                            case CCTokenContext.WHILE_ID:
                            case CCTokenContext.ELSE_ID:
                            case CCTokenContext.TRY_ID:
                            case CCTokenContext.ASM_ID:
                            case CCTokenContext.CATCH_ID:
                                indent = getTokenIndent(stmt);
                                if (isHalfIndentNewlineBeforeBrace()){
                                    indent += getShiftWidth()/2;
                                }
                                break;
                            case CCTokenContext.SWITCH_ID:
                                indent = getTokenIndent(stmt);
                                if (isHalfIndentNewlineBeforeBraceSwitch()){
                                    indent += getShiftWidth()/2;
                                }
                                break;
                                
                            case CCTokenContext.LBRACE_ID:
                                indent = getTokenIndent(stmt) + getShiftWidth();
                                break;
                                
                            default:
                                stmt = findStatementStart(token);
                                if (stmt == null) {
                                    indent = 0;
                                } else if (stmt == token) {
                                    stmt = findStatement(token); // search for delimiter
                                    indent = (stmt != null) ? indent = getTokenIndent(stmt) : 0;
                                } else { // valid statement
                                    indent = getTokenIndent(stmt);
                                    switch (stmt.getTokenID().getNumericID()) {
                                        case CCTokenContext.LBRACE_ID:
                                            indent += getShiftWidth();
                                            break;
                                    }
                                }
                        }
                    }
                    break;
                    
                case CCTokenContext.RBRACE_ID:
                    TokenItem rbmt = findMatchingToken(token, null, CCTokenContext.LBRACE, true);
                    if (rbmt != null) { // valid matching left-brace
                        TokenItem t = findStatement(rbmt);
                        boolean forceFirstNonWhitespace = false;
                        if (t == null) {
                            t = rbmt; // will get indent of the matching brace
                        } else {
                            switch (t.getTokenID().getNumericID()) {
                                case CCTokenContext.SEMICOLON_ID:
                                case CCTokenContext.LBRACE_ID:
                                case CCTokenContext.RBRACE_ID:
                                {
                                    t = rbmt;
                                    forceFirstNonWhitespace = true;
                                }
                            }
                        }
                        // the right brace must be indented to the first
                        // non-whitespace char - forceFirstNonWhitespace=true
                        indent = getTokenIndent(t, forceFirstNonWhitespace);
                        switch (t.getTokenID().getNumericID()){
                            case CCTokenContext.FOR_ID:
                            case CCTokenContext.IF_ID:
                            case CCTokenContext.WHILE_ID:
                            case CCTokenContext.DO_ID:
                            case CCTokenContext.ELSE_ID:
                            case CCTokenContext.TRY_ID:
                            case CCTokenContext.ASM_ID:
                            case CCTokenContext.CATCH_ID:
                                if (isHalfIndentNewlineBeforeBrace()){
                                    indent += getShiftWidth()/2;
                                }
                                break;
                            case CCTokenContext.SWITCH_ID:
                                if (isHalfIndentNewlineBeforeBraceSwitch()){
                                    indent += getShiftWidth()/2;
                                }
                                break;
                        }
                    } else { // no matching left brace
                        indent = getTokenIndent(token); // leave as is
                    }
                    break;
                    
                case CCTokenContext.CASE_ID:
                case CCTokenContext.DEFAULT_ID:
                    TokenItem swss = findSwitch(token);
                    if (swss != null) {
                        indent = getTokenIndent(swss);
                        if (indentCasesFromSwitch()) {
                            indent += getShiftWidth();
                        } else if (isHalfIndentNewlineBeforeBraceSwitch()) {
                            indent += getShiftWidth()/2;
                        }
                    }
                    break;
                case CCTokenContext.PUBLIC_ID:
                case CCTokenContext.PRIVATE_ID:
                case CCTokenContext.PROTECTED_ID:
                    TokenItem cls = findClassifier(token);
                    if (cls != null) {
                        indent = getTokenIndent(cls);
                    }
                    break;
                case CCTokenContext.CLASS_ID:
                case CCTokenContext.STRUCT_ID:
                    TokenItem clsTemplate = findClassifierStart(token);
                    if (clsTemplate != null) {
                        indent = getTokenIndent(clsTemplate);
                    }
                    break;
            }
        }

        // If indent not found, search back for the first important token
        if (indent < 0) { // if not yet resolved
            TokenItem t = findImportantToken(token, null, true, true);
            if (t != null) { // valid important token
                switch (t.getTokenID().getNumericID()) {
                    case CCTokenContext.SEMICOLON_ID: // semicolon found
                        TokenItem tt = findStatementStart(token);
                        // preprocessor tokens are not important (bug#22570)
                        while (tt != null && (isPreprocessorLine(tt) ||
				    tt.getImage().startsWith("\n"))) { // NOI18N
			    tt = findStatementStart(tt.getPrevious());
                        }
                        if (tt !=null){
                            switch (tt.getTokenID().getNumericID()) {
                                case CCTokenContext.PUBLIC_ID:
                                case CCTokenContext.PRIVATE_ID:
                                case CCTokenContext.PROTECTED_ID:
                                    indent = getTokenIndent(tt) + getShiftWidth();
                                    break;
                                default:
                                    indent = getTokenIndent(tt);
                                    break;
                            }
                        } else {
                            indent = getTokenIndent(tt);
                        }
                        break;

                    case CCTokenContext.LBRACE_ID:
                        TokenItem lbss = findStatementStart(t, false);
                        if (lbss == null) {
                            lbss = t;
                        }
                        switch (lbss.getTokenID().getNumericID()){
                            case CCTokenContext.FOR_ID:
                            case CCTokenContext.IF_ID:
                            case CCTokenContext.WHILE_ID:
                            case CCTokenContext.DO_ID:
                            case CCTokenContext.ELSE_ID:
                            case CCTokenContext.TRY_ID:
                            case CCTokenContext.ASM_ID:
                            case CCTokenContext.CATCH_ID:
                            case CCTokenContext.SWITCH_ID:
                                indent = getTokenIndent(lbss) + getShiftWidth();
                                break;
                            default:
                                indent = getTokenIndent(lbss) + getRightIndentDeclaration();
                                break;
                        }
                        break;

                    case CCTokenContext.RBRACE_ID:
			TokenItem t3 = findStatementStart(token);
			indent = getTokenIndent(t3);
			break;

                    case CCTokenContext.COLON_ID:
                        TokenItem ttt = getVisibility(t);
                        if (ttt != null){
                            indent = getTokenIndent(ttt) + getRightIndentDeclaration();
                        } else {
                            ttt = findAnyToken(t, null,
                                    new TokenID[] {CCTokenContext.CASE,
                                    CCTokenContext.DEFAULT,
                                    CCTokenContext.QUESTION,
                                    CCTokenContext.PRIVATE,
                                    CCTokenContext.PROTECTED,
                                    CCTokenContext.PUBLIC}, t.getTokenContextPath(), true);
                            if (ttt != null) {
                                int id = ttt.getTokenID().getNumericID();
                                if (id == CCTokenContext.QUESTION_ID) {
                                    indent = getTokenIndent(ttt) + getShiftWidth();
                                } else if (id == CCTokenContext.CASE_ID || id == CCTokenContext.DEFAULT_ID){
                                    indent = getTokenIndent(ttt) + getRightIndentSwitch();
                                } else {
                                    indent = getTokenIndent(t);// + getShiftWidth();
                                }
                            } else {
                                // Indent of line with ':' plus one indent level
                                indent = getTokenIndent(t);// + getShiftWidth();
                            }
                        }
			break;

                    case CCTokenContext.QUESTION_ID:
                        indent = getTokenIndent(t) + getShiftWidth();
                        break;
                    case CCTokenContext.DO_ID:
                    case CCTokenContext.ELSE_ID:
                        indent = getTokenIndent(t) + getRightIndent();
                        break;

                    case CCTokenContext.RPAREN_ID:
                        // Try to find the matching left paren
                        TokenItem rpmt = findMatchingToken(t, null, CCTokenContext.LPAREN, true);
                        if (rpmt != null) {
                            rpmt = findImportantToken(rpmt, null, true, true);
                            // Check whether there are the indent changing kwds
                            if (rpmt != null && rpmt.getTokenContextPath() == tokenContextPath) {
                                switch (rpmt.getTokenID().getNumericID()) {
                                case CCTokenContext.FOR_ID:
                                case CCTokenContext.IF_ID:
                                case CCTokenContext.WHILE_ID:
                                    // Indent one level
                                    indent = getTokenIndent(rpmt) + getRightIndent();
                                    break;
                                case CCTokenContext.IDENTIFIER_ID:
                                    if (token != null && token.getTokenID().getNumericID() == CCTokenContext.IDENTIFIER_ID) {
                                        indent = getTokenIndent(t);
                                    }
                                    break;
                                }
                            }
                        }
                        if (indent < 0) {
                            indent = computeStatementIndent(t);
                        }
                        break;

                    case CCTokenContext.IDENTIFIER_ID:
                        if (token != null && token.getTokenID().getNumericID() == CCTokenContext.IDENTIFIER_ID) {
                            indent = getTokenIndent(t);
                            break;
                        }
                        indent = computeStatementIndent(t);
                        break;
                        
                    case CCTokenContext.COMMA_ID:
                        if (isEnumComma(t)) {
                            indent = getTokenIndent(t);
                            break;
                        }
                        indent = computeStatementIndent(t);
                        break;
                    default:
                        indent = computeStatementIndent(t);
                        break;
                }

                if (indent < 0) { // no indent found yet
                    indent = getTokenIndent(t);
                }
            }
        }
        
        if (indent < 0) { // no important token found
            indent = 0;
        }
        return indent;
    }

    private int computeStatementIndent(final TokenItem t) {
        int indent;
        // Find stmt start and add continuation indent
        TokenItem stmtStart = findStatementStart(t);
        indent = getTokenIndent(stmtStart);
        //int tindent = getTokenIndent(t);
        //if (tindent > indent)
        //    return tindent;
        
        if (stmtStart != null) {
            // Check whether there is a comma on the previous line end
            // and if so then also check whether the present
            // statement is inside array initialization statement
            // and not inside parents and if so then do not indent
            // statement continuation
            if (t != null && tokenEquals(t, CCTokenContext.COMMA, tokenContextPath)) {
                if (isArrayInitializationBraceBlock(t, null) &&
                    getLeftParen(t, stmtStart)==null) {
                    return indent;
                }
                TokenItem lparen = getLeftParen(t, stmtStart);
                if (lparen != null){
                    TokenItem prev = findImportantToken(lparen, null, true, true);
                    if (prev != null && 
                        prev.getTokenID().getNumericID() == CCTokenContext.IDENTIFIER_ID){
                        if (isStatement(stmtStart)) {
                            if (alignMultilineCallArgs()){
                                return getVisualColumnOffset(getPosition(lparen, 0))+1;
                            }
                        } else {
                            if (alignMultilineMethodParams()){
                                return getVisualColumnOffset(getPosition(lparen, 0))+1;
                            }
                        }
                    }
                }
            } else if (!isStatement(stmtStart)){
                return indent;
            }
            indent += getFormatStatementContinuationIndent();
        }
        return indent;
    }

    private boolean isStatement(TokenItem t){
        boolean likeDeclaration = false;
        boolean findLParen = false;
        boolean findQuestion = false;
        int identifiers = 0;
        while (t != null) {
            if (t.getTokenContextPath() == tokenContextPath) {
                switch (t.getTokenID().getNumericID()) {
                    case CCTokenContext.EQ_ID:
                    case CCTokenContext.AND_EQ_ID:
                    case CCTokenContext.OR_EQ_ID:
                    case CCTokenContext.MUL_EQ_ID:
                    case CCTokenContext.DIV_EQ_ID:
                    case CCTokenContext.XOR_EQ_ID:
                    case CCTokenContext.MOD_EQ_ID:
                    case CCTokenContext.DELETE_ID:
                    case CCTokenContext.RETURN_ID:
                    case CCTokenContext.BREAK_ID:
                    case CCTokenContext.CASE_ID:
                    case CCTokenContext.CATCH_ID:
                    case CCTokenContext.CONTINUE_ID:
                    case CCTokenContext.DEFAULT_ID:
                    case CCTokenContext.DO_ID:
                    case CCTokenContext.ELSE_ID:
                    case CCTokenContext.FOR_ID:
                    case CCTokenContext.GOTO_ID:
                    case CCTokenContext.IF_ID:
                    case CCTokenContext.SIZEOF_ID:
                    case CCTokenContext.SWITCH_ID:
                    case CCTokenContext.THIS_ID:
                    case CCTokenContext.THROW_ID:
                    case CCTokenContext.TRY_ID:
                    case CCTokenContext.USING_ID:
                    case CCTokenContext.WHILE_ID:
                        return true;
                    case CCTokenContext.SEMICOLON_ID:
                        if (likeDeclaration) {
                            return false;
                        }
                        return true;
                    case CCTokenContext.FRIEND_ID:
                    case CCTokenContext.EXPLICIT_ID:
                    case CCTokenContext.EXTERN_ID:
                    case CCTokenContext.CLASS_ID:
                    case CCTokenContext.STATIC_ID:
                    case CCTokenContext.OPERATOR_ID:
                    case CCTokenContext.PRIVATE_ID:
                    case CCTokenContext.PROTECTED_ID:
                    case CCTokenContext.PUBLIC_ID:
                    case CCTokenContext.NAMESPACE_ID:
                    case CCTokenContext.TEMPLATE_ID:
                    case CCTokenContext.UNION_ID:
                    case CCTokenContext.ENUM_ID:
                    case CCTokenContext.VIRTUAL_ID:
                    case CCTokenContext.INLINE_ID:
                    case CCTokenContext.LBRACE_ID:
                        return false;
                    case CCTokenContext.COLON_ID:
                        if (!findQuestion) {
                            return false;
                        }
                        break;
                    case CCTokenContext.QUESTION_ID:
                        findQuestion = true;
                        break;
                    case CCTokenContext.SCOPE_ID:
                        if (!findLParen && identifiers == 1){
                            likeDeclaration = true;
                        }
                        break;
                    case CCTokenContext.RPAREN_ID:
                        break;
                    case CCTokenContext.LPAREN_ID:
                        if (!findLParen && identifiers > 1){
                            likeDeclaration = true;
                        }
                        findLParen = true;
                        break;
                    case CCTokenContext.ASM_ID:
                    case CCTokenContext.AUTO_ID:
                    case CCTokenContext.BOOLEAN_ID:
                    case CCTokenContext.CHAR_ID:
                    case CCTokenContext.DOUBLE_ID:
                    case CCTokenContext.EXPORT_ID:
                    case CCTokenContext.FLOAT_ID:
                    case CCTokenContext.INT_ID:
                    case CCTokenContext.LONG_ID:
                    case CCTokenContext.MUTABLE_ID:
                    case CCTokenContext.REGISTER_ID:
                    case CCTokenContext.SHORT_ID:
                    case CCTokenContext.SIGNED_ID:
                    case CCTokenContext.STRUCT_ID:
                    case CCTokenContext.TYPEDEF_ID:
                    case CCTokenContext.TYPENAME_ID:
                    case CCTokenContext.UNSIGNED_ID:
                    case CCTokenContext.VOID_ID:
                    case CCTokenContext.WCHAR_T_ID:
                    case CCTokenContext.VOLATILE_ID:
                    case CCTokenContext.CONST_ID:
                        if (!findLParen) {
                            return false;
                        }
                        break;
                    case CCTokenContext.IDENTIFIER_ID:
                        identifiers++;
                        break;
                }
            }
            t = t.getNext();
        }
        return true;
    }
    
    public FormatTokenPosition indentLine(FormatTokenPosition pos, boolean ignoreInComment) {
        int indent = 0; // Desired indent

        // Get the first non-whitespace position on the line
        FormatTokenPosition firstNWS = findLineFirstNonWhitespace(pos);
        if (firstNWS != null) { // some non-WS on the line
            if (isPreprocessorLine(firstNWS.getToken())){
                 // leave untouched for now, (bug#22570)
                if (!isPreprocessorAtLineStart(firstNWS.getToken())) {
                    return pos;
                }
            } else if (isComment(firstNWS)) { // comment is first on the line
                if (ignoreInComment) {
                    return pos;
                } else {
                    if (isMultiLineComment(firstNWS) && firstNWS.getOffset() != 0) {

                        // Indent the inner lines of the multi-line comment by one
                        indent = getLineIndent(getPosition(firstNWS.getToken(), 0), true) + 1;

                        // If the line is inside multi-line comment and doesn't contain '*'
                        if (isIndentOnly()) {
                            if (getChar(firstNWS) != '*') { // e.g. not for '*/'
                                if (isCCDocComment(firstNWS.getToken())) {
                                    if (getFormatLeadingStarInComment()) {
                                        insertString(firstNWS, "* "); // NOI18N
                                        setIndentShift(2);
                                    }
                                }
                            }
                        } else {
                            if (getChar(firstNWS) != '*') {
                                if (isCCDocComment(firstNWS.getToken())) {
                                    if (getFormatLeadingStarInComment()) {
                                        insertString(firstNWS, "* "); // NOI18N
                                    }
                                } else {
                                    // For non-java-doc not because it can be commented code
                                    indent = getLineIndent(pos, true);
                                }
                            }
                        }
                    } else if (!isMultiLineComment(firstNWS)) { // line-comment
                        indent = firstNWS.equals(findLineStart(firstNWS)) ? getLineIndent(firstNWS, true) : findIndent(firstNWS.getToken());
                    } else { // multi-line comment
                        if (isIndentOnly() && firstNWS.getOffset() != 0) {
                            return pos;
                        } else {
                            if (isCCDocComment(firstNWS.getToken())) {
                                indent = findIndent(firstNWS.getToken());
                            } else {
                                // check whether the multiline comment isn't finished on the same line (see issue 12821)
                                if (firstNWS.getToken().getImage().indexOf('\n') == -1) {
                                    indent = findIndent(firstNWS.getToken());
                                } else {
                                    indent = getLineIndent(firstNWS, true);
                                }
                            }
                        }
                    }
                }
            } else { // first non-WS char is not comment
                indent = findIndent(firstNWS.getToken());
            }
        } else { // whole line is WS
            // Can be empty line inside multi-line comment
            TokenItem token = pos.getToken();
            if (token == null) {
                token = findLineStart(pos).getToken();
                if (token == null) { // empty line
                    token = getLastToken();
                }
            }

            if (token != null && isMultiLineComment(token)) {
                if (getFormatLeadingStarInComment() && (isIndentOnly() || isCCDocComment(token))) {
                    // Insert initial '*'
                    insertString(pos, "*"); // NOI18N
                    setIndentShift(1);
                }
                
                // Indent the multi-comment
                indent = getVisualColumnOffset(getPosition(token, 0)) + 1;
            } else { // non-multi-line comment
                indent = findIndent(pos.getToken());
            }
        }

        // For indent-only always indent
        return changeLineIndent(pos, indent);
    }

    /** 
     * Check whether the given semicolon is inside the for() statement
     *
     * @param token token to check. It must be a semicolon
     * @return true if the given semicolon is inside the for() statement, or false otherwise
     */
    public boolean isForLoopSemicolon(TokenItem token) {
        if (token == null || !tokenEquals(token, CCTokenContext.SEMICOLON, tokenContextPath)) {
            throw new IllegalArgumentException("Only accept ';'."); // NOI18N
        }

        int parDepth = 0; // parenthesis depth
        int braceDepth = 0; // brace depth
        boolean semicolonFound = false; // next semicolon
        token = token.getPrevious(); // ignore this semicolon
        while (token != null) {
            if (tokenEquals(token, CCTokenContext.LPAREN, tokenContextPath)) {
                if (parDepth == 0) { // could be a 'for ('
                    FormatTokenPosition tp = getPosition(token, 0);
                    tp = findImportant(tp, null, false, true);
                    if (tp != null && tokenEquals(tp.getToken(),
                            CCTokenContext.FOR, tokenContextPath)) {
                        return true;
                    }
                    return false;
                } else { // non-zero depth
                    parDepth--;
                }
            } else if (tokenEquals(token, CCTokenContext.RPAREN, tokenContextPath)) {
                parDepth++;
            } else if (tokenEquals(token, CCTokenContext.LBRACE, tokenContextPath)) {
                if (braceDepth == 0) { // unclosed left brace
                    return false;
                }
                braceDepth--;
            } else if (tokenEquals(token, CCTokenContext.RBRACE, tokenContextPath)) {
                braceDepth++;
            } else if (tokenEquals(token, CCTokenContext.SEMICOLON, tokenContextPath)) {
                if (semicolonFound) { // one semicolon already found
                    return false;
                }
                semicolonFound = true;
            }

            token = token.getPrevious();
        }

        return false;
    }

    private TokenItem findClassifierStart(TokenItem token) {
        while (true) {
            token = findStatement(token);
            if (token == null) {
                return null;
            }
            switch (token.getTokenID().getNumericID()) {
                case CCTokenContext.LBRACE_ID:
                case CCTokenContext.RBRACE_ID:
                case CCTokenContext.SEMICOLON_ID:
                    return null;
                case CCTokenContext.TEMPLATE_ID:
                    return findStatementStart(token);
            }
        }
    }
    
    private TokenItem getVisibility(TokenItem token) {
        TokenItem t = token;
        if (t != null){
            t = token.getPrevious();
        }
        while (t != null) {
            if (t.getTokenContextPath() == tokenContextPath) {
                switch (t.getTokenID().getNumericID()) {
                    case CCTokenContext.SEMICOLON_ID:
                    case CCTokenContext.LBRACE_ID:
                    case CCTokenContext.RBRACE_ID:
                        return null;
                    case CCTokenContext.PRIVATE_ID:
                    case CCTokenContext.PROTECTED_ID:
                    case CCTokenContext.PUBLIC_ID:
                        return t;
                }
            }
            t = t.getPrevious();
        }
        return null;
    }

    /**
     * Check whether there are left parenthesis before the given token
     * until the limit token.
     * 
     * @param token non-null token from which to start searching back.
     * @param limitToken limit token when reached the search will stop
     *  with returning false.
     * @return true if there is LPAREN token before the given token
     *  (while respecting paren nesting).
     */
    private TokenItem getLeftParen(TokenItem token, TokenItem limitToken) {
        int depth = 0;
        token = token.getPrevious();

        while (token != null && token != limitToken) {
            if (tokenEquals(token, CCTokenContext.LPAREN, tokenContextPath)) {
                if (--depth < 0) {
                    return token;
                }

            } else if (tokenEquals(token, CCTokenContext.RPAREN, tokenContextPath)) {
                depth++;
            }
            token = token.getPrevious();
        }
        return null;
    }

    /**
     * Check whether the given token is located in array initialization block.
     * 
     * @param token non-null token from which to start searching back.
     * @param limitToken limit token when reached the search will stop
     *  with returning false.
     * @return true if the token is located inside the brace block of array
     *  initialization.
     */
    private boolean isArrayInitializationBraceBlock(TokenItem token, TokenItem limitToken) {
        int depth = 0;
        token = token.getPrevious();

        while (token != null && token != limitToken && token.getTokenContextPath() == tokenContextPath) {
            switch (token.getTokenID().getNumericID()) {
                case CCTokenContext.RBRACE_ID:
                    depth++;
                    break;

                case CCTokenContext.LBRACE_ID:
                    depth--;
                    if (depth < 0) {
                        TokenItem prev = findImportantToken(token, limitToken, true, true);
                        // Array initialization left brace should be preceded
                        // by either '=' or ']' i.e.
                        // either "String array = { "a", "b", ... }"
                        // or     "String array = new String[] { "a", "b", ... }"
                        return (prev != null && prev.getTokenContextPath() == tokenContextPath
                                && (CCTokenContext.RBRACKET.equals(prev.getTokenID())
                                 || CCTokenContext.EQ.equals(prev.getTokenID())));
                    }
                    break;

                // Array initialization block should not contain statements or ';'
                case CCTokenContext.DO_ID:
                case CCTokenContext.FOR_ID:
                case CCTokenContext.IF_ID:
                case CCTokenContext.WHILE_ID:
                case CCTokenContext.SEMICOLON_ID:
                    if (depth == 0) {
                        return false;
                    }
            }                    
            token = token.getPrevious();
        }
        return false;
    }

    public boolean isEnumComma(TokenItem token) {
        while (token != null && tokenEquals(token, CCTokenContext.COMMA, tokenContextPath)) {
            TokenItem itm = findStatementStart(token);
            if (itm == token) {
                break;
            }
            token = itm;
        }
        if (token != null && 
            (tokenEquals(token, CCTokenContext.IDENTIFIER, tokenContextPath) ||
             tokenEquals(token, CCTokenContext.DOT, tokenContextPath))) {
            TokenItem itm = findImportantToken(token, null, true, true);
            if (itm != null && tokenEquals(itm, CCTokenContext.LBRACE, tokenContextPath)) {
                TokenItem startItem = findStatementStart(itm);
                if (startItem != null && findToken(startItem, itm, CCTokenContext.ENUM, tokenContextPath, null, false) != null)
                    return true;
                if (startItem != null && findToken(startItem, itm, CCTokenContext.EQ, tokenContextPath, null, false) != null)
                    return true;
            }
        }
        return false;
    }

    private CodeStyle getCodeStyle(){
        return this.codeStyle;
    }

    public boolean getFormatSpaceBeforeMethodCallParenthesis() {
        //return getCodeStyle().getFormatSpaceBeforeParenthesis();
        return getCodeStyle().spaceBeforeMethodCallParen();
    }

    public boolean getFormatSpaceAfterComma() {
        return getCodeStyle().spaceAfterComma();
    }

    public boolean indentCasesFromSwitch() {
        return getCodeStyle().indentCasesFromSwitch();
    }

    public boolean getFormatNewlineBeforeBrace() {
        return getCodeStyle().getFormatNewlineBeforeBrace() != CodeStyle.BracePlacement.SAME_LINE;
    }

    public boolean isHalfIndentNewlineBeforeBrace() {
        return getCodeStyle().getFormatNewlineBeforeBrace() == CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED;
    }

    public boolean getFormatNewlineBeforeBraceSwitch() {
        return getCodeStyle().getFormatNewLineBeforeBraceSwitch() != CodeStyle.BracePlacement.SAME_LINE;
    }

    public boolean isHalfIndentNewlineBeforeBraceSwitch() {
        return getCodeStyle().getFormatNewLineBeforeBraceSwitch() == CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED;
    }

    public boolean getFormatNewlineBeforeBraceDeclaration() {
        return getCodeStyle().getFormatNewlineBeforeBraceDeclaration() != CodeStyle.BracePlacement.SAME_LINE;
    }

    public boolean isHalfIndentNewlineBeforeBraceDeclaration() {
        return getCodeStyle().getFormatNewlineBeforeBraceDeclaration() == CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED;
    }
    
    public boolean getFormatLeadingStarInComment() {
        return getCodeStyle().getFormatLeadingStarInComment();
    }

    private int getFormatStatementContinuationIndent() {
        return getCodeStyle().getFormatStatementContinuationIndent();
    }

    @Override
    public int getShiftWidth() {
        return getCodeStyle().indentSize();
    }

    private boolean alignMultilineCallArgs() {
        return getCodeStyle().alignMultilineCallArgs();
    }

    private boolean alignMultilineMethodParams() {
        return getCodeStyle().alignMultilineMethodParams();
    }

    /*   this is fix for bugs: 7980 and 9111. if user enters
     *        {   foo();
     *   and press enter at the end of the line, she wants
     *   to be indented just under "f" in "foo();" and not under the "{" 
     *   as it happens now. and this is what findLineFirstNonWhitespaceAndNonLeftBrace checks
     */    
    public FormatTokenPosition findLineFirstNonWhitespaceAndNonLeftBrace(FormatTokenPosition pos) {
        // first call the findLineFirstNonWhitespace
        FormatTokenPosition ftp = super.findLineFirstNonWhitespace(pos);
        if (ftp == null) { // no line start, no WS
            return null;
        }

        // now checks if the first non-whitespace char is "{"
        // if it is, find the next non-whitespace char
        if (!ftp.getToken().getImage().startsWith("{")) // NOI18N
            return ftp;

        // if the left brace is closed on the same line - "{ foo(); }"
        // it must be ignored. otherwise next statement is incorrectly indented 
        // under the "f" and not under the "{" as expected
        FormatTokenPosition eolp = findNextEOL(ftp);
        TokenItem rbmt = findMatchingToken(ftp.getToken(), 
            eolp != null ? eolp.getToken() : null, CCTokenContext.RBRACE, false);
        if (rbmt != null)
            return ftp;
        
        FormatTokenPosition ftp_next = getNextPosition(ftp);
        if (ftp_next == null)
            return ftp;
        
        FormatTokenPosition ftp2 = findImportant(ftp_next, null, true, false);
        if (ftp2 != null)
            return ftp2;
        else
            return ftp;
    }

    public TokenItem findImportantToken(TokenItem startToken, TokenItem limitToken, boolean backward, boolean ignorePreprocessor) {
        TokenItem t = findImportantToken(startToken, limitToken, backward);
        if (ignorePreprocessor) {
            // preprocessor tokens are not important (bug#22570)
            while (t != null && getPosition(t, 0) != null){
                 FormatTokenPosition ft = findLineFirstNonWhitespace(getPosition(t, 0));
                 if (ft == null) {
                     return null;
                 }
                 if (isPreprocessorLine(ft.getToken())) {
                    t = backward ? t.getPrevious() : t.getNext();
                    if (t != null) {
                        t = findImportantToken(t, limitToken, backward);
                    }
                 } else {
                     break;
                 }
            }
        }
        return t;
    }
}
