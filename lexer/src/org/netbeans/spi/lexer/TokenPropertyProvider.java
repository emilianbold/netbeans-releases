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
 * Provides extra properties of a token.
 * <br/>
 * Normally each token has an extra instance of the property provider:
 * <pre>
 * final class MyTokenPropertyProvider implements TokenPropertyProvider {
 *
 *     private final Object value;
 *
 *     TokenPropProvider(Object value) {
 *         this.value = value;
 *     }
 *      
 *     public Object getValue (Token token, Object key) {
 *         if ("type".equals(key))
 *             return value;
 *         return null;
 *     }
 *
 * }
 * </pre>
 * <br/>
 * However multiple flyweight instances of the provider may be used to save memory
 * if there are just several values for a property.
 * <br/>
 * Example of two instances of a provider for boolean property "key":
 * <pre>
 * final class MyTokenPropertyProvider implements TokenPropertyProvider {
 *
 *     static final MyTokenPropertyProvider TRUE = new MyTokenPropertyProvider(Boolean.TRUE);
 *
 *     static final MyTokenPropertyProvider FALSE = new MyTokenPropertyProvider(Boolean.FALSE);
 * 
 *     private final Boolean value;
 *
 *     private MyTokenPropertyProvider(Boolean value) {
 *         this.value = value;
 *     }
 *
 *     public Object getValue(Token token, Object key) {
 *         if ("key".equals(key)) {
 *             return value;
 *         }
 *         return null;
 *     }
 *
 * }
 * </pre>
 * <br/>
 * A special kind of token <code>PropertyToken</code> allows to carry token properties.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface TokenPropertyProvider<T extends TokenId> {
    
    /**
     * Get value of a token property.
     *
     * @param token non-null token for which the property is being retrieved.
     *  It might be useful if the property would be computed dynamically.
     * @param key non-null key for which the value should be retrieved.
     * @return value of the property or null if there is no value for the given key.
     */
    Object getValue(Token token, Object key);

}
