/*
 * SLanguageProvider.java
 *
 * Created on October 17, 2006, 10:10 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.languages.lexer;

import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageProvider;

/**
 *
 * @author Jan Jancura
 */
public class SLanguageProvider extends LanguageProvider {
    
    /** Creates a new instance of SLanguageProvider */
    public SLanguageProvider () {
    }

    public Language<STokenId> findLanguage (String mimePath) {
//        System.out.println("findLanguage " + mimePath);
        if (LanguagesManager.getDefault ().getSupportedMimeTypes ().contains (mimePath))
            return new SLanguageHierarchy (mimePath).language ();
        return null;
    }

    public LanguageEmbedding<? extends TokenId> findLanguageEmbedding (Token<? extends TokenId> token, LanguagePath tokenLanguage, InputAttributes inputAttributes) {
        return null;
    }
}
