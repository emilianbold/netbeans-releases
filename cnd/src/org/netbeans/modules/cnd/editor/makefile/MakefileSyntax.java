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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.editor.makefile;

import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenID;
import java.util.Stack;

/**
 *  Syntax analyzes for Makefile files.
 *  Tokens and internal states are given below.
 */

public class MakefileSyntax extends Syntax {

    //internal analyzer states
    //numbers assigned to states are not important as long as they are unique
    private static final int AFTER_COLON = 1;       // after ':'
    private static final int AFTER_DOLLAR = 2;      // after '$'
    private static final int AFTER_BSLASH = 3;      // after '\\'
    private static final int AFTER_PLUS = 4;        // after '+'
    private static final int IN_STRING = 5;              // inside string constant
    private static final int IN_STRING_AFTER_BSLASH = 6; // inside string constant
						   // after backslash
    private static final int IN_MACRO = 7;               // inside macro
    private static final int IN_MACRO_AFTER_DELIM = 8;   // inside macro  $() ${}
    private static final int IN_WHITESPACE = 9;        // inside white space
    private static final int IN_LINE_COMMENT = 10;     // inside line comment
    private static final int IN_IDENTIFIER = 11;       // inside identifier
    private static final int IN_DOT_IDENTIFIER = 12;   // inside .identifier
    private static final int IN_COLON_IDENTIFIER = 13; // inside :identifier

    private static final char LEFT_PAREN = '(';
    private static final char RIGHT_PAREN = ')';
    private static final char LEFT_CURLY = '{';
    private static final char RIGHT_CURLY = '}';


    /**
     *  Specifies if the string is defined in double quotes or single quote
     */
    private static boolean STRING_IN_DOUBLE_QUOTE = true;

    /**
     *  Specifies how many  macro names has the  current macro in it
     *  e.g. the macro $(USER-$(IDE)) is valid and nested_macro_num = 1
     */
    private static Integer BRACE_DELIM= new Integer(1);
    private static Integer PARAN_DELIM= new Integer(2);
    private Stack macroDelimStack = new Stack();

    /** constructor */
    public MakefileSyntax() {
	tokenContextPath = MakefileTokenContext.contextPath;
    }

