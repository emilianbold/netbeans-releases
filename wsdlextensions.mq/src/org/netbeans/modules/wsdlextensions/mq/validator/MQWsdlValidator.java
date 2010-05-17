/*
 * BEGIN_HEADER - DO NOT EDIT
 * 
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 */

package org.netbeans.modules.wsdlextensions.mq.validator;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javax.xml.namespace.QName;

import org.netbeans.modules.wsdlextensions.mq.MQAddress;
import org.netbeans.modules.wsdlextensions.mq.MQBinding;
import org.netbeans.modules.wsdlextensions.mq.MQBody;
import org.netbeans.modules.wsdlextensions.mq.MQFault;
import org.netbeans.modules.wsdlextensions.mq.MQHeader;
import org.netbeans.modules.wsdlextensions.mq.MQOperation;
import org.netbeans.modules.wsdlextensions.mq.MQRedelivery;
import org.netbeans.modules.wsdlextensions.mq.MessageDescriptors;
import org.netbeans.modules.wsdlextensions.mq.XmlSchemaDataTypes;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.spi.Validator;

public class MQWsdlValidator {
    private static final ResourceBundle mMessages =
            ResourceBundle.getBundle(
                    "org.netbeans.modules.wsdlextensions.mq.validator.Bundle");
    
    private final Validator mOwner;
    
    private final Set<Validator.ResultItem> mResults =
            new HashSet<Validator.ResultItem>();


    /**
     * Constructor.
     * @param owner org.netbeans.modules.xml.xam.spi.Validator object
     */
    public MQWsdlValidator(Validator owner) {
        mOwner = owner;
    }

    /**
     * Process the document model. If the evaluation produces error or warning
     * results, these are internally kept, and can be accessed by a subsequent
     * call to {@link #getValidationResults()}.
     * <p/>
     * Every call to this method resets the internal result set.
     *
     * @param model The document model to examine.
     */
    public void validate(WSDLModel model) {
        Definitions defs = model.getDefinitions();
        synchronized (mResults) {
            mResults.clear();
            validateBindings(defs.getBindings());
            validateServices(defs.getServices());
        }
    }

    /**
     * Obtain the results corresponding to the most recent call to
     * {@link #validate(org.netbeans.modules.xml.wsdl.model.WSDLModel)}
     * @return An unmodifiable set of results.
     */
    public Set<Validator.ResultItem> getValidationResults() {
        synchronized (mResults) {
            return Collections.unmodifiableSet(mResults);
        }
    }
    
    private void validateBindings(Collection<Binding> bindingList) {
        assert bindingList != null;
        
        for (Binding binding: bindingList) {
            if (binding.getType() == null || binding.getType().get() == null) {
                continue;
            }
            // Only interested in definitions with MQ BC binding.
            if (!hasMqBinding(binding)) {
                continue;
            }
            validateBindingOps(binding.getBindingOperations(), binding);
        }
    }

    private boolean isOneWayOp(BindingOperation bindingOp) {
        return (bindingOp.getBindingOutput() == null);
    }
    
