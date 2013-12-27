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
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo;
import org.netbeans.modules.nativeexecution.api.util.RemoteStatistics;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.impl.fs.DirEntry;
import org.netbeans.modules.remote.impl.fs.DirEntryList;
import org.netbeans.modules.remote.impl.fs.DirEntrySftp;
import org.netbeans.modules.remote.impl.fs.RemoteDirectory;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystemTransport;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystemUtils;
import org.openide.util.RequestProcessor;

/**
 *
 * @author vkvashin
 */
public class FSSTransport extends RemoteFileSystemTransport implements ConnectionListener {
    
    private static final Map<ExecutionEnvironment, FSSTransport> instances = new HashMap<ExecutionEnvironment, FSSTransport>();
    private static final Object instancesLock = new Object();
    
    public static final boolean USE_FS_SERVER = RemoteFileSystemUtils.getBoolean("remote.fs_server", true);
    public static final boolean VERBOSE_RESPONSE = Boolean.getBoolean("remote.fs_server.verbose.response");

    private final ExecutionEnvironment env;

    private final FSSDispatcher dispatcher;
    
    private final AtomicInteger dirReadCnt = new AtomicInteger(0);
    private final AtomicInteger warmupCnt = new AtomicInteger(0);
    
    public static FSSTransport getInstance(ExecutionEnvironment env) {
        if (!USE_FS_SERVER) {
            return null;
        }
        synchronized (instancesLock) {
            FSSTransport instance = instances.get(env);
            if (instance == null) {
                instance = new FSSTransport(env);
                instances.put(env, instance);
                ConnectionManager.getInstance().addConnectionListener(instance);
            }
            return instance;
        }
    }

    private FSSTransport(ExecutionEnvironment env) {
        this.env = env;
        this.dispatcher = FSSDispatcher.getInstance(env);        
    }
    
    @Override
    public boolean isValid() {
        return dispatcher.isValid();
    }

    @Override
    protected FileInfoProvider.StatInfo stat(String path) 
            throws InterruptedException, ExecutionException {
        return stat_or_lstat(path, false);
    }

    @Override
    protected FileInfoProvider.StatInfo lstat(String path) 
            throws InterruptedException, ExecutionException {
        return stat_or_lstat(path, true);
    }

    private FileInfoProvider.StatInfo stat_or_lstat(String path, boolean lstat) 
            throws InterruptedException, ExecutionException {

        if (path.isEmpty()) {
            path = "/"; // NOI18N
        }

        FSSRequestKind requestKind = lstat ? FSSRequestKind.FS_REQ_LSTAT : FSSRequestKind.FS_REQ_STAT;
        FSSRequest request = new FSSRequest(requestKind, path);
        long time = System.currentTimeMillis();
        RemoteStatistics.ActivityID activityID = RemoteStatistics.startChannelActivity(
                lstat ? "fs_server_lstat" : "fs_server_stat", path); // NOI18N
        FSSResponse response = null;
        try {
            RemoteLogger.finest("Sending stat/lstat request #{0} for {1} to fs_server", 
                    request.getId(), path);
            try {
                response = dispatcher.dispatch(request);
            } catch (IOException ioe) {
                throw new ExecutionException(ioe);
            }
            FSSResponse.Package pkg = response.getNextPackage();
            if (pkg.getKind() == FSSResponseKind.FS_RSP_ENTRY) {
                Buffer buf = pkg.getBuffer();
                char kindChar = buf.getChar();
                assert kindChar == pkg.getKind().getChar();
                assert pkg.getKind() == FSSResponseKind.FS_RSP_ENTRY;
                int id = buf.getInt();
                assert id == request.getId();
                String name = buf.getString();
                int uid = buf.getInt();
                int gid = buf.getInt();
                int mode = buf.getInt();
                long size = buf.getLong();
                long mtime = buf.getLong() / 1000 * 1000; // to be consistent with jsch sftp
                String linkTarget = buf.getString();
                if (linkTarget.isEmpty()) {
                    linkTarget = null;
                }
                StatInfo statInfo = new StatInfo(name, uid, gid, size,
                        linkTarget, mode, new Date(mtime));
                return statInfo;
            } else if (pkg.getKind() == FSSResponseKind.FS_RSP_ERROR) {
                Buffer buf = pkg.getBuffer();
                buf.getChar(); // skip kind                
                int id = buf.getInt();
                assert id == request.getId();
                int errno = buf.getInt();
                String emsg = buf.getRest();
                IOException ioe = FSSUtil.createIOException(errno, emsg);
                throw new ExecutionException(ioe);
            } else {
                throw new IllegalStateException("wrong response: " + pkg); //NOI18N
            }
            
        } finally {
            if (response != null) {
                response.dispose();
            }
            RemoteStatistics.stopChannelActivity(activityID, 0);
            RemoteLogger.finest("Getting stat/lstat #{0} from fs_server for {1} took {2} ms",
                    request.getId(), path, System.currentTimeMillis() - time);
        }
        
    }
    
