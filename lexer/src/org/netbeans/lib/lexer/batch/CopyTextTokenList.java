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

package org.netbeans.lib.lexer.batch;

import java.io.Reader;
import java.util.Set;
import org.netbeans.api.lexer.Language;
import org.netbeans.lib.lexer.LexerInputOperation;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.TokenHierarchyOperation;


/**
 * Token list for situation when the input text must be copied.
 * It works together with SkimTokenList instances that act
 * as a filter over this token list.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class CopyTextTokenList<T extends TokenId> extends BatchTokenList<T> {
    
    /** Either reader or char sequence */
    private final Object input;
    
    public CopyTextTokenList(TokenHierarchyOperation<?,T> tokenHierarchyOperation, Reader inputReader,
    Language<T> language, Set<T> skipTokenIds, InputAttributes inputAttributes) {
        super(tokenHierarchyOperation, language, skipTokenIds, inputAttributes);
        this.input = inputReader;
    }
    
    public CopyTextTokenList(TokenHierarchyOperation<?,T> tokenHierarchyOperation, CharSequence inputText,
    Language<T> language, Set<T> skipTokenIds, InputAttributes inputAttributes) {
        super(tokenHierarchyOperation, language, skipTokenIds, inputAttributes);
        this.input = inputText;
    }
    
    public int childTokenOffset(int rawOffset) {
        // Cluster should be used so this method should never be called
        throwShouldNeverBeCalled();
        return 0; // never reached
    }

    public char childTokenCharAt(int rawOffset, int index) {
        // Cluster should be used so this method should never be called
        throwShouldNeverBeCalled();
        return ' '; // never reached
    }
    
    private void throwShouldNeverBeCalled() {
        throw new IllegalStateException("Should never be called"); // NOI18N
    }

    protected LexerInputOperation<T> createLexerInputOperation() {
        return (input instanceof Reader)
            ? new SkimLexerInputOperation<T>(this, (Reader)input)
            : new SkimLexerInputOperation<T>(this, (CharSequence)input);
    }

}
