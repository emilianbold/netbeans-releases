/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.impl.fs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.remote.impl.fs.DirectoryStorage.Entry;
import org.netbeans.modules.remote.support.RemoteLogger;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Kvashin
 */
public class RemoteDirectory extends RemoteFileObjectBase {

    public static final String FLAG_FILE_NAME = ".rfs"; // NOI18N

    private Reference<DirectoryStorage> storageRef;

    public RemoteDirectory(RemoteFileSystem fileSystem, ExecutionEnvironment execEnv, 
            FileObject parent, String remotePath, File cache) {
        super(fileSystem, execEnv, parent, remotePath, cache);
    }

    @Override
    public boolean isFolder() {
        return true;
    }

    @Override
    public boolean isData() {
        return false;
    }

    @Override
    public FileObject getFileObject(String name, String ext) {
         return getFileObject(name + '.' + ext); // NOI18N
    }

    /*package*/ boolean canWrite(String childNameExt) throws IOException {
        try {
            DirectoryStorage storage = getDirectoryStorage();
            Entry entry = storage.getEntry(childNameExt);
            return entry.canWrite(execEnv.getUser()); //TODO:rfs - check groups
        } catch (ConnectException ex) {
            return false; // don't report
        } catch (InterruptedException ex) {
            return false; // don't report
        } catch (CancellationException ex) {
            return false; // don't report
        }
    }

    private String removeDoubleSlashes(String path) {
        if (path == null) {
            return null;
        }
        return path.replace("//", "/"); //TODO:rfs remove triple paths, etc
    }

