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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.wsdlextensions.file.validator;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
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
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.xam.spi.Validator.class)
public class FileComponentValidator
        implements Validator, FileComponent.Visitor {
        	
    public static final String NUMBER_MARKER = "%d"; // transient sequence
    public static final String TIMESTAMP_MARKER = "%t";
    public static final String GUID_MARKER = "%u";
    public static final String SEQ_REF_EXP = "%\\{([0-9_\\-\\.a-zA-Z]+)\\}"; // named sequence
    public static final String UUID_REGEX="[0-9[abcdef]]{8}-[0-9[abcdef]]{4}-[0-9[abcdef]]{4}-[0-9[abcdef]]{4}-[0-9[abcdef]]{12}";
    public static final Pattern SEQ_REF_EXP_PATT = Pattern.compile(SEQ_REF_EXP);

    /** Utility to get the regular expression to match
     * the date format.
     * Reason this is in its own method is that we can
     * localize the date format recognition pattern in
     * this method only.
     * The pattern we currently support is UTC
     */
    public static final String TIMESTAMP_REGEX = "[0-9]{4,4}(0[1-9]|1[0-2])([0-2][0-9]|3[0-1])(\\-(1[0-9]|2[0-3])\\-[0-5][0-9]\\-[0-5][0-9]\\-[0-9]{0,3})?";

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
            	
                if (binding.getType() == null || binding.getType().get() == null) {
                    continue;
                }
                

                int numFileBindings = binding.getExtensibilityElements(FileBinding.class).size();
                
                if (numFileBindings == 0) {
                    continue;
                }
                
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
                       !target.getPathRelativeTo().equals("Default System Temp Dir") ) {
                try {
                    if ( !Utils.hasMigrationEnvVarRef(target.getPathRelativeTo()) ) {
                            results.add(new Validator.ResultItem(this,
                                    Validator.ResultType.ERROR,
                                    target,
                                    getMessage("FileAddressValidation.invalid_root_path", target.getPathRelativeTo()))); 
                       }
                }
                catch (Exception ex) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            getMessage("FileAddressValidation.Exception_when_searching_varref_in_root_path", new Object[] {target.getPathRelativeTo(), ex}))); 
                }
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
            boolean hasVarRef = false;
            try {
                hasVarRef = Utils.hasMigrationEnvVarRef(target.getFileName());
            }
            catch (Exception ex) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("FileMessageValidation.Exception_searching_varref_in_file_name_pattern", new Object[] {target.getFileName(), ex})));
            }
            String[] decomposed = new String[3];
            FileNamePatternType type = validatePattern(target.getFileName(), decomposed, results, target);
            if ( type == FileNamePatternType.NAME_INVALID && !hasVarRef ) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("FileMessageValidation.invalid_file_name_pattern", target.getFileName())));
            }
        }
        
        // validating: fileType is "text"
         if ( target.getFileType() != null) {
             String fileType = target.getFileType();
             boolean hasVarRef = false;
             try {
                 hasVarRef = Utils.hasMigrationEnvVarRef(fileType);
             }
             catch (Exception ex) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("FileMessageValidation.Exception_when_searching_varref_in_file_type", new Object[] {target.getFileType(), ex})));
             }
             if (!target.getFileType().equals("text")   &&
                 !target.getFileType().equals("binary") &&
                 !hasVarRef ) {
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
    
    private FileNamePatternType validatePattern(String pattern, String[] decomposed, Collection<ResultItem> results, FileMessage target) {
        // decomposed array must be a 3 element string array
        // with:
        // decomposed[0] as string before the pattern
        // decomposed[1] as the pattern it self
        // decomposed[2] as string after the pattern
        if(pattern == null || pattern.trim().length() == 0 ) {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("FileMessageValidation.Invalid_pattern_blank")));
        }

        FileNamePatternType type = FileNamePatternType.NAME_INVALID;
        int index1 = 0;
        int index2 = 0;
        int pcnt = 0;
        String prefix = "";
        String suffix = "";
        String patternFound = null;

        if ( (index1 = pattern.indexOf(NUMBER_MARKER)) >= 0 ) {
            // has at least 1 %d
            index2 = pattern.lastIndexOf(NUMBER_MARKER);
            if ( index1 < index2 ) {
                // more than one occurrence
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("FileMessageValidation.Invalid_pattern_only_1_seq_allowed", pattern)));
            }
            pcnt++;
            type = FileNamePatternType.NAME_WITH_SEQ;
            patternFound = NUMBER_MARKER;
            prefix = index1 == 0 ? "" : pattern.substring(0, index1);
            suffix = index1 + 2 >= pattern.length() ? "" : pattern.substring(index1 + 2);
        }
        
        if ( (index1 = pattern.indexOf(GUID_MARKER)) >= 0 ) {
            // has at least 1 %u
            index2 = pattern.lastIndexOf(GUID_MARKER);
            if ( index1 < index2 ) {
                // more than one occurrence
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("FileMessageValidation.Invalid_pattern_only_1_uuid_allowed", pattern)));
            }
            if ( pcnt > 0 ) {
                // more than one pattern mark
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("FileMessageValidation.Invalid_pattern_only_1_pattern_allowed", pattern)));
            }
            pcnt++;
            type = FileNamePatternType.NAME_WITH_UUID;
            patternFound = GUID_MARKER;
            prefix = index1 == 0 ? "" : pattern.substring(0, index1);
            suffix = index1 + 2 >= pattern.length() ? "" : pattern.substring(index1 + 2);
        }

        if ( (index1 = pattern.indexOf(TIMESTAMP_MARKER)) >= 0 ) {
            // has at least 1 %t
            index2 = pattern.lastIndexOf(TIMESTAMP_MARKER);
            if ( index1 < index2 ) {
                // more than one occurrence
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("FileMessageValidation.Invalid_pattern_only_1_timestamp_allowed", pattern)));
            }
            if ( pcnt > 0 ) {
                // more than one pattern mark
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("FileMessageValidation.Invalid_pattern_only_1_pattern_allowed", pattern)));
            }
            pcnt++;
            type = FileNamePatternType.NAME_WITH_TIMESTAMP;
            patternFound = TIMESTAMP_MARKER;
            prefix = index1 == 0 ? "" : pattern.substring(0, index1);
            suffix = index1 + 2 >= pattern.length() ? "" : pattern.substring(index1 + 2);
        }

        // added for named sequence (persisted sequence)
        if ( (index1 = pattern.indexOf("%{")) >= 0 ) {
            Matcher m = SEQ_REF_EXP_PATT.matcher(pattern);
            if ( m.find() ) {
                patternFound = m.group(1);
                String wholeMatch = m.group();
                if ( pcnt > 0 || m.find() ) {
                    // more than one pattern detected
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            getMessage("FileMessageValidation.Invalid_pattern_only_1_pattern_allowed", pattern)));
                }
                index1 = pattern.indexOf(wholeMatch);
                pcnt++;
                type = FileNamePatternType.NAME_WITH_PERSISTED_SEQ;
                prefix = index1 == 0 ? "" : pattern.substring(0, index1);
                suffix = index1 + wholeMatch.length() >= pattern.length() ? "" : pattern.substring(index1 + wholeMatch.length());
            }
        }

        if ( pcnt == 0 ) {
            // note, if there is env var reference, then it is too early to say
            // the file name does not have marker 
            // postpone to runtime
            boolean hasVarRef = false;
            try {
                hasVarRef = Utils.hasMigrationEnvVarRef(pattern);
            }
            catch (Exception ex) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("FileMessageValidation.Exception_when_searching_varref_in_pattern", new Object[] {pattern, ex})));
            }
            if ( !hasVarRef ) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("FileMessageValidation.Invalid_pattern_no_marker_found", pattern)));
            }
        }
        
        decomposed[0] = prefix;
        decomposed[1] = patternFound;
        decomposed[2] = suffix;
        
        return type;
    }
}
