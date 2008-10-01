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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.editor.fortran;

import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenID;

import org.netbeans.modules.cnd.settings.CppSettings;

/**
 * Syntax analyzes for Fortran source files.
 * Tokens and internal states are given below.
 *
 */

public class FSyntax extends Syntax {

    //internal analyzer states
    //numbers assigned to states are not important as long as they are unique
    private static final int AFTER_SLASH = 1;       // after slash char
    private static final int AFTER_EQ = 2;          // after '='
    private static final int AFTER_STAR = 3;        // after '*'
    private static final int AFTER_LESSTHAN = 4;    // after '<'
    private static final int AFTER_GREATERTHAN = 5; // after '>'
    private static final int AFTER_B = 6;           // after 'b' or 'B'
    private static final int AFTER_O = 7;           // after 'o' or 'O'
    private static final int AFTER_Z = 8;           // after 'z' or 'Z'
    private static final int AFTER_DOT = 9;         // after '.'
    
    private static final int IN_STRING = 10;        // inside string constant
    private static final int IN_STRING_AFTER_BSLASH = 11; //inside string const
    // after backslash
    private static final int IN_LINE_COMMENT = 12;     // inside line comment
    private static final int IN_IDENTIFIER = 13;       // inside identifier
    private static final int IN_DOT_IDENTIFIER = 14;   // inside .identifier
    private static final int IN_WHITESPACE = 15;       // inside white space
    private static final int IN_INT = 16;    // integer number
    private static final int IN_BINARY = 17; // binary number
    private static final int IN_OCTAL = 18;  // octal number
    private static final int IN_HEX = 19;    // hex number
    private static final int IN_REAL = 20;   // real number
    
    /**specifies if the string is defined in double quotes
     * or single quote
     */
    private static boolean STRING_IN_DOUBLE_QUOTE = true;
    
    /**this variable is put for detecting the "_" in integers and reals
     */
    private static boolean HAS_NUMERIC_UNDERSCORE = false;
    
    /**points to the last newline character
     */
    protected int lastNL = 0;
    
    /** constructor
     */
    public FSyntax() {
        tokenContextPath = FTokenContext.contextPath;
    }
    
    /** This function returns true if the colun number
     *  exceeds the limit defined by FSettingsDefaults.maximumTextWidth
     *  otherwise it returns null
     */
    protected boolean isLineBeyondLimit() {
        if ( (offset - lastNL > FSettingsDefaults.maximumTextWidth)  &&
        (lastNL >= 0) &&
        (state != IN_LINE_COMMENT) ) {
            state = IN_LINE_COMMENT;
            return true;
        }
        
        return false;
    }
    
