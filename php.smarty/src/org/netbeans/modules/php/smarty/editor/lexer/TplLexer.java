/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.php.smarty.editor.lexer;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Lexical analyzer for FUSE tpl templates
 * 
 * @author Martin Fousek
 */
public class TplLexer implements Lexer<TplTokenId> {

    private static final int EOF = LexerInput.EOF;
    private final LexerInput input;
    private String keyword;
    private boolean argValue;
    private boolean endingTag;
    private final TokenFactory<TplTokenId> tokenFactory;
    private final InputAttributes inputAttributes;
    private int lexerState = INIT;

    private final String phpEmbeddingDelimiter = "php";

    // Internal states
    private static final int INIT = 0;
    private static final int ISI_TEXT = 1;          // Plain text
    private static final int ISI_ERROR = 2;         // Syntax error in TPL syntax
    private static final int ISA_DOLLAR = 3;        // After dollar char - "$_"
    private static final int ISI_VAR_PHP = 4;       // PHP-like variables - "$v_" "$va_"
    private static final int ISP_VAR_PHP_X = 5;     // X-switch after variable name
    private static final int ISI_VAR_CONFIG = 6;    // CONFIG-like variables - "#var#"
    private static final int ISA_PIPE = 7;          // Is after pipe - "$var|_"
    private static final int ISI_PIPE = 8;          // Is in pipe syntax - "$var|da_"
    private static final int ISA_WS = 9;            // Is after whitespace - " _"
    private static final int ISA_HASH = 10;         // Is after hash - "#_"
    private static final int ISA_STAR = 11;         // Is after hash - "*_" "* _"
    private static final int ISI_QUOT = 12;         // Is in quot - "'_" "'asd_"
    private static final int ISI_DQUOT = 13;        // Is in double quot - "\"_" "\"asdfasd_"
    private static final int ISI_PHP = 14;          // Is in PHP code - "{php}_" "php _"



    static final Set<String> VARIABLE_MODIFIERS = new HashSet<String>();
    static {
        // See http://www.smarty.net/manual/en/language.modifiers.php
        VARIABLE_MODIFIERS.add("capitalize"); // NOI18N
        VARIABLE_MODIFIERS.add("cat"); // NOI18N
        VARIABLE_MODIFIERS.add("count_characters"); // NOI18N
        VARIABLE_MODIFIERS.add("count_paragraphs"); // NOI18N
        VARIABLE_MODIFIERS.add("count_sentences"); // NOI18N
        VARIABLE_MODIFIERS.add("count_words"); // NOI18N
        VARIABLE_MODIFIERS.add("date_format"); // NOI18N
        VARIABLE_MODIFIERS.add("default"); // NOI18N
        VARIABLE_MODIFIERS.add("escape"); // NOI18N
        VARIABLE_MODIFIERS.add("indent"); // NOI18N
        VARIABLE_MODIFIERS.add("lower"); // NOI18N
        VARIABLE_MODIFIERS.add("nl2br"); // NOI18N
        VARIABLE_MODIFIERS.add("regex_replace"); // NOI18N
        VARIABLE_MODIFIERS.add("replace"); // NOI18N
        VARIABLE_MODIFIERS.add("spacify"); // NOI18N
        VARIABLE_MODIFIERS.add("string_format"); // NOI18N
        VARIABLE_MODIFIERS.add("strip"); // NOI18N
        VARIABLE_MODIFIERS.add("strip_tags"); // NOI18N
        VARIABLE_MODIFIERS.add("truncate"); // NOI18N
        VARIABLE_MODIFIERS.add("upper"); // NOI18N
        VARIABLE_MODIFIERS.add("wordwrap"); // NOI18N
    }

    static final Set<String> OPERATORS = new HashSet<String>();
    static {
        // See http://www.smarty.net/manual/en/language.function.if.php
        OPERATORS.add("div"); // NOI18N
        OPERATORS.add("by"); // NOI18N
        OPERATORS.add("even"); // NOI18N
        OPERATORS.add("is"); // NOI18N
        OPERATORS.add("not"); // NOI18N
        OPERATORS.add("odd"); // NOI18N
        OPERATORS.add("eq"); // NOI18N
        OPERATORS.add("ge"); // NOI18N
        OPERATORS.add("gt"); // NOI18N
        OPERATORS.add("gte"); // NOI18N
        OPERATORS.add("le"); // NOI18N
        OPERATORS.add("lt"); // NOI18N
        OPERATORS.add("lte"); // NOI18N
        OPERATORS.add("mod"); // NOI18N
        OPERATORS.add("ne"); // NOI18N
        OPERATORS.add("neq"); // NOI18N
        OPERATORS.add("not"); // NOI18N
    }

