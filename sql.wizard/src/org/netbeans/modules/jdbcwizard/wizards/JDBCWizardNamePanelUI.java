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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * 
 * Copyright 2009 Sun Microsystems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.netbeans.modules.jdbcwizard.wizards;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openide.util.NbBundle;

/**
 * Accepts input of unique name for new JDBC Collaboration instance.
 * 
 * @author
 */
public class JDBCWizardNamePanelUI extends JPanel {
    private JDBCWizardNamePanel wizardPanel;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    class NameFieldKeyAdapter extends KeyAdapter {
        /**
         * Overrides default implementation to notify listeners of new collab name value in
         * associated textfield.
         * 
         * @param e KeyEvent to be handled
         */
        @Override
        public void keyReleased(final KeyEvent e) {
            final String collaborationName = JDBCWizardNamePanelUI.this.textField.getText();

            if (collaborationName != null && collaborationName.trim().length() != 0) {
                wizardPanel.collabName = collaborationName.trim();
            } else {
                wizardPanel.collabName = null;
            }

            wizardPanel.fireChangeEvent();
        }
    }

    private JDBCCollaborationWizard owner;

    private JTextField textField;

    private String title;

    /**
     * No-arg constructor for this wizard descriptor.
     */
    private JDBCWizardNamePanelUI (JDBCWizardNamePanel wp) {
        wizardPanel = wp;
        this.setLayout(new BorderLayout());

        final JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new GridBagLayout());

        // Top filler panel to absorb 20% of any expansion up and down the page.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.2;
        outerPanel.add(new JPanel(), gbc);

        // Text field label.
        final JLabel header = new JLabel(NbBundle.getMessage(JDBCWizardNamePanelUI.class, "LBL_tblwizard_namefield"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets.right = 10;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        outerPanel.add(header, gbc);

        // Text field.
        this.textField = new JTextField();
        this.textField.addKeyListener(new NameFieldKeyAdapter());

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        outerPanel.add(this.textField, gbc);

        // Bottom filler panel to absorb 80% of any expansion up and down the page.
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.8;
        outerPanel.add(new JPanel(), gbc);

        this.add(outerPanel, BorderLayout.CENTER);
    }

    /**
     * Create the wizard panel descriptor, using the given panel title, content panel
     * 
     * @param myOwner JDBCWizard that owns this panel
     * @param panelTitle text to display as panel title
     */
    public JDBCWizardNamePanelUI (JDBCWizardNamePanel wp, final JDBCCollaborationWizard myOwner, final String panelTitle) {
        this (wp);

        this.title = panelTitle;
        this.setName(this.title);
        this.owner = myOwner;
    }

}
