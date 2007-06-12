/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.repository.support;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import org.netbeans.modules.cnd.repository.spi.*;
import org.openide.util.Lookup;

/**
 *
 * @author Nickolay Dalmatov
 */
public abstract class  KeyFactory extends AbstractObjectFactory {
    
    /** default instance */
    private static KeyFactory defaultFactory;
    
    protected KeyFactory() {
    }
    
    /** Static method to obtain the factory.
     * @return the factory
     */
    public static synchronized KeyFactory getDefaultFactory() {
        if (defaultFactory != null) {
            return defaultFactory;
        }
        defaultFactory = (KeyFactory) Lookup.getDefault().lookup(KeyFactory.class);
        if (defaultFactory == null) {
            throw new UnsupportedOperationException("There is no KeyFactory implementation to be used"); //NOI18N
        }
        return defaultFactory;
    }
        
    
    /** Method to serialize a key
     * @param aKey  A key
     * @param aStream A DataOutput Stream
     */
    abstract public void writeKey(Key aKey, DataOutput aStream) throws IOException;
    
    /** Method to deserialize a key
     * @param aStream A DataOutput Stream
     * @return A key
     */
    abstract public Key readKey(DataInput aStream) throws IOException;
    
    /** Method to serialize a colleaction of keys
     * @param aColliection   A collection of keys
     * @param aStream A DataOutput Stream
     */
    abstract public void writeKeyCollection(Collection<Key> aCollection, DataOutput aStream ) throws IOException;
    
    /** Method to deserialize a colleaction of keys
     * @param aColliection   A collection of keys
     * @param aStream A DataOutput Stream
     */
    abstract public void readKeyCollection(Collection<Key> aCollection, DataInput aStream) throws IOException;
}
