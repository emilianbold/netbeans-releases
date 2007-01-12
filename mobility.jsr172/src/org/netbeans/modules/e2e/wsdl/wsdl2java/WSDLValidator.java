/*
 * WSDLValidator.java
 *
 * Created on October 30, 2006, 5:17 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.wsdl.wsdl2java;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.e2e.api.wsdl.Binding;
import org.netbeans.modules.e2e.api.wsdl.BindingInput;
import org.netbeans.modules.e2e.api.wsdl.BindingOperation;
import org.netbeans.modules.e2e.api.wsdl.BindingOutput;
import org.netbeans.modules.e2e.api.wsdl.Definition;
import org.netbeans.modules.e2e.api.wsdl.Port;
import org.netbeans.modules.e2e.api.wsdl.Service;
import org.netbeans.modules.e2e.api.wsdl.extensions.ExtensibilityElement;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPAddress;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPBinding;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPBody;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPOperation;
import org.netbeans.modules.e2e.api.wsdl.wsdl2java.WSDL2Java;
import org.netbeans.modules.e2e.api.wsdl.wsdl2java.WSDL2Java.ValidationResult;
import org.netbeans.modules.e2e.api.wsdl.wsdl2java.WSDL2Java.ValidationResult.ErrorLevel;
import org.netbeans.modules.e2e.wsdl.extensions.soap.SOAPConstants;
import org.openide.util.NbBundle;

/**
 *
 * @author Michal Skvor
 */
class WSDLValidator {
    
    private Definition definition;
            
    private List<ValidationResult> result;

    private Service service;
    
    // validation flags
    private boolean flagNoSoapBinding;
    
    
    /** Creates a new instance of WSDLValidator */
    public WSDLValidator( List<ValidationResult> result, Definition definition ) {
        this.definition = definition;
        
        this.result = new ArrayList();
        this.result.addAll( result );
    }
                
    public List<ValidationResult> validate() {
        Set flags = new HashSet();
        
        checkDefinition( definition );
                        
        printMessages();
        
        return result;
    }    
    
    private void checkDefinition( Definition definition ) {
        for( Service service : definition.getServices().values()) {
            
            checkService( service );
        }        
    }
    
    private void checkService( Service service ) {
        this.service = service;
        for( Port port : service.getPorts()) {
            checkPort( port );
        }
    }
    
    private void checkPort( Port port ) {        
        for( ExtensibilityElement portEE : port.getExtensibilityElements()) {
            if( SOAPConstants.ADDRESS.equals( portEE.getElementType())) {
                SOAPAddress soapAddress = (SOAPAddress) portEE;

                // Port contains at least one with SOAP binding
//                    flags.add( SOAPConstants.ADDRESS );

                Binding binding = port.getBinding();
                checkBinding( binding );
            }
        }
    }
    
    private void checkBinding( Binding binding ) {        
        for( ExtensibilityElement bindingEE : binding.getExtensibilityElements()) {
            if( SOAPConstants.BINDING.equals( bindingEE.getElementType())) {
                SOAPBinding soapBinding = (SOAPBinding) bindingEE;

                // Binding is SOAP
//                flags.add( SOAPConstants.BINDING );

                System.err.println(" binding - style = " + soapBinding.getStyle());

                for( BindingOperation bindingOperation : binding.getBindingOperations()) {
                    checkBindingOperation( bindingOperation );
                }
            }
        }
    }
    
    private void checkBindingOperation( BindingOperation bindingOperation ) {        
        for( ExtensibilityElement bindingOperationEE : bindingOperation.getExtensibilityElements()) {
            if( SOAPConstants.OPERATION.equals( bindingOperationEE.getElementType())) {
                SOAPOperation soapOperation = (SOAPOperation) bindingOperationEE;
                System.err.println(" operation - style = " + soapOperation.getStyle());
                if( !SOAPConstants.STYLE_DOCUMENT.equals( soapOperation.getStyle())) {
                    addMessage( ErrorLevel.FATAL, "0001", bindingOperation.getName());
                }

                BindingInput bindingInput = bindingOperation.getBindingInput();
                if (bindingInput != null) {//it is not notification operation
                    for( ExtensibilityElement bindingInputEE : bindingInput.getExtensibilityElements()) {
                        if( SOAPConstants.BODY.equals( bindingInputEE.getElementType())) {
                            SOAPBody soapBody = (SOAPBody) bindingInputEE;
                            System.err.println("body - use = " + soapBody.getUse());
                            if( !SOAPConstants.USE_LITERAL.equals( soapBody.getUse())) {
                                addMessage( ErrorLevel.FATAL, "0002", bindingOperation.getName(), "input" );
                            }
                        }
                    }
                }
                BindingOutput bindingOutput = bindingOperation.getBindingOutput();                                            
                if (bindingOutput != null) {//it is not one way operation
                    for( ExtensibilityElement bindingOutputEE : bindingOutput.getExtensibilityElements()) {
                        if( SOAPConstants.BODY.equals( bindingOutputEE.getElementType())) {
                            SOAPBody soapBody = (SOAPBody) bindingOutputEE;
                            System.err.println("body - use = " + soapBody.getUse());
                            if( !SOAPConstants.USE_LITERAL.equals( soapBody.getUse())) {
                                addMessage( ErrorLevel.FATAL, "0002", bindingOperation.getName(), "output" );
                            }
                        }
                    }
                }
            }
        }                                                                                
    }
    
    private void printMessages() {
        System.err.println("Validation messages: " + result.size());
        for( ValidationResult msg : result ) {
            System.err.println(" - " + msg.getErrorLevel() + " " + msg.getMessage());
        }
    }
    
    
    private void addMessage( ValidationResult.ErrorLevel errorLevel, String messageKey ) {
        result.add( new ValidationResult( errorLevel, NbBundle.getMessage( WSDLValidator.class, messageKey )));
    }
    
    private void addMessage( ValidationResult.ErrorLevel errorLevel, String messageKey, String param1 ) {
        result.add( new ValidationResult( errorLevel, NbBundle.getMessage( WSDLValidator.class, messageKey, param1 )));
    }
    
    private void addMessage( ValidationResult.ErrorLevel errorLevel, String messageKey, String param1, String param2 ) {
        result.add( new ValidationResult( errorLevel, NbBundle.getMessage( WSDLValidator.class, messageKey, param1, param2 )));
    }
    
    private void addMessage( ValidationResult.ErrorLevel errorLevel, String messageKey, String[] params ) {
        result.add( new ValidationResult( errorLevel, NbBundle.getMessage( WSDLValidator.class , messageKey, params )));
    }
}
