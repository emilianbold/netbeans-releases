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

package org.netbeans.modules.bugtracking.ui.nodes;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Root node representing Bugtracking in the Servises window
 *
 * @author Tomas Stupka
 */
public class BugtrackingRootNode extends AbstractNode {
    
    private static final String ICON_BASE = "org/netbeans/modules/bugtracking/ui/resources/bugtracking.png"; // NOI18N
    
    /** Init lock */
    private static final Object LOCK_INIT = new Object();

    /** The only instance of the BugtrackingRootNode in the system */
    private static BugtrackingRootNode defaultInstance;
    
    /**
     * Creates a new instance of BugtrackingRootNode
     */
    private BugtrackingRootNode() {
        super(new RootNodeChildren());
        setName("bugtracking"); // NOI18N
        setDisplayName(NbBundle.getMessage(BugtrackingRootNode.class, "LBL_BugtrackingNode")); // NOI18N
        setIconBaseWithExtension(ICON_BASE);
    }
    
    /**
     * Creates default instance of BugtrackingRootNode
     *
     * @return default instance of BugtrackingRootNode
     */
    @ServicesTabNodeRegistration(
        name="bugtracking",                                                                 // NOI18N
        displayName="org.netbeans.modules.bugtracking.ui.nodes.Bundle#LBL_BugtrackingNode", // NOI18N
        iconResource="org/netbeans/modules/bugtracking/ui/resources/bugtracking.png",       // NOI18N
        position=588
    )
    public static BugtrackingRootNode getDefault() {
        synchronized(LOCK_INIT) {
            if (defaultInstance == null) {
                defaultInstance = new BugtrackingRootNode();
            }
            return defaultInstance;
        }
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            new AbstractAction(NbBundle.getMessage(BugtrackingRootNode.class, "LBL_CreateRepository")) { // NOI18N
                public void actionPerformed(ActionEvent e) {
                    BugtrackingUtil.createRepository();
                }
            }
        };
    }
    
    private static class RootNodeChildren extends Children.Keys implements PropertyChangeListener  {

        /**
         * Creates a new instance of RootNodeChildren
         */
        public RootNodeChildren() {
            BugtrackingManager.getInstance().addPropertyChangeListener(this);
        }

        @Override
        protected Node[] createNodes(Object key) {
            if(key instanceof WaitNode) {
                return new Node[] {(Node)key};
            }
            assert key instanceof Repository;
            return new Node[] {((Repository)key).getNode()};
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            refreshKeys();
        }

        @Override
        protected void removeNotify() {
            setKeys(Collections.<Repository>emptySet());
            super.removeNotify();
        }

        private void refreshKeys() {
            AbstractNode waitNode = new WaitNode(org.openide.util.NbBundle.getMessage(BugtrackingRootNode.class, "LBL_Wait")); // NOI18N
            setKeys(Collections.singleton(waitNode));
            BugtrackingManager.getInstance().getRequestProcessor().post(new Runnable() {
                public void run() {
                    List<Repository> l = new ArrayList<Repository>();
                    l.addAll(Arrays.asList(BugtrackingManager.getInstance().getRepositories()));
                    Collections.sort(l, new RepositoryComparator());
                    setKeys(l);
                }
            });
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(BugtrackingManager.EVENT_REPOSITORIES_CHANGED)) {
                refreshKeys();
            }
        }
    }

    private static class RepositoryComparator implements Comparator<Repository> {
        public int compare(Repository r1, Repository r2) {
            if(r1 == null && r2 == null) return 0;
            if(r1 == null) return -1;
            if(r2 == null) return 1;
            return r1.getDisplayName().compareTo(r2.getDisplayName());
        }
    }
}