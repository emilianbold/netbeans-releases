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
package org.netbeans.modules.wsdlextensions.smtp.validator;

import org.netbeans.modules.wsdlextensions.smtp.SMTPBinding;
import java.net.URI;
import java.net.URL;
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
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Port;

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

import org.netbeans.modules.wsdlextensions.smtp.SMTPComponent;
import org.netbeans.modules.wsdlextensions.smtp.SMTPOperation;
import org.netbeans.modules.wsdlextensions.smtp.SMTPInput;
//import com.sun.jbi.ui.devtool.wsdl.smtp.SMTPBinding;
import org.netbeans.modules.wsdlextensions.smtp.SMTPAddress;
import org.netbeans.modules.wsdlextensions.smtp.validator.SMTPAddressURL;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Part;


/**
 * semantic validation, check WSDL elements & attributes values and 
 * any relationship between;
 *
 * @author Sainath Adiraju
 */
public class SMTPComponentValidator
        implements Validator, SMTPComponent.Visitor {
    
  //  private static final String SMTP_URL_PREFIX = "smtp://";
 //   private static final String SMTP_URL_LOGIN_HOST_DELIM = "@";
  //  private static final String SMTP_URL_COLON_DELIM = ":";
  

    private static final ResourceBundle mMessages =
            ResourceBundle.getBundle("org.netbeans.modules.wsdlextensions.smtp.validator.Bundle");
    
    private Validation mValidation;
    private ValidationType mValidationType;
    private ValidationResult mValidationResult;
    
    public static final ValidationResult EMPTY_RESULT = 
        new ValidationResult( Collections.EMPTY_SET, 
                Collections.EMPTY_SET);
    
    public SMTPComponentValidator() {}
    
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


                int numSMTPBindings = binding.getExtensibilityElements(SMTPBinding.class).size();

				if (numSMTPBindings == 0) {
                    continue;
                }

                if (numSMTPBindings > 0 && numSMTPBindings != 1) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            mMessages.getString("SMTPBindingValidation.ONLY_ONE_SMTP_BINDING_ALLOWED")));
                } 

                Iterator<BindingOperation> bindingOps =
                        binding.getBindingOperations().iterator();
                boolean foundSMTPOp = false;
                while (bindingOps.hasNext()) {
                    BindingOperation bindingOp = bindingOps.next();
                    List smtpOpsList = bindingOp.getExtensibilityElements(SMTPOperation.class);
                    Iterator<SMTPOperation> smtpOps =
                            smtpOpsList.iterator();
                    
                    while (smtpOps.hasNext()) {
                        smtpOps.next().accept(this);
                    }
                    
                    if(smtpOpsList.size() > 0) {
                        foundSMTPOp = true;
                        BindingInput bindingInput = bindingOp.getBindingInput();
                        if (bindingInput != null) {
                            int inputMessageCnt = 0;
                            Iterator<SMTPInput> smtpInput =
                                    bindingInput.getExtensibilityElements(SMTPInput.class).iterator();
                            while (smtpInput.hasNext()) {
                                inputMessageCnt++;
                                SMTPInput smtpInputEle = smtpInput.next();
                                smtpInputEle.accept(this);
                                validate(smtpInputEle, bindingInput.getInput().get());
                                //smtpInputEle.accept(this,bindingInput.getInput().get());
                            }
                            if ( inputMessageCnt > 1 ) {
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        binding,
                                        mMessages.getString("SMTPBindingValidation.ATMOST_ONE_Message_IN_INPUT") + inputMessageCnt));
                            }
                        }
                        
                        
                    }
                }
                // there is smtp:binding but no smtp:operation
                if ( numSMTPBindings > 0 && !foundSMTPOp ) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            mMessages.getString("SMTPBindingValidation.MISSING_SMTP_OPERATION")));
                }
                // there is no smtp:binding but there are smtp:operation
                if ( numSMTPBindings == 0 && foundSMTPOp ) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            mMessages.getString("SMTPBindingValidation.SMTP_OPERATION_WO_SMTP_BINDING")));
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
                            int numRelatedSMTPBindings = binding.getExtensibilityElements(SMTPBinding.class).size();
                            Iterator<SMTPAddress> smtpAddresses = port.getExtensibilityElements(SMTPAddress.class).iterator();
                            if((numRelatedSMTPBindings > 0) && (!smtpAddresses.hasNext())){
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        port,
                                        mMessages.getString("SMTPExtValidation.MISSING_SMTP_ADDRESS")));
                            }
                            
                            if(port.getExtensibilityElements(SMTPAddress.class).size() > 1){
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        port,
                                        mMessages.getString("SMTPExtValidation.ONLY_ONE_SMTPADDRESS_ALLOWED")));
                            }
                            while (smtpAddresses.hasNext()) {
                                smtpAddresses.next().accept(this);
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

    public void visit(SMTPAddress target) {
        // validate the following:
        // (1) attribute 'url' has the right syntax: i.e. mailto:sainath.adirjau@sun.com
        // 
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();
        
        SMTPAddressURL url = new SMTPAddressURL();
        try{
        url.unmarshal(results, this, target);
        }catch(Exception ex){
         
    }
    }
    public void visit(SMTPBinding target) {
        // for smtp binding tag - nothing to validate at this point
   }

    public void visit(SMTPOperation target) {
        // for smtp operation tag - nothing to validate at this point
    }
    public void visit(SMTPInput target){
     //nothing to validate
    }
    private void validate (SMTPInput target,OperationParameter opParam){
    	
    	Collection<ResultItem> results = mValidationResult.getValidationResult();
    	String  from = target.getFrom();

        if(from != null){
	if (from==""){
                results.add(new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        target,
                                        mMessages.getString("SMTPINPUTValidation.INVALID_FROM_ADDRESS")));
		}else if (!referencesValidMessagePart(opParam.getMessage(), from)) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            mMessages.getString("SMTPINPUTValidation.INVALID_PART_NAME")));                 
               }
               
        }
		
	String to = target.getTo();
        if(to != null){
	if(to==""){
                results.add(new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        target,
                                        mMessages.getString("SMTPINPUTValidation.INVALID_TO_ADDRESS")));
		}else if (!referencesValidMessagePart(opParam.getMessage(), to)) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            mMessages.getString("SMTPINPUTValidation.INVALID_PART_NAME")));
        }    
        }
        String cc = target.getCc();
        if(cc != null){
        if(cc==""){
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            mMessages.getString("SMTPINPUTValidation.INVALID_CC_ADDRESS")));
	           }else if (!referencesValidMessagePart(opParam.getMessage(), cc)) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            mMessages.getString("SMTPINPUTValidation.INVALID_PART_NAME")));
                   }
        }
        String bcc = target.getBcc();
        if(bcc != null){
        if(bcc==""){
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            mMessages.getString("SMTPINPUTValidation.INVALID_BCC_ADDRESS")));
                    }else if (!referencesValidMessagePart(opParam.getMessage(), bcc)) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            mMessages.getString("SMTPINPUTValidation.INVALID_PART_NAME")));
                    }
        }
        String message = target.getMessageName();
        if(message != null){
        if(message==""){
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            mMessages.getString("SMTPINPUTValidation.INVALID_BCC_ADDRESS")));
                    }else if (!referencesValidMessagePart(opParam.getMessage(), message)) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            mMessages.getString("SMTPINPUTValidation.INVALID_PART_NAME")));
                    }
        }
        String subject = target.getSubjectName();
        if(subject != null){
        if(subject==""){
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            mMessages.getString("SMTPINPUTValidation.INVALID_BCC_ADDRESS")));
                    }else if (!referencesValidMessagePart(opParam.getMessage(), subject)) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            mMessages.getString("SMTPINPUTValidation.INVALID_PART_NAME")));
                    }
        }
        String charset = target.getCharSet();
        if(charset != null){
        if(charset==""){
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            mMessages.getString("SMTPINPUTValidation.INVALID_BCC_ADDRESS")));
                    }else if (!referencesValidMessagePart(opParam.getMessage(), charset)) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            mMessages.getString("SMTPINPUTValidation.INVALID_PART_NAME")));
                    }
        }
        
        String use = target.getUse();
        String literal = "literal";
        String encodingStyle = target.getEncodingStyle();
        if(use != null && !"".equals(use) ){
           if(  !literal.equals(use) && encodingStyle == null)
                results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            mMessages.getString("SMTPINPUTValidation.ENCODING_STYLE_NOT_SPECIFIED")));
        }
        if(encodingStyle != null && !"".equals(encodingStyle)){
            if(use == null || literal.equals(use))
             results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            mMessages.getString("SMTPINPUTValidation.ENCODING_USE_NOT_SPECIFIED")));
       }
        
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

	private boolean nonEmptyString(String strToTest) {
        boolean nonEmpty = false;
        if (strToTest != null && strToTest.length() > 0) {
            nonEmpty = true;
        }
        return nonEmpty;
    }
}
