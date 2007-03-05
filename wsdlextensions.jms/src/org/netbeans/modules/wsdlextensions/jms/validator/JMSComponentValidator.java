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
package org.netbeans.modules.wsdlextensions.jms.validator;

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
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

import org.netbeans.modules.wsdlextensions.jms.JMSComponent;
import org.netbeans.modules.wsdlextensions.jms.JMSConstants;
import org.netbeans.modules.wsdlextensions.jms.JMSOperation;
import org.netbeans.modules.wsdlextensions.jms.JMSOptions;
import org.netbeans.modules.wsdlextensions.jms.JMSOption;
import org.netbeans.modules.wsdlextensions.jms.JMSMessage;
import org.netbeans.modules.wsdlextensions.jms.JMSBinding;
import org.netbeans.modules.wsdlextensions.jms.JMSAddress;
import org.netbeans.modules.wsdlextensions.jms.JMSMapMessage;
import org.netbeans.modules.wsdlextensions.jms.JMSMapMessagePart;
import org.netbeans.modules.wsdlextensions.jms.JMSProperties;
import org.netbeans.modules.wsdlextensions.jms.JMSProperty;

/**
 * JMSComponentValidator
 * semantic validation, check WSDL elements & attributes values and 
 * any relationship between;
 *
 * 
 */
