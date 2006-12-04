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

package org.netbeans.spi.lexer;

import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.TokenHierarchyOperation;

/**
 * Control class for managing token hierarchy of a mutable text input.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenHierarchyControl<I> {

    private MutableTextInput<I> input;

    private TokenHierarchyOperation<I,?> operation;

    TokenHierarchyControl(MutableTextInput<I> input) {
        this.input = input;
    }
    
    private <T extends TokenId> void init() {
        Language<? extends TokenId> language = input.language();
        if (language != null) {
            this.operation = createOperation(language);
        }
    }
    
    private <T extends TokenId> TokenHierarchyOperation<I,T> createOperation(Language<T> language) {
        return new TokenHierarchyOperation<I,T>(input, language);
    }
    
    public synchronized TokenHierarchy<I> tokenHierarchy() {
        if (operation == null) {
            init();
        }
        return (operation != null)
            ? operation.tokenHierarchy()
            : null;
    }
    
    /**
     * Notify that the text of the mutable text input was modified.
     *
     * @param offset &gt;=0 offset where the modification occurred.
     * @param removedLength &gt;=0 number of characters removed from the input.
     * @param removedText text removed from the input. If it's not available
     *  to determine the removed text then this parameter may be null.
     *  <br>
     *  Providing of the removed text allows the incremental
     *  algorithm to use an efficient token validation if possible.
     * @param insertedLength &gt;=0 number of characters inserted at the offset
     *  after the removal.
     */
    public void textModified(int offset,
    int removedLength, CharSequence removedText,
    int insertedLength) {
        if (operation != null) {
            operation.textModified(offset, removedLength, removedText, insertedLength);
        }
    }

    /**
     * Making the token hierarchy inactive will release all the tokens in the hierarchy
     * so that there will be no tokens.
     */
    public void setActive(boolean active) {
        if (operation != null) {
            operation.setActive(active);
        }
    }
    
    public boolean isActive() {
        return (operation != null) ? operation.isActive() : false;
    }

    /**
     * Rebuild token hierarchy completely.
     * <br/>
     * This may be necessary if lexing depends on some input properties
     * that get changed.
     * <br/>
     * This method will drop all present tokens and let them to be lazily recreated.
     */
    public void rebuild() {
        operation.rebuild();
    }

}
