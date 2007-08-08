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

package org.netbeans.modules.j2ee.deployment.impl.ui;

import javax.swing.Action;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.netbeans.modules.j2ee.deployment.impl.*;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.*;


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
    
    @Override
    public Action[] getActions(boolean b) {
        return new Action[] {};
    }
    
    // StateListener implementation -------------------------------------------
    
    public void stateChanged(int oldState, int newState) {
        if (instance.getServerState() != ServerInstance.STATE_WAITING
            && instance.getServerState() != ServerInstance.STATE_SUSPENDED) {
            setChildren(new InstanceChildren(instance));
            getChildren().getNodes(true);
        } else if (instance.getServerState() == ServerInstance.STATE_SUSPENDED) {
            setChildren(Children.LEAF);
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