public class JMSComponentValidator
        implements Validator {
    
    private static final ResourceBundle mMessages =
            ResourceBundle.getBundle("org.netbeans.modules.wsdlextensions.jms.validator.Bundle");
    
    private Validation mValidation;
    private ValidationType mValidationType;
    private ValidationResult mValidationResult;
    
    public static final ValidationResult EMPTY_RESULT = 
        new ValidationResult( Collections.EMPTY_SET, 
                Collections.EMPTY_SET);
    
    public JMSComponentValidator() {}
    
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
                
                int numJMSBindings = binding.getExtensibilityElements(JMSBinding.class).size();
                if (numJMSBindings == 0) {
                    continue;
                }
                
                if (numJMSBindings > 0 && numJMSBindings != 1) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            getMessage("JMSBindingValidation.ONLY_ONE_JMS_BINDING_ALLOWED",
                                       new Object[]{binding.getName(), 
                                                    new Integer(numJMSBindings)})));
                }

                Iterator<BindingOperation> bindingOps =
                        binding.getBindingOperations().iterator();
                boolean foundJMSOp = false;
                int msgCnt = 0;
                while (bindingOps.hasNext()) {
                    BindingOperation bindingOp = bindingOps.next();
                    List <JMSOperation> jmsOpsList = bindingOp.getExtensibilityElements(JMSOperation.class);
                    Iterator<JMSOperation> jmsOps =
                            jmsOpsList.iterator();
                    
                    while (jmsOps.hasNext()) {
                        validate(bindingOp, jmsOps.next());
                    }
                    
                    if(jmsOpsList.size() > 0) {
                        foundJMSOp = true;
                        if ( !checkSignature(bindingOp) ) {
                            results.add(
                                    new Validator.ResultItem(this,
                                    Validator.ResultType.ERROR,
                                    bindingOp,
                                    getMessage("JMSBindingValidation.OP_SIG_MISMATCH_BINDING_ABSTRACT", 
                                                new Object[] {binding.getName(),
                                                              bindingOp.getName()})));
                            continue;
                        }
                        BindingInput bindingInput = bindingOp.getBindingInput();
                        if (bindingInput != null) {
                            msgCnt = 0;
                            // assumption:
                            // under <input>, there could be one of the following:
                            // <jms:message>
                            // but only one is allowed;
                            // 
                            Iterator<JMSMessage> jmsMessages =
                                    bindingInput.getExtensibilityElements(JMSMessage.class).iterator();
                            if ( jmsMessages != null ) {                                    
                                while (jmsMessages.hasNext()) {
                                    msgCnt++;
                                    JMSMessage jmsMessage = jmsMessages.next();
                                    validate(bindingOp, bindingInput.getInput().get(), jmsMessage);
                                }
                                if ( msgCnt > 1 ) {
                                    results.add(
                                            new Validator.ResultItem(this,
                                            Validator.ResultType.ERROR,
                                            bindingInput,
                                            getMessage("JMSBindingValidation.ATMOST_ONE_MESSAGE_IN_INPUT",
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
                                        getMessage("JMSBindingValidation.NO_MESSAGE_FOUND_IN_INPUT",
                                                  new Object [] {bindingOp.getName(),
                                                                 bindingInput.getName()})));
                            }                            
                        }
                                                    
                        BindingOutput bindingOutput = bindingOp.getBindingOutput();
                        if (bindingOutput != null) {
                            // reset and do output checking
                            msgCnt = 0;

                            Iterator<JMSMessage> jmsMessages =
                                    bindingOutput.getExtensibilityElements(JMSMessage.class).iterator();
                            if ( jmsMessages != null ) {
                                while (jmsMessages.hasNext()) {
                                    msgCnt++;
                                    JMSMessage jmsMessage = jmsMessages.next();
                                    validate(bindingOp, bindingOutput.getOutput().get(), jmsMessage);
                                }
                                if ( msgCnt > 1 ) {
                                    results.add(
                                            new Validator.ResultItem(this,
                                            Validator.ResultType.ERROR,
                                            bindingOutput,
                                            getMessage("JMSBindingValidation.ATMOST_ONE_MESSAGE_IN_OUTPUT",
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
                                        getMessage("JMSBindingValidation.NO_MESSAGE_FOUND_IN_OUTPUT",
                                                  new Object [] {bindingOp.getName(),
                                                                 bindingOutput.getName()})));
                            }                        
                        }                        
                    }
                    
                    // check to ensure options are defined at the right level
                    List <JMSOptions> jmsOptions = bindingOp.getExtensibilityElements(JMSOptions.class);
                    if (jmsOptions.size() > 0) {
                            results.add(
                                    new Validator.ResultItem(this,
                                    Validator.ResultType.ERROR,
                                    jmsOptions.get(0),
                                    getMessage("JMSBindingValidation.IMPROPER_USAGE_OF_OPTIONS", 
                                                new Object[] {binding.getName(),
                                                              jmsOptions.size()})));
                    }
                    
                }
                // there is jms:binding but no jms:operation
                if ( numJMSBindings > 0 && !foundJMSOp ) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            getMessage("JMSBindingValidation.MISSING_JMS_OPERATION",
                                       new Object[]{binding.getName()})));
                }
                // there is no jms:binding but there are jms:operation
                if ( numJMSBindings == 0 && foundJMSOp ) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            getMessage("JMSBindingValidation.JMS_OPERATION_WO_JMS_BINDING",
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
                            int numRelatedJMSBindings = binding.getExtensibilityElements(JMSBinding.class).size();
                            List <JMSAddress> jmsAddressList = port.getExtensibilityElements(JMSAddress.class);
                            Iterator<JMSAddress> jmsAddresses = jmsAddressList.iterator();
                            if((numRelatedJMSBindings > 0) && (jmsAddressList.size()==0)){
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        port,
                                        getMessage("JMSAddressValidation.MISSING_JMS_ADDRESS",
                                                   new Object[]{port.getName(), 
                                                                new Integer(numRelatedJMSBindings)})));                                        
                            }
                            
                            if(jmsAddressList.size() > 1){
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        port,
                                        getMessage("JMSAddressValidation.ONLY_ONE_JMS_ADDRESS_ALLOWED",
                                                   new Object[]{port.getName(), 
                                                                new Integer(jmsAddressList.size())})));
                            }
                            while (jmsAddresses.hasNext()) {
                                validate(jmsAddresses.next());
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

    private void validate(JMSAddress target) {
        // validate connection url
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();
        
        String connectionURL = target.getConnectionURL();
        
        if (!isAToken(connectionURL, target)) {
        
            Validator.ResultItem sunOneValidationErr = null;

            // first try sun one url parser
            try {
                SunOneUrlParser urlParser = new SunOneUrlParser(connectionURL);
                urlParser.validate();
            } catch (ValidationException ex) {
                sunOneValidationErr = new Validator.ResultItem(this,
                                          Validator.ResultType.ERROR,
                                          target,
                                          getMessage("JMSAddress.INVALID_CONNECTION_URL",
                                          new Object[] {target.getConnectionURL(), ex}));
            }

            boolean genericError = false;

            // try generic url parser
            if (sunOneValidationErr != null) {

                UrlParser url = new UrlParser(target.getConnectionURL());

                // cause url to be parsed
                String protocol = null;
                String host = null;
                int UNLIKELY_PORT = -291; // unlikely that user will enter this number
                int port = UNLIKELY_PORT;
                try {
                     protocol = url.getProtocol();
                     host = url.getHost();
                     port = url.getPort();
                } catch (Throwable t) {
                    genericError = true;
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            getMessage("JMSAddress.INVALID_CONNECTION_URL",
                                       new Object[] {target.getConnectionURL(), t.getLocalizedMessage()})));
                } 

                if (protocol == null || protocol.length() == 0) {
                    genericError = true;
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            getMessage("JMSAddress.NO_PROTOCOL_SPECIFIED",
                                       new Object[] {target.getConnectionURL()})));            
                } else {
                    if (!protocol.equals(ConnectionUrl.PROTOCOL_JMS_PROVIDER_SUN_JAVA_SYSTEM_MQ) &&
                        !protocol.equals(ConnectionUrl.PROTOCOL_JMS_PROVIDER_WEPSHERE_MQ) &&
                        !protocol.equals(ConnectionUrl.PROTOCOL_JMS_PROVIDER_JBOSS) &&
                        !protocol.equals(ConnectionUrl.PROTOCOL_JMS_PROVIDER_STCMS) &&
                        !protocol.equals(ConnectionUrl.PROTOCOL_JMS_PROVIDER_WAVE) &&
                        !protocol.equals(ConnectionUrl.PROTOCOL_JMS_PROVIDER_WEBLOGIC)) {
                        genericError = true;
                        results.add(new Validator.ResultItem(this,
                                Validator.ResultType.ERROR,
                                target,
                                getMessage("JMSAddress.PROVIDER_NOT_SUPPORTED",
                                           new Object[] {target.getConnectionURL(), protocol})));
                    }
                }

                if (host == null || host.length() == 0) {
                    genericError = true;
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            getMessage("JMSAddress.NO_HOST_SPECIFIED",
                                       new Object[] {target.getConnectionURL()})));            
                }

                if (port == UNLIKELY_PORT) {
                    genericError = true;
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            getMessage("JMSAddress.NO_PORT_SPECIFIED",
                                       new Object[] {target.getConnectionURL()})));            
                } else if (port <= 0) {
                    genericError = true;
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            getMessage("JMSAddress.INVALID_PORT_SPECIFIED",
                                       new Object[] {target.getConnectionURL(),
                                                     new Integer(port)})));            
                }
            }

            if (genericError) {
                // add provider specific errors to error bundle
                if (sunOneValidationErr != null) {
                    results.add(sunOneValidationErr);
                }
            }
        }
        
        String username = target.getUsername();
        
        if (username != null) {
            isAToken(username, target);
            String password = target.getPassword();
            if (password == null) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("JMSAddress.MISSING_PASSWORD",
                                   new Object[] {username})));                
            } else {
                isAToken(password, target);
            }
        }
    }

    private void validate(JMSBinding target) {
        // for jms binding tag - nothing to validate at this point
    }

    private void validate(BindingOperation bindingOp, JMSOperation target) {
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();
        
        // ToDo: validate JMS operation
        String mep = "in-only";
        boolean hasInput = bindingOp.getBindingInput() != null;
        boolean hasOutput = bindingOp.getBindingOutput() != null;
        
        if (hasInput && hasOutput) {
            mep = "in-out";
        }
        
        String destination = target.getDestination();
        isAToken(destination, target);
        
        String destinationType = target.getDestinationType();
        if (destination == null && destination.length() == 0) {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("JMSOperation.EMPTY_DESTINATION_EMPTY",
                               new Object[] {bindingOp.getName()})));            
        } 
                
        String subscriptionDurability = target.getSubscriptionDurability();
        if (subscriptionDurability != null &&
            subscriptionDurability.equals(JMSConstants.DURABLE)) {
            String subscriptionName = target.getSubscriptionName();
            if (subscriptionName == null || subscriptionName.length() == 0) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("JMSOperation.DURABLE_SUBSCRIBER_BUT_NO_SUBSCRIPTION_NAME",
                                   new Object[] {bindingOp.getName()})));                            
            }
            isAToken(subscriptionName, target);
            
            if (destinationType.equals(JMSConstants.QUEUE)) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("JMSOperation.DURABLE_SUBSCRIBER_BUT_DESTINATION_TYPE_IS_QUEUE",
                                   new Object[] {bindingOp.getName()})));                
            }
            
            String clientID = target.getClientID();
            if (clientID == null || clientID.length() == 0) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.WARNING,
                        target,
                        getMessage("JMSOperation.DURABLE_SUBSCRIBER_BUT_NO_CLIENT_ID",
                                   new Object[] {bindingOp.getName()})));                
            }
            isAToken(clientID, target);
        }

        int maxConcurrentConsumers = target.getMaxConcurrentConsumers();
        if (maxConcurrentConsumers > 1 && destinationType.equals(JMSConstants.TOPIC)) {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.WARNING,
                    target,
                    getMessage("JMSOperation.MAX_CONCURRENT_CONSUMERS_SUPPORTED_FOR_TOPICS",
                               new Object[] {bindingOp.getName(),
                                             maxConcurrentConsumers})));                 
        }
        
        int batchSize = target.getBatchSize();
        if (batchSize > 1) {
            /* should be ok for topic - there will only be one consumer
            if (destinationType.equals(JMSConstants.TOPIC)) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("JMSOperation.BATCH_NOT_SUPPORTED_FOR_TOPICS",
                                   new Object[] {bindingOp.getName(),
                                                 batchSize})));     
            }
             **/
            String transaction = target.getTransaction();             
            if (transaction != null &&
                transaction.equals(JMSConstants.TRANSACTION_XA)) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("JMSOperation.XA_NOT_SUPPORTED_FOR_BATCH",
                                   new Object[] {bindingOp.getName(),
                                                 batchSize})));     
            }            
        }

        String redeliveryHandling = target.getRedeliveryHandling();
        if (redeliveryHandling != null && redeliveryHandling.length() > 0) {
            if (!isAToken(redeliveryHandling, target)) {
                try {
                    RedeliveryHandlingParser.parse(redeliveryHandling,"none",JMSConstants.QUEUE);
                } catch (Throwable t) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            getMessage("JMSOperation.INVALID_REDELIVERY_HANDLING_ACTIONS",
                                       new Object[] {bindingOp.getName(),
                                                     redeliveryHandling, 
                                                     t.getLocalizedMessage()})));                
                }
            }
        } 
        
        // check list of options
        List <JMSOptions> options = target.getExtensibilityElements(JMSOptions.class);
        if (options.size() > 1) {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("JMSOperation.ATMOST_ONE_OPTIONS_ELEM_IN_JMS_OPERATION",
                               new Object[] {bindingOp.getName(),
                                             options.size()})));
        } /*else if (options.size() == 1) {
            JMSOptions jmsOptions = options.iterator().next();
            // validate each option??
            Iterator <JMSOption> optionIter = jmsOptions.getOptions().iterator();
            while (optionIter.hasNext()) {
                JMSOption jmsOption = optionIter.next();
                String name = jmsOption.getName();
                String value = jmsOption.getValue();
            }
        }*/
        
        // ensure that option elements are child elements of options element
        List <JMSOption> option = target.getExtensibilityElements(JMSOption.class);
        if (option.size() > 0) {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    option.get(0),
                    getMessage("JMSOperation.IMPROPER_USAGE_OF_OPTION",
                               new Object[] {bindingOp.getName(),
                                             option.size()})));        
        }
    }

    private void validate(BindingOperation bindingOp,
                          OperationParameter opParam, 
                          JMSMessage target) {
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();
        
        // get JMS message type
        String jmsMsgType = target.getMessageType();
        if (jmsMsgType.equals(JMSConstants.TEXT_MESSAGE)) {
            // Check textPart
            String textPart = target.getTextPart();
            if (textPart == null || textPart.length() == 0) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("JMSMessage.TEXT_MESSAGE_TEXTPART_NOT_SPECIFIED",
                                   new Object[] {bindingOp.getName(),
                                                 (opParam instanceof Input)? "input":"output",
                                                 opParam.getName()})));                
            } else {
                // make sure textPart references a vald wsdl message part
                if (!referencesValidMessagePart(opParam.getMessage(), textPart)) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            getMessage("JMSMessage.TEXT_PART_REFERENCES_NON_EXISTENT_PART",
                                       new Object[] {bindingOp.getName(),
                                                     (opParam instanceof Input)? "input":"output",
                                                     opParam.getName(),
                                                     textPart,
                                                     opParam.getMessage().getQName()})));                    
                }
            }
            
            List <JMSMapMessage> mapmessageList = target.getExtensibilityElements(JMSMapMessage.class);
            if (mapmessageList.size() > 0) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            mapmessageList.iterator().next(),
                            getMessage("JMSMessage.TEXT_MESSAGE_CANNOT_HAVE_MAPMESSAGE_ELEM",
                                       new Object[] {bindingOp.getName(),
                                                     (opParam instanceof Input)? "input":"output",
                                                     opParam.getName()})));                
            }

            // Check use and encoded
            String use = target.getUse();
            if (use != null && use.length() > 0) {
                String encodingStyle = target.getJMSEncodingStyle();
                if (use.equals(JMSMessage.ATTR_USE_TYPE_ENCODED)) {
                    // ensure encodingStyle is defined
                    if (encodingStyle == null || encodingStyle.length() == 0) {
                        results.add(new Validator.ResultItem(this,
                                Validator.ResultType.ERROR,
                                target,
                                getMessage("JMSMessage.ENCODING_STYLE_NOT_SPECIFIED",
                                           new Object[] {bindingOp.getName(),
                                                         (opParam instanceof Input)? "input":"output",
                                                         opParam.getName()})));                        
                    } // add rule to validate different styles?
                } else { // must be 'literal'
                    // encodingStyle should not be defined
                    if (encodingStyle != null && encodingStyle.length() > 0) {
                        results.add(new Validator.ResultItem(this,
                                Validator.ResultType.ERROR,
                                target,
                                getMessage("JMSMessage.ENCODING_STYLE_INVALID_USE",
                                           new Object[] {bindingOp.getName(),
                                                         (opParam instanceof Input)? "input":"output",
                                                         opParam.getName(),
                                                         encodingStyle})));                        
                    }
                }
            }
        } else if (jmsMsgType.equals(JMSConstants.MAP_MESSAGE)) {                    
            // Check textPart
            String textPart = target.getTextPart();
            if (textPart != null && textPart.length() > 0) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("JMSMessage.MAP_MESSAGE_CANNOT_HAVE_TEXTPART_ATTR",
                                   new Object[] {bindingOp.getName(),
                                                 (opParam instanceof Input)? "input":"output",
                                                 opParam.getName()})));                 
            }
            
            // Check use
            String use = target.getUse();
            if (use != null && use.length() > 0) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.WARNING,
                        target,
                        getMessage("JMSMessage.USE_IGNORED",
                                   new Object[] {bindingOp.getName(),
                                                 (opParam instanceof Input)? "input":"output",
                                                 opParam.getName(),
                                                 use})));                        
            }
            
            // check for jms:mapmessage definition
            List <JMSMapMessage> mapmessageList = target.getExtensibilityElements(JMSMapMessage.class);
            switch (mapmessageList.size()) {
                case 0: 
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            getMessage("JMSMessage.MISSING_MAPMESSAGE_ELEM_IN_JMS_MESSAGE",
                                   new Object[] {bindingOp.getName(),
                                                 (opParam instanceof Input)? "input":"output",
                                                 opParam.getName()})));
                    break;
                case 1:
                    JMSMapMessage mapmessage = mapmessageList.iterator().next();
                    List<JMSMapMessagePart> mappartList = mapmessage.getMapMessageParts();
                    switch (mappartList.size()) {
                        case 0:
                            results.add(new Validator.ResultItem(this,
                                    Validator.ResultType.ERROR,
                                    mapmessage,
                                    getMessage("JMSMessage.EMPTY_MAPMESSAGE_PARTS",
                                           new Object[] {bindingOp.getName(),
                                                         (opParam instanceof Input)? "input":"output",
                                                         opParam.getName()})));                            
                            break;
                        default:
                            Iterator<JMSMapMessagePart> mappartIter = mappartList.iterator();
                            while (mappartIter.hasNext()) {
                                JMSMapMessagePart mappart = mappartIter.next();
                                String name = mappart.getName();
                                String type = mappart.getType();
                                String part = mappart.getPart();
                                
                                if (name == null || name.length() == 0) {
                                    results.add(new Validator.ResultItem(this,
                                            Validator.ResultType.ERROR,
                                            mappart,
                                            getMessage("JMSMessage.EMTPY_MAPPART_NAME",
                                                   new Object[] {bindingOp.getName(),
                                                                 (opParam instanceof Input)? "input":"output",
                                                                 opParam.getName()})));                            

                                }                                
                                if (type == null || type.length() == 0) {
                                    results.add(new Validator.ResultItem(this,
                                            Validator.ResultType.ERROR,
                                            mappart,
                                            getMessage("JMSMessage.EMTPY_MAPPART_TYPE",
                                                   new Object[] {bindingOp.getName(),
                                                                 (opParam instanceof Input)? "input":"output",
                                                                 opParam.getName()})));                            

                                }
                                if (part == null || part.length() == 0){
                                    results.add(new Validator.ResultItem(this,
                                            Validator.ResultType.ERROR,
                                            mappart,
                                            getMessage("JMSMessage.EMTPY_MAPPART_PART",
                                                   new Object[] {bindingOp.getName(),
                                                                 (opParam instanceof Input)? "input":"output",
                                                                 opParam.getName()})));                                                                
                                } else {
                                    if (!referencesValidMessagePart(opParam.getMessage(),part)) {
                                        results.add(new Validator.ResultItem(this,
                                                Validator.ResultType.ERROR,
                                                mappart,
                                                getMessage("JMSMessage.MAPPART_PART_REFERENCES_NON_EXISTENT_PART",
                                                           new Object[] {bindingOp.getName(),
                                                                         (opParam instanceof Input)? "input":"output",
                                                                         opParam.getName(),
                                                                         part,
                                                                         opParam.getMessage().getQName()})));                                        
                                    }                                    
                                }                            
                            }
                    }
                    break;
                default:
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            getMessage("JMSMessage.ATMOST_ONE_MAPMESSAGE_ELEM_IN_JMS_MESSAGE",
                                   new Object[] {bindingOp.getName(),
                                                 (opParam instanceof Input)? "input":"output",
                                                 opParam.getName(),
                                                 mapmessageList.size()})));
            }
        } else {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("JMSMessage.MESSAGE_TYPE_IS_NOT_SUPPORTED",
                               new Object[] {bindingOp.getName(),
                                             (opParam instanceof Input)? "input":"output",
                                             opParam.getName(),
                                             jmsMsgType})));            
        }
        
        // check jms:properties
        List <JMSProperties> propertiesList = target.getExtensibilityElements(JMSProperties.class);
        switch (propertiesList.size()) {
            case 0:
                // JMS user properties (jms:properties) are optional
                break;
            case 1:
                    JMSProperties properties = propertiesList.iterator().next();
                    List<JMSProperty> propertyList = properties.getProperties();
                    switch (propertyList.size()) {
                        case 0:
                            results.add(new Validator.ResultItem(this,
                                    Validator.ResultType.ERROR,
                                    properties,
                                    getMessage("JMSMessage.EMPTY_PROPERTIES_PARTS",
                                           new Object[] {bindingOp.getName(),
                                                         (opParam instanceof Input)? "input":"output",
                                                         opParam.getName()})));                            
                            break;
                        default:
                            Iterator<JMSProperty> propertyIter = propertyList.iterator();
                            while (propertyIter.hasNext()) {
                                JMSProperty property = propertyIter.next();
                                String name = property.getName();
                                String type = property.getType();
                                String part = property.getPart();
                                
                                if (name == null || name.length() == 0) {
                                    results.add(new Validator.ResultItem(this,
                                            Validator.ResultType.ERROR,
                                            property,
                                            getMessage("JMSMessage.EMTPY_PROPERTY_NAME",
                                                   new Object[] {bindingOp.getName(),
                                                                 (opParam instanceof Input)? "input":"output",
                                                                 opParam.getName()})));                            

                                }                                
                                if (type == null || type.length() == 0) {
                                    results.add(new Validator.ResultItem(this,
                                            Validator.ResultType.ERROR,
                                            property,
                                            getMessage("JMSMessage.EMTPY_PROPERTY_TYPE",
                                                   new Object[] {bindingOp.getName(),
                                                                 (opParam instanceof Input)? "input":"output",
                                                                 opParam.getName()})));                            

                                }
                                if (part == null || part.length() == 0){
                                    results.add(new Validator.ResultItem(this,
                                            Validator.ResultType.ERROR,
                                            property,
                                            getMessage("JMSMessage.EMTPY_PROPERTY_PART",
                                                   new Object[] {bindingOp.getName(),
                                                                 (opParam instanceof Input)? "input":"output",
                                                                 opParam.getName()})));                                                                
                                } else {
                                    if (!referencesValidMessagePart(opParam.getMessage(),part)) {
                                        results.add(new Validator.ResultItem(this,
                                                Validator.ResultType.ERROR,
                                                property,
                                                getMessage("JMSMessage.PROPERTY_PART_REFERENCES_NON_EXISTENT_PART",
                                                           new Object[] {bindingOp.getName(),
                                                                         (opParam instanceof Input)? "input":"output",
                                                                         opParam.getName(),
                                                                         part,
                                                                         opParam.getMessage().getQName()})));                                        
                                    }                                    
                                }                            
                            }
                    }                
                break;
            default:
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("JMSMessage.ATMOST_ONE_PROPERTIES_ELEM_IN_JMS_MESSAGE",
                               new Object[] {bindingOp.getName(),
                                             (opParam instanceof Input)? "input":"output",
                                             opParam.getName(),
                                             propertiesList.size()})));
        }
        
        // check JMS standard headers
        String correlationIDPart = target.getCorrelationIdPart();
        if (correlationIDPart != null) {
            if (correlationIDPart.length() == 0) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("JMSMessage.EMTPY_CORRELATIONID_PART",
                               new Object[] {bindingOp.getName(),
                                             (opParam instanceof Input)? "input":"output",
                                             opParam.getName()})));                                                                
                
            } else if (!referencesValidMessagePart(opParam.getMessage(), correlationIDPart)) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            getMessage("JMSMessage.CORRELATIONID_PART_REFERENCES_NON_EXISTENT_PART",
                                   new Object[] {bindingOp.getName(),
                                                 (opParam instanceof Input)? "input":"output",
                                                 opParam.getName(),
                                                 correlationIDPart,
                                                 opParam.getMessage().getQName()})));                    
            }
        }
    
        String deliveryModePart = target.getDeliveryModePart();
        if (deliveryModePart != null) {
            if (deliveryModePart.length() == 0) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("JMSMessage.EMTPY_DELIVERYMODE_PART",
                               new Object[] {bindingOp.getName(),
                                             (opParam instanceof Input)? "input":"output",
                                             opParam.getName()})));                                                                
                
            } else if (!referencesValidMessagePart(opParam.getMessage(), deliveryModePart)) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            getMessage("JMSMessage.DELIVERYMODE_PART_REFERENCES_NON_EXISTENT_PART",
                                   new Object[] {bindingOp.getName(),
                                                 (opParam instanceof Input)? "input":"output",
                                                 opParam.getName(),
                                                 deliveryModePart,
                                                 opParam.getMessage().getQName()})));                    
            }
        }

        String priorityPart = target.getPriorityPart();
        if (priorityPart != null) {
            if (priorityPart.length() == 0) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("JMSMessage.EMTPY_PRIORITY_PART",
                               new Object[] {bindingOp.getName(),
                                             (opParam instanceof Input)? "input":"output",
                                             opParam.getName()})));                                                                
                
            } else if (!referencesValidMessagePart(opParam.getMessage(), priorityPart)) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            getMessage("JMSMessage.PRIORITY_PART_REFERENCES_NON_EXISTENT_PART",
                                   new Object[] {bindingOp.getName(),
                                                 (opParam instanceof Input)? "input":"output",
                                                 opParam.getName(),
                                                 priorityPart,
                                                 opParam.getMessage().getQName()})));                    
            }
        }

        String typePart = target.getTypePart();
        if (typePart != null) {
            if (typePart.length() == 0) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("JMSMessage.EMTPY_TYPE_PART",
                               new Object[] {bindingOp.getName(),
                                             (opParam instanceof Input)? "input":"output",
                                             opParam.getName()})));                                                                
                
            } else if (!referencesValidMessagePart(opParam.getMessage(), typePart)) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            getMessage("JMSMessage.TYPE_PART_REFERENCES_NON_EXISTENT_PART",
                                   new Object[] {bindingOp.getName(),
                                                 (opParam instanceof Input)? "input":"output",
                                                 opParam.getName(),
                                                 typePart,
                                                 opParam.getMessage().getQName()})));                    
            }
        }

        String messageIDPart = target.getMessageIDPart();
        if (messageIDPart != null) {
            if (messageIDPart.length() == 0) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("JMSMessage.EMTPY_MESSAGEID_PART",
                               new Object[] {bindingOp.getName(),
                                             (opParam instanceof Input)? "input":"output",
                                             opParam.getName()})));                                                                
                
            } else if (!referencesValidMessagePart(opParam.getMessage(), messageIDPart)) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            getMessage("JMSMessage.MESSAGEID_PART_REFERENCES_NON_EXISTENT_PART",
                                   new Object[] {bindingOp.getName(),
                                                 (opParam instanceof Input)? "input":"output",
                                                 opParam.getName(),
                                                 messageIDPart,
                                                 opParam.getMessage().getQName()})));                    
            }
        }

        String redeliveredPart = target.getRedeliveredPart();
        if (redeliveredPart != null) {
            if (redeliveredPart.length() == 0) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("JMSMessage.EMTPY_REDELIVERED_PART",
                               new Object[] {bindingOp.getName(),
                                             (opParam instanceof Input)? "input":"output",
                                             opParam.getName()})));                                                                
                
            } else if (!referencesValidMessagePart(opParam.getMessage(), redeliveredPart)) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            getMessage("JMSMessage.REDELIVERED_PART_REFERENCES_NON_EXISTENT_PART",
                                   new Object[] {bindingOp.getName(),
                                                 (opParam instanceof Input)? "input":"output",
                                                 opParam.getName(),
                                                 redeliveredPart,
                                                 opParam.getMessage().getQName()})));                    
            }
        }

        String timestampPart = target.getTimestampPart();
        if (timestampPart != null) {
            if (timestampPart.length() == 0) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("JMSMessage.EMTPY_TIMESTAMP_PART",
                               new Object[] {bindingOp.getName(),
                                             (opParam instanceof Input)? "input":"output",
                                             opParam.getName()})));                                                                
                
            } else if (!referencesValidMessagePart(opParam.getMessage(), timestampPart)) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            getMessage("JMSMessage.TIMESTAMP_PART_REFERENCES_NON_EXISTENT_PART",
                                   new Object[] {bindingOp.getName(),
                                                 (opParam instanceof Input)? "input":"output",
                                                 opParam.getName(),
                                                 timestampPart,
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
    
    private boolean isAToken(String name, JMSComponent target) {
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();
        
    	boolean isToken = false;
    	
        if (name != null && name.startsWith("${")) {
            isToken = true;
            if (!name.endsWith("}")) {
                results.add(
                        new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        getMessage("JMSComponentValidator.INVALID_ENVIRONMENT_TOKEN_NAME",
                                   new Object[]{name})));               
            }
        }
        
        return isToken;
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
