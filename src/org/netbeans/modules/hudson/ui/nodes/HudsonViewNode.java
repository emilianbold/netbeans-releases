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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonChangeListener;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonView;
import org.netbeans.modules.hudson.impl.HudsonJobImpl;
import org.netbeans.modules.hudson.impl.HudsonViewImpl;
import org.netbeans.modules.hudson.ui.actions.OpenUrlAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 * Describes HudsonView in RuntimeTab
 *
 * @author Michal Mocnak
 */
public class HudsonViewNode extends AbstractNode {
    
    private static final String ICON_BASE = "org/netbeans/modules/hudson/ui/resources/jobs.png";
    
    private HudsonView view;
    
    public HudsonViewNode(HudsonViewImpl view) {
        super(new ViewNodeChildren(view), Lookups.singleton(view));
        
        this.view = view;
        
        setDisplayName(view.getName() + " " +
                NbBundle.getMessage(HudsonViewNode.class, "LBL_ViewNode"));
        setIconBaseWithExtension(ICON_BASE);
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(OpenUrlAction.class)
        };
    }
    
    public void setHudsonView(HudsonView view) {
        this.view = view;
    }
    
    public static class ViewNodeChildren extends Children.Keys<HudsonJobImpl> implements HudsonChangeListener {
        
        private HudsonInstance instance;
        private HudsonView view;
        
        private java.util.Map<String, HudsonJobNode> cache = new HashMap<String, HudsonJobNode>();
        
        public ViewNodeChildren(HudsonView view) {
            this.view = view;
            
            // Lookup HudsonInstance
            instance = view.getLookup().lookup(HudsonInstance.class);
            
            // Add HudsonChangeListener into instance
            instance.addHudsonChangeListener(this);
        }
        
        protected Node[] createNodes(HudsonJobImpl job) {
            return new Node[] {HudsonNodesFactory.getDefault().getHudsonJobNode(this, job)};
        }
        
        @Override
        protected void addNotify() {
            super.addNotify();
            setKeys(getKeys());
        }
        
        @Override
        protected void removeNotify() {
            setKeys(Collections.<HudsonJobImpl>emptySet());
            super.removeNotify();
        }
        
        private void refreshKeys() {
            setKeys(getKeys());
        }
        
        private Collection<HudsonJobImpl> getKeys() {
            List<HudsonJobImpl> l = new ArrayList<HudsonJobImpl>();
            
            for (HudsonJobImpl job : Arrays.asList(instance.getJobs().toArray(new HudsonJobImpl[] {}))) {
                if (job.getViews().contains(view))
                    l.add(job);
            }
            
            return l;
        }
        
        public void setView(HudsonView view) {
            this.view = view;
            
            // Refresh
            refreshKeys();
        }
        
        public void stateChanged() {}
        
        public void contentChanged() {
            refreshKeys();
        }
    }
}