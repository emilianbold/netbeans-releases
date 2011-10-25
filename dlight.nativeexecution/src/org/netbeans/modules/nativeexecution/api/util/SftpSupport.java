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
package org.netbeans.modules.nativeexecution.api.util;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.ConnectException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport.UploadStatus;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo;
import org.netbeans.modules.nativeexecution.api.util.Md5checker.Result;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 * @author Vladimir Kvashin
 */
class SftpSupport {

    //
    // Static stuff
    //
    private static final boolean isUnitTest = Boolean.getBoolean("nativeexecution.mode.unittest"); // NOI18N
    private static final java.util.logging.Logger LOG = Logger.getInstance();
    private static final Object instancesLock = new Object();
    private static Map<ExecutionEnvironment, SftpSupport> instances = new HashMap<ExecutionEnvironment, SftpSupport>();
    private static AtomicInteger uploadCount = new AtomicInteger(0);

    private static final int PUT_RETRY_COUNT = Integer.getInteger("sftp.put.retries", 1); // NOI18N
    
    /** for test purposes only */
    /*package-local*/ static int getUploadCount() {
        return uploadCount.get();
    }

    /*package*/ static SftpSupport getInstance(ExecutionEnvironment execEnv) {
        SftpSupport instance = null;
        synchronized (instancesLock) {
            instance = instances.get(execEnv);
            if (instance == null) {
                instance = new SftpSupport(execEnv);
                instances.put(execEnv, instance);
            }
        }
        return instance;
    }

    private static int CONCURRENCY_LEVEL = 
            Integer.getInteger("remote.sftp.threads", Runtime.getRuntime().availableProcessors() + 2); // NOI18N

    private static final String PREFIX = "SFTP: "; // NOI18N;
//    private final RequestProcessor readRuestProcessor = new RequestProcessor(PREFIX + "read", CONCURRENCY_LEVEL); //NOI18N
//    private final RequestProcessor writeRuestProcessor = new RequestProcessor(PREFIX + "write", 1); //NOI18N
    private final RequestProcessor requestProcessor = new RequestProcessor(PREFIX, CONCURRENCY_LEVEL); //NOI18N
        
    //
    // Instance stuff
    //
    private final ExecutionEnvironment execEnv;

    private LinkedList<ChannelSftp> spareChannels = new LinkedList<ChannelSftp>();
    
    // just a primitive statistics
    private int currBusyChannels = 0;
    private int maxBusyChannels = 0;
    
    private final Object channelLock = new Object();

    private SftpSupport(ExecutionEnvironment execEnv) {
        this.execEnv = execEnv;
        LOG.log(Level.FINE, "SftpSupport for {0} started with maximum thread count: {1}", new Object[] { execEnv, CONCURRENCY_LEVEL });
    }

    private RequestProcessor getReadRequestProcessor() {
        return requestProcessor; // readRuestProcessor;
    }

    public RequestProcessor getWriteRuestProcessor() {
        return requestProcessor; // writeRuestProcessor;
    }
    
    private void incrementStatistics() {
        synchronized (channelLock) {
            currBusyChannels++;
            if (currBusyChannels > maxBusyChannels) {
                maxBusyChannels = currBusyChannels;
                Logger.getInstance().log(Level.FINEST, "SFTP max. busy channels reached: {0}", maxBusyChannels);
            }
        }
    }
    
    private void decrementStatistics() {
        synchronized (channelLock) {
            currBusyChannels--;
        }
    }
    
    private void releaseChannel(ChannelSftp channel) {
        synchronized (channelLock) {
            spareChannels.push(channel);
            decrementStatistics();
        }
    }

