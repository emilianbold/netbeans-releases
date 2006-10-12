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

package org.netbeans.modules.j2ee.archive.customizer;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import org.netbeans.modules.j2ee.archive.project.ArchiveProject;
import org.netbeans.modules.j2ee.archive.project.ArchiveProjectProperties;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;

import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.CustomizerProvider;


/** Customization of J2EE/Java EE archive project
 *
 * @author vince kraemer
 */
public class ProvidesCustomizer implements CustomizerProvider {
    
    private final Project project;
    private final AntProjectHelper antProjectHelper;
    
    // Option indexes
    private static final int OPTION_OK = 0;
    private static final int OPTION_CANCEL = OPTION_OK + 1;
    
    // Option command names
    private static final String COMMAND_OK = "OK";          // NOI18N
    private static final String COMMAND_CANCEL = "CANCEL";  // NOI18N
    
    public ProvidesCustomizer(Project project, AntProjectHelper antProjectHelper) {
        this.project = project;
        this.antProjectHelper = antProjectHelper;
    }
    
    public void showCustomizer() {
        Dialog dialog = createDialog();
        dialog.setVisible(true);
    }
    
    Dialog createDialog() {
        // Create options
        JButton options[] = new JButton[] {
            new JButton( NbBundle.getMessage( ProvidesCustomizer.class, "LBL_Customizer_Ok_Option") ), // NOI18N
            new JButton( NbBundle.getMessage( ProvidesCustomizer.class, "LBL_Customizer_Cancel_Option" ) ) , // NOI18N
        };
        
        // Set commands
        options[ OPTION_OK ].setActionCommand( COMMAND_OK );
        options[ OPTION_CANCEL ].setActionCommand( COMMAND_CANCEL );
        
        // RegisterListener
        ArchiveProjectProperties apProperties = new ArchiveProjectProperties( (ArchiveProject)project, antProjectHelper );
        ActionListener optionsListener = new OptionListener( project, apProperties );
        options[ OPTION_OK ].addActionListener( optionsListener );
        options[ OPTION_CANCEL ].addActionListener( optionsListener );
        
        
        ArchiveProjectCustomizer innerPane = new ArchiveProjectCustomizer(apProperties); // , pwm); // , preselectedNodeName);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(
                innerPane, // new EarCustomizer(apProperties, pwm), // innerPane
                NbBundle.getMessage( ProvidesCustomizer.class, "LBL_Customizer_Title" , // displayName
                ProjectUtils.getInformation(project).getDisplayName() ),
                false,                                  // modal
                options,                                // options
                options[OPTION_OK],                     // initial value
                DialogDescriptor.BOTTOM_ALIGN,          // options align
                null,                                   // helpCtx
                null );                                 // listener
        
        innerPane.setDialogDescriptor(dialogDescriptor);
        dialogDescriptor.setClosingOptions( new Object[] { options[ OPTION_OK ], options[ OPTION_CANCEL ] } );
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog( dialogDescriptor );
        return dialog; // dialog.show();
    }
    
    /** Listens to the actions on the Customizer's option buttons */
    private static class OptionListener implements ActionListener {
        
        private Project project;
        private ArchiveProjectProperties apProperties;
        
        OptionListener( Project project, ArchiveProjectProperties apProperties ) {
            this.project = project;
            this.apProperties = apProperties;
        }
        
        public void actionPerformed( ActionEvent e ) {
            String command = e.getActionCommand();
            
            if (COMMAND_OK.equals(command)) {
                // Store the properties
                apProperties.save();
                
                // XXX Maybe move into WebProjectProperties
                // And save the project
                try {
                    ProjectManager.getDefault().saveProject(project);
                } catch ( IOException ex ) {
                    ErrorManager.getDefault().notify( ex );
                }
            }
            
        }
        
    }
    
}
