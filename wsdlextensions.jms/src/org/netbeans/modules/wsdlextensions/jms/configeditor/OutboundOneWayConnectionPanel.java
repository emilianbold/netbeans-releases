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
import java.awt.GridBagConstraints;
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
import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.modules.wsdlextensions.jms.JMSAddress;
import org.netbeans.modules.wsdlextensions.jms.JMSBinding;
import org.netbeans.modules.wsdlextensions.jms.JMSConstants;
import org.netbeans.modules.wsdlextensions.jms.JMSMessage;
import org.netbeans.modules.wsdlextensions.jms.JMSOperation;
import org.netbeans.modules.wsdlextensions.jms.validator.JMSComponentValidator;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
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
public class OutboundOneWayConnectionPanel extends javax.swing.JPanel {

    private WSDLComponent mWsdlComponent;

    /** QName **/
    private QName mQName;

    /** resource bundle for file bc **/
    private ResourceBundle mBundle = ResourceBundle.getBundle(
            "org.netbeans.modules.wsdlextensions.jms.resources.Bundle");

    private static final Logger mLogger = Logger.
            getLogger(OutboundOneWayConnectionPanel.class.getName());

    private MyItemListener mItemListener = null;

    private MyActionListener mActionListener = null;

    private DescriptionPanel descPanel = null;
    private DescriptionPanel descPanelArchivePanel = null;
    private DescriptionPanel descPanelTextPanel = null;

    private JMSConnectionPanel connectionPanel = null;

    private Dialog mDetailsDlg = null;
    private DialogDescriptor mDetailsDlgDesc = null;

    private Dialog mDetailsJNDIDlg = null;
    private DialogDescriptor mDetailsJNDIDlgDesc = null;    

    /**
     * Project associated with this wsdl
     */
    private Project mProject = null;
    private Part mPart = null;    
    private MessageTypePanel mMessageTypePanel = null;
    
