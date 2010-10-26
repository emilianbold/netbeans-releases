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

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.FileStatusCache;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.hooks.HgHook;
import org.netbeans.modules.versioning.hooks.HgHookContext;
import org.netbeans.modules.versioning.hooks.VCSHookContext;
import org.netbeans.modules.versioning.hooks.VCSHooks;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.common.VCSCommitOptions;
import org.netbeans.modules.versioning.util.common.VCSCommitPanel;
import org.netbeans.modules.versioning.util.common.VCSCommitParameters;
import org.netbeans.modules.versioning.util.common.VCSCommitParameters.DefaultCommitParameters;
import org.netbeans.modules.versioning.util.common.VCSCommitTable;
import org.netbeans.modules.versioning.util.common.VCSFileNode;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class GitCommitPanel extends VCSCommitPanel {

    public static GitCommitPanel create(final File[] roots, final File repository, VCSContext context) {
        
        Preferences preferences = GitModuleConfig.getDefault().getPreferences();
        String lastCanceledCommitMessage = GitModuleConfig.getDefault().getLastCanceledCommitMessage();
        DefaultCommitParameters parameters = new DefaultCommitParameters(preferences, lastCanceledCommitMessage);
        
        Collection<HgHook> hooks = VCSHooks.getInstance().getHooks(HgHook.class);
        HgHookContext hooksCtx = new HgHookContext(context.getRootFiles().toArray( new File[context.getRootFiles().size()]), null, new HgHookContext.LogEntry[] {});        
        
        DiffProvider diffProvider = new DiffProvider();
        
        return new GitCommitPanel(roots, repository, parameters, preferences, hooks, hooksCtx, diffProvider);
    }
    private final Collection<HgHook> hooks;
    private final File[] roots;
    private final File repository;

    private GitCommitPanel(final File[] roots, final File repository, VCSCommitParameters parameters, Preferences preferences, Collection<HgHook> hooks, VCSHookContext hooksContext, MultiDiffProvider diffProvider) {
        super(parameters, preferences, hooks, hooksContext, diffProvider);
        this.roots = roots;
        this.repository = repository;
        this.hooks = hooks;
    }

    @Override
    public DefaultCommitParameters getParameters() {
        return (DefaultCommitParameters) super.getParameters();
    }

    public Collection<HgHook> getHooks() {
        return hooks;
    }

    @Override
    protected void computeNodes() {
        RequestProcessor rp = Git.getInstance().getRequestProcessor(repository);
        final GitProgressSupport support = new GitProgressSupport( /*, cancel*/) {
            @Override
            public void perform() {
                try {
                    // Ensure that cache is uptodate
                    FileStatusCache cache = Git.getInstance().getFileStatusCache();
                    cache.refreshAllRoots(roots);

                    File[][] split = Utils.splitFlatOthers(roots);
                    List<File> fileList = new ArrayList<File>();
                    for (int c = 0; c < split.length; c++) {
                        File[] splitRoots = split[c];
                        boolean recursive = c == 1;
                        if (recursive) {
                            File[] files = cache.listFiles(splitRoots, FileInformation.STATUS_LOCAL_CHANGES);
                            for (int i = 0; i < files.length; i++) {
                                for(int r = 0; r < splitRoots.length; r++) {
                                    if(Utils.isAncestorOrEqual(splitRoots[r], files[i]))
                                    {
                                        if(!fileList.contains(files[i])) {
                                            fileList.add(files[i]);
                                        }
                                    }
                                }
                            }
                        } else {
                            File[] files = GitUtils.flatten(splitRoots, FileInformation.STATUS_LOCAL_CHANGES);
                            for (int i= 0; i<files.length; i++) {
                                if(!fileList.contains(files[i])) {
                                    fileList.add(files[i]);
                                }
                            }
                        }
                    }
                    if(fileList.isEmpty()) {
                        return;
                    }

                    ArrayList<VCSFileNode> nodesList = new ArrayList<VCSFileNode>(fileList.size());

                    for (Iterator<File> it = fileList.iterator(); it.hasNext();) {
                        File file = it.next();
                        VCSFileNode node = new GitFileNode(repository, file);
                        nodesList.add(node);
                    }
                    final VCSFileNode[] nodes = nodesList.toArray(new VCSFileNode[fileList.size()]);
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            getCommitTable().setNodes(nodes);
                        }
                    });
                } finally {
                    stopProgress();
                }
            }
        };
        String preparingMessage = NbBundle.getMessage(CommitAction.class, "Progress_Preparing_Commit");        
        startProgress(preparingMessage, support.getProgressComponent());
        support.start(rp, repository, preparingMessage);
    }
    
    @Override
    protected void commitTableChanged() {
        assert EventQueue.isDispatchThread();
        VCSCommitTable table = getCommitTable();
        Map<VCSFileNode, VCSCommitOptions> files = table.getCommitFiles();

        boolean enabled = true;
        for (VCSFileNode fileNode : files.keySet()) {
            VCSCommitOptions options = files.get(fileNode);
            if (options == VCSCommitOptions.EXCLUDE) {
                continue;
            }
            FileInformation info = (FileInformation) fileNode.getInformation();
            if (info.containsStatus(FileInformation.Status.VERSIONED_CONFLICT)) {
                enabled = false;
                String msg = NbBundle.getMessage(CommitAction.class, "MSG_CommitForm_ErrorConflicts"); // NOI18N
                setErrorLabel("<html><font color=\"#002080\">" + msg + "</font></html>");  // NOI18N                
            }
        }        
        enableCommitButton(enabled && table.containsCommitable());        
    }

    private static class DiffProvider extends VCSCommitPanel.MultiDiffProvider {
        
        @Override
        public Set<File> getModifiedFiles() {
            // XXX implement me
//            HashMap<File, SaveCookie> modifiedFiles = new HashMap<File, SaveCookie>();
//            for (Map.Entry<File, MultiDiffPanel> e : displayedDiffs.entrySet()) {
//                SaveCookie[] cookies = e.getValue().getSaveCookies(false);
//                if (cookies.length > 0) {
//                    modifiedFiles.put(e.getKey(), cookies[0]);
//                }
//            }
//            return modifiedFiles;
            return super.getModifiedFiles();
        }

        @Override
        public JComponent createDiffComponent(File file) {
            JPanel p = new JPanel();
            p.add(new JLabel("not yet implemented !!!"));
            return p; // XXX implement me new MultiDiffPanel(file, HgRevision.BASE, HgRevision.CURRENT, false); // switch the last parameter to true if editable diff works poorly
        }
        

        /**
         * Returns save cookies available for files in the commit table
         * @return
         */
        protected SaveCookie[] getSaveCookies() {
            return super.getSaveCookies(); // XXX getModifiedFiles().values().toArray(new SaveCookie[0]);
        }

        /**
         * Returns editor cookies available for modified and not open files in the commit table
         * @return
         */
        protected EditorCookie[] getEditorCookies() {
//            LinkedList<EditorCookie> allCookies = new LinkedList<EditorCookie>();
            // XXX      
//            for (Map.Entry<File, MultiDiffPanel> e : displayedDiffs.entrySet()) {
//                EditorCookie[] cookies = e.getValue().getEditorCookies(true);
//                if (cookies.length > 0) {
//                    allCookies.add(cookies[0]);
//                }
//            }
//            return allCookies.toArray(new EditorCookie[allCookies.size()]);
            return super.getEditorCookies();
        }

        
    }    
}
