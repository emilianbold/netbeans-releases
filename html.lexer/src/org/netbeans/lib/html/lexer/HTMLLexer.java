/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.html.lexer;

import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Lexical analyzer for HTML. Based on original HTML lexer from html/editor module.
 *
 * @author Petr Nejedly
 * @author Miloslav Metelka
 * @author Jan Lahoda
 * @author Marek Fukala
 * @version 1.00
 */

public class HTMLLexer implements Lexer<HTMLTokenId> {
    
    private static final int EOF = LexerInput.EOF;
    
    private LexerInput input;
    
    private TokenFactory<HTMLTokenId> tokenFactory;
    
    public Object state() {
        return subState * 1000000 + state * 1000 + scriptState;
    }
    
    
    /** Internal state of the lexical analyzer before entering subanalyzer of
     * character references. It is initially set to INIT, but before first usage,
     * this will be overwritten with state, which originated transition to
     * charref subanalyzer.
     */
    private int subState = INIT;
    private int state    = INIT;
    
    /** indicated whether we are in a script */
    private int scriptState = INIT;
    
    // internal 'in script' state. 'scriptState' internal state is set to it when the
    // analyzer goes into a script tag body
    private static final int ISI_SCRIPT = 1;
    
    // Internal states
    private static final int INIT = 0;
    private static final int ISI_TEXT = 1;    // Plain text between tags
    private static final int ISI_ERROR = 2;   // Syntax error in HTML syntax
    private static final int ISA_LT = 3;      // After start of tag delimiter - "<"
    private static final int ISA_SLASH = 4;   // After ETAGO - "</"
    private static final int ISI_ENDTAG = 5;  // Inside endtag - "</[a..Z]+"
    private static final int ISP_ENDTAG_X = 6;  // X-switch after ENDTAG's name
    private static final int ISP_ENDTAG_WS = 7; // In WS in ENDTAG - "</A_ _>"
    private static final int ISI_TAG = 8;     // Inside tag - "<[a..Z]+"
    private static final int ISP_TAG_X = 9;   // X-switch after TAG's name
    private static final int ISP_TAG_WS = 10; // In WS in TAG - "<A_ _...>"
    private static final int ISI_ARG = 11;    // Inside tag's argument - "<A h_r_...>"
    private static final int ISP_ARG_X = 12;  // X-switch after ARGUMENT's name
    private static final int ISP_ARG_WS = 13; // Inside WS after argument awaiting '='
    private static final int ISP_EQ = 14;     // X-switch after '=' in TAG's ARGUMENT
    private static final int ISP_EQ_WS = 15;  // In WS after '='
    private static final int ISI_VAL = 16;    // Non-quoted value
    private static final int ISI_VAL_QUOT = 17;   // Single-quoted value - may contain " chars
    private static final int ISI_VAL_DQUOT = 18;  // Double-quoted value - may contain ' chars
    private static final int ISA_SGML_ESCAPE = 19;  // After "<!"
    private static final int ISA_SGML_DASH = 20;    // After "<!-"
    private static final int ISI_HTML_COMMENT = 21; // Somewhere after "<!--"
    private static final int ISA_HTML_COMMENT_DASH = 22;  // Dash in comment - maybe end of comment
    private static final int ISI_HTML_COMMENT_WS = 23;  // After end of comment, awaiting end of comment declaration
    private static final int ISI_SGML_DECL = 24;
    private static final int ISA_SGML_DECL_DASH = 25;
    private static final int ISI_SGML_COMMENT = 26;
    private static final int ISA_SGML_COMMENT_DASH = 27;
    private static final int ISA_REF = 28;    // when comes to character reference, e.g. &amp;, after &
    private static final int ISI_REF_NAME = 29; // if the reference is symbolic - by predefined name
    private static final int ISA_REF_HASH = 30; // for numeric references - after &#
    private static final int ISI_REF_DEC = 31;  // decimal character reference, e.g. &#345;
    private static final int ISA_REF_X = 32;    //
    private static final int ISI_REF_HEX = 33;  // hexadecimal reference, in &#xa.. of &#X9..
    private static final int ISI_TAG_SLASH = 34; //after slash in html tag
    
    public HTMLLexer(LexerRestartInfo<HTMLTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        if (info.state() == null) {
            this.subState = INIT;
            this.state = INIT;
            this.scriptState = INIT;
        } else {
            int encoded = ((Integer) info.state()).intValue();
            this.subState = encoded / 1000000;
            int remainder = encoded % 1000000;
            this.state    = remainder / 1000;
            this.scriptState = remainder % 1000;
        }
    }
    
