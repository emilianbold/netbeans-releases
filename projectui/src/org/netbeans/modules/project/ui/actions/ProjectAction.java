/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui.actions;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.awt.Mnemonics;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/** Action sensitive to current project
 * 
 * @author Pet Hrebejk 
 */
public class ProjectAction extends LookupSensitiveAction implements Presenter.Menu, ContextAwareAction {
    
    private String command;
    private ProjectActionPerformer performer;
    private String namePattern;
    private String presenterName;
    private JMenuItem menuPresenter;    
    
    /** 
     * Constructor for global actions. E.g. actions in main menu which 
     * listen to the global context.
     *
     */
    public ProjectAction(String command, String namePattern, Icon icon, Lookup lookup) {
        this( command, null, namePattern, icon, lookup );
    }
    
    public ProjectAction( ProjectActionPerformer performer, String namePattern, Icon icon, Lookup lookup) {
        this( null, performer, namePattern, icon, lookup );
    }
    
    private ProjectAction( String command, ProjectActionPerformer performer, String namePattern, Icon icon, Lookup lookup ) {
        super( icon, lookup, new Class[] { Project.class, DataObject.class } );
        this.command = command;
        if ( command != null ) {
            ActionsUtil.SHORCUTS_MANAGER.registerAction( command, this );
        }
        this.performer = performer;
        this.namePattern = namePattern;
        presenterName = ActionsUtil.formatName( getNamePattern(), 0, "" );
        setDisplayName( presenterName );
    }
    
    public void putValue( String key, Object value ) {
        super.putValue( key, value );
        
        if ( key == Action.ACCELERATOR_KEY ) {
            ActionsUtil.SHORCUTS_MANAGER.registerShortcut( command, value );
        }
        
    }
       
    protected void actionPerformed( Lookup context ) {
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, command );
        
        if ( projects.length == 1 ) {
            if ( command != null ) {
                ActionProvider ap = (ActionProvider)projects[0].getLookup().lookup( ActionProvider.class );        
                ap.invokeAction( command, Lookup.EMPTY );        
            }
            else if ( performer != null ) {
                performer.perform( projects[0] );
            }
        }
        
    }
    
    protected void refresh( Lookup context ) {
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, command );
        
        if ( command != null ) {
            setEnabled( projects.length == 1 );
            presenterName = ActionsUtil.formatProjectSensitiveName( namePattern, projects );
        }
        else if ( performer != null && projects.length == 1 ) {
            setEnabled( performer.enable( projects[0] ) );
            presenterName = ActionsUtil.formatProjectSensitiveName( namePattern, projects );
        }
        else {
            setEnabled( false );
            presenterName = ActionsUtil.formatProjectSensitiveName( namePattern, projects );
        }
        
        if ( menuPresenter != null ) {
            Mnemonics.setLocalizedText( menuPresenter, presenterName );
        }
                        
    }
    
    protected final String getCommand() {
        return command;
    }
    
    protected final String getNamePattern() {
        return namePattern;
    }
    
    
    // Implementation of Presenter.Menu ----------------------------------------
    
    public JMenuItem getMenuPresenter () {
        
        if ( menuPresenter == null ) {
            menuPresenter = new JMenuItem( this );

            Icon icon = null;
            // ignore icon if noIconInMenu flag is set
            if (!Boolean.TRUE.equals( getValue( "noIconInMenu" ) ) ) { 
                icon = (Icon)getValue( Action.SMALL_ICON );
            }
            menuPresenter.setIcon( icon );
            Mnemonics.setLocalizedText( menuPresenter, presenterName );
        }
        return menuPresenter;        
    }
    
    
    // Implementation of ContextAwareAction ------------------------------------
    
    public Action createContextAwareInstance( Lookup actionContext ) {
        return new ProjectAction( command, performer, namePattern, (Icon)getValue( SMALL_ICON ), actionContext );
    }

    
}