/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.team.server.nb;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.netbeans.modules.team.server.api.TeamServerManager;
import org.netbeans.modules.team.server.ui.common.TeamServerComparator;
import org.netbeans.modules.team.server.ui.spi.TeamServer;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Root node in Services tab.
 * @author Jan Becicka
 */
public class TeamRootNode extends AbstractNode {

    public static final String TEAM_NODE_NAME = "team"; // NOI18N
    private static final String ICON_BASE = "org/netbeans/modules/team/server/resources/team-small.png"; // NOI18N
    
    @ServicesTabNodeRegistration(name=TEAM_NODE_NAME, displayName="#LBL_TeamNode", iconResource=ICON_BASE, position=489) // NOI18N
    public static TeamRootNode getDefault() {
        return new TeamRootNode();
    }

    private TeamRootNode() {
        super(Children.create(new RootNodeChildren(), true));
        setName(TEAM_NODE_NAME);
        setDisplayName(NbBundle.getMessage(TeamRootNode.class, "LBL_TeamNode"));
        setIconBaseWithExtension(ICON_BASE);
    }
    
    public @Override Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(new AddInstanceAction());
        return actions.toArray(new Action[actions.size()]);
    }

    private static class RootNodeChildren extends ChildFactory<TeamServer> implements PropertyChangeListener {
        
        public RootNodeChildren() {
            TeamServerManager.getDefault().addPropertyChangeListener(this);
        }

        protected @Override Node createNodeForKey(TeamServer key) {
            return new TeamServerInstanceNode(key);
        }
        
        @Override
        protected boolean createKeys(List<TeamServer> toPopulate) {
            toPopulate.addAll(TeamServerManager.getDefault().getTeamServers());
            Collections.sort(toPopulate, new TeamServerComparator());
            return true;
        }
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            refresh(false);
        }

    }
    
    private static final Logger LOG = Logger.getLogger(AddInstanceAction.class.getName());
    private static class AddInstanceAction extends org.netbeans.modules.team.server.ui.common.AddInstanceAction {
        @Override
        public void actionPerformed(ActionEvent ae) {
            super.actionPerformed(ae); 
            TeamServer teamServer = getTeamServer();
            if(teamServer != null) {
                selectNode(teamServer.getUrl().toString());
            }
        }
        private static void selectNode(final String... path) {
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
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
                    final ExplorerManager mgr = ((ExplorerManager.Provider) tab).getExplorerManager();
                    final Node root = mgr.getRootContext();
                    RequestProcessor.getDefault().post(new Runnable() {
                        @Override
                        public void run() {
                            Node hudson = NodeOp.findChild(root, TeamRootNode.TEAM_NODE_NAME);
                            if (hudson == null) {
                                LOG.fine("ServicesTab does not contain " + TeamRootNode.TEAM_NODE_NAME);
                                return;
                            }
                            Node _selected;
                            try {
                                _selected = NodeOp.findPath(hudson, path);
                            } catch (NodeNotFoundException x) {
                                LOG.log(Level.FINE, "Could not find subnode", x);
                                _selected = x.getClosestNode();
                            }
                            final Node selected = _selected;
                            Mutex.EVENT.readAccess(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        mgr.setSelectedNodes(new Node[] {selected});
                                    } catch (PropertyVetoException x) {
                                        LOG.log(Level.FINE, "Could not select path", x);
                                    }
                                }
                            });
                        }
                    });
                }
            });
        }
        
    }
}
