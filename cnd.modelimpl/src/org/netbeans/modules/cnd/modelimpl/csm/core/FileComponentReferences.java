/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.modelimpl.repository.FileReferencesKey;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDProviderIml;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 *
 * @author Alexander Simon
 */
public class FileComponentReferences extends FileComponent implements Persistent, SelfPersistent {

    private static final boolean TRACE = false;
    //private static int request = 0;
    //private static int request_hit = 0;
    //private static int respons = 0;
    //private static int respons_hit = 0;

    public static boolean isKindOf(CsmReference ref, Set<CsmReferenceKind> kinds) {
        return ref instanceof FileComponentReferences.ReferenceImpl && kinds.contains(ref.getKind());
    }

    private final SortedMap<ReferenceImpl, CsmUID<CsmObject>> references;
    private final SortedMap<ReferenceImpl, CsmUID<CsmObject>> type2classifier;
    private final ReadWriteLock referencesLock = new ReentrantReadWriteLock();
    private final CsmUID<CsmFile> fileUID;

    // empty stub
    private static final FileComponentReferences EMPTY = new FileComponentReferences() {

        @Override
        public void put() {
        }
    };

    public static FileComponentReferences empty() {
        return EMPTY;
    }

    public FileComponentReferences(FileImpl file) {
        super(new FileReferencesKey(file));
        references = new TreeMap<ReferenceImpl, CsmUID<CsmObject>>();
        type2classifier = new TreeMap<ReferenceImpl, CsmUID<CsmObject>>();
        this.fileUID = file.getUID();
        put();
    }

    public FileComponentReferences(RepositoryDataInput input) throws IOException {
        super(input);
        UIDObjectFactory defaultFactory = UIDObjectFactory.getDefaultFactory();
        fileUID = defaultFactory.readUID(input);
        references = defaultFactory.readReferencesSortedToUIDMap(input, fileUID);
        type2classifier = defaultFactory.readReferencesSortedToUIDMap(input, fileUID);
    }

    // only for EMPTY static field
    private FileComponentReferences() {
        super((org.netbeans.modules.cnd.repository.spi.Key) null);
        references = new TreeMap<ReferenceImpl, CsmUID<CsmObject>>();
        type2classifier = new TreeMap<ReferenceImpl, CsmUID<CsmObject>>();
        fileUID = null;
    }

    void clean() {
        referencesLock.writeLock().lock();
        try {
            references.clear();
            type2classifier.clear();
        } finally {
            referencesLock.writeLock().unlock();
        }
        put();
    }

    Collection<CsmReference> getReferences(Collection<CsmObject> objects) {
        Set<CsmUID<CsmObject>> searchFor = new HashSet<CsmUID<CsmObject>>(objects.size());
        for(CsmObject obj : objects) {
            CsmUID<CsmObject> uid = UIDs.get(obj);
            searchFor.add(uid);
        }
        List<CsmReference> res = new ArrayList<CsmReference>();
        referencesLock.readLock().lock();
        try {
            for(Map.Entry<ReferenceImpl, CsmUID<CsmObject>> entry : references.entrySet()) {
                if (searchFor.contains(entry.getValue())){
                    res.add(entry.getKey());
                }
            }
        } finally {
            referencesLock.readLock().unlock();
        }
        return res;
    }

    Collection<CsmReference> getReferences() {
        List<CsmReference> res = new ArrayList<CsmReference>();
        referencesLock.readLock().lock();
        try {
            for(Map.Entry<ReferenceImpl, CsmUID<CsmObject>> entry : references.entrySet()) {
                res.add(entry.getKey());
            }
        } finally {
            referencesLock.readLock().unlock();
        }
        return res;
    }

    CsmReference getReference(int offset) {
        return getReferenceImpl(offset, references);
    }

    CsmReference getResolvedReference(CsmReference ref) {
        referencesLock.readLock().lock();
        try {
            for(Map.Entry<ReferenceImpl, CsmUID<CsmObject>> entry : type2classifier.tailMap(new ReferenceImpl(ref.getStartOffset(), ref.getEndOffset(), ref.getText())).entrySet()) {
                if (entry.getKey().start == ref.getStartOffset() && 
                    entry.getKey().end == ref.getEndOffset() &&
                        entry.getKey().identifier.equals(ref.getText())) {
                    //request_hit++;
                    return entry.getKey();
                } else {
                    return null;
                }
            }
        } finally {
            referencesLock.readLock().unlock();
        }
        return null;
    }
    
    CsmReference getReferenceImpl(int offset, SortedMap<ReferenceImpl, CsmUID<CsmObject>> storage) {
        //if (request > 0 && request%1000 == 0) {
        //    System.err.println("Reference statictic:");
        //    System.err.println("\tRequest:"+request+" hit "+request_hit);
        //    System.err.println("\tPut:"+respons+" hit "+respons_hit);
        //}
        //request++;
        referencesLock.readLock().lock();
        try {
            for(Map.Entry<ReferenceImpl, CsmUID<CsmObject>> entry : storage.tailMap(new ReferenceImpl(offset)).entrySet()) {
                if (entry.getKey().start <= offset && offset < entry.getKey().end) {
                    //request_hit++;
                    return entry.getKey();
                } else {
                    return null;
                }
            }
        } finally {
            referencesLock.readLock().unlock();
        }
        return null;
    }

