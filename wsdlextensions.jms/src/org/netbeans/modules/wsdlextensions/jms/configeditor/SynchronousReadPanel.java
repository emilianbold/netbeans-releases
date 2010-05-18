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
import javax.swing.Action;
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
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * JMSBindingConfigurationPanel - Panel that allows configuration of
 * properties specifically for JMS Binding component
 *
 * @author  jalmero
 */
public class SynchronousReadPanel extends javax.swing.JPanel {

    private WSDLComponent mComponent;

    /** QName **/
    private QName mQName;

    /** resource bundle for file bc **/
    private ResourceBundle mBundle = ResourceBundle.getBundle(
            "org.netbeans.modules.wsdlextensions.jms.resources.Bundle");

    private static final Logger mLogger = Logger.
            getLogger(SynchronousReadPanel.class.getName());

    private DescriptionPanel descPanel = null;

    private MyItemListener mItemListener = null;

    private MyActionListener mActionListener = null;
    
    private JMSDestinationPanel mDestinationPanel = null;

    /** Creates new form JMSBindingConfigurationPanel */
    public SynchronousReadPanel(QName qName, WSDLComponent component) {
        initComponents();
        initCustomComponents();
        populateView(qName, component);
    }

    @Override
    public String getName() {
        return "Request Configuration";
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
        mDestinationPanel.populateView(qName, component);
        mDestinationPanel.setDescriptionPanel(descPanel);
        initListeners();
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
    String getInputMessageType() {
//        return (String) inputTypeComboBox.getSelectedItem();
        // TODO
        return "";
    }

    /**
     * Return the message text for input message
     * @return String message text
     */
    String getInputMessageText() {
//        if ((inputTextComboBox.getSelectedItem() != null) &&
//                (!inputTextComboBox.getSelectedItem().toString().
//                equals(JMSConstants.NOT_SET))) {
//            return inputTextComboBox.getSelectedItem().toString();
//        } else {
//            return null;
//
//        }
        // TODO
        return "";
    }

    /**
     * Return the message selector value
     * @return String message selector
     */
    String getMessageSelector() {
        return msgSelectorTextField.getText();
    }

    /**
     * Return timeout value
     * @return
     */
    long getTimeout() {
        if (timeoutTextField.getText().length() == 0) {
            return 0;
        } else {
            return new Long(timeoutTextField.getText()).longValue();
        }
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
     * Return the validate message selector
     * @return String validate message selector
     */
    boolean getValidateMessageSelector() {
        return validateMsgSelectorBox.isSelected();
    }

    String getInputCorrelationPart() {
        if ((inputCorrelationPartComboBox.getSelectedItem() != null) &&
                (!inputCorrelationPartComboBox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
            return inputCorrelationPartComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
// TODO
//        if (requestCorrelationBox.isSelected()) {
//            // need to generate the actual Part model (see Sherry) and then
              // populate the model as well
//        }
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

    String getInputTypePart() {
        if ((inputTypePartComboBox.getSelectedItem() != null) &&
                (!inputTypePartComboBox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
            return inputTypePartComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    String getInputMessageIDPart() {
        if ((inputMsgIDPartComboBox.getSelectedItem() != null) &&
                (!inputMsgIDPartComboBox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
            return inputMsgIDPartComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    String getInputRedeliveredPart() {
        if ((inputRedeliveredPartComboBox.getSelectedItem() != null) &&
                (!inputRedeliveredPartComboBox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
            return inputRedeliveredPartComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    String getInputTimestamp() {
        if ((inputTimestampPartComboBox.getSelectedItem() != null) &&
                (!inputTimestampPartComboBox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
            return inputTimestampPartComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    String getOutputCorrelationPart() {
//        if ((outputCorrelationPartComboBox.getSelectedItem() != null) &&
//                (!outputCorrelationPartComboBox.getSelectedItem().toString().
//                equals(JMSConstants.NOT_SET))) {
//            return outputCorrelationPartComboBox.getSelectedItem().toString();
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

    String getOutputTypePart() {
//        if ((outputTypePartComboBox.getSelectedItem() != null) &&
//                (!outputTypePartComboBox.getSelectedItem().toString().
//                equals(JMSConstants.NOT_SET))) {
//            return outputTypePartComboBox.getSelectedItem().toString();
//        } else {
//            return null;
//        }
        return "";
    }

    String getOutputMessageIDPart() {
//        if ((outputMsgIDPartComboBox.getSelectedItem() != null) &&
//                (!outputMsgIDPartComboBox.getSelectedItem().toString().
//                equals(JMSConstants.NOT_SET))) {
//            return outputMsgIDPartComboBox.getSelectedItem().toString();
//        } else {
//            return null;
//        }
        return "";
    }

    String getOutputRedeliveredPart() {
//        if ((outputRedeliveredPartComboBox.getSelectedItem() != null) &&
//                (!outputRedeliveredPartComboBox.getSelectedItem().toString().
//                equals(JMSConstants.NOT_SET))) {
//            return outputRedeliveredPartComboBox.getSelectedItem().toString();
//        } else {
//            return null;
//        }
        return "";
    }

    String getOutputTimestamp() {
//        if ((outputTimestampPartComboBox.getSelectedItem() != null) &&
//                (!outputTimestampPartComboBox.getSelectedItem().toString().
//                equals(JMSConstants.NOT_SET))) {
//            return outputTimestampPartComboBox.getSelectedItem().toString();
//        } else {
//            return null;
//        }
        return "";
    }

    /**
     * Return the destination
     * @return String destination
     */
    String getDestination() {
        return mDestinationPanel.getDestination();
    }

    /**
     * Return the destination type value
     * @return String destination type
     */
    String getDestinationType() {
        return mDestinationPanel.getDestinationType();
    }

    /**
     * Return transaction value
     * @return String transaction
     */
    String getTransaction() {
        return mDestinationPanel.getTransaction();
    }

    /**
     * Return the client ID value
     * @return String client ID
     */
    String getClientID() {
        return mDestinationPanel.getClientID();
    }   

    /**
     * Return the subscription name value
     * @return String subscription name
     */
    String getSubscriptionName() {
        return mDestinationPanel.getSubscriptionName();
    }

    String getSubscriptionDurability() {
        return mDestinationPanel.getSubscriptionDurability();
    }        
    
    /**
     * Set the operation name to be configured
     * @param opName
     */
    void setOperationName(String opName) {
        if (opName != null) {
            operationNameComboBox.setSelectedItem(opName);
            
            if (mDestinationPanel != null) {
                mDestinationPanel.setOperationName(opName);
            }
        }
    }    
    
    private void initCustomComponents() {
        mDestinationPanel = new JMSDestinationPanel(null, null);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
        inputMessagesPanelTab.add(mDestinationPanel, gridBagConstraints);
        
        this.getAccessibleContext().setAccessibleName(getName());
        this.getAccessibleContext().setAccessibleDescription(getName());
        
        setAccessibility();
    }
    
    private void setAccessibility() {     
        msgSelectorTextField.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_messageSelector")); // NOI18N
        msgSelectorTextField.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_messageSelector")); // NOI18N
        timeoutTextField.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_timeout")); // NOI18N
        timeoutTextField.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_timeout")); // NOI18N
        
    }
    
    private void updateDescriptionArea(FocusEvent evt) {
        if (descPanel != null) {
            descPanel.setText("");
        }

        String[] desc = null;
        boolean casaEdited = false;

        if (evt.getSource() == msgSelectorTextField) {
            desc = new String[]{"Message Selector \n\n",
                   msgSelectorTextField.getToolTipText()}; 
        } else if (evt.getSource() == timeoutTextField) {
            desc = new String[]{"TimeOut \n\n",
                   timeoutTextField.getToolTipText()}; 
        }
        if (desc != null) {
            if (descPanel != null) {
                descPanel.setText(desc[0], desc[1]);
            }
            return;
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
        java.awt.GridBagConstraints gridBagConstraints;

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
        validateMsgSelectorBox = new javax.swing.JCheckBox();
        inputTypeComboBox = new javax.swing.JComboBox();
        inputTextComboBox = new javax.swing.JComboBox();
        jSplitPane1 = new javax.swing.JSplitPane();
        inputMessagesPanelTab = new javax.swing.JPanel();
        consumerPanel = new javax.swing.JPanel();
        consumerLab = new javax.swing.JLabel();
        jSeparator7 = new javax.swing.JSeparator();
        msgSelectorTextField = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        timeoutLab = new javax.swing.JLabel();
        timeoutTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        descriptionPanel1 = new javax.swing.JPanel();

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

        org.openide.awt.Mnemonics.setLocalizedText(jLabel26, org.openide.util.NbBundle.getMessage(SynchronousReadPanel.class, "SynchronousReadPanel.jLabel26.text")); // NOI18N
        jLabel26.setName("jLabel26"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(portTypeLabel, org.openide.util.NbBundle.getMessage(SynchronousReadPanel.class, "SynchronousReadPanel.portTypeLabel.text")); // NOI18N
        portTypeLabel.setName("portTypeLabel"); // NOI18N

        operationNameComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        operationNameComboBox.setName("operationNameComboBox"); // NOI18N
        operationNameComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                operationNameComboBoxItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel42, org.openide.util.NbBundle.getMessage(SynchronousReadPanel.class, "SynchronousReadPanel.jLabel42.text")); // NOI18N
        jLabel42.setName("jLabel42"); // NOI18N

        bindingNameComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        bindingNameComboBox.setName("bindingNameComboBox"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(operationNameLabel, org.openide.util.NbBundle.getMessage(SynchronousReadPanel.class, "SynchronousReadPanel.operationNameLabel.text_1")); // NOI18N
        operationNameLabel.setName("operationNameLabel"); // NOI18N

        servicePortComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        servicePortComboBox.setName("servicePortComboBox"); // NOI18N
        servicePortComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                servicePortComboBoxItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(bindingNameLabel, org.openide.util.NbBundle.getMessage(SynchronousReadPanel.class, "SynchronousReadPanel.bindingNameLabel.text")); // NOI18N
        bindingNameLabel.setName("bindingNameLabel"); // NOI18N

        portTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        portTypeComboBox.setName("portTypeComboBox"); // NOI18N

        jSeparator2.setName("jSeparator2"); // NOI18N

        org.jdesktop.layout.GroupLayout portBindingPanelLayout = new org.jdesktop.layout.GroupLayout(portBindingPanel);
        portBindingPanel.setLayout(portBindingPanelLayout);
        portBindingPanelLayout.setHorizontalGroup(
            portBindingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 556, Short.MAX_VALUE)
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
                            .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 523, Short.MAX_VALUE)))
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

        org.openide.awt.Mnemonics.setLocalizedText(validateMsgSelectorBox, org.openide.util.NbBundle.getMessage(SynchronousReadPanel.class, "SynchronousReadPanel.validateMsgSelectorBox.text")); // NOI18N
        validateMsgSelectorBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        validateMsgSelectorBox.setName("validateMsgSelectorBox"); // NOI18N

        inputTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        inputTypeComboBox.setName("inputTypeComboBox"); // NOI18N

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

        inputMessagesPanelTab.setName("inputMessagesPanelTab"); // NOI18N
        inputMessagesPanelTab.setLayout(new java.awt.GridBagLayout());

        consumerPanel.setName("consumerPanel"); // NOI18N
        consumerPanel.setLayout(new java.awt.GridBagLayout());

        consumerLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(consumerLab, org.openide.util.NbBundle.getMessage(SynchronousReadPanel.class, "SynchronousReadPanel.consumerLab.text")); // NOI18N
        consumerLab.setName("consumerLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        consumerPanel.add(consumerLab, gridBagConstraints);

        jSeparator7.setName("jSeparator7"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 170, 0, 0);
        consumerPanel.add(jSeparator7, gridBagConstraints);

        msgSelectorTextField.setText(org.openide.util.NbBundle.getMessage(SynchronousReadPanel.class, "SynchronousReadPanel.msgSelectorTextField.text")); // NOI18N
        msgSelectorTextField.setToolTipText(mBundle.getString("DESC_Attribute_messageSelector"));
        msgSelectorTextField.setName("msgSelectorTextField"); // NOI18N
        msgSelectorTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                msgSelectorTextFieldActionPerformed(evt);
            }
        });
        msgSelectorTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                msgSelectorTextFieldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        consumerPanel.add(msgSelectorTextField, gridBagConstraints);

        jLabel28.setLabelFor(msgSelectorTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel28, org.openide.util.NbBundle.getMessage(SynchronousReadPanel.class, "SynchronousReadPanel.jLabel28.text")); // NOI18N
        jLabel28.setName("jLabel28"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        consumerPanel.add(jLabel28, gridBagConstraints);
        jLabel28.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SynchronousReadPanel.class, "InboundMessageConsumerPanel.jLabel28.AccessibleContext.accessibleName")); // NOI18N

        timeoutLab.setLabelFor(timeoutTextField);
        org.openide.awt.Mnemonics.setLocalizedText(timeoutLab, org.openide.util.NbBundle.getMessage(SynchronousReadPanel.class, "SynchronousReadPanel.timeoutLab.text")); // NOI18N
        timeoutLab.setName("timeoutLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        consumerPanel.add(timeoutLab, gridBagConstraints);

        timeoutTextField.setText(org.openide.util.NbBundle.getMessage(SynchronousReadPanel.class, "SynchronousReadPanel.timeoutTextField.text")); // NOI18N
        timeoutTextField.setToolTipText(mBundle.getString("DESC_Attribute_timeout"));
        timeoutTextField.setName("timeoutTextField"); // NOI18N
        timeoutTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                timeoutTextFieldFocusGained(evt);
            }
        });
        timeoutTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                timeoutTextFieldKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 85;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        consumerPanel.add(timeoutTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SynchronousReadPanel.class, "SynchronousReadPanel.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        consumerPanel.add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        inputMessagesPanelTab.add(consumerPanel, gridBagConstraints);

        jSplitPane1.setTopComponent(inputMessagesPanelTab);

        descriptionPanel1.setMinimumSize(new java.awt.Dimension(400, 50));
        descriptionPanel1.setName("descriptionPanel1"); // NOI18N
        descriptionPanel1.setPreferredSize(new java.awt.Dimension(400, 75));
        descriptionPanel1.setLayout(new java.awt.BorderLayout());

        descPanel = new DescriptionPanel();
        descriptionPanel1.add(descPanel, java.awt.BorderLayout.CENTER);

        jSplitPane1.setBottomComponent(descriptionPanel1);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

private void inputMsgIDPartComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputMsgIDPartComboBoxActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_inputMsgIDPartComboBoxActionPerformed

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

private void msgSelectorTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_msgSelectorTextFieldActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_msgSelectorTextFieldActionPerformed

private void timeoutTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_timeoutTextFieldKeyTyped
    // TODO add your handling code here:
    if (!Character.isDigit(evt.getKeyChar())) {
        evt.consume();
    }
}//GEN-LAST:event_timeoutTextFieldKeyTyped

private void timeoutTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_timeoutTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_timeoutTextFieldFocusGained

private void msgSelectorTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_msgSelectorTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_msgSelectorTextFieldFocusGained

private void inputTextComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_inputTextComboBoxItemStateChanged
// TODO add your handling code here:
    validateTextPart();
}//GEN-LAST:event_inputTextComboBoxItemStateChanged



    private void resetView() {
//        enableBatchBox.removeItemListener(mItemListener);
        servicePortComboBox.setEnabled(false);
        servicePortComboBox.removeAllItems();
        bindingNameComboBox.removeAllItems();
//        transactionComboBox.removeAllItems();
        inputTypeComboBox.removeAllItems();
//        outputTypeComboBox.removeAllItems();
        inputTextComboBox.removeAllItems();
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
        inputTypeComboBox.addItem(JMSConstants.MAP_MESSAGE);
        inputTypeComboBox.addItem(JMSConstants.TEXT_MESSAGE);
        inputTypeComboBox.addItem(JMSConstants.BYTES_MESSAGE);           
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

    private void initListeners() {
        if (mItemListener == null)  {
            mItemListener = new MyItemListener();
        }

        if (mActionListener == null) {
            mActionListener = new MyActionListener();
        }
//        enableBatchBox.addItemListener(mItemListener);
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
            inputTypeComboBox.setSelectedItem(inputJMSMessage.getMessageType());
            inputTextComboBox.setSelectedItem(inputJMSMessage.getTextPart());
            inputCorrelationPartComboBox.setSelectedItem(
                    inputJMSMessage.getCorrelationIdPart());
            // TODO
//            inputDeliveryModePartComboBox.setSelectedItem(
//                    inputJMSMessage.getDeliveryModePart());
//            inputPriorityPartComboBox.setSelectedItem(
//                    inputJMSMessage.getPriorityPart());
            inputTypePartComboBox.setSelectedItem(
                    inputJMSMessage.getTypePart());
            inputMsgIDPartComboBox.setSelectedItem(
                    inputJMSMessage.getMessageIDPart());
            inputRedeliveredPartComboBox.setSelectedItem(
                    inputJMSMessage.getRedeliveredPart());
            inputTimestampPartComboBox.setSelectedItem(
                    inputJMSMessage.getTimestampPart());
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

                        msgSelectorTextField.setText(jmsOp.getMessageSelector());
                        timeoutTextField.setText(String.valueOf(jmsOp.getTimeout()));
                        concurModeComboBox.setSelectedItem(jmsOp.getAttribute(
                                JMSOperation.ATTR_CONCURRENCY_MODE));
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
 
                    // get input message type
                    inputTypeComboBox.setSelectedItem(inputJMSMessage.
                            getMessageType());
                    // get input text
                    inputTextComboBox.setSelectedItem(inputJMSMessage.
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
                            (output.getMessage().get() != null)) {                    
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
        FileError fileError = mDestinationPanel.validateMe(fireEvent);

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

    private void validateTextPart() {
        if (getInputMessageText() == null) {
             firePropertyChange(
                    ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_ERROR_EVT, null,
                    NbBundle.getMessage(SynchronousReadPanel.class,
                    "JMSBindingConfiguratonnPanel.INPUT_TEXT_EMPTY"));
             return;
        }

        firePropertyChange(
                ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_CLEAR_MESSAGES_EVT, null, "");


    }

    private void handleItemStateChanged(ItemEvent evt) {
//        if (evt.getSource() == enableBatchBox) {
//            if (enableBatchBox.isSelected()) {
//                batchSizeSpinner.setEnabled(true);
//            } else {
//                batchSizeSpinner.setEnabled(false);
//            }
//        }

    }

    private void handleActionPerformed(ActionEvent evt) {

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
    private javax.swing.ButtonGroup QTopicBGroup;
    private javax.swing.JComboBox bindingNameComboBox;
    private javax.swing.JLabel bindingNameLabel;
    private javax.swing.JComboBox concurModeComboBox;
    private javax.swing.JLabel consumerLab;
    private javax.swing.JPanel consumerPanel;
    private javax.swing.ButtonGroup deliveryModeBtnGrp;
    private javax.swing.JPanel descriptionPanel1;
    private javax.swing.ButtonGroup durabilityBGrp;
    private javax.swing.JComboBox inputCorrelationPartComboBox;
    private javax.swing.ButtonGroup inputEncodingGroup;
    private javax.swing.JPanel inputMessagesPanelTab;
    private javax.swing.JComboBox inputMsgIDPartComboBox;
    private javax.swing.JComboBox inputRedeliveredPartComboBox;
    private javax.swing.JComboBox inputTextComboBox;
    private javax.swing.JComboBox inputTimestampPartComboBox;
    private javax.swing.JComboBox inputTypeComboBox;
    private javax.swing.JComboBox inputTypePartComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextField msgSelectorTextField;
    private javax.swing.JComboBox operationNameComboBox;
    private javax.swing.JLabel operationNameLabel;
    private javax.swing.ButtonGroup outputEncodingGroup;
    private javax.swing.JPanel portBindingPanel;
    private javax.swing.JComboBox portTypeComboBox;
    private javax.swing.JLabel portTypeLabel;
    private javax.swing.JComboBox servicePortComboBox;
    private javax.swing.JLabel timeoutLab;
    private javax.swing.JTextField timeoutTextField;
    private javax.swing.JCheckBox validateMsgSelectorBox;
    // End of variables declaration//GEN-END:variables

}
