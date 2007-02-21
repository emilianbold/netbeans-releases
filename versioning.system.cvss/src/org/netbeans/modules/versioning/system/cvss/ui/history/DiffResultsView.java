/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.versioning.system.cvss.ui.history;

import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.ui.actions.diff.DiffStreamSource;
import org.netbeans.modules.versioning.system.cvss.VersionsCache;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.Diff;
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
    
    private DiffView                currentDiff;
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
        setBottomComponent(new NoContentPanel(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_NoRevisions")));
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
                showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_NoRevisions"));
                parent.refreshComponents(false);
                return;
            }
            else if (nodes.length > 2) {
                showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_TooManyRevisions"));
                parent.refreshComponents(false);
                return;
            }

            // invoked asynchronously becase treeView.getSelection() may not be ready yet
            Runnable runnable = new Runnable() {
                public void run() {
                    if (treeView.getSelection().length == 0) return;
                    SearchHistoryPanel.ResultsContainer container1 = (SearchHistoryPanel.ResultsContainer) nodes[0].getLookup().lookup(SearchHistoryPanel.ResultsContainer.class);
                    SearchHistoryPanel.DispRevision r1 = (SearchHistoryPanel.DispRevision) nodes[0].getLookup().lookup(SearchHistoryPanel.DispRevision.class);
                    try {
                        if (r1 == null || !r1.isBranchRoot()) {
                            currentIndex = treeView.getSelection()[0];
                            if (nodes.length == 1) {
                                if (container1 != null) {
                                    showContainerDiff(container1, onSelectionshowLastDifference);
                                }
                                else if (r1 != null) {
                                    showRevisionDiff(r1, onSelectionshowLastDifference);
                                }
                            } else if (nodes.length == 2) {
                                SearchHistoryPanel.DispRevision r2 = (SearchHistoryPanel.DispRevision) nodes[1].getLookup().lookup(SearchHistoryPanel.DispRevision.class);
                                if (r2.isBranchRoot()) throw new Exception();
                                if (r2.getRevision().getLogInfoHeader() != r1.getRevision().getLogInfoHeader()) {
                                    throw new Exception();
                                }
                                String revision1 = r1.getRevision().getNumber();
                                String revision2 = r2.getRevision().getNumber();
                                if (compareRevisions(revision1, revision2) == 1) {
                                    revision2 = r1.getRevision().getNumber();
                                    revision1 = r2.getRevision().getNumber();
                                }
                                showDiff(r1.getRevision().getLogInfoHeader(), revision1, revision2, false);
                            }
                        } else {
                            showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_IllegalSelection"));
                        }
                    } catch (Exception e) {
                        showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_IllegalSelection"));
                        parent.refreshComponents(false);
                        return;
                    }
                }
            };
            SwingUtilities.invokeLater(runnable);
        }
    }

    private static int compareRevisions(String r1, String r2) {
        StringTokenizer st1 = new StringTokenizer(r1, "."); // NOI18N
        StringTokenizer st2 = new StringTokenizer(r2, "."); // NOI18N
        for (;;) {
            if (!st1.hasMoreTokens()) {
                return st2.hasMoreTokens() ? -1 : 0;
            }
            if (!st2.hasMoreTokens()) {
                return st1.hasMoreTokens() ? 1 : 0;
            }
            int n1 = Integer.parseInt(st1.nextToken());
            int n2 = Integer.parseInt(st2.nextToken());
            if (n1 != n2) return n1 - n2;
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
            SearchHistoryPanel.DispRevision newest = (SearchHistoryPanel.DispRevision) container.getRevisions().get(1);
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
                currentDiff.setCurrentDifference(currentDifferenceIndex);
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
                currentDiff.setCurrentDifference(currentDifferenceIndex);
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
    void select(SearchHistoryPanel.DispRevision revision) {
        treeView.requestFocusInWindow();
        treeView.setSelection(revision);
    }

    void select(SearchHistoryPanel.ResultsContainer container) {
        treeView.requestFocusInWindow();
        treeView.setSelection(container);
    }

    private class ShowDiffTask implements Runnable, Cancellable {
        
        private final LogInformation header;
        private final String revision1;
        private final String revision2;
        private boolean showLastDifference;
        private volatile boolean cancelled;
        private Thread thread;

        public ShowDiffTask(LogInformation header, String revision1, String revision2, boolean showLastDifference) {
            this.header = header;
            this.revision1 = revision1;
            this.revision2 = revision2;
            this.showLastDifference = showLastDifference;
        }

        public void run() {
            thread = Thread.currentThread();
            final Diff diff = Diff.getDefault();
            final DiffStreamSource s1 = new DiffStreamSource(header.getFile(), revision1, revision1 == VersionsCache.REVISION_CURRENT ? 
                    NbBundle.getMessage(DiffResultsView.class, "LBL_DiffPanel_LocalCopy") : revision1);
            final DiffStreamSource s2 = new DiffStreamSource(header.getFile(), revision2, revision2 == VersionsCache.REVISION_CURRENT ? 
                    NbBundle.getMessage(DiffResultsView.class, "LBL_DiffPanel_LocalCopy") : revision2);

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
                        final DiffView view = diff.createDiff(s1, s2);
                        if (currentTask == ShowDiffTask.this) {
                            currentDiff = view;
                            setBottomComponent(currentDiff.getComponent());
                            if (currentDiff.getDifferenceCount() > 0) {
                                currentDifferenceIndex = showLastDifference ? currentDiff.getDifferenceCount() - 1 : 0;
                                currentDiff.setCurrentDifference(currentDifferenceIndex);
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
            if (thread != null) {
                thread.interrupt();
            }
            return true;
        }
    }
    
    public JComponent getComponent() {
        return diffView;
    }
}

