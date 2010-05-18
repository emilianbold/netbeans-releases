/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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

