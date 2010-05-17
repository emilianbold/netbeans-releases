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
 * from the visual components as well as persistence of the model
 * 
 * @author jalmero
 */
public class SolicitedReadPersistenceController {
    private SolicitedReadPanel editorPanel = null;
    private WSDLComponent mWSDLComponent = null;
    
    public SolicitedReadPersistenceController(WSDLComponent modelComponent,
            SolicitedReadPanel visualComponent) {
        editorPanel = visualComponent;
        mWSDLComponent = modelComponent;
    }
    
    /**
     * Commit all changes
     * @return
     */
    public boolean commit() {
        FileError fileError = editorPanel.validateMe();
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

    /**
     * Rollback any changes
     * @return
     */
    public boolean rollback() {
        return true;
    }
    
    private boolean commitAddress(FileAddress fileAddress) { 
        WSDLModel wsdlModel = fileAddress.getModel();
        try {            
            if (!wsdlModel.isIntransaction()) {
               wsdlModel.startTransaction(); 
            }
            fileAddress.setFileDirectory(editorPanel.getDirectory());
            if (editorPanel.getRelativePath()) {
                fileAddress.setRelativePath(true);
                fileAddress.setPathRelativeTo(editorPanel.getPathRelativeTo());
            } else {
                fileAddress.setAttribute(FileAddress.ATTR_FILE_RELATIVE_PATH,
                        null);
                fileAddress.setPathRelativeTo(null);
            }

            fileAddress.setRecursive(editorPanel.getFetchRecursive());
            fileAddress.setRecursiveExclude(editorPanel.getFetchRecursiveExclude());
            
            Port port = (Port) fileAddress.getParent();
            Binding binding = port.getBinding().get();
            String operationName = editorPanel.getOperationName();
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            // only 1
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(operationName)) {                    
                    
                    Collection<FileOperation> fileOps =
                            bop.getExtensibilityElements(FileOperation.class);
                    if ((fileOps != null) && (!fileOps.isEmpty())) {
                        for (FileOperation fileOp : fileOps) {
                            // one op
                            String verb = fileOp.getVerb();
                            if (!FileConstants.VERB_READ.equals(verb)) {
                                // this verb must be a read
                                fileOp.setVerb(FileConstants.VERB_READ);
                                break;
                            }
                        }
                    }   
                    
                    BindingOutput bo = bop.getBindingOutput();
                    if (bo != null) {
                        List<FileMessage> outputFileMessages =
                                bo.getExtensibilityElements(FileMessage.class);
                        if (outputFileMessages.size() > 0) {
                            FileMessage outputFileMessage =
                                    outputFileMessages.get(0);
                            commitOutputFileMessage(binding, operationName,
                                    outputFileMessage);
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
            FileAddress fileAddress = editorPanel.
                    getFileAddressPerSelectedPort();
            if (fileAddress != null) {
                fileAddress.setFileDirectory(editorPanel.getDirectory());
               if (editorPanel.getRelativePath()) {   
                    fileAddress.setRelativePath(true);
                    fileAddress.setPathRelativeTo(editorPanel.getPathRelativeTo());
                } else {
                    fileAddress.setAttribute(FileAddress.ATTR_FILE_RELATIVE_PATH,
                            null);
                    fileAddress.setPathRelativeTo(null);
                }                
            }
            Binding binding = (Binding) fileBinding.getParent();
            
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            String operationName = editorPanel.getOperationName();
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(operationName)) {
                    BindingInput bi = bop.getBindingInput();
                    if (bi != null) {
                        List<FileMessage> inputFileMessages =
                                bi.getExtensibilityElements(FileMessage.class);
                        if (inputFileMessages.size() > 0) {
                            FileMessage inputFileMessage =
                                    inputFileMessages.get(0);
                            commitOutputFileMessage(binding, operationName,
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
    
    private void commitOutputFileMessage(Binding binding, String opName,
            FileMessage outputFileMessage) {
        outputFileMessage.setFileName(editorPanel.getFileName());

        if ( editorPanel.getInputFileIsRegex() ) {
            outputFileMessage.setFileNameIsRegex(true);
        } else {
            outputFileMessage.setAttribute(FileMessage.ATTR_FILE_NAME_IS_REGEX,
                null);
        }

        if (editorPanel.getOutputUseType().equals(FileConstants.ENCODED)) {        
            outputFileMessage.setFileEncodingStyle(editorPanel.
                    getEncodingStyle());
            outputFileMessage.setFileUseType(editorPanel.getOutputUseType());
        } else if ((editorPanel.isEncodedTypeEnabled()) &&
            (editorPanel.getEncodingStyle() != null)) {
                outputFileMessage.setFileUseType(FileConstants.ENCODED);
                outputFileMessage.setFileEncodingStyle(editorPanel.
                    getEncodingStyle());                              
        } else if ((outputFileMessage.getFileUseType() != null) &&
                    (editorPanel.getOutputUseType().equals(FileConstants.LITERAL))) {
            outputFileMessage.setFileUseType(null);
            outputFileMessage.setFileEncodingStyle(null);                        
        } else {
            outputFileMessage.setFileUseType(FileConstants.LITERAL);
        }
       
        if (editorPanel.getForwardAsAttachment()) {
            outputFileMessage.setForwardAsAttachment(editorPanel.
                    getForwardAsAttachment());
        } else {
            outputFileMessage.setAttribute(FileMessage.ATTR_FORWARD_AS_ATTACHMENT,
                null);            
        }  
        
        if (editorPanel.getOutputFileIsPattern()) {
            outputFileMessage.setFileNameIsPattern(editorPanel.
                    getOutputFileIsPattern());
        } else {
            outputFileMessage.setAttribute(FileMessage.ATTR_FILE_NAME_IS_PATTERN,
                null);            
        }        
        
        // on the MessageType option from user
        commitMessageType(binding, opName, outputFileMessage);        
 
        // only persist if payload section is editable
        if (editorPanel.isPayloadEditable()) {
            if (editorPanel.getCharset() != null) {
                outputFileMessage.setAttribute(FileMessage.ATTR_FILE_CHARSET,
                    editorPanel.getCharset());
            } else if (outputFileMessage.getAttribute(FileMessage.ATTR_FILE_CHARSET) != null) {
                outputFileMessage.setAttribute(FileMessage.ATTR_FILE_CHARSET, null);
            }
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
        BindingOperation parentOp = null;
        if (parentObj instanceof BindingInput) {
            parentOp = (BindingOperation) ((BindingInput) parentObj).getParent();
        } else if (parentObj instanceof  BindingOutput) {
            parentOp = (BindingOperation) ((BindingOutput) parentObj).getParent();
        }
        if (parentObj != null) {
            Binding parentBinding = (Binding) parentOp.getParent();
            Collection<FileBinding> bindings = parentBinding.getExtensibilityElements(FileBinding.class);
            if (!bindings.isEmpty()) {
                return commitBinding(bindings.iterator().next());
            }
        }
        return false;
    }
     
    private boolean commitOperation(FileOperation fileOperation) {
        Object obj = fileOperation.getParent();
        if (obj instanceof BindingOperation) {
            Binding parentBinding = (Binding) ((BindingOperation) obj).getParent();
            Collection<FileBinding> bindings = parentBinding.getExtensibilityElements(FileBinding.class);
            if (!bindings.isEmpty()) {
                return commitBinding(bindings.iterator().next());
            }
        }
        return false;
    }    
         
    private void commitMessageType(Binding binding, String opName,
            FileMessage outputFileMessage) {
        String partName = editorPanel.getOutputPart();
        if (partName != null) {
            // set the output part to file:message
            Collection outParts = FileUtilities.getOutputParts(binding, opName);
            if ((outParts != null) && (outParts.size() > 0)) {
                Iterator iter = outParts.iterator();
                while(iter.hasNext()) {
                    Part partEntry = (Part) iter.next();
                    if (partEntry.getName().equals(partName)) {
                        FileUtilities.setPartType(partEntry, editorPanel.getMessageType(), 
                                editorPanel.getSelectedPartType(), editorPanel.getSelectedElementType());
                    } 
                }
            }    
            FileUtilities.setPart(editorPanel.getMessageType(),
                    outputFileMessage, partName);             
        } else {
            // create a message for input but do not set the part to file:message
            Message newMessageIn = FileUtilities.
                    createMessage(mWSDLComponent.getModel(), "SolicitReadInputMessage");
            
            // create a message for output
            Message newMessageOut = FileUtilities.
                    createMessage(mWSDLComponent.getModel(), "SolicitReadOutputMessage");
            if (newMessageOut != null) {
                Part newPart = FileUtilities.createPartAndSetType(mWSDLComponent.getModel(),
                        newMessageOut, "part1", editorPanel.getMessageType(), 
                        editorPanel.getSelectedPartType(), editorPanel.getSelectedElementType());
                if (newPart != null) {
                    FileUtilities.setPart(editorPanel.getMessageType(),
                                        outputFileMessage, newPart.getName());                     
                }                
            }            
        }        
    }  
        
    public static void populate() {
        
    }


}
