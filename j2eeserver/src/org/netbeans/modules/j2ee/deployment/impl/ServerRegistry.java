/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.impl;

import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.openide.filesystems.*;
import org.openide.*;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import java.util.*;
import java.io.*;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.openide.modules.InstalledFileLocator;

//import java.util.logging.*;

public final class ServerRegistry implements java.io.Serializable {
    
    public static final String DIR_INSTALLED_SERVERS = "/J2EE/InstalledServers"; //NOI18N
    public static final String DIR_JSR88_PLUGINS = "/J2EE/DeploymentPlugins"; //NOI18N
    public static final String URL_ATTR = InstanceProperties.URL_ATTR;
    public static final String USERNAME_ATTR = InstanceProperties.USERNAME_ATTR;
    public static final String PASSWORD_ATTR = InstanceProperties.PASSWORD_ATTR;
    public static final String FILE_DEFAULT_INSTANCE = "DefaultInstance.settings"; //NOI18N
    public static final String J2EE_DEFAULT_SERVER = "j2ee.defaultServer"; //NOI18N
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
    private transient Map servers = null;
    private transient Map instances = null;
    private transient Collection pluginListeners = new HashSet();
    private transient Collection instanceListeners = new ArrayList();
    private transient InstanceListener[] instanceListenersArray;
    
    // This is the serializable portion of ServerRegistry
    private ServerString defaultInstance;
    
    public ServerRegistry() {
    }
    private synchronized void init() {
        if (servers != null && instances != null)
            return;
        //long t0 = System.currentTimeMillis();
        servers = new HashMap();
        instances = new HashMap();
        Repository rep = (Repository) Lookup.getDefault().lookup(Repository.class);
        FileObject dir = rep.getDefaultFileSystem().findResource(DIR_JSR88_PLUGINS);
        dir.addFileChangeListener(new PluginInstallListener());
        FileObject[] ch = dir.getChildren();
        for(int i = 0; i < ch.length; i++) {
            //long t1=System.currentTimeMillis();
            addPlugin(ch[i]);
            //System.out.println("ServerRegistry.addPlugin("+ch[i]+")="+(System.currentTimeMillis()-t1));
        }
        
        dir = rep.getDefaultFileSystem().findResource(DIR_INSTALLED_SERVERS);
        dir.addFileChangeListener(new InstanceInstallListener());
        ch = dir.getChildren();
        
        for(int i = 0; i < ch.length; i++) {
            //long t1=System.currentTimeMillis();
            addInstance(ch[i]);
            //System.out.println("ServerRegistry.addInstance("+ch[i]+")="+(System.currentTimeMillis()-t1));
        }
        //System.out.println("ServerRegistry.init="+(System.currentTimeMillis()-t0));
    }
    private Map serversMap() {
        init();
        return servers;
    }
    private Map instancesMap() {
        init();
        return instances;
    }
    private synchronized void addPlugin(FileObject fo) {
        String name = ""; //NOI18N
        try {
            if(fo.isFolder()) {
                name = fo.getName();
                if(serversMap().containsKey(name)) return;
                Server server = new Server(fo);
                serversMap().put(name,server);
                firePluginListeners(server,true);
            }
        } catch (Exception e) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, ("Plugin "+name+" installation failed")); //NOI18N
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    // PENDING should be private
    synchronized void removePlugin(FileObject fo) {
        String name = fo.getName();
        if(serversMap().containsKey(name)) {
            Server server = (Server) serversMap().get(name);
            serversMap().remove(name);
            firePluginListeners(server,false);
        }
    }
    
    class PluginInstallListener extends LayerListener {
        public void fileFolderCreated(FileEvent fe) {
            super.fileFolderCreated(fe);
            addPlugin(fe.getFile());
        }
        public void fileDeleted(FileEvent fe) {
            super.fileDeleted(fe);
            removePlugin(fe.getFile());
        }
    }
    
    class InstanceInstallListener extends LayerListener {
        public void fileDataCreated(FileEvent fe) {
            super.fileDataCreated(fe);
            addInstance(fe.getFile());
        }
        // PENDING should support removing of instances?
    }
    
    class LayerListener implements FileChangeListener {
        
        public void fileAttributeChanged(FileAttributeEvent fae) {
            java.util.logging.Logger.global.log(java.util.logging.Level.FINEST,"Attribute changed event"); // NOI18N
        }
        public void fileChanged(FileEvent fe) {
        }
        public void fileFolderCreated(FileEvent fe) {
        }
        public void fileRenamed(FileRenameEvent fe) {
        }
        
        public void fileDataCreated(FileEvent fe) {
        }
        public void fileDeleted(FileEvent fe) {
        }
        
    }
    
    public Collection getServers() {
        return serversMap().values();
    }
    
