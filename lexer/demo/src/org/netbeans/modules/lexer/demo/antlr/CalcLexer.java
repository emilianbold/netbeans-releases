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

package org.netbeans.modules.lexer.demo.antlr;

import antlr.LexerSharedInputState;
import antlr.TokenStreamException;
import org.netbeans.api.lexer.Lexer;
import org.netbeans.api.lexer.LexerInput;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.antlr.AntlrToken;
import org.netbeans.spi.lexer.util.LexerInputReader;

/**
 * Wrapper for antlr generated {@link antlr.CharScanner}
 * that implements the {@link org.netbeans.lexer.Lexer}.
 * <br>It supports extended tokens formed from
 * two or more sub-tokens. For example the error
 * tokens are typically returned
 * just as "everything-else" single characters
 * from the scanner and formed into one extended
 * error token in the lexer.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class CalcLexer implements Lexer {
    
    private static final int INVALID_TOKEN_INT_ID = -1;
    
    private CalcLanguage language;
    
    private CalcScanner scanner;
    
    private LexerInput lexerInput;
    
    /** Int id of the extended token */
    private int extendedTokenIntId;
    
    /** Length of the extended token. Zero if not in extended token. */
    private int extendedTokenLength;
    
    /** Token int id after the extended token. */
    private int followsExtendedTokenIntId = INVALID_TOKEN_INT_ID;
    
    public CalcLexer(CalcLanguage language) {
        this.language = language;
        this.scanner = new CalcScanner((LexerSharedInputState)null);
    }
    
    public Token nextToken() {
        int tokenIntId = followsExtendedTokenIntId;
        int tokenLength = 0;

        next_token: {
            while (true) {
                if (tokenIntId == INVALID_TOKEN_INT_ID) {
                    // token must be fetched from scanner
                    try {
                        AntlrToken antlrToken = (AntlrToken)scanner.nextToken();
                        if (antlrToken != null) {
                            tokenIntId = antlrToken.getType();
                            tokenLength = antlrToken.getLength();

                        } else { // antlrToken is null - no more tokens from scanner
                            tokenIntId = CalcScannerTokenTypes.EOF;
                            /* In this case the tokenIntId is set to EOF
                             * and tokenLength is 0.
                             * The loop will be broken automatically.
                             */
                        }
                    } catch (TokenStreamException e) {
                        throw new IllegalStateException(
                            e + "\nTokenStreamException occurred."
                            + "\nIt's necessary to fix the lexer to be able to correctly"
                            + " recognize the input that caused this exception."
                        );
                    }

                } else { // tokenIntId != INVALID_TOKEN_INT_ID
                    /* It means that extended token was returned
                     * in the previous call to nextToken().
                     * The scanner already found a valid token previously
                     * (that valid token ended the extended token)
                     * and the token type of that valid token was stored
                     * in followsExtendedTokenIntId and the length
                     * in the extendedTokenLength and it will be now used.
                     */
                    followsExtendedTokenIntId = -1; // resume normal operation
                    tokenLength = extendedTokenLength;
                    extendedTokenLength = 0;
                } // END-IF-ELSE tokenIntId == INVALID_TOKEN_INT_ID
                
                /* Now tokenIntId is filled with a valid token type.
                 * EOF is considered a valid token type here.
                 */

                if (extendedTokenLength == 0) { // not in extended token
                    switch (tokenIntId) { // check for types that start extended token
                        case CalcScannerTokenTypes.ERROR:
                            extendedTokenIntId = tokenIntId;
                            extendedTokenLength = tokenLength;
                            break; // get next token inside the loop

                        default: // regular token type found
                            // both tokenIntId and tokenLength are populated correctly
                            break next_token;
                    }

                } else { // currently in extended token
                    boolean continueExtended = (extendedTokenIntId == tokenIntId);
                    boolean includeCurrent = false;
                    /* Some additional checking may need to be done here
                     * to set the continueExtended and includeCurrent variables
                     * appropriately.
                     */

                    if (continueExtended) { // extended
                        extendedTokenLength += tokenLength;

                    } else { // Stop extending
                        /* Extended token is finished. includeCurrent flag
                         * resolves whether the currently found token extends
                         * (ends) the extended token or not.
                         * The extended token will be returned.
                         * If the current token is not included in the extended token
                         * then the current token will be saved
                         * in followsExtendedTokenIntId and returned
                         * from the next call to nextToken().
                         * Setting followsExtendedTokenIntId to >= 0 value
                         * will invoke a special treatment next time
                         * the nextToken() will be called.
                         * extendedTokenLength will be returned so it must be
                         * copied into tokenLength but the current tokenLength
                         * must be saved somewhere so that it can be returned
                         * in the next call to nextToken(). It will be stored
                         * in extendedTokenLength because that variable can be reused
                         * as it no longer needs to hold the original value.
                         */
                        if (includeCurrent) {
                            tokenLength = extendedTokenLength + tokenLength;
                            extendedTokenLength = 0;

                        } else { // do not include current token
                            followsExtendedTokenIntId = tokenIntId;

                            int tmpLength = tokenLength;
                            tokenLength = extendedTokenLength;
                            extendedTokenLength = tmpLength;

                        }
                            
                        tokenIntId = extendedTokenIntId;
                        break next_token;
                    } // END-OF Stop extending
                } // END-OF currently in extended token

                tokenIntId = INVALID_TOKEN_INT_ID; // push to get next token from scanner
            } // END-OF while (true)
        } // END-OF next_token label statement


        // Find the tokenId
        return (tokenIntId != CalcScannerTokenTypes.EOF)
            ? lexerInput.createToken(language.getValidId(tokenIntId), tokenLength)
            : null;
    }

    public Object getState() {
        return null;
    }

    public void restart(LexerInput input, Object state) {
        this.lexerInput = input;
        followsExtendedTokenIntId = INVALID_TOKEN_INT_ID;
        extendedTokenLength = 0;

        LexerSharedInputState inputState = null;
        if (lexerInput != null) {
            inputState = new LexerSharedInputState(new LexerInputReader(lexerInput));
        }
        scanner.setInputState(inputState);
        scanner.resetText();
    }
    
}
