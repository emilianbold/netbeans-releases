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

package org.netbeans.modules.cnd.remote.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandler;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.Env;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;

/**
 *
 * @author Vladimir Kvashin
 */
/* package-local */
class RemoteBuildProjectActionHandler implements ProjectActionHandler {

    private ProjectActionHandler delegate;
    private ProjectActionEvent pae;
    private ProjectActionEvent[] paes;
    private ExecutionEnvironment execEnv;
    private static final boolean allAtOnce = false;

    private File localDir;
    private PrintWriter out;
    private PrintWriter err;
    private File privProjectStorageDir;
    private String remoteDir;

    private NativeProcess remoteControllerProcess = null;

    /* package-local */
    RemoteBuildProjectActionHandler() {
    }
    
    @Override
    public void init(ProjectActionEvent pae, ProjectActionEvent[] paes) {
        this.pae = pae;
        this.paes = paes;
        this.delegate = RemoteBuildProjectActionHandlerFactory.createDelegateHandler(pae);
        this.delegate.init(pae, paes);
        this.execEnv = pae.getConfiguration().getDevelopmentHost().getExecutionEnvironment();
    }

    private void initRfsIfNeed() throws IOException, InterruptedException, ExecutionException {
        if (RfsSyncFactory.ENABLE_RFS) {            
            if (execEnv.isRemote()) {
                if (ServerList.get(execEnv).getSyncFactory().getID().equals(RfsSyncFactory.ID)) {
                    // FIXUP: this should be done via ActionHandler.
                    RfsSyncWorker.Parameters params = RfsSyncWorker.getLastParameters();
                    assert params != null; // FIXUP: it's impossible because of the check above
                    this.localDir = params.localDir;
                    this.remoteDir = params.remoteDir;
                    this.out = params.out;
                    this.err = params.err;
                    this.privProjectStorageDir = params.privProjectStorageDir;
                    initRfs();
                }
            }
        }
    }

    private void remoteControllerCleanup() {
        // nobody calls this concurrently => no synchronization
        if (remoteControllerProcess != null) {
            remoteControllerProcess.destroy();
            remoteControllerProcess = null;
        }
    }

    private void initRfs() throws IOException, InterruptedException, ExecutionException {

        final Env env = pae.getProfile().getEnvironment();

        String remoteControllerPath = RfsSetupProvider.getController(execEnv);
        if (remoteControllerPath == null) {
            return;
        }

        NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(execEnv);
        // nobody calls this concurrently => no synchronization
        remoteControllerCleanup(); // just in case
        pb.setExecutable(remoteControllerPath); //I18N
        remoteControllerProcess = pb.call();

        RequestProcessor.getDefault().post(new ErrorReader(remoteControllerProcess.getErrorStream(), err));

        final InputStream rcStream = remoteControllerProcess.getInputStream();
        LocalController localController = new LocalController(
                execEnv, localDir,  remoteDir, rcStream,
                remoteControllerProcess.getOutputStream(), err);
        // read port
        String line = new BufferedReader(new InputStreamReader(rcStream)).readLine();
        String port;
        if (line != null && line.startsWith("PORT ")) { // NOI18N
            port = line.substring(5);
        } else if (line == null) {
            int rc = remoteControllerProcess.waitFor();
            throw new ExecutionException(String.format("Remote controller failed; rc=%d\n", rc), null);
        } else {
            String message = String.format("Protocol error: read \"%s\" expected \"%s\"\n", line,  "PORT <port-number>"); //NOI18N
            System.err.printf(message); // NOI18N
            remoteControllerProcess.destroy();
            throw new ExecutionException(message, null); //NOI18N
        }
        RemoteUtil.LOGGER.fine("Remote Controller listens port " + port); // NOI18N
        RequestProcessor.getDefault().post(localController);


        String preload = RfsSetupProvider.getPreload(execEnv);
        assert preload != null;
        env.putenv("LD_PRELOAD", preload); // NOI18N
        env.putenv("RFS_CONTROLLER_DIR", remoteDir); // NOI18N
        env.putenv("RFS_CONTROLLER_PORT", port); // NOI18N
        
        String preloadLog = System.getProperty("cnd.remote.fs.preload.log");
        if (preloadLog != null) {
            env.putenv("RFS_PRELOAD_LOG", preloadLog); // NOI18N
        }
        String controllerLog = System.getProperty("cnd.remote.fs.controller.log");
        if (controllerLog != null) {
            env.putenv("RFS_CONTROLLER_LOG", controllerLog); // NOI18N
        }
        
        delegate.addExecutionListener(new ExecutionListener() {
            public void executionStarted(int pid) {
                RemoteUtil.LOGGER.fine("RemoteBuildProjectActionHandler: build started; PID=" + pid);
            }
            public void executionFinished(int rc) {
                RemoteUtil.LOGGER.fine("RemoteBuildProjectActionHandler: build finished; RC=" + rc);
                shutdownRfs();
            }
        });
    }

    private void shutdownRfs() {
        remoteControllerCleanup();
    }

    @Override
    public void addExecutionListener(ExecutionListener l) {
        delegate.addExecutionListener(l);
    }

