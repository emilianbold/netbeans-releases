package org.netbeans.modules.xml.wsdl.model.extensions.soap.validation;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPOperation;
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
import org.openide.util.NbBundle;


/**
 *
 * @author afung
 */
public class SOAPComponentValidator
        implements Validator, SOAPComponent.Visitor {
    
    private static final String HTTP_DEFAULT_PORT_TOKEN = "${HttpDefaultPort}";
    private static final String HTTPS_DEFAULT_PORT_TOKEN = "${HttpsDefaultPort}";
    
    private Validation mValidation;
    private ValidationType mValidationType;
    private ValidationResult mValidationResult;
    
    @SuppressWarnings("unchecked")
    public static final ValidationResult EMPTY_RESULT = 
        new ValidationResult( Collections.EMPTY_SET, 
                Collections.EMPTY_SET);
    
    /** Creates a new instance of SOAPComponentValidator */
    public SOAPComponentValidator() {}
    
    /////////////////////////////////////////////
    ////
    ////  Validator interface
    ////
    /////////////////////////////////////////////
    
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
        // Initialize the mValidation object
        mValidation = validation;
        
        // Initialize the mValidationType object
        mValidationType = validationType;
        
        // Initialize our result object
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
                int numSoapBindings = binding.getExtensibilityElements(SOAPBinding.class).size();
                if (numSoapBindings > 0 && numSoapBindings != 1) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            NbBundle.getMessage(SOAPComponentValidator.class, "SOAPBindingValidator.Only_one_binding_allowed")));
                }
                Iterator<SOAPBinding> soapBindings =
                        binding.getExtensibilityElements(SOAPBinding.class).iterator();
                while (soapBindings.hasNext()) {
                    soapBindings.next().accept(this);
                }
                Iterator<BindingOperation> bindingOps =
                        binding.getBindingOperations().iterator();
                while (bindingOps.hasNext()) {
                    BindingOperation bindingOp = bindingOps.next();
                    List soapOpsList = bindingOp.getExtensibilityElements(SOAPOperation.class);
                    Iterator<SOAPOperation> soapOps =
                            soapOpsList.iterator();
                    while (soapOps.hasNext()) {
                        soapOps.next().accept(this);
                    }
                    
                    if(soapOpsList.size() > 0) {
                        BindingInput bindingInput = bindingOp.getBindingInput();
                        if (bindingInput != null) {
                            Iterator<SOAPHeader> soapHeaders=
                                    bindingInput.getExtensibilityElements(SOAPHeader.class).iterator();
                            while (soapHeaders.hasNext()) {
                                SOAPHeader soapHeader = soapHeaders.next();
                                soapHeader.accept(this);
                                Iterator<SOAPHeaderFault> soapHeaderFaults =
                                        soapHeader.getSOAPHeaderFaults().iterator();
                                while(soapHeaderFaults.hasNext()) {
                                    soapHeaderFaults.next().accept(this);
                                }
                            }
                            
                            int numSoapBodies = bindingInput.getExtensibilityElements(SOAPBody.class).size();
                            if(numSoapBodies == 0) {
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        bindingInput,
                                        NbBundle.getMessage(SOAPComponentValidator.class, "SOAPBodyValidator.Atleast_one_body_Required")));
                                
                            } else if (numSoapBodies > 0 && numSoapBodies != 1) {
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        bindingInput,
                                        NbBundle.getMessage(SOAPComponentValidator.class, "SOAPBodyValidator.Only_one_body_allowed")));
                            }
                            Iterator<SOAPBody> soapBodies =
                                    bindingInput.getExtensibilityElements(SOAPBody.class).iterator();
                            while(soapBodies.hasNext()) {
                                soapBodies.next().accept(this);
                            }
                        }
                        
                        BindingOutput bindingOutput = bindingOp.getBindingOutput();
                        if (bindingOutput != null) {
                            Iterator<SOAPHeader> soapHeaders=
                                    bindingOutput.getExtensibilityElements(SOAPHeader.class).iterator();
                            while (soapHeaders.hasNext()) {
                                SOAPHeader soapHeader = soapHeaders.next();
                                soapHeader.accept(this);
                                Iterator<SOAPHeaderFault> soapHeaderFaults =
                                        soapHeader.getSOAPHeaderFaults().iterator();
                                while(soapHeaderFaults.hasNext()) {
                                    soapHeaderFaults.next().accept(this);
                                }
                            }
                            
                            int numSoapBodies = bindingOutput.getExtensibilityElements(SOAPBody.class).size();
                            if(numSoapBodies == 0) {
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        bindingOutput,
                                        NbBundle.getMessage(SOAPComponentValidator.class, "SOAPBodyValidator.Atleast_one_body_Required")));
                                
                            } else if (numSoapBodies > 0 && numSoapBodies != 1) {
                                
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        bindingOutput,
                                        NbBundle.getMessage(SOAPComponentValidator.class, "SOAPBodyValidator.Only_one_body_allowed")));
                            }
                            Iterator<SOAPBody> soapBodies =
                                    bindingOutput.getExtensibilityElements(SOAPBody.class).iterator();
                            while(soapBodies.hasNext()) {
                                soapBodies.next().accept(this);
                            }
                        }
                        
                        Iterator<BindingFault> bindingFaults =
                                bindingOp.getBindingFaults().iterator();
                        while (bindingFaults.hasNext()) {
                            BindingFault bindingFault = bindingFaults.next();
                            int numSoapFaults = bindingFault.getExtensibilityElements(SOAPFault.class).size();
                            if (numSoapFaults == 0) {
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        bindingFault,
                                        NbBundle.getMessage(SOAPComponentValidator.class, "SOAPFaultValidator.No_soap_fault_defined")));
                            }
                            if (numSoapFaults > 0 && numSoapFaults != 1) {
                                
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        bindingFault,
                                        NbBundle.getMessage(SOAPComponentValidator.class, "SOAPFaultValidator.Only_one_fault_allowed")));
                            }

                            Iterator<SOAPFault> soapFaults =
                                    bindingFault.getExtensibilityElements(SOAPFault.class).iterator();
                            while(soapFaults.hasNext()) {
                                SOAPFault soapFault = (SOAPFault) soapFaults.next();   // should be only one defined
                                if (!soapFault.getName().equals(bindingFault.getName()))  {                       
                                    results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        bindingFault,
                                        NbBundle.getMessage(SOAPComponentValidator.class, "SOAPFaultValidator.Fault_name_not_match")));
                                 }
                                 soapFault.accept(this);
                            }

                        }
                    }
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
                            int numRelatedSoapBindings = binding.getExtensibilityElements(SOAPBinding.class).size();
                            Iterator<SOAPAddress> soapAddresses = port.getExtensibilityElements(SOAPAddress.class).iterator();
                            if((numRelatedSoapBindings > 0) && (!soapAddresses.hasNext())){
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        port,
                                        NbBundle.getMessage(SOAPComponentValidator.class, "SOAPAddressValidator.Missing_SoapAddress")));
                            }
                            
                            if(port.getExtensibilityElements(SOAPAddress.class).size() > 1){
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        port,
                                        NbBundle.getMessage(SOAPComponentValidator.class, "SOAPAddressValidator.Only_one_SoapAddress_allowed")));
                            }
                            while (soapAddresses.hasNext()) {
                                soapAddresses.next().accept(this);
                            }
                        }
                    }
                }
            }
        }
        
        // Clear out our state
        mValidation = null;
        mValidationType = null;
	ValidationResult rv = mValidationResult;
	mValidationResult = null;
        
        return rv;
    }
    
    /////////////////////////////////////////////
    ////
    ////  SOAPComponent.Visitor interface
    ////
    /////////////////////////////////////////////
    
    public void visit(SOAPHeader header) {
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();
        
        
        NamedComponentReference<Message> message = header.getMessage();
        if (message == null) {
            results.add(
                    new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    header,
                    NbBundle.getMessage(SOAPComponentValidator.class, "SOAPHeaderValidator.Missing_message")));
        }
        
        String part = header.getPart();
        if (part == null) {
            results.add(
                    new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    header,
                    NbBundle.getMessage(SOAPComponentValidator.class, "SOAPHeaderValidator.Missing_part")));
        }
        
        try {
            SOAPMessageBase.Use use = header.getUse();
            if (use == null) {
                results.add(
                        new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        header,
                        NbBundle.getMessage(SOAPComponentValidator.class, "SOAPHeaderValidator.Missing_use")));
            }
        } catch (Throwable th) {
            results.add(
                    new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    header,
                    NbBundle.getMessage(SOAPComponentValidator.class, "SOAPHeaderValidator.Unsupported_header_use_attribute")));
        }
        
        Collection<String> encodingStyles = header.getEncodingStyles();
        if (encodingStyles != null) {
            // This is optional.  Don't verify contents at this point.
        }
        
        String namespace = header.getNamespace();
        if (namespace != null) {
            // This is optional.  We should verify that it is a valid URI, but
            // I don't want to be too restrictive at this point.
            
        }
    }
    
    public void visit(SOAPAddress address) {
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();
        
        String location = address.getLocation();
        if (location == null) {
            results.add(
                    new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    address,
                    NbBundle.getMessage(SOAPComponentValidator.class, "SOAPAddressValidator.Missing_location")));
            return;
        }
        
        ////////////////////////////////////////////////////////
        // GSR changed for Java EE Service Engine
        // As instructed by Jerry Waldorf.
        ////////////////////////////////////////////////////////
        if("REPLACE_WITH_ACTUAL_URL".equals(location)) {
            return;
        }
        
        ///////////////////////////////////////////////////////
        // Check for valid tokens for default HTTP and HTTPS port
        // Introduced to support clustering
        ////////////////////////////////////////////////////////
        
        if (location.indexOf(HTTP_DEFAULT_PORT_TOKEN, 6) > 0) {
            int colonIndex = -1;
            int contextStartIndex = -1;
            
            if (location.startsWith("http")) {
                // look for ${HttpDefaultPort} token 
                colonIndex = location.indexOf(":", 6);
                contextStartIndex = location.indexOf("/", 7);
                
                if (HTTP_DEFAULT_PORT_TOKEN.equals(location.substring(colonIndex + 1, contextStartIndex))) {
                    return;
                } else {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            address,
                            NbBundle.getMessage(SOAPComponentValidator.class, "SOAPAddressValidator.Unsupported_location_attribute")));
                    return;
                }
            }
        }
        
        if (location.indexOf(HTTPS_DEFAULT_PORT_TOKEN, 7) > 0) {
            int colonIndex = -1;
            int contextStartIndex = -1;
            
            if (location.startsWith("https")) {
                // look for ${HttpDefaultPort} token 
                colonIndex = location.indexOf(":", 7);
                contextStartIndex = location.indexOf("/", 8);
                
                if (HTTPS_DEFAULT_PORT_TOKEN.equals(location.substring(colonIndex + 1, contextStartIndex))) {
                    return;
                } else {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            address,
                            NbBundle.getMessage(SOAPComponentValidator.class, "SOAPAddressValidator.Unsupported_location_attribute")));
                    return;
                }
            }
        }
        
            if(containsToken(location)) {
                if(!isValidSoapAddressToken(location)) {
                        results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            address,
                            NbBundle.getMessage(SOAPComponentValidator.class, "SOAPAddressValidator.Unsupported_Token_Format")));
                    return;
                }
            } else {
                try {
                    URI uri = new URI(location);
                    String scheme = uri.getScheme();
                    if (!scheme.equalsIgnoreCase("http") &&
                        !scheme.equalsIgnoreCase("https")) {
                    return;
                    }
                    URL url = uri.toURL();
                } catch (Exception ex) {
                    results.add(
                        new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        address,
                        NbBundle.getMessage(SOAPComponentValidator.class, "SOAPAddressValidator.Unsupported_location_attribute")));
                }   
        }
    }
    
    public void visit(SOAPBinding binding) {
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();
        
        String transportURI = binding.getTransportURI();
        if (transportURI == null) {
            results.add(
                    new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    binding,
                    NbBundle.getMessage(SOAPComponentValidator.class, "SOAPBindingValidator.Transport_URI_required")));
        } else if (!transportURI.equals("http://schemas.xmlsoap.org/soap/http")) {
            results.add(
                    new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    binding,
                    NbBundle.getMessage(SOAPComponentValidator.class, "SOAPBindingValidator.Unsupported_transport")));
        }
        
        try {
            SOAPBinding.Style style = binding.getStyle();
        } catch (Throwable th) {
            results.add(
                    new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    binding,
                    NbBundle.getMessage(SOAPComponentValidator.class, "SOAPBindingValidator.Unsupported_style_attribute")));
        }
    }
    
    public void visit(SOAPBody body) {
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();
        
        
        Collection<String> encodingStyles = body.getEncodingStyles();
        if (encodingStyles != null) {
            // This is optional.  Don't verify contents at this point.
        }
        
        String namespace = body.getNamespace();
        if (namespace != null) {
            // This is optional.  We should verify that it is a valid URI, but
            // I don't want to be too restrictive at this point.
        }
        
        try {
            SOAPMessageBase.Use use = body.getUse();
        } catch (Throwable th) {
            results.add(
                    new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    body,
                    NbBundle.getMessage(SOAPComponentValidator.class, "SOAPBodyValidator.Unsupported_use_attribute")));
        }
        
        List<String> parts = body.getParts();
        if (parts != null) {
            // This is optional.  Don't verify contents at this point.
        }
        
        // Make sure that the Message definition exists
        /*
        WSDLComponent bindingMessage = body.getParent();
        System.out.println("BindingMessage: " + bindingMessage);
        if (bindingMessage instanceof BindingInput) {
            BindingInput bindingInput = (BindingInput)bindingMessage;
            System.out.println("BindingInput: " + bindingInput);
            System.out.println("BindingInput Name: "+ bindingInput.getName());
            System.out.println("Reference: " + bindingInput.getInput());
            Input abstractInput = bindingInput.getInput().get();
            if (abstractInput == null) {
                results.add(
                    new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        components,
                        NbBundle.getMessage(SOAPComponentValidator.class, "SOAPBodyValidator.No_abstract_message"),
                        ""));
            }
            NamedComponentReference<Message> message = abstractInput.getMessage();
            if (message == null) {
                results.add(
                    new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        components,
                        NbBundle.getMessage(SOAPComponentValidator.class, "SOAPBodyValidator.No_abstract_message"),
                        ""));
            }
         
        } else {
         
        }
         */
    }
    
    public void visit(SOAPFault fault) {
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();
        
        String name = fault.getName();
        if (name == null) {
            results.add(
                    new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    fault,
                    NbBundle.getMessage(SOAPComponentValidator.class, "SOAPFaultValidator.Missing_name")));
        }
        
        Collection<String> encodingStyles = fault.getEncodingStyles();
        if (encodingStyles != null) {
            // This is optional.  Don't verify contents at this point.
        }
        
        String namespace = fault.getNamespace();
        if (namespace != null) {
            // This is optional.  We should verify that it is a valid URI, but
            // I don't want to be too restrictive at this point.
        }
        
        try {
            SOAPMessageBase.Use use = fault.getUse();
        } catch (Throwable th) {
            results.add(
                    new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    fault,
                    NbBundle.getMessage(SOAPComponentValidator.class, "SOAPFaultValidator.Unsupported_use_attribute")));
        }
    }
    
    public void visit(SOAPHeaderFault headerFault) {
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();
        
        NamedComponentReference<Message> message = headerFault.getMessage();
        if (message == null) {
            results.add(
                    new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    headerFault,
                    NbBundle.getMessage(SOAPComponentValidator.class, "SOAPHeaderFaultValidator.Missing_header_fault_message")));
        }
        
        String part = headerFault.getPart();
        if (part == null) {
            results.add(
                    new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    headerFault,
                    NbBundle.getMessage(SOAPComponentValidator.class, "SOAPHeaderFaultValidator.Missing_header_fault_part")));
        }
        
        try {
            SOAPMessageBase.Use use = headerFault.getUse();
            if (use == null) {
                results.add(
                        new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        headerFault,
                        NbBundle.getMessage(SOAPComponentValidator.class, "SOAPHeaderFaultValidator.Missing_header_fault_use")));
            }
        } catch (Throwable th) {
            results.add(
                    new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    headerFault,
                    NbBundle.getMessage(SOAPComponentValidator.class, "SOAPHeaderFaultValidator.Unsupported_header_fault_use_attribute")));
        }
        
        
        Collection<String> encodingStyles = headerFault.getEncodingStyles();
        if (encodingStyles != null) {
            // This is optional.  Don't verify contents at this point.
        }
        
        String namespace = headerFault.getNamespace();
        if (namespace != null) {
            // This is optional.  We should verify that it is a valid URI, but
            // I don't want to be too restrictive at this point.
        }
        
    }
    
    public void visit(SOAPOperation operation) {
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();
        
        String soapActionURI = operation.getSoapAction();
        if (soapActionURI != null) {
            // This is fine.  The URI can be anything.  In reality,
            // we should verify that this is a valid URI, but I don't want
            // to be too restrictive.
        }
        
        try {
            SOAPBinding.Style style = operation.getStyle();
        } catch (Throwable th) {
            results.add(
                    new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    operation,
                    NbBundle.getMessage(SOAPComponentValidator.class, "SOAPOperationValidator.Unsupported_style_attribute")));
        }
    }


    private boolean containsToken(String val) {
        if(val.contains("${")) {                        
            return true;
        }
        return false;
    }

    /**
     * A token string can be of the following format: 
     * 1. http(s)://${host}:${port}/${context}
     * 2. ${URL}
     */
    private boolean isValidSoapAddressToken(String tokenString) {
        boolean containsProtocolInfo = false;
        boolean isValidToken = true;
        if(tokenString.startsWith("http://")) {
            //strip off the protocol stuff
            tokenString = tokenString.substring(7, tokenString.length());
            containsProtocolInfo = true;
        }
        if(tokenString.startsWith("https://")) {
            //strip off the protocol stuff
            tokenString = tokenString.substring(8, tokenString.length());
            containsProtocolInfo = true;
        }
        //No protocol info, it better be of the format ${URL}
        if(!containsProtocolInfo) {
            int indexOfTokenStart = tokenString.indexOf("${");
            int indexOfTokenEnd = tokenString.indexOf("}");
            if((indexOfTokenEnd == tokenString.length() - 1) && (indexOfTokenStart == 0)) {
                isValidToken = true;
            } else {
                return false;
            }
        }
        if(tokenString.contains("${") ) {
            int indexOfPortSeparator = tokenString.indexOf(":");
            int indexOfContextSeparator = tokenString.lastIndexOf("/");
            
            //Context separator / exists.
            if(indexOfContextSeparator != -1) {
                //The token is in the context.
                String context = tokenString.substring(indexOfContextSeparator+1, tokenString.length());
                int indexOfContextTokenStart = context.indexOf("${");
                if(indexOfContextTokenStart == 0) {
                    int indexOfTokenEnd = context.indexOf("}");
                    if(indexOfTokenEnd < indexOfContextTokenStart) {
                        return false;
                    }
                } else if(context.indexOf("}") > 0) {
                    return false;
                }
                
                int indexOfTokenStart = tokenString.indexOf("${");
                if(indexOfTokenStart == 0) { //The token is in the host
                    String host = tokenString.substring(1, indexOfPortSeparator);
                    int indexOfTokenEnd = host.indexOf("}");
                    if(indexOfTokenEnd < 1) {
                        return false;
                    }
                } else if(tokenString.substring(1, indexOfPortSeparator).indexOf("}") > 0) {
                    return false;
                }
                String port = tokenString.substring(indexOfPortSeparator + 1, indexOfContextSeparator);
                if((port.indexOf("${") != -1) && (port.indexOf("}") > 0)) {
                    isValidToken = true;
                } else {
                    return false;
                }
            }
        }
        return isValidToken;
    }
}
