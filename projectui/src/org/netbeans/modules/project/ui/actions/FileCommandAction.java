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

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/** An action sensitive to selected node. Used for 1-off actions
 */
public class FileCommandAction extends ProjectAction implements Presenter.Menu {

    private String command;
    private String presenterName;
    private JMenuItem menuPresenter;
        
    public FileCommandAction( String command, String namePattern, String iconResource, Lookup lookup ) {
        this( command, namePattern, new ImageIcon( Utilities.loadImage( iconResource ) ), lookup );
    }
    
    public FileCommandAction( String command, String namePattern, Icon icon, Lookup lookup ) {
        super( command, namePattern, icon, lookup );
        this.command = command;
        assert namePattern != null : "Name patern must not be null";
        presenterName = ActionsUtil.formatName( getNamePattern(), 0, "" );
        setDisplayName( presenterName );
    }
    
    protected void refresh( Lookup context ) {
                
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, command );

        if ( projects.length != 1 ) {
            setEnabled( false ); // Zero or more than one projects found or command not supported            
            presenterName = ActionsUtil.formatName( getNamePattern(), 0, "" );            
        }
        else {
            FileObject[] files = ActionsUtil.getFilesFromLookup( context, projects[0] );
            setEnabled( true );
            presenterName = ActionsUtil.formatName( getNamePattern(), files.length, files.length > 0 ? files[0].getNameExt() : "" ); // NOI18N
        }
        
        if ( menuPresenter != null ) {
            Mnemonics.setLocalizedText( menuPresenter, presenterName );
        }
    }
    
    protected void actionPerformed( Lookup context ) {
                
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, command );

        if ( projects.length == 1 ) {            
            ActionProvider ap = (ActionProvider)projects[0].getLookup().lookup( ActionProvider.class );
            ap.invokeAction( command, context );
        }
        
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
    

    public Action createContextAwareInstance( Lookup actionContext ) {
        
        return new FileCommandAction( command, getNamePattern(), (Icon)getValue( SMALL_ICON ), actionContext );
    }

    
}