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

package org.netbeans.lib.lexer;

import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.inc.TokenChangeInfo;
import org.netbeans.lib.lexer.inc.TokenHierarchyEventInfo;
import org.netbeans.spi.lexer.LanguageHierarchy;


/**
 * Accessor for the package-private functionality in org.netbeans.api.editor.fold.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class LexerApiPackageAccessor {
    
    private static LexerApiPackageAccessor INSTANCE;
    
    public static LexerApiPackageAccessor get() {
        return INSTANCE;
    }

    /**
     * Register the accessor. The method can only be called once
     * - othewise it throws IllegalStateException.
     * 
     * @param accessor instance.
     */
    public static void register(LexerApiPackageAccessor accessor) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already registered"); // NOI18N
        }
        INSTANCE = accessor;
    }
    
    public abstract <T extends TokenId> Language<T> createLanguage(
    LanguageHierarchy<T> languageHierarchy);

    public abstract <T extends TokenId> LanguageHierarchy<T> languageHierarchy(
    Language<T> language);

    public abstract <T extends TokenId> LanguageOperation<T> languageOperation(
    Language<T> language);
    
    public abstract <I> TokenHierarchy<I> createTokenHierarchy(
    TokenHierarchyOperation<I,?> tokenHierarchyOperation);
    
    public abstract TokenHierarchyEvent createTokenChangeEvent(
    TokenHierarchyEventInfo info);
    
    public abstract <T extends TokenId> TokenChange<T> createTokenChange(
    TokenChangeInfo<T> info);
    
    public abstract <T extends TokenId> TokenChangeInfo<T> tokenChangeInfo(
    TokenChange<T> tokenChange);
    
    public abstract <I> TokenHierarchyOperation<I,?> tokenHierarchyOperation(
    TokenHierarchy<I> tokenHierarchy);

    public abstract <T extends TokenId> TokenSequence<T> createTokenSequence(
    TokenList<T> tokenList);

}
