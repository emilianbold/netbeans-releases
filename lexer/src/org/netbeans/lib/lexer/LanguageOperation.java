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

import org.netbeans.api.lexer.LanguageDescription;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.lib.lexer.token.TextToken;
import org.netbeans.spi.lexer.TokenValidator;

/**
 * The operation behind the language hierarchy.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class LanguageOperation<T extends TokenId> {
    
    private static final Object NULL = new Object();
    
    private LanguageHierarchy<T> languageHierarchy;
    
    private LanguageDescription<T> language;
    
    private Object[] tokenValidators;
    
    private FlyItem<T>[] flyItems;
    
    public LanguageOperation(LanguageHierarchy<T> languageHierarchy) {
        this.languageHierarchy = languageHierarchy;
    }
    
    public LanguageHierarchy<T> languageHierarchy() {
        return languageHierarchy;
    }
    
    /**
     * Get description of the language at this level of language hierarchy.
     *
     * @return description of the language.
     */
    public synchronized LanguageDescription<T> language() {
        if (language == null) {
            // Cause api accessor impl to get initialized
            try {
                Class.forName(LanguageDescription.class.getName(), true, LanguageOperation.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                //cannot happen
            }
            
            // Both tokenIds() and tokenCategories() should impose no locks
            // so call in synchronized block
            language = LexerApiPackageAccessor.get().createLanguageDescription(
                    languageHierarchy);
        }
        return language;
    }
    
    public synchronized TokenValidator tokenValidator(TokenId id) {
        if (tokenValidators == null) {
            tokenValidators = new Object[language.maxOrdinal() + 1];
        }
        // Not synced intentionally (no problem to create dup instances)
        Object o = tokenValidators[id.ordinal()];
        if (o == null) {
            o = LexerSpiPackageAccessor.get().createTokenValidator(languageHierarchy(), id);
            if (o == null) {
                o = NULL;
            }
            tokenValidators[id.ordinal()] = o;
        }
        return (o == NULL) ? null : (TokenValidator)o;
    }
    
    public synchronized TextToken<T> getFlyweightToken(T id, String text) {
        TextToken<T> token;
        if (flyItems == null) {
            // Create flyItems array
            @SuppressWarnings("unchecked")
            FlyItem<T>[] arr = (FlyItem<T>[])new FlyItem[language.maxOrdinal() + 1];
            flyItems = arr;
        }
        FlyItem<T> item = flyItems[id.ordinal()];
        if (item == null) {
            token = new TextToken<T>(id, text); // create flyweight token
            token.makeFlyweight();
            flyItems[id.ordinal()] = new FlyItem<T>(token);
        } else { // already a valid item
            token = item.token();
            if (token.text() != text) {
                token = item.token2();
                if (token == null || token.text() != text) {
                    token = item.token();
                    if (!CharSequenceUtilities.textEquals(token.text(), text)) {
                        token = item.token2();
                        if (token == null || !CharSequenceUtilities.textEquals(token.text(), text)) {
                            // Create new token
                            token = new TextToken<T>(id, text);
                            token.makeFlyweight();
                        }
                        item.pushToken(token);
                    }
                } else { // found token2
                    item.pushToken(token);
                }
            }
        }
        assert (token != null); // Should return non-null token
        return token;
    }

    private static final class FlyItem<T extends TokenId> {
        
        private TextToken<T> token;
        
        private TextToken<T> token2;
        
        public FlyItem(TextToken<T> token) {
            this.token = token;
        }
        
        public TextToken<T> token() {
            return token;
        }
        
        public TextToken<T> token2() {
            return token2;
        }
        
        public void pushToken(TextToken<T> token) {
            this.token2 = this.token;
            this.token = token;
        }
        
    }

}
