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

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Arrays;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

import org.openide.util.NbBundle;

import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBColumn;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBTable;
import org.netbeans.modules.jdbcwizard.builder.util.XMLCharUtil;
import org.netbeans.modules.jdbcwizard.builder.wsdl.GenerateWSDL;
import org.netbeans.modules.jdbcwizard.builder.xsd.XSDGenerator;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBConnectionDefinition;



/**
 * @author npedapudi
 */
public class JNDINamePanel extends javax.swing.JPanel implements WizardDescriptor.Panel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected final Set listeners = new HashSet(1);

    private static final String XSD_EXT = ".xsd";

    private static final boolean enableNext = false;

    private static final String JNDI_DEFAULT_NAME = "jdbc/__defaultDS";

	private static final String CONNECTION_INFO_FILE = "config\\ConnectionInfo.xml";

    /** Creates new form JNDINamePanel */
    public JNDINamePanel(final String title) {
        if (title != null && title.trim().length() != 0) {
            this.setName(title);
        }
		this.initComponents();
    }

    /**
     * intializes the components
     */
    private void initComponents() {
        this.jLabel1 = new javax.swing.JLabel();
        this.jTextField1 = new javax.swing.JTextField();
        this.jTextField1.setText(JNDINamePanel.JNDI_DEFAULT_NAME);

        this.jLabel1.setText(NbBundle.getMessage( JNDINamePanel.class, "LBL_JNDIName"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().add(55, 55, 55).add(this.jLabel1,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(28, 28, 28).add(this.jTextField1,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 179,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addContainerGap(68, Short.MAX_VALUE)));
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().add(133, 133, 133).add(
                        layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(this.jLabel1).add(
                                this.jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addContainerGap(128, Short.MAX_VALUE)));
    }

    /**
     * @return
     */
    public Component getComponent() {
        return this;
    }

    /**
     * @return
     */
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
       return new HelpCtx(JNDINamePanel.class);
    }

    /**
     * @param settings
     */
    public void readSettings(final Object settings) {
        WizardDescriptor wd = null;
        if (settings instanceof JDBCWizardContext) {
            final JDBCWizardContext wizardContext = (JDBCWizardContext) settings;
            wd = (WizardDescriptor) wizardContext.getProperty(JDBCWizardContext.WIZARD_DESCRIPTOR);

        } else if (settings instanceof WizardDescriptor) {
            wd = (WizardDescriptor) settings;
        }
        final Object selectedOption = wd.getValue();
        final boolean isAdvancingPanel = selectedOption == WizardDescriptor.NEXT_OPTION
                || selectedOption == WizardDescriptor.FINISH_OPTION;

        if (isAdvancingPanel) {

        }

    }

    /**
     * @param settings
     */
    public void storeSettings(final Object settings) {
        WizardDescriptor wd = null;
        if (settings instanceof JDBCWizardContext) {
            final JDBCWizardContext wizardContext = (JDBCWizardContext) settings;
            wd = (WizardDescriptor) wizardContext.getProperty(JDBCWizardContext.WIZARD_DESCRIPTOR);

        } else if (settings instanceof WizardDescriptor) {
            wd = (WizardDescriptor) settings;
        }

        if (wd != null) {

            final Object selectedOption = wd.getValue();
            if (NotifyDescriptor.CANCEL_OPTION == selectedOption || NotifyDescriptor.CLOSED_OPTION == selectedOption) {
                return;
            }
            // Need to revisit the code here
            final boolean isAdvancingPanel = selectedOption == WizardDescriptor.NEXT_OPTION
                    || selectedOption == WizardDescriptor.FINISH_OPTION;

            if (isAdvancingPanel) {
                final Object[] listObj = (Object[]) wd.getProperty(JDBCWizardContext.SELECTEDTABLES);
                final List list = Arrays.asList(listObj);
                final String jndiName = this.jTextField1.getText().trim();
                try {
                    final XSDGenerator xsdGen = new XSDGenerator();
                    final String targetFolderPath = (String) wd.getProperty(JDBCWizardContext.TARGETFOLDER_PATH);
                    final String collabName = (String) wd.getProperty(JDBCWizardContext.COLLABORATION_NAME);
                    DBTable selTable = null;
                    final Iterator it = list.iterator();
                    while (it.hasNext()) {
                        final DBTable sTable = (DBTable) it.next();
                        if (sTable.isSelected()) {
                        	//Make the xsd file name as valid name
                        	//Otherwise this will fail in "schemalocation" of the wsdl
                        	// the reaso ins schemalocation in wsdl is NCName
                            xsdGen.generate(sTable.getName(), targetFolderPath + File.separator + XMLCharUtil.makeValidNCName(sTable.getName())
                                    + JNDINamePanel.XSD_EXT, sTable);
                            selTable = sTable;
                        }
                    }

                    // Generate WSDL
                    final String dbType = (String) wd.getProperty(JDBCWizardContext.DBTYPE);
                    final GenerateWSDL tsk = new GenerateWSDL();
                    tsk.setSrcDirectoryLocation(targetFolderPath);
                    tsk.setWSDLFileName(collabName);
                    tsk.setDBTable(selTable);
                    tsk.setDBType(dbType);
                    tsk.setJNDIName(jndiName);
                    tsk.setDBInfo((DBConnectionDefinition) wd.getProperty(JDBCWizardContext.CONNECTION_INFO));
                    tsk.execute();
                   
                } catch (final Exception e) {

                }
            }
        }
    }
	
    /**
     * @param l
     */
    public void addChangeListener(final ChangeListener l) {
        synchronized (this.listeners) {
            this.listeners.add(l);
        }
    }

    /**
     * @param l
     */
    public void removeChangeListener(final ChangeListener l) {
        synchronized (this.listeners) {
            this.listeners.remove(l);
        }
    }

    /**
     * 
     *
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

    /**
     * @see org.openide.WizardDescriptor.Panel#isValid
     */
    public boolean isValid() {
        if (this.enableNext) {
            return true;
        }

        return super.isValid();
    }

    // Variables declaration - do not modify
    private javax.swing.JLabel jLabel1;

    private javax.swing.JTextField jTextField1;
    // End of variables declaration

}
