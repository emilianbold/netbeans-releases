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
import java.io.StringWriter;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.util.NbBundle;

/**
 * Note: public access is needed for tests
 * 
 */
public class SftpTransport extends RemoteFileSystemTransport {

    private final ExecutionEnvironment execEnv;    
    
    private SftpTransport(ExecutionEnvironment execEnv) {
        this.execEnv = execEnv;        
    }
    
    public static SftpTransport getInstance(ExecutionEnvironment execEnv) {
        return new SftpTransport(execEnv);
    }

    @Override
    protected DirEntry stat(String path) 
            throws TimeoutException, ConnectException, InterruptedException, ExecutionException {
        try {
            return stat_or_lstat(path, false, 0);
        } catch (TimeoutException ex) {
            RemoteFileSystemUtils.reportUnexpectedTimeout(ex, path);
            throw ex;
        }
    }

    @Override
    protected DirEntry stat(String path, int timeoutMillis)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {
        return stat_or_lstat(path, false, timeoutMillis);
    }

    @Override
    protected DirEntry lstat(String path) 
            throws TimeoutException, ConnectException, InterruptedException, ExecutionException {
        try {
            return stat_or_lstat(path, true, 0);
        } catch (TimeoutException ex) {
            RemoteFileSystemUtils.reportUnexpectedTimeout(ex, path);
            throw ex;
        }
    }

    @Override
    protected DirEntry lstat(String path, int timeoutMillis)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {
        return stat_or_lstat(path, true, timeoutMillis);
    }

