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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.jboss4.nodes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.Set;
import javax.enterprise.deploy.shared.ModuleType;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentFactory;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Michal Mocnak
 */
public class Util {
    public static final String WAIT_NODE = "wait_node"; //NOI18N
    public static final String INFO_NODE = "info_node"; //NOI18N
    
    /**
     * Lookup a JBoss4 RMI Adaptor
     */
    public static Object getRMIServer(Lookup lookup) {
        return getRMIServer((JBDeploymentManager)lookup.lookup(JBDeploymentManager.class));
    }
    
    /**
     * Lookup a JBoss4 RMI Adaptor
     */
    public static Object getRMIServer(JBDeploymentManager manager) {
        return manager.getRMIServer();
    }
    
    /* Creates and returns the instance of the node
     * representing the status 'WAIT' of the node.
     * It is used when it spent more time to create elements hierarchy.
     * @return the wait node.
     */
    public static Node createWaitNode() {
        AbstractNode n = new AbstractNode(Children.LEAF);
        n.setName(NbBundle.getMessage(JBItemNode.class, "LBL_WaitNode_DisplayName")); //NOI18N
        n.setIconBaseWithExtension("org/openide/src/resources/wait.gif"); // NOI18N
        return n;
    }
    
    /* Creates and returns the instance of the node
     * representing the status 'INFO' of the node.
     * It is used when it spent more time to create elements hierarchy.
     * @return the wait node.
     */
    public static Node createInfoNode() {
        AbstractNode n = new AbstractNode(Children.LEAF);
        n.setName(NbBundle.getMessage(JBItemNode.class, "LBL_InfoNode_DisplayName")); //NOI18N
        n.setShortDescription(NbBundle.getMessage(JBItemNode.class, "LBL_InfoNode_ToolTip")); //NOI18N
        n.setIconBaseWithExtension("org/netbeans/core/resources/exception.gif"); // NOI18N
        return n;
    }
    
    /*
     * Checks if the Jboss installation has installed remote management package
     *
     * @return is remote management supported
     */
    public static boolean isRemoteManagementSupported(Lookup lookup) {
        
        try {
            Object server = Util.getRMIServer(lookup);
            ObjectName searchPattern;
            searchPattern = new ObjectName("jboss.management.local:*");
            Set managedObj = (Set)server.getClass().getMethod("queryMBeans", new Class[] {ObjectName.class, QueryExp.class}).invoke(server, new Object[] {searchPattern, null});
            
            if(managedObj.size() == 0)
                return false;
        } catch (SecurityException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (IllegalArgumentException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (IllegalAccessException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (InvocationTargetException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (NoSuchMethodException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (MalformedObjectNameException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (NullPointerException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        return true;
    }
    
    /*
     * Checks if the specified object is deployed in JBoss Application Server
     *
     * @return if specified object is deployed
     */
    public static boolean isObjectDeployed(Object server, ObjectName searchPattern) {
        try {
            Set managedObj = (Set)server.getClass().getMethod("queryMBeans", new Class[] {ObjectName.class, QueryExp.class}).invoke(server, new Object[] {searchPattern, null});
            
            if(managedObj.size() > 0)
                return true;
        } catch (IllegalArgumentException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (SecurityException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (InvocationTargetException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (IllegalAccessException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (NoSuchMethodException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        return false;
    }
    
    /*
     * It only returns string representation of the ModuleType (accorded to the JBoss JMX requirements)
     *
     * @return string representation of the ModuleType
     */
    public static String getModuleTypeString(ModuleType mt) {
        if(mt.equals(ModuleType.EAR))
            return "J2EEApplication";
        else if(mt.equals(ModuleType.WAR))
            return "WebModule";
        else if(mt.equals(ModuleType.EJB))
            return "EJBModule";
        
        return "undefined";
    }
    
    /*
     * Returns MBean attribute which you can specify via method parameters
     *
     * @return MBean attribute
     */
    public static Object getMBeanParameter(JBDeploymentManager dm, String name, String targetObject, Class paramClass) {
        ClassLoader oldLoader = null;
        InstanceProperties ip = dm.getInstanceProperties();
        
        try{
            oldLoader = Thread.currentThread().getContextClassLoader();

            URLClassLoader loader = JBDeploymentFactory.getJBClassLoader(ip.getProperty(JBPluginProperties.PROPERTY_ROOT_DIR), 
                    ip.getProperty(JBPluginProperties.PROPERTY_SERVER_DIR));
            Thread.currentThread().setContextClassLoader(loader);
            
            Object srv = dm.refreshRMIServer();
            
            Class objectName = loader.loadClass("javax.management.ObjectName"); // NOI18N
            Method getInstance = objectName.getMethod("getInstance", new Class[] {String.class} );          // NOI18N
            Object target = getInstance.invoke(null, new Object[]{targetObject});       // NOI18N
            Class[] params = new Class[]{loader.loadClass("javax.management.ObjectName"), paramClass};    // NOI18N
            Method getAttribute = srv.getClass().getMethod("getAttribute", params);                 // NOI18N
            return getAttribute.invoke(srv, new Object[]{target, name});       // NOI18N
            
        } catch (NullPointerException ex) {
            // It's normal behaviour when the server is off
            return null;
        } catch (IllegalAccessException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (ClassNotFoundException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (NoSuchMethodException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (InvocationTargetException ex) {
            // It's normal behaviour when the server is off
            return null;
        } finally{
            if (oldLoader != null) {
                Thread.currentThread().setContextClassLoader(oldLoader);
            }
        }
        
        return null;
    }
    
    /*
     * Parse web application's deployment descriptor and returns context root
     *
     * @return context-root of web application
     */
    public static String getWebContextRoot(String dd) {
        Document doc = null;
        
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(dd.getBytes()));
        } catch (SAXException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        } catch (ParserConfigurationException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        }
        
        org.w3c.dom.Node node = doc.getElementsByTagName("context-root").item(0);
        
        return node.getTextContent();
    }
}
