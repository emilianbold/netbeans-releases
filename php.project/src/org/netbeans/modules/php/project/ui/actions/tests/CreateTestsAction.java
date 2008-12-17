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

import java.io.File;
import java.io.IOException;
import java.lang.Integer;
import java.util.HashSet;
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
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpSources;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.ui.options.PHPOptionsCategory;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.netbeans.modules.php.project.util.PhpUnit;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
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
    protected void performAction(Node[] activatedNodes) {
        String phpUnitPath = PhpOptions.getInstance().getPhpUnit();
        if (Utils.validatePhpUnit(phpUnitPath) != null) {
            OptionsDisplayer.getDefault().open(PHPOptionsCategory.PATH_IN_LAYER);
            return;
        }
        // XXX check whether test directory exists
        RUNNABLES.add(getRunnable(activatedNodes, new PhpUnit(phpUnitPath)));
        TASK.schedule(0);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return false;
        }
        for (Node node : activatedNodes) {
            FileObject fileObj = getFileObject(node);
            if (fileObj == null) {
                return false;
            }

            if (!CommandUtils.isPhpFile(fileObj)
                    /*|| (node.getCookie(DataFolder.class) != null)*/) {
                return false;
            }

            Project prj = FileOwnerQuery.getOwner(fileObj);
            if ((prj == null) || (getSourceGroup(fileObj, prj) == null)) {
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

    static SourceGroup getSourceGroup(FileObject file, Project prj) {
        Sources src = ProjectUtils.getSources(prj);
        SourceGroup[] srcGrps = src.getSourceGroups(PhpSources.SOURCES_TYPE_PHP);
        for (SourceGroup srcGrp : srcGrps) {
            FileObject rootFolder = srcGrp.getRootFolder();
            if (((file == rootFolder) || FileUtil.isParentOf(rootFolder, file))
                    && srcGrp.contains(file)) {
                return srcGrp;
            }
        }
        return null;
    }

    static FileObject getFileObject(Node node) {
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

    private Runnable getRunnable(final Node[] activatedNodes, final PhpUnit phpUnit) {
        return new Runnable() {
            public void run() {
                Set<FileObject> failed = new HashSet<FileObject>();
                Set<File> toOpen = new HashSet<File>();
                boolean error = false;
                for (Node node : activatedNodes) {
                    FileObject sourceFo = getFileObject(node);
                    assert sourceFo != null : "A valid file object not found for node: " + node;

                    final File parent = FileUtil.toFile(sourceFo.getParent());
                    final File generatedFile = getGeneratedFile(sourceFo, parent);
                    final File testFile = getTestFile(sourceFo, generatedFile);
                    if (testFile.isFile()) {
                        // already exists
                        toOpen.add(testFile);
                        continue;
                    }

                    ExternalProcessBuilder externalProcessBuilder = new ExternalProcessBuilder(phpUnit.getPhpUnit());
                    externalProcessBuilder = externalProcessBuilder.workingDirectory(parent);
                    externalProcessBuilder = externalProcessBuilder.addArgument("--skeleton-test"); // NOI18N
                    externalProcessBuilder = externalProcessBuilder.addArgument(sourceFo.getName());
                    ExecutionService service = ExecutionService.newService(externalProcessBuilder, EXECUTION_DESCRIPTOR, null);
                    Future<Integer> result = service.run();
                    try {
                        if (result.get() != 0) {
                            // test not generated
                            failed.add(sourceFo);
                            continue;
                        }
                        toOpen.add(moveGeneratedFile(generatedFile, testFile));
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (ExecutionException ex) {
                        if (!error) {
                            error = true;
                            CommandUtils.processExecutionException(ex);
                        }
                        break;
                    }
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
                    toRefresh.add(file.getParentFile());
                    try {
                        FileObject fo = FileUtil.toFileObject(file);
                        assert fo != null : "File object not found for " + file;
                        assert fo.isValid() : "File object not valid for " + file;
                        DataObject dobj = DataObject.find(fo);
                        EditorCookie ec = dobj.getCookie(EditorCookie.class);
                        ec.open();
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

                if (!toRefresh.isEmpty()) {
                    FileUtil.refreshFor(toRefresh.toArray(new File[toRefresh.size()]));
                }
            }

            private File getGeneratedFile(FileObject source, File parent) {
                return new File(parent, source.getName() + "Test.php"); // NOI18N
            }

            private File getTestFile(FileObject source, File generatedFile) {
                Project project = FileOwnerQuery.getOwner(source);
                assert project != null : "Project must be found";
                final PhpProject phpProject = project.getLookup().lookup(PhpProject.class);
                assert phpProject != null : "PHP project must be found for " + project;

                FileObject testDirectory = ProjectPropertiesSupport.getTestDirectory(phpProject);
                FileObject sourcesDirectory = ProjectPropertiesSupport.getSourcesDirectory(phpProject);
                FileObject targetDirectory = testDirectory != null ? testDirectory : sourcesDirectory;
                assert targetDirectory != null && targetDirectory.isValid() : "Valid target folder for tests must be found for " + phpProject;

                String relativePath = FileUtil.getRelativePath(sourcesDirectory, source.getParent());
                assert relativePath != null : String.format("Relative path must be found % and %s", sourcesDirectory, source.getParent());

                return new File(new File(FileUtil.toFile(targetDirectory), relativePath), generatedFile.getName());
            }

            private File moveGeneratedFile(File generatedFile, File testFile) {
                assert generatedFile.isFile() : "Generated files must exist: " + generatedFile;
                assert !testFile.exists() : "Test file cannot exist: " + testFile;

                // create all the parents
                try {
                    FileUtil.createFolder(testFile.getParentFile());
                } catch (IOException exc) {
                    // what to do now??
                    Logger.getLogger(CreateTestsAction.class.getName()).log(Level.WARNING, null, exc);
                    return generatedFile;
                }

                if (!generatedFile.renameTo(testFile)) {
                    // what to do now??
                    return generatedFile;
                }
                assert testFile.isFile() : "Test file must exist: " + testFile;
                // XXX adjust 'require...' in the test file
                return testFile;
            }
        };
    }
}
