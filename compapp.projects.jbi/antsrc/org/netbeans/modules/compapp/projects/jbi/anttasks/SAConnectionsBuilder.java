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

package org.netbeans.modules.compapp.projects.jbi.anttasks;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.compapp.projects.jbi.descriptor.XmlUtil;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.netbeans.modules.compapp.projects.jbi.descriptor.endpoints.model.Connection;
import org.netbeans.modules.compapp.projects.jbi.descriptor.endpoints.model.Endpoint;

import static org.netbeans.modules.compapp.projects.jbi.JbiConstants.*;
import static org.netbeans.modules.compapp.projects.jbi.CasaConstants.*;

/**
 * Create Service Assembly connections for the JBI deployment descriptor
 *
 * @author tli
 * @author jqian
 */
public class SAConnectionsBuilder implements Serializable {
    
    /*
    connections =
      element connections {
        element connection {
          element consumer {
            ( attribute interface-name { xsd:QName } |
             (attribute service-name { xsd:QName }, attribute endpoint-name { text })
            )
          },
          element provider {
            attribute service-name { xsd:QName }, attribute endpoint-name { text }
          }
        }*,
        element* -this:* { text }*
      }
     */
    
    public static final String QOS_NAMESPACE_URI = "http://www.sun.com/jbi/qos";  // NO18N
    
    public static final String VERSION_ATTR_NAME = "version"; // NOI18N
    public static final String VERSION_ATTR_VALUE = "1.0"; // NOI18N
    public static final String NS_ATTR_NAME = "xmlns";  // NOI18N
    public static final String NS_ATTR_VALUE="http://java.sun.com/xml/ns/jbi"; // NOI18N
    public static final String NS_XSI_ATTR_NAME = "xmlns:xsi";  // NOI18N
    public static final String NS_XSI_ATTR_VALUE ="http://www.w3.org/2001/XMLSchema-instance"; // NOI18N
    public static final String XSI_ATTR_NAME = "xsi:schemaLocation"; // NOI18N
    public static final String XSI_ATTR_VALUE ="http://java.sun.com/xml/ns/jbi jbi.xsd"; // NOI18N
    
    public static final String NAMESPACE_PREFIX = "ns"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    static Document document;
        
    /**
     * DOCUMENT ME!
     *
     * @param container DOCUMENT ME!
     * @param repo DOCUMENT ME!     *
     * @param p
     * @throws javax.xml.parsers.ParserConfigurationException DOCUMENT ME!
     *
    public void buildDOMTree(ConnectionResolver container, 
            String saName, String saDescription, Document casaDocument)
            throws ParserConfigurationException {
                
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.newDocument(); // Create from whole cloth
        
        Element root = document.createElement(JBI_ELEM_NAME);
        root.setAttribute(VERSION_ATTR_NAME, VERSION_ATTR_VALUE);
        root.setAttribute(NS_ATTR_NAME, NS_ATTR_VALUE);
        //root.setAttribute(NS_XSI_ATTR_NAME, NS_XSI_ATTR_VALUE);
        //root.setAttribute(XSI_ATTR_NAME, XSI_ATTR_VALUE);
        
        Map<String, String> nsMap = container.getNamespaceMap();
        for (String key : nsMap.keySet()) {
            if (key != null) {
                String value = nsMap.get(key);
                root.setAttribute("xmlns:" + key, value);
            }
        }
        
        document.appendChild(root);
        Element sroot = document.createElement(JBI_SERVICE_ASSEMBLY_ELEM_NAME);
        root.appendChild(sroot);
        
        // add identification
        Element id = document.createElement(JBI_IDENTIFICATION_ELEM_NAME);
        id.appendChild(createTextNode(JBI_NAME_ELEM_NAME, saName));
        id.appendChild(createTextNode(JBI_DESCRIPTION_ELEM_NAME, saDescription));
        sroot.appendChild(id);
        
        // add connections (and qos:connections)
        List<Element> connectionsElements = 
                createConnections(container, document, casaDocument);
        for (Element connections : connectionsElements) {
            sroot.appendChild(connections);
        }
    }*/
    
    public List<Element> createConnections(ConnectionResolver container, 
            Document document, Document casaDocument) {
        List<Element> ret = new ArrayList<Element>();
        
        NodeList rs = document.getElementsByTagName(JBI_ELEM_NAME);
        
        if ((rs != null) && (rs.getLength() > 0)) {
           
            Element root = (Element) rs.item(0);

            // create connections
            Element connections = document.createElement(JBI_CONNECTIONS_ELEM_NAME);
            
            // qos:connections will be created on demand
            Element qosConnections = null;
            
            List<Element> casaConnectionElementsWithExtension = 
                    getCasaConnectionsWithExtensions(casaDocument);

            Map<String, String> nsMap = container.getNamespaceMap();
            for (Connection con : container.getConnectionList()) {
                Endpoint c = con.getConsume();
                Endpoint p = con.getProvide();
                if (!c.equals(p)) {
                    // create connection; add provides and consumes 
                    Element connection = document.createElementNS(
                            JBI_NAMESPACE_URI, JBI_CONNECTION_ELEM_NAME);
                    Element consumes = createEndpoint(c, JBI_CONSUMER_ELEM_NAME, 
                            root, nsMap, document); 
                    Element provides = createEndpoint(p, JBI_PROVIDER_ELEM_NAME, 
                            root, nsMap, document);  
                    connection.appendChild(consumes);
                    connection.appendChild(provides);
                    connections.appendChild(connection);

                    // create qos:connection; add qos:provides and qos:consumes;
                    // add jbi extension                
                    if (casaDocument != null) {
                        Element casaConnection = getCasaConnection(
                                casaConnectionElementsWithExtension, 
                                casaDocument,
                                c, p);
                        if (casaConnection != null && 
                                casaConnection.getChildNodes().getLength() > 0) {     
                            if (qosConnections == null) {
                                qosConnections = document.createElementNS(
                                        QOS_NAMESPACE_URI, JBI_CONNECTIONS_ELEM_NAME);
                            }
                            Element qosConnection = document.createElement( 
                                    JBI_CONNECTION_ELEM_NAME);
                            qosConnection.appendChild(consumes.cloneNode(false));
                            qosConnection.appendChild(provides.cloneNode(false));
                            qosConnections.appendChild(qosConnection);
                            CasaBuilder.deepCloneChildren(
                                    casaConnection, qosConnection);

                            // Performance Enhancement
                            casaConnectionElementsWithExtension.remove(casaConnection);
                        }
                    }
                }
            }
            
            ret.add(connections);
            if (qosConnections != null) {
                ret.add(qosConnections);
            }
        }
        
        return ret;
    }
    
