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

package org.netbeans.lib.lexer;

import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LexerInput;

/**
 * Used when branching a token with preprocessed text.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class PreprocessedTextLexerInputOperation<T extends TokenId> extends TextLexerInputOperation<T> {

    private final PreprocessedTextStorage preprocessedText;

    private int prepStartIndex;

    private int prepEndIndex;

    private int tokenStartRawLengthShift;
    
    private int lastRawLengthShift;
    
    private int tokenEndRawLengthShift;

    public PreprocessedTextLexerInputOperation(TokenList<T> tokenList, PreprocessedTextStorage prepText) {
        this(tokenList, 0, null, prepText, 0, 0, prepText.length());
    }

    public PreprocessedTextLexerInputOperation(TokenList<T> tokenList, int tokenIndex,
    Object lexerRestartState, PreprocessedTextStorage prepText, int prepTextStartOffset,
    int startOffset, int endOffset) {
        super(tokenList, tokenIndex, lexerRestartState, prepText,
                prepTextStartOffset, startOffset, endOffset);
        this.preprocessedText = prepText;
        int index = startOffset - prepTextStartOffset;
        if (index > 0) {
            tokenStartRawLengthShift = preprocessedText.rawLengthShift(index);
            lastRawLengthShift = tokenStartRawLengthShift;
        }
        preprocessingLevelCount++; // extra level of preprocessing
    }

    public int deepRawLength(int length) {
        return length + preprocessedText.rawLengthShift(tokenStartIndex() + length - 1)
                - tokenStartRawLengthShift;
    }
    
    public int deepRawLengthShift(int index) {
        return preprocessedText.rawLengthShift(tokenStartIndex() + index)
                - tokenStartRawLengthShift;
    }

    public int read(int index) { // index >= 0 is guaranteed by contract
        index += tokenStartIndex();
        if (index < readEndIndex()) {
            // Check whether the char is preprocessed
            int rls = preprocessedText.rawLengthShift(index);
            if (rls != lastRawLengthShift) { // Preprocessed
                lastRawLengthShift = rls;
                if (prepStartIndex >= index) { // prepStartIndex already inited
                    prepStartIndex = index;
                }
                prepEndIndex = index + 1;
            }
            return preprocessedText.charAt(index);
        } else { // must read next or return EOF
            return LexerInput.EOF;
        }
    }

    public void tokenRecognized(int tokenLength) {
        super.tokenRecognized(tokenLength);
        tokenEndRawLengthShift = preprocessedText.rawLengthShift(
                tokenStartIndex() + tokenLength() - 1);
    }
    
    public void tokenApproved() {
        // Increase base raw length shift by the token's last-char shift
        tokenStartRawLengthShift += tokenEndRawLengthShift;

        if (prepStartIndex != Integer.MAX_VALUE) { // some prep chars (may be after token length)
            if (prepStartIndex < tokenLength()) { // prep chars before token end
                if (prepEndIndex <= tokenLength()) { // no preprocessed chars past token end
                    prepStartIndex = Integer.MAX_VALUE; // signal no preprocessed chars
                } else { // prepEndIndex > tokenLength => initial prep chars in the next token
                    prepStartIndex = 0;
                    prepEndIndex -= tokenLength();
                }

            } else { // prepStartIndex >= tokenLength
                prepStartIndex -= tokenLength();
                prepEndIndex -= tokenLength();
            }
        }
        super.tokenApproved();
    }
    
    public void collectExtraPreprocessedChars(CharProvider.ExtraPreprocessedChars epc,
    int prepStartIndex, int prepEndIndex, int topPrepEndIndex) {
        if (prepStartIndex < tokenLength()) { // Some preprocessed characters
            // Check for any pre-prepChars
            int preCount = Math.max(prepStartIndex - this.prepStartIndex, 0);
            // Check for post-prepChars
            int postCount;
            if (this.prepEndIndex > tokenLength()) {
                postCount = tokenLength() - prepEndIndex;
                if (postCount > 0) {
                    int i = tokenLength() - 2;
                    // Optimize the case when there are lookahead chars
                    // for the present token and the ending chars could possibly
                    // be non-preprocessed (prepEndIndex > tokenLength)
                    while (--i >= prepStartIndex && postCount > 0
                            && preprocessedText.rawLengthShift(i + tokenStartIndex()) == tokenEndRawLengthShift
                    ) { // not preprocessed
                        postCount--;
                    }
                } else // postCount <= 0
                    postCount = 0;
                
            } else { // this.prepEndIndex <= tokenLength
                postCount = this.prepEndIndex - prepEndIndex;
            }
            
            assert (preCount >= 0 && postCount >= 0);
            epc.ensureExtraLength(preCount + postCount);
            while (--preCount >= 0) {
                epc.insert(readExisting(prepStartIndex - 1), deepRawLength(prepStartIndex) - prepStartIndex);
                prepStartIndex--;
            }
            while (--postCount >= 0) {
                epc.append(readExisting(prepEndIndex), deepRawLength(prepEndIndex) - topPrepEndIndex);
                prepEndIndex++;
                topPrepEndIndex++;
            }
        }
    }

}
