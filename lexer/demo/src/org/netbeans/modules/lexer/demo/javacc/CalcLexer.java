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

package org.netbeans.modules.lexer.demo.javacc;

import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Lexer;
import org.netbeans.api.lexer.LexerInput;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.javacc.LexerInputCharStream;
import org.netbeans.spi.lexer.javacc.TokenMgrError;

/**
 * Wrapper around generated token manager
 *
 * @author Miloslav Metelka
 * @version 1.00
 */


public class CalcLexer implements Lexer {
    
    private static final int INVALID_TOKEN_INT_ID = -1;
    
    private CalcLanguage language;
    
    private CalcTokenManager tm;

    private LexerInput lexerInput;
    
    /** Integer id of the extended token */
    private int extendedTokenIntId;
    
    /** Length of the extended token. Zero if not in extended token. */
    private int extendedTokenLength;
    
    /** Token type after the extended token. */
    private int followsExtendedTokenIntId = INVALID_TOKEN_INT_ID;
    
    public CalcLexer(CalcLanguage language) {
        this.tm = new CalcTokenManager(new LexerInputCharStream());
        this.language = language;
    }
    
    public Token nextToken() {
        TokenId id = null; // Resulting token identification
        int tokenLength = 0;
        int tokenIntId = followsExtendedTokenIntId;

        next_token: {
            while (true) {
                if (tokenIntId == INVALID_TOKEN_INT_ID) {
                    // token must be fetched from token manager
                    try {
                        org.netbeans.spi.lexer.javacc.Token javaccToken = tm.getNextToken();
                        if (javaccToken != null) {
                            tokenIntId = javaccToken.kind;
                            tokenLength = lexerInput.getReadLength() - extendedTokenLength;

                        } else { // antlrToken is null - no more tokens from token manager
                            tokenIntId = CalcConstants.EOF;
                            /* In this case the tokenIntId is set to EOF
                             * and tokenLength is 0.
                             * The loop will be broken automatically.
                             */
                        }
                    } catch (TokenMgrError e) {
                        throw new IllegalStateException(
                            e + "\nTokenMgrError occurred."
                            + "\nIt's necessary to fix the lexer to be able to correctly"
                            + " recognize the input that caused this exception."
                        );
                    }

                } else { // tokenIntId != INVALID_TOKEN_INT_ID
                    /* It means that extended token was returned
                     * in the previous call to nextToken().
                     * The token manager already found a valid token previously
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

                if (extendedTokenLength == 0) { // currently not in extended token
                    switch (tokenIntId) { // check for types that start extended token
                        case CalcConstants.ERROR:
                            extendedTokenIntId = tokenIntId;
                            extendedTokenLength = tokenLength;
                            break; // get next token inside the loop

                        case CalcConstants.ML_COMMENT_START:
                            extendedTokenIntId = CalcLanguage.INCOMPLETE_ML_COMMENT_INT;
                            extendedTokenLength = tokenLength;
                            break; // get next token inside the loop

                        default: // regular token type found
                            // both tokenIntId and tokenLength are populated correctly
                            break next_token;
                    }

                } else { // currently in extended token
                    boolean continueExtended = false;
                    boolean includeCurrent = false;
                    switch (extendedTokenIntId) {
                        case CalcConstants.ERROR:
                            continueExtended = (tokenIntId == CalcConstants.ERROR);
                            // includeCurrent stays false
                            break;
                            
                        case CalcLanguage.INCOMPLETE_ML_COMMENT_INT:
                            continueExtended = false; // do not continue extended token
                            includeCurrent = true; // but include current token
                            if (tokenIntId != CalcConstants.EOF) {
                                extendedTokenIntId = tokenIntId;
                            } // in case of EOF INCOMPLETE_ML_COMMENT_INT will be returned
                    }

                    /* Some additional checking can be done
                     * if e.g. starting and ending token types
                     * differ for the extended token.
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

                tokenIntId = INVALID_TOKEN_INT_ID; // push to get next token from token manager
            } // END-OF while (true)
        } // END-OF next_token label statement


        // Find the tokenId
        return (tokenIntId != CalcConstants.EOF)
            ? lexerInput.createToken(language.getValidId(tokenIntId), tokenLength)
            : null;
    }
    
    public Object getState() {
        return (tm.curLexState == tm.defaultLexState)
            ? null
            : CalcLanguage.CONSTANT_TO_INTEGER[tm.curLexState];
    }

    public void restart(LexerInput input, Object state) {
        this.lexerInput = input;
        LexerInputCharStream charStream = (LexerInputCharStream)tm.getCharStream();
        charStream.setLexerInput(lexerInput);
        tm.ReInit(charStream,
            (state != null)
                ? ((Integer)state).intValue()
                : tm.defaultLexState
        );
    }

}
