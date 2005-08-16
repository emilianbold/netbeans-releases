/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.impl.ui;

import java.awt.Component;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.netbeans.modules.j2ee.deployment.impl.*;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;

import org.openide.windows.WindowManager;

import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;



/**
 * Instance node is a base for any manager node. The behaviour of this base instance 
 * node can be customized/extended by the manager node provided by the plugin.
 *
 * @author George FinKlang
 */
public class InstanceNode extends AbstractNode implements ServerInstance.StateListener {
    
    private static int cursorChangeCounter = 0;
    
    protected ServerInstance instance;
    
    private boolean running;
    
    public InstanceNode(ServerInstance instance, boolean addStateListener) {
        super(new InstanceChildren(instance));
        this.instance = instance;
        setIconBase(instance.getServer().getIconBase());
        getCookieSet().add(instance);
        if (addStateListener) {
            instance.addStateListener(this);
        }
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
    
    // StateListener implementation -------------------------------------------
    
    public void stateChanged(int oldState, int newState) {
        if (instance.getServerState() != ServerInstance.STATE_WAITING) {
            setChildren(new InstanceChildren(instance));
            InstanceChildren ch = (InstanceChildren) getChildren();
            ch.updateKeys();
        }
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
            //return new Node[] { new TargetBaseNode(org.openide.nodes.Children.LEAF, child) };
            return new Node[] { serverInstance.getServer().
                                 getNodeProvider().createTargetNode(child) };
        }
    }
    
}
