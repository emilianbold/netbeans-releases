/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.lib.xml.lexer;

import org.netbeans.api.lexer.PartType;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Lexical analyzer for XML. Based on original XML lexer from xml/editor module.
 *
 * @author Petr Nejedly
 * @author Miloslav Metelka
 * @author Jan Lahoda
 * @author Marek Fukala
 * @author Tomasz Slota
 * @version 1.00
 */

public class XMLLexer implements Lexer<XMLTokenId> {
    private LexerInput input;
    
    private TokenFactory<XMLTokenId> tokenFactory;
    
    public Object state() {
        Integer encoded = (prevState << 030) + (subState << 020) + (this.state << 010) + (subInternalDTD ? 1 : 0);
        return encoded;
    }
    
    private void loadState(final Object state) {
        if (state == null) {
            subState = INIT;
            this.state = INIT;
            subInternalDTD = false;
        } else {
            int encoded = ((Integer) state).intValue();
            
            this.prevState = (encoded & 0xff000000) >> 030;
            subState = (encoded & 0xff0000) >> 020;
            this.state    = (encoded & 0xff00) >> 010;
            subInternalDTD = encoded % 2 == 1;
        }
    }
    
    /**
     * Internal state of the lexical analyzer before entering subanalyzer of
     * character references. It is initially set to INIT, but before first
     * usage, this will be overwritten with state, which originated
     * ransition to charref subanalyzer.
     */
    protected int state = INIT;
    
    /**
     * Internal state of the lexical analyzer before entering subanalyzer of
     * character references. It is initially set to INIT, but before first
     * usage, this will be overwritten with state, which originated
     * ransition to charref subanalyzer.
     */
    protected int subState = INIT;

    /**
     * The previous saved state for transitions from _WS to non WS states.
     */
    protected int prevState = INIT;
    
    /**
     * Identifies internal DTD layer. Most of functionality is same
     * as at document layer, however there are minor exceptions.
     * @see isInternalDTD checks in code
     */
    protected boolean subInternalDTD = false;
    
    /** Initial internal state of the analyzer */
    public static final int INIT = 0;
    
    // Internal states I = in state
    //                 P = expected (char probed but not consumed)
    //                 A = after (char probed and consumed)
    
    
    private static final int ISI_TEXT = 1;    // Plain text between tags
    private static final int ISI_ERROR = 2;   // Syntax error in XML syntax
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
    private static final int ISI_VAL_APOS = 17;   // Single-quoted value - may contain " chars
    private static final int ISI_VAL_QUOT = 18;  // Double-quoted value - may contain ' chars
    private static final int ISA_SGML_ESCAPE = 19;  // After "<!"
    private static final int ISA_SGML_DASH = 20;    // After "<!-"
    private static final int ISI_XML_COMMENT = 21; // Somewhere after "<!--"
    private static final int ISA_XML_COMMENT_DASH = 22;  // Dash in comment - maybe end of comment
    private static final int ISI_XML_COMMENT_WS = 23;  // After end of comment, awaiting end of comment declaration
    private static final int ISI_SGML_DECL = 24;
    private static final int ISA_SGML_DECL_DASH = 25;
    //    private static final int ISI_SGML_COMMENT = 26;
    //    private static final int ISA_SGML_COMMENT_DASH = 27;
    private static final int ISA_REF = 28;    // when comes to character reference, e.g. &amp;, after &
    private static final int ISI_REF_NAME = 29; // if the reference is symbolic - by predefined name
    private static final int ISA_REF_HASH = 30; // for numeric references - after &#
    private static final int ISI_REF_DEC = 31;  // decimal character reference, e.g. &#345;
    private static final int ISA_REF_X = 32;    //
    private static final int ISI_REF_HEX = 33;  // hexadecimal reference, in &#xa.. of &#X9..
    
    
    private static final int ISI_PI = 35;  //after <?...
    private static final int ISI_PI_TARGET = 36;  //in <?..|..
    private static final int ISP_PI_TARGET_WS = 37; //after <?...|
    private static final int ISI_PI_CONTENT = 38;   //in PI content
    private static final int ISA_PI_CONTENT_QMARK = 39;  //after ? in content
    private static final int ISP_PI_CONTENT_QMARK = 40;  //spotet ? in content
    
