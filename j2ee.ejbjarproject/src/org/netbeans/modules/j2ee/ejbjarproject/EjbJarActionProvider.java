/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.j2ee.ejbjarproject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.lang.model.element.TypeElement;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.AttachingDICookie;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.common.project.ui.DeployOnSaveUtils;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/** Action provider of the Web project. This is the place where to do
 * strange things to Web actions. E.g. compile-single.
 */
class EjbJarActionProvider implements ActionProvider {
    
    // Definition of commands
    
    private static final String COMMAND_COMPILE = "compile"; //NOI18N
    private static final String COMMAND_VERIFY = "verify"; //NOI18N
    
    // Commands available from Web project
    private static final String[] supportedActions = {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        COMMAND_RUN,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG,
        COMMAND_DEBUG_SINGLE,
        EjbProjectConstants.COMMAND_REDEPLOY,
        JavaProjectConstants.COMMAND_JAVADOC,
        COMMAND_TEST,
        COMMAND_TEST_SINGLE,
        COMMAND_DEBUG_TEST_SINGLE,
        JavaProjectConstants.COMMAND_DEBUG_FIX,
        COMMAND_COMPILE,
        COMMAND_VERIFY,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME
    };
    
    // Project
    EjbJarProject project;
    
    // Ant project helper of the project
    private final AntProjectHelper antProjectHelper;
    private ReferenceHelper refHelper;
    
    /** Map from commands to ant targets */
    Map<String,String[]> commands;
    
