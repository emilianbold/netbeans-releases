/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.languages.apacheconf;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ApacheConfLanguageHierarchy extends LanguageHierarchy<ApacheConfTokenId> {

    @Override
    protected Collection<ApacheConfTokenId> createTokenIds() {
        return EnumSet.allOf(ApacheConfTokenId.class);
    }

    @Override
    protected Lexer<ApacheConfTokenId> createLexer(LexerRestartInfo<ApacheConfTokenId> info) {
        return new ApacheConfLexer(info);
    }

    @Override
    protected String mimeType() {
        return ApacheConfLanguageProvider.MIME_TYPE;
    }

}
