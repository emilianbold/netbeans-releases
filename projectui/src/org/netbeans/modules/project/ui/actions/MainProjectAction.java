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
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.util.Lookup;

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

        if ( p == null ) {
            return; // Action should be disabled
        }

        if ( command != null ) {
            ActionProvider ap = (ActionProvider)p.getLookup().lookup( ActionProvider.class );
            ap.invokeAction( command, Lookup.EMPTY );            
        }
        else {
            performer.perform( p );
        }
    }
        
       
    // Implementation of PropertyChangeListener --------------------------------
    
    public void propertyChange( PropertyChangeEvent evt ) {
        
        if ( evt.getPropertyName() == OpenProjectList.PROPERTY_MAIN_PROJECT ) {
            refreshView();            
        }
               
    }   
    
    // Private methods ---------------------------------------------------------
    
    private void refreshView() {
        
        Project p = OpenProjectList.getDefault().getMainProject();
        
        
        if ( command == null ) {
            setEnabled( performer.enable( p ) );
        }
        else {
            setEnabled( p != null );
        }        
    }
    
}