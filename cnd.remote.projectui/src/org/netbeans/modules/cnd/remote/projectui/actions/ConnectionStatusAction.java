/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.projectui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionListener;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Alexander Simon
 */
@ActionID(id = "org.netbeans.modules.cnd.remote.projectui.actions.ConnectionStatusAction", category = "Project")
@ActionRegistration(iconInMenu = true, displayName = "#ConnectionStatusAction.submenu.title", lazy = false)
@ActionReference(path = "Toolbars/Remote", position = 450)
public class ConnectionStatusAction  extends AbstractAction implements Presenter.Toolbar, PropertyChangeListener, ConnectionListener {

    private RequestProcessor RP = new RequestProcessor("Connection worker", 1); //NOI18N
    private JButton lastToolbarPresenter;
    private static final Logger logger = Logger.getLogger("remote.toolbar"); //NOI18N

    public ConnectionStatusAction() {
        super(NbBundle.getMessage(ConnectionStatusAction.class, "ConnectionStatusAction.submenu.title"));
        putValue("iconBase","org/netbeans/modules/cnd/remote/projectui/resources/disconnected.png"); //NOI18N
        ServerList.addPropertyChangeListener(WeakListeners.propertyChange(this, this));
        ConnectionManager.getInstance().addConnectionListener(WeakListeners.create(ConnectionListener.class, this, this));
        // initial status
        ExecutionEnvironment executionEnvironment = ServerList.getDefaultRecord().getExecutionEnvironment();
        boolean connectedTo = ConnectionManager.getInstance().isConnectedTo(executionEnvironment);
        updateStatus(executionEnvironment, connectedTo);        
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ExecutionEnvironment executionEnvironment = ServerList.getDefaultRecord().getExecutionEnvironment();
        if (executionEnvironment.isLocal()) {
            return;
        }
        actionPerformed(executionEnvironment, ConnectionManager.getInstance().isConnectedTo(executionEnvironment));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ServerList.PROP_DEFAULT_RECORD.equals(evt.getPropertyName())) {
            ExecutionEnvironment executionEnvironment = ServerList.getDefaultRecord().getExecutionEnvironment();
            boolean connectedTo = ConnectionManager.getInstance().isConnectedTo(executionEnvironment);
            logger.log(Level.FINE, "change default host {0}, connected {1}", new Object[]{executionEnvironment, connectedTo});
            updateStatus(executionEnvironment, connectedTo);
        }
    }
    @Override
    public void connected(ExecutionEnvironment env) {
        ExecutionEnvironment executionEnvironment = ServerList.getDefaultRecord().getExecutionEnvironment();
        if (env.equals(executionEnvironment)) {
            logger.log(Level.FINE, "change state host {0}, connected {1}", new Object[]{executionEnvironment, true});
            updateStatus(executionEnvironment, true);
        }
    }

    @Override
    public void disconnected(ExecutionEnvironment env) {
        ExecutionEnvironment executionEnvironment = ServerList.getDefaultRecord().getExecutionEnvironment();
        if (env.equals(executionEnvironment)) {
            logger.log(Level.FINE, "change state host {0}, connected {1}", new Object[]{executionEnvironment, false});
            updateStatus(executionEnvironment, false);
        }
    }

    private void updateStatus(final ExecutionEnvironment executionEnvironment, final boolean connectedTo) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                setEnabled(!executionEnvironment.isLocal());
                if (!executionEnvironment.isLocal()) {
                    if (connectedTo) {
                        putValue("iconBase", "org/netbeans/modules/cnd/remote/projectui/resources/connected.png"); //NOI18N
                    } else {
                        putValue("iconBase", "org/netbeans/modules/cnd/remote/projectui/resources/disconnected.png"); //NOI18N
                    }
                }
            }
        });
    }

    private void actionPerformed(final ExecutionEnvironment executionEnvironment, final boolean isConnected) {
        RP.post(new Runnable() {

            @Override
            public void run() {
                try {
                    if (!isConnected) {
                        ConnectionManager.getInstance().connectTo(executionEnvironment);
                    } else {
                        ConnectionManager.getInstance().disconnect(executionEnvironment);
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (CancellationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }

    @Override
    public JButton getToolbarPresenter() {
        lastToolbarPresenter = new JButton();
        Actions.connect(lastToolbarPresenter, this);
        return lastToolbarPresenter;
    }
}
