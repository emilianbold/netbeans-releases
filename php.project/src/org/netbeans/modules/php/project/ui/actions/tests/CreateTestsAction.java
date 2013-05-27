/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.actions.tests;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpProjectValidator;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.ui.customizer.CompositePanelProviderImpl;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
import org.netbeans.modules.php.spi.testing.create.CreateTestsResult;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 * Action for creating new PHP Unit tests.
 * @author Tomas Mysik
 */
public final class CreateTestsAction extends NodeAction {

    private static final long serialVersionUID = -468532132435473111L;

    private static final Logger LOGGER = Logger.getLogger(CreateTestsAction.class.getName());

    private static final RequestProcessor RP = new RequestProcessor("Generate PHP unit tests", 1); // NOI18N
    static final Queue<Runnable> RUNNABLES = new ConcurrentLinkedQueue<>();
    private static final RequestProcessor.Task TASK = RP.create(new Runnable() {
        @Override
        public void run() {
            Runnable toRun = RUNNABLES.poll();
            while (toRun != null) {
                toRun.run();
                toRun = RUNNABLES.poll();
            }
        }
    }, true);

    public CreateTestsAction() {
        putValue("noIconInMenu", true); // NOI18N
    }

    @Override
    public boolean asynchronous() {
        return false;
    }

    @Override
    protected void performAction(final Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return;
        }

        // ensure that test sources directory exists
        final PhpProject phpProject = PhpProjectUtils.getPhpProject(activatedNodes[0]);
        assert phpProject != null : "PHP project must be found for " + activatedNodes[0];

        if (ProjectPropertiesSupport.getTestDirectory(phpProject, true) == null) {
            return;
        }
        List<PhpTestingProvider> testingProviders = phpProject.getTestingProviders();
        if (testingProviders.isEmpty()) {
            PhpProjectUtils.openCustomizer(phpProject, CompositePanelProviderImpl.TESTING);
            return;
        }
        final PhpTestingProvider testingProvider = selectTestingProvider(testingProviders);
        if (testingProvider == null) {
            return;
        }

        RUNNABLES.add(new Runnable() {
            @Override
            public void run() {
                ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(CreateTestsAction.class, "LBL_CreatingTests"));
                handle.start();
                try {
                    LifecycleManager.getDefault().saveAll();
                    generateTests(activatedNodes, phpProject, testingProvider);
                } finally {
                    handle.finish();
                }
            }
        });
        TASK.schedule(0);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return false;
        }

        PhpProject onlyOneProjectAllowed = null;
        for (Node node : activatedNodes) {
            FileObject fileObj = CommandUtils.getFileObject(node);
            if (fileObj == null) {
                return false;
            }

            // only php files or folders allowed
            if (fileObj.isData()
                    && !FileUtils.isPhpFile(fileObj)) {
                return false;
            }

            PhpProject phpProject = PhpProjectUtils.getPhpProject(fileObj);
            if (phpProject == null) {
                return false;
            }
            if (PhpProjectValidator.isFatallyBroken(phpProject)) {
                return false;
            }
            if (onlyOneProjectAllowed == null) {
                onlyOneProjectAllowed = phpProject;
            } else if (!onlyOneProjectAllowed.equals(phpProject)) {
                // tests can be generated only for one project at one time
                return false;
            }

            if (!CommandUtils.isUnderSources(phpProject, fileObj)
                    || CommandUtils.isUnderTests(phpProject, fileObj, false)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(CreateTestsAction.class, "LBL_CreateTests");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    void generateTests(final Node[] activatedNodes, final PhpProject phpProject, final PhpTestingProvider testingProvider) {
        assert phpProject != null;
        assert !EventQueue.isDispatchThread();

        List<FileObject> files = CommandUtils.getFileObjects(activatedNodes);
        assert !files.isEmpty() : "No files for tests?!";
        final List<FileObject> sanitizedFiles = new ArrayList<>(files.size() * 2);
        sanitizeFiles(sanitizedFiles, files, phpProject, PhpVisibilityQuery.forProject(phpProject));
        if (sanitizedFiles.isEmpty()) {
            LOGGER.info("No visible files for creating tests -> exiting.");
            return;
        }

        final Set<FileObject> succeeded = new HashSet<>();
        final Set<FileObject> failed = new HashSet<>();
        final PhpModule phpModule = phpProject.getPhpModule();
        FileUtil.runAtomicAction(new Runnable() {
            @Override
            public void run() {
                CreateTestsResult result = testingProvider.createTests(phpModule, sanitizedFiles);
                succeeded.addAll(result.getSucceeded());
                failed.addAll(result.getFailed());
            }
        });
        showFailures(failed);
        reformat(succeeded);
        open(succeeded);
        refreshTests(ProjectPropertiesSupport.getTestDirectory(phpProject, false));
    }

    private void sanitizeFiles(List<FileObject> sanitizedFiles, List<FileObject> files, PhpProject phpProject, PhpVisibilityQuery phpVisibilityQuery) {
        for (FileObject fo : files) {
            if (fo.isData()
                    && FileUtils.isPhpFile(fo)
                    && !CommandUtils.isUnderTests(phpProject, fo, false)
                    && !CommandUtils.isUnderSelenium(phpProject, fo, false)
                    && PhpProjectUtils.isVisible(phpVisibilityQuery, fo)) {
                sanitizedFiles.add(fo);
            }
            FileObject[] children = fo.getChildren();
            if (children.length > 0) {
                sanitizeFiles(sanitizedFiles, Arrays.asList(children), phpProject, phpVisibilityQuery);
            }
        }
    }

    private void showFailures(Set<FileObject> files) {
        if (files.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder(50);
        for (FileObject file : files) {
            sb.append(file.getNameExt());
            sb.append("\n"); // NOI18N
        }
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(
                NbBundle.getMessage(CreateTestsAction.class, "MSG_TestNotGenerated", sb.toString()), NotifyDescriptor.WARNING_MESSAGE));
    }

    private void reformat(Set<FileObject> files) {
        for (FileObject file : files) {
            try {
                PhpProjectUtils.reformatFile(FileUtil.toFile(file));
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, "Cannot reformat file " + file, ex);
            }
        }
    }

    private void open(Set<FileObject> files) {
        for (FileObject file : files) {
            assert file.isData() : "File must be given to open: " + file;
            PhpProjectUtils.openFile(FileUtil.toFile(file));
        }
    }

    private void refreshTests(final FileObject testDir) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                FileUtil.refreshFor(FileUtil.toFile(testDir));
            }
        });
    }

    @CheckForNull
    private PhpTestingProvider selectTestingProvider(List<PhpTestingProvider> testingProviders) {
        if (testingProviders.size() == 1) {
            return testingProviders.get(0);
        }
        return SelectProviderPanel.open(testingProviders);
    }

}
