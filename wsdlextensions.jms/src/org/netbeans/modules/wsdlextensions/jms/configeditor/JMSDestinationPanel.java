/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.wsdlextensions.jms.configeditor;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.namespace.QName;
import org.netbeans.modules.wsdlextensions.jms.JMSAddress;
import org.netbeans.modules.wsdlextensions.jms.JMSBinding;
import org.netbeans.modules.wsdlextensions.jms.JMSConstants;
import org.netbeans.modules.wsdlextensions.jms.JMSMessage;
import org.netbeans.modules.wsdlextensions.jms.JMSOperation;
import org.netbeans.modules.wsdlextensions.jms.validator.JMSComponentValidator;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 * JMSBindingConfigurationPanel - Panel that allows configuration of
 * properties specifically for JMS Binding component
 *
 * @author  jalmero
 */
public class JMSDestinationPanel extends javax.swing.JPanel {

    private WSDLComponent mComponent;

    /** QName **/
    private QName mQName;

    /** resource bundle for file bc **/
    private ResourceBundle mBundle = ResourceBundle.getBundle(
            "org.netbeans.modules.wsdlextensions.jms.resources.Bundle");

    private static final Logger mLogger = Logger.
            getLogger(JMSDestinationPanel.class.getName());

    private MyItemListener mItemListener = null;

    private MyActionListener mActionListener = null;
    private MyDocumentListener mDocumentListener = null;

    private DescriptionPanel mDescPanel = null;
    private DescriptionPanel descPanelArchivePanel = null;
    private DescriptionPanel descPanelTextPanel = null;

    private Dialog mDetailsDlg = null;
    private DialogDescriptor mDetailsDlgDesc = null;

    private Dialog mDetailsJNDIDlg = null;
    private DialogDescriptor mDetailsJNDIDlgDesc = null;

    private Dialog mJNDIEnvDlg = null;
    private DialogDescriptor mJNDIEnvDlgDesc = null;
    private JNDIEnvironmentPanel mJNDIEnvPanel = null;    

    /** Creates new form JMSBindingConfigurationPanel */
    public JMSDestinationPanel(QName qName, WSDLComponent component) {
        initComponents();
        populateView(qName, mComponent);
        setAccessibility();
    }

    @Override
    public String getName() {
        return "Request Configuration";
    }    

    /**
     * Return the destination
     * @return String destination
     */
    String getDestination() {
        return trimTextFieldInput(destinationTextField.getText());
    }

    /**
     * Return the destination type value
     * @return String destination type
     */
    String getDestinationType() {
        if (queueRBtn.isSelected()) {
            return JMSConstants.QUEUE;
        }else {
            return JMSConstants.TOPIC;
        }
    }

    /**
     * Return transaction value
     * @return String transaction
     */
    String getTransaction() {
        if (xaTransactionBox.isSelected()) {
            return JMSConstants.TRANSACTION_XA;
        } else {
            return JMSConstants.TRANSACTION_NONE;
        }
    }

    /**
     * Return the client ID value
     * @return String client ID
     */
    String getClientID() {
        return trimTextFieldInput(clientIdTextField.getText());
    }

    /**
     * Return the subscription name value
     * @return String subscription name
     */
    String getSubscriptionName() {
        return trimTextFieldInput(subscriptionNameTextField.getText());
    }

    String getSubscriptionDurability() {
        if (durableRadioButton.isSelected()) {
            return JMSConstants.DURABLE;
        }else {
            return JMSConstants.NON_DURABLE;
        }
    }

 
    void showXATransaction(boolean mode) {
        xaTransactionBox.setVisible(mode);    
    }
    
    void showTopicDetails(boolean show) {
        topicDetailsPanel.setVisible(show);
    }
    
   /**
     * Set the operation name to be configured
     * @param opName
     */
    void setOperationName(String opName) {
        if (opName != null) {
            operationNameComboBox.setSelectedItem(opName);
        }
    }    

