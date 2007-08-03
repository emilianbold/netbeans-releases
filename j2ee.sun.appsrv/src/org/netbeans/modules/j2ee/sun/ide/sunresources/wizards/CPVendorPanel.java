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
/*
 * CPVendorPanel.java -- synopsis.
 *
 */


package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.ButtonGroup;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataListener;
import javax.swing.event.DocumentListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;

import org.openide.util.HelpCtx;
import org.openide.loaders.TemplateWizard;

import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ResourceUtils;
import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ResourceConfigurator;
import org.netbeans.modules.j2ee.sun.sunresources.beans.Field;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup;
import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroupHelper;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldHelper;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

public class CPVendorPanel extends ResourceWizardPanel implements ChangeListener, DocumentListener, ListDataListener {
    
    static final long serialVersionUID = 93474632245456421L;
    
    private ArrayList dbconns;
    private ResourceConfigHelper helper;
    private FieldGroup generalGroup, propGroup, vendorGroup;
    private boolean useExistingConnection = true;
    private String[] vendors;
    private boolean firstTime = true;
    private boolean setupValid = true;
    
    private static final String CONST_TRUE = "true"; // NOI18N
        
    /** Creates new form DBSchemaConnectionpanel */
    public CPVendorPanel(ResourceConfigHelper helper, Wizard wiardInfo) {
        this.firstTime = true;
        this.helper = helper;
        this.generalGroup = FieldGroupHelper.getFieldGroup(wiardInfo, __General); 
        this.propGroup = FieldGroupHelper.getFieldGroup(wiardInfo, __Properties); 
        this.vendorGroup = FieldGroupHelper.getFieldGroup(wiardInfo, __PropertiesURL); 
        ButtonGroup bg = new ButtonGroup();
        dbconns = new ArrayList();
        
        setName(bundle.getString("TITLE_ConnPoolWizardPanel_dbConn")); //NOI18N

        initComponents ();
        initAccessibility();
        
        nameLabel.setLabelFor(nameField);
        nameLabel.setDisplayedMnemonic(bundle.getString("LBL_pool-name_Mnemonic").charAt(0)); //NOI18N
        existingConnRadioButton.setMnemonic(bundle.getString("ExistingConnection_Mnemonic").charAt(0)); //NOI18N
        newCofigRadioButton.setMnemonic(bundle.getString("NewConfiguration_Mnemonic").charAt(0)); //NOI18N
        isXA.setMnemonic(bundle.getString("isXA_Mnemonic").charAt(0)); //NOI18N
        
        nameComboBox.registerKeyboardAction(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    nameComboBox.requestFocus();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);            

        bg.add(existingConnRadioButton);
        bg.add(newCofigRadioButton);
        bg.getSelection().addChangeListener(this);
        try{
            DatabaseConnection[] cons = ConnectionManager.getDefault().getConnections();
            for(int i=0; i < cons.length; i++){
                existingConnComboBox.addItem(cons[i].getName());
                dbconns.add(cons[i]);
            }
        }catch(Exception ex){
            // Connection could not be found
        }
        if (existingConnComboBox.getItemCount() == 0) {
            existingConnComboBox.insertItemAt(bundle.getString("NoConnection"), 0); //NOI18N
            newCofigRadioButton.setSelected(true);
        } else {
            existingConnComboBox.insertItemAt(bundle.getString("SelectFromTheList"), 0); //NOI18N
            existingConnRadioButton.setSelected(true);
            setExistingConnData();
        }
        
        //String vendorName = "other"; //NOI18N
        existingConnRadioButton.setEnabled(true);
        existingConnComboBox.setEnabled(true);
        newCofigRadioButton.setEnabled(true);
        nameComboBox.setEnabled(true);
        
        Field vendorField = FieldHelper.getField(this.generalGroup, __DatabaseVendor);
        vendors = FieldHelper.getTags(vendorField);
        for (int i = 0; i < vendors.length; i++) {
            nameComboBox.addItem(bundle.getString("DBVendor_" + vendors[i]));   //NOI18N
        }
        
        if (nameComboBox.getItemCount() == 0)
            nameComboBox.insertItemAt(bundle.getString("NoTemplate"), 0); //NOI18N
        else
            nameComboBox.insertItemAt(bundle.getString("SelectFromTheList"), 0); //NOI18N
        nameComboBox.setSelectedIndex(0);
        
        existingConnComboBox.getModel().addListDataListener(this);
        nameComboBox.getModel().addListDataListener(this);
        isXA.setSelected(helper.getData().getString(__IsXA).equals(CONST_TRUE));  //NOI18N
        isXA.addChangeListener(this);
        newCofigRadioButton.addChangeListener(this);
        
        getAccessibleContext().setAccessibleName(bundle.getString("TITLE_ConnPoolWizardPanel_dbConn"));
        getAccessibleContext().setAccessibleDescription(bundle.getString("TITLE_ConnPoolWizardPanel_dbConn"));
        
        this.firstTime = false;
    }
    
