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

import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerTarget;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.nodes.Node;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


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
    private InstanceTargetChildren instanceTargetChildren;
    
    public InstanceTargetXNode(Node instanceNode, ServerInstance instance) {
        this(instanceNode, Node.EMPTY, new InstanceTargetChildren(Node.EMPTY));
        this.instance = instance;
        instanceProperties = instance.getInstanceProperties();
        instance.addStateListener(this);
    }
    
    private InstanceTargetXNode(Node instanceNode, Node xnode, InstanceTargetChildren instanceTargetChildren) {
        super(instanceNode, xnode, true, instanceTargetChildren);
        this.instanceTargetChildren = instanceTargetChildren;
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
        if (newState == ServerInstance.STATE_RUNNING || newState == ServerInstance.STATE_DEBUGGING
            || newState == ServerInstance.STATE_PROFILING) {
            if (oldState == ServerInstance.STATE_SUSPENDED) {
                // it looks like the server is being debugged right now, show the
                // cached nodes rather than trying to retrieve new ones
                instanceTargetChildren.showLastNodes();
                getChildren().getNodes(true); // this will make the nodes expand
            } else {
                instanceTarget = null;
                resetDelegateTargetNode();
                instanceTargetChildren.hideNodes();
                setChildren(instanceTargetChildren);
                instanceTargetChildren.updateNodes(this);
                getChildren().getNodes(true); // this will make the nodes expand
            }
        } else {
            instanceTargetChildren.hideNodes();
        }
    }
    
    public static class InstanceTargetChildren extends Children {
        
        private ServerTarget target;
        private Node lastDelegateTargetNode;
        
        public InstanceTargetChildren(Node original) {
            super(original);
        }
        
        public void updateNodes(final InstanceTargetXNode parent) {
            if (isFurtherExpandable()) {
                if (original == Node.EMPTY) {
                    changeOriginal(createWaitNode());
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            Node newOriginal = null;
                            if (parent != null) {
                                newOriginal = parent.getDelegateTargetNode();
                                lastDelegateTargetNode = newOriginal;
                            }
                            if (newOriginal != null) {
                                changeOriginal(newOriginal);
                            } else {
                                changeOriginal(Node.EMPTY);
                            }
                        }
                    });
                }
            } else {
                changeOriginal(Node.EMPTY);
            }
        }
        
        public void hideNodes() {
            changeOriginal(Node.EMPTY);
        }
        
        public void showLastNodes() {
            Node node = lastDelegateTargetNode;
            if (node != null) {
                changeOriginal(node);
            }
        }
        
        private Node createWaitNode() {
            AbstractNode node = new AbstractNode(Children.LEAF);
            node.setName(NbBundle.getMessage(InstanceTargetXNode.class, "LBL_WaitNode_DisplayName"));
            node.setIconBaseWithExtension("org/netbeans/modules/j2ee/deployment/impl/ui/resources/wait.gif"); // NOI18N
            
            Children.Array children = new Children.Array();
            children.add(new Node[]{node});
            return new AbstractNode(children);
        }
        
        private boolean isFurtherExpandable() {
            ServerRegistryNode root = ServerRegistryNode.getServerRegistryNode();
            if (root != null) {
                return root.isExpandablePassTargetNode();
            }
            return true;
        }
    }
}
