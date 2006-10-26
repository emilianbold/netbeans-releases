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

package org.netbeans.api.lexer;

import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.inc.TokenListChange;

/**
 * Description of the changes made in a token hierarchy.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenHierarchyEvent extends java.util.EventObject {

    private final TokenChange<? extends TokenId> tokenChange;

    TokenHierarchyEvent(TokenListChange tokenListChange) {
        super(tokenListChange.tokenHierarchyOperation().tokenHierarchy());
        this.tokenChange = new TokenChange<TokenId>(tokenListChange);
    }

    /**
     * Get source of this event as a token hierarchy instance.
     */
    public TokenHierarchy tokenHierarchy() {
        return (TokenHierarchy)getSource();
    }
    
    /**
     * Get the token change that occurred in the tokens
     * at the top-level of the token hierarchy.
     */
    public TokenChange<? extends TokenId> tokenChange() {
        return tokenChange;
    }

    /**
     * Get the token change if the top level of the token hierarchy
     * contains tokens of the given language.
     *
     * @param language non-null language.
     * @return non-null token change if the language at the top level
     *  of the token hierarchy equals to the given language.
     *  Returns null otherwise.
     */
    public <T extends TokenId> TokenChange<T> tokenChange(Language<T> language) {
        @SuppressWarnings("unchecked")
        TokenChange<T> tc = (TokenChange<T>)tokenChange();
        return (tc != null && tc.language() == language) ? tc : null;
    }

    /**
     * Get reason why a token hierarchy event was fired.
     */
    public Type type() {
        return tokenChange.tokenListChange().type();
    }

    /**
     * Token hierarchy event type determines the reason
     * why token hierarchy modification happened.
     */
    public enum Type {
        
        /**
         * The token change was caused by modification (insert/remove) of the characters
         * in the underlying character sequence.
         */
        TEXT_MODIFY,
        
        /**
         * The token change was caused by a partial rebuilding
         * of the token hierarchy.
         * <br/>
         * The partial rebuilding may be caused by changes in input attributes.
         * <br/>
         * This change is notified under modification lock (write lock)
         * of the corresponding input source.
         */
        PARTIAL_REBUILD,
        
        /**
         * The token change was caused by a complete rebuild
         * of the token hierarchy.
         * <br/>
         * That may be necessary because of changes
         * in input attributes that influence the lexing.
         * <br/>
         * When the whole hierarchy is rebuilt only the removed tokens
         * will be notified. There will be no added tokens
         * because they will be created lazily when asked.
         * <br/>
         * This change is notified under modification lock (write lock)
         * of the corresponding input source.
         */
        FULL_REBUILD,
        
        /**
         * The token change was caused by change in activity
         * of the token hierarchy.
         * <br/>
         * The current activity state can be determined by {@link TokenHierarchy#isActive()}.
         * <br/>
         * Firing with this token change type may happen because the input source
         * (for which the token hierarchy was created) has not been used for a long time
         * and its token hierarchy is being deactivated. Or the token hierarchy is just going
         * to be activated again.
         * <br/>
         * The hierarchy will only notify the tokens being removed (for the case when
         * the hierarchy is going to be deactivated). There will be no added tokens
         * because they will be created lazily when asked.
         * <br/>
         * This change is notified under modification lock (write lock)
         * of the corresponding input source.
         */
        ACTIVATION;
        
    }

}