    private void setAccessibility() {
        destinationTextField.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_destination")); // NOI18N
        destinationTextField.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_destination")); // NOI18N         
        subscriptionNameTextField.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_subscriptionName")); // NOI18N
        subscriptionNameTextField.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_subscriptionName")); // NOI18N         
        clientIdTextField.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_clientID")); // NOI18N
        clientIdTextField.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_clientID")); // NOI18N                 
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        QTopicBGroup = new javax.swing.ButtonGroup();
        portBindingPanel = new javax.swing.JPanel();
        jLabel26 = new javax.swing.JLabel();
        portTypeLabel = new javax.swing.JLabel();
        operationNameComboBox = new javax.swing.JComboBox();
        jLabel42 = new javax.swing.JLabel();
        bindingNameComboBox = new javax.swing.JComboBox();
        operationNameLabel = new javax.swing.JLabel();
        servicePortComboBox = new javax.swing.JComboBox();
        bindingNameLabel = new javax.swing.JLabel();
        portTypeComboBox = new javax.swing.JComboBox();
        jSeparator2 = new javax.swing.JSeparator();
        topicSectionPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        durableBtnGrp = new javax.swing.ButtonGroup();
        connectionPanel = new javax.swing.JPanel();
        jSeparator3 = new javax.swing.JSeparator();
        destinationLab = new javax.swing.JLabel();
        destinationTextField = new javax.swing.JTextField();
        destinationTypeLab = new javax.swing.JLabel();
        queueRBtn = new javax.swing.JRadioButton();
        topicRBtn = new javax.swing.JRadioButton();
        xaTransactionBox = new javax.swing.JCheckBox();
        topicDetailsPanel = new javax.swing.JPanel();
        subscriptionLabel = new javax.swing.JLabel();
        durableRadioButton = new javax.swing.JRadioButton();
        nondurableRadioButton = new javax.swing.JRadioButton();
        subscriptionNameTextField = new javax.swing.JTextField();
        clientIdLabel = new javax.swing.JLabel();
        clientIdTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        destinationSectionLab = new javax.swing.JLabel();

        portBindingPanel.setName("portBindingPanel"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel26, org.openide.util.NbBundle.getMessage(JMSDestinationPanel.class, "JMSConnectionPanel.jLabel26.text")); // NOI18N
        jLabel26.setName("jLabel26"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(portTypeLabel, org.openide.util.NbBundle.getMessage(JMSDestinationPanel.class, "JMSConnectionPanel.portTypeLabel.text")); // NOI18N
        portTypeLabel.setName("portTypeLabel"); // NOI18N

        operationNameComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        operationNameComboBox.setName("operationNameComboBox"); // NOI18N
        operationNameComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                operationNameComboBoxItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel42, org.openide.util.NbBundle.getMessage(JMSDestinationPanel.class, "JMSConnectionPanel.jLabel42.text")); // NOI18N
        jLabel42.setName("jLabel42"); // NOI18N

        bindingNameComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        bindingNameComboBox.setName("bindingNameComboBox"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(operationNameLabel, org.openide.util.NbBundle.getMessage(JMSDestinationPanel.class, "JMSConnectionPanel.operationNameLabel.text")); // NOI18N
        operationNameLabel.setName("operationNameLabel"); // NOI18N

        servicePortComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        servicePortComboBox.setName("servicePortComboBox"); // NOI18N
        servicePortComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                servicePortComboBoxItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(bindingNameLabel, org.openide.util.NbBundle.getMessage(JMSDestinationPanel.class, "JMSConnectionPanel.bindingNameLabel.text")); // NOI18N
        bindingNameLabel.setName("bindingNameLabel"); // NOI18N

        portTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        portTypeComboBox.setName("portTypeComboBox"); // NOI18N

        jSeparator2.setName("jSeparator2"); // NOI18N

        org.jdesktop.layout.GroupLayout portBindingPanelLayout = new org.jdesktop.layout.GroupLayout(portBindingPanel);
        portBindingPanel.setLayout(portBindingPanelLayout);
        portBindingPanelLayout.setHorizontalGroup(
            portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 417, Short.MAX_VALUE)
            .add(portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(portBindingPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .add(portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(portBindingPanelLayout.createSequentialGroup()
                            .add(10, 10, 10)
                            .add(portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(jLabel26)
                                .add(portTypeLabel)
                                .add(operationNameLabel)
                                .add(bindingNameLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 72, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(servicePortComboBox, 0, 0, Short.MAX_VALUE)
                                .add(portBindingPanelLayout.createSequentialGroup()
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(org.jdesktop.layout.GroupLayout.TRAILING, portTypeComboBox, 0, 0, Short.MAX_VALUE)
                                        .add(operationNameComboBox, 0, 0, Short.MAX_VALUE)))
                                .add(org.jdesktop.layout.GroupLayout.TRAILING, bindingNameComboBox, 0, 0, Short.MAX_VALUE)))
                        .add(portBindingPanelLayout.createSequentialGroup()
                            .add(jLabel42)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)))
                    .addContainerGap()))
        );
        portBindingPanelLayout.setVerticalGroup(
            portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 152, Short.MAX_VALUE)
            .add(portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(portBindingPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .add(portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                        .add(jLabel42)
                        .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel26)
                        .add(servicePortComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(bindingNameComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(bindingNameLabel))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(portTypeLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(portTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(operationNameLabel)
                        .add(operationNameComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        topicSectionPanel.setMinimumSize(new java.awt.Dimension(279, 120));
        topicSectionPanel.setName("topicSectionPanel"); // NOI18N
        topicSectionPanel.setPreferredSize(new java.awt.Dimension(375, 230));
        topicSectionPanel.setLayout(new java.awt.GridBagLayout());

        jPanel1.setName("jPanel1"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weighty = 0.5;
        topicSectionPanel.add(jPanel1, gridBagConstraints);

        setName("Form"); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        connectionPanel.setName("connectionPanel"); // NOI18N
        connectionPanel.setLayout(new java.awt.GridBagLayout());

        jSeparator3.setName("jSeparator3"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 145, 0, 0);
        connectionPanel.add(jSeparator3, gridBagConstraints);

        destinationLab.setLabelFor(destinationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(destinationLab, org.openide.util.NbBundle.getMessage(JMSDestinationPanel.class, "JMSConnectionPanel.destinationLab.text_1")); // NOI18N
        destinationLab.setName("destinationLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        connectionPanel.add(destinationLab, gridBagConstraints);

        destinationTextField.setText(org.openide.util.NbBundle.getMessage(JMSDestinationPanel.class, "JMSConnectionPanel.destinationTextField.text_1")); // NOI18N
        destinationTextField.setToolTipText( mBundle.getString("DESC_Attribute_destination"));
        destinationTextField.setName("destinationTextField"); // NOI18N
        destinationTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                destinationTextFieldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        connectionPanel.add(destinationTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(destinationTypeLab, org.openide.util.NbBundle.getMessage(JMSDestinationPanel.class, "JMSConnectionPanel.destinationTypeLab.text_1")); // NOI18N
        destinationTypeLab.setName("destinationTypeLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 10);
        connectionPanel.add(destinationTypeLab, gridBagConstraints);

        QTopicBGroup.add(queueRBtn);
        org.openide.awt.Mnemonics.setLocalizedText(queueRBtn, org.openide.util.NbBundle.getMessage(JMSDestinationPanel.class, "JMSConnectionPanel.queueRBtn.text_1")); // NOI18N
        queueRBtn.setToolTipText(mBundle.getString("DESC_Attribute_destinationType"));
        queueRBtn.setName("queueRBtn"); // NOI18N
        queueRBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                queueRBtnFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        connectionPanel.add(queueRBtn, gridBagConstraints);

        QTopicBGroup.add(topicRBtn);
        org.openide.awt.Mnemonics.setLocalizedText(topicRBtn, org.openide.util.NbBundle.getMessage(JMSDestinationPanel.class, "JMSConnectionPanel.topicRBtn.text_1")); // NOI18N
        topicRBtn.setToolTipText(mBundle.getString("DESC_Attribute_destinationType"));
        topicRBtn.setName("topicRBtn"); // NOI18N
        topicRBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                topicRBtnFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 80, 0, 0);
        connectionPanel.add(topicRBtn, gridBagConstraints);

        xaTransactionBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(xaTransactionBox, org.openide.util.NbBundle.getMessage(JMSDestinationPanel.class, "JMSConnectionPanel.xaTransactionBox.text_1")); // NOI18N
        xaTransactionBox.setToolTipText(mBundle.getString("DESC_Attribute_transaction"));
        xaTransactionBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        xaTransactionBox.setName("xaTransactionBox"); // NOI18N
        xaTransactionBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                xaTransactionBoxFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        connectionPanel.add(xaTransactionBox, gridBagConstraints);

        topicDetailsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        topicDetailsPanel.setName("topicDetailsPanel"); // NOI18N
        topicDetailsPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(subscriptionLabel, org.openide.util.NbBundle.getMessage(JMSDestinationPanel.class, "JMSDestinationPanel.subscriptionLabel.text")); // NOI18N
        subscriptionLabel.setToolTipText(org.openide.util.NbBundle.getMessage(JMSDestinationPanel.class, "JMSDestinationPanel.subscriptionLabel.toolTipText")); // NOI18N
        subscriptionLabel.setName("subscriptionLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        topicDetailsPanel.add(subscriptionLabel, gridBagConstraints);

        durableBtnGrp.add(durableRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(durableRadioButton, org.openide.util.NbBundle.getMessage(JMSDestinationPanel.class, "JMSDestinationPanel.durableRadioButton.text")); // NOI18N
        durableRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(JMSDestinationPanel.class, "JMSDestinationPanel.durableRadioButton.toolTipText")); // NOI18N
        durableRadioButton.setName("durableRadioButton"); // NOI18N
        durableRadioButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                durableRadioButtonFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        topicDetailsPanel.add(durableRadioButton, gridBagConstraints);

        durableBtnGrp.add(nondurableRadioButton);
        nondurableRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(nondurableRadioButton, org.openide.util.NbBundle.getMessage(JMSDestinationPanel.class, "JMSDestinationPanel.nondurableRadioButton.text")); // NOI18N
        nondurableRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(JMSDestinationPanel.class, "JMSDestinationPanel.nondurableRadioButton.toolTipText")); // NOI18N
        nondurableRadioButton.setName("nondurableRadioButton"); // NOI18N
        nondurableRadioButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                nondurableRadioButtonFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        topicDetailsPanel.add(nondurableRadioButton, gridBagConstraints);

        subscriptionNameTextField.setText(org.openide.util.NbBundle.getMessage(JMSDestinationPanel.class, "JMSDestinationPanel.subscriptionNameTextField.text")); // NOI18N
        subscriptionNameTextField.setToolTipText(mBundle.getString("DESC_Attribute_subscriptionName"));
        subscriptionNameTextField.setName("subscriptionNameTextField"); // NOI18N
        subscriptionNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                subscriptionNameTextFieldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        topicDetailsPanel.add(subscriptionNameTextField, gridBagConstraints);

        clientIdLabel.setLabelFor(clientIdTextField);
        org.openide.awt.Mnemonics.setLocalizedText(clientIdLabel, org.openide.util.NbBundle.getMessage(JMSDestinationPanel.class, "JMSDestinationPanel.clientIdLabel.text")); // NOI18N
        clientIdLabel.setToolTipText(org.openide.util.NbBundle.getMessage(JMSDestinationPanel.class, "JMSDestinationPanel.clientIdLabel.toolTipText")); // NOI18N
        clientIdLabel.setName("clientIdLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        topicDetailsPanel.add(clientIdLabel, gridBagConstraints);

        clientIdTextField.setText(org.openide.util.NbBundle.getMessage(JMSDestinationPanel.class, "JMSDestinationPanel.clientIdTextField.text")); // NOI18N
        clientIdTextField.setToolTipText(org.openide.util.NbBundle.getMessage(JMSDestinationPanel.class, "DESC_Attribute_clientID")); // NOI18N
        clientIdTextField.setName("clientIdTextField"); // NOI18N
        clientIdTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                clientIdTextFieldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        topicDetailsPanel.add(clientIdTextField, gridBagConstraints);

        jLabel1.setLabelFor(subscriptionNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(JMSDestinationPanel.class, "JMSDestinationPanel.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 80, 0, 0);
        topicDetailsPanel.add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        connectionPanel.add(topicDetailsPanel, gridBagConstraints);

        destinationSectionLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(destinationSectionLab, org.openide.util.NbBundle.getMessage(JMSDestinationPanel.class, "JMSDestinationPanel.destinationSectionLab.text")); // NOI18N
        destinationSectionLab.setName("destinationSectionLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        connectionPanel.add(destinationSectionLab, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(connectionPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void operationNameComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_operationNameComboBoxItemStateChanged
    // TODO add your handling code here:
    if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
        String selectedOperation = (String) operationNameComboBox.
                getSelectedItem();
        if (mComponent != null)  {
            Binding binding = null;
            if (mComponent instanceof JMSAddress) {
                Port port = (Port) ((JMSAddress) mComponent).getParent();
                binding = port.getBinding().get();

            } else if (mComponent instanceof JMSBinding) {
                binding = (Binding) ((JMSBinding) mComponent).getParent();
            } else if (mComponent instanceof Port) {
                if (((Port) mComponent).getBinding() != null) {
                    binding = ((Port) mComponent).getBinding().get();
                }
            }
            if (binding != null) {
                JMSMessage inputMessage = getInputJMSMessage(binding,
                        selectedOperation);
                updateInputMessageView(inputMessage);

            }
        }
    }
}//GEN-LAST:event_operationNameComboBoxItemStateChanged

private void servicePortComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_servicePortComboBoxItemStateChanged
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
                    Iterator<JMSAddress> jmsAddresses = selServicePort.
                            getExtensibilityElements(JMSAddress.class).
                            iterator();
                    // 1 fileaddress for 1 binding
                    while (jmsAddresses.hasNext()) {
                        break;
                    }
                }
            }
        }
    }
}//GEN-LAST:event_servicePortComboBoxItemStateChanged

private void destinationTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_destinationTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_destinationTextFieldFocusGained

private void queueRBtnFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_queueRBtnFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_queueRBtnFocusGained

private void topicRBtnFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_topicRBtnFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_topicRBtnFocusGained

private void xaTransactionBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_xaTransactionBoxFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_xaTransactionBoxFocusGained

private void durableRadioButtonFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_durableRadioButtonFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_durableRadioButtonFocusGained

private void nondurableRadioButtonFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nondurableRadioButtonFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_nondurableRadioButtonFocusGained

private void subscriptionNameTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_subscriptionNameTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_subscriptionNameTextFieldFocusGained

private void clientIdTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_clientIdTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_clientIdTextFieldFocusGained


    private void initListeners() {
        if (mItemListener == null)  {
            mItemListener = new MyItemListener();
        }

        if (mActionListener == null) {
            mActionListener = new MyActionListener();
        }

        if (mDocumentListener == null) {
            mDocumentListener = new MyDocumentListener();
        }
        
        queueRBtn.addItemListener(mItemListener);
        topicRBtn.addItemListener(mItemListener);   
        durableRadioButton.addItemListener(mItemListener);
        nondurableRadioButton.addItemListener(mItemListener); 
        subscriptionNameTextField.getDocument().addDocumentListener(mDocumentListener);
    }

    private void resetView() {
        queueRBtn.removeItemListener(mItemListener);
        topicRBtn.removeItemListener(mItemListener);
        durableRadioButton.removeItemListener(mItemListener);
        nondurableRadioButton.removeItemListener(mItemListener);         
        subscriptionNameTextField.getDocument().removeDocumentListener(mDocumentListener);
 
        servicePortComboBox.setEnabled(false);
        servicePortComboBox.removeAllItems();
        bindingNameComboBox.removeAllItems();
        portTypeComboBox.removeAllItems();
        operationNameComboBox.removeAllItems();

        queueRBtn.setSelected(true);
        destinationTextField.setText("");
        subscriptionNameTextField.setText("");
        clientIdTextField.setText("");
    }

    /**
     * Populate the view with the given the model component
     * @param qName
     * @param component
     */
    public void populateView(QName qName, WSDLComponent component) {
        cleanUp();
        mQName = qName;
        mComponent = component;
        resetView();
        populateView(mComponent);        
        initListeners();        
    }

    public void setDescriptionPanel(DescriptionPanel descPanel) {
        mDescPanel = descPanel;
    }
    
    private void populateView(WSDLComponent component) {
        if (component != null) {
            if (component instanceof JMSAddress) {
                populateJMSAddress((JMSAddress) component);
            } else if (component instanceof JMSBinding) {
                populateJMSBinding((JMSBinding) component, null);
            } else if (component instanceof Port) {
                Collection<JMSAddress> address = ((Port) component).
                        getExtensibilityElements(JMSAddress.class);
                if (!address.isEmpty()) {
                    populateJMSAddress(address.iterator().next());
                }
            } else if (component instanceof JMSMessage) {
                Object obj = ((JMSMessage)component).getParent();
                Binding parentBinding = null;
                if (obj instanceof BindingInput) {
                    BindingOperation parentOp =
                            (BindingOperation) ((BindingInput) obj).getParent();
                    parentBinding = (Binding) parentOp.getParent();
                } else if (obj instanceof BindingOutput) {
                    BindingOperation parentOp = (BindingOperation)
                            ((BindingOutput) obj).getParent();
                    parentBinding = (Binding) parentOp.getParent();
                }
                if (parentBinding != null) {
                    Collection<JMSBinding> bindings = parentBinding.
                            getExtensibilityElements(JMSBinding.class);
                    if (!bindings.isEmpty()) {
                        populateJMSBinding(bindings.iterator().next(), null);
                        bindingNameComboBox.
                                setSelectedItem(parentBinding.getName());
                    }
                }

            } else if (component instanceof JMSOperation) {
                Object obj = ((JMSOperation)component).getParent();
                if (obj instanceof BindingOperation) {
                    Binding parentBinding = (Binding)
                            ((BindingOperation)obj).getParent();
                    Collection<JMSBinding> bindings = parentBinding.
                            getExtensibilityElements(JMSBinding.class);
                    if (!bindings.isEmpty()) {
                        populateJMSBinding(bindings.iterator().next(), null);
                        bindingNameComboBox.setSelectedItem(parentBinding.getName());
                    }
                }
            }
        }
        updateTopicOnlySection();
    }

    private void populateJMSAddress(JMSAddress jmsAddress) {
        Port port = (Port) jmsAddress.getParent();
        Binding binding = port.getBinding().get();
        Collection<JMSBinding> bindings = binding.
                getExtensibilityElements(JMSBinding.class);
        if (!bindings.isEmpty()) {
            populateJMSBinding(bindings.iterator().next(), jmsAddress);
        }
        bindingNameComboBox.setSelectedItem(binding.getName());
        portTypeComboBox.addItem(port.getName());
    }

    private void populateJMSBinding(JMSBinding jmsBinding,
            JMSAddress jmsAddress) {
        if (jmsAddress == null) {
            servicePortComboBox.setEnabled(true);
            jmsAddress = getJMSAddress(jmsBinding);
        }
        if (jmsAddress == null) {
            return;
        }
        Port port = (Port) jmsAddress.getParent();

        // need to populate with all service ports that uses this binding
        populateListOfPorts(jmsBinding);
        servicePortComboBox.setSelectedItem(port);

        // from Binding, need to allow changing of Port
        bindingNameComboBox.setEditable(false);
        bindingNameComboBox.setEnabled(false);

        if (jmsBinding != null) {
            populateListOfBindings(jmsBinding);
            populateListOfPortTypes(jmsBinding);
            Binding binding = (Binding) jmsBinding.getParent();
            bindingNameComboBox.setSelectedItem(binding.getName());
            NamedComponentReference<PortType> pType = binding.getType();
            PortType portType = pType.get();
            portTypeComboBox.addItem(portType.getName());

            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            populateOperationBox(bindingOperations);

            operationNameComboBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent evt) {
                    // based on selected operation, populate messages
                    operationNameComboBoxItemStateChanged(evt);
                }
            });
            populatePartBoxes(binding, bindingOperations);
            // select the 1st item since this is not a configurable param
            operationNameComboBox.setSelectedIndex(0);
            if (operationNameComboBox.getItemCount() == 1) {
                // need to implicitly call update on messages because above
                // listener will not change selection if only 1 item
                if (binding != null) {
                    JMSMessage inputMessage = getInputJMSMessage(binding,
                            operationNameComboBox.getSelectedItem().toString());
                    updateInputMessageView(inputMessage);

                }
            }
            updateGeneralView(jmsAddress, bindingOperations);
        }
    }

    private void updateInputMessageView(JMSMessage inputJMSMessage) {
        if (inputJMSMessage != null) {
//TODO
//            inputTypeComboBox.setSelectedItem(inputJMSMessage.getMessageType());
//            inputTextComboBox.setSelectedItem(inputJMSMessage.getTextPart());

            // TODO
//            if (inputJMSMessage.getUse() != null) {
//                if (inputJMSMessage.getUse().equals(JMSConstants.ENCODED)) {
//                    inputEncodingRBtn.setSelected(true);
//                } else {
//                    inputEncodingRBtn.setSelected(false);
//                }
//            } else {
//                inputEncodingRBtn.setSelected(false);
//            }
//            inputEncodingStyleTextField.setText(
//                    inputJMSMessage.getJMSEncodingStyle());
        }
    }

    private void populateOperationBox(Collection bindingOps) {
        Iterator iter = bindingOps.iterator();
        while (iter.hasNext()) {
            BindingOperation bop = (BindingOperation) iter.next();
            operationNameComboBox.addItem(bop.getName());
        }
    }

    JMSAddress getJMSAddress(JMSBinding jmsBinding) {
        JMSAddress jmsAddress = null;
        if ((jmsBinding != null) && (jmsBinding.getParent() != null)) {
            Binding parentBinding = (Binding) jmsBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<Service> services = defs.getServices().iterator();
            String bindingName = parentBinding.getName();
            while (services.hasNext()) {
                Iterator<Port> ports = services.next().getPorts().iterator();
                while (ports.hasNext()) {
                    Port port = ports.next();
                    if(port.getBinding() != null) {
                        Binding binding = port.getBinding().get();

                        if ((binding != null) && (binding.getName().
                                equals(bindingName))) {
                            Iterator<JMSAddress> jmsAddresses = port.
                                    getExtensibilityElements(JMSAddress.class).
                                    iterator();
                            // 1 jmsaddress for 1 binding
                            while (jmsAddresses.hasNext()) {
                                return jmsAddresses.next();
                            }
                        }
                    }
                }
            }
        }
        return jmsAddress;
    }

    private JMSMessage getInputJMSMessage(Binding binding,
            String selectedOperation) {
        JMSMessage inputJMSMessage = null;
        if (binding != null) {
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(selectedOperation)) {
                    BindingInput bi = bop.getBindingInput();
                    List<JMSMessage> inputJMSMessages =
                            bi.getExtensibilityElements(JMSMessage.class);
                    if (inputJMSMessages.size() > 0) {
                        inputJMSMessage = inputJMSMessages.get(0);
                        break;
                    }
                }
            }
        }
        return inputJMSMessage;
    }

    private JMSMessage getOutputJMSMessage(Binding binding,
            String selectedOperation) {
        JMSMessage outputJMSMessage = null;
        if (binding != null) {
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(selectedOperation)) {
                    BindingOutput bo = bop.getBindingOutput();
                    List<JMSMessage> outputJMSMessages =
                            bo.getExtensibilityElements(JMSMessage.class);
                    if (outputJMSMessages.size() > 0) {
                        outputJMSMessage = outputJMSMessages.get(0);
                        break;
                    }
                }
            }
        }
        return outputJMSMessage;
    }

