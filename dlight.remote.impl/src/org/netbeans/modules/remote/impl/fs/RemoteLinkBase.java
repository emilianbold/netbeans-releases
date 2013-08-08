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


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
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
 *
 * @author vk155633
 */
public abstract class RemoteLinkBase extends RemoteFileObjectBase implements FileChangeListener {
    
    protected RemoteLinkBase(RemoteFileObject wrapper, RemoteFileSystem fileSystem, ExecutionEnvironment execEnv, RemoteFileObjectBase parent, String remotePath) {
        super(wrapper, fileSystem, execEnv, parent, remotePath, null);
    }
    
    protected final void initListeners(boolean add) {
        if (add) {
            getFileSystem().getFactory().addFileChangeListener(getDelegateNormalizedPath(), this);
        } else {
            getFileSystem().getFactory().removeFileChangeListener(getDelegateNormalizedPath(), this);
        }
    }

    public abstract RemoteFileObjectBase getCanonicalDelegate();
    protected abstract String getDelegateNormalizedPath();
    protected abstract RemoteFileObjectBase getDelegateImpl();
 
    protected FileNotFoundException fileNotFoundException(String operation) {
        return new FileNotFoundException("can not " + operation + ' ' + getPath() + ": can not find link target"); //NOI18N
    }
    