    boolean addResolvedReference(CsmReference ref, CsmObject cls) {
         return addReferenceImpl(ref, cls, type2classifier);
    }

    void removeResolvedReference(CsmReference ref) {
        CsmUID<CsmObject> remove;
        referencesLock.writeLock().lock();
        try {
            remove = type2classifier.remove(new ReferenceImpl(ref.getStartOffset(), ref.getEndOffset(), ref.getText()));
        } finally {
            referencesLock.writeLock().unlock();
        }
        if (remove != null) {
            put();
        }
    }
    
    boolean addReference(CsmReference ref, CsmObject referencedObject) {
         return addReferenceImpl(ref, referencedObject, references);
    }
    
    private boolean addReferenceImpl(CsmReference ref, CsmObject referencedObject, Map<ReferenceImpl, CsmUID<CsmObject>> storage) {
        //respons++;
        if (!UIDCsmConverter.isIdentifiable(referencedObject)) {
            // ignore local references
            if (TRACE) {
                new Exception("Ignore reference to local object "+referencedObject).printStackTrace(System.err); // NOI18N
            }
            return false;
        }
        CsmUID<CsmObject> referencedUID = UIDs.get(referencedObject);
        if (!UIDProviderIml.isPersistable(referencedUID)) {
            // ignore local references
            if (TRACE) {
                new Exception("Ignore reference to local object "+referencedObject).printStackTrace(System.err); // NOI18N
            }
            return false;
        }
        CsmObject owner = ref.getOwner(); // storing
        CsmUID<CsmObject> ownerUID = getUID(owner, "Ignore local owners ", TRACE); // NOI18N
        CsmObject closestTopLevelObject = ref.getClosestTopLevelObject();
        CsmUID<CsmObject> closestTopLevelObjectUID = getUID(closestTopLevelObject, "Why local top level object? ", true); // NOI18N
        assert closestTopLevelObjectUID == null || UIDProviderIml.isPersistable(closestTopLevelObjectUID) : "not persistable top level object " + closestTopLevelObject;
        ReferenceImpl refImpl = new ReferenceImpl(fileUID, ref, referencedUID, ownerUID, closestTopLevelObjectUID);
        //if (ref.getContainingFile().getAbsolutePath().toString().endsWith("ConjunctionScorer.cpp")) {
        //    if (("sort".contentEquals(ref.getText())) && ref.getStartOffset() == 1478) {
        //        Logger.getLogger("xRef").log(Level.INFO, "{0} \n with {1} \n and owner {2}\n", new Object[]{ref, referencedObject, ownerUID});
        //    }
        //}
        referencesLock.writeLock().lock();
        try {
            storage.put(refImpl, referencedUID);
        } finally {
            referencesLock.writeLock().unlock();
        }
        put();
        //respons_hit++;
        return true;
    }

    private CsmUID<CsmObject> getUID(CsmObject csmObject, String warning, boolean trace) {
        CsmUID<CsmObject> csmObjectUID = null;
        if (csmObject != null) {
            if (UIDCsmConverter.isIdentifiable(csmObject)) {
                CsmUID<CsmObject> aClosestTopLevelObjectUID = UIDs.get(csmObject);
                if (UIDProviderIml.isPersistable(aClosestTopLevelObjectUID)) {
                    csmObjectUID = aClosestTopLevelObjectUID;
                } else {
                    if (trace) {
                        Utils.LOG.log(Level.WARNING, "{0} {1}\n {2}", new Object[] {warning, csmObject, new Exception()});
                    }
                }
            } else {
                if (trace) {
                    Utils.LOG.log(Level.WARNING, "{0} {1}\n {2}", new Object[] {warning, csmObject, new Exception()});
                }
            }
        }
        return csmObjectUID;
    }

    @Override
    public void write(RepositoryDataOutput out) throws IOException {
        super.write(out);
        UIDObjectFactory defaultFactory = UIDObjectFactory.getDefaultFactory();
        defaultFactory.writeUID(fileUID, out);
        referencesLock.readLock().lock();
        try {
            out.writeInt(references.size());
            for(Map.Entry<ReferenceImpl, CsmUID<CsmObject>> entry : references.entrySet()) {
                defaultFactory.writeUID(entry.getValue(), out);
                entry.getKey().write(defaultFactory, out);
            }
            out.writeInt(type2classifier.size());
            for(Map.Entry<ReferenceImpl, CsmUID<CsmObject>> entry : type2classifier.entrySet()) {
                defaultFactory.writeUID(entry.getValue(), out);
                entry.getKey().write(defaultFactory, out);
            }
        } finally {
            referencesLock.readLock().unlock();
        }
    }

