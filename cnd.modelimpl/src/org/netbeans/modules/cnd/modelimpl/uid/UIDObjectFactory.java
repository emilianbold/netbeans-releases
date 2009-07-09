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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.utils.cache.APTStringManager;
import org.netbeans.modules.cnd.modelimpl.csm.BuiltinTypes;
import org.netbeans.modules.cnd.modelimpl.csm.BuiltinTypes.BuiltInUID;
import org.netbeans.modules.cnd.modelimpl.csm.Instantiation;
import org.netbeans.modules.cnd.modelimpl.csm.Instantiation.InstantiationUID;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceImpl;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceImpl.FileNameSortedKey;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl.NameSortedKey;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl.OffsetSortedKey;
import org.netbeans.modules.cnd.modelimpl.repository.KeyObjectFactory;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.ClassifierUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.DeclarationUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.FileUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.IncludeUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.MacroUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.NamespaceUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.ParamListUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.ProjectUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.TypedefUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.UnnamedClassifierUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.UnnamedOffsetableDeclarationUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.UnresolvedClassUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.UnresolvedFileUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.UnresolvedNamespaceUID;
import org.netbeans.modules.cnd.repository.support.AbstractObjectFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;
import org.openide.util.Exceptions;

/**
 *
 * @author Nickolay Dalmatov
 */
public class UIDObjectFactory extends AbstractObjectFactory {

    private static UIDObjectFactory theFactory;

    /** Creates a new instance of UIDObjectFactory */
    protected UIDObjectFactory() {
    }

    public static UIDObjectFactory getDefaultFactory() {
        UIDObjectFactory out = theFactory;
        if (out == null) {
            out = theFactory;
            synchronized (UIDObjectFactory.class) {
                out = theFactory;
                if (out == null) {
                    theFactory = out = new UIDObjectFactory();
                }
            }
        }
        return theFactory;
    }

    public void writeUID(CsmUID<?> anUID, DataOutput aStream) throws IOException {
        if (!(anUID == null || anUID instanceof SelfPersistent)) {
            assert false : anUID + ", " + anUID.getObject();
        }
        super.writeSelfPersistent((SelfPersistent) anUID, aStream);
    }

    @SuppressWarnings("unchecked") // okay
    public <T> CsmUID<T> readUID(DataInput aStream) throws IOException {
        assert aStream != null;
        SelfPersistent out = super.readSelfPersistent(aStream);
        assert out == null || out instanceof CsmUID<?>;
        return (CsmUID<T>) out;
    }

    public <T> void writeUIDCollection(Collection<CsmUID<T>> aCollection, DataOutput aStream, boolean sync) throws IOException {
        assert aStream != null;
        if (aCollection == null) {
            aStream.writeInt(NULL_POINTER);
        } else {
            aCollection = sync ? copySyncCollection(aCollection) : aCollection;
            int collSize = aCollection.size();
            aStream.writeInt(collSize);

            for (CsmUID<T> uid : aCollection) {
                assert uid != null;
                writeUID(uid, aStream);
            }
        }
    }

    public <A, T extends Collection<CsmUID<A>>> T readUIDCollection(T aCollection, DataInput aStream) throws IOException {
        assert aCollection != null;
        assert aStream != null;
        int collSize = aStream.readInt();
        return readUIDCollection(aCollection, aStream, collSize);
    }

    public <A, T extends Collection<CsmUID<A>>> T readUIDCollection(T aCollection, DataInput aStream, int collSize) throws IOException {
        if (collSize == NULL_POINTER) {
            return null;
        } else {
            for (int i = 0; i < collSize; ++i) {
                CsmUID<A> anUID = readUID(aStream);
                assert anUID != null;
                aCollection.add(anUID);
            }
            return aCollection;
        }
    }


