/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


/*
 * ServerRegNode.java -- synopsis
 *
 */
package org.netbeans.modules.j2ee.deployment.impl.ui;

import java.awt.Component;
import java.util.*;
import java.lang.reflect.InvocationTargetException;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.ErrorManager;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.*;
import org.netbeans.modules.j2ee.deployment.impl.*;
import org.netbeans.modules.j2ee.deployment.impl.ui.FilterXNode;

/**
 * The server registry node is a node representing the registry in global options.
 * @author Joe Cortopazzi
 * @author George FinKlang
 */

public class ServerRegistryNode extends AbstractNode {
    
    static final String REGISTRY_ICON_BASE = "org/netbeans/modules/j2ee/deployment/impl/ui/resources/ServerRegistry";//NOI18N
    
    private transient ServerRegistry.PluginListener pluginListener;
    private transient ServerRegistry.InstanceListener instanceListener;
    private transient Set keys = new HashSet();
    private transient Map serverNodes = new HashMap();
    private transient Node[] nodes = null;
    private transient HelpCtx helpCtx;
    
    public ServerRegistryNode() {
        super(new ServerChildren());
        ServerString defaultInstance = ServerRegistry.getInstance().getDefaultInstance();
        String msg = NbBundle.getBundle(ServerRegistryNode.class).getString("SERVER_REGISTRY_NODE_NO_DEFAULT");//NOI18N
        if (defaultInstance != null) {
            ServerInstance si = defaultInstance.getServerInstance();
            msg = NbBundle.getMessage(ServerRegistryNode.class,
            "SERVER_REGISTRY_NODE_DEFAULT",
            ((si == null) ? defaultInstance.getUrl() : si.getDisplayName()));//NOI18N
        }
        setDisplayName(msg);
        setName("");//NOI18N
        setIconBase(REGISTRY_ICON_BASE);
        try {
            
            pluginListener = new ServerRegistry.PluginListener() {
                public void serverAdded(Server server) {
                    //                    System.err.println("Server added " + server);
                    updateKeys();
                }
                public void serverRemoved(Server server) {
                    //                    System.err.println("Server removed " + server);
                    updateKeys();
                }
            };
            
            instanceListener = new ServerRegistry.InstanceListener() {
                public void instanceAdded(ServerString instance) {
                    refreshServerNode(instance);
                }
                public void instanceRemoved(ServerString instance) {
                    refreshServerNode(instance);
                }
                public void changeDefaultInstance(ServerString oldInstance, ServerString instance) {
                    //                    System.err.println("Changing default to " + instance);
                    setInstance(instance == null ? null : instance.getServerInstance());
                }
                private void refreshServerNode(ServerString instance) {
                    Server server = instance.getServer();
                    Node node = getServerNode(server);
                    ServerNode serverNode;
                    if (node instanceof FilterXNode)
                        serverNode = (ServerNode) ((FilterXNode)node).getXNode();
                    else
                        serverNode = (ServerNode) node;
                    serverNode.refreshChildren();
                }
            };
            
            ServerRegistry.getInstance().getServers(pluginListener);
            updateKeys();
            
            Collection instances = ServerRegistry.getInstance().getInstances(instanceListener);
            
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION,  e);
        }
    }
    
    private void updateKeys() {
        //        System.err.println("Updating the children of the server registry");
        ((ServerChildren) getChildren()).updateKeys();
    }
    
    Node getServerNode(Server s) {
        Node node = (Node) serverNodes.get(s);
        if(node == null) {
            node = s.getNodeProvider().createServerNode(s);
            serverNodes.put(s,node);
        }
        return node;
    }
    
    public SystemAction[] createActions() {
        return new SystemAction[] {
            SystemAction.get(FindDeploymentManagerAction.class),
            SystemAction.get(SetDefaultServerAction.class),
            SystemAction.get(NodeHelpAction.class)};
    }
    
    public HelpCtx getHelpCtx() {
        if(helpCtx == null)
            helpCtx = new HelpCtx(NbBundle.getBundle(ServerRegistryNode.class).getString("nodes_server_registry_node_html"));//NOI18N
        return helpCtx;
    }
    
    public void setInstance(ServerInstance inst) {
        String message = NbBundle.getMessage(ServerRegistryNode.class,"SERVER_REGISTRY_NODE_NO_DEFAULT");//NOI18N
        if(inst != null) {
            message = NbBundle.getMessage(ServerRegistryNode.class,"SERVER_REGISTRY_NODE_DEFAULT", inst.getDisplayName());//NOI18N
        }
        setDisplayName(message);
    }

    private static class ServerChildren extends Children.Keys {
        
        public ServerChildren() {
            updateKeys();
        }
        public void updateKeys() {
            setKeys(ServerRegistry.getInstance().getServers());
        }
        protected Node[] createNodes(Object key) {
            Server s = (Server) key;
            //return new Node[] {new FilterNode(((ServerRegistryNode)getNode()).getServerNode(s))};
            return new Node[] {((ServerRegistryNode)getNode()).getServerNode(s)};
        }
    }
}


