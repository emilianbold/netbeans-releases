/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.server;

import java.awt.Dialog;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerListUI;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.ui.options.ServerListUIEx;
import org.netbeans.modules.cnd.ui.options.ToolsCacheManager;
import org.netbeans.modules.cnd.remote.ui.EditServerListDialog;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * ServerListDisplayer implementation
 * @author Vladimir Kvashin
 */
@ServiceProvider(service = ServerListUI.class)
public class RemoteServerListUI extends ServerListUIEx {

    @Override
    protected boolean showServerListDialogImpl() {
        ToolsCacheManager cacheManager = new ToolsCacheManager();
        if (showServerListDialog(cacheManager)) {
            cacheManager.applyChanges();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean showServerListDialogImpl(ToolsCacheManager cacheManager) {
        EditServerListDialog dlg = new EditServerListDialog(cacheManager);
        DialogDescriptor dd = new DialogDescriptor(dlg, NbBundle.getMessage(getClass(), "TITLE_EditServerList"), true,
                    DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, null);
        dlg.setDialogDescriptor(dd);
        dd.addPropertyChangeListener(dlg);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            cacheManager.setHosts(dlg.getHosts());
            cacheManager.setDefaultRecord(dlg.getDefaultRecord());
            return true;
        } else {
            return false;
        }
    }

    private boolean showConfirmDialog(final String message, final String title) {
        final AtomicBoolean res = new AtomicBoolean(false);
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    int option = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), 
                            message, title, JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        res.set(true);                                
                    }
                }
            });
        } catch (InterruptedException ex) {
        } catch (InvocationTargetException ex) {
        }
        return res.get();
    }

    @Override
    protected boolean ensureRecordOnlineImpl(ExecutionEnvironment env, String customMessage) {
        CndUtils.assertNonUiThread();
        if (env.isLocal()) {
            return true;
        }
        ServerRecord record = ServerList.get(env);
        boolean result = false;
        if (record.isDeleted()) {
            final String message = MessageFormat.format(
                    NbBundle.getMessage(getClass(), "ERR_RequestingDeletedConnection"),
                    record.getDisplayName());
            boolean res = showConfirmDialog(message, NbBundle.getMessage(getClass(), "DLG_TITLE_DeletedConnection"));
            if (res) {
                ServerList.addServer(record.getExecutionEnvironment(), record.getDisplayName(), record.getSyncFactory(), false, true);
                result = true;
            }
        } else if (record.isOnline()) {
            result = true;
        } else { //  !record.isOnline()
            final String message = MessageFormat.format(
                    NbBundle.getMessage(getClass(), "ERR_NeedToConnectToRemoteHost"),
                    record.getDisplayName());
            boolean res = showConfirmDialog(message, NbBundle.getMessage(getClass(), "DLG_TITLE_Connect"));
            if (res) {
                try {
                    if (ConnectionManager.getInstance().isConnectedTo(record.getExecutionEnvironment())) {
                        ConnectionManager.getInstance().connectTo(record.getExecutionEnvironment());
                    }
                    record.validate(true);
                    result = true;
                } catch (CancellationException ex) {
                    // don't log
                } catch (InterruptedIOException ex) {
                    // don't log
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return result;
    }

    @Override
    protected boolean ensureRecordOnlineImpl(ExecutionEnvironment env) {
        return ensureRecordOnlineImpl(env, null);
    }
}
