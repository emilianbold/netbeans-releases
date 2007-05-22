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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006s Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package projects.apitest;

import java.util.logging.Level;
import java.util.logging.Logger;
import projects.apitest.ProjectOpenListener;
import java.io.File;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;

/**
 *
 * @author jaromiruhrik
 */
public class Utilities {
    static Logger logger = Logger.getLogger(Utilities.class.getName());
    /** Creates a new instance of Utilities */
    public Utilities() {
    }
    
    /** Opens project in specified directory.
     * @param projectDir a directory with project to open
     * @return Project instance of opened project
     */
    public static Project openProject(File projectDir) {
        final ProjectOpenListener listener = new ProjectOpenListener();
        try {
            // open project
            final Project project = FileOwnerQuery.getOwner(FileUtil.toFileObject(projectDir));
            // posting the to AWT event thread
            Mutex.EVENT.writeAccess(new Runnable() {
                public void run() {
                    OpenProjects.getDefault().addPropertyChangeListener(listener);
                    OpenProjects.getDefault().open(new Project[]{project},false);
                    // Set main? Probably user should do this if he wants.
                    // OpenProjectList.getDefault().setMainProject(project);
                }
            });
            // WAIT PROJECT OPEN - start
            // We need to wait until project is open and then we can start to
            // wait when scanning finishes. If we don't wait, scanning is started
            // too early and finishes immediatelly.
            listener.waitFinished();
//            try {
//                waitThread.join(60000L);  // wait 1 minute at the most
//            } catch (InterruptedException iex) {
//                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, iex);
//            }
//            if (waitThread.isAlive()) {
//                // time-out expired, project not opened -> interrupt the wait thread
//                ErrorManager.getDefault().log(ErrorManager.USER, "Project not opened in 60 second.");
//                waitThread.interrupt();
//            }
//            // WAIT PROJECT OPEN - end
//            // wait until metadata scanning is finished
//            waitScanFinished();
            return project;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        } finally {
            OpenProjects.getDefault().removePropertyChangeListener(listener);
        }
    }
    /** Opens project on specified path.
     * @param projectPath path to a directory with project to open
     * @return Project instance of opened project
     */
    public static Object openProject(String projectPath) {
        return openProject(new File(projectPath));
    }
    
    /** Closes project with given name.
     * @param name system or display name of project to be closed.
     * @return true if project is closed, false in other cases.
     */
    public static boolean closeProject(String name){
        Project[] projectList = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i < projectList.length; i++) {
            final Project project = projectList[i];
            if(ProjectUtils.getInformation(project).getDisplayName().equals(name) || ProjectUtils.getInformation(project).getName().equals(name)){
                Mutex.EVENT.writeAccess(new Runnable(){
                    public void run(){
                        OpenProjects.getDefault().close(new Project[] {project});
                    }
                });
                return true;
            }
        }
        return false;
    }
    
    public static boolean deleteProjectFolder(String projectFolder){
        File folder = new File(projectFolder);
        return deleteDirContent(folder);
    }
    
    private static boolean deleteDirContent(File dir){
        File[] files = dir.listFiles();
        if(files != null){
            for (int i = 0; i < files.length; i++) {
                if(files[i].isFile()){
                    files[i].delete();
                }else{
                    deleteDirContent(files[i]);
                }
            }
        }
        return dir.delete();
    }
}
