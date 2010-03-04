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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
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

    public static void register(Collection<File> localFiles, ExecutionEnvironment env, File privStorageDir) {
        HostUpdates.get(env, true, privStorageDir).register(localFiles);
    }

    /** test method */
    /*package*/ static List<FileDownloadInfo> testGetUpdates(ExecutionEnvironment env) {
        HostUpdates hu = get(env, false, null);
        if (hu != null) {
            return new ArrayList<FileDownloadInfo>(hu.infos);
        }
        return Collections.<FileDownloadInfo>emptyList();
    }

    private static HostUpdates get(ExecutionEnvironment env, boolean create, File privStorageDir) {
        synchronized (map) {
            HostUpdates updates = map.get(env);
            if (updates == null) {
                updates = new HostUpdates(env, privStorageDir);
                map.put(env, updates);
            }
            return updates;
        }
    }

    private final ExecutionEnvironment env;
    private final RemotePathMap mapper;
    private Notification notification;
    private final HostUpdatesPersistence persistence;

    /*
     * FileDownloadInfo.LOCK guards everything.
     * We don't need that much of concurrency here - just correct behavior
     * (since downloading is much, much longer than all the rest,
     * the only thing to do is to ensure we don't hold lock when downloading)
     */
    private final List<FileDownloadInfo> infos = new ArrayList<FileDownloadInfo>();

    private HostUpdates(ExecutionEnvironment env, File privStorageDir) {
        this.env = env;
        this.mapper = RemotePathMap.getPathMap(env);
        this.persistence = new HostUpdatesPersistence(privStorageDir, env);
    }

    private FileDownloadInfo getFileInfo(File file) {
        synchronized (FileDownloadInfo.LOCK) {
            for (FileDownloadInfo info : infos) {
                if (file.equals(info.getLocalFile())) {
                    return info;
                }
            }
        }
        return null;
    }

    private void register(Collection<File> localFiles) {
        synchronized (FileDownloadInfo.LOCK) {
            for (File file : localFiles) {
                FileDownloadInfo info = getFileInfo(file);
                if (info == null) {
                    infos.add(new FileDownloadInfo(file, mapper.getRemotePath(file.getAbsolutePath(), false), env));
                } else {
                    info.reset();
                }
            }
        }
        showNotification();
    }

    private void showNotification() {
        if (persistence.getRememberChoice()) {
            download();
            return;
        }
        ActionListener onClickAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CndUtils.assertUiThread();
                showConfirmDialog();
            }
        };
        String envString = RemoteUtil.getDisplayName(env);
        try {
            synchronized (FileDownloadInfo.LOCK) {
                if (notification != null) {
                    notification.clear();
                }
                notification = NotificationDisplayer.getDefault().notify(
                        NbBundle.getMessage(getClass(), "RemoteUpdatesNotifier.TITLE", envString),
                        ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/remote/sync/download/remote-updates.png", false), // NOI18N
                        NbBundle.getMessage(getClass(), "RemoteUpdatesNotifier.DETAILS", envString),
                        onClickAction,
                        NotificationDisplayer.Priority.NORMAL);
            }
        } catch (RuntimeException e) {
            // FIXUP: for some reasons exceptions aren't printed
            e.printStackTrace();
            throw e;
        }
    }

    private Collection<FileDownloadInfo> getByState(FileDownloadInfo.State state) {
        Collection<FileDownloadInfo> result = new ArrayList<FileDownloadInfo>();
        synchronized (FileDownloadInfo.LOCK) {
            for (FileDownloadInfo info : infos) {
                if (info.getState() == state) {
                    result.add(info);
                }
            }
        }
        return result;
    }

    private void showConfirmDialog() {
        Collection<FileDownloadInfo> unconfirmed;
        synchronized (FileDownloadInfo.LOCK) {
            unconfirmed = getByState(FileDownloadInfo.State.UNCONFIRMED);
            if (unconfirmed.isEmpty()) {
                return;
            }
            if (notification != null) {
                notification.clear();
                notification = null;
            }
        }
        final Set<FileDownloadInfo> confirmed = HostUpdatesRequestPanel.request(unconfirmed, env, persistence);
        if (confirmed == null) { // null means user pressed cancel - re-show notification
            synchronized (FileDownloadInfo.LOCK) {
                if (notification == null) {
                    showNotification();
                }
            }
        } else {
            synchronized (FileDownloadInfo.LOCK) {
                for (FileDownloadInfo info : unconfirmed) {
                    if (confirmed.contains(info)) {
                        info.confirm();
                    } else {
                        info.reject();
                    }
                }
                unconfirmed.removeAll(confirmed);
                infos.removeAll(unconfirmed);
            }
            if (!confirmed.isEmpty()) {
                NamedRunnable r = new NamedRunnable("Remote updates synchronizer for " + env.getDisplayName()) { //NOI18N
                    @Override
                    protected void runImpl() {
                        download(confirmed);
                    }
                };
                RequestProcessor.getDefault().post(r);
            }
        }
    }
    
    /** */
    private void download() {
        final Collection<FileDownloadInfo> confirmed = new ArrayList<FileDownloadInfo>();
        synchronized (FileDownloadInfo.LOCK) {
            Collection<FileDownloadInfo> unconfirmed = getByState(FileDownloadInfo.State.UNCONFIRMED);
            for (FileDownloadInfo info : unconfirmed) {
                if (persistence.getFileSelected(info.getLocalFile(), true)) {
                    info.confirm();
                    confirmed.add(info);
                } else {
                    info.reject();
                }
            }
            unconfirmed.removeAll(confirmed);
            infos.removeAll(unconfirmed);
        }
        if (!confirmed.isEmpty()) {
            RequestProcessor.getDefault().post(new NamedRunnable("") {
                @Override
                protected void runImpl() {
                    download(confirmed);
                }
            });
        }
    }

    private void download(Collection<FileDownloadInfo> infos) {
        ProgressHandle handle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(getClass(), "RemoteUpdatesProgress_Title", RemoteUtil.getDisplayName(env)));
        handle.start();
        handle.switchToDeterminate(infos.size());
        int cnt = 0;
        for (FileDownloadInfo info : infos) {
            handle.progress(NbBundle.getMessage(getClass(), "RemoteUpdatesProgress_Message", info.getLocalFile().getName()), cnt++);
            info.download();
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
        for (FileDownloadInfo info : infos) {
            if (info.getState() != FileDownloadInfo.State.DONE) {
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
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                Notification n = notRef.get();
                if (n != null) {
                    n.clear();
                }
            }
        }, 15*1000);
    }
}
