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

package org.netbeans.modules.cnd.modelimpl.repository;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities.AbstractKey;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities.FileKey;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities.NamespaceKey;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities.ProjectKey;
import org.netbeans.modules.cnd.repository.spi.Key;

/**
 *
 * @author Nickolay Dalmatov
 */
public class KeyObjectFactory {
    private static KeyObjectFactory theFactory;
    
    /** Creates a new instance of KeyObjectFactory */
    protected KeyObjectFactory() {
    }
    
    synchronized public static KeyObjectFactory getDefaultFactory() {
        if (theFactory == null) {
            theFactory = new KeyObjectFactory();
        }
        
        return theFactory;
    }
    
    public void write (Key aKey, DataOutput aStream) throws IOException, IllegalArgumentException {
        assert aKey != null;
        assert aStream != null;
        
        int aHandle ; 
        
        if (aKey instanceof ProjectKey ) {
            aHandle = KEY_PROJECT_KEY;
        }  else if (aKey instanceof NamespaceKey) {
            aHandle = KEY_NAMESPACE_KEY;
        } else if (aKey instanceof FileKey ) {
            aHandle = KEY_FILE_KEY;
        } else {
            throw new IllegalArgumentException("The Key is istance of unknown final class");  // NOI18N
        }

        aStream.writeInt(aHandle);
        ((AbstractKey)aKey).write(aStream);
    }
    
    public Key read (DataInput aStream) throws IOException, IllegalArgumentException {
        assert aStream != null;
        int aHandle = aStream.readInt();
        AbstractKey aKey = null;
        
        if (aHandle == KEY_PROJECT_KEY) {
            aKey = new ProjectKey();
        } else if (aHandle == KEY_NAMESPACE_KEY) {
            aKey = new NamespaceKey();
        } else if (aHandle == KEY_FILE_KEY) {
            aKey = new FileKey();
        } else {
            throw new IllegalArgumentException("The Key is istance of unknown final class");  // NOI18N
        }
        
        aKey.read(aStream);
        return aKey;
    }
    
    public void writeKeyCollection (Collection<Key> aCollection, DataOutput aStream ) throws IOException, 
                                                                                          IllegalArgumentException {
        assert aCollection != null;
        assert aStream != null;
        
        int collSize = aCollection.size();
        aStream.writeInt(collSize);
        
        Iterator <Key> iter = aCollection.iterator();
        
        while (iter.hasNext()) {
            Key aKey = iter.next();
            write(aKey, aStream);
        }
    }
    
    public void readKeyCollection (Collection<Key> aCollection, DataInput aStream) throws IOException, 
                                                                                          IllegalArgumentException {
       assert aCollection != null;
       assert aStream != null;        
       
       int collSize = aStream.readInt();
       
       for (int i = 0; i < collSize; ++i) {
           Key aKey = read(aStream);
           aCollection.add(aKey);
       }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // constants which defines the handle of a key in the stream
    private static final int KEY_UNKNOWN  = 0;
    private static final int KEY_PROJECT_KEY = 1;
    private static final int KEY_NAMESPACE_KEY = 2;
    private static final int KEY_FILE_KEY = 3;
    
}
