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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.namespace.QName;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.wsdlextensions.jms.JMSAddress;
import org.netbeans.modules.wsdlextensions.jms.JMSBinding;
import org.netbeans.modules.wsdlextensions.jms.JMSConstants;
import org.netbeans.modules.wsdlextensions.jms.JMSMessage;
import org.netbeans.modules.wsdlextensions.jms.JMSOperation;
import org.netbeans.modules.wsdlextensions.jms.validator.JMSComponentValidator;
import org.netbeans.modules.wsdlextensions.jms.validator.RedeliveryHandlingParser;
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
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
//import org.openide.nodes.Node;
import org.openide.util.NbBundle;
//import org.openide.windows.TopComponent;

/**
 * JMSBindingConfigurationPanel - Panel that allows configuration of
 * properties specifically for JMS Binding component
 *
 * @author  jalmero
 */
public class JMSConsumerPanel extends javax.swing.JPanel {

    private WSDLComponent mComponent;

    /** QName **/
    private QName mQName;

    /** resource bundle for file bc **/
    private ResourceBundle mBundle = ResourceBundle.getBundle(
            "org.netbeans.modules.wsdlextensions.jms.resources.Bundle");

    private static final Logger mLogger = Logger.
            getLogger(JMSConsumerPanel.class.getName());

    private DescriptionPanel mDescPanel = null;
    private DescriptionPanel descPanel = null;

    private MyItemListener mItemListener = null;

    private MyActionListener mActionListener = null;
    private MyDocumentListener mDocumentListener = null;    

    private Dialog mDetailsDlg = null;
    private DialogDescriptor mDetailsDlgDesc = null;    
    
    /**
     * Project associated with this wsdl model
     */
    Project mProject = null;    

    /** Creates new form JMSBindingConfigurationPanel */
    public JMSConsumerPanel(QName qName, WSDLComponent component) {
        initComponents();
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
        initListeners();
        setAccessibility();
    }

