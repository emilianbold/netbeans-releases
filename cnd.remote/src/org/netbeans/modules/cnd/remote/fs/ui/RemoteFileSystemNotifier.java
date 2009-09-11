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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
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

    //
    // Static stuff
    //
    private static final Map<ExecutionEnvironment, RemoteFileSystemNotifier> notifiers =
            new HashMap<ExecutionEnvironment, RemoteFileSystemNotifier>();

    public static void show(ExecutionEnvironment execEnv) {
        System.err.printf("111\n");
        RemoteFileSystemNotifier notifier = getNotifier(execEnv);
        notifier.showIfNeed();
    }

    private static synchronized RemoteFileSystemNotifier getNotifier(ExecutionEnvironment execEnv) {
        RemoteFileSystemNotifier notifier = notifiers.get(execEnv);
        if (notifier == null) {
            notifier = new RemoteFileSystemNotifier(execEnv);
            notifiers.put(execEnv, notifier);
        }
        return notifier;
    }

    private static synchronized void removeNotifier(ExecutionEnvironment execEnv) {
        notifiers.remove(execEnv);
    }

    //
    // Instance stuff
    //

    private final ExecutionEnvironment env;
    private boolean shown;
    private Notification notification;

    private RemoteFileSystemNotifier(ExecutionEnvironment execEnv) {
        this.env = execEnv;
        shown = false;
    }

    private void showIfNeed() {
        synchronized(this) {
            if (shown) {
                System.err.printf("222\n");
                return;
            } else {
                shown = true;
                System.err.printf("333\n");
                show();
            }
        }
    }

    private void show() {
        ActionListener onClickAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showConnectDialog();
            }
        };
        notification = NotificationDisplayer.getDefault().notify(
                NbBundle.getMessage(RemoteFileSystemNotifier.class, "RemoteFileSystemNotifier.TITLE"),
                ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/remote/fs/ui/error.png", false), // NOI18N
                NbBundle.getMessage(RemoteFileSystemNotifier.class, "RemoteFileSystemNotifier.DETAILS"),
                onClickAction,
                NotificationDisplayer.Priority.HIGH);

//        RequestProcessor.getDefault().post(new Runnable() {
//            public void run() {
//                notification.clear();
//            }
//        }, 45*1000);
    }

    private void showConnectDialog() {
        final NotifierPanel panel = new NotifierPanel();
        String caption = NbBundle.getMessage(RemoteFileSystemNotifier.class, "RemoteFileSystemNotifier.TITLE");
        DialogDescriptor dd = new DialogDescriptor(panel, caption, true,
                new Object[]{DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION},
                DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, null, null);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.setVisible(true);
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {            
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    connect(panel.getPassword(), panel.isRememberPassword());
                }
            });
        } else {
            reShow();
        }
    }

    private void connect(char[] password, boolean rememberPassword) {
        boolean connected = false;
        try {
            if (password == null || password.length == 0) {
                ConnectionManager.getInstance().connectTo(env);
            } else {
                ConnectionManager.getInstance().connectTo(env, password, rememberPassword);
            }
            connected = true;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (CancellationException ex) {
            // don't log cancellation exception
        }
        if (connected) {
            removeNotifier(env);
        } else {
            reShow();
        }
    }
    
    private void reShow() {
        shown = false;
        show();
        //notification.clear();
    }
}