    private void validateBindingOps(Collection<BindingOperation> bindingList,
                                    Binding binding) {
        assert bindingList != null;
        assert binding != null;

        // At least one operation in the binding must have an MQ binding
        // so I keep a running count of how many are found.
        int foundOps = 0;

        for (BindingOperation bindingOp : bindingList) {
            // Operations
            List<MQOperation> opList = bindingOp.getExtensibilityElements(
                    MQOperation.class);
            if (opList.size() == 0) {
                continue;
            }
            foundOps += 1;
            validateOperations(opList, bindingOp);

            // Redeliveries
            List<MQRedelivery> redeliveryList = bindingOp.getExtensibilityElements(
                    MQRedelivery.class);
            if (redeliveryList.size() != 0) {
                validateRedeliveries(redeliveryList, bindingOp);
            }

            // One-Way Operations must have an input MQ binding.
            // Request-Response ops must have input and output MQ binding.
            boolean isOneWayOperation = false;
            boolean foundMqInputBinding = false;
            boolean foundMqOutputBinding = false;
            
            // Input:
            // 1. Validate MQ Body extensibles
            // 2. Validate MQ Headers
            BindingInput input = bindingOp.getBindingInput();
            if (input != null) {
                List<MQBody> bodyList =
                        input.getExtensibilityElements(MQBody.class);
                isOneWayOperation = true;
                foundMqInputBinding = bodyList.size() > 0;
                if (bodyList.size() > 1) {
                    mResults.add(new Validator.ResultItem(mOwner,
                            Validator.ResultType.ERROR,
                            input,
                            getMessage(
                                    "MQBindingValidation.ATMOST_ONE_MQBODY_INPUT",
                                    bindingOp.getName())));
                } else if (bodyList.size() > 0) {
                    validateBody(bodyList, input, bindingOp);
                    List<MQHeader> headerList =
                            input.getExtensibilityElements(MQHeader.class);
                    if (headerList.size() > 0) {
                        validateHeaders(headerList, input, bindingOp);
                    }
                }
            }
            
            // Output: same as Input.
            BindingOutput output = bindingOp.getBindingOutput();
            if (output != null) {
                List<MQBody> bodyList =
                        output.getExtensibilityElements(MQBody.class);
                isOneWayOperation = false;
                foundMqOutputBinding = bodyList.size() > 0;
                if (bodyList.size() > 1) {
                    mResults.add(new Validator.ResultItem(mOwner,
                            Validator.ResultType.ERROR,
                            output,
                            getMessage(
                                    "MQBindingValidation.ATMOST_ONE_MQBODY_OUTPUT",
                                    bindingOp.getName())));
                } else if (bodyList.size() > 0) {
                    validateBody(bodyList, output, bindingOp);
                    List<MQHeader> headerList =
                            output.getExtensibilityElements(MQHeader.class);
                    if (headerList.size() > 0) {
                        validateHeaders(headerList, output, bindingOp);
                    }
                }
            }
            
            // Fault
            Collection<BindingFault> faults = bindingOp.getBindingFaults();
            if (faults != null) {
                for (BindingFault fault : faults) {
                    List<MQFault> faultList = fault.getExtensibilityElements(MQFault.class);
                    if (faultList.size() > 1) {
                        mResults.add(new Validator.ResultItem(mOwner,
                                Validator.ResultType.ERROR,
                                fault,
                                getMessage(
                                        "MQFault.ATMOST_ONE_MQFAULT",
                                        bindingOp.getName())));
                    } else if (faultList.size() > 0) {
                        validateFault(faultList, fault, bindingOp);
                    }
                }
            }
            
            if (isOneWayOperation) {
                if (!foundMqInputBinding) {
                    mResults.add(new Validator.ResultItem(mOwner,
                            Validator.ResultType.ERROR,
                            input,
                            getMessage(
                                    "MQBindingValidation.NO_INPUT",
                                    bindingOp.getName())));
                }
            } else {
                if (!foundMqInputBinding) {
                    mResults.add(new Validator.ResultItem(mOwner,
                            Validator.ResultType.ERROR,
                            input,
                            getMessage(
                                    "MQBindingValidation.NO_INPUT",
                                    bindingOp.getName())));
                }
                if (!foundMqOutputBinding) {
                    mResults.add(new Validator.ResultItem(mOwner,
                            Validator.ResultType.ERROR,
                            output,
                            getMessage(
                                    "MQBindingValidation.NO_OUTPUT",
                                    bindingOp.getName())));
                }
            }
        }

        // No MQ operations were found
        if (foundOps == 0) {
            mResults.add(new Validator.ResultItem(mOwner,
                    Validator.ResultType.ERROR,
                    binding,
                    getMessage("MQBindingValidation.MISSING_MQ_OPERATION",
                            binding.getName())));
        }
    }

    private void validateOperations(Collection<MQOperation> opList,
                                    BindingOperation bindingOp) {
        assert opList != null;
        assert bindingOp != null;
        
        for (MQOperation op : opList) {
            
            // Queue name is mandatory
            if (isEmpty(op.getQueueName())) {
                mResults.add(new Validator.ResultItem(mOwner,
                        Validator.ResultType.ERROR,
                        op,
                        getMessage("MQOperation.EMPTY_QUEUENAME_EMPTY",
                                bindingOp.getName())));
            }
        }
    }
    
