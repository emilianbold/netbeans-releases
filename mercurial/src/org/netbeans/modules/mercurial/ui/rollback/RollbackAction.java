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
package org.netbeans.modules.mercurial.ui.rollback;

import org.netbeans.modules.versioning.spi.VCSContext;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.util.HgRepositoryContextCache;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.ui.update.ConflictResolvedAction;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Pull action for mercurial: 
 * hg pull - pull changes from the specified source
 * 
 * @author John Rice
 */
public class RollbackAction extends AbstractAction {
    
    private final VCSContext context;
    private static File pullPath = null;
            
    public RollbackAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }
    
    public void actionPerformed(ActionEvent e) {
        rollback(context);
    }
    
    public static void rollback(final VCSContext ctx){
        final File root = HgUtils.getRootFile(ctx);
        if (root == null) return;
        String repository = root.getAbsolutePath();
         
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {
            public void perform() {
                
                try {
                    List<String> list = HgCommand.doRollback(root);
                    
                    if(list != null && !list.isEmpty()){                      
                        //HgUtils.clearOutputMercurialTab();
                        HgUtils.outputMercurialTabInRed(
                                    NbBundle.getMessage(RollbackAction.class,
                                    "MSG_ROLLBACK_TITLE")); // NOI18N
                        HgUtils.outputMercurialTabInRed(
                                    NbBundle.getMessage(RollbackAction.class,
                                    "MSG_ROLLBACK_TITLE_SEP")); // NOI18N
                        
                        if(HgCommand.isNoRollbackPossible(list.get(0))){
                            HgUtils.outputMercurialTab(
                                    NbBundle.getMessage(RollbackAction.class,
                                    "MSG_NO_ROLLBACK"));     // NOI18N                       
                        }else{
                            HgUtils.outputMercurialTab(list.get(0));
                            if (HgCommand.hasHistory(root)) {
                                int response = JOptionPane.showOptionDialog(null,
                                        NbBundle.getMessage(RollbackAction.class,"MSG_ROLLBACK_CONFIRM_QUERY") ,  // NOI18N
                                        NbBundle.getMessage(RollbackAction.class,"MSG_ROLLBACK_CONFIRM"), // NOI18N
                                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,null, null, null);
                            
                                if( response == JOptionPane.YES_OPTION){
                                    HgUtils.outputMercurialTab(
                                            NbBundle.getMessage(RollbackAction.class,
                                            "MSG_ROLLBACK_FORCE_UPDATE", root.getAbsolutePath())); // NOI18N
                                    list = HgCommand.doUpdateAll(root, true, null);
                                    
                                    FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();        
                                    if(cache.listFiles(ctx, FileInformation.STATUS_VERSIONED_CONFLICT).length != 0){
                                        ConflictResolvedAction.resolved(ctx);                                       
                                    }
                                    HgUtils.forceStatusRefresh(root);                                     
                                    
                                    if (list != null && !list.isEmpty()){
                                        HgUtils.outputMercurialTab(list);
                                    }
                                }
                            } else {
                                JOptionPane.showMessageDialog(null,
                                        NbBundle.getMessage(RollbackAction.class,"MSG_ROLLBACK_MESSAGE_NOHISTORY") ,  // NOI18N
                                        NbBundle.getMessage(RollbackAction.class,"MSG_ROLLBACK_MESSAGE"), // NOI18N
                                        JOptionPane.INFORMATION_MESSAGE,null);
                            
                            }
                        }
                        HgUtils.outputMercurialTabInRed(
                                    NbBundle.getMessage(RollbackAction.class,
                                    "MSG_ROLLBACK_INFO")); // NOI18N
                        HgUtils.outputMercurialTab(""); // NOI18N
                    }
                } catch (HgException ex) {
                    NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
                    DialogDisplayer.getDefault().notifyLater(e);
                }
                
            }
        };
        support.start(rp, repository,org.openide.util.NbBundle.getMessage(RollbackAction.class, "MSG_ROLLBACK_PROGRESS")); // NOI18N
    }
    
    public boolean isEnabled() {
        return HgRepositoryContextCache.hasHistory(context);
    }
}