    static final Set<String> FUNCTIONS = new HashSet<String>();
    static {
        // See http://www.smarty.net/manual/en/language.builtin.functions.php,
        //     http://www.smarty.net/manual/en/language.custom.functions.php
        FUNCTIONS.add("capture"); // NOI18N
        FUNCTIONS.add("config_load"); // NOI18N
        FUNCTIONS.add("foreach"); // NOI18N,
        FUNCTIONS.add("foreachelse"); // NOI18N
        FUNCTIONS.add("if"); // NOI18N,
        FUNCTIONS.add("elseif"); // NOI18N,
        FUNCTIONS.add("else"); // NOI18N
        FUNCTIONS.add("include"); // NOI18N
        FUNCTIONS.add("include_php"); // NOI18N
        FUNCTIONS.add("insert"); // NOI18N
        FUNCTIONS.add("ldelim"); // NOI18N,
        FUNCTIONS.add("rdelim"); // NOI18N
        FUNCTIONS.add("literal"); // NOI18N
//        FUNCTIONS.add("php"); // NOI18N
        FUNCTIONS.add("section"); // NOI18N,
        FUNCTIONS.add("sectionelse"); // NOI18N
        FUNCTIONS.add("strip"); // NOI18N
        FUNCTIONS.add("assign"); // NOI18N
        FUNCTIONS.add("counter"); // NOI18N
        FUNCTIONS.add("cycle"); // NOI18N
        FUNCTIONS.add("debug"); // NOI18N
        FUNCTIONS.add("eval"); // NOI18N
        FUNCTIONS.add("fetch"); // NOI18N
        FUNCTIONS.add("html_checkboxes"); // NOI18N
        FUNCTIONS.add("html_image"); // NOI18N
        FUNCTIONS.add("html_options"); // NOI18N
        FUNCTIONS.add("html_radios"); // NOI18N
        FUNCTIONS.add("html_select_date"); // NOI18N
        FUNCTIONS.add("html_select_time"); // NOI18N
        FUNCTIONS.add("html_table"); // NOI18N
        FUNCTIONS.add("mailto"); // NOI18N
        FUNCTIONS.add("math"); // NOI18N
        FUNCTIONS.add("popup"); // NOI18N
        FUNCTIONS.add("popup_init"); // NOI18N
        FUNCTIONS.add("textformat"); // NOI18N
    }

    /**
     * Create new TplLexer.
     * @param info from which place it should start again
     */
    public TplLexer(LexerRestartInfo<TplTokenId> info) {
        this.input = info.input();
        this.inputAttributes = info.inputAttributes();
        this.tokenFactory = info.tokenFactory();
        this.keyword = "";
        this.argValue = false;
        this.endingTag = false;
        if (info.state() == null) {
            this.lexerState = INIT;
        } else {
            lexerState = (Integer) info.state();
        }
    }

    public Object state() {
        return lexerState; // always in default state after token recognition
    }

    private final boolean isVariablePart(int character) {
        return Character.isJavaIdentifierPart(character);
    }

    private final boolean isWS(int character) {
        if (Character.isWhitespace(character)) {
            return true;
        }
        return false;
    }

    private final boolean isEndOfWord(int character) {
        if (Character.isWhitespace(character) || character == '-' || character == '|' ||
                character == '.' || character == ':' || character == '=' ||
                character == '<' || character == '!' || character == '>') {
            return true;
        }
        return false;
    }

