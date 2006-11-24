/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
        return CvsVersioningSystem.getInstance().getFileTableModel(Utils.getCurrentContext(nodes), FileInformation.STATUS_LOCAL_CHANGE).getNodes().length > 0;
    }
    
    protected boolean asynchronous() {
        return false;
    }

    public void performCvsAction(final Node[] nodes) {
        if (!confirmed(null, null)) return;
        RequestProcessor.getDefault().post(new Runnable() {
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
        ExecutorGroup group = new ExecutorGroup(NbBundle.getMessage(GetCleanAction.class, "CTL_RevertModifications_Progress"));
        try {
            group.progress(NbBundle.getMessage(GetCleanAction.class, "CTL_RevertModifications_ProgressPrepare"));
            FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
            File [] files = cache.listFiles(getContext(nodes), FileInformation.STATUS_LOCAL_CHANGE & FileInformation.STATUS_IN_REPOSITORY);
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                rollback(file, VersionsCache.REVISION_BASE, group);
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
                        target = FileUtil.toFileObject(file);
                    }
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

                } finally {
//                    CvsVersioningSystem.ignoreFilesystemEvents(false);
                }
                if (entry != null && entry.isUserFileToBeRemoved()) {
                    entry.setRevision(entry.getRevision().substring(1));
                    ah.setEntry(file, entry);
                }
                cache.refresh(file, revision == VersionsCache.REVISION_BASE ? FileStatusCache.REPOSITORY_STATUS_UPTODATE : FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                // 'atomic' action  <<<
            } else {
                if (group.isCancelled()) {
                    return;
                }
                // locally delete? NOt yet there seems to be bug in checkout -p
                ErrorManager.getDefault().log(ErrorManager.WARNING, "Unable to checkout " + file.getName()); // NOI18N
                cleanFile.getName(); // raise compatability NPE
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
}
