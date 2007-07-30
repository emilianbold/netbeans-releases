/*
 * SchemaParser.java
 *
 * Created on October 9, 2006, 4:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.schema;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
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
import org.netbeans.modules.e2e.api.schema.Element;
import org.netbeans.modules.e2e.api.schema.RepeatableSchemaConstruct;
import org.netbeans.modules.e2e.api.schema.SchemaConstruct;
import org.netbeans.modules.e2e.api.schema.SchemaException;
import org.netbeans.modules.e2e.api.schema.SchemaHolder;
import org.netbeans.modules.e2e.api.schema.Type;
import org.netbeans.modules.e2e.api.wsdl.wsdl2java.WSDL2Java;
import org.netbeans.modules.e2e.wsdl.WSDLConstants;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Michal Skvor
 */
public class SchemaParser extends DefaultHandler {
    
    private Stack<String> state = new Stack();
    private Stack<SchemaConstruct> schemaConstructs = new Stack();

    private SchemaHolder schemaHolder;
    
    private String targetNamespace;
    private boolean elementFormDefault = false;
    private Stack<String> targetNamespaceStack = new Stack();
    private Map<String, String> prefixMapping = new HashMap();
    private Locator locator;
        
    /* SCHEMA constants */
    private static final String SCHEMA          = "schema";
    
    private static final String ELEMENT         = "element";
    private static final String COMPLEX_TYPE    = "complexType";
    private static final String SEQUENCE        = "sequence";
    private static final String SIMPLE_TYPE     = "simpleType";
    private static final String RESTRICTION     = "restriction";
    private static final String ENUMERATION     = "enumeration";
    
    private boolean parseWSDLTags = true;
    
    private List<WSDL2Java.ValidationResult> validationResults;
    
    public SchemaParser() {
        schemaHolder = new SchemaHolderImpl();
        validationResults = new ArrayList();
        
        addPrimitiveTypes();
    }
    
    public SchemaParser( boolean parseWSDLTags ) {
        this();
        this.parseWSDLTags = parseWSDLTags;
        
        state.push( WSDLConstants.TYPES.getLocalPart());
    }

    private void addPrimitiveTypes() {
        Type string_type = new Type( SchemaConstants.TYPE_STRING, Type.FLAVOR_PRIMITIVE );
        string_type.setJavaTypeName( "java.lang.String" );
        schemaHolder.addSchemaType( string_type );
        Type int_type = new Type( SchemaConstants.TYPE_INT, Type.FLAVOR_PRIMITIVE );
        int_type.setJavaTypeName( "int" );
        schemaHolder.addSchemaType( int_type );
        Type short_type = new Type( SchemaConstants.TYPE_SHORT, Type.FLAVOR_PRIMITIVE );
        short_type.setJavaTypeName( "short" );
        schemaHolder.addSchemaType( short_type );
        Type long_type = new Type( SchemaConstants.TYPE_LONG, Type.FLAVOR_PRIMITIVE );
        long_type.setJavaTypeName( "long" );
        schemaHolder.addSchemaType( long_type );
        Type boolean_type = new Type( SchemaConstants.TYPE_BOOLEAN, Type.FLAVOR_PRIMITIVE );
        boolean_type.setJavaTypeName( "boolean" );
        schemaHolder.addSchemaType( boolean_type );
        Type float_type = new Type( SchemaConstants.TYPE_FLOAT, Type.FLAVOR_PRIMITIVE );
        float_type.setJavaTypeName( "float" );
        schemaHolder.addSchemaType( float_type );
        Type double_type = new Type( SchemaConstants.TYPE_DOUBLE, Type.FLAVOR_PRIMITIVE );
        double_type.setJavaTypeName( "double" );
        schemaHolder.addSchemaType( double_type );
        Type byte_type = new Type( SchemaConstants.TYPE_BYTE, Type.FLAVOR_PRIMITIVE );
        byte_type.setJavaTypeName( "byte" );
        schemaHolder.addSchemaType( byte_type );
        
        Type base64binary_type = new Type( SchemaConstants.TYPE_BASE64_BINARY, Type.FLAVOR_PRIMITIVE );
        base64binary_type.setJavaTypeName( "byte[]" );
        schemaHolder.addSchemaType( base64binary_type );
        Type hexBinary_type = new Type( SchemaConstants.TYPE_HEX_BINARY, Type.FLAVOR_PRIMITIVE );
        hexBinary_type.setJavaTypeName( "byte[]" );
        schemaHolder.addSchemaType( hexBinary_type );
        Type qname_type = new Type( SchemaConstants.TYPE_QNAME, Type.FLAVOR_PRIMITIVE );
        qname_type.setJavaTypeName( "javax.xml.namespace.QName" );
        schemaHolder.addSchemaType( qname_type );
    }
    
