/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2001-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.editor.cplusplus;

import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.ext.FormatTokenPosition;
import org.netbeans.editor.ext.ExtFormatSupport;
import org.netbeans.editor.ext.FormatWriter;

/**
* CC indentation services are located here
*
* duped from editor/libsrc/org/netbeans/editor/ext/java/JavaFormatSupport.java
*/

public class CCFormatSupport extends ExtFormatSupport {

    private TokenContextPath tokenContextPath;

    public CCFormatSupport(FormatWriter formatWriter) {
        this(formatWriter, CCTokenContext.contextPath);
    }

    public CCFormatSupport(FormatWriter formatWriter, TokenContextPath tokenContextPath) {
        super(formatWriter);
        this.tokenContextPath = tokenContextPath;
    }

    public TokenContextPath getTokenContextPath() {
        return tokenContextPath;
    }

    public boolean isComment(TokenItem token, int offset) {
        TokenID tokenID = token.getTokenID();
        return (token.getTokenContextPath() == tokenContextPath
                && (tokenID == CCTokenContext.LINE_COMMENT
                    || tokenID == CCTokenContext.BLOCK_COMMENT));
    }
    
     /** Is given token a preprocessor **/
     public boolean isPreprocessorAtLineStart(TokenItem token){
         if( getSettingBoolean(CCSettingsNames.FORMAT_PREPROCESSOR_AT_LINE_START, 
                    CCSettingsDefaults.defaultFormatPreprocessorAtLineStart) ) 
             return false;
         return (token != null && token.getImage().startsWith("#") && // NOI18N
		    getVisualColumnOffset(getPosition(token,0)) == 0);
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

    public TokenID getWhitespaceTokenID() {
        return CCTokenContext.WHITESPACE;
    }

    public TokenContextPath getWhitespaceTokenContextPath() {
        return tokenContextPath;
    }

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
     *  If the start of the statement is not found, null is retrurned.
     */
    public TokenItem findStatement(TokenItem token) {
        TokenItem lit = null; // last important token
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
                    case CCTokenContext.RBRACE_ID:
                    case CCTokenContext.COLON_ID:
                    case CCTokenContext.ELSE_ID:
                        return (lit != null) ? lit : t;

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
                                mt = findImportantToken(mt.getNext(), token, false);
                                if (mt != null) {
                                    return mt;
                                }
                            }
                        }
                        // No further stmt found, return this one
                        return t;
                }
                // Remember last important token (preprocessor token are not important (?) (4922370))
                if (!isPreprocessorAtLineStart(t) && isImportant(t, 0)) {
                    lit = t;
                }
            }
            t = t.getPrevious();
        }
        return lit;
    }


    /** Find the 'if' when the 'else' is provided.
     * @param elseToken the token with the 'else' command
     *  for which the 'if' is being searched.
     * @return corresponding 'if' token or null if there's
     *  no corresponding 'if' statement.
     */
    public TokenItem findIf(TokenItem elseToken) {
        if (elseToken == null || !tokenEquals(elseToken,
                    CCTokenContext.ELSE, tokenContextPath)
        ) {
            throw new IllegalArgumentException(ABundle.getText(
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
        if (caseToken == null || 
             (!tokenEquals(caseToken, CCTokenContext.CASE,
               tokenContextPath)
               && !tokenEquals(caseToken, CCTokenContext.DEFAULT,
                    tokenContextPath))
        ) {
            throw new IllegalArgumentException(ABundle.getText(
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

    /** Find the 'try' when the 'catch' is provided.
     * @param catchToken the token with the 'catch' command
     *  for which the 'try' is being searched.
     * @return corresponding 'try' token or null if there's
     *  no corresponding 'try' statement.
     */
    public TokenItem findTry(TokenItem catchToken) {
        if (catchToken == null || 
             (!tokenEquals(catchToken, CCTokenContext.CATCH,
               tokenContextPath))
        ) {
            throw new IllegalArgumentException(ABundle.getText(
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
        TokenItem t = findStatement(token);
        if (t != null) {
            switch (t.getTokenID().getNumericID()) {
                case CCTokenContext.SEMICOLON_ID: // ';' found
                    TokenItem scss = findStatement(t);
                    switch (scss.getTokenID().getNumericID()) {
                        case CCTokenContext.LBRACE_ID: // '{' then ';'
                        case CCTokenContext.RBRACE_ID: // '}' then ';'
                        case CCTokenContext.COLON_ID: // ':' then ';'
                        case CCTokenContext.CASE_ID: // 'case' then ';'
                        case CCTokenContext.DEFAULT_ID:
                        case CCTokenContext.SEMICOLON_ID: // ';' then ';'
                            return t; // return ';'

                        case CCTokenContext.DO_ID:
                        case CCTokenContext.FOR_ID:
                        case CCTokenContext.IF_ID:
                        case CCTokenContext.WHILE_ID:
                            return findStatementStart(t);

                        case CCTokenContext.ELSE_ID: // 'else' then ';'
                            // Find the corresponding 'if'
                            TokenItem ifss = findIf(scss);
                            if (ifss != null) { // 'if' ... 'else' then ';'
                                return findStatementStart(ifss);

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
                                        return findStatementStart(bscss);

                                    case CCTokenContext.ELSE_ID:
                                        // Find the corresponding 'if'
                                        ifss = findIf(bscss);
                                        if (ifss != null) { // 'if' ... 'else' ... ';'
                                            return findStatementStart(ifss);

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
                    TokenItem lb = findMatchingToken(t, null,
                            CCTokenContext.LBRACE, true);
                    if (lb != null) { // valid matching left-brace
                        // Find a stmt-start of the '{'
                        TokenItem lbss = findStatement(lb);
                        if (lbss != null) {
                            switch (lbss.getTokenID().getNumericID()) {
                                case CCTokenContext.ELSE_ID: // 'else {'
                                    // Find the corresponding 'if'
                                    TokenItem ifss = findIf(lbss);
                                    if (ifss != null) { // valid 'if'
                                        return findStatementStart(ifss);
                                    } else {
                                        return lbss; // return 'else'
                                    }

                                case CCTokenContext.CATCH_ID: // 'catch (...) {'
                                    // Find the corresponding 'try'
                                    TokenItem tryss = findTry(lbss);
                                    if (tryss != null) { // valid 'try'
                                        return findStatementStart(tryss);
                                    } else {
                                        return lbss; // return 'catch'
                                    }

                                case CCTokenContext.DO_ID:
                                case CCTokenContext.FOR_ID:
                                case CCTokenContext.IF_ID:
                                case CCTokenContext.WHILE_ID:
                                    return findStatementStart(lbss);
                            }
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
                    return (ifss != null) ? findStatementStart(ifss) : t;

                case CCTokenContext.DO_ID:
                case CCTokenContext.FOR_ID:
                case CCTokenContext.IF_ID:
                case CCTokenContext.WHILE_ID:
                    return findStatementStart(t);
            }
        }
        return token; // return original token
    }

    /** Get the indentation for the given token.
     * It first searches whether there's an non-whitespace and a non-leftbrace
     * character on the line with the token and if so,
     * it takes indent of the non-ws char instead.
     * @param token token for which the indent is being searched.
     *  The token itself is ignored and the previous token
     *  is used as a base for the search.
     * @param forceFirstNonWhitespace set true to ignore leftbrace and search 
     * directly for first non-whitespace
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
    
    /** Find the indentation for the first token on the line.
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
                                indent = getTokenIndent(stmt);
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
                    TokenItem rbmt = findMatchingToken(token, null,
                                CCTokenContext.LBRACE, true);
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

                    } else { // no matching left brace
                        indent = getTokenIndent(token); // leave as is
                    }
                    break;

                case CCTokenContext.CASE_ID:
                case CCTokenContext.DEFAULT_ID:
                    TokenItem swss = findSwitch(token);
                    if (swss != null) {
                        indent = getTokenIndent(swss) + getShiftWidth();
                    }
                    break;
            }
        }

        // If indent not found, search back for the first important token
        if (indent < 0) { // if not yet resolved
            TokenItem t = findImportantToken(token, null, true);
            // preprocessor tokens are not important (bug#22570)
            while (t != null && 
		    getPosition(t,0) != null && 
		    findLineFirstNonWhitespace(getPosition(t,0)) != null &&
		    isPreprocessorAtLineStart(findLineFirstNonWhitespace(getPosition(t,0)).getToken())) 
                t = findImportantToken(t, null, true);
            if (t != null) { // valid important token
                switch (t.getTokenID().getNumericID()) {
                    case CCTokenContext.SEMICOLON_ID: // semicolon found
                        TokenItem tt = findStatementStart(token);
                        // preprocessor tokens are not important (bug#22570)
                        while (tt != null && (isPreprocessorAtLineStart(tt) ||
				    tt.getImage().startsWith("\n"))) // NOI18N
			    tt = findStatementStart(tt.getPrevious());                        
                        indent = getTokenIndent(tt);
                            
                        break;

                    case CCTokenContext.LBRACE_ID:
                        TokenItem lbss = findStatementStart(t);
                        indent = getTokenIndent(t) + getShiftWidth();
                        break;

                    case CCTokenContext.RBRACE_ID:
                        if (true) {
                            TokenItem t3 = findStatementStart(token);
                            indent = getTokenIndent(t3);
                            break;
                        }

                        /** Check whether the following situation occurs:
                         *  if (t1)
                         *    if (t2) {
                         *      ...
                         *    }
                         * 
                         *  In this case the indentation must be shifted
                         *  one level back.
                         */
                        TokenItem rbmt = findMatchingToken(t, null,
                                CCTokenContext.LBRACE, true);
                        if (rbmt != null) { // valid matching left-brace
                            // Check whether there's a indent stmt
                            TokenItem t6 = findStatement(rbmt);
                            if (t6 != null) {
                                switch (t6.getTokenID().getNumericID()) {
                                    case CCTokenContext.ELSE_ID:
                                        /* Check the following situation:
                                         * if (t1)
                                         *   if (t2)
                                         *     c1();
                                         *   else {
                                         *     c2();
                                         *   }
                                         */

                                        // Find the corresponding 'if'
                                        t6 = findIf(t6);
                                        if (t6 != null) { // valid 'if'
                                            TokenItem t7 = findStatement(t6);
                                            if (t7 != null) {
                                                switch (t7.getTokenID().getNumericID()) {
                                                    case CCTokenContext.DO_ID:
                                                    case CCTokenContext.FOR_ID:
                                                    case CCTokenContext.IF_ID:
                                                    case CCTokenContext.WHILE_ID:
                                                        indent = getTokenIndent(t7);
                                                        break;

                                                    case CCTokenContext.ELSE_ID:
                                                        indent = getTokenIndent(findStatementStart(t6));
                                                }
                                            }
                                        }
                                        break;

                                    case CCTokenContext.DO_ID:
                                    case CCTokenContext.FOR_ID:
                                    case CCTokenContext.IF_ID:
                                    case CCTokenContext.WHILE_ID:
                                        /* Check the following:
                                         * if (t1)
                                         *   if (t2) {
                                         *     c1();
                                         *   }
                                         */
                                        TokenItem t7 = findStatement(t6);
                                        if (t7 != null) {
                                            switch (t7.getTokenID().getNumericID()) {
                                                case CCTokenContext.DO_ID:
                                                case CCTokenContext.FOR_ID:
                                                case CCTokenContext.IF_ID:
                                                case CCTokenContext.WHILE_ID:
                                                    indent = getTokenIndent(t7);
                                                    break;

                                                case CCTokenContext.ELSE_ID:
                                                    indent = getTokenIndent(findStatementStart(t6));
                                            }
                                        }
                                        break;

                                    case CCTokenContext.LBRACE_ID: // '{' ... '{'
                                        indent = getTokenIndent(rbmt);
                                        break;
                                }
                            }
                            if (indent < 0) {
                                indent = getTokenIndent(t); // indent of original rbrace
                            }
                        } else { // no matching left-brace
                            indent = getTokenIndent(t); // return indent of '}'
                        }
                        break;

                    case CCTokenContext.RPAREN_ID:
                        // Try to find the matching left paren
                        TokenItem rpmt = findMatchingToken(t, null, CCTokenContext.LPAREN, true);
                        if (rpmt != null) {
                            rpmt = findImportantToken(rpmt, null, true);
                            // Check whether there are the indent changing kwds
                            if (rpmt != null && rpmt.getTokenContextPath() == tokenContextPath) {
                                switch (rpmt.getTokenID().getNumericID()) {
                                    case CCTokenContext.FOR_ID:
                                    case CCTokenContext.IF_ID:
                                    case CCTokenContext.WHILE_ID:
                                        // Indent one level
                                        indent = getTokenIndent(rpmt) + getShiftWidth();
                                        break;
                                }
                            }
                        }
                        break;

                    case CCTokenContext.COLON_ID:
                        // Indent of line with ':' plus one indent level
                        indent = getTokenIndent(t) + getShiftWidth();
                        break;

                    case CCTokenContext.DO_ID:
                    case CCTokenContext.ELSE_ID:
                        indent = getTokenIndent(t) + getShiftWidth();
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

    public FormatTokenPosition indentLine(FormatTokenPosition pos) {
        int indent = 0; // Desired indent

        // Get the first non-whitespace position on the line
        FormatTokenPosition firstNWS = findLineFirstNonWhitespace(pos);
        if (firstNWS != null) { // some non-WS on the line
            if (isPreprocessorAtLineStart(firstNWS.getToken())){
                    // leave untouched for now, (bug#22570)
            }else if (isComment(firstNWS)) { // comment is first on the line
                if (isMultiLineComment(firstNWS) && firstNWS.getOffset() != 0) {

                    // Indent the inner lines of the multi-line comment by one
                    indent = getLineIndent(getPosition(firstNWS.getToken(), 0), true) + 1;

                    // If the line is inside multi-line comment and doesn't contain '*'
                    if (!isIndentOnly() && getChar(firstNWS) != '*') {
                        if (!isCCDocComment(firstNWS.getToken())) { //XXXgah changed multiline 
                            // For non-java-doc not because it can be commented code
                            indent = getLineIndent(pos, true);
                        }
                    }

                } else if (!isMultiLineComment(firstNWS)) { // line-comment
                    indent = findIndent(firstNWS.getToken());
                } else { // multi-line comment
                    if (isCCDocComment(firstNWS.getToken()))
                    {
                        indent = findIndent(firstNWS.getToken());
                    }
                    else
                    {
                        indent = getLineIndent(firstNWS, true);
                    }
                }

            } else { // first non-WS char is not comment
                
                /* This is bugfix 10771. See the issue for problem description.
                 * The behaviour before fix was that whenever the lbrace is
                 * entered, the line is indented. This make no sense if a text
                 * exist on the line before the lbrace. In this case we
                 * simply will not indent the line. And this is what next 
                 * "skipIndent" condition does. 
                 */
                if (isIndentOnly())
                {
                    boolean skipIndent = false;
                    FormatTokenPosition eol = findLineEnd(pos);
                    FormatTokenPosition prevEol = findNonWhitespace(eol, null, true, true);
                    if (prevEol != null && prevEol.getToken() != null && 
			(prevEol.getToken().getTokenID() == CCTokenContext.LBRACE || prevEol.getToken().getTokenID() == CCTokenContext.RBRACE))
                    {
                        // if the last token on the line is "{" or "}", check if something is before it:
                        FormatTokenPosition prevprevEol = findNonWhitespace(prevEol, null, true, true);
                        if (prevprevEol != null && prevprevEol.getToken() != null)
                            skipIndent = true;  // do not indent if exist some text before "{" or "}" on the line
                    }
                    if (skipIndent)
                        return firstNWS;    // just left the indentation, value is not important
                }
                
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

            if (token != null && isMultiLineComment(token)) { //XXXgah changed multiline 
                // Indent the multi-comment
                indent = getVisualColumnOffset(getPosition(token, 0));

            } else { // non-multi-line comment
                indent = findIndent(pos.getToken());
            }
        }

        // For indent-only always indent
        return changeLineIndent(pos, indent);
    }

    /** Check whether the given semicolon is inside
     * the for() statement.
     * @param token token to check. It must be a semicolon.
     * @return true if the given semicolon is inside
     *  the for() statement, or false otherwise.
     */
    public boolean isForLoopSemicolon(TokenItem token) {
        if (token == null || !tokenEquals(token,
                    CCTokenContext.SEMICOLON, tokenContextPath)
        ) {
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
                            CCTokenContext.FOR, tokenContextPath)
                    ) {
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

    public boolean getFormatSpaceBeforeParenthesis() {
        return getSettingBoolean(CCSettingsNames.FORMAT_SPACE_BEFORE_PARENTHESIS,
                                 CCSettingsDefaults.defaultFormatSpaceBeforeParenthesis);
    }

    public boolean getFormatSpaceAfterComma() {
        return getSettingBoolean(CCSettingsNames.FORMAT_SPACE_AFTER_COMMA,
                                 CCSettingsDefaults.defaultFormatSpaceAfterComma);
    }

    public boolean getFormatNewlineBeforeBrace() {
        return getSettingBoolean(CCSettingsNames.FORMAT_NEWLINE_BEFORE_BRACE,
                                 CCSettingsDefaults.defaultFormatNewlineBeforeBrace);
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

}
