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
package org.netbeans.modules.mercurial.ui.log;

import java.io.File;
import java.util.List;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.util.HgRepositoryContextCache;
import org.netbeans.modules.versioning.spi.VCSContext;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.ui.merge.MergeAction;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgProjectUtils;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Log action for mercurial: 
 * hg log - show revision history of entire repository or files
 * 
 * @author John Rice
 */
public class LogAction extends AbstractAction {
    
    private final VCSContext context;
    private List<File> logFiles = new ArrayList<File>();
    private boolean bMergeNeeded = false;
    
    public LogAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }
    
    public void actionPerformed(ActionEvent e) {
        log(context);
    }
    
    private void log(VCSContext ctx){
        Mercurial hg = Mercurial.getInstance();
        
        File [] files = ctx.getRootFiles().toArray(new File[ctx.getRootFiles().size()]);
        if (files == null || files.length == 0) return;
        final File root = hg.getTopmostManagedParent(files[0]);
        if (root == null) return;
        String projectName = HgProjectUtils.getProjectName(root);
        Boolean projIsRepos = true;
        File projFile = root;
        if (projectName == null) {
            projIsRepos = false;
            projFile = HgUtils.getProjectFile(ctx);
            projectName =  HgProjectUtils.getProjectName(projFile);
        }
        final String prjName = projectName;
        final File prjFile = projFile;
        
        final boolean bLogAll = root.equals(files[0]);
        
        if (!bLogAll){
            FileStatusCache cache = hg.getFileStatusCache();        
            addFiles (files, cache);
        }
                
        RequestProcessor rp = hg.getRequestProcessor(root.getAbsolutePath());        
        HgProgressSupport support = new HgProgressSupport() {
            public void perform() {
                Mercurial hg = Mercurial.getInstance();
                try {                    
                    List<String> headRevList = HgCommand.getHeadRevisions(root);
                    if(headRevList == null) return;
                    bMergeNeeded = headRevList.size() > 1;
                    
                    List<String> list = new LinkedList<String>();
                    if (bLogAll){
                        list = HgCommand.doLogAll(root);
                    }else{
                        list = HgCommand.doLog(root, logFiles);
                    }
                    
                    if (list != null && !list.isEmpty()){
                        String tabTitle;
                        int rev =  HgCommand.getBranchRev(root);
                        if ( rev > -1){
                            tabTitle = NbBundle.getMessage(LogAction.class,
                                    "MSG_Log_TabTitle", prjName); // NOI18N
                        }else{
                            tabTitle = NbBundle.getMessage(LogAction.class,
                                    "MSG_Log_TabTitleNotCommitted"); // NOI18N
                        }
                        InputOutput io = IOProvider.getDefault().getIO(tabTitle, false);
                        io.select();
                        OutputWriter out = io.getOut();
                        OutputWriter outRed = io.getErr();

                        outRed.println(NbBundle.getMessage(LogAction.class, "MSG_Log_Title")); // NOI18N
                        outRed.println(NbBundle.getMessage(LogAction.class, "MSG_Log_Title_Sep")); // NOI18N
                        
                        for( String s : list){
                            if (s.indexOf(Mercurial.CHANGESET_STR) == 0){
                                outRed.println(s);
                            }else if( !s.equals("")){ // NOI18N
                                out.println(s);
                            }
                        }
                        if (!bLogAll)
                            outRed.println(NbBundle.getMessage(LogAction.class, "MSG_Log_Files", // NOI18N
                                logFiles));
                        
                        outRed.println(NbBundle.getMessage(LogAction.class, "MSG_Log_PrjName", // NOI18N
                                prjName));
                        outRed.println(NbBundle.getMessage(LogAction.class, "MSG_Log_PrjPath", // NOI18N
                                prjFile.getAbsolutePath()));
                        File pullPath = HgCommand.getPullDefault(root);
                        if(pullPath != null){
                            outRed.println(NbBundle.getMessage(LogAction.class, "MSG_Log_Pull_Path",pullPath.getAbsolutePath())); // NOI18N
                        }
                        File pushPath = HgCommand.getPushDefault(root);
                        if(pushPath != null){
                            outRed.println(NbBundle.getMessage(LogAction.class, "MSG_Log_Push_Path",pushPath.getAbsolutePath())); // NOI18N
                        }
                        
                        if(bMergeNeeded){
                            outRed.println(""); // NOI18N
                            MergeAction.printMergeWarning(outRed, headRevList);
                        }
                        out.println(""); // NOI18N
                        out.close();
                        outRed.close();
                    }
                } catch (HgException ex) {
                    NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
                    DialogDisplayer.getDefault().notifyLater(e);
                }
            }
        };
        support.start(rp, root.getAbsolutePath(), org.openide.util.NbBundle.getMessage(LogAction.class, "MSG_Log_Progress")); // NOI18N

        //COMMENT: think this is redundent with Merge menu support added
        //if(bMergeNeeded){
        //    HgUtils.warningDialog(MergeAction.class,"MSG_MERGE_WARN_TITLE","MSG_MERGE_WARN_TEXT");   // NOI18N
        //}

    }
    
    private void addFiles(File[] files, FileStatusCache cache) {
        for (File file : files) {
            if (file.isDirectory()) {
                addFiles (file.listFiles(), cache);
            } else if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_VERSIONED) != 0){
                logFiles.add(file);
            }
        }
    }

    public boolean isEnabled() {
        // If it's a mercurial managed repository enable log action - Show History
        return HgRepositoryContextCache.hasHistory(context);
    } 
}    
