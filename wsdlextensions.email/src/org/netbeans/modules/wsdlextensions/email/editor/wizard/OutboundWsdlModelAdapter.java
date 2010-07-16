package org.netbeans.modules.wsdlextensions.email.editor.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


import org.netbeans.modules.wsdlextensions.email.EmailConstants;
import org.netbeans.modules.wsdlextensions.email.smtp.SMTPAddress;
import org.netbeans.modules.wsdlextensions.email.smtp.SMTPBinding;
import org.netbeans.modules.wsdlextensions.email.smtp.SMTPInput;
import org.netbeans.modules.wsdlextensions.email.smtp.SMTPComponent;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.soa.wsdl.bindingsupport.ui.util.BindingComponentUtils;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

import org.openide.util.NbBundle;

/**
 * Adapts information from a WSDLModel to the representation that a
 * BindingsConfigurationEditorForm understands.
 *
 * @see {@link OutboundBindingConfigurationEditorForm.Model}
 */
public class OutboundWsdlModelAdapter implements OutboundBindingConfigurationEditorForm.Model {

    private WSDLModel model;
    private SMTPAddress focusedSMTPAddress;
    private SMTPInput focusedSMTPInput;
    private boolean hasFocus;
    private String location;
    private String emailServer;
    private String userName;
    private String password;
    private String port;
    private boolean useSSL;
    private String messageType;
    private String charset;
    private String encodingStyle;
    private String use;
    private String sendOption;
    private boolean embedImagesInHtml;
    private boolean handleNMAttachments;
    private String xsdElementOrType;
    private GlobalElement elementType;
    private GlobalType partType;

    /**
     * Create a WsdlConfigModelAdapter using the specified model.
     *
     * @param model Data model
     */
    public OutboundWsdlModelAdapter(WSDLModel model) {
        if (model == null) {
            throw new NullPointerException(NbBundle.getMessage(OutboundWsdlModelAdapter.class,
                    "OutboundWsdlModelAdapter.WsdlModelIsNull"));
        }
        this.model = model;
    }

    /**
     * Select a SMTP binding set (port->binding->operation) in the underlying
     * model that will be the subject of reads and writes thru this adapter.
     *
     * @param component A email extensibility element that exists in the model.
     */
    public void focus(WSDLComponent component) {
        synchronized (this) {
            hasFocus = component != null && _focus(component);
        }
    }

    public synchronized String getLocation() {
        return Utils.safeString(location);
    }

    public synchronized String getEmailServer() {
        return Utils.safeString(emailServer);
    }

    public synchronized String getPort() {
        return Utils.safeString(port);
    }

    public synchronized String getUserName() {
        return Utils.safeString(userName);
    }

    public synchronized String getPassword() {
        return Utils.safeString(password);
    }

    public synchronized boolean getUseSSL() {
        return useSSL;
    }

    public synchronized String getMessageType() {
        return Utils.safeString(messageType);
    }

    public synchronized String getCharset() {
        return Utils.safeString(charset);
    }

    public synchronized String getEncodingStyle() {
        return Utils.safeString(encodingStyle);
    }

    public synchronized String getUse() {
        return Utils.safeString(use);
    }

    public synchronized String getSendOption() {
        return Utils.safeString(sendOption);
    }

    public synchronized boolean getEmbedImagesInHtml() {
        return embedImagesInHtml;
    }

    public synchronized boolean getHandleNMAttachments() {
        return handleNMAttachments;
    }

    public synchronized String getXsdElementOrType() {
        return xsdElementOrType;
    }

    public synchronized GlobalType getPartType() {
        return partType;
    }

    public synchronized GlobalElement getElementType() {
        return elementType;
    }

