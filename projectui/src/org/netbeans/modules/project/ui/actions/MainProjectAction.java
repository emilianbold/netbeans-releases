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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.ui.NoMainProjectWarning;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Actions;
import org.openide.awt.MouseUtils;
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
        // XXX #47160: listen to changes in supported commands on current project, when that becomes possible
    }

    public void actionPerformed( ActionEvent e ) {
    
        Project p = OpenProjectList.getDefault().getMainProject();
        
        // if no main project than show warning and allow choose a main project
        if (p == null) {
            // show warning, if cancel then return
            if (showNoMainProjectWarning (OpenProjectList.getDefault().getOpenProjects (), name)) {
                return ;
            }
            p = OpenProjectList.getDefault().getMainProject();
        }

        if ( command != null ) {
            ActionProvider ap = (ActionProvider)p.getLookup().lookup( ActionProvider.class );
            if (Arrays.asList(ap.getSupportedActions()).contains(command)) {
                ap.invokeAction(command, Lookup.EMPTY);
            } else {
                // #47160: was a supported command (e.g. on a freeform project) but was then removed.
                Toolkit.getDefaultToolkit().beep();
                refreshView();
            }
        }
        else {
            performer.perform( p );
        }
    }
        
       
    // Private methods ---------------------------------------------------------
    
    // Implementation of PropertyChangeListener --------------------------------
    
    public void propertyChange( PropertyChangeEvent evt ) {
        
        if ( evt.getPropertyName() == OpenProjectList.PROPERTY_MAIN_PROJECT || 
             evt.getPropertyName() == OpenProjectList.PROPERTY_OPEN_PROJECTS ) {
            refreshView ();
        }
               
    }   
    
    private void refreshView() {
        
        Project p = OpenProjectList.getDefault().getMainProject();
        boolean noOpenProject = OpenProjectList.getDefault ().getOpenProjects ().length == 0;
        
        if ( command == null ) {
            setEnabled( performer.enable( p ) );
        }
        else {
            if ( p == null ) {
                setEnabled( !noOpenProject );
            }
            else if ( ActionsUtil.commandSupported ( p, command, Lookup.EMPTY ) ) {
                setEnabled( !noOpenProject );
            }
            else {
                setEnabled( false );
            }
        }        
    }
    
   private boolean showNoMainProjectWarning (Project[] projects, String action) {
        boolean canceled;
        final JButton okButton = new JButton (NbBundle.getMessage (NoMainProjectWarning.class, "LBL_NoMainClassWarning_ChooseMainProject_OK")); // NOI18N        
        okButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (NoMainProjectWarning.class, "AD_NoMainClassWarning_ChooseMainProject_OK"));
        
        // no main project set => warning
        final NoMainProjectWarning panel = new NoMainProjectWarning (projects);

        Object[] options = new Object[] {
            okButton,
            DialogDescriptor.CANCEL_OPTION
        };
        
        panel.addChangeListener (new ChangeListener () {
           public void stateChanged (ChangeEvent e) {
               if (e.getSource () instanceof MouseEvent && MouseUtils.isDoubleClick (((MouseEvent)e.getSource ()))) {
                   // click button and the finish dialog with selected class
                   if (panel.getSelectedProject () != null) {
                       okButton.doClick ();
                   }
               } else {
                   okButton.setEnabled (panel.getSelectedProject () != null);
               }
           }
        });
        
        okButton.setEnabled (panel.getSelectedProject () != null);
        
        DialogDescriptor desc = new DialogDescriptor (panel,
                action == null ?
                    NbBundle.getMessage(NoMainProjectWarning.class, "CTL_NoMainProjectWarning_Title") :
                    Actions.cutAmpersand(action),
            true, options, options[0], DialogDescriptor.DEFAULT_ALIGN, null, null);
        desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
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