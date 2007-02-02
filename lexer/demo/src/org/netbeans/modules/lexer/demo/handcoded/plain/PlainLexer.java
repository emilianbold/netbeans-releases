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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.lexer.demo.handcoded.plain;

import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Lexer;
import org.netbeans.api.lexer.LexerInput;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.Token;

/**
 * Lexer that recognizes PlainLanguage.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

final class PlainLexer implements Lexer {

    private static final PlainLanguage language = PlainLanguage.get();

    private LexerInput lexerInput;

    public PlainLexer() {
    }

    public Object getState() {
        return null;
    }

    public void restart(LexerInput input, Object state) {
        this.lexerInput = input;
    }

    public Token nextToken() {
        int ch = lexerInput.read();
        while (ch != LexerInput.EOF && ch != '\n') {
            ch = lexerInput.read();
        }
        
        return (lexerInput.getReadLength() > 0) // read one or more chars
            ? lexerInput.createToken(PlainLanguage.TEXT)
            : null; // was immediate EOF on input
    }
    
}