    private void validateRedeliveries(Collection<MQRedelivery> deliveryList,
                                      BindingOperation bindingOp) {
        assert deliveryList != null;
        assert bindingOp != null;
        
        boolean isOneWay = isOneWayOp(bindingOp);
        if (!isOneWay) {
            mResults.add(new Validator.ResultItem(mOwner,
                    Validator.ResultType.WARNING,
                    bindingOp,
                    getMessage("MQRedelivery.REDELIVERY_IRRELEVANT_TWOWAY",
                            bindingOp.getName())));
            return;
        }
        
        for (MQRedelivery redelivery : deliveryList) {
            
            // Count, if present, must be zero or positive
            String count = redelivery.getCount();
            if (!isEmpty(count)) {
                try {
                    int value = Integer.parseInt(count);
                    if (value < 0) {
                        mResults.add(new Validator.ResultItem(mOwner,
                                Validator.ResultType.ERROR,
                                redelivery,
                                getMessage("MQRedelivery.COUNT_INVALID",
                                        bindingOp.getName(),
                                        count)));
                    }
                } catch (NumberFormatException e) {
                    mResults.add(new Validator.ResultItem(mOwner,
                            Validator.ResultType.ERROR,
                            redelivery,
                            getMessage("MQRedelivery.COUNT_INVALID",
                                    bindingOp.getName(),
                                    count)));
                }
            }
            
            // Delay, if present, must be zero or positive
            String delay = redelivery.getDelay();
            if (!isEmpty(delay)) {
                try {
                    int value = Integer.parseInt(delay);
                    if (value < 0) {
                        mResults.add(new Validator.ResultItem(mOwner,
                                Validator.ResultType.ERROR,
                                redelivery,
                                getMessage("MQRedelivery.DELAY_INVALID",
                                        bindingOp.getName(),
                                        delay)));
                    }
                } catch (NumberFormatException e) {
                    mResults.add(new Validator.ResultItem(mOwner,
                            Validator.ResultType.ERROR,
                            redelivery,
                            getMessage("MQRedelivery.DELAY_INVALID",
                                    bindingOp.getName(),
                                    delay)));
                }
            }
        }
    }

    private void validateBody(Collection<MQBody> bodyList,
                              BindingInput input,
                              BindingOperation bindingOp) {
        assert bodyList != null;
        assert input != null;
        assert bindingOp != null;
        
        for (MQBody body : bodyList) {
            
            // messageType is mandatory
            String msgType = body.getMessageType();
            if (isEmpty(msgType)) {
                mResults.add(new Validator.ResultItem(mOwner,
                        Validator.ResultType.ERROR,
                        body,
                        getMessage("MQMessage.INPUT_MESSAGETYPE_UNSPECIFIED",
                                bindingOp.getName())));
            } else if (MQBody.TEXT_MESSAGE.equals(msgType)) {
                // messageBody is required if this is a request-only operation
                boolean isRequestOnly = bindingOp.getBindingOutput() == null;
                String bodyPartAttribute = body.getMessageBodyPart();
                if (isEmpty(bodyPartAttribute)) {
                    if (isRequestOnly) {
                        mResults.add(new Validator.ResultItem(mOwner,
                                Validator.ResultType.ERROR,
                                body,
                                getMessage("MQMessage.INPUT_MESSAGEBODY_UNSPECIFIED",
                                        bindingOp.getName())));
                    }
                }
                // messageBody not meaningful if request-reply operation
                else if (!isRequestOnly) {
                    mResults.add(new Validator.ResultItem(mOwner,
                            Validator.ResultType.WARNING,
                            body,
                            getMessage("MQMessage.MESSAGEBODY_IGNORED_FOR_REQUEST_REPLY",
                                    bindingOp.getName())));
                } else {
                    // Verify that the referenced message part exists
                    if (input.getInput() == null
                            || input.getInput().get() == null
                            || input.getInput().get().getMessage() == null
                            || input.getInput().get().getMessage().get() == null) {
                        mResults.add(new Validator.ResultItem(mOwner,
                                Validator.ResultType.ERROR,
                                body,
                                getMessage("MQMessage.INPUT_MESSAGE_REFERENCE_MISSING",
                                        bindingOp.getName())));
                    }
                    Part part = getPart(input.getInput().get().getMessage().get(),
                            bodyPartAttribute);
                    if (part == null) {
                        mResults.add(new Validator.ResultItem(mOwner,
                                Validator.ResultType.ERROR,
                                body,
                                getMessage(
                                        "MQMessage.INPUT_BODY_PART_REFERENCE_MISSING",
                                        bindingOp.getName(),
                                        bodyPartAttribute)));
                    } else if (part.getType() != null) {
                        QName type = part.getType().getQName();
                        if (!type.equals(XmlSchemaDataTypes.STRING.qname)) {
                            mResults.add(new Validator.ResultItem(mOwner,
                                    Validator.ResultType.ERROR,
                                    body,
                                    getMessage(
                                            "MQMessage.BODY_PART_NEED_STRING_FOR_TEXTMESSAGE",
                                            bindingOp.getName(),
                                            bodyPartAttribute)));
                        }
                    } else if (part.getElement() == null) {
                        mResults.add(new Validator.ResultItem(mOwner,
                                Validator.ResultType.ERROR,
                                body,
                                getMessage(
                                        "MQMessage.BODY_PART_NEITHER_PART_OR_ELEMENT",
                                        bindingOp.getName(),
                                        bodyPartAttribute)));
                    }
                }
            }
        }
    }
    
