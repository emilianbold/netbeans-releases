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

package org.netbeans.modules.ant.freeform.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.ant.freeform.FreeformProject;
import org.netbeans.modules.ant.freeform.spi.ProjectAccessor;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Petr Hrebejk, David Konecny
 */
public class ProjectCustomizerProvider implements CustomizerProvider {
    
    private final FreeformProject project;
    
    public static final String CUSTOMIZER_FOLDER_PATH = "Projects/org-netbeans-modules-ant-freeform/Customizer"; //NO18N
    
    private static Map<Project,Dialog> project2Dialog = new HashMap<Project,Dialog>(); 
    
    public ProjectCustomizerProvider(FreeformProject project) {
        this.project = project;
    }
            
    public void showCustomizer() {
        Dialog dialog = project2Dialog.get (project);
        if ( dialog != null ) {            
            dialog.setVisible(true);
            return;
        }
        else {
            InstanceContent ic = new InstanceContent();
            Lookup context = new AbstractLookup(ic);
            ic.add(project);
            ic.add(project.getLookup().lookup(ProjectAccessor.class));
            ic.add(project.getLookup().lookup(AuxiliaryConfiguration.class));
            //TODO replace with generic apis..
            ic.add(ic);
            
            OptionListener listener = new OptionListener( project );
            dialog = ProjectCustomizer.createCustomizerDialog(CUSTOMIZER_FOLDER_PATH, context, null, listener, null );
            dialog.addWindowListener( listener );
            dialog.setTitle( MessageFormat.format(                 
                    NbBundle.getMessage( ProjectCustomizerProvider.class, "LBL_Customizer_Title" ), // NOI18N 
                    new Object[] { ProjectUtils.getInformation(project).getDisplayName() } ) );

            project2Dialog.put(project, dialog);
            dialog.setVisible(true);
        }
    }    
    

    /** Listens to the actions on the Customizer's option buttons */
    private class OptionListener extends WindowAdapter implements ActionListener {
    
        private Project project;
        
        OptionListener( Project project) {
            this.project = project;
        }
        
        // Listening to OK button ----------------------------------------------
        
        public void actionPerformed( ActionEvent e ) {
//#95952 some users experience this assertion on a fairly random set of changes in 
// the customizer, that leads me to assume that a project can be already marked
// as modified before the project customizer is shown. 
//            assert !ProjectManager.getDefault().isModified(project) : 
//                "Some of the customizer panels has written the changed data before OK Button was pressed. Please file it as bug."; //NOI18N
            
            // Close & dispose the the dialog
            Dialog dialog = project2Dialog.get( project );
            if ( dialog != null ) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }        
        
        // Listening to window events ------------------------------------------
                
        public void windowClosed( WindowEvent e) {
            project2Dialog.remove( project );
        }    
        
        public void windowClosing (WindowEvent e) {
            //Dispose the dialog otherwsie the {@link WindowAdapter#windowClosed}
            //may not be called
            Dialog dialog = project2Dialog.get( project );
            if ( dialog != null ) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
    }
                            
}
