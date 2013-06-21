/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.lib.java.lexer;

import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;
import org.netbeans.spi.lexer.TokenPropertyProvider;

/**
 * Lexical analyzer for javadoc language.
 * @author Miloslav Metelka
 * @version 1.00
 */

public class JavadocLexer implements Lexer<JavadocTokenId> {

    private static final int EOF = LexerInput.EOF;

    private LexerInput input;
    
    private TokenFactory<JavadocTokenId> tokenFactory;
    
    private Integer state = null;
    
    public JavadocLexer(LexerRestartInfo<JavadocTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        this.state = (Integer) info.state();        
    }
    
    public Object state() {
        return state;
    }
    
    public Token<JavadocTokenId> nextToken() {
        int ch = input.read();
        
        if (ch == EOF) {
            return null;
        }
        
        if (Character.isJavaIdentifierStart(ch)) {
            //TODO: EOF
            while (Character.isJavaIdentifierPart(input.read()))
                ;
            
            input.backup(1);
            if (state != null && state == 2) {
                state = 1;
                return token(JavadocTokenId.IDENT, "javadoc-identifier"); //NOI18N
            }
            state = 1;
            return token(JavadocTokenId.IDENT);
        }
        
        if ("@<.#".indexOf(ch) == (-1)) {
            boolean newline = state == null;
            boolean leftbr = false;
            while (!Character.isJavaIdentifierStart(ch) && "<.#".indexOf(ch) == (-1) && ch != EOF && (ch != '@' || !newline && !leftbr)) {
                if (ch == '{') {
                    leftbr = true;
                    newline = false;
                } else if (ch == '\n') {
                    newline = true;
                } else if (!Character.isWhitespace(ch) && (!newline || ch != '*')) {
                    leftbr = false;
                    newline = false;
                }
                ch = input.read();
            }
            if (newline || leftbr)
                state = null;
            
            if (ch != EOF)
                input.backup(1);
            return token(JavadocTokenId.OTHER_TEXT);
        }
        
        switch (ch) {
            case '@':
                if (state != null) {
                    boolean newline = false;
                    boolean leftbr = false;
                    while (!Character.isJavaIdentifierStart(ch) && "<.#".indexOf(ch) == (-1) && ch != EOF && (ch != '@' || !newline && !leftbr)) {
                        if (ch == '{') {
                            leftbr = true;
                            newline = false;
                        } else if (ch == '\n') {
                            newline = true;
                        } else if (!Character.isWhitespace(ch) && (!newline || ch != '*')) {
                            leftbr = false;
                            newline = false;
                        }
                        ch = input.read();
                    }
                    if (newline || leftbr)
                        state = null;

                    if (ch != EOF)
                        input.backup(1);
                    return token(JavadocTokenId.OTHER_TEXT);
                }
                String tag = "";
                while (true) {
                    ch = input.read();
                    if (!Character.isLetter(ch)) {
                        state = "param".equals(tag) ? 2 : 1; //NOI18N
                        input.backup(1);
                        return tokenFactory.createToken(JavadocTokenId.TAG, input.readLength());
                    } else {
                        tag+=new String(Character.toChars(ch));
                    }
                }
            case '<':
                int backupCounter = 0;
                boolean newline = false;
                boolean asterisk = false;
                while (true) {
                    ch = input.read();
                    ++backupCounter;
                    if (ch == EOF) {
                        state = null;
                        return token(JavadocTokenId.HTML_TAG);
                    } else if (ch == '>') {
                        if (state != null && state == 2) {
                            state = 1;
                            return token(JavadocTokenId.IDENT, "javadoc-identifier"); //NOI18N
                        }
                        state = 1;
                        return token(JavadocTokenId.HTML_TAG);
                    } else if (ch == '<') {
                        state = 1;
                        input.backup(1);
                        return token(JavadocTokenId.HTML_TAG);
                    } else if (ch == '\n') {
                        state = null;
                        backupCounter = 1;
                        newline = true;
                        asterisk = false;
                    } else if (newline && ch == '@') {
                        input.backup(backupCounter);
                        return token(JavadocTokenId.HTML_TAG);
                    } else if (newline && !asterisk && ch == '*') {
                        asterisk = true;
                    } else if (newline && !Character.isWhitespace(ch)) {
                        newline = false;
                    }
                }
            case '.':
                state = 1;
                return token(JavadocTokenId.DOT);
            case '#':
                state = 1;
                return token(JavadocTokenId.HASH);
        } // end of switch (ch)
        
        assert false;
        
        return null;
    }

    private Token<JavadocTokenId> token(JavadocTokenId id) {
        return tokenFactory.createToken(id);
    }

    private Token<JavadocTokenId> token(JavadocTokenId id, final Object property) {
        return tokenFactory.createPropertyToken(id, input.readLength(), new TokenPropertyProvider<JavadocTokenId>() {

        @Override
        public Object getValue(Token<JavadocTokenId> token, Object key) {
            if (property.equals(key)) 
                return true; 
            return null;
        }
        
    });
    }

    public void release() {
    }

}
