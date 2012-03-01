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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.net.ConnectException;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport.UploadStatus;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo.FileType;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Vladimir Kvashin
 */
public final class RemotePlainFile extends RemoteFileObjectBase {

    private static final int LOCK_TIMEOUT = Integer.getInteger("remote.rwlock.timeout", 4); // NOI18N
    
    private final char fileTypeChar;
    private SoftReference<CachedRemoteInputStream> fileContentCache = new SoftReference<CachedRemoteInputStream>(null);
    private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    /*package*/ RemotePlainFile(RemoteFileObject wrapper, RemoteFileSystem fileSystem, ExecutionEnvironment execEnv, 
            RemoteDirectory parent, String remotePath, File cache, FileType fileType) {
        super(wrapper, fileSystem, execEnv, parent, remotePath, cache);
        fileTypeChar = fileType.toChar(); // TODO: pass when created
    }

    @Override
    public final RemoteFileObject[] getChildren() {
        return new RemoteFileObject[0];
    }

    @Override
    public final boolean isFolder() {
        return false;
    }

    @Override
    public boolean isData() {
        return true;
    }

    @Override
    public final RemoteFileObject getFileObject(String name, String ext) {
        return null;
    }

    @Override
    public RemoteFileObject getFileObject(String relativePath) {
        // taken from FileObject.getFileObject(String relativePath)
        if (relativePath.startsWith("/")) { //NOI18N
            relativePath = relativePath.substring(1);
        }
        RemoteFileObject res = this.getOwnerFileObject();
        StringTokenizer st = new StringTokenizer(relativePath, "/"); //NOI18N
        while ((res != null) && st.hasMoreTokens()) {
            String nameExt = st.nextToken();
            if (nameExt.equals("..")) { // NOI18N
                res = res.getParent();
            } else {
                if (!nameExt.equals(".")) { //NOI18N
                    res = res.getFileObject(nameExt, null);
                }
            }
        }
        return res;
    }

    @Override
    public RemoteDirectory getParent() {
        return (RemoteDirectory) super.getParent(); // cast guaranteed by constructor
    }

    private final class InputStreamWrapper extends InputStream {

        private final InputStream is;

        public InputStreamWrapper(InputStream is) {
            this.is = is;
        }

        @Override
        public int read() throws IOException {
            return is.read();
        }

        @Override
        public int available() throws IOException {
            return is.available();
        }

        @Override
        public void close() throws IOException {
            try {
                is.close();
            } finally {
                if (rwl.getReadLockCount() > 0) {
                    rwl.readLock().unlock();
                }
            }
        }

        @Override
        public boolean equals(Object obj) {
            return is.equals(obj);
        }

        @Override
        public int hashCode() {
            return is.hashCode();
        }

        @Override
        public synchronized void mark(int readlimit) {
            is.mark(readlimit);
        }

        @Override
        public boolean markSupported() {
            return is.markSupported();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return is.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return is.read(b, off, len);
        }

        @Override
        public synchronized void reset() throws IOException {
            is.reset();
        }

        @Override
        public long skip(long n) throws IOException {
            return is.skip(n);
        }
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        // TODO: check error processing
        try {
            CachedRemoteInputStream stream = fileContentCache.get();
            if (stream != null) {
                CachedRemoteInputStream reuse = stream.reuse();
                if (reuse != null) {
                    return reuse;
                }
                fileContentCache.clear();
            }
            RemoteDirectory parent = RemoteFileSystemUtils.getCanonicalParent(this);
            if (parent == null) {
                return RemoteFileSystemUtils.createDummyInputStream();
            }
            InputStream newStream = parent._getInputStream(this);
            if (newStream instanceof CachedRemoteInputStream) {
                fileContentCache = new SoftReference<CachedRemoteInputStream>((CachedRemoteInputStream) newStream);
            } else {
                if (stream != null) {
                    fileContentCache.clear();
                }

            }
            if (rwl.readLock().tryLock(LOCK_TIMEOUT, TimeUnit.SECONDS)) {
                return new InputStreamWrapper(newStream);
            } else {
                return new InputStream() {

                    @Override
                    public int read() throws IOException {
                        throw new FileAlreadyLockedException("Cannot read from locked file: " + this);  //NOI18N
                    }
                };
            }

            //getParent().ensureChildSync(this);
        } catch (ConnectException ex) {
            return new ByteArrayInputStream(new byte[]{});
        } catch (IOException ex) {
            throw newFileNotFoundException(ex);
        } catch (InterruptedException ex) {
            throw newFileNotFoundException(ex);
        } catch (ExecutionException ex) {
            throw newFileNotFoundException(ex);
        } catch (CancellationException ex) {
            // TODO: do we need this? unfortunately CancellationException is RuntimeException, so I'm not sure
            return new ByteArrayInputStream(new byte[]{});
        }
    }

    private FileNotFoundException newFileNotFoundException(Exception cause) {
        FileNotFoundException ex = new FileNotFoundException("" + getExecutionEnvironment() + ':' + getPath()); //NOI18N
        ex.initCause(cause);
        return ex;
    }

    @Override
    public FileObject createDataImpl(String name, String ext, RemoteFileObjectBase orig) throws IOException {
        throw new IOException("Plain file can not have children"); // NOI18N
    }

    @Override
    public FileObject createFolderImpl(String name, RemoteFileObjectBase orig) throws IOException {
        throw new IOException("Plain file can not have children"); // NOI18N
    }

