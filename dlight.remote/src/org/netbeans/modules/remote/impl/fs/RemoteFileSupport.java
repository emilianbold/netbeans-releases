/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.impl.fs;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.api.ui.ConnectionNotifier;
import org.netbeans.modules.remote.support.RemoteLogger;
import org.openide.util.NbBundle;

/**
 * Responsible for copying files from remote host.
 * Each instance of the RemoteFileSupport class corresponds to a remote server
 * 
 * @author Vladimir Kvashin
 */
public class RemoteFileSupport extends ConnectionNotifier.NamedRunnable {

    private final PendingFilesQueue pendingFilesQueue = new PendingFilesQueue();
    private final ExecutionEnvironment execEnv;

    public RemoteFileSupport(ExecutionEnvironment execEnv) {
        super(NbBundle.getMessage(RemoteFileSupport.class, "RemoteDownloadTask.TITLE", getDisplayName(execEnv)));
        this.execEnv = execEnv;
    }

    @Override
    protected void runImpl() {
        try {
            onConnect();
        } catch (ConnectException ex) {
            RemoteLogger.getInstance().log(Level.INFO, NbBundle.getMessage(getClass(), "RemoteFileSystemNotifier.ERROR", execEnv), ex);
            ConnectionNotifier.addTask(execEnv, this);
        } catch (InterruptedException ex) {
            // don't report interruption
        } catch (InterruptedIOException ex) {
            // don't report interruption
        } catch (IOException ex) {
            RemoteLogger.getInstance().log(Level.INFO, NbBundle.getMessage(getClass(), "RemoteFileSystemNotifier.ERROR", execEnv), ex);
            ConnectionNotifier.addTask(execEnv, this);
        } catch (ExecutionException ex) {
            RemoteLogger.getInstance().log(Level.INFO, NbBundle.getMessage(getClass(), "RemoteFileSystemNotifier.ERROR", execEnv), ex);
            ConnectionNotifier.addTask(execEnv, this);
        }
    }

    private static String getDisplayName(ExecutionEnvironment env) {
        return env.getDisplayName(); // RemoteUtil.getDisplayName(env);
    }

    // NB: it is always called in a specially created thread
    private void onConnect() throws InterruptedException, ConnectException, InterruptedIOException, IOException, ExecutionException {
        ProgressHandle handle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(getClass(), "Progress_Title", getDisplayName(execEnv)));
        handle.start();
        handle.switchToDeterminate(pendingFilesQueue.size());
        int cnt = 0;
        try {
            RemoteFileSystem fs = RemoteFileSystemManager.getInstance().getFileSystem(execEnv);
            PendingFile pendingFile;
            // die after half a minute inactivity period
            while ((pendingFile = pendingFilesQueue.poll(1, TimeUnit.SECONDS)) != null) {
                RemoteFileObjectBase dir = fs.findResource(pendingFile.remotePath);
                if (dir != null) {
                    dir.ensureSync();
                } else {
                    RemoteLogger.getInstance().log(Level.INFO, "Directory {0}:{1} does not exist (was it removed?)", new Object[]{execEnv, pendingFile.remotePath});
                }
                handle.progress(NbBundle.getMessage(getClass(), "Progress_Message", pendingFile.remotePath), cnt++); // NOI18N
            }
        } finally {
            handle.finish();
            RemoteFileSystemManager.getInstance().fireDownloadListeners(execEnv);
        }
    }

    public void addPendingFile(RemoteFileObjectBase fo) {
        RemoteLogger.getInstance().log(Level.FINEST, "Adding notification for {0}:{1}", new Object[]{execEnv, fo.remotePath}); //NOI18N
        //pendingFilesQueue.add(fo.remotePath);
        ConnectionNotifier.addTask(execEnv, this);
    }

    private static class PendingFile {
        public final String remotePath;
        public PendingFile(String remotePath) {
            this.remotePath = remotePath;
        }
    }

    /**
     * NB: the class is not optimized, for in fact it's used from CndFileUtils,
     * which perform all necessary caching.
     */
    private static class PendingFilesQueue {

        private final BlockingQueue<PendingFile> queue = new LinkedBlockingQueue<PendingFile>();
        private final Set<String> remoteAbsPaths = new TreeSet<String>();

        public synchronized void add(String remotePath) {
            if (remoteAbsPaths.add(remotePath)) {
                queue.add(new PendingFile(remotePath));
            }
        }

        public synchronized PendingFile take() throws InterruptedException {
            PendingFile pendingFile = queue.take();
            remoteAbsPaths.remove(pendingFile.remotePath);
            return pendingFile;
        }

        public synchronized PendingFile poll(long timeout, TimeUnit unit) throws InterruptedException {
            PendingFile pendingFile = queue.poll(timeout, unit);
            if (pendingFile != null) {
                remoteAbsPaths.remove(pendingFile.remotePath);
            }
            return pendingFile;
        }
        
        public synchronized List<String> getPendingFiles() {
            return Collections.unmodifiableList(new ArrayList<String>(remoteAbsPaths));
        }

        public int size() {
            return remoteAbsPaths.size();
        }
    }
}
