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
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.ui.customizer.MainClassWarning;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/** Action provider of the J2SE project. This is the place where to do
 * strange things to J2SE actions. E.g. compile-single.
 */
class J2SEActionProvider implements ActionProvider {
    
    // Definition of commands
    private static final String COMMAND_COMPILE_SINGLE = "compile.single"; /*XXX define somewhere*/ // NOI18N
    private static final String COMMAND_COMPILE_TEST_SINGLE = "compile.test.single"; /*XXX define somewhere*/ // NOI18N
    private static final String COMMAND_RUN = "run"; /*XXX define somewhere*/ // NOI18N
    private static final String COMMAND_DEBUG = "debug"; /*XXX define somewhere*/ // NOI18N
    private static final String COMMAND_JAVADOC = "javadoc"; /*XXX define somewhere*/ // NOI18N
    private static final String COMMAND_TEST = "test"; /*XXX define somewhere*/ // NOI18N
    private static final String COMMAND_TEST_SINGLE = "test.single"; /*XXX define somewhere*/ // NOI18N
    private static final String COMMAND_DEBUG_TEST_SINGLE = "debug.test.single"; /*XXX define somewhere*/ // NOI18N
    private static final String COMMAND_DEBUG_FIX = "debug.fix"; /*XXX define somewhere*/ // NOI18N
        
    // Commands available from J2SE project
    private static final String[] supportedActions = {
        COMMAND_BUILD, 
        COMMAND_CLEAN, 
        COMMAND_REBUILD, 
        COMMAND_COMPILE_SINGLE, 
        COMMAND_RUN, 
        COMMAND_DEBUG, 
        COMMAND_JAVADOC, 
        COMMAND_TEST, 
        COMMAND_TEST_SINGLE, 
        COMMAND_DEBUG_TEST_SINGLE, 
        COMMAND_DEBUG_FIX       
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
            commands.put(COMMAND_COMPILE_TEST_SINGLE, new String[] {"compile-test-single"}); // NOI18N
            commands.put(COMMAND_RUN, new String[] {"run"}); // NOI18N
            commands.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
            commands.put(COMMAND_JAVADOC, new String[] {"javadoc"}); // NOI18N
            commands.put(COMMAND_TEST, new String[] {"test"}); // NOI18N
            commands.put(COMMAND_TEST_SINGLE, new String[] {"test-single"}); // NOI18N
            commands.put(COMMAND_DEBUG_TEST_SINGLE, new String[] {"debug-test-single"}); // NOI18N
            commands.put(COMMAND_DEBUG_FIX, new String[] {"debug-fix"}); // NOI18N
        
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
        Properties p;
        String[] targetNames;
        
        if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            FileObject[] files = findSources( context );
            p = new Properties();
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
            FileObject[] files = findTestSources(context, true);
            p = new Properties();
            p.setProperty("test.includes", ActionUtils.antIncludesList(files, project.getTestSourceDirectory())); // NOI18N
            targetNames = new String[] {"test-single"}; // NOI18N
        } 
        else if ( command.equals( COMMAND_DEBUG_TEST_SINGLE ) ) {
            FileObject[] files = findTestSources(context, true);
            String path = FileUtil.getRelativePath(project.getTestSourceDirectory(), files[0]);
            // Convert foo/FooTest.java -> foo.FooTest
            p = new Properties();
            p.setProperty("test.class", path.substring(0, path.length() - 5).replace('/', '.')); // NOI18N
            targetNames = new String[] {"debug-test-single"}; // NOI18N
        } 
        else if ( command.equals( COMMAND_DEBUG_FIX ) ) {
            FileObject[] files = findSources( context );
            String path = null;
            p = new Properties();
            if (files != null) {
                path = FileUtil.getRelativePath(project.getSourceDirectory(), files[0]);
            } 
            else {
                files = findTestSources(context, false);
                path = FileUtil.getRelativePath(project.getTestSourceDirectory(), files[0]);
                p.setProperty("is.test", "true"); // NOI18N
            }
            // Convert foo/FooTest.java -> foo/FooTest
            if (path.endsWith(".java")) { // NOI18N
                path = path.substring(0, path.length() - 5);
            }
            p.setProperty("fix.includes", path); // NOI18N
            targetNames = new String[] {"debug-fix"}; // NOI18N
        }
        else if (command.equals (COMMAND_RUN)) {
            EditableProperties ep = antProjectHelper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);

