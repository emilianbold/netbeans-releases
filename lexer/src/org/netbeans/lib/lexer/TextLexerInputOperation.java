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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
