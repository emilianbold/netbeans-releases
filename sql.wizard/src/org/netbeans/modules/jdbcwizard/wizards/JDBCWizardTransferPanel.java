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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.ChangeListener;

import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * Implements a two-list transfer panel with bulk add/remove capability.
 * 
 * @author
 */
public class JDBCWizardTransferPanel implements ActionListener, WizardDescriptor.Panel {

    /* Log4J category string */
    private static final String LOG_CATEGORY = JDBCWizardTransferPanel.class.getName();

    /* Set <ChangeListeners> */
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);

    private List selTableList = new ArrayList();

    private JDBCWizardTablePanel tablePanel;
    private JDBCWizardTransferPanelUI comp;
    private String title;

    /**
     * Creates a new instance of JDBCWizardTransferPanel using the given ListModels to initially
     * populate the source and destination panels.
     * 
     * @param title String to be displayed as title of this panel
     * @param dsList List of DatabaseModels used to populate datasource panel
     * @param destColl Collection of selected DatabaseModels
     * @param sourceOTD true if this panel displays available selections for source OTDs; false if
     *            it displays available destination OTDs
     */
    public JDBCWizardTransferPanel(final String title) {
        this.title = title;
//        final ArrayList testList = new ArrayList();
//        this.tablePanel = new JDBCWizardTablePanel(testList);
//
//        this.setLayout(new BorderLayout());
//        this.add(this.tablePanel, BorderLayout.CENTER);
//        JDBCWizardTransferPanel.this.tablePanel.resetTable(this.selTableList);
    }

    /**
     * Invoked whenever one of the transfer buttons is clicked.
     * 
     * @param e ActionEvent to handle
     */
    public void actionPerformed(final ActionEvent e) {
        // String cmd = e.getActionCommand();

    }

    /**
     * @see org.openide.WizardDescriptor.Panel#addChangeListener
     */
    public void addChangeListener(final ChangeListener l) {
        synchronized (this.listeners) {
            this.listeners.add(l);
        }
    }

    /**
     * @see org.openide.WizardDescriptor.Panel#getComponent
     */
    public Component getComponent() {
        if (comp == null) {
            comp = new JDBCWizardTransferPanelUI (title);
        }
        return comp;
    }

    /**
     * @see org.openide.WizardDescriptor.Panel#getHelp
     */
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
      return new HelpCtx(JDBCWizardTransferPanel.class);

    }

    /**
     * @see org.openide.WizardDescriptor.Panel#isValid
     */
    public boolean isValid() {
        boolean returnVal = false;

        if (this.tablePanel.getTables().size() != 0) {
            returnVal = true;
        }
        return returnVal;
    }

    /**
     * @see org.openide.WizardDescriptor.Panel#readSettings
     */
    public void readSettings(final Object settings) {
		WizardDescriptor wizard = null;
		if (settings instanceof JDBCWizardContext) {
			final JDBCWizardContext wizardContext = (JDBCWizardContext) settings;
			wizard = (WizardDescriptor) wizardContext
					.getProperty(JDBCWizardContext.WIZARD_DESCRIPTOR);

		} else if (settings instanceof WizardDescriptor) {
			wizard = (WizardDescriptor) settings;
		}

		if (wizard != null
				&& WizardDescriptor.NEXT_OPTION.equals(wizard.getValue())) {
			final Object[] sources = (Object[]) wizard
					.getProperty(JDBCWizardContext.SELECTEDTABLES);
			this.selTableList = Arrays.asList(sources);
			final ArrayList testList = new ArrayList();
		    this.tablePanel = new JDBCWizardTablePanel(testList);

		    if (comp == null) {
                getComponent ();
            }
            comp.setLayout(new BorderLayout());
		    comp.add(this.tablePanel, BorderLayout.CENTER);
		    JDBCWizardTransferPanel.this.tablePanel.resetTable(this.selTableList);
		}
	}

    /**
	 * @see org.openide.WizardDescriptor.Panel#removeChangeListener
	 */
    public void removeChangeListener(final ChangeListener l) {
        synchronized (this.listeners) {
            this.listeners.remove(l);
        }
    }

    /**
     * @see org.openide.WizardDescriptor.Panel#storeSettings
     */
    public void storeSettings(final Object settings) {
        WizardDescriptor wizard = null;
        if (settings instanceof JDBCWizardContext) {
            final JDBCWizardContext wizardContext = (JDBCWizardContext) settings;
            wizard = (WizardDescriptor) wizardContext.getProperty(JDBCWizardContext.WIZARD_DESCRIPTOR);

        } else if (settings instanceof WizardDescriptor) {
            wizard = (WizardDescriptor) settings;
        }
        
        final Object selectedOption = wizard.getValue();
        if (NotifyDescriptor.CANCEL_OPTION == selectedOption || NotifyDescriptor.CLOSED_OPTION == selectedOption) {
                return;
        }
        if(selectedOption.toString().equals("PREVIOUS_OPTION")){
            if (comp == null) {
                getComponent ();
            }
        	comp.remove(this.tablePanel);
        	this.selTableList = null;
        	return;
        }
    }

}
