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

package org.netbeans.lib.lexer;

import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.lib.lexer.token.AbstractToken;

/**
 * Abstract lexer input operation over a character sequence.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class TextLexerInputOperation<T extends TokenId> extends LexerInputOperation<T> {

    /**
     * Input text from which the reading of characters is done.
     */
    private final CharSequence inputText;

    private final int inputTextStartOffset;
    
    /**
     * End of valid chars in readCharArray (points to first invalid char).
     */
    private int readEndIndex;
    

    public TextLexerInputOperation(TokenList<T> tokenList, CharSequence inputText) {
        this(tokenList, 0, null, inputText, 0, 0, inputText.length());
    }

    public TextLexerInputOperation(TokenList<T> tokenList, int tokenIndex,
    Object lexerRestartState, CharSequence inputText, int inputTextStartOffset,
    int startOffset, int endOffset) {
        super(tokenList, tokenIndex, lexerRestartState);
        this.inputText = inputText;
        this.inputTextStartOffset = inputTextStartOffset;

        // Make the offsets relative to the input start offset
        startOffset -= inputTextStartOffset;
        endOffset -= inputTextStartOffset;
        assert (0 <= startOffset) && (startOffset <= endOffset)
            && (endOffset <= inputText.length())
            : "startOffset=" + startOffset + ", endOffset=" + endOffset
                + ", inputText.length()=" + inputText.length();
        setTokenStartIndex(startOffset);
        readEndIndex = endOffset;
    }
    
    public int read(int index) { // index >= 0 is guaranteed by contract
        index += tokenStartIndex();
        if (index < readEndIndex) {
            return inputText.charAt(index);
        } else { // must read next or return EOF
            return LexerInput.EOF;
        }
    }

    public char readExisting(int index) {
        return inputText.charAt(tokenStartIndex() + index);
    }

    public void approveToken(AbstractToken<T> token) {
        int tokenLength = tokenLength();
        if (isSkipToken(token)) {
            preventFlyToken();

        } else if (token.isFlyweight()) {
            assert isFlyTokenAllowed();
            flyTokenAdded();

        } else { // non-flyweight token
            token.setTokenList(tokenList());
            token.setRawOffset(inputTextStartOffset + tokenStartIndex());
            clearFlySequence();
        }

        tokenApproved();
    }

    protected final int readEndIndex() {
        return readEndIndex;
    }

}
