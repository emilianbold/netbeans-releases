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

package gui.setup;

import java.beans.PropertyVetoException;

import java.io.File;
import java.io.IOException;

import java.util.Enumeration;

import java.net.URL;
import java.net.URLClassLoader;

import org.netbeans.api.project.Project;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.project.ui.OpenProjectListSettings;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.MainWindowOperator;

import org.netbeans.jemmy.operators.JMenuBarOperator;


import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.modules.InstalledFileLocator;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

import org.netbeans.modules.editor.java.JCStorage;
import org.netbeans.modules.vcs.advanced.CommandLineVcsFileSystem;
import org.netbeans.modules.vcscore.settings.GeneralVcsSettings;

public class IDESetupTest extends NbTestCase {
    
    public IDESetupTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new NbTestSuite(IDESetupTest.class);
    }
    
    /** Configures filesystems. */
    public void testSetupFilesystems() {
        Repository r = Repository.getDefault();
        File src = new File (System.getProperty("xtest.sketchpad"), "srcdir");
        
        InstalledFileLocator ilf = InstalledFileLocator.getDefault();
        File jarFile = ilf.locate("modules/autoload/ext/xerces-2.6.0.jar", null, false);
        if (jarFile == null) {
            // NB3.5.1 ships with older version
            jarFile = ilf.locate("modules/autoload/ext/xerces-2.0.2-1.jar", null, false);
        }
        File vcs = new File (System.getProperty("xtest.sketchpad"), "vcsdir");
    }


    
    
    public void testOpenWebProject() {
        openProject(System.getProperty("xtest.data")+"/PerformanceTestWebApplication");
    }
    
    
  
    public void openProject(String projectPath) {
        File projectDir = new File(projectPath);
        
        OpenProjectListSettings opls = OpenProjectListSettings.getInstance();
        
        Project project = OpenProjectList.fileToProject( projectDir );
        
        if ( project != null ) {
            OpenProjectList.getDefault().open(
            project,                    // Put the project into OpenProjectList
            opls.isOpenSubprojects() ); // And optionaly open subprojects
            
            if ( opls.isOpenAsMain() ) {
                // Set main project if selected
                OpenProjectList.getDefault().setMainProject( project );
            }
        } else {
        }
    }
    
    
    /* public void openProject(String projectName) {
        final MainWithProjectsInterface projectsHandle;
        try {
            projectsHandle = (MainWithProjectsInterface)new WithProjectsClassLoader().loadClass("org.netbeans.xtest.plugin.ide.MainWithProjects").newInstance();
        } catch (Exception e) {
            System.err.println(e);
            return;
        }
        projectsHandle.openProject(System.getProperty(projectName));
        
    }

    
    private static class WithProjectsClassLoader extends URLClassLoader {
        public WithProjectsClassLoader() {
            super(new URL[] {IDESetupTest.class.getProtectionDomain().getCodeSource().getLocation()},
                  Thread.currentThread().getContextClassLoader());
        }
        protected Class loadClass(String n, boolean r) throws ClassNotFoundException {
            if (n.startsWith("org.netbeans.xtest.plugin.ide.MainWithProjects")) { // NOI18N
                // Do not proxy to parent!
                Class c = findLoadedClass(n);
                if (c != null) return c;
                c = findClass(n);
                if (r) resolveClass(c);
                return c;
            } else {
                return super.loadClass(n, r);
            }
        }
    }
    
    public static interface MainWithProjectsInterface {
        void openProject(String projectPath);
        void createProject(String projectDir);
    }
*/   
    /** 
     * Close Memory Toolbar. 
     *
     *
     *  Memory Toolbar isn't present in trunk build if run in tests now.
     *
     *
     *
    public void testCloseMemoryToolbar(){
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenu("View|Toolbars|Memory","|");
    }
*/    
    
    private FileSystem installVcs(Repository r, File f) {
        final CommandLineVcsFileSystem fs = new CommandLineVcsFileSystem ();
        try {
            // set config file and turn off refresh
            String profile = "cvs.xml"; // NOI18N
            System.out.println("VCS profile = "+profile);
            if (fs.readConfiguration(profile)) {
                fs.setConfigFileName(profile);
            }
            fs.setAutoRefresh(GeneralVcsSettings.AUTO_REFRESH_NO_REFRESH);
            fs.disableRefresh();

            fs.setRootDirectory(f);
            r.addFileSystem(fs);
// promoD            Task t = new RequestProcessor ().post(new Runnable() {
//                public void run() {
//                    JCStorage storage = JCStorage.getStorage();
//                    storage.parseFSOnBackground (fs);
//                }
//            });  
//            t.waitFinished();
            // TODO should wait until code completion is created
        }
        catch (IOException ioe) {
            fail ("Filesystem mounting failed - "+ioe.getLocalizedMessage());
        }
        catch (PropertyVetoException ioe) {
            fail ("Filesystem mounting failed - "+ioe.getLocalizedMessage());
        }
        return fs;
    }
}
