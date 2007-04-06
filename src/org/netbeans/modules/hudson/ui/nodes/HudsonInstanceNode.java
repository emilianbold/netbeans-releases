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

package org.netbeans.modules.hudson.ui.nodes;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.hudson.api.HudsonJob.Color;
import org.netbeans.modules.hudson.impl.HudsonInstanceImpl;
import org.netbeans.modules.hudson.impl.HudsonJobImpl;
import org.netbeans.modules.hudson.ui.actions.OpenUrlAction;
import org.netbeans.modules.hudson.ui.actions.RemoveInstanceAction;
import org.netbeans.modules.hudson.ui.actions.SynchronizeAction;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
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
    private boolean warn = false;
    private boolean run = false;
    private boolean alive = false;
    
    /**
     *
     * @param instance
     */
    public HudsonInstanceNode(final HudsonInstanceImpl instance) {
        super(new InstanceNodeChildren(instance), Lookups.singleton(instance));
        
        setDisplayName(instance.getName());
        setShortDescription(instance.getUrl());
        setIconBaseWithExtension(ICON_BASE);
        
        this.instance = instance;
        
        addNodeListener(new NodeAdapter() {
            @Override
            public void childrenAdded(NodeMemberEvent event) {
                refreshState();
            }
            
            @Override
            public void childrenReordered(NodeReorderEvent event) {
                refreshState();
            }
        });
        
        refreshState();
    }
    
    @Override
    public String getHtmlDisplayName() {
        return (run ? "<b>" : "") + (warn ? "<font color=\"#A40000\">" : "") +
                instance.getName() + (warn ? "</font>" : "") + (run ? "</b>" : "") +
                (alive ? "" : " <font color=\"#A40000\">" +
                NbBundle.getMessage(HudsonInstanceNode.class, "MSG_Disconnected") + "</font>");
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(SynchronizeAction.class),
            SystemAction.get(OpenUrlAction.class),
            SystemAction.get(RemoveInstanceAction.class),
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
    
    private void refreshState() {
        warn = false;
        run = false;
        alive = false;
        
        for (Node n : getChildren().getNodes()) {
            if (n instanceof HudsonJobNode) {
                Color c = ((HudsonJobNode) n).getColor();
                
                if (c.equals(Color.RED) || c.equals(Color.RED_ANIME))
                    warn = true;
                
                if (c.equals(Color.BLUE_ANIME) || c.equals(Color.GREY_ANIME)
                        || c.equals(Color.RED_ANIME) || c.equals(Color.YELLOW_ANIME))
                    run = true;
            }
        }
        
        alive = instance.isConnected();
        
        setDisplayName(getHtmlDisplayName());
    }
    
    private static class InstanceNodeChildren extends Children.Keys<HudsonJobImpl> implements ChangeListener {
        
        private HudsonInstanceImpl instance;
        
        public InstanceNodeChildren(HudsonInstanceImpl instance) {
            this.instance = instance;
            instance.addChangeListener(this);
        }
        
        protected Node[] createNodes(HudsonJobImpl job) {
            return new Node[] {new HudsonJobNode(job)};
        }
        
        @Override
        protected void addNotify() {
            super.addNotify();
            setKeys(getKeys());
        }
        
        @Override
        protected void removeNotify() {
            instance.removeChangeListener(this);
            setKeys(Collections.<HudsonJobImpl>emptySet());
            super.removeNotify();
        }
        
        public void stateChanged(ChangeEvent e) {
            setKeys(getKeys());
        }
        
        private Collection<HudsonJobImpl> getKeys() {
            List<HudsonJobImpl> l = Arrays.asList(instance.getJobs().toArray(new HudsonJobImpl[] {}));
            
            // Sort repositories
            Collections.sort(l);
            
            return l;
        }
    }
}