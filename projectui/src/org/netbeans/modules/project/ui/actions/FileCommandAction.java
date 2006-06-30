/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui.actions;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.openide.awt.Actions;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/** An action sensitive to selected node. Used for 1-off actions
 */
public final class FileCommandAction extends ProjectAction {

    private String presenterName;
        
    public FileCommandAction( String command, String namePattern, String iconResource, Lookup lookup ) {
        this( command, namePattern, new ImageIcon( Utilities.loadImage( iconResource ) ), lookup );
    }
    
    public FileCommandAction( String command, String namePattern, Icon icon, Lookup lookup ) {
        super( command, namePattern, icon, lookup );
        assert namePattern != null : "Name patern must not be null";
        presenterName = ActionsUtil.formatName( getNamePattern(), 0, "" );
        setDisplayName( presenterName );
        putValue(SHORT_DESCRIPTION, Actions.cutAmpersand(presenterName));
    }
    
    protected void refresh( Lookup context ) {
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, getCommand() );

        if ( projects.length != 1 ) {
            setEnabled( false ); // Zero or more than one projects found or command not supported            
            presenterName = ActionsUtil.formatName( getNamePattern(), 0, "" );            
        }
        else {
            FileObject[] files = ActionsUtil.getFilesFromLookup( context, projects[0] );
            setEnabled( true );
            presenterName = ActionsUtil.formatName( getNamePattern(), files.length, files.length > 0 ? files[0].getNameExt() : "" ); // NOI18N
        }
        
        setLocalizedTextToMenuPresented(presenterName);
        
        putValue(SHORT_DESCRIPTION, Actions.cutAmpersand(presenterName));
    }
    
    protected void actionPerformed( Lookup context ) {
                
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, getCommand() );

        if ( projects.length == 1 ) {            
            ActionProvider ap = (ActionProvider)projects[0].getLookup().lookup( ActionProvider.class );
            ap.invokeAction( getCommand(), context );
        }
        
    }
    

    public Action createContextAwareInstance( Lookup actionContext ) {
        
        return new FileCommandAction( getCommand(), getNamePattern(), (Icon)getValue( SMALL_ICON ), actionContext );
    }

    
}