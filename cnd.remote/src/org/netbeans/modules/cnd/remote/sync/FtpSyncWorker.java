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

package org.netbeans.modules.cnd.remote.sync;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import static org.netbeans.modules.cnd.remote.sync.FileState.COPIED;
import static org.netbeans.modules.cnd.remote.sync.FileState.ERROR;
import static org.netbeans.modules.cnd.remote.sync.FileState.INITIAL;
import static org.netbeans.modules.cnd.remote.sync.FileState.TOUCHED;
import static org.netbeans.modules.cnd.remote.sync.FileState.UNCONTROLLED;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport.UploadStatus;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Kvashin
 */
/*package-local*/ final class FtpSyncWorker extends BaseSyncWorker implements RemoteSyncWorker, Cancellable {

    private FileData fileData;
    private FileCollector fileCollector;
    private final RemoteUtil.PrefixedLogger logger;
    private final RemotePathMap mapper;
    private final SharabilityFilter filter;

    private int uploadCount;
    private long uploadSize;
    private volatile Thread thread;
    private boolean cancelled;
    private ProgressHandle progressHandle;

    private static final boolean HARD_CODED_FILTER = Boolean.valueOf(System.getProperty("cnd.remote.hardcoded.filter", "true")); //NOI18N

    public FtpSyncWorker(ExecutionEnvironment executionEnvironment, PrintWriter out, PrintWriter err, 
            FileObject privProjectStorageDir, List<FSPath> paths, List<FSPath> buildResults) {
        super(executionEnvironment, out, err, privProjectStorageDir, paths, buildResults);
        this.mapper = RemotePathMap.getPathMap(executionEnvironment);
        this.logger = new RemoteUtil.PrefixedLogger("FtpSyncWorker[" + executionEnvironment + "]"); //NOI18N
        this.filter = new SharabilityFilter();
    }

    /** for trace/debug purposes */
    private StringBuilder getLocalFilesString() {
        StringBuilder sb = new StringBuilder();
        for (File f : files) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(f.getAbsolutePath());
        }
        return sb;
    }

    private boolean needsCopying(File file) {

        if (HARD_CODED_FILTER) {
            // Filter out configurations.xml, timestamps, etc
            // Auto-copy would never request these; but FTP will copy, unless filtered out
            File parent = file.getParentFile();
            if (parent != null) {
                if (parent.getName().equals("nbproject")) { // NOI18N
                    // we never need configuratins.xml for build purposes; however it might be quite large
                    if (file.getName().equals("configurations.xml")) { // NOI18N
                        return false;
                    }
                } else if (parent.getName().equals("private")) { // NOI18N
                    File granpa = parent.getParentFile();
                    if (granpa.getName().equals("nbproject")) { // NOI18N
                        if (!file.getName().endsWith(".mk") && !file.getName().endsWith(".sh") && !file.getName().endsWith(".bash")) { // NOI18N
                            return false;
                        }
                    }
                }
            }
        }

        FileData.FileInfo info = fileData.getFileInfo(file);
        FileState state = (info == null) ? FileState.INITIAL : info.state;
        switch (state) {
            case INITIAL:       return true;
            case TOUCHED:       return true;
            case COPIED:        return info.timestamp != file.lastModified();
            case ERROR:         return true;
            case UNCONTROLLED:  return false;
            default:
                CndUtils.assertTrue(false, "Unexpected state: " + state); //NOI18N
                return false;
        }
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void synchronizeImpl(String remoteRoot) throws InterruptedException, ExecutionException, IOException, ConnectionManager.CancellationException {

        fileData = FileData.get(privProjectStorageDir, executionEnvironment);
        fileCollector = new FileCollector(files, buildResults, logger, mapper, filter, fileData, executionEnvironment, err);

        uploadCount = 0;
        uploadSize = 0;
        long time = 0;
        
        if (RemoteUtil.LOGGER.isLoggable(Level.FINE)) {
            System.out.printf("Uploading %s to %s ...\n", getLocalFilesString(), executionEnvironment); // NOI18N
            time = System.currentTimeMillis();
        }

        fileCollector.gatherFiles();

        progressHandle.switchToDeterminate(fileCollector.getFiles().size());

        createDirs();
        createLinks();
        if (!fileCollector.initNewFilesDiscovery()) {
            throw new IOException();
        }
        uploadPlainFiles();

        if (RemoteUtil.LOGGER.isLoggable(Level.FINE)) {
            time = System.currentTimeMillis() - time;
            long bps = uploadSize * 1000L / time;
            String speed = (bps < 1024*8) ? (bps + " b/s") : ((bps/1024) + " Kb/s"); // NOI18N

            String strUploadSize = (uploadSize < 1024 ? (uploadSize + " bytes") : ((uploadSize/1024) + " K")); // NOI18N
            System.out.printf("\n\nCopied to %s:%s: %s in %d files. Time: %d ms. Avg. speed: %s\n\n", // NOI18N
                    executionEnvironment, remoteRoot,
                    strUploadSize, uploadCount, time, speed); // NOI18N
        }
    }

    private void createDirs() throws IOException {
        List<String> dirsToCreate = new LinkedList<String>();
        for (FileCollector.FileInfo fileInfo : fileCollector.getFiles()) {
            if (fileInfo.file.isDirectory() && ! fileInfo.isLink()) {
                String remoteDir = mapper.getRemotePath(fileInfo.file.getAbsolutePath(), true);
                CndUtils.assertNotNull(remoteDir, "null remote file for " + fileInfo.file.getAbsolutePath()); //NOI18N
                if (remoteDir != null) {
                    dirsToCreate.add(remoteDir);
                }
            }
        }
        if (cancelled) {
            return;
        }
        if (!dirsToCreate.isEmpty()) {
            dirsToCreate.add(0, "-p"); // NOI18N
            ExitStatus status = ProcessUtils.execute(executionEnvironment, "mkdir", dirsToCreate.toArray(new String[dirsToCreate.size()])); // NOI18N
            if (!status.isOK()) {
                throw new IOException("Can not check remote directories: " + status.toString()); // NOI18N
            }
            uploadCount += dirsToCreate.size();
            progressHandle.progress(uploadCount);
        }
    }

    private void createLinks() throws IOException {
        for (FileCollector.FileInfo fileInfo : fileCollector.getFiles()) {
            if (cancelled) {
                return;
            }
            if (fileInfo.isLink()) {
                progressHandle.progress(fileInfo.file.getAbsolutePath());
                String localBaseDir = fileInfo.file.getParentFile().getAbsolutePath();
                String remoteBaseDir = mapper.getRemotePath(localBaseDir, true);
                CndUtils.assertNotNull(remoteBaseDir, "null remote dir for " + localBaseDir); //NOI18N
                if (remoteBaseDir == null) {
                    continue;
                }
                // TODO: We now call "ln -s" per file. Optimize this: write and run a script.
                ExitStatus status = ProcessUtils.executeInDir(remoteBaseDir, executionEnvironment, 
                        "ln", "-s", fileInfo.getLinkTarget(), fileInfo.file.getName()); // NOI18N
                if (!status.isOK()) {
                    throw new IOException("Can not check remote directories: " + status.toString()); // NOI18N
                }
                progressHandle.progress(uploadCount++);
            }
        }
    }

    private void uploadPlainFiles() throws InterruptedException, ExecutionException, IOException {
        for (FileCollector.FileInfo fileInfo : fileCollector.getFiles()) {
            if (cancelled) {
                return;
            }
            if (!fileInfo.isLink() && !fileInfo.file.isDirectory()) {
                File srcFile = fileInfo.file;
                if (srcFile.exists() && needsCopying(srcFile)) {
                    progressHandle.progress(srcFile.getAbsolutePath());
                    String remotePath = mapper.getRemotePath(srcFile.getAbsolutePath(), false);
                    Future<UploadStatus> fileTask = CommonTasksSupport.uploadFile(srcFile.getAbsolutePath(),
                            executionEnvironment, remotePath, srcFile.canExecute() ? 0700 : 0600);
                    UploadStatus uploadStatus = fileTask.get();
                    if (uploadStatus.isOK()) {
                        fileData.setState(srcFile, FileState.COPIED);
                        uploadSize += srcFile.length();
                    } else {
                        if (err != null) {
                            err.println(uploadStatus.getError());
                        }
                        throw new IOException("uploading " + srcFile + " to " + executionEnvironment + ':' + remotePath + // NOI18N
                                " finished with error code " + uploadStatus.getExitCode()); // NOI18N
                    }
                }
                progressHandle.progress(uploadCount++);
            }
        }
    }

    @Override
    public boolean startup(Map<String, String> env2add) {

        if (SyncUtils.isDoubleRemote(executionEnvironment, fileSystem)) {
            SyncUtils.warnDoubleRemote(executionEnvironment, fileSystem);
            return false;
        }

        // Later we'll allow user to specify where to copy project files to
        String remoteRoot = RemotePathMap.getRemoteSyncRoot(executionEnvironment);
        if (remoteRoot == null) {
            if (err != null) {
                err.printf("%s\n", NbBundle.getMessage(getClass(), "MSG_Cant_find_sync_root", ServerList.get(executionEnvironment).toString()));
            }
            return false; // TODO: error processing
        }

        boolean success = false;
        thread = Thread.currentThread();
        cancelled = false;
        //String title = NbBundle.getMessage(getClass(), "PROGRESS_UPLOADING", ServerList.get(executionEnvironment).getDisplayName());
        String title = "Uploading to " + ServerList.get(executionEnvironment).getDisplayName(); //NOI18N FIXUP
        progressHandle = ProgressHandleFactory.createHandle(title, this);
        progressHandle.start();
        try {
            if (out != null) {
                out.printf("%s\n", NbBundle.getMessage(getClass(), "MSG_Copying",
                        remoteRoot, ServerList.get(executionEnvironment).toString()));
            }
            synchronizeImpl(remoteRoot);
            success = ! cancelled;
            if (success) {
                fileData.store();
            }
        } catch (InterruptedException ex) {
            // reporting does not make sense, just return false
            RemoteUtil.LOGGER.finest(ex.getMessage());
        } catch (InterruptedIOException ex) {
            // reporting does not make sense, just return false
            RemoteUtil.LOGGER.finest(ex.getMessage());
        } catch (ExecutionException ex) {
            RemoteUtil.LOGGER.log(Level.FINE, null, ex);
            if (err != null) {
                err.printf("%s\n", NbBundle.getMessage(getClass(), "MSG_Error_Copying",
                        remoteRoot, ServerList.get(executionEnvironment).toString(), ex.getLocalizedMessage()));
            }
        } catch (IOException ex) {
            RemoteUtil.LOGGER.log(Level.FINE, null, ex);
            if (err != null) {
                err.printf("%s\n", NbBundle.getMessage(getClass(), "MSG_Error_Copying",
                        remoteRoot, ServerList.get(executionEnvironment).toString(), ex.getLocalizedMessage()));
            }
        } catch (ConnectionManager.CancellationException ex) {
            cancelled = true;
        } finally {
            cancelled = false;
            thread = null;
            progressHandle.finish();
        }
        return success;
    }

    @Override
    public void shutdown() {
        try {
            fileCollector.runNewFilesDiscovery(true);
            fileCollector.shutDownNewFilesDiscovery();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        } catch (InterruptedException | ConnectionManager.CancellationException ex) {
            // don't report InterruptedException or CancellationException
        }
    }

    @Override
    public boolean cancel() {
        cancelled = true;
        Thread t = thread;
        if (t != null) {
            t.interrupt();
        }
        return true;
    }
}
