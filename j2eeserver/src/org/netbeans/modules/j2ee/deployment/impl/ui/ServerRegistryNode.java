/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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


/*
 * ServerRegNode.java -- synopsis
 *
 */
package org.netbeans.modules.j2ee.deployment.impl.ui;

import javax.swing.Action;
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
    
    private transient HelpCtx helpCtx;
    
    /** Creates a new instance of ServerRegistryNode2 */
    private ServerRegistryNode() {
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

    @Override
    public Action[] getActions(boolean context) {
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
            // XXX this is commented out because of the UISupoort calling this
            // method - it should be fixed in UISupport as it is quite ugly
            //instance.refresh(); // detect the server instance status
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
    
    private static ServerRegistryNode instance;
    
    public static synchronized ServerRegistryNode getServerRegistryNode() {
        if (instance == null) {
            instance = new ServerRegistryNode();
        }
        return instance;
        //return Lookups.forPath("UI/Runtime").lookup(ServerRegistryNode.class);
    }
}
