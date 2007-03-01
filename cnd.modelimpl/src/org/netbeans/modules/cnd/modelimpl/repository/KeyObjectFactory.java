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
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities.FileKey;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities.IncludeKey;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities.MacroKey;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities.NamespaceKey;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities.OffsetableDeclarationKey;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities.ProjectKey;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.support.AbstractObjectFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 *
 * @author Nickolay Dalmatov
 */
public class KeyObjectFactory extends AbstractObjectFactory {
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
    
    public void writeKey(Key aKey, DataOutput aStream) throws IOException {
        assert aKey instanceof SelfPersistent;
        super.writeSelfPersistent((SelfPersistent)aKey, aStream);
    }
    
    public Key readKey(DataInput aStream) throws IOException {
        assert aStream != null;
        SelfPersistent out = super.readSelfPersistent(aStream);
        assert out instanceof Key;
        return (Key)out;
    }
    
    public void writeKeyCollection(Collection<Key> aCollection, DataOutput aStream ) throws IOException {
        assert aCollection != null;
        assert aStream != null;
        
        int collSize = aCollection.size();
        aStream.writeInt(collSize);
        
        Iterator <Key> iter = aCollection.iterator();
        
        while (iter.hasNext()) {
            Key aKey = iter.next();
            assert aKey != null;
            writeKey(aKey, aStream);
        }
    }
    
    public void readKeyCollection(Collection<Key> aCollection, DataInput aStream) throws IOException {
        assert aCollection != null;
        assert aStream != null;
        
        int collSize = aStream.readInt();
        
        for (int i = 0; i < collSize; ++i) {
            Key aKey = readKey(aStream);
            assert aKey != null;
            aCollection.add(aKey);
        }
    }
    
    protected int getHandler(Object object) {
        int aHandle ;
        
        if (object instanceof ProjectKey ) {
            aHandle = KEY_PROJECT_KEY;
        }  else if (object instanceof NamespaceKey) {
            aHandle = KEY_NAMESPACE_KEY;
        } else if (object instanceof FileKey ) {
            aHandle = KEY_FILE_KEY;
        } else if (object instanceof MacroKey) {
            aHandle = KEY_MACRO_KEY;
        } else if (object instanceof IncludeKey) {
            aHandle = KEY_INCLUDE_KEY;
        } else if (object instanceof OffsetableDeclarationKey) {
            aHandle = KEY_DECLARATION_KEY;
        } else {
            throw new IllegalArgumentException("The Key is an instance of the unknown final class " + object.getClass().getName());  // NOI18N
        }
        
        return aHandle;
    }
    
    protected SelfPersistent createObject(int handler, DataInput aStream) throws IOException {
        SelfPersistent aKey;
        
        switch (handler) {
            case KEY_PROJECT_KEY:
                aKey = new ProjectKey(aStream);
                break;
            case KEY_NAMESPACE_KEY:
                aKey = new NamespaceKey(aStream);
                break;
            case KEY_FILE_KEY:
                aKey = new FileKey(aStream);
                break;
            case KEY_MACRO_KEY:
                aKey = new MacroKey(aStream);
                break;
            case KEY_INCLUDE_KEY:
                aKey = new IncludeKey(aStream);
                break;
            case KEY_DECLARATION_KEY:
                aKey = new OffsetableDeclarationKey(aStream);
                break;
            default:
                throw new IllegalArgumentException("Unknown hander was provided: " + handler);  // NOI18N
        }
        
        return aKey;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // constants which defines the handle of a key in the stream
    
    private static final int FIRST_INDEX        = AbstractObjectFactory.LAST_INDEX + 1;
    
    public static final int KEY_PROJECT_KEY    = FIRST_INDEX;
    public static final int KEY_NAMESPACE_KEY  = KEY_PROJECT_KEY + 1;
    public static final int KEY_FILE_KEY       = KEY_NAMESPACE_KEY + 1;
    public static final int KEY_MACRO_KEY      = KEY_FILE_KEY + 1;
    public static final int KEY_INCLUDE_KEY    = KEY_MACRO_KEY + 1;
    public static final int KEY_DECLARATION_KEY = KEY_INCLUDE_KEY + 1;
    
    // index to be used in another factory (but only in one) 
    // to start own indeces from the next after LAST_INDEX    
    public static final int LAST_INDEX          = KEY_DECLARATION_KEY;
}
