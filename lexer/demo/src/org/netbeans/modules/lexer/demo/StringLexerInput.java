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

package org.netbeans.modules.lexer.demo;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.LexerInput;
import org.netbeans.api.lexer.TokenId;

/**
 * Token iterator that works over the given char sequence.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class StringLexerInput implements LexerInput {

    private String text;
    
    /** Index from which the read() methods read the next character */
    private int inputIndex;
    
    /** Index of the begining of the current token */
    private int tokenIndex;
    
    /** Helper variable for getReadLookahead() computation. */
    private int lookaheadIndex;
    
    /** 1 if lookahead reached EOF or 0 if not */
    private int eof;
    
    public StringLexerInput(String text) {
        this.text = text;
    }
    
    public int read() {
        if (inputIndex >= text.length()) {
            eof = 1;
            return LexerInput.EOF;
            
        } else {
            return text.charAt(inputIndex++);
        }
    }
    
    public int getReadLookahead() {
        return Math.max(lookaheadIndex, inputIndex + eof) - tokenIndex;
    }
    
    public int getReadLength() {
        return inputIndex - tokenIndex;
    }
    
    public void backup(int count) {
        lookaheadIndex = Math.max(lookaheadIndex, inputIndex + eof);
        inputIndex -= count;
        if (inputIndex < tokenIndex) {
            inputIndex += count;
            throw new IllegalArgumentException("count=" + count
                + " > " + (inputIndex - tokenIndex));
        }
    }
    
    public Token createToken(TokenId id, int tokenLength) {
        if (tokenLength <= 0) {
            throw new IllegalArgumentException("tokenLength="
                + tokenLength + " <= 0");
        }

        if (tokenIndex + tokenLength > inputIndex) {
            throw new IllegalArgumentException("tokenLength="
                + tokenLength + " > number-of-read-characters="
                + (inputIndex - tokenIndex)
            );
        }

        Token ret = new StringToken(id, text.substring(tokenIndex,
            tokenIndex + tokenLength));
        tokenIndex += tokenLength;
        return ret;
    }
    
    public Token createToken(TokenId id) {
        return createToken(id, inputIndex - tokenIndex);
    }

}
