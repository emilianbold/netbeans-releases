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

import org.netbeans.api.java.lexer.JavaStringTokenId;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Lexical analyzer for java string language.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class JavaStringLexer implements Lexer<JavaStringTokenId> {

    private static final int EOF = LexerInput.EOF;

    private LexerInput input;
    
    private TokenFactory<JavaStringTokenId> tokenFactory;
    
    public JavaStringLexer(LexerRestartInfo<JavaStringTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        assert (info.state() == null); // passed argument always null
    }
    
    public Object state() {
        return null;
    }
    
    public Token<JavaStringTokenId> nextToken() {
        while(true) {
            int ch = input.read();
            switch (ch) {
                case EOF:
                    if (input.readLength() > 0)
                        return token(JavaStringTokenId.TEXT);
                    else
                        return null;
                case '\\':
                    if (input.readLength() > 1) {// already read some text
                        input.backup(1);
                        return tokenFactory.createToken(JavaStringTokenId.TEXT, input.readLength());
                    }
                    switch (ch = input.read()) {
                        case 'b':
                            return token(JavaStringTokenId.BACKSPACE);
                        case 'f':
                            return token(JavaStringTokenId.FORM_FEED);
                        case 'n':
                            return token(JavaStringTokenId.NEWLINE);
                        case 'r':
                            return token(JavaStringTokenId.CR);
                        case 't':
                            return token(JavaStringTokenId.TAB);
                        case '\'':
                            return token(JavaStringTokenId.SINGLE_QUOTE);
                        case '"':
                            return token(JavaStringTokenId.DOUBLE_QUOTE);
                        case '\\':
                            return token(JavaStringTokenId.BACKSLASH);
                        case '0': case '1': case '2': case '3':
                            switch (input.read()) {
                                case '0': case '1': case '2': case '3':
                                case '4': case '5': case '6': case '7':
                                    switch (input.read()) {
                                        case '0': case '1': case '2': case '3':
                                        case '4': case '5': case '6': case '7':
                                            return token(JavaStringTokenId.OCTAL_ESCAPE);
                                    }
                                    return token(JavaStringTokenId.OCTAL_ESCAPE_INVALID);
                            }
                            return token(JavaStringTokenId.OCTAL_ESCAPE_INVALID);
                    }
                    return token(JavaStringTokenId.ESCAPE_SEQUENCE_INVALID);
            } // end of switch (ch)
        } // end of while(true)
    }

    private Token<JavaStringTokenId> token(JavaStringTokenId id) {
        return tokenFactory.createToken(id);
    }

    public void release() {
    }

}
