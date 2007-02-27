/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.languages.lexer;

import org.netbeans.api.languages.LanguagesManager;
import org.netbeans.api.languages.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.Language.TokenType;
import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.languages.LanguagesManagerImpl;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;


/**
 *
 * @author Jan Jancura
 */
public class SLanguageHierarchy extends LanguageHierarchy<STokenId> {
    
    private String                      mimeType;
    private Collection<STokenId>        tokenIds;
    private HashMap<String,STokenId>    tokensMap;
    
    
    SLanguageHierarchy (String mimeType) {
        this.mimeType = mimeType;
    }
    
    protected Collection<STokenId> createTokenIds () {
        if (tokenIds == null) {
            tokenIds = new ArrayList<STokenId> ();
            tokensMap = new HashMap<String,STokenId> ();
            Iterator<TokenType> it = getLanguage ().getTokenTypes ().iterator ();
            while (it.hasNext ()) {
                TokenType t = it.next ();
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
            STokenId embeddingTokenId = new STokenId (
                "PE",
                tokenIds.size (), 
                "PE"
            );
            tokenIds.add (embeddingTokenId);
            tokensMap.put ("PE", embeddingTokenId);
        }
        return tokenIds;
    }

    protected Lexer<STokenId> createLexer (LexerRestartInfo<STokenId> info) {
        if (tokensMap == null) createTokenIds ();
        return new SLexer (
            getLanguage (), 
            tokensMap, 
            info
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
                language = ((LanguagesManagerImpl) LanguagesManager.getDefault ()).getLanguage (mimeType);
            } catch (ParseException ex) {
                language = new Language (mimeType);
            }
        return language;
    }
}
