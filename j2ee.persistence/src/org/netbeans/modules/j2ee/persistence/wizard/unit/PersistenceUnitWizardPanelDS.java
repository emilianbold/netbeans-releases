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

package org.netbeans.modules.j2ee.persistence.wizard.unit;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.provider.DefaultProvider;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSource;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourcePopulator;
import org.netbeans.modules.j2ee.persistence.util.PersistenceProviderComboboxHelper;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel.TableGeneration;
import org.openide.util.NbBundle;

/**
 *
 * @author  Martin Adamek
 */
public class PersistenceUnitWizardPanelDS extends PersistenceUnitWizardPanel {
    
    public PersistenceUnitWizardPanelDS(Project project, ChangeListener changeListener, boolean editName) {
        this(project, changeListener, editName, TableGeneration.CREATE);
    }
    
    public PersistenceUnitWizardPanelDS(Project project, ChangeListener changeListener,
            boolean editName, TableGeneration tg) {

        super(project);
        initComponents();
        setTableGeneration(tg);
       
        if (ProviderUtil.isValidServerInstanceOrNone(project)){
            connectDatasources();
        }
        
        PersistenceProviderComboboxHelper comboHelper = new PersistenceProviderComboboxHelper(project);
        comboHelper.connect(providerCombo);
        unitNameTextField.setText(getCandidateName());
        unitNameTextField.selectAll();

        // unit name editing is not available when adding first PU
        unitNameTextField.setVisible(editName);
        unitNameLabel.setVisible(editName);
        
        unitNameTextField.getDocument().addDocumentListener(new ValidationListener());
        errorMessage.setForeground(Color.RED);
    }
    
    /**
     * Pre-selects appropriate table generation strategy radio button.
     */
    private void setTableGeneration(TableGeneration tg){
        if (TableGeneration.CREATE.equals(tg)){
            ddlCreate.setSelected(true);
        } else if (TableGeneration.DROP_CREATE.equals(tg)){
            ddlDropCreate.setSelected(true);
        } else {
            ddlUnkown.setSelected(true);
        }
    }
    