    private DirEntry stat_or_lstat(String path, boolean lstat, int timeoutMillis)
            throws TimeoutException, ConnectException, InterruptedException, ExecutionException {
        
        Future<FileInfoProvider.StatInfo> stat = lstat ?
                FileInfoProvider.lstat(execEnv, path) :
                FileInfoProvider.stat(execEnv, path);

        if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
            throw RemoteExceptions.createConnectException(RemoteFileSystemUtils.getConnectExceptionMessage(execEnv));
        }        
        if (timeoutMillis == 0) {
            return DirEntryImpl.create(stat.get(), execEnv);
        } else {
            return DirEntryImpl.create(stat.get(timeoutMillis, TimeUnit.MILLISECONDS), execEnv);
        }
    }

    @Override
    protected boolean canCopy(String from, String to) {
        return false;
    }

    @Override
    protected DirEntryList copy(String from, String to, 
            Collection<IOException> subdirectoryExceptions) 
            throws ConnectException, InterruptedException, ExecutionException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean canMove(String from, String to) {
        return true;
    }

    @Override
    protected MoveInfo move(String from, String to) 
            throws ConnectException, InterruptedException, ExecutionException {
        Future<FileInfoProvider.StatInfo> f = FileInfoProvider.move(execEnv, from, to);
        f.get();
        String fromParent = PathUtilities.getDirName(from);
        DirEntryList fromList = readDirectory(fromParent == null ? "/" : fromParent); // NOI18N
        String toParent = PathUtilities.getDirName(to);
        DirEntryList toList = readDirectory(toParent == null ? "/" : toParent); // NOI18N
        return new MoveInfo(fromList, toList);
    }

    @Override
    protected DirEntryList readDirectory(String remotePath)
            throws ConnectException, InterruptedException, ExecutionException {
        if (remotePath.length() == 0) {
            remotePath = "/"; //NOI18N
        } else  {
            if (!remotePath.startsWith("/")) { //NOI18N
                throw new IllegalArgumentException("path should be absolute: " + remotePath); // NOI18N
            }
        }
        if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
            throw RemoteExceptions.createConnectException(RemoteFileSystemUtils.getConnectExceptionMessage(execEnv));
        }
        Future<StatInfo[]> res = FileInfoProvider.ls(execEnv, remotePath);
        StatInfo[] infos;
        try {
            infos = res.get();
        } catch (InterruptedException ex) {
            res.cancel(true);
            throw ex;
        }
        List<DirEntry> newEntries = new ArrayList<>(infos.length);
        for (StatInfo statInfo : infos) {
            // filtering of "." and ".." is up to provider now
            newEntries.add(DirEntryImpl.create(statInfo, execEnv));
        }
        return new DirEntryList(newEntries, System.currentTimeMillis());
    }
    
    @Override
    public boolean isValidFast() {
        return true;
    }

    @Override
    protected boolean isValidSlow() {
        return true;
    }

    @Override
    protected boolean needsClientSidePollingRefresh() {
        return true;        
    }

    @Override
    protected boolean canRefreshFast() {
        return false;
    }

    @Override
    protected void refreshFast(String path, boolean expected) {
        throw new UnsupportedOperationException("fast refresh not supported for sftp transport"); //NOI18N
    }
    
    @Override
    protected void registerDirectoryImpl(RemoteDirectory directory) {
        if (RefreshManager.REFRESH_ON_CONNECT && directory.getCache().exists() && ConnectionManager.getInstance().isConnectedTo(execEnv)) {
            // see issue #210125 Remote file system does not refresh directory that wasn't instantiated at connect time
            directory.getFileSystem().getRefreshManager().scheduleRefresh(Arrays.<RemoteFileObjectBase>asList(directory), false);
        }        
    }

    @Override
    protected void unregisterDirectoryImpl(String path) {
        
    }    

    @Override
    protected void scheduleRefresh(Collection<String> paths) {
        RemoteFileSystemManager.getInstance().getFileSystem(execEnv).getRefreshManager().scheduleRefreshExistent(paths, true);
    }

    @Override
    protected DirEntryList delete(String path, boolean directory) throws IOException {
        if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
            throw RemoteExceptions.createConnectException(RemoteFileSystemUtils.getConnectExceptionMessage(execEnv));
        }        
        StringWriter writer = new StringWriter();
        Future<Integer> task;
        if (directory) {
            task = CommonTasksSupport.rmDir(execEnv, path, true, writer);
        } else {
            task = CommonTasksSupport.rmFile(execEnv, path, writer);
        }
        try {
            if (task.get().intValue() != 0) {
                throw RemoteExceptions.createIOException(NbBundle.getMessage(SftpTransport.class,
                        "EXC_CantDelete", RemoteFileObjectBase.getDisplayName(execEnv, path))); // NOI18N
            }
        } catch (InterruptedException ex) {
            throw RemoteExceptions.createInterruptedIOException(ex.getLocalizedMessage(), ex); //NOI18N
        } catch (ExecutionException ex) {
            final String errorText = writer.getBuffer().toString();
            throw RemoteExceptions.createIOException(NbBundle.getMessage(SftpTransport.class, 
                    "EXC_CantDeleteWReason", RemoteFileObjectBase.getDisplayName(execEnv, path), errorText), ex); //NOI18N
        }
        return null;
    }

    @Override
    protected DirEntry uploadAndRename(File srcFile, String pathToUpload, String pathToRename) 
            throws ConnectException, IOException, InterruptedException, ExecutionException, InterruptedException {

        if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
            throw RemoteExceptions.createConnectException(RemoteFileSystemUtils.getConnectExceptionMessage(execEnv));
        }
        
        CommonTasksSupport.UploadParameters params = new CommonTasksSupport.UploadParameters(
                srcFile, execEnv, pathToUpload, pathToRename, -1, false, null);
        Future<CommonTasksSupport.UploadStatus> task = CommonTasksSupport.uploadFile(params);
        CommonTasksSupport.UploadStatus uploadStatus = task.get();
        if (uploadStatus.isOK()) {
            RemoteLogger.getInstance().log(Level.FINEST, "WritingQueue: uploading {0} succeeded", this);
            return DirEntryImpl.create(uploadStatus.getStatInfo(), execEnv);
        } else {
            RemoteLogger.getInstance().log(Level.FINEST, "WritingQueue: uploading {0} failed", this);
            throw RemoteExceptions.createIOException("" + uploadStatus.getError() + " " + uploadStatus.getExitCode()); //NOI18N
        }
    }    

    @Override
    protected boolean canSetAccessCheckType() {
        return false;
    }

    @Override
    protected void setAccessCheckType(FileSystemProvider.AccessCheckType accessCheckType) {
    }

    @Override
    protected FileSystemProvider.AccessCheckType getAccessCheckType() {
        return null;
    }

    @Override
    protected boolean canDeleteOnDisconnect() {
        return false;
    }

    @Override
    protected void deleteOnDisconnect(String[] paths) {
    }
}
