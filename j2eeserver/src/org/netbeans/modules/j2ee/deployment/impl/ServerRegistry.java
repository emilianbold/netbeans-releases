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

import org.netbeans.modules.j2ee.deployment.impl.gen.nbd.*;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.openide.filesystems.*;
import org.openide.*;
import org.openide.util.Lookup;

import java.util.*;
import java.io.IOException;

public final class ServerRegistry implements java.io.Serializable {
    
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
        FileObject dir = rep.findResource("/J2EE/Jsr88Plugins");
        dir.addFileChangeListener(new PluginInstallListener());
        FileObject[] ch = dir.getChildren();
        for(int i = 0; i < ch.length; i++)
            addPlugin(ch[i]);
        dir = rep.findResource("/J2EE/InstalledServers");
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
            //            System.out.println("Attribute changed event");
        }
        public void fileChanged(FileEvent fe) {
            //            System.out.println("File changed event");
        }
        public void fileFolderCreated(FileEvent fe) {
            //            System.out.println("Folder created event");
        }
        public void fileRenamed(FileRenameEvent fe) {
            //            System.out.println("File renamed event");
        }
        
        public void fileDataCreated(FileEvent fe) {
            //            System.out.println("file created event");
        }
        public void fileDeleted(FileEvent fe) {
            //            System.out.println("file deleted event");
        }
        
    }
    
    public Collection getServers() {
        return servers.values();
    }
    
    public Collection getInstances() {
        return instances.values();
    }
    
    public Server getServer(String name) {
        return (Server) servers.get(name);
    }
    
    public synchronized Collection getServers(PluginListener pl) {
        pluginListeners.add(pl);
        return getServers();
    }
    
    public ServerInstance getServerInstance(String name) {
        return (ServerInstance) instances.get(name);
    }
    
    public ServerInstance[] getServerInstances() {
        ServerInstance[] ret = new ServerInstance[instances.size()];
        instances.values().toArray(ret);
        return ret;
    }
    
    public static final String INSTALLED_SERVERS_PATH = "/J2EE/InstalledServers";
    public static final String URL_ATTR = "url";
    public static final String USERNAME_ATTR = "username";
    public static final String PASSWORD_ATTR = "password";
    public static FileObject getInstanceFileObject(String url) {
        Repository rep = (Repository) Lookup.getDefault().lookup(Repository.class);
        return rep.findResource(INSTALLED_SERVERS_PATH+"/"+url);
    }
    
    public synchronized void addInstance(String url, String username, String password) throws IOException {
        Repository rep = (Repository) Lookup.getDefault().lookup(Repository.class);
        FileObject dir = rep.findResource(INSTALLED_SERVERS_PATH);
        String name = FileUtil.findFreeFileName(dir,"instance",null);
        FileObject fo = dir.createData(name);
        fo.setAttribute(URL_ATTR, url);
        fo.setAttribute(USERNAME_ATTR, username);
        fo.setAttribute(PASSWORD_ATTR, password);
        // PENDING synchronize this so that the instance isn't created before
        // the attributes are set?
    }
    
    public void addInstance(FileObject fo) {
        String url = (String) fo.getAttribute(URL_ATTR);
        String username = (String) fo.getAttribute(USERNAME_ATTR);
        String password = (String) fo.getAttribute(PASSWORD_ATTR);
        //        System.err.println("Adding instance " + fo);
        for(Iterator i = servers.values().iterator(); i.hasNext();) {
            Server server = (Server) i.next();
            try {
                DeploymentManager manager = server.getDeploymentManager(url,username,password);
                if(manager != null) {
                    ServerInstance instance = new ServerInstance(server,url,manager);
                    // PENDING persist url/password in ServerString as well?
                    instances.put(url,instance);
                    ServerString str = new ServerString(server.getShortName(),url,null);
                    fireInstanceListeners(str,true);
                }
            } catch (Exception e) {
            }
        }
    }
    
    public Collection getInstances(InstanceListener il) {
        instanceListeners.add(il);
        return getInstances();
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
        }
    }
    
    private void fireDefaultInstance(ServerString instance) {
        for(Iterator i = instanceListeners.iterator();i.hasNext();) {
            InstanceListener pl = (InstanceListener)i.next();
            pl.changeDefaultInstance(instance);
        }
    }
    
    public void setDefaultInstance(ServerString instance) {
        defaultInstance = instance;
        fireDefaultInstance(instance);
    }
    
    public ServerString getDefaultInstance() {
        return defaultInstance;
    }
    
    public interface PluginListener {
        
        public void serverAdded(Server name);
        
        public void serverRemoved(Server name);
        
    }
    
    public interface InstanceListener {
        
        public void instanceAdded(ServerString instance);
        
        // PENDING remove instance?
        
        public void changeDefaultInstance(ServerString instance);
        
    }
    
}
