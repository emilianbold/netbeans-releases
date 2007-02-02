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

package org.netbeans.modules.lexer.demo;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.LexerInput;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.util.AbstractCharSequence;
import org.netbeans.spi.lexer.util.CharSubSequence;
import org.netbeans.spi.lexer.util.Compatibility;

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

    /**
     * Lazily created character sequence representing subsequence of text read 
     * from the lexer input by read() operations.
     */
    private CharSubSequence subReadText;
    
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
    
    public boolean isEOFLookahead() {
        return (eof != 0);
    }
    
    public void backup(int count) {
        lookaheadIndex = Math.max(lookaheadIndex, inputIndex + eof);
        inputIndex -= count;
        if (inputIndex < tokenIndex) {
            inputIndex += count;
            throw new IllegalArgumentException("count=" + count
                + " > " + (inputIndex - tokenIndex));
            
        } else if (inputIndex > lookaheadIndex - eof) {
            inputIndex += count;
            throw new IllegalArgumentException("count=" + count
                + " < " + (inputIndex + eof - lookaheadIndex));
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

    public CharSequence getReadText(int start, int end) {
        if (subReadText == null) {
            subReadText = new CharSubSequence(new ReadText());
        }

        subReadText.setBounds(start, end);

        return subReadText;
    }    
    
    char readTextCharAt(int index) {
        if (index < 0) {
            throw  new IndexOutOfBoundsException("index=" + index + " < 0");
        }

        if (index >= getReadLength()) {
            throw new IndexOutOfBoundsException("index=" + index
                + " >= getReadLength()=" + getReadLength());
        }

        return text.charAt(tokenIndex + index);
    }
    
    private class ReadText extends AbstractCharSequence {
        
        public int length() {
            return getReadLength();
        }

        public char charAt(int index) {
            return readTextCharAt(index);
        }
        
    }

}
