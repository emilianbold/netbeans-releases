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
import javax.xml.namespace.QName;
import org.netbeans.modules.e2e.api.schema.Element;
import org.netbeans.modules.e2e.api.schema.SchemaConstruct;
import org.netbeans.modules.e2e.api.schema.Type;
import org.netbeans.modules.e2e.api.wsdl.Binding;
import org.netbeans.modules.e2e.api.wsdl.BindingInput;
import org.netbeans.modules.e2e.api.wsdl.BindingOperation;
import org.netbeans.modules.e2e.api.wsdl.BindingOutput;
import org.netbeans.modules.e2e.api.wsdl.Definition;
import org.netbeans.modules.e2e.api.wsdl.Input;
import org.netbeans.modules.e2e.api.wsdl.Message;
import org.netbeans.modules.e2e.api.wsdl.Operation;
import org.netbeans.modules.e2e.api.wsdl.Output;
import org.netbeans.modules.e2e.api.wsdl.Part;
import org.netbeans.modules.e2e.api.wsdl.Port;
import org.netbeans.modules.e2e.api.wsdl.PortType;
import org.netbeans.modules.e2e.api.wsdl.Service;
import org.netbeans.modules.e2e.api.wsdl.extensions.ExtensibilityElement;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPAddress;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPBinding;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPBody;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPOperation;
import org.netbeans.modules.e2e.api.wsdl.wsdl2java.WSDL2Java;
import org.netbeans.modules.e2e.api.wsdl.wsdl2java.WSDL2Java.ValidationResult;
import org.netbeans.modules.e2e.api.wsdl.wsdl2java.WSDL2Java.ValidationResult.ErrorLevel;
import org.netbeans.modules.e2e.schema.SchemaConstants;
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
//            System.err.println("Checking port: " + port.getName());
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
//                System.err.println("Checking binding: " + binding.getName());
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