    public EjbJarActionProvider(EjbJarProject project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
        commands = new HashMap<String,String[]>();
        commands.put(COMMAND_BUILD, new String[] {"dist"}); // NOI18N
        commands.put(COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
        commands.put(COMMAND_REBUILD, new String[] {"clean", "dist"}); // NOI18N
        commands.put(COMMAND_COMPILE_SINGLE, new String[] {"compile-single"}); // NOI18N
        commands.put(COMMAND_RUN, new String[] {"run"}); // NOI18N
        commands.put(COMMAND_RUN_SINGLE, new String[] {"run-main"}); // NOI18N
        commands.put(EjbProjectConstants.COMMAND_REDEPLOY, new String[] {"run"}); // NOI18N
        commands.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
        commands.put(JavaProjectConstants.COMMAND_JAVADOC, new String[] {"javadoc"}); // NOI18N
        commands.put(COMMAND_TEST, new String[] {"test"}); // NOI18N
        commands.put(COMMAND_TEST_SINGLE, new String[] {"test-single"}); // NOI18N
        commands.put(COMMAND_DEBUG_TEST_SINGLE, new String[] {"debug-test"}); // NOI18N
        commands.put(JavaProjectConstants.COMMAND_DEBUG_FIX, new String[] {"debug-fix"}); // NOI18N
        commands.put(COMMAND_COMPILE, new String[] {"compile"}); // NOI18N
        commands.put(COMMAND_VERIFY, new String[] {"verify"}); // NOI18N
        
        this.antProjectHelper = antProjectHelper;
        this.project = project;
        this.refHelper = refHelper;
    }
    
    private FileObject findBuildXml() {
        return project.getProjectDirectory().getFileObject(project.getBuildXmlName());
    }
    
    public String[] getSupportedActions() {
        return supportedActions;
    }
    
    public void invokeAction(final String command, final Lookup context ) throws IllegalArgumentException {
        if (COMMAND_DELETE.equals(command)) {
            DefaultProjectOperations.performDefaultDeleteOperation(project);
            return ;
        }
        
        if (COMMAND_COPY.equals(command)) {
            DefaultProjectOperations.performDefaultCopyOperation(project);
            return ;
        }
        
        if (COMMAND_MOVE.equals(command)) {
            DefaultProjectOperations.performDefaultMoveOperation(project);
            return ;
        }
        
        if (COMMAND_RENAME.equals(command)) {
            DefaultProjectOperations.performDefaultRenameOperation(project, null);
            return ;
        }

        String realCommand = command;
        if (COMMAND_BUILD.equals(realCommand) && isCosEnabled()) {
            boolean cleanAndBuild = DeployOnSaveUtils.showBuildActionWarning(project,
                    new DeployOnSaveUtils.CustomizerPresenter() {

                public void showCustomizer(String category) {
                    CustomizerProviderImpl provider = project.getLookup().lookup(CustomizerProviderImpl.class);
                    provider.showCustomizer(category);
                }
            });
            if (cleanAndBuild) {
                realCommand = COMMAND_REBUILD;
            } else {
                return;
            }
        }
        
        final String commandToExecute = realCommand;
        Runnable action = new Runnable() {
            public void run() {
                Properties p = new Properties();
                String[] targetNames;
                
                targetNames = getTargetNames(commandToExecute, context, p);
                if (targetNames == null) {
                    return;
                }
                if (targetNames.length == 0) {
                    targetNames = null;
                }
                if (p.keySet().size() == 0) {
                    p = null;
                }
                try {
                    FileObject buildFo = findBuildXml();
                    if (buildFo == null || !buildFo.isValid()) {
                        //The build.xml was deleted after the isActionEnabled was called
                        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(EjbJarActionProvider.class,
                                "LBL_No_Build_XML_Found"), NotifyDescriptor.WARNING_MESSAGE); // NOI18N
                        DialogDisplayer.getDefault().notify(nd);
                    } else {
                        ActionUtils.runTarget(buildFo, targetNames, p);
                    }
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        };
        
        action.run();
    }
    
    /**
     * @return array of targets or null to stop execution; can return empty array
     */
    /*private*/ String[] getTargetNames(String command, Lookup context, Properties p) throws IllegalArgumentException {
        String[] targetNames = commands.get(command);
        
        //EXECUTION PART
        if (command.equals(COMMAND_RUN_SINGLE)) {
            FileObject[] files = findTestSources(context, false);
            if (files != null) {
                targetNames = setupTestSingle(p, files);
            } else {
                // run Java
                FileObject[] javaFiles = findJavaSources(context);
                if ((javaFiles != null) && (javaFiles.length>0)) {
                    FileObject file = javaFiles[0];
                    String clazz = FileUtil.getRelativePath(getRoot(project.getSourceRoots().getRoots(),file), file);
                    p.setProperty("javac.includes", clazz); // NOI18N
                    // Convert foo/FooTest.java -> foo.FooTest
                    if (clazz.endsWith(".java")) { // NOI18N
                        clazz = clazz.substring(0, clazz.length() - 5);
                    }
                    clazz = clazz.replace('/','.');
                    
                    if (!SourceUtils.getMainClasses(file).isEmpty()) {
                        p.setProperty("run.class", clazz); // NOI18N
                        targetNames = commands.get(COMMAND_RUN_SINGLE);
                    } else {
                        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(EjbJarActionProvider.class, "LBL_No_Main_Classs_Found", clazz), NotifyDescriptor.INFORMATION_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                        return null;
                    }
                } else {
                    return null;
                }
            }
        } else if (command.equals(COMMAND_RUN) || command.equals(EjbProjectConstants.COMMAND_REDEPLOY)) {
            if (!isSelectedServer()) {
                return null;
            }
            if (isDebugged()) {
                p.setProperty("is.debugged", "true");
            }
            if (command.equals(EjbProjectConstants.COMMAND_REDEPLOY)) {
                p.setProperty("forceRedeploy", "true"); //NOI18N
            } else {
                p.setProperty("forceRedeploy", "false"); //NOI18N
            }
        //DEBUGGING PART
        } else if (command.equals(COMMAND_DEBUG) || command.equals(COMMAND_DEBUG_SINGLE)) {
            FileObject[] javaFiles = findJavaSources(context);
            if (javaFiles != null && javaFiles.length == 1 && !SourceUtils.getMainClasses(javaFiles[0]).isEmpty()) {
                FileObject javaFile = javaFiles[0];
                // debug Java with Main method
                String clazz = FileUtil.getRelativePath(getRoot(project.getSourceRoots().getRoots(), javaFile), javaFile);
                p.setProperty("javac.includes", clazz); // NOI18N
                // Convert foo/FooTest.java -> foo.FooTest
                if (clazz.endsWith(".java")) { // NOI18N
                    clazz = clazz.substring(0, clazz.length() - 5);
                }
                clazz = clazz.replace('/','.');
                p.setProperty("main.class", clazz); // NOI18N
                targetNames = new String [] {"debug-single-main"};
                return targetNames;
            }
            
            FileObject[] testFiles = findTestSources(context, false);
            if (testFiles != null) {
                targetNames = setupDebugTestSingle(p, testFiles);
            } else {
                if (!isSelectedServer()) {
                    return null;
                }
                if (isDebugged()) {
                    p.setProperty("is.debugged", "true");
                }
            }
        // APPLY CODE CHANGES DEBUGGING
        } else if (command.equals(JavaProjectConstants.COMMAND_DEBUG_FIX)) {
            FileObject[] files = findJavaSources(context);
            String path = null;
            final String[] classes = { "" };
            if (files != null) {
                path = FileUtil.getRelativePath(getRoot(project.getSourceRoots().getRoots(),files[0]), files[0]);
                targetNames = new String[] {"debug-fix"}; // NOI18N
                JavaSource js = JavaSource.forFileObject(files[0]);
                if (js != null) {
                    try {
                        js.runUserActionTask(new org.netbeans.api.java.source.Task<CompilationController>() {
                            public void run(CompilationController ci) throws Exception {
                                if (ci.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED).compareTo(JavaSource.Phase.ELEMENTS_RESOLVED) < 0) {
                                    ErrorManager.getDefault().log(ErrorManager.WARNING,
                                            "Unable to resolve "+ci.getFileObject()+" to phase "+JavaSource.Phase.RESOLVED+", current phase = "+ci.getPhase()+
                                            "\nDiagnostics = "+ci.getDiagnostics()+
                                            "\nFree memory = "+Runtime.getRuntime().freeMemory());
                                    return;
                                }
                                List<? extends TypeElement> types = ci.getTopLevelElements();
                                if (types.size() > 0) {
                                    for (TypeElement type : types) {
                                        if (classes[0].length() > 0) {
                                            classes[0] = classes[0] + " ";            // NOI18N
                                        }
                                        classes[0] = classes[0] + type.getQualifiedName().toString().replace('.', '/') + "*.class";  // NOI18N
                                    }
                                }
                            }
                        }, true);
                    } catch (java.io.IOException ioex) {
                        Exceptions.printStackTrace(ioex);
                    }
                }
            } else {
                files = findTestSources(context, false);
                path = FileUtil.getRelativePath(getRoot(project.getTestSourceRoots().getRoots(),files[0]), files[0]);
                targetNames = new String[] {"debug-fix-test"}; // NOI18N
            }
            // Convert foo/FooTest.java -> foo/FooTest
            if (path.endsWith(".java")) { // NOI18N
                path = path.substring(0, path.length() - 5);
            }
            p.setProperty("fix.includes", path); // NOI18N
            p.setProperty("fix.classes", classes[0]); // NOI18N
        //COMPILATION PART
        } else if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            FileObject[] sourceRoots = project.getSourceRoots().getRoots();
            FileObject[] files = findSourcesAndPackages( context, sourceRoots);
            boolean recursive = (context.lookup(NonRecursiveFolder.class) == null);
            if (files != null) {
                p.setProperty("javac.includes", ActionUtils.antIncludesList(files, getRoot(sourceRoots,files[0]), recursive)); // NOI18N
            } else {
                FileObject[] testRoots = project.getTestSourceRoots().getRoots();
                files = findSourcesAndPackages(context, testRoots);
                p.setProperty("javac.includes", ActionUtils.antIncludesList(files, getRoot(testRoots,files[0]), recursive)); // NOI18N
                targetNames = new String[] {"compile-test-single"}; // NOI18N
            }
        // TEST PART
        } else if ( command.equals( COMMAND_TEST_SINGLE ) ) {
            FileObject[] files = findTestSourcesForSources(context);
            targetNames = setupTestSingle(p, files);
        } else if ( command.equals( COMMAND_DEBUG_TEST_SINGLE ) ) {
            FileObject[] files = findTestSourcesForSources(context);
            targetNames = setupDebugTestSingle(p, files);
        } else {
            if (targetNames == null) {
                throw new IllegalArgumentException(command);
            }
        }
        
        return targetNames;
    }
    