    @Override
    protected DirEntryList readDirectory(String path) throws IOException, InterruptedException, CancellationException, ExecutionException {
        if (path.isEmpty()) {
            path = "/"; // NOI18N
        }
        FSSRequest request = new FSSRequest(FSSRequestKind.FS_REQ_LS, path);
        long time = System.currentTimeMillis();
        RemoteStatistics.ActivityID activityID = RemoteStatistics.startChannelActivity("fs_server_ls", path); // NOI18N
        AtomicInteger realCnt = new AtomicInteger(0);
        FSSResponse response = null;
        try {
            RemoteLogger.finest("Sending request #{0} for directry {1} to fs_server", 
                    request.getId(), path);
            // XXX: a temporary simplistic solution
            response = dispatcher.dispatch(request);
            FSSResponse.Package pkg = response.getNextPackage();
            assert pkg.getKind() == FSSResponseKind.FS_RSP_LS;
            Buffer buf = pkg.getBuffer();
            buf.getChar();
            int respId = buf.getInt();
            assert respId == request.getId();
            String serverPath = buf.getString();
            assert serverPath.equals(path);
            return readEntries(response, path, request.getId(), realCnt);
        } finally {
            dirReadCnt.incrementAndGet();
            if (response != null) {
                response.dispose();
            }
            RemoteStatistics.stopChannelActivity(activityID, 0);
            RemoteLogger.finest("Communication #{0} with fs_server for directry {1} ({2} entries read) took {3} ms",
                    request.getId(), path, realCnt.get(), System.currentTimeMillis() - time);
        }
    }

