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
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.wsdlextensions.mq.editor;

import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.netbeans.modules.wsdlextensions.mq.MQAddress;
import org.netbeans.modules.wsdlextensions.mq.MQBinding;
import org.netbeans.modules.wsdlextensions.mq.MQBody;
import org.netbeans.modules.wsdlextensions.mq.MQOperation;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardContext;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * Adapts information from a WSDLModel to the representation that a
 * MqBindingsConfigurationEditorForm understands.
 *
 * @author Noel.Ang@sun.com
 * @see {@link MqBindingsFormModel}
 */
final class WsdlConfigModelAdapter
        extends MqBindingsConfigurationEditorModel {

    /**
     * Create a WsdlConfigModelAdapter using the specified model.
     *
     * @param model Data model
     */
    public WsdlConfigModelAdapter(WSDLModel model) {
        super();
        if (model == null) {
            throw new NullPointerException("model");
        }
        this.model = model;
        context = null;
        updateFromModel();
    }

    /**
     * Create a WsdlConfigModelAdapter using the specific WSDLWizardContext.
     *
     * @param context WSDLWizardContext object with an associated WSDLModel (now
     * or later).
     */
    public WsdlConfigModelAdapter(WSDLWizardContext context) {
        super();
        if (context == null) {
            throw new NullPointerException("context");
        }
        
        // The WSDL model obtained thru a WSDLWizardContext
        // has been observed to be lazily initialized, so I will
        // attempt to acquire on-demand.
        model = null;
        this.context = context;
        updateFromModel();
    }

    /**
     * Specifies the WSDL operation that should be the object of regard when
     * reading from or writing to the underlying WSDL model. By default, the
     * first MQ-bound binding's first MQ-bound operation is the object of
     * regard. Calling this method with a null argument causes this default
     * effect.
     * 
     * If the specified operation is not found in the WSDL model, or is not
     * MQ-bound, the default effect is also caused.
     *
     * @param operation An operation defined in the WSDL model specified during
     * this object's creation.
     */
    synchronized void focus(Operation operation) {
        operationFocus = operation;
        suspendModelUpdate = true;
        updateFromModel();
        suspendModelUpdate = false;
    }

    /**
     * Specifies the WSDL port whose first MQ-bound binding operation would be
     * the object of regard when reading from or writing to the underlying WSDL
     * model. By default, the first MQ-bound binding's first MQ-bound operation
     * is the object of regard. Calling this method with a null argument
     * causes this default effect.
     * 
     * If the specified port is not found in the WSDL model, or does not
     * have an MQ-bound binding, or the binding does not have an MQ-bound
     * operation, the default effect is also caused.
     *
     * @param port A port defined in the WSDL model specified during
     * this object's creation.
     */
    void focus(Port port) {
        Operation operation = null;
        if (port != null && port.getBinding() != null) {
            Binding binding = port.getBinding().get();
            List<MQBinding> mqBindings = binding.getExtensibilityElements(MQBinding.class);
            if (!mqBindings.isEmpty()) {
                MQBinding mqBinding = mqBindings.get(0);
                MQOperation mqOperation = findMqOperation(mqBinding);
                if (mqOperation != null) {
                    WSDLComponent wsdlComponent = mqOperation.getParent();
                    if (wsdlComponent instanceof BindingOperation) {
                        BindingOperation bindingOp = (BindingOperation) wsdlComponent;
                        if (bindingOp.getOperation() != null) {
                            operation = bindingOp.getOperation().get();
                        }
                    }
                }
            }
        }
        focus(operation);
    }

    /**
     * Write field values to the WSDL model. Had to make this non-private as a
     * hack to allow an external entity to execute a lazy update of the model.
     * This is done to accomodate WSDLWizardContext implementatiosn that
     * lazy-init their associated WSDLModel objects.
     */
    void updateModel() {
        if (suspendModelUpdate) {
            return;
        }
        synchronized (this) {
            WSDLModel model = this.model;
            if (model == null) {
                if (context != null) {
                    // this.model is not updated.
                    // This is intentional; reference on-demand without caching.
                    // There is a situation where the WSDL model
                    // may change at the last moment (e.g.,
                    // WSDL Wizard is returned to the initial page and
                    // a different WSDL template is selected.)
                    model = context.getWSDLModel();
                }
            }
            if (model != null) {
                boolean myTransaction = false;
                if (!model.isIntransaction()) {
                    myTransaction = model.startTransaction();
                }
                if (model.isIntransaction()) {
                    MQBinding binding = findMqBinding(model);
                    Operation wsdlOperation = this.operationFocus;
                    if (binding != null) {
                        MQAddress addressing = findMqAddress(model, binding);
                        MQOperation operation = (wsdlOperation == null
                                ? findMqOperation(binding)
                                : findMqOperation((Binding) binding.getParent(),
                                        wsdlOperation
                                ));
                        if (wsdlOperation != null && operation == null) {
                            operation = findMqOperation(binding);
                        }
                        MQBody inputBody = findMqInputBody(operation);
                        MQBody outputBody = findMqOutputBody(operation);
                        
                        // all or nothing (outputBody optional)
                        if (addressing != null
                                && operation != null
                                && inputBody != null
                                ) {
                            update(addressing);
                            update(operation);
                            update(inputBody, outputBody);
                        }
                    }
                    if (myTransaction) {
                        model.endTransaction();
                    }
                }
            }
        }
    }

    /** Write WSDL model values to object fields. */
    private void updateFromModel() {
        synchronized (this) {
            WSDLModel model = this.model;
            if (model == null) {
                if (context != null) {
                    // this.model is not updated.
                    // This is intentional; reference on-demand without caching.
                    // There is a situation where the WSDL model
                    // may change at the last moment (e.g.,
                    // WSDL Wizard is returned to the initial page and
                    // a different WSDL template is selected.)
                    model = context.getWSDLModel();
                }
            }
            if (model != null) {
                Operation wsdlOperation = this.operationFocus;
                MQBinding binding = findMqBinding(model);
                if (binding != null) {
                    MQAddress addressing = findMqAddress(model, binding);
                    MQOperation operation = (wsdlOperation == null
                            ? findMqOperation(binding)
                            : findMqOperation((Binding) binding.getParent(),
                                    wsdlOperation
                            ));
                    if (wsdlOperation != null && operation == null) {
                        operation = findMqOperation(binding);
                    }
                    MQBody inputBody = findMqInputBody(operation);
                    MQBody outputBody = findMqOutputBody(operation);
                    
                    // all or nothing (outputBody optional)
                    if (addressing != null
                            && operation != null
                            && inputBody != null
                            ) {
                        parse(addressing);
                        parse(operation);
                        parse(inputBody, outputBody);
                    }
                }
            }
        }
    }

    private MQBinding findMqBinding(WSDLModel model) {
        // MQ extensibility element hierarchy:
        //
        // wsdl:binding
        //     mq:binding  <---
        //     wsdl:operation
        //         mq:operation <---
        //         wsdl:input
        //             mq:body <---
        //             mq:header ... <---
        //         wsdl:output
        //             mq:body <---
        //             mq:header ... <---
        // wsdl:service
        //     wsdl:port
        //         mq:address <---

        // Find the first WSDL Binding with an MQ binding.
        // If operation-focus is specified, match that, too.
        MQBinding mqBinding = null;
        Operation operationFocus = this.operationFocus;
        String operationFocusName =
                (operationFocus != null ? operationFocus.getName() : null);

        Definitions definitions = model.getDefinitions();
        
        if (operationFocusName == null) {
            for (Binding binding : definitions.getBindings()) {
                List<MQBinding> bindings =
                        binding.getExtensibilityElements(MQBinding.class);
                if (!bindings.isEmpty()) {
                    mqBinding = bindings.get(0);
                    break;
                }
            }
        } else {
            for (Binding binding : definitions.getBindings()) {
                List<MQBinding> bindings =
                        binding.getExtensibilityElements(MQBinding.class);
                if (!bindings.isEmpty()) {
                    for (BindingOperation bindingOp : binding.getBindingOperations()) {
                        if (bindingOp.getOperation() != null) {
                            Operation operation = bindingOp.getOperation().get();
                            if (operation.getName().equals(operationFocusName)) {
                                mqBinding = bindings.get(0);
                                break;
                            }
                        }
                    }
                }
            }
        }

        return mqBinding;
    }

    /**
     * Given an MQ Binding element, find an MQ Address element whose port
     * matches the binding.
     *
     * @param model The WSDL model to search.
     * @param mqBinding The MQBinding object whose associated MQAddress
     * information is sought.
     *
     * @return The MQAddress object associated in the underlying WSDL document
     *         model with the specified MQ Binding.
     */
    private MQAddress findMqAddress(WSDLModel model, MQBinding mqBinding) {
        assert mqBinding != null;
        MQAddress mqAddress = null;
        Definitions definitions = model.getDefinitions();
        if (definitions != null) {
            WSDLComponent parent = mqBinding.getParent();
            if (Binding.class.isAssignableFrom(parent.getClass())) {
                Binding targetBinding = (Binding) mqBinding.getParent();
                search:
                for (Service service : definitions.getServices()) {
                    for (Port port : service.getPorts()) {
                        if (isReferencing(port.getBinding())) {
                            Binding binding = port.getBinding().get();
                            if (binding.equals(targetBinding)) {
                                List<MQAddress> addresses =
                                        port.getExtensibilityElements(MQAddress.class);
                                if (!addresses.isEmpty()) {
                                    mqAddress = addresses.get(0);
                                    break search;
                                }
                            }
                        }
                    }
                }
            }
        }
        return mqAddress;
    }

    /**
     * Given an MQ Binding element, find the first MQ Operation element
     * belonging to the same binding.
     *
     * @param mqBinding The MQBinding object whose first MQ Operation is
     * retrieved.
     *
     * @return The first MQOperation object associated in the underlying WSDL
     *         document model with the specified MQ Binding, or null if no
     *         such operation can be found (non-existent or not MQ bound). 
     */
    private MQOperation findMqOperation(MQBinding mqBinding) {
        assert mqBinding != null;
        MQOperation mqOperation = null;
        WSDLComponent parent = mqBinding.getParent();
        if (Binding.class.isAssignableFrom(parent.getClass())) {
            Binding binding = (Binding) parent;
            for (BindingOperation operation : binding.getBindingOperations()) {
                List<MQOperation> operations =
                        operation.getExtensibilityElements(MQOperation.class);
                if (!operations.isEmpty()) {
                    mqOperation = operations.get(0);
                    break;
                }
            }
        }
        return mqOperation;
    }

    /**
     * Find the MQOperation binding of the indicated WSDL operation.
     *
     * @param binding The WSDL binding possessing the operation.
     * @param operation The WSDL operation whose MQ binding is to be found.
     *
     * @return The MQOperation binding object associated with the operation,
     *         or null if no matching operation (non-existent, or not MQ bound)
     *         can be found. 
     */
    private MQOperation findMqOperation(Binding binding, Operation operation) {
        assert binding != null;
        assert operation != null;

        MQOperation mqOperation = null;
        for (BindingOperation bindingOp : binding.getBindingOperations()) {
            if (bindingOp.getOperation() != null) {
                Operation op = bindingOp.getOperation().get();
                if (op.getName().equals(operation.getName())) {
                    List<MQOperation> operations =
                            bindingOp.getExtensibilityElements(MQOperation.class);
                    if (!operations.isEmpty()) {
                        mqOperation = operations.get(0);
                        break;
                    }
                }
            }
        }
        return mqOperation;
    }

    private MQBody findMqOutputBody(MQOperation operation) {
        assert operation != null;
        MQBody body = null;
        WSDLComponent parent = operation.getParent();
        assert parent != null;
        if (parent instanceof BindingOperation) {
            BindingOutput bindingOutput =
                    ((BindingOperation) parent).getBindingOutput();
            if (bindingOutput != null) {
                List<MQBody> bodies =
                        bindingOutput.getExtensibilityElements(MQBody.class);
                if (!bodies.isEmpty()) {
                    body = bodies.get(0);
                }
            }
        }
        return body;
    }

    private MQBody findMqInputBody(MQOperation operation) {
        assert operation != null;
        MQBody body = null;
        WSDLComponent parent = operation.getParent();
        assert parent != null;
        if (parent instanceof BindingOperation) {
            BindingInput bindingInput =
                    ((BindingOperation) parent).getBindingInput();
            if (bindingInput != null) {
                List<MQBody> bodies =
                        bindingInput.getExtensibilityElements(MQBody.class);
                if (!bodies.isEmpty()) {
                    body = bodies.get(0);
                }
            }
        }
        return body;
    }

    private void update(MQAddress address) {
        assert address != null;
        
        // required parameters
        address.setQueueManagerName(getQueueManager());
        
        // optional parameters
        String value = getHost();
        address.setHostName("".equals(value) ? null : value);
        value = getPort();
        address.setPortNumber("".equals(value) ? null : value);
        value = getChannel();
        address.setChannelName("".equals(value) ? null : value);
        value = getCipherSuite();
        address.setCipherSuite("".equals(value) ? null : value);
        value = getSslPeerName();
        address.setSslPeerName("".equals(value) ? null : value);
    }

    private void update(MQOperation operation) {
        assert operation != null;
        
        // required parameters
        operation.setQueueName(getQueue());
        
        useTransaction.accept(getIsTransactional());
        operation.setTransaction(useTransaction.choice());
        
        useDefaultBindingOption.accept(getIsDefaultBindingOption());
        useNoBindingOption.accept(getIsNoBindingOption());
        useOnOpenBindingOption.accept(getIsOnOpenBindingOption());
        useDefaultReadOption.accept(getIsDefaultReadOption());
        useSharedReadOption.accept(getIsSharedReadOption());
        useExclusiveReadOption.accept(getIsExclusiveReadOption());
        StringBuffer buffer = new StringBuffer();
        buffer.append(serializeBooleanOptions(bindOptions, readOptions));
        operation.setQueueOpenOptions(Utils.safeString(buffer.toString()));
        
        // optional parameters
        String value = getPolling();
        operation.setPollingInterval("".equals(value) ? null : value);
    }

    private void update(MQBody inputBody, MQBody outputBody) {
        assert inputBody != null;
        
        messageType.accept(getMessageType());
        inputBody.setMessageType(messageType.choice());
        inputBody.setSyncpoint(getIsSyncpoint());

        // outputBody can be null, when operation is one-way.
        if (outputBody != null) {
            outputBody.setMessageType(messageType.choice());
        }
        
        // messageBody is a mandatory input/output attrbute that is not
        // specifiable from the GUI because its a WSDL abstraction.
        // I need to set it on the input or output depending on whether
        // the WSDL model describes a one-way or request-response operation.
        boolean isOneWayOperation = (outputBody == null);
        MQBody body = (isOneWayOperation ? inputBody : outputBody);
        if (body.getMessageBodyPart() == null
                || "".equals(body.getMessageBodyPart())) {
            body.setMessageBodyPart(findMqMessageBodyPartName(body,
                    isOneWayOperation
            )
            );
        }
    }

    private String findMqMessageBodyPartName(MQBody mqBody,
                                             boolean searchForOneWayOperation
    ) {
        assert mqBody != null;
        
        String name = null;
        WSDLComponent wsdlComponent = mqBody.getParent();
        
        while (wsdlComponent != null && !(wsdlComponent instanceof BindingOperation)) {
            wsdlComponent = wsdlComponent.getParent();
        }
        
        if (wsdlComponent != null) {
            BindingOperation boundOperation = (BindingOperation) wsdlComponent;
            if (boundOperation.getOperation() != null) {
                Operation operation = boundOperation.getOperation().get();
                Message message = null;
                if (searchForOneWayOperation) {
                    Input input = operation.getInput();
                    if (input.getMessage() != null) {
                        message = input.getMessage().get();
                    }
                } else {
                    Output output = operation.getOutput();
                    if (output.getMessage() != null) {
                        message = output.getMessage().get();
                    }
                }
                if (message != null) {
                    Collection<Part> parts = message.getParts();
                    if (!parts.isEmpty()) {
                        name = parts.iterator().next().getName();
                    }
                }
            }
        }
        return name;
    }

    /**
     * BooleanOptions rendered into a serializable form for the WSDL output.
     *
     * @param optionSets One or more arrays of BooleanOption objects.
     *
     * @return String consisting of the options represented by their constants
     *         separated by commas.
     */
    private String serializeBooleanOptions(BooleanOption[]... optionSets) {
        StringBuffer buffer = new StringBuffer();
        for (BooleanOption[] options : optionSets) {
            for (BooleanOption option : options) {
                if (option.value()) {
                    buffer.append(option.name()).append(",");
                }
            }
        }
        int last = buffer.length() - 1;
        if (last >= 0) {
            buffer.deleteCharAt(last);
        }
        return buffer.toString();
    }

    private void parse(MQAddress address) {
        assert address != null;
        setHost(address.getHostName());
        setPort(address.getPortNumber());
        setChannel(address.getChannelName());
        setCipherSuite(address.getCipherSuite());
        setSslPeerName(address.getSslPeerName());
        setQueueManager(address.getQueueManagerName());
    }

    private void parse(MQOperation operation) {
        assert operation != null;

        setQueue(operation.getQueueName());
        setPolling(operation.getPollingInterval());

        useTransaction.accept(operation.getTransaction());
        setIsTransactional(useTransaction.value());

        parseQueueOpenOptions(operation.getQueueOpenOptions());
    }

    private void parse(MQBody inputBody, MQBody outputBody) {
        assert inputBody != null;

        messageType.accept(inputBody.getMessageType());
        String messageTypeChoice = messageType.choice();
        setMessageType(messageTypeChoice);
        setIsSyncpoint(inputBody.getSyncpoint());

        if (outputBody != null) {
            messageType.accept(outputBody.getMessageType());
            if (!messageTypeChoice.equals(messageType.choice())) {
                if (messageType.value()) {
                    setMessageType(messageType.choice());
                }
            }
        }
    }

    private void parseQueueOpenOptions(String optionValues) {
        assert optionValues != null;
        StringTokenizer options = new StringTokenizer(optionValues, ",");
        while (options.hasMoreTokens()) {
            String option = Utils.safeString(options.nextToken());
            if (option.toUpperCase().startsWith("BIND")) {
                useDefaultBindingOption.accept(option);
                useNoBindingOption.accept(option);
                useOnOpenBindingOption.accept(option);
                setIsDefaultBindingOption(useDefaultBindingOption.value());
                setIsNoBindingOption(useNoBindingOption.value());
                setIsOnOpenBindingOption(useOnOpenBindingOption.value());
            } else if (option.toUpperCase().startsWith("READ")) {
                useDefaultReadOption.accept(option);
                useSharedReadOption.accept(option);
                useExclusiveReadOption.accept(option);
                setIsDefaultReadOption(useDefaultReadOption.value());
                setIsSharedReadOption(useSharedReadOption.value());
                setIsExclusiveReadOption(useExclusiveReadOption.value());
            }
        }
    }

    private boolean isReferencing(NamedComponentReference obj) {
        return obj != null && obj.get() != null;
    }

    public synchronized void setHost(String value) {
        super.setHost(value);
        updateModel();
    }

    public synchronized void setPort(String value) {
        super.setPort(value);
        updateModel();
    }

    public synchronized void setQueue(String value) {
        super.setQueue(value);
        updateModel();
    }

    public synchronized void setQueueManager(String value) {
        super.setQueueManager(value);
        updateModel();
    }

    public synchronized void setChannel(String value) {
        super.setChannel(value);
        updateModel();
    }

    public synchronized void setPolling(String value) {
        super.setPolling(value);
        updateModel();
    }

    public synchronized void setIsTransactional(boolean useTransaction) {
        super.setIsTransactional(useTransaction);
        updateModel();
    }

    public synchronized void setIsDefaultBindingOption(boolean use) {
        super.setIsDefaultBindingOption(use);
        updateModel();
    }

    public synchronized void setIsOnOpenBindingOption(boolean use) {
        super.setIsOnOpenBindingOption(use);
        updateModel();
    }

    public synchronized void setIsNoBindingOption(boolean use) {
        super.setIsNoBindingOption(use);
        updateModel();
    }

    public synchronized void setIsDefaultReadOption(boolean use) {
        super.setIsDefaultReadOption(use);
        updateModel();
    }

    public synchronized void setIsExclusiveReadOption(boolean use) {
        super.setIsExclusiveReadOption(use);
        updateModel();
    }

    public synchronized void setIsSharedReadOption(boolean use) {
        super.setIsSharedReadOption(use);
        updateModel();
    }

    public synchronized void setIsSyncpoint(boolean useSyncpoint) {
        super.setIsSyncpoint(useSyncpoint);
        updateModel();
    }

    public synchronized void setPassword(char[] value) {
        super.setPassword(value);
        updateModel();
    }

    public synchronized void setUsername(String value) {
        super.setUsername(value);
        updateModel();
    }

    public synchronized void setCipherSuite(String value) {
        super.setCipherSuite(value);
        updateModel();
    }

    public synchronized void setSslPeerName(String value) {
        super.setSslPeerName(value);
        updateModel();
    }

    @Override
    public synchronized void adopt(Form.FormModel model) {
        suspendModelUpdate = true;
        super.adopt(model);
        suspendModelUpdate = false;
    }

    private WSDLModel model;
    private volatile boolean suspendModelUpdate;
    private volatile Operation operationFocus;
    private final WSDLWizardContext context;

    private final BinaryAliasedChoiceOption messageType =
            new BinaryAliasedChoiceOption(
                    new String[]{"TextMessage", "Text"},
                    new String[]{"BinaryMessage", "Binary"}
            );
    private final BinaryChoiceOption useTransaction =
            new BinaryChoiceOption("XATransaction", "NoTransaction");
    private final BooleanOption useDefaultBindingOption =
            new BooleanOption("BIND_AS_DEFAULT");
    private final BooleanOption useNoBindingOption =
            new BooleanOption("BIND_NOT_FIXED");
    private final BooleanOption useOnOpenBindingOption =
            new BooleanOption("BIND_ON_OPEN");
    private final BooleanOption useDefaultReadOption =
            new BooleanOption("READ_AS_DEFAULT");
    private final BooleanOption useSharedReadOption =
            new BooleanOption("READ_SHARED");
    private final BooleanOption useExclusiveReadOption =
            new BooleanOption("READ_EXCLUSIVE");
    private final BooleanOption[] bindOptions = new BooleanOption[]{
            useDefaultBindingOption, useNoBindingOption, useOnOpenBindingOption,
    };
    private final BooleanOption[] readOptions = new BooleanOption[]{
            useDefaultReadOption, useExclusiveReadOption, useSharedReadOption,
    };

    private abstract class Option<T> {
        public abstract String name();

        public abstract boolean accept(Object value);

        public abstract T value();
    }

    /** An Option that is either true or false. */
    private class BooleanOption
            extends Option<Boolean> {
        private boolean value = true;
        private final String name;

        BooleanOption(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

        public synchronized boolean accept(Object value) {
            if (value instanceof String) {
                this.value = ((String) value).equalsIgnoreCase(name());
            } else {
                this.value = Boolean.valueOf(String.valueOf(value));
            }
            return this.value;
        }

        public synchronized Boolean value() {
            return value;
        }
    }

    /** An Option with two possible values. */
    private class BinaryChoiceOption
            extends BooleanOption {
        private final String alternateValue;

        BinaryChoiceOption(String name, String alternateValue) {
            super(name);
            this.alternateValue = alternateValue;
        }

        public String choice() {
            return (value() ? name() : alternateValue);
        }
    }

    /**
     * An Option with two possible values that have multiple representations.
     */
    private class BinaryAliasedChoiceOption
            extends BinaryChoiceOption {
        private final String[] values;
        private final String[] altValues;
        
        private BinaryAliasedChoiceOption(String[] values, String[] altvalues) {
            super(values[0], altvalues[0]);
            
            this.values = new String[values.length];
            System.arraycopy(values, 0, this.values, 0, this.values.length);
            
            this.altValues = new String[altvalues.length];
            System.arraycopy(altvalues, 0, this.altValues, 0, this.altValues.length);
        }

        private boolean isAlias(Object value) {
            String strValue = value.toString();
            for (String name : values) {
                if (name.equalsIgnoreCase(strValue)) {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public boolean accept(Object value) {
            return super.accept(value)
                    || (isAlias(value) && super.accept(values[0]));
        }
    }
}
