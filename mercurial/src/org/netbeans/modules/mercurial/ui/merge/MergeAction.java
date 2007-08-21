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
package org.netbeans.modules.mercurial.ui.merge;

import java.io.File;
import java.util.List;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.versioning.spi.VCSContext;
import javax.swing.*;
import java.awt.event.ActionEvent;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.util.HgRepositoryContextCache;
import org.openide.windows.OutputWriter;

/**
 * Merge action for mercurial:
 * hg merge - attempts to merge changes when the repository has 2 heads
 *
 * @author John Rice
 */
public class MergeAction extends AbstractAction {

    private final VCSContext context;
    private String revStr;
    private final static int MULTIPLE_AUTOMERGE_HEAD_LIMIT = 2;
    
    public MergeAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
        
        System.out.println("Merge: " + context);
    }

    public boolean isEnabled() {        
        return HgRepositoryContextCache.hasHeads(context);
    }

    public void actionPerformed(ActionEvent ev) {
        final File root = HgUtils.getRootFile(context);
        if (root == null) {
            return;
        }
        String repository = root.getAbsolutePath();
        try{
            List<String> headList = HgCommand.getHeadRevisions(root);
            revStr = null;
            if (headList.size() > MULTIPLE_AUTOMERGE_HEAD_LIMIT){
                final MergeRevisions mergeDlg = new MergeRevisions(root);
                if (!mergeDlg.showDialog()) {
                    return;
                }
                revStr = mergeDlg.getSelectionRevision();               
            }
        } catch(HgException ex) {
                    NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
                    DialogDisplayer.getDefault().notifyLater(e);
        }
        
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {
            public void perform() {
                try {
                    HgUtils.outputMercurialTabInRed(
                            NbBundle.getMessage(MergeAction.class, "MSG_MERGE_TITLE"));
                    HgUtils.outputMercurialTabInRed(
                            NbBundle.getMessage(MergeAction.class, "MSG_MERGE_TITLE_SEP"));
                    doMergeAction(root, revStr);
                    HgUtils.forceStatusRefresh(root);
                    HgUtils.outputMercurialTab("");
                } catch (HgException ex) {
                    NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
                    DialogDisplayer.getDefault().notifyLater(e);
                }
            }
        };
        support.start(rp, repository, NbBundle.getMessage(MergeAction.class, "MSG_MERGE_PROGRESS")); // NOI18N
    }

    public static boolean doMergeAction(File root, String revStr) throws HgException {
        List<String> listMerge = HgCommand.doMerge(root, revStr);
        Boolean bConflicts = false;
        Boolean bMergeFailed = false;
        
        if (listMerge != null && !listMerge.isEmpty()) {
            HgUtils.outputMercurialTab(listMerge);
            for (String line : listMerge) {
                if (HgCommand.isMergeAbortMultipleHeadsMsg(line) || 
                        HgCommand.isMergeAbortUncommittedMsg(line)){ 
                        bMergeFailed = true;
                        HgUtils.outputMercurialTabInRed(NbBundle.getMessage(MergeAction.class, 
                        "MSG_MERGE_FAILED"));
                        break;
                }
                if (HgCommand.isMergeConflictMsg(line)) {
                    bConflicts = true;
                    String filepath = line.substring(HgCommand.HG_MERGE_CONFLICT_ERR.length());
                    HgUtils.outputMercurialTabInRed(NbBundle.getMessage(MergeAction.class, "MSG_MERGE_CONFLICT", filepath));
                    HgCommand.createConflictFile(filepath);
                }
            }
         
            if (bConflicts) {
                HgUtils.outputMercurialTabInRed(NbBundle.getMessage(MergeAction.class, 
                        "MSG_MERGE_DONE_CONFLICTS"));
            }
            if (!bMergeFailed && !bConflicts) {
                HgUtils.outputMercurialTabInRed(NbBundle.getMessage(MergeAction.class, 
                        "MSG_MERGE_DONE"));
            }
        }
        return true;
    }
    
    public static void printMergeWarning(OutputWriter outRed, List<String> list){
        if(list == null || list.isEmpty() || list.size() <= 1) return;
        
        if (list.size() == 2) {
            outRed.println(NbBundle.getMessage(MergeAction.class, 
                    "MSG_MERGE_WARN_NEEDED", list));
            outRed.println(NbBundle.getMessage(MergeAction.class, 
                    "MSG_MERGE_DO_NEEDED"));
        } else {
            outRed.println(NbBundle.getMessage(MergeAction.class, 
                    "MSG_MERGE_WARN_MULTIPLE_HEADS", list.size(), list));
            outRed.println(NbBundle.getMessage(MergeAction.class, 
                    "MSG_MERGE_DONE_MULTIPLE_HEADS"));
        }
    }
    
    public static void printMergeWarning(List<String> list){
        if(list == null || list.isEmpty() || list.size() <= 1) return;
        
        if (list.size() == 2) {
            HgUtils.outputMercurialTabInRed(NbBundle.getMessage(MergeAction.class, 
                    "MSG_MERGE_WARN_NEEDED", list));
            HgUtils.outputMercurialTabInRed(NbBundle.getMessage(MergeAction.class, 
                    "MSG_MERGE_DO_NEEDED"));
        } else {
            HgUtils.outputMercurialTabInRed(NbBundle.getMessage(MergeAction.class, 
                    "MSG_MERGE_WARN_MULTIPLE_HEADS", list.size(), list));
            HgUtils.outputMercurialTabInRed(NbBundle.getMessage(MergeAction.class, 
                    "MSG_MERGE_DONE_MULTIPLE_HEADS"));
        }
    }

}
