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
/*
 * ServerManager.java
 *
 * Created on June 14, 2006, 1:05 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.identity.server.manager.api;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.identity.profile.api.configurator.ServerProperties;
import org.netbeans.modules.identity.server.manager.spi.ServerInstanceListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * This class manages multiples instances of ServerInstance. 
 *
 * Created on June 14, 2006, 1:05 AM
 *
 * @author ptliu
 */
public class ServerManager {
    private static String DIR_CONFIGURED_SERVERS = "/Identity/ConfiguredServers"; //NOI18N
    
    public static final String DEFAULT_ID = "Default";              //NOI18N
    
    //public static final String DEFAULT_DISPLAY_NAME = "Default Instance";   //NOI18N
    
    private static ServerManager instance;
    
    private Map<String, ServerInstance> instancesMap;
    
    private Set<ServerInstanceListener> listeners;
    
    private ServerInstance defaultServerInstance;
   
    /** Creates a new instance of ServerManager */
    private ServerManager() {
        init();
    }
    
    public static ServerManager getDefault() {
        if (instance == null) {
            instance = new ServerManager();
        }
        
        return instance;
    }
    
    public void init() {
        listeners = new HashSet<ServerInstanceListener>();
        instancesMap = new HashMap<String, ServerInstance>();
        
        defaultServerInstance = createDefaultServerInstance();
        addServerInstanceInternal(defaultServerInstance);
        
        FileObject dir = getConfiguredServersDirectory();
        //dir.addFileChangeListener(new InstanceInstallListener());
        FileObject[] ch = dir.getChildren();
        
        for(int i = 0; i < ch.length; i++) {
            //System.out.println("addInstance() ch = " + ch[i]);
            ServerInstance instance = convertToServerInstance(ch[i]);
            addServerInstanceInternal(instance);
        }
    }
    
    private ServerInstance createDefaultServerInstance() {
        ServerInstance instance = new ServerInstance();
        ServerProperties properties = new ServerProperties();
        
        instance.setID(DEFAULT_ID);
        
        try {
            instance.setDisplayName(NbBundle.getMessage(ServerManager.class, 
                    "LBL_DefaultInstanceName")); //NOI18N
        } catch (PropertyVetoException ex) {
            ex.printStackTrace();
        }
        
        instance.setIsDefault(true);
        
        instance.setHost(properties.getProperty(ServerProperties.PROP_HOST));
        instance.setPort(properties.getProperty(ServerProperties.PROP_PORT));
        instance.setContextRoot(properties.getProperty(ServerProperties.PROP_CONTEXT_ROOT));
        instance.setUserName(properties.getProperty(ServerProperties.PROP_USERNAME));
        instance.setPassword(properties.getProperty(ServerProperties.PROP_PASSWORD));
        
        return instance;
    }
    
    public ServerInstance getDefaultServerInstance() {
        return defaultServerInstance;
    }
    
    public Collection<ServerInstance> getServerInstances() {
        return Collections.unmodifiableCollection(instancesMap.values());
    }
    
    public ServerInstance getSeverInstance(ServerProperties properties) {
        String displayName = properties.getProperty(ServerProperties.PROP_ID);
        
        return getServerInstance(displayName);
    }
    
    public ServerInstance getServerInstance(String displayName) {
        Collection<ServerInstance> instances = getServerInstances();
        
        for (ServerInstance instance : instances) {
            if (displayName.equals(instance.getDisplayName())) {
                return instance;
            }
        }
        
        //return getDefaultServerInstance();
        return null;
    }
   
    public Collection<ServerProperties> getAllServerProperties() {
        Collection<ServerInstance> instances = getServerInstances();
        Collection<ServerProperties> result = new ArrayList<ServerProperties>();
        
        for (ServerInstance instance : instances) {
            result.add(instance.getServerProperties());
        }
        
        return result;
    }
    
