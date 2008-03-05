/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.lexer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.php.editor.PHPVersion;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;



/**
 *
 * @author Petr Pisl
 */
public class GSFPHPLexer implements Lexer<PHPTokenId> {
    
    // empty buffer
    private static final StringReader EMPTY_STRING_READER = new StringReader("");
    
    /** This is still not working; I wonder if release() is called correctly at all times...*/
    private static final boolean REUSE_LEXERS = false;
    
    private static GSFPHPLexer cached;
    private final PHPScanner scanner;
    private LexerInput input;
    private TokenFactory<PHPTokenId> tokenFactory;    
    
    private GSFPHPLexer(LexerRestartInfo<PHPTokenId> info) {
        scanner = PHPScannerManager.getDefault().getPHPScanner(PHPVersion.PHP_5, EMPTY_STRING_READER, false);
    }
    
    public static synchronized GSFPHPLexer create(LexerRestartInfo<PHPTokenId> info) {
        GSFPHPLexer phpLexer = cached;

        if (phpLexer == null) {
            phpLexer = new GSFPHPLexer(info);
        }

        phpLexer.restart(info);

        return phpLexer;
    }
    
    void restart(LexerRestartInfo<PHPTokenId> info) {

        input = info.input();
        Reader lexerReader = new LexerInputReader(input);
        scanner.reset(lexerReader);
        
        tokenFactory = info.tokenFactory();

        Object state = info.state();
        if (state instanceof PHP5ColoringLexer.LexerState) {
            scanner.setState((PHP5ColoringLexer.LexerState)state);
        }
        
    }
    
    private Token<PHPTokenId> createToken(PHPTokenId id, int length) {
        String fixedText = id.fixedText();

        return (fixedText != null) ? tokenFactory.getFlyweightToken(id, fixedText)
                                   : tokenFactory.createToken(id, length);
    }
    
    public Token<PHPTokenId> nextToken() {
        try {
            PHPTokenId symbol = scanner.nextToken(); 
            Token<PHPTokenId> token = null;
            if (symbol != null) {
                token = createToken(symbol, scanner.getTokenLength());
            }
            return token;
        } catch (IOException ex) {
            Logger.getLogger(GSFPHPLexer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Object state() {
        Object state = scanner.getState();
        return state;
    }

    public void release() {
        if (REUSE_LEXERS) {
            // Possibly reset the structures that could cause memory leaks
            synchronized (GSFPHPLexer.class) {
                cached = this;
            }
        }
    }
    
    private static class LexerInputReader extends Reader {
        private LexerInput input;

        LexerInputReader(LexerInput input) {
            this.input = input;
        }

        public int read(char[] buf, int off, int len) throws IOException {
            for (int i = 0; i < len; i++) {
                int c = input.read();
                if (c == LexerInput.EOF) {
                    if (i > 0) {
                        return i;
                    }
                    else {
                        return -1;
                    }
                }
                buf[i + off] = (char)c;
            }
            return len;
        }

        public void close() throws IOException {
        }
    }

}
