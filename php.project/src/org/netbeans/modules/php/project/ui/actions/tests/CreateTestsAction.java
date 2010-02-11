/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.actions.tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.modules.php.project.phpunit.PhpUnit;
import org.netbeans.modules.php.project.phpunit.PhpUnit.ConfigFiles;
import org.netbeans.modules.php.project.ui.Utils;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
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
    private static final long serialVersionUID = 952382987542628824L;

    private static final Logger LOGGER = Logger.getLogger(CreateTestsAction.class.getName());

    private static final String REQUIRE_ONCE_TPL = "require_once '%s';"; // NOI18N

    private static final ExecutionDescriptor EXECUTION_DESCRIPTOR
            = new ExecutionDescriptor().controllable(false).frontWindow(false);
    private static final RequestProcessor RP = new RequestProcessor("Generate PHP Unit tests", 1); // NOI18N
    static final Queue<Runnable> RUNNABLES = new ConcurrentLinkedQueue<Runnable>();
    private static final RequestProcessor.Task TASK = RP.create(new Runnable() {
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
        final PhpUnit phpUnit = CommandUtils.getPhpUnit(true);
        if (phpUnit == null) {
            return;
        }
        // ensure that test sources directory exists
        final PhpProject phpProject = PhpProjectUtils.getPhpProject(activatedNodes[0]);
        assert phpProject != null : "PHP project must be found for " + activatedNodes[0];
        if (ProjectPropertiesSupport.getTestDirectory(phpProject, true) == null) {
            return;
        }

        if (!Utils.validatePhpUnitForProject(phpUnit, phpProject)) {
            return;
        }

        RUNNABLES.add(new Runnable() {
            public void run() {
                ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(CreateTestsAction.class, "LBL_CreatingTests"));
                handle.start();
                try {
                    LifecycleManager.getDefault().saveAll();
                    generateTests(activatedNodes, phpUnit, phpProject);
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

    void generateTests(final Node[] activatedNodes, final PhpUnit phpUnit, final PhpProject phpProject) {
        assert phpProject != null;

        final List<FileObject> files = CommandUtils.getFileObjects(activatedNodes);
        assert !files.isEmpty() : "No files for tests?!";

        final Set<FileObject> proceeded = new HashSet<FileObject>();
        final Set<FileObject> failed = new HashSet<FileObject>();
        final Set<File> toOpen = new HashSet<File>();
        FileUtil.runAtomicAction(new Runnable() {
            public void run() {
                try {
                    final PhpVisibilityQuery phpVisibilityQuery = PhpVisibilityQuery.forProject(phpProject);
                    for (FileObject fo : files) {
                        generateTest(phpUnit, phpProject, phpVisibilityQuery, fo, proceeded, failed, toOpen);
                        Enumeration<? extends FileObject> children = fo.getChildren(true);
                        while (children.hasMoreElements()) {
                            generateTest(phpUnit, phpProject, phpVisibilityQuery, children.nextElement(), proceeded, failed, toOpen);
                        }
                    }
                } catch (ExecutionException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                    UiUtils.processExecutionException(ex);
                }
            }
        });

        if (!failed.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (FileObject file : failed) {
                sb.append(file.getNameExt());
                sb.append("\n"); // NOI18N
            }
            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(
                    NbBundle.getMessage(CreateTestsAction.class, "MSG_TestNotGenerated", sb.toString()), NotifyDescriptor.WARNING_MESSAGE));
        }

        Set<File> toRefresh = new HashSet<File>();
        for (File file : toOpen) {
            assert file.isFile() : "File must be given to open: " + file;
            toRefresh.add(file.getParentFile());
            try {
                FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
                assert fo != null : "File object not found for " + file;
                assert fo.isValid() : "File object not valid for " + file;
                DataObject dobj = DataObject.find(fo);
                EditorCookie ec = dobj.getCookie(EditorCookie.class);
                ec.open();
            } catch (DataObjectNotFoundException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }

        if (!toRefresh.isEmpty()) {
            FileUtil.refreshFor(toRefresh.toArray(new File[toRefresh.size()]));
        }
    }

    private void generateTest(PhpUnit phpUnit, PhpProject phpProject, PhpVisibilityQuery phpVisibilityQuery, FileObject sourceFo,
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

        final ConfigFiles configFiles = PhpUnit.getConfigFiles(phpProject, false);
        final String paramSkeleton = PhpUnit.hasValidVersion(phpUnit) ? PhpUnit.PARAM_SKELETON : PhpUnit.PARAM_SKELETON_OLD;
        final File sourceFile = FileUtil.toFile(sourceFo);
        final File parent = FileUtil.toFile(sourceFo.getParent());
        final File workingDirectory = phpUnit.getWorkingDirectory(configFiles, parent);

        // find out the name of a class(es)
        EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
        assert editorSupport != null : "Editor support must exist";
        Collection<PhpClass> classes = editorSupport.getClasses(sourceFo);
        if (classes.size() == 0) {
            // run phpunit in order to have some output
            generateSkeleton(phpUnit, configFiles, sourceFo.getName(), sourceFo, workingDirectory, paramSkeleton);
            failed.add(sourceFo);
            return;
        }
        for (PhpClass phpClass : classes) {
            final String className = phpClass.getName();
            final File testFile = getTestFile(phpProject, sourceFo, className);
            if (testFile.isFile()) {
                // already exists
                toOpen.add(testFile);
                continue;
            }
            final File generatedFile = getGeneratedFile(className, parent);

            // test does not exist yet
            Future<Integer> result = generateSkeleton(phpUnit, configFiles, phpClass.getFullyQualifiedName(), sourceFo, workingDirectory, paramSkeleton);
            try {
                if (result.get() != 0) {
                    // test not generated
                    failed.add(sourceFo);
                    continue;
                }
                File moved = moveAndAdjustGeneratedFile(generatedFile, testFile, sourceFile);
                if (moved == null) {
                    failed.add(sourceFo);
                } else {
                    toOpen.add(moved);
                }
            } catch (InterruptedException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
    }

    private Future<Integer> generateSkeleton(PhpUnit phpUnit, ConfigFiles configFiles, String className, FileObject sourceFo, File workingDirectory, String paramSkeleton) {
        // test does not exist yet
        ExternalProcessBuilder externalProcessBuilder = phpUnit.getProcessBuilder()
                .workingDirectory(workingDirectory);

        // #179960
        if (configFiles.bootstrap != null
                && configFiles.useBootstrapForCreateTests) {
            externalProcessBuilder = externalProcessBuilder
                    .addArgument(PhpUnit.PARAM_BOOTSTRAP)
                    .addArgument(configFiles.bootstrap.getAbsolutePath());
        }
        if (configFiles.configuration != null) {
            externalProcessBuilder = externalProcessBuilder
                    .addArgument(PhpUnit.PARAM_CONFIGURATION)
                    .addArgument(configFiles.configuration.getAbsolutePath());
        }

        // http://www.phpunit.de/ticket/904
        if (className.startsWith("\\")) { // NOI18N
            className = className.substring(1);
        }

        externalProcessBuilder = externalProcessBuilder
                .addArgument(paramSkeleton)
                .addArgument(className)
                .addArgument(FileUtil.toFile(sourceFo).getAbsolutePath());
        ExecutionService service = ExecutionService.newService(externalProcessBuilder, EXECUTION_DESCRIPTOR,
                String.format("%s %s %s %s", phpUnit.getProgram(), paramSkeleton, className, sourceFo.getNameExt())); // NOI18N
        return service.run();
    }

    private File getGeneratedFile(String className, File parent) {
        return new File(parent, className + PhpUnit.TEST_FILE_SUFFIX);
    }

    private File getTestDirectory(PhpProject phpProject) {
        FileObject testDirectory = ProjectPropertiesSupport.getTestDirectory(phpProject, false);
        assert testDirectory != null && testDirectory.isValid() : "Valid folder for tests must be found for " + phpProject;
        return FileUtil.toFile(testDirectory);
    }

    private File getTestFile(PhpProject project, FileObject source, String className) {
        assert project != null;
        assert source != null;

        FileObject sourcesDirectory = ProjectPropertiesSupport.getSourcesDirectory(project);
        String relativeSourcePath = FileUtil.getRelativePath(sourcesDirectory, source.getParent());
        assert relativeSourcePath != null : String.format("Relative path must be found for sources %s and folder %s", sourcesDirectory, source.getParent());

        File relativeTestDirectory = new File(getTestDirectory(project), relativeSourcePath.replace('/', File.separatorChar)); // NOI18N

        return new File(relativeTestDirectory, className + PhpUnit.TEST_FILE_SUFFIX);
    }

    private File moveAndAdjustGeneratedFile(File generatedFile, File testFile, File sourceFile) {
        assert generatedFile.isFile() : "Generated files must exist: " + generatedFile;
        assert !testFile.exists() : "Test file cannot exist: " + testFile;

        // create all the parents
        try {
            FileUtil.createFolder(testFile.getParentFile());
        } catch (IOException exc) {
            // what to do now??
            LOGGER.log(Level.WARNING, null, exc);
            return generatedFile;
        }

        testFile = adjustFileContent(generatedFile, testFile, sourceFile, PhpUnit.getRequireOnce(testFile, sourceFile));
        if (testFile == null) {
            return null;
        }
        assert testFile.isFile() : "Test file must exist: " + testFile;

        // reformat the file
        try {
            PhpProjectUtils.reformatFile(testFile);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Cannot reformat file " + testFile, ex);
        }

        return testFile;
    }

    private File adjustFileContent(File generatedFile, File testFile, File sourceFile, String requireOnce) {
        try {
            // input
            BufferedReader in = new BufferedReader(new FileReader(generatedFile));

            try {
                // output
                BufferedWriter out = new BufferedWriter(new FileWriter(testFile));

                try {
                    String line;
                    boolean requireWritten = false;
                    String filename = sourceFile.getName();
                    while ((line = in.readLine()) != null) {
                        if (!requireWritten && PhpUnit.isRequireOnceSourceFile(line.trim(), filename)) {
                            // original require generated by phpunit
                            out.write(String.format(REQUIRE_ONCE_TPL, requireOnce).replace("''.", "")); // NOI18N
                            requireWritten = true;
                        } else {
                            out.write(line);
                        }
                        out.newLine();
                    }
                } finally {
                    out.flush();
                    out.close();
                }
            } finally {
                in.close();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return null;
        }

        if (!generatedFile.delete()) {
            LOGGER.info("Cannot delete generated file " + generatedFile);
        }
        return testFile;
    }
}
