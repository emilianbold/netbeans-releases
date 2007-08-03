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

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.io.File;
import java.net.InetAddress;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.netbeans.modules.sun.manager.jbi.management.AdministrationService;
import org.netbeans.modules.sun.manager.jbi.management.JBIComponentConfigurator;
import org.netbeans.modules.sun.manager.jbi.management.JBIFrameworkService;
import org.netbeans.modules.sun.manager.jbi.management.connectors.HTTPServerConnector;
import org.netbeans.modules.sun.manager.jbi.management.JBIClassLoader;
import org.netbeans.modules.sun.manager.jbi.util.ComparableAttribute;
import org.netbeans.modules.sun.manager.jbi.util.ServerInstance;
import org.netbeans.modules.sun.manager.jbi.util.ServerInstanceReader;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author jqian
 */
public class AppserverJBIMgmtController {
    
    private MBeanServerConnection mBeanServerConnection;
    
    private AdministrationService adminService;
    
    private static final String HOST_MBEAN_NAME =
            "com.sun.appserv:name=server,type=virtual-server,category=monitor,server=server"; // NOI18N
    private static final String HOST_ASADMIN_MBEAN_NAME = 
            "com.sun.appserv:type=Host,host=__asadmin"; // NOI18N
    private static final String HTTP_PORT_MBEAN_NAME =
            "com.sun.appserv:type=http-listener,id=http-listener-1,config=server-config,category=config";  // NOI18N
    
    
    /**
     * Creates a new instance of JBIAppserverMgmtController
     */
    public AppserverJBIMgmtController(MBeanServerConnection mbeanServerConn) {
        this.mBeanServerConnection = mbeanServerConn;
    }
        
    /**
     *
     */
    public boolean isJBIFrameworkEnabled() {
        JBIFrameworkService service = getJBIFrameworkService();
        return service.isJbiFrameworkEnabled();
    }
    
    public JBIFrameworkService getJBIFrameworkService() {
        return new JBIFrameworkService(mBeanServerConnection);
    }
    
