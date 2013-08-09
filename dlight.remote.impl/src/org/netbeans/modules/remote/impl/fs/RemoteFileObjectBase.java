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
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo.FileType;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider.FileProxyI;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider.FilesystemInterceptor;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider.IOHandler;
import org.openide.filesystems.*;

/**
 *
 * @author Vladimir Kvashin
 */
public abstract class RemoteFileObjectBase {

    private final RemoteFileSystem fileSystem;
    private final RemoteFileObjectBase parent;
    private volatile String remotePath;
    private final File cache;
    private CopyOnWriteArrayList<FileChangeListener> listeners = new CopyOnWriteArrayList<FileChangeListener>();
    private FileLock lock;
    private final Object instanceLock = new Object();
    public static final boolean USE_VCS;
    static {
        if ("false".equals(System.getProperty("remote.vcs.suport"))) { //NOI18N
            USE_VCS = false;
        } else {
            USE_VCS = true;
        }
    }

    private volatile byte flags;
    
    private final RemoteFileObject fileObject;

    private static final byte MASK_VALID = 1;
    private static final byte CHECK_CAN_WRITE = 2;
    private static final byte BEING_UPLOADED = 4;
    protected static final byte CONNECTION_ISSUES = 8;
    
    protected RemoteFileObjectBase(RemoteFileObject wrapper, RemoteFileSystem fileSystem, ExecutionEnvironment execEnv,
            RemoteFileObjectBase parent, String remotePath, File cache) {
        RemoteLogger.assertTrue(execEnv.isRemote());        
        //RemoteLogger.assertTrue(cache.exists(), "Cache should exist for " + execEnv + "@" + remotePath); //NOI18N
        this.parent = parent;
        this.remotePath = remotePath; // RemoteFileSupport.fromFixedCaseSensitivePathIfNeeded(remotePath);
        this.cache = cache;
        setFlag(MASK_VALID, true);
        this.fileSystem = wrapper.getFileSystem();
        this.fileObject = wrapper;
        wrapper.setImplementor(this);
    }

    public abstract boolean isFolder();
    public abstract boolean isData();
    public abstract RemoteFileObject getFileObject(String name, String ext, @NonNull Set<String> antiLoop);
    public abstract RemoteFileObject getFileObject(String relativePath, @NonNull Set<String> antiLoop);
    public abstract InputStream getInputStream(boolean checkLock) throws FileNotFoundException;
    public abstract RemoteFileObject[] getChildren();
    public abstract FileType getType();

    protected RemoteFileObject getOwnerFileObject() {
        return fileObject;
    }