    public Collection getInstances() {
        return instancesMap().values();
    }
    
    public String[] getInstanceURLs() {
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
    
    public ServerInstance getServerInstance(String url) {
        return (ServerInstance) instancesMap().get(url);
    }
    
    public void removeServerInstance(String url) {
        if (url == null)
            return;
        
        // Make sure defaultInstance cache is reset
        ServerString def = getDefaultInstance();
        if (url.equals(def.getUrl())) {
            defaultInstance = null;
        }
        
        ServerInstance instance = (ServerInstance) instancesMap().remove(url);
        if (instance != null) {
            fireInstanceListeners(url, false);
            removeInstanceFromFile(url);
        }
        ServerString newinst = getDefaultInstance(false);
        fireDefaultInstance(def != null ? def.getUrl() : null, 
                newinst != null ? newinst.getUrl() : null);
    }
    
    public ServerInstance[] getServerInstances() {
        ServerInstance[] ret = new ServerInstance[instancesMap().size()];
        instancesMap().values().toArray(ret);
        return ret;
    }
    
    public static FileObject getInstanceFileObject(String url) {
        Repository rep = (Repository) Lookup.getDefault().lookup(Repository.class);
        FileObject[] installedServers = rep.getDefaultFileSystem().findResource(DIR_INSTALLED_SERVERS).getChildren();
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
     * @throws InstanceCreationException when instance with same url is already 
     *         registered.
     */
    public void addInstance(String url, String username, String password, 
            String displayName) throws InstanceCreationException {
        // should never have empty url; UI should have prevented this
        if (url == null || url.equals("")) { //NOI18N
            ErrorManager.getDefault().log(NbBundle.getMessage(ServerRegistry.class, "MSG_EmptyUrl"));
            return;
        }
        
        checkInstanceAlreadyExists(url);
        if (!addInstanceImpl(url, username, password, displayName)) {
            throw new InstanceCreationException(NbBundle.getMessage(ServerRegistry.class, "MSG_FailedToCreateInstance", displayName));
        }
    }    
    
    private synchronized void writeInstanceToFile(String url, String username, String password) throws IOException {
        if (url == null) {
            ErrorManager.getDefault().log(ErrorManager.ERROR, NbBundle.getMessage(ServerRegistry.class, "MSG_NullUrl"));
            return;
        }
        Repository rep = (Repository) Lookup.getDefault().lookup(Repository.class);
        FileObject dir = rep.getDefaultFileSystem().findResource(DIR_INSTALLED_SERVERS);
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
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
        }
    }
    
    /**
     * Add a new server instance in the server registry.
     *
     * @param  url URL to access deployment manager.
     * @param  username username used by the deployment manager.
     * @param  password password used by the deployment manager.
     * @param  displayName display name wich represents server instance in IDE.
     *
     * @return <code>true</code> if the server instance was created successfully,
     *         <code>false</code> otherwise.
     */
    private synchronized boolean addInstanceImpl(String url, String username, 
            String password, String displayName) {
        if (instancesMap().containsKey(url)) return false;
        for(Iterator i = serversMap().values().iterator(); i.hasNext();) {
            Server server = (Server) i.next();
            try {
                if(server.handlesUri(url)) {
                    // try to create a disconnected deployment manager to see
                    // whether the instance is not corrupted - see #46929
                    DeploymentManager manager = server.getDisconnectedDeploymentManager(url);
                    if (manager != null) {
                        ServerInstance instance = new ServerInstance(server,url);
                        // PENDING persist url/password in ServerString as well
                        instancesMap().put(url,instance);
                        ServerString str = new ServerString(server.getShortName(),url,null);
                        writeInstanceToFile(url,username,password);
                        if (displayName != null) instance.getInstanceProperties().setProperty(
                                InstanceProperties.DISPLAY_NAME_ATTR, displayName);
                        fireInstanceListeners(url, true);
                        return true;
                    }
                }
            } catch (Exception e) {
                org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.WARNING, e);
            }
        }
        return false;
    }
    
