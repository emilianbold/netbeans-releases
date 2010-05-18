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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.soa.wsdl.bindingsupport.ui.util.BindingComponentUtils;
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
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * JMSBindingConfigurationPanel - Panel that allows configuration of
 * properties specifically for JMS Binding component
 *
 * @author  jalmero
 */
public class MessageTypePanel extends javax.swing.JPanel {

    private WSDLComponent mWsdlComponent;

    /** QName **/
    private QName mQName;

    /** resource bundle for file bc **/
    private ResourceBundle mBundle = ResourceBundle.getBundle(
            "org.netbeans.modules.wsdlextensions.jms.resources.Bundle");

    private static final Logger mLogger = Logger.
            getLogger(MessageTypePanel.class.getName());

    private MyItemListener mItemListener = null;

    private MyActionListener mActionListener = null;
    private MyDocumentListener mDocumentListener = null;


    private DescriptionPanel descPanel = null;
    private DescriptionPanel descPanelArchivePanel = null;
    private DescriptionPanel descPanelTextPanel = null;

    private Dialog mDetailsDlg = null;
    private DialogDescriptor mDetailsDlgDesc = null;
    private MessageTypePanel mInstance = null;

    private Dialog mDetailsJNDIDlg = null;
    private DialogDescriptor mDetailsJNDIDlgDesc = null;    
    private JMSMessage mJMSMessage;
    private String mOpName;
    private Part mPart;
    private GlobalType mType = null;
    private GlobalElement mElement = null;
    
    private Project mProject = null;
    
    private boolean mIsInbound = true;
    private String mErrMessage = "";    

    /** Creates new form JMSBindingConfigurationPanel */
    public MessageTypePanel(QName qName, WSDLComponent component, Part part, 
            JMSMessage jmsMessage, String operationName) {
        this(component, part, jmsMessage, operationName, true);     
    }

    public MessageTypePanel(WSDLComponent component, Part part,
            JMSMessage jmsMessage, String operationName, boolean isInbound) {
        mInstance = this;
        mIsInbound = isInbound;
        initComponents();
        populateView(component, part, jmsMessage, null, operationName);        
        setAccessibility();
    }    

    @Override
    public String getName() {
        return NbBundle.getMessage(MessageTypePanel.class,
                "InboundMessagePanel.StepLabel");
    }    

    String getInputUse() {
        if (isEncodedPayload()) {
            return JMSConstants.ENCODED;
        } else {
            return JMSConstants.LITERAL;
        }      
    }

    String getInputEncodingStyle() {
        return trimTextFieldInput(inputEncodedTypeTfld.getText());
    }

    /**
     * Return message type.  Options are JMSConstants.XML_MESSAGE_TYPE,
     * JMSConstants.TEXT_MESSAGE_TYPE, JMSConstants.ENCODED_MESSAGE_TYPE,
     * JMSConstants.BINARY_MESSAGE_TYPE
     * 
     * @return
     */
    int getMessageType() {
        if (isXMLPayload()) {
            return JMSConstants.XML_MESSAGE_TYPE;
        } else if (isTextPayload()) {
            return JMSConstants.TEXT_MESSAGE_TYPE;
        } else if (isBinaryPayload()) {
            return JMSConstants.BINARY_MESSAGE_TYPE;
        } else {
            return JMSConstants.ENCODED_MESSAGE_TYPE;
        }
    }
    
    public void setProject(Project project) {
        mProject = project;        
    }
    
    GlobalType getSelectedPartType() {
        return mType;
    }
    
    GlobalElement getSelectedElementType() {
        return mElement;
    }

    void setDescriptionPanel(DescriptionPanel panel) {
        descPanel = panel;
    }
    
    /**
     * Return true if payload is to be forwarded as an attachment
     * @return boolean 
     */
    boolean getForwardAsAttachment() {
        return attachmentBox.isSelected();
    }
    
    /**
     * Enable the Processing Payload section accordingly
     * @param enable
     */
    public void enablePayloadProcessing(boolean enable) {
//        payloadProcessingSectionLab.setEnabled(enable);
//        inputMessageTypeSep.setEnabled(enable);

        messageTypeBox.setEnabled(enable);
        inputEncodedTypeTfld.setEnabled(enable);
        inputEncodedDetailsBtn.setEnabled(enable);
        inputEncodedTypeLab.setEnabled(enable);
        inputEncodedXsdTfld.setEnabled(enable);
//        attachmentBox.setEnabled(enable);
    }
        