    public <T> void writeStringToUIDMap(Map<CharSequence, CsmUID<T>> aMap, DataOutput aStream, boolean sync) throws IOException {
        assert aMap != null;
        assert aStream != null;
        aMap = sync ? copySyncMap(aMap) : aMap;
        int collSize = aMap.size();
        aStream.writeInt(collSize);

        for (Map.Entry<CharSequence, CsmUID<T>> anEntry : aMap.entrySet()) {
            CharSequence key = anEntry.getKey();
            assert key != null;
            PersistentUtils.writeUTF(key, aStream);
            CsmUID<T> anUID = anEntry.getValue();
            assert anUID != null;
            writeUID(anUID, aStream);
        }

    }

    public <T> void writeOffsetSortedToUIDMap(Map<FileImpl.OffsetSortedKey, CsmUID<T>> aMap, DataOutput aStream, boolean sync) throws IOException {
        assert aMap != null;
        assert aStream != null;
        aMap = sync ? copySyncMap(aMap) : aMap;
        int collSize = aMap.size();
        aStream.writeInt(collSize);

        for (Map.Entry<FileImpl.OffsetSortedKey, CsmUID<T>> anEntry : aMap.entrySet()) {
            anEntry.getKey().write(aStream);
            CsmUID<T> anUID = anEntry.getValue();
            assert anUID != null;
            writeUID(anUID, aStream);
        }
    }

    public <T> void writeNameSortedToUIDMap(Map<FileImpl.NameSortedKey, CsmUID<T>> aMap, DataOutput aStream, boolean sync) throws IOException {
        assert aMap != null;
        assert aStream != null;
        aMap = sync ? copySyncMap(aMap) : aMap;
        int collSize = aMap.size();
        aStream.writeInt(collSize);

        for (Map.Entry<FileImpl.NameSortedKey, CsmUID<T>> anEntry : aMap.entrySet()) {
            anEntry.getKey().write(aStream);
            CsmUID<T> anUID = anEntry.getValue();
            assert anUID != null;
            writeUID(anUID, aStream);
        }
    }

    public <T> void writeNameSortedToUIDMap2(Map<NamespaceImpl.FileNameSortedKey, CsmUID<T>> aMap, DataOutput aStream, boolean sync) throws IOException {
        assert aMap != null;
        assert aStream != null;
        aMap = sync ? copySyncMap(aMap) : aMap;
        int collSize = aMap.size();
        aStream.writeInt(collSize);

        for (Map.Entry<NamespaceImpl.FileNameSortedKey, CsmUID<T>> anEntry : aMap.entrySet()) {
            anEntry.getKey().write(aStream);
            CsmUID<T> anUID = anEntry.getValue();
            assert anUID != null;
            writeUID(anUID, aStream);
        }
    }

    public void writeStringToArrayUIDMap(Map<CharSequence, Object> aMap, DataOutput aStream, boolean sync) throws IOException {
        assert aMap != null;
        assert aStream != null;
        aMap = sync ? copySyncMap(aMap) : aMap;
        int collSize = aMap.size();
        aStream.writeInt(collSize);

        for (Map.Entry<CharSequence, Object> anEntry : aMap.entrySet()) {
            CharSequence key = anEntry.getKey();
            assert key != null;
            PersistentUtils.writeUTF(key, aStream);
            Object o = anEntry.getValue();
            if (o instanceof CsmUID<?>) {
                aStream.writeInt(1);
                writeUID((CsmUID<?>) o, aStream);
            } else {
                CsmUID<?>[] arr = (CsmUID<?>[]) o;
                aStream.writeInt(arr.length);
                for (CsmUID<?> uid : arr) {
                    assert uid != null;
                    writeUID(uid, aStream);
                }
            }
        }
    }

    private static <T> Collection<CsmUID<T>> copySyncCollection(Collection<CsmUID<T>> col) {
        Collection<CsmUID<T>> out;
        synchronized (col) {
            out = new ArrayList<CsmUID<T>>(col);
        }
        return out;
    }

    private static <K, V> Map<K, V> copySyncMap(Map<K, V> map) {
        Map<K, V> out;
        synchronized (map) {
            out = new HashMap<K, V>(map);
        }
        return out;
    }

