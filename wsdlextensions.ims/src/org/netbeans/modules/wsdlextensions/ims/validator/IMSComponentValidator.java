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
package org.netbeans.modules.wsdlextensions.ims.validator;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.text.MessageFormat;

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

import org.netbeans.modules.wsdlextensions.ims.model.IMSComponent;
import org.netbeans.modules.wsdlextensions.ims.model.IMSOperation;
import org.netbeans.modules.wsdlextensions.ims.model.IMSMessage;
import org.netbeans.modules.wsdlextensions.ims.model.IMSBinding;
import org.netbeans.modules.wsdlextensions.ims.model.IMSAddress;

/**
 * semantic validation, check WSDL elements & attributes values and
 * any relationship between;
 *
 * @author Sun Microsystems
 */

public class IMSComponentValidator
        implements Validator, IMSComponent.Visitor {

    private static final ResourceBundle mMessages =
            ResourceBundle.getBundle("org.netbeans.modules.wsdlextensions.ims.validator.Bundle");

    private Validation mValidation;

    private ValidationType mValidationType;

	private ValidationResult mValidationResult;

    public static final ValidationResult EMPTY_RESULT =
        new ValidationResult( Collections.EMPTY_SET,
                Collections.EMPTY_SET);

    public IMSComponentValidator() {}

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

                int numIMSBindings = binding.getExtensibilityElements(IMSBinding.class).size();

				if (numIMSBindings == 0) {
					continue;
                }

                if (numIMSBindings > 0 && numIMSBindings != 1) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            mMessages.getString("IMSBindingValidation.ONLY_ONE_IMS_BINDING_ALLOWED")));
                    continue;
                }

                Iterator<BindingOperation> bindingOps =
                        binding.getBindingOperations().iterator();
                boolean foundIMSOp = false;
                while (bindingOps.hasNext()) {
                    BindingOperation bindingOp = bindingOps.next();
                    List imsOpsList = bindingOp.getExtensibilityElements(IMSOperation.class);
                    Iterator<IMSOperation> imsOps =
                            imsOpsList.iterator();

                    while (imsOps.hasNext()) {
                        imsOps.next().accept(this);
                    }

                    if(imsOpsList.size() > 0) {
                        foundIMSOp = true;
                        BindingInput bindingInput = bindingOp.getBindingInput();
                        if (bindingInput != null) {
                            int inputMessageCnt = 0;
                            Iterator<IMSMessage> imsMessages =
                                    bindingInput.getExtensibilityElements(IMSMessage.class).iterator();
                            while (imsMessages.hasNext()) {
                                inputMessageCnt++;
                                IMSMessage imsMessage = imsMessages.next();
                                imsMessage.accept(this);
                            }
                            if ( inputMessageCnt > 1 ) {
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        binding,
                                        mMessages.getString("IMSBindingValidation.ATMOST_ONE_MESSAGE_IN_INPUT") + " " + inputMessageCnt));
                            }
                        }

                        BindingOutput bindingOutput = bindingOp.getBindingOutput();
                        if (bindingOutput != null) {
                            int outputMessageCnt = 0;
                            Iterator<IMSMessage> imsMessages =
                                    bindingOutput.getExtensibilityElements(IMSMessage.class).iterator();
                            while (imsMessages.hasNext()) {
                                outputMessageCnt++;
                                IMSMessage imsMessage = imsMessages.next();
                                imsMessage.accept(this);
                            }
                            if ( outputMessageCnt > 1 ) {
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        binding,
                                        mMessages.getString("IMSBindingValidation.ATMOST_ONE_MESSAGE_IN_OUTPUT") + " " + outputMessageCnt));
                            }
                        }
                    }
                }
                // there is ims:binding but no ims:operation
                if ( numIMSBindings > 0 && !foundIMSOp ) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            mMessages.getString("IMSBindingValidation.MISSING_IMS_OPERATION")));
                }
                // there is no ims:binding but there are ims:operation
                if ( numIMSBindings == 0 && foundIMSOp ) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            mMessages.getString("IMSBindingValidation.IMS_OPERATION_WO_IMS_BINDING")));
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
                            int numRelatedIMSBindings = binding.getExtensibilityElements(IMSBinding.class).size();
                            Iterator<IMSAddress> imsAddresses = port.getExtensibilityElements(IMSAddress.class).iterator();
                            if((numRelatedIMSBindings > 0) && (!imsAddresses.hasNext())){
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        port,
                                        mMessages.getString("IMSExtValidation.MISSING_IMS_ADDRESS")));
                            }

                            if(port.getExtensibilityElements(IMSAddress.class).size() > 1){
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        port,
                                        mMessages.getString("IMSExtValidation.ONLY_ONE_IMSADDRESS_ALLOWED")));
                            }
                            while (imsAddresses.hasNext()) {
                                imsAddresses.next().accept(this);
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

    public void visit(IMSAddress target) {
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();
        String[] stArr = target.getImsServerLocation().split(":");
        // Address url has to be in the form <ims://Server_Name:Port_Number>
        if(!(stArr.length == 3 && stArr[0].equals("ims") && stArr[1].indexOf("//") 
                == 0 && Integer.parseInt(stArr[2])<= 65535)){
            results.add(new Validator.ResultItem(this,
						Validator.ResultType.ERROR,
						target,
						mMessages.getString("IMSAddress.IMS_INVALID_URL")));
        }
    }

	public void visit(IMSBinding target) {
        // for ims binding tag - nothing to validate at this point
    }

    public void visit(IMSOperation target) {
        // for ims operation tag - nothing to validate at this point
    }

    public void visit(IMSMessage target) {
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();
		//check encodingStyle
		String useType = target.getUse();
		String encodingStyle = target.getEncodingStyle();
		if(useType.equals("encoded")){
			if(isNull(encodingStyle)){
				results.add(new Validator.ResultItem(this,
						Validator.ResultType.ERROR,
						target,
						mMessages.getString("IMSMessage.IMS_ENCODING_STYLE_MISSING")));
			}
		}

    }

	//check whether it is null or empty
	private boolean isNull(String val){
		if((val == null) || (val.trim().equals(""))){
			return true;
		} else {
			return false;
		}
	}

	private String getMessage(String key, String param) {
		return getMessage(key, new Object[] { param });
	}

	private String getMessage(String key, Object[] params) {
		String fmt = mMessages.getString(key);
		if (params != null) {
			return MessageFormat.format(fmt, params);
		} else {
			return fmt;
		}
    }
}