    @Override
    public RemoteFileObject[] getChildren() {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate != null) {
            RemoteFileObject[] children = delegate.getChildren();
            for (int i = 0; i < children.length; i++) {
                children[i] = wrapFileObject(children[i], null);
            }
            return children;
        }
        return new RemoteFileObject[0];
    }

    private RemoteFileObject wrapFileObject(RemoteFileObject fo, String relativePath) {
        String childAbsPath;
        if (relativePath == null) {
            childAbsPath = getPath() + '/' + fo.getNameExt();
        } else {
            childAbsPath = RemoteFileSystemUtils.normalize(getPath() + '/' + relativePath);
        }
        // NB: here it can become not a remote link child (in the case it changed remotely and refreshed concurrently)
        RemoteFileObjectBase result = getFileSystem().getFactory().createRemoteLinkChild(this, childAbsPath, fo.getImplementor());
        return result.getOwnerFileObject();
    }

    // ------------ delegating methods -------------------

    @Override
    protected boolean hasCache() {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        return (delegate == null) ? false : delegate.hasCache();
    }

    @Override
    public RemoteFileObject getFileObject(String name, String ext, Set<String> antiLoop) {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate != null) {
            RemoteFileObject fo = delegate.getFileObject(name, ext, antiLoop);
            if (fo != null) {
                fo = wrapFileObject(fo, null);
            }
            return fo;
        }
        return null;
    }

    @Override
    public RemoteFileObject getFileObject(String relativePath, Set<String> antiLoop) {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate != null) {
            RemoteFileObject fo = delegate.getFileObject(relativePath, antiLoop);
            if (fo != null) {
                fo = wrapFileObject(fo, relativePath);
            }
            return fo;
        }
        return null;
    }

    @Override
    public boolean isFolder() {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        return (delegate == null) ? false : delegate.isFolder();
    }

    @Override
    public boolean isData() {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        return (delegate == null) ? true : delegate.isData();
    }

    @Override
    public InputStream getInputStream(boolean checkLock) throws FileNotFoundException {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate == null) {
            throw fileNotFoundException("read"); //NOI18N
        }
        return delegate.getInputStream(checkLock);
    }

    @Override
    public boolean canRead() {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        return (delegate == null) ? false : delegate.canRead();
    }

    @Override
    protected FileLock lockImpl(RemoteFileObjectBase orig) throws IOException {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate != null) {
            return delegate.lockImpl(orig);
        } else {
            throw fileNotFoundException("lock"); //NOI18N
        }
    }

    @Override
    public Date lastModified() {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate != null) {
            return delegate.lastModified();
        } else {
            return new Date(0); // consistent with File.lastModified(), which returns 0 for inexistent file
        }
    }

    @Override
    protected boolean checkLock(FileLock aLock) throws IOException {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate != null) {
            return delegate.checkLock(aLock);
        } else {
            return super.checkLock(aLock);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected boolean isReadOnlyImpl(RemoteFileObjectBase orig) {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        return (delegate == null) ? true : delegate.isReadOnlyImpl(orig);
    }

    @Override
    protected OutputStream getOutputStreamImpl(FileLock lock, RemoteFileObjectBase orig) throws IOException {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate != null) {
            return delegate.getOutputStreamImpl(lock, orig);
        } else {
            throw fileNotFoundException("write"); //NOI18N
        }
    }  
  
    @Override
    protected final void refreshThisFileMetadataImpl(boolean recursive, Set<String> antiLoop, boolean expected) throws ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {
        // TODO: this dummy implementation is far from optimal in terms of performance. It needs to be improved.
        if (getParent() != null) {
            getParent().refreshImpl(false, antiLoop, expected);
        }
    }    
    
    @Override
    protected final void refreshImpl(boolean recursive, Set<String> antiLoop, boolean expected) throws ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {
        if (antiLoop == null) {
            antiLoop = new HashSet<String>();
        }
        if (antiLoop.contains(getPath())) {
            return;
        } else {
            antiLoop.add(getPath());
        }
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        // For link we need to refresh both delegate and link metadata itself
        refreshThisFileMetadataImpl(recursive, antiLoop, expected);
        if (delegate != null) {
            delegate.refreshImpl(recursive, antiLoop, expected);
        } else {
            RemoteLogger.log(Level.FINEST, "Null delegate for link {0}", this); //NOI18N
        }
    }
    
    @Override
    protected void renameChild(FileLock lock, RemoteFileObjectBase toRename, String newNameExt, RemoteFileObjectBase orig) 
            throws ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {
        // all work in it's wrapped delegate
        RemoteLogger.assertTrueInConsole(false, "renameChild is not supported on " + this.getClass() + " path=" + getPath()); // NOI18N
    }
    
    @Override
    protected RemoteFileObject createFolderImpl(String name, RemoteFileObjectBase orig) throws IOException {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate != null) {
            return wrapFileObject(delegate.createFolderImpl(name, orig), null);
        } else {
            throw fileNotFoundException("create a folder in"); //NOI18N
        }
    }

    @Override
    protected RemoteFileObject createDataImpl(String name, String ext, RemoteFileObjectBase orig) throws IOException {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate != null) {
            return wrapFileObject(delegate.createDataImpl(name, ext, orig), null);
        } else {
            throw fileNotFoundException("create a file in"); //NOI18N
        }
    }

    @Override
    public boolean canWriteImpl(RemoteFileObjectBase orig) {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        return (delegate == null) ? false : delegate.canWriteImpl(orig);
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
        fireFileAttributeChangedEvent(getListeners(), (FileAttributeEvent)transform(fe));
    }

    @Override
    public void fileChanged(FileEvent fe) {
        fireFileChangedEvent(getListeners(), transform(fe));
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        fireFileDataCreatedEvent(getListeners(), transform(fe));
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        if (!isCyclicLink()) {
            fireFileDeletedEvent(getListeners(), transform(fe));
        }
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        fireFileFolderCreatedEvent(getListeners(), transform(fe));
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        fireFileRenamedEvent(getListeners(), (FileRenameEvent)transform(fe));
    }

    public boolean isCyclicLink() {
        Set<RemoteFileObjectBase> antiCycle = new HashSet<RemoteFileObjectBase>();
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate == null && getPath() != null) {
            // self-referencing link
            return true;
        }
        while (delegate != null) {
            if (delegate instanceof RemoteLinkBase) {
                if (antiCycle.contains(delegate)) return true;
                antiCycle.add(delegate);
                delegate = ((RemoteLinkBase) delegate).getCanonicalDelegate();
            } else {
                break;
            }
        }        
        return false;
    }
    
    private FileEvent transform(FileEvent fe) {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate != null) {
            FileObject src = transform((FileObject) fe.getSource(), delegate);
            FileObject file = transform(fe.getFile(), delegate);
            if (file != fe.getFile() || src != fe.getSource()) {
                if (fe instanceof FileRenameEvent) {
                    FileRenameEvent fre = (FileRenameEvent) fe;
                    fe = new FileRenameEvent(src, file, fre.getName(), fre.getExt(), fe.isExpected());
                } else if (fe instanceof FileAttributeEvent) {
                    FileAttributeEvent fae = (FileAttributeEvent) fe;
                    fe = new FileAttributeEvent(src, file, fae.getName(), fae.getOldValue(), fae.getNewValue(), fe.isExpected());
                } else {
                    fe = new FileEvent(src, file, fe.isExpected(), fe.getTime());
                }
            }
        }
        return fe;
    }

    private FileObject transform(FileObject fo, RemoteFileObjectBase delegate) {
        if (fo instanceof RemoteFileObject) {
            RemoteFileObjectBase originalFO = ((RemoteFileObject) fo).getImplementor();
            if (originalFO == delegate) {
                return this.getOwnerFileObject();
            }
            if (originalFO.getParent() == delegate) {
                String path = RemoteLinkBase.this.getPath() + '/' + fo.getNameExt();
                // NB: here it can become not a remote link child (in the case it changed remotely and refreshed concurrently)
                RemoteFileObjectBase linkChild = getFileSystem().getFactory().createRemoteLinkChild(this, path, originalFO);
                return linkChild.getOwnerFileObject();
            }
        }
        return fo;
    }
    
    @Override
    protected byte[] getMagic() {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate != null) {
            return delegate.getMagic();
        }
        return null;
    }
}
