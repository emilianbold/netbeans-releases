/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsCacheManager;
import org.netbeans.modules.cnd.remote.server.RemoteServerRecord;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.cnd.remote.ui.HostPropertiesDialog;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionListener;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.windows.WindowManager;

/**
 *
 * @author Vladimir Kvashin
 */
public final class HostNode extends AbstractNode implements ConnectionListener, PropertyChangeListener {

    private final ExecutionEnvironment env;

    public HostNode(ExecutionEnvironment execEnv) {
        super(createChildren(execEnv), Lookups.singleton(execEnv));
        this.env = execEnv;
        ConnectionManager.getInstance().addConnectionListener(WeakListeners.create(ConnectionListener.class, this, null));
        ServerList.addPropertyChangeListener(WeakListeners.propertyChange(this, null));
    }

    private static Children createChildren(ExecutionEnvironment execEnv) {
        final Collection<? extends HostNodesProvider> providers = Lookup.getDefault().lookupAll(HostNodesProvider.class);
        if (providers.isEmpty()) {
            return Children.LEAF;
        } else {
            return Children.create(new HostSubnodeChildren(execEnv, providers), true);
        }
    }

    @Override
    public void connected(ExecutionEnvironment env) {
        fireIconChange();
    }

    @Override
    public void disconnected(ExecutionEnvironment env) {
        fireIconChange();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(ServerList.PROP_DEFAULT_RECORD)) {
            refresh();
        }
    }

    private void refresh() {
        fireDisplayNameChange("", getDisplayName()); // to make Node refresh
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public Image getIcon(int type) {
        Image main = ImageUtilities.loadImage("org/netbeans/modules/remote/ui/single_server.png"); // NOI18N
        Image connection = ImageUtilities.loadImage("org/netbeans/modules/remote/ui/" + //NOI18N
                (isConnected() ? "connected.png" : "disconnected.png")); //NOI18N
        Image merged = ImageUtilities.mergeImages(main, connection, 0, 8);
        return merged;
    }

    private boolean isConnected() {
        return ConnectionManager.getInstance().isConnectedTo(env);
    }

    @Override
    public String getHtmlDisplayName() {
        String displayName = RemoteUtil.getDisplayName(env);
        ServerRecord defRec = ServerList.getDefaultRecord();
        if (defRec != null && defRec.getExecutionEnvironment().equals(env)) {
            displayName = "<b>" + displayName + "<\b>"; // NOI18N
        }
        return displayName;
    }


    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            new PropertiesAction(),
            new SetDefaultAction(),
            new ConnectAction(env),
            new DisconnectAction(),
            new RemoveHostAction()            
        };
    }

    @Override
    public Action getPreferredAction() {
        return new PropertiesAction();
    }


    public ExecutionEnvironment getExecutionEnvironment() {
        return getLookup().lookup(ExecutionEnvironment.class);
    }

    private static class HostSubnodeChildren extends ChildFactory<HostNodesProvider> {

        private final Collection<? extends HostNodesProvider> providers;
        private final ExecutionEnvironment execEnv;

        public HostSubnodeChildren(ExecutionEnvironment execEnv, Collection<? extends HostNodesProvider> providers) {
            this.execEnv = execEnv;
            this.providers = providers;
        }

        @Override
        protected boolean createKeys(List<HostNodesProvider> toPopulate) {
            toPopulate.addAll(providers);
            return true;
        }

        @Override
        protected Node createNodeForKey(HostNodesProvider key) {
            return key.createNode(execEnv);
        }
    }

    private class RemoveHostAction extends AbstractAction { // extends HostAction

        public RemoveHostAction() {
            super(NbBundle.getMessage(HostListRootNode.class, "RemoveHostMenuItem"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ServerRecord record = ServerList.get(getExecutionEnvironment());
            String title = NbBundle.getMessage(HostNode.class, "RemoveHostCaption");
            String message = NbBundle.getMessage(HostNode.class, "RemoveHostQuestion", record.getDisplayName());

            if (JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                    message, title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                ToolsCacheManager cacheManager = ToolsCacheManager.createInstance(true);
                List<ServerRecord> hosts = new ArrayList<ServerRecord>(ServerList.getRecords());
                hosts.remove(record);
                cacheManager.setHosts(hosts);
                ServerRecord defaultRecord = ServerList.getDefaultRecord();
                if (defaultRecord.getExecutionEnvironment().equals(getExecutionEnvironment())) {
                    defaultRecord = ServerList.get(ExecutionEnvironmentFactory.getLocal());
                }
                cacheManager.setDefaultRecord(defaultRecord);
                cacheManager.applyChanges();
            }
        }
    }

    private class SetDefaultAction extends AbstractAction { // extends HostAction

        public SetDefaultAction() {
            super(NbBundle.getMessage(HostListRootNode.class, "SetDefaultMenuItem"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ServerList.setDefaultRecord(ServerList.get(getExecutionEnvironment()));
        }
    }

    private class PropertiesAction extends AbstractAction { // extends HostAction

        public PropertiesAction() {
            super(NbBundle.getMessage(HostListRootNode.class, "PropertirsMenuItem"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RemoteServerRecord record = (RemoteServerRecord) ServerList.get(env);
            HostPropertiesDialog.invokeMe(record);
            refresh(); // TODO: introduce listeners for server records
        }
    }

    private class DisconnectAction extends AbstractAction implements Runnable {

        public DisconnectAction() {
            super(NbBundle.getMessage(HostListRootNode.class, "DisconnectMenuItem"));
            setEnabled(ConnectionManager.getInstance().isConnectedTo(env));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RequestProcessor.getDefault().post(this);
        }

        public void run() {
            if (ConnectionManager.getInstance().isConnectedTo(env)) {
                ConnectionManager.getInstance().disconnect(env);
            }
        }
    }
}
