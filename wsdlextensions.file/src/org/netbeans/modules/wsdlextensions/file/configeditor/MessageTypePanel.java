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

/*
 * InboundMessagePanel.java
 *
 * Created on Jul 11, 2008, 2:14:05 AM
 */

package org.netbeans.modules.wsdlextensions.file.configeditor;

import java.awt.CardLayout;
import java.awt.Dialog;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.wsdlextensions.file.model.FileConstants;
import org.netbeans.modules.wsdlextensions.file.model.FileMessage;
import org.netbeans.modules.wsdlextensions.file.validator.FileComponentValidator;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.soa.wsdl.bindingsupport.ui.util.BindingComponentUtils;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author jalmero
 */
public class MessageTypePanel extends javax.swing.JPanel {

    /** the WSDL model to configure **/
    private WSDLComponent mWsdlComponent;
    
    private FileMessage mFileMessage;
    private String mOpName;
    private Part mPart;

    /** resource bundle for file bc **/
    private ResourceBundle mBundle = ResourceBundle.getBundle(
            "org.netbeans.modules.wsdlextensions.file.resources.Bundle");

    private static final Logger mLogger = Logger.
            getLogger(MessageTypePanel.class.getName());

    private DescriptionPanel descPanel = null;
    private DescriptionPanel inputDescPanel = null;
    private DescriptionPanel outputDescPanel = null;

    private MyItemListener mItemListener = null;
    private MyActionListener mActionListener = null;
    private MyDocumentListener mDocumentListener = null;

    private MyPopupAction mPopupAction = null;
    private JPopupMenu mPopupMenu = null;    
    private JCheckBoxMenuItem mShowDescriptionMenu = null;   
    
    private MyPopupAction mOutputPopupAction = null;
    private JPopupMenu mOutputPopupMenu = null;
    private JCheckBoxMenuItem mOutputShowDescriptionMenu = null;
    
    private JFormattedTextField fixedLengthTF = null;
    private JFormattedTextField onlyReadUpToTF = null;
    
    private MessageTypePanel mInstance = null;

    private Dialog mDetailsDlg = null;
    private Dialog mTextDlg = null;

    private DialogDescriptor mDetailsDlgDesc = null;
    private DialogDescriptor mTextDlgDesc = null;   
    
    private GlobalType mType = null;
    private GlobalElement mElement = null;
    
    private Project mProject = null;
    
    private boolean mIsInbound = true;
    private String mErrMessage = "";
    private boolean mPayloadEditable = true;

    /** Creates new form InboundMessagePanel */
    public MessageTypePanel(WSDLComponent component, Part part,
            FileMessage fileMessage, String operationName) {
        this(component, part, fileMessage, operationName, true);     
    }

    public MessageTypePanel(WSDLComponent component, Part part,
            FileMessage fileMessage, String operationName, boolean isInbound) {
        mInstance = this;
        mIsInbound = isInbound;
        initComponents();
        setAccessibility();
        populateView(component, part, fileMessage, null, operationName);        
        initCustomizeComponents();
    }    
    
    @Override
    public String getName() {
        return NbBundle.getMessage(MessageTypePanel.class,
                "InboundMessagePanel.StepLabel");
    }

    /**
     * Populate the view with the given the model component
     * @param component
     * @param part
     * @param fileMessage
     * @param operationName
     */
    public void populateView(WSDLComponent component, Part part,
            FileMessage fileMessage, Project project, String operationName) {
        cleanUp();
        mWsdlComponent = component;
        mPart = part;
        mFileMessage = fileMessage;
        mProject = project;
        mOpName = operationName;
        if (mOpName == null) {
            mOpName = "";
        }
        resetView();
        populateView();
        initListeners();
    }

    /**
     * Return the encoding style value
     * @return String encoding style
     */
    String getEncodingStyle() {
        return trimTextFieldInput(inputEncodedTypeTfld.getText());
    }

    /**
     * Return the use type for input message
     * @return String use type
     */
    String getInputUseType() {
        if (FileConstants.ENCODED_DATA.equals(messageTypeBox.getSelectedItem().toString())) {
            return FileConstants.ENCODED;
        } else {
            return FileConstants.LITERAL;
        }
    }
    
    /**
     * Return the remove eol value for the input message
     * @return boolean true if file is pattern; otherwise false
     */
    boolean getInputRemoveEOL() {
        if (inputRemoveEOLBox.isSelected()) {
            return FileConstants.stringValueIsTrue("true");
        } else {
            return false;
        }
    }
    
    /**
     * Return the remove eol value for the input message
     * @return boolean true if file is pattern; otherwise false
     */
    boolean getOutputAddEOL() {
        if (addEOLBox.isSelected()) {
            return FileConstants.stringValueIsTrue("true");
        } else {
            return false;
        }
    }
    
