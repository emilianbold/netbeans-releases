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

/*
 * InboundMessagePanel.java
 *
 * Created on Jul 11, 2008, 2:14:05 AM
 */

package org.netbeans.modules.wsdlextensions.file.configeditor;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.modules.wsdlextensions.file.model.FileAddress;
import org.netbeans.modules.wsdlextensions.file.model.FileBinding;
import org.netbeans.modules.wsdlextensions.file.model.FileConstants;
import org.netbeans.modules.wsdlextensions.file.model.FileMessage;
import org.netbeans.modules.wsdlextensions.file.model.FileOperation;
import org.netbeans.modules.wsdlextensions.file.validator.FileComponentValidator;
import org.netbeans.modules.wsdlextensions.file.validator.Utils;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.bindingsupport.utils.WSDLUtils;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
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
 *
 * @author jalmero
 */
public class InboundMessagePanel extends javax.swing.JPanel {

    /** the WSDL model to configure **/
    private WSDLComponent mWsdlComponent;

    /** QName **/
    private QName mQName;
    
    /**
     * Project associated with this wsdl
     */
    private Project mProject = null;

    /** resource bundle for file bc **/
    private ResourceBundle mBundle = ResourceBundle.getBundle(
            "org.netbeans.modules.wsdlextensions.file.resources.Bundle");

    private static final Logger mLogger = Logger.
            getLogger(FileBindingConfigurationPanel.class.getName());

    private DescriptionPanel descPanel = null;
    private DescriptionPanel descPanelArchivePanel = null;
    private DescriptionPanel descPanelTextPanel = null;

    private MyItemListener mItemListener = null;
    private MyActionListener mActionListener = null;
    private MyDocumentListener mDocumentListener = null;


    private InboundMessagePanel mInstance = null;

    private Dialog mDetailsDlg = null;
    private Dialog mTextDlg = null;

    private DialogDescriptor mDetailsDlgDesc = null;
    private DialogDescriptor mTextDlgDesc = null;   

    private JFormattedTextField fixedLengthTF = null;
    private JFormattedTextField onlyReadUpToTF = null;
    
    private GlobalType mType = null;
    private GlobalElement mElement = null;
    
    private Part mPart = null;
    
    private MessageTypePanel mMessageTypePanel = null;
    private FileMessage mInputFileMessage = null;

    /** Creates new form InboundMessagePanel */
    public InboundMessagePanel(QName qName, WSDLComponent component) {
        mInstance = this;
        initComponents();
        initCustomComponents();
        initFileChooser();
        populateView(qName, component);        
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(InboundMessagePanel.class,
                "InboundMessagePanel.StepLabel");
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
        mMessageTypePanel.setDescriptionPanel(descPanel);
        initListeners();
    }

    /**
     * Return the directory setting
     * @return String directory path
     */
    String getDirectory() {
        return trimTextFieldInput(directoryTextField.getText());
    }

    boolean getPollRecursive() {
        return pollRecursiveCheckBox.isSelected();
    }

    String getPollRecursiveExclude() {
        return trimTextFieldInput(pollRecursiveExcludeText.getText());
    }