    public boolean addServerInstance(ServerInstance instance) {
        if (serverInstanceExists(instance)) {
            // TODO:  Need to report error
            return false;
        }
        try {
            writeInstanceToFile(instance);
            addServerInstanceInternal(instance);
            fireServerInstanceAdded(instance);
            
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return false;
    }
    
    public void removeServerInstance(ServerInstance instance) {
        try {
            removeInstanceFromFile(instance);
            removeServerInstanceInternal(instance);
            fireServerInstanceRemoved(instance);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void addServerInstanceListener(ServerInstanceListener listener) {
        listeners.add(listener);
    }
    
    public void removeServerInstanceListener(ServerInstanceListener listener) {
        listeners.remove(listener);
    }
    
    public void fireServerInstanceAdded(ServerInstance instance) {
        String id = instance.getID();
        
        for (ServerInstanceListener l : listeners) {
            l.instanceAdded(id);
        }
    }
    
    public void fireServerInstanceRemoved(ServerInstance instance) {
        String id = instance.getID();
        
        for (ServerInstanceListener l : listeners) {
            l.instanceRemoved(id);
        }
    }
    
    private void addServerInstanceInternal(ServerInstance instance) {
        instancesMap.put(instance.getID(), instance);
        
        //TODO:  Need to add change listeners
    }
    
    private void removeServerInstanceInternal(ServerInstance instance) {
        instancesMap.remove(instance.getID());
    }
    
    private boolean serverInstanceExists(ServerInstance instance) {
        if (instancesMap.get(instance.getID()) == null) {
            return false;
        }
        
        return true;
    }
    
    private ServerInstance convertToServerInstance(FileObject fObj) {
        try {
            ServerInstance instance = new ServerInstance();
            
            instance.setID((String) fObj.getAttribute(ServerInstance.PROP_ID));
            instance.setDisplayName((String) fObj.getAttribute(ServerInstance.PROP_DISPLAY_NAME));
            instance.setHost((String) fObj.getAttribute(ServerInstance.PROP_HOST));
            instance.setPort((String) fObj.getAttribute(ServerInstance.PROP_PORT));
            instance.setContextRoot((String) fObj.getAttribute(ServerInstance.PROP_CONTEXT_ROOT));
            instance.setUserName((String) fObj.getAttribute(ServerInstance.PROP_USERNAME));
            instance.setPassword((String) fObj.getAttribute(ServerInstance.PROP_PASSWORD));
            
            return instance;
        } catch (PropertyVetoException ex) {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    public synchronized void writeInstanceToFile(ServerInstance instance)
    throws IOException {
        FileObject dir = getConfiguredServersDirectory();
        FileObject instanceFOs[] = dir.getChildren();
        FileObject instanceFO = null;
        String id = instance.getID();
        
        if (id != null) {
            for (int i = 0; i < instanceFOs.length; i++) {
                if (id.equals(instanceFOs[i].getAttribute(ServerInstance.PROP_ID)))
                    instanceFO = instanceFOs[i];
            }
        }
        
        if (instanceFO == null) {
            String fileName = FileUtil.findFreeFileName(dir, "instance", null);  //NOI18N
            instanceFO = dir.createData(fileName);
            instance.setID(fileName);
        }
        
        instanceFO.setAttribute(ServerInstance.PROP_ID, instance.getID());
        instanceFO.setAttribute(ServerInstance.PROP_DISPLAY_NAME, instance.getDisplayName());
        instanceFO.setAttribute(ServerInstance.PROP_HOST, instance.getHost());
        instanceFO.setAttribute(ServerInstance.PROP_PORT, instance.getPort());
        instanceFO.setAttribute(ServerInstance.PROP_CONTEXT_ROOT, instance.getContextRoot());
        instanceFO.setAttribute(ServerInstance.PROP_USERNAME, instance.getUserName());
        instanceFO.setAttribute(ServerInstance.PROP_PASSWORD, instance.getPassword());
    }
    
    private void removeInstanceFromFile(ServerInstance instance)
    throws IOException {
        FileObject dir = getConfiguredServersDirectory();
        FileObject instanceFOs[] = dir.getChildren();
        FileObject instanceFO = null;
        String id = instance.getID();
        
        if (id != null) {
            for (int i = 0; i < instanceFOs.length; i++) {
                if (id.equals(instanceFOs[i].getAttribute(ServerInstance.PROP_ID)))
                    instanceFO = instanceFOs[i];
            }
        }
        
        if (instanceFO != null) instanceFO.delete();
    }
    
    public static FileObject getInstanceFileObject(String id) {
        FileObject dir = getConfiguredServersDirectory();
        FileObject[] servers = dir.getChildren();
        
        for (int i = 0; i < servers.length; i++) {
            String val = (String) servers[i].getAttribute(ServerInstance.PROP_ID);
            if (val != null && val.equals(id))
                return servers[i];
        }
        return null;
    }
    
    private static FileObject getConfiguredServersDirectory() {
        Repository rep = (Repository) Lookup.getDefault().lookup(Repository.class);
        return rep.getDefaultFileSystem().findResource(DIR_CONFIGURED_SERVERS);
    }
}

