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

package org.netbeans.modules.mercurial.remote.ui.queues;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.modules.mercurial.remote.FileInformation;
import org.netbeans.modules.mercurial.remote.FileStatusCache;
import org.netbeans.modules.mercurial.remote.HgException;
import org.netbeans.modules.mercurial.remote.HgModuleConfig;
import org.netbeans.modules.mercurial.remote.HgProgressSupport;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.config.HgConfigFiles;
import org.netbeans.modules.mercurial.remote.ui.diff.MultiDiffPanel;
import org.netbeans.modules.mercurial.remote.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.remote.ui.log.HgLogMessage.HgRevision;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.remotefs.versioning.hooks.HgQueueHook;
import org.netbeans.modules.remotefs.versioning.hooks.HgQueueHookContext;
import org.netbeans.modules.remotefs.versioning.hooks.VCSHookContext;
import org.netbeans.modules.remotefs.versioning.hooks.VCSHooks;
import org.netbeans.modules.remotefs.versioning.util.common.VCSCommitDiffProvider;
import org.netbeans.modules.remotefs.versioning.util.common.VCSCommitFilter;
import org.netbeans.modules.remotefs.versioning.util.common.VCSCommitPanel;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier;
import org.netbeans.modules.versioning.util.common.VCSCommitParameters.DefaultCommitParameters;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * 
 */
public class QCommitPanel extends VCSCommitPanel<QFileNode> {

    private final Collection<HgQueueHook> hooks;
    private final VCSFileProxy[] roots;
    private final VCSFileProxy repository;
    private final NodesProvider nodesProvider;
    private final HelpCtx helpCtx;

    private QCommitPanel(QCommitTable table, final VCSFileProxy[] roots, final VCSFileProxy repository, DefaultCommitParameters parameters, Preferences preferences, Collection<HgQueueHook> hooks, 
            VCSHookContext hooksContext, VCSCommitDiffProvider diffProvider, NodesProvider nodesProvider, HelpCtx helpCtx) {
        super(table, parameters, preferences, hooks, hooksContext, Collections.<VCSCommitFilter>emptyList(), diffProvider);
        this.roots = roots;
        this.repository = repository;
        this.hooks = hooks;
        this.nodesProvider = nodesProvider;
        this.helpCtx = helpCtx;
    }

    public static QCommitPanel createNewPanel (final VCSFileProxy[] roots, final VCSFileProxy repository, String commitMessage,
            final String helpCtxId) {
        final Preferences preferences = HgModuleConfig.getDefault(roots[0]).getPreferences();
        List<String> recentUsers = getRecentUsers(repository);
        final DefaultCommitParameters parameters = new QCreatePatchParameters(roots[0], preferences, commitMessage, null, recentUsers);
        final Collection<HgQueueHook> hooks = VCSHooks.getInstance().getHooks(HgQueueHook.class);
        
        return Mutex.EVENT.readAccess(new Mutex.Action<QCommitPanel>() {
            @Override
            public QCommitPanel run () {
                DiffProvider diffProvider = new DiffProvider();
                VCSCommitPanelModifier modifier = RefreshPanelModifier.getDefault("create"); //NOI18N
                HgQueueHookContext hooksCtx = new HgQueueHookContext(roots, null, null);
                return new QCommitPanel(new QCommitTable(modifier), roots, repository, parameters, preferences, hooks, hooksCtx, diffProvider, new ModifiedNodesProvider(),
                        new HelpCtx(helpCtxId));
            }
        });
    }

