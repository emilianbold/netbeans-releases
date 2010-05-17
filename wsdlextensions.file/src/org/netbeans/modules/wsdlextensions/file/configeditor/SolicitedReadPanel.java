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
 * OutboundMessagePanel.java
 *
 * Created on Jul 11, 2008, 2:14:05 AM
 */

package org.netbeans.modules.wsdlextensions.file.configeditor;

import java.awt.GridBagConstraints;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
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
import org.openide.util.NbBundle;

/**
 *
 * @author jalmero
 */
public class SolicitedReadPanel extends javax.swing.JPanel {

    /** the WSDL model to configure **/
    private WSDLComponent mWsdlComponent;

    /** QName **/
    private QName mQName;
    
    /**
     * Project associated with this wsdl
     */
    private Project mProject = null;
    private Part mPart = null;    
    private MessageTypePanel mMessageTypePanel = null; 

    /** resource bundle for file bc **/
    private ResourceBundle mBundle = ResourceBundle.getBundle(
            "org.netbeans.modules.wsdlextensions.file.resources.Bundle");

    private static final Logger mLogger = Logger.
            getLogger(FileBindingConfigurationPanel.class.getName());

    private DescriptionPanel descPanel = null;

    private MyItemListener mItemListener = null;
    private MyDocumentListener mDocumentListener = null;
    private MyActionListener mActionListener = null;
    private MyPopupAction mPopupAction = null;
    private JPopupMenu mPopupMenu = null;
    private JCheckBoxMenuItem mShowDescriptionMenu = null;

    /** Creates new form SolicitedReadPanel */
    public SolicitedReadPanel(QName qName, WSDLComponent component) {
        initComponents();
        initFileChooser(); 
        initCustomComponents();
        populateView(qName, component);
        mMessageTypePanel.enableForwardAsAttachment(true);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(OutboundMessagePanel.class,
                "SolicitedReadPanel.StepLabel");
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

    String getFetchRecursiveExclude() {
        return trimTextFieldInput(fetchRecursiveExcludePatternTextFld.getText());
    }

    boolean getFetchRecursive() {
        return fetchRecursiveChkBox.isSelected();
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
     * Return the file is pattern value for input message
     * @return boolean true if file is pattern; otherwise false
     */
    boolean getInputFileIsPattern() {
        return isValidPatternSpecified();
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
        //File file = new File(outputFileNameTextField.getText());
        //return file.getName();
        return outputFileNameTextField.getText();
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
     * Return the relative path value
     * @return String relative path
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
    boolean getOutputFileIsPattern() {
        return isValidPatternSpecified();
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
     * Return the use type for input message
     * @return String use type
     */
    String getOutputUseType() {
        return mMessageTypePanel.getInputUseType();
    }

    /**
     * Return the part value for the input message
     * return String part used
     */
    String getOutputPart() {
        return trimTextFieldInput((String) outputPartComboBox.getSelectedItem());
    }
    
    /**
     * Return true if payload is to be forwarded as an attachment
     * @return boolean 
     */
    boolean getForwardAsAttachment() {
        return mMessageTypePanel.getForwardAsAttachment();
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
        operationNameComboBox.addItemListener(mItemListener);

        directoryTextField.addActionListener(mActionListener);
        outputFileNameTextField.addActionListener(mActionListener);
        browseButton.addActionListener(mActionListener);

        outputFileNameTextField.getDocument().addDocumentListener(mDocumentListener);
        directoryTextField.getDocument().addDocumentListener(mDocumentListener);
        
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
                        bindingNameComboBox.
                                setSelectedItem(parentBinding.getName());
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
                        bindingNameComboBox.setSelectedItem(
                                parentBinding.getName());
                    }
                }
            }
        }     
    }

    private void populateFileAddress(FileAddress fileAddress) {
        Port port = (Port) fileAddress.getParent();
        if (port.getBinding() != null) {
            Binding binding = port.getBinding().get();
            Collection<FileBinding> bindings = binding.
                    getExtensibilityElements(FileBinding.class);
            servicePortComboBox.setEnabled(false);
            servicePortComboBox.setSelectedItem(port.getName());
            if (!bindings.isEmpty()) {
                populateFileBinding(bindings.iterator().next(), fileAddress);
                bindingNameComboBox.setSelectedItem(binding.getName());
            }
            // from Port, need to disable binding box as 1:1 relationship
            bindingNameComboBox.setEditable(false);
            bindingNameComboBox.setEnabled(false);
        }
    }

