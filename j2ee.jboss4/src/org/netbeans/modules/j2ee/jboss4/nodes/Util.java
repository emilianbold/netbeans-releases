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

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ModuleType;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Michal Mocnak
 */
public class Util {
    public static final String WAIT_NODE = "wait_node"; //NOI18N
    public static final String INFO_NODE = "info_node"; //NOI18N

    private static final Logger LOGGER = Logger.getLogger(Util.class.getName());

    /**
     * Lookup a JBoss4 RMI Adaptor
     */
    public static MBeanServerConnection getRMIServer(Lookup lookup) {
        return getRMIServer((JBDeploymentManager)lookup.lookup(JBDeploymentManager.class));
    }

    /**
     * Lookup a JBoss4 RMI Adaptor
     */
    public static MBeanServerConnection getRMIServer(JBDeploymentManager manager) {
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
        n.setIconBaseWithExtension("org/netbeans/modules/j2ee/jboss4/resources/wait.gif"); // NOI18N
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
            Logger.getLogger("global").log(Level.INFO, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        } catch (MalformedObjectNameException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        } catch (NullPointerException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
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
            Logger.getLogger("global").log(Level.INFO, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
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
    public static Object getMBeanParameter(JBDeploymentManager dm, String name, String targetObject) {
        MBeanServerConnection server = dm.refreshRMIServer();
        try {
            return server.getAttribute(new ObjectName(targetObject), name);
        } catch (InstanceNotFoundException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        } catch (AttributeNotFoundException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        } catch (MalformedObjectNameException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        } catch (NullPointerException ex) {
            // it's normal behaviour when the server is not running
        } catch (IllegalArgumentException ex) {
            // it's normal behaviour when the server is not running
        } catch (ReflectionException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        } catch (MBeanException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        } catch (IOException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        }

        return null;
    }

    /*
     * Parse web application's deployment descriptor and returns context root.
     * According to the jboss specification, if no context root specification exists,
     * the context root will be the base name of the WAR file.
     *
     * @param descriptor deployment descriptor
     * @param warName name of the war
     * @return context-root of web application
     */
    public static String getWebContextRoot(String descriptor, String warName) {
        String context = getDescriptorContextRoot(descriptor);
        if (context == null) {
            context = getWarContextRoot(warName);
        }

        if ("/ROOT".equals(context)) {
            return "/";
        }

        return context;
    }

    private static String getDescriptorContextRoot(String descriptor) {
        if (descriptor == null || "".equals(descriptor.trim())) {
            return null;
        }

        Document doc = null;

        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(descriptor)));
        } catch (SAXException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return null;
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return null;
        } catch (ParserConfigurationException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return null;
        }

        org.w3c.dom.Node node = doc.getElementsByTagName("context-root").item(0);

        if (node == null || node.getTextContent() == null) {
            return null;
        }

        String text = node.getTextContent();
        if (!text.startsWith("/")) {
            text = "/" + text;
        }

        return text;
    }

    private static String getWarContextRoot(String warName) {
        if (warName == null) {
            return null;
        }
        if (!warName.endsWith(".war")) {
            return "/" + warName;
        }

        return "/" + warName.substring(0, warName.lastIndexOf(".war"));
    }
}