    /**
     *
     * @return
     */
    public AdministrationService getJBIAdministrationService() {
        
        // The MBeanServerConnection we get from NetBeans
        // (org.netbeans.modules.j2ee.sun.util.PluginRequestInterceptor)
        // doesn't provide good error message when the RPC fails.
        // The wrapped one (com.sun.enterprise.admin.jmx.remote.internal.RemoteMBeanServerConnection)
        // doesn't do the job either.
        // See ControllerUtil.java in org.netbeans.modules.j2ee.sun.ide.controllers
        // in appserverplugin.
        
        // Use HTTPServerConnector instead.
        
//        MBeanServerConnection connection = getMBeanServerConnection();
//        AdministrationService adminService = new AdministrationService(connection);
        
//        DeploymentManager deploymentManager = controller.getDeploymentManager();
//        if (deploymentManager instanceof SunDeploymentManagerInterface) {
//            SunDeploymentManager sunDeploymentManager = (SunDeploymentManager) deploymentManager;
//            String host = sunDeploymentManager.getHost();
//            String userName = sunDeploymentManager.getUserName();
//            String password = sunDeploymentManager.getPassword();
//            String adminPortNumber = sunDeploymentManager.getAdminPortNumber();
//        }
        
        if (adminService == null) {
            String netBeansUserDir = System.getProperty("netbeans.user"); // NOI18N
            
            try {
                if (netBeansUserDir != null) {
                    ServerInstance serverInstance = null;
                    
                    String settingsFileName = 
                            netBeansUserDir + ServerInstanceReader.RELATIVE_FILE_PATH;
                    File settingsFile = new File(settingsFileName);
                    if (settingsFile.exists()) {
                        //System.out.println("Retrieving settings from " + settingsFileName);
                        ServerInstanceReader settings = 
                                new ServerInstanceReader(settingsFileName);                        
                        for (ServerInstance instance : settings.getServerInstances()) {
                            if (isCurrentInstance(instance)) {
                                serverInstance = instance;
                                break;
                            }
                        }
                    }
                    
                    if (serverInstance == null) {
                        throw new RuntimeException(
                                "The application server definition file " +  // NOI18N
                                settingsFileName +
                                " is missing or does not contain the expected server instance." // NOI18N
                                );
                    }
                    JBIClassLoader jbiClassLoader = new JBIClassLoader(serverInstance);
                    
                    String hostName = serverInstance.getHostName();
                    String port     = serverInstance.getAdminPort();
                    String userName = serverInstance.getUserName();
                    String password = serverInstance.getPassword();                    
                    
                    HTTPServerConnector httpConnector = new HTTPServerConnector(
                            hostName, port, userName, password, jbiClassLoader);
                    
                    adminService = new AdministrationService(httpConnector);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }              
        
        if (adminService == null) {
            String msg = NbBundle.getMessage(getClass(), "NULL_ADMIN_SERVICE_MSG"); // NOI18N
            NotifyDescriptor d = 
                    new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
        
        return adminService;
    }
        
    public Map<Attribute, MBeanAttributeInfo> getJBIComponentConfigProperties(
            String containerType, String componentName, boolean sort)
            throws Exception {
        JBIComponentConfigurator configurator =
                getComponentConfigurator(containerType, componentName);
        Map<Attribute, MBeanAttributeInfo> propertyMap = configurator.getPropertyMap();
        return sort ? getSortedPropertyMap(propertyMap) : propertyMap;
    }
    
    public Map<Attribute, MBeanAttributeInfo> getJBIComponentLoggerProperties(
            String componentName, boolean sort)
            throws Exception {
        AdministrationService adminService = getJBIAdministrationService();
        Map propertyMap = adminService.getComponentLoggerProperties(componentName);
        return sort ? getSortedPropertyMap(propertyMap) : propertyMap;
    }
    
    public Map<Attribute, MBeanAttributeInfo> getJBIComponentIdentificationProperties(
            String componentName, boolean sort) throws Exception {
        AdministrationService adminService = getJBIAdministrationService();
        Map propertyMap = adminService.getComponentIdentificationProperties(componentName);
        return sort ? getSortedPropertyMap(propertyMap) : propertyMap;
    }
    
    public Map<Attribute, MBeanAttributeInfo> getSharedLibraryIdentificationProperties(
            String componentName, boolean sort) throws Exception {
        AdministrationService adminService = getJBIAdministrationService();
        Map propertyMap = adminService.getSharedLibraryIdentificationProperties(componentName);
        return sort ? getSortedPropertyMap(propertyMap) : propertyMap;
    }
    
    private Map<Attribute, MBeanAttributeInfo> getSortedPropertyMap(
            Map<Attribute, MBeanAttributeInfo> propertyMap) {
        Map<Attribute, MBeanAttributeInfo> sortedMap =
                new TreeMap<Attribute, MBeanAttributeInfo>();
        
        Set<Attribute> attrSet = propertyMap.keySet();
        for (Attribute attr : attrSet) {
            MBeanAttributeInfo info = (MBeanAttributeInfo) propertyMap.get(attr);
            sortedMap.put(new ComparableAttribute(attr), info);
        }
        return sortedMap;
    }
    
    /**
     *
     * @param containerType
     * @param componentName
     * @param attrName
     * @return
     */
// TMP
    public Object getJBIComponentConfigPropertyValue(String containerType,
            String componentName, String attrName) throws Exception {
        
        JBIComponentConfigurator configurator =
                getComponentConfigurator(containerType, componentName);
        
        return configurator.getPropertyValue(attrName);
    }
    
    /**
     *
     * @param containerType
     * @param componentName
     * @param attrName
     * @param value
     */
    public void setJBIComponentConfigProperty(String containerType,
            String componentName, String attrName, Object value) 
            throws Exception {
        
        JBIComponentConfigurator configurator =
                getComponentConfigurator(containerType, componentName);
        
        configurator.setPropertyValue(attrName, value);
    }
    
    public void setJBIComponentLoggerProperty(
            String componentName, String attrName, Object value) 
            throws Exception {
        AdministrationService adminService = getJBIAdministrationService();
        adminService.setComponentLoggerProperty(componentName, attrName, value);
    }
    
    public JBIComponentConfigurator getComponentConfigurator(
            String containerType, String componentName) {
        try {
            return new JBIComponentConfigurator(containerType, componentName,
                    mBeanServerConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private boolean isCurrentInstance(ServerInstance instance) {
        
        boolean isLocalHost = false;
        String instanceHost = instance.getHostName();
        if (instanceHost.equals("localhost")) { // NOI18N
            instanceHost = getHostName();
            isLocalHost = true;
        }
        
        try {            
            ObjectName objectName = new ObjectName(HOST_MBEAN_NAME);
            
            String host = (String) mBeanServerConnection.getAttribute(objectName, "hosts-current");  // NOI18N
            if (host.toLowerCase().startsWith(instanceHost.toLowerCase()) ||
                    instanceHost.toLowerCase().startsWith(host.toLowerCase())) {    // FIXME: domain name
                objectName = new ObjectName(HOST_ASADMIN_MBEAN_NAME);
                String appBase = (String) mBeanServerConnection.getAttribute(objectName, "appBase");    // NOI18N
                                
                // For local domains, use instance LOCATION instead of url location  (#90749)
                String localInstanceLocation = instance.getLocation();
                assert localInstanceLocation != null;                
                localInstanceLocation = localInstanceLocation.replace('\\', '/'); // NOI18N
                        
                // FIXME
                // For remote host, there is no point checking the server path.
                // Here I am assuming there is no two remote servers running on
                // the same machine.
                
                if (!isLocalHost || appBase.toLowerCase().startsWith(localInstanceLocation.toLowerCase())) {
                    objectName = new ObjectName(HTTP_PORT_MBEAN_NAME);
                    String port = (String) mBeanServerConnection.getAttribute(objectName, "port");  // NOI18N
                    String instanceHttpPort = instance.getHttpPortNumber();
                    if (port.equals(instanceHttpPort)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    private static String getHostName() {
        String hostName = null;
        try {
            InetAddress localMachine = InetAddress.getLocalHost();
            hostName = localMachine.getHostName();
        } catch (java.net.UnknownHostException e) {
            e.printStackTrace();
        }
        
        return hostName;
    }
}