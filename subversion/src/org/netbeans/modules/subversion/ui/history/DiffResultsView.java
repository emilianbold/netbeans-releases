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
package org.netbeans.modules.subversion.ui.history;

import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.util.Cancellable;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.ErrorManager;
import org.openide.windows.TopComponent;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.Diff;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.netbeans.modules.subversion.ui.diff.DiffStreamSource;
import org.netbeans.modules.subversion.ui.diff.DiffSetupSource;
import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;
import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.*;
import java.awt.Component;
import java.io.File;
import java.io.IOException;

/**
 * Shows Search History results in a table with Diff pane below it.
 * 
 * @author Maros Sandor
 */
class DiffResultsView implements AncestorListener, PropertyChangeListener, DiffSetupSource {

    private final SearchHistoryPanel parent;

    private DiffTreeTable treeView;
    private JSplitPane    diffView;
    
    private ShowDiffTask            currentTask;
    private RequestProcessor.Task   currentShowDiffTask;
    
    private DiffView                currentDiff;
    private int                     currentDifferenceIndex;
    private int                     currentIndex;
    private boolean                 dividerSet;
    private List<RepositoryRevision> results;
    private static final RequestProcessor rp = new RequestProcessor("SubversionDiff", 1, true);  // NOI18N

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
                            RepositoryRevision.Event r2 = (RepositoryRevision.Event) nodes[1].getLookup().lookup(RepositoryRevision.Event.class);
                            if (r2.getFile() == null || !r2.getFile().equals(r1.getFile())) {
                                throw new Exception();
                            }
                            long revision2 = r1.getLogInfoHeader().getLog().getRevision().getNumber();
                            long revision1 = r2.getLogInfoHeader().getLog().getRevision().getNumber();
                            showDiff(r1, Long.toString(revision1), Long.toString(revision2), false);
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

    private void showDiffError(String s) {
        setBottomComponent(new NoContentPanel(s));
    }

    private void setBottomComponent(Component component) {
        int dl = diffView.getDividerLocation();
        diffView.setBottomComponent(component);
        diffView.setDividerLocation(dl);
    }

    private void showDiff(RepositoryRevision.Event header, String revision1, String revision2, boolean showLastDifference) {
        synchronized(this) {
            cancelBackgroundTasks();
            currentTask = new ShowDiffTask(header, revision1, revision2, showLastDifference);
            currentShowDiffTask = rp.create(currentTask);
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

    private void showRevisionDiff(RepositoryRevision.Event rev, boolean showLastDifference) {
        if (rev.getFile() == null) return;
        long revision2 = rev.getLogInfoHeader().getLog().getRevision().getNumber();
        long revision1 = revision2 - 1;
        showDiff(rev, Long.toString(revision1), Long.toString(revision2), showLastDifference);
    }

    private void showContainerDiff(RepositoryRevision container, boolean showLastDifference) {        
        List<RepositoryRevision.Event> revs = container.getEvents();
        
        RepositoryRevision.Event newest = null;
        //try to get the root        
        File[] roots = parent.getRoots();
        for(File root : roots) {
            for(RepositoryRevision.Event evt : revs) {
                if(root.equals(evt.getFile())) {
                    newest = evt;   
                }   
            }            
        }
        if(newest == null) {
            newest = revs.get(0);   
        }        
        if (newest.getFile() == null) return;
        long rev = newest.getLogInfoHeader().getLog().getRevision().getNumber();
        showDiff(newest, Long.toString(rev - 1), Long.toString(rev), showLastDifference);
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
    void select(RepositoryRevision.Event revision) {
        treeView.requestFocusInWindow();
        treeView.setSelection(revision);
    }

    void select(RepositoryRevision container) {
        treeView.requestFocusInWindow();
        treeView.setSelection(container);
    }

    private class ShowDiffTask implements Runnable, Cancellable {
        
        private final RepositoryRevision.Event header;
        private final String revision1;
        private final String revision2;
        private boolean showLastDifference;
        private volatile boolean cancelled;

        public ShowDiffTask(RepositoryRevision.Event header, String revision1, String revision2, boolean showLastDifference) {
            this.header = header;
            this.revision1 = revision1;
            this.revision2 = revision2;
            this.showLastDifference = showLastDifference;
        }

        public void run() { 
            final Diff diff = Diff.getDefault();
            final DiffStreamSource s1 = new DiffStreamSource(header.getFile(), null, revision1, revision1);
            final DiffStreamSource s2 = new DiffStreamSource(header.getFile(), null, revision2, revision2);

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
            return true;
        }
    }
    
    public JComponent getComponent() {
        return diffView;
    }
}


