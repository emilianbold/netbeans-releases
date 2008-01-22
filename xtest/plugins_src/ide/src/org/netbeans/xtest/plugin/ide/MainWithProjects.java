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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.xtest.plugin.ide;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.ErrorManager;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
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
            if(project == null) {
                ErrorManager.getDefault().notify(new Exception("Project not found: "+projectDir));
                return;
            }
            final MainWithProjects instance = this;
            projectOpened = false;
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
            // wait until classpath scanning is finished
            SourceUtils.waitScanFinished();
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
            J2SEProjectGenerator.createProject(dir, name, mainClass, null, null);
            openProject(dir);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        }
    }

}
