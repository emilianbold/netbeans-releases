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
package org.netbeans.modules.cnd.testrunner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSession.SessionType;
import org.netbeans.modules.cnd.testrunner.ui.CndUnitHandlerFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;

/**
 * Test runner implmentation for running PyUnit tests
 *
 * @author Erno Mononen
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.testrunner.TestRunner.class)
public final class CndUnitRunner implements TestRunner/*, RakeTaskCustomizer*/ {

    private static final Logger LOGGER = Logger.getLogger(CndUnitRunner.class.getName());
    private static final String NB_TEST_RUNNER = "NB_TEST_RUNNER"; //NOI18N
    public static final String RUNNER_SCRIPT_NAME = "nb_test_runner.py";  //NOI18N
    private static final CndUnitRunner INSTANCE = new CndUnitRunner();

    static {
        // this env variable is referenced from nb_test_runner.py, where it
        // gets appended to the rake require path
        System.setProperty(NB_TEST_RUNNER, getScript(RUNNER_SCRIPT_NAME).getAbsolutePath());
    }

    public CndUnitRunner getInstance() {
        return INSTANCE;
    }

    public void runSingleTest(FileObject testFile, String testMethod, boolean debug) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void runSingleTest(FileObject testFile, String className, String testMethod, boolean debug) {
        List<String> additionalArgs = getTestFileArgs(testFile);
        additionalArgs.add("-m"); // NOI18N
        String methodRef;
        String file = additionalArgs.get(1);
        if (className.startsWith(file+".")) { // NOI18N
            className = className.substring(file.length()+1);
        }
        methodRef = className + "." + testMethod; // NOI18N
        additionalArgs.add(methodRef);
        run(FileOwnerQuery.getOwner(testFile), additionalArgs, testMethod, debug);
    }

    public void runTest(FileObject testFile, boolean debug) {
        Project project = FileOwnerQuery.getOwner(testFile);
        if (!testFile.isFolder()) {
            run(project, getTestFileArgs(testFile), testFile.getName(), debug);
        } else {
            List<String> additionalArgs = new ArrayList<String>();
            additionalArgs.add("-d"); //NOI18N
            additionalArgs.add(FileUtil.toFile(testFile).getAbsolutePath());
            String name = ProjectUtils.getInformation(project).getDisplayName();
            run(project, additionalArgs, name, debug);
        }
    }

    private List<String> getTestFileArgs(FileObject testFile) {
        Project project = FileOwnerQuery.getOwner(testFile);
        String testFilePath = FileUtil.toFile(testFile).getAbsolutePath();
//        if (project instanceof PythonProject) {
//            PythonProject pythonPrj = (PythonProject)project;
//            FileObject root = null;
//            for (FileObject r : pythonPrj.getSourceRootFiles()) {
//                if (FileUtil.isParentOf(r, testFile)) {
//                    root = r;
//                    break;
//                }
//            }
//            if (root == null) {
//                for (FileObject r : pythonPrj.getTestSourceRootFiles()) {
//                    if (FileUtil.isParentOf(r, testFile)) {
//                        root = r;
//                        break;
//                    }
//                }
//            }
//
//            if (root != null) {
//                String rootPath = FileUtil.toFile(root).getAbsolutePath();
//                if (testFilePath.startsWith(rootPath)) {
//                    testFilePath = testFilePath.substring(rootPath.length());
//                    if (testFilePath.startsWith("/") || testFilePath.startsWith("\\")) { // NOI18N
//                        testFilePath = testFilePath.substring(1);
//                    }
//
//                    testFilePath = testFilePath.replace("\\", ".");
//                    testFilePath = testFilePath.replace("/", ".");
//
//                    if (testFilePath.endsWith(".py")) {
//                        testFilePath = testFilePath.substring(0, testFilePath.length()-3);
//                    }
//                }
//            }
//        }
        List<String> additionalArgs = new ArrayList<String>();
        additionalArgs.add("-f"); //NOI18N
        additionalArgs.add(testFilePath);
        return additionalArgs;
    }

    private static File getScript(String name) {
        File script = InstalledFileLocator.getDefault().locate(
                name, "org.netbeans.modules.python.testrunner", false);  // NOI18N

        if (script == null) {
            throw new IllegalStateException("Could not locate " + name); // NOI18N

        }
        return script;

    }

    static void addPyUnitRunnerToEnv(Map<String, String> env) {
        env.put(NB_TEST_RUNNER, getScript(RUNNER_SCRIPT_NAME).getAbsolutePath());
    }
    
