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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.ui.NewFileWizard;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/** Action for invoking the project sensitive NewFile Wizard
 */
public class NewFile extends ProjectAction implements PropertyChangeListener {

    private static final Icon ICON = new ImageIcon( Utilities.loadImage( "org/netbeans/modules/project/ui/resources/newFile.gif" ) ); //NOI18N        
    private static final String name = NbBundle.getMessage(NewFile.class, "LBL_NewFileAction_Name"); // NI18N
    
    public NewFile() {
        this( null );
    }
    
    public NewFile( Lookup context ) {
        super( (String)null, name, ICON, context ); //NOI18N        
        OpenProjectList.getDefault().addPropertyChangeListener( this );
        refresh( getLookup() );
    }

    protected void refresh( Lookup context ) {
        setEnabled( OpenProjectList.getDefault().getOpenProjects().length > 0 );
        setDisplayName( name );
    }

    //private NewFileWizard wizardIterator;  

    protected void actionPerformed( Lookup context ) {

        NewFileWizard wd = new NewFileWizard( preselectedProject( context ) /* , null */ );

        DataFolder preselectedFolder = preselectedFolder( context );
        if ( preselectedFolder != null ) {
            wd.setTargetFolder( preselectedFolder );
        }

        try { 
            Set resultSet = wd.instantiate ();
            
            if (resultSet == null) {
                // no new object, no work
                return ;
            }
            
            Iterator it = resultSet.iterator ();
            
            while (it.hasNext ()) {
                Object obj = it.next ();
                assert !(obj instanceof FileObject) : obj;
                try {
                    DataObject newDO = DataObject.find ((FileObject)obj);
                    if (newDO != null) {
                        // Same what template wizard does - not very nice
                        // run default action (hopefully should be here)
                        final Node node = newDO.getNodeDelegate ();
                        Action a = node.getPreferredAction();
                        if (a instanceof ContextAwareAction) {
                            a = ((ContextAwareAction)a).createContextAwareInstance(node.getLookup ());
                        }
                        if (a != null) {
                            a.actionPerformed(new ActionEvent(node, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                        }
                    }
                } catch (DataObjectNotFoundException x) {
                    // XXX
                    assert false : obj;
                }
            }
        }
        catch ( IOException e ) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, e);
        }

    }
    
    public Action createContextAwareInstance( Lookup actionContext ) {
        return new NewFile( actionContext );
    }

    private Project preselectedProject( Lookup context ) {
        Project preselectedProject = null;

        // if ( activatedNodes != null && activatedNodes.length != 0 ) {

        Project[] projects = ActionsUtil.getProjectsFromLookup( context, null );
        if ( projects.length > 0 ) {
            preselectedProject = projects[0];
        }

        
        if ( preselectedProject == null ) {
            // No project context => use main project
            preselectedProject = OpenProjectList.getDefault().getMainProject();
            if ( preselectedProject == null ) {
                // No main project => use the first one
                preselectedProject = OpenProjectList.getDefault().getOpenProjects()[0];
            }
        }

        if ( preselectedProject == null ) {
            assert false : "Action should be disabled"; // NOI18N
        }

        return preselectedProject;    
    }

    private DataFolder preselectedFolder( Lookup context ) {
        
        DataFolder preselectedFolder = null;
        
        // Try to find selected folder
        preselectedFolder = (DataFolder)context.lookup( DataFolder.class );
        if ( preselectedFolder == null ) {
            // No folder selectd try with DataObject
            DataObject dobj = (DataObject)context.lookup( DataObject.class );
            if ( dobj != null) {
                // DataObject found => we'll use the parent folder
                preselectedFolder = dobj.getFolder();
            }
        }
        
        return preselectedFolder;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        refresh( Lookup.EMPTY );
    }
    
}
    
    
    