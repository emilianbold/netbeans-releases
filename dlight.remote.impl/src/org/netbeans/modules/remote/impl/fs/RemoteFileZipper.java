/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.impl.fs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.openide.util.RequestProcessor;

/**
 */
public class RemoteFileZipper {

    private final ExecutionEnvironment execEnv;
    private final RemoteFileSystem fileSystem;
    private final RequestProcessor rp;
    
    private final Map<String, Worker> workers = new HashMap<>();
    private final Object workersLock = new Object();
    
    public RemoteFileZipper(ExecutionEnvironment execEnv, RemoteFileSystem fileSystem) {
        this.execEnv = execEnv;
        this.fileSystem = fileSystem;
        // throughput is set to 1 to prevent making all channels busy for a long time
        rp = new RequestProcessor(getClass().getSimpleName() + ' ' + execEnv, 1); //NOI18N
    }

    public void schedule(File zipFile, File zipPartFile, String path, Collection<String> extensions) {
        synchronized (workersLock) {
            Worker worker = workers.get(path);
            if (worker == null) {
                worker = new Worker(zipFile, zipPartFile, path, extensions);
                workers.put(path, worker);
                rp.post(worker);
            } else {
                // TODO: consider whatto do
            }
        }
    }

    private class Worker implements Runnable {

        private final File zipFile;
        private final File zipPartFile;
        private final String path;
        private final Collection<String> extensions;

        public Worker(File zipFile, File zipPartFile, String path, Collection<String> extensions) {
            this.zipFile = zipFile;
            this.zipPartFile = zipPartFile;
            this.path = path;
            this.extensions = extensions;
        }

        @Override
        public void run() {
            if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                return;
            }
            String oldName = Thread.currentThread().getName();
            Thread.currentThread().setName(RemoteFileZipper.class.getSimpleName() + 
                    ' ' + execEnv + ": zipping " + path); //NOI18N
            try {                
                zip();
            } finally {
                Thread.currentThread().setName(oldName);
                synchronized (workersLock) {
                    workers.remove(path);
                }
            }            
        }

