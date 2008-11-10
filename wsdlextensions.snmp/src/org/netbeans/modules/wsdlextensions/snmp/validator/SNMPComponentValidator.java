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
package org.netbeans.modules.wsdlextensions.snmp.validator;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;

import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

import org.netbeans.modules.wsdlextensions.snmp.SNMPConstants;
import org.netbeans.modules.wsdlextensions.snmp.SNMPOperation;
import org.netbeans.modules.wsdlextensions.snmp.SNMPMessage;
import org.netbeans.modules.wsdlextensions.snmp.SNMPBinding;
import org.netbeans.modules.wsdlextensions.snmp.SNMPAddress;


@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.xam.spi.Validator.class)
public class SNMPComponentValidator
        implements Validator {
    
    private static final ResourceBundle mMessages =
            ResourceBundle.getBundle("org.netbeans.modules.wsdlextensions.snmp.validator.Bundle");
    
    private Validation mValidation;
    private ValidationType mValidationType;
    private ValidationResult mValidationResult;
    
    public static final ValidationResult EMPTY_RESULT = 
        new ValidationResult( Collections.EMPTY_SET, 
                Collections.EMPTY_SET);
    
    public SNMPComponentValidator() {}
    
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
                
                int numSNMPBindings = binding.getExtensibilityElements(SNMPBinding.class).size();
                if (numSNMPBindings == 0) {
                    continue;
                }
                
                if (numSNMPBindings > 0 && numSNMPBindings != 1) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            getMessage("SNMPBindingValidation.ONLY_ONE_SNMP_BINDING_ALLOWED",
                                       new Object[]{binding.getName(), 
                                                    new Integer(numSNMPBindings)})));
                }

                Iterator<BindingOperation> bindingOps =
                        binding.getBindingOperations().iterator();
                boolean foundSNMPOp = false;
                int msgCnt = 0;
                while (bindingOps.hasNext()) {
                    BindingOperation bindingOp = bindingOps.next();
                    List <SNMPOperation> snmpOpsList = bindingOp.getExtensibilityElements(SNMPOperation.class);
                    Iterator<SNMPOperation> snmpOps =
                            snmpOpsList.iterator();
                    
                    while (snmpOps.hasNext()) {
                        validate(bindingOp, snmpOps.next());
                    }
                    
                    if(snmpOpsList.size() > 0) {
                        foundSNMPOp = true;
                        if ( !checkSignature(bindingOp) ) {
                            results.add(
                                    new Validator.ResultItem(this,
                                    Validator.ResultType.ERROR,
                                    bindingOp,
                                    getMessage("SNMPBindingValidation.OP_SIG_MISMATCH_BINDING_ABSTRACT", 
                                                new Object[] {binding.getName(),
                                                              bindingOp.getName()})));
                            continue;
                        }
                        BindingInput bindingInput = bindingOp.getBindingInput();
                        if (bindingInput != null) {
                            msgCnt = 0;
                            // assumption:
                            // under <input>, there could be one of the following:
                            // <snmp:message>
                            // but only one is allowed;
                            // 
                            Iterator<SNMPMessage> snmpMessages =
                                    bindingInput.getExtensibilityElements(SNMPMessage.class).iterator();
                            if (snmpMessages != null) {                                    
                                while (snmpMessages.hasNext()) {
                                    msgCnt++;
                                    SNMPMessage snmpMessage = snmpMessages.next();
                                    validate(bindingOp, bindingInput.getInput().get(), snmpMessage);
                                }
                                if ( msgCnt > 1 ) {
                                    results.add(
                                            new Validator.ResultItem(this,
                                            Validator.ResultType.ERROR,
                                            bindingInput,
                                            getMessage("SNMPBindingValidation.ATMOST_ONE_MESSAGE_IN_INPUT",
                                                       new Object [] {bindingOp.getName(), 
                                                                      new Integer(msgCnt),
                                                                      bindingInput.getName()})));
                                }
                            }
                            
                            if (msgCnt == 0 ) {
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        bindingInput,
                                        getMessage("SNMPBindingValidation.NO_MESSAGE_FOUND_IN_INPUT",
                                                  new Object [] {bindingOp.getName(),
                                                                 bindingInput.getName()})));
                            }                            
                        }
                                                    
                        BindingOutput bindingOutput = bindingOp.getBindingOutput();
                        if (bindingOutput != null) {
                            // reset and do output checking
                            msgCnt = 0;

                            Iterator<SNMPMessage> snmpMessages =
                                    bindingOutput.getExtensibilityElements(SNMPMessage.class).iterator();
                            if (snmpMessages != null) {
                                while (snmpMessages.hasNext()) {
                                    msgCnt++;
                                    SNMPMessage snmpMessage = snmpMessages.next();
                                    validate(bindingOp, bindingOutput.getOutput().get(), snmpMessage);
                                }
                                if ( msgCnt > 1 ) {
                                    results.add(
                                            new Validator.ResultItem(this,
                                            Validator.ResultType.ERROR,
                                            bindingOutput,
                                            getMessage("SNMPBindingValidation.ATMOST_ONE_MESSAGE_IN_OUTPUT",
                                                       new Object [] {bindingOp.getName(), 
                                                                      new Integer(msgCnt),
                                                                      bindingOutput.getName()})));
                                }
                            }
                            
                            if (msgCnt == 0 ) {
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        bindingOutput,
                                        getMessage("SNMPBindingValidation.NO_MESSAGE_FOUND_IN_OUTPUT",
                                                  new Object [] {bindingOp.getName(),
                                                                 bindingOutput.getName()})));
                            }                        
                        }                        
                    }
                    
                }
                // there is snmp:binding but no snmp:operation
                if (numSNMPBindings > 0 && !foundSNMPOp) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            getMessage("SNMPBindingValidation.MISSING_SNMP_OPERATION",
                                       new Object[]{binding.getName()})));
                }
                // there is no snmp:binding but there are snmp:operation
                if (numSNMPBindings == 0 && foundSNMPOp) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            getMessage("SNMPBindingValidation.SNMP_OPERATION_WO_SNMP_BINDING",
                                       new Object[]{binding.getName()})));
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
                            int numRelatedSNMPBindings = binding.getExtensibilityElements(SNMPBinding.class).size();
                            List <SNMPAddress> snmpAddressList = port.getExtensibilityElements(SNMPAddress.class);
                            Iterator<SNMPAddress> snmpAddresses = snmpAddressList.iterator();
                            if((numRelatedSNMPBindings > 0) && (snmpAddressList.size()==0)){
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        port,
                                        getMessage("SNMPAddressValidation.MISSING_SNMP_ADDRESS",
                                                   new Object[]{port.getName(), 
                                                                new Integer(numRelatedSNMPBindings)})));                                        
                            }
                            
                            if(snmpAddressList.size() > 1){
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        port,
                                        getMessage("SNMPAddressValidation.ONLY_ONE_SNMP_ADDRESS_ALLOWED",
                                                   new Object[]{port.getName(), 
                                                                new Integer(snmpAddressList.size())})));
                            }
                            while (snmpAddresses.hasNext()) {
                                SNMPAddress snmpAddress = snmpAddresses.next();
                                if (numRelatedSNMPBindings > 0) {
                                    validate(snmpAddress, binding);
                                }
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

    private void validate(SNMPAddress target, Binding binding) {
        // validate connection url
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();
        
        int port = target.getPort();
        boolean isMof = false;
        for (Iterator<BindingOperation> iter = binding.getBindingOperations().iterator(); iter.hasNext(); ) {
            BindingOperation op = iter.next();
            List<SNMPOperation> snmpOps = op.getExtensibilityElements(SNMPOperation.class);
            if (snmpOps.size() > 0) {
                SNMPOperation snmpOp = snmpOps.get(0);
                if (SNMPConstants.MOF.equals(snmpOp.getType())) {
                    isMof = true;
                    break;
                }
            }
        }
        
        if (isMof && port <= 0) {
            results.add(new Validator.ResultItem(this,
                Validator.ResultType.ERROR,
                target,
                getMessage("SNMPAddress.INVALID_PORT",
                    new Object[]{})));
        }

    }

    private void validate(BindingOperation bindingOp, SNMPOperation target) {
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();
        
        // ToDo: validate SNMP operation
        String mep = "in-only";
        boolean hasInput = bindingOp.getBindingInput() != null;
        boolean hasOutput = bindingOp.getBindingOutput() != null;
        
        if (hasInput && hasOutput) {
            mep = "in-out";
        }
        
        String type = target.getType();
        if (type == null || type.equals("")) {
            results.add(new Validator.ResultItem(this,
                Validator.ResultType.ERROR,
                target,
                getMessage("SNMPOperation.TYPE_EMPTY",
                           new Object[] {bindingOp.getName()})));
            return;
        }
        
        String mofId = target.getMofId();
        String adaptationId = target.getAdaptationId();
        String mofIdRef = target.getMofIdRef();
        
        if (type.equals(SNMPConstants.MOF)) {
            if (mofId == null || mofId.equals("")) {
                results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("SNMPOperation.MOFID_EMPTY",
                               new Object[] {bindingOp.getName()})));
            }
            if (adaptationId != null && !adaptationId.equals("")) {
                results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("SNMPOperation.ADAPTATIONID_NOT_NEEDED",
                               new Object[] {bindingOp.getName()})));
            }
            if (mofIdRef != null && !mofIdRef.equals("")) {
                results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("SNMPOperation.MOFIDREF_NOT_NEEDED",
                               new Object[] {bindingOp.getName()})));
            }
        } else if (type.equals(SNMPConstants.ADAPTATION)) {
            if (adaptationId == null || adaptationId.equals("")) {
                results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("SNMPOperation.ADAPTATIONID_EMPTY",
                               new Object[] {bindingOp.getName()})));
            }
            if (mofId != null && !mofId.equals("")) {
                results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("SNMPOperation.MOFID_NOT_NEEDED",
                               new Object[] {bindingOp.getName()})));
            }
            if (mofIdRef != null && !mofIdRef.equals("")) {
                results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("SNMPOperation.MOFIDREF_NOT_NEEDED",
                               new Object[] {bindingOp.getName()})));
            }
        } else if (type.equals(SNMPConstants.PM)) {
            if (mofIdRef == null || mofIdRef.equals("")) {
                results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("SNMPOperation.MOFIDREF_EMPTY",
                               new Object[] {bindingOp.getName()})));
            }
            if (mofId != null && !mofId.equals("")) {
                results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("SNMPOperation.MOFID_NOT_NEEDED",
                               new Object[] {bindingOp.getName()})));
            }
            if (adaptationId != null && !adaptationId.equals("")) {
                results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("SNMPOperation.ADAPTATIONID_NOT_NEEDED",
                               new Object[] {bindingOp.getName()})));
            }
        } else {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("SNMPOperation.UNKNOWN_TYPE",
                        new Object[] {bindingOp.getName(), type})
                    ));
        }
        
    }

    private void validate(BindingOperation bindingOp,
                          OperationParameter opParam, 
                          SNMPMessage target) {
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();
        
        // SNMP validation
        String snmpTrapPart = target.getTrapPart();
        if (snmpTrapPart == null || snmpTrapPart.length() == 0) {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("SNMPMessage.TRAPPART_NOT_SPECIFIED",
                        new Object[] {bindingOp.getName(),
                        (opParam instanceof Input)? "input":"output",
                        opParam.getName()})));
        } else {
            // make sure trapPart references a vald wsdl message part
            if (!referencesValidMessagePart(opParam.getMessage(), snmpTrapPart)) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("SNMPMessage.TRAP_PART_REFERENCES_NON_EXISTENT_PART",
                                   new Object[] {bindingOp.getName(),
                                                 (opParam instanceof Input)? "input":"output",
                                                 opParam.getName(),
                                                 snmpTrapPart,
                                                 opParam.getMessage().getQName()})));                    
            }
        }
        
    }

    private boolean checkSignature(BindingOperation bindingOp/*, Object inputChild, Object outputChild*/) {
        boolean result = true;
        Reference<Operation> opRef = bindingOp.getOperation();
        if ( opRef == null )
            return false;
        Operation op = opRef.get();
        if ( op == null )
            return false;
        if ( (op.getInput() == null && bindingOp.getBindingInput() == null /*&& inputChild == null*/)
         ||
             (op.getInput() != null && bindingOp.getBindingInput() != null /*&& inputChild != null*/)   ) {
            
        }
        else {
            result = false;
        }
            
        if ( (op.getOutput() == null && bindingOp.getBindingOutput() == null /*&& outputChild == null*/)
        ||
             (op.getOutput() != null && bindingOp.getBindingOutput() != null /*&& outputChild != null*/)   ) {
            
        }
        else {
            result = false;
        }
        return result;        
    }
    
    private boolean referencesValidMessagePart (NamedComponentReference<Message> wsdlMessage, 
                                                String partName) {
        
        // Let wsdl validator catch undefined message for operation input or output
        if (wsdlMessage == null || wsdlMessage.get() == null || wsdlMessage.get().getParts() == null) {
            return true;
        }
        
        boolean isValdPartReference = false;
        Iterator<Part> partIter = wsdlMessage.get().getParts().iterator();
        while(partIter.hasNext()) {
            Part p = partIter.next();
            if (p.getName().equals(partName)) {
                isValdPartReference = true;
                break;
            }
        }
        return isValdPartReference;
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
}
