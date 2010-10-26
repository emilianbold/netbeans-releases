/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.ui.commit;

import java.util.Set;
import org.netbeans.modules.versioning.util.common.VCSCommitOptions;
import org.netbeans.modules.versioning.util.common.VCSCommitTable;
import org.netbeans.modules.versioning.util.common.VCSFileNode;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.versioning.hooks.HgHook;
import org.netbeans.modules.versioning.hooks.HgHookContext;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class CommitAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(CommitAction.class.getName());

    @Override
    protected void performAction (File repository, File[] roots, VCSContext context) {

        final GitCommitPanel panel = GitCommitPanel.create(roots, repository, context);
        VCSCommitTable table = panel.getCommitTable();
        boolean ok = panel.open(context, new HelpCtx(CommitAction.class));
        
        if (ok) {

            final Map<VCSFileNode, VCSCommitOptions> commitFiles = table.getCommitFiles();
            
            GitModuleConfig.getDefault().setLastCanceledCommitMessage(""); //NOI18N            
            panel.getParameters().storeCommitMessage();
            
            RequestProcessor rp = Git.getInstance().getRequestProcessor(repository);
            GitProgressSupport support = new GitProgressSupport() {
                @Override
                public void perform() {
                    GitClient client;
                    try {
                        client = getClient();
                        performCommit(panel.getParameters().getCommitMessage(), commitFiles, client, this, panel.getHooks());
                    } catch (GitException ex) {
                        LOG.log(Level.WARNING, null, ex);
                        return;
                    }
                }
            };
            support.start(rp, repository, org.openide.util.NbBundle.getMessage(CommitAction.class, "LBL_Commit_Progress")); // NOI18N

        }
    }

    private static void performCommit(
            String message, 
            Map<VCSFileNode, VCSCommitOptions> commitFiles,
            GitClient client, 
            GitProgressSupport support, 
            Collection<HgHook> hooks) 
    {
       
        List<File> addCandidates = new LinkedList<File>();
        List<File> deleteCandidates = new LinkedList<File>();
        List<File> commitCandidates = new LinkedList<File>();
        Set<File> filesToRefresh = new HashSet<File>();
        
        populateCandidates(
                commitFiles, 
                addCandidates, 
                deleteCandidates,
                commitCandidates,
                support);
        
        if (support.isCanceled()) {
            return;
        }

        try {
            support.outputInRed(
                    NbBundle.getMessage(CommitAction.class,
                    "MSG_COMMIT_TITLE")); // NOI18N
            support.outputInRed(
                    NbBundle.getMessage(CommitAction.class,
                    "MSG_COMMIT_TITLE_SEP")); // NOI18N
            support.output(message); // NOI18N

            client.add(addCandidates.toArray(new File[addCandidates.size()]), support);
            client.remove(deleteCandidates.toArray(new File[deleteCandidates.size()]), false, support);
            
            beforeCommitHook(commitCandidates, hooks, message);
            
            commit(commitCandidates, client, message, support);
            
        } /*catch (GitException.HgCommandCanceledException ex) {
            // XXX
            // canceled by user, do nothing
        } */catch (GitException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        } finally {
            refreshFS(filesToRefresh);
            Git.getInstance().getFileStatusCache().refreshAllRoots(commitCandidates);
            support.outputInRed(NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_DONE")); // NOI18N
            support.output(""); // NOI18N
        }
    }

    private static void populateCandidates(
            Map<VCSFileNode, VCSCommitOptions> commitFiles, 
            List<File> addCandidates,
            List<File> deleteCandidates,
            List<File> commitCandidates,
            GitProgressSupport support) 
    {
        List<String> excPaths = new ArrayList<String>();        
        List<String> incPaths = new ArrayList<String>();
        
        Iterator<VCSFileNode> it = commitFiles.keySet().iterator();
        while (it.hasNext()) {
            if (support.isCanceled()) {
                return;
            }
            GitFileNode node = (GitFileNode) it.next();
            FileInformation info = node.getInformation();
             
            VCSCommitOptions option = commitFiles.get(node);
            File file = node.getFile();
            if (option != VCSCommitOptions.EXCLUDE) {
                if (info.containsStatus(Status.NOTVERSIONED_NEW_IN_WORKING_TREE)) {
                    addCandidates.add(file);
                } else  if (info.containsStatus(FileInformation.STATUS_REMOVED)) {
                    deleteCandidates.add(file);
                }
                commitCandidates.add(file);
                incPaths.add(file.getAbsolutePath());
            } else {
                excPaths.add(file.getAbsolutePath());                
            }
        }
        
        if (!excPaths.isEmpty()) {
            GitModuleConfig.getDefault().addExclusionPaths(excPaths);
        }
        if (!incPaths.isEmpty()) {
            GitModuleConfig.getDefault().removeExclusionPaths(incPaths);
        }        
    }


    private static void beforeCommitHook(List<File> commitCandidates, Collection<HgHook> hooks, String message) {
        File[] hookFiles = null;
        if(hooks.size() > 0) {                
            hookFiles = commitCandidates.toArray(new File[commitCandidates.size()]);
        }
        HgHookContext context = new HgHookContext(hookFiles, message, new HgHookContext.LogEntry[] {});
        for (HgHook hook : hooks) {
            try {
                // XXX handle returned context
                context = hook.beforeCommit(context);
                if(context != null) {
                    message = context.getMessage();
                }
            } catch (IOException ex) {
                // XXX handle veto
            }
        }
    }
      
    private static void commit(List<File> commitCandidates, GitClient client, String message, GitProgressSupport support) throws GitException {
        boolean commitAfterMerge = false;        
        try {                                        
            client.commit(commitCandidates.toArray(new File[commitCandidates.size()]), message, support);                    
        } catch (GitException ex) {
            // XXX
//                    if (HgCommand.COMMIT_AFTER_MERGE.equals(ex.getMessage())) {
//                        // committing after a merge, all modified files have to be committed, even excluded files
//                        // ask the user for confirmation
//                        if (support.isCanceled()) {
//                            return;
//                        } else if(!commitAfterMerge(Boolean.TRUE.equals(locallyModifiedExcluded.get(repository)), repository)) {
//                            return;
//                        } else {
//                            HgCommand.doCommit(repository, Collections.EMPTY_LIST, msg, logger);
//                            refreshFiles = new HashSet<File>(Mercurial.getInstance().getSeenRoots(repository));
//                            commitAfterMerge = true;
//                        }
//                    } else {
//                        throw ex;
//                    }
            throw ex;
        } 
        
        // XXX
//                HgLogMessage tip = HgCommand.doTip(repository, logger);
//
//                context = new HgHookContext(hookFiles, msg, new HgHookContext.LogEntry(
//                        tip.getMessage(),
//                        tip.getAuthor(),
//                        tip.getCSetShortID(),
//                        tip.getDate()));
//                for (HgHook hook : hooks) {
//                    hook.afterCommit(context);
//                }

        if (commitAfterMerge) {
            support.getLogger().output(
                    NbBundle.getMessage(CommitAction.class,
                    "MSG_COMMITED_FILES_AFTER_MERGE"));         //NOI18N
        } else {
            if (commitCandidates.size() == 1) {
                support.getLogger().output(
                        NbBundle.getMessage(CommitAction.class,
                        "MSG_COMMIT_INIT_SEP_ONE", commitCandidates.size())); //NOI18N
            } else {
                support.getLogger().output(
                        NbBundle.getMessage(CommitAction.class,
                        "MSG_COMMIT_INIT_SEP", commitCandidates.size())); //NOI18N
            }
            for (File f : commitCandidates) {
                support.getLogger().output("\t" + f.getAbsolutePath());      //NOI18N
            }
        }
        // XXX HgUtils.logHgLog(tip, logger);
    }
        
    private static void refreshFS (final Set<File> filesToRefresh) {
        Git.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                FileUtil.refreshFor(filesToRefresh.toArray(new File[filesToRefresh.size()]));
            }
        }, 100);
    }    
    
}