    // CDATA section handler
    private static final int ISA_LTEXBR = 41;
    private static final int ISA_LTEXBRC = 42;
    private static final int ISA_LTEXBRCD = 43;
    private static final int ISA_LTEXBRCDA = 44;
    private static final int ISA_LTEXBRCDAT = 45;
    private static final int ISA_LTEXBRCDATA = 46;
    private static final int ISI_CDATA = 47;
    private static final int ISA_CDATA_BR = 48;
    private static final int ISA_CDATA_BRBR = 49;
    
    // strings in declaration
    private static final int ISI_DECL_CHARS = 50;
    private static final int ISI_DECL_STRING = 51;
    private static final int ISP_DECL_CHARS = 52;
    private static final int ISP_DECL_STRING = 53;
    
    // internal DTD handling
    private static final int ISA_INIT_BR = 54;
    
    private static final int ISI_ERROR_TAG = 55;
    private static final int ISI_ERROR_TAG_RECOVER = 55;
    
    public XMLLexer(LexerRestartInfo<XMLTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        loadState(info.state());
    }
    
    private final boolean isAZ( int ch ) {
        return( (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') );
    }
    
    /**
     * Resolves if given char is whitespace in terms of XML4.0 specs
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
    
    private void enterInternalDTD() {
        subInternalDTD = true;
    }
    
    private void leaveInternalDTD() {
        subInternalDTD = false;
    }
    
    private boolean isInternalDTD() {
        return subInternalDTD;
    }
    
    public Token<XMLTokenId> nextToken() {
        
        int actChar;
        while(true) {
            actChar = input.read();
            
            if (actChar == LexerInput.EOF){
                
                if (input.readLength() == 0){
                    return null;
                }
                
                input.backup(1);
                break;
            }
            
            switch( state ) {
                case INIT:              //     DONE
                    switch( actChar ) {
                        case '<':
                            state = ISA_LT;
                            break;
                        case '&':
                            if (isInternalDTD() == false) {
                                state = ISA_REF;
                                subState = ISI_TEXT;
                            } else {
                                state = ISI_TEXT;
                            }
                            break;
                        case '%':
                            if (isInternalDTD()) {
                                state = ISA_REF;
                                subState = INIT;
                            } else {
                                state = ISI_TEXT;
                            }
                            break;
                        case ']':
                            if (isInternalDTD()) {
                                state = ISA_INIT_BR;
                            } else {
                                state = ISI_TEXT;
                            }
                            break;
                        default:
                            state = ISI_TEXT;
                            break;
                    }
                    
                    break;
                    
                case ISI_TEXT:        // DONE
                    switch( actChar ) {
                        case '<':
                            state = INIT;
                            input.backup(1);
                            if(input.readLength() > 0) {
                                return token(XMLTokenId.TEXT);
                            }
                            break;
                        case '&':
                            if (isInternalDTD() == false) {
                                state = INIT;
                                input.backup(1);
                                if(input.readLength() > 0) {
                                    return token(XMLTokenId.TEXT);
                                }
                            }
                            break;
                        case '%':
                            if (isInternalDTD()) {
                                state = INIT;
                                input.backup(1);
                                return token(XMLTokenId.TEXT);
                            }
                            break;
                        case ']':
                            if (isInternalDTD()) {
                                state = ISA_INIT_BR;
                            }
                            break;
                    }
                    break;
                    
                case ISI_ERROR:      // DONE
                    state = INIT;
                    prevState = 0;
                    subState = 0;
                    if (input.readLength() > 1) {
                        input.backup(1);
                    }
                    return token(XMLTokenId.ERROR);
                    
                case ISI_ERROR_TAG:
                    state = ISP_TAG_X;
                    if (input.readLength() > 1) {
                        input.backup(1);
                    }
                    return token(XMLTokenId.ERROR);
                
                case ISA_LT:         // DONE
                    
                    if( UnicodeClasses.isXMLNameStartChar( actChar ) && isInternalDTD() == false) {
                        state = ISI_TAG;
                        break;
                    }
                    switch( actChar ) {
                        case '/':               // ETAGO - </
                            state = ISA_SLASH;
                            break;
                        case '!':
                            state = ISA_SGML_ESCAPE;
                            break;
                        case '?':
                            state = ISI_PI;
                            return token(XMLTokenId.PI_START);
                        default:
                            // note: it would be more correct to raise an error here,
                            // and return TAG PartType=Start, BUT some code already expects
                            // unfinished tags to be reported as TEXT.
                            state = INIT;
                            input.backup(1);
                            return tokenFactory.createToken(
                                    XMLTokenId.TEXT, input.readLength());
                    }
                    break;
                    
                case ISI_PI:
                    if ( UnicodeClasses.isXMLNameStartChar( actChar )) {
                        state = ISI_PI_TARGET;
                        break;
                    }
                    state = ISI_ERROR;
                    break;
                    
                case ISI_PI_TARGET:
                    if ( UnicodeClasses.isXMLNameChar( actChar )) break;
                    if (isWS( actChar )) {
                        state = ISP_PI_TARGET_WS;
                        input.backup(1);
                        return token(XMLTokenId.PI_TARGET);
                    }
                    state = ISI_ERROR;
                    break;
                    
                case ISP_PI_TARGET_WS:
                    if (isWS( actChar)) break;
                    state = ISI_PI_CONTENT;
                    input.backup(1);
                    return token(XMLTokenId.WS);
                    
                case ISI_PI_CONTENT:
                    // < is in theory allowed in PI content, as the delimiter is ?>, nut noone uses it.
                    if (actChar == '<') {
                        state = INIT;
                        input.backup(1);
                    } else {
                        if (actChar != '?') break;  // eat content
                        state = ISP_PI_CONTENT_QMARK;
                        input.backup(1);
                    }
                    if(input.readLength() > 0) {
                        return token(XMLTokenId.PI_CONTENT);  // may do extra break
                    }
                    break;
                    
                case ISP_PI_CONTENT_QMARK:
                    if (actChar != '?') throw new IllegalStateException("'?' expected in ISP_PI_CONTENT_QMARK");
                    state = ISA_PI_CONTENT_QMARK;
                    break;
                    
                case ISA_PI_CONTENT_QMARK:
                    if (actChar != '>') {
                        state = ISI_PI_CONTENT;
                        break;
                    }
                    state = INIT;
                    return token(XMLTokenId.PI_END);
                    
                case ISA_SLASH:        // DONE
                    
                    if( UnicodeClasses.isXMLNameStartChar( actChar )){
                        state = ISI_ENDTAG;
                        break;
                    }
                    switch( actChar ) {
                        case ' ':
                            state = ISI_TEXT;
                            continue;
                        case '\n':
                            state = ISI_TEXT;
                            continue;
                        case '\r':
                            state = ISI_TEXT;
                            continue;
                        default:                // Part of text, e.g. </3, </'\n', RELAXED
                            state = ISI_TEXT;
                            continue;             // don'e eat the char
                    }
                    //break;
                    
                case ISI_ENDTAG:        // DONE
                    if( UnicodeClasses.isXMLNameChar( actChar )){
                        break;    // Still in endtag identifier, eat next char
                    }
                    
                    state = ISP_ENDTAG_X;
                    input.backup(1);
                    return token(XMLTokenId.TAG);
                    
                    
                case ISP_ENDTAG_X:      // DONE
                    if( isWS( actChar ) ) {
                        state = ISP_ENDTAG_WS;
                        break;
                    }
                    switch( actChar ) {
                        case '>':               // Closing of endtag, e.g. </H6 _>_
                            state = INIT;
                            return token(XMLTokenId.TAG);
                        default:
                            state = ISI_ERROR;
                            continue; //don't eat
                    }
                    //break;
                    
                case ISP_ENDTAG_WS:      // DONE
                    if( isWS( actChar ) ) break;  // eat all WS
                    state = ISP_ENDTAG_X;
                    input.backup(1);
                    if (actChar == '>') {  
                        return token(XMLTokenId.WS);
                    }
                    state = ISI_ERROR;
                    break;
                    
                case ISI_TAG:        // DONE
                    if( UnicodeClasses.isXMLNameChar( actChar ) ) break; // Still in tag identifier, eat next char
                    state = ISP_TAG_X;
                    input.backup(1);
                    return token(XMLTokenId.TAG);
                    
                case ISP_TAG_X:     // DONE
                    if( isWS( actChar ) ) {
                        state = ISP_TAG_WS;
                        break;
                    }
                    if( UnicodeClasses.isXMLNameStartChar( actChar ) ) {
                        if (prevState == ISP_TAG_WS) {
                            prevState = 0;
                            input.backup(1);
                            return token(XMLTokenId.WS);
                        }
                        state = ISI_ARG;
                        break;
                    }
                    int c;
                    
                    switch( actChar ) {
                        case '/':
                            c = input.read();
                            if (c == '>') {
                                if (prevState == ISP_TAG_WS) {
                                    prevState = 0;
                                    input.backup(2);
                                    return token(XMLTokenId.WS);
                                }
                                state = INIT;
                                return token(XMLTokenId.TAG);
                            } else {
                                state = ISI_ERROR;
                                input.backup(1);
                                continue;
                            }
                        case '?': //Prolog and PI's now similar to Tag
                            c = input.read();
                            if (c == '>') {
                                if (prevState == ISP_TAG_WS) {
                                    prevState = 0;
                                    input.backup(1);
                                    return token(XMLTokenId.WS);
                                }
                                state = INIT;
                                return token(XMLTokenId.TAG);
                            } else {
                                state = ISI_ERROR;
                                input.backup(1);
                                continue;
                            }
                        case '>':
                            if (prevState == ISP_TAG_WS) {
                                prevState = 0;
                                input.backup(1);
                                return token(XMLTokenId.WS);
                            }
                            state = INIT;
                            return token(XMLTokenId.TAG);
                        case '<':
                            if (prevState == ISP_TAG_WS) {
                                prevState = 0;
                                input.backup(1);
                                state = ISI_ERROR;
                                continue;
                            }
                            // unexpected tag start:
                            state = INIT;
                            input.backup(1);
                            continue;
                        default:
                            if (prevState == ISP_TAG_WS) {
                                prevState = 0;
                                input.backup(1);
                                return token(XMLTokenId.WS);
                            }
                            input.backup(1);
                            state = ISI_ERROR_TAG;
                            continue;
                    }
                    
                    
                case ISP_TAG_WS:        // DONE
                    //input.backup(1);
                    if( isWS( actChar ) ) break;    // eat all WS
                    prevState = state;
                    state = ISP_TAG_X;
                    input.backup(1);
                    break;
                    //return token(XMLTokenId.WS);
                    
                case ISI_ARG:           // DONE
                    if( UnicodeClasses.isXMLNameChar( actChar ) ) break; // eat next char
                    prevState = ISI_ARG;
                    state = ISP_ARG_X;
                    input.backup(1);
                    return token(XMLTokenId.ARGUMENT);
                    
                case ISP_ARG_X:
                    if( isWS( actChar ) ) {
                        state = ISP_ARG_WS;
                        break;
                    }
                    switch( actChar ) {
                        case '=':
                            if (prevState == ISP_ARG_WS) {
                                prevState = 0;
                                input.backup(1);
                                return token(XMLTokenId.WS);
                            }
                            prevState = state;
                            state = ISP_EQ;
                            break;
                        case '<':
                            if (prevState == ISP_ARG_WS) {
                                prevState = 0;
                                state = ISI_ERROR;
                                input.backup(1);
                                continue;
                            } else {
                                input.backup(1);
                                state = INIT;
                                continue;
                            }
                            
                        default:
                            if (input.readLength() > 1) {
                                input.backup(1);
                            }
                            if (prevState == ISP_ARG_WS) {
                                prevState = 0;
                                state = ISI_ERROR_TAG;
                                continue;
                            }
                            prevState = state;
                            state = ISI_ERROR_TAG;
                            continue;
                    }
                    break;
                    
                case ISP_ARG_WS:
                    if( isWS( actChar ) ) break;    // Eat all WhiteSpace
                    prevState = state;
                    state = ISP_ARG_X;
                    input.backup(1);
                    break;
                    
                case ISP_EQ:
                    if (prevState == ISI_VAL_APOS || prevState == ISI_VAL_QUOT) {
                        state = prevState;
                        break;
                    }
                    if( isWS( actChar ) ) {
                        state = ISP_EQ_WS;
                        input.backup(1);
                        return token(XMLTokenId.OPERATOR);
                    }
                    int pSubstate = prevState;
                    switch( actChar ) {
                        case '\'':
                            prevState = ISI_VAL_APOS;
                            break;
                        case '"':
                            prevState = ISI_VAL_QUOT;
                            break;
                        case '<':
                            if (input.readLength() > 0) {
                                input.backup(1);
                            }
                            state = ISI_ERROR;
                            continue;
                        default:
                            if (prevState == ISP_EQ_WS && input.readLength() > 1) {
                                input.backup(1);
                                // erroneous whitespace
                            }
                            state = ISI_ERROR_TAG;
                            continue;
                            
                    }
                    input.backup(1);
                    if (pSubstate == ISP_EQ_WS) {
                        return token(XMLTokenId.WS);
                    }
                    return token(XMLTokenId.OPERATOR);
                    
                case ISP_EQ_WS:
                    if( isWS( actChar ) ) break;    // Consume all WS
                    prevState = state;
                    state = ISP_EQ;
                    input.backup(1);
                    break;
                    
                case ISI_VAL_APOS:
                    switch( actChar ) {
                        case '\'':
                            state = ISP_TAG_X;
                            return token(XMLTokenId.VALUE);
                        case '&':
                            if(input.readLength() == 1) {
                                subState = state;
                                state = ISA_REF;
                                break;
                            } else {
                                input.backup(1);
                                return token(XMLTokenId.VALUE);
                            }
                        case '<':
                            // error / unterminated tag, but the next token should be
                            state = INIT;
                            input.backup(1);
                            if(input.readLength() > 0) {
                                return token(XMLTokenId.VALUE);
                            }
                            break;
                    }
                    break;  // else simply consume next char of VALUE
                    
                case ISI_VAL_QUOT:
                    switch( actChar ) {
                        case '"':
                            state = ISP_TAG_X;
                            return token(XMLTokenId.VALUE);
                        case '&':
                            if(input.readLength() == 1) {
                                subState = state;
                                state = ISA_REF;
                                break;
                            } else {
                                input.backup(1);
                                return token(XMLTokenId.VALUE);
                            }
                        case '<':
                            // error / unterminated tag, but the next token should be
                            state = INIT;
                            input.backup(1);
                            if(input.readLength() > 0) {
                                return token(XMLTokenId.VALUE);
                            }
                            break;
                    }
                    break;  // else simply consume next char of VALUE
                    
                    
                case ISA_SGML_ESCAPE:       // DONE
                    if (actChar == '[') {
                        state = ISA_LTEXBR;
                        break;
                    } else if( isAZ(actChar) ) {
                        state = ISI_SGML_DECL;
                        break;
                    }
                    switch( actChar ) {
                        case '-':
                            state = ISA_SGML_DASH;
                            break;
                        default:
                            state = ISI_TEXT;
                            continue;
                    }
                    break;
                    
                case ISA_LTEXBR:
                    if (actChar == 'C') {
                        state = ISA_LTEXBRC;
                        break;
                    } else {
                        state = ISI_TEXT;
                        continue;
                    }
                    
                case ISA_LTEXBRC:
                    if (actChar == 'D') {
                        state = ISA_LTEXBRCD;
                        break;
                    } else {
                        state = ISI_TEXT;
                        continue;
                    }
                    
                case ISA_LTEXBRCD:
                    if (actChar == 'A') {
                        state = ISA_LTEXBRCDA;
                        break;
                    } else {
                        state = ISI_TEXT;
                        continue;
                    }
                    
                case ISA_LTEXBRCDA:
                    if (actChar == 'T') {
                        state = ISA_LTEXBRCDAT;
                        break;
                    } else {
                        state = ISI_TEXT;
                        continue;
                    }
                    
                case ISA_LTEXBRCDAT:
                    if (actChar == 'A') {
                        state = ISA_LTEXBRCDATA;
                        break;
                    } else {
                        state = ISI_TEXT;
                        continue;
                    }
                    
                case ISA_LTEXBRCDATA:
                    if (actChar == '[') {
                        state = ISI_CDATA;
                        break;
                    } else {
                        state = ISI_TEXT;
                        continue;
                    }
                    
                case ISI_CDATA:
                    if (actChar == ']') {
                        state = ISA_CDATA_BR;
                        break;
                    }
                    
                case ISA_CDATA_BR:
                    if (actChar == ']') {
                        state = ISA_CDATA_BRBR;
                        break;
                    } else {
                        state = ISI_CDATA;
                        break;
                    }
                    
                case ISA_CDATA_BRBR:
                    if (actChar == '>') {
                        state = ISI_TEXT;           //It s allowed only in content
                        return token(XMLTokenId.CDATA_SECTION);
                    } else if (actChar == ']') {
                        // stay in the same state
                        break;
                    } else {
                        state = ISI_CDATA;
                        break;
                    }
                    
                    
                case ISA_SGML_DASH:       // DONE
                    switch( actChar ) {
                        case '-':
                            state = ISI_XML_COMMENT;
                            break;
                        default:
                            state=ISI_ERROR;
                            continue;
                    }
                    break;
                    
                case ISI_XML_COMMENT:        // DONE
                    switch( actChar ) {
                        case '-':
                            state = ISA_XML_COMMENT_DASH;
                            break;
//                            //create an XML comment token for each line of the comment - a workaround fix for performance bug #39446
//                            //this also causes a SyntaxtElement to be created for each line of the comment - see XMLSyntaxSupport.createElement:277
//                            //PENDING - this code can be removed after editor solve it somehow in their code
//                        case '\n':
//                            //leave the some state - we are still in an XML comment,
//                            //we just need to create a token for each line.
//                            return token(XMLTokenId.BLOCK_COMMENT);
                    }
                    break;
                    
                case ISA_XML_COMMENT_DASH:
                    switch( actChar ) {
                        case '-':
                            state = ISI_XML_COMMENT_WS;
                            break;
                        default:
                            state = ISI_XML_COMMENT;
                            continue;
                    }
                    break;
                    
                case ISI_XML_COMMENT_WS:       // DONE
                    if( isWS( actChar ) ) break;  // Consume all WS
                    switch( actChar ) {
                        case '>':
                            state = INIT;
                            return token(XMLTokenId.BLOCK_COMMENT);
                        default:
                            state = ISI_ERROR;
                            input.backup(1);
                            return token(XMLTokenId.BLOCK_COMMENT);
                    }
                    
                case ISP_DECL_STRING:
                    if (actChar != '"') throw new IllegalStateException("Unexpected " + actChar);
                    state = ISI_DECL_STRING;
                    break;
                    
                case ISI_DECL_STRING:
                    if ( actChar == '"') {
                        state = ISI_SGML_DECL;
                        return token(XMLTokenId.VALUE);
                    }
                    break;
                    
                case ISP_DECL_CHARS:
                    if (actChar != '\'') throw new IllegalStateException("Unexpected " + actChar);
                    state = ISI_DECL_CHARS;
                    break;
                    
                case ISI_DECL_CHARS:
                    if ( actChar == '\'') {
                        state = ISI_SGML_DECL;
                        return token(XMLTokenId.VALUE);
                    }
                    break;
                    
                case ISI_SGML_DECL:
                    switch( actChar ) {
                        case '"':
                            state = ISP_DECL_STRING;
                            input.backup(1);
                            if (input.readLength() > 0)
                                return token(XMLTokenId.DECLARATION);
                            break;
                        case '\'':
                            state = ISP_DECL_CHARS;
                            input.backup(1);
                            if (input.readLength() > 0)
                                return token(XMLTokenId.DECLARATION);
                            break;
                        case '[':
                            state = INIT;
                            enterInternalDTD();
                            return token(XMLTokenId.DECLARATION);
                        case '>':
                            state = INIT;
                            return token(XMLTokenId.DECLARATION);
                    }
                    break;
                    
                case ISA_INIT_BR:
                    if (isWS(actChar)) break;
                    if (actChar == '>') {
                        state = INIT;
                        leaveInternalDTD();
                        return token(XMLTokenId.DECLARATION);
                    } else {
                        state = INIT;
                        input.backup(1);
                        if (input.readLength() > 0)
                            return token(XMLTokenId.ERROR);
                    }
                    break;
                    
                case ISA_SGML_DECL_DASH:
                    if( actChar == '-' ) {
                        state = ISI_ERROR;
                        break;
                    } else {
                        if(isWS(actChar)){
                            state = ISI_ERROR;
                            continue;
                        } else {
                            state = ISI_SGML_DECL;
                            continue;
                        }
                    }
                    
                case ISA_REF:
                    if( UnicodeClasses.isXMLNameStartChar( actChar ) ) {
                        state = ISI_REF_NAME;
                        break;
                    }
                    if( actChar == '#') {
                        state = ISA_REF_HASH;
                        break;
                    }
                    // get back to &, proclaim as character, although not according to spec.
                    input.backup(1);
                    state = subState;
                    continue;
                    
                case ISI_REF_NAME:
                    if( UnicodeClasses.isXMLNameChar( actChar ) ) break;
                    if( actChar != ';' ) input.backup(1);
                    state = subState;
                    return token(XMLTokenId.CHARACTER);
                    
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
                        return token(XMLTokenId.ERROR);
                    }
                    state = subState;
                    continue;
                    
                case ISI_REF_DEC:
                    if( actChar >= '0' && actChar <= '9' ) break;
                    if( actChar != ';' ) input.backup(1);
                    state = subState;
                    return token(XMLTokenId.CHARACTER);
                    
                case ISA_REF_X:
                    if (isHex(actChar)) {
                        state = ISI_REF_HEX;
                        break;
                    }
                    state = subState;
                    input.backup(1);
                    return token(XMLTokenId.ERROR);       // error on previous "&#x" sequence
                    
                case ISI_REF_HEX:
                    if (isHex(actChar)) break;
                    if (actChar != ';' ) input.backup(1);
                    state = subState;
                    return token(XMLTokenId.CHARACTER);
            }
        } // end of while(offset...)
        
        switch( state ) {
            case INIT:
            case ISI_TEXT:
            case ISA_LT:
            case ISA_SLASH:
            case ISA_SGML_ESCAPE:
            case ISA_SGML_DASH:
                return token(XMLTokenId.TEXT);
                
            case ISA_REF:
            case ISA_REF_HASH:
                if( subState == ISI_TEXT ) return token(XMLTokenId.TEXT);
                else return token(XMLTokenId.VALUE);
                
            case ISI_XML_COMMENT:
            case ISA_XML_COMMENT_DASH:
            case ISI_XML_COMMENT_WS:
                return token(XMLTokenId.BLOCK_COMMENT);
                
            case ISI_TAG:
            case ISI_ENDTAG:
                return token(XMLTokenId.TAG);
                
            case ISI_ARG:
                return token(XMLTokenId.ARGUMENT);
                
            case ISI_ERROR:
                return token(XMLTokenId.ERROR);
                
            case ISP_ARG_WS:
            case ISP_TAG_WS:
            case ISP_ENDTAG_WS:
            case ISP_EQ_WS:
                return token(XMLTokenId.WS);
                
            case ISP_ARG_X:
            case ISP_TAG_X:
            case ISP_ENDTAG_X:
            case ISP_EQ:
                return token(XMLTokenId.WS);
                
            case ISI_VAL_APOS:
            case ISI_VAL_QUOT:
            case ISI_DECL_CHARS:
            case ISI_DECL_STRING:
                return token(XMLTokenId.VALUE);
                
            case ISI_SGML_DECL:
            case ISA_SGML_DECL_DASH:
            case ISP_DECL_STRING:
            case ISP_DECL_CHARS:
                return token(XMLTokenId.DECLARATION);
                
            case ISI_REF_NAME:
            case ISI_REF_DEC:
            case ISA_REF_X:
            case ISI_REF_HEX:
                return token(XMLTokenId.CHARACTER);
                
            case ISI_PI:
                return token(XMLTokenId.PI_START);
            case ISI_PI_TARGET:
                return token(XMLTokenId.PI_TARGET);
            case ISP_PI_TARGET_WS:
                return token(XMLTokenId.WS);
            case ISI_PI_CONTENT:
                return token(XMLTokenId.PI_CONTENT);
            case ISA_PI_CONTENT_QMARK:
            case ISP_PI_CONTENT_QMARK:
                // we are at end of the last buffer and expect that next char will be '>'
                return token(XMLTokenId.PI_END);
                
            case ISA_LTEXBR:
            case ISA_LTEXBRC:
            case ISA_LTEXBRCD:
            case ISA_LTEXBRCDA:
            case ISA_LTEXBRCDAT:
            case ISA_LTEXBRCDATA:
                return token(XMLTokenId.TEXT);
                
            case ISI_CDATA:
            case ISA_CDATA_BR:
            case ISA_CDATA_BRBR:
                return token(XMLTokenId.CDATA_SECTION);
                
            case ISA_INIT_BR:
                return token(XMLTokenId.TEXT);
                
            default:
                throw new IllegalStateException("Last buffer does not handle state " + state + "!");    //NOI18N
        }
        
    }
    
    private Token<XMLTokenId> token(XMLTokenId id) {
//        System.out.print("--- token(" + id + "; '" + input.readText().toString() + "')");
//        if(input.readLength() == 0) {
//            System.out.println("XMLLexer error - zero length token!");
//        }
        Token<XMLTokenId> t = tokenFactory.createToken(id);
//        System.out.println(t.id() + "; " + t.length());
        return t;
    }
    
    private boolean isHex(int ch) {
        return (ch >= '0' && ch <= '9') || isAF(ch);
    }
    
    private boolean isAF(int ch) {
        return( (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F') );
    }

    public void release() {
    }

}
