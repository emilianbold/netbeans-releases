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

package org.netbeans.modules.cnd.remote.sync.download;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JOptionPane;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Vladimir Kvashin
 */
public class HostUpdates {

    //
    //  Static stuff
    //

    private static final Map<ExecutionEnvironment, HostUpdates> map = new HashMap<ExecutionEnvironment, HostUpdates>();

    public static void register(Collection<File> localFiles, ExecutionEnvironment env) {
        HostUpdates.get(env).register(localFiles);
    }

    private static HostUpdates get(ExecutionEnvironment env) {
        synchronized (map) {
            HostUpdates updates = map.get(env);
            if (updates == null) {
                updates = new HostUpdates(env);
                map.put(env, updates);
            }
            return updates;
        }
    }

    //
    //  Instance stuff
    //

    private static enum FileState {
        UNCONFIRMED,
        PENDING,
        COPYING,
        CANCELLED,
        DONE,
        ERROR}

    // UNCONFIRMED ---> PENDING ---------> COPYING ---> DONE
    //       |           |                   |  |
    //       +---------->+--->CANCELLED <----+  +-----> ERROR

    private class FileInfo {

        public final File file;
        public FileState state;
        public Future<Integer> copyTask;

        public FileInfo(File file) {
            this.file = file;
            this.state = FileState.UNCONFIRMED;
        }

        public void copy() {
            String remoteFile = mapper.getRemotePath(file.getAbsolutePath(), false);
            synchronized (lock) {
                state = FileState.COPYING;
                if (copyTask != null) {
                    copyTask.cancel(true);
                }
                copyTask = CommonTasksSupport.downloadFile(remoteFile, env, file.getAbsolutePath(), null);
            }
            try {
                int rc = copyTask.get().intValue();
                synchronized (lock) {
                    state = (rc == 0) ? FileState.DONE : FileState.ERROR;
                }
            } catch (InterruptedException ex) {
                synchronized (lock) {
                    state = FileState.CANCELLED;
                }
            } catch (ExecutionException ex) {
                synchronized (lock) {
                    state = FileState.ERROR;
                }
            } finally {
                synchronized (lock) {
                    copyTask = null;
                }
            }
        }

    }

    private final ExecutionEnvironment env;
    private final List<FileInfo> infos = new ArrayList<FileInfo>();
    private final RemotePathMap mapper;
    private Notification notification;

    /**
     * Guards everything.
     * We don't need that much of concurrency here
     * (since downloading is much, much longer than all the rest), 
     * just correct behavior 
     */
    private final Object lock = new Object();

    private HostUpdates(ExecutionEnvironment env) {
        this.env = env;
        this.mapper = RemotePathMap.getPathMap(env);
    }

    private FileInfo getFileInfo(File file) {
        synchronized (lock) {
            for (FileInfo info : infos) {
                if (file.equals(info.file)) {
                    return info;
                }
            }
        }
        return null;
    }

    private void register(Collection<File> localFiles) {
        synchronized (lock) {
            for (File file : localFiles) {
                FileInfo info = getFileInfo(file);
                if (info == null) {
                    infos.add(new FileInfo(file));
                } else {
                    Future<Integer> copyTask = info.copyTask;
                    if (copyTask != null) {
                        copyTask.cancel(true);
                    }
                    info.state = FileState.UNCONFIRMED;
                }
            }
        }
        showRemoteUpdatesNotification();
    }

    private void showRemoteUpdatesNotification() {
        ActionListener onClickAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CndUtils.assertUiThread();
                showConfirmDialog();
            }
        };
        String envString = RemoteUtil.getDisplayName(env);
        try {
            notification = NotificationDisplayer.getDefault().notify(
                    NbBundle.getMessage(getClass(), "RemoteUpdatesNotifier.TITLE", envString),
                    ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/remote/sync/download/remote-updates.png", false), // NOI18N
                    NbBundle.getMessage(getClass(), "RemoteUpdatesNotifier.DETAILS", envString),
                    onClickAction,
                    NotificationDisplayer.Priority.NORMAL);
        } catch (RuntimeException e) {
            // FIXUP: for some reasons exceptions aren't printed
            e.printStackTrace();
            throw e;
        }
    }

    private Collection<FileInfo> getByState(FileState state) {
        Collection<FileInfo> result = new ArrayList<FileInfo>();
        synchronized (lock) {
            for (FileInfo info : infos) {
                if (info.state == state) {
                    result.add(info);
                }
            }
        }
        return result;
    }

    private void showConfirmDialog() {
        final Collection<FileInfo> unconfirmed = getByState(FileState.UNCONFIRMED);
        if (JOptionPane.showConfirmDialog(null, "Download?", "Download", // NOI18N
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            synchronized (lock) {
                for (FileInfo info : unconfirmed) {
                    info.state = FileState.PENDING;
                }
                NamedRunnable r = new NamedRunnable("Remote updates synchronizer for " + env.getDisplayName()) { //NOI18N
                    @Override
                    protected void runImpl() {
                        download(unconfirmed);
                    }
                };
                RequestProcessor.getDefault().post(r);
            }
        } else {
            synchronized (lock) {
                for (FileInfo info : unconfirmed) {
                    info.state = FileState.CANCELLED;
                }
                infos.removeAll(unconfirmed);
            }
        }
        notification.clear();
    }

    private void download(Collection<FileInfo> infos) {
        ProgressHandle handle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(getClass(), "RemoteUpdatesProgress_Title", RemoteUtil.getDisplayName(env)));
        handle.start();
        handle.switchToDeterminate(infos.size());
        int cnt = 0;
        for (FileInfo info : infos) {
            handle.progress(NbBundle.getMessage(getClass(), "RemoteUpdatesProgress_Message", info.file.getName()), cnt++);
            info.copy();
        }
        handle.finish();

        final AtomicReference<Notification> notRef = new AtomicReference<Notification>();

        ActionListener onClickAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Notification n = notRef.get();
                if (n != null) {
                    n.clear();
                }
            }
        };

        String envString = RemoteUtil.getDisplayName(env);
        String iconString = "org/netbeans/modules/cnd/remote/sync/download/check.png"; //NOI18N
        String title = NbBundle.getMessage(getClass(), "RemoteUpdatesOK.TITLE", envString, infos.size());
        String detailsText = NbBundle.getMessage(getClass(), "RemoteUpdatesOK.DETAILS", envString, infos.size());
        for (FileInfo info : infos) {
            if (info.state != FileState.DONE) {
                iconString = "org/netbeans/modules/cnd/remote/sync/download/error.png"; //NOI18N
                title = NbBundle.getMessage(getClass(), "RemoteUpdatesERR.TITLE", envString, infos.size());
                detailsText = NbBundle.getMessage(getClass(), "RemoteUpdatesERR.DETAILS", envString, infos.size());
                break;
            }
        }
        notRef.set(NotificationDisplayer.getDefault().notify(
                title,
                ImageUtilities.loadImageIcon(iconString, false), // NOI18N
                detailsText,
                onClickAction,
                NotificationDisplayer.Priority.LOW));
    }
}
