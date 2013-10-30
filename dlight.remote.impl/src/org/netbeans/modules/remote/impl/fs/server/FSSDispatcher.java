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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.impl.fs.RefreshManager;
import org.netbeans.modules.remote.impl.fs.RemoteFileObject;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystemManager;
import org.openide.util.RequestProcessor;

/**
 *
 * @author vkvashin
 */
/*package*/ final class FSSDispatcher {

    private static final Map<ExecutionEnvironment, FSSDispatcher> instances = 
            new HashMap<ExecutionEnvironment, FSSDispatcher>();
    private static final Object instanceLock = new Object();
    
    private final ExecutionEnvironment env;
    private final Map<Integer, FSSResponse> responses = new LinkedHashMap<Integer, FSSResponse>();
    private final Object responseLock = new Object();

    // should we have a request queue as well?
    private final LinkedList<FSSRequest> requestQueue = new LinkedList<FSSRequest>();
    private final Object requestLock = new Object();
    
    private static final String SERVER_PATH = System.getProperty("remote.fs_server.path");
    public static final int REFRESH_INTERVAL = Integer.getInteger("remote.fs_server.refresh", 2); // NOI18N
    public static final boolean VERBOSE = Boolean.getBoolean("remote.fs_server.verbose");

    // Actually this RP should have only 2 tasks: one reads error, another stdout;
    // but in the case of, say, connection failure and reconnect, old task can still be alive,
    // while we need to post new one.
    private final RequestProcessor RP = new RequestProcessor(null, 20);
    
    private FsServer server;
    private final Object serverLock = new Object();
    
    private final String traceName;

    public FSSDispatcher(ExecutionEnvironment env) {
        this.env = env;
        traceName = "fs_server[" + env + ']'; // NOI18N
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

    void connected() {
        try {
            getOrCreateServer();
        } catch (IOException ex) {
            ex.printStackTrace(System.err); // TODO: error processing!!!
        }
    }

    private class MainLoop implements Runnable {

        public MainLoop() {
        }
        
        @Override
        public void run() {
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
                    char c = line.charAt(0);
                    Buffer buf = new Buffer(line);
                    char respKind = buf.getChar();
                    int respId = buf.getInt();
                    if (respId == 0) {
                        RemoteLogger.finest("got fs_server notification: {0}", line);
                        if (respKind == FSSResponseKind.CHANGE.getChar()) {                            
                            // example: "c 0 8 /tmp/tmp"
                            String path = buf.getString();
                            RemoteFileObject fo = RemoteFileSystemManager.getInstance().getFileSystem(env).findResource(path);
                            if (fo != null) {
                                RefreshManager refreshManager = RemoteFileSystemManager.getInstance().getFileSystem(env).getRefreshManager();
                                refreshManager.scheduleRefresh(Arrays.asList(fo.getImplementor()), false);
                            }
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
            } catch (IOException ioe) {
                ioe.printStackTrace(System.err);
            } finally {
                Thread.currentThread().setName(oldThreadName);
            }
        }
    }
    
    
    public FSSResponse dispatch(FSSRequest request) throws IOException {
        FSSResponse response = new FSSResponse(request);
        synchronized (responseLock) {
            RemoteLogger.assertNull(responses.get(request.getId()),
                    "response should be null for id {0}", request.getId()); // NOI18N
            responses.put(request.getId(), response);
        }
        FsServer srv = getOrCreateServer();
        sendRequest(srv.getWriter(), request);
        return response;
    }
    
    /*package*/ static void sendRequest(PrintWriter writer, FSSRequest request) {
        writer.printf("%c %d %d %s\n", request.getKind().getChar(), // NOI18N
                request.getId(), request.getPath().length(), request.getPath());
        writer.flush();        
    }

    private FsServer getServer() {        
        synchronized (serverLock) {
            return server;
        }
    }
    
    private FsServer getOrCreateServer() throws IOException {        
        synchronized (serverLock) {
            if (server == null) {
                if (!ConnectionManager.getInstance().isConnectedTo(env)) {
                    throw new ConnectException();
                }
                server = new FsServer();
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
                        System.err.printf("%s\n", line); //NOI18N
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

        public FsServer() throws IOException {
            NativeProcessBuilder processBuilder = NativeProcessBuilder.newProcessBuilder(env);
            processBuilder.setExecutable(SERVER_PATH);
            String[] args = VERBOSE ? 
                    new String[] { "-t", "4", "-p", "-r", "" + REFRESH_INTERVAL, "-v" } : // NOI18N
                    new String[] { "-t", "4", "-p", "-r", "" + REFRESH_INTERVAL }; // NOI18N
            processBuilder.setArguments(args);
            process = processBuilder.call();
            writer = new PrintWriter(process.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
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
