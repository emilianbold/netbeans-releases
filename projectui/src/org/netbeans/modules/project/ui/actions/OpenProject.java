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

package org.netbeans.modules.project.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.project.ui.OpenProjectListSettings;
import org.netbeans.modules.project.ui.ProjectChooserAccessory;
import org.netbeans.modules.project.ui.ProjectTab;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

public class OpenProject extends BasicAction {
    
    private static final String DISPLAY_NAME = NbBundle.getMessage( OpenProject.class, "LBL_OpenProjectAction_Name" ); // NOI18N
    private static final String _SHORT_DESCRIPTION = NbBundle.getMessage( OpenProject.class, "LBL_OpenProjectAction_Tooltip" ); // NOI18N
        
    /** Creates a new instance of BrowserAction */
    public OpenProject() {
        super( DISPLAY_NAME, new ImageIcon( Utilities.loadImage( "org/netbeans/modules/project/ui/resources/openProject.png" ) ) );
        putValue("iconBase","org/netbeans/modules/project/ui/resources/openProject.png"); //NOI18N
        putValue(SHORT_DESCRIPTION, _SHORT_DESCRIPTION);
    }

    public void actionPerformed( ActionEvent evt ) {
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
                    OpenProjectList.getDefault().open( 
                        projectsArray,                    // Put the project into OpenProjectList
                        opls.isOpenSubprojects(),         // And optionaly open subprojects
			true);                            // open asynchronously
                    if ( opls.isOpenAsMain() && projectsArray.length == 1 ) {
                        // Set main project if selected
                        OpenProjectList.getDefault().setMainProject( projectsArray[0] );
                    }
                    final ProjectTab ptLogial  = ProjectTab.findDefault (ProjectTab.ID_LOGICAL);
                    
                    // invoke later to select the being opened project if the focus is outside ProjectTab
                    SwingUtilities.invokeLater (new Runnable () {
                        public void run () {
                            Node root = ptLogial.getExplorerManager ().getRootContext ();
                            
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
                                ptLogial.getExplorerManager ().setSelectedNodes (nodesArray);
                                if (!Boolean.getBoolean("project.tab.no.selection")) { //NOI18N
                                    ptLogial.open ();
                                    ptLogial.requestActive ();
                                }
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
        
        opls.setLastOpenProjectDir( chooser.getCurrentDirectory().getPath() );
        
    }
    
        
}
