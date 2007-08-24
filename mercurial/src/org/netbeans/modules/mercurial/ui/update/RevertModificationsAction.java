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

package org.netbeans.modules.mercurial.ui.update;

import java.io.File;
import java.util.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.HgException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import javax.swing.AbstractAction;
import org.netbeans.modules.mercurial.util.HgRepositoryContextCache;

/**
 * Reverts local changes.
 *
 * @author Padraig O'Briain
 */
public class RevertModificationsAction extends AbstractAction {
    
    private final VCSContext context;
 
    public RevertModificationsAction(String name, VCSContext context) {        
        this.context =  context;
        putValue(Action.NAME, name);
    }

    public void actionPerformed(ActionEvent e) {
        revert(context);
    }

    public static void revert(final VCSContext ctx) {
        final File[] files = ctx.getRootFiles().toArray(new File[ctx.getRootFiles().size()]);
        final File repository  = HgUtils.getRootFile(ctx);
        if (repository == null) return;
        String rev = null;

        final RevertModifications revertModifications = new RevertModifications(repository, files);
        if (!revertModifications.showDialog()) {
            return;
        }
        rev = revertModifications.getSelectionRevision();
        final String revStr = rev;

        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {
            public void perform() {
                performRevert(repository, revStr, files);
            }
        };
        support.start(rp, repository.getAbsolutePath(), org.openide.util.NbBundle.getMessage(UpdateAction.class, "MSG_Update_Progress")); // NOI18N

        return;
    }

    public static void performRevert(File repository, String revStr, File file) {
        File[] files = new File[1];
        files[0] = file;

        performRevert(repository, revStr, files);
    }

    public static void performRevert(File repository, String revStr, File[] files) {
        try {
            List<File> revertFiles = new ArrayList();
            for (File file : files) {
                revertFiles.add(file);
            }
            HgUtils.outputMercurialTabInRed(
                    NbBundle.getMessage(RevertModificationsAction.class,
                    "MSG_REVERT_TITLE")); // NOI18N
            HgUtils.outputMercurialTabInRed(
                    NbBundle.getMessage(RevertModificationsAction.class,
                    "MSG_REVERT_TITLE_SEP")); // NOI18N
            HgUtils.outputMercurialTab(
                    NbBundle.getMessage(RevertModificationsAction.class,
                    "MSG_REVERT_REVISION_STR", revStr)); // NOI18N
            for (File file : files) {
                HgUtils.outputMercurialTab(file.getAbsolutePath());
            }
            HgUtils.outputMercurialTab(""); // NOI18N
 
            HgCommand.doRevert(repository, revertFiles, revStr);
            FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
            File[] conflictFiles = cache.listFiles(files, FileInformation.STATUS_VERSIONED_CONFLICT);
            if(conflictFiles.length != 0){
                ConflictResolvedAction.conflictResolved(repository, conflictFiles);
            }
        } catch (HgException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        }

        if (revStr == null) {
            for (File file : files) {
                HgUtils.forceStatusRefresh(file);
            }
        } else {
            HgUtils.forceStatusRefresh(files[0]);
        }
        // refresh filesystem to take account of changes
        FileObject rootObj = FileUtil.toFileObject(repository);
        try {
            rootObj.getFileSystem().refresh(true);
        } catch (java.lang.Exception exc) {
        }
        HgUtils.outputMercurialTabInRed(
                NbBundle.getMessage(RevertModificationsAction.class,
                "MSG_REVERT_DONE")); // NOI18N
 
    }

    public boolean isEnabled() {
        return HgRepositoryContextCache.hasHistory(context);
    }
}
