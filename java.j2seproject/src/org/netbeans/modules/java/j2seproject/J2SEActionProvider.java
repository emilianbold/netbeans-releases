/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject;

import java.awt.Dialog;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.j2seproject.applet.AppletSupport;
import org.netbeans.modules.java.j2seproject.ui.customizer.MainClassChooser;
import org.netbeans.modules.java.j2seproject.ui.customizer.MainClassWarning;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.MouseUtils;
import org.openide.cookies.SourceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.src.ClassElement;
import org.openide.src.SourceElement;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/** Action provider of the J2SE project. This is the place where to do
 * strange things to J2SE actions. E.g. compile-single.
 */
class J2SEActionProvider implements ActionProvider {
    
    // Commands available from J2SE project
    private static final String[] supportedActions = {
        COMMAND_BUILD, 
        COMMAND_CLEAN, 
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
    J2SEProject project;
    
    // Ant project helper of the project
    private UpdateHelper updateHelper;
    
        
    /** Map from commands to ant targets */
    Map/*<String,String[]>*/ commands;
    
    public J2SEActionProvider( J2SEProject project, UpdateHelper updateHelper ) {
        
        commands = new HashMap();
            commands.put(COMMAND_BUILD, new String[] {"jar"}); // NOI18N
            commands.put(COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
            commands.put(COMMAND_REBUILD, new String[] {"clean", "jar"}); // NOI18N
            commands.put(COMMAND_COMPILE_SINGLE, new String[] {"compile-single"}); // NOI18N
            // commands.put(COMMAND_COMPILE_TEST_SINGLE, new String[] {"compile-test-single"}); // NOI18N
            commands.put(COMMAND_RUN, new String[] {"run"}); // NOI18N
            commands.put(COMMAND_RUN_SINGLE, new String[] {"run-single"}); // NOI18N
            commands.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
            commands.put(COMMAND_DEBUG_SINGLE, new String[] {"debug-single"}); // NOI18N
            commands.put(JavaProjectConstants.COMMAND_JAVADOC, new String[] {"javadoc"}); // NOI18N
            commands.put(COMMAND_TEST, new String[] {"test"}); // NOI18N
            commands.put(COMMAND_TEST_SINGLE, new String[] {"test-single"}); // NOI18N
            commands.put(COMMAND_DEBUG_TEST_SINGLE, new String[] {"debug-test"}); // NOI18N
            commands.put(JavaProjectConstants.COMMAND_DEBUG_FIX, new String[] {"debug-fix"}); // NOI18N
            commands.put(COMMAND_DEBUG_STEP_INTO, new String[] {"debug-stepinto"}); // NOI18N
        
        this.updateHelper = updateHelper;
        this.project = project;
    }
    
    private FileObject findBuildXml() {
        return project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
    }
    
    public String[] getSupportedActions() {
        return supportedActions;
    }
    
    public void invokeAction( String command, Lookup context ) throws IllegalArgumentException {
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
            ActionUtils.runTarget(findBuildXml(), targetNames, p);
        } 
        catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    /**
     * @return array of targets or null to stop execution; can return empty array
     */
    /*private*/ String[] getTargetNames(String command, Lookup context, Properties p) throws IllegalArgumentException {
        String[] targetNames = new String[0];
        if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            FileObject[] sourceRoots = project.getSourceRoots().getRoots();
            FileObject[] files = findSourcesAndPackages( context, sourceRoots);
            boolean recursive = (context.lookup(NonRecursiveFolder.class) == null);
            if (files != null) {
                p.setProperty("javac.includes", ActionUtils.antIncludesList(files, getRoot(sourceRoots,files[0]), recursive)); // NOI18N
                targetNames = new String[] {"compile-single"}; // NOI18N
            } 
            else {
                FileObject[] testRoots = project.getTestSourceRoots().getRoots();
                files = findSourcesAndPackages(context, testRoots);
                p.setProperty("javac.includes", ActionUtils.antIncludesList(files, getRoot(testRoots,files[0]), recursive)); // NOI18N
                targetNames = new String[] {"compile-test-single"}; // NOI18N
            }
        } 
        else if ( command.equals( COMMAND_TEST_SINGLE ) ) {
            FileObject[] files = findTestSourcesForSources(context);
            targetNames = setupTestSingle(p, files);
        } 
        else if ( command.equals( COMMAND_DEBUG_TEST_SINGLE ) ) {
            FileObject[] files = findTestSourcesForSources(context);
            targetNames = setupDebugTestSingle(p, files);
        } 
        else if ( command.equals( JavaProjectConstants.COMMAND_DEBUG_FIX ) ) {
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
        }
        else if (command.equals (COMMAND_RUN) || command.equals(COMMAND_DEBUG) || command.equals(COMMAND_DEBUG_STEP_INTO)) {
            EditableProperties ep = updateHelper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);

            // check project's main class
            String mainClass = (String)ep.get ("main.class"); // NOI18N
            int result;
            while ((result=isSetMainClass (project.getSourceRoots().getRoots(), mainClass))!=0) {
                
                // show warning, if cancel then return
                if (showMainClassWarning (mainClass, ProjectUtils.getInformation(project).getDisplayName(), ep,result)) {
                    return null;
                }
                mainClass = (String)ep.get ("main.class"); // NOI18N
                updateHelper.putProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
            }

            p.setProperty("main.class", mainClass); // NOI18N
            if (!command.equals(COMMAND_RUN)) {
                p.setProperty("debug.class", mainClass); // NOI18N
            }
            
            targetNames = (String[])commands.get(command);
            if (targetNames == null) {
                throw new IllegalArgumentException(command);
            }
        } else if (command.equals (COMMAND_RUN_SINGLE) || command.equals (COMMAND_DEBUG_SINGLE)) {
            FileObject[] files = findTestSources(context, false);
            if (files != null) {
                if (command.equals(COMMAND_RUN_SINGLE)) {
                    targetNames = setupTestSingle(p, files);
                } else {
                    targetNames = setupDebugTestSingle(p, files);
                }
            } else {
                FileObject file = findSources(context)[0];
                String clazz = FileUtil.getRelativePath(getRoot(project.getSourceRoots().getRoots(),file), file);
                p.setProperty("javac.includes", clazz); // NOI18N
                // Convert foo/FooTest.java -> foo.FooTest
                if (clazz.endsWith(".java")) { // NOI18N
                    clazz = clazz.substring(0, clazz.length() - 5);
                }
                clazz = clazz.replace('/','.');

                if (!J2SEProjectUtil.hasMainMethod (file)) {
                    if (AppletSupport.isApplet(file)) {
                        
                        EditableProperties ep = updateHelper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        String jvmargs = ep.getProperty("run.jvmargs");
                        
                        URL url = null;

                        // do this only when security policy is not set manually
                        if ((jvmargs == null) || !(jvmargs.indexOf("java.security.policy") > 0)) {  //NOI18N
                            AppletSupport.generateSecurityPolicy(project.getProjectDirectory());
                            if ((jvmargs == null) || (jvmargs.length() == 0)) {
                                ep.setProperty("run.jvmargs", "-Djava.security.policy=applet.policy"); //NOI18N
                            } else {
                                ep.setProperty("run.jvmargs", jvmargs + " -Djava.security.policy=applet.policy"); //NOI18N
                            }
                            updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                            try {
                                ProjectManager.getDefault().saveProject(project);
                            } catch (Exception e) {
                                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Error while saving project: " + e);
                            }
                        }
                        
                        if (file.existsExt("html") || file.existsExt("HTML")) { //NOI18N
                            url = copyAppletHTML(file, "html"); //NOI18N
                        } else {                    
                            url = generateAppletHTML(file);
                        }
                        if (url == null) {
                            return null;
                        }
                        p.setProperty("applet.url", url.toString()); // NOI18N
                        if (command.equals (COMMAND_RUN_SINGLE)) {
                            targetNames = new String[] {"run-applet"}; // NOI18N
                        } else {
                            p.setProperty("debug.class", clazz); // NOI18N
                            targetNames = new String[] {"debug-applet"}; // NOI18N
                        }
                    } else {
                        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(J2SEActionProvider.class, "LBL_No_Main_Classs_Found", clazz), NotifyDescriptor.INFORMATION_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                        return null;
                    }
                } else {
                    if (command.equals (COMMAND_RUN_SINGLE)) {
                        p.setProperty("run.class", clazz); // NOI18N
                        targetNames = (String[])commands.get(COMMAND_RUN_SINGLE);
                    } else {
                        p.setProperty("debug.class", clazz); // NOI18N
                        targetNames = (String[])commands.get(COMMAND_DEBUG_SINGLE);
                    }
                }
            }
        } else {
            targetNames = (String[])commands.get(command);
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
        if ( findBuildXml() == null ) {
            return false;
        }
        if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            return findSourcesAndPackages( context, project.getSourceRoots().getRoots()) != null
                    || findSourcesAndPackages( context, project.getTestSourceRoots().getRoots()) != null;
        }
        else if ( command.equals( COMMAND_TEST_SINGLE ) ) {
            return findTestSourcesForSources(context) != null;
        }
        else if ( command.equals( COMMAND_DEBUG_TEST_SINGLE ) ) {
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

    private FileObject[] findSourcesAndPackages (Lookup context, FileObject srcDir) {
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
    
    private FileObject[] findSourcesAndPackages (Lookup context, FileObject[] srcRoots) {
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
            FileObject[] files = findSources (context);
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
    
    private FileObject getRoot (FileObject[] roots, FileObject file) {
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
    

    /**
     * Tests if the main class is set
     * @param sourcesRoots source roots
     * @param mainClass main class name
     * @return 0 if the main class is set and is valid
     *        -1 if the main class is not set
     *        -2 if the main class is set but is not valid
     */
    private int isSetMainClass (FileObject[] sourcesRoots, String mainClass) {

        // support for unit testing
        if (MainClassChooser.unitTestingSupport_hasMainMethodResult != null) {
            return MainClassChooser.unitTestingSupport_hasMainMethodResult.booleanValue () ? 0 : -2;
        }

        if (mainClass == null || mainClass.length () == 0) {
            return -1;
        }
        
        ClassPath classPath = ClassPath.getClassPath (sourcesRoots[0], ClassPath.EXECUTE);  //Single compilation unit
        if (J2SEProjectUtil.isMainClass (mainClass, classPath)) {
            return 0;
        }
        return -2;
    }
    
    /** Checks if given file object contains the main method.
     *
     * @param classFO file object represents java 
     * @return false if parameter is null or doesn't contain SourceCookie
     * or SourceCookie doesn't contain the main method
     */    
    public static boolean canBeRun (FileObject classFO) {
        if (classFO == null) {
            return false;
        }
        try {
            DataObject classDO = DataObject.find (classFO);
            Object obj = classDO.getCookie (SourceCookie.class);
            if (obj == null || !(obj instanceof SourceCookie)) {
                return false;
            }
            SourceCookie cookie = (SourceCookie) obj;
            // check the main class
            SourceElement source = cookie.getSource ();
            ClassElement[] classes = source.getClasses();
            boolean hasMain = false;
            for (int i = 0; i < classes.length; i++) {
                if (classes[i].hasMainMethod()) {
                    return true;
                }
            }
        } catch (DataObjectNotFoundException ex) {
            // can ignore it, classFO could be wrongly set
        }
        return false;
    }


    /**
     * Asks user for name of main class
     * @param mainClass current main class
     * @param projectName the name of project
     * @param ep EditableProperties
     * @param messgeType type of dialog -1 when the main class is not set, -2 when the main class in not valid
     * @return true if user selected main class
     */
    private boolean showMainClassWarning (String mainClass, String projectName, EditableProperties ep, int messageType) {
        boolean canceled;
        final JButton okButton = new JButton (NbBundle.getMessage (MainClassWarning.class, "LBL_MainClassWarning_ChooseMainClass_OK")); // NOI18N
        okButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (MainClassWarning.class, "AD_MainClassWarning_ChooseMainClass_OK"));
        
        // main class goes wrong => warning
        String message;
        switch (messageType) {
            case -1:
                message = MessageFormat.format (NbBundle.getMessage(MainClassWarning.class,"LBL_MainClassNotFound"), new Object[] {
                    projectName
                });
                break;
            case -2:
                message = MessageFormat.format (NbBundle.getMessage(MainClassWarning.class,"LBL_MainClassWrong"), new Object[] {
                    mainClass,
                    projectName
                });
                break;
            default:
                throw new IllegalArgumentException ();
        }
        final MainClassWarning panel = new MainClassWarning (message,project.getSourceRoots().getRoots());
        Object[] options = new Object[] {
            okButton,
            DialogDescriptor.CANCEL_OPTION
        };
        
        panel.addChangeListener (new ChangeListener () {
           public void stateChanged (ChangeEvent e) {
               if (e.getSource () instanceof MouseEvent && MouseUtils.isDoubleClick (((MouseEvent)e.getSource ()))) {
                   // click button and the finish dialog with selected class
                   okButton.doClick ();
               } else {
                   okButton.setEnabled (panel.getSelectedMainClass () != null);
               }
           }
        });
        
        okButton.setEnabled (false);
        DialogDescriptor desc = new DialogDescriptor (panel,
            NbBundle.getMessage (MainClassWarning.class, "CTL_MainClassWarning_Title", ProjectUtils.getInformation(project).getDisplayName()), // NOI18N
            true, options, options[0], DialogDescriptor.BOTTOM_ALIGN, null, null);
        desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
        Dialog dlg = DialogDisplayer.getDefault ().createDialog (desc);
        dlg.setVisible (true);
        if (desc.getValue() != options[0]) {
            canceled = true;
        } else {
            mainClass = panel.getSelectedMainClass ();
            canceled = false;
            ep.put ("main.class", mainClass == null ? "" : mainClass); // NOI18N
        }
        dlg.dispose();            

        return canceled;
    }

    private URL generateAppletHTML(FileObject file) {
        URL url = null;
        try {
            String buildDirProp = project.evaluator().getProperty("build.dir"); //NOI18N
            String classesDirProp = project.evaluator().getProperty("build.classes.dir"); //NOI18N
            FileObject buildDir = this.updateHelper.getAntProjectHelper().resolveFileObject(buildDirProp);
            FileObject classesDir = this.updateHelper.getAntProjectHelper().resolveFileObject(classesDirProp);

            if (buildDir == null) {
                buildDir = FileUtil.createFolder(project.getProjectDirectory(), buildDirProp);
            }

            if (classesDir == null) {
                classesDir = FileUtil.createFolder(project.getProjectDirectory(), classesDirProp);
            }
            String activePlatformName = project.evaluator().getProperty("platform.active"); //NOI18N
            url = AppletSupport.generateHtmlFileURL(file, buildDir, classesDir, activePlatformName);
        } catch (FileStateInvalidException fe) {
            //ingore
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
            return null;
        }
        return url;
    }

    private URL copyAppletHTML(FileObject file, String ext) {
        URL url = null;
        try {
            String buildDirProp = project.evaluator().getProperty("build.dir"); //NOI18N
            FileObject buildDir = updateHelper.getAntProjectHelper().resolveFileObject(buildDirProp);

            if (buildDir == null) {
                buildDir = FileUtil.createFolder(project.getProjectDirectory(), buildDirProp);
            }
            
            FileObject htmlFile = null;
            htmlFile = file.getParent().getFileObject(file.getName(), "html"); //NOI18N
            if (htmlFile == null) {
                htmlFile = file.getParent().getFileObject(file.getName(), "HTML"); //NOI18N
            }
            if (htmlFile == null) {
                return null;
            }
            
            FileObject existingFile = buildDir.getFileObject(htmlFile.getName(), htmlFile.getExt());
            if (existingFile != null) {
                existingFile.delete();
            }
            
            FileObject targetHtml = htmlFile.copy(buildDir, file.getName(), ext);
            
            if (targetHtml != null) {
                String activePlatformName = project.evaluator().getProperty("platform.active"); //NOI18N
                url = AppletSupport.getHTMLPageURL(targetHtml, activePlatformName);
            }
        } catch (FileStateInvalidException fe) {
            //ingore
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
            return null;
        }
        return url;
    }
    
}
