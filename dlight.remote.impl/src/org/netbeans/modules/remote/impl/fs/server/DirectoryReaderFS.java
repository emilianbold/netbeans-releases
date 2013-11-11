package org.netbeans.modules.remote.impl.fs.server;

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

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionListener;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo;
import org.netbeans.modules.nativeexecution.api.util.RemoteStatistics;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.impl.fs.DirEntry;
import org.netbeans.modules.remote.impl.fs.DirEntrySftp;
import org.netbeans.modules.remote.impl.fs.DirectoryReader;
import org.netbeans.modules.remote.impl.fs.RemoteFileObject;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystemManager;
import org.openide.modules.OnStart;
import org.openide.modules.OnStop;
import org.openide.util.Exceptions;

/**
 *
 * @author vkvashin
 */
public class DirectoryReaderFS implements DirectoryReader {
    
    private static final Map<ExecutionEnvironment, DirectoryReaderFS> instances = new HashMap<ExecutionEnvironment, DirectoryReaderFS>();
    private static final Object instancesLock = new Object();
    
    public static final boolean USE_FS_SERVER = Boolean.getBoolean("remote.fs_server");
    public static final boolean VERBOSE_RESPONSE = Boolean.getBoolean("remote.fs_server.verbose.response");

    private final ExecutionEnvironment env;

    private final Map<String, List<DirEntry>> cache = new HashMap<String, List<DirEntry>>();
    private final Object cacheLock = new Object();
    private final Object lock = new Object();

    private final FSSDispatcher dispatcher;
    
    public static DirectoryReaderFS getInstance(ExecutionEnvironment env) {
        if (!USE_FS_SERVER) {
            return null;
        }
        synchronized (instancesLock) {
            DirectoryReaderFS instance = instances.get(env);
            if (instance == null) {
                instance = new DirectoryReaderFS(env);
                instances.put(env, instance);
            }
            return instance;
        }
    }

    private DirectoryReaderFS(ExecutionEnvironment env) {
        this.env = env;
        this.dispatcher = new FSSDispatcher(env);
    }

    public void warmap(String path) {
        long time = System.currentTimeMillis();
        RemoteStatistics.ActivityID activityID = RemoteStatistics.startChannelActivity("fs_server_warmup", path); // NOI18N            
        try {
            RemoteLogger.fine("Warming up fs_server for {0}", path);
            warmapImpl(path);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        } catch (InterruptedException ex) {
            // don't report InterruptedException
        } finally {
            RemoteStatistics.stopChannelActivity(activityID, 0);
            RemoteLogger.fine("Warming up fs_server for {0} took {1} ms", 
                    path, System.currentTimeMillis() - time);            
        }
    }
    
    public void warmapImpl(String path) throws IOException, InterruptedException {
        if (path.isEmpty()) {
            path = "/"; // NOI18N
        }
        FSSRequest request = new FSSRequest(FSSRequestKind.RECURSE, path);
        List<String> paths = new ArrayList<String>();
        long time = System.currentTimeMillis();
        AtomicInteger realCnt = new AtomicInteger(0);
        try {
            RemoteLogger.finest("Sending recursive request #{0} for directry {1} to fs_server", 
                    request.getId(), path);
            synchronized (lock) { 
                // XXX: a temporary simplistic solution
                FSSResponse response = dispatcher.dispatch(request);
                while (true) {
                    FSSResponse.Package pkg = response.getNextPackage();
                    if (pkg.getKind() == FSSResponseKind.END) {
                        break;
                    }
                    Buffer buf = pkg.getBuffer();
                    char respKind = buf.getChar();
                    assert respKind == FSSResponseKind.RECURSE.getChar();
                    int respId = buf.getInt();
                    assert respId == request.getId();
                    String serverPath = buf.getString();
                    int cnt = buf.getInt();
                    List<DirEntry> entries = readEntries(response, serverPath, cnt, request.getId(), realCnt);
                    cache.put(serverPath, entries);
                    paths.add(serverPath);
                }
            }
        } catch (CancellationException ex) {
            // don't report CancellationException
            synchronized (lock) { 
                cache.clear();
                return;
            }
        } catch (ConnectException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            ex.printStackTrace(System.err);
        } finally {
            RemoteLogger.finest("Communication #{0} with fs_server for directry {1}: ({2} entries) took {3} ms",
                    request.getId(), path, realCnt.get(), System.currentTimeMillis() - time);
        }

        time = System.currentTimeMillis();
        for (String p : paths) {
            RemoteFileObject fo = RemoteFileSystemManager.getInstance().getFileSystem(env).findResource(p);
            if (fo != null) {
                fo.getChildren();
            }
        }
        RemoteLogger.finest("Instantiating #{0} {1} subdirectories with fs_server for directry {2} took {3} ms",
                request.getId(), paths.size(), path, System.currentTimeMillis() - time);
        
        RemoteFileObject root = RemoteFileSystemManager.getInstance().getFileSystem(env).findResource(path);
        time = System.currentTimeMillis();
        root.refresh();
        RemoteLogger.finest("Refreshing #{0} with fs_server for directry {1} took {2} ms",
                request.getId(), path, System.currentTimeMillis() - time);

        synchronized (lock) { 
            cache.clear();
        }
    }
    
