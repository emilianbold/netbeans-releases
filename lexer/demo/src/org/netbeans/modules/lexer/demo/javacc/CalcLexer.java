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
import org.netbeans.modules.lexer.javacc.LexerInputCharStream;
import org.netbeans.modules.lexer.javacc.TokenMgrError;

/**
 * Wrapper around generated java token manager
 *
 * @author Miloslav Metelka
 * @version 1.00
 */


public class CalcLexer extends CalcTokenManager implements Lexer {
    
    private CalcLanguage language;
    
    public CalcLexer(CalcLanguage language) {
        super(new LexerInputCharStream());
        this.language = language;
    }

    public Token nextToken() {
        int tokenKind;
        try {
            tokenKind = getNextToken().kind;

            switch (tokenKind) {
                case CalcConstants.ML_COMMENT_START:
                    tokenKind = getNextToken().kind; // get the rest of the token
                    break;
                    
            }

        } catch (TokenMgrError e) {
            throw new IllegalStateException(
                e + "\nTokenMgrError occurred. curLexState=" + curLexState);
        }

        if (tokenKind == 0) { // EOF reached
            return null;
        }

        TokenId id = language.getId(tokenKind);
        if (id == null) {
            throw new IllegalStateException("No tokenID for kind=" + tokenKind);
        }

        return ((LexerInputCharStream)getCharStream()).getLexerInput().createToken(id);
    }
    
    public Object getState() {
        return (curLexState == defaultLexState)
            ? null
            : CalcLanguage.STATE_TABLE[curLexState];
    }

    public void restart(LexerInput input, Object state) {
        LexerInputCharStream charStream = (LexerInputCharStream)getCharStream();
        charStream.setLexerInput(input);
        ReInit(charStream,
            (state != null)
                ? ((Integer)state).intValue()
                : defaultLexState
        );
    }

}
