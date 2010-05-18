/*
 * DBBindingConfigurationPanel.java
 *
 * Created on August 11, 2008, 8:14 PM
 */
package org.netbeans.modules.wsdlextensions.jdbc.configeditor.panels;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.xml.namespace.QName;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCAddress;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCBinding;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCOperationInput;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCOperationOutput;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCOperation;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.openide.util.NbBundle;

/**
 *
 * @author  Naveen K
 */
public class DBBindingConfigurationPanel extends javax.swing.JPanel {


    /** Creates new form DBBindingConfigurationPanel */
    public DBBindingConfigurationPanel(QName qName,
            WSDLComponent component) {
        initComponents();
        resetView();
        initActionListeners();
        localizeText();
        populateView(qName, component);
        this.mComponent = component;
        this.mQName = qName;
    }

    private JDBCOperationInput getInputJDBCOperationInput(Binding binding, String selectedOperation) {
        JDBCOperationInput jdbcInput = null;
        if (binding != null) {
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(selectedOperation)) {
                    BindingInput bi = bop.getBindingInput();
                    List<JDBCOperationInput> jdbcInputs =
                            bi.getExtensibilityElements(JDBCOperationInput.class);
                    if (jdbcInputs.size() > 0) {
                        jdbcInput = jdbcInputs.get(0);
                        break;
                    }
                }
            }
        }
        return jdbcInput;
    }

    private void initActionListeners() {
        bindingNameComboBox = new JComboBox();
        portTypeComboBox = new JComboBox();
        servicePortComboBox = new JComboBox();
        operationNameComboBox = new JComboBox();

        bindingNameComboBox.setModel(new javax.swing.DefaultComboBoxModel());
        bindingNameComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                bindingNameComboBoxItemStateChanged(evt);
            }
        });

        portTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel());

        servicePortComboBox.setModel(new javax.swing.DefaultComboBoxModel());
        servicePortComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                servicePortComboBoxItemStateChanged(evt);
            }
        });

        operationNameComboBox.setModel(new javax.swing.DefaultComboBoxModel());
        operationNameComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                operationNameComboBoxItemStateChanged(evt);
            }
        });

        resConfigRadioButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                resConfigRadioButtonChangeActionPerformed(evt);
            }

            private void resConfigRadioButtonChangeActionPerformed(ChangeEvent evt) {
                jndiNameTextField.setEnabled(resConfigRadioButton.isSelected());
            }
        });

    }

    private void localizeText() {
        //Tooltips
        noRecordsTextField.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.noRecordsTextField.toolTipText")); // NOI18N
        tableNameTextField.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.tableNameTextField.toolTipText")); // NOI18N
        pollConfigCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.pollConfigCheckBox.toolTipText")); // NOI18N
        markColumnValueTextField.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.markColumnValueTextField.toolTipText")); // NOI18N
        postPollProcessingComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.postPollProcessingComboBox.toolTipText")); // NOI18N
        pollIntervalTextField.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.pollIntervalTextField.toolTipText")); // NOI18N
        markColNameTextField.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.markColNameTextField.toolTipText")); // NOI18N
        moveRowtoTabTextField.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.moveRowtoTabTextField.toolTipText")); // NOI18N
        primaryKeyNameTextField.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.primaryKeyNameTextField.toolTipText")); // NOI18N
        sqlStmtTextArea.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.sqlStmtTextArea.toolTipText")); // NOI18N
        paramOrderTextField.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.paramOrderTextField.toolTipText")); // NOI18N
        jndiNameTextField.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.jndiNameTextField.toolTipText")); // NOI18N
        resConfigRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.resConfigRadioButton.toolTipText")); // NOI18N
        transactionComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.transactionComboBox.toolTipText")); // NOI18N

        //508 Compliance
        descLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.descLabel.AccessibleContext.accessibleName")); // NOI18N
        descLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.descLabel.AccessibleContext.accessibleDescription")); // NOI18N
        userFocusedDescTextPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.userFocusedDescTextPane.AccessibleContext.accessibleName")); // NOI18N
        userFocusedDescTextPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.userFocusedDescTextPane.AccessibleContext.accessibleDescription")); // NOI18N
        markColumnValueTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.markColumnValueTextField.AccessibleContext.accessibleName_1")); // NOI18N
        markColumnValueTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.markColumnValueTextField.AccessibleContext.accessibleDescription")); // NOI18N
        postPollProcessingComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.postPollProcessingComboBox.AccessibleContext.accessibleName_1")); // NOI18N
        postPollProcessingComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.postPollProcessingComboBox.AccessibleContext.accessibleDescription_1")); // NOI18N
        pollIntervalTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.pollIntervalTextField.AccessibleContext.accessibleName_1")); // NOI18N
        pollIntervalTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.pollIntervalTextField.AccessibleContext.accessibleDescription")); // NOI18N
        markColNameTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.markColNameTextField.AccessibleContext.accessibleName_1")); // NOI18N
        markColNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.markColNameTextField.AccessibleContext.accessibleDescription_1")); // NOI18N
        moveRowtoTabTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.moveRowtoTabTextField.AccessibleContext.accessibleName_1")); // NOI18N
        moveRowtoTabTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.moveRowtoTabTextField.AccessibleContext.accessibleDescription_1")); // NOI18N
        primaryKeyNameTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.primaryKeyNameTextField.AccessibleContext.accessibleName_1")); // NOI18N
        primaryKeyNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.primaryKeyNameTextField.AccessibleContext.accessibleDescription_1")); // NOI18N
        sqlStmtTextArea.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.sqlStmtTextArea.AccessibleContext.accessibleName")); // NOI18N
        sqlStmtTextArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.sqlStmtTextArea.AccessibleContext.accessibleDescription")); // NOI18N
        noRecordsTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.noRecordsTextField.AccessibleContext.accessibleName")); // NOI18N
        noRecordsTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.noRecordsTextField.AccessibleContext.accessibleDescription")); // NOI18N
        tableNameTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.tableNameTextField.AccessibleContext.accessibleName")); // NOI18N
        pollConfigCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.pollConfigCheckBox.AccessibleContext.accessibleName")); // NOI18N
        pollConfigCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.pollConfigCheckBox.toolTipText")); // NOI18N
        paramOrderTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.paramOrderTextField.AccessibleContext.accessibleName")); // NOI18N
        paramOrderTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.paramOrderTextField.toolTipText")); // NOI18N
        qPropPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.qPropPanel.AccessibleContext.accessibleName")); // NOI18N
        qPropPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.qPropPanel.AccessibleContext.accessibleDescription")); // NOI18N
        driverConfigRadioButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.driverConfigRadioButton.AccessibleContext.accessibleName")); // NOI18N
        driverConfigRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DESC_Attribute_urlConfig")); // NOI18N
        jndiNameTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.jndiNameTextField.AccessibleContext.accessibleName_1")); // NOI18N
        resConfigRadioButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.resConfigRadioButton.AccessibleContext.accessibleName")); // NOI18N
        resConfigRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.resConfigRadioButton.AccessibleContext.accessibleDescription_1")); // NOI18N
        transactionComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.transactionComboBox.AccessibleContext.accessibleName_1")); // NOI18N
        transactionComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.transactionComboBox.AccessibleContext.accessibleDescription_1")); // NOI18N
        connectionComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.connectionComboBox.AccessibleContext.accessibleName")); // NOI18N
        connectionComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.connectionComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        serviceConfigPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.serviceConfigPanel.AccessibleContext.accessibleName")); // NOI18N
        serviceConfigPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.serviceConfigPanel.AccessibleContext.accessibleDescription")); // NOI18N
        userFocusedDescTextPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.userFocusedDescTextPane.AccessibleContext.accessibleName")); // NOI18N
        userFocusedDescTextPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.userFocusedDescTextPane.AccessibleContext.accessibleDescription")); // NOI18N

        //mnemonics
        org.openide.awt.Mnemonics.setLocalizedText(descLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.descLabel.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(noRecordsdLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.noRecordsdLabel.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(tableNameLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.tableNameLabel.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(queryPropsLbl, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.queryPropsLbl.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(pollConfigCheckBox, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.pollConfigCheckBox.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(markColumnValLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.markColumnValLabel.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(moveRowtoTabLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.moveRowtoTabLabel.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(markColNameLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.markColNameLabel.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(pollIntervalLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.pollIntervalLabel.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(postPollProcessingLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.postPollProcessingLabel.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(primaryKeyNameLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.primaryKeyNameLabel.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(sqlStmtLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.sqlStmtLabel.text")+ getOperation());
        org.openide.awt.Mnemonics.setLocalizedText(paramOrderLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.paramOrderLabel.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(driverConfigRadioButton, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.driverConfigRadioButton.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(serviceConfigLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.serviceConfigLabel.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jndiNameLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.jndiNameLabel.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(resConfigRadioButton, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.resConfigRadioButton.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(transactionPropsLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.transactionPropsLabel.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(transactionLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.transactionLabel.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(connectionURLLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.connectionURLLabel.text")); // NOI18N
    }

    private void populateDescriptionAndTooltip() {
    }

    public void resetView() {
        tableNameTextField.setText("");
        noRecordsTextField.setText("");
        sqlStmtTextArea.setText("");
        paramOrderTextField.setText("");
        jndiNameTextField.setText("");
        markColNameTextField.setText("");
        markColumnValueTextField.setText("");
        moveRowtoTabTextField.setText("");
        pollIntervalTextField.setText("");
        primaryKeyNameTextField.setText("");
        userFocusedDescTextPane.setText("");
    }

    /**
     * Trims input and returns null, if blank.
     *
     * @param text
     * @return trimmed text, if blank returns null.
     */
    private String trimTextFieldInput(String text) {
        if (text == null) {
            return text;
        }
        String trimmedText = text.trim();
        if (trimmedText.length() == 0) {
            return null;
        }
        return text.trim();
    }

    public String getTableName() {
        return trimTextFieldInput(tableNameTextField.getText());
    }

    public void setTableName(String tname) {
        tableNameTextField.setText(tname);
    }

    public int getNoOfRecords() {
        return Integer.parseInt(noRecordsTextField.getText());
    }

    public void setNoOfRecords(int nRec) {
        noRecordsTextField.setText(nRec + "");
    }

    public String getTransaction() {
        return (String) transactionComboBox.getSelectedItem();
    }

    public void setTransaction(String tran) {
        transactionComboBox.setSelectedItem(tran);
    }

    public String getPKName() {
        return trimTextFieldInput(primaryKeyNameTextField.getText());
    }

    public void setPKName(String pk) {
        primaryKeyNameTextField.setText(pk);
    }

    public String getMarkColumnName() {
        return markColNameTextField.getText();
    }

    public void setMarkColumnName(String mName) {
        markColNameTextField.setText(mName);
    }

    public String getMarkColumnValue() {
        return markColumnValueTextField.getText();
    }

    public void setMarkColumnValue(String mName) {
        markColumnValueTextField.setText(mName);
    }

    public String getMoveRowToTableName() {
        return moveRowtoTabTextField.getText();
    }

    public void setMoveRowToTableName(String mName) {
        moveRowtoTabTextField.setText(mName);
    }

    public void setPollInterval(long pollInt) {
        pollIntervalTextField.setText(pollInt + "");
    }

    public long getPollInterval() {
        return Long.parseLong(pollIntervalTextField.getText());
    }

    public void setMarkColumnName(long mName) {
        markColNameTextField.setText(mName + "");
    }

    public String getPollingPostProcessing() {
        return (String) postPollProcessingComboBox.getSelectedItem();
    }

    public void setPollingPostProcessing(String mName) {
        postPollProcessingComboBox.setSelectedItem(mName);
    }

    public String getJNDIName() {
        return trimTextFieldInput(jndiNameTextField.getText());
    }

    public void setJNDIName(String mName) {
        jndiNameTextField.setText(mName);
    }

    public void setOperation(String op) {
        this.operationType = op;
    }

    public String getOperation() {
        return this.operationType;
    }

    public void setSQLStatement(String stmt) {
        sqlStmtTextArea.setText(stmt);
        //this.sqlStmt = stmt;
    }

    public String getSQLStatement() {
       // return this.sqlStmt;
        return sqlStmtTextArea.getText();
    }

    public void setParamOrder(String parOrd) {
        paramOrderTextField.setText(parOrd);
        //this.paramOrder = parOrd;
    }

    public String getParamOrder() {
        //return this.paramOrder;
        return paramOrderTextField.getText();
    }

    /**
     *
     * @param qname
     * @param component
     */
    public void populateView(QName qname, WSDLComponent component) {
        cleanUp();
        this.mQName = qname;
        this.mComponent = component;
        populateView(component);
    }

    private void populateView(WSDLComponent component) {
        if (component != null) {
            if (component instanceof JDBCAddress) {
                populateJDBCAddress((JDBCAddress) component);
            } else if (component instanceof JDBCBinding) {
                populateJDBCBinding((JDBCBinding) component, null);
            } else if (component instanceof Port) {
                Collection<JDBCAddress> address = ((Port) component).getExtensibilityElements(JDBCAddress.class);
                if (!address.isEmpty()) {
                    populateJDBCAddress(address.iterator().next());
                }
            } else if (component instanceof JDBCOperationInput) {
                Object obj = ((JDBCOperationInput) component).getParent();
                Binding parentBinding = null;
                if (obj instanceof BindingInput) {
                    BindingOperation parentOp =
                            (BindingOperation) ((BindingInput) obj).getParent();
                    parentBinding = (Binding) parentOp.getParent();
                }
                if (parentBinding != null) {
                    Collection<JDBCBinding> bindings = parentBinding.getExtensibilityElements(JDBCBinding.class);
                    if (!bindings.isEmpty()) {
                        populateJDBCBinding(bindings.iterator().next(), null);
                        operationType = parentBinding.getName();
                    }
                }
            } else if (component instanceof JDBCOperationOutput) {
            } else if (component instanceof JDBCOperation) {
                Object obj = ((JDBCOperation) component).getParent();
                if (obj instanceof BindingOperation) {
                    Binding parentBinding = (Binding) ((BindingOperation) obj).getParent();
                    Collection<JDBCBinding> bindings = parentBinding.getExtensibilityElements(JDBCBinding.class);
                    if (!bindings.isEmpty()) {
                        populateJDBCBinding(bindings.iterator().next(), null);
                        operationType = parentBinding.getName();
                    }
                }
            }
            populateDescriptionAndTooltip();
        }
    }

    private void populateJDBCAddress(JDBCAddress jdbcAddress) {
        setJNDIName(jdbcAddress.getJDBCURL());
        Port port = (Port) jdbcAddress.getParent();
        if (port.getBinding() != null) {
            Binding binding = port.getBinding().get();
            Collection<JDBCBinding> bindings = binding.getExtensibilityElements(JDBCBinding.class);
            servicePortComboBox.setEnabled(false);
            servicePortComboBox.setSelectedItem(port.getName());
            if (!bindings.isEmpty()) {
                populateJDBCBinding(bindings.iterator().next(), jdbcAddress);
                bindingNameComboBox.setSelectedItem(binding.getName());
            }
            // from Port, need to disable binding box as 1:1 relationship
            bindingNameComboBox.setEditable(false);
            bindingNameComboBox.setEnabled(false);
        }
    }

    private void populateJDBCBindingOperationInput(JDBCOperationInput jdbcOperationInput) {
        setMarkColumnName(jdbcOperationInput.getMarkColumnName());
        setMarkColumnValue(jdbcOperationInput.getMarkColumnValue());
        setMoveRowToTableName(jdbcOperationInput.getMoveRowToTableName());
        setNoOfRecords(jdbcOperationInput.getNumberOfRecords());
        setPollingPostProcessing(jdbcOperationInput.getPollingPostProcessing());
        setTableName(jdbcOperationInput.getTableName());
        setTransaction(jdbcOperationInput.getTransaction());
        setPKName(jdbcOperationInput.getPKName());
        setOperation(jdbcOperationInput.getOperationType());
        if ((jdbcOperationInput.getOperationType()).equals("poll")) {
            pollConfigCheckBox.setSelected(true);
	}
        setSQLStatement(jdbcOperationInput.getSql());
        setParamOrder(jdbcOperationInput.getParamOrder());
    }

    private void populateJDBCBinding(JDBCBinding jdbcBinding, JDBCAddress jdbcAddress) {
        if (jdbcAddress == null) {
            servicePortComboBox.setEnabled(true);
            jdbcAddress = getJDBCAddress(jdbcBinding);
        }
        if (jdbcAddress == null) {
            return;
        }
        Port port = (Port) jdbcAddress.getParent();

        // need to populate with all service ports that uses this binding
        populateListOfPorts(jdbcBinding);
        servicePortComboBox.setSelectedItem(port);
        updateServiceView(jdbcAddress);

        // from Binding, need to allow changing of Port
        bindingNameComboBox.setEditable(false);
        bindingNameComboBox.setEnabled(false);

        if (jdbcBinding != null) {
            populateListOfBindings(jdbcBinding);
            populateListOfPortTypes(jdbcBinding);
            Binding binding = (Binding) jdbcBinding.getParent();
            bindingNameComboBox.setSelectedItem(binding.getName());
            NamedComponentReference<PortType> pType = binding.getType();
            PortType portType = pType.get();
            portTypeComboBox.setSelectedItem(portType.getName());
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            populateOperations(bindingOperations);

            operationNameComboBox.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent evt) {
                    // based on selected operation, populate messages
                    operationNameComboBoxItemStateChanged(evt);
                }
            });

            // select the 1st item since this is not a configurable param
            //if(operationNameComboBox.iss)
            //operationNameComboBox.setSelectedIndex(0);
            //if (operationNameComboBox.getItemCount() == 1) {
                // need to implicitly call update on messages because above
                // listener will not change selection if only 1 item
                if (binding != null) {
                    JDBCOperationInput jdbcInput = getInputJDBCOperationInput(binding,
                            operationNameComboBox.getSelectedItem().toString());
                    populateJDBCBindingOperationInput(jdbcInput);
                //updateInputMessageView(inputMessage);
                }
           // }
        }
    }

    private void populateListOfPorts(JDBCBinding jdbcBinding) {
        Vector<Port> portV = new Vector<Port>();

        if ((jdbcBinding != null) && (jdbcBinding.getParent() != null)) {
            Binding parentBinding = (Binding) jdbcBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<Service> services = defs.getServices().iterator();
            String bindingName = parentBinding.getName();
            while (services.hasNext()) {
                Iterator<Port> ports = services.next().getPorts().iterator();
                while (ports.hasNext()) {
                    Port port = ports.next();
                    if (port.getBinding() != null) {
                        Binding binding = port.getBinding().get();

                        if ((binding != null) && (binding.getName().
                                equals(bindingName))) {
                            portV.add(port);
                        }
                    }
                }
            }
        }
        servicePortComboBox.setModel(new DefaultComboBoxModel(portV));
        servicePortComboBox.setRenderer(new PortCellRenderer());

    }

    private void populateListOfPortTypes(JDBCBinding jdbcBinding) {
        if ((jdbcBinding != null) && (jdbcBinding.getParent() != null)) {
            Binding parentBinding = (Binding) jdbcBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<PortType> portTypes = defs.getPortTypes().iterator();
            List<PortType> jdbcPortTypes = null;
            while (portTypes.hasNext()) {
                PortType portType = portTypes.next();
                portTypeComboBox.addItem(portType.getName());
            }
        }
    }

    private boolean portTypeExists(String portTypeName) {
        boolean exists = false;
        //implement
        return exists;

    }

    private PortType getPortType(String bindingName, JDBCAddress jdbcAddress) {

        if ((jdbcAddress != null) && (jdbcAddress.getParent() != null)) {
            Port parentPort = (Port) jdbcAddress.getParent();
            Service parentService = (Service) parentPort.getParent();
            Definitions defs = (Definitions) parentService.getParent();
            Iterator<Binding> bindings = defs.getBindings().iterator();
            List<JDBCBinding> ldapBindings = null;
            while (bindings.hasNext()) {
                Binding binding = bindings.next();
                if (binding.getType() == null
                        || binding.getType().get() == null) {
                    continue;
                }
                NamedComponentReference<PortType> portType = binding.getType();
                if (binding.getName().equals(bindingName)) {
                    return portType.get();
                }
            }
        }
        return null;
    }

    private void populateListOfBindings(JDBCBinding jdbcBinding) {
        if ((jdbcBinding != null) && (jdbcBinding.getParent() != null)) {
            Binding parentBinding = (Binding) jdbcBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<Binding> bindings = defs.getBindings().iterator();
            List<JDBCBinding> jdbcBindings = null;

            while (bindings.hasNext()) {
                Binding binding = bindings.next();
                if (binding.getType() == null || binding.getType().get() == null) {
                    continue;
                }

                jdbcBindings = binding.getExtensibilityElements(JDBCBinding.class);
                if (jdbcBindings != null) {
                    Iterator iter = jdbcBindings.iterator();
                    while (iter.hasNext()) {
                        JDBCBinding b = (JDBCBinding) iter.next();
                        Binding fBinding = (Binding) b.getParent();
                        bindingNameComboBox.addItem(fBinding.getName());
                    }
                }
            }
        }
    }

    private void populateOperations(Collection bindingOps) {
        Iterator iter = bindingOps.iterator();
        while (iter.hasNext()) {
            BindingOperation bop = (BindingOperation) iter.next();
            operationNameComboBox.addItem(bop.getName());
        }
    }

    /**
     * Return the service port
     * @return String service port
     */
    Port getServicePort() {
        return (Port) servicePortComboBox.getSelectedItem();
    }

    /**
     * Return the binding name used
     * @return String binding name
     */
    String getBinding() {
        if ((bindingNameComboBox.getSelectedItem() != null) &&
                (!bindingNameComboBox.getSelectedItem().toString().
                equals("<Not Set>"))) {
            return bindingNameComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    /**
     * Return the port type used
     * @return String port type name
     */
    String getPortType() {
        if ((portTypeComboBox.getSelectedItem() != null) &&
                (!portTypeComboBox.getSelectedItem().toString().
                equals("<Not Set>"))) {
            return portTypeComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    /**
     * Return the operation name
     * @return String operation name
     */
    public String getOperationName() {
        if ((operationNameComboBox.getSelectedItem() != null) &&
                (!operationNameComboBox.getSelectedItem().toString().
                equals("<Not Set>"))) {
            return operationNameComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    private void resetPanel() {
        tableNameTextField.setText("");
        noRecordsTextField.setText("1");
        primaryKeyNameTextField.setText("");
    }

    private JDBCAddress getJDBCAddress(JDBCBinding jdbcBinding) {
        JDBCAddress jdbcAddress = null;
        if ((jdbcBinding != null) && (jdbcBinding.getParent() != null)) {
            Binding parentBinding = (Binding) jdbcBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<Service> services = defs.getServices().iterator();
            String bindingName = parentBinding.getName();
            while (services.hasNext()) {
                Iterator<Port> ports = services.next().getPorts().iterator();
                while (ports.hasNext()) {
                    Port port = ports.next();
                    if (port.getBinding() != null) {
                        Binding binding = port.getBinding().get();

                        if ((binding != null) && (binding.getName().
                                equals(bindingName))) {
                            Iterator<JDBCAddress> jdbcAddresses = port.getExtensibilityElements(JDBCAddress.class).
                                    iterator();
                            // 1 jdbcaddress for 1 binding
                            while (jdbcAddresses.hasNext()) {
                                return jdbcAddresses.next();
                            }
                        }
                    }
                }
            }
        }
        return jdbcAddress;
    }

    private void updateInputMessageView(JDBCOperationInput jdbcInput) {
    }

    private void updateServiceView(JDBCAddress jdbcAddress) {
        if (jdbcAddress != null) {
            setJNDIName(jdbcAddress.getJDBCURL());
        }
    }

    public void initDataSourceCombo() {
        providers = new DefaultComboBoxModel();
        //int longinx = 0, longest = 0;
        final DatabaseConnection[] conns = ConnectionManager.getDefault().getConnections();
        providers.addElement("<Select an Item from the list>");
        if (conns.length == 1) {
            providers.addElement("");
        }
        if (conns.length > 0) {
            for (int i = 0; i < conns.length; i++) {
                providers.addElement(new ConnectionWrapper(conns[i]));

            }
        } else {
            providers.addElement("<Configure connections in the services tab>");
        }

        this.connectionComboBox.setModel(providers);
        this.connectionComboBox.setSelectedIndex(0);
    //this.datasourceComboBox.setPrototypeDisplayValue((new ConnectionWrapper(conns[longinx])).getDatabaseConnection().getDisplayName());

    }

    private static class ConnectionWrapper {

        private DatabaseConnection conn;

        ConnectionWrapper(final DatabaseConnection conn) {
            this.conn = conn;
        }

        public DatabaseConnection getDatabaseConnection() {
            return this.conn;
        }

        public String toString() {
            return this.conn.getDisplayName();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        bindingPropertiesTabbedPane = new javax.swing.JTabbedPane();
        qPropPanel = new javax.swing.JPanel();
        queryPropsLbl = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        queryPropsPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        sqlStmtTextArea = new javax.swing.JTextArea();
        noRecordsdLabel = new javax.swing.JLabel();
        paramOrderTextField = new javax.swing.JTextField();
        noRecordsTextField = new javax.swing.JTextField();
        sqlStmtLabel = new javax.swing.JLabel();
        sqlStmtLabel.setText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.sqlStmtLabel.text").concat(getOperation()));
        paramOrderLabel = new javax.swing.JLabel();
        tableNameTextField = new javax.swing.JTextField();
        tableNameLabel = new javax.swing.JLabel();
        pollConfigCheckBox = new javax.swing.JCheckBox();
        pollConfigSeparator = new javax.swing.JSeparator();
        pollConfigPanel = new javax.swing.JPanel();
        markColumnValLabel = new javax.swing.JLabel();
        moveRowtoTabLabel = new javax.swing.JLabel();
        markColNameLabel = new javax.swing.JLabel();
        pollIntervalLabel = new javax.swing.JLabel();
        postPollProcessingLabel = new javax.swing.JLabel();
        markColumnValueTextField = new javax.swing.JTextField();
        postPollProcessingComboBox = new javax.swing.JComboBox();
        pollIntervalTextField = new javax.swing.JTextField();
        markColNameTextField = new javax.swing.JTextField();
        moveRowtoTabTextField = new javax.swing.JTextField();
        primaryKeyNameLabel = new javax.swing.JLabel();
        primaryKeyNameTextField = new javax.swing.JTextField();
        serviceConfigPanel = new javax.swing.JPanel();
        transactionPropsLabel = new javax.swing.JLabel();
        transactionPropsSeparator = new javax.swing.JSeparator();
        transactionLabel = new javax.swing.JLabel();
        transactionComboBox = new javax.swing.JComboBox();
        serviceConfigLabel = new javax.swing.JLabel();
        serviceConfigSeparator = new javax.swing.JSeparator();
        resConfigRadioButton = new javax.swing.JRadioButton();
        jndiNameLabel = new javax.swing.JLabel();
        jndiNameTextField = new javax.swing.JTextField();
        driverConfigRadioButton = new javax.swing.JRadioButton();
        connectionURLLabel = new javax.swing.JLabel();
        connectionComboBox = new javax.swing.JComboBox();
        descLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        userFocusedDescTextPane = new javax.swing.JTextPane();
        mDoc = userFocusedDescTextPane.getStyledDocument();
        mStyles = new String[]{"bold", "regular"};

        setMinimumSize(new java.awt.Dimension(600, 600));
        setPreferredSize(new java.awt.Dimension(600, 600));

        bindingPropertiesTabbedPane.setMinimumSize(new java.awt.Dimension(100, 100));
        bindingPropertiesTabbedPane.setPreferredSize(new java.awt.Dimension(605, 505));

        qPropPanel.setMinimumSize(new java.awt.Dimension(600, 500));
        qPropPanel.setPreferredSize(new java.awt.Dimension(600, 500));

        queryPropsLbl.setFont(queryPropsLbl.getFont().deriveFont(queryPropsLbl.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(queryPropsLbl, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.queryPropsLbl.text")); // NOI18N

        sqlStmtTextArea.setColumns(20);
        sqlStmtTextArea.setRows(5);
        sqlStmtTextArea.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.sqlStmtTextArea.toolTipText")); // NOI18N
        sqlStmtTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                sqlStmtTextAreaFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                sqlStmtTextAreaFocusLost(evt);
            }
        });
        jScrollPane3.setViewportView(sqlStmtTextArea);
        sqlStmtTextArea.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.sqlStmtTextArea.AccessibleContext.accessibleName")); // NOI18N
        sqlStmtTextArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.sqlStmtTextArea.AccessibleContext.accessibleDescription")); // NOI18N

        noRecordsdLabel.setFont(noRecordsdLabel.getFont());
        noRecordsdLabel.setLabelFor(noRecordsTextField);
        org.openide.awt.Mnemonics.setLocalizedText(noRecordsdLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.noRecordsdLabel.text")); // NOI18N

        paramOrderTextField.setText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.paramOrderTextField.text")); // NOI18N
        paramOrderTextField.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.paramOrderTextField.toolTipText")); // NOI18N
        paramOrderTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                paramOrderTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                paramOrderTextFieldFocusLost(evt);
            }
        });

        noRecordsTextField.setFont(noRecordsTextField.getFont());
        noRecordsTextField.setText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.noRecordsTextField.text")); // NOI18N
        noRecordsTextField.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.noRecordsTextField.toolTipText")); // NOI18N
        noRecordsTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                noRecordsTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                noRecordsTextFieldFocusLost(evt);
            }
        });

        sqlStmtLabel.setLabelFor(sqlStmtTextArea);
        org.openide.awt.Mnemonics.setLocalizedText(sqlStmtLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.sqlStmtLabel.text")+ getOperation());

        paramOrderLabel.setLabelFor(paramOrderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(paramOrderLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.paramOrderLabel.text")); // NOI18N

        tableNameTextField.setText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.tableNameTextField.text")); // NOI18N
        tableNameTextField.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.tableNameTextField.toolTipText")); // NOI18N
        tableNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tableNameTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tableNameTextFieldFocusLost(evt);
            }
        });

        tableNameLabel.setLabelFor(tableNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(tableNameLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.tableNameLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout queryPropsPanelLayout = new org.jdesktop.layout.GroupLayout(queryPropsPanel);
        queryPropsPanel.setLayout(queryPropsPanelLayout);
        queryPropsPanelLayout.setHorizontalGroup(
            queryPropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(queryPropsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(queryPropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(noRecordsdLabel)
                    .add(sqlStmtLabel)
                    .add(tableNameLabel)
                    .add(paramOrderLabel))
                .add(41, 41, 41)
                .add(queryPropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(paramOrderTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
                    .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
                    .add(noRecordsTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
                    .add(tableNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE))
                .addContainerGap())
        );
        queryPropsPanelLayout.setVerticalGroup(
            queryPropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(queryPropsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(queryPropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tableNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(tableNameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(queryPropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(noRecordsdLabel)
                    .add(noRecordsTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(queryPropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(sqlStmtLabel)
                    .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 76, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(queryPropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(paramOrderLabel)
                    .add(paramOrderTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        paramOrderTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.paramOrderTextField.AccessibleContext.accessibleName")); // NOI18N
        paramOrderTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.paramOrderTextField.toolTipText")); // NOI18N
        noRecordsTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.noRecordsTextField.AccessibleContext.accessibleName")); // NOI18N
        noRecordsTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.noRecordsTextField.AccessibleContext.accessibleDescription")); // NOI18N
        tableNameTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.tableNameTextField.AccessibleContext.accessibleName")); // NOI18N

        pollConfigCheckBox.setFont(pollConfigCheckBox.getFont().deriveFont(pollConfigCheckBox.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(pollConfigCheckBox, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.pollConfigCheckBox.text")); // NOI18N
        pollConfigCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.pollConfigCheckBox.toolTipText")); // NOI18N
        pollConfigCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                pollConfigCheckBoxStateChanged(evt);
            }
        });
        pollConfigCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pollConfigCheckBoxActionPerformed(evt);
            }
        });
        pollConfigCheckBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                pollConfigCheckBoxFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                pollConfigCheckBoxFocusLost(evt);
            }
        });

        markColumnValLabel.setLabelFor(markColumnValueTextField);
        org.openide.awt.Mnemonics.setLocalizedText(markColumnValLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.markColumnValLabel.text")); // NOI18N

        moveRowtoTabLabel.setLabelFor(moveRowtoTabTextField);
        org.openide.awt.Mnemonics.setLocalizedText(moveRowtoTabLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.moveRowtoTabLabel.text")); // NOI18N

        markColNameLabel.setLabelFor(markColNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(markColNameLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.markColNameLabel.text")); // NOI18N

        pollIntervalLabel.setLabelFor(pollIntervalTextField);
        org.openide.awt.Mnemonics.setLocalizedText(pollIntervalLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.pollIntervalLabel.text")); // NOI18N

        postPollProcessingLabel.setLabelFor(postPollProcessingComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(postPollProcessingLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.postPollProcessingLabel.text")); // NOI18N

        markColumnValueTextField.setText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.markColumnValueTextField.text")); // NOI18N
        markColumnValueTextField.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.markColumnValueTextField.toolTipText")); // NOI18N
        markColumnValueTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                markColumnValueTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                markColumnValueTextFieldFocusLost(evt);
            }
        });

        postPollProcessingComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<Not set>", "Delete", "MarkColumn", "MoveRow", "CopyRow" }));
        postPollProcessingComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.postPollProcessingComboBox.toolTipText")); // NOI18N
        postPollProcessingComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                postPollProcessingComboBoxFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                postPollProcessingComboBoxFocusLost(evt);
            }
        });

        pollIntervalTextField.setText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.pollIntervalTextField.text")); // NOI18N
        pollIntervalTextField.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.pollIntervalTextField.toolTipText")); // NOI18N
        pollIntervalTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                pollIntervalTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                pollIntervalTextFieldFocusLost(evt);
            }
        });

        markColNameTextField.setText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.markColNameTextField.text")); // NOI18N
        markColNameTextField.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.markColNameTextField.toolTipText")); // NOI18N
        markColNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                markColNameTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                markColNameTextFieldFocusLost(evt);
            }
        });

        moveRowtoTabTextField.setText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.moveRowtoTabTextField.text")); // NOI18N
        moveRowtoTabTextField.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.moveRowtoTabTextField.toolTipText")); // NOI18N
        moveRowtoTabTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                moveRowtoTabTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                moveRowtoTabTextFieldFocusLost(evt);
            }
        });

        primaryKeyNameLabel.setFont(primaryKeyNameLabel.getFont());
        primaryKeyNameLabel.setLabelFor(primaryKeyNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(primaryKeyNameLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.primaryKeyNameLabel.text")); // NOI18N

        primaryKeyNameTextField.setFont(primaryKeyNameTextField.getFont());
        primaryKeyNameTextField.setText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.primaryKeyNameTextField.text")); // NOI18N
        primaryKeyNameTextField.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.primaryKeyNameTextField.toolTipText")); // NOI18N
        primaryKeyNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                primaryKeyNameTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                primaryKeyNameTextFieldFocusLost(evt);
            }
        });

        org.jdesktop.layout.GroupLayout pollConfigPanelLayout = new org.jdesktop.layout.GroupLayout(pollConfigPanel);
        pollConfigPanel.setLayout(pollConfigPanelLayout);
        pollConfigPanelLayout.setHorizontalGroup(
            pollConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pollConfigPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(pollConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(markColumnValLabel)
                    .add(pollConfigPanelLayout.createSequentialGroup()
                        .add(primaryKeyNameLabel)
                        .add(51, 51, 51)
                        .add(primaryKeyNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE))
                    .add(pollConfigPanelLayout.createSequentialGroup()
                        .add(pollIntervalLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pollIntervalTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE))
                    .add(pollConfigPanelLayout.createSequentialGroup()
                        .add(pollConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(moveRowtoTabLabel)
                            .add(markColNameLabel)
                            .add(postPollProcessingLabel))
                        .add(16, 16, 16)
                        .add(pollConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(markColNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
                            .add(moveRowtoTabTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
                            .add(postPollProcessingComboBox, 0, 379, Short.MAX_VALUE)
                            .add(markColumnValueTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE))))
                .addContainerGap())
        );
        pollConfigPanelLayout.setVerticalGroup(
            pollConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pollConfigPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(pollConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(markColumnValLabel)
                    .add(markColumnValueTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pollConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(postPollProcessingLabel)
                    .add(postPollProcessingComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pollConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(moveRowtoTabLabel)
                    .add(moveRowtoTabTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pollConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(markColNameLabel)
                    .add(markColNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pollConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(pollIntervalLabel)
                    .add(pollIntervalTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pollConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(primaryKeyNameLabel)
                    .add(primaryKeyNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        markColumnValueTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.markColumnValueTextField.AccessibleContext.accessibleName_1")); // NOI18N
        markColumnValueTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.markColumnValueTextField.AccessibleContext.accessibleDescription")); // NOI18N
        postPollProcessingComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.postPollProcessingComboBox.AccessibleContext.accessibleName_1")); // NOI18N
        postPollProcessingComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.postPollProcessingComboBox.AccessibleContext.accessibleDescription_1")); // NOI18N
        pollIntervalTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.pollIntervalTextField.AccessibleContext.accessibleName_1")); // NOI18N
        pollIntervalTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.pollIntervalTextField.AccessibleContext.accessibleDescription")); // NOI18N
        markColNameTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.markColNameTextField.AccessibleContext.accessibleName_1")); // NOI18N
        markColNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.markColNameTextField.AccessibleContext.accessibleDescription_1")); // NOI18N
        moveRowtoTabTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.moveRowtoTabTextField.AccessibleContext.accessibleName_1")); // NOI18N
        moveRowtoTabTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.moveRowtoTabTextField.AccessibleContext.accessibleDescription_1")); // NOI18N
        primaryKeyNameTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.primaryKeyNameTextField.AccessibleContext.accessibleName_1")); // NOI18N
        primaryKeyNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.primaryKeyNameTextField.AccessibleContext.accessibleDescription_1")); // NOI18N

        org.jdesktop.layout.GroupLayout qPropPanelLayout = new org.jdesktop.layout.GroupLayout(qPropPanel);
        qPropPanel.setLayout(qPropPanelLayout);
        qPropPanelLayout.setHorizontalGroup(
            qPropPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(qPropPanelLayout.createSequentialGroup()
                .add(qPropPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(qPropPanelLayout.createSequentialGroup()
                        .add(23, 23, 23)
                        .add(queryPropsLbl)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE))
                    .add(qPropPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(pollConfigCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pollConfigSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE))
                    .add(qPropPanelLayout.createSequentialGroup()
                        .add(23, 23, 23)
                        .add(qPropPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(queryPropsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(pollConfigPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .add(6, 6, 6))
        );
        qPropPanelLayout.setVerticalGroup(
            qPropPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(qPropPanelLayout.createSequentialGroup()
                .add(qPropPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(qPropPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(queryPropsLbl))
                    .add(qPropPanelLayout.createSequentialGroup()
                        .add(18, 18, 18)
                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(queryPropsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(qPropPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(pollConfigCheckBox)
                    .add(pollConfigSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pollConfigPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pollConfigCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.pollConfigCheckBox.AccessibleContext.accessibleName")); // NOI18N
        pollConfigCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.pollConfigCheckBox.toolTipText")); // NOI18N

        bindingPropertiesTabbedPane.addTab(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.qPropPanel.TabConstraints.tabTitle"), qPropPanel); // NOI18N
        qPropPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.qPropPanel.AccessibleContext.accessibleName")); // NOI18N
        qPropPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.qPropPanel.AccessibleContext.accessibleDescription")); // NOI18N

        serviceConfigPanel.setMinimumSize(new java.awt.Dimension(600, 500));
        serviceConfigPanel.setPreferredSize(new java.awt.Dimension(600, 500));

        transactionPropsLabel.setFont(transactionPropsLabel.getFont().deriveFont(transactionPropsLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(transactionPropsLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.transactionPropsLabel.text")); // NOI18N

        transactionLabel.setFont(transactionLabel.getFont());
        transactionLabel.setLabelFor(transactionComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(transactionLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.transactionLabel.text")); // NOI18N

        transactionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<Not Set>", "NOTransaction", "XATransaction" }));
        transactionComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.transactionComboBox.toolTipText")); // NOI18N
        transactionComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                transactionComboBoxFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                transactionComboBoxFocusLost(evt);
            }
        });

        serviceConfigLabel.setFont(serviceConfigLabel.getFont().deriveFont(serviceConfigLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(serviceConfigLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.serviceConfigLabel.text")); // NOI18N

        buttonGroup1.add(resConfigRadioButton);
        resConfigRadioButton.setFont(resConfigRadioButton.getFont().deriveFont(resConfigRadioButton.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(resConfigRadioButton, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.resConfigRadioButton.text")); // NOI18N
        resConfigRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.resConfigRadioButton.toolTipText")); // NOI18N
        resConfigRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resConfigRadioButtonActionPerformed(evt);
            }
        });
        resConfigRadioButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                resConfigRadioButtonFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                resConfigRadioButtonFocusLost(evt);
            }
        });

        jndiNameLabel.setFont(jndiNameLabel.getFont());
        jndiNameLabel.setLabelFor(jndiNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jndiNameLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.jndiNameLabel.text")); // NOI18N

        jndiNameTextField.setText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.jndiNameTextField.text")); // NOI18N
        jndiNameTextField.setToolTipText(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.jndiNameTextField.toolTipText")); // NOI18N
        jndiNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jndiNameTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jndiNameTextFieldFocusLost(evt);
            }
        });

        buttonGroup1.add(driverConfigRadioButton);
        driverConfigRadioButton.setFont(driverConfigRadioButton.getFont().deriveFont(driverConfigRadioButton.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(driverConfigRadioButton, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.driverConfigRadioButton.text")); // NOI18N
        driverConfigRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                driverConfigRadioButtonActionPerformed(evt);
            }
        });
        driverConfigRadioButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                driverConfigRadioButtonFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                driverConfigRadioButtonFocusLost(evt);
            }
        });

        connectionURLLabel.setLabelFor(connectionComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(connectionURLLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.connectionURLLabel.text")); // NOI18N

        connectionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        connectionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectionComboBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout serviceConfigPanelLayout = new org.jdesktop.layout.GroupLayout(serviceConfigPanel);
        serviceConfigPanel.setLayout(serviceConfigPanelLayout);
        serviceConfigPanelLayout.setHorizontalGroup(
            serviceConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(serviceConfigPanelLayout.createSequentialGroup()
                .add(serviceConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(serviceConfigPanelLayout.createSequentialGroup()
                        .add(49, 49, 49)
                        .add(serviceConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jndiNameLabel)
                            .add(connectionURLLabel)))
                    .add(serviceConfigPanelLayout.createSequentialGroup()
                        .add(36, 36, 36)
                        .add(transactionLabel)))
                .add(26, 26, 26)
                .add(serviceConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(transactionComboBox, 0, 405, Short.MAX_VALUE)
                    .add(jndiNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE)
                    .add(connectionComboBox, 0, 405, Short.MAX_VALUE))
                .addContainerGap())
            .add(serviceConfigPanelLayout.createSequentialGroup()
                .add(28, 28, 28)
                .add(resConfigRadioButton)
                .addContainerGap(388, Short.MAX_VALUE))
            .add(serviceConfigPanelLayout.createSequentialGroup()
                .add(28, 28, 28)
                .add(driverConfigRadioButton)
                .addContainerGap(352, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, serviceConfigPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(serviceConfigLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(serviceConfigSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
                .add(10, 10, 10))
            .add(serviceConfigPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(transactionPropsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(transactionPropsSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                .addContainerGap())
        );
        serviceConfigPanelLayout.setVerticalGroup(
            serviceConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(serviceConfigPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(serviceConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(transactionPropsLabel)
                    .add(transactionPropsSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(serviceConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(transactionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(transactionLabel))
                .add(18, 18, 18)
                .add(serviceConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(serviceConfigLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, serviceConfigPanelLayout.createSequentialGroup()
                        .add(8, 8, 8)
                        .add(serviceConfigSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 6, Short.MAX_VALUE)))
                .add(18, 18, 18)
                .add(resConfigRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(serviceConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jndiNameLabel)
                    .add(jndiNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(13, 13, 13)
                .add(driverConfigRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(serviceConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(connectionURLLabel)
                    .add(connectionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(292, 292, 292))
        );

        transactionComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.transactionComboBox.AccessibleContext.accessibleName_1")); // NOI18N
        transactionComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.transactionComboBox.AccessibleContext.accessibleDescription_1")); // NOI18N
        resConfigRadioButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.resConfigRadioButton.AccessibleContext.accessibleName")); // NOI18N
        resConfigRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.resConfigRadioButton.AccessibleContext.accessibleDescription_1")); // NOI18N
        jndiNameTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.jndiNameTextField.AccessibleContext.accessibleName_1")); // NOI18N
        jndiNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.jndiNameTextField.AccessibleContext.accessibleDescription_1")); // NOI18N
        driverConfigRadioButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.driverConfigRadioButton.AccessibleContext.accessibleName")); // NOI18N
        driverConfigRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DESC_Attribute_urlConfig")); // NOI18N
        connectionURLLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.connectionURLLabel.AccessibleContext.accessibleName")); // NOI18N
        connectionURLLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DESC_Attribute_urlConfig")); // NOI18N
        connectionComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.connectionComboBox.AccessibleContext.accessibleName")); // NOI18N
        connectionComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.connectionComboBox.AccessibleContext.accessibleDescription")); // NOI18N

        bindingPropertiesTabbedPane.addTab(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.serviceConfigPanel.TabConstraints.tabTitle"), serviceConfigPanel); // NOI18N
        serviceConfigPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.serviceConfigPanel.AccessibleContext.accessibleName")); // NOI18N
        serviceConfigPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.serviceConfigPanel.AccessibleContext.accessibleDescription")); // NOI18N

        descLabel.setFont(descLabel.getFont().deriveFont(descLabel.getFont().getStyle() | java.awt.Font.BOLD));
        descLabel.setLabelFor(userFocusedDescTextPane);
        org.openide.awt.Mnemonics.setLocalizedText(descLabel, org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.descLabel.text")); // NOI18N

        userFocusedDescTextPane.setEditable(false);
        jScrollPane2.setViewportView(userFocusedDescTextPane);
        userFocusedDescTextPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.userFocusedDescTextPane.AccessibleContext.accessibleName")); // NOI18N
        userFocusedDescTextPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.userFocusedDescTextPane.AccessibleContext.accessibleDescription")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, bindingPropertiesTabbedPane, 0, 0, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 578, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, descLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 240, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(12, 12, 12))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(bindingPropertiesTabbedPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 441, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(13, 13, 13)
                .add(descLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE)
                .addContainerGap())
        );

        descLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.descLabel.AccessibleContext.accessibleName")); // NOI18N
        descLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DBBindingConfigurationPanel.class, "DBBindingConfigurationPanel.descLabel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void noRecordsTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_noRecordsTextFieldFocusGained
    noRecordsTextField.selectAll();
    updateDescriptionArea(evt);
}//GEN-LAST:event_noRecordsTextFieldFocusGained

private void tableNameTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tableNameTextFieldFocusGained
    tableNameTextField.selectAll();
    updateDescriptionArea(evt);
}//GEN-LAST:event_tableNameTextFieldFocusGained

private void pollConfigCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_pollConfigCheckBoxStateChanged
markColNameTextField.setEditable(pollConfigCheckBox.isSelected());
    markColumnValueTextField.setEditable(pollConfigCheckBox.isSelected());
    pollIntervalTextField.setEditable(pollConfigCheckBox.isSelected());
    postPollProcessingComboBox.setEditable(pollConfigCheckBox.isSelected());
    primaryKeyNameTextField.setEditable(pollConfigCheckBox.isSelected());
    moveRowtoTabTextField.setEditable(pollConfigCheckBox.isSelected());
    pollIntervalTextField.setEditable(pollConfigCheckBox.isSelected());
}//GEN-LAST:event_pollConfigCheckBoxStateChanged

private void pollConfigCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pollConfigCheckBoxActionPerformed

}//GEN-LAST:event_pollConfigCheckBoxActionPerformed

private void markColumnValueTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_markColumnValueTextFieldFocusGained
    markColumnValueTextField.selectAll();
    updateDescriptionArea(evt);
}//GEN-LAST:event_markColumnValueTextFieldFocusGained

private void primaryKeyNameTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_primaryKeyNameTextFieldFocusGained
    primaryKeyNameTextField.selectAll();
    updateDescriptionArea(evt);
}//GEN-LAST:event_primaryKeyNameTextFieldFocusGained

private void driverConfigRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_driverConfigRadioButtonActionPerformed
if (driverConfigRadioButton.isSelected()) {
    }
}//GEN-LAST:event_driverConfigRadioButtonActionPerformed

private void resConfigRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resConfigRadioButtonActionPerformed
if (resConfigRadioButton.isSelected()) {
        jndiNameTextField.setEnabled(true);
        jndiNameTextField.setEditable(true);
        jndiNameTextField.selectAll();
    }
}//GEN-LAST:event_resConfigRadioButtonActionPerformed

private void connectionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectionComboBoxActionPerformed
Set oldConnections = new HashSet(Arrays.asList(ConnectionManager.getDefault().getConnections()));

    final Object item = this.connectionComboBox.getSelectedItem();
    if (item instanceof ConnectionWrapper) {
        final ConnectionWrapper cw = (ConnectionWrapper) item;
        this.selectedConnection = cw.getDatabaseConnection();

        ConnectionManager.getDefault().showConnectionDialog(this.selectedConnection);
        Connection jDBCConnection = this.selectedConnection.getJDBCConnection();
        providers.removeElement("");
    //this.persistModel();
    }/*else{
    ConnectionManager.getDefault().showAddConnectionDialog(null);
    }*/
    // try to find the new connection
    DatabaseConnection[] newConnections = ConnectionManager.getDefault().getConnections();
    if (newConnections.length == oldConnections.size()) {
        // no new connection, so...
        return;
    }

    providers.removeElement(new String("New Datasource..."));
    for (int i = 0; i < newConnections.length; i++) {
        if (!oldConnections.contains(newConnections[i])) {
            providers.addElement(new ConnectionWrapper(newConnections[i]));
            break;
        }
    }
}//GEN-LAST:event_connectionComboBoxActionPerformed

private void tableNameTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tableNameTextFieldFocusLost
    clearToolTipTextArea();
}//GEN-LAST:event_tableNameTextFieldFocusLost

private void noRecordsTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_noRecordsTextFieldFocusLost
    clearToolTipTextArea();
}//GEN-LAST:event_noRecordsTextFieldFocusLost

private void sqlStmtTextAreaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_sqlStmtTextAreaFocusGained
    sqlStmtTextArea.selectAll();
    updateDescriptionArea(evt);
}//GEN-LAST:event_sqlStmtTextAreaFocusGained

private void sqlStmtTextAreaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_sqlStmtTextAreaFocusLost
    clearToolTipTextArea();
}//GEN-LAST:event_sqlStmtTextAreaFocusLost

private void paramOrderTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_paramOrderTextFieldFocusGained
    paramOrderTextField.selectAll();
    updateDescriptionArea(evt);
}//GEN-LAST:event_paramOrderTextFieldFocusGained

private void paramOrderTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_paramOrderTextFieldFocusLost
    clearToolTipTextArea();
}//GEN-LAST:event_paramOrderTextFieldFocusLost

private void pollConfigCheckBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pollConfigCheckBoxFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_pollConfigCheckBoxFocusGained

private void pollConfigCheckBoxFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pollConfigCheckBoxFocusLost
    clearToolTipTextArea();
}//GEN-LAST:event_pollConfigCheckBoxFocusLost

private void markColumnValueTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_markColumnValueTextFieldFocusLost
    clearToolTipTextArea();
}//GEN-LAST:event_markColumnValueTextFieldFocusLost

private void postPollProcessingComboBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_postPollProcessingComboBoxFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_postPollProcessingComboBoxFocusGained

private void postPollProcessingComboBoxFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_postPollProcessingComboBoxFocusLost
    clearToolTipTextArea();
}//GEN-LAST:event_postPollProcessingComboBoxFocusLost

private void moveRowtoTabTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_moveRowtoTabTextFieldFocusGained
    moveRowtoTabTextField.selectAll();
    updateDescriptionArea(evt);
}//GEN-LAST:event_moveRowtoTabTextFieldFocusGained

private void moveRowtoTabTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_moveRowtoTabTextFieldFocusLost
    clearToolTipTextArea();
}//GEN-LAST:event_moveRowtoTabTextFieldFocusLost

private void markColNameTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_markColNameTextFieldFocusGained
    markColNameTextField.selectAll();
    updateDescriptionArea(evt);
}//GEN-LAST:event_markColNameTextFieldFocusGained

private void markColNameTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_markColNameTextFieldFocusLost
    clearToolTipTextArea();
}//GEN-LAST:event_markColNameTextFieldFocusLost

private void pollIntervalTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pollIntervalTextFieldFocusGained
    pollIntervalTextField.selectAll();
    updateDescriptionArea(evt);
}//GEN-LAST:event_pollIntervalTextFieldFocusGained

private void pollIntervalTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pollIntervalTextFieldFocusLost
    clearToolTipTextArea();
}//GEN-LAST:event_pollIntervalTextFieldFocusLost

private void primaryKeyNameTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_primaryKeyNameTextFieldFocusLost
    clearToolTipTextArea();
}//GEN-LAST:event_primaryKeyNameTextFieldFocusLost

private void transactionComboBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_transactionComboBoxFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_transactionComboBoxFocusGained

private void transactionComboBoxFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_transactionComboBoxFocusLost
    clearToolTipTextArea();
}//GEN-LAST:event_transactionComboBoxFocusLost

private void resConfigRadioButtonFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_resConfigRadioButtonFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_resConfigRadioButtonFocusGained

private void resConfigRadioButtonFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_resConfigRadioButtonFocusLost
    clearToolTipTextArea();
}//GEN-LAST:event_resConfigRadioButtonFocusLost

private void jndiNameTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jndiNameTextFieldFocusGained
    jndiNameTextField.selectAll();
    updateDescriptionArea(evt);
}//GEN-LAST:event_jndiNameTextFieldFocusGained

private void jndiNameTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jndiNameTextFieldFocusLost
    clearToolTipTextArea();
}//GEN-LAST:event_jndiNameTextFieldFocusLost

private void driverConfigRadioButtonFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_driverConfigRadioButtonFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_driverConfigRadioButtonFocusGained

private void driverConfigRadioButtonFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_driverConfigRadioButtonFocusLost
    clearToolTipTextArea();
}//GEN-LAST:event_driverConfigRadioButtonFocusLost

private void bindingNameComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {
// TODO add your handling code here:
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            String selectedBinding = (String) bindingNameComboBox.getSelectedItem();
            // if binding name is changed, update the selected port type
            if (mComponent != null) {
                if (mComponent instanceof JDBCAddress) {
                    PortType portType = getPortType(selectedBinding,
                            (JDBCAddress) mComponent);
                    portTypeComboBox.setSelectedItem(portType.getName());
                } else if (mComponent instanceof JDBCBinding) {
                    Binding parentBinding = (Binding) ((JDBCBinding) mComponent).getParent();
                    NamedComponentReference<PortType> portType =
                            parentBinding.getType();
                    if ((portType != null) && (portType.get() != null)) {
                        portTypeComboBox.setSelectedItem(portType.get().getName());
                    }
                } else if (mComponent instanceof JDBCOperationInput) {
                    Object obj = ((JDBCOperationInput) mComponent).getParent();
                    Binding parentBinding = null;
                    if (obj instanceof BindingInput) {
                        BindingOperation parentOp =
                                (BindingOperation) ((BindingInput) obj).getParent();
                        parentBinding = (Binding) parentOp.getParent();
                    }
                    if ((parentBinding != null) &&
                            (parentBinding.getType() != null) &&
                            (parentBinding.getType().get() == null)) {
                        NamedComponentReference<PortType> portType =
                                parentBinding.getType();
                        if (parentBinding.getName().equals(selectedBinding)) {
                            portTypeComboBox.setSelectedItem(portType.get().getName());
                        }
                    }
                } else if (mComponent instanceof JDBCOperationOutput) {
                    Object obj = ((JDBCOperationOutput) mComponent).getParent();
                    Binding parentBinding = null;
                    if (obj instanceof BindingOutput) {
                        BindingOperation parentOp =
                                (BindingOperation) ((BindingOutput) obj).getParent();
                        parentBinding = (Binding) parentOp.getParent();
                    }
                    if ((parentBinding != null) &&
                            (parentBinding.getType() != null) &&
                            (parentBinding.getType().get() == null)) {
                        NamedComponentReference<PortType> portType =
                                parentBinding.getType();
                        if (parentBinding.getName().equals(selectedBinding)) {
                            portTypeComboBox.setSelectedItem(portType.get().getName());
                        }
                    }
                } else if (mComponent instanceof JDBCOperation) {
                    Object obj = ((JDBCOperation) mComponent).getParent();
                    if (obj instanceof BindingOperation) {
                        Binding parentBinding = (Binding) ((BindingOperation) obj).getParent();
                        Collection<JDBCBinding> bindings = parentBinding.getExtensibilityElements(JDBCBinding.class);
                        if (!bindings.isEmpty()) {
                            populateJDBCBinding(bindings.iterator().next(), null);
                            bindingNameComboBox.setSelectedItem(
                                    parentBinding.getName());
                        }
                    }
                }
            }
        }
    }

    private void servicePortComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {
// TODO add your handling code here:
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            Object selObj = servicePortComboBox.getSelectedItem();
            String selBindingName = "";
            if (bindingNameComboBox.getSelectedItem() != null) {
                selBindingName = bindingNameComboBox.getSelectedItem().toString();
            }
            if ((selObj != null) && (mComponent != null)) {
                Port selServicePort = (Port) selObj;
                if (selServicePort.getBinding() != null) {
                    Binding binding = selServicePort.getBinding().get();
                    if ((binding != null) && (binding.getName().
                            equals(selBindingName))) {
                        Iterator<JDBCAddress> ldapAddresses = selServicePort.getExtensibilityElements(JDBCAddress.class).iterator();
                        // 1 fileaddress for 1 binding
                        while (ldapAddresses.hasNext()) {
                            updateServiceView(ldapAddresses.next());
                            break;
                        }
                    }
                }
            }
        }
    }

    private void operationNameComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {
// TODO add your handling code here:
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            String selectedOperation = (String) operationNameComboBox.getSelectedItem();
            if (mComponent != null) {
                Binding binding = null;
                if (mComponent instanceof JDBCAddress) {
                    Port port = (Port) ((JDBCAddress) mComponent).getParent();
                    binding = port.getBinding().get();
                } else if (mComponent instanceof JDBCBinding) {
                    binding = (Binding) ((JDBCBinding) mComponent).getParent();

                } else if (mComponent instanceof JDBCOperationInput) {
                    Object obj = ((JDBCOperationInput) mComponent).getParent();
                    if (obj instanceof BindingInput) {
                        BindingOperation parentOp =
                                (BindingOperation) ((BindingInput) obj).getParent();
                        binding = (Binding) parentOp.getParent();
                    }

                } else if (mComponent instanceof JDBCOperation) {
                    Object obj = ((JDBCOperation) mComponent).getParent();
                    if (obj instanceof BindingOperation) {
                        binding = (Binding) ((BindingOperation) obj).getParent();
                    }
                }
                if (binding != null) {

                    JDBCOperationInput jdbcInput = getInputJDBCOperationInput(binding,
                            selectedOperation);
                    updateInputMessageView(jdbcInput);
                    populateJDBCBindingOperationInput(jdbcInput);
                }
            }
        }
    }

    private void updateDescriptionArea(FocusEvent evt) {
        userFocusedDescTextPane.setText("");
        /** style document for description area **/
        Style style =  mDoc.addStyle("StyleName", null);

        String[] desc = null;
        boolean casaEdited = false;

        if (evt.getSource() == tableNameTextField) {
            desc = new String[]{"Table Name Text Field  ",
                    mBundle.getString("DESC_Attribute_TableName")};
            casaEdited = true;
        } else if (evt.getSource() == noRecordsTextField){
            desc = new String[]{" Number of Records Text Field  ",
                    mBundle.getString("DESC_Attribute_numberOfRecords")};
            casaEdited = true;
        } else if (evt.getSource() == sqlStmtTextArea){
            desc = new String[]{" SQL Statement Text Area   ",
                    mBundle.getString("DESC_Attribute_sql")};
             casaEdited = true;
        } else if (evt.getSource() == paramOrderTextField){
            desc = new String[]{" Param Order Text Field  ",
                    mBundle.getString("DESC_Attribute_paramOrder")};
             casaEdited = true;
        } else if (evt.getSource() == pollConfigCheckBox){
            desc = new String[]{" Poll Configuration Check box  ",
                    mBundle.getString("DESC_Attribute_pollConfig")};
             casaEdited = true;
        } else if (evt.getSource() == markColumnValueTextField){
            desc = new String[]{" Mark Column Value Text Field  ",
                    mBundle.getString("DESC_Attribute_MarkColumnValue")};
             casaEdited = true;
        } else if (evt.getSource() == postPollProcessingComboBox){
            desc = new String[]{" Polling post processing Combo Box  ",
                    mBundle.getString("DESC_Attribute_PollingPostProcessing")};
             casaEdited = true;
        } else if (evt.getSource() == moveRowtoTabTextField){
            desc = new String[]{" Move row to the Table Text Field  ",
                    mBundle.getString("DESC_Attribute_MoveRowToTableName")};
             casaEdited = true;
        } else if (evt.getSource() == markColNameTextField){
            desc = new String[]{" Mark Column Name Text Field  ",
                    mBundle.getString("DESC_Attribute_MarkColumnName")};
             casaEdited = true;
        } else if (evt.getSource() == pollIntervalTextField){
            desc = new String[]{" Poll Interval Text Field  ",
                    mBundle.getString("DESC_Attribute_PollMilliSeconds")};
             casaEdited = true;
        } else if (evt.getSource() == primaryKeyNameTextField){
            desc = new String[]{" Primary Key Name Text Field  ",
                    mBundle.getString("DESC_Attribute_PKName")};
             casaEdited = true;
        } else if (evt.getSource() == transactionComboBox){
            desc = new String[]{" Transaction Combo Box  ",
                    mBundle.getString("DESC_Attribute_Transaction")};
             casaEdited = true;
        } else if (evt.getSource() == resConfigRadioButton){
            desc = new String[]{" Resource Configuration Radio Button  ",
                    mBundle.getString("DESC_Attribute_ResourceConfig")};
             casaEdited = true;
        } else if (evt.getSource() == jndiNameTextField){
            desc = new String[]{" JNDI Name Text Field ",
                    mBundle.getString("DESC_Attribute_jndiName")};
             casaEdited = true;
        } else if (evt.getSource() == driverConfigRadioButton){
            desc = new String[]{" URL Configuration Radio Button ",
                    mBundle.getString("DESC_Attribute_urlConfig")};
             casaEdited = true;
        }

        if (desc != null) {
            try {
                AttributeSet boldAs = new SimpleAttributeSet();
                StyleConstants.setBold((MutableAttributeSet) boldAs, true);
                userFocusedDescTextPane.getStyledDocument().insertString(mDoc.getLength(), desc[0],
                        boldAs);
                mDoc.insertString(mDoc.getLength(), ": \n",
                        mDoc.getStyle(mStyles[0]));
                mDoc.insertString(mDoc.getLength(), desc[1],
                        mDoc.getStyle(mStyles[1]));

           if (casaEdited) {
                    mDoc.insertString(mDoc.getLength(), "\n",
                            mDoc.getStyle(mStyles[1]));
                    mDoc.insertString(mDoc.getLength(), "  " + NbBundle.getMessage(DBBindingConfigurationPanel.class,
                            "DBBindingConfigurationPanel.CASA_EDITED"),
                            mDoc.getStyle(mStyles[1]));
               }

                userFocusedDescTextPane.setCaretPosition(0);
            } catch(BadLocationException ble) {
            }
            return;
        }
    }

    private void clearToolTipTextArea() {
        userFocusedDescTextPane.setText("");
    }

    private void cleanUp() {
        mComponent = null;
        mQName = null;
        resetPanel();
    }

    public class PortCellRenderer extends JLabel
            implements javax.swing.ListCellRenderer {

        public PortCellRenderer() {
            super();
            setOpaque(true);
        }

        public Component getListCellRendererComponent(javax.swing.JList list,
                Object value, int index, boolean isSelected,
                boolean isFocused) {
            if ((value != null) && (value instanceof Port)) {
                setText(((Port) value).getName());
                setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
                setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            }
            return this;
        }
    }
    /** the WSDL model to configure **/
    private WSDLComponent mComponent;
    /** QName **/
    private QName mQName;

    private ResourceBundle mBundle = ResourceBundle.getBundle(
            "org.netbeans.modules.wsdlextensions.jdbc.resources.Bundle");

    private StyledDocument mDoc = null;
    private StyledDocument mDocAdv = null;
    private StyledDocument mDocAdvOut = null;
    private String[] mStyles = null;

    String operationType = "";
    String sqlStmt = "";
    String paramOrder = "";
    DefaultComboBoxModel providers;
    private JComboBox bindingNameComboBox;
    private JComboBox servicePortComboBox;
    private JComboBox operationNameComboBox;
    private JComboBox portTypeComboBox;
    private DatabaseConnection selectedConnection;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JTabbedPane bindingPropertiesTabbedPane;
    protected javax.swing.ButtonGroup buttonGroup1;
    protected javax.swing.JComboBox connectionComboBox;
    protected javax.swing.JLabel connectionURLLabel;
    protected javax.swing.JLabel descLabel;
    protected javax.swing.JRadioButton driverConfigRadioButton;
    protected javax.swing.JScrollPane jScrollPane2;
    protected javax.swing.JScrollPane jScrollPane3;
    protected javax.swing.JSeparator jSeparator1;
    protected javax.swing.JLabel jndiNameLabel;
    protected javax.swing.JTextField jndiNameTextField;
    protected javax.swing.JLabel markColNameLabel;
    protected javax.swing.JTextField markColNameTextField;
    protected javax.swing.JLabel markColumnValLabel;
    protected javax.swing.JTextField markColumnValueTextField;
    protected javax.swing.JLabel moveRowtoTabLabel;
    protected javax.swing.JTextField moveRowtoTabTextField;
    protected javax.swing.JTextField noRecordsTextField;
    protected javax.swing.JLabel noRecordsdLabel;
    protected javax.swing.JLabel paramOrderLabel;
    protected javax.swing.JTextField paramOrderTextField;
    protected javax.swing.JCheckBox pollConfigCheckBox;
    protected javax.swing.JPanel pollConfigPanel;
    protected javax.swing.JSeparator pollConfigSeparator;
    protected javax.swing.JLabel pollIntervalLabel;
    protected javax.swing.JTextField pollIntervalTextField;
    protected javax.swing.JComboBox postPollProcessingComboBox;
    protected javax.swing.JLabel postPollProcessingLabel;
    protected javax.swing.JLabel primaryKeyNameLabel;
    protected javax.swing.JTextField primaryKeyNameTextField;
    protected javax.swing.JPanel qPropPanel;
    protected javax.swing.JLabel queryPropsLbl;
    protected javax.swing.JPanel queryPropsPanel;
    protected javax.swing.JRadioButton resConfigRadioButton;
    protected javax.swing.JLabel serviceConfigLabel;
    protected javax.swing.JPanel serviceConfigPanel;
    protected javax.swing.JSeparator serviceConfigSeparator;
    protected javax.swing.JLabel sqlStmtLabel;
    protected javax.swing.JTextArea sqlStmtTextArea;
    protected javax.swing.JLabel tableNameLabel;
    protected javax.swing.JTextField tableNameTextField;
    protected javax.swing.JComboBox transactionComboBox;
    protected javax.swing.JLabel transactionLabel;
    protected javax.swing.JLabel transactionPropsLabel;
    protected javax.swing.JSeparator transactionPropsSeparator;
    protected javax.swing.JTextPane userFocusedDescTextPane;
    // End of variables declaration//GEN-END:variables
}
