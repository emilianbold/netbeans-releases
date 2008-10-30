/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import org.netbeans.modules.cnd.utils.cache.APTStringManager;
import org.netbeans.modules.cnd.modelimpl.csm.BuiltinTypes;
import org.netbeans.modules.cnd.modelimpl.csm.BuiltinTypes.BuiltInUID;
import org.netbeans.modules.cnd.modelimpl.csm.Instantiation;
import org.netbeans.modules.cnd.modelimpl.csm.Instantiation.InstantiationUID;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.repository.KeyObjectFactory;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
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
    
    public <T> void writeStringToUIDMap(Map <CharSequence, CsmUID<T>> aMap, DataOutput aStream, boolean sync) throws IOException {
        assert aMap != null;
        assert aStream != null;
        aMap = sync ? copySyncMap(aMap) : aMap;
        int collSize = aMap.size();
        aStream.writeInt(collSize);
        
        for (Map.Entry<CharSequence, CsmUID<T>> anEntry : aMap.entrySet()) {
            String key = anEntry.getKey().toString();
            assert key != null;
            PersistentUtils.writeUTF(key, aStream);
            CsmUID anUID = anEntry.getValue();
            assert anUID != null;
            writeUID(anUID, aStream);
        }
        
    }

    public <T> void writeOffsetSortedToUIDMap(Map <FileImpl.OffsetSortedKey, CsmUID<T>> aMap, DataOutput aStream, boolean sync) throws IOException {
        assert aMap != null;
        assert aStream != null;
        aMap = sync ? copySyncMap(aMap) : aMap;
        int collSize = aMap.size();
        aStream.writeInt(collSize);
        
        for (Map.Entry<FileImpl.OffsetSortedKey, CsmUID<T>> anEntry : aMap.entrySet()) {
            anEntry.getKey().write(aStream);
            CsmUID anUID = anEntry.getValue();
            assert anUID != null;
            writeUID(anUID, aStream);
        }
    }

    public <T> void writeNameSortedToUIDMap(Map <FileImpl.NameSortedKey, CsmUID<T>> aMap, DataOutput aStream, boolean sync) throws IOException {
        assert aMap != null;
        assert aStream != null;
        aMap = sync ? copySyncMap(aMap) : aMap;
        int collSize = aMap.size();
        aStream.writeInt(collSize);
        
        for (Map.Entry<FileImpl.NameSortedKey, CsmUID<T>> anEntry : aMap.entrySet()) {
            anEntry.getKey().write(aStream);
            CsmUID anUID = anEntry.getValue();
            assert anUID != null;
            writeUID(anUID, aStream);
        }
    }
    
    public void writeStringToArrayUIDMap(Map <CharSequence, Object> aMap, DataOutput aStream, boolean sync) throws IOException {
        assert aMap != null;
        assert aStream != null;
        aMap = sync ? copySyncMap(aMap) : aMap;
        int collSize = aMap.size();
        aStream.writeInt(collSize);
        
        for (Map.Entry<CharSequence, Object> anEntry : aMap.entrySet()) {
            String key = anEntry.getKey().toString();
            assert key != null;
            PersistentUtils.writeUTF(key, aStream);
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
    
    public <T> void readStringToUIDMap(Map <CharSequence, CsmUID<T>> aMap, DataInput aStream, APTStringManager manager) throws IOException {
        assert aMap != null;
        assert aStream != null;
        
        int collSize = aStream.readInt();
        
        for (int i = 0; i < collSize; ++i) {
            CharSequence key = PersistentUtils.readUTF(aStream);
            key = manager == null ? key : manager.getString(key);
            assert key != null;
            CsmUID uid = readUID(aStream);
            assert uid != null;
            aMap.put(key, uid);
        }
    }

    public <T> void readOffsetSortedToUIDMap(Map <FileImpl.OffsetSortedKey, CsmUID<T>> aMap, DataInput aStream, APTStringManager manager) throws IOException {
        assert aMap != null;
        assert aStream != null;
        
        int collSize = aStream.readInt();
        
        for (int i = 0; i < collSize; ++i) {
            FileImpl.OffsetSortedKey key = new FileImpl.OffsetSortedKey(aStream);
            assert key != null;
            CsmUID uid = readUID(aStream);
            assert uid != null;
            aMap.put(key, uid);
        }
    }
    
    public <T> void readNameSortedToUIDMap(Map <FileImpl.NameSortedKey, CsmUID<T>> aMap, DataInput aStream, APTStringManager manager) throws IOException {
        assert aMap != null;
        assert aStream != null;
        
        int collSize = aStream.readInt();
        
        for (int i = 0; i < collSize; ++i) {
            FileImpl.NameSortedKey key = new FileImpl.NameSortedKey(aStream);
            assert key != null;
            CsmUID uid = readUID(aStream);
            assert uid != null;
            aMap.put(key, uid);
        }
    }
    
    public void readStringToArrayUIDMap(Map <CharSequence, Object> aMap, DataInput aStream, APTStringManager manager) throws IOException {
        assert aMap != null;
        assert aStream != null;
        
        int collSize = aStream.readInt();
        
        for (int i = 0; i < collSize; ++i) {
            CharSequence key = PersistentUtils.readUTF(aStream);
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
        } else if (object instanceof InstantiationUID) {
            aHandler = UID_INSTANTIATION_UID;
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

            case UID_INSTANTIATION_UID:
                anUID = new Instantiation.InstantiationUID(aStream);
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
    private static final int UID_INSTANTIATION_UID      = UID_BUILT_IN_UID + 1;
    
    private static final int UID_UNRESOLVED_CLASS       = UID_INSTANTIATION_UID + 1;
    private static final int UID_UNRESOLVED_FILE        = UID_UNRESOLVED_CLASS + 1;
    private static final int UID_UNRESOLVED_NAMESPACE   = UID_UNRESOLVED_FILE + 1;
    
    // index to be used in another factory (but only in one)
    // to start own indeces from the next after LAST_INDEX
    public static final int LAST_INDEX                  = UID_UNRESOLVED_NAMESPACE;
}
