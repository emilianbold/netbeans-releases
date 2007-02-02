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

package org.netbeans.spi.lexer;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;

/**
 * Token validator checks whether an existing token was affected
 * by just performed input source modification so that it needs to be relexed.
 * <br/>
 * If the modification was limited to a single non-flyweight token and the token validator
 * exists for a particular token id then the token validation is attempted.
 * <br/>
 * Token validator can refuse validation by returning null from its only method
 * if the modification affects the token or if the validation is unsure.
 *
 * <p>
 * Token validation is part of fine-tuning of the lexing
 * and should be considered for all tokens that may have significant length
 * such as whitespace or comments.
 * <br/>
 * The advantage of validation is that compared to lexing
 * it typically only explores the modified characters and few adjacent characters.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface TokenValidator<T extends TokenId> {
    
    /**
     * This method is invoked in mutable environments prior lexer invocation
     * to check whether token in which the text modification occurred
     * was only slightly modified by the performed modification and the lexer's
     * invocation is not necessary.
     * <br/>
     * Typically the token can be validated by returning the token with the same
     * token id (just with different length that can be determined
     * by <code>tokenText.length()</code>).
     * <br/>
     * But the validator can also return a token with different token id
     * (e.g. the identifier can become a keyword after the modification).
     *
     * @param token non-null token affected by the modification. The token's text
     *  is undefined and must not be retrieved from the token at this time.
     * @param factory non-null for producing of the new token to be returned.
     * @param tokenText non-null text of the token already affected by the modification.
     * @param modRelOffset &gt;0 offset of the text removal/insertion inside the token.
     * @param insertedLength &gt;0 length of the inserted text.
     * @return a new token instance produced by the token factory.
     *  <br/>
     *  Null should be returned if the token must be relexed or if the validator
     *  is unsure whether it's able to resolve the situation properly.
     */
    Token<T> validateToken(Token<T> token,
    TokenFactory<T> factory,
    CharSequence tokenText, int modRelOffset,
    int removedLength, CharSequence removedText,
    int insertedLength, CharSequence insertedText);

}