    /**
     * Return message type.  Options are FileConstants.XML_MESSAGE_TYPE,
     * FileConstants.TEXT_MESSAGE_TYPE, FileConstants.ENCODED_MESSAGE_TYPE
     * 
     * @return
     */
    int getMessageType() {
        if (FileConstants.XML.equals(messageTypeBox.getSelectedItem().toString())) {
            return FileConstants.XML_MESSAGE_TYPE;
        } else if (FileConstants.TEXT.equals(messageTypeBox.getSelectedItem().toString())) {
            return FileConstants.TEXT_MESSAGE_TYPE;
        } else if (FileConstants.BINARY.equals(messageTypeBox.getSelectedItem().toString())) {
            return FileConstants.BINARY_MESSAGE_TYPE;
        } else {
            return FileConstants.ENCODED_MESSAGE_TYPE;
        }
    }     
    
    GlobalType getSelectedPartType() {
        return mType;
    }
    
    GlobalElement getSelectedElementType() {
        return mElement;
    }
    
    /**
     * Return true if payload is to be forwarded as an attachment
     * @return boolean 
     */
    boolean getForwardAsAttachment() {
        return attachmentBox.isSelected();
    }
    
    /**
     * Return the charset value specified
     * @return
     */
    String getCharset() {
        if (getMessageType() == FileConstants.TEXT_MESSAGE_TYPE) {
            String val = null;
            if (charsetBox.getSelectedItem() != null) {//trimTextFieldInput(charsetFld.getText());
                val = charsetBox.getSelectedItem().toString();
            }
            if ((val != null) && (val.equals(FileConstants.CHARSET_DEFAULT))) {
                return null;
            } else {
                return val;
            }
        } else {
            return null;
        }        
    }
    
    boolean validVariableEntry(String val) {
        boolean valid = false;
        if ((val != null) && (val.startsWith("{$") && (val.endsWith("}")))) {
            valid = true;
        }
        return valid;
    }
    
    void setProject(Project project) {
        mProject = project;
    }
    
    void setDescriptionPanel(DescriptionPanel panel) {
        descPanel = panel;
    }
    
    /**
     * Enable the Processing Payload section accordingly
     * @param enable
     */
    public void enablePayloadProcessing(boolean enable) {
//        payloadProcessingSectionLab.setEnabled(enable);
//        inputMessageTypeSep.setEnabled(enable);
        messageTypeLab.setEnabled(enable);
        messageTypeBox.setEnabled(enable);
        xsdElemTypeLab.setEnabled(enable);
        inputEncodedTypeTfld.setEnabled(enable);
        inputEncodedDetailsBtn.setEnabled(enable);
//        inputXMLTfld.setEnabled(enable);
//        inputXmlDetailsBtn.setEnabled(enable);
        inputEncodedTypeLab.setEnabled(enable);
        inputEncodedXsdTfld.setEnabled(enable);
//        attachmentBox.setEnabled(enable);
//        addEOLBox.setEnabled(enable);
//        inputRemoveEOLBox.setEnabled(enable);
        
        charsetFld.setEnabled(enable);
        charsetBox.setEnabled(enable);
        charsetLab.setEnabled(enable);
        mPayloadEditable = enable;
            
        if (isEncodedTypeEnabled()) {
            inputEncodedTypeLab.setEnabled(true);
            inputEncodedTypeTfld.setEnabled(true);
            inputEncodedTypeLab.setVisible(true);
            inputEncodedTypeTfld.setVisible(true);
        }     
    }
    
    /**
     * Enables the xml message payload
     * @param enable
     */
    public void enableXMLPayloadProcessing(boolean enable) {
        // now a combobox; enable regardless
        messageTypeBox.setEnabled(true);
        
        // in case user selects the append which invalidates xml radio btn
        validateMe(true);
    }
   