    public static QCommitPanel createRefreshPanel (final VCSFileProxy[] roots, final VCSFileProxy repository,
            String commitMessage, final QPatch patch, final HgRevision parentRevision, final String helpCtxId) {
        final Preferences preferences = HgModuleConfig.getDefault(roots[0]).getPreferences();
        List<String> recentUsers = getRecentUsers(repository);
        final DefaultCommitParameters parameters = new QCreatePatchParameters(roots[0], preferences, commitMessage, patch, recentUsers);
        final Collection<HgQueueHook> hooks = VCSHooks.getInstance().getHooks(HgQueueHook.class);
        
        return Mutex.EVENT.readAccess(new Mutex.Action<QCommitPanel>() {
            @Override
            public QCommitPanel run () {
                // own diff provider, displays qdiff instead of regular diff
                DiffProvider diffProvider = new QDiffProvider(parentRevision);
                VCSCommitPanelModifier msgProvider = RefreshPanelModifier.getDefault("refresh"); //NOI18N
                HgQueueHookContext hooksCtx = new HgQueueHookContext(roots, null, patch.getId());
                // own node computer, displays files not modified in cache but files returned by qdiff
                return new QCommitPanel(new QCommitTable(msgProvider), roots, repository, parameters, preferences, hooks, hooksCtx, diffProvider, new QRefreshNodesProvider(parentRevision),
                        new HelpCtx(helpCtxId));
                }
        });
    }
    
    private static List<String> getRecentUsers (VCSFileProxy repository) {
        HgConfigFiles config = new HgConfigFiles(repository);
        String userName = config.getUserName(false);
        if (userName.isEmpty()) {
            config = HgConfigFiles.getSysInstance(repository);
            userName = config.getUserName(false);
        }
        List<String> recentUsers = HgModuleConfig.getDefault(repository).getRecentCommitAuthors();
        if (!userName.isEmpty()) {
            recentUsers.remove(userName);
            recentUsers.add(0, userName);
        }
        return recentUsers;
    }
    
    @Override
    public QCreatePatchParameters getParameters() {
        return (QCreatePatchParameters) super.getParameters();
    }

    public Collection<HgQueueHook> getHooks() {
        return hooks;
    }

    @Override
    protected void computeNodes() {      
        computeNodesIntern();
    }
    
    HelpCtx getHelpContext () {
        return helpCtx;
    }

    @Override
    public boolean open (VCSContext context, HelpCtx helpCtx) {
        // synchronize access to this static field
        assert EventQueue.isDispatchThread();
        boolean ok = super.open(context, helpCtx);
        HgProgressSupport supp = support;
        if (supp != null) {
            supp.cancel();
        }
        return ok;
    }
    
    /** used by unit tests */
    HgProgressSupport support;
    RequestProcessor.Task computeNodesIntern() {      
        final boolean refreshFinnished[] = new boolean[] { false };
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);

