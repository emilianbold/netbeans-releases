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

package org.netbeans.modules.project.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.project.ui.OpenProjectListSettings;
import org.netbeans.modules.project.ui.ProjectChooserAccessory;
import org.netbeans.modules.project.ui.ProjectTab;
import org.netbeans.modules.project.ui.ProjectUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

public class OpenProject extends BasicAction {
    
    private static final Icon ICON = new ImageIcon( Utilities.loadImage( "org/netbeans/modules/project/ui/resources/openProject.gif" ) ); //NOI18N    
    private static final String NAME = NbBundle.getMessage( OpenProject.class, "LBL_OpenProjectAction_Name" ); // NOI18N
        
    /** Creates a new instance of BrowserAction */
    public OpenProject() {
        super( NAME, ICON );
        putValue("iconBase","org/netbeans/modules/project/ui/resources/openProject.gif"); //NOI18N
    }

    public void actionPerformed( ActionEvent evt ) {
        JFileChooser chooser = ProjectChooserAccessory.createProjectChooser( true ); // Create the jFileChooser
        
        OpenProjectListSettings opls = OpenProjectListSettings.getInstance();
        
        while( true ) {  // Cycle while users does some reasonable action e.g.
                         // select project dir or cancel the chooser
        
            int option = chooser.showOpenDialog( WindowManager.getDefault().getMainWindow() ); // Sow the chooser
              
            if ( option == JFileChooser.APPROVE_OPTION ) {

                final File projectDir = FileUtil.normalizeFile(chooser.getSelectedFile());

                Project project = OpenProjectList.fileToProject( projectDir ); 
                
                if ( project == null ) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                NbBundle.getMessage( OpenProject.class, "MSG_notProjectDir"), // NOI18N
                                NotifyDescriptor.WARNING_MESSAGE));                    
                }
                else {
                    OpenProjectList.getDefault().open( 
                        project,                    // Put the project into OpenProjectList
                        opls.isOpenSubprojects() ); // And optionaly open subprojects
                    if ( opls.isOpenAsMain() ) {
                        // Set main project if selected
                        OpenProjectList.getDefault().setMainProject( project );
                    }
                    final ProjectTab ptLogial  = ProjectTab.findDefault (ProjectTab.ID_LOGICAL);
                    
                    // invoke later to select the being opened project if the focus is outside ProjectTab
                    SwingUtilities.invokeLater (new Runnable () {
                        public void run () {
                            Node root = ptLogial.getExplorerManager ().getRootContext ();
                            Node projNode = root.getChildren ().findChild (projectDir.getName ());
                            try {
                                ptLogial.getExplorerManager ().setSelectedNodes (new Node[] {projNode});
                                ptLogial.open ();
                                ptLogial.requestActive ();
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
