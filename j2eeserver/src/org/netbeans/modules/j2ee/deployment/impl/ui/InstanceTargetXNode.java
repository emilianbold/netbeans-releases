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

import java.awt.Image;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerTarget;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.openide.util.Utilities;


/**
 * A node for an admin instance that is also a target server. Manager and target
 * nodes are merged into one.
 *
 * @author  nn136682
 */
public class InstanceTargetXNode extends FilterXNode implements ServerInstance.StateListener {
    private ServerTarget instanceTarget;
    private ServerInstance instance;
    private InstanceProperties instanceProperties;
    
    private boolean running;
    
    public InstanceTargetXNode(Node instanceNode, ServerInstance instance) {
        this(instanceNode, Node.EMPTY, instance);
        instance.addStateListener(this);
    }
    
    public InstanceTargetXNode(Node instanceNode, Node xnode, ServerInstance instance) {
        super(instanceNode, xnode, true, new InstanceTargetChildren(xnode, instance));
        this.instance = instance;
        instanceProperties = instance.getInstanceProperties();
        instance.addStateListener(this);
    }
    
    private ServerTarget getServerTarget() {
        if (instanceTarget != null) {
            return instanceTarget;
        }
        instanceTarget = instance.getCoTarget();
        return instanceTarget;
    }
    
    public Node getDelegateTargetNode() {
        if (xnode != null && xnode != Node.EMPTY)
            return xnode;
        ServerTarget st = getServerTarget();
        if (st == null)
            return xnode;
        Node tn = instance.getServer().getNodeProvider().createTargetNode(st);
        if (tn != null)
            xnode = tn;
        return xnode;
    }
    
    private void resetDelegateTargetNode() {
        xnode = null;
    }
    
    public static class InstanceTargetChildren extends Children {
        ServerInstance instance;
        ServerTarget target;
        
        public InstanceTargetChildren(Node original, ServerInstance instance) {
            super(original);
            this.instance = instance;
        }
        protected void addNotify() {
            super.addNotify();
            if (isFurtherExpandable()) {
                if (original == Node.EMPTY) {
                    InstanceTargetXNode parent = (InstanceTargetXNode) getNode();
                    Node newOriginal = null;
                    if (parent != null)
                        newOriginal = parent.getDelegateTargetNode();
                    if (newOriginal != null && newOriginal != Node.EMPTY)
                        this.changeOriginal(newOriginal);
                    else {
                        RequestProcessor.getDefault().post(new Runnable() {
                            public void run() {
                                try {
                                    instance.isRunning();
                                } catch (IllegalStateException e) {
                                    // might happen when user removing instance
                                    org.openide.ErrorManager.getDefault().log(e.toString());
                                }
                            }
                        });
                    }
                }
            } else {
                this.setKeys(java.util.Collections.EMPTY_SET);
            }
        }
        public void updateKeys() {
            addNotify();
        }
        private boolean isFurtherExpandable() {
            ServerRegistryNode root = ServerRegistryNode.getServerRegistryNode();
            if (root != null) 
                return root.isExpandablePassTargetNode();

            return true;
        }
    }
    
    public javax.swing.Action[] getActions(boolean context) {
        List actions = new ArrayList();
        actions.addAll(Arrays.asList(getOriginal().getActions(context)));
        /*Boolean isRunning = instance.checkRunning();
        if (isRunning != null && isRunning.booleanValue()) {*/
        if (getServerTarget() != null) {
            actions.addAll(Arrays.asList(getDelegateTargetNode().getActions(context)));
        }
        
        return (javax.swing.Action[]) actions.toArray(new javax.swing.Action[actions.size()]);
    }
    
    public PropertySet[] getPropertySets() {
        Node delegateNode = getDelegateTargetNode();
        if (delegateNode == null)
            return getOriginal().getPropertySets();
        return FilterXNode.merge(getOriginal().getPropertySets(), delegateNode.getPropertySets());
    }
    
    public org.openide.nodes.Node.Cookie getCookie(Class type) {
        Node tn = getDelegateTargetNode();
        org.openide.nodes.Node.Cookie c = null;
        if (tn != null)
            c = tn.getCookie(type);
        if (c == null)
            c = super.getCookie(type);
        return c;
    }
    
    // StateListener implementation -------------------------------------------
    
    public void stateChanged(int oldState, int newState) {
        if (instance.getServerState() != ServerInstance.STATE_WAITING) {
            instanceTarget = null;
            resetDelegateTargetNode();
            setChildren(new InstanceTargetChildren(Node.EMPTY, instance));
            ((InstanceTargetChildren)getChildren()).updateKeys();
        }
    }
}
