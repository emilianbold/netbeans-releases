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

/*
 * 
 * Copyright 2005 Sun Microsystems, Inc.
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
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Accepts input of unique name for new JDBC Collaboration instance.
 * 
 * @author
 */
public class JDBCWizardNamePanel extends JPanel implements WizardDescriptor.Panel {

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
        public void keyReleased(final KeyEvent e) {
            final String collaborationName = JDBCWizardNamePanel.this.textField.getText();

            if (collaborationName != null && collaborationName.trim().length() != 0) {
                JDBCWizardNamePanel.this.collabName = collaborationName.trim();
            } else {
                JDBCWizardNamePanel.this.collabName = null;
            }

            JDBCWizardNamePanel.this.fireChangeEvent();
        }
    }

    protected String collabName;

    /* Set <ChangeListeners> */
    protected final Set listeners = new HashSet(1);

    protected JDBCCollaborationWizard owner;

    protected JTextField textField;

    protected String title;

    /**
     * No-arg constructor for this wizard descriptor.
     */
    public JDBCWizardNamePanel() {
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
        final JLabel header = new JLabel(NbBundle.getMessage(JDBCWizardNamePanel.class, "LBL_tblwizard_namefield"));
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
    public JDBCWizardNamePanel(final JDBCCollaborationWizard myOwner, final String panelTitle) {
        this();

        this.title = panelTitle;
        this.setName(this.title);
        this.owner = myOwner;
    }

    /**
     * @see JDBCWizardPanel#addChangeListener
     */
    public final void addChangeListener(final ChangeListener l) {
        synchronized (this.listeners) {
            this.listeners.add(l);
        }
    }

    /**
     * @see JDBCWizardPanel#fireChangeEvent
     */
    public void fireChangeEvent() {
        Iterator it;

        synchronized (this.listeners) {
            it = new HashSet(this.listeners).iterator();
        }

        final ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    /**
     * @see JDBCWizardPanel#getComponent
     */
    public Component getComponent() {
        return this;
    }

    /**
     * Gets current value of collaboration name as entered by user.
     * 
     * @return current user-specified name
     */
    public String getCollabName() {
        return this.collabName;
    }

    /**
     * @seeJDBCWizardPanel#getHelp
     */
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return new HelpCtx(JDBCWizardNamePanel.class);

    }

    /**
     * Indicates whether current contents of collaboration name textfield correspond to the name of
     * an existing collaboration in the current project.
     * 
     * @return true if textfield contains the name of an existing collab; false otherwise
     */
    public boolean isDuplicateCollabName() {
        return false;
    }

    /**
     * @see JDBCWizardPanel#isValid
     */
    public boolean isValid() {
        boolean returnVal = false;
        if (this.collabName != null) {
            returnVal = true;
        }
        return returnVal;
    }

    /**
     * @see JDBCWizardPanel#readSettings
     */
    public void readSettings(final Object settings) {
        WizardDescriptor wd = null;
        if (settings instanceof JDBCWizardContext) {
            final JDBCWizardContext wizardContext = (JDBCWizardContext) settings;
            wd = (WizardDescriptor) wizardContext.getProperty(JDBCWizardContext.WIZARD_DESCRIPTOR);
        } else if (settings instanceof WizardDescriptor) {
            wd = (WizardDescriptor) settings;
        }

        if (wd != null) {
            final String myCollabName = (String) wd.getProperty(JDBCCollaborationWizard.COLLABORATION_NAME);
            this.textField.setText(myCollabName);
        }
    }

    /**
     * @see JDBCWizardPanel#removeChangeListener
     */
    public final void removeChangeListener(final ChangeListener l) {
        synchronized (this.listeners) {
            this.listeners.remove(l);
        }
    }

    /**
     * @see JDBCWizardPanel#storeSettings
     */
    public void storeSettings(final Object settings) {
        WizardDescriptor wd = null;
        if (settings instanceof JDBCWizardContext) {
            final JDBCWizardContext wizardContext = (JDBCWizardContext) settings;
            wd = (WizardDescriptor) wizardContext.getProperty(JDBCWizardContext.WIZARD_DESCRIPTOR);
        } else if (settings instanceof WizardDescriptor) {
            wd = (WizardDescriptor) settings;
            this.owner.setDescriptor(wd);
        }

        if (wd != null) {

            final Object selectedOption = wd.getValue();
            if (NotifyDescriptor.CANCEL_OPTION == selectedOption || NotifyDescriptor.CLOSED_OPTION == selectedOption) {
                return;
            }

            wd.putProperty(JDBCCollaborationWizard.COLLABORATION_NAME, this.collabName);

        }
    }
}