    private void validateBody(Collection<MQBody> bodyList,
                              BindingOutput output,
                              BindingOperation bindingOp) {
        assert bodyList != null;
        assert output != null;
        assert bindingOp != null;
        
        for (MQBody body : bodyList) {
            
            // messageType is mandatory
            String msgType = body.getMessageType();
            if (isEmpty(msgType)) {
                mResults.add(new Validator.ResultItem(mOwner,
                        Validator.ResultType.ERROR,
                        body,
                        getMessage("MQMessage.OUTPUT_MESSAGETYPE_UNSPECIFIED",
                                bindingOp.getName())));
            } else if (MQBody.TEXT_MESSAGE.equals(msgType)) {
                // messageBody is mandatory
                String bodyPartAttribute = body.getMessageBodyPart();
                if (isEmpty(bodyPartAttribute)) {
                    mResults.add(new Validator.ResultItem(mOwner,
                            Validator.ResultType.ERROR,
                            body,
                            getMessage("MQMessage.OUTPUT_MESSAGEBODY_UNSPECIFIED",
                                    bindingOp.getName())));
                } else {
                    // Verify that the referenced message part exists
                    if (output.getOutput() == null
                            || output.getOutput().get() == null
                            || output.getOutput().get().getMessage() == null
                            || output.getOutput().get().getMessage().get() == null) {
                        mResults.add(new Validator.ResultItem(mOwner,
                                Validator.ResultType.ERROR,
                                body,
                                getMessage("MQMessage.OUTPUT_MESSAGE_REFERENCE_MISSING",
                                        bindingOp.getName())));
                    }
                    Part part = getPart(output.getOutput().get().getMessage().get(),
                            bodyPartAttribute);
                    if (part == null) {
                        mResults.add(new Validator.ResultItem(mOwner,
                                Validator.ResultType.ERROR,
                                body,
                                getMessage(
                                        "MQMessage.OUTPUT_BODY_PART_REFERENCE_MISSING",
                                        bindingOp.getName(),
                                        bodyPartAttribute)));
                    } else if (part.getType() != null) {
                        QName type = part.getType().getQName();
                        if (!type.equals(XmlSchemaDataTypes.STRING.qname)) {
                            mResults.add(new Validator.ResultItem(mOwner,
                                    Validator.ResultType.ERROR,
                                    body,
                                    getMessage(
                                            "MQMessage.BODY_PART_NEED_STRING_FOR_TEXTMESSAGE",
                                            bindingOp.getName(),
                                            bodyPartAttribute)));
                        }
                    } else if (part.getElement() == null) {
                        mResults.add(new Validator.ResultItem(mOwner,
                                Validator.ResultType.ERROR,
                                body,
                                getMessage(
                                        "MQMessage.BODY_PART_NEITHER_PART_OR_ELEMENT",
                                        bindingOp.getName(),
                                        bodyPartAttribute)));
                    }
                }
            }
        }
    }
    
