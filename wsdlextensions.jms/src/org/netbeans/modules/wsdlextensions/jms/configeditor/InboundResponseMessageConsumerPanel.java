/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
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
public class InboundResponseMessageConsumerPanel extends javax.swing.JPanel {

    private WSDLComponent mComponent;

    /** QName **/
    private QName mQName;

    /** resource bundle for file bc **/
    private ResourceBundle mBundle = ResourceBundle.getBundle(
            "org.netbeans.modules.wsdlextensions.jms.resources.Bundle");

    private static final Logger mLogger = Logger.
            getLogger(InboundResponseMessageConsumerPanel.class.getName());

    private DescriptionPanel descPanel = null;
    private JMSConsumerPanel consumerPanel = null;
    private Message mInputMessage = null;
    private Message mOutputMessage = null;
    private JMSMessage mInputJMSMessage = null;
    private Dialog mDetailsDlg = null;
    private DialogDescriptor mDetailsDlgDesc = null;    
    private JMSDestinationPanel destinationPanel = null;
    
    /** Creates new form JMSBindingConfigurationPanel */
    public InboundResponseMessageConsumerPanel(QName qName, WSDLComponent component) {
        mQName = qName;
        mComponent =component;
        initComponents();
        initCustomComponents();
        populateView(mQName, mComponent);
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
        consumerPanel.populateView(qName, component);
        consumerPanel.setDescriptionPanel(descPanel);
        //initListeners();
    }
    
    /**
     * Route the property change event to this panel
     */
    public void doFirePropertyChange(String name, Object oldValue, Object newValue) {           
        firePropertyChange(name, oldValue, 
                newValue);         
    }  

    @Override
    public String getName() {
        return "Request Configuration";
    }
    
