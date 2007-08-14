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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.support.DatabaseExplorerUIs;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.util.PersistenceProviderComboboxHelper;
import org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibrarySupport;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel.TableGeneration;
import org.openide.util.NbBundle;

/**
 *
 * @author  Martin Adamek
 */
public class PersistenceUnitWizardPanelJdbc extends PersistenceUnitWizardPanel{
    
    public PersistenceUnitWizardPanelJdbc(Project project, ChangeListener changeListener,  boolean editName) {
        this(project, changeListener, editName, TableGeneration.CREATE);
    }
    
    public PersistenceUnitWizardPanelJdbc(Project project, ChangeListener changeListener,
            boolean editName, TableGeneration tg) {
            
        super(project);
        initComponents();
        setTableGeneration(tg);
        
        PersistenceProviderComboboxHelper comboHelper = new PersistenceProviderComboboxHelper(project);
        comboHelper.connect(libraryCombo);
        
        unitNameTextField.setText(getCandidateName());
        unitNameTextField.selectAll();
        // unit name editing is not available when adding first PU
        unitNameTextField.setVisible(editName);
        unitNameLabel.setVisible(editName);
        
        DatabaseExplorerUIs.connect(jdbcCombo, ConnectionManager.getDefault());
        jdbcCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                checkValidity();
            }
        });
        
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
    
    public Library getPersistenceLibrary() {
        return PersistenceLibrarySupport.getLibrary((Provider) libraryCombo.getSelectedItem());
    }
    
    public Provider getSelectedProvider(){
        return (Provider) libraryCombo.getSelectedItem();
    }
    
    public DatabaseConnection getPersistenceConnection() {
        return (DatabaseConnection) jdbcCombo.getSelectedItem();
    }
    
    public void setPreselectedDB(String db) {
        boolean hasItem = false;
        for (int i = 0; i < jdbcCombo.getItemCount(); i++) {
            if (jdbcCombo.getItemAt(i) instanceof DatabaseConnection) {
                DatabaseConnection conn = (DatabaseConnection) jdbcCombo.getItemAt(i);
                if (conn.getDisplayName().equals(db) || conn.toString().equals(db)) {
                    hasItem = true;
                    break;
                }
            }
        }
        jdbcCombo.setSelectedItem(ConnectionManager.getDefault().getConnection(db));
        jdbcCombo.setEnabled(!hasItem);
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
        if (!(libraryCombo.getSelectedItem() instanceof Provider)) {
            return false;
        }
        if (!(jdbcCombo.getSelectedItem() instanceof DatabaseConnection)) {
            return false;
        }
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
    
    public void setErrorMessage(String msg) {
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
        libraryLabel = new javax.swing.JLabel();
        libraryCombo = new javax.swing.JComboBox();
        jdbcCombo = new javax.swing.JComboBox();
        jdbcLabel = new javax.swing.JLabel();
        errorMessage = new javax.swing.JLabel();

        setName(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelJdbc.class, "LBL_Step1"));
        unitNameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("MN_UnitName").charAt(0));
        unitNameLabel.setLabelFor(unitNameTextField);
        unitNameLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelJdbc.class, "LBL_UnitName"));
        unitNameLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("ACSD_UnitName"));

        unitNameTextField.setColumns(40);
        unitNameTextField.setText("em");

        jLabel1.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelJdbc.class, "LBL_SpecifyPersistenceProvider"));

        jLabel2.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelJdbc.class, "LBL_TableGeneration"));
        jLabel2.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("ACSD_TableGeneration"));

        tableCreationButtonGroup.add(ddlCreate);
        ddlCreate.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("CHB_Create_mnem").charAt(0));
        ddlCreate.setSelected(true);
        ddlCreate.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelJdbc.class, "LBL_Create"));
        ddlCreate.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ddlCreate.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ddlCreate.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("ACSD_Create"));

        tableCreationButtonGroup.add(ddlDropCreate);
        ddlDropCreate.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("CHB_DropCreate_mnem").charAt(0));
        ddlDropCreate.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelJdbc.class, "LBL_DropCreate"));
        ddlDropCreate.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ddlDropCreate.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ddlDropCreate.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("ACSD_DropCreate"));

        tableCreationButtonGroup.add(ddlUnkown);
        ddlUnkown.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("CHB_None_mnem").charAt(0));
        ddlUnkown.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelJdbc.class, "LBL_None"));
        ddlUnkown.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ddlUnkown.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ddlUnkown.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("ACSD_None"));

        libraryLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("MN_Library").charAt(0));
        libraryLabel.setLabelFor(libraryCombo);
        libraryLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelJdbc.class, "LBL_Library"));
        libraryLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("ACSD_Library"));

        libraryCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                libraryComboActionPerformed(evt);
            }
        });

        jdbcCombo.setRenderer(new JdbcListCellRenderer());

        jdbcLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("MN_JdbcConnection").charAt(0));
        jdbcLabel.setLabelFor(jdbcCombo);
        jdbcLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelJdbc.class, "LBL_JdbcConnection"));
        jdbcLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("ACSD_JdbcConnection"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(errorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(unitNameLabel)
                            .add(libraryLabel)
                            .add(jdbcLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(libraryCombo, 0, 352, Short.MAX_VALUE)
                            .add(jdbcCombo, 0, 352, Short.MAX_VALUE)
                            .add(unitNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel1)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ddlCreate)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ddlDropCreate)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ddlUnkown)))
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
                    .add(libraryLabel)
                    .add(libraryCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jdbcLabel)
                    .add(jdbcCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(ddlCreate)
                    .add(ddlDropCreate)
                    .add(ddlUnkown)
                    .add(jLabel2))
                .add(20, 20, 20)
                .add(errorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void libraryComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_libraryComboActionPerformed
        checkValidity();
    }//GEN-LAST:event_libraryComboActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JRadioButton ddlCreate;
    private javax.swing.JRadioButton ddlDropCreate;
    private javax.swing.JRadioButton ddlUnkown;
    private javax.swing.JLabel errorMessage;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JComboBox jdbcCombo;
    private javax.swing.JLabel jdbcLabel;
    private javax.swing.JComboBox libraryCombo;
    private javax.swing.JLabel libraryLabel;
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
