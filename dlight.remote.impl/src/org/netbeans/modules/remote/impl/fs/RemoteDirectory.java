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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import org.netbeans.modules.remote.support.RemoteLogger;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Kvashin
 */
public class RemoteDirectory extends RemoteFileObjectBase {

    private static final boolean trace = RemoteLogger.getInstance().isLoggable(Level.FINEST);
    private static boolean LS_VIA_SFTP = ! Boolean.getBoolean("remote.parse.ls");

    private Reference<DirectoryStorage> storageRef;
    private static final class RefLock {}
    private final Object refLock = new RefLock();    

    public RemoteDirectory(RemoteFileSystem fileSystem, ExecutionEnvironment execEnv,
            RemoteFileObjectBase parent, String remotePath, File cache) {
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
    public RemoteFileObjectBase getFileObject(String name, String ext) {
         return getFileObject(name + '.' + ext); // NOI18N
    }

    /*package*/ boolean canWrite(String childNameExt) throws IOException, ConnectException {
        try {
            DirectoryStorage storage = getDirectoryStorage();
            DirEntry entry = storage.getEntry(childNameExt);
            return entry != null && entry.canWrite(getExecutionEnvironment()); //TODO:rfs - check groups
        } catch (ConnectException ex) {
            throw ex; // don't report
        } catch (InterruptedIOException ex) {
            RemoteLogger.finest(ex);
            return false; // don't report
        } catch (ExecutionException ex) {
            RemoteLogger.finest(ex);
            return false; // don't report
        } catch (InterruptedException ex) {
            RemoteLogger.finest(ex);
            return false; // don't report
        } catch (CancellationException ex) {
            return false; // don't report
        }
    }

    /*package*/ boolean canRead(String childNameExt) throws IOException {
        try {
            DirectoryStorage storage = getDirectoryStorage();
            DirEntry entry = storage.getEntry(childNameExt);
            return entry != null && entry.canRead(getExecutionEnvironment());
        } catch (ConnectException ex) {
            return false; // don't report
        } catch (InterruptedIOException ex) {
            RemoteLogger.finest(ex);
            return false; // don't report
        } catch (ExecutionException ex) {
            RemoteLogger.finest(ex);
            return false; // don't report
        } catch (InterruptedException ex) {
            RemoteLogger.finest(ex);
            return false; // don't report
        } catch (CancellationException ex) {
            return false; // don't report
        }
    }

    @Override
    public FileObject createData(String name) throws IOException {
        return create(name, false);
    }
    
    @Override
    public FileObject createData(String name, String ext) throws IOException {
        if (ext == null || ext.length() == 0) {
            return create(name, false);
        } else {
            return create(name + '.' + ext, false);
        }
    }

    @Override
    public FileObject createFolder(String name) throws IOException {
        return create(name, true);
    }

    @Override
    protected void postDeleteChild(FileObject child) {
        try {
            DirectoryStorage ds = refreshDirectoryStorage(); // it will fire events itself
        } catch (ConnectException ex) {
            RemoteLogger.getInstance().log(Level.INFO, "Error post removing child " + child, ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);            
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (CancellationException ex) {
            // too late
        }
    }
    
    @Override
    protected void deleteImpl() throws IOException {
        RemoteFileSystemUtils.delete(getExecutionEnvironment(), getPath(), true);
    }

    private FileObject create(String name, boolean directory) throws IOException {
        // Have to comment this out since NB does lots of stuff in the UI thread and I have no way to control this :(
        // RemoteLogger.assertNonUiThread("Remote file operations should not be done in UI thread");
        String path = getPath() + '/' + name;
        if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
            throw new ConnectException("Can not create " + getUrlToReport(path) + ": connection required"); //NOI18N
        }
        ProcessUtils.ExitStatus res;
        if (directory) {
            res = ProcessUtils.execute(getExecutionEnvironment(), "mkdir", path); //NOI18N
        } else {
            String script = String.format("ls \"%s\" || touch \"%s\"", name, name); // NOI18N
            res = ProcessUtils.executeInDir(getPath(), getExecutionEnvironment(), "sh", "-c", script); // NOI18N
            if (res.isOK() && res.error.length() == 0) {
                throw new IOException("Already exists: " + getUrlToReport(path)); // NOI18N
            }
        }
        if (res.isOK()) {
            try {
                refreshImpl();
                FileObject fo = getFileObject(name);
                if (fo == null) {
                    throw new FileNotFoundException("Can not create FileObject " + getUrlToReport(path)); //NOI18N
                }
                if (directory) {
                    fireFileFolderCreatedEvent(getListeners(), new FileEvent(fo));
                } else {
                    fireFileDataCreatedEvent(getListeners(), new FileEvent(fo));
                }
                return fo;
            } catch (ConnectException ex) {
                throw new IOException("Can not create " + path + ": not connected", ex); // NOI18N
            } catch (InterruptedIOException ex) {
                throw new IOException("Can not create " + path + ": interrupted", ex); // NOI18N
            } catch (IOException ex) {
                throw ex;
            } catch (ExecutionException ex) {
                throw new IOException("Can not create " + path + ": exception occurred", ex); // NOI18N
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
        return getExecutionEnvironment().getDisplayName() + ':' + path;
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
            String parentRemotePath = getPath() + '/' + relativePath.substring(0, slashPos); //TODO:rfs: process ../..
            String childNameExt = relativePath.substring(slashPos + 1);
            RemoteFileObjectBase parentFileObject = getFileSystem().findResource(parentRemotePath);
            if (parentFileObject != null &&  parentFileObject.isFolder()) {
                return parentFileObject.getFileObject(childNameExt);
            } else {
                return null;
            }
        }
        if (".".equals(relativePath)) { // NOI18N
            return this;
        } else if ("..".equals(relativePath)) { // NOI18N
            RemoteFileObjectBase parent = getParent();
            return (parent == null) ? this : parent ;
        }
        RemoteLogger.assertTrue(slashPos == -1);
        try {
            DirectoryStorage storage = getDirectoryStorage();
            DirEntry entry = storage.getEntry(relativePath);
            if (entry == null) {
                return null;
            }
            return createFileObject(entry, false);
        } catch (InterruptedException ex) {
            RemoteLogger.finest(ex);
            return null;
        } catch (InterruptedIOException ex) {
            RemoteLogger.finest(ex);
            return null;
        } catch (CancellationException ex) {
            RemoteLogger.finest(ex);
            return null;
        } catch (ExecutionException ex) {
            RemoteLogger.finest(ex);
            return null;
        } catch (ConnectException ex) {
            // don't report, this just means that we aren't connected
            RemoteLogger.finest(ex);
            return null;
        } catch (FileNotFoundException ex) {
            RemoteLogger.finest(ex);
            return null;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            //RemoteLogger.finest(ex);
            return null;
        }
    }
    
    private RemoteFileObjectBase createFileObject(DirEntry entry, boolean fire) {
        File childCache = new File(getCache(), entry.getCache());
        String childPath = getPath() + '/' + entry.getName();
        RemoteFileObjectBase fo;
        if (entry.isDirectory()) {
            fo = getFileSystem().getFactory().createRemoteDirectory(this, childPath, childCache);
        }  else if (entry.isLink()) {
            fo = getFileSystem().getFactory().createRemoteLink(this, childPath, entry.getLinkTarget());
        } else {
            fo = getFileSystem().getFactory().createRemotePlainFile(this, childPath, childCache, FileType.File);
        }
        if (fire) {
            FileEvent e = new FileEvent(fo);
            if (fo instanceof RemoteDirectory) { // fo.isFolder() very slow if it is a link
                fireFileFolderCreatedEvent(getListeners(), e);
            } else if (fo instanceof RemotePlainFile) {
                fireFileDataCreatedEvent(getListeners(), e);
            } else {
                RemoteLogger.getInstance().warning("firing fireFileDataCreatedEvent for a link");
                fireFileDataCreatedEvent(getListeners(), e);
            }
        }
        return fo;
    }

    private RemoteFileObjectBase[] getExistentChildren() throws ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {
        DirectoryStorage storage = getDirectoryStorage();
        List<DirEntry> entries = storage.list();
        List<RemoteFileObjectBase> result = new ArrayList<RemoteFileObjectBase>(entries.size());
        for (DirEntry entry : entries) {
            String path = getPath() + '/' + entry.getName();
            RemoteFileObjectBase fo = getFileSystem().getFactory().getCachedFileObject(path);
            if (fo != null) {
                result.add(fo);
            }
        }
        return result.toArray(new RemoteFileObjectBase[result.size()]);
    }
            
    @Override
    public RemoteFileObjectBase[] getChildren() {
        try {
            DirectoryStorage storage = getDirectoryStorage();
            List<DirEntry> entries = storage.list();
            RemoteFileObjectBase[] childrenFO = new RemoteFileObjectBase[entries.size()];
            for (int i = 0; i < entries.size(); i++) {
                DirEntry entry = entries.get(i);
                childrenFO[i] = createFileObject(entry, false);
            }
            return childrenFO;
        } catch (InterruptedException ex) {
            // don't report, this just means that we aren't connected
            // or just interrupted (for example by FileChooser UI)
            RemoteLogger.finest(ex);
        } catch (InterruptedIOException ex) {
            // don't report, for example FileChooser UI can interrupt us
            RemoteLogger.finest(ex);
        } catch (ExecutionException ex) {
            RemoteLogger.finest(ex);
            // should we report it?
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
        return new RemoteFileObjectBase[0];
    }

    private DirectoryStorage getDirectoryStorage() throws
            ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {
        long time = System.currentTimeMillis();
        try {
            return getDirectoryStorageImpl(false);
        } finally {
            if (trace) {
                trace("getDirectoryStorage for {1} took {0} ms", this, System.currentTimeMillis() - time); // NOI18N
            }
        }
    }
    
    private DirectoryStorage refreshDirectoryStorage() throws
            ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {
        long time = System.currentTimeMillis();
        try {
            return getDirectoryStorageImpl(true);
        } finally {
            if (trace) {
                trace("refreshDirectoryStorage for {1} took {0} ms", this, System.currentTimeMillis() - time); // NOI18N
            }
        }
    }

    private DirectoryStorage getDirectoryStorageImpl(boolean force) throws
            ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {

        if (force && ! ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
            RemoteLogger.getInstance().warning("getDirectoryStorage is called with force == true while host is not connected");
            force = false;
        }

        DirectoryStorage storage = null;

        File storageFile = new File(getCache(), RemoteFileSystem.CACHE_FILE_NAME);

        if (!force) {
            // check whether it is cached in memory
            synchronized (refLock) {
                if (storageRef != null) {
                    storage = storageRef.get();
                }
            }
            if (storage != null) {
                return storage;
            }
        }

        boolean loaded;
        
        if (storage == null) {
            // try loading from disk
            loaded = false;
            storage = new DirectoryStorage(storageFile);
            if (storageFile.exists()) {
                Lock lock = RemoteFileSystem.getLock(getCache()).readLock();
                try {
                    lock.lock();
                    try {
                        storage.load();
                        loaded = true;
                    } catch (FormatException e) {
                        Level level = e.isExpexted() ? Level.FINE : Level.WARNING;
                        RemoteLogger.getInstance().log(level, "Error reading directory cache", e); // NOI18N
                        storageFile.delete();
                    } catch (InterruptedIOException e) {
                        throw e;
                    } catch (FileNotFoundException e) {
                        // this might happen if we switch to different DirEntry implementations, see storageFile.delete() above
                        RemoteLogger.finest(e);
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
        
        if (force) {
            loaded = false;
        }

        if (loaded) {
            synchronized (refLock) {
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

        // neither memory nor disk cache helped
        checkConnection(this, true);

        Lock lock = RemoteFileSystem.getLock(getCache()).writeLock();
        if (trace) { trace("waiting for lock"); } // NOI18N
        lock.lock();
        try {
            if (!force) {
                // in case another thread synchronized content while we were waiting for lock
                synchronized (refLock) {
                    if (storageRef != null) {
                        DirectoryStorage stor = storageRef.get();
                        if (trace) { trace("got storage: {0} -> {1}", storageRef, stor); } // NOI18N
                        if (stor != null) {
                            return stor;
                        }
                    }
                }
            }
            if (!getCache().exists()) {
                getCache().mkdirs();
                if (!getCache().exists()) {
                    throw new IOException("Can not create cache directory " + getCache()); // NOI18N
                }
            }
            DirectoryReader directoryReader = getLsViaSftp() ? 
                    new DirectoryReaderSftp(getExecutionEnvironment(), getPath()) : new DirectoryReaderLs(getExecutionEnvironment(), getPath());
            if (trace) { trace("synchronizing"); } // NOI18N
            try {
                directoryReader.readDirectory();
            }  catch (FileNotFoundException ex) {
                throw ex;
            }  catch (IOException ex) {
                if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
                    // connection was broken while we read directory content -
                    // add notification and return cache if available
                    getFileSystem().getRemoteFileSupport().addPendingFile(this);
                    if (loaded && storage != null) {
                        return storage;
                    } else {
                        throw new ConnectException(ex.getMessage());
                    }
                }
                
            }  catch (ExecutionException ex) {
                if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
                    // connection was broken while we read directory content -
                    // add notification and return cache if available
                    getFileSystem().getRemoteFileSupport().addPendingFile(this);
                    if (loaded && storage != null) {
                        return storage;
                    } else {
                        throw ex;
                    }
                }                
            }
            getFileSystem().incrementDirSyncCount();
            Map<String, List<DirEntry>> dupLowerNames = new HashMap<String, List<DirEntry>>();
            boolean hasDups = false;
            Map<String, DirEntry> newEntries = new HashMap<String, DirEntry>();
            for (DirEntry entry : directoryReader.getEntries()) {
                newEntries.put(entry.getName(), entry);
            }
            boolean changed = false;
            Set<DirEntry> keepCacheNames = new HashSet<DirEntry>();
            for (DirEntry newEntry : newEntries.values()) {
                String cacheName;
                DirEntry oldEntry = storage.getEntry(newEntry.getName());
                if (oldEntry == null) {
                    changed = true;
                    cacheName = RemoteFileSystemUtils.escapeFileName(newEntry.getName());
                } else {
                    if (oldEntry.isSameType(newEntry)) {
                        cacheName = oldEntry.getCache();
                        keepCacheNames.add(newEntry);
                        if (!newEntry.isSameLastModified(oldEntry)) {
                            if (newEntry.isPlainFile()) {
                                changed = true;
                                File entryCache = new File(getCache(), oldEntry.getCache());
                                if (entryCache.exists()) {
                                    if (trace) { trace("removing cache for updated file {0}", entryCache.getAbsolutePath()); } // NOI18N
                                    entryCache.delete();
                                }
                            } else if (!equals(newEntry.getLinkTarget(), oldEntry.getLinkTarget())) {
                                changed = true;
                                getFileSystem().getFactory().setLink(this, getPath() + '/' + newEntry.getName(), newEntry.getLinkTarget());
                            } else if (!newEntry.getAccessAsString().equals(oldEntry.getAccessAsString())) {
                                changed = true;
                            } else if (!newEntry.isSameUser(oldEntry)) {
                                changed = true;
                            } else if (!newEntry.isSameGroup(oldEntry)) {
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
                    List<DirEntry> dupEntries = dupLowerNames.get(lowerCacheName);
                    if (dupEntries == null) {
                        dupEntries = new ArrayList<DirEntry>();
                        dupLowerNames.put(lowerCacheName, dupEntries);
                    } else {
                        hasDups = true;
                    }
                    dupEntries.add(newEntry);
                }
            }
            if (changed || newEntries.size() != storage.size()) {
                // Check for removal
                for (DirEntry oldEntry : storage.list()) {
                    if (!newEntries.containsKey(oldEntry.getName())) {
                        changed = true;
                        invalidate(oldEntry);
                    }
                }
                for (DirEntry newEntry : newEntries.values()) {
                    DirEntry oldEntry = storage.getEntry(newEntry.getName());
                    if (oldEntry == null) {
                        createFileObject(newEntry, true);
                    }
                }
            }

            if (changed) {
                if (hasDups) {
                    for (Map.Entry<String, List<DirEntry>> mapEntry :
                        new ArrayList<Map.Entry<String, List<DirEntry>>>(dupLowerNames.entrySet())) {

                        List<DirEntry> dupEntries = mapEntry.getValue();
                        if (dupEntries.size() > 1) {
                            for (int i = 0; i < dupEntries.size(); i++) {
                                DirEntry entry = dupEntries.get(i);
                                if (keepCacheNames.contains(entry) || i == 0) {
                                    continue; // keep the one that already exists or otherwise 0-th one
                                }
                                for (int j = 0; j < Integer.MAX_VALUE; j++) {
                                    String cacheName = mapEntry.getKey() + '_' + j;
                                    String lowerCacheName = cacheName.toLowerCase();
                                    if (!dupLowerNames.containsKey(lowerCacheName)) {
                                        if (trace) { trace("resolving cache names conflict in {0}: {1} -> {2}", // NOI18N
                                                getCache().getAbsolutePath(), entry.getCache(), cacheName); }
                                        entry.setCache(cacheName);
                                        dupLowerNames.put(lowerCacheName, Collections.singletonList(entry));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                storage.setEntries(newEntries.values());
                storage.store();
            } else {
                storage.touch();
            }
            synchronized (refLock) {
                storageRef = new SoftReference<DirectoryStorage>(storage);
            }
            storageFile.setLastModified(System.currentTimeMillis());
            if (trace) { trace("set lastModified to {0}", storageFile.lastModified()); } // NOI18N
        } finally {
            lock.unlock();
        }
        return storage;
    }

    InputStream _getInputStream(RemotePlainFile child) throws
            ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {
        Lock lock = RemoteFileSystem.getLock(child.getCache()).readLock();
        lock.lock();
        try {
            if (child.getCache().exists()) {
                return new FileInputStream(child.getCache());
            }
        } finally {
            lock.unlock();
        }
        checkConnection(child, true);
        DirectoryStorage storage = getDirectoryStorage(); // do we need this?
        return new CachedRemoteInputStream(child, getExecutionEnvironment());
    }
    
    void ensureChildSync(RemotePlainFile child) throws
            ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {

        Lock lock = RemoteFileSystem.getLock(child.getCache()).readLock();
        lock.lock();
        try {
            if (child.getCache().exists()) {
                return;
            }
        } finally {
            lock.unlock();
        }
        checkConnection(child, true);
        DirectoryStorage storage = getDirectoryStorage(); // do we need this?
        lock = RemoteFileSystem.getLock(child.getCache()).writeLock();
        lock.lock();
        try {
            if (child.getCache().exists()) {
                return;
            }
            final File cacheParentFile = child.getCache().getParentFile();
            if (!cacheParentFile.exists()) {
                cacheParentFile.mkdirs();
                if (!cacheParentFile.exists()) {
                    throw new IOException("Unable to create parent firectory " + cacheParentFile.getAbsolutePath()); //NOI18N
                }
            }
            Future<Integer> task = CommonTasksSupport.downloadFile(child.getPath(), getExecutionEnvironment(), child.getCache().getAbsolutePath(), null);
            int rc = task.get().intValue();
            if (rc == 0) {
                getFileSystem().incrementFileCopyCount();
            } else {
                throw new IOException("Can't copy file " + child.getCache().getAbsolutePath() + // NOI18N
                        " from " + getExecutionEnvironment() + ':' + getPath() + ": rc=" + rc); //NOI18N
            }
        } catch (InterruptedException ex) {
            child.getCache().delete();
            throw ex;
        } catch (ExecutionException ex) {
            child.getCache().delete();
            throw ex;
        } finally {
            lock.unlock();
        }
    }

    private void checkConnection(RemoteFileObjectBase fo, boolean throwConnectException) throws ConnectException {
        if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
            getFileSystem().getRemoteFileSupport().addPendingFile(fo);
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

    private void invalidate(DirEntry oldEntry) {
        FileObject fo = getFileSystem().getFactory().invalidate(getPath() + '/' + oldEntry.getName());
        File oldEntryCache = new File(getCache(), oldEntry.getCache());
        removeFile(oldEntryCache);
        if (fo != null) {
            fireFileDeletedEvent(getListeners(), new FileEvent(fo));
        }
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
                        File childCache = new File(pathname, RemoteFileSystem.CACHE_FILE_NAME);
                        setStorageTimestamp(childCache, timestamp, true);
                    }
                    return false;
                }
            });
        }
    }

    @Override
    protected void refreshImpl() throws ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {
        refreshDirectoryStorage();
        for (RemoteFileObjectBase child : getExistentChildren()) {
            child.refreshImpl();
        }
    }
    
    private void trace(String message, Object... args) {
        if (trace) {
            message = "SYNC [" + getPath() + "][" + System.identityHashCode(this) + "][" + Thread.currentThread().getId() + "]: " + message; // NOI18N
            RemoteLogger.getInstance().log(Level.FINEST, message, args);
        }
    }

    private static boolean equals(String s1, String s2) {
        return (s1 == null) ? (s2 == null) : s1.equals(s2);
    }

    private DirEntry getChildEntry(RemoteFileObjectBase child) {
        try {
            DirectoryStorage directoryStorage = getDirectoryStorage();
            if (directoryStorage != null) {
                DirEntry entry = directoryStorage.getEntry(child.getNameExt());
                if (entry != null) {
                    return entry;
                } else {
                    RemoteLogger.getInstance().log(Level.INFO, "Not found entry for file {0}", child); // NOI18N
                }
            }
        } catch (ConnectException ex) {
            RemoteLogger.finest(ex);
        } catch (IOException ex) {
            RemoteLogger.finest(ex);
        } catch (ExecutionException ex) {
            RemoteLogger.finest(ex);
        } catch (InterruptedException ex) {
            RemoteLogger.finest(ex);
        } catch (CancellationException ex) {
            RemoteLogger.finest(ex);
        }
        return null;
    }

    long getSize(RemoteFileObjectBase child) {
        DirEntry childEntry = getChildEntry(child);
        if (childEntry != null) {
            return childEntry.getSize();
        }
        return 0;
    }

    /*package*/ Date lastModified(RemoteFileObjectBase child) {
        DirEntry childEntry = getChildEntry(child);
        if (childEntry != null) {
            return childEntry.getLastModified();
        }
        return new Date(0); // consistent with File.lastModified(), which returns 0 for inexistent file
    }
    
    /*package*/ static boolean getLsViaSftp() {
        return LS_VIA_SFTP;
    }
    
    /*package*/ static void testSetLsViaSftp(boolean value) {
        LS_VIA_SFTP = value;
    }
}