    private void populateFileBinding(FileBinding fileBinding,
            FileAddress fileAddress) {
        if (fileAddress == null) {
            servicePortComboBox.setEnabled(true);
            fileAddress = getFileAddress(fileBinding);
        }
        if (fileAddress == null) {
            return;
        }
        Port port = (Port) fileAddress.getParent();

        // need to populate with all service ports that uses this binding
        populateListOfPorts(fileBinding);
        servicePortComboBox.setSelectedItem(port);

        // from Binding, need to allow changing of Port
        bindingNameComboBox.setEditable(false);
        bindingNameComboBox.setEnabled(false);

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

            // select the 1st item since this is not a configurable param
            if (operationNameComboBox.getItemCount() > 0) {
                operationNameComboBox.setSelectedIndex(0);
            }
            
            if ((bindingOperations != null) && (bindingOperations.size() > 0)) {
                BindingOperation bop = getBindingOperation(bindingOperations);
                if (binding != null) {
                    
                    FileMessage inputMessage = getInputFileMessage(binding,
                            bop.getName());

                    // for solicited read, file:message will come from output
                    FileMessage outputMessage = getOutputFileMessage(binding,
                            bop.getName());
                    updateOutputMessageView(binding, outputMessage);
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
                if (!portTypeExists(portType.getName())) {
                    portTypeComboBox.addItem(portType.getName());
                }
            }
        }
    }

// TODO
    private boolean portTypeExists(String portTypeName) {
        boolean exists = false;
        int count = portTypeComboBox.getItemCount();
        for (int i = 0; i < count; i++) {
            String name = (String) portTypeComboBox.getItemAt(i);
            if (portTypeName.equals(name)) {
                exists = true;
                break;
            }
        }
        return exists;
    }

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
                        bindingNameComboBox.addItem(fBinding.getName());
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
        servicePortComboBox.setModel(new DefaultComboBoxModel(portV));
        servicePortComboBox.setRenderer(new PortCellRenderer());

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
    
