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

package org.netbeans.modules.sun.manager.jbi.util;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;

/**
 * Reader for NetBeans server config file.
 * 
 * @author jqian
 */
public class ServerInstanceReader {
    
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String RELATIVE_FILE_PATH = 
            FILE_SEPARATOR + "config" +  // NOI18N
            FILE_SEPARATOR + "J2EE" +  // NOI18N
            FILE_SEPARATOR + "InstalledServers" +  // NOI18N
            FILE_SEPARATOR + ".nbattrs"; // NOI18N
    
    private static final String NB_DEFAULT_ATTRS_DTD = "xml/entities/NetBeans/DTD_DefaultAttributes_1_0"; // NOI18N
    private static final String NB_DEFAULT_ATTRS_PUBLIC_ID = "-//NetBeans//DTD DefaultAttributes 1.0//EN"; // NOI18N
    
    private static final String LOCAL_NB_DEFAULT_ATTRS_DTD = "NetBeansDefaultAttrs_1_0.dtd";
    
    private List<ServerInstance> instances;
    private String fileName;
    
    /**
     * Creates a new ServerInstanceReader object.
     *
     * @param fileName 
     */
    public ServerInstanceReader(String fileName) {
        assert fileName != null;
        this.fileName = fileName.replace('\\', '/'); // NOI18N
        assert new File(fileName).exists() : "Server config file " + fileName + " doesn't exist.";
    }
    
    /**
     * Gets a list of server instances in the server config file that meet our 
     * assumption: no null data for host and location... see 
     * AppserverJBIMgmtController.isCurrentInstance.
     */
    public List<ServerInstance> getServerInstances() {
        if (instances == null) {
            instances = new ArrayList<ServerInstance>();
            
            try {
                XPath xpath = XPathFactory.newInstance().newXPath();
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();                
                DocumentBuilder builder = factory.newDocumentBuilder();
                
                builder.setEntityResolver(new EntityResolver() {
                    public InputSource resolveEntity(String publicID, String systemID)
                    throws SAXException, IOException {
                        if (NB_DEFAULT_ATTRS_PUBLIC_ID.equals(publicID)) {
                            FileObject file = Repository.getDefault().getDefaultFileSystem().findResource(NB_DEFAULT_ATTRS_DTD);
                            if (file != null) {
                                return new InputSource(file.getInputStream());
                            } else { // command line support for offline ATS
                                URL url = getClass().getResource(LOCAL_NB_DEFAULT_ATTRS_DTD);
                                return new InputSource(url.openStream());
                            }
                        }
                        
                        // use the default behavior
                        return null;
                    }
                });
                Document document = builder.parse(new File(fileName));
                
                NodeList nodes = (NodeList) xpath.evaluate(
                        "/attributes/fileobject", document,  // NOI18N
                        XPathConstants.NODESET);
                
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);
                    NodeList childNodes = node.getChildNodes();
                    
                    ServerInstance instance = new ServerInstance();
                    
                    for (int j = 0; j < childNodes.getLength(); j++) {
                        Node childNode = childNodes.item(j);
                        String childNodeName = childNode.getNodeName();
                        
                        if ((childNode.getNodeType() == Node.ELEMENT_NODE) &&
                                (childNodeName.equalsIgnoreCase("attr"))) { // NOI18N
                            Element attrElement = (Element)childNode;
                            
                            String key = attrElement.getAttribute("name");  // NOI18N
                            String value = attrElement.getAttribute("stringvalue"); // NOI18N
                            
                            if (key.equalsIgnoreCase(ServerInstance.DISPLAY_NAME)) {
                                instance.setDisplayName(value);
                            } else if (key.equalsIgnoreCase(ServerInstance.DOMAIN)) {
                                instance.setDomain(value);
                            } else if (key.equalsIgnoreCase(ServerInstance.HTTP_MONITOR_ON)) {
                                instance.setHttpMonitorOn(value);
                            } else if (key.equalsIgnoreCase(ServerInstance.HTTP_PORT_NUMBER)) {
                                instance.setHttpPortNumber(value);
                            } else if (key.equalsIgnoreCase(ServerInstance.LOCATION)) {
                                instance.setLocation(value);
                            } else if (key.equalsIgnoreCase(ServerInstance.PASSWORD)) {
                                instance.setPassword(value);
                            } else if (key.equalsIgnoreCase(ServerInstance.URL)) {
                                instance.setUrl(value);
                            } else if (key.equalsIgnoreCase(ServerInstance.USER_NAME)) {
                                instance.setUserName(value);
                            }
                        }
                    }
                    
                    if (instance.getHostName() != null && instance.getLocation() != null) {
                        instances.add(instance);              
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
        
        return instances;
    }
    
    
    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        ServerInstanceReader settings = null;
        
        try {
            settings = new ServerInstanceReader(
                    "C:/Documents and Settings/Graj/.netbeans/dev" +  // NOI18N
                    ServerInstanceReader.RELATIVE_FILE_PATH
                    );
            
            List list = settings.getServerInstances();
            java.util.Iterator iterator = list.iterator();
            
            while (iterator.hasNext() == true) {
                ServerInstance instance = (ServerInstance) iterator.next();
                instance.printOut();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
