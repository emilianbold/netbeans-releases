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

/**
 * Token interface encapsulates identification
 * and text of token created by {@link Lexer} using
 * {@link LexerInput#createToken(TokenId)}
 * or {@link LexerInput#createToken(TokenId, int tokenLength)}.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface Token {

    /**
     * Get identification of this token.
     * @return non-null identification of this token.
     */
    public TokenId getId();

    /**
     * Get text of this token (also called image)
     * as character sequence.
     * @return non-null and non-empty text of this token.
     *  <BR>Character sequence being returned must implement <CODE>hashCode()</CODE>
     *  in the same way like in <CODE>java.lang.String</CODE>
     *  and it must return <CODE>true</CODE> from <CODE>equals()</CODE>
     *  for all objects implementing <CODE>java.lang.CharSequence</CODE>
     *  (including <CODE>java.lang.String</CODE>) containing the same characters
     *  in the same order as the character sequence being returned.
     */
    public CharSequence getText();

}

