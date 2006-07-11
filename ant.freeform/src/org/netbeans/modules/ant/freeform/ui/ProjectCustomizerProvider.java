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
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.JButton;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.ant.freeform.FreeformProject;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hrebejk, David Konecny
 */
public class ProjectCustomizerProvider implements CustomizerProvider {
    
    private final FreeformProject project;
    
    // Option indexes
    private static final int OPTION_OK = 0;
    private static final int OPTION_CANCEL = OPTION_OK + 1;
    
    // Option command names
    private static final String COMMAND_OK = "OK";          // NOI18N
    private static final String COMMAND_CANCEL = "CANCEL";  // NOI18N
    
    private DialogDescriptor dialogDescriptor;
    private Map customizerPerProject = new WeakHashMap (); // Is weak needed here?
    
    public ProjectCustomizerProvider(FreeformProject project) {
        this.project = project;
    }
            
    public void showCustomizer() {
        
        if (customizerPerProject.containsKey (project)) {
            Dialog dlg = (Dialog)customizerPerProject.get (project);
            
            // check if the project is being customized
            if (dlg.isShowing ()) {
                // make it showed
                dlg.setVisible(true);
                return ;
            }
        }

        // Create options
        JButton options[] = new JButton[] { 
            new JButton( NbBundle.getMessage( ProjectCustomizerProvider.class, "LBL_Customizer_Ok_Option") ), // NOI18N
            new JButton( NbBundle.getMessage( ProjectCustomizerProvider.class, "LBL_Customizer_Cancel_Option" ) ) , // NOI18N
        };

        // Set commands
        options[ OPTION_OK ].setActionCommand( COMMAND_OK );
        options[ OPTION_CANCEL ].setActionCommand( COMMAND_CANCEL );

        ProjectCustomizer pc = new ProjectCustomizer(project);
        // RegisterListener
        ActionListener optionsListener = new OptionListener( project, pc);
        options[ OPTION_OK ].addActionListener( optionsListener );
        options[ OPTION_OK ].getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage (ProjectCustomizerProvider.class, "ACSD_Customizer_Ok_Option")); // NOI18N
        options[ OPTION_CANCEL ].addActionListener( optionsListener );
        options[ OPTION_CANCEL ].getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage (ProjectCustomizerProvider.class, "ACSD_Customizer_Cancel_Option")); // NOI18N

        dialogDescriptor = new DialogDescriptor( 
            pc, // innerPane
            MessageFormat.format(                 // displayName
                NbBundle.getMessage( ProjectCustomizerProvider.class, "LBL_Customizer_Title" ), // NOI18N 
                new Object[] { ProjectUtils.getInformation(project).getDisplayName() } ),    
            false,                                  // modal
            options,                                // options
            options[OPTION_OK],                     // initial value
            DialogDescriptor.BOTTOM_ALIGN,          // options align
            null,                                   // helpCtx
            null );                                 // listener 

        pc.setDialogDescriptor( dialogDescriptor );        
        dialogDescriptor.setClosingOptions( new Object[] { options[ OPTION_OK ], options[ OPTION_CANCEL ] } );

        Dialog dialog = DialogDisplayer.getDefault().createDialog( dialogDescriptor );

        customizerPerProject.put (project, dialog);

        dialog.setVisible(true);
        
    }    
    

    
    /** Listens to the actions on the Customizer's option buttons */
    private static class OptionListener implements ActionListener {
    
        private Project project;
        private ProjectCustomizer projectCustomizer;
        
        private OptionListener(Project project, ProjectCustomizer projectCustomizer) {
            this.project = project;
            this.projectCustomizer = projectCustomizer;
        }
        
        public void actionPerformed( ActionEvent e ) {
            String command = e.getActionCommand();
            
            if ( COMMAND_OK.equals( command ) ) {
                projectCustomizer.save();
                
                try {
                    ProjectManager.getDefault().saveProject(project);
                } catch ( IOException ex ) {
                    ErrorManager.getDefault().notify( ex );
                }
            }
            
        }        
        
    }
                            
}