        HgProgressSupport supp = this.support;
        if (supp != null) {
            supp.cancel();
        }
        support = getProgressSupport(refreshFinnished);
        final String preparingMessage = NbBundle.getMessage(QCommitPanel.class, "Progress_Preparing_Commit"); //NOI18N
        setupProgress(preparingMessage, support.getProgressComponent());
        Task task = support.start(rp, repository, preparingMessage);
        
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
        return task;
    }

    // merge-type commit dialog can hook into this method
    protected HgProgressSupport getProgressSupport (final boolean[] refreshFinished) {
        return new HgProgressSupport() {
            @Override
            public void perform() {
                try {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            getCommitTable().setNodes(new QFileNode[0]);
                        }
                    });
                    final QFileNode[] nodes = nodesProvider.getNodes(repository, roots, refreshFinished);
                    if (nodes != null) {
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                getCommitTable().setNodes(nodes);
                            }
                        });
                    }
                } finally {
                    refreshFinished[0] = true;
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            stopProgress();
                        }
                    });
                }
            }
        };
    }

    private static class DiffProvider extends VCSCommitDiffProvider {

        final Map<VCSFileProxy, MultiDiffPanel> panels = new HashMap<>();

        @Override
        public Set<VCSFileProxy> getModifiedFiles () {
            return getSaveCookiesPerFile().keySet();
        }

        private Map<VCSFileProxy, SaveCookie> getSaveCookiesPerFile () {
            Map<VCSFileProxy, SaveCookie> modifiedFiles = new HashMap<>();
            for (Map.Entry<VCSFileProxy, MultiDiffPanel> e : panels.entrySet()) {
                SaveCookie[] cookies = e.getValue().getSaveCookies(false);
                if (cookies.length > 0) {
                    modifiedFiles.put(e.getKey(), cookies[0]);
                }
            }
            return modifiedFiles;
        }

        @Override
        public JComponent createDiffComponent (VCSFileProxy file) {
            MultiDiffPanel panel = new MultiDiffPanel(file, HgRevision.BASE, HgRevision.CURRENT, false);
            panels.put(file, panel);
            return panel;
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
            LinkedList<EditorCookie> allCookies = new LinkedList<>();
            for (Map.Entry<VCSFileProxy, MultiDiffPanel> e : panels.entrySet()) {
                EditorCookie[] cookies = e.getValue().getEditorCookies(true);
                if (cookies.length > 0) {
                    allCookies.add(cookies[0]);
                }
            }
            return allCookies.toArray(new EditorCookie[allCookies.size()]);
        }        
    }
    
    private static class QDiffProvider extends DiffProvider {
        private final HgRevision parent;
        
        QDiffProvider (HgRevision parent) {
            this.parent = parent;
        }
        
        @Override
        public JComponent createDiffComponent (VCSFileProxy file) {
            MultiDiffPanel panel = new MultiDiffPanel(file, parent, HgRevision.CURRENT, false);
            panels.put(file, panel);
            return panel;
        }
    }
    
    private static interface NodesProvider {
        QFileNode[] getNodes (VCSFileProxy repository, VCSFileProxy[] roots, boolean[] refreshFinished);
    }
    
    /**
     * Used in qnew panel, provides files modified in cache
     */
    private static final class ModifiedNodesProvider implements NodesProvider {

        @Override
        public QFileNode[] getNodes (VCSFileProxy repository, VCSFileProxy[] roots, boolean[] refreshFinished) {
            // Ensure that cache is uptodate
            FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
            cache.refreshAllRoots(Collections.<VCSFileProxy, Set<VCSFileProxy>>singletonMap(repository, new HashSet<>(Arrays.asList(roots))));
            // the realy time consuming part is over;
            // no need to show the progress component,
            // which only makes the dialog flicker
            refreshFinished[0] = true;
            VCSFileProxy[][] split = VCSFileProxySupport.splitFlatOthers(roots);
            List<VCSFileProxy> fileList = new ArrayList<>();
            for (int c = 0; c < split.length; c++) {
                VCSFileProxy[] splitRoots = split[c];
                boolean recursive = c == 1;
                if (recursive) {
                    VCSFileProxy[] files = cache.listFiles(splitRoots, FileInformation.STATUS_LOCAL_CHANGE);
                    for (int i = 0; i < files.length; i++) {
                        for(int r = 0; r < splitRoots.length; r++) {
                            if(VCSFileProxySupport.isAncestorOrEqual(splitRoots[r], files[i]))
                            {
                                if(!fileList.contains(files[i])) {
                                    fileList.add(files[i]);
                                }
                            }
                        }
                    }
                } else {
                    VCSFileProxy[] files = HgUtils.flatten(splitRoots, FileInformation.STATUS_LOCAL_CHANGE);
                    for (int i= 0; i<files.length; i++) {
                        if(!fileList.contains(files[i])) {
                            fileList.add(files[i]);
                        }
                    }
                }
            }
            if(fileList.isEmpty()) {
                return null;
            }

            ArrayList<QFileNode> nodesList = new ArrayList<>(fileList.size());

            for (Iterator<VCSFileProxy> it = fileList.iterator(); it.hasNext();) {
                VCSFileProxy file = it.next();
                QFileNode node = new QFileNode(repository, file);
                nodesList.add(node);
            }
            return nodesList.toArray(new QFileNode[fileList.size()]);
        }
        
    }

    /**
     * Used in qrefresh panel, provides also files that are already part of a patch
     * So it displays:
     * - all local modifications against the WC parent (qtip)
     * - plus all files already in the patch, i.e. difference between qtip and parent revision
     */
    private static final class QRefreshNodesProvider implements NodesProvider {
        private final HgRevision parent;

        private QRefreshNodesProvider (HgRevision parentRevision) {
            this.parent = parentRevision;
        }

        @Override
        public QFileNode[] getNodes (VCSFileProxy repository, VCSFileProxy[] roots, boolean[] refreshFinished) {
            try {
                if (parent != null && parent != HgLogMessage.HgRevision.EMPTY) {
                    Map<VCSFileProxy, FileInformation> patchChanges = HgCommand.getStatus(repository, Collections.singletonList(repository), parent.getRevisionNumber(), QPatch.TAG_QTIP);
                    FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
                    Set<VCSFileProxy> toRefresh = new HashSet<>(Arrays.asList(roots));
                    toRefresh.addAll(patchChanges.keySet());
                    cache.refreshAllRoots(Collections.<VCSFileProxy, Set<VCSFileProxy>>singletonMap(repository, toRefresh));
                    
                    Map<VCSFileProxy, FileInformation> statuses = getLocalChanges(roots, cache);
                    statuses.keySet().retainAll(VCSFileProxySupport.flattenFiles(roots, statuses.keySet()));
                    Set<VCSFileProxy> patchChangesUnderSelection = getPatchChangesUnderSelection(patchChanges, roots);
                    
                    for (Map.Entry<VCSFileProxy, FileInformation> e : patchChanges.entrySet()) {
                        if (patchChangesUnderSelection.contains(e.getKey())) {
                            if (!statuses.containsKey(e.getKey())) {
                                statuses.put(e.getKey(), new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, null, false));
                            }
                        } else {
                            FileInformation info = cache.getCachedStatus(e.getKey());
                            if ((info.getStatus() & FileInformation.STATUS_LOCAL_CHANGE) != 0) {
                                statuses.put(e.getKey(), info);
                            }
                        }
                    }

                    refreshFinished[0] = true;

                    if(statuses.isEmpty()) {
                        return null;
                    }

                    ArrayList<QFileNode> nodesList = new ArrayList<>(statuses.size());
                    for (Map.Entry<VCSFileProxy, FileInformation> e : statuses.entrySet()) {
                        VCSFileProxy f = e.getKey();
                        FileInformation fi = e.getValue();
                        if ((fi.getStatus() & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) != 0 && HgUtils.isIgnored(f)) {
                            // do not include not sharable files
                            continue;
                        }
                        QFileNode node = new QFileNode(repository, f, fi);
                        nodesList.add(node);
                    }
                    return nodesList.toArray(new QFileNode[nodesList.size()]);
                }
            } catch (HgException.HgCommandCanceledException ex) {
                //
            } catch (HgException ex) {
                Mercurial.LOG.log(Level.INFO, null, ex);
            }
            return null;
        }

        // should contain only patch changes that apply to current selection
        private Set<VCSFileProxy> getPatchChangesUnderSelection(Map<VCSFileProxy, FileInformation> patchChanges, VCSFileProxy[] roots) {
            Set<VCSFileProxy> patchChangesUnderSelection = new HashSet<>(patchChanges.keySet());
            for (Iterator<VCSFileProxy> it = patchChangesUnderSelection.iterator(); it.hasNext(); ) {
                VCSFileProxy f = it.next();
                boolean isUnderRoots = false;
                for (VCSFileProxy root : roots) {
                    if (VCSFileProxySupport.isAncestorOrEqual(root, f)) {
                        isUnderRoots = true;
                        break;
                    }
                }
                if (!isUnderRoots) {
                    it.remove();
                }
            }
            patchChangesUnderSelection = VCSFileProxySupport.flattenFiles(roots, patchChangesUnderSelection);
            return patchChangesUnderSelection;
        }

        private Map<VCSFileProxy, FileInformation> getLocalChanges (VCSFileProxy[] roots, FileStatusCache cache) {
            VCSFileProxy[] files = cache.listFiles(roots, FileInformation.STATUS_LOCAL_CHANGE);
            Map<VCSFileProxy, FileInformation> retval = new HashMap<>(files.length);
            for (VCSFileProxy file : files) {
                retval.put(file, cache.getCachedStatus(file));
            }
            return retval;
        }
        
    }
}
