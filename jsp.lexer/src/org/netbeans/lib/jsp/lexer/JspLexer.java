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

package org.netbeans.lib.jsp.lexer;

import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Syntax class for JSP tags.
 *
 * @author Petr Jiricka
 * @author Marek Fukala
 *
 * @version 1.00
 */

public class JspLexer implements Lexer<JspTokenId> {
    
    private static final int EOF = LexerInput.EOF;
    
    private LexerInput input;
    
    private TokenFactory<JspTokenId> tokenFactory;
    
    public Object state() {
        return state + before_el_state * 1000;
    }
    
    //main internal lexer state
    private int state = INIT;
    
    //secondary internal state for EL expressions in JSP
    //is it used to eliminate a number of lexer states when EL is found - 
    //we have 8 states just in attribute value so I would have to copy the EL 
    //recognition code eight-times.
    private int before_el_state = INIT; 
    
    // Internal analyzer states
    // general
    private static final int INIT                =  0;  // initial lexer state = content language
    private static final int ISI_ERROR           =  1; // when the fragment does not start with <
    private static final int ISA_LT              =  2; // after '<' char
    // tags and directives
    private static final int ISI_TAGNAME         =  3; // inside JSP tag name
    private static final int ISI_DIRNAME         =  4; // inside JSP directive name
    private static final int ISP_TAG             =  5; // after JSP tag name
    private static final int ISP_DIR             =  6; // after JSP directive name
    private static final int ISI_TAG_I_WS        =  7; // inside JSP tag after whitespace
    private static final int ISI_DIR_I_WS        =  8; // inside JSP directive after whitespace
    private static final int ISI_ENDTAG          =  9; // inside end JSP tag
    private static final int ISI_TAG_ATTR        = 10; // inside tag attribute
    private static final int ISI_DIR_ATTR        = 11; // inside directive attribute
    private static final int ISP_TAG_EQ          = 12; // just after '=' in tag
    private static final int ISP_DIR_EQ          = 13; // just after '=' in directive
    private static final int ISI_TAG_STRING      = 14; // inside string (value - "") in tag
    private static final int ISI_DIR_STRING      = 15; // inside string (value - "") in directive
    private static final int ISI_TAG_STRING_B    = 16; // inside string (value - "") after backslash in tag
    private static final int ISI_DIR_STRING_B    = 17; // inside string (value - "") after backslash in directive
    private static final int ISI_TAG_STRING2     = 18; // inside string (value - '') in tag
    private static final int ISI_DIR_STRING2     = 19; // inside string (value - '') in directive
    private static final int ISI_TAG_STRING2_B   = 20; // inside string (value - '') after backslash in tag
    private static final int ISI_DIR_STRING2_B   = 21; // inside string (value - '') after backslash in directive
    private static final int ISA_ENDSLASH        = 22; // after ending '/' in JSP tag
    private static final int ISA_ENDPC           = 23; // after ending '%' in JSP directive
    // comments (+directives)
    private static final int ISA_LT_PC           = 24; // after '<%' - comment or directive or scriptlet
    private static final int ISI_JSP_COMMENT     = 25; // after <%-
    
    private static final int ISI_JSP_COMMENT_M   = 26; // inside JSP comment after -
    private static final int ISI_JSP_COMMENT_MM  = 27; // inside JSP comment after --
    private static final int ISI_JSP_COMMENT_MMP = 28; // inside JSP comment after --%
    // end state
//    static final int ISA_END_JSP                 = 29; // JSP fragment has finished and control
    // should be returned to master syntax
    // more errors
    private static final int ISI_TAG_ERROR       = 30; // error in tag, can be cleared by > or \n
    private static final int ISI_DIR_ERROR       = 31; // error in directive, can be cleared by %>, \n, \t or space
    private static final int ISI_DIR_ERROR_P     = 32; // error in directive after %, can be cleared by > or \n
    
    private static final int ISA_LT_PC_AT        = 33; // after '<%@' (directive)
    private static final int ISA_LT_SLASH        = 34; // after '</' sequence
    private static final int ISA_LT_PC_DASH      = 35; // after <%- ;not comment yet
    
    private static final int ISI_SCRIPTLET       = 36; // inside java scriptlet/declaration/expression
    private static final int ISP_SCRIPTLET_PC   = 37; // just after % in scriptlet
    