    /**
     * Return the operation name
     * @return String operation name
     */
    String getOperationName() {
        if ((operationNameComboBox.getSelectedItem() != null) &&
                (!operationNameComboBox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
            return operationNameComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    /**
     * Return the message type for input message
     * @return String message type
     */
    String getOutputMessageType() {
        return (String) outputTypeComboBox.getSelectedItem();

    }

    /**
     * Return the message text for input message
     * @return String message text
     */
    String getOutputMessageText() {
        if ((outputTextComboBox.getSelectedItem() != null) &&
                (!outputTextComboBox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
            return outputTextComboBox.getSelectedItem().toString();
        } else {
            return null;

        }
    }

    /**
     * Return the message selector value
     * @return String message selector
     */
    String getMessageSelector() {
        return consumerPanel.getMessageSelector();
    }

    /**
     * Return the concurrency mode value
     * @return String concurrency mode
     */
    String getConcurrencyMode() {
        if ((concurModeComboBox.getSelectedItem() != null) &&
                (!concurModeComboBox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
            return concurModeComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }
       
    /**
     * Return delivery mode
     * @return
     */
    String getDeliveryMode() {
        return consumerPanel.getDeliveryMode();
    }    

    /**
     * Return the Redeliver value
     * @return String redelivery
     */
    String getRedelivery() {
        return consumerPanel.getRedelivery();
    }

    /**
     * Return the batch size value
     * @return long batch size
     */
    long getBatchSize() {
        return consumerPanel.getBatchSize();
    }

    String getMaxConcurrentConsumer() {
        return consumerPanel.getMaxConcurrentConsumer();
    }

    String getInputDeliveryModePart() {
//        if ((inputDeliveryModePartComboBox.getSelectedItem() != null) &&
//                (!inputDeliveryModePartComboBox.getSelectedItem().toString().
//                equals(JMSConstants.NOT_SET))) {
//            return inputDeliveryModePartComboBox.getSelectedItem().toString();
//        } else {
//            return null;
//        }
        // TODO
        return "";
    }

    String getInputPriorityPart() {
//        if ((inputPriorityPartComboBox.getSelectedItem() != null) &&
//                (!inputPriorityPartComboBox.getSelectedItem().toString().
//                equals(JMSConstants.NOT_SET))) {
//            return inputPriorityPartComboBox.getSelectedItem().toString();
//        } else {
//            return null;
//        }
         // TODO
        return "";
    }

    String getOutputDeliveryModePart() {
//        if ((outputDeliveryModePartComboBox.getSelectedItem() != null) &&
//                (!outputDeliveryModePartComboBox.getSelectedItem().toString().
//                equals(JMSConstants.NOT_SET))) {
//            return outputDeliveryModePartComboBox.getSelectedItem().toString();
//        } else {
//            return null;
//        }
        // TODO
        return "";
    }

    String getOutputPriorityPart() {
//        if ((outputPriorityPartComboBox.getSelectedItem() != null) &&
//                (!outputPriorityPartComboBox.getSelectedItem().toString().
//                equals(JMSConstants.NOT_SET))) {
//            return outputPriorityPartComboBox.getSelectedItem().toString();
//        } else {
//            return null;
//        }
        // TODO
        return "";
    }

    /**
     * Set the operation name to be configured
     * @param opName
     */
    void setOperationName(String opName) {
        if (opName != null) {
            operationNameComboBox.setSelectedItem(opName);
            if (consumerPanel != null) {
                consumerPanel.setOperationName(opName);
            }
        }
    }
    
    private void initCustomComponents() {
        consumerPanel = new JMSConsumerPanel(null, null); 
        GridBagConstraints gridBagConstraints = new GridBagConstraints();        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        inputMessagesPanelTab.add(consumerPanel, gridBagConstraints);  
        
        this.getAccessibleContext().setAccessibleName(getName());
        this.getAccessibleContext().setAccessibleDescription(getName());
        
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

        jndiSectionPanel = new javax.swing.JPanel();
        jndiLabel = new javax.swing.JLabel();
        connectionFactoryNameLab = new javax.swing.JLabel();
        initialContextFactoryLab = new javax.swing.JLabel();
        providerURLLab = new javax.swing.JLabel();
        securityPrincipalLab = new javax.swing.JLabel();
        securityCredentialLab = new javax.swing.JLabel();
        securityCrendentialsTextField = new javax.swing.JTextField();
        securityPrincipalTextField = new javax.swing.JTextField();
        providerURLTextField = new javax.swing.JTextField();
        initialContextFactoryTextField = new javax.swing.JTextField();
        connectionFactoryNameTextField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jTextField1 = new javax.swing.JTextField();
        inputCorrelationPartComboBox = new javax.swing.JComboBox();
        inputMsgIDPartComboBox = new javax.swing.JComboBox();
        inputRedeliveredPartComboBox = new javax.swing.JComboBox();
        inputTimestampPartComboBox = new javax.swing.JComboBox();
        inputTypePartComboBox = new javax.swing.JComboBox();
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
        inputEncodingGroup = new javax.swing.ButtonGroup();
        outputEncodingGroup = new javax.swing.ButtonGroup();
        QTopicBGroup = new javax.swing.ButtonGroup();
        durabilityBGrp = new javax.swing.ButtonGroup();
        concurModeComboBox = new javax.swing.JComboBox();
        deliveryModeBtnGrp = new javax.swing.ButtonGroup();
        outputTypeComboBox = new javax.swing.JComboBox();
        outputTextComboBox = new javax.swing.JComboBox();
        jSplitPane1 = new javax.swing.JSplitPane();
        inputMessagesPanelTab = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        descriptionPanel = new javax.swing.JPanel();

        jndiSectionPanel.setName("jndiSectionPanel"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jndiLabel, org.openide.util.NbBundle.getMessage(InboundResponseMessageConsumerPanel.class, "InboundResponseMessageConsumerPanel.jndiLabel.text")); // NOI18N
        jndiLabel.setName("jndiLabel"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(connectionFactoryNameLab, org.openide.util.NbBundle.getMessage(InboundResponseMessageConsumerPanel.class, "InboundResponseMessageConsumerPanel.connectionFactoryNameLab.text")); // NOI18N
        connectionFactoryNameLab.setName("connectionFactoryNameLab"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(initialContextFactoryLab, org.openide.util.NbBundle.getMessage(InboundResponseMessageConsumerPanel.class, "InboundResponseMessageConsumerPanel.initialContextFactoryLab.text")); // NOI18N
        initialContextFactoryLab.setName("initialContextFactoryLab"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(providerURLLab, org.openide.util.NbBundle.getMessage(InboundResponseMessageConsumerPanel.class, "InboundResponseMessageConsumerPanel.providerURLLab.text")); // NOI18N
        providerURLLab.setName("providerURLLab"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(securityPrincipalLab, org.openide.util.NbBundle.getMessage(InboundResponseMessageConsumerPanel.class, "InboundResponseMessageConsumerPanel.securityPrincipalLab.text")); // NOI18N
        securityPrincipalLab.setName("securityPrincipalLab"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(securityCredentialLab, org.openide.util.NbBundle.getMessage(InboundResponseMessageConsumerPanel.class, "InboundResponseMessageConsumerPanel.securityCredentialLab.text")); // NOI18N
        securityCredentialLab.setName("securityCredentialLab"); // NOI18N

        securityCrendentialsTextField.setText(org.openide.util.NbBundle.getMessage(InboundResponseMessageConsumerPanel.class, "InboundResponseMessageConsumerPanel.securityCrendentialsTextField.text")); // NOI18N
        securityCrendentialsTextField.setName("securityCrendentialsTextField"); // NOI18N

        securityPrincipalTextField.setText(org.openide.util.NbBundle.getMessage(InboundResponseMessageConsumerPanel.class, "InboundResponseMessageConsumerPanel.securityPrincipalTextField.text")); // NOI18N
        securityPrincipalTextField.setName("securityPrincipalTextField"); // NOI18N

        providerURLTextField.setText(org.openide.util.NbBundle.getMessage(InboundResponseMessageConsumerPanel.class, "InboundResponseMessageConsumerPanel.providerURLTextField.text")); // NOI18N
        providerURLTextField.setName("providerURLTextField"); // NOI18N

        initialContextFactoryTextField.setText(org.openide.util.NbBundle.getMessage(InboundResponseMessageConsumerPanel.class, "InboundResponseMessageConsumerPanel.initialContextFactoryTextField.text")); // NOI18N
        initialContextFactoryTextField.setName("initialContextFactoryTextField"); // NOI18N
        initialContextFactoryTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                initialContextFactoryTextFieldActionPerformed(evt);
            }
        });

        connectionFactoryNameTextField.setText(org.openide.util.NbBundle.getMessage(InboundResponseMessageConsumerPanel.class, "InboundResponseMessageConsumerPanel.connectionFactoryNameTextField.text")); // NOI18N
        connectionFactoryNameTextField.setName("connectionFactoryNameTextField"); // NOI18N

        jSeparator1.setName("jSeparator1"); // NOI18N

        org.jdesktop.layout.GroupLayout jndiSectionPanelLayout = new org.jdesktop.layout.GroupLayout(jndiSectionPanel);
        jndiSectionPanel.setLayout(jndiSectionPanelLayout);
        jndiSectionPanelLayout.setHorizontalGroup(
            jndiSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jndiSectionPanelLayout.createSequentialGroup()
                .add(jndiSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jndiSectionPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jndiLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 528, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jndiSectionPanelLayout.createSequentialGroup()
                        .add(20, 20, 20)
                        .add(jndiSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(connectionFactoryNameLab)
                            .add(securityCredentialLab)
                            .add(initialContextFactoryLab)
                            .add(providerURLLab)
                            .add(securityPrincipalLab))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jndiSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jndiSectionPanelLayout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jndiSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, securityPrincipalTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, providerURLTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)
                                    .add(securityCrendentialsTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)))
                            .add(initialContextFactoryTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)
                            .add(connectionFactoryNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jndiSectionPanelLayout.setVerticalGroup(
            jndiSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jndiSectionPanelLayout.createSequentialGroup()
                .add(jndiSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jndiSectionPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jndiLabel))
                    .add(jndiSectionPanelLayout.createSequentialGroup()
                        .add(18, 18, 18)
                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jndiSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(connectionFactoryNameLab)
                    .add(connectionFactoryNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jndiSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(initialContextFactoryLab)
                    .add(initialContextFactoryTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jndiSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(providerURLTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(providerURLLab))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jndiSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(securityPrincipalTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(securityPrincipalLab))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jndiSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(securityCredentialLab)
                    .add(securityCrendentialsTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        jTextField1.setText(org.openide.util.NbBundle.getMessage(InboundResponseMessageConsumerPanel.class, "InboundResponseMessageConsumerPanel.jTextField1.text_1")); // NOI18N
        jTextField1.setName("jTextField1"); // NOI18N

        inputCorrelationPartComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        inputCorrelationPartComboBox.setName("inputCorrelationPartComboBox"); // NOI18N

        inputMsgIDPartComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        inputMsgIDPartComboBox.setName("inputMsgIDPartComboBox"); // NOI18N
        inputMsgIDPartComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputMsgIDPartComboBoxActionPerformed(evt);
            }
        });

        inputRedeliveredPartComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        inputRedeliveredPartComboBox.setName("inputRedeliveredPartComboBox"); // NOI18N

        inputTimestampPartComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        inputTimestampPartComboBox.setName("inputTimestampPartComboBox"); // NOI18N
        inputTimestampPartComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputTimestampPartComboBoxActionPerformed(evt);
            }
        });

        inputTypePartComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        inputTypePartComboBox.setName("inputTypePartComboBox"); // NOI18N

        portBindingPanel.setName("portBindingPanel"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel26, org.openide.util.NbBundle.getMessage(InboundResponseMessageConsumerPanel.class, "InboundResponseMessageConsumerPanel.jLabel26.text")); // NOI18N
        jLabel26.setName("jLabel26"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(portTypeLabel, org.openide.util.NbBundle.getMessage(InboundResponseMessageConsumerPanel.class, "InboundResponseMessageConsumerPanel.portTypeLabel.text")); // NOI18N
        portTypeLabel.setName("portTypeLabel"); // NOI18N

        operationNameComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        operationNameComboBox.setName("operationNameComboBox"); // NOI18N
        operationNameComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                operationNameComboBoxItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel42, org.openide.util.NbBundle.getMessage(InboundResponseMessageConsumerPanel.class, "InboundResponseMessageConsumerPanel.jLabel42.text")); // NOI18N
        jLabel42.setName("jLabel42"); // NOI18N

        bindingNameComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        bindingNameComboBox.setName("bindingNameComboBox"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(operationNameLabel, org.openide.util.NbBundle.getMessage(InboundResponseMessageConsumerPanel.class, "InboundResponseMessageConsumerPanel.operationNameLabel.text_1")); // NOI18N
        operationNameLabel.setName("operationNameLabel"); // NOI18N

        servicePortComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        servicePortComboBox.setName("servicePortComboBox"); // NOI18N
        servicePortComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                servicePortComboBoxItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(bindingNameLabel, org.openide.util.NbBundle.getMessage(InboundResponseMessageConsumerPanel.class, "InboundResponseMessageConsumerPanel.bindingNameLabel.text")); // NOI18N
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

        concurModeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        concurModeComboBox.setName("concurModeComboBox"); // NOI18N

        outputTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        outputTypeComboBox.setName("outputTypeComboBox"); // NOI18N

        outputTextComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        outputTextComboBox.setName("outputTextComboBox"); // NOI18N
        outputTextComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                outputTextComboBoxItemStateChanged(evt);
            }
        });

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(1.0);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        inputMessagesPanelTab.setName("inputMessagesPanelTab"); // NOI18N
        inputMessagesPanelTab.setLayout(new java.awt.GridBagLayout());

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
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        inputMessagesPanelTab.add(jPanel1, gridBagConstraints);

        jSplitPane1.setTopComponent(inputMessagesPanelTab);

        descriptionPanel.setMinimumSize(new java.awt.Dimension(400, 50));
        descriptionPanel.setName("descriptionPanel"); // NOI18N
        descriptionPanel.setPreferredSize(new java.awt.Dimension(400, 75));
        descriptionPanel.setLayout(new java.awt.BorderLayout());
        descPanel = new DescriptionPanel();
        descriptionPanel.add(descPanel, java.awt.BorderLayout.CENTER);
        jSplitPane1.setBottomComponent(descriptionPanel);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

private void initialContextFactoryTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_initialContextFactoryTextFieldActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_initialContextFactoryTextFieldActionPerformed

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
                        updateServiceView(jmsAddresses.next());
                        break;
                    }
                }
            }
        }
    }
}//GEN-LAST:event_servicePortComboBoxItemStateChanged

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
            }
            if (binding != null) {
                
                // set the global mInputMessage
                Operation op = JMSUtilities.getOperation(binding,
                        operationNameComboBox.getSelectedItem().toString());
                if (op != null) {
                    Input input = op.getInput();
                    NamedComponentReference<Message> messageIn = input.getMessage();
                    mInputMessage = messageIn.get();

                    Output output = op.getOutput();
                    NamedComponentReference<Message> messageOut = output.getMessage();
                    mOutputMessage = messageOut.get();  
                }        
                
                
                JMSMessage inputMessage = getInputJMSMessage(binding,
                        selectedOperation);
                updateInputMessageView(inputMessage);
            }
        }
    }
}//GEN-LAST:event_operationNameComboBoxItemStateChanged

