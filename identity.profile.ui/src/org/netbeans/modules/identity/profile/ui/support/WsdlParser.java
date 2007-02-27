/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.identity.profile.ui.support;

/**
 * Custom parser for a wsdl file.
 *
 * Created on April 18, 2006, 1:29 PM
 *
 * @author Srividhya Narayanan
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

public class WsdlParser {
    public static final String TNS = "targetNamespace"; // NOI18N
    public static final String ADDRESS = "soapAddress"; // NOI18N
    public static final String PORT = "port"; // NOI18N
            
    /** Creates a new instance of XMLParser */
    public WsdlParser() {
    }
    
    public static WsdlData parseWSDLFile(File xmlFile, String svc) 
        throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(xmlFile));
        InputSource is = new InputSource(br);
        try {
            return parseWSDLFile(is, svc);
        } finally {
            br.close();
        }
    }
	
    public static WsdlData parseWSDLFile(InputSource xmlFile, String svc) 
        throws IOException {
        WsdlData wsdlData = null;
        try {
            SAXParserFactory saxFactory = SAXParserFactory.newInstance();
            saxFactory.setNamespaceAware(true);
            SAXParser parser = saxFactory.newSAXParser();
            WsdlHandler handler = new WsdlHandler(svc);
            parser.parse(xmlFile, handler);
            wsdlData = handler.getWsdlData();
        } catch (ParserConfigurationException excp) {
            IOException ioe = new IOException();
			ioe.initCause(excp);
            throw ioe;
        } catch (SAXException excp) {
			IOException ioe = new IOException();
			ioe.initCause(excp);
            throw ioe;
        }
        return wsdlData;
    }

    public static ArrayList getWsdlSvcNames(File xmlFile) 
        throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(xmlFile));
        InputSource is = new InputSource(br);
        try {
            return getWsdlSvcNames(is);
        } finally {
            br.close();
        }
    }
	
    public static ArrayList getWsdlSvcNames(InputSource xmlFile) 
        throws IOException {
        try {
            SAXParserFactory saxFactory = SAXParserFactory.newInstance();
            saxFactory.setNamespaceAware(true);
            SAXParser parser = saxFactory.newSAXParser();
            SvcNameHandler handler = new SvcNameHandler();
            parser.parse(xmlFile, handler);
            return handler.getWsdlSvcNames();
        } catch (ParserConfigurationException excp) {
            IOException ioe = new IOException();
			ioe.initCause(excp);
            throw ioe;
        } catch (SAXException excp) {
			IOException ioe = new IOException();
			ioe.initCause(excp);
            throw ioe;
        }
    }

    private static class WsdlHandler extends DefaultHandler {
        private static final String W3C_WSDL_SCHEMA = "http://schemas.xmlsoap.org/wsdl"; // NOI18N
        private static final String W3C_WSDL_SCHEMA_SLASH = "http://schemas.xmlsoap.org/wsdl/"; // NOI18N
        
        private String svcName;
        WsdlData wsdlData = new WsdlData();
        boolean found = false;
        
        public WsdlHandler(String svc) {
            super();
            svcName = svc;
        }
        
        public WsdlData getWsdlData() {
            return wsdlData;
        }
        
        public void startDocument() {
        }
        
        public void endDocument() {
        }
        
        public void startElement(String uri, String localName, 
                String qName, Attributes attributes) {
            if(W3C_WSDL_SCHEMA.equals(uri) || W3C_WSDL_SCHEMA_SLASH.equals(uri)) {
                if (localName.equals("definitions")) { // NOI18N
                    wsdlData.setTargetNameSpace(attributes.getValue("targetNamespace")); // NOI18N
                }
                if (localName.equals("service")) { // NOI18N
                    if (svcName.equalsIgnoreCase(attributes.getValue("name"))) { // NOI18N
                        found = true;
                    }
                }
                if (found && localName.equals("port")) { // NOI18N
                    wsdlData.setPort(attributes.getValue("name")); // NOI18N
                }
            }
            if (found && localName.equals("address")) { // NOI18N
                wsdlData.setAddress(attributes.getValue("location")); // NOI18N
                found = false;
            }
        }
        
        public void endElement(String uri, String localName, String qName) {
        }
    }
    
    private static class SvcNameHandler extends DefaultHandler {
        private static final String W3C_WSDL_SCHEMA = "http://schemas.xmlsoap.org/wsdl"; // NOI18N
        private static final String W3C_WSDL_SCHEMA_SLASH = "http://schemas.xmlsoap.org/wsdl/"; // NOI18N
        
        ArrayList wsdlSvcNames = new ArrayList();
      
        public SvcNameHandler() {
            super();
        }
        
        public ArrayList getWsdlSvcNames() {
            return wsdlSvcNames;
        }
        
        public void startDocument() {
        }
        
        public void endDocument() {
        }
        
        public void startElement(String uri, String localName, 
                String qName, Attributes attributes) {
            if(W3C_WSDL_SCHEMA.equals(uri) || W3C_WSDL_SCHEMA_SLASH.equals(uri)) {
                if (localName.equals("service")) { // NOI18N
                    String name = attributes.getValue("name"); // NOI18N
                    wsdlSvcNames.add(name);
                }
            }
        }
        
        public void endElement(String uri, String localName, String qName) {
        }
    }
}

