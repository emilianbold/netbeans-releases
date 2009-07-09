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

package org.netbeans.modules.bugtracking.vcs;

import java.awt.EventQueue;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.File;
import java.util.logging.Logger;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.BugtrackingOwnerSupport;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import static java.util.logging.Level.FINEST;

/**
 * Loads the list of repositories and determines the default one off the AWT
 * thread. Once the results are ready, updates the UI (the combo-box).
 * It is activated by method {@code hierarchyChanged} when the hook panel
 * is displayed. At this moment, a routine for finding the known repositories
 * and for determination the default repository is started in a separated
 * thread. As soon as the list of known repositories is ready, the repositories
 * combo-box is updated. When the default repository is determined, it is
 * pre-selected in the combo-box, unless the user had already selected some
 * repository.
 *
 * @author  Marian Petras
 */
final class RepositorySelector implements HierarchyListener, Runnable {

    private static final Logger LOG
            = Logger.getLogger(RepositorySelector.class.getName());

    private final HookPanel hookPanel;
    private final File refFile;
    private boolean tooLate;
    private boolean repositoriesDisplayed = false;
    private boolean defaultRepoSelected = false;
    private volatile Repository[] repositories;
    private volatile boolean defaultRepoComputed;
    private volatile Repository defaultRepo;

    static void setup(HookPanel hookPanel, File referenceFile) {
        hookPanel.addHierarchyListener(
                new RepositorySelector(hookPanel, referenceFile));
    }

    private RepositorySelector(HookPanel hookPanel, File refFile) {
        super();
        this.hookPanel = hookPanel;
        this.refFile = refFile;
    }

    public void hierarchyChanged(HierarchyEvent e) {
        if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
            assert e.getChanged() == hookPanel;
            if (hookPanel.isDisplayable()) {
                hookPanelDisplayed();
            } else {
                hookPanelClosed();
            }
        }
    }

    private void hookPanelDisplayed() {
        LOG.finer("hookPanelDisplayed()");                              //NOI18N
        RequestProcessor.getDefault().post(this);
    }

    private void hookPanelClosed() {
        /*
         * The panel had been closed sooner than the default repository has been
         * determined.
         */
        tooLate = true;
        hookPanel.removeHierarchyListener(this);
    }

    public void run() {
        if (RequestProcessor.getDefault().isRequestProcessorThread()) {

            loadRepositories();
            EventQueue.invokeLater(this);

            try {
                findDefaultRepository();
            } finally {
                defaultRepoComputed = true;
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
                LOG.finest(" - too late - the HookPanel has been already closed"); //NOI18N
                return;
            }

            hookPanel.removeHierarchyListener(this);

            boolean repositoriesJustDisplayed = false;
            if (!repositoriesDisplayed) {
                hookPanel.setRepositories(repositories);
                repositoriesJustDisplayed = true;
                repositoriesDisplayed = true;
            }
            if (defaultRepoComputed) {
                if (repositoriesJustDisplayed) {
                    LOG.finest("                      - going also to select the default repository (if any)"); //NOI18N
                }
                try {
                    if (defaultRepo != null) {
                        hookPanel.preselectRepository(defaultRepo);
                    } else {
                        LOG.finest(" - default repository not determined - abort"); //NOI18N
                    }
                } finally {
                    defaultRepoSelected = true;
                }
            }
        }
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
