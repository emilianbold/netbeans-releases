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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.hudson.ui.nodes;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonChangeListener;
import org.netbeans.modules.hudson.api.HudsonManager;
import org.netbeans.modules.hudson.impl.HudsonInstanceImpl;
import org.netbeans.modules.hudson.impl.HudsonManagerImpl;
import org.netbeans.modules.hudson.ui.actions.AddInstanceAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.actions.SystemAction;

/**
 * Root node which is situated in the RuntimeTab
 *
 * @author Michal Mocnak
 */
public class HudsonRootNode extends AbstractNode {
    
    private static final String ICON_BASE = "org/netbeans/modules/hudson/ui/resources/hudson.png";
    
    /** Init lock */
    private static final Object LOCK_INIT = new Object();
    
    /** The only instance of the hudson nodes factory in the system */
    private static HudsonRootNode defaultInstance;
    
    /**
     * Creates a new instance of HudsonRootNode
     */
    private HudsonRootNode() {
        super(new RootNodeChildren());
        
        setDisplayName(NbBundle.getMessage(HudsonRootNode.class, "LBL_HudsonNode"));
        setIconBaseWithExtension(ICON_BASE);
    }
    
    /**
     * Creates default instance of HudsonRootNode
     *
     * @return default instance of HudsonRootNode
     */
    public static HudsonRootNode getDefault() {
        synchronized(LOCK_INIT) {
            if (null == defaultInstance)
                defaultInstance = new HudsonRootNode();
            
            return defaultInstance;
        }
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {SystemAction.get(AddInstanceAction.class)};
    }
    
    private static class RootNodeChildren extends Children.Keys<Object> implements HudsonChangeListener {
        
        private final Node tooltip = new TooltipNode(NbBundle.getMessage(HudsonRootNode.class,
                "MSG_Tooltip"), SystemAction.get(AddInstanceAction.class));
        
        /**
         * Creates a new instance of RootNodeChildren
         */
        public RootNodeChildren() {
            HudsonManagerImpl.getDefault().addHudsonChangeListener(this);
        }
        
        protected Node[] createNodes(Object o) {
            if (o instanceof HudsonInstanceImpl)
                return new Node[] {new HudsonInstanceNode((HudsonInstanceImpl) o)};
            
            if (o instanceof TooltipNode)
                return new Node[] {(TooltipNode) o};
            
            return new Node[] {};
        }
        
        @Override
        protected void addNotify() {
            super.addNotify();
            refreshKeys();
        }
        
        @Override
        protected void removeNotify() {
            setKeys(Collections.<HudsonInstanceImpl>emptySet());
            super.removeNotify();
        }
        
        private void refreshKeys() {
            Collection<HudsonInstanceImpl> keys = getKeys();
            
            if (keys.size() == 0 && !NbPreferences.forModule(
                    HudsonManager.class).getBoolean(HudsonManagerImpl.STARTUP_PROP, false))
                setKeys(new Object[] {tooltip});
            else
                setKeys(keys);
        }
        
        private Collection<HudsonInstanceImpl> getKeys() {
            List<HudsonInstanceImpl> l = Arrays.asList(HudsonManagerImpl.getDefault().
                    getInstances().toArray(new HudsonInstanceImpl[] {}));
            
            // Sort repositories
            Collections.sort(l);
            
            return l;
        }
        
        public void stateChanged() {}
        
        public void contentChanged() {
            refreshKeys();
        }
    }
}