    @Override
    public FileObject getFileObject(String relativePath) {
        relativePath = removeDoubleSlashes(relativePath);
        if (relativePath != null && relativePath.length()  > 0 && relativePath.charAt(0) == '/') { //NOI18N
            relativePath = relativePath.substring(1);
        }
        if (relativePath.endsWith("/")) { // NOI18N
            relativePath = relativePath.substring(0,relativePath.length()-1);
        }
        int slashPos = relativePath.lastIndexOf('/');
        if (slashPos > 0) { // can't be 0 - see the check above
            // relative path contains '/' => delegate to direct parent
            String parentRemotePath = remotePath + '/' + relativePath.substring(0, slashPos); //TODO:rfs: process ../..
            String childNameExt = relativePath.substring(slashPos + 1);
            FileObject parentFileObject = fileSystem.findResource(parentRemotePath);
            if (parentFileObject != null &&  parentFileObject.isFolder()) {
                return parentFileObject.getFileObject(childNameExt);
            } else {
                return null;
            }
        }
        RemoteLogger.assertTrue(slashPos == -1);
        try {
            DirectoryStorage storage = getDirectoryStorage();
            DirectoryStorage.Entry entry = storage.getEntry(relativePath);
            if (entry == null) {
                return null;
            }
            File childCache = new File(cache, entry.getCache());
            String remoteAbsPath = remotePath + '/' + relativePath;
            if (entry.getFileType() == FileType.Directory) {
                return fileSystem.getFactory().createRemoteDirectory(this, remoteAbsPath, childCache);
            } else {
                return fileSystem.getFactory().createRemotePlainFile(this, remoteAbsPath, childCache, entry.getFileType());
            }
        } catch (InterruptedException ex) {
            return null;
        } catch (CancellationException ex) {
            return null;
        } catch (ConnectException ex) {
            // don't report, this just means that we aren't connected
            return null;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    public FileObject[] getChildren() {
        try {
            DirectoryStorage storage = getDirectoryStorage();
            List<DirectoryStorage.Entry> entries = storage.list();
            FileObject[] childrenFO = new FileObject[entries.size()];
            for (int i = 0; i < entries.size(); i++) {
                DirectoryStorage.Entry entry = entries.get(i);
                String childPath = remotePath + '/' + entry.getName(); //NOI18N
                File childCache = new File(cache, entry.getCache());
                if (entry.getFileType() == FileType.Directory) {
                    childrenFO[i] = fileSystem.getFactory().createRemoteDirectory(this, childPath, childCache);
                } else {
                    childrenFO[i] = fileSystem.getFactory().createRemotePlainFile(this, childPath, childCache, entry.getFileType());
                }
            }
            return childrenFO;
        } catch (InterruptedException ex) {
            // don't report, this just means that we aren't connected
        } catch (ConnectException ex) {
            // don't report, this just means that we aren't connected
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (CancellationException ex) {
            // never report CancellationException
        }
        return new FileObject[0];
    }

    private File getStorageFile() {
        return new File(cache, FLAG_FILE_NAME);
    }

    @Override
    protected void ensureSync() throws ConnectException, IOException, InterruptedException, CancellationException {
        getDirectoryStorage();
    }

    private DirectoryStorage getDirectoryStorage() throws
            ConnectException, IOException, InterruptedException, CancellationException {

        DirectoryStorage storage = null;

        // check whether it is cached in memory
        synchronized (this) {
            if (storageRef != null) {
                storage = storageRef.get();
            }
        }
        if (storage != null) {
            return storage;
        }

        File storageFile = getStorageFile();
        storage = new DirectoryStorage(storageFile);

        // try loading from disk
        boolean loaded = false;
        if (storageFile.exists()) {
            Lock lock = RemoteFileSystem.getLock(storageFile).readLock();
            try {
                lock.lock();
                try {
                    storage.load();
                } catch (DirectoryStorage.FormatException e) {
                    RemoteLogger.getInstance().log(Level.FINE, e.getMessage(), e);
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            } finally {
                lock.unlock();
            }
        }

        if (loaded) {
            synchronized (this) {
                if (storageRef != null) {
                    DirectoryStorage s = storageRef.get();
                    if (s != null) {
                        return s;
                    }
                }
                storageRef = new WeakReference<DirectoryStorage>(storage);
            }
            return storage;
        }

        // neither memory nor disk cache helped
        checkConnection(cache, remotePath, true);

        Lock lock = RemoteFileSystem.getLock(cache).writeLock();
        lock.lock();
        try {
            synchronized (this) {
                if (storageRef != null) {
                    DirectoryStorage stor = storageRef.get();
                    if (stor != null) {
                        return stor;
                    }
                }
            }
            if (!cache.exists()) {
                cache.mkdirs();
            }
            if (!cache.exists()) {
                throw new IOException("Can not create cache directory " + cache);
            }
            DirectoryReader directoryReader = new DirectoryReader(execEnv, remotePath);
            RemoteLogger.getInstance().log(Level.FINEST, "Synchronizing remote path {0}{1}{2}", new Object[]{execEnv, ':', remotePath});
            directoryReader.readDirectory();
            fileSystem.incrementDirSyncCount();
            List<DirectoryStorage.Entry> entries = directoryReader.getEntries();
            for (DirectoryStorage.Entry entry : entries) {
                entry.setCache(entry.getName()); //TODO:rfs case sensivity
            }
            storage.setEntries(entries);
            storage.store();
            synchronized (this) {
                storageRef = new WeakReference<DirectoryStorage>(storage);
            }

        } finally {
            lock.unlock();
        }
        return storage;
    }

    void ensureChildSync(RemotePlainFile child) throws
            ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {

        if (child.cache.exists() && child.cache.length() > 0) {
            return;
        }
        checkConnection(child.cache, child.remotePath, false);
        DirectoryStorage storage = getDirectoryStorage();
        Future<Integer> task = CommonTasksSupport.downloadFile(child.remotePath, execEnv, child.cache.getAbsolutePath(), null);
        try {
            int rc = task.get().intValue();
            if (rc == 0) {
                fileSystem.incrementFileCopyCount();
            } else {
                throw new IOException("Can't copy file " + child.cache.getAbsolutePath() + // NOI18N
                        " from " + execEnv + ':' + remotePath + ": rc=" + rc); //NOI18N
            }
        } catch (InterruptedException ex) {
            truncate(child.cache);
            throw ex;
        } catch (ExecutionException ex) {
            truncate(child.cache);
            throw ex;
        }
    }

    private void truncate(File file) throws IOException {
        OutputStream os = new FileOutputStream(file);
        os.close();
    }

    private void checkConnection(File localFile, String remotePath, boolean isDirectory) throws ConnectException {
        if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
            fileSystem.getRemoteFileSupport().addPendingFile(localFile, remotePath, isDirectory);
            throw new ConnectException();
        }
    }

    @Override
    public FileType getType() {
        return FileType.Directory;
    }
    
    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
}
