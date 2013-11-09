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
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.modelimpl.content.project.FileContainer.FileEntry;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.PreprocessorStatePair;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.repository.IncludedFileStorageKey;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.KeyBasedUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.filesystems.FileSystem;
import org.openide.util.CharSequences;

/**
 * container to keep files included from project.
 * @author Vladimir Voskresensky
 */
public final class IncludedFileContainer extends ProjectComponent {
    private final ConcurrentMap<CsmUID<CsmProject>,MyEntry> myStorage = new ConcurrentHashMap<CsmUID<CsmProject>,MyEntry>();

    public IncludedFileContainer(ProjectBase startProject) {
        super(new IncludedFileStorageKey(startProject));
        put();
    }

    public IncludedFileContainer(RepositoryDataInput aStream) throws IOException {
        super(aStream);
        while(true) {
            CsmUID<CsmProject> uid = UIDObjectFactory.getDefaultFactory().readUID(aStream);
            if (uid == null) {
                break;
            }
            FileSystem fs = PersistentUtils.readFileSystem(aStream);
            MyEntry entry = new MyEntry(fs);
            myStorage.put(uid, entry);
            FileContainer.readStringToFileEntryMap(fs, getIncludedUnitId(uid), aStream, entry.myFiles);
        }
    }

    @Override
    public void write(RepositoryDataOutput aStream) throws IOException {
        super.write(aStream);
        for(Map.Entry<CsmUID<CsmProject>,MyEntry> entry : myStorage.entrySet()) {
            UIDObjectFactory.getDefaultFactory().writeUID(entry.getKey(), aStream);
            PersistentUtils.writeFileSystem(entry.getValue().fileSystem, aStream);
            FileContainer.writeStringToFileEntryMap(getIncludedUnitId(entry.getKey()), aStream, entry.getValue().myFiles);
        }
        UIDObjectFactory.getDefaultFactory().writeUID(null, aStream);
    }

    public void clear() {
        myStorage.clear();
        put();
    }

    public void invalidateIncludeStorage(CsmUID<CsmProject> libraryUID) {
        myStorage.remove(libraryUID);
    }

    public void prepareIncludeStorage(ProjectBase includedProject) {
        final CsmUID<CsmProject> uid = UIDs.get((CsmProject)includedProject);
        if (!myStorage.containsKey(uid)) {
            final MyEntry entry = myStorage.get(uid);
            if (entry == null) {
                myStorage.putIfAbsent(uid, new MyEntry(includedProject.getFileSystem()));
            }
        }
    }

    public Map<CharSequence, FileContainer.FileEntry> getStorageForProject(ProjectBase includedFileOwner) {
        final CsmUID<CsmProject> uid = UIDs.get((CsmProject)includedFileOwner);
        final MyEntry entry = myStorage.get(uid);
        if (entry != null) {
            return Collections.unmodifiableMap(entry.myFiles);
        }
        return null;
    }

    public boolean putStorage(ProjectBase includedProject) {
        final CsmUID<CsmProject> uid = UIDs.get((CsmProject)includedProject);
        MyEntry entry = myStorage.get(uid);
        if (entry != null) {
            put();
            return true;
        }
        return false;
    }

    public FileEntry getOrCreateEntryForIncludedFile(FileEntry entryToLockOn, ProjectBase includedProject, FileImpl includedFile) {
        assert Thread.holdsLock(entryToLockOn.getLock()) : "does not hold lock for " + includedFile;
        final CharSequence fileKey = FileContainer.getFileKey(includedFile.getAbsolutePath(), false);
        final CsmUID<CsmProject> uid = UIDs.get((CsmProject)includedProject);
        final MyEntry entry = myStorage.get(uid);
        if (entry != null) {
            FileEntry e = entry.myFiles.get(fileKey);
            if (e == null) {
                e = FileContainer.createFileEntry(includedFile);
                FileEntry prev = entry.myFiles.putIfAbsent(fileKey, e);
                if (prev != null) {
                    // must be called under FileImpl's entry lock
                    throw new ConcurrentModificationException("someone put the same file entry for " + includedFile); // NOI18N
                }
            }
            return e;
        }
        return null;
    }

