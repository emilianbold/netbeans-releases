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

package org.netbeans.modules.groovy.editor.lexer;

import groovyjarjarantlr.CharBuffer;
import groovyjarjarantlr.CharQueue;
import groovyjarjarantlr.CharStreamException;
import groovyjarjarantlr.LexerSharedInputState;
import groovyjarjarantlr.TokenStreamException;
import java.io.IOException;
import java.io.Reader;
import org.codehaus.groovy.antlr.parser.GroovyRecognizer;
import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Lexer based on old coyote groovy lexer.
 * 
 * @author Mila Metelka
 * @author Martin Adamek
 */
final class GroovyLexer implements Lexer<GroovyTokenId> {
    
    private GroovyScanner scanner;
    private LexerInput lexerInput;
    private MyCharBuffer myCharBuffer;
    private TokenFactory<GroovyTokenId> tokenFactory;
    
    public GroovyLexer(LexerRestartInfo<GroovyTokenId> info) {
        this.scanner = new GroovyScanner((LexerSharedInputState)null);
        scanner.setWhitespaceIncluded(true);
        GroovyRecognizer parser = GroovyRecognizer.make(scanner);
        restart(info);
    }
    
    private void restart(LexerRestartInfo<GroovyTokenId> info) {
        tokenFactory = info.tokenFactory();
        this.lexerInput = info.input();

        LexerSharedInputState inputState = null;
        if (lexerInput != null) {
            myCharBuffer = new MyCharBuffer(new LexerInputReader(lexerInput));
            inputState = new LexerSharedInputState(myCharBuffer);
        }
        scanner.setInputState(inputState);
        if (inputState != null) {
            scanner.resetText();
        }
    }

    private void scannerConsumeChar() {
        try {
            scanner.consume();
        } catch (CharStreamException e) {
            throw new IllegalStateException();
        }
    }

    private Token<GroovyTokenId> createToken(int tokenIntId, int tokenLength) {
        GroovyTokenId id = GroovyTokenId.getTokenId(tokenIntId);
        String fixedText = id.fixedText();
        return (fixedText != null) ? tokenFactory.getFlyweightToken(id, fixedText)
                                   : tokenFactory.createToken(id, tokenLength);
    }

    public Token<GroovyTokenId> nextToken() {
        try {
            groovyjarjarantlr.Token antlrToken = scanner.nextToken();
            if (antlrToken != null) {
                int intId = antlrToken.getType();

                int len = lexerInput.readLengthEOF() - myCharBuffer.getExtraCharCount();
                if ( antlrToken.getText() != null ) {
                    len = Math.max( len, antlrToken.getText().length() );
                }

                if ( len == 0 ) {
                    if ( lexerInput.readLength() > 0 ) {
                        if ("\"".equals(lexerInput.readText().toString())) {
                            return createToken(GroovyTokenTypes.STRING_LITERAL, 1);
                        } else if ("\n".equals(lexerInput.readText().toString())) {
                            return createToken(GroovyTokenTypes.NLS, 1);
                        } else {
                            return createToken(GroovyTokenTypes.WS, lexerInput.readLength());
                        }
                    }
                }

                if ( scanner.getStringCtorState() != 0) {
                    intId = GroovyTokenTypes.STRING_LITERAL;
                }

                // this wasn't in original coyote lexer
                // without this len was counted as -1 and thrown exception
                if (intId == GroovyTokenTypes.EOF) {
                    return null;
                }

                return createToken(intId, len);

            } 
            else { // antlrToken is null
                int scannerTextTokenLength = scanner.getText().length();
                if ( scannerTextTokenLength > 0 ) {
                    return createToken(GroovyTokenTypes.WS, scannerTextTokenLength);
                }
                return null;  // no more tokens from tokenManager
            }
        } catch (TokenStreamException e) {
            int len = lexerInput.readLength() - myCharBuffer.getExtraCharCount();
            int tokenLength = lexerInput.readLength();
            
            scanner.resetText();
            
            while (len < tokenLength) {
                scannerConsumeChar();
                len++;
            }
            
            scanner.resetText();
            
            if (lexerInput.readText().toString().startsWith("/")) {
                lexerInput.backup(lexerInput.readLength() - 1);
                return createToken(GroovyTokenTypes.DIV, 1);
            } else if (lexerInput.readText().toString().startsWith("\"")) {
                lexerInput.backup(lexerInput.readLength() - 1);
                return createToken(GroovyTokenTypes.STRING_LITERAL, 1);
            } else if (lexerInput.readText().toString().startsWith("\n")) {
                lexerInput.backup(lexerInput.readLength() - 1);
                return createToken(GroovyTokenTypes.NLS, 1);
            } else {
                return createToken(GroovyTokenId.ERROR.ordinal(), tokenLength);
            }
        }
    }
    
    public Object state() {
        return null;
    }

    public void release() {
    }

    private static class MyCharBuffer extends CharBuffer {
        public MyCharBuffer(Reader reader) {
            super( reader );
            queue = new MyCharQueue(1);
        }
        
        public int getExtraCharCount() {
            syncConsume();
            return ( (MyCharQueue) queue ).getNbrEntries() ;
        }

    }     
    
    private static class MyCharQueue extends CharQueue {
        public MyCharQueue(int minSize) {
            super(minSize);
        }
        
        public int getNbrEntries() {
            return nbrEntries;
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
                    return -1;
                }
                buf[i + off] = (char)c;
            }
            return len;
        }

        public void close() throws IOException {
        }
    }

    private static class GroovyScanner extends org.codehaus.groovy.antlr.parser.GroovyLexer {

        public GroovyScanner(LexerSharedInputState state) {
            super(state);
        }
        
        public int getStringCtorState() {
            return stringCtorState;
        }
        
    }
    
}