    public static final class ReferenceImpl implements CsmReference, Comparable<ReferenceImpl>{
        private final CsmUID<CsmFile> file;
        private final CsmReferenceKind refKind;
        private final CsmUID<CsmObject> refObj;
        private final int start;
        private final int end;
        private final CharSequence identifier;
        private final CsmUID<CsmObject> ownerUID;
        private final CsmUID<CsmObject> closestTopLevelObjectUID;

        // to search
        private ReferenceImpl(int start) {
            this.start = start;
            this.end = start;
            this.file = null;
            this.refKind = null;
            this.refObj = null;
            this.identifier = null;
            this.ownerUID = null;
            this.closestTopLevelObjectUID = null;
        }

        // to remove
        private ReferenceImpl(int start, int end, CharSequence identifier) {
            this.start = start;
            this.end = end;
            this.file = null;
            this.refKind = null;
            this.refObj = null;
            this.identifier = identifier;
            this.ownerUID = null;
            this.closestTopLevelObjectUID = null;
        }

        private ReferenceImpl(CsmUID<CsmFile> fileUID, CsmReference delegate, CsmUID<CsmObject> refObj, CsmUID<CsmObject> ownerUID, CsmUID<CsmObject> closestTopLevelObjectUID) {
            this.file = fileUID;
            this.refKind = delegate.getKind();
            this.refObj = refObj;
            assert refObj != null;
            this.start = PositionManager.createPositionID(fileUID, delegate.getStartOffset(), PositionManager.Position.Bias.FOWARD);
            this.end = PositionManager.createPositionID(fileUID, delegate.getEndOffset(), PositionManager.Position.Bias.BACKWARD);
            this.identifier = NameCache.getManager().getString(delegate.getText());
            this.ownerUID = ownerUID;
            this.closestTopLevelObjectUID = closestTopLevelObjectUID;
        }

        public ReferenceImpl(CsmUID<CsmFile> fileUID, CsmUID<CsmObject> refObj, UIDObjectFactory defaultFactory, RepositoryDataInput input) throws IOException {
            this.file = fileUID;
            this.refObj = refObj;
            assert refObj != null;
            this.start = input.readInt();
            this.end = input.readInt();
            this.identifier = PersistentUtils.readUTF(input, NameCache.getManager());
            this.refKind = CsmReferenceKind.values()[input.readByte()];
            this.ownerUID = defaultFactory.readUID(input);
            this.closestTopLevelObjectUID = defaultFactory.readUID(input);
        }

        private void write(UIDObjectFactory defaultFactory, RepositoryDataOutput out) throws IOException {
            out.writeInt(this.start);
            out.writeInt(this.end);
            PersistentUtils.writeUTF(identifier, out);
            out.writeByte(this.refKind.ordinal());
            defaultFactory.writeUID(this.ownerUID, out);
            defaultFactory.writeUID(this.closestTopLevelObjectUID, out);
        }

        @Override
        public CsmReferenceKind getKind() {
            return refKind;
        }

        @Override
        public CsmObject getReferencedObject() {
            CsmObject out = UIDCsmConverter.UIDtoCsmObject(refObj);
            if (out == null) {
                Logger.getLogger("xRef").log(Level.INFO, "how can we store nulls? {0}", refObj); // NOI18N
            }
            return out;
        }

        @Override
        public CsmObject getOwner() {
            return UIDCsmConverter.UIDtoCsmObject(ownerUID);
        }

        @Override
        public CsmObject getClosestTopLevelObject() {
            return UIDCsmConverter.UIDtoCsmObject(closestTopLevelObjectUID);
        }
        
        @Override
        public CsmFile getContainingFile() {
            return file.getObject();
        }

        @Override
        public int getStartOffset() {
            return PositionManager.getOffset(file, start);
        }

        @Override
        public int getEndOffset() {
            return PositionManager.getOffset(file, end);
        }

        @Override
        public Position getStartPosition() {
            return PositionManager.getPosition(file, start);
        }

        @Override
        public Position getEndPosition() {
            return PositionManager.getPosition(file, end);
        }

        @Override
        public CharSequence getText() {
            return identifier;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 17 * hash + this.start;
            hash = 17 * hash + this.end;
            hash = 17 * hash + (this.identifier != null ? this.identifier.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ReferenceImpl other = (ReferenceImpl) obj;
            if (this.start != other.start) {
                return false;
            }
            if (this.end != other.end) {
                return false;
            }
            if (this.identifier != other.identifier && (this.identifier == null || !this.identifier.equals(other.identifier))) {
                return false;
            }
            return true;
        }
        @Override
        public int compareTo(ReferenceImpl o) {
            int res = start - o.end;
            if (res > 0) {
                return res;
            }
            res = end - o.start;
            if (res < 0) {
                return res;
            }
            // we are equal now
            res = 0;
            if (identifier != null && o.identifier != null) {
                res = identifier.hashCode() - o.identifier.hashCode();
            }
            return res;
        }

        @Override
        public String toString() {
            return identifier+"["+start+","+end+"] file=" + file + ";refKind=" + refKind + ";refObj=" + refObj + ";topUID=" + closestTopLevelObjectUID + ";ownerUID=" + ownerUID + '}'; // NOI18N
        }
    }
}
