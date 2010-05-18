/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.mobility.project.ui;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.mobility.project.ui.customizer.J2MECustomizer;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
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
                
                // close the customizer
                if (provider.getDialog()!=null)
                {
                    provider.getDialog().dispose();
                    
                }
            }
        }
    }
}