    /**
     * Checks if the xml payload toggle is turned on
     * @return boolean true if the XML payload is selected
     */
    public boolean isXMLPayload() {
        if (FileConstants.XML.equals(messageTypeBox.getSelectedItem().toString())) {
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
        if (FileConstants.ENCODED_DATA.equals(messageTypeBox.getSelectedItem().toString())) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Allow client to enable the ForwardAsAttachment; ie for SolicitedRead,
     * even if data is an output, we allow this parameter (per mgmt)
     */
    public void enableForwardAsAttachment(boolean mode) {
        attachmentBox.setVisible(true);    
    }

    /**
     * Return true if the Payload Processing section is disabled or not
     * @return boolean
     */
    public boolean isPayloadEditable() {
        return mPayloadEditable;
    }
    
    /**
     * Validate the model
     * @return boolean true if model validation is successful; otherwise false
     */
    protected boolean validateContent() {
        // do FileBC-specific validation first

        FileError fileError = validateMe(true);
        if (ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_ERROR_EVT.equals(fileError.getErrorMode())) {
            return false;
        }

        ValidationResult results = new FileComponentValidator().
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
            ErrorPropagator.doFirePropertyChange(type, null, firstResult.getDescription(), this);
            return result;
        } else {
            ErrorPropagator.doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_CLEAR_MESSAGES_EVT, null, "", this);
            return true;
        }

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

    private void resetView() {              
        messageTypeBox.removeItemListener(mItemListener);
        
        inputEncodedDetailsBtn.removeActionListener(mActionListener);        
//        inputXmlDetailsBtn.removeActionListener(mActionListener);        
        
        inputEncodedTypeTfld.getDocument().removeDocumentListener(mDocumentListener);
        inputEncodedXsdTfld.getDocument().removeDocumentListener(mDocumentListener);
//        inputXMLTfld.getDocument().removeDocumentListener(mDocumentListener);
        
//        inputXMLTfld.setText("");
        inputEncodedTypeTfld.setText("");
        inputEncodedXsdTfld.setText("");   
        messageTypeBox.removeAllItems();
        charsetBox.removeAllItems();
        
        messageTypeBox.addItem(FileConstants.TEXT);
        messageTypeBox.addItem(FileConstants.BINARY);
        messageTypeBox.addItem(FileConstants.XML);
        messageTypeBox.addItem(FileConstants.ENCODED_DATA);      
        
        charsetBox.removeAllItems();
        charsetBox.addItem(FileConstants.CHARSET_DEFAULT);
        SortedMap<String, Charset> cs = Charset.availableCharsets();
        if ( cs != null ) {
            Iterator it = cs.keySet().iterator();
            while ( it.hasNext() ) {
                charsetBox.addItem(cs.get(it.next()).name());
            }
        }
        charsetFld.setText(FileConstants.CHARSET_DEFAULT);
        charsetBox.setSelectedItem(FileConstants.CHARSET_DEFAULT);
        mType = null;
        mElement = null;
        
        if (mIsInbound) {
            addEOLBox.setVisible(false);
            attachmentBox.setVisible(true);
            inputRemoveEOLBox.setVisible(true);
        } else {            
            addEOLBox.setVisible(true);
            attachmentBox.setVisible(false);  
            inputRemoveEOLBox.setVisible(false);
        }
        java.awt.Dimension charsetDim = charsetLab.getPreferredSize();
        java.awt.Dimension messageDim = messageTypeLab.getPreferredSize();
        if ((charsetDim != null) & (messageDim != null)) {
            messageTypeLab.setPreferredSize(new java.awt.Dimension(charsetDim.width, messageDim.height));
        }
    }

    private void updateDescriptionArea(FocusEvent evt) {
        if (descPanel != null) {
            descPanel.setText("");
        }

        // The image must first be wrapped in a style
//        Style style = mDoc.addStyle("StyleName", null);
//        StyleConstants.setIcon(style, mCASAImg);
        String[] desc = null;
        boolean casaEdited = false;

        if (evt.getSource() == fixedLengthTF) {
            desc = new String[]{"Max Bytes Per Record\n\n",
                    mBundle.getString("DESC_Attribute_maxBytesPerRecord")};
        } else if (evt.getSource() == onlyReadUpToTF) {
            desc = new String[]{"Max Bytes Per Record\n\n",
                    mBundle.getString("DESC_Attribute_maxBytesPerRecord")};
        }

        if (desc != null) {
            inputDescPanel.setText(desc[0], desc[1]);
            return;
        } 
        
        desc = null;
//        if (evt.getSource() == textRBtn) {
//            desc = new String[]{"Text\n\n",
//                   textRBtn.getToolTipText()};             
//        } else if (evt.getSource() == xmlRBtn) {
//            desc = new String[]{"XML\n\n",
//                   xmlRBtn.getToolTipText()};             
//        } else if (evt.getSource() == inputEncodedRBtn) {
//            desc = new String[]{"Encoded Data\n\n",
//                   inputEncodedRBtn.getToolTipText()};             
//        } 
        if (evt.getSource() == attachmentBox) {
            desc = new String[]{"Forward as Attachment\n\n",
                   attachmentBox.getToolTipText()};             
        } else if (evt.getSource() == addEOLBox) {
            desc = new String[]{"AddEOL\n\n",
                    mBundle.getString("DESC_Attribute_addEOL")};
        } else if (evt.getSource() == inputRemoveEOLBox) {
            desc = new String[]{"RemoveEOL\n\n",
                    mBundle.getString("DESC_Attribute_removeEOL")};
        } else if (evt.getSource() == messageTypeBox) {
            desc = new String[]{"Message Type\n\n",
                    messageTypeBox.getToolTipText()};
        } else if (evt.getSource() == charsetFld) {
            desc = new String[]{"Charset\n\n",
                    charsetFld.getToolTipText()};
        } else if (evt.getSource() == charsetBox) {
            desc = new String[]{"Character Encoding\n\n",
                    charsetBox.getToolTipText()};
        }
        if (desc != null) {
            descPanel.setText(desc[0], desc[1]);
            return;
        }             
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
        
        messageTypeBox.addItemListener(mItemListener);

        inputEncodedDetailsBtn.addActionListener(mActionListener);        
//        inputXmlDetailsBtn.addActionListener(mActionListener);
        
        inputEncodedTypeTfld.getDocument().addDocumentListener(mDocumentListener);
        inputEncodedXsdTfld.getDocument().addDocumentListener(mDocumentListener);
//        inputXMLTfld.getDocument().addDocumentListener(mDocumentListener);
                
    }
    
    private void populateView() {
        if (mFileMessage != null) {
            boolean encodedOn = false;
            if ((mFileMessage.getFileUseType() != null) &&
                    (mFileMessage.getFileUseType().equals(
                    FileConstants.ENCODED))) {
                messageTypeBox.setSelectedItem(FileConstants.ENCODED_DATA);
                encodedOn = true;
                if (mFileMessage.getFileEncodingStyle() != null) {
                    inputEncodedTypeTfld.setText(mFileMessage.getFileEncodingStyle());
                } else {
                    inputEncodedTypeTfld.setText("");
                }                
            } else {
                inputEncodedTypeTfld.setText("");
            } 
            
            attachmentBox.setSelected(mFileMessage.getForwardAsAttachment());

            if (mPart != null) {
                String ptStr = FileUtilities.getPartTypeOrElementString(mPart);
                if (FileConstants.XSD_STRING.equals(ptStr)) {
                    messageTypeBox.setSelectedItem(FileConstants.TEXT);
                } else if (FileConstants.XSD_BASE64_BINARY.equals(ptStr) && (!encodedOn)) {
                    messageTypeBox.setSelectedItem(FileConstants.BINARY);
                } else {
                    if (encodedOn) {
                        inputEncodedXsdTfld.setText(ptStr);
                    } else {
                        messageTypeBox.setSelectedItem(FileConstants.XML);
                        inputEncodedXsdTfld.setText(ptStr);
                    }
                }
            }    
           
            if (mIsInbound) {
                updateInboundRecordInfo();
            } else {
                updateOutboundRecordInfo();
            }           

            String charsetVal = mFileMessage.getAttribute(FileMessage.ATTR_FILE_CHARSET);
            if (getMessageType() == FileConstants.TEXT_MESSAGE_TYPE) {
                if (charsetVal != null) {
                    charsetFld.setText(charsetVal);
                    charsetBox.setSelectedItem(charsetVal);
                }            
            }
            
        } else {
            // null out view
//            inputXMLTfld.setText("");
            inputEncodedTypeTfld.setText("");
            inputRemoveEOLBox.setSelected(false); 
            
            addEOLBox.setSelected(false);
            inputRemoveEOLBox.setSelected(false);
                     
            
        }
        handleMessageTypeRecord();
    }   
    
    private void updateOutboundRecordInfo() {
        addEOLBox.setSelected(mFileMessage.getAddEOL());    
    }
    
    private void updateInboundRecordInfo() {
        inputRemoveEOLBox.setSelected(mFileMessage.getRemoveEOL());    
    }
    
    private void cleanUp() {     
        mWsdlComponent = null;
        mFileMessage = null;        
    }

    private void showXSDElementTypeDialog() {
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
                                "InboundMessagePanel.invalidType"));
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