    /** Creates new form JMSBindingConfigurationPanel */
    public OutboundOneWayConnectionPanel(QName qName, WSDLComponent component) {
        mWsdlComponent = component;
        mQName = qName;
        initComponents();
        initCustomComponents();
        resetView();
        populateView(qName, mWsdlComponent);
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
     * Return the connection URL
     * @return String connection url
     */
    String getConnectionURL() {
        return connectionPanel.getConnectionURL();
    }

    /**
     * Return the message type for input message
     * @return String message type
     */
    String getInputMessageType() {
//        return (String) inputTypeComboBox2.getSelectedItem();
        // now that we support both binary and text, we return the right 
        // message type based on the payload processing
        if (mMessageTypePanel.getMessageType() == JMSConstants.BINARY_MESSAGE_TYPE) {
            return JMSConstants.BYTES_MESSAGE;
        } else {
            return JMSConstants.TEXT_MESSAGE;
        }          

    }

    /**
     * Return the message text for input message
     * @return String message text
     */
    String getInputMessageText() {
        if ((inputTextComboBox.getSelectedItem() != null) &&
                (!inputTextComboBox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
            return inputTextComboBox.getSelectedItem().toString();
        } else {
            return null;
        }

    }

    String getInputUse() {
        return mMessageTypePanel.getInputUse();               
    }

    String getInputEncodingStyle() {
        return mMessageTypePanel.getInputEncodingStyle();
    }

    String getUserName() {
        return connectionPanel.getUserName();
    }

    String getPassword() {
        return connectionPanel.getPassword();
    }

    String getConnectionFactoryName() {
        return connectionPanel.getConnectionFactoryName();
    }

    String getInitialContextFactory() {
        return connectionPanel.getInitialContextFactory();
    }

    String getProviderURL() {
        return connectionPanel.getProviderURL();
    }

    String getSecurityPrincipal() {
        return connectionPanel.getSecurityPrincipal();
    }

    String getSecurityCredentials() {
        return connectionPanel.getSecurityCredentials();
    }

    /**
     * Return message type.  Options are FileConstants.XML_MESSAGE_TYPE,
     * FileConstants.TEXT_MESSAGE_TYPE, FileConstants.ENCODED_MESSAGE_TYPE
     * 
     * @return
     */
    int getMessageType() {
        return mMessageTypePanel.getMessageType();
    }   
        
   void setProject(Project project) {
        mProject = project;
        mMessageTypePanel.setProject(project);
    }    
    
    GlobalType getSelectedPartType() {
        return mMessageTypePanel.getSelectedPartType();
    }
    
    GlobalElement getSelectedElementType() {
        return mMessageTypePanel.getSelectedElementType();
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
    
    /**
     * Enable the Processing Payload section accordingly
     * @param enable
     */
    public void enablePayloadProcessing(boolean enable) {
        if (mMessageTypePanel != null) {
            mMessageTypePanel.enablePayloadProcessing(enable);
        }
    }
    
    private void initCustomComponents() {
        connectionPanel = new JMSConnectionPanel(null, null);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
        generalPanel.add(connectionPanel, gridBagConstraints);      
        
        mMessageTypePanel = new MessageTypePanel(mWsdlComponent, null, null, null, false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        generalPanel.add(mMessageTypePanel, gridBagConstraints);   
        
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
        messageTypeBtnGrp = new javax.swing.ButtonGroup();
        inputTypeComboBox2 = new javax.swing.JComboBox();
        inputTextComboBox = new javax.swing.JComboBox();
        jSplitPane1 = new javax.swing.JSplitPane();
        generalPanel = new javax.swing.JPanel();
        descriptionPanel = new javax.swing.JPanel();

        portBindingPanel.setName("portBindingPanel"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel26, org.openide.util.NbBundle.getMessage(OutboundOneWayConnectionPanel.class, "OutboundOneWayConnectionPanel.jLabel26.text")); // NOI18N
        jLabel26.setName("jLabel26"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(portTypeLabel, org.openide.util.NbBundle.getMessage(OutboundOneWayConnectionPanel.class, "OutboundOneWayConnectionPanel.portTypeLabel.text")); // NOI18N
        portTypeLabel.setName("portTypeLabel"); // NOI18N

        operationNameComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        operationNameComboBox.setName("operationNameComboBox"); // NOI18N
        operationNameComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                operationNameComboBoxItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel42, org.openide.util.NbBundle.getMessage(OutboundOneWayConnectionPanel.class, "OutboundOneWayConnectionPanel.jLabel42.text")); // NOI18N
        jLabel42.setName("jLabel42"); // NOI18N

        bindingNameComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        bindingNameComboBox.setName("bindingNameComboBox"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(operationNameLabel, org.openide.util.NbBundle.getMessage(OutboundOneWayConnectionPanel.class, "OutboundOneWayConnectionPanel.operationNameLabel.text_1")); // NOI18N
        operationNameLabel.setName("operationNameLabel"); // NOI18N

        servicePortComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        servicePortComboBox.setName("servicePortComboBox"); // NOI18N
        servicePortComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                servicePortComboBoxItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(bindingNameLabel, org.openide.util.NbBundle.getMessage(OutboundOneWayConnectionPanel.class, "OutboundOneWayConnectionPanel.bindingNameLabel.text")); // NOI18N
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

        inputTypeComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        inputTypeComboBox2.setName("inputTypeComboBox2"); // NOI18N

        inputTextComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        inputTextComboBox.setName("inputTextComboBox"); // NOI18N
        inputTextComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                inputTextComboBoxItemStateChanged(evt);
            }
        });

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(1.0);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        generalPanel.setName("generalPanel"); // NOI18N
        generalPanel.setLayout(new java.awt.GridBagLayout());
        jSplitPane1.setLeftComponent(generalPanel);

        descriptionPanel.setMinimumSize(new java.awt.Dimension(400, 50));
        descriptionPanel.setName("descriptionPanel"); // NOI18N
        descriptionPanel.setPreferredSize(new java.awt.Dimension(400, 75));
        descriptionPanel.setLayout(new java.awt.BorderLayout());
        descPanel = new DescriptionPanel();
        descriptionPanel.add(descPanel, java.awt.BorderLayout.CENTER);
        jSplitPane1.setBottomComponent(descriptionPanel);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

private void servicePortComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_servicePortComboBoxItemStateChanged
// TODO add your handling code here:
    if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
        Object selObj = servicePortComboBox.getSelectedItem();
        String selBindingName = "";
        if (bindingNameComboBox.getSelectedItem() != null) {
            selBindingName = bindingNameComboBox.getSelectedItem().toString();
        }
        if ((selObj != null) && (mWsdlComponent != null)) {
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
        if (mWsdlComponent != null)  {
            Binding binding = null;
            if (mWsdlComponent instanceof JMSAddress) {
                Port port = (Port) ((JMSAddress) mWsdlComponent).getParent();
                binding = port.getBinding().get();

            } else if (mWsdlComponent instanceof JMSBinding) {
                binding = (Binding) ((JMSBinding) mWsdlComponent).getParent();
            }
            if (binding != null) {
//                JMSMessage outputMessage = getOutputJMSMessage(binding,
//                        selectedOperation);
//                updateOutputMessageView(outputMessage);

            }
        }
    }
}//GEN-LAST:event_operationNameComboBoxItemStateChanged

private void inputTextComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_inputTextComboBoxItemStateChanged
// TODO add your handling code here:
    validateTextPart();
}//GEN-LAST:event_inputTextComboBoxItemStateChanged


    private void initListeners() {
        if (mItemListener == null)  {
            mItemListener = new MyItemListener();
        }

        if (mActionListener == null) {
            mActionListener = new MyActionListener();
        }
    }

    private void resetView() {
        servicePortComboBox.setEnabled(false);
        servicePortComboBox.removeAllItems();
        bindingNameComboBox.removeAllItems();
//        transactionComboBox.removeAllItems();
        inputTypeComboBox2.removeAllItems();
//        outputTypeComboBox.removeAllItems();
        inputTextComboBox.removeAllItems();
//        outputTextComboBox.removeAllItems();
        portTypeComboBox.removeAllItems();
        operationNameComboBox.removeAllItems();
        
        inputTypeComboBox2.addItem(JMSConstants.MAP_MESSAGE);
        inputTypeComboBox2.addItem(JMSConstants.TEXT_MESSAGE);
        inputTypeComboBox2.addItem(JMSConstants.BYTES_MESSAGE);      
    }

    /**
     * Populate the view with the given the model component
     * @param qName
     * @param component
     */
    public void populateView(QName qName, WSDLComponent component) {
        cleanUp();
        mQName = qName;
        mWsdlComponent = component;
        resetView();
        populateView(mWsdlComponent);
        connectionPanel.populateView(qName, component);
        connectionPanel.setDescriptionPanel(descPanel);
        mMessageTypePanel.setDescriptionPanel(descPanel);        
        initListeners();
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
            // select the 1st item since this is not a configurable param
            operationNameComboBox.setSelectedIndex(0);
            if (operationNameComboBox.getItemCount() > 0) {
                // need to implicitly call update on messages because above
                // listener will not change selection if only 1 item
                if (binding != null) {
                    JMSMessage inputMessage = getInputJMSMessage(binding,
                            operationNameComboBox.getSelectedItem().toString());
                    updateInputMessageView(binding, inputMessage);

                }
            }
            updateGeneralView(jmsAddress, bindingOperations);
        }
    }

