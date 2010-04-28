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
package org.netbeans.modules.cnd.remote.fs.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.ImageIcon;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionListener;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.PasswordManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Vladimir Kvashin
 */
public class RemoteFileSystemNotifier {

    public interface Callback {

        /**
         * Is called as soon as the host has been connected.
         * It is always called in a specially created thread
         */
        void connected() throws InterruptedException, ConnectException, InterruptedIOException, IOException, ExecutionException;

        List<String> getPendingFiles();
    }

    private class Listener implements ConnectionListener {

        @Override
        public void connected(ExecutionEnvironment env) {
            if (RemoteFileSystemNotifier.this.env.equals(env)) {
                ConnectionManager.getInstance().removeConnectionListener(this);
                RequestProcessor.getDefault().post(new NamedRunnable("Pending files synchronizer for " + env.getDisplayName()) { //NOI18N
                    @Override
                    protected void runImpl() {
                        RemoteFileSystemNotifier.this.connected();
                    }
                });
            }
        }

        @Override
        public void disconnected(ExecutionEnvironment env) {
        }
    }
    private final ExecutionEnvironment env;
    private final Callback callback;
    private boolean shown;
    private Notification notification;

    public RemoteFileSystemNotifier(ExecutionEnvironment execEnv, Callback callback) {
        this.env = execEnv;
        this.callback = callback;
        shown = false;
    }

    private void connected() {
        notification.clear();
        try {
            callback.connected();
        } catch (ConnectException ex) {
            reShow(ex);
        } catch (InterruptedException ex) {
            // don't report interruption
        } catch (InterruptedIOException ex) {
            // don't report interruption
        } catch (IOException ex) {
            reShow(ex);
        } catch (ExecutionException ex) {
            reShow(ex);
        }
    }

    public void showIfNeed() {
        synchronized (this) {
            if (shown) {
                return;
            } else {
                shown = true;
                ConnectionManager cm = ConnectionManager.getInstance();
                ConnectionListener listener = new Listener();
                cm.addConnectionListener(listener);
                show(null);
            }
        }
    }

    private void show(Exception error) {
        ActionListener onClickAction = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (PasswordManager.getInstance().isRememberPassword(env)) {
                    RequestProcessor.getDefault().post(new NamedRunnable("Requesting connection for " + env.getDisplayName()) { //NOI18N

                        @Override
                        protected void runImpl() {
                            connect();
                        }
                    });
                    return;
                }
                showConnectDialog();
            }
        };
        String envString = RemoteUtil.getDisplayName(env);

        String title, details;
        ImageIcon icon;

        if (error == null) {
            title = NbBundle.getMessage(RemoteFileSystemNotifier.class, "RemoteFileSystemNotifier.TITLE", envString);
            icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/remote/fs/ui/exclamation.gif", false); // NOI18N
            details = NbBundle.getMessage(RemoteFileSystemNotifier.class, "RemoteFileSystemNotifier.DETAILS", envString);
        } else {
            title = NbBundle.getMessage(getClass(), "RemoteFileSystemNotifier.error.TITLE", envString);
            icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/remote/fs/ui/error.png", false); // NOI18N
            String errMsg = (error.getMessage() == null) ? "" : error.getMessage();
            details = NbBundle.getMessage(getClass(), "RemoteFileSystemNotifier.error.DETAILS", envString, errMsg, envString);
        }
        notification = NotificationDisplayer.getDefault().notify(title, icon, details, onClickAction, NotificationDisplayer.Priority.HIGH);
    }

    private void showConnectDialog() {
        final NotifierPanel panel = new NotifierPanel(env);
        panel.setPendingFiles(callback.getPendingFiles());
        String envString = RemoteUtil.getDisplayName(env);
        String caption = NbBundle.getMessage(RemoteFileSystemNotifier.class, "RemoteFileSystemNotifier.TITLE", envString);
        DialogDescriptor dd = new DialogDescriptor(panel, caption, true,
                new Object[]{DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION},
                DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, null, null);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.setVisible(true);
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            RequestProcessor.getDefault().post(new NamedRunnable("Requesting connection for " + env.getDisplayName()) { //NOI18N

                @Override
                protected void runImpl() {
//                    PasswordManager.getInstance().storePassword(env, panel.getPassword(), panel.isRememberPassword());
                    connect();
                }
            });
        } else {
            reShow(null);
        }
    }

    private void connect() {
        try {
            ConnectionManager.getInstance().connectTo(env);
            // callback.connected(); // we now use listener instead
        } catch (IOException ex) {
            ex.printStackTrace();
            reShow(ex);
        } catch (CancellationException ex) {
            // don't log cancellation exception
            reShow(null);
        }
    }

    private void reShow(Exception error) {
        synchronized (this) {
            shown = false;
        }
        show(error);
        //notification.clear();
    }
}
