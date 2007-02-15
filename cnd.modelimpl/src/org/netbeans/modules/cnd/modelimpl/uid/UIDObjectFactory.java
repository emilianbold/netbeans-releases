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

package org.netbeans.modules.cnd.modelimpl.uid;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.ClassifierUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.FileUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.NamespaceUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.ProjectUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.TypedefUID;

/**
 *
 * @author Nickolay Dalmatov
 */
public class UIDObjectFactory {
    private static UIDObjectFactory theFactory;
    
    /** Creates a new instance of UIDObjectFactory */
    protected UIDObjectFactory() {
    }
    
    synchronized public static UIDObjectFactory getDefaultFactory () {
        if (theFactory == null) {
            theFactory = new UIDObjectFactory();
        }
        return theFactory;
    }
    
    public void write (CsmUID anUID, DataOutput aStream) throws IOException, IllegalArgumentException {
        assert anUID != null;
        assert aStream != null;
        
        int aHandler;
        
        if (anUID instanceof ProjectUID) {
            aHandler = UID_PROJECT_UID;
        } else if (anUID instanceof NamespaceUID) {
            aHandler = UID_NAMESPACE_UID;
        } else if (anUID instanceof FileUID) {
            aHandler = UID_FILE_UID;
        } else if (anUID instanceof TypedefUID) {
            aHandler = UID_TYPEDEF_UID;
        } else if (anUID instanceof ClassifierUID) {
            aHandler = UID_CLASSIFIER_UID;
        } else {
            throw new IllegalArgumentException("The UID is an instance of unknow class"); //NOI18N
        }
        
        aStream.writeInt(aHandler);
        ((KeyBasedUID)anUID).write(aStream);
    }
    
    public CsmUID read (DataInput aStream) throws IOException, IllegalArgumentException {
        assert aStream != null;
        
        int aHandler = aStream.readInt();
        KeyBasedUID anUID;
        
        switch (aHandler) {
            case UID_PROJECT_UID:
                anUID = new ProjectUID();
                break;
                
            case UID_NAMESPACE_UID:
                anUID = new NamespaceUID();
                break;
                
            case UID_FILE_UID:
                anUID = new FileUID();
                break;
                
            case UID_TYPEDEF_UID:
                anUID = new TypedefUID();
                break;
                
            case UID_CLASSIFIER_UID:
                anUID = new ClassifierUID();
                break;
                
            default:
                throw new IllegalArgumentException("The UID is an instance of unknow class"); //NOI18N
        }
        
        ((KeyBasedUID)anUID).read(aStream);
        
        return anUID;
    }
    
  public void writeUIDCollection (Collection<CsmUID> aCollection, DataOutput aStream ) throws IOException, 
                                                                                          IllegalArgumentException {
        assert aCollection != null;
        assert aStream != null;
        
        int collSize = aCollection.size();
        aStream.writeInt(collSize);
        
        Iterator <CsmUID> iter = aCollection.iterator();
        
        while (iter.hasNext()) {
            CsmUID anUID = iter.next();
            write(anUID, aStream);
        }
    }
    
    public void readUIDCollection (Collection<CsmUID> aCollection, DataInput aStream) throws IOException, 
                                                                                          IllegalArgumentException {
       assert aCollection != null;
       assert aStream != null;        
       
       int collSize = aStream.readInt();
       
       for (int i = 0; i < collSize; ++i) {
           CsmUID anUID = read(aStream);
           aCollection.add(anUID);
       }
    }    
    
    public <T> void writeStringToUIDMap (Map <String, CsmUID<T>> aMap, DataOutput aStream) throws IOException, 
                                                                                          IllegalArgumentException {
        assert aMap != null;
        assert aStream != null;
        
        int collSize = aMap.size();
        aStream.writeInt(collSize);
        
        Iterator <Map.Entry<String, CsmUID<T>>> iter = aMap.entrySet().iterator();
        
        while (iter.hasNext()) {
            Map.Entry<String, CsmUID<T>> anEntry = iter.next();
            aStream.writeUTF(anEntry.getKey());
            write(anEntry.getValue(), aStream);
        }        
        
    }
    
    public <T> void readStringToUIDMap (Map <String, CsmUID<T>> aMap, DataInput aStream) throws IOException, 
                                                                                          IllegalArgumentException {
        assert aMap != null;
        assert aStream != null;
        
        int collSize = aStream.readInt();
        
        for (int i = 0; i < collSize; ++i) {
            aMap.put(aStream.readUTF(), read(aStream));
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    //  constants which defines the handle of an UID in the stream
    private static final int UID_PROJECT_UID = 1;
    private static final int UID_NAMESPACE_UID = 2;
    private static final int UID_FILE_UID = 3;
    private static final int UID_TYPEDEF_UID = 4;
    private static final int UID_CLASSIFIER_UID = 5;
    
}