    private void setAccessibility() {
        messageTypeBox.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_messageType")); // NOI18N
        messageTypeBox.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_messageType")); // NOI18N        
        inputEncodedXsdTfld.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_payloadEncoded")); // NOI18N
        inputEncodedXsdTfld.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_payloadEncoded")); // NOI18N        
        inputEncodedDetailsBtn.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_payloadEncoded")); // NOI18N
        inputEncodedDetailsBtn.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_payloadEncoded")); // NOI18N                
        inputEncodedTypeTfld.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_encodingStyle")); // NOI18N
        inputEncodedTypeTfld.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_encodingStyle")); // NOI18N                
        attachmentBox.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_forwardAsAttachment")); // NOI18N
        attachmentBox.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_forwardAsAttachment")); // NOI18N                
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

        inputEncodingGroup = new javax.swing.ButtonGroup();
        outputEncodingGroup = new javax.swing.ButtonGroup();
        messageTypeBtnGrp = new javax.swing.ButtonGroup();
        messageTypePanel = new javax.swing.JPanel();
        payloadProcessingSectionLab = new javax.swing.JLabel();
        inputMessageTypeSep = new javax.swing.JSeparator();
        inputEncodedTypeTfld = new javax.swing.JTextField();
        inputEncodedDetailsBtn = new javax.swing.JButton();
        inputEncodedTypeLab = new javax.swing.JLabel();
        inputEncodedXsdTfld = new javax.swing.JTextField();
        attachmentBox = new javax.swing.JCheckBox();
        messageTypeLab = new javax.swing.JLabel();
        messageTypeBox = new javax.swing.JComboBox();
        xsdElementTypeLab = new javax.swing.JLabel();

        setName("Form"); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        messageTypePanel.setName("messageTypePanel"); // NOI18N
        messageTypePanel.setLayout(new java.awt.GridBagLayout());

        payloadProcessingSectionLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(payloadProcessingSectionLab, org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "InboundOneWayMessagePanel.messageTypeLab.text_1_1")); // NOI18N
        payloadProcessingSectionLab.setName("payloadProcessingSectionLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        messageTypePanel.add(payloadProcessingSectionLab, gridBagConstraints);

        inputMessageTypeSep.setName("inputMessageTypeSep"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 120, 0, 0);
        messageTypePanel.add(inputMessageTypeSep, gridBagConstraints);

        inputEncodedTypeTfld.setToolTipText(mBundle.getString("DESC_Attribute_encodingStyle"));
        inputEncodedTypeTfld.setName("inputEncodedTypeTfld"); // NOI18N
        inputEncodedTypeTfld.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                inputEncodedTypeTfldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 125, 0, 0);
        messageTypePanel.add(inputEncodedTypeTfld, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(inputEncodedDetailsBtn, org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.inputEncodedDetailsBtn.text")); // NOI18N
        inputEncodedDetailsBtn.setMargin(new java.awt.Insets(2, 5, 2, 5));
        inputEncodedDetailsBtn.setName("inputEncodedDetailsBtn"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        messageTypePanel.add(inputEncodedDetailsBtn, gridBagConstraints);

        inputEncodedTypeLab.setLabelFor(inputEncodedTypeTfld);
        org.openide.awt.Mnemonics.setLocalizedText(inputEncodedTypeLab, org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.inpuTypeLab.text")); // NOI18N
        inputEncodedTypeLab.setName("inputEncodedTypeLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 50, 0, 0);
        messageTypePanel.add(inputEncodedTypeLab, gridBagConstraints);

        inputEncodedXsdTfld.setToolTipText(mBundle.getString("DESC_Attribute_payloadEncoded"));
        inputEncodedXsdTfld.setName("inputEncodedXsdTfld"); // NOI18N
        inputEncodedXsdTfld.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                inputEncodedXsdTfldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 0, 0);
        messageTypePanel.add(inputEncodedXsdTfld, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(attachmentBox, org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.attachmentBox.text")); // NOI18N
        attachmentBox.setToolTipText(mBundle.getString("DESC_Attribute_forwardAsAttachment"));
        attachmentBox.setName("attachmentBox"); // NOI18N
        attachmentBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                attachmentBoxFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        messageTypePanel.add(attachmentBox, gridBagConstraints);

        messageTypeLab.setLabelFor(messageTypeBox);
        org.openide.awt.Mnemonics.setLocalizedText(messageTypeLab, org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.messageTypeLab.text")); // NOI18N
        messageTypeLab.setName("messageTypeLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 0, 0);
        messageTypePanel.add(messageTypeLab, gridBagConstraints);

        messageTypeBox.setToolTipText(mBundle.getString("DESC_Attribute_messageType"));
        messageTypeBox.setName("messageTypeBox"); // NOI18N
        messageTypeBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                messageTypeBoxFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 0, 0);
        messageTypePanel.add(messageTypeBox, gridBagConstraints);

        xsdElementTypeLab.setLabelFor(inputEncodedXsdTfld);
        org.openide.awt.Mnemonics.setLocalizedText(xsdElementTypeLab, org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.xsdElementTypeLab.text")); // NOI18N
        xsdElementTypeLab.setName("xsdElementTypeLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 0, 0);
        messageTypePanel.add(xsdElementTypeLab, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        add(messageTypePanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void inputEncodedTypeTfldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputEncodedTypeTfldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_inputEncodedTypeTfldFocusGained

private void inputEncodedXsdTfldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputEncodedXsdTfldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_inputEncodedXsdTfldFocusGained

private void attachmentBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_attachmentBoxFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_attachmentBoxFocusGained

private void messageTypeBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_messageTypeBoxFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_messageTypeBoxFocusGained


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
      
        messageTypeBox.addItemListener(mItemListener);
        inputEncodedTypeTfld.getDocument().addDocumentListener(mDocumentListener);       
        inputEncodedXsdTfld.getDocument().addDocumentListener(mDocumentListener);        
        inputEncodedDetailsBtn.addActionListener(mActionListener);        
                
    }

    private void resetView() {
        messageTypeBox.removeItemListener(mItemListener);
        inputEncodedDetailsBtn.removeActionListener(mActionListener);        
        inputEncodedTypeTfld.getDocument().removeDocumentListener(mDocumentListener);
        inputEncodedXsdTfld.getDocument().removeDocumentListener(mDocumentListener);        

        inputEncodedXsdTfld.setText("");
        inputEncodedTypeTfld.setText("");
        
        attachmentBox.setSelected(false);
        
        messageTypeBox.removeAllItems();
        
        messageTypeBox.addItem(JMSConstants.TEXT);
        messageTypeBox.addItem(JMSConstants.BINARY);
        messageTypeBox.addItem(JMSConstants.XML);
        messageTypeBox.addItem(JMSConstants.ENCODED_DATA); 
        messageTypeBox.setSelectedIndex(0);
        
        if (mIsInbound) {
            attachmentBox.setVisible(true);            
        } else {            
            attachmentBox.setVisible(false);                
        }        
    }


    /**
     * Populate the view with the given the model component
     * @param component
     * @param part
     * @param fileMessage
     * @param operationName
     */
    public void populateView(WSDLComponent component, Part part,
            JMSMessage jmsMessage, Project project, String operationName) {
        cleanUp();
        mWsdlComponent = component;
        mPart = part;
        mJMSMessage = jmsMessage;
        mProject = project;
        mOpName = operationName;
        if (mOpName == null) {
            mOpName = "";
        }
        resetView();
        populateView();
        initListeners();
    }

    private void populateView() {
        if (mJMSMessage != null) {
            boolean encodedOn = false;
            if (mJMSMessage.getUse() != null) {
                if (mJMSMessage.getUse().equals(JMSConstants.ENCODED)) {
                    messageTypeBox.setSelectedItem(JMSConstants.ENCODED_DATA);
                    encodedOn = true;
                }
            } 
            if (mJMSMessage.getJMSEncodingStyle() != null) {
                inputEncodedTypeTfld.setText(
                        mJMSMessage.getJMSEncodingStyle());
        
             } else {
                inputEncodedTypeTfld.setText("");
             }            
                        
            attachmentBox.setSelected(mJMSMessage.getForwardAsAttachment());

            if (mPart != null) {
                String ptStr = JMSUtilities.getPartTypeOrElementString(mPart);
                if (JMSConstants.XSD_STRING.equals(ptStr) && (!encodedOn)) {
                    messageTypeBox.setSelectedItem(JMSConstants.TEXT);
                } else if (JMSConstants.XSD_BASE64_BINARY.equals(ptStr) && (!encodedOn)) {
                    messageTypeBox.setSelectedItem(JMSConstants.BINARY);
                } else {
                    if (encodedOn) {
                        inputEncodedXsdTfld.setText(ptStr);
                    } else {
                        messageTypeBox.setSelectedItem(JMSConstants.XML);
                        inputEncodedXsdTfld.setText(ptStr);
                    }
                }
            }    
           
            if (mIsInbound) {
                
            } else {
                
            }

        } else {
            // null out view
            inputEncodedTypeTfld.setText("");                    
            inputEncodedXsdTfld.setText("");
        }
        handleMessageTypeRecord();
    }    

    /**
     * Checks if the text payload toggle is turned on
     * @return boolean true if the text payload is selected
     */
    public boolean isTextPayload() {
        if (JMSConstants.TEXT.equals(messageTypeBox.getSelectedItem().toString())) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Checks if the binary payload toggle is turned on
     * @return boolean true if the binary payload is selected
     */
    public boolean isBinaryPayload() {
        if (JMSConstants.BINARY.equals(messageTypeBox.getSelectedItem().toString())) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Checks if the xml payload toggle is turned on
     * @return boolean true if the XML payload is selected
     */
    public boolean isXMLPayload() {
        if (JMSConstants.XML.equals(messageTypeBox.getSelectedItem().toString())) {
            return true;
        } else {
            return false;
        }
    }
       
    /**
     * Checks if the encoded payload is selected
     * @return boolean true if the encoded payload is selected
     */
    public boolean isEncodedPayload() {
        if (JMSConstants.ENCODED_DATA.equals(messageTypeBox.getSelectedItem().toString())) {
            return true;
        } else {
            return false;
        }
    }
    
    private void cleanUp() {
        // clean up listeners TODO
        // null out data TODO
    }    
    
    public FileError validateMe() {
        return validateMe(false);
    }
    
    public FileError validateMe(boolean fireEvent) {
        boolean valid = true;
        FileError fileError = new FileError();
        if ((isXMLPayload()) && (messageTypeBox.isEnabled())) {
            if (inputEncodedXsdTfld.getText().length() == 0) {
                valid = false;
                mErrMessage = NbBundle.getMessage(MessageTypePanel.class, 
                        "MessageTypePanel.XMLElementIncomplete");
                mLogger.finest(mErrMessage);
                fileError.setErrorMessage(mErrMessage);
                fileError.setErrorMode(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_ERROR_EVT);
                if (fireEvent) {
                    ErrorPropagator.doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.
                                PROPERTY_ERROR_EVT, null, mErrMessage, this);                
                }
            }
        } else if (isEncodedPayload()) {
            valid = validateEncodingSection();
            if (!valid) {                
                mErrMessage = NbBundle.getMessage(MessageTypePanel.class, 
                        "MessageTypePanel.EncodedDataIncomplete");  
                fileError.setErrorMessage(mErrMessage);
                fileError.setErrorMode(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_ERROR_EVT);   
                if (fireEvent) {
                    ErrorPropagator.doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.
                                PROPERTY_ERROR_EVT, null, mErrMessage, this);                
                }
            }
        }
        if (valid) {
            fileError.setErrorMessage("");
            fileError.setErrorMode(
                   ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_CLEAR_MESSAGES_EVT);    
            if (fireEvent) {
                ErrorPropagator.doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_CLEAR_MESSAGES_EVT, null, "", this);
            }
        }
        
        return fileError;
    }
        
    private boolean validateEncodingSection() {
        if (isEncodedPayload()) {
            if ((inputEncodedTypeTfld.getText().length() == 0) ||
                    (inputEncodedXsdTfld.getText().length() == 0)) {
                return false;
            }
        }
        return true;
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



    private void handleMessageTypeRecord() {       
        if (isXMLPayload()) {
            inputEncodedXsdTfld.setEnabled(true);
            inputEncodedTypeTfld.setEnabled(false);
            inputEncodedTypeLab.setEnabled(false);
            inputEncodedDetailsBtn.setEnabled(true);
            inputEncodedTypeLab.setEnabled(true);
        } else if (isTextPayload()) {
            inputEncodedXsdTfld.setEnabled(false);
            inputEncodedTypeTfld.setEnabled(false);
            inputEncodedDetailsBtn.setEnabled(false);
            inputEncodedTypeLab.setEnabled(false);
        } else if (isEncodedPayload()) {
            inputEncodedXsdTfld.setEnabled(true);
            inputEncodedTypeTfld.setEnabled(true);
            inputEncodedDetailsBtn.setEnabled(true);
            inputEncodedTypeLab.setEnabled(true);                        
        } else if (isBinaryPayload()) {
            inputEncodedXsdTfld.setEnabled(false);
            inputEncodedTypeTfld.setEnabled(false);
            inputEncodedDetailsBtn.setEnabled(false);
            inputEncodedTypeLab.setEnabled(false);
        } 
        
        // need to make sure both element and encoding style are supplied before
        // allowing user to continue
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
    
    private void updateDescriptionArea(FocusEvent evt) {
        if (descPanel != null) {
            descPanel.setText("");
        }

        String[] desc = null;
        boolean casaEdited = false;

        if (evt.getSource() == inputEncodedTypeTfld) {
            desc = new String[]{"Encoded Style \n\n",
                   inputEncodedTypeTfld.getToolTipText()}; 
        } else if (evt.getSource() == attachmentBox) {
            desc = new String[]{"Forward as Attachment \n\n",
                   attachmentBox.getToolTipText()}; 
        } else if (evt.getSource() == inputEncodedXsdTfld) {
            desc = new String[]{"Encoded Data - XSD element \n\n",
                   inputEncodedXsdTfld.getToolTipText()}; 
        } else if (evt.getSource() == messageTypeBox) {
            desc = new String[]{"Message Type \n\n",
                   messageTypeBox.getToolTipText()}; 
        }
        if (desc != null) {
            if (descPanel != null) {
                descPanel.setText(desc[0], desc[1]);
            }
            return;
        }
    }     
    
    private void showEncodedTypeDialog() {
        WSDLModel wsdlModel = mWsdlComponent.getModel();
        SchemaComponent schemaComponent = null;
        if (mPart != null) {
            schemaComponent = mPart.getElement() == null ? null : mPart.getElement().get();
            if (schemaComponent == null) {
                schemaComponent = mPart.getType() == null ? null : mPart.getType().get();
            }          
        }
            
        if (mProject != null) {
            boolean ok = BindingComponentUtils.browseForElementOrType(mProject,
                    wsdlModel, schemaComponent);
            if (ok) {
                mType = BindingComponentUtils.getElementOrType();
                mElement = BindingComponentUtils.getSchemaComponent();
                String partTypeStr = BindingComponentUtils.getPrefixNameSpace();
                if (isEncodedPayload()) {
                    if (mElement == null) {
                        // encoded must be of element type only
                        NotifyDescriptor d = new NotifyDescriptor.Message(
                                NbBundle.getMessage(MessageTypePanel.class,
                                "MessageTypePanel.invalidType"));
                        DialogDisplayer.getDefault().notify(d);                      
                        return;
                    }
                }
                
                inputEncodedXsdTfld.setText(partTypeStr);
            }         
        } else {
            NotifyDescriptor d = new NotifyDescriptor.Message(
                    NbBundle.getMessage(MessageTypePanel.class,
                    "MessageTypePanel.UnknownProject"));
            DialogDisplayer.getDefault().notify(d);
            return;                    
        }
    }  
    
    private void handleItemStateChanged(ItemEvent evt) {
        if (evt.getSource() == messageTypeBox) {
            handleMessageTypeRecord();
        }
    }

    private void handleActionPerformed(ActionEvent evt) {
        if (evt.getSource() == inputEncodedDetailsBtn) {
            showEncodedTypeDialog();
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
    private javax.swing.JCheckBox attachmentBox;
    private javax.swing.JButton inputEncodedDetailsBtn;
    private javax.swing.JLabel inputEncodedTypeLab;
    private javax.swing.JTextField inputEncodedTypeTfld;
    private javax.swing.JTextField inputEncodedXsdTfld;
    private javax.swing.ButtonGroup inputEncodingGroup;
    private javax.swing.JSeparator inputMessageTypeSep;
    private javax.swing.JComboBox messageTypeBox;
    private javax.swing.ButtonGroup messageTypeBtnGrp;
    private javax.swing.JLabel messageTypeLab;
    private javax.swing.JPanel messageTypePanel;
    private javax.swing.ButtonGroup outputEncodingGroup;
    private javax.swing.JLabel payloadProcessingSectionLab;
    private javax.swing.JLabel xsdElementTypeLab;
    // End of variables declaration//GEN-END:variables

}