    /**
     * Return true if path is relative path
     * @return boolean true if path specified is a relative path
     */
    boolean getRelativePath() {
        // will use path relative to box to set this field with
        if (getPathRelativeTo() != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return the path relative to value
     * @return String path relative to name
     */
    String getPathRelativeTo() {
        if ((pathRelativeToComboBox.getSelectedItem() != null) &&
            (!pathRelativeToComboBox.getSelectedItem().toString().equals(FileConstants.NOT_SET))) {
            return pathRelativeToComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    /**
     * Return the file name
     * @return String file name
     */
    String getFileName() {
        // since the enhancement that
        // a regular expression can be put
        // as the file matching criteria
        // and a regex can contain chars
        // like "/" or "\" etc. that might be
        // recognized by java.io.File as path separator
        // hence causing the regular expression
        // screwed up
        // refer to open esb issue 2126:
        // https://open-esb.dev.java.net/issues/show_bug.cgi?id=2126
        //
//        File file = new File(inputFileNameTextField.getText());
//        return file.getName();
        return inputFileNameTextField.getText();
    }

    /**
     * Return the polling interval
     * @return String polling interval
     */
    String getInputPollingInterval() {
        if (inputPollingTfld.getText().length() == 0) {
            return null;
        } else {
            return inputPollingTfld.getText();
        }        
    }

    /**
     * Return the encoding style value
     * @return String encoding style
     */
    String getEncodingStyle() {
        return mMessageTypePanel.getEncodingStyle();
    }

    /**
     * Return the file is pattern value for input message
     * @return boolean true if file is pattern; otherwise false
     */
    boolean getInputFileIsPattern() {
        return isValidPatternSpecified();
    }

    /**
     * Return archive value for input message
     * @return boolean true if archive is set; otherwise false
     */
    boolean getInputArchive() {
        if (inputArchiveBox.isSelected()) {
            return FileConstants.stringValueIsTrue("true");
        } else {
            return false;
        }
    }

    /**
     * Return is regex flag
     * @return boolean true if regex is set; otherwise false
     */
    boolean getInputFileIsRegex() {
        if (inputFileRegExBox.isSelected()) {
            return FileConstants.stringValueIsTrue("true");
        } else {
            return false;
        }
    }

    /**
     * Return archive relative value for input message
     * @return boolean true if archive relative is set; otherwise false
     */
    boolean getInputArchiveRelative() {
        return inputArchiveRelativeToPollingDir.isSelected();
    }

    /**
     * Return archive directory value for input message
     * @return String archive directory
     */
    String getInputArchiveDirectory() {
        return trimTextFieldInput(inputArchiveDirectoryTextField.getText());
    }

    /**
     * Return the remove eol value for the input message
     * @return boolean true if file is pattern; otherwise false
     */
    boolean getInputRemoveEOL() {
        return mMessageTypePanel.getInputRemoveEOL();
    }

    /**
     * Return record delimiter for input message
     * @return String record delimiter
     */
    String getInputRecordDelimiter() {
        if ((inputMultipleRecordBox.isSelected()) && (inputDelimiterBox.isEnabled()))  {
            return trimTextFieldInput(inputDelimiterBox.getSelectedItem().toString());          
        } 
        return null;                   
    }

    /**
     * Return multi records per file value for input message
     * @return boolean true if multi records per file is set; otherwise false
     */
    boolean getInputMultiRecordsPerFile() {
        if (inputMultipleRecordBox.isSelected()) {
            return true;
        } else {
            return false;
        }        
    }

    /**
     * Return maximum bytes per record value for input message
     * @return long maximum bytes per record
     */
    String getInputMaxBytesPerRecord() {
        if (maxBytesPerRecordBox.isSelected()) {
             if (maxBytesPeRecordTfld.getText().length() > 0) {
                 return maxBytesPeRecordTfld.getText();
             }
        }
        return null;        
    }

    FileAddress getFileAddressPerSelectedPort() {
        FileAddress address = null;
        Port selectedServicePort = (Port) servicePortComboBox.getSelectedItem();
        if (selectedServicePort != null) {
            Binding binding = selectedServicePort.getBinding().get();
            String selBindingName = bindingNameComboBox.
                    getSelectedItem().toString();
            if ((binding != null) && (binding.getName().
                    equals(selBindingName))) {
                Iterator<FileAddress> fileAddresses = selectedServicePort.
                        getExtensibilityElements(FileAddress.class).
                        iterator();
                // 1 fileaddress for 1 binding
                while (fileAddresses.hasNext()) {
                    return fileAddresses.next();
                }
            }
        }
        return address;
    }    

    /**
     * Return the operation name
     * @return String operation name
     */
    String getOperationName() {
        if ((operationNameComboBox.getSelectedItem() != null) &&
                (!operationNameComboBox.getSelectedItem().toString().
                equals(FileConstants.NOT_SET))) {
            return operationNameComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    /**
     * Return the use type for input message
     * @return String use type
     */
    String getInputUseType() {
        return mMessageTypePanel.getInputUseType();
    }

    /**
     * Return the part value for the input message
     * return String part used
     */
    String getInputPart() {
        return trimTextFieldInput((String) inputPartComboBox.getSelectedItem());
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
    
    /**
     * Returns the selected part 
     * @return
     */
    GlobalType getSelectedPartType() {
        return mMessageTypePanel.getSelectedPartType();
    }
    
    /**
     * Returns the selected element
     * @return
     */
    GlobalElement getSelectedElementType() {
        return mMessageTypePanel.getSelectedElementType();
    }
    
    /**
     * Return true if payload is to be forwarded as an attachment
     * @return boolean 
     */
    boolean getForwardAsAttachment() {
        return mMessageTypePanel.getForwardAsAttachment();
    }
    
    /**
     * Set the Project associated with the wsdl for this panel
     * @param project
     */
    void setProject(Project project) {
        mProject = project;
        mMessageTypePanel.setProject(project);
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
    
    /**
     * Route the property change event to this panel
     */
    public void doFirePropertyChange(String name, Object oldValue, Object newValue) {           
        firePropertyChange(name, oldValue, 
                newValue);         
    }   
    
    /**
     * Return the message type for input message
     * @return String message type
     */
    String getInputMessageType() {
        // now that we support both binary and text, we return the right 
        // message type based on the payload processing since user can change
        // the message type
        if (mMessageTypePanel.getMessageType() == FileConstants.BINARY_MESSAGE_TYPE) {
            return FileConstants.BINARY;
        } else {
            return FileConstants.TEXT;
        }
    }
    
    /**
     * Return the charset defined for the text payload
     * @return String charset value
     */
    public String getCharset() {
        return mMessageTypePanel.getCharset();   
    }
    
    /**
     * Return true if the Payload Processing section is disabled or not
     * @return boolean
     */
    public boolean isPayloadEditable() {
        return mMessageTypePanel.isPayloadEditable();
    }
    
    /**
     * Return true if the encoded type is enabled or not
     * @return boolean
     */
    public boolean isEncodedTypeEnabled() {
        return mMessageTypePanel.isEncodedTypeEnabled();
    }
    
    /**
     * Validate the model
     * @return boolean true if model validation is successful; otherwise false
     */
    protected boolean validateContent() {
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
            doFirePropertyChange(type, null, firstResult.getDescription());
            return result;
        } else {
            doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_CLEAR_MESSAGES_EVT, null, "");
            return true;
        }

    }
    
    private FileError validateFile() {
        return validateFile(false);
    }

    private FileError validateFile(boolean fireEvent) {
        FileError fileError = new FileError();
        if (inputFileNameTextField.getText().length() == 0) {
            if (fireEvent) {
                ErrorPropagator.doFirePropertyChange(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_ERROR_EVT, null, NbBundle.getMessage(
                        FileBindingConfigurationPanel.class,
                        "InboundMessagePanel.FileNameMustBeSet"), mMessageTypePanel);
            }
            fileError.setErrorMessage(NbBundle.getMessage(
                        FileBindingConfigurationPanel.class,
                        "InboundMessagePanel.FileNameMustBeSet"));
            fileError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_ERROR_EVT);
        } else {
            if (fireEvent) {
                ErrorPropagator.doFirePropertyChange(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_CLEAR_MESSAGES_EVT, null, "", mMessageTypePanel);  
            }
        }
        return fileError;
    }
    
    private FileError validateDirectory() {
        return validateDirectory(false);
    }
    
    private FileError validateDirectory(boolean fireEvent) {
        FileError fileError = new FileError();
        String directory = getDirectory();
        if ((getPathRelativeTo() != null) && ((directory == null) ||
                (directory.equals("")))) {
            if (fireEvent) {
                ErrorPropagator.doFirePropertyChange(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_ERROR_EVT, null, NbBundle.getMessage(
                        FileBindingConfigurationPanel.class,
                        "FileBindingConfigurationPanel.FileDirectoryMustBeSet"), mMessageTypePanel);
            }
            fileError.setErrorMessage(NbBundle.getMessage(
                        FileBindingConfigurationPanel.class,
                        "FileBindingConfigurationPanel.FileDirectoryMustBeSet"));
            fileError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_ERROR_EVT);
        } else if ((directory == null) ||
                (directory.equals(""))) {           
            if (fireEvent) {
                ErrorPropagator.doFirePropertyChange(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_ERROR_EVT, null, NbBundle.getMessage(
                        FileBindingConfigurationPanel.class,
                        "FileBindingConfigurationPanel.FileDirectoryMustBeSet"), mMessageTypePanel);
            }
            fileError.setErrorMessage(NbBundle.getMessage(
                        FileBindingConfigurationPanel.class,
                        "FileBindingConfigurationPanel.FileDirectoryMustBeSet"));
            fileError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_ERROR_EVT);             
        } else {
            if (fireEvent) {
                ErrorPropagator.doFirePropertyChange(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_CLEAR_MESSAGES_EVT, null, "", mMessageTypePanel);            
            }
        }
        return fileError;
    }
        
    public FileError validateMe() {
        return validateMe(false);
    }
    
    public FileError validateMe(boolean fireEvent) {  
        // validate directory
        FileError fileError = validateDirectory(fireEvent);
        if (ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT.equals(fileError.getErrorMode())) {
            
            // validate file
            fileError = validateFile(fireEvent);  
            if (ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT.equals(fileError.getErrorMode())) {
                
                // validate relative path box
                fileError = handlePathRelativeToComboBoxChange(fireEvent);
                if (ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_CLEAR_MESSAGES_EVT.equals(fileError.getErrorMode())) {
                    
                    // validate polling and max btes for app variable
                    if ((getInputPollingInterval() != null) &&
                            (FileUtilities.validVariableEntry(getInputPollingInterval()))) {
                                                

                        // validate polling and max btes for app variable
                        if (maxBytesPerRecordBox.isSelected()) {
                            if ((getInputMaxBytesPerRecord() != null) &&
                                    (FileUtilities.validVariableEntry(getInputMaxBytesPerRecord()))) {
                                if (ExtensibilityElementConfigurationEditorComponent.
                                        PROPERTY_CLEAR_MESSAGES_EVT.equals(fileError.getErrorMode())) {

                                    // validate message section
                                    fileError = mMessageTypePanel.validateMe();                         
                                }  else {
                                    fileError.setErrorMessage(NbBundle.getMessage(InboundMessagePanel.class, 
                                            "InboundMessagePanel.invalidVariable"));
                                    fileError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.
                                        PROPERTY_ERROR_EVT);                                    
                                }                          
                            } else {
                                if (getInputMaxBytesPerRecord() == null) {
                                    fileError.setErrorMessage(NbBundle.getMessage(InboundMessagePanel.class, 
                                            "InboundMessagePanel.EMPTY_MAX_RECORD"));
                                    fileError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.
                                        PROPERTY_ERROR_EVT);                                 
                                } else {                        
                                    fileError.setErrorMessage(NbBundle.getMessage(InboundMessagePanel.class, 
                                            "InboundMessagePanel.invalidVariable"));
                                    fileError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.
                                        PROPERTY_ERROR_EVT);                              
                                }
                            }    
                        } else {
                            // validate message section
                            fileError = mMessageTypePanel.validateMe();                             
                        }
                    } else {
                        if (getInputMaxBytesPerRecord() == null) {
                                fileError.setErrorMessage(NbBundle.getMessage(InboundMessagePanel.class, 
                                        "InboundMessagePanel.EMPTY_POLLING"));
                                fileError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.
                                    PROPERTY_ERROR_EVT);                                 
                         } else {                       
                            fileError.setErrorMessage(NbBundle.getMessage(InboundMessagePanel.class, 
                                    "InboundMessagePanel.invalidVariable"));
                            fileError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.
                                PROPERTY_ERROR_EVT);   
                        }
                    }                   
                }                           
            }
        }

        if (fireEvent) {
            ErrorPropagator.doFirePropertyChange(fileError.getErrorMode(), null,
                    fileError.getErrorMessage(), mMessageTypePanel);
        }      
        return fileError;
    }

    private boolean isValidPatternSpecified() {
        boolean isPattern = false;
        String inputFileName = getFileName();
        if (((inputFileName != null) && (!inputFileName.equals("")) &&
                ((inputFileName.indexOf("%d") > -1) ||
                (inputFileName.indexOf("%t") > -1) ||
                (inputFileName.indexOf("%u") > -1) ||
                ((inputFileName.indexOf("%{") > -1) && (inputFileName.indexOf("}") > -1))))) {
            return true;
        }
        return isPattern;
    }

    private boolean anyPatternSpecified() {
        boolean anyPattern = false;
        String inputFileName = getFileName();
        if (((inputFileName != null) && (!inputFileName.equals("")) &&
                (inputFileName.indexOf("%") > -1))) {
            return true;
        }
        return anyPattern;
    }

    private boolean isDirectoryRelative(Object source) {
        boolean valid = true;
        if (source == directoryTextField) {
                String dir = getDirectory();
                if (dir == null) {
                    return false;
                }
                File fileDir = new File(dir);
                if (fileDir.isAbsolute()) {
                    return false;
                }
        } else if (source == inputArchiveDirectoryTextField) {
            if (getInputArchiveRelative()) {
                // make sure directory is filled in and must be relative
                String dir = getInputArchiveDirectory();
                File fileDir = new File(dir);
                if ((dir == null) || (fileDir.isAbsolute())) {
                    return false;
                }
            }
        } else if (source == inputArchiveRelativeToPollingDir) {
            if (inputArchiveRelativeToPollingDir.isSelected()) {
                // make sure directory is filled in and must be relative
                String dir = getInputArchiveDirectory();                
                if (dir == null) {
                    return false;
                }
                File fileDir = new File(dir);
                if (fileDir.isAbsolute()) {
                    return false;
                }
            }

        }
        return valid;
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

        pathRelativeToComboBox.removeItemListener(mItemListener);
        inputArchiveRelativeToPollingDir.removeItemListener(mItemListener);
        inputArchiveBox.removeItemListener(mItemListener);
        operationNameComboBox.removeItemListener(mItemListener);
        
        directoryTextField.removeActionListener(mActionListener);
        browseButton.removeActionListener(mActionListener);
        inputFileNameTextField.removeActionListener(mActionListener);
        inputArchiveButton.removeActionListener(mActionListener);
        inputArchiveDirectoryTextField.removeActionListener(mActionListener);
        enableArchiveDetailsBtn.removeActionListener(mActionListener);
        
        inputMultipleRecordBox.removeItemListener(mItemListener);        
        maxBytesPerRecordBox.removeItemListener(mItemListener);
        
        inputFileNameTextField.getDocument().removeDocumentListener(mDocumentListener);
        directoryTextField.getDocument().removeDocumentListener(mDocumentListener);
        inputPollingTfld.getDocument().removeDocumentListener(mDocumentListener);
        maxBytesPeRecordTfld.getDocument().removeDocumentListener(mDocumentListener);        

        pathRelativeToComboBox.removeAllItems();
        pathRelativeToComboBox.addItem(FileConstants.NOT_SET);
        pathRelativeToComboBox.addItem(FileConstants.USER_HOME);
//        pathRelativeToComboBox.addItem(FileConstants.CURRENT_WORKING_DIR);
        pathRelativeToComboBox.addItem(FileConstants.DEFAULT_SYSTEM_TEMP_DIR);
        
        inputDelimiterBox.removeAllItems();
        inputDelimiterBox.addItem(FileConstants.DELIM_LINE_FEED); 
        
        servicePortComboBox.setEnabled(false);
        portTypeComboBox.setEditable(false);
        servicePortComboBox.removeAllItems();
        bindingNameComboBox.removeAllItems();
        portTypeComboBox.removeAllItems();
        operationNameComboBox.removeAllItems();
        inputPartComboBox.removeAllItems();                   

        mType = null;
        mElement = null;
    }

    private void initDescriptionPanel() {
        descPanel = new DescriptionPanel();
        java.awt.GridBagConstraints gridBagConstraints =
                new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(descPanel, gridBagConstraints);
    }

    private void updateDescriptionArea(FocusEvent evt) {
        descPanel.setText("");
        descPanelArchivePanel.setText("");

        // The image must first be wrapped in a style
//        Style style = mDoc.addStyle("StyleName", null);
//        StyleConstants.setIcon(style, mCASAImg);
        String[] desc = null;
        boolean casaEdited = false;

        if (evt.getSource() == directoryTextField) {
            desc = new String[]{"File Directory\n\n",
                    mBundle.getString("DESC_Attribute_fileDirectory")};
            casaEdited = true;
        } else if (evt.getSource() == inputFileNameTextField) {
            desc = new String[]{"File Name\n\n",
                    mBundle.getString("DESC_Attribute_fileName")};
//        } else if (evt.getSource() == isRelativePathBox) {
//            desc = new String[]{"Is Relative Path\n\n",
//                    mBundle.getString("DESC_Attribute_relativePath")};
//            casaEdited = true;
        } else if (evt.getSource() == pathRelativeToComboBox) {
            desc = new String[]{"Path Relative To\n\n",
                    mBundle.getString("DESC_Attribute_pathRelativeTo")};
            casaEdited = true;
//        } else if (evt.getSource() == lockNameTextField) {
//            desc = new String[]{"Lock Name\n\n",
//                    mBundle.getString("DESC_Attribute_lockName")};
//            casaEdited = true;
//        } else if (evt.getSource() == outputFileNameTextField) {
//            desc = new String[]{"File Name\n\n",
//                    mBundle.getString("DESC_Attribute_fileName")};
//        } else if (evt.getSource() == workAreaTextField) {
//            desc = new String[]{"Processing Area\n\n",
//                    mBundle.getString("DESC_Attribute_workArea")};
//            casaEdited = true;
//        } else if (evt.getSource() == seqNameTextField) {
//            desc = new String[]{"Sequential Number FileName\n\n",
//                    mBundle.getString("DESC_Attribute_seqName")};
//            casaEdited = true;
//        } else if (evt.getSource() == inputFileIsPatternBox) {
//            desc = new String[]{"Input File Is Pattern\n\n",
//                    mBundle.getString("DESC_Attribute_fileNameIsPattern")};
//        } else if (evt.getSource() == outputFileIsPatternBox) {
//            desc = new String[]{"Output File Is Pattern\n\n",
//                    mBundle.getString("DESC_Attribute_fileNameIsPattern")};
        } else if (evt.getSource() == inputPollingTfld) {
            desc = new String[]{"Polling Interval\n\n",
                    mBundle.getString("DESC_Attribute_pollingInterval")};
//        } else if (evt.getSource() == inputFileTypeComboBox) {
//            desc = new String[]{"File Type\n\n",
//                    mBundle.getString("DESC_Attribute_fileType")};
//        } else if (evt.getSource() == outputFileTypeComboBox) {
//            desc = new String[]{"File Type\n\n",
//                    mBundle.getString("DESC_Attribute_fileType")};
        } else if (evt.getSource() == inputArchiveBox) {
            desc = new String[]{"Is Archive\n\n",
                    mBundle.getString("DESC_Attribute_archive")};
        } else if (evt.getSource() == inputMultipleRecordBox) {
            desc = new String[]{"Multi Records per File\n\n",
                    mBundle.getString("DESC_Attribute_multipleRecordsPerFile")};
        } else if (evt.getSource() == maxBytesPeRecordTfld) {
            desc = new String[]{"Max Bytes Per Record\n\n",
                    mBundle.getString("DESC_Attribute_maxBytesPerRecord")};
        } else if (evt.getSource() == fixedLengthTF) {
            desc = new String[]{"Max Bytes Per Record\n\n",
                    mBundle.getString("DESC_Attribute_maxBytesPerRecord")};
        }  else if (evt.getSource() == ((ComboBoxEditor)inputDelimiterBox.getEditor()).getEditorComponent()) {
            desc = new String[]{"Record Delimiter\n\n",
                    mBundle.getString("DESC_Attribute_recordDelimiter")};
        } else if (evt.getSource() == maxBytesPerRecordBox) {
            desc = new String[]{"Max Bytes Per Record\n\n",
                    mBundle.getString("DESC_Attribute_maxBytesPerRecord")};
        } else if (evt.getSource() == inputFileRegExBox) {
            desc = new String[]{"Regular Expression Pattern\n\n",
                    mBundle.getString("DESC_Attribute_fileNameIsRegex")};
        } else if (evt.getSource() == pollRecursiveCheckBox ) {
            desc = new String[]{"Poll Recursive\n\n",
                    mBundle.getString("DESC_Attribute_pollInputDirRecursively")};
        } else if (evt.getSource() == pollRecursiveExcludeText ) {
            desc = new String[]{"Poll Recursive Exclude\n\n",
                    mBundle.getString("DESC_Attribute_pollRecursivelyExclude")};
        }
        
        if (desc != null) {
//            try {
//                mDoc.insertString(mDoc.getLength(), desc[0],
//                        mDoc.getStyle(mStyles[0]));
//                mDoc.insertString(mDoc.getLength(), desc[1],
//                        mDoc.getStyle(mStyles[1]));
                String htmlStr = "<html><b>" + desc[0] + "</b></html>" + desc[1];
                // Insert the image
                if (casaEdited) {
//                    mDoc.insertString(mDoc.getLength(), "\n",
//                            mDoc.getStyle(mStyles[1]));
//                    mDoc.insertString(mDoc.getLength(), "ignored text", style);
//                    mDoc.insertString(mDoc.getLength(), "  " + NbBundle.
//                            getMessage(FileBindingConfigurationPanel.class,
//                            "FileBindingConfigurationPanel.CASA_EDITED"),
//                            mDoc.getStyle(mStyles[1]));
                }
                descPanel.setText(desc[0], desc[1]);
//                descriptionTextPane.setCaretPosition(0);
//            } catch(BadLocationException ble) {
//                mLogger.log(Level.FINER, ble.getMessage());
//            }
            return;
        }

        if (evt.getSource() == inputArchiveDirectoryTextField) {
            desc = new String[]{"Archive Directory\n\n",
                    mBundle.getString("DESC_Attribute_archiveDirectory")};
        } else if (evt.getSource() == inputArchiveRelativeToPollingDir) {
            desc = new String[]{"Archive Directory Is Relative\n\n",
                    mBundle.getString("DESC_Attribute_archiveDirIsRelative")};
        }

        if (desc != null) {
            descPanelArchivePanel.setText(desc[0], desc[1]);
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
        
        pathRelativeToComboBox.addItemListener(mItemListener);
        inputArchiveRelativeToPollingDir.addItemListener(mItemListener);
        inputArchiveBox.addItemListener(mItemListener);
        operationNameComboBox.addItemListener(mItemListener);
        inputMultipleRecordBox.addItemListener(mItemListener);
        maxBytesPerRecordBox.addItemListener(mItemListener);
        directoryTextField.addActionListener(mActionListener);
        inputFileNameTextField.addActionListener(mActionListener);
        browseButton.addActionListener(mActionListener);
        inputArchiveButton.addActionListener(mActionListener);
        inputArchiveDirectoryTextField.addActionListener(mActionListener);
        enableArchiveDetailsBtn.addActionListener(mActionListener);
        inputFileNameTextField.getDocument().addDocumentListener(mDocumentListener);
        directoryTextField.getDocument().addDocumentListener(mDocumentListener);
        inputPollingTfld.getDocument().addDocumentListener(mDocumentListener);
        maxBytesPeRecordTfld.getDocument().addDocumentListener(mDocumentListener);        
    }
    
    private void populateView(WSDLComponent component) {
        if (component != null) {
            if (component instanceof FileAddress) {
                populateFileAddress((FileAddress) component);
            } else if (component instanceof FileBinding) {
                populateFileBinding((FileBinding) component, null);
            } else if (component instanceof Port) {
                Collection<FileAddress> address = ((Port) component).
                        getExtensibilityElements(FileAddress.class);
                if (!address.isEmpty()) {
                    populateFileAddress(address.iterator().next());
                }
            } else if (component instanceof FileMessage) {
                Object obj = ((FileMessage)component).getParent();
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
                    Collection<FileBinding> bindings = parentBinding.
                            getExtensibilityElements(FileBinding.class);
                    if (!bindings.isEmpty()) {
                        populateFileBinding(bindings.iterator().next(), null);
// TODO
//                        bindingNameComboBox.
//                                setSelectedItem(parentBinding.getName());
                    }
                }
            } else if (component instanceof FileOperation) {
                Object obj = ((FileOperation)component).getParent();
                if (obj instanceof BindingOperation) {
                    Binding parentBinding = (Binding)
                            ((BindingOperation)obj).getParent();
                    Collection<FileBinding> bindings = parentBinding.
                            getExtensibilityElements(FileBinding.class);
                    if (!bindings.isEmpty()) {
                        populateFileBinding(bindings.iterator().next(), null);
// TODO
//                        bindingNameComboBox.setSelectedItem(
//                                parentBinding.getName());
                    }
                }
            }
// TODO
//            populateDescriptionAndTooltip();
        }
    }

    private void populateFileAddress(FileAddress fileAddress) {
        Port port = (Port) fileAddress.getParent();
        if (port.getBinding() != null) {
            Binding binding = port.getBinding().get();
            Collection<FileBinding> bindings = binding.
                    getExtensibilityElements(FileBinding.class);
// TODO
//            servicePortComboBox.setEnabled(false);
//            servicePortComboBox.setSelectedItem(port.getName());
            if (!bindings.isEmpty()) {
                populateFileBinding(bindings.iterator().next(), fileAddress);
// TODO
//                bindingNameComboBox.setSelectedItem(binding.getName());
            }
            // from Port, need to disable binding box as 1:1 relationship
// TODO
//            bindingNameComboBox.setEditable(false);
//            bindingNameComboBox.setEnabled(false);
        }
    }

    private void populateFileBinding(FileBinding fileBinding,
            FileAddress fileAddress) {
        if (fileAddress == null) {
// TODO
//            servicePortComboBox.setEnabled(true);
            fileAddress = getFileAddress(fileBinding);
        }
        if (fileAddress == null) {
            return;
        }
        Port port = (Port) fileAddress.getParent();

        // need to populate with all service ports that uses this binding
        populateListOfPorts(fileBinding);
// TODO
//        servicePortComboBox.setSelectedItem(port);

        // from Binding, need to allow changing of Port
// TODO
//        bindingNameComboBox.setEditable(false);
//        bindingNameComboBox.setEnabled(false);

        updateServiceView(fileAddress);
        if (fileBinding != null) {
            populateListOfBindings(fileBinding);
            populateListOfPortTypes(fileBinding);
            Binding binding = (Binding) fileBinding.getParent();

            bindingNameComboBox.setSelectedItem(binding.getName());
            NamedComponentReference<PortType> pType = binding.getType();
            PortType portType = pType.get();
            portTypeComboBox.setSelectedItem(portType.getName());

            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            populateOperations(bindingOperations);

            if (operationNameComboBox.getItemCount() > 0) {
                operationNameComboBox.setSelectedIndex(0);
            }

            if ((bindingOperations != null) && (bindingOperations.size() > 0)) {
                BindingOperation bop = getBindingOperation(bindingOperations);
                if (binding != null) {
                    FileMessage inputMessage = getInputFileMessage(binding,
                            bop.getName());
                    updateInputMessageView(binding, inputMessage);

                }
            }

        }
    }

    private void populateListOfPortTypes(FileBinding fileBinding) {
        if ((fileBinding != null) && (fileBinding.getParent() != null)) {
            Binding parentBinding = (Binding) fileBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<PortType> portTypes = defs.getPortTypes().iterator();
            List<PortType> filePortTypes = null;
            while (portTypes.hasNext()) {
                PortType portType = portTypes.next();
// TODO
//                if (!portTypeExists(portType.getName())) {
//
//                    portTypeComboBox.addItem(portType.getName());
//                }
            }
        }
    }

