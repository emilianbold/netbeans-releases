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
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.dlight.libs.common.InvalidFileObjectSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.remote.support.RemoteLogger;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Kvashin
 */
public abstract class RemoteFileObjectBase extends FileObject implements Serializable {

    private final RemoteFileSystem fileSystem;
    private final RemoteFileObjectBase parent;
    private final String remotePath;
    private final File cache;
    private CopyOnWriteArrayList<FileChangeListener> listeners = new CopyOnWriteArrayList<FileChangeListener>();
    private final FileLock lock = new FileLock();
    static final long serialVersionUID = 1931650016889811086L;

    private byte flags;
    
    private static final byte MASK_VALID = 1;
    private static final byte CHECK_CAN_WRITE = 2;

    public RemoteFileObjectBase(RemoteFileSystem fileSystem, ExecutionEnvironment execEnv,
            RemoteFileObjectBase parent, String remotePath, File cache) {
        RemoteLogger.assertTrue(execEnv.isRemote());        
        //RemoteLogger.assertTrue(cache.exists(), "Cache should exist for " + execEnv + "@" + remotePath); //NOI18N
        this.fileSystem = fileSystem;
        this.parent = parent;
        this.remotePath = remotePath; // RemoteFileSupport.fromFixedCaseSensitivePathIfNeeded(remotePath);
        this.cache = cache;
        setFlag(MASK_VALID, true);
    }
    
    private boolean getFlag(byte mask) {
        return (flags & mask) == mask;
    }
    
    private void setFlag(byte mask, boolean value) {
        if (value) {
            flags |= mask;
        } else {
            flags &= ~mask;
        }
    }
    
    public ExecutionEnvironment getExecutionEnvironment() {
        return fileSystem.getExecutionEnvironment();
    }

    protected File getCache() {
        return cache;
    }

    @Override
    public String getPath() {
        return this.remotePath;
    }

    @Override
    public void addFileChangeListener(FileChangeListener fcl) {
        listeners.add(fcl);
    }
    
    @Override
    public void removeFileChangeListener(FileChangeListener fcl) {
        listeners.remove(fcl);
    }
    
    protected final Enumeration<FileChangeListener> getListeners() {
        return Collections.enumeration(listeners);
    }
    
    protected abstract void deleteImpl() throws IOException;

    protected abstract void postDeleteChild(FileObject child);
    
    @Override
    public void delete(FileLock lock) throws IOException {
        deleteImpl();
        invalidate();
        RemoteFileObjectBase p = getParent();
        if (p != null) {
            p.postDeleteChild(this);
        }
    }

    @Override
    public String getExt() {
        String nameExt = getNameExt();
        int pointPos = nameExt.lastIndexOf('.');
        return (pointPos < 0) ? "" : nameExt.substring(pointPos + 1);
    }

    @Override
    public RemoteFileSystem getFileSystem() {
        return fileSystem;
    }

    protected RemoteFileSupport getRemoteFileSupport() {
        return getFileSystem().getRemoteFileSupport();
    }

    @Override
    public String getName() {
        String nameExt = getNameExt();
        int pointPos = nameExt.lastIndexOf('.');
        return (pointPos < 0) ? nameExt : nameExt.substring(0, pointPos);
    }

    @Override
    public String getNameExt() {
        int slashPos = this.getPath().lastIndexOf('/');
        return (slashPos < 0) ? "" : this.getPath().substring(slashPos + 1);
    }

    @Override
    public OutputStream getOutputStream(FileLock lock) throws IOException {
        throw new ReadOnlyException();
    }

    @Override
    abstract public RemoteFileObjectBase getFileObject(String relativePath);

    @Override
    abstract public RemoteFileObjectBase getFileObject(String name, String ext);

    @Override
    abstract public RemoteFileObjectBase[] getChildren();

    @Override
    public RemoteFileObjectBase getParent() {
        return parent;
    }

