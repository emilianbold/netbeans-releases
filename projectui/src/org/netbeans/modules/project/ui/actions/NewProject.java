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
import org.openide.util.Utilities;

public class NewProject extends BasicAction {
        
    private static final Icon ICON = new ImageIcon( Utilities.loadImage( "org/netbeans/modules/project/ui/resources/newProject.gif" ) ); //NOI18N    
    private static final String NAME = NbBundle.getMessage( NewProject.class, "LBL_NewProjectAction_Name" ); // NOI18N
    
    
    private static NewProjectWizard wizard;

    public NewProject() {
        super( NAME, ICON );
        putValue("iconBase","org/netbeans/modules/project/ui/resources/newProject.gif"); //NOI18N
    }

    public void actionPerformed( ActionEvent evt ) {

        if ( wizard == null ) {
            FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource( "Templates/Project" ); //NOI18N                
            wizard = new NewProjectWizard(fo);
        }
        else {
            //Reset the inline message
            wizard.putProperty( "WizardPanel_errorMessage", "");  //NOI18N
        }

        try {
            Set newObjects = wizard.instantiate ();
            Object mainProperty = wizard.getProperty( /* XXX Define somewhere */ "setAsMain" ); // NOI18N
            boolean setFirstMain = true;
            if ( mainProperty instanceof Boolean ) {
                setFirstMain = ((Boolean)mainProperty).booleanValue();
            }

            if ( newObjects != null && !newObjects.isEmpty() ) { 
                // First. Open all returned projects in the GUI.
                
                LinkedList filesToOpen = new LinkedList();
                
                for( Iterator it = newObjects.iterator(); it.hasNext(); ) {
                    Object obj = it.next ();
                    FileObject newFo = null;
                    if (obj instanceof DataObject) {
                        // old style way with Set/*DataObject*/
                        final DataObject newDo = (DataObject)obj;
                        
                        // check if it's project's directory
                        if (newDo.getPrimaryFile ().isFolder ()) {
                            Project p = ProjectManager.getDefault().findProject( newDo.getPrimaryFile () );
                            if ( p != null ) {
                                OpenProjectList.getDefault().open( p, true );
                                if ( setFirstMain ) {
                                    OpenProjectList.getDefault().setMainProject( p );
                                    setFirstMain = false;
                                }
                            }
                        } else {
                            filesToOpen.add( newDo );                            
                        }
                    } else {
                        assert false : obj;
                    }
                }
                // Second open the files                
                for( Iterator it = filesToOpen.iterator(); it.hasNext(); ) {
                    ProjectUtilities.openAndSelectNewObject( (DataObject)it.next() );
                }
                
            }
        }
        catch ( IOException e ) {
            ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, e );
        }
    }
    
}