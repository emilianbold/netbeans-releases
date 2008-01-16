/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        final File root = HgUtils.getRootFile(ctx);
        if (root == null) return;
        
        File [] files = ctx.getRootFiles().toArray(new File[ctx.getRootFiles().size()]);
        if (files == null || files.length == 0) return;
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
        
        for (File file: files) {
            logFiles.add(file);
        }
                
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root.getAbsolutePath());        
        HgProgressSupport support = new HgProgressSupport() {
            public void perform() {
                Mercurial hg = Mercurial.getInstance();
                try {                    
                    List<String> headRevList = HgCommand.getHeadRevisions(root);
                    if(headRevList == null) return;
                    bMergeNeeded = headRevList.size() > 1;
                    
                    List<String> list = new LinkedList<String>();
                    list = HgCommand.doLog(root, logFiles);
                    
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
                        outRed.println(NbBundle.getMessage(LogAction.class, "MSG_Log_Files", // NOI18N
                                logFiles));
                        
                        outRed.println(NbBundle.getMessage(LogAction.class, "MSG_Log_PrjName", // NOI18N
                                prjName));
                        outRed.println(NbBundle.getMessage(LogAction.class, "MSG_Log_PrjPath", // NOI18N
                                prjFile.getAbsolutePath()));
                        String pullPath = HgCommand.getPullDefault(root);
                        if(pullPath != null){
                            outRed.println(NbBundle.getMessage(LogAction.class, "MSG_Log_Pull_Path",pullPath)); // NOI18N
                        }
                        String pushPath = HgCommand.getPushDefault(root);
                        if(pushPath != null){
                            outRed.println(NbBundle.getMessage(LogAction.class, "MSG_Log_Push_Path",pushPath)); // NOI18N
                        }
                        
                        if(bMergeNeeded){
                            outRed.println(""); // NOI18N
                            MergeAction.printMergeWarning(outRed, headRevList);
                        }
                        outRed.println(NbBundle.getMessage(LogAction.class, "MSG_Log_DONE")); // NOI18N
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
    
    public boolean isEnabled() {
        if(!Mercurial.getInstance().isGoodVersion()) return false;
        // If it's a mercurial managed repository enable log action - Show History
        return HgRepositoryContextCache.hasHistory(context);
    } 
}    
