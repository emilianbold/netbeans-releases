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

package org.netbeans.modules.j2ee.sun.bridge.apis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;

import javax.management.remote.JMXConnector;
import javax.management.Notification;
import javax.management.MBeanServerConnection;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;

import com.sun.appserv.management.config.StandaloneServerConfig;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.QueryMgr;
import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.config.RARModuleConfig;
import com.sun.appserv.management.config.ResourceConfig;
import com.sun.appserv.management.j2ee.J2EEManagedObject;
import com.sun.appserv.management.j2ee.J2EEDomain;
import com.sun.appserv.management.j2ee.ResourceAdapterModule;

import org.netbeans.modules.j2ee.sun.util.NodeTypes;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.appsrvapi.PortDetector;

import com.sun.enterprise.deployment.backend.DeploymentStatus;
import com.sun.enterprise.deployment.client.DeploymentFacility;
import com.sun.enterprise.deployment.client.ServerConnectionIdentifier;
import com.sun.enterprise.deployment.client.DeploymentFacilityFactory;
import com.sun.enterprise.deployment.client.JESProgressObject;
import java.util.Properties;
import org.netbeans.modules.j2ee.sun.ide.controllers.ControllerUtil;

/**
 * 
 */
public abstract class AppserverMgmtControllerBase 
        implements javax.management.NotificationListener {
    
    private AMX amxObj;
    private ProxyFactory amxProxyFactory;
    private MBeanServerConnection mbeanServerConn;
    private DeploymentManager deployMgr;
    private static Logger logger;
    
    protected AppserverConnectionSource appMgmtConnection;
    protected static final String DAS_SERVER_NAME = "server";
    protected static final String BACKEND_COM_SUN_APPSERV_MBEAN_DOMAIN_NAME =
            "com.sun.appserv";
        
    static {
        logger = Logger.getLogger("org.netbeans.modules.j2ee.sun");
    }
    
    
    /**
     * Create an instance of AppserverMgmtController used in the interaction
     * with AMX API for Sun Java System Application Server. 
     *
     * @param connection An AppserverConnectionSource object used to connect
     *                   to the appserver. 
     */
    public AppserverMgmtControllerBase(final DeploymentManager deploymentMgr,
                final AppserverConnectionSource connection) {
        this.deployMgr = deploymentMgr;
        this.appMgmtConnection = connection;
        initializeAMXConnectorWithInterceptor();
    }
    
    
    /**
     * Create an instance of AppserverMgmtController used in the interaction
     * with AMX API for Sun Java System Application Server. 
     *
     * @param amxObject The amx object that this controller wraps.
     * @param connection An AppserverConnectionSource object used to connect
     *                   to the appserver. 
     */
    public AppserverMgmtControllerBase(final AMX amxObject,
            final DeploymentManager deploymentMgr,
            final AppserverConnectionSource connection) {
        this.deployMgr = deploymentMgr;
        this.appMgmtConnection = connection;
        initializeAMXConnectorWithInterceptor();
        this.amxObj = amxObject;
    }

    
    
    /**
     * Create an instance of AppserverMgmtController used in the interaction
     * with AMX API for Sun Java System Application Server. 
     *
     * @param amxObject The amx object that this controller wraps.
     * @param connection An AppserverConnectionSource object used to connect
     *                   to the appserver. 
     */
    public AppserverMgmtControllerBase(final AMX amxObject, 
            final AppserverConnectionSource connection) {
        appMgmtConnection = connection;
        setupProxyFactory();
        this.amxObj = amxObject;
    }
    
    
    /* getter for AppserverConnectionSource
     * can be used to see if it is null (possible case when a password or 
     * user name is incorrect
     **/
    
    public AppserverConnectionSource getAppserverConnectionSource(){
        return appMgmtConnection;
    }
    /**
     *
     *
     */
    private void initializeAMXConnectorWithInterceptor() {
        try {
            ControllerUtil.checkIfServerInDebugMode(getDeploymentManager());
            JMXConnector jmxConn = 
                appMgmtConnection.getJMXConnector(false);
            jmxConn.addConnectionNotificationListener(this,null,null);
            //setup the proxy factory
            setupProxyFactory();
        } catch (RuntimeException rex) {
            getLogger().log(Level.FINE, rex.getMessage(), rex);
        } catch (Exception ex) {
            getLogger().log(Level.FINE, ex.getMessage(), ex);
        }
    }
    
    
    /**
     * Sets up the local proxyFactory connection.
     */
    private void setupProxyFactory() {
        try {
            ControllerUtil.checkIfServerInDebugMode(getDeploymentManager());
            this.amxProxyFactory = 
                ProxyFactory.getInstance(getMBeanServerConnection()); 
        } catch (RuntimeException rex) {
            getLogger().log(Level.FINE, rex.getMessage(), rex);
        } catch (Exception ex) {
            getLogger().log(Level.FINE, ex.getMessage(), ex);
        }
    }
    
    
    /**
     *
     *
     */
    public void handleNotification(final Notification notification, 
            final Object handback)  {
        try {
            JMXConnector jmxConn = appMgmtConnection.getJMXConnector(false);
            if(jmxConn != null) {
                jmxConn.close();
            } 
        } catch(IOException io) {
            getLogger().log(Level.FINE, io.getMessage(), io);
        }
        appMgmtConnection = null;
    }
    
    
    
    /**
     * Returns the MBeanServerConnection associated with this connection.
     *
     * @return The MBeanServerConnection.
     */
    public MBeanServerConnection getMBeanServerConnection() {
        try {
            if(mbeanServerConn == null) {
                testIfServerInDebugAndLogException();
                mbeanServerConn = 
                    ControllerUtil.getMBeanServerConnWithInterceptor(
                        (SunDeploymentManagerInterface)deployMgr, 
                            appMgmtConnection);
            } 
            testIfServerInDebugAndLogException();
        } catch (RuntimeException rex) {
            getLogger().log(Level.FINE, rex.getMessage(), rex);
        } catch(Exception e) {
            getLogger().log(Level.FINE, e.getMessage(), e);
        }
        return mbeanServerConn;
    }
    
    
    /**
     * Returns the DeploymentManager object from the controller.
     *
     * @return The DeploymentManager.
     */
    public DeploymentManager getDeploymentManager() {
        return deployMgr;
    }
    
    
    /**
     * Returns the name of the component that this controller wraps.
     *
     * @return The name of the component that this controller wraps. 
     */
    public String getName() {
        return getAMXObject().getName();
    }
    
    
    /**
     * Returns the AMX mbean that this controller wraps.
     *
     * @return The AMX mbean that this controller wraps.
     */
    public AMX getAMXObject() {
        return amxObj;
    }
    

    /**
     * Returns the AMX API DomainRoot for the connected appserver. 
     *
     * @return the DomainRoot of the application server.
     */
    final DomainRoot getDomainRoot() {
        testIfServerInDebug();
        return amxProxyFactory.getDomainRoot();       
    }
    
    
    /**
     *
     */
    final DomainConfig getDomainConfig() {
        // stop anything from crossing into AMX if the server is in debug
        testIfServerInDebug();
        return getDomainRoot().getDomainConfig();
    }
    
    
    /**
     * Returns the AMX API DomainRoot for the connected appserver. 
     *
     * @return the DomainRoot of the application server.
     */
    protected final J2EEDomain getJ2EEDomain() {
        return getDomainRoot().getJ2EEDomain();
    }
    
        
    /**
     * Returns the QueryMgr object for this controller.
     *
     * @return The QueryMgr AMX mbean.
     */
    final protected QueryMgr getQueryMgr() {
        return getDomainRoot().getQueryMgr();
    }
    
    
    /**
     * Returns the proxy factory object in AMX. This is used for looking up
     * AMX mbean instances given and objectName or setting a custom 
     * MBeanServerConnection such as the one used for intercepting requests to
     * the server (org.netbeans.modules.j2ee.sun.util.PluginRequestInterceptor).
     *
     * @return The ProxyFactory instance for the current JMX connection.
     */
    final ProxyFactory getAMXProxyFactory() {
        return this.amxProxyFactory;
    }
    
    
    /**
     * Returns the properties of the application given the nodeType and name.
     * 
     * @param nodeType The name of the node.
     * @param amx The amx mbean from which properties will be extracted.
     * @param propsToIgnore Properties to be ignored.
     * @return All the component properties.
     */
    public Map getJ2EEAndConfigProperties(String nodeType,
            AMX j2eeMod, AMX configPeer, List propsToIgnore) {
        try {
            Map propsMap = new HashMap();
            if(configPeer == null && j2eeMod != null){
                configPeer = getConfigPeerByNodeTypeAndName(j2eeMod, nodeType);
            }            
            if(configPeer != null) {
                propsMap = getConfigPropertiesFromBackend(nodeType, 
                        configPeer, propsToIgnore);
                
            }
            if(j2eeMod != null){
                Map j2eeProps =
                        getPropertiesFromBackend(nodeType, j2eeMod, propsToIgnore);
                propsMap.putAll(j2eeProps);
            }
            if(propsMap == null || propsMap.size() == 0) {
                getLogger().log(Level.FINE, "The props in getJ2EE is 0!"); 
            }
            propsMap = ControllerUtil.modifyEnabledProperty(propsMap, configPeer);
            return propsMap;
        } catch (RuntimeException rex) {
            getLogger().log(Level.FINE, rex.getMessage(), rex); 
            return new HashMap();
        } catch (Exception e) {
            getLogger().log(Level.FINE, e.getMessage());
            return new HashMap();
        }
    }

    
    /**
     * Return all the properties for this particular AMX resource.
     *
     * @param nodeType The type of node defined in NodeTypes.
     * @param amx The AMX mbean interface.
     * @param propsToIgnore Properties to be ignored.
     * @return A java.util.Map of Attributes and their respective 
     *         MBeanAttributeInfos.
     */
    protected Map getPropertiesFromBackend(final String nodeType,
            final AMX res, final List propsToIgnore) {
        testIfServerInDebug();
        Class intrface = NodeTypes.getAMXInterface(nodeType);
        return ControllerUtil.getAllAttributes(intrface, res, propsToIgnore, 
                getMBeanServerConnection(), nodeType); 
    }
    
    
    /**
     * Return all the properties for this particular AMX resource.
     *
     * @param nodeType The type of node defined in NodeTypes.
     * @param amx The AMX mbean interface.
     * @param propsToIgnore Properties to be ignored.
     * @return A java.util.Map of Attributes and their respective 
     *         MBeanAttributeInfos.
     */
    protected Map getConfigPropertiesFromBackend(final String nodeType,
            final AMX res, final List propsToIgnore) {
        testIfServerInDebug();
        Class intrface = NodeTypes.getAMXConfigPeerInterface(nodeType);
        return ControllerUtil.getAllAttributes(intrface, res, propsToIgnore,
                getMBeanServerConnection(), nodeType);
    }
    
    
    /**
     * Returns the config peer for the J2EE_TYPE and module name given. This
     * is required since some of the getConfigPeer methods for JSR77 related
     * AMX mbeans are not working as of SJSAS 8.1.
     *
     * @param nodeType The nodeType.
     * @param moduleName The name of the module.
     * @return The AMX module interface for the config peer.
     */
    protected AMX getConfigPeerByNodeTypeAndName(final AMX amx, 
            final String nodeType) {
        testIfServerInDebug();
        J2EEManagedObject managedObj = (J2EEManagedObject) amx;
        AMX configPeer = managedObj.getConfigPeer();
        if(configPeer != null) {
            return configPeer;
        } else {
            final String j2eeType = 
                NodeTypes.getAMXConfigPeerJ2EETypeByNodeType(nodeType);
            java.util.Set set = getQueryMgr().queryJ2EETypeSet(j2eeType);
            for(Iterator itr = set.iterator(); itr.hasNext(); ) {
                AMX config = (AMX) itr.next();
                if(config.getName().equals(amx.getName())) {
                    return config;
                }
            }
        }
       return null;
    }
    
    /**
     * Return all the properties for this particular AMX resource.
     *
     * @param amx The AMX mbean interface.
     * @param propNames Properties to be added.
     * @return A java.util.Map of Attributes and their respective 
     *         MBeanAttributeInfos.
     */
    protected Map getLogPropertiesFromBackend(final AMX res, final Map propNames) {
        testIfServerInDebug();
        return ControllerUtil.getLogAttributes(res, propNames, getMBeanServerConnection()); 
    }
    
    /**
     * Undeploys an application, web module, app client, connector, or ejb
     * module. Currently, this method first removes the reference to the 
     * component using the ServerConfig AMX mbean. Then it uses the
     * AMX DeploymentMgr mbean to undeploy the component. Finally, since there
     * is a bug in the underlying old JMX config mbeans, the corresponding
     * JSR77 mbeans have to be cleaned up (removed) because the backend doesn't
     * currently handle this without passing a target server name to the 
     * undeploy method on com.sun.appserv:type=applications,category=config
     * mbean.
     *
     */
    public void undeploy() {
        testIfServerInDebug();
        DeploymentFacility df = createDeploymentFacility();
        JESProgressObject progressObject = null;
        Properties props = null;    
        
        if((getAMXObject() instanceof ResourceAdapterModule) || 
                (getAMXObject() instanceof RARModuleConfig)){
            props = new Properties();
            props.put("name", getName()); //NOI18N
            props.put("cascade", "true"); //NOI18N
        }
        
        if (df.isConnected()) {
                String[] targetNames = getRelevantTargets(true);
                Target[] targets = df.createTargets(targetNames);
                progressObject = df.undeploy(targets, getName(), props);
                df.waitFor(progressObject);
        }
        // TODO : this looks suspect. resolve
        if (null != progressObject) {
            DeploymentStatus ds = progressObject.getCompletedStatus();
            ds.toString();
        }
    }
    
    public void undeploy(String name) {
        testIfServerInDebug();
        DeploymentFacility df = createDeploymentFacility();
        JESProgressObject progressObject = null;
        Properties props = null;    
        
        if (df.isConnected()) {
            String[] targetNames = getRelevantTargets(true);
            Target[] targets = df.createTargets(targetNames);
            progressObject = df.undeploy(targets, name, props);
            df.waitFor(progressObject);
        }
        // TODO : this looks suspect. resolve
        if (null != progressObject) {
            DeploymentStatus ds = progressObject.getCompletedStatus();
            ds.toString();
        }
    }
    
    private String[] getRelevantTargets(boolean isApp) {
        String[] targetNames = new String[]{"domain"};
        try {
            List<String> instances = ControllerUtil.getDeployedTargets(getAMXObject(), isApp, getMBeanServerConnection());
            if (instances.size() > 0) {
                targetNames = instances.toArray(new String[instances.size()]);
            }
        } catch (Exception ex) {
            getLogger().log(Level.FINE, ex.getMessage(), ex);
        }
        return targetNames;
    }
    
    private DeploymentFacility createDeploymentFacility(){
        DeploymentFacility df = DeploymentFacilityFactory.getDeploymentFacility();
        SunDeploymentManagerInterface sunDplmtMgr = (SunDeploymentManagerInterface)getDeploymentManager();
        ServerConnectionIdentifier conn = createServerConnectionIdentifier(
                sunDplmtMgr.getHost(), sunDplmtMgr.getPort(), sunDplmtMgr.getUserName(), sunDplmtMgr.getPassword());
        df.connect(conn);
        return df;
    }
    
    private ServerConnectionIdentifier createServerConnectionIdentifier(
            String host, int port, String user, String password) {
        ServerConnectionIdentifier conn = 
            new ServerConnectionIdentifier();
        conn.setHostName(host);
        conn.setHostPort(port);
        conn.setUserName(user);
        conn.setPassword(password);
        try{
            if (PortDetector.isSecurePort(host, port)) {
                conn.setSecure(true);
            } else {
                conn.setSecure(false);
            }
        }catch(Exception ex){
            conn.setSecure(false);
        }
        return conn;
    }
    
    
   
    
    /**
     * Removes the server reference to a resource
     *
     * @param resName The name of the resource to be removed as a reference.
     */
    protected void removeResourceRef(ResourceConfig resConfig, String resName) {
        testIfServerInDebug();
        
        Map serverConfigs = ControllerUtil.getStandaloneServerInstancesMap(resConfig);
        for(Iterator itr = serverConfigs.values().iterator(); itr.hasNext(); ) {
            StandaloneServerConfig config = (StandaloneServerConfig)itr.next();
            boolean contains = config.getResourceRefConfigMap().containsKey(resName);
            if(contains) {
                config.removeResourceRefConfig(resName);
            }
        }    
    }
        
    /**
     * Returns the logger for the controller.
     *
     * @returns The java.util.logging.Logger impl. for this controller.
     */
     protected final Logger getLogger() {
          if ( logger == null ) {
               logger = Logger.getLogger("org.netbeans.modules.j2ee.sun");
          }
          return logger;
     }
     
     
    /**
     *
     *
     */
    protected void testIfServerInDebug() {
        try {
            ControllerUtil.checkIfServerInDebugMode(getDeploymentManager());
        } catch (RuntimeException rex) {
            getLogger().log(Level.FINE, rex.getMessage(), rex);
            throw rex;
        } catch(Exception e) {
            getLogger().log(Level.FINE, e.getMessage(), e);
        }
    }
    
    
    /**
     *
     *
     */
    protected void testIfServerInDebugAndLogException() {
        try {
            ControllerUtil.checkIfServerInDebugMode(getDeploymentManager());
        } catch (RuntimeException rex) {
            getLogger().log(Level.FINE, rex.getMessage(), rex);
        } catch(Exception e) {
            getLogger().log(Level.FINE, e.getMessage(), e);
        }
    }

    public boolean isDeployMgrLocal(){   
        return ((SunDeploymentManagerInterface)deployMgr).isLocal();
    }
     
    public boolean isSIPEnabled(){   
        return ControllerUtil.isSIPEnabled(getMBeanServerConnection()); 
    }
    
        
}
