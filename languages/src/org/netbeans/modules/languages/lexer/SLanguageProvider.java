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

import java.util.Map;
import org.netbeans.api.languages.LanguagesManager;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.languages.Evaluator;
import org.netbeans.modules.languages.LanguagesManagerImpl;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageProvider;
import org.openide.ErrorManager;

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

    public LanguageEmbedding<? extends TokenId> findLanguageEmbedding (
        Token token, 
        LanguagePath tokenLanguage, 
        InputAttributes inputAttributes
    ) {
        String mimeType = tokenLanguage.innerLanguage ().mimeType ();
        if (!LanguagesManager.getDefault ().getSupportedMimeTypes ().contains (mimeType))
            return null;
        try {
            org.netbeans.modules.languages.Language language = 
                ((LanguagesManagerImpl) LanguagesManager.getDefault ()).
                getLanguage (mimeType);
            Map properties = (Map) language.getFeature (
                org.netbeans.modules.languages.Language.IMPORT, 
                token.id ().name ()
            );
            if (properties == null)
                return null;
            String innerMT = (String) ((Evaluator) properties.get ("mimeType")).evaluate ();
            Language l = Language.find (innerMT);
            int startSkipLength = 0, endSkipLength = 0;
            if (properties.containsKey ("startSkipLength")) {
                String s = (String) ((Evaluator) properties.get ("startSkipLength")).evaluate ();
                startSkipLength = Integer.parseInt (s);
            } else {
                Integer i = (Integer) token.getProperty ("startSkipLength");
                if (i != null)
                    startSkipLength = i.intValue ();
            }
            if (properties.containsKey ("endSkipLength")) {
                String s = (String) ((Evaluator) properties.get ("endSkipLength")).evaluate ();
                endSkipLength = Integer.parseInt (s);
            } else {
                Integer i = (Integer) token.getProperty ("endSkipLength");
                if (i != null)
                    endSkipLength = i.intValue ();
            }
            return LanguageEmbedding.create (l, startSkipLength, endSkipLength);
        } catch (ParseException ex) {
            ErrorManager.getDefault ().notify (ex);
            return null;
        }
    }
}
