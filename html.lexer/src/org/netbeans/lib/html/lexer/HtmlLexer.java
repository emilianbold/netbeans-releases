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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.lib.html.lexer;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
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

public final class HtmlLexer implements Lexer<HTMLTokenId> {

    private static final Logger LOGGER = Logger.getLogger(HtmlLexer.class.getName());
    private static final boolean LOG = Boolean.getBoolean("j2ee_lexer_debug"); //NOI18N

    private static final int EOF = LexerInput.EOF;

    private final LexerInput input;

    private final TokenFactory<HTMLTokenId> tokenFactory;

    class CompoundState {
        private int lexerState;
        private int lexerSubState;
        private int lexerEmbeddingState;
        private String jsAttributeName;
        private String cssAttributeName;

        public CompoundState(int lexerState, int lexerSubState, int lexerEmbeddingState, String jsAttributeName, String cssAttributeName) {
            this.lexerState = lexerState;
            this.lexerSubState = lexerSubState;
            this.lexerEmbeddingState = lexerEmbeddingState;
            this.jsAttributeName = jsAttributeName;
            this.cssAttributeName = cssAttributeName;
        }

        @Override
        public String toString() {
           // return "state=" + (lexerSubState * 1000000 + lexerState * 1000 + lexerEmbeddingState) + "," + attributeName.toString();
           int state = lexerSubState * 1000000 + lexerState * 1000 + lexerEmbeddingState;
           return Integer.toString(state)
                   + (jsAttributeName != null ? "," + jsAttributeName.toString() : "")
                   + (cssAttributeName != null ? "," + cssAttributeName.toString() : "");
        }
    }

    public Object state() {
        if (jsAttributeName != null || cssAttributeName != null) {
            return new CompoundState(lexerState, lexerSubState, lexerEmbeddingState, jsAttributeName, cssAttributeName);
        } else {
            return lexerSubState * 1000000 + lexerState * 1000 + lexerEmbeddingState;
        }
    }

    //script and style tag names
    private static final String SCRIPT = "script";
    private static final String STYLE = "style";

    private static final String STYLE_ATTR = "style";

    /** Internal state of the lexical analyzer before entering subanalyzer of
     * character references. It is initially set to INIT, but before first usage,
     * this will be overwritten with state, which originated transition to
     * charref subanalyzer.
     */
    private int lexerSubState = INIT;
    private int lexerState    = INIT;
    private String jsAttributeName;
    private String cssAttributeName;

    /** indicated whether we are in a script */
    private int lexerEmbeddingState = INIT;

    // internal 'in script' state. 'scriptState' internal state is set to it when the
    // analyzer goes into a script tag body
    private static final int ISI_SCRIPT = 1;
    private static final int ISI_STYLE = 2;

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

    private static final int ISI_STYLE_CONTENT = 38; //after <style> tags closing symbol '>' - the tag content
    private static final int ISI_STYLE_CONTENT_AFTER_LT = 39; //after < in style content
    private static final int ISI_STYLE_CONTENT_ENDTAG = 40; //after </ in style content

    private static final int ISI_SGML_DECL_WS = 41; //after whitespace in SGML declaration

