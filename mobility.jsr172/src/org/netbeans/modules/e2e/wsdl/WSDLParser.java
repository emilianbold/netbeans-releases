/*
 * WSDLParser.java
 *
 * Created on September 24, 2006, 4:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.wsdl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.modules.e2e.api.schema.SchemaException;
import org.netbeans.modules.e2e.api.wsdl.Binding;
import org.netbeans.modules.e2e.api.wsdl.BindingFault;
import org.netbeans.modules.e2e.api.wsdl.BindingInput;
import org.netbeans.modules.e2e.api.wsdl.BindingOperation;
import org.netbeans.modules.e2e.api.wsdl.BindingOutput;
import org.netbeans.modules.e2e.api.wsdl.Definition;
import org.netbeans.modules.e2e.api.wsdl.Fault;
import org.netbeans.modules.e2e.api.wsdl.Input;
import org.netbeans.modules.e2e.api.wsdl.Message;
import org.netbeans.modules.e2e.api.wsdl.Operation;
import org.netbeans.modules.e2e.api.wsdl.Output;
import org.netbeans.modules.e2e.api.wsdl.Part;
import org.netbeans.modules.e2e.api.wsdl.Port;
import org.netbeans.modules.e2e.api.wsdl.PortType;
import org.netbeans.modules.e2e.api.wsdl.Service;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPAddress;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPBinding;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPBody;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPFault;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPHeader;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPHeaderFault;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPOperation;
import org.netbeans.modules.e2e.api.wsdl.wsdl2java.WSDL2Java;
import org.netbeans.modules.e2e.schema.SchemaParser;
import org.netbeans.modules.e2e.wsdl.extensions.soap.SOAPAddressImpl;
import org.netbeans.modules.e2e.wsdl.extensions.soap.SOAPBindingImpl;
import org.netbeans.modules.e2e.wsdl.extensions.soap.SOAPBodyImpl;
import org.netbeans.modules.e2e.wsdl.extensions.soap.SOAPConstants;
import org.netbeans.modules.e2e.wsdl.extensions.soap.SOAPFaultImpl;
import org.netbeans.modules.e2e.wsdl.extensions.soap.SOAPHeaderImpl;
import org.netbeans.modules.e2e.wsdl.extensions.soap.SOAPOperationImpl;
import org.openide.util.Exceptions;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Michal Skvor
 */
public class WSDLParser extends DefaultHandler {

    private static final String SCHEMA  = "http://www.w3.org/2001/XMLSchema";    
        
    /* WSDL states */
    private static final String DEFINITION          = "definitions";
    private static final String MESSAGE             = "message";
    private static final String PART                = "part";
    private static final String SERVICE             = "service";
    private static final String PORT                = "port";    
    private static final String PORT_TYPE           = "portType";
    private static final String OPERATION           = "operation";
    
    private static final String BINDING             = "binding";
    private static final String BINDING_OPERATION   = "binding-operation";
    private static final String BINDING_INPUT       = "binding-input";
    private static final String BINDING_OUTPUT      = "binding-output";
    private static final String BINDING_FAULT       = "binding-fault";

    private static final String INPUT               = "input";
    private static final String OUTPUT              = "output";
    private static final String FAULT               = "fault";
    
    private List<WSDL2Java.ValidationResult> validationResults;    
    
    public WSDLParser() {
        validationResults = new ArrayList<WSDL2Java.ValidationResult>();
    }
    
    public Definition parse( String uri ) throws WSDLException {
        definition = new DefinitionImpl();
        
//        DefaultHandler handler = new DefaultHandlerImpl( definition );
        SchemaParser schemaParser = new SchemaParser();
        
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            spf.setNamespaceAware( true );
            
            SAXParser parser = spf.newSAXParser();
            parser.parse( uri, this );
            try {                
                schemaParser.parseLocation( uri );
            } catch (SchemaException ex) {
                throw new WSDLException( ex );
            }
        } catch( SAXException e ) {
            validationResults.add( new WSDL2Java.ValidationResult(
                WSDL2Java.ValidationResult.ErrorLevel.FATAL, "Error during parsing : " + e.getMessage()));
        } catch( ParserConfigurationException e ) {
            validationResults.add( new WSDL2Java.ValidationResult(
                WSDL2Java.ValidationResult.ErrorLevel.FATAL, "Error during parsing : " + e.getMessage()));
        } catch( IOException e ) {
            validationResults.add( new WSDL2Java.ValidationResult(
                WSDL2Java.ValidationResult.ErrorLevel.FATAL, "Communication error : " + e.getMessage()));
        } 
        definition.setSchemaHolder( schemaParser.getSchemaHolder());
        validationResults.addAll( schemaParser.getValidationResults());
        
