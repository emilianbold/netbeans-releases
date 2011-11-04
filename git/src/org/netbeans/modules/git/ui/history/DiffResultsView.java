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
package org.netbeans.modules.git.ui.history;

import org.netbeans.libs.git.GitException;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.ErrorManager;
import org.netbeans.modules.versioning.util.NoContentPanel;
import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;
import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.*;
import java.awt.Component;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.diff.DiffController;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.diff.DiffStreamSource;

/**
 * Shows Search History results in a table with Diff pane below it.
 * 
 * @author Maros Sandor
 */
class DiffResultsView implements AncestorListener, PropertyChangeListener {

    protected final SearchHistoryPanel parent;

    protected DiffTreeTable treeView;
    private JSplitPane    diffView;
    
    protected GitProgressSupport            currentTask;
    
    protected DiffController            currentDiff;
    private int                     currentDifferenceIndex;
    protected int                     currentIndex;
    private boolean                 dividerSet;
    protected List<RepositoryRevision> results;
    private static final RequestProcessor rp = new RequestProcessor("GitDiff", 1, true);  // NOI18N
    private static final Logger LOG = Logger.getLogger(DiffResultsView.class.getName());

    public DiffResultsView (SearchHistoryPanel parent, List<RepositoryRevision> results) {
        this.parent = parent;
        this.results = results;
        treeView = new DiffTreeTable(parent);
        treeView.setResults(results);
        treeView.addAncestorListener(this);

        diffView = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        diffView.setTopComponent(treeView);
        setBottomComponent(new NoContentPanel(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_NoRevisions"))); // NOI18N
    }

    @Override
    public void ancestorAdded(AncestorEvent event) {
        ExplorerManager em = ExplorerManager.find(treeView);
        em.addPropertyChangeListener(this);
        if (!dividerSet) {
            SwingUtilities.invokeLater(new Runnable() {
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
        ExplorerManager em = ExplorerManager.find(treeView);
        em.removePropertyChangeListener(this);
    }

    @Override
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
                @Override
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
                            if (r1 == null || revOlder == null || revOlder.getFile() == null || !(revOlder.getFile().equals(r1.getFile()) || revOlder.getRenames().contains(r1.getFile()))) {
                                throw new Exception();
                            }
                            showDiff(r1, revOlder, r1, false);
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

    protected final void setBottomComponent(Component component) {
        assert EventQueue.isDispatchThread();
        int dl = diffView.getDividerLocation();
        diffView.setBottomComponent(component);
        diffView.setDividerLocation(dl);
    }

    protected GitProgressSupport createShowDiffTask(RepositoryRevision.Event header, RepositoryRevision.Event revision1, RepositoryRevision.Event revision2, boolean showLastDifference) {
        return new ShowDiffTask(header, revision1, revision2, showLastDifference);
    }

    /**
     *
     * @param header
     * @param revision1 if null then parent revision will be used
     * @param revision2
     * @param showLastDifference
     */
    protected void showDiff(RepositoryRevision.Event header, RepositoryRevision.Event revision1, RepositoryRevision.Event revision2, boolean showLastDifference) {
        synchronized(this) {
            cancelBackgroundTasks();
            char action = header.getAction();
            if (action == 'M') {
                currentTask = createShowDiffTask(header, revision1, revision2, showLastDifference);
            } else if(action == 'A') {
                currentTask = createShowDiffTask(header, null, revision2, showLastDifference);
            } else if(action == 'D') {
                currentTask = createShowDiffTask(header, revision1, null, showLastDifference);
            } else if(action == 'C') {
                currentTask = createShowDiffTask(header, null, revision2, showLastDifference);
            } else {
                currentTask = createShowDiffTask(header, revision1, revision2, showLastDifference);
            }
            currentTask.start(rp, parent.getRepository(), NbBundle.getMessage(DiffResultsView.class, "LBL_SearchHistory_Diffing"));
        }
    }

    synchronized void cancelBackgroundTasks () {
        if (currentTask != null) {
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
        showDiff(rev, null, rev, showLastDifference);
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
        outer:
        for(File root : roots) {
            for(RepositoryRevision.Event evt : revs) {
                if (root.equals(evt.getFile()) || evt.getRenames().contains(root)) {
                    event = evt;
                    break outer;
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
                parent.updateActions();
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
                parent.updateActions();
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

    private class ShowDiffTask extends GitProgressSupport {
        
        private final RepositoryRevision.Event header;
        private RepositoryRevision.Event revision1;
        private final RepositoryRevision.Event revision2;
        private boolean showLastDifference;
        private DiffStreamSource s1;
        private DiffStreamSource s2;

        public ShowDiffTask(RepositoryRevision.Event header, RepositoryRevision.Event revision1, RepositoryRevision.Event revision2, boolean showLastDifference) {
            this.header = header;
            this.revision1 = revision1;
            this.revision2 = revision2;
            this.showLastDifference = showLastDifference;
        }

        @Override
        public void perform () {
            showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_LoadingDiff")); //NOI18N
            File f = revision1 == null ? header.getFile() : revision1.getFile();
            String revision = revision1 == null ? null : revision1.getLogInfoHeader().getLog().getRevision();
            if (revision1 == null) {
                try {
                    revision = header.getLogInfoHeader().getAncestorCommit(header.getFile(), getClient(), NULL_PROGRESS_MONITOR);
                } catch (GitException ex) {
                    LOG.log(Level.INFO, null, ex);
                }
            }
            if (isCanceled()) {
                return;
            }
            s1 = new DiffStreamSource(f, revision, revision);
            f = revision2 == null ? header.getFile() : revision2.getFile();
            revision = revision2 == null ? null : revision2.getLogInfoHeader().getLog().getRevision();
            s2 = new DiffStreamSource(f, revision, revision);

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
                                        parent.updateActions();
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
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            });
        }

        @Override
        public boolean cancel () {
            if (s1 != null) {
                s1.close();
            }
            if (s2 != null) {
                s2.close();
            }
            return super.cancel();
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