    private void handleMessageTypeRecord() {       
        if (messageTypeBox.getSelectedItem().toString().equals(FileConstants.XML)) {
            inputEncodedXsdTfld.setVisible(true);
            inputEncodedTypeTfld.setVisible(false);
            inputEncodedTypeLab.setVisible(false);
            inputEncodedDetailsBtn.setVisible(true);
            xsdElemTypeLab.setVisible(true);

            charsetPanel.setVisible(false);
            charsetFld.setVisible(false);
            charsetBox.setVisible(false);
            charsetLab.setVisible(false);
                       
        } else if ((messageTypeBox.getSelectedItem().toString().equals(FileConstants.TEXT)) ||
                  (messageTypeBox.getSelectedItem().toString().equals(FileConstants.BINARY))) { 

            inputEncodedXsdTfld.setVisible(false);
            inputEncodedTypeTfld.setVisible(false);
            inputEncodedDetailsBtn.setVisible(false);
            inputEncodedTypeLab.setVisible(false);
            xsdElemTypeLab.setVisible(false);
            if (messageTypeBox.getSelectedItem().toString().equals("text")) {
                addEOLBox.setVisible(true);
                charsetPanel.setVisible(true);
                charsetFld.setVisible(true);
                charsetBox.setVisible(true);
                charsetLab.setVisible(true);
            } else { 
                addEOLBox.setVisible(false);
                charsetPanel.setVisible(false);
                charsetFld.setVisible(false);
                charsetBox.setVisible(false);
                charsetLab.setVisible(false);
            }           
           
        } else if (messageTypeBox.getSelectedItem().toString().equals(FileConstants.ENCODED_DATA)) { //inputEncodedRBtn.isSelected()) {
            inputEncodedXsdTfld.setVisible(true);
            inputEncodedTypeTfld.setVisible(true);
            inputEncodedTypeLab.setVisible(true);
            inputEncodedDetailsBtn.setVisible(true);
            xsdElemTypeLab.setVisible(true);

            charsetPanel.setVisible(false);
            charsetFld.setVisible(false);
            charsetBox.setVisible(false);
            charsetLab.setVisible(false);
        }
        // need to make sure both element and encoding style are supplied before
        // allowing user to continue
        validateMe(true);
        charsetFld.setVisible(false);
    }

