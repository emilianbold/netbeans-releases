/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.subversion.remote.ui.history;

import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;
import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.*;
import java.awt.Component;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.api.diff.DiffController;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.SvnProgressSupport;
import org.netbeans.modules.subversion.remote.ui.diff.DiffSetupSource;
import org.netbeans.modules.subversion.remote.ui.diff.Setup;
import org.netbeans.modules.subversion.remote.ui.history.RepositoryRevision.Event;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.openide.filesystems.FileSystem;
import org.openide.util.Cancellable;
import org.openide.util.WeakListeners;

/**
 * Shows Search History results in a table with Diff pane below it.
 * 
 * 
 */
class DiffResultsView implements AncestorListener, PropertyChangeListener, DiffSetupSource {

    protected final SearchHistoryPanel parent;

    protected DiffTreeTable treeView;
    private final JSplitPane    diffView;
    
    protected SvnProgressSupport        currentTask;
    private RequestProcessor.Task       currentShowDiffTask;
    
    protected DiffController            currentDiff;
    private int                         currentDifferenceIndex;
    protected int                       currentIndex;
    private boolean                     dividerSet;
    protected List<RepositoryRevision>  results;
    private static final RequestProcessor rp = new RequestProcessor("SubversionDiff", 1, true);  // NOI18N
    private final PropertyChangeListener list;
    private Node[] selectedNodes;
    private final Set<RepositoryRevision> revisionsToRefresh = new HashSet<>(2);
    private int lastDividerLoc;