    private FileMessage getOutputFileMessage(Binding binding,
            String selectedOperation) {
        FileMessage outputFileMessage = null;
        if (binding != null) {
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(selectedOperation)) {
                    BindingOutput bo = bop.getBindingOutput();
                    if (bo != null) {
                        List<FileMessage> outputFileMessages =
                                bo.getExtensibilityElements(FileMessage.class);
                        if (outputFileMessages.size() > 0) {
                            outputFileMessage = outputFileMessages.get(0);
                            break;
                        }
                    }
                }
            }
        }
        return outputFileMessage;
    }

    private void updateOutputMessageView(Binding binding,
            FileMessage outputFileMessage) {
        if (outputFileMessage != null) {
            outputFileNameTextField.setText(outputFileMessage.getFileName());
            outputFileNameTextField.setToolTipText(
                    mBundle.getString("DESC_Attribute_fileName"));      //NOI18N       
        
            BindingOutput output = (BindingOutput) outputFileMessage.getParent();
            Collection<Part> parts = WSDLUtils.getParts(output);
            Vector<String> vect = new Vector<String>();
            for (Part part : parts) {
                vect.add(part.getName());
            }
            outputPartComboBox.setModel(new DefaultComboBoxModel(vect));
            String part = outputFileMessage.getPart();
            if (part == null) {
                // per BC developer, will preselect 1st item
                if (outputPartComboBox.getItemCount() > 0) {
                    outputPartComboBox.setSelectedIndex(0);
                    part = (String) outputPartComboBox.getSelectedItem();
                }
            } else {
                outputPartComboBox.setSelectedItem(part);
            }

            // the newly exposed isRegex check box
            inputFileRegExBox.setSelected(outputFileMessage.getFileNameIsRegex());
            inputFileRegExBox.setToolTipText(mBundle.getString("DESC_Attribute_fileNameIsRegex"));
            
            // check if Part selected has a type and set correct msg type toggle
            // get the Message
            Operation op = FileUtilities.getOperation(binding,
                    operationNameComboBox.getSelectedItem().toString());
            if (op != null) {
                Output outputOp = op.getOutput();
                if (outputOp != null) {
                    NamedComponentReference<Message> messageIn = outputOp.getMessage();
                    if (outputPartComboBox.getSelectedItem() != null) {
                        mPart = FileUtilities.getMessagePart(part, messageIn.get());
                    }                                
                }
            }  
            
            mMessageTypePanel.populateView(mWsdlComponent, mPart, 
                    outputFileMessage, mProject, 
                    operationNameComboBox.getSelectedItem().toString());            
        } else {
            // null out view
            outputFileNameTextField.setText("");
            directoryTextField.setText("");
            inputFileRegExBox.setSelected(false);
        }
    }

    private void cleanUp() {
        // clean up listeners TODO
        // null out data TODO
        mQName = null;
        mWsdlComponent = null;
    }

    private void updateServiceView(FileAddress fileAddress) {
        if (fileAddress != null) {
            directoryTextField.setText(fileAddress.getFileDirectory());
            directoryTextField.setToolTipText(
                    mBundle.getString("DESC_Attribute_fileDirectory")); //NOI18N

            fetchRecursiveChkBox.setSelected(fileAddress.getRecursive());
            fetchRecursiveChkBox.setToolTipText(mBundle.getString("DESC_Attribute_pollInputDirRecursively"));//NOI18N

            fetchRecursiveExcludePatternTextFld.setText(fileAddress.getRecursiveExclude());
            fetchRecursiveExcludePatternTextFld.setToolTipText(mBundle.getString("DESC_Attribute_pollRecursivelyExclude"));//NOI18N

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
     * Set the Project associated with the wsdl for this panel
     * @param project
     */      
   void setProject(Project project) {
        mProject = project;
        mMessageTypePanel.setProject(project);
    }    
    
    /**
     * Return the selected part 
     * @return
     */     
    GlobalType getSelectedPartType() {
        return mMessageTypePanel.getSelectedPartType();
    }
    
    /**
     * Return the selected part from element
     * @return
     */      
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
    
    /**
     * Return true if the encoded type is enabled or not
     * @return boolean
     */
    public boolean isEncodedTypeEnabled() {
        return mMessageTypePanel.isEncodedTypeEnabled();
    }
    
    /**
     * Route the property change event to this panel
     */
    public void doFirePropertyChange(String name, Object oldValue, Object newValue) {           
        firePropertyChange(name, oldValue, 
                newValue);         
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
            ErrorPropagator.doFirePropertyChange(type, null, 
                    firstResult.getDescription(), mMessageTypePanel);
            return result;
        } else {
            ErrorPropagator.doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_CLEAR_MESSAGES_EVT, null, "", mMessageTypePanel);
            return true;
        }

    }

    private FileError validateFile() {
        return validateFile(false);
    }

    private FileError validateFile(boolean fireEvent) {
        FileError fileError = new FileError();
        if (outputFileNameTextField.getText().length() == 0) {
            fileError.setErrorMessage(NbBundle.getMessage(
                        FileBindingConfigurationPanel.class,
                        "InboundMessagePanel.FileNameMustBeSet"));
            fileError.setErrorMode(ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_ERROR_EVT);            
            if (fireEvent) {
                ErrorPropagator.doFirePropertyChange(
                    ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_ERROR_EVT, null, NbBundle.getMessage(
                    FileBindingConfigurationPanel.class,
                    "InboundMessagePanel.FileNameMustBeSet"), mMessageTypePanel);
            }            
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
                        "FileBindingConfigurationPanel.FileDirectoryMustBeSet"), 
                        mMessageTypePanel);
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
                    
                    // validate message section
                    fileError = mMessageTypePanel.validateMe(fireEvent);                                     
                }                           
            }
        }
        
        if (fireEvent) {
            ErrorPropagator.doFirePropertyChange(fileError.getErrorMode(), null,
                    fileError.getErrorMessage(), mMessageTypePanel);
        }
        
        return fileError;
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
        operationNameComboBox.removeItemListener(mItemListener);
        directoryTextField.removeActionListener(mActionListener);
        browseButton.removeActionListener(mActionListener);
        outputFileNameTextField.removeActionListener(mActionListener);
        outputFileNameTextField.getDocument().removeDocumentListener(mDocumentListener);
        directoryTextField.getDocument().removeDocumentListener(mDocumentListener);

        pathRelativeToComboBox.removeAllItems();
        pathRelativeToComboBox.addItem(FileConstants.NOT_SET);
        pathRelativeToComboBox.addItem(FileConstants.USER_HOME);
        pathRelativeToComboBox.addItem(FileConstants.CURRENT_WORKING_DIR);
        pathRelativeToComboBox.addItem(FileConstants.DEFAULT_SYSTEM_TEMP_DIR);

        servicePortComboBox.setEnabled(false);
        portTypeComboBox.setEditable(false);
        servicePortComboBox.removeAllItems();
        bindingNameComboBox.removeAllItems();
        portTypeComboBox.removeAllItems();
        operationNameComboBox.removeAllItems();

        outputPartComboBox.removeAllItems();        
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

        // The image must first be wrapped in a style
