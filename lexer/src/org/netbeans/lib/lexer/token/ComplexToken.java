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

package org.netbeans.lib.lexer.token;

import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.PreprocessedTextStorage;
import org.netbeans.spi.lexer.CharPreprocessor;
import org.netbeans.spi.lexer.TokenPropertyProvider;

/**
 * Token that holds information about preprocessed characters
 * and also carries properties.
 *
 * <p>
 * Instances of this token are more costly than other token types
 * because in addition to regular information they store preprocessed
 * text of the token.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class ComplexToken<T extends TokenId> extends PreprocessedTextToken<T> {

    private final TokenPropertyProvider propertyProvider; // 36 bytes (32-super + 4)

    private final CharSequence customText; // 40 bytes
    
    private final PartType partType; // 44 bytes

    public ComplexToken(T id, int length,
    TokenPropertyProvider propertyProvider, CharSequence customText, PartType partType) {
        super(id, length);
        this.propertyProvider = propertyProvider;
        this.customText = customText;
        this.partType = partType;
    }

    @Override
    public boolean hasProperties() {
        return (propertyProvider != null);
    }

    @Override
    public Object getProperty(Object key) {
        return (propertyProvider != null) ? propertyProvider.getValue(this, key) : null;
    }
    
    @Override
    public CharSequence text() {
        return (customText != null) ? customText : super.text();
    }
    
    @Override
    public PartType partType() {
        return partType;
    }

    @Override
    protected String dumpInfoTokenType() {
        return "PPrT"; // NOI18N "PrepToken"
    }
    
}
