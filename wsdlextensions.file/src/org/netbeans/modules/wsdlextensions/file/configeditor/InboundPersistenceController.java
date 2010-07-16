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

package org.netbeans.modules.wsdlextensions.file.configeditor;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.wsdlextensions.file.model.FileAddress;
import org.netbeans.modules.wsdlextensions.file.model.FileBinding;
import org.netbeans.modules.wsdlextensions.file.model.FileConstants;
import org.netbeans.modules.wsdlextensions.file.model.FileMessage;
import org.netbeans.modules.wsdlextensions.file.model.FileOperation;

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 * Controller to allow for persisting of the model from
 * from the visual components as well as retrieval from the model
 * 
 * @author jalmero
 */
public class InboundPersistenceController {
    private InboundMessagePanel fileEditorPanel = null;
    private WSDLComponent mWSDLComponent = null;
    private InboundPersistenceController mController = null;
        
    public InboundPersistenceController(WSDLComponent modelComponent,
            InboundMessagePanel visualComponent) {
        fileEditorPanel = visualComponent;
        mWSDLComponent = modelComponent;
    }
    
    /**
     * Commit all changes
     * @return
     */
    public boolean commit() {
        FileError fileError = fileEditorPanel.validateMe();
        if (ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_ERROR_EVT.equals(fileError.getErrorMode())) {
            return false;
        }        

        if (mWSDLComponent instanceof FileAddress) {
            return commitAddress((FileAddress) mWSDLComponent);
        } else if (mWSDLComponent instanceof FileBinding) {
            return commitBinding((FileBinding) mWSDLComponent);
        } else if (mWSDLComponent instanceof Port) {
            return commitPort((Port) mWSDLComponent);
        } else if (mWSDLComponent instanceof FileMessage) {
            return commitMessage((FileMessage) mWSDLComponent);
        } else if (mWSDLComponent instanceof FileOperation) {
            return commitOperation((FileOperation) mWSDLComponent);
        }
        return false;
    }

    private boolean commitAddress(FileAddress fileAddress) { 
        WSDLModel wsdlModel = fileAddress.getModel();
        try {            
            if (!wsdlModel.isIntransaction()) {
               wsdlModel.startTransaction(); 
            }
            fileAddress.setFileDirectory(fileEditorPanel.getDirectory());
            if (fileEditorPanel.getRelativePath()) {   
                fileAddress.setRelativePath(true);
                fileAddress.setPathRelativeTo(fileEditorPanel.getPathRelativeTo());
            } else {
                fileAddress.setAttribute(FileAddress.ATTR_FILE_RELATIVE_PATH,
                        null);
                fileAddress.setPathRelativeTo(null);
            }            

            fileAddress.setRecursive(fileEditorPanel.getPollRecursive());
            fileAddress.setRecursiveExclude(fileEditorPanel.getPollRecursiveExclude());
            
            Port port = (Port) fileAddress.getParent();
            Binding binding = port.getBinding().get();
            String operationName = fileEditorPanel.getOperationName();
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
                       
            // only 1
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(operationName)) {
                    BindingInput bi = bop.getBindingInput();
                    if (bi != null) {
                        List<FileMessage> inputFileMessages =
                                bi.getExtensibilityElements(FileMessage.class);
                        if (inputFileMessages.size() > 0) {
                            FileMessage inputFileMessage =
                                    inputFileMessages.get(0);
                            commitInputFileMessage(binding, operationName,
                                    inputFileMessage);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return false;
        }    
        return true;
    }

    private boolean commitBinding(FileBinding fileBinding) {
        WSDLModel wsdlModel = fileBinding.getModel();
        try {            
            if (!wsdlModel.isIntransaction()) {
               wsdlModel.startTransaction(); 
            }
            FileAddress fileAddress = fileEditorPanel.
                    getFileAddressPerSelectedPort();
            if (fileAddress != null) {
                fileAddress.setFileDirectory(fileEditorPanel.getDirectory());
                if (fileEditorPanel.getRelativePath()) {   
                    fileAddress.setRelativePath(true);
                    fileAddress.setPathRelativeTo(fileEditorPanel.getPathRelativeTo());
                } else {
                    fileAddress.setAttribute(FileAddress.ATTR_FILE_RELATIVE_PATH,
                            null);
                    fileAddress.setPathRelativeTo(null);
                }                    
            }
            Binding binding = (Binding) fileBinding.getParent();
            
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            String operationName = fileEditorPanel.getOperationName();           
            
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(operationName)) {
                    BindingInput bi = bop.getBindingInput();
                    if (bi != null) {
                        List<FileMessage> inputFileMessages =
                                bi.getExtensibilityElements(FileMessage.class);
                        if (inputFileMessages.size() > 0) {
                            FileMessage inputFileMessage =
                                    inputFileMessages.get(0);
                            commitInputFileMessage(binding, operationName,
                                    inputFileMessage);
                        }
                    }
                }
            }      
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return false;
        } 
        return true;
    }
    