    @Override
    public List<DirEntry> readDirectory(String path) throws IOException, InterruptedException, CancellationException, ExecutionException {
        if (path.isEmpty()) {
            path = "/"; // NOI18N
        }
        synchronized (cacheLock) {
            List<DirEntry> entries = cache.get(path);
            if (entries != null) {
                RemoteLogger.fine("Got entries from fs_server cache for {0}", path);
                return entries;
            }
        }
        FSSRequest request = new FSSRequest(FSSRequestKind.LS, path);
        long time = System.currentTimeMillis();
        RemoteStatistics.ActivityID activityID = RemoteStatistics.startChannelActivity("fs_server_ls", path); // NOI18N
        AtomicInteger realCnt = new AtomicInteger(0);
        try {
            RemoteLogger.finest("Sending request #{0} for directry {1} to fs_server", 
                    request.getId(), path);
            synchronized (lock) { 
                // XXX: a temporary simplistic solution
                FSSResponse response = dispatcher.dispatch(request);
                FSSResponse.Package pkg = response.getNextPackage();
                assert pkg.getKind() == FSSResponseKind.LS;
                Buffer buf = pkg.getBuffer();
                buf.getChar();
                int respId = buf.getInt();
                assert respId == request.getId();
                String serverPath = buf.getString();
                assert serverPath.equals(path);
                int cnt = buf.getInt();
                return readEntries(response, path, cnt, request.getId(), realCnt);
            }
        } finally {
            RemoteStatistics.stopChannelActivity(activityID, 0);
            RemoteLogger.finest("Communication #{0} with fs_server for directry {1} ({2} entries read) took {3} ms",
                    request.getId(), path, realCnt.get(), System.currentTimeMillis() - time);
        }
    }

    private List<DirEntry> readEntries(FSSResponse response, String path, int cnt, long reqId, AtomicInteger realCnt) 
            throws IOException, InterruptedException {
        try {
            RemoteLogger.finest("Reading response #{0} from fs_server for directry {1})",
                    reqId, path);
            List<FSSResponse.Package> packages = new ArrayList<FSSResponse.Package>(cnt);
            for (FSSResponse.Package pkg = response.getNextPackage(); 
                    pkg.getKind() != FSSResponseKind.END; 
                    pkg = response.getNextPackage()) {
                if (pkg.getKind() == FSSResponseKind.END) {
                    break;
                }
                realCnt.incrementAndGet();
                if (VERBOSE_RESPONSE) {
                    RemoteLogger.finest("\tfs_server response #{0}: [{1}] {2}",
                            reqId, realCnt.get(), pkg.getData());
                }
                packages.add(pkg);
            }
            RemoteLogger.finest("Processing response #{0} from fs_server for directry {1}",
                    reqId, path);
            List<DirEntry> result = new ArrayList<DirEntry>(cnt);
            for (FSSResponse.Package pkg : packages) {
                try {
                    assert pkg != null;
                    Buffer buf = pkg.getBuffer();
                    char kindChar = buf.getChar();
                    assert kindChar == pkg.getKind().getChar();
                    assert pkg.getKind() == FSSResponseKind.ENTRY;
                    int id = buf.getInt();
                    assert id == reqId;
                    String name = buf.getString();
                    int uid = buf.getInt();
                    int gid = buf.getInt();
                    int mode = buf.getInt();
                    long size = buf.getLong();
                    long mtime = buf.getLong();
                    String linkTarget = buf.getString();
                    StatInfo statInfo = new StatInfo(name, uid, gid, size,
                            linkTarget, mode, new Date(mtime));
                    DirEntry entry = new DirEntrySftp(statInfo, statInfo.getName());
                    // TODO: windows names
                    result.add(entry);
                } catch (Throwable thr) {
                    thr.printStackTrace(System.err);
                }
            }
            return result;
        } finally {
        }
    }
    
    private void shutdown() {
    }

    private void connected() {
        dispatcher.connected();
    }
    
    @OnStop
    public static class Closer implements Runnable {
        @Override
        public void run() {
            Collection<DirectoryReaderFS> inctancesCopy;
            synchronized (instancesLock) {                
                inctancesCopy = instances.values();
            }
            for (DirectoryReaderFS instance : inctancesCopy) {
                instance.shutdown();
            }
        }        
    }
    
    @OnStart
    public static class Starter implements Runnable, ConnectionListener {

        @Override
        public void run() {
            ConnectionManager.getInstance().addConnectionListener(this);
        }

        @Override
        public void connected(ExecutionEnvironment env) {
            if (USE_FS_SERVER) {
                getInstance(env).connected();
            }
        }

        @Override
        public void disconnected(ExecutionEnvironment env) {
        }
    }
}
