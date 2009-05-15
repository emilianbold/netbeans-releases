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

import java.beans.PropertyVetoException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.netbeans.modules.hudson.api.HudsonChangeListener;
import org.netbeans.modules.hudson.impl.HudsonInstanceImpl;
import org.netbeans.modules.hudson.impl.HudsonManagerImpl;
import org.netbeans.modules.hudson.ui.actions.AddInstanceAction;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Root node in Services tab.
 */
public class HudsonRootNode extends AbstractNode {

    private static final String HUDSON_NODE_NAME = "hudson"; // NOI18N
    private static final String ICON_BASE = "org/netbeans/modules/hudson/ui/resources/hudson.png"; // NOI18N
    private static final Logger LOG = Logger.getLogger(HudsonRootNode.class.getName());
    
    @ServicesTabNodeRegistration(name=HUDSON_NODE_NAME, displayName="#LBL_HudsonNode", iconResource=ICON_BASE, position=488)
    public static HudsonRootNode getDefault() {
        return new HudsonRootNode();
    }

    private HudsonRootNode() {
        super(Children.create(new RootNodeChildren(), true));
        setName(HUDSON_NODE_NAME);
        setDisplayName(NbBundle.getMessage(HudsonRootNode.class, "LBL_HudsonNode"));
        setIconBaseWithExtension(ICON_BASE);
    }
    
    public @Override Action[] getActions(boolean context) {
        return new Action[] {
            new AddInstanceAction(),
        };
    }
    
    private static class RootNodeChildren extends ChildFactory<HudsonInstanceImpl> implements HudsonChangeListener {
        
        public RootNodeChildren() {
            HudsonManagerImpl.getDefault().addHudsonChangeListener(this);
        }

        protected @Override Node createNodeForKey(HudsonInstanceImpl key) {
            return new HudsonInstanceNode(key);
        }
        
        protected boolean createKeys(List<HudsonInstanceImpl> toPopulate) {
            toPopulate.addAll(HudsonManagerImpl.getDefault().getInstances());
            Collections.sort(toPopulate);
            return true;
        }

        public void stateChanged() {}
        
        public void contentChanged() {
            refresh(false);
        }

    }

    /**
     * Try to select a node somewhere beneath the root node.
     * @param path a path as in {@link NodeOp#findPath(Node, String[])}
     */
    public static void select(final String... path) {
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                TopComponent tab = WindowManager.getDefault().findTopComponent("services"); // NOI18N
                if (tab == null) {
                    // XXX have no way to open it, other than by calling ServicesTabAction
                    LOG.fine("No ServicesTab found");
                    return;
                }
                tab.open();
                tab.requestActive();
                if (!(tab instanceof ExplorerManager.Provider)) {
                    LOG.fine("ServicesTab not an ExplorerManager.Provider");
                    return;
                }
                ExplorerManager mgr = ((ExplorerManager.Provider) tab).getExplorerManager();
                Node root = mgr.getRootContext();
                Node hudson = NodeOp.findChild(root, HUDSON_NODE_NAME);
                if (hudson == null) {
                    LOG.fine("ServicesTab does not contain " + HUDSON_NODE_NAME);
                    return;
                }
                Node selected;
                try {
                    selected = NodeOp.findPath(hudson, path);
                } catch (NodeNotFoundException x) {
                    LOG.log(Level.FINE, "Could not find subnode", x);
                    selected = x.getClosestNode();
                }
                try {
                    mgr.setSelectedNodes(new Node[] {selected});
                } catch (PropertyVetoException x) {
                    LOG.log(Level.FINE, "Could not select path", x);
                }
            }
        });
    }

}
