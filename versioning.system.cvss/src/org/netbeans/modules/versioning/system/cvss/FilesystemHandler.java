/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.openide.ErrorManager;

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.SearchHistoryAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.UpdateExecutor;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.util.FileUtils;
import org.netbeans.modules.versioning.util.SearchHistorySupport;

/**
 * Handles events fired from the filesystem such as file/folder create/delete/move.
 * 
 * 
 */
class FilesystemHandler extends VCSInterceptor {
        
    private final FileStatusCache   cache;
    private static Thread ignoredThread;

    public FilesystemHandler(CvsVersioningSystem cvs) {
        cache = cvs.getStatusCache();
    }

    /**
     * We save all CVS metadata to be able to commit files that were in that directory.
     * 
     * @param file File, we are only interested in files inside CVS directory
     */ 
    public boolean beforeDelete(File file) {
        return !ignoringEvents();
    }

    public void doDelete(File file) throws IOException {
        if (org.netbeans.modules.versioning.system.cvss.util.Utils.isPartOfCVSMetadata(file)) {
            // medatada are never deleted
        } else if (file.isDirectory() && hasMetadata(file)) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File ch : children) {
                    doDelete(ch);
                }
            }
            CvsVisibilityQuery.hideFolder(file);
            cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN, true);
        } else {
            FileUtils.deleteRecursively(file);
            if (file.exists()) {
                throw new IOException("Failed to delete file: " + file.getAbsolutePath());
            }
            fileDeletedImpl(file, false);
        }
    }

    public void afterDelete(File file) {
        refreshDeleted(file, false);
    }
    
    private void refreshDeleted(File file, boolean refreshNow) {
        if (refreshNow) {
            cache.refreshNow(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN, false);
        } else {
            cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN, false);
        }
        if (file.getName().equals(CvsVersioningSystem.FILENAME_CVSIGNORE)) cache.directoryContentChanged(file.getParentFile());
    }

    /**
     * We handle directory renames that are managed by CVS.
     */
    public boolean beforeMove(File from, File to) {
        File destDir = to.getParentFile();
        return from != null && destDir != null && org.netbeans.modules.versioning.system.cvss.util.Utils.containsMetadata(from);
    }
    
    /**
     * We only handle directories, file renames are examined ex post. Both directories share the same parent.
     * 
     * @param from source directory to be renamed
     * @param to new directory to be created 
     */
    public void doMove(File from, File to) throws IOException {
        List<File> affectedFiles = new ArrayList<File>();
        moveRecursively(affectedFiles, from, to);
        cvsRemoveRecursively(from);
        refresh(affectedFiles);
    }

    private void moveRecursively(List<File> affectedFiles, File from, File to) throws IOException {
        File [] files = from.listFiles();
        if (files != null) {
            to.mkdirs(); // make sure destination fodler is created even if the source folder is empty
            for (File file : files) {
                String fileName = file.getName();
                if (file.isDirectory()) {
                    if (fileName.equals(CvsVersioningSystem.FILENAME_CVS)) {
                        CvsVisibilityQuery.hideFolder(file.getParentFile());
                        continue;
                    }
                    File toFile = new File(to, fileName);
                    moveRecursively(affectedFiles, file, toFile);
                    affectedFiles.add(file);
                    affectedFiles.add(toFile);
                } else {
                    File toFile = new File(to, fileName);
                    file.renameTo(toFile);
                    affectedFiles.add(file);
                    affectedFiles.add(toFile);
                }
            }
        }
    }

    private void cvsRemoveRecursively(File dir) {
        StandardAdminHandler sah = new StandardAdminHandler();
        Entry [] entries = null;
        try {
            entries = sah.getEntriesAsArray(dir);
        } catch (IOException e) {
            // the Entry is not available, continue with no Entry
        }
        
        if (entries != null) {
            for (Entry entry : entries) {
                if (entry != null && !entry.isDirectory() && !entry.isUserFileToBeRemoved()) {
                    File file = new File(dir, entry.getName());
                    cvsRemoveLocally(sah, file, entry);
                }
            }
        }
        
        File [] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) cvsRemoveRecursively(file);
        }
    }

    public void afterMove(final File from, final File to) {
        if (ignoringEvents()) return;
        Utils.post(new Runnable() {
            public void run() {
                fileDeletedImpl(from, true);
                fileCreatedImpl(to);
            }
        });
    }
    
    public boolean beforeCreate(File file, boolean isDirectory) {
        if (ignoringEvents()) return false;
        return isDirectory && file.getName().equals(CvsVersioningSystem.FILENAME_CVS);
    }

    public void doCreate(File file, boolean isDirectory) throws IOException {
        file.mkdir();
        File f = new File(file, CvsLiteAdminHandler.INVALID_METADATA_MARKER);
        try {
            f.createNewFile();
        } catch (IOException e) {
            ErrorManager.getDefault().log(ErrorManager.ERROR, "Unable to create marker: " + f.getAbsolutePath()); // NOI18N
        }
    }

    public void afterCreate(final File file) {
        if (ignoringEvents()) return;
        Utils.post(new Runnable() {
            public void run() {
                fileCreatedImpl(file);
            }
        });
    }

    public void afterChange(final File file) {
        if (ignoringEvents()) return;
        Utils.post(new Runnable() {
            public void run() {
                cache.refreshCached(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                if (file.getName().equals(CvsVersioningSystem.FILENAME_CVSIGNORE)) cache.directoryContentChanged(file.getParentFile());
            }
        });
    }

    @Override
    public Object getAttribute(final File file, String attrName) {
        if("ProvidedExtensions.Refresh".equals(attrName)) {
            return new Runnable() {
                public void run() {
                    UpdateCommand cmd = new UpdateCommand();
                    GlobalOptions options = CvsVersioningSystem.createGlobalOptions();

                    cmd.setFiles(new File[] {file});
                    cmd.setBuildDirectories(true);
                    cmd.setPruneDirectories(true);
                    options.setDoNoChanges(true);
                    // TODO: javacvs library fails to obey the -P flag when -q is specified
            //        options.setModeratelyQuiet(true);
                    final ExecutorGroup refreshCommandGroup = new ExecutorGroup(null);
                    refreshCommandGroup.addExecutors(UpdateExecutor.splitCommand(cmd, CvsVersioningSystem.getInstance(), options, null));
                    CvsVersioningSystem.getInstance().getParallelRequestProcessor().post(new Runnable() {
                        public void run() {
                            refreshCommandGroup.execute();
                        }
                    });
        
                }
            };
        } else if (SearchHistorySupport.PROVIDED_EXTENSIONS_SEARCH_HISTORY.equals(attrName)){
            return new CvsSearchHistorySupport(file);
        } else {
            return super.getAttribute(file, attrName);
        }
    }

    @Override
    public long refreshRecursively(File dir, long lastTimeStamp, List<? super File> children) {
        long retval = -1;
        if (org.netbeans.modules.versioning.system.cvss.util.Utils.isPartOfCVSMetadata(dir)) {
            retval = 0;
        }
        return retval;
    }

    @Override
    public boolean isMutable(File file) {
        return org.netbeans.modules.versioning.system.cvss.util.Utils.isPartOfCVSMetadata(file) || super.isMutable(file);
    }

    // private methods ---------------------------

    private void fileCreatedImpl(File file) {
        if (file == null) return;
        int status = cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus();

        if ((status & FileInformation.STATUS_MANAGED) == 0) return;

        if (status == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) {
            StandardAdminHandler sah = new StandardAdminHandler();
            Entry entry = null;
            try {
                entry = sah.getEntry(file);
            } catch (IOException e) {
                // the Entry is not available, continue with no Entry
            }
            if (entry != null && !entry.isDirectory() && entry.isUserFileToBeRemoved()) {
                cvsUndoRemoveLocally(sah, file, entry);    
            }
            cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        }
        if (file.getName().equals(CvsVersioningSystem.FILENAME_CVSIGNORE)) cache.directoryContentChanged(file.getParentFile());
        if (file.isDirectory()) cache.directoryContentChanged(file);
    }

    /**
     * If a regular file is deleted then update its Entries as if it has been removed.
     * 
     * @param file deleted file
     */ 
    private void fileDeletedImpl(File file, boolean refreshNow) {
        if (file == null) return;
        
        StandardAdminHandler sah = new StandardAdminHandler();
        Entry entry = null;
        try {
            entry = sah.getEntry(file);
        } catch (IOException e) {
            // the Entry is not available, continue with no Entry
        }
        if (entry != null && !entry.isDirectory() && !entry.isUserFileToBeRemoved()) {
            cvsRemoveLocally(sah, file, entry);    
        }

        refreshDeleted(file, refreshNow);
    }
    
    /**
     * Emulates the 'cvs remove' command by modifying Entries. We do this to avoid contacting the
     * server.
     * 
     * @param ah
     * @param file
     * @param entry
     */ 
    private void cvsRemoveLocally(AdminHandler ah, File file, Entry entry) {
        try {
            if (entry.isNewUserFile()) {
                ah.removeEntry(file);
            } else {
                entry.setRevision("-" + entry.getRevision()); // NOI18N
                entry.setConflict(Entry.DUMMY_TIMESTAMP);
                ah.setEntry(file, entry);
            }
        } catch (IOException e) {
            // failed to set/remove entry, there is no way to recover from this
        }
    }

    private void cvsUndoRemoveLocally(AdminHandler ah, File file, Entry entry) {
        entry.setRevision(entry.getRevision().substring(1));
        entry.setConflict(Entry.getLastModifiedDateFormatter().format(new Date(System.currentTimeMillis() - 1000)));
        try {
            ah.setEntry(file, entry);
        } catch (IOException e) {
            // failed to set entry, the file will be probably resurrected during update
        }
    }
    
    private void refresh(List<File> files) {
        for (File file : files) {
            cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN, true);          
        }
    }

    private boolean hasMetadata(File file) {
        return new File(file, "CVS/Repository").canRead();
    }
    
    /**
     * Ignores (internal) events from current thread. E.g.:
     * <pre>
     * try {
     *     FilesystemHandler.ignoreEvents(true);
     *     fo.createData(file.getName());
     * } finally {
     *     FilesystemHandler.ignoreEvents(false);
     * }
     * </pre>
     *
     * <p>It assumes that filesystem operations fire
     * synchronous events.
     * @see {http://javacvs.netbeans.org/nonav/issues/show_bug.cgi?id=68961}
     */
    static void ignoreEvents(boolean ignore) {
        if (ignore) {
            ignoredThread = Thread.currentThread();
        } else {
            ignoredThread = null;
        }
    }

    /**
     * @return true if filesystem events are ignored in current thread, false otherwise
     */ 
    private static boolean ignoringEvents() {
        return ignoredThread == Thread.currentThread();
    }
    
    public class CvsSearchHistorySupport extends SearchHistorySupport {

        public CvsSearchHistorySupport(File file) {
            super(file);
        }

        @Override
        protected boolean searchHistoryImpl(final int line) throws IOException {
            assert line < 0 : "Search History a for specific not supported yet!";  // NOI18N
            File file = getFile();
            Set<File> s = new HashSet<File>();
            s.add(file);
            Context context = new Context(Collections.EMPTY_SET, s, Collections.EMPTY_SET);
            SearchHistoryAction.openHistory(context, file.getName());
            return true;
        }

    }    
}
