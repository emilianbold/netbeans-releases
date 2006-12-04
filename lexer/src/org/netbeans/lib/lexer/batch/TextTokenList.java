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

import java.util.Set;
import org.netbeans.api.lexer.Language;
import org.netbeans.lib.lexer.LexerInputOperation;
import org.netbeans.lib.lexer.TextLexerInputOperation;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.TokenHierarchyOperation;


/**
 * Batch token list over text expressed as character sequence.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TextTokenList<T extends TokenId> extends BatchTokenList<T> {

    private CharSequence inputText;

    public TextTokenList(TokenHierarchyOperation<?,T> tokenHierarchyOperation, CharSequence inputText,
    Language<T> language, Set<T> skipTokenIds, InputAttributes inputAttributes) {
        super(tokenHierarchyOperation, language, skipTokenIds, inputAttributes);
        this.inputText = inputText;
    }
    
    public char childTokenCharAt(int rawOffset, int index) {
        return inputText.charAt(rawOffset + index); // rawOffset is absolute
    }
    
    protected LexerInputOperation<T> createLexerInputOperation() {
        return new TextLexerInputOperation<T>(this, inputText);
    }

}