// TODO
//    private boolean portTypeExists(String portTypeName) {
//        boolean exists = false;
//        int count = portTypeComboBox.getItemCount();
//        for (int i = 0; i < count; i++) {
//            String name = (String) portTypeComboBox.getItemAt(i);
//            if (portTypeName.equals(name)) {
//                exists = true;
//                break;
//            }
//        }
//        return exists;
//    }

    private PortType getPortType(String bindingName, FileAddress fileAddress) {
        if ((fileAddress != null) && (fileAddress.getParent() != null)) {
            Port parentPort = (Port) fileAddress.getParent();
            Service parentService = (Service) parentPort.getParent();
            Definitions defs = (Definitions) parentService.getParent();
            Iterator<Binding> bindings = defs.getBindings().iterator();
            List<FileBinding> fileBindings = null;
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

    private void populateListOfBindings(FileBinding fileBinding) {
        if ((fileBinding != null) && (fileBinding.getParent() != null)) {
            Binding parentBinding = (Binding) fileBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<Binding> bindings = defs.getBindings().iterator();
            List<FileBinding> fileBindings = null;

            while (bindings.hasNext()) {
                Binding binding = bindings.next();
                if (binding.getType() == null
                        || binding.getType().get() == null) {
                    continue;
                }

                fileBindings = binding.
                        getExtensibilityElements(FileBinding.class);
                if (fileBindings != null) {
                    Iterator iter = fileBindings.iterator();
                    while (iter.hasNext()) {
                        FileBinding b = (FileBinding) iter.next();
                        Binding fBinding = (Binding) b.getParent();
// TODO
//                        bindingNameComboBox.addItem(fBinding.getName());
                    }
                }
            }
        }
    }

    private void populateListOfPorts(FileBinding fileBinding) {
            Vector<Port> portV = new Vector<Port>();

        if ((fileBinding != null) && (fileBinding.getParent() != null)) {
            Binding parentBinding = (Binding) fileBinding.getParent();
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
                            portV.add(port);
                        }
                    }
                }
            }
        }
