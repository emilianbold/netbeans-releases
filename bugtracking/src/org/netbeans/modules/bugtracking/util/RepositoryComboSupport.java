/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.util;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import static java.awt.event.HierarchyEvent.DISPLAYABILITY_CHANGED;
import static java.util.logging.Level.FINEST;

/**
 * Loads the list of repositories and determines the default one off the AWT
 * thread. Once the results are ready, updates the UI (the combo-box).
 * It is activated by method {@code hierarchyChanged} when the component
 * containing the repository ComboBox is displayed. At this moment, a routine
 * for finding the known repositories  and for determination the default
 * repository is started in a separated thread. As soon as the list of known
 * repositories is ready, the repositories combo-box is updated. When the
 * default repository is determined, it is pre-selected in the combo-box,
 * unless the user had already selected some repository.
 *
 * @author  Marian Petras
 * @author  Tomas Stupka
 */
public final class RepositoryComboSupport implements ItemListener, Runnable {

    static final String LOADING_REPOSITORIES = "loading";               //NOI18N
    static final String NO_REPOSITORIES = "no repositories";            //NOI18N
    static final String SELECT_REPOSITORY = "select";                   //NOI18N

    private static final Logger LOG = Logger.getLogger(RepositoryComboSupport.class.getName());

    private final JComboBox comboBox;
    private final RepositoryComboModel comboBoxModel;
    private final File refFile;
    private final Node[] selectedNodes;
    private DisplayabilityListener displayabilityListener;
    private boolean shutdown;
    private boolean repositoriesDisplayed = false;
    private boolean defaultRepoSelected = false;
    private volatile Repository[] repositories;
    private volatile boolean defaultRepoComputationPending;
    private boolean preselectSingleRepo = false;
    private volatile Repository defaultRepo;

    /**
     * Setups the given repository with RepositoryComboRenderer. As soon as the component holding the combo
     * is displayed a list of all known repositories is retrieved and the first in the list is selected
     *
     * @param component component with the combobox
     * @param comboBox repository combobox
     * @param selectRepoIfSingle  if {@code true} and there is only one repository
     *                        known, then the (only) repository is preselected;
     *                        otherwise no repository is selected in the combo
     * @return
     */
    public static RepositoryComboSupport setup(JComponent component, JComboBox comboBox, boolean selectRepoIfSingle) {
        assert EventQueue.isDispatchThread();

        Node[] selectedNodes = TopComponent.getRegistry().getCurrentNodes();
        if (selectedNodes == null) {
            selectedNodes = new Node[0];
        }

        RepositoryComboSupport repositoryComboSupport
                = new RepositoryComboSupport(comboBox, (Repository) null,
                                                       (File) null,
                                                       selectedNodes);
        repositoryComboSupport.preselectSingleRepo = selectRepoIfSingle;
        repositoryComboSupport.activate(component);
        return repositoryComboSupport;
    }

    /**
     * Setups the given repository with RepositoryComboRenderer. As soon as the component holding the combo
     * is displayed a list of all known repositories is retrieved and either the default repository will be
     * selected or nothing if it's not between the known repositories
     *
     * @param component component with the combobox
     * @param comboBox repository combobox
     * @param defaultRepo the repository to be selected
     * @return
     */
    public static RepositoryComboSupport setup(JComponent component, JComboBox comboBox, Repository defaultRepo) {
        if (defaultRepo == null) {
            throw new IllegalArgumentException("default repository must be specified"); //NOI18N
        }

        RepositoryComboSupport repositoryComboSupport
                = new RepositoryComboSupport(comboBox, defaultRepo,
                                                       (File) null,
                                                       (Node[]) null);
        repositoryComboSupport.activate(component);
        return repositoryComboSupport;
    }

    /**
     * Setups the given repository with RepositoryComboRenderer. As soon as the component holding the combo
     * is displayed a list of all known repositories is retrieved and either the repository associated with
     * referenceFile will be selected or nothing if there is no such repository
     *
     * @param component component with the combobox
     * @param comboBox repository combobox
     * @param referenceFile file associated with a repository
     * @return
     */
    public static RepositoryComboSupport setup(JComponent component, JComboBox comboBox, File referenceFile) {
        if (referenceFile == null) {
            throw new IllegalArgumentException("reference file must be specified"); //NOI18N
        }

        RepositoryComboSupport repositoryComboSupport
                = new RepositoryComboSupport(comboBox, (Repository) null,
                                                       referenceFile,
                                                       (Node[]) null);
        repositoryComboSupport.activate(component);
        return repositoryComboSupport;
    }

