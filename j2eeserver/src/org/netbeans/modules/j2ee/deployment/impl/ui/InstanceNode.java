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
import org.netbeans.modules.j2ee.deployment.plugins.api.StartServer;

/**
 * @author George FinKlang
 */

public class InstanceNode extends AbstractNode implements ServerInstance.RefreshListener {
    
    protected ServerInstance instance;
    
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
    
    //static javax.swing.Action[] runningActions;
    //static javax.swing.Action[] stoppedActions;
    static javax.swing.Action[] unknownActions;
    
    /*private javax.swing.Action[] getRunningActions() {
        if (runningActions == null) {
            runningActions = new SystemAction[] {
                SystemAction.get(StopServerAction.class),
                SystemAction.get(RemoveInstanceAction.class)
            };
        }
        return runningActions;
    }
    private javax.swing.Action[] getStoppedActions() {
        if (stoppedActions == null) {
            stoppedActions = new SystemAction[] {
                SystemAction.get(StartServerAction.class),
                SystemAction.get(RemoveInstanceAction.class)
            };
        }
        return stoppedActions;
    }*/
    private javax.swing.Action[] getUnknownActions() {
        if (unknownActions == null) {
            unknownActions = new SystemAction[] {
                SystemAction.get(ServerStatusAction.class),
                SystemAction.get(RefreshAction.class),
                SystemAction.get(RemoveInstanceAction.class)
            };
        }
        return unknownActions;
    }

    public javax.swing.Action[] getActions(boolean context) {
        return  getUnknownActions();
        /*Boolean isRunning = instance.checkRunning();
        if (isRunning == null) {
            return getUnknownActions();
        } else if (isRunning.booleanValue()) {
            return getRunningActions();
        } else
            return getStoppedActions();*/
    }
    
    ServerInstance getServerInstance() {
        return (ServerInstance) getCookieSet().getCookie(ServerInstance.class);
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
            instance.reset();
            instance.refresh(instance.isRunning());
        }
    }

    public void handleRefresh(boolean running) {
        if (! running) {
            setChildren(new InstanceChildren(instance));
        }
        InstanceChildren ch = (InstanceChildren) getChildren();
        ch.updateKeys();
    }    
    
     public static class InstanceChildren extends Children.Keys {
        ServerInstance serverInstance;
        public InstanceChildren(ServerInstance inst) {
            this.serverInstance = inst;
        }
        protected void addNotify() {
            setKeys(serverInstance.getTargets());
        }
        public void updateKeys() {
            setKeys(serverInstance.getTargets());
        }
        protected void removeNotify() {
            setKeys(java.util.Collections.EMPTY_SET);
        }
        protected org.openide.nodes.Node[] createNodes(Object obj) {
            ServerTarget child = (ServerTarget) obj;
            return new Node[] { new TargetBaseNode(org.openide.nodes.Children.LEAF, child) };
        }
    }
    
}
