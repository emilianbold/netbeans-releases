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

import org.openide.nodes.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.netbeans.modules.j2ee.deployment.impl.*;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.*;
import org.openide.util.actions.*;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.RemoveInstanceAction;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;

/**
 * @author George FinKlang
 */

public class InstanceNode extends AbstractNode implements ServerInstance.RefreshListener {
    
    private ServerInstance instance;
    
    public InstanceNode(ServerInstance instance) {
        super(new InstanceChildren(instance));
        this.instance = instance;
        setDisplayName(instance.getDisplayName());
        setName(instance.getUrl());
        setIconBase(instance.getServer().getIconBase());
        getCookieSet().add(instance);
        getCookieSet().add(new Refresher());
        instance.addRefreshListener(this);
    }
    
    public javax.swing.Action[] getActions(boolean context) {
        SystemAction startOrStop = getStartOrStopAction();
        if (startOrStop != null)
            return new SystemAction[] {
                startOrStop,
                // SystemAction.get(SetAsDefaultServerAction.class),
                // SystemAction.get(NodeHelpAction.class),
                SystemAction.get(RemoveInstanceAction.class),
                SystemAction.get(RefreshAction.class)
            };  

        return new SystemAction[] {
            // SystemAction.get(SetAsDefaultServerAction.class),
            // SystemAction.get(NodeHelpAction.class),
            SystemAction.get(RemoveInstanceAction.class),
            SystemAction.get(RefreshAction.class)
        };
    }
    
    private ServerInstance getServerInstance() {
        return (ServerInstance) getCookieSet().getCookie(ServerInstance.class);
    }
    
    private SystemAction getStartOrStopAction() {
        StartServer ss = getServerInstance().getStartServer();
        if (ss == null ||  ! ss.supportsStartDeploymentManager())
            return null;
        System.out.println("getStartOrStopAction: called: isRunning = "+ss.isRunning());
        if (ss.isRunning())
            return SystemAction.get(StopServerAction.class);
        else
            return SystemAction.get(StartServerAction.class);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public org.openide.nodes.Node.Cookie getCookie(Class type) {
        if (ServerInstance.class.isAssignableFrom(type)) {
            return instance;
        }
        return super.getCookie(type);
    }
    
    class Refresher implements RefreshAction.RefreshCookie {
        
        public void refresh() {
            instance.refresh();
            
        }
      
    }
    

        public void handleRefresh() {
            Children old = getChildren();
            Children replacing = new InstanceChildren(instance);
            //PENDING: traverse old subtree to remove listener.
            setChildren(replacing);
        }           
    
    private static class InstanceChildren extends Children.Keys {
        ServerInstance serverInstance;
        InstanceChildren(ServerInstance inst) {
            this.serverInstance = inst;
        }
        protected void addNotify() {
            setKeys(serverInstance.getTargets());
        }
        protected void removeNotify() {
            setKeys(java.util.Collections.EMPTY_SET);
        }
        protected org.openide.nodes.Node[] createNodes(Object obj) {
            ServerTarget child = (ServerTarget) obj;
            return new Node[] { new TargetNode(child) };
        }
    }
    
}
