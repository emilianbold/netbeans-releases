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

import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.spi.lexer.TokenPropertyProvider;

/**
 * Token that holds information about preprocessed characters.
 *
 * <p>
 * Instances of this token are more costly than other token types
 * because in addition to regular information they store preprocessed
 * text of the token.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class PropertyToken<T extends TokenId> extends DefaultToken<T> {
    
    private TokenPropertyProvider propertyProvider; // 28 bytes (24-super + 4)
    
    private Object tokenStoreValue; // 32 bytes
    
    public PropertyToken(T id, int length,
    TokenPropertyProvider propertyProvider, Object tokenStoreValue) {
        super(id, length);
        this.propertyProvider = propertyProvider;
        this.tokenStoreValue = tokenStoreValue;
    }
    
    public boolean hasProperties() {
        return true;
    }

    public Object getProperty(Object key) {
        return LexerUtilsConstants.getTokenProperty(this, propertyProvider, key, tokenStoreValue);
    }

    protected String dumpInfoTokenType() {
        return "ProT"; // NOI18N "PrepToken"
    }

}
