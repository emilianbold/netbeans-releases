/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.javaee.wildfly;

import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.javaee.wildfly.ide.ui.JBPluginProperties;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.modules.javaee.wildfly.ide.ui.JBPluginUtils;
import org.openide.util.NbBundle;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import org.netbeans.modules.javaee.wildfly.ide.ui.JBPluginUtils.Version;

/**
 *
 * @author Petr Hejl
 */
public class WildFlyDeploymentFactory implements DeploymentFactory {

    public static final String URI_PREFIX = "wildfly-deployer:"; // NOI18N
    
    private static final String DISCONNECTED_URI = URI_PREFIX + "http://localhost:8080&"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(WildFlyDeploymentFactory.class.getName());

    /**
     * Mapping of a instance properties to a deployment factory.
     * <i>GuardedBy(WildFlyDeploymentFactory.class)</i>
     */
    private final Map<InstanceProperties, DeploymentFactory> factoryCache =
            new WeakHashMap<InstanceProperties, DeploymentFactory>();

    /**
     * Mapping of a instance properties to a deployment manager.
     * <i>GuardedBy(WildFlyDeploymentFactory.class)</i>
     */
    private final Map<InstanceProperties, WildFlyDeploymentManager> managerCache =
            new WeakHashMap<InstanceProperties, WildFlyDeploymentManager>();

    private final Map<InstanceProperties, WildFlyDeploymentFactory.WildFlyClassLoader> classLoaderCache =
            new WeakHashMap<InstanceProperties, WildFlyDeploymentFactory.WildFlyClassLoader>();

    private static WildFlyDeploymentFactory instance;

    private WildFlyDeploymentFactory() {
        super();
    }

    public static synchronized WildFlyDeploymentFactory getInstance() {
        if (instance == null) {
            instance = new WildFlyDeploymentFactory();
            DeploymentFactoryManager.getInstance().registerDeploymentFactory(instance);

        }

        return instance;
    }

    public static class WildFlyClassLoader extends URLClassLoader {

        public WildFlyClassLoader(URL[] urls, ClassLoader parent) throws MalformedURLException, RuntimeException {
            super(urls, parent);
        }

        protected PermissionCollection getPermissions(CodeSource codeSource) {
            Permissions p = new Permissions();
            p.add(new AllPermission());
            return p;
        }

       public Enumeration<URL> getResources(String name) throws IOException {
           // get rid of annoying warnings
           if (name.indexOf("jndi.properties") != -1) {// || name.indexOf("i18n_user.properties") != -1) { // NOI18N
               return Collections.enumeration(Collections.<URL>emptyList());
           }

           return super.getResources(name);
       }
    }

    public synchronized WildFlyClassLoader getWildFlyClassLoader(InstanceProperties ip) {
        WildFlyClassLoader cl = classLoaderCache.get(ip);
        if (cl == null) {
            DeploymentFactory factory = factoryCache.get(ip);
            if (factory != null && factory.getClass().getClassLoader() instanceof WildFlyClassLoader) {
                cl = (WildFlyClassLoader) factory.getClass().getClassLoader();
            }
            if (cl == null) {
                cl = createWildFlyClassLoader(ip.getProperty(JBPluginProperties.PROPERTY_ROOT_DIR),
                            ip.getProperty(JBPluginProperties.PROPERTY_SERVER_DIR));
            }
            classLoaderCache.put(ip, cl);
        }
        return cl;
    }

