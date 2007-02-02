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
import org.netbeans.lib.lexer.TokenList;

/**
 * Token with an explicit text - either serving as a custom text token
 * or a flyweight token.
 * <br/>
 * The represented text can differ from the original content
 * of the recognized text input portion.
 * <br/>
 * Token with the custom text cannot be branched by a language embedding.
 *
 * <p>
 * The text token can act as a flyweight token by calling
 * {@link AbstractToken.makeFlyweight()}. In such case a single token
 * instance is shared for all the occurrences of the token.
 * <br/>
 * The rawOffset is -1 and tokenList reference is null.
 * </p>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class TextToken<T extends TokenId> extends AbstractToken<T> {
    
    private final CharSequence text; // 24 bytes (20-super + 4)

    /**
     * Create text token. The token's text
     * is expected to correspond to the recognized input portion
     * (i.e. the text is not custom).
     * <br/>
     * The token can be made flyweight by using <code>setRawOffset(-1)</code>.
     *
     * @param id non-null identification of the token.
     * @param text non-null text of the token.
     */
    public TextToken(T id, CharSequence text) {
        super(id);
        assert (text != null);
        this.text = text;
    }
    
    private TextToken(T id, TokenList<T> tokenList, int rawOffset, CharSequence text) {
        super(id, tokenList, rawOffset);
        assert (text != null);
        this.text = text;
    }

    public final int length() {
        return text.length();
    }

    public final CharSequence text() {
        return text;
    }
    
    public final TextToken<T> createCopy(TokenList<T> tokenList, int rawOffset) {
        return new TextToken<T>(id(), tokenList, rawOffset, text());
    }
    
    protected String dumpInfoTokenType() {
        return isFlyweight() ? "FlyT" : "TexT"; // NOI18N "TextToken" or "FlyToken"
    }

    public String toString() {
        return text.toString();
    }

}
