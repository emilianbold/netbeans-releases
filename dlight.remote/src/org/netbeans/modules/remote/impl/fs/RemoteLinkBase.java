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
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

/**
 *
 * @author vk155633
 */
public abstract class RemoteLinkBase extends RemoteFileObjectBase {
    
    protected RemoteLinkBase(RemoteFileSystem fileSystem, ExecutionEnvironment execEnv, FileObject parent, String remotePath) {
        super(fileSystem, execEnv, parent, remotePath, null);
    }
    
    public abstract RemoteFileObjectBase getDelegate();

    protected FileNotFoundException fileNotFoundException(String operation) {
        return new FileNotFoundException("can not " + operation + ' ' + remotePath + ": can not find link target"); //NOI18N
    }
    
    @Override
    public RemoteFileObjectBase[] getChildren() {
        RemoteFileObjectBase delegate = getDelegate();
        if (delegate != null) {
            RemoteFileObjectBase[] children = delegate.getChildren();
            for (int i = 0; i < children.length; i++) {
                children[i] = wrapFileObject(children[i], null);
            }
            return children;
        }
        return new RemoteFileObjectBase[0];
    }

    private RemoteFileObjectBase wrapFileObject(RemoteFileObjectBase fo, String relativePath) {
        String childAbsPath;
        if (relativePath == null) {
            childAbsPath = remotePath + '/' + fo.getNameExt();
        } else {
            relativePath = RemoteFileSystemUtils.normalize(remotePath + '/' + relativePath);
        }
        return new RemoteLinkChild(fileSystem, execEnv, this, remotePath + '/' + fo.getNameExt(), fo);
    }
    
    // ------------ delegating methods -------------------

    @Override
    public RemoteFileObjectBase getFileObject(String name, String ext) {
        RemoteFileObjectBase delegate = getDelegate();
        if (delegate != null) {
            RemoteFileObjectBase fo = delegate.getFileObject(name, ext);
            if (fo != null) {
                fo = wrapFileObject(fo, null);
            }
            return fo;
        }
        return null;
    }

    @Override
    public RemoteFileObjectBase getFileObject(String relativePath) {
        RemoteFileObjectBase delegate = getDelegate();
        if (delegate != null) {
            RemoteFileObjectBase fo = delegate.getFileObject(relativePath);
            if (fo != null) {
                fo = wrapFileObject(fo, relativePath);
            }
            return fo;
        }
        return null;
    }

    @Override
    public void refresh() {
        RemoteFileObjectBase delegate = getDelegate();
        if (delegate != null) {
            delegate.refresh();
        }
    }

    @Override
    public void refresh(boolean expected) {
        RemoteFileObjectBase delegate = getDelegate();
        if (delegate != null) {
            delegate.refresh(expected);
        }
    }

    @Override
    public boolean isFolder() {
        RemoteFileObjectBase delegate = getDelegate();
        return (delegate == null) ? false : delegate.isFolder();
    }

    @Override
    public boolean isData() {
        RemoteFileObjectBase delegate = getDelegate();
        return (delegate == null) ? true : delegate.isData();
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        RemoteFileObjectBase delegate = getDelegate();
        if (delegate == null) {
            throw fileNotFoundException("read"); //NOI18N
        }
        return delegate.getInputStream();
    }

    @Override
    public FileObject createData(String name) throws IOException {
        RemoteFileObjectBase delegate = getDelegate();
        return (delegate == null) ? null : delegate.createData(name);
    }

    @Override
    public boolean canRead() {
        RemoteFileObjectBase delegate = getDelegate();
        return (delegate == null) ? false : delegate.canRead();
    }

    @Override
    public void removeFileChangeListener(FileChangeListener fcl) {
        RemoteFileObjectBase delegate = getDelegate();
        if (delegate != null) {
            delegate.removeFileChangeListener(fcl);
        }
    }

    @Override
    public FileLock lock() throws IOException {
        RemoteFileObjectBase delegate = getDelegate();
        if (delegate != null) {
            return delegate.lock();
        } else {
            throw fileNotFoundException("lock"); //NOI18N
        }
    }

//    @Override
//    public Date lastModified() {
//        RemoteFileObjectBase delegate = getDelegate();
//        return (delegate == null) ? null : delegate.lastModified();
//    }

    @Override
    public boolean isValid() {
        RemoteFileObjectBase delegate = getDelegate();
        return delegate != null && delegate.isValid();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isReadOnly() {
        RemoteFileObjectBase delegate = getDelegate();
        return (delegate == null) ? true : delegate.isReadOnly();
    }

    @Override
    public OutputStream getOutputStream(FileLock lock) throws IOException {
        RemoteFileObjectBase delegate = getDelegate();
        if (delegate != null) {
            return delegate.getOutputStream(lock);
        } else {
            throw fileNotFoundException("write"); //NOI18N
        }
    }

    @Override
    protected void ensureSync() throws ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {
        RemoteFileObjectBase delegate = getDelegate();
        if (delegate != null) {
            delegate.ensureSync();
        }
    }

    @Override
    public FileObject createFolder(String name) throws IOException {
        RemoteFileObjectBase delegate = getDelegate();
        if (delegate != null) {
            return delegate.createFolder(name);
        } else {
            throw fileNotFoundException("create a folder in"); //NOI18N
        }
    }

    @Override
    public FileObject createData(String name, String ext) throws IOException {
        RemoteFileObjectBase delegate = getDelegate();
        if (delegate != null) {
            return delegate.createData(name, ext);
        } else {
            throw fileNotFoundException("create a file in"); //NOI18N
        }
    }

    @Override
    public boolean canWrite() {
        RemoteFileObjectBase delegate = getDelegate();
        return (delegate == null) ? false : delegate.canWrite();
    }

    @Override
    public void addFileChangeListener(FileChangeListener fcl) {
        RemoteFileObjectBase delegate = getDelegate();
        if (delegate != null) {
            delegate.addFileChangeListener(fcl);
        }
    }

}
