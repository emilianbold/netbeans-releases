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
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/** An action sensitive to selected node. Used for 1-off actions
 */
public class FileCommandAction extends ProjectSensitiveAction {

    private String command;
    private String namePattern;
    private Lookup lookup;
    
    
    public FileCommandAction( String command, String namePattern, String iconResource, Lookup lookup ) {
        this( command, namePattern, new ImageIcon( Utilities.loadImage( iconResource ) ), lookup );
    }
    
    public FileCommandAction( String command, String namePattern, Icon icon, Lookup lookup ) {
        super( icon, lookup );
        this.command = command;
        this.namePattern = namePattern;
        refresh( getLookup()  );        
    }

    protected void refresh( Lookup context ) {
        
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, command );

        if ( projects.length != 1 || !ActionsUtil.commandSupported(projects[0], command, context) ) {
            setEnabled( false ); // Zero or more than one projects found or commande not supported
            setDisplayName( ActionsUtil.formatName( namePattern, 0, "" ) );
        }
        else {
            FileObject[] files = ActionsUtil.getFilesFromLookup( context, command, projects[0] );
            setEnabled( true );
            setDisplayName( ActionsUtil.formatName( namePattern, files.length, files[0].getNameExt() ) );
        }
    }
    
    protected void actionPerformed( Lookup context ) {

        Project[] projects = ActionsUtil.getProjectsFromLookup( context, command );

        if ( projects.length == 1 ) {            
            ActionProvider ap = (ActionProvider)projects[0].getLookup().lookup( ActionProvider.class );
            ap.invokeAction( command, context );
        }
        
    }

    public Action createContextAwareInstance( Lookup actionContext ) {
        return new FileCommandAction( command, namePattern, (Icon)getValue( SMALL_ICON ), actionContext );
    }
   
        
}