/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject;

import java.awt.Dialog;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
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
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.java.j2seproject.applet.AppletSupport;
import org.openide.filesystems.FileStateInvalidException;
import java.net.URL;

import org.netbeans.api.project.ProjectUtils;

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
    private AntProjectHelper antProjectHelper;
    
        
    /** Map from commands to ant targets */
    Map/*<String,String[]>*/ commands;
    
    public J2SEActionProvider( J2SEProject project, AntProjectHelper antProjectHelper ) {
        
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
        
        this.antProjectHelper = antProjectHelper;
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
            FileObject[] files = findSources( context );
            if (files != null) {
                p.setProperty("javac.includes", ActionUtils.antIncludesList(files, project.getSourceDirectory())); // NOI18N
                targetNames = new String[] {"compile-single"}; // NOI18N
            } 
            else {
                files = findTestSources(context, false);
                p.setProperty("javac.includes", ActionUtils.antIncludesList(files, project.getTestSourceDirectory())); // NOI18N
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
                path = FileUtil.getRelativePath(project.getSourceDirectory(), files[0]);
                targetNames = new String[] {"debug-fix"}; // NOI18N
            } else {
                files = findTestSources(context, false);
                path = FileUtil.getRelativePath(project.getTestSourceDirectory(), files[0]);
                targetNames = new String[] {"debug-fix-test"}; // NOI18N
            }
            // Convert foo/FooTest.java -> foo/FooTest
            if (path.endsWith(".java")) { // NOI18N
                path = path.substring(0, path.length() - 5);
            }
            p.setProperty("fix.includes", path); // NOI18N
        }
        else if (command.equals (COMMAND_RUN) || command.equals(COMMAND_DEBUG) || command.equals(COMMAND_DEBUG_STEP_INTO)) {
            EditableProperties ep = antProjectHelper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);

            // check project's main class
            String mainClass = (String)ep.get ("main.class"); // NOI18N
            
            while (!isSetMainClass (J2SEProjectUtil.getProjectSourceDirectory (project), mainClass)) {
                // show warning, if cancel then return
                if (showMainClassWarning (mainClass, ProjectUtils.getInformation(project).getDisplayName(), ep)) {
                    return null;
                }
                mainClass = (String)ep.get ("main.class"); // NOI18N
                antProjectHelper.putProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
            }

            p.setProperty("main.class", mainClass); // NOI18N
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
                String clazz = FileUtil.getRelativePath(project.getSourceDirectory(), file);
                p.setProperty("javac.includes", clazz); // NOI18N
                // Convert foo/FooTest.java -> foo.FooTest
                if (clazz.endsWith(".java")) { // NOI18N
                    clazz = clazz.substring(0, clazz.length() - 5);
                }
                clazz = clazz.replace('/','.');

                if (!J2SEProjectUtil.hasMainMethod (file)) {
                    if (AppletSupport.isApplet(file)) {
                        URL url = generateAppletHTML(file);
                        if (url == null) {
                            return null;
                        }
                        p.setProperty("applet.url", url.toString()); // NOI18N
                        if (command.equals (COMMAND_RUN_SINGLE)) {
                            targetNames = new String[] {"run-applet"}; // NOI18N
                        } else {
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
        p.setProperty("test.includes", ActionUtils.antIncludesList(files, project.getTestSourceDirectory())); // NOI18N
        p.setProperty("javac.includes", ActionUtils.antIncludesList(files, project.getTestSourceDirectory())); // NOI18N
        return new String[] {"test-single"}; // NOI18N
    }

    private String[] setupDebugTestSingle(Properties p, FileObject[] files) {
        String path = FileUtil.getRelativePath(project.getTestSourceDirectory(), files[0]);
        // Convert foo/FooTest.java -> foo.FooTest
        p.setProperty("test.class", path.substring(0, path.length() - 5).replace('/', '.')); // NOI18N
        return new String[] {"debug-test"}; // NOI18N
    }

    public boolean isActionEnabled( String command, Lookup context ) {
        if ( findBuildXml() == null ) {
            return false;
        }
        if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            return findSources( context ) != null || findTestSources( context, false ) != null;
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
    
    /** Find selected sources 
     */
    private FileObject[] findSources(Lookup context) {
        FileObject srcDir = project.getSourceDirectory();
        if (srcDir != null) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, srcDir, ".java", true); // NOI18N
            return files;
        } else {
            return null;
        }
    }
    
    /** Find either selected tests or tests which belong to selected source files
     */
    private FileObject[] findTestSources(Lookup context, boolean checkInSrcDir) {
        FileObject testSrcDir = project.getTestSourceDirectory();
        if (testSrcDir != null) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, testSrcDir, ".java", true); // NOI18N
            if (files != null) {
                return files;
            }
        }
        if (checkInSrcDir && testSrcDir != null) {
            FileObject srcDir = project.getSourceDirectory();
            if (srcDir != null) {
                FileObject[] files = ActionUtils.findSelectedFiles(context, srcDir, ".java", true); // NOI18N
                if (files != null) {
                    FileObject[] files2 = ActionUtils.regexpMapFiles(files, srcDir, SRCDIRJAVA, testSrcDir, SUBST, true);
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
        FileObject testSrcDir = project.getTestSourceDirectory();
        FileObject srcDir = project.getSourceDirectory();
        return ActionUtils.regexpMapFiles(sourceFiles, srcDir, SRCDIRJAVA, testSrcDir, SUBST, true);
    }    
    
    private boolean isSetMainClass (FileObject sourcesRoot, String mainClass) {
        if (mainClass == null || mainClass.length () == 0) {
            return false;
        }
        
        return J2SEProjectUtil.isMainClass (mainClass, sourcesRoot);
    }
    
    private boolean showMainClassWarning (String mainClass, String projectName, EditableProperties ep) {
        boolean canceled;
        final JButton okButton = new JButton (NbBundle.getMessage (MainClassWarning.class, "LBL_MainClassWarning_ChooseMainClass_OK")); // NOI18N
        
        // main class goes wrong => warning
        final MainClassWarning panel = new MainClassWarning (ProjectUtils.getInformation(project).getDisplayName(), project.getSourceDirectory ());

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
            FileObject buildDir = antProjectHelper.resolveFileObject(buildDirProp);
            FileObject classesDir = antProjectHelper.resolveFileObject(classesDirProp);

            if (buildDir == null) {
                buildDir = FileUtil.createFolder(project.getProjectDirectory(), buildDirProp);
            }

            if (classesDir == null) {
                classesDir = FileUtil.createFolder(project.getProjectDirectory(), classesDirProp);
            }
            url = AppletSupport.generateHtmlFileURL(file, buildDir, classesDir);
        } catch (FileStateInvalidException fe) {
            //ingore
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
            return null;
        }
        return url;
    }
    
}
