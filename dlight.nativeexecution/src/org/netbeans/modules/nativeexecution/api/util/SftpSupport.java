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
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Writer;
import java.net.ConnectException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.Md5checker.Result;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

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

    private static SftpSupport getInstance(ExecutionEnvironment execEnv) {
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

    static Future<Integer> uploadFile(
            final String srcFileName,
            final ExecutionEnvironment execEnv,
            final String dstFileName,
            final int mask, final Writer error, final boolean checkMd5) {
        return getInstance(execEnv).uploadFile(srcFileName, dstFileName, mask, error, checkMd5);
    }

    static Future<Integer> downloadFile(
            final String srcFileName,
            final ExecutionEnvironment execEnv,
            final String dstFileName,
            final Writer error) {
        return getInstance(execEnv).downloadFile(srcFileName, dstFileName, error);
    }
    //
    // Instance stuff
    //
    private final ExecutionEnvironment execEnv;

    //private final RequestProcessor requestProcessor;
    // Trying to work around #184068 -  Instable remote unit tests failure
    private static final RequestProcessor requestProcessor = new RequestProcessor("SFTP request processor"); // NOI18N;

    // its's ok to hav a single one since we have only single-threaded request processor
    private ChannelSftp channel;
    private final Object channelLock = new Object();

    private SftpSupport(ExecutionEnvironment execEnv) {
        this.execEnv = execEnv;
        // we've got some sftp issues => only 1 task at a moment
        // make it statis: workaround for #184068 -  Instable remote unit tests failure
        // requestProcessor = new RequestProcessor("SFTP request processor for " + execEnv, 1); // NOI18N
    }

    private ChannelSftp getChannel() throws IOException, CancellationException, JSchException, ExecutionException {
        synchronized (channelLock) {
            if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                channel = null;
                ConnectionManager.getInstance().connectTo(execEnv);
            }
            if (channel != null && !channel.isConnected()) {
                channel = null;
            }
            if (channel == null) {
                ConnectionManagerAccessor cmAccess = ConnectionManagerAccessor.getDefault();
                if (cmAccess == null) { // is it a paranoja?
                    throw new ExecutionException("Error getting ConnectionManagerAccessor", new NullPointerException()); //NOI18N
                }
                Session session = cmAccess.getConnectionSession(execEnv, true);
                if (session == null) {
                    throw new ExecutionException("Error getting connection session", new NullPointerException()); //NOI18N
                }
                channel = (ChannelSftp) session.openChannel("sftp"); // NOI18N
                channel.connect();
            }
        }
        return channel;
    }

    private abstract class Worker implements Callable<Integer> {

        protected final String srcFileName;
        protected final ExecutionEnvironment execEnv;
        protected final String dstFileName;
        protected final Writer error;

        public Worker(String srcFileName, ExecutionEnvironment execEnv, String dstFileName, Writer error) {
            this.srcFileName = srcFileName;
            this.execEnv = execEnv;
            this.dstFileName = dstFileName;
            this.error = error;
        }

        protected abstract void work() throws JSchException, SftpException, IOException, CancellationException, InterruptedException, ExecutionException;

        protected abstract String getTraceName();

        @Override
        public Integer call() throws Exception {
            int rc = -1;
            try {                
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
                logException(ex);
                rc = 4;
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

    private class Uploader extends Worker implements Callable<Integer> {

        private final int mask;
        private final boolean checkMd5;

        public Uploader(String srcFileName, ExecutionEnvironment execEnv, String dstFileName, int mask, Writer error, boolean checkMd5) {
            super(srcFileName, execEnv, dstFileName, error);
            this.mask = mask;
            this.checkMd5 = checkMd5;
        }

        @Override
        protected void work() throws IOException, CancellationException, JSchException, SftpException, InterruptedException, ExecutionException {
            boolean checkDir = false;
            if (checkMd5) {
                LOG.log(Level.FINE, "Md5 check for {0}:{1} started", new Object[]{execEnv, dstFileName});
                Result res = null;
                try {
                    res = new Md5checker(execEnv).check(new File(srcFileName), dstFileName);
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getInstance().log(Level.WARNING, "Can not perform md5 check for {0}: {1}", new Object[]{execEnv.getDisplayName(), ex.getMessage()});
                    if (HostInfoUtils.fileExists(execEnv, dstFileName)) {
                        res = Md5checker.Result.UPTODATE;
                    } else {
                        res = Md5checker.Result.INEXISTENT;
                    }
                } catch (Md5checker.CheckSumException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
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
            if (checkDir) {
                int slashPos = dstFileName.lastIndexOf('/'); //NOI18N
                if (slashPos >= 0) {
                    String remoteDir = dstFileName.substring(0, slashPos);
                    CommonTasksSupport.mkDir(execEnv, remoteDir, error).get();
                }
            }
            put(cftp);
            cftp.chmod(mask, dstFileName);
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
                        throw e;
                    } else {
                        String message = String.format("Error on attempt %d to copy %s to %s:%s :\n", // NOI18N
                                attempt, srcFileName, execEnv, dstFileName);
                        LOG.log(Level.FINE, message);
                        if (attempt == 2) {
                            Logger.fullThreadDump(message);
                        }
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        protected String getTraceName() {
            return "Uploading " + srcFileName + " to " + execEnv + ":" + dstFileName; // NOI18N
        }
    }

    private class Downloader extends Worker implements Callable<Integer> {

        public Downloader(String srcFileName, ExecutionEnvironment execEnv, String dstFileName, Writer error) {
            super(srcFileName, execEnv, dstFileName, error);
        }

        @Override
        protected void work() throws IOException, CancellationException, JSchException, SftpException, ExecutionException {
            LOG.log(Level.FINE, "{0} started", getTraceName());
            ChannelSftp cftp = getChannel();
            cftp.get(srcFileName, dstFileName);
        }

        @Override
        protected String getTraceName() {
            return "Downloading " + execEnv + ":" + srcFileName + " to " + dstFileName; // NOI18N
        }
    }

    private Future<Integer> uploadFile(
            final String srcFileName,
            final String dstFileName,
            final int mask, final Writer error, final boolean checkMd5) {

        Uploader uploader = new Uploader(srcFileName, execEnv, dstFileName, mask, error, checkMd5);
        FutureTask<Integer> ftask = new FutureTask<Integer>(uploader);
        requestProcessor.post(ftask);
        LOG.log(Level.FINE, "{0} schedulled", uploader.getTraceName());
        return ftask;
    }

    private Future<Integer> downloadFile(
            final String srcFileName,
            final String dstFileName,
            final Writer error) {

        Downloader downloader = new Downloader(srcFileName, execEnv, dstFileName, error);
        FutureTask<Integer> ftask = new FutureTask<Integer>(downloader);
        requestProcessor.post(ftask);
        LOG.log(Level.FINE, "{0} schedulled", downloader.getTraceName());
        return ftask;
    }
}