    private void validateHeaders(Collection<MQHeader> headerList,
                                 BindingInput input,
                                 BindingOperation bindingOp) {
        assert headerList != null;
        assert input != null;
        assert bindingOp != null;

        for (MQHeader header : headerList) {
            
            // Ensure that a valid descriptor is named.
            String descriptorName = header.getDescriptor();
            MessageDescriptors descriptor = null;
            for (MessageDescriptors desc : MessageDescriptors.values()) {
                if (desc.name().equalsIgnoreCase(descriptorName)) {
                    descriptor = desc;
                    break;
                }
            }
            if (descriptor == null) {
                mResults.add(new Validator.ResultItem(mOwner,
                        Validator.ResultType.ERROR,
                        header,
                        getMessage("MQMessage.HEADER_DESCRIPTOR_UNSPECIFIED",
                                bindingOp.getName())));
            }
            
            // Ensure that the message part referenced exists.
            String partName = header.getPart();
            Part part = null;
            if (isEmpty(partName)) {
                mResults.add(new Validator.ResultItem(mOwner,
                        Validator.ResultType.ERROR,
                        header,
                        getMessage("MQMessage.HEADER_PART_UNSPECIFIED",
                                bindingOp.getName(),
                                descriptor.name())));
            } else {
                if (input.getInput() == null
                        || input.getInput().get() == null
                        || input.getInput().get().getMessage() == null
                        || input.getInput().get().getMessage().get() == null) {
                    mResults.add(new Validator.ResultItem(mOwner,
                            Validator.ResultType.ERROR,
                            header,
                            getMessage("MQMessage.INPUT_MESSAGE_REFERENCE_MISSING",
                                    bindingOp.getName())));
                } else {
                    part = getPart(input.getInput().get().getMessage().get(), partName);
                    if (part == null) {
                        mResults.add(new Validator.ResultItem(mOwner,
                                Validator.ResultType.ERROR,
                                header,
                                getMessage("MQMessage.HEADER_PART_UNKNOWN",
                                        bindingOp.getName(),
                                        descriptor.name(),
                                        partName)));
                    }
                }
            }
            
            // The message part must be type-compatible to the descriptor.
            if (descriptor != null && part != null) {
                if (part.getType() == null) {
                    mResults.add(new Validator.ResultItem(mOwner,
                            Validator.ResultType.ERROR,
                            header,
                            getMessage("MQMessage.INPUT_PART_TYPE_REFERENCE_MISSING",
                                    bindingOp.getName(),
                                    partName)));
                } else {
                    QName type = part.getType().getQName();
                    if (!descriptor.isRepresentableAs(type)) {
                        Set<QName> forms = descriptor.getRepresentations();
                        StringBuffer formBuffer = new StringBuffer();
                        for (QName form : forms) {
                            formBuffer.append(form.toString()).append('\n');
                        }
                        mResults.add(new Validator.ResultItem(mOwner,
                                Validator.ResultType.ERROR,
                                header,
                                getMessage("MQMessage.HEADER_PART_TYPE_UNSUPPORTED",
                                        bindingOp.getName(),
                                        descriptor.name(),
                                        partName,
                                        type.toString(),
                                        formBuffer.toString())));
                    }
                }
            }
        }
    }
    
    private void validateHeaders(Collection<MQHeader> headerList,
                                 BindingOutput output,
                                 BindingOperation bindingOp) {
        assert headerList != null;
        assert output != null;
        assert bindingOp != null;

        for (MQHeader header : headerList) {
            
            // Ensure that a valid descriptor is named.
            String descriptorName = header.getDescriptor();
            MessageDescriptors descriptor = null;
            for (MessageDescriptors desc : MessageDescriptors.values()) {
                if (desc.name().equalsIgnoreCase(descriptorName)) {
                    descriptor = desc;
                    break;
                }
            }
            if (descriptor == null) {
                mResults.add(new Validator.ResultItem(mOwner,
                        Validator.ResultType.ERROR,
                        header,
                        getMessage("MQMessage.HEADER_DESCRIPTOR_UNSPECIFIED",
                                bindingOp.getName())));
            }
            
            // Ensure that the message part referenced exists.
            String partName = header.getPart();
            Part part = null;
            if (isEmpty(partName)) {
                mResults.add(new Validator.ResultItem(mOwner,
                        Validator.ResultType.ERROR,
                        header,
                        getMessage("MQMessage.HEADER_PART_UNSPECIFIED",
                                bindingOp.getName(),
                                descriptor.name())));
            } else {
                if (output.getOutput() == null
                        || output.getOutput().get() == null
                        || output.getOutput().get().getMessage() == null
                        || output.getOutput().get().getMessage().get() == null) {
                    mResults.add(new Validator.ResultItem(mOwner,
                            Validator.ResultType.ERROR,
                            header,
                            getMessage("MQMessage.OUTPUT_MESSAGE_REFERENCE_MISSING",
                                    bindingOp.getName())));
                } else {
                    part = getPart(output.getOutput().get().getMessage().get(), partName);
                    if (part == null) {
                        mResults.add(new Validator.ResultItem(mOwner,
                                Validator.ResultType.ERROR,
                                header,
                                getMessage("MQMessage.HEADER_PART_UNKNOWN",
                                        bindingOp.getName(),
                                        descriptor.name(),
                                        partName)));
                    }
                }
            }
            
            // The message part must be type-compatible to the descriptor.
            if (descriptor != null && part != null) {
                if (part.getType() == null) {
                    mResults.add(new Validator.ResultItem(mOwner,
                            Validator.ResultType.ERROR,
                            header,
                            getMessage("MQMessage.OUTPUT_PART_TYPE_REFERENCE_MISSING",
                                    bindingOp.getName(),
                                    partName)));
                } else {
                    QName type = part.getType().getQName();
                    if (!descriptor.isRepresentableAs(type)) {
                        Set<QName> forms = descriptor.getRepresentations();
                        StringBuffer formBuffer = new StringBuffer();
                        for (QName form : forms) {
                            formBuffer.append(form.toString()).append('\n');
                        }
                        mResults.add(new Validator.ResultItem(mOwner,
                                Validator.ResultType.ERROR,
                                header,
                                getMessage("MQMessage.HEADER_PART_TYPE_UNSUPPORTED",
                                        bindingOp.getName(),
                                        descriptor.name(),
                                        partName,
                                        type.toString(),
                                        formBuffer.toString())));
                    }
                }
            }
        }
    }

