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

import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.lexer.inc.SnapshotTokenList;
import org.netbeans.spi.lexer.CharPreprocessor;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.spi.lexer.TokenPropertyProvider;

/**
 * Various utility methods and constants in lexer module.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class LexerUtilsConstants {
    
    /**
     * Maximum allowed number of consecutive flyweight tokens.
     * <br>
     * High number of consecutive flyweight tokens
     * would degrade performance of offset
     * finding.
     */
    public static final int MAX_FLY_SEQUENCE_LENGTH = 5;
    
    /**
     * Minimum number of characters that will be lexed
     * at once in a mutable input setup.
     * <br>
     * The created tokens will be notified in one token change event.
     * <br>
     * This should roughly cover a single page with text
     * (so that an initial page of text is lexed at once)
     * but it's not strictly necessary.
     */
    public static final int MIN_LEXED_AREA_LENGTH = 4096;
    
    /**
     * Fraction of the mutable input size that will be lexed at once.
     * <br>
     * This should avoid notifying of token creations too many times
     * for large inputs.
     */
    public static final int LEXED_AREA_INPUT_SIZE_FRACTION = 10;
    
    /**
     * Check that there are no more characters to be read from the given
     * lexer input operation.
     */
    public static void checkLexerInputFinished(CharProvider input, LexerInputOperation operation) {
        if (input.read() != LexerInput.EOF) {
            throw new IllegalStateException(
                "Lexer " + operation.lexer() + // NOI18N
                " returned null token" + // NOI18N
                " but EOF was not read from lexer input yet." + // NOI18N
                " Fix the lexer."// NOI18N
            );
        }
        if (input.readIndex() > 0) {
            throw new IllegalStateException(
                "Lexer " + operation.lexer() + // NOI18N
                " returned null token but lexerInput.readLength()=" + // NOI18N
                input.readIndex() +
                " - these characters need to be tokenized." + // NOI18N
                " Fix the lexer." // NOI18N
            );
        }
    }
    
    public static void tokenLengthZeroOrNegative(int tokenLength) {
        if (tokenLength == 0) {
            throw new IllegalArgumentException(
                "Tokens with zero length are not supported by the framework." // NOI18N
              + " Fix the lexer." // NOI18N
            );
        } else { // tokenLength < 0
            throw new IllegalArgumentException(
                "Negative token length " + tokenLength // NOI18N
            );
        }
    }

    public static void throwFlyTokenProhibited() {
        throw new IllegalStateException("Flyweight token created but prohibited." // NOI18N
                + " Lexer needs to check lexerInput.isFlyTokenAllowed()."); // NOI18N
    }

    public static void throwBranchTokenFlyProhibited(AbstractToken token) {
        throw new IllegalStateException("Language embedding cannot be created" // NOI18N
                + " for flyweight token=" + token // NOI18N
                + "\nFix the lexer to not create flyweight token instance when"
                + " language embedding exists for the token."
        );
    }
    
    public static void checkValidBackup(int count, int maxCount) {
        if (count > maxCount) {
            throw new IndexOutOfBoundsException("Cannot backup " // NOI18N
                    + count + " characters. Maximum: " // NOI18N
                    + maxCount + '.');
        }
    }
    
    public static LanguageHierarchy languageHierarchy(Language language) {
        return LexerApiPackageAccessor.get().languageHierarchy(language);
    }

    public static LanguageHierarchy languageHierarchy(LanguagePath languagePath) {
        return languageHierarchy(languagePath.innerLanguage());
    }
    
    public static LanguageOperation languageOperation(Language language) {
        return LexerSpiPackageAccessor.get().operation(languageHierarchy(language));
    }

    public static LanguageOperation languageOperation(LanguagePath languagePath) {
        return LexerSpiPackageAccessor.get().operation(languageHierarchy(languagePath));
    }

    public static AbstractToken<? extends TokenId> token(Object tokenOrBranch) {
        return (tokenOrBranch.getClass() == BranchTokenList.class)
            ? ((BranchTokenList)tokenOrBranch).branchToken()
            : (AbstractToken<? extends TokenId>)tokenOrBranch;
    }

    public static AbstractToken token(TokenList tokenList, int index) {
        return token(tokenList.tokenOrBranch(index));
    }

    public static StringBuilder appendTokenList(StringBuilder sb, TokenList tokenList, int currentIndex) {
        if (sb == null) {
            sb = new StringBuilder();
        }
        TokenHierarchy tokenHierarchy;
        if (tokenList instanceof SnapshotTokenList) {
                tokenHierarchy = ((SnapshotTokenList)tokenList).snapshot().tokenHierarchy();
                sb.append(tokenList).append('\n');
        } else {
                tokenHierarchy = null;
        }

        int tokenCount = tokenList.tokenCountCurrent();
        int digitCount = ArrayUtilities.digitCount(tokenCount);
        for (int i = 0; i < tokenCount; i++) {
            sb.append((i == currentIndex) ? '*' : ' ');
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            Object tokenOrBranch = tokenList.tokenOrBranch(i);
            if (tokenOrBranch == null) {
                System.err.println("tokenList=" + tokenList + ", i=" + i);
            }
            sb.append((tokenOrBranch.getClass() == BranchTokenList.class) ? '<' : ' ');
            sb.append(": ");
            AbstractToken token = token(tokenOrBranch);
            sb.append(token.dumpInfo(tokenHierarchy));
            sb.append('\n');
        }
        return sb;
    }
    
    public static boolean statesEqual(Object state1, Object state2) {
        return (state1 == null && state2 == null)
            || (state1 != null && state1.equals(state2));
    }
    
    public static <T extends TokenId> Object getTokenProperty(Token<T> token,
    TokenPropertyProvider propertyProvider, Object key, Object tokenStoreValue) {
        Object tokenStoreKey = propertyProvider.tokenStoreKey();
        if (tokenStoreKey != null && tokenStoreKey.equals(key)) { // token store value
            return propertyProvider.getValue(token, tokenStoreKey, tokenStoreValue);
        } else {
            return propertyProvider.getValue(token, key);
        }
    }
    
    public static String idToString(TokenId id) {
        return id.name() + '[' + id.ordinal() + ']'; // NOI18N;
    }
    
    private LexerUtilsConstants() {
        // no instances
    }

}
