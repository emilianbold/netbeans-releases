/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.wsdlextensions.file.validator;

import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.ResourceBundle;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

import org.netbeans.modules.wsdlextensions.file.model.FileAddress;
import org.netbeans.modules.wsdlextensions.file.model.FileBinding;
import org.netbeans.modules.wsdlextensions.file.model.FileComponent;
import org.netbeans.modules.wsdlextensions.file.model.FileMessage;
import org.netbeans.modules.wsdlextensions.file.model.FileOperation;

/**
 * This class enables semantic validations for
 * File WSDL documents.
 *
 * @author sweng
 */
public class FileComponentValidator
        implements Validator, FileComponent.Visitor {
        	
    private static final ResourceBundle mMessages =
            ResourceBundle.getBundle("org.netbeans.modules.wsdlextensions.file.validator.Bundle");
    
    private Validation mValidation;
    private ValidationType mValidationType;
    private ValidationResult mValidationResult;
    
    public static final ValidationResult EMPTY_RESULT = 
        new ValidationResult( Collections.EMPTY_SET, 
                Collections.EMPTY_SET);
    
    public FileComponentValidator() {}
    
    /**
     * Returns name of this validation service.
     */
    public String getName() {
        return getClass().getName();
    }
    
    /**
     * Validates given model.
     *
     * @param model model to validate.
     * @param validation reference to the validation context.
     * @param validationType the type of validation to perform
     * @return ValidationResult.
     */
    public ValidationResult validate(Model model, Validation validation,
            ValidationType validationType) {
        mValidation = validation;
        mValidationType = validationType;
        
        HashSet<ResultItem> results = new HashSet<ResultItem>();
        HashSet<Model> models = new HashSet<Model>();
        models.add(model);
        mValidationResult = new ValidationResult(results, models);
        
        // Traverse the model
        if (model instanceof WSDLModel) {
            WSDLModel wsdlModel = (WSDLModel)model;
            
            if (model.getState() == State.NOT_WELL_FORMED) {
                return EMPTY_RESULT;
            }
            
            Definitions defs = wsdlModel.getDefinitions();
            Iterator<Binding> bindings = defs.getBindings().iterator();
            
            while (bindings.hasNext()) {
                Binding binding = bindings.next();
            	
                PortType portType = binding.getType().get();
            	if (portType == null) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            getMessage("FileBindingValidation.invalid_null_port_type",
                                       new Object[]{binding.getName()})));
                    continue;
                }
                

                int numFileBindings = binding.getExtensibilityElements(FileBinding.class).size();
                if (numFileBindings > 0 && numFileBindings != 1) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            mMessages.getString("FileBindingValidation.only_one_binding_allowed")));
                }

                Iterator<BindingOperation> bindingOps =
                        binding.getBindingOperations().iterator();
                boolean foundFileOp = false;
                int count = 0;
                while (bindingOps.hasNext()) {
                    BindingOperation bindingOp = bindingOps.next();
                    List fileOpsList = bindingOp.getExtensibilityElements(FileOperation.class);
                    Iterator<FileOperation> fileOps =
                            fileOpsList.iterator();
                    
                    while (fileOps.hasNext()) {
                        fileOps.next().accept(this);
                    }
                    
                    if(fileOpsList.size() > 0) {
                        foundFileOp = true;
                        BindingInput bindingInput = bindingOp.getBindingInput();
                        if (bindingInput != null) {
                            count = 0;
                            Iterator<FileMessage> fileMessages =
                                    bindingInput.getExtensibilityElements(FileMessage.class).iterator();
                            if ( fileMessages != null ) {                                    
                                while (fileMessages.hasNext()) {
                                    count++;
                                    FileMessage fileMessage = fileMessages.next();
                                    fileMessage.accept(this);
                                    additionalFileMessageValidation(bindingOp, bindingInput.getInput().get(), fileMessage);
                                }
                                if ( count > 1 ) {
                                    results.add(
                                            new Validator.ResultItem(this,
                                            Validator.ResultType.ERROR,
                                            bindingOp,
                                            getMessage("FileInputValidation.only_one_message_allowed", bindingOp.getName()) + count));
                                }
                            }
                        }

                        BindingOutput bindingOutput = bindingOp.getBindingOutput();
                        if (bindingOutput != null) {
                            // reset count for output
                            count = 0;
                            Iterator<FileMessage> fileMessages =
                                    bindingOutput.getExtensibilityElements(FileMessage.class).iterator();
                            if ( fileMessages != null ) {
                                while (fileMessages.hasNext()) {
                                    count++;
                                    FileMessage fileMessage = fileMessages.next();
                                    fileMessage.accept(this);
                                    additionalFileMessageValidation(bindingOp, bindingOutput.getOutput().get(), fileMessage);
                                }
                                if ( count > 1 ) {
                                    results.add(
                                            new Validator.ResultItem(this,
                                            Validator.ResultType.ERROR,
                                            bindingOp,
                                            getMessage("FileOutputValidation.only_one_message_allowed", bindingOp.getName()) + count));
                                }
                            }
                        }
                    }
                }
                // validating: file:binding found but no file:operation is defined
                if ( numFileBindings > 0 && !foundFileOp ) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            getMessage("FileOperationValidation.no_operation_defined", binding.getName())));
                }
                // validating: found file:operation but no file:binding is defined
                if ( numFileBindings == 0 && foundFileOp ) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            getMessage("FileBindingValidation.no_binding_defined", binding.getName())));
                }
            }

            Iterator<Service> services = defs.getServices().iterator();
            while (services.hasNext()) {
                Iterator<Port> ports = services.next().getPorts().iterator();
                while (ports.hasNext()) {
                    Port port = ports.next();
                    if(port.getBinding() != null) {
                        Binding binding = port.getBinding().get();
                        if(binding != null) {
                            int numRelatedFileBindings = binding.getExtensibilityElements(FileBinding.class).size();
                            Iterator<FileAddress> fileAddresses = port.getExtensibilityElements(FileAddress.class).iterator();
                            if((numRelatedFileBindings > 0) && (!fileAddresses.hasNext())){
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        port,
                                        getMessage("FileAddressValidation.no_file_address", port.getName())));
                            }
                            
                            if(port.getExtensibilityElements(FileAddress.class).size() > 1){
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        port,
                                        getMessage("FileAddressValidation.only_one_address_allowed", port.getName())));
                            }
                            while (fileAddresses.hasNext()) {
                                fileAddresses.next().accept(this);
                            }
                        }
                    }
                }
            }
        }
        // Clear out our state
        mValidation = null;
        mValidationType = null;
        
        return mValidationResult;
    }

    public void visit(FileAddress target) {
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();
        if (target.getRelativePath()) {
            if (target.getPathRelativeTo() == null) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("FileAddressValidation.missing_root_path"))); 
            } else if (!target.getPathRelativeTo().equals("User Home") &&
                       !target.getPathRelativeTo().equals("Current Working Dir") &&
                       !target.getPathRelativeTo().equals("Default System Temp Dir") &&
                       !isToken(target.getPathRelativeTo())) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("FileAddressValidation.invalid_root_path", target.getPathRelativeTo()))); 
            }
        } else {
            if ( target.getFileDirectory() == null|| 
                 target.getFileDirectory().trim().equals("") ) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("FileAddressValidation.missing_file_directory")));
            }
        }
    }

    public void visit(FileBinding target) {
        // no attributes defined
    }

    public void visit(FileOperation target) {
        // no attributes defined
    }

    public void visit(FileMessage target) {
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();
        // validating: fileName is a required attribute in file:message
        if ( target.getFileName() == null
                || target.getFileName().trim().equals("") ) {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("FileMessageValidation.missing_file_name")));
        }
        
        // validating: if use="encoded", encodingStyle must be specified
        if ( target.getFileUseType() != null && target.getFileUseType().equals("encoded") ) {
        	if ( target.getFileEncodingStyle() == null || 
                     target.getFileEncodingStyle().trim().equals("") ) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("FileMessageValidation.no_encoding_style")));
        	}
        }
        
        // validating: if fileNameIsPattern="true", fileName must be a valid pattern
        if ( target.getFileNameIsPattern()) {
            if ( target.getFileName().indexOf("%t") < 0 &&
                 target.getFileName().indexOf("%d") < 0 &&
                 target.getFileName().indexOf("%u") < 0 &&
                 !isToken(target.getFileName()) ) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("FileMessageValidation.invalid_file_name_pattern", target.getFileName())));
            }
        }
        
        // validating: fileType is "text"
         if ( target.getFileType() != null) {
             if (!target.getFileType().equals("text")   &&
                 !target.getFileType().equals("binary") &&
                 !isToken(target.getFileType())) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("FileMessageValidation.invalid_file_type", target.getFileType())));
             }
             if (target.getFileType().equals("binary")) {
                 results.add(new Validator.ResultItem(this,
                        Validator.ResultType.WARNING,
                        target,
                        getMessage("FileMessageValidation.unsupported_file_type", target.getFileType())));
             }
        }
        
        // validating: handling multiple records
        if (target.getMultipleRecordsPerFile()) {
            if (target.getRecordDelimiter() == null) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.WARNING,
                        target,
                        getMessage("FileMessageValidation.no_delimiter_defined")));
            }
        }
        
        // validating: maxBytesPerRecord
        if (target.getMaxBytesPerRecord() != null && 
            target.getMaxBytesPerRecord().longValue() < 0) {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("FileMessageValidation.invalid_max_bytes", "" + target.getMaxBytesPerRecord())));
        }
        
        // validating: pollingInterval
        if (target.getPollingInterval() != null &&
            target.getPollingInterval().longValue() < 0) {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("FileMessageValidation.invalid_polling_interval", "" + target.getPollingInterval())));
        }
    }

    private void additionalFileMessageValidation(BindingOperation bindingOp, OperationParameter param, FileMessage target) {
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();
        
        String part = target.getPart();  
            if (part != null) {
                // make sure textPart references a vald wsdl message part
                if (!matchesValidMessagePart(param.getMessage(), part)) {
                    results.add(new Validator.ResultItem(this,
                                Validator.ResultType.ERROR,
                                target,
                                getMessage("FileMessageValidation.part_name_not_matching",
                                       new Object[] {bindingOp.getName(),
                                                     (param instanceof Input)? "input":"output",
                                                     part,
                                                     param.getMessage().getQName()})));
                }
            }
    }
   
    private String getMessage(String key) {
        return mMessages.getString(key);
    }

    private String getMessage(String key, String param) {
        return getMessage(key, new Object[] {param});
    }
    
    private String getMessage(String key, Object[] params) {
        String fmt = mMessages.getString(key);
        if ( params != null ) {
            return MessageFormat.format(fmt, params);
        } else {
            return fmt;
        }
    }
    
    private boolean matchesValidMessagePart(NamedComponentReference<Message> wsdlMessage, 
                                            String partName) {
        boolean isValid = false;
        Iterator<Part> partIter = wsdlMessage.get().getParts().iterator();
        while(partIter.hasNext()) {
            Part p = partIter.next();
            if (p.getName().equals(partName)) {
                isValid = true;
                break;
            }
        }
        return isValid;
    }
    
    private boolean isToken(String value) {
        if (value.startsWith("${") && value.endsWith("}")) {
            return true;
        }
        
        return false;
    }
}