    public void setXsdElementOrType(String value) {
        value = Utils.safeString(value);
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        NbBundle.getMessage(OutboundWsdlModelAdapter.class,
                        "OutboundWsdlModelAdapter.BindingModifyUnspecified"));
            }
            xsdElementOrType = value;
            updateModel();
            updatePart();
        }
    }

    public void setLocation(String value) {
        value = Utils.safeString(value);
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        NbBundle.getMessage(OutboundWsdlModelAdapter.class,
                        "OutboundWsdlModelAdapter.BindingModifyUnspecified"));
            }
            location = value;
            updateModel();
        }
    }

    public void setEmailServer(String value) {
        value = Utils.safeString(value);
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        NbBundle.getMessage(OutboundWsdlModelAdapter.class,
                        "OutboundWsdlModelAdapter.BindingModifyUnspecified"));
            }
            emailServer = value;
            updateModel();
        }
    }

    public void setPort(String value) {
        value = Utils.safeString(value);
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        NbBundle.getMessage(OutboundWsdlModelAdapter.class,
                        "OutboundWsdlModelAdapter.BindingModifyUnspecified"));
            }
            port = value;
            updateModel();
        }
    }

    public void setUserName(String value) {
        value = Utils.safeString(value);
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        NbBundle.getMessage(OutboundWsdlModelAdapter.class,
                        "OutboundWsdlModelAdapter.BindingModifyUnspecified"));
            }
            userName = value;
            updateModel();
        }
    }

    public void setPassword(String value) {
        value = Utils.safeString(value);
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        NbBundle.getMessage(OutboundWsdlModelAdapter.class,
                        "OutboundWsdlModelAdapter.BindingModifyUnspecified"));
            }
            password = value;
            updateModel();
        }
    }

    public void setUseSSL(boolean value) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        NbBundle.getMessage(OutboundWsdlModelAdapter.class,
                        "OutboundWsdlModelAdapter.BindingModifyUnspecified"));
            }
            useSSL = value;
            updateModel();
        }
    }

    public void setMessageType(String value) {
        value = Utils.safeString(value);
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        NbBundle.getMessage(OutboundWsdlModelAdapter.class,
                        "OutboundWsdlModelAdapter.BindingModifyUnspecified"));
            }
            messageType = value;
            updateModel();
        }
    }

    public void setCharset(String value) {
        value = Utils.safeString(value);
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        NbBundle.getMessage(OutboundWsdlModelAdapter.class,
                        "OutboundWsdlModelAdapter.BindingModifyUnspecified"));
            }
            charset = value;
            updateModel();
        }
    }

    public void setEncodingStyle(String value) {
        value = Utils.safeString(value);
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        NbBundle.getMessage(OutboundWsdlModelAdapter.class,
                        "OutboundWsdlModelAdapter.BindingModifyUnspecified"));
            }
            encodingStyle = value;
            updateModel();
        }
    }

    public void setSendOption(String value) {
        value = Utils.safeString(value);
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        NbBundle.getMessage(OutboundWsdlModelAdapter.class,
                        "OutboundWsdlModelAdapter.BindingModifyUnspecified"));
            }
            sendOption = value;
            updateModel();
        }
    }

    public void setEmbedImagesInHtml(boolean value) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        NbBundle.getMessage(OutboundWsdlModelAdapter.class,
                        "OutboundWsdlModelAdapter.BindingModifyUnspecified"));
            }
            embedImagesInHtml = value;
            updateModel();
        }
    }

    public void setHandleNMAttachments(boolean value) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        NbBundle.getMessage(OutboundWsdlModelAdapter.class,
                        "OutboundWsdlModelAdapter.BindingModifyUnspecified"));
            }
            handleNMAttachments = value;
            updateModel();
        }
    }

    public void setUse(String value) {
        value = Utils.safeString(value);
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        NbBundle.getMessage(OutboundWsdlModelAdapter.class,
                        "OutboundWsdlModelAdapter.BindingModifyUnspecified"));
            }
            use = value;
            updateModel();
        }
    }

    public void setPartType(GlobalType value) {
        synchronized (this) {
            partType = value;
        }
    }

    public void setElementType(GlobalElement value) {
        synchronized (this) {
            elementType = value;
        }
    }

    /** Update the data model with the data cached in object fields. */
    private void updateModel() {
        synchronized (this) {
            if (hasFocus) {
                _updateAddress();
                _updateInput();
            }
        }
    }

    private void updatePart() {
        assert model != null;
        SMTPBinding smtpBinding = null;
        BindingOperation operation = null;

        Definitions definitions = model.getDefinitions();
        for (Binding binding : definitions.getBindings()) {

            List<SMTPBinding> bindings =
                    binding.getExtensibilityElements(SMTPBinding.class);
            if (!bindings.isEmpty()) {
                smtpBinding = bindings.get(0);
                break;
            }
        }
        Binding binding = (Binding) smtpBinding.getParent();
        if (binding.getBindingOperations().size() > 0) {
            operation = binding.getBindingOperations().iterator().next();
        }
        SMTPInput smtpInput = this.findSmtpInput(smtpBinding);
        String messagePart = smtpInput.getMessage();
        if (messagePart != null) {
            Collection<Part> parts = findParts(binding, operation.getName());
            if ((parts != null) && (parts.size() > 0)) {
                for (Part part : parts) {
                    if (part.getName().equals(messagePart)) {
                        //Part can come from the abstract wsdl
                        //This is not the right solution, but part can be changed only when in wizard.
                        // Even though there is no change, it tries to set the type.
                        //TODO: Fix this properly.
                        if (part.getModel().isIntransaction()) {
                            updatePartType(part, getMessageType(), getPartType(), getElementType());
                        }
                    }
                }
            }
        }

    }

    private Collection<Part> findParts(Binding binding, String opName) {
        Collection<Part> inputParts = new ArrayList<Part>();
        if (binding != null) {
            NamedComponentReference<PortType> pType = binding.getType();
            PortType type = pType.get();
            Collection ops = type.getOperations();
            Iterator iter = ops.iterator();
            while (iter.hasNext()) {
                Operation op = (Operation) iter.next();
                if ((op != null) && (op.getName().equals(opName))) {
                    Input input = op.getInput();
                    if (input != null) {
                        NamedComponentReference<Message> messageIn = input.getMessage();
                        if ((messageIn != null) && (messageIn.get() != null)) {
                            Message msgIn = messageIn.get();
                            Collection parts = msgIn.getParts();
                            Iterator partIter = parts.iterator();
                            while (partIter.hasNext()) {
                                Part part = (Part) partIter.next();
                                inputParts.add(part);
                            }
                        }
                    }
                }
            }
        }
        return inputParts;
    }

    private void updatePartType(Part part, String messageType, GlobalType gT,
            GlobalElement gE) {
        SchemaComponent schemaComponent = null;
        if (messageType.equalsIgnoreCase(EmailConstants.XML)) {
            if (gT != null) {
                part.setType(part.createSchemaReference(
                        gT, GlobalType.class));
                part.setElement(null);
                schemaComponent = gT;
            } else if (gE != null) {
                part.setElement(part.createSchemaReference(
                        gE, GlobalElement.class));
                part.setType(null);
                schemaComponent = gE;
            }
        } else if (messageType.equalsIgnoreCase(EmailConstants.TEXT)) {
            part.setType(part.createSchemaReference(
                    getPrimitiveType("string"),
                    GlobalType.class));
            part.setElement(null);
        } else if (messageType.equalsIgnoreCase(EmailConstants.ENCODED_DATA)) {
            if (gT != null) {
                part.setType(part.createSchemaReference(
                        gT, GlobalType.class));
                part.setElement(null);
                schemaComponent = gT;
            } else if (gE != null) {
                part.setType(null);
                part.setElement(part.createSchemaReference(
                        gE, GlobalElement.class));
                schemaComponent = gE;
            }
        } else if (messageType.equalsIgnoreCase(EmailConstants.BINARY)) {
            part.setType(part.createSchemaReference(
                    getPrimitiveType("base64Binary"),
                    GlobalType.class));
            part.setElement(null);
        }
        BindingComponentUtils.addSchemaImport(schemaComponent, part.getModel());
    }

    public static GlobalSimpleType getPrimitiveType(String typeName) {
        SchemaModel primitiveModel = SchemaModelFactory.getDefault().getPrimitiveTypesModel();
        Collection<GlobalSimpleType> primitives = primitiveModel.getSchema().getSimpleTypes();
        for (GlobalSimpleType ptype : primitives) {
            if (ptype.getName().equals(typeName)) {
                return ptype;
            }
        }
        return null;
    }

    private void _updateAddress() {
        assert focusedSMTPAddress != null;
        if (Utils.hasValue(location)) {
            focusedSMTPAddress.setLocation(location);
        }
        focusedSMTPAddress.setEmailServer(emailServer);
        if (Utils.hasValue(userName)) {
            focusedSMTPAddress.setUserName(userName);
        }
        if (Utils.hasValue(password)) {
            focusedSMTPAddress.setPassword(password);
        }
        if (Utils.hasValue(port)) {
            focusedSMTPAddress.setPort(port);
        }
        focusedSMTPAddress.setUseSSL(useSSL);
    }

    // Unsynchronized and unchecked!  Use internally, use defensively.
    private void _updateInput() {
        assert focusedSMTPInput != null;
        if (Utils.hasValue(use)) {
            focusedSMTPInput.setUse(use);
        }

        if (EmailConstants.BINARY.equals(messageType)) {
            sendOption = null;
            charset = null;
        }
        
        if (!EmailConstants.ENCODED.equals(messageType)) {
            encodingStyle = null;
        }

        if (Utils.hasValue(charset)) {
            focusedSMTPInput.setCharset(charset);
        }

        if (Utils.hasValue(encodingStyle)) {
            focusedSMTPInput.setEncodingStyle(encodingStyle);
        }

        if (Utils.hasValue(sendOption)) {
            focusedSMTPInput.setSendOption(sendOption);
        }

        if (EmailConstants.HTML_ONLY.equals(sendOption) || EmailConstants.BOTH_TEXT_AND_HTML.equals(sendOption)) {
            if (embedImagesInHtml) {
                focusedSMTPInput.setEmbedImagesInHtml(Boolean.TRUE.toString());
            }
        } else {
            focusedSMTPInput.setEmbedImagesInHtml(null);
        }

        if (handleNMAttachments) {
            focusedSMTPInput.setHandleNMAttachments(Boolean.TRUE.toString());
        } else {
            focusedSMTPInput.setHandleNMAttachments(null);
        }

    }

    private boolean _focus(WSDLComponent component) {
        assert component != null;
        boolean hasFocus;

        // SMTP extensibility element hierarchy:
        //
        // wsdl:binding
        //     smtp:binding  <---
        //     wsdl:operation
        //         smtp:operation <---
        //         wsdl:input
        //             smtp:input <---
        // wsdl:service
        //     wsdl:port
        //         smtp:address <---
        //
        // 1. Given a smtp:address, resolve associated smtp:binding, and parse.
        // 2. Given a smtp:binding, resolve associated port, and parse.
        // 3. Given any other SMTP extensibility element, resolve ancestor
        //    operation, binding, associated port, and parse.
        if (SMTPAddress.class.isAssignableFrom(component.getClass())) {
            SMTPBinding binding = findSmtpBinding((SMTPAddress) component);
            SMTPInput input = findSmtpInput(binding);
            hasFocus = _parse((SMTPAddress) component) && _parse(input);
        } else if (SMTPBinding.class.isAssignableFrom(component.getClass())) {
            SMTPInput input = findSmtpInput((SMTPBinding) component);
            SMTPAddress address = findSmtpAddress((SMTPBinding) component);
            hasFocus = _parse(address) && _parse(input);
        } else if (SMTPInput.class.isAssignableFrom(component.getClass())) {
            SMTPBinding binding = findSmtpBinding((SMTPInput) component);
            SMTPAddress address = findSmtpAddress(binding);
            hasFocus = _parse(address) && _parse((SMTPInput) component);
        } else {
            // Non-SMTP extensibility elements.
            hasFocus = false;
            if (Port.class.isAssignableFrom(component.getClass())) {
                List<SMTPAddress> addresses = component.getExtensibilityElements(SMTPAddress.class);
                if (!addresses.isEmpty()) {
                    SMTPAddress address = addresses.get(0);
                    SMTPBinding binding = findSmtpBinding((SMTPAddress) address);
                    SMTPInput input = findSmtpInput(binding);
                    hasFocus = _parse(address) && _parse(input);
                }
            }
        }
        return hasFocus;
    }

    private boolean _parse(SMTPAddress address) {
        assert address != null;
        String location = Utils.safeString(address.getLocation());
        String emailServer = Utils.safeString(address.getEmailServer());
        String port = Utils.safeString(address.getPort());
        String userName = Utils.safeString(address.getUserName());
        String password = Utils.safeString(address.getPassword());
        boolean useSSL = address.getUseSSL();
        synchronized (this) {
            this.location = location;
            this.emailServer = emailServer;
            this.port = port;
            this.userName = userName;
            this.password = password;
            this.useSSL = useSSL;
        }
        focusedSMTPAddress = address;
        return true;
    }

    private boolean _parse(SMTPInput input) {
        assert input != null;
        String charset = Utils.safeString(input.getCharset());
        String encodingStyle = Utils.safeString(input.getEncodingStyle());
        String use = Utils.safeString(input.getUse());
        String sendOption = Utils.safeString(input.getSendOption());
        boolean embedImgHtml = input.isEmbedImagesInHtml();
        boolean handleNMAtt = input.isHandleNMAttachments();
        synchronized (this) {
            this.charset = charset;
            this.encodingStyle = encodingStyle;
            this.use = use;
            this.sendOption = sendOption;
            this.embedImagesInHtml = embedImgHtml;
            this.handleNMAttachments = handleNMAtt;
        }
        focusedSMTPInput = input;
        return true;
    }

    /**
     * Given an SMTP Binding element, find an SMTP Address element whose port
     * matches the binding.
     */
    private SMTPAddress findSmtpAddress(SMTPBinding smtpBinding) {
        assert smtpBinding != null;
        SMTPAddress smtpAddress = null;
        Definitions definitions = model.getDefinitions();
        if (definitions != null) {
            WSDLComponent parent = smtpBinding.getParent();
            if (Binding.class.isAssignableFrom(parent.getClass())) {
                Binding targetBinding = (Binding) smtpBinding.getParent();
                search:
                for (Service service : definitions.getServices()) {
                    for (Port port : service.getPorts()) {
                        if (isReferencing(port.getBinding())) {
                            Binding binding = port.getBinding().get();
                            if (binding.equals(targetBinding)) {
                                List<SMTPAddress> addresses =
                                        port.getExtensibilityElements(SMTPAddress.class);
                                if (!addresses.isEmpty()) {
                                    smtpAddress = addresses.get(0);
                                    break search;
                                }
                            }
                        }
                    }
                }
            }
        }
        return smtpAddress;
    }

    /**
     * Given an SMTP Address element, find the SMTP Binding associated with its
     * port.
     */
    private SMTPBinding findSmtpBinding(SMTPAddress smtpAddress) {
        assert smtpAddress != null;
        SMTPBinding smtpBinding = null;
        WSDLComponent parent = smtpAddress.getParent();
        if (Port.class.isAssignableFrom(parent.getClass())) {
            Port port = (Port) parent;
            if (isReferencing(port.getBinding())) {
                Binding binding = port.getBinding().get();
                List<SMTPBinding> bindings = binding.getExtensibilityElements(
                        SMTPBinding.class);
                if (!bindings.isEmpty()) {
                    smtpBinding = bindings.get(0);
                }
            }
        }
        return smtpBinding;
    }

    /**
     * Given an SMTP Binding element, find the first SMTP Input element
     * belonging to the same binding.
     */
    private SMTPInput findSmtpInput(SMTPBinding smtpBinding) {
        assert smtpBinding != null;
        SMTPInput smtpInput = null;
        WSDLComponent parent = smtpBinding.getParent();
        if (Binding.class.isAssignableFrom(parent.getClass())) {
            Binding binding = (Binding) parent;
            if (binding.getBindingOperations().size() > 0) {
                BindingOperation operation = binding.getBindingOperations().iterator().next();
                List<SMTPInput> smtpInputs = operation.getBindingInput().getExtensibilityElements(SMTPInput.class);
                if (!smtpInputs.isEmpty()) {
                    smtpInput = smtpInputs.get(0);
                }
            }
        }
        return smtpInput;
    }

    /**
     * Given an SMTP extensibility element, find the SMTP Binding associated with
     * it.
     */
    private SMTPBinding findSmtpBinding(SMTPComponent smtpComponent) {
        assert smtpComponent != null;
        SMTPBinding smtpBinding = null;
        WSDLComponent element = smtpComponent;
        if (SMTPAddress.class.isAssignableFrom(smtpComponent.getClass())) {
            smtpBinding = findSmtpBinding((SMTPAddress) smtpComponent);
        } else {
            // Traverse up the model until we find the SMTP Binding.
            while (element != null && !Binding.class.isAssignableFrom(element.getClass())) {
                element = element.getParent();
            }
            if (element != null && Binding.class.isAssignableFrom(element.getClass())) {
                List<SMTPBinding> smtpBindings = element.getExtensibilityElements(SMTPBinding.class);
                if (!smtpBindings.isEmpty()) {
                    smtpBinding = smtpBindings.get(0);
                }
            }
        }
        return smtpBinding;
    }

    /** Decides if a reference isn't broken. */
    private boolean isReferencing(NamedComponentReference obj) {
        return obj != null && obj.get() != null;
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

    abstract class Option<T> {

        public abstract String name();

        public abstract boolean accept(Object value);

        public abstract T value();
    }

    /** An Option that is either true or false. */
    class BooleanOption extends Option<Boolean> {

        private boolean value;
        private final String name;

        BooleanOption(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

        public synchronized boolean accept(Object value) {
            if (!(value instanceof String)) {
                this.value = Boolean.valueOf(String.valueOf(value));
            } else {
                this.value = value.toString().equalsIgnoreCase(name());
            }
            return this.value;
        }

        public synchronized Boolean value() {
            return value;
        }
    }

    /** An Option that is one thing, or another thing. */
    class BinaryChoiceOption extends BooleanOption {

        private final String alternateValue;

        BinaryChoiceOption(String name, String alternateValue) {
            super(name);
            this.alternateValue = alternateValue;
        }

        public String choice() {
            return (value() ? name() : alternateValue);
        }
    }
}
