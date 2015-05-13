/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.impl.fs;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Collection;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.impl.fs.server.FSSTransport;
import org.netbeans.modules.remote.spi.FileSystemProvider;

/**
 *
 * @author vk155633
 */
public abstract class RemoteFileSystemTransport {

    public interface Warmup {
        DirEntryList getAndRemove(String path) throws InterruptedException;
        void remove(String path);
        DirEntryList tryGetAndRemove(String path);
    }
    
    public static Warmup createWarmup(ExecutionEnvironment execEnv, String path) {
        return getInstance(execEnv).createWarmup(path);
    }

    public static boolean needsClientSidePollingRefresh(ExecutionEnvironment execEnv) {
        return getInstance(execEnv).needsClientSidePollingRefresh();
    }
    
    public static boolean canRefreshFast(ExecutionEnvironment execEnv) {
        return getInstance(execEnv).canRefreshFast();
    }

    public static boolean canSetAccessCheckType(ExecutionEnvironment execEnv) {
        return getInstance(execEnv).canSetAccessCheckType();
    }

    public static void setAccessCheckType(ExecutionEnvironment execEnv, FileSystemProvider.AccessCheckType accessCheckType) {
        getInstance(execEnv).setAccessCheckType(accessCheckType);
    }

    static FileSystemProvider.AccessCheckType getAccessCheckType(ExecutionEnvironment execEnv) {
        return getInstance(execEnv).getAccessCheckType();
    }
    
    public static void refreshFast(RemoteDirectory directory, boolean expected) 
            throws ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {
        if (!directory.hasCache()) {
            return;
        }
        String path = directory.getPath();
        if (path.isEmpty()) {
            path = "/"; // NOI18N
        }
        long time = 0;
        int foCount = 0, syncCount = 0;
        if (RemoteLogger.getInstance().isLoggable(Level.FINE)) {
            final RemoteFileSystem fs = directory.getFileSystem();
            time = System.currentTimeMillis();
            foCount = fs.getCachedFileObjectsCount();
            syncCount = fs.getDirSyncCount();
        }

        getInstance(directory.getExecutionEnvironment()).refreshFast(path,expected);

        if (RemoteLogger.getInstance().isLoggable(Level.FINE)) {
            final RemoteFileSystem fs = directory.getFileSystem();
            time = System.currentTimeMillis() - time;
            foCount = fs.getCachedFileObjectsCount() - foCount;
            syncCount = fs.getDirSyncCount() - syncCount;            
            RemoteLogger.fine("Fast refresh {0} [{1} fo, {2} syncs, {3} new fo] took {4} ms",  //NOI18N
                    directory.getDisplayName(), fs.getCachedFileObjectsCount(),
                    syncCount, foCount, time);
        }
    }

    public static void scheduleRefresh(ExecutionEnvironment env, Collection<String> paths) {
        getInstance(env).scheduleRefresh(paths);
    }

    static void onFocusGained(ExecutionEnvironment execEnv) {
        getInstance(execEnv).onFocusGained();
    }

    static void onConnect(ExecutionEnvironment execEnv) {
        getInstance(execEnv).onConnect();
    }
        
    public static void registerDirectory(RemoteDirectory directory) {
        getInstance(directory.getExecutionEnvironment()).registerDirectoryImpl(directory);
    }

    public static void unregisterDirectory(ExecutionEnvironment execEnv, String path) {
        getInstance(execEnv).unregisterDirectoryImpl(path);
    }

    public static DirEntryList readDirectory(ExecutionEnvironment execEnv, String path) 
            throws ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {

        DirEntryList entries = null;
        RemoteFileSystemTransport transport = FSSTransport.getInstance(execEnv);
        if (transport != null && transport.isValid()) {
            try {
                entries = transport.readDirectory(path);
                // The agreement is as follows: if a fatal error occurs
                // (so we suppose fs_server can't work)
                // DirectoryReaderFS throws ExecutionException or IOException 
                // (InterruptedException and CancellationException don't mean server failed)
                // and DirectoryReaderFS.isValid()  is set to false.
                // In this case we need to fallback to the default (sftp) implementation
                // TODO: consider redesign?
            } catch (ExecutionException | IOException ex) {
                if (transport.isValid()) {
                    throw ex; // process as usual
                } // else fall back to sftp implementation
            }
        }
        if (entries == null) {
            if (RemoteFileSystemUtils.isUnitTestMode()) {
                // no fallback for unit tests!
                throw new ExecutionException("Can not get valid transport for " + execEnv, null); //NOI18N
            }            
            RemoteFileSystemTransport directoryReader = SftpTransport.getInstance(execEnv);
            entries = directoryReader.readDirectory(path);
        }            
        return entries;
    }
    
    public static DirEntry stat(ExecutionEnvironment execEnv, String path) 
            throws ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {

        return getInstance(execEnv).stat(path);
    }
    
