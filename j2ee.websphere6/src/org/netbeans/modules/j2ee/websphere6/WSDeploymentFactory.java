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
package org.netbeans.modules.j2ee.websphere6;

import java.io.*;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.openide.util.NbBundle;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;

import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.netbeans.modules.j2ee.websphere6.util.WSDebug;
import org.netbeans.modules.j2ee.websphere6.WSClassLoader;
import org.netbeans.modules.j2ee.websphere6.WSDeploymentManager.WsVersion;


/**
 * The main entry point to the plugin. Keeps the required static data for the
 * plugin and returns the DeploymentManagers required for deployment and
 * configuration. Does not directly perform any interaction with the server.
 *
 * @author Kirill Sorokin
 */
public class WSDeploymentFactory implements DeploymentFactory {
    
    // additional properties that are stored in the InstancePropeties object
    public static final String SERVER_ROOT_ATTR = "serverRoot";        // NOI18N
    public static final String DOMAIN_ROOT_ATTR = "domainRoot";        // NOI18N
    public static final String IS_LOCAL_ATTR = "isLocal";              // NOI18N
    public static final String HOST_ATTR = "host";                     // NOI18N
    public static final String PORT_ATTR = "port";                     // NOI18N
    public static final String DEBUGGER_PORT_ATTR = "debuggerPort";    // NOI18N
    public static final String SERVER_NAME_ATTR = "serverName";        // NOI18N
    public static final String CONFIG_XML_PATH = "configXmlPath";      // NOI18N
    public static final String ADMIN_PORT_ATTR = "adminPort";          // NOI18N
    
    public static final String USERNAME_ATTR = "username";
    public static final String PASSWORD_ATTR = "password";
    public static final String DEFAULT_HOST_PORT_ATTR="defaultHostPort";

    public final WsVersion version;
    
    /**
     * The singleton instance of the factory
     */
    private static WSDeploymentFactory instance60;
    private static WSDeploymentFactory instance61;

    private WSClassLoader loader;

    private HashMap<String, DeploymentFactory> factories = new HashMap<String, DeploymentFactory>();
    private HashMap<String, DeploymentManager> managers = new HashMap<String, DeploymentManager>();

    private WSDeploymentFactory(WsVersion version) {
        this.version = version;
    }
    
    /**
     * Factory method to create DeploymentFactory for WAS 6.0
     */
    public static synchronized WSDeploymentFactory create60() {
        if (instance60 == null) {
            instance60 = new WSDeploymentFactory(WsVersion.WS_60);
            DeploymentFactoryManager.getInstance().registerDeploymentFactory(instance60);
        }
        return instance60;
    }

    /**
     * Factory method to create DeploymentFactory for WAS 6.1
     */
    public static synchronized WSDeploymentFactory create61() {
	 if (instance61 == null) {
            instance61 = new WSDeploymentFactory(WsVersion.WS_61);
            DeploymentFactoryManager.getInstance().registerDeploymentFactory(instance61);
        }
        return instance61;
    }


    
    ////////////////////////////////////////////////////////////////////////////
    // DeploymentFactory implementation
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Returns a wrapper for the connected deployment manager
     * 
     * @return a connected DeploymentManager implementation
     */
    public DeploymentManager getDeploymentManager(String uri, String username, 
            String password) throws DeploymentManagerCreationException {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("getDeploymentManager(" + uri + ", " +      // NOI18N
                    username + ", " + password + ")");                 // NOI18N
 
        DeploymentManager manager = managers.get(uri);
        
        if (null == manager) {
            manager = new WSDeploymentManager(uri);
            managers.put(uri, manager);
        }
        
        return manager; 
    }
    
    /**
     * Returns a wrapper for the disconnecter deployment manager
     * 
     * @return a disconnected DeploymentManager implementation
     */
    public DeploymentManager getDisconnectedDeploymentManager(String uri) 
            throws DeploymentManagerCreationException {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("getDisconnectedDeploymentManager(" + uri + // NOI18N
                    ")");                                              // NOI18N
      
        // return a new deployment manager
        return getDeploymentManager(uri,null,null);
    }
        
   public DeploymentFactory getWSDeploymentFactory(String uri) {
        if (WSDebug.isEnabled())
            System.out.println("getWSDeploymentFactory");

        DeploymentFactory factory = factories.get(uri);

        if (null == factory) {
            InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);

            String serverRoot = null;
            String domainRoot = null;
            if (null != ip) {
                serverRoot = ip.getProperty(SERVER_ROOT_ATTR);
                domainRoot = ip.getProperty(DOMAIN_ROOT_ATTR);
            }

            if (null == serverRoot)
                serverRoot = NbPreferences.forModule(WSDeploymentFactory.class).get(SERVER_ROOT_ATTR, "");

            if (null == domainRoot)
                domainRoot = NbPreferences.forModule(WSDeploymentFactory.class).get(DOMAIN_ROOT_ATTR, "");
            
            if (WSDebug.isEnabled())
                System.out.println("loadDeplomentFactory: serverRoot=" + serverRoot);

            loader = WSClassLoader.getInstance(serverRoot,domainRoot);
            loader.updateLoader();

            try {
                 factory = (DeploymentFactory) loader.loadClass(
                        "com.ibm.ws.management.application.j2ee." +    // NOI18N
                        "deploy.spi.factories.DeploymentFactoryImpl"). // NOI18N
                        newInstance();
            } catch (ClassNotFoundException e) {
                Logger.getLogger("global").log(Level.SEVERE, null, e);
            } catch (InstantiationException e) {
                Logger.getLogger("global").log(Level.SEVERE, null, e);
            } catch (IllegalAccessException e) {
                Logger.getLogger("global").log(Level.SEVERE, null, e);
            } finally {
                loader.restoreLoader();
            }

            factories.put(uri, factory);
        }

        return factory;
    }


    
    /**
     * Tells whether this deployment factory is capable to handle the server
     * identified by the given URI
     * 
     * @param uri the server URI
     * @return can or cannot handle the URI
     */
    public boolean handlesURI(String uri) {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("handlesURI(" + uri + ")");                 // NOI18N
        
        //return uri == null ? false : uri.startsWith(
        return uri == null ? false : (uri.indexOf(WSURIManager.WSURI)>-1);                                // NOI18N
    }
    
    /**
     * Returns the product version of the deployment factory
     * 
     * @return the product version
     */
    public String getProductVersion() {
        return NbBundle.getMessage(WSDeploymentFactory.class, 
                "TXT_productVersion");                                 // NOI18N
    }
    
    /**
     * Returns the deployment factory dysplay name
     * 
     * @return display name
     */
    public String getDisplayName() {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("getDisplayName()");                        // NOI18N
        
	switch (version) {
            case WS_60:
        	return NbBundle.getMessage(WSDeploymentFactory.class, 
                "TXT_displayName60");                                    // NOI18N
            case WS_61:
	    default:
        	return NbBundle.getMessage(WSDeploymentFactory.class, 
                "TXT_displayName61");                                    // NOI18N
	}
    }
}