    private RepositoryComboSupport(JComboBox comboBox, Repository defaultRepo,
                                                       File refFile,
                                                       Node[] selectedNodes) {
        assert EventQueue.isDispatchThread();

        checkJustOneSpecified(defaultRepo, refFile, selectedNodes);

        checkOldComboBoxModel(comboBox);

        this.comboBox = comboBox;
        this.comboBox.setModel(comboBoxModel = new RepositoryComboModel());
        this.comboBox.setRenderer(new RepositoryComboRenderer());
        this.defaultRepo = defaultRepo;
        this.refFile = refFile;
        this.selectedNodes = selectedNodes;

        defaultRepoComputationPending = (defaultRepo == null);

        setComboBoxData(new Object[] {LOADING_REPOSITORIES});
    }

    private void checkOldComboBoxModel(JComboBox comboBox) {
        ComboBoxModel oldModel = comboBox.getModel();
        if ((oldModel != null) && (oldModel.getSize() != 0)) {
            throw new IllegalStateException("The combo-box must be empty."); //NOI18N
        }
    }

    private void setComboBoxData(Object[] data) {
        comboBox.setPopupVisible(false);
        comboBoxModel.setData(data);
    }

    private void checkJustOneSpecified(Object... items) {
        boolean oneSpecifed = false;
        for (Object item : items) {
            if (item == null) {
                continue;
            }
            if (oneSpecifed) {
                throw new IllegalArgumentException("At most one item may be specified."); //NOI18N
            }
            oneSpecifed = true;
        }
        if (!oneSpecifed) {
            throw new IllegalArgumentException("At least one item must be specified."); //NOI18N
        }
    }

    /**
     * Activates the mechanism of the bugtracking repository combo-box.
     * If a non-null component is passed as an argument, a trigger is set up
     * such that loading of bugtracking repositories is started as soon as
     * the given component becomes displayable. Otherwise, loading is started
     * immediately.
     *
     * @param  triggerComponent  component whose displayability should activate
     *                           the combo-box, or {@code null}
     * @exception  java.lang.IllegalStateException
     *             if the given component is already displayable
     */
    private void activate(Component triggerComponent) {
        assert EventQueue.isDispatchThread();

        if (triggerComponent != null) {
            setupDisplayabilityTrigger(triggerComponent);
        } else {
            start();
        }
    }

    private void start() {
        assert EventQueue.isDispatchThread();

        RequestProcessor.getDefault().post(this);
    }

    private void shutdown() {
        assert EventQueue.isDispatchThread();

        shutdownDisplayabilityTrigger();
        comboBox.removeItemListener(this);

        shutdown = true;
    }

    private void setupDisplayabilityTrigger(final Component triggerComponent) {
        assert EventQueue.isDispatchThread();

        if (triggerComponent.isDisplayable()) {
            throw new IllegalStateException(
                    "The trigger component must not be initially displayable.");//NOI18N
        }

        displayabilityListener = new DisplayabilityListener(triggerComponent);
        triggerComponent.addHierarchyListener(displayabilityListener);
    }

    private void shutdownDisplayabilityTrigger() {
        assert EventQueue.isDispatchThread();

        if (displayabilityListener != null) {
            displayabilityListener.dispose();
            displayabilityListener = null;
        }
    }

    private final class DisplayabilityListener implements HierarchyListener {
        private final Component triggerComponent;
        private DisplayabilityListener(Component triggerComponent) {
            this.triggerComponent = triggerComponent;
        }
        public void hierarchyChanged(HierarchyEvent e) {
            if ((e.getChangeFlags() & DISPLAYABILITY_CHANGED) == 0) {
                return;
            }

            assert e.getChanged() == triggerComponent;

            if (triggerComponent.isDisplayable()) {
                LOG.finer("triggerComponentDisplayed()");               //NOI18N
                RepositoryComboSupport.this.start();
            } else {
                LOG.finer("triggerComponentClosed()");                  //NOI18N
                RepositoryComboSupport.this.shutdown();
            }
        }
        private void dispose() {
            triggerComponent.removeHierarchyListener(this);
        }
    }