    private void validateFault(Collection<MQFault> faults,
                               BindingFault bindingFault,
                               BindingOperation bindingOp) {
        assert faults != null;
        assert bindingOp != null;
        
        for (MQFault fault : faults) {
            
            // reasonCodePart is mandatory
            String reasonCodePartName = fault.getReasonCodePart();
            if (isEmpty(reasonCodePartName)) {
                mResults.add(new Validator.ResultItem(mOwner,
                        Validator.ResultType.ERROR,
                        fault,
                        getMessage("MQFault.REASONCODE_PART_UNSPECIFIED",
                                bindingOp.getName())));
                continue;
            }

            
            // Verify that the referenced message parts exists
            if (bindingFault.getFault() == null
                    || bindingFault.getFault().get() == null
                    || bindingFault.getFault().get().getMessage() == null
                    || bindingFault.getFault().get().getMessage().get() == null) {
                mResults.add(new Validator.ResultItem(mOwner,
                        Validator.ResultType.ERROR,
                        fault,
                        getMessage("MQFault.MESSAGE_REFERENCE_MISSING",
                                bindingOp.getName())));
                return;
            }
            
            // reasonCodePart check
            Part reasonCodePart = getPart(bindingFault.getFault().get().getMessage().get(),
                    reasonCodePartName);
            if (reasonCodePart == null) {
                mResults.add(new Validator.ResultItem(mOwner,
                        Validator.ResultType.ERROR,
                        fault,
                        getMessage(
                                "MQFault.REASONCODE_PART_REFERENCE_MISSING",
                                bindingOp.getName(),
                                reasonCodePartName)));
            } else if (reasonCodePart.getType() != null) {
                QName type = reasonCodePart.getType().getQName();
                if (
                        (XmlSchemaDataTypes.STRING.qname.equals(type)
                                || XmlSchemaDataTypes.INTEGER.qname.equals(type)
                        )
                        ||
                        (XmlSchemaDataTypes.STRING.isDerivableTo(type)
                                && !XmlSchemaDataTypes.TOKEN.isDerivableTo(type)
                        )
                        ||
                        (XmlSchemaDataTypes.INTEGER.isDerivableTo(type)
                                && !XmlSchemaDataTypes.NONPOSITIVEINTEGER.qname.equals(type)
                                && !XmlSchemaDataTypes.NONPOSITIVEINTEGER.isDerivableTo(type)
                        )) {
                    // valid; fall-thru
                } else {
                    StringBuffer buffer = new StringBuffer();
                    Collection<QName> types = XmlSchemaDataTypes.STRING.getLineage();
                    types.removeAll(XmlSchemaDataTypes.TOKEN.getLineage());
                    types.add(XmlSchemaDataTypes.TOKEN.qname);
                    types.addAll(XmlSchemaDataTypes.INTEGER.getLineage());
                    types.removeAll(XmlSchemaDataTypes.NONPOSITIVEINTEGER.getLineage());
                    types.removeAll(XmlSchemaDataTypes.SHORT.getLineage());
                    types.add(XmlSchemaDataTypes.SHORT.qname);
                    for (QName name : types) {
                        buffer.append(name).append("\n");
                    }
                    mResults.add(new Validator.ResultItem(mOwner,
                            Validator.ResultType.ERROR,
                            fault,
                            getMessage(
                                    "MQFault.REASONCODE_PART_BAD_TYPE",
                                    bindingOp.getName(),
                                    reasonCodePartName,
                                    buffer.toString())));
                }
            } else {
                mResults.add(new Validator.ResultItem(mOwner,
                        Validator.ResultType.ERROR,
                        fault,
                        getMessage(
                                "MQFault.REASONCODE_PART_ELEMENT_UNSUPPORTED",
                                bindingOp.getName(),
                                reasonCodePartName)));
            }
            
            // reasonTextPart check.  It is not mandatory, but if present
            // its value must be validated
            String reasonTextPartName = fault.getReasonTextPart();
            if (!isEmpty(reasonTextPartName)) {
                Part reasonTextPart = getPart(bindingFault.getFault().get().getMessage().get(),
                        reasonTextPartName);
                if (reasonTextPart == null) {
                    mResults.add(new Validator.ResultItem(mOwner,
                            Validator.ResultType.ERROR,
                            fault,
                            getMessage(
                                    "MQFault.REASONTEXT_PART_REFERENCE_MISSING",
                                    bindingOp.getName(),
                                    reasonTextPartName)));
                } else if (reasonTextPart.getType() != null) {
                    QName type = reasonTextPart.getType().getQName();
                    if (
                            XmlSchemaDataTypes.STRING.qname.equals(type)
                            ||
                            (XmlSchemaDataTypes.STRING.isDerivableTo(type)
                                    && !XmlSchemaDataTypes.TOKEN.qname.equals(type)
                                    && !XmlSchemaDataTypes.TOKEN.isDerivableTo(type)
                            )) {
                        // valid; fall-thru
                    } else {
                        StringBuffer buffer = new StringBuffer();
                        Collection<QName> types = XmlSchemaDataTypes.STRING.getLineage();
                        types.removeAll(XmlSchemaDataTypes.TOKEN.getLineage());
                        for (QName name : types) {
                            buffer.append(name).append("\n");
                        }
                        mResults.add(new Validator.ResultItem(mOwner,
                                Validator.ResultType.ERROR,
                                fault,
                                getMessage(
                                        "MQFault.REASONTEXT_PART_BAD_TYPE",
                                        bindingOp.getName(),
                                        reasonTextPartName,
                                        buffer.toString())));
                    }
                } else {
                    mResults.add(new Validator.ResultItem(mOwner,
                            Validator.ResultType.ERROR,
                            fault,
                            getMessage(
                                    "MQFault.REASONTEXT_PART_ELEMENT_UNSUPPORTED",
                                    bindingOp.getName(),
                                    reasonTextPartName)));
                }
            
            }
            
            // reasonCode and reasonText cannot reference the same part
            if (!isEmpty(reasonTextPartName)) {
                if (reasonTextPartName.equals(reasonCodePartName)) {
                    mResults.add(new Validator.ResultItem(mOwner,
                            Validator.ResultType.ERROR,
                            fault,
                            getMessage(
                                    "MQFault.REASON_PARTS_IDENTICAL",
                                    bindingOp.getName(),
                                    reasonTextPartName)));
                }
            }
        }
    }
    
