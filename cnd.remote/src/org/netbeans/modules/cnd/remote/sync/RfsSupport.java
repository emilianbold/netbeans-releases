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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.Env;
import org.netbeans.modules.cnd.remote.support.RemoteException;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;

/**
 *
 * @author Vladimir Kvashin
 */
/*package-local*/ class RfsSupport implements ExecutionListener {

    private final ProjectActionEvent pae;
    private final ExecutionEnvironment execEnv;

    private File[] localDirs;
    private PrintWriter out;
    private PrintWriter err;
    private File privProjectStorageDir;
    private String remoteDir;

    private NativeProcess remoteControllerProcess;
    private RfsLocalController localController;


    public RfsSupport(ProjectActionEvent pae) {
        this.pae = pae;
        this.execEnv = pae.getConfiguration().getDevelopmentHost().getExecutionEnvironment();
    }

    /*package-local*/ void setIO(InputOutput io) {
        err = (io == null) ? null : io.getErr();
        out = (io == null) ? null : io.getOut();
    }

    /*package-local*/ void initRfsIfNeed() throws IOException, InterruptedException, ExecutionException, RemoteException {
        if (RfsSyncFactory.ENABLE_RFS) {
            if (execEnv.isRemote()) {
                if (ServerList.get(execEnv).getSyncFactory().getID().equals(RfsSyncFactory.ID)) {
                    // FIXUP: this should be done via ActionHandler.
                    RfsSyncWorker.Parameters params = RfsSyncWorker.getLastParameters();
                    assert params != null; // FIXUP: it's impossible because of the check above
                    this.localDirs = params.localDirs;
                    this.remoteDir = params.remoteDir;
                    //this.out = params.out;
                    //this.err = params.err;
                    this.privProjectStorageDir = params.privProjectStorageDir;
                    initRfs();
                }
            }
        }
    }

    private void shutdownRfs() {
        remoteControllerCleanup();
        if (localController != null) {
            localController.shutdown();
        }
    }


    private synchronized void remoteControllerCleanup() {
        // nobody calls this concurrently => no synchronization
        if (remoteControllerProcess != null) {
            remoteControllerProcess.destroy();
            remoteControllerProcess = null;
        }
    }

    private void initRfs() throws IOException, InterruptedException, ExecutionException, RemoteException, org.netbeans.modules.cnd.remote.support.RemoteException {
        String remoteControllerPath;
        String ldLibraryPath;
        try {
            remoteControllerPath = RfsSetupProvider.getControllerPath(execEnv);
            CndUtils.assertTrue(remoteControllerPath != null);
            ldLibraryPath = RfsSetupProvider.getLdLibraryPath(execEnv);
            CndUtils.assertTrue(ldLibraryPath != null);
        } catch (ParseException ex) {
            throw new ExecutionException(ex);
        }

        NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(execEnv);
        // nobody calls this concurrently => no synchronization
        remoteControllerCleanup(); // just in case
        pb.setExecutable(remoteControllerPath); //I18N
        pb.setWorkingDirectory(remoteDir);
        remoteControllerProcess = pb.call();

        RequestProcessor.getDefault().post(new ErrorReader(remoteControllerProcess.getErrorStream(), err));

        final InputStream rcInputStream = remoteControllerProcess.getInputStream();
        final OutputStream rcOutputStream = remoteControllerProcess.getOutputStream();
        localController = new RfsLocalController(
                execEnv, localDirs,  remoteDir, rcInputStream,
                rcOutputStream, err, new FileData(privProjectStorageDir, execEnv));

        localController.feedFiles(rcOutputStream, new SharabilityFilter());

        //try { rcOutputStream.flush(); Thread.sleep(10000); } catch (InterruptedException e) {}

        // read port
        String line = new BufferedReader(new InputStreamReader(rcInputStream)).readLine();
        String port;
        if (line != null && line.startsWith("PORT ")) { // NOI18N
            port = line.substring(5);
        } else if (line == null) {
            int rc = remoteControllerProcess.waitFor();
            throw new ExecutionException(String.format("Remote controller failed; rc=%d\n", rc), null); // NOI18N
        } else {
            String message = String.format("Protocol error: read \"%s\" expected \"%s\"\n", line,  "PORT <port-number>"); //NOI18N
            System.err.printf(message); // NOI18N
            remoteControllerProcess.destroy();
            throw new ExecutionException(message, null); //NOI18N
        }
        RemoteUtil.LOGGER.fine("Remote Controller listens port " + port); // NOI18N
        RequestProcessor.getDefault().post(localController);

        String preload = RfsSetupProvider.getPreloadName(execEnv);
        CndUtils.assertTrue(preload != null);
        // to be able to trace what we're doing, first put it all to a map
        Map<String, String> env2add = new HashMap<String, String>();

        //Alas, this won't work
        //MacroMap mm = MacroMap.forExecEnv(execEnv);
        //mm.prependPathVariable("LD_LIBRARY_PATH", ldLibraryPath);
        //mm.prependPathVariable("LD_PRELOAD", preload); // NOI18N

        env2add.put("LD_PRELOAD", preload); // NOI18N
        String ldLibPathVar = "LD_LIBRARY_PATH"; // NOI18N
        String oldLdLibPath = RemoteUtil.getEnv(execEnv, ldLibPathVar);
        if (oldLdLibPath != null) {
            ldLibraryPath += ":" + oldLdLibPath; // NOI18N
        }
        env2add.put(ldLibPathVar, ldLibraryPath); // NOI18N
        env2add.put("RFS_CONTROLLER_DIR", remoteDir); // NOI18N
        env2add.put("RFS_CONTROLLER_PORT", port); // NOI18N

        addRemoteEnv(env2add, "cnd.rfs.preload.sleep", "RFS_PRELOAD_SLEEP"); // NOI18N
        addRemoteEnv(env2add, "cnd.rfs.preload.log", "RFS_PRELOAD_LOG"); // NOI18N
        addRemoteEnv(env2add, "cnd.rfs.controller.log", "RFS_CONTROLLER_LOG"); // NOI18N
        addRemoteEnv(env2add, "cnd.rfs.controller.port", "RFS_CONTROLLER_PORT"); // NOI18N
        addRemoteEnv(env2add, "cnd.rfs.controller.host", "RFS_CONTROLLER_HOST"); // NOI18N

        RemoteUtil.LOGGER.fine("Setting environment:");
        Env env = pae.getProfile().getEnvironment();
        for (Map.Entry<String, String> entry : env2add.entrySet()) {
            if (RemoteUtil.LOGGER.isLoggable(Level.FINE)) {
                RemoteUtil.LOGGER.fine(String.format("\t%s=%s", entry.getKey(), entry.getValue()));
            }
            env.putenv(entry.getKey(), entry.getValue());
        }
    }

    public void executionStarted(int pid) {
        RemoteUtil.LOGGER.fine("RemoteBuildProjectActionHandler: build started; PID=" + pid);
    }

    public void executionFinished(int rc) {
        RemoteUtil.LOGGER.fine("RemoteBuildProjectActionHandler: build finished; RC=" + rc);
        shutdownRfs();
    }
    
    private void addRemoteEnv(Map<String, String> env2add, String localJavaPropertyName, String remoteEnvVarName) {
        String value = System.getProperty(localJavaPropertyName, null);
        if (value != null) {
            env2add.put(remoteEnvVarName, value);
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
}
