/*
 * WSDLConstants.java
 *
 * Created on September 27, 2006, 10:25 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.wsdl;

import javax.xml.namespace.QName;

/**
 *
 * @author Michal Skvor
 */
public final class WSDLConstants {
        
    public static final String WSDL_URI    = "http://schemas.xmlsoap.org/wsdl/";
    
    public static final QName TYPES         = new QName( WSDL_URI, "types" );
    public static final QName DEFINITIONS   = new QName( WSDL_URI, "definitions" );
    public static final QName MESSAGE       = new QName( WSDL_URI, "message" );
    public static final QName PART          = new QName( WSDL_URI, "part" );
    public static final QName SERVICE       = new QName( WSDL_URI, "service" );
    public static final QName PORT          = new QName( WSDL_URI, "port" );
    public static final QName PORT_TYPE     = new QName( WSDL_URI, "portType" );
    public static final QName OPERATION     = new QName( WSDL_URI, "operation" ); 
    public static final QName BINDING       = new QName( WSDL_URI, "binding" );
    
    public static final QName INPUT         = new QName( WSDL_URI, "input" );
    public static final QName OUTPUT        = new QName( WSDL_URI, "output" );
    public static final QName FAULT         = new QName( WSDL_URI, "fault" );

    public static final QName IMPORT        = new QName( WSDL_URI, "import" );
    
    public static final QName DOCUMENTATION = new QName( WSDL_URI, "documentation" );
}
