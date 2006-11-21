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
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
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
            CustomizerProvider cp = projects[0].getLookup().lookup( CustomizerProvider.class );
            if ( cp != null ) {
                if (!DataObject.getRegistry().getModifiedSet().isEmpty()) {
                    // #50992: danger! Project properties dialog may try to write to the same config files.
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                            NbBundle.getMessage(CustomizeProject.class, "CustomizeProject.save_modified_files"),
                            NotifyDescriptor.WARNING_MESSAGE));
                    return;
                }
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