    @Override
    public long getSize() {
        RemoteDirectory canonicalParent;
        try {
            canonicalParent = RemoteFileSystemUtils.getCanonicalParent(this);
            if (canonicalParent != null) {
                return canonicalParent.getSize(this);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return 0;
    }

    @Override
    @Deprecated
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public boolean canRead() {
        try {
            RemoteDirectory canonicalParent = RemoteFileSystemUtils.getCanonicalParent(this);
            if (canonicalParent == null) {
                return true;
            } else {
                return canonicalParent.canRead(getNameExt());
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return true;
        }
    }

    void connectionChanged() {
        if (getFlag(CHECK_CAN_WRITE)) {
            setFlag(CHECK_CAN_WRITE, false);
            // react the same way org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObj does
            fireFileAttributeChangedEvent(getListeners(), 
                    new FileAttributeEvent(this, this, "DataEditorSupport.read-only.refresh", null, null)); //NOI18N
        }
    }
    
    @Override
    public boolean canWrite() {
        setFlag(CHECK_CAN_WRITE, true);
        if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {            
            return false;
        }
        try {
            RemoteDirectory canonicalParent = RemoteFileSystemUtils.getCanonicalParent(this);
            if (canonicalParent == null) {
                return false;
            } else {
                boolean result = canonicalParent.canWrite(getNameExt());
                if (!result) {
                    setFlag(CHECK_CAN_WRITE, false); // even if we get disconnected, r/o status won't change
                }
                return result;
            }
        } catch (ConnectException ex) {
            return false;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

    protected void refreshImpl() throws ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {        
    }

    @Override
    public void refresh(boolean expected) {
        try {
            refreshImpl();
        } catch (ConnectException ex) {
            RemoteLogger.finest(ex);
        } catch (IOException ex) {
            RemoteLogger.info(ex);
        } catch (InterruptedException ex) {
            RemoteLogger.finest(ex);
        } catch (CancellationException ex) {
            RemoteLogger.finest(ex);
        } catch (ExecutionException ex) {
            RemoteLogger.info(ex);
        }
    }

    @Override
    public void refresh() {
        refresh(false);
    }
    
    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public boolean isValid() {
        return getFlag(MASK_VALID);
    }

    /*package*/ void invalidate() {
        setFlag(MASK_VALID, false);
    }

    @Override
    public boolean isVirtual() {
        return false;
    }

    @Override
    public Date lastModified() {
        try {
            RemoteDirectory canonicalParent = RemoteFileSystemUtils.getCanonicalParent(this);
            if (canonicalParent != null) {
                return canonicalParent.lastModified(this);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return new Date(0); // consistent with File.lastModified(), which returns 0 for inexistent file
    }

    @Override
    public FileLock lock() throws IOException {
        return lock;
    }

    @Override
    public void rename(FileLock lock, String name, String ext) throws IOException {
        throw new ReadOnlyException();
    }

    @Override
    public Object getAttribute(String attrName) {
        return getFileSystem().getAttribute(this, attrName);
    }

    @Override
    public Enumeration<String> getAttributes() {
        return getFileSystem().getAttributes(this);
    }

    @Override
    public void setAttribute(String attrName, Object value) throws IOException {
        getFileSystem().setAttribute(this, attrName, value);
    }

    @Override
    @Deprecated
    public void setImportant(boolean b) {
        // Deprecated. Noithing to do.
    }

    public abstract FileType getType();

    private static class ReadOnlyException extends IOException {
        public ReadOnlyException() {
            super("The remote file system is read-only"); //NOI18N
        }
    }

    @Override
    public String toString() {
        return getExecutionEnvironment().toString() + ":" + getPath(); //NOI18N
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RemoteFileObjectBase other = (RemoteFileObjectBase) obj;
        if (this.getFileSystem() != other.getFileSystem() && (this.getFileSystem() == null || !this.fileSystem.equals(other.fileSystem))) {
            return false;
        }
        if (this.getExecutionEnvironment() != other.getExecutionEnvironment() && (this.getExecutionEnvironment() == null || !this.getExecutionEnvironment().equals(other.getExecutionEnvironment()))) {
            return false;
        }
        if (this.getCache() != other.getCache() && (this.getCache() == null || !this.cache.equals(other.cache))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + (this.getFileSystem() != null ? this.getFileSystem().hashCode() : 0);
        hash = 11 * hash + (this.getExecutionEnvironment() != null ? this.getExecutionEnvironment().hashCode() : 0);
        hash = 11 * hash + (this.getCache() != null ? this.getCache().hashCode() : 0);
        return hash;
    }
    
   /* Java serialization*/ Object writeReplace() throws ObjectStreamException {
        return new SerializedForm(getExecutionEnvironment(), getPath());
    }
    
    private static class SerializedForm implements Serializable {
        
        private final ExecutionEnvironment env;
        private final String remotePath;

        public SerializedForm(ExecutionEnvironment env, String remotePath) {
            this.env = env;
            this.remotePath = remotePath;
        }
                
        /* Java serialization*/ Object readResolve() throws ObjectStreamException {
            RemoteFileSystem fs = RemoteFileSystemManager.getInstance().getFileSystem(env);
            FileObject fo = fs.findResource(remotePath);
            if (fo == null) {
                fo = InvalidFileObjectSupport.getInvalidFileObject(fs, remotePath);
            }
            return fo;
        }
    }    
}
