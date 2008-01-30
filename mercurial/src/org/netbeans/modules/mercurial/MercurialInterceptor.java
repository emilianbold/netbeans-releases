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
package org.netbeans.modules.mercurial;

import javax.swing.SwingUtilities;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.mercurial.HgException;
import java.io.File;
import java.io.IOException;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.util.RequestProcessor;
import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Collection;
import java.util.Calendar;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.api.queries.SharabilityQuery;

/**
 * Listens on file system changes and reacts appropriately, mainly refreshing affected files' status.
 * 
 * @author Maros Sandor
 */
public class MercurialInterceptor extends VCSInterceptor {

    private final FileStatusCache   cache;

    private List<File> dirsToDelete; 

    public MercurialInterceptor() {
        cache = Mercurial.getInstance().getFileStatusCache();
        dirsToDelete = new ArrayList<File>();
    }

    public boolean beforeDelete(File file) {
        if (file == null) return true;
        if (HgUtils.isPartOfMercurialMetadata(file)) return false;
        
        // We track the deletion of top level directories
        if (file.isDirectory()) {
            for (Iterator i = dirsToDelete.iterator(); i.hasNext();) {
                File dir = (File) i.next();
                if (file.equals(dir.getParentFile())) {
                    i.remove();
                }
            }
            if (SharabilityQuery.getSharability(file) != SharabilityQuery.NOT_SHARABLE) {
                dirsToDelete.add(file);
            }
        }
        return true;
    }

    public void doDelete(File file) throws IOException {
        return;
    }

    public void afterDelete(final File file) {
        Utils.post(new Runnable() {
            public void run() {
                fileDeletedImpl(file);
            }
        });
    }
    
    private void fileDeletedImpl(final File file) {
        if (file == null) return;
        Mercurial hg = Mercurial.getInstance();
        final File root = hg.getTopmostManagedParent(file);
        RequestProcessor rp = null;
        if (root != null) {
            rp = hg.getRequestProcessor(root.getAbsolutePath());
        }
        if (file.exists()) {
            if (file.isDirectory()) {
                file.delete();
                if (!dirsToDelete.remove(file)) return;
                if (root == null) return;
                HgProgressSupport support = new HgProgressSupport() {
                    public void perform() {
                        try {
                            HgCommand.doRemove(root, file);
                            // We need to cache the status of all deleted files
                            Map<File, FileInformation> interestingFiles = HgCommand.getInterestingStatus(root, file);
                            if (!interestingFiles.isEmpty()){
                                Collection<File> files = interestingFiles.keySet();

                                Map<File, Map<File,FileInformation>> interestingDirs =
                                        HgUtils.getInterestingDirs(interestingFiles, files);

                                Calendar start = Calendar.getInstance();
                                for (File tmpFile : files) {
                                    if(this.isCanceled()) {
                                        return;
                                    }
                                    FileInformation fi = interestingFiles.get(tmpFile);

                                    cache.refreshFileStatus(tmpFile, fi,
                                    interestingDirs.get(tmpFile.isDirectory()? tmpFile: tmpFile.getParentFile()), true);
                                }
                                Calendar end = Calendar.getInstance();
                            }
                        } catch (HgException ex) {
                            Mercurial.LOG.log(Level.FINE, "fileDeletedImpl(): File: {0} {1}", new Object[] {file.getAbsolutePath(), ex.toString()}); // NOI18N
                        }             
                    }
                };

                support.start(rp, root.getAbsolutePath(), 
                        org.openide.util.NbBundle.getMessage(MercurialInterceptor.class, "MSG_Remove_Progress")); // NOI18N
            } else {
                // If we are deleting a parent directory of this file
                // skip the call to hg remove as we will do it for the directory
                file.delete();
                if (root == null) return;
                for (File dir : dirsToDelete) {
                    File tmpFile = file.getParentFile();
                    while (tmpFile != null) {
                        if (tmpFile.equals(dir)) return;
                        tmpFile = tmpFile.getParentFile();
                    }
                }
                HgProgressSupport support = new HgProgressSupport() {
                    public void perform() {
                        try {
                            HgCommand.doRemove(root, file);
                            cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                        } catch (HgException ex) {
                            Mercurial.LOG.log(Level.FINE, "fileDeletedImpl(): File: {0} {1}", new Object[] {file.getAbsolutePath(), ex.toString()}); // NOI18N
                        }             
                    }
                };
                support.start(rp, root.getAbsolutePath(), 
                        org.openide.util.NbBundle.getMessage(MercurialInterceptor.class, "MSG_Remove_Progress")); // NOI18N
            }
        }
    }

    public boolean beforeMove(File from, File to) {
        if (from == null || to == null || to.exists()) return true;
        
        Mercurial hg = Mercurial.getInstance();
        if (hg.isManaged(from)) {
            return hg.isManaged(to);
        }
        return super.beforeMove(from, to);
    }

