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
package org.netbeans.xtest.plugin.ide;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.ErrorManager;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.javacore.JMManager;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.openide.util.Mutex;

/**
 * Portion of Main that needs to run with access to Projects API & impl.
 * @author Jan Chalupa, Jesse Glick, Jiri Skrivanek
 */
public class MainWithProjects implements Main.MainWithProjectsInterface, PropertyChangeListener {
    
    /** Opens project on specified path.
     * @param projectPath path to a directory with project to open
     */
    public void openProject(String projectPath) {
        openProject(new File(projectPath));
    }
    
    /** Listen for property which changes when project is hopefully opened. */
    public void propertyChange(PropertyChangeEvent evt) {
        if(OpenProjectList.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
            projectOpened = true;
        }
    }
    
    /** Signal that project was opened. */
    boolean projectOpened = false;
    
    /** Opens project in specified directory.
     * @param projectDir a directory with project to open
     */
    public void openProject(File projectDir) {
        try {
            // open project
            final Project project = OpenProjectList.fileToProject(projectDir);
            final MainWithProjects instance = this;
            // posting the to AWT event thread
            Mutex.EVENT.writeAccess(new Runnable() {
                public void run() {
                    OpenProjectList.getDefault().addPropertyChangeListener(instance);
                    OpenProjectList.getDefault().open(project);
                    // Set main? Probably user should do this if he wants.
                    // OpenProjectList.getDefault().setMainProject(project);
                }
            });
            // WAIT PROJECT OPEN - start
            // We need to wait until project is open and then we can start to 
            // wait when scanning finishes. If we don't wait, scanning is started
            // too early and finishes immediatelly.
            Thread waitThread = new Thread(new Runnable() {
                public void run() {
                    while(!projectOpened) {
                        try {
                            Thread.sleep(50);
                        } catch (Exception e) {
                            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                        }
                    }
                }
            });
            waitThread.start();
            try {
                waitThread.join(60000L);  // wait 1 minute at the most
            }
            catch (InterruptedException iex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, iex);
            }
            if (waitThread.isAlive()) {
                // time-out expired, project not opened -> interrupt the wait thread
                ErrorManager.getDefault().log(ErrorManager.USER, "Project not opened in 60 second.");
                waitThread.interrupt();
            }
            // WAIT PROJECT OPEN - end
            // wait until metadata scanning is finished
            ((JMManager)JMManager.getManager()).waitScanFinished();
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        } finally {
            OpenProjectList.getDefault().removePropertyChangeListener(this);
        }
    }
    
    /** Creates an empty Java project in specified directory and opens it. 
     * Its name is XTestProject. It is cleaned
     * after each test bag if the property test.reuse.ide is not set to true.
     * @param projectDir directory where to create XTestProject subdirectory and
     * new project structure in that subdirectory.
     */
    public void createProject(String projectDir) {
        File dir = new File(projectDir, "XTestProject");
        String name = "XTestProject";
        String mainClass = null;
        try {
            J2SEProjectGenerator.createProject(dir, name, mainClass, null);
            openProject(dir);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        }
    }

}
