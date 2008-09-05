/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.mysql.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.mysql.DatabaseServer;
import org.netbeans.modules.db.mysql.ui.PropertiesDialog;
import org.netbeans.modules.db.mysql.util.Utils;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author David
 */
public class ReconnectPropertyChangeListener implements PropertyChangeListener {
    private static final Logger LOGGER = Logger.getLogger(ReconnectPropertyChangeListener.class.getName());
    private final DatabaseServer server;
    private boolean reconnecting = false;

    public ReconnectPropertyChangeListener(DatabaseServer server) {
        this.server = server;
    }

    private synchronized boolean isReconnecting() {
        return reconnecting;
    }

    private synchronized void setReconnecting(boolean reconnecting) {
        this.reconnecting = reconnecting;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (isReconnecting()) {
            // Don't reconnect multiple times for multiple property changes
            return;
        }

        if (server.propertyChangeNeedsReconnect(evt)) {
            setReconnecting(true);

            // Show a status bar because sometimes the progress bar is so fast that
            // people don't know that a reconnect just happened.
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(ReconnectPropertyChangeListener.class, "MSG_ReconnectingToMySQL"));

            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        server.reconnect();
                        setReconnecting(false);
                    } catch (DatabaseException dbe) {
                        LOGGER.log(Level.INFO, dbe.getMessage(), dbe);
                        setReconnecting(false);

                        boolean displayProperties = Utils.displayYesNoDialog(
                                NbBundle.getMessage(ReconnectPropertyChangeListener.class, "MSG_ReconnectFailed", dbe.getMessage()));

                        if (displayProperties) {
                            Mutex.EVENT.postReadRequest(new Runnable() {
                                public void run() {
                                    PropertiesDialog dialog = new PropertiesDialog(server);
                                    dialog.displayDialog();
                                }
                            });
                        }
                    }
                }

            });

        }
    }

}