    /** This is core function of analyzer and it returns either the token-id
     *  or null to indicate that the end of buffer was found.
     *  The function scans the active character and does one or more
     *  of the following actions:
     *  1. change internal analyzer state
     *  2. set the token-context-path and return token-id
     *  3. adjust current position to signal different end of token;
     *     the character that offset points to is not included in the token
     */
    @Override
    protected TokenID parseToken() {
        char actChar;
        //WHILE OFFSET
        while(offset < stopOffset) {
            actChar = buffer[offset];
            
            //STATE SWITCH
            switch (state) {
                //INIT STATE
                case INIT:
                    if (isLineBeyondLimit()) {
                        offset--; //reevaluate the char
                        break;
                    }
                    switch (actChar) {
                        case '\n':
                            lastNL = offset;
                            offset++;
                            return  FTokenContext.WHITESPACE;
                        case 'b':
                        case 'B':
                            state = AFTER_B;
                            break;
                        case 'o':
                        case 'O':
                            state = AFTER_O;
                            break;
                        case 'z':
                        case 'Z':
                            state = AFTER_Z;
                            break;
                        case '"':
                            //make sure that this case is always after cases b, o and z
                            state = IN_STRING;
                            STRING_IN_DOUBLE_QUOTE = true;
                            break;
                        case '\'': {
                            //make sure that this case is always after cases b, o and z
			    if (offset > 0) { //fix for 4838228 Error generated ...
				char beforeApostrophe = buffer[offset - 1];
				if (Character.isJavaIdentifierPart(beforeApostrophe)) { //e.g. L'
				    offset++;
				    return  FTokenContext.APOSTROPHE_CHAR;
				}
			    }
                            state = IN_STRING;
                            STRING_IN_DOUBLE_QUOTE = false;
                            break;
                        }
                        case '/':
                            state = AFTER_SLASH;
                            break;
                        case '=':
                            state = AFTER_EQ;
                            break;
                        case '+':
                            offset++;
                            return FTokenContext.OP_PLUS;
                        case '-':
                            offset++;
                            return FTokenContext.OP_MINUS;
                        case '*':
                            state = AFTER_STAR;
                            break;
                        case '!':
                            // Fortran comments begin with a ! and last to end of line
                            state = IN_LINE_COMMENT;
                            break;
                        case 'C':
                        case 'c':
                            if( (lastNL == offset-1 || offset == 0) && !CppSettings.getDefault().isFreeFormatFortran() ) {
                                state = IN_LINE_COMMENT;
                            } else {
                                state = IN_IDENTIFIER;
                            } 
                            break;
                        case '<':
                            state = AFTER_LESSTHAN;
                            break;
                        case '>':
                            state = AFTER_GREATERTHAN;
                            break;
                        case '.':
                            state = AFTER_DOT;
                            break;
                        case ',':
                            offset++;
                            return FTokenContext.COMMA;
                        case ':':
                            offset++;
                            return FTokenContext.COLON;
                        case '%':
                            offset++;
                            return FTokenContext.PERCENT;
                        case '&':
                            offset++;
                            return FTokenContext.AMPERSAND;
                        case '(':
                            offset++;
                            return FTokenContext.LPAREN;
                        case ')':
                            offset++;
                            return FTokenContext.RPAREN;
                        case ';':
                            offset++;
                            return FTokenContext.SEMICOLON;
                        case '?':
                            offset++;
                            return FTokenContext.QUESTION_MARK;
                        case '$':
                            offset++;
                            return FTokenContext.CURRENCY;
                        default:
                            // Check for whitespace
                            if (Character.isWhitespace(actChar)) {
                                state = IN_WHITESPACE;
                                break;
                            }
                            
                            // Check for digit
                            if (Character.isDigit(actChar)) {
                                state = IN_INT;
                                break;
                            }
                            
                            // Check for identifier
                            // To find out why we're using isJAVAidentifier
                            // here, grep for isJavaIdentifierStart in
                            // CCSyntax.java
                            if (Character.isJavaIdentifierStart(actChar)) {
                                state = IN_IDENTIFIER;
                                break;
                            }
                            
                            offset++;
                            return FTokenContext.ERR_INVALID_CHAR;
                    }//switch(actchar)
                    break;
                    //END INIT STATE
                    
                case IN_WHITESPACE: // white space
                    if (isLineBeyondLimit()) {
                        return FTokenContext.WHITESPACE;
                    }
                    if ((!Character.isWhitespace(actChar)) || (actChar == '\n') ) {
                        state = INIT;
                        return FTokenContext.WHITESPACE;
                    }
                    break;
                    
                case AFTER_B:
                    if (isLineBeyondLimit()) {
                        return FTokenContext.IDENTIFIER;
                    }
                    switch (actChar) {
                        case '"':
                        case '\'':
                            if (Character.isDigit(buffer[offset+1])) {
                                state = IN_BINARY;
                                break;
                            } //else continue to default
                        default:
                            state = IN_IDENTIFIER;
                            offset--;  //go back and evaluate the character
                            break;
                    }//switch AFTER_B
                    break;
                    
                case AFTER_O:
                    if (isLineBeyondLimit()) {
                        return FTokenContext.IDENTIFIER;
                    }
                    switch (actChar) {
                        case '"':
                        case '\'':
                            if (Character.isDigit(buffer[offset+1])) {
                                state = IN_OCTAL;
                                break;
                            } //else continue to default
                        default:
                            state = IN_IDENTIFIER;
                            offset--;  //go back and evaluate the character
                            break;
                    }//switch AFTER_O
                    break;
                    
                case AFTER_Z:
                    if (isLineBeyondLimit()) {
                        return FTokenContext.IDENTIFIER;
                    }
                    switch (actChar) {
                        case '"':
                        case '\'':
                            if (Character.isLetterOrDigit(buffer[offset+1])) {
                                state = IN_HEX;
                                break;
                            } //else continue to default
                        default:
                            state = IN_IDENTIFIER;
                            offset--;  //go back and evaluate the character
                            break;
                    }//switch AFTER_Z
                    break;
                    
                case IN_LINE_COMMENT:
                    switch (actChar) {
                        case '\n':
                            state = INIT;
                            lastNL = offset;
                            offset++;
                            return FTokenContext.LINE_COMMENT;
                    }//switch IN_LINE_COMMENT
                    break;
                    
                case IN_STRING:
                    if (isLineBeyondLimit()) {
                        return FTokenContext.ERR_INCOMPLETE_STRING_LITERAL;
                    }
                    switch (actChar) {
                        case '\\':
                            state = IN_STRING_AFTER_BSLASH;
                            break;
                        case '\n':
                            state = INIT;
                            lastNL = offset;
                            offset++;
                            supposedTokenID = FTokenContext.STRING_LITERAL;
                            //return FTokenContext.INCOMPLETE_STRING_LITERAL;
                            return supposedTokenID;
                        case '"':
                            if (STRING_IN_DOUBLE_QUOTE) {
                                offset++;
                                state = INIT;
                                return FTokenContext.STRING_LITERAL;
                            }
                            break;
                        case '\'':
                            if (!STRING_IN_DOUBLE_QUOTE) {
                                offset++;
                                state = INIT;
                                return FTokenContext.STRING_LITERAL;
                            }
                            break;
                    } //switch IN_STRING
                    break;
                    
                case IN_STRING_AFTER_BSLASH:
                    if (isLineBeyondLimit()) {
                        return FTokenContext.ERR_INCOMPLETE_STRING_LITERAL;
                    }
                    switch (actChar) {
                        case '"':
                        case '\'':
                        case '\\':
                            break;   //ignore the meaning of these characters
                        default:
                            offset--;  //go back and evaluate the character
                            break;
                    }//switch IN_STRING_AFTER_BSLASH:
                    state = IN_STRING;
                    break;
                    
                case AFTER_SLASH:
                    if (isLineBeyondLimit()) {
                        return FTokenContext.OP_DIV;
                    }
                    switch (actChar) {
                        case '/':
                            offset++;
                            state = INIT;
                            return FTokenContext.OP_CONCAT;
                        case '=':
                            offset++;
                            state = INIT;
                            return FTokenContext.OP_NOT_EQ;
                        default:
                            state = INIT;
                            return FTokenContext.OP_DIV;
                    }//switch AFTER_SLASH
                    //break;
                    
                case AFTER_EQ:
                    if (isLineBeyondLimit()) {
                        return FTokenContext.EQ;
                    }
                    switch (actChar) {
                        case '=':
                            offset++;
                            state = INIT;
                            return  FTokenContext.OP_LOG_EQ;
                        default:
                            state = INIT;
                            return FTokenContext.EQ;
                    }//switch AFTER_EQ
                    //break;
                    
                case AFTER_STAR:
                    if (isLineBeyondLimit()) {
                        return FTokenContext.OP_MUL;
                    }
                    switch (actChar) {
                        case '*':
                            offset++;
                            state = INIT;
                            return  FTokenContext.OP_POWER;
                        default:
                            state = INIT;
                            return FTokenContext.OP_MUL;
                    }//switch AFTER_STAR
                    //break;
                    
                case AFTER_LESSTHAN:
                    if (isLineBeyondLimit()) {
                        return FTokenContext.OP_LT;
                    }
                    switch (actChar) {
                        case '=':
                            offset++;
                            state = INIT;
                            return  FTokenContext.OP_LT_EQ;
                        default:
                            state = INIT;
                            return FTokenContext.OP_LT;
                    }//switch AFTER_LESSTHAN
                    //break;
                    
                case AFTER_GREATERTHAN:
                    if (isLineBeyondLimit()) {
                        return FTokenContext.OP_GT;
                    }
                    switch (actChar) {
                        case '=':
                            offset++;
                            state = INIT;
                            return  FTokenContext.OP_GT_EQ;
                        default:
                            state = INIT;
                            return FTokenContext.OP_GT;
                    }//switch AFTER_GREATERTHAN
                    //break;
                    
                case IN_IDENTIFIER:
                    if (isLineBeyondLimit()) {
                        TokenID tid = matchKeyword(buffer, tokenOffset, offset - tokenOffset);
                        return (tid != null) ? tid : FTokenContext.IDENTIFIER;
                    }
                    // To find out why we're using isJAVAidentifier
                    // here, grep for isJavaIdentifierStart in
                    // CCSyntax.java
                    
                    //check if it is a keyword ending with a "="
                    if (actChar == '=') {
                        //+1 is for "=" sign
                        TokenID tid = matchKeyword(buffer, tokenOffset, offset - tokenOffset +1);
                        if (tid != null) {
                            offset++;
                            state = INIT;
                            return tid;
                        }
                    }
                    
                    if (! (Character.isJavaIdentifierPart(actChar)) ) {
                        state = INIT;
                        TokenID tid = matchKeyword(buffer, tokenOffset, offset - tokenOffset);
                        return (tid != null) ? tid : FTokenContext.IDENTIFIER;
                    }
                    break;
                    
                case IN_BINARY:
                    if (isLineBeyondLimit()) {
                        return FTokenContext.ERR_INVALID_BINARY_LITERAL;
                    }
                    if ((actChar == '\'' || actChar == '"')) {
                        state = INIT;
                        offset++;
                        return FTokenContext.NUM_LITERAL_BINARY;
                    }
                    else if (((Character.isDigit(actChar)) && (actChar > '1')) ||
                    !(Character.isDigit(actChar)))   {
                        state = INIT;
                        return FTokenContext.ERR_INVALID_BINARY_LITERAL;
                    }
                    break;
                    
                case IN_OCTAL:
                    if (isLineBeyondLimit()) {
                        return FTokenContext.ERR_INVALID_OCTAL_LITERAL;
                    }
                    if ((actChar == '\'' || actChar == '"')) {
                        state = INIT;
                        offset++;
                        return FTokenContext.NUM_LITERAL_OCTAL;
                    }
                    else if (((Character.isDigit(actChar)) && (actChar > '7')) ||
                    !(Character.isDigit(actChar)))   {
                        state = INIT;
                        return FTokenContext.ERR_INVALID_OCTAL_LITERAL;
                    }
                    break;
                    
                case IN_HEX:
                    if (isLineBeyondLimit()) {
                        return FTokenContext.ERR_INVALID_HEX_LITERAL;
                    }
                    if ((actChar == '\'' || actChar == '"')) {
                        state = INIT;
                        offset++;
                        return FTokenContext.NUM_LITERAL_HEX;
                    }
                    else if ( !(Character.isDigit(actChar)) &&
                    ( (Character.toLowerCase(actChar) < 'a') ||
                    (Character.toLowerCase(actChar) > 'f') )
                    ) {
                        state = INIT;
                        return FTokenContext.ERR_INVALID_HEX_LITERAL;
                    }
                    break;
                    
                case IN_INT:
                    if (isLineBeyondLimit()) {
                        return FTokenContext.NUM_LITERAL_INT;
                    }
                    switch (actChar) {
                        case '_':
                            offset++;
                            HAS_NUMERIC_UNDERSCORE = true;
                            break;
                        case '.':
                            if (HAS_NUMERIC_UNDERSCORE) {
                                offset++;
                                state = INIT;
                                HAS_NUMERIC_UNDERSCORE = false;
                                return FTokenContext.ERR_INVALID_INTEGER;
                            }
                            else {
                                state = IN_REAL;
                                break;
                            }
                        case 'd':
                        case 'D':
                        case 'e':
                        case 'E':
                            if (!HAS_NUMERIC_UNDERSCORE)
                                state = IN_REAL;
                            break;
                        default:
                            if ( ((HAS_NUMERIC_UNDERSCORE)&& (!(Character.isLetterOrDigit(actChar)))) ||
                            ((!HAS_NUMERIC_UNDERSCORE)&& (!(Character.isDigit(actChar)))) ){
                                state = INIT;
                                HAS_NUMERIC_UNDERSCORE = false;
                                return FTokenContext.NUM_LITERAL_INT;
                            }
                    }//switch
                    break;
                    
                case IN_REAL:
                    if (isLineBeyondLimit()) {
                        return FTokenContext.NUM_LITERAL_REAL;
                    }
                    switch (actChar) {
                        case '_':
                            offset++;
                            HAS_NUMERIC_UNDERSCORE = true;
                            break;
                        case 'd':
                        case 'D':
                        case 'e':
                        case 'E':
                            if (!HAS_NUMERIC_UNDERSCORE)
                                break;
                        default:
                            if ( ((HAS_NUMERIC_UNDERSCORE)&& (!(Character.isLetterOrDigit(actChar)))) ||
                            ((!HAS_NUMERIC_UNDERSCORE)&& (!(Character.isDigit(actChar)))) ){
                                state = INIT;
                                HAS_NUMERIC_UNDERSCORE = false;
                                return FTokenContext.NUM_LITERAL_REAL;
                            }
                    }//switch
                    break;
                    
                case AFTER_DOT:
                    if (isLineBeyondLimit()) {
                        return FTokenContext.DOT;
                    }
                    // To find out why we're using isJAVAidentifier
                    // here, grep for isJavaIdentifierStart in
                    // CCSyntax.java
                    if (Character.isDigit(actChar))
                        state = IN_REAL;
                    else if (Character.isJavaIdentifierPart(actChar))
                        // Keyword, like .gt., .le., etc.
                        state = IN_DOT_IDENTIFIER;
                    else{
                        state = INIT;
                        return FTokenContext.DOT;
                    }
                    break;
                    
                case IN_DOT_IDENTIFIER:
                    if (isLineBeyondLimit()) {
                        //highlight the first dot and reevaluate the rest of the string since dot
                        offset = tokenOffset + 1;
                        state = INIT;
                        return FTokenContext.DOT;
                    }
                    if (!Character.isJavaIdentifierPart(actChar)) {
                        //if char is "." we have to move the offset to the next char
                        //before evaluating the string
                        offset += (actChar == '.') ? 1 : 0;
                        state = INIT;
                        TokenID tid = matchKeyword(buffer, tokenOffset, offset - tokenOffset);
                        if (tid != null)
                            return tid;
                        else {
                            //highlight the first dot and reevaluate the rest of the string since dot
                            offset = tokenOffset + 1;
                            state = INIT;
                            return FTokenContext.DOT;
                        }
                    }
                    break;
                    
            } // end of switch(state)
            //END STATE SWITCH
            offset++;
        } //while(offset...)
        //END WHILE OFFSET
        
        /** At this stage there's no more text in the scanned buffer.
         * Scanner first checks whether this is completely the last
         * available buffer.
         */
        if (lastBuffer) {
            switch(state) {
                case IN_WHITESPACE:
                    state = INIT;
                    return FTokenContext.WHITESPACE;
                case AFTER_B:
                case AFTER_O:
                case AFTER_Z:
                    state = INIT;
                    return FTokenContext.IDENTIFIER;
                case IN_BINARY:
                    state = INIT;
                    return FTokenContext.ERR_INVALID_BINARY_LITERAL;
                case IN_OCTAL:
                    state = INIT;
                    return FTokenContext.ERR_INVALID_OCTAL_LITERAL;
                case IN_HEX:
                    state = INIT;
                    return FTokenContext.ERR_INVALID_HEX_LITERAL;
                case IN_DOT_IDENTIFIER:
                case IN_IDENTIFIER:
                    state = INIT;
                    TokenID kwd = matchKeyword(buffer, tokenOffset, offset - tokenOffset);
                    return (kwd != null) ? kwd : FTokenContext.IDENTIFIER;
                case IN_STRING:
                case IN_STRING_AFTER_BSLASH:
                    return FTokenContext.STRING_LITERAL; // hold the state
                case AFTER_SLASH:
                    state = INIT;
                    return FTokenContext.OP_DIV;
                case AFTER_EQ:
                    state = INIT;
                    return FTokenContext.EQ;
                case AFTER_STAR:
                    state = INIT;
                    return FTokenContext.OP_MUL;
                case IN_LINE_COMMENT:
                    return FTokenContext.LINE_COMMENT; //stay in line-comment state
                case AFTER_LESSTHAN:
                    state = INIT;
                    return FTokenContext.OP_LT;
                case AFTER_GREATERTHAN:
                    state = INIT;
                    return FTokenContext.OP_GT;
                case IN_INT:
                    state = INIT;
                    return FTokenContext.NUM_LITERAL_INT;
                case IN_REAL:
                    state = INIT;
                    return FTokenContext.NUM_LITERAL_REAL;
                case AFTER_DOT:
                    state = INIT;
                    return FTokenContext.DOT;
                    
            } //switch
        }//if (lastbuffer)
        
        /* At this stage there's no more text in the scanned buffer, but
         * this buffer is not the last so the scan will continue on another
         * buffer. The scanner tries to minimize the amount of characters
         * that will be prescanned in the next buffer by returning the token
         * where possible.
         */
        switch (state) {
            case IN_WHITESPACE:
                return FTokenContext.WHITESPACE;
        }
        
        return null; // nothing found
    }
    