    private void initAccessibility(){
        descriptionTextArea.getAccessibleContext().setAccessibleName(bundle.getString("ACS_DescriptionA11yName"));  // NOI18N
        descriptionTextArea.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_DescriptionA11yDesc"));  // NOI18N
        
        nameLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ToolTip_pool-name")); //NOI18N
        nameField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_pool-nameA11yDesc")); //NOI18N
        
        existingConnRadioButton.getAccessibleContext().setAccessibleName(bundle.getString("ExistingConnection")); //NOI18N
        existingConnRadioButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_ExistingConnectionA11yDesc")); //NOI18N
        existingConnComboBox.getAccessibleContext().setAccessibleName(bundle.getString("ACS_ExistingConnectionComboBoxA11yName"));  // NOI18N
        existingConnComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_ExistingConnectionComboBoxA11yDesc"));  // NOI18N
        
        newCofigRadioButton.getAccessibleContext().setAccessibleName(bundle.getString("NewConfiguration")); //NOI18N
        newCofigRadioButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_NewConnectionA11yDesc")); //NOI18N
        nameComboBox.getAccessibleContext().setAccessibleName(bundle.getString("ACS_NewConnectionComboBoxA11yName"));  // NOI18N
        nameComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_NewConnectionComboBoxA11yDesc"));  // NOI18N
        
        isXA.getAccessibleContext().setAccessibleName(bundle.getString("isXA")); //NOI18N
        isXA.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_isXA_A11yDesc")); //NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        descriptionTextArea = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        existingConnRadioButton = new javax.swing.JRadioButton();
        existingConnComboBox = new javax.swing.JComboBox();
        newCofigRadioButton = new javax.swing.JRadioButton();
        nameComboBox = new javax.swing.JComboBox();
        isXA = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        setMaximumSize(new java.awt.Dimension(600, 350));
        setMinimumSize(new java.awt.Dimension(600, 350));
        setPreferredSize(new java.awt.Dimension(600, 350));
        descriptionTextArea.setEditable(false);
        descriptionTextArea.setFont(javax.swing.UIManager.getFont("Label.font"));
        descriptionTextArea.setText(bundle.getString("Description"));
        descriptionTextArea.setDisabledTextColor(javax.swing.UIManager.getColor("Label.foreground"));
        descriptionTextArea.setRequestFocusEnabled(false);
        descriptionTextArea.setEnabled(false);
        descriptionTextArea.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(descriptionTextArea, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        nameLabel.setText(bundle.getString("LBL_pool-name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 12, 0);
        jPanel1.add(nameLabel, gridBagConstraints);

        nameField.setText(this.helper.getData().getString(__Name));
        nameField.setMinimumSize(new java.awt.Dimension(60, 21));
        nameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CPVendorPanel.this.nameFieldActionPerformed(evt);
            }
        });
        nameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                CPVendorPanel.this.nameFieldKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 0);
        jPanel1.add(nameField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 12, 20, 12);
        add(jPanel1, gridBagConstraints);

        existingConnRadioButton.setSelected(true);
        existingConnRadioButton.setText(bundle.getString("ExistingConnection"));
        existingConnRadioButton.setToolTipText(bundle.getString("ACS_ExistingConnectionA11yDesc"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(existingConnRadioButton, gridBagConstraints);

        existingConnComboBox.setToolTipText(bundle.getString("ACS_ExistingConnectionComboBoxA11yDesc"));
        existingConnComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CPVendorPanel.this.existingConnComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 24, 0, 11);
        add(existingConnComboBox, gridBagConstraints);

        newCofigRadioButton.setText(bundle.getString("NewConfiguration"));
        newCofigRadioButton.setToolTipText(bundle.getString("ACS_NewConnectionA11yDesc"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(newCofigRadioButton, gridBagConstraints);

        nameComboBox.setToolTipText(bundle.getString("ACS_NewConnectionComboBoxA11yDesc"));
        nameComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CPVendorPanel.this.nameComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 24, 50, 11);
        add(nameComboBox, gridBagConstraints);

        isXA.setText(bundle.getString("isXA"));
        isXA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CPVendorPanel.this.isXAActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 5, 11);
        add(isXA, gridBagConstraints);

    }//GEN-END:initComponents

