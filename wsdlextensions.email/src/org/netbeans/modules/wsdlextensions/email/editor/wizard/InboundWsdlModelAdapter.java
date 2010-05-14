package org.netbeans.modules.wsdlextensions.email.editor.wizard;

import java.util.List;

import org.netbeans.modules.wsdlextensions.email.pop3.POP3Address;
import org.netbeans.modules.wsdlextensions.email.pop3.POP3Binding;
import org.netbeans.modules.wsdlextensions.email.pop3.POP3Input;
import org.netbeans.modules.wsdlextensions.email.pop3.POP3Component;
import org.netbeans.modules.wsdlextensions.email.imap.IMAPAddress;
import org.netbeans.modules.wsdlextensions.email.imap.IMAPBinding;
import org.netbeans.modules.wsdlextensions.email.imap.IMAPInput;
import org.netbeans.modules.wsdlextensions.email.imap.IMAPComponent;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

import org.openide.util.NbBundle;
/**
 * Adapts information from a WSDLModel to the representation that a
 * BindingsConfigurationEditorForm understands.
 *
 * @see {@link InboundBindingConfigurationEditorForm.Model}
 */
public class InboundWsdlModelAdapter implements InboundBindingConfigurationEditorForm.Model {

	    private WSDLModel model;
	    private IMAPAddress focusedIMAPAddress;
	    private IMAPInput focusedIMAPInput;
	    private POP3Address focusedPOP3Address;
	    private POP3Input focusedPOP3Input;
	    private boolean hasFocus;
	    private String emailServer;
	    private String userName;
	    private String password;
	    private String port;
	    private boolean useSSL;
	    private String mailFolder;
	    private String maxMessageCount;
	    private String messageAckMode;
	    private String messageAckOperation;
	    private String pollingInterval;
	    private String saveAttachmentsToDir;
	    //private boolean handleNMAttachments;
        private String templateType = "";

	    /**
	     * Create a WsdlConfigModelAdapter using the specified model.
	     *
	     * @param model Data model
	     */
	    public InboundWsdlModelAdapter(WSDLModel model, String templateType) {
	        if (model == null) {
	            throw new NullPointerException(NbBundle.getMessage(InboundWsdlModelAdapter.class,
	                "InboundWsdlModelAdapter.WsdlModelIsNull"));
	        }
	        this.model = model;
            this.templateType = templateType;
	    }