    private void validateServices(Collection<Service> serviceList) {
        assert serviceList != null;
        
        for (Service service : serviceList) {
            validatePorts(service.getPorts());
        }
    }

    private void validatePorts(Collection<Port> portList) {
        assert portList != null;
        
        for (Port port : portList) {
            if (port.getBinding() == null || port.getBinding().get() == null) {
                continue;
            }
            if (!hasMqBinding(port.getBinding().get())) {
                continue;
            }
            
            // MQAddress
            List<MQAddress> addressList = port.getExtensibilityElements(
                    MQAddress.class);
            if (addressList.size() == 0) {
                mResults.add(new Validator.ResultItem(mOwner,
                        Validator.ResultType.ERROR,
                        port,
                        getMessage("MQAddressValidation.MISSING_MQ_ADDRESS",
                                port.getName())));

            } else if (addressList.size() > 1) {
                mResults.add(new Validator.ResultItem(mOwner,
                        Validator.ResultType.ERROR,
                        port,
                        getMessage(
                                "MQAddressValidation.ONLY_ONE_MQ_ADDRESS_ALLOWED",
                                port.getName())));
            } else {
                validateAddress(addressList);
            }
        }
    }

    private void validateAddress(Collection<MQAddress> addressList) {
        assert addressList != null;
        
        for (MQAddress address : addressList) {
            // Queue manager
            String qmgrName = address.getQueueManagerName();
            if (qmgrName == null || qmgrName.trim().equals("")) {
                mResults.add(new Validator.ResultItem(mOwner,
                        Validator.ResultType.ERROR,
                        address,
                        getMessage("MQAddressValidation.NO_QMGR_SPECIFIED")));
            }

            // Host name
            String hostname = address.getHostName();
            if (hostname == null || hostname.trim().equals("")) {
                mResults.add(new Validator.ResultItem(mOwner,
                        Validator.ResultType.WARNING,
                        address,
                        getMessage("MQAddressValidation.ADVICE_HOSTNAME")));

            }
            
            validateForSslConnection(address);
        }
    }
    