    private String[] setupTestSingle(Properties p, FileObject[] files) {
        FileObject[] testSrcPath = project.getTestSourceRoots().getRoots();
        FileObject root = getRoot(testSrcPath, files[0]);
        p.setProperty("test.includes", ActionUtils.antIncludesList(files, root)); // NOI18N
        p.setProperty("javac.includes", ActionUtils.antIncludesList(files, root)); // NOI18N
        return new String[] {"test-single"}; // NOI18N
    }
    
    private String[] setupDebugTestSingle(Properties p, FileObject[] files) {
        FileObject[] testSrcPath = project.getTestSourceRoots().getRoots();
        FileObject root = getRoot(testSrcPath, files[0]);
        String path = FileUtil.getRelativePath(root, files[0]);
        // Convert foo/FooTest.java -> foo.FooTest
        p.setProperty("test.class", path.substring(0, path.length() - 5).replace('/', '.')); // NOI18N
        return new String[] {"debug-test"}; // NOI18N
    }
    
    public boolean isActionEnabled( String command, Lookup context ) {
        FileObject buildXml = findBuildXml();
        if (buildXml == null || !buildXml.isValid()) {
            return false;
        }

        if (isCosEnabled() && COMMAND_COMPILE_SINGLE.equals(command)) {
            return false;
        }

        if ( command.equals( COMMAND_VERIFY ) ) {
            return project.getEjbModule().hasVerifierSupport();
        }
        if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            return findSourcesAndPackages( context, project.getSourceRoots().getRoots()) != null
                    || findSourcesAndPackages( context, project.getTestSourceRoots().getRoots()) != null;
        }
        if ( command.equals( COMMAND_RUN_SINGLE ) ) {
            FileObject[] javaFiles = findTestSources(context,false);
            if ((javaFiles != null) && (javaFiles.length > 0)) {
                return true;
            }
            FileObject files[] = findJavaSources(context);
            return files != null && files.length == 1;
        } else if ( command.equals( COMMAND_TEST_SINGLE ) ) {
            return findTestSourcesForSources(context) != null;
        } else if ( command.equals( COMMAND_DEBUG_TEST_SINGLE ) ) {
            FileObject[] files = findTestSourcesForSources(context);
            return files != null && files.length == 1;
        } else if (command.equals(COMMAND_DEBUG_SINGLE)) {
            FileObject[] testFiles = findTestSources(context, false);
            FileObject[] javaFiles = findJavaSources(context);
            return ((testFiles != null && testFiles.length == 1) ||
                    (javaFiles != null && javaFiles.length == 1));
        } else {
            // other actions are global
            return true;
        }
        
        
    }
    
    // Private methods -----------------------------------------------------
    
    private static final String SUBST = "Test.java"; // NOI18N
    private static final Pattern SRCDIRJAVA = Pattern.compile("\\.java$"); // NOI18N
    
    /** Find selected java sources
     */
    private FileObject[] findJavaSources(Lookup context) {
        FileObject[] srcPath = project.getSourceRoots().getRoots();
        for (int i=0; i< srcPath.length; i++) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, srcPath[i], ".java", true); // NOI18N
            if (files != null) {
                return files;
            }
        }
        return null;
    }
    
    private FileObject[] findSourcesAndPackages(Lookup context, FileObject srcDir) {
        if (srcDir != null) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, srcDir, null, true); // NOI18N
            //Check if files are either packages of java files
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (!files[i].isFolder() && !"java".equals(files[i].getExt())) {
                        return null;
                    }
                }
            }
            return files;
        } else {
            return null;
        }
    }
    
    private FileObject[] findSourcesAndPackages(Lookup context, FileObject[] srcRoots) {
        for (int i=0; i<srcRoots.length; i++) {
            FileObject[] result = findSourcesAndPackages(context, srcRoots[i]);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
    
    /** Find either selected tests or tests which belong to selected source files
     */
    private FileObject[] findTestSources(Lookup context, boolean checkInSrcDir) {
        //XXX: Ugly, should be rewritten
        FileObject[] testSrcPath = project.getTestSourceRoots().getRoots();
        for (int i=0; i< testSrcPath.length; i++) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, testSrcPath[i], ".java", true); // NOI18N
            if (files != null) {
                return files;
            }
        }
        if (checkInSrcDir && testSrcPath.length>0) {
            FileObject[] files = findJavaSources(context);
            if (files != null) {
                //Try to find the test under the test roots
                FileObject srcRoot = getRoot(project.getSourceRoots().getRoots(),files[0]);
                for (int i=0; i<testSrcPath.length; i++) {
                    FileObject[] files2 = ActionUtils.regexpMapFiles(files,srcRoot, SRCDIRJAVA, testSrcPath[i], SUBST, true);
                    if (files2 != null) {
                        return files2;
                    }
                }
            }
        }
        return null;
    }
    
    /** Find tests corresponding to selected sources.
     */
    private FileObject[] findTestSourcesForSources(Lookup context) {
        FileObject[] sourceFiles = findJavaSources(context);
        if (sourceFiles == null) {
            return null;
        }
        FileObject[] testSrcPath = project.getTestSourceRoots().getRoots();
        if (testSrcPath.length == 0) {
            return null;
        }
        FileObject[] srcPath = project.getSourceRoots().getRoots();
        FileObject srcDir = getRoot(srcPath, sourceFiles[0]);
        for (int i=0; i<testSrcPath.length; i++) {
            FileObject[] files2 = ActionUtils.regexpMapFiles(sourceFiles, srcDir, SRCDIRJAVA, testSrcPath[i], SUBST, true);
            if (files2 != null) {
                return files2;
            }
        }
        return null;
    }
    
    private FileObject getRoot(FileObject[] roots, FileObject file) {
        FileObject srcDir = null;
        for (int i=0; i< roots.length; i++) {
            if (FileUtil.isParentOf(roots[i],file) || roots[i].equals(file)) {
                srcDir = roots[i];
                break;
            }
        }
        return srcDir;
    }
    
    private boolean isDebugged() {
        
        J2eeModuleProvider jmp = project.getLookup().lookup(J2eeModuleProvider.class);
        ServerDebugInfo sdi = jmp.getServerDebugInfo();
        if (sdi == null) {
            return false;
        }
        //        server.getServerInstance().getStartServer().getDebugInfo(null);
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        
        for (int i=0; i < sessions.length; i++) {
            Session s = sessions[i];
            if (s != null) {
                Object o = s.lookupFirst(null, AttachingDICookie.class);
                if (o != null) {
                    AttachingDICookie attCookie = (AttachingDICookie)o;
                    if (sdi.getTransport().equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                        if (attCookie.getSharedMemoryName().equalsIgnoreCase(sdi.getShmemName())) {
                            return true;
                        }
                    } else {
                        if (attCookie.getHostName().equalsIgnoreCase(sdi.getHost())) {
                            if (attCookie.getPortNumber() == sdi.getPort()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private boolean isSelectedServer() {
        String instance = antProjectHelper.getStandardPropertyEvaluator().getProperty(EjbJarProjectProperties.J2EE_SERVER_INSTANCE);
        if (instance != null) {
            String id = Deployment.getDefault().getServerID(instance);
            if (id != null) {
                return true;
            }
        }
        
        // if there is some server instance of the type which was used
        // previously do not ask and use it
        String serverType = antProjectHelper.getStandardPropertyEvaluator().getProperty(EjbJarProjectProperties.J2EE_SERVER_TYPE);
        if (serverType != null) {
            String[] servInstIDs = Deployment.getDefault().getInstancesOfServer(serverType);
            if (servInstIDs.length > 0) {
                setServerInstance(servInstIDs[0]);
                return true;
            }
        }
        
        // no selected server => warning
        String msg = NbBundle.getMessage(EjbJarActionProvider.class, "MSG_No_Server_Selected"); //  NOI18N
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE));
        return false;
    }
    
    private void setServerInstance(final String serverInstanceId) {
        EjbJarProjectProperties.setServerInstance(project, antProjectHelper, serverInstanceId);
    }
    
    private boolean isCosEnabled() {
        return !Boolean.parseBoolean(project.evaluator().getProperty(EjbJarProjectProperties.DISABLE_DEPLOY_ON_SAVE));
    }
}
