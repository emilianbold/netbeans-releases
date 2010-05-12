/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.j2ee.deployment.impl;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.modules.j2ee.deployment.config.J2eeModuleAccessor;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInitializationException;
import org.netbeans.modules.j2ee.deployment.profiler.spi.Profiler;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public final class ServerRegistry implements java.io.Serializable {

    private static final Logger LOGGER = Logger.getLogger(ServerRegistry.class.getName());

    public static final String DIR_INSTALLED_SERVERS = "/J2EE/InstalledServers"; //NOI18N
    public static final String DIR_JSR88_PLUGINS = "/J2EE/DeploymentPlugins"; //NOI18N
    public static final String URL_ATTR = InstanceProperties.URL_ATTR;
    public static final String USERNAME_ATTR = InstanceProperties.USERNAME_ATTR;
    public static final String PASSWORD_ATTR = InstanceProperties.PASSWORD_ATTR;
    public static final String TARGETNAME_ATTR = "targetName"; //NOI18N
    public static final String SERVER_NAME = "serverName"; //NOI18N
    private static ServerRegistry instance = null;
    public synchronized static ServerRegistry getInstance() {
        if(instance == null) instance = new ServerRegistry();
        return instance;

        //PENDING need to get this from lookup
        //    return (ServerRegistry) Lookup.getDefault().lookup(ServerRegistry.class);
    }

    /** Utility method that returns true if the ServerRegistry was initialized
     * during the current IDE session and false otherwise.
     */
    public synchronized static boolean wasInitialized () {
        return instance != null && instance.servers != null && instance.instances != null;
    }
    private transient Map<String, Server> servers = null;
    private transient Map instances = null;
    private transient Collection pluginListeners = new HashSet();
    private transient Collection instanceListeners = new ArrayList();
    private transient InstanceListener[] instanceListenersArray;
    private transient PluginInstallListener pluginL;
    private transient InstanceInstallListener instanceL;

    private ServerRegistry() {
        super();
    }

    private synchronized void init() {
        LOGGER.log(Level.FINEST, "Entering registry initialization"); // NOI18N

        if (servers != null && instances != null) {
            return;
        }

        servers = new HashMap();
        instances = new HashMap();

        FileObject dir = FileUtil.getConfigFile(DIR_JSR88_PLUGINS);
        if (dir != null) {
            LOGGER.log(Level.FINE, "Loading server plugins"); // NOI18N
            dir.addFileChangeListener(pluginL = new PluginInstallListener(dir));
            FileObject[] ch = dir.getChildren();
            for (int i = 0; i < ch.length; i++) {
                addPlugin(ch[i]);
            }

            LOGGER.log(Level.FINE, "Loading server instances"); // NOI18N
            dir = FileUtil.getConfigFile(DIR_INSTALLED_SERVERS);
            dir.addFileChangeListener(instanceL = new InstanceInstallListener(dir));
            ch = dir.getChildren();
            for (int i = 0; i < ch.length; i++) {
                addInstance(ch[i]);
            }

            LOGGER.log(Level.FINE, "Finish initializing plugins"); // NOI18N
            List<String> notInitialized = new LinkedList<String>();
            for (Map.Entry<String, Server> entry : serversMap().entrySet()) {
                OptionalDeploymentManagerFactory odmf = entry.getValue().getOptionalFactory();
                if (null != odmf) {
                    try {
                        odmf.finishServerInitialization();
                    } catch (ServerInitializationException sie) {
                        LOGGER.log(Level.INFO, "Server plugin not initialized", sie);
                        notInitialized.add(entry.getKey());                        
                    } catch (RuntimeException ex) {
                        LOGGER.log(Level.WARNING, "Plugin implementation BUG -- Unexpected Exception from finishServerInitialization", ex);
                        notInitialized.add(entry.getKey());
                    }
                }
            }
            serversMap().keySet().removeAll(notInitialized);
        } else {
            LOGGER.log(Level.WARNING, "No DIR_JSR88_PLUGINS folder found, no server plugins will be availabe"); // NOI18N
        }
    }

    private Map<String,Server> serversMap() {
        init();
        return servers;
    }
    private synchronized Map instancesMap() {
        init();
        return instances;
    }

    private void addPlugin(FileObject fo) {
        String name = ""; //NOI18N
        try {
            if (fo.isFolder()) {
                name = fo.getName();
                Server server = null;
                synchronized (this) {
                    if (serversMap().containsKey(name)) {
                        return;
                    }
                    server = new Server(fo);
                    serversMap().put(name, server);
                }
                if (server != null) {
                    firePluginListeners(server, true);
                    fetchInstances(server);
                }
            }
        } catch (Exception e) {
            //LOGGER.log(Level.WARNING, "Plugin installation failed {0}", fo.toString()); //NOI18N
            LOGGER.log(Level.INFO, null, e);
        }
    }

    private void fetchInstances(Server server) {
        FileObject dir = FileUtil.getConfigFile(DIR_INSTALLED_SERVERS);
        FileObject[] ch = dir.getChildren();
        for (int i = 0; i < ch.length; i++) {
            String url = (String) ch[i].getAttribute(URL_ATTR);
            if (url != null && server.handlesUri(url)) {
                addInstance(ch[i]);
            }
        }
    }

    private void removePlugin(FileObject fo) {
        Server server = null;
        synchronized (this) {
            String name = fo.getName();

            server = (Server) serversMap().get(name);
            if (server != null) {
                // remove all registered server instances of the given server type
                ServerInstance[] tmp = getServerInstances();
                for (int i = 0; i < tmp.length; i++) {
                    ServerInstance si = tmp[i];
                    if (server.equals(si.getServer())) {
                        removeServerInstance(si.getUrl());
                    }
                }
            }
            serversMap().remove(name);
        }
        if (server != null) {
            firePluginListeners(server, false);
        }
    }

    class PluginInstallListener extends FileChangeAdapter {
        private final FileObject dir;

        private PluginInstallListener(FileObject dir) {
            this.dir = dir;
        }
        @Override
        public void fileFolderCreated(FileEvent fe) {
            super.fileFolderCreated(fe);
            addPlugin(fe.getFile());
        }
        @Override
        public void fileDeleted(FileEvent fe) {
            super.fileDeleted(fe);
            removePlugin(fe.getFile());
        }
    }

    class InstanceInstallListener extends FileChangeAdapter {
        private final FileObject dir;

        private InstanceInstallListener(FileObject dir) {
            this.dir = dir;
        }
        @Override
        public void fileDataCreated(FileEvent fe) {
            super.fileDataCreated(fe);
            addInstance(fe.getFile());
        }
        // PENDING should support removing of instances?
    }

    public Collection<Server> getServers() {
        return serversMap().values();
    }

    public synchronized Collection getInstances() {
        return new ArrayList(instancesMap().values());
    }

    public synchronized String[] getInstanceURLs() {
        return (String[]) instancesMap().keySet().toArray(new String[instancesMap().size()]);
    }

    public void checkInstanceAlreadyExists(String url) throws InstanceCreationException {
        if (getServerInstance(url) != null) {
            String msg = NbBundle.getMessage(ServerRegistry.class, "MSG_InstanceAlreadyExists", url);
            throw new InstanceCreationException(msg);
        }
    }

    public void checkInstanceExists(String url) {
        if (getServerInstance(url) == null) {
            String msg = NbBundle.getMessage(ServerRegistry.class, "MSG_InstanceNotExists", url);
            throw new IllegalArgumentException(msg);
        }
    }

    public Server getServer(String name) {
        return (Server) serversMap().get(name);
    }

    public void addPluginListener(PluginListener pl) {
        pluginListeners.add(pl);
    }

    public synchronized ServerInstance getServerInstance(String url) {
        return (ServerInstance) instancesMap().get(url);
    }

    public void removeServerInstance(String url) {
        if (url == null)
            return;

        ServerInstance tmp = null;
        synchronized (this) {
            tmp = (ServerInstance) instancesMap().remove(url);
        }
        if (tmp != null) {
            fireInstanceListeners(url, false);
            removeInstanceFromFile(url);
        }
    }

    public synchronized ServerInstance[] getServerInstances() {
        ServerInstance[] ret = new ServerInstance[instancesMap().size()];
        instancesMap().values().toArray(ret);
        return ret;
    }

    public static FileObject getInstanceFileObject(String url) {
        FileObject installedServersDir = FileUtil.getConfigFile(DIR_INSTALLED_SERVERS);
        if (installedServersDir == null) {
            return null;
        }
        FileObject[] installedServers = installedServersDir.getChildren();
        for (int i=0; i<installedServers.length; i++) {
            String val = (String) installedServers[i].getAttribute(URL_ATTR);
            if (val != null && val.equals(url))
                return installedServers[i];
        }
        return null;
    }

    /**
     * Add a new server instance in the server registry.
     *
     * @param  url URL to access deployment manager.
     * @param  username username used by the deployment manager.
     * @param  password password used by the deployment manager.
     * @param  displayName display name wich represents server instance in IDE.
     * @param initialProperties any other properties to set during the instance creation.
     *             If the map contains any of InstanceProperties.URL_ATTR,
     *             InstanceProperties.USERNAME_ATTR, InstanceProperties.PASSWORD_ATTR
     *             or InstanceProperties.DISPLAY_NAME_ATTR they will be ignored
     *             - the explicit parameter values are always used.
     *             <code>null</code> is accepted.
     *
     * @throws InstanceCreationException when instance with same url is already
     *         registered.
     */
    public void addInstance(String url, String username, String password,
            String displayName, boolean withoutUI, Map<String, String> initialproperties) throws InstanceCreationException {
        // should never have empty url; UI should have prevented this
        // may happen when autoregistered instance is removed
        if (url == null || url.equals("")) { //NOI18N
            LOGGER.log(Level.INFO, NbBundle.getMessage(ServerRegistry.class, "MSG_EmptyUrl"));
            return;
        }

        checkInstanceAlreadyExists(url);
        try {
            addInstanceImpl(url, username, password, displayName, withoutUI, initialproperties,true);
        } catch (InstanceCreationException ice) {
            InstanceCreationException e = new InstanceCreationException(NbBundle.getMessage(ServerRegistry.class, "MSG_FailedToCreateInstance", displayName));
            e.initCause(ice);
            throw e;
        }
    }

    private synchronized void writeInstanceToFile(String url, String username, String password) throws IOException {
        if (url == null) {
            Logger.getLogger("global").log(Level.SEVERE, NbBundle.getMessage(ServerRegistry.class, "MSG_NullUrl"));
            return;
        }

        FileObject dir = FileUtil.getConfigFile(DIR_INSTALLED_SERVERS);
        FileObject instanceFOs[] = dir.getChildren();
        FileObject instanceFO = null;
        for (int i=0; i<instanceFOs.length; i++) {
            if (url.equals(instanceFOs[i].getAttribute(URL_ATTR)))
                instanceFO = instanceFOs[i];
        }
        String name = FileUtil.findFreeFileName(dir,"instance",null);
        if (instanceFO == null)
            instanceFO = dir.createData(name);
        instanceFO.setAttribute(URL_ATTR, url);
        instanceFO.setAttribute(USERNAME_ATTR, username);
        instanceFO.setAttribute(PASSWORD_ATTR, password);
    }

    private synchronized void removeInstanceFromFile(String url) {
        FileObject instanceFO = getInstanceFileObject(url);
        if (instanceFO == null)
            return;
        try {
            instanceFO.delete();
        } catch (IOException ioe) {
            Logger.getLogger("global").log(Level.INFO, null, ioe);
        }
    }

    /**
     * Add a new server instance in the server registry.
     *
     * @param url URL to access deployment manager.
     * @param username username used by the deployment manager.
     * @param password password used by the deployment manager.
     * @param displayName display name wich represents server instance in IDE.
     * @param initialProperties any other properties to set during the instance creation.
     *             If the map contains any of InstanceProperties.URL_ATTR,
     *             InstanceProperties.USERNAME_ATTR, InstanceProperties.PASSWORD_ATTR
     *             or InstanceProperties.DISPLAY_NAME_ATTR they will be ignored
     *             - the explicit parameter values are always used.
     *             <code>null</code> is accepted.
     *
     * @return <code>true</code> if the server instance was created successfully,
     *             <code>false</code> otherwise.
     */
    private void addInstanceImpl(String url, String username,
            String password, String displayName, boolean withoutUI,
            Map<String, String> initialProperties, boolean loadPlugins) throws InstanceCreationException {

        if (url == null) {
            // may happen when autoregistered instance is removed
            LOGGER.log(Level.FINE, "Tried to add instance with null url");
        }

        synchronized (this) {
            if (instancesMap().containsKey(url)) {
                throw new InstanceCreationException("already exists");
            }

            LOGGER.log(Level.FINE, "Registering instance {0}", url);

            Map<String, String> properties = cleanInitialProperties(initialProperties);

            Collection serversMap = serversMap().values();
            for (Iterator i = serversMap.iterator(); i.hasNext();) {
                Server server = (Server) i.next();
                try {
                    if (server.handlesUri(url)) {
                        ServerInstance tmp = new ServerInstance(server, url);
                        // PENDING persist url/password in ServerString as well
                        instancesMap().put(url, tmp);
                        // try to create a disconnected deployment manager to see
                        // whether the instance is not corrupted - see #46929
                        writeInstanceToFile(url, username, password);
                        tmp.getInstanceProperties().setProperty(
                                InstanceProperties.REGISTERED_WITHOUT_UI, Boolean.toString(withoutUI));
                        if (displayName != null) {
                            tmp.getInstanceProperties().setProperty(
                                    InstanceProperties.DISPLAY_NAME_ATTR, displayName);
                        }

                        for (Map.Entry<String, String> entry : properties.entrySet()) {
                            tmp.getInstanceProperties().setProperty(entry.getKey(), entry.getValue());
                        }

                        DeploymentManager manager = server.getDisconnectedDeploymentManager(url);
                        // FIXME this shouldn't be called in synchronized block
                        if (manager != null) {
                            fireInstanceListeners(url, true);
                            return; //  true;
                        } else {
                            removeInstanceFromFile(url);
                            instancesMap().remove(url);
                        }
                    }
                } catch (Exception e) {
                    if (instancesMap().containsKey(url)) {
                        removeInstanceFromFile(url);
                        instancesMap().remove(url);
                    }
                    LOGGER.log(Level.INFO, null, e);
                }
            }
        }

        if (loadPlugins) {
            // don't wait for FS event, try to load the plugin now
            FileObject dir = FileUtil.getConfigFile(DIR_JSR88_PLUGINS);
            if (dir != null) {
                for (FileObject fo : dir.getChildren()) {
                    // if it is already registered this is noop
                    addPlugin(fo);
                }
            }

            addInstanceImpl(url, username, password, displayName, withoutUI, initialProperties, false);
            return;
        }

        throw new InstanceCreationException("No handlers for " + url);
    }

    private Map<String, String> cleanInitialProperties(Map<String, String> initialProperties) {
        if (initialProperties == null) {
            return Collections.<String, String>emptyMap();
        }

        Map<String,String> properties = new HashMap(initialProperties);
        properties.remove(InstanceProperties.URL_ATTR);
        properties.remove(InstanceProperties.USERNAME_ATTR);
        properties.remove(InstanceProperties.PASSWORD_ATTR);
        properties.remove(InstanceProperties.DISPLAY_NAME_ATTR);
        properties.remove(InstanceProperties.REGISTERED_WITHOUT_UI);
        return properties;
    }

    public void addInstance(FileObject fo) {
        String url = (String) fo.getAttribute(URL_ATTR);
        String username = (String) fo.getAttribute(USERNAME_ATTR);
        String password = (String) fo.getAttribute(PASSWORD_ATTR);
        String displayName = (String) fo.getAttribute(InstanceProperties.DISPLAY_NAME_ATTR);
        String withoutUI = (String) fo.getAttribute(InstanceProperties.REGISTERED_WITHOUT_UI);
        boolean withoutUIFlag = withoutUI == null ? false : Boolean.valueOf(withoutUI);
        try {
            addInstanceImpl(url, username, password, displayName, withoutUIFlag, null, false);
        } catch (InstanceCreationException ice) {
            // yes... we are ignoring this.. because that
        }
    }

    public Collection getInstances(InstanceListener il) {
        if (il != null) {
            synchronized(instanceListeners) {
                instanceListenersArray = null;
                instanceListeners.add(il);
            }
        }
        return getInstances();
    }

    public void addInstanceListener(InstanceListener il) {
        synchronized(instanceListeners) {
            instanceListenersArray = null;
            instanceListeners.add(il);
        }
    }

    public void removeInstanceListener(InstanceListener il) {
        synchronized(instanceListeners) {
            instanceListenersArray = null;
            instanceListeners.remove(il);
        }
    }

    public synchronized void removePluginListener(PluginListener pl) {
        pluginListeners.remove(pl);
    }

    private void firePluginListeners(Server server, boolean add) {
        LOGGER.log(Level.FINE, "Firing plugin listener"); // NOI18N
        for(Iterator i = pluginListeners.iterator();i.hasNext();) {
            PluginListener pl = (PluginListener)i.next();
            if(add) pl.serverAdded(server);
            else pl.serverRemoved(server);
        }
	configNamesByType = null;
    }


    private InstanceListener[] getInstanceListeners() {
        InstanceListener[]  retValue = null;
        synchronized (instanceListeners) {
            retValue = instanceListenersArray;
            if (retValue == null) {
                retValue = (InstanceListener[])instanceListeners.toArray(new InstanceListener[instanceListeners.size()]);
                instanceListenersArray = retValue;
            }
        }
        return retValue;
    }

    private void fireInstanceListeners(String instance, boolean add) {
        InstanceListener[] instListeners = getInstanceListeners();
        for(int i = 0; i < instListeners.length; i++) {
            if(add) {
                instListeners[i].instanceAdded(instance);
            } else {
                instListeners[i].instanceRemoved(instance);
            }
        }
    }

    public interface PluginListener extends EventListener {

        public void serverAdded(Server name);

        public void serverRemoved(Server name);

    }

    private static HashMap configNamesByType = null;
    private static final J2eeModule.Type[] allTypes = new J2eeModule.Type[] {
        J2eeModule.Type.EAR, J2eeModule.Type.RAR, J2eeModule.Type.CAR, J2eeModule.Type.EJB, J2eeModule.Type.WAR };

    private void initConfigNamesByType() {
        if (configNamesByType != null) {
            return;
        }
        configNamesByType = new HashMap();
        for (int i=0 ; i<allTypes.length; i++) {
            Set configNames = new HashSet();
            for (Iterator j=servers.values().iterator(); j.hasNext();) {
		Server s = (Server) j.next();
		String[] paths = s.getDeploymentPlanFiles(allTypes[i]);
                if (paths == null)
                    continue;
		for (int k=0 ; k<paths.length; k++) {
		    File path = new File(paths[k]);
		    configNames.add(path.getName());
		}
            }
	    configNamesByType.put(allTypes[i], configNames);
        }
    }

    public boolean isConfigFileName(String name, J2eeModule.Type type) {
	initConfigNamesByType();
        Object jsrModuleType = J2eeModuleAccessor.getDefault().getJsrModuleType(type);
	Set configNames = (Set) configNamesByType.get(jsrModuleType);
	return (configNames != null && configNames.contains(name));
    }

    /** Return profiler if any is registered in the IDE, null otherwise. */
    public static Profiler getProfiler() {
        return (Profiler)Lookup.getDefault().lookup(Profiler.class);
    }
}
