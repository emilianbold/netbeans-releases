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
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.project.ui.NewProjectWizard;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.project.ui.ProjectUtilities;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

public class NewProject extends BasicAction {
        
    private static final Icon ICON = new ImageIcon( Utilities.loadImage( "org/netbeans/modules/project/ui/resources/newProject.gif" ) ); //NOI18N    
    private static final String NAME = NbBundle.getMessage( NewProject.class, "LBL_NewProjectAction_Name" ); // NOI18N
    
    private boolean isPreselect = false;
    
    private RequestProcessor.Task bodyTask;

    public NewProject() {
        super( NAME, ICON );
        putValue("iconBase","org/netbeans/modules/project/ui/resources/newProject.gif"); //NOI18N
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
        
    private void doPerform () {
        
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource( "Templates/Project" ); //NOI18N                
        NewProjectWizard wizard = new NewProjectWizard(fo);
            
        if ( isPreselect ) {
            // XXX make the properties public ?
            wizard.putProperty( "PRESELECT_CATEGORY", getValue ("PRESELECT_CATEGORY")); 
            wizard.putProperty( "PRESELECT_TEMPLATE", getValue ("PRESELECT_TEMPLATE")); 
        }
        else {
            wizard.putProperty( "PRESELECT_CATEGORY", null ); 
            wizard.putProperty( "PRESELECT_TEMPLATE", null ); 
        }
        // generic way to pass down action values to wizard properties
        wizard.putProperty("javax.swing.Action", this);  //  NOI18N
        
        try {
                        
            final Set newObjects = wizard.instantiate ();            
            Object mainProperty = wizard.getProperty( /* XXX Define somewhere */ "setAsMain" ); // NOI18N
            boolean setFirstMain = true;
            if ( mainProperty instanceof Boolean ) {
                setFirstMain = ((Boolean)mainProperty).booleanValue();
            }
            final boolean setFirstMainFinal = setFirstMain; 
            
            SwingUtilities.invokeLater( new Runnable() {
            
                public void run() {
                    ProjectUtilities.WaitCursor.show();
                    
                    if ( newObjects != null && !newObjects.isEmpty() ) { 
                        // First. Open all returned projects in the GUI.

                        LinkedList filesToOpen = new LinkedList();

                        Project p = null;        
                        for( Iterator it = newObjects.iterator(); it.hasNext(); ) {
                            Object obj = it.next ();
                            FileObject newFo = null;
                            if (obj instanceof DataObject) {
                                // old style way with Set/*DataObject*/
                                final DataObject newDo = (DataObject)obj;
                                boolean mainProjectSet = false;
                                
                                // check if it's project's directory
                                if (newDo.getPrimaryFile ().isFolder ()) {
                                    try {
                                        p = ProjectManager.getDefault().findProject( newDo.getPrimaryFile () );                            
                                        if ( p != null ) {
                                            // It is a project open it
                                            OpenProjectList.getDefault().open( p, true );
                                            if ( setFirstMainFinal && !mainProjectSet ) {
                                                OpenProjectList.getDefault().setMainProject( p );
                                                mainProjectSet = true;
                                            }
                                        }
                                        else {
                                            // Just a folder to expand
                                            filesToOpen.add( newDo );
                                        }
                                    }
                                    catch ( IOException e ) {
                                        continue;
                                    }
                                }                                 
                                else {                            
                                    filesToOpen.add( newDo );                            
                                }
                            } 
                            else {
                                assert false : obj;
                            }
                        }
                        
                        // Show the project tab to show the user we did something
                        ProjectUtilities.makeProjectTabVisible( true );
                        
                        // Second open the files                
                        if ( filesToOpen.isEmpty() && p != null) {
                            // Just select and expand the project node
                            ProjectUtilities.selectAndExpandProject( p );
                        }
                        else {
                            for( Iterator it = filesToOpen.iterator(); it.hasNext(); ) { // Open the files
                                ProjectUtilities.openAndSelectNewObject( (DataObject)it.next() );
                            }
                        }                                                

                    }
                    ProjectUtilities.WaitCursor.hide();
                }
            } );
        }
        catch ( IOException e ) {
            ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, e );
        }
        
        
    }
    
}
