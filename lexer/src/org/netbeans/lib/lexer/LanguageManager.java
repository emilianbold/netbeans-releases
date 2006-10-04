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

package org.netbeans.lib.lexer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguageDescription;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.LanguageProvider;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.TokenFactory;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author vita
 */
public final class LanguageManager extends LanguageProvider implements LookupListener, PropertyChangeListener {
    
    private static final LanguageDescription NO_LANG = new LanguageHierarchy() {
        protected Lexer createLexer(LexerInput input, TokenFactory tokenFactory, Object state, LanguagePath languagePath, InputAttributes inputAttributes) {
            return null;
        }
        protected Collection createTokenIds() {
            return Collections.EMPTY_LIST;
        }
        protected LanguageEmbedding embedding(Token token, boolean tokenComplete, LanguagePath languagePath, InputAttributes inputAttributes) {
            return null;
        }
        protected String mimeType() {
            return "obscure/no-language-marker"; //NOI18N
        }
    }.language();
    
    private static LanguageManager instance = null;
    
    public static synchronized LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }
    
    private Lookup.Result<LanguageProvider> lookupResult = null;

    private List<LanguageProvider> providers = Collections.<LanguageProvider>emptyList();
    private HashMap<String, WeakReference<LanguageDescription>> langCache = new HashMap<String, WeakReference<LanguageDescription>>();
    private WeakHashMap<Token, WeakReference<LanguageDescription>> tokenLangCache = new WeakHashMap<Token, WeakReference<LanguageDescription>>();
    
    private final String LOCK = new String("LanguageManager.LOCK");
    
    /** Creates a new instance of LanguageManager */
    private LanguageManager() {
        lookupResult = Lookup.getDefault().lookup(new Lookup.Template<LanguageProvider>(LanguageProvider.class));
        lookupResult.addLookupListener(this);
        refreshProviders();
    }

    // -------------------------------------------------------------------
    //  LanguageProvider implementation
    // -------------------------------------------------------------------
    
    public LanguageDescription findLanguage(String mimePath) {
        synchronized(LOCK) {
            WeakReference<LanguageDescription> ref = langCache.get(mimePath);
            LanguageDescription lang = ref == null ? null : ref.get();
            
            if (lang == null) {
                for(LanguageProvider p : providers) {
                    if (null != (lang = p.findLanguage(mimePath))) {
                        break;
                    }
                }
                
                if (lang == null) {
                    lang = NO_LANG;
                }
                
                langCache.put(mimePath, new WeakReference<LanguageDescription>(lang));
            }
            
            return lang == NO_LANG ? null : lang;
        }
    }

    public LanguageDescription findEmbeddedLanguage(LanguagePath tokenLanguage, Token token, InputAttributes inputAttributes) {
        synchronized(LOCK) {
            WeakReference<LanguageDescription> ref = tokenLangCache.get(token);
            LanguageDescription lang = ref == null ? null : ref.get();
            
            if (lang == null) {
                for(LanguageProvider p : providers) {
                    if (null != (lang = p.findEmbeddedLanguage(tokenLanguage, token, inputAttributes))) {
                        break;
                    }
                }
                
                if (lang == null) {
                    lang = NO_LANG;
                }
                
                tokenLangCache.put(token, new WeakReference<LanguageDescription>(lang));
            }
            
            return lang == NO_LANG ? null : lang;
        }
    }

    // -------------------------------------------------------------------
    //  LookupListener implementation
    // -------------------------------------------------------------------
    
    public void resultChanged(LookupEvent ev) {
        refreshProviders();
    }

    // -------------------------------------------------------------------
    //  PropertyChangeListener implementation
    // -------------------------------------------------------------------
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == null) {
            synchronized(LOCK) {
                langCache.clear();
                tokenLangCache.clear();
            }
        } else if (LanguageProvider.PROP_LANGUAGE.equals(evt.getPropertyName())) {
            synchronized(LOCK) {
                langCache.clear();
            }
        } else if (LanguageProvider.PROP_EMBEDDED_LANGUAGE.equals(evt.getPropertyName())) {
            synchronized(LOCK) {
                tokenLangCache.clear();
            }
        }
    }
    
    // -------------------------------------------------------------------
    //  private implementation
    // -------------------------------------------------------------------
    
    private void refreshProviders() {
        Collection<? extends LanguageProvider> newProviders = lookupResult.allInstances();
        
        synchronized(LOCK) {
            for(LanguageProvider p : providers) {
                p.removePropertyChangeListener(this);
            }
            
            providers = new ArrayList<LanguageProvider>(newProviders);
            
            for(LanguageProvider p : providers) {
                p.addPropertyChangeListener(this);
            }
            
            langCache.clear();
            tokenLangCache.clear();
        }
    }

}
