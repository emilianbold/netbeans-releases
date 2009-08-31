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

import java.awt.EventQueue;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.openide.util.RequestProcessor;
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
public final class RepositoryComboSupport implements HierarchyListener, Runnable {

    static final String LOADING_REPOSITORIES = "loading";       //NOI18N
    private static final Logger LOG = Logger.getLogger(RepositoryComboSupport.class.getName());

    private final JComponent component;
    private final JComboBox comboBox;
    private final File refFile;
    private boolean tooLate;
    private boolean repositoriesDisplayed = false;
    private boolean defaultRepoSelected = false;
    private volatile Repository[] repositories;
    private volatile boolean defaultRepoAvailable;
    private boolean preselectFirst = false;
    private volatile Repository defaultRepo;

    /**
     * Setups the given repository with RepositoryComboRenderer. As soon as the component holding the combo
     * is displayed a list of all known repositories is retrieved and the first in the list is selected
     *
     * @param component component with the combobox
     * @param comboBox repository combobox
     * @return
     */
    public static RepositoryComboSupport setup(JComponent component, JComboBox comboBox) {
        RepositoryComboSupport repositoryComboSupport = new RepositoryComboSupport(component, comboBox, null, null);
        repositoryComboSupport.preselectFirst = true;
        component.addHierarchyListener(repositoryComboSupport);
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
        RepositoryComboSupport repositoryComboSupport = new RepositoryComboSupport(component, comboBox, defaultRepo, null);
        component.addHierarchyListener(repositoryComboSupport);
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
        RepositoryComboSupport repositoryComboSupport = new RepositoryComboSupport(component, comboBox, null, referenceFile);
        component.addHierarchyListener(repositoryComboSupport);
        return repositoryComboSupport;
    }

    private RepositoryComboSupport(JComponent panel, JComboBox comboBox, Repository defaultRepo, File refFile) {
        super();     
        this.component = panel;
        this.refFile = refFile;
        this.comboBox = comboBox;
        this.defaultRepo = defaultRepo;
        defaultRepoAvailable = defaultRepo != null;

        comboBox.setModel(new DefaultComboBoxModel(new Object[] {LOADING_REPOSITORIES}));
        comboBox.setRenderer(new RepositoryComboRenderer());

    }

    public void hierarchyChanged(HierarchyEvent e) {
        if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
            assert e.getChanged() == component;
            if (component.isDisplayable()) {
                componentDisplayed();
            } else {
                componentClosed();
            }
        }
    }

    private void componentDisplayed() {
        LOG.finer("componentDisplayed()");                              //NOI18N
        RequestProcessor.getDefault().post(this);
    }

    private void componentClosed() {
        /*
         * The panel had been closed sooner than the default repository has been
         * determined.
         */
        tooLate = true;
        component.removeHierarchyListener(this);
    }

    public void run() {
        if (RequestProcessor.getDefault().isRequestProcessorThread()) {

            loadRepositories();
            EventQueue.invokeLater(this);

            if(defaultRepo == null) {
                try {
                    findDefaultRepository();
                } finally {
                    defaultRepoAvailable = true;
                }
            }
            EventQueue.invokeLater(this);

        } else {
            assert EventQueue.isDispatchThread();
            if (defaultRepoSelected) {
                /*
                 * The default repository selection was performed during the
                 * previous invocation of this method from AWT thread
                 * (in one shot with displaying the list of available
                 * repositories).
                 */
                LOG.finest("run() called from AWT - nothing to do - all work already done"); //NOI18N
                return;
            }

            if (LOG.isLoggable(FINEST)) {
                LOG.finest(!repositoriesDisplayed
                           ? "run() called from AWT - going to display the list of repositories" //NOI18N
                           : "run() called from AWT - going to select the repository"); //NOI18N
            }

            if (tooLate) {
                LOG.finest(" - too late - the component has been already closed"); //NOI18N
                return;
            }

            component.removeHierarchyListener(this);

            boolean repositoriesJustDisplayed = false;
            if (!repositoriesDisplayed) {
                setRepositories(repositories);
                repositoriesJustDisplayed = true;
                repositoriesDisplayed = true;
            }
            if (defaultRepoAvailable) {
                if (repositoriesJustDisplayed) {
                    LOG.finest("                      - going also to select the default repository (if any)"); //NOI18N
                }
                try {
                    if (defaultRepo != null) {
                        preselectRepository(defaultRepo, false);
                    } else if(preselectFirst && comboBox.getModel().getSize() > 0) {
                        Object item = comboBox.getModel().getElementAt(0);
                        if(item != null && item instanceof Repository) {
                            preselectRepository((Repository) item, false);
                        }
                    } else {
                        LOG.finest(" - default repository not determined - abort"); //NOI18N
                    }
                } finally {
                    defaultRepoSelected = true;
                }
            }
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
    private void preselectRepository(final Repository repoToPreselect, boolean onlyIfNotSelected) {
        assert EventQueue.isDispatchThread();

        if (repoToPreselect == null) {
            LOG.finer("preselectRepository(null)");                     //NOI18N
            return;
        }

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("preselectRepository(" + repoToPreselect.getDisplayName() + ')'); //NOI18N
        }

        if (onlyIfNotSelected && isRepositorySelected()) {
            LOG.finest(" - cancelled - already selected by the user");  //NOI18N
            return;
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
                    preselectRepositoryUnconditionally(repoToPreselect);
                }
            });
        } else {
            preselectRepositoryUnconditionally(repoToPreselect);
        }
    }

    private void preselectRepositoryUnconditionally(Repository repoToPreselect) {
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("preselectRepositoryUnconditionally(" + repoToPreselect.getDisplayName() + ')'); //NOI18N
        }

        comboBox.setSelectedItem(repoToPreselect);
    }

    /**
     * Determines whether some bug-tracking repository is selected in the
     * Issue Tracker combo-box.
     *
     * @return  {@code true} if some repository is selected,
     *          {@code false} otherwise
     */
    private boolean isRepositorySelected() {
        Object selectedItem = comboBox.getSelectedItem();
        return (selectedItem != null) && (selectedItem != LOADING_REPOSITORIES);
    }

    private void setRepositories(Repository[] repos) {
        Repository[] comboData;
        if (repos == null) {
            comboData = new Repository[0];
        } else {
            comboData = new Repository[repos.length];
            if (repos.length != 0) {
                System.arraycopy(repos, 0, comboData, 0, repos.length);
            }
        }
        comboBox.setModel(new DefaultComboBoxModel(comboData));
        comboBox.setSelectedItem(null);             // HACK to force itemSeleted evetn after first time selection
    }

    public void refreshRepositoryModel() {
        LOG.finer("refreshRepositoryModel()");
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                DefaultComboBoxModel repoModel;
                loadRepositories();
                final Object item = comboBox.getSelectedItem();
                repoModel = new DefaultComboBoxModel(repositories);
                comboBox.setModel(repoModel);
                if(item != null) {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            Repository lastSelection = null;
                            if(item instanceof Repository) {
                                lastSelection = (Repository) item;
                            }
                            preselectRepository(lastSelection, false);
                        }
                    });
                } else {
                    LOG.finest(" no previous selection available - done"); //NOI18N
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

        if (refFile != null) {
            result = BugtrackingOwnerSupport.getInstance()
                     .getRepository(refFile, false);
            if ((result == null) && LOG.isLoggable(FINEST)) {
                LOG.finest(" could not find issue tracker for " + refFile); //NOI18N
            }
        } else {
            result = BugtrackingOwnerSupport.getInstance()
                    .getRepository(BugtrackingOwnerSupport.ContextType.ALL_PROJECTS);
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
