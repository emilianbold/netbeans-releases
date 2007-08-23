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
package org.netbeans.modules.mercurial.ui.update;

import java.io.File;
import java.util.List;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import javax.swing.*;
import java.awt.event.ActionEvent;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.util.RequestProcessor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import javax.swing.AbstractAction;

/**
 * Update action for mercurial: 
 * hg update - update or merge working directory
 * 
 * @author John Rice
 */
public class UpdateAction extends AbstractAction {
    
    private final VCSContext context;

    public UpdateAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }
    
    public void actionPerformed(ActionEvent e) {
        update(context, null);
    }
    
    public static void update(VCSContext ctx, final String revision){
        final File root = HgUtils.getRootFile(ctx);
        if (root == null) return;
        String repository = root.getAbsolutePath();
        
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {
            public void perform() {
                boolean bNoUpdates = true;
                try {
                    List<String> list = HgCommand.doUpdateAll(root, false, revision);
                    
                    if (list != null && !list.isEmpty()){
                        bNoUpdates = HgCommand.isNoUpdates(list.get(0));
                        //HgUtils.clearOutputMercurialTab();
                        HgUtils.outputMercurialTabInRed(
                                NbBundle.getMessage(UpdateAction.class,
                                "MSG_UPDATE_TITLE")); // NOI18N
                        HgUtils.outputMercurialTabInRed(
                                NbBundle.getMessage(UpdateAction.class,
                                "MSG_UPDATE_TITLE_SEP")); // NOI18N
                        HgUtils.outputMercurialTab(list);
                        HgUtils.outputMercurialTab(""); // NOI18N
                    }  
                    // refresh filesystem to take account of changes
                    FileObject rootObj = FileUtil.toFileObject(root);
                    try {
                        rootObj.getFileSystem().refresh(true);
                    } catch (Exception ex) {
                    }

                } catch (HgException ex) {
                    NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
                    DialogDisplayer.getDefault().notifyLater(e);
                }
                
                // Force Status Refresh from this dir and below
                if(!bNoUpdates)
                    HgUtils.forceStatusRefresh(root);

                HgUtils.outputMercurialTabInRed(
                        NbBundle.getMessage(UpdateAction.class,
                        "MSG_UPDATE_DONE")); // NOI18N
            }
        };
        support.start(rp, repository, org.openide.util.NbBundle.getMessage(UpdateAction.class, "MSG_Update_Progress")); // NOI18N
    }
    
    public boolean isEnabled() {
        // If it's a mercurial managed repository enable Update action
        File root = HgUtils.getRootFile(context);
        if (root == null)
            return false;
        else
            return true;
    }     
}
