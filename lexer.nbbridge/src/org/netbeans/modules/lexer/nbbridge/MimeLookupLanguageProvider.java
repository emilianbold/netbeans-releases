/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.lexer.nbbridge;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageProvider;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author vita
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.lexer.LanguageProvider.class)
public final class MimeLookupLanguageProvider extends LanguageProvider {
    
    private final Map<String, Lookup.Result<Language>> langLkpResultsMap = 
                  new HashMap<>();
    private final Map<String, Lookup.Result<LanguagesEmbeddingMap>> embeddingsLkpResultsMap = 
                  new HashMap<>();
    private final String LOCK = new String("MimeLookupLanguageProvider.LOCK"); //NOI18N
    
    public MimeLookupLanguageProvider() {
        super();
    }

    public Language<?> findLanguage(String mimeType) {
        Lookup lookup = MimeLookup.getLookup(MimePath.parse(mimeType));
        
        //268649: add lookup listener for Language.class
        synchronized (LOCK) {
            Lookup.Result result = langLkpResultsMap.get(mimeType);
            if (result == null) {
                result = lookup.lookup(new Lookup.Template(Language.class));
                result.addLookupListener((LookupEvent evt) -> {
                    firePropertyChange(PROP_LANGUAGE);
                });
                langLkpResultsMap.put(mimeType, result);
            }
        }
        
        return (Language<?>)lookup.lookup(Language.class);
    }

    public LanguageEmbedding<?> findLanguageEmbedding(
    Token<?> token, LanguagePath languagePath, InputAttributes inputAttributes) {
        String mimePath = languagePath.mimePath();
        Lookup lookup = MimeLookup.getLookup(MimePath.parse(mimePath));
        
        //268649: add lookup listener for LanguagesEmbeddingMap.class            
        synchronized (LOCK) {
            Lookup.Result result = embeddingsLkpResultsMap.get(mimePath);
            if (result == null) {
                result = lookup.lookup(new Lookup.Template(LanguagesEmbeddingMap.class));
                result.addLookupListener((LookupEvent evt) -> {
                    firePropertyChange(PROP_EMBEDDED_LANGUAGE);
                });
                embeddingsLkpResultsMap.put(mimePath, result);
            }
        }

        LanguagesEmbeddingMap map = lookup.lookup(LanguagesEmbeddingMap.class);
        return map == null ? null : map.getLanguageEmbeddingForTokenName(token.id().name());
    }

}
