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

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.ui.NoMainProjectWarning;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/** Invokes command on the main project.
 * 
 * @author Pet Hrebejk 
 */
public class MainProjectAction extends BasicAction implements PropertyChangeListener {
    
    private String command;
    private ProjectActionPerformer performer;
    private String name;
        
    public MainProjectAction(ProjectActionPerformer performer, String name, Icon icon) {
        this( null, performer, name, icon );
    }
    
    public MainProjectAction(String command, String name, Icon icon) {
        this( command, null, name, icon );
    }
    
    public MainProjectAction(String command, ProjectActionPerformer performer, String name, Icon icon) {
            
        this.command = command;
        this.performer = performer;
        this.name = name;
        
        setDisplayName( name );
        if ( icon != null ) {
            setSmallIcon( icon );
        }
        
        refreshView();                
        // Start listening on open projects list to correctly enable the action
        OpenProjectList.getDefault().addPropertyChangeListener( this );
    }

    public void actionPerformed( ActionEvent e ) {
    
        Project p = OpenProjectList.getDefault().getMainProject();
        
        // if no main project than show warning and allow choose a main project
        if (p == null) {
            // show warning, if cancel then return
            if (showNoMainProjectWarning (OpenProjectList.getDefault().getOpenProjects ())) {
                return ;
            }
            p = OpenProjectList.getDefault().getMainProject();
        }

        if ( command != null ) {
            ActionProvider ap = (ActionProvider)p.getLookup().lookup( ActionProvider.class );
            ap.invokeAction( command, Lookup.EMPTY );            
        }
        else {
            performer.perform( p );
        }
    }
        
       
    // Private methods ---------------------------------------------------------
    
    // Implementation of PropertyChangeListener --------------------------------
    
    public void propertyChange( PropertyChangeEvent evt ) {
        
        if ( evt.getPropertyName() == OpenProjectList.PROPERTY_MAIN_PROJECT ) {
            refreshView ();
        }
               
    }   
    
    private void refreshView() {
        
        Project p = OpenProjectList.getDefault().getMainProject();
        
        if ( command == null ) {
            setEnabled( performer.enable( p ) );
        }
        else {
            if ( p == null ) {
                setEnabled( true );
            }
            else if ( ActionsUtil.commandSupported ( p, command, Lookup.EMPTY ) ) {
                setEnabled( true );
            }
            else {
                setEnabled( false );
            }
        }        
    }
    
   private boolean showNoMainProjectWarning (Project[] projects) {
        boolean canceled;
        
        // no main project set => warning
        NoMainProjectWarning panel = new NoMainProjectWarning (projects);

        Object[] options = new Object[] {
            NbBundle.getMessage (NoMainProjectWarning.class, "LBL_NoMainClassWarning_ChooseMainProject_OK"), // NOI18N
            DialogDescriptor.CANCEL_OPTION
        };
        DialogDescriptor desc = new DialogDescriptor (panel,
                NbBundle.getMessage (NoMainProjectWarning.class, "CTL_NoMainProjectWarning_Title"), // NOI18N
            true, options, options[0], DialogDescriptor.DEFAULT_ALIGN, null, null);
        Dialog dlg = DialogDisplayer.getDefault ().createDialog (desc);
        dlg.setVisible (true);
        if (desc.getValue() != options[0]) {
            canceled = true;
        } else {
            Project mainProject = panel.getSelectedProject ();
            OpenProjectList.getDefault ().setMainProject (mainProject);
            canceled = false;
        }
        dlg.dispose();            

        return canceled;
    }
        
}