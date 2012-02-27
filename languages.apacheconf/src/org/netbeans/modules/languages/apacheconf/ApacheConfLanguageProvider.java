/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.languages.apacheconf;

import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
@ServiceProvider(service = LanguageProvider.class)
public class ApacheConfLanguageProvider extends LanguageProvider {

    public static final String MIME_TYPE = "text/x-apache-conf"; // NOI18N

    @Override
    public Language<?> findLanguage(String mimeType) {
        if (MIME_TYPE.equals(mimeType)) {
            return new ApacheConfLanguageHierarchy().language();
        }
        return null;
    }

    @Override
    public LanguageEmbedding<?> findLanguageEmbedding(Token<?> token, LanguagePath lp, InputAttributes ia) {
        return null;
    }

}
