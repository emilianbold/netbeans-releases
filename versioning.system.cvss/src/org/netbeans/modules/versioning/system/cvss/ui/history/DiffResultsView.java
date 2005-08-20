/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.history;

import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.util.NoContentPanel;
import org.netbeans.modules.versioning.system.cvss.ui.actions.diff.DiffStreamSource;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.Diff;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
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

    private final List              results;

    private DiffTreeTable treeView;
    private JSplitPane    diffView;
    
    private ShowDiffTask            currentTask;
    private RequestProcessor.Task   currentShowDiffTask;
    
    private DiffView                currentDiff;
    private int                     currentDifferenceIndex;
    private int                     currentIndex;

    public DiffResultsView(List results) {
        this.results = results;
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
    }

    public void ancestorMoved(AncestorEvent event) {
    }

    public void ancestorRemoved(AncestorEvent event) {
        ExplorerManager em = ExplorerManager.find(treeView);
        em.removePropertyChangeListener(this);
        currentTask = null;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            final Node [] nodes = (Node[]) evt.getNewValue();
            currentDifferenceIndex = 0;
            if (nodes.length == 0) {
                showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_NoRevisions"));
                return;
            }
            else if (nodes.length > 2) {
                showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_TooManyRevisions"));
                return;
            }

            // invoked asynchronously becase treeView.getSelection() may not be ready yet
            Runnable runnable = new Runnable() {
                public void run() {
                    SearchHistoryPanel.ResultsContainer container1 = (SearchHistoryPanel.ResultsContainer) nodes[0].getLookup().lookup(SearchHistoryPanel.ResultsContainer.class);
                    LogInformation.Revision r1 = (LogInformation.Revision) nodes[0].getLookup().lookup(LogInformation.Revision.class);
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
                            LogInformation.Revision r2 = (LogInformation.Revision) nodes[1].getLookup().lookup(LogInformation.Revision.class);
                            if (r2.getLogInfoHeader() != r1.getLogInfoHeader()) {
                                throw new Exception();
                            }
                            String revision2 = r1.getNumber();
                            String revision1 = r2.getNumber();
                            showDiff(r1.getLogInfoHeader(), revision1, revision2, false);
                        }
                    } catch (Exception e) {
                        showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_IllegalSelection"));
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
            if (currentShowDiffTask != null && !currentShowDiffTask.isFinished()) {
                currentShowDiffTask.cancel();
            }
            currentTask = new ShowDiffTask(header, revision1, revision2, showLastDifference);
            currentShowDiffTask = RequestProcessor.getDefault().create(currentTask);
            currentShowDiffTask.schedule(0);
        }
    }

    private boolean onSelectionshowLastDifference;

    private void setDiffIndex(int idx, boolean showLastDifference) {
        currentIndex = idx;
        onSelectionshowLastDifference = showLastDifference;
        treeView.setSelection(idx);
    }

    private void showRevisionDiff(LogInformation.Revision rev, boolean showLastDifference) {
        String revision2 = rev.getNumber();
        String revision1 = Utils.previousRevision(revision2);
        showDiff(rev.getLogInfoHeader(), revision1, revision2, showLastDifference);
    }

    private void showContainerDiff(SearchHistoryPanel.ResultsContainer container, boolean showLastDifference) {
        List revs = container.getRevisions();
        LogInformation.Revision newest = (LogInformation.Revision) revs.get(0);
        showDiff(newest.getLogInfoHeader(), container.getEldestRevision(), newest.getNumber(), showLastDifference);
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

    /**
     * Selects given revision in the view as if done by the user.
     *
     * @param revision revision to select
     */
    void select(LogInformation.Revision revision) {
        treeView.requestFocusInWindow();
        treeView.setSelection(revision);
    }

    void select(SearchHistoryPanel.ResultsContainer container) {
        treeView.requestFocusInWindow();
        treeView.setSelection(container);
    }

    private class ShowDiffTask implements Runnable {
        
        private final LogInformation header;
        private final String revision1;
        private final String revision2;
        private boolean showLastDifference;

        public ShowDiffTask(LogInformation header, String revision1, String revision2, boolean showLastDifference) {
            this.header = header;
            this.revision1 = revision1;
            this.revision2 = revision2;
            this.showLastDifference = showLastDifference;
        }

        public void run() {
            Diff diff = Diff.getDefault();
            try {
                DiffStreamSource s1 = new DiffStreamSource(header.getFile(), revision1, revision1, false);
                DiffStreamSource s2 = new DiffStreamSource(header.getFile(), revision2, revision2, false);
                
                if (currentTask != this) return;
                final DiffView view = diff.createDiff(s1, s2);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (currentTask == ShowDiffTask.this) {
                            currentDiff = view;
                            setBottomComponent(currentDiff.getComponent());
                            if (currentDiff.getDifferenceCount() > 0) {
                                currentDifferenceIndex = showLastDifference ? currentDiff.getDifferenceCount() - 1 : 0;
                                currentDiff.setCurrentDifference(currentDifferenceIndex);
                            }
                        }
                    }
                });
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }
    
    public JComponent getComponent() {
        return diffView;
    }
}

