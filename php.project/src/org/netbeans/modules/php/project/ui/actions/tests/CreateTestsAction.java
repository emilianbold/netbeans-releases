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
import java.util.LinkedList;
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
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.spi.PhpUnitSupport;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.modules.php.project.util.PhpUnit;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDisplayer;
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

    private static final String PHP_OPEN_TAG = "<?php"; // NOI18N
    private static final String INCLUDE_PATH_TPL = "ini_set(\"include_path\", %sini_get(\"include_path\"));"; // NOI18N
    private static final String INCLUDE_PATH_PART = "\"%s\".PATH_SEPARATOR."; // NOI18N

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
        return false;
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

        RUNNABLES.add(new Runnable() {
            public void run() {
                generateTests(activatedNodes, phpUnit, phpProject);
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
                    && !CommandUtils.isPhpFile(fileObj)) {
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

        List<FileObject> files = CommandUtils.getFileObjects(activatedNodes);
        assert !files.isEmpty() : "No files for tests?!";

        final Set<FileObject> proceeded = new HashSet<FileObject>();
        final Set<FileObject> failed = new HashSet<FileObject>();
        final Set<File> toOpen = new HashSet<File>();
        try {
            for (FileObject fo : files) {
                generateTest(phpUnit, phpProject, fo, proceeded, failed, toOpen);
                Enumeration<? extends FileObject> children = fo.getChildren(true);
                while (children.hasMoreElements()) {
                    generateTest(phpUnit, phpProject, children.nextElement(), proceeded, failed, toOpen);
                }
            }
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
            UiUtils.processExecutionException(ex);
        }

        if (!failed.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (FileObject file : failed) {
                sb.append(file.getNameExt());
                sb.append("\n");
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

    private void generateTest(PhpUnit phpUnit, PhpProject phpProject, FileObject sourceFo,
            Set<FileObject> proceeded, Set<FileObject> failed, Set<File> toOpen) throws ExecutionException {
        if (sourceFo.isFolder()
                || !CommandUtils.isPhpFile(sourceFo)
                || proceeded.contains(sourceFo)) {
            return;
        }
        proceeded.add(sourceFo);

        final String paramSkeleton = phpUnit.supportedVersionFound() ? PhpUnit.PARAM_SKELETON : PhpUnit.PARAM_SKELETON_OLD;
        final File parent = FileUtil.toFile(sourceFo.getParent());

        // find out the name of a class(es)
        PhpUnitSupport phpUnitSupport = Lookup.getDefault().lookup(PhpUnitSupport.class);
        assert phpUnitSupport != null : "PHP unit support must exist";
        Collection<? extends String> classNames = phpUnitSupport.getClassNames(sourceFo);
        if (classNames.size() == 0) {
            // run phpunit in order to have some output
            generateSkeleton(phpUnit, sourceFo.getName(), sourceFo, parent, paramSkeleton);
            failed.add(sourceFo);
            return;
        }
        for (String className : classNames) {
            final File testFile = getTestFile(phpProject, sourceFo, className);
            if (testFile.isFile()) {
                // already exists
                toOpen.add(testFile);
                continue;
            }
            final File generatedFile = getGeneratedFile(className, parent);

            // test does not exist yet
            Future<Integer> result = generateSkeleton(phpUnit, className, sourceFo, parent, paramSkeleton);
            try {
                if (result.get() != 0) {
                    // test not generated
                    failed.add(sourceFo);
                    continue;
                }
                File moved = moveAndAdjustGeneratedFile(generatedFile, testFile, getTestDirectory(phpProject));
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

    private Future<Integer> generateSkeleton(PhpUnit phpUnit, String className, FileObject sourceFo, File parent, String paramSkeleton) {
        // test does not exist yet
        ExternalProcessBuilder externalProcessBuilder = new ExternalProcessBuilder(phpUnit.getProgram())
                .workingDirectory(parent)
                .addArgument(paramSkeleton)
                .addArgument(className)
                .addArgument(sourceFo.getNameExt());
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

    private File moveAndAdjustGeneratedFile(File generatedFile, File testFile, File testDirectory) {
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

        testFile = adjustFileContent(generatedFile, testFile, getIncludePaths(generatedFile, testFile, testDirectory));
        if (testFile == null) {
            return null;
        }
        assert testFile.isFile() : "Test file must exist: " + testFile;
        return testFile;
    }

    static List<String> getIncludePaths(File generatedFile, File testFile, File testDirectory) {
        File toFile = generatedFile.getParentFile();
        List<String> includePaths = new LinkedList<String>();
        includePaths.add(PropertyUtils.relativizeFile(testDirectory, toFile));

        File parent = testFile.getParentFile();
        if (!testDirectory.equals(parent)) {
            includePaths.add(PropertyUtils.relativizeFile(parent, toFile));
        }
        return includePaths;
    }

    private File adjustFileContent(File generatedFile, File testFile, List<String> includePaths) {
        try {
            // input
            BufferedReader in = new BufferedReader(new FileReader(generatedFile));

            try {
                // output
                BufferedWriter out = new BufferedWriter(new FileWriter(testFile));

                try {
                    // data
                    StringBuilder sb = new StringBuilder(200);
                    for (String path : includePaths) {
                        sb.append(String.format(INCLUDE_PATH_PART, path));
                    }

                    String line;
                    boolean written = false;
                    while ((line = in.readLine()) != null) {
                        out.write(line);
                        out.newLine();
                        if (!written && PHP_OPEN_TAG.equals(line.trim())) {
                            out.write(String.format(INCLUDE_PATH_TPL, sb.toString()));
                            out.newLine();
                            written = true;
                        }
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