    public void doMove(final File from, final File to) throws IOException {
        if (from == null || to == null || to.exists()) return;
        
        if (SwingUtilities.isEventDispatchThread()) {

            Mercurial.LOG.log(Level.INFO, "Warning: launching external process in AWT", new Exception().fillInStackTrace()); // NOI18N
            final Throwable innerT[] = new Throwable[1];
            Runnable outOfAwt = new Runnable() {
                public void run() {
                    try {
                        hgMoveImplementation(from, to);
                    } catch (Throwable t) {
                        innerT[0] = t;
                    }
                }
            };

            Mercurial.getInstance().getRequestProcessor().post(outOfAwt).waitFinished();
            if (innerT[0] != null) {
                if (innerT[0] instanceof IOException) {
                    throw (IOException) innerT[0];
                } else if (innerT[0] instanceof RuntimeException) {
                    throw (RuntimeException) innerT[0];
                } else if (innerT[0] instanceof Error) {
                    throw (Error) innerT[0];
                } else {
                    throw new IllegalStateException("Unexpected exception class: " + innerT[0]);  // NOI18N
                }
            }

            // end of hack

        } else {
            hgMoveImplementation(from, to);
        }
    }

    private void hgMoveImplementation(final File srcFile, final File dstFile) throws IOException {
        final Mercurial hg = Mercurial.getInstance();
        final File root = hg.getTopmostManagedParent(srcFile);
        if (root == null) return;

        RequestProcessor rp = hg.getRequestProcessor(root.getAbsolutePath());

        Runnable moveImpl = new Runnable() {
            public void run() {
                try {
                    if (srcFile.isDirectory()) {
                        srcFile.renameTo(dstFile);
                        HgCommand.doRenameAfter(root, srcFile, dstFile);
                        return;
                    }
                    int status = hg.getFileStatusCache().getStatus(srcFile).getStatus();
                    if (status == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) {
                        srcFile.renameTo(dstFile);
                    } else if (status == FileInformation.STATUS_VERSIONED_ADDEDLOCALLY) {
                        srcFile.renameTo(dstFile);
                        HgCommand.doRemove(root, srcFile);
                        HgCommand.doAdd(root, dstFile);
                    } else {
                        HgCommand.doRename(root, srcFile, dstFile);
                    }
                } catch (HgException e) {
                    Mercurial.LOG.log(Level.FINE, "Mercurial failed to rename: File: {0} {1}", new Object[] {srcFile.getAbsolutePath(), dstFile.getAbsolutePath()}); // NOI18N
                }
            }
        };

        rp.post(moveImpl).waitFinished();
    }

    public void afterMove(final File from, final File to) {
        Utils.post(new Runnable() {
            public void run() {
                fileMovedImpl(from, to);
            }
        });
    }

    private void fileMovedImpl(final File from, final File to) {
        if (from == null || to == null || !to.exists()) return;
        if (to.isDirectory()) return;
        Mercurial hg = Mercurial.getInstance();        
        final File root = hg.getTopmostManagedParent(from);
        if (root == null) return;
        
        RequestProcessor rp = hg.getRequestProcessor(root.getAbsolutePath());

        HgProgressSupport supportCreate = new HgProgressSupport() {
            public void perform() {
                cache.refresh(from, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                cache.refresh(to, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
            }
        };

        supportCreate.start(rp, root.getAbsolutePath(), 
                org.openide.util.NbBundle.getMessage(MercurialInterceptor.class, "MSG_Move_Progress")); // NOI18N
    }
    
    public boolean beforeCreate(File file, boolean isDirectory) {
        return super.beforeCreate(file, isDirectory);
    }

    public void doCreate(File file, boolean isDirectory) throws IOException {
        super.doCreate(file, isDirectory);
    }

    public void afterCreate(final File file) {
        Utils.post(new Runnable() {
            public void run() {
                fileCreatedImpl(file);
            }
        });
    }

    private void fileCreatedImpl(final File file) {
        if (file.isDirectory()) return;
        Mercurial hg = Mercurial.getInstance();        
        final File root = hg.getTopmostManagedParent(file);
        if (root == null) return;
        
        RequestProcessor rp = hg.getRequestProcessor(root.getAbsolutePath());

        HgProgressSupport supportCreate = new HgProgressSupport() {
            public void perform() {
                cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
            }
        };

        supportCreate.start(rp, root.getAbsolutePath(), 
                org.openide.util.NbBundle.getMessage(MercurialInterceptor.class, "MSG_Create_Progress")); // NOI18N
    }
    
    public void afterChange(final File file) {
        Utils.post(new Runnable() {
            public void run() {
                fileChangedImpl(file);
            }
        });
    }

    private void fileChangedImpl(final File file) {
        if (file.isDirectory()) return;
        Mercurial hg = Mercurial.getInstance();        
        final File root = hg.getTopmostManagedParent(file);
        if (root == null) return;
        
        RequestProcessor rp = hg.getRequestProcessor(root.getAbsolutePath());

        HgProgressSupport supportCreate = new HgProgressSupport() {
            public void perform() {
                Mercurial.LOG.log(Level.FINE, "fileChangedImpl(): File: {0}", file); // NOI18N
                cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
            }
        };

        supportCreate.start(rp, root.getAbsolutePath(), 
                org.openide.util.NbBundle.getMessage(MercurialInterceptor.class, "MSG_Change_Progress")); // NOI18N
   }
}
