/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.wsdlextensions.rest.validator;

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

import org.netbeans.modules.wsdlextensions.rest.RESTComponent;
import org.netbeans.modules.wsdlextensions.rest.RESTOperation;
import org.netbeans.modules.wsdlextensions.rest.RESTBinding;
import org.netbeans.modules.wsdlextensions.rest.RESTAddress;

/**
 * semantic validation, check WSDL elements & attributes values and 
 * any relationship between;
 *
 * @author raghunadh.teegavarapu@sun.com
 */
public class RESTComponentValidator implements Validator, RESTComponent.Visitor {

    private static final String REST_URL_PREFIX = "rest://";
    private static final String REST_URL_LOGIN_HOST_DELIM = "@";
    private static final String REST_URL_COLON_DELIM = ":";
    private static final String REST_URL_PATH_DELIM = "/";
    private static final String RESET_RECOURSE_ACTION = "Reset";
    private static final String RESEND_RECOURSE_ACTION = "Resend";
    private static final String SUSPEND_RECOURSE_ACTION = "Suspend";
    private static final String ERROR_RECOURSE_ACTION = "Error";
    private static final String SKIPMESSAGE_RECOURSE_ACTION = "Skipmessage";
    private static final String PATTERN = "(\\d+);(\\d+)";
    private static final ResourceBundle mMessages =
            ResourceBundle.getBundle("org.netbeans.modules.wsdlextensions.rest.validator.Bundle");
    private Validation mValidation;
    private ValidationType mValidationType;
    private ValidationResult mValidationResult;
    private HashSet<String> mMessageTypesSet;
    public static final ValidationResult EMPTY_RESULT =
            new ValidationResult(Collections.EMPTY_SET, Collections.EMPTY_SET);

    public RESTComponentValidator() {
    }

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
        mMessageTypesSet = new HashSet<String>();
        models.add(model);
        mValidationResult = new ValidationResult(results, models);

        // Traverse the model
        if (model instanceof WSDLModel) {
            WSDLModel wsdlModel = (WSDLModel) model;

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

                int numRESTBindings = binding.getExtensibilityElements(RESTBinding.class).size();
                if (numRESTBindings == 0) {
                    continue;
                }

                if (numRESTBindings > 0 && numRESTBindings != 1) {
                    results.add(new Validator.ResultItem(this, Validator.ResultType.ERROR, binding,
                            mMessages.getString("RESTBindingValidation.ONLY_ONE_REST_BINDING_ALLOWED")));
                }

                Iterator<BindingOperation> bindingOps = binding.getBindingOperations().iterator();
                boolean foundRESTOp = false;
                while (bindingOps.hasNext()) {
                    BindingOperation bindingOp = bindingOps.next();
                    List<RESTOperation> restOpsList = bindingOp.getExtensibilityElements(RESTOperation.class);
                    Iterator<RESTOperation> restOps = restOpsList.iterator();

                    while (restOps.hasNext()) {
                        restOps.next().accept(this);
                    }

                    if (restOpsList.size() > 0) {
                        foundRESTOp = true;
                        BindingInput bindingInput = bindingOp.getBindingInput();
                        if (bindingInput != null) {
//                            int inputMessageCnt = 0;
//                            Iterator<RESTMessage> restMessages = bindingInput.getExtensibilityElements(RESTMessage.class).iterator();
//                            while (restMessages.hasNext()) {
//                                inputMessageCnt++;
//                                RESTMessage restMessage = restMessages.next();
//                                restMessage.accept(this);
//                            }
//                            if (inputMessageCnt > 1) {
//                                results.add(new Validator.ResultItem(this, Validator.ResultType.ERROR, binding,
//                                        mMessages.getString("RESTBindingValidation.ATMOST_ONE_MESSAGE_IN_INPUT") + inputMessageCnt));
//                            }
//                            if (inputMessageCnt == 0) {
//                                results.add(new Validator.ResultItem(
//                                        this,
//                                        Validator.ResultType.ERROR,
//                                        binding,
//                                        mMessages.getString("RESTBindingValidation.NO_EXTENSIBILITY_ELEMENT_FOUND_IN_INPUT")));
//                            }
                        }

                    }
                }
                // there is rest:binding but no rest:operation
                if (numRESTBindings > 0 && !foundRESTOp) {
                    results.add(new Validator.ResultItem(this, Validator.ResultType.ERROR, binding,
                            mMessages.getString("RESTBindingValidation.MISSING_REST_OPERATION")));
                }
                // there is no rest:binding but there are rest:operation
                if (numRESTBindings == 0 && foundRESTOp) {
                    results.add(new Validator.ResultItem(this, Validator.ResultType.ERROR, binding,
                            mMessages.getString("RESTBindingValidation.REST_OPERATION_WO_REST_BINDING")));
                }
            }

