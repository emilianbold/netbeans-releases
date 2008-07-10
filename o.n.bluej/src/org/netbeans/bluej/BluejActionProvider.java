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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.bluej;

import java.awt.Dialog;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.MouseUtils;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/** Action provider of the J2SE project. This is the place where to do
 * strange things to J2SE actions. E.g. compile-single.
 */
class BluejActionProvider implements ActionProvider {
    
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
    BluejProject project;
    
////    // Ant project helper of the project
    private UpdateHelper updateHelper;
    
    
    /** Map from commands to ant targets */
    Map/*<String,String[]>*/ commands;
    
    public BluejActionProvider(BluejProject project, UpdateHelper updateHelper) {
        
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
    
    public void invokeAction( final String command, final Lookup context ) throws IllegalArgumentException {
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
                        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(BluejActionProvider.class,
                                "LBL_No_Build_XML_Found"), NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    } else {
                        ExecutorTask task = ActionUtils.runTarget(buildFo, targetNames, p);
                    }
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        };
        
        action.run();
    }
    
    /**
     * @return array of targets or null to stop execution; can return empty array
     */
    /*private*/ String[] getTargetNames(String command, Lookup context, Properties p) throws IllegalArgumentException {
        String[] targetNames = new String[0];
        if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            FileObject[] sourceRoots = new FileObject[] { project.getProjectDirectory() };
            FileObject[] files = findSourcesAndPackages( context, sourceRoots);
            boolean recursive = (context.lookup(NonRecursiveFolder.class) == null);
            if (files != null) {
                p.setProperty("javac.includes", ActionUtils.antIncludesList(files, getRoot(sourceRoots,files[0]), recursive)); // NOI18N
                targetNames = new String[] {"compile-single"}; // NOI18N
            }
            //TODO what to do here if we have all source in one root..
////            else {
////                FileObject[] testRoots = project.getTestSourceRoots().getRoots();
////                files = findSourcesAndPackages(context, testRoots);
////                p.setProperty("javac.includes", ActionUtils.antIncludesList(files, getRoot(testRoots,files[0]), recursive)); // NOI18N
////                targetNames = new String[] {"compile-test-single"}; // NOI18N
////            }
        } else if ( command.equals( COMMAND_TEST_SINGLE ) ) {
            FileObject[] files = findTestSourcesForSources(context);
            targetNames = setupTestSingle(p, files);
        } else if ( command.equals( COMMAND_DEBUG_TEST_SINGLE ) ) {
            FileObject[] files = findTestSourcesForSources(context);
            targetNames = setupDebugTestSingle(p, files);
        }
