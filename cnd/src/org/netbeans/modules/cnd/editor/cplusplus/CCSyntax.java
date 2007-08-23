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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.editor.cplusplus;

import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenID;

/**
 * Syntax analyzes for CC source files.
 * Tokens and internal states are given below.
 */
public class CCSyntax extends Syntax {

    // Internal states
    private static final int ISI_WHITESPACE = 2; // inside white space
    private static final int ISI_LINE_COMMENT = 4; // inside line comment //
    private static final int ISI_BLOCK_COMMENT = 5; // inside block comment /* ... */
    private static final int ISI_STRING = 6; // inside string constant
    private static final int ISI_STRING_A_BSLASH = 7; // inside string constant after backslash
    private static final int ISI_CHAR = 8; // inside char constant
    private static final int ISI_CHAR_A_BSLASH = 9; // inside char constant after backslash
    private static final int ISI_IDENTIFIER = 10; // inside identifier
    private static final int ISA_SLASH = 11; // slash char
    private static final int ISA_EQ = 12; // after '='
    private static final int ISA_GT = 13; // after '>'
    private static final int ISA_GTGT = 14; // after '>>'
    private static final int ISA_LT = 16; // after '<'
    private static final int ISA_LTLT = 17; // after '<<'
    private static final int ISA_PLUS = 18; // after '+'
    private static final int ISA_MINUS = 19; // after '-'
    private static final int ISA_STAR = 20; // after '*'
    private static final int ISA_STAR_I_BLOCK_COMMENT = 21; // after '*'
    private static final int ISA_PIPE = 22; // after '|'
    private static final int ISA_PERCENT = 23; // after '%'
    private static final int ISA_AND = 24; // after '&'
    private static final int ISA_XOR = 25; // after '^'
    private static final int ISA_EXCLAMATION = 26; // after '!'
    private static final int ISA_ZERO = 27; // after '0'
    private static final int ISI_INT = 28; // integer number
    private static final int ISI_OCTAL = 29; // octal number
    private static final int ISI_DOUBLE = 30; // double number
    private static final int ISI_DOUBLE_EXP = 31; // double number
    private static final int ISI_HEX = 32; // hex number
    private static final int ISA_DOT = 33; // after '.'
    private static final int ISA_HASH = 34; // after "#"
    private static final int ISA_HASH_WS = ISA_HASH + 1; // after "#" and whitespace
    private static final int ISA_BACKSLASH = ISA_HASH_WS + 1; // after backslash
    private static final int ISA_LINE_CONTINUATION = ISA_BACKSLASH + 1; // after backslash eol
    private static final int ISA_COMMA = ISA_LINE_CONTINUATION + 1; // after backslash eol
    private static final int ISA_INCLUDE = ISA_COMMA + 1; // after #include 
    private static final int ISA_INCLUDE_A_WS = ISA_INCLUDE + 1; // after #include and whitespaces
    private static final int ISI_SYS_INCLUDE = ISA_INCLUDE_A_WS + 1; // inside <filename> include directive
    private static final int ISI_USR_START_INCLUDE = ISI_SYS_INCLUDE + 1; // inside "filename" include directive at first '"'
    private static final int ISI_USR_INCLUDE = ISI_USR_START_INCLUDE + 1; // inside "filename" include directive
    private static final int ISA_COLON = ISI_USR_INCLUDE + 1; // after ':'
    private static final int ISA_ARROW = ISA_COLON + 1; // after '->'
    
    protected static final String IS_CPLUSPLUS = "C++"; // NOI18N
    protected static final String IS_C = "C"; // NOI18N
    
    protected String lang;

    public CCSyntax() {
        tokenContextPath = CCTokenContext.contextPath;
        lang = IS_CPLUSPLUS;
    }