        private void zip() {

            long time;
            
            //
            // Zip directory on remote host
            //
            time = System.currentTimeMillis();
            StringBuilder script = new StringBuilder("TZ=UTC; export TZ; F=`mktemp /tmp/rfs_warmup_XXXXXXXX.zip`; if [ $? -eq 0 ]; then echo ZIP=$F; rm -rf $F; "); //NOI18N
            boolean all;
            if (extensions == null || extensions.isEmpty()) {
                all = true;
            } else {
                String next = extensions.iterator().next();
                all = (next == null) || next.equals("*"); // NOI18N
            }
            
            if (all) {
                script.append("zip -rq $F \"").append(path).append("\""); // NOI18N
            } else {
                script.append("find ").append(path); // NOI18N
                boolean first = true;
                for (String ext : extensions) {
                    if (first) {
                        first = false;
                    } else {
                        script. append(" -o "); // NOI18N
                    }
                    script. append(" -name \"*.").append(ext).append("\""); // NOI18N
                }
                script.append(" | xargs zip -rq $F "); // NOI18N
            }            
            script.append("; echo RC=$?; fi"); //NOI18N
            NativeProcessBuilder processBuilder = NativeProcessBuilder.newProcessBuilder(execEnv);
            processBuilder.setExecutable("sh").setWorkingDirectory("/").setArguments("-c", script.toString()); //NOI18N
            NativeProcess process = null;
            try {
                process = processBuilder.call();
            } catch (IOException ex) {
                RemoteLogger.fine(ex);
                RemoteLogger.info("Warmup: error zipping {0}:{1} can't launch remote process", //NOI18N
                        execEnv, path);
                return;                
            }
            Future<List<String>> stderrFuture = ProcessUtils.readProcessErrorAsync(process);
            String remoteZipPath = null;
            int rc = -1;
            // Output should be like the following:
            // ZIP=/tmp/tmp.xLDawcYe5M
            // RC=0
            try (BufferedReader rdr = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = rdr.readLine()) != null) {
                    if (remoteZipPath == null) {
                        if (line.startsWith("ZIP=")) { //NOI18N
                            remoteZipPath = line.substring(4);
                            fileSystem.deleteOnDisconnect(remoteZipPath);
                        } else { // NOI18N
                            RemoteLogger.info("Warmup: error zipping {0} at {1}: unexpected output: {2}", //NOI18N
                                    path, execEnv, line);
                            // don't return here, read the rest of the output first
                        }                        
                    } else if (line.startsWith("RC=")) { //NOI18N
                        try {
                            rc = Integer.parseInt(line.substring(3));
                        } catch (NumberFormatException ex) {
                            RemoteLogger.info("Warmup: error zipping {0} at {1}: unexpected output: {2}", //NOI18N
                                    path, execEnv, line);
                            // don't return here, read the rest of the output first
}                            
                    }
                }
            } catch (IOException ex) {
                RemoteLogger.fine(ex);
                RemoteLogger.info("Warmup: error zipping {0}:{1} error reading script output: {2}", //NOI18N
                        execEnv, path, ex.getLocalizedMessage());
            }
            if (rc == -1 || remoteZipPath == null) { 
                // unexpected process output or its absence
                // error message should be printed by the code above
                return;
            }

            int shrc = -1;
            List<String> stderr = Collections.emptyList();
            try {
                shrc = process.waitFor();
                stderr = stderrFuture.get();
            } catch (InterruptedException | ExecutionException ex) {
                RemoteLogger.fine(ex);
                RemoteLogger.info("Warmup: error zipping {0}:{1} {2} {3}", //NOI18N
                        execEnv, path, ex.getClass().getSimpleName(), ex.getLocalizedMessage());
                return;
            }

            if (shrc != 0 || rc != 0) {
                RemoteLogger.info("Warmup: error zipping {0}:{1} {2}", //NOI18N
                        execEnv, path, merge(stderr));
                return;
            }
            
            RemoteLogger.fine("zipping {0} at {1} took {2}", //NOI18N
                    path, execEnv, System.currentTimeMillis() - time);

            try {
                //
                // Download zip from remote host
                //
                time = System.currentTimeMillis();
                zipPartFile.getParentFile().mkdirs();
                Future<Integer> task = CommonTasksSupport.downloadFile(remoteZipPath, execEnv,
                        zipPartFile.getAbsolutePath(), new PrintWriter(System.err));

                rc = -1;
                try {
                    rc = task.get();
                } catch (InterruptedException ex) {
                    // nothing
                } catch (ExecutionException ex) {
                    ex.printStackTrace(System.err);
                }
                if (rc != 0) {
                    if (rc != 0) {
                        RemoteLogger.info("Warmup: error downloading {0}:{1} to {2}, rc={3}", //NOI18N
                                execEnv, remoteZipPath, zipPartFile.getAbsolutePath(), rc);
                        return;
                    }
                }
                RemoteLogger.fine("downloading {0}:{1} to {2} took {3} ms", //NOI18N
                        execEnv, path, zipPartFile.getAbsolutePath(), System.currentTimeMillis() - time);
                
                if (!zipPartFile.renameTo(zipFile)) {
                    RemoteLogger.info("Warmup: error renaming {0} to {1}",  //NOI18N
                            zipPartFile.getAbsolutePath(), zipFile.getAbsolutePath());
                }
            } finally {
                // Remove temp. zip file from remote host
                time = System.currentTimeMillis();
                Future<Integer> task = CommonTasksSupport.rmFile(execEnv, remoteZipPath, new PrintWriter(System.err));
                rc = -1;
                try {
                    rc = task.get();
                } catch (InterruptedException ex) {
                    // nothing
                } catch (ExecutionException ex) {
                    ex.printStackTrace(System.err);
                }
                RemoteLogger.fine("removing {0} at {1} finished with rc={2} and took {3} ms", //NOI18N
                        remoteZipPath, execEnv, rc, System.currentTimeMillis() - time);
            }
        }
    }
    
    private static String merge(List<String> outputLines) {
        StringBuilder sb = new StringBuilder();
        if (outputLines != null) {
            for (String line : outputLines) {
                if (sb.length() > 0) {
                    sb.append('\n'); //NOI18N
                }
                sb.append(line);
            }
        }
        return sb.toString();
    }
    
}
