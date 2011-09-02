/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import org.netbeans.modules.e2e.api.wsdl.Message.MessageReference;
import org.netbeans.modules.e2e.api.wsdl.PortType.PortTypeReference;
import org.netbeans.modules.e2e.api.wsdl.extensions.ExtensibilityElement;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPAddress;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPBinding;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPBody;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPOperation;
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
                if ( portType instanceof PortTypeReference ){
                    PortTypeReference reference = (PortTypeReference)portType;
                    if ( !reference.isValid() ){
                        addMessage( ErrorLevel.FATAL, "0016", binding.getName(), 
                                reference.getQName().toString() ); 
                    }
                }
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
        boolean isValid = true; 
        if ( message instanceof MessageReference ){
            MessageReference reference = (MessageReference)message;
            isValid = reference.isValid();
            if ( !isValid ){
                QName name = reference.getQName();
                addMessage(ErrorLevel.FATAL, "0017", operation.getName(), 
                        name.toString());
            }
        }
        if ( isValid ){
            if( message.getParts().size() == 0 ) {
                addMessage( ErrorLevel.FATAL, "0014", message.getName());
            } else if( message.getParts().size() > 1 ) {
                addMessage( ErrorLevel.FATAL, "0006", operation.getName());
            }
        }
        
        for( Part part : message.getParts()) {
            QName elementName = part.getElementName();
            QName typeName = part.getTypeName();
            if ( elementName != null && typeName != null ){
                addMessage( ErrorLevel.FATAL, "0023", message.getName(),
                        operation.getName(),part.getName() );
                continue;
            }
            Element element = null;
            Type type = null;
            if( elementName != null ) {
                element = definition.getSchemaHolder().getSchemaElement( elementName );
                if ( element == null ){
                    addMessage( ErrorLevel.FATAL, "0019", message.getName(),
                            operation.getName(),part.getName(), elementName.toString());
                }
                else { 
                    checkType( element, null,  new HashSet<Element>());
                }
            }
            /*
             * javax.microedition.xml.rpc.Operation.newInstance() method
             * takes ONLY Elements as input/output arguments.  
             * It is not possible to put there Type argument.
             * So "type" attribute should not be used here.
             * 
             * else if ( typeName != null ){
                type = definition.getSchemaHolder().getSchemaType( typeName );
                if ( type == null ){
                    addMessage( ErrorLevel.FATAL, "0021", message.getName(),
                            operation.getName(),part.getName(), typeName.toString());
                }
                checkType( null, type, new HashSet<Element>());
            }*/
            else {
                addMessage( ErrorLevel.FATAL, "0008", message.getName(),
                        operation.getName(),part.getName());
            }
        }
        
        // Input
        Input input = operation.getInput();
        if( input == null ) {
            addMessage( ErrorLevel.FATAL, "0009", operation.getName());
            return;
        }
        isValid = true;
        message = input.getMessage();
        if ( message instanceof MessageReference ){
            MessageReference reference = (MessageReference)message;
            isValid = reference.isValid();
            if ( !isValid ){
                QName name = reference.getQName();
                addMessage(ErrorLevel.FATAL, "0018", operation.getName(), 
                        name.toString());
            }
        }
        if ( isValid ){
            if( message.getParts().size() == 0 ) {            
                addMessage( ErrorLevel.FATAL, "0014", message.getName());
            } else if( message.getParts().size() > 1 ) {
                addMessage( ErrorLevel.FATAL, "0006", operation.getName());
            } 
        }
        
        for( Part part : message.getParts()) {
            QName elementName = part.getElementName();
            QName typeName = part.getTypeName();
            if ( elementName != null && typeName != null ){
                addMessage( ErrorLevel.FATAL, "0024", message.getName(),
                        operation.getName(),part.getName() );
                continue;
            }
            Element element = null;
            Type type = null;
            if( elementName != null ) {
                element = definition.getSchemaHolder().getSchemaElement( elementName );
                if ( element == null ){
                    addMessage( ErrorLevel.FATAL, "0022", message.getName(),
                            operation.getName(),part.getName(), elementName.toString());
                }
                else {
                    checkType( element, null, new HashSet<Element>());
                }
            }
            /*
             * javax.microedition.xml.rpc.Operation.newInstance() method
             * takes ONLY Elements as input/output arguments.  
             * It is not possible to put there Type argument.
             * So "type" attribute should not be used here.
             * 
             else if ( typeName != null ){
                type = definition.getSchemaHolder().getSchemaType( typeName );
                if ( type == null ){
                    addMessage( ErrorLevel.FATAL, "0021", message.getName(),
                            operation.getName(),part.getName(), typeName.toString());
                }
                checkType( null, type, new HashSet<Element>());
            }*/
            else {
                addMessage( ErrorLevel.FATAL, "0008", message.getName(),
                        operation.getName(),part.getName());
            }
        }
    }
    
    private void checkType( Element element,  Type type,
            Set<Element> usedComplexTypes ) 
    {
        if ( type == null ){
            type = element.getType();
        }
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
            } else if( SchemaConstants.TYPE_QNAME.equals( typeName )) {
                addMessage( ErrorLevel.WARNING, "0015" );
            } else {
                addMessage( ErrorLevel.FATAL, "0003", typeName.toString());
            }
        } else if( Type.FLAVOR_SEQUENCE == type.getFlavor()) {
            if ( element!= null ){
                usedComplexTypes.add( element );
            }
            if( type.getSubconstructs().size() == 0 ) {
                return;
            } else {
                for( SchemaConstruct sc : type.getSubconstructs()) {
                    if( SchemaConstruct.ConstructType.ELEMENT.equals( sc.getConstructType())) {
                        Element sce = (Element) sc;
                        if( !usedComplexTypes.contains( sce )) {
                            checkType( sce, null, usedComplexTypes );
                        } else {
                            addMessage( ErrorLevel.FATAL, "0010" );
                        }
                    } else {
                        if ( element != null ){
                            addMessage( ErrorLevel.FATAL, "0005", 
                                    element.getName().toString());
                        }
                        else {
                            addMessage( ErrorLevel.FATAL, "0025", 
                                    type.getName().toString());
                        }
                    }
                }
            }
        } else {
            if ( element != null ){
                addMessage( ErrorLevel.FATAL, "0004", element.getName().toString());
            }
            else {
                addMessage( ErrorLevel.FATAL, "0026", type.getName().toString());
            }
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
    
    private void addMessage( ValidationResult.ErrorLevel errorLevel, String messageKey, String... params ) {
        result.add( new ValidationResult( errorLevel, NbBundle.getMessage( WSDLValidator.class , messageKey, params )));
    }
}
