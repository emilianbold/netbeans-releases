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

package org.netbeans.modules.cnd.remote.fs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.remote.fs.ui.RemoteFileSystemNotifier;
import org.netbeans.modules.cnd.remote.server.RemoteServerListUI;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.util.NbBundle;

/**
 * Reponsible for copying files from remote host
 * @author Vladimir Kvashin
 */
public class RemoteFileSupport {

    private final ExecutionEnvironment execEnv;

    /** File transfer statistics */
    private int fileCopyCount;

    /** Directory synchronization statistics */
    private int dirSyncCount;

    private final Object mainLock = new Object();
    private Map<File, Object> locks = new HashMap<File, Object>();

    private final Map<ExecutionEnvironment, Boolean> cancels = new HashMap<ExecutionEnvironment, Boolean>();

    public static final String FLAG_FILE_NAME = ".rfs"; // NOI18N

    public RemoteFileSupport(ExecutionEnvironment execEnv) {
        this.execEnv = execEnv;
        resetStatistic();
    }


    private Object getLock(File file) {
        synchronized(mainLock) {
            Object lock = locks.get(file);
            if (lock == null) {
                lock = new Object();
                locks.put(file, lock);
            }
            return lock;
        }
    }

    private void removeLock(File file) {
        synchronized(mainLock) {
            locks.remove(file);
        }
    }


    public void ensureFileSync(File file, String remotePath) throws IOException, InterruptedException, ExecutionException {
        if (!file.exists() || file.length() == 0) {
            synchronized (getLock(file)) {
                // dbl check is ok here since it's file-based
                if (!file.exists() || file.length() == 0) {
                    syncFile(file, remotePath);
                    removeLock(file);
                }
            }
        }
    }

    public void syncFile(File file, String remotePath) throws IOException, InterruptedException, ExecutionException, CancellationException {
        checkConnection(execEnv, remotePath);
        Future<Integer> task = CommonTasksSupport.downloadFile(remotePath, execEnv, file.getAbsolutePath(), null);
        try {
            int rc = task.get().intValue();
            if (rc == 0) {
                fileCopyCount++;
            } else {
                throw new IOException("Can't copy file " + file.getAbsolutePath() + // NOI18N
                        " from " + execEnv + ':' + remotePath + ": rc=" + rc); //NOI18N
            }
        } catch (InterruptedException ex) {
            truncate(file);
            throw ex;
        } catch (ExecutionException ex) {
            truncate(file);
            throw ex;
        }
    }

    private void truncate(File file) throws IOException {
        OutputStream os = new FileOutputStream(file);
        os.close();

    }

    /**
     * Ensured that the directory is synchronized
     * @param remoteChild - used only in the case connection is needed,
     * to inform user, which file are we going to synchronize
     */
    public void ensureDirSync(File dir, String remoteDir, String remoteChild) throws IOException, CancellationException {        
        // TODO: synchronization
        if( ! dir.exists() || ! new File(dir, FLAG_FILE_NAME).exists()) {
            synchronized (getLock(dir)) {
                // dbl check is ok here since it's file-based
                if( ! dir.exists() || ! new File(dir, FLAG_FILE_NAME).exists()) {
                    syncDirStruct(dir, remoteDir, remoteChild);
                    removeLock(dir);
                }
            }
        }
    }

    private void syncDirStruct(File dir, String remoteDir, String remoteChild) throws IOException, CancellationException {
        checkConnection(execEnv, remoteChild);
        if (dir.exists()) {
            CndUtils.assertTrue(dir.isDirectory(), dir.getAbsolutePath() + " is not a directory"); //NOI18N
        } else {
            if( !dir.mkdirs()) {
                throw new IOException("Can not create directory " + dir.getAbsolutePath()); //NOI18N
            }
        }
        NativeProcessBuilder processBuilder = NativeProcessBuilder.newProcessBuilder(execEnv);
        // TODO: error processing
        processBuilder.setWorkingDirectory(remoteDir);
        processBuilder.setCommandLine("ls -1F"); // NOI18N
        processBuilder.redirectError();
        NativeProcess process = processBuilder.call();
        final InputStream is = process.getInputStream();
        final BufferedReader rdr = new BufferedReader(new InputStreamReader(is));
        String fileName;
        while ((fileName = rdr.readLine()) != null) {
            boolean directory = fileName.endsWith("/"); // NOI18N
            File file = new File(dir, fileName);
            boolean result = directory ? file.mkdirs() : file.createNewFile();
            // TODO: error processing
                RemoteUtil.LOGGER.finest("\t" + fileName);
            file.createNewFile(); // TODO: error processing
        }
        rdr.close();
        is.close();
        File flag = new File(dir, FLAG_FILE_NAME);
        flag.createNewFile(); // TODO: error processing
        dirSyncCount++;
    }

    /*package-local test method*/ void resetStatistic() {
        this.dirSyncCount = 0;
        this.fileCopyCount = 0;
    }

    /*package-local test method*/ int getDirSyncCount() {
        return dirSyncCount;
    }

    /*package-local test method*/ int getFileCopyCount() {
        return fileCopyCount;
    }

    private String toString(ExecutionEnvironment execEnv) {
        ServerRecord rec = ServerList.get(execEnv);
        if (rec == null) {
            return execEnv.toString();
        } else {
            return rec.getDisplayName();
        }
    }

    private void checkConnection(ExecutionEnvironment execEnv, String filePath) throws IOException, CancellationException {
        if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
            // a workaround for #171731 IDE hangs after start
            if (true) { // fool javac
                if (Boolean.getBoolean("cnd.remote.fs.notify")) {
                    RemoteFileSystemNotifier.show(execEnv);
                }
                throw new CancellationException();
            }
            synchronized (cancels) {
                Boolean cancel = cancels.get(execEnv);
                if (cancel != null && cancel.booleanValue()) {
                    throw new CancellationException();
                }
            }
            if (SwingUtilities.isEventDispatchThread()) {
                // TODO: error processing
                throw new CancellationException();
            }
            try {
                String title = NbBundle.getMessage(getClass(), "DLG_TITLE_Connect");
                String message = MessageFormat.format(
                        NbBundle.getMessage(getClass(), "ERR_NeedToConnectToRemoteHost"),
                        filePath, toString(execEnv));
                if (RemoteServerListUI.showConfirmDialog(message, title)) {
                    if (!ConnectionManager.getInstance().connectTo(execEnv)) {
                        throw new IOException("Can not connect to " + execEnv); //NOI18N
                    }
                } else {
                    throw new CancellationException();
                }
            } catch (CancellationException ex) {
                synchronized(cancels) {
                    cancels.put(execEnv, Boolean.TRUE);
                }
                throw ex;
            } catch (IOException ex) {
                throw ex;
            }
        }
    }
}
