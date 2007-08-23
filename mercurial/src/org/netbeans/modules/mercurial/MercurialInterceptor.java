/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]" // NOI18N
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.mercurial;

import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.mercurial.HgException;
import java.io.File;
import java.io.IOException;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.util.RequestProcessor;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.api.queries.SharabilityQuery;

/**
 * Listens on file system changes and reacts appropriately, mainly refreshing affected files' status.
 * 
 * @author Maros Sandor
 */
public class MercurialInterceptor extends VCSInterceptor {

    private final FileStatusCache   cache;

    public MercurialInterceptor() {
        cache = Mercurial.getInstance().getFileStatusCache();
    }

    public boolean beforeDelete(File file) {
        Mercurial.getInstance().deletedFile = file.getAbsolutePath();
        Mercurial.getInstance().isDirectory = file.isDirectory();
        // We want to control the removal of a directory.
        if (Mercurial.getInstance().isDirectory)  {
            return true;
        } else {
            return false;
        }
    }

    public void doDelete(File file) throws IOException {
        // We should only get here for a directory; see beforeDelete.
        // We will delete the directory later.
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
        Mercurial hg = Mercurial.getInstance();
        final File root = hg.getTopmostManagedParent(file);
        if (root == null) return;
        RequestProcessor rp = hg.getRequestProcessor(root.getAbsolutePath());
        if (file.getAbsolutePath().equals(Mercurial.getInstance().deletedFile) && Mercurial.getInstance().isDirectory) {
            if (!file.exists()) return;

            // We delete the directory here; at this point we should have 
            // finished calling cache.refresh on all the files deleted in
            // the directory.
            HgProgressSupport supportCreate = new HgProgressSupport() {
                public void perform() {
                    file.delete();
                }
            };

            supportCreate.start(rp, root.getAbsolutePath(), 
                    org.openide.util.NbBundle.getMessage(MercurialInterceptor.class, "MSG_Remove_Progress")); // NOI18N

        } else {
            HgProgressSupport supportCreate = new HgProgressSupport() {
                public void perform() {
                
                    try {
                        HgCommand.doRemove(root, file);
                        cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                    } catch (HgException ex) {
                        Mercurial.LOG.log(Level.FINE, "fileDeletedImpl(): File: {0} {1}", new Object[] {file.getAbsolutePath(), ex.toString()}); // NOI18N
                    }             
                }
            };

            supportCreate.start(rp, root.getAbsolutePath(), 
                    org.openide.util.NbBundle.getMessage(MercurialInterceptor.class, "MSG_Remove_Progress")); // NOI18N

        }
    }

    public boolean beforeMove(File from, File to) {
        if (from == null || to == null || to.exists()) return true;
        
        return super.beforeMove(from, to);
    }

    public void doMove(File from, File to) throws IOException {
        if (from == null || to == null || to.exists()) return;
        
        super.doMove(from, to);
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
        Mercurial hg = Mercurial.getInstance();        
        final File root = hg.getTopmostManagedParent(from);
        if (root == null) return;
        
        RequestProcessor rp = hg.getRequestProcessor(root.getAbsolutePath());

        HgProgressSupport supportCreate = new HgProgressSupport() {
            public void perform() {
                
                try {
                    if (HgUtils.isLocallyAdded(from)){
                        HgCommand.doRemove(root, from);
                    } else {
                        HgCommand.doRenameAfter(root, from, to);
                    }
                    cache.refresh(from, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                    cache.refresh(to, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                } catch (HgException ex) {
                    Mercurial.LOG.log(Level.FINE, "fileMovedImpl(): From: {0} To: {1} {2}", new Object[] {from.getAbsolutePath(), to.getAbsolutePath(), ex.toString()}); // NOI18N
                }             
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
        if(HgUtils.isIgnored(file) || SharabilityQuery.getSharability(file) == SharabilityQuery.NOT_SHARABLE){
            Mercurial.LOG.log(Level.FINE, "fileCreatedImpl(): Ignored File: {0}", new Object[] {file.getAbsolutePath()}); // NOI18N
            return;
        }
        cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
    }
    
    public void afterChange(final File file) {
        Utils.post(new Runnable() {
            public void run() {
                fileChangedImpl(file);
            }
        });
    }

    private void fileChangedImpl(File file) {
        if (file.isDirectory()) return;
        if(HgUtils.isIgnored(file) || SharabilityQuery.getSharability(file) == SharabilityQuery.NOT_SHARABLE){
            Mercurial.LOG.log(Level.FINE, "fileChangedImpl(): Ignored File: {0}", new Object[] {file.getAbsolutePath()}); // NOI18N
            return;
        }
        cache.refreshForce(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
   }
}