    private void nameFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameFieldKeyReleased
        // Add your handling code here:
        ResourceConfigData data = this.helper.getData();
        String value = data.getString(__Name);
        String newValue = nameField.getText();
        if (!value.equals(newValue)) {
            this.helper.getData().setString(__Name, newValue);
        }
        fireChange(this);
    }//GEN-LAST:event_nameFieldKeyReleased

    private void nameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
        // Add your handling code here:
        setResourceName();
    }//GEN-LAST:event_nameFieldActionPerformed
    
    public String getNameField() {
        return nameField.getText();
    }
    
    private void setResourceName() {
        ResourceConfigData data = this.helper.getData();
        String value = data.getString(__Name);
        String newValue = nameField.getText();
        if (!value.equals(newValue)) {
            this.helper.getData().setString(__Name, newValue);
            fireChange(this);
        }
        
        if((this.getRootPane().getDefaultButton() != null) && (this.getRootPane().getDefaultButton().isEnabled())){
            this.getRootPane().getDefaultButton().doClick();
        }
    }
    
    private void isXAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_isXAActionPerformed
        // Add your handling code here:
        setNewConfigData(false); 
    }//GEN-LAST:event_isXAActionPerformed

    private void nameComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameComboBoxActionPerformed
        // Add your handling code here:
        setNewConfigData(true);      