    @Override
    public void removeExecutionListener(ExecutionListener l) {
        delegate.removeExecutionListener(l);
    }

    @Override
    public boolean canCancel() {
        return delegate.canCancel();
    }

    @Override
    public void cancel() {
        delegate.cancel();
    }

    @Override
    public void execute(InputOutput io) {
        try {
            initRfsIfNeed();
            delegate.execute(io);
        } catch (InterruptedException ex) {
            // reporting does not make sense, just return false
            RemoteUtil.LOGGER.finest(ex.getMessage());
        } catch (InterruptedIOException ex) {
            // reporting does not make sense, just return false
            RemoteUtil.LOGGER.finest(ex.getMessage());
        } catch (IOException ex) {
            RemoteUtil.LOGGER.log(Level.FINE, null, ex);
            if (err != null) {
                err.printf("%s\n", NbBundle.getMessage(getClass(), "MSG_Error_Copying",
                        remoteDir, ServerList.get(execEnv).toString(), ex.getLocalizedMessage()));
            }
        } catch (ExecutionException ex) {
            RemoteUtil.LOGGER.log(Level.FINE, null, ex);
            if (err != null) {
                String message = NbBundle.getMessage(getClass(), "MSG_Error_Copying",
                        remoteDir, ServerList.get(execEnv).toString(), ex.getLocalizedMessage());
                io.getErr().printf("%s\n", message);
                io.getErr().printf("%s\n", NbBundle.getMessage(getClass(), "MSG_Build_Failed"));
                err.printf("%s\n", message);
            }
        }
    }

    private static class ErrorReader implements Runnable {

        private final BufferedReader errorReader;
        private final PrintWriter errorWriter;

        public ErrorReader(InputStream errorStream, PrintWriter errorWriter) {
            this.errorReader = new BufferedReader(new InputStreamReader(errorStream));
            this.errorWriter = errorWriter;
        }
        public void run() {
            try {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    if (errorWriter != null) {
                         errorWriter.println(line);
                    }
                    RemoteUtil.LOGGER.fine(line);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static class LocalController implements Runnable {

        private final BufferedReader requestReader;
        private final PrintStream responseStream;
        private final String remoteDir;
        private final File localDir;
        private final ExecutionEnvironment execEnv;
        private final PrintWriter err;

        private static final Logger logger = Logger.getLogger("cnd.remote.logger"); //NOI18N

        private final Set<String> processedFiles = new HashSet<String>();

        public LocalController(ExecutionEnvironment executionEnvironment,
                File localDir, String remoteDir,
                InputStream requestStream, OutputStream responseStream,
                PrintWriter err) {
            this.execEnv = executionEnvironment;
            this.localDir = localDir;
            this.remoteDir = remoteDir;
            this.requestReader = new BufferedReader(new InputStreamReader(requestStream));
            this.responseStream = new PrintStream(responseStream);
            this.err = err;
        }

        private void respond_ok() {
            responseStream.printf("1\n"); // NOI18N
            responseStream.flush();
        }
        private void respond_err(String tail) {
            responseStream.printf("0 %s\n", tail); // NOI18N
            responseStream.flush();
        }

        public void run() {
            long totalCopyingTime = 0;
            while (true) {
                try {
                    String request = requestReader.readLine();
                    String remoteFile = request;
                    logger.finest("LC: REQ " + request);
                    if (request == null) {
                        break;
                    }
                    if (processedFiles.contains(remoteFile)) {
                        logger.info("RC asks for file " + remoteFile + " again?!");
                        respond_ok();
                        continue;
                    } else {
                        processedFiles.add(remoteFile);
                    }
                    if (remoteFile.startsWith(remoteDir)) {
                        File localFile =  new File(localDir, remoteFile.substring(remoteDir.length()));
                        if (localFile.exists() && !localFile.isDirectory() && !allAtOnce) {
                            logger.finest("LC: uploading " + localFile + " to " + remoteFile + " started");
                            long fileTime = System.currentTimeMillis();
                            Future<Integer> task = CommonTasksSupport.uploadFile(localFile.getAbsolutePath(),
                                    execEnv, remoteFile, 0777, err);
                            try {
                                int rc = task.get();
                                fileTime = System.currentTimeMillis() - fileTime;
                                totalCopyingTime += fileTime;
                                System.err.printf("LC: uploading %s to %s finished; rc=%d time =%d total time = %d ms \n",
                                        localFile, remoteFile, rc, fileTime, totalCopyingTime);
                                if (rc == 0) {
                                    respond_ok();
                                } else {
                                    respond_err("1"); // NOI18N
                                }
                            } catch (InterruptedException ex) {
                                Exceptions.printStackTrace(ex);
                                break;
                            } catch (ExecutionException ex) {
                                Exceptions.printStackTrace(ex);
                                respond_err("2 execution exception\n"); // NOI18N
                            } finally {
                                responseStream.flush();
                            }
                        } else {
                            respond_ok();
                        }
                    } else {
                        respond_ok();
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex); //TODO: error processing
                }
            }
        }

    }

}