//                System.err.println(" binding - style = " + soapBinding.getStyle());

                for( BindingOperation bindingOperation : binding.getBindingOperations()) {
//                    System.err.println("Checking bindingOperation:" + bindingOperation.getName());
                    checkBindingOperation( bindingOperation );
                }
                PortType portType = binding.getPortType();
                for( Operation operation : portType.getOperations()) {
//                    System.err.println("Checking operation: " + operation.getName());
                    checkOperation( operation );
                }
            }
        }
    }
    
    private void checkBindingOperation( BindingOperation bindingOperation ) {        
        SOAPOperation soapOperation = null;
        for( ExtensibilityElement bindingOperationEE : bindingOperation.getExtensibilityElements()) {
            if( SOAPConstants.OPERATION.equals( bindingOperationEE.getElementType())) {
                if( soapOperation == null ) {
                    soapOperation = (SOAPOperation) bindingOperationEE;
                } else {
                    addMessage( ErrorLevel.FATAL, "0011", bindingOperation.getName());
                }
            }
        }                                                                                
        if( soapOperation != null ) {
//            System.err.println(" operation - style = " + soapOperation.getStyle());
            if( !SOAPConstants.STYLE_DOCUMENT.equals( soapOperation.getStyle())) {
                addMessage( ErrorLevel.FATAL, "0001", bindingOperation.getName());
            }
        }
        
        // BindingInput
        BindingInput bindingInput = bindingOperation.getBindingInput();
        if( bindingInput != null ) {//it is not notification operation
            for( ExtensibilityElement bindingInputEE : bindingInput.getExtensibilityElements()) {
                if( SOAPConstants.BODY.equals( bindingInputEE.getElementType())) {
                    SOAPBody soapBody = (SOAPBody) bindingInputEE;
//                        System.err.println("body - use = " + soapBody.getUse());
                    if( !SOAPConstants.USE_LITERAL.equals( soapBody.getUse())) {
                        addMessage( ErrorLevel.FATAL, "0002", bindingOperation.getName(), "input" );
                    }
                }
            }
        } else {
            addMessage( ErrorLevel.FATAL, "0012", bindingOperation.getName());
        }
        
        // BindingOutput
        BindingOutput bindingOutput = bindingOperation.getBindingOutput();                                            
        if( bindingOutput != null ) {//it is not one way operation
            for( ExtensibilityElement bindingOutputEE : bindingOutput.getExtensibilityElements()) {
                if( SOAPConstants.BODY.equals( bindingOutputEE.getElementType())) {
                    SOAPBody soapBody = (SOAPBody) bindingOutputEE;
//                    System.err.println("body - use = " + soapBody.getUse());
                    if( !SOAPConstants.USE_LITERAL.equals( soapBody.getUse())) {
                        addMessage( ErrorLevel.FATAL, "0002", bindingOperation.getName(), "output" );
                    }
                }
            }
        } else {
            addMessage( ErrorLevel.FATAL, "0013", bindingOperation.getName());
        }
    }
    
    private void checkOperation( Operation operation ) {        
        // Output
        Output output = operation.getOutput();
        if( output == null ) {
            addMessage( ErrorLevel.FATAL, "0007", operation.getName());
            return;
        }
        Message message = output.getMessage();
        if( message.getParts().size() == 0 ) {
            addMessage( ErrorLevel.FATAL, "0014", message.getName());
        } else if( message.getParts().size() > 1 ) {
            addMessage( ErrorLevel.FATAL, "0006", operation.getName());
        } else {
            
        }
        for( Part part : message.getParts()) {
            QName elementName = part.getElementName();
            QName typeName = part.getTypeName();
            Element element = null;
            Type type = null;
            if( elementName != null ) {
                element = definition.getSchemaHolder().getSchemaElement( elementName );
                type = element.getType();
                checkType( element, new HashSet());
            } else {
                addMessage( ErrorLevel.FATAL, "0008", operation.getName());
            }
        }
        
        // Input
        Input input = operation.getInput();
        if( input == null ) {
            addMessage( ErrorLevel.FATAL, "0009", operation.getName());
            return;
        }
        message = input.getMessage();
        if( message.getParts().size() == 0 ) {            
            addMessage( ErrorLevel.FATAL, "0014", message.getName());
        } else if( message.getParts().size() > 1 ) {
            addMessage( ErrorLevel.FATAL, "0006", operation.getName());
        } else {
            
        }
        
        for( Part part : message.getParts()) {
            QName elementName = part.getElementName();
            QName typeName = part.getTypeName();
            Element element = null;
            Type type = null;
            if( elementName != null ) {
                element = definition.getSchemaHolder().getSchemaElement( elementName );
                type = element.getType();
                checkType( element, new HashSet());
            } else {
                addMessage( ErrorLevel.FATAL, "0008", operation.getName());
            }
        }
    }
    
    private void checkType( Element element, Set<Element> usedComplexTypes ) {
        Type type = element.getType();
        if( Type.FLAVOR_PRIMITIVE == type.getFlavor()) {
            QName typeName = type.getName();
            if( SchemaConstants.TYPE_INT.equals( typeName )) {
            } else if( SchemaConstants.TYPE_BOOLEAN.equals( typeName )) {
            } else if( SchemaConstants.TYPE_BYTE.equals( typeName )) {
            } else if( SchemaConstants.TYPE_DOUBLE.equals( typeName )) {
            } else if( SchemaConstants.TYPE_FLOAT.equals( typeName )) {
            } else if( SchemaConstants.TYPE_LONG.equals( typeName )) {
            } else if( SchemaConstants.TYPE_SHORT.equals( typeName )) {
            } else if( SchemaConstants.TYPE_BASE64_BINARY.equals( typeName )) {
            } else if( SchemaConstants.TYPE_HEX_BINARY.equals( typeName )) {
            } else if( SchemaConstants.TYPE_STRING.equals( typeName )) {
            } else {
                addMessage( ErrorLevel.FATAL, "0003", typeName.toString());
            }
        } else if( Type.FLAVOR_SEQUENCE == type.getFlavor()) {
            usedComplexTypes.add( element );
            if( type.getSubconstructs().size() == 0 ) {
                return;
            } else {
                for( SchemaConstruct sc : type.getSubconstructs()) {
                    if( SchemaConstruct.ConstructType.ELEMENT.equals( sc.getConstructType())) {
                        Element sce = (Element) sc;
                        if( !usedComplexTypes.contains( sce )) {
                            checkType( sce, usedComplexTypes );
                        } else {
                            addMessage( ErrorLevel.FATAL, "0010" );
                        }
                    } else {
                        addMessage( ErrorLevel.FATAL, "0005", element.getName().toString());
                    }
                }
            }
        } else {
            addMessage( ErrorLevel.FATAL, "0004", element.getName().toString());
        }
    }
    
    private void printMessages() {
//        System.err.println("Validation messages: " + result.size());
//        for( ValidationResult msg : result ) {
//            System.err.println(" - " + msg.getErrorLevel() + " " + msg.getMessage());
//        }
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