            // check project's main class
            String mainClass = (String)ep.get ("main.class"); // NOI18N
            
            while (!isSetMainClass (mainClass)) {
                // show warning, if cancel then return
                if (showMainClassWarning (mainClass, antProjectHelper.getDisplayName (), ep)) {
                    return ;
                }
                mainClass = (String)ep.get ("main.class"); // NOI18N
                antProjectHelper.putProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
            }
            

            p = new Properties();
            p.setProperty("main.class", mainClass); // NOI18N
            targetNames = (String[])commands.get(COMMAND_RUN);
            if (targetNames == null) {
                throw new IllegalArgumentException(COMMAND_RUN);
            }
        }
        else {
            p = null;
            targetNames = (String[])commands.get(command);
            if (targetNames == null) {
                throw new IllegalArgumentException(command);
            }
        }
        
        
        try {
            ActionUtils.runTarget(findBuildXml(), targetNames, p);
        } 
        catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
        
            
    }
    
    public boolean isActionEnabled( String command, Lookup context ) {
        
        if ( findBuildXml() == null ) {
            return false;
        }
        if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            return findSources( context ) != null || findTestSources( context, false ) != null;
        }
        else if ( command.equals( COMMAND_TEST_SINGLE ) ) {
            return findTestSources( context, true ) != null;
        }
        else if ( command.equals( COMMAND_DEBUG_TEST_SINGLE ) ) {
            FileObject[] files = findTestSources( context, true );
            return files != null && files.length == 1;
        }
        else if ( command.equals( COMMAND_DEBUG_FIX ) ) {
            return findSources( context ) != null || findTestSources( context, false ) != null;
        }
        else {
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
            FileObject[] files = ActionUtils.findSelectedFiles(context, srcDir, ".java", true);
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
            FileObject[] files = ActionUtils.findSelectedFiles(context, testSrcDir, ".java", true);
            if (files != null) {
                return files;
            }
        }
        if (checkInSrcDir && testSrcDir != null) {
            FileObject srcDir = project.getSourceDirectory();
            if (srcDir != null) {
                FileObject[] files = ActionUtils.findSelectedFiles(context, srcDir, ".java", true);
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
    
    private boolean isSetMainClass (String mainClass) {
        return (mainClass != null && mainClass.length () > 0);
    }
    
    private boolean showMainClassWarning (String mainClass, String projectName, EditableProperties ep) {
        boolean canceled;
        final JButton okButton = new JButton (NbBundle.getMessage (MainClassWarning.class, "LBL_MainClassWarning_ChooseMainClass_OK")); // NOI18N
        
        // main class goes wrong => warning
        final MainClassWarning panel = new MainClassWarning (antProjectHelper.getDisplayName (), project.getSourceDirectory ());

        Object[] options = new Object[] {
            okButton,
            DialogDescriptor.CANCEL_OPTION
        };
        
        panel.addChangeListener (new ChangeListener () {
           public void stateChanged (ChangeEvent e) {
               okButton.setEnabled (panel.getSelectedMainClass () != null);
           }
        });
        
        okButton.setEnabled (false);
        DialogDescriptor desc = new DialogDescriptor (panel,
                NbBundle.getMessage (MainClassWarning.class, "CTL_MainClassWarning_Title", antProjectHelper.getDisplayName ()), // NOI18N
            true, options, options[0], DialogDescriptor.DEFAULT_ALIGN, null, null);
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
        
}