    /**
     *  This is core function of analyzer and it returns either the token-id
     *  or null to indicate that the end of buffer was found.
     *  The function scans the active character and does one or more
     *  of the following actions:
     *  1. change internal analyzer state
     *  2. set the token-context-path and return token-id
     *  3. adjust current position to signal different end of token;
     *     the character that offset points to is not included in the token
     */
    protected TokenID parseToken() {
	char actChar;

	while (offset < stopOffset) {
	    actChar = buffer[offset];

	    switch (state) {
	    case INIT:
		switch (actChar) {
		case '#': // Makefile comments begin with a # and last to the end of line
		  state = IN_LINE_COMMENT;
		  break;

		case ':':
		  state = AFTER_COLON;
		  break;

		case '.':
		  state = IN_DOT_IDENTIFIER;
		  break;

		case '$':
		  state = AFTER_DOLLAR;
		  break;

		case '\\':
		  state = AFTER_BSLASH;
		  break;

		case '"': 
		  state = IN_STRING;
		  STRING_IN_DOUBLE_QUOTE = true;
		  break;

		case '\'':
		  state = IN_STRING;
		  STRING_IN_DOUBLE_QUOTE = false;
		  break;

		case '+':
		  state = AFTER_PLUS;
		  break;

		case '-':
		  offset++;
		  return MakefileTokenContext.RULES_MINUS;

		case '@':
		  offset++;
		  return MakefileTokenContext.RULES_AT;

		case '?':
		  offset++;
		  return MakefileTokenContext.RULES_QUESTION_MARK;

		case '!':
		  offset++;
		  return MakefileTokenContext.RULES_EXCLAMATION;

		case '%':
		  offset++;
		  return MakefileTokenContext.TARGET_PERCENT;

		case '=':
		  offset++;
		  return MakefileTokenContext.MACRO_OP_EQUALS;

		case LEFT_PAREN:
		  offset++;
		  return MakefileTokenContext.MACRO_LPAREN;

		case RIGHT_PAREN:
		  offset++;
		  return MakefileTokenContext.MACRO_RPAREN;

		case LEFT_CURLY:
		  offset++;
		  return MakefileTokenContext.MACRO_LBRACE;

		case RIGHT_CURLY:
		  offset++;
		  return MakefileTokenContext.MACRO_RBRACE;

		default:
		  // Check for whitespace
		  if (Character.isWhitespace(actChar)) {
		      state = IN_WHITESPACE;
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

		  //for the sake of the syntax-highlighting
		  //assume non-identifiers and non language specific characters
		  //are identifiers
		  offset++;
		  return MakefileTokenContext.IDENTIFIER;

		} // switch(actchar)
		break; // END INIT STATE

	    case IN_WHITESPACE:
		if (!Character.isWhitespace(actChar)) {
		    state = INIT;
		    return MakefileTokenContext.WHITESPACE;
		}
		break;

	    case IN_LINE_COMMENT:
		switch (actChar) {
		case '\n':
		    state = INIT;
		    offset++;
		    return MakefileTokenContext.LINE_COMMENT;
		} // switch IN_LINE_COMMENT
		break;

	    case AFTER_BSLASH:
		switch (actChar) {
		case '$':
		    offset++;
		    state = INIT;
		    return MakefileTokenContext.MACRO_ESCAPED_DOLLAR;

		default:
		    state = IN_IDENTIFIER;
		    offset--;  //go back and evaluate the character
		    break;
		}//switch AFTER_BSLASH
		break;

	    case AFTER_DOLLAR:
		switch (actChar) {
		case '$':
		    offset++;
		    state = INIT;
		    return MakefileTokenContext.MACRO_DOLAR_REFERENCE;

		case '*':
		    offset++;
		    state = INIT;
		    return MakefileTokenContext.MACRO_DYN_TARGET_BASENAME;

		case '<':
		    offset++;
		    state = INIT;
		    return MakefileTokenContext.MACRO_DYN_DEPENDENCY_FILENAME;

		case '@':
		    offset++;
		    state = INIT;
		    return MakefileTokenContext.MACRO_DYN_CURRENTTARGET;

		case '?':
		    offset++;
		    state = INIT;
		    return MakefileTokenContext.MACRO_DYN_DEPENDENCY_LIST;

		case '%':
		    offset++;
		    state = INIT;
		    return MakefileTokenContext.MACRO_DYN_LIBRARYNAME;

		case LEFT_PAREN:
		case LEFT_CURLY:
		    offset--;  //go back and evaluate the character
		    state = IN_MACRO_AFTER_DELIM;
		    break;

		default:
		    state = IN_MACRO;
		    break;
		} //switch AFTER_DOLLAR
		break;

	    case IN_MACRO:
		if (!(Character.isJavaIdentifierPart(actChar))) {
		    switch (actChar) {
		    case '.':   //allowable macroname characters
		    case '-':
			break;
		    default:
			state = INIT;
			return  MakefileTokenContext.MACRO_LITERAL;
		     } //switch IN_MACRO
		}
		break;

	    case IN_MACRO_AFTER_DELIM:
		if (!(Character.isJavaIdentifierPart(actChar))) {
		    switch (actChar) {
		    case '.':   //allowable macroname characters
		    case '-':
			break;

		    case LEFT_PAREN:
			macroDelimStack.push(PARAN_DELIM);
			break;

		    case LEFT_CURLY:
			macroDelimStack.push(BRACE_DELIM);
			break;

		    case RIGHT_PAREN:
		    case RIGHT_CURLY:
			Integer delim = (actChar == RIGHT_PAREN)  ? PARAN_DELIM : BRACE_DELIM;
			if (!macroDelimStack.empty())  {
			    if (((Integer)macroDelimStack.pop()) == delim) {
				if (macroDelimStack.empty())  {
				    state = INIT;
				    offset++;
				    return MakefileTokenContext.MACRO_LITERAL;
				} else {
				    break;
				}
			    }
			}

			//this statement is reached only if there is an error in macro string
			state = INIT;
			offset++;
			macroDelimStack = new Stack();
			return MakefileTokenContext.ERR_INCOMPLETE_MACRO_LITERAL;

		    default:
			if (macroDelimStack.empty()) {
			    state = INIT;
			    return MakefileTokenContext.MACRO_LITERAL;
			} else {
			    state = INIT;
			    macroDelimStack = new Stack();
                            // Fix for beta13: IZ 86737
			    //return MakefileTokenContext.ERR_INCOMPLETE_MACRO_LITERAL;
			}
		    } //switch IN_MACRO_AFTER_DELIM
		}
		break;

	    case AFTER_PLUS:
		switch (actChar) {
		case '=':
		    state = INIT;
		    offset++;
		    return MakefileTokenContext.MACRO_OP_APPEND;

		default:
		    state = INIT;
		    offset++;
		    return MakefileTokenContext.RULES_PLUS;
		} // switch AFTER_PLUS

	    case AFTER_COLON:
		switch (actChar) {
		case '=':
		    offset++;
		    state = INIT;
		    return MakefileTokenContext.MACRO_OP_CONDITIONAL;

		case ':':
		    offset++;
		    state = INIT;
		    return MakefileTokenContext.TARGET_DOUBLE_COLON;

		case 's':
		case 'S':
		    state = IN_COLON_IDENTIFIER;
		    break;

		default:
		    state = INIT;
		    return MakefileTokenContext.TARGET_COLON;
		} //switch AFTER_COLON
		break;

	    case IN_COLON_IDENTIFIER:
		if (!Character.isJavaIdentifierPart(actChar)) {
		    state = INIT;
		    TokenID tid = matchKeyword(buffer, tokenOffset, offset - tokenOffset);
		    if (tid != null) {
			return tid;
		    } else {
			//highlight the first colon and reevaluate the rest of the string since colon
			offset = tokenOffset + 1;
			return MakefileTokenContext.TARGET_COLON;
		    }
		} // switch IN_COLON_IDENTIFIER
		break;

	    case IN_DOT_IDENTIFIER:
		if (!Character.isJavaIdentifierPart(actChar)) {
		state = INIT;
		TokenID tid = matchKeyword(buffer, tokenOffset, offset - tokenOffset);
		    if (tid != null)
			return tid;
		    else {
			//highlight the first dot and reevaluate the rest of the string since dot
			offset = tokenOffset + 1;
			state = INIT;
			return MakefileTokenContext.IDENTIFIER;
		    }
		} // switch IN_DOT_IDENTIFIER
		break;

	    case IN_IDENTIFIER:
		// To find out why we're using isJAVAidentifier
		// here, grep for isJavaIdentifierStart in
		// CCSyntax.java
		if (!(Character.isJavaIdentifierPart(actChar))) {
		    state = INIT;
		    TokenID tid = matchKeyword(buffer, tokenOffset, offset - tokenOffset);
		    return (tid != null) ? tid : MakefileTokenContext.IDENTIFIER;
		}
		break;

	    case IN_STRING:
		switch (actChar) {
		case '\\':
		    state = IN_STRING_AFTER_BSLASH;
		    break;

		case '\n':
		    state = INIT;
		    offset++;
		    supposedTokenID = MakefileTokenContext.STRING_LITERAL;
		    return supposedTokenID;

		case '"':
		    if (STRING_IN_DOUBLE_QUOTE) {
			offset++;
			state = INIT;
			return MakefileTokenContext.STRING_LITERAL;
		    }
		    break;

		case '\'':
		    if (!STRING_IN_DOUBLE_QUOTE) {
			offset++;
			state = INIT;
			return MakefileTokenContext.STRING_LITERAL;
		    }
		    break;
		} //switch IN_STRING
		break;

	    case IN_STRING_AFTER_BSLASH:
		switch (actChar) {
		case '"':
		case '\'': 
		case '\\':
		    break;   //ignore the meaning of these characters

		default:
		    offset--;  //go back and evaluate the character
		    break;
		} // End switch (actChar)
		state = IN_STRING;
		break; // switch IN_STRING_AFTER_BSLASH:
	    } // end of switch(state)
	    // END STATE SWITCH
	    offset++;
	} // while(offset...)

	/*
	 *  At this stage there's no more text in the scanned buffer.
	 *  Scanner first checks whether this is completely the last
	 *  available buffer.
	 */
	if (lastBuffer) {
	    switch(state) {
	    case IN_WHITESPACE:
		state = INIT;
		return MakefileTokenContext.WHITESPACE;

	    case IN_DOT_IDENTIFIER:
	    case IN_COLON_IDENTIFIER:
	    case IN_IDENTIFIER:
		state = INIT;
		TokenID kwd = matchKeyword(buffer, tokenOffset, offset - tokenOffset);
		return (kwd != null) ? kwd : MakefileTokenContext.IDENTIFIER;

	    case IN_STRING:
	    case IN_STRING_AFTER_BSLASH:
		return MakefileTokenContext.STRING_LITERAL; // hold the state

	    case IN_MACRO:
	    case IN_MACRO_AFTER_DELIM:
		state = INIT;
		return MakefileTokenContext.MACRO_LITERAL;

	    case AFTER_BSLASH:
		state = INIT;
		return MakefileTokenContext.IDENTIFIER;

	    case AFTER_PLUS:
		state = INIT;
		return MakefileTokenContext.RULES_PLUS;

	    case AFTER_DOLLAR:
		state = INIT;
		return MakefileTokenContext.MACRO_DOLLAR;

	    case AFTER_COLON:
		state = INIT;
		return MakefileTokenContext.TARGET_COLON;

	    case IN_LINE_COMMENT:
		return MakefileTokenContext.LINE_COMMENT; // stay in line-comment state
	    } // switch (state)
	} // if (lastbuffer)

	/*
	 * At this stage there's no more text in the scanned buffer, but this buffer
	 * is not the last so the scan will continue on another buffer. The scanner
	 * tries to minimize the amount of characters that will be prescanned in the
	 * next buffer by returning the token where possible.
	 */
	switch (state) {
	case IN_WHITESPACE:
	    return MakefileTokenContext.WHITESPACE;
	}

	return null; // nothing found
    }

    public String getStateName(int stateNumber) {
	switch(stateNumber) {
	case AFTER_COLON:
	    return "AFTER_COLON"; //NOI18N

	case AFTER_DOLLAR:
	    return "AFTER_DOLLAR"; //NOI18N

	case AFTER_BSLASH:
	    return "AFTER_BSLASH"; //NOI18N

	case AFTER_PLUS:
	    return "AFTER_PLUS"; //NOI18N

	case IN_STRING:
	    return "IN_STRING"; //NOI18N

	case IN_STRING_AFTER_BSLASH:
	    return "IN_STRING_AFTER_BSLASH"; //NOI18N

	case IN_MACRO:
	    return "IN_MACRO"; //NOI18N

	case IN_MACRO_AFTER_DELIM:
	    return "IN_MACRO_AFTER_DELIM"; //NOI18N

	case IN_LINE_COMMENT:
	    return "IN_LINE_COMMENT"; //NOI18N

	case IN_IDENTIFIER:
	    return "IN_IDENTIFIER"; //NOI18N

	case IN_DOT_IDENTIFIER:
	    return "IN_DOT_IDENTIFIER"; //NOI18N

	case IN_COLON_IDENTIFIER:
	    return "IN_COLON_IDENTIFIER"; //NOI18N

	case IN_WHITESPACE:
	    return "IN_WHITESPACE"; //NOI18N

	default:
	    return super.getStateName(stateNumber);
	}
    }

    public static TokenID matchKeyword(char[] buffer, int offset, int len) {
	if (len <= 1 || len > 17) {
	    return null;
	}

	//BEGIN MOTHER SWITCH
	switch (Character.toLowerCase(buffer[offset++])) {
	//DOT
	//.DEFAULT  .DONE .FAILED .GET_POSIX .IGNORE .INIT .KEEP_STATE
	//.KEEP_STATE_FILE .MAKE_VERSION .NO_PARALLEL .PARALLEL .POSIX .PRECIOUS
	//.SCCS_GET .SCCS_GET_POSIX .SILENT .SUFFIXES .WAIT
	case '.':
	    if ((len < 5) || (len > 16)) {
		return null;
	    }

	    switch (Character.toLowerCase(buffer[offset++])) {
	    case 'd': // .DEFAULT  .DONE
		switch (Character.toLowerCase(buffer[offset++])) {
		case 'e': // .DEFAULT
		    return (len == 8
			    && Character.toLowerCase(buffer[offset++]) == 'f'
			    && Character.toLowerCase(buffer[offset++]) == 'a'
			    && Character.toLowerCase(buffer[offset++]) == 'u'
			    && Character.toLowerCase(buffer[offset++]) == 'l'
			    && Character.toLowerCase(buffer[offset++]) == 't')
			    ? MakefileTokenContext.TARGET_DEFAULT : null;

		case 'o': // .DONE
		    return (len == 5
			    && Character.toLowerCase(buffer[offset++]) == 'n'
			    && Character.toLowerCase(buffer[offset++]) == 'e')
			    ? MakefileTokenContext.TARGET_DONE : null;

		default:
		    return null;
		} // switch .d

	    case 'f': //.FAILED
		return (len == 7
			&& Character.toLowerCase(buffer[offset++]) == 'a'
			&& Character.toLowerCase(buffer[offset++]) == 'i'
			&& Character.toLowerCase(buffer[offset++]) == 'l'
			&& Character.toLowerCase(buffer[offset++]) == 'e'
			&& Character.toLowerCase(buffer[offset++]) == 'd')
			? MakefileTokenContext.TARGET_FAILED : null;

	    case 'g': //.GET_POSIX
		return (len == 10
			&& Character.toLowerCase(buffer[offset++]) == 'e'
			&& Character.toLowerCase(buffer[offset++]) == 't'
			&& Character.toLowerCase(buffer[offset++]) == '_'
			&& Character.toLowerCase(buffer[offset++]) == 'p'
			&& Character.toLowerCase(buffer[offset++]) == 'o'
			&& Character.toLowerCase(buffer[offset++]) == 's'
			&& Character.toLowerCase(buffer[offset++]) == 'i'
			&& Character.toLowerCase(buffer[offset++]) == 'x')
			? MakefileTokenContext.TARGET_GETPOSIX : null;

	    case 'i': //.IGNORE .INIT
		switch (Character.toLowerCase(buffer[offset++])) {
		case 'g': //.IGNORE
		    return (len == 7
			&& Character.toLowerCase(buffer[offset++]) == 'n'
			&& Character.toLowerCase(buffer[offset++]) == 'o'
			&& Character.toLowerCase(buffer[offset++]) == 'r'
			&& Character.toLowerCase(buffer[offset++]) == 'e')
			? MakefileTokenContext.TARGET_IGNORE : null;

		case 'n': //.INIT
		    return (len == 5
			&& Character.toLowerCase(buffer[offset++]) == 'i'
			&& Character.toLowerCase(buffer[offset++]) == 't')
			? MakefileTokenContext.TARGET_INIT : null;

		default:
		    return null;
		} // switch .i

	    case 'k': //.KEEP_STATE .KEEP_STATE_FILE
		if (len >= 11
			&& Character.toLowerCase(buffer[offset++]) == 'e'
			&& Character.toLowerCase(buffer[offset++]) == 'e'
			&& Character.toLowerCase(buffer[offset++]) == 'p'
			&& Character.toLowerCase(buffer[offset++]) == '_'
			&& Character.toLowerCase(buffer[offset++]) == 's'
			&& Character.toLowerCase(buffer[offset++]) == 't'
			&& Character.toLowerCase(buffer[offset++]) == 'a'
			&& Character.toLowerCase(buffer[offset++]) == 't'
			&& Character.toLowerCase(buffer[offset++]) == 'e') {
		    if (len == 11) {
			return MakefileTokenContext.TARGET_KEEPSTATE;
		    }

		    switch (Character.toLowerCase(buffer[offset++])) {
		    case '_': //.KEEP_STATE_FILE
			return (len == 16
				&& Character.toLowerCase(buffer[offset++]) == 'f'
				&& Character.toLowerCase(buffer[offset++]) == 'i'
				&& Character.toLowerCase(buffer[offset++]) == 'l'
				&& Character.toLowerCase(buffer[offset++]) == 'e')
				? MakefileTokenContext.TARGET_KEEPSTATEFILE : null;

		    default:
			return null;
		    } // switch .KEEP_STATE
		} else {
		    return null;
		}

	    case 'm': //.MAKE_VERSION
		return (len == 13
				&& Character.toLowerCase(buffer[offset++]) == 'a'
				&& Character.toLowerCase(buffer[offset++]) == 'k'
				&& Character.toLowerCase(buffer[offset++]) == 'e'
				&& Character.toLowerCase(buffer[offset++]) == '_'
				&& Character.toLowerCase(buffer[offset++]) == 'v'
				&& Character.toLowerCase(buffer[offset++]) == 'e'
				&& Character.toLowerCase(buffer[offset++]) == 'r'
				&& Character.toLowerCase(buffer[offset++]) == 's'
				&& Character.toLowerCase(buffer[offset++]) == 'i'
				&& Character.toLowerCase(buffer[offset++]) == 'o'
				&& Character.toLowerCase(buffer[offset++]) == 'n')
				? MakefileTokenContext.TARGET_MAKEVERSION : null;

	    case 'n': //.NO_PARALLEL
		return (len == 12
				&& Character.toLowerCase(buffer[offset++]) == 'o'
				&& Character.toLowerCase(buffer[offset++]) == '_'
				&& Character.toLowerCase(buffer[offset++]) == 'p'
				&& Character.toLowerCase(buffer[offset++]) == 'a'
				&& Character.toLowerCase(buffer[offset++]) == 'r'
				&& Character.toLowerCase(buffer[offset++]) == 'a'
				&& Character.toLowerCase(buffer[offset++]) == 'l'
				&& Character.toLowerCase(buffer[offset++]) == 'l'
				&& Character.toLowerCase(buffer[offset++]) == 'e'
				&& Character.toLowerCase(buffer[offset++]) == 'l')
				? MakefileTokenContext.TARGET_NOPARALLEL : null;

	    case 'p': // .PARALLEL .POSIX .PRECIOUS
		switch (Character.toLowerCase(buffer[offset++])) {
		case 'a': //.PARALLEL
		    return (len == 9
				&& Character.toLowerCase(buffer[offset++]) == 'r'
				&& Character.toLowerCase(buffer[offset++]) == 'a'
				&& Character.toLowerCase(buffer[offset++]) == 'l'
				&& Character.toLowerCase(buffer[offset++]) == 'l'
				&& Character.toLowerCase(buffer[offset++]) == 'e'
				&& Character.toLowerCase(buffer[offset++]) == 'l')
				? MakefileTokenContext.TARGET_PARALLEL : null;

		case 'o': //.POSIX
		    return (len == 6
				&& Character.toLowerCase(buffer[offset++]) == 's'
				&& Character.toLowerCase(buffer[offset++]) == 'i'
				&& Character.toLowerCase(buffer[offset++]) == 'x')
				? MakefileTokenContext.TARGET_POSIX : null;

		case 'r': //.PRECIOUS
		    return (len == 9
				&& Character.toLowerCase(buffer[offset++]) == 'e'
				&& Character.toLowerCase(buffer[offset++]) == 'c'
				&& Character.toLowerCase(buffer[offset++]) == 'i'
				&& Character.toLowerCase(buffer[offset++]) == 'o'
				&& Character.toLowerCase(buffer[offset++]) == 'u'
				&& Character.toLowerCase(buffer[offset++]) == 's')
				? MakefileTokenContext.TARGET_PRECIOUS : null;

		default:
		    return null;
		} // switch .p

	    case 's': // .SCCS_GET .SCCS_GET_POSIX .SILENT .SUFFIXES
		switch (Character.toLowerCase(buffer[offset++])) {
		case 'c': //.SCCS_GET .SCCS_GET_POSIX
		    if (len >= 9
				&& Character.toLowerCase(buffer[offset++]) == 'c'
				&& Character.toLowerCase(buffer[offset++]) == 's'
				&& Character.toLowerCase(buffer[offset++]) == '_'
				&& Character.toLowerCase(buffer[offset++]) == 'g'
				&& Character.toLowerCase(buffer[offset++]) == 'e'
				&& Character.toLowerCase(buffer[offset++]) == 't') {
			if (len == 9) {
			    return MakefileTokenContext.TARGET_SCCSGET;
			}

			switch (Character.toLowerCase(buffer[offset++])) {
			case '_': //.SCCS_GET_POSIX
			    return (len == 15
				&& Character.toLowerCase(buffer[offset++]) == 'p'
				&& Character.toLowerCase(buffer[offset++]) == 'o'
				&& Character.toLowerCase(buffer[offset++]) == 's'
				&& Character.toLowerCase(buffer[offset++]) == 'i'
				&& Character.toLowerCase(buffer[offset++]) == 'x')
				? MakefileTokenContext.TARGET_SCCSGETPOSIX : null;

			default:
			    return null;
			} // switch .SCCS_GET
		    } else {
			return null;
		    }

		case 'i': //.SILENT
		    return (len == 7
				&& Character.toLowerCase(buffer[offset++]) == 'l'
				&& Character.toLowerCase(buffer[offset++]) == 'e'
				&& Character.toLowerCase(buffer[offset++]) == 'n'
				&& Character.toLowerCase(buffer[offset++]) == 't')
				? MakefileTokenContext.TARGET_SILENT : null;

		case 'u': //.SUFFIXES
		    return (len == 9
				&& Character.toLowerCase(buffer[offset++]) == 'f'
				&& Character.toLowerCase(buffer[offset++]) == 'f'
				&& Character.toLowerCase(buffer[offset++]) == 'i'
				&& Character.toLowerCase(buffer[offset++]) == 'x'
				&& Character.toLowerCase(buffer[offset++]) == 'e'
				&& Character.toLowerCase(buffer[offset++]) == 's')
				? MakefileTokenContext.TARGET_SUFFIXES : null;

		default:
		    return null;
		}//switch .s

	    case 'w': //.WAIT
		return (len == 5
			    && Character.toLowerCase(buffer[offset++]) == 'a'
			    && Character.toLowerCase(buffer[offset++]) == 'i'
			    && Character.toLowerCase(buffer[offset++]) == 't')
			    ? MakefileTokenContext.TARGET_WAIT : null;

	    default:
		return null;
	    } // switch dot

	case ':':   // ":" and ":sh"
	    return (len == 3
		    && Character.toLowerCase(buffer[offset++]) == 's'
		    && Character.toLowerCase(buffer[offset++]) == 'h')
		    ? MakefileTokenContext.MACRO_COMMAND_SUBSTITUTE : null;

	case 'i':   // "include"
	    return (len == 7
		    && Character.toLowerCase(buffer[offset++]) == 'n'
		    && Character.toLowerCase(buffer[offset++]) == 'c'
		    && Character.toLowerCase(buffer[offset++]) == 'l'
		    && Character.toLowerCase(buffer[offset++]) == 'u'
		    && Character.toLowerCase(buffer[offset++]) == 'd'
		    && Character.toLowerCase(buffer[offset++]) == 'e')
		    ? MakefileTokenContext.GLOBAL_INCLUDE : null;

	default:
	    return null;
	} // END MOTHER SWITCH
    } // matchKeyword
}
