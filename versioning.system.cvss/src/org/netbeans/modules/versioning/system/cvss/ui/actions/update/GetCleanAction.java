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

package org.netbeans.modules.versioning.system.cvss.ui.actions.update;

import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.netbeans.modules.versioning.system.cvss.*;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.lib.cvsclient.file.FileUtils;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.ErrorManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;

import java.io.*;
import java.util.logging.Logger;
import java.util.*;
import java.util.concurrent.Callable;
import org.netbeans.modules.versioning.util.IndexingBridge;

/**
 * Revert modifications action.
 * 
 * @author Maros Sandor
 */
public class GetCleanAction extends AbstractSystemAction {

    protected String getBaseName(Node [] activatedNodes) {
        return "CTL_MenuItem_GetClean";  // NOI18N
    }

    protected boolean enable(Node[] nodes) {
        return CvsVersioningSystem.getInstance().getStatusCache().listFiles(getContext(nodes), FileInformation.STATUS_LOCAL_CHANGE).length > 0;
    }
    
    protected boolean asynchronous() {
        return false;
    }

    public void performCvsAction(final Node[] nodes) {
        if (!confirmed(null, null)) return;
        CvsVersioningSystem.getInstance().getParallelRequestProcessor().post(new Runnable() {
            public void run() {
                revertModifications(nodes);
            }
        });
    }

    private static boolean confirmed(File file, String revision) {
        String message;
        if (file == null || revision == null) {
            message = NbBundle.getMessage(GetCleanAction.class, "CTL_RevertModifications_Prompt");
        } else {
            message = NbBundle.getMessage(GetCleanAction.class, "CTL_RevertModifications_Prompt2", file.getName(), revision);
        }
        NotifyDescriptor descriptor = new NotifyDescriptor(
                message,
                NbBundle.getMessage(GetCleanAction.class, "CTL_RevertModifications_Title"),
                NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.WARNING_MESSAGE,
                null,
                null
        );
        Object option = DialogDisplayer.getDefault().notify(descriptor);
        return option == NotifyDescriptor.YES_OPTION;
    }
    
    private void revertModifications(Node[] nodes) {
        final ExecutorGroup group = new ExecutorGroup(NbBundle.getMessage(GetCleanAction.class, "CTL_RevertModifications_Progress"));
        try {
            group.progress(NbBundle.getMessage(GetCleanAction.class, "CTL_RevertModifications_ProgressPrepare"));
            FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
            final File [] files = cache.listFiles(getContext(nodes), FileInformation.STATUS_LOCAL_CHANGE & FileInformation.STATUS_IN_REPOSITORY);
            
            Callable<Object> c = new Callable<Object>() {
                public Object call() {
                    for (int i = 0; i < files.length; i++) {
                        File file = files[i];
                        rollback(file, VersionsCache.REVISION_BASE, group);
                    }
                    return null;
                }
            };
            try {
                IndexingBridge.getInstance().runWithoutIndexing(c, false, files);
            } catch (Exception ex) {
                org.netbeans.modules.versioning.util.Utils.logError(GetCleanAction.class, ex);
            }
            for (int i = 0; i < files.length; i++) {
                refresh(files[i]);
            }
        } finally {
            group.executed();
        }

    }

    /**
     * Overwrites given file with its specified revision. Revision number and sticky information in Entries is NOT modified, 
     * only the content is overwritten.
     * 
     * @param file the file to overwrite
     * @param revision revision to get
     */ 
    public static void rollback(File file, String revision) {
        if (!confirmed(file, revision)) return;
        rollback(file, revision, null);
        refresh(file);
    }
    
