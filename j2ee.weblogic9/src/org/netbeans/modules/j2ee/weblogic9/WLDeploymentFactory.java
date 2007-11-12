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
package org.netbeans.modules.j2ee.weblogic9;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Collections;
import java.util.Enumeration;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;

import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;

import org.openide.util.NbBundle;

/**
 * The main entry point to the plugin. Keeps the required static data for the
 * plugin and returns the DeploymentManagers required for deployment and
 * configuration. Does not directly perform any interaction with the server.
 *
 * @author Kirill Sorokin
 */
public class WLDeploymentFactory implements DeploymentFactory {

    public static final String URI_PREFIX = "deployer:WebLogic:http://"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(WLDeploymentFactory.class.getName());

    /**
     * The singleton instance of the factory
     */
    private static WLDeploymentFactory instance;

    private static final WeakHashMap<InstanceProperties, WLDeploymentManager> managerCache =
            new WeakHashMap<InstanceProperties, WLDeploymentManager>();

    /**
     * The singleton factory method
     *
     * @return the singleton instance of the factory
     */
    public static synchronized DeploymentFactory getInstance() {
        if (instance == null) {
            instance = new WLDeploymentFactory();
            //DeploymentFactoryManager.getInstance().registerDeploymentFactory(instance);
        }
        return instance;
    }

    private static class WLClassLoader extends URLClassLoader {

        public WLClassLoader(URL[] urls, ClassLoader parent) throws MalformedURLException {
            super(urls, parent);
        }

        public void addURL(File f) throws MalformedURLException {
            if (f.isFile()) {
                addURL(f.toURL());
            }
        }

        protected PermissionCollection getPermissions(CodeSource codeSource) {
            Permissions p = new Permissions();
            p.add(new AllPermission());
            return p;
        }

        public Enumeration<URL> getResources(String name) throws IOException {
            // get rid of annoying warnings
            if (name.indexOf("jndi.properties") != -1 || name.indexOf("i18n_user.properties") != -1) { // NOI18N
                return Collections.enumeration(Collections.<URL>emptyList());
            }

            return super.getResources(name);
        }
    }

    private static WLClassLoader loader;

    public static synchronized ClassLoader getWLClassLoader (String serverRoot) {
        if (loader == null) {
            try {
                URL[] urls = new URL[] { new File(serverRoot + "/server/lib/weblogic.jar").toURI().toURL()}; // NOI18N
                loader = new WLClassLoader(urls, WLDeploymentFactory.class.getClassLoader());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, null, e);
            }
        }
        return loader;
    }

    /*package*/ DeploymentManager getVendorDeploymentManager(String uri, String username, String password, String host, String port) throws DeploymentManagerCreationException {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "getDM, uri:" + uri+" username:" + username+" password:"+password+" host:"+host+" port:"+port); // NOI18N
        }

        DeploymentManagerCreationException dmce = null;
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        try {
            String serverRoot = InstanceProperties.getInstanceProperties(uri).
                                    getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
            // if serverRoot is null, then we are in a server instance registration process, thus this call
            // is made from InstanceProperties creation -> WLPluginProperties singleton contains
            // install location of the instance being registered
            if (serverRoot == null)
                serverRoot = WLPluginProperties.getInstance().getInstallLocation();

            ClassLoader loader = getWLClassLoader(serverRoot);
            Thread.currentThread().setContextClassLoader(loader);
            Class helperClazz = Class.forName("weblogic.deploy.api.tools.SessionHelper", false, loader); //NOI18N
            Method m = helperClazz.getDeclaredMethod("getDeploymentManager", new Class [] {String.class,String.class,String.class,String.class}); // NOI18N
            Object o = m.invoke(null, new Object [] {host, port, username, password});
            if (DeploymentManager.class.isAssignableFrom(o.getClass())) {
                return (DeploymentManager) o;
            } else {
                dmce = new DeploymentManagerCreationException ("Instance created by weblogic is not DeploymentManager instance.");
            }
        } catch (Exception e) {
            dmce = new DeploymentManagerCreationException ("Cannot create weblogic DeploymentManager instance.");
            dmce.initCause(e);
        } catch (NoClassDefFoundError err) {
            dmce = new DeploymentManagerCreationException("Cannot create weblogic DeploymentManager instance.");
            dmce.initCause(err);
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }
        throw dmce;
    }

    /*package*/ DeploymentManager getVendorDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        DeploymentManagerCreationException dmce = null;
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        try {
            String serverRoot = InstanceProperties.getInstanceProperties(uri).
                                    getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
            // if serverRoot is null, then we are in a server instance registration process, thus this call
            // is made from InstanceProperties creation -> WLPluginProperties singleton contains
            // install location of the instance being registered
            if (serverRoot == null)
                serverRoot = WLPluginProperties.getInstance().getInstallLocation();

            ClassLoader loader = getWLClassLoader(serverRoot);
            Thread.currentThread().setContextClassLoader(loader);
            Class helperClazz = Class.forName("weblogic.deploy.api.tools.SessionHelper", false, loader); //NOI18N
            Method m = helperClazz.getDeclaredMethod("getDisconnectedDeploymentManager", new Class [] {}); // NOI18N
            Object o = m.invoke(null, new Object [] {});
            if (DeploymentManager.class.isAssignableFrom(o.getClass())) {
                return (DeploymentManager) o;
            } else {
                dmce = new DeploymentManagerCreationException ("Instance created by weblogic is not disconnected DeploymentManager instance.");
            }
        } catch (Exception e) {
            dmce = new DeploymentManagerCreationException ("Cannot create weblogic disconnected DeploymentManager instance.");
            dmce.initCause(e);
        } catch (NoClassDefFoundError err) {
            dmce = new DeploymentManagerCreationException("Cannot create weblogic DeploymentManager instance.");
            dmce.initCause(err);
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }
        throw dmce;
    }

    public boolean handlesURI(String uri) {
        if (uri != null && uri.startsWith(URI_PREFIX)) {
            return true;
        }

        return false;
    }

    public DeploymentManager getDeploymentManager(String uri, String username,
            String password) throws DeploymentManagerCreationException {

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "getDeploymentManager, uri:" + uri+" username:" + username+" password:"+password); // NOI18N
        }

        String[] parts = uri.split(":");                               // NOI18N
        String host = parts[3].substring(2);
        String port = parts[4];
        WLDeploymentManager dm = new WLDeploymentManager(this, uri, username, password, host, port);
        updateManagerCache(dm, uri);
        return dm;
    }

    public DeploymentManager getDisconnectedDeploymentManager(String uri)
            throws DeploymentManagerCreationException {

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "getDisconnectedDeploymentManager, uri:" + uri); // NOI18N
        }

        String[] parts = uri.split(":");                               // NOI18N
        String host = parts[3].substring(2);
        String port = parts[4];
        WLDeploymentManager dm = new WLDeploymentManager(this, uri, host, port);
        updateManagerCache(dm, uri);
        return dm;
    }

    private void updateManagerCache(WLDeploymentManager dm, String uri) {
        InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
        if (managerCache.get(ip) != null) {
            dm.setServerProcess(managerCache.get(ip).getServerProcess());
            dm.setOutputManager(managerCache.get(ip).getOutputManager());
        }
        managerCache.put(ip, dm);
    }

    public String getProductVersion() {
        return NbBundle.getMessage(WLDeploymentFactory.class,
                "TXT_productVersion");                                  // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(WLDeploymentFactory.class,
                "TXT_displayName");                                    // NOI18N
    }
}
