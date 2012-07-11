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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpProjectValidator;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.phpunit.PhpUnit;
import org.netbeans.modules.php.project.phpunit.PhpUnit.ConfigFiles;
import org.netbeans.modules.php.project.phpunit.PhpUnitSkelGen;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
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

    private static final RequestProcessor RP = new RequestProcessor("Generate PHP Unit tests", 1); // NOI18N
    static final Queue<Runnable> RUNNABLES = new ConcurrentLinkedQueue<Runnable>();
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
        return true;
    }

    @Override
    protected void performAction(final Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return;
        }

        // ensure that test sources directory exists
        final PhpProject phpProject = PhpProjectUtils.getPhpProject(activatedNodes[0]);
        assert phpProject != null : "PHP project must be found for " + activatedNodes[0];

        // programs available?
        PhpUnitSkelGen skelGen = CommandUtils.getPhpUnitSkelGen(false);
        PhpUnit phpUnit = CommandUtils.getPhpUnit(phpProject, false);
        if (skelGen == null && phpUnit == null) {
            // prefer skelGen, show customizer
            CommandUtils.getPhpUnitSkelGen(true);
            return;
        }
        if (ProjectPropertiesSupport.getTestDirectory(phpProject, true) == null) {
            return;
        }

        RUNNABLES.add(new Runnable() {
            @Override
            public void run() {
                ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(CreateTestsAction.class, "LBL_CreatingTests"));
                handle.start();
                try {
                    LifecycleManager.getDefault().saveAll();
                    generateTests(activatedNodes, phpProject);
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

    void generateTests(final Node[] activatedNodes, final PhpProject phpProject) {
        assert phpProject != null;

        final List<FileObject> files = CommandUtils.getFileObjects(activatedNodes);
        assert !files.isEmpty() : "No files for tests?!";

        final Set<FileObject> proceeded = new HashSet<FileObject>();
        final Set<FileObject> failed = new HashSet<FileObject>();
        final Set<File> toOpen = new HashSet<File>();
        FileUtil.runAtomicAction(new Runnable() {
            @Override
            public void run() {
                try {
                    final PhpVisibilityQuery phpVisibilityQuery = PhpVisibilityQuery.forProject(phpProject);
                    for (FileObject fo : files) {
                        generateTest(phpProject, phpVisibilityQuery, fo, proceeded, failed, toOpen);
                        Enumeration<? extends FileObject> children = fo.getChildren(true);
                        while (children.hasMoreElements()) {
                            generateTest(phpProject, phpVisibilityQuery, children.nextElement(), proceeded, failed, toOpen);
                        }
                    }
                } catch (ExecutionException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                    UiUtils.processExecutionException(ex);
                }
            }
        });

        showFailures(failed);
        reformat(toOpen);
        open(toOpen);
    }

    private void generateTest(PhpProject phpProject, PhpVisibilityQuery phpVisibilityQuery, FileObject sourceFo,
            Set<FileObject> proceeded, Set<FileObject> failed, Set<File> toOpen) throws ExecutionException {
        if (sourceFo.isFolder()
                || !FileUtils.isPhpFile(sourceFo)
                || proceeded.contains(sourceFo)
                || CommandUtils.isUnderTests(phpProject, sourceFo, false)
                || CommandUtils.isUnderSelenium(phpProject, sourceFo, false)
                || !PhpProjectUtils.isVisible(phpVisibilityQuery, sourceFo)) {
            return;
        }
        proceeded.add(sourceFo);

        final TestGenerator testGenerator = getTestGenerator(phpProject, sourceFo);

        // find out the name of a class(es)
        EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
        assert editorSupport != null : "Editor support must exist";
        Collection<PhpClass> classes = editorSupport.getClasses(sourceFo);
        if (classes.isEmpty()) {
            failed.add(sourceFo);
            return;
        }
        for (PhpClass phpClass : classes) {
            File testFile = testGenerator.generateTest(phpClass);
            if (testFile != null) {
                toOpen.add(testFile);
            } else {
                // test not generated
                failed.add(sourceFo);
            }
        }
    }

    private TestGenerator getTestGenerator(PhpProject phpProject, FileObject source) {
        ConfigFiles configFiles = PhpUnit.getConfigFiles(phpProject, false);
        PhpUnitSkelGen skelGen = CommandUtils.getPhpUnitSkelGen(false);
        if (skelGen != null) {
            // phpunit-skel-gen is preferred
            LOGGER.log(Level.FINE, "Using phpunit-skel-gen for generating a test for {0}", source.getNameExt());
            return new PhpUnitSkelGenTestGenerator(skelGen, phpProject, source, configFiles);
        }
        LOGGER.log(Level.FINE, "Using phpunit-skel-gen for generating a test for {0}", source.getNameExt());
        PhpUnit phpUnit = CommandUtils.getPhpUnit(phpProject, false);
        File parent = FileUtil.toFile(source.getParent());
        File workingDirectory = phpUnit.getWorkingDirectory(configFiles, parent);
        return new PhpUnitTestGenerator(phpUnit, phpProject, source, configFiles, workingDirectory);
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

    private void reformat(Set<File> files) {
        for (File file : files) {
            try {
                PhpProjectUtils.reformatFile(file);
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, "Cannot reformat file " + file, ex);
            }
        }
    }

    private void open(Set<File> files) {
        Set<File> toRefresh = new HashSet<File>();
        for (File file : files) {
            assert file.isFile() : "File must be given to open: " + file;
            toRefresh.add(file.getParentFile());
            PhpProjectUtils.openFile(file);
        }

        if (!toRefresh.isEmpty()) {
            FileUtil.refreshFor(toRefresh.toArray(new File[toRefresh.size()]));
        }
    }

    //~ Inner classes

    private interface TestGenerator {
        File generateTest(PhpClass phpClass);
    }

    private static final class PhpUnitTestGenerator implements TestGenerator {

        private final PhpUnit phpUnit;
        private final PhpProject phpProject;
        private final FileObject source;
        private final ConfigFiles configFiles;
        private final File workingDirectory;


        public PhpUnitTestGenerator(PhpUnit phpUnit, PhpProject phpProject, FileObject source, ConfigFiles configFiles, File workingDirectory) {
            this.phpUnit = phpUnit;
            this.phpProject = phpProject;
            this.source = source;
            this.configFiles = configFiles;
            this.workingDirectory = workingDirectory;
        }

        @Override
        public File generateTest(PhpClass phpClass) {
            return phpUnit.generateTest(phpProject, configFiles, phpClass, source, workingDirectory);
        }

    }

    private static final class PhpUnitSkelGenTestGenerator implements TestGenerator {

        private final PhpUnitSkelGen skelGen;
        private final PhpProject phpProject;
        private final FileObject source;
        private final ConfigFiles configFiles;


        public PhpUnitSkelGenTestGenerator(PhpUnitSkelGen skelGen, PhpProject phpProject, FileObject source, ConfigFiles configFiles) {
            this.skelGen = skelGen;
            this.phpProject = phpProject;
            this.source = source;
            this.configFiles = configFiles;
        }

        @Override
        public File generateTest(PhpClass phpClass) {
            return skelGen.generateTest(configFiles, phpClass.getFullyQualifiedName(), FileUtil.toFile(source),
                    phpClass.getFullyQualifiedName() + PhpUnit.TEST_CLASS_SUFFIX, PhpUnit.getTestFile(phpProject, source, phpClass.getName()));
        }

    }

}