    public SchemaHolder getSchemaHolder() {
        return schemaHolder;
    }
    
    public List<WSDL2Java.ValidationResult> getValidationResults() {
        return Collections.unmodifiableList( validationResults );
    }
    
    public void parse( InputStream is ) throws SchemaException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            spf.setNamespaceAware( true );
            spf.setValidating( false );
            
            SAXParser parser = spf.newSAXParser();
            
            parser.parse( is, this );
        } catch( SAXException e ) {
            if( e.getException() instanceof SchemaException ) {
                throw new SchemaException( e.getCause());
            }
        } catch( ParserConfigurationException e ) {
            e.printStackTrace();
        } catch( IOException e ) {
            e.printStackTrace();
        }        
    }
    
    public void parseLocation( String uri ) throws SchemaException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            spf.setNamespaceAware( true );
            spf.setValidating( false );
            
            SAXParser parser = spf.newSAXParser();
            
            parser.parse( uri, this );
        } catch( SAXException e ) {
            if( e.getException() instanceof SchemaException ) {
                validationResults.add( new WSDL2Java.ValidationResult( 
                        WSDL2Java.ValidationResult.ErrorLevel.FATAL, "Error during parsing of the schema file." ));
//                throw new SchemaException( e.getCause());
            }
        } catch( ParserConfigurationException e ) {
            e.printStackTrace();
        } catch( FileNotFoundException e ) {
            validationResults.add( new WSDL2Java.ValidationResult(
                WSDL2Java.ValidationResult.ErrorLevel.FATAL, "Schema " + uri + " cannot be located." ));
//            throw new SchemaException( "");
        } catch( ConnectException e ) {
            validationResults.add( new WSDL2Java.ValidationResult(
                WSDL2Java.ValidationResult.ErrorLevel.FATAL, "Connection problem. Cannot download schema from " + uri + " location." ));
        } catch( IOException e ) {
            validationResults.add( new WSDL2Java.ValidationResult(
                WSDL2Java.ValidationResult.ErrorLevel.FATAL, "Connection problem. Cannot download schema from " + uri + " location." ));
        } catch( IllegalArgumentException e ) {
            validationResults.add( new WSDL2Java.ValidationResult(
                WSDL2Java.ValidationResult.ErrorLevel.FATAL, e.getLocalizedMessage()));
        } catch( Exception e ) {
            validationResults.add( new WSDL2Java.ValidationResult(
                WSDL2Java.ValidationResult.ErrorLevel.FATAL, e.getLocalizedMessage()));
        }
    }

    public void setDocumentLocator( Locator locator ) {
        this.locator = locator;
    }
    
    public void startPrefixMapping( String prefix, String uri ) throws SAXException {
        prefixMapping.put( prefix, uri );
    }        

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        QName qname = new QName( uri, localName );
        
        if( uri.equals( WSDLConstants.WSDL_URI )) {
            if( WSDLConstants.DEFINITIONS.getLocalPart().equals( localName )) {
                state.push( WSDLConstants.DEFINITIONS.getLocalPart());
                return;
            }
            if( WSDLConstants.TYPES.getLocalPart().equals( localName )) {
                state.push( WSDLConstants.TYPES.getLocalPart());
                return;
            }
        }
        
        if( uri.equals( SchemaConstants.SCHEMA_URI ) && state.size() > 0 ) {
            //System.err.println("<schema:" + localName + ">" );
            
            if( localName.equals( "import" )) {
                String namespace = attributes.getValue( "namespace" );
                String schemaLocation = attributes.getValue( "schemaLocation" );
//                System.err.println("<import namespace='" + namespace + "' schemaLocation='" + schemaLocation + "'/>" );
                SchemaParser sp = new SchemaParser( false );
                try {
                    sp.parseLocation( schemaLocation );
                } catch( SchemaException ex ) {
                    ex.printStackTrace();
                }
                schemaHolder.importSchema( sp.getSchemaHolder());
                validationResults.addAll( sp.getValidationResults());
            }
            
            // schema
            if( localName.equalsIgnoreCase( SchemaConstants.SCHEMA.getLocalPart())) {
                state.push( SCHEMA );
//                System.err.println("<schema>");
                targetNamespace = targetNamespaceStack.push( attributes.getValue( "targetNamespace" ));
                if( "qualified".equals( attributes.getValue( "elementFormDefault" ))) {
                    elementFormDefault = true;
                }
                return;
            }
            
            // element
            if( localName.equalsIgnoreCase( SchemaConstants.ELEMENT.getLocalPart())) {
                state.push( ELEMENT );
                // TODO: check name
                QName qn = null;
                // Check for ref attribute
                if( attributes.getValue( "ref" ) != null ) {
                    validationResults.add( new WSDL2Java.ValidationResult(
                        WSDL2Java.ValidationResult.ErrorLevel.FATAL, "Reference in element is not supported by this version of stub compiler." ));
                } else if( schemaConstructs.isEmpty() || elementFormDefault ) {
                    qn = new QName( targetNamespace, attributes.getValue( "name" ));
                } else {
                    qn = new QName( "", attributes.getValue( "name" ));
                }
                Element e = new Element( qn );
                if( schemaConstructs.isEmpty() || elementFormDefault ) {
                    e.setTargetNamespace( targetNamespace );
                }
                parseSchemaConstruct( e, attributes );
                String nillable = attributes.getValue( "nillable" );
                if( nillable != null && "true".equals( nillable )) {
                    e.setNillable( true );
                }
                String typeName = attributes.getValue( "type" );
                if( typeName != null ) {
                    QName typeQName = parseQName( typeName );
                    Type type = schemaHolder.getSchemaType( typeQName );
                    if( type == null ) {
                        type = new Type( typeQName );
                        schemaHolder.addSchemaType( type );
                    }
                    e.setType( type );
                }
                if( !schemaConstructs.isEmpty()) {
                    SchemaConstruct.ConstructType t = schemaConstructs.peek().getConstructType();
                    if( SchemaConstruct.ConstructType.TYPE.equals( schemaConstructs.peek().getConstructType())) {
                        ((Type) schemaConstructs.peek()).addSubconstruct( e );
                    }
                }
                
                schemaHolder.addSchemaElement( e );
                schemaConstructs.push( e );
                return;
            }
            // complexType
            if( localName.equalsIgnoreCase( SchemaConstants.COMPLEX_TYPE.getLocalPart())) {
                state.push( COMPLEX_TYPE );
                String name = attributes.getValue( "name" );
                QName qn = null;
                Type type = null;
                if( name != null ) {
                    qn = new QName( targetNamespace, name );
                    type = schemaHolder.getSchemaType( qn );
                    if( type == null ) {
                        type  = new Type( qn );
                        schemaHolder.addSchemaType( type );
                    } else {
                        type.setFlavor( Type.FLAVOR_SEQUENCE );
                    }
                }
                if( type == null ) {
                    type = new Type();
                    type.setFlavor( Type.FLAVOR_SEQUENCE );
                }
                if( !schemaConstructs.isEmpty() && SchemaConstruct.ConstructType.ELEMENT.equals( schemaConstructs.peek().getConstructType())) {
                    Element e = (Element)schemaConstructs.peek();
                    e.setType( type );
                } else {
                    schemaHolder.addSchemaType( type );
                }
                schemaConstructs.push( type );
                return;
            }
            // Sequence
            if( localName.equalsIgnoreCase( SchemaConstants.SEQUENCE.getLocalPart())) {
                state.push( SEQUENCE );
                SchemaConstruct sc = schemaConstructs.peek();
                if( sc instanceof Type ) {
                    Type type = (Type)sc;
                    type.setFlavor( Type.FLAVOR_SEQUENCE );
                }
                return;
            }
            // Simple Type
            if( localName.equals( SchemaConstants.SIMPLE_TYPE.getLocalPart())) {
//                state.push( SIMPLE_TYPE );
//                String name = attributes.getValue( "name" );
//                QName qn = null;
//                if( name != null ) {
//                    qn = new QName( targetNamespace, name );                    
//                }
//                // add as type
//                type = new Type( qn );
//                schemaHolder.addSchemaType( type );
                String typeName = attributes.getValue( "name" );
                if( typeName == null ) typeName = "";
                
                validationResults.add( new WSDL2Java.ValidationResult(
                        WSDL2Java.ValidationResult.ErrorLevel.FATAL, "Simple type is not supported by JSR-172 - " + typeName ));
//                throw new SAXException( "", new SchemaException( "Invalid type 'simple type'" )); 
            }
//            // Restriction
//            if( localName.equals( SchemaConstants.RESTRICTION.getLocalPart())) {
//                state.push( RESTRICTION );
//            }
//            if( localName.equals( SchemaConstants.ENUMERATION.getLocalPart())) {
//                state.push( ENUMERATION );
//            }
            // Enumeration
            /* Unsupported tags */
//            if( localName.equals( SchemaConstants.QNAME_RESTRICTION.getLocalPart())) {
//                throw new SAXException( "", new SchemaException( "restriction is not supported" ));
//            }
            // ALL 
            if( localName.equalsIgnoreCase( SchemaConstants.ALL.getLocalPart()) && state.peek().equals( COMPLEX_TYPE )) {
                validationResults.add( new WSDL2Java.ValidationResult(
                        WSDL2Java.ValidationResult.ErrorLevel.FATAL, "'all' element in complex-type element is not supported by JSR-172." ));
            }
            if( localName.equalsIgnoreCase( SchemaConstants.CHOICE.getLocalPart()) && state.peek().equals( COMPLEX_TYPE )) {
                validationResults.add( new WSDL2Java.ValidationResult(
                        WSDL2Java.ValidationResult.ErrorLevel.FATAL, "'choice' element in complex-type element is not supported by JSR-172." ));
            }
        }        
    }
    
    public void endElement(String uri, String localName, String qName) throws SAXException {
        
        if( uri.equals( WSDLConstants.WSDL_URI )) {
            if( WSDLConstants.DEFINITIONS.getLocalPart().equals( localName )) {
                state.pop();
                return;
            }
            if( WSDLConstants.TYPES.getLocalPart().equals( localName )) {
                state.pop();
                return;
            }
        }
        
        if( uri.equalsIgnoreCase( SchemaConstants.SCHEMA_URI ) && state.size() > 0 ) {
            // element
            if( SchemaConstants.ELEMENT.getLocalPart().equals( localName )) {
                schemaConstructs.pop();
                if( !ELEMENT.equals( state.pop())) {
                    throw new SAXException( "", new SchemaException( "Invalid end tag for 'element'." ));
                }
                if( SCHEMA.equals( state.peek())) {
//                    System.err.println(" - top element ");
                } else {
                    if( !SEQUENCE.equals( state.peek())) {
                        throw new SAXException( "", new SchemaException( "Invalid super tag for 'element'." )); 
                    }
                }
            }
            // complexType
            if( SchemaConstants.COMPLEX_TYPE.getLocalPart().equals( localName )) {
                schemaConstructs.pop();
                if( !COMPLEX_TYPE.equals( state.pop())) {
                    throw new SAXException( "", new SchemaException( "Invalid end tag for 'complexType'." ));
                }
                
            }
            if( SchemaConstants.SEQUENCE.getLocalPart().equals( localName )) {
                if( !SEQUENCE.equals( state.pop())) {
                    throw new SAXException( "", new SchemaException( "Invalid end tag for 'sequence'." ));
                }
                if( !COMPLEX_TYPE.equals( state.peek())) {
                    throw new SAXException( "", new SchemaException( "Invalid 'sequence' tag position." ));
                } 
            }
        }
    }
    
    
    private void parseSchemaConstruct( RepeatableSchemaConstruct psc, Attributes attributes ) {
        String minOccurs = attributes.getValue( "minOccurs" );
        if( minOccurs != null ) {
            int value = Integer.parseInt( minOccurs );
            psc.setMinOccurs( value );
        } else {
            psc.setMinOccurs( 1 );
        }
        
        String maxOccurs = attributes.getValue( "maxOccurs" );
        if( maxOccurs != null ) {
            if( "unbounded".equals( maxOccurs )) {
                psc.setMaxOccurs( RepeatableSchemaConstruct.UNBOUNDED );
            } else {
                int value = Integer.parseInt( maxOccurs );
                psc.setMaxOccurs( value );
            }
        } else {
            psc.setMaxOccurs( 1 );
        }
    }
    
    private QName parseQName( String qName ) {
        if( qName == null ) return null;
        int colonPos = qName.indexOf( ':' );
        if( colonPos > 0 ) {
            String prefix = qName.substring( 0, colonPos );
            String uri = prefixMapping.get( prefix );
            return new QName( uri, qName.substring( colonPos + 1 ), prefix );
        }
        return new QName( targetNamespaceStack.peek(), qName );
    }    
}