    public Token<TplTokenId> nextToken() {
        int actChar;

        while (true) {
            actChar = input.read();

            if (actChar == EOF) {
                if (input.readLengthEOF() == 1) {
                    return null; //just EOL is read
                }
            }
            switch (lexerState) {
                case INIT:
                    switch (actChar) {
                        case '$':           // Dollar, e.g. $
                            lexerState = ISA_DOLLAR;
                            break;
                        case '#':           // Hash, e.g. #
                            lexerState = ISA_HASH;
                            break;
                        case '*':           // Hash, e.g. *
                            lexerState = ISA_STAR;
                            break;
                        case '\'':          
                            lexerState = ISI_QUOT;
                            break;
                        case '/':
                            endingTag = true;
                            break;
                        case '"':
                            lexerState = ISI_DQUOT;
                            break;
                        case '=':
                            argValue = true;
                            return token(TplTokenId.TEXT);
                        case '|':           // Pipe, e.g. $var|, ''|
                            return token(TplTokenId.PIPE);
                        case '\n':
                        case ' ':
                        case '\r':
                        case '\t':
                            lexerState = ISA_WS;
                            return token(TplTokenId.WHITESPACE);
                        case EOF:
                            return null;
                        default:
                            input.backup(1);
                            lexerState = ISI_TEXT;
                            break;
                    }
                    break;

                case ISA_DOLLAR:            // '$_'
                    argValue = false;
                    if (Character.isJavaIdentifierStart(actChar)) {
                        lexerState = ISI_VAR_PHP;
                    } else {
                        input.backup(1);
                        lexerState = ISI_ERROR;
                    }
                    break;

                case ISA_HASH:            // '#_'
                    argValue = false;
                    if (Character.isJavaIdentifierPart(actChar)) {
                        break;
                    } else if (actChar == '#' && input.readLength() > 2) {
                        lexerState = INIT;
                        return token(TplTokenId.CONFIG_VARIABLE);
                    } else {
                        return token(TplTokenId.ERROR);
                    }

                case ISA_STAR:            // '*_', '* afssd_'
                    if (actChar == '*' || actChar == EOF) {
                        lexerState = INIT;
                        return token(TplTokenId.COMMENT);
                    }
                    break;

                case ISI_QUOT:            // ''_', '' afssd_'
                    argValue = false;
                    if (actChar == '\'' || actChar == EOF) {
                        lexerState = INIT;
                        return token(TplTokenId.STRING);
                    }
                    break;

                case ISI_DQUOT:            // '"_', '" afssd_'
                    argValue = false;
                    if (actChar == '"' || actChar == EOF) {
                        lexerState = INIT;
                        return token(TplTokenId.STRING);
                    }
                    break;

                case ISI_VAR_PHP:           // '$a_'
                    if (isVariablePart(actChar)) {
                        break;    // Still in tag identifier, eat next char
                    }
                    lexerState = INIT;
                    if(input.readLength() > 1) { //lexer restart check, token already returned before last EOF
                        input.backup(1);
                        return token(TplTokenId.PHP_VARIABLE);
                    }
                    break;

                case ISA_WS:         // '$var _', '$var?|'
                    if( isWS( actChar ) ) {
                        return token(TplTokenId.WHITESPACE);
                    }
                    input.backup(1);
                    lexerState = INIT;
                    break;

                case ISI_ERROR:
                    lexerState = INIT;
                    return token(TplTokenId.ERROR);

                case ISI_PHP:
                    return token(TplTokenId.PHP_EMBEDDING);

                case ISI_TEXT:
                    if( isVariablePart(actChar) ) {
                        keyword += Character.toString((char)actChar);
                        break;
                    } else if (input.readLength() == 1) {
                        lexerState = INIT;
                        return token(TplTokenId.WHITESPACE);
                    }
                    input.backup(1);
                    TplTokenId tokenId = resolveStringToken(keyword);
                    keyword = "";
                    lexerState = INIT;
                    return token(tokenId);

                default:
                    return token(TplTokenId.TEXT);
            } // end of switch (c)
        } // end of while(true)

//        return token(TplTokenId.TEXT);
    }

    public void release() {
    }

    private Token<TplTokenId> token(TplTokenId tplTokenId) {
        return tokenFactory.createToken(tplTokenId);
    }

    private TplTokenId resolveStringToken(String keyword) {
        // check variable modifiers 
        if (isVariableModifier(keyword)) {
            return TplTokenId.VARIABLE_MODIFIER;
        }

        // check operators
        if (isVariableOperator(keyword)) {
            return TplTokenId.OPERATOR;
        }

        // check start and end of embedded PHP code
        if (keyword.toString().toLowerCase(Locale.ENGLISH).equals(phpEmbeddingDelimiter)) {
            if (endingTag) {
                lexerState = INIT;
                return TplTokenId.PHP_EMBEDDING_DEL;
            }
            else {
                lexerState = ISI_PHP;
                return TplTokenId.PHP_EMBEDDING_DEL;
            }
        }

        // check functions
        if (isSmartyFunction(keyword)) {
            endingTag = false;
            return TplTokenId.FUNCTION;
        }

        // check if it's argument of its value
        if (argValue) {
            return TplTokenId.ARGUMENT_VALUE;
        }
        else {
            return TplTokenId.ARGUMENT;
        }
    }

    private boolean  isVariableModifier(String keyword) {
        return VARIABLE_MODIFIERS.contains(keyword.toString().toLowerCase(Locale.ENGLISH));
    }

    private boolean isVariableOperator(String keyword) {
        return OPERATORS.contains(keyword.toString().toLowerCase(Locale.ENGLISH));
    }

    private boolean isSmartyFunction(String keyword) {
        return FUNCTIONS.contains(keyword.toString().toLowerCase(Locale.ENGLISH));
    }

}
