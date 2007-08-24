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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.apt.utils.APTStringManager;
import org.netbeans.modules.cnd.modelimpl.csm.BuiltinTypes;
import org.netbeans.modules.cnd.modelimpl.csm.BuiltinTypes.BuiltInUID;
import org.netbeans.modules.cnd.modelimpl.repository.KeyObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.ClassifierUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.DeclarationUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.FileUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.IncludeUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.MacroUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.NamespaceUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.ProjectUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.TypedefUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.UnnamedClassifierUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.UnnamedOffsetableDeclarationUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.UnresolvedClassUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.UnresolvedFileUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.UnresolvedNamespaceUID;
import org.netbeans.modules.cnd.repository.support.AbstractObjectFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 *
 * @author Nickolay Dalmatov
 */
public class UIDObjectFactory extends AbstractObjectFactory {
    private static UIDObjectFactory theFactory;
    
    /** Creates a new instance of UIDObjectFactory */
    protected UIDObjectFactory() {
    }
    
    synchronized public static UIDObjectFactory getDefaultFactory() {
        if (theFactory == null) {
            theFactory = new UIDObjectFactory();
        }
        return theFactory;
    }
    
    public void writeUID(CsmUID anUID, DataOutput aStream) throws IOException {
        assert anUID == null || anUID instanceof SelfPersistent;
        super.writeSelfPersistent((SelfPersistent)anUID, aStream);
    }
    
    public CsmUID readUID(DataInput aStream) throws IOException {
        assert aStream != null;
        SelfPersistent out = super.readSelfPersistent(aStream);
        assert out == null || out instanceof CsmUID;
        return (CsmUID)out;
    }
    
    public <T> void writeUIDCollection(Collection<CsmUID<T>> aCollection, DataOutput aStream , boolean sync) throws IOException {
        assert aStream != null;
        if (aCollection == null) {
            aStream.writeInt(NULL_POINTER);
        } else {
            aCollection = sync ? copySyncCollection(aCollection) : aCollection;
            int collSize = aCollection.size();
            aStream.writeInt(collSize);
            
            for (CsmUID uid : aCollection) {
                assert uid != null;
                writeUID(uid, aStream);
            }
        }
    }
    
    public  <T extends Collection> T readUIDCollection(T aCollection, DataInput aStream) throws IOException {
        assert aCollection != null;
        assert aStream != null;
        int collSize = aStream.readInt();
        if (collSize == NULL_POINTER) {
            return null;
        } else {
            for (int i = 0; i < collSize; ++i) {
                CsmUID anUID = readUID(aStream);
                assert anUID != null;
                aCollection.add(anUID);
            }
            return aCollection;
        }
    }
    
    public <T> void writeStringToUIDMap(Map <String, CsmUID<T>> aMap, DataOutput aStream, boolean sync) throws IOException {
        assert aMap != null;
        assert aStream != null;
        aMap = sync ? copySyncMap(aMap) : aMap;
        int collSize = aMap.size();
        aStream.writeInt(collSize);
        
        for (Map.Entry<String, CsmUID<T>> anEntry : aMap.entrySet()) {
            String key = anEntry.getKey();
            assert key != null;
            aStream.writeUTF(key);
            CsmUID anUID = anEntry.getValue();
            assert anUID != null;
            writeUID(anUID, aStream);
        }
        
    }
    
    public void writeStringToArrayUIDMap(Map <String, Object> aMap, DataOutput aStream, boolean sync) throws IOException {
        assert aMap != null;
        assert aStream != null;
        aMap = sync ? copySyncMap(aMap) : aMap;
        int collSize = aMap.size();
        aStream.writeInt(collSize);
        
        for (Map.Entry<String, Object> anEntry : aMap.entrySet()) {
            String key = anEntry.getKey();
            assert key != null;
            aStream.writeUTF(key);
            Object o = anEntry.getValue();
            if (o instanceof CsmUID){
                aStream.writeInt(1);
                writeUID((CsmUID)o, aStream);
            } else {
                CsmUID[] arr = (CsmUID[])o;
                aStream.writeInt(arr.length);
                for(CsmUID uid:arr){
                    assert uid != null;
                    writeUID(uid, aStream);
                }
            }
        }
    }
    
