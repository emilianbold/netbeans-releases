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
 * InstanceTargetNode.java
 *
 * Created on December 7, 2003, 9:11 PM
 */

package org.netbeans.modules.j2ee.deployment.impl.ui;

import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerState;
import org.netbeans.modules.j2ee.deployment.impl.ServerTarget;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;


/**
 * A node for an admin instance that is also a target server.
 *
 * @author  nn136682
 */
public class InstanceTargetXNode extends FilterXNode implements ServerInstance.RefreshListener, 
        PropertyChangeListener {
    private ServerTarget instanceTarget;
    private ServerInstance instance;
    private InstanceProperties instanceProperties;
    
    public InstanceTargetXNode(Node instanceNode, ServerInstance instance) {
        this(instanceNode, Node.EMPTY, instance);
        instance.addRefreshListener(this);
    }
    
    public InstanceTargetXNode(Node instanceNode, Node xnode, ServerInstance instance) {
        super(instanceNode, xnode, true, new InstanceTargetChildren(xnode, instance));
        this.instance = instance;
        instanceProperties = instance.getInstanceProperties();
        instanceProperties.addPropertyChangeListener(this);
    }
    
    // this should preven customizer action from being displayed in server instance
    // context menu in the server registry - customizer should be accessible only 
    // through the server manager
    public boolean hasCustomizer() {
        return false;
    }
    
    public String getDisplayName() {
        return instance.getDisplayNameWithState();
    }
    
    public String getName() {
        String name = instanceProperties.getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        return name == null ? "" : name; // NOI18N
    }
    
    public void setName(String name) {
        instanceProperties.setProperty(InstanceProperties.DISPLAY_NAME_ATTR, name);
    }
    
    public boolean canRename() { 
        return true;
    }
    
    private ServerTarget getServerTarget() {
        if (instanceTarget != null)
            return instanceTarget;
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
    
    /**
     * Handle InstanceProperties changes.
     *
     * @param evt A PropertyChangeEvent object describing the event source 
     *   	and the property that has changed.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (InstanceProperties.DISPLAY_NAME_ATTR.equals(evt.getPropertyName()) &&
            (evt.getNewValue() != null && !evt.getNewValue().equals(evt.getOldValue()))) {
            setDisplayName(getDisplayName());
        }
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
    
    public void handleRefresh(ServerState serverState) {
        if (serverState == ServerState.CHECKING) {
            setDisplayName(instance.getDisplayNameWithState());
            return;
        }
        //if (! running) {
            instanceTarget = null;
            resetDelegateTargetNode();
            setChildren(new InstanceTargetChildren(Node.EMPTY, instance));
        //}
        this.setDisplayName(instance.getDisplayNameWithState());
        ((InstanceTargetChildren)getChildren()).updateKeys();
    }
}