        return definition;
    }
    
    public List<WSDL2Java.ValidationResult> getValidationResults() {
        return Collections.unmodifiableList( validationResults );
    }    
    
//    private static class DefaultHandlerImpl extends DefaultHandler {
        
        private Stack<String> state = new Stack<String>();
        
        private Definition definition;
        private Message message;
        private Part part;
        private Service service;
        private Port port;
        private PortType portType;
        private Operation operation;
        private Input input;
        private Output output;
        private Fault fault;
        
        private Binding binding;
        private BindingOperation bindingOperation;
        private BindingInput bindingInput;
        private BindingOutput bindingOutput;
        private BindingFault bindingFault;
        
        private Map<String, String> prefixMapping = new HashMap<String, String>();
        private String targetNamespace;
        
        private SOAPAddress soapAddress;
        private SOAPBinding soapBinding;
        private SOAPOperation soapOperation;
        private SOAPBody soapBody;
        private SOAPHeader soapHeader;
        private SOAPHeaderFault soapHeaderFault;
        private SOAPFault soapFault;
        
        private String tagString;        
                
        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
//            System.err.println(" - mapping : " + prefix + " ~ " + uri );
            prefixMapping.put( prefix, uri );
        }        

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) 
                throws SAXException 
        {
            tagString = "";
            if( uri.equals( SOAPConstants.SOAP_URI )) {
                // soap:address
                if( localName.equals( SOAPConstants.ADDRESS.getLocalPart()) & state.peek().equals( PORT )) {
                    soapAddress = new SOAPAddressImpl( attributes.getValue( "location" ));
                    return;
                }
                // soap:binding
                if( localName.equals( SOAPConstants.BINDING.getLocalPart()) && state.peek().equals( BINDING )) {
                    String style = attributes.getValue( "style" ) != null ? attributes.getValue( "style" ) : SOAPConstants.STYLE_DOCUMENT;
                    soapBinding = new SOAPBindingImpl( attributes.getValue( "transport" ), style);
                    return;
                }
                // soap:operation
                if( localName.equals( SOAPConstants.OPERATION.getLocalPart()) && state.peek().equals( BINDING_OPERATION )) {
                    soapOperation = new SOAPOperationImpl();
                    // TODO: validation
                    soapOperation.setSoapActionURI( attributes.getValue( "soapAction" ));
                    String style = attributes.getValue( "style" ) != null ? attributes.getValue( "style" ) : soapBinding.getStyle();
                    soapOperation.setStyle( style );
                    
                    return;
                }
                // soap:body
                if( localName.equals( SOAPConstants.BODY.getLocalPart()) && ( 
                        state.peek().equals( BINDING_INPUT ) || state.peek().equals( BINDING_OUTPUT ))) 
                {
                    soapBody = new SOAPBodyImpl( attributes.getValue( "use" ));
                    // TODO: parse parts
                    // TODO: parse encoding styles
                    soapBody.setNamespaceURI( attributes.getValue( "namespace" ));
                    return;
                }
                // soap:header
                if( localName.equals( SOAPConstants.HEADER.getLocalPart()) && ( 
                        state.peek().equals( BINDING_INPUT ) || state.peek().equals( BINDING_OUTPUT ))) {
                    soapHeader = new SOAPHeaderImpl( parseQName( attributes.getValue( "message" )),
                            attributes.getValue( "part" ), attributes.getValue( "use" ));
                    // TODO: parse encoding styles
                    soapHeader.setNamespaceURI( attributes.getValue( "namespace" ));
                    return;
                }
                // soap:headerfault
                // TODO: soap:headerfault
                // soap:fault
                if( localName.equals( SOAPConstants.FAULT.getLocalPart()) && state.peek().equals( BINDING_FAULT )) {
                    soapFault = new SOAPFaultImpl( attributes.getValue( "name" ), attributes.getValue( "use" ));
                    // TODO: parse encoding style
                    soapFault.setNamespaceURI( attributes.getValue( "namespace" ));
                    return;
                }                
            }            
            
            if( uri.equals( WSDLConstants.WSDL_URI )) {
//                System.err.println("<" + localName + ">" );
                if( localName.equals( DEFINITION ) && state.empty()) {
                    targetNamespace = attributes.getValue( "targetNamespace" );
                    definition.setTargetNamespace( targetNamespace );
                    state.push( DEFINITION );
                    return;
                }
                
                // Message
                if( localName.equals( WSDLConstants.MESSAGE.getLocalPart()) && state.peek().equals( DEFINITION )) {
                    state.push( MESSAGE );
                    String name = attributes.getValue( "name" );
                    message = new MessageImpl( name );
                    return;
                }
                // Message Part
                if( localName.equals( WSDLConstants.PART.getLocalPart()) && state.peek().equals( MESSAGE )) {
                    state.push( PART );
                    part = new PartImpl( attributes.getValue( "name" ),
                            parseQName( attributes.getValue( "type" )), parseQName( attributes.getValue( "element" )));
                    return;
                }

                // PortType
                if( localName.equals( WSDLConstants.PORT_TYPE.getLocalPart()) && state.peek().equals( DEFINITION )) {
                    state.push( PORT_TYPE );
                    portType = new PortTypeImpl( attributes.getValue( "name" ));
                    return;
                }
                // Operation
                if( localName.equals( WSDLConstants.OPERATION.getLocalPart()) && state.peek().equals( PORT_TYPE )) {
                    state.push( OPERATION );
                    operation = new OperationImpl( attributes.getValue( "name" ));
                    // TODO: parameterOrder
                    return;
                }
                if( localName.equals( WSDLConstants.INPUT.getLocalPart()) && state.peek().equals( OPERATION )) {
                    state.push( INPUT );
                    String messageName = parseQName( attributes.getValue( "message" )).getLocalPart();
                    input = new InputImpl( attributes.getValue( "name" ), definition.getMessage( messageName ));
                    return;
                }
                if( localName.equals( WSDLConstants.OUTPUT.getLocalPart()) && state.peek().equals( OPERATION )) {
                    state.push( OUTPUT );
                    String messageName = parseQName( attributes.getValue( "message" )).getLocalPart();
                    output = new OutputImpl( attributes.getValue( "name" ), definition.getMessage( messageName ));
                    return;
                }
                // Fault
                if( localName.equals( WSDLConstants.FAULT.getLocalPart()) && state.peek().equals( OPERATION )) {
                    state.push( FAULT );
                    String messageName = attributes.getValue( "message" );
                    fault = new FaultImpl(  attributes.getValue( "name" ), definition.getMessage( messageName ));
                    return;
                }
                
                // Binding
                if( localName.equals( WSDLConstants.BINDING.getLocalPart()) && state.peek().equals( DEFINITION )) {
                    state.push( BINDING );
                    QName typeQName = parseQName( attributes.getValue( "type" ));
                    binding = new BindingImpl( attributes.getValue( "name" ));
                    binding.setPortType( definition.getPortType( typeQName.getLocalPart()));
                    return;
                }
                // BindingOperation
                if( localName.equals( WSDLConstants.OPERATION.getLocalPart()) && state.peek().equals( BINDING )) {
                    state.push( BINDING_OPERATION );
                    bindingOperation = new BindingOperationImpl( attributes.getValue( "name" ));
                    return;
                }
                if( localName.equals( WSDLConstants.INPUT.getLocalPart()) && state.peek().equals( BINDING_OPERATION )) {
                    state.push( BINDING_INPUT );
                    bindingInput = new BindingInputImpl( attributes.getValue( "name" ));
                    return;
                }
                if( localName.equals( WSDLConstants.OUTPUT.getLocalPart()) && state.peek().equals( BINDING_OPERATION )) {
                    state.push( BINDING_OUTPUT );
                    bindingOutput = new BindingOutputImpl( attributes.getValue( "name" ));
                    return;
                }
                if( localName.equals( WSDLConstants.FAULT.getLocalPart()) && state.peek().equals( BINDING_OPERATION )) {
                    state.push( BINDING_FAULT );
                    bindingFault = new BindingFaultImpl( attributes.getValue( "name" ));
                    return;
                }
                
                // Service
                if( localName.equals( WSDLConstants.SERVICE.getLocalPart()) && state.peek().equals( DEFINITION )) {
                    state.push( SERVICE );
                    service = new ServiceImpl( attributes.getValue( "name" ));
                    return;
                }
                // Port
                if( localName.equals( WSDLConstants.PORT.getLocalPart()) && state.peek().equals( SERVICE )) {
                    state.push( PORT );
                    Binding b = definition.getBinding( parseQName( attributes.getValue( "binding" )).getLocalPart());
                    // TODO: check for null
                    port = new PortImpl( attributes.getValue( "name" ), b );
                    return;
                }
                // Import
                if( localName.equals( WSDLConstants.IMPORT.getLocalPart()) && state.peek().equals( DEFINITION )) {
                    try {
                        String namespace = attributes.getValue( "namespace" );
                        String location = attributes.getValue( "location" );
                        WSDLParser parser = new WSDLParser();
                        Definition d = parser.parse( location );
                        
                        for( Binding b : d.getBindings().values()) definition.addBinding( b );
                        for( Message m : d.getMessages().values()) definition.addMessage( m );
                        for( Service s : d.getServices().values()) definition.addService( s );
                        for( PortType pt : d.getPortTypes().values()) definition.addPortType( pt );
                        if( d.getSchemaHolder() != null ) {
                            definition.setSchemaHolder( d.getSchemaHolder());
                        }
                        
                        validationResults.addAll( parser.getValidationResults());
                        return;
                    } catch( WSDLException e ) {
                        Exceptions.printStackTrace( e );
                    }
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) 
                throws SAXException 
        {
            if( uri.equals( SOAPConstants.SOAP_URI )) {
                // soap:address
                if( localName.equals( SOAPConstants.ADDRESS.getLocalPart()) & state.peek().equals( PORT )) {
                    port.addExtensibilityElement( soapAddress );
                    return;
                }
                // soap:binding
                if( localName.equals( SOAPConstants.BINDING.getLocalPart()) && state.peek().equals( BINDING )) {
                    binding.addExtensibilityElement( soapBinding );
                    return;
                }
                // soap:operation
                if( localName.equals( SOAPConstants.OPERATION.getLocalPart()) && state.peek().equals( BINDING_OPERATION )) {
                    // TODO: validation
                    bindingOperation.addExtensibilityElement( soapOperation );
                    return;
                }
                // soap:body
                if( localName.equals( SOAPConstants.BODY.getLocalPart())) {
                    if( state.peek().equals( BINDING_INPUT )) {
                        bindingInput.addExtensibilityElement( soapBody );
                    }
                    if( state.peek().equals( BINDING_OUTPUT )) {
                        bindingOutput.addExtensibilityElement( soapBody );
                    }
                    return;
                }
                // soap:header
                if( localName.equals( SOAPConstants.HEADER.getLocalPart())) {
                    if( state.peek().equals( BINDING_INPUT )) {
                        bindingInput.addExtensibilityElement( soapHeader );
                    }
                    if( state.peek().equals( BINDING_OUTPUT )) {
                        bindingOutput.addExtensibilityElement( soapHeader );
                    }
                    return;
                }
                // soap:headerfault
                // TODO: header fault
                // soap:fault
                if( localName.equals( SOAPConstants.FAULT.getLocalPart()) && state.peek().equals( BINDING_FAULT )) {
                    bindingFault.addExtensibilityElement( soapFault );
                    return;
                }                
            }
            
            if( uri.equals( WSDLConstants.WSDL_URI )) {
//                System.err.println("</" + localName + ">" );
                if( localName.equals( WSDLConstants.DEFINITIONS.getLocalPart()) && state.peek().equals( DEFINITION )) {
                    state.pop();
                    return;
                }
                
                // Message
                if( localName.equals( WSDLConstants.MESSAGE.getLocalPart()) && state.peek().equals( MESSAGE )) {
                    state.pop();
                    definition.addMessage( message );
                    return;
                }
                // Message Part
                if( localName.equals( WSDLConstants.PART.getLocalPart()) && state.peek().equals( PART )) {
                    state.pop();
                    message.addPart( part );
                    return;
                }

                // PortType
                if( localName.equals( WSDLConstants.PORT_TYPE.getLocalPart()) && state.peek().equals( PORT_TYPE )) {
                    state.pop();
                    definition.addPortType( portType );
                    return;
                }
                // Operation
                if( localName.equals( WSDLConstants.OPERATION.getLocalPart()) && state.peek().equals( OPERATION )) {
                    state.pop();
                    portType.addOperation( operation );
                    return;
                }
                // Input
                if( localName.equals( WSDLConstants.INPUT.getLocalPart()) && state.peek().equals( INPUT )) {
                    state.pop();
                    operation.setInput( input );
                    return;
                }
                // Output
                if( localName.equals( WSDLConstants.OUTPUT.getLocalPart()) && state.peek().equals( OUTPUT )) {
                    state.pop();
                    operation.setOutput( output );
                    return;
                }
                // Fault
                if( localName.equals( WSDLConstants.FAULT.getLocalPart()) && state.peek().equals( FAULT )) {
                    state.pop();
                    operation.addFault( fault );
                    return;
                }
                
                // Binding
                if( localName.equals( WSDLConstants.BINDING.getLocalPart()) && state.peek().equals( BINDING )) {
                    state.pop();
                    definition.addBinding( binding );
                    return;
                }
                // BindingOperation
                if( localName.equals( WSDLConstants.OPERATION.getLocalPart()) && state.peek().equals( BINDING_OPERATION )) {
                    state.pop();
                    binding.addBindingOperation( bindingOperation );
                    return;
                }
                // BindingInput
                if( localName.equals( WSDLConstants.INPUT.getLocalPart()) && state.peek().equals( BINDING_INPUT )) {
                    state.pop();
                    bindingOperation.setBindingInput( bindingInput );
                    return;
                }
                // BindingOutput
                if( localName.equals( WSDLConstants.OUTPUT.getLocalPart()) && state.peek().equals( BINDING_OUTPUT )) {
                    state.pop();
                    bindingOperation.setBindingOutput( bindingOutput );
                    return;
                }
                // BindingFault
                if( localName.equals( WSDLConstants.FAULT.getLocalPart()) && state.peek().equals( BINDING_FAULT )) {
                    state.pop();
                    bindingOperation.addBindingFault( bindingFault );
                    return;
                }
                
                // Service
                if( localName.equals( WSDLConstants.SERVICE.getLocalPart()) && state.peek().equals( SERVICE )) {
                    state.pop();
                    definition.addService( service );
                    return;
                }
                // Port
                if( localName.equals( WSDLConstants.PORT.getLocalPart()) && state.peek().equals( PORT )) {
                    state.pop();
                    service.addPort( port );
                    return;
                }
                
                if( localName.equals( WSDLConstants.DOCUMENTATION.getLocalPart())) {
                    if( state.peek().equals( OPERATION )) {
                        operation.setDocumentation( tagString );
                    }
                    return;
                }
            }
        }
        
        public QName parseQName( String qName ) {
            if( qName == null ) return null;
            int colonPos = qName.indexOf( ':' );
            if( colonPos > 0 ) {
                String prefix = qName.substring( 0, colonPos );
                String uri = prefixMapping.get( prefix );
                return new QName( uri, qName.substring( colonPos + 1 ), prefix );
            }
            return new QName( targetNamespace, qName );
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            tagString += new String( ch, start, length );
        }
        
//    }
}