    private void updateInputMessageView(Binding binding, JMSMessage inputJMSMessage) {
        if (inputJMSMessage != null) {
//TODO
            inputTypeComboBox2.setSelectedItem(inputJMSMessage.getMessageType());
            inputTextComboBox.setSelectedItem(inputJMSMessage.getTextPart());

            // TODO
//            if (inputJMSMessage.getUse() != null) {
//                if (inputJMSMessage.getUse().equals(JMSConstants.ENCODED)) {
//                    inputEncodedRBtn.setSelected(true);
//                } else {
//                    inputEncodedRBtn.setSelected(false);
//                }
//            } else {
//                inputEncodedRBtn.setSelected(false);
//            }
//            inputEncodedTypeTfld.setText(
//                    inputJMSMessage.getJMSEncodingStyle());
            
            String part = null;
            if (JMSConstants.BYTES_MESSAGE.equals(inputJMSMessage.getMessageType())) {
                part = inputJMSMessage.getAttribute(JMSMessage.ATTR_BYTES_PART);
            } else {
                part = inputJMSMessage.getTextPart();
            }              
            if ((part == null) || (JMSConstants.CHANGE_ME.equals(part))) {
                // per BC developer, will preselect 1st item
                if (inputTextComboBox.getItemCount() > 0) {
                    String partToSetWith = JMSUtilities.
                            getInputPartToSetWith(inputTextComboBox);
                    if (partToSetWith != null) {
                        inputTextComboBox.setSelectedItem(partToSetWith);
                        part = partToSetWith;
                    }                    
                }
            } else {
                inputTextComboBox.setSelectedItem(part);
            }
            // check if Part selected has a type and set correct msg type toggle
            // get the Message
            Operation op = JMSUtilities.getOperation(binding,
                    operationNameComboBox.getSelectedItem().toString());
            if (op != null) {
                Input inputOp = op.getInput();
                NamedComponentReference<Message> messageIn = inputOp.getMessage();
                if (inputTextComboBox.getSelectedItem() != null) {
                    mPart = JMSUtilities.getMessagePart(part, messageIn.get());
                }                                
            }  
            
            mMessageTypePanel.populateView(mWsdlComponent, mPart, 
                    inputJMSMessage, mProject, 
                    operationNameComboBox.getSelectedItem().toString());            
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


                        //batchSizeTextField.setText(jmsOp.
                        //        getAttribute(JMSOperation.ATTR_BATCH_SZIE));
                        //valMsgSelComboBox.setSelectedItem(jmsOp.
                        //        getAttribute(JMSOperation.ATTR_));

//                        batchSizeSpinner.setValue(jmsOp.getBatchSize());

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
                if (bi != null) {
                    List<JMSMessage> inputJMSMessages =
                            bi.getExtensibilityElements(JMSMessage.class);
                    if ((inputJMSMessages != null) &&
                            (inputJMSMessages.size() > 0)) {
                        JMSMessage inputJMSMessage = inputJMSMessages.get(0);
    // TODO
                        // get input message type
                        inputTypeComboBox2.setSelectedItem(inputJMSMessage.
                                getMessageType());
                        // get input text
                        inputTextComboBox.setSelectedItem(inputJMSMessage.
                                getTextPart());
                    }
                }
//                BindingOutput bo = bop.getBindingOutput();
//                if (bo != null) {
//                    List<JMSMessage> outputJMSMessages =
//                            bo.getExtensibilityElements(JMSMessage.class);
//                    if ((outputJMSMessages != null) &&
//                            (outputJMSMessages.size() > 0)) {
//                        JMSMessage outputJMSMessage = outputJMSMessages.get(0);
//    // TODO
//    //                    // get output message type
//    //                    outputTypeComboBox.setSelectedItem(outputJMSMessage.
//    //                            getMessageType());
//    //
//    //                    // get output text
//    //                    outputTextComboBox.setSelectedItem(outputJMSMessage.
//    //                            getTextPart());
//                    }
//                }
            }
        }
    }

    private void updateServiceView(JMSAddress jmsAddress) {
        if (jmsAddress != null) {
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
                    if ((input != null) && (input.getMessage() != null) &&
                            (input.getMessage().get() != null)) {
                        NamedComponentReference<Message> messageIn = input.getMessage();
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
                    if ((output != null) && (output.getMessage() != null) &&
                            output.getMessage().get() != null) {
                        NamedComponentReference<Message> messageOut = output.getMessage();
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
        return outputParts;
    }

    private void populateInputPartComboBox(Collection textItems) {
        if ((textItems != null) && (textItems.size() > 0)) {
            Iterator iter = textItems.iterator();
            while(iter.hasNext()) {
                String partName = (String) iter.next();
                inputTextComboBox.addItem(partName);
 
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
    
   public FileError validateMe() {
        return validateMe(false);
    }
    
    public FileError validateMe(boolean fireEvent) {  
        // validate connection panel
        FileError fileError = connectionPanel.validateMe(fireEvent);
        if (ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_CLEAR_MESSAGES_EVT.equals(fileError.getErrorMode())) {                               
            // validate message section
            fileError = mMessageTypePanel.validateMe(fireEvent);                                  
        }

        if (fireEvent) {
            ErrorPropagator.doFirePropertyChange(fileError.getErrorMode(), null,
                    fileError.getErrorMessage(), this);
        }      
        return fileError;
    }    
    
    /**
     * Route the property change event to this panel
     */
    public void doFirePropertyChange(String name, Object oldValue, Object newValue) {  
         super.firePropertyChange(name, oldValue, newValue);
    }    

    protected boolean validateContent() {
        ValidationResult results = new JMSComponentValidator().
                validate(mWsdlComponent.getModel(), null, ValidationType.COMPLETE);
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

//    private void updateEncodingSection() {
//        inputEncodingStyleLab.setEnabled(inputEncodingRBtn.isSelected());
//        inputEncodingStyleTextField.setEnabled(inputEncodingRBtn.isSelected());
//
//        outputEncodingStyleLab.setEnabled(outputEncodingRBtn.isSelected());
//        outputEncodingStyleTextField.setEnabled(outputEncodingRBtn.isSelected());
//    }

    private void validateTextPart() {
        if (getInputMessageText() == null) {
             firePropertyChange(
                    ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_ERROR_EVT, null,
                    NbBundle.getMessage(OutboundOneWayConnectionPanel.class,
                    "JMSBindingConfiguratonnPanel.INPUT_TEXT_EMPTY"));
             return;
        }

        firePropertyChange(
                ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_CLEAR_MESSAGES_EVT, null, "");


    }

    private void updateDescriptionArea(FocusEvent evt) {
        if (descPanel != null) {
            descPanel.setText("");
        }

        String[] desc = null;
        boolean casaEdited = false;

        if (desc != null) {
            if (descPanel != null) {
                descPanel.setText(desc[0], desc[1]);
            }
            return;
        }
    }  
    
    private void showXSDElementTypeDialog() {

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

    private void handleItemStateChanged(ItemEvent evt) {

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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox bindingNameComboBox;
    private javax.swing.JLabel bindingNameLabel;
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JPanel generalPanel;
    private javax.swing.ButtonGroup inputEncodingGroup;
    private javax.swing.JComboBox inputTextComboBox;
    private javax.swing.JComboBox inputTypeComboBox2;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.ButtonGroup messageTypeBtnGrp;
    private javax.swing.JComboBox operationNameComboBox;
    private javax.swing.JLabel operationNameLabel;
    private javax.swing.ButtonGroup outputEncodingGroup;
    private javax.swing.JPanel portBindingPanel;
    private javax.swing.JComboBox portTypeComboBox;
    private javax.swing.JLabel portTypeLabel;
    private javax.swing.JComboBox servicePortComboBox;
    // End of variables declaration//GEN-END:variables

}
