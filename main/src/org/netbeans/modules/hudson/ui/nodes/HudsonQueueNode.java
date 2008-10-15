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

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonChangeListener;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.impl.HudsonInstanceImpl;
import org.netbeans.modules.hudson.impl.HudsonJobImpl;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

/**
 * Describes Hudson's build queue in RuntimeTab
 *
 * @author Michal Mocnak
 */
public class HudsonQueueNode extends AbstractNode {
    
    private static final String ICON_BASE = "org/netbeans/modules/hudson/ui/resources/queue.png";
    
    public HudsonQueueNode(HudsonInstanceImpl instance) {
        super(new QueueNodeChildren(instance));
        
        setDisplayName(NbBundle.getMessage(HudsonQueueNode.class, "LBL_QueueNode"));
        setIconBaseWithExtension(ICON_BASE);
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {};
    }
    
    @Override
    public PasteType getDropType(final Transferable t, int action, int index) {
        return new PasteType() {
            public Transferable paste() throws IOException {
                Node n = NodeTransfer.node(t, NodeTransfer.DND_COPY);
                
                if (null != n && n instanceof HudsonJobNode) {
                    HudsonJob job = ((HudsonJobNode) n).getJob();
                    
                    if (job.isBuildable())
                        job.start();    
                }
                
                return t;
            }
        };
    }
    
    private static class QueueNodeChildren extends Children.Keys<HudsonJobImpl> implements HudsonChangeListener {
        
        private HudsonInstanceImpl instance;
        
        public QueueNodeChildren(HudsonInstanceImpl instance) {
            this.instance = instance;
            
            // Add HudsonChangeListener into instance
            instance.addHudsonChangeListener(this);
        }
        
        protected Node[] createNodes(HudsonJobImpl job) {
            return new Node[] {HudsonNodesFactory.getDefault().getHudsonJobNode(this, job)};
        }
        
        @Override
        protected void addNotify() {
            super.addNotify();
            refreshKeys();
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
                if (job.isInQueue())
                    l.add(job);
            }
            
            return l;
        }
        
        public void stateChanged() {}
        
        public void contentChanged() {
            refreshKeys();
        }
    }
}