    private void updateGeneralView(JMSAddress jmsAddress,
            Collection<BindingOperation> bindingOperations) {
        if (jmsAddress != null) {
        }
        if (bindingOperations != null) {
            for (BindingOperation bop : bindingOperations) {
                List<JMSOperation> jmsOpsList = bop.
                        getExtensibilityElements(JMSOperation.class);
                Iterator<JMSOperation> jmsOps =
                        jmsOpsList.iterator();
                // there should only be one jms:operation for the binding op
                if (jmsOpsList.size() > 0) {
                    JMSOperation jmsOp = jmsOps.next();
                    if (jmsOp != null) {
                        // get Destination
                        destinationTextField.setText(jmsOp.getAttribute(
                                JMSOperation.ATTR_DESTINATION));
                        // get Destination type
                        String dType = jmsOp.getAttribute(JMSOperation.
                                ATTR_DESTINATION_TYPE);
                        if (dType != null) {
                            if (dType.equals(JMSConstants.QUEUE)) {
                                queueRBtn.setSelected(true);
                            } else {
                                topicRBtn.setSelected(true);
                            }
                        }
                        // get Transactino type

                        if (JMSConstants.TRANSACTION_XA.equals(jmsOp.getTransaction())) {
                            xaTransactionBox.setSelected(true);
                        } else {
                            xaTransactionBox.setSelected(false);
                        }
//                        transactionComboBox.setSelectedItem(jmsOp.
//                                getAttribute(JMSOperation.ATTR_TRANSACTION));
                        clientIdTextField.setText(jmsOp.
                                getAttribute(JMSOperation.ATTR_CLIENT_ID));

                        subscriptionNameTextField.setText(jmsOp.getAttribute(
                                JMSOperation.ATTR_SUBSCRIPTION_NAME));

                        //batchSizeTextField.setText(jmsOp.
                        //        getAttribute(JMSOperation.ATTR_BATCH_SZIE));
                        //valMsgSelComboBox.setSelectedItem(jmsOp.
                        //        getAttribute(JMSOperation.ATTR_));

//                        batchSizeSpinner.setValue(jmsOp.getBatchSize());
                        if (jmsOp.getSubscriptionDurability() != null) {
                            if (jmsOp.getSubscriptionDurability().equals(JMSConstants.DURABLE)) {
                                durableRadioButton.setSelected(true);
                            } else {
                                nondurableRadioButton.setSelected(true);
                            }
                        } else {
                            nondurableRadioButton.setSelected(true);
                            subscriptionNameTextField.setEnabled(false);
                        }

                    }
                }
            }
        }
        
        updateTopicOnlySection();
        updateDurability();
    }

