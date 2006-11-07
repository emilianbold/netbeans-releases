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

package org.netbeans.junit.ide;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.ErrorManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Mutex;


/** A helper class to work with projects in test cases. It is packed to
 * nbjunit-ide.jar. This jar is added to compile classpath in module_harness.xml
 * and it is added to system class loader classpath in ide_plugin_targets.xml.
 * Then it ican be used in test cases.
 */
public class ProjectSupport {
    
    /** This class is just a helper class and it should not be instantiated. */
    private ProjectSupport() {
        throw new UnsupportedOperationException("It is just a helper class.");
    }
    
    /** Opens project in specified directory.
     * @param projectDir a directory with project to open
     * @return Project instance of opened project
     */
    public static Object openProject(File projectDir) {
        final ProjectListListener listener = new ProjectListListener();
        try {
            // open project
            final Project project = OpenProjectList.fileToProject(projectDir);
            if(project == null) {
                ErrorManager.getDefault().log(ErrorManager.USER, "Project not found: "+projectDir);
                return null;
            }
            // posting the to AWT event thread
            Mutex.EVENT.writeAccess(new Runnable() {
                public void run() {
                    OpenProjectList.getDefault().addPropertyChangeListener(listener);
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
                    while (!listener.projectListChanged) {
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
            } catch (InterruptedException iex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, iex);
            }
            if (waitThread.isAlive()) {
                // time-out expired, project not opened -> interrupt the wait thread
                ErrorManager.getDefault().log(ErrorManager.USER, "Project not opened in 60 second.");
                waitThread.interrupt();
                return null;
            }
            // WAIT PROJECT OPEN - end
            // wait until metadata scanning is finished
            waitScanFinished();
            return project;
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            return null;
        } finally {
            OpenProjectList.getDefault().removePropertyChangeListener(listener);
        }
    }
    
    /** Opens project on specified path.
     * @param projectPath path to a directory with project to open
     * @return Project instance of opened project
     */
    public static Object openProject(String projectPath) {
        return openProject(new File(projectPath));
    }
    
    /** Creates an empty Java project in specified directory and opens it.
     * Its name is defined by name parameter.
     * @param projectParentPath path to directory where to create name subdirectory and
     * new project structure in that subdirectory.
     * @param name name of the project
     * @return Project instance of created project
     */
    public static Object createProject(String projectParentPath, String name) {
        return createProject(new File(projectParentPath), name);
    }
    
    /** Creates an empty Java project in specified directory and opens it.
     * Its name is defined by name parameter.
     * @param projectParentDir directory where to create name subdirectory and
     * new project structure in that subdirectory.
     * @param name name of the project
     * @return Project instance of created project
     */
    public static Object createProject(File projectParentDir, String name) {
        String mainClass = null;
        try {
            File projectDir = new File(projectParentDir, name);
            J2SEProjectGenerator.createProject(projectDir, name, mainClass, null);
            return openProject(projectDir);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            return null;
        }
    }
    
    /** Closes project with system or display name equals to given name.
     * It also closes all files open in editor. If a file is modified, all
     * changes are discarded.
     * @param name system or display name of project to be closed.
     * @return true if project is closed, false otherwise (i.e. project was
     * not found).
     */
    public static boolean closeProject(String name) {
        Project[] projects = OpenProjectList.getDefault().getOpenProjects();
        for(int i=0;i<projects.length;i++) {
            final Project project = projects[i];
            if(ProjectUtils.getInformation(project).getDisplayName().equals(name) ||
                    ProjectUtils.getInformation(project).getName().equals(name)) {
                final ProjectListListener listener = new ProjectListListener();
                // posting the to AWT event thread
                Mutex.EVENT.writeAccess(new Runnable() {
                    public void run() {
                        discardChanges(project);
                        OpenProjectList.getDefault().addPropertyChangeListener(listener);
                        OpenProjectList.getDefault().close(new Project[] {project}, true);
                    }
                });
                // WAIT PROJECT CLOSED - start
                Thread waitThread = new Thread(new Runnable() {
                    public void run() {
                        while (!listener.projectListChanged) {
                            try {
                                Thread.sleep(50);
                            }
                            catch (Exception e) {
                                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                            }
                        }
                    }
                });
                waitThread.start();
                try {
                    waitThread.join(60000L);  // wait 1 minute at the most
                } catch (InterruptedException iex) {
                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, iex);
                }
                if (waitThread.isAlive()) {
                    // time-out expired, project not opened -> interrupt the wait thread
                    ErrorManager.getDefault().log(ErrorManager.USER, "Project not closed in 60 second.");
                    waitThread.interrupt();
                    return false;
                }
                // WAIT PROJECT CLOSED - end
                return true;
            }
        }
        // project not found
        return false;
    }
    
    /** Discards all changes in modified files of given project. */
    private static void discardChanges(Project project) {
        // discard all changes in modified files
        Iterator iter = DataObject.getRegistry().getModifiedSet().iterator();
        while (iter.hasNext()) {
            DataObject dobj = (DataObject) iter.next();
            if (dobj != null) {
                FileObject fobj = dobj.getPrimaryFile();
                Project owner = FileOwnerQuery.getOwner(fobj);
                if(owner == project) {
                    dobj.setModified(false);
                }
            }
        }
    }
    
    /** Waits until metadata scanning is finished. */
    public static void waitScanFinished() {
        try {
            SourceUtils.waitScanFinished();
        } catch (InterruptedException ex) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        }
    }
    
    /** Listener for project open. */
    static class ProjectListListener implements PropertyChangeListener {
        public boolean projectListChanged = false;
        
        /** Listen for property which changes when project is hopefully opened. */
        public void propertyChange(PropertyChangeEvent evt) {
            if(OpenProjectList.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
                projectListChanged = true;
            }
        }
    }
}
