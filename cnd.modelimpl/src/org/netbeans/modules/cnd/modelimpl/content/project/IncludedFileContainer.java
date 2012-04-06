/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.content.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmValidable;
import org.netbeans.modules.cnd.modelimpl.content.project.FileContainer.FileEntry;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.PreprocessorStatePair;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.repository.IncludedFileStorageKey;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.filesystems.FileSystem;

/**
 * container to keep files included from project.
 * @author Vladimir Voskresensky
 */
public final class IncludedFileContainer {
    private final Collection<Entry> list;
    private final ProjectBase srorageListOwner;

    public IncludedFileContainer(ProjectBase startProject) {
        this.srorageListOwner = startProject;
        // create storage for startProject project
        Storage storage = Storage.create(startProject, startProject);
//        Entry startProjectEntry = new Entry(startProject.getUID(), startProject, storage.getKey());
        Entry startProjectEntry = new Entry(startProject.getUID(), startProject, storage);
        list = new ArrayList<IncludedFileContainer.Entry>(1);
        list.add(startProjectEntry);
    }

    public IncludedFileContainer(ProjectBase startProject, RepositoryDataInput aStream) throws IOException {
        this.srorageListOwner = startProject;
        int count = aStream.readInt();
        list = new ArrayList<IncludedFileContainer.Entry>(count);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
//        KeyFactory keyFactory = KeyFactory.getDefaultFactory();
        for (int i = 0; i < count; i++) {
            CsmUID<CsmProject> includedProjectUID = factory.<CsmProject>readUID(aStream);
//            list.add(new Entry(includedProjectUID, startProject, storageKey));
//            Key storageKey = keyFactory.readKey(aStream);
            Storage storage = new Storage(aStream);
            list.add(new Entry(includedProjectUID, startProject, storage));
        }
    }

    public void write(RepositoryDataOutput aStream) throws IOException {
        aStream.writeInt(list.size());
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
//        KeyFactory keyFactory = KeyFactory.getDefaultFactory();
        for (Entry entry : list) {
            factory.writeUID(entry.prjUID, aStream);
            entry.getStorage().write(aStream);
//            keyFactory.writeKey(entry.storageKey, aStream);
        }
    }
    
    private Storage getStorageForProject(ProjectBase startProject, ProjectBase includedFileOwner, boolean createIfAbsent) {
        CsmUID<CsmProject> uid = includedFileOwner.getUID();
        synchronized (list) {
            for (Entry entry : list) {
                if (entry.prjUID.equals(uid)) {
                    return entry.getStorage();
                }
            }
            assert !startProject.equals(includedFileOwner);
            Storage storage = Storage.create(startProject, includedFileOwner);
//            Entry includedProjectEntry = new Entry(includedFileOwner.getUID(), startProject, storage.getKey());
            Entry includedProjectEntry = new Entry(includedFileOwner.getUID(), startProject, storage);
            list.add(includedProjectEntry);
            return includedProjectEntry.getStorage();
        }
    }

    public void putStorage(ProjectBase startProject, ProjectBase includedProject) {
        Storage storage = getStorageForProject(startProject, includedProject, false);
        assert storage != null : "no storage for " + startProject + " and included " + includedProject;
        storage.put();
    }

    public FileEntry getEntryForIncludedFile(FileEntry entryToLockOn, ProjectBase startProject, ProjectBase includedProject, FileImpl includedFile) {
        assert Thread.holdsLock(entryToLockOn.getLock()) : "does not hold lock for " + includedFile;
        Storage storage = getStorageForProject(startProject, includedProject, true);
        return storage.getOrCreateFileEntry(includedFile);
    }

    public Map<CsmUID<CsmProject> , Collection<PreprocessorStatePair>> getPairs(FileImpl fileToSearch) {
        Map<CsmUID<CsmProject>, Collection<PreprocessorStatePair>> out = new HashMap<CsmUID<CsmProject>, Collection<PreprocessorStatePair>>();
        synchronized (list) {
            for (Entry entry : list) {
                Collection<PreprocessorStatePair> pairs = entry.getStorage().getPairs(fileToSearch);
                if (!pairs.isEmpty()) {
                    out.put(entry.prjUID, pairs);
                }
            }
        }
        return out;
    }

    public final static class Storage extends ProjectComponent  {

        private Collection<PreprocessorStatePair> getPairs(FileImpl fileToSearch) {
            CharSequence fileKey = FileContainer.getFileKey(fileToSearch.getAbsolutePath(), false);
            FileEntry entry = myFiles.get(fileKey);
            if (entry != null) {
                return entry.getStatePairs();
            } else {
                return Collections.emptyList();
            }
        }

        private final ConcurrentMap<CharSequence, FileContainer.FileEntry> myFiles = new ConcurrentHashMap<CharSequence, FileContainer.FileEntry>();
        private final FileSystem fileSystem;

        private Storage(Key key, FileSystem fileSystem) {
            super(key);
            this.fileSystem = fileSystem;
        }

        private static Storage create(ProjectBase startProject, ProjectBase includedProject) {
            Storage storage = new Storage(new IncludedFileStorageKey(startProject, includedProject), includedProject.getFileSystem());
            storage.put();
            return storage;
        }

        private FileEntry getOrCreateFileEntry(FileImpl includedFile) {
            CharSequence fileKey = FileContainer.getFileKey(includedFile.getAbsolutePath(), false);
            FileEntry entry = myFiles.get(fileKey);
            if (entry == null) {
                entry = FileContainer.createFileEntry(includedFile);
                FileEntry prev = myFiles.putIfAbsent(fileKey, entry);
                if (prev != null) {
                    // must be called under FileImpl's entry lock
                    throw new ConcurrentModificationException("someone put the same file entry for " + includedFile); // NOI18N
                }
            }
            return entry;
        }

        public Storage(RepositoryDataInput aStream) throws IOException {
            super(aStream);
            fileSystem = PersistentUtils.readFileSystem(aStream);
            FileContainer.readStringToFileEntryMap(fileSystem, aStream, myFiles);
        }

        @Override
        public void write(RepositoryDataOutput aStream) throws IOException {
            super.write(aStream);
            PersistentUtils.writeFileSystem(fileSystem, aStream);
            FileContainer.writeStringToFileEntryMap(aStream, myFiles);
        }
    }
    
    private static final class Entry {

        private final CsmUID<CsmProject> prjUID;
//        private final Key storageKey;
//        private final WeakContainer<Storage> storage;
        private final Storage storage;

//        public Entry(CsmUID<CsmProject> prj, CsmValidable stateOwner, Key storageKey) {
        public Entry(CsmUID<CsmProject> prj, CsmValidable stateOwner, Storage storage) {
            this.prjUID = prj;
//            this.storageKey = storageKey;
//            this.storage = new WeakContainer<Storage>(stateOwner, storageKey);
            this.storage = storage;
        }

        private Storage getStorage() {
//            Storage container = storage.getContainer();
//            assert container != null;
//            return container;
            assert storage != null;
            return storage;
        }
    }
}