    private void handleMessageTypeRecordOrig() {
        if (messageTypeBox.getSelectedItem().toString().equals(FileConstants.XML)) {
            inputEncodedXsdTfld.setEnabled(false);
            inputEncodedTypeTfld.setEnabled(false);
            inputEncodedDetailsBtn.setEnabled(false);
            inputEncodedTypeLab.setEnabled(false);
            inputEncodedXsdTfld.setEnabled(true);
            inputEncodedTypeTfld.setEnabled(false);
            inputEncodedTypeLab.setEnabled(false);               
            inputEncodedDetailsBtn.setEnabled(true);  
            
            charsetFld.setEnabled(false);
            charsetLab.setEnabled(false);            
                       
        } else if ((messageTypeBox.getSelectedItem().toString().equals(FileConstants.TEXT)) ||
                  (messageTypeBox.getSelectedItem().toString().equals(FileConstants.BINARY))) { 
//            inputXMLTfld.setEnabled(false);
//            inputXmlDetailsBtn.setEnabled(false);
            inputEncodedXsdTfld.setEnabled(false);
            inputEncodedTypeTfld.setEnabled(false);
            inputEncodedDetailsBtn.setEnabled(false);
            inputEncodedTypeLab.setEnabled(false);
            if (messageTypeBox.getSelectedItem().toString().equals("text")) {
                addEOLBox.setEnabled(true);
                charsetFld.setEnabled(true);
                charsetLab.setEnabled(true);                 
            } else { 
                addEOLBox.setEnabled(false);  
                charsetFld.setEnabled(false);
                charsetLab.setEnabled(false);                 
            }           
            inputEncodedXsdTfld.setEnabled(false);
            inputEncodedTypeTfld.setEnabled(false);
            inputEncodedTypeLab.setEnabled(false);            
            inputEncodedDetailsBtn.setEnabled(false);             
        } else if (messageTypeBox.getSelectedItem().toString().equals(FileConstants.ENCODED_DATA)) { //inputEncodedRBtn.isSelected()) {
//            inputXMLTfld.setEnabled(false);
//            inputXmlDetailsBtn.setEnabled(false);
            inputEncodedXsdTfld.setEnabled(true);
            inputEncodedTypeTfld.setEnabled(true);
            inputEncodedDetailsBtn.setEnabled(true);
            inputEncodedTypeLab.setEnabled(true);
            inputEncodedXsdTfld.setEnabled(true);
            inputEncodedTypeTfld.setEnabled(true);
            inputEncodedTypeLab.setEnabled(true);
            inputEncodedDetailsBtn.setEnabled(true); 
            
            charsetFld.setEnabled(false);
            charsetLab.setEnabled(false);            
        }
        // need to make sure both element and encoding style are supplied before
        // allowing user to continue
        validateMe(true);       
    }
    
    public boolean isEncodedTypeEnabled() {
        boolean enable = false;  
        // if this is launched from CASA, then we should still allow 
        // configuration of encoded type IF it is an XML type and
        // if element attribute is defined or
        // if type attribute is defined AND not built in type        
        if ((!isPayloadEditable()) && (isXMLPayload())) {   

                if (mPart != null) {
                    if (mPart.getElement() != null) {
                        enable = true;
                    } else if (mPart.getType() != null) {
                        String ns = mPart.getType().getEffectiveNamespace();
                        boolean isBuiltInType = WSDLUtilities.isBuiltInType(ns);
                        if (!isBuiltInType) {
                            enable = true;
                        }
                    }
                }
            
        }
        return enable;
    }
    
    private void handleActionPerformed(ActionEvent evt) {
        if (evt.getSource() == inputEncodedDetailsBtn) {
            showEncodedTypeDialog();
        } //else if (evt.getSource() == inputXmlDetailsBtn) {
//            showXSDElementTypeDialog();
//        }
    }   
    
    public class MyPopupAction extends AbstractAction {
        public void actionPerformed(ActionEvent evt) {
            if (mIsInbound) {
                if (mShowDescriptionMenu.isSelected()) {
                    enableDescriptionView(false);
                } else {
                    enableDescriptionView(true);
                }
            } else {
                if (mOutputShowDescriptionMenu.isSelected()) {
                    enableDescriptionView(false);
                } else {
                    enableDescriptionView(true);
                }                
            }
        }
    }

