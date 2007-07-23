package org.netbeans.modules.xml.wsdl.model.extensions.http.validation;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPAddress;
import org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPOperation;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPBinding.Verb;
import org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPUrlEncoded;
import org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPUrlReplacement;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.util.NbBundle;


public class HTTPComponentValidator
        implements Validator, HTTPComponent.Visitor {
    
    private static final String HTTP_HOST_TOKEN = "${HttpHostName}";
    private static final String HTTP_DEFAULT_PORT_TOKEN = "${HttpDefaultPort}";
    private static final String HTTPS_DEFAULT_PORT_TOKEN = "${HttpsDefaultPort}";
    
    private Validation mValidation;
    private ValidationType mValidationType;
    private ValidationResult mValidationResult;
    private Verb mVerb;
    
    @SuppressWarnings("unchecked")
    public static final ValidationResult EMPTY_RESULT = 
        new ValidationResult(Collections.EMPTY_SET, Collections.EMPTY_SET);
    
    public HTTPComponentValidator() {}
    
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
    public ValidationResult validate(Model model,
                                     Validation validation,
                                     ValidationType validationType) {
        
        mVerb = Verb.GET;
        mValidation = validation;
        mValidationType = validationType;
        
        // Prepare result container
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
            
            
            
            // -------------------BINDINGS----------------------
            for (Binding binding : defs.getBindings()) {
                
                // Validate http:binding
                // Only one http:binding must exist
                int numSoapBindings = binding.getExtensibilityElements(HTTPBinding.class).size();
                if (numSoapBindings > 1) {
                    results.add(
                            new Validator.ResultItem(
                                this,
                                Validator.ResultType.ERROR,
                                binding,
                                NbBundle.getMessage(
                                    HTTPComponentValidator.class,
                                    "HTTPBindingValidator.One_binding_allowed")));
                }
                for (HTTPBinding httpBinding: binding.getExtensibilityElements(HTTPBinding.class)) {
                    httpBinding.accept(this);
                }
                
                
                // Validate http:operation
                for (BindingOperation bindingOp: binding.getBindingOperations()) {
                    for (HTTPOperation httpOp: bindingOp.getExtensibilityElements(HTTPOperation.class)) {
                        httpOp.accept(this);
                    }
                
                    // Validate operation input for http:urlEncoded / http:urlReplacement
                    List<HTTPOperation> httpOpsList = bindingOp.getExtensibilityElements(HTTPOperation.class);
                    if(httpOpsList.size() > 0) {
                        BindingInput bindingInput = bindingOp.getBindingInput();
                        if (bindingInput != null) {

                            // Must have http:urlEncoded or http:urlReplacement element, but not both
                            int countUrlEncoded = bindingInput.getExtensibilityElements(HTTPUrlEncoded.class).size();
                            int countUrlReplacement = bindingInput.getExtensibilityElements(HTTPUrlReplacement.class).size();
                            int sum = countUrlEncoded + countUrlReplacement;
                            if (sum == 0) {
                                results.add(
                                        new Validator.ResultItem(
                                            this,
                                            Validator.ResultType.ERROR,
                                            bindingInput,
                                            NbBundle.getMessage(
                                                HTTPComponentValidator.class,
                                                "HTTPUrlEncodingValidator.Encoding_required")));
                            } else if (sum > 1) {
                                results.add(
                                        new Validator.ResultItem(
                                            this,
                                            Validator.ResultType.ERROR,
                                            bindingInput,
                                            NbBundle.getMessage(
                                                HTTPComponentValidator.class,
                                                "HTTPUrlEncodingValidator.One_encoding_allowed")));
                            }
                            for (HTTPUrlEncoded encodingElem: bindingInput.getExtensibilityElements(HTTPUrlEncoded.class)) {
                                encodingElem.accept(this);
                            }
                            for (HTTPUrlReplacement encodingElem: bindingInput.getExtensibilityElements(HTTPUrlReplacement.class)) {
                                encodingElem.accept(this);
                            }
                        }
                    }
                }
            }
            
            // -------------------SERVICES----------------------
            for (Service service: defs.getServices()) {
                for (Port port: service.getPorts()) {
                    if(port.getBinding() != null) {
                        Binding binding = port.getBinding().get();
                        if(binding != null) {
                            int numHttpAddresses = port.getExtensibilityElements(HTTPAddress.class).size();
                            int numHttpBindings = binding.getExtensibilityElements(HTTPBinding.class).size();
                            if(numHttpBindings > 0) {
                                if (numHttpAddresses == 0) {
                                    results.add(
                                            new Validator.ResultItem(
                                                this,
                                                Validator.ResultType.ERROR,
                                                port,
                                                NbBundle.getMessage(
                                                    HTTPComponentValidator.class,
                                                    "HTTPAddressValidator.Missing_address")
                                            )
                                    );
                                } else if (numHttpAddresses > 1) {
                                    results.add(
                                            new Validator.ResultItem(
                                                this,
                                                Validator.ResultType.ERROR,
                                                port,
                                                NbBundle.getMessage(
                                                    HTTPComponentValidator.class,
                                                    "HTTPAddressValidator.One_address_allowed")
                                            )
                                    );
                                }
                            }
                            for (HTTPAddress httpAddress: port.getExtensibilityElements(HTTPAddress.class)) {
                                httpAddress.accept(this);
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
    
    
    
    public void visit(HTTPAddress address) {
        Collection<ResultItem> results = mValidationResult.getValidationResult();
        
        String location = address.getLocation();
        if (location == null) {
            results.add(new Validator.ResultItem(
                    this,
                    Validator.ResultType.ERROR,
                    address,
                    NbBundle.getMessage(HTTPComponentValidator.class, "HTTPAddressValidator.Missing_location")
                    )
            );
            return;
        }
        
        if("REPLACE_WITH_ACTUAL_URL".equals(location)) {
            return;
        }
        
        
        
        // ---------Check for substitution variables/tokens--------
        
        // look for ${HttpDefaultPort} token 
        if (location.indexOf(HTTP_DEFAULT_PORT_TOKEN, 6) > 0) {
            if (location.startsWith("http")) {
                int portColonIndex = location.indexOf(":", 6);
                int contextStartIndex = location.indexOf("/", 7);
                if (HTTP_DEFAULT_PORT_TOKEN.equals(location.substring(portColonIndex + 1, contextStartIndex))) {
                    return;
                } else {
                    results.add(
                            new Validator.ResultItem(this,
                                Validator.ResultType.ERROR,
                                address,
                                NbBundle.getMessage(HTTPComponentValidator.class,
                                    "HTTPAddressValidator.Unsupported_location_attribute")
                            )
                    );
                    return;
                }
            }
        }
        
        // look for ${HttpsDefaultPort} token 
        if (location.indexOf(HTTPS_DEFAULT_PORT_TOKEN, 7) > 0) {
            if (location.startsWith("https")) {
                int portColonIndex = location.indexOf(":", 7);
                int contextStartIndex = location.indexOf("/", 8);
                if (HTTPS_DEFAULT_PORT_TOKEN.equals(location.substring(portColonIndex + 1, contextStartIndex))) {
                    return;
                } else {
                    results.add(
                            new Validator.ResultItem(this,
                                Validator.ResultType.ERROR,
                                address,
                                NbBundle.getMessage(HTTPComponentValidator.class,
                                    "HTTPAddressValidator.Unsupported_location_attribute")));
                    return;
                }
            }
        }
        
        if(containsToken(location)) {
            if(!isValidAddressToken(location)) {
                    results.add(
                        new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            address,
                            NbBundle.getMessage(HTTPComponentValidator.class,
                                "HTTPAddressValidator.Unsupported_token_format")
                        )
                    );
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
                uri.toURL();
            } catch (Exception ex) {
                results.add(
                    new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        address,
                        NbBundle.getMessage(HTTPComponentValidator.class,
                            "HTTPAddressValidator.Unsupported_location_attribute")
                    )
                );
            }   
        }
    }
    
    public void visit(HTTPBinding binding) {
        Collection<ResultItem> results = mValidationResult.getValidationResult();
        
        Verb verb = binding.getVerb();
        if (verb == null) {
            results.add(
                    new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        binding,
                        NbBundle.getMessage(HTTPComponentValidator.class,
                            "HTTPBindingValidator.Verb_required")));
        } else {
            mVerb = verb;
        }
    }
    
    public void visit(HTTPOperation operation) {
        Collection<ResultItem> results = mValidationResult.getValidationResult();
        
        if (operation.getLocation() != null) {
            // This is fine.  The URI can be anything.  In reality,
            // we should verify that this is a valid URI, but I don't want
            // to be too restrictive.
        } else {
            results.add(
                    new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        operation,
                        NbBundle.getMessage(HTTPComponentValidator.class,
                            "HTTPOperationValidator.Location_attribute_required")
                    )
            );
        }
    }

    public void visit(HTTPUrlEncoded enc) {
        Collection<ResultItem> results = mValidationResult.getValidationResult();
        
        if (mVerb == Verb.POST) {
            results.add(
                new Validator.ResultItem(this,
                    Validator.ResultType.WARNING,
                    enc,
                    NbBundle.getMessage(HTTPComponentValidator.class,
                        "HTTPUrlEncodingValidator.POST_ignores_urlEncoding")
                )
            );
        }
    }

    public void visit(HTTPUrlReplacement enc) {
        Collection<ResultItem> results = mValidationResult.getValidationResult();
        
        if (mVerb == Verb.POST) {
            results.add(
                new Validator.ResultItem(this,
                    Validator.ResultType.WARNING,
                    enc,
                    NbBundle.getMessage(HTTPComponentValidator.class,
                        "HTTPUrlEncodingValidator.POST_ignores_urlEncoding")
                )
            );
        }
    }

    private boolean containsToken(String val) {
        boolean hasToken = false;
        if (val != null && val.length() >= 4) {
            int idx1 = val.indexOf("${");
            int idx2 = (idx1 == -1 ? -1 : val.indexOf("}", idx1 + 1));
            hasToken = ((idx2 - idx1) >= 3);
        }
        return hasToken;
    }
    
    private boolean isToken(String val) {
        boolean isToken = false;
        if (val != null && val.length() >= 4) {
            isToken = val.charAt(0) == '$'
                    && val.charAt(1) == '{'
                    && val.indexOf("${", 1) == -1
                    && val.indexOf("}") == val.length() - 1;
        }
        return isToken;
    }

    /**
     * A token string can be of the following format: 
     * 1. http(s)://${host}:${port}/${context}
     * 2. http(s)://${host}/${context}
     * 2. ${URL}
     */
    private boolean isValidAddressToken(String tokenString) {
        boolean isValidToken = true;
        
        if(tokenString.startsWith("http://")) {
            //strip off the protocol stuff
            tokenString = tokenString.substring(7, tokenString.length());
        } else if(tokenString.startsWith("https://")) {
            //strip off the protocol stuff
            tokenString = tokenString.substring(8, tokenString.length());
        } else {
            //No protocol info, it better be of the format ${URL}
            isValidToken = "${".equals(tokenString.substring(0, 2))
                    && tokenString.charAt(tokenString.length() - 1) == '}';
        }
        
        if (isValidToken) {
            if(containsToken(tokenString)) {
                final int indexOfPortSeparator = tokenString.indexOf(":");
                final int indexOfContextSeparator = tokenString.lastIndexOf("/");
                
                // Port token
                if (indexOfPortSeparator != -1) {
                    isValidToken = indexOfPortSeparator < indexOfContextSeparator;
                    if (isValidToken) {
                        String port = tokenString.substring(indexOfPortSeparator + 1, indexOfContextSeparator);
                        isValidToken = isToken(port);
                    }
                } else {
                    // Port token is optional
                    ;
                }

                // Context token
                if(indexOfContextSeparator != -1) {
                    String context = tokenString.substring(indexOfContextSeparator+1, tokenString.length());
                    isValidToken = isToken(context);
                } else {
                    // Context token is mandatory
                    isValidToken = false;
                }

                // Host token
                if (indexOfPortSeparator != -1) {
                    isValidToken = isToken(tokenString.substring(0, indexOfPortSeparator));
                } else if (indexOfContextSeparator != -1) {
                    isValidToken = isToken(tokenString.substring(0, indexOfContextSeparator));
                } else {
                    // Host token is mandatory
                    isValidToken = false;
                }
            }
        }
        return isValidToken;
    }
}
