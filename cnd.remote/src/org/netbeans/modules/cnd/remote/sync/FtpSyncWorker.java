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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.remote.support.RemoteLogger;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import static org.netbeans.modules.cnd.remote.sync.FileState.COPIED;
import static org.netbeans.modules.cnd.remote.sync.FileState.ERROR;
import static org.netbeans.modules.cnd.remote.sync.FileState.INITIAL;
import static org.netbeans.modules.cnd.remote.sync.FileState.TOUCHED;
import static org.netbeans.modules.cnd.remote.sync.FileState.UNCONTROLLED;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport.UploadStatus;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

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

    private final RequestProcessor RP = new RequestProcessor("FtpSyncWorker", 2); // NOI18N

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
        
        RemoteLogger.fine("Uploading {0} to {1} ...\n", getLocalFilesString(), executionEnvironment); // NOI18N
        long time = System.currentTimeMillis();

        out.println(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Message_GatherFiles"));
        fileCollector.gatherFiles();

        progressHandle.switchToDeterminate(fileCollector.getFiles().size());

        long time2;

        time2 = System.currentTimeMillis();
        out.println(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Message_CheckDirs"));
        progressHandle.progress(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Progress_CheckDirs"));
        createDirs();
        RemoteLogger.fine("Creating directories at {0} took {1} ms", executionEnvironment, (System.currentTimeMillis()-time2));
        
        time2 = System.currentTimeMillis();
        out.println(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Message_CheckLinks"));
        progressHandle.progress(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Progress_CheckLinks"));
        createLinks();
        RemoteLogger.fine("Creating links at {0} took {1} ms", executionEnvironment, (System.currentTimeMillis()-time2));

        if (!fileCollector.initNewFilesDiscovery()) {
            throw new IOException(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Msg_Err_NewFilesDiscovery"));
        }
        time2 = System.currentTimeMillis();
        
        if (CndUtils.getBoolean("cnd.remote.zip", true)) {
            try {
                uploadPlainFilesInZip(remoteRoot);
            } catch (ZipIOException ex) {
                err.println(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Msg_TryingToRecoverViaPlainFiles"));
                uploadPlainFiles();
            }
        } else {
            uploadPlainFiles();
        }
        RemoteLogger.fine("Uploading plain files to {0} took {1} ms", executionEnvironment, (System.currentTimeMillis()-time2));

        out.println(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Message_Done"));
        out.println();
        
        if (RemoteLogger.getInstance().isLoggable(Level.FINE)) {
            time = System.currentTimeMillis() - time;
            long bps = uploadSize * 1000L / time;
            String speed = (bps < 1024*8) ? (bps + " b/s") : ((bps/1024) + " Kb/s"); // NOI18N

            String strUploadSize = (uploadSize < 1024 ? (uploadSize + " bytes") : ((uploadSize/1024) + " K")); // NOI18N
            RemoteLogger.fine("\nCopied to {0}:{1}: {2} in {3} files. Time: {4} ms. Avg. speed: {5}\n", // NOI18N
                    executionEnvironment, remoteRoot,
                    strUploadSize, uploadCount, time, speed); // NOI18N
        }
    }

    private interface XArgsFeeder {
        public void feed(BufferedWriter requestWriter) throws IOException;
    }
    
    private void xargs(final XArgsFeeder feeder, String command, String... args) throws IOException {
        if (cancelled) {
            return;
        }
        NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(executionEnvironment);
        pb.setExecutable(command);
        pb.setArguments(args);
        final NativeProcess process;
        process = pb.call();
 
        final AtomicReference<IOException> problem = new AtomicReference<>();

        RP.post(new Runnable() {
            @Override
            public void run() {
                BufferedWriter requestWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                try {
                    feeder.feed(requestWriter);
                } catch (IOException ex) {
                    problem.set(ex);
                } finally {
                    try {
                        requestWriter.close();
                    } catch (IOException ex) {
                        problem.set(ex);
                    }
                }
            }
        });

        RP.post(new Runnable() {
            private final BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            @Override
            public void run() {
                try {
                    for (String errLine = errorReader.readLine(); errLine != null; errLine = errorReader.readLine()) {
                        err.println(errLine); // local println is OK  
                    }
                } catch (IOException ex) {
                    problem.set(ex);
                } finally {
                    try {
                        errorReader.close();
                    } catch (IOException ex) {
                        problem.set(ex);
                    }
                }
            }
        });
        // output supposed to be empty, but in case it's wrong we must read it
        for (String line : ProcessUtils.readProcessOutput(process)) {
            out.println(line); // local println is OK 
        }

        if (problem.get() != null) {
            throw problem.get();
        }
        try {
            int rc = process.waitFor();
            if (rc != 0) {
                throw new IOException();
            }
        } catch (InterruptedException ex) {
            throw new InterruptedIOException();
        }
    }
    
    private void createDirs() throws IOException {
        final List<String> dirsToCreate = new LinkedList<>();
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
            XArgsFeeder feeder = new XArgsFeeder() {
                @Override
                public void feed(BufferedWriter requestWriter) throws IOException {
                    for (String dir : dirsToCreate) {
                        if (cancelled) {
                            throw new InterruptedIOException();
                        }
                        requestWriter.append(dir).append(' ');
                    }
                }
            };
            try {
                xargs(feeder, "xargs", "mkdir", "-p"); // NOI18N
            } catch (InterruptedIOException ex) {
                throw new IOException(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Msg_Err_Canceled"));
            } catch (IOException ex) {
                throw new IOException(NbBundle.getMessage(FtpSyncWorker.class,
                        "FTP_Msg_Err_CheckDirs", ex.getMessage() == null ? "" : ex.getMessage()), ex);
            }
            uploadCount += dirsToCreate.size();
            progressHandle.progress(uploadCount);
        }
    }

    private void createLinks() throws IOException {
        if (cancelled) {
            return;
        }
        XArgsFeeder feeder = new XArgsFeeder() {
            @Override
            public void feed(BufferedWriter requestWriter) throws IOException {
                for (FileCollector.FileInfo fileInfo : fileCollector.getFiles()) {
                    if (cancelled) {
                        throw new InterruptedIOException();
                    }
                    if (fileInfo.isLink()) {
                        progressHandle.progress(fileInfo.file.getAbsolutePath());
                        String localBaseDir = fileInfo.file.getParentFile().getAbsolutePath();
                        String remoteBaseDir = mapper.getRemotePath(localBaseDir, true);
                        CndUtils.assertNotNull(remoteBaseDir, "null remote dir for " + localBaseDir); //NOI18N
                        if (remoteBaseDir != null) {
                            requestWriter.append("cd ").append(remoteBaseDir).append('\n'); // NOI18N
                            requestWriter.append("rm -rf ").append(fileInfo.file.getName()).append('\n'); // NOI18N
                            requestWriter.append("ln -s ") // NOI18N
                                    .append(fileInfo.getLinkTarget()).append(' ')
                                    .append(fileInfo.file.getName()).append('\n');
                        }
                        progressHandle.progress(fileInfo.file.getName(), uploadCount++);
                    }
                }
            }
        };
        try {
            xargs(feeder, "sh", "-s"); // NOI18N
        } catch (InterruptedIOException ex) {
            throw new IOException(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Msg_Err_Canceled"));
        } catch (IOException ex) {
            throw new IOException(NbBundle.getMessage(FtpSyncWorker.class,
                    "FTP_Msg_Err_CheckLinks", ex.getMessage() == null ? "" : ex.getMessage()), ex);
        }
    }

    private void uploadPlainFiles() throws InterruptedException, ExecutionException, IOException {

        List<FileCollector.FileInfo> toCopy = new ArrayList<>();

        for (FileCollector.FileInfo fileInfo : fileCollector.getFiles()) {
            if (cancelled) {
                throw new InterruptedException();
            }
            if (!fileInfo.isLink() && !fileInfo.file.isDirectory()) {
                File srcFile = fileInfo.file;
                if (srcFile.exists() && needsCopying(srcFile)) {
                    toCopy.add(fileInfo);
                }
            }
        }

        if (toCopy.isEmpty()) {
            out.println(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Message_NoFilesToUpload"));
            return;
        }

        out.println(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Message_UploadFilesPlain", toCopy.size()));
        for (FileCollector.FileInfo fileInfo : toCopy) {
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
                        throw new IOException(
                                NbBundle.getMessage(FtpSyncWorker.class, "FTP_Msg_Err_UploadFile", 
                                        srcFile, executionEnvironment, remotePath, 
                                        uploadStatus.getExitCode()));
                    }
                }
                progressHandle.progress(uploadCount++);
            }
        }
    }

    private static final class ZipIOException extends IOException {
        private ZipIOException(String message) {
            super(message);
        }
    }

    private void uploadPlainFilesInZip(String remoteRoot) throws InterruptedException, ExecutionException, IOException {
    
        out.println(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Message_UploadFilesInZip"));
        
        List<FileCollector.FileInfo> toCopy = new ArrayList<>();
        
        for (FileCollector.FileInfo fileInfo : fileCollector.getFiles()) {
            if (cancelled) {
                throw new InterruptedException();
            }
            if (!fileInfo.isLink() && !fileInfo.file.isDirectory()) {
                File srcFile = fileInfo.file;
                if (srcFile.exists() && needsCopying(srcFile)) {
                    toCopy.add(fileInfo);
                }
            }
        }
        
        if (toCopy.isEmpty()) {
            return;
        }
        
        out.println(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Message_Zipping", toCopy.size()));  
        progressHandle.progress(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Progress_Zipping"));            
        File zipFile = null;
        String remoteFile = null;
        try  {
            String localFileName = files[0].getName();
            if (localFileName.length() < 3) {
                localFileName = localFileName + ((localFileName.length() == 1) ? "_" : "__"); //NOI18N
            }
            zipFile = File.createTempFile(localFileName, ".zip", getTemp()); // NOI18N
            Zipper zipper = new Zipper(zipFile);
            {
                RemoteLogger.fine("SFTP/ZIP: Zipping {0} to {1}...", getLocalFilesString(), zipFile);
                long zipTime = System.currentTimeMillis();
                int progress = 0;
                for (FileCollector.FileInfo fileInfo : toCopy) {
                    if (cancelled) {
                        throw new InterruptedException();
                    }
                    File srcFile = fileInfo.file;
                    String remoteDir = mapper.getRemotePath(srcFile.getParent(), false);
                    if (remoteDir == null) { // this never happens since mapper is fixed
                        throw new IOException(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Err_CantMap", srcFile.getAbsolutePath()));
                    }
                    String base;
                    if (remoteDir.startsWith(remoteRoot)) {
                        base = remoteDir.substring(remoteRoot.length() + 1);
                    } else {
                        // this is never the case! - but...
                        throw new IOException(remoteDir + " should start with " + remoteRoot); //NOI18N
                    }
                    zipper.add(srcFile, filter, base); // TODO: do we need filter? isn't it already filtered?
                    if (progress++ % 3 == 0) {
                        progressHandle.progress(srcFile.getName(), uploadCount++);
                    }
                }
                zipper.close();
                RemoteLogger.fine("SFTP/ZIP: Zipping {0} files to {1} took {2} ms\n", //NOI18N
                        toCopy.size(), zipFile, System.currentTimeMillis()-zipTime); //NOI18N
            }

            if (cancelled) {
                throw new InterruptedException();
            }
 
            out.println(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Message_UploadingZip", executionEnvironment));
            progressHandle.progress(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Progress_UploadingZip"));
            remoteFile = remoteRoot + '/' + zipFile.getName(); //NOI18N
            {
                long uploadStart = System.currentTimeMillis();
                Future<UploadStatus> upload = CommonTasksSupport.uploadFile(zipFile.getAbsolutePath(), executionEnvironment, remoteFile, 0600);
                UploadStatus uploadStatus = upload.get();
                RemoteLogger.fine("SFTP/ZIP:  uploading {0}to {1}:{2} finished in {3} ms with rc={4}", //NOI18N
                        zipFile, executionEnvironment, remoteFile, 
                        System.currentTimeMillis()-uploadStart, uploadStatus.getExitCode());
                if (!uploadStatus.isOK()) {
                    throw new IOException(uploadStatus.getError());
                }
            }
            
            if (cancelled) {
                throw new InterruptedException();
            }

            out.println(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Message_Unzipping", executionEnvironment));  
            progressHandle.progress(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Progress_Unzipping"),
                    (uploadCount += (toCopy.size()/3)));
            {
                long unzipTime = System.currentTimeMillis();
                NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(executionEnvironment);
                pb.getEnvironment().put("TZ", TimeZone.getDefault().getID()); //NOI18N
                pb.setExecutable("unzip"); // NOI18N
                pb.setArguments("-oqq", remoteFile); // NOI18N
                pb.setWorkingDirectory(remoteRoot);
                Process proc = pb.call();
                String line;

                BufferedReader inReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                try {
                    while ((line = inReader.readLine()) != null) {
                        if (RemoteUtil.LOGGER.isLoggable(Level.FINEST)) {
                            System.out.printf("\tunzip: %s\n", line); // NOI18N
                        } //NOI18N
                    }
                } finally {
                    inReader.close();
                }

                BufferedReader errReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                try {
                    while ((line = errReader.readLine()) != null) {
                        err.printf("unzip: %s\n", line); //NOI18N
                    }
                } finally {
                    errReader.close();
                }

                int rc = proc.waitFor();
                
                RemoteLogger.fine("SFTP/ZIP: Unzipping {0}:{1} finished in {2} ms; rc={3}", // NOI18N
                        executionEnvironment , remoteFile, System.currentTimeMillis()-unzipTime, rc); 
            
                if (rc != 0) {
                    throw new ZipIOException(NbBundle.getMessage(FtpSyncWorker.class, "FTP_Err_Unzip",
                            remoteFile, executionEnvironment, rc)); // NOI18N
                }
                for (FileCollector.FileInfo fileInfo : toCopy) {
                    fileData.setState(fileInfo.file, FileState.COPIED);
                }
            }
        } finally {
            if (zipFile != null && zipFile.exists()) {
                if (!zipFile.delete()) {
                    RemoteUtil.LOGGER.log(Level.INFO, "Can not delete temporary file {0}", zipFile.getAbsolutePath()); //NOI18N
                }
            }
            if (remoteFile != null) {
                CommonTasksSupport.rmFile(executionEnvironment, remoteFile, null);
            }
        }
        progressHandle.progress(uploadCount += (toCopy.size()/3));
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
    
    private static File getTemp() {
        String tmpPath = System.getProperty("java.io.tmpdir");
        File tmpFile = CndFileUtils.createLocalFile(tmpPath);
        return tmpFile.exists() ? tmpFile : null;
    }    
}
