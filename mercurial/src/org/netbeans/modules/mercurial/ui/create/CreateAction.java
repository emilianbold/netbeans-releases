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
package org.netbeans.modules.mercurial.ui.create;

import java.io.File;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgProjectUtils;
import org.openide.util.RequestProcessor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Create action for mercurial: 
 * hg init - create a new repository in the given directory
 * 
 * @author John Rice
 */
public class CreateAction extends AbstractAction {
    
    private final VCSContext context;
    Map<File, FileInformation> repositoryFiles = new HashMap<File, FileInformation>();

    public CreateAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }

    public boolean isEnabled() {
        // If it is not a mercurial managed repository enable action
        File root = HgUtils.getRootFile(context);
        File [] files = context.getRootFiles().toArray(new File[context.getRootFiles().size()]);
        if ( files == null || files.length == 0) 
            return false;
        
        if (root == null)
            return true;
        else
            return false;
    } 

    public void actionPerformed(ActionEvent e) {
        final Mercurial hg = Mercurial.getInstance();
        File [] files = context.getRootFiles().toArray(new File[context.getRootFiles().size()]);
        if(files == null || files.length == 0) return;
        
        File root = files[0];
        if(!root.isDirectory()) root = root.getParentFile();
        if (hg.getTopmostManagedParent(root) != null) return;
        HgUtils.outputMercurialTabInRed(
                NbBundle.getMessage(CreateAction.class,
                "MSG_CREATE_TITLE")); // NOI18N
        HgUtils.outputMercurialTabInRed(
                NbBundle.getMessage(CreateAction.class,
                "MSG_CREATE_TITLE_SEP")); // NOI18N
        while (HgProjectUtils.getProjectName(root) == null) {
            root = root.getParentFile();
        }
        final File rootToManage = root;
        final String prjName = HgProjectUtils.getProjectName(rootToManage);

        RequestProcessor rp = hg.getRequestProcessor(rootToManage.getAbsolutePath());
        
        HgProgressSupport supportCreate = new HgProgressSupport() {
            public void perform() {
                
                try {
                    HgUtils.outputMercurialTab(
                            NbBundle.getMessage(CreateAction.class,
                            "MSG_CREATE_INIT", prjName, rootToManage)); // NOI18N
                    HgCommand.doCreate(rootToManage);
                    hg.versionedFilesChanged();
                    hg.refreshAllAnnotations();      
                    HgUtils.createIgnored(rootToManage);
                    
                } catch (HgException ex) {
                    NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
                    DialogDisplayer.getDefault().notifyLater(e);
                }             
            }
        };
        supportCreate.start(rp, rootToManage.getAbsolutePath(), 
                org.openide.util.NbBundle.getMessage(CreateAction.class, "MSG_Create_Progress")); // NOI18N

        
        HgProgressSupport supportAdd = new HgProgressSupport() {
            public void perform() {
                List<File> addFiles = new LinkedList<File>();
                try {
                    repositoryFiles = HgCommand.getAllUnknownStatus(rootToManage);
                    addFiles.addAll(repositoryFiles.keySet());
                    HgUtils.outputMercurialTab(
                            NbBundle.getMessage(CreateAction.class,
                            "MSG_CREATE_ADD", addFiles.size())); // NOI18N
                    for(File f: addFiles){
                        HgUtils.outputMercurialTab("\t" + f.getAbsolutePath()); // NOI18N
                    }
                 } catch (HgException ex) {
                    NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
                    DialogDisplayer.getDefault().notifyLater(e);
                }
                FileStatusCache cache = hg.getFileStatusCache();
                for (File file : addFiles){
                    cache.refreshCached(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                } 
                HgUtils.outputMercurialTabInRed(NbBundle.getMessage(CreateAction.class,
                            "MSG_CREATE_DONE")); // NOI18N
                HgUtils.outputMercurialTab(""); // NOI18N
            }
        };
        supportAdd.start(rp, rootToManage.getAbsolutePath(), 
                org.openide.util.NbBundle.getMessage(CreateAction.class, "MSG_Create_Add_Progress")); // NOI18N

        /*
         * TODO: Do we commit the NB project files on creation or not?
        HgProgressSupport supportCommit = new HgProgressSupport() {
            public void perform() {
                // Commit newly added files
                String contentTitle = Utils.getContextDisplayName(context);
                CommitAction.commit(contentTitle + " - Create", VCSContext.forFiles(repositoryFiles.keySet())); // NOI18N
            }
        };
        supportCommit.start(rp, rootToManage.getAbsolutePath(),
                org.openide.util.NbBundle.getMessage(CreateAction.class, "MSG_Create_Commit_Progress")); // NOI18N
        */
    }
}
