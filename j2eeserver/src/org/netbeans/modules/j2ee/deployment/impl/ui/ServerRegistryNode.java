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

import java.lang.reflect.InvocationTargetException;
import org.openide.nodes.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.*;
import org.netbeans.modules.j2ee.deployment.impl.*;
import org.netbeans.modules.j2ee.deployment.impl.ui.FilterXNode;
import java.awt.Component;
import java.util.*;

/**
 * The server registry node is a node representing the registry in global options.
 * @author Joe Cortopazzi
 * @author George FinKlang
 */

public class ServerRegistryNode extends AbstractNode
implements ServerRegistry.PluginListener, ServerRegistry.InstanceListener {
    
    static final String REGISTRY_ICON_BASE = "org/netbeans/modules/j2ee/deployment/impl/ui/resources/ServerRegistry";//NOI18N
    
    private transient Map serverNodes = new HashMap();
    private transient HelpCtx helpCtx;
    private boolean expandablePassTargetNode = true;
    
    public ServerRegistryNode() {
        super(new ServerChildren());
        long t0 = System.currentTimeMillis();
        setName("");//NOI18N
        String msg = NbBundle.getBundle(ServerRegistryNode.class).getString("SERVER_REGISTRY_NODE");//NOI18N
        setDisplayName(msg);
        setIconBase(REGISTRY_ICON_BASE);
        String shortDescription = NbBundle.getBundle(ServerRegistryNode.class).getString("SERVER_REGISTRY_SHORT_DESCRIPTION");//NOI18N
        setShortDescription(shortDescription);
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
    
    public void instanceAdded(ServerString instance) {
        refreshServerNode(instance);
    }
    public void instanceRemoved(ServerString instance) {
        refreshServerNode(instance);
    }
    public void changeDefaultInstance(ServerString oldInstance, ServerString instance) {
        setDisplayNameWithDefaultServer(instance == null ? null : instance.getServerInstance());
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
    
    private void updateKeys() {
        ((ServerChildren) getChildren()).updateKeys();
    }
    
    Node getServerNode(Server s) {
        Node node = (Node) serverNodes.get(s);
        if(node == null) {
            try {
                node = s.getNodeProvider().createServerNode(s);
            } catch (Exception e) {
                ErrorManager.getDefault().log(ErrorManager.EXCEPTION, e.getLocalizedMessage());
            }
            serverNodes.put(s,node);
        }
        return node;
    }
    private void initDefaultServerChildrenNodes() {
        ServerString ss = ServerRegistry.getInstance().getDefaultInstance();
        if (ss == null)
            return;
        Server s = ss.getServer();
        if (s == null)
            return;
        Node node = getServerNode(s);
        if (node != null) {
            node.getChildren().getNodes();
        }
    }
    public SystemAction[] createActions() {
        return new SystemAction[] {
            //SystemAction.get(FindDeploymentManagerAction.class),
            SystemAction.get(SetDefaultServerAction.class)
        };
    }
    
    public HelpCtx getHelpCtx() {
        if(helpCtx == null)
            helpCtx = new HelpCtx(NbBundle.getBundle(ServerRegistryNode.class).getString("nodes_server_registry_node_html"));//NOI18N
        return helpCtx;
    }
    
    private void displayNameWithDefaultServer() {
        ServerString server = ServerRegistry.getInstance().getDefaultInstance();
        ServerInstance inst = null;
        if (server != null)
            inst = server.getServerInstance();
        setDisplayNameWithDefaultServer(inst);
    }
    
    void setDisplayNameWithDefaultServer(ServerInstance inst) {
        String name = NbBundle.getMessage(ServerRegistryNode.class,"SERVER_REGISTRY_NODE_NO_DEFAULT");//NOI18N
        if (inst != null) {
            String instanceName = inst.getDisplayName();
            String serverName = inst.getServer().getShortName();
            name = NbBundle.getMessage(ServerRegistryNode.class, "SERVER_REGISTRY_NODE_DEFAULT", instanceName, serverName);
        }
        setDisplayName(name);
    }
    
    private static class ServerChildren extends Children.Keys {
        private boolean listenersAdded = false;
        
        public ServerChildren() {
        }
        public void updateKeys() {
            setKeys(ServerRegistry.getInstance().getServers());
        }
        protected void addNotify() {
            updateKeys();
            
            if (! listenersAdded) {
                ServerRegistryNode parent = (ServerRegistryNode) getNode();
                ServerRegistry.getInstance().addPluginListener(parent);
                ServerRegistry.getInstance().addInstanceListener(parent);
                parent.initDefaultServerChildrenNodes();
                parent.displayNameWithDefaultServer();
                listenersAdded = true;
            }
        }
        protected Node[] createNodes(Object key) {
            Server s = (Server) key;
            //return new Node[] {new FilterNode(((ServerRegistryNode)getNode()).getServerNode(s))};
            return new Node[] {((ServerRegistryNode)getNode()).getServerNode(s)};
        }
    }
    
    public static ServerRegistryNode getServerRegistryNode() {
        try {
            FileSystem defaultFileSystem = Repository.getDefault().getDefaultFileSystem();
            FileObject fo = defaultFileSystem.findResource("UI/Runtime");    //NOI18N
            DataFolder df = (DataFolder) DataObject.find(fo);
            org.openide.util.Lookup l = new FolderLookup(df).getLookup();
            return (ServerRegistryNode) l.lookup(ServerRegistryNode.class);
        } catch (DataObjectNotFoundException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return null;
        }
    }
}


