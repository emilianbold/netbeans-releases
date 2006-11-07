/*
 * SLanguageProvider.java
 *
 * Created on October 17, 2006, 10:10 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.languages.lexer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.languages.parser.ParseException;
import org.netbeans.modules.languages.parser.SToken;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.TokenFactory;
import org.netbeans.spi.lexer.LexerRestartInfo;


/**
 *
 * @author Jan Jancura
 */
public class SLanguageHierarchy extends LanguageHierarchy<STokenId> {
    
    private String                  mimeType;
    private Collection<STokenId>    tokenIds;
    private HashMap                 tokensMap;
    
    
    SLanguageHierarchy (String mimeType) {
        this.mimeType = mimeType;
    }
    
    protected Collection<STokenId> createTokenIds () {
        if (tokenIds == null) {
            tokenIds = new ArrayList ();
            tokensMap = new HashMap ();
            Iterator it = getLanguage ().getParser ().getTokens ().iterator ();
            while (it.hasNext ()) {
                SToken t = (SToken) it.next ();
                if (tokensMap.containsKey (t.getType ())) continue;
                STokenId tokenId = new STokenId (
                    t.getType (), 
                    tokenIds.size (), 
                    t.getType ()
                );
                tokenIds.add (tokenId);
                tokensMap.put (t.getType (), tokenId);
            }
            STokenId errorTokenId = new STokenId (
                "error",
                tokenIds.size (), 
                "error"
            );
            tokenIds.add (errorTokenId);
            tokensMap.put ("error", errorTokenId);
        }
        return tokenIds;
    }

    protected Lexer<STokenId> createLexer (LexerRestartInfo<STokenId> info) {
        if (tokensMap == null) createTokenIds ();
        return new SLexer (
            getLanguage (), 
            info.input (), 
            info.tokenFactory (), 
            tokensMap, 
            info.state ()
        );
    }

    protected String mimeType () {
        return mimeType;
    }
    
    
    // other methods ...........................................................
    
    private Language language;
    
    private Language getLanguage () {
        if (language == null)
            try {
                language = LanguagesManager.getDefault ().getLanguage (mimeType);
            } catch (ParseException ex) {
                language = new Language (mimeType);
            }
        return language;
    }
}