    private void populatePartBoxes(Binding binding,
            Collection<BindingOperation> bindingOperations) {
        if (bindingOperations != null) {
            Collection inputTextParts = new ArrayList();
            Collection outputTextParts = new ArrayList();
            for (BindingOperation bop : bindingOperations) {

                // get the input text part
                inputTextParts = getInputParts(binding, bop.getName());

//                // get the output text part
//                outputTextParts = getOutputParts(binding, bop.getName());

                // populate text part
                populateInputPartComboBox(inputTextParts);

//                // populate text part
//                populateOutputPartComboBox(outputTextParts);

                BindingInput bi = bop.getBindingInput();
                List<JMSMessage> inputJMSMessages =
                        bi.getExtensibilityElements(JMSMessage.class);
                if ((inputJMSMessages != null) &&
                        (inputJMSMessages.size() > 0)) {
                    JMSMessage inputJMSMessage = inputJMSMessages.get(0);
// TODO
//                    // get input message type
//                    inputTypeComboBox.setSelectedItem(inputJMSMessage.
//                            getMessageType());
//                    // get input text
//                    inputTextComboBox.setSelectedItem(inputJMSMessage.
//                            getTextPart());
                }

//                BindingOutput bo = bop.getBindingOutput();
//                List<JMSMessage> outputJMSMessages =
//                        bo.getExtensibilityElements(JMSMessage.class);
//                if ((outputJMSMessages != null) &&
//                        (outputJMSMessages.size() > 0)) {
//                    JMSMessage outputJMSMessage = outputJMSMessages.get(0);
//// TODO
////                    // get output message type
////                    outputTypeComboBox.setSelectedItem(outputJMSMessage.
////                            getMessageType());
////
////                    // get output text
////                    outputTextComboBox.setSelectedItem(outputJMSMessage.
////                            getTextPart());
//                }
            }
        }
    }

