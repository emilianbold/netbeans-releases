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
package org.netbeans.modules.wsdlextensions.ldap.validator;

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
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

import org.netbeans.modules.wsdlextensions.ldap.LDAPComponent;
import org.netbeans.modules.wsdlextensions.ldap.LDAPOperation;
import org.netbeans.modules.wsdlextensions.ldap.LDAPOperationInput;
import org.netbeans.modules.wsdlextensions.ldap.LDAPOperationOutput;
import org.netbeans.modules.wsdlextensions.ldap.LDAPBinding;
import org.netbeans.modules.wsdlextensions.ldap.LDAPAddress;


/**
 *
 * @author 
 */
public class LDAPComponentValidator
        implements Validator, LDAPComponent.Visitor {
    
    private static final String LDAP_URL_PREFIX = "ldap";
    private static final String LDAP_URL_LOGIN_HOST_DELIM = "@";
    private static final String LDAP_URL_COLON_DELIM = ":";
    private static final String LDAP_URL_PATH_DELIM = "/";

    private static final ResourceBundle mMessages =
            ResourceBundle.getBundle("org.netbeans.modules.wsdlextensions.ldap.validator.Bundle");
    
    private Validation mValidation;
    private ValidationType mValidationType;
    private ValidationResult mValidationResult;
    
    private String mopType = null;
    
    @SuppressWarnings("unchecked")
    public static final ValidationResult EMPTY_RESULT = 
        new ValidationResult( Collections.EMPTY_SET, 
                Collections.EMPTY_SET);
    
    public LDAPComponentValidator() {}
    
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
    @SuppressWarnings("unchecked")
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
                // bindings port type will be validated - generically by WSDL editor
                // so don't need to bother about it.
               
			    if (binding.getType() == null || binding.getType().get() == null) {
                    continue;
                }
                
               int numLDAPBindings = binding.getExtensibilityElements(LDAPBinding.class).size();
               if (numLDAPBindings == 0) {
                    continue;
               }
                
				if (numLDAPBindings > 0 && numLDAPBindings != 1) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            mMessages.getString("LDAPBindingValidation.ONLY_ONE_LDAP_BINDING_ALLOWED")));
                }

                Iterator<BindingOperation> bindingOps =
                        binding.getBindingOperations().iterator();
                
                boolean foundLDAPOp = false;
                while (bindingOps.hasNext()) {
                    BindingOperation bindingOp = bindingOps.next();                    
                    List ldapOpsList = bindingOp.getExtensibilityElements(LDAPOperation.class);
                    Iterator<LDAPOperation> ldapOps =
                            ldapOpsList.iterator();                    
                    while (ldapOps.hasNext()) {
                        ldapOps.next().accept(this);
                    }                    
                    if(ldapOpsList.size() > 0) {
                        foundLDAPOp = true;
                        BindingInput bindingInput = bindingOp.getBindingInput();
                        if (bindingInput != null) {
                            int inputCnt = 0;
                            Iterator<LDAPOperationInput> ldapInput =
                                    bindingInput.getExtensibilityElements(LDAPOperationInput.class).iterator();
                            while (ldapInput.hasNext()) {
                                inputCnt++;
                                LDAPOperationInput ldapinput = ldapInput.next();
                                ldapinput.accept(this);									
                            }
                            if ( inputCnt > 1 ) {
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        binding,
                                        mMessages.getString("LDAPBindingValidation.ATMOST_ONE_TRANSFER_IN_INPUT") + inputCnt));
                            }
                        }
                        
                        BindingOutput bindingOutput = bindingOp.getBindingOutput();
                        if (bindingOutput != null) {
                            int outputCnt = 0;
                            Iterator<LDAPOperationOutput> ldapOuput =
                                    bindingOutput.getExtensibilityElements(LDAPOperationOutput.class).iterator();
                            while (ldapOuput.hasNext()) {
                                outputCnt++;
                                LDAPOperationOutput ldapoutput = ldapOuput.next();
                                ldapoutput.accept(this);
                            }
                            if ( outputCnt > 1 ) {
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        binding,
                                        mMessages.getString("LDAPBindingValidation.ATMOST_ONE_TRANSFER_IN_OUTPUT") + outputCnt));
                            }
                        }
                    }
                }
                // there is ldap:binding but no ldap:operation
                if ( numLDAPBindings > 0 && !foundLDAPOp ) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            mMessages.getString("LDAPBindingValidation.MISSING_LDAP_OPERATION")));
                }
                // there is no ldap:binding but there are ldap:operation
                if ( numLDAPBindings == 0 && foundLDAPOp ) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            mMessages.getString("LDAPBindingValidation.LDAP_OPERATION_WO_LDAP_BINDING")));
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
                            int numRelatedLDAPBindings = binding.getExtensibilityElements(LDAPBinding.class).size();
                            Iterator<LDAPAddress> ldapAddresses = port.getExtensibilityElements(LDAPAddress.class).iterator();
                            if((numRelatedLDAPBindings > 0) && (!ldapAddresses.hasNext())){
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        port,
                                        mMessages.getString("LDAPExtValidation.MISSING_LDAP_ADDRESS")));
                            }
                            
                            if(port.getExtensibilityElements(LDAPAddress.class).size() > 1){
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        port,
                                        mMessages.getString("LDAPExtValidation.ONLY_ONE_LDAPADDRESS_ALLOWED")));
                            }
                            while (ldapAddresses.hasNext()) {
                                ldapAddresses.next().accept(this);
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

    public void visit(LDAPAddress target) {
    }

    public void visit(LDAPBinding target) {
    }

    public void visit(LDAPOperation target) {
    }

    public void visit(LDAPOperationInput target) {
    }//input

		public void visit(LDAPOperationOutput target) {
    }//output
}