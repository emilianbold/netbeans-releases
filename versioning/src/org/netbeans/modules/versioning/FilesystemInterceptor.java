/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.versioning;

import org.openide.filesystems.*;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Plugs into IDE filesystem and delegates file operations to registered versioning systems.
 *
 * @author Maros Sandor
 */
class FilesystemInterceptor extends ProvidedExtensions implements FileChangeListener {

    /**
     * A versioned files remote repository or origin.
     */
    private static final String ATTRIBUTE_REMOTE_LOCATION = "ProvidedExtensions.RemoteLocation";

    /**
     * A Runnable to refresh the file given in {@link #getAttribute()}.
     */
    private static final String ATTRIBUTE_REFRESH = "ProvidedExtensions.Refresh";

    /**
     * A o.n.m.versioning.util.SearchHistorySupport instance
     */
    private static final String ATTRIBUTE_SEARCH_HISTORY = "ProvidedExtensions.SearchHistorySupport";

    /**
     * Determines if a file is versioned or not
     */
    private static final String ATTRIBUTE_VCS_MANAGED = "ProvidedExtensions.VCSManaged";

    private VersioningManager master;

    // === LIFECYCLE =======================================================================================

    /**
     * Initializes the interceptor by registering it into master filesystem.
     * Registers listeners to all disk filesystems.
     * @param versioningManager
     */
    void init(VersioningManager versioningManager) {
        assert master == null;
        master = versioningManager;
        FileSystem fileSystem = Utils.getRootFilesystem();
        fileSystem.addFileChangeListener(this);
    }

    /**
     * Unregisters listeners from all disk filesystems.
     */
    void shutdown() {
        FileSystem fileSystem = Utils.getRootFilesystem();
        fileSystem.removeFileChangeListener(this);
    }    

    // ==================================================================================================
    // QUERIES
    // ==================================================================================================

    @Override
    public boolean canWrite(File file) {
        if (Utils.canWrite(file)) {
            return true;
        }
        if (!Utils.exists(file)) {
            return false;
        }
        // can be optimized by taking out local history from the search
        return getInterceptor(file, false, "isMutable").isMutable(file);        // NOI18N
    }

    @Override
    public Object getAttribute(File file, String attrName) {
        if(ATTRIBUTE_REMOTE_LOCATION.equals(attrName) ||           
           ATTRIBUTE_REFRESH.equals(attrName) ||
           ATTRIBUTE_SEARCH_HISTORY.equals(attrName))
        {
            return getInterceptor(file, file.isDirectory(), "getAttribute").getAttribute(attrName); // NOI18N
        } else if (ATTRIBUTE_VCS_MANAGED.equals(attrName)) {
            return master.getOwner(file) != null;
        } else {
            return null;
        }
    }

    // ==================================================================================================
    // CHANGE
    // ==================================================================================================

    public void fileChanged(FileEvent fe) {
        removeFromDeletedFiles(fe.getFile());
        getInterceptor(fe, "afterChange").afterChange();
    }

    public void beforeChange(FileObject fo) {
        getInterceptor(FileUtil.toFile(fo), fo.isFolder(), "beforeChange").beforeChange(); // NOI18N
    }

    private boolean needsLH(String... methodNames) {
        for (String methodName : methodNames) {
            if(master.needsLocalHistory(methodName)) {
                return true;
            }
        }
        return false;
    }

    // ==================================================================================================
    // DELETE
    // ==================================================================================================

    private void removeFromDeletedFiles(File file) {
        synchronized(deletedFiles) {
            deletedFiles.remove(file);
        }
    }

    private void removeFromDeletedFiles(FileObject fo) {
        synchronized(deletedFiles) {
            if (deletedFiles.size() > 0) {
                deletedFiles.remove(FileUtil.toFile(fo));
            }
        }
    }

    public DeleteHandler getDeleteHandler(File file) {
        removeFromDeletedFiles(file);
        DelegatingInterceptor dic = getInterceptor(file, false, "beforeDelete", "doDelete"); // NOI18N
        return dic.beforeDelete() ? dic : null;
    }

    public void fileDeleted(FileEvent fe) {
        removeFromDeletedFiles(fe.getFile());
        getInterceptor(fe, "afterDelete").afterDelete(); // NOI18N
    }