    /** conveniency shortcut */
    protected final void fireFileChangedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        getOwnerFileObject().fireFileChangedEvent(en, fe);
    }

    /** conveniency shortcut */
    protected final void fireFileDeletedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        getOwnerFileObject().fireFileDeletedEvent(en, fe);
    }

    /** conveniency shortcut */
    protected final void fireFileAttributeChangedEvent(Enumeration<FileChangeListener> en, FileAttributeEvent fe) {
        getOwnerFileObject().fireFileAttributeChangedEvent(en, fe);
    }
    
    /** conveniency shortcut */
    protected final void fireFileDataCreatedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        getOwnerFileObject().fireFileDataCreatedEvent(en, fe);
    }
    
    /** conveniency shortcut */
    protected final void fireFileFolderCreatedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        getOwnerFileObject().fireFileFolderCreatedEvent(en, fe);
    }
    
    /** conveniency shortcut */
    protected final void fireFileRenamedEvent(Enumeration<FileChangeListener> en, FileRenameEvent fe) {
        getOwnerFileObject().fireFileRenamedEvent(en, fe);
    }

    protected boolean getFlag(byte mask) {
        return (flags & mask) == mask;
    }
    
    protected final void setFlag(byte mask, boolean value) {
        if (value) {
            flags |= mask;
        } else {
            flags &= ~mask;
        }
    }
    
    /*package*/ boolean isPendingRemoteDelivery() {
        return getFlag(BEING_UPLOADED);
    }
    
    /*package*/ void setPendingRemoteDelivery(boolean value) {
        setFlag(BEING_UPLOADED, value);
    }
    
    public ExecutionEnvironment getExecutionEnvironment() {
        return fileSystem.getExecutionEnvironment();
    }

    /**
     * local cache of this FileObject (for directory - local dir, for file - local file with content)
     * @return 
     */
    protected final File getCache() {
        return cache;
    }

    protected boolean hasCache() {
        return cache != null && cache.exists();
    }

    public String getPath() {
        return this.remotePath;
    }

    public void addFileChangeListener(FileChangeListener fcl) {
        listeners.add(fcl);
    }
    
    public void removeFileChangeListener(FileChangeListener fcl) {
        listeners.remove(fcl);
    }
    
    protected final Enumeration<FileChangeListener> getListeners() {
        return Collections.enumeration(listeners);
    }
    
    protected final Enumeration<FileChangeListener> getListenersWithParent() {
        RemoteFileObjectBase p = getParent();
        if (p == null) {
            return getListeners();
        }
        Enumeration<FileChangeListener> parentListeners = p.getListeners();
        if (!parentListeners.hasMoreElements()) {
            return getListeners();
        }
        List<FileChangeListener> result = new ArrayList<FileChangeListener>(listeners);
        while (parentListeners.hasMoreElements()) {
            result.add(parentListeners.nextElement());
        }
        return Collections.enumeration(result);
    }    

    public void addRecursiveListener(FileChangeListener fcl) {
        if (isFolder()) {
            getFileSystem().addFileChangeListener(new RecursiveListener(getOwnerFileObject(), fcl, false));
        } else {
            addFileChangeListener(fcl);
        }
    }

    public void removeRecursiveListener(FileChangeListener fcl) {
        if (isFolder()) {
            getFileSystem().removeFileChangeListener(new RecursiveListener(getOwnerFileObject(), fcl, false));
        } else {
            removeFileChangeListener(fcl);
        }
    }

    public final FileObject createData(String name) throws IOException {
        return createDataImpl(name, "", this);
    }

    public final FileObject createData(String name, String ext) throws IOException {
        return createDataImpl(name, ext, this);
    }

    abstract protected RemoteFileObject createDataImpl(String name, String ext, RemoteFileObjectBase orig) throws IOException;

    public final FileObject createFolder(String name) throws IOException {
        return createFolderImpl(name, this);
    }

    abstract protected RemoteFileObject createFolderImpl(String name, RemoteFileObjectBase orig) throws IOException;

    protected abstract boolean deleteImpl(FileLock lock) throws IOException;

    protected abstract void postDeleteChild(FileObject child);
    
    
    public final void delete(FileLock lock) throws IOException {
        deleteImpl(lock, this);
    }
    
    protected void deleteImpl(FileLock lock, RemoteFileObjectBase orig) throws IOException {
        if (!checkLock(lock)) {
            throw new IOException("Wrong lock"); //NOI18N
        }
        FilesystemInterceptor interceptor = null;
        if (USE_VCS) {
            interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(fileSystem);
        }
        boolean result;
        if (interceptor != null) {
            FileProxyI fileProxy = FilesystemInterceptorProvider.toFileProxy(orig.getOwnerFileObject());
            IOHandler deleteHandler = interceptor.getDeleteHandler(fileProxy);
            if (deleteHandler != null) {
                deleteHandler.handle();
                result = true;
            } else {
                result = deleteImpl(lock);
            }
            if (!result) {
                throw new IOException("Cannot delete "+getPath()); // NOI18N
            }
            // TODO remove attributes
            // TODO clear cache?
            // TODO fireFileDeletedEvent()?
            interceptor.deleteSuccess(fileProxy);
        } else {
            result = deleteImpl(lock);
            if (!result) {
                throw new IOException("Cannot delete "+getPath()); // NOI18N
            }
        }
        RemoteFileObject fo = getOwnerFileObject();
        for(Map.Entry<String, Object> entry : getAttributesMap().entrySet()) {
            fo.fireFileAttributeChangedEvent(getListenersWithParent(), new FileAttributeEvent(fo, fo, entry.getKey(), entry.getValue(), null));
        }
        FileEvent fe = new FileEvent(fo, fo, true);
        for(RemoteFileObjectBase child: getExistentChildren(true)) {
            fo.fireFileDeletedEvent(Collections.enumeration(child.listeners), fe);
        }        
        invalidate();        
        RemoteFileObjectBase p = getParent();
        if (p != null) {
            p.postDeleteChild(getOwnerFileObject());
        }
    }
    
    public String getExt() {
        String nameExt = getNameExt();
        int pointPos = nameExt.lastIndexOf('.');
        return (pointPos < 0) ? "" : nameExt.substring(pointPos + 1);
    }

    public RemoteFileSystem getFileSystem() {
        return fileSystem;
    }

    public String getName() {
        String nameExt = getNameExt();
        int pointPos = nameExt.lastIndexOf('.');
        return (pointPos < 0) ? nameExt : nameExt.substring(0, pointPos);
    }

    public String getNameExt() {
        int slashPos = this.getPath().lastIndexOf('/');
        return (slashPos < 0) ? "" : this.getPath().substring(slashPos + 1);
    }

    public final OutputStream getOutputStream(FileLock lock) throws IOException {
        return getOutputStreamImpl(lock, this);
    }
    
    protected OutputStream getOutputStreamImpl(FileLock lock, RemoteFileObjectBase orig) throws IOException {
        throw new ReadOnlyException();
    }
    
    protected byte[] getMagic() {
        try {
            RemoteDirectory canonicalParent = RemoteFileSystemUtils.getCanonicalParent(this);
            if (canonicalParent != null) {
                return canonicalParent.getMagic(this);
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        return null;
    }
    
    private void populateWithChildren(RemoteFileObjectBase rfl, List<RemoteFileObjectBase> children) {
        children.add(rfl);
        for(RemoteFileObjectBase child: rfl.getExistentChildren()) {
            populateWithChildren(child, children);
        }
    }
    
    protected RemoteFileObjectBase[] getExistentChildren(boolean recursive) {
        if (!recursive) return getExistentChildren();
        List<RemoteFileObjectBase> children = new LinkedList<RemoteFileObjectBase>();
        populateWithChildren(this, children);
        children.remove(this);
        return children.toArray(new RemoteFileObjectBase[0]);
    }
    
    protected RemoteFileObjectBase[] getExistentChildren() {
        return new RemoteFileObjectBase[0];
    }

    public RemoteFileObjectBase getParent() {
        return parent;
    }

    public long getSize() {
        RemoteDirectory canonicalParent;
        try {
            canonicalParent = RemoteFileSystemUtils.getCanonicalParent(this);
            if (canonicalParent != null) {
                return canonicalParent.getSize(this);
            }
        } catch (IOException ex) {
            reportIOException(ex);
        }
        return 0;
    }

    @Deprecated
    public final boolean isReadOnly() {
        return isReadOnlyImpl(this);
    }
    
    protected boolean isReadOnlyImpl(RemoteFileObjectBase orig) {
        if (USE_VCS) {
            FilesystemInterceptor interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(fileSystem);
            if (interceptor != null) {
                return !canWriteImpl(orig) && isValid();
            }
        }
        return !canRead();
    }

    public boolean canRead() {
        try {
            RemoteDirectory canonicalParent = RemoteFileSystemUtils.getCanonicalParent(this);
            if (canonicalParent == null) {
                return true;
            } else {
                return canonicalParent.canRead(getNameExt());
            }
        } catch (IOException ex) {
            reportIOException(ex);
            return true;
        }
    }

    
    public boolean canExecute() {
        try {
            RemoteDirectory canonicalParent = RemoteFileSystemUtils.getCanonicalParent(this);
            if (canonicalParent == null) {
                return true;
            } else {
                return canonicalParent.canExecute(getNameExt());
            }
        } catch (IOException ex) {
            reportIOException(ex);
            return true;
        }
    }
    
    void connectionChanged() {
        if (getFlag(CHECK_CAN_WRITE)) {
            setFlag(CHECK_CAN_WRITE, false);
            fireFileAttributeChangedEvent("DataEditorSupport.read-only.refresh", null, null);  //NOI18N
        }
    }

    final void fireFileAttributeChangedEvent(final String attrName, final Object oldValue, final Object newValue) {
        Enumeration<FileChangeListener> pListeners = (parent != null) ? parent.getListeners() : null;

        fireFileAttributeChangedEvent(getListeners(), new FileAttributeEvent(getOwnerFileObject(), getOwnerFileObject(), attrName, oldValue, newValue));

        if (parent != null && pListeners != null) {
            parent.fireFileAttributeChangedEvent(pListeners, new FileAttributeEvent(parent.getOwnerFileObject(), getOwnerFileObject(), attrName, oldValue, newValue));
        }
    }

    public final boolean canWrite() {
        return canWriteImpl(this);
    }
    
    protected boolean canWriteImpl(RemoteFileObjectBase orig) {
        setFlag(CHECK_CAN_WRITE, true);
        if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
            getFileSystem().addReadOnlyConnectNotification(this);
            return false;
        }
        try {
            RemoteDirectory canonicalParent = RemoteFileSystemUtils.getCanonicalParent(this);
            if (canonicalParent == null) {
                return false;
            } else {
                boolean result = canonicalParent.canWrite(getNameExt());
                if (!result && USE_VCS) {
                    FilesystemInterceptor interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(fileSystem);
                    if (interceptor != null) {
                        result = interceptor.canWriteReadonlyFile(FilesystemInterceptorProvider.toFileProxy(orig.getOwnerFileObject()));
                    }
                }
                if (!result) {
                    setFlag(CHECK_CAN_WRITE, false); // even if we get disconnected, r/o status won't change
                }
                return result;
            }
        } catch (ConnectException ex) {
            return false;
        } catch (IOException ex) {
            reportIOException(ex);
            return false;
        }
    }

    protected void refreshThisFileMetadataImpl(boolean recursive, Set<String> antiLoop, boolean expected) throws ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {
    }
    
    protected void refreshImpl(boolean recursive, Set<String> antiLoop, boolean expected) throws ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {        
    }

    /*package*/ void nonRecursiveRefresh() {
        try {
            refreshImpl(false, null, true);
        } catch (ConnectException ex) {
            RemoteLogger.finest(ex, this);
        } catch (IOException ex) {
            RemoteLogger.info(ex, this);
        } catch (InterruptedException ex) {
            RemoteLogger.finest(ex, this);
        } catch (CancellationException ex) {
            RemoteLogger.finest(ex, this);
        } catch (ExecutionException ex) {
            RemoteLogger.info(ex, this);
        }
    }

    public final void refresh(boolean expected) {
        try {
            refreshImpl(true, null, expected);
        } catch (ConnectException ex) {
            RemoteLogger.finest(ex, this);
        } catch (IOException ex) {
            RemoteLogger.info(ex, this);
        } catch (InterruptedException ex) {
            RemoteLogger.finest(ex, this);
        } catch (CancellationException ex) {
            RemoteLogger.finest(ex, this);
        } catch (ExecutionException ex) {
            RemoteLogger.info(ex, this);
        }
    }

    public final void refresh() {
        refresh(false);
    }
    
    public boolean isRoot() {
        return false;
    }

    public boolean isValid() {
        if(getFlag(MASK_VALID)) {
            RemoteFileObjectBase p = getParent();
            return (p == null) || p.isValid();
        }
        return false;
    }
    
    /*package*/ void invalidate() {
        setFlag(MASK_VALID, false);
    }

    public boolean isVirtual() {
        return false;
    }

    public Date lastModified() {
        if (isPendingRemoteDelivery()) {
            return new Date(-1);
        }
        try {
            RemoteDirectory canonicalParent = RemoteFileSystemUtils.getCanonicalParent(this);
            if (canonicalParent != null) {
                return canonicalParent.lastModified(this);
            }
        } catch (IOException ex) {
            reportIOException(ex);
        }
        return new Date(0); // consistent with File.lastModified(), which returns 0 for inexistent file
    }

    private void reportIOException(IOException ex) {
        System.err.printf("Error in %s: %s\n", remotePath, ex.getMessage());
    }

    public final FileLock lock() throws IOException {
        return lockImpl(this);
    }

    protected FileLock lockImpl(RemoteFileObjectBase orig) throws IOException {
        synchronized(instanceLock) {
            if (lock != null && lock.isValid()) {
                throw new FileAlreadyLockedException(getPath());
            }
            lock =  new FileLock();
        }
        return lock;
    }
    
    public boolean isLocked() {
        boolean res = false;
        synchronized(instanceLock) {
            if (lock != null) {
                res = lock.isValid();
                if (!res) {
                    lock = null;
                }
            }
        }
        return res;
    }
    
    protected boolean checkLock(FileLock aLock) throws IOException {
        if (aLock != null) {
            synchronized(instanceLock) {
                return lock == aLock;
            }
        }
        return true;
    }

    public final void rename(FileLock lock, String name, String ext) throws IOException {
        renameImpl(lock, name, ext, this);
    }

    protected void renameImpl(FileLock lock, String name, String ext, RemoteFileObjectBase orig) throws IOException {
        if (!checkLock(lock)) {
            throw new IOException("Wrong lock"); //NOI18N
        }
        RemoteFileObjectBase p = getParent();
        if (p != null) {
            String newNameExt = composeName(name, ext);
            if (newNameExt.equals(getNameExt())) {
                // nothing to rename
                return;
            }
            if (!p.isValid()) {
                throw new IOException("Can not rename in " + p.getPath());//NOI18N
            }
            // Can not rename in read only folder
            if (!p.canWrite()) {
                throw new IOException("Can not rename in read only " + p.getPath());//NOI18N
            }
            // check there are no other child with such name
            if (p.getOwnerFileObject().getFileObject(newNameExt) != null) {
                throw new IOException("Can not rename to " + newNameExt);//NOI18N
            }
            
            if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
                throw new IOException("No connection: Can not rename in " + p.getPath()); //NOI18N
            }
            try {
                Map<String, Object> map = getAttributesMap();
                p.renameChild(lock, this, newNameExt, orig);
                setAttributeMap(map, this.getOwnerFileObject());
            } catch (ConnectException ex) {
                throw new IOException("No connection: Can not rename in " + p.getPath(), ex); //NOI18N
            } catch (InterruptedException ex) {
                InterruptedIOException outEx = new InterruptedIOException("interrupted: Can not rename in " + p.getPath()); //NOI18N
                outEx.initCause(ex);
                throw outEx;
            } catch (CancellationException ex) {
                throw new IOException("cancelled: Can not rename in " + p.getPath(), ex); //NOI18N
            } catch (ExecutionException ex) {
                throw new IOException("Can not rename to " + newNameExt + ": exception occurred", ex); // NOI18N
            }
        }
    }

    public FileObject copy(FileObject target, String name, String ext) throws IOException {
        return copyImpl(target, name, ext, this);
    }

    protected FileObject copyImpl(FileObject target, String name, String ext, RemoteFileObjectBase orig) throws IOException {
        if (USE_VCS) {
            FilesystemInterceptor interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(fileSystem);
            if (interceptor != null) {
                FileProxyI to = FilesystemInterceptorProvider.toFileProxy(target, name, ext);
                FileProxyI from = FilesystemInterceptorProvider.toFileProxy(orig.getOwnerFileObject());
                interceptor.beforeCopy(from, to);
                FileObject result = null;
                try {
                    final IOHandler copyHandler = interceptor.getCopyHandler(from, to);
                    if (copyHandler != null) {
                        copyHandler.handle();
                        refresh(true);
                        //perfromance bottleneck to call refresh on folder
                        //(especially for many files to be copied)
                        target.refresh(true); // XXX ?
                        result = target.getFileObject(name, ext); // XXX ?
                        assert result != null : "Cannot find " + target + " with " + name + "." + ext;
                        FileUtil.copyAttributes(getOwnerFileObject(), result);
                    } else {
                        result = RemoteFileSystemUtils.copy(getOwnerFileObject(), target, name, ext);
                    }
                } catch (IOException ioe) {
                    throw ioe;
                }
                interceptor.copySuccess(from, to);
                return result;
            }
        }
        return RemoteFileSystemUtils.copy(getOwnerFileObject(), target, name, ext);
    }
    
    public final FileObject move(FileLock lock, FileObject target, String name, String ext) throws IOException {
        return moveImpl(lock, target, name, ext, this);
    }
    
    protected FileObject moveImpl(FileLock lock, FileObject target, String name, String ext, RemoteFileObjectBase orig) throws IOException {
        if (!checkLock(lock)) {
            throw new IOException("Wrong lock"); //NOI18N
        }
        if (USE_VCS) {
            FilesystemInterceptor interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(fileSystem);
            if (interceptor != null) {
                FileProxyI to = FilesystemInterceptorProvider.toFileProxy(target, name, ext);
                FileProxyI from = FilesystemInterceptorProvider.toFileProxy(orig.getOwnerFileObject());
                FileObject result = null;
                try {
                    final IOHandler moveHandler = interceptor.getMoveHandler(from, to);
                    if (moveHandler != null) {
                        Map<String,Object> attr = getAttributesMap();
                        moveHandler.handle();
                        refresh(true);
                        //perfromance bottleneck to call refresh on folder
                        //(especially for many files to be moved)
                        target.refresh(true);
                        result = target.getFileObject(name, ext); // XXX ?
                        assert result != null : "Cannot find " + target + " with " + name + "." + ext;
                        //FileUtil.copyAttributes(this, result);
                        if (result instanceof RemoteFileObject) {
                            setAttributeMap(attr, (RemoteFileObject)result);
                        }
                    } else {
                        result = superMove(lock, target, name, ext);
                    }
                } catch (IOException ioe) {
                    throw ioe;
                }
                interceptor.afterMove(from, to);
                return result;
            }
        }
        return superMove(lock, target, name, ext);
    }
    
    /** Copy-paste from FileObject.copy */
    private FileObject superMove(FileLock lock, FileObject target, String name, String ext) throws IOException {
        if (getOwnerFileObject().getParent().equals(target)) {
            // it is possible to do only rename
            rename(lock, name, ext);
            return this.getOwnerFileObject();
        } else {
            // have to do copy
            FileObject dest = getOwnerFileObject().copy(target, name, ext);
            delete(lock);
            return dest;
        }        
    }
            
    
    private Map<String,Object> getAttributesMap() throws IOException {
        Map<String,Object> map = new HashMap<String,Object>();
        Enumeration<String> attributes = getAttributes();
        while(attributes.hasMoreElements()) {
            String attr = attributes.nextElement();
            map.put(attr, getAttribute(attr));
        }
        return map;
    }
    
    private void setAttributeMap(Map<String,Object> map, RemoteFileObject to) throws IOException {
        for(Map.Entry<String,Object> entry : map.entrySet()) {
            to.setAttribute(entry.getKey(), entry.getValue());
        }
    }
    
    public Object getAttribute(String attrName) {
        return getFileSystem().getAttribute(this, attrName);
    }

    public Enumeration<String> getAttributes() {
        return getFileSystem().getAttributes(this);
    }

    public void setAttribute(String attrName, Object value) throws IOException {
        getFileSystem().setAttribute(this, attrName, value);
    }

    @Deprecated
    public void setImportant(boolean b) {
        // Deprecated. Noithing to do.
    }
    
    protected abstract void renameChild(FileLock lock, RemoteFileObjectBase toRename, String newNameExt, RemoteFileObjectBase orig) 
            throws ConnectException, IOException, InterruptedException, CancellationException, ExecutionException;

    final void renamePath(String newPath) {
        this.remotePath = newPath;
    }

    public void diagnostics(boolean recursive) {}

    private static class ReadOnlyException extends IOException {
        public ReadOnlyException() {
            super("The remote file system is read-only"); //NOI18N
        }
    }

    @Override
    public String toString() {
        String validity;
        if (isValid()) {
            validity = " [valid]"; //NOI18N
        } else {
            validity = getFlag(MASK_VALID) ? " [invalid] (flagged)" : " [invalid]"; //NOI18N
        }
        return getExecutionEnvironment().toString() + ":" + getPath() + validity; // NOI18N
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        // hash code should not be counted by volatale field.
        //int hash = 3;
        //hash = 11 * hash + (this.getFileSystem() != null ? this.getFileSystem().hashCode() : 0);
        //hash = 11 * hash + (this.getExecutionEnvironment() != null ? this.getExecutionEnvironment().hashCode() : 0);
        //String thisPath = this.getPath();
        //hash = 11 * hash + (thisPath != null ? thisPath.hashCode() : 0);
        //return hash;
        return System.identityHashCode(this);
    }
    
    protected static String composeName(String name, String ext) {
        return (ext != null && ext.length() > 0) ? (name + "." + ext) : name;//NOI18N
    }
}
