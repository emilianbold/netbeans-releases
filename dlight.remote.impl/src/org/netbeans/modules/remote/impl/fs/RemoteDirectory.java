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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.ConnectException;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo.FileType;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider.FilesystemInterceptor;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Kvashin
 */
public class RemoteDirectory extends RemoteFileObjectBase {

    private static final boolean trace = Boolean.getBoolean("cnd.remote.directory.trace"); //NOI18N

    private Reference<DirectoryStorage> storageRef = new SoftReference<DirectoryStorage>(null);
    private Reference<MagicCache> magicCache = new SoftReference<MagicCache>(null);

    private static final class RefLock {}
    private final Object refLock = new RefLock();    

    private static final class MagicLock {}
    private final Object magicLock = new MagicLock();    

    /*package*/ RemoteDirectory(RemoteFileObject wrapper, RemoteFileSystem fileSystem, ExecutionEnvironment execEnv,
            RemoteFileObjectBase parent, String remotePath, File cache) {
        super(wrapper, fileSystem, execEnv, parent, remotePath, cache);
        if (RefreshManager.REFRESH_ON_CONNECT && cache.exists() && ConnectionManager.getInstance().isConnectedTo(execEnv)) {
            // see issue #210125 Remote file system does not refresh directory that wasn't instantiated at connect time
            fileSystem.getRefreshManager().scheduleRefresh(Arrays.<RemoteFileObjectBase>asList(this), false);
        }
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
    public RemoteFileObject getFileObject(String name, String ext, Set<String> antiLoop) {
         return getFileObject(composeName(name, ext), antiLoop);
    }

    private DirEntry getEntry(String childNameExt) throws IOException {
        try {
            DirectoryStorage storage = getDirectoryStorage(childNameExt);
            DirEntry entry = storage.getValidEntry(childNameExt);
            return entry;
        } catch (ConnectException ex) {
            throw ex;
        } catch (InterruptedIOException ex) {
            RemoteLogger.finest(ex, this);
            return null; // don't report
        } catch (ExecutionException ex) {
            RemoteLogger.finest(ex, this);
            return null; // don't report
        } catch (InterruptedException ex) {
            RemoteLogger.finest(ex, this);
            return null; // don't report
        } catch (CancellationException ex) {
            return null; // don't report
        }
    }
    
    /*package*/ boolean canWrite(String childNameExt) throws IOException, ConnectException {
            DirEntry entry = getEntry(childNameExt);
            return entry != null && entry.canWrite(getExecutionEnvironment()); //TODO:rfs - check groups
    }

    /*package*/ boolean canRead(String childNameExt) throws IOException {
        DirEntry entry = getEntry(childNameExt);
        return entry != null && entry.canRead(getExecutionEnvironment());
    }

    /*package*/ boolean canExecute(String childNameExt) throws IOException {
        DirEntry entry = getEntry(childNameExt);
        return entry != null && entry.canExecute(getExecutionEnvironment());
    }

    @Override
    public RemoteFileObject createDataImpl(String name, String ext, RemoteFileObjectBase orig) throws IOException {
        return create(composeName(name, ext), false, orig);
    }

    @Override
    public RemoteFileObject createFolderImpl(String name, RemoteFileObjectBase orig) throws IOException {
        return create(name, true, orig);
    }

    @Override
    protected void postDeleteChild(FileObject child) {
        try {
            DirectoryStorage ds = refreshDirectoryStorage(child.getNameExt(), false); // it will fire events itself
        } catch (ConnectException ex) {
            RemoteLogger.getInstance().log(Level.INFO, "Error post removing child " + child, ex);
        } catch (IOException ex) {
            RemoteLogger.finest(ex, this);
        } catch (ExecutionException ex) {
            RemoteLogger.finest(ex, this);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            RemoteLogger.finest(ex, this);
        } catch (CancellationException ex) {
            // too late
        }
    }
    
    @Override
    protected boolean deleteImpl(FileLock lock) throws IOException {
        return RemoteFileSystemUtils.delete(getExecutionEnvironment(), getPath(), true);
    }

    private RemoteFileObject create(String name, boolean directory, RemoteFileObjectBase orig) throws IOException {
        // Have to comment this out since NB does lots of stuff in the UI thread and I have no way to control this :(
        // RemoteLogger.assertNonUiThread("Remote file operations should not be done in UI thread");
        String path = getPath() + '/' + name;
        if (name.contains("\\") || name.contains("/")) { //NOI18N
            throw new IOException("Cannot create file "+path); //NOI18N
        }
        if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
            throw new ConnectException("Can not create " + getUrlToReport(path) + ": connection required"); //NOI18N
        }
        if (USE_VCS) {
            FilesystemInterceptorProvider.FilesystemInterceptor interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(getFileSystem());
            if (interceptor != null) {
                interceptor.beforeCreate(FilesystemInterceptorProvider.toFileProxy(orig.getOwnerFileObject()), name, directory);
            }
        }
        ProcessUtils.ExitStatus res;
        if (directory) {
            res = ProcessUtils.execute(getExecutionEnvironment(), "mkdir", path); //NOI18N
        } else {
            String script = String.format("ls \"%s\" || touch \"%s\"", name, name); // NOI18N
            res = ProcessUtils.executeInDir(getPath(), getExecutionEnvironment(), "sh", "-c", script); // NOI18N
            if (res.isOK() && res.error.length() == 0) {
                creationFalure(name, directory, orig);
                throw new IOException("Already exists: " + getUrlToReport(path)); // NOI18N
            }
        }
        if (res.isOK()) {
            try {
                refreshDirectoryStorage(name, false);
                RemoteFileObject fo = getFileObject(name, new HashSet<String>());
                if (fo == null) {
                    creationFalure(name, directory, orig);
                    throw new FileNotFoundException("Can not create FileObject " + getUrlToReport(path)); //NOI18N
                }
                if (USE_VCS) {
                    FilesystemInterceptorProvider.FilesystemInterceptor interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(getFileSystem());
                    if (interceptor != null) {
                        if (this == orig) {
                            interceptor.createSuccess(FilesystemInterceptorProvider.toFileProxy(fo));
                        } else {
                            RemoteFileObject originalFO = orig.getFileObject(name, new HashSet<String>());
                            if (originalFO == null) {
                                throw new FileNotFoundException("Can not create FileObject " + getUrlToReport(path)); //NOI18N
                            }
                            interceptor.createSuccess(FilesystemInterceptorProvider.toFileProxy(originalFO));
                        }
                    }
                }
                return fo;
            } catch (ConnectException ex) {
                creationFalure(name, directory, orig);
                throw new IOException("Can not create " + path + ": not connected", ex); // NOI18N
            } catch (InterruptedIOException ex) {
                creationFalure(name, directory, orig);
                throw new IOException("Can not create " + path + ": interrupted", ex); // NOI18N
            } catch (IOException ex) {
                creationFalure(name, directory, orig);
                throw ex;
            } catch (ExecutionException ex) {
                creationFalure(name, directory, orig);
                throw new IOException("Can not create " + path + ": exception occurred", ex); // NOI18N
            } catch (InterruptedException ex) {
                creationFalure(name, directory, orig);
                throw new IOException("Can not create " + path + ": interrupted", ex); // NOI18N
            } catch (CancellationException ex) {
                creationFalure(name, directory, orig);
                throw new IOException("Can not create " + path + ": cancelled", ex); // NOI18N
            }
        } else {
            creationFalure(name, directory, orig);
            throw new IOException("Can not create " + getUrlToReport(path) + ": " + res.error); // NOI18N
        }
    }
    