    protected TokenID parseToken() {
        char actChar;

        while(offset < stopOffset) {
            actChar = buffer[offset];

            switch (state) {
            case INIT:
                switch (actChar) {
                case '"': 
                    state = ISI_STRING;
                    break;
                case '\'':
                    state = ISI_CHAR;
                    break;
                case '/':
                    state = ISA_SLASH;
                    break;
                case '\\':
                    state = ISA_BACKSLASH;
                    break;
                case '=':
                    state = ISA_EQ;
                    break;
                case '>':
                    state = ISA_GT;
                    break;
                case '<':
                    state = ISA_LT;
                    break;
                case '+':
                    state = ISA_PLUS;
                    break;
                case '-':
                    state = ISA_MINUS;
                    break;
                case '*':
                    state = ISA_STAR;
                    break;
                case '|':
                    state = ISA_PIPE;
                    break;
                case '%':
                    state = ISA_PERCENT;
                    break;
                case '&':
                    state = ISA_AND;
                    break;
                case '^':
                    state = ISA_XOR;
                    break;
                case '~':
                    offset++;
                    return CCTokenContext.NEG;
                case '!':
                    state = ISA_EXCLAMATION;
                    break;
                case '0':
                    state = ISA_ZERO;
                    break;
                case '.':
                    state = ISA_DOT;
                    break;
                case ',':
                    offset++;
                    return CCTokenContext.COMMA;
                case ';':
                    offset++;
                    return CCTokenContext.SEMICOLON;
                case ':':
                    //state = ISA_COMMA;
                    state = ISA_COLON;
                    break;
                case '?':
                    offset++;
                    return CCTokenContext.QUESTION;
                case '(':
                    offset++;
                    return CCTokenContext.LPAREN;
                case ')':
                    offset++;
                    return CCTokenContext.RPAREN;
                case '[':
                    offset++;
                    return CCTokenContext.LBRACKET;
                case ']':
                    offset++;
                    return CCTokenContext.RBRACKET;
                case '{':
                    offset++;
                    return CCTokenContext.LBRACE;
                case '}':
                    offset++;
                    return CCTokenContext.RBRACE;
                case '#':
                    state = ISA_HASH;
                    break;

                default:
                    // Check for whitespace
                    if (Character.isWhitespace(actChar)) {
                        state = ISI_WHITESPACE;
                        break;
                    }

                    // Check for digit
                    if (Character.isDigit(actChar)) {
                        state = ISI_INT;
                        break;
                    }

                    // Check for identifier

                    // At this point, you're probably wondering
                    // "why is he using isJavaIdentifier here
                    // when this is C++, not Java?
                    // The answer is that isJavaIdentifierStart
                    // is implemented very efficiently. Implementing
                    // something equivalent requires a huge lookup
                    // table, probably not worth the extra footprint
                    // considering that isJavaIdentifierStart pretty
                    // closely matches what is considered an
                    // identifier in C++.  The main difference seems
                    // to be that allowable unicode characters that
                    // are not ASCII *would* be allowed by this
                    // function. Not worth the trouble IMHO.

                    // XXX Perhaps I should write an efficient
                    // recognizor here which ONLY considers ASCII
                    // characters valid! (as identifiers that is).
                    // That allows a small table (or even fairly simple
                    // bit operations on the character value)
                    // since I can take advantage of unicode's
                    // ASCII range.
                    // But this might require some convoluted logic since the
                    // compiler PARTIALLY allows other code sets.
                    // Nay, I say, isJ* is okay!
                    if (Character.isJavaIdentifierStart(actChar)) {
                        state = ISI_IDENTIFIER;
                        break;
                    }

                    offset++;
                    return CCTokenContext.INVALID_CHAR;
                } // end of case INIT
                break;

            case ISI_WHITESPACE: // white space
                if (!Character.isWhitespace(actChar)) {
                    state = INIT;
                    return CCTokenContext.WHITESPACE;
                }
                break;

            case ISI_LINE_COMMENT:
                switch (actChar) {
                case '\n':
                    state = INIT;
                    return CCTokenContext.LINE_COMMENT;
                }
                break;

            case ISI_BLOCK_COMMENT:
                switch (actChar) {
                case '*':
                    state = ISA_STAR_I_BLOCK_COMMENT;
                    break;
                }
                break;

            case ISI_STRING:
                switch (actChar) {
                case '\\':
                    state = ISI_STRING_A_BSLASH;
                    break;
                case '\n':
                    state = INIT;
                    supposedTokenID = CCTokenContext.STRING_LITERAL;
// was commented in java                    return CCTokenContext.INCOMPLETE_STRING_LITERAL;
                    return supposedTokenID;
                case '"':
                    offset++;
                    state = INIT;
                    return CCTokenContext.STRING_LITERAL;
                }
                break;

            case ISI_STRING_A_BSLASH:
                switch (actChar) {
                case '"':
                case '\\':
                    break;
                default:
                    offset--;
                    break;
                }
                state = ISI_STRING;
                break;

            case ISI_CHAR:
                switch (actChar) {
                case '\\':
                    state = ISI_CHAR_A_BSLASH;
                    break;
                case '\n':
                    state = INIT;
                    supposedTokenID = CCTokenContext.CHAR_LITERAL;
// was commented in java                    return CCTokenContext.INCOMPLETE_CHAR_LITERAL;
                    return supposedTokenID;
                case '\'':
                    offset++;
                    state = INIT;
                    return CCTokenContext.CHAR_LITERAL;
                }
                break;

            case ISI_CHAR_A_BSLASH:
                switch (actChar) {
                case '\'':
                case '\\':
                    break;
                default:
                    offset--;
                    break;
                }
                state = ISI_CHAR;
                break;

            case ISI_IDENTIFIER:
		// For a comment of why we use isJAVAidentifier here,
		// grep backwards for isJavaIdentifierStart
                if (!(Character.isJavaIdentifierPart(actChar))) {
                    state = INIT;
                    TokenID tid = matchKeyword(buffer, tokenOffset, offset - tokenOffset);
                    if (tid == null) {
                        tid = matchCPPKeyword(buffer, tokenOffset,  offset - tokenOffset);
                        if (tid != null &&
			    ((tid.getNumericID() == CCTokenContext.CPPINCLUDE_ID) ||
			    (tid.getNumericID() == CCTokenContext.CPPINCLUDE_NEXT_ID))) {
                            state = ISA_INCLUDE;
                        }                        
                    }
                    return (tid != null) ? tid : CCTokenContext.IDENTIFIER;
                }
                break;

            case ISA_INCLUDE:
                if (!(isSpaceChar(actChar) || actChar == '"' || actChar == '<')) {
                    state = INIT;
                    return CCTokenContext.CPPINCLUDE;                                        
                }                 
                state = ISA_INCLUDE_A_WS; 
                break;
                
            case ISA_INCLUDE_A_WS:
                if (isSpaceChar(actChar)) {
                    state = ISA_INCLUDE_A_WS;
                    break;
                }
                switch (actChar) {
                case '<':
                    state = ISI_SYS_INCLUDE;
                    break;
                case '"':
                    state = ISI_USR_START_INCLUDE;
                    break;
                default:
                    if (Character.isJavaIdentifierStart(actChar)) {
                        state = ISI_IDENTIFIER;
                        break;
                    } else {     
                        // does not consume actChar, as it is part of next token
                        state = INIT;
                    }
                }
                return CCTokenContext.WHITESPACE;
                
            case ISI_SYS_INCLUDE:
                switch (actChar) {
                    case '>':
                    // consume actChar, as it is part of token
                    offset++;
                    state = INIT;
                    supposedTokenID = CCTokenContext.SYS_INCLUDE;
                    // check non-empty included file #include <>
                    return ((offset - tokenOffset) <= 2) ? 
                            CCTokenContext.INCOMPLETE_SYS_INCLUDE :
                            CCTokenContext.SYS_INCLUDE;
                    case '\n':
                    // new line without closed '"'
                    // does not consume actChar, as it is part of next token
                    state = INIT;     
                    supposedTokenID = CCTokenContext.SYS_INCLUDE;
                    return CCTokenContext.INCOMPLETE_SYS_INCLUDE;
                    default:
                }
                break;
                
            case ISI_USR_START_INCLUDE:
                switch (actChar) {
                    case '"':
                    // does not consume actChar, as it is done at the end of main while loop
                    state = ISI_USR_INCLUDE;
                    break;
                    default:
                }
                break;  
  
            case ISI_USR_INCLUDE:
                switch (actChar) {
                    case '"':
                    // consume actChar, as it is part of token
                    offset++;
                    state = INIT;
                    supposedTokenID = CCTokenContext.USR_INCLUDE;
                    // check non-empty included file #include ""
                    return ((offset - tokenOffset) <= 2) ? 
                        CCTokenContext.INCOMPLETE_USR_INCLUDE : 
                        CCTokenContext.USR_INCLUDE;
                    case '\n':
                    // does not consume actChar, as it is part of next token    
                    state = INIT;   
                    supposedTokenID = CCTokenContext.USR_INCLUDE;
                    return CCTokenContext.INCOMPLETE_USR_INCLUDE;                    
                    default:
                }
                break;
                
            case ISA_SLASH:
                switch (actChar) {
                case '=':
                    offset++;
                    state = INIT;
                    return CCTokenContext.DIV_EQ;
                case '/':
                    state = ISI_LINE_COMMENT;
                    break;
                case '*':
                    state = ISI_BLOCK_COMMENT;
                    break;
                default:
                    state = INIT;
                    return CCTokenContext.DIV;
                }
                break;

            case ISA_BACKSLASH:
                switch (actChar) {
                case '\n':
                    state = ISA_LINE_CONTINUATION;
                    break;
                default:
                    state = INIT;
                    return CCTokenContext.INVALID_BACKSLASH;
                }
                break;
		    
            case ISA_LINE_CONTINUATION:
                state = INIT;
                return CCTokenContext.BACKSLASH;

	    case ISA_HASH:
                // Check for whitespace, but not eol
                if (isSpaceChar(actChar)) {
                    state = ISA_HASH_WS;
                    break;
                }

                // Check for identifier

                // At this point, you're probably wondering
                // "why is he using isJavaIdentifier here
                // when this is C++, not Java?
                // The answer is that isJavaIdentifierStart
                // is implemented very efficiently. Implementing
                // something equivalent requires a huge lookup
                // table, probably not worth the extra footprint
                // considering that isJavaIdentifierStart pretty
                // closely matches what is considered an
                // identifier in C++.  The main difference seems
                // to be that allowable unicode characters that
                // are not ASCII *would* be allowed by this
                // function. Not worth the trouble IMHO.

                // XXX Perhaps I should write an efficient
                // recognizor here which ONLY considers ASCII
                // characters valid! (as identifiers that is).
                // That allows a small table (or even fairly simple
                // bit operations on the character value)
                // since I can take advantage of unicode's
                // ASCII range.
                // But this might require some convoluted logic since the
                // compiler PARTIALLY allows other code sets.
                // Nay, I say, isJ* is okay!
                if (Character.isJavaIdentifierStart(actChar)) {
                    state = ISI_IDENTIFIER;
                    break;
                }
                
                state = INIT;
                if (actChar == '#') {
                    offset++;
                    return CCTokenContext.DOUBLE_HASH;
                }
                return CCTokenContext.HASH;

	    case ISA_HASH_WS:
                // Check for whitespace, but not eol
                if (isSpaceChar(actChar)) {
                    state = ISA_HASH_WS;
                    break;
                }

                // Check for identifier

                // At this point, you're probably wondering
                // "why is he using isJavaIdentifier here
                // when this is C++, not Java?
                // The answer is that isJavaIdentifierStart
                // is implemented very efficiently. Implementing
                // something equivalent requires a huge lookup
                // table, probably not worth the extra footprint
                // considering that isJavaIdentifierStart pretty
                // closely matches what is considered an
                // identifier in C++.  The main difference seems
                // to be that allowable unicode characters that
                // are not ASCII *would* be allowed by this
                // function. Not worth the trouble IMHO.

                // XXX Perhaps I should write an efficient
                // recognizor here which ONLY considers ASCII
                // characters valid! (as identifiers that is).
                // That allows a small table (or even fairly simple
                // bit operations on the character value)
                // since I can take advantage of unicode's
                // ASCII range.
                // But this might require some convoluted logic since the
                // compiler PARTIALLY allows other code sets.
                // Nay, I say, isJ* is okay!
                if (Character.isJavaIdentifierStart(actChar)) {
                    state = ISI_IDENTIFIER;
                    break;
                }
                state = INIT;
                return CCTokenContext.HASH;
                
            case ISA_EQ:
                switch (actChar) {
                case '=':
                    offset++;
                    return  CCTokenContext.EQ_EQ;
                default:
                    state = INIT;
                    return CCTokenContext.EQ;
                }
                // break;

            case ISA_GT:
                switch (actChar) {
                case '>':
                    state = ISA_GTGT;
                    break;
                case '=':
                    offset++;
                    return CCTokenContext.GT_EQ;
                default:
                    state = INIT;
                    return CCTokenContext.GT;
                }
                break;

            case ISA_GTGT:
                switch (actChar) {
                case '=':
                    offset++;
                    return CCTokenContext.RSSHIFT_EQ;
                default:
                    state = INIT;
                    return CCTokenContext.RSSHIFT;
                }
                // break;

            case ISA_LT:
                switch (actChar) {
                case '<':
                    state = ISA_LTLT;
                    break;
                case '=':
                    offset++;
                    return CCTokenContext.LT_EQ;
                default:
                    state = INIT;
                    return CCTokenContext.LT;
                }
                break;

            case ISA_LTLT:
                switch (actChar) {
                case '<':
                    state = INIT;
                    offset++;
                    return CCTokenContext.INVALID_OPERATOR;
                case '=':
                    offset++;
                    return CCTokenContext.LSHIFT_EQ;
                default:
                    state = INIT;
                    return CCTokenContext.LSHIFT;
                }

            case ISA_PLUS:
                switch (actChar) {
                case '+':
                    offset++;
                    return CCTokenContext.PLUS_PLUS;
                case '=':
                    offset++;
                    return CCTokenContext.PLUS_EQ;
                default:
                    state = INIT;
                    return CCTokenContext.PLUS;
                }

            case ISA_MINUS:
                switch (actChar) {
                case '-':
                    offset++;
                    return CCTokenContext.MINUS_MINUS;
                case '=':
                    offset++;
                    return CCTokenContext.MINUS_EQ;
                case '>':
                    state = ISA_ARROW;
                    break;
                default:
                    state = INIT;
                    return CCTokenContext.MINUS;
                }
                break;

            case ISA_ARROW:
                switch (actChar) {
                    case '*':
                        state = INIT;
                        offset++;
                        return CCTokenContext.ARROWMBR;
                    default:
                        state = INIT;
                        return CCTokenContext.ARROW;
                }
                    
            case ISA_COMMA:
                state = INIT;
                return CCTokenContext.COMMA;
                
            case ISA_COLON:
                switch (actChar) {
                    case ':':
                    offset++;
                    return CCTokenContext.SCOPE;
                default:
                    state = INIT;
                    return CCTokenContext.COLON;
                }                
                
            case ISA_STAR:
                switch (actChar) {
                case '=':
                    offset++;
                    return CCTokenContext.MUL_EQ;
                case '/':
                    // either '*/' outside comment
                    // or pointer like
                    // int*/* commnet*/var;
                    if ((offset+1 < stopOffset) && (buffer[offset+1] != '*')) {
                        offset++;
                        state = INIT;
                        return CCTokenContext.INVALID_COMMENT_END; // '*/' outside comment
                    } else {
                        //nobreak;
                    }
                default:
                    state = INIT;
                    return CCTokenContext.MUL;
                }

            case ISA_STAR_I_BLOCK_COMMENT:
                switch (actChar) {
                case '/':
                    offset++;
                    state = INIT;
                    return CCTokenContext.BLOCK_COMMENT;
                default:
                    offset--;
                    state = ISI_BLOCK_COMMENT;
                    break;
                }
                break;

            case ISA_PIPE:
                switch (actChar) {
                case '=':
                    offset++;
                    state = INIT;
                    return CCTokenContext.OR_EQ;
                case '|':
                    offset++;
                    state = INIT;
                    return CCTokenContext.OR_OR;
                default:
                    state = INIT;
                    return CCTokenContext.OR;
                }
                // break;

            case ISA_PERCENT:
                switch (actChar) {
                case '=':
                    offset++;
                    state = INIT;
                    return CCTokenContext.MOD_EQ;
                default:
                    state = INIT;
                    return CCTokenContext.MOD;
                }
                // break;

            case ISA_AND:
                switch (actChar) {
                case '=':
                    offset++;
                    state = INIT;
                    return CCTokenContext.AND_EQ;
                case '&':
                    offset++;
                    state = INIT;
                    return CCTokenContext.AND_AND;
                default:
                    state = INIT;
                    return CCTokenContext.AND;
                }
                // break;

            case ISA_XOR:
                switch (actChar) {
                case '=':
                    offset++;
                    state = INIT;
                    return CCTokenContext.XOR_EQ;
                default:
                    state = INIT;
                    return CCTokenContext.XOR;
                }
                // break;

            case ISA_EXCLAMATION:
                switch (actChar) {
                case '=':
                    offset++;
                    state = INIT;
                    return CCTokenContext.NOT_EQ;
                default:
                    state = INIT;
                    return CCTokenContext.NOT;
                }
                // break;

            case ISA_ZERO:
                switch (actChar) {
                case '.':
                    state = ISI_DOUBLE;
                    break;
                case 'x':
                case 'X':
                    state = ISI_HEX;
                    break;
                case 'l':
                case 'L':
                    offset++;
                    state = INIT;
                    return CCTokenContext.LONG_LITERAL;
                case 'f':
                case 'F':
                    offset++;
                    state = INIT;
                    return CCTokenContext.FLOAT_LITERAL;
                case 'd':
                case 'D':
                    offset++;
                    state = INIT;
                    return CCTokenContext.DOUBLE_LITERAL;
                case '8': // it's error to have '8' and '9' in octal number
                case '9':
                    state = INIT;
                    offset++;
                    return CCTokenContext.INVALID_OCTAL_LITERAL;
                case 'e':
                case 'E':
                    state = ISI_DOUBLE_EXP;
                    break;
                default:
                    if (Character.isDigit(actChar)) { // '8' and '9' already handled
                        state = ISI_OCTAL;
                        break;
                    }
                    state = INIT;
                    return CCTokenContext.INT_LITERAL;
                }
                break;

            case ISI_INT:
                switch (actChar) {
                case 'l':
                case 'L':
                    offset++;
                    state = INIT;
                    return CCTokenContext.LONG_LITERAL;
                case '.':
                    state = ISI_DOUBLE;
                    break;
                case 'f':
                case 'F':
                    offset++;
                    state = INIT;
                    return CCTokenContext.FLOAT_LITERAL;
                case 'd':
                case 'D':
                    offset++;
                    state = INIT;
                    return CCTokenContext.DOUBLE_LITERAL;
                case 'e':
                case 'E':
                    state = ISI_DOUBLE_EXP;
                    break;
                default:
                    if (!(actChar >= '0' && actChar <= '9')) {
                        state = INIT;
                        return CCTokenContext.INT_LITERAL;
                    }
                }
                break;

            case ISI_OCTAL:
                if (!(actChar >= '0' && actChar <= '7')) {

                    state = INIT;
                    return CCTokenContext.OCTAL_LITERAL;
                }
                break;

            case ISI_DOUBLE:
                switch (actChar) {
                case 'f':
                case 'F':
                    offset++;
                    state = INIT;
                    return CCTokenContext.FLOAT_LITERAL;
                case 'd':
                case 'D':
                    offset++;
                    state = INIT;
                    return CCTokenContext.DOUBLE_LITERAL;
                case 'e':
                case 'E':
                    state = ISI_DOUBLE_EXP;
                    break;
                default:
                    if (!((actChar >= '0' && actChar <= '9')
                            || actChar == '.')) {

                        state = INIT;
                        return CCTokenContext.DOUBLE_LITERAL;
                    }
                }
                break;

            case ISI_DOUBLE_EXP:
                switch (actChar) {
                case 'f':
                case 'F':
                    offset++;
                    state = INIT;
                    return CCTokenContext.FLOAT_LITERAL;
                case 'd':
                case 'D':
                    offset++;
                    state = INIT;
                    return CCTokenContext.DOUBLE_LITERAL;
                default:
                    if (!(Character.isDigit(actChar)
                            || actChar == '-' || actChar == '+')) {
                        state = INIT;
                        return CCTokenContext.DOUBLE_LITERAL;
                    }
                }
                break;

            case ISI_HEX:
                if (!((actChar >= 'a' && actChar <= 'f')
                        || (actChar >= 'A' && actChar <= 'F')
                        || Character.isDigit(actChar))
                   ) {

                    state = INIT;
                    return CCTokenContext.HEX_LITERAL;
                }
                break;

            case ISA_DOT:
                if (Character.isDigit(actChar)) {
                    state = ISI_DOUBLE;
                } else if (actChar == '*') {
                    state = INIT;
                    offset++;
                    return CCTokenContext.DOTMBR;
                } else { // only single dot
                    state = INIT;
                    return CCTokenContext.DOT;
                }
                break;

            } // end of switch(state)

            offset++;
        } // end of while(offset...)

        /** At this stage there's no more text in the scanned buffer.
        * Scanner first checks whether this is completely the last
        * available buffer.
        */

        if (lastBuffer) {
            switch(state) {
            case ISI_WHITESPACE:
                state = INIT;
                return CCTokenContext.WHITESPACE;
            case ISI_IDENTIFIER:
                state = INIT;
                TokenID kwd = matchKeyword(buffer, tokenOffset, offset - tokenOffset);
                if (kwd == null) {
                    kwd = matchCPPKeyword(buffer, tokenOffset,  offset - tokenOffset);
                    if (kwd != null && kwd.getNumericID() == CCTokenContext.CPPINCLUDE_ID) {
                        state = ISA_INCLUDE;
                    }
                }
                return (kwd != null) ? kwd : CCTokenContext.IDENTIFIER;
            case ISI_LINE_COMMENT:
                return CCTokenContext.LINE_COMMENT; // stay in line-comment state
            case ISI_BLOCK_COMMENT:
            case ISA_STAR_I_BLOCK_COMMENT:
                return CCTokenContext.BLOCK_COMMENT; // stay in block-comment state
            case ISI_STRING:
            case ISI_STRING_A_BSLASH:
                return CCTokenContext.STRING_LITERAL; // hold the state
            case ISI_SYS_INCLUDE:
                return CCTokenContext.INCOMPLETE_SYS_INCLUDE; // hold the state
            case ISA_INCLUDE:
                return CCTokenContext.CPPINCLUDE; // hold the state
            case ISA_INCLUDE_A_WS:
                return CCTokenContext.WHITESPACE; // hold the state
            case ISI_USR_INCLUDE:
                return CCTokenContext.INCOMPLETE_USR_INCLUDE; // hold the state
            case ISI_CHAR:
            case ISI_CHAR_A_BSLASH:
                return CCTokenContext.CHAR_LITERAL; // hold the state
            case ISA_ZERO:
            case ISI_INT:
                state = INIT;
                return CCTokenContext.INT_LITERAL;
            case ISI_OCTAL:
                state = INIT;
                return CCTokenContext.OCTAL_LITERAL;
            case ISI_DOUBLE:
            case ISI_DOUBLE_EXP:
                state = INIT;
                return CCTokenContext.DOUBLE_LITERAL;
            case ISI_HEX:
                state = INIT;
                return CCTokenContext.HEX_LITERAL;
            case ISA_BACKSLASH:
                state = INIT;
                return CCTokenContext.BACKSLASH;
            case ISA_LINE_CONTINUATION:
                state = INIT;
                return CCTokenContext.LINE_CONTINUATION;
            case ISA_DOT:
                state = INIT;
                return CCTokenContext.DOT;
            case ISA_SLASH:
                state = INIT;
                return CCTokenContext.DIV;
            case ISA_EQ:
                state = INIT;
                return CCTokenContext.EQ;
            case ISA_GT:
                state = INIT;
                return CCTokenContext.GT;
            case ISA_GTGT:
                state = INIT;
                return CCTokenContext.RSSHIFT;
            case ISA_LT:
                state = INIT;
                return CCTokenContext.LT;
            case ISA_LTLT:
                state = INIT;
                return CCTokenContext.LSHIFT;
            case ISA_PLUS:
                state = INIT;
                return CCTokenContext.PLUS;
            case ISA_MINUS:
                state = INIT;
                return CCTokenContext.MINUS;
            case ISA_ARROW:
                state = INIT;
                return CCTokenContext.ARROW;                
            case ISA_COMMA:
                state = INIT;
                return CCTokenContext.COMMA;
            case ISA_STAR:
                state = INIT;
                return CCTokenContext.MUL;
            case ISA_PIPE:
                state = INIT;
                return CCTokenContext.OR;
            case ISA_PERCENT:
                state = INIT;
                return CCTokenContext.MOD;
            case ISA_AND:
                state = INIT;
                return CCTokenContext.AND;
            case ISA_XOR:
                state = INIT;
                return CCTokenContext.XOR;
            case ISA_EXCLAMATION:
                state = INIT;
                return CCTokenContext.NOT;
            case ISA_HASH:
                state = INIT;
                return CCTokenContext.HASH;
            case ISA_COLON:
                state = INIT;
                return CCTokenContext.COLON;
            }
        }

        /* At this stage there's no more text in the scanned buffer, but
        * this buffer is not the last so the scan will continue on another buffer.
        * The scanner tries to minimize the amount of characters
        * that will be prescanned in the next buffer by returning the token
        * where possible.
        */

        switch (state) {
        case ISI_WHITESPACE:
            return CCTokenContext.WHITESPACE;
        }

        return null; // nothing found
    }