    public void setDescriptionPanel(DescriptionPanel descPanel) {
        mDescPanel = descPanel;
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
        return trimTextFieldInput(msgSelectorTextField.getText());
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
     * Return the deliver mode value
     * 
     * @return
     */
    String getDeliveryMode() {
        if (synchRBtn.isSelected()) {
            return JMSConstants.SYNC;
        } else {
            return JMSConstants.CC;
        }
    }

    /**
     * Return the Redeliver value
     * @return String redelivery
     */
    public String getRedelivery() {
        return trimTextFieldInput(redeliveryTextField.getText());
    }    
    
    /**
     * Return the Redeliver value
     * @return String redelivery
     */
    private String getRedeliveryValue() {
        // taken from jmsjca implementation
        String redeliveryStr = delayTextField.getText() == null ? "" : delayTextField.getText().trim(); // NOI18N
        if (redeliveryStr.length() > 0 && !redeliveryStr.endsWith(";")) { // NOI18N
            redeliveryStr = redeliveryStr + ";"; // NOI18N
        }
        String timesStr = ((Number) moveDeleteAfterSpinner.getValue()).toString();
        if (actionMoveRadioButton.isSelected()) {
            String destStr = moveToDestinationTextField.getText() == null ? "" : moveToDestinationTextField.getText().trim(); // NOI18N
            if (moveToQueueRadioButton.isSelected()) {
                redeliveryStr = redeliveryStr + timesStr + ":move(queue:" + destStr + ")"; // NOI18N
            } else if (moveToTopicRadioButton.isSelected()) {
                redeliveryStr = redeliveryStr + timesStr + ":move(topic:" + destStr + ")"; // NOI18N
            } else {
                redeliveryStr = redeliveryStr + timesStr + ":move(same:" + destStr + ")"; // NOI18N
            }
        } else if (actionDeleteRadioButton.isSelected()) {
            redeliveryStr = redeliveryStr + timesStr + ":delete"; // NOI18N
        }
        
        if ((redeliveryStr != null) && (redeliveryStr.length() == 0)) {
            redeliveryStr = null;
        }
        return redeliveryStr;
    }

    void setRedelivery(String redeliveryStr) {
        if (redeliveryStr != null) {
            try {
                RedeliveryHandling parser = RedeliveryHandling.parse(redeliveryStr);
                StringBuilder sb = new StringBuilder();
                for (RedeliveryHandling.Action action : parser.getActions()) {
                    if (action.actionType == RedeliveryHandling.ActionType.DELAY) {
                        sb.append(action.timesSeen + ":" + action.delayTimeInMillis + ";"); // NOI18N
                    } else if (action.actionType == RedeliveryHandling.ActionType.DELETE) {
                        actionDeleteRadioButton.setSelected(true);
                        moveDeleteAfterSpinner.setValue(action.timesSeen);
                    } else {
                        actionMoveRadioButton.setSelected(true);
                        moveDeleteAfterSpinner.setValue(action.timesSeen);
                        if (action.moveType == RedeliveryHandling.MoveType.QUEUE) {
                            moveToQueueRadioButton.setSelected(true);
                            moveToDestinationTextField.setText(action.moveDestinationName);
                        } else if (action.moveType == RedeliveryHandling.MoveType.TOPIC) {
                            moveToTopicRadioButton.setSelected(true);
                            moveToDestinationTextField.setText(action.moveDestinationName);
                        } else if (action.moveType == RedeliveryHandling.MoveType.SAME) {
                            moveToSameRadioButton.setSelected(true);
                            moveToDestinationTextField.setText(action.moveDestinationName);
                        }
                    }
                }
                delayTextField.setText(sb.toString());
            } catch (Exception e) {

            }
        }             
    }

    /**
     * Return the batch size value
     * @return long batch size
     */
    long getBatchSize() {
        if (enableBatchBox.isSelected()) {
            if (batchSizeSpinner.getValue() != null) {
                return new Long(batchSizeSpinner.getValue().toString()).longValue();
            }
        }
        return -1;        
    }

    String getMaxConcurrentConsumer() {
        if (maxConcurrentSpinner.getText() != null) {
            return maxConcurrentSpinner.getText();
        } else {
            return "1";
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
    
    public void setProject(Project project) {
        mProject = project;
    }
    
    /**
     * Set the operation name to be configured
     * @param opName
     */
    public void setOperationName(String opName) {
        if (opName != null) {
            operationNameComboBox.setSelectedItem(opName);
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
        redeliveryHandlingPanel = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        delayLabel = new javax.swing.JLabel();
        moveDeleteAfterLabel = new javax.swing.JLabel();
        actionLabel = new javax.swing.JLabel();
        moveToQueueTopicLabel = new javax.swing.JLabel();
        moveToDestinationLabel = new javax.swing.JLabel();
        moveToDestinationTextField = new javax.swing.JTextField();
        delayTextField = new javax.swing.JTextField();
        moveToDestButton = new javax.swing.JButton();
        jpnlTermination = new javax.swing.JPanel();
        actionNoneRadioButton = new javax.swing.JRadioButton();
        actionMoveRadioButton = new javax.swing.JRadioButton();
        actionDeleteRadioButton = new javax.swing.JRadioButton();
        jpnlMoveDeleteAfter = new javax.swing.JPanel();
        moveDeleteAfterSpinner = new javax.swing.JSpinner();
        timesLabel = new javax.swing.JLabel();
        jpnlMoveQueueTopic = new javax.swing.JPanel();
        moveToQueueRadioButton = new javax.swing.JRadioButton();
        moveToTopicRadioButton = new javax.swing.JRadioButton();
        moveToSameRadioButton = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        descriptionPanel = new javax.swing.JPanel();
        terminationBtnGrp = new javax.swing.ButtonGroup();
        inputMessagesPanelTab = new javax.swing.JPanel();
        consumerPanel = new javax.swing.JPanel();
        consumerLab = new javax.swing.JLabel();
        jSeparator7 = new javax.swing.JSeparator();
        jLabel30 = new javax.swing.JLabel();
        concurrencyLab = new javax.swing.JLabel();
        msgSelectorTextField = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        batchSizeSpinner = new javax.swing.JSpinner();
        //maxConcurrentSpinner = new javax.swing.JSpinner();
        maxConcurrentSpinner = new javax.swing.JTextField();
        synchRBtn = new javax.swing.JRadioButton();
        ccRBtn1 = new javax.swing.JRadioButton();
        enableBatchBox = new javax.swing.JCheckBox();
        redeliveryBtn = new javax.swing.JButton();
        redeliveryTextField = new javax.swing.JTextField();
        sizeLab = new javax.swing.JLabel();

        jTextField1.setText(org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.jTextField1.text_1")); // NOI18N
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

        org.openide.awt.Mnemonics.setLocalizedText(jLabel26, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.jLabel26.text")); // NOI18N
        jLabel26.setName("jLabel26"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(portTypeLabel, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.portTypeLabel.text")); // NOI18N
        portTypeLabel.setName("portTypeLabel"); // NOI18N

        operationNameComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        operationNameComboBox.setName("operationNameComboBox"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel42, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.jLabel42.text")); // NOI18N
        jLabel42.setName("jLabel42"); // NOI18N

        bindingNameComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        bindingNameComboBox.setName("bindingNameComboBox"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(operationNameLabel, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.operationNameLabel.text_1")); // NOI18N
        operationNameLabel.setName("operationNameLabel"); // NOI18N

        servicePortComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        servicePortComboBox.setName("servicePortComboBox"); // NOI18N
        servicePortComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                servicePortComboBoxItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(bindingNameLabel, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.bindingNameLabel.text")); // NOI18N
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

        redeliveryHandlingPanel.setToolTipText(org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.redeliveryHandlingPanel.toolTipText")); // NOI18N
        redeliveryHandlingPanel.setMinimumSize(new java.awt.Dimension(446, 200));
        redeliveryHandlingPanel.setName("redeliveryHandlingPanel"); // NOI18N
        redeliveryHandlingPanel.setPreferredSize(new java.awt.Dimension(462, 350));
        redeliveryHandlingPanel.setLayout(new java.awt.BorderLayout());

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new java.awt.GridBagLayout());

        delayLabel.setLabelFor(delayTextField);
        org.openide.awt.Mnemonics.setLocalizedText(delayLabel, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.delayLabel.text")); // NOI18N
        delayLabel.setToolTipText(org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.delayLabel.toolTipText")); // NOI18N
        delayLabel.setName("delayLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 5, 5);
        jPanel2.add(delayLabel, gridBagConstraints);

        moveDeleteAfterLabel.setLabelFor(moveDeleteAfterSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(moveDeleteAfterLabel, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.moveDeleteAfterLabel.text")); // NOI18N
        moveDeleteAfterLabel.setName("moveDeleteAfterLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 5, 5);
        jPanel2.add(moveDeleteAfterLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(actionLabel, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.actionLabel.text")); // NOI18N
        actionLabel.setName("actionLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 5, 5);
        jPanel2.add(actionLabel, gridBagConstraints);

        moveToQueueTopicLabel.setDisplayedMnemonic('t');
        org.openide.awt.Mnemonics.setLocalizedText(moveToQueueTopicLabel, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.moveToQueueTopicLabel.text")); // NOI18N
        moveToQueueTopicLabel.setName("moveToQueueTopicLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 5, 5);
        jPanel2.add(moveToQueueTopicLabel, gridBagConstraints);

        moveToDestinationLabel.setDisplayedMnemonic('s');
        moveToDestinationLabel.setLabelFor(moveToDestinationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(moveToDestinationLabel, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.moveToDestinationLabel.text")); // NOI18N
        moveToDestinationLabel.setName("moveToDestinationLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 5, 5);
        jPanel2.add(moveToDestinationLabel, gridBagConstraints);

        moveToDestinationTextField.setText(org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.moveToDestinationTextField.text")); // NOI18N
        moveToDestinationTextField.setToolTipText(org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.moveToDestinationTextField.toolTipText")); // NOI18N
        moveToDestinationTextField.setMinimumSize(new java.awt.Dimension(54, 19));
        moveToDestinationTextField.setName("moveToDestinationTextField"); // NOI18N
        moveToDestinationTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                moveToDestinationTextFieldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(moveToDestinationTextField, gridBagConstraints);

        delayTextField.setText(org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.delayTextField.text")); // NOI18N
        delayTextField.setToolTipText(org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.delayTextField.toolTipText")); // NOI18N
        delayTextField.setMinimumSize(new java.awt.Dimension(50, 19));
        delayTextField.setName("delayTextField"); // NOI18N
        delayTextField.setPreferredSize(new java.awt.Dimension(100, 19));
        delayTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                delayTextFieldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(delayTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(moveToDestButton, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.moveToDestButton.text")); // NOI18N
        moveToDestButton.setToolTipText(org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.moveToDestButton.toolTipText")); // NOI18N
        moveToDestButton.setMinimumSize(new java.awt.Dimension(23, 23));
        moveToDestButton.setName("moveToDestButton"); // NOI18N
        moveToDestButton.setPreferredSize(new java.awt.Dimension(23, 23));
        moveToDestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveToDestButtonmoveToDestActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 5, 5);
        jPanel2.add(moveToDestButton, gridBagConstraints);

        jpnlTermination.setName("jpnlTermination"); // NOI18N
        jpnlTermination.setLayout(new java.awt.GridBagLayout());

        terminationBtnGrp.add(actionNoneRadioButton);
        actionNoneRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(actionNoneRadioButton, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.actionNoneRadioButton.text")); // NOI18N
        actionNoneRadioButton.setName("actionNoneRadioButton"); // NOI18N
        actionNoneRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionNoneRadioButtonActionPerformed(evt);
            }
        });
        actionNoneRadioButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                actionNoneRadioButtonFocusGained(evt);
            }
        });
        actionNoneRadioButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                actionNoneRadioButtonredeliveryActionChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jpnlTermination.add(actionNoneRadioButton, gridBagConstraints);

        terminationBtnGrp.add(actionMoveRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(actionMoveRadioButton, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.actionMoveRadioButton.text")); // NOI18N
        actionMoveRadioButton.setName("actionMoveRadioButton"); // NOI18N
        actionMoveRadioButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                actionMoveRadioButtonFocusGained(evt);
            }
        });
        actionMoveRadioButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                actionMoveRadioButtonredeliveryActionChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jpnlTermination.add(actionMoveRadioButton, gridBagConstraints);

        terminationBtnGrp.add(actionDeleteRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(actionDeleteRadioButton, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.actionDeleteRadioButton.text")); // NOI18N
        actionDeleteRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.actionDeleteRadioButton.toolTipText")); // NOI18N
        actionDeleteRadioButton.setName("actionDeleteRadioButton"); // NOI18N
        actionDeleteRadioButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                actionDeleteRadioButtonFocusGained(evt);
            }
        });
        actionDeleteRadioButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                actionDeleteRadioButtonredeliveryActionChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jpnlTermination.add(actionDeleteRadioButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jpnlTermination, gridBagConstraints);

        jpnlMoveDeleteAfter.setName("jpnlMoveDeleteAfter"); // NOI18N
        jpnlMoveDeleteAfter.setLayout(new java.awt.GridBagLayout());

        moveDeleteAfterSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(0), null, Integer.valueOf(1)));
        moveDeleteAfterSpinner.setMinimumSize(new java.awt.Dimension(25, 20));
        moveDeleteAfterSpinner.setName("moveDeleteAfterSpinner"); // NOI18N
        moveDeleteAfterSpinner.setPreferredSize(new java.awt.Dimension(50, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jpnlMoveDeleteAfter.add(moveDeleteAfterSpinner, gridBagConstraints);
        JTextField moveDeleteTF = ((JSpinner.DefaultEditor)moveDeleteAfterSpinner.getEditor()).getTextField();
        moveDeleteTF.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                updateDescriptionArea(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(timesLabel, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.timesLabel.text")); // NOI18N
        timesLabel.setName("timesLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jpnlMoveDeleteAfter.add(timesLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jpnlMoveDeleteAfter, gridBagConstraints);

        jpnlMoveQueueTopic.setName("jpnlMoveQueueTopic"); // NOI18N
        jpnlMoveQueueTopic.setLayout(new java.awt.GridBagLayout());

        QTopicBGroup.add(moveToQueueRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(moveToQueueRadioButton, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.moveToQueueRadioButton.text")); // NOI18N
        moveToQueueRadioButton.setToolTipText(mBundle.getString("DESC_Attribute_moveSample"));
        moveToQueueRadioButton.setName("moveToQueueRadioButton"); // NOI18N
        moveToQueueRadioButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                moveToQueueRadioButtonFocusGained(evt);
            }
        });
        moveToQueueRadioButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                moveToQueueRadioButtonmoveActionChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jpnlMoveQueueTopic.add(moveToQueueRadioButton, gridBagConstraints);

        QTopicBGroup.add(moveToTopicRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(moveToTopicRadioButton, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.moveToTopicRadioButton.text")); // NOI18N
        moveToTopicRadioButton.setToolTipText(mBundle.getString("DESC_Attribute_moveSample"));
        moveToTopicRadioButton.setName("moveToTopicRadioButton"); // NOI18N
        moveToTopicRadioButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                moveToTopicRadioButtonFocusGained(evt);
            }
        });
        moveToTopicRadioButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                moveToTopicRadioButtonmoveActionChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jpnlMoveQueueTopic.add(moveToTopicRadioButton, gridBagConstraints);

        QTopicBGroup.add(moveToSameRadioButton);
        moveToSameRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(moveToSameRadioButton, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.moveToSameRadioButton.text")); // NOI18N
        moveToSameRadioButton.setToolTipText(mBundle.getString("DESC_Attribute_moveSample"));
        moveToSameRadioButton.setName("moveToSameRadioButton"); // NOI18N
        moveToSameRadioButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                moveToSameRadioButtonFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jpnlMoveQueueTopic.add(moveToSameRadioButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jpnlMoveQueueTopic, gridBagConstraints);

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
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weighty = 0.5;
        jPanel2.add(jPanel1, gridBagConstraints);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        jPanel2.add(jLabel1, gridBagConstraints);

        jSeparator1.setName("jSeparator1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 75, 0, 10);
        jPanel2.add(jSeparator1, gridBagConstraints);

        jSplitPane1.setTopComponent(jPanel2);

        descriptionPanel.setMinimumSize(new java.awt.Dimension(400, 50));
        descriptionPanel.setName("descriptionPanel"); // NOI18N
        descriptionPanel.setPreferredSize(new java.awt.Dimension(400, 75));
        descriptionPanel.setLayout(new java.awt.BorderLayout());
        descPanel = new DescriptionPanel();
        descriptionPanel.add(descPanel, java.awt.BorderLayout.CENTER);
        jSplitPane1.setBottomComponent(descriptionPanel);

        redeliveryHandlingPanel.add(jSplitPane1, java.awt.BorderLayout.PAGE_START);

        setName("Form"); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        inputMessagesPanelTab.setName("inputMessagesPanelTab"); // NOI18N
        inputMessagesPanelTab.setLayout(new java.awt.GridBagLayout());

        consumerPanel.setName("consumerPanel"); // NOI18N
        consumerPanel.setLayout(new java.awt.GridBagLayout());

        consumerLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(consumerLab, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.consumerLab.text")); // NOI18N
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

        org.openide.awt.Mnemonics.setLocalizedText(jLabel30, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.jLabel30.text")); // NOI18N
        jLabel30.setName("jLabel30"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        consumerPanel.add(jLabel30, gridBagConstraints);

        concurrencyLab.setLabelFor(maxConcurrentSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(concurrencyLab, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.concurrencyLab.text")); // NOI18N
        concurrencyLab.setName("concurrencyLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        consumerPanel.add(concurrencyLab, gridBagConstraints);

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
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 100;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        consumerPanel.add(msgSelectorTextField, gridBagConstraints);

        jLabel28.setLabelFor(msgSelectorTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel28, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.jLabel28.text")); // NOI18N
        jLabel28.setName("jLabel28"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        consumerPanel.add(jLabel28, gridBagConstraints);
        jLabel28.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "InboundMessageConsumerPanel.jLabel28.AccessibleContext.accessibleName")); // NOI18N

        jLabel35.setLabelFor(redeliveryTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel35, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.jLabel35.text")); // NOI18N
        jLabel35.setName("jLabel35"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        consumerPanel.add(jLabel35, gridBagConstraints);

        batchSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        batchSizeSpinner.setToolTipText(mBundle.getString("DESC_Attribute_batchSize"));
        batchSizeSpinner.setName("batchSizeSpinner"); // NOI18N
        batchSizeSpinner.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                batchSizeSpinnerFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 140, 0, 0);
        consumerPanel.add(batchSizeSpinner, gridBagConstraints);
        JTextField batchSizeTF = ((JSpinner.DefaultEditor)batchSizeSpinner.getEditor()).getTextField();
        batchSizeTF.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                updateDescriptionArea(evt);
            }
        });

        //maxConcurrentSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        maxConcurrentSpinner.setToolTipText(mBundle.getString("DESC_Attribute_maxConcurrentConsumers"));
        maxConcurrentSpinner.setName("maxConcurrentSpinner"); // NOI18N
        maxConcurrentSpinner.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                maxConcurrentSpinnerFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
            	maxConcurrentSpinnerFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        consumerPanel.add(maxConcurrentSpinner, gridBagConstraints);
        JTextField concurrencyTF = maxConcurrentSpinner;
        concurrencyTF.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                updateDescriptionArea(evt);
            }
        });

        deliveryModeBtnGrp.add(synchRBtn);
        org.openide.awt.Mnemonics.setLocalizedText(synchRBtn, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.synchRBtn.text")); // NOI18N
        synchRBtn.setToolTipText(mBundle.getString("DESC_Attribute_concurrencyMode"));
        synchRBtn.setMargin(new java.awt.Insets(2, 0, 2, 2));
        synchRBtn.setName("synchRBtn"); // NOI18N
        synchRBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                synchRBtnFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        consumerPanel.add(synchRBtn, gridBagConstraints);

        deliveryModeBtnGrp.add(ccRBtn1);
        ccRBtn1.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(ccRBtn1, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.ccRBtn1.text")); // NOI18N
        ccRBtn1.setToolTipText(mBundle.getString("DESC_Attribute_concurrencyMode"));
        ccRBtn1.setName("ccRBtn1"); // NOI18N
        ccRBtn1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                ccRBtn1FocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        consumerPanel.add(ccRBtn1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(enableBatchBox, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.enableBatchBox.text")); // NOI18N
        enableBatchBox.setToolTipText(mBundle.getString("DESC_Attribute_batchSize"));
        enableBatchBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        enableBatchBox.setName("enableBatchBox"); // NOI18N
        enableBatchBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                enableBatchBoxFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        consumerPanel.add(enableBatchBox, gridBagConstraints);
        enableBatchBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "InboundMessageConsumerPanel.enableBatchBox.AccessibleContext.accessibleName")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(redeliveryBtn, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.redeliveryBtn.text")); // NOI18N
        redeliveryBtn.setToolTipText(mBundle.getString("DESC_Attribute_redeliveryHandling"));
        redeliveryBtn.setName("redeliveryBtn"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        consumerPanel.add(redeliveryBtn, gridBagConstraints);

        redeliveryTextField.setText(org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.redeliveryTextField.text")); // NOI18N
        redeliveryTextField.setToolTipText(mBundle.getString("DESC_Attribute_redeliveryHandling"));
        redeliveryTextField.setName("redeliveryTextField"); // NOI18N
        redeliveryTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                redeliveryTextFieldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        consumerPanel.add(redeliveryTextField, gridBagConstraints);
        redeliveryTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.redeliveryTextField.AccessibleContext.accessibleName")); // NOI18N

        sizeLab.setLabelFor(batchSizeSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(sizeLab, org.openide.util.NbBundle.getMessage(JMSConsumerPanel.class, "JMSConsumerPanel.sizeLab.text")); // NOI18N
        sizeLab.setName("sizeLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 115, 0, 0);
        consumerPanel.add(sizeLab, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        inputMessagesPanelTab.add(consumerPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        add(inputMessagesPanelTab, gridBagConstraints);
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
                        //updateServiceView(jmsAddresses.next());
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
        JMSAddress jmsAddress = null;
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
                    Collection<JMSAddress> address = ((Port) mComponent).
                            getExtensibilityElements(JMSAddress.class);
                    if (!address.isEmpty()) {
                        jmsAddress = address.iterator().next();
                    }                    
                }
            }
            if (binding != null) {
                JMSMessage inputMessage = getInputJMSMessage(binding,
                        selectedOperation);
                updateInputMessageView(inputMessage, jmsAddress, binding);
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

private void moveToDestButtonmoveToDestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveToDestButtonmoveToDestActionPerformed
    if (mProject != null) {
        String jndiStr = JndiBrowser.popupJndiBrowserDialog(mProject, 
                JndiBrowser.Category.ADMIN_OBJECT);
        if (jndiStr != null) {
            moveToDestinationTextField.setText("lookup://" + jndiStr); // NOI18N
        }           
    } else {
        NotifyDescriptor d = new NotifyDescriptor.Message(
                NbBundle.getMessage(JMSConsumerPanel.class, 
                "JMSConsumerPanel.PROJECT_NULL"));                
        DialogDisplayer.getDefault().notify(d);
    }

}//GEN-LAST:event_moveToDestButtonmoveToDestActionPerformed

private void actionNoneRadioButtonredeliveryActionChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_actionNoneRadioButtonredeliveryActionChanged
    updateModeRedeliveryDetailsSection();
}//GEN-LAST:event_actionNoneRadioButtonredeliveryActionChanged

private void actionMoveRadioButtonredeliveryActionChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_actionMoveRadioButtonredeliveryActionChanged
    updateModeRedeliveryDetailsSection();
}//GEN-LAST:event_actionMoveRadioButtonredeliveryActionChanged

private void actionDeleteRadioButtonredeliveryActionChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_actionDeleteRadioButtonredeliveryActionChanged
    updateModeRedeliveryDetailsSection();
}//GEN-LAST:event_actionDeleteRadioButtonredeliveryActionChanged

private void moveToQueueRadioButtonmoveActionChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_moveToQueueRadioButtonmoveActionChanged
    if (actionMoveRadioButton.isSelected()) {
        moveToDestinationLabel.setEnabled(true);
        moveToDestinationTextField.setEnabled(true);
        moveToDestButton.setEnabled(true);
    } else {
        moveToDestinationLabel.setEnabled(false);
        moveToDestinationTextField.setEnabled(false);
        moveToDestButton.setEnabled(false);
    }
}//GEN-LAST:event_moveToQueueRadioButtonmoveActionChanged

private void moveToTopicRadioButtonmoveActionChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_moveToTopicRadioButtonmoveActionChanged
    if (actionMoveRadioButton.isSelected()) {
        moveToDestinationLabel.setEnabled(true);
        moveToDestinationTextField.setEnabled(true);
        moveToDestButton.setEnabled(true);
    } else {
        moveToDestinationLabel.setEnabled(false);
        moveToDestinationTextField.setEnabled(false);
        moveToDestButton.setEnabled(false);
    }
}//GEN-LAST:event_moveToTopicRadioButtonmoveActionChanged

private void ccRBtn1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ccRBtn1FocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_ccRBtn1FocusGained

private void synchRBtnFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_synchRBtnFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_synchRBtnFocusGained

private void maxConcurrentSpinnerFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_maxConcurrentSpinnerFocusGained
// TODO add your handling code here:
	initialMaxConcurrency = maxConcurrentSpinner.getText(); 
    updateDescriptionArea(evt);
}//GEN-LAST:event_maxConcurrentSpinnerFocusGained

private void maxConcurrentSpinnerFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_maxConcurrentSpinnerFocusLost
	if(!isValidConcurrency(maxConcurrentSpinner.getText())) {
		mLogger.log(Level.WARNING,maxConcurrentSpinner.getText()+" Is not a Valid MaxConcurrency, Reseting to "+initialMaxConcurrency );
		maxConcurrentSpinner.setText(initialMaxConcurrency);
		maxConcurrentSpinner.grabFocus();
	}
}//GEN-LAST:event_maxConcurrentSpinnerFocusGained

private void msgSelectorTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_msgSelectorTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_msgSelectorTextFieldFocusGained

private void enableBatchBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_enableBatchBoxFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_enableBatchBoxFocusGained

private void batchSizeSpinnerFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_batchSizeSpinnerFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_batchSizeSpinnerFocusGained

private void redeliveryTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_redeliveryTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_redeliveryTextFieldFocusGained

private void delayTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_delayTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_delayTextFieldFocusGained

private void actionNoneRadioButtonFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_actionNoneRadioButtonFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_actionNoneRadioButtonFocusGained

private void actionMoveRadioButtonFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_actionMoveRadioButtonFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_actionMoveRadioButtonFocusGained

private void actionDeleteRadioButtonFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_actionDeleteRadioButtonFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_actionDeleteRadioButtonFocusGained

private void moveToQueueRadioButtonFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_moveToQueueRadioButtonFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);    
}//GEN-LAST:event_moveToQueueRadioButtonFocusGained

private void moveToTopicRadioButtonFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_moveToTopicRadioButtonFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);    
}//GEN-LAST:event_moveToTopicRadioButtonFocusGained

private void moveToDestinationTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_moveToDestinationTextFieldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_moveToDestinationTextFieldFocusGained

private void actionNoneRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionNoneRadioButtonActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_actionNoneRadioButtonActionPerformed

private void moveToSameRadioButtonFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_moveToSameRadioButtonFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt); 
}//GEN-LAST:event_moveToSameRadioButtonFocusGained

private boolean isValidConcurrency(String concurrency) {
	
    if (concurrency != null && !concurrency.equals("")) {
    	if (concurrency.matches("\\d{1,5}")){ // an integer of length 1 to 5
        	return true;
        }
        if (concurrency.startsWith("${")) {
            if (concurrency.endsWith("}")) { // A Token
            	return true;
            } else {
            	return false;
            }
        }
    }
	return false;
}


    private void resetView() {
        enableBatchBox.removeItemListener(mItemListener);
        operationNameComboBox.removeItemListener(mItemListener);
        redeliveryBtn.removeActionListener(mActionListener);
//        delayTextField.getDocument().removeDocumentListener(mDocumentListener);        
        redeliveryTextField.getDocument().removeDocumentListener(mDocumentListener);   
        servicePortComboBox.setEnabled(false);
        servicePortComboBox.removeAllItems();
        bindingNameComboBox.removeAllItems();
        redeliveryTextField.setText("");
        enableBatchBox.setSelected(false);
        ccRBtn1.setSelected(true);
//        transactionComboBox.removeAllItems();
//        inputTypeComboBox.removeAllItems();
//        outputTypeComboBox.removeAllItems();
//        inputTextComboBox.removeAllItems();
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
//        inputTypeComboBox.addItem(JMSConstants.MAP_MESSAGE);
//        inputTypeComboBox.addItem(JMSConstants.MESSAGE_MESSAGE);
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
        
        if (mDocumentListener == null) {
            mDocumentListener = new MyDocumentListener();
        }
        
        enableBatchBox.addItemListener(mItemListener);
        redeliveryBtn.addActionListener(mActionListener);
        operationNameComboBox.addItemListener(mItemListener);
//        delayTextField.getDocument().addDocumentListener(mDocumentListener);        
        redeliveryTextField.getDocument().addDocumentListener(mDocumentListener);  
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

    private void setAccessibility() {     
        msgSelectorTextField.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_messageSelector")); // NOI18N
        msgSelectorTextField.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_messageSelector")); // NOI18N
        redeliveryTextField.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_redeliveryHandling")); // NOI18N
        redeliveryTextField.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_redeliveryHandling")); // NOI18N       
        (maxConcurrentSpinner).getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_maxConcurrentConsumers")); // NOI18N
        (maxConcurrentSpinner).getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_maxConcurrentConsumers")); // NOI18N
        concurrencyLab.setLabelFor((maxConcurrentSpinner));
        ((JSpinner.DefaultEditor)batchSizeSpinner.getEditor()).getTextField().getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_batchSize")); // NOI18N
        ((JSpinner.DefaultEditor)batchSizeSpinner.getEditor()).getTextField().getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_batchSize")); // NOI18N       
        sizeLab.setLabelFor(((JSpinner.DefaultEditor)batchSizeSpinner.getEditor()).getTextField());
        redeliveryBtn.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_redeliveryHandling")); // NOI18N       
    }
    
    private void populateJMSAddress(JMSAddress jmsAddress) {
//        updateServiceView(jmsAddress);
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

//        updateServiceView(jmsAddress);
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

            populatePartBoxes(binding, bindingOperations);
            // select the 1st item since this is not a configurable param
            operationNameComboBox.setSelectedIndex(0);
            if (operationNameComboBox.getItemCount() > 1) {
                // need to implicitly call update on messages because above
                // listener will not change selection if only 1 item
                if (binding != null) {
                    JMSMessage inputMessage = getInputJMSMessage(binding,
                            operationNameComboBox.getSelectedItem().toString());
                    updateInputMessageView(inputMessage, null, null);
                }
            }
            updateGeneralView(jmsAddress, bindingOperations);   
        }
    }

    private void updateInputMessageView(JMSMessage inputJMSMessage,
            JMSAddress jmsAddress, Binding binding) {
        if (inputJMSMessage != null) {
            // TODO
//            inputTypeComboBox.setSelectedItem(inputJMSMessage.getMessageType());
//            inputTextComboBox.setSelectedItem(inputJMSMessage.getTextPart());
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
        if (binding != null) {
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            updateGeneralView(jmsAddress, bindingOperations);
        }
    }

    private void updateModeRedeliveryDetailsSection() {
       if (actionNoneRadioButton.isSelected()) {
            moveDeleteAfterLabel.setEnabled(false);
            moveDeleteAfterSpinner.setEnabled(false);
            timesLabel.setEnabled(false);
            moveToQueueTopicLabel.setEnabled(false);
            moveToQueueRadioButton.setEnabled(false);
            moveToTopicRadioButton.setEnabled(false);
            moveToSameRadioButton.setEnabled(false);
            moveToDestinationLabel.setEnabled(false);
            moveToDestinationTextField.setEnabled(false);
        } else if (actionMoveRadioButton.isSelected()) {
            moveDeleteAfterLabel.setEnabled(true);
            moveDeleteAfterSpinner.setEnabled(true);
            timesLabel.setEnabled(true);
            moveToQueueTopicLabel.setEnabled(true);
            moveToQueueRadioButton.setEnabled(true);
            moveToTopicRadioButton.setEnabled(true);
            moveToSameRadioButton.setEnabled(true);
            moveToDestinationLabel.setEnabled(true);
            moveToDestinationTextField.setEnabled(true);
        } else {
            moveDeleteAfterLabel.setEnabled(true);
            moveDeleteAfterSpinner.setEnabled(true);
            timesLabel.setEnabled(true);
            moveToQueueTopicLabel.setEnabled(false);
            moveToQueueRadioButton.setEnabled(false);
            moveToTopicRadioButton.setEnabled(false);
            moveToSameRadioButton.setEnabled(false);
            moveToDestinationLabel.setEnabled(false);
            moveToDestinationTextField.setEnabled(false);
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
            String selOperation = null;
            if (operationNameComboBox.getSelectedItem() != null) {
                selOperation = operationNameComboBox.getSelectedItem().toString();
            }
        
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(selOperation)) {
                    List<JMSOperation> jmsOpsList = bop.getExtensibilityElements(JMSOperation.class);
                    Iterator<JMSOperation> jmsOps =
                            jmsOpsList.iterator();
                    // there should only be one jms:operation for the binding op
                    if (jmsOpsList.size() > 0) {
                        JMSOperation jmsOp = jmsOps.next();
                        if (jmsOp != null) {

                            if (jmsOp.getConcurrencyMode() != null) {
                                if (jmsOp.getConcurrencyMode().equals(JMSConstants.CC)) {
                                    ccRBtn1.setSelected(true);
                                }
                            }
                            msgSelectorTextField.setText(jmsOp.getMessageSelector());
                            concurModeComboBox.setSelectedItem(jmsOp.getConcurrencyMode());
                            setRedelivery(jmsOp.getRedeliveryHandling());

                            if (jmsOp.getBatchSize() > 0) {
                                batchSizeSpinner.setEnabled(true);
                                enableBatchBox.setSelected(true);
                            } else {
                                batchSizeSpinner.setEnabled(false);
                                enableBatchBox.setSelected(false);
                            }
                            batchSizeSpinner.setValue(jmsOp.getBatchSize());
                            if (jmsOp.getMaxConcurrentConsumers() != null) {
                                	maxConcurrentSpinner.setText(jmsOp.getMaxConcurrentConsumers());
                            } else {
                                maxConcurrentSpinner.setText("1");
                            }

                            redeliveryTextField.setText(jmsOp.getRedeliveryHandling());
                        }
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

//                // get the output text part
//                outputTextParts = getOutputParts(binding, bop.getName());

                // populate text part
                populateInputPartComboBox(inputTextParts);
//
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
                    Collection parts = msgOut.getParts();
                    Iterator partIter = parts.iterator();
                    while (partIter.hasNext()) {
                        Part part = (Part) partIter.next();
                        outputParts.add(part.getName());
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

    private FileError validateDelayValue(boolean fireEvent, String delayVal) {
        FileError fileError = new FileError();
        boolean valid = true;
        try {
            RedeliveryHandlingParser delayParser = new RedeliveryHandlingParser();
            String  strToValidate = delayTextField.getText();
            if (delayVal != null) {
                strToValidate = delayVal;
            }
            if (!delayParser.checkValid(strToValidate)) {    
                valid = false;
            }
        } catch (Exception utilEx) {
            mLogger.log(Level.FINER, utilEx.getMessage());
        }     
        if (!valid) {
            fileError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.
                            PROPERTY_ERROR_EVT);
            fileError.setErrorMessage(NbBundle.getMessage(JMSConsumerPanel.class, 
                    "JMSConsumerPanel.invalidDelayValue"));            
        }
        if (mDetailsDlgDesc != null) {
            mDetailsDlgDesc.setValid(valid);  
        }
        
        if (fireEvent) {
            ErrorPropagator.doFirePropertyChange(fileError.getErrorMode(), null,
                    fileError.getErrorMessage(), this);
        }          
        return fileError;
    }
    
    public FileError validateMe() {
        return validateMe(false);
    }
    
    public FileError validateMe(boolean fireEvent) {  
        // validate delay value
        FileError fileError = validateDelayValue(fireEvent, redeliveryTextField.getText());
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

    private void validateTextPart() {
        if (getInputMessageText() == null) {
             firePropertyChange(
                    ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_ERROR_EVT, null,
                    NbBundle.getMessage(JMSConsumerPanel.class,
                    "JMSBindingConfiguratonnPanel.INPUT_TEXT_EMPTY"));
             return;
        }

        firePropertyChange(
                ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_CLEAR_MESSAGES_EVT, null, "");


    }

    private void showRedeliverDetails() {
        if (mDetailsDlg == null) {
            mDetailsDlgDesc = new DialogDescriptor(
                    redeliveryHandlingPanel,
                    NbBundle.getMessage(JMSConnectionPanel.class,
                    "JMSConsumerPanel.RedeliveryOnlyTitle"),
                    true, null);
            mDetailsDlg = DialogDisplayer.getDefault().
                    createDialog(mDetailsDlgDesc);
        }
        mDetailsDlg.setSize(redeliveryHandlingPanel.getPreferredSize());
        updateModeRedeliveryDetailsSection();
        // repopulate visual based on changes to redelivery
        setRedelivery(redeliveryTextField.getText());
        
        // reset error
        mDetailsDlgDesc.setValid(true);
        mDetailsDlg.setVisible(true);
        if (mDetailsDlgDesc.getValue() == DialogDescriptor.OK_OPTION) {
            redeliveryTextField.setText(getRedeliveryValue());
        } else {
            delayTextField.setText(""); //reset so invalid format err is cleared
        }
        // reset error
        mDetailsDlgDesc.setValid(true); 
        ErrorPropagator.doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_CLEAR_MESSAGES_EVT, null, "", this);        
        mDetailsDlg.setVisible(false);
    }

    private void updateDescriptionArea(FocusEvent evt) {
        if (mDescPanel != null) {
            mDescPanel.setText("");
        }
        
        if (descPanel != null) {
            descPanel.setText("");
        }        

        String[] desc = null;
        boolean casaEdited = false;

        if (evt.getSource() == ccRBtn1) {
            desc = new String[]{"Delivery Mode\n\n",
                   ccRBtn1.getToolTipText()}; 
        } else if (evt.getSource() == synchRBtn) {
            desc = new String[]{"Delivery Mode\n\n",
                   synchRBtn.getToolTipText()}; 
        } else if (evt.getSource() == maxConcurrentSpinner) {
            desc = new String[]{"Max Concurrent Consumers\n\n",
                   maxConcurrentSpinner.getToolTipText()}; 
        } else if (evt.getSource() == redeliveryTextField) {
            desc = new String[]{"Redelivery\n\n",
                   redeliveryBtn.getToolTipText()}; 
        } else if (evt.getSource() == msgSelectorTextField) {
            desc = new String[]{"Message Selector\n\n",
                   msgSelectorTextField.getToolTipText()}; 
        } else if (evt.getSource() == enableBatchBox) {
            desc = new String[]{"Enable Batch Size\n\n",
                   enableBatchBox.getToolTipText()}; 
        } else if (evt.getSource() == ((JSpinner.DefaultEditor)batchSizeSpinner.getEditor()).getTextField()) {
            desc = new String[]{"Batch Size\n\n",
                   batchSizeSpinner.getToolTipText()}; 
        } 
        if (desc != null) {
            if (mDescPanel != null) {
                mDescPanel.setText(desc[0], desc[1]);
            }
            return;
        }
        

        if (evt.getSource() == delayTextField) {
            desc = new String[]{"Delay\n\n",
                   delayTextField.getToolTipText()}; 
        } else if (evt.getSource() == actionNoneRadioButton) {
            desc = new String[]{"None\n\n",
                   actionNoneRadioButton.getToolTipText()}; 
        } else if (evt.getSource() == actionDeleteRadioButton) {
            desc = new String[]{"Delete\n\n",
                   actionDeleteRadioButton.getToolTipText()}; 
        } else if (evt.getSource() == actionMoveRadioButton) {
            desc = new String[]{"Move\n\n",
                   actionDeleteRadioButton.getToolTipText()}; 
        } else if (evt.getSource() == ((JSpinner.DefaultEditor)moveDeleteAfterSpinner.getEditor()).getTextField()) {
            desc = new String[]{"Move/Delete After\n\n",
                   moveDeleteAfterSpinner.getToolTipText()}; 
        } else if (evt.getSource() == moveToQueueRadioButton) {
            desc = new String[]{"Queue\n\n",
                   moveToQueueRadioButton.getToolTipText()}; 
        } else if (evt.getSource() == moveToTopicRadioButton) {
            desc = new String[]{"Topic\n\n",
                   moveToTopicRadioButton.getToolTipText()}; 
        } else if (evt.getSource() == moveToSameRadioButton) {
            desc = new String[]{"Same\n\n",
                   moveToSameRadioButton.getToolTipText()}; 
        } else if (evt.getSource() == moveToDestinationTextField) {
            desc = new String[]{"Move to Destination\n\n",
                   moveToDestinationTextField.getToolTipText()}; 
        }
        
        if (desc != null) {
            if (descPanel != null) {
                descPanel.setText(desc[0], desc[1]);
            }
            return;
        }        
    }   
    
    private void handleItemStateChanged(ItemEvent evt) {
        if (evt.getSource() == enableBatchBox) {
            if (enableBatchBox.isSelected()) {
                batchSizeSpinner.setEnabled(true);
            } else {
                batchSizeSpinner.setEnabled(false);
            }
        } else  if (evt.getSource() == operationNameComboBox) {
            operationNameComboBoxItemStateChanged(evt);            
        }

    }

    private void handleActionPerformed(ActionEvent evt) {
        if (evt.getSource() == redeliveryBtn) {
            showRedeliverDetails();
        }
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
    
    public class MyDocumentListener implements DocumentListener {
	// Handle insertions into the text field
        public void insertUpdate(DocumentEvent event) {
            if (redeliveryTextField.isFocusOwner()) {
                validateDelayValue(true, redeliveryTextField.getText());
            } else {
                validateDelayValue(true, null);
            }
        }

	// Handle deletions	from the text field
        public void removeUpdate(DocumentEvent event) {
            if (redeliveryTextField.isFocusOwner()) {
                validateDelayValue(true, redeliveryTextField.getText());
            } else {
                validateDelayValue(true, null);
            }            
        }

	// Handle changes to the text field
        public void changedUpdate(DocumentEvent event) {
            // empty
        }
             
    }    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup QTopicBGroup;
    private javax.swing.JRadioButton actionDeleteRadioButton;
    private javax.swing.JLabel actionLabel;
    private javax.swing.JRadioButton actionMoveRadioButton;
    private javax.swing.JRadioButton actionNoneRadioButton;
    private javax.swing.JSpinner batchSizeSpinner;
    private javax.swing.JComboBox bindingNameComboBox;
    private javax.swing.JLabel bindingNameLabel;
    private javax.swing.JRadioButton ccRBtn1;
    private javax.swing.JComboBox concurModeComboBox;
    private javax.swing.JLabel concurrencyLab;
    private javax.swing.JLabel consumerLab;
    private javax.swing.JPanel consumerPanel;
    private javax.swing.JLabel delayLabel;
    private javax.swing.JTextField delayTextField;
    private javax.swing.ButtonGroup deliveryModeBtnGrp;
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.ButtonGroup durabilityBGrp;
    private javax.swing.JCheckBox enableBatchBox;
    private javax.swing.JComboBox inputCorrelationPartComboBox;
    private javax.swing.ButtonGroup inputEncodingGroup;
    private javax.swing.JPanel inputMessagesPanelTab;
    private javax.swing.JComboBox inputMsgIDPartComboBox;
    private javax.swing.JComboBox inputRedeliveredPartComboBox;
    private javax.swing.JComboBox inputTimestampPartComboBox;
    private javax.swing.JComboBox inputTypePartComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JPanel jpnlMoveDeleteAfter;
    private javax.swing.JPanel jpnlMoveQueueTopic;
    private javax.swing.JPanel jpnlTermination;
    //private javax.swing.JSpinner maxConcurrentSpinner;
    private javax.swing.JTextField maxConcurrentSpinner;
    private String initialMaxConcurrency;
    private javax.swing.JLabel moveDeleteAfterLabel;
    private javax.swing.JSpinner moveDeleteAfterSpinner;
    private javax.swing.JButton moveToDestButton;
    private javax.swing.JLabel moveToDestinationLabel;
    private javax.swing.JTextField moveToDestinationTextField;
    private javax.swing.JRadioButton moveToQueueRadioButton;
    private javax.swing.JLabel moveToQueueTopicLabel;
    private javax.swing.JRadioButton moveToSameRadioButton;
    private javax.swing.JRadioButton moveToTopicRadioButton;
    private javax.swing.JTextField msgSelectorTextField;
    private javax.swing.JComboBox operationNameComboBox;
    private javax.swing.JLabel operationNameLabel;
    private javax.swing.ButtonGroup outputEncodingGroup;
    private javax.swing.JPanel portBindingPanel;
    private javax.swing.JComboBox portTypeComboBox;
    private javax.swing.JLabel portTypeLabel;
    private javax.swing.JButton redeliveryBtn;
    private javax.swing.JPanel redeliveryHandlingPanel;
    private javax.swing.JTextField redeliveryTextField;
    private javax.swing.JComboBox servicePortComboBox;
    private javax.swing.JLabel sizeLab;
    private javax.swing.JRadioButton synchRBtn;
    private javax.swing.ButtonGroup terminationBtnGrp;
    private javax.swing.JLabel timesLabel;
    // End of variables declaration//GEN-END:variables

}
