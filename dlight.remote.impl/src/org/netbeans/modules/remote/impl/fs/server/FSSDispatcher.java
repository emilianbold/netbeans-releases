/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.impl.fs.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.impl.fs.RefreshManager;
import org.netbeans.modules.remote.impl.fs.RemoteFileObject;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystemManager;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author vkvashin
 */
/*package*/ final class FSSDispatcher implements Disposer<FSSResponse> {

    private static final boolean SUPPRESS_STDERR = Boolean.parseBoolean(System.getProperty("remote.fs_server.suppress.stderr", "true"));
    private static final Map<ExecutionEnvironment, FSSDispatcher> instances = 
            new HashMap<ExecutionEnvironment, FSSDispatcher>();
    private static final Object instanceLock = new Object();
    
    private final ExecutionEnvironment env;
    private final Map<Integer, FSSResponse> responses = new LinkedHashMap<Integer, FSSResponse>();
    private final Object responseLock = new Object();

    private static final String USER_DEFINED_SERVER_PATH = System.getProperty("remote.fs_server.path");
    private static final int REFRESH_INTERVAL = Integer.getInteger("remote.fs_server.refresh", 2); // NOI18N
    private static final int VERBOSE = Integer.getInteger("remote.fs_server.verbose", 0); // NOI18N
    private static final boolean LOG = Boolean.getBoolean("remote.fs_server.log");

    // Actually this RP should have only 2 tasks: one reads error, another stdout;
    // but in the case of, say, connection failure and reconnect, old task can still be alive,
    // while we need to post new one.
    private final RequestProcessor RP = new RequestProcessor(getClass().getSimpleName(), 20);
    
    private FsServer server;
    private final Object serverLock = new Object();
    
    private final String traceName;
    
    private volatile boolean valid = true;
    private final AtomicInteger attempts = new AtomicInteger();
    private static final int MAX_ATTEMPTS = Integer.getInteger("remote.fs_server.attempts", 3); // NOI18N
    
    private final AtomicReference<String> lastErrorMessage = new AtomicReference<String>();
    
    private final Set<String> refreshSet = new TreeSet<String>();
    private final LinkedList<String> refreshQueue = new LinkedList<String>();
    private final Object refreshLock = new Object();
    private final RequestProcessor refreshRp = new RequestProcessor(getClass().getSimpleName() + "_Refresh"); //NOI18N
    private final RequestProcessor.Task refreshTask = refreshRp.create(new RefreshRunnable());

    private volatile boolean cleanupUponStart = false;
    
    private FSSDispatcher(ExecutionEnvironment env) {
        this.env = env;
        traceName = "fs_server[" + env + ']'; // NOI18N
    }
    
    public void setCleanupUponStart(boolean cleanup) {
        cleanupUponStart = cleanup;
    }
    
    private class RefreshRunnable implements Runnable {
        @Override
        public void run() {
            while (true) {
                List<String> paths = new ArrayList<String>();
                synchronized (refreshLock) {
                    if (refreshQueue.isEmpty()) {
                        try {
                            refreshLock.wait(1000);
                        } catch (InterruptedException ex) {
                            //nothing
                        }
                    } else {
                        paths.addAll(refreshQueue);
                        refreshQueue.clear();
                        refreshSet.clear();
                    }
                }
                for (String path : paths) {
                    RemoteFileObject fo = RemoteFileSystemManager.getInstance().getFileSystem(env).findResource(path);
                    if (fo != null) {
                        RefreshManager refreshManager = RemoteFileSystemManager.getInstance().getFileSystem(env).getRefreshManager();
                        refreshManager.scheduleRefresh(Arrays.asList(fo.getImplementor()), false);
                    }
                }
            }
        }        
    }
    
    private void addToRefresh(String path) {
        synchronized (refreshLock) {
            if (!refreshSet.contains(path)) {
                refreshSet.add(path);
                refreshQueue.add(path);
                refreshLock.notifyAll();
            }
        }
        refreshTask.schedule(0);
    }
    
    public static FSSDispatcher getInstance(ExecutionEnvironment env) {
        synchronized (instanceLock) {
            FSSDispatcher instance = instances.get(env);
            if (instance == null) {
                instance = new FSSDispatcher(env);
                instances.put(env, instance);
            }
            return instance;
        }
    }

    public void connected() {
        RP.post(new ConnectTask());
    }

    private class ConnectTask implements Runnable {
        @Override
        public void run() {
            String oldThreadName = Thread.currentThread().getName();
            Thread.currentThread().setName("fs_server on-connect initialization for " + env); // NOI18N
            try {
                getOrCreateServer();
            } catch (ConnectException ex) {
                ex.printStackTrace(System.err);
            } catch (ConnectionManager.CancellationException ex) {
                ex.printStackTrace(System.err);
            } catch (IOException ioe) {
                ioe.printStackTrace(System.err);
                setInvalid(false);
            } catch (InterruptedException ex) {
                ex.printStackTrace(System.err);
            } catch (ExecutionException ex) {
                ex.printStackTrace(System.err);
                setInvalid(false);
            } finally {
                Thread.currentThread().setName(oldThreadName);
            }
        }
    }
    
    private class MainLoop implements Runnable {
        @Override
        public void run() {
            int exceptionsCount = 0;
            String oldThreadName = Thread.currentThread().getName();
            try {
                Thread.currentThread().setName("fs_server dispatcher for " + env); // NOI18N
                FsServer server = getServer();
                if (server == null) {
                    RemoteLogger.warning("Can not launch file system server on {0}", env);
                    return;
                }
                BufferedReader reader = server.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty()) {
                        RemoteLogger.info("error: empty line for " + traceName);
                        continue;
                    }
                    line = unescape(line);
                    Buffer buf = new Buffer(line);
                    char respKind = buf.getChar();
                    int respId = buf.getInt();
                    if (respId == 0) {
                        RemoteLogger.finest("got fs_server notification: {0}", line);
                        if (respKind == FSSResponseKind.FS_RSP_CHANGE.getChar()) {                            
                            // example: "c 0 8 /tmp/tmp"
                            String path = buf.getString();
                            addToRefresh(path);
                        } else {
                            RemoteLogger.info("wrong response #0: {1}", line);
                        }
                    } else {
                        synchronized (responseLock) {
                            FSSResponse response = responses.get(respId);
                            if (response == null) {
                                RemoteLogger.info("skipping {0} response #{1}: {2}",
                                        traceName, respId, line);
                            } else {
                                response.addPackage(FSSResponseKind.fromChar(respKind), line);
                            }
                        }
                    }
                }
                NativeProcess process = server.getProcess();
                if (!ProcessUtils.isAlive(process)) {
                    int rc = process.waitFor();                    
                    RemoteLogger.finest("fs_server (pid {0} at {1}) exited with rc = {2}", process.getPID(), env, rc);//NOI18N
                    
                }
            } catch (IOException ioe) {
                ioe.printStackTrace(System.err);
            } catch (Throwable thr) { // too wide, but we need to guarantee dispatcher is alive
                thr.printStackTrace(System.err);
                if (exceptionsCount++ > 1000) {
                    setInvalid(true);
                }
            } finally {
                try {
                    checkValid();
                } catch (ExecutionException ex) {
                    ex.printStackTrace(System.err);
                } catch (InterruptedException ex) {
                    // none
                }
                Thread.currentThread().setName(oldThreadName);
            }
        }
    }

    public boolean isValid() {
        return valid;
    }
    
    private void setInvalid(boolean force) {
        if (force) {
            valid = false;
        } else {
            if (attempts.incrementAndGet() > MAX_ATTEMPTS) {
                valid = false;
            }
        }
    }
    
    private void checkValid() throws ExecutionException, InterruptedException {
        if (ConnectionManager.getInstance().isConnectedTo(env)) {
            FsServer srv = getServer();
            if (srv != null) {
                NativeProcess process = srv.getProcess();
                if (!ProcessUtils.isAlive(process)) {
                    try {
                        int rc = process.waitFor();
                        if (rc != 0 && rc != -1) { // -1 most likely means just disconnect
                            setInvalid(false);
                        }
                        ExecutionException exception = new ExecutionException(lastErrorMessage.get(), null);
                        synchronized (responseLock) {
                            for (FSSResponse rsp : responses.values()) {                                
                                rsp.failed(exception);
                            }
                        }
                        throw exception;
                    } catch (IllegalThreadStateException ex) {
                        ex.printStackTrace(System.err);
                    }
                }
            }
        }
    }

    @Override
    public void dispose(FSSResponse response) {
        synchronized (responseLock) {
            responses.remove(response.getId());
        }
    }

    public FSSResponse dispatch(FSSRequest request) throws IOException, ConnectException, 
            CancellationException, InterruptedException, ExecutionException {
        FSSResponse response = new FSSResponse(request, this);
        synchronized (responseLock) {
            RemoteLogger.assertNull(responses.get(request.getId()),
                    "response should be null for id {0}", request.getId()); // NOI18N
            responses.put(request.getId(), response);
        }
        FsServer srv;
        try {
            srv = getOrCreateServer();
        } catch (ConnectionManager.CancellationException ex) {
            throw new java.util.concurrent.CancellationException(ex.getMessage());
        } catch (IOException ex) {
            setInvalid(false);
            throw ex;
        } catch (ExecutionException ex) {
            setInvalid(false);
            throw ex;
        }
        PrintWriter writer = srv.getWriter();
        sendRequest(writer, request);
        if(writer.checkError()) { // should we use just input stream instead of writer?
            checkValid();
        }     
        return response;
    }
    
    /*package*/ static void sendRequest(PrintWriter writer, FSSRequest request) {
        String escapedPath = escape(request.getPath());
        String buffer = String.format("%c %d %d %s\n", request.getKind().getChar(), // NOI18N
                request.getId(), escapedPath.length(), escapedPath);
        RemoteLogger.finest("### sending request {0}", buffer);
        writer.print(buffer);
        writer.flush();   
    }

    private FsServer getServer() {        
        synchronized (serverLock) {
            return server;
        }
    }
    
    private static boolean isFreeBSD(ExecutionEnvironment execEnv) {
        ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "uname"); // NOI18N
        if (res.isOK()) {
            if (res.output.equals("FreeBSD")) { // NOI18N
                return true;
            }
        }
        return false;
    }
    
    /*package*/ static File testGetOriginalFSServerFile(ExecutionEnvironment execEnv) 
            throws IOException, ConnectionManager.CancellationException {
        String path = getOriginalFSServerPath(execEnv);
        return InstalledFileLocator.getDefault().locate(
                path, "org.netbeans.modules.remote.impl", false); // NOI18N
   }
    
    private static String getOriginalFSServerPath(ExecutionEnvironment execEnv) 
            throws IOException, ConnectionManager.CancellationException {

        String toolPath = "";
        MacroExpanderFactory.MacroExpander macroExpander = MacroExpanderFactory.getExpander(execEnv);
        try {
            String platformPath;
            HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);
            HostInfo.OSFamily osFamily = hostInfo.getOSFamily();
            if (osFamily == HostInfo.OSFamily.UNKNOWN) {
                if (isFreeBSD(execEnv)) {
                    platformPath = "FreeBSD-x86"; // NOI18N
                } else {                    
                    throw new IOException("Unsupported platform on " + execEnv.getDisplayName()); //NOI18N
                }
            } else {
                String toExpand = "$osname-$platform" + // NOI18N
                        ((osFamily == HostInfo.OSFamily.LINUX) ? "${_isa}" : ""); // NOI18N
                platformPath = macroExpander.expandPredefinedMacros(toExpand);
            }
            toolPath += "bin/" + platformPath + "/fs_server"; //NOI18N
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return toolPath;
    }
    
    private String checkServerSetup() throws ConnectException, IOException, 
            ConnectionManager.CancellationException, InterruptedException, ExecutionException {

        if (!ConnectionManager.getInstance().isConnectedTo(env)) {
            throw new ConnectException();
        }

        String toolPath;
        try {
            toolPath = getOriginalFSServerPath(env);
        } catch (IOException ioe) {
            setInvalid(true);
            throw ioe;
        }
        HostInfo hostInfo = HostInfoUtils.getHostInfo(env);

        String remotePath = USER_DEFINED_SERVER_PATH;
        if (remotePath == null) {
            remotePath = hostInfo.getTempDir() + "/" + toolPath; // NOI18N
        }
        String remoteBase = PathUtilities.getDirName(remotePath);
        
        File localFile = InstalledFileLocator.getDefault().locate(
                toolPath, "org.netbeans.modules.remote.impl", false); // NOI18N
        if (localFile != null && localFile.exists()) {
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
            npb.setExecutable("/bin/mkdir").setArguments("-p", remoteBase); // NOI18N
            npb.call().waitFor();
            Future<CommonTasksSupport.UploadStatus> copyTask;
            copyTask = CommonTasksSupport.uploadFile(localFile, env, remotePath, 0755, true); // NOI18N
            CommonTasksSupport.UploadStatus uploadStatus = copyTask.get(); // is it OK not to check upload exit code?
            if (!uploadStatus.isOK()) {
                throw new IOException(uploadStatus.getError());
            }
        } else {
            if (!HostInfoUtils.fileExists(env, remotePath)) {
                setInvalid(true);
                throw new FileNotFoundException(env.getDisplayName() + ':' + remotePath); //NOI18N
            }
        }
        return remotePath;
    }
    
    private static String unescape(String line) {
        if (line.indexOf('\\') == -1) {
            return line;
        } else {
            return  line.replace("\\n", "\n").replace("\\\\", "\\"); // NOI18N
        }
    }
    
    private static String escape(String line) {
        if (line.indexOf('\n') == -1 && line.indexOf('\\') == -1) {
            return line;
        } else {
            return  line.replace("\n", "\\n").replace("\\", "\\\\"); // NOI18N
        }
    }

    private FsServer getOrCreateServer() throws IOException, ConnectException, 
            ConnectionManager.CancellationException, InterruptedException, ExecutionException {
        synchronized (serverLock) {
            if (server != null) {
                if (!ProcessUtils.isAlive(server.getProcess())) {
                    server = null;
                }
            }
            if (server == null) {
                if (!ConnectionManager.getInstance().isConnectedTo(env)) {
                    throw new ConnectException();
                }
                String path = checkServerSetup();
                server = new FsServer(path);
                RP.post(new MainLoop());
                RP.post(new ErrorReader(server.getProcess().getErrorStream()));
            }
            return server;
        }
    }
    
    private class ErrorReader implements Runnable {
        
        private final InputStream inputStream;

        public ErrorReader(InputStream inputStream) {
            this.inputStream = inputStream;
        }
        
        @Override
        public void run() {
            String oldThreadName = Thread.currentThread().getName();
            try {
                Thread.currentThread().setName("fs_server error reader for " + env); // NOI18N
                BufferedReader reader = ProcessUtils.getReader(inputStream, true);
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!SUPPRESS_STDERR) {
                            System.err.printf("%s %s\n", env, line); //NOI18N
                        }
                        lastErrorMessage.set(line);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                } finally {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                    }
                }
            } finally {
                Thread.currentThread().setName(oldThreadName);
            }
        }
        
    }
    
    private class FsServer {

        private final PrintWriter writer;
        private final BufferedReader reader;
        private final NativeProcess process;
        private final String path;

        public FsServer(String path) throws IOException {
            this.path = path;
            NativeProcessBuilder processBuilder = NativeProcessBuilder.newProcessBuilder(env);
            processBuilder.setExecutable(this.path);
            List<String> args = new ArrayList<String>();
            args.add("-t"); // NOI18N
            args.add("4"); // NOI18N
            args.add("-p"); // NOI18N
            args.add("-d"); // NOI18N
            args.add(getSubdir());
            if (REFRESH_INTERVAL > 0) {
                args.add("-r"); // NOI18N
                args.add("" + REFRESH_INTERVAL);
            }
            if (VERBOSE > 0) {
                args.add("-v"); // NOI18N
                args.add("" + VERBOSE); // NOI18N
            }
            if (LOG) {
                args.add("-l"); // NOI18N
            }
            if (cleanupUponStart) {
                args.add("-c"); // NOI18N
            }
            processBuilder.setArguments(args.toArray(new String[args.size()]));
            process = processBuilder.call();
            writer = new PrintWriter(process.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        }
        
        private String getSubdir() {
            assert HostInfoUtils.isHostInfoAvailable(env);
            String tmp = env.toString() + '/' + Places.getUserDirectory().getAbsolutePath(); // NOI18N
            return Integer.toString(tmp.hashCode()).replace('-', '0');
        }

        public PrintWriter getWriter() {
            return writer;
        }

        public BufferedReader getReader() {
            return reader;
        }

        public NativeProcess getProcess() {
            return process;
        }
    }
}