    public String getStateName(int stateNumber) {
        switch(stateNumber) {
        case ISI_WHITESPACE:
            return "ISI_WHITESPACE"; //NOI18N
        case ISI_LINE_COMMENT:
            return "ISI_LINE_COMMENT"; //NOI18N
        case ISI_BLOCK_COMMENT:
            return "ISI_BLOCK_COMMENT"; //NOI18N
        case ISI_STRING:
            return "ISI_STRING"; //NOI18N
        case ISI_STRING_A_BSLASH:
            return "ISI_STRING_A_BSLASH"; //NOI18N
        case ISI_CHAR:
            return "ISI_CHAR"; //NOI18N
        case ISI_CHAR_A_BSLASH:
            return "ISI_CHAR_A_BSLASH"; //NOI18N
        case ISI_IDENTIFIER:
            return "ISI_IDENTIFIER"; //NOI18N
        case ISA_SLASH:
            return "ISA_SLASH"; //NOI18N
        case ISA_BACKSLASH:
            return "ISA_BACKSLASH"; //NOI18N
        case ISA_EQ:
            return "ISA_EQ"; //NOI18N
        case ISA_GT:
            return "ISA_GT"; //NOI18N
        case ISA_GTGT:
            return "ISA_GTGT"; //NOI18N
        case ISA_LT:
            return "ISA_LT"; //NOI18N
        case ISA_LTLT:
            return "ISA_LTLT"; //NOI18N
        case ISA_PLUS:
            return "ISA_PLUS"; //NOI18N
        case ISA_MINUS:
            return "ISA_MINUS"; //NOI18N
        case ISA_COMMA:
            return "ISA_COMMA"; //NOI18N
        case ISA_STAR:
            return "ISA_STAR"; //NOI18N
        case ISA_STAR_I_BLOCK_COMMENT:
            return "ISA_STAR_I_BLOCK_COMMENT"; //NOI18N
        case ISA_PIPE:
            return "ISA_PIPE"; //NOI18N
        case ISA_PERCENT:
            return "ISA_PERCENT"; //NOI18N
        case ISA_AND:
            return "ISA_AND"; //NOI18N
        case ISA_XOR:
            return "ISA_XOR"; //NOI18N
        case ISA_EXCLAMATION:
            return "ISA_EXCLAMATION"; //NOI18N
        case ISA_ZERO:
            return "ISA_ZERO"; //NOI18N
        case ISI_INT:
            return "ISI_INT"; //NOI18N
        case ISI_OCTAL:
            return "ISI_OCTAL"; //NOI18N
        case ISI_DOUBLE:
            return "ISI_DOUBLE"; //NOI18N
        case ISI_DOUBLE_EXP:
            return "ISI_DOUBLE_EXP"; //NOI18N
        case ISI_HEX:
            return "ISI_HEX"; //NOI18N
        case ISA_DOT:
            return "ISA_DOT"; //NOI18N
        case ISA_HASH:
            return "ISA_HASH"; //NOI18N
        case ISA_INCLUDE:
            return "ISA_INCLUDE"; //NOI18N
        case ISA_INCLUDE_A_WS:
            return "ISA_INCLUDE_A_WS"; //NOI18N
        case ISI_SYS_INCLUDE:
            return "ISI_SYS_INCLUDE"; //NOI18N
        case ISI_USR_INCLUDE:
            return "ISI_USR_INCLUDE"; //NOI18N
        case ISI_USR_START_INCLUDE:
            return "ISI_USR_START_INCLUDE"; //NOI18N	   
        default:
            return super.getStateName(stateNumber);
        }
    }

