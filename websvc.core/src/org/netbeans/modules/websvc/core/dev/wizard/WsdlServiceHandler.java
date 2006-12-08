/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.core.dev.wizard;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** ConteHandler that gives information of the first service and port
 *
 * @author mkuchtiak
 */
public class WsdlServiceHandler extends DefaultHandler{
    
    public static final String WSDL_SOAP_URI = "http://schemas.xmlsoap.org/wsdl/"; //NOI18N
    
    private boolean insideService;
    private String serviceName, portName;
    
    public static WsdlServiceHandler parse(String wsdlUrl) throws ParserConfigurationException, SAXException, IOException {
        WsdlServiceHandler handler = new WsdlServiceHandler();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(wsdlUrl, handler);
        return handler;
    }
    
    /** Creates a new instance of WsdlWrapperHandler */
    private WsdlServiceHandler() {
    }    
    
    public void startElement(String uri, String localName, String qname, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException {
        if (WSDL_SOAP_URI.equals(uri) && "service".equals(localName)) { // NOI18N
            insideService=true;
            if (serviceName==null) {
                serviceName = attributes.getValue("name");// NOI18N
            }
        } else if("port".equals(localName) && insideService) { // NOI18N
            if (portName==null) {
                portName = attributes.getValue("name"); // NOI18N
            }
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (WSDL_SOAP_URI.equals(uri) && "service".equals(localName)) {
            insideService=false;
        }
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public String getPortName() {
        return portName;
    }
}