    public void runAllTests(Project project, boolean debug) {
//        List<String> additionalArgs = new ArrayList<String>();
//        PythonProject baseProject = project.getLookup().lookup(PythonProject.class);
//        boolean haveTestFolders = false;
//        for (FileObject testDir : baseProject.getTestSourceRootFiles()) {
//            additionalArgs.add("-d"); //NOI18N
//            additionalArgs.add(FileUtil.toFile(testDir).getAbsolutePath());
//            haveTestFolders = true;
//        }
//        if (!haveTestFolders) {
//            for (FileObject testDir : baseProject.getSourceRootFiles()) {
//                additionalArgs.add("-d"); //NOI18N
//                additionalArgs.add(FileUtil.toFile(testDir).getAbsolutePath());
//            }
//        }
//
//        String name = ProjectUtils.getInformation(project).getDisplayName();
//
//        run(project, additionalArgs, name, debug);
    }

//    protected ArrayList<String> buildPythonPath( PythonPlatform platform , PythonProject project ) {
//      final ArrayList<String> pythonPath = new ArrayList<String>() ;
//      // start with platform
//      pythonPath.addAll(platform.getPythonPath());
//      for (FileObject fo : project.getSourceRoots().getRoots()) {
//        File f = FileUtil.toFile(fo);
//        pythonPath.add(f.getAbsolutePath());
//      }
//      for (FileObject fo : project.getTestRoots().getRoots()) {
//          File f = FileUtil.toFile(fo);
//          pythonPath.add(f.getAbsolutePath());
//      }
//      PythonProjectProperties properties = new PythonProjectProperties(project);
//      pythonPath.addAll(properties.getPythonPath());
//      return pythonPath ;
//    }

    /**
     *
     * provide a reasonable common Build of JAVAPATH for Run or Debug Jython commands
     * @param platform current platform
     * @param project current project
     * @return JavaPath fileList for jython CLASSPATH command
     */
//    protected ArrayList<String> buildJavaPath( PythonPlatform platform , PythonProject project ) {
//      final ArrayList<String> javaPath = new ArrayList<String>() ;
//      // start with platform
//      javaPath.addAll(platform.getJavaPath());
//      PythonProjectProperties properties = new PythonProjectProperties(project);
//      javaPath.addAll(properties.getJavaPath());
//      return javaPath ;
//    }

    private void run(Project project, List<String> additionalArgs, String name, boolean debug) {
//        PythonPlatform platform = PythonPlatform.platformFor(project);
//
//        PythonExecution desc = null;
//        desc = new PythonExecution();
//
//        File pwd = FileUtil.toFile(project.getProjectDirectory());
//
//            desc.setDisplayName(name);
//            desc.setScriptArgs(Utilities.escapeParameters(additionalArgs.toArray(new String[additionalArgs.size()])));
//            desc.setWorkingDirectory(pwd.getAbsolutePath());
//            desc.setCommand(platform.getInterpreterCommand());
//            desc.setScript(getScript(RUNNER_SCRIPT_NAME).getAbsolutePath());
//            desc.setCommandArgs(platform.getInterpreterArgs());
//            if (project instanceof PythonProject) {
//                PythonProject pythonProject = (PythonProject)project;
//                desc.setPath(PythonPlatform.buildPath(buildPythonPath(platform, pythonProject)));
//                desc.setJavaPath(PythonPlatform.buildPath(buildJavaPath(platform, pythonProject)));
//            }
//            desc.setShowControls(true);
//            desc.setShowInput(true);
//            desc.setShowWindow(true);
//            desc.addStandardRecognizers();
//
//            PythonCoverageProvider coverageProvider = PythonCoverageProvider.get(project);
//            if (coverageProvider != null && coverageProvider.isEnabled()) {
//                desc = coverageProvider.wrapWithCoverage(desc);
//            }
//
//            if (LOGGER.isLoggable(Level.INFO)) {
//                LOGGER.log(Level.INFO, "Running Python Unit Test with the following descriptor: command={0} " +
//                        "commandArgs={1} displayName={2} javaPath={3} path={4} script={5} scriptArgs={6}" +
//                        " workingDirectory={7}", new Object[]{desc.getCommand(), desc.getCommandArgs(),
//                        desc.getDisplayName(), desc.getJavaPath(), desc.getPath(), desc.getScript(),
//                        desc.getScriptArgs(), desc.getWorkingDirectory()});
//            }
//
        final TestSession session = new TestSession(name,
                project,
                debug ? SessionType.DEBUG : SessionType.TEST, new CndTestRunnerNodeFactory());

        TestExecutionManager.getInstance().start(/*desc,*/ new CndUnitHandlerFactory(), session);
    }

    public boolean supports(TestType type) {
        return type == TestType.CPP_UNIT;
    }

}