    private void commitInputFileMessage(Binding binding, String opName,
            FileMessage inputFileMessage) {
        inputFileMessage.setFileName(fileEditorPanel.getFileName());
        inputFileMessage.setAttribute(FileMessage.ATTR_POLLING_INTERVAL, 
                fileEditorPanel.getInputPollingInterval());
        if (fileEditorPanel.getInputUseType().equals(FileConstants.ENCODED)) {        
            inputFileMessage.setFileEncodingStyle(fileEditorPanel.
                    getEncodingStyle());
            inputFileMessage.setFileUseType(fileEditorPanel.getInputUseType());
        } else if ((fileEditorPanel.isEncodedTypeEnabled()) &&
            (fileEditorPanel.getEncodingStyle() != null)) {
                inputFileMessage.setFileUseType(FileConstants.ENCODED);
                inputFileMessage.setFileEncodingStyle(fileEditorPanel.
                    getEncodingStyle());                              
        } else if ((inputFileMessage.getFileUseType() != null) &&
                    (fileEditorPanel.getInputUseType().equals(FileConstants.LITERAL))) {
                inputFileMessage.setFileUseType(null);
                inputFileMessage.setFileEncodingStyle(null);                   
        } else {
            inputFileMessage.setFileUseType(FileConstants.LITERAL);
        }
       
        if (fileEditorPanel.getForwardAsAttachment()) {
            inputFileMessage.setForwardAsAttachment(fileEditorPanel.
                    getForwardAsAttachment());
        } else {
            inputFileMessage.setAttribute(FileMessage.ATTR_FORWARD_AS_ATTACHMENT,
                null);            
        }        
        
        if (fileEditorPanel.getInputFileIsPattern()) {
            inputFileMessage.setFileNameIsPattern(fileEditorPanel.
                    getInputFileIsPattern());
        } else {
            inputFileMessage.setAttribute(FileMessage.ATTR_FILE_NAME_IS_PATTERN,
                null);            
        }

        if (fileEditorPanel.getInputFileIsRegex()) {
            inputFileMessage.setFileNameIsRegex(fileEditorPanel.
                    getInputFileIsRegex());
        } else {
            inputFileMessage.setAttribute(FileMessage.ATTR_FILE_NAME_IS_REGEX,
                null);
        }

        // set the type of the message part if it is undefined based
        // on the MessageType option from user
        commitMessageType(binding, opName, inputFileMessage);

        // only persist if payload section is editable
        if (fileEditorPanel.isPayloadEditable()) {
            if (fileEditorPanel.getCharset() != null) {
                inputFileMessage.setAttribute(FileMessage.ATTR_FILE_CHARSET,
                    fileEditorPanel.getCharset());
            } else if (inputFileMessage.getAttribute(FileMessage.ATTR_FILE_CHARSET) != null) {
                inputFileMessage.setAttribute(FileMessage.ATTR_FILE_CHARSET, null);
            }
        }

        if (fileEditorPanel.getInputRemoveEOL()) {
            inputFileMessage.setRemoveEOL(fileEditorPanel.getInputRemoveEOL());
        } else {
            inputFileMessage.setAttribute(FileMessage.ATTR_REMOVE_EOL, null);
        }
              
        if (fileEditorPanel.getInputMultiRecordsPerFile()) {
            if (fileEditorPanel.getInputRecordDelimiter() != null) {
               inputFileMessage.setRecordDelimiter(fileEditorPanel.
                    getInputRecordDelimiter()); 

                if (fileEditorPanel.getInputMaxBytesPerRecord() != null)                 {
                    inputFileMessage.setAttribute(FileMessage.ATTR_MAX_BYTES_PER_RECORD,fileEditorPanel.
                            getInputMaxBytesPerRecord());
                } else {
                    inputFileMessage.setAttribute(FileMessage.
                            ATTR_MAX_BYTES_PER_RECORD, null);
                }               
            } else {
                if (inputFileMessage.getRecordDelimiter() != null) {
                    inputFileMessage.setAttribute(FileMessage.ATTR_RECORD_DELIM, null);
                }
                
                if (fileEditorPanel.getInputMaxBytesPerRecord() != null)                 {
                    inputFileMessage.setAttribute(FileMessage.ATTR_MAX_BYTES_PER_RECORD,
                            fileEditorPanel.getInputMaxBytesPerRecord());
                } else {
                    inputFileMessage.setAttribute(FileMessage.
                            ATTR_MULTIPLE_RECORDS_PER_FILE, null);
                }
            }                      
        } else {
            if (fileEditorPanel.getInputMaxBytesPerRecord() != null) {
                 inputFileMessage.setAttribute(FileMessage.ATTR_MAX_BYTES_PER_RECORD, 
                            fileEditorPanel.getInputMaxBytesPerRecord());
                inputFileMessage.setAttribute(FileMessage.ATTR_RECORD_DELIM, null);
                inputFileMessage.setAttribute(FileMessage.ATTR_MULTIPLE_RECORDS_PER_FILE, null);
            } else {
                inputFileMessage.setAttribute(FileMessage.ATTR_RECORD_DELIM, null);
                inputFileMessage.setAttribute(FileMessage.ATTR_MULTIPLE_RECORDS_PER_FILE, null);
                inputFileMessage.setAttribute(FileMessage.ATTR_MAX_BYTES_PER_RECORD, null);
            }
        }

        if (inputFileMessage.getMultipleRecordsPerFile() != 
                fileEditorPanel.getInputMultiRecordsPerFile()) {
            inputFileMessage.setMultipleRecordsPerFile(fileEditorPanel.
                    getInputMultiRecordsPerFile());
        }
        
        if (fileEditorPanel.getInputArchive()) {
            inputFileMessage.setArchiveEnabled(fileEditorPanel.
                    getInputArchive());            
            inputFileMessage.setArchiveDirIsRelative(fileEditorPanel.
                    getInputArchiveRelative());
            inputFileMessage.setArchiveDirectory(fileEditorPanel.
                    getInputArchiveDirectory());
        } else {
            inputFileMessage.setAttribute(FileMessage.ATTR_ARCHIVE_ENABLED, null);
            inputFileMessage.setAttribute(FileMessage.ATTR_ARCHIVE_DIR_IS_RELATIVE, null);
            inputFileMessage.setAttribute(FileMessage.ATTR_ARCHIVE_DIR, null);
        }        
                
    }   