    private final boolean isAZ( int ch ) {
        return( (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') );
    }
    
    private final boolean isName( int ch ) {
        return Character.isLetterOrDigit(ch) ||
                ch == '-' || ch == '_' || ch == '.' || ch == ':';
        //        return( (ch >= 'a' && ch <= 'z') ||
        //                (ch >= 'A' && ch <= 'Z') ||
        //                (ch >= '0' && ch <= '9') ||
        //                ch == '-' || ch == '_' || ch == '.' || ch == ':' );
        
    }
    
    /**
     * Resolves if given char is whitespace in terms of HTML4.0 specs
     * According to specs, following characters are treated as whitespace:
     * Space - <CODE>'\u0020'</CODE>, Tab - <CODE>'\u0009'</CODE>,
     * Formfeed - <CODE>'\u000C'</CODE>,Zero-width space - <CODE>'\u200B'</CODE>,
     * Carriage return - <CODE>'\u000D'</CODE> and Line feed - <CODE>'\u000A'</CODE>
     * CR's are included for completenes only, they should never appear in document
     */
    
    private final boolean isWS( int ch ) {
        return Character.isWhitespace(ch);
        //        return ( ch == '\u0020' || ch == '\u0009' || ch == '\u000c'
        //              || ch == '\u200b' || ch == '\n' || ch == '\r' );
    }
    
    public Token<HTMLTokenId> nextToken() {
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
            
            //System.out.println("HTMLSyntax: parseToken tokenOffset=" + tokenOffset + ", actChar='" + actChar + "', offset=" + offset + ", state=" + getStateName(state) +
            //      ", stopOffset=" + stopOffset + ", lastBuffer=" + lastBuffer);
            switch( state ) {
                case INIT:              // DONE
                    switch( actChar ) {
                        case '<':
                            state = ISA_LT;
                            break;
                        case '&':
                            state = ISA_REF;
                            subState = ISI_TEXT;
                            break;
                        default:
                            state = ISI_TEXT;
                            break;
                    }
                    break;
                    
                case ISI_TEXT:        // DONE
                    switch( actChar ) {
                        case '<':
                        case '&':
                            state = INIT;
                            input.backup(1);
                            if(input.readLength() > 0) { //is there any text before & or < ???
                                return token(scriptState == INIT ? HTMLTokenId.TEXT : HTMLTokenId.SCRIPT);
                            }
                            break;
                    }
                    break;
                    
                case ISI_ERROR:      // DONE
                    state = INIT;
                    return token(HTMLTokenId.ERROR);
                    
                case ISA_LT:         // PENDING other transitions - e.g '<?'
                    if( isAZ( actChar ) ) {   // <'a..Z'
                        state = ISI_TAG;
                        input.backup(1);
                        return token(HTMLTokenId.TAG_OPEN_SYMBOL);
                    }
                    switch( actChar ) {
                        case '/':               // ETAGO - </
                            state = ISA_SLASH;
                            return token(HTMLTokenId.TAG_OPEN_SYMBOL);
                        case '>':               // Empty start tag <>, RELAXED
                            state = INIT;
                            return token(HTMLTokenId.TAG_CLOSE_SYMBOL);
                        case '!':
                            state = ISA_SGML_ESCAPE;
                            break;
                        default:                // Part of text, RELAXED
                            state = ISI_TEXT;
                            continue;             // don't eat the char, maybe its '&'
                    }
                    break;
                    
                case ISA_SLASH:        // DONE
                    if( isAZ( actChar ) ) {   // </'a..Z'
                        state = ISI_ENDTAG;
                        break;
                    }
                    switch( actChar ) {
                        case '>':               // Empty end tag </>, RELAXED
                            state = INIT;
                            return token(HTMLTokenId.TAG_CLOSE_SYMBOL);
                        default:                // Part of text, e.g. </3, </'\n', RELAXED
                            state = ISI_TEXT;
                            input.backup(1);
                            continue;             // don'e eat the char
                    }
                    //break;
                    
                case ISI_ENDTAG:        // DONE
                    if( isName( actChar ) ) break;    // Still in endtag identifier, eat next char
                    state = ISP_ENDTAG_X;
                    input.backup(1);
                    //test if the tagname is SCRIPT
                    if("script".equalsIgnoreCase(input.readText().toString())) { //NOI18N
                        scriptState = INIT;
                        //System.out.println("---end of script");
                    }
                    
                    return token(HTMLTokenId.TAG_CLOSE);
                    
                    
                case ISP_ENDTAG_X:      // DONE
                    if( isWS( actChar ) ) {
                        state = ISP_ENDTAG_WS;
                        break;
                    }
                    switch( actChar ) {
                        case '>':               // Closing of endtag, e.g. </H6 _>_
                            state = INIT;
                            return token(HTMLTokenId.TAG_CLOSE_SYMBOL);
                        case '<':               // next tag, e.g. </H6 _<_, RELAXED
                            state = INIT;
                            input.backup(1);
                            continue;
                        default:
                            state = ISI_ERROR;
                            input.backup(1);
                            continue; //don't eat
                    }
                    //break;
                    
                case ISP_ENDTAG_WS:      // DONE
                    if( isWS( actChar ) ) break;  // eat all WS
                    state = ISP_ENDTAG_X;
                    input.backup(1);
                    return token(HTMLTokenId.WS);
                    
                    
                case ISI_TAG:        // DONE
                    if( isName( actChar ) ) break;    // Still in tag identifier, eat next char
                    state = ISP_TAG_X;
                    input.backup(1);
                    //test if the tagname is SCRIPT
                    if("script".equalsIgnoreCase(input.readText().toString())) { //NOI18N
                        scriptState = ISI_SCRIPT;
                        //System.out.println("+++start of script");
                    }
                    return token(HTMLTokenId.TAG_OPEN);
                    
                case ISP_TAG_X:     // DONE
                    if( isWS( actChar ) ) {
                        state = ISP_TAG_WS;
                        break;
                    }
                    if( isAZ( actChar ) ) {
                        state = ISI_ARG;
                        break;
                    }
                    switch( actChar ) {
                        case '/':
                            state = ISI_TAG_SLASH;
                            continue;
                        case '>':
                            state = INIT;
                            return token(HTMLTokenId.TAG_CLOSE_SYMBOL);
                        case '<':
                            state = INIT;
                            input.backup(1);
                            continue;       // don't eat it!!!
                        default:
                            state = ISI_ERROR;
                            input.backup(1);
                            continue;
                    }
                    //break;
                    
                case ISP_TAG_WS:        // DONE
                    if( isWS( actChar ) ) break;    // eat all WS
                    state = ISP_TAG_X;
                    input.backup(1);
                    return token(HTMLTokenId.WS);
                    
                case ISI_TAG_SLASH:
                    switch( actChar ) {
                        case '>':
                            state = INIT;
                            return token(HTMLTokenId.TAG_CLOSE_SYMBOL);
                        default:
                            state = ISI_ERROR;
                            input.backup(1);
                            continue;
                    }
                    
                case ISI_ARG:           // DONE
                    if( isName( actChar ) ) break; // eat next char
                    state = ISP_ARG_X;
                    input.backup(1);
                    return token(HTMLTokenId.ARGUMENT);
                    
                case ISP_ARG_X:
                    if( isWS( actChar ) ) {
                        state = ISP_ARG_WS;
                        break;
                    }
                    if( isAZ( actChar ) ) {
                        state = ISI_ARG;
                        break;
                    }
                    switch( actChar ) {
                        case '/':
                        case '>':
                            state = INIT;
                            return token(HTMLTokenId.TAG_OPEN);
                        case '<':
                            state = INIT;
                            input.backup(1);
                            continue;           // don't eat !!!
                        case '=':
                            state = ISP_EQ;
                            return token(HTMLTokenId.OPERATOR);
                        default:
                            state = ISI_ERROR;
                            input.backup(1);
                            continue;
                    }
                    //break;
                    
                case ISP_ARG_WS:
                    if( isWS( actChar ) ) break;    // Eat all WhiteSpace
                    state = ISP_ARG_X;
                    input.backup(1);
                    return token(HTMLTokenId.WS);
                    
                case ISP_EQ:
                    if( isWS( actChar ) ) {
                        state = ISP_EQ_WS;
                        break;
                    }
                    switch( actChar ) {
                        case '\'':
                            state = ISI_VAL_QUOT;
                            break;
                        case '"':
                            state = ISI_VAL_DQUOT;
                            break;
                        case '>':
                            state = INIT;
                            return token(HTMLTokenId.TAG_OPEN);
                        default:
                            state = ISI_VAL; //everything else if attribute value
                            break;
                    }
                    break;
                    
                case ISP_EQ_WS:
                    if( isWS( actChar ) ) break;    // Consume all WS
                    state = ISP_EQ;
                    input.backup(1);
                    return token(HTMLTokenId.WS);
                    
                    
                case ISI_VAL:
                    if( !isWS( actChar )
                    && !(actChar == '/' || actChar == '>' || actChar == '<')) break;  // Consume whole value
                    state = ISP_TAG_X;
                    input.backup(1);
                    return token(HTMLTokenId.VALUE);
                    
                case ISI_VAL_QUOT:
                    switch( actChar ) {
                        case '\'':
                            state = ISP_TAG_X;
                            return token(HTMLTokenId.VALUE);
                        case '&':
                            if( input.readLength() == 1 ) {
                                subState = state;
                                state = ISA_REF;
                                break;
                            } else {
                                input.backup(1);
                                return token(HTMLTokenId.VALUE);
                            }
                    }
                    break;  // else simply consume next char of VALUE
                    
                case ISI_VAL_DQUOT:
                    switch( actChar ) {
                        case '"':
                            state = ISP_TAG_X;
                            return token(HTMLTokenId.VALUE);
                        case '&':
                            if( input.readLength() == 1 ) {
                                subState = state;
                                state = ISA_REF;
                                break;
                            } else {
                                input.backup(1);
                                return token(HTMLTokenId.VALUE);
                            }
                    }
                    break;  // else simply consume next char of VALUE
                    
                    
                    
                case ISA_SGML_ESCAPE:       // DONE
                    if( isAZ(actChar) ) {
                        state = ISI_SGML_DECL;
                        break;
                    }
                    switch( actChar ) {
                        case '-':
                            state = ISA_SGML_DASH;
                            break;
                        default:
                            state = ISI_TEXT;
                            input.backup(1);
                            continue;
                    }
                    break;
                    
                case ISA_SGML_DASH:       // DONE
                    switch( actChar ) {
                        case '-':
                            state = ISI_HTML_COMMENT;
                            break;
                        default:
                            state = ISI_TEXT;
                            input.backup(1);
                            continue;
                    }
                    break;
                    
                case ISI_HTML_COMMENT:        // DONE
                    switch( actChar ) {
                        case '-':
                            state = ISA_HTML_COMMENT_DASH;
                            break;
                            //create an HTML comment token for each line of the comment - a performance fix for #43532
                        case '\n':
                            //leave the some state - we are still in an HTML comment,
                            //we just need to create a token for each line.
                            return token(HTMLTokenId.BLOCK_COMMENT);
                    }
                    break;
                    
                case ISA_HTML_COMMENT_DASH:
                    switch( actChar ) {
                        case '-':
                            state = ISI_HTML_COMMENT_WS;
                            break;
                        default:
                            state = ISI_HTML_COMMENT;
                            continue;
                    }
                    break;
                    
                case ISI_HTML_COMMENT_WS:       // DONE
                    if( isWS( actChar ) ) break;  // Consume all WS
                    switch( actChar ) {
                        case '>':
                            state = INIT;
                            return token(HTMLTokenId.BLOCK_COMMENT);
                        default:
                            state = ISI_HTML_COMMENT;
                            input.backup(1);
                            continue;
                    }
                    //break;
                    
                case ISI_SGML_DECL:
                    switch( actChar ) {
                        case '>':
                            state = INIT;
                            return token(HTMLTokenId.DECLARATION);
                        case '-':
                            if( input.readLength() == 1 ) {
                                state = ISA_SGML_DECL_DASH;
                                break;
                            } else {
                                input.backup(1);
                                return token(HTMLTokenId.DECLARATION);
                            }
                    }
                    break;
                    
                case ISA_SGML_DECL_DASH:
                    if( actChar == '-' ) {
                        state = ISI_SGML_COMMENT;
                        break;
                    } else {
                        state = ISI_SGML_DECL;
                        input.backup(1);
                        continue;
                    }
                    
                case ISI_SGML_COMMENT:
                    switch( actChar ) {
                        case '-':
                            state = ISA_SGML_COMMENT_DASH;
                            break;
                    }
                    break;
                    
                case ISA_SGML_COMMENT_DASH:
                    if( actChar == '-' ) {
                        state = ISI_SGML_DECL;
                        return token(HTMLTokenId.SGML_COMMENT);
                    } else {
                        state = ISI_SGML_COMMENT;
                        input.backup(1);
                        continue;
                    }
                    
                    
                case ISA_REF:
                    if( isAZ( actChar ) ) {
                        state = ISI_REF_NAME;
                        break;
                    }
                    if( actChar == '#' ) {
                        state = ISA_REF_HASH;
                        break;
                    }
                    state = subState;
                    input.backup(1);
                    continue;
                    
                case ISI_REF_NAME:
                    if( isName( actChar ) ) break;
                    if( actChar != ';' )
                        input.backup(1);
                    state = subState;
                    return token(HTMLTokenId.CHARACTER);
                    
                case ISA_REF_HASH:
                    if( actChar >= '0' && actChar <= '9' ) {
                        state = ISI_REF_DEC;
                        break;
                    }
                    if( actChar == 'x' || actChar == 'X' ) {
                        state = ISA_REF_X;
                        break;
                    }
                    if( isAZ( actChar ) ) {
                        state = subState;
                        return token(HTMLTokenId.ERROR);
                    }
                    state = subState;
                    input.backup(1);
                    continue;
                    
                case ISI_REF_DEC:
                    if( actChar >= '0' && actChar <= '9' ) break;
                    if( actChar != ';' )
                        input.backup(1);
                    state = subState;
                    return token(HTMLTokenId.CHARACTER);
                    
                case ISA_REF_X:
                    if( (actChar >= '0' && actChar <= '9') ||
                            (actChar >= 'a' && actChar <= 'f') ||
                            (actChar >= 'A' && actChar <= 'F')
                            ) {
                        state = ISI_REF_HEX;
                        break;
                    }
                    state = subState;
                    input.backup(1);
                    return token(HTMLTokenId.ERROR);       // error on previous "&#x" sequence
                    
                case ISI_REF_HEX:
                    if( (actChar >= '0' && actChar <= '9') ||
                            (actChar >= 'a' && actChar <= 'f') ||
                            (actChar >= 'A' && actChar <= 'F')
                            ) break;
                    if( actChar != ';' )
                        input.backup(1);
                    state = subState;
                    return token(HTMLTokenId.CHARACTER);
            }
        } // end of while(offset...)
        
        /** At this stage there's no more text in the scanned buffer.
         * Scanner first checks whether this is completely the last
         * available buffer.
         */
        switch( state ) {
            case INIT:
                if (input.readLength() == 0)
                    return null;
            case ISI_TEXT:
            case ISA_LT:
            case ISA_SLASH:
            case ISA_SGML_ESCAPE:
            case ISA_SGML_DASH:
                state = INIT;
                return token(scriptState == INIT ? HTMLTokenId.TEXT : HTMLTokenId.SCRIPT);
                
            case ISA_REF:
            case ISA_REF_HASH:
                state = INIT;
                if( subState == ISI_TEXT ) return token(scriptState == INIT ? HTMLTokenId.TEXT : HTMLTokenId.SCRIPT);
                else return token(HTMLTokenId.VALUE);
                
            case ISI_HTML_COMMENT:
            case ISA_HTML_COMMENT_DASH:
            case ISI_HTML_COMMENT_WS:
                state = INIT;
                return token(HTMLTokenId.BLOCK_COMMENT);
                
            case ISI_TAG:
                state = INIT;
                return token(HTMLTokenId.TAG_OPEN);
            case ISI_ENDTAG:
                state = INIT;
                return token(HTMLTokenId.TAG_CLOSE);
                
            case ISI_ARG:
                state = INIT;
                return token(HTMLTokenId.ARGUMENT);
                
            case ISI_ERROR:
                state = INIT;
                return token(HTMLTokenId.ERROR);
                
            case ISP_ARG_WS:
            case ISP_TAG_WS:
            case ISP_ENDTAG_WS:
            case ISP_EQ_WS:
                state = INIT;
                return token(HTMLTokenId.WS);
                
            case ISP_ARG_X:
            case ISP_TAG_X:
            case ISP_ENDTAG_X:
            case ISP_EQ:
                state = INIT;
                return token(HTMLTokenId.WS);
                
            case ISI_VAL:
            case ISI_VAL_QUOT:
            case ISI_VAL_DQUOT:
                state = INIT;
                return token(HTMLTokenId.VALUE);
                
            case ISI_SGML_DECL:
            case ISA_SGML_DECL_DASH:
                state = INIT;
                return token(HTMLTokenId.DECLARATION);
                
            case ISI_SGML_COMMENT:
            case ISA_SGML_COMMENT_DASH:
                state = INIT;
                return token(HTMLTokenId.SGML_COMMENT);
                
            case ISI_REF_NAME:
            case ISI_REF_DEC:
            case ISA_REF_X:
            case ISI_REF_HEX:
                state = INIT;
                return token(HTMLTokenId.CHARACTER);
        }
        
        return null;
    }
    
    private Token<HTMLTokenId> token(HTMLTokenId id) {
//        System.out.print("--- token(" + id + "; '" + input.readText().toString() + "')");
//        if(input.readLength() == 0) {
//            System.out.println("\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! HTMLLexer error - zero length token!");
//        }
        Token<HTMLTokenId> t = tokenFactory.createToken(id);
//        System.out.println(t.id() + "; " + t.length());
        return t;
    }
    
}
