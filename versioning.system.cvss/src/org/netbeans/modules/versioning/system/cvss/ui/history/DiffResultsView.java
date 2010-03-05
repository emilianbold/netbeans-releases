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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.versioning.system.cvss.ui.history;

import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.ui.actions.diff.DiffStreamSource;
import org.netbeans.modules.versioning.system.cvss.VersionsCache;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.netbeans.api.diff.DiffController;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Cancellable;
import org.openide.ErrorManager;

import javax.swing.*;
import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;
import java.util.*;
import java.util.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.awt.*;

/**
 * Shows Search History results in a table with Diff pane below it.
 * 
 * @author Maros Sandor
 */
class DiffResultsView implements AncestorListener, PropertyChangeListener {

    private final SearchHistoryPanel parent;

    private DiffTreeTable treeView;
    private JSplitPane    diffView;
    
    private ShowDiffTask            currentTask;
    private RequestProcessor.Task   currentShowDiffTask;
    
    private DiffController          currentDiff;
    private int                     currentDifferenceIndex;
    private int                     currentIndex;
    private boolean                 dividerSet;

    public DiffResultsView(SearchHistoryPanel parent, List results) {
        this.parent = parent;
        treeView = new DiffTreeTable();
        treeView.setResults(results);
        treeView.addAncestorListener(this);

        diffView = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        diffView.setTopComponent(treeView);
        setBottomComponent(new NoContentPanel(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_NoRevisions")));  // NOI18N
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
        if (DiffController.PROP_DIFFERENCES.equals(evt.getPropertyName())) {
            currentDifferenceIndex = currentDiff.getDifferenceIndex();
            parent.refreshComponents(false);
        } else if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            final Node [] nodes = (Node[]) evt.getNewValue();
            currentDifferenceIndex = 0;
            if (nodes.length == 0) {
                showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_NoRevisions"));  // NOI18N
                parent.refreshComponents(false);
                return;
            }
            else if (nodes.length > 2) {
                showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_TooManyRevisions"));  // NOI18N
                parent.refreshComponents(false);
                return;
            }

            // invoked asynchronously becase treeView.getSelection() may not be ready yet
            Runnable runnable = new Runnable() {
                public void run() {
                    if (treeView.getSelection().length == 0) return;
                    SearchHistoryPanel.ResultsContainer container1 = nodes[0].getLookup().lookup(SearchHistoryPanel.ResultsContainer.class);
                    SearchHistoryPanel.DispRevision r1 = nodes[0].getLookup().lookup(SearchHistoryPanel.DispRevision.class);
                    try {
                        if (r1 == null || !r1.isBranchRoot()) {
                            if (nodes.length == 1) {
                                currentIndex = treeView.getSelection()[0];
                                if (container1 != null) {
                                    showContainerDiff(container1, onSelectionshowLastDifference);
                                }
                                else if (r1 != null) {
                                    showRevisionDiff(r1, onSelectionshowLastDifference);
                                }
                            } else if (nodes.length == 2) {
                                currentIndex = -1;
                                SearchHistoryPanel.DispRevision r2 = nodes[1].getLookup().lookup(SearchHistoryPanel.DispRevision.class);
                                if (r2.isBranchRoot()) throw new Exception();
                                if (r2.getRevision().getLogInfoHeader() != r1.getRevision().getLogInfoHeader()) {
                                    throw new Exception();
                                }
                                String revision1 = r1.getRevision().getNumber();
                                String revision2 = r2.getRevision().getNumber();
                                if (SearchHistoryPanel.compareRevisions(revision1, revision2) > 0) {
                                    revision2 = r1.getRevision().getNumber();
                                    revision1 = r2.getRevision().getNumber();
                                }
                                showDiff(r1.getRevision().getLogInfoHeader(), revision1, revision2, false);
                            }
                        } else {
                            showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_IllegalSelection"));  // NOI18N
                        }
                    } catch (Exception e) {
                        showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_IllegalSelection"));  // NOI18N
                        parent.refreshComponents(false);
                        return;
                    }
                }
            };
            SwingUtilities.invokeLater(runnable);
        }
    }

    private void showDiffError(String s) {
        setBottomComponent(new NoContentPanel(s));
    }

    private void setBottomComponent(Component component) {
        int dl = diffView.getDividerLocation();
        diffView.setBottomComponent(component);
        diffView.setDividerLocation(dl);
    }

    private void showDiff(LogInformation header, String revision1, String revision2, boolean showLastDifference) {
        synchronized(this) {
            cancelBackgroundTasks();
            currentTask = new ShowDiffTask(header, revision1, revision2, showLastDifference);
            currentShowDiffTask = RequestProcessor.getDefault().create(currentTask);
            currentShowDiffTask.schedule(0);
        }
    }

    private synchronized void cancelBackgroundTasks() {
        if (currentShowDiffTask != null && !currentShowDiffTask.isFinished()) {
            currentShowDiffTask.cancel();  // it almost always late it's enqueued, so:
            currentTask.cancel();
        }
    }

    private boolean onSelectionshowLastDifference;

    private void setDiffIndex(int idx, boolean showLastDifference) {
        currentIndex = idx;
        onSelectionshowLastDifference = showLastDifference;
        treeView.setSelection(idx);
    }

    private void showRevisionDiff(SearchHistoryPanel.DispRevision rev, boolean showLastDifference) {
        String revision2 = rev.getRevision().getNumber();
        String revision1;
        if (revision2 == VersionsCache.REVISION_CURRENT) {
            SearchHistoryPanel.ResultsContainer container = findParent(rev);
            SearchHistoryPanel.DispRevision newest = container.getRevisions().get(1);
            revision1 = newest.getRevision().getNumber();
        } else {
            revision1 = Utils.previousRevision(revision2);
        }
        showDiff(rev.getRevision().getLogInfoHeader(), revision1, revision2, showLastDifference);
    }

    private SearchHistoryPanel.ResultsContainer findParent(SearchHistoryPanel.DispRevision rev) {
        List results = parent.getDispResults();
        for (Object o : results) {
            if (o instanceof SearchHistoryPanel.ResultsContainer) {
                SearchHistoryPanel.ResultsContainer container = (SearchHistoryPanel.ResultsContainer) o;
                if (container.getRevisions().contains(rev)) return container;
            }
        }
        return null;
    }

    private void showContainerDiff(SearchHistoryPanel.ResultsContainer container, boolean showLastDifference) {
        List revs = container.getRevisions();
        SearchHistoryPanel.DispRevision newest = (SearchHistoryPanel.DispRevision) revs.get(1);
        showDiff(newest.getRevision().getLogInfoHeader(), container.getEldestRevision(), newest.getRevision().getNumber(), showLastDifference);
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
            } else {
                currentDiff.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, currentDifferenceIndex);
            }
        } else {
            if (--currentIndex < 0) currentIndex = treeView.getRowCount() - 1;
            setDiffIndex(currentIndex, true);
        }
    }

    boolean isNextEnabled() {
        if (currentDiff != null) {
            return currentIndex != -1 && currentIndex < treeView.getRowCount() - 1 || currentDifferenceIndex < currentDiff.getDifferenceCount() - 1;
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
    void select(SearchHistoryPanel.DispRevision revision) {
        treeView.requestFocusInWindow();
        treeView.setSelection(revision);
    }

    void select(SearchHistoryPanel.ResultsContainer container) {
        treeView.requestFocusInWindow();
        treeView.setSelection(container);
    }

    /**
     * @return Collection<Object> currently selected items in the view or an empty Collection.
     */
    List<Object> getSelection() {
        Node [] nodes = ExplorerManager.find(treeView).getSelectedNodes();
        List<Object> selection = new ArrayList<Object>(nodes.length);
        for (Node node : nodes) {
            RevisionNode rnode = (RevisionNode) node;
            if (rnode.getContainer() != null) {
                selection.add(rnode.getContainer());
            } else {
                selection.add(rnode.getDispRevision());
            }
        }
        return selection;
    }

    private class ShowDiffTask implements Runnable, Cancellable {
        
        private final LogInformation header;
        private final String revision1;
        private final String revision2;
        private boolean showLastDifference;
        private volatile boolean cancelled;

        public ShowDiffTask(LogInformation header, String revision1, String revision2, boolean showLastDifference) {
            this.header = header;
            this.revision1 = revision1;
            this.revision2 = revision2;
            this.showLastDifference = showLastDifference;
        }

        public void run() {
            parent.getUndoRedo().setDiffView(null);            
            final DiffStreamSource s1 = new DiffStreamSource(header.getFile(), revision1, revision1 == VersionsCache.REVISION_CURRENT ? 
                    NbBundle.getMessage(DiffResultsView.class, "LBL_DiffPanel_LocalCopy") : revision1);  // NOI18N
            final DiffStreamSource s2 = new DiffStreamSource(header.getFile(), revision2, revision2 == VersionsCache.REVISION_CURRENT ? 
                    NbBundle.getMessage(DiffResultsView.class, "LBL_DiffPanel_LocalCopy") : revision2);  // NOI18N

            // it's enqueued at ClientRuntime queue and does not return until previous request handled
            s1.getMIMEType();  // triggers s1.init()
            if (cancelled) {
                return;
            }

            s2.getMIMEType();  // triggers s2.init()
            if (cancelled) {
                return;
            }

            if (currentTask != this) return;

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        if (cancelled) {
                            return;
                        }
                        DiffController view = DiffController.createEnhanced(s1, s2);
                        if (currentTask == ShowDiffTask.this) {
                            if (currentDiff != null) {
                                currentDiff.removePropertyChangeListener(DiffResultsView.this);
                            }
                            currentDiff = view;
                            currentDiff.addPropertyChangeListener(DiffResultsView.this);
                            parent.getUndoRedo().setDiffView((JComponent) currentDiff.getJComponent());
                            setBottomComponent(currentDiff.getJComponent());
                            if (currentDiff.getDifferenceCount() > 0) {
                                currentDifferenceIndex = showLastDifference ? currentDiff.getDifferenceCount() - 1 : 0;
                                currentDiff.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, currentDifferenceIndex);
                            }
                            parent.refreshComponents(false);
                        }
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            });
        }

        public boolean cancel() {
            cancelled = true;
            return true;
        }
    }
    
    JComponent getCurrentDiffComponent() {
        return (JComponent) (currentDiff == null ? null : currentDiff.getJComponent());
    }

    public JComponent getComponent() {
        return diffView;
    }
}

