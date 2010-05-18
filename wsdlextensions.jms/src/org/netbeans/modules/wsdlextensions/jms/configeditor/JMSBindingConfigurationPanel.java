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
public class JMSBindingConfigurationPanel extends javax.swing.JPanel {

    private WSDLComponent mComponent;

    /** QName **/
    private QName mQName;

    /** resource bundle for file bc **/
    private ResourceBundle mBundle = ResourceBundle.getBundle(
            "org.netbeans.modules.wsdlextensions.jms.resources.Bundle");

    private static final Logger mLogger = Logger.
            getLogger(JMSBindingConfigurationPanel.class.getName());

    /** Creates new form JMSBindingConfigurationPanel */
    public JMSBindingConfigurationPanel(QName qName, WSDLComponent component) {
        mComponent = component;
        mQName = qName;
        initComponents();
        resetView();
        populateView(mQName, mComponent);
    }

    /** Creates new form JMSBindingConfigurationPanel */
    public JMSBindingConfigurationPanel() {
        this(null, null);
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
        return connectionURLTextField.getText();
    }

    /**
     * Return the message type for input message
     * @return String message type
     */
    String getInputMessageType() {
        return (String) inputTypeComboBox.getSelectedItem();
    }

    /**
     * Return the message type for output message
     * @return String message type
     */
    String getOutputMessageType() {
        return (String) outputTypeComboBox.getSelectedItem();
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

    /**
     * Return the message text for output message
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
     * Return the destination
     * @return String destination
     */
    String getDestination() {
        return destinationTextField.getText();
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
        if ((transactionComboBox.getSelectedItem() != null) &&
                (!transactionComboBox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
            return transactionComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    /**
     * Return the client ID value
     * @return String client ID
     */
    String getClientID() {
        return clientIDTextField.getText();
    }

    /**
     * Return the message selector value
     * @return String message selector
     */
    String getMessageSelector() {
        return msgSelectorTextField.getText();
    }

    /**
     * Return the subscription name value
     * @return String subscription name
     */
    String getSubscriptionName() {
        return subsNameTextField.getText();
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
     * Return the Redeliver value
     * @return String redelivery
     */
    String getRedelivery() {
        return redeliveryTextField.getText();
    }

    /**
     * Return the batch size value
     * @return long batch size
     */
    long getBatchSize() {
        if (batchSizeSpinner.getValue() != null) {
            return new Long(batchSizeSpinner.getValue().toString()).
                    longValue();
        } else {
            return 0;
        }
    }

    /**
     * Return the validate message selector
     * @return String validate message selector
     */
    boolean getValidateMessageSelector() {
        if ((valMsgSelComboBox.getSelectedItem() != null) &&
                (!valMsgSelComboBox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
             return JMSConstants.stringValueIsTrue(valMsgSelComboBox.
                    getSelectedItem().toString());
        } else {
            return false;
        }
    }

    String getSubscriptionDurability() {
        if (durabiltyRBtn.isSelected()) {
            return JMSConstants.DURABLE;
        }else {
            return JMSConstants.NON_DURABLE;
        }
    }

    String getMaxConcurrentConsumer() {
        if (maxConcurrentSpinner.getValue() != null) {
            return maxConcurrentSpinner.getValue().toString();
        } else {
            return null;
        }
    }

    String getDeliverMode() {
        if ((deliveryModeComboBox.getSelectedItem() != null) &&
                (!deliveryModeComboBox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
            return deliveryModeComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    String getTimeToLive() {
        if (timeToLiveSpinner.getValue() != null) {
            return timeToLiveSpinner.getValue().toString();
        } else {
            return null;
        }
    }

    boolean getDisableMessageID() {
        if ((disableMsgIdCombobox.getSelectedItem() != null) &&
                (!disableMsgIdCombobox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
             return JMSConstants.stringValueIsTrue(disableMsgIdCombobox.
                    getSelectedItem().toString());
        } else {
            return false;
        }
    }

    String getPriority() {
        if ((priorityComboBox.getSelectedItem() != null) &&
                (!priorityComboBox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
            return priorityComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    long getTimeout() {
        if (timeoutTextField.getText().length() == 0) {
            return 0;
        } else {
            return new Long(timeoutTextField.getText()).longValue();
        }
    }

    boolean getDisableMessageTimestamp() {
        if ((disableMsgTimeCombobox.getSelectedItem() != null) &&
                (!disableMsgTimeCombobox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
             return JMSConstants.stringValueIsTrue(disableMsgTimeCombobox.
                    getSelectedItem().toString());
        } else {
            return false;
        }
    }

    String getInputCorrelationPart() {
        if ((inputCorrelationPartComboBox.getSelectedItem() != null) &&
                (!inputCorrelationPartComboBox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
            return inputCorrelationPartComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    String getInputDeliveryModePart() {
        if ((inputDeliveryModePartComboBox.getSelectedItem() != null) &&
                (!inputDeliveryModePartComboBox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
            return inputDeliveryModePartComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    String getInputPriorityPart() {
        if ((inputPriorityPartComboBox.getSelectedItem() != null) &&
                (!inputPriorityPartComboBox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
            return inputPriorityPartComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
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

    String getInputUse() {
        if (inputLiteralRBtn.isSelected()) {
            return JMSConstants.LITERAL;
        } else {
            return JMSConstants.ENCODED;
        }
    }

    String getInputEncodingStyle() {
        return inputEncodingStyleTextField.getText();
    }

    String getOutputCorrelationPart() {
        if ((outputCorrelationPartComboBox.getSelectedItem() != null) &&
                (!outputCorrelationPartComboBox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
            return outputCorrelationPartComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    String getOutputDeliveryModePart() {
        if ((outputDeliveryModePartComboBox.getSelectedItem() != null) &&
                (!outputDeliveryModePartComboBox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
            return outputDeliveryModePartComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    String getOutputPriorityPart() {
        if ((outputPriorityPartComboBox.getSelectedItem() != null) &&
                (!outputPriorityPartComboBox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
            return outputPriorityPartComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    String getOutputTypePart() {
        if ((outputTypePartComboBox.getSelectedItem() != null) &&
                (!outputTypePartComboBox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
            return outputTypePartComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    String getOutputMessageIDPart() {
        if ((outputMsgIDPartComboBox.getSelectedItem() != null) &&
                (!outputMsgIDPartComboBox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
            return outputMsgIDPartComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    String getOutputRedeliveredPart() {
        if ((outputRedeliveredPartComboBox.getSelectedItem() != null) &&
                (!outputRedeliveredPartComboBox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
            return outputRedeliveredPartComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    String getOutputTimestamp() {
        if ((outputTimestampPartComboBox.getSelectedItem() != null) &&
                (!outputTimestampPartComboBox.getSelectedItem().toString().
                equals(JMSConstants.NOT_SET))) {
            return outputTimestampPartComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    String getOutputUse() {
        if (outputLiteralRBtn.isSelected()) {
            return JMSConstants.LITERAL;
        } else {
            return JMSConstants.ENCODED;
        }
    }

    String getOutputEncodingStyle() {
        return outputEncodingStyleTextField.getText();
    }

    String getUserName() {
        return userNameTextField.getText();
    }

    String getPassword() {
        char[] pass = passwordTextField.getPassword();
        if (pass.length > 0) {
            String passStr = new String(pass);
            Arrays.fill(pass, '0');
            return passStr;
        }
        return null;
    }

    String getConnectionFactoryName() {
        return connectionFactoryNameTextField.getText();
    }

    String getInitialContextFactory() {
        return initialContextFactoryTextField.getText();
    }

    String getProviderURL() {
        return providerURLTextField.getText();
    }

    String getSecurityPrincipal() {
        return securityPrincipalTextField.getText();
    }

    String getSecurityCredentials() {
        return securityCrendentialsTextField.getText();
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
        jTabbedPane1 = new javax.swing.JTabbedPane();
        generalPanel = new javax.swing.JPanel();
        connectionPanel = new javax.swing.JPanel();
        jLabel43 = new javax.swing.JLabel();
        connectionURLLabel = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        passwordTextField = new javax.swing.JPasswordField();
        userNameTextField = new javax.swing.JTextField();
        connectionURLTextField = new javax.swing.JTextField();
        jSeparator3 = new javax.swing.JSeparator();
        jndiBtn = new javax.swing.JButton();
        operationGeneralPanel = new javax.swing.JPanel();
        operationLab = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        transactionComboBox = new javax.swing.JComboBox();
        destinationTextField = new javax.swing.JTextField();
        jSeparator6 = new javax.swing.JSeparator();
        queueRBtn = new javax.swing.JRadioButton();
        topicRBtn = new javax.swing.JRadioButton();
        topicOnlyPanel = new javax.swing.JPanel();
        clientIDLab = new javax.swing.JLabel();
        clientIDTextField = new javax.swing.JTextField();
        subscriptionDurabilityLab = new javax.swing.JLabel();
        subsNameTextField = new javax.swing.JTextField();
        subscriptionNameLab = new javax.swing.JLabel();
        durabiltyRBtn = new javax.swing.JRadioButton();
        nonDurabilityRBtn = new javax.swing.JRadioButton();
        inputGeneralPanel = new javax.swing.JPanel();
        inputMessageLab = new javax.swing.JLabel();
        inputTypeLabel = new javax.swing.JLabel();
        inputTextLabel = new javax.swing.JLabel();
        inputTextComboBox = new javax.swing.JComboBox();
        inputTypeComboBox = new javax.swing.JComboBox();
        jSeparator4 = new javax.swing.JSeparator();
        outputGeneralPanel = new javax.swing.JPanel();
        outputMessageLab = new javax.swing.JLabel();
        outputTypeLabel = new javax.swing.JLabel();
        outputTextLabel = new javax.swing.JLabel();
        outputTextComboBox = new javax.swing.JComboBox();
        outputTypeComboBox = new javax.swing.JComboBox();
        jSeparator5 = new javax.swing.JSeparator();
        inputMessagesPanelTab = new javax.swing.JPanel();
        inputReadWriteHeadersPanel = new javax.swing.JPanel();
        inputReadWriteLab = new javax.swing.JLabel();
        correlationPartLabel = new javax.swing.JLabel();
        inputDeliveryModePartLab = new javax.swing.JLabel();
        inputDeliveryModePartComboBox = new javax.swing.JComboBox();
        inputCorrelationPartComboBox = new javax.swing.JComboBox();
        jSeparator9 = new javax.swing.JSeparator();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        inputTypePartComboBox = new javax.swing.JComboBox();
        inputPriorityPartComboBox = new javax.swing.JComboBox();
        inputReadOnlyHeadersPanel = new javax.swing.JPanel();
        inputReadOnlyLab = new javax.swing.JLabel();
        inputMsgIDPartLab = new javax.swing.JLabel();
        inputRedeliveredPartLab = new javax.swing.JLabel();
        inputRedeliveredPartComboBox = new javax.swing.JComboBox();
        inputMsgIDPartComboBox = new javax.swing.JComboBox();
        jSeparator10 = new javax.swing.JSeparator();
        jLabel16 = new javax.swing.JLabel();
        inputTimestampPartComboBox = new javax.swing.JComboBox();
        inputEncodingPanel = new javax.swing.JPanel();
        inputEncodingLab = new javax.swing.JLabel();
        inputUseLab = new javax.swing.JLabel();
        inputEncodingStyleLab = new javax.swing.JLabel();
        inputLiteralRBtn = new javax.swing.JRadioButton();
        inputEncodingRBtn = new javax.swing.JRadioButton();
        inputEncodingStyleTextField = new javax.swing.JTextField();
        jSeparator11 = new javax.swing.JSeparator();
        outputMessagesPanelTab = new javax.swing.JPanel();
        outputReadWriteHeadersPanel = new javax.swing.JPanel();
        outputReadWriteLab = new javax.swing.JLabel();
        outputCorrelationPartLabel = new javax.swing.JLabel();
        outputDeliveryModePartLab = new javax.swing.JLabel();
        outputDeliveryModePartComboBox = new javax.swing.JComboBox();
        outputCorrelationPartComboBox = new javax.swing.JComboBox();
        jSeparator15 = new javax.swing.JSeparator();
        jLabel15 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        outputTypePartComboBox = new javax.swing.JComboBox();
        outputPriorityPartComboBox = new javax.swing.JComboBox();
        outputReadOnlyHeadersPanel = new javax.swing.JPanel();
        outputReadOnlyLab = new javax.swing.JLabel();
        outputMsgIDPartLab = new javax.swing.JLabel();
        outputRedeliveredPartLab = new javax.swing.JLabel();
        outputRedeliveredPartComboBox = new javax.swing.JComboBox();
        outputMsgIDPartComboBox = new javax.swing.JComboBox();
        jSeparator16 = new javax.swing.JSeparator();
        jLabel18 = new javax.swing.JLabel();
        outputTimestampPartComboBox = new javax.swing.JComboBox();
        outputEncodingPanel = new javax.swing.JPanel();
        outputEncodingLab = new javax.swing.JLabel();
        outputUseLab = new javax.swing.JLabel();
        outputEncodingStyleLab = new javax.swing.JLabel();
        outputLiteralRBtn = new javax.swing.JRadioButton();
        outputEncodingRBtn = new javax.swing.JRadioButton();
        outputEncodingStyleTextField = new javax.swing.JTextField();
        jSeparator17 = new javax.swing.JSeparator();
        operationPanel = new javax.swing.JPanel();
        consumerPanel = new javax.swing.JPanel();
        jLabel46 = new javax.swing.JLabel();
        jSeparator7 = new javax.swing.JSeparator();
        jLabel30 = new javax.swing.JLabel();
        concurModeComboBox = new javax.swing.JComboBox();
        jLabel34 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        valMsgSelComboBox = new javax.swing.JComboBox();
        msgSelectorTextField = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        redeliveryTextField = new javax.swing.JTextField();
        batchSizeSpinner = new javax.swing.JSpinner();
        maxConcurrentSpinner = new javax.swing.JSpinner();
        producerPanel = new javax.swing.JPanel();
        jLabel47 = new javax.swing.JLabel();
        jSeparator8 = new javax.swing.JSeparator();
        jLabel36 = new javax.swing.JLabel();
        deliveryModeComboBox = new javax.swing.JComboBox();
        priorityComboBox = new javax.swing.JComboBox();
        jLabel39 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        timeoutTextField = new javax.swing.JTextField();
        timeToLiveSpinner = new javax.swing.JSpinner();
        jLabel38 = new javax.swing.JLabel();
        disableMsgIdCombobox = new javax.swing.JComboBox();
        disableMsgTimeCombobox = new javax.swing.JComboBox();
        jLabel41 = new javax.swing.JLabel();

        jndiLabel.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jndiLabel.text")); // NOI18N

        connectionFactoryNameLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.connectionFactoryNameLab.text")); // NOI18N

        initialContextFactoryLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.initialContextFactoryLab.text")); // NOI18N

        providerURLLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.providerURLLab.text")); // NOI18N

        securityPrincipalLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.securityPrincipalLab.text")); // NOI18N

        securityCredentialLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.securityCredentialLab.text")); // NOI18N

        securityCrendentialsTextField.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.securityCrendentialsTextField.text")); // NOI18N

        securityPrincipalTextField.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.securityPrincipalTextField.text")); // NOI18N

        providerURLTextField.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.providerURLTextField.text")); // NOI18N

        initialContextFactoryTextField.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.initialContextFactoryTextField.text")); // NOI18N
        initialContextFactoryTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                initialContextFactoryTextFieldActionPerformed(evt);
            }
        });

        connectionFactoryNameTextField.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.connectionFactoryNameTextField.text")); // NOI18N

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

        jTextField1.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jTextField1.text_1")); // NOI18N

        jLabel26.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel26.text")); // NOI18N

        portTypeLabel.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.portTypeLabel.text")); // NOI18N

        operationNameComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        operationNameComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                operationNameComboBoxItemStateChanged(evt);
            }
        });

        jLabel42.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel42.text")); // NOI18N

        bindingNameComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        operationNameLabel.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.operationNameLabel.text_1")); // NOI18N

        servicePortComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        servicePortComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                servicePortComboBoxItemStateChanged(evt);
            }
        });

        bindingNameLabel.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.bindingNameLabel.text")); // NOI18N

        portTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

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

        setLayout(new java.awt.BorderLayout());

        generalPanel.setLayout(new java.awt.GridBagLayout());

        connectionPanel.setLayout(new java.awt.GridBagLayout());

        jLabel43.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel43.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel43.text")); // NOI18N
        jLabel43.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel43MouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        connectionPanel.add(jLabel43, gridBagConstraints);

        connectionURLLabel.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.connectionURLLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 10);
        connectionPanel.add(connectionURLLabel, gridBagConstraints);

        jLabel4.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel4.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        connectionPanel.add(jLabel4, gridBagConstraints);

        jLabel5.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel5.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        connectionPanel.add(jLabel5, gridBagConstraints);

        passwordTextField.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.passwordTextField.text")); // NOI18N
        passwordTextField.setToolTipText( mBundle.getString("DESC_Attribute_password"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        connectionPanel.add(passwordTextField, gridBagConstraints);

        userNameTextField.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.userNameTextField.text_1")); // NOI18N
        userNameTextField.setToolTipText( mBundle.getString("DESC_Attribute_username"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        connectionPanel.add(userNameTextField, gridBagConstraints);

        connectionURLTextField.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.connectionURLTextField.text_1")); // NOI18N
        connectionURLTextField.setToolTipText(mBundle.getString("DESC_Attribute_connectionURL"));
        connectionURLTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectionURLTextFieldActionPerformed(evt);
            }
        });
        connectionURLTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                connectionURLTextFieldKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                connectionURLTextFieldKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        connectionPanel.add(connectionURLTextField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 80, 0, 0);
        connectionPanel.add(jSeparator3, gridBagConstraints);

        jndiBtn.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jndiBtn.text")); // NOI18N
        jndiBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jndiBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        connectionPanel.add(jndiBtn, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        generalPanel.add(connectionPanel, gridBagConstraints);

        operationGeneralPanel.setLayout(new java.awt.GridBagLayout());

        operationLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        operationLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.operationLab.text")); // NOI18N
        operationLab.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                operationLabMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        operationGeneralPanel.add(operationLab, gridBagConstraints);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        operationGeneralPanel.add(jLabel1, gridBagConstraints);

        jLabel2.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 10);
        operationGeneralPanel.add(jLabel2, gridBagConstraints);

        jLabel3.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        operationGeneralPanel.add(jLabel3, gridBagConstraints);

        transactionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        operationGeneralPanel.add(transactionComboBox, gridBagConstraints);

        destinationTextField.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.destinationTextField.text_1")); // NOI18N
        destinationTextField.setToolTipText( mBundle.getString("DESC_Attribute_destination"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        operationGeneralPanel.add(destinationTextField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 75, 0, 0);
        operationGeneralPanel.add(jSeparator6, gridBagConstraints);

        QTopicBGroup.add(queueRBtn);
        queueRBtn.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.queueRBtn.text")); // NOI18N
        queueRBtn.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                queueRBtnItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        operationGeneralPanel.add(queueRBtn, gridBagConstraints);

        QTopicBGroup.add(topicRBtn);
        topicRBtn.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.topicRBtn.text")); // NOI18N
        topicRBtn.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                topicRBtnItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 70, 0, 0);
        operationGeneralPanel.add(topicRBtn, gridBagConstraints);

        topicOnlyPanel.setLayout(new java.awt.GridBagLayout());

        clientIDLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.clientIDLab.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        topicOnlyPanel.add(clientIDLab, gridBagConstraints);

        clientIDTextField.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.clientIDTextField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        topicOnlyPanel.add(clientIDTextField, gridBagConstraints);

        subscriptionDurabilityLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.subscriptionDurabilityLab.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        topicOnlyPanel.add(subscriptionDurabilityLab, gridBagConstraints);

        subsNameTextField.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.subsNameTextField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        topicOnlyPanel.add(subsNameTextField, gridBagConstraints);

        subscriptionNameLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.subscriptionNameLab.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        topicOnlyPanel.add(subscriptionNameLab, gridBagConstraints);

        durabilityBGrp.add(durabiltyRBtn);
        durabiltyRBtn.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.durabiltyRBtn.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        topicOnlyPanel.add(durabiltyRBtn, gridBagConstraints);

        durabilityBGrp.add(nonDurabilityRBtn);
        nonDurabilityRBtn.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.nonDurabilityRBtn.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 90, 0, 0);
        topicOnlyPanel.add(nonDurabilityRBtn, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        operationGeneralPanel.add(topicOnlyPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        generalPanel.add(operationGeneralPanel, gridBagConstraints);

        inputGeneralPanel.setLayout(new java.awt.GridBagLayout());

        inputMessageLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        inputMessageLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.inputMessageLab.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        inputGeneralPanel.add(inputMessageLab, gridBagConstraints);

        inputTypeLabel.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.inputTypeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        inputGeneralPanel.add(inputTypeLabel, gridBagConstraints);

        inputTextLabel.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.inputTextLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        inputGeneralPanel.add(inputTextLabel, gridBagConstraints);

        inputTextComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        inputTextComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                inputTextComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        inputGeneralPanel.add(inputTextComboBox, gridBagConstraints);

        inputTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        inputGeneralPanel.add(inputTypeComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 100, 0, 0);
        inputGeneralPanel.add(jSeparator4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        generalPanel.add(inputGeneralPanel, gridBagConstraints);

        outputGeneralPanel.setLayout(new java.awt.GridBagLayout());

        outputMessageLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        outputMessageLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.outputMessageLab.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        outputGeneralPanel.add(outputMessageLab, gridBagConstraints);

        outputTypeLabel.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.outputTypeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        outputGeneralPanel.add(outputTypeLabel, gridBagConstraints);

        outputTextLabel.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.outputTextLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        outputGeneralPanel.add(outputTextLabel, gridBagConstraints);

        outputTextComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        outputTextComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                outputTextComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        outputGeneralPanel.add(outputTextComboBox, gridBagConstraints);

        outputTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 100;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        outputGeneralPanel.add(outputTypeComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 110, 0, 0);
        outputGeneralPanel.add(jSeparator5, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        generalPanel.add(outputGeneralPanel, gridBagConstraints);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.generalPanel.TabConstraints.tabTitle"), generalPanel); // NOI18N

        inputMessagesPanelTab.setLayout(new java.awt.GridBagLayout());

        inputReadWriteHeadersPanel.setLayout(new java.awt.GridBagLayout());

        inputReadWriteLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        inputReadWriteLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.inputReadWriteLab.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        inputReadWriteHeadersPanel.add(inputReadWriteLab, gridBagConstraints);

        correlationPartLabel.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.correlationPartLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        inputReadWriteHeadersPanel.add(correlationPartLabel, gridBagConstraints);

        inputDeliveryModePartLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.inputDeliveryModePartLab.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        inputReadWriteHeadersPanel.add(inputDeliveryModePartLab, gridBagConstraints);

        inputDeliveryModePartComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        inputReadWriteHeadersPanel.add(inputDeliveryModePartComboBox, gridBagConstraints);

        inputCorrelationPartComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        inputReadWriteHeadersPanel.add(inputCorrelationPartComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 130, 0, 0);
        inputReadWriteHeadersPanel.add(jSeparator9, gridBagConstraints);

        jLabel13.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel13.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        inputReadWriteHeadersPanel.add(jLabel13, gridBagConstraints);

        jLabel14.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel14.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        inputReadWriteHeadersPanel.add(jLabel14, gridBagConstraints);

        inputTypePartComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 30, 0, 0);
        inputReadWriteHeadersPanel.add(inputTypePartComboBox, gridBagConstraints);

        inputPriorityPartComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        inputPriorityPartComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputPriorityPartComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 30, 0, 0);
        inputReadWriteHeadersPanel.add(inputPriorityPartComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        inputMessagesPanelTab.add(inputReadWriteHeadersPanel, gridBagConstraints);

        inputReadOnlyHeadersPanel.setLayout(new java.awt.GridBagLayout());

        inputReadOnlyLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        inputReadOnlyLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.inputReadOnlyLab.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        inputReadOnlyHeadersPanel.add(inputReadOnlyLab, gridBagConstraints);

        inputMsgIDPartLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.inputMsgIDPartLab.text")); // NOI18N
        inputMsgIDPartLab.setName("inputMsgIDPartLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        inputReadOnlyHeadersPanel.add(inputMsgIDPartLab, gridBagConstraints);

        inputRedeliveredPartLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.inputRedeliveredPartLab.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        inputReadOnlyHeadersPanel.add(inputRedeliveredPartLab, gridBagConstraints);

        inputRedeliveredPartComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        inputReadOnlyHeadersPanel.add(inputRedeliveredPartComboBox, gridBagConstraints);

        inputMsgIDPartComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        inputMsgIDPartComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputMsgIDPartComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        inputReadOnlyHeadersPanel.add(inputMsgIDPartComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 130, 0, 0);
        inputReadOnlyHeadersPanel.add(jSeparator10, gridBagConstraints);

        jLabel16.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel16.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        inputReadOnlyHeadersPanel.add(jLabel16, gridBagConstraints);

        inputTimestampPartComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        inputTimestampPartComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputTimestampPartComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        inputReadOnlyHeadersPanel.add(inputTimestampPartComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        inputMessagesPanelTab.add(inputReadOnlyHeadersPanel, gridBagConstraints);

        inputEncodingPanel.setLayout(new java.awt.GridBagLayout());

        inputEncodingLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        inputEncodingLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.inputEncodingLab.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        inputEncodingPanel.add(inputEncodingLab, gridBagConstraints);

        inputUseLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.inputUseLab.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        inputEncodingPanel.add(inputUseLab, gridBagConstraints);

        inputEncodingStyleLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.inputEncodingStyleLab.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        inputEncodingPanel.add(inputEncodingStyleLab, gridBagConstraints);

        inputEncodingGroup.add(inputLiteralRBtn);
        inputLiteralRBtn.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.inputLiteralRBtn.text")); // NOI18N
        inputLiteralRBtn.setMargin(new java.awt.Insets(2, 0, 2, 2));
        inputLiteralRBtn.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                inputLiteralRBtnItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 30, 0, 0);
        inputEncodingPanel.add(inputLiteralRBtn, gridBagConstraints);

        inputEncodingGroup.add(inputEncodingRBtn);
        inputEncodingRBtn.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.inputEncodingRBtn.text")); // NOI18N
        inputEncodingRBtn.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                inputEncodingRBtnItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 100, 0, 0);
        inputEncodingPanel.add(inputEncodingRBtn, gridBagConstraints);

        inputEncodingStyleTextField.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.inputEncodingStyleTextField.text")); // NOI18N
        inputEncodingStyleTextField.setName("inputEncodingStyleTextField"); // NOI18N
        inputEncodingStyleTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputEncodingStyleTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 30, 0, 0);
        inputEncodingPanel.add(inputEncodingStyleTextField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 70, 0, 0);
        inputEncodingPanel.add(jSeparator11, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        inputMessagesPanelTab.add(inputEncodingPanel, gridBagConstraints);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.inputMessagesPanelTab.TabConstraints.tabTitle"), inputMessagesPanelTab); // NOI18N

        outputMessagesPanelTab.setLayout(new java.awt.GridBagLayout());

        outputReadWriteHeadersPanel.setLayout(new java.awt.GridBagLayout());

        outputReadWriteLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        outputReadWriteLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.outputReadWriteLab.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        outputReadWriteHeadersPanel.add(outputReadWriteLab, gridBagConstraints);

        outputCorrelationPartLabel.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.outputCorrelationPartLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        outputReadWriteHeadersPanel.add(outputCorrelationPartLabel, gridBagConstraints);

        outputDeliveryModePartLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.outputDeliveryModePartLab.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        outputReadWriteHeadersPanel.add(outputDeliveryModePartLab, gridBagConstraints);

        outputDeliveryModePartComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        outputReadWriteHeadersPanel.add(outputDeliveryModePartComboBox, gridBagConstraints);

        outputCorrelationPartComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        outputReadWriteHeadersPanel.add(outputCorrelationPartComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 130, 0, 0);
        outputReadWriteHeadersPanel.add(jSeparator15, gridBagConstraints);

        jLabel15.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel15.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        outputReadWriteHeadersPanel.add(jLabel15, gridBagConstraints);

        jLabel17.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel17.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        outputReadWriteHeadersPanel.add(jLabel17, gridBagConstraints);

        outputTypePartComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 30, 0, 0);
        outputReadWriteHeadersPanel.add(outputTypePartComboBox, gridBagConstraints);

        outputPriorityPartComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 30, 0, 0);
        outputReadWriteHeadersPanel.add(outputPriorityPartComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        outputMessagesPanelTab.add(outputReadWriteHeadersPanel, gridBagConstraints);

        outputReadOnlyHeadersPanel.setLayout(new java.awt.GridBagLayout());

        outputReadOnlyLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        outputReadOnlyLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.outputReadOnlyLab.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        outputReadOnlyHeadersPanel.add(outputReadOnlyLab, gridBagConstraints);

        outputMsgIDPartLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.outputMsgIDPartLab.text")); // NOI18N
        outputMsgIDPartLab.setName("inputMsgIDPartLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        outputReadOnlyHeadersPanel.add(outputMsgIDPartLab, gridBagConstraints);

        outputRedeliveredPartLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.outputRedeliveredPartLab.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        outputReadOnlyHeadersPanel.add(outputRedeliveredPartLab, gridBagConstraints);

        outputRedeliveredPartComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        outputReadOnlyHeadersPanel.add(outputRedeliveredPartComboBox, gridBagConstraints);

        outputMsgIDPartComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        outputMsgIDPartComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputMsgIDPartComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        outputReadOnlyHeadersPanel.add(outputMsgIDPartComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 130, 0, 0);
        outputReadOnlyHeadersPanel.add(jSeparator16, gridBagConstraints);

        jLabel18.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel18.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        outputReadOnlyHeadersPanel.add(jLabel18, gridBagConstraints);

        outputTimestampPartComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        outputTimestampPartComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputTimestampPartComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        outputReadOnlyHeadersPanel.add(outputTimestampPartComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        outputMessagesPanelTab.add(outputReadOnlyHeadersPanel, gridBagConstraints);

        outputEncodingPanel.setLayout(new java.awt.GridBagLayout());

        outputEncodingLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        outputEncodingLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.outputEncodingLab.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        outputEncodingPanel.add(outputEncodingLab, gridBagConstraints);

        outputUseLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.outputUseLab.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        outputEncodingPanel.add(outputUseLab, gridBagConstraints);

        outputEncodingStyleLab.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.outputEncodingStyleLab.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        outputEncodingPanel.add(outputEncodingStyleLab, gridBagConstraints);

        inputEncodingGroup.add(outputLiteralRBtn);
        outputLiteralRBtn.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.outputLiteralRBtn.text")); // NOI18N
        outputLiteralRBtn.setMargin(new java.awt.Insets(2, 0, 2, 2));
        outputLiteralRBtn.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                outputLiteralRBtnItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 30, 0, 0);
        outputEncodingPanel.add(outputLiteralRBtn, gridBagConstraints);

        inputEncodingGroup.add(outputEncodingRBtn);
        outputEncodingRBtn.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.outputEncodingRBtn.text")); // NOI18N
        outputEncodingRBtn.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                outputEncodingRBtnItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 100, 0, 0);
        outputEncodingPanel.add(outputEncodingRBtn, gridBagConstraints);

        outputEncodingStyleTextField.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.outputEncodingStyleTextField.text")); // NOI18N
        outputEncodingStyleTextField.setName("inputEncodingStyleComboBox"); // NOI18N
        outputEncodingStyleTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputEncodingStyleTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 30, 0, 0);
        outputEncodingPanel.add(outputEncodingStyleTextField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 70, 0, 0);
        outputEncodingPanel.add(jSeparator17, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        outputMessagesPanelTab.add(outputEncodingPanel, gridBagConstraints);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.outputMessagesPanelTab.TabConstraints.tabTitle"), outputMessagesPanelTab); // NOI18N

        operationPanel.setLayout(new java.awt.GridBagLayout());

        consumerPanel.setLayout(new java.awt.GridBagLayout());

        jLabel46.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel46.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel46.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        consumerPanel.add(jLabel46, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 75, 0, 0);
        consumerPanel.add(jSeparator7, gridBagConstraints);

        jLabel30.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel30.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        consumerPanel.add(jLabel30, gridBagConstraints);

        concurModeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        consumerPanel.add(concurModeComboBox, gridBagConstraints);

        jLabel34.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel34.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        consumerPanel.add(jLabel34, gridBagConstraints);

        jLabel32.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel32.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        consumerPanel.add(jLabel32, gridBagConstraints);

        valMsgSelComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        consumerPanel.add(valMsgSelComboBox, gridBagConstraints);

        msgSelectorTextField.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.msgSelectorTextField.text")); // NOI18N
        msgSelectorTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                msgSelectorTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 60, 0, 0);
        consumerPanel.add(msgSelectorTextField, gridBagConstraints);

        jLabel28.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel28.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        consumerPanel.add(jLabel28, gridBagConstraints);

        jLabel31.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel31.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        consumerPanel.add(jLabel31, gridBagConstraints);

        jLabel35.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel35.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        consumerPanel.add(jLabel35, gridBagConstraints);

        redeliveryTextField.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.redeliveryTextField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 60, 0, 0);
        consumerPanel.add(redeliveryTextField, gridBagConstraints);

        batchSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(Long.valueOf(0L), Long.valueOf(0L), null, Long.valueOf(1L)));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        consumerPanel.add(batchSizeSpinner, gridBagConstraints);

        maxConcurrentSpinner.setModel(new javax.swing.SpinnerNumberModel(Long.valueOf(0L), Long.valueOf(0L), null, Long.valueOf(1L)));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 60, 0, 0);
        consumerPanel.add(maxConcurrentSpinner, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        operationPanel.add(consumerPanel, gridBagConstraints);

        producerPanel.setLayout(new java.awt.GridBagLayout());

        jLabel47.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel47.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel47.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        producerPanel.add(jLabel47, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 65, 0, 0);
        producerPanel.add(jSeparator8, gridBagConstraints);

        jLabel36.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel36.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        producerPanel.add(jLabel36, gridBagConstraints);

        deliveryModeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 42, 0, 0);
        producerPanel.add(deliveryModeComboBox, gridBagConstraints);

        priorityComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        producerPanel.add(priorityComboBox, gridBagConstraints);

        jLabel39.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel39.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        producerPanel.add(jLabel39, gridBagConstraints);

        jLabel37.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel37.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        producerPanel.add(jLabel37, gridBagConstraints);

        jLabel40.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel40.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        producerPanel.add(jLabel40, gridBagConstraints);

        timeoutTextField.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.timeoutTextField.text")); // NOI18N
        timeoutTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                timeoutTextFieldKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        producerPanel.add(timeoutTextField, gridBagConstraints);

        timeToLiveSpinner.setModel(new javax.swing.SpinnerNumberModel(Long.valueOf(0L), Long.valueOf(0L), null, Long.valueOf(1L)));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 42, 0, 0);
        producerPanel.add(timeToLiveSpinner, gridBagConstraints);

        jLabel38.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel38.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        producerPanel.add(jLabel38, gridBagConstraints);

        disableMsgIdCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        disableMsgIdCombobox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disableMsgIdComboboxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 42, 0, 0);
        producerPanel.add(disableMsgIdCombobox, gridBagConstraints);

        disableMsgTimeCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        producerPanel.add(disableMsgTimeCombobox, gridBagConstraints);

        jLabel41.setText(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.jLabel41.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        producerPanel.add(jLabel41, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        operationPanel.add(producerPanel, gridBagConstraints);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(JMSBindingConfigurationPanel.class, "JMSBindingConfigurationPanel.operationPanel.TabConstraints.tabTitle"), operationPanel); // NOI18N

        add(jTabbedPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

private void inputMsgIDPartComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputMsgIDPartComboBoxActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_inputMsgIDPartComboBoxActionPerformed

private void timeoutTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_timeoutTextFieldKeyTyped
// TODO add your handling code here:
    if (!Character.isDigit(evt.getKeyChar())) {
        evt.consume();
    }
}//GEN-LAST:event_timeoutTextFieldKeyTyped

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
                JMSMessage inputMessage = getInputJMSMessage(binding,
                        selectedOperation);
                updateInputMessageView(inputMessage);

                JMSMessage outputMessage = getOutputJMSMessage(binding,
                        selectedOperation);
                updateOutputMessageView(outputMessage);
            }
        }
    }
}//GEN-LAST:event_operationNameComboBoxItemStateChanged

private void msgSelectorTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_msgSelectorTextFieldActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_msgSelectorTextFieldActionPerformed

private void inputTimestampPartComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputTimestampPartComboBoxActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_inputTimestampPartComboBoxActionPerformed

private void connectionURLTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectionURLTextFieldActionPerformed
    // TODO add your handling code here:
    updateJNDISection();
}//GEN-LAST:event_connectionURLTextFieldActionPerformed

private void inputEncodingStyleTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputEncodingStyleTextFieldActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_inputEncodingStyleTextFieldActionPerformed

private void jndiBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jndiBtnActionPerformed
    // TODO add your handling code here:
//    InboundActionStepWizard action = (InboundActionStepWizard) SystemAction.
//            get(InboundActionStepWizard.class);
//    action.performAction();
}//GEN-LAST:event_jndiBtnActionPerformed

private void queueRBtnItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_queueRBtnItemStateChanged
    // TODO add your handling code here:
    updateTopicOnlySection();
}//GEN-LAST:event_queueRBtnItemStateChanged

private void topicRBtnItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_topicRBtnItemStateChanged
    // TODO add your handling code here:
    updateTopicOnlySection();
}//GEN-LAST:event_topicRBtnItemStateChanged

private void connectionURLTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_connectionURLTextFieldKeyTyped
    // TODO add your handling code here:
    //updateJNDISection();
}//GEN-LAST:event_connectionURLTextFieldKeyTyped

private void outputMsgIDPartComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputMsgIDPartComboBoxActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_outputMsgIDPartComboBoxActionPerformed

private void outputTimestampPartComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputTimestampPartComboBoxActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_outputTimestampPartComboBoxActionPerformed

private void outputEncodingStyleTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputEncodingStyleTextFieldActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_outputEncodingStyleTextFieldActionPerformed

private void connectionURLTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_connectionURLTextFieldKeyPressed
    // TODO add your handling code here:
    //updateJNDISection();
}//GEN-LAST:event_connectionURLTextFieldKeyPressed

private void inputPriorityPartComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputPriorityPartComboBoxActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_inputPriorityPartComboBoxActionPerformed

private void inputLiteralRBtnItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_inputLiteralRBtnItemStateChanged
    // TODO add your handling code here:
    updateEncodingSection();
}//GEN-LAST:event_inputLiteralRBtnItemStateChanged

private void inputEncodingRBtnItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_inputEncodingRBtnItemStateChanged
    // TODO add your handling code here:
    updateEncodingSection();
}//GEN-LAST:event_inputEncodingRBtnItemStateChanged

private void outputLiteralRBtnItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_outputLiteralRBtnItemStateChanged
    // TODO add your handling code here:
    updateEncodingSection();
}//GEN-LAST:event_outputLiteralRBtnItemStateChanged

private void outputEncodingRBtnItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_outputEncodingRBtnItemStateChanged
    // TODO add your handling code here:
    updateEncodingSection();
}//GEN-LAST:event_outputEncodingRBtnItemStateChanged

private void inputTextComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_inputTextComboBoxItemStateChanged
    // TODO add your handling code here:
    validateTextPart();
}//GEN-LAST:event_inputTextComboBoxItemStateChanged

private void outputTextComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_outputTextComboBoxItemStateChanged
    // TODO add your handling code here:
    validateTextPart();
}//GEN-LAST:event_outputTextComboBoxItemStateChanged

private void jLabel43MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel43MouseClicked
    // TODO add your handling code here:
//        InboundActionStepWizard action = (InboundActionStepWizard) SystemAction.get(InboundActionStepWizard.class);
//        action.performAction(mQName, mComponent);
}//GEN-LAST:event_jLabel43MouseClicked

private void disableMsgIdComboboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disableMsgIdComboboxActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_disableMsgIdComboboxActionPerformed

private void operationLabMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_operationLabMouseClicked
    // TODO add your handling code here:
//        OutboundActionStepWizard action = (OutboundActionStepWizard) SystemAction.get(OutboundActionStepWizard.class);
//        action.performAction(mQName, mComponent);
}//GEN-LAST:event_operationLabMouseClicked



    private void resetView() {
        servicePortComboBox.setEnabled(false);
        servicePortComboBox.removeAllItems();
        bindingNameComboBox.removeAllItems();
        transactionComboBox.removeAllItems();
        inputTypeComboBox.removeAllItems();
        outputTypeComboBox.removeAllItems();
        inputTextComboBox.removeAllItems();
        outputTextComboBox.removeAllItems();
        portTypeComboBox.removeAllItems();
        operationNameComboBox.removeAllItems();
        valMsgSelComboBox.removeAllItems();
        concurModeComboBox.removeAllItems();
        deliveryModeComboBox.removeAllItems();
        disableMsgIdCombobox.removeAllItems();
        priorityComboBox.removeAllItems();
        disableMsgTimeCombobox.removeAllItems();
        inputCorrelationPartComboBox.removeAllItems();
        inputDeliveryModePartComboBox.removeAllItems();
        inputPriorityPartComboBox.removeAllItems();
        inputTypePartComboBox.removeAllItems();
        inputMsgIDPartComboBox.removeAllItems();
        inputRedeliveredPartComboBox.removeAllItems();
        inputTimestampPartComboBox.removeAllItems();
//        inputUseComboBox.removeAllItems();
        outputCorrelationPartComboBox.removeAllItems();
        outputDeliveryModePartComboBox.removeAllItems();
        outputPriorityPartComboBox.removeAllItems();
        outputTypePartComboBox.removeAllItems();
        outputMsgIDPartComboBox.removeAllItems();
        outputRedeliveredPartComboBox.removeAllItems();
        outputTimestampPartComboBox.removeAllItems();

        transactionComboBox.addItem(JMSConstants.TRANSACTION_NONE);
        transactionComboBox.addItem(JMSConstants.TRANSACTION_LOCAL);
        transactionComboBox.addItem(JMSConstants.TRANSACTION_XA);
        inputTypeComboBox.addItem(JMSConstants.MAP_MESSAGE);
        inputTypeComboBox.addItem(JMSConstants.MESSAGE_MESSAGE);
        inputTypeComboBox.addItem(JMSConstants.BYTES_MESSAGE);  
        outputTypeComboBox.addItem(JMSConstants.MAP_MESSAGE);
        outputTypeComboBox.addItem(JMSConstants.MESSAGE_MESSAGE);
        outputTypeComboBox.addItem(JMSConstants.BYTES_MESSAGE);  
        valMsgSelComboBox.addItem(JMSConstants.NOT_SET);
        valMsgSelComboBox.addItem(JMSConstants.BOOLEAN_FALSE);
        valMsgSelComboBox.addItem(JMSConstants.BOOLEAN_TRUE);
        queueRBtn.setSelected(true);
        concurModeComboBox.addItem(JMSConstants.NOT_SET);
        concurModeComboBox.addItem(JMSConstants.CC);
        concurModeComboBox.addItem(JMSConstants.SYNC);
        deliveryModeComboBox.addItem(JMSConstants.NOT_SET);
        deliveryModeComboBox.addItem(JMSConstants.DELIVERYMODE_PERSISTENT);
        deliveryModeComboBox.addItem(JMSConstants.DELIVERYMODE_NON_PERSISTENT);
        priorityComboBox.addItem(JMSConstants.NOT_SET);
        priorityComboBox.addItem(JMSConstants.PRIORITY_DEFAULT);
        priorityComboBox.addItem(Integer.toString(JMSConstants.PRIORITY_0));
        priorityComboBox.addItem(Integer.toString(JMSConstants.PRIORITY_1));
        priorityComboBox.addItem(Integer.toString(JMSConstants.PRIORITY_2));
        priorityComboBox.addItem(Integer.toString(JMSConstants.PRIORITY_3));
        priorityComboBox.addItem(Integer.toString(JMSConstants.PRIORITY_4));
        priorityComboBox.addItem(Integer.toString(JMSConstants.PRIORITY_5));
        priorityComboBox.addItem(Integer.toString(JMSConstants.PRIORITY_6));
        priorityComboBox.addItem(Integer.toString(JMSConstants.PRIORITY_7));
        priorityComboBox.addItem(Integer.toString(JMSConstants.PRIORITY_8));
        priorityComboBox.addItem(Integer.toString(JMSConstants.PRIORITY_9));
        disableMsgIdCombobox.addItem(JMSConstants.NOT_SET);
        disableMsgIdCombobox.addItem(JMSConstants.BOOLEAN_FALSE);
        disableMsgIdCombobox.addItem(JMSConstants.BOOLEAN_TRUE);
        disableMsgTimeCombobox.addItem(JMSConstants.NOT_SET);
        disableMsgTimeCombobox.addItem(JMSConstants.BOOLEAN_FALSE);
        disableMsgTimeCombobox.addItem(JMSConstants.BOOLEAN_TRUE);
        inputLiteralRBtn.setSelected(true);
        outputLiteralRBtn.setSelected(true);
//        inputUseComboBox.addItem(JMSConstants.NOT_SET);
//        inputUseComboBox.addItem(JMSConstants.LITERAL);
//        inputUseComboBox.addItem(JMSConstants.ENCODED);
//        outputUseComboBox.addItem(JMSConstants.NOT_SET);
//        outputUseComboBox.addItem(JMSConstants.LITERAL);
//        outputUseComboBox.addItem(JMSConstants.ENCODED);
    }

    public void populateView(QName qName, WSDLComponent component) {
        mQName = qName;
        mComponent = component;
        resetView();

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

                    JMSMessage outputMessage = getOutputJMSMessage(binding,
                            operationNameComboBox.getSelectedItem().toString());
                    updateOutputMessageView(outputMessage);
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
            inputDeliveryModePartComboBox.setSelectedItem(
                    inputJMSMessage.getDeliveryModePart());
            inputPriorityPartComboBox.setSelectedItem(
                    inputJMSMessage.getPriorityPart());
            inputTypePartComboBox.setSelectedItem(
                    inputJMSMessage.getTypePart());
            inputMsgIDPartComboBox.setSelectedItem(
                    inputJMSMessage.getMessageIDPart());
            inputRedeliveredPartComboBox.setSelectedItem(
                    inputJMSMessage.getRedeliveredPart());
            inputTimestampPartComboBox.setSelectedItem(
                    inputJMSMessage.getTimestampPart());
            if (inputJMSMessage.getUse() != null) {
                if (inputJMSMessage.getUse().equals(JMSConstants.ENCODED)) {
                    inputEncodingRBtn.setSelected(true);
                } else {
                    inputEncodingRBtn.setSelected(false);
                }
            } else {
                inputEncodingRBtn.setSelected(false);
            }
            inputEncodingStyleTextField.setText(
                    inputJMSMessage.getJMSEncodingStyle());
        }
    }

    private void updateOutputMessageView(JMSMessage outputJMSMessage) {
        if (outputJMSMessage != null) {
            outputTypeComboBox.setSelectedItem(
                    outputJMSMessage.getMessageType());
            outputTextComboBox.setSelectedItem(outputJMSMessage.getTextPart());
            outputCorrelationPartComboBox.setSelectedItem(
                    outputJMSMessage.getCorrelationIdPart());
            outputDeliveryModePartComboBox.setSelectedItem(
                    outputJMSMessage.getDeliveryModePart());
            outputPriorityPartComboBox.setSelectedItem(
                    outputJMSMessage.getPriorityPart());
            outputTypePartComboBox.setSelectedItem(
                    outputJMSMessage.getTypePart());
            outputMsgIDPartComboBox.setSelectedItem(
                    outputJMSMessage.getMessageIDPart());
            outputRedeliveredPartComboBox.setSelectedItem(
                    outputJMSMessage.getRedeliveredPart());
            outputTimestampPartComboBox.setSelectedItem(
                    outputJMSMessage.getTimestampPart());

            if (outputJMSMessage.getUse() != null) {
                if (outputJMSMessage.getUse().equals(JMSConstants.ENCODED)) {
                    outputEncodingRBtn.setSelected(true);
                } else {
                    outputEncodingRBtn.setSelected(false);
                }
            } else {
                outputLiteralRBtn.setSelected(false);
            }
            outputEncodingStyleTextField.setText(
                    outputJMSMessage.getJMSEncodingStyle());
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
                    if (bo != null) {
                        List<JMSMessage> outputJMSMessages =                            
                                bo.getExtensibilityElements(JMSMessage.class);
                        if (outputJMSMessages.size() > 0) {
                            outputJMSMessage = outputJMSMessages.get(0);
                            break;
                        }
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
                                queueRBtn.setSelected(true);
                            }
                        }
                        // get Transactino type
                        transactionComboBox.setSelectedItem(jmsOp.
                                getAttribute(JMSOperation.ATTR_TRANSACTION));
                        clientIDTextField.setText(jmsOp.
                                getAttribute(JMSOperation.ATTR_CLIENT_ID));
                        msgSelectorTextField.setText(jmsOp.getAttribute(
                                JMSOperation.ATTR_MESSAGE_SELECTOR));
                        subsNameTextField.setText(jmsOp.getAttribute(
                                JMSOperation.ATTR_SUBSCRIPTION_NAME));
                        concurModeComboBox.setSelectedItem(jmsOp.getAttribute(
                                JMSOperation.ATTR_CONCURRENCY_MODE));
                        redeliveryTextField.setText(jmsOp.getAttribute(
                                JMSOperation.ATTR_REDELIVERY_HANDLING));
                        //batchSizeTextField.setText(jmsOp.
                        //        getAttribute(JMSOperation.ATTR_BATCH_SZIE));
                        //valMsgSelComboBox.setSelectedItem(jmsOp.
                        //        getAttribute(JMSOperation.ATTR_));

                        batchSizeSpinner.setValue(jmsOp.getBatchSize());
                        if (jmsOp.getSubscriptionDurability() != null) {
                            if (jmsOp.getSubscriptionDurability().equals(JMSConstants.DURABLE)) {
                                durabiltyRBtn.setSelected(true);
                            } else {
                                nonDurabilityRBtn.setSelected(true);
                            }
                        } else {
                            durabiltyRBtn.setSelected(true);
                        }
                        if (jmsOp.getMaxConcurrentConsumers() != null) {
                            maxConcurrentSpinner.setValue(jmsOp.
                                getMaxConcurrentConsumers());
                        } else {
                            maxConcurrentSpinner.setValue(0);
                        }
                        deliveryModeComboBox.setSelectedItem(jmsOp.getDeliveryMode());
                        timeToLiveSpinner.setValue(jmsOp.getTimeToLive());

                        disableMsgIdCombobox.setSelectedItem(jmsOp.getAttribute(
                                JMSOperation.ATTR_DISABLE_MESSAGE_ID));
                        priorityComboBox.setSelectedItem(jmsOp.
                                getAttribute(JMSOperation.ATTR_PRIORITY));
                        timeoutTextField.setText(jmsOp.
                                getAttribute(JMSOperation.ATTR_TIME_TO_LIVE));
                        disableMsgTimeCombobox.setSelectedItem(jmsOp.
                                getAttribute(JMSOperation.
                                ATTR_DISABLE_MESSAGE_TIMESTAMP));
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
                        // get input message type
                        inputTypeComboBox.setSelectedItem(inputJMSMessage.
                                getMessageType());
                        // get input text
                        inputTextComboBox.setSelectedItem(inputJMSMessage.
                                getTextPart());
                    }
                }
                BindingOutput bo = bop.getBindingOutput();
                if (bo != null) {
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
    }

    private void updateServiceView(JMSAddress jmsAddress) {
        if (jmsAddress != null) {
            connectionURLTextField.setText(jmsAddress.
                    getAttribute(JMSAddress.ATTR_CONNECTION_URL));
            updateJNDISection();
            userNameTextField.setText(jmsAddress.
                    getAttribute(JMSAddress.ATTR_USERNAME));
            passwordTextField.setText(jmsAddress.
                    getAttribute(JMSAddress.ATTR_PASSWORD));
            connectionFactoryNameTextField.setText(jmsAddress.
                    getAttribute(JMSAddress.ATTR_JNDI_CONNECTION_FACTORY_NAME));
            initialContextFactoryTextField.setText(jmsAddress.
                    getAttribute(JMSAddress.ATTR_JNDI_INITIAL_CONTEXT_FACTORY));
            providerURLTextField.setText(jmsAddress.
                    getAttribute(JMSAddress.ATTR_JNDI_PROVIDER_URL));
            securityPrincipalTextField.setText(jmsAddress.
                    getAttribute(JMSAddress.ATTR_JNDI_SECURITY_PRINCIPAL));
            securityCrendentialsTextField.setText(jmsAddress.
                    getAttribute(JMSAddress.ATTR_JNDI_SECURITY_CRDENTIALS));
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
                    if (input != null) {
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
                    if (output != null) {
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
                inputDeliveryModePartComboBox.addItem(partName);
                inputPriorityPartComboBox.addItem(partName);
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
                outputTextComboBox.addItem(partName);
                outputCorrelationPartComboBox.addItem(partName);
                outputDeliveryModePartComboBox.addItem(partName);
                outputPriorityPartComboBox.addItem(partName);
                outputTypePartComboBox.addItem(partName);
                outputMsgIDPartComboBox.addItem(partName);
                outputRedeliveredPartComboBox.addItem(partName);
                outputTimestampPartComboBox.addItem(partName);
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
        String val = getConnectionURL();
        if ((val != null) && (val.toLowerCase().startsWith("jndi://"))) {
            jndiBtn.setEnabled(true);
        } else {
            jndiBtn.setEnabled(false);
        }
    }

    private void updateTopicOnlySection() {
        subscriptionDurabilityLab.setEnabled(topicRBtn.isSelected());
        subscriptionNameLab.setEnabled(topicRBtn.isSelected());
        durabiltyRBtn.setEnabled(topicRBtn.isSelected());
        nonDurabilityRBtn.setEnabled(topicRBtn.isSelected());
        subscriptionNameLab.setEnabled(topicRBtn.isSelected());
        subsNameTextField.setEnabled(topicRBtn.isSelected());
        clientIDLab.setEnabled(topicRBtn.isSelected());
        clientIDTextField.setEnabled(topicRBtn.isSelected());
    }

    private void updateEncodingSection() {
        inputEncodingStyleLab.setEnabled(inputEncodingRBtn.isSelected());
        inputEncodingStyleTextField.setEnabled(inputEncodingRBtn.isSelected());

        outputEncodingStyleLab.setEnabled(outputEncodingRBtn.isSelected());
        outputEncodingStyleTextField.setEnabled(outputEncodingRBtn.isSelected());
    }

    private void validateTextPart() {
        if (getInputMessageText() == null) {
             firePropertyChange(
                    ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_ERROR_EVT, null,
                    NbBundle.getMessage(JMSBindingConfigurationPanel.class,
                    "JMSBindingConfiguratonnPanel.INPUT_TEXT_EMPTY"));
             return;
        }
        if (getOutputMessageText() == null) {
             firePropertyChange(
                    ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_ERROR_EVT, null,
                    NbBundle.getMessage(JMSBindingConfigurationPanel.class,
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
    private javax.swing.JSpinner batchSizeSpinner;
    private javax.swing.JComboBox bindingNameComboBox;
    private javax.swing.JLabel bindingNameLabel;
    private javax.swing.JLabel clientIDLab;
    private javax.swing.JTextField clientIDTextField;
    private javax.swing.JComboBox concurModeComboBox;
    private javax.swing.JLabel connectionFactoryNameLab;
    private javax.swing.JTextField connectionFactoryNameTextField;
    private javax.swing.JPanel connectionPanel;
    private javax.swing.JLabel connectionURLLabel;
    private javax.swing.JTextField connectionURLTextField;
    private javax.swing.JPanel consumerPanel;
    private javax.swing.JLabel correlationPartLabel;
    private javax.swing.JComboBox deliveryModeComboBox;
    private javax.swing.JTextField destinationTextField;
    private javax.swing.JComboBox disableMsgIdCombobox;
    private javax.swing.JComboBox disableMsgTimeCombobox;
    private javax.swing.ButtonGroup durabilityBGrp;
    private javax.swing.JRadioButton durabiltyRBtn;
    private javax.swing.JPanel generalPanel;
    private javax.swing.JLabel initialContextFactoryLab;
    private javax.swing.JTextField initialContextFactoryTextField;
    private javax.swing.JComboBox inputCorrelationPartComboBox;
    private javax.swing.JComboBox inputDeliveryModePartComboBox;
    private javax.swing.JLabel inputDeliveryModePartLab;
    private javax.swing.ButtonGroup inputEncodingGroup;
    private javax.swing.JLabel inputEncodingLab;
    private javax.swing.JPanel inputEncodingPanel;
    private javax.swing.JRadioButton inputEncodingRBtn;
    private javax.swing.JLabel inputEncodingStyleLab;
    private javax.swing.JTextField inputEncodingStyleTextField;
    private javax.swing.JPanel inputGeneralPanel;
    private javax.swing.JRadioButton inputLiteralRBtn;
    private javax.swing.JLabel inputMessageLab;
    private javax.swing.JPanel inputMessagesPanelTab;
    private javax.swing.JComboBox inputMsgIDPartComboBox;
    private javax.swing.JLabel inputMsgIDPartLab;
    private javax.swing.JComboBox inputPriorityPartComboBox;
    private javax.swing.JPanel inputReadOnlyHeadersPanel;
    private javax.swing.JLabel inputReadOnlyLab;
    private javax.swing.JPanel inputReadWriteHeadersPanel;
    private javax.swing.JLabel inputReadWriteLab;
    private javax.swing.JComboBox inputRedeliveredPartComboBox;
    private javax.swing.JLabel inputRedeliveredPartLab;
    private javax.swing.JComboBox inputTextComboBox;
    private javax.swing.JLabel inputTextLabel;
    private javax.swing.JComboBox inputTimestampPartComboBox;
    private javax.swing.JComboBox inputTypeComboBox;
    private javax.swing.JLabel inputTypeLabel;
    private javax.swing.JComboBox inputTypePartComboBox;
    private javax.swing.JLabel inputUseLab;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator15;
    private javax.swing.JSeparator jSeparator16;
    private javax.swing.JSeparator jSeparator17;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton jndiBtn;
    private javax.swing.JLabel jndiLabel;
    private javax.swing.JPanel jndiSectionPanel;
    private javax.swing.JSpinner maxConcurrentSpinner;
    private javax.swing.JTextField msgSelectorTextField;
    private javax.swing.JRadioButton nonDurabilityRBtn;
    private javax.swing.JPanel operationGeneralPanel;
    private javax.swing.JLabel operationLab;
    private javax.swing.JComboBox operationNameComboBox;
    private javax.swing.JLabel operationNameLabel;
    private javax.swing.JPanel operationPanel;
    private javax.swing.JComboBox outputCorrelationPartComboBox;
    private javax.swing.JLabel outputCorrelationPartLabel;
    private javax.swing.JComboBox outputDeliveryModePartComboBox;
    private javax.swing.JLabel outputDeliveryModePartLab;
    private javax.swing.ButtonGroup outputEncodingGroup;
    private javax.swing.JLabel outputEncodingLab;
    private javax.swing.JPanel outputEncodingPanel;
    private javax.swing.JRadioButton outputEncodingRBtn;
    private javax.swing.JLabel outputEncodingStyleLab;
    private javax.swing.JTextField outputEncodingStyleTextField;
    private javax.swing.JPanel outputGeneralPanel;
    private javax.swing.JRadioButton outputLiteralRBtn;
    private javax.swing.JLabel outputMessageLab;
    private javax.swing.JPanel outputMessagesPanelTab;
    private javax.swing.JComboBox outputMsgIDPartComboBox;
    private javax.swing.JLabel outputMsgIDPartLab;
    private javax.swing.JComboBox outputPriorityPartComboBox;
    private javax.swing.JPanel outputReadOnlyHeadersPanel;
    private javax.swing.JLabel outputReadOnlyLab;
    private javax.swing.JPanel outputReadWriteHeadersPanel;
    private javax.swing.JLabel outputReadWriteLab;
    private javax.swing.JComboBox outputRedeliveredPartComboBox;
    private javax.swing.JLabel outputRedeliveredPartLab;
    private javax.swing.JComboBox outputTextComboBox;
    private javax.swing.JLabel outputTextLabel;
    private javax.swing.JComboBox outputTimestampPartComboBox;
    private javax.swing.JComboBox outputTypeComboBox;
    private javax.swing.JLabel outputTypeLabel;
    private javax.swing.JComboBox outputTypePartComboBox;
    private javax.swing.JLabel outputUseLab;
    private javax.swing.JPasswordField passwordTextField;
    private javax.swing.JPanel portBindingPanel;
    private javax.swing.JComboBox portTypeComboBox;
    private javax.swing.JLabel portTypeLabel;
    private javax.swing.JComboBox priorityComboBox;
    private javax.swing.JPanel producerPanel;
    private javax.swing.JLabel providerURLLab;
    private javax.swing.JTextField providerURLTextField;
    private javax.swing.JRadioButton queueRBtn;
    private javax.swing.JTextField redeliveryTextField;
    private javax.swing.JLabel securityCredentialLab;
    private javax.swing.JTextField securityCrendentialsTextField;
    private javax.swing.JLabel securityPrincipalLab;
    private javax.swing.JTextField securityPrincipalTextField;
    private javax.swing.JComboBox servicePortComboBox;
    private javax.swing.JTextField subsNameTextField;
    private javax.swing.JLabel subscriptionDurabilityLab;
    private javax.swing.JLabel subscriptionNameLab;
    private javax.swing.JSpinner timeToLiveSpinner;
    private javax.swing.JTextField timeoutTextField;
    private javax.swing.JPanel topicOnlyPanel;
    private javax.swing.JRadioButton topicRBtn;
    private javax.swing.JComboBox transactionComboBox;
    private javax.swing.JTextField userNameTextField;
    private javax.swing.JComboBox valMsgSelComboBox;
    // End of variables declaration//GEN-END:variables

}