    static final Set<String> EVENT_HANDLER_NAMES = new HashSet<String>();
    static {
        // See http://www.w3.org/TR/html401/interact/scripts.html
        EVENT_HANDLER_NAMES.add("onload"); // NOI18N
        EVENT_HANDLER_NAMES.add("onunload"); // NOI18N
        EVENT_HANDLER_NAMES.add("onclick"); // NOI18N
        EVENT_HANDLER_NAMES.add("ondblclick"); // NOI18N
        EVENT_HANDLER_NAMES.add("onmousedown"); // NOI18N
        EVENT_HANDLER_NAMES.add("onmouseup"); // NOI18N
        EVENT_HANDLER_NAMES.add("onmouseover"); // NOI18N
        EVENT_HANDLER_NAMES.add("onmousemove"); // NOI18N
        EVENT_HANDLER_NAMES.add("onmouseout"); // NOI18N
        EVENT_HANDLER_NAMES.add("onfocus"); // NOI18N
        EVENT_HANDLER_NAMES.add("onblur"); // NOI18N
        EVENT_HANDLER_NAMES.add("onkeypress"); // NOI18N
        EVENT_HANDLER_NAMES.add("onkeydown"); // NOI18N
        EVENT_HANDLER_NAMES.add("onkeyup"); // NOI18N
        EVENT_HANDLER_NAMES.add("onsubmit"); // NOI18N
        EVENT_HANDLER_NAMES.add("onreset"); // NOI18N
        EVENT_HANDLER_NAMES.add("onselect"); // NOI18N
        EVENT_HANDLER_NAMES.add("onchange"); // NOI18N

        // IMPORTANT - if you add any that DON'T start with "o" here,
        // make sure you update the optimized firstchar look in isJavaScriptArgument
    }

    public HtmlLexer(LexerRestartInfo<HTMLTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        if (info.state() == null) {
            this.lexerSubState = INIT;
            this.lexerState = INIT;
            this.lexerEmbeddingState = INIT;
        } else {
            Object state = info.state();
            if (state instanceof CompoundState) {
                CompoundState cs = (CompoundState)state;
                lexerState = cs.lexerState;
                lexerSubState = cs.lexerSubState;
                lexerEmbeddingState = cs.lexerEmbeddingState;
                jsAttributeName = cs.jsAttributeName;
                cssAttributeName = cs.cssAttributeName;
            } else {
                int encoded = ((Integer) info.state()).intValue();
                this.lexerSubState = encoded / 1000000;
                int remainder = encoded % 1000000;
                this.lexerState    = remainder / 1000;
                this.lexerEmbeddingState = remainder % 1000;
            }
        }
    }