    public void addInstance(FileObject fo) {
        String url = (String) fo.getAttribute(URL_ATTR);
        String username = (String) fo.getAttribute(USERNAME_ATTR);
        String password = (String) fo.getAttribute(PASSWORD_ATTR);
        String displayName = (String) fo.getAttribute(InstanceProperties.DISPLAY_NAME_ATTR);
        //        System.err.println("Adding instance " + fo);
        addInstanceImpl(url, username, password, displayName);
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
    
    private void fireDefaultInstance(String oldInstance, String newInstance) {
        InstanceListener[] instListeners = getInstanceListeners();
        for(int i = 0; i < instListeners.length; i++) {
            instListeners[i].changeDefaultInstance(oldInstance, newInstance);
        }
    }
    
    public void setDefaultInstance(ServerString instance) {
        if (instance != null && instance.equals(defaultInstance)) {
            return;
        }
        
        if (instance == null) {
            removeDefaultInstanceFile();
            ServerString oldValue = defaultInstance;
            defaultInstance = null;
            fireDefaultInstance(oldValue != null ? oldValue.getUrl() : null, null);
        } else {
            if (ServerStringConverter.writeServerInstance(instance, DIR_INSTALLED_SERVERS, FILE_DEFAULT_INSTANCE)) {
                ServerString oldValue = defaultInstance;
                defaultInstance = instance;
                fireDefaultInstance(oldValue != null ? oldValue.getUrl() : null,
                        instance != null ? instance.getUrl() : null);
            }
        }
    }

    static private void removeDefaultInstanceFile() {
        FileLock lock = null;
        Writer writer = null;
        try {
            String pathName = DIR_INSTALLED_SERVERS + "/" + FILE_DEFAULT_INSTANCE; // NOI18N
            FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource(pathName);
            if (fo != null)
                fo.delete();
        } catch(Exception ioe) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.WARNING, ioe);
        }
    }

    private ServerString getInstallerDefaultPlugin() {
        File propFile = InstalledFileLocator.getDefault ().locate ("config/install.properties", null, false); // NOI18N
        Properties installProp = readProperties(propFile); //NOI18N
        
        String j2eeDefaultServerFileName = installProp.getProperty(J2EE_DEFAULT_SERVER);
        if (j2eeDefaultServerFileName == null)
            return null;
        
        File serverFile = InstalledFileLocator.getDefault ().locate (j2eeDefaultServerFileName, null, false);
        Properties defaultServerProp = readProperties(serverFile);
        String serverName = defaultServerProp.getProperty(SERVER_NAME);
        String url = defaultServerProp.getProperty(URL_ATTR);
        String user = defaultServerProp.getProperty(USERNAME_ATTR);
        String password = defaultServerProp.getProperty(PASSWORD_ATTR);
        String targetName = defaultServerProp.getProperty(TARGETNAME_ATTR);
        
        try {
            if (url != null) {
                InstanceProperties instProp = InstanceProperties.getInstanceProperties(url);
                if (instProp == null)
                    instProp = InstanceProperties.createInstanceProperties(url, user, password);
                instProp.setProperties(defaultServerProp);
                
                ServerInstance inst = getServerInstance(url);
                if (inst != null)
                    return new ServerString(inst, targetName);
                
            } else if (serverName != null) {
                Server server = getServer(serverName);
                if (server != null) {
                    ServerInstance[] instances = server.getInstances();
                    if (instances.length > 1)
                        return new ServerString(instances[0]);
                }
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        return null;
    }
    
    static private Properties readProperties(File propFile) {
        Properties prop = new Properties();
        try {
            if (propFile != null && propFile.exists())
                prop.load(new FileInputStream(propFile));
        } catch (IOException ioe) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, ioe.toString());
        }
        return prop;
    }
    
    public ServerString getDefaultInstance() {
        return getDefaultInstance(true);
    }
    
    public ServerString getDefaultInstance(boolean readFromFile) {
        if (defaultInstance != null)
            return defaultInstance;
        
        if (readFromFile) {
            defaultInstance = ServerStringConverter.readServerInstance(DIR_INSTALLED_SERVERS, FILE_DEFAULT_INSTANCE);
            
            if (defaultInstance == null) {
                defaultInstance = getInstallerDefaultPlugin();
            }
            
        }
        
        if (defaultInstance == null) {
            ServerInstance[] instances = getServerInstances();
            if (instances != null && instances.length > 0) {
                defaultInstance = new ServerString(instances[0]);
            }
        }
        
        setDefaultInstance(defaultInstance);
        return defaultInstance;
    }
    
    public interface PluginListener {
        
        public void serverAdded(Server name);
        
        public void serverRemoved(Server name);
        
    }

    private static HashMap configNamesByType = null;
    private static final Object[] allTypes = new Object[] {
        J2eeModule.EAR, J2eeModule.CLIENT, J2eeModule.CONN, J2eeModule.EJB, J2eeModule.WAR };
        
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
    public boolean isConfigFileName(String name, Object type) {
	initConfigNamesByType();
	Set configNames = (Set) configNamesByType.get(type);
	return (configNames != null && configNames.contains(name));
    }
    
    public ServerInstance getInstanceOrDefault(String uri) {
        ServerInstance instance = getServerInstance(uri);
        if (instance == null) {
            instance = getDefaultInstance().getServerInstance();
        }
        if (instance != null)
            return instance;
        throw new RuntimeException(NbBundle.getMessage(ServerRegistry.class, "MSG_NoServerInstances", uri));
    }

}