    public <T> void readStringToUIDMap(Map<CharSequence, CsmUID<T>> aMap, DataInput aStream, APTStringManager manager, int collSize) throws IOException {
        for (int i = 0; i < collSize; ++i) {
            CharSequence key = PersistentUtils.readUTF(aStream, manager);
            assert key != null;
            CsmUID<T> uid = readUID(aStream);
            assert uid != null;
            aMap.put(key, uid);
        }
    }

    public TreeMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> readOffsetSortedToUIDMap(DataInput aStream, APTStringManager manager) throws IOException {
        assert aStream != null;
        HelperDeclarationsSortedMap helper = new HelperDeclarationsSortedMap(this, aStream, manager);
        return new TreeMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>>(helper);
    }

    public TreeMap<NameSortedKey, CsmUID<CsmMacro>> readNameSortedToUIDMap(DataInput aStream, APTStringManager manager) throws IOException {
        assert aStream != null;
        HelperMacrosSortedMap helper = new HelperMacrosSortedMap(this, aStream, manager);
        return new TreeMap<NameSortedKey, CsmUID<CsmMacro>>(helper);
    }

    public TreeMap<NamespaceImpl.FileNameSortedKey, CsmUID<CsmNamespaceDefinition>> readNameSortedToUIDMap2(DataInput aStream, APTStringManager manager) throws IOException {
        assert aStream != null;
        HelperNamespaceDefinitionSortedMap helper = new HelperNamespaceDefinitionSortedMap(this, aStream, manager);
        return new TreeMap<NamespaceImpl.FileNameSortedKey, CsmUID<CsmNamespaceDefinition>>(helper);
    }

    public TreeMap<CharSequence, Object> readStringToArrayUIDMap(DataInput aStream, APTStringManager manager) throws IOException {
        assert aStream != null;
        HelperCharSequencesSortedMap helper = new HelperCharSequencesSortedMap(this, aStream, manager);
        return new TreeMap<CharSequence, Object>(helper);
    }