    // ==================================================================================================
    // CREATE
    // ==================================================================================================

    /**
     * Stores files that are being created inside the IDE and the owner interceptor wants to handle the creation. Entries
     * are added in beforeCreate() and removed in fileDataCreated() or createFailure().
     */
    private final Map<FileEx, DelegatingInterceptor> filesBeingCreated = new HashMap<FileEx, DelegatingInterceptor>(10);

    public void beforeCreate(FileObject parent, String name, boolean isFolder) {
        File file = FileUtil.toFile(parent);
        if (file == null) return;
        file = new File(file, name);
        DelegatingInterceptor dic = getInterceptor(file, isFolder, "beforeCreate"); // NOI18N
        if (dic.beforeCreate()) {
            filesBeingCreated.put(new FileEx(parent, name, isFolder), dic);
        }
    }

    public void createFailure(FileObject parent, String name, boolean isFolder) {
        filesBeingCreated.remove(new FileEx(parent, name, isFolder));
    }

    public void fileFolderCreated(FileEvent fe) {
        fileDataCreated(fe);
    }

    public void fileDataCreated(FileEvent fe) {
        FileObject fo = fe.getFile();
        FileEx fileEx = new FileEx(fo.getParent(), fo.getNameExt(), fo.isFolder());
        DelegatingInterceptor interceptor = filesBeingCreated.remove(fileEx);
        if (interceptor != null) {
            try {
                interceptor.doCreate();
            } catch (Exception e) {
                // ignore errors, the file is already created anyway
            }
        }
        removeFromDeletedFiles(fe.getFile());
        if(interceptor == null) {
            interceptor = getInterceptor(fe, "afterCreate");                    // NOI18N
        }   
        interceptor.afterCreate();
    }

    // ==================================================================================================
    // MOVE
    // ==================================================================================================

    public IOHandler getMoveHandler(File from, File to) {
        DelegatingInterceptor dic = getInterceptor(from, to, "beforeMove", "doMove"); // NOI18N
        return dic.beforeMove() ? dic : null;
    }

    public IOHandler getRenameHandler(File from, String newName) {
        File to = new File(from.getParentFile(), newName);
        return getMoveHandler(from, to);
    }

    public void fileRenamed(FileRenameEvent fe) {
        removeFromDeletedFiles(fe.getFile());
        getInterceptor(fe, "afterMove").afterMove();                            // NOI18N
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
        // not interested
    }

    /**
     * There is a contract that says that when a file is locked, it is expected to be changed. This is what openide/text
     * does when it creates a Document. A versioning system is expected to make the file r/w.
     *
     * @param fo a FileObject
     */
    public void fileLocked(FileObject fo) {
        getInterceptor(new FileEvent(fo), "beforeEdit").beforeEdit();           // NOI18N
    }

    private DelegatingInterceptor getInterceptor(FileEvent fe, String... forMethods) {
        if (master == null) return nullDelegatingInterceptor;
        FileObject fo = fe.getFile();
        if (fo == null) return nullDelegatingInterceptor;
        File file = FileUtil.toFile(fo);
        if (file == null) return nullDelegatingInterceptor;

        VersioningSystem lh = needsLH(forMethods) ? master.getLocalHistory(file) : null;
        VersioningSystem vs = master.getOwner(file);

        VCSInterceptor vsInterceptor = vs != null ? vs.getVCSInterceptor() : null;
        VCSInterceptor lhInterceptor = lh != null ? lh.getVCSInterceptor() : null;

        if (vsInterceptor == null && lhInterceptor == null) return nullDelegatingInterceptor;

        if (fe instanceof FileRenameEvent) {
            FileRenameEvent fre = (FileRenameEvent) fe;
            File parent = file.getParentFile();
            if (parent != null) {
                String name = fre.getName();
                String ext = fre.getExt();
                if (ext != null && ext.length() > 0) {  // NOI18N
                    name += "." + ext;  // NOI18N
                }
                File from = new File(parent, name);
                return new DelegatingInterceptor(vsInterceptor, lhInterceptor, from, file, false);
            }
            return nullDelegatingInterceptor;
        } else {
            return new DelegatingInterceptor(vsInterceptor, lhInterceptor, file, null, false);
        }
    }

