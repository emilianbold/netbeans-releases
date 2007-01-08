/*
 * SOAPConstants.java
 *
 * Created on September 27, 2006, 10:46 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.wsdl.extensions.soap;

import javax.xml.namespace.QName;

/**
 *
 * @author Michal Skvor
 */
public final class SOAPConstants {

    public static final String SOAP_URI = "http://schemas.xmlsoap.org/wsdl/soap/";
    
    public static final String STYLE_RPC        = "rpc";
    public static final String STYLE_DOCUMENT   = "document";

    public static final String USE_LITERAL      = "literal";
    public static final String USE_ENCODED      = "encoded";
    
    public static final QName ADDRESS       = new QName( SOAP_URI, "address" );
    public static final QName BINDING       = new QName( SOAP_URI, "binding" );
    public static final QName OPERATION     = new QName( SOAP_URI, "operation" );
    
    public static final QName BODY          = new QName( SOAP_URI, "body" );
    public static final QName HEADER        = new QName( SOAP_URI, "header" );
    public static final QName HEADER_FAULT  = new QName( SOAP_URI, "headerfault" );
    public static final QName FAULT         = new QName( SOAP_URI, "fault" );
}
