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
    private final TokenFactory<TplTokenId> tokenFactory;
    private final InputAttributes inputAttributes;
    private int lexerState = INIT;
    
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


    /**
     * Create new TplLexer.
     * @param info from which place it should start again
     */
    public TplLexer(LexerRestartInfo<TplTokenId> info) {
        this.input = info.input();
        this.inputAttributes = info.inputAttributes();
        this.tokenFactory = info.tokenFactory();
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
        return Character.isWhitespace(character);
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
                        case '$':
                            lexerState = ISA_DOLLAR;
                            break;
                        default:
                            lexerState = ISI_TEXT;
                            break;
                    }
                    break;

                case ISA_DOLLAR:            // '$_'
                    if (Character.isJavaIdentifierStart(actChar)) {
                        lexerState = ISI_VAR_PHP;
                    } else {
                        input.backup(1);
                        lexerState = ISI_ERROR;
                    }
                    break;

                case ISI_VAR_PHP:           // '$a_'
                    if (isVariablePart(actChar)) {
                        break;    // Still in tag identifier, eat next char
                    }
                    lexerState = ISP_VAR_PHP_X;
                    if(input.readLength() > 1) { //lexer restart check, token already returned before last EOF
                        input.backup(1);
                        return token(TplTokenId.PHP_VARIABLE);
                    }
                    break;

                case ISP_VAR_PHP_X:         // '$var _', '$var?|'
                    if( isWS( actChar ) ) {
                        break;
                    }
                    switch( actChar ) {
                        case '|':           // Pipe after vairable, e.g. $var|
                            lexerState = ISA_PIPE;
                            return token(TplTokenId.PIPE);
                        case EOF:           // End of command, e.g. {$var}, {$var }
                            lexerState = INIT;
                            break;
                        default:
                            lexerState = ISI_ERROR;
                            input.backup(1);
                            break;
                    }
                    break;

                case ISI_ERROR:
                    lexerState = INIT;
                    return token(TplTokenId.ERROR);

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
}