    private boolean commitPort(Port port) {
        Collection<FileAddress> address = port.
                getExtensibilityElements(FileAddress.class);
        FileAddress fileAddress = address.iterator().next();
        return commitAddress(fileAddress);
    }
    
    private boolean commitMessage(FileMessage fileMessage) {
        Object parentObj = fileMessage.getParent();
        FileBinding fileBinding = null;
        BindingOperation parentOp = null;
        if (parentObj instanceof BindingInput) {
            parentOp = 
                    (BindingOperation) ((BindingInput) parentObj).getParent();
        } else if (parentObj instanceof  BindingOutput) {
            parentOp = 
                    (BindingOperation) ((BindingOutput) parentObj).getParent();
        }
        if (parentObj != null) {
            Binding parentBinding = (Binding) parentOp.getParent();
            Collection<FileBinding> bindings = 
                    parentBinding.getExtensibilityElements(FileBinding.class);
            if (!bindings.isEmpty()) {
                return commitBinding(bindings.iterator().next());
            }
        }
        return false;
    }
     
    private boolean commitOperation(FileOperation fileOperation) {
        Object obj = fileOperation.getParent();
        if (obj instanceof BindingOperation) {
            Binding parentBinding = 
                    (Binding) ((BindingOperation) obj).getParent();
            Collection<FileBinding> bindings = 
                    parentBinding.getExtensibilityElements(FileBinding.class);
            if (!bindings.isEmpty()) {
                return commitBinding(bindings.iterator().next());
            }
        }
        return false;
    }    
    
    private void commitMessageType(Binding binding, String opName,
            FileMessage inputFileMessage) {
        String partName = fileEditorPanel.getInputPart();
        if (partName != null) {
            Collection parts = FileUtilities.getInputParts(binding, opName);
            if ((parts != null) && (parts.size() > 0)) {
                Iterator iter = parts.iterator();
                while(iter.hasNext()) {
                    Part partEntry = (Part) iter.next();
                    if (partEntry.getName().equals(partName)) {
                        FileUtilities.setPartType(partEntry, fileEditorPanel.getMessageType(),
                                fileEditorPanel.getSelectedPartType(), fileEditorPanel.getSelectedElementType());
                    }
                }
            }
            FileUtilities.setPart(fileEditorPanel.getMessageType(), 
                    inputFileMessage, partName);
        } else {
            // need to create a message with part
            Message newMessage = FileUtilities.
                    createMessage(mWSDLComponent.getModel(), "PollInputMessage");
            if (newMessage != null) {
                Part newPart = FileUtilities.createPart(mWSDLComponent.getModel(), 
                        newMessage, "part1");
                if (newPart != null) {
                    FileUtilities.setPartType(newPart, fileEditorPanel.getMessageType(),
                            fileEditorPanel.getSelectedPartType(), fileEditorPanel.getSelectedElementType());
          
                    if (FileUtilities.getMessagePart(newPart.getName(), newMessage) == null) {
                        newMessage.addPart(newPart);
                    }
                    FileUtilities.setPart(fileEditorPanel.getMessageType(), 
                            inputFileMessage, newPart.getName());
                }
            }
        }        
    }  
    
    public static void populate() {
        // TODO
    }
    
}
