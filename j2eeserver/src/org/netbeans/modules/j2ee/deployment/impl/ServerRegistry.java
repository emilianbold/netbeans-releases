/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.impl;

import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.openide.filesystems.*;
import org.openide.*;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import java.util.*;
import java.io.*;
//import java.util.logging.*;

public final class ServerRegistry implements java.io.Serializable {

    public static final String DIR_INSTALLED_SERVERS = "/J2EE/InstalledServers"; //NOI18N
    public static final String DIR_JSR88_PLUGINS = "/J2EE/Jsr88Plugins"; //NOI18N
    public static final String URL_ATTR = "url"; //NOI18N
    public static final String USERNAME_ATTR = InstanceProperties.USERNAME_ATTR;
    public static final String PASSWORD_ATTR = InstanceProperties.PASSWORD_ATTR;
    public static final String FILE_DEFAULT_INSTANCE = "DefaultInstance.settings";

    private static ServerRegistry instance = null;
    public synchronized static ServerRegistry getInstance() {

        if(instance == null) instance = new ServerRegistry();
        return instance;

        //PENDING need to get this from lookup
        //    return (ServerRegistry) Lookup.getDefault().lookup(ServerRegistry.class);
    }

    private transient Map servers = new HashMap();
    private transient Map instances = new HashMap();
    private transient Collection pluginListeners = new HashSet();
    private transient Collection instanceListeners = new LinkedList();
    
    // This is the serializable portion of ServerRegistry
    private ServerString defaultInstance;

    public ServerRegistry() {

        Repository rep = (Repository) Lookup.getDefault().lookup(Repository.class);
        FileObject dir = rep.findResource(DIR_JSR88_PLUGINS);
        dir.addFileChangeListener(new PluginInstallListener());
        FileObject[] ch = dir.getChildren();
        for(int i = 0; i < ch.length; i++)
            addPlugin(ch[i]);

        dir = rep.findResource(DIR_INSTALLED_SERVERS);
        dir.addFileChangeListener(new InstanceInstallListener());
        ch = dir.getChildren();

        for(int i = 0; i < ch.length; i++)
            addInstance(ch[i]);
    }

    private synchronized void addPlugin(FileObject fo) {
        try {
            if(fo.isFolder()) {
                String name = fo.getName();
                if(servers.containsKey(name)) return;
                Server server = new Server(fo);
                servers.put(name,server);
                firePluginListeners(server,true);
            }

        } catch (Exception e) {

            e.printStackTrace(System.err);
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Plugin installation failed"));
        }
    }
    
    // PENDING should be private
    synchronized void removePlugin(FileObject fo) {
        String name = fo.getName();
        if(servers.containsKey(name)) {
            Server server = (Server) servers.get(name);
            servers.remove(name);
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
            java.util.logging.Logger.global.log(java.util.logging.Level.FINEST,"Attribute changed event");
        }
        public void fileChanged(FileEvent fe) {
            //
            System.out.println("File changed event");
        }
        public void fileFolderCreated(FileEvent fe) {
            //
            System.out.println("Folder created event");
        }
        public void fileRenamed(FileRenameEvent fe) {
            //
            System.out.println("File renamed event");
        }
        
        public void fileDataCreated(FileEvent fe) {
            //
            System.out.println("file created event");
        }
        public void fileDeleted(FileEvent fe) {
            //
            System.out.println("file deleted event");
        }
        
    }
    
    public Collection getServers() {
        return servers.values();
    }
    
    public Collection getInstances() {
        return instances.values();
    }

    public String[] getInstanceURLs() {
        return (String[]) instances.keySet().toArray(new String[instances.size()]);
    }

    public void checkInstanceExists(String url) throws InstanceCreationException {
        if (getServerInstance(url) != null) {
            String msg = NbBundle.getMessage(ServerRegistry.class, "MSG_InstanceAlreadyExists", url);
            throw new InstanceCreationException(msg);
        }
    }
    
    public Server getServer(String name) {
        return (Server) servers.get(name);
    }
    
    public synchronized Collection getServers(PluginListener pl) {
        pluginListeners.add(pl);
        return getServers();
    }
    
    public ServerInstance getServerInstance(String url) {
        return (ServerInstance) instances.get(url);
    }
    
    public void removeServerInstance(String url) {
        if (url == null)
            return;
        
        // Make sure defaultInstance cache is reset
        ServerString def = getDefaultInstance();
        if (url.equals(def.getUrl())) {
            defaultInstance = null;
        }
        
        ServerInstance instance = (ServerInstance) instances.remove(url);
        if (instance != null) {
            ServerString ss = new ServerString(instance);
            fireInstanceListeners(ss, false);
            removeInstanceFromFile(instance.getUrl());
        }
        getDefaultInstance(false);
    }
    
    public ServerInstance[] getServerInstances() {
        ServerInstance[] ret = new ServerInstance[instances.size()];
        instances.values().toArray(ret);
        return ret;
    }
    
    public static FileObject getInstanceFileObject(String url) {
        Repository rep = (Repository) Lookup.getDefault().lookup(Repository.class);
        FileObject[] installedServers = rep.findResource(DIR_INSTALLED_SERVERS).getChildren();
        for (int i=0; i<installedServers.length; i++) {
            String val = (String) installedServers[i].getAttribute(URL_ATTR);
            if (val != null && val.equals(url))
                return installedServers[i];
        }
        return null;
    }
    
