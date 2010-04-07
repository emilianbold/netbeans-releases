/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.project.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.project.ui.NewProjectWizard;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.project.ui.OpenProjectListSettings;
import org.netbeans.modules.project.ui.ProjectUtilities;
import org.netbeans.modules.project.ui.TemplatesPanel;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

public class NewProject extends BasicAction {
        
    private boolean isPreselect = false;
    
    private RequestProcessor.Task bodyTask;

    public NewProject() {
        super(NbBundle.getMessage(NewProject.class, "LBL_NewProjectAction_Name"),
                ImageUtilities.loadImageIcon("org/netbeans/modules/project/ui/resources/newProject.png", false));
        putValue("iconBase","org/netbeans/modules/project/ui/resources/newProject.png"); //NOI18N
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(NewProject.class, "LBL_NewProjectAction_Tooltip"));
        bodyTask = new RequestProcessor( "NewProjectBody" ).create( new Runnable () { // NOI18N
            public void run () {
                doPerform ();
            }
        });
    }
    
    public static NewProject newSample() {
        NewProject np = new NewProject();
        np.setDisplayName( "New Sample" ); 
        np.isPreselect = true;
        return np;
    }

    public void actionPerformed( ActionEvent evt ) {
        bodyTask.schedule( 0 );
        
        if ( "waitFinished".equals( evt.getActionCommand() ) ) {
            bodyTask.waitFinished();
        }
    }    
        
    /*T9Y*/ NewProjectWizard prepareWizardDescriptor(FileObject fo) {
        NewProjectWizard wizard = new NewProjectWizard(fo);
            
        if ( isPreselect ) {
            // XXX make the properties public ?
            wizard.putProperty(TemplatesPanel.PRESELECT_CATEGORY, getValue(TemplatesPanel.PRESELECT_CATEGORY));
            wizard.putProperty(TemplatesPanel.PRESELECT_TEMPLATE, getValue(TemplatesPanel.PRESELECT_TEMPLATE));
        }
        else {
            wizard.putProperty(TemplatesPanel.PRESELECT_CATEGORY, null);
            wizard.putProperty(TemplatesPanel.PRESELECT_TEMPLATE, null);
        }

        FileObject folder = (FileObject) getValue(CommonProjectActions.EXISTING_SOURCES_FOLDER);
        if (folder != null) {
            wizard.putProperty(CommonProjectActions.EXISTING_SOURCES_FOLDER, folder);
        }
        return wizard;
    }
    
    private void doPerform () {
        
        FileObject fo = FileUtil.getConfigFile( "Templates/Project" ); //NOI18N
        final NewProjectWizard wizard = prepareWizardDescriptor(fo);
        
        
        SwingUtilities.invokeLater( new Runnable() {
            
            public void run() {
                try {
                    
                    Set newObjects = wizard.instantiate();
                    // #75960 - test if any folder was created during the wizard and if yes and it's empty delete it
                    Preferences prefs = NbPreferences.forModule(OpenProjectListSettings.class);
                    String nbPrjDirPath = prefs.get(OpenProjectListSettings.PROP_CREATED_PROJECTS_FOLDER, null);
                    prefs.remove(OpenProjectListSettings.PROP_CREATED_PROJECTS_FOLDER);
                    if (nbPrjDirPath != null) {
                        File prjDir = new File(nbPrjDirPath);
                        if (prjDir.exists() && prjDir.isDirectory() && prjDir.listFiles() != null && prjDir.listFiles().length == 0) {
                            prjDir.delete();
                        }
                    }
                    
                    Object mainProperty = wizard.getProperty( /* XXX Define somewhere */ "setAsMain" ); // NOI18N
                    boolean setFirstMain = true;
                    if ( mainProperty instanceof Boolean ) {
                        setFirstMain = ((Boolean)mainProperty).booleanValue();
                    }
                    final boolean setFirstMainFinal = setFirstMain;
                    
                    //#69618: the non-project cache may contain a project folder listed in newObjects:
                    ProjectManager.getDefault().clearNonProjectCache();
                    ProjectUtilities.WaitCursor.show();
                    
                    if ( newObjects != null && !newObjects.isEmpty() ) {
                        // First. Open all returned projects in the GUI.

                        LinkedList<DataObject> filesToOpen = new LinkedList<DataObject>();
                        List<Project> projectsToOpen = new LinkedList<Project>();

                        for( Iterator it = newObjects.iterator(); it.hasNext(); ) {
                            Object obj = it.next();
                            FileObject newFo;
                            DataObject newDo;
                            if (obj instanceof DataObject) {
                                newDo = (DataObject) obj;
                                newFo = newDo.getPrimaryFile();
                            } else if (obj instanceof FileObject) {
                                newFo = (FileObject) obj;
                                try {
                                    newDo = DataObject.find(newFo);
                                } catch (DataObjectNotFoundException e) {
                                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                                    continue;
                                }
                            } else {
                                ErrorManager.getDefault().log(ErrorManager.WARNING, "Found unrecognized object " + obj + " in result set from instantiate()");
                                continue;
                            }
                            // check if it's a project directory
                            if (newFo.isFolder()) {
                                try {
                                    Project p = ProjectManager.getDefault().findProject(newFo);
                                    if (p != null) {
                                        // It is a project, so schedule it to open:
                                        projectsToOpen.add(p);
                                    } else {
                                        // Just a folder to expand
                                        filesToOpen.add(newDo);
                                    }
                                } catch (IOException e) {
                                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                                    continue;
                                }
                            } else {
                                filesToOpen.add(newDo);
                            }
                        }
                        
                        Project lastProject = projectsToOpen.size() > 0 ? projectsToOpen.get(0) : null;
                        
                        Project mainProject = null;
                        if (setFirstMainFinal && lastProject != null) {
                            mainProject = lastProject;
                        }
                        
                        OpenProjectList.getDefault().open(projectsToOpen.toArray(new Project[0]), false, true, mainProject);
                        
                        // Show the project tab to show the user we did something
                        ProjectUtilities.makeProjectTabVisible();
                        
                        if (lastProject != null) {
                            // Just select and expand the project node
                            ProjectUtilities.selectAndExpandProject(lastProject);
                        }
                        // Second open the files
                        for (DataObject d : filesToOpen) { // Open the files
                            ProjectUtilities.openAndSelectNewObject(d);
                        }
                        
                    }
                    ProjectUtilities.WaitCursor.hide();
                } catch ( IOException e ) {
                    ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, e );
                }
            }
            
        } );
        
        
        
    }
    
}
