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
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.mercurial.ui.pull;

import org.netbeans.modules.versioning.spi.VCSContext;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.ui.merge.MergeAction;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgProjectUtils;
import org.netbeans.modules.mercurial.util.HgRepositoryContextCache;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Pull action for mercurial:
 * hg pull - pull changes from the specified source
 *
 * @author John Rice
 */
public class PullAction extends AbstractAction {

    public enum PullType {

        LOCAL, OTHER
    }
    {
    }

    private final VCSContext context;

    public PullAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }

    public void actionPerformed(ActionEvent e) {
        pull(context);
    }

    public static boolean confirmWithLocalChanges(VCSContext ctx, Class bundleLocation, String title, String query) {
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        File[] localModFiles = cache.listFiles(ctx, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY);
        if (localModFiles != null && localModFiles.length > 0) {
            int response = JOptionPane.showOptionDialog(null, NbBundle.getMessage(bundleLocation, query), NbBundle.getMessage(bundleLocation, title), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

            if (response == JOptionPane.NO_OPTION) {
                return false;
            }
        }
        return true;
    }


    static void annotateChangeSets(List<String> list, Class bundleLocation, String title) {
        InputOutput io = IOProvider.getDefault().getIO(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE, false);
        io.select();
        OutputWriter out = io.getOut();
        OutputWriter outRed = io.getErr();
        outRed.println(NbBundle.getMessage(bundleLocation, title));
        for (String s : list) {
            if (s.indexOf(Mercurial.CHANGESET_STR) == 0) {
                outRed.println(s);
            } else if (!s.equals("")) {
                out.println(s);
            }
        }
        out.println("");
        out.close();
        outRed.close();
    }

    public static void pull(final VCSContext ctx) {
        final File root = HgUtils.getRootFile(ctx);
        if (root == null) return;
        String repository = root.getAbsolutePath();
        final String pullPath = HgCommand.getPullDefault(root).getAbsolutePath();
        final String fromPrjName = HgProjectUtils.getProjectName(root);
        final String toPrjName = HgProjectUtils.getProjectName(root);
        
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {
            public void perform() { performPull(PullType.LOCAL, ctx, root, pullPath, fromPrjName, toPrjName); } };
            
        support.start(rp, repository, org.openide.util.NbBundle.getMessage(PullAction.class, "MSG_PULL_PROGRESS")); // NOI18N
    }

    public boolean isEnabled() {
        // If the repository has a default pull path then enable action
        return HgRepositoryContextCache.getPullDefault(context) == null ? false: true;
    }

    static void performPull(PullType type, VCSContext ctx, File root, String pullPath, String fromPrjName, String toPrjName) {
        if(root == null || pullPath == null) return;
        File bundleFile = null; 
        
        try {
            List<String> listIncoming;
            if(type == PullType.LOCAL){
                listIncoming = HgCommand.doIncoming(root);
            }else{
                for (int i = 0; i < 10000; i++) {
                    if (!new File(root.getParentFile(), root.getName() + "_bundle" + i).exists()) { // NOI18N
                        bundleFile = new File(root.getParentFile(), root.getName() + "_bundle" + i); // NOI18N
                        break;
                    }
                }
                listIncoming = HgCommand.doIncoming(root, pullPath, bundleFile);
            }
            if (listIncoming == null || listIncoming.isEmpty()) return;
            
            boolean bNoChanges = HgCommand.isNoChanges(listIncoming.get(listIncoming.size() - 1));

            // Warn User when there are Local Changes are present that Pull could overwrite
            if (!bNoChanges && !confirmWithLocalChanges(ctx, PullAction.class, "MSG_PULL_LOCALMODS_CONFIRM_TITLE", "MSG_PULL_LOCALMODS_CONFIRM_QUERY")) { // NOI18N
                HgUtils.outputMercurialTabInRed(NbBundle.getMessage(PullAction.class, "MSG_PULL_TITLE")); // NOI18N
                HgUtils.outputMercurialTabInRed(NbBundle.getMessage(PullAction.class, "MSG_PULL_TITLE_SEP")); // NOI18N
                HgUtils.outputMercurialTabInRed(NbBundle.getMessage(PullAction.class, "MSG_PULL_LOCALMODS_CANCEL")); // NOI18N
                HgUtils.outputMercurialTab(""); // NOI18N
                return;
            }

            // Do Pull if there are changes to be pulled
            List<String> list;
            if (bNoChanges) {
                list = listIncoming;
            } else {
                if(type == PullType.LOCAL){
                    list = HgCommand.doPull(root);
                }else{
                    list = HgCommand.doUnbundle(root, bundleFile);
                }
            }            
                       
            if (list != null && !list.isEmpty()) {
                HgUtils.outputMercurialTabInRed(NbBundle.getMessage(PullAction.class, "MSG_PULL_TITLE")); // NOI18N
                HgUtils.outputMercurialTabInRed(NbBundle.getMessage(PullAction.class, "MSG_PULL_TITLE_SEP")); // NOI18N

                if (!bNoChanges) {
                    annotateChangeSets(listIncoming, PullAction.class, "MSG_CHANGESETS_TO_PULL"); // NOI18N
                }

                HgUtils.outputMercurialTab(list);
                HgUtils.outputMercurialTabInRed(NbBundle.getMessage(
                        PullAction.class, "MSG_PULL_FROM", fromPrjName, pullPath)); // NOI18N
                HgUtils.outputMercurialTabInRed(NbBundle.getMessage(
                        PullAction.class, "MSG_PULL_TO", toPrjName, root)); // NOI18N

                // Handle Merge - both automatic and merge with conflicts
                boolean bMergeNeededDueToPull = HgCommand.isMergeNeededMsg(list.get(list.size() - 1));
                boolean bComfirmMerge = false;
                if(bMergeNeededDueToPull){
                    bComfirmMerge = HgUtils.confirmDialog(
                        PullAction.class, "MSG_PULL_MERGE_CONFIRM_TITLE", "MSG_PULL_MERGE_CONFIRM_QUERY"); // NOI18N
                }
                if (bComfirmMerge) {
                    HgUtils.outputMercurialTab(""); // NOI18N
                    HgUtils.outputMercurialTabInRed(NbBundle.getMessage(PullAction.class, "MSG_PULL_MERGE_DO")); // NOI18N
                    MergeAction.doMergeAction(root, null);
                }
                
                List<String> headRevList = HgCommand.getHeadRevisions(root);
                if (headRevList != null && headRevList.size() > 1){
                    MergeAction.printMergeWarning(headRevList);
                }
            }

            if (!bNoChanges) {
                HgUtils.forceStatusRefresh(root);
                // refresh filesystem to take account of deleted files.
                FileObject rootObj = FileUtil.toFileObject(root);
                try {
                    rootObj.getFileSystem().refresh(true);
                } catch (java.lang.Exception ex) {
                }
            }
            HgUtils.outputMercurialTab(""); // NOI18N
            
        } catch (HgException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        } finally {
            if (bundleFile != null) {
                bundleFile.delete();
            }
        }
    }
}