    private static Collection copySyncCollection(Collection col) {
        Collection out;
        synchronized (col) {
            out = new ArrayList(col);
        }
        return out;
    }
    
    private static Map copySyncMap(Map map) {
        Map out;
        synchronized (map) {
            out = new HashMap(map);
        }
        return out;
    }
    
    public <T> void readStringToUIDMap(Map <String, CsmUID<T>> aMap, DataInput aStream, APTStringManager manager) throws IOException {
        assert aMap != null;
        assert aStream != null;
        
        int collSize = aStream.readInt();
        
        for (int i = 0; i < collSize; ++i) {
            String key = aStream.readUTF();
            key = manager == null ? key : manager.getString(key);
            assert key != null;
            CsmUID uid = readUID(aStream);
            assert uid != null;
            aMap.put(key, uid);
        }
    }
    
    public void readStringToArrayUIDMap(Map <String, Object> aMap, DataInput aStream, APTStringManager manager) throws IOException {
        assert aMap != null;
        assert aStream != null;
        
        int collSize = aStream.readInt();
        
        for (int i = 0; i < collSize; ++i) {
            String key = aStream.readUTF();
            key = manager == null ? key : manager.getString(key);
            assert key != null;
            int arrSize = aStream.readInt();
            if (arrSize == 1){
                CsmUID uid = readUID(aStream);
                assert uid != null;
                aMap.put(key, uid);
            } else {
                CsmUID[] uids = new CsmUID[arrSize];
                for(int k = 0; k < arrSize; k++){
                    CsmUID uid = readUID(aStream);
                    assert uid != null;
                    uids[k] = uid;
                }
                aMap.put(key, uids);
            }
        }
    }
    
    protected int getHandler(Object object) {
        int aHandler;
        
        if (object instanceof ProjectUID) {
            aHandler = UID_PROJECT_UID;
        } else if (object instanceof NamespaceUID) {
            aHandler = UID_NAMESPACE_UID;
        } else if (object instanceof FileUID) {
            aHandler = UID_FILE_UID;
        } else if (object instanceof TypedefUID) {
            aHandler = UID_TYPEDEF_UID;
        } else if (object instanceof ClassifierUID) {
            aHandler = UID_CLASSIFIER_UID;
        } else if (object instanceof UnnamedClassifierUID) {
            aHandler = UID_UNNAMED_CLASSIFIER_UID;
        } else if (object instanceof MacroUID) {
            aHandler = UID_MACRO_UID;
        } else if (object instanceof IncludeUID) {
            aHandler = UID_INCLUDE_UID;
        } else if (object instanceof UnnamedOffsetableDeclarationUID) {
            aHandler = UID_UNNAMED_OFFSETABLE_DECLARATION_UID;
        } else if (object instanceof DeclarationUID) {
            aHandler = UID_DECLARATION_UID;
        } else if (object instanceof BuiltInUID) {
            aHandler = UID_BUILT_IN_UID;
        } else if (object instanceof UnresolvedClassUID) {
            aHandler = UID_UNRESOLVED_CLASS;
        } else if (object instanceof UnresolvedFileUID) {
            aHandler = UID_UNRESOLVED_FILE;
        } else if (object instanceof UnresolvedNamespaceUID) {
            aHandler = UID_UNRESOLVED_NAMESPACE;
        } else {
            throw new IllegalArgumentException("The UID is an instance of unknow class"); //NOI18N
        }
        
        return aHandler;
    }
    
