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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.netbeans.modules.hudson.api.HudsonChangeListener;
import org.netbeans.modules.hudson.impl.HudsonInstanceImpl;
import org.netbeans.modules.hudson.impl.HudsonManagerImpl;
import org.netbeans.modules.hudson.ui.actions.AddInstanceAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Root node which is situated in the RuntimeTab
 *
 * @author Michal Mocnak
 */
public class HudsonRootNode extends AbstractNode {
    
    private static final String ICON_BASE = "org/netbeans/modules/hudson/ui/resources/hudson.png"; // NOI18N
    
    /** Init lock */
    private static final Object LOCK_INIT = new Object();
    
    /** The only instance of the hudson nodes factory in the system */
    private static HudsonRootNode defaultInstance;
    
    /**
     * Creates a new instance of HudsonRootNode
     */
    private HudsonRootNode() {
        super(new RootNodeChildren());
        setName("hudson"); // NOI18N
        setDisplayName(NbBundle.getMessage(HudsonRootNode.class, "LBL_HudsonNode"));
        setIconBaseWithExtension(ICON_BASE);
    }
    
    /**
     * Creates default instance of HudsonRootNode
     *
     * @return default instance of HudsonRootNode
     */
    @ServicesTabNodeRegistration(
        name="hudson",
        displayName="org.netbeans.modules.hudson.ui.nodes.Bundle#LBL_HudsonNode",
        iconResource="org/netbeans/modules/hudson/ui/resources/hudson.png",
        position=488
    )
    public static HudsonRootNode getDefault() {
        synchronized(LOCK_INIT) {
            if (null == defaultInstance)
                defaultInstance = new HudsonRootNode();
            
            return defaultInstance;
        }
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            new AddInstanceAction(),
        };
    }
    
    private static class RootNodeChildren extends Children.Keys<HudsonInstanceImpl> implements HudsonChangeListener {
        
        /**
         * Creates a new instance of RootNodeChildren
         */
        public RootNodeChildren() {
            HudsonManagerImpl.getDefault().addHudsonChangeListener(this);
        }
        
        protected Node[] createNodes(HudsonInstanceImpl instance) {
            return new Node[] {new HudsonInstanceNode(instance)};
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
            List<HudsonInstanceImpl> l = Arrays.asList(HudsonManagerImpl.getDefault().
                    getInstances().toArray(new HudsonInstanceImpl[] {}));
            
            // Sort repositories
            Collections.sort(l);
            
            setKeys(l);
        }
        
        public void stateChanged() {}
        
        public void contentChanged() {
            refreshKeys();
        }
    }
}