/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.clientproject;


import java.awt.Dialog;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.AttachingDICookie;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.clientproject.ui.customizer.AppClientProjectProperties;
import org.netbeans.modules.j2ee.clientproject.ui.customizer.MainClassChooser;
import org.netbeans.modules.j2ee.clientproject.ui.customizer.MainClassWarning;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.MouseUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Action provider of the Application Client project.
 */
class AppClientActionProvider implements ActionProvider {
    
    private static final String COMMAND_COMPILE = "compile"; //NOI18N
    private static final String COMMAND_VERIFY = "verify"; //NOI18N
    
    // Commands available from Application Client project
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
        COMMAND_DEBUG_STEP_INTO,
        COMMAND_COMPILE,
        COMMAND_VERIFY,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME,
    };
    
    
    private static final String[] platformSensitiveActions = {
        COMMAND_BUILD,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        COMMAND_RUN,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG,
        COMMAND_DEBUG_SINGLE,
        JavaProjectConstants.COMMAND_JAVADOC,
        COMMAND_TEST,
        COMMAND_TEST_SINGLE,
        COMMAND_DEBUG_TEST_SINGLE,
        JavaProjectConstants.COMMAND_DEBUG_FIX,
        COMMAND_DEBUG_STEP_INTO,
    };
    
    // Project
    AppClientProject project;
    
    // Ant project helper of the project
    private final AntProjectHelper antProjectHelper;
    private final UpdateHelper updateHelper;
    
    /** Map from commands to ant targets */
    Map<String,String[]> commands;
    
    /**Set of commands which are affected by background scanning*/
    final Set<String> bkgScanSensitiveActions;
    
    public AppClientActionProvider( AppClientProject project, AntProjectHelper antProjectHelper, UpdateHelper updateHelper ) {
        commands = new HashMap<String, String[]>();
        commands.put(COMMAND_BUILD, new String[] {"dist"}); // NOI18N
        commands.put(COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
        commands.put(COMMAND_REBUILD, new String[] {"clean", "dist"}); // NOI18N
        commands.put(COMMAND_COMPILE_SINGLE, new String[] {"compile-single"}); // NOI18N
        // commands.put(COMMAND_COMPILE_TEST_SINGLE, new String[] {"compile-test-single"}); // NOI18N
        commands.put(COMMAND_RUN, new String[] {"run"}); // NOI18N
        commands.put(COMMAND_RUN_SINGLE, new String[] {"run-single"}); // NOI18N
        commands.put(EjbProjectConstants.COMMAND_REDEPLOY, new String[] {"run-deploy"}); // NOI18N
        commands.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
        //commands.put(COMMAND_DEBUG_SINGLE, new String[] {"debug-single"}); // NOI18N
        commands.put(JavaProjectConstants.COMMAND_JAVADOC, new String[] {"javadoc"}); // NOI18N
        commands.put(COMMAND_TEST, new String[] {"test"}); // NOI18N
        commands.put(COMMAND_TEST_SINGLE, new String[] {"test-single"}); // NOI18N
        commands.put(COMMAND_DEBUG_TEST_SINGLE, new String[] {"debug-test"}); // NOI18N
        commands.put(JavaProjectConstants.COMMAND_DEBUG_FIX, new String[] {"debug-fix"}); // NOI18N
        commands.put(COMMAND_DEBUG_STEP_INTO, new String[] {"debug-stepinto"}); // NOI18N
        commands.put(COMMAND_COMPILE, new String[] {"compile"}); // NOI18N
        commands.put(COMMAND_VERIFY, new String[] {"verify"}); // NOI18N
        
        this.bkgScanSensitiveActions = new HashSet<String>(Arrays.asList(new String[] {
            COMMAND_RUN,
            COMMAND_RUN_SINGLE,
            COMMAND_DEBUG,
            COMMAND_DEBUG_SINGLE,
            COMMAND_DEBUG_STEP_INTO
        }));
        
        this.antProjectHelper = antProjectHelper;
        this.updateHelper = updateHelper;
        this.project = project;
    }
    
    private FileObject findBuildXml() {
        return project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
    }
    
    public String[] getSupportedActions() {
        return supportedActions;
    }
    
    public void invokeAction( final String command, final Lookup context ) throws IllegalArgumentException {
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
        
        Runnable action = new Runnable() {
            public void run() {
                Properties p = new Properties();
                String[] targetNames;

                targetNames = getTargetNames(command, context, p);
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
                        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(AppClientActionProvider.class,
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
        
//        if (this.bkgScanSensitiveActions.contains(command)) {
            //TODO: RETOUCHE waitScanFinished
//            JMManager.getManager().invokeAfterScanFinished(action, NbBundle.getMessage(AppClientActionProvider.class,"ACTION_"+command)); //NOI18N
//        } else {
            action.run();
//        }
    }
    
    /**
     * @return array of targets or null to stop execution; can return empty array
     */
    /*private*/ String[] getTargetNames(String command, Lookup context, Properties p) throws IllegalArgumentException {
        if (Arrays.asList(platformSensitiveActions).contains(command)) {
            final String activePlatformId = this.project.evaluator().getProperty(AppClientProjectProperties.JAVA_PLATFORM);  //NOI18N
            if (AppClientProjectUtil.getActivePlatform(activePlatformId) == null) {
                showPlatformWarning();
                return null;
            }
        }
        String[] targetNames = new String[0];
        if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            FileObject[] sourceRoots = project.getSourceRoots().getRoots();
            FileObject[] files = findSourcesAndPackages( context, sourceRoots);
            boolean recursive = (context.lookup(NonRecursiveFolder.class) == null);
            if (files != null) {
                p.setProperty("javac.includes", ActionUtils.antIncludesList(files, getRoot(sourceRoots,files[0]), recursive)); // NOI18N
                targetNames = new String[] {"compile-single"}; // NOI18N
            } else {
                FileObject[] testRoots = project.getTestSourceRoots().getRoots();
                files = findSourcesAndPackages(context, testRoots);
                p.setProperty("javac.includes", ActionUtils.antIncludesList(files, getRoot(testRoots,files[0]), recursive)); // NOI18N
                targetNames = new String[] {"compile-test-single"}; // NOI18N
            }
        } else if ( command.equals( COMMAND_TEST_SINGLE ) ) {
            FileObject[] files = findTestSourcesForSources(context);
            targetNames = setupTestSingle(p, files);
        } else if ( command.equals( COMMAND_DEBUG_TEST_SINGLE ) ) {
            FileObject[] files = findTestSourcesForSources(context);
            targetNames = setupDebugTestSingle(p, files);
        } else if ( command.equals( JavaProjectConstants.COMMAND_DEBUG_FIX ) ) {
            //this is maybe not needed
            FileObject[] files = findSources( context );
            String path = null;
            if (files != null) {
                path = FileUtil.getRelativePath(getRoot(project.getSourceRoots().getRoots(),files[0]), files[0]);
                targetNames = new String[] {"debug-fix"}; // NOI18N
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
        } else if (command.equals(COMMAND_RUN) || command.equals(EjbProjectConstants.COMMAND_REDEPLOY) || command.equals(COMMAND_DEBUG) /*|| command.equals(COMMAND_DEBUG_STEP_INTO)*/) {
            EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            //check server
            if (!isSelectedServer()) {
                // no selected server => warning
                String msg = NbBundle.getMessage(
                        AppClientActionProvider.class, "MSG_No_Server_Selected"); //  NOI18N
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE));
                return null;
            }
            
            //see issue 83056
            if (command.equals(COMMAND_DEBUG)) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(AppClientActionProvider.class, "MSG_Server_State_Question"), NotifyDescriptor.QUESTION_MESSAGE);
                nd.setOptionType(NotifyDescriptor.YES_NO_OPTION);
                nd.setOptions(new Object[] {NotifyDescriptor.YES_OPTION, NotifyDescriptor.NO_OPTION});
                if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
                    nd = new NotifyDescriptor.Message(NbBundle.getMessage(AppClientActionProvider.class, "MSG_Server_State"), NotifyDescriptor.INFORMATION_MESSAGE);
                    Object o = DialogDisplayer.getDefault().notify(nd);
                    return null;
                }
            }
            
            // check project's main class
            String mainClass = ep.get("main.class"); // NOI18N
            MainClassStatus result = isSetMainClass(project.getSourceRoots().getRoots(), mainClass);
            if (result != MainClassStatus.SET_AND_VALID) {
                do {
                    // show warning, if cancel then return
                    if (showMainClassWarning(mainClass, ProjectUtils.getInformation(project).getDisplayName(), ep,result)) {
                        return null;
                    }
                    mainClass = ep.get("main.class"); // NOI18N
                    result=isSetMainClass(project.getSourceRoots().getRoots(), mainClass);
                } while (result != MainClassStatus.SET_AND_VALID);
                try {
                    if (updateHelper.requestSave()) {
                        updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,ep);
                        ProjectManager.getDefault().saveProject(project);
                    } else {
                        return null;
                    }
                } catch (IOException ioe) {
                    Logger.getLogger("global").log(Level.INFO, "Error while saving project: " + ioe);
                }
            }
            
            if (command.equals (EjbProjectConstants.COMMAND_REDEPLOY)) {
                p.setProperty("forceRedeploy", "true"); //NOI18N
            } else {
                p.setProperty("forceRedeploy", "false"); //NOI18N
            }
            
            targetNames = commands.get(command);
            if (targetNames == null) {
                throw new IllegalArgumentException(command);
            }
        } else if (command.equals(COMMAND_RUN_SINGLE)) {
            FileObject[] files = findTestSources(context, false);
            if (files != null) {
                targetNames = setupTestSingle(p, files);
            } else {
                //run java
                FileObject file = findSources(context)[0];
                String clazz = FileUtil.getRelativePath(getRoot(project.getSourceRoots().getRoots(),file), file);
                p.setProperty("javac.includes", clazz); // NOI18N
                // Convert foo/FooTest.java -> foo.FooTest
                if (clazz.endsWith(".java")) { // NOI18N
                    clazz = clazz.substring(0, clazz.length() - 5);
                }
                clazz = clazz.replace('/','.');
                
                if (!AppClientProjectUtil.hasMainMethod(file)) {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(AppClientActionProvider.class, "LBL_No_Main_Classs_Found", clazz), NotifyDescriptor.INFORMATION_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                    return null;
                } else {
                    p.setProperty("run.class", clazz); // NOI18N
                    targetNames = commands.get(COMMAND_RUN_SINGLE);
                        /*
                    } else {
                        p.setProperty("debug.class", clazz); // NOI18N
                        targetNames = (String[])commands.get(COMMAND_DEBUG_SINGLE);
                    }
                         */
                }
            }
        //DEBUGGING PART
        } else if (command.equals(COMMAND_DEBUG_SINGLE)) {
            if (!isSelectedServer()) {
                // no selected server => warning
                String msg = NbBundle.getMessage(
                        AppClientActionProvider.class, "MSG_No_Server_Selected"); //  NOI18N
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE));
                return null;
            }
            if (isDebugged()) {
                NotifyDescriptor nd;
                nd = new NotifyDescriptor.Confirmation(
                            NbBundle.getMessage(AppClientActionProvider.class, "MSG_FinishSession"),
                            NotifyDescriptor.OK_CANCEL_OPTION);
                Object o = DialogDisplayer.getDefault().notify(nd);
                if (o.equals(NotifyDescriptor.OK_OPTION)) {            
                    DebuggerManager.getDebuggerManager().getCurrentSession().kill();
                } else {
                    return null;
                }
            }
        } else {
            targetNames = commands.get(command);
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
        if (  buildXml == null || !buildXml.isValid()) {
            return false;
        }
        if ( command.equals( COMMAND_VERIFY ) ) {
            return project.getCarModule().hasVerifierSupport();
        }
        if (command.equals(COMMAND_RUN)) {
            //see issue #92895
            //XXX - replace this method with a call to API as soon as issue 109895 will be fixed
            return isSelectedServer() && !isTargetServerRemote();
        } else if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            return findSourcesAndPackages( context, project.getSourceRoots().getRoots()) != null
                    || findSourcesAndPackages( context, project.getTestSourceRoots().getRoots()) != null;
        } else if ( command.equals( COMMAND_TEST_SINGLE ) ) {
            return findTestSourcesForSources(context) != null;
        } else if ( command.equals( COMMAND_DEBUG_TEST_SINGLE ) ) {
            FileObject[] files = findTestSourcesForSources(context);
            return files != null && files.length == 1;
        } else if (command.equals(COMMAND_RUN_SINGLE) ||
                command.equals(COMMAND_DEBUG_SINGLE) ||
                command.equals(JavaProjectConstants.COMMAND_DEBUG_FIX)) {
            FileObject fos[] = findSources(context);
            if (fos != null && fos.length == 1) {
                return true;
            }
            fos = findTestSources(context, false);
            return fos != null && fos.length == 1;
        } else {
            // other actions are global
            return true;
        }
    }
    
    
    
    // Private methods -----------------------------------------------------
    
    
    private static final Pattern SRCDIRJAVA = Pattern.compile("\\.java$"); // NOI18N
    private static final String SUBST = "Test.java"; // NOI18N
    
    /** Find selected sources, the sources has to be under single source root,
     *  @param context the lookup in which files should be found
     */
    private FileObject[] findSources(Lookup context) {
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
                    if (!files[i].isFolder() && !"java".equals(files[i].getExt())) { // NOI18N
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
            FileObject[] files = findSources(context);
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
        FileObject[] sourceFiles = findSources(context);
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
        assert file != null : "File can't be null";   //NOI18N
        FileObject srcDir = null;
        for (int i=0; i< roots.length; i++) {
            assert roots[i] != null : "Source Path Root can't be null"; //NOI18N
            if (FileUtil.isParentOf(roots[i],file) || roots[i].equals(file)) {
                srcDir = roots[i];
                break;
            }
        }
        return srcDir;
    }
    
    
    private static enum MainClassStatus {
        SET_AND_VALID,
        SET_BUT_INVALID,
        UNSET
    }

    /**
     * Tests if the main class is set
     * @param sourcesRoots source roots
     * @param mainClass main class name
     * @return status code
     */
    private MainClassStatus isSetMainClass(FileObject[] sourcesRoots, String mainClass) {

        // support for unit testing
        if (MainClassChooser.unitTestingSupport_hasMainMethodResult != null) {
            return MainClassChooser.unitTestingSupport_hasMainMethodResult ? MainClassStatus.SET_AND_VALID : MainClassStatus.SET_BUT_INVALID;
        }

        if (mainClass == null || mainClass.length () == 0) {
            return MainClassStatus.UNSET;
        }
        
        ClassPath bootPath = ClassPath.getClassPath (sourcesRoots[0], ClassPath.BOOT);        //Single compilation unit
        ClassPath compilePath = ClassPath.getClassPath (sourcesRoots[0], ClassPath.COMPILE);
        ClassPath sourcePath = ClassPath.getClassPath(sourcesRoots[0], ClassPath.SOURCE);
        if (AppClientProjectUtil.isMainClass (mainClass, bootPath, compilePath, sourcePath)) {
            return MainClassStatus.SET_AND_VALID;
        }
        return MainClassStatus.SET_BUT_INVALID;
    }
    
    /** Checks if given file object contains the main method.
     *
     * @param classFO file object represents java
     * @return false if parameter is null or doesn't contain class with main method
     */
    public static boolean canBeRun(FileObject classFO) {
        return !SourceUtils.getMainClasses(classFO).isEmpty();
    }
    
    
    /**
     * Asks user for name of main class
     * @param mainClass current main class
     * @param projectName the name of project
     * @param ep EditableProperties
     * @param messgeType type of dialog -1 when the main class is not set, -2 when the main class in not valid
     * @return true if user selected main class
     */
    private boolean showMainClassWarning(String mainClass, String projectName, EditableProperties ep, MainClassStatus messageType) {
        boolean canceled;
        final JButton okButton = new JButton(NbBundle.getMessage(MainClassWarning.class, "LBL_MainClassWarning_ChooseMainClass_OK")); // NOI18N
        okButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MainClassWarning.class, "AD_MainClassWarning_ChooseMainClass_OK"));
        
        // main class goes wrong => warning
        String message;
        switch (messageType) {
            case UNSET:
                message = MessageFormat.format(NbBundle.getMessage(MainClassWarning.class,"LBL_MainClassNotFound"), new Object[] {
                    projectName
                });
                break;
            case SET_BUT_INVALID:
                message = MessageFormat.format(NbBundle.getMessage(MainClassWarning.class,"LBL_MainClassWrong"), new Object[] {
                    mainClass,
                    projectName
                });
                break;
            default:
                throw new IllegalArgumentException();
        }
        final MainClassWarning panel = new MainClassWarning(message,project.getSourceRoots().getRoots());
        Object[] options = new Object[] {
            okButton,
            DialogDescriptor.CANCEL_OPTION
        };
        
        panel.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (e.getSource() instanceof MouseEvent && MouseUtils.isDoubleClick(((MouseEvent)e.getSource()))) {
                    // click button and the finish dialog with selected class
                    okButton.doClick();
                } else {
                    okButton.setEnabled(panel.getSelectedMainClass() != null);
                }
            }
        });
        
        okButton.setEnabled(false);
        DialogDescriptor desc = new DialogDescriptor(panel,
                NbBundle.getMessage(MainClassWarning.class, "CTL_MainClassWarning_Title", ProjectUtils.getInformation(project).getDisplayName()), // NOI18N
                true, options, options[0], DialogDescriptor.BOTTOM_ALIGN, null, null);
        desc.setMessageType(DialogDescriptor.INFORMATION_MESSAGE);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
        dlg.setVisible(true);
        if (desc.getValue() != options[0]) {
            canceled = true;
        } else {
            mainClass = panel.getSelectedMainClass();
            canceled = false;
            ep.put("main.class", mainClass == null ? "" : mainClass); // NOI18N
        }
        dlg.dispose();
        
        return canceled;
    }
    
    private void showPlatformWarning() {
        final JButton closeOption = new JButton(NbBundle.getMessage(AppClientActionProvider.class, "CTL_BrokenPlatform_Close"));
        closeOption.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AppClientActionProvider.class, "AD_BrokenPlatform_Close"));
        final ProjectInformation pi = this.project.getLookup().lookup(ProjectInformation.class);
        final String projectDisplayName = pi == null ?
            NbBundle.getMessage(AppClientActionProvider.class,"TEXT_BrokenPlatform_UnknownProjectName")
            : pi.getDisplayName();
        final DialogDescriptor dd = new DialogDescriptor(
                NbBundle.getMessage(AppClientActionProvider.class, "TEXT_BrokenPlatform", projectDisplayName),
                NbBundle.getMessage(AppClientActionProvider.class, "MSG_BrokenPlatform_Title"),
                true,
                new Object[] {closeOption},
                closeOption,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);
        dd.setMessageType(DialogDescriptor.WARNING_MESSAGE);
        final Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.setVisible(true);
    }
    
    private boolean isSelectedServer() {
        String instance = antProjectHelper.getStandardPropertyEvaluator ().getProperty (AppClientProjectProperties.J2EE_SERVER_INSTANCE);
        if (instance != null) {
            String id = Deployment.getDefault().getServerID(instance);
            if (id != null) {
                return true;
            }
        }
        
        // if there is some server instance of the type which was used
        // previously do not ask and use it
        String serverType = antProjectHelper.getStandardPropertyEvaluator ().getProperty (AppClientProjectProperties.J2EE_SERVER_TYPE);
        if (serverType != null) {
            String[] servInstIDs = Deployment.getDefault().getInstancesOfServer(serverType);
            if (servInstIDs.length > 0) {
                setServerInstance(servInstIDs[0]);
                return true;
            }
        }
        return false;
    }
    
    private void setServerInstance(final String serverInstanceId) {
        AppClientProjectProperties.setServerInstance(project, antProjectHelper, serverInstanceId);
    }
    
   private boolean isDebugged() {
        J2eeModuleProvider jmp = project.getLookup().lookup(J2eeModuleProvider.class);
        ServerDebugInfo sdi = jmp.getServerDebugInfo();
        if (sdi == null) {
            return false;
        }
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
                        if (attCookie.getHostName().equalsIgnoreCase(sdi.getHost()) &&
                                attCookie.getPortNumber() == sdi.getPort()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
   
    private boolean isTargetServerRemote() {
        J2eeModuleProvider module = project.getLookup().lookup(J2eeModuleProvider.class);
        InstanceProperties props = module.getInstanceProperties();
        String domain = props.getProperty("DOMAIN"); //NOI18N
        String location = props.getProperty("LOCATION"); //NOI18N
        return "".equals(domain) && "".equals(location); //NOI18N
    }
}