    public static DirEntry lstat(ExecutionEnvironment execEnv, String path)
            throws ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {

        return getInstance(execEnv).lstat(path);
     }

    public static DirEntryList delete(ExecutionEnvironment execEnv, String path, boolean directory) 
            throws ConnectException, IOException {
        return getInstance(execEnv).delete(path, directory);
    }

    public static boolean canCopy(ExecutionEnvironment execEnv, String from, String to) {
        return getInstance(execEnv).canCopy(from, to);
    }

    /**
     * Copies the file, returns new parent directory content. Returning parent
     * directory content is for the sake of optimization. For example,
     * fs_server, can do copy and return refreshed content in one call. It can
     * return null if there is no way of doing that more effective than just
     * calling RemoteFileSystemTransport.readDirectory
     * @param  subdirectoryExceptions if we are copying a directory, 
     * problems that occur inside are to be re reported via this list;
     * if we are unable to create directory itself, then exception is thrown.
     * Can be null.
     *
     * @return parent directory content (can be null - see above)
     */
    public static DirEntryList copy(ExecutionEnvironment execEnv, String from, String to, 
            Collection<IOException> subdirectoryExceptions)
            throws ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {
        return getInstance(execEnv).copy(from, to, subdirectoryExceptions);
    }

    public static class MoveInfo {
        public final DirEntryList from;
        public final DirEntryList to;
        public MoveInfo(DirEntryList src, DirEntryList dst) {
            this.from = src;
            this.to = dst;
        }
    }

    public static boolean canMove(ExecutionEnvironment execEnv, String from, String to) {
        return getInstance(execEnv).canMove(from, to);
    }

    public static MoveInfo move(ExecutionEnvironment execEnv, String from, String to)
            throws ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {
        return getInstance(execEnv).move(from, to);
    }
    
    public static DirEntry uploadAndRename(ExecutionEnvironment execEnv, File src, 
            String pathToUpload, String pathToRename) 
            throws ConnectException, IOException, InterruptedException, ExecutionException, InterruptedException {
        return getInstance(execEnv).uploadAndRename(src, pathToUpload, pathToRename);
    }

    private static RemoteFileSystemTransport getInstance(ExecutionEnvironment execEnv) {
        RemoteFileSystemTransport transport = FSSTransport.getInstance(execEnv);
        if (transport == null || ! transport.isValid()) {
            transport = SftpTransport.getInstance(execEnv);
        }
        return transport;
    }

    protected abstract boolean canCopy(String from, String to);

    protected abstract DirEntryList copy(String from, String to, 
            Collection<IOException> subdirectoryExceptions)
            throws ConnectException, IOException, InterruptedException, CancellationException, ExecutionException;

    protected abstract boolean canMove(String from, String to);

    protected abstract MoveInfo move(String from, String to)
            throws ConnectException, IOException, InterruptedException, CancellationException, ExecutionException;

    protected abstract DirEntry stat(String path) 
            throws ConnectException, IOException, InterruptedException, CancellationException, ExecutionException;
    
    protected abstract DirEntry lstat(String path) 
            throws ConnectException, IOException, InterruptedException, CancellationException, ExecutionException;

    protected abstract DirEntryList readDirectory(String path) 
            throws ConnectException, IOException, InterruptedException, CancellationException, ExecutionException;

    protected abstract boolean isValid();
    
    protected abstract boolean needsClientSidePollingRefresh();

    protected abstract boolean canRefreshFast();
    
    protected abstract void refreshFast(String path, boolean expected)
            throws ConnectException, IOException, InterruptedException, CancellationException, ExecutionException;
    
    protected abstract void registerDirectoryImpl(RemoteDirectory directory);

    protected abstract void unregisterDirectoryImpl(String path);

    protected abstract void scheduleRefresh(Collection<String> paths);
    
    protected abstract DirEntry uploadAndRename(File srcFile, String pathToUpload, String pathToRename)
            throws ConnectException, IOException, InterruptedException, ExecutionException, InterruptedException;

    protected abstract boolean canSetAccessCheckType();

    protected abstract void setAccessCheckType(FileSystemProvider.AccessCheckType accessCheckType);

    /** can be null */
    protected abstract FileSystemProvider.AccessCheckType getAccessCheckType();

    /** 
     * Deletes the file, returns parent directory content.
     * Returning parent directory content is for the sake of optimization.
     * For example, fs_server, can do remove and return refreshed content in one call.
     * It can return null if there is no way of doing that more effective than
     * just calling RemoteFileSystemTransport.readDirectory
     * @return parent directory content (can be null - see above)
     */
    protected abstract DirEntryList delete(String path, boolean directory) throws ConnectException, IOException;

    protected void onConnect() {
    }

    protected void onFocusGained() {
    }
    
    protected Warmup createWarmup(String path) {
        return null;
    }                
}
