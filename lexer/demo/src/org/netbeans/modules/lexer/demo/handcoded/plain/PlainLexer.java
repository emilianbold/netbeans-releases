/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
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