    private void creationFalure(String name, boolean directory, RemoteFileObjectBase orig) {
        if (USE_VCS) {
            FilesystemInterceptorProvider.FilesystemInterceptor interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(getFileSystem());
            if (interceptor != null) {
                interceptor.createFailure(FilesystemInterceptorProvider.toFileProxy(getOwnerFileObject()), name, directory);
            }
        }
    }

    private String getUrlToReport(String path) {
        return getExecutionEnvironment().getDisplayName() + ':' + path;
    }
    
    @Override
    public RemoteFileObject getFileObject(String relativePath, Set<String> antiLoop) {
        relativePath = PathUtilities.normalizeUnixPath(relativePath);
        if ("".equals(relativePath)) { // NOI18N
            return getOwnerFileObject();
        }
        if (relativePath.startsWith("..")) { //NOI18N
            String absPath = getPath() + '/' + relativePath;
            absPath = PathUtilities.normalizeUnixPath(absPath);
            return getFileSystem().findResource(absPath, antiLoop);
        }
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
            if (antiLoop != null) {
                String absPath = getPath() + '/' + relativePath;
                if (antiLoop.contains(absPath)) {
                    return null;
                }
                antiLoop.add(absPath);
            }
            String childNameExt = relativePath.substring(slashPos + 1);
            RemoteFileObject parentFileObject = getFileSystem().findResource(parentRemotePath, antiLoop);
            if (parentFileObject != null &&  parentFileObject.isFolder()) {
                RemoteFileObject result = parentFileObject.getFileObject(childNameExt);
                return result;
            } else {
                return null;
            }
        }
        RemoteLogger.assertTrue(slashPos == -1);
        try {
            DirectoryStorage storage = getDirectoryStorage(relativePath);
            DirEntry entry = storage.getValidEntry(relativePath);
            if (entry == null) {
                return null;
            }
            return getFileSystem().getFactory().createFileObject(this, entry).getOwnerFileObject();
        } catch (InterruptedException ex) {
            RemoteLogger.finest(ex, this);
            return null;
        } catch (InterruptedIOException ex) {
            RemoteLogger.finest(ex, this);
            return null;
        } catch (CancellationException ex) {
            RemoteLogger.finest(ex, this);
            return null;
        } catch (ExecutionException ex) {
            RemoteLogger.finest(ex, this);
            return null;
        } catch (ConnectException ex) {
            // don't report, this just means that we aren't connected
            setFlag(CONNECTION_ISSUES, true);
            RemoteLogger.finest(ex, this);
            return null;
        } catch (FileNotFoundException ex) {
            RemoteLogger.finest(ex, this);
            return null;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            //RemoteLogger.finest(ex);
            return null;
        }
    }
    
    private void fireRemoteFileObjectCreated(RemoteFileObject fo) {
        FileEvent e = new FileEvent(this.getOwnerFileObject(), fo);
        RemoteFileObjectBase delegate = fo.getImplementor();
        if (delegate instanceof RemoteDirectory) { // fo.isFolder() very slow if it is a link
            fireFileFolderCreatedEvent(getListeners(), e);
        } else if (delegate instanceof RemotePlainFile) {
            fireFileDataCreatedEvent(getListeners(), e);
        } else {
            RemoteLogger.getInstance().warning("firing fireFileDataCreatedEvent for a link");
            fireFileDataCreatedEvent(getListeners(), e);
        }
//            if (fo.isFolder()) { // fo.isFolder() very slow if it is a link
//                fireFileFolderCreatedEvent(getListeners(), e);
//            } else {
//                fireFileDataCreatedEvent(getListeners(), e);
//            }
    }

    @Override
    protected RemoteFileObjectBase[] getExistentChildren() {
        return getExistentChildren(getExistingDirectoryStorage());
    }
    
    private DirectoryStorage getExistingDirectoryStorage() {
        
        DirectoryStorage storage;
        synchronized (refLock) {
            storage = storageRef.get();
        }
        if (storage == null) {
            File storageFile = getStorageFile();
            if (storageFile.exists()) {
                Lock readLock = RemoteFileSystem.getLock(getCache()).readLock();
                readLock.lock();
                try {
                    storage = DirectoryStorage.load(storageFile);
                } catch (FormatException e) {
                    FormatException.reportIfNeeded(e);
                    storageFile.delete();
                } catch (InterruptedIOException e) {
                    // nothing
                } catch (FileNotFoundException e) {
                    // this might happen if we switch to different DirEntry implementations, see storageFile.delete() above
                    RemoteLogger.finest(e, this);
                } catch (IOException e) {
                    RemoteLogger.finest(e, this);
                } finally {
                    readLock.unlock();
                }
            }
        }
        return  storage == null ? DirectoryStorage.EMPTY : storage;
    }

    private RemoteFileObjectBase[] getExistentChildren(DirectoryStorage storage) {
        List<DirEntry> entries = storage.listValid();
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
    public RemoteFileObject[] getChildren() {
        try {
            DirectoryStorage storage = getDirectoryStorage(null);
            List<DirEntry> entries = storage.listValid();
            RemoteFileObject[] childrenFO = new RemoteFileObject[entries.size()];
            for (int i = 0; i < entries.size(); i++) {
                DirEntry entry = entries.get(i);
                childrenFO[i] = getFileSystem().getFactory().createFileObject(this, entry).getOwnerFileObject();
            }
            return childrenFO;
        } catch (InterruptedException ex) {
            // don't report, this just means that we aren't connected
            // or just interrupted (for example by FileChooser UI)
            RemoteLogger.finest(ex, this);
        } catch (InterruptedIOException ex) {
            // don't report, for example FileChooser UI can interrupt us
            RemoteLogger.finest(ex, this);
        } catch (ExecutionException ex) {
            RemoteLogger.finest(ex, this);
            // should we report it?
        } catch (ConnectException ex) {
            // don't report, this just means that we aren't connected
            setFlag(CONNECTION_ISSUES, true);
            RemoteLogger.finest(ex, this);
        } catch (FileNotFoundException ex) {
            RemoteLogger.finest(ex, this);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (CancellationException ex) {
            // never report CancellationException
            RemoteLogger.finest(ex, this);
        }
        return new RemoteFileObject[0];
    }

    private DirectoryStorage getDirectoryStorage(String childName) throws
            ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {
        long time = System.currentTimeMillis();
        try {
            return getDirectoryStorageImpl(false, null, childName, false);
        } catch (StackOverflowError soe) { // workaround for #130929
            String text = "StackOverflowError when accessing " + getPath(); //NOI18N
            Exceptions.printStackTrace(new Exception(text, soe));
            throw new IOException(text, soe);
        } finally {
            if (trace) {
                trace("getDirectoryStorage for {1} took {0} ms", this, System.currentTimeMillis() - time); // NOI18N
            }
        }
    }

    private DirectoryStorage refreshDirectoryStorage(String expectedName, boolean expected) throws
            ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {
        long time = System.currentTimeMillis();
        try {
            return getDirectoryStorageImpl(true, expectedName, null, expected);
        } finally {
            if (trace) {
                trace("refreshDirectoryStorage for {1} took {0} ms", this, System.currentTimeMillis() - time); // NOI18N
            }
        }
    }
        
    private static final Collection<String> AUTO_MOUNTS;
    static {
        List<String> list = new ArrayList<String>(Arrays.asList("/net", "/set", "/import", "/shared", "/home", "/ade_autofs", "/ade", "/workspace")); //NOI18N
        String t = System.getProperty("remote.autofs.list"); //NOI18N
        if (t != null) {
            String[] paths = t.split(","); //NOI18N
            for (String p : paths) {
                if (p.startsWith("/")) { //NOI18N
                    list.add(p);
                }
            }
        }
        AUTO_MOUNTS = Collections.unmodifiableList(list);
    }

    private boolean isProhibited() {
        return getPath().equals("/proc");//NOI18N
    }

    private Map<String, DirEntry> readEntries(DirectoryStorage oldStorage, boolean forceRefresh, String childName) throws IOException, InterruptedException, ExecutionException, CancellationException {
        if (isProhibited()) {
            return Collections.<String, DirEntry>emptyMap();
        }
        Map<String, DirEntry> newEntries = new HashMap<String, DirEntry>();            
        boolean canLs = canLs();
        if (canLs) {
            DirectoryReader directoryReader = new DirectoryReaderSftp(getExecutionEnvironment(), getPath());
            directoryReader.readDirectory();
            for (DirEntry entry : directoryReader.getEntries()) {
                newEntries.put(entry.getName(), entry);
            }
            
        }
        if (canLs && !isAutoMount()) {
            return newEntries;
        }
        if (childName != null) {
            String absPath = getPath() + '/' + childName;
            RemoteLogger.assertTrueInConsole(!oldStorage.isKnown(childName) || forceRefresh, "should not get here: " + absPath); //NOI18N
            if (!newEntries.containsKey(childName)) {
                DirEntry entry = getSpecialDirChildEntry(absPath, childName);
                newEntries.put(entry.getName(), entry);
            }
        }
        for (DirEntry oldEntry : oldStorage.listAll()) {
            String oldChildName = oldEntry.getName();
            if (!newEntries.containsKey(oldChildName)) {
                if (forceRefresh) {
                    if (oldEntry.isValid()) {
                        String absPath = getPath() + '/' + oldChildName;
                        DirEntry newEntry = getSpecialDirChildEntry(absPath, oldChildName);
                        newEntries.put(oldChildName, newEntry);
                    }
                } else {
                    newEntries.put(oldChildName, oldEntry);
                }
            }
        }
        return newEntries;
    }
    
    private DirEntry getSpecialDirChildEntry(String absPath, String childName) throws InterruptedException, ExecutionException {
        StatInfo statInfo;
        try {
            statInfo = FileInfoProvider.stat(getExecutionEnvironment(), absPath, new PrintWriter(System.err)).get();
        } catch (ExecutionException e) {
            if (RemoteFileSystemUtils.isFileNotFoundException(e)) {
                statInfo = null;
            } else {
                throw e;
            }
        }
        DirEntry entry = (statInfo == null) ? new DirEntryInvalid(childName) : new DirEntrySftp(statInfo, statInfo.getName());
        return entry;
    }

    private boolean isAutoMount() {
        String path = getPath();
        if (AUTO_MOUNTS.contains(path)) {
            return true;
        }
        return false;
    }

    private boolean canLs() {
        return canRead();
    }

    private boolean isSpecialDirectory() {
        return isAutoMount() || !canLs();
    }
    
    private boolean isAlreadyKnownChild(DirectoryStorage storage, String childName) {
        if (childName != null && storage != null) {
            if (!storage.isKnown(childName)) {
                if (ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
                    if (isSpecialDirectory()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    @Override
    protected final void renameChild(FileLock lock, RemoteFileObjectBase directChild2Rename, String newNameExt, RemoteFileObjectBase orig) throws 
            ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {
        String nameExt2Rename = directChild2Rename.getNameExt();
        String name2Rename = directChild2Rename.getName();
        String ext2Rename = directChild2Rename.getExt();
        String path2Rename = directChild2Rename.getPath();

        checkConnection(this, true);

        Lock writeLock = RemoteFileSystem.getLock(getCache()).writeLock();
        if (trace) {trace("waiting for lock");} // NOI18N
        writeLock.lock();
        try {
            DirectoryStorage storage = getExistingDirectoryStorage();
            if (storage.getValidEntry(nameExt2Rename) == null) {
                throw new IOException(nameExt2Rename + " is not an existing child of " + this); // NOI18N
            }
            if (!getCache().exists()) {
                getCache().mkdirs();
                if (!getCache().exists()) {
                    throw new IOException("Can not create cache directory " + getCache()); // NOI18N
                }
            }
            if (trace) {trace("renaming");} // NOI18N
            boolean isRenamed = false;
            if (USE_VCS) {
                FilesystemInterceptor interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(getFileSystem());
                if (interceptor != null) {
                    FilesystemInterceptorProvider.IOHandler renameHandler = interceptor.getRenameHandler(FilesystemInterceptorProvider.toFileProxy(orig.getOwnerFileObject()), newNameExt);
                    if (renameHandler != null) {
                        renameHandler.handle();
                        isRenamed = true;
                    }
                }
            }
            if (!isRenamed) {
                ProcessUtils.ExitStatus ret = ProcessUtils.executeInDir(getPath(), getExecutionEnvironment(), "mv", nameExt2Rename, newNameExt);// NOI18N
                if (!ret.isOK()) {
                    throw new IOException(ret.error);
                }
            }
            
            if (trace) {trace("synchronizing");} // NOI18N
            Exception problem = null;
            Map<String, DirEntry> newEntries = Collections.emptyMap();
            try {
                newEntries = readEntries(storage, true, newNameExt);
            } catch (FileNotFoundException ex) {
                throw ex;
            } catch (IOException ex) {
                problem = ex;
            } catch (ExecutionException ex) {
                problem = ex;
            }
            if (problem != null) {
                if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
                    // connection was broken while we read directory content - add notification
                    getFileSystem().addPendingFile(this);
                    throw new ConnectException(problem.getMessage());
                } else {
                    boolean fileNotFoundException = RemoteFileSystemUtils.isFileNotFoundException(problem);
                    if (fileNotFoundException) {
                        this.invalidate();
                        synchronized (refLock) {
                            storageRef = new SoftReference<DirectoryStorage>(DirectoryStorage.EMPTY);
                        }
                    }
                    if (!fileNotFoundException) {
                        if (problem instanceof IOException) {
                            throw (IOException) problem;
                        } else if (problem instanceof ExecutionException) {
                            throw (ExecutionException) problem;
                        } else {
                            throw new IllegalStateException("Unexpected exception class: " + problem.getClass().getName(), problem); //NOI18N
                        }
                    }
                }
            }
            getFileSystem().incrementDirSyncCount();
            Map<String, List<DirEntry>> dupLowerNames = new HashMap<String, List<DirEntry>>();
            boolean hasDups = false;
            boolean changed = true;
            Set<DirEntry> keepCacheNames = new HashSet<DirEntry>();
            List<DirEntry> entriesToFireChanged = new ArrayList<DirEntry>();
            List<DirEntry> entriesToFireCreated = new ArrayList<DirEntry>();
            List<RemoteFileObject> filesToFireDeleted = new ArrayList<RemoteFileObject>();
            for (DirEntry newEntry : newEntries.values()) {
                if (newEntry.isValid()) {
                    String cacheName;
                    DirEntry oldEntry = storage.getValidEntry(newEntry.getName());
                    if (oldEntry == null) {
                        cacheName = RemoteFileSystemUtils.escapeFileName(newEntry.getName());
                        if (newEntry.getName().equals(newNameExt)) {
                            DirEntry renamedEntry = storage.getValidEntry(nameExt2Rename);
                            RemoteLogger.assertTrueInConsole(renamedEntry != null, "original DirEntry is absent for " + path2Rename + " in " + this); // NOI18N
                            // reuse cache from original file
                            if (renamedEntry != null) {
                                cacheName = renamedEntry.getCache();
                                newEntry.setCache(cacheName);
                                keepCacheNames.add(newEntry);
                            }
                        } else {
                            entriesToFireCreated.add(newEntry);
                        }
                    } else {
                        if (oldEntry.isSameType(newEntry)) {
                            cacheName = oldEntry.getCache();
                            keepCacheNames.add(newEntry);
                            boolean fire = false;
                            if (!newEntry.isSameLastModified(oldEntry) || newEntry.getSize() != oldEntry.getSize()) {
                                if (newEntry.isPlainFile()) {
                                    changed = fire = true;
                                    File entryCache = new File(getCache(), oldEntry.getCache());
                                    if (entryCache.exists()) {
                                        if (trace) {trace("removing cache for updated file {0}", entryCache.getAbsolutePath());} // NOI18N
                                        entryCache.delete(); // TODO: We must just mark it as invalid instead of physically deleting cache file...
                                    }
                                }
                            } 
                            if (!equals(newEntry.getLinkTarget(), oldEntry.getLinkTarget())) {
                                changed = fire = true; // TODO: we forgot old link path, probably should be passed to change event 
                                getFileSystem().getFactory().setLink(this, getPath() + '/' + newEntry.getName(), newEntry.getLinkTarget());
                            } 
                            if (!newEntry.getAccessAsString().equals(oldEntry.getAccessAsString())) {
                                changed = fire = true;
                            } 
                            if (!newEntry.isSameUser(oldEntry)) {
                                changed = fire = true;
                            } 
                            if (!newEntry.isSameGroup(oldEntry)) {
                                changed = fire = true;
                            } 
                            if (!newEntry.isDirectory() && (newEntry.getSize() != oldEntry.getSize())) {
                                changed = fire = true;// TODO: shouldn't it be the same as time stamp change?
                            }
                            if (fire) {
                                entriesToFireChanged.add(newEntry);
                            }
                        } else {
                            changed = true;
                            getFileSystem().getFactory().changeImplementor(this, oldEntry, newEntry);
                            entriesToFireChanged.add(newEntry);
                            cacheName = null; // unchanged
                        }
                    }
                    if (cacheName !=null) {
                        newEntry.setCache(cacheName);
                    }
                    String lowerCacheName = RemoteFileSystemUtils.isSystemCaseSensitive() ? newEntry.getCache() : newEntry.getCache().toLowerCase();
                    List<DirEntry> dupEntries = dupLowerNames.get(lowerCacheName);
                    if (dupEntries == null) {
                        dupEntries = new ArrayList<DirEntry>();
                        dupLowerNames.put(lowerCacheName, dupEntries);
                    } else {
                        hasDups = true;
                    }
                    dupEntries.add(newEntry);
                } else {
                    changed = true;
                }
            }
            if (changed) {
                // Check for removal
                for (DirEntry oldEntry : storage.listValid()) {
                    if (!oldEntry.getName().equals(nameExt2Rename)) {
                        DirEntry newEntry = newEntries.get(oldEntry.getName());
                        if (newEntry == null || !newEntry.isValid()) {
                            RemoteFileObject removedFO = invalidate(oldEntry);
                            if (removedFO != null) {
                                filesToFireDeleted.add(removedFO);
                            }
                        }
                    }
                }
                if (hasDups) {
                    for (Map.Entry<String, List<DirEntry>> mapEntry :
                            new ArrayList<Map.Entry<String, List<DirEntry>>>(dupLowerNames.entrySet())) {

                        List<DirEntry> dupEntries = mapEntry.getValue();
                        if (dupEntries.size() > 1) {
                            for (int i = 0; i < dupEntries.size(); i++) {
                                DirEntry entry = dupEntries.get(i);
                                if (keepCacheNames.contains(entry)) {
                                    continue; // keep the one that already exists
                                }
                                // all duplicates will have postfix
                                for (int j = 0; j < Integer.MAX_VALUE; j++) {
                                    String cacheName = mapEntry.getKey() + '_' + j;
                                    String lowerCacheName = cacheName.toLowerCase();
                                    if (!dupLowerNames.containsKey(lowerCacheName)) {
                                        if (trace) {
                                            trace("resolving cache names conflict in {0}: {1} -> {2}", // NOI18N
                                                    getCache().getAbsolutePath(), entry.getCache(), cacheName);
                                        }
                                        entry.setCache(cacheName);
                                        dupLowerNames.put(lowerCacheName, Collections.singletonList(entry));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                storage = new DirectoryStorage(getStorageFile(), newEntries.values());
                storage.store();
            } else {
                storage.touch();
            }
            // always put new content in cache 
            // do it before firing events, to give liseners real content
            synchronized (refLock) {
                storageRef = new SoftReference<DirectoryStorage>(storage);
            }
            // fire all event under lockImpl
            if (changed) {
                dropMagic();
                for (FileObject deleted : filesToFireDeleted) {
                    fireFileDeletedEvent(getListeners(), new FileEvent(this.getOwnerFileObject(), deleted));
                }
                for (DirEntry entry : entriesToFireCreated) {
                    RemoteFileObjectBase fo = getFileSystem().getFactory().createFileObject(this, entry);
                    fireRemoteFileObjectCreated(fo.getOwnerFileObject());
                }
                for (DirEntry entry : entriesToFireChanged) {
                    RemoteFileObjectBase fo = getFileSystem().getFactory().getCachedFileObject(getPath() + '/' + entry.getName());
                    if (fo != null) {
                        RemoteFileObject ownerFileObject = fo.getOwnerFileObject();
                        fireFileChangedEvent(getListeners(), new FileEvent(ownerFileObject, ownerFileObject, false, ownerFileObject.lastModified().getTime()));
                    }
                }
                // rename itself
                String newPath = getPath() + '/' + newNameExt;
                getFileSystem().getFactory().rename(path2Rename, newPath, directChild2Rename);
                // fire rename
                fireFileRenamedEvent(directChild2Rename.getListeners(), 
                        new FileRenameEvent(directChild2Rename.getOwnerFileObject(), directChild2Rename.getOwnerFileObject(), name2Rename, ext2Rename));
                fireFileRenamedEvent(this.getListeners(), 
                        new FileRenameEvent(this.getOwnerFileObject(), directChild2Rename.getOwnerFileObject(), name2Rename, ext2Rename));
            }
        } finally {
            writeLock.unlock();
        }
    }
    
    /*package */void updateStat(RemotePlainFile fo, StatInfo statInfo) {
        RemoteLogger.assertTrue(fo.getNameExt().equals(statInfo.getName()));
        RemoteLogger.assertFalse(statInfo.isDirectory());
        RemoteLogger.assertFalse(statInfo.isLink());
        Lock writeLock = RemoteFileSystem.getLock(getCache()).writeLock();
        if (trace) {trace("waiting for lock");} // NOI18N
        writeLock.lock();
        try {
            DirectoryStorage storage = getExistingDirectoryStorage();
            if (storage == DirectoryStorage.EMPTY) {
                Exceptions.printStackTrace(new IllegalStateException("Update stat is called but remote directory cache does not exist")); // NOI18N
            } else {
                List<DirEntry> entries = storage.listValid(fo.getNameExt());                
                DirEntry entry = new DirEntrySftp(statInfo, fo.getCache().getName());
                entries.add(entry);
                DirectoryStorage newStorage = new DirectoryStorage(getStorageFile(), entries);
                try {
                    newStorage.store();
                } catch (IOException ex) {                    
                    Exceptions.printStackTrace(ex); // what else can we do?..
                }
                synchronized (refLock) {
                    storageRef = new SoftReference<DirectoryStorage>(newStorage);
                }
                fo.setPendingRemoteDelivery(false);
            }
        } finally {
            writeLock.unlock();
        }
    }
    
    private DirectoryStorage getDirectoryStorageImpl(final boolean forceRefresh, final String expectedName, final String childName, final boolean expected) throws
            ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {

        if (forceRefresh && ! ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
            //RemoteLogger.getInstance().warning("refreshDirectoryStorage is called while host is not connected");
            //force = false;
            throw new ConnectException();
        }

        DirectoryStorage storage = null;

        File storageFile = getStorageFile();

        // check whether it is cached in memory
        synchronized (refLock) {
            storage = storageRef.get();
        }
        boolean fromMemOrDiskCache;

        if (storage == null) {
            // try loading from disk
            fromMemOrDiskCache = false;
            storage = DirectoryStorage.EMPTY;
            if (storageFile.exists()) {
                Lock readLock = RemoteFileSystem.getLock(getCache()).readLock();
                try {
                    readLock.lock();       
                    try {
                        storage = DirectoryStorage.load(storageFile);
                        fromMemOrDiskCache = true;
                        // try to keep loaded cache in memory
                        synchronized (refLock) {
                            DirectoryStorage s = storageRef.get();
                            // it could be cache put in memory by writer (the best content)
                            // or by previous reader => it's the same as loaded
                            if (s != null) {
                                if (trace) { trace("using storage that was kept by other thread"); } // NOI18N
                                storage = s;
                            } else {
                                storageRef = new SoftReference<DirectoryStorage>(storage);
                            }
                        }
                    } catch (FormatException e) {
                        FormatException.reportIfNeeded(e);
                        storageFile.delete();
                    } catch (InterruptedIOException e) {
                        throw e;
                    } catch (FileNotFoundException e) {
                        // this might happen if we switch to different DirEntry implementations, see storageFile.delete() above
                        RemoteLogger.finest(e, this);
                    } catch (IOException e) {
                        Exceptions.printStackTrace(e);
                    }
                } finally {
                    readLock.unlock();
                }
            }
        } else {
            if (trace) { trace("use memory cached storage"); } // NOI18N
            fromMemOrDiskCache = true;
        }
        
        if (fromMemOrDiskCache && !forceRefresh && isAlreadyKnownChild(storage, childName)) {
            RemoteLogger.assertTrue(storage != null);
            if (trace) { trace("returning cached storage"); } // NOI18N
            return storage;
        }
        // neither memory nor disk cache helped or was request to force refresh
        // proceed with reading remote content
                
        checkConnection(this, true);

        Lock writeLock = RemoteFileSystem.getLock(getCache()).writeLock();
        if (trace) { trace("waiting for lock"); } // NOI18N
        writeLock.lock();
        try {
            // in case another writer thread already synchronized content while we were waiting for lockImpl
            // even in refresh mode, we need this content, otherwise we'll generate events twice
            synchronized (refLock) {
                DirectoryStorage s = storageRef.get();
                if (s != null) {
                    if (trace) { trace("got storage from mem cache after waiting on writeLock: {0} expectedName={1}", getPath(), expectedName); } // NOI18N
                    if (forceRefresh || !isAlreadyKnownChild(s, childName)) {
                        storage = s;
                    } else {
                        return s;
                    }
                }
            }
            if (!getCache().exists()) {
                getCache().mkdirs();
                if (!getCache().exists()) {
                    throw new IOException("Can not create cache directory " + getCache()); // NOI18N
                }
            }
            if (trace) { trace("synchronizing"); } // NOI18N
            Exception problem = null;
            Map<String, DirEntry> newEntries = Collections.emptyMap();
            try {
                newEntries = readEntries(storage, forceRefresh, childName);
            }  catch (FileNotFoundException ex) {
                throw ex;
            }  catch (IOException ex) {
                problem = ex;
            }  catch (ExecutionException ex) {
                problem = ex;
            }
            if (problem != null) {
                if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
                    // connection was broken while we read directory content - add notification
                    getFileSystem().addPendingFile(this);
                    throw new ConnectException(problem.getMessage());
                } else {
                    boolean fileNotFoundException = RemoteFileSystemUtils.isFileNotFoundException(problem);
                    if (fileNotFoundException) {
                        synchronized (refLock) {
                            storageRef = new SoftReference<DirectoryStorage>(DirectoryStorage.EMPTY);
                        }
                    }
                    if (!fileNotFoundException) { 
                        if (problem instanceof IOException) { 
                            throw (IOException) problem;
                        } else if (problem instanceof ExecutionException) {
                            throw (ExecutionException) problem;
                        } else {
                            throw new IllegalStateException("Unexpected exception class: " + problem.getClass().getName(), problem); //NOI18N
                        }
                    }
                }
            }
            getFileSystem().incrementDirSyncCount();
            Map<String, List<DirEntry>> dupLowerNames = new HashMap<String, List<DirEntry>>();
            boolean hasDups = false;
            boolean changed = (newEntries.size() != storage.listAll().size()) || (storage == DirectoryStorage.EMPTY);
            Set<DirEntry> keepCacheNames = new HashSet<DirEntry>();
            List<DirEntry> entriesToFireChanged = new ArrayList<DirEntry>();
            List<DirEntry> entriesToFireChangedRO = new ArrayList<DirEntry>();
            List<DirEntry> entriesToFireCreated = new ArrayList<DirEntry>();
            DirEntry expectedCreated = null;
            List<RemoteFileObject> filesToFireDeleted = new ArrayList<RemoteFileObject>();
            for (DirEntry newEntry : newEntries.values()) {
                if (newEntry.isValid()) {
                    String cacheName;
                    DirEntry oldEntry = storage.getValidEntry(newEntry.getName());
                    if (oldEntry == null || !oldEntry.isValid()) {
                        changed = true;
                        cacheName = RemoteFileSystemUtils.escapeFileName(newEntry.getName());
                        if (fromMemOrDiskCache || newEntry.getName().equals(expectedName) || getFlag(CONNECTION_ISSUES)) {
                            entriesToFireCreated.add(newEntry);
                            expectedCreated = newEntry;
                        }
                    } else {
                        if (oldEntry.isSameType(newEntry)) {
                            cacheName = oldEntry.getCache();
                            keepCacheNames.add(newEntry);
                            boolean fire = false;
                            if (!newEntry.isSameLastModified(oldEntry) || newEntry.getSize() != oldEntry.getSize()) {
                                if (newEntry.isPlainFile()) {
                                    changed = fire = true;
                                    File entryCache = new File(getCache(), oldEntry.getCache());
                                    if (entryCache.exists()) {
                                        if (trace) { trace("removing cache for updated file {0}", entryCache.getAbsolutePath()); } // NOI18N
                                        entryCache.delete(); // TODO: We must just mark it as invalid instead of physically deleting cache file...
                                    }
                                } 

                            } 
                            if (!equals(newEntry.getLinkTarget(), oldEntry.getLinkTarget())) {
                                changed = fire = true; // TODO: we forgot old link path, probably should be passed to change event 
                                getFileSystem().getFactory().setLink(this, getPath() + '/' + newEntry.getName(), newEntry.getLinkTarget());
                            } 
                            if (!newEntry.getAccessAsString().equals(oldEntry.getAccessAsString())) {
                                changed = fire = true;
                            } 
                            if (!newEntry.isSameUser(oldEntry)) {
                                changed = fire = true;
                            } 
                            if (!newEntry.isSameGroup(oldEntry)) {
                                changed = fire = true;
                            } 
                            if (!newEntry.isDirectory() && (newEntry.getSize() != oldEntry.getSize())) {
                                changed = fire = true;// TODO: shouldn't it be the same as time stamp change?
                            }
                            if (fire) {
                                entriesToFireChanged.add(newEntry);
                            }
                        } else {
                            changed = true;
                            getFileSystem().getFactory().changeImplementor(this, oldEntry, newEntry);
                            if (oldEntry.isLink() && newEntry.isPlainFile() && newEntry.canWrite(getExecutionEnvironment())) {
                                entriesToFireChangedRO.add(newEntry);
                            } else {
                                entriesToFireChanged.add(newEntry);
                            }
                            cacheName = null; // unchanged
                        }
                    }
                    if (cacheName !=null) {
                        newEntry.setCache(cacheName);
                    }
                    String lowerCacheName = RemoteFileSystemUtils.isSystemCaseSensitive() ? newEntry.getCache() : newEntry.getCache().toLowerCase();
                    List<DirEntry> dupEntries = dupLowerNames.get(lowerCacheName);
                    if (dupEntries == null) {
                        dupEntries = new ArrayList<DirEntry>();
                        dupLowerNames.put(lowerCacheName, dupEntries);
                    } else {
                        hasDups = true;
                    }
                    dupEntries.add(newEntry);
                } else {
                    if (!storage.isKnown(childName)) {
                        changed = true;
                    }
                }
            }
            if (changed) {
                // Check for removal
                for (DirEntry oldEntry : storage.listValid()) {
                    DirEntry newEntry = newEntries.get(oldEntry.getName());
                    if (newEntry == null || !newEntry.isValid()) {
                        RemoteFileObject removedFO = invalidate(oldEntry);
                        if (removedFO != null) {
                            filesToFireDeleted.add(removedFO);
                        }
                    }
                }
                if (hasDups) {
                    for (Map.Entry<String, List<DirEntry>> mapEntry :
                        new ArrayList<Map.Entry<String, List<DirEntry>>>(dupLowerNames.entrySet())) {

                        List<DirEntry> dupEntries = mapEntry.getValue();
                        if (dupEntries.size() > 1) {
                            for (int i = 0; i < dupEntries.size(); i++) {
                                DirEntry entry = dupEntries.get(i);
                                if (keepCacheNames.contains(entry)) {
                                    continue; // keep the one that already exists
                                }
                                // all duplicates will have postfix
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
                storage = new DirectoryStorage(storageFile, newEntries.values());
                storage.store();  
            } else {
                storage.touch();
            }
            setFlag(CONNECTION_ISSUES, false);
            // always put new content in cache 
            // do it before firing events, to give liseners real content
            synchronized (refLock) {
                storageRef = new SoftReference<DirectoryStorage>(storage);
            }
            // fire all event under lockImpl
            if (changed) {
                dropMagic();
                FilesystemInterceptorProvider.FilesystemInterceptor interceptor = null;
                if (USE_VCS) {
                    interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(getFileSystem());
                }
                for (RemoteFileObject deleted : filesToFireDeleted) {
                    fireDeletedEvent(this.getOwnerFileObject(), deleted, interceptor, expected);
                }
                for (DirEntry entry : entriesToFireCreated) {
                    RemoteFileObject fo = getFileSystem().getFactory().createFileObject(this, entry).getOwnerFileObject();
                    if (interceptor != null && expectedCreated != null && !expectedCreated.equals(entry)) {
                        interceptor.createdExternally(FilesystemInterceptorProvider.toFileProxy(fo));
                    }
                    fireRemoteFileObjectCreated(fo);
                }
                for (DirEntry entry : entriesToFireChanged) {
                    RemoteFileObjectBase fo = getFileSystem().getFactory().getCachedFileObject(getPath() + '/' + entry.getName());
                    if (fo != null) {
                        if (fo.isPendingRemoteDelivery()) {
                            RemoteLogger.getInstance().log(Level.FINE, "Skipping change event for pending file {0}", fo);
                        } else {
                            RemoteFileObject ownerFileObject = fo.getOwnerFileObject();
                            fireFileChangedEvent(getListeners(), new FileEvent(ownerFileObject, ownerFileObject, expected, ownerFileObject.lastModified().getTime()));
                        }
                    }
                }
                for (DirEntry entry : entriesToFireChangedRO) {
                    RemoteFileObjectBase fo = getFileSystem().getFactory().getCachedFileObject(getPath() + '/' + entry.getName());
                    if (fo != null) {
                        if (fo.isPendingRemoteDelivery()) {
                            RemoteLogger.getInstance().log(Level.FINE, "Skipping change event for pending file {0}", fo);
                        } else {
                            fo.fireFileAttributeChangedEvent("DataEditorSupport.read-only.refresh", null, null);  //NOI18N
                        }
                    }
                }
                //fireFileChangedEvent(getListeners(), new FileEvent(this));
            }
        } finally {
            writeLock.unlock();
        }
        return storage;
    }
    
    private void fireDeletedEvent(RemoteFileObject parent, RemoteFileObject fo, FilesystemInterceptorProvider.FilesystemInterceptor interceptor, boolean expected) {
        if (interceptor != null) {
            interceptor.deletedExternally(FilesystemInterceptorProvider.toFileProxy(fo));
        }
        fo.fireFileDeletedEvent(fo.getImplementor().getListeners(), new FileEvent(fo, fo, expected));
        parent.fireFileDeletedEvent(parent.getImplementor().getListeners(), new FileEvent(parent, fo, expected));
    }
    
//    InputStream _getInputStream(RemotePlainFile child) throws
//            ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {
//        Lock lock = RemoteFileSystem.getLock(child.getCache()).readLock();
//        lock.lock();
//        try {
//            if (child.getCache().exists()) {
//                return new FileInputStream(child.getCache());
//            }
//        } finally {
//            lock.unlock();
//        }
//        checkConnection(child, true);
//        DirectoryStorage storage = getDirectoryStorage(child.getNameExt()); // do we need this?
//        return new CachedRemoteInputStream(child, getExecutionEnvironment());
//    }
    
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
        DirectoryStorage storage = getDirectoryStorage(child.getNameExt()); // do we need this?
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
            getFileSystem().addPendingFile(fo);
            if (throwConnectException) {
                throw new ConnectException();
            }
        }
    }

    @Override
    public FileType getType() {
        return FileType.Directory;
    }

    @Override
    public final InputStream getInputStream() throws FileNotFoundException {
        throw new FileNotFoundException(getPath());
    }

    public byte[] getMagic(RemoteFileObjectBase file) {
        return getMagicCache().get(file.getNameExt());
    }

    private MagicCache getMagicCache() {
        MagicCache magic;
        synchronized (magicLock) {
            magic = magicCache.get();
            if (magic == null) {
                magic = new MagicCache(this);
                magicCache = new SoftReference<MagicCache>(magic);
            }
        }
        return magic;
    }

    private void dropMagic() {
        synchronized (magicLock) {
            MagicCache magic = magicCache.get();
            if (magic != null) {
                magic.clean(null);
            } else {
                new MagicCache(this).clean(null);
            }
        }
    }
    
    @Override
    protected final OutputStream getOutputStreamImpl(final FileLock lock, RemoteFileObjectBase orig) throws IOException {
        throw new IOException(getPath());
    }

    private RemoteFileObject invalidate(DirEntry oldEntry) {
        RemoteFileObject fo = getFileSystem().getFactory().invalidate(getPath() + '/' + oldEntry.getName());
        File oldEntryCache = new File(getCache(), oldEntry.getCache());
        removeFile(oldEntryCache);
        return fo;
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
                @Override
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
    protected void refreshImpl(boolean recursive, Set<String> antiLoop, boolean expected) throws ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {
        if (antiLoop != null) {
            if (antiLoop.contains(getPath())) {
                return;
            } else {
                antiLoop.add(getPath());
            }
        }
        DirectoryStorage storage = getExistingDirectoryStorage();
        if (storage ==  null ||storage == DirectoryStorage.EMPTY) {
            return;
        }
        // unfortunately we can't skip refresh if there is a storage but no children exists
        // in this case we have to reafresh just storage - but for the time being only RemoteDirectory can do that
        // TODO: revisit this after refactoring cache into a separate class(es)
        DirectoryStorage refreshedStorage = refreshDirectoryStorage(null, expected);
        if (recursive) {
            for (RemoteFileObjectBase child : getExistentChildren(refreshedStorage)) {
                child.refreshImpl(true, antiLoop, expected);
            }
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
            DirectoryStorage directoryStorage = getDirectoryStorage(child.getNameExt());
            if (directoryStorage != null) {
                DirEntry entry = directoryStorage.getValidEntry(child.getNameExt());
                if (entry != null) {
                    return entry;
                } else {
                    RemoteLogger.getInstance().log(Level.INFO, "Not found entry for file {0}", child); // NOI18N
                }
            }
        } catch (ConnectException ex) {
            RemoteLogger.finest(ex, this);
        } catch (IOException ex) {
            RemoteLogger.finest(ex, this);
        } catch (ExecutionException ex) {
            RemoteLogger.finest(ex, this);
        } catch (InterruptedException ex) {
            RemoteLogger.finest(ex, this);
        } catch (CancellationException ex) {
            RemoteLogger.finest(ex, this);
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
    
    /** for tests ONLY! */
    /*package*/ DirectoryStorage testGetExistingDirectoryStorage() {
        return getExistingDirectoryStorage();
    }    
    
    private File getStorageFile() {
        return new File(getCache(), RemoteFileSystem.CACHE_FILE_NAME);
    }

    @Override
    public void diagnostics(boolean recursive) {
        RemoteFileObjectBase[] existentChildren = getExistentChildren();
        System.err.printf("\nRemoteFS diagnostics for %s\n", this); //NOI18N
        System.err.printf("Existing children count: %d\n", existentChildren.length); //NOI18N
        File cache = getStorageFile();
        System.err.printf("Cache file: %s\n", cache.getAbsolutePath()); //NOI18N
        System.err.printf("Cache content: \n"); //NOI18N
        printFile(cache, System.err);
        System.err.printf("Existing children:\n"); //NOI18N
        for (RemoteFileObjectBase fo : existentChildren) {
            System.err.printf("\t%s [%s] %d\n",  //NOI18N
                    fo.getNameExt(), fo.getCache().getName(), fo.getCache().length());
        }
        if (recursive) {
            for (RemoteFileObjectBase fo : existentChildren) {
                fo.diagnostics(recursive);
            }
        }
    }

    private static void printFile(File file, PrintStream out) {
        BufferedReader rdr = null;
        try {
            rdr = new BufferedReader(new FileReader(file));
            try {
                String line;
                while ((line = rdr.readLine()) != null) {
                    out.printf("%s\n", line);
                }
            } finally {
                try {
                    rdr.close();
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        } finally {
            try {
                if (rdr != null) {
                    rdr.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

}
