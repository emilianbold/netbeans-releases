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
 * A special kind of token <code>PropertyToken</code> allows to carry token properties.
 * <br/>
 * That token may store a value of one property in its instance
 * (see <code>PropertyToken.tokenStoreValue</code>. If the provider
 * wants to use that field for storing of the value it needs to return
 * the corresponding key for that value from {@link #tokenStoreKey()}.
 * 
 * <p/>
 * Generally this interface can be used in multiple ways:
 * <ul>
 *  <li>
 *    A new instance of the provider per each token.
 *    This is suitable for all situations.
 *  </li>
 * 
 *  <li>
 *    A single instance of the provider for multiple tokens.
 *    Each token may have a specific value of the given property.
 *    <br/>
 *    This might be achieved by returning the particular key
 *    from {@link #tokenStoreKey()} and using the token store value
 *    for the storage of the property value.
 *  </li>
 * 
 *  <li>
 *    Multiple flyweight instances of the provider.
 *    This might be useful if there is just several values for the property.
 *    For example if there is a boolean property there will be two instances
 *    of the provider (one returning <code>Boolean.TRUE</code>
 *    and the other one returning <code>Boolean.FALSE</code>).
 *  </li>
 * </ul>
 *
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
     * @return value of the property or null if there is no value for the given key.
     */
    Object getValue(Token token, Object key);

    /**
     * Get value of a token-store property.
     * <br/>
     * This method is only invoked if {@link #tokenStoreKey()} returned non-null value.
     * <br/>
     * When called for the first time the <code>tokenStoreValue</code>
     * will have the value given to
     * {@link TokenFactory#createPropertyToken(TokenId,int,TokenPropertyProvider,Object)}.
     * <br/>
     * For subsequent invocations of this method the value returned from
     * a last call to it (for the given token) will be used.
     *
     * @param token non-null token for which the property is being retrieved.
     * @param tokenStoreKey non-null key for which the value should be retrieved.
     * @param tokenStoreValue value that was is currently stored in the token.
     * @return value for the tokenStoreKey. The value will be both returned
     *  and stored in the token.
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
