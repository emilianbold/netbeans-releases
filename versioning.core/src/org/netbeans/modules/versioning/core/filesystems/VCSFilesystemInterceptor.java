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
package org.netbeans.modules.versioning.core.filesystems;

import java.awt.Image;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.modules.versioning.core.Utils;
import org.netbeans.modules.versioning.core.VersioningAnnotationProvider;
import org.netbeans.modules.versioning.core.VersioningManager;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSInterceptor;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStatusListener;

/**
 * Work in progress - summarizes the current communication between VCS and masterfs 
 * 
 * @author Tomas Stupka
 */
public final class VCSFilesystemInterceptor {
    private static final Logger LOG = VersioningManager.LOG;
    private static final VersioningManager master = VersioningManager.getInstance();
    /**
     * Delete interceptor: holds files and folders that we do not want to delete
     * but must pretend that they were deleted.
     */
    private static final Set<VCSFileProxy> deletedFiles = new HashSet<VCSFileProxy>(5);
    
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
    
    private VCSFilesystemInterceptor() {
    }
    
    // ==================================================================================================
    // ANNOTATIONS
    // ==================================================================================================

    /** Listeners are held weakly, and can GC if nobody else holds them */
    public static void registerFileStatusListener(FileStatusListener listener) {
        VersioningManager.statusListener(listener, true);
    }
    