private void inputTimestampPartComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputTimestampPartComboBoxActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_inputTimestampPartComboBoxActionPerformed

private void inputMsgIDPartComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputMsgIDPartComboBoxActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_inputMsgIDPartComboBoxActionPerformed

private void outputTextComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_outputTextComboBoxItemStateChanged
// TODO add your handling code here:
    validateTextPart();
}//GEN-LAST:event_outputTextComboBoxItemStateChanged



    private void resetView() {
        servicePortComboBox.setEnabled(false);
        servicePortComboBox.removeAllItems();
        bindingNameComboBox.removeAllItems();
//        transactionComboBox.removeAllItems();
        outputTypeComboBox.removeAllItems();
//        outputTypeComboBox.removeAllItems();
        outputTextComboBox.removeAllItems();
//        outputTextComboBox.removeAllItems();
        portTypeComboBox.removeAllItems();
        operationNameComboBox.removeAllItems();
//        valMsgSelComboBox.removeAllItems();
        concurModeComboBox.removeAllItems();
//        deliveryModeComboBox.removeAllItems();
//        disableMsgIdCombobox.removeAllItems();
//        priorityComboBox.removeAllItems();
//        disableMsgTimeCombobox.removeAllItems();
        inputCorrelationPartComboBox.removeAllItems();
//        inputDeliveryModePartComboBox.removeAllItems();
//        inputPriorityPartComboBox.removeAllItems();
        inputTypePartComboBox.removeAllItems();
        inputMsgIDPartComboBox.removeAllItems();
        inputRedeliveredPartComboBox.removeAllItems();
        inputTimestampPartComboBox.removeAllItems();
//        inputUseComboBox.removeAllItems();
//        outputCorrelationPartComboBox.removeAllItems();
//        outputDeliveryModePartComboBox.removeAllItems();
//        outputPriorityPartComboBox.removeAllItems();
//        outputTypePartComboBox.removeAllItems();
//        outputMsgIDPartComboBox.removeAllItems();
//        outputRedeliveredPartComboBox.removeAllItems();
//        outputTimestampPartComboBox.removeAllItems();
//
//        transactionComboBox.addItem(JMSConstants.TRANSACTION_NONE);
//        transactionComboBox.addItem(JMSConstants.TRANSACTION_LOCAL);
//        transactionComboBox.addItem(JMSConstants.TRANSACTION_XA);
        outputTypeComboBox.addItem(JMSConstants.MAP_MESSAGE);
        outputTypeComboBox.addItem(JMSConstants.TEXT_MESSAGE);
        outputTypeComboBox.addItem(JMSConstants.BYTES_MESSAGE);
//        outputTypeComboBox.addItem(JMSConstants.MAP_MESSAGE);
//        outputTypeComboBox.addItem(JMSConstants.MESSAGE_MESSAGE);
//        valMsgSelComboBox.addItem(JMSConstants.NOT_SET);
//        valMsgSelComboBox.addItem(JMSConstants.BOOLEAN_FALSE);
//        valMsgSelComboBox.addItem(JMSConstants.BOOLEAN_TRUE);
//        queueRBtn.setSelected(true);
        concurModeComboBox.addItem(JMSConstants.NOT_SET);
        concurModeComboBox.addItem(JMSConstants.CC);
        concurModeComboBox.addItem(JMSConstants.SYNC);
//        deliveryModeComboBox.addItem(JMSConstants.NOT_SET);
//        deliveryModeComboBox.addItem(JMSConstants.DELIVERYMODE_PERSISTENT);
//        deliveryModeComboBox.addItem(JMSConstants.DELIVERYMODE_NON_PERSISTENT);
//        priorityComboBox.addItem(JMSConstants.NOT_SET);
//        priorityComboBox.addItem(JMSConstants.PRIORITY_DEFAULT);
//        priorityComboBox.addItem(Integer.toString(JMSConstants.PRIORITY_0));
//        priorityComboBox.addItem(Integer.toString(JMSConstants.PRIORITY_1));
//        priorityComboBox.addItem(Integer.toString(JMSConstants.PRIORITY_2));
//        priorityComboBox.addItem(Integer.toString(JMSConstants.PRIORITY_3));
//        priorityComboBox.addItem(Integer.toString(JMSConstants.PRIORITY_4));
//        priorityComboBox.addItem(Integer.toString(JMSConstants.PRIORITY_5));
//        priorityComboBox.addItem(Integer.toString(JMSConstants.PRIORITY_6));
//        priorityComboBox.addItem(Integer.toString(JMSConstants.PRIORITY_7));
//        priorityComboBox.addItem(Integer.toString(JMSConstants.PRIORITY_8));
//        priorityComboBox.addItem(Integer.toString(JMSConstants.PRIORITY_9));
//        disableMsgIdCombobox.addItem(JMSConstants.NOT_SET);
//        disableMsgIdCombobox.addItem(JMSConstants.BOOLEAN_FALSE);
//        disableMsgIdCombobox.addItem(JMSConstants.BOOLEAN_TRUE);
//        disableMsgTimeCombobox.addItem(JMSConstants.NOT_SET);
//        disableMsgTimeCombobox.addItem(JMSConstants.BOOLEAN_FALSE);
//        disableMsgTimeCombobox.addItem(JMSConstants.BOOLEAN_TRUE);
//        inputLiteralRBtn.setSelected(true);
//        outputLiteralRBtn.setSelected(true);
//        inputUseComboBox.addItem(JMSConstants.NOT_SET);
//        inputUseComboBox.addItem(JMSConstants.LITERAL);
//        inputUseComboBox.addItem(JMSConstants.ENCODED);
//        outputUseComboBox.addItem(JMSConstants.NOT_SET);
//        outputUseComboBox.addItem(JMSConstants.LITERAL);
//        outputUseComboBox.addItem(JMSConstants.ENCODED);
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
    }

    private void populateJMSAddress(JMSAddress jmsAddress) {
        updateServiceView(jmsAddress);
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

        updateServiceView(jmsAddress);
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

//            operationNameComboBox.addItemListener(new ItemListener() {
//                public void itemStateChanged(ItemEvent evt) {
//                    // based on selected operation, populate messages
//                    operationNameComboBoxItemStateChanged(evt);
//                }
//            });
            populatePartBoxes(binding, bindingOperations);
            
            // set the global mInputMessage
            Operation op = JMSUtilities.getOperation(binding,
                    operationNameComboBox.getSelectedItem().toString());
            if (op != null) {
                Input input = op.getInput();
                NamedComponentReference<Message> messageIn = input.getMessage();
                mInputMessage = messageIn.get();
                
                Output output = op.getOutput();
                NamedComponentReference<Message> messageOut = output.getMessage();
                mOutputMessage = messageOut.get();                
            }

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
            // TODO
            outputTypeComboBox.setSelectedItem(inputJMSMessage.getMessageType());
            outputTextComboBox.setSelectedItem(inputJMSMessage.getTextPart());
            
            // per mgmt,do not populate individually 08/14/08
//            inputCorrelationPartComboBox.setSelectedItem(
//                    inputJMSMessage.getCorrelationIdPart());
//            // TODO
////            inputDeliveryModePartComboBox.setSelectedItem(
////                    inputJMSMessage.getDeliveryModePart());
////            inputPriorityPartComboBox.setSelectedItem(
////                    inputJMSMessage.getPriorityPart());
//            inputTypePartComboBox.setSelectedItem(
//                    inputJMSMessage.getTypePart());
//            inputMsgIDPartComboBox.setSelectedItem(
//                    inputJMSMessage.getMessageIDPart());
//            inputRedeliveredPartComboBox.setSelectedItem(
//                    inputJMSMessage.getRedeliveredPart());
//            inputTimestampPartComboBox.setSelectedItem(
//                    inputJMSMessage.getTimestampPart());
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

//                        msgSelectorTextField.setText(jmsOp.getMessageSelector());
//                        concurModeComboBox.setSelectedItem(jmsOp.getAttribute(
//                                JMSOperation.ATTR_CONCURRENCY_MODE));
//                        redeliveryTextField.setText(jmsOp.getAttribute(
//                                JMSOperation.ATTR_REDELIVERY_HANDLING));
//                        batchSizeSpinner.setValue(jmsOp.getBatchSize());
//                        if (jmsOp.getMaxConcurrentConsumers() > -1) {
//                            maxConcurrentSpinner.setValue(jmsOp.
//                                getMaxConcurrentConsumers());
//                        } else {
//                            maxConcurrentSpinner.setValue(0);
//                        }
                    }
                }
            }
        }
    }

    private void populatePartBoxes(Binding binding,
            Collection<BindingOperation> bindingOperations) {
        if (bindingOperations != null) {
            Collection inputTextParts = new ArrayList();
            Collection outputTextParts = new ArrayList();
            for (BindingOperation bop : bindingOperations) {

                // get the input text part
                inputTextParts = getInputParts(binding, bop.getName());

                // get the output text part
                outputTextParts = getOutputParts(binding, bop.getName());

                // populate text part
                populateInputPartComboBox(inputTextParts);

                // populate text part
                populateOutputPartComboBox(outputTextParts);

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

                BindingOutput bo = bop.getBindingOutput();
                List<JMSMessage> outputJMSMessages =
                        bo.getExtensibilityElements(JMSMessage.class);
                if ((outputJMSMessages != null) &&
                        (outputJMSMessages.size() > 0)) {
                    JMSMessage outputJMSMessage = outputJMSMessages.get(0);
                   
                    // get output message type
                    outputTypeComboBox.setSelectedItem(outputJMSMessage.
                            getMessageType());

                    // get output text
                    outputTextComboBox.setSelectedItem(outputJMSMessage.
                            getTextPart());
                }
            }
        }
    }

    private void updateServiceView(JMSAddress jmsAddress) {
//        if (jmsAddress != null) {
//            connectionURLTextField.setText(jmsAddress.
//                    getAttribute(JMSAddress.ATTR_CONNECTION_URL));
//            updateJNDISection();
//            userNameTextField.setText(jmsAddress.
//                    getAttribute(JMSAddress.ATTR_USERNAME));
//            passwordTextField.setText(jmsAddress.
//                    getAttribute(JMSAddress.ATTR_PASSWORD));
//            connectionFactoryNameTextField.setText(jmsAddress.
//                    getAttribute(JMSAddress.ATTR_JNDI_CONNECTION_FACTORY_NAME));
//            initialContextFactoryTextField.setText(jmsAddress.
//                    getAttribute(JMSAddress.ATTR_JNDI_INITIAL_CONTEXT_FACTORY));
//            providerURLTextField.setText(jmsAddress.
//                    getAttribute(JMSAddress.ATTR_JNDI_PROVIDER_URL));
//            securityPrincipalTextField.setText(jmsAddress.
//                    getAttribute(JMSAddress.ATTR_JNDI_SECURITY_PRINCIPAL));
//            securityCrendentialsTextField.setText(jmsAddress.
//                    getAttribute(JMSAddress.ATTR_JNDI_SECURITY_CRDENTIALS));
//        }
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
                    if (input != null) {
                        NamedComponentReference<Message> messageIn = input.getMessage();
                        if ((messageIn != null) && (messageIn.get() != null)) {
                            Message msgIn = messageIn.get();
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
                    if (output != null) {
                        NamedComponentReference<Message> messageOut = output.getMessage();
                        if ((messageOut != null) && (messageOut.get() != null)) {
                            Message msgOut = messageOut.get();
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
                inputCorrelationPartComboBox.addItem(partName);
// TODO
//                inputDeliveryModePartComboBox.addItem(partName);
//                inputPriorityPartComboBox.addItem(partName);
                inputTypePartComboBox.addItem(partName);
                inputMsgIDPartComboBox.addItem(partName);
                inputRedeliveredPartComboBox.addItem(partName);
                inputTimestampPartComboBox.addItem(partName);
            }
        }
    }

    private void populateOutputPartComboBox(Collection textItems) {
        if ((textItems != null) && (textItems.size() > 0)) {
            Iterator iter = textItems.iterator();
            while(iter.hasNext()) {
                String partName = (String) iter.next();
//                outputTextComboBox.addItem(partName);
//                outputCorrelationPartComboBox.addItem(partName);
//                outputDeliveryModePartComboBox.addItem(partName);
//                outputPriorityPartComboBox.addItem(partName);
//                outputTypePartComboBox.addItem(partName);
//                outputMsgIDPartComboBox.addItem(partName);
//                outputRedeliveredPartComboBox.addItem(partName);
//                outputTimestampPartComboBox.addItem(partName);
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

   public FileError validateMe() {
        return validateMe(false);
    }
    
    public FileError validateMe(boolean fireEvent) {  
        // validate connection panel
        FileError fileError = consumerPanel.validateMe(fireEvent);
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

    private void updateJNDISection() {
        // enable JNDI Section only if ConnectionURL starts out with jndi
//        String val = getConnectionURL();
//        if ((val != null) && (val.toLowerCase().startsWith("jndi://"))) {
//            jndiBtn.setEnabled(true);
//        } else {
//            jndiBtn.setEnabled(false);
//        }
    }

    private void updateTopicOnlySection() {
//        subscriptionDurabilityLab.setEnabled(topicRBtn.isSelected());
//        subscriptionNameLab.setEnabled(topicRBtn.isSelected());
//        durabiltyRBtn.setEnabled(topicRBtn.isSelected());
//        nonDurabilityRBtn.setEnabled(topicRBtn.isSelected());
//        subscriptionNameLab.setEnabled(topicRBtn.isSelected());
//        subsNameTextField.setEnabled(topicRBtn.isSelected());
//        clientIDLab.setEnabled(topicRBtn.isSelected());
//        clientIDTextField.setEnabled(topicRBtn.isSelected());
    }

    private void updateEncodingSection() {
//        inputEncodingStyleLab.setEnabled(inputEncodingRBtn.isSelected());
//        inputEncodingStyleTextField.setEnabled(inputEncodingRBtn.isSelected());
//
//        outputEncodingStyleLab.setEnabled(outputEncodingRBtn.isSelected());
//        outputEncodingStyleTextField.setEnabled(outputEncodingRBtn.isSelected());
    }

    private void validateTextPart() {
        if (getOutputMessageText() == null) {
             firePropertyChange(
                    ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_ERROR_EVT, null,
                    NbBundle.getMessage(InboundOneWayConsumerPanel.class,
                    "JMSBindingConfiguratonnPanel.OUTPUT_TEXT_EMPTY"));
             return;
        }

        firePropertyChange(
                ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_CLEAR_MESSAGES_EVT, null, "");


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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup QTopicBGroup;
    private javax.swing.JComboBox bindingNameComboBox;
    private javax.swing.JLabel bindingNameLabel;
    private javax.swing.JComboBox concurModeComboBox;
    private javax.swing.JLabel connectionFactoryNameLab;
    private javax.swing.JTextField connectionFactoryNameTextField;
    private javax.swing.ButtonGroup deliveryModeBtnGrp;
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.ButtonGroup durabilityBGrp;
    private javax.swing.JLabel initialContextFactoryLab;
    private javax.swing.JTextField initialContextFactoryTextField;
    private javax.swing.JComboBox inputCorrelationPartComboBox;
    private javax.swing.ButtonGroup inputEncodingGroup;
    private javax.swing.JPanel inputMessagesPanelTab;
    private javax.swing.JComboBox inputMsgIDPartComboBox;
    private javax.swing.JComboBox inputRedeliveredPartComboBox;
    private javax.swing.JComboBox inputTimestampPartComboBox;
    private javax.swing.JComboBox inputTypePartComboBox;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel jndiLabel;
    private javax.swing.JPanel jndiSectionPanel;
    private javax.swing.JComboBox operationNameComboBox;
    private javax.swing.JLabel operationNameLabel;
    private javax.swing.ButtonGroup outputEncodingGroup;
    private javax.swing.JComboBox outputTextComboBox;
    private javax.swing.JComboBox outputTypeComboBox;
    private javax.swing.JPanel portBindingPanel;
    private javax.swing.JComboBox portTypeComboBox;
    private javax.swing.JLabel portTypeLabel;
    private javax.swing.JLabel providerURLLab;
    private javax.swing.JTextField providerURLTextField;
    private javax.swing.JLabel securityCredentialLab;
    private javax.swing.JTextField securityCrendentialsTextField;
    private javax.swing.JLabel securityPrincipalLab;
    private javax.swing.JTextField securityPrincipalTextField;
    private javax.swing.JComboBox servicePortComboBox;
    // End of variables declaration//GEN-END:variables

}
