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

package org.netbeans.modules.e2e.schema;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
import org.openide.util.Exceptions;

/**
 *
 */
public class SchemaParser extends DefaultHandler {
    
    private Stack<String> state = new Stack<String>();
    private Stack<SchemaConstruct> schemaConstructs = new Stack<SchemaConstruct>();

    private SchemaHolder schemaHolder;
    
    private String targetNamespace;
    private boolean elementFormDefault = false;
    private Stack<String> targetNamespaceStack = new Stack<String>();
    private Map<String, String> prefixMapping = new HashMap<String, String>();
    private Locator locator;
    
    private URL schemaURL;
    
    /* SCHEMA constants */
    private static final String SCHEMA          = "schema";
    
    private static final String ELEMENT         = "element";
    private static final String COMPLEX_TYPE    = "complexType";
    private static final String COMPLEX_CONTENT = "complexContent";
    private static final String EXTENSION       = "extension";
    private static final String SEQUENCE        = "sequence";
    private static final String SIMPLE_TYPE     = "simpleType";
    private static final String RESTRICTION     = "restriction";
    private static final String ENUMERATION     = "enumeration";
    
    private boolean parseWSDLTags = true;
    
    private List<WSDL2Java.ValidationResult> validationResults;
    
    public SchemaParser() {
        schemaHolder = new SchemaHolderImpl();
        validationResults = new ArrayList<WSDL2Java.ValidationResult>();
        
        addPrimitiveTypes();
    }
    
    public SchemaParser( boolean parseWSDLTags ) {
        this();
        this.parseWSDLTags = parseWSDLTags;
        
        state.push( WSDLConstants.TYPES.getLocalPart());
    }

    private void addPrimitiveTypes() {
        Type string_type = new Type( SchemaConstants.TYPE_STRING, Type.FLAVOR_PRIMITIVE );
        string_type.setJavaTypeName( "String" );
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
    
    public void parseLocation( URL url, String targetNamespace ) throws SchemaException {
        this.targetNamespace = targetNamespace;
        
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            spf.setNamespaceAware( true );
            spf.setValidating( false );
            
            SAXParser parser = spf.newSAXParser();
            
            schemaURL = url;
            parser.parse( url.toURI().toString() , this );
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
                WSDL2Java.ValidationResult.ErrorLevel.FATAL, "Schema " + url + " cannot be located." ));
//            throw new SchemaException( "");
        } catch( ConnectException e ) {
            validationResults.add( new WSDL2Java.ValidationResult(
                WSDL2Java.ValidationResult.ErrorLevel.FATAL, "Connection problem. Cannot download schema from " + url + " location." ));
        } catch( IOException e ) {
            validationResults.add( new WSDL2Java.ValidationResult(
                WSDL2Java.ValidationResult.ErrorLevel.FATAL, "Connection problem. Cannot download schema from " + url + " location." ));
        } catch( IllegalArgumentException e ) {
            validationResults.add( new WSDL2Java.ValidationResult(
                WSDL2Java.ValidationResult.ErrorLevel.FATAL, e.getLocalizedMessage()));
        } catch( Exception e ) {
            validationResults.add( new WSDL2Java.ValidationResult(
                WSDL2Java.ValidationResult.ErrorLevel.FATAL, e.getLocalizedMessage()));
        }
    }

    @Override
    public void setDocumentLocator( Locator locator ) {
        this.locator = locator;
    }
    
    @Override
    public void startPrefixMapping( String prefix, String uri ) throws SAXException {
        prefixMapping.put( prefix, uri );
    }        

    @Override
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
                if( schemaLocation == null ) return;
                SchemaParser sp = new SchemaParser( false );
                    
                try {
                    URI u = schemaURL.toURI();
                    URI sl = u.resolve( schemaLocation );
                    sp.parseLocation( sl.toURL(), namespace);
                } catch( SchemaException ex ) {
                    Exceptions.printStackTrace( ex );
                } catch( URISyntaxException ex ) {
                    Exceptions.printStackTrace( ex );
                } catch( MalformedURLException e ){
                    Exceptions.printStackTrace( e );
                }
                schemaHolder.importSchema( sp.getSchemaHolder());
                validationResults.addAll( sp.getValidationResults());
            }
            
            // schema
            if( localName.equalsIgnoreCase( SchemaConstants.SCHEMA.getLocalPart())) {
                state.push( SCHEMA );
//                System.err.println("<schema>");
                targetNamespace = targetNamespaceStack.push( 
                        attributes.getValue( "targetNamespace" ) != null ? attributes.getValue( "targetNamespace" ) : targetNamespace);
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
//            // Complex content
            if( localName.equalsIgnoreCase( SchemaConstants.COMPLEX_CONTENT.getLocalPart()) && state.peek().equals( COMPLEX_TYPE )) {
                state.push( COMPLEX_CONTENT );
            }
            // Extension
            if( localName.equalsIgnoreCase( SchemaConstants.EXTENSION.getLocalPart()) && state.peek().equals( COMPLEX_CONTENT )) {
                state.push( EXTENSION );
                String name = attributes.getValue( "base" );
                QName qn = null;
                Type type = null;
                if( name != null ) {
                    qn = parseQName( name );
                    type = schemaHolder.getSchemaType( qn );
                    if( type == null ) {
                        type = new Type( qn );
                        schemaHolder.addSchemaType( type );
                    }
                    SchemaConstruct sc = schemaConstructs.peek();
                    sc.setParent( type );
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
    
    @Override
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
                if( !COMPLEX_TYPE.equals( state.peek()) && !EXTENSION.equals( state.peek())) {
                    throw new SAXException( "", new SchemaException( "Invalid 'sequence' tag position." ));
                } 
            }
            if( SchemaConstants.EXTENSION.getLocalPart().equals( localName )) {
                state.pop();
            }
            if( SchemaConstants.COMPLEX_CONTENT.getLocalPart().equals( localName )) {
                state.pop();
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
