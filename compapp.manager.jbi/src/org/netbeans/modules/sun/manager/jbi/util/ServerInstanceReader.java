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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
    
    public static final String RELATIVE_FILE_PATH = "/config/J2EE/InstalledServers/.nbattrs"; // NOI18N
    private static final String NB_DEFAULT_ATTRS_DTD = "xml/entities/NetBeans/DTD_DefaultAttributes_1_0"; // NOI18N
    private static final String NB_DEFAULT_ATTRS_PUBLIC_ID = "-//NetBeans//DTD DefaultAttributes 1.0//EN"; // NOI18N
    
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
     * Gets a list of server instances in the server config file.
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
                    
                    instances.add(instance);                    
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