    private void enableDescriptionView(boolean show) {
        if (show) {
            if (mIsInbound) {
                splitpaneText.remove(descriptionTextPanel);
            }
        } else {         
            if (mIsInbound) {
                descriptionTextPanel.add(inputDescPanel,
                        java.awt.BorderLayout.CENTER);
                splitpaneText.setBottomComponent(descriptionTextPanel);
            }
        }
    }

    void showPopup(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            if (mIsInbound) {
                if (mPopupAction == null) {
                    mPopupAction = new MyPopupAction();

                    mPopupMenu = new JPopupMenu();
                    mShowDescriptionMenu = new JCheckBoxMenuItem(mPopupAction);
                    mShowDescriptionMenu.setSelected(true);
                    mPopupMenu.add(mShowDescriptionMenu);
                    mShowDescriptionMenu.setText(org.openide.util.NbBundle.getMessage(MessageTypePanel.class,
                        "InboundMessagePanel.ShowDescription"));
                }

                Point p = evt.getPoint();
                mPopupMenu.show(evt.getComponent(), p.x, p.y);
            } else {
                if (mOutputPopupAction == null) {
                    mOutputPopupAction = new MyPopupAction();

                    mOutputPopupMenu = new JPopupMenu();
                    mOutputShowDescriptionMenu = new JCheckBoxMenuItem(mOutputPopupAction);
                    mOutputShowDescriptionMenu.setSelected(true);
                    mOutputPopupMenu.add(mOutputShowDescriptionMenu);
                    mOutputShowDescriptionMenu.setText(org.openide.util.NbBundle.getMessage(MessageTypePanel.class,
                        "InboundMessagePanel.ShowDescription"));
                }

                Point p = evt.getPoint();
                mOutputPopupMenu.show(evt.getComponent(), p.x, p.y);                
            }

        }
        
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
    
    private void setAccessibility() {
        messageTypeBox.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_messageType")); // NOI18N
        messageTypeBox.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_messageType")); // NOI18N        
        inputEncodedXsdTfld.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_PayloadProcessing_encodeddata")); // NOI18N
        inputEncodedXsdTfld.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_PayloadProcessing_encodeddata")); // NOI18N        
        inputEncodedDetailsBtn.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_PayloadProcessing_encodeddata")); // NOI18N
        inputEncodedDetailsBtn.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_PayloadProcessing_encodeddata")); // NOI18N                
    }

