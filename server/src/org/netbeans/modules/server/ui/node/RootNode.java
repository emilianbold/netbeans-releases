/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.server.ui.node;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.server.ServerRegistry;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

public final class RootNode extends AbstractNode {

    private static final RequestProcessor REFRESH_PROCESSOR =
            new RequestProcessor("Server registry update/refresh", 5);

    private static final String SERVERS_ICON = "org/netbeans/modules/server/ui/resources/servers.png"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(RootNode.class.getName());

    private static RootNode node;

    private RootNode(ChildFactory factory) {
        super(Children.create(factory, true));

        setName(""); // NOI18N
        setDisplayName(NbBundle.getMessage(RootNode.class, "Server_Registry_Node_Name"));
        setShortDescription(NbBundle.getMessage(RootNode.class, "Server_Registry_Node_Short_Description"));
        setIconBaseWithExtension(SERVERS_ICON);
    }

    @ServicesTabNodeRegistration(
        name = "servers",
        displayName = "org.netbeans.modules.server.ui.node.Bundle#Server_Registry_Node_Name",
        shortDescription = "org.netbeans.modules.server.ui.node.Bundle#Server_Registry_Node_Short_Description",
        iconResource = "org/netbeans/modules/server/ui/resources/servers.png",
        position = 400
    )
    public static synchronized RootNode getInstance() {
        if (node == null) {
            ChildFactory factory = new ChildFactory();
            factory.init();

            node = new RootNode(factory);
        }
        return node;
    }

    @Override
    public Action[] getActions(boolean context) {
        Action[] arr = Utilities.actionsForPath("Servers/Actions").toArray(new Action[0]); // NOI18N
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == null) {
                continue;
            }
            if (Boolean.TRUE.equals(arr[i].getValue("serverNodeHidden"))) { // NOI18N
                arr[i] = null;
            }
        }
        return arr;
    }


    static void enableActionsOnExpand() {
        FileObject fo = FileUtil.getConfigFile("Servers/Actions"); // NOI18N
        Enumeration<String> en;
        if (fo != null) {
            for (FileObject o : fo.getChildren()) {
                en = o.getAttributes();
                while (en.hasMoreElements()) {
                    String attr = en.nextElement();
                    boolean enable = false;
                    final String prefix = "property-"; // NOI18N
                    if (attr.startsWith(prefix)) {
                        attr = attr.substring(prefix.length());
                        if (System.getProperty(attr) != null) {
                            enable = true;
                        }
                    } else {
                        final String config = "config-"; // NOI18N
                        if (attr.startsWith(config)) {
                            attr = attr.substring(config.length());
                            if (FileUtil.getConfigFile(attr) != null) {
                                enable = true;
                            }
                        }
                    }

                    if (enable) {
                        Lookup l = Lookups.forPath("Servers/Actions"); // NOI18N
                        for (Lookup.Item<Action> item : l.lookupResult(Action.class).allItems()) {
                            if (item.getId().contains(o.getName())) {
                                Action a = item.getInstance();
                                a.actionPerformed(new ActionEvent(getInstance(), 0, "noui")); // NOI18N
                            }
                        }
                    }
                }
            }
        }
    }

    private static class ChildFactory extends org.openide.nodes.ChildFactory<ServerInstance> 
    implements ChangeListener, Runnable {

        private static final Comparator<ServerInstance> COMPARATOR = new InstanceComparator();

        /** <i>GuardedBy("this")</i> */
        private final List<ServerInstanceProvider> types = new ArrayList<ServerInstanceProvider>();

        public ChildFactory() {
            super();
        }

        public void init() {
            REFRESH_PROCESSOR.post(new Runnable() {

                public void run() {
                    synchronized (ChildFactory.this) {
                        final ServerRegistry registry = ServerRegistry.getInstance();

                        registry.addChangeListener(
                            WeakListeners.create(ChangeListener.class, ChildFactory.this, registry));
                        updateState(new ChangeEvent(registry));
                    }
                }
            });
        }

        public void stateChanged(final ChangeEvent e) {
            REFRESH_PROCESSOR.post(new Runnable() {

                public void run() {
                    updateState(e);
                }
            });
        }

        private synchronized void updateState(final ChangeEvent e) {
            if (e.getSource() instanceof ServerRegistry) {
                for (ServerInstanceProvider type : types) {
                    type.removeChangeListener(ChildFactory.this);
                }

                types.clear();
                types.addAll(((ServerRegistry) e.getSource()).getProviders());
                for (ServerInstanceProvider type : types) {
                    type.addChangeListener(ChildFactory.this);
                }
            }
            refresh();
        }

        protected final void refresh() {
            refresh(false);
        }

        @Override
        protected Node createNodeForKey(ServerInstance key) {
            return key.getFullNode();
        }

        @Override
        protected boolean createKeys(List<ServerInstance> toPopulate) {
            List<ServerInstance> fresh = new ArrayList<ServerInstance>();

            Mutex.EVENT.readAccess(this);

            ServerRegistry registry = ServerRegistry.getInstance();
            for (ServerInstanceProvider type : registry.getProviders()) {
                fresh.addAll(type.getInstances());
            }

            Collections.sort(fresh, COMPARATOR);

            toPopulate.addAll(fresh);
            return true;
        }

        private static boolean actionsPropertiesDone;
        public void run() {
            if (actionsPropertiesDone) {
                return;
            }
            assert EventQueue.isDispatchThread();
            actionsPropertiesDone = true;
            enableActionsOnExpand();
            ServerRegistry.getInstance().getProviders();
        }
    } // end of ChildFactory

    private static class InstanceComparator implements Comparator<ServerInstance>, Serializable {

        public int compare(ServerInstance o1, ServerInstance o2) {
            boolean firstNull = false;
            boolean secondNull = false;

            if (o1.getDisplayName() == null) {
                LOGGER.log(Level.INFO, "Instance display name is null for {0}", o1);
                firstNull = true;
            }
            if (o2.getDisplayName() == null) {
                LOGGER.log(Level.INFO, "Instance display name is null for {0}", o2);
                secondNull = true;
            }

            if (firstNull && secondNull) {
                return 0;
            } else if (firstNull && !secondNull) {
                return -1;
            } else if (!firstNull && secondNull) {
                return 1;
            }

            return o1.getDisplayName().compareTo(o2.getDisplayName());
        }

    }
}
