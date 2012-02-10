/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remote.impl.fs;

import com.sun.org.apache.xerces.internal.impl.dv.dtd.NMTOKENDatatypeValidator;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;

/**
 * The only class that comes outside.
 * Fixing #208084 - Remote file system should keep FileObject instances when a file is replaced with symlink and vice versa 
 * @author vk155633
 */
public final class RemoteFileObject extends FileObject implements Serializable {

    private final RemoteFileSystem fileSystem;
    private RemoteFileObjectBase delegate;
    
    /*package*/ RemoteFileObject(RemoteFileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }
    
    /*package*/ void setDelegate(RemoteFileObjectBase delegate) {
        this.delegate = delegate;
    }

    public RemoteFileObjectBase getDelegate() {
        if (delegate == null) {
            String errMsg = "Null delegate: " + getPath(); // NOI18N
            RemoteLogger.getInstance().log(Level.WARNING, errMsg, new NullPointerException(errMsg));
        }
        return delegate;
    }
    
    @Override
    public RemoteFileSystem getFileSystem() {
        return fileSystem;
    }

    public ExecutionEnvironment getExecutionEnvironment() {
        return fileSystem.getExecutionEnvironment();
    }

    // <editor-fold desc="Moved from RemoteFileObjectFile.">
    
    private ThreadLocal<AtomicInteger> magic = new ThreadLocal<AtomicInteger>() {

        @Override
        protected AtomicInteger initialValue() {
            return new AtomicInteger(0);
        }
    };

    @Override
    public String getMIMEType() {
        magic.get().incrementAndGet();
        try {
            return super.getMIMEType();
        } finally {
            magic.get().decrementAndGet();
        }
    }

    @Override
    public String getMIMEType(String... withinMIMETypes) {
        magic.get().incrementAndGet();
        try {
            return super.getMIMEType(withinMIMETypes);
        } finally {
            magic.get().decrementAndGet();
        }
    }

    protected boolean isMimeResolving() {
        if (magic.get().intValue() > 0) {
            return true;
        }
        for(StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if ("org.openide.filesystems.MIMESupport".equals(element.getClassName()) && "findMIMEType".equals(element.getMethodName()) ||  //NOI18N
                "org.openide.loaders.DefaultDataObject".equals(element.getClassName()) && "fixCookieSet".equals(element.getMethodName())) { //NOI18N
                return true;
            }
        }
        return false;
    }
    
    protected byte[] getMagic() {
        try {
            RemoteDirectory parent = RemoteFileSystemUtils.getCanonicalParent(this.getDelegate());
            if (parent != null) {
                return parent.getMagic(this.getDelegate());
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        return null;
    }
    
    // <editor-fold">
    
    // <editor-fold desc="Moved from RemoteFileObjectBase.">
    
    /** Overridden to make possible calls from other package classes */
    @Override
    protected void fireFileChangedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        super.fireFileChangedEvent(en, fe);
    }

    /** Overridden to make possible calls from other package classes */
    @Override
    protected void fireFileDeletedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        super.fireFileDeletedEvent(en, fe);
    }

    /** Overridden to make possible calls from other package classes */
    @Override
    protected void fireFileAttributeChangedEvent(Enumeration<FileChangeListener> en, FileAttributeEvent fe) {
        super.fireFileAttributeChangedEvent(en, fe);
    }
    
