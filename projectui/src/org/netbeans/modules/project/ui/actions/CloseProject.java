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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.project.ui.ProjectUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

/** Action for removing project from the open projects tab
 */
public class CloseProject extends ProjectAction implements PropertyChangeListener, Presenter.Popup {
    
    private static final String namePattern = NbBundle.getMessage( CloseProject.class, "LBL_CloseProjectAction_Name" ); // NOI18N
    private static final String namePatternPopup = NbBundle.getMessage( CloseProject.class, "LBL_CloseProjectAction_Popup_Name" ); // NOI18N
    
    private String popupName;
    
    private PropertyChangeListener wpcl;
   
    /** Creates a new instance of BrowserAction */
    public CloseProject() {
        this( null );        
    }
    
    public CloseProject( Lookup context ) {
        super( (String)null, namePattern, null, context );        
        wpcl = WeakListeners.propertyChange( this, OpenProjectList.getDefault() );
        OpenProjectList.getDefault().addPropertyChangeListener( wpcl );
        refresh( getLookup() );
    }
        
    protected void actionPerformed( Lookup context ) {
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, null );        
        // show all modified documents, if an user cancel it then no project is closed
        if (ProjectUtilities.closeAllDocuments (projects)) {
            for( int i = 0; i < projects.length; i++ ) {
                OpenProjectList.getDefault().close( projects[i] );
            }
        }
    }
    
    public void refresh( Lookup context ) {        
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, null );
        // XXX make it work better for mutliple open projects
        if ( projects.length == 0 || !OpenProjectList.getDefault().isOpen( projects[0] ) ) {
            setEnabled( false );
            setDisplayName( ActionsUtil.formatProjectSensitiveName( namePattern, new Project[0] ) );
            popupName = ActionsUtil.formatProjectSensitiveName( namePatternPopup, new Project[0] );
        }
        else {
            setEnabled( true );
            setDisplayName( ActionsUtil.formatProjectSensitiveName( namePattern, projects ) );
            popupName = ActionsUtil.formatProjectSensitiveName( namePatternPopup, projects );
        }        
    }
    
    public Action createContextAwareInstance( Lookup actionContext ) {
        return new CloseProject( actionContext );
    }
    
    public void propertyChange( PropertyChangeEvent evt ) {
        refresh( getLookup() );
    }
    
    // Implementation of Presenter.Popup ---------------------------------------
    
    public JMenuItem getPopupPresenter() {
        JMenuItem popupPresenter = new JMenuItem( this );

        popupPresenter.setIcon( null );
        popupPresenter.setText( popupName );
        
        return popupPresenter;
    }
    
}
