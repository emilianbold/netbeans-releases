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

package org.netbeans.modules.identity.server.manager.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import org.netbeans.modules.identity.server.manager.api.ServerInstance;
import org.netbeans.modules.identity.server.manager.ui.actions.CustomizerAction;
import org.netbeans.modules.identity.server.manager.ui.actions.RemoveServerInstanceAction;
import org.netbeans.modules.identity.server.manager.ui.actions.ViewAdminConsoleAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;

/**
 * This class represents a AM server instance node in the Runtime tab.
 *
 * Created on June 14, 2006, 12:48 AM 
 *
 * @author ptliu
 */
public class ServerInstanceNode extends AbstractNode {
    
    private static final String SERVER_INSTANCE_ICON = "org/netbeans/modules/identity/server/manager/ui/resources/ServerInstance.png";//NOI18N
    
    private static final String HELP_ID = "idmtools_am_ww_am_instances"; //NOI18N
    
    private ServerInstance instance;
    
    /**
     * Creates a new instance of ServerInstanceNode
     *
     * TODO: use Lookup
     */
    public ServerInstanceNode(ServerInstance instance) {
        super(new ServerInstanceChildren(instance));
        
        this.instance = instance;
        
        setName(instance.getID());
        setDisplayName(instance.getDisplayName());
        setIconBaseWithExtension(SERVER_INSTANCE_ICON);
        instance.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                refreshNode();
               
            }
        });
    }
    
    private void refreshNode() {
        setDisplayName(instance.getDisplayName());
    }
    
    public Action[] getActions(boolean context) {
        Action[] actions = new Action[] {
            SystemAction.get(ViewAdminConsoleAction.class),
            null,
            SystemAction.get(RemoveServerInstanceAction.class),
            null,
            SystemAction.get(CustomizerAction.class)
        };
        
        return actions;
    }
    
    public ServerInstance getInstance() {
        return instance;
    }
    
    public String getAdminURL() {
        return instance.getAdminURL();
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(HELP_ID);
    }
    
    private static class ServerInstanceChildren extends Children.Keys {
        private static final String PROFILES_NODE_KEY = "Profiles";     //NOI18N
        
        private ServerInstance instance;
        
        public ServerInstanceChildren(ServerInstance instance) {
            this.instance = instance;
        }
        
        protected void addNotify() {
            setKeys(new String[] {PROFILES_NODE_KEY});
        }
        
        protected Node[] createNodes(Object key) {
            if (key.equals(PROFILES_NODE_KEY)) {
                return new Node[] {new ProfilesNode(instance)};
            }
            
            return null;
        }
    }
}