    /** Overridden to make possible calls from other package classes */
    @Override
    protected void fireFileDataCreatedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        super.fireFileDataCreatedEvent(en, fe);
    }

    /** Overridden to make possible calls from other package classes */
    @Override
    protected void fireFileFolderCreatedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        super.fireFileFolderCreatedEvent(en, fe);
    }

    /** Overridden to make possible calls from other package classes */
    @Override
    protected void fireFileRenamedEvent(Enumeration<FileChangeListener> en, FileRenameEvent fe) {
        super.fireFileRenamedEvent(en, fe);
    }
    

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Delegating all methods. Keep collapsed.">
    
    @Override
    public int hashCode() {
        return getDelegate().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RemoteFileObject other = (RemoteFileObject) obj;
        if (this.getFileSystem() != other.getFileSystem() && (this.getFileSystem() == null || !this.fileSystem.equals(other.fileSystem))) {
            return false;
        }
        if (this.getExecutionEnvironment() != other.getExecutionEnvironment() && (this.getExecutionEnvironment() == null || !this.getExecutionEnvironment().equals(other.getExecutionEnvironment()))) {
            return false;
        }
        String thisPath = this.getPath();
        String otherPath = other.getPath();
        if (thisPath != otherPath && (thisPath == null || !thisPath.equals(otherPath))) {
            return false;
        }
        RemoteLogger.log(Level.WARNING, "Multiple instances for file objects: {0} and {1}", this, other);
        return true;
    }

    @Override
    public String toString() {
        return getDelegate().toString();
    }

    @Override
    public void setImportant(boolean b) {
        getDelegate().setImportant(b);
    }

    @Override
    public void setAttribute(String attrName, Object value) throws IOException {
        getDelegate().setAttribute(attrName, value);
    }

    @Override
    public void rename(FileLock lock, String name, String ext) throws IOException {
        getDelegate().rename(lock, name, ext);
    }

    @Override
    public void removeRecursiveListener(FileChangeListener fcl) {
        getDelegate().removeRecursiveListener(fcl);
    }

    @Override
    public void removeFileChangeListener(FileChangeListener fcl) {
        getDelegate().removeFileChangeListener(fcl);
    }

    @Override
    public void refresh() {
        getDelegate().refresh();
    }

    @Override
    public void refresh(boolean expected) {
        getDelegate().refresh(expected);
    }

    @Override
    public FileObject move(FileLock lock, FileObject target, String name, String ext) throws IOException {
        return getDelegate().move(lock, target, name, ext);
    }

    @Override
    public FileLock lock() throws IOException {
        return getDelegate().lock();
    }

    @Override
    public Date lastModified() {
        return getDelegate().lastModified();
    }

    @Override
    public boolean isVirtual() {
        return getDelegate().isVirtual();
    }

    @Override
    public boolean isValid() {
        return getDelegate().isValid();
    }

    @Override
    public boolean isRoot() {
        return getDelegate().isRoot();
    }

    @Override
    public boolean isReadOnly() {
        return getDelegate().isReadOnly();
    }

    @Override
    public boolean isLocked() {
        return getDelegate().isLocked();
    }

    @Override
    public boolean isFolder() {
        return getDelegate().isFolder();
    }

    @Override
    public boolean isData() {
        return getDelegate().isData();
    }

    @Override
    public long getSize() {
        return getDelegate().getSize();
    }

    @Override
    public String getPath() {
        return getDelegate().getPath();
    }

    @Override
    public RemoteFileObject getParent() {
        RemoteFileObjectBase parent = getDelegate().getParent();
        return (parent == null) ? null : parent.getWrapper();
    }

    @Override
    public OutputStream getOutputStream(FileLock lock) throws IOException {
        return getDelegate().getOutputStream(lock);
    }

    @Override
    public String getNameExt() {
        return getDelegate().getNameExt();
    }

    @Override
    public String getName() {
        return getDelegate().getName();
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        if (!getDelegate().getCache().exists()) {
            if (isMimeResolving()) {
                byte[] b = getMagic();
                if (b != null) {
                    return new ByteArrayInputStream(b);
                }
            }
        }
        
        return getDelegate().getInputStream();
    }

    @Override
    public RemoteFileObject getFileObject(String relativePath) {
        return getDelegate().getFileObject(relativePath);
    }

    @Override
    public RemoteFileObject getFileObject(String name, String ext) {
        return getDelegate().getFileObject(name, ext);
    }

    @Override
    public String getExt() {
        return getDelegate().getExt();
    }

    @Override
    public RemoteFileObject[] getChildren() {
        return getDelegate().getChildren();
    }

    @Override
    public Enumeration<String> getAttributes() {
        return getDelegate().getAttributes();
    }

    @Override
    public Object getAttribute(String attrName) {
        return getDelegate().getAttribute(attrName);
    }

    @Override
    public void delete(FileLock lock) throws IOException {
        getDelegate().delete(lock);
    }

    @Override
    public FileObject createFolder(String name) throws IOException {
        return getDelegate().createFolder(name);
    }

    @Override
    public FileObject createData(String name) throws IOException {
        return getDelegate().createData(name);
    }

    @Override
    public FileObject createData(String name, String ext) throws IOException {
        return getDelegate().createData(name, ext);
    }

    @Override
    public boolean canWrite() {
        return getDelegate().canWrite();
    }

    @Override
    public boolean canRead() {
        return getDelegate().canRead();
    }

    @Override
    public void addRecursiveListener(FileChangeListener fcl) {
        getDelegate().addRecursiveListener(fcl);
    }

    @Override
    public void addFileChangeListener(FileChangeListener fcl) {
        getDelegate().addFileChangeListener(fcl);
    }
    // </editor-fold>
}
