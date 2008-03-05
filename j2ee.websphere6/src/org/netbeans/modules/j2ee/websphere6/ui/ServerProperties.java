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
package org.netbeans.modules.j2ee.websphere6.ui;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.*;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.netbeans.modules.j2ee.websphere6.ui.wizard.WSInstantiatingIterator;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.openide.*;
import org.openide.util.*;

/**
 *
 * @author dlm198383
 */
public class ServerProperties {

    private static final Logger LOGGER = Logger.getLogger(ServerProperties.class.getName());
    
    private JComboBox serverTypeCombo;
    private JComboBox localInstancesCombo;
    private JTextField domainPathField;
    private JTextField hostField;
    private JTextField portField;
    ServerTypeActionListener serverTypeActionListener=null;
    InstanceSelectionListener instanceSelectionListener=null;
    WSInstantiatingIterator instantiatingIterator;
    
    /** Creates a new instance of ServerProperties */
    public ServerProperties() {
        serverTypeCombo=null;
        localInstancesCombo=null;
        domainPathField=null;
        hostField=null;
        portField=null;
    }
    /** Creates a new instance of ServerProperties */
    public ServerProperties(JComboBox serverCombobox,
            JComboBox localInstancesCombobox,
            JTextField domainPathField,
            JTextField hostField,
            JTextField portField) {
        
        
        this.serverTypeCombo=serverCombobox;
        this.localInstancesCombo=localInstancesCombobox;
        this.domainPathField=domainPathField;
        this.hostField=hostField;
        this.portField=portField;
        
    }
    public void setVariables(JComboBox serverCombobox,
            JComboBox localInstancesCombobox,
            JTextField domainPathField,
            JTextField hostField,
            JTextField portField,
            WSInstantiatingIterator instantiatingIterator) {
        
        
        if(this.serverTypeCombo==null)      this.serverTypeCombo=serverCombobox;
        if(this.localInstancesCombo==null)  this.localInstancesCombo=localInstancesCombobox;
        if(this.domainPathField==null)      this.domainPathField=domainPathField;
        if(this.hostField==null)            this.hostField=hostField;
        if(this.portField==null)            this.portField=portField;
        if(this.instantiatingIterator==null) this.instantiatingIterator=instantiatingIterator;
    }
    
    /**
     * Gets the list of registered domains according to the given server
     * installation root
     *
     * @param serverRoot the server's installation location
     *
     * @return an array if strings with the domains' paths
     */
    public static String[] getRegisteredDomains(String serverRoot) {
        // init the resulting vector
        Vector result = new Vector();
        
        // is the server root was not defined, return an empty array of domains
        if (serverRoot == null) {
            return new String[0];
        }
        
        // the relative path to the domains list file
        String domainListFile = "/properties/profileRegistry.xml";     // NOI18N
        
        // init the input stream for the file and the w3c document object
        InputStream inputStream = null;
        Document document = null;
        
        try {
            // open the stream to the domains list file
            inputStream = new FileInputStream(new File(serverRoot +
                    domainListFile));
            
            // create a document from the input stream
            document = DocumentBuilderFactory.newInstance().
                    newDocumentBuilder().parse(inputStream);
            
            // get the root element
            Element root = document.getDocumentElement();
            
            // get its children
            NodeList children = root.getChildNodes();
            
            // for each child
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                // if the child's name equals 'profile' add its 'path' attribute
                // to the resulting vector
                if (child.getNodeName().equals("profile")) {           // NOI18N
                    String path = child.getAttributes().
                            getNamedItem("path").getNodeValue();       // NOI18N
                    result.add(path);
                }
            }
        } catch (FileNotFoundException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } catch (ParserConfigurationException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } catch (SAXException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } catch (IOException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } finally {
            // close the input stream
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
        }
        
