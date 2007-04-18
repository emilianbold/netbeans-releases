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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.projects.jbi.ui;

import org.netbeans.modules.compapp.projects.jbi.CasaHelper;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiJarCustomizer;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;

import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.CustomizerProvider;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;

import org.openide.util.NbBundle;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;

import java.text.MessageFormat;

import javax.swing.JButton;

/**
 * Customization of web project
 *
 * @author Petr Hrebejk
 */
public class JbiCustomizerProvider implements CustomizerProvider {
    // Option indexes
    private static final int OPTION_OK = 0;
    private static final int OPTION_CANCEL = OPTION_OK + 1;

    // Option command names
    private static final String COMMAND_OK = "OK"; // NOI18N
    private static final String COMMAND_CANCEL = "CANCEL"; // NOI18N
    private final Project project;
    private final AntProjectHelper antProjectHelper;
    private final ReferenceHelper refHelper;
    private JbiProjectProperties webProperties;

    /**
     * Creates a new JbiCustomizerProvider object.
     *
     * @param project DOCUMENT ME!
     * @param antProjectHelper DOCUMENT ME!
     * @param refHelper DOCUMENT ME!
     */
    public JbiCustomizerProvider(
        Project project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper
    ) {
        this.project = project;
        this.antProjectHelper = antProjectHelper;
        this.refHelper = refHelper;
    }

    /**
     * DOCUMENT ME!
     */
    public void showCustomizer() {
        // Create options
        JButton[] options = new JButton[] {
                new JButton(
                    NbBundle.getMessage(JbiCustomizerProvider.class, "LBL_Customizer_Ok_Option") // NOI18N
                ), // NOI18N
                new JButton(
                    NbBundle.getMessage(
                        JbiCustomizerProvider.class, "LBL_Customizer_Cancel_Option" // NOI18N
                    )
                ), // NOI18N
            };

        // Set commands
        options[OPTION_OK].setActionCommand(COMMAND_OK);
        options[OPTION_CANCEL].setActionCommand(COMMAND_CANCEL);

        // RegisterListener
        webProperties = new JbiProjectProperties(project, antProjectHelper, refHelper);

        ActionListener optionsListener = new OptionListener(project, webProperties);
        options[OPTION_OK].addActionListener(optionsListener);
        options[OPTION_CANCEL].addActionListener(optionsListener);
            
        CasaHelper.saveCasa(project);
        
        JbiJarCustomizer innerPane = new JbiJarCustomizer(webProperties); // , preselectedNodeName);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(
                innerPane,//new JbiJarCustomizer(webProperties), //pwm)
                MessageFormat.format( // displayName
                    NbBundle.getMessage(JbiCustomizerProvider.class, "LBL_Customizer_Title"), // NOI18N
                    new Object[] {ProjectUtils.getInformation(project).getDisplayName()}
                ), false, // modal
                options, // options
                options[OPTION_OK], // initial value
                DialogDescriptor.BOTTOM_ALIGN, // options align
                null, // helpCtx
                null
            ); // listener 

        innerPane.setDialogDescriptor(dialogDescriptor);
        dialogDescriptor.setClosingOptions(
            new Object[] {options[OPTION_OK], options[OPTION_CANCEL]}
        );

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setVisible(true);
    }

    /**
     * Listens to the actions on the Customizer's option buttons
     */
    private static class OptionListener implements ActionListener {
        private Project project;
        private JbiProjectProperties webProperties;

        /**
         * Creates a new OptionListener object.
         *
         * @param project DOCUMENT ME!
         * @param webProperties DOCUMENT ME!
         */
        OptionListener(Project project, JbiProjectProperties webProperties) {
            this.project = project;
            this.webProperties = webProperties;
        }

        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();

            if (COMMAND_OK.equals(command)) {
                // Store the properties
                webProperties.store();
//                webProperties.saveAssemblyInfo();

                // XXX Maybe move into WebProjectProperties
                // And save the project
                try {
                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }
    }
}