    @Override
    protected FileLock lockImpl(RemoteFileObjectBase orig) throws IOException {
        FilesystemInterceptorProvider.FilesystemInterceptor interceptor = null;
        if (USE_VCS) {
            interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(getFileSystem());
            if (interceptor != null) {
                if (!canWriteImpl(orig)) {
                    throw new IOException("Cannot lock " + this); // NOI18N
                }
            }
        }
        FileLock lock = super.lockImpl(orig);
        if (interceptor != null) {
            interceptor.fileLocked(FilesystemInterceptorProvider.toFileProxy(orig.getOwnerFileObject()));
        }
        return lock;
    }

    @Override
    protected void postDeleteChild(FileObject child) {
        RemoteLogger.getInstance().log(Level.WARNING, "postDeleteChild is called on {0}", getClass().getSimpleName());
    }

    @Override
    protected boolean deleteImpl(FileLock lock) throws IOException {
        return RemoteFileSystemUtils.delete(getExecutionEnvironment(), getPath(), false);
    }

    @Override
    protected void renameChild(FileLock lock, RemoteFileObjectBase toRename, String newNameExt, RemoteFileObjectBase orig)
            throws ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {
        // plain file can not be container of children
        RemoteLogger.assertTrueInConsole(false, "renameChild is not supported on " + this.getClass() + " path=" + getPath()); // NOI18N
    }

    @Override
    protected OutputStream getOutputStreamImpl(FileLock lock, RemoteFileObjectBase orig) throws IOException {
        try {
            if (!isValid()) {
                throw new FileNotFoundException("FileObject " + this + " is not valid."); //NOI18N
            }
            FilesystemInterceptorProvider.FilesystemInterceptor interceptor = null;
            if (USE_VCS) {
                interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(getFileSystem());
            }
            if (rwl.writeLock().tryLock(LOCK_TIMEOUT, TimeUnit.SECONDS)) {
                return new DelegateOutputStream(interceptor, orig);
            } else {
                return new OutputStream() {

                    @Override
                    public void write(int b) throws IOException {
                        throw new FileAlreadyLockedException("Cannot write to locked file: " + this);  //NOI18N
                    }
                };
            }
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
    }

    // Fixing #206726 - If a remote file is saved frequently, "File modified externally" message appears, user changes are lost
    @Override
    public void refresh(boolean expected) {
        try {
            if (RemoteFileObjectBase.DEFER_WRITES) {
                WritingQueue.getInstance(getExecutionEnvironment()).waitFinished(Collections.<FileObject>singleton(this.getOwnerFileObject()), null);
            }
        } catch (InterruptedException ex) {
            RemoteLogger.finest(ex, this);
        }
    }

    @Override
    public FileType getType() {
        return FileType.fromChar(fileTypeChar);
    }

    private class DelegateOutputStream extends OutputStream {

        private final FileOutputStream delegate;

        public DelegateOutputStream(FilesystemInterceptorProvider.FilesystemInterceptor interceptor, RemoteFileObjectBase orig) throws IOException {
            if (interceptor != null) {
                interceptor.beforeChange(FilesystemInterceptorProvider.toFileProxy(orig.getOwnerFileObject()));
            }
            delegate = new FileOutputStream(getCache());
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            delegate.write(b, off, len);
        }

        @Override
        public void write(int b) throws IOException {
            delegate.write(b);
        }

        @Override
        public void close() throws IOException {
            try {
                delegate.close();
                FileEvent ev = new FileEvent(getOwnerFileObject(), getOwnerFileObject(), true);
                RemotePlainFile.this.setPendingRemoteDelivery(true);
                if (RemoteFileObjectBase.DEFER_WRITES) {
                    getOwnerFileObject().fireFileChangedEvent(getListenersWithParent(), ev);
                    WritingQueue.getInstance(getExecutionEnvironment()).add(RemotePlainFile.this);
                } else {
                    CommonTasksSupport.UploadParameters params = new CommonTasksSupport.UploadParameters(
                            getCache(), getExecutionEnvironment(), getPath(), -1, false, null);
                    Future<UploadStatus> task = CommonTasksSupport.uploadFile(params);
                    try {
                        UploadStatus uploadStatus = task.get();
                        if (uploadStatus.isOK()) {
                            RemoteLogger.getInstance().log(Level.FINEST, "WritingQueue: uploading {0} succeeded", this);
                            getParent().updateStat(RemotePlainFile.this, uploadStatus.getStatInfo());
                            getOwnerFileObject().fireFileChangedEvent(getListenersWithParent(), ev);
                        } else {
                            RemoteLogger.getInstance().log(Level.FINEST, "WritingQueue: uploading {0} failed", this);
                            setPendingRemoteDelivery(false);
                            throw new IOException(uploadStatus.getError() + " " + uploadStatus.getExitCode()); //NOI18N
                        }
                    } catch (InterruptedException ex) {
                        throw newIOException(ex);
                    } catch (ExecutionException ex) {
                        //Exceptions.printStackTrace(ex); // should never be the case - the task is done
                        if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
                            getFileSystem().addPendingFile(RemotePlainFile.this);
                            throw new ConnectException(ex.getMessage());
                        } else {
                            if (RemoteFileSystemUtils.isFileNotFoundException(ex)) {
                                throw new FileNotFoundException(getPath());
                            } else if (ex.getCause() instanceof IOException) {
                                throw (IOException) ex.getCause();
                            } else {
                                throw newIOException(ex);
                            }
                        }
                    }
                }
            } finally {
                if (rwl.isWriteLocked()) {
                    rwl.writeLock().unlock();
                }
            }
        }

        private IOException newIOException(Exception cause) {
            return new IOException("Error uploading " + getPath() + " to " + getExecutionEnvironment() + ':' + //NOI18N
                    cause.getMessage(), cause);
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
        }
    }
}