    @Override
    public String getStateName(int stateNumber) {
        switch(stateNumber) {
            case IN_WHITESPACE:
                return "IN_WHITESPACE"; //NOI18N
            case IN_LINE_COMMENT:
                return "IN_LINE_COMMENT"; //NOI18N
            case IN_STRING:
                return "IN_STRING"; //NOI18N
            case IN_STRING_AFTER_BSLASH:
                return "IN_STRING_AFTER_BSLASH"; //NOI18N
            case IN_IDENTIFIER:
                return "IN_IDENTIFIER"; //NOI18N
            case IN_DOT_IDENTIFIER:
                return "IN_DOT_IDENTIFIER"; //NOI18N
            case IN_BINARY:
                return "IN_BINARY"; //NOI18N
            case IN_OCTAL:
                return "IN_OCTAL"; //NOI18N
            case IN_HEX:
                return "IN_HEX"; //NOI18N
            case IN_INT:
                return "IN_INT"; //NOI18N
            case IN_REAL:
                return "IN_REAL"; //NOI18N
            case AFTER_B:
                return "AFTER_B"; //NOI18N
            case AFTER_O:
                return "AFTER_O"; //NOI18N
            case AFTER_Z:
                return "AFTER_Z"; //NOI18N
            case AFTER_SLASH:
                return "AFTER_SLASH"; //NOI18N
            case AFTER_EQ:
                return "AFTER_EQ"; //NOI18N
            case AFTER_STAR:
                return "AFTER_STAR"; //NOI18N
            case AFTER_LESSTHAN:
                return "AFTER_LESSTHAN"; //NOI18N
            case AFTER_GREATERTHAN:
                return "AFTER_GREATERTHAN"; //NOI18N
            case AFTER_DOT:
                return "AFTER_DOT"; //NOI18N
            default:
                return super.getStateName(stateNumber);
        }
    }
    
    public static TokenID matchKeyword(char[] buffer, int offset, int len) {
        if (len > 17)
            return null;
        if (len <= 1)
            return null;
        //BEGIN MOTHER SWITCH
        switch (Character.toLowerCase(buffer[offset++])) {
            //DOT
            // .and. .eq. .eqv. .false. .ge. .gt. .le. .lt. .ne. .neqv. .not. .or. .true.
            case '.':
                if ((len < 4) || (len > 7))
                    return null;
                switch (Character.toLowerCase(buffer[offset++])) {
                    case 'a': // .and.
                        return (len == 5
                        && Character.toLowerCase(buffer[offset++]) == 'n'
                        && Character.toLowerCase(buffer[offset++]) == 'd'
                        && Character.toLowerCase(buffer[offset++]) == '.')
                        ? FTokenContext.KWOP_AND : null;
                    case 'e': // .eq. .eqv.
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'q': // else, elseif, elsewhere
                                if (len == 4
                                && Character.toLowerCase(buffer[offset++]) == '.')
                                    return FTokenContext.KWOP_EQ;
                                else if (len == 5
                                && Character.toLowerCase(buffer[offset++]) == 'v'
                                && Character.toLowerCase(buffer[offset++]) == '.')
                                    return FTokenContext.KWOP_EQV;
                                else
                                    return null;
                                
                            default:
                                return null;
                        }
                        
                    case 'f': //false
                        return (len == 7
                        && Character.toLowerCase(buffer[offset++]) == 'a'
                        && Character.toLowerCase(buffer[offset++]) == 'l'
                        && Character.toLowerCase(buffer[offset++]) == 's'
                        && Character.toLowerCase(buffer[offset++]) == 'e'
                        && Character.toLowerCase(buffer[offset++]) == '.')
                        ? FTokenContext.KWOP_FALSE : null;
                        
                    case 'g': // ge, gt
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'e': // ge
                                return (len == 4
                                && Character.toLowerCase(buffer[offset++]) == '.')
                                ? FTokenContext.KWOP_GE : null;
                            case 't': // gt
                                return (len == 4
                                && Character.toLowerCase(buffer[offset++]) == '.')
                                ? FTokenContext.KWOP_GT : null;
                            default:
                                return null;
                        }
                        
                    case 'l': // le, lt
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'e': // le
                                return (len == 4
                                && Character.toLowerCase(buffer[offset++]) == '.')
                                ? FTokenContext.KWOP_LE : null;
                            case 't': // lt
                                return (len == 4
                                && Character.toLowerCase(buffer[offset++]) == '.')
                                ? FTokenContext.KWOP_LT : null;
                            default:
                                return null;
                        }
                        
