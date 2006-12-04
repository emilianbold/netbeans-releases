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

import org.netbeans.lib.lexer.inc.TokenHierarchyEventInfo;
import org.netbeans.lib.lexer.inc.TokenListChange;

/**
 * Description of the changes made in a token hierarchy.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenHierarchyEvent extends java.util.EventObject {

    private final TokenHierarchyEventInfo info;

    TokenHierarchyEvent(TokenHierarchyEventInfo info) {
        super(info.tokenHierarchyOperation().tokenHierarchy());
        this.info = info;
    }

    /**
     * Get source of this event as a token hierarchy instance.
     */
    public TokenHierarchy<?> tokenHierarchy() {
        return (TokenHierarchy<?>)getSource();
    }
    
    /**
     * Get reason why a token hierarchy event was fired.
     */
    public TokenHierarchyEventType type() {
        return info.type();
    }

    /**
     * Get the token change that occurred in the tokens
     * at the top-level of the token hierarchy.
     */
    public TokenChange<? extends TokenId> tokenChange() {
        return info.tokenChange();
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
        TokenChange<? extends TokenId> tc = tokenChange();
        @SuppressWarnings("unchecked")
        TokenChange<T> tcl = (tc != null && tc.language() == language) ? (TokenChange<T>)tc : null;
        return tcl;
    }
    
    /**
     * Get start offset of the area that was affected by the attached
     * token change(s).
     */
    public int affectedStartOffset() {
        return info.affectedStartOffset();
    }
    
    /**
     * Get end offset of the area that was affected by the attached
     * token change(s).
     * <br/>
     * If there was a text modification the offsets are related
     * to the state after the modification.
     */
    public int affectedEndOffset() {
        return info.affectedEndOffset();
    }

    /**
     * Get offset in the input source where the modification occurred.
     *
     * @return modification offset or -1
     *  if this event's type is not {@link TokenHierarchyEventType#MODIFICATION}.
     */
    public int modificationOffset() {
        return info.modificationOffset();
    }
    
    /**
     * Get number of characters inserted by the text modification
     * that caused this token change.
     *
     * @return number of inserted characters by the modification.
     *  <br/>
     *  Returns 0 
     *  if this event's type is not {@link TokenHierarchyEventType#MODIFICATION}.
     */
    public int insertedLength() {
        return info.insertedLength();
    }
    
    /**
     * Get number of characters removed by the text modification
     * that caused this token change.
     *
     * @return number of inserted characters by the modification.
     *  <br/>
     *  Returns 0 
     *  if this event's type is not {@link TokenHierarchyEventType#MODIFICATION}.
     */
    public int removedLength() {
        return info.removedLength();
    }
    

}