    private void connectDatasources() {
        JPADataSourcePopulator dsPopulator = project.getLookup().lookup(JPADataSourcePopulator.class);
        dsPopulator.connect(dsCombo);
        
        dsCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkValidity();
            }
        });
        
        ((JTextComponent)dsCombo.getEditor().getEditorComponent()).
                getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                checkValidity();
            }
            public void insertUpdate(DocumentEvent e) {
                checkValidity();
            }
            public void removeUpdate(DocumentEvent e) {
                checkValidity();
            }
        });
    }
    
    
    /**
     * Checks whether this panel is in valid state (see <code>#isValidPanel()</code>)
     * and fires appropriate property changes.
     */
    private void checkValidity(){
        if (isValidPanel()) {
            firePropertyChange(IS_VALID, false, true);
        } else {
            firePropertyChange(IS_VALID, true, false);
        }
    }
    
    public String getPersistenceUnitName() {
        return unitNameTextField.getText();
    }
    
    public String getDatasource() {
        return ((JTextComponent)dsCombo.getEditor().getEditorComponent()).getText();
    }
    
    public void setPreselectedDB(String db) {
        boolean hasItem = false;
        for (int i = 0; i < dsCombo.getItemCount(); i++) {
            if (dsCombo.getItemAt(i) instanceof JPADataSource) {
                JPADataSource ds = (JPADataSource) dsCombo.getItemAt(i);
                if (ds.getJndiName().equals(db)) {
                    hasItem = true;
                    break;
                }
            }
        }
        dsCombo.setSelectedItem(db);
        dsCombo.setEnabled(!hasItem);
    }
    
    public boolean isNonDefaultProviderEnabled() {
        return !(providerCombo.getSelectedItem() instanceof DefaultProvider);
    }
    
    public String getNonDefaultProvider() {
        return ((Provider) providerCombo.getSelectedItem()).getProviderClass();
    }
    
    
    public boolean isJTA() {
        return jtaCheckBox.isEnabled() && jtaCheckBox.isSelected();
    }
    
    
    public String getTableGeneration() {
        if (ddlCreate.isSelected()) {
            return Provider.TABLE_GENERATION_CREATE;
        } else if (ddlDropCreate.isSelected()) {
            return Provider.TABLE_GENERATION_DROPCREATE;
        } else {
            return Provider.TABLE_GENERATTION_UNKOWN;
        }
    }
    
    public boolean isValidPanel() {
        try{
            if (!isNameValid()){
                return false;
            }
        } catch (InvalidPersistenceXmlException ipx){
            setErrorMessage(NbBundle.getMessage(PersistenceUnitWizardDescriptor.class,"ERR_InvalidPersistenceXml", ipx.getPath())); //NO18N
            return false;
        }
        return true;
    }
    
    /**
     * Checks whether name of the persistence unit is valid, i.e. it's not
     * empty and it's unique.
     */
    private boolean isNameValid() throws InvalidPersistenceXmlException{
        return isEmptyOrNull(getPersistenceUnitName()) ? false : isNameUnique();
    }
    
    /**
     * @return true if given str is null, empty or contains only spaces.
     */
    private boolean isEmptyOrNull(String str){
        return str == null || "".equals(str.trim());
    }
    
    public Provider getSelectedProvider() {
        return (Provider) providerCombo.getSelectedItem();
    }
    
    public void setErrorMessage(String msg){
        errorMessage.setText(msg);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        buttonGroup1 = new javax.swing.ButtonGroup();
        tableCreationButtonGroup = new javax.swing.ButtonGroup();
        unitNameLabel = new javax.swing.JLabel();
        unitNameTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        ddlCreate = new javax.swing.JRadioButton();
        ddlDropCreate = new javax.swing.JRadioButton();
        ddlUnkown = new javax.swing.JRadioButton();
        datasourceLabel = new javax.swing.JLabel();
        jtaCheckBox = new javax.swing.JCheckBox();
        dsCombo = new javax.swing.JComboBox();
        persistenceProviderLabel = new javax.swing.JLabel();
        providerCombo = new javax.swing.JComboBox();
        errorMessage = new javax.swing.JLabel();

        setName(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelDS.class, "LBL_Step1"));
        unitNameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("MN_UnitName").charAt(0));
        unitNameLabel.setLabelFor(unitNameTextField);
        unitNameLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelDS.class, "LBL_UnitName"));
        unitNameLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("ACSD_UnitName"));

        unitNameTextField.setColumns(40);
        unitNameTextField.setText("em");
        unitNameTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("ACSD_UnitName"));

        jLabel1.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelDS.class, "LBL_SpecifyPersistenceProvider"));

        jLabel2.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelDS.class, "LBL_TableGeneration"));
        jLabel2.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("ACSD_TableGeneration"));

        tableCreationButtonGroup.add(ddlCreate);
        ddlCreate.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("CHB_Create_mnem").charAt(0));
        ddlCreate.setSelected(true);
        ddlCreate.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelDS.class, "LBL_Create"));
        ddlCreate.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ddlCreate.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ddlCreate.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("ACSD_Create"));

        tableCreationButtonGroup.add(ddlDropCreate);
        ddlDropCreate.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("CHB_DropCreate_mnem").charAt(0));
        ddlDropCreate.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelDS.class, "LBL_DropCreate"));
        ddlDropCreate.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ddlDropCreate.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ddlDropCreate.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("ACSD_DropCreate"));

        tableCreationButtonGroup.add(ddlUnkown);
        ddlUnkown.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("CHB_None_mnem").charAt(0));
        ddlUnkown.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelDS.class, "LBL_None"));
        ddlUnkown.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ddlUnkown.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ddlUnkown.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("ACSD_None"));

        datasourceLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("MN_DatasourceName").charAt(0));
        datasourceLabel.setLabelFor(dsCombo);
        datasourceLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelDS.class, "LBL_ServerDataSource"));
        datasourceLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("ACSD_DatasourceName"));

        jtaCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("CHB_JTA_mnem").charAt(0));
        jtaCheckBox.setSelected(true);
        jtaCheckBox.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelDS.class, "LBL_JTA"));
        jtaCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jtaCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jtaCheckBox.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("ACSD_JTA"));

        dsCombo.setEditable(true);

        persistenceProviderLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("MN_Provider").charAt(0));
        persistenceProviderLabel.setLabelFor(providerCombo);
        persistenceProviderLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("LBL_PersistenceProvider"));
        persistenceProviderLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("ACSD_Provider"));
        persistenceProviderLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("ACSD_Provider"));

        providerCombo.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("ACSD_Provider"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(unitNameLabel)
                            .add(persistenceProviderLabel)
                            .add(datasourceLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(unitNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                            .add(providerCombo, 0, 360, Short.MAX_VALUE)
                            .add(dsCombo, 0, 360, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ddlCreate)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ddlDropCreate)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ddlUnkown))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jtaCheckBox)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel1)
                    .add(errorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 514, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(unitNameLabel)
                    .add(unitNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(persistenceProviderLabel)
                    .add(providerCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(datasourceLabel)
                    .add(dsCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jtaCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(ddlCreate)
                    .add(ddlDropCreate)
                    .add(ddlUnkown)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(errorMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel datasourceLabel;
    private javax.swing.JRadioButton ddlCreate;
    private javax.swing.JRadioButton ddlDropCreate;
    private javax.swing.JRadioButton ddlUnkown;
    private javax.swing.JComboBox dsCombo;
    private javax.swing.JLabel errorMessage;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JCheckBox jtaCheckBox;
    private javax.swing.JLabel persistenceProviderLabel;
    private javax.swing.JComboBox providerCombo;
    private javax.swing.ButtonGroup tableCreationButtonGroup;
    private javax.swing.JLabel unitNameLabel;
    private javax.swing.JTextField unitNameTextField;
    // End of variables declaration//GEN-END:variables
    
    /**
     * Document listener that invokes <code>checkValidity</code> when
     * changes are made.
     */
    private class ValidationListener implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            checkValidity();
        }
        public void removeUpdate(DocumentEvent e) {
            checkValidity();
        }
        public void changedUpdate(DocumentEvent e) {
            checkValidity();
        }
    }
}