////        else if ( command.equals( JavaProjectConstants.COMMAND_DEBUG_FIX ) ) {
////            FileObject[] files = findSources( context );
////            String path = null;
////            if (files != null) {
////                path = FileUtil.getRelativePath(getRoot(project.getProjectDirectory(),files[0]), files[0]);
////                targetNames = new String[] {"debug-fix"}; // NOI18N
////            //TODO what to do here if we have all source in one root..
////
////            } else {
////                files = findTestSources(context, false);
////                path = FileUtil.getRelativePath(getRoot(project.getProjectDirectory(),files[0]), files[0]);
////                targetNames = new String[] {"debug-fix-test"}; // NOI18N
////            }
////            // Convert foo/FooTest.java -> foo/FooTest
////            if (path.endsWith(".java")) { // NOI18N
////                path = path.substring(0, path.length() - 5);
////            }
////            p.setProperty("fix.includes", path); // NOI18N
////        }
        else if (command.equals(COMMAND_RUN) || command.equals(COMMAND_DEBUG) || command.equals(COMMAND_DEBUG_STEP_INTO)) {
            EditableProperties ep = updateHelper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
            EditableProperties eprivate = updateHelper.getProperties (AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            if (eprivate == null) {
                eprivate = new EditableProperties();
                updateHelper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, eprivate);
            }
            // check project's main class
            String mainClass = (String)ep.get ("main.class"); // NOI18N
            
//                if (!JMManager.getManager().isScanInProgress()) {
//                    // in case the value gets back in some reasonable time,
//                    // check if we have just one mainclass and use it then without a dialog.
//                    List lst = MainClassChooser.getMainClasses(new FileObject[] { project.getProjectDirectory() }, false);
//                    if (lst.size() == 1) {
//                        showDialog = false;
//                        ep.put ("main.class", lst.get(0) == null ? "" : (String)lst.get(0)); // NOI18N
//                    }
//                }
                if (mainClass == null) {
                    // show warning, if cancel then return
                    if (showMainClassWarning(mainClass, ProjectUtils.getInformation(project).getDisplayName(), ep, eprivate, -1)) {
                        return null;
                    }
                    mainClass = (String)ep.get("main.class"); // NOI18N
                }
//                result = isSetMainClass(project.getProjectDirectory(), mainClass);
            try {
                if (updateHelper.requestSave()) {
                    updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                    updateHelper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, eprivate);
                    ProjectManager.getDefault().saveProject(project);
                }
            } catch (IOException ioe) {
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Error while saving project: " + ioe);
            }
            if (!command.equals(COMMAND_RUN)) {
                p.setProperty("debug.class", mainClass); // NOI18N
            }

            targetNames = (String[])commands.get(command);
            if (targetNames == null) {
                throw new IllegalArgumentException(command);
            }
        } else if (command.equals(COMMAND_RUN_SINGLE) || command.equals(COMMAND_DEBUG_SINGLE)) {
            FileObject[] files = findTestSources(context, false);
            if (files != null) {
                if (command.equals(COMMAND_RUN_SINGLE)) {
                    targetNames = setupTestSingle(p, files);
                } else {
                    targetNames = setupDebugTestSingle(p, files);
                }
            } else {
                FileObject file = findSources(context)[0];
                String clazz = FileUtil.getRelativePath(project.getProjectDirectory(), file);
                p.setProperty("javac.includes", clazz); // NOI18N
                // Convert foo/FooTest.java -> foo.FooTest
                if (clazz.endsWith(".java")) { // NOI18N
                    clazz = clazz.substring(0, clazz.length() - 5);
                }
                clazz = clazz.replace('/','.');
                
                if (!hasMainMethod(file)) {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(BluejActionProvider.class, "LBL_No_Main_Classs_Found", clazz), NotifyDescriptor.INFORMATION_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                    return null;
                } else {
                    if (command.equals(COMMAND_RUN_SINGLE)) {
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
        FileObject[] testSrcPath = new FileObject[] {project.getProjectDirectory()};
        FileObject root = getRoot(testSrcPath, files[0]);
        p.setProperty("test.includes", ActionUtils.antIncludesList(files, root)); // NOI18N
        p.setProperty("javac.includes", ActionUtils.antIncludesList(files, root)); // NOI18N
        return new String[] {"test-single"}; // NOI18N
    }
    
    private String[] setupDebugTestSingle(Properties p, FileObject[] files) {
        FileObject[] testSrcPath = new FileObject[] {project.getProjectDirectory()};
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
        if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            return findSourcesAndPackages( context, project.getProjectDirectory()) != null
                    || findSourcesAndPackages( context, project.getProjectDirectory()) != null;
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
        FileObject[] srcPath = new FileObject[] {project.getProjectDirectory()};
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
                    if (!files[i].isFolder() && !"java".equals(files[i].getExt())) { //NOI18N
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
        FileObject[] files = ActionUtils.findSelectedFiles(context, project.getProjectDirectory(), "Test.java", true); // NOI18N
        if (files != null) {
            return files;
        }
        if (checkInSrcDir) {
            files = findSources(context);
            if (files != null) {
                //Try to find the test under the test roots
                FileObject[] files2 = ActionUtils.regexpMapFiles(files, project.getProjectDirectory(), SRCDIRJAVA, project.getProjectDirectory(), SUBST, true);
                if (files2 != null) {
                    return files2;
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
        FileObject srcDir = project.getProjectDirectory();
            FileObject[] files2 = ActionUtils.regexpMapFiles(sourceFiles, srcDir, SRCDIRJAVA, project.getProjectDirectory(), SUBST, true);
            if (files2 != null) {
                return files2;
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
    
    
////    /**
////     * Tests if the main class is set
////     * @param sourcesRoots source roots
////     * @param mainClass main class name
////     * @return 0 if the main class is set and is valid
////     *        -1 if the main class is not set
////     *        -2 if the main class is set but is not valid
////     */
////    private int isSetMainClass (FileObject[] sourcesRoots, String mainClass) {
////
////        // support for unit testing
////        if (MainClassChooser.unitTestingSupport_hasMainMethodResult != null) {
////            return MainClassChooser.unitTestingSupport_hasMainMethodResult.booleanValue () ? 0 : -2;
////        }
////
////        if (mainClass == null || mainClass.length () == 0) {
////            return -1;
////        }
////
////        ClassPath classPath = ClassPath.getClassPath (sourcesRoots[0], ClassPath.EXECUTE);  //Single compilation unit
////        if (J2SEProjectUtil.isMainClass (mainClass, classPath)) {
////            return 0;
////        }
////        return -2;
////    }
    
////    /** Checks if given file object contains the main method.
////     *
////     * @param classFO file object represents java
////     * @return false if parameter is null or doesn't contain SourceCookie
////     * or SourceCookie doesn't contain the main method
////     */
////    public static boolean canBeRun (FileObject classFO) {
////        if (classFO == null) {
////            return false;
////        }
////        try {
////            DataObject classDO = DataObject.find (classFO);
////            Object obj = classDO.getCookie (SourceCookie.class);
////            if (obj == null || !(obj instanceof SourceCookie)) {
////                return false;
////            }
////            SourceCookie cookie = (SourceCookie) obj;
////            // check the main class
////            SourceElement source = cookie.getSource ();
////            ClassElement[] classes = source.getClasses();
////            boolean hasMain = false;
////            for (int i = 0; i < classes.length; i++) {
////                if (classes[i].hasMainMethod()) {
////                    return true;
////                }
////            }
////        } catch (DataObjectNotFoundException ex) {
////            // can ignore it, classFO could be wrongly set
////        }
////        return false;
////    }
    
    
    
////    private void showPlatformWarning() {
////        final JButton closeOption = new JButton(NbBundle.getMessage(BluejActionProvider.class, "CTL_BrokenPlatform_Close"));
////        closeOption.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BluejActionProvider.class, "AD_BrokenPlatform_Close"));
////        final ProjectInformation pi = (ProjectInformation) this.project.getLookup().lookup(ProjectInformation.class);
////        final String projectDisplayName = pi == null ?
////            NbBundle.getMessage(BluejActionProvider.class,"TEXT_BrokenPlatform_UnknownProjectName")
////            : pi.getDisplayName();
////        final DialogDescriptor dd = new DialogDescriptor(
////                NbBundle.getMessage(BluejActionProvider.class, "TEXT_BrokenPlatform", projectDisplayName),
////                NbBundle.getMessage(BluejActionProvider.class, "MSG_BrokenPlatform_Title"),
////                true,
////                new Object[] {closeOption},
////                closeOption,
////                DialogDescriptor.DEFAULT_ALIGN,
////                null,
////                null);
////        dd.setMessageType(DialogDescriptor.WARNING_MESSAGE);
////        final Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
////        dlg.setVisible(true);
////    }
    
    /**
     * Asks user for name of main class
     * @param mainClass current main class
     * @param projectName the name of project
     * @param ep EditableProperties
     * @param messgeType type of dialog -1 when the main class is not set, -2 when the main class in not valid
     * @return true if user selected main class
     */
    private boolean showMainClassWarning (String mainClass, String projectName, EditableProperties ep, EditableProperties eprivate, int messageType) {
        boolean canceled;
        final JButton okButton = new JButton ("OK"); // NOI18N
//        okButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (MainClassWarning.class, "AD_MainClassWarning_ChooseMainClass_OK"));
        
        // main class goes wrong => warning
        String message;
        switch (messageType) {
            case -1:
                message = MessageFormat.format (NbBundle.getMessage(BluejActionProvider.class, "LBL_MainClassNotFound"), new Object[] {
                    projectName
                });
                break;
            case -2:
                message = MessageFormat.format (NbBundle.getMessage(BluejActionProvider.class, "LBL_MainClassWrong"), new Object[] {
                    mainClass,
                    projectName
                });
                break;
            default:
                throw new IllegalArgumentException ();
        }
        final MainClassWarning panel = new MainClassWarning (message, new FileObject[] { project.getProjectDirectory() } );
        panel.setArguments(eprivate.getProperty("application.args")); //NOI18N
        panel.setSelectedMainClass(ep.getProperty("main.class")); //NOI18N
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
        DialogDescriptor desc = new DialogDescriptor (panel, "Run Project", // NOI18N
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
            eprivate.put("application.args", panel.getArguments()); //NOI18N
        }
        dlg.dispose();            

        return canceled;
    }
    
    
    /** Check if the given file object represents a source with the main method.
     * 
     * @param fo source
     * @return true if the source contains the main method
     */
    public static boolean hasMainMethod(FileObject fo) {
        if (fo == null) {
            // ??? maybe better should be thrown IAE
            return false;
        }
        return !SourceUtils.getMainClasses(fo).isEmpty();
        }
    
}
