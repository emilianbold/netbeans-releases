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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.lexer.nbbridge;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageProvider;
import org.openide.util.Lookup;

/**
 *
 * @author vita
 */
public final class MimeLookupLanguageProvider extends LanguageProvider {
    
    public MimeLookupLanguageProvider() {
        super();
    }

    public Language<? extends TokenId> findLanguage(String mimePath) {
        Lookup lookup = MimeLookup.getLookup(MimePath.parse(mimePath));
        return (Language<? extends TokenId>)lookup.lookup(Language.class);
    }

    public LanguageEmbedding<? extends TokenId> findLanguageEmbedding(
    Token<? extends TokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
        Lookup lookup = MimeLookup.getLookup(MimePath.parse(languagePath.mimePath()));
        LanguagesEmbeddingMap map = lookup.lookup(LanguagesEmbeddingMap.class);
        return map == null ? null : map.getLanguageEmbeddingForTokenName(token.id().name());
    }

}
