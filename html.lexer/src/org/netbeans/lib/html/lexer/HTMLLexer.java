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

import java.util.logging.Level;
import java.util.logging.Logger;
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

public final class HTMLLexer implements Lexer<HTMLTokenId> {
    
    private static final Logger LOGGER = Logger.getLogger(HTMLLexer.class.getName());
    private static final boolean LOG = Boolean.getBoolean("j2ee_lexer_debug"); //NOI18N
    
    private static final int EOF = LexerInput.EOF;
    
    private final LexerInput input;
    
    private final TokenFactory<HTMLTokenId> tokenFactory;
    
    public Object state() {
        return lexerSubState * 1000000 + lexerState * 1000 + lexerScriptState;
    }
    
    
    /** Internal state of the lexical analyzer before entering subanalyzer of
     * character references. It is initially set to INIT, but before first usage,
     * this will be overwritten with state, which originated transition to
     * charref subanalyzer.
     */
    private int lexerSubState = INIT;
    private int lexerState    = INIT;
    
    /** indicated whether we are in a script */
    private int lexerScriptState = INIT;
    
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
    private static final int ISI_SCRIPT_CONTENT = 35; //after <script> tags closing symbol '>' - the tag content
    private static final int ISI_SCRIPT_CONTENT_AFTER_LT = 36; //after < in script content
    private static final int ISI_SCRIPT_CONTENT_ENDTAG = 37; //after </ in script content
    
    
    
    public HTMLLexer(LexerRestartInfo<HTMLTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        if (info.state() == null) {
            this.lexerSubState = INIT;
            this.lexerState = INIT;
            this.lexerScriptState = INIT;
        } else {
            int encoded = ((Integer) info.state()).intValue();
            this.lexerSubState = encoded / 1000000;
            int remainder = encoded % 1000000;
            this.lexerState    = remainder / 1000;
            this.lexerScriptState = remainder % 1000;
        }
    }
    
