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

package org.netbeans.lib.java.lexer;

import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Lexical analyzer for javadoc language.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class JavadocLexer implements Lexer<JavadocTokenId> {

    private static final int EOF = LexerInput.EOF;

    private LexerInput input;
    
    private TokenFactory<JavadocTokenId> tokenFactory;
    
    public JavadocLexer(LexerRestartInfo<JavadocTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        assert (info.state() == null); // passed argument always null
    }
    
    public Object state() {
        return null;
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
            return token(JavadocTokenId.IDENT);
        }
        
        if ("@<.#".indexOf(ch) == (-1)) {
            //TODO: EOF
            ch = input.read();
            
            while (!Character.isJavaIdentifierStart(ch) && "@<.#".indexOf(ch) == (-1) && ch != EOF)
                ch = input.read();
            
            if (ch != EOF)
                input.backup(1);
            return token(JavadocTokenId.OTHER_TEXT);
        }
        
        switch (ch) {
            case '@':
                while (true) {
                    ch = input.read();
                    
                    if (!Character.isLetter(ch)) {
                        input.backup(1);
                        return tokenFactory.createToken(JavadocTokenId.TAG, input.readLength());
                    }
                }
            case '<':
                while (true) {
                    ch = input.read();
                    if (ch == '>' || ch == EOF) {
                        return token(JavadocTokenId.HTML_TAG);
                    }
                }
            case '.':
                return token(JavadocTokenId.DOT);
            case '#':
                return token(JavadocTokenId.HASH);
        } // end of switch (ch)
        
        assert false;
        
        return null;
    }

    private Token<JavadocTokenId> token(JavadocTokenId id) {
        return tokenFactory.createToken(id);
    }

    public void release() {
    }

}
