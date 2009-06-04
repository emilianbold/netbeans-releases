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
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import static java.util.logging.Level.FINEST;

/**
 * Determines the default issue tracking repository for the
 * {@code HookPanel} and selects it in the panel's combo-box.
 * It is activated by method {@code hierarchyChanged} when the hook panel
 * is displayed. At this moment, a routine for finding the default
 * repository is started in a separated thread. Once the routine finishes,
 * the determined default repository is selected in the repository
 * combo-box in the {@code HookPanel}, unless the user has already selected
 * one.
 *
 * @author  Marian Petras
 */
final class DefaultRepositorySelector implements HierarchyListener, Runnable {

    private static final Logger LOG
            = Logger.getLogger(DefaultRepositorySelector.class.getName());

    private final HookPanel hookPanel;
    private final File refFile;
    private boolean tooLate;
    private transient Repository defaultRepo;

    static void setup(HookPanel hookPanel, File referenceFile) {
        hookPanel.addHierarchyListener(
                new DefaultRepositorySelector(hookPanel, referenceFile));
    }

    private DefaultRepositorySelector(HookPanel hookPanel, File refFile) {
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
            findDefaultRepository();
        } else {
            assert EventQueue.isDispatchThread();

            LOG.finest("run() called from AWT - going to select the repository"); //NOI18N

            if (tooLate) {
                LOG.finest(" - too late - the HookPanel has been already closed"); //NOI18N
                return;
            }

            hookPanel.removeHierarchyListener(this);

            if (defaultRepo == null) {
                LOG.finest(" - default repository not determined - abort"); //NOI18N
                return;
            }

            hookPanel.preselectRepository(defaultRepo);
        }
    }

    private void findDefaultRepository() {
        assert RequestProcessor.getDefault().isRequestProcessorThread();
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
            EventQueue.invokeLater(this);
        } else {
            LOG.finest(" - default repository: <null>");                //NOI18N
        }
    }
}