    public DiffResultsView(SearchHistoryPanel parent, List<RepositoryRevision> results) {
        this.parent = parent;
        this.results = results;
        treeView = new DiffTreeTable(parent);
        treeView.setResults(results);
        treeView.addAncestorListener(this);

        diffView = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        diffView.setTopComponent(treeView);
        setBottomComponent(new NoContentPanel(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_NoRevisions"))); // NOI18N
        list = WeakListeners.propertyChange(this, null);
    }

    @Override
    public void ancestorAdded(AncestorEvent event) {
        ExplorerManager em = ExplorerManager.find(treeView);
        em.addPropertyChangeListener(this);
        if (dividerSet) {
            if (lastDividerLoc != 0) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        diffView.setDividerLocation(lastDividerLoc);
                    }
                });
            }
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    dividerSet = true;
                    diffView.setDividerLocation(0.33);
                }
            });
        }
    }

    @Override
    public void ancestorMoved(AncestorEvent event) {
    }

    @Override
    public void ancestorRemoved(AncestorEvent event) {
        if (dividerSet) {
            lastDividerLoc = diffView.getDividerLocation();
        }
        ExplorerManager em = ExplorerManager.find(treeView);
        em.removePropertyChangeListener(this);
        cancelBackgroundTasks();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            assert EventQueue.isDispatchThread();
            selectedNodes = (Node[]) evt.getNewValue();
            currentDifferenceIndex = 0;
            if (selectedNodes.length == 0) {
                showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_NoRevisions")); // NOI18N
                parent.refreshComponents(false);
                return;
            }
            else if (selectedNodes.length > 2) {
                showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_TooManyRevisions")); // NOI18N
                parent.refreshComponents(false);
                return;
            }
            revisionsToRefresh.clear();

            // invoked asynchronously becase treeView.getSelection() may not be ready yet
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    showDiff();
                }
            };
            EventQueue.invokeLater(runnable);
        } else if (RepositoryRevision.PROP_EVENTS_CHANGED.equals(evt.getPropertyName())) {
            if (evt.getSource() instanceof RepositoryRevision) {
                RepositoryRevision revision = (RepositoryRevision) evt.getSource();
                revision.removePropertyChangeListener(RepositoryRevision.PROP_EVENTS_CHANGED, list);
                if (revisionsToRefresh.contains(revision) && selectedNodes != null && selectedNodes.length > 0) {
                    showDiff();
                }
            }
        }
    }
    
    private void showDiff () {
        RepositoryRevision container1 = selectedNodes[0].getLookup().lookup(RepositoryRevision.class);
        RepositoryRevision.Event r1 = selectedNodes[0].getLookup().lookup(RepositoryRevision.Event.class);
        boolean error = false;
        boolean loading = false;
        try {
            currentIndex = treeView.getSelection()[0];
            if (selectedNodes.length == 1) {
                if (container1 != null) {
                    container1.removePropertyChangeListener(RepositoryRevision.PROP_EVENTS_CHANGED, list);
                    container1.addPropertyChangeListener(RepositoryRevision.PROP_EVENTS_CHANGED, list);
                    if (container1.expandEvents()) {
                        revisionsToRefresh.add(container1);
                        loading = true;
                    } else {
                        container1.removePropertyChangeListener(RepositoryRevision.PROP_EVENTS_CHANGED, list);
                    }
                    if (showContainerDiff(container1, onSelectionshowLastDifference)) {
                        loading = false;
                    }
                } else if (r1 != null) {
                    showRevisionDiff(r1, onSelectionshowLastDifference);
                }
            } else if (selectedNodes.length == 2) {
                RepositoryRevision.Event revOlder = null;
                if (container1 != null) {
                    /**
                        * both repository revision events must be acquired from a container, not through a Lookup as before,
                        * since only two containers (and no rev-event) are present in the lookup
                        */
                    RepositoryRevision container2 = selectedNodes[1].getLookup().lookup(RepositoryRevision.class);
                    if (container2 == null) {
                        error = true;
                    } else {
                        container1.removePropertyChangeListener(RepositoryRevision.PROP_EVENTS_CHANGED, list);
                        container1.addPropertyChangeListener(RepositoryRevision.PROP_EVENTS_CHANGED, list);
                        container2.removePropertyChangeListener(RepositoryRevision.PROP_EVENTS_CHANGED, list);
                        container2.addPropertyChangeListener(RepositoryRevision.PROP_EVENTS_CHANGED, list);
                        if (container1.expandEvents() || container2.expandEvents()) {
                            loading = true;
                            revisionsToRefresh.add(container1);
                            revisionsToRefresh.add(container2);
                        } else {
                            container1.removePropertyChangeListener(RepositoryRevision.PROP_EVENTS_CHANGED, list);
                            container2.removePropertyChangeListener(RepositoryRevision.PROP_EVENTS_CHANGED, list);
                        }
                        r1 = getEventForRoots(container1, null);
                        revOlder = getEventForRoots(container2, r1 == null ? null : r1.getFile());
                        if (r1 != null && revOlder != null) {
                            loading = false;
                        }
                    }
                } else {
                    revOlder = selectedNodes[1].getLookup().lookup(RepositoryRevision.Event.class);
                }
                if (r1 == null || revOlder == null || revOlder.getFile() == null) {
                    error = true;
                } else {
                    showDiff(r1.getLogInfoHeader().getRepositoryRootUrl(), revOlder, r1, false);
                }
            }
        } catch (Exception e) {
            error = true;
        }
        
        if (loading) {
            showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_LoadingDiff")); //NOI18N
            parent.refreshComponents(false);
        } else if (error) {
            showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_IllegalSelection")); // NOI18N
            parent.refreshComponents(false);
        } else {
            revisionsToRefresh.clear();
        }
    }

    @Override
    public Collection<Setup> getSetups() {
        Node [] nodes = TopComponent.getRegistry().getActivatedNodes();
        if (nodes.length == 0) {
            return parent.getSetups(results.toArray(new RepositoryRevision[results.size()]), new RepositoryRevision.Event[0]);
        }
        
        Set<RepositoryRevision.Event> events = new HashSet<>();
        Set<RepositoryRevision> revisions = new HashSet<>();
        for (Node n : nodes) {
            RevisionNode node = (RevisionNode) n;
            if (node.getEvent() != null) {
                events.add(node.getEvent());
            } else {
                revisions.add(node.getContainer());
            }
        }
        return parent.getSetups(revisions.toArray(new RepositoryRevision[revisions.size()]), events.toArray(new RepositoryRevision.Event[events.size()]));
    }

    @Override
    public String getSetupDisplayName() {
        return null;
    }

    protected void showDiffError (final String s) {
        Runnable inAWT = new Runnable() {
            @Override
            public void run() {
                setBottomComponent(new NoContentPanel(s));
            }
        };
        if (EventQueue.isDispatchThread()) {
            inAWT.run();
        } else {
            EventQueue.invokeLater(inAWT);
        }
    }

    protected SvnProgressSupport createShowDiffTask(Event revision1, Event revision2, boolean showLastDifference) {
        return new ShowDiffTask(parent.getFileSystem(), revision1, revision2, showLastDifference);
    }

    protected final void setBottomComponent(Component component) {
        assert EventQueue.isDispatchThread();
        int dl = diffView.getDividerLocation();
        diffView.setBottomComponent(component);
        diffView.setDividerLocation(dl);
    }

    protected void showDiff (SVNUrl repositoryRootUrl, RepositoryRevision.Event revision1, RepositoryRevision.Event revision2, boolean showLastDifference) {
        synchronized(this) {
            cancelBackgroundTasks();
            currentTask = createShowDiffTask(revision1, revision2, showLastDifference);
            currentTask.start(rp, repositoryRootUrl, NbBundle.getMessage(DiffResultsView.class, "LBL_SearchHistory_Diffing")); //NOI18N
        }
    }

    synchronized void cancelBackgroundTasks() {
        if (currentShowDiffTask != null && !currentShowDiffTask.isFinished()) {
            currentTask.cancel();
            currentShowDiffTask.cancel();  // it almost always late it's enqueued, so:
        }
    }

    private boolean onSelectionshowLastDifference;

    protected void setDiffIndex(int idx, boolean showLastDifference) {
        currentIndex = idx;
        onSelectionshowLastDifference = showLastDifference;
        treeView.setSelection(idx);
    }

    protected void showRevisionDiff(RepositoryRevision.Event rev, boolean showLastDifference) {
        showDiff(rev.getLogInfoHeader().getRepositoryRootUrl(), null, rev, showLastDifference);
    }

    protected boolean showContainerDiff(RepositoryRevision container, boolean showLastDifference) {
        boolean initialized = container.isEventsInitialized();
        List<RepositoryRevision.Event> revs = container.getEvents();
        
        RepositoryRevision.Event newest = getEventForRoots(container, null);
        if(newest == null) {
            newest = revs.get(0);   
        }
        if (newest == null && !initialized) {
            return false;
        } else {
            showRevisionDiff(newest, showLastDifference);
            return true;
        }
    }

    private RepositoryRevision.Event getEventForRoots (RepositoryRevision container, VCSFileProxy preferedFile) {
        RepositoryRevision.Event event = null;
        List<RepositoryRevision.Event> revs;
        if (container.isEventsInitialized()) {
            revs = container.getEvents();
        } else {
            revs = container.getDummyEvents();
        }

        //try to get the root
        VCSFileProxy[] roots = parent.getRoots();
        outer:
        for(RepositoryRevision.Event evt : revs) {
            if (preferedFile == null) {
                for(VCSFileProxy root : roots) {
                    if (root.equals(evt.getFile())) {
                        event = evt;
                        break outer;
                    } else if (similarPaths(root, evt.getFile())) {
                        event = evt;
                    }
                }
            } else {
                if (preferedFile.equals(evt.getFile())) {
                    event = evt;
                    break;
                } else if (similarPaths(preferedFile, evt.getFile())) {
                    event = evt;
                }
            }
        }
        return event;
    }
    
    void onNextButton() {
        if (currentDiff != null) {
            if (++currentDifferenceIndex >= currentDiff.getDifferenceCount()) {
                currentDifferenceIndex = 0; // preventing exception when only one item is showed
                if (++currentIndex >= treeView.getRowCount()) currentIndex = 0;
                setDiffIndex(currentIndex, false);
            } else {
                currentDiff.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, currentDifferenceIndex);
            }
        } else {
            if (++currentIndex >= treeView.getRowCount()) currentIndex = 0;
            setDiffIndex(currentIndex, false);
        }
    }

    void onPrevButton() {
        if (currentDiff != null) {
            if (--currentDifferenceIndex < 0) {
                currentDifferenceIndex = 0; // preventing exception when only one item is showed
                if (--currentIndex < 0) currentIndex = treeView.getRowCount() - 1;
                setDiffIndex(currentIndex, true);
            } else if (currentDifferenceIndex < currentDiff.getDifferenceCount()) {
                currentDiff.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, currentDifferenceIndex);
            }
        } else {
            if (--currentIndex < 0) currentIndex = treeView.getRowCount() - 1;
            setDiffIndex(currentIndex, true);
        }
    }

    boolean isNextEnabled() {
        return currentDiff != null;
    }

    boolean isPrevEnabled() {
        return currentDiff != null;
    }
    
    /**
     * Selects given revision in the view as if done by the user.
     *
     * @param revision revision to select
     */
    void select(RepositoryRevision.Event revision) {
        treeView.requestFocusInWindow();
        treeView.setSelection(revision);
    }

    void select(RepositoryRevision container) {
        treeView.requestFocusInWindow();
        treeView.setSelection(container);
    }

    void refreshResults (List<RepositoryRevision> res) {
        results = res;
        treeView.refreshResults(res);
    }

    private boolean similarPaths (VCSFileProxy referenceFile, VCSFileProxy file) {
        if (file == null) {
            return false;
        }
        return referenceFile.getName().equals(file.getName())
                || referenceFile.getPath().equalsIgnoreCase(null);
    }

    private class ShowDiffTask extends SvnProgressSupport {
        
        private VCSFileProxy file1;
        private String revision1;
        private final boolean showLastDifference;
        private final RepositoryRevision.Event event2;
        private DiffStreamSource s1;
        private DiffStreamSource s2;
        private String filePath1;
        private String name1;

        private ShowDiffTask(FileSystem fileSystem, RepositoryRevision.Event event1, RepositoryRevision.Event event2, boolean showLastDifference) {
            super(fileSystem);
            this.event2 = event2;
            if (event1 != null) {
                revision1 = event1.getLogInfoHeader().getLog().getRevision().toString();
                file1 = event1.getOriginalFile() == null ? event1.getFile() : event1.getOriginalFile();
                name1 = event1.getOriginalName() == null ? event1.getName() : event1.getOriginalName();
                filePath1 = event1.getOriginalPath() == null ? event1.getChangedPath().getPath() : event1.getOriginalPath();
            }
            this.showLastDifference = showLastDifference;
        }

        @Override
        protected void perform() {
            showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_LoadingDiff")); // NOI18N
            if (revision1 == null) {
                revision1 = Long.toString(event2.getChangedPath().getCopySrcRevision() == null 
                        ? event2.getLogInfoHeader().getLog().getRevision().getNumber() - 1
                        : event2.getChangedPath().getCopySrcRevision().getNumber());
                file1 = event2.getOriginalFile() == null ? event2.getFile() : event2.getOriginalFile();
                name1 = event2.getOriginalName() == null ? event2.getName() : event2.getOriginalName();
                filePath1 = event2.getOriginalPath() == null ? event2.getChangedPath().getPath() : event2.getOriginalPath();
            }
            if (isCanceled()) {
                return;
            }
            String revision2 = event2.getLogInfoHeader().getLog().getRevision().toString();
            String pegRevision1 = revision1;
            String pegRevision2 = revision2;
            String title1 = name1 + " (" + revision1 + ")"; //NOI18N
            String title2 = event2.getName() + " (" + revision2 + ")"; //NOI18N
            SVNUrl repoUrl = event2.getLogInfoHeader().getRepositoryRootUrl();
            SVNUrl fileUrl = repoUrl.appendPath(event2.getChangedPath().getPath());

            // through peg revision always except from 'deleting the file', since the file does not exist in the newver revision
            s1 = new DiffStreamSource(
                    getFileSystem(),
                    file1,
                    name1,
                    repoUrl,
                    repoUrl.appendPath(filePath1),
                    revision1,
                    pegRevision1, title1);

            s2 = new DiffStreamSource(
                    getFileSystem(),
                    event2.getFile(),
                    event2.getName(),
                    repoUrl,
                    fileUrl,
                    revision2,
                    pegRevision2,
                    title2);

            this.setCancellableDelegate(new Cancellable() {
                @Override
                public boolean cancel() {
                    s1.cancel();
                    s2.cancel();
                    return true;
                }
            });
            // it's enqueued at ClientRuntime queue and does not return until previous request handled
            s1.getMIMEType();  // triggers s1.init()
            if (isCanceled()) {
                showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_NoRevisions")); // NOI18N
                return;
            }

            s2.getMIMEType();  // triggers s2.init()
            if (isCanceled()) {
                showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_NoRevisions")); // NOI18N
                return;
            }

            if (currentTask != this) {
                return;
            }

            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (isCanceled()) {
                            showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_NoRevisions")); // NOI18N
                            return;
                        }
                        final DiffController view = DiffController.createEnhanced(s1, s2);
                        if (currentTask == ShowDiffTask.this) {
                            currentDiff = view;
                            setBottomComponent(currentDiff.getJComponent());
                            final int dl = diffView.getDividerLocation();
                            if (!setLocation(view)) {
                                view.addPropertyChangeListener(new PropertyChangeListener() {
                                    @Override
                                    public void propertyChange(PropertyChangeEvent evt) {
                                        view.removePropertyChangeListener(this);
                                        setLocation(view);
                                        Runnable inAWT = new Runnable() {
                                            @Override
                                            public void run() {
                                                parent.refreshComponents(false);
                                            }
                                        };
                                        if (EventQueue.isDispatchThread()) {
                                            inAWT.run();
                                        } else {
                                            EventQueue.invokeLater(inAWT);
                                        }
                                    }
                                });
                            }
                            parent.refreshComponents(false);
                            EventQueue.invokeLater(new Runnable () {
                                @Override
                                public void run() {
                                    diffView.setDividerLocation(dl);
                                }
                            });
                        }
                    } catch (IOException e) {
                        Subversion.LOG.log(Level.INFO, null, e);
                    }
                }
            });
        }

        private boolean setLocation (DiffController view) {
            boolean locationSet = false;
            if (view == currentDiff && view.getDifferenceCount() > 0) {
                locationSet = true;
                currentDifferenceIndex = showLastDifference ? view.getDifferenceCount() - 1 : 0;
                view.setLocation(DiffController.DiffPane.Base, DiffController.LocationType.DifferenceIndex, currentDifferenceIndex);
            }
            return locationSet;
        }
    }
    
    public JComponent getComponent() {
        return diffView;
    }
}