    /**
     * Gets a list of CASA connection elements that contain extension elements.
     */
    // needed for performance purpose
    private static List<Element> getCasaConnectionsWithExtensions(
            Document casaDocument) {
        List<Element> ret = new ArrayList<Element>();
        
        if (casaDocument != null) {        
            NodeList casaConnections = casaDocument.getElementsByTagName(
                    CASA_CONNECTION_ELEM_NAME);
            for (int i = 0; i < casaConnections.getLength(); i++) {
                Element casaConnection = (Element) casaConnections.item(i);

                if (casaConnection.hasChildNodes()) {
                    ret.add(casaConnection);
                }
            }
        }
        
        return ret;
    }    
    
    private static Element getCasaConnection(
            List<Element> casaConnectionElements, 
            Document casaDocument, 
            Endpoint c, Endpoint p) {
        
        assert casaConnectionElements != null && c != null && p != null;
        
        for (Element casaConnection : casaConnectionElements) {
            
            String consumerEndpointID = casaConnection.getAttribute(CASA_CONSUMER_ATTR_NAME);
            Endpoint consumer = CasaBuilder.getEndpoint(casaDocument, consumerEndpointID);
            if (c.equals(consumer)) {

                String providerEndpointID = casaConnection.getAttribute(CASA_PROVIDER_ATTR_NAME);
                Endpoint provider = CasaBuilder.getEndpoint(casaDocument, providerEndpointID);
                if (p.equals(provider)) {
                    return casaConnection;
                }
            }
        }
        
        return null;
    }    
    
    private Element createTextNode(String name, String text) {
        Element element = document.createElement(name);
        element.appendChild(
                document.createTextNode(text)
                );
        return element;
    }
    
    private Element createEndpoint(Endpoint ep, String type, Element root, 
            Map map, Document document) {
        Element endpointNode = document.createElement(type);
        
        endpointNode.setAttribute(
                JBI_SERVICE_NAME_ATTR_NAME,
                xlateQName(ep.getServiceQName().toString(), root, map)
                );
        endpointNode.setAttribute(
                JBI_ENDPOINT_NAME_ATTR_NAME, ep.getEndpointName()
                );
        
        /*
        //todo: 03/23/06 use namespace prefix locally...
        String qname = ep.getService();
        String ns = getNS(qname);
        String prefix = null;
        if (ns != null) {
            prefix = findNamespacePrefix(map, ns, root);
            qname = prefix + ":" + stripNS(qname);
        }
        endpointNode.setAttribute(
            SERVICE_ATTR_NAME, qname
        );
        endpointNode.setAttribute(
            ENDPOINT_ATTR_NAME, ep.getEndpoint()
        );
        if (prefix != null) {
            endpointNode.setAttribute("xmlns:"+prefix, ns);
        }
         */
        return endpointNode;
    }
    
    private String xlateQName(String qname, Element root, Map map) {
        String ns = getNS(qname);
        if (ns == null) {
            return qname; // no namespace
        }
        
        String name = stripNS(qname);
        String prefix = findNamespacePrefix(map, ns, root);
        return prefix + ":" + name; // NOI18N
    }
    
    private String getNS(String name) {
        if (name == null) {
            return null;
        }
        if (name.startsWith("{")) {
            int k = name.indexOf('}');  // NOI18N
            if (k > 0) {
                return name.substring(1, k);
            }
        }
        return null;
    }
    
    private String stripNS(String name) {
        if (name == null) {
            return null;
        }
        int k = name.indexOf('}'); // NOI18N
        if (k > 0) {
            return name.substring(k+1);
        }
        return name;
    }
    
    private String findNamespacePrefix(Map<String, String> nsMap, 
            String uri, Element root) {
        for (String prefix : nsMap.keySet()) {
            if ((nsMap.get(prefix)).compareTo(uri) == 0) {
                return prefix;
            }
        }
        
        // not found needs to add...
        for (int i = 1; i<Integer.MAX_VALUE; i++) {
            String prefix = NAMESPACE_PREFIX + i;
            if (nsMap.get(prefix) == null) {
                nsMap.put(prefix, uri);
                root.setAttribute("xmlns:"+prefix, uri);
                return prefix;
            }
        }
        
        // got a problem..
        return null;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param fileLocation DOCUMENT ME!
     *
     * @throws javax.xml.transform.TransformerConfigurationException DOCUMENT ME!
     * @throws javax.xml.transform.TransformerException DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public void writeToFile(String fileLocation)
            throws TransformerConfigurationException, TransformerException, Exception {
        XmlUtil.writeToFile(fileLocation, document);
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws TransformerConfigurationException DOCUMENT ME!
     * @throws TransformerException DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public byte[] writeToBytes()
            throws TransformerConfigurationException, TransformerException, Exception {
        return XmlUtil.writeToBytes(document);
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
}