    //expression language
    
    //EL in content language
    private static final int ISA_EL_DELIM        = 38; //after $ or # in content language
    private static final int ISI_EL              = 39; //expression language in content (after ${ or #{ )
    
    public JspLexer(LexerRestartInfo<JspTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        if (info.state() == null) {
            this.state = INIT;
        } else {
            int encoded = ((Integer) info.state()).intValue();
            before_el_state = encoded / 1000;
            state = encoded % 1000;
        }
    }
    
    public boolean isIdentifierPart(char ch) {
        return Character.isJavaIdentifierPart(ch);
    }
    
    private Token<JspTokenId> token(JspTokenId id) {
        System.out.print("JSP token(" + id + "; '" + input.readText().toString() + "')");
        if(input.readLength() == 1) {
            System.out.println("Error - token length is zero!; state = " + state);
        }
        Token<JspTokenId> t = tokenFactory.createToken(id);
        System.out.println(t.id() + "; " + t.length());
        return t;
    }
    
    /** Determines whether a given string is a JSP tag. */
    protected boolean isJspTag(String tagName) {
        boolean canBeJsp = tagName.startsWith("jsp:");  // NOI18N
        //TODO handle custom tags from JSP parser here
        return canBeJsp;
    }
    
    /** Looks ahead into the character buffer and checks if a jsp tag name follows. */
    private boolean followsJspTag() {
        int actChar;
        int prev_read = input.readLength(); //remember the size of the read sequence
        int read = 0;
        while(true) {
            actChar = input.read();
            read++;
            if(!(Character.isLetter(actChar) ||
                    Character.isDigit(actChar) ||
                    (actChar == '_') ||
                    (actChar == '-') ||
                    (actChar == ':') ||
                    (actChar == '.')) ||
                    (actChar == EOF)) { // EOL or not alpha
                //end of tagname
                String tagName = input.readText().toString().substring(prev_read);
                input.backup(read); //put the lookahead text back to the buffer
                return isJspTag(tagName);
            }
        }
    }
    