	    /**
	     * Select a POP3/IMAP binding set (port->binding->operation) in the underlying
	     * model that will be the subject of reads and writes thru this adapter.
	     *
	     * @param component A email extensibility element that exists in the model.
	     */
	    public void focus(WSDLComponent component) {
	        synchronized (this) {
	            hasFocus = component != null && _focus(component);
	        }
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

	    public synchronized String getMailFolder() {
	        return Utils.safeString(mailFolder);
	    }

	    public synchronized String getMaxMessageCount() {
	        return Utils.safeString(maxMessageCount);
	    }

	    public synchronized String getMessageAckMode() {
	        return Utils.safeString(messageAckMode);
	    }

	    public synchronized String getMessageAckOperation() {
	        return Utils.safeString(messageAckOperation);
	    }
	    
	    public synchronized String getPollingInterval() {
	        return Utils.safeString(pollingInterval);
	    }

	    public synchronized String getSaveAttachmentsToDir() {
	        return Utils.safeString(saveAttachmentsToDir);
	    }

	  /*  public synchronized boolean getHandleNMAttachments() {
	        return handleNMAttachments;
        }*/
   
	    public void setEmailServer(String value) {
	        value = Utils.safeString(value);
	        synchronized (this) {
	            if (!hasFocus) {
	                throw new IllegalStateException(
	                        NbBundle.getMessage(InboundWsdlModelAdapter.class,
	                "InboundWsdlModelAdapter.BindingModifyUnspecified"));
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
	                        NbBundle.getMessage(InboundWsdlModelAdapter.class,
	                "InboundWsdlModelAdapter.BindingModifyUnspecified"));
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
	                        NbBundle.getMessage(InboundWsdlModelAdapter.class,
	                "InboundWsdlModelAdapter.BindingModifyUnspecified"));
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
	                        NbBundle.getMessage(InboundWsdlModelAdapter.class,
	                "InboundWsdlModelAdapter.BindingModifyUnspecified"));
	            }
	            password = value;
	            updateModel();
	        }
	    }

	    public void setUseSSL(boolean value) {
	        synchronized (this) {
	            if (!hasFocus) {
	                throw new IllegalStateException(
	                        NbBundle.getMessage(InboundWsdlModelAdapter.class,
	                "InboundWsdlModelAdapter.BindingModifyUnspecified"));
	            }
	            useSSL = value;
	            updateModel();
	        }
	    }

	    public void setMailFolder(String value) {
	        value = Utils.safeString(value);
	        synchronized (this) {
	            if (!hasFocus) {
	                throw new IllegalStateException(
	                       NbBundle.getMessage(InboundWsdlModelAdapter.class,
	                "InboundWsdlModelAdapter.BindingModifyUnspecified"));
	            }
	            mailFolder = value;
	            updateModel();
	        }
	    }

	    public void setMaxMessageCount(String value) {
	        value = Utils.safeString(value);
	        synchronized (this) {
	            if (!hasFocus) {
	                throw new IllegalStateException(
	                        NbBundle.getMessage(InboundWsdlModelAdapter.class,
	                "InboundWsdlModelAdapter.BindingModifyUnspecified"));
	            }
	            maxMessageCount = value;
	            updateModel();
	        }
	    }

	    public void setMessageAckMode(String value) {
	        value = Utils.safeString(value);
	        synchronized (this) {
	            if (!hasFocus) {
	                throw new IllegalStateException(
	                        NbBundle.getMessage(InboundWsdlModelAdapter.class,
	                "InboundWsdlModelAdapter.BindingModifyUnspecified"));
	            }
	            messageAckMode = value;
	            updateModel();
	        }
	    }

	    public void setMessageAckOperation(String value) {
			value = Utils.safeString(value);
	        synchronized (this) {
	            if (!hasFocus) {
	                throw new IllegalStateException(
	                       NbBundle.getMessage(InboundWsdlModelAdapter.class,
	                "InboundWsdlModelAdapter.BindingModifyUnspecified"));
	            }
	            messageAckOperation = value;
	            updateModel();
	        }
	    }

	    public void setPollingInterval(String value) {
			value = Utils.safeString(value);
	        synchronized (this) {
	            if (!hasFocus) {
	                throw new IllegalStateException(
	                       NbBundle.getMessage(InboundWsdlModelAdapter.class,
	                "InboundWsdlModelAdapter.BindingModifyUnspecified"));
	            }
	            pollingInterval = value;
	            updateModel();
	        }
	    }

	    public void setSaveAttachmentsToDir(String value) {
			value = Utils.safeString(value);
	        synchronized (this) {
	            if (!hasFocus) {
	                throw new IllegalStateException(
	                       NbBundle.getMessage(InboundWsdlModelAdapter.class,
	                "InboundWsdlModelAdapter.BindingModifyUnspecified"));
	            }
	            saveAttachmentsToDir = value;
	            updateModel();
	        }
	    }

	   /* public void setHandleNMAttachments(boolean value) {
	        synchronized (this) {
	            if (!hasFocus) {
	                throw new IllegalStateException(
	                        NbBundle.getMessage(InboundWsdlModelAdapter.class,
	                "InboundWsdlModelAdapter.BindingModifyUnspecified"));
	            }
	            handleNMAttachments = value;
	            updateModel();
	        }
	    }*/

	    /** Update the data model with the data cached in object fields. */
	    private void updateModel() {
	        synchronized (this) {
	            if (hasFocus) {
                    if(this.templateType.equalsIgnoreCase("IMAP")) {
                        _updateIMAPAddress();
                        _updateIMAPInput();
                    } else {
                        _updatePOP3Address();
                        _updatePOP3Input();
                    }
                }
            }
        }

	    private void _updateIMAPAddress() {
	        assert focusedIMAPAddress != null;
	        focusedIMAPAddress.setEmailServer(emailServer);
			focusedIMAPAddress.setUserName(userName);
			focusedIMAPAddress.setPassword(password);
			focusedIMAPAddress.setPort(port);
			focusedIMAPAddress.setUseSSL(useSSL);
            if(Utils.hasValue(mailFolder)){
                focusedIMAPAddress.setMailFolder(mailFolder);
            }
            if(Utils.hasValue(maxMessageCount)){
                focusedIMAPAddress.setMaxMessageCount(maxMessageCount);
            }
            if(Utils.hasValue(messageAckMode)){
                focusedIMAPAddress.setMessageAckMode(messageAckMode);
            }
            if(Utils.hasValue(messageAckOperation)){
                focusedIMAPAddress.setMessageAckOperation(messageAckOperation);
            }
            if(Utils.hasValue(pollingInterval)){
                focusedIMAPAddress.setPollingInterval(pollingInterval);
            }
	    }

	    // Unsynchronized and unchecked!  Use internally, use defensively.
	    private void _updateIMAPInput() {
	        assert focusedIMAPInput != null;
            if(Utils.hasValue(saveAttachmentsToDir)){
                focusedIMAPInput.setSaveAttachmentsToDir(saveAttachmentsToDir);
            }
	        //focusedIMAPInput.setHandleNMAttachments(handleNMAttachments);
	    }

	    private void _updatePOP3Address() {
	        assert focusedPOP3Address != null;
	        focusedPOP3Address.setEmailServer(emailServer);
			focusedPOP3Address.setUserName(userName);
			focusedPOP3Address.setPassword(password);
			focusedPOP3Address.setPort(port);
			focusedPOP3Address.setUseSSL(useSSL);
            if(Utils.hasValue(maxMessageCount)){
                focusedPOP3Address.setMaxMessageCount(maxMessageCount);
            }
            if(Utils.hasValue(messageAckMode)){
                focusedPOP3Address.setMessageAckMode(messageAckMode);
            }
            if(Utils.hasValue(messageAckOperation)){
                focusedPOP3Address.setMessageAckOperation(messageAckOperation);
            }
            if(Utils.hasValue(pollingInterval)){
                focusedPOP3Address.setPollingInterval(pollingInterval);
            }
	    }

	    // Unsynchronized and unchecked!  Use internally, use defensively.
	    private void _updatePOP3Input() {
	        assert focusedPOP3Input != null;
            if(Utils.hasValue(saveAttachmentsToDir)){
                focusedPOP3Input.setSaveAttachmentsToDir(saveAttachmentsToDir);
            }
	        //focusedPOP3Input.setHandleNMAttachments(handleNMAttachments);
	    }

	    private boolean _focus(WSDLComponent component) {
	        assert component != null;
	        boolean hasFocus;

	        // IMAP/POP3 extensibility element hierarchy:
	        //
	        // wsdl:binding
	        //     imap/pop3:binding  <---
	        //     wsdl:operation
	        //         imap/pop3:operation <---
	        //         wsdl:input
	        //             imap/pop3:input <---
	        // wsdl:service
	        //     wsdl:port
	        //         imap/pop3:address <---
	        //
	        // 1. Given a imap/pop3:address, resolve associated smtp:binding, and parse.
	        // 2. Given a imap/pop3:binding, resolve associated port, and parse.
	        // 3. Given any other IMAP/POP3 extensibility element, resolve ancestor
	        //    operation, binding, associated port, and parse.
            if(this.templateType.equalsIgnoreCase("IMAP")){
                if (IMAPAddress.class.isAssignableFrom(component.getClass())) {
                    IMAPBinding binding = findImapBinding((IMAPAddress) component);
                    IMAPInput input = findImapInput(binding);
                    hasFocus = _parse((IMAPAddress) component) && _parse(input);
                } else if (IMAPBinding.class.isAssignableFrom(component.getClass())) {
                    IMAPInput input = findImapInput((IMAPBinding) component);
                    IMAPAddress address = findImapAddress((IMAPBinding) component);
                    hasFocus = _parse(address) && _parse(input);
                } else if (IMAPInput.class.isAssignableFrom(component.getClass())) {
                    IMAPBinding binding = findImapBinding((IMAPInput) component);
                    IMAPAddress address = findImapAddress(binding);
                    hasFocus = _parse(address) && _parse((IMAPInput) component);
                } else {
                    // Non-IMAP/POP3 extensibility elements.
                    hasFocus = false;
                    if (Port.class.isAssignableFrom(component.getClass())) {
                        List<IMAPAddress> addresses = component.getExtensibilityElements(IMAPAddress.class);
                        if (!addresses.isEmpty()) {
                            IMAPAddress address = addresses.get(0);
                            IMAPBinding binding = findImapBinding((IMAPAddress) address);
                            IMAPInput input = findImapInput(binding);
                            hasFocus = _parse(address) && _parse(input);
                        }
                    }
                }
            } else {
                if (POP3Address.class.isAssignableFrom(component.getClass())) {
                    POP3Binding binding = findPop3Binding((POP3Address) component);
                    POP3Input input = findPop3Input(binding);
                    hasFocus = _parse((POP3Address) component) && _parse(input);
                } else if (POP3Binding.class.isAssignableFrom(component.getClass())) {
                    POP3Input input = findPop3Input((POP3Binding) component);
                    POP3Address address = findPop3Address((POP3Binding) component);
                    hasFocus = _parse(address) && _parse(input);
                } else if (POP3Input.class.isAssignableFrom(component.getClass())) {
                    POP3Binding binding = findPop3Binding((POP3Input) component);
                    POP3Address address = findPop3Address(binding);
                    hasFocus = _parse(address) && _parse((POP3Input) component);
                } else {
                    // Non-POP3 extensibility elements.
                    hasFocus = false;
                    if (Port.class.isAssignableFrom(component.getClass())) {
                        List<POP3Address> addresses = component.getExtensibilityElements(POP3Address.class);
                        if (!addresses.isEmpty()) {
                            POP3Address address = addresses.get(0);
                            POP3Binding binding = findPop3Binding((POP3Address) address);
                            POP3Input input = findPop3Input(binding);
                            hasFocus = _parse(address) && _parse(input);
                        }
                    }
                }
            }
	        return hasFocus;
	    }

	    private boolean _parse(IMAPAddress address) {
	        assert address != null;
	        String emailServer = Utils.safeString(address.getEmailServer());
			String port = Utils.safeString(address.getPort());
			String userName = Utils.safeString(address.getUserName());
			String password = Utils.safeString(address.getPassword());
			boolean useSSL = address.getUseSSL();
            String mailFolder = Utils.safeString(address.getMailFolder());
            String maxMsgCount = Utils.safeString(address.getMaxMessageCount());
            String msgAckMode = Utils.safeString(address.getMessageAckMode());
            String msgAckOperation = Utils.safeString(address.getMessageAckOperation());
            String pollingInterval = Utils.safeString(address.getPollingInterval());
	        synchronized (this) {
				this.emailServer = emailServer;
				this.port = port;
				this.userName = userName;
				this.password = password;
				this.useSSL = useSSL;
                this.mailFolder = mailFolder;
                this.maxMessageCount = maxMsgCount;
                this.messageAckMode = msgAckMode;
                this.messageAckOperation = msgAckOperation;
                this.pollingInterval = pollingInterval;
	        }
	        focusedIMAPAddress = address;
	        return true;
	    }

	    private boolean _parse(IMAPInput input) {
	        assert input != null;
	        String saveAttDir = Utils.safeString(input.getSaveAttachmentsToDir());
	        //boolean handleNMAtt = input.getHandleNMAttachments();
	        synchronized (this) {
	            this.saveAttachmentsToDir = saveAttDir;
	            //this.handleNMAttachments = handleNMAtt;
	        }
	        focusedIMAPInput = input;
	        return true;
	    }

	    private boolean _parse(POP3Address address) {
	        assert address != null;
	        String emailServer = Utils.safeString(address.getEmailServer());
			String port = Utils.safeString(address.getPort());
			String userName = Utils.safeString(address.getUserName());
			String password = Utils.safeString(address.getPassword());
			boolean useSSL = address.getUseSSL();
            String maxMsgCount = Utils.safeString(address.getMaxMessageCount());
            String msgAckMode = Utils.safeString(address.getMessageAckMode());
            String msgAckOperation = Utils.safeString(address.getMessageAckOperation());
            String pollingInterval = Utils.safeString(address.getPollingInterval());
	        synchronized (this) {
				this.emailServer = emailServer;
				this.port = port;
				this.userName = userName;
				this.password = password;
				this.useSSL = useSSL;
                this.maxMessageCount = maxMsgCount;
                this.messageAckMode = msgAckMode;
                this.messageAckOperation = msgAckOperation;
                this.pollingInterval = pollingInterval;
	        }
	        focusedPOP3Address = address;
	        return true;
	    }

	    private boolean _parse(POP3Input input) {
	        assert input != null;
	        String saveAttDir = Utils.safeString(input.getSaveAttachmentsToDir());
	        //boolean handleNMAtt = input.getHandleNMAttachments();
	        synchronized (this) {
	            this.saveAttachmentsToDir = saveAttDir;
	            //this.handleNMAttachments = handleNMAtt;
	        }
	        focusedPOP3Input = input;
	        return true;
	    }

	    /**
	     * Given an IMAP Binding element, find an IMAP Address element whose port
	     * matches the binding.
	     */
	    private IMAPAddress findImapAddress(IMAPBinding imapBinding) {
	        assert imapBinding != null;
	        IMAPAddress imapAddress = null;
	        Definitions definitions = model.getDefinitions();
	        if (definitions != null) {
	            WSDLComponent parent = imapBinding.getParent();
	            if (Binding.class.isAssignableFrom(parent.getClass())) {
	                Binding targetBinding = (Binding)imapBinding.getParent();
	                search:
	                for (Service service : definitions.getServices()) {
	                    for (Port port : service.getPorts()) {
	                        if (isReferencing(port.getBinding())) {
	                            Binding binding = port.getBinding().get();
	                            if (binding.equals(targetBinding)) {
	                                List<IMAPAddress> addresses =
	                                        port.getExtensibilityElements(IMAPAddress.class);
	                                if (!addresses.isEmpty()) {
	                                    imapAddress = addresses.get(0);
	                                    break search;
	                                }
	                            }
	                        }
	                    }
	                }
	            }
	        }
	        return imapAddress;
	    }

	    /**
	     * Given an IMAP Binding element, find an IMAP Address element whose port
	     * matches the binding.
	     */
	    private POP3Address findPop3Address(POP3Binding pop3Binding) {
	        assert pop3Binding != null;
	        POP3Address pop3Address = null;
	        Definitions definitions = model.getDefinitions();
	        if (definitions != null) {
	            WSDLComponent parent = pop3Binding.getParent();
	            if (Binding.class.isAssignableFrom(parent.getClass())) {
	                Binding targetBinding = (Binding)pop3Binding.getParent();
	                search:
	                for (Service service : definitions.getServices()) {
	                    for (Port port : service.getPorts()) {
	                        if (isReferencing(port.getBinding())) {
	                            Binding binding = port.getBinding().get();
	                            if (binding.equals(targetBinding)) {
	                                List<POP3Address> addresses =
	                                        port.getExtensibilityElements(POP3Address.class);
	                                if (!addresses.isEmpty()) {
	                                    pop3Address = addresses.get(0);
	                                    break search;
	                                }
	                            }
	                        }
	                    }
	                }
	            }
	        }
	        return pop3Address;
	    }

	    /**
	     * Given an IMAP Address element, find the IMAP Binding associated with its
	     * port.
	     */
	    private IMAPBinding findImapBinding(IMAPAddress imapAddress) {
	        assert imapAddress != null;
	        IMAPBinding imapBinding = null;
	        WSDLComponent parent = imapAddress.getParent();
	        if (Port.class.isAssignableFrom(parent.getClass())) {
	            Port port = (Port) parent;
	            if (isReferencing(port.getBinding())) {
	                Binding binding = port.getBinding().get();
	                List<IMAPBinding> bindings = binding.getExtensibilityElements(
	                        IMAPBinding.class);
	                if (!bindings.isEmpty()) {
	                    imapBinding = bindings.get(0);
	                }
	            }
	        }
	        return imapBinding;
	    }

	    /**
	     * Given an POP3 Address element, find the POP3 Binding associated with its
	     * port.
	     */
	    private POP3Binding findPop3Binding(POP3Address pop3Address) {
	        assert pop3Address != null;
	        POP3Binding pop3Binding = null;
	        WSDLComponent parent = pop3Address.getParent();
	        if (Port.class.isAssignableFrom(parent.getClass())) {
	            Port port = (Port) parent;
	            if (isReferencing(port.getBinding())) {
	                Binding binding = port.getBinding().get();
	                List<POP3Binding> bindings = binding.getExtensibilityElements(
	                        POP3Binding.class);
	                if (!bindings.isEmpty()) {
	                    pop3Binding = bindings.get(0);
	                }
	            }
	        }
	        return pop3Binding;
	    }

	    /**
	     * Given an IMAP Binding element, find the first IMAP Input element
	     * belonging to the same binding.
	     */
	    private IMAPInput findImapInput(IMAPBinding imapBinding) {
	        assert imapBinding != null;
	        IMAPInput imapInput = null;
	        WSDLComponent parent = imapBinding.getParent();
	        if (Binding.class.isAssignableFrom(parent.getClass())) {
	            Binding binding = (Binding) parent;
	            if(binding.getBindingOperations().size() > 0){
	            		BindingOperation operation = binding.getBindingOperations().iterator().next();
	                    List<IMAPInput> imapInputs = operation.getBindingInput().getExtensibilityElements(IMAPInput.class);
	                    if(!imapInputs.isEmpty()){
	                        imapInput = imapInputs.get(0);
	                    }	                    
	            }
	        }
	        return imapInput;
	    }

	    /**
	     * Given an POP3 Binding element, find the first POP3 Input element
	     * belonging to the same binding.
	     */
	    private POP3Input findPop3Input(POP3Binding pop3Binding) {
	        assert pop3Binding != null;
	        POP3Input pop3Input = null;
	        WSDLComponent parent = pop3Binding.getParent();
	        if (Binding.class.isAssignableFrom(parent.getClass())) {
	            Binding binding = (Binding) parent;
	            if(binding.getBindingOperations().size() > 0){
	            		BindingOperation operation = binding.getBindingOperations().iterator().next();
	                    List<POP3Input> pop3Inputs = operation.getBindingInput().getExtensibilityElements(POP3Input.class);
	                    if(!pop3Inputs.isEmpty()){
	                        pop3Input = pop3Inputs.get(0);
	                    }
	            }
	        }
	        return pop3Input;
	    }

	    /**
	     * Given an IMAP extensibility element, find the IMAP Binding associated with
	     * it.
	     */
	    private IMAPBinding findImapBinding(IMAPComponent imapComponent) {
	        assert imapComponent != null;
	        IMAPBinding imapBinding = null;
	        WSDLComponent element =imapComponent;
	        if (IMAPAddress.class.isAssignableFrom(imapComponent.getClass())) {
	            imapBinding = findImapBinding((IMAPAddress) imapComponent);
	        } else {
	            // Traverse up the model until we find the SMTP Binding.
	            while (element != null && !Binding.class
	                    .isAssignableFrom(element.getClass())) {
	                element = element.getParent();
	            }
	            if (element != null && Binding.class
	                    .isAssignableFrom(element.getClass())) {
	                List<IMAPBinding> imapBindings = element.getExtensibilityElements(IMAPBinding.class);
	                if (!imapBindings.isEmpty()) {
	                    imapBinding = imapBindings.get(0);
	                }
	            }
	        }
	        return imapBinding;
	    }

	    /**
	     * Given an POP3 extensibility element, find the POP3 Binding associated with
	     * it.
	     */
	    private POP3Binding findPop3Binding(POP3Component pop3Component) {
	        assert pop3Component != null;
	        POP3Binding pop3Binding = null;
	        WSDLComponent element =pop3Component;
	        if (POP3Address.class.isAssignableFrom(pop3Component.getClass())) {
	            pop3Binding = findPop3Binding((POP3Address) pop3Component);
	        } else {
	            // Traverse up the model until we find the SMTP Binding.
	            while (element != null && !Binding.class
	                    .isAssignableFrom(element.getClass())) {
	                element = element.getParent();
	            }
	            if (element != null && Binding.class
	                    .isAssignableFrom(element.getClass())) {
	                List<POP3Binding> pop3Bindings = element.getExtensibilityElements(POP3Binding.class);
	                if (!pop3Bindings.isEmpty()) {
	                    pop3Binding = pop3Bindings.get(0);
	                }
	            }
	        }
	        return pop3Binding;
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
	    private String serializeBooleanOptions(BooleanOption[] ... optionSets) {
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
