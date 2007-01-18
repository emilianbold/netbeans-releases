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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.UIManager;
import org.netbeans.modules.j2ee.common.DatasourceUIHelper;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.ui.EJBPreferences;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;


/**
 * Provide an interface to support datasource selection.
 * @author  Chris Webster
 */
public class SelectDatabasePanel extends javax.swing.JPanel {
    
    public static final String IS_VALID = "SelectDatabasePanel_isValid"; //NOI18N
    
    private Color nbErrorForeground;
    
    private Node driverNode;
    private static String PROTOTYPE_VALUE = "jdbc:pointbase://localhost/sample [pbpublic on PBPUBLIC] "; //NOI18N
    private final ServiceLocatorStrategyPanel slPanel;
    private final DatasourceComboBoxHelper comboHelper;
    private final EJBPreferences ejbPreferences;

    public SelectDatabasePanel(J2eeModuleProvider provider, String lastLocator) {
        initComponents();
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SelectDatabasePanel.class, "ACSD_ChooseDatabase"));
        ejbPreferences = new EJBPreferences();
        
        dsCombo.setPrototypeDisplayValue(PROTOTYPE_VALUE);
        if (provider != null && provider.isDatasourceCreationSupported()) {
            // DS API is supported by the server plugin
            if (Util.isValidServerInstance(provider)) {
                DatasourceUIHelper.connect(provider, dsCombo);
            } else {
                // copied from WizardDescriptor
                nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
                if (nbErrorForeground == null) {
                    //nbErrorForeground = new Color(89, 79, 191); // RGB suggested by Bruce in #28466
                    nbErrorForeground = new Color(255, 0, 0); // RGB suggested by jdinga in #65358
                }

                errorLabel.setForeground(nbErrorForeground);
                errorLabel.setText(NbBundle.getMessage(SelectDatabasePanel.class, "ERR_MissingServer"));
            }
            comboHelper = null;
        } else {
            comboHelper = new DatasourceComboBoxHelper(dsCombo);
        }
        
        dsCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                checkDatasource();
            }
        });
        
        slPanel = new ServiceLocatorStrategyPanel(lastLocator);
        serviceLocatorPanel.add(slPanel, BorderLayout.CENTER);
        createResourcesCheckBox.setSelected(ejbPreferences.isAgreedCreateServerResources());
        slPanel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(ServiceLocatorStrategyPanel.IS_VALID)) {
                    Object newvalue = evt.getNewValue();
                    if (newvalue instanceof Boolean) {
                        boolean isServiceLocatorOk = ((Boolean)newvalue).booleanValue();
                        if (isServiceLocatorOk) {
                            checkDatasource();
                        } else {
                            firePropertyChange(IS_VALID, true, false);
                        }
                    }
                }
            }
        });
    }
    
    public Datasource getDatasource() {
        return (Datasource)dsCombo.getSelectedItem();
    }

    public String getServiceLocator() {
        return slPanel.classSelected();
    }
    
    public boolean createServerResources() {
        return createResourcesCheckBox.isSelected();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        dsCombo = new javax.swing.JComboBox();
        dsLabel = new javax.swing.JLabel();
        serviceLocatorPanel = new javax.swing.JPanel();
        createResourcesCheckBox = new javax.swing.JCheckBox();
        errorLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 11, 11);
        add(dsCombo, gridBagConstraints);
        dsCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectDatabasePanel.class, "ACSD_dsCombo"));

        dsLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(SelectDatabasePanel.class, "LBL_ConnectionMnemonic").charAt(0));
        dsLabel.setLabelFor(dsCombo);
        org.openide.awt.Mnemonics.setLocalizedText(dsLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entres/Bundle").getString("LBL_DataSource"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 11, 11);
        add(dsLabel, gridBagConstraints);
        dsLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectDatabasePanel.class, "ACSD_DataSource"));

        serviceLocatorPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 11);
        add(serviceLocatorPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(createResourcesCheckBox, org.openide.util.NbBundle.getBundle(SelectDatabasePanel.class).getString("LBL_CreateServerResources"));
        createResourcesCheckBox.setToolTipText(org.openide.util.NbBundle.getBundle(SelectDatabasePanel.class).getString("ToolTip_CreateServerResources"));
        createResourcesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        createResourcesCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        createResourcesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createResourcesCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 11);
        add(createResourcesCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(errorLabel, " ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 11);
        add(errorLabel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    private void createResourcesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createResourcesCheckBoxActionPerformed
        ejbPreferences.setAgreedCreateServerResources(createResourcesCheckBox.isSelected());
    }//GEN-LAST:event_createResourcesCheckBoxActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox createResourcesCheckBox;
    private javax.swing.JComboBox dsCombo;
    private javax.swing.JLabel dsLabel;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JPanel serviceLocatorPanel;
    // End of variables declaration//GEN-END:variables
    
    protected void checkDatasource() {
        if (dsCombo.getSelectedItem() instanceof Datasource) {
            firePropertyChange(IS_VALID, false, true);
        } else {
            firePropertyChange(IS_VALID, true, false);
        }
    }

}