    private final boolean isAZ( int character ) {
        return( (character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z') );
    }
    
    private final boolean isName( int character ) {
        return Character.isLetterOrDigit(character) ||
                character == '-' || character == '_' || character == '.' || character == ':';
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
    
    private final boolean isWS( int character ) {
        return Character.isWhitespace(character);
        //        return ( ch == '\u0020' || ch == '\u0009' || ch == '\u000c'
        //              || ch == '\u200b' || ch == '\n' || ch == '\r' );
    }
    
    private boolean followsScriptCloseTag() {
        int actChar;
        int prev_read = input.readLength(); //remember the size of the read sequence //substract the first read character
        int read = 0;
        while(true) {
            actChar = input.read();
            read++;
            if(!(Character.isLetter(actChar) ||
                    Character.isDigit(actChar) ||
                    (actChar == '_') ||
                    (actChar == '-') ||
                    (actChar == ':') ||
                    (actChar == '.') ||
                    (actChar == '/')) ||
                    (actChar == EOF)) { // EOL or not alpha
                //end of tagname
                CharSequence tagName = input.readText().subSequence(prev_read, prev_read + read - 1);
                
                input.backup(read); //put the lookahead text back to the buffer

                if("script".equalsIgnoreCase(tagName.toString())) {
                    if(actChar == '>') {
                        return true;
                    }
                }
                
                return false;
            }
        }
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
            switch( lexerState ) {
                case INIT:              // DONE
                    switch( actChar ) {
                        case '<':
                            lexerState = ISA_LT;
                            break;
                        case '&':
                            lexerState = ISA_REF;
                            lexerSubState = ISI_TEXT;
                            break;
                        default:
                            lexerState = ISI_TEXT;
                            break;
                    }
                    break;
                    
                case ISI_TEXT:        // DONE
                    switch( actChar ) {
                        case '<':
                        case '&':
                            lexerState = INIT;
                            input.backup(1);
                            if(input.readLength() > 0) { //is there any text before & or < ???
                                return token(HTMLTokenId.TEXT);
                            }
                            break;
                    }
                    break;
                    
                case ISI_ERROR:      // DONE
                    lexerState = INIT;
                    return token(HTMLTokenId.ERROR);
                    
                case ISA_LT:         // PENDING other transitions - e.g '<?'
                    if( isAZ( actChar ) ) {   // <'a..Z'
                        lexerState = ISI_TAG;
                        input.backup(1);
                        return token(HTMLTokenId.TAG_OPEN_SYMBOL);
                    }
                    switch( actChar ) {
                        case '/':               // ETAGO - </
                            lexerState = ISA_SLASH;
                            return token(HTMLTokenId.TAG_OPEN_SYMBOL);
                        case '>':               // Empty start tag <>, RELAXED
                            lexerState = INIT;
                            return token(HTMLTokenId.TAG_CLOSE_SYMBOL);
                        case '!':
                            lexerState = ISA_SGML_ESCAPE;
                            break;
                        default:                // Part of text, RELAXED
                            lexerState = ISI_TEXT;
                            break;
                    }
                    break;
                    
                case ISA_SLASH:        // DONE
                    if( isAZ( actChar ) ) {   // </'a..Z'
                        lexerState = ISI_ENDTAG;
                        break;
                    }
                    switch( actChar ) {
                        case '>':               // Empty end tag </>, RELAXED
                            lexerState = INIT;
                            return token(HTMLTokenId.TAG_CLOSE_SYMBOL);
                        default:                // Part of text, e.g. </3, </'\n', RELAXED
                            lexerState = ISI_TEXT;
                            input.backup(1);
                            break;
                    }
                    break;
                    
                case ISI_ENDTAG:        // DONE
                    if( isName( actChar ) ) break;    // Still in endtag identifier, eat next char
                    lexerState = ISP_ENDTAG_X;
                    input.backup(1);
                    return token(HTMLTokenId.TAG_CLOSE);
                    
                    
                case ISP_ENDTAG_X:      // DONE
                    if( isWS( actChar ) ) {
                        lexerState = ISP_ENDTAG_WS;
                        break;
                    }
                    switch( actChar ) {
                        case '>':               // Closing of endtag, e.g. </H6 _>_
                            lexerState = INIT;
                            return token(HTMLTokenId.TAG_CLOSE_SYMBOL);
                        case '<':               // next tag, e.g. </H6 _<_, RELAXED
                            lexerState = INIT;
                            input.backup(1);
                            break;
                        default:
                            lexerState = ISI_ERROR;
                            input.backup(1);
                            break;
                    }
                    break;
                    
                case ISP_ENDTAG_WS:      // DONE
                    if( isWS( actChar ) ) break;  // eat all WS
                    lexerState = ISP_ENDTAG_X;
                    input.backup(1);
                    return token(HTMLTokenId.WS);
                    
                    
                case ISI_TAG:        // DONE
                    if( isName( actChar ) ) break;    // Still in tag identifier, eat next char
                    lexerState = ISP_TAG_X;
                    input.backup(1);
                    //test if the tagname is SCRIPT
                    if("script".equalsIgnoreCase(input.readText().toString())) { //NOI18N
                        lexerScriptState = ISI_SCRIPT;
                    }
                    return token(HTMLTokenId.TAG_OPEN);
                    
                case ISP_TAG_X:     // DONE
                    if( isWS( actChar ) ) {
                        lexerState = ISP_TAG_WS;
                        break;
                    }
                    if( isAZ( actChar ) ) {
                        lexerState = ISI_ARG;
                        break;
                    }
                    switch( actChar ) {
                        case '/':
                            lexerState = ISI_TAG_SLASH;
                            break;
                        case '>':
                            if(lexerScriptState == ISI_SCRIPT) {
                                lexerState = ISI_SCRIPT_CONTENT;
                            } else {
                                lexerState = INIT;
                            }
                            return token(HTMLTokenId.TAG_CLOSE_SYMBOL);
                        case '<':
                            lexerState = INIT;
                            input.backup(1);
                            break;
                        default:
                            lexerState = ISI_ERROR;
                            input.backup(1);
                            break;
                    }
                    break;
                    
                case ISP_TAG_WS:        // DONE
                    if( isWS( actChar ) ) break;    // eat all WS
                    lexerState = ISP_TAG_X;
                    input.backup(1);
                    return token(HTMLTokenId.WS);
                    
                case ISI_TAG_SLASH:
                    switch( actChar ) {
                        case '>':
                            switch(lexerScriptState) {
                                case INIT:
                                    lexerState = INIT;
                                    break;
                                case ISI_SCRIPT:
                                    lexerState = ISI_SCRIPT_CONTENT;
                                    break;
                            }
                            return token(HTMLTokenId.TAG_CLOSE_SYMBOL);
                        default:
                            lexerState = ISI_ERROR;
                            input.backup(1);
                            break;
                    }
                    break;
                    
                case ISI_SCRIPT_CONTENT:   
                    switch( actChar ) {
                        case '<' :
                            lexerState = ISI_SCRIPT_CONTENT_AFTER_LT;
                            break;
                        default:
                            break;
                    }
                    break;
                    
                case ISI_SCRIPT_CONTENT_AFTER_LT:
                    if (actChar == '/') {
                        if (followsScriptCloseTag()) {
                            //end of script section found
                            lexerScriptState = INIT;
                            lexerState = INIT;
                            input.backup(2); //backup the '</', we will read it again
                            if (input.readLength() > 0) {
                                //the script has a body
                                return token(HTMLTokenId.SCRIPT);
                            } else {
                                break;
                            }
                        }
                    }
                    lexerState = ISI_SCRIPT_CONTENT;
                    break;

                case ISI_ARG:           // DONE
                    if( isName( actChar ) ) break; // eat next char
                    lexerState = ISP_ARG_X;
                    input.backup(1);
                    return token(HTMLTokenId.ARGUMENT);
                    
                case ISP_ARG_X:
                    if( isWS( actChar ) ) {
                        lexerState = ISP_ARG_WS;
                        break;
                    }
                    if( isAZ( actChar ) ) {
                        lexerState = ISI_ARG;
                        break;
                    }
                    switch( actChar ) {
                        case '/':
                        case '>':
                            input.backup(1);
                            lexerState = ISP_TAG_X;
                            break;
                        case '<':
                            lexerState = INIT;
                            input.backup(1);
                            break;
                        case '=':
                            lexerState = ISP_EQ;
                            return token(HTMLTokenId.OPERATOR);
                        default:
                            lexerState = ISI_ERROR;
                            input.backup(1);
                            break;
                    }
                    break;
                    
                case ISP_ARG_WS:
                    if( isWS( actChar ) ) break;    // Eat all WhiteSpace
                    lexerState = ISP_ARG_X;
                    input.backup(1);
                    return token(HTMLTokenId.WS);
                    
                case ISP_EQ:
                    if( isWS( actChar ) ) {
                        lexerState = ISP_EQ_WS;
                        break;
                    }
                    switch( actChar ) {
                        case '\'':
                            lexerState = ISI_VAL_QUOT;
                            break;
                        case '"':
                            lexerState = ISI_VAL_DQUOT;
                            break;
                        case '/':
                        case '>':
                            input.backup(1);
                            lexerState = ISP_TAG_X;
                            break;
                        default:
                            lexerState = ISI_VAL; //everything else if attribute value
                            break;
                    }
                    break;
                    
                case ISP_EQ_WS:
                    if( isWS( actChar ) ) break;    // Consume all WS
                    lexerState = ISP_EQ;
                    input.backup(1);
                    return token(HTMLTokenId.WS);
                    
                    
                case ISI_VAL:
                    if( !isWS( actChar )
                    && !(actChar == '/' || actChar == '>' || actChar == '<')) break;  // Consume whole value
                    lexerState = ISP_TAG_X;
                    input.backup(1);
                    return token(HTMLTokenId.VALUE);
                    
                case ISI_VAL_QUOT:
                    switch( actChar ) {
                        case '\'':
                            lexerState = ISP_TAG_X;
                            return token(HTMLTokenId.VALUE);
                        case '&':
                            if( input.readLength() == 1 ) {
                                lexerSubState = lexerState;
                                lexerState = ISA_REF;
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
                            lexerState = ISP_TAG_X;
                            return token(HTMLTokenId.VALUE);
                        case '&':
                            if( input.readLength() == 1 ) {
                                lexerSubState = lexerState;
                                lexerState = ISA_REF;
                                break;
                            } else {
                                input.backup(1);
                                return token(HTMLTokenId.VALUE);
                            }
                    }
                    break;  // else simply consume next char of VALUE
                    
                    
                    
                case ISA_SGML_ESCAPE:       // DONE
                    if( isAZ(actChar) ) {
                        lexerState = ISI_SGML_DECL;
                        break;
                    }
                    switch( actChar ) {
                        case '-':
                            lexerState = ISA_SGML_DASH;
                            break;
                        default:
                            lexerState = ISI_TEXT;
                            input.backup(1);
                            continue;
                    }
                    break;
                    
                case ISA_SGML_DASH:       // DONE
                    switch( actChar ) {
                        case '-':
                            lexerState = ISI_HTML_COMMENT;
                            break;
                        default:
                            lexerState = ISI_TEXT;
                            input.backup(1);
                            continue;
                    }
                    break;
                    
                case ISI_HTML_COMMENT:        // DONE
                    switch( actChar ) {
                        case '-':
                            lexerState = ISA_HTML_COMMENT_DASH;
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
                            lexerState = ISI_HTML_COMMENT_WS;
                            break;
                        default:
                            lexerState = ISI_HTML_COMMENT;
                            continue;
                    }
                    break;
                    
                case ISI_HTML_COMMENT_WS:       // DONE
                    if( isWS( actChar ) ) break;  // Consume all WS
                    switch( actChar ) {
                        case '>':
                            lexerState = INIT;
                            return token(HTMLTokenId.BLOCK_COMMENT);
                        default:
                            lexerState = ISI_HTML_COMMENT;
                            input.backup(1);
                            break;
                    }
                    break;
                    
                case ISI_SGML_DECL:
                    switch( actChar ) {
                        case '>':
                            lexerState = INIT;
                            return token(HTMLTokenId.DECLARATION);
                        case '-':
                            if( input.readLength() == 1 ) {
                                lexerState = ISA_SGML_DECL_DASH;
                                break;
                            } else {
                                input.backup(1);
                                return token(HTMLTokenId.DECLARATION);
                            }
                    }
                    break;
                    
                case ISA_SGML_DECL_DASH:
                    if( actChar == '-' ) {
                        lexerState = ISI_SGML_COMMENT;
                        break;
                    } else {
                        lexerState = ISI_SGML_DECL;
                        input.backup(1);
                        continue;
                    }
                    
                case ISI_SGML_COMMENT:
                    switch( actChar ) {
                        case '-':
                            lexerState = ISA_SGML_COMMENT_DASH;
                            break;
                    }
                    break;
                    
                case ISA_SGML_COMMENT_DASH:
                    if( actChar == '-' ) {
                        lexerState = ISI_SGML_DECL;
                        return token(HTMLTokenId.SGML_COMMENT);
                    } else {
                        lexerState = ISI_SGML_COMMENT;
                        input.backup(1);
                        continue;
                    }
                    
                    
                case ISA_REF:
                    if( isAZ( actChar ) ) {
                        lexerState = ISI_REF_NAME;
                        break;
                    }
                    if( actChar == '#' ) {
                        lexerState = ISA_REF_HASH;
                        break;
                    }
                    lexerState = lexerSubState;
                    input.backup(1);
                    continue;
                    
                case ISI_REF_NAME:
                    if( isName( actChar ) ) break;
                    if( actChar != ';' )
                        input.backup(1);
                    lexerState = lexerSubState;
                    return token(HTMLTokenId.CHARACTER);
                    
                case ISA_REF_HASH:
                    if( actChar >= '0' && actChar <= '9' ) {
                        lexerState = ISI_REF_DEC;
                        break;
                    }
                    if( actChar == 'x' || actChar == 'X' ) {
                        lexerState = ISA_REF_X;
                        break;
                    }
                    if( isAZ( actChar ) ) {
                        lexerState = lexerSubState;
                        return token(HTMLTokenId.ERROR);
                    }
                    lexerState = lexerSubState;
                    input.backup(1);
                    continue;
                    
                case ISI_REF_DEC:
                    if( actChar >= '0' && actChar <= '9' ) break;
                    if( actChar != ';' )
                        input.backup(1);
                    lexerState = lexerSubState;
                    return token(HTMLTokenId.CHARACTER);
                    
                case ISA_REF_X:
                    if( (actChar >= '0' && actChar <= '9') ||
                            (actChar >= 'a' && actChar <= 'f') ||
                            (actChar >= 'A' && actChar <= 'F')
                            ) {
                        lexerState = ISI_REF_HEX;
                        break;
                    }
                    lexerState = lexerSubState;
                    input.backup(1);
                    return token(HTMLTokenId.ERROR);       // error on previous "&#x" sequence
                    
                case ISI_REF_HEX:
                    if( (actChar >= '0' && actChar <= '9') ||
                            (actChar >= 'a' && actChar <= 'f') ||
                            (actChar >= 'A' && actChar <= 'F')
                            ) break;
                    if( actChar != ';' )
                        input.backup(1);
                    lexerState = lexerSubState;
                    return token(HTMLTokenId.CHARACTER);
            }
        } // end of while(offset...)
        
        /** At this stage there's no more text in the scanned buffer.
         * Scanner first checks whether this is completely the last
         * available buffer.
         */
        switch( lexerState ) {
            case INIT:
                if (input.readLength() == 0) {
                    return null;
                }
                break;
            case ISI_TEXT:
            case ISA_LT:
            case ISA_SLASH:
            case ISA_SGML_ESCAPE:
            case ISA_SGML_DASH:
            case ISI_TAG_SLASH:
                return token(HTMLTokenId.TEXT);
                
            case ISA_REF:
            case ISA_REF_HASH:
                if( lexerSubState == ISI_TEXT ) return token(HTMLTokenId.TEXT);
                else return token(HTMLTokenId.VALUE);
                
            case ISI_HTML_COMMENT:
            case ISA_HTML_COMMENT_DASH:
            case ISI_HTML_COMMENT_WS:
                return token(HTMLTokenId.BLOCK_COMMENT);
                
            case ISI_TAG:
                return token(HTMLTokenId.TAG_OPEN);
            case ISI_ENDTAG:
                return token(HTMLTokenId.TAG_CLOSE);
                
            case ISI_ARG:
                return token(HTMLTokenId.ARGUMENT);
                
            case ISI_ERROR:
                return token(HTMLTokenId.ERROR);
                
            case ISP_ARG_WS:
            case ISP_TAG_WS:
            case ISP_ENDTAG_WS:
            case ISP_EQ_WS:
                return token(HTMLTokenId.WS);
                
            case ISP_ARG_X:
            case ISP_TAG_X:
            case ISP_ENDTAG_X:
            case ISP_EQ:
                return token(HTMLTokenId.WS);
                
            case ISI_VAL:
            case ISI_VAL_QUOT:
            case ISI_VAL_DQUOT:
                return token(HTMLTokenId.VALUE);
                
            case ISI_SGML_DECL:
            case ISA_SGML_DECL_DASH:
                return token(HTMLTokenId.DECLARATION);
                
            case ISI_SGML_COMMENT:
            case ISA_SGML_COMMENT_DASH:
                return token(HTMLTokenId.SGML_COMMENT);
                
            case ISI_REF_NAME:
            case ISI_REF_DEC:
            case ISA_REF_X:
            case ISI_REF_HEX:
                return token(HTMLTokenId.CHARACTER);
            case ISI_SCRIPT_CONTENT:
            case ISI_SCRIPT_CONTENT_ENDTAG:
            case ISI_SCRIPT_CONTENT_AFTER_LT:
                return token(HTMLTokenId.SCRIPT);
                
        }
        
        return null;
    }
    
    private Token<HTMLTokenId> token(HTMLTokenId tokenId) {
        if(LOG) {
            if(input.readLength() == 0) {
                LOGGER.log(Level.INFO, "Found zero length token: ");
            }
            LOGGER.log(Level.INFO, "[" + this.getClass().getSimpleName() + "] token ('" + input.readText().toString() + "'; id=" + tokenId + "; state=" + state() + ")\n");
        }
        return tokenFactory.createToken(tokenId);
    }
    
    public void release() {
    }

}
