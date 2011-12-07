/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class ReferencesIndex {

    private static final class ComparatorImpl implements Comparator<CsmUID<?>> {

        public ComparatorImpl() {
        }

        @Override
        public int compare(CsmUID<?> o1, CsmUID<?> o2) {
            int projectID1 = UIDUtilities.getProjectID(o1);
            int projectID2 = UIDUtilities.getProjectID(o2);
            if (projectID1 != projectID2) {
                return projectID1 - projectID2;
            }
            int fileID1 = UIDUtilities.getFileID(o1);
            int fileID2 = UIDUtilities.getFileID(o2);
            if (fileID1 != fileID2) {
                CharSequence fileName1 = KeyUtilities.getFileNameById(projectID1, fileID1);
                CharSequence fileName2 = KeyUtilities.getFileNameById(projectID2, fileID2);
                return CharSequenceUtils.ComparatorIgnoreCase.compare(fileName1, fileName2);
            }
            int startOffset1 = UIDUtilities.getStartOffset(o1);
            int startOffset2 = UIDUtilities.getStartOffset(o2);
            return startOffset1 - startOffset2;
        }
    }

    private static final class RefComparator implements Comparator<CsmReference> {

        public RefComparator() {
        }

        @Override
        public int compare(CsmReference o1, CsmReference o2) {
            CharSequence containingFile1 = o1.getContainingFile().getAbsolutePath();
            CharSequence containingFile2 = o2.getContainingFile().getAbsolutePath();
            int res = CharSequenceUtils.ComparatorIgnoreCase.compare(containingFile1, containingFile2);
            if (res != 0) {
                return res;
            }
            res = o1.getStartOffset() - o2.getStartOffset();
            if (res != 0) {
                return res;
            }
            return o1.getEndOffset() - o2.getEndOffset();
        }
    }

    private static final class RefImpl implements CsmReference {
        private final CsmUID<CsmFile> containingFile;
        private final int start;
        private final int end;
        private final CsmReferenceKind kind;
        public RefImpl(CsmUID<CsmFile> fileUID, int start, int end, CsmReferenceKind kind) {
            this.containingFile = fileUID;
            this.start = start;
            this.end = end;
            this.kind = kind;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final RefImpl other = (RefImpl) obj;
            if (this.containingFile != other.containingFile && (this.containingFile == null || !this.containingFile.equals(other.containingFile))) {
                return false;
            }
            if (this.start != other.start) {
                return false;
            }
            if (this.end != other.end) {
                return false;
            }
            if (this.kind != other.kind) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + (this.containingFile != null ? this.containingFile.hashCode() : 0);
            hash = 37 * hash + this.start;
            hash = 37 * hash + this.end;
            hash = 37 * hash + (this.kind != null ? this.kind.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "RefImpl{" + "start=" + start + ", end=" + end + ", kind=" + kind + '}'; // NOI18N
        }

        @Override
        public CsmReferenceKind getKind() {
            return kind;
        }

        @Override
        public CsmObject getReferencedObject() {
            return null;
        }

        @Override
        public CsmObject getOwner() {
            return null;
        }

        @Override
        public CsmObject getClosestTopLevelObject() {
            return null;
        }

        @Override
        public CsmFile getContainingFile() {
            return UIDCsmConverter.UIDtoFile(containingFile);
        }

        @Override
        public int getStartOffset() {
            return start;
        }

        @Override
        public int getEndOffset() {
            return end;
        }

        @Override
        public Position getStartPosition() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public Position getEndPosition() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public CharSequence getText() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }
    }
    
    private ReferencesIndex() {};
    
    private static final ReferencesIndex INSTANCE = new ReferencesIndex();

    public static void dumpInfo(PrintWriter printOut) {
        INSTANCE.trace(printOut);
    }

    public static void clearIndex() {
        INSTANCE.clear();
    }
    
    public static void put(CsmUID<?> refedObject, CsmUID<CsmFile> fileUID, CsmReference ref) {
        INSTANCE.addRef(refedObject, fileUID, ref);
    }
    
    public static Collection<CsmReference> getAllReferences(CsmUID<?> referedObject) {
        return INSTANCE.getRefs(referedObject);
    }
    
    // value either ref or collection of refs
    private final Map<CsmUID<?>, Collection<CsmReference>> obj2refs = new HashMap<CsmUID<?>, Collection<CsmReference>>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    private void trace(PrintWriter printOut) {
        if (!ENABLED) {
            printOut.printf("INDEX IS DISABLED\n"); // NOI18N
            return;
        }
        if (obj2refs.isEmpty()) {
            printOut.printf("INDEX IS EMPTY\n"); // NOI18N
            return;
        }
        List<CsmUID<?>> keys = new ArrayList<CsmUID<?>>(obj2refs.keySet());
        Collections.sort(keys, new ComparatorImpl());
        int lastProjectID = -1;
        int lastFileID = -1;
        for (CsmUID<?> csmUID : keys) {
            int curProjectID = UIDUtilities.getProjectID(csmUID);
            if (lastProjectID != curProjectID) {
                lastProjectID = curProjectID;
                printOut.printf("Elements of project [%d] %s\n", curProjectID, KeyUtilities.getUnitName(curProjectID));// NOI18N
            }
            int curFileID = UIDUtilities.getFileID(csmUID);
            if (lastFileID != curFileID) {
                lastFileID = curFileID;
                printOut.printf("Elements of project %s of file [%d] %s\n", KeyUtilities.getUnitName(curProjectID), curFileID, KeyUtilities.getFileNameById(curProjectID, curFileID));// NOI18N
            }
            Object obj = obj2refs.get(csmUID);
            if (obj == null) {
                printOut.printf("NO REFERENCES for %s\n", csmUID); // NOI18N
            }
            Collection<CsmReference> refs = getRefs(csmUID);
            if (refs.isEmpty()) {
                printOut.printf("NO REFERENCES 2 for %s\n", csmUID); // NOI18N
            } else {
                printOut.printf("%s is referenced from:\n", csmUID); // NOI18N
                CsmFile prevFile = null;
                for (CsmReference csmReference : refs) {
                    CsmFile containingFile = csmReference.getContainingFile();
                    if (containingFile != null) {
                        if (containingFile != prevFile) {
                            prevFile = containingFile;
                            printOut.printf("\tFILE %s\n", containingFile.getAbsolutePath()); // NOI18N
                        }
                        printOut.printf("\t%s\n", csmReference); // NOI18N
                    } else {
                        printOut.printf("NOT FROM FILE %s\n", csmReference); // NOI18N
                    }
                }
            }
        }
    }

    private void clear() {
        lock.writeLock().lock();
        try {
            obj2refs.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
    private static final boolean ENABLED = false;
    private void addRef(CsmUID<?> referedObject, CsmUID<CsmFile> fileUID, CsmReference ref) {
        if (!ENABLED) {
            return;
        }
        RefImpl toPut = new RefImpl(fileUID, ref.getStartOffset(), ref.getEndOffset(), ref.getKind());
        lock.writeLock().lock();
        try {
            Collection<CsmReference> value = obj2refs.get(referedObject);
            if (value == null) {
                value = new TreeSet<CsmReference>(new RefComparator());
                obj2refs.put(referedObject, value);
            }
            value.add(toPut);
        } finally {
            lock.writeLock().unlock();
        }
    }    

    private Collection<CsmReference> getRefs(CsmUID<?> refedObject) {
        lock.readLock().lock();
        try {
            Collection value = obj2refs.get(refedObject);
            if (value == null) {
                return Collections.emptyList();
            } else {
                return new ArrayList<CsmReference>(value);
            }
        } finally {
            lock.readLock().unlock();
        }
    }
}
