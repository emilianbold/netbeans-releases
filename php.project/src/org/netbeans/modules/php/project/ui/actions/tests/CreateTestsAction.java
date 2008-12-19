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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
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
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
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
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;
import org.openide.windows.InputOutput;

/**
 * Action for creating new PHP Unit tests.
 * @author Tomas Mysik
 */
public final class CreateTestsAction extends NodeAction {
    private static final long serialVersionUID = 9523829206628824L;

    private static final Logger LOGGER = Logger.getLogger(CreateTestsAction.class.getName());

    // php unit related
    private static final String PARAM_SKELETON = "--skeleton-test"; // NOI18N
    private static final String TEST_FILE_SUFFIX = "Test.php"; // NOI18N

    private static final String TMP_FILE_SUFFIX = ".nb-tmp"; // NOI18N
    private static final int OFFSET = 2;
    private static final String INCLUDE_PATH_TPL = "ini_set(\"include_path\", %sini_get(\"include_path\"));"; // NOI18N
    private static final String INCLUDE_PATH_PART = "\"%s\".PATH_SEPARATOR."; // NOI18N

    private static final ExecutionDescriptor EXECUTION_DESCRIPTOR
            = new ExecutionDescriptor().controllable(false).frontWindow(false).inputOutput(InputOutput.NULL);
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
        final PhpProject phpProject = getPhpProject(activatedNodes[0]);
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
            FileObject fileObj = getFileObject(node);
            if (fileObj == null) {
                return false;
            }

            // only php files or folders allowed
            if (fileObj.isData()
                    && !CommandUtils.isPhpFile(fileObj)) {
                return false;
            }

            PhpProject phpProject = getPhpProject(fileObj);
            if (phpProject == null) {
                return false;
            }
            if (onlyOneProjectAllowed == null) {
                onlyOneProjectAllowed = phpProject;
            } else if (!onlyOneProjectAllowed.equals(phpProject)) {
                // tests can be generated only for one project at one time
                return false;
            }

            FileObject tests = ProjectPropertiesSupport.getTestDirectory(phpProject, false);
            FileObject sources = ProjectPropertiesSupport.getSourcesDirectory(phpProject);
            boolean inTests = tests != null && (tests.equals(fileObj) || FileUtil.isParentOf(tests, fileObj));
            boolean inSources = sources.equals(fileObj) || FileUtil.isParentOf(sources, fileObj);
            if (!inSources || inTests) {
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

    /**
     * @return a PHP project or <code>null</code>.
     */
    private static PhpProject getPhpProject(Node node) {
        return getPhpProject(getFileObject(node));
    }

    /**
     * @return a PHP project or <code>null</code>.
     */
    private static PhpProject getPhpProject(FileObject fo) {
        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null) {
            return null;
        }
        return project.getLookup().lookup(PhpProject.class);
    }

    private static FileObject getFileObject(Node node) {
        DataObject dataObj = node.getCookie(DataObject.class);
        if (dataObj == null) {
            return null;
        }
        FileObject fileObj = dataObj.getPrimaryFile();
        if ((fileObj == null) || !fileObj.isValid()) {
            return null;
        }
        return fileObj;
    }

    private static List<FileObject> getFileObjects(final Node[] activatedNodes) {
        final List<FileObject> files = new ArrayList<FileObject>();
        for (Node node : activatedNodes) {
            FileObject fo = getFileObject(node);
            assert fo != null : "A valid file object not found for node: " + node;
            files.add(fo);
        }
        return files;
    }

    void generateTests(final Node[] activatedNodes, final PhpUnit phpUnit, final PhpProject phpProject) {
        assert phpProject != null;

        List<FileObject> files = getFileObjects(activatedNodes);
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
            CommandUtils.processExecutionException(ex);
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
                FileObject fo = FileUtil.toFileObject(file);
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

        final File parent = FileUtil.toFile(sourceFo.getParent());
        final File generatedFile = getGeneratedFile(sourceFo, parent);
        final File testFile = getTestFile(sourceFo, generatedFile, phpProject);
        if (testFile.isFile()) {
            // already exists
            toOpen.add(testFile);
            return;
        }

        // test does not exist yet
        ExternalProcessBuilder externalProcessBuilder = new ExternalProcessBuilder(phpUnit.getPhpUnit());
        externalProcessBuilder = externalProcessBuilder.workingDirectory(parent);
        externalProcessBuilder = externalProcessBuilder.addArgument(PARAM_SKELETON);
        externalProcessBuilder = externalProcessBuilder.addArgument(sourceFo.getName());
        ExecutionService service = ExecutionService.newService(externalProcessBuilder, EXECUTION_DESCRIPTOR, null);
        Future<Integer> result = service.run();
        try {
            if (result.get() != 0) {
                // test not generated
                failed.add(sourceFo);
                return;
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

    private File getGeneratedFile(FileObject source, File parent) {
        return new File(parent, source.getName() + TEST_FILE_SUFFIX);
    }

    private File getTestDirectory(PhpProject phpProject) {
        FileObject testDirectory = ProjectPropertiesSupport.getTestDirectory(phpProject, false);
        assert testDirectory != null && testDirectory.isValid() : "Valid folder for tests must be found for " + phpProject;
        return FileUtil.toFile(testDirectory);
    }

    private File getTestFile(FileObject source, File generatedFile, PhpProject phpProject) {
        FileObject sourcesDirectory = ProjectPropertiesSupport.getSourcesDirectory(phpProject);
        String relativePath = FileUtil.getRelativePath(sourcesDirectory, source.getParent());
        assert relativePath != null : String.format("Relative path must be found % and %s", sourcesDirectory, source.getParent());
        return new File(new File(getTestDirectory(phpProject), relativePath), generatedFile.getName());
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

        if (!generatedFile.renameTo(testFile)) {
            // what to do now??
            return generatedFile;
        }
        assert testFile.isFile() : "(1) Test file must exist: " + testFile;
        testFile = adjustFileContent(testFile, getIncludePaths(generatedFile, testFile, testDirectory));
        if (testFile == null) {
            return null;
        }
        assert testFile.isFile() : "(2) Test file must exist: " + testFile;
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

    private File adjustFileContent(File testFile, List<String> includePaths) {
        File tmpFile = new File(testFile.getAbsolutePath() + TMP_FILE_SUFFIX);
        assert !tmpFile.exists() : "TMP file should not exist: " + tmpFile;
        try {
            // input
            FileInputStream fis = new FileInputStream(testFile);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));

            try {
                // output
                FileOutputStream fos = new FileOutputStream(tmpFile);
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(fos));

                try {
                    // data
                    StringBuilder sb = new StringBuilder(200);
                    for (String path : includePaths) {
                        sb.append(String.format(INCLUDE_PATH_PART, path));
                    }

                    String line;
                    int i = 0;
                    while ((line = in.readLine()) != null) {
                        ++i;
                        if (i == OFFSET) {
                            out.write(String.format(INCLUDE_PATH_TPL, sb.toString()));
                            out.newLine();
                        }
                        out.write(line);
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
        }

        if (!testFile.delete()) {
            LOGGER.info("Cannot delete file " + testFile);
            return testFile;
        }
        if (!tmpFile.renameTo(testFile)) {
            LOGGER.info(String.format("Cannot rename file %s to %s", tmpFile, testFile));
            tmpFile.delete();
            tmpFile.deleteOnExit();
            return null;
        }
        return testFile;
    }
}