    /**
     * for tracing purpose only.
     */
    public Map<CsmUID<CsmProject> , Collection<PreprocessorStatePair>> getPairsToDump(FileImpl fileToSearch) {
        Map<CsmUID<CsmProject>, Collection<PreprocessorStatePair>> out = new HashMap<CsmUID<CsmProject>, Collection<PreprocessorStatePair>>();
        final CharSequence fileKey = FileContainer.getFileKey(fileToSearch.getAbsolutePath(), false);
        for(Map.Entry<CsmUID<CsmProject>, MyEntry> entry : myStorage.entrySet()) {
            FileEntry fe = entry.getValue().myFiles.get(fileKey);
            if (fe != null) {
                Collection<PreprocessorStatePair> pairs = fe.getStatePairs();
                if (!pairs.isEmpty()) {
                    out.put(entry.getKey(), pairs);
                }
            }
        }
        return out;
    }

    public void invalidate(Object lock, ProjectBase includedFileOwner, CharSequence fileKey) {
        assert Thread.holdsLock(lock) : "does not hold lock for " + fileKey;
        fileKey = FileContainer.getFileKey(fileKey, false);
        assert CharSequences.isCompact(fileKey);
        final CsmUID<CsmProject> uid = UIDs.get((CsmProject)includedFileOwner);
        final MyEntry entry = myStorage.get(uid);
        if (entry != null) {
            FileEntry e = entry.myFiles.get(fileKey);
            if (e != null) {
                e.invalidateStates();
                put();
            }
        }
    }

    public boolean remove(Object lock, ProjectBase includedFileOwner, CharSequence fileKey) {
        assert Thread.holdsLock(lock) : "does not hold lock for " + fileKey;
        fileKey = FileContainer.getFileKey(fileKey, false);
        assert CharSequences.isCompact(fileKey);
        final CsmUID<CsmProject> uid = UIDs.get((CsmProject)includedFileOwner);
        final MyEntry entry = myStorage.get(uid);
        if (entry != null) {
            final FileEntry res = entry.myFiles.remove(fileKey);
            if (res != null) {
                put();
            }
            return true;
        }
        return false;
    }

    public FileContainer.FileEntry getIncludedFileEntry(Object lock, ProjectBase includedFileOwner, CharSequence fileKey) {
        assert Thread.holdsLock(lock) : "does not hold lock for " + fileKey;
        fileKey = FileContainer.getFileKey(fileKey, false);
        assert CharSequences.isCompact(fileKey);
        final CsmUID<CsmProject> uid = UIDs.get((CsmProject)includedFileOwner);
        final MyEntry entry = myStorage.get(uid);
        if (entry != null) {
            return entry.myFiles.get(fileKey);
        }
        return null;
    }

    /*tests-only*/public void debugClearState() {
        for(Map.Entry<CsmUID<CsmProject>,MyEntry> entry : myStorage.entrySet()) {
            for(Map.Entry<CharSequence, FileContainer.FileEntry> e : entry.getValue().myFiles.entrySet()) {
                e.getValue().debugClearState();
            }
        }
        put();
    }

    private int getIncludedUnitId(CsmUID<CsmProject> uid) {
        if (uid instanceof KeyBasedUID<?>) {
            Key k = ((KeyBasedUID<?>)uid).getKey();
            return KeyUtilities.getProjectIndex(k);
        }
        throw new IllegalArgumentException();
    }

    private static class MyEntry {
        private final ConcurrentMap<CharSequence, FileContainer.FileEntry> myFiles = new ConcurrentHashMap<CharSequence, FileContainer.FileEntry>();
        private final FileSystem fileSystem;
        private MyEntry(FileSystem fileSystem) {
            this.fileSystem = fileSystem;
        }
    }
}
