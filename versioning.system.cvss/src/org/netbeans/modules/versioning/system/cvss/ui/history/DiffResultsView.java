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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;

/**
 * Shows Search History results in a table with Diff pane below it.
 * 
 * @author Maros Sandor
 */
class DiffResultsView implements AncestorListener, PropertyChangeListener {

    private DiffTreeTable treeView;
    private JSplitPane    diffView;
    
    private ShowDiffTask            currentTask;
    private RequestProcessor.Task   currentShowDiffTask;

    public DiffResultsView(List results) {
        treeView = new DiffTreeTable();
        treeView.setResults(results);
        treeView.addAncestorListener(this);

        diffView = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        diffView.setTopComponent(treeView);
        diffView.setBottomComponent(new NoContentPanel(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_NoRevisions")));
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
            Node [] nodes = (Node[]) evt.getNewValue();
            if (nodes.length == 0) {
                showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_NoRevisions"));
                return;
            }
            else if (nodes.length > 2) {
                showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_TooManyRevisions"));
                return;
            }

            SearchHistoryPanel.ResultsContainer container1 = (SearchHistoryPanel.ResultsContainer) nodes[0].getLookup().lookup(SearchHistoryPanel.ResultsContainer.class);
            LogInformation.Revision r1 = (LogInformation.Revision) nodes[0].getLookup().lookup(LogInformation.Revision.class);
            try {
                if (nodes.length == 1) {
                    if (container1 != null) {
                        List revs = container1.getRevisions();
                        LogInformation.Revision newest = (LogInformation.Revision) revs.get(0);
                        LogInformation.Revision eldest = (LogInformation.Revision) revs.get(revs.size() - 1);
                        showDiff(newest.getLogInfoHeader(), eldest.getNumber(), newest.getNumber());
                    }
                    else if (r1 != null) {
                        String revision2 = r1.getNumber();
                        String revision1 = Utils.previousRevision(revision2);
                        showDiff(r1.getLogInfoHeader(), revision1, revision2);
                    }
                } else if (nodes.length == 2) {
                    LogInformation.Revision r2 = (LogInformation.Revision) nodes[1].getLookup().lookup(LogInformation.Revision.class);
                    if (r2.getLogInfoHeader() != r1.getLogInfoHeader()) {
                        throw new Exception();
                    }
                    String revision2 = r1.getNumber();
                    String revision1 = r2.getNumber();
                    showDiff(r1.getLogInfoHeader(), revision1, revision2);
                }
            } catch (Exception e) {
                showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_IllegalSelection"));
                return;
            }
        }
    }

    private void showDiffError(String s) {
        diffView.setBottomComponent(new NoContentPanel(s));
    }
    
    private void showDiff(LogInformation header, String revision1, String revision2) {
        synchronized(this) {
            if (currentShowDiffTask != null && !currentShowDiffTask.isFinished()) {
                currentShowDiffTask.cancel();
            }
            currentTask = new ShowDiffTask(header, revision1, revision2);
            currentShowDiffTask = RequestProcessor.getDefault().create(currentTask);
            currentShowDiffTask.schedule(0);
        }
    }

    private class ShowDiffTask implements Runnable {
        
        private final LogInformation header;
        private final String revision1;
        private final String revision2;

        public ShowDiffTask(LogInformation header, String revision1, String revision2) {
            this.header = header;
            this.revision1 = revision1;
            this.revision2 = revision2;
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
                            int dl = diffView.getDividerLocation();
                            diffView.setBottomComponent(view.getComponent());
                            diffView.setDividerLocation(dl);
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

