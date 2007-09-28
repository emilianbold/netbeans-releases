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
package org.netbeans.modules.mercurial.ui.push;

import org.netbeans.modules.versioning.spi.VCSContext;


import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.ui.merge.MergeAction;
import org.netbeans.modules.mercurial.ui.pull.PullAction;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgProjectUtils;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.util.HgRepositoryContextCache;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;

/**
 * Push action for mercurial: 
 * hg push - push changes to the specified destination
 * 
 * @author John Rice
 */
public class PushAction extends AbstractAction {
    
    private final VCSContext context;

    public PushAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }
    
    public void actionPerformed(ActionEvent e) {
        push(context);
    }
    
    public static void push(VCSContext ctx){
        final File root = HgUtils.getRootFile(ctx);
        if (root == null) return;
        String repository = root.getAbsolutePath();
        final File pushFile = HgCommand.getPushDefault(root);
        if(pushFile == null) return;
        final String fromPrjName = HgProjectUtils.getProjectName(root);
        final String toPrjName = HgProjectUtils.getProjectName(pushFile);

        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {
            public void perform() { performPush(root, pushFile, fromPrjName, toPrjName); } };
        support.start(rp, repository, 
                org.openide.util.NbBundle.getMessage(PushAction.class, "MSG_PUSH_PROGRESS")); // NOI18N
        
    }
                
    static void performPush(File root, File pushFile, String fromPrjName, String toPrjName) {
        try {
            List<String> listOutgoing = HgCommand.doOutgoing(root, pushFile);
            if ((listOutgoing == null) || listOutgoing.isEmpty()) return;

            boolean bLocalPush = (FileUtil.toFileObject(FileUtil.normalizeFile(pushFile)) != null);
            boolean bNoChanges = HgCommand.isNoChanges(listOutgoing.get(listOutgoing.size()-1));

            HgUtils.outputMercurialTabInRed(NbBundle.getMessage(PushAction.class, "MSG_PUSH_TITLE")); // NOI18N
            HgUtils.outputMercurialTabInRed(NbBundle.getMessage(PushAction.class, "MSG_PUSH_TITLE_SEP")); // NOI18N

            if (bLocalPush) {
                // Warn user if there are local changes which Push will overwrite
                if (!bNoChanges && !PullAction.confirmWithLocalChanges(pushFile, PushAction.class,
                     "MSG_PUSH_LOCALMODS_CONFIRM_TITLE", "MSG_PUSH_LOCALMODS_CONFIRM_QUERY", listOutgoing)) { // NOI18N
                    HgUtils.outputMercurialTabInRed(NbBundle.getMessage(PullAction.class, "MSG_PULL_LOCALMODS_CANCEL")); // NOI18N
                    HgUtils.outputMercurialTab(""); // NOI18N
                    return;
                }
            }

            List<String> list = HgCommand.doPush(root, pushFile);
                    
            if (list != null && !list.isEmpty()) {
                        
                if(!HgCommand.isNoChanges(listOutgoing.get(listOutgoing.size()-1))){
                    InputOutput io = IOProvider.getDefault().getIO(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE, false);
                    io.select();
                    OutputWriter out = io.getOut();
                    OutputWriter outRed = io.getErr();
                    outRed.println(NbBundle.getMessage(PushAction.class,"MSG_CHANGESETS_TO_PUSH")); // NOI18N
                    for( String s : listOutgoing){
                        if (s.indexOf(Mercurial.CHANGESET_STR) == 0){
                            outRed.println(s);
                        }else if( !s.equals("")){ // NOI18N
                            out.println(s);
                        }
                    }
                    out.println(""); // NOI18N
                    out.close();
                    outRed.close();
                }

                HgUtils.outputMercurialTab(list);
                        
                if (toPrjName == null) {
                    HgUtils.outputMercurialTabInRed(
                                NbBundle.getMessage(PushAction.class,
                                "MSG_PUSH_TO_NONAME", pushFile)); // NOI18N
                } else {
                    HgUtils.outputMercurialTabInRed(
                                NbBundle.getMessage(PushAction.class,
                                "MSG_PUSH_TO", toPrjName, pushFile)); // NOI18N
                }
                HgUtils.outputMercurialTabInRed(
                            NbBundle.getMessage(PushAction.class,
                            "MSG_PUSH_FROM", fromPrjName, root)); // NOI18N
                                            
                boolean bMergeNeeded = HgCommand.isHeadsCreated(list.get(list.size()-1));
                boolean bConfirmMerge = false;
                // Push does not do an Update of the target Working Dir
                if(!bMergeNeeded){
                    HgUtils.outputMercurialTab(""); // NOI18N
                    if (!bLocalPush) {
                        HgUtils.outputMercurialTabInRed(
                                    NbBundle.getMessage(PushAction.class,
                                    "MSG_PUSH_UPDATE_NEEDED_NONAME", toPrjName, pushFile)); // NOI18N
                    } else {
                        list = HgCommand.doUpdateAll(pushFile, false, null, false);                    
                        HgUtils.outputMercurialTab(list);
                        if (toPrjName != null) {
                            HgUtils.outputMercurialTabInRed(
                                        NbBundle.getMessage(PushAction.class,
                                        "MSG_PUSH_UPDATE_DONE", toPrjName, pushFile)); // NOI18N
                        } else {
                            HgUtils.outputMercurialTabInRed(
                                        NbBundle.getMessage(PushAction.class,
                                        "MSG_PUSH_UPDATE_DONE_NONAME", pushFile)); // NOI18N
                        }
                        boolean bOutStandingUncommittedMerges = HgCommand.isErrorOutStandingUncommittedMerges(list.get(list.size() -1));
                        if (bOutStandingUncommittedMerges) {
                            bConfirmMerge = HgUtils.confirmDialog(PushAction.class, "MSG_PUSH_MERGE_CONFIRM_TITLE", "MSG_PUSH_MERGE_UNCOMMITTED_CONFIRM_QUERY");
                        }
                    }
                } else {     
                    bConfirmMerge = HgUtils.confirmDialog(PushAction.class, "MSG_PUSH_MERGE_CONFIRM_TITLE", "MSG_PUSH_MERGE_CONFIRM_QUERY");
                }     

                if (bConfirmMerge) {
                    HgUtils.outputMercurialTab(""); // NOI18N
                    HgUtils.outputMercurialTabInRed(
                               NbBundle.getMessage(PushAction.class,
                               "MSG_PUSH_MERGE_DO")); // NOI18N
                    MergeAction.doMergeAction(pushFile, null);
                } else {
                    List<String> headRevList = HgCommand.getHeadRevisions(pushFile);
                    if (headRevList != null && headRevList.size() > 1) {
                        MergeAction.printMergeWarning(headRevList);
                    }
                }
            }     
            if (bLocalPush && !bNoChanges) {
                HgUtils.forceStatusRefresh(pushFile);
                // refresh filesystem to take account of deleted files
                FileObject rootObj = FileUtil.toFileObject(pushFile);
                try {
                    rootObj.getFileSystem().refresh(true);
                } catch (java.lang.Exception ex) {
                }
            }
            HgUtils.outputMercurialTab(""); // NOI18N
        } catch (HgException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        }
    }
    
    public boolean isEnabled() {
        // If the repository has a default push path then enable action
        return HgRepositoryContextCache.getPushDefault(context) == null ? false: true;
    }
}
