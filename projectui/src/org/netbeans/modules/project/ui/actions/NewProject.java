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
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.project.ui.NewProjectWizard;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.project.ui.ProjectTab;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class NewProject extends BasicAction {
        
    private static final Icon ICON = new ImageIcon( Utilities.loadImage( "org/netbeans/modules/project/ui/resources/newProject.gif" ) ); //NOI18N    
    private static final String NAME = NbBundle.getMessage( NewProject.class, "LBL_NewProjectAction_Name" ); // NOI18N
    
    
    private static NewProjectWizard wizard;

    public NewProject() {
        super( NAME, ICON );
    }

    public void actionPerformed( ActionEvent evt ) {

        if ( wizard == null ) {
            FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource( "Templates/Project" ); //NOI18N                
            wizard = new NewProjectWizard(fo);
        }

        try {
            Set newObjects = wizard.instantiate ();
            Object mainProperty = wizard.getProperty( /* XXX Define somewhere */ "setAsMain" ); // NOI18N
            boolean setFirstMain = true;
            if ( mainProperty instanceof Boolean ) {
                setFirstMain = ((Boolean)mainProperty).booleanValue();
            }

            if ( newObjects != null && !newObjects.isEmpty() ) { // Open all returned projects in the GUI
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
                            // call the preferred action on main class
                            Mutex.EVENT.writeAccess (new Runnable () {
                                public void run () {
                                    final Node node = newDo.getNodeDelegate ();
                                    Action a = node.getPreferredAction();
                                    if (a instanceof ContextAwareAction) {
                                        a = ((ContextAwareAction)a).createContextAwareInstance(node.getLookup ());
                                    }
                                    if (a != null) {
                                        a.actionPerformed(new ActionEvent(node, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                                    }

                                    // next action -> expand && select main class in package view
                                    final ProjectTab pt  = ProjectTab.findDefault (ProjectTab.ID_LOGICAL);
                                    // invoke later, Mutex.EVENT.writeAccess isn't suffice to 
                                    // select && expand if the focus is outside ProjectTab
                                    SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            pt.selectNode (newDo.getPrimaryFile ());        
                                        }
                                    });
                                }
                            });
                        }
                    } else {
                        assert false : obj;
                    }
                }
            }
        }
        catch ( IOException e ) {
            ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, e );
        }
    }
    
}