    public TokenID matchKeyword(char[] buffer, int offset, int len) {
//        String kw = new String(buffer, offset, len);
//        System.err.println("matchKeyword[" + lang + "]: " + kw);
        
        if (len > 16)
            return null;
        if (len <= 1)
            return null;
        switch (buffer[offset++]) {
        case 'a':
            if (len <= 2)
                return null;
            switch (buffer[offset++]) {
	    case 's':   // keyword "asm" (C++ only)
	      return (lang == IS_CPLUSPLUS && len == 3 && buffer[offset++] == 'm') ? CCTokenContext.ASM : null;
	    case 'u':   // keyword "auto"
	      return (len == 4 && buffer[offset++] == 't' && buffer[offset++] == 'o')
                    ? CCTokenContext.AUTO : null;
	    default:
	      return null;
	    }
        case 'b':
            if (len <= 3)
                return null;
            switch (buffer[offset++]) {
            case 'o':   // keyword "bool" (C++ only)
                return (lang == IS_CPLUSPLUS && len == 4
                        && buffer[offset++] == 'o'
                        && buffer[offset++] == 'l')
                       ? CCTokenContext.BOOLEAN : null;
            case 'r':   // keyword "break"
                return (len == 5
                        && buffer[offset++] == 'e'
                        && buffer[offset++] == 'a'
                        && buffer[offset++] == 'k')
                       ? CCTokenContext.BREAK : null;
            default:
                return null;
            }
	case 'c':
	    if (len <= 3)
		return null;
	    switch (buffer[offset++]) {
	    case 'a':
		switch (buffer[offset++]) {
		case 's': // keyword "case"
		    return (len == 4 && buffer[offset++] == 'e') ? CCTokenContext.CASE : null;
		case 't': // keyword "catch" (C++ only)
		    return (lang == IS_CPLUSPLUS && len == 5
			    && buffer[offset++] == 'c'
			    && buffer[offset++] == 'h')
			? CCTokenContext.CATCH : null;
		default:
		    return null;
		}
	    case 'h': // keyword "char"
		return (len == 4
			&& buffer[offset++] == 'a'
			&& buffer[offset++] == 'r')
		    ? CCTokenContext.CHAR : null;
	    case 'l': // keyword "class" (C++ only)
		return (lang == IS_CPLUSPLUS && len == 5
			&& buffer[offset++] == 'a'
			&& buffer[offset++] == 's'
			&& buffer[offset++] == 's')
		    ? CCTokenContext.CLASS : null;
	    case 'o':
		if (len <= 4)
		    return null;
		if (buffer[offset++] != 'n')
		    return null;
		switch (buffer[offset++]) {
		case 's':
		    if (lang == IS_CPLUSPLUS && len == 5) { // keyword "const" (C++ only)
			return (buffer[offset++] == 't') ? CCTokenContext.CONST : null;
		    } else if (lang == IS_CPLUSPLUS && len == 10) { // keyword "const_cast" (C++ only)
			return (buffer[offset++] == 't'
				&& buffer[offset++] == '_'
				&& buffer[offset++] == 'c'
				&& buffer[offset++] == 'a'
				&& buffer[offset++] == 's'
				&& buffer[offset++] == 't')
			    ? CCTokenContext.CONST_CAST : null;
		    } else {
			return null;
		    }
		case 't': // keyword "continue"
		    return (len == 8
			    && buffer[offset++] == 'i'
			    && buffer[offset++] == 'n'
			    && buffer[offset++] == 'u'
			    && buffer[offset++] == 'e')
			? CCTokenContext.CONTINUE : null;
		default:
		    return null;
		}
	    default:
		return null;
	    }
        case 'd':
	    if (len <= 1)
		return null;
	    switch (buffer[offset++]) {
	    case 'e':
		switch (buffer[offset++]) {
		case 'f': // keyword "default"
		    return (len == 7
			    && buffer[offset++] == 'a'
			    && buffer[offset++] == 'u'
			    && buffer[offset++] == 'l'
			    && buffer[offset++] == 't')
			? CCTokenContext.DEFAULT : null;
		case 'l': // keyword "delete" (C++ only)
		    return (lang == IS_CPLUSPLUS && len == 6
			    && buffer[offset++] == 'e'
			    && buffer[offset++] == 't'
			    && buffer[offset++] == 'e')
			? CCTokenContext.DELETE : null;
		default:
		    return null;
		}
	    case 'o':
		if (len == 2) { // keyword "do"
		    return CCTokenContext.DO;
		}
		return (len == 6 // keyword "double"
			&& buffer[offset++] == 'u'
			&& buffer[offset++] == 'b'
			&& buffer[offset++] == 'l'
			&& buffer[offset++] == 'e')
		    ? CCTokenContext.DOUBLE : null;
	    case 'y': // keyword "dynamic_cast" (C++ only)
		return (lang == IS_CPLUSPLUS && len == 12
			&& buffer[offset++] == 'n'
			&& buffer[offset++] == 'a'
			&& buffer[offset++] == 'm'
			&& buffer[offset++] == 'i'
			&& buffer[offset++] == 'c'
			&& buffer[offset++] == '_'
			&& buffer[offset++] == 'c'
			&& buffer[offset++] == 'a'
			&& buffer[offset++] == 's'
			&& buffer[offset++] == 't')
		    ? CCTokenContext.DYNAMIC_CAST : null;
	    default:
		return null;
	    }
	case 'e':
	    if (len <= 3)
		return null;
	    switch (buffer[offset++]) {
	    case 'l': // keyword "else"
		return (len == 4
			&& buffer[offset++] == 's'
			&& buffer[offset++] == 'e')
		    ? CCTokenContext.ELSE : null;
	    case 'n': // keyword "enum"
		return (len == 4
			&& buffer[offset++] == 'u'
			&& buffer[offset++] == 'm')
		    ? CCTokenContext.ENUM : null;
	    case 'x':
		switch (buffer[offset++]) {
		case 'p':
		    switch (buffer[offset++]) {
		    case 'l': // keyword "explicit" (C++ only)
			return (lang == IS_CPLUSPLUS && len == 8
				&& buffer[offset++] == 'i'
				&& buffer[offset++] == 'c'
				&& buffer[offset++] == 'i'
				&& buffer[offset++] == 't')
			    ? CCTokenContext.EXPLICIT : null;
		    case 'o': // keyword "export" (C++ only)
			return (lang == IS_CPLUSPLUS && len == 6
				&& buffer[offset++] == 'r'
				&& buffer[offset++] == 't')
			    ? CCTokenContext.EXPORT : null;
		    default:
			return null;
		    }
		case 't': // keyword "extern"
		    return (len == 6
			    && buffer[offset++] == 'e'
			    && buffer[offset++] == 'r'
			    && buffer[offset++] == 'n')
			? CCTokenContext.EXTERN : null;
		default:
		    return null;
		}
	    default:
		return null;
	    }
        case 'f':
            if (len <= 2)
                return null;
            switch (buffer[offset++]) {
            case 'a': // keyword "false" (C++ only)
                return (lang == IS_CPLUSPLUS && len == 5
                        && buffer[offset++] == 'l'
                        && buffer[offset++] == 's'
                        && buffer[offset++] == 'e')
                       ? CCTokenContext.FALSE : null;
            case 'l': // keyword "float"
                return (len == 5
                        && buffer[offset++] == 'o'
                        && buffer[offset++] == 'a'
                        && buffer[offset++] == 't')
                       ? CCTokenContext.FLOAT : null;
            case 'o': // keyword "for"
                return (len == 3
                        && buffer[offset++] == 'r')
                       ? CCTokenContext.FOR : null;
            case 'r': // keyword "friend" (C++ only)
                return (lang == IS_CPLUSPLUS && len == 6
                        && buffer[offset++] == 'i'
                        && buffer[offset++] == 'e'
                        && buffer[offset++] == 'n'
                        && buffer[offset++] == 'd')
                       ? CCTokenContext.FRIEND : null;
            default:
                return null;
            }
        case 'g': // keyword "goto"
            return (len == 4
                    && buffer[offset++] == 'o'
                    && buffer[offset++] == 't'
                    && buffer[offset++] == 'o')
                   ? CCTokenContext.GOTO : null;
        case 'i':
            switch (buffer[offset++]) {
            case 'f': // keyword "if"
                return (len == 2)
                       ? CCTokenContext.IF : null;
	    case 'n':
		switch (buffer[offset++]) {
		case 't': // keyword "int"
		    return (len == 3)
			? CCTokenContext.INT : null;
		case 'l': // keyword "inline"
		    return (len == 6
			    && buffer[offset++] == 'i'
			    && buffer[offset++] == 'n'
			    && buffer[offset++] == 'e')
			? CCTokenContext.INLINE : null;
		default:
		    return null;
		}	      
            default:
                return null;
            }
        case 'l': // keyword "long"
            return (len == 4
                    && buffer[offset++] == 'o'
                    && buffer[offset++] == 'n'
                    && buffer[offset++] == 'g')
                   ? CCTokenContext.LONG : null;
        case 'm': // keyword "mutable" (C++ only)
            return (lang == IS_CPLUSPLUS && len == 7
                    && buffer[offset++] == 'u'
                    && buffer[offset++] == 't'
                    && buffer[offset++] == 'a'
                    && buffer[offset++] == 'b'
                    && buffer[offset++] == 'l'
                    && buffer[offset++] == 'e')
                   ? CCTokenContext.MUTABLE : null;
        case 'n':
            if (len <= 2)
                return null;
            switch (buffer[offset++]) {
            case 'a': // keyword "namespace" (C++ only)
                return (lang == IS_CPLUSPLUS && len == 9
                        && buffer[offset++] == 'm'
                        && buffer[offset++] == 'e'
                        && buffer[offset++] == 's'
                        && buffer[offset++] == 'p'
                        && buffer[offset++] == 'a'
                        && buffer[offset++] == 'c'
                        && buffer[offset++] == 'e')
                       ? CCTokenContext.NAMESPACE : null;
            case 'e': // keyword "new" (C++ only)
                return (lang == IS_CPLUSPLUS && len == 3
                        && buffer[offset++] == 'w')
                       ? CCTokenContext.NEW : null;
            default:
                return null;
            }
        case 'o': // keyword "operator" (C++ only)
            return (lang == IS_CPLUSPLUS && len == 8
                    && buffer[offset++] == 'p'
                    && buffer[offset++] == 'e'
                    && buffer[offset++] == 'r'
                    && buffer[offset++] == 'a'
                    && buffer[offset++] == 't'
                    && buffer[offset++] == 'o'
                    && buffer[offset++] == 'r')
                   ? CCTokenContext.OPERATOR : null;
        case 'p':
            if (lang == IS_C || len <= 5)
                return null;
            switch (buffer[offset++]) {
            case 'r':
                if (len <= 6)
                    return null;
                switch (buffer[offset++]) {
                case 'i': // keyword "private" (C++ only)
                    return (len == 7
                            && buffer[offset++] == 'v'
                            && buffer[offset++] == 'a'
                            && buffer[offset++] == 't'
                            && buffer[offset++] == 'e')
                           ? CCTokenContext.PRIVATE : null;
                case 'o': // keyword "protected" (C++ only)
                    return (len == 9
                            && buffer[offset++] == 't'
                            && buffer[offset++] == 'e'
                            && buffer[offset++] == 'c'
                            && buffer[offset++] == 't'
                            && buffer[offset++] == 'e'
                            && buffer[offset++] == 'd')
                           ? CCTokenContext.PROTECTED : null;
                default:
                    return null;
                }
            case 'u': // keyword "public" (C++ only)
                return (len == 6
                        && buffer[offset++] == 'b'
                        && buffer[offset++] == 'l'
                        && buffer[offset++] == 'i'
                        && buffer[offset++] == 'c')
                       ? CCTokenContext.PUBLIC : null;
            default:
                return null;
            }
        case 'r':
	  if (len < 6 || buffer[offset++] != 'e')
	    return null;

	  switch (buffer[offset++]) {
	  case 'g': // keyword "register"
	      return (len == 8
		      && buffer[offset++] == 'i'
		      && buffer[offset++] == 's'
		      && buffer[offset++] == 't'
		      && buffer[offset++] == 'e'
		      && buffer[offset++] == 'r')
		? CCTokenContext.REGISTER : null;
	  case 'i': // keyword "reinterpret_cast" (C++ only)
	      return (lang == IS_CPLUSPLUS && len == 16
		      && buffer[offset++] == 'n'
		      && buffer[offset++] == 't'
		      && buffer[offset++] == 'e'
		      && buffer[offset++] == 'r'
		      && buffer[offset++] == 'p'
		      && buffer[offset++] == 'r'
		      && buffer[offset++] == 'e'
		      && buffer[offset++] == 't'
		      && buffer[offset++] == '_'
		      && buffer[offset++] == 'c'
		      && buffer[offset++] == 'a'
		      && buffer[offset++] == 's'
		      && buffer[offset++] == 't')
		? CCTokenContext.REINTERPRET_CAST : null;
	  case 's': // keyword "restrict"
            return (lang == IS_C && len == 8
                    && buffer[offset++] == 't'
                    && buffer[offset++] == 'r'
                    && buffer[offset++] == 'i'
                    && buffer[offset++] == 'c'
                    && buffer[offset++] == 't')
                   ? CCTokenContext.RESTRICT : null;
	  case 't': // keyword "return"
            return (len == 6
                    && buffer[offset++] == 'u'
                    && buffer[offset++] == 'r'
                    && buffer[offset++] == 'n')
                   ? CCTokenContext.RETURN : null;
	  default:
	    return null;
	  }
	case 's':
	    if (len <= 4)
		return null;
	    switch (buffer[offset++]) {
	    case 'h': // keyword "short"
		return (len == 5
			&& buffer[offset++] == 'o'
			&& buffer[offset++] == 'r'
			&& buffer[offset++] == 't')
		    ? CCTokenContext.SHORT : null;
	    case 'i':
		switch (buffer[offset++]) {
		case 'z': // keyword "sizeof"
		    return (len == 6
			    && buffer[offset++] == 'e'
			    && buffer[offset++] == 'o'
			    && buffer[offset++] == 'f')
			? CCTokenContext.SIZEOF : null;
		case 'g': // keyword "signed"
		    return (len == 6
			    && buffer[offset++] == 'n'
			    && buffer[offset++] == 'e'
			    && buffer[offset++] == 'd')
			? CCTokenContext.SIGNED : null;
		default:
		    return null;
		}
	    case 't':
		switch (buffer[offset++]) {
		case 'r': // keyword "struct"
		    return (len == 6
			    && buffer[offset++] == 'u'
			    && buffer[offset++] == 'c'
			    && buffer[offset++] == 't')
			? CCTokenContext.STRUCT : null;
		case 'a':
		    if (len == 6) { // keyword "static"
			return (buffer[offset++] == 't'
				&& buffer[offset++] == 'i'
				&& buffer[offset++] == 'c')
			    ? CCTokenContext.STATIC : null;
		    } else if (lang == IS_CPLUSPLUS && len == 11) { // keyword "static_cast" (C++ only)
			return (buffer[offset++] == 't'
				&& buffer[offset++] == 'i'
				&& buffer[offset++] == 'c'
				&& buffer[offset++] == '_'
				&& buffer[offset++] == 'c'
				&& buffer[offset++] == 'a'
				&& buffer[offset++] == 's'
				&& buffer[offset++] == 't')
			    ? CCTokenContext.STATIC_CAST : null;
		    } else {
			return null;
		    }
		default:
		    return null;
		}
	    case 'w': // keyword "switch"
		return (len == 6
			&& buffer[offset++] == 'i'
			&& buffer[offset++] == 't'
			&& buffer[offset++] == 'c'
			&& buffer[offset++] == 'h')
		    ? CCTokenContext.SWITCH : null;
	    default:
		return null;
	    }
	case 't':
	    if (len <= 2)
		return null;
	    switch (buffer[offset++]) {
	    case 'e': // keyword "template" (C++ only)
		return (lang == IS_CPLUSPLUS && len == 8
			&& buffer[offset++] == 'm'
			&& buffer[offset++] == 'p'
			&& buffer[offset++] == 'l'
			&& buffer[offset++] == 'a'
			&& buffer[offset++] == 't'
			&& buffer[offset++] == 'e')
		    ? CCTokenContext.TEMPLATE : null;
	    case 'h':
		switch (buffer[offset++]) {
		case 'i': // keyword "this" (C++ only)
		    return (lang == IS_CPLUSPLUS && len == 4
			    && buffer[offset++] == 's')
			? CCTokenContext.THIS : null;
		case 'r': // keyword "throw" (C++ only)
		    return (lang == IS_CPLUSPLUS && len == 5
			    && buffer[offset++] == 'o'
			    && buffer[offset++] == 'w')
			? CCTokenContext.THROW : null;
		default:
		    return null;
		}
	    case 'r':
		switch (buffer[offset++]) {
		case 'u': // keyword "true" (C++ only)
		    return (lang == IS_CPLUSPLUS && len == 4
			    && buffer[offset++] == 'e')
			? CCTokenContext.TRUE : null;
		case 'y': // keyword "try" (C++ only)
		    return (lang == IS_CPLUSPLUS && len == 3)
			? CCTokenContext.TRY : null;
		default:
		    return null;
		}
	    case 'y':
                if (len <= 5 || buffer[offset++] != 'p' || buffer[offset++] != 'e') {
                    return null;
                } else {
                    switch (buffer[offset++]) {
                        case 'd':    // keyword "typedef"                 
                            return (len == 7
                                    && buffer[offset++] == 'e'
                                    && buffer[offset++] == 'f')
                                        ? CCTokenContext.TYPEDEF : null;
                        case 'i': // keyword "typeid" (C++ only)
                            return (lang == IS_CPLUSPLUS && len == 6 && buffer[offset++] == 'd')
                                ? CCTokenContext.TYPEID : null;       
                        case 'n': // keyword "typename" (C++ only)
                            return (lang == IS_CPLUSPLUS && len == 8 
                                    && buffer[offset++] == 'a'
                                    && buffer[offset++] == 'm'
                                    && buffer[offset++] == 'e')
                                    ? CCTokenContext.TYPENAME : null;
                        case 'o': // keyword "typeof" (C only)
                            return (lang == IS_C && len == 6 && buffer[offset++] == 'f')
                                ? CCTokenContext.TYPEOF : null;      
                        default:
                            return null;                                
                    }
                }
	    default:
	      return null;
	    }
	case 'u':
	    if (len <= 4)
		return null;
	    switch (buffer[offset++]) {
	    case 's': // keyword "using" (C++ only)
		return (lang == IS_CPLUSPLUS && len == 5
			&& buffer[offset++] == 'i'
			&& buffer[offset++] == 'n'
			&& buffer[offset++] == 'g')
		    ? CCTokenContext.USING : null;
	    case 'n':
		switch (buffer[offset++]) {
		case 'i': // keyword "union"
		    return (len == 5
			    && buffer[offset++] == 'o'
			    && buffer[offset++] == 'n')
			? CCTokenContext.UNION : null;
		case 's': // keyword "unsigned"
		    return (len == 8
			    && buffer[offset++] == 'i'
			    && buffer[offset++] == 'g'
			    && buffer[offset++] == 'n'
			    && buffer[offset++] == 'e'
			    && buffer[offset++] == 'd')
			? CCTokenContext.UNSIGNED : null;
		default:
		    return null;
		}
	    default:
		return null;
	    }
	case 'v':
	    if (len <= 3)
		return null;
	    switch (buffer[offset++]) {
	    case 'i': // keyword "virtual" (C++ only)
		return (lang == IS_CPLUSPLUS && len == 7
			&& buffer[offset++] == 'r'
			&& buffer[offset++] == 't'
			&& buffer[offset++] == 'u'
			&& buffer[offset++] == 'a'
			&& buffer[offset++] == 'l')
		    ? CCTokenContext.VIRTUAL : null;
	    case 'o':
		switch (buffer[offset++]) {
		case 'i': // keyword "void"
		    return (len == 4
			    && buffer[offset++] == 'd')
			? CCTokenContext.VOID : null;
		case 'l': // keyword "volatile"
		    return (len == 8
			    && buffer[offset++] == 'a'
			    && buffer[offset++] == 't'
			    && buffer[offset++] == 'i'
			    && buffer[offset++] == 'l'
			    && buffer[offset++] == 'e')
			? CCTokenContext.VOLATILE : null;
		default:
		    return null;
		}
	    default:
		return null;
	    }
	case 'w':
	    if (len <= 4)
		return null;
	    switch (buffer[offset++]) {
	    case 'c': // keyword "wchar_t" (C++ only)
		return (lang == IS_CPLUSPLUS && len == 7
			&& buffer[offset++] == 'h'
			&& buffer[offset++] == 'a'
			&& buffer[offset++] == 'r'
			&& buffer[offset++] == '_'
			&& buffer[offset++] == 't')
		    ? CCTokenContext.WCHAR_T : null;
	    case 'h': // keyword "while"
		return (len == 5
			&& buffer[offset++] == 'i'
			&& buffer[offset++] == 'l'
			&& buffer[offset++] == 'e')
		    ? CCTokenContext.WHILE : null;
	    default:
	      return null;
	    }
            case '_':
                if (len <= 4) {
                    return null;
                }
                switch (buffer[offset++]) {
                case 'B': // keyword "_Bool" (C only)
                    return (lang == IS_C && len == 5
                            && buffer[offset++] == 'o'
                            && buffer[offset++] == 'o'
                            && buffer[offset++] == 'l')
                        ? CCTokenContext._BOOL : null;
                case 'C': // keyword "_Complex" (C only)
                    return (lang == IS_C && len == 8
                            && buffer[offset++] == 'o'
                            && buffer[offset++] == 'm'
                            && buffer[offset++] == 'p'
                            && buffer[offset++] == 'l'
                            && buffer[offset++] == 'e'
                            && buffer[offset++] == 'x')
                        ? CCTokenContext._COMPLEX : null;
                case 'I': // keyword "_Imaginary" (C only)
                    return (lang == IS_C && len == 10
                            && buffer[offset++] == 'm'
                            && buffer[offset++] == 'a'
                            && buffer[offset++] == 'g'
                            && buffer[offset++] == 'i'
                            && buffer[offset++] == 'n'
                            && buffer[offset++] == 'a'
                            && buffer[offset++] == 'r'
                            && buffer[offset++] == 'y')
                        ? CCTokenContext._IMAGINARY : null;
                }
	default:
	  return null;
	}
    }


