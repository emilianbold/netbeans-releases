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
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.remote.support.RemoteException;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Vladimir Kvashin
 */
/*package*/ final class RfsSyncWorker extends BaseSyncWorker implements RemoteSyncWorker {

    private NativeProcess remoteControllerProcess;
    private RfsLocalController localController;
    private String remoteDir;

    public RfsSyncWorker(ExecutionEnvironment executionEnvironment, PrintWriter out, PrintWriter err, File privProjectStorageDir, File... files) {
        super(executionEnvironment, out, err, privProjectStorageDir, files);
    }

    @Override
    public boolean startup(Map<String, String> env2add) {
        RemotePathMap mapper = RemotePathMap.getPathMap(executionEnvironment);
        remoteDir = mapper.getRemotePath("/", false); // NOI18N
        if (remoteDir == null) {
            if (err != null) {
                err.printf("%s\n", NbBundle.getMessage(getClass(), "MSG_Cant_find_sync_root", ServerList.get(executionEnvironment).toString()));
            }
            return false; // TODO: error processing
        }

        boolean success = false;
        try {
            if (out != null) {
                out.printf("%s\n", NbBundle.getMessage(getClass(), "MSG_Copying",
                        remoteDir, ServerList.get(executionEnvironment).toString()));
            }
            Future<Integer> mkDir = CommonTasksSupport.mkDir(executionEnvironment, remoteDir, err);
            if (mkDir.get() != 0) {
                throw new IOException("Can not create directory " + remoteDir); //NOI18N
            }
            startupImpl(env2add);
            success = true;
        } catch (RemoteException ex) {
            printErr(ex);
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
                        remoteDir, ServerList.get(executionEnvironment).toString(), ex.getLocalizedMessage()));
            }
        } catch (IOException ex) {
            RemoteUtil.LOGGER.log(Level.FINE, null, ex);
            if (err != null) {
                err.printf("%s\n", NbBundle.getMessage(getClass(), "MSG_Error_Copying",
                        remoteDir, ServerList.get(executionEnvironment).toString(), ex.getLocalizedMessage()));
            }
        }
        return success;
    }

    private void printErr(Exception ex) throws MissingResourceException {
        RemoteUtil.LOGGER.finest(ex.getMessage());
        if (err != null) {
            String message = NbBundle.getMessage(getClass(), "MSG_Error_Copying", remoteDir, ServerList.get(executionEnvironment).toString(), ex.getLocalizedMessage());
            err.printf("%s\n", message); // NOI18N
            err.printf("%s\n", NbBundle.getMessage(getClass(), "MSG_Build_Failed"));
            err.printf("%s\n", message); // NOI18N
        }
    }

    private void startupImpl(Map<String, String> env2add) throws IOException, InterruptedException, ExecutionException, RemoteException {
        String remoteControllerPath;
        String ldLibraryPath;
        try {
            remoteControllerPath = RfsSetupProvider.getControllerPath(executionEnvironment);
            CndUtils.assertTrue(remoteControllerPath != null);
            ldLibraryPath = RfsSetupProvider.getLdLibraryPath(executionEnvironment);
            CndUtils.assertTrue(ldLibraryPath != null);
        } catch (ParseException ex) {
            throw new ExecutionException(ex);
        }

        NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(executionEnvironment);
        // nobody calls this concurrently => no synchronization
        remoteControllerCleanup(); // just in case
        pb.setExecutable(remoteControllerPath); //I18N
        pb.setWorkingDirectory(remoteDir);
        String rfsTrace = System.getProperty("cnd.rfs.controller.trace");
        if (rfsTrace != null) {
            pb.getEnvironment().put("RFS_CONTROLLER_TRACE", rfsTrace); // NOI18N
        }
        remoteControllerProcess = pb.call();

        RequestProcessor.getDefault().post(new ErrorReader(remoteControllerProcess.getErrorStream(), err));

        final InputStream rcInputStream = remoteControllerProcess.getInputStream();
        final OutputStream rcOutputStream = remoteControllerProcess.getOutputStream();
        final BufferedReader rcInputStreamReader = ProcessUtils.getReader(rcInputStream, executionEnvironment.isRemote());
        final PrintWriter rcOutputStreamWriter = ProcessUtils.getWriter(rcOutputStream, executionEnvironment.isRemote());
        localController = new RfsLocalController(
                executionEnvironment, files, rcInputStreamReader,
                rcOutputStreamWriter, err, privProjectStorageDir);

        localController.feedFiles(new SharabilityFilter());

        // read port
        String line = rcInputStreamReader.readLine();
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
        RemoteUtil.LOGGER.log(Level.FINE, "Remote Controller listens port {0}", port); // NOI18N
        RequestProcessor.getDefault().post(localController);

        String preload = RfsSetupProvider.getPreloadName(executionEnvironment);
        CndUtils.assertTrue(preload != null);
        // to be able to trace what we're doing, first put it all to a map

        //Alas, this won't work
        //MacroMap mm = MacroMap.forExecEnv(executionEnvironment);
        //mm.prependPathVariable("LD_LIBRARY_PATH", ldLibraryPath);
        //mm.prependPathVariable("LD_PRELOAD", preload); // NOI18N

        env2add.put("LD_PRELOAD", preload); // NOI18N
        String ldLibPathVar = "LD_LIBRARY_PATH"; // NOI18N
        String oldLdLibPath = RemoteUtil.getEnv(executionEnvironment, ldLibPathVar);
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
        addRemoteEnv(env2add, "cnd.rfs.preload.trace", "RFS_PRELOAD_TRACE"); // NOI18N

        RemoteUtil.LOGGER.fine("Setting environment:");
    }

    private void addRemoteEnv(Map<String, String> env2add, String localJavaPropertyName, String remoteEnvVarName) {
        String value = System.getProperty(localJavaPropertyName, null);
        if (value != null) {
            env2add.put(remoteEnvVarName, value);
        }
    }

    @Override
    public void shutdown() {
        remoteControllerCleanup();
        localControllerCleanup();
    }

    @Override
    public boolean cancel() {
        return false;
    }

    private void localControllerCleanup() {
        RfsLocalController lc;
        synchronized (this) {
            lc = localController;
            localController = null;
        }
        if (lc != null) {
            lc.shutdown();
        }
    }
    
    private void remoteControllerCleanup() {
        NativeProcess rc;
        synchronized (this) {
            rc = remoteControllerProcess;
            remoteControllerProcess = null;
        }
        if (rc != null) {
            rc.destroy();
            rc = null;
        }
    }


    private static class ErrorReader implements Runnable {

        private final BufferedReader errorReader;
        private final PrintWriter errorWriter;

        public ErrorReader(InputStream errorStream, PrintWriter errorWriter) {
            this.errorReader = new BufferedReader(new InputStreamReader(errorStream));
            this.errorWriter = errorWriter;
        }
        @Override
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
