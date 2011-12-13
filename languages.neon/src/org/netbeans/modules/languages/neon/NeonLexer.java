/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.languages.neon;

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
public class NeonLexer implements Lexer<NeonTokenId> {

    private final NeonColoringLexer scanner;
    private final TokenFactory<NeonTokenId> tokenFactory;

    public NeonLexer(LexerRestartInfo<NeonTokenId> info) {
        scanner = new NeonColoringLexer(info);
        tokenFactory = info.tokenFactory();
    }

    @Override
    public Token<NeonTokenId> nextToken() {
        try {
            NeonTokenId tokenId = scanner.nextToken();
            Token<NeonTokenId> token = null;
            if (tokenId != null) {
                token = tokenFactory.createToken(tokenId);
            }
            return token;
        } catch (IOException ex) {
            Logger.getLogger(NeonLexer.class.getName()).log(Level.SEVERE, null, ex);
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