    public void addInstance(String url, String username, String password) throws InstanceCreationException {
        // should never have empty url; UI should have prevented this
        if (url == null || url.equals("")) { //NOI18N
            ErrorManager.getDefault().log(NbBundle.getMessage(ServerRegistry.class, "MSG_EmptyUrl"));
            return;
        }
        
        try {
            addInstanceImpl(url,username,password);
        } catch(java.io.IOException e) {
            String msg = NbBundle.getMessage(ServerRegistry.class, "MSG_ErrorCreateInstance", url);
            InstanceCreationException ice = new InstanceCreationException(msg);
            throw (InstanceCreationException) ErrorManager.getDefault().annotate(ice, e);
        }
    }
    
    private synchronized void writeInstanceToFile(String url, String username, String password) throws IOException {
        Repository rep = (Repository) Lookup.getDefault().lookup(Repository.class);
        FileObject dir = rep.findResource(DIR_INSTALLED_SERVERS);
        String name = FileUtil.findFreeFileName(dir,"instance",null);
        FileObject fo = dir.createData(name);
        fo.setAttribute(URL_ATTR, url);
        fo.setAttribute(USERNAME_ATTR, username);
        fo.setAttribute(PASSWORD_ATTR, password);
    }
    
    private synchronized void removeInstanceFromFile(String url) {
        FileObject instanceFO = getInstanceFileObject(url);
        if (instanceFO == null)
            return;
        try {
            instanceFO.delete();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe);
        }
    }
    
    private synchronized boolean addInstanceImpl(String url, String username, String password) {
        if (instances.containsKey(url)) return false;
        for(Iterator i = servers.values().iterator(); i.hasNext();) {
            Server server = (Server) i.next();
            try {
               if(server.handlesUri(url)) {
                    DeploymentManager manager = server.getDeploymentManager(url,username,password);
                    if(manager != null) {
                        ServerInstance instance = new ServerInstance(server,url,manager);
                        // PENDING persist url/password in ServerString as well
                        instances.put(url,instance);
                        ServerString str = new ServerString(server.getShortName(),url,null);
                        writeInstanceToFile(url,username,password);
                        fireInstanceListeners(str,true);
                        return true;
                    }
                }
            } catch (javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException dmce) {
                org.openide.ErrorManager.getDefault().log(org.openide.ErrorManager.INFORMATIONAL, dmce.toString());
            } catch (Exception e) {
                org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.WARNING, e);
            }
        }
        // PENDING need error dialog saying this server wasn't recognized by any plugin
        return false;
    }
    
    public void addInstance(FileObject fo) {
        String url = (String) fo.getAttribute(URL_ATTR);
        String username = (String) fo.getAttribute(USERNAME_ATTR);
        String password = (String) fo.getAttribute(PASSWORD_ATTR);
        //        System.err.println("Adding instance " + fo);
        addInstanceImpl(url,username,password);
    }
    
    public Collection getInstances(InstanceListener il) {
        if (il != null)
            instanceListeners.add(il);
        return getInstances();
    }
    
    public synchronized void addInstanceListener (InstanceListener il) {
        instanceListeners.add (il);
    }
    
    public synchronized void removeInstanceListener(InstanceListener il) {
        instanceListeners.remove(il);
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
    }
    
    private void fireInstanceListeners(ServerString instance, boolean add) {
        for(Iterator i = instanceListeners.iterator();i.hasNext();) {
            InstanceListener pl = (InstanceListener)i.next();
            if(add) pl.instanceAdded(instance);
            else pl.instanceRemoved(instance);
        }
    }
    
    private void fireDefaultInstance(ServerString oldInstance, ServerString newInstance) {
        for(Iterator i = instanceListeners.iterator();i.hasNext();) {
            InstanceListener pl = (InstanceListener)i.next();
            pl.changeDefaultInstance(oldInstance, newInstance);
        }
    }
    
    public void setDefaultInstance(ServerString instance) {
        if (instance == null || instance.equals (defaultInstance)) {
            return;
        }
        if (ServerStringConverter.writeServerInstance(instance, DIR_INSTALLED_SERVERS, FILE_DEFAULT_INSTANCE)) {
            ServerString oldValue = defaultInstance;
            defaultInstance = instance;
            fireDefaultInstance(oldValue, instance);
        }
    }

    public ServerString getDefaultInstance() {
        return getDefaultInstance(true);
    }
    
    public ServerString getDefaultInstance(boolean readFromFile) {
        if (defaultInstance == null && readFromFile) {
            defaultInstance = ServerStringConverter.readServerInstance(DIR_INSTALLED_SERVERS, FILE_DEFAULT_INSTANCE);
        }
        
        if (defaultInstance == null) {
            ServerInstance[] instances = getServerInstances();
            if (instances != null && instances.length > 0) {
                //PENDING: layer.xml entry for a preferred server instance
                setDefaultInstance(new ServerString(instances[0]));
            }
        }
        return defaultInstance;
    }
    
    public interface PluginListener {
        
        public void serverAdded(Server name);
        
        public void serverRemoved(Server name);
        
    }
    
    public interface InstanceListener {
        
        public void instanceAdded(ServerString instance);
        
        public void instanceRemoved(ServerString instance);
        
        public void changeDefaultInstance(ServerString oldInstance, ServerString newInstance);
        
    }
    
}