    private DelegatingInterceptor getInterceptor(File file, boolean isDirectory, String... forMethods) {
        if (file == null || master == null) return nullDelegatingInterceptor;

        VersioningSystem vs = master.getOwner(file);
        VCSInterceptor vsInterceptor = vs != null ? vs.getVCSInterceptor() : nullVCSInterceptor;

        VersioningSystem lhvs = needsLH(forMethods) ? master.getLocalHistory(file) : null;
        VCSInterceptor localHistoryInterceptor = lhvs != null ? lhvs.getVCSInterceptor() : nullVCSInterceptor;

        return new DelegatingInterceptor(vsInterceptor, localHistoryInterceptor, file, null, isDirectory);
    }

    private DelegatingInterceptor getInterceptor(File from, File to, String... forMethods) {
        if (from == null || to == null) return nullDelegatingInterceptor;

        VersioningSystem vs = master.getOwner(from);
        VCSInterceptor vsInterceptor = vs != null ? vs.getVCSInterceptor() : nullVCSInterceptor;

        VersioningSystem lhvs = needsLH(forMethods) ? master.getLocalHistory(from) : null;
        VCSInterceptor localHistoryInterceptor = lhvs != null ? lhvs.getVCSInterceptor() : nullVCSInterceptor;

        return new DelegatingInterceptor(vsInterceptor, localHistoryInterceptor, from, to, false);
    }

    private final DelegatingInterceptor nullDelegatingInterceptor = new DelegatingInterceptor() {
        public boolean beforeDelete() { return false; }
        public void doDelete() throws IOException {  }
        public void afterDelete() { }
        public boolean beforeMove() { return false; }
        public void doMove() throws IOException {  }
        public boolean beforeCreate() { return false; }
        public void doCreate() throws IOException {  }
        public void afterCreate() {  }
        public void beforeChange() {  }
        public void beforeEdit() { }
        public void afterChange() {  }
        public void afterMove() {  }
        public void handle() throws IOException {  }
        public boolean delete(File file) {  throw new UnsupportedOperationException();  }
    };

    private final VCSInterceptor nullVCSInterceptor = new VCSInterceptor() {};

    /**
     * Delete interceptor: holds files and folders that we do not want to delete but must pretend that they were deleted.
     */
    private final Set<File> deletedFiles = new HashSet<File>(5);

    private class DelegatingInterceptor implements IOHandler, DeleteHandler {

        final Collection<VCSInterceptor> interceptors;
        final VCSInterceptor  interceptor;
        final VCSInterceptor  lhInterceptor;
        final File            file;
        final File            to;
        private final boolean isDirectory;

        private DelegatingInterceptor() {
            this((VCSInterceptor) null, null, null, null, false);
        }

        public DelegatingInterceptor(VCSInterceptor interceptor, VCSInterceptor lhInterceptor, File file, File to, boolean isDirectory) {
            this.interceptor = interceptor != null ? interceptor : nullVCSInterceptor;
            this.interceptors = Collections.singleton(this.interceptor);
            this.lhInterceptor = lhInterceptor != null ? lhInterceptor : nullVCSInterceptor;
            this.file = file;
            this.to = to;
            this.isDirectory = isDirectory;
        }

        // TODO: special hotfix for #95243
        public DelegatingInterceptor(Collection<VCSInterceptor> interceptors, VCSInterceptor lhInterceptor, File file, File to, boolean isDirectory) {
            this.interceptors = interceptors != null && interceptors.size() > 0 ? interceptors : Collections.singleton(nullVCSInterceptor);
            this.interceptor = this.interceptors.iterator().next();
            this.lhInterceptor = lhInterceptor != null ? lhInterceptor : nullVCSInterceptor;
            this.file = file;
            this.to = to;
            this.isDirectory = isDirectory;
        }

        public boolean isMutable(File file) {
            return interceptor.isMutable(file);
        }

        private Object getAttribute(String attrName) {
            return interceptor.getAttribute(file, attrName);
        }

        public boolean beforeDelete() {
            lhInterceptor.beforeDelete(file);
            return interceptor.beforeDelete(file);
        }

