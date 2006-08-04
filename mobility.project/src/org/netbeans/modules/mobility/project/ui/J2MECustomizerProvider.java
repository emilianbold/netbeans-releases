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

package org.netbeans.modules.mobility.project.ui;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JButton;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.mobility.project.ui.customizer.J2MECustomizer;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Customization of J2ME project
 *
 * @author Petr Hrebejk, Adam Sotona
 */
public class J2MECustomizerProvider implements CustomizerProvider
{
    
    private final Project project;
    private final AntProjectHelper antProjectHelper;
    private final ReferenceHelper refHelper;
    private final ProjectConfigurationsHelper configHelper;
    
    Dialog dialog=null;
    
    // Option indexes
    private static final int OPTION_OK = 0;
    private static final int OPTION_CANCEL = OPTION_OK + 1;
    
    // Option command names
    private static final String COMMAND_OK = "OK";          // NOI18N
    private static final String COMMAND_CANCEL = "CANCEL";  // NOI18N
    
    private DialogDescriptor dialogDescriptor;
    
    public J2MECustomizerProvider(Project project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper, ProjectConfigurationsHelper configHelper)
    {
        this.project = project;
        this.antProjectHelper = antProjectHelper;
        this.refHelper = refHelper;
        this.configHelper = configHelper;
    }
    
    public void showCustomizer()
    {
        showCustomizer(false);
    }
    
    public void showCustomizer(final boolean addConfig)
    {
        // Create options
        final JButton options[] = new JButton[] {
            new JButton( NbBundle.getMessage( J2MECustomizerProvider.class, "LBL_Customizer_Ok_Option") ), // NOI18N
            new JButton( NbBundle.getMessage( J2MECustomizerProvider.class, "LBL_Customizer_Cancel_Option" ) ) , // NOI18N
        };
        
        // Set commands
        options[ OPTION_OK ].setActionCommand( COMMAND_OK );
        options[ OPTION_CANCEL ].setActionCommand( COMMAND_CANCEL );
        
        // RegisterListener
        final J2MEProjectProperties j2meProperties = new J2MEProjectProperties( project, antProjectHelper, refHelper, configHelper );
        final ActionListener optionsListener = new OptionListener( this, project, j2meProperties );
        options[ OPTION_OK ].addActionListener( optionsListener );
        options[ OPTION_CANCEL ].addActionListener( optionsListener );
        final J2MECustomizer customizer = addConfig ? new J2MECustomizer( j2meProperties, J2MECustomizer.ADD_CONFIG_DIALOG) : new J2MECustomizer( j2meProperties );
        dialogDescriptor = new DialogDescriptor(
                customizer, // innerPane
                ProjectUtils.getInformation(project).getDisplayName(),               // displayName
                true,                                   // modal
                options,                                // options
                options[OPTION_OK],                     // initial value
                DialogDescriptor.BOTTOM_ALIGN,          // options align
                new HelpCtx(J2MECustomizerProvider.class), // helpCtx
                null );                                 // listener
        
        dialogDescriptor.setClosingOptions( new Object[] { options[ OPTION_CANCEL ] } );
        customizer.setDialogDescriptor(dialogDescriptor);
        
        dialog = DialogDisplayer.getDefault().createDialog( dialogDescriptor );
        dialog.setVisible(true);
    }
    
    
    
    Dialog getDialog()
    {
        return this.dialog;
    }
    
    /** Listens to the actions on the Customizer's option buttons */
    private static class OptionListener implements ActionListener
    {
        
        final private Project project;
        final private J2MEProjectProperties j2meProperties;
        final private J2MECustomizerProvider provider;
        
        OptionListener( J2MECustomizerProvider provider, Project project, J2MEProjectProperties j2meProperties )
        {
            this.project = project;
            this.j2meProperties = j2meProperties;
            this.provider=provider;
        }
        
        public void actionPerformed( final ActionEvent e )
        {
            final String command = e.getActionCommand();
            
            if ( COMMAND_OK.equals( command ) )
            {
                // Store the properties
                j2meProperties.store();
                
                // XXX Maybe move into J2MEProjectProperties
                // And save the project
                try
                {
                    ProjectManager.getDefault().saveProject(project);
                }
                catch ( IOException ex )
                {
                    ErrorManager.getDefault().notify( ex );
                }
                
                // close the customizer
                if (provider.getDialog()!=null)
                {
                    provider.getDialog().dispose();
                    
                }
            }
        }
    }
}