    public static Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files) {
        return VersioningAnnotationProvider.getDefault().annotateIcon(icon, iconType, files);
    }
    
    public static String annotateNameHtml(String name, Set<? extends FileObject> files) {
        return VersioningAnnotationProvider.getDefault().annotateNameHtml(name, files);
    }
    
    public static Action[] actions(Set<? extends FileObject> files) {
        return VersioningAnnotationProvider.getDefault().actions(files);
    }

    
    // ==================================================================================================
    // QUERIES
    // ==================================================================================================

    /**
     * Determines if the given file is writable
     * @param file
     * @return 
     */
    public static Boolean canWrite(VCSFileProxy file) {
        LOG.log(Level.FINE, "canWrite {0}", file);
        // can be optimized by taking out local history from the search
        return getInterceptor(file, false, "isMutable").isMutable(file); // NOI18N
    }

    /**
     * Returns the given files files attribute
     * @param file
     * @param attrName
     * @return 
     */
    public static Object getAttribute(VCSFileProxy file, String attrName) {
        LOG.log(Level.FINE, "getAttribute {0}, {1}", new Object[] {file, attrName});
        if (ATTRIBUTE_REMOTE_LOCATION.equals(attrName)
                || ATTRIBUTE_REFRESH.equals(attrName)
                || ATTRIBUTE_SEARCH_HISTORY.equals(attrName)) {
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

    public static void beforeChange(VCSFileProxy file) {
        LOG.log(Level.FINE, "beforeChange {0}", file);
        getInterceptor(file, file.isDirectory(), "beforeChange").beforeChange(); // NOI18N
    }
    
    public static void fileChanged(VCSFileProxy file) {
        LOG.log(Level.FINE, "fileChanged {0}", file);
        removeFromDeletedFiles(file);
        getInterceptor(file, "afterChange").afterChange();
    }

    // ==================================================================================================
    // DELETE
    // ==================================================================================================

    private static void removeFromDeletedFiles(VCSFileProxy file) {
        synchronized(deletedFiles) {
            deletedFiles.remove(file);
        }
    }

    public static DeleteHandler getDeleteHandler(VCSFileProxy file) {
        LOG.log(Level.FINE, "getDeleteHandler {0}", file);
        removeFromDeletedFiles(file);
        DelegatingInterceptor dic = getInterceptor(file, (Boolean) null, "beforeDelete", "doDelete"); // NOI18N
        return dic.beforeDelete() ? dic : null;
    }

    public static void deleteSuccess(VCSFileProxy file) {
        LOG.log(Level.FINE, "fileDeleted {0}", file);
        removeFromDeletedFiles(file);
        getInterceptor(file, "afterDelete").afterDelete(); // NOI18N
    }

    public static void deletedExternally(VCSFileProxy file) {
        LOG.log(Level.FINE, "fileDeleted {0}", file);
        removeFromDeletedFiles(file);
        getInterceptor(file, "afterDelete").afterDelete(); // NOI18N
    }
   
    // ==================================================================================================
    // CREATE
    // ==================================================================================================

    public static void beforeCreate(VCSFileProxy parent, String name, boolean isFolder) {
        LOG.log(Level.FINE, "beforeCreate {0}, {1}, {2} ", new Object[] {parent, name, isFolder});
        if (parent == null) return;
        VCSFileProxy file = VCSFileProxy.createFileProxy(parent, name);
        DelegatingInterceptor dic = getInterceptor(file, isFolder, "beforeCreate"); // NOI18N
        if (dic.beforeCreate()) {
            filesBeingCreated.put(new FileEx(parent, name, isFolder), dic);
        }
    }

    public static void createFailure(VCSFileProxy parent, String name, boolean isFolder) {
        LOG.log(Level.FINE, "createFailure {0}, {1}, {2} ", new Object[] {parent, name, isFolder});
        filesBeingCreated.remove(new FileEx(parent, name, isFolder));
    }

    public static void createSuccess(VCSFileProxy file) {
        FileEx fileEx = new FileEx(file.getParentFile(), file.getName(), file.isDirectory());
        DelegatingInterceptor interceptor = filesBeingCreated.remove(fileEx);
        if (interceptor != null) {
            try {
                interceptor.doCreate();
            } catch (Exception e) {
                // ignore errors, the file is already created anyway
            }
        }
        removeFromDeletedFiles(file);
        if (interceptor == null) {
            interceptor = getInterceptor(file, "afterCreate"); // NOI18N
        }
        interceptor.afterCreate();
    }

    public static void createdExternally(VCSFileProxy file) {
        createSuccess(file);
    }

    // ==================================================================================================
    // MOVE
    // ==================================================================================================

    public static IOHandler getMoveHandler(VCSFileProxy from, VCSFileProxy to) {
        LOG.log(Level.FINE, "getMoveHandler {0}, {1}", new Object[]{from, to});
        return getMoveHandlerIntern(from, to);
    }

    public static IOHandler getRenameHandler(VCSFileProxy from, String newName) {
        LOG.log(Level.FINE, "getRenameHandler {0}, {1}", new Object[] {from, newName});
        return getMoveHandlerIntern(from, VCSFileProxy.createFileProxy(from.getParentFile(), newName));
    }

    private static IOHandler getMoveHandlerIntern(VCSFileProxy from, VCSFileProxy to) {
        DelegatingInterceptor dic = getInterceptor(from, to, "beforeMove", "doMove"); // NOI18N
        return dic.beforeMove() ? dic.getMoveHandler() : null;
    }

    public static void afterMove(VCSFileProxy from, VCSFileProxy to) {
        removeFromDeletedFiles(from);
        getInterceptor(from, to, "afterMove").afterMove();
    }

    // ==================================================================================================
    // COPY
    // ==================================================================================================

    public static IOHandler getCopyHandler(VCSFileProxy from, VCSFileProxy to) {
        LOG.log(Level.FINE, "getCopyHandler {0}, {1}", new Object[]{from, to});
        DelegatingInterceptor dic = getInterceptor(from, to, "beforeCopy", "doCopy"); // NOI18N
        return dic.beforeCopy() ? dic.getCopyHandler() : null;
    }

    public static void beforeCopy(VCSFileProxy from, VCSFileProxy to) {
    }
    
    public static void copySuccess(VCSFileProxy from, VCSFileProxy to) {
        getInterceptor(from, to, "afterCopy").afterCopy();
    }

    // ==================================================================================================
    // MISC
    // ==================================================================================================    
    
    /**
     * There is a contract that says that when a file is locked, it is expected to be changed. This is what openide/text
     * does when it creates a Document. A versioning system is expected to make the file r/w.
     *
     * @param fo a VCSFileProxy
     */
    public static void fileLocked(VCSFileProxy fo) {
        LOG.log(Level.FINE, "fileLocked {0}", fo);
        getInterceptor(fo, "beforeEdit").beforeEdit();           // NOI18N
    }

    public static long listFiles(VCSFileProxy dir, long lastTimeStamp, List<? super VCSFileProxy> children) {
        LOG.log(Level.FINE, "refreshRecursively {0}, {1}", new Object[]{dir, lastTimeStamp});
        if(LOG.isLoggable(Level.FINER)) {
            for (Object f : children) {
                LOG.log(Level.FINE, "  refreshRecursively child {1}", f);
            }
        }
        DelegatingInterceptor interceptor = getRefreshInterceptor(dir);
        return interceptor.refreshRecursively(dir, lastTimeStamp, children);
    }
    
    // ==================================================================================================
    // HANDLERS
    // ==================================================================================================
    
    public interface IOHandler {
        /**
         * @throws java.io.IOException if handled operation isn't successful
         */
        void handle() throws IOException;
    }
    
    public interface DeleteHandler {
        /**
         * Deletes the file or directory denoted by this abstract pathname.  If
         * this pathname denotes a directory, then the directory must be empty in
         * order to be deleted.
         *
         * @return  <code>true</code> if and only if the file or directory is
         *          successfully deleted; <code>false</code> otherwise
         */
        boolean delete(VCSFileProxy file); // XXX IOException ?
    }    

    
    // private methods
    

    private boolean needsLH(String... methodNames) {
        for (String methodName : methodNames) {
            if(master.needsLocalHistory(methodName)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Stores files that are being created inside the IDE and the owner interceptor wants to handle the creation. Entries
     * are added in beforeCreate() and removed in fileDataCreated() or createFailure().
     */
    private final static Map<FileEx, DelegatingInterceptor> filesBeingCreated = new HashMap<FileEx, DelegatingInterceptor>(10);
    private static class FileEx {
        final VCSFileProxy parent;
        final String name;
        final boolean isFolder;

        public FileEx(VCSFileProxy parent, String name, boolean folder) {
            this.parent = parent;
            this.name = name;
            isFolder = folder;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof FileEx)) return false;
            FileEx fileEx = (FileEx) o;
            return isFolder == fileEx.isFolder && name.equals(fileEx.name) && parent.equals(fileEx.parent);
        }

        @Override
        public int hashCode() {
            int result = parent.hashCode();
            result = 17 * result + name.hashCode();
            result = 17 * result + (isFolder ? 1 : 0);
            return result;
        }
    }

    /*
    private DelegatingInterceptor getInterceptor(FileEvent fe, String... forMethods) {
        if (master == null) return nullDelegatingInterceptor;
        FileObject fo = fe.getFile();
        if (fo == null) return nullDelegatingInterceptor;
        VCSFileProxy file = VCSFileProxy.createFileProxy(fo);
        if (file == null) return nullDelegatingInterceptor;
        
        VersioningSystem lh = null; // XXX: needsLH(forMethods) ? master.getLocalHistory(file, !fo.isFolder()) : null;
        VersioningSystem vs = master.getOwner(file, !fo.isFolder());

        VCSInterceptor vsInterceptor = vs != null ? vs.getInterceptor() : null;
        VCSInterceptor lhInterceptor = lh != null ? lh.getInterceptor() : null;

        if (vsInterceptor == null && lhInterceptor == null) return nullDelegatingInterceptor;

        if (fe instanceof FileRenameEvent) {
            FileRenameEvent fre = (FileRenameEvent) fe;
            VCSFileProxy parent = file.getParentFile();
            if (parent != null) {
                String name = fre.getName();
                String ext = fre.getExt();
                if (ext != null && ext.length() > 0) {  // NOI18N
                    name += "." + ext;  // NOI18N
                }
                VCSFileProxy from = VCSFileProxy.createFileProxy(parent, name);
                return new DelegatingInterceptor(vsInterceptor, lhInterceptor, from, file, false);
            }
            return nullDelegatingInterceptor;
        } else {
            return new DelegatingInterceptor(vsInterceptor, lhInterceptor, file, null, false);
        }
    }
*/
    
    private static DelegatingInterceptor getInterceptor(VCSFileProxy file, String... forMethods) {
        return getInterceptor(file, file.isDirectory(), forMethods);
    }
    private static DelegatingInterceptor getInterceptor(VCSFileProxy file, Boolean isDirectory, String... forMethods) {
        if (file == null || master == null) return nullDelegatingInterceptor;

        Boolean isFile = isDirectory != null ? !isDirectory : null;
        isDirectory = isDirectory != null ? isDirectory : false;
        
        VersioningSystem vs = master.getOwner(file, isFile);
        VCSInterceptor vsInterceptor = vs != null ? vs.getInterceptor() : nullInterceptor;

        VersioningSystem lhvs = null; // XXX: needsLH(forMethods) ? master.getLocalHistory(file, isFile) : null;
        VCSInterceptor localHistoryInterceptor = lhvs != null ? lhvs.getInterceptor() : nullInterceptor;

        return new DelegatingInterceptor(vsInterceptor, localHistoryInterceptor, file, null, isDirectory);
    }

    private static DelegatingInterceptor getInterceptor(VCSFileProxy from, VCSFileProxy to, String... forMethods) {
        if (from == null || to == null) return nullDelegatingInterceptor;

        VersioningSystem vs = master.getOwner(from);
        VCSInterceptor vsInterceptor = vs != null ? vs.getInterceptor() : nullInterceptor;

        VersioningSystem lhvs = null; // XXX: needsLH(forMethods) ? master.getLocalHistory(from) : null;
        VCSInterceptor localHistoryInterceptor = lhvs != null ? lhvs.getInterceptor() : nullInterceptor;

        return new DelegatingInterceptor(vsInterceptor, localHistoryInterceptor, from, to, false);
    }

    private static DelegatingInterceptor getRefreshInterceptor (VCSFileProxy dir) {
        if (dir == null) return nullDelegatingInterceptor;
        VersioningSystem vs = master.getOwner(dir);
        VCSInterceptor Interceptor = vs != null ? vs.getInterceptor() : nullInterceptor;
        return new DelegatingInterceptor(Interceptor, nullInterceptor, dir, null, true);
    }

    private static final DelegatingInterceptor nullDelegatingInterceptor = new DelegatingInterceptor() {
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
    };

    private final static VCSInterceptor nullInterceptor = new VCSInterceptor() {

        @Override
        public Boolean isMutable(VCSFileProxy file) {
            return null;
        }

        @Override
        public Object getAttribute(VCSFileProxy file, String attrName) {
            return null;
        }

        @Override
        public boolean beforeDelete(VCSFileProxy file) {
            return false;
        }

        @Override
        public void doDelete(VCSFileProxy file) throws IOException { }

        @Override
        public void afterDelete(VCSFileProxy file) {}

        @Override
        public boolean beforeMove(VCSFileProxy from, VCSFileProxy to) {
            return false;
        }

        @Override
        public void doMove(VCSFileProxy from, VCSFileProxy to) throws IOException {}

        @Override
        public void afterMove(VCSFileProxy from, VCSFileProxy to) {}

        @Override
        public boolean beforeCopy(VCSFileProxy from, VCSFileProxy to) {
            return false;
        }

        @Override
        public void doCopy(VCSFileProxy from, VCSFileProxy to) throws IOException {}

        @Override
        public void afterCopy(VCSFileProxy from, VCSFileProxy to) {}

        @Override
        public boolean beforeCreate(VCSFileProxy file, boolean isDirectory) {
            return false;
        }

        @Override
        public void doCreate(VCSFileProxy file, boolean isDirectory) throws IOException {}

        @Override
        public void afterCreate(VCSFileProxy file) {}

        @Override
        public void afterChange(VCSFileProxy file) {}

        @Override
        public void beforeChange(VCSFileProxy file) {}

        @Override
        public void beforeEdit(VCSFileProxy file) {}

        @Override
        public long refreshRecursively(VCSFileProxy dir, long lastTimeStamp, List<? super VCSFileProxy> children) {
            return -1;
        }
    };

    private static class DelegatingInterceptor implements DeleteHandler {
        final Collection<VCSInterceptor> interceptors;
        final VCSInterceptor interceptor;
        final VCSInterceptor lhInterceptor;
        final VCSFileProxy file;
        final VCSFileProxy to;
        private final boolean isDirectory;
        private IOHandler moveHandler;
        private IOHandler copyHandler;

        private DelegatingInterceptor() {
            this((VCSInterceptor) null, null, null, null, false);
        }

        public DelegatingInterceptor(VCSInterceptor interceptor, VCSInterceptor lhInterceptor, VCSFileProxy file, VCSFileProxy to, boolean isDirectory) {
            this.interceptor = interceptor != null ? interceptor : nullInterceptor;
            this.interceptors = Collections.singleton(this.interceptor);
            this.lhInterceptor = lhInterceptor != null ? lhInterceptor : nullInterceptor;
            this.file = file;
            this.to = to;
            this.isDirectory = isDirectory;
        }

        // TODO: special hotfix for #95243
        public DelegatingInterceptor(Collection<VCSInterceptor> interceptors, VCSInterceptor lhInterceptor, VCSFileProxy file, VCSFileProxy to, boolean isDirectory) {
            this.interceptors = interceptors != null && interceptors.size() > 0 ? interceptors : Collections.singleton(nullInterceptor);
            this.interceptor = this.interceptors.iterator().next();
            this.lhInterceptor = lhInterceptor != null ? lhInterceptor : nullInterceptor;
            this.file = file;
            this.to = to;
            this.isDirectory = isDirectory;
        }

        public Boolean isMutable(VCSFileProxy file) {
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

        public boolean beforeCopy() {
            lhInterceptor.beforeCopy(file, to);
            return interceptor.beforeCopy(file, to);
        }

        public void doCopy() throws IOException {
            lhInterceptor.doCopy(file, to);
            interceptor.doCopy(file, to);
        }

        public void afterCopy() {
            lhInterceptor.afterCopy(file, to);
            interceptor.afterCopy(file, to);
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

        private IOHandler getMoveHandler() {
            if (moveHandler == null) {
                moveHandler = new IOHandler() {

                    @Override
                    public void handle() throws IOException {
                        doMove();
                    }
                };
            }
            return moveHandler;
        }

        private IOHandler getCopyHandler() {
            if (copyHandler == null) {
                copyHandler = new IOHandler() {

                    @Override
                    public void handle() throws IOException {
                        doCopy();
                    }
                };
            }
            return copyHandler;
        }

        /**
         * This must act EXACTLY like java.io.File.delete(). This means:
         *
         * 1.1 if the file is a file and was deleted, return true 1.2 if the
         * file is a file and was NOT deleted because we want to keep it (is
         * part of versioning metadata), also return true this is done this way
         * to enable bottom-up recursive file deletion 1.3 if the file is a file
         * that should be deleted but the operation failed (the file is locked,
         * for example), return false
         *
         * 2.1 if the file is an empty directory that was deleted, return true
         * 2.2 if the file is a NON-empty directory that was NOT deleted because
         * it contains files that were NOT deleted in step 1.2, return true 2.3
         * if the file is a NON-empty directory that was NOT deleted because it
         * contains some files that were not previously deleted, return false
         *
         * @param file file or folder to delete
         * @return true if the file was successfully deleted (event virtually
         * deleted), false otherwise
         */
        public boolean delete(VCSFileProxy file) {
            VCSFileProxy[] children = file.listFiles();
            if (children != null) {
                synchronized (deletedFiles) {
                    for (VCSFileProxy child : children) {
                        if (!deletedFiles.contains(child)) {
                            return false;
                        }
                    }
                }
            }
            try {
                lhInterceptor.doDelete(file);
                interceptor.doDelete(file);
                synchronized (deletedFiles) {
                    if (file.isDirectory()) {
                        // the directory was virtually deleted, we can forget about its children
                        for (Iterator<VCSFileProxy> i = deletedFiles.iterator(); i.hasNext();) {
                            VCSFileProxy fakedFile = i.next();
                            if (file.equals(fakedFile.getParentFile())) {
                                i.remove();
                            }
                        }
                    }
                    if (file.exists()) {
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
        public long refreshRecursively(VCSFileProxy dir, long lastTimeStamp, List<? super VCSFileProxy> children) {
            return interceptor.refreshRecursively(dir, lastTimeStamp, children);
        }
    }
    
}