    private static void refresh(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null) {
            fo.refresh();
        }
    }

    /**
     * If the revision is BASE and there is no Entry for the file, then the file is backed up and deleted.
     */
    private static void rollback(File file, String revision, ExecutorGroup group) {
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        AdminHandler ah = CvsVersioningSystem.getInstance().getAdminHandler();
        Entry entry = null;
        try {
            entry = ah.getEntry(file);
        } catch (IOException e) {
            // non-fatal, we have no entry for this file
        }
        if ((entry == null || entry.isNewUserFile()) && revision.equals(VersionsCache.REVISION_BASE)) {
            backup(file, entry);
            file.delete();
            return;
        }
        try {
            File cleanFile = VersionsCache.getInstance().getRemoteFile(file, revision, group);
            if (cleanFile != null) {
                // 'atomic' action  >>>
                backup(file, entry);
                try {
//                    CvsVersioningSystem.ignoreFilesystemEvents(true);
                    FileObject target;
                    if (file.exists() == false) {
                        File dir = file.getParentFile();
                        FileObject folder = Utils.mkfolders(dir);
                        target = folder.createData(file.getName());
                    } else {
                        target = FileUtil.toFileObject(FileUtil.normalizeFile(file));
                    }
                    if (target == null) {
                        org.netbeans.modules.versioning.util.Utils.copyStreamsCloseAll(new FileOutputStream(file), new FileInputStream(cleanFile));
                        FileUtil.refreshFor(file);
                    } else {
                        InputStream in = null;
                        OutputStream out = null;
                        FileLock lock = null;
                        try {
                            in = new FileInputStream(cleanFile);
                            lock = target.lock();
                            out = target.getOutputStream(lock);
                            copyStream(in, out);
                        } finally {
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException alreadyClosed) {
                                }
                            }
                            if (out != null) {
                                try {
                                    out.close();
                                } catch (IOException alreadyClosed) {
                                }
                            }
                            if (lock != null) {
                                lock.releaseLock();
                            }
                        }
                    }                   
                } finally {
//                    CvsVersioningSystem.ignoreFilesystemEvents(false);
                }
                if (entry != null && entry.isUserFileToBeRemoved()) {
                    entry.setRevision(entry.getRevision().substring(1));
                    ah.setEntry(file, entry);
                }
                if (revision == VersionsCache.REVISION_BASE) {
                    // set Entry so that cache marks this file as having no local modifications after revert
                    if (entry.getLastModified() != null) {
                        file.setLastModified(entry.getLastModified().getTime());
                    }
                    entry.setConflict(Entry.getLastModifiedDateFormatter().format(new Date(file.lastModified())));
                    try {
                        ah.setEntry(file, entry);
                    } catch (IOException e) {
                        org.netbeans.modules.versioning.util.Utils.logError(GetCleanAction.class, e);
                    }
                }
                cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                // 'atomic' action  <<<
            } else {
                if (group != null && group.isCancelled()) {
                    return;
                }
                // locally delete? NOt yet there seems to be bug in checkout -p
                Logger.getLogger(GetCleanAction.class.getName()).severe("Unable to revert changes in " + file.getName() + "; checkout failed");
            }
        } catch (Exception e) {
            if (e.getCause() instanceof InterruptedException) {
                // command aborted
            } else {
                ErrorManager.getDefault().notify(e);
            }
        }
    }

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte [] buffer = new byte[4096];
        for (;;) {
            int n = in.read(buffer, 0, 4096);
            if (n < 0) return;
            out.write(buffer, 0, n);
        }
    }

    private static void backup(File file, Entry entry) {
        if (!file.isFile()) return; // nothing to backup (avoid creating unnecessary directories in FileUtils.copyFile) 
        try {
            File backup;
            if (entry != null) {
                backup = new File(file.getParentFile(), ".#" + file.getName() + "." + entry.getRevision());
            } else {
                backup = new File(file.getParentFile(), ".#" + file.getName() + "." + "LOCAL");
            }
            FileUtils.copyFile(file, backup);
        } catch (IOException e) {
            // ignore
        }
    }
    @Override
    protected String iconResource() {
        return "org/netbeans/modules/versioning/system/cvss/resources/icons/get_clean.png"; // NOI18N
    }
}
