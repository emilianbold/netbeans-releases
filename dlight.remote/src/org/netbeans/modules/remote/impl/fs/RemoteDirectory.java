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
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.remote.impl.fs.DirectoryStorage.Entry;
import org.netbeans.modules.remote.support.RemoteLogger;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Kvashin
 */
public class RemoteDirectory extends RemoteFileObjectBase {

    public static final String FLAG_FILE_NAME = ".rfs"; // NOI18N
    private static final boolean trace = RemoteLogger.getInstance().isLoggable(Level.FINEST);

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
            DirectoryStorage storage = getDirectoryStorage(true);
            Entry entry = storage.getEntry(childNameExt);
            return entry != null && entry.canWrite(execEnv.getUser()); //TODO:rfs - check groups
        } catch (ConnectException ex) {
            return false; // don't report
        } catch (InterruptedException ex) {
            return false; // don't report
        } catch (CancellationException ex) {
            return false; // don't report
        }
    }

    /*package*/ boolean canRead(String childNameExt) throws IOException {
        try {
            DirectoryStorage storage = getDirectoryStorage(true);
            Entry entry = storage.getEntry(childNameExt);
            return entry != null && entry.canRead(execEnv.getUser()); //TODO:rfs - check groups
        } catch (ConnectException ex) {
            return false; // don't report
        } catch (InterruptedException ex) {
            return false; // don't report
        } catch (CancellationException ex) {
            return false; // don't report
        }
    }

    @Override
    public FileObject createData(String name, String ext) throws IOException {
        return create(name + '.' + ext, false);
    }

    @Override
    public FileObject createFolder(String name) throws IOException {
        return create(name, true);
    }

    private FileObject create(String name, boolean directory) throws IOException {
        RemoteLogger.assertNonUiThread("Remote file operations should not be done in UI thread");
        String path = remotePath + '/' + name;
        if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
            throw new ConnectException("Can not create " + getUrlToReport(path) + ": connection required"); //NOI18N
        }
        ProcessUtils.ExitStatus res;
        if (directory) {
            res = ProcessUtils.execute(execEnv, "mkdir", path); //NOI18N
        } else {
            String keyword = "exists"; // NOI18N
            String script = String.format("test -e %s && echo \"%s\" || touch %s", name, keyword, name); // NOI18N
            res = ProcessUtils.executeInDir(remotePath, execEnv, "sh", "-c", script); // NOI18N
            if (res.isOK() && keyword.equals(res.output)) {
                throw new IOException("Already exists: " + getUrlToReport(path)); // NOI18N
            }
        }
        if (res.isOK()) {
            try {
                refreshImpl(false);
                ensureSync();
                FileObject fo = getFileObject(name);
                if (fo == null) {
                    throw new FileNotFoundException("Can not create FileObject " + getUrlToReport(path)); //NOI18N
                }
                return fo;
            } catch (ConnectException ex) {
                throw new IOException("Can not create " + path, ex); // NOI18N
            } catch (InterruptedIOException ex) {
                throw new IOException("Can not create " + path + ": interrupted", ex); // NOI18N
            } catch (IOException ex) {
                throw ex;
            } catch (InterruptedException ex) {
                throw new IOException("Can not create " + path + ": interrupted", ex); // NOI18N
            } catch (CancellationException ex) {
                throw new IOException("Can not create " + path + ": cancelled", ex); // NOI18N
            }
        } else {
            throw new IOException("Can not create " + getUrlToReport(path) + ": " + res.error); // NOI18N
        }
    }

    private String getUrlToReport(String path) {
        return execEnv.getDisplayName() + ':' + path;
    }

    private String removeDoubleSlashes(String path) {
        if (path == null) {
            return null;
        }
        return path.replace("//", "/"); //TODO:rfs remove triple paths, etc
    }

    @Override
    public RemoteFileObjectBase getFileObject(String relativePath) {
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
            RemoteFileObjectBase parentFileObject = fileSystem.findResource(parentRemotePath);
            if (parentFileObject != null &&  parentFileObject.isFolder()) {
                return parentFileObject.getFileObject(childNameExt);
            } else {
                return null;
            }
        }
        if (".".equals(relativePath)) { // NOI18N
            return this;
        } else if ("..".equals(relativePath)) { // NOI18N
            return getParent();
        }
        RemoteLogger.assertTrue(slashPos == -1);
        try {
            DirectoryStorage storage = getDirectoryStorage(true);
            DirectoryStorage.Entry entry = storage.getEntry(relativePath);
            if (entry == null) {
                return null;
            }
            File childCache = new File(cache, entry.getCache());
            String remoteAbsPath = remotePath + '/' + relativePath;
            if (entry.getFileType() == FileType.Directory) {
                return fileSystem.getFactory().createRemoteDirectory(this, remoteAbsPath, childCache);
            }  else if (entry.getFileType() == FileType.Symlink) {
                return fileSystem.getFactory().createRemoteLink(this, remoteAbsPath, childCache, entry.getLink());
            } else {
                return fileSystem.getFactory().createRemotePlainFile(this, remoteAbsPath, childCache, entry.getFileType());
            }
        } catch (InterruptedException ex) {
            RemoteLogger.finest(ex);
            return null;
        } catch (InterruptedIOException ex) {
            RemoteLogger.finest(ex);
            return null;
        } catch (CancellationException ex) {
            RemoteLogger.finest(ex);
            return null;
        } catch (ConnectException ex) {
            // don't report, this just means that we aren't connected
            RemoteLogger.finest(ex);
            return null;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            //RemoteLogger.finest(ex);
            return null;
        }
    }

    @Override
    public FileObject[] getChildren() {
        try {
            DirectoryStorage storage = getDirectoryStorage(true);
            List<DirectoryStorage.Entry> entries = storage.list();
            FileObject[] childrenFO = new FileObject[entries.size()];
            for (int i = 0; i < entries.size(); i++) {
                DirectoryStorage.Entry entry = entries.get(i);
                String childPath = remotePath + '/' + entry.getName(); //NOI18N
                File childCache = new File(cache, entry.getCache());
                if (entry.getFileType() == FileType.Directory) {
                    childrenFO[i] = fileSystem.getFactory().createRemoteDirectory(this, childPath, childCache);
                } else if(entry.getFileType() == FileType.Symlink) {
                    childrenFO[i] = fileSystem.getFactory().createRemoteLink(this, childPath, childCache, entry.getLink());
                } else {
                    childrenFO[i] = fileSystem.getFactory().createRemotePlainFile(this, childPath, childCache, entry.getFileType());
                }
            }
            return childrenFO;
        } catch (InterruptedException ex) {
            // don't report, this just means that we aren't connected
            // or just interrupted (for example by FileChooser UI)
            RemoteLogger.finest(ex);
        } catch (InterruptedIOException ex) {
            // don't report, for example FileChooser UI can interrupt us
            RemoteLogger.finest(ex);
        } catch (ConnectException ex) {
            // don't report, this just means that we aren't connected
            RemoteLogger.finest(ex);
        } catch (FileNotFoundException ex) {
            RemoteLogger.finest(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (CancellationException ex) {
            // never report CancellationException
            RemoteLogger.finest(ex);
        }
        return new FileObject[0];
    }

    @Override
    protected void ensureSync() throws ConnectException, IOException, InterruptedException, CancellationException {
        getDirectoryStorage(true);
    }

    private DirectoryStorage getDirectoryStorage(boolean sync) throws
            ConnectException, IOException, InterruptedException, CancellationException {
        long time = System.currentTimeMillis();
        try {
            return getDirectoryStorageImpl(sync);
        } finally {
            if (trace) {
                trace("sync took {0} ms", System.currentTimeMillis() - time); // NOI18N
            }
        }
    }

    private DirectoryStorage getDirectoryStorageImpl(boolean sync) throws
            ConnectException, IOException, InterruptedException, CancellationException {

        DirectoryStorage storage = null;

        File storageFile = new File(cache, FLAG_FILE_NAME);

        // check whether it is cached in memory
        synchronized (this) {
            if (storageRef != null) {
                storage = storageRef.get();
            }
        }
        if (storage != null) {
            if (storageFile.lastModified() >= fileSystem.getDirtyTimestamp()) {
                trace("timestamps check passed; returning cached storage"); // NOI18N
                return storage;
            } else if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                trace("timestamps check NOT passed, but the host is offline; returning cached storage"); // NOI18N
                fileSystem.getRemoteFileSupport().addPendingFile(this);
                return storage;
            } else if (!sync) {
                trace("timestamps check NOT passed, but no sync is required; returning cached storage"); // NOI18N
                fileSystem.getRemoteFileSupport().addPendingFile(this);
                return storage;
            }
        }

        if (trace && storageFile.lastModified() < fileSystem.getDirtyTimestamp()) {
            trace("dirty directory: file={0} fs={1}", storageFile.lastModified(), fileSystem.getDirtyTimestamp()); // NOI18N
        }

        boolean loaded;

        if (storage == null) {
            // try loading from disk
            loaded = false;
            storage = new DirectoryStorage(storageFile);
            if (storageFile.exists()) {
                Lock lock = RemoteFileSystem.getLock(storageFile).readLock();
                try {
                    lock.lock();
                    try {
                        storage.load();
                        loaded = true;
                    } catch (DirectoryStorage.FormatException e) {
                        Level level = e.isExpexted() ? Level.FINE : Level.WARNING;
                        RemoteLogger.getInstance().log(level, "Error reading directory cache", e); // NOI18N
                        storageFile.delete();
                    } catch (InterruptedIOException e) {
                        throw e;
                    } catch (IOException e) {
                        Exceptions.printStackTrace(e);
                    }
                } finally {
                    lock.unlock();
                }
            }
        } else {
            loaded = true;
        }

        if (loaded) {
            boolean ok = false;
            if (storageFile.lastModified() >= fileSystem.getDirtyTimestamp()) {
                trace("timestamps check passed; returning just loaded storage"); // NOI18N
                ok = true;
            } else if(!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                trace("timestamps check NOT passed, but the host is offline; returning just loaded storage"); // NOI18N
                ok = true;
                fileSystem.getRemoteFileSupport().addPendingFile(this);
            } else if (!sync) {
                trace("timestamps check NOT passed, but no sync is required; returning just loaded storage"); // NOI18N
                ok = true;
                fileSystem.getRemoteFileSupport().addPendingFile(this);
            }
            if (ok) {
                synchronized (this) {
                    if (storageRef != null) {
                        DirectoryStorage s = storageRef.get();
                        if (s != null) {
                            if (trace) { trace("returning storage that was loaded by other thread"); } // NOI18N
                            return s;
                        }
                    }
                    storageRef = new SoftReference<DirectoryStorage>(storage);
                }
                if (trace) { trace("returning just loaded storage"); } // NOI18N
                return storage;
            }
        }

        // neither memory nor disk cache helped
        checkConnection(this, true);

        Lock lock = RemoteFileSystem.getLock(cache).writeLock();
        if (trace) { trace("waiting for lock"); } // NOI18N
        lock.lock();
        try {
            // in case another thread synchronized content while we were waiting for lock
            synchronized (this) {
                if (trace) { trace("checking storageRef and timestamp: ref={0} file={1} fs: {2}", storageRef, storageFile.lastModified(), fileSystem.getDirtyTimestamp()); } // NOI18N
                if (storageRef != null && storageFile.lastModified() >= fileSystem.getDirtyTimestamp()) {
                    DirectoryStorage stor = storageRef.get();
                    if (trace) { trace("got storage: {0} -> {1}", storageRef, stor); } // NOI18N
                    if (stor != null) {
                        return stor;
                    }
                }
            }
            if (!cache.exists()) {
                cache.mkdirs();
            }
            if (!cache.exists()) {
                throw new IOException("Can not create cache directory " + cache); // NOI18N
            }
            DirectoryReader directoryReader = new DirectoryReader(execEnv, remotePath);
            if (trace) { trace("synchronizing"); } // NOI18N
            try {
                directoryReader.readDirectory();
            }  catch (FileNotFoundException ex) {
                throw ex;
            }  catch (IOException ex) {
                if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                    // connection was broken while we read directory content -
                    // add notification and return cache if available
                    fileSystem.getRemoteFileSupport().addPendingFile(this);
                    if (loaded && storage != null) {
                        return storage;
                    }
                }
                throw ex;
            }
            fileSystem.incrementDirSyncCount();
            Map<String, List<DirectoryStorage.Entry>> dupLowerNames = new HashMap<String, List<DirectoryStorage.Entry>>();
            boolean hasDups = false;
            Map<String, DirectoryStorage.Entry> entries = new HashMap<String, DirectoryStorage.Entry>();
            for (DirectoryStorage.Entry entry : directoryReader.getEntries()) {
                entries.put(entry.getName(), entry);
            }
            boolean changed = false;
            Set<DirectoryStorage.Entry> keepCacheNames = new HashSet<DirectoryStorage.Entry>();
            for (DirectoryStorage.Entry newEntry : entries.values()) {
                String cacheName;
                DirectoryStorage.Entry oldEntry = storage.getEntry(newEntry.getName());
                if (oldEntry == null) {
                    changed = true;
                    cacheName = RemoteFileSystemUtils.escapeFileName(newEntry.getName());
                } else {
                    if (oldEntry.getFileType() == newEntry.getFileType()) {
                        cacheName = oldEntry.getCache();
                        keepCacheNames.add(newEntry);
                        if (!newEntry.getTimestamp().equals(oldEntry.getTimestamp())) {
                            if (newEntry.getFileType() == FileType.File) {
                                changed = true;
                                File entryCache = new File(cache, oldEntry.getCache());
                                if (entryCache.exists()) {
                                    if (trace) { trace("removing cache for updated file {0}", entryCache.getAbsolutePath()); } // NOI18N
                                    entryCache.delete();
                                }
                            } else if (!equals(newEntry.getLink(), oldEntry.getLink())) {
                                changed = true;
                                fileSystem.getFactory().setLink(this, remotePath + '/' + newEntry.getName(), newEntry.getLink());
                            } else if (!newEntry.getAccessAsString().equals(oldEntry.getAccessAsString())) {
                                changed = true;
                            } else if (!newEntry.getUser().equals(oldEntry.getUser())) {
                                changed = true;
                            } else if (!newEntry.getGroup().equals(oldEntry.getGroup())) {
                                changed = true;
                            } else if (newEntry.getSize() != oldEntry.getSize()) {
                                changed = true;
                            }
                        }
                    } else {
                        changed = true;
                        invalidate(oldEntry);
                        cacheName = RemoteFileSystemUtils.escapeFileName(newEntry.getName());
                    }
                }
                newEntry.setCache(cacheName);
                if (!RemoteFileSystemUtils.isSystemCaseSensitive()) {
                    String lowerCacheName = newEntry.getCache().toLowerCase();
                    List<DirectoryStorage.Entry> dupEntries = dupLowerNames.get(lowerCacheName);
                    if (dupEntries == null) {
                        dupEntries = new ArrayList<Entry>();
                        dupLowerNames.put(lowerCacheName, dupEntries);
                    } else {
                        hasDups = true;
                    }
                    dupEntries.add(newEntry);
                }
            }
            if (changed || entries.size() != storage.size()) {
                // Check for removal
                for (DirectoryStorage.Entry oldEntry : storage.list()) {
                    if (!entries.containsKey(oldEntry.getName())) {
                        changed = true;
                        invalidate(oldEntry);
                    }
                }
            }

            if (changed) {
                if (hasDups) {
                    for (Map.Entry<String, List<DirectoryStorage.Entry>> mapEntry :
                        new ArrayList<Map.Entry<String, List<DirectoryStorage.Entry>>>(dupLowerNames.entrySet())) {

                        List<DirectoryStorage.Entry> dupEntries = mapEntry.getValue();
                        if (dupEntries.size() > 1) {
                            for (int i = 0; i < dupEntries.size(); i++) {
                                DirectoryStorage.Entry entry = dupEntries.get(i);
                                if (keepCacheNames.contains(entry) || i == 0) {
                                    continue; // keep the one that already exists or otherwise 0-th one
                                }
                                for (int j = 0; j < Integer.MAX_VALUE; j++) {
                                    String cacheName = mapEntry.getKey() + '_' + j;
                                    String lowerCacheName = cacheName.toLowerCase();
                                    if (!dupLowerNames.containsKey(lowerCacheName)) {
                                        if (trace) { trace("resolving cache names conflict in {0}: {1} -> {2}", // NOI18N
                                                cache.getAbsolutePath(), entry.getCache(), cacheName); }
                                        entry.setCache(cacheName);
                                        dupLowerNames.put(lowerCacheName, Collections.singletonList(entry));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                storage.setEntries(entries.values());
                storage.store();
            }
            synchronized (this) {
                storageRef = new SoftReference<DirectoryStorage>(storage);
            }
            storageFile.setLastModified(System.currentTimeMillis());
            if (trace) { trace("set lastModified to {0}", storageFile.lastModified()); } // NOI18N
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
        checkConnection(child, true);
        DirectoryStorage storage = getDirectoryStorage(true);
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

    private void checkConnection(RemoteFileObjectBase fo, boolean throwConnectException) throws ConnectException {
        if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
            fileSystem.getRemoteFileSupport().addPendingFile(fo);
            if (throwConnectException) {
                throw new ConnectException();
            }
        }
    }

    @Override
    public FileType getType() {
        return FileType.Directory;
    }

    public final InputStream getInputStream() throws FileNotFoundException {
        throw new FileNotFoundException(getPath());
    }

    @Override
    public final OutputStream getOutputStream(final FileLock lock) throws IOException {
        throw new IOException(getPath());
    }

    private void invalidate(DirectoryStorage.Entry oldEntry) {
        fileSystem.getFactory().invalidate(remotePath + '/' + oldEntry.getName());
        File oldEntryCache = new File(cache, oldEntry.getCache());
        removeFile(oldEntryCache);
    }

    private void removeFile(File cache) {
        if (cache.isDirectory()) {
            for (File child : cache.listFiles()) {
                removeFile(child);
            }
        }
        cache.delete();
    }

    private static void setStorageTimestamp(File cache, final long timestamp, boolean recursive) {
        cache.setLastModified(timestamp);
        if (recursive && cache.exists()) {
            // no need to gather all files into array - process just in filter
            cache.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    if (pathname.isDirectory()) {
                        File childCache = new File(pathname, FLAG_FILE_NAME);
                        setStorageTimestamp(childCache, timestamp, true);
                    }
                    return false;
                }
            });
        }
    }

    protected void refreshImpl(boolean recursive) {
        final long timestamp = fileSystem.getDirtyTimestamp() - 1;
        trace("setting last modified to {0}", timestamp); // NOI18N
        setStorageTimestamp(new File(cache, FLAG_FILE_NAME), timestamp, true);
    }

    @Override
    public void refresh(boolean expected) {
        refreshImpl(true);
    }

    @Override
    public void refresh() {
        refreshImpl(true);
    }

    private void trace(String message, Object... args) {
        if (trace) {
            message = "SYNC [" + remotePath + "][" + System.identityHashCode(this) + "][" + Thread.currentThread().getId() + "]: " + message; // NOI18N
            RemoteLogger.getInstance().log(Level.FINEST, message, args);
        }
    }

    private static boolean equals(String s1, String s2) {
        return (s1 == null) ? (s2 == null) : s1.equals(s2);
    }
}