    public Token<JspTokenId> nextToken() {
        int actChar;
        while (true) {
            actChar = input.read();
            
            if (actChar == EOF) {
                if(input.readLengthEOF() == 1) {
                    return null; //just EOL is read
                } else {
                    //there is something else in the buffer except EOL
                    //we will return last token now
                    input.backup(1); //backup the EOL, we will return null in next nextToken() call
                    break;
                }
            }
            
            switch (state) {
                case INIT:
                    switch (actChar) {
//                        case '\n':
//                            return token(JspTokenId.EOL);
                        case '<':
                            state = ISA_LT;
                            break;
//                        default:
//                            state = ISI_ERROR;
//                            break;
                        case '$':
                        case '#': //maybe expression language
                            before_el_state = state; //remember main state
                            state = ISA_EL_DELIM; 
                            break;
                    }
                    break;
                    
                case ISA_EL_DELIM:
                    switch(actChar) {
                        case '{':
                            if(input.readLength() > 2) {
                                //we have something read except the '${' or '#{' => it's content language
                                input.backup(2); //backup the '$/#{'
                                state = before_el_state; //we will read the '$/#{' again
                                before_el_state = INIT;
                                return token(JspTokenId.TEXT); //return the content language token
                            }
                            state = ISI_EL;
                            break;
                        default:
                            state = before_el_state;
                            before_el_state = INIT;
                    }
                    break;
                    
                case ISI_EL:
                    if(actChar == '}') {
                        //return EL token
                        state = before_el_state;
                        before_el_state = INIT;
                        return token(JspTokenId.EL);
                    }
                    //stay in EL
                    break;
                    
                case ISA_LT:
                    if (Character.isLetter(actChar) ||
                            (actChar == '_')
                            ) { // possible tag begining
                        input.backup(1); //backup the read letter
                        if(followsJspTag()) { //test if a jsp tag follows
                            if(input.readLength() > 1) {
                                //we have something read except the '<' => it's content language
                                input.backup(1); //backup the '<'
                                state = INIT; //we will read the '<' again
                                return token(JspTokenId.TEXT); //return the content language token
                            }
                            state = ISI_TAGNAME;
                            break;
                        } else {
                            //just a content language
                            state = INIT;
                            break;
                        }
//                        input.backup(1);
//                        return token(JspTokenId.SYMBOL);
                    }
                    
                    switch (actChar) {
                        case '/':
                            state = ISA_LT_SLASH;
                            break;
//                        case '\n':
//                            state = ISI_TAG_ERROR;
//                            input.backup(1);
//                            return token(JspTokenId.SYMBOL);
                        case '%':
                            state = ISA_LT_PC;
                            break;
                        default:
                            state = INIT; //just content
//                            state = ISI_TAG_ERROR;
//                            break;
                    }
                    break;
                    
                case ISA_LT_SLASH:
                    if (Character.isLetter(actChar) ||
                            (actChar == '_')) {
                        //possible end tag beginning
                        input.backup(1); //backup the first letter
                        if(followsJspTag()) {
                            if(input.readLength() > 2) {
                                //we have something read except the '</' symbol
                                input.backup(2);
                                state = INIT;
                                return token(JspTokenId.TEXT);
                            } else {
                                state = ISI_ENDTAG;
                            }
                            break;
                        } else {
                            //just a content language
                            state = INIT;
                            break;
                        }
                    }
                    
                    //everyting alse is an error
                    state = ISI_TAG_ERROR;
                    break;
                    
                case ISI_TAGNAME:
                case ISI_DIRNAME:
                    
                    if (!(Character.isLetter(actChar) ||
                            Character.isDigit(actChar) ||
                            (actChar == '_') ||
                            (actChar == '-') ||
                            (actChar == ':') ||
                            (actChar == '.'))) { // not alpha
                        switch(actChar) {
                            case '<':
                                state = INIT;
                                input.backup(1);
                                break;
                            case '/':
                                input.backup(1);
                                state = ((state == ISI_TAGNAME) ? ISP_TAG : ISP_DIR);
                                break;
                            case '>':
                                state = INIT;
                                break;
                            case ' ':
                                input.backup(1);
                                state = ((state == ISI_TAGNAME) ? ISP_TAG : ISP_DIR);
                                break;
                            default:
                                state = ((state == ISI_TAGNAME) ? ISP_TAG : ISP_DIR);
                        }
                        return token(JspTokenId.TAG);
                    }
                    break;
                    
                case ISP_TAG:
                case ISP_DIR:
                    if (Character.isLetter(actChar) ||
                            (actChar == '_')
                            ) {
                        state = ((state == ISP_TAG) ? ISI_TAG_ATTR : ISI_DIR_ATTR);
                        break;
                    }
                    switch (actChar) {
                        case '\n':
//                            if (input.readLength() == 1) { // no char
                            return token(JspTokenId.EOL);
//                            } else { // return string first
//                                input.backup(1);
//                                return decide_jsp_tag_token();
//                            }
                        case '>': // for tags
                            if (state == ISP_TAG) {
//                                if (input.readLength() == 1) {  // no char
//                                    state = ISA_END_JSP;
                                state = INIT;
                                return token(JspTokenId.SYMBOL);
//                                } else { // return string first
//                                    input.backup(1);
//                                    return decide_jsp_tag_token();
//                                }
                            } else { // directive
                                //state = ISI_DIR_ERROR;
                                //commented out to minimize errors during the process of writing directives
                                break;
                            }
                        case '/': // for tags
                            if (state == ISP_TAG) {
//                                if (input.readLength() == 1) {  // no char
                                state = ISA_ENDSLASH;
                                break;
//                                } else { // return string first
//                                    input.backup(1);
//                                    return decide_jsp_tag_token();
//                                }
                            } else { // directive
                                //state = ISI_DIR_ERROR;
                                //commented out to minimize errors during the process of writing directives
                                break;
                            }
                        case '%': // for directives
                            if (state == ISP_DIR) {
//                                if (input.readLength() == 1) {  // no char
                                state = ISA_ENDPC;
                                break;
//                                } else { // return string first
//                                    input.backup(1);
//                                    return decide_jsp_tag_token();
//                                }
                            } else { // tag
                                state = ISI_TAG_ERROR;
                                break;
                            }
                        case '=':
                            state = ((state == ISP_TAG) ? ISP_TAG_EQ : ISP_DIR_EQ);
                            return token(JspTokenId.SYMBOL);
                        case ' ':
                        case '\t':
                            state = ((state == ISP_TAG) ? ISI_TAG_I_WS : ISI_DIR_I_WS);
                            break;
                        case '<': // assume that this is the start of the next tag
//                            state=ISA_END_JSP;
                            state = INIT;
                            input.backup(1);
                            return token(JspTokenId.TAG);
                        default: //numbers or illegal symbols
                            state = ((state == ISP_TAG) ? ISI_TAG_ERROR : ISI_DIR_ERROR);
                            break;
                    }
                    break;
                    
                case ISI_TAG_I_WS:
                case ISI_DIR_I_WS:
                    switch (actChar) {
                        case ' ':
                        case '\t':
                            break;
                        case '<': //start of the next tag
//                            state = ISA_END_JSP;
                            state = INIT;
                            input.backup(1);
                            return token(JspTokenId.TAG);
                        default:
                            state = ((state == ISI_TAG_I_WS) ? ISP_TAG : ISP_DIR);
                            input.backup(1);
                            return token(JspTokenId.WHITESPACE);
                    }
                    break;
                    
                case ISI_ENDTAG:
                    if (!(Character.isLetter(actChar) ||
                            Character.isDigit(actChar) ||
                            (actChar == '_') ||
                            (actChar == '-') ||
                            (actChar == ':'))
                            ) { // not alpha
                        state = ISP_TAG;
                        input.backup(1);
                        return token(JspTokenId.TAG);
                    }
                    break;
                    
                case ISI_TAG_ATTR:
                case ISI_DIR_ATTR:
                    if (!(Character.isLetter(actChar) ||
                            Character.isDigit(actChar) ||
                            (actChar == '_') ||
                            (actChar == ':') ||
                            (actChar == '-'))
                            ) { // not alpha or '-' (http-equiv)
                        state = ((state == ISI_TAG_ATTR) ? ISP_TAG : ISP_DIR);
                        input.backup(1);
                        return token(JspTokenId.ATTRIBUTE);
                    }
                    break;
                    
                case ISP_TAG_EQ:
                case ISP_DIR_EQ:
                    switch (actChar) {
                        case '\n':
//                            if (input.readLength() == 1) { // no char
                            return token(JspTokenId.EOL);
//                            } else { // return string first
//                                input.backup(1);
//                                return token(JspTokenId.ATTR_VALUE);
//                            }
                        case '"':
                            state = ((state == ISP_TAG_EQ) ? ISI_TAG_STRING : ISI_DIR_STRING);
                            break;
                        case '\'':
                            state = ((state == ISP_TAG_EQ) ? ISI_TAG_STRING2 : ISI_DIR_STRING2);
                            break;
                        case ' ':
                        case '\t':
                            // don't change the state
                            break;
                        default:
                            state = ((state == ISP_TAG_EQ) ? ISP_TAG : ISP_DIR);
                            input.backup(1);
                            //return token(JspTokenId.ATTR_VALUE);
                            break;
                    }
                    break;
                    
                case ISI_TAG_STRING:
                case ISI_DIR_STRING:
                case ISI_TAG_STRING2:
                case ISI_DIR_STRING2:
                    if ((actChar == '"') && ((state == ISI_TAG_STRING) || (state == ISI_DIR_STRING))) {
                        state = ((state == ISI_TAG_STRING) ? ISP_TAG : ISP_DIR);
                        return token(JspTokenId.ATTR_VALUE);
                    }
                    
                    if ((actChar == '\'') && ((state == ISI_TAG_STRING2) || (state == ISI_DIR_STRING2))) {
                        state = ((state == ISI_TAG_STRING2) ? ISP_TAG : ISP_DIR);
                        return token(JspTokenId.ATTR_VALUE);
                    }
                    
                    switch (actChar) {
                        case '\\':
                            switch (state) {
                                case ISI_TAG_STRING:
                                    state = ISI_TAG_STRING_B;
                                    break;
                                case ISI_DIR_STRING:
                                    state = ISI_DIR_STRING_B;
                                    break;
                                case ISI_TAG_STRING2:
                                    state = ISI_TAG_STRING2_B;
                                    break;
                                case ISI_DIR_STRING2:
                                    state = ISI_DIR_STRING2_B;
                                    break;
                            }
                            break;
                        case '\n':
//                            if (input.readLength() == 1) { // no char
                            return token(JspTokenId.EOL);
//
//                            } else { // return string first
//                                input.backup(1);
//                                return token(JspTokenId.ATTR_VALUE);
//                            }
                        case '$':
                        case '#':
                            before_el_state = state; //remember main state
                            state = ISA_EL_DELIM;
                            break;
                            
                        default:
                            //stay in ISI_TAG_STRING/2;
                    }
                    break;
                    
                case ISI_TAG_STRING_B:
                case ISI_DIR_STRING_B:
                case ISI_TAG_STRING2_B:
                case ISI_DIR_STRING2_B:
                    switch (actChar) {
                        case '"':
                        case '\'':
                        case '\\':
                            break;
                        default:
                            input.backup(1);
                            break;
                    }
                    switch (state) {
                        case ISI_TAG_STRING_B:
                            state = ISI_TAG_STRING;
                            break;
                        case ISI_DIR_STRING_B:
                            state = ISI_DIR_STRING;
                            break;
                        case ISI_TAG_STRING2_B:
                            state = ISI_TAG_STRING2;
                            break;
                        case ISI_DIR_STRING2_B:
                            state = ISI_DIR_STRING2;
                            break;
                    }
                    break;
                    
                case ISA_ENDSLASH:
                    switch (actChar) {
                        case '>':
//                            state = ISA_END_JSP;
                            state = INIT;
                            return token(JspTokenId.SYMBOL);
                        case '\n':
                            state = ISI_TAG_ERROR;
                            input.backup(1);
                            return token(JspTokenId.SYMBOL);
                        default:
                            state = ISP_TAG;
                            input.backup(1);
                            return token(JspTokenId.SYMBOL);
                    }
                    //break; not reached
                    
                case ISA_ENDPC:
                    switch (actChar) {
                        case '>':
//                            state = ISA_END_JSP;
                            state = INIT;
                            return token(JspTokenId.SYMBOL);
                        case '\n':
                            state = ISI_DIR_ERROR;
                            input.backup(1);
                            return token(JspTokenId.SYMBOL);
                        default:
                            state = ISP_DIR;
                            input.backup(1);
                            return token(JspTokenId.SYMBOL);
                    }
                    //break; not reached
                    
                case ISA_LT_PC:
                    switch (actChar) {
                        case '@':
                            if(input.readLength() == 3) {
                                // just <%@ read
                                state = ISA_LT_PC_AT;
                                return token(JspTokenId.SYMBOL);
                            } else {
                                //jsp symbol, but we also have content language in the buffer
                                input.backup(3); //backup <%@
                                state = INIT;
                                return token(JspTokenId.TEXT); //return CL token
                            }
                        case '-': //may be JSP comment
                            state = ISA_LT_PC_DASH;
                            break;
                        case '!': // java declaration
                        case '=': // java expression
                            if(input.readLength() == 3) {
                                // just <%! or <%= read
                                state = ISI_SCRIPTLET;
                                return token(JspTokenId.SYMBOL2);
                            } else {
                                //jsp symbol, but we also have content language in the buffer
                                input.backup(3); //backup <%! or <%=
                                state = INIT;
                                return token(JspTokenId.TEXT); //return CL token
                            }
                        default:  //java scriptlet delimiter '<%'
                            if(input.readLength() == 3) {
                                // just <% + something != [-,!,=,@] read
                                state = ISI_SCRIPTLET;
                                input.backup(1); //backup the third character, it is a part of the java scriptlet
                                return token(JspTokenId.SYMBOL2);
                            } else {
                                //jsp symbol, but we also have content language in the buffer
                                input.backup(3); //backup <%@
                                state = INIT;
                                return token(JspTokenId.TEXT); //return CL token
                            }
                    }
                    break;
                    
                case ISI_SCRIPTLET:
                    switch(actChar) {
                        case '%':
                            state = ISP_SCRIPTLET_PC;
                            break;
                    }
                    break;
                    
                case ISP_SCRIPTLET_PC:
                    switch(actChar) {
                        case '>':
                            if(input.readLength() == 2) {
                                //just the '%>' symbol read
                                state = INIT;
                                return token(JspTokenId.SYMBOL2);
                            } else {
                                //return the scriptlet content
                                input.backup(2); // backup '%>' we will read JUST them again
                                state = ISI_SCRIPTLET;
                                return token(JspTokenId.SCRIPTLET);
                            }
                        default:
                            state = ISI_SCRIPTLET;
                            break;
                    }
                    break;
                    
                case ISA_LT_PC_DASH:
                    switch(actChar) {
                        case '-':
                            if(input.readLength() == 4) {
                                //just the '<%--' symbol read
                                state = ISI_JSP_COMMENT;
                            } else {
                                //return the scriptlet content
                                input.backup(4); // backup '<%--', we will read it again
                                state = INIT;
                                return token(JspTokenId.TEXT);
                            }
                            break;
                        default:
//                            state = ISA_END_JSP;
                            state = INIT; //XXX how to handle content language?
                            return token(JspTokenId.TEXT); //marek: should I token here????
                    }
                    
                    // JSP states
                case ISI_JSP_COMMENT:
                    switch (actChar) {
                        case '\n':
                            if (input.readLength() == 1) { // no char
                                return token(JspTokenId.EOL);
                            } else { // return block comment first
                                input.backup(1);
                                return token(JspTokenId.COMMENT);
                            }
                        case '-':
                            state = ISI_JSP_COMMENT_M;
                            break;
                    }
                    break;
                    
                case ISI_JSP_COMMENT_M:
                    switch (actChar) {
                        case '\n':
                            state = ISI_JSP_COMMENT;
                            if (input.readLength() == 1) { // no char
                                return token(JspTokenId.EOL);
                            } else { // return block comment first
                                input.backup(1);
                                return token(JspTokenId.COMMENT);
                            }
                        case '-':
                            state = ISI_JSP_COMMENT_MM;
                            break;
                        default:
                            state = ISI_JSP_COMMENT;
                            break;
                    }
                    break;
                    
                case ISI_JSP_COMMENT_MM:
                    switch (actChar) {
                        case '\n':
                            state = ISI_JSP_COMMENT;
                            if (input.readLength() == 1) { // no char
                                return token(JspTokenId.EOL);
                            } else { // return block comment first
                                input.backup(1);
                                return token(JspTokenId.COMMENT);
                            }
                        case '%':
                            state = ISI_JSP_COMMENT_MMP;
                            break;
                        case '-':
                            state = ISI_JSP_COMMENT_MM;
                            break;
                        default:
                            state = ISI_JSP_COMMENT;
                            break;
                    }
                    break;
                    
                case ISI_JSP_COMMENT_MMP:
                    switch (actChar) {
                        case '\n':
                            state = ISI_JSP_COMMENT;
                            if (input.readLength() == 1) { // no char
                                return token(JspTokenId.EOL);
                            } else { // return block comment first
                                input.backup(1);
                                return token(JspTokenId.COMMENT);
                            }
                        case '>':
//                            state = ISA_END_JSP;
                            state = INIT;
                            return token(JspTokenId.COMMENT);
                        default:
                            state = ISI_JSP_COMMENT;
                            break;
                    }
                    break;
                    
                case ISI_ERROR:
                    switch (actChar) {
                        case '\n':
                            state = INIT;
                            input.backup(1);
                            return token(JspTokenId.ERROR);
                        case '<':
                            state = ISA_LT;
                            input.backup(1);
                            return token(JspTokenId.ERROR);
                    }
                    break;
                    
                case ISI_TAG_ERROR:
                    switch (actChar) {
                        case '\n':
                            if (input.readLength() == 1) { // no char
                                state = ISI_TAG_I_WS;
                                return token(JspTokenId.EOL);
                            } else { // return error first
//                                input.backup(1);
                                return token(JspTokenId.ERROR);
                            }
                        case '>':
                        case ' ':
                        case '\t':
                            state = ISP_TAG;
                            input.backup(1);
                            return token(JspTokenId.ERROR);
                    }
                    break;
                    
                case ISI_DIR_ERROR:
                    switch (actChar) {
                        case '\n':
                            if (input.readLength() == 1) { // no char
                                state = ISI_DIR_I_WS;
                                return token(JspTokenId.EOL);
                            } else { // return error first
                                input.backup(1);
                                return token(JspTokenId.ERROR);
                            }
                        case '%':
                            state = ISI_TAGNAME;
                            input.backup(1);
                            return token(JspTokenId.ERROR);
                        case '\t':
                        case ' ':
                            state = ISI_DIR_I_WS;
                            input.backup(1);
                            return token(JspTokenId.ERROR);
                    }
                    break;
                    
                case ISI_DIR_ERROR_P:
                    switch (actChar) {
                        case '\n':
                            if (input.readLength() == 1) { // no char
                                state = ISI_DIR_I_WS;
                                return token(JspTokenId.EOL);
                            } else { // return error first
                                input.backup(1);
                                return token(JspTokenId.ERROR);
                            }
                        case '>':
                            input.backup(2);
                            state = ISI_DIR_I_WS;
                            return token(JspTokenId.ERROR);
                    }
                    break;
                    
//                case ISA_END_JSP:
//                    if (input.readLength() == 1) {
//                        offset++;
//                        return JspTokenId.AFTER_UNEXPECTED_LT;
//                    }
//                    else {
//                        return JspTokenId.TEXT;
//                    }
//                    //break;
                    
                    // added states
                case ISA_LT_PC_AT:
                    if (Character.isLetter(actChar) ||
                            (actChar == '_')
                            ) { // the directive starts
                        state = ISI_DIRNAME;
//                        marek: why to create an empty tag token????
//                        input.backup(1);
//                        return decide_jsp_tag_token();
                    }
                    
                    switch (actChar) {
                        case '\n':
                            if (input.readLength() == 1) { // no char
                                return token(JspTokenId.EOL);
                            } else {
                                input.backup(1);
                                return token(JspTokenId.TAG);
                            }
                    }
                    break;
                    
            }
            
        }
        
        // At this stage there's no more text in the scanned buffer.
        // Scanner first checks whether this is completely the last
        // available buffer.
        
        switch(state) {
            case INIT:
                if (input.readLength() == 0) {
                    return null;
                } else {
                    return token(JspTokenId.TEXT);
                }
            case ISI_ERROR:
            case ISI_TAG_ERROR:
                state = INIT;
                return token(JspTokenId.ERROR);
            case ISI_DIR_ERROR:
            case ISI_DIR_ERROR_P:
                state = INIT;
                return token(JspTokenId.ERROR);
            case ISA_LT:
            case ISA_LT_SLASH:
            case ISA_ENDSLASH:
            case ISP_TAG_EQ:
                state = INIT;
                return token(JspTokenId.SYMBOL);
            case ISA_LT_PC:
            case ISA_LT_PC_DASH:
            case ISA_ENDPC:
            case ISP_DIR_EQ:
                state = INIT;
                return token(JspTokenId.SYMBOL);
            case ISI_TAGNAME:
            case ISI_ENDTAG:
                state = INIT;
                return token(JspTokenId.TAG);
            case ISI_DIRNAME:
                state = INIT;
                return token(JspTokenId.TAG);
            case ISP_TAG:
            case ISI_TAG_I_WS:
                state = INIT;
                return token(JspTokenId.TAG);
            case ISP_DIR:
            case ISI_DIR_I_WS:
            case ISA_LT_PC_AT:
                state = INIT;
                return token(JspTokenId.TAG);
            case ISI_TAG_ATTR:
                state = INIT;
                return token(JspTokenId.ATTRIBUTE);
            case ISI_DIR_ATTR:
                state = INIT;
                return token(JspTokenId.ATTRIBUTE);
            case ISI_TAG_STRING:
            case ISI_TAG_STRING_B:
            case ISI_TAG_STRING2:
            case ISI_TAG_STRING2_B:
                state = INIT;
                return token(JspTokenId.ATTR_VALUE);
            case ISI_DIR_STRING:
            case ISI_DIR_STRING_B:
            case ISI_DIR_STRING2:
            case ISI_DIR_STRING2_B:
                state = INIT;
                return token(JspTokenId.ATTR_VALUE);
            case ISI_JSP_COMMENT:
            case ISI_JSP_COMMENT_M:
            case ISI_JSP_COMMENT_MM:
            case ISI_JSP_COMMENT_MMP:
                state = INIT;
                return token(JspTokenId.COMMENT);
            case ISA_EL_DELIM:
                state = INIT;
                return token(JspTokenId.TEXT);
            case ISI_EL:
                state = INIT;
                return token(JspTokenId.EL);
            default:
                System.out.println("JSPLexer - unhandled state : " + state);   // NOI18N
        }
        
        return null;
        
    }
    
}