    private Collection getInputParts(Binding binding, String opName) {
        Collection<String> inputParts = new ArrayList<String>();
        if (binding != null) {
            NamedComponentReference<PortType> pType = binding.getType();
            PortType type = pType.get();
            Collection ops = type.getOperations();
            Iterator iter = ops.iterator();
            inputParts.add(JMSConstants.NOT_SET);
            while(iter.hasNext()) {
                Operation op = (Operation) iter.next();
                if ((op != null) && (op.getName().equals(opName))) {
                    Input input = op.getInput();
                    NamedComponentReference<Message> messageIn = input.getMessage();
                    Message msgIn = messageIn.get();
                    if (msgIn != null) {
                        Collection parts = msgIn.getParts();
                        Iterator partIter = parts.iterator();
                        while (partIter.hasNext()) {
                            Part part = (Part) partIter.next();
                            inputParts.add(part.getName());
                        }
                    }
                }
            }
        }
        return inputParts;
    }

    private Collection getOutputParts(Binding binding, String opName) {
        Collection<String> outputParts = new ArrayList<String>();
        if (binding != null) {
            NamedComponentReference<PortType> pType = binding.getType();
            PortType type = pType.get();
            Collection ops = type.getOperations();
            Iterator iter = ops.iterator();
            outputParts.add(JMSConstants.NOT_SET);
            while(iter.hasNext()) {
                Operation op = (Operation) iter.next();
                if ((op != null) && (op.getName().equals(opName))) {
                    Output output = op.getOutput();
                    NamedComponentReference<Message> messageOut = output.getMessage();
                    Message msgOut = messageOut.get();
                    if (msgOut != null) {
                        Collection parts = msgOut.getParts();
                        Iterator partIter = parts.iterator();
                        while (partIter.hasNext()) {
                            Part part = (Part) partIter.next();
                            outputParts.add(part.getName());
                        }
                    }
                }
            }
        }
        return outputParts;
    }