    private void initCustomizeComponents() {
        messageTypeBox.setPreferredSize(new java.awt.Dimension(10, messageTypeBox.getPreferredSize().height));
        charsetFld.setPreferredSize(new java.awt.Dimension(25, charsetFld.getPreferredSize().height));
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

        inputMessageTypeBtnGrp = new javax.swing.ButtonGroup();
        messageRecordBtnGrp = new javax.swing.ButtonGroup();
        delimitedFixedBtnGrp = new javax.swing.ButtonGroup();
        outputMessageRecordBtnGrp = new javax.swing.ButtonGroup();
        textPopupPanel = new javax.swing.JPanel();
        splitpaneText = new javax.swing.JSplitPane();
        textDetailsPanel = new javax.swing.JPanel();
        Unwrapping = new javax.swing.JLabel();
        unwrappingSectionSep1 = new javax.swing.JSeparator();
        jPanel2 = new javax.swing.JPanel();
        descriptionTextPanel = new javax.swing.JPanel();
        messageTypePanel = new javax.swing.JPanel();
        payloadProcessingSectionLab = new javax.swing.JLabel();
        inputMessageTypeSep = new javax.swing.JSeparator();
        messageTypeLab = new javax.swing.JLabel();
        messageTypeBox = new javax.swing.JComboBox();
        charsetLab = new javax.swing.JLabel();
        charsetPanel = new javax.swing.JPanel();
        charsetFld = new javax.swing.JTextField();
        charsetBox = new javax.swing.JComboBox();
        addEOLBox = new javax.swing.JCheckBox();
        xsdElemTypeLab = new javax.swing.JLabel();
        inputEncodedXsdTfld = new javax.swing.JTextField();
        inputEncodedDetailsBtn = new javax.swing.JButton();
        inputEncodedTypeLab = new javax.swing.JLabel();
        inputEncodedTypeTfld = new javax.swing.JTextField();
        attachmentBox = new javax.swing.JCheckBox();
        inputRemoveEOLBox = new javax.swing.JCheckBox();

        textPopupPanel.setName("textPopupPanel"); // NOI18N
        textPopupPanel.setPreferredSize(new java.awt.Dimension(350, 380));
        textPopupPanel.setLayout(new java.awt.BorderLayout());

        splitpaneText.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitpaneText.setName("splitpaneText"); // NOI18N

        textDetailsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        textDetailsPanel.setName("textDetailsPanel"); // NOI18N
        textDetailsPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                textDetailsPanelMouseReleased(evt);
            }
        });
        textDetailsPanel.setLayout(new java.awt.GridBagLayout());

        Unwrapping.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(Unwrapping, org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.Unwrapping.text")); // NOI18N
        Unwrapping.setName("Unwrapping"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        textDetailsPanel.add(Unwrapping, gridBagConstraints);

        unwrappingSectionSep1.setName("unwrappingSectionSep1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 80, 0, 0);
        textDetailsPanel.add(unwrappingSectionSep1, gridBagConstraints);

        jPanel2.setName("jPanel2"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 0.5;
        textDetailsPanel.add(jPanel2, gridBagConstraints);

        splitpaneText.setTopComponent(textDetailsPanel);

        descriptionTextPanel.setInheritsPopupMenu(true);
        descriptionTextPanel.setMinimumSize(new java.awt.Dimension(300, 50));
        descriptionTextPanel.setName("descriptionTextPanel"); // NOI18N
        descriptionTextPanel.setPreferredSize(new java.awt.Dimension(300, 50));
        descriptionTextPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                descriptionTextPanelMouseReleased(evt);
            }
        });
        descriptionTextPanel.setLayout(new java.awt.BorderLayout());
        splitpaneText.setBottomComponent(descriptionTextPanel);
        inputDescPanel = new DescriptionPanel();
        descriptionTextPanel.add(inputDescPanel, java.awt.BorderLayout.CENTER);

        textPopupPanel.add(splitpaneText, java.awt.BorderLayout.CENTER);

        setName("Form"); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        messageTypePanel.setName("messageTypePanel"); // NOI18N
        messageTypePanel.setLayout(new java.awt.GridBagLayout());

        payloadProcessingSectionLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(payloadProcessingSectionLab, org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "InboundMessagePanel.jLabel4.text")); // NOI18N
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

        messageTypeLab.setLabelFor(messageTypeBox);
        org.openide.awt.Mnemonics.setLocalizedText(messageTypeLab, org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.messageTypeLab.text")); // NOI18N
        messageTypeLab.setName("messageTypeLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
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
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        messageTypePanel.add(messageTypeBox, gridBagConstraints);
        charsetLab.setLabelFor(charsetFld);
        org.openide.awt.Mnemonics.setLocalizedText(charsetLab, org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.charsetLab.text")); // NOI18N
        charsetLab.setName("charsetLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        messageTypePanel.add(charsetLab, gridBagConstraints);

        charsetPanel.setName("charsetPanel"); // NOI18N
        charsetPanel.setLayout(new java.awt.GridBagLayout());

        charsetFld.setToolTipText(mBundle.getString("DESC_Attribute_charset"));
        charsetFld.setName("charsetFld"); // NOI18N
        charsetFld.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                charsetFldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        charsetPanel.add(charsetFld, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/wsdlextensions/file/resources/Bundle"); // NOI18N
        charsetFld.getAccessibleContext().setAccessibleName(bundle.getString("DESC_Attribute_charset")); // NOI18N
        charsetFld.getAccessibleContext().setAccessibleDescription(bundle.getString("DESC_Attribute_charset")); // NOI18N

        charsetBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        charsetBox.setToolTipText(mBundle.getString("DESC_Attribute_charset"));
        charsetBox.setName("charsetBox"); // NOI18N
        charsetBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                charsetBoxFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        charsetPanel.add(charsetBox, gridBagConstraints);
        charsetBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.charsetBox.AccessibleContext.accessibleName")); // NOI18N
        charsetBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.charsetBox.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addEOLBox, org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.addEOLBox.text")); // NOI18N
        addEOLBox.setToolTipText(mBundle.getString("DESC_Attribute_addEOL"));
        addEOLBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        addEOLBox.setName("addEOLBox"); // NOI18N
        addEOLBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                addEOLBoxparameterFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        charsetPanel.add(addEOLBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        messageTypePanel.add(charsetPanel, gridBagConstraints);

        xsdElemTypeLab.setLabelFor(inputEncodedXsdTfld);
        org.openide.awt.Mnemonics.setLocalizedText(xsdElemTypeLab, org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.xsdElemTypeLab.text")); // NOI18N
        xsdElemTypeLab.setName("xsdElemTypeLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        messageTypePanel.add(xsdElemTypeLab, gridBagConstraints);

        inputEncodedXsdTfld.setName("inputEncodedXsdTfld"); // NOI18N
        inputEncodedXsdTfld.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                inputEncodedXsdTfldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        messageTypePanel.add(inputEncodedXsdTfld, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(inputEncodedDetailsBtn, org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.inputEncodedDetailsBtn.text")); // NOI18N
        inputEncodedDetailsBtn.setMargin(new java.awt.Insets(2, 5, 2, 5));
        inputEncodedDetailsBtn.setName("inputEncodedDetailsBtn"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        messageTypePanel.add(inputEncodedDetailsBtn, gridBagConstraints);

        inputEncodedTypeLab.setLabelFor(inputEncodedTypeTfld);
        org.openide.awt.Mnemonics.setLocalizedText(inputEncodedTypeLab, org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.inpuTypeLab.text")); // NOI18N
        inputEncodedTypeLab.setName("inputEncodedTypeLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        messageTypePanel.add(inputEncodedTypeLab, gridBagConstraints);

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
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        messageTypePanel.add(inputEncodedTypeTfld, gridBagConstraints);
        inputEncodedTypeTfld.getAccessibleContext().setAccessibleName(bundle.getString("DESC_Attribute_encodingStyle")); // NOI18N
        inputEncodedTypeTfld.getAccessibleContext().setAccessibleDescription(bundle.getString("DESC_Attribute_encodingStyle")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(attachmentBox, org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.attachmentBox.text")); // NOI18N
        attachmentBox.setToolTipText(mBundle.getString("DESC_Attribute_ForwardAsAttachment"));
        attachmentBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        attachmentBox.setName("attachmentBox"); // NOI18N
        attachmentBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                attachmentBoxFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        messageTypePanel.add(attachmentBox, gridBagConstraints);

        inputRemoveEOLBox.setMnemonic('t');
        org.openide.awt.Mnemonics.setLocalizedText(inputRemoveEOLBox, org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.inputRemoveEOLBox.text")); // NOI18N
        inputRemoveEOLBox.setToolTipText(bundle.getString("DESC_Attribute_removeEOL")); // NOI18N
        inputRemoveEOLBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        inputRemoveEOLBox.setName("inputRemoveEOLBox"); // NOI18N
        inputRemoveEOLBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                inputRemoveEOLBoxparameterFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        messageTypePanel.add(inputRemoveEOLBox, gridBagConstraints);
        inputRemoveEOLBox.getAccessibleContext().setAccessibleName(bundle.getString("DESC_Attribute_removeEOL")); // NOI18N
        inputRemoveEOLBox.getAccessibleContext().setAccessibleDescription(bundle.getString("DESC_Attribute_removeEOL")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        add(messageTypePanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void inputRemoveEOLBoxparameterFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputRemoveEOLBoxparameterFocusGained
        // TODO add your handling code here:
        updateDescriptionArea(evt);
    }//GEN-LAST:event_inputRemoveEOLBoxparameterFocusGained

    private void descriptionTextPanelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_descriptionTextPanelMouseReleased
        // TODO add your handling code here:
        showPopup(evt);
    }//GEN-LAST:event_descriptionTextPanelMouseReleased

    private void textDetailsPanelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_textDetailsPanelMouseReleased
        // TODO add your handling code here:
        showPopup(evt);
    }//GEN-LAST:event_textDetailsPanelMouseReleased

private void inputEncodedXsdTfldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputEncodedXsdTfldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_inputEncodedXsdTfldFocusGained

private void inputEncodedTypeTfldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputEncodedTypeTfldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_inputEncodedTypeTfldFocusGained

private void attachmentBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_attachmentBoxFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_attachmentBoxFocusGained

private void addEOLBoxparameterFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_addEOLBoxparameterFocusGained
// TODO add your handling code here:
        updateDescriptionArea(evt);
}//GEN-LAST:event_addEOLBoxparameterFocusGained

private void messageTypeBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_messageTypeBoxFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_messageTypeBoxFocusGained

private void charsetFldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_charsetFldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_charsetFldFocusGained

private void charsetBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_charsetBoxFocusGained
    // TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_charsetBoxFocusGained


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Unwrapping;
    private javax.swing.JCheckBox addEOLBox;
    private javax.swing.JCheckBox attachmentBox;
    private javax.swing.JComboBox charsetBox;
    private javax.swing.JTextField charsetFld;
    private javax.swing.JLabel charsetLab;
    private javax.swing.JPanel charsetPanel;
    private javax.swing.ButtonGroup delimitedFixedBtnGrp;
    private javax.swing.JPanel descriptionTextPanel;
    private javax.swing.JButton inputEncodedDetailsBtn;
    private javax.swing.JLabel inputEncodedTypeLab;
    private javax.swing.JTextField inputEncodedTypeTfld;
    private javax.swing.JTextField inputEncodedXsdTfld;
    private javax.swing.ButtonGroup inputMessageTypeBtnGrp;
    private javax.swing.JSeparator inputMessageTypeSep;
    private javax.swing.JCheckBox inputRemoveEOLBox;
    private javax.swing.JPanel jPanel2;
    private javax.swing.ButtonGroup messageRecordBtnGrp;
    private javax.swing.JComboBox messageTypeBox;
    private javax.swing.JLabel messageTypeLab;
    private javax.swing.JPanel messageTypePanel;
    private javax.swing.ButtonGroup outputMessageRecordBtnGrp;
    private javax.swing.JLabel payloadProcessingSectionLab;
    private javax.swing.JSplitPane splitpaneText;
    private javax.swing.JPanel textDetailsPanel;
    private javax.swing.JPanel textPopupPanel;
    private javax.swing.JSeparator unwrappingSectionSep1;
    private javax.swing.JLabel xsdElemTypeLab;
    // End of variables declaration//GEN-END:variables

}