/*           
        usernameTextField.setText(""); //NOI18N
        passwordField.setText(""); //NOI18N
        
        data.setDriver(driverTextField.getText());
        data.setSchema(null);
        schemas = false;
*/       
    }//GEN-LAST:event_nameComboBoxActionPerformed

    private void setNewConfigData(boolean replaceProps) {
        if (firstTime) {
            return;
        }
        int index = nameComboBox.getSelectedIndex();

        if (index > 0) {
            if (useExistingConnection) {
                useExistingConnection = false; 
            }
            ResourceConfigData data = this.helper.getData();
            data.setString(__IsCPExisting, "false"); //NOI18N
            String vendorName = vendors[index - 1];     
            String savedVendorName = data.getString(__DatabaseVendor);
            String savedXA = data.getString(__IsXA);
            String XA = isXA.isSelected()?CONST_TRUE:"false";  //NOI18N
            boolean vendorNotChanged = vendorName.equals(savedVendorName);
            boolean isXANotChanged = XA.equals(savedXA);

            if (vendorNotChanged && isXANotChanged) {
                return;
            }
            if (!vendorNotChanged) {
                data.setString(__DatabaseVendor, vendorName);
            }
            if (!isXANotChanged) {
                data.setString(__IsXA, XA);
            }
            
            setDataSourceClassNameAndResTypeInData(vendorName);
            
            if (replaceProps) {
                setPropertiesInData(vendorName);
            }
        }    
    }
    
    private void setDataSourceClassNameAndResTypeInData(String vendorName) {
        //change datasource classname following database vendor change
        ResourceConfigData data = this.helper.getData();
        Field dsField;
        if (isXA.isSelected())
            dsField = FieldHelper.getField(this.generalGroup, __XADatasourceClassname);
        else
            dsField = FieldHelper.getField(this.generalGroup, __DatasourceClassname);
        data.setString(__DatasourceClassname, FieldHelper.getConditionalFieldValue(dsField, vendorName));
        
        if (isXA.isSelected()) {
            data.setString(__ResType, "javax.sql.XADataSource");  //NOI18N
            data.setString(__IsXA, CONST_TRUE);  //NOI18N
        }else {
            data.setString(__ResType, "javax.sql.DataSource");  //NOI18N
            data.setString(__IsXA, "false");  //NOI18N
        }
    }
    
    private void setPropertiesInData(String vendorName) {
        //change standard properties following database vendor change
        ResourceConfigData data = this.helper.getData();
        data.setProperties(new Vector());
        Field[] propFields = this.propGroup.getField();
        for (int i = 0; i < propFields.length; i++) {
            String value = FieldHelper.getConditionalFieldValue(propFields[i], vendorName);
            String name = propFields[i].getName();
            if (name.equals(__Url) && value.length() > 0)
                data.addProperty(name, FieldHelper.toUrl(value));
            else if (name.equals(__DatabaseName) && value.length() > 0)
                data.addProperty(name, FieldHelper.toUrl(value));
            else if (name.equals(__User) || name.equals(__Password)) {
                data.addProperty(propFields[i].getName(), value);
            }else{
                //All Others
                if(value.length() > 0 && (value.equals(__NotApplicable))){
                    data.addProperty(propFields[i].getName(), ""); //NOI18N
                }
            }
        }
    }
        
    
    private void existingConnComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_existingConnComboBoxActionPerformed
        setExistingConnData();
    }//GEN-LAST:event_existingConnComboBoxActionPerformed
  
    public void setExistingConnData() {
        if(existingConnComboBox.getSelectedIndex() > 0) {
            if (!useExistingConnection) {
                this.helper.getData().setResourceName(__JdbcConnectionPool);
                useExistingConnection = true;  
            }
            this.helper.getData().setString(__IsCPExisting, CONST_TRUE); //NOI18N
            DatabaseConnection dbconn = (DatabaseConnection)dbconns.get(existingConnComboBox.getSelectedIndex() - 1);
            String url = dbconn.getDatabaseURL();
            String user = dbconn.getUser();
            String password = dbconn.getPassword();
            String tmpStr = url;
            
            Field urlField = FieldHelper.getField(this.vendorGroup, "vendorUrls"); //NOI18N
            String vendorName = FieldHelper.getOptionNameFromValue(urlField, tmpStr);
                        
            ResourceConfigData data = this.helper.getData();    
            data.setProperties(new Vector());
            data.setString(__DatabaseVendor, vendorName);
            
            if (vendorName.equals("pointbase")) {  //NOI18N
                data.addProperty(__DatabaseName, dbconn.getDatabaseURL());
            }else if(vendorName.startsWith("derby")) {  //NOI18N)
                setDerbyProps(vendorName, url);
            }else    
                data.addProperty(__Url, url);
            data.addProperty(__User, user);
            data.addProperty(__Password, password);
            
            setDataSourceClassNameAndResTypeInData(vendorName);
        }
           
    }
    
    private void setDerbyProps(String vendorName, String url) {
        //change standard properties following database vendor change
        ResourceConfigData data = this.helper.getData();
        data.setProperties(new Vector());
        data.addProperty(__Url, url);
        Field[] propFields = this.propGroup.getField();
        for (int i = 0; i < propFields.length; i++) {
            String value = FieldHelper.getConditionalFieldValue(propFields[i], vendorName);
            if(value.equals(__NotApplicable)){
                String name = propFields[i].getName();
                if(vendorName.equals("derby_net")) {//NOI18N
                    String hostName = "";
                    String portNumber = "";
                    String databaseName = "";
                    try{
                        String workingUrl = url.substring(url.indexOf("//") + 2, url.length());
                        ResourceConfigurator rci = new ResourceConfigurator();
                        hostName = rci.getDerbyServerName(workingUrl);
                        portNumber = rci.getDerbyPortNo(workingUrl);
                        databaseName = rci.getDerbyDatabaseName(workingUrl);
                    }catch(java.lang.StringIndexOutOfBoundsException ex){
                    }
                    if (name.equals(__DerbyPortNumber)) {
                        data.addProperty(name, portNumber);
                    } else if (name.equals(__DerbyDatabaseName)) {
                        data.addProperty(name, databaseName);
                    } else if (name.equals(__ServerName)) {
                        data.addProperty(name, hostName);
                    }
                }   
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField nameField;
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JComboBox nameComboBox;
    private javax.swing.JRadioButton newCofigRadioButton;
    private javax.swing.JRadioButton existingConnRadioButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox existingConnComboBox;
    private javax.swing.JCheckBox isXA;
    // End of variables declaration//GEN-END:variables

//    private static final ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.Bundle"); //NOI18N
    
    public boolean isValid() {
        if(! setupValid){
            setErrorMsg(bundle.getString("Err_InvalidSetup"));
            return false;
        }
        setErrorMsg(bundle.getString("Empty_String"));
        String name = nameField.getText();
        if (name == null || name.length() == 0){
            setErrorMsg(bundle.getString("Err_InvalidName"));
            return false;
        }else if(! ResourceUtils.isLegalResourceName(name))
            return false;
        else if(! ResourceUtils.isUniqueFileName(name, this.helper.getData().getTargetFileObject(), __ConnectionPoolResource)){
            setErrorMsg(bundle.getString("Err_DuplFileName"));
            return false;
        }
        
        if (existingConnRadioButton.isSelected()) {
            if (existingConnComboBox.getSelectedIndex() > 0)
                return true;
            else
                setErrorMsg(bundle.getString("Err_ChooseDBConn"));
        }else if (newCofigRadioButton.isSelected()) {
            if (nameComboBox.getSelectedIndex() > 0)
                return true;
            else
                setErrorMsg(bundle.getString("Err_ChooseDBVendor"));
        } 
        
        return false;
    }

    public void removeUpdate(final javax.swing.event.DocumentEvent event) {
        fireChange(this);
    }
    
    public void changedUpdate(final javax.swing.event.DocumentEvent event) {
        fireChange(this);
    }
    
    public void insertUpdate(final javax.swing.event.DocumentEvent event) {
        fireChange(this);
    }

    public void intervalAdded(final javax.swing.event.ListDataEvent p1) {
        fireChange(this);
    }
    
    public void intervalRemoved(final javax.swing.event.ListDataEvent p1) {
        fireChange(this);
    }
    
    public void contentsChanged(final javax.swing.event.ListDataEvent p1) {
        fireChange(this);
    }

    public void stateChanged(final javax.swing.event.ChangeEvent p1) {
        if (firstTime) {
            return;
        }
        if (p1.getSource().getClass() == javax.swing.JToggleButton.ToggleButtonModel.class) {
            if (existingConnRadioButton.isSelected()) {
                //To solve a problem on Win2K only
                if (firstTime) {
                    return;
                }
                existingConnComboBox.setEnabled(true);
                nameComboBox.setEnabled(false);
//                isXA.setEnabled(false);
                setExistingConnData();
            } else {
                existingConnComboBox.setEnabled(false);
                nameComboBox.setEnabled(true);
                setNewConfigData(true);
            }  
        }
        fireChange(this);
    }
    
    public CPVendorPanel setFirstTime(boolean first) {
        this.firstTime = first;
        return this;
    }

    protected void initData() {
        /*if (existingConnRadioButton.isSelected()) {
            data.setExistingConn(true);
            if(existingConnComboBox.getSelectedIndex() > 0)
                data.setConnectionNodeInfo((ConnectionNodeInfo) connInfos.get(existingConnComboBox.getSelectedIndex() - 1));
            
            data.setDriver(null);
            data.setUrl(null);
            data.setUsername(null);
            data.setPassword(null);
        } else {
            data.setExistingConn(false);
            data.setDriver(driverTextField.getText());
            data.setUrl(urlTextField.getText());
            data.setUsername(usernameTextField.getText());
            data.setPassword(new String(passwordField.getPassword()));

            data.setConnectionNodeInfo(null);
        }*/
    }
    
    public HelpCtx getHelp() {
         return new HelpCtx("AS_Wiz_ConnPool_chooseDB"); //NOI18N
    }
    
    public void readSettings(Object settings) {
        this.wizDescriptor = (WizardDescriptor)settings;
        TemplateWizard wizard = (TemplateWizard)settings;
        String targetName = wizard.getTargetName();
        if(this.helper.getData().getString(__DynamicWizPanel).equals(CONST_TRUE)){ //NOI18N
            targetName = null;
        }  
        FileObject setupFolder = ResourceUtils.getResourceDirectory(this.helper.getData().getTargetFileObject());
        this.helper.getData().setTargetFileObject (setupFolder);
        if(setupFolder != null){
            targetName = ResourceUtils.createUniqueFileName (targetName, setupFolder, __ConnectionPoolResource);
            this.nameField.setText (targetName);
            this.helper.getData ().setString (__Name, targetName);
            this.helper.getData ().setTargetFile (targetName);
        }else
            setupValid = false;
    }
    
    public void setInitialFocus(){
        new setFocus(nameField);
    }
    
//    private boolean setupValid(){
//        return setupValid;
//    }
}
