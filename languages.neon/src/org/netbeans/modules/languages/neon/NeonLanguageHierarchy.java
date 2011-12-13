/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.languages.neon;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class NeonLanguageHierarchy extends LanguageHierarchy<NeonTokenId> {

    @Override
    protected Collection<NeonTokenId> createTokenIds() {
        return EnumSet.allOf(NeonTokenId.class);
    }

    @Override
    protected Lexer<NeonTokenId> createLexer(LexerRestartInfo<NeonTokenId> info) {
        return new NeonLexer(info);
    }

    @Override
    protected String mimeType() {
        return NeonLanguageProvider.MIME_TYPE;
    }

}
