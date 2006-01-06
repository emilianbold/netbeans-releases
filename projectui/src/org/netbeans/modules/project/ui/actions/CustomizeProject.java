/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui.actions;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/** Action for invoking project customizer
 */
public class CustomizeProject extends ProjectAction implements Presenter.Popup {
    
    private static final String namePattern = NbBundle.getMessage( CustomizeProject.class, "LBL_CustomizeProjectAction_Name" ); // NOI18N
    private static final String namePatternPopup = NbBundle.getMessage( CustomizeProject.class, "LBL_CustomizeProjectAction_Popup_Name" ); // NOI18N
        
    public CustomizeProject() {
        this( null );
    }
    
    public CustomizeProject( Lookup context ) {
        super( (String)null, namePattern, null, context );
        refresh( getLookup() );
    }
            
    
   
    protected void refresh( Lookup context ) {
     
        super.refresh( context );
        
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, null );
                            
        if ( projects.length != 1 || projects[0].getLookup().lookup( CustomizerProvider.class ) == null ) {
            setEnabled( false );
            // setDisplayName( ActionsUtil.formatProjectSensitiveName( namePattern, new Project[0] ) );
        }
        else { 
            setEnabled( true );
            // setDisplayName( ActionsUtil.formatProjectSensitiveName( namePattern, projects ) );
        }
        
        
    }   
    
    public void actionPerformed( Lookup context ) {
    
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, null );
        
        if ( projects.length == 1 ) {
            CustomizerProvider cp = (CustomizerProvider)projects[0].getLookup().lookup( CustomizerProvider.class );
            if ( cp != null ) {
                cp.showCustomizer();
            }
        }
        
    }
    
    public Action createContextAwareInstance( Lookup actionContext ) {
        return new CustomizeProject( actionContext );
    }
    
    
    // Implementation of Presenter.Popup ---------------------------------------
    
    public JMenuItem getPopupPresenter() {
        JMenuItem popupPresenter = new JMenuItem( this );
        popupPresenter.setText( namePatternPopup );
        popupPresenter.setIcon( null );

        return popupPresenter;
    }
    
}