// TODO
//        servicePortComboBox.setModel(new DefaultComboBoxModel(portV));
//        servicePortComboBox.setRenderer(new PortCellRenderer());

    }

    private BindingOperation getBindingOperation(Collection bindingOps) {
        Iterator iter = bindingOps.iterator();
        while (iter.hasNext()) {
            BindingOperation bop = (BindingOperation) iter.next();
            return bop;
        }
        return null;
    }

    private void populateOperations(Collection bindingOps) {
        Iterator iter = bindingOps.iterator();
        while (iter.hasNext()) {
            BindingOperation bop = (BindingOperation) iter.next();
            operationNameComboBox.addItem(bop.getName());
        }
    }

    private void populateArchiveSection(FileMessage inputFileMessage,
            boolean resetArchiveBox) {
        mInputFileMessage = inputFileMessage;
        if (inputFileMessage != null) {
            if (resetArchiveBox) {
                if (inputFileMessage.getArchiveEnabled()) {
                    inputArchiveBox.setSelected(true);
                } else {
                    inputArchiveBox.setSelected(false);
                }            
            }

            inputArchiveBox.setToolTipText(
                    mBundle.getString("DESC_Attribute_archive"));       //NOI18N

            inputArchiveDirectoryTextField.setText(inputFileMessage.
                    getArchiveDirectory());
            inputArchiveDirectoryTextField.setToolTipText(
                    mBundle.getString("DESC_Attribute_archiveDirectory"));//NOI18N   
            
            if (inputFileMessage.getArchiveDirIsRelative()) {
                inputArchiveRelativeToPollingDir.setSelected(true);
            } else {
                inputArchiveRelativeToPollingDir.setSelected(false);
            }             
            
        }
    }
    
    FileAddress getFileAddress(FileBinding fileBinding) {
        FileAddress fileAddress = null;
        if ((fileBinding != null) && (fileBinding.getParent() != null)) {
            Binding parentBinding = (Binding) fileBinding.getParent();
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
                            Iterator<FileAddress> fileAddresses = port.
                                    getExtensibilityElements(FileAddress.class).
                                    iterator();
                            // 1 fileaddress for 1 binding
                            while (fileAddresses.hasNext()) {
                                return fileAddresses.next();
                            }
                        }
                    }
                }
            }
        }
        return fileAddress;
    }

    private FileMessage getInputFileMessage(Binding binding,
            String selectedOperation) {
        FileMessage inputFileMessage = null;
        if (binding != null) {
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(selectedOperation)) {
                    BindingInput bi = bop.getBindingInput();
                    List<FileMessage> inputFileMessages =
                            bi.getExtensibilityElements(FileMessage.class);
                    if (inputFileMessages.size() > 0) {
                        inputFileMessage = inputFileMessages.get(0);
                        break;
                    }
                }
            }
        }
        return inputFileMessage;
    }

    private void cleanUp() {     
        mQName = null;
        mWsdlComponent = null;
        mInputFileMessage = null;
        
    }

    private void updateInputMessageView(Binding binding,
            FileMessage inputFileMessage) {
        if (inputFileMessage != null) {
            inputFileNameTextField.setText(inputFileMessage.getFileName());
            inputFileNameTextField.setToolTipText(
                    mBundle.getString("DESC_Attribute_fileName"));      //NOI18N     
            String pollStr = inputFileMessage.getAttribute(FileMessage.ATTR_POLLING_INTERVAL);
            if (pollStr != null) {
                inputPollingTfld.setText(pollStr);
            }

            String isRegExStr = inputFileMessage.
                    getAttribute(FileMessage.ATTR_FILE_NAME_IS_REGEX);
            boolean isRegEx = false;
            if ((isRegExStr != null)
                    && (FileConstants.stringValueIsTrue(isRegExStr))) {
                isRegEx = true;
            }
            inputFileRegExBox.setSelected(isRegEx);
            
            populateArchiveSection(inputFileMessage, true);
            
            BindingInput input = (BindingInput) inputFileMessage.getParent();
            Collection<Part> parts = WSDLUtils.getParts(input);
            Vector<String> vect = new Vector<String>();
            //vect.add("");
            for (Part part : parts) {
                vect.add(part.getName());
            }
 
            // BASED on Message Type selected, need to check if Part selected has a type            
            inputPartComboBox.setModel(new DefaultComboBoxModel(vect));
            String part = inputFileMessage.getPart();
            if (part == null) {
                // per BC developer, will preselect 1st item
                if (inputPartComboBox.getItemCount() > 0) {
                    inputPartComboBox.setSelectedIndex(0);
                    part = (String) inputPartComboBox.getSelectedItem();
                }
            } else {
                inputPartComboBox.setSelectedItem(part);
            }
            
            // check if Part selected has a type and set correct msg type toggle
            // get the Message
            Operation op = FileUtilities.getOperation(binding,
                    operationNameComboBox.getSelectedItem().toString());
            if (op != null) {
                Input inputOp = op.getInput();
                NamedComponentReference<Message> messageIn = inputOp.getMessage();
                if (inputPartComboBox.getSelectedItem() != null) {
                    if (part != null) {
                        mPart = FileUtilities.getMessagePart(part, messageIn.get());
                    }
                }                                
            }  
            
            mMessageTypePanel.populateView(mWsdlComponent, mPart, 
                    inputFileMessage, mProject, 
                    operationNameComboBox.getSelectedItem().toString());
            updateInboundRecordInfo(inputFileMessage);
            inputPartComboBox.setToolTipText(
                    mBundle.getString("DESC_Attribute_part"));          //NOI18N

        } else {
            // null out view
            inputFileNameTextField.setText("");
            mMessageTypePanel.populateView(mWsdlComponent, mPart, 
                    inputFileMessage, null, null);
            inputArchiveBox.setSelected(false);
            inputArchiveDirectoryTextField.setText("");
            inputMultipleRecordBox.setSelected(false);
            inputFileRegExBox.setSelected(false);
            maxBytesPeRecordTfld.setText("1"); 
                  
        }
        updateArchiveDetails();
    }

    private void updateServiceView(FileAddress fileAddress) {
        if (fileAddress != null) {
            directoryTextField.setText(fileAddress.
                    getAttribute(FileAddress.ATTR_FILE_ADDRESS));
            directoryTextField.setToolTipText(
                    mBundle.getString("DESC_Attribute_fileDirectory")); //NOI18N

            pollRecursiveCheckBox.setSelected(fileAddress.getRecursive());
            pollRecursiveCheckBox.setToolTipText(
                    mBundle.getString("DESC_Attribute_pollInputDirRecursively"));//NOI18N
            
            pollRecursiveExcludeText.setText(fileAddress.getRecursiveExclude());
            pollRecursiveExcludeText.setToolTipText(
                    mBundle.getString("DESC_Attribute_pollRecursivelyExclude"));//NOI18N

            if (fileAddress.getPathRelativeTo() == null) {
                pathRelativeToComboBox.setSelectedItem(FileConstants.NOT_SET);
            } else {
                pathRelativeToComboBox.setSelectedItem(fileAddress.
                        getPathRelativeTo());
            }
            pathRelativeToComboBox.setToolTipText(
                    mBundle.getString("DESC_Attribute_pathRelativeTo"));//NOI18N

        }
    }
    
    private void updateInboundRecordInfo(FileMessage fileMessage) {

        if (fileMessage.getRecordDelimiter() != null) {
            inputDelimiterBox.setSelectedItem(fileMessage.getRecordDelimiter());
        } else {
            inputDelimiterBox.setSelectedIndex(0);
        }

        if (fileMessage.getMultipleRecordsPerFile()) {
            inputMultipleRecordBox.setSelected(true);
        } else {
            inputMultipleRecordBox.setSelected(false);
        }

        String maxBytes = fileMessage.getAttribute(FileMessage.ATTR_MAX_BYTES_PER_RECORD);
        if (maxBytes != null) {
            maxBytesPerRecordBox.setSelected(true);
            maxBytesPeRecordTfld.setText(maxBytes);
        } else {
            maxBytesPeRecordTfld.setText("1");
        }
        
        updateEnableModeForMessageRecord();
    }    

    private void updateEnableModeForMessageRecord()  {        
        if (inputMultipleRecordBox.isSelected()) {
            inputDelimiterBox.setEnabled(true);
//            inputMaxBytesPerRecordSpinner.setEnabled(false);
//            maxBytesPerRecordBox.setEnabled(false);
            if (maxBytesPerRecordBox.isSelected()) {
                maxBytesPeRecordTfld.setEnabled(true);
                inputDelimiterBox.setEnabled(false);
            } else {
                maxBytesPeRecordTfld.setEnabled(false);
            }
//            ErrorPropagator.doFirePropertyChange(
//                    ExtensibilityElementConfigurationEditorComponent.
//                    PROPERTY_CLEAR_MESSAGES_EVT, null, "",
//                    mMessageTypePanel);          
        } else {
            inputDelimiterBox.setEnabled(false);
            maxBytesPerRecordBox.setEnabled(true);
            if (maxBytesPerRecordBox.isSelected()) {
                maxBytesPeRecordTfld.setEnabled(true);
//                if (getInputMaxBytesPerRecord() == null) {
//                    ErrorPropagator.doFirePropertyChange(
//                            ExtensibilityElementConfigurationEditorComponent.
//                            PROPERTY_ERROR_EVT, null,
//                            NbBundle.getMessage(FileBindingConfigurationPanel.class,
//                            "InboundMessagePanel.EMPTY_MAX_RECORD"),
//                            mMessageTypePanel);                    
//                }
            } else {
                maxBytesPeRecordTfld.setEnabled(false);
            }
        }                   
    }      
    
    private void updateArchiveDetails() {
        if (inputArchiveBox.isSelected()) {
            enableArchiveDetailsBtn.setEnabled(true);
        } else {
            enableArchiveDetailsBtn.setEnabled(false);
        }
    }

    private boolean validateFilePathExistence(String filePath) {
        boolean exists = true;
        if (filePath != null) {
            File file = new File(filePath);
            if ((file == null) || (!file.exists())) {
                ErrorPropagator.doFirePropertyChange(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_ERROR_EVT, null,
                        NbBundle.getMessage(FileBindingConfigurationPanel.class,
                        "FileBindingConfiguratonnPanel.FILE_NOT_FOUND"),
                        mMessageTypePanel);
            } else {
                ErrorPropagator.doFirePropertyChange(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_CLEAR_MESSAGES_EVT, null, "",
                        mMessageTypePanel);
            }
        }
        return exists;
    }

    private void createAndSetPartType(Part part, String javaPrimType) {
        if (part != null) {
            part.setType(part.createSchemaReference(
                    FileUtilities.getPrimitiveType("string"), GlobalType.class));
        }
    }
    
    private void initFileChooser() {
        if (this.directoryFileChooser != null) {
            this.directoryFileChooser.setFileSelectionMode(JFileChooser.
                    DIRECTORIES_ONLY);
            this.directoryFileChooser.setAcceptAllFileFilterUsed(false);
        }
    }

    private void showArchiveDetails() {
        if (mDetailsDlg == null) {
            mDetailsDlgDesc = new DialogDescriptor(
                    archivePopupPanel,
                    NbBundle.getMessage(FileBindingConfigurationPanel.class,
                    "FileBindingConfigurationPanel.AdvancedArchiveTitle"),
                    true, null);
            mDetailsDlg = DialogDisplayer.getDefault().
                    createDialog(mDetailsDlgDesc);
        }
        mDetailsDlg.setSize(archivePopupPanel.getPreferredSize());
        mDetailsDlg.setVisible(true);
        if (mDetailsDlgDesc.getValue() == DialogDescriptor.OK_OPTION) {
        } else {
            // reset values
            populateArchiveSection(mInputFileMessage, false);
        }

        // 
        handleArchivePathRelativeToComboBoxChange();      
        
        mDetailsDlg.setVisible(false);
    }


    private void handleItemStateChanged(ItemEvent evt) {
        if (evt.getSource() == pathRelativeToComboBox) {
            handlePathRelativeToComboBoxChange(evt);
        } else if (evt.getSource() == inputArchiveRelativeToPollingDir) {
            handleArchivePathRelativeToComboBoxChange();
        } else if (evt.getSource() == inputArchiveBox) {
            updateArchiveDetails();
        } else if (evt.getSource() == operationNameComboBox) {
            handleOperationNameComboBoxItemStateChanged(evt);            
        } else if ((evt.getSource() == inputMultipleRecordBox) ||
                   (evt.getSource() == maxBytesPerRecordBox)) {
            updateEnableModeForMessageRecord();            
        }
        
    }

    private void handlePathRelativeToComboBoxChange(ItemEvent evt) {
        FileError fileError = new FileError();
        boolean ok = true;
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            fileError = handlePathRelativeToComboBoxChange(true);
            if (ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_ERROR_EVT.equals(fileError.getErrorMode())) {
                return;
            }            
        }
        if (ok) {
            ErrorPropagator.doFirePropertyChange(
                    ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_CLEAR_MESSAGES_EVT, null, "",
                    mMessageTypePanel);
        }        
    }

    private FileError handlePathRelativeToComboBoxChange() {
        return handlePathRelativeToComboBoxChange(false);
    }
    
    private FileError handlePathRelativeToComboBoxChange(boolean fireEvent)  {
        FileError fileError = new FileError();    
        String directory = getDirectory();
        if ((getPathRelativeTo() != null) && ((directory == null) ||
                (directory.equals("")))) {
            fileError.setErrorMessage(NbBundle.getMessage(
                        FileBindingConfigurationPanel.class,
                        "FileBindingConfigurationPanel.FileDirectoryMustBeSet"));
            fileError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.
                         PROPERTY_ERROR_EVT);               
            if (fireEvent) {            
                ErrorPropagator.doFirePropertyChange(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_ERROR_EVT, null, NbBundle.getMessage(
                        FileBindingConfigurationPanel.class,
                        "FileBindingConfigurationPanel.FileDirectoryMustBeSet"),
                        mMessageTypePanel);
            }
        }

        if ((getPathRelativeTo() != null) &&
                        (!getPathRelativeTo().equals(FileConstants.NOT_SET))) {
            if (!isDirectoryRelative(directoryTextField)) {
                // make sure directory is filled in and must be relative
                fileError.setErrorMessage(NbBundle.getMessage(
                        FileBindingConfigurationPanel.class,
                            "FileBindingConfigurationPanel.directoryMustBeRelative.text"));
                fileError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.
                         PROPERTY_ERROR_EVT);                
                if (fireEvent) {                
                    ErrorPropagator.doFirePropertyChange(
                            ExtensibilityElementConfigurationEditorComponent.
                            PROPERTY_ERROR_EVT, null, NbBundle.getMessage(
                            FileBindingConfigurationPanel.class,
                            "FileBindingConfigurationPanel.directoryMustBeRelative.text"),
                            mMessageTypePanel);
                }
            }
        }
        return fileError;
    }

    private void handleArchivePathRelativeToComboBoxChange() {
        FileError fileError = new FileError();
        if (!isDirectoryRelative(inputArchiveRelativeToPollingDir)) {
            // make sure directory is filled in and must be relative
            fileError.setErrorMessage(NbBundle.getMessage(
                    FileBindingConfigurationPanel.class,
                    "FileBindingConfigurationPanel.inputArchiveDirectoryMustBeRelative.text"));
            fileError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_ERROR_EVT);
            ErrorPropagator.doFirePropertyChange(fileError.getErrorMode(), null,
                    fileError.getErrorMessage(), mMessageTypePanel, fileError);
            if (mDetailsDlgDesc != null) {
                mDetailsDlgDesc.setValid(false);
            }
        } else {
             ErrorPropagator.doFirePropertyChange(fileError.getErrorMessage(),
                     null, "", mMessageTypePanel, fileError);
            if (mDetailsDlgDesc != null) {
                mDetailsDlgDesc.setValid(true);
            }            
        }
    }

    private void handleActionPerformed(ActionEvent evt) {
        if (evt.getSource() == inputArchiveButton) {
            handleArchiveButtonActionPerformed();
        } else if (evt.getSource() == inputArchiveDirectoryTextField) {
            handleArchivePathRelativeToComboBoxChange();
        } else if (evt.getSource() == directoryTextField) {
            handlePathRelativeToComboBoxChange(true);
        } else if (evt.getSource() == inputFileNameTextField) {
            handleIsFilePattern();
        } else if (evt.getSource() == browseButton) {
            handleBrowseButtonActionPerformed();
        } else if (evt.getSource() == enableArchiveDetailsBtn) {
            showArchiveDetails();
        } 
    }
    
    private void handleArchiveButtonActionPerformed() {                                                   
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int retVal = directoryFileChooser.showDialog(mInstance, "Select");
                if (retVal == javax.swing.JFileChooser.APPROVE_OPTION) {
                    inputArchiveDirectoryTextField.setText(directoryFileChooser.
                            getSelectedFile().getAbsolutePath());
                    if (validateFilePathExistence(
                            inputArchiveDirectoryTextField.getText())) {                        
                        handleArchivePathRelativeToComboBoxChange();
                    }
                }
            }
        });
    }

    private void handleBrowseButtonActionPerformed() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int retVal = directoryFileChooser.showDialog(mInstance, "Select");
                if (retVal == javax.swing.JFileChooser.APPROVE_OPTION) {
                    directoryTextField.setText(directoryFileChooser.
                            getSelectedFile().getAbsolutePath());
                    handlePathRelativeToComboBoxChange(true);
                }
            }
        });
    }

    private void handleIsFilePattern() {
        if (anyPatternSpecified()) {
            if (!isValidPatternSpecified()) {
                // just a warning and should not prevent from proceeding
                firePropertyChange(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_WARNING_EVT, null, NbBundle.getMessage(
                        FileBindingConfigurationPanel.class,
                        "FileBindingConfigurationPanel.UnsupportedFileNamePattern"));
                return;
            }
        }
        firePropertyChange(
                ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_CLEAR_MESSAGES_EVT, null, "");

    }

    private void handleOperationNameComboBoxItemStateChanged(ItemEvent evt) {                                                       
        // TODO add your handling code here:
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            String selectedOperation = (String) operationNameComboBox.
                    getSelectedItem();
            if (mWsdlComponent != null)  {
                Binding binding = null;
                if (mWsdlComponent instanceof FileAddress) {
                    Port port = (Port) ((FileAddress) mWsdlComponent).getParent();
                    binding = port.getBinding().get();
                } else if (mWsdlComponent instanceof FileBinding) {
                    binding = (Binding) ((FileBinding) mWsdlComponent).getParent();
                } else if (mWsdlComponent instanceof FileMessage) {
                    Object obj = ((FileMessage)mWsdlComponent).getParent();
                    if (obj instanceof BindingInput) {
                        BindingOperation parentOp =
                                (BindingOperation) ((BindingInput) obj).getParent();
                        binding = (Binding) parentOp.getParent();
                    } else if (obj instanceof BindingOutput) {
                        BindingOperation parentOp = (BindingOperation)
                                ((BindingOutput) obj).getParent();
                        binding = (Binding) parentOp.getParent();
                    }
                } else if (mWsdlComponent instanceof FileOperation) {
                    Object obj = ((FileOperation)mWsdlComponent).getParent();
                    if (obj instanceof BindingOperation) {
                        binding = (Binding) ((BindingOperation)obj).getParent();
                    }
                } else if (mWsdlComponent instanceof Port) {
                    if (((Port)mWsdlComponent).getBinding() != null) {
                        binding = ((Port)mWsdlComponent).getBinding().get();
                    }
                }
                if (binding != null) {
                    FileMessage inputMessage = getInputFileMessage(binding,
                            selectedOperation);
                    updateInputMessageView(binding, inputMessage);
//                    FileMessage outputMessage = getOutputFileMessage(binding,
//                            selectedOperation);
//                    updateOutputMessageView(outputMessage);
//                    if (outputMessage == null) {
//                        updateOutputMessageViewFromInput(inputMessage);
//                    }
                }
            }
        }
    }  

    void showPopup(MouseEvent evt) {
        if (evt.isPopupTrigger()) {

        }
        
    }

    private void handlePropertyChangeEvent(PropertyChangeEvent evt) {
        boolean oldVal = true;//vt.getOldValue();
        boolean newVal = false;//evt.getNewValue();
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
    
    private void initCustomComponents() {
        mMessageTypePanel = new MessageTypePanel(mWsdlComponent, null, null, null);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        inboundPanel.add(mMessageTypePanel, gridBagConstraints);        
        
//        javax.swing.JPanel tmpPanel = new javax.swing.JPanel();
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 3;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
//        gridBagConstraints.weightx = 0.5;
//        gridBagConstraints.weighty = 0.5;
//        inboundPanel.add(tmpPanel, gridBagConstraints);
        
        inputPollingTfld.setMinimumSize(
                new java.awt.Dimension(inputArchiveButton.getPreferredSize().
                width - 2, inputPollingTfld.getPreferredSize().height));
        inputPollingTfld.setPreferredSize(inputPollingTfld.getMinimumSize());              
        
        setAccessibility();
    }
    
    private void setAccessibility() {
        this.getAccessibleContext().setAccessibleName(getName());
        this.getAccessibleContext().setAccessibleDescription(getName());
        browseButton.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_fileDirectory")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_fileDirectory")); // NOI18N
        inputArchiveButton.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_archive")); // NOI18N
        inputArchiveButton.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_archive")); // NOI18N
        inputArchiveDirectoryTextField.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_archiveDirectory")); // NOI18N
        inputArchiveDirectoryTextField.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_archiveDirectory")); // NOI18N        
        pathRelativeToComboBox.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_pathRelativeTo")); // NOI18N
        pathRelativeToComboBox.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_pathRelativeTo")); // NOI18N
        inputFileNameTextField.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_fileName")); // NOI18N
        inputFileNameTextField.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_fileName")); // NOI18N
        directoryTextField.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_fileDirectory")); // NOI18N
        directoryTextField.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_fileDirectory")); // NOI18N           
        maxBytesPeRecordTfld.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_maxBytesPerRecord")); // NOI18N
        maxBytesPeRecordTfld.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_maxBytesPerRecord")); // NOI18N                   
        inputArchiveRelativeToPollingDir.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_archiveDirIsRelative")); // NOI18N                   
        inputArchiveRelativeToPollingDir.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_archiveDirIsRelative")); // NOI18N                           
        enableArchiveDetailsBtn.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_archive")); // NOI18N                   
        enableArchiveDetailsBtn.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_archive")); // NOI18N                           
        
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
        buttonGroup2 = new javax.swing.ButtonGroup();
        messageRecordBtnGrp = new javax.swing.ButtonGroup();
        delimitedFixedBtnGrp = new javax.swing.ButtonGroup();
        directoryFileChooser = new javax.swing.JFileChooser();
        archivePopupPanel = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        enableArchivePanel = new javax.swing.JPanel();
        archiveDirLabel = new javax.swing.JLabel();
        inputArchiveDirectoryTextField = new javax.swing.JTextField();
        inputArchiveButton = new javax.swing.JButton();
        archiveSectionLab = new javax.swing.JLabel();
        archivePanelSep1 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        inputArchiveRelativeToPollingDir = new javax.swing.JCheckBox();
        descriptionArchivePanel = new javax.swing.JPanel();
        portBindingNameGeneralPanel = new javax.swing.JPanel();
        bindingNameLabel = new javax.swing.JLabel();
        bindingNameComboBox = new javax.swing.JComboBox();
        portTypeLabel = new javax.swing.JLabel();
        portTypeComboBox = new javax.swing.JComboBox();
        portNameLabel = new javax.swing.JLabel();
        servicePortComboBox = new javax.swing.JComboBox();
        servicesGeneralLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        operationNamePanel = new javax.swing.JPanel();
        serviceBindingOperationLabel = new javax.swing.JLabel();
        operationNameLabel = new javax.swing.JLabel();
        operationNameComboBox = new javax.swing.JComboBox();
        sep1General = new javax.swing.JSeparator();
        inputPartPanel = new javax.swing.JPanel();
        inputPartLabel = new javax.swing.JLabel();
        inputPartComboBox = new javax.swing.JComboBox();
        jSplitPane1 = new javax.swing.JSplitPane();
        inboundPanel = new javax.swing.JPanel();
        filePollingSectionPanel = new javax.swing.JPanel();
        filePollingLab = new javax.swing.JLabel();
        filePollingSep = new javax.swing.JSeparator();
        inputFileNameLabel = new javax.swing.JLabel();
        inputFileNameTextField = new javax.swing.JTextField();
        inputFileRegExBox = new javax.swing.JCheckBox();
        directoryLabImage = new javax.swing.JLabel();
        directoryLabel = new javax.swing.JLabel();
        directoryTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        pollingDirPanel = new javax.swing.JPanel();
        pathRelativeToComboBox = new javax.swing.JComboBox();
        pathRelativeToLabel = new javax.swing.JLabel();
        isRelToIcon = new javax.swing.JLabel();
        inputPollingIntervalLabel = new javax.swing.JLabel();
        pollingIntervalPanel = new javax.swing.JPanel();
        inputPollingTfld = new javax.swing.JTextField();
        msLab = new javax.swing.JLabel();
        inputArchiveBox = new javax.swing.JCheckBox();
        enableArchiveDetailsBtn = new javax.swing.JButton();
        pollRecursiveCheckBox = new javax.swing.JCheckBox();
        pollRecursiveExcludeLab = new javax.swing.JLabel();
        pollRecursiveExcludeText = new javax.swing.JTextField();
        pollRecursiveExcludeIconLab = new javax.swing.JLabel();
        messageRecordPanell = new javax.swing.JPanel();
        recordSectionLab = new javax.swing.JLabel();
        recordSectionSep = new javax.swing.JSeparator();
        inputMultipleRecordBox = new javax.swing.JCheckBox();
        delimitedLab = new javax.swing.JLabel();
        inputDelimiterBox = new javax.swing.JComboBox();
        maxBytesPerRecordBox = new javax.swing.JCheckBox();
        maxBytesPeRecordTfld = new javax.swing.JTextField();
        descriptionPanel = new javax.swing.JPanel();

        directoryFileChooser.setName("directoryFileChooser"); // NOI18N

        archivePopupPanel.setName("archivePopupPanel"); // NOI18N
        archivePopupPanel.setPreferredSize(new java.awt.Dimension(400, 230));
        archivePopupPanel.setLayout(new java.awt.BorderLayout());

        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setName("jSplitPane2"); // NOI18N

        enableArchivePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        enableArchivePanel.setName("enableArchivePanel"); // NOI18N
        enableArchivePanel.setLayout(new java.awt.GridBagLayout());

        archiveDirLabel.setLabelFor(inputArchiveDirectoryTextField);
        org.openide.awt.Mnemonics.setLocalizedText(archiveDirLabel, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.archiveDirLabel.text")); // NOI18N
        archiveDirLabel.setName("archiveDirLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        enableArchivePanel.add(archiveDirLabel, gridBagConstraints);

        inputArchiveDirectoryTextField.setText(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.inputArchiveDirectoryTextField.text")); // NOI18N
        inputArchiveDirectoryTextField.setToolTipText(mBundle.getString("DESC_Attribute_protectDirectory"));
        inputArchiveDirectoryTextField.setName("inputArchiveDirectoryTextField"); // NOI18N
        inputArchiveDirectoryTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                inputArchiveDirectoryTextFieldparameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                inputArchiveDirectoryTextFieldparameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        enableArchivePanel.add(inputArchiveDirectoryTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(inputArchiveButton, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.inputArchiveButton.text")); // NOI18N
        inputArchiveButton.setName("inputArchiveButton"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        enableArchivePanel.add(inputArchiveButton, gridBagConstraints);

        archiveSectionLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(archiveSectionLab, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.archiveSectionLab.text")); // NOI18N
        archiveSectionLab.setName("archiveSectionLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        enableArchivePanel.add(archiveSectionLab, gridBagConstraints);

        archivePanelSep1.setName("archivePanelSep1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 55, 0, 0);
        enableArchivePanel.add(archivePanelSep1, gridBagConstraints);

        jPanel1.setName("jPanel1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        enableArchivePanel.add(jPanel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(inputArchiveRelativeToPollingDir, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.inputArchiveRelativeToPollingDir.text_1")); // NOI18N
        inputArchiveRelativeToPollingDir.setMargin(new java.awt.Insets(2, 0, 2, 2));
        inputArchiveRelativeToPollingDir.setName("inputArchiveRelativeToPollingDir"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        enableArchivePanel.add(inputArchiveRelativeToPollingDir, gridBagConstraints);

        jSplitPane2.setTopComponent(enableArchivePanel);

        descriptionArchivePanel.setName("descriptionArchivePanel"); // NOI18N
        descriptionArchivePanel.setPreferredSize(new java.awt.Dimension(300, 50));
        descriptionArchivePanel.setLayout(new java.awt.BorderLayout());
        jSplitPane2.setBottomComponent(descriptionArchivePanel);
        descPanelArchivePanel = new DescriptionPanel();
        descriptionArchivePanel.add(descPanelArchivePanel, java.awt.BorderLayout.CENTER);

        archivePopupPanel.add(jSplitPane2, java.awt.BorderLayout.CENTER);

        portBindingNameGeneralPanel.setName("portBindingNameGeneralPanel"); // NOI18N
        portBindingNameGeneralPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(bindingNameLabel, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.bindingNameLabel.text")); // NOI18N
        bindingNameLabel.setName("bindingNameLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 5);
        portBindingNameGeneralPanel.add(bindingNameLabel, gridBagConstraints);

        bindingNameComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        bindingNameComboBox.setName("bindingNameComboBox"); // NOI18N
        bindingNameComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                bindingNameComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        portBindingNameGeneralPanel.add(bindingNameComboBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(portTypeLabel, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.portTypeLabel.text")); // NOI18N
        portTypeLabel.setName("portTypeLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 5);
        portBindingNameGeneralPanel.add(portTypeLabel, gridBagConstraints);

        portTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        portTypeComboBox.setName("portTypeComboBox"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        portBindingNameGeneralPanel.add(portTypeComboBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(portNameLabel, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.portNameLabel.text")); // NOI18N
        portNameLabel.setName("portNameLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 5);
        portBindingNameGeneralPanel.add(portNameLabel, gridBagConstraints);

        servicePortComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        servicePortComboBox.setName("servicePortComboBox"); // NOI18N
        servicePortComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                servicePortComboBoxItemStateChanged(evt);
            }
        });
        servicePortComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                servicePortComboBoxFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        portBindingNameGeneralPanel.add(servicePortComboBox, gridBagConstraints);

        servicesGeneralLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(servicesGeneralLabel, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.servicesGeneralLabel.text")); // NOI18N
        servicesGeneralLabel.setName("servicesGeneralLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        portBindingNameGeneralPanel.add(servicesGeneralLabel, gridBagConstraints);

        jSeparator1.setName("jSeparator1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 65, 0, 10);
        portBindingNameGeneralPanel.add(jSeparator1, gridBagConstraints);

        operationNamePanel.setName("operationNamePanel"); // NOI18N
        operationNamePanel.setLayout(new java.awt.GridBagLayout());

        serviceBindingOperationLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(serviceBindingOperationLabel, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.serviceBindingOperationLabel.text")); // NOI18N
        serviceBindingOperationLabel.setName("serviceBindingOperationLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 0, 0);
        operationNamePanel.add(serviceBindingOperationLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(operationNameLabel, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.operationNameLabel.text")); // NOI18N
        operationNameLabel.setName("operationNameLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        operationNamePanel.add(operationNameLabel, gridBagConstraints);

        operationNameComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        operationNameComboBox.setName("operationNameComboBox"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        operationNamePanel.add(operationNameComboBox, gridBagConstraints);

        sep1General.setName("sep1General"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 50, 0, 10);
        operationNamePanel.add(sep1General, gridBagConstraints);

        inputPartPanel.setName("inputPartPanel"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(inputPartLabel, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.inputPartLabel.text")); // NOI18N
        inputPartLabel.setName("inputPartLabel"); // NOI18N
        inputPartPanel.add(inputPartLabel);

        inputPartComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        inputPartComboBox.setName("inputPartComboBox"); // NOI18N
        inputPartComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                inputPartComboBoxparameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                inputPartComboBoxparameterFocusLost(evt);
            }
        });
        inputPartPanel.add(inputPartComboBox);

        setName("Form"); // NOI18N
        setPreferredSize(new java.awt.Dimension(400, 500));
        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(1.0);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        inboundPanel.setName("inboundPanel"); // NOI18N
        inboundPanel.setLayout(new java.awt.GridBagLayout());

        filePollingSectionPanel.setName("filePollingSectionPanel"); // NOI18N
        filePollingSectionPanel.setLayout(new java.awt.GridBagLayout());

        filePollingLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(filePollingLab, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.filePollingLab.text")); // NOI18N
        filePollingLab.setName("filePollingLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        filePollingSectionPanel.add(filePollingLab, gridBagConstraints);

        filePollingSep.setName("filePollingSep"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 65, 0, 0);
        filePollingSectionPanel.add(filePollingSep, gridBagConstraints);

        inputFileNameLabel.setLabelFor(inputFileNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(inputFileNameLabel, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.inputFileNameLabel.text")); // NOI18N
        inputFileNameLabel.setName("inputFileNameLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        filePollingSectionPanel.add(inputFileNameLabel, gridBagConstraints);

        inputFileNameTextField.setName("inputFileNameTextField"); // NOI18N
        inputFileNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                inputFileNameTextFieldparameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                inputFileNameTextFieldparameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        filePollingSectionPanel.add(inputFileNameTextField, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/wsdlextensions/file/resources/Bundle"); // NOI18N
        inputFileNameTextField.getAccessibleContext().setAccessibleName(bundle.getString("DESC_Attribute_fileName")); // NOI18N
        inputFileNameTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("DESC_Attribute_fileName")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(inputFileRegExBox, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.inputFileRegExBox.text_1")); // NOI18N
        inputFileRegExBox.setToolTipText(bundle.getString("DESC_Attribute_fileNameIsRegex"));
        inputFileRegExBox.setName("inputFileRegExBox"); // NOI18N
        inputFileRegExBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                inputFileRegExBoxFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        filePollingSectionPanel.add(inputFileRegExBox, gridBagConstraints);
        inputFileRegExBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.isRegexBox.AccessibleContext.accessibleDescription")); // NOI18N

        directoryLabImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/file/resources/service_composition_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(directoryLabImage, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.directoryLabImage.text")); // NOI18N
        directoryLabImage.setToolTipText(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.directoryLabImage.toolTipText")); // NOI18N
        directoryLabImage.setName("directoryLabImage"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        filePollingSectionPanel.add(directoryLabImage, gridBagConstraints);

        directoryLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        directoryLabel.setLabelFor(directoryTextField);
        org.openide.awt.Mnemonics.setLocalizedText(directoryLabel, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.directoryLabel.text")); // NOI18N
        directoryLabel.setName("directoryLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 40, 0, 10);
        filePollingSectionPanel.add(directoryLabel, gridBagConstraints);

        directoryTextField.setName("directoryTextField"); // NOI18N
        directoryTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                directoryTextFieldparameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                directoryTextFieldparameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        filePollingSectionPanel.add(directoryTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.browseButton.text")); // NOI18N
        browseButton.setName("browseButton"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        filePollingSectionPanel.add(browseButton, gridBagConstraints);

        pollingDirPanel.setName("pollingDirPanel"); // NOI18N
        pollingDirPanel.setLayout(new java.awt.GridBagLayout());

        pathRelativeToComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        pathRelativeToComboBox.setName("pathRelativeToComboBox"); // NOI18N
        pathRelativeToComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                pathRelativeToComboBoxparameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                pathRelativeToComboBoxparameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        pollingDirPanel.add(pathRelativeToComboBox, gridBagConstraints);

        pathRelativeToLabel.setLabelFor(pathRelativeToComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(pathRelativeToLabel, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.pathRelativeToLabel.text")); // NOI18N
        pathRelativeToLabel.setName("pathRelativeToLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        pollingDirPanel.add(pathRelativeToLabel, gridBagConstraints);

        isRelToIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/file/resources/service_composition_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(isRelToIcon, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.isRelToIcon.text")); // NOI18N
        isRelToIcon.setToolTipText(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.isRelToIcon.toolTipText")); // NOI18N
        isRelToIcon.setName("isRelToIcon"); // NOI18N
        isRelToIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                isRelToIconMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pollingDirPanel.add(isRelToIcon, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        filePollingSectionPanel.add(pollingDirPanel, gridBagConstraints);

        inputPollingIntervalLabel.setLabelFor(inputPollingTfld);
        org.openide.awt.Mnemonics.setLocalizedText(inputPollingIntervalLabel, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.inputPollingIntervalLabel.text")); // NOI18N
        inputPollingIntervalLabel.setName("inputPollingIntervalLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        filePollingSectionPanel.add(inputPollingIntervalLabel, gridBagConstraints);

        pollingIntervalPanel.setName("pollingIntervalPanel"); // NOI18N
        pollingIntervalPanel.setLayout(new java.awt.GridBagLayout());

        inputPollingTfld.setToolTipText(bundle.getString("DESC_Attribute_pollingInterval")); // NOI18N
        inputPollingTfld.setName("inputPollingTfld"); // NOI18N
        inputPollingTfld.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                inputPollingTfldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        pollingIntervalPanel.add(inputPollingTfld, gridBagConstraints);
        inputPollingTfld.getAccessibleContext().setAccessibleName(bundle.getString("DESC_Attribute_pollingInterval")); // NOI18N
        inputPollingTfld.getAccessibleContext().setAccessibleDescription(bundle.getString("DESC_Attribute_pollingInterval")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(msLab, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.msLab.text")); // NOI18N
        msLab.setName("msLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        pollingIntervalPanel.add(msLab, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        filePollingSectionPanel.add(pollingIntervalPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(inputArchiveBox, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.inputArchiveBox.text")); // NOI18N
        inputArchiveBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        inputArchiveBox.setName("inputArchiveBox"); // NOI18N
        inputArchiveBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                inputArchiveBoxparameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                inputArchiveBoxparameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        filePollingSectionPanel.add(inputArchiveBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(enableArchiveDetailsBtn, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.enableArchiveDetailsBtn.text")); // NOI18N
        enableArchiveDetailsBtn.setName("enableArchiveDetailsBtn"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        filePollingSectionPanel.add(enableArchiveDetailsBtn, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(pollRecursiveCheckBox, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.pollRecursiveCheckBox.text")); // NOI18N
        pollRecursiveCheckBox.setToolTipText("");
        pollRecursiveCheckBox.setName("pollRecursiveCheckBox"); // NOI18N
        pollRecursiveCheckBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                pollRecursiveChkBoxFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        filePollingSectionPanel.add(pollRecursiveCheckBox, gridBagConstraints);

        pollRecursiveExcludeLab.setLabelFor(pollRecursiveExcludeText);
        org.openide.awt.Mnemonics.setLocalizedText(pollRecursiveExcludeLab, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.pollRecursiveExcludeLab.text")); // NOI18N
        pollRecursiveExcludeLab.setName("pollRecursiveExcludeLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 30, 0, 0);
        filePollingSectionPanel.add(pollRecursiveExcludeLab, gridBagConstraints);

        pollRecursiveExcludeText.setText(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.pollRecursiveExcludeText.text_1")); // NOI18N
        pollRecursiveExcludeText.setName("pollRecursiveExcludeText"); // NOI18N
        pollRecursiveExcludeText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pollRecursiveExcludeTextActionPerformed(evt);
            }
        });
        pollRecursiveExcludeText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                pollRecursiveExcludeTextFldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        filePollingSectionPanel.add(pollRecursiveExcludeText, gridBagConstraints);

        pollRecursiveExcludeIconLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/file/resources/service_composition_16.png"))); // NOI18N
        pollRecursiveExcludeIconLab.setToolTipText(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.pollRecursiveExcludeconLab.tooltip.text")); // NOI18N
        pollRecursiveExcludeIconLab.setName("pollRecursiveExcludeIconLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        filePollingSectionPanel.add(pollRecursiveExcludeIconLab, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        inboundPanel.add(filePollingSectionPanel, gridBagConstraints);

        messageRecordPanell.setName("messageRecordPanell"); // NOI18N
        messageRecordPanell.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                messageRecordPanellMouseReleased(evt);
            }
        });
        messageRecordPanell.setLayout(new java.awt.GridBagLayout());

        recordSectionLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(recordSectionLab, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.recordSectionLab.text")); // NOI18N
        recordSectionLab.setName("recordSectionLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        messageRecordPanell.add(recordSectionLab, gridBagConstraints);

        recordSectionSep.setName("recordSectionSep"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 115, 0, 0);
        messageRecordPanell.add(recordSectionSep, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(inputMultipleRecordBox, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.inputMultipleRecordBox.text_1")); // NOI18N
        inputMultipleRecordBox.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/wsdlextensions/file/resources/Bundle").getString("DESC_Attribute_multipleRecordsPerFile"));
        inputMultipleRecordBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        inputMultipleRecordBox.setName("inputMultipleRecordBox"); // NOI18N
        inputMultipleRecordBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                inputMultipleRecordBoxFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        messageRecordPanell.add(inputMultipleRecordBox, gridBagConstraints);

        delimitedLab.setLabelFor(inputDelimiterBox);
        org.openide.awt.Mnemonics.setLocalizedText(delimitedLab, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.delimitedLab.text_1")); // NOI18N
        delimitedLab.setName("delimitedLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        messageRecordPanell.add(delimitedLab, gridBagConstraints);

        inputDelimiterBox.setEditable(true);
        inputDelimiterBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        inputDelimiterBox.setToolTipText(mBundle.getString("DESC_Attribute_recordDelimiter"));
        inputDelimiterBox.setName("inputDelimiterBox"); // NOI18N
        inputDelimiterBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                inputDelimiterBoxFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        messageRecordPanell.add(inputDelimiterBox, gridBagConstraints);
        Component inputDelimiterBoxTF = ((ComboBoxEditor)inputDelimiterBox.getEditor()).getEditorComponent();
        inputDelimiterBoxTF.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                updateDescriptionArea(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(maxBytesPerRecordBox, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.maxBytesPerRecordBox.text_1")); // NOI18N
        maxBytesPerRecordBox.setToolTipText(bundle.getString("DESC_Attribute_maxBytesPerRecord")); // NOI18N
        maxBytesPerRecordBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        maxBytesPerRecordBox.setName("maxBytesPerRecordBox"); // NOI18N
        maxBytesPerRecordBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                maxBytesPerRecordBoxFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        messageRecordPanell.add(maxBytesPerRecordBox, gridBagConstraints);

        maxBytesPeRecordTfld.setName("maxBytesPeRecordTfld"); // NOI18N
        maxBytesPeRecordTfld.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                maxBytesPeRecordTfldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        messageRecordPanell.add(maxBytesPeRecordTfld, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        inboundPanel.add(messageRecordPanell, gridBagConstraints);

        jSplitPane1.setTopComponent(inboundPanel);

        descriptionPanel.setMinimumSize(new java.awt.Dimension(400, 50));
        descriptionPanel.setName("descriptionPanel"); // NOI18N
        descriptionPanel.setPreferredSize(new java.awt.Dimension(400, 50));
        descriptionPanel.setLayout(new java.awt.BorderLayout());
        descPanel = new DescriptionPanel();
        descriptionPanel.add(descPanel, java.awt.BorderLayout.CENTER);
        jSplitPane1.setBottomComponent(descriptionPanel);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void directoryTextFieldparameterFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_directoryTextFieldparameterFocusGained
        // TODO add your handling code here:
        updateDescriptionArea(evt);
    }//GEN-LAST:event_directoryTextFieldparameterFocusGained

    private void directoryTextFieldparameterFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_directoryTextFieldparameterFocusLost
        // TODO add your handling code here:
        //    clearDescriptionArea();
    }//GEN-LAST:event_directoryTextFieldparameterFocusLost

    private void inputFileNameTextFieldparameterFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputFileNameTextFieldparameterFocusGained
        // TODO add your handling code here:
        updateDescriptionArea(evt);
    }//GEN-LAST:event_inputFileNameTextFieldparameterFocusGained

    private void inputFileNameTextFieldparameterFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputFileNameTextFieldparameterFocusLost
        // TODO add your handling code here:
        //    clearDescriptionArea();
    }//GEN-LAST:event_inputFileNameTextFieldparameterFocusLost

    private void pathRelativeToComboBoxparameterFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pathRelativeToComboBoxparameterFocusGained
        // TODO add your handling code here:
        updateDescriptionArea(evt);
    }//GEN-LAST:event_pathRelativeToComboBoxparameterFocusGained

    private void pathRelativeToComboBoxparameterFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pathRelativeToComboBoxparameterFocusLost
        // TODO add your handling code here:
        //    clearDescriptionArea();
    }//GEN-LAST:event_pathRelativeToComboBoxparameterFocusLost

    private void inputArchiveBoxparameterFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputArchiveBoxparameterFocusGained
        // TODO add your handling code here:
        updateDescriptionArea(evt);
    }//GEN-LAST:event_inputArchiveBoxparameterFocusGained

    private void inputArchiveBoxparameterFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputArchiveBoxparameterFocusLost
        // TODO add your handling code here:
        //    clearDescriptionArea();
    }//GEN-LAST:event_inputArchiveBoxparameterFocusLost

    private void inputArchiveDirectoryTextFieldparameterFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputArchiveDirectoryTextFieldparameterFocusGained
        // TODO add your handling code here:
        updateDescriptionArea(evt);
}//GEN-LAST:event_inputArchiveDirectoryTextFieldparameterFocusGained

    private void inputArchiveDirectoryTextFieldparameterFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputArchiveDirectoryTextFieldparameterFocusLost
        // TODO add your handling code here:
        //    clearDescriptionArea();
}//GEN-LAST:event_inputArchiveDirectoryTextFieldparameterFocusLost

    private void bindingNameComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_bindingNameComboBoxItemStateChanged
        // TODO add your handling code here:
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            String selectedBinding = (String) bindingNameComboBox.
                    getSelectedItem();
            // if binding name is changed, update the selected port type
            if (mWsdlComponent != null)  {
                if (mWsdlComponent instanceof FileAddress) {
                    PortType portType = getPortType(selectedBinding,
                            (FileAddress) mWsdlComponent);
                    if (portType != null)  {
                        portTypeComboBox.setSelectedItem(portType.getName());
                    }
                } else if (mWsdlComponent instanceof FileBinding) {
                    Binding parentBinding = (Binding)
                            ((FileBinding) mWsdlComponent).getParent();
                    NamedComponentReference<PortType> portType =
                            parentBinding.getType();
                    if ((portType != null) && (portType.get() != null)) {
                        portTypeComboBox.setSelectedItem(portType.get().getName());
                    }
                } else if (mWsdlComponent instanceof FileMessage) {
                    Object obj = ((FileMessage)mWsdlComponent).getParent();
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
                    if ((parentBinding != null) &&
                            (parentBinding.getType() != null) &&
                            (parentBinding.getType().get() == null)) {
                        NamedComponentReference<PortType> portType =
                                parentBinding.getType();
                        if (parentBinding.getName().equals(selectedBinding)) {
                            portTypeComboBox.setSelectedItem(portType.get().getName());
                        }
                    }
                } else if (mWsdlComponent instanceof FileOperation) {
                    Object obj = ((FileOperation)mWsdlComponent).getParent();
                    if (obj instanceof BindingOperation) {
                        Binding parentBinding = (Binding)
                                ((BindingOperation)obj).getParent();
                        Collection<FileBinding> bindings = parentBinding.
                                getExtensibilityElements(FileBinding.class);
                        if (!bindings.isEmpty()) {
                            populateFileBinding(bindings.iterator().next(), null);
                            bindingNameComboBox.setSelectedItem(
                                    parentBinding.getName());
                        }
                    }
                }
            }
        }
    }//GEN-LAST:event_bindingNameComboBoxItemStateChanged

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
                        Iterator<FileAddress> fileAddresses = selServicePort.
                                getExtensibilityElements(FileAddress.class).
                                iterator();
                        // 1 fileaddress for 1 binding
                        while (fileAddresses.hasNext()) {
                            updateServiceView(fileAddresses.next());
                            break;
                        }
                    }
                }
            }
        }
    }//GEN-LAST:event_servicePortComboBoxItemStateChanged

    private void servicePortComboBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_servicePortComboBoxFocusGained
        // TODO add your handling code here:
        updateDescriptionArea(evt);
    }//GEN-LAST:event_servicePortComboBoxFocusGained

    private void inputPartComboBoxparameterFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputPartComboBoxparameterFocusGained
        // TODO add your handling code here:
        updateDescriptionArea(evt);
    }//GEN-LAST:event_inputPartComboBoxparameterFocusGained

    private void inputPartComboBoxparameterFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputPartComboBoxparameterFocusLost
        // TODO add your handling code here:
        //    clearDescriptionArea();
    }//GEN-LAST:event_inputPartComboBoxparameterFocusLost

    private void isRelToIconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_isRelToIconMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_isRelToIconMouseClicked

private void inputDelimiterBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputDelimiterBoxFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_inputDelimiterBoxFocusGained

private void messageRecordPanellMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_messageRecordPanellMouseReleased
// TODO add your handling code here:
        showPopup(evt);
}//GEN-LAST:event_messageRecordPanellMouseReleased

private void inputMultipleRecordBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputMultipleRecordBoxFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_inputMultipleRecordBoxFocusGained

private void maxBytesPerRecordBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_maxBytesPerRecordBoxFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_maxBytesPerRecordBoxFocusGained

private void inputPollingTfldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputPollingTfldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_inputPollingTfldFocusGained

private void maxBytesPeRecordTfldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_maxBytesPeRecordTfldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_maxBytesPeRecordTfldFocusGained

private void inputFileRegExBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputFileRegExBoxFocusGained
    // TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_inputFileRegExBoxFocusGained

private void pollRecursiveExcludeTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pollRecursiveExcludeTextActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_pollRecursiveExcludeTextActionPerformed

private void pollRecursiveChkBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pollRecursiveChkBoxFocusGained
    // TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_pollRecursiveChkBoxFocusGained

private void pollRecursiveExcludeTextFldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pollRecursiveExcludeTextFldFocusGained
    // TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_pollRecursiveExcludeTextFldFocusGained


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel archiveDirLabel;
    private javax.swing.JSeparator archivePanelSep1;
    private javax.swing.JPanel archivePopupPanel;
    private javax.swing.JLabel archiveSectionLab;
    private javax.swing.JComboBox bindingNameComboBox;
    private javax.swing.JLabel bindingNameLabel;
    private javax.swing.JButton browseButton;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup delimitedFixedBtnGrp;
    private javax.swing.JLabel delimitedLab;
    private javax.swing.JPanel descriptionArchivePanel;
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JFileChooser directoryFileChooser;
    private javax.swing.JLabel directoryLabImage;
    private javax.swing.JLabel directoryLabel;
    private javax.swing.JTextField directoryTextField;
    private javax.swing.JButton enableArchiveDetailsBtn;
    private javax.swing.JPanel enableArchivePanel;
    private javax.swing.JLabel filePollingLab;
    private javax.swing.JPanel filePollingSectionPanel;
    private javax.swing.JSeparator filePollingSep;
    private javax.swing.JPanel inboundPanel;
    private javax.swing.JCheckBox inputArchiveBox;
    private javax.swing.JButton inputArchiveButton;
    private javax.swing.JTextField inputArchiveDirectoryTextField;
    private javax.swing.JCheckBox inputArchiveRelativeToPollingDir;
    private javax.swing.JComboBox inputDelimiterBox;
    private javax.swing.JLabel inputFileNameLabel;
    private javax.swing.JTextField inputFileNameTextField;
    private javax.swing.JCheckBox inputFileRegExBox;
    private javax.swing.ButtonGroup inputMessageTypeBtnGrp;
    private javax.swing.JCheckBox inputMultipleRecordBox;
    private javax.swing.JComboBox inputPartComboBox;
    private javax.swing.JLabel inputPartLabel;
    private javax.swing.JPanel inputPartPanel;
    private javax.swing.JLabel inputPollingIntervalLabel;
    private javax.swing.JTextField inputPollingTfld;
    private javax.swing.JLabel isRelToIcon;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTextField maxBytesPeRecordTfld;
    private javax.swing.JCheckBox maxBytesPerRecordBox;
    private javax.swing.ButtonGroup messageRecordBtnGrp;
    private javax.swing.JPanel messageRecordPanell;
    private javax.swing.JLabel msLab;
    private javax.swing.JComboBox operationNameComboBox;
    private javax.swing.JLabel operationNameLabel;
    private javax.swing.JPanel operationNamePanel;
    private javax.swing.JComboBox pathRelativeToComboBox;
    private javax.swing.JLabel pathRelativeToLabel;
    private javax.swing.JCheckBox pollRecursiveCheckBox;
    private javax.swing.JLabel pollRecursiveExcludeIconLab;
    private javax.swing.JLabel pollRecursiveExcludeLab;
    private javax.swing.JTextField pollRecursiveExcludeText;
    private javax.swing.JPanel pollingDirPanel;
    private javax.swing.JPanel pollingIntervalPanel;
    private javax.swing.JPanel portBindingNameGeneralPanel;
    private javax.swing.JLabel portNameLabel;
    private javax.swing.JComboBox portTypeComboBox;
    private javax.swing.JLabel portTypeLabel;
    private javax.swing.JLabel recordSectionLab;
    private javax.swing.JSeparator recordSectionSep;
    private javax.swing.JSeparator sep1General;
    private javax.swing.JLabel serviceBindingOperationLabel;
    private javax.swing.JComboBox servicePortComboBox;
    private javax.swing.JLabel servicesGeneralLabel;
    // End of variables declaration//GEN-END:variables

}