    protected SelfPersistent createObject(int handler, DataInput aStream) throws IOException {
        
        SelfPersistent anUID;
        boolean share = true;
        switch (handler) {
            case UID_PROJECT_UID:
                anUID = new ProjectUID(aStream);
                break;
                
            case UID_NAMESPACE_UID:
                anUID = new NamespaceUID(aStream);
                break;
                
            case UID_FILE_UID:
                anUID = new FileUID(aStream);
                break;
                
            case UID_TYPEDEF_UID:
                anUID = new TypedefUID(aStream);
                break;
                
            case UID_CLASSIFIER_UID:
                anUID = new ClassifierUID(aStream);
                break;
                
            case UID_UNNAMED_CLASSIFIER_UID:
                anUID = new UnnamedClassifierUID(aStream);
                break;
                
            case UID_MACRO_UID:
                anUID = new MacroUID(aStream);
                break;
                
            case UID_INCLUDE_UID:
                anUID = new IncludeUID(aStream);
                break;
                
            case UID_UNNAMED_OFFSETABLE_DECLARATION_UID:
                anUID = new UnnamedOffsetableDeclarationUID(aStream);
                break;
                
            case UID_DECLARATION_UID:
                anUID = new DeclarationUID(aStream);
                break;
                
            case UID_BUILT_IN_UID:
                anUID = BuiltinTypes.readUID(aStream);
                share = false;
                break;

            case UID_UNRESOLVED_CLASS:
		anUID = new UIDUtilities.UnresolvedClassUID(aStream);
                break;

            case UID_UNRESOLVED_FILE:
		anUID = new UIDUtilities.UnresolvedFileUID(aStream);
                break;

            case UID_UNRESOLVED_NAMESPACE:
		anUID = new UIDUtilities.UnresolvedNamespaceUID(aStream);
                break;
            default:
                throw new IllegalArgumentException("The UID is an instance of unknown class: " + handler); //NOI18N
        }
        if (share) {
            assert anUID != null;
            assert anUID instanceof CsmUID;
            CsmUID shared = UIDManager.instance().getSharedUID((CsmUID)anUID);
            assert shared != null;
            assert shared instanceof SelfPersistent;
            anUID = (SelfPersistent)anUID;
        }
        return anUID;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    //  constants which defines the handle of an UID in the stream
    
    private static final int FIRST_INDEX                = KeyObjectFactory.LAST_INDEX + 1;
    
    private static final int UID_PROJECT_UID            = FIRST_INDEX;
    private static final int UID_NAMESPACE_UID          = UID_PROJECT_UID + 1;
    private static final int UID_FILE_UID               = UID_NAMESPACE_UID + 1;
    private static final int UID_TYPEDEF_UID            = UID_FILE_UID + 1;
    private static final int UID_CLASSIFIER_UID         = UID_TYPEDEF_UID + 1;
    private static final int UID_UNNAMED_CLASSIFIER_UID = UID_CLASSIFIER_UID + 1;
    private static final int UID_MACRO_UID              = UID_UNNAMED_CLASSIFIER_UID + 1;
    private static final int UID_INCLUDE_UID            = UID_MACRO_UID + 1;
    private static final int UID_UNNAMED_OFFSETABLE_DECLARATION_UID = UID_INCLUDE_UID + 1;
    private static final int UID_DECLARATION_UID        = UID_UNNAMED_OFFSETABLE_DECLARATION_UID + 1;
    private static final int UID_BUILT_IN_UID           = UID_DECLARATION_UID + 1;
    
    private static final int UID_UNRESOLVED_CLASS       = UID_BUILT_IN_UID + 1;
    private static final int UID_UNRESOLVED_FILE        = UID_UNRESOLVED_CLASS + 1;
    private static final int UID_UNRESOLVED_NAMESPACE   = UID_UNRESOLVED_FILE + 1;
    
    // index to be used in another factory (but only in one)
    // to start own indeces from the next after LAST_INDEX
    public static final int LAST_INDEX                  = UID_UNRESOLVED_NAMESPACE;
}
