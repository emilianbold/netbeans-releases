/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.makeproject.api;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;
import javax.swing.JButton;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.makeproject.MakeSources;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.customizer.MakeCustomizer;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/** Customization of Make project
 */
public class MakeCustomizerProvider implements CustomizerProvider {
    
    private final Project project;
    private final AntProjectHelper antProjectHelper;   
    private final PropertyEvaluator evaluator;
    private final ReferenceHelper refHelper;
    
    // Option indexes
    private static final int OPTION_OK = 0;
    private static final int OPTION_CANCEL = 1;
    private static final int OPTION_APPLY = 2;
    
    // Option command names
    private static final String COMMAND_OK = "OK";          // NOI18N
    private static final String COMMAND_CANCEL = "CANCEL";  // NOI18N
    private static final String COMMAND_APPLY = "APPLY";  // NOI18N
    
    private DialogDescriptor dialogDescriptor;
    private Map customizerPerProject = new WeakHashMap (); // Is is weak needed here?
    private ConfigurationDescriptorProvider projectDescriptorProvider;
    
    public MakeCustomizerProvider(Project project, AntProjectHelper antProjectHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper, ConfigurationDescriptorProvider projectDescriptorProvider) {
        this.project = project;
        this.antProjectHelper = antProjectHelper;
        this.evaluator = evaluator;
        this.refHelper = refHelper;
        this.projectDescriptorProvider = projectDescriptorProvider;
    }
            
    public void showCustomizer() {
        showCustomizer(null, null);
    }

    public void showCustomizer(Item item) {
        showCustomizer(null, item);
    }

    public void showCustomizer(String preselectedNodeName) {
        showCustomizer(preselectedNodeName, null);
    }
    
    public void showCustomizer(String preselectedNodeName, Item item) {
        
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
            new JButton( NbBundle.getMessage( MakeCustomizerProvider.class, "LBL_Customizer_Ok_Option") ), // NOI18N
            new JButton( NbBundle.getMessage( MakeCustomizerProvider.class, "LBL_Customizer_Cancel_Option" ) ) , // NOI18N
            new JButton( NbBundle.getMessage( MakeCustomizerProvider.class, "LBL_Customizer_Apply_Option" ) ) , // NOI18N
        };

        // Set commands
        options[ OPTION_OK ].setActionCommand( COMMAND_OK );
        options[ OPTION_OK ].getAccessibleContext ().setAccessibleDescription ( NbBundle.getMessage( MakeCustomizerProvider.class, "ACSD_Customizer_Ok_Option") ); // NOI18N
        options[ OPTION_CANCEL ].setActionCommand( COMMAND_CANCEL );
        options[ OPTION_CANCEL ].getAccessibleContext ().setAccessibleDescription ( NbBundle.getMessage( MakeCustomizerProvider.class, "ACSD_Customizer_Cancel_Option") ); // NOI18N
        options[ OPTION_APPLY ].setActionCommand( COMMAND_APPLY );
        options[ OPTION_APPLY ].getAccessibleContext ().setAccessibleDescription ( NbBundle.getMessage( MakeCustomizerProvider.class, "ACSD_Customizer_Apply_Option") ); // NOI18N

        //A11Y
        options[ OPTION_OK].getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(MakeCustomizerProvider.class,"AD_MakeCustomizerProviderOk")); // NOI18N
        options[ OPTION_CANCEL].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizerProvider.class,"AD_MakeCustomizerProviderCancel")); // NOI18N
        options[ OPTION_APPLY].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizerProvider.class,"AD_MakeCustomizerProviderApply")); // NOI18N

	// Mnemonics
        options[ OPTION_APPLY ].setMnemonic(NbBundle.getMessage(MakeCustomizerProvider.class, "MNE_Customizer_Apply_Option").charAt(0)); // NOI18N

        // RegisterListener
	ConfigurationDescriptor clonedProjectdescriptor = projectDescriptorProvider.getConfigurationDescriptor().cloneProjectDescriptor();
	Vector controls = new Vector();
	controls.add(options[OPTION_OK]);
        MakeCustomizer innerPane = new MakeCustomizer(project, preselectedNodeName, clonedProjectdescriptor, item, controls);
        ActionListener optionsListener = new OptionListener( project, projectDescriptorProvider.getConfigurationDescriptor(), clonedProjectdescriptor, antProjectHelper, innerPane);
        options[ OPTION_OK ].addActionListener( optionsListener );
        options[ OPTION_CANCEL ].addActionListener( optionsListener );
        options[ OPTION_APPLY ].addActionListener( optionsListener );

        dialogDescriptor = new DialogDescriptor( 
            innerPane, // innerPane
            MessageFormat.format(                 // displayName
                NbBundle.getMessage( MakeCustomizerProvider.class, "LBL_Customizer_Title" ), // NOI18N 
                new Object[] { ProjectUtils.getInformation(project).getDisplayName() } ),    
            true,                                  // modal
            options,                                // options
            options[OPTION_OK],                     // initial value
            DialogDescriptor.BOTTOM_ALIGN,          // options align
            null,                                   // helpCtx
            null );                                 // listener 
 
        dialogDescriptor.setClosingOptions( new Object[] { options[ OPTION_OK ], options[ OPTION_CANCEL ] } );

        Dialog dialog = DialogDisplayer.getDefault().createDialog( dialogDescriptor );

        customizerPerProject.put (project, dialog);

        dialog.setVisible(true);
        
    }    
    

    
    /** Listens to the actions on the Customizer's option buttons */
    private static class OptionListener implements ActionListener {
    
        private Project project;
	private ConfigurationDescriptor projectDescriptor;
	private ConfigurationDescriptor clonedProjectdescriptor;
	private AntProjectHelper antProjectHelper;
	private MakeCustomizer makeCustomizer;
        
        OptionListener( Project project, ConfigurationDescriptor projectDescriptor, ConfigurationDescriptor clonedProjectdescriptor, AntProjectHelper antProjectHelper, MakeCustomizer makeCustomizer) {
            this.project = project;
	    this.projectDescriptor = projectDescriptor;
	    this.clonedProjectdescriptor = clonedProjectdescriptor;
	    this.antProjectHelper = antProjectHelper;
	    this.makeCustomizer = makeCustomizer;
        }
        
        public void actionPerformed( ActionEvent e ) {
            String command = e.getActionCommand();
            
            if (command.equals(COMMAND_OK) || command.equals(COMMAND_APPLY)) {
		//projectDescriptor.copyFromProjectDescriptor(clonedProjectdescriptor);
		projectDescriptor.assign(clonedProjectdescriptor);
		projectDescriptor.setModified();
                ((MakeConfigurationDescriptor)projectDescriptor).checkForChangedItems();

		((MakeSources)ProjectUtils.getSources(project)).descriptorChanged();// FIXUP: should be moved into ProjectDescriptorHelper...
                
                // And save the project
                try {
                    ProjectManager.getDefault().saveProject(project);
                }
                catch ( IOException ex ) {
                    ErrorManager.getDefault().notify( ex );
                }
            }
            if (command.equals(COMMAND_APPLY)) {
		makeCustomizer.refresh();
	    }
        }        
    }
}