                    case 'n': // ne, neqv, not
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'e': // ne
                                if (len == 4
                                && Character.toLowerCase(buffer[offset++]) == '.')
                                    return FTokenContext.KWOP_NE;
                                else if (len == 6
                                && Character.toLowerCase(buffer[offset++]) == 'q'
                                && Character.toLowerCase(buffer[offset++]) == 'v'
                                && Character.toLowerCase(buffer[offset++]) == '.')
                                    return FTokenContext.KWOP_NEQV;
                                else
                                    return null;
                            case 'o': // not
                                return (len == 5
                                && Character.toLowerCase(buffer[offset++]) == 't'
                                && Character.toLowerCase(buffer[offset++]) == '.')
                                ? FTokenContext.KWOP_NOT : null;
                            default:
                                return null;
                        }
                        
                    case 'o': // .or.
                        return (len == 4
                        && Character.toLowerCase(buffer[offset++]) == 'r'
                        && Character.toLowerCase(buffer[offset++]) == '.')
                        ? FTokenContext.KWOP_OR : null;
                        
                    case 't': // .true.
                        return (len == 6
                        && Character.toLowerCase(buffer[offset++]) == 'r'
                        && Character.toLowerCase(buffer[offset++]) == 'u'
                        && Character.toLowerCase(buffer[offset++]) == 'e'
                        && Character.toLowerCase(buffer[offset++]) == '.')
                        ? FTokenContext.KWOP_OR : null;
                    default:
                        return null;
                }//switch dot
                //END DOT
                
                //A
                //access= action= advance= allocatable allocate apostrophe assignment
            case 'a':
                if ((len < 7) || (len > 11))
                    return null;
                
                switch (Character.toLowerCase(buffer[offset++])) {
                    case 'c': //access= action=
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'c': // access=
                                return (len == 7
                                && Character.toLowerCase(buffer[offset++]) == 'e'
                                && Character.toLowerCase(buffer[offset++]) == 's'
                                && Character.toLowerCase(buffer[offset++]) == 's'
                                && Character.toLowerCase(buffer[offset++]) == '=')
                                ? FTokenContext.KW_ACCESS_EQ : null;
                            case 't' : //action=
                                return (len == 7
                                && Character.toLowerCase(buffer[offset++]) == 'i'
                                && Character.toLowerCase(buffer[offset++]) == 'o'
                                && Character.toLowerCase(buffer[offset++]) == 'n'
                                && Character.toLowerCase(buffer[offset++]) == '=')
                                ? FTokenContext.KW_ACTION_EQ : null;
                            default:
                                return null;
                        }//switch ac
                        
                        
                    case 'd': // advance=
                        return (len == 8
                        && Character.toLowerCase(buffer[offset++]) == 'v'
                        && Character.toLowerCase(buffer[offset++]) == 'a'
                        && Character.toLowerCase(buffer[offset++]) == 'n'
                        && Character.toLowerCase(buffer[offset++]) == 'c'
                        && Character.toLowerCase(buffer[offset++]) == 'e'
                        && Character.toLowerCase(buffer[offset++]) == '=')
                        ? FTokenContext.KW_ADVANCE_EQ : null;
                        
                    case 'l': // allocatable allocate
                        if (len >= 8
                        && Character.toLowerCase(buffer[offset++]) == 'l'
                        && Character.toLowerCase(buffer[offset++]) == 'o'
                        && Character.toLowerCase(buffer[offset++]) == 'c'
                        && Character.toLowerCase(buffer[offset++]) == 'a'
                        && Character.toLowerCase(buffer[offset++]) == 't') {
                            
                            if (len == 11
                            && Character.toLowerCase(buffer[offset++]) == 'a'
                            && Character.toLowerCase(buffer[offset++]) == 'b'
                            && Character.toLowerCase(buffer[offset++]) == 'l'
                            && Character.toLowerCase(buffer[offset++]) == 'e')
                                return FTokenContext.KW_ALLOCATABLE;
                            else if (len == 8
                            && Character.toLowerCase(buffer[offset++]) == 'e')
                                return FTokenContext.KW_ALLOCATE;
                            else
                                return null;
                        }
                        else {
                            return null;
                        }
                        
                    case 'p': // apostrophe
                        return (len == 10
                        && Character.toLowerCase(buffer[offset++]) == 'o'
                        && Character.toLowerCase(buffer[offset++]) == 's'
                        && Character.toLowerCase(buffer[offset++]) == 't'
                        && Character.toLowerCase(buffer[offset++]) == 'r'
                        && Character.toLowerCase(buffer[offset++]) == 'o'
                        && Character.toLowerCase(buffer[offset++]) == 'p'
                        && Character.toLowerCase(buffer[offset++]) == 'h'
                        && Character.toLowerCase(buffer[offset++]) == 'e')
                        ? FTokenContext.KW_APOSTROPHE : null;
                        
                    case 's': // assignment
                        return (len == 10
                        && Character.toLowerCase(buffer[offset++]) == 's'
                        && Character.toLowerCase(buffer[offset++]) == 'i'
                        && Character.toLowerCase(buffer[offset++]) == 'g'
                        && Character.toLowerCase(buffer[offset++]) == 'n'
                        && Character.toLowerCase(buffer[offset++]) == 'm'
                        && Character.toLowerCase(buffer[offset++]) == 'e'
                        && Character.toLowerCase(buffer[offset++]) == 'n'
                        && Character.toLowerCase(buffer[offset++]) == 't')
                        ? FTokenContext.KW_ASSIGNMENT : null;
                        
                    default:
                        return null;
                }//switch a
                //END A
                
                //B
                //backspace blank= block blockdata
            case 'b':
                if ((len < 5) || (len > 9))
                    return null;
                
                switch (Character.toLowerCase(buffer[offset++])) {
                    case 'a': // backspace
                        return (len == 9
                        && Character.toLowerCase(buffer[offset++]) == 'c'
                        && Character.toLowerCase(buffer[offset++]) == 'k'
                        && Character.toLowerCase(buffer[offset++]) == 's'
                        && Character.toLowerCase(buffer[offset++]) == 'p'
                        && Character.toLowerCase(buffer[offset++]) == 'a'
                        && Character.toLowerCase(buffer[offset++]) == 'c'
                        && Character.toLowerCase(buffer[offset++]) == 'e')
                        ? FTokenContext.KW_BACKSPACE : null;
                        
                    case 'l': // blank= block blockdata
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'a': //blank=
                                return (len == 6
                                && Character.toLowerCase(buffer[offset++]) == 'n'
                                && Character.toLowerCase(buffer[offset++]) == 'k'
                                && Character.toLowerCase(buffer[offset++]) == '=')
                                ? FTokenContext.KW_BLANK_EQ : null;
                                
                            case 'o': //block blockdata
                                if (len >= 5
                                && Character.toLowerCase(buffer[offset++]) == 'c'
                                && Character.toLowerCase(buffer[offset++]) == 'k') {
                                    
                                    if (len == 5)
                                        return FTokenContext.KW_BLOCK;
                                    else if (len == 9
                                    && Character.toLowerCase(buffer[offset++]) == 'd'
                                    && Character.toLowerCase(buffer[offset++]) == 'a'
                                    && Character.toLowerCase(buffer[offset++]) == 't'
                                    && Character.toLowerCase(buffer[offset++]) == 'a')
                                        return FTokenContext.KW_BLOCKDATA;
                                    else
                                        return null;
                                }
                                
                                else {
                                    return null;
                                }
                                
                            default:
                                return null;
                        }//switch l
                        
                    default:
                        return null;
                }//switch b
                //END B
                
                //C
                //case character close common complex contains continue cycle
            case 'c':
                if ((len < 4) || (len > 9))
                    return null;
                
                switch (Character.toLowerCase(buffer[offset++])) {
                    case 'a': //  case or call
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'l': // call
                                return (len == 4
                                && Character.toLowerCase(buffer[offset++]) == 'l')
                                ? FTokenContext.KW_CALL : null;
                                
                            case 's': // case
                                return (len == 4
                                && Character.toLowerCase(buffer[offset++]) == 'e')
                                ? FTokenContext.KW_CASE : null;
                                
                            default:
                                return null;
                        }
                    case 'h': // character
                        return (len == 9
                        && Character.toLowerCase(buffer[offset++]) == 'a'
                        && Character.toLowerCase(buffer[offset++]) == 'r'
                        && Character.toLowerCase(buffer[offset++]) == 'a'
                        && Character.toLowerCase(buffer[offset++]) == 'c'
                        && Character.toLowerCase(buffer[offset++]) == 't'
                        && Character.toLowerCase(buffer[offset++]) == 'e'
                        && Character.toLowerCase(buffer[offset++]) == 'r')
                        ? FTokenContext.KW_CHARACTER : null;
                        
                    case 'l': // close
                        return (len == 5
                        && Character.toLowerCase(buffer[offset++]) == 'o'
                        && Character.toLowerCase(buffer[offset++]) == 's'
                        && Character.toLowerCase(buffer[offset++]) == 'e')
                        ? FTokenContext.KW_CLOSE : null;
                        
                    case 'o': // common, complex, contains, continue
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'm': // common, complex
                                switch (Character.toLowerCase(buffer[offset++])) {
                                    case 'm': // common
                                        return (len == 6
                                        && Character.toLowerCase(buffer[offset++]) == 'o'
                                        && Character.toLowerCase(buffer[offset++]) == 'n')
                                        ? FTokenContext.KW_COMMON : null;
                                    case 'p': // complex
                                        return (len == 7
                                        && Character.toLowerCase(buffer[offset++]) == 'l'
                                        && Character.toLowerCase(buffer[offset++]) == 'e'
                                        && Character.toLowerCase(buffer[offset++]) == 'x')
                                        ? FTokenContext.KW_COMPLEX : null;
                                    default:
                                        return null;
                                } //switch com
                                
                            case 'n': // contains continue
                                if (len == 8
                                && Character.toLowerCase(buffer[offset++]) == 't'){
                                    switch (Character.toLowerCase(buffer[offset++])) {
                                        case 'a': // contains
                                            return (len == 8
                                            && Character.toLowerCase(buffer[offset++]) == 'i'
                                            && Character.toLowerCase(buffer[offset++]) == 'n'
                                            && Character.toLowerCase(buffer[offset++]) == 's')
                                            ? FTokenContext.KW_CONTAINS : null;
                                        case 'i' : //continue
                                            return (len == 8
                                            && Character.toLowerCase(buffer[offset++]) == 'n'
                                            && Character.toLowerCase(buffer[offset++]) == 'u'
                                            && Character.toLowerCase(buffer[offset++]) == 'e')
                                            ? FTokenContext.KW_CONTINUE : null;
                                        default:
                                            return null;
                                    }//switch cont
                                }//if
                                else
                                    return null;
                                
                            default:
                                return null;
                        } //switch co
                        
                    case 'y': // cycle
                        return (len == 5
                        && Character.toLowerCase(buffer[offset++]) == 'c'
                        && Character.toLowerCase(buffer[offset++]) == 'l'
                        && Character.toLowerCase(buffer[offset++]) == 'e')
                        ? FTokenContext.KW_CYCLE : null;
                        
                    default:
                        return null;
                } //switch c
                //END C
                
                
                //D
                //data deallocate default delim= dimension direct= do double doubleprecision
            case 'd':
                if ((len < 2) || (len > 15))
                    return null;
                
                switch (Character.toLowerCase(buffer[offset++])) {
                    case 'a': // data
                        return (len == 4
                        && Character.toLowerCase(buffer[offset++]) == 't'
                        && Character.toLowerCase(buffer[offset++]) == 'a')
                        ? FTokenContext.KW_DATA : null;
                        
                    case 'e': // deallocate default delim=
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'a': // deallocate
                                return (len == 10
                                && Character.toLowerCase(buffer[offset++]) == 'l'
                                && Character.toLowerCase(buffer[offset++]) == 'l'
                                && Character.toLowerCase(buffer[offset++]) == 'o'
                                && Character.toLowerCase(buffer[offset++]) == 'c'
                                && Character.toLowerCase(buffer[offset++]) == 'a'
                                && Character.toLowerCase(buffer[offset++]) == 't'
                                && Character.toLowerCase(buffer[offset++]) == 'e')
                                ? FTokenContext.KW_DEALLOCATE : null;
                            case 'f': // default
                                return (len == 7
                                && Character.toLowerCase(buffer[offset++]) == 'a'
                                && Character.toLowerCase(buffer[offset++]) == 'u'
                                && Character.toLowerCase(buffer[offset++]) == 'l'
                                && Character.toLowerCase(buffer[offset++]) == 't')
                                ? FTokenContext.KW_DEFAULT : null;
                            case 'l': // delim=
                                return (len == 6
                                && Character.toLowerCase(buffer[offset++]) == 'i'
                                && Character.toLowerCase(buffer[offset++]) == 'm'
                                && Character.toLowerCase(buffer[offset++]) == '=')
                                ? FTokenContext.KW_DELIM_EQ : null;
                            default:
                                return null;
                        } //switch de
                        
                    case 'i': // dimension direct=
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'm': // dimension
                                return (len == 9
                                && Character.toLowerCase(buffer[offset++]) == 'e'
                                && Character.toLowerCase(buffer[offset++]) == 'n'
                                && Character.toLowerCase(buffer[offset++]) == 's'
                                && Character.toLowerCase(buffer[offset++]) == 'i'
                                && Character.toLowerCase(buffer[offset++]) == 'o'
                                && Character.toLowerCase(buffer[offset++]) == 'n')
                                ? FTokenContext.KW_DIMENSION : null;
                            case 'r': // direct=
                                return (len == 7
                                && Character.toLowerCase(buffer[offset++]) == 'e'
                                && Character.toLowerCase(buffer[offset++]) == 'c'
                                && Character.toLowerCase(buffer[offset++]) == 't'
                                && Character.toLowerCase(buffer[offset++]) == '=')
                                ? FTokenContext.KW_DIRECT_EQ : null;
                            default:
                                return null;
                        } //switch di
                        
                    case 'o': // do, double, doubleprecision
                        if (len == 2)
                            return FTokenContext.KW_DO;
                        
                        else if (len >= 6
                        && Character.toLowerCase(buffer[offset++]) == 'u'
                        && Character.toLowerCase(buffer[offset++]) == 'b'
                        && Character.toLowerCase(buffer[offset++]) == 'l'
                        && Character.toLowerCase(buffer[offset++]) == 'e') {
                            if (len == 6)
                                return FTokenContext.KW_DOUBLE;
                            else if (len == 15
                            && Character.toLowerCase(buffer[offset++]) == 'p'
                            && Character.toLowerCase(buffer[offset++]) == 'r'
                            && Character.toLowerCase(buffer[offset++]) == 'e'
                            && Character.toLowerCase(buffer[offset++]) == 'c'
                            && Character.toLowerCase(buffer[offset++]) == 'i'
                            && Character.toLowerCase(buffer[offset++]) == 's'
                            && Character.toLowerCase(buffer[offset++]) == 'i'
                            && Character.toLowerCase(buffer[offset++]) == 'o'
                            && Character.toLowerCase(buffer[offset++]) == 'n')
                                return FTokenContext.KW_DOUBLEPRECISION;
                            else
                                return null;
                        }
                        else
                            return null;
                        
                    default:
                        return null;
                }//switch d
                //END D
                
                //E
                //elemental else elseif elsewhere end endblock endblockdata enddo end=
                //endfile endforall endfunction endif endinterface endmodule endprogram
                //endselect endsubroutine endtype endwhere entry eor= equivalance err=
                //exist= exit external
            case 'e':
                if ((len < 3) || (len > 13))
                    return null;
                
                switch (Character.toLowerCase(buffer[offset++])) {
                    case 'l': // elemental else elseif elsewhere
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'e'://elemental
                                return (len == 9
                                && Character.toLowerCase(buffer[offset++]) == 'm'
                                && Character.toLowerCase(buffer[offset++]) == 'e'
                                && Character.toLowerCase(buffer[offset++]) == 'n'
                                && Character.toLowerCase(buffer[offset++]) == 't'
                                && Character.toLowerCase(buffer[offset++]) == 'a'
                                && Character.toLowerCase(buffer[offset++]) == 'l')
                                ? FTokenContext.KW_ELEMENTAL : null;
                            case 's':  //else elseif elsewhere
                                if (len >= 4 && Character.toLowerCase(buffer[offset++]) == 'e') {
                                    if (len == 4)
                                        return FTokenContext.KW_ELSE;
                                    else if (len == 6
                                    && Character.toLowerCase(buffer[offset++]) == 'i'
                                    && Character.toLowerCase(buffer[offset++]) == 'f')
                                        return FTokenContext.KW_ELSEIF;
                                    else if (len == 9
                                    && Character.toLowerCase(buffer[offset++]) == 'w'
                                    && Character.toLowerCase(buffer[offset++]) == 'h'
                                    && Character.toLowerCase(buffer[offset++]) == 'e'
                                    && Character.toLowerCase(buffer[offset++]) == 'r'
                                    && Character.toLowerCase(buffer[offset++]) == 'e')
                                        return FTokenContext.KW_ELSEWHERE;
                                    else
                                        return null;
                                    
                                }
                                else
                                    return null;
                                
                            default:
                                return null;
                        } //switch el
                        
                    case 'n': // end end[*] entry
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'd': // end end[*]
                                //BEGIN "END"
                                if (len == 3)
                                    return FTokenContext.KW_END;
                                switch (Character.toLowerCase(buffer[offset++])) {
                                    case '=': // end=
                                        return (len == 4)
                                        ? FTokenContext.KW_END_EQ : null;
                                        
                                    case 'b': //endblock endblockdata
                                        if (len >= 8
                                        && Character.toLowerCase(buffer[offset++]) == 'l'
                                        && Character.toLowerCase(buffer[offset++]) == 'o'
                                        && Character.toLowerCase(buffer[offset++]) == 'c'
                                        && Character.toLowerCase(buffer[offset++]) == 'k') {
                                            
                                            if (len == 8)
                                                return FTokenContext.KW_ENDBLOCK;
                                            else if (len == 12
                                            && Character.toLowerCase(buffer[offset++]) == 'd'
                                            && Character.toLowerCase(buffer[offset++]) == 'a'
                                            && Character.toLowerCase(buffer[offset++]) == 't'
                                            && Character.toLowerCase(buffer[offset++]) == 'a')
                                                return FTokenContext.KW_ENDBLOCKDATA;
                                            else
                                                return null;
                                        }
                                        else
                                            return null;
                                        
                                    case 'd': // enddo
                                        return (len == 5
                                        && Character.toLowerCase(buffer[offset++]) == 'o')
                                        ? FTokenContext.KW_ENDDO : null;
                                        
                                    case 'f': //endfile endforall endfunction
                                        switch (Character.toLowerCase(buffer[offset++])) {
                                            case 'i'://endfile
                                                return (len == 7
                                                && Character.toLowerCase(buffer[offset++]) == 'l'
                                                && Character.toLowerCase(buffer[offset++]) == 'e')
                                                ? FTokenContext.KW_ENDFILE : null;
                                            case 'o':  //endforall
                                                return (len == 9
                                                && Character.toLowerCase(buffer[offset++]) == 'r'
                                                && Character.toLowerCase(buffer[offset++]) == 'a'
                                                && Character.toLowerCase(buffer[offset++]) == 'l'
                                                && Character.toLowerCase(buffer[offset++]) == 'l')
                                                ? FTokenContext.KW_ENDFORALL : null;
                                            case 'u':  //endfunction
                                                return (len == 11
                                                && Character.toLowerCase(buffer[offset++]) == 'n'
                                                && Character.toLowerCase(buffer[offset++]) == 'c'
                                                && Character.toLowerCase(buffer[offset++]) == 't'
                                                && Character.toLowerCase(buffer[offset++]) == 'i'
                                                && Character.toLowerCase(buffer[offset++]) == 'o'
                                                && Character.toLowerCase(buffer[offset++]) == 'n')
                                                ? FTokenContext.KW_ENDFUNCTION : null;
                                            default:
                                                return null;
                                        } //switch endf
                                        
                                    case 'i': // endif endinterface
                                        switch (Character.toLowerCase(buffer[offset++])) {
                                            case 'f'://endif
                                                return (len == 5)
                                                ? FTokenContext.KW_ENDIF : null;
                                            case 'n': //endinterface
                                                return (len == 12
                                                && Character.toLowerCase(buffer[offset++]) == 't'
                                                && Character.toLowerCase(buffer[offset++]) == 'e'
                                                && Character.toLowerCase(buffer[offset++]) == 'r'
                                                && Character.toLowerCase(buffer[offset++]) == 'f'
                                                && Character.toLowerCase(buffer[offset++]) == 'a'
                                                && Character.toLowerCase(buffer[offset++]) == 'c'
                                                && Character.toLowerCase(buffer[offset++]) == 'e')
                                                ? FTokenContext.KW_ENDINTERFACE : null;
                                            default:
                                                return null;
                                        } //switch endi
                                        
                                    case 'm':
                                        switch (Character.toLowerCase(buffer[offset++])) {
                                            case 'a': // endmap
                                                return (len == 6
                                                && Character.toLowerCase(buffer[offset++]) == 'p')
                                                ? FTokenContext.KW_ENDMAP : null;
                                                
                                            case 'o': // endmodule
                                                return (len == 9
                                                && Character.toLowerCase(buffer[offset++]) == 'd'
                                                && Character.toLowerCase(buffer[offset++]) == 'u'
                                                && Character.toLowerCase(buffer[offset++]) == 'l'
                                                && Character.toLowerCase(buffer[offset++]) == 'e')
                                                ? FTokenContext.KW_ENDMODULE : null;
                                            default:
                                                return null;
                                        }
                                        
                                    case 'p': // endprogram
                                        return (len == 10
                                        && Character.toLowerCase(buffer[offset++]) == 'r'
                                        && Character.toLowerCase(buffer[offset++]) == 'o'
                                        && Character.toLowerCase(buffer[offset++]) == 'g'
                                        && Character.toLowerCase(buffer[offset++]) == 'r'
                                        && Character.toLowerCase(buffer[offset++]) == 'a'
                                        && Character.toLowerCase(buffer[offset++]) == 'm')
                                        ? FTokenContext.KW_ENDPROGRAM : null;
                                        
                                    case 's': // endselect endsubroutine
                                        switch (Character.toLowerCase(buffer[offset++])) {
                                            case 'e'://endselect
                                                return (len == 9
                                                && Character.toLowerCase(buffer[offset++]) == 'l'
                                                && Character.toLowerCase(buffer[offset++]) == 'e'
                                                && Character.toLowerCase(buffer[offset++]) == 'c'
                                                && Character.toLowerCase(buffer[offset++]) == 't')
                                                ? FTokenContext.KW_ENDSELECT : null;
                                            case 't': //endstructure
                                                return (len == 12
                                                && Character.toLowerCase(buffer[offset++]) == 'r'
                                                && Character.toLowerCase(buffer[offset++]) == 'u'
                                                && Character.toLowerCase(buffer[offset++]) == 'c'
                                                && Character.toLowerCase(buffer[offset++]) == 't'
                                                && Character.toLowerCase(buffer[offset++]) == 'u'
                                                && Character.toLowerCase(buffer[offset++]) == 'r'
                                                && Character.toLowerCase(buffer[offset++]) == 'e')
                                                ? FTokenContext.KW_ENDSTRUCTURE : null;
                                            case 'u': //endsubroutine
                                                return (len == 13
                                                && Character.toLowerCase(buffer[offset++]) == 'b'
                                                && Character.toLowerCase(buffer[offset++]) == 'r'
                                                && Character.toLowerCase(buffer[offset++]) == 'o'
                                                && Character.toLowerCase(buffer[offset++]) == 'u'
                                                && Character.toLowerCase(buffer[offset++]) == 't'
                                                && Character.toLowerCase(buffer[offset++]) == 'i'
                                                && Character.toLowerCase(buffer[offset++]) == 'n'
                                                && Character.toLowerCase(buffer[offset++]) == 'e')
                                                ? FTokenContext.KW_ENDSUBROUTINE : null;
                                            default:
                                                return null;
                                        } //switch "ends"
                                        
                                    case 't': // endtype
                                        return (len == 7
                                        && Character.toLowerCase(buffer[offset++]) == 'y'
                                        && Character.toLowerCase(buffer[offset++]) == 'p'
                                        && Character.toLowerCase(buffer[offset++]) == 'e')
                                        ? FTokenContext.KW_ENDTYPE : null;
                                        
                                    case 'u': // endunion
                                        return (len == 8
                                        && Character.toLowerCase(buffer[offset++]) == 'n'
                                        && Character.toLowerCase(buffer[offset++]) == 'i'
                                        && Character.toLowerCase(buffer[offset++]) == 'o'
                                        && Character.toLowerCase(buffer[offset++]) == 'n')
                                        ? FTokenContext.KW_ENDUNION : null;
                                    case 'w': // endwhere
                                        return (len == 8
                                        && Character.toLowerCase(buffer[offset++]) == 'h'
                                        && Character.toLowerCase(buffer[offset++]) == 'e'
                                        && Character.toLowerCase(buffer[offset++]) == 'r'
                                        && Character.toLowerCase(buffer[offset++]) == 'e')
                                        ? FTokenContext.KW_ENDWHERE : null;
                                        
                                    default:
                                        return null;
                                        
                                } //switch "end"
                                //END "END"
                            case 't': // entry
                                return (len == 5
                                && Character.toLowerCase(buffer[offset++]) == 'r'
                                && Character.toLowerCase(buffer[offset++]) == 'y')
                                ? FTokenContext.KW_ENTRY : null;
                            default:
                                return null;
                        } //switch en
                        
                    case 'o': // eor=
                        return (len == 4
                        && Character.toLowerCase(buffer[offset++]) == 'r'
                        && Character.toLowerCase(buffer[offset++]) == '=')
                        ? FTokenContext.KW_EOR_EQ : null;
                        
                    case 'q': // equivalance
                        return (len == 11
                        && Character.toLowerCase(buffer[offset++]) == 'u'
                        && Character.toLowerCase(buffer[offset++]) == 'i'
                        && Character.toLowerCase(buffer[offset++]) == 'v'
                        && Character.toLowerCase(buffer[offset++]) == 'a'
                        && Character.toLowerCase(buffer[offset++]) == 'l'
                        && Character.toLowerCase(buffer[offset++]) == 'a'
                        && Character.toLowerCase(buffer[offset++]) == 'n'
                        && Character.toLowerCase(buffer[offset++]) == 'c'
                        && Character.toLowerCase(buffer[offset++]) == 'e')
                        ? FTokenContext.KW_EQUIVALENCE : null;
                        
                    case 'r': // err=
                        return (len == 4
                        && Character.toLowerCase(buffer[offset++]) == 'r'
                        && Character.toLowerCase(buffer[offset++]) == '=')
                        ? FTokenContext.KW_ERR_EQ : null;
                        
                    case 'x': // exists=, exit, external
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'i': // exists=, exit
                                switch (Character.toLowerCase(buffer[offset++])) {
                                    case 's': //exist=
                                        return (len == 6
                                        && Character.toLowerCase(buffer[offset++]) == 't'
                                        && Character.toLowerCase(buffer[offset++]) == '=')
                                        ? FTokenContext.KW_EXIST_EQ : null;
                                    case 't': //exit
                                        return (len == 4)
                                        ? FTokenContext.KW_EXIT : null;
                                    default:
                                        return null;
                                } //switch exi
                                
                            case 't': // external
                                return (len == 8
                                && Character.toLowerCase(buffer[offset++]) == 'e'
                                && Character.toLowerCase(buffer[offset++]) == 'r'
                                && Character.toLowerCase(buffer[offset++]) == 'n'
                                && Character.toLowerCase(buffer[offset++]) == 'a'
                                && Character.toLowerCase(buffer[offset++]) == 'l')
                                ? FTokenContext.KW_EXTERNAL : null;
                            default:
                                return null;
                        }//switch ex
                    default:
                        return null;
                }//switch e
                //END E
                
                //F
                //file file= forall form= format formatted function
            case 'f': // format function
                if ((len < 4) || (len > 9))
                    return null;
                
                switch (Character.toLowerCase(buffer[offset++])) {
                    case 'i': // file file=
                        if (len >= 4
                        && Character.toLowerCase(buffer[offset++]) == 'l'
                        && Character.toLowerCase(buffer[offset++]) == 'e') {
                            
                            if (len == 4)
                                return FTokenContext.KW_FILE;
                            else if (len == 5
                            && Character.toLowerCase(buffer[offset++]) == '=')
                                return FTokenContext.KW_FILE_EQ;
                            else
                                return null;
                        }
                        else
                            return null;
                        
                    case 'o': // forall form= format formatted
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'r':
                                switch (Character.toLowerCase(buffer[offset++])) {
                                    case 'a': //forall
                                        return (len == 6
                                        && Character.toLowerCase(buffer[offset++]) == 'l'
                                        && Character.toLowerCase(buffer[offset++]) == 'l')
                                        ? FTokenContext.KW_FORALL : null;
                                    case 'm': //form= format formatted
                                        switch (Character.toLowerCase(buffer[offset++])) {
                                            case '=':   //form=
                                                return (len == 5)
                                                ? FTokenContext.KW_FORM_EQ : null;
                                            case 'a': //format formatted
                                                if (len >= 6
                                                && Character.toLowerCase(buffer[offset++]) == 't') {
                                                    if (len == 6)
                                                        return FTokenContext.KW_FORMAT;
                                                    else if (len == 9
                                                    && Character.toLowerCase(buffer[offset++]) == 't'
                                                    && Character.toLowerCase(buffer[offset++]) == 'e'
                                                    && Character.toLowerCase(buffer[offset++]) == 'd')
                                                        return FTokenContext.KW_FORMATTED;
                                                    else
                                                        return null;
                                                }
                                                else
                                                    return null;
                                            default:
                                                return null;
                                        } //switch form
                                        
                                    default:
                                        return null;
                                } //switch for
                            default:
                                return null;
                        }//switch fo
                        
                    case 'u': // function
                        return (len == 8
                        && Character.toLowerCase(buffer[offset++]) == 'n'
                        && Character.toLowerCase(buffer[offset++]) == 'c'
                        && Character.toLowerCase(buffer[offset++]) == 't'
                        && Character.toLowerCase(buffer[offset++]) == 'i'
                        && Character.toLowerCase(buffer[offset++]) == 'o'
                        && Character.toLowerCase(buffer[offset++]) == 'n')
                        ? FTokenContext.KW_FUNCTION : null;
                        
                    default:
                        return null;
                }//switch  f
                //END F
                
                //G
                //go goto
            case 'g':
                if ((len < 2) || (len > 4))
                    return null;
                
                if (len >= 2
                && Character.toLowerCase(buffer[offset++]) == 'o') {
                    if (len == 2)
                        return FTokenContext.KW_GO;
                    else if (len == 4
                    && Character.toLowerCase(buffer[offset++]) == 't'
                    && Character.toLowerCase(buffer[offset++]) == 'o')
                        return FTokenContext.KW_GOTO ;
                    else
                        return null;
                }
                else
                    return null;
                //END G
                
                //I
                //if implicit in include inout inquire integer intent interface intrinsic
                //iostat=
                
            case 'i':
                if ((len < 2) || (len > 9))
                    return null;
                
                switch (Character.toLowerCase(buffer[offset++])) {
                    case 'f': // if
                        return (len == 2)
                        ? FTokenContext.KW_IF : null;
                        
                    case 'm': // implicit
                        return (len == 8
                        && Character.toLowerCase(buffer[offset++]) == 'p'
                        && Character.toLowerCase(buffer[offset++]) == 'l'
                        && Character.toLowerCase(buffer[offset++]) == 'i'
                        && Character.toLowerCase(buffer[offset++]) == 'c'
                        && Character.toLowerCase(buffer[offset++]) == 'i'
                        && Character.toLowerCase(buffer[offset++]) == 't')
                        ? FTokenContext.KW_IMPLICIT : null;
                        
                    case 'n': // in include inout inquire integer intent interface intrinsic
                        if (len == 2)
                            return FTokenContext.KW_IN;
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'c' :  //include
                                return (len == 7
                                && Character.toLowerCase(buffer[offset++]) == 'l'
                                && Character.toLowerCase(buffer[offset++]) == 'u'
                                && Character.toLowerCase(buffer[offset++]) == 'd'
                                && Character.toLowerCase(buffer[offset++]) == 'e')
                                ? FTokenContext.KW_INCLUDE : null;
                            case 'o' :  //inout
                                return (len == 5
                                && Character.toLowerCase(buffer[offset++]) == 'u'
                                && Character.toLowerCase(buffer[offset++]) == 't')
                                ? FTokenContext.KW_INOUT : null;
                            case 'q' : //inquire
                                return (len == 7
                                && Character.toLowerCase(buffer[offset++]) == 'u'
                                && Character.toLowerCase(buffer[offset++]) == 'i'
                                && Character.toLowerCase(buffer[offset++]) == 'r'
                                && Character.toLowerCase(buffer[offset++]) == 'e')
                                ? FTokenContext.KW_INQUIRE : null;
                            case 't' : // integer intent interface intrinsic
                                switch (Character.toLowerCase(buffer[offset++])) {
                                    case 'e' :  //integer intent interface
                                        switch (Character.toLowerCase(buffer[offset++])) {
                                            case 'g' : //integer
                                                return (len == 7
                                                && Character.toLowerCase(buffer[offset++]) == 'e'
                                                && Character.toLowerCase(buffer[offset++]) == 'r')
                                                ? FTokenContext.KW_INTEGER : null;
                                            case 'n' : //intent
                                                return (len == 6
                                                && Character.toLowerCase(buffer[offset++]) == 't')
                                                ? FTokenContext.KW_INTENT : null;
                                            case 'r' : //interface
                                                return (len == 9
                                                && Character.toLowerCase(buffer[offset++]) == 'f'
                                                && Character.toLowerCase(buffer[offset++]) == 'a'
                                                && Character.toLowerCase(buffer[offset++]) == 'c'
                                                && Character.toLowerCase(buffer[offset++]) == 'e')
                                                ? FTokenContext.KW_INTERFACE : null;
                                            default:
                                                return null;
                                        } //switch inte
                                    case 'r' : // intrinsic
                                        return (len == 9
                                        && Character.toLowerCase(buffer[offset++]) == 'i'
                                        && Character.toLowerCase(buffer[offset++]) == 'n'
                                        && Character.toLowerCase(buffer[offset++]) == 's'
                                        && Character.toLowerCase(buffer[offset++]) == 'i'
                                        && Character.toLowerCase(buffer[offset++]) == 'c')
                                        ? FTokenContext.KW_INTRINSIC : null;
                                    default :
                                        return null;
                                } //switch int
                            default:
                                return null;
                        } //switch in
                        
                    case 'o' : //iostat=
                        return (len == 7
                        && Character.toLowerCase(buffer[offset++]) == 's'
                        && Character.toLowerCase(buffer[offset++]) == 't'
                        && Character.toLowerCase(buffer[offset++]) == 'a'
                        && Character.toLowerCase(buffer[offset++]) == 't'
                        && Character.toLowerCase(buffer[offset++]) == '=')
                        ? FTokenContext.KW_IOSTAT_EQ : null;
                        
                    default:
                        return null;
                } //switch i
                //END I
                
                //K
                //kind
            case 'k':
                return (len == 4
                && Character.toLowerCase(buffer[offset++]) == 'i'
                && Character.toLowerCase(buffer[offset++]) == 'n'
                && Character.toLowerCase(buffer[offset++]) == 'd')
                ? FTokenContext.KW_KIND : null;
                //END K
                
                //L
                //len logical
            case 'l':
                if ((len < 3) || (len > 7))
                    return null;
                
                switch (Character.toLowerCase(buffer[offset++])) {
                    case 'e': //len
                        return (len == 3
                        && Character.toLowerCase(buffer[offset++]) == 'n')
                        ? FTokenContext.KW_LEN : null;
                    case 'o': //logical
                        return (len == 7
                        && Character.toLowerCase(buffer[offset++]) == 'g'
                        && Character.toLowerCase(buffer[offset++]) == 'i'
                        && Character.toLowerCase(buffer[offset++]) == 'c'
                        && Character.toLowerCase(buffer[offset++]) == 'a'
                        && Character.toLowerCase(buffer[offset++]) == 'l')
                        ? FTokenContext.KW_LOGICAL : null;
                    default:
                        return null;
                }//switch l
                //END L
                
                //M
                //map module
            case 'm':
                switch (Character.toLowerCase(buffer[offset++])) {
                    case 'a': //map
                        return (len == 3
                        && Character.toLowerCase(buffer[offset++]) == 'p')
                        ? FTokenContext.KW_MAP : null;
                    case 'o': //module
                        return (len == 6
                        && Character.toLowerCase(buffer[offset++]) == 'd'
                        && Character.toLowerCase(buffer[offset++]) == 'u'
                        && Character.toLowerCase(buffer[offset++]) == 'l'
                        && Character.toLowerCase(buffer[offset++]) == 'e')
                        ? FTokenContext.KW_MODULE : null;
                    default:
                        return null;
                }
                //END M
                
                //N
                //name= named= namelist nextrec nml= none nullify number=
            case 'n':
                if ((len < 4) || (len > 8))
                    return null;
                
                switch (Character.toLowerCase(buffer[offset++])) {
                    case 'a': // name= named= namelist
                        if (len >= 5
                        && Character.toLowerCase(buffer[offset++]) == 'm'
                        && Character.toLowerCase(buffer[offset++]) == 'e') {
                            switch (Character.toLowerCase(buffer[offset++])) {
                                case '=': //name=
                                    return (len == 5)
                                    ? FTokenContext.KW_NAME_EQ : null;
                                case 'd': //named=
                                    return (len == 6
                                    && Character.toLowerCase(buffer[offset++]) == '=')
                                    ? FTokenContext.KW_NAMED_EQ : null;
                                case 'l': //namelist
                                    return (len == 8
                                    && Character.toLowerCase(buffer[offset++]) == 'i'
                                    && Character.toLowerCase(buffer[offset++]) == 's'
                                    && Character.toLowerCase(buffer[offset++]) == 't')
                                    ? FTokenContext.KW_NAMELIST : null;
                                default:
                                    return null;
                            }//switch na
                        }
                        else
                            return null;
                        
                    case 'e': // nextrec
                        return (len == 7
                        && Character.toLowerCase(buffer[offset++]) == 'x'
                        && Character.toLowerCase(buffer[offset++]) == 't'
                        && Character.toLowerCase(buffer[offset++]) == 'r'
                        && Character.toLowerCase(buffer[offset++]) == 'e'
                        && Character.toLowerCase(buffer[offset++]) == 'c')
                        ? FTokenContext.KW_NEXTREC : null;
                        
                    case 'm': // nml=
                        return (len == 4
                        && Character.toLowerCase(buffer[offset++]) == 'l'
                        && Character.toLowerCase(buffer[offset++]) == '=')
                        ? FTokenContext.KW_NML_EQ : null;
                        
                    case 'o': // none
                        return (len == 4
                        && Character.toLowerCase(buffer[offset++]) == 'n'
                        && Character.toLowerCase(buffer[offset++]) == 'e')
                        ? FTokenContext.KW_NONE : null;
                        
                    case 'u': // nullify number=
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'l': //nullify
                                return (len == 7
                                && Character.toLowerCase(buffer[offset++]) == 'l'
                                && Character.toLowerCase(buffer[offset++]) == 'i'
                                && Character.toLowerCase(buffer[offset++]) == 'f'
                                && Character.toLowerCase(buffer[offset++]) == 'y')
                                ? FTokenContext.KW_NULLIFY : null;
                            case 'm': //number=
                                return (len == 7
                                && Character.toLowerCase(buffer[offset++]) == 'b'
                                && Character.toLowerCase(buffer[offset++]) == 'e'
                                && Character.toLowerCase(buffer[offset++]) == 'r'
                                && Character.toLowerCase(buffer[offset++]) == '=')
                                ? FTokenContext.KW_NUMBER_EQ : null;
                            default:
                                return null;
                        }//switch nu
                        
                    default:
                        return null;
                }//switch n
                //END N
                
                //O
                //only open opened= operator optional out
            case 'o':
                if ((len < 3) || (len > 8))
                    return null;
                
                switch (Character.toLowerCase(buffer[offset++])) {
                    case 'n': // only
                        return (len == 4
                        && Character.toLowerCase(buffer[offset++]) == 'l'
                        && Character.toLowerCase(buffer[offset++]) == 'y')
                        ? FTokenContext.KW_ONLY : null;
                        
                    case 'p': // open opened= operator optional
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'e': // open opened= operator
                                switch (Character.toLowerCase(buffer[offset++])) {
                                    case 'n' : //open opened=
                                        if (len == 4)
                                            return FTokenContext.KW_OPEN;
                                        else if (len == 7  //opened=
                                        && Character.toLowerCase(buffer[offset++]) == 'e'
                                        && Character.toLowerCase(buffer[offset++]) == 'd'
                                        && Character.toLowerCase(buffer[offset++]) == '=')
                                            return FTokenContext.KW_OPENED_EQ;
                                        else
                                            return null;
                                    case 'r' : //operator
                                        return (len == 8
                                        && Character.toLowerCase(buffer[offset++]) == 'a'
                                        && Character.toLowerCase(buffer[offset++]) == 't'
                                        && Character.toLowerCase(buffer[offset++]) == 'o'
                                        && Character.toLowerCase(buffer[offset++]) == 'r')
                                        ? FTokenContext.KW_OPERATOR : null;
                                    default:
                                        return null;
                                } //switch ope
                            case 't': // optional
                                return (len == 8
                                && Character.toLowerCase(buffer[offset++]) == 'i'
                                && Character.toLowerCase(buffer[offset++]) == 'o'
                                && Character.toLowerCase(buffer[offset++]) == 'n'
                                && Character.toLowerCase(buffer[offset++]) == 'a'
                                && Character.toLowerCase(buffer[offset++]) == 'l')
                                ? FTokenContext.KW_OPTIONAL : null;
                            default:
                                return null;
                        }//switch op
                        
                    case 'u': // out
                        return (len == 3
                        && Character.toLowerCase(buffer[offset++]) == 't')
                        ? FTokenContext.KW_OUT : null;
                    default:
                        return null;
                } //switch o
                //END O
                
                //P
                //pad= parameter pointer position= precision print private procedure
                //program public pure
            case 'p':
                if ((len < 4) || (len > 9))
                    return null;
                
                switch (Character.toLowerCase(buffer[offset++])) {
                    case 'a': // pad= parameter
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'd': // pad=
                                return (len == 4
                                && Character.toLowerCase(buffer[offset++]) == '=')
                                ? FTokenContext.KW_PAD_EQ : null;
                            case 'r': // parameter
                                return (len == 9
                                && Character.toLowerCase(buffer[offset++]) == 'a'
                                && Character.toLowerCase(buffer[offset++]) == 'm'
                                && Character.toLowerCase(buffer[offset++]) == 'e'
                                && Character.toLowerCase(buffer[offset++]) == 't'
                                && Character.toLowerCase(buffer[offset++]) == 'e'
                                && Character.toLowerCase(buffer[offset++]) == 'r')
                                ? FTokenContext.KW_PARAMETER : null;
                            default:
                                return null;
                        }//switch pa
                        
                    case 'o': // pointer position=
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'i': // pointer
                                return (len == 7
                                && Character.toLowerCase(buffer[offset++]) == 'n'
                                && Character.toLowerCase(buffer[offset++]) == 't'
                                && Character.toLowerCase(buffer[offset++]) == 'e'
                                && Character.toLowerCase(buffer[offset++]) == 'r')
                                ? FTokenContext.KW_POINTER : null;
                            case 's': // position=
                                return (len == 9
                                && Character.toLowerCase(buffer[offset++]) == 'i'
                                && Character.toLowerCase(buffer[offset++]) == 't'
                                && Character.toLowerCase(buffer[offset++]) == 'i'
                                && Character.toLowerCase(buffer[offset++]) == 'o'
                                && Character.toLowerCase(buffer[offset++]) == 'n'
                                && Character.toLowerCase(buffer[offset++]) == '=')
                                ? FTokenContext.KW_POSITION_EQ : null;
                            default:
                                return null;
                        }//switch po
                        
                    case 'r': // precision print private procedure program
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'e': // precision
                                return (len == 9
                                && Character.toLowerCase(buffer[offset++]) == 'c'
                                && Character.toLowerCase(buffer[offset++]) == 'i'
                                && Character.toLowerCase(buffer[offset++]) == 's'
                                && Character.toLowerCase(buffer[offset++]) == 'i'
                                && Character.toLowerCase(buffer[offset++]) == 'o'
                                && Character.toLowerCase(buffer[offset++]) == 'n')
                                ? FTokenContext.KW_PRECISION : null;
                            case 'i': // print private
                                switch (Character.toLowerCase(buffer[offset++])) {
                                    case 'n' : //print
                                        return (len == 5
                                        && Character.toLowerCase(buffer[offset++]) == 't')
                                        ? FTokenContext.KW_PRINT : null;
                                    case 'v' : //private
                                        return (len == 7
                                        && Character.toLowerCase(buffer[offset++]) == 'a'
                                        && Character.toLowerCase(buffer[offset++]) == 't'
                                        && Character.toLowerCase(buffer[offset++]) == 'e')
                                        ? FTokenContext.KW_PRIVATE : null;
                                    default:
                                        return null;
                                } //switch pri
                            case 'o': // procedure program
                                switch (Character.toLowerCase(buffer[offset++])) {
                                    case 'c' ://procedure
                                        return (len == 9
                                        && Character.toLowerCase(buffer[offset++]) == 'e'
                                        && Character.toLowerCase(buffer[offset++]) == 'd'
                                        && Character.toLowerCase(buffer[offset++]) == 'u'
                                        && Character.toLowerCase(buffer[offset++]) == 'r'
                                        && Character.toLowerCase(buffer[offset++]) == 'e')
                                        ? FTokenContext.KW_PROCEDURE : null;
                                    case 'g' : //program
                                        return (len == 7
                                        && Character.toLowerCase(buffer[offset++]) == 'r'
                                        && Character.toLowerCase(buffer[offset++]) == 'a'
                                        && Character.toLowerCase(buffer[offset++]) == 'm')
                                        ? FTokenContext.KW_PROGRAM : null;
                                    default:
                                        return null;
                                } //switch pro
                            default:
                                return null;
                        } //switch pr
                        
                    case 'u': // public pure
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'b': // public
                                return (len == 6
                                && Character.toLowerCase(buffer[offset++]) == 'l'
                                && Character.toLowerCase(buffer[offset++]) == 'i'
                                && Character.toLowerCase(buffer[offset++]) == 'c')
                                ? FTokenContext.KW_PUBLIC : null;
                            case 'r': // pure
                                return (len == 4
                                && Character.toLowerCase(buffer[offset++]) == 'e')
                                ? FTokenContext.KW_PURE : null;
                            default:
                                return null;
                        }//switch pu
                        
                    default:
                        return null;
                } //switch p
                //END P
                
                //Q
                //quote
            case 'q':
                return (len == 5
                && Character.toLowerCase(buffer[offset++]) == 'u'
                && Character.toLowerCase(buffer[offset++]) == 'o'
                && Character.toLowerCase(buffer[offset++]) == 't'
                && Character.toLowerCase(buffer[offset++]) == 'e')
                ? FTokenContext.KW_QUOTE : null;
                //END Q
                
                //R
                //read read= readwrite= real rec= recl= recursive result return rewind
            case 'r':
                if ((len < 4) || (len > 10))
                    return null;
                
                if (Character.toLowerCase(buffer[offset++]) != 'e') {
                    return null;
                }
                
                switch (Character.toLowerCase(buffer[offset++])) {
                    case 'a': // read read= readwrite= real
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'd': // read
                                if (len == 4)
                                    return FTokenContext.KW_READ;
                                switch (Character.toLowerCase(buffer[offset++])) {
                                    case '=': // read=
                                        return (len == 5)
                                        ? FTokenContext.KW_READ_EQ : null;
                                    case 'w' : //readwrite=
                                        return (len == 10
                                        && Character.toLowerCase(buffer[offset++]) == 'r'
                                        && Character.toLowerCase(buffer[offset++]) == 'i'
                                        && Character.toLowerCase(buffer[offset++]) == 't'
                                        && Character.toLowerCase(buffer[offset++]) == 'e'
                                        && Character.toLowerCase(buffer[offset++]) == '=')
                                        ? FTokenContext.KW_READWRITE_EQ : null;
                                    default:
                                        return null;
                                } //switch read
                            case 'l': // real
                                return (len == 4)
                                ? FTokenContext.KW_REAL : null;
                            default:
                                return null;
                        }//switch rea
                        
                    case 'c': // rec= recl= recursive
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case '=': // rec=
                                return (len == 4)
                                ? FTokenContext.KW_REC_EQ : null;
                            case 'l': // recl=
                                return (len == 5
                                && Character.toLowerCase(buffer[offset++]) == '=')
                                ? FTokenContext.KW_RECL_EQ : null;
                            case 'u': // recursive
                                return (len == 9
                                && Character.toLowerCase(buffer[offset++]) == 'r'
                                && Character.toLowerCase(buffer[offset++]) == 's'
                                && Character.toLowerCase(buffer[offset++]) == 'i'
                                && Character.toLowerCase(buffer[offset++]) == 'v'
                                && Character.toLowerCase(buffer[offset++]) == 'e')
                                ? FTokenContext.KW_RECURSIVE : null;
                            default:
                                return null;
                        }//switch rec
                        
                    case 's': // result
                        return (len == 6
                        && Character.toLowerCase(buffer[offset++]) == 'u'
                        && Character.toLowerCase(buffer[offset++]) == 'l'
                        && Character.toLowerCase(buffer[offset++]) == 't')
                        ? FTokenContext.KW_RESULT : null;
                        
                    case 't': // return
                        return (len == 6
                        && Character.toLowerCase(buffer[offset++]) == 'u'
                        && Character.toLowerCase(buffer[offset++]) == 'r'
                        && Character.toLowerCase(buffer[offset++]) == 'n')
                        ? FTokenContext.KW_RETURN : null;
                        
                    case 'w': // rewind
                        return (len == 6
                        && Character.toLowerCase(buffer[offset++]) == 'i'
                        && Character.toLowerCase(buffer[offset++]) == 'n'
                        && Character.toLowerCase(buffer[offset++]) == 'd')
                        ? FTokenContext.KW_REWIND : null;
                        
                    default:
                        return null;
                }  //switch r
                //END R
                
                // S
                // save size stop select selectcase sequence sequential= size= stat= status=
                // structure subroutine
            case 's':
                if ((len < 4) || (len > 11))
                    return null;
                
                if (len == 4) {
                    switch (Character.toLowerCase(buffer[offset++])) {
                        case 'a':// save
                            return (len == 4
                            && Character.toLowerCase(buffer[offset++]) == 'v'
                            && Character.toLowerCase(buffer[offset++]) == 'e')
                            ? FTokenContext.KW_SAVE : null;
                            
                        case 'i':// size
                            return (len == 4
                            && Character.toLowerCase(buffer[offset++]) == 'z'
                            && Character.toLowerCase(buffer[offset++]) == 'e')
                            ? FTokenContext.KW_SIZE : null;
                            
                        case 't': // stop
                            return (len == 4
                            && Character.toLowerCase(buffer[offset++]) == 'o'
                            && Character.toLowerCase(buffer[offset++]) == 'p')
                            ? FTokenContext.KW_STOP : null;
                        default:
                            return null;
                    }
                }
                switch (Character.toLowerCase(buffer[offset++])) {
                    case 'e': // select selectcase sequence sequential=
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'l': //select selectcase
                                if (len >= 6
                                && Character.toLowerCase(buffer[offset++]) == 'e'
                                && Character.toLowerCase(buffer[offset++]) == 'c'
                                && Character.toLowerCase(buffer[offset++]) == 't') {
                                    if (len == 6)
                                        return FTokenContext.KW_SELECT ;
                                    else if (len == 10
                                    && Character.toLowerCase(buffer[offset++]) == 'c'
                                    && Character.toLowerCase(buffer[offset++]) == 'a'
                                    && Character.toLowerCase(buffer[offset++]) == 's'
                                    && Character.toLowerCase(buffer[offset++]) == 'e')
                                        return FTokenContext.KW_SELECTCASE ;
                                    else
                                        return null;
                                }
                                else
                                    return null;
                            case 'q': //sequence sequential=
                                if (len >= 8
                                && Character.toLowerCase(buffer[offset++]) == 'u'
                                && Character.toLowerCase(buffer[offset++]) == 'e'
                                && Character.toLowerCase(buffer[offset++]) == 'n') {
                                    switch (Character.toLowerCase(buffer[offset++])) {
                                        case 'c':   //sequence
                                            return (len == 8
                                            && Character.toLowerCase(buffer[offset++]) == 'e')
                                            ? FTokenContext.KW_SEQUENCE : null;
                                        case 't':  //sequential=
                                            return (len == 11
                                            && Character.toLowerCase(buffer[offset++]) == 'i'
                                            && Character.toLowerCase(buffer[offset++]) == 'a'
                                            && Character.toLowerCase(buffer[offset++]) == 'l'
                                            && Character.toLowerCase(buffer[offset++]) == '=')
                                            ? FTokenContext.KW_SEQUENTIAL_EQ : null;
                                        default:
                                            return null;
                                    } //switch sequen
                                }
                                else
                                    return null;
                            default:
                                return null;
                        } //switch se
                        
                    case 'i': // size=
                        return (len == 5
                        && Character.toLowerCase(buffer[offset++]) == 'z'
                        && Character.toLowerCase(buffer[offset++]) == 'e'
                        && Character.toLowerCase(buffer[offset++]) == '=')
                        ? FTokenContext.KW_SIZE_EQ : null;
                        
                    case 't': // stat= status= stop
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'a': // stat= status=
                                if (len >= 5
                                && Character.toLowerCase(buffer[offset++]) != 't') {
                                    switch (Character.toLowerCase(buffer[offset++])) {
                                        case '=' : //stat=
                                            return (len == 5)
                                            ? FTokenContext.KW_STAT_EQ : null;
                                        case 'u' : //status=
                                            return (len == 7
                                            && Character.toLowerCase(buffer[offset++]) == 's'
                                            && Character.toLowerCase(buffer[offset++]) == '=')
                                            ? FTokenContext.KW_STATUS_EQ : null;
                                        default:
                                            return null;
                                    }//switch stat
                                }//if
                                else
                                    return null;
                                
                            case 'r': // structure
                                return (len == 9
                                && Character.toLowerCase(buffer[offset++]) == 'u'
                                && Character.toLowerCase(buffer[offset++]) == 'c'
                                && Character.toLowerCase(buffer[offset++]) == 't'
                                && Character.toLowerCase(buffer[offset++]) == 'u'
                                && Character.toLowerCase(buffer[offset++]) == 'r'
                                && Character.toLowerCase(buffer[offset++]) == 'e')
                                ? FTokenContext.KW_STRUCTURE : null;
                                
                            default:
                                return null;
                        }//switch st
                        
                    case 'u': // subroutine
                        return (len == 10
                        && Character.toLowerCase(buffer[offset++]) == 'b'
                        && Character.toLowerCase(buffer[offset++]) == 'r'
                        && Character.toLowerCase(buffer[offset++]) == 'o'
                        && Character.toLowerCase(buffer[offset++]) == 'u'
                        && Character.toLowerCase(buffer[offset++]) == 't'
                        && Character.toLowerCase(buffer[offset++]) == 'i'
                        && Character.toLowerCase(buffer[offset++]) == 'n'
                        && Character.toLowerCase(buffer[offset++]) == 'e')
                        ? FTokenContext.KW_SUBROUTINE : null;
                        
                    default:
                        return null;
                }//switch s
                //END S
                
                //T
                //target then to type
            case 't': // then type
                if ((len < 2) || (len > 6))
                    return null;
                
                switch (Character.toLowerCase(buffer[offset++])) {
                    case 'a': // target
                        return (len == 6
                        && Character.toLowerCase(buffer[offset++]) == 'r'
                        && Character.toLowerCase(buffer[offset++]) == 'g'
                        && Character.toLowerCase(buffer[offset++]) == 'e'
                        && Character.toLowerCase(buffer[offset++]) == 't')
                        ? FTokenContext.KW_TARGET : null;
                        
                    case 'h': // then
                        return (len == 4
                        && Character.toLowerCase(buffer[offset++]) == 'e'
                        && Character.toLowerCase(buffer[offset++]) == 'n')
                        ? FTokenContext.KW_THEN : null;
                        
                    case 'o': // to
                        return (len == 2)
                        ? FTokenContext.KW_TO : null;
                        
                    case 'y': // type
                        return (len == 4
                        && Character.toLowerCase(buffer[offset++]) == 'p'
                        && Character.toLowerCase(buffer[offset++]) == 'e')
                        ? FTokenContext.KW_TYPE : null;
                        
                    default:
                        return null;
                }	 //switch t
                //END T
                
                //U
                //unformatted= use
            case 'u':
                if ((len < 3) || (len > 12))
                    return null;
                
                switch (Character.toLowerCase(buffer[offset++])) {
                    case 'n':
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'f': //unformatted=
                                return (len == 12
                                && Character.toLowerCase(buffer[offset++]) == 'o'
                                && Character.toLowerCase(buffer[offset++]) == 'r'
                                && Character.toLowerCase(buffer[offset++]) == 'm'
                                && Character.toLowerCase(buffer[offset++]) == 'a'
                                && Character.toLowerCase(buffer[offset++]) == 't'
                                && Character.toLowerCase(buffer[offset++]) == 't'
                                && Character.toLowerCase(buffer[offset++]) == 'e'
                                && Character.toLowerCase(buffer[offset++]) == 'd'
                                && Character.toLowerCase(buffer[offset++]) == '=')
                                ? FTokenContext.KW_UNFORMATTED_EQ : null;
                            case 'i': //union
                                return (len == 5
                                && Character.toLowerCase(buffer[offset++]) == 'o'
                                && Character.toLowerCase(buffer[offset++]) == 'n')
                                ? FTokenContext.KW_UNION : null;
                            default:
                                return null;
                        }//end switch
                    case 's': //use
                        return (len == 3
                        && Character.toLowerCase(buffer[offset++]) == 'e')
                        ? FTokenContext.KW_USE : null;
                        
                    default:
                        return null;
                }//switch un
                //END U
                
                //W
                //where while write write=
            case 'w': // where while write
                if ((len < 5) || (len > 6))
                    return null;
                
                switch (Character.toLowerCase(buffer[offset++])) {
                    case 'h': // where while
                        switch (Character.toLowerCase(buffer[offset++])) {
                            case 'e': // where
                                return (len == 5
                                && Character.toLowerCase(buffer[offset++]) == 'r'
                                && Character.toLowerCase(buffer[offset++]) == 'e')
                                ? FTokenContext.KW_WHERE : null;
                            case 'i': // while
                                return (len == 5
                                && Character.toLowerCase(buffer[offset++]) == 'l'
                                && Character.toLowerCase(buffer[offset++]) == 'e')
                                ? FTokenContext.KW_WHILE : null;
                            default:
                                return null;
                        }//switch wh
                        
                    case 'r': // write write=
                        if (len >= 5
                        && Character.toLowerCase(buffer[offset++]) == 'i'
                        && Character.toLowerCase(buffer[offset++]) == 't'
                        && Character.toLowerCase(buffer[offset++]) == 'e') {
                            if (len == 5)
                                return FTokenContext.KW_WRITE;
                            else if (len == 6
                            && Character.toLowerCase(buffer[offset++]) == '=')
                                return FTokenContext.KW_WRITE_EQ;
                            else
                                return null;
                        }
                        else
                            return null;
                        
                    default:
                        return null;
                }//switch w
                //END W
                
            default:
                return null;
        } //switch
        //END MOTHER SWITCH
    } //matchKeyword
    
    
    public static class MyStateInfo extends Syntax.BaseStateInfo {
        
        /** the column number of last porcessed character */
        private int column;
        
        public int getColumn() {
            return column;
        }
        
        public void setColumn(int column) {
            this.column = column;
        }
    }
    
    /** Load valid mark state into the analyzer. Offsets
     * are already initialized when this method is called. This method
     * must get the state from the mark and set it to the analyzer. Then
     * it must decrease tokenOffset by the preScan stored in the mark state.
     * @param markState mark state to be loaded into syntax. It must be non-null value.
     */
    @Override
    public void loadState(StateInfo stateInfo) {
        super.loadState( stateInfo );
        // lastNL < 0 means that the last \n was somewhere in the previous buffer.
        lastNL = offset - ((MyStateInfo)stateInfo).getColumn();
    }
    
    /** Store state of this analyzer into given mark state. */
    @Override
    public void storeState(StateInfo stateInfo) {
        super.storeState( stateInfo );
        ((MyStateInfo)stateInfo).setColumn( offset-lastNL  );
    }
    
    /** Compare state of this analyzer to given state info */
    @Override
    public int compareState(StateInfo stateInfo) {
        if( super.compareState( stateInfo ) == DIFFERENT_STATE )
            return DIFFERENT_STATE;
        return ( ((MyStateInfo)stateInfo).getColumn() == (offset - lastNL)) ?
        EQUAL_STATE : DIFFERENT_STATE;
    }
    
    /** Create state info appropriate for particular analyzer */
    @Override
    public StateInfo createStateInfo() {
        return new MyStateInfo();
    }
    
}
