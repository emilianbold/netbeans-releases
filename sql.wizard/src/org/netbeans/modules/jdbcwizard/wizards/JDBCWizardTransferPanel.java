/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * Implements a two-list transfer panel with bulk add/remove capability.
 * 
 * @author
 */
public class JDBCWizardTransferPanel extends JPanel implements ActionListener, WizardDescriptor.Panel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /* Log4J category string */
    private static final String LOG_CATEGORY = JDBCWizardTransferPanel.class.getName();

    /* Set <ChangeListeners> */
    private final Set listeners = new HashSet(1);

    private List selTableList = new ArrayList();

    private JDBCWizardTablePanel tablePanel;

    /** Creates a default instance of JDBCWizardTransferPanel. */
    public JDBCWizardTransferPanel() {
    }

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
        this();
        if (title != null && title.trim().length() != 0) {
            this.setName(title);
        }

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
        return this;
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

		    this.setLayout(new BorderLayout());
		    this.add(this.tablePanel, BorderLayout.CENTER);
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
        	this.remove(this.tablePanel);
        	this.selTableList = null;
        	return;
        }
    }

}