    private DirEntryList readEntries(FSSResponse response, String path, long reqId, AtomicInteger realCnt) 
            throws IOException, InterruptedException, ExecutionException {
        try {
            RemoteLogger.finest("Reading response #{0} from fs_server for directry {1})",
                    reqId, path);
            List<FSSResponse.Package> packages = new ArrayList<FSSResponse.Package>();
            for (FSSResponse.Package pkg = response.getNextPackage(); 
                    pkg.getKind() != FSSResponseKind.FS_RSP_END; 
                    pkg = response.getNextPackage()) {
                if (pkg.getKind() == FSSResponseKind.FS_RSP_END) {
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
            List<DirEntry> result = new ArrayList<DirEntry>();
            for (FSSResponse.Package pkg : packages) {
                try {
                    assert pkg != null;
                    Buffer buf = pkg.getBuffer();
                    char kindChar = buf.getChar();
                    assert kindChar == pkg.getKind().getChar();
                    assert pkg.getKind() == FSSResponseKind.FS_RSP_ENTRY;
                    int id = buf.getInt();
                    assert id == reqId;
                    String name = buf.getString();
                    int uid = buf.getInt();
                    int gid = buf.getInt();
                    int mode = buf.getInt();
                    long size = buf.getLong();
                    long mtime = buf.getLong() / 1000 * 1000; // to be consistent with jsch sftp
                    String linkTarget = buf.getString();
                    if (linkTarget.isEmpty()) {
                        linkTarget = null;
                    }
                    StatInfo statInfo = new StatInfo(name, uid, gid, size,
                            linkTarget, mode, new Date(mtime));
                    DirEntry entry = new DirEntrySftp(statInfo, statInfo.getName());
                    // TODO: windows names
                    result.add(entry);
                } catch (Throwable thr) {
                    thr.printStackTrace(System.err);
                }
            }
            return new DirEntryList(result, System.currentTimeMillis());
        } finally {
        }
    }
    
    @Override
    public void connected(ExecutionEnvironment env) {
        if (env.equals(this.env)) {
            dispatcher.connected();
        }
    }

    @Override
    public void disconnected(ExecutionEnvironment env) {
    }
    
    public final void testSetCleanupUponStart(boolean cleanup) {
        dispatcher.setCleanupUponStart(cleanup);
    }

    @Override
    protected boolean needsClientSidePollingRefresh() {
        return false;
    }
    
    @Override
    protected void registerDirectoryImpl(RemoteDirectory directory) {
        if (ConnectionManager.getInstance().isConnectedTo(env)) {
            requestRefreshCycle(directory.getPath());
        }
    }

    @Override
    protected void unregisterDirectoryImpl(String path) {
        
    }
    
    @Override
    protected void onConnect() {
        // nothing: see ConnectTask
    }

    @Override
    protected void onFocusGained() {
        requestRefreshCycle("/"); //NOI18N
    }

    @Override
    protected void scheduleRefresh(Collection<String> paths) {
        if (!dispatcher.isRefreshing()) {
            for (String path : paths) {
                dispatcher.requestRefreshCycle(path.isEmpty() ? "/" : path); // NOI18N
            }
        }
    }
    
    private void requestRefreshCycle(String path) {
        if (!dispatcher.isRefreshing()) {
            // file system root has empty path
            dispatcher.requestRefreshCycle(path.isEmpty() ? "/" : path); // NOI18N
        }
    }    
    
    @Override
    protected Warmup createWarmup(String path) {
        WarmupImpl warmup = new WarmupImpl(path);
        warmup.start();
        return warmup;
    }            

    private class WarmupImpl implements Warmup, FSSResponse.Listener, Runnable {

        private final String path;
        private final Map<String, DirEntryList> cache = new HashMap<String, DirEntryList>();
        private final Object lock = new Object();
        private FSSResponse response;

        private final boolean useListener = RemoteFileSystemUtils.getBoolean("remote.warmup.listener", false);
        private final RequestProcessor rp;

        public WarmupImpl(String path) {
            this.path = path.isEmpty() ? "/" : path; //NOI18N
            rp = useListener ? null : new RequestProcessor("Warming Up " + env + ':' + this.path, 1); //NOI18N
        }

        public void start() {            
            if (useListener) {
                try {
                    sendRequest();
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                } catch (ExecutionException ex) {
                    ex.printStackTrace(System.err);
                } catch (CancellationException ex) {
                    // don't log CancellationException
                } catch (InterruptedException ex) {
                    // don't log InterruptedException
                }
            } else {
                rp.post(this);                
            }
        }

        @Override
        public void packageAdded(FSSResponse.Package pkg) {
            if (useListener) {
                synchronized (lock) {
                    lock.notifyAll();
                }                
            }
        }

        @Override
        public DirEntryList getAndRemove(String path) throws InterruptedException {
            while (true) {
                DirEntryList l = tryGetAndRemove(path);
                if (l != null) {
                    return l;
                }
                synchronized (lock) {
                    lock.wait(1000);
                }
            }
        }
        

        @Override
        public DirEntryList tryGetAndRemove(String path) {
            synchronized (lock) {
                DirEntryList entries = cache.remove(path);
                if (entries != null) {
                    RemoteLogger.fine("Warming up: got entries for {0}; {1} cached entry lists remain", path, cache.size());
                    return entries;
                }
            }
            return null;
        }

        @Override
        public void remove(String path){
            synchronized (lock) {
                cache.remove(path);
            }
        }

        
        @Override
        public void run() {
            long time = System.currentTimeMillis();
            RemoteStatistics.ActivityID activityID = RemoteStatistics.startChannelActivity("fs_server_warmup", path); // NOI18N            
            try {
                RemoteLogger.fine("Warming up fs_server for {0}", path);
                warmapImpl();
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            } catch (InterruptedException ex) {
                // don't report InterruptedException
            } finally {
                warmupCnt.incrementAndGet();
                RemoteStatistics.stopChannelActivity(activityID, 0);
                RemoteLogger.fine("Warming up fs_server for {0} took {1} ms",
                        path, System.currentTimeMillis() - time);
            }            
        }

        private FSSResponse sendRequest() 
                throws IOException, ConnectException, ExecutionException,
                CancellationException, InterruptedException {

            FSSRequest request = new FSSRequest(FSSRequestKind.FS_REQ_RECURSIVE_LS, path);
                RemoteLogger.finest("Sending recursive request #{0} for directry {1} to fs_server",
                        request.getId(), path);
            return dispatcher.dispatch(request, this);
        }

        private void warmapImpl() throws IOException, InterruptedException {
            long time = System.currentTimeMillis();
            AtomicInteger realCnt = new AtomicInteger(0);
            try {
                synchronized (lock) {
                    response = sendRequest();                    
                }

                while (true) {
                    FSSResponse.Package pkg = response.getNextPackage();
                    if (pkg.getKind() == FSSResponseKind.FS_RSP_END) {
                        break;
                    }
                    Buffer buf = pkg.getBuffer();
                    char respKind = buf.getChar();
                    assert respKind == FSSResponseKind.FS_RSP_RECURSIVE_LS.getChar();
                    int respId = buf.getInt();
                    assert respId == response.getId();
                    String serverPath = buf.getString();
                    DirEntryList entries = readEntries(response, serverPath, response.getId(), realCnt);
                    synchronized (lock) {
                        cache.put(serverPath, entries);
                        lock.notifyAll();
                    }
                }
                
            } catch (CancellationException ex) {
                // don't report CancellationException
                synchronized (lock) {
                    cache.clear();
                }
            } catch (ConnectException ex) {
                ex.printStackTrace(System.err);
            } catch (ExecutionException ex) {
                ex.printStackTrace(System.err);
            } finally {
                FSSResponse r;
                synchronized (lock) {
                    r = response;
                }
                if (r != null) {
                    r.dispose();
                }
                RemoteLogger.finest("Warming up directry {1}:{2}: ({3} entries) took {4} ms",
                        env, path, realCnt.get(), System.currentTimeMillis() - time);
            }
        }
    }
}