            Iterator<Service> services = defs.getServices().iterator();
            while (services.hasNext()) {
                Iterator<Port> ports = services.next().getPorts().iterator();
                while (ports.hasNext()) {
                    Port port = ports.next();
                    if (port.getBinding() != null) {
                        Binding binding = port.getBinding().get();
                        if (binding != null) {
                            int numRelatedRESTBindings = binding.getExtensibilityElements(RESTBinding.class).size();
                            Iterator<RESTAddress> restAddresses = port.getExtensibilityElements(RESTAddress.class).iterator();
                            if ((numRelatedRESTBindings > 0) && (!restAddresses.hasNext())) {
                                results.add(new Validator.ResultItem(this, Validator.ResultType.ERROR, port,
                                        mMessages.getString("RESTExtValidation.MISSING_REST_ADDRESS")));
                            }

                            if (port.getExtensibilityElements(RESTAddress.class).size() > 1) {
                                results.add(new Validator.ResultItem(this, Validator.ResultType.ERROR, port,
                                        mMessages.getString("RESTExtValidation.ONLY_ONE_RESTADDRESS_ALLOWED")));
                            }
                            while (restAddresses.hasNext()) {
                                restAddresses.next().accept(this);
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

    public void visit(RESTAddress target) {
        // for rest address tag - nothing to validate at this point
    }

    private boolean isAToken(String name, RESTComponent target) {
        Collection<ResultItem> results = mValidationResult.getValidationResult();

        boolean isToken = false;

        if (name != null && name.startsWith("${")) {
            isToken = true;
            if (!name.endsWith("}")) {
                results.add(new Validator.ResultItem(this, Validator.ResultType.ERROR, target, getMessage(
                        "RESTExtValidation.INVALID_ENVIRONMENT_TOKEN_NAME", new Object[]{name})));
            }
        }

        return isToken;
    }

    public void visit(RESTBinding target) {
        // for rest binding tag - nothing to validate at this point
    }

    public void visit(RESTOperation target) {
        // FIXME: validate property file content
//        Collection<ResultItem> results = mValidationResult.getValidationResult();
//        String messageType = target.getMessageType();
//        // message type is optional according to the rest-ext.xsd
////        if (!nonEmptyString(messageType)) {
////            results.add(new Validator.ResultItem(this, Validator.ResultType.ERROR, target,
////                    mMessages.getString("RESTMessage.INVALID_REST_MessageType")));
////            return;
////        }
//        if (messageType == null) {
//            ; // do nothing
//        } else if (!mMessageTypesSet.contains(messageType)) {
//            mMessageTypesSet.add(messageType);
//        } else {
//            results.add(new Validator.ResultItem(this, Validator.ResultType.ERROR, target,
//                    getMessage("RESTMessage.DUPLICATE_REST_MessageType", new Object[]{messageType})));
//        }
    }

    private boolean nonEmptyString(String strToTest) {
        boolean nonEmpty = false;
        if (strToTest != null && strToTest.length() > 0) {
            nonEmpty = true;
        }
        return nonEmpty;
    }

    private String getMessage(String key, String param) {
        return getMessage(key, new Object[]{param});
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
