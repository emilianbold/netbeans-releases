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
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

public class NewProject extends BasicAction {
        
    private static TemplateWizard wizard;

    public NewProject() {
        super( NbBundle.getMessage( NewProject.class, "LBL_NewProjectAction_Name" ),  // NOI18N
               "org/netbeans/modules/project/ui/resources/newProject.gif" );        //NOI18N 
    }

    public void actionPerformed( ActionEvent evt ) {

        if ( wizard == null ) {
            wizard = new TemplateWizard();                
            FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource( "Templates/Project" ); //NOI18N                

            DataFolder templates = DataFolder.findFolder( fo );
            wizard.setTemplatesFolder( templates ); 
        }

        try {
            Set dataObjects = wizard.instantiate();

            Object mainProperty = wizard.getProperty( /* XXX Define somewhere */ "setAsMain" ); // NOI18N
            boolean main = true;
            if ( mainProperty instanceof Boolean ) {
                main = ((Boolean)mainProperty).booleanValue();
            }

            if ( dataObjects != null && !dataObjects.isEmpty() ) { // Open all returned projects in the GUI
                for( Iterator it = dataObjects.iterator(); it.hasNext(); ) {
                    DataObject prjDirDo = (DataObject)it.next();
                    FileObject prjDirFo = prjDirDo.getPrimaryFile();
                    Project p = ProjectManager.getDefault().findProject( prjDirFo );
                    if ( p != null ) {
                        OpenProjectList.getDefault().open( p, true );
                        if ( main ) {
                            OpenProjectList.getDefault().setMainProject( p );
                        }
                    }
                }
            }
        }
        catch ( IOException e ) {
            ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, e );
        }

    }
    
}