        // convert the vector to an array and return
        return (String[]) result.toArray(new String[result.size()]);
    }

    public static boolean isSecurityEnabled(String cellPath) {
        File securityFile = new File(cellPath, "security.xml"); // NOI18N
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(securityFile));
            try {
                XPath xpath = XPathFactory.newInstance().newXPath();
                xpath.setNamespaceContext(new NamespaceContext() {

                    public String getNamespaceURI(String prefix) {
                        if ("security".equals(prefix)) { // NOI18N
                            return "http://www.ibm.com/websphere/appserver/schemas/5.0/security.xmi"; // NOI18N
                        }
                        return XMLConstants.NULL_NS_URI;
                    }

                    public String getPrefix(String namespaceURI) {
                        throw new UnsupportedOperationException("Not needed");
                    }

                    public Iterator getPrefixes(String namespaceURI) {
                        throw new UnsupportedOperationException("Not needed");
                    }
                });
                String value = xpath.evaluate("/security:Security/@enabled", new InputSource(is)); // NOI18N
                return value != null && Boolean.parseBoolean(value);
            } finally {
                is.close();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
        } catch (XPathExpressionException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return false;
    }

    /**
     * Gets the port for a given cell basing on the cell's root directory.
     *
     * @param cellPath the root directory of the cell
     *
     * @return the cell's port
     */
    
    public static String getCellPort(String cellPath) {
        // get the list of files under the nodes subfolder
        String[] files = new File(cellPath + "/nodes").list();
        
        // for each file check whether it is a directory and there exists
        // serverindex.xml, if it does, remember the path and break the loop
        for (int i = 0; i < files.length; i++) {
            String path = cellPath + "/nodes/" + files[i] + "/serverindex.xml";
            if (new File(path).exists()) { // NOI18N
                cellPath = path; // NOI18N
                break;
            }
        }
        
        // init the input stream for the file and the w3c document object
        InputStream inputStream = null;
        Document document = null;
        
        try {
            // open the stream to the cell properties file
            inputStream = new FileInputStream(new File(cellPath));
            
            // create a document from the input stream
            document = DocumentBuilderFactory.newInstance().
                    newDocumentBuilder().parse(inputStream);
            
            // get the root element
            Element root = document.getDocumentElement();
            
            // get the child nodes
            NodeList children = root.getChildNodes();
            
            // for each child
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                // if the child's name equals 'serverEntries' get its children
                // and iterate over them
                if (child.getNodeName().equals("serverEntries")) {     // NOI18N
                    NodeList nl = child.getChildNodes();
                    for (int j = 0; j < nl.getLength(); j++){
                        Node ch = nl.item(j);
                        // if the grandchild's name equals specialEndpoints, and
                        // it has the SOAP_CONNECTOR_ADDRESS attribute
                        if (ch.getNodeName().equals(
                                "specialEndpoints") && ch.             // NOI18N
                                getAttributes().getNamedItem
                                ("endPointName").getNodeValue().       // NOI18N
                                equals("SOAP_CONNECTOR_ADDRESS")) {    // NOI18N
                            NodeList nl2 = ch.getChildNodes();
                            // iterate over its children (the
                            // grandgrandchildren of the root node) and get the
                            // one the the name 'endPoint', from it get the
                            // port attribute
                            for (int k = 0; k < nl2.getLength(); k++) {
                                Node ch2 = nl2.item(k);
                                if (ch2.getNodeName().equals(
                                        "endPoint")) {                 // NOI18N
                                    String port = ch2.getAttributes().
                                            getNamedItem("port").      // NOI18N
                                            getNodeValue();
                                    return port;
                                }
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } catch (ParserConfigurationException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } catch (SAXException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } catch (IOException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } finally {
            // close the input stream
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
        }
        
        // if nothing is found - return an empty string
        return "";                                                     // NOI18N
        
    }
    
    public static String getCellAdminPort(String cellPath) {
        // get the list of files under the nodes subfolder
        String[] files = new File(cellPath + "/nodes").list();
        
        // for each file check whether it is a directory and there exists
        // serverindex.xml, if it does, remember the path and break the loop
        for (int i = 0; i < files.length; i++) {
            String path = cellPath + "/nodes/" + files[i] + "/serverindex.xml";
            if (new File(path).exists()) { // NOI18N
                cellPath = path; // NOI18N
                break;
            }
        }
        
        // init the input stream for the file and the w3c document object
        InputStream inputStream = null;
        Document document = null;
        
        try {
            // open the stream to the cell properties file
            inputStream = new FileInputStream(new File(cellPath));
            
            // create a document from the input stream
            document = DocumentBuilderFactory.newInstance().
                    newDocumentBuilder().parse(inputStream);
            
            // get the root element
            Element root = document.getDocumentElement();
            
            // get the child nodes
            NodeList children = root.getChildNodes();
            
            // for each child
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                // if the child's name equals 'serverEntries' get its children
                // and iterate over them
                if (child.getNodeName().equals("serverEntries")) {     // NOI18N
                    NodeList nl = child.getChildNodes();
                    for (int j = 0; j < nl.getLength(); j++){
                        Node ch = nl.item(j);
                        // if the grandchild's name equals specialEndpoints, and
                        // it has the SOAP_CONNECTOR_ADDRESS attribute
                        if (ch.getNodeName().equals(
                                "specialEndpoints") && ch.             // NOI18N
                                getAttributes().getNamedItem
                                ("endPointName").getNodeValue().       // NOI18N
                                equals("WC_adminhost")) {    // NOI18N
                            NodeList nl2 = ch.getChildNodes();
                            // iterate over its children (the
                            // grandgrandchildren of the root node) and get the
                            // one the the name 'endPoint', from it get the
                            // port attribute
                            for (int k = 0; k < nl2.getLength(); k++) {
                                Node ch2 = nl2.item(k);
                                if (ch2.getNodeName().equals(
                                        "endPoint")) {                 // NOI18N
                                    String port = ch2.getAttributes().
                                            getNamedItem("port").      // NOI18N
                                            getNodeValue();
                                    return port;
                                }
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } catch (ParserConfigurationException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } catch (SAXException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } catch (IOException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } finally {
            // close the input stream
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
        }
        
        // if nothing is found - return an empty string
        return "";                                                     // NOI18N
        
    }
     public static String getCellDefaultHostPort(String cellPath) {
        // get the list of files under the nodes subfolder
        String[] files = new File(cellPath + "/nodes").list();
        
        // for each file check whether it is a directory and there exists
        // serverindex.xml, if it does, remember the path and break the loop
        for (int i = 0; i < files.length; i++) {
            String path = cellPath + "/nodes/" + files[i] + "/serverindex.xml";
            if (new File(path).exists()) { // NOI18N
                cellPath = path; // NOI18N
                break;
            }
        }
        
        // init the input stream for the file and the w3c document object
        InputStream inputStream = null;
        Document document = null;
        
        try {
            // open the stream to the cell properties file
            inputStream = new FileInputStream(new File(cellPath));
            
            // create a document from the input stream
            document = DocumentBuilderFactory.newInstance().
                    newDocumentBuilder().parse(inputStream);
            
            // get the root element
            Element root = document.getDocumentElement();
            
            // get the child nodes
            NodeList children = root.getChildNodes();
            
            // for each child
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                // if the child's name equals 'serverEntries' get its children
                // and iterate over them
                if (child.getNodeName().equals("serverEntries")) {     // NOI18N
                    NodeList nl = child.getChildNodes();
                    for (int j = 0; j < nl.getLength(); j++){
                        Node ch = nl.item(j);
                        // if the grandchild's name equals specialEndpoints, and
                        // it has the SOAP_CONNECTOR_ADDRESS attribute
                        if (ch.getNodeName().equals(
                                "specialEndpoints") && ch.             // NOI18N
                                getAttributes().getNamedItem
                                ("endPointName").getNodeValue().       // NOI18N
                                equals("WC_defaulthost")) {    // NOI18N
                            NodeList nl2 = ch.getChildNodes();
                            // iterate over its children (the
                            // grandgrandchildren of the root node) and get the
                            // one the the name 'endPoint', from it get the
                            // port attribute
                            for (int k = 0; k < nl2.getLength(); k++) {
                                Node ch2 = nl2.item(k);
                                if (ch2.getNodeName().equals(
                                        "endPoint")) {                 // NOI18N
                                    String port = ch2.getAttributes().
                                            getNamedItem("port").      // NOI18N
                                            getNodeValue();
                                    return port;
                                }
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } catch (ParserConfigurationException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } catch (SAXException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } catch (IOException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } finally {
            // close the input stream
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
        }
        
        // if nothing is found - return an empty string
        return "";                                                     // NOI18N
        
    }
    
    public static String getCellHttpPort(String cellPath) {
        // get the list of files under the nodes subfolder
        String[] files = new File(cellPath + "/nodes").list();
        
        // for each file check whether it is a directory and there exists
        // serverindex.xml, if it does, remember the path and break the loop
        for (int i = 0; i < files.length; i++) {
            String path = cellPath + "/nodes/" + files[i] + "/serverindex.xml";
            if (new File(path).exists()) { // NOI18N
                cellPath = path; // NOI18N
                break;
            }
        }
        
        // init the input stream for the file and the w3c document object
        InputStream inputStream = null;
        Document document = null;
        
        try {
            // open the stream to the cell properties file
            inputStream = new FileInputStream(new File(cellPath));
            
            // create a document from the input stream
            document = DocumentBuilderFactory.newInstance().
                    newDocumentBuilder().parse(inputStream);
            
            // get the root element
            Element root = document.getDocumentElement();
            
            // get the child nodes
            NodeList children = root.getChildNodes();
            
            // for each child
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                // if the child's name equals 'serverEntries' get its children
                // and iterate over them
                if (child.getNodeName().equals("serverEntries")) {     // NOI18N
                    NodeList nl = child.getChildNodes();
                    for (int j = 0; j < nl.getLength(); j++){
                        Node ch = nl.item(j);
                        // if the grandchild's name equals specialEndpoints, and
                        // it has the SOAP_CONNECTOR_ADDRESS attribute
                        if (ch.getNodeName().equals(
                                "specialEndpoints") && ch.             // NOI18N
                                getAttributes().getNamedItem
                                ("endPointName").getNodeValue().       // NOI18N
                                equals("WC_defaulthost")) {    // NOI18N
                            NodeList nl2 = ch.getChildNodes();
                            // iterate over its children (the
                            // grandgrandchildren of the root node) and get the
                            // one the the name 'endPoint', from it get the
                            // port attribute
                            for (int k = 0; k < nl2.getLength(); k++) {
                                Node ch2 = nl2.item(k);
                                if (ch2.getNodeName().equals(
                                        "endPoint")) {                 // NOI18N
                                    String port = ch2.getAttributes().
                                            getNamedItem("port").      // NOI18N
                                            getNodeValue();
                                    return port;
                                }
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } catch (ParserConfigurationException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } catch (SAXException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } catch (IOException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } finally {
            // close the input stream
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
        }
        
        // if nothing is found - return an empty string
        return "";                                                     // NOI18N
        
    }
    /**
     * Gets the path to the server's server.xml file
     *
     * @param cellPath the root directory of the cell
     *
     * @return the path to the cell's server.xml
     */
    public static String getConfigXmlPath(String cellPath) {
        // get the list of files under the nodes subfolder
        String[] files = new File(cellPath + "/nodes").list();         // NOI18N
        
        // get the server name
        String serverName = getServerName(cellPath);
        
        // for each file check whether it is a directory and there exists
        // serverindex.xml, if it does, remember the path and break the loop
        for (int i = 0; i < files.length; i++) {
            String path = cellPath + "/nodes/" + files[i] +            // NOI18N
                    "/servers/" + serverName + "/server.xml";          // NOI18N

            if (new File(path).exists()) {
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.log(Level.FINEST, "Config path: " + path); // NOI18N
                }
                return path;
            }
        }

        return "";
    }
    
    /**
     * Gets the server's name for a given cell basing on the cell's root
     * directory.
     *
     * @param cellPath the root directory of the cell
     *
     * @return the server's name
     */
    public static String getServerName(String cellPath) {
        // get the list of files under the nodes subfolder
        String[] files = new File(cellPath + "/nodes").list();         // NOI18N
        
        // for each file check whether it is a directory and there exists
        // serverindex.xml, if it does, remember the path and break the loop
        for (int i = 0; i < files.length; i++) {
            String path = cellPath + "/nodes/" + files[i] +            // NOI18N
                    "/serverindex.xml";                                // NOI18N
            if (new File(path).exists()) {
                cellPath = path; // NOI18N
                break;
            }
        }
        
        // init the input stream for the file and the w3c document object
        InputStream inputStream = null;
        Document document = null;
        
        try {
            inputStream = new FileInputStream(new File(cellPath));
            document = DocumentBuilderFactory.newInstance().
                    newDocumentBuilder().parse(inputStream);
            
            // get the root element
            Element root = document.getDocumentElement();
            
            // get the child nodes
            NodeList children = root.getChildNodes();
            
            for (int i = 0; i < children.getLength(); i++) {
                // if the child's name equals 'serverEntries' get its children
                // and iterate over them
                Node child = children.item(i);
                
                // if the child's name is serverEntries, get its serverName
                // attribute
                if (child.getNodeName().equals("serverEntries")) {     // NOI18N
                    return  child.getAttributes().getNamedItem(
                            "serverName").getNodeValue();              // NOI18N
                }
            }
        } catch (FileNotFoundException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } catch (ParserConfigurationException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } catch (SAXException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } catch (IOException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } finally {
            // close the input stream
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
        }
        
        // if nothing is found - return an empty string
        return "";                                                     // NOI18N
    }
    
    
    /**
     * Gets the list of local server instances.
     *
     * @return a vector with the local instances
     */
    // FIXME use List here
    public static Vector getServerInstances(String serverRoot){
        // initialize the resulting vector
        Vector result = new Vector();
        
        // get the list of registered profiles
        String[] domains = ServerProperties.getRegisteredDomains(
                serverRoot);
        
        // for each domain get the list of cells
        for (int i = 0; i < domains.length; i++) {
            // get the cells root directory
            File file = new File(domains[i] + "/config/cells");        // NOI18N
            
            // get the cells directories list
            String[] files = file.list(new DirectoryFilter());
            
            // for each cell get all the required information and add to the
            // resulting vector
            if (files != null) {
                for (int j = 0; j < files.length; j++){
                    String nextCellPath = file.getAbsolutePath() + File.separator +
                            files[j];
                    boolean securityEnabled = ServerProperties.isSecurityEnabled(nextCellPath);
                    String address = "localhost";                          // NOI18N
                    String port = ServerProperties.getCellPort(nextCellPath);
                    String adminPort = ServerProperties.getCellAdminPort(nextCellPath);
                    String httpPort = ServerProperties.getCellAdminPort(nextCellPath);
                    String serverName = ServerProperties.getServerName(nextCellPath);
                    String configXmlPath = ServerProperties.getConfigXmlPath(nextCellPath);
                    String defaultHostPort = ServerProperties.getCellDefaultHostPort(nextCellPath);
                    result.add(new Instance(serverName, address, port, domains[i],
                            configXmlPath, adminPort, httpPort, defaultHostPort, securityEnabled));
                }
            }
        }
        
        // return the vector
        return result;
    }
    /**
     * Checks whether the specified path is the valid domain root directory.
     *
     * @return true if the path is the valid domain root, false otherwise
     */
    public static boolean isValidDomainRoot(String path) {
        // set the child directories/files that should be present and validate
        // the directory as the domain root
        String[] children = {
            "config/cells",                                    // NOI18N
            "etc/ws-security",                                 // NOI18N
            "properties/soap.client.props",                    // NOI18N
            "properties/wsadmin.properties",                   // NOI18N
        };
        return ServerProperties.hasChildren(path, children);
    }
    
    /**
     * Checks whether the supplied directory has the required children
     *
     * @return true if the directory contains all the children, false otherwise
     */
    public static boolean hasChildren(String parent, String[] children) {
        // if parent is null, it cannot contain any children
        if (parent == null) {
            return false;
        }
        
        // if the children array is null, then the condition is fullfilled
        if (children == null) {
            return true;
        }
        
        // for each child check whether it is contained and if it is not,
        // return false
        for (int i = 0; i < children.length; i++) {
            if (!(new File(parent + File.separator + children[i]).exists())) {
                return false;
            }
        }
        
        // all is good
        return true;
    }
    
    
    
    
    
    
    /**
     * An extension of the FileNameFilter class that is setup to accept only
     * directories.
     *
     * @author Kirill Sorokin
     */
    
    private static class DirectoryFilter implements FilenameFilter {
        /**
         * This method is called when it is needed to decide whether a chosen
         * file meets the filter's requirements
         *
         * @return true if the file meets the requirements, false otherwise
         */
        public boolean accept(File dir, String name) {
            // if the file exists and it's a directory - accept it
            if ((new File(dir.getAbsolutePath()+File.separator+name)).
                    isDirectory()) {
                return true;
            }
            
            // in all other cases - refuse
            return false;
        }
    }
    /**
     * A listener that reacts to the change of the server type combobox,
     * is the local server type is selected we should disable several fields
     * and enable some others instead.
     *
     * @author Kirill Sorokin
     */
    public class ServerTypeActionListener implements ActionListener {
        /**
         * The main action handler. This method is called when the combobox
         * value changes
         */
        
        public void actionPerformed(ActionEvent e) {
            // if the selected type is local
            if (serverTypeCombo.getSelectedItem().equals(NbBundle.
                    getMessage(ServerProperties.class,
                    "TXT_ServerTypeLocal"))) {                         // NOI18N
                Instance instance = (Instance) localInstancesCombo.
                        getSelectedItem();
                if (instance == null) {
                    return;
                }
                
                // enable the local instances combo
                localInstancesCombo.setEnabled(true);
                
                // enable and set as read-only the domain path field
                domainPathField.setEnabled(true);
                domainPathField.setEditable(false);
                
                // enable and set as read-only the host field
                hostField.setEnabled(true);
                hostField.setEditable(false);
                hostField.setText(instance.getHost());
                
                // enable and set as read-only the port field
                //portField.setEnabled(true);
                portField.setEnabled(true);
                portField.setEditable(false);
                //portField.setValue(new Integer(instance.getPort()));
                portField.setText(instance.getPort());
            } else {
                // disable the local instances combo
                localInstancesCombo.setEnabled(false);
                
                // disable the domain path field
                domainPathField.setEnabled(false);
                domainPathField.setEditable(false);
                
                // enable and set as read-write the host field
                hostField.setEnabled(true);
                hostField.setEditable(true);
                
                // enable and set as read-write the port field
                portField.setEnabled(true);
                portField.setEditable(true);
            }
        }
    }
    public ServerTypeActionListener getServerTypeActionListener() {
        if(serverTypeActionListener==null) {
            serverTypeActionListener=new ServerTypeActionListener();
        }
        
        return serverTypeActionListener;
    }
    ////////////////////////////////////////////////////////////////////////////
    // Listeners section
    ////////////////////////////////////////////////////////////////////////////
    /**
     * The registrered listeners vector
     */
    private Vector listeners = new Vector();
    
    /**
     * Removes a registered listener
     *
     * @param listener the listener to be removed
     */
    public void removeChangeListener(ChangeListener listener) {
        if (listeners != null) {
            synchronized (listeners) {
                listeners.remove(listener);
            }
        }
    }
    
    /**
     * Adds a listener
     *
     * @param listener the listener to be added
     */
    public void addChangeListener(ChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    /**
     * Fires a change event originating from this panel
     */
    private void fireChangeEvent() {
        ChangeEvent event = new ChangeEvent(this);
        fireChangeEvent(event);
    }
    
    /**
     * Fires a custom change event
     *
     * @param event the event
     */
    private void fireChangeEvent(ChangeEvent event) {
        Vector targetListeners;
        synchronized (listeners) {
            targetListeners = (Vector) listeners.clone();
        }
        
        for (int i = 0; i < targetListeners.size(); i++) {
            ChangeListener listener =
                    (ChangeListener) targetListeners.elementAt(i);
            listener.stateChanged(event);
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    // Inner classes
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Simple key listener that delegates the event to its parent's listeners
     *
     * @author Kirill Sorokin
     */
    public class KeyListener extends KeyAdapter {
        /**
         * This method is called when a user presses a key on the keyboard
         */
        public void keyTyped(KeyEvent event) {
            fireChangeEvent();
        }
        
        /**
         * This method is called when a user releases a key on the keyboard
         */
        public void keyReleased(KeyEvent event) {
            fireChangeEvent();
        }
    }
    
    
    /**
     * Updates the local instances combobox model with the fresh local
     * instances list
     */
    public void updateInstancesList(String serverRoot) {
        localInstancesCombo.setModel(
                new InstancesModel(
                getServerInstances(serverRoot)));
        
        updateInstanceInfo();
    }
    
    /**
     * Updates the selected local instance information, i.e. profile path,
     * host, port.
     */
    private void updateInstanceInfo() {
        // get the selected local instance
        Instance instance = (Instance) localInstancesCombo.getSelectedItem();
        
        if (instance != null) {
            // set the fields' values
            domainPathField.setText(instance.getDomainPath());
            hostField.setText(instance.getHost());
            //portField.setValue(new Integer(instance.getPort()));
            portField.setText(instance.getPort());
            if(instantiatingIterator!=null) {
                instantiatingIterator.setHost(instance.getHost());
                instantiatingIterator.setPort(instance.getPort());
                instantiatingIterator.setAdminPort(instance.getAdminPort());
                instantiatingIterator.setConfigXmlPath(instance.getConfigXmlPath());
                instantiatingIterator.setDefaultHostPort(instance.getDefaultHostPort());
                instantiatingIterator.setDomainRoot(instance.getDomainPath());
                instantiatingIterator.setServerName(instance.getName());
            }
        }
    }
    /**
     * A simple listeners that reacts to user's selectin a local instance. It
     * updates the selected instance info.
     *
     * @author Kirill Sorokin
     */
    public class InstanceSelectionListener implements ActionListener {
        /**
         * The main action handler. This method is called when a new local
         * instance is selected
         */
        public void actionPerformed(ActionEvent e) {
            updateInstanceInfo();
        }
    }
    /**
     * Create new or get existing InstanceSelectionListener
     */
    public InstanceSelectionListener getInstanceSelectionListener() {
        if(instanceSelectionListener==null) {
            instanceSelectionListener=new InstanceSelectionListener();
        }
        return instanceSelectionListener;
    }
}