    /* Match C preprocessor tokens. These are:
         #define name token-string
	 #define name(argument [, argument] ... ) token-string
	 #undef name
	 #include "filename"
	 #include <filename>
	 #line integer-constant "filename"
	 #if constant-expression
	 #ifdef name
	 #ifndef name
	 #elif constant-expression
	 #else
	 #endif
	 #error
	 #warning
       plus the special names
         __LINE__
	 __FILE__

       In addition, also recognize #pragma
     */

    // define, elif, else, endif, if, ifdef, ifndef, include, line, undef
    public static TokenID matchCPPKeyword(char[] buffer, int offset, int len) {
//	System.err.print("In matchCPPKeyword: ");
//	int x;
//	for (x = offset; x <offset+len; x++) {
//	    System.err.print(buffer[x]);
//	}
//	System.err.println("");
        
	if (buffer[offset] != '#') {
	    return null;
	}
	len--;
	offset++;
        
        // skip all whitespaces
        while (len > 0 && Character.isWhitespace(buffer[offset])) {
            offset++;
            len--;
        }
	TokenID defCPPToken = CCTokenContext.CPPIDENTIFIER;
        if (len > 15)
            return defCPPToken;
        if (len <= 1)
            return defCPPToken;
        switch (buffer[offset++]) {
        case 'd': // define
	    return (len == 6
		    && buffer[offset++] == 'e'
		    && buffer[offset++] == 'f'
		    && buffer[offset++] == 'i'
		    && buffer[offset++] == 'n'
		    && buffer[offset++] == 'e')
		? CCTokenContext.CPPDEFINE : defCPPToken;
	case 'e': // elif, else, endif, error
	    if (len <= 3)
		return defCPPToken;
	    switch (buffer[offset++]) {
	    case 'l': // elif, else
		switch (buffer[offset++]) {
		case 's': // else
		    return (len == 4
			    && buffer[offset++] == 'e')
			? CCTokenContext.CPPELSE : defCPPToken;
		case 'i': // endif
		    return (len == 4
			    && buffer[offset++] == 'f')
			? CCTokenContext.CPPELIF : defCPPToken;
		default:
		    return defCPPToken;
		}
	    case 'n': // endif
		return (len == 5
			&& buffer[offset++] == 'd'
			&& buffer[offset++] == 'i'
			&& buffer[offset++] == 'f')
		    ? CCTokenContext.CPPENDIF : defCPPToken;
	    case 'r': // error
		return (len == 5
			&& buffer[offset++] == 'r'
			&& buffer[offset++] == 'o'
			&& buffer[offset++] == 'r')
		    ? CCTokenContext.CPPERROR : defCPPToken;
	    default:
		return defCPPToken;
	    }
        case 'i': // if, ifdef, ifndef, include
            switch (buffer[offset++]) {
            case 'f': // if, ifdef, ifndef
		if (len == 2) {
		    return CCTokenContext.CPPIF;
		}
                switch (buffer[offset++]) {
                case 'd':
                    return (len == 5
                            && buffer[offset++] == 'e'
                            && buffer[offset++] == 'f')
                           ? CCTokenContext.CPPIFDEF : defCPPToken;
                case 'n':
                    return (len == 6
                            && buffer[offset++] == 'd'
                            && buffer[offset++] == 'e'
                            && buffer[offset++] == 'f')
                           ? CCTokenContext.CPPIFNDEF : defCPPToken;
                default:
                    return defCPPToken;
                }
            case 'n': // include
                if (len >= 7 
                            && buffer[offset++] == 'c'
                            && buffer[offset++] == 'l'
                            && buffer[offset++] == 'u'
                            && buffer[offset++] == 'd'
                            && buffer[offset++] == 'e') {
                    if (len == 7) {
                        return CCTokenContext.CPPINCLUDE;
                    } else if (len == 12
                            && buffer[offset++] == '_'
                            && buffer[offset++] == 'n'
                            && buffer[offset++] == 'e'
                            && buffer[offset++] == 'x'
                            && buffer[offset++] == 't') {
                        return CCTokenContext.CPPINCLUDE_NEXT;
                    } else {
                        return defCPPToken;
                    }
                } else {
                    return defCPPToken;
                }
            default:
                return defCPPToken;
            }
        case 'l': // line
	    if (len != 4) {
		return defCPPToken;
	    }
	    return (   buffer[offset++] == 'i'
		    && buffer[offset++] == 'n'
		    && buffer[offset++] == 'e')
		? CCTokenContext.CPPLINE : defCPPToken;
        case 'p': // pragma
	    return (len == 6
		    && buffer[offset++] == 'r'
		    && buffer[offset++] == 'a'
		    && buffer[offset++] == 'g'
		    && buffer[offset++] == 'm'
		    && buffer[offset++] == 'a')
		? CCTokenContext.CPPPRAGMA : defCPPToken;
	case 'u': // undef
	    if (len != 5)
		return defCPPToken;
	    return (   buffer[offset++] == 'n'
		    && buffer[offset++] == 'd'
		    && buffer[offset++] == 'e'
		    && buffer[offset++] == 'f')
		? CCTokenContext.CPPUNDEF : defCPPToken;
	case 'w': // warning
	    if (len != 7)
		return defCPPToken;
	    return (   buffer[offset++] == 'a'
		    && buffer[offset++] == 'r'
		    && buffer[offset++] == 'n'
		    && buffer[offset++] == 'i'
		    && buffer[offset++] == 'n'
		    && buffer[offset++] == 'g')
		? CCTokenContext.CPPWARNING : defCPPToken;
	default:
	  return defCPPToken;
	}
    }
    
    public static boolean isSpaceChar(char actChar) {
        return Character.isSpaceChar(actChar) || actChar == '\t';
    }
    
    public static boolean isLineSeparator(char actChar) {
        return actChar == '\n' || actChar == '\r';
    }
}
