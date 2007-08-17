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

import java.util.List;
import java.util.Set;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.GapList;
import org.netbeans.lib.lexer.token.ComplexToken;
import org.netbeans.lib.lexer.token.PreprocessedTextToken;
import org.netbeans.spi.lexer.CharPreprocessor;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.ComplexToken;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Implementation of the functionality related to lexer input.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class LexerInputOperation<T extends TokenId> implements CharProvider {
    
    /** Flag for additional correctness checks (may degrade performance). */
    private static final boolean testing = Boolean.getBoolean("netbeans.debug.lexer.test");
    
    /**
     * Current reading index in the operation.
     * At all times it must be &gt;=0.
     */
    private int readIndex;
    
    /**
     * Maximum index from which the char was fetched for current
     * (or previous) tokens recognition.
     * <br>
     * The index is updated lazily - only when EOF is reached
     * and when backup() is called.
     */
    private int lookaheadIndex;
    
    /**
     * Active preprocessor or null if there is no preprocessor.
     */
    private CharPreprocessorOperation preprocessorOperation;
    
    /**
     * Computed and cached token length.
     */
    private int tokenLength;
    
    private final TokenList<T> tokenList;
    
    private final boolean mutableInput;
    
    private final Lexer<T> lexer;
    
    /**
     * Start of the token being currently recognized.
     */
    private int tokenStartIndex;

    private boolean lexerFinished;
    
    /**
     * How many flyweight tokens were created in a row.
     */
    private int flySequenceLength;
    
    private List<CharPreprocessorError> preprocessErrorList;
    
    /**
     * Total count of preprocessors used during lexing.
     * It's used to determine whether extra preprocessed chars need to be used.
     */
    protected int preprocessingLevelCount;

    private CharProvider.ExtraPreprocessedChars extraPreprocessedChars;

    public LexerInputOperation(TokenList<T> tokenList, int tokenIndex, Object lexerRestartState) {
        this.tokenList = tokenList;
        this.mutableInput = (tokenList.modCount() != -1);
        // Determine flySequenceLength setting
        while (--tokenIndex >= 0 && LexerUtilsConstants.token(
                tokenList, tokenIndex).isFlyweight()
        ) {
            flySequenceLength++;
        }
        
        LanguagePath languagePath = tokenList.languagePath();
        LanguageHierarchy<T> languageHierarchy = LexerUtilsConstants.innerLanguageHierarchy(languagePath);
        TokenFactory<T> tokenFactory = LexerSpiPackageAccessor.get().createTokenFactory(this);
        
        // Check whether character preprocessing is necessary
        CharPreprocessor p = LexerSpiPackageAccessor.get().createCharPreprocessor(languageHierarchy);
        if (p != null) {
            preprocessingLevelCount++;
            preprocessorOperation = new CharPreprocessorOperation(
                    ((preprocessorOperation != null)
                        ? (CharProvider)preprocessorOperation
                        : this),
                    p,
                    this
            );
        }
        
        LexerInput lexerInput = LexerSpiPackageAccessor.get().createLexerInput(
                (preprocessorOperation != null) ? preprocessorOperation : this);

        LexerRestartInfo<T> info = LexerSpiPackageAccessor.get().createLexerRestartInfo(
                lexerInput, tokenFactory, lexerRestartState,
                tokenList.languagePath(), inputAttributes());
        lexer = LexerSpiPackageAccessor.get().createLexer(languageHierarchy, info);
    }

    public abstract int read(int index);
    
    public abstract char readExisting(int index);
    
    public abstract void approveToken(AbstractToken<T> token);

    public Set<T> skipTokenIds() {
        return tokenList.skipTokenIds();
    }
    
    public final int read() {
        int c = read(readIndex++);
        if (c == LexerInput.EOF) {
            lookaheadIndex = readIndex; // count EOF char into lookahead
            readIndex--; // readIndex must not include EOF
        }
        return c;
    }
    
    public int deepRawLength(int length) {
        // No preprocessing by default
        return length;
    }
    
    public int deepRawLengthShift(int index) {
        // No preprocessing by default
        return index;
    }
    
    public final int readIndex() {
        return readIndex;
    }
    
    public final void backup(int count) {
        if (lookaheadIndex < readIndex) {
            lookaheadIndex = readIndex;
        }
        readIndex -= count;
    }
    
    /**
     * Get a distance between the index of the rightmost character already returned
     * by previous {@link #read()} operations and the present read index.
     * <br/>
     * If there were no {@link #backup(int)} operation performed
     * the lookahead will be zero except the case when EOF was already returned.
     *
     * @return &gt;=0 number of characters between the rightmost reading index reached
     *   and the present read position.
     *   <br/>
     *   The EOF (when reached by reading) is treated as a single character
     *   in lookahead.
     *   <br/>
     *   If there is an active character preprocessor the returned value
     *   is a raw length of the lookahead.
     */
    public final int lookahead() {
        return (lookaheadIndex > readIndex)
                ? ((preprocessorOperation != null)
                        ? preprocessorOperation.deepRawLength(lookaheadIndex - readIndex)
                        : (lookaheadIndex - readIndex))
                : 0;
    }
    
    public final int tokenLength() {
        return tokenLength;
    }
    
    public void tokenRecognized(int tokenLength) {
        if (tokenLength > readIndex()) {
            throw new IndexOutOfBoundsException("tokenLength=" + tokenLength // NOI18N
                    + " >" + readIndex());
        }
        this.tokenLength = tokenLength;
    }
    
    public void tokenApproved() {
        tokenStartIndex += tokenLength;
        readIndex -= tokenLength;
        lookaheadIndex -= tokenLength;
    }
    
    protected final TokenList<T> tokenList() {
        return tokenList;
    }
    
    protected final int tokenStartIndex() {
        return tokenStartIndex;
    }

    public final void setTokenStartIndex(int tokenStartIndex) {
        this.tokenStartIndex = tokenStartIndex;
    }

    protected final CharPreprocessorOperation preprocessor() {
        return preprocessorOperation;
    }
    
    public final boolean isMutableInput() {
        return mutableInput;
    }
    
    public final boolean isStoreLookaheadAndState() {
        return isMutableInput() || testing;
    }
    
    public AbstractToken<T> nextToken() {
        assert (!lexerFinished);
        while (true) {
            @SuppressWarnings("unchecked")
            AbstractToken<T> token = (AbstractToken<T>)lexer().nextToken();
            if (token == null) {
                LexerUtilsConstants.checkLexerInputFinished(
                        (preprocessorOperation != null) ? (CharProvider)preprocessorOperation : this, this);
                lexerFinished = true;
                return null;
            } else {
                approveToken(token);
            }
            if (token == TokenFactory.SKIP_TOKEN)
                continue; // Fetch next token
            return token;
        }
    }
    
    /**
     * Notification that the token was recognized.
     * @param tokenLength length of the recognized token.
     * @param skip whether the token should be skipped
     * @return true if the token holding preprocessed text should be created.
     *  If skip is true then false is returned.
     */
    public final boolean tokenRecognized(int tokenLength, boolean skip) {
        if (preprocessorOperation != null) {
            preprocessorOperation.tokenRecognized(tokenLength);
        } else { // no preprocessor
            tokenRecognized(tokenLength);
        }

        // If the token is not skipped check whether preprocessed token
        // should be created instead of the regular token.
        if (!skip && tokenLength != this.tokenLength
                || (preprocessErrorList != null 
                    && preprocessErrorList.get(0).index() < this.tokenLength)
        ) {
            if (extraPreprocessedChars == null && preprocessingLevelCount > 1) {
                // For more than one preprocessing level need to handle
                // extra preprocessed chars before and after the main ones
                // on the parent levels.
                extraPreprocessedChars = new CharProvider.ExtraPreprocessedChars();
            }
            return true;
        }
        return false;
    }
    
    public void notifyPreprocessorError(CharPreprocessorError error) {
        if (preprocessErrorList == null) {
            preprocessErrorList = new GapList<CharPreprocessorError>();
        }
        preprocessErrorList.add(error);
    }

    public final void initPreprocessedToken(AbstractToken<T> token) {
        CharPreprocessorError error = null;
        if (preprocessErrorList != null && preprocessErrorList.size() > 0) {
            for (int i = preprocessErrorList.size() - 1; i >= 0; i--) {
                error = preprocessErrorList.get(i);
                if (error.index() < tokenLength) {
                    preprocessErrorList.remove(i);
                } else {// Above errors for this token
                    // Relocate - subtract token length
                    error.updateIndex(-tokenLength);
                    error = null;
                }
            }
        }
        
        PreprocessedTextStorage storage = preprocessorOperation.createPreprocessedTextStorage(
                token.text(), extraPreprocessedChars);
        
        if (token.getClass() == ComplexToken.class) {
            ((ComplexToken)token).initPrep(storage, error);
        } else {
            ((PreprocessedTextToken)token).initPrep(storage, error);
        }
    }
    
    public void collectExtraPreprocessedChars(CharProvider.ExtraPreprocessedChars epc,
    int prepStartIndex, int prepEndIndex, int topPrepEndIndex) {
        // No extra preprocessed characters
    }
    
    public final LanguageOperation<T> languageOperation() {
        return LexerUtilsConstants.innerLanguageOperation(tokenList.languagePath());
    }
    
    public final Object lexerState() {
        return lexer.state();
    }

    public final boolean isFlyTokenAllowed() {
        return (flySequenceLength < LexerUtilsConstants.MAX_FLY_SEQUENCE_LENGTH);
    }
    
    protected final void flyTokenAdded() {
        flySequenceLength++;
    }
    
    protected final void preventFlyToken() {
        flySequenceLength = LexerUtilsConstants.MAX_FLY_SEQUENCE_LENGTH;
    }
    
    protected final void clearFlySequence() {
        flySequenceLength = 0;
    }
    
    protected final boolean isSkipToken(AbstractToken<T> token) {
        return (token == TokenFactory.SKIP_TOKEN);
    }
    
    public final Lexer lexer() {
        return lexer;
    }
    
    public final InputAttributes inputAttributes() {
        return tokenList.inputAttributes();
    }
    
    public final void release() {
        lexer.release();
    }
    
}