    private void validateForSslConnection(MQAddress address) {
        String cipherSuite = address.getCipherSuite();
        String sslPeerName = address.getSslPeerName();
        
        // cipherSuite - optional
        if (cipherSuite != null) {
            if (cipherSuite.trim().equals("")) {
                mResults.add(new Validator.ResultItem(mOwner,
                        Validator.ResultType.ERROR,
                        address,
                        getMessage("MQAddressValidation.MISSING_CIPHERSUITE")));
            }
        }
        
        // sslPeerName - optional
        if (sslPeerName != null) {
            if (sslPeerName.trim().equals("")) {
                mResults.add(new Validator.ResultItem(mOwner,
                        Validator.ResultType.ERROR,
                        address,
                        getMessage("MQAddressValidation.MISSING_SSLPEERNAME")));
            }
        }
        
        // sslPeerName without cipherSuite is meaningless.
        if (sslPeerName != null && cipherSuite == null) {
            mResults.add(new Validator.ResultItem(mOwner,
                    Validator.ResultType.ERROR,
                    address,
                    getMessage("MQAddressValidation.SSLPEERNAME_NEEDS_CIPHERSUITE")));
        }
        
        // Both sslPeerName and cipherSuite unspecified means SSL connection not needed
        if (cipherSuite == null && sslPeerName == null) {
            return;
        }
        
        // ssl connections must be client (transport) connections, not bindings.
        String hostname = address.getHostName();
        String channel = address.getChannelName();
        if (cipherSuite != null) {
            if (hostname == null || hostname.trim().equals("")) {
                mResults.add(new Validator.ResultItem(mOwner,
                        Validator.ResultType.ERROR,
                        address,
                        getMessage("MQAddressValidation.SSL_NEEDS_MISSING_HOST")));
            }
            if (channel == null || channel.trim().equals("")) {
                mResults.add(new Validator.ResultItem(mOwner,
                        Validator.ResultType.ERROR,
                        address,
                        getMessage("MQAddressValidation.SSL_NEEDS_MISSING_CHANNEL")));
            }
        }
    }

    /**
     * Determines if a message definition does have the named part.
     * @param wsdlMessage The message definition to search
     * @param partName    The name of the message part to look for
     * @return true if the part exists in the message, false otherwise.
     */
    private boolean hasPart(Message wsdlMessage, String partName) {
        assert wsdlMessage != null;
        assert partName != null;
        
        return getPart(wsdlMessage, partName) != null;
    }
    
    private Part getPart(Message wsdlMessage, String partName) {
        assert wsdlMessage != null;
        assert partName != null;
        
        Part thePart = null;
        for (Part part : wsdlMessage.getParts()) {
            if (part.getName().equals(partName)) {
                thePart = part;
                break;
            }
        }
        return thePart;
    }
    
    private boolean isEmpty(String value) {
        return value == null || value.trim().equals("");
    }

    /**
     * Determines if the binding possesses an MQ Binding extensibility element.
     * @param binding The binding to examine
     * @return true if the binding contains an MQ Binding.
     */
    private boolean hasMqBinding(Binding binding) {
        assert binding != null;
        return binding.getExtensibilityElements(MQBinding.class).size() != 0;
    }

    /**
     * Retrieves resources from internal ResourceBundle and performs
     * parameterized formatting. Formatting is delegated to
     * {@link java.text.MessageFormat#format(String, Object[])}.
     * 
     * @param key  Resource key
     * @param args Variable number of arguments to apply to formatting
     * @return Formatted string
     */
    private String getMessage(String key, Object ... args) {
        assert key != null;
        key = (key.length() <= 256 ? key : key.substring(0, 256));
        String msg = mMessages.getString(key);
        return java.text.MessageFormat.format(msg, args);
    }
}
