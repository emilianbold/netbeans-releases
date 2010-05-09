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
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.project.ui.OpenProjectListSettings;
import org.netbeans.modules.project.ui.ProjectChooserAccessory;
import org.netbeans.modules.project.ui.ProjectTab;
import org.netbeans.modules.project.ui.ProjectUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

public class OpenProject extends BasicAction {
    
    private static final String DISPLAY_NAME = NbBundle.getMessage( OpenProject.class, "LBL_OpenProjectAction_Name" ); // NOI18N
    private static final String _SHORT_DESCRIPTION = NbBundle.getMessage( OpenProject.class, "LBL_OpenProjectAction_Tooltip" ); // NOI18N
        
    /** Creates a new instance of BrowserAction */
    public OpenProject() {
        super( DISPLAY_NAME, ImageUtilities.loadImageIcon("org/netbeans/modules/project/ui/resources/openProject.png", false));
        putValue("iconBase","org/netbeans/modules/project/ui/resources/openProject.png"); //NOI18N
        putValue(SHORT_DESCRIPTION, _SHORT_DESCRIPTION);
    }

    public void actionPerformed( ActionEvent evt ) {
        Project projectToExpand = null;
        JFileChooser chooser = ProjectChooserAccessory.createProjectChooser( true ); // Create the jFileChooser
        chooser.setMultiSelectionEnabled( true );
        
        // Check to see if the current selection matches a file/folder owned by a non-open project;
        // if so, use that as the starting directory, as a convenience in case that is what should be opened.
        // XXX may also want to check lookup for FileObject
        for (DataObject d : Utilities.actionsGlobalContext().lookupAll(DataObject.class)) {
            Project selected = FileOwnerQuery.getOwner(d.getPrimaryFile());
            if (selected != null && !OpenProjectList.getDefault().isOpen(selected)) {
                File dir = FileUtil.toFile(selected.getProjectDirectory());
                if (dir != null) {
                    chooser.setCurrentDirectory(dir.getParentFile());
                    chooser.setSelectedFiles(new File[] {dir});
                    break;
                }
            }
        }
        
        OpenProjectListSettings opls = OpenProjectListSettings.getInstance();
        
        while( true ) {  // Cycle while users does some reasonable action e.g.
                         // select project dir or cancel the chooser
        
            int option = chooser.showOpenDialog( WindowManager.getDefault().getMainWindow() ); // Sow the chooser
              
            if ( option == JFileChooser.APPROVE_OPTION ) {

                final File[] projectDirs;
                if ( chooser.isMultiSelectionEnabled() ) {                    
                    projectDirs = chooser.getSelectedFiles();
                }
                else {
                    projectDirs = new File[] { chooser.getSelectedFile() };
                }
                
                // Project project = OpenProjectList.fileToProject( projectDir ); 
                ArrayList<Project> projects = new ArrayList<Project>( projectDirs.length );
                for (File d : projectDirs) {
                    Project p = OpenProjectList.fileToProject(FileUtil.normalizeFile(d));
                    if ( p != null ) {
                        projects.add( p );
                    }
                }
                
                if ( projects.isEmpty() ) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                NbBundle.getMessage( OpenProject.class, "MSG_notProjectDir"), // NOI18N
                                NotifyDescriptor.WARNING_MESSAGE));                    
                }
                else {
                    Project projectsArray[] = new Project[ projects.size() ];
                    projects.toArray( projectsArray );
                    
                    Project mainProject = null;
                    if ( opls.isOpenAsMain() && projectsArray.length == 1 ) {
                        // Set main project if selected
                        mainProject = projectsArray[0];
                    }

                    if (projectsArray.length == 1) {
                        projectToExpand = projectsArray[0];
                    }

                    OpenProjectList.getDefault().open( 
                        projectsArray,                    // Put the project into OpenProjectList
                        opls.isOpenSubprojects(),         // And optionaly open subprojects
                        true,                             // open asynchronously
                        mainProject);
                    
                    final ProjectTab ptLogical  = ProjectTab.findDefault (ProjectTab.ID_LOGICAL);
                    
                    // invoke later to select the being opened project if the focus is outside ProjectTab
                    SwingUtilities.invokeLater (new Runnable () {
                        public void run () {
                            Node root = ptLogical.getExplorerManager ().getRootContext ();
                            
                            ArrayList<Node> nodes = new ArrayList<Node>( projectDirs.length );
                            for( int i = 0; i < projectDirs.length; i++ ) {                
                                Node projNode = root.getChildren ().findChild (projectDirs[i].getName () );
                                if ( projNode != null ) {
                                    nodes.add( projNode );
                                }
                            }
                            try {
                                Node[] nodesArray = new Node[ nodes.size() ];
                                nodes.toArray( nodesArray );
                                ptLogical.getExplorerManager ().setSelectedNodes (nodesArray);
                                ProjectUtilities.makeProjectTabVisible();
                            } catch (Exception ignore) {
                                // may ignore it
                            }
                        }
                    });
                    break; // and exit the loop
                }
            }
            else {
                return ; // OK user changed his mind and won't open anything
                         // Don't remeber the last selected dir
            }
        }

        if (projectToExpand != null) {
            ProjectUtilities.selectAndExpandProject(projectToExpand);
        }
        opls.setLastOpenProjectDir( chooser.getCurrentDirectory().getPath() );
        
    }
    
        
}
