/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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