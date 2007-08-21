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

/**
 * Push action for mercurial: 
 * hg push - push changes to the specified destination
 * 
 * @author John Rice
 */
public class PushAction extends AbstractAction {
    
    private final VCSContext context;
    private static File pushPath = null;
    private static boolean bMergeNeeded = false;

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
        pushPath = HgCommand.getPushDefault(root);
        if(pushPath == null) return;
        final String fromPrjName = HgProjectUtils.getProjectName(root);
        final String toPrjName = HgProjectUtils.getProjectName(pushPath);
        
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {
            public void perform() {
                
                try {
                    // TODO: for remote repositories use --bundle so you do not have to push down
                    // changesets twice.
                    List<String> listOutgoing = HgCommand.doOutgoing(root, pushPath);
                    List<String> list = HgCommand.doPush(root, pushPath);
                    
                    if(list != null && !list.isEmpty()){                      
                        HgUtils.outputMercurialTabInRed(
                                    NbBundle.getMessage(PushAction.class,
                                    "MSG_PUSH_TITLE"));
                        HgUtils.outputMercurialTabInRed(
                                    NbBundle.getMessage(PushAction.class,
                                    "MSG_PUSH_TITLE_SEP"));
                        boolean bNoChanges = HgCommand.isNoChanges(list.get(list.size()-1));
                        bMergeNeeded = HgCommand.isHeadsCreated(list.get(list.size()-1));
                        
                        if(listOutgoing != null && !listOutgoing.isEmpty() && 
                                !HgCommand.isNoChanges(listOutgoing.get(listOutgoing.size()-1))){
                            InputOutput io = IOProvider.getDefault().getIO(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE, false);
                            io.select();
                            OutputWriter out = io.getOut();
                            OutputWriter outRed = io.getErr();
                            outRed.println(NbBundle.getMessage(PushAction.class,"MSG_CHANGESETS_TO_PUSH"));
                            for( String s : listOutgoing){
                                if (s.indexOf(Mercurial.CHANGESET_STR) == 0){
                                    outRed.println(s);
                                }else if( !s.equals("")){
                                    out.println(s);
                                }
                            }
                            out.println("");
                            out.close();
                            outRed.close();
                        }

                        HgUtils.outputMercurialTab(list);
                        
                        HgUtils.outputMercurialTabInRed(
                                    NbBundle.getMessage(PushAction.class,
                                    "MSG_PUSH_TO", toPrjName, pushPath));
                        HgUtils.outputMercurialTabInRed(
                                    NbBundle.getMessage(PushAction.class,
                                    "MSG_PUSH_FROM", fromPrjName, root));
                                                
                        // Push does not do an Update of the target Working Dir
                        if(!bMergeNeeded && !bNoChanges){
                            HgUtils.outputMercurialTab("");
                            HgUtils.outputMercurialTabInRed(
                                        NbBundle.getMessage(PushAction.class,
                                        "MSG_PUSH_UPDATE_NEEDED", toPrjName, pushPath));
                            list = HgCommand.doUpdateAll(pushPath, false, null);                    
                            HgUtils.outputMercurialTab(list);
                        }     
                        
                        if (bMergeNeeded){
                            // TODO: Handle Merge
                            HgUtils.outputMercurialTab("");
                            List<String> headRevList = HgCommand.getHeadRevisions(root);
                            if (headRevList != null && headRevList.size() > 1) {
                                MergeAction.printMergeWarning(headRevList);
                            }
                        }
                        HgUtils.outputMercurialTab("");
                     }
                } catch (HgException ex) {
                    NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
                    DialogDisplayer.getDefault().notifyLater(e);
                }
                
            }
        };
        support.start(rp, repository, 
                org.openide.util.NbBundle.getMessage(PushAction.class, "MSG_PUSH_PROGRESS")); // NOI18N
        
    }
    
    public boolean isEnabled() {
        // If the repository has a default push path then enable action
        return HgRepositoryContextCache.getPushDefault(context) == null ? false: true;
    }
}
