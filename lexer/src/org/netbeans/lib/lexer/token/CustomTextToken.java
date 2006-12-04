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

package org.netbeans.lib.lexer.token;

import org.netbeans.api.lexer.TokenId;

/**
 * Token with a custom text and the token length likely different
 * from text's length.
 * <br/>
 * Token with the custom text cannot be branched by a language embedding.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class CustomTextToken<T extends TokenId> extends DefaultToken<T> {

    private final CharSequence text; // 28 bytes (24-super + 4)

    /**
     * @param id non-null identification of the token.
     * @param length length of the token.
     * @param text non-null text of the token.
     */
    public CustomTextToken(T id, int length, CharSequence text) {
        super(id, length);
        assert (text != null);
        this.text = text;
    }

    public final CharSequence text() {
        return text;
    }
    
    protected String dumpInfoTokenType() {
        return "CusT"; // NOI18N "TextToken" or "FlyToken"
    }
    
}