    /**
     * This is called when an issue tracker is selected/deselected in the
     * Issue Tracker combo-box (at the top of the Issue Window).
     * This method causes that if item "Select Issue Tracker" is selected
     * in the combo-box and the user selects a real issue tracker (repository),
     * the item "Select Issue Tracker" is removed.
     * 
     * @param  e  {@code ItemEvent} that holds information about the
     *            (de)selection event
     */
    public void itemStateChanged(ItemEvent e) {
        if ((e.getStateChange() == ItemEvent.DESELECTED)
                && (e.getItem() == SELECT_REPOSITORY)
                && (comboBox.getItemAt(0) == SELECT_REPOSITORY)) {
            if (comboBox.isPopupVisible()) {

                /*
                 * We do not want to remove item "Select repository..."
                 * while the popup is visible. Only remove it once the user
                 * closes the popup with another item selected.
                 */

                /*
                 * Do not respond to item selection changes while the popup
                 * is visible:
                 */
                comboBox.removeItemListener(this);

                comboBox.addPopupMenuListener(new PopupMenuListener() {
                    public void popupMenuCanceled(PopupMenuEvent e) {
                        comboBox.removePopupMenuListener(this);

                        /* Restore the item selection listener: */
                        comboBox.addItemListener(RepositoryComboSupport.this);
                    }
                    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                        comboBox.removePopupMenuListener(this);

                        /* Restore the item selection listener: */
                        comboBox.addItemListener(RepositoryComboSupport.this);
                    }
                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                        comboBox.removePopupMenuListener(this);
                        if (comboBox.getSelectedItem() != SELECT_REPOSITORY) {
                            comboBoxModel.removeElementAt(0);
                        } else {
                            /* Restore the item selection listener: */
                            comboBox.addItemListener(RepositoryComboSupport.this);
                        }
                    }
                });
            } else {
                comboBoxModel.removeElementAt(0);
                comboBox.removeItemListener(this);
            }
        }
    }

    public void run() {
        if (RequestProcessor.getDefault().isRequestProcessorThread()) {
            loadRepositories();

            if ((defaultRepoComputationPending)
                    && (repositories.length == 1)
                    && preselectSingleRepo) {
                defaultRepo = repositories[0];
                defaultRepoComputationPending = false;
            }

            /* schedule display of list of repositories */
            EventQueue.invokeLater(this);

            if (defaultRepoComputationPending) {
                try {
                    findDefaultRepository();
                } finally {
                    defaultRepoComputationPending = false;
                }
                if (defaultRepo != null) {
                    /* schedule selection of default repository (if any) */
                    EventQueue.invokeLater(this);
                }
            }
        } else {
            doGuiJob();
        }
    }

    private void doGuiJob() {
        assert EventQueue.isDispatchThread();

        if (repositoriesDisplayed && defaultRepoSelected) {
            /*
             * The default repository selection was performed during the
             * previous invocation of this method (in one shot with displaying
             * the list of available repositories).
             */
            LOG.finest("run() called from AWT - nothing to do - all work already done"); //NOI18N
            return;
        }

        if (shutdown) {
            LOG.finest(" - too late - the component has been already closed"); //NOI18N
            return;
        }

        shutdownDisplayabilityTrigger();

        if (!repositoriesDisplayed) {
            displayRepositories();
        } else {
            selectDefaultRepository();
        }
    }

    private void displayRepositories() {
        boolean computationPending = defaultRepoComputationPending;
        Repository knownDefaultRepo = computationPending ? null : defaultRepo;

        LOG.finest("going to display the list of repositories");        //NOI18N
        if ((knownDefaultRepo != null) && (LOG.isLoggable(FINEST))) {
            LOG.finest("  - default repository: "                       //NOI18N
                       + knownDefaultRepo.getDisplayName());
        }
        try {
            setRepositories(repositories, knownDefaultRepo);
        } finally {
            repositoriesDisplayed = true;
            defaultRepoSelected = !computationPending;
        }
    }

    private void selectDefaultRepository() {
        assert (!defaultRepoComputationPending) && (defaultRepo != null);
        /*
         * We are going to preselect the default repository.
         */
        LOG.finest("going to select the default repository");           //NOI18N
        try {
            if ((comboBox.getSelectedItem() instanceof Repository)
                    && !comboBox.isPopupVisible()) {
                /*
                 * the user has already selected some item - do not override it
                 */
            } else {
                preselectRepository(defaultRepo);
            }
        } finally {
            defaultRepoSelected = true;
        }
    }

    /**
     * Selects the given repository in the combo-box if no repository has been
     * selected yet by the user.
     * If the user had already selected some repository before this method
     * was called, this method does nothing. If this method is called at
     * the moment the popup of the combo-box is opened, the operation of
     * pre-selecting the repository is deferred until the popup is closed. If
     * the popup had been displayed at the moment this method was called
     * and the user selects some repository during the period since the
     * call of this method until the deferred selection takes place, the
     * deferred selection operation is cancelled.
     *
     * @param  repoToPreselect  repository to preselect
     */
    private void preselectRepository(final Repository repoToPreselect) {
        assert EventQueue.isDispatchThread();

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("preselectRepository(" + repoToPreselect.getDisplayName() + ')'); //NOI18N
        }

        if (comboBox.isPopupVisible()) {
            LOG.finest(" - the popup is visible - deferred");           //NOI18N
            comboBox.addPopupMenuListener(new PopupMenuListener() {
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) { }
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    LOG.finer("popupMenuWillBecomeInvisible()");        //NOI18N
                    comboBox.removePopupMenuListener(this);
                }
                public void popupMenuCanceled(PopupMenuEvent e) {
                    LOG.finer("popupMenuCanceled()");                   //NOI18N
                    comboBox.removePopupMenuListener(this);
                    LOG.finest(" - processing deferred selection");     //NOI18N
                    preselectItemUnconditionally(repoToPreselect);
                }
            });
        } else {
            preselectItemUnconditionally(repoToPreselect);
        }
    }

    private void preselectItemUnconditionally(Object item) {
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("preselectItemUnconditionally(" + getItemName(item) + ')'); //NOI18N
        }

        comboBox.setSelectedItem(item);
    }

    private static String getItemName(Object item) {
        if (item instanceof Repository) {
            return ((Repository) item).getDisplayName();
        } else{
            return item.toString();
        }
    }

    private void setRepositories(Repository[] repos,
                                 Repository knownDefaultRepository) {
        assert EventQueue.isDispatchThread();

        int reposCount = (repos != null) ? repos.length : 0;
        Object[] comboData;

        int startIndex = 0;
        if (reposCount == 0) {
            comboData = new Object[] {NO_REPOSITORIES};
        } else if (knownDefaultRepository == null) {
            comboData = new Object[reposCount + 1];
            comboData[startIndex++] = SELECT_REPOSITORY;
        } else {
            comboData = new Object[reposCount];
        }
        if (reposCount != 0) {
            System.arraycopy(repos, 0, comboData, startIndex, reposCount);
        }

        setComboBoxData(comboData);
        if (knownDefaultRepository != null) {
            comboBox.setSelectedItem(knownDefaultRepository);
        } else if (reposCount != 0) {
            assert (comboBox.getSelectedItem() == SELECT_REPOSITORY);
            comboBox.addItemListener(this);
        }
    }

    private void refreshComboBoxData(Repository[] repos) {
        setComboBoxData((repos.length == 0) ? new Object[] {NO_REPOSITORIES}
                                            : repos);
    }

    public void refreshRepositoryModel() {
        LOG.finer("refreshRepositoryModel()");                          //NOI18N
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                if (RequestProcessor.getDefault().isRequestProcessorThread()) {
                    loadRepositories();
                    EventQueue.invokeLater(this);
                } else {
                    assert EventQueue.isDispatchThread();
                    refreshComboBoxData(repositories);
                }
            }
        });
    }

    private void loadRepositories() {
        assert RequestProcessor.getDefault().isRequestProcessorThread();
        LOG.finer("loadRepositories()");                                //NOI18N

        long startTimeMillis = System.currentTimeMillis();

        repositories = BugtrackingUtil.getKnownRepositories();

        long endTimeMillis = System.currentTimeMillis();
        if (LOG.isLoggable(FINEST)) {
            LOG.finest("BugtrackingUtil.getKnownRepositories() took "   //NOI18N
                       + (endTimeMillis - startTimeMillis) + " ms.");   //NOI18N
        }
    }

    private void findDefaultRepository() {
        assert RequestProcessor.getDefault().isRequestProcessorThread();
        LOG.finer("findDefaultRepository()");                           //NOI18N

        long startTimeMillis, endTimeMillis;
        Repository result;

        startTimeMillis = System.currentTimeMillis();

        assert (refFile == null) ^ (selectedNodes == null);

        if (refFile != null) {
            result = BugtrackingOwnerSupport.getInstance().getRepository(refFile,
                                                                         false);
        } else {
            assert (selectedNodes != null);
            result = BugtrackingOwnerSupport.getInstance().getRepository(selectedNodes);
        }

        endTimeMillis = System.currentTimeMillis();

        if (LOG.isLoggable(FINEST)) {
            LOG.finest("BugtrackingOwnerSupport.getRepository(...) took " //NOI18N
                       + (endTimeMillis - startTimeMillis) + " ms.");   //NOI18N
        }

        if (result != null) {
            if (LOG.isLoggable(FINEST)) {
                LOG.finest(" - default repository: " + result.getDisplayName()); //NOI18N
            }
            defaultRepo = result;
        } else {
            LOG.finest(" - default repository: <null>");                //NOI18N
        }
    }
}
