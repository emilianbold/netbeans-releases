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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonChangeListener;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJob.Color;
import org.netbeans.modules.hudson.api.HudsonVersion;
import org.netbeans.modules.hudson.api.HudsonView;
import org.netbeans.modules.hudson.impl.HudsonInstanceImpl;
import org.netbeans.modules.hudson.impl.HudsonViewImpl;
import org.netbeans.modules.hudson.ui.actions.OpenUrlAction;
import org.netbeans.modules.hudson.ui.actions.RemoveInstanceAction;
import org.netbeans.modules.hudson.ui.actions.SynchronizeAction;
import org.netbeans.modules.hudson.util.Utilities;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 * Describes HudsonInstance in the Runtime Tab
 *
 * @author Michal Mocnak
 */
public class HudsonInstanceNode extends AbstractNode {
    
    private static final String ICON_BASE = "org/netbeans/modules/hudson/ui/resources/instance.png";
    
    private HudsonInstanceImpl instance;
    private InstanceNodeChildren children;
    
    private boolean warn = false;
    private boolean run = false;
    private boolean alive = false;
    private boolean version = false;
    
    /**
     *
     * @param instance
     */
    public HudsonInstanceNode(final HudsonInstanceImpl instance) {
        super(new Children.Array(), Lookups.singleton(instance));
        
        children = new InstanceNodeChildren(instance);
        
        setDisplayName(instance.getName());
        setShortDescription(instance.getUrl());
        setIconBaseWithExtension(ICON_BASE);
        
        this.instance = instance;
        
        // Add change listener into instance
        instance.addHudsonChangeListener(new HudsonChangeListener() {
            public void stateChanged() {
                refreshState();
            }
            
            public void contentChanged() {
                refreshContent();
            }
        });
        
        // Refresh
        refreshState();
        refreshContent();
    }
    
    @Override
    public String getHtmlDisplayName() {
        boolean pers = instance.isPersisted();
        return (run ? "<b>" : "") + (warn ? "<font color=\"#A40000\">" : "") +
                instance.getName() + (warn ? "</font>" : "") + (run ? "</b>" : "") +
                (alive ? (version ? "" : " <font color=\"#A40000\">" +
                NbBundle.getMessage(HudsonInstanceNode.class, "MSG_WrongVersion",
                HudsonVersion.SUPPORTED_VERSION) + "</font>") : " <font color=\"#A40000\">" +
                NbBundle.getMessage(HudsonInstanceNode.class, "MSG_Disconnected") + "</font>") +
                (!pers ? "<i>" : "") + "(Project based)" + (!pers ? "</i>" : "");
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(SynchronizeAction.class),
            SystemAction.get(OpenUrlAction.class),
            null,
            SystemAction.get(RemoveInstanceAction.class),
            null,
            SystemAction.get(PropertiesAction.class)
        };
    }
    
    @Override
    protected Sheet createSheet() {
        // Create a property sheet
        Sheet s = super.createSheet();
        
        // Put properties in
        s.put(instance.getProperties().getSheetSet());
        
        return s;
    }
    
    private synchronized void refreshState() {
        // Save html name
        String oldHtmlName = getHtmlDisplayName();
        
        alive = instance.isConnected();
        version = Utilities.isSupportedVersion(instance.getVersion());
        
        // Refresh children
        if (!alive || !version)
            setChildren(new Children.Array());
        else if (getChildren().getNodesCount() == 0)
            setChildren(children);
        
        // Fire changes if any
        fireDisplayNameChange(oldHtmlName, getHtmlDisplayName());
    }
    
    private synchronized void refreshContent() {
        // Get HTML Display Name
        String oldHtmlName = getHtmlDisplayName();
        
        // Clear flags
        warn = false;
        run = false;
        
        // Refresh state flags
        for (HudsonJob job : instance.getJobs()) {
            if (job.getColor().equals(Color.red) || job.getColor().equals(Color.red_anime))
                warn = true;
            
            if (job.getColor().equals(Color.blue_anime) || job.getColor().equals(Color.grey_anime)
                    || job.getColor().equals(Color.red_anime) || job.getColor().equals(Color.yellow_anime))
                run = true;
            
            if (warn && run)
                break; // it's not necessary to continue
        }
        
        // Fire changes if any
        fireDisplayNameChange(oldHtmlName, getHtmlDisplayName());
    }
    
    private static class InstanceNodeChildren extends Children.Keys<Node> implements HudsonChangeListener {
        
        private HudsonInstanceImpl instance;
        private HudsonQueueNode queue;
        
        private java.util.Map<String, HudsonViewNode> cache = new HashMap<String, HudsonViewNode>();
        
        public InstanceNodeChildren(HudsonInstanceImpl instance) {
            this.instance = instance;
            this.queue = new HudsonQueueNode(instance);
            
            // Add HudsonChangeListener into instance
            instance.addHudsonChangeListener(this);
        }
        
        protected Node[] createNodes(Node node) {
            return new Node[] {node};
        }
        
        @Override
        protected void addNotify() {
            super.addNotify();
            refreshKeys();
        }
        
        @Override
        protected void removeNotify() {
            setKeys(Collections.<Node>emptySet());
            super.removeNotify();
        }
        
        private void refreshKeys() {
            List<Node> l = new ArrayList<Node>();
            
            // Add queue node
            l.add(queue);
            
            for (Node n : getKeys())
                l.add(n);
            
            setKeys(l);
        }
        
        private Collection<Node> getKeys() {
            List<Node> l = new ArrayList<Node>();
            
            for (HudsonView v : instance.getViews())
                l.add(HudsonNodesFactory.getDefault().getHudsonViewNode(this, (HudsonViewImpl) v));
            
            return l;
        }
        
        public void stateChanged() {}
        
        public void contentChanged() {
            refreshKeys();
        }
    }
}