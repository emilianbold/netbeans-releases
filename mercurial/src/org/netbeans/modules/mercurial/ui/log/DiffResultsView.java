/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.ErrorManager;
import org.openide.windows.TopComponent;
import org.netbeans.modules.versioning.util.NoContentPanel;
import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;
import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.*;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.diff.DiffController;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.ui.diff.DiffSetupSource;
import org.netbeans.modules.mercurial.ui.diff.DiffStreamSource;

/**
 * Shows Search History results in a table with Diff pane below it.
 * 
 * @author Maros Sandor
 */
class DiffResultsView implements AncestorListener, PropertyChangeListener, DiffSetupSource {

    protected final SearchHistoryPanel parent;

    protected DiffTreeTable treeView;
    private JSplitPane    diffView;
    
    protected HgProgressSupport            currentTask;
    private RequestProcessor.Task   currentShowDiffTask;
    
    protected DiffController            currentDiff;
    private int                     currentDifferenceIndex;
    protected int                     currentIndex;
    private boolean                 dividerSet;
    protected List<RepositoryRevision> results;
    private static final RequestProcessor rp = new RequestProcessor("MercurialDiff", 1, true);  // NOI18N

    private static String HgNoRev = "-1"; // NOI18N
    
    public DiffResultsView(SearchHistoryPanel parent, List<RepositoryRevision> results) {
        this.parent = parent;
        this.results = results;
        treeView = new DiffTreeTable(parent);
        treeView.setResults(results);
        treeView.addAncestorListener(this);

        diffView = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        diffView.setTopComponent(treeView);
        setBottomComponent(new NoContentPanel(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_NoRevisions"))); // NOI18N
    }

    public void ancestorAdded(AncestorEvent event) {
        ExplorerManager em = ExplorerManager.find(treeView);
        em.addPropertyChangeListener(this);
        if (!dividerSet) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    dividerSet = true;
                    diffView.setDividerLocation(0.33);
                }
            });
        }
    }

    public void ancestorMoved(AncestorEvent event) {
    }

    public void ancestorRemoved(AncestorEvent event) {
        ExplorerManager em = ExplorerManager.find(treeView);
        em.removePropertyChangeListener(this);
        cancelBackgroundTasks();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            final Node [] nodes = (Node[]) evt.getNewValue();
            currentDifferenceIndex = 0;
            if (nodes.length == 0) {
                showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_NoRevisions")); // NOI18N
                parent.refreshComponents(false);
                return;
            }
            else if (nodes.length > 2) {
                showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_TooManyRevisions")); // NOI18N
                parent.refreshComponents(false);
                return;
            }

            // invoked asynchronously becase treeView.getSelection() may not be ready yet
            Runnable runnable = new Runnable() {
                public void run() {
                    RepositoryRevision container1 = nodes[0].getLookup().lookup(RepositoryRevision.class);
                    RepositoryRevision.Event r1 = nodes[0].getLookup().lookup(RepositoryRevision.Event.class);
                    try {
                        currentIndex = treeView.getSelection()[0];
                        if (nodes.length == 1) {
                            if (container1 != null) {
                                showContainerDiff(container1, onSelectionshowLastDifference);
                            }
                            else if (r1 != null) {
                                showRevisionDiff(r1, onSelectionshowLastDifference);
                            }
                        } else if (nodes.length == 2) {
                            RepositoryRevision.Event revOlder = null;
                            if (container1 != null) {
                                /**
                                 * both repository revision events must be acquired from a container, not through a Lookup as before,
                                 * since only two containers (and no rev-event) are present in the lookup
                                 */
                                RepositoryRevision container2 = nodes[1].getLookup().lookup(RepositoryRevision.class);
                                r1 = getEventForRoots(container1);
                                revOlder = getEventForRoots(container2);
                            } else {
                                revOlder = (RepositoryRevision.Event) nodes[1].getLookup().lookup(RepositoryRevision.Event.class);
                            }
                            if (r1 == null || revOlder == null || revOlder.getFile() == null || !revOlder.getFile().equals(r1.getFile())) {
                                throw new Exception();
                            }
                            String revisionNumberOlder = r1.getLogInfoHeader().getLog().getRevision();
                            String revisionNumberNewer = revOlder.getLogInfoHeader().getLog().getRevision();
                            showDiff(r1, revisionNumberNewer, revisionNumberOlder, false);
                        }
                    } catch (Exception e) {
                        showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_IllegalSelection")); // NOI18N
                        parent.refreshComponents(false);
                        return;
                    }

                }
            };
            SwingUtilities.invokeLater(runnable);
        }
    }

    public Collection getSetups() {
        Node [] nodes = TopComponent.getRegistry().getActivatedNodes();
        if (nodes.length == 0) {
            return parent.getSetups(results.toArray(new RepositoryRevision[results.size()]), new RepositoryRevision.Event[0]);
        }
        
        Set<RepositoryRevision.Event> events = new HashSet<RepositoryRevision.Event>();
        Set<RepositoryRevision> revisions = new HashSet<RepositoryRevision>();
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

    public String getSetupDisplayName() {
        return null;
    }

    protected void showDiffError(String s) {
        setBottomComponent(new NoContentPanel(s));
    }

    protected void setBottomComponent(Component component) {
        int dl = diffView.getDividerLocation();
        diffView.setBottomComponent(component);
        diffView.setDividerLocation(dl);
    }

    protected HgProgressSupport createShowDiffTask(RepositoryRevision.Event header, String revision1, String revision2, boolean showLastDifference) {
        return new ShowDiffTask(header, revision1, revision2, showLastDifference);
    }

    /**
     *
     * @param header
     * @param revision1 if null then parent revision will be used
     * @param revision2
     * @param showLastDifference
     */
    protected void showDiff(RepositoryRevision.Event header, String revision1, String revision2, boolean showLastDifference) {
        synchronized(this) {
            cancelBackgroundTasks();
            char action = header.getChangedPath().getAction();
            if(action == HgLogMessage.HgModStatus){
                currentTask = createShowDiffTask(header, revision1, revision2, showLastDifference);
            }else if(action == HgLogMessage.HgAddStatus){
                currentTask = createShowDiffTask(header, HgNoRev, revision2, showLastDifference);
            }else if(action == HgLogMessage.HgDelStatus){
                currentTask = createShowDiffTask(header, revision1, HgNoRev, showLastDifference);
            }else if(action == HgLogMessage.HgCopyStatus){
                currentTask = createShowDiffTask(header, HgNoRev, revision2, showLastDifference);
            } else {
                currentTask = createShowDiffTask(header, revision1, revision2, showLastDifference);
            }
            currentShowDiffTask = currentTask.start(rp, header.getLogInfoHeader().getRepositoryRoot(), NbBundle.getMessage(DiffResultsView.class, "LBL_SearchHistory_Diffing"));
        }
    }

    private synchronized void cancelBackgroundTasks() {
        if (currentShowDiffTask != null && !currentShowDiffTask.isFinished()) {
            currentShowDiffTask.cancel();  // it almost always late it's enqueued, so:
            currentTask.cancel();
        }
    }

    private boolean onSelectionshowLastDifference;

    protected void setDiffIndex(int idx, boolean showLastDifference) {
        currentIndex = idx;
        onSelectionshowLastDifference = showLastDifference;
        treeView.setSelection(idx);
    }

    protected void showRevisionDiff(RepositoryRevision.Event rev, boolean showLastDifference) {
        if (rev.getFile() == null) return;
        long revision2 = Long.parseLong(rev.getLogInfoHeader().getLog().getRevision());
        showDiff(rev, null, Long.toString(revision2), showLastDifference);
    }

    protected void showContainerDiff(RepositoryRevision container, boolean showLastDifference) {
        List<RepositoryRevision.Event> revs = container.getEvents();
        
        RepositoryRevision.Event newest = getEventForRoots(container);
        if(newest == null) {
            newest = revs.get(0);   
        }        
        showRevisionDiff(newest, showLastDifference);
    }

    private RepositoryRevision.Event getEventForRoots(RepositoryRevision container) {
        RepositoryRevision.Event event = null;
        List<RepositoryRevision.Event> revs = container.getEvents();

        //try to get the root
        File[] roots = parent.getRoots();
        for(File root : roots) {
            for(RepositoryRevision.Event evt : revs) {
                if(root.equals(evt.getFile())) {
                    event = evt;
                }
            }
        }

        return event;
    }
    
    void onNextButton() {
        if (currentDiff != null) {
            if (++currentDifferenceIndex >= currentDiff.getDifferenceCount()) {
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
        if (currentDiff != null) {
            return currentIndex < treeView.getRowCount() - 1 || currentDifferenceIndex < currentDiff.getDifferenceCount() - 1;
        } else {
            return false;
        }
    }

    boolean isPrevEnabled() {
        return currentIndex > 0 || currentDifferenceIndex > 0;
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

    private class ShowDiffTask extends HgProgressSupport {
        
        private final RepositoryRevision.Event header;
        private String revision1;
        private final String revision2;
        private boolean showLastDifference;

        public ShowDiffTask(RepositoryRevision.Event header, String revision1, String revision2, boolean showLastDifference) {
            this.header = header;
            this.revision1 = revision1;
            this.revision2 = revision2;
            this.showLastDifference = showLastDifference;
        }

        public void perform () {
            showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_LoadingDiff")); //NOI18N
            if (revision1 == null) {
                revision1 = header.getLogInfoHeader().getLog().getAncestor(null);
            }
            final DiffStreamSource s1 = new DiffStreamSource(header.getFile(), revision1, revision1);
            final DiffStreamSource s2 = new DiffStreamSource(header.getFile(), revision2, revision2);

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

            if (currentTask != this) return;

            SwingUtilities.invokeLater(new Runnable() {
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
                            if (!setLocation(view)) {
                                view.addPropertyChangeListener(new PropertyChangeListener() {
                                    public void propertyChange(PropertyChangeEvent evt) {
                                        view.removePropertyChangeListener(this);
                                        setLocation(view);
                                    }
                                });
                            }
                            parent.refreshComponents(false);
                        }
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
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


