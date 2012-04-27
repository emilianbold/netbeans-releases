/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.languages.apacheconf;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ApacheConfLexer implements Lexer<ApacheConfTokenId> {

    private final ApacheConfColoringLexer scanner;
    private final TokenFactory<ApacheConfTokenId> tokenFactory;

    public ApacheConfLexer(LexerRestartInfo<ApacheConfTokenId> info) {
        scanner = new ApacheConfColoringLexer(info);
        tokenFactory = info.tokenFactory();
    }

    @Override
    public Token<ApacheConfTokenId> nextToken() {
        try {
            ApacheConfTokenId tokenId = scanner.nextToken();
            Token<ApacheConfTokenId> token = null;
            if (tokenId != null) {
                token = tokenFactory.createToken(tokenId);
            }
            return token;
        } catch (IOException ex) {
            Logger.getLogger(ApacheConfLexer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Object state() {
        return scanner.getState();
    }

    @Override
    public void release() {
    }

}