    private final boolean isAZ( int character ) {
        return( (character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z') );
    }

    private final boolean isName( int character ) {
        return Character.isLetterOrDigit(character) ||
                character == '-' || character == '_' || character == '.' || character == ':';
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
    }

    private boolean isJavascriptEventHandlerName(CharSequence attributeName) {
        if(attributeName.length() > 2) {
            char firstChar = attributeName.charAt(0);
            char secondChar = attributeName.charAt(1);
            if((firstChar == 'o' || firstChar == 'O') &&
                    (secondChar == 'n' || secondChar == 'N')) {
                return EVENT_HANDLER_NAMES.contains(attributeName.toString().toLowerCase(Locale.ENGLISH));
            }
        }
        return false;
    }

    private boolean isStyleAttributeName(CharSequence chs) {
        if(chs.length() == STYLE_ATTR.length()) {
            for(int i = 0; i < chs.length(); i++) {
                if(Character.toLowerCase(chs.charAt(i)) != Character.toLowerCase(STYLE_ATTR.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean followsCloseTag(CharSequence closeTagName) {
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

                if(equals(closeTagName, tagName, true, true)) {
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
                        if(input.readLength() > 1) { //lexer restart check, token already returned before last EOF
                            input.backup(1);
                            return token(HTMLTokenId.TAG_OPEN_SYMBOL);
                        }
                        break;
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
                        default:
                            input.backup(1);
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
                    if(input.readLength() > 1) { //lexer restart check, token already returned before last EOF
                        input.backup(1);
                        return token(HTMLTokenId.TAG_CLOSE);
                    }
                    break;


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
                    if(input.readLength() > 1) { //lexer restart check, token already returned before last EOF
                        input.backup(1);
                        return token(HTMLTokenId.WS);
                    }
                    break;


                case ISI_TAG:        // DONE
                    if( isName( actChar ) ) break;    // Still in tag identifier, eat next char
                    lexerState = ISP_TAG_X;
                    if(input.readLength() > 1) { //lexer restart check, token already returned before last EOF
                        input.backup(1);
                        //test if the tagname is SCRIPT

                        if(equals(SCRIPT, input.readText(), true, true)) {
                            lexerEmbeddingState = ISI_SCRIPT;
                        }
                        if(equals(STYLE, input.readText(), true, true)) {
                            lexerEmbeddingState = ISI_STYLE;
                        }
                        return token(HTMLTokenId.TAG_OPEN);
                    }
                    break;

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
                            switch (lexerEmbeddingState) {
                                case INIT:
                                    lexerState = INIT;
                                    break;
                                case ISI_SCRIPT:
                                    lexerState = ISI_SCRIPT_CONTENT;
                                    break;
                                case ISI_STYLE:
                                    lexerState = ISI_STYLE_CONTENT;
                                    break;
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
                    if(input.readLength() > 1) { //lexer restart check, token already returned before last EOF
                        input.backup(1);
                        return token(HTMLTokenId.WS);
                    }

                case ISI_TAG_SLASH:
                    switch( actChar ) {
                        case '>':
                            lexerEmbeddingState = INIT; //possibly cancel 'in script' if empty tag found
                            lexerState = INIT;
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
                        if (followsCloseTag(SCRIPT)) {
                            //end of script section found
                            lexerEmbeddingState = INIT;
                            lexerState = INIT;
                            input.backup(input.readLength() > 2 ? 2 : input.readLength()); //backup the '</', we will read it again
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

                case ISI_STYLE_CONTENT:
                    switch( actChar ) {
                        case '<' :
                            lexerState = ISI_STYLE_CONTENT_AFTER_LT;
                            break;
                        default:
                            break;
                    }
                    break;

                case ISI_STYLE_CONTENT_AFTER_LT:
                    if (actChar == '/') {
                        if (followsCloseTag(STYLE)) {
                            //end of script section found
                            lexerEmbeddingState = INIT;
                            lexerState = INIT;
                            input.backup(input.readLength() > 2 ? 2 : input.readLength()); //backup the '</', we will read it again
                            if (input.readLength() > 0) {
                                //the script has a body
                                return token(HTMLTokenId.STYLE);
                            } else {
                                break;
                            }
                        }
                    }
                    lexerState = ISI_STYLE_CONTENT;
                    break;

                case ISI_ARG:           // DONE
                    if( isName( actChar ) ) break; // eat next char
                    lexerState = ISP_ARG_X;
                    if(input.readLength() > 1) { //lexer restart check, token already returned before last EOF
                        input.backup(1);
                        jsAttributeName = isJavascriptEventHandlerName(input.readText()) ? input.readText().toString() : null;
                        cssAttributeName = isStyleAttributeName(input.readText()) ? input.readText().toString() : null;
                        return token(HTMLTokenId.ARGUMENT);
                    }
                    break;

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
                    if(input.readLength() > 1) { //lexer restart check, token already returned before last EOF
                        input.backup(1);
                        return token(HTMLTokenId.WS);
                    }
                    break;

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
                    if(input.readLength() > 1) { //lexer restart check, token already returned before last EOF
                        input.backup(1);
                        return token(HTMLTokenId.WS);
                    }
                    break;


                case ISI_VAL:
                    if( !isWS( actChar )
                    && !(actChar == '/' || actChar == '>' || actChar == '<')) break;  // Consume whole value
                    lexerState = ISP_TAG_X;
                    if(input.readLength() > 1) { //lexer restart check, token already returned before last EOF
                        input.backup(1);
                        if (jsAttributeName != null) {
                            jsAttributeName = null;
                            return token(HTMLTokenId.VALUE_JAVASCRIPT);
                        }
                        jsAttributeName = null;
                         if (cssAttributeName != null) {
                            cssAttributeName = null;
                            return token(HTMLTokenId.VALUE_CSS);
                        }
                        cssAttributeName = null;
                        return token(HTMLTokenId.VALUE);
                    }
                    break;

                case ISI_VAL_QUOT:
                    switch( actChar ) {
                        case '\'':
                            lexerState = ISP_TAG_X;
                            if (jsAttributeName != null) {
                                jsAttributeName = null;
                                return token(HTMLTokenId.VALUE_JAVASCRIPT);
                            }
                            jsAttributeName = null;
                            if (cssAttributeName != null) {
                                cssAttributeName = null;
                                return token(HTMLTokenId.VALUE_CSS);
                            }
                            cssAttributeName = null;
                            return token(HTMLTokenId.VALUE);

//                        Workaround for [Issue 117450]  Provide unified LexerInput across multiple joined embedded sections
//                        The problem is described in detail in issue [Issue 118892]  Allow Schlieman lexer to continuously lex embedded language over  more tokens of its parent language
//                        Should be removed once the issue is fixed.
//
//                        case '&':
//                            if( input.readLength() == 1 ) {
//                                lexerSubState = lexerState;
//                                lexerState = ISA_REF;
//                                break;
//                            } else {
//                                if(input.readLength() > 1) { //lexer restart check, token already returned before last EOF
//                                    input.backup(1);
//                                    return token(HTMLTokenId.VALUE);
//                                }
//                            }
                    }
                    break;  // else simply consume next char of VALUE

                case ISI_VAL_DQUOT:
                    switch( actChar ) {
                        case '"':
                            lexerState = ISP_TAG_X;
                            if (jsAttributeName != null) {
                                jsAttributeName = null;
                                return token(HTMLTokenId.VALUE_JAVASCRIPT);
                            }
                            jsAttributeName = null;
                            if (cssAttributeName != null) {
                                cssAttributeName = null;
                                return token(HTMLTokenId.VALUE_CSS);
                            }
                            cssAttributeName = null;
                            return token(HTMLTokenId.VALUE);

//                        Workaround for [Issue 117450]  Provide unified LexerInput across multiple joined embedded sections
//                        The problem is described in detail in issue [Issue 118892]  Allow Schlieman lexer to continuously lex embedded language over  more tokens of its parent language
//                        Should be removed once the issue is fixed.
//
//                        case '&':
//                            if( input.readLength() == 1 ) {
//                                lexerSubState = lexerState;
//                                lexerState = ISA_REF;
//                                break;
//                            } else {
//                                if(input.readLength() > 1) { //lexer restart check, token already returned before last EOF
//                                    input.backup(1);
//                                    return token(HTMLTokenId.VALUE);
//                                }
//                            }
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
                    switch( actChar ) {
                        case '>':
                            lexerState = INIT;
                            return token(HTMLTokenId.BLOCK_COMMENT);
                        default:
                            lexerState = ISI_HTML_COMMENT;
                            input.backup(2); //backup everything except the first comma
                            break;
                    }
                    break;

                case ISI_SGML_DECL:
                    if(Character.isWhitespace(actChar)) {
                        lexerState = ISI_SGML_DECL_WS;
                        if(input.readLength() > 1) {
                            input.backup(1); //backup the whitespace
                            return token(HTMLTokenId.DECLARATION);
                        }
                        break;
                    }
                    switch( actChar ) {
                        case '>':
                            if(input.readLength() > 1) {
                                input.backup(1); //backup the '<' char
                                return token(HTMLTokenId.DECLARATION);
                            } else {
                                //just the symbol read - return it as a part of declaration
                                lexerState = INIT;
                                return token(HTMLTokenId.DECLARATION);
                            }

                    }
                    break;

                case ISI_SGML_DECL_WS:
                    if(actChar == '-') {
                            if( input.readLength() == 1 ) {
                                lexerState = ISA_SGML_DECL_DASH;
                                break;
                            } else {
                                if(input.readLength() > 1) { //lexer restart check, token already returned before last EOF
                                    input.backup(1);
                                    return token(HTMLTokenId.DECLARATION);
                                }
                            }
                    } else if(!Character.isWhitespace(actChar)) {
                        lexerState = ISI_SGML_DECL;
                        input.backup(1);
                        return token(HTMLTokenId.WS);
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
                    lexerState = lexerSubState;
                    if(input.readLength() > 1) { //lexer restart check, token already returned before last EOF
                        if( actChar != ';' ) {
                            input.backup(1);
                        }
                        return token(HTMLTokenId.CHARACTER);
                    }
                    break;

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
                    lexerState = lexerSubState;
                    if(input.readLength() > 1) { //lexer restart check, token already returned before last EOF
                        if( actChar != ';' )
                            input.backup(1);
                        return token(HTMLTokenId.CHARACTER);
                    }
                    break;

                case ISA_REF_X:
                    if( (actChar >= '0' && actChar <= '9') ||
                            (actChar >= 'a' && actChar <= 'f') ||
                            (actChar >= 'A' && actChar <= 'F')
                            ) {
                        lexerState = ISI_REF_HEX;
                        break;
                    }
                    lexerState = lexerSubState;
                    if(input.readLength() > 1) { //lexer restart check, token already returned before last EOF
                        input.backup(1);
                        return token(HTMLTokenId.ERROR);       // error on previous "&#x" sequence
                    }
                    break;

                case ISI_REF_HEX:
                    if( (actChar >= '0' && actChar <= '9') ||
                            (actChar >= 'a' && actChar <= 'f') ||
                            (actChar >= 'A' && actChar <= 'F')
                            ) break;
                    lexerState = lexerSubState;
                    if(input.readLength() > 1) { //lexer restart check, token already returned before last EOF
                        if( actChar != ';' )
                            input.backup(1);
                        return token(HTMLTokenId.CHARACTER);
                    }
                    break;
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
                lexerState = ISP_TAG_X;
                //test if the tagname is SCRIPT
                if(equals(SCRIPT, input.readText(), true, true)) {
                    lexerEmbeddingState = ISI_SCRIPT;
                }
                if(equals(STYLE, input.readText(), true, true)) {
                    lexerEmbeddingState = ISI_STYLE;
                }
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
                if (jsAttributeName != null) {
                    return token(HTMLTokenId.VALUE_JAVASCRIPT);
                }
                if (cssAttributeName != null) {
                    return token(HTMLTokenId.VALUE_CSS);
                }
                return token(HTMLTokenId.VALUE);

            case ISI_SGML_DECL:
            case ISA_SGML_DECL_DASH:
            case ISI_SGML_DECL_WS:
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
            case ISI_STYLE_CONTENT:
            case ISI_STYLE_CONTENT_ENDTAG:
            case ISI_STYLE_CONTENT_AFTER_LT:
                return token(HTMLTokenId.STYLE);


        }

        assert input.readLength() == 0 : "Returning null even if some chars still needs to be tokenized! " +
            "lexer state=" + lexerState + "; " +
            "lexer substate=" + lexerSubState + "; " +
            "lexer embedding state=" + lexerEmbeddingState + "; " +
            "readtext='" + input.readText() + "'";

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

    /** @param optimized - first sequence is lowercase, one call to Character.toLowerCase() */
    private boolean equals(CharSequence text1, CharSequence text2, boolean ignoreCase, boolean optimized) {
        if (text1.length() != text2.length()) {
            return false;
        } else {
            //compare content
            for (int i = 0; i < text1.length(); i++) {
                char ch1 = ignoreCase && !optimized ? Character.toLowerCase(text1.charAt(i)) : text1.charAt(i);
                char ch2 = ignoreCase ? Character.toLowerCase(text2.charAt(i)) : text2.charAt(i);
                if (ch1 != ch2) {
                    return false;
                }
            }
            return true;
        }
    }

}
