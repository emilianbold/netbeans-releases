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

import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;
import javax.xml.namespace.QName;
import org.netbeans.modules.wsdlextensions.file.model.FileAddress;
import org.netbeans.modules.wsdlextensions.file.model.FileBinding;
import org.netbeans.modules.wsdlextensions.file.model.FileConstants;
import org.netbeans.modules.wsdlextensions.file.model.FileMessage;
import org.netbeans.modules.wsdlextensions.file.model.FileOperation;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author skini
 */
public class FileConfigurationEditorComponent 
        implements ExtensibilityElementConfigurationEditorComponent {

    private static FileBindingConfigurationPanel fileEditorPanel = null;
    private WSDLComponent component;
    private QName mQName;
    
    
    public FileConfigurationEditorComponent(QName qName,
            WSDLComponent component) {
        if (fileEditorPanel == null) {
            fileEditorPanel = new FileBindingConfigurationPanel(qName, component);
        } else {
            fileEditorPanel.populateView(qName, component);
        }
        mQName = qName;
        this.component = component;
        fileEditorPanel.setName(getTitle());
    }

    /**
     * Return the main panel
     * @return
     */
    public JPanel getEditorPanel() {
        return fileEditorPanel;
    }

    /**
     * Return the title
     * @return
     */
    public String getTitle() {
        return NbBundle.getMessage(FileConfigurationEditorComponent.class,
                "FileConfigurationEditorPanel.CONFIGURE_TITLE");                                 //NOI18N
    }

    /**
     * Return the Help
     * @return
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Return the action listeners if any
     * @return
     */
    public ActionListener getActionListener() {
        return null;
    }

    /**
     * Commit all changes
     * @return
     */
    public boolean commit() {
        if (!fileEditorPanel.validateMe()) {
            return false;
        }
        if (component instanceof FileAddress) {
            return commitAddress((FileAddress) component);
        } else if (component instanceof FileBinding) {
            return commitBinding((FileBinding) component);
        } else if (component instanceof Port) {
            return commitPort((Port) component);
        } else if (component instanceof FileMessage) {
            return commitMessage((FileMessage) component);
        } else if (component instanceof FileOperation) {
            return commitOperation((FileOperation) component);
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
            fileAddress.setAttribute(FileAddress.ATTR_FILE_ADDRESS,
                    fileEditorPanel.getFileAddress());
            fileAddress.setAttribute(FileAddress.ATTR_FILE_LOCK_NAME,
                    fileEditorPanel.getFileLockName());
            fileAddress.setAttribute(FileAddress.ATTR_FILE_SEQ_NAME,
                    fileEditorPanel.getSeqName());
            fileAddress.setAttribute(FileAddress.ATTR_FILE_WORK_AREA,
                    fileEditorPanel.getWorkArea());
            fileAddress.setAttribute(FileAddress.ATTR_FILE_RELATIVE_PATH,
                    fileEditorPanel.getRelativePath());
            fileAddress.setAttribute(FileAddress.ATTR_FILE_PATH_RELATIVE_TO,
                    fileEditorPanel.getPathRelativeTo());
            Port port = (Port) fileAddress.getParent();
            Binding binding = port.getBinding().get();
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
                            commitInputFileMessage(inputFileMessage);
                        }
                    }
                    
                    BindingOutput bo = bop.getBindingOutput();
                    if (bo != null) {
                        List<FileMessage> outputFileMessages =
                                bo.getExtensibilityElements(FileMessage.class);
                        if (outputFileMessages.size() > 0) {
                            FileMessage outputFileMessage =
                                    outputFileMessages.get(0);
                            commitOutputFileMessage(outputFileMessage);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return false;
        } finally {
            if (wsdlModel.isIntransaction()) {
               wsdlModel.endTransaction(); 
            }                        
            return true;
        }    
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
                fileAddress.setAttribute(FileAddress.ATTR_FILE_ADDRESS,
                        fileEditorPanel.getFileAddress());
                fileAddress.setAttribute(FileAddress.ATTR_FILE_LOCK_NAME,
                        fileEditorPanel.getFileLockName());
                fileAddress.setAttribute(FileAddress.ATTR_FILE_SEQ_NAME,
                        fileEditorPanel.getSeqName());
                fileAddress.setAttribute(FileAddress.ATTR_FILE_WORK_AREA,
                        fileEditorPanel.getWorkArea());
                fileAddress.setAttribute(FileAddress.ATTR_FILE_RELATIVE_PATH,
                        fileEditorPanel.getRelativePath());
                fileAddress.setAttribute(FileAddress.ATTR_FILE_PATH_RELATIVE_TO,
                        fileEditorPanel.getPathRelativeTo());                
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
                            commitInputFileMessage(inputFileMessage);
                        }
                    }
                    BindingOutput bo = bop.getBindingOutput();
                    if (bo != null) {
                        List<FileMessage> outputFileMessages =
                                bo.getExtensibilityElements(FileMessage.class);
                        if (outputFileMessages.size() > 0) {
                            FileMessage outputFileMessage =
                                    outputFileMessages.get(0);
                            commitOutputFileMessage(outputFileMessage);
                        }
                    }
                }
            }      
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return false;
        } finally {
            if (wsdlModel.isIntransaction()) {
               wsdlModel.endTransaction(); 
            }                        
            return true;
        }          
    }
    
    private void commitInputFileMessage(FileMessage inputFileMessage) {
        inputFileMessage.setFileName(fileEditorPanel.getInputFileName());
        inputFileMessage.setPollingInterval(fileEditorPanel.
                getInputPollingInterval());
        inputFileMessage.setFileUseType(fileEditorPanel.getInputUseType());
        inputFileMessage.setFileEncodingStyle(fileEditorPanel.
                getEncodingStyle());
        inputFileMessage.setFileType(fileEditorPanel.getInputFileType());
	inputFileMessage.setFileNameIsPattern(fileEditorPanel.
                getInputFileIsPattern());
        inputFileMessage.setPart(fileEditorPanel.getInputPart());
	inputFileMessage.setRemoveEOL(fileEditorPanel.getInputRemoveEOL());
        if ((fileEditorPanel.getReadWriteMode() == FileConstants.WRITE) ||
                (fileEditorPanel.getReadWriteMode() == FileConstants.
                        READ_WRITE)) {
            inputFileMessage.setAddEOL(fileEditorPanel.getInputAddEOL());
        }
        inputFileMessage.setRecordDelimiter(fileEditorPanel.
                getInputRecordDelimiter());
        inputFileMessage.setMultipleRecordsPerFile(fileEditorPanel.
                getInputMultiRecordsPerFile());
        inputFileMessage.setMaxBytesPerRecord(fileEditorPanel.
                getInputMaxBytesPerRecord());
        inputFileMessage.setArchiveEnabled(fileEditorPanel.
                getInputArchive());
        inputFileMessage.setArchiveDirIsRelative(fileEditorPanel.
                getInputArchiveRelative());
        inputFileMessage.setArchiveDirectory(fileEditorPanel.
                getInputArchiveDirectory());
        if ((fileEditorPanel.getReadWriteMode() == FileConstants.WRITE) ||
                (fileEditorPanel.getReadWriteMode() == FileConstants.
                        READ_WRITE)) {
            inputFileMessage.setProtectEnabled(fileEditorPanel.
                    getInputProtect());
            inputFileMessage.setProtectDirIsRelative(fileEditorPanel.
                    getInputProtectRelative());
            inputFileMessage.setProtectDirectory(fileEditorPanel.
                    getInputProtectDirectory());

            inputFileMessage.setStagingEnabled(fileEditorPanel.getInputStage());
            inputFileMessage.setStagingDirIsRelative(fileEditorPanel.
                    getInputStageRelative());
            inputFileMessage.setStagingDirectory(fileEditorPanel.
                    getInputStageDirectory());
        }
    }
    
    private void commitOutputFileMessage(FileMessage outputFileMessage) {
        outputFileMessage.setFileName(fileEditorPanel.getOutputFileName());
        outputFileMessage.setFileUseType(fileEditorPanel.getOutputUseType());
//        outputFileMessage.setPollingInterval(fileEditorPanel.
//                getOutputPollingInterval());        
        outputFileMessage.setFileEncodingStyle(fileEditorPanel.
                getOutputEncodingStyle());
        outputFileMessage.setFileType(fileEditorPanel.getOutputFileType());
	outputFileMessage.setFileNameIsPattern(fileEditorPanel.
                getOutputFileIsPattern());
        outputFileMessage.setPart(fileEditorPanel.getOutputPart());
	outputFileMessage.setAddEOL(fileEditorPanel.getOutputAddEOL());
        outputFileMessage.setRecordDelimiter(fileEditorPanel.
                getOutputRecordDelimiter());
        outputFileMessage.setMultipleRecordsPerFile(fileEditorPanel.
                getOutputMultiRecordsPerFile());
        outputFileMessage.setMaxBytesPerRecord(fileEditorPanel.
                getOutputMaxBytesPerRecord());

        outputFileMessage.setProtectEnabled(fileEditorPanel.getOutputProtect());
        outputFileMessage.setProtectDirIsRelative(fileEditorPanel.
                getOutputProtectRelative());
        outputFileMessage.setProtectDirectory(fileEditorPanel.
                getOutputProtectDirectory());
            
        outputFileMessage.setStagingEnabled(fileEditorPanel.getOutputStage());
        outputFileMessage.setStagingDirIsRelative(fileEditorPanel.
                getOutputStageRelative());
        outputFileMessage.setStagingDirectory(fileEditorPanel.
                getOutputStageDirectory());
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
    
    /**
     * Check if the model is valid or not
     * @return boolean true if model is valid; otherwise false
     */
    public boolean isValid() {
        return fileEditorPanel.validateContent();
    }
}