    public TreeMap<CharSequence,CsmUID<CsmNamespaceDefinition>> readStringToUIDMap(DataInput aStream, APTStringManager manager) throws IOException {
        assert aStream != null;
        HelperCharSequencesSortedMap2 helper = new HelperCharSequencesSortedMap2(this, aStream, manager);
        return new TreeMap<CharSequence,CsmUID<CsmNamespaceDefinition>>(helper);
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
        } else if (object instanceof ClassifierUID<?>) {
            aHandler = UID_CLASSIFIER_UID;
        } else if (object instanceof UnnamedClassifierUID<?>) {
            aHandler = UID_UNNAMED_CLASSIFIER_UID;
        } else if (object instanceof MacroUID) {
            aHandler = UID_MACRO_UID;
        } else if (object instanceof IncludeUID) {
            aHandler = UID_INCLUDE_UID;
        } else if (object instanceof ParamListUID<?>) {
            aHandler = UID_PARAM_LIST_UID;
        } else if (object instanceof UnnamedOffsetableDeclarationUID<?>) {
            aHandler = UID_UNNAMED_OFFSETABLE_DECLARATION_UID;
        } else if (object instanceof DeclarationUID<?>) {
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
        boolean share = false;
        switch (handler) {
            case UID_PROJECT_UID:
                share = true;
                anUID = new ProjectUID(aStream);
                break;

            case UID_NAMESPACE_UID:
                share = true;
                anUID = new NamespaceUID(aStream);
                break;

            case UID_FILE_UID:
                share = true;
                anUID = new FileUID(aStream);
                break;

            case UID_TYPEDEF_UID:
                anUID = new TypedefUID(aStream);
                break;

            case UID_CLASSIFIER_UID:
                anUID = new ClassifierUID<CsmOffsetableDeclaration>(aStream);
                break;

            case UID_UNNAMED_CLASSIFIER_UID:
                anUID = new UnnamedClassifierUID<CsmOffsetableDeclaration>(aStream);
                break;

            case UID_MACRO_UID:
                share = true;
                anUID = new MacroUID(aStream);
                break;

            case UID_INCLUDE_UID:
                share = true;
                anUID = new IncludeUID(aStream);
                break;

            // no reason to cache declaration and more detailed uids.
            case UID_PARAM_LIST_UID:
                anUID = new ParamListUID<CsmNamedElement>(aStream);
                break;

            case UID_UNNAMED_OFFSETABLE_DECLARATION_UID:
                anUID = new UnnamedOffsetableDeclarationUID<CsmOffsetableDeclaration>(aStream);
                break;

            case UID_DECLARATION_UID:
                anUID = new DeclarationUID<CsmOffsetableDeclaration>(aStream);
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
            assert anUID instanceof CsmUID<?>;
            CsmUID<?> shared = UIDManager.instance().getSharedUID((CsmUID<?>) anUID);
            assert shared != null;
            assert shared instanceof SelfPersistent;
            anUID = (SelfPersistent) shared;
        }
        return anUID;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////
    //  constants which defines the handle of an UID in the stream
    private static final int FIRST_INDEX = KeyObjectFactory.LAST_INDEX + 1;
    private static final int UID_PROJECT_UID = FIRST_INDEX;
    private static final int UID_NAMESPACE_UID = UID_PROJECT_UID + 1;
    private static final int UID_FILE_UID = UID_NAMESPACE_UID + 1;
    private static final int UID_TYPEDEF_UID = UID_FILE_UID + 1;
    private static final int UID_CLASSIFIER_UID = UID_TYPEDEF_UID + 1;
    private static final int UID_UNNAMED_CLASSIFIER_UID = UID_CLASSIFIER_UID + 1;
    private static final int UID_MACRO_UID = UID_UNNAMED_CLASSIFIER_UID + 1;
    private static final int UID_INCLUDE_UID = UID_MACRO_UID + 1;
    private static final int UID_PARAM_LIST_UID = UID_INCLUDE_UID + 1;
    private static final int UID_UNNAMED_OFFSETABLE_DECLARATION_UID = UID_PARAM_LIST_UID + 1;
    private static final int UID_DECLARATION_UID = UID_UNNAMED_OFFSETABLE_DECLARATION_UID + 1;
    private static final int UID_BUILT_IN_UID = UID_DECLARATION_UID + 1;
    private static final int UID_INSTANTIATION_UID = UID_BUILT_IN_UID + 1;
    private static final int UID_UNRESOLVED_CLASS = UID_INSTANTIATION_UID + 1;
    private static final int UID_UNRESOLVED_FILE = UID_UNRESOLVED_CLASS + 1;
    private static final int UID_UNRESOLVED_NAMESPACE = UID_UNRESOLVED_FILE + 1;
    // index to be used in another factory (but only in one)
    // to start own indeces from the next after LAST_INDEX
    public static final int LAST_INDEX = UID_UNRESOLVED_NAMESPACE;

    private static final Comparator<OffsetSortedKey> OSKComparator = new Comparator<OffsetSortedKey>() {
       public int compare(OffsetSortedKey o1, OffsetSortedKey o2) {
            return o1.compareTo(o2);
        }
    };

    private static final class HelperDeclarationsSortedMap implements SortedMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> {
        private final DataInput aStream;
        private final int size;
        private final UIDObjectFactory factory;
        private final APTStringManager manager;

        private HelperDeclarationsSortedMap(UIDObjectFactory factory, DataInput aStream, APTStringManager manager) throws IOException {
            size = aStream.readInt();
            this.aStream = aStream;
            this.factory = factory;
            this.manager = manager;
        }
        public Comparator<? super OffsetSortedKey> comparator() {
            return OSKComparator;
        }
        public SortedMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> subMap(OffsetSortedKey fromKey, OffsetSortedKey toKey) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public SortedMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> headMap(OffsetSortedKey toKey) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public SortedMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> tailMap(OffsetSortedKey fromKey) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public OffsetSortedKey firstKey() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public OffsetSortedKey lastKey() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public int size() {
            return size;
        }
        public boolean isEmpty() {
            return size == 0;
        }
        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public CsmUID<CsmOffsetableDeclaration> get(Object key) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public CsmUID<CsmOffsetableDeclaration> put(OffsetSortedKey key, CsmUID<CsmOffsetableDeclaration> value) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public CsmUID<CsmOffsetableDeclaration> remove(Object key) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public void putAll(Map<? extends OffsetSortedKey, ? extends CsmUID<CsmOffsetableDeclaration>> t) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public void clear() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public Set<OffsetSortedKey> keySet() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public Collection<CsmUID<CsmOffsetableDeclaration>> values() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public Set<Entry<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>>> entrySet() {
            return new Set<Entry<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>>>(){
                public int size() {
                    return size;
                }
                public boolean isEmpty() {
                    return size == 0;
                }
                public boolean contains(Object o) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public Iterator<Entry<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>>> iterator() {
                    return new Iterator<Entry<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>>>(){
                        private int current = 0;
                        public boolean hasNext() {
                            return current < size;
                        }
                        public Entry<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> next() {
                            if (current < size) {
                                current++;
                                try {
                                    final OffsetSortedKey key = new OffsetSortedKey(aStream);
                                    assert key != null;
                                    final CsmUID<CsmOffsetableDeclaration> uid = factory.<CsmOffsetableDeclaration>readUID(aStream);
                                    assert uid != null;
                                    return new Map.Entry<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>>(){
                                        public OffsetSortedKey getKey() {
                                            return key;
                                        }
                                        public CsmUID<CsmOffsetableDeclaration> getValue() {
                                            return uid;
                                        }
                                        public CsmUID<CsmOffsetableDeclaration> setValue(CsmUID<CsmOffsetableDeclaration> value) {
                                            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                                        }
                                    };
                                } catch (IOException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                            return null;
                        }
                        public void remove() {
                            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                        }
                    };
                }
                public Object[] toArray() {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public <T> T[] toArray(T[] a) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean add(Entry<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> o) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean remove(Object o) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean containsAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean addAll(Collection<? extends Entry<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>>> c) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean retainAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean removeAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public void clear() {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
            };
        }
    }

    private static final Comparator<NameSortedKey> NSKComparator = new Comparator<NameSortedKey>() {
       public int compare(NameSortedKey o1, NameSortedKey o2) {
            return o1.compareTo(o2);
        }
    };

    private static final class HelperMacrosSortedMap implements SortedMap<NameSortedKey, CsmUID<CsmMacro>> {
        private final DataInput aStream;
        private final int size;
        private final UIDObjectFactory factory;
        private final APTStringManager manager;

        private HelperMacrosSortedMap(UIDObjectFactory factory, DataInput aStream, APTStringManager manager) throws IOException {
            size = aStream.readInt();
            this.aStream = aStream;
            this.factory = factory;
            this.manager = manager;
        }
        public Comparator<? super NameSortedKey> comparator() {
            return NSKComparator;
        }
        public SortedMap<NameSortedKey, CsmUID<CsmMacro>> subMap(NameSortedKey fromKey, NameSortedKey toKey) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public SortedMap<NameSortedKey, CsmUID<CsmMacro>> headMap(NameSortedKey toKey) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public SortedMap<NameSortedKey, CsmUID<CsmMacro>> tailMap(NameSortedKey fromKey) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public NameSortedKey firstKey() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public NameSortedKey lastKey() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public int size() {
            return size;
        }
        public boolean isEmpty() {
            return size > 0;
        }
        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public CsmUID<CsmMacro> get(Object key) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public CsmUID<CsmMacro> put(NameSortedKey key, CsmUID<CsmMacro> value) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public CsmUID<CsmMacro> remove(Object key) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public void putAll(Map<? extends NameSortedKey, ? extends CsmUID<CsmMacro>> t) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public void clear() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public Set<NameSortedKey> keySet() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public Collection<CsmUID<CsmMacro>> values() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public Set<Entry<NameSortedKey, CsmUID<CsmMacro>>> entrySet() {
            return new Set<Entry<NameSortedKey, CsmUID<CsmMacro>>>(){
                public int size() {
                    return size;
                }
                public boolean isEmpty() {
                    return size > 0;
                }
                public boolean contains(Object o) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public Iterator<Entry<NameSortedKey, CsmUID<CsmMacro>>> iterator() {
                    return new Iterator<Entry<NameSortedKey, CsmUID<CsmMacro>>>(){
                        private int current = 0;
                        public boolean hasNext() {
                            return current < size;
                        }
                        public Entry<NameSortedKey, CsmUID<CsmMacro>> next() {
                            if (current < size) {
                                current++;
                                try {
                                    final NameSortedKey key = new NameSortedKey(aStream);
                                    assert key != null;
                                    final CsmUID<CsmMacro> uid = factory.<CsmMacro>readUID(aStream);
                                    assert uid != null;
                                    return new Map.Entry<NameSortedKey, CsmUID<CsmMacro>>(){
                                        public NameSortedKey getKey() {
                                            return key;
                                        }
                                        public CsmUID<CsmMacro> getValue() {
                                            return uid;
                                        }
                                        public CsmUID<CsmMacro> setValue(CsmUID<CsmMacro> value) {
                                            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                                        }
                                    };
                                } catch (IOException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                            return null;
                        }
                        public void remove() {
                            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                        }
                    };
                }
                public Object[] toArray() {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public <T> T[] toArray(T[] a) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean add(Entry<NameSortedKey, CsmUID<CsmMacro>> o) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean remove(Object o) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean containsAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean addAll(Collection<? extends Entry<NameSortedKey, CsmUID<CsmMacro>>> c) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean retainAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean removeAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public void clear() {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
            };
        }
    }

    private static final class HelperNamespaceDefinitionSortedMap implements SortedMap<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>> {
        private final DataInput aStream;
        private final int size;
        private final UIDObjectFactory factory;
        private final APTStringManager manager;

        private HelperNamespaceDefinitionSortedMap(UIDObjectFactory factory, DataInput aStream, APTStringManager manager) throws IOException {
            size = aStream.readInt();
            this.aStream = aStream;
            this.factory = factory;
            this.manager = manager;
        }
        public Comparator<? super FileNameSortedKey> comparator() {
            return NamespaceImpl.defenitionComparator;
        }
        public SortedMap<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>> subMap(FileNameSortedKey fromKey, FileNameSortedKey toKey) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public SortedMap<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>> headMap(FileNameSortedKey toKey) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public SortedMap<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>> tailMap(FileNameSortedKey fromKey) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public FileNameSortedKey firstKey() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public FileNameSortedKey lastKey() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public int size() {
            return size;
        }
        public boolean isEmpty() {
            return size == 0;
        }
        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public CsmUID<CsmNamespaceDefinition> get(Object key) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public CsmUID<CsmNamespaceDefinition> put(FileNameSortedKey key, CsmUID<CsmNamespaceDefinition> value) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public CsmUID<CsmNamespaceDefinition> remove(Object key) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public void putAll(Map<? extends FileNameSortedKey, ? extends CsmUID<CsmNamespaceDefinition>> t) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public void clear() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public Set<FileNameSortedKey> keySet() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public Collection<CsmUID<CsmNamespaceDefinition>> values() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public Set<Entry<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>>> entrySet() {
            return new Set<Entry<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>>>(){
                public int size() {
                    return size;
                }
                public boolean isEmpty() {
                    return size == 0;
                }
                public boolean contains(Object o) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public Iterator<Entry<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>>> iterator() {
                    return new Iterator<Entry<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>>>(){
                        int current = 0;
                        public boolean hasNext() {
                            return current < size;
                        }
                        public Entry<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>> next() {
                            if (current < size) {
                                current++;
                                try {
                                    final FileNameSortedKey key = new FileNameSortedKey(aStream);
                                    final CsmUID<CsmNamespaceDefinition> value = factory.readUID(aStream);
                                    assert value != null;
                                    return new Entry<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>>(){
                                        public FileNameSortedKey getKey() {
                                            return key;
                                        }
                                        public CsmUID<CsmNamespaceDefinition> getValue() {
                                            return value;
                                        }
                                        public CsmUID<CsmNamespaceDefinition> setValue(CsmUID<CsmNamespaceDefinition> value) {
                                            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                                        }
                                    };
                                } catch (IOException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                            return null;
                        }
                        public void remove() {
                            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                        }
                    };
                }
                public Object[] toArray() {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public <T> T[] toArray(T[] a) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean add(Entry<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>> o) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean remove(Object o) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean containsAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean addAll(Collection<? extends Entry<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>>> c) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean retainAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean removeAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public void clear() {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
            };
        }
    }

    private static final class HelperCharSequencesSortedMap implements SortedMap<CharSequence, Object> {
        private final DataInput aStream;
        private final int size;
        private final UIDObjectFactory factory;
        private final APTStringManager manager;

        private HelperCharSequencesSortedMap(UIDObjectFactory factory, DataInput aStream, APTStringManager manager) throws IOException {
            size = aStream.readInt();
            this.aStream = aStream;
            this.factory = factory;
            this.manager = manager;
        }
        public Comparator<? super CharSequence> comparator() {
            return CharSequenceKey.Comparator;
        }
        public SortedMap<CharSequence, Object> subMap(CharSequence fromKey, CharSequence toKey) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public SortedMap<CharSequence, Object> headMap(CharSequence toKey) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public SortedMap<CharSequence, Object> tailMap(CharSequence fromKey) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public CharSequence firstKey() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public CharSequence lastKey() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public int size() {
            return size;
        }
        public boolean isEmpty() {
            return size == 0;
        }
        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public Object get(Object key) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public Object put(CharSequence key, Object value) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public Object remove(Object key) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public void putAll(Map<? extends CharSequence, ? extends Object> t) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public void clear() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public Set<CharSequence> keySet() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public Collection<Object> values() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public Set<Entry<CharSequence, Object>> entrySet() {
            return new Set<Entry<CharSequence, Object>>(){
                public int size() {
                    return size;
                }
                public boolean isEmpty() {
                    return size == 0;
                }
                public boolean contains(Object o) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public Iterator<Entry<CharSequence, Object>> iterator() {
                    return new Iterator<Entry<CharSequence, Object>>(){
                        private int current = 0;
                        public boolean hasNext() {
                            return current < size;
                        }
                        public Entry<CharSequence, Object> next() {
                            if (current < size) {
                                current++;
                                try {
                                    final CharSequence key = PersistentUtils.readUTF(aStream, manager);
                                    assert key != null;
                                    int arrSize = aStream.readInt();
                                    final Object value;
                                    if (arrSize == 1) {
                                        value = factory.readUID(aStream);
                                        assert value != null;
                                    } else {
                                        CsmUID<?>[] uids = new CsmUID<?>[arrSize];
                                        for (int k = 0; k < arrSize; k++) {
                                            CsmUID<?> uid = factory.readUID(aStream);
                                            assert uid != null;
                                            uids[k] = uid;
                                        }
                                        value = uids;
                                    }
                                    return new Map.Entry<CharSequence, Object>(){
                                        public CharSequence getKey() {
                                            return key;
                                        }
                                        public Object getValue() {
                                            return value;
                                        }
                                        public Object setValue(Object value) {
                                            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                                        }
                                    };
                                } catch (IOException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                            return null;
                        }
                        public void remove() {
                            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                        }
                    };
                }
                public Object[] toArray() {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public <T> T[] toArray(T[] a) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean add(Entry<CharSequence, Object> o) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean remove(Object o) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean containsAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean addAll(Collection<? extends Entry<CharSequence, Object>> c) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean retainAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean removeAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public void clear() {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
            };
        }
    }

    private static final class HelperCharSequencesSortedMap2 implements SortedMap<CharSequence, CsmUID<CsmNamespaceDefinition>> {
        private final DataInput aStream;
        private final int size;
        private final UIDObjectFactory factory;
        private final APTStringManager manager;

        private HelperCharSequencesSortedMap2(UIDObjectFactory factory, DataInput aStream, APTStringManager manager) throws IOException {
            size = aStream.readInt();
            this.aStream = aStream;
            this.factory = factory;
            this.manager = manager;
        }
        public Comparator<? super CharSequence> comparator() {
            return CharSequenceKey.Comparator;
        }
        public SortedMap<CharSequence, CsmUID<CsmNamespaceDefinition>> subMap(CharSequence fromKey, CharSequence toKey) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public SortedMap<CharSequence, CsmUID<CsmNamespaceDefinition>> headMap(CharSequence toKey) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public SortedMap<CharSequence, CsmUID<CsmNamespaceDefinition>> tailMap(CharSequence fromKey) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public CharSequence firstKey() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public CharSequence lastKey() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public int size() {
            return size;
        }
        public boolean isEmpty() {
            return size == 0;
        }
        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public CsmUID<CsmNamespaceDefinition> get(Object key) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public CsmUID<CsmNamespaceDefinition> put(CharSequence key, CsmUID<CsmNamespaceDefinition> value) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public CsmUID<CsmNamespaceDefinition> remove(Object key) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public void putAll(Map<? extends CharSequence, ? extends CsmUID<CsmNamespaceDefinition>> t) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public void clear() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public Set<CharSequence> keySet() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public Collection<CsmUID<CsmNamespaceDefinition>> values() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        public Set<Entry<CharSequence, CsmUID<CsmNamespaceDefinition>>> entrySet() {
            return new Set<Entry<CharSequence, CsmUID<CsmNamespaceDefinition>>>(){
                public int size() {
                    return size;
                }
                public boolean isEmpty() {
                    return size == 0;
                }
                public boolean contains(Object o) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public Iterator<Entry<CharSequence, CsmUID<CsmNamespaceDefinition>>> iterator() {
                    return new Iterator<Entry<CharSequence, CsmUID<CsmNamespaceDefinition>>>(){
                        private int current = 0;
                        public boolean hasNext() {
                            return current < size;
                        }
                        public Entry<CharSequence, CsmUID<CsmNamespaceDefinition>> next() {
                            if (current < size) {
                                current++;
                                try {
                                    final CharSequence key = PersistentUtils.readUTF(aStream, manager);
                                    assert key != null;
                                    final CsmUID<CsmNamespaceDefinition> uid = factory.readUID(aStream);
                                    assert uid != null;
                                    return new Entry<CharSequence, CsmUID<CsmNamespaceDefinition>>(){
                                        public CharSequence getKey() {
                                            return key;
                                        }
                                        public CsmUID<CsmNamespaceDefinition> getValue() {
                                            return uid;
                                        }
                                        public CsmUID<CsmNamespaceDefinition> setValue(CsmUID<CsmNamespaceDefinition> value) {
                                            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                                        }
                                    };
                                } catch (IOException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                            return null;
                        }
                        public void remove() {
                            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                        }
                    };
                }
                public Object[] toArray() {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public <T> T[] toArray(T[] a) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean add(Entry<CharSequence, CsmUID<CsmNamespaceDefinition>> o) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean remove(Object o) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean containsAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean addAll(Collection<? extends Entry<CharSequence, CsmUID<CsmNamespaceDefinition>>> c) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean retainAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public boolean removeAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
                public void clear() {
                    throw new UnsupportedOperationException("Not supported yet."); //NOI18N
                }
            };
        }
    }
}
