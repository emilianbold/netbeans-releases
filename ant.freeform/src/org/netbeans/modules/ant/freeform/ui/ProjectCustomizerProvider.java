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
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
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
    private final AntProjectHelper antProjectHelper;   
    private final PropertyEvaluator evaluator;
    
    // Option indexes
    private static final int OPTION_OK = 0;
    private static final int OPTION_CANCEL = OPTION_OK + 1;
    
    // Option command names
    private static final String COMMAND_OK = "OK";          // NOI18N
    private static final String COMMAND_CANCEL = "CANCEL";  // NOI18N
    
    private DialogDescriptor dialogDescriptor;
    private Map customizerPerProject = new WeakHashMap (); // Is is weak needed here?
    
    public ProjectCustomizerProvider(FreeformProject project, AntProjectHelper antProjectHelper, PropertyEvaluator evaluator) {
        this.project = project;
        this.antProjectHelper = antProjectHelper;
        this.evaluator = evaluator;
    }
            
    public void showCustomizer() {
        
        if (customizerPerProject.containsKey (project)) {
            Dialog dlg = (Dialog)customizerPerProject.get (project);
            
            // check if the project is being customized
            if (dlg.isShowing ()) {
                // make it showed
                dlg.show();
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

        ProjectCustomizer pc = new ProjectCustomizer(project, antProjectHelper);
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

        dialog.show();
        
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