    private void populateInputPartComboBox(Collection textItems) {
        if ((textItems != null) && (textItems.size() > 0)) {
            Iterator iter = textItems.iterator();
            while(iter.hasNext()) {
                String partName = (String) iter.next();
// TODO
//                inputTextComboBox.addItem(partName);
 
            }
        }
    }

    private void populateOutputPartComboBox(Collection textItems) {
        if ((textItems != null) && (textItems.size() > 0)) {
            Iterator iter = textItems.iterator();
            while(iter.hasNext()) {
                String partName = (String) iter.next();
// TODO                
//                outputTextComboBox.addItem(partName);

            }
        }
    }

    private void populateListOfPortTypes(JMSBinding jmsBinding) {
        if ((jmsBinding != null) && (jmsBinding.getParent() != null)) {
            Binding parentBinding = (Binding) jmsBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<PortType> portTypes = defs.getPortTypes().iterator();
            List<PortType> filePortTypes = null;
            while (portTypes.hasNext()) {
                PortType portType = portTypes.next();
                portTypeComboBox.addItem(portType.getName());
            }
        }
    }

    private void populateListOfPorts(JMSBinding jmsBinding) {
            Vector<Port> portV = new Vector<Port>();

        if ((jmsBinding != null) && (jmsBinding.getParent() != null)) {
            Binding parentBinding = (Binding) jmsBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<Service> services = defs.getServices().iterator();
            String bindingName = parentBinding.getName();
            boolean found = false;
            while (services.hasNext()) {
                Iterator<Port> ports = services.next().getPorts().iterator();
                while (ports.hasNext()) {
                    Port port = ports.next();
                    if(port.getBinding() != null) {
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

   private void populateListOfBindings(JMSBinding jmsBinding) {
        if ((jmsBinding != null) && (jmsBinding.getParent() != null)) {
            Binding parentBinding = (Binding) jmsBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<Binding> bindings = defs.getBindings().iterator();
            List<JMSBinding> jmsBindings = null;

            while (bindings.hasNext()) {
                Binding binding = bindings.next();
                if (binding.getType() == null
                        || binding.getType().get() == null) {
                    continue;
                }

                jmsBindings = binding.
                        getExtensibilityElements(JMSBinding.class);
                if (jmsBindings != null) {
                    Iterator iter = jmsBindings.iterator();
                    while (iter.hasNext()) {
                        JMSBinding b = (JMSBinding) iter.next();
                        Binding fBinding = (Binding) b.getParent();
                        bindingNameComboBox.addItem(fBinding.getName());
                    }
                }
            }
        }
    }

    JMSAddress getJMSAddressPerSelectedPort() {
        JMSAddress address = null;
        Port selectedServicePort = (Port) servicePortComboBox.getSelectedItem();
        if (selectedServicePort != null) {
            Binding binding = selectedServicePort.getBinding().get();
            String selBindingName = bindingNameComboBox.
                    getSelectedItem().toString();
            if ((binding != null) && (binding.getName().
                    equals(selBindingName))) {
                Iterator<JMSAddress> jmsAddresses = selectedServicePort.
                        getExtensibilityElements(JMSAddress.class).
                        iterator();
                // 1 fileaddress for 1 binding
                while (jmsAddresses.hasNext()) {
                    return jmsAddresses.next();
                }
            }
        }
        return address;
    }

    private void cleanUp() {
        // clean up listeners TODO
        // null out data TODO
    }

    private void updateDescriptionArea(FocusEvent evt) {
        if (mDescPanel != null) {
            mDescPanel.setText("");
        }

        String[] desc = null;
        boolean casaEdited = false;

        if (evt.getSource() == destinationTextField) {
            desc = new String[]{"Destination\n\n",
                   destinationTextField.getToolTipText()}; 
        } else if (evt.getSource() == queueRBtn) {
            desc = new String[]{"Destination Type\n\n",
                   queueRBtn.getToolTipText()}; 
        } else if (evt.getSource() == topicRBtn) {
            desc = new String[]{"Destination Type\n\n",
                   topicRBtn.getToolTipText()}; 
        } else if (evt.getSource() == xaTransactionBox) {
            desc = new String[]{"Transaction\n\n",
                   xaTransactionBox.getToolTipText()}; 
        } else if (evt.getSource() == subscriptionNameTextField) {
            desc = new String[]{"Subcription Name\n\n",
                   subscriptionNameTextField.getToolTipText()}; 
        } else if (evt.getSource() == durableRadioButton) {
            desc = new String[]{"Subscription Durability\n\n",
                   durableRadioButton.getToolTipText()}; 
        } else if (evt.getSource() == nondurableRadioButton) {
            desc = new String[]{"Subscription Durability\n\n",
                   nondurableRadioButton.getToolTipText()}; 
        } else if (evt.getSource() == clientIdTextField) {
            desc = new String[]{"Client ID\n\n",
                   clientIdTextField.getToolTipText()}; 
        }    
        if (desc != null) {
            if (mDescPanel != null) {
                mDescPanel.setText(desc[0], desc[1]);
            }
            return;
        }
    }    
    
    public FileError validateMe() {  
        return validateMe(false);
    }
    
    public FileError validateMe(boolean fireEvent) {  
        // validate user/password
        FileError fileError = new FileError();
        if ((durableRadioButton.isSelected()) && (durableRadioButton.isEnabled()) && (getSubscriptionName() == null)) {
            fileError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_ERROR_EVT);
            fileError.setErrorMessage(NbBundle.getMessage(JMSConnectionPanel.class, 
                    "JMSDestinationPanel.SubscriptionNameEmpty"));
        }
        
        if (fireEvent) {
            ErrorPropagator.doFirePropertyChange(fileError.getErrorMode(), null,
                    fileError.getErrorMessage(), this);
        }      
        return fileError;        
    }
    
    protected boolean validateContent() {
        ValidationResult results = new JMSComponentValidator().
                validate(mComponent.getModel(), null, ValidationType.COMPLETE);
        Collection<ResultItem> resultItems = results.getValidationResult();
        ResultItem firstResult = null;
        String type = ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_ERROR_EVT;
        boolean result = true;
        if (resultItems != null && !resultItems.isEmpty()) {
            for (ResultItem item : resultItems) {
                if (item.getType() == ResultType.ERROR) {
                    firstResult = item;
                    type = ExtensibilityElementConfigurationEditorComponent.
                            PROPERTY_ERROR_EVT;
                    result = false;
                    break;
                } else if (firstResult == null) {
                    firstResult = item;
                    type = ExtensibilityElementConfigurationEditorComponent.
                            PROPERTY_WARNING_EVT;
                }
            }
        }
        if (firstResult != null) {
            firePropertyChange(type, null, firstResult.getDescription());
            return result;
        } else {
            firePropertyChange(ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_CLEAR_MESSAGES_EVT, null, null);
            return true;
        }
    }   

    private void updateTopicOnlySection() {
        subscriptionLabel.setEnabled(topicRBtn.isSelected());
        durableRadioButton.setEnabled(topicRBtn.isSelected());
        nondurableRadioButton.setEnabled(topicRBtn.isSelected());        
        clientIdLabel.setEnabled(topicRBtn.isSelected());
        clientIdTextField.setEnabled(topicRBtn.isSelected());
        // only disable/enable IF correct toggle is enabled
        if ((durableRadioButton.isSelected() &&
                topicRBtn.isSelected())) {
            subscriptionNameTextField.setEnabled(true);
        } else {
            subscriptionNameTextField.setEnabled(false);
        }
        
    }
    
    private void updateDurability() {
        if (durableRadioButton.isEnabled() && durableRadioButton.isSelected()) {
            subscriptionNameTextField.setEnabled(true);
        }
        if (nondurableRadioButton.isEnabled() && nondurableRadioButton.isSelected()) {
            subscriptionNameTextField.setEnabled(false);
        }
        
        validateMe(true);
    }
    
    public class PortCellRenderer extends JLabel
            implements javax.swing.ListCellRenderer {

        public PortCellRenderer() {
            super();
            setOpaque(true);
        }

        public java.awt.Component getListCellRendererComponent(javax.swing.JList list,
                Object value, int index, boolean isSelected,
                boolean isFocused) {
            if ((value != null) && (value instanceof Port)) {
                setText(((Port) value).getName());
                setBackground(isSelected ?
                    list.getSelectionBackground() : list.getBackground());
                setForeground(isSelected ?
                    list.getSelectionForeground() : list.getForeground());
            }
            return this;
        }
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

    private void showTopicDetails() {
        if (mDetailsDlg == null) {
            mDetailsDlgDesc = new DialogDescriptor(
                    topicSectionPanel,
                    NbBundle.getMessage(JMSDestinationPanel.class,
                    "JMSBindingConfigurationPanel.TopicsOnlyTitle"),
                    true, null);
            mDetailsDlg = DialogDisplayer.getDefault().
                    createDialog(mDetailsDlgDesc);
        }
        mDetailsDlg.setSize(topicSectionPanel.getPreferredSize());
        mDetailsDlg.setVisible(true);
        if (mDetailsDlgDesc.getValue() == DialogDescriptor.OK_OPTION) {
        } else {
        }
        mDetailsDlg.setVisible(false);
    }

    private void handleItemStateChanged(ItemEvent evt) {
        if ((evt.getSource() == queueRBtn) || (evt.getSource() == topicRBtn)) {
            updateTopicOnlySection();
            validateMe(true);
        } else if ((evt.getSource() == durableRadioButton) ||
                (evt.getSource() == nondurableRadioButton)) {
            updateDurability();
        }
    }

    private void handleActionPerformed(ActionEvent evt) {

    }

    public class MyItemListener implements ItemListener {
        public void itemStateChanged(java.awt.event.ItemEvent evt) {
            handleItemStateChanged(evt);
        }
    }

    public class MyActionListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            handleActionPerformed(evt);
        }
    }
    
    public class MyDocumentListener implements DocumentListener {
	// Handle insertions into the text field
        public void insertUpdate(DocumentEvent event) {
            validateMe(true);
        }

	// Handle deletions	from the text field
        public void removeUpdate(DocumentEvent event) {
            validateMe(true);
        }

	// Handle changes to the text field
        public void changedUpdate(DocumentEvent event) {
            // empty
        }             
    }    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup QTopicBGroup;
    private javax.swing.JComboBox bindingNameComboBox;
    private javax.swing.JLabel bindingNameLabel;
    private javax.swing.JLabel clientIdLabel;
    private javax.swing.JTextField clientIdTextField;
    private javax.swing.JPanel connectionPanel;
    private javax.swing.JLabel destinationLab;
    private javax.swing.JLabel destinationSectionLab;
    private javax.swing.JTextField destinationTextField;
    private javax.swing.JLabel destinationTypeLab;
    private javax.swing.ButtonGroup durableBtnGrp;
    private javax.swing.JRadioButton durableRadioButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JRadioButton nondurableRadioButton;
    private javax.swing.JComboBox operationNameComboBox;
    private javax.swing.JLabel operationNameLabel;
    private javax.swing.JPanel portBindingPanel;
    private javax.swing.JComboBox portTypeComboBox;
    private javax.swing.JLabel portTypeLabel;
    private javax.swing.JRadioButton queueRBtn;
    private javax.swing.JComboBox servicePortComboBox;
    private javax.swing.JLabel subscriptionLabel;
    private javax.swing.JTextField subscriptionNameTextField;
    private javax.swing.JPanel topicDetailsPanel;
    private javax.swing.JRadioButton topicRBtn;
    private javax.swing.JPanel topicSectionPanel;
    private javax.swing.JCheckBox xaTransactionBox;
    // End of variables declaration//GEN-END:variables

}
