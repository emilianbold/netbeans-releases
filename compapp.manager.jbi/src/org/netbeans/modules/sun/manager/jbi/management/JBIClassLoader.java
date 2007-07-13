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

package org.netbeans.modules.sun.manager.jbi.management;

import org.netbeans.modules.sun.manager.jbi.util.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;

/**
 * ClassLoader for AppServer interface, see org.netbeans.modules.j2ee.sun.api.ExtendedClassLoader
 */
public class JBIClassLoader extends URLClassLoader {
    
    public static final String INSTALL_ROOT_PROP_NAME = "com.sun.aas.installRoot"; // NOI18N
    
    /**
     * Creates new JbiClassLoader
     */
    public JBIClassLoader() throws MalformedURLException, RuntimeException {
        super(new URL[0]);
    }
    
    public JBIClassLoader(ClassLoader loader) throws MalformedURLException, RuntimeException {
        super(new URL[0], loader);
    }
    
    public JBIClassLoader(ServerInstance instance) throws MalformedURLException, RuntimeException {
        this();
        init(instance);
    }
    
    public JBIClassLoader(ServerInstance instance, ClassLoader loader) throws MalformedURLException, RuntimeException {
        this(loader);
        init(instance);
    }
    
    public void addURL(File f) throws MalformedURLException, RuntimeException {
        if (f.isFile()) {
            addURL(f.toURI().toURL());
        }
    }
    
    protected PermissionCollection getPermissions(CodeSource _cs) {
        Permissions p = new Permissions();
        p.add(new AllPermission());
        return p;
    }
    
    private void init(ServerInstance instance) throws MalformedURLException {
        
        // Retrieve the jar files required to make the HTTPServerConnection.
        String serverInstanceLocation = instance.getLocation();
        
        // For remote server, the server instance location is
        // not defined in the config file.
        if (serverInstanceLocation == null || serverInstanceLocation.trim().length() == 0) {
            // We can get the local server installation location
            // from the url string.
            serverInstanceLocation = instance.getUrlLocation();
        }
        
        String appserv_rt_jar = serverInstanceLocation + "/lib/appserv-rt.jar"; // NOI18N
        
        File appserv_rt_jarFile = new File(appserv_rt_jar);
        if (appserv_rt_jarFile.exists()) {
            addURL(appserv_rt_jarFile);
        } else {
            throw new RuntimeException("JbiClassLoader: Cannot find " + appserv_rt_jar + ".");
        }
        
        String jbi_rt_jar_90 = serverInstanceLocation + "/addons/jbi/lib/jbi_rt.jar"; // NOI18N // Glassfish 9.0
        String jbi_rt_jar_91 = serverInstanceLocation + "/jbi/lib/jbi_rt.jar"; // NOI18N // Glassfish 9.1
        
        File jbi_rt_jarFile = new File(jbi_rt_jar_90);
        if (jbi_rt_jarFile.exists()) {
            addURL(jbi_rt_jarFile);
        } else {
            jbi_rt_jarFile = new File(jbi_rt_jar_91);
            if (jbi_rt_jarFile.exists()) {
                addURL(jbi_rt_jarFile);
            } else {
                throw new RuntimeException("JbiClassLoader: Cannot locate " +
                        jbi_rt_jar_90 + " or " + jbi_rt_jar_91 + ".");
            }
        }
    }
    
    /*
    private String getJbiHome(String serverInstanceLocation) {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
//            builderFactory.setValidating(false);
//            builderFactory.setExpandEntityReferences(false);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
     
            String domloc = serverInstanceLocation
                    + "/domains/domain1/config/domain.xml"; // NOI18N
            File domFile = new File(domloc);
            if (domFile.exists()) {
                Document doc = builder.parse(domFile);
                NodeList lcNodeList = doc
                        .getElementsByTagName("lifecycle-module"); // NOI18N
                for (int i = 0, isize = lcNodeList.getLength(); i < isize; i++) {
                    Node lcNode = lcNodeList.item(i);
                    NodeList children = lcNode.getChildNodes();
                    for (int j = 0, jsize = children.getLength(); j < jsize; j++) {
                        Node child = children.item(j);
                        if (child.getNodeName().equalsIgnoreCase("property")) { // NOI18N
                            NamedNodeMap map = child.getAttributes();
                            String name = map.getNamedItem("name")
                            .getNodeValue(); // NOI18N
                            String value = map.getNamedItem("value")
                            .getNodeValue(); // NOI18N
                            if (name.equalsIgnoreCase("com.sun.jbi.home")) { // NOI18N
                                return value;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error parsing XML: " + e);
        }
        return null;
    }
     */
}
