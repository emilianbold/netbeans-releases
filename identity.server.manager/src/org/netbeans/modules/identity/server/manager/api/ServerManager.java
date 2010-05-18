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
/*
 * ServerManager.java
 *
 * Created on June 14, 2006, 1:05 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.identity.server.manager.api;

import java.io.IOException;
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


/**
 * This class manages multiples instances of ServerInstance.
 *
 * Created on June 14, 2006, 1:05 AM
 *
 * @author ptliu
 */
public class ServerManager {
    private static String DIR_CONFIGURED_SERVERS = "/Identity/ConfiguredServers"; //NOI18N
    
    private static ServerManager instance;
    
    private Map<String, ServerInstance> instancesMap;
    
    private Set<ServerInstanceListener> listeners;
    
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
        
        FileObject dir = getConfiguredServersDirectory();
        //dir.addFileChangeListener(new InstanceInstallListener());
        FileObject[] ch = dir.getChildren();
        
        for(int i = 0; i < ch.length; i++) {
            //System.out.println("addInstance() ch = " + ch[i]);
            ServerInstance instance = convertToServerInstance(ch[i]);
            addServerInstanceInternal(instance);
        }
    }
    
    public Collection<ServerInstance> getServerInstances() {
        return Collections.unmodifiableCollection(instancesMap.values());
    }
    
    public ServerInstance getServerInstance(ServerProperties properties) {
        String id = properties.getProperty(ServerProperties.PROP_ID);
        
        ServerInstance instance = instancesMap.get(id);
        
        if (instance == null) {
            instance = createServerInstance(properties);
            addServerInstance(instance);
        }
        
        return instance;
    }
    
    public ServerInstance getServerInstance(String id) {
        ServerProperties properties = ServerProperties.getInstance(id);
        
        return getServerInstance(properties);
        
    }
    
    private ServerInstance createServerInstance(ServerProperties properties) {
        ServerInstance instance = new ServerInstance();
        
        instance.setID(properties.getProperty(ServerProperties.PROP_ID));
        instance.setHost(properties.getProperty(ServerProperties.PROP_HOST));
        instance.setPort(properties.getProperty(ServerProperties.PROP_PORT));
        instance.setContextRoot(properties.getProperty(ServerProperties.PROP_CONTEXT_ROOT));
        instance.setUserName(properties.getProperty(ServerProperties.PROP_USERNAME));
        instance.setPassword(properties.getProperty(ServerProperties.PROP_PASSWORD));
        
        return instance;
    }
    
    public boolean addServerInstance(ServerInstance instance) {
        //writeInstanceToFile(instance);
        addServerInstanceInternal(instance);
        fireServerInstanceAdded(instance);
        
        return true;
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
        ServerInstance instance = new ServerInstance();
        
        instance.setID((String) fObj.getAttribute(ServerInstance.PROP_ID));
        instance.setHost((String) fObj.getAttribute(ServerInstance.PROP_HOST));
        instance.setPort((String) fObj.getAttribute(ServerInstance.PROP_PORT));
        instance.setContextRoot((String) fObj.getAttribute(ServerInstance.PROP_CONTEXT_ROOT));
        instance.setUserName((String) fObj.getAttribute(ServerInstance.PROP_USERNAME));
        instance.setPassword((String) fObj.getAttribute(ServerInstance.PROP_PASSWORD));
        
        return instance;
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
        }
        
        instanceFO.setAttribute(ServerInstance.PROP_ID, instance.getID());
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
        return FileUtil.getConfigFile(DIR_CONFIGURED_SERVERS);
    }
}