    public static WildFlyClassLoader createWildFlyClassLoader(String serverRoot, String domainRoot) {
        try {

            Version jbossVersion = JBPluginUtils.getServerVersion(new File (serverRoot));

            String sep = File.separator;

            File domFile = new File(serverRoot, JBPluginUtils.getModulesBase(serverRoot)
                    + "org" + sep + "dom4j" + sep + "main" + sep + "dom4j-1.6.1.jar"); // NOI18N

            if (!domFile.exists()) {
                domFile = null;
                LOGGER.log(Level.INFO, "No dom4j.jar availabale on classpath"); // NOI18N
            }

            List<URL> urlList = new ArrayList<URL>();

            if (domFile != null) {
                urlList.add(domFile.toURI().toURL());
            }

            File org = new File(serverRoot, JBPluginUtils.getModulesBase(serverRoot) + "org");

            File jboss = new File(org, "jboss");
            File as = new File(jboss, "as");

            if (domFile != null && domFile.exists()) {
                urlList.add(domFile.toURI().toURL());
            }

            urlList.add(new File(serverRoot, "jboss-modules.jar").toURI().toURL());
            urlList.add(new File(serverRoot, "bin" + sep + "client" + sep + "jboss-client.jar").toURI().toURL());

            addUrl(urlList, jboss, "dmr" + sep + "main", Pattern.compile("jboss-dmr-.*.jar"));
            addUrl(urlList, jboss, "logging" + sep + "main", Pattern.compile("jboss-logging-.*.jar"));
            addUrl(urlList, jboss, "marshalling" + sep + "main", Pattern.compile("jboss-marshalling-.*.jar"));
            addUrl(urlList, jboss, "marshalling" + sep + "river" + sep + "main", Pattern.compile("jboss-marshalling-river-.*.jar"));
            addUrl(urlList, jboss, "remoting" + sep + "main", Pattern.compile("jboss-remoting-.*.jar"));
            addUrl(urlList, jboss, "sals" + sep + "main", Pattern.compile("jboss-sasl-.*.jar"));
            addUrl(urlList, jboss, "threads" + sep + "main", Pattern.compile("jboss-threads-.*.jar"));
            addUrl(urlList, as, "controller" + sep + "main", Pattern.compile("wildfly-controller-.*.jar"));
            addUrl(urlList, as, "controller-client" + sep + "main", Pattern.compile("wildfly-controller-client-.*.jar"));
            addUrl(urlList, as, "protocol" + sep + "main", Pattern.compile("wildfly-protocol-.*.jar"));
            addUrl(urlList, jboss, "xnio" + sep + "main", Pattern.compile("xnio-api-.*.jar"));
            addUrl(urlList, jboss, "xnio" + sep + "nio" + sep + "main", Pattern.compile("xnio-nio-.*.jar"));

            WildFlyClassLoader loader = new WildFlyClassLoader(urlList.toArray(new URL[] {}), WildFlyDeploymentFactory.class.getClassLoader());
            return loader;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, null, e);
        }
        return null;
    }

    private static void addUrl(List<URL> result, File root, String path, final Pattern pattern) {
        File folder = new File(root, path);
        File[] children = folder.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return pattern.matcher(name).matches();
            }
        });
        if (children != null) {
            for (File child : children) {
                try {
                    result.add(child.toURI().toURL());
                } catch (MalformedURLException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
        }
    }

    @Override
    public boolean handlesURI(String uri) {
        if (uri != null && uri.startsWith(URI_PREFIX)) {
            return true;
        }

        return false;
    }

    @Override
    public DeploymentManager getDeploymentManager(String uri, String uname, String passwd) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            throw new DeploymentManagerCreationException(NbBundle.getMessage(WildFlyDeploymentFactory.class, "MSG_INVALID_URI", uri)); // NOI18N
        }

        synchronized (WildFlyDeploymentFactory.class) {
            InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
            if (ip != null) {
                WildFlyDeploymentManager dm = managerCache.get(ip);
                if (dm != null) {
                    return dm;
                }
            }

            try {
                DeploymentFactory df = getFactory(uri);
                if (df == null) {
                    throw new DeploymentManagerCreationException(NbBundle.getMessage(WildFlyDeploymentFactory.class, "MSG_ERROR_CREATING_DM", uri)); // NOI18N
                }

                String jbURI = uri;
                try {
                    int index1 = uri.indexOf('#'); // NOI18N
                    int index2 = uri.indexOf('&'); // NOI18N
                    int index = Math.min(index1, index2);
                    jbURI = uri.substring(0, index); // NOI18N
                } catch (Exception e) {
                    LOGGER.log(Level.INFO, null, e);
                }

                // see #228619
                // The default host where the DM is connecting is based on
                // serverHost parameter if it is null it uses InetAddress.getLocalHost()
                // which is however based on hostname. If hostname is not mapped
                // to localhost (the interface where the JB is running) we get
                // an excpetion
                if (jbURI.endsWith("as7")) { // NOI18N
                    jbURI = jbURI + "&serverHost=" // NOI18N
                            + (ip != null ? ip.getProperty(JBPluginProperties.PROPERTY_HOST) : "localhost"); // NOI18N
                }
                WildFlyDeploymentManager dm = new WildFlyDeploymentManager(df, uri, jbURI, uname, passwd);
                if (ip != null) {
                    managerCache.put(ip, dm);
                }
                return dm;
            } catch (NoClassDefFoundError e) {
                DeploymentManagerCreationException dmce = new DeploymentManagerCreationException("Classpath is incomplete"); // NOI18N
                dmce.initCause(e);
                throw dmce;
            }
        }
    }

    @Override
    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            throw new DeploymentManagerCreationException(NbBundle.getMessage(WildFlyDeploymentFactory.class, "MSG_INVALID_URI", uri)); // NOI18N
        }

        try {
            InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
            if (ip == null) {
                // null ip either means that the instance is not registered, or that this is the disconnected URL
                if (!DISCONNECTED_URI.equals(uri)) {
                    throw new DeploymentManagerCreationException("JBoss instance " + uri + " is not registered in the IDE."); // NOI18N
                }
            }

            if (ip != null) {
                String root = ip.getProperty(JBPluginProperties.PROPERTY_ROOT_DIR);
                if (root == null || !new File(root).isDirectory()) {
                    throw new DeploymentManagerCreationException("Non existent server root " + root); // NOI18N
                }
                String server = ip.getProperty(JBPluginProperties.PROPERTY_SERVER_DIR);
                if (server == null || !new File(server).isDirectory()) {
                    throw new DeploymentManagerCreationException("Non existent domain root " + server); // NOI18N
                }
            }
            
            return new WildFlyDeploymentManager(null, uri, null, null, null);
        } catch (NoClassDefFoundError e) {
            DeploymentManagerCreationException dmce = new DeploymentManagerCreationException("Classpath is incomplete"); // NOI18N
            dmce.initCause(e);
            throw dmce;
        }
    }

    public String getProductVersion() {
        return NbBundle.getMessage (WildFlyDeploymentFactory.class, "LBL_JBossFactoryVersion");
    }

    public String getDisplayName() {
        return NbBundle.getMessage(WildFlyDeploymentFactory.class, "SERVER_NAME"); // NOI18N
    }

    private DeploymentFactory getFactory(String instanceURL) {
        DeploymentFactory jbossFactory = null;
        try {
            String jbossRoot = InstanceProperties.getInstanceProperties(instanceURL).
                                    getProperty(JBPluginProperties.PROPERTY_ROOT_DIR);

            String domainRoot = InstanceProperties.getInstanceProperties(instanceURL).
                                    getProperty(JBPluginProperties.PROPERTY_SERVER_DIR);

            // if jbossRoot is null, then we are in a server instance registration process, thus this call
            // is made from InstanceProperties creation -> JBPluginProperties singleton contains
            // install location of the instance being registered
            if (jbossRoot == null) {
                jbossRoot = JBPluginProperties.getInstance().getInstallLocation();
            }

            // if domainRoot is null, then we are in a server instance registration process, thus this call
            // is made from InstanceProperties creation -> JBPluginProperties singleton contains
            // install location of the instance being registered
            if (domainRoot == null) {
                domainRoot = JBPluginProperties.getInstance().getDomainLocation();
            }

            InstanceProperties ip = InstanceProperties.getInstanceProperties(instanceURL);
            synchronized (WildFlyDeploymentFactory.class) {
                if (ip != null) {
                    jbossFactory = (DeploymentFactory) factoryCache.get(ip);
                }
               if (jbossFactory == null) {
                    /*Version version = JBPluginUtils.getServerVersion(new File(jbossRoot));
                    URLClassLoader loader = (ip != null) ? getWildFlyClassLoader(ip) : createWildFlyClassLoader(jbossRoot, domainRoot);
                    if(version!= null && "7".equals(version.getMajorNumber())) {
                        Class<?> c = loader.loadClass("org.jboss.as.ee.deployment.spi.factories.DeploymentFactoryImpl");
                        c.getMethod("register").invoke(null);
                        jbossFactory = (DeploymentFactory) c.newInstance();//NOI18N
                    } else {
                        jbossFactory = (DeploymentFactory) loader.loadClass("org.jboss.deployment.spi.factories.DeploymentFactoryImpl").newInstance();//NOI18N
                    }*/

                    // XXX is this ok ?
                    jbossFactory = this;
                    if (ip != null) {
                        factoryCache.put(ip, jbossFactory);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.INFO, null, e);
        }

        return jbossFactory;
    }

}