        public void doDelete() throws IOException {
            lhInterceptor.doDelete(file);
            interceptor.doDelete(file);
        }

        public void afterDelete() {
            lhInterceptor.afterDelete(file);
            interceptor.afterDelete(file);
        }

        public boolean beforeMove() {
            lhInterceptor.beforeMove(file, to);
            return interceptor.beforeMove(file, to);
        }

        public void doMove() throws IOException {
            lhInterceptor.doMove(file, to);
            interceptor.doMove(file, to);
        }

        public void afterMove() {
            lhInterceptor.afterMove(file, to);
            interceptor.afterMove(file, to);
        }

        public boolean beforeCreate() {
            lhInterceptor.beforeCreate(file, isDirectory);
            return interceptor.beforeCreate(file, isDirectory);
        }

        public void doCreate() throws IOException {
            lhInterceptor.doCreate(file, isDirectory);
            interceptor.doCreate(file, isDirectory);
        }

        public void afterCreate() {
            lhInterceptor.afterCreate(file);
            interceptor.afterCreate(file);
        }

        public void afterChange() {
            lhInterceptor.afterChange(file);
            interceptor.afterChange(file);
        }

        public void beforeChange() {
            lhInterceptor.beforeChange(file);
            interceptor.beforeChange(file);
        }

        public void beforeEdit() {
            lhInterceptor.beforeEdit(file);
            interceptor.beforeEdit(file);
        }

        /**
         * We are doing MOVE here, inspite of the generic name of the method.
         *
         * @throws IOException
         */
        public void handle() throws IOException {
            lhInterceptor.doMove(file, to);
            interceptor.doMove(file, to);
            lhInterceptor.afterMove(file, to);
            interceptor.afterMove(file, to);
        }

        /**
         * This must act EXACTLY like java.io.File.delete(). This means:

         * 1.1  if the file is a file and was deleted, return true
         * 1.2  if the file is a file and was NOT deleted because we want to keep it (is part of versioning metadata), also return true
         *      this is done this way to enable bottom-up recursive file deletion
         * 1.3  if the file is a file that should be deleted but the operation failed (the file is locked, for example), return false
         *
         * 2.1  if the file is an empty directory that was deleted, return true
         * 2.2  if the file is a NON-empty directory that was NOT deleted because it contains files that were NOT deleted in step 1.2, return true
         * 2.3  if the file is a NON-empty directory that was NOT deleted because it contains some files that were not previously deleted, return false
         *
         * @param file file or folder to delete
         * @return true if the file was successfully deleted (event virtually deleted), false otherwise
         */
        public boolean delete(File file) {
            File [] children = file.listFiles();
            if (children != null) {
                synchronized(deletedFiles) {
                    for (File child : children) {
                        if (!deletedFiles.contains(child)) return false;
                    }
                }
            }
            try {
                lhInterceptor.doDelete(file);
                interceptor.doDelete(file);
                synchronized(deletedFiles) {
                    if (file.isDirectory()) {
                        // the directory was virtually deleted, we can forget about its children
                        for (Iterator<File> i = deletedFiles.iterator(); i.hasNext(); ) {
                            File fakedFile = i.next();
                            if (file.equals(fakedFile.getParentFile())) {
                                i.remove();
                            }
                        }
                    }
                    if (Utils.exists(file)) {
                        deletedFiles.add(file);
                    } else {
                        deletedFiles.remove(file);
                    }
                }
                return true;
            } catch (IOException e) {
                // the interceptor failed to delete the file
                return false;
            }
        }
//        VCSInterceptor getInterceptor() {
//            return interceptor;
//        }
    }

    private class FileEx {
        final FileObject  parent;
        final String      name;
        final boolean     isFolder;

        public FileEx(FileObject parent, String name, boolean folder) {
            this.parent = parent;
            this.name = name;
            isFolder = folder;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof FileEx)) return false;
            FileEx fileEx = (FileEx) o;
            return isFolder == fileEx.isFolder && name.equals(fileEx.name) && parent.equals(fileEx.parent);
        }

        public int hashCode() {
            int result = parent.hashCode();
            result = 17 * result + name.hashCode();
            result = 17 * result + (isFolder ? 1 : 0);
            return result;
        }
    }
}
