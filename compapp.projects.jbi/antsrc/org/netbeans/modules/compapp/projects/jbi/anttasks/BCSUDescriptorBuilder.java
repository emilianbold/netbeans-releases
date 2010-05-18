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

import javax.xml.namespace.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.netbeans.modules.compapp.projects.jbi.descriptor.endpoints.model.*;
import org.netbeans.modules.compapp.projects.jbi.descriptor.XmlUtil;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.*;
import java.util.*;
import java.io.File;
import java.io.Serializable;

import static org.netbeans.modules.compapp.projects.jbi.JbiConstants.*;

/**
 * Creates a Binding Component Service Unit's deployment descriptor (jbi.xml).
 *
 * @author tli
 * @author jqian
 */
public class BCSUDescriptorBuilder implements Serializable {
    
    public static final String VERSION_ATTR_NAME = "version"; // NOI18N
    public static final String VERSION_ATTR_VALUE = "1.0"; // NOI18N
    public static final String NS_ATTR_NAME = "xmlns";  // NOI18N
    public static final String NS_XSI_ATTR_NAME = "xmlns:xsi";  // NOI18N
    public static final String NS_XSI_ATTR_VALUE ="http://www.w3.org/2001/XMLSchema-instance"; // NOI18N
    public static final String XSI_ATTR_NAME = "xsi:schemaLocation"; // NOI18N
    public static final String XSI_ATTR_VALUE ="http://java.sun.com/xml/ns/jbi jbi.xsd"; // NOI18N
    
    public static final String NAMESPACE_PREFIX = "ns"; // NOI18N
    
    private Document document;
    
    /**
     * Creates a new instance of CreateBCSUDescriptor
     */
    public BCSUDescriptorBuilder() {
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param connectionResolver 
     * @param bcName                name of the binding component
     *
     * @throws ParserConfigurationException DOCUMENT ME!
     */
    public void buildDOMTree(ConnectionResolver connectionResolver, 
            String bcName)
            throws ParserConfigurationException {   
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.newDocument(); // Create from whole cloth
        
        Element root = document.createElement(JBI_ELEM_NAME);
        root.setAttribute(VERSION_ATTR_NAME, VERSION_ATTR_VALUE);
        root.setAttribute(NS_ATTR_NAME, JBI_NAMESPACE_URI);
        //root.setAttribute(NS_XSI_ATTR_NAME, NS_XSI_ATTR_VALUE);
        //root.setAttribute(XSI_ATTR_NAME, XSI_ATTR_VALUE);
        
        Map<String, String> nsMap = connectionResolver.getNamespaceMap();
        for (String key : nsMap.keySet()) {
            String value = nsMap.get(key);
            root.setAttribute("xmlns:" + key, value);
        }
        
        document.appendChild(root);
        
        // add services
        Element services = document.createElement(JBI_SERVICES_ELEM_NAME);
        services.setAttribute(JBI_BINDING_COMPONENT_ATTR_NAME, "true");
        root.appendChild(services);
        
        // provides and consumes
        List<Connection>[] list = connectionResolver.getBCConnections().get(bcName);
        List<Connection> clist = list[0];   // port as the connection consumer
        List<Connection> plist = list[1];   // port as the connection provider

        // 03/14/07, merget consume and provide list
        HashSet<Endpoint> providerSet = new HashSet<Endpoint>();
        
        // add provides first
        for (Connection con : plist) {
            Endpoint provide = con.getProvide();
            // There can be more than one consumer but only one provider.
            if (!providerSet.contains(provide)){   
                providerSet.add(provide);
                Element providesElement = document.createElement(JBI_PROVIDES_ELEM_NAME);
                setEndpointElementAttributes(providesElement, provide, root, nsMap); // FIXME
                services.appendChild(providesElement);
            }
        }        
             
        // add consumes
        for (Connection con : clist) {
            Endpoint consume = con.getConsume();
            Element consumesElement = document.createElement(JBI_CONSUMES_ELEM_NAME);
            setEndpointElementAttributes(consumesElement, consume, root, nsMap);  // FIXME  
            services.appendChild(consumesElement);
        }          
        
        document.normalizeDocument();
    }
    
    /**
     * Decorates consumes/provides endpoint using extension elements in CASA.
     */
    public void decorateEndpoints(Document casaDocument) {
        ServiceUnitDescriptorEnhancer.decorateEndpoints(document, casaDocument);
    }    
    
    private void setEndpointElementAttributes(Element endpointElement, 
            Endpoint endpoint,
            Element root, Map<String, String> nsMap) {
        endpointElement.setAttribute(JBI_INTERFACE_NAME_ATTR_NAME,
                xlateQName(endpoint.getInterfaceQName(), root, nsMap));
        endpointElement.setAttribute(JBI_SERVICE_NAME_ATTR_NAME,
                xlateQName(endpoint.getServiceQName(), root, nsMap));
        endpointElement.setAttribute(JBI_ENDPOINT_NAME_ATTR_NAME, 
                endpoint.getEndpointName());
    }
        
    private String xlateQName(QName qname, Element root, Map<String, String> map) {
        String ns = qname.getNamespaceURI(); 
        if (ns == null || ns.trim().length() == 0) {
            return qname.getLocalPart(); // no namespace
        } else {            
            String name = qname.getLocalPart(); 
            String prefix = findNamespacePrefix(map, ns, root);
            return prefix + ":" + name; // NOI18N
        }
    }
    
    private String findNamespacePrefix(Map<String, String> map, String uri, Element root) {
        for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();) {
            String prefix = (String) iterator.next();
            if (map.get(prefix).equals(uri)) {
                return prefix;
            }
        }
        
        // not found needs to add...
        for (int i = 1; i<Integer.MAX_VALUE; i++) {
            String prefix = NAMESPACE_PREFIX + i;
            if (map.get(prefix) == null) {
                map.put(prefix, uri);
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
     * @param directoryPath DOCUMENT ME!
     *
     * @throws TransformerConfigurationException DOCUMENT ME!
     * @throws TransformerException DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public void writeToFile(String directoryPath)
    throws TransformerConfigurationException, TransformerException, Exception {
        File file = new File(directoryPath);
        
        if ((file.isDirectory() == false) || (file.exists() == false)) {
            throw new Exception("Directory Path: " + directoryPath + " is invalid.");
        }
        
        String fileLocation = file.getAbsolutePath() + File.separator + "jbi.xml";
//        System.out.println("Writing out to file: " + fileLocation);
        
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
}