//        Style style = mDoc.addStyle("StyleName", null);
//        StyleConstants.setIcon(style, mCASAImg);
        String[] desc = null;
        boolean casaEdited = false;

        if (evt.getSource() == directoryTextField) {
            desc = new String[]{"File Directory\n\n",
                    mBundle.getString("DESC_Attribute_fileDirectory")};
            casaEdited = true;
        } else if (evt.getSource() == outputFileNameTextField) {
            desc = new String[]{"File Name\n\n",
                    mBundle.getString("DESC_Attribute_fileName")};
        } else if (evt.getSource() == inputFileRegExBox) {
            desc = new String[]{"File Name Is Regex\n\n",
                    mBundle.getString("DESC_Attribute_fileNameIsRegex")};
        } else if (evt.getSource() == fetchRecursiveChkBox ) {
            desc = new String[]{"Fetch Recursive\n\n",
                    mBundle.getString("DESC_Attribute_pollInputDirRecursively")};
        } else if (evt.getSource() == fetchRecursiveExcludePatternTextFld ) {
            desc = new String[]{"Fetch Recursive Exclude\n\n",
                    mBundle.getString("DESC_Attribute_pollRecursivelyExclude")};
        } else if (evt.getSource() == pathRelativeToComboBox) {
            desc = new String[]{"Path Relative To\n\n",
                    mBundle.getString("DESC_Attribute_pathRelativeTo")};
            casaEdited = true;
        }
        if (desc != null) {
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
                return;
//            try {
//                mDoc.insertString(mDoc.getLength(), desc[0],
//                        mDoc.getStyle(mStyles[0]));
//                mDoc.insertString(mDoc.getLength(), desc[1],
//                        mDoc.getStyle(mStyles[1]));
//
//                // Insert the image
//                if (casaEdited) {
//                    mDoc.insertString(mDoc.getLength(), "\n",
//                            mDoc.getStyle(mStyles[1]));
//                    mDoc.insertString(mDoc.getLength(), "ignored text", style);
//                    mDoc.insertString(mDoc.getLength(), "  " + NbBundle.
//                            getMessage(FileBindingConfigurationPanel.class,
//                            "FileBindingConfigurationPanel.CASA_EDITED"),
//                            mDoc.getStyle(mStyles[1]));
//                }
//
//                descriptionTextPane.setCaretPosition(0);
//            } catch(BadLocationException ble) {
//                mLogger.log(Level.FINER, ble.getMessage());
//            }
        }

    }

    private void validateFilePath(String filePath) {
        if (filePath != null) {
            File file = new File(filePath);
            if ((file == null) || (!file.exists())) {
                firePropertyChange(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_ERROR_EVT, null,
                        NbBundle.getMessage(FileBindingConfigurationPanel.class,
                        "FileBindingConfiguratonnPanel.FILE_NOT_FOUND"));
            } else {
                firePropertyChange(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_CLEAR_MESSAGES_EVT, null, "");
            }
        }
    }

    private void initFileChooser() {
        if (this.directoryFileChooser != null) {
            this.directoryFileChooser.setFileSelectionMode(JFileChooser.
                    DIRECTORIES_ONLY);
            this.directoryFileChooser.setAcceptAllFileFilterUsed(false);
        }
    }

    public class PortCellRenderer extends JLabel
            implements javax.swing.ListCellRenderer {

        public PortCellRenderer() {
            super();
            setOpaque(true);
        }

        public java.awt.Component getListCellRendererComponent(JList list,
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

    private void handleItemStateChanged(ItemEvent evt) {
        if (evt.getSource() == pathRelativeToComboBox) {
            handlePathRelativeToComboBoxChange(evt);
        } else if (evt.getSource() == operationNameComboBox) {
            operationNameComboBoxItemStateChanged(evt);
        } 
    }

    private void handleActionPerformed(ActionEvent evt) {
        if (evt.getSource() == directoryTextField) {
            handlePathRelativeToComboBoxChange(true);
        } else if (evt.getSource() == outputFileNameTextField) {
            handleIsFilePattern();
        } else if (evt.getSource() == browseButton) {
            handleBrowseButtonActionPerformed(evt);
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
                            "FileBindingConfigurationPanel.directoryMustBeRelative.text"), mMessageTypePanel);
                }
            }
        }
        return fileError;
    }
    
    private void handleBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {                                             
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int retVal = directoryFileChooser.showDialog(null, "Select");
                if (retVal == javax.swing.JFileChooser.APPROVE_OPTION) {
                    directoryTextField.setText(directoryFileChooser.
                            getSelectedFile().getAbsolutePath());
                    // per BC developer, no need to validate directory
                    //validateFilePath(getDirectoryTextField().getText());
                }
            }
        });
    }

    private void operationNameComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {
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
                    FileMessage outputMessage = getOutputFileMessage(binding,
                            selectedOperation);
                    updateOutputMessageView(binding, outputMessage);
                }
            }
        }
    }

    private void handleIsFilePattern() {
        if (anyPatternSpecified()) {
            if (!isValidPatternSpecified()) {
                // just a warning and should not prevent from proceeding
                firePropertyChange(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_NORMAL_MESSAGE_EVT, null, NbBundle.getMessage(
                        FileBindingConfigurationPanel.class,
                        "FileBindingConfigurationPanel.UnsupportedFileNamePattern"));
                return;
            }
        }
        firePropertyChange(
                ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_CLEAR_MESSAGES_EVT, null, "");

    }

   public class MyPopupAction extends AbstractAction {
        public void actionPerformed(ActionEvent evt) {
            if (mShowDescriptionMenu.isSelected()) {
            }
        }    
    }

    void showPopup(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            if (mPopupAction == null) {
                mPopupAction = new MyPopupAction();

                mPopupMenu = new JPopupMenu();
                mShowDescriptionMenu = new JCheckBoxMenuItem(mPopupAction);
                mShowDescriptionMenu.setSelected(true);
                mPopupMenu.add(mShowDescriptionMenu);
                mShowDescriptionMenu.setText(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class,
                    "InboundMessagePanel.ShowDescription"));
            }

            Point p = evt.getPoint();
            mPopupMenu.show(evt.getComponent(), p.x, p.y);

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
    
   private void initCustomComponents() {
        mMessageTypePanel = new MessageTypePanel(mWsdlComponent, null, null, null, false);        
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        outboundPanel.add(mMessageTypePanel, gridBagConstraints);        
        
        setAccessibility();
    }
    
    private void setAccessibility() {
        this.getAccessibleContext().setAccessibleName(getName());
        this.getAccessibleContext().setAccessibleDescription(getName());        
        outputFileNameTextField.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_fileName")); // NOI18N
        outputFileNameTextField.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_fileName")); // NOI18N        
        browseButton.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_fileDirectory")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_fileDirectory")); // NOI18N
        pathRelativeToComboBox.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_pathRelativeTo")); // NOI18N
        pathRelativeToComboBox.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_pathRelativeTo")); // NOI18N
        directoryTextField.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_fileDirectory")); // NOI18N
        directoryTextField.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_fileDirectory")); // NOI18N        
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

        MultiSingleBtnGrp = new javax.swing.ButtonGroup();
        msgTypeBtnGrp = new javax.swing.ButtonGroup();
        fileExistsBtnGrp = new javax.swing.ButtonGroup();
        directoryFileChooser = new javax.swing.JFileChooser();
        DelimitedFixedBtnGrp = new javax.swing.ButtonGroup();
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
        outputPartPanel = new javax.swing.JPanel();
        outputPartLabel = new javax.swing.JLabel();
        outputPartComboBox = new javax.swing.JComboBox();
        jSplitPane1 = new javax.swing.JSplitPane();
        outboundPanel = new javax.swing.JPanel();
        fileWiteSectionPanel = new javax.swing.JPanel();
        filePollingLab = new javax.swing.JLabel();
        filePollingSep = new javax.swing.JSeparator();
        directoryLabImage = new javax.swing.JLabel();
        directoryLabel = new javax.swing.JLabel();
        directoryTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        outputFileNameLabel = new javax.swing.JLabel();
        outputFileNameTextField = new javax.swing.JTextField();
        pathRelativeToLabel = new javax.swing.JLabel();
        pathRelativeToComboBox = new javax.swing.JComboBox();
        isRelToIcon = new javax.swing.JLabel();
        fetchRecursiveChkBox = new javax.swing.JCheckBox();
        fetchRecursiveExcludeIconLab = new javax.swing.JLabel();
        fetchRecursiveExcludeLab = new javax.swing.JLabel();
        fetchRecursiveExcludePatternTextFld = new javax.swing.JTextField();
        inputFileRegExBox = new javax.swing.JCheckBox();
        descriptionPanel = new javax.swing.JPanel();

        directoryFileChooser.setName("directoryFileChooser"); // NOI18N

        portBindingNameGeneralPanel.setName("portBindingNameGeneralPanel"); // NOI18N
        portBindingNameGeneralPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(bindingNameLabel, org.openide.util.NbBundle.getMessage(SolicitedReadPanel.class, "SolicitedReadPanel.bindingNameLabel.text_1")); // NOI18N
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

        org.openide.awt.Mnemonics.setLocalizedText(portTypeLabel, org.openide.util.NbBundle.getMessage(SolicitedReadPanel.class, "SolicitedReadPanel.portTypeLabel.text_1")); // NOI18N
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

        org.openide.awt.Mnemonics.setLocalizedText(portNameLabel, org.openide.util.NbBundle.getMessage(SolicitedReadPanel.class, "SolicitedReadPanel.portNameLabel.text_1")); // NOI18N
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
        org.openide.awt.Mnemonics.setLocalizedText(servicesGeneralLabel, org.openide.util.NbBundle.getMessage(SolicitedReadPanel.class, "SolicitedReadPanel.servicesGeneralLabel.text_1")); // NOI18N
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
        org.openide.awt.Mnemonics.setLocalizedText(serviceBindingOperationLabel, org.openide.util.NbBundle.getMessage(SolicitedReadPanel.class, "SolicitedReadPanel.serviceBindingOperationLabel.text_1")); // NOI18N
        serviceBindingOperationLabel.setName("serviceBindingOperationLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 0, 0);
        operationNamePanel.add(serviceBindingOperationLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(operationNameLabel, org.openide.util.NbBundle.getMessage(SolicitedReadPanel.class, "SolicitedReadPanel.operationNameLabel.text_1")); // NOI18N
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

        outputPartPanel.setName("outputPartPanel"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(outputPartLabel, org.openide.util.NbBundle.getMessage(SolicitedReadPanel.class, "SolicitedReadPanel.outputPartLabel.text")); // NOI18N
        outputPartLabel.setToolTipText(mBundle.getString("DESC_Attribute_part"));
        outputPartLabel.setName("outputPartLabel"); // NOI18N
        outputPartPanel.add(outputPartLabel);

        outputPartComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        outputPartComboBox.setToolTipText(mBundle.getString("DESC_Attribute_part"));
        outputPartComboBox.setName("outputPartComboBox"); // NOI18N
        outputPartComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                outputPartComboBoxparameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                outputPartComboBoxparameterFocusLost(evt);
            }
        });
        outputPartPanel.add(outputPartComboBox);

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(1.0);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        outboundPanel.setMinimumSize(new java.awt.Dimension(392, 153));
        outboundPanel.setName("outboundPanel"); // NOI18N
        outboundPanel.setPreferredSize(new java.awt.Dimension(396, 153));
        outboundPanel.setLayout(new java.awt.GridBagLayout());

        fileWiteSectionPanel.setMinimumSize(new java.awt.Dimension(372, 143));
        fileWiteSectionPanel.setName("fileWiteSectionPanel"); // NOI18N
        fileWiteSectionPanel.setPreferredSize(new java.awt.Dimension(376, 143));
        fileWiteSectionPanel.setLayout(new java.awt.GridBagLayout());

        filePollingLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(filePollingLab, org.openide.util.NbBundle.getMessage(SolicitedReadPanel.class, "SolicitedReadPanel.filePollingLab.text_1")); // NOI18N
        filePollingLab.setName("filePollingLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        fileWiteSectionPanel.add(filePollingLab, gridBagConstraints);

        filePollingSep.setName("filePollingSep"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 55, 0, 0);
        fileWiteSectionPanel.add(filePollingSep, gridBagConstraints);

        directoryLabImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/file/resources/service_composition_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(directoryLabImage, org.openide.util.NbBundle.getMessage(SolicitedReadPanel.class, "SolicitedReadPanel.directoryLabImage.text_1")); // NOI18N
        directoryLabImage.setToolTipText(org.openide.util.NbBundle.getMessage(SolicitedReadPanel.class, "SolicitedReadPanel.directoryLabImage.toolTipText_1")); // NOI18N
        directoryLabImage.setName("directoryLabImage"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        fileWiteSectionPanel.add(directoryLabImage, gridBagConstraints);

        directoryLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        directoryLabel.setLabelFor(directoryTextField);
        org.openide.awt.Mnemonics.setLocalizedText(directoryLabel, org.openide.util.NbBundle.getMessage(SolicitedReadPanel.class, "SolicitedReadPanel.directoryLabel.text_1")); // NOI18N
        directoryLabel.setName("directoryLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 40, 0, 10);
        fileWiteSectionPanel.add(directoryLabel, gridBagConstraints);

        directoryTextField.setName("directoryTextField"); // NOI18N
        directoryTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                directoryTextFieldActionPerformed(evt);
            }
        });
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
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        fileWiteSectionPanel.add(directoryTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(SolicitedReadPanel.class, "SolicitedReadPanel.browseButton.text_1")); // NOI18N
        browseButton.setName("browseButton"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        fileWiteSectionPanel.add(browseButton, gridBagConstraints);

        outputFileNameLabel.setLabelFor(outputFileNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(outputFileNameLabel, org.openide.util.NbBundle.getMessage(SolicitedReadPanel.class, "SolicitedReadPanel.outputFileNameLabel.text")); // NOI18N
        outputFileNameLabel.setName("outputFileNameLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        fileWiteSectionPanel.add(outputFileNameLabel, gridBagConstraints);

        outputFileNameTextField.setToolTipText(mBundle.getString("DESC_Attribute_fileName"));
        outputFileNameTextField.setName("outputFileNameTextField"); // NOI18N
        outputFileNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                outputFileNameTextFieldparameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                outputFileNameTextFieldparameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        fileWiteSectionPanel.add(outputFileNameTextField, gridBagConstraints);

        pathRelativeToLabel.setLabelFor(pathRelativeToComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(pathRelativeToLabel, org.openide.util.NbBundle.getMessage(SolicitedReadPanel.class, "SolicitedReadPanel.pathRelativeToLabel.text_1")); // NOI18N
        pathRelativeToLabel.setName("pathRelativeToLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 60, 0, 0);
        fileWiteSectionPanel.add(pathRelativeToLabel, gridBagConstraints);

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
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 140, 0, 0);
        fileWiteSectionPanel.add(pathRelativeToComboBox, gridBagConstraints);

        isRelToIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/file/resources/service_composition_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(isRelToIcon, org.openide.util.NbBundle.getMessage(SolicitedReadPanel.class, "SolicitedReadPanel.isRelToIcon.text_1")); // NOI18N
        isRelToIcon.setToolTipText(org.openide.util.NbBundle.getMessage(SolicitedReadPanel.class, "SolicitedReadPanel.isRelToIcon.toolTipText_1")); // NOI18N
        isRelToIcon.setName("isRelToIcon"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 40, 0, 0);
        fileWiteSectionPanel.add(isRelToIcon, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(fetchRecursiveChkBox, org.openide.util.NbBundle.getMessage(SolicitedReadPanel.class, "SolicitedReadPanel.fetchRecursiveChkBox.text")); // NOI18N
        fetchRecursiveChkBox.setName("fetchRecursiveChkBox"); // NOI18N
        fetchRecursiveChkBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fetchRecursiveChkBoxActionPerformed(evt);
            }
        });
        fetchRecursiveChkBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                fetchRecursiveChkBoxFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 40, 0, 0);
        fileWiteSectionPanel.add(fetchRecursiveChkBox, gridBagConstraints);
        fetchRecursiveChkBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SolicitedReadPanel.class, "SolicitedReadPanel.fetchRecursiveChkBox.AccessibleContext.accessibleDescription")); // NOI18N

        fetchRecursiveExcludeIconLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/file/resources/service_composition_16.png"))); // NOI18N
        fetchRecursiveExcludeIconLab.setToolTipText(org.openide.util.NbBundle.getMessage(SolicitedReadPanel.class, "SolicitedReadPanel.fetchRecursiveExcludeIconLab.tooltip.text")); // NOI18N
        fetchRecursiveExcludeIconLab.setName("fetchRecursiveExcludeIconLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 40, 0, 0);
        fileWiteSectionPanel.add(fetchRecursiveExcludeIconLab, gridBagConstraints);

        fetchRecursiveExcludeLab.setLabelFor(fetchRecursiveExcludePatternTextFld);
        org.openide.awt.Mnemonics.setLocalizedText(fetchRecursiveExcludeLab, org.openide.util.NbBundle.getMessage(SolicitedReadPanel.class, "SolicitedReadPanel.fetchRecursiveExcludeLab.text")); // NOI18N
        fetchRecursiveExcludeLab.setName("fetchRecursiveExcludeLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 60, 0, 0);
        fileWiteSectionPanel.add(fetchRecursiveExcludeLab, gridBagConstraints);
        fetchRecursiveExcludeLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SolicitedReadPanel.class, "SolicitedReadPanel.fetchRecursiveExcludeLab.AccessibleContext.accessibleDescription")); // NOI18N

        fetchRecursiveExcludePatternTextFld.setText(org.openide.util.NbBundle.getMessage(SolicitedReadPanel.class, "SolicitedReadPanel.fetchRecursiveExcludePatternTextFld.text")); // NOI18N
        fetchRecursiveExcludePatternTextFld.setName("fetchRecursiveExcludePatternTextFld"); // NOI18N
        fetchRecursiveExcludePatternTextFld.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                fetchRecursiveExcludeTextFldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        fileWiteSectionPanel.add(fetchRecursiveExcludePatternTextFld, gridBagConstraints);
        fetchRecursiveExcludePatternTextFld.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SolicitedReadPanel.class, "SolicitedReadPanel.fetchRecursiveExcludePatternTextFld.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(inputFileRegExBox, org.openide.util.NbBundle.getMessage(SolicitedReadPanel.class, "SolicitedReadPanel.inputFileRegExBox.text")); // NOI18N
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
        fileWiteSectionPanel.add(inputFileRegExBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        outboundPanel.add(fileWiteSectionPanel, gridBagConstraints);

        jSplitPane1.setTopComponent(outboundPanel);

        descriptionPanel.setMinimumSize(new java.awt.Dimension(400, 50));
        descriptionPanel.setName("descriptionPanel"); // NOI18N
        descriptionPanel.setPreferredSize(new java.awt.Dimension(400, 50));
        descriptionPanel.setLayout(new java.awt.BorderLayout());
        descPanel = new DescriptionPanel();
        descriptionPanel.add(descPanel, java.awt.BorderLayout.CENTER);
        jSplitPane1.setBottomComponent(descriptionPanel);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void directoryTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_directoryTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_directoryTextFieldActionPerformed

    private void directoryTextFieldparameterFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_directoryTextFieldparameterFocusGained
        // TODO add your handling code here:
        updateDescriptionArea(evt);
    }//GEN-LAST:event_directoryTextFieldparameterFocusGained

    private void directoryTextFieldparameterFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_directoryTextFieldparameterFocusLost
        // TODO add your handling code here:
        //    clearDescriptionArea();
    }//GEN-LAST:event_directoryTextFieldparameterFocusLost

    private void outputFileNameTextFieldparameterFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_outputFileNameTextFieldparameterFocusGained
        // TODO add your handling code here:
        updateDescriptionArea(evt);
    }//GEN-LAST:event_outputFileNameTextFieldparameterFocusGained

    private void outputFileNameTextFieldparameterFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_outputFileNameTextFieldparameterFocusLost
        // TODO add your handling code here:
        //    clearDescriptionArea();
    }//GEN-LAST:event_outputFileNameTextFieldparameterFocusLost

    private void pathRelativeToComboBoxparameterFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pathRelativeToComboBoxparameterFocusGained
        // TODO add your handling code here:
        updateDescriptionArea(evt);
    }//GEN-LAST:event_pathRelativeToComboBoxparameterFocusGained

    private void pathRelativeToComboBoxparameterFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pathRelativeToComboBoxparameterFocusLost
        // TODO add your handling code here:
        //    clearDescriptionArea();
    }//GEN-LAST:event_pathRelativeToComboBoxparameterFocusLost

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

    private void outputPartComboBoxparameterFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_outputPartComboBoxparameterFocusGained
        // TODO add your handling code here:
        updateDescriptionArea(evt);
    }//GEN-LAST:event_outputPartComboBoxparameterFocusGained

    private void outputPartComboBoxparameterFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_outputPartComboBoxparameterFocusLost
        // TODO add your handling code here:
        //    clearDescriptionArea();
    }//GEN-LAST:event_outputPartComboBoxparameterFocusLost

    private void fetchRecursiveChkBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fetchRecursiveChkBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fetchRecursiveChkBoxActionPerformed

    private void fetchRecursiveChkBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fetchRecursiveChkBoxFocusGained
        // TODO add your handling code here:
        updateDescriptionArea(evt);
    }//GEN-LAST:event_fetchRecursiveChkBoxFocusGained

    private void fetchRecursiveExcludeTextFldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fetchRecursiveExcludeTextFldFocusGained
        // TODO add your handling code here:
        updateDescriptionArea(evt);
    }//GEN-LAST:event_fetchRecursiveExcludeTextFldFocusGained

    private void inputFileRegExBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputFileRegExBoxFocusGained
        // TODO add your handling code here:
        updateDescriptionArea(evt);
}//GEN-LAST:event_inputFileRegExBoxFocusGained


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup DelimitedFixedBtnGrp;
    private javax.swing.ButtonGroup MultiSingleBtnGrp;
    private javax.swing.JComboBox bindingNameComboBox;
    private javax.swing.JLabel bindingNameLabel;
    private javax.swing.JButton browseButton;
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JFileChooser directoryFileChooser;
    private javax.swing.JLabel directoryLabImage;
    private javax.swing.JLabel directoryLabel;
    private javax.swing.JTextField directoryTextField;
    private javax.swing.JCheckBox fetchRecursiveChkBox;
    private javax.swing.JLabel fetchRecursiveExcludeIconLab;
    private javax.swing.JLabel fetchRecursiveExcludeLab;
    private javax.swing.JTextField fetchRecursiveExcludePatternTextFld;
    private javax.swing.ButtonGroup fileExistsBtnGrp;
    private javax.swing.JLabel filePollingLab;
    private javax.swing.JSeparator filePollingSep;
    private javax.swing.JPanel fileWiteSectionPanel;
    private javax.swing.JCheckBox inputFileRegExBox;
    private javax.swing.JLabel isRelToIcon;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.ButtonGroup msgTypeBtnGrp;
    private javax.swing.JComboBox operationNameComboBox;
    private javax.swing.JLabel operationNameLabel;
    private javax.swing.JPanel operationNamePanel;
    private javax.swing.JPanel outboundPanel;
    private javax.swing.JLabel outputFileNameLabel;
    private javax.swing.JTextField outputFileNameTextField;
    private javax.swing.JComboBox outputPartComboBox;
    private javax.swing.JLabel outputPartLabel;
    private javax.swing.JPanel outputPartPanel;
    private javax.swing.JComboBox pathRelativeToComboBox;
    private javax.swing.JLabel pathRelativeToLabel;
    private javax.swing.JPanel portBindingNameGeneralPanel;
    private javax.swing.JLabel portNameLabel;
    private javax.swing.JComboBox portTypeComboBox;
    private javax.swing.JLabel portTypeLabel;
    private javax.swing.JSeparator sep1General;
    private javax.swing.JLabel serviceBindingOperationLabel;
    private javax.swing.JComboBox servicePortComboBox;
    private javax.swing.JLabel servicesGeneralLabel;
    // End of variables declaration//GEN-END:variables

}
