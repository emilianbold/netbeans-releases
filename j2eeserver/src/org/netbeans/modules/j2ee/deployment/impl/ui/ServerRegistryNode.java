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
 * ServerRegNode.java -- synopsis
 *
 */
package org.netbeans.modules.j2ee.deployment.impl.ui;

import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.openide.nodes.*;
import org.openide.filesystems.*;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.*;
import org.netbeans.modules.j2ee.deployment.impl.*;
import java.util.*;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.openide.util.lookup.Lookups;

/**
 * The server registry node is a node representing the registry in global options.
 * @author Joe Cortopazzi
 * @author George FinKlang
 */

public class ServerRegistryNode extends AbstractNode
implements ServerRegistry.PluginListener, InstanceListener {
    
    static final String SERVERS_ICON = "org/netbeans/modules/j2ee/deployment/impl/ui/resources/Servers.png";//NOI18N
    
    private transient Map serverNodes = new HashMap();
    private transient HelpCtx helpCtx;
    private boolean expandablePassTargetNode = true;
    
    /** Creates a new instance of ServerRegistryNode2 */
    public ServerRegistryNode() {
        super(new ServerChildren());
        setName(""); //NOI18N
        setDisplayName(NbBundle.getMessage(ServerRegistryNode.class, "SERVER_REGISTRY_NODE"));
        setShortDescription(NbBundle.getMessage(ServerRegistryNode.class, "SERVER_REGISTRY_SHORT_DESCRIPTION"));
        setIconBaseWithExtension(SERVERS_ICON);
    }

    public void serverAdded(Server server) {
        updateKeys();
    }
    public void serverRemoved(Server server) {
        updateKeys();
    }
    
    public boolean isExpandablePassTargetNode() {
        return expandablePassTargetNode;
    }
    public void setExpandablePassTargetNode(boolean v) {
        expandablePassTargetNode = v;
        setChildren(new ServerChildren());
        serverNodes.clear();
    }
    
    public void instanceAdded(String instance) {
        updateKeys();
    }
    public void instanceRemoved(String instance) {
        updateKeys();
    }
    public void changeDefaultInstance(String oldInstance, String newInstance) {
    }
    
    private void updateKeys() {
        ((ServerChildren) getChildren()).updateKeys();
    }
    
    public SystemAction[] createActions() {
        return new SystemAction[] {
            SystemAction.get(AddServerInstanceAction.class)
        };
    }
    
    public HelpCtx getHelpCtx() {
        if(helpCtx == null)
            helpCtx = new HelpCtx(NbBundle.getBundle(ServerRegistryNode.class).getString("nodes_server_registry_node_html"));//NOI18N
        return helpCtx;
    }
        
    private static class ServerChildren extends Children.Keys {
        private boolean listenersAdded = false;
        
        public ServerChildren() {
        }
        
        protected void addNotify() {
            updateKeys();
            
            if (! listenersAdded) {
                ServerRegistryNode parent = (ServerRegistryNode) getNode();
                ServerRegistry.getInstance().addPluginListener(parent);
                ServerRegistry.getInstance().addInstanceListener(parent);
                listenersAdded = true;
            }
        }
        
        protected Node[] createNodes(Object obj) {
            ServerInstance instance = (ServerInstance) obj;
            if (instance == null)
                return new Node[0];
            
            Node childNode;
            StartServer startServer = instance.getStartServer();
            if (startServer == null) {
                return new Node[0];
            }
            if (startServer.isAlsoTargetServer(null)) {
                childNode = instance.getServer().getNodeProvider().createInstanceTargetNode(instance);
            } else {
                childNode = instance.getServer().getNodeProvider().createInstanceNode(instance);
            }
            instance.refresh(); // detect the server instance status
            return new Node[] { childNode };
        }
        
        public void updateKeys() {
            List instances = new ArrayList();
            Iterator serverIter = ServerRegistry.getInstance().getServers().iterator();
            while (serverIter.hasNext()) {
                Server server = (Server)serverIter.next();
                ServerInstance[] serverInstances = server.getInstances();
                for (int i = 0; i < serverInstances.length; i++) {
                    instances.add(serverInstances[i]);
                }
            }
            Collections.sort(instances);
            setKeys(instances);
        }
    }
    
    public static ServerRegistryNode getServerRegistryNode() {
        return Lookups.forPath("UI/Runtime").lookup(ServerRegistryNode.class);
    }
}
