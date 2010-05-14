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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


/*
 * Created on Jan 26, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.netbeans.modules.sun.manager.jbi.management.model;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


/**
 * DOCUMENT ME!
 *
 * @author Graj TODO To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Style - Code Templates
 */
public class ComponentInformationParser extends DefaultHandler implements Serializable {
    //  Private members needed to parse the XML document
    private boolean parsingInProgress; // keep track of parsing
    private Stack qNameStack = new Stack(); // keep track of QName
    
    /**
     * DOCUMENT ME!
     */
    JBIComponentStatus component = null;
    
    /**
     * DOCUMENT ME!
     */
    List<JBIComponentStatus> components = new ArrayList<JBIComponentStatus>();
    
    /**
     *
     */
    public ComponentInformationParser() {
        super();
        
        // TODO Auto-generated constructor stub
    }
    
    
    public List<JBIComponentStatus> getComponents() {
        return components;
    }
        
    /**
     * DOCUMENT ME!
     *
     * @param documentString DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws SAXException DOCUMENT ME!
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws ParserConfigurationException DOCUMENT ME!
     * @throws SAXException DOCUMENT ME!
     */
    public static List<JBIComponentStatus> parse(String documentString)
            throws IOException, SAXException, FileNotFoundException, 
            ParserConfigurationException, SAXException {
        List<JBIComponentStatus> ret = new ArrayList<JBIComponentStatus>();
        
        // Get an instance of the SAX parser factory
        SAXParserFactory factory = SAXParserFactory.newInstance();
        
        // Get an instance of the SAX parser
        SAXParser saxParser = factory.newSAXParser();
        
        StringReader reader = new StringReader(documentString);
        
        // Create an InputSource from the Reader
        InputSource inputSource = new InputSource(reader);
        
        // Parse the input XML document stream, using my event handler
        ComponentInformationParser myEventHandler = new ComponentInformationParser();
        
        if (inputSource != null) {
            saxParser.parse(inputSource, myEventHandler);
            ret = myEventHandler.getComponents();
        }
        
        return ret; 
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param documentFile DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws SAXException DOCUMENT ME!
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws ParserConfigurationException DOCUMENT ME!
     * @throws SAXException DOCUMENT ME!
     */
    public static List<JBIComponentStatus> parse(File documentFile)
            throws IOException, SAXException, FileNotFoundException,
            ParserConfigurationException, SAXException {
        List<JBIComponentStatus> ret = new ArrayList<JBIComponentStatus>();
        
        // Get an instance of the SAX parser factory
        SAXParserFactory factory = SAXParserFactory.newInstance();
        
        // Get an instance of the SAX parser
        SAXParser saxParser = factory.newSAXParser();
        saxParser.setProperty("http://apache.org/xml/properties/input-buffer-size",  // NOI18N
                new Integer(8192));
        
        FileReader reader = new FileReader(documentFile);
        
        // Create an InputSource from the Reader
        InputSource inputSource = new InputSource(reader);
        
        // Parse the input XML document stream, using my event handler
        ComponentInformationParser myEventHandler = new ComponentInformationParser();
        saxParser.parse(inputSource, myEventHandler);
        ret = myEventHandler.getComponents();
        
        return ret;
    }
    
    /**
     * Start of document processing.
     *
     * @throws SAXException is any SAX exception, possibly wrapping another exception.
     */
    @Override
    public void startDocument() throws SAXException {
        parsingInProgress = true;
        qNameStack.removeAllElements();
        components.clear();
    }
    
    /**
     * End of document processing.
     *
     * @throws SAXException is any SAX exception, possibly wrapping another exception.
     */
    @Override
    public void endDocument() throws SAXException {
        parsingInProgress = false;        
    }
    
    /**
     * Process the new element.
     *
     * @param uri is the Namespace URI, or the empty string if the element has no Namespace URI or
     *        if Namespace processing is not being performed.
     * @param localName is the local name (without prefix), or the empty string if Namespace
     *        processing is not being performed.
     * @param qName is the qualified name (with prefix), or the empty string if qualified names are
     *        not available.
     * @param attributes is the attributes attached to the element. If there are no attributes, it
     *        shall be an empty Attributes object.
     *
     * @throws SAXException is any SAX exception, possibly wrapping another exception.
     */
    @Override
    public void startElement(String uri, String localName, String qName, 
            Attributes attributes)
            throws SAXException {
        if (JBIComponentDocument.COMP_INFO_LIST_NODE_NAME.equals(qName)) {
            String key = null;
            String value = null;
            
            for (int index = 0; index < attributes.getLength(); index++) {
                key = attributes.getQName(index);
                
                if (key != null) {
                    value = attributes.getValue(key);
                    
                    if (value != null) {
//                        namespaceMap.put(key, value);
                    }
                }
            }
        } else {
            if (JBIComponentDocument.COMP_INFO_NODE_NAME.equals(qName)) {
                component = new JBIComponentStatus();
                
                String key = null;
                String value = null;
                
                for (int index = 0; index < attributes.getLength(); index++) {
                    key = attributes.getQName(index);
                    
                    if (key != null) {
                        value = attributes.getValue(key);
                        
                        if (value != null) {
                            if (JBIComponentDocument.NAME_NODE_NAME.equals(key)) {
                                if ((component != null) && (value != null)) {
                                    component.setName(value);
                                }
                            } else if (JBIComponentDocument.STATUS_NODE_NAME.equals(key)) {
                                if ((component != null) && (value != null)) {
                                    component.setState(value);
                                }
                            } else if (JBIComponentDocument.TYPE_NODE_NAME.equals(key)) {
                                if ((component != null) && (value != null)) {
                                    // component.setType(value);
                                    String type = value.toLowerCase();
                                    
                                    if (type.indexOf("binding") > -1) { // NOI18N
                                        component.setType("binding"); // NOI18N
                                    } else if (type.indexOf("engine") > -1) { // NOI18N
                                        component.setType("engine"); // NOI18N
                                    } else {
                                        component.setType(type);
                                    }
                                }
                            } else if (JBIComponentDocument.NAMESPACE_NODE_NAME.equals(key)) {
                                if ((component != null) && (value != null)) {
                                    component.addNamespace(value);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Keep track of QNames
        qNameStack.push(qName);
    }
    
    /**
     * Process the character data for current tag.
     *
     * @param ch are the element's characters.
     * @param start is the start position in the character array.
     * @param length is the number of characters to use from the character array.
     *
     * @throws SAXException is any SAX exception, possibly wrapping another exception.
     */
    @Override
    public void characters(char[] ch, int start, int length)
    throws SAXException {

        //
        // todo: 01/19/07, The original code is buggy.. it does not handle end-of-buffer condition
        // todo: 01/19/07, needs to buffer chars and do the logic in the endElement processing...
        //
        String qName;
        String chars = new String(ch, start, length);
        
        //  Get current QName
        qName = (String) qNameStack.peek();
        
        if (JBIComponentDocument.DESCRIPTION_NODE_NAME.equals(qName)) {
            if ((component != null) && (chars != null)) {
                component.setDescription(chars);
            }
        } else if (JBIComponentDocument.NAME_NODE_NAME.equals(qName)) {
            if ((component != null) && (chars != null)) {
                component.setName(chars);
            }
        } else if (JBIComponentDocument.STATUS_NODE_NAME.equals(qName)) {
            if ((component != null) && (chars != null)) {
                component.setState(chars);
            }
        } else if (JBIComponentDocument.TYPE_NODE_NAME.equals(qName)) {
            if ((component != null) && (chars != null)) {
                String type = chars.toLowerCase();
                
                if (type.indexOf("binding") > -1) { // NOI18N
                    component.setType("binding"); // NOI18N
                } else if (type.indexOf("engine") > -1) { // NOI18N
                    component.setType("engine"); // NOI18N
                } else {
                    component.setType(type);
                }
            }
        } else if (JBIComponentDocument.NAMESPACE_NODE_NAME.equals(qName)) {
            if ((component != null) && (chars != null)) {
                component.addNamespace(chars);
            }
        }        
    }
    
    /**
     * Process the end element tag.
     *
     * @param uri is the Namespace URI, or the empty string if the element has no Namespace URI or
     *        if Namespace processing is not being performed.
     * @param localName is the local name (without prefix), or the empty string if Namespace
     *        processing is not being performed.
     * @param qName is the qualified name (with prefix), or the empty string if qualified names are
     *        not available.
     *
     * @throws SAXException is any SAX exception, possibly wrapping another exception.
     */
    @Override
    public void endElement(String uri, String localName, String qName)
        throws SAXException {
        
        //  Pop QName, since we are done with it
        qNameStack.pop();
        
        if (JBIComponentDocument.COMP_INFO_LIST_NODE_NAME.equals(qName)) {
            //  We have encountered the end of
            // JBIComponentDocument.COMP_INFO_LIST_NODE_NAME
            //  ...
        } else if (JBIComponentDocument.COMP_INFO_NODE_NAME.equals(qName)) {
            //  We have encountered the end of
            // JBIComponentDocument.COMP_INFO_NODE_NAME
            //  ...
            //this.component.dump();
            components.add(component);
            this.component = null;
        }
    }
}