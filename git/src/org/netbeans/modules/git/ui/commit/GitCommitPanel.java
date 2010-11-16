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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import org.netbeans.libs.git.GitUser;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.FileStatusCache;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.diff.MultiDiffPanelController;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.hooks.GitHook;
import org.netbeans.modules.versioning.hooks.GitHookContext;
import org.netbeans.modules.versioning.hooks.VCSHookContext;
import org.netbeans.modules.versioning.hooks.VCSHooks;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.common.VCSCommitDiffProvider;
import org.netbeans.modules.versioning.util.common.VCSCommitFilter;
import org.netbeans.modules.versioning.util.common.VCSCommitOptions;
import org.netbeans.modules.versioning.util.common.VCSCommitPanel;
import org.netbeans.modules.versioning.util.common.VCSCommitParameters.DefaultCommitParameters;
import org.netbeans.modules.versioning.util.common.VCSCommitTable;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class GitCommitPanel extends VCSCommitPanel<GitFileNode> {

    static final GitCommitFilter FILTER_HEAD_VS_WORKING = new GitCommitFilter(
                "HEAD_VS_WORKING", 
                new ImageIcon(GitCommitPanel.class.getResource("/org/netbeans/modules/git/resources/icons/head_vs_working.png")),
                NbBundle.getMessage(GitCommitPanel.class, "ParametersPanel.tgbHeadVsWorking.toolTipText"),
                true); 
    static final GitCommitFilter FILTER_HEAD_VS_INDEX = new GitCommitFilter(
                "HEAD_VS_INDEX", 
                new ImageIcon(GitCommitPanel.class.getResource("/org/netbeans/modules/git/resources/icons/head_vs_index.png")),
                NbBundle.getMessage(GitCommitPanel.class, "ParametersPanel.tgbHeadVsIndex.toolTipText"),
                false);
    
    private final Collection<GitHook> hooks;
    private final File[] roots;
    private final File repository;

    private GitCommitPanel(GitCommitTable table, final File[] roots, final File repository, DefaultCommitParameters parameters, Preferences preferences, Collection<GitHook> hooks, VCSHookContext hooksContext, VCSCommitDiffProvider diffProvider) {
        super(table, parameters, preferences, hooks, hooksContext, createFilters(), diffProvider);
        this.roots = roots;
        this.repository = repository;
        this.hooks = hooks;        
    }

    public static GitCommitPanel create(final File[] roots, final File repository, GitUser user) {
        
        Preferences preferences = GitModuleConfig.getDefault().getPreferences();
        String lastCanceledCommitMessage = GitModuleConfig.getDefault().getLastCanceledCommitMessage();
        
        DefaultCommitParameters parameters = new GitCommitParameters(preferences, lastCanceledCommitMessage, user);
        
        Collection<GitHook> hooks = VCSHooks.getInstance().getHooks(GitHook.class);
        GitHookContext hooksCtx = new GitHookContext(roots, null, new GitHookContext.LogEntry[] {});        
        
        DiffProvider diffProvider = new DiffProvider();
        
        return new GitCommitPanel(new GitCommitTable(), roots, repository, parameters, preferences, hooks, hooksCtx, diffProvider);
    }
    
    private static List<VCSCommitFilter> createFilters() {
        List<VCSCommitFilter> filters = new LinkedList<VCSCommitFilter>();
        filters.add(FILTER_HEAD_VS_WORKING);            
        filters.add(FILTER_HEAD_VS_INDEX);
        return filters;
    }
    
    @Override
    public GitCommitParameters getParameters() {
        return (GitCommitParameters) super.getParameters();
    }

    public Collection<GitHook> getHooks() {
        return hooks;
    }

    @Override
    protected void computeNodes() {      
        final boolean refreshFinnished[] = new boolean[] { false };
        RequestProcessor rp = Git.getInstance().getRequestProcessor(repository);
        final GitProgressSupport support = new GitProgressSupport( /*, cancel*/) {
            @Override
            public void perform() {
                try {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            getCommitTable().setNodes(new GitFileNode[0]);                            
                        }
                    });
                    
                    // Ensure that cache is uptodate
                    FileStatusCache cache = Git.getInstance().getFileStatusCache();
                    cache.refreshAllRoots(roots);

                    // the realy time consuming part is over; 
                    // no need to show the progress component, 
                    // which only makes the dialog flicker
                    refreshFinnished[0] = true;
                    
                    File[][] split = Utils.splitFlatOthers(roots);
                    List<File> fileList = new ArrayList<File>();
                    for (int c = 0; c < split.length; c++) {
                        File[] splitRoots = split[c];
                        boolean recursive = c == 1;
                        if (recursive) {
                            File[] files = cache.listFiles(splitRoots, getAcceptedStatus());
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
                            File[] files = GitUtils.flatten(splitRoots, getAcceptedStatus());
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

                    ArrayList<GitFileNode> nodesList = new ArrayList<GitFileNode>(fileList.size());

                    for (Iterator<File> it = fileList.iterator(); it.hasNext();) {
                        File file = it.next();
                        GitFileNode node = new GitFileNode(repository, file);
                        nodesList.add(node);
                    }
                    final GitFileNode[] nodes = nodesList.toArray(new GitFileNode[fileList.size()]);
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            getCommitTable().setNodes(nodes);
                        }
                    });
                } finally {
                    refreshFinnished[0] = true;
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            stopProgress();
                        }
                    });         
                }
            }
        };
        final String preparingMessage = NbBundle.getMessage(CommitAction.class, "Progress_Preparing_Commit");        
        setupProgress(preparingMessage, support.getProgressComponent());
        support.start(rp, repository, preparingMessage);
        
        // do not show progress in dialog if task finnished early        
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!(refreshFinnished[0])) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            showProgress();                            
                        }
                    });                     
                }
            }
        }, 1000);
    }
    
    EnumSet<Status> getAcceptedStatus() {
        VCSCommitFilter f = getSelectedFilter();
        if(f == FILTER_HEAD_VS_INDEX) {
            return FileInformation.STATUS_MODIFIED_HEAD_VS_INDEX;
        } else if(f == FILTER_HEAD_VS_WORKING) {
            return FileInformation.STATUS_MODIFIED_HEAD_VS_WORKING;                
        }         
        throw new IllegalStateException("wrong filter " + (f != null ? f.getID() : "NULL"));    // NOI18N        
    }

    private static class DiffProvider extends VCSCommitDiffProvider {

        private final Map<File, MultiDiffPanelController> controllers = new HashMap<File, MultiDiffPanelController>();

        @Override
        public Set<File> getModifiedFiles () {
            return getSaveCookiesPerFile().keySet();
        }

        private Map<File, SaveCookie> getSaveCookiesPerFile () {
            Map<File, SaveCookie> modifiedFiles = new HashMap<File, SaveCookie>();
            for (Map.Entry<File, MultiDiffPanelController> e : controllers.entrySet()) {
                SaveCookie[] cookies = e.getValue().getSaveCookies(false);
                if (cookies.length > 0) {
                    modifiedFiles.put(e.getKey(), cookies[0]);
                }
            }
            return modifiedFiles;
        }

        @Override
        public JComponent createDiffComponent (File file) {
            MultiDiffPanelController controller = new MultiDiffPanelController(file);
            controllers.put(file, controller);
            return controller.getPanel();
        }
        

        /**
         * Returns save cookies available for files in the commit table
         * @return
         */
        @Override
        protected SaveCookie[] getSaveCookies () {
            return getSaveCookiesPerFile().values().toArray(new SaveCookie[0]);
        }

        /**
         * Returns editor cookies available for modified and not open files in the commit table
         * @return
         */
        @Override
        protected EditorCookie[] getEditorCookies () {
            LinkedList<EditorCookie> allCookies = new LinkedList<EditorCookie>();
            for (Map.Entry<File, MultiDiffPanelController> e : controllers.entrySet()) {
                EditorCookie[] cookies = e.getValue().getEditorCookies(true);
                if (cookies.length > 0) {
                    allCookies.add(cookies[0]);
                }
            }
            return allCookies.toArray(new EditorCookie[allCookies.size()]);
        }        
    }    
    
    private static class GitCommitFilter extends VCSCommitFilter {
        private final Icon icon;
        private final String tooltip;
        private final String id;

        GitCommitFilter(String id, Icon icon, String tooltip, boolean selected) {
            super(selected);
            this.icon = icon;
            this.tooltip = tooltip;
            this.id = id;
        }
        
        @Override
        public Icon getIcon() {
            return icon;
        }

        @Override
        public String getTooltip() {
            return tooltip;
        }

        @Override
        public String getID() {
            return id;
        }
        
    }
}
