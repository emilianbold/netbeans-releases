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

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;

/**
 * Provides extra properties of a token.
 * <br/>
 * This interface can be used in two ways:
 * <ul>
 *  <li> A new instance of the provider per each token.
 *    This is generally suitable for all the situations. </li>
 *  <li> A single instance for multiple tokens. Suitable if there is just a single
 *    property - the token can store a value of one property in itself.
 * </ul>
 *
 * <p>
 * A special kind of token <code>PropertyToken</code> allows to carry token properties.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface TokenPropertyProvider<T extends TokenId> {
    
    /**
     * Get value of a property which is not a token-store property.
     *
     * @param token non-null token for which the property is being retrieved.
     * @param key non-null key for which the value should be retrieved.
     * @return value of the property.
     */
    Object getValue(Token token, Object key);

    /**
     * Get value of a token-store property.
     * <br/>
     * This method is only invoked if {@link #tokenStoreKey()} returned non-null value.
     *
     * @param token non-null token for which the property is being retrieved.
     * @param tokenStoreKey non-null key for which the value should be retrieved.
     */
    Object getValue(Token token, Object tokenStoreKey, Object tokenStoreValue);
    
    /**
     * Get a key of the property that is stored in the token.
     *
     * @return key of the property which is stored in the token itself
     *  or null if the property provider does not want any property
     *  to be stored in the token.
     */
    Object tokenStoreKey();

}
