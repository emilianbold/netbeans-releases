/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.reglib;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

// For write operation
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * XML Support Class for Product Registration.
 */
class StatusDocument {

    private static final String STATUS_DATA_SCHEMA =
            "/org/netbeans/modules/reglib/resources/status.xsd";
    private static final String STATUS_DATA_VERSION = "1.0";
    final static String ST_NODE_REGISTRATION_STATUS = "registration_status";
    final static String ST_ATTR_REGISTRATION_STATUS_VERSION = "version";
    final static String ST_NODE_STATUS = "status";
    final static String ST_NODE_TIMESTAMP = "timestamp";
    final static String ST_NODE_DELAY = "delay";
    
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.reglib.StatusDocument"); // NOI18N
    
    static StatusData load(InputStream in) throws IOException {
        Document doc = initializeDocument(in);
        
        Element root = doc.getDocumentElement();
        if (!root.getNodeName().equals(ST_NODE_REGISTRATION_STATUS)) {
            throw new IllegalArgumentException("Not a " + ST_NODE_REGISTRATION_STATUS +
                    " node \"" + root.getNodeName() + "\"");
        }
        
        String val, stValue;
        val = getTextValue(root, ST_NODE_STATUS);
        //Validate
        if (StatusData.STATUS_LATER.equals(val)) {
            stValue = StatusData.STATUS_LATER;
        } else if (StatusData.STATUS_NEVER.equals(val)) {
            stValue = StatusData.STATUS_NEVER;
        } else if (StatusData.STATUS_REGISTERED.equals(val)) {
            stValue = StatusData.STATUS_REGISTERED;
        } else {
            stValue = StatusData.STATUS_UNKNOWN;
        }
        
        int delay = StatusData.DEFAULT_DELAY;
        val = getTextValue(root, ST_NODE_DELAY);
        try {
            delay = Integer.parseInt(val);
        } catch (NumberFormatException exc) {
            LOG.log(Level.INFO,"Error: Cannot parse delay value:" + val,exc);
        }
        
        StatusData sd = new StatusData(stValue,delay);
        
        Date tsValue = Util.parseTimestamp(getTextValue(root, ST_NODE_TIMESTAMP));
        sd.setTimestamp(tsValue);
        
        return sd;
    }
    
    static void store(OutputStream os, StatusData status) throws IOException {
        // create a new document with the root node
        Document document = initializeDocument();
        
        addStatusNode(document,status);
        transform(document, os);
    }
    
    private static String getTextValue(Element e, String tagName) {
        String value = "";
        NodeList nl = e.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
            Node node = el.getFirstChild();
            if (node != null) {
                value = node.getNodeValue();
            }
        }
        return value;
    }
    
    private static void addStatusNode (Document document, StatusData status) {        
        Element r = document.getDocumentElement();
                
        Element s = document.createElement(ST_NODE_STATUS);
        s.appendChild(document.createTextNode(status.getStatus()));
        r.appendChild(s);        
        
        Element t = document.createElement(ST_NODE_TIMESTAMP);
        t.appendChild(document.createTextNode(Util.formatTimestamp(status.getTimestamp())));
        r.appendChild(t);
        
        t = document.createElement(ST_NODE_DELAY);
        t.appendChild(document.createTextNode(Integer.toString(status.getDelay())));
        r.appendChild(t);
    }

    // initialize a document from an input stream 
    private static Document initializeDocument(InputStream in) throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            // XML schema for validation
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            URL xsdUrl = StatusDocument.class.getResource(STATUS_DATA_SCHEMA);
            Schema schema = sf.newSchema(xsdUrl);
            Validator validator = schema.newValidator();

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(in));
            validator.validate(new DOMSource(doc));
            return doc;
        } catch (SAXException sxe) {
            IllegalArgumentException e = new IllegalArgumentException("Error generated in parsing");
            e.initCause(sxe);
            throw e;
        } catch (ParserConfigurationException pce) {
            // Parser with specific options can't be built
            // should not reach here
            InternalError x = new InternalError("Error in creating the new document");
            x.initCause(pce);
            throw x;
        }
    }
    
    // initialize a new document for the registration data
    private static Document initializeDocument() throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            
            // initialize the document with the registration_data root
            Element root = doc.createElement(ST_NODE_REGISTRATION_STATUS);
            doc.appendChild(root);
            root.setAttribute(ST_ATTR_REGISTRATION_STATUS_VERSION, STATUS_DATA_VERSION);

            return doc;
        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            // should not reach here
            InternalError x = new InternalError("Error in creating the new document");
            x.initCause(pce);
            throw x;
        }
    }
    
    // Transform the current DOM tree with the given output stream.
    private static void transform(Document document, OutputStream os) {
        try {
            // Use a Transformer for output
            TransformerFactory tFactory = TransformerFactory.newInstance();
            tFactory.setAttribute("indent-number", new Integer(3));

            Transformer transformer = tFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            transformer.transform(new DOMSource(document),
                new StreamResult(new BufferedWriter(new OutputStreamWriter(os, "UTF-8"))));
        } catch (UnsupportedEncodingException ue) {
            // Should not reach here
            InternalError x = new InternalError("Error generated during transformation");
            x.initCause(ue);
            throw x;
        } catch (TransformerConfigurationException tce) {
            // Error generated by the parser
            // Should not reach here
            InternalError x = new InternalError("Error in creating the new document");
            x.initCause(tce);
            throw x;
        } catch (TransformerException te) {
            // Error generated by the transformer
            InternalError x = new InternalError("Error generated during transformation");
            x.initCause(te);
            throw x;
        }
    }
    
}