    private ChannelSftp getChannel() throws IOException, CancellationException, JSchException, ExecutionException, InterruptedException {
        // try to reuse channel
        synchronized (channelLock) {
            if (!spareChannels.isEmpty()) {
                ChannelSftp channel = spareChannels.pop();
                if (channel.isConnected()) {
                    incrementStatistics();
                    return channel;
                }
            }
        }
        // no spare channels - create a new one            
        if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
            ConnectionManager.getInstance().connectTo(execEnv);
        }
        ConnectionManagerAccessor cmAccess = ConnectionManagerAccessor.getDefault();
        if (cmAccess == null) { // is it a paranoja?
            throw new ExecutionException("Error getting ConnectionManagerAccessor", new NullPointerException()); //NOI18N
        }                
        ChannelSftp channel = (ChannelSftp) cmAccess.openAndAcquireChannel(execEnv, "sftp", true); // NOI18N
        if (channel == null) {
            throw new ExecutionException("ConnectionManagerAccessor returned null channel while waitIfNoAvailable was set to true", new NullPointerException()); //NOI18N
        }
        channel.connect();
        incrementStatistics();
        return channel;
    }

    private static SftpException decorateSftpException(SftpException e, String path) {
        if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
            FileNotFoundException fnfe = (path == null) ?  
                    new FileNotFoundException(e.getMessage()) : new FileNotFoundException(path);
            return new SftpException(e.id, e.getMessage(), fnfe);
        } else {
            return e;
        }
    }

    private abstract class Worker implements Callable<Integer> {

        protected final Writer error;

        public Worker(Writer error) {
            this.error = error;
        }

        protected abstract void work() throws JSchException, SftpException, IOException, CancellationException, InterruptedException, ExecutionException;

        protected abstract String getTraceName();

        @Override
        public Integer call() throws InterruptedException {
            int rc = -1;
            try {                
                Thread.currentThread().setName(PREFIX + ": " + getTraceName()); // NOI18N
                work();
                rc = 0;
            } catch (JSchException ex) {
                if (ex.getMessage().contains("Received message is too long: ")) { // NOI18N
                    // This is a known issue... but we cannot
                    // do anything with this ;(
                    if (isUnitTest) {
                        logException(ex);
                    } else {
                        Message message = new NotifyDescriptor.Message(NbBundle.getMessage(SftpSupport.class, "SftpConnectionReceivedMessageIsTooLong.error.text"), Message.ERROR_MESSAGE); // NOI18N
                        DialogDisplayer.getDefault().notifyLater(message);
                    }
                    rc = 7;
                } else {
                    logException(ex);
                    rc = 1;
                }
            } catch (SftpException ex) {
                logException(ex);
                rc = 2;
            } catch (ConnectException ex) {
                logException(ex);
                rc = 3;
            } catch (InterruptedIOException ex) {
                rc = 4;
                throw new InterruptedException(ex.getMessage());
            } catch (IOException ex) {
                logException(ex);
                rc = 5;
            } catch (CancellationException ex) {
                // no trace
                rc = 6;
            } catch (ExecutionException ex) {
                logException(ex);
                rc = 7;
            }
            LOG.log(Level.FINE, "{0}{1}", new Object[]{getTraceName(), rc == 0 ? " OK" : " FAILED"});
            return rc;
        }

        protected void logException(Exception ex) {
            LOG.log(Level.INFO, "Error " + getTraceName(), ex);
        }
    }
    
    private class Uploader implements Callable<UploadStatus> {

        private final int mask;
        private final boolean checkMd5;
        protected final String srcFileName;
        protected final String dstFileName;
        protected StatInfo statInfo;

        public Uploader(String srcFileName, String dstFileName, int mask, boolean checkMd5) {
            this.srcFileName = srcFileName;
            this.dstFileName = dstFileName;
            this.mask = mask;
            this.checkMd5 = checkMd5;
        }

        @Override
        public UploadStatus call() throws InterruptedException {
            StringBuilder err = new StringBuilder();
            int rc = -1;
            try {                
                Thread.currentThread().setName(PREFIX + ": " + getTraceName()); // NOI18N
                work(err);
                rc = 0;
            } catch (JSchException ex) {
                if (ex.getMessage().contains("Received message is too long: ")) { // NOI18N
                    // This is a known issue... but we cannot
                    // do anything with this ;(
                    if (isUnitTest) {
                        logException(ex);
                    } else {
                        Message message = new NotifyDescriptor.Message(NbBundle.getMessage(SftpSupport.class, "SftpConnectionReceivedMessageIsTooLong.error.text"), Message.ERROR_MESSAGE); // NOI18N
                        DialogDisplayer.getDefault().notifyLater(message);
                    }
                    rc = 7;
                } else {
                    logException(ex);
                    rc = 1;
                }
                err.append(ex.getMessage());
            } catch (SftpException ex) {
                err.append(ex.getMessage());
                logException(ex);
                rc = 2;
            } catch (ConnectException ex) {
                err.append(ex.getMessage());
                logException(ex);
                rc = 3;
            } catch (InterruptedIOException ex) {
                err.append(ex.getMessage());
                rc = 4;
                throw new InterruptedException(ex.getMessage());
            } catch (IOException ex) {
                err.append(ex.getMessage());
                logException(ex);
                rc = 5;
            } catch (CancellationException ex) {
                err.append(ex.getMessage());
                // no trace
                rc = 6;
            } catch (ExecutionException ex) {
                err.append(ex.getMessage());
                logException(ex);
                rc = 7;
            }
            LOG.log(Level.FINE, "{0}{1}", new Object[]{getTraceName(), rc == 0 ? " OK" : " FAILED"});
            return new UploadStatus(rc, err.toString(), statInfo);
        }
        
        protected void logException(Exception ex) {
            LOG.log(Level.INFO, "Error " + getTraceName(), ex);
        }
        
        private void work(StringBuilder err) throws IOException, CancellationException, JSchException, SftpException, InterruptedException, ExecutionException {
            boolean checkDir = false;
            if (checkMd5) {
                LOG.log(Level.FINE, "Md5 check for {0}:{1} started", new Object[]{execEnv, dstFileName});
                Result res = null;
                try {
                    res = new Md5checker(execEnv).check(new File(srcFileName), dstFileName);
                } catch (NoSuchAlgorithmException ex) {
                    LOG.log(Level.WARNING, "Can not perform md5 check for {0}: {1}", new Object[]{execEnv.getDisplayName(), ex.getMessage()});
                    if (HostInfoUtils.fileExists(execEnv, dstFileName)) {
                        res = Md5checker.Result.UPTODATE;
                    } else {
                        res = Md5checker.Result.INEXISTENT;
                    }
                } catch (Md5checker.CheckSumException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InterruptedException ex) {
                    LOG.log(Level.FINE, "SftpSupport interrupted", ex);
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
                switch (res) {
                    case UPTODATE:
                        LOG.log(Level.FINE, "{0}:{1} up to date - skipped", new Object[]{execEnv, dstFileName});
                        return;
                    case DIFFERS:
                        break;
                    case INEXISTENT:
                        checkDir = true;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected MD5 check result: " + res); //NOI18N
                }
            }
            LOG.log(Level.FINE, "{0} started", getTraceName());
            ChannelSftp cftp = getChannel();
            try {
                if (checkDir) {
                    int slashPos = dstFileName.lastIndexOf('/'); //NOI18N
                    if (slashPos >= 0) {
                        String remoteDir = dstFileName.substring(0, slashPos);
                        StringWriter swr = new StringWriter();
                        CommonTasksSupport.mkDir(execEnv, remoteDir, swr).get();
                        err.append(swr.getBuffer()).append(' ');
                    }
                }
                put(cftp);
                if (mask >= 0) {
                    cftp.chmod(mask, dstFileName);
                }
                SftpATTRS attrs = cftp.lstat(dstFileName);
                // can't use PathUtilities since we are in ide cluster
                int slashPos = dstFileName.lastIndexOf('/');
                String dirName, baseName;
                if (slashPos < 0) {
                    dirName = dstFileName;
                    baseName = "";
                } else {
                    dirName = dstFileName.substring(0, slashPos);
                    baseName = dstFileName.substring(slashPos + 1);
                }
                statInfo = createStatInfo(dirName, baseName, attrs, cftp);                
            } catch (SftpException e) {
                throw decorateSftpException(e, dstFileName);
            } finally {
                releaseChannel(cftp);
            }
            uploadCount.incrementAndGet();
        }

        private void put(ChannelSftp cftp) throws SftpException {
            // the below is just the replacement for one code line:
            // cftp.put(srcFileName, dstFileName);
            // (connected with #184068 -  Instable remote unit tests failure)
            int attempt = 0;
            while (true) {
                attempt++;
                try {
                    cftp.put(srcFileName, dstFileName);
                    if (attempt > 1) {
                        LOG.log(Level.FINE, "Success on attempt {0} to copy {1} to {2}:{3} :\n",
                                new Object[] {attempt, srcFileName, execEnv, dstFileName});
                    }
                    return;
                } catch (SftpException e) {
                    if (attempt > PUT_RETRY_COUNT) {
                        throw decorateSftpException(e, dstFileName);
                    } else {
                        String message = String.format("Error on attempt %d to copy %s to %s:%s :\n", // NOI18N
                                attempt, srcFileName, execEnv, dstFileName);
                        LOG.log(Level.FINE, message, e);
                        if (attempt == 2) {
                            Logger.fullThreadDump(message);
                        }
                        e.printStackTrace();
                    }
                }
            }
        }

        protected String getTraceName() {
            return "Uploading " + srcFileName + " to " + execEnv + ":" + dstFileName; // NOI18N
        }
    }

    private class Downloader extends Worker implements Callable<Integer> {

        protected final String srcFileName;
        protected final String dstFileName;
        
        public Downloader(String srcFileName, String dstFileName, Writer error) {
            super(error);
            this.srcFileName = srcFileName;
            this.dstFileName = dstFileName;
        }

        @Override
        protected void work() throws IOException, CancellationException, JSchException, SftpException, ExecutionException, InterruptedException {
            LOG.log(Level.FINE, "{0} started", getTraceName());
            ChannelSftp cftp = getChannel();
            try {
                cftp.get(srcFileName, dstFileName);
            } catch (SftpException e) {
                throw decorateSftpException(e, srcFileName);
            } finally {
                releaseChannel(cftp);
            }
        }

        @Override
        protected String getTraceName() {
            return "Downloading " + execEnv + ":" + srcFileName + " to " + dstFileName; // NOI18N
        }
    }
    
    /*package*/ Future<UploadStatus> uploadFile(CommonTasksSupport.UploadParameters parameters) {
        Logger.assertTrue(parameters.dstExecEnv.equals(execEnv));
        Uploader uploader = new Uploader(
                parameters.srcFile.getAbsolutePath(),
                parameters.dstFileName, parameters.mask, parameters.checkMd5);
        final FutureTask<UploadStatus> ftask = new FutureTask<UploadStatus>(uploader);
        RequestProcessor.Task requestProcessorTask = getWriteRuestProcessor().create(ftask);
        if (parameters.callback != null) {
            final ChangeListener callback = parameters.callback;
            requestProcessorTask.addTaskListener(new TaskListener() {
                @Override
                public void taskFinished(Task task) {
                    callback.stateChanged(new ChangeEvent(ftask));
                }
            });
        }
        requestProcessorTask.schedule(0);
        LOG.log(Level.FINE, "{0} schedulled", uploader.getTraceName());
        return ftask;
    }

    /*package*/ Future<Integer> downloadFile(
            final String srcFileName,
            final String dstFileName,
            final Writer error) {

        Downloader downloader = new Downloader(srcFileName, dstFileName, error);
        FutureTask<Integer> ftask = new FutureTask<Integer>(downloader);
        getReadRequestProcessor().post(ftask);
        LOG.log(Level.FINE, "{0} schedulled", downloader.getTraceName());
        return ftask;
    }

    private class StatLoader implements Callable<StatInfo> {

        private final String path;

        public StatLoader(String path) {
            assert path.startsWith("/"); //NOI18N
            this.path = path;
        }
                
        @Override
        public StatInfo call() throws IOException, CancellationException, JSchException, ExecutionException, InterruptedException, SftpException {
            LOG.log(Level.FINE, "{0} started", getTraceName());
            StatInfo result;
            ChannelSftp cftp = getChannel();
            try {
                Thread.currentThread().setName(PREFIX + ": " + getTraceName()); // NOI18N
                SftpATTRS attrs = cftp.lstat(path);            
                String dirName, baseName;
                int slashPos = path.lastIndexOf('/');
                if (slashPos == 0) {
                    dirName = "";
                    baseName = path.substring(1);
                } else {
                    dirName = path.substring(0, slashPos);
                    baseName = path.substring(slashPos + 1);
                }
                result = createStatInfo(dirName, baseName, attrs, cftp);
            } catch (SftpException e) {
                throw decorateSftpException(e, path);
            } finally {
                releaseChannel(cftp);
            }
            LOG.log(Level.FINE, "{0} finished", getTraceName());
            return result;
        }

        public String getTraceName() {
            return "Getting stat for " + path; //NOI18N
        }
    }

    private class LsLoader implements Callable<StatInfo[]> {

        private static final int S_IFMT   =  0xF000; //bitmask for the file type bitfields
        private static final int S_IFREG  =  0x8000; //regular file
        private final String path;

        public LsLoader(String path) {
            assert path.startsWith("/"); //NOI18N
            this.path = path;
        }
                
        @Override
        @SuppressWarnings("unchecked")
        public StatInfo[] call() throws IOException, CancellationException, JSchException, ExecutionException, InterruptedException, SftpException {
            LOG.log(Level.FINE, "{0} started", getTraceName());
            List<StatInfo> result = Collections.<StatInfo>emptyList();
            ChannelSftp cftp = getChannel();
            try {
                Thread.currentThread().setName(PREFIX + ": " + getTraceName()); // NOI18N
                List<LsEntry> entries = (List<LsEntry>) cftp.ls(path);
                result = new ArrayList<StatInfo>(Math.max(1, entries.size() - 2));
                int i = 0;
                for (LsEntry entry : entries) {
                    String name = entry.getFilename();
                    if (! ".".equals(name) && ! "..".equals(name)) { //NOI18N
                        SftpATTRS attrs = entry.getAttrs();
                        if (!(attrs.isDir() || attrs.isLink())) {
                            if ( (attrs.getPermissions() & S_IFMT) != S_IFREG) {
                                // skip not regular files
                                continue;
                            }
                        }
                        result.add(createStatInfo(path, name, attrs, cftp));
                    }
                }            
            } catch (SftpException e) {
                throw decorateSftpException(e, path);
            } finally {
                releaseChannel(cftp);
            }
            LOG.log(Level.FINE, "{0} finished", getTraceName());
            return result.toArray(new StatInfo[result.size()]);
        }

        public String getTraceName() {
            return "listing directory " + path; //NOI18N
        }
    }
    
    private StatInfo createStatInfo(String dirName, String baseName, SftpATTRS attrs, ChannelSftp cftp) throws SftpException {
        String linkTarget = null;
        if (attrs.isLink()) {
            String path = dirName + '/' + baseName;
            LOG.log(Level.FINE, "performing readlink {0}", path);
            try {
                linkTarget = cftp.readlink(path);
            } catch (SftpException e) {
                throw decorateSftpException(e, path);
            }
        }
        Date lastModified = new Date(attrs.getMTime()*1000L);
        StatInfo result = new FileInfoProvider.StatInfo(baseName, attrs.getUId(), attrs.getGId(), attrs.getSize(), 
                attrs.isDir(), attrs.isLink(), linkTarget, attrs.getPermissions(), lastModified);
        return result;
    }
    
    /*package*/ Future<StatInfo> stat(String absPath, Writer error) {
        StatLoader loader = new StatLoader(absPath);
        FutureTask<StatInfo> ftask = new FutureTask<StatInfo>(loader);
        getReadRequestProcessor().post(ftask);
        LOG.log(Level.FINE, "{0} schedulled", loader.getTraceName());
        return ftask;
    }

    /*package*/ Future<StatInfo[]> ls(String absPath, Writer error) {
        LsLoader loader = new LsLoader(absPath);
        FutureTask<StatInfo[]> ftask = new FutureTask<StatInfo[]>(loader);
        getReadRequestProcessor().post(ftask);
        LOG.log(Level.FINE, "{0} schedulled", loader.getTraceName());
        return ftask;
    }
    
    /*package*/ static void testSetConcurrencyLevel(int level) {
        boolean hadInstances;
        synchronized (instancesLock) {
            hadInstances = ! instances.isEmpty();
            instances.clear();
        }
        CONCURRENCY_LEVEL = level;
        if (hadInstances) {
            System.err.printf("Warning: SFTP concurrency level was set while there were some %s instances\n", SftpSupport.class.getSimpleName());
        }
    }
    
    /*package*/ int getMaxBusyChannels() {
        synchronized (channelLock) {
            return maxBusyChannels;
        }
    }
}
