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

import java.lang.ref.WeakReference;
import java.util.*;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider.FileProxyI;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider.FilesystemInterceptor;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 *
 */
final class RecursiveListener extends WeakReference<FileObject>
implements FileChangeListener {
    private final FileChangeListener fcl;
    private final Set<FileObject> kept;

    public RecursiveListener(RemoteFileObject source, FileChangeListener fcl, boolean keep) {
        super(source);
        this.fcl = fcl;
        this.kept = keep ? new HashSet<FileObject>() : null;
        addAll(source);
        try {
            init(source, -1, false);
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void init(RemoteFileObject source, long previous, boolean expected) throws FileStateInvalidException {
        if (RemoteFileObjectBase.USE_VCS) {
            try {
                final FileSystem fileSystem = source.getFileSystem();
                source.getFileSystem().setInsideVCS(true);
                FilesystemInterceptor interseptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(fileSystem);
                LinkedList<FileProxyI> list = new LinkedList<>();
                if (interseptor != null) {
                    long tc = interseptor.refreshRecursively(FilesystemInterceptorProvider.toFileProxy(source), previous, list);
                }
                for (FileProxyI proxy : list) {
                    FileObject fo = source.getFileSystem().findResource(proxy.getPath());
                    // TODO what should be fire?
                }
            } finally {
                source.getFileSystem().setInsideVCS(false);
            }
        }
    }

    private void addAll(FileObject folder) {
        if (kept != null) {
            kept.add(folder);
            Enumeration<? extends FileObject> en = folder.getChildren(true);
            while (en.hasMoreElements()) {
                FileObject fo = en.nextElement();
                kept.add(fo);
            }
        }
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        FileObject thisFo = this.get();
        if (thisFo != null && isParentOf(thisFo, fe.getFile())) {
            fcl.fileRenamed(fe);
        }
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        FileObject thisFo = this.get();
        final FileObject file = fe.getFile();
        if (thisFo != null && isParentOf(thisFo, file)) {
            fcl.fileFolderCreated(fe);
            addAll(file);
        }
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        FileObject thisFo = this.get();
        final FileObject file = fe.getFile();
        if (thisFo != null && isParentOf(thisFo, file)) {
            fcl.fileDeleted(fe);
            if (kept != null) {
                kept.remove(file);
            }
        }
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        FileObject thisFo = this.get();
        final FileObject file = fe.getFile();
        if (thisFo != null && isParentOf(thisFo, file)) {
            fcl.fileDataCreated(fe);
            if (kept != null) {
                kept.add(file);
            }
        }
    }

    @Override
    public void fileChanged(FileEvent fe) {
        FileObject thisFo = this.get();
        if (thisFo != null && isParentOf(thisFo, fe.getFile())) {
            fcl.fileChanged(fe);
        }
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
        FileObject thisFo = this.get();
        if (thisFo != null && isParentOf(thisFo, fe.getFile())) {
            fcl.fileAttributeChanged(fe);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RecursiveListener other = (RecursiveListener) obj;
        if (this.fcl != other.fcl && (this.fcl == null || !this.fcl.equals(other.fcl))) {
            return false;
        }
        final FileObject otherFo = other.get();
        final FileObject thisFo = this.get();
        if (thisFo != otherFo && (thisFo == null || !thisFo.equals(otherFo))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final FileObject thisFo = this.get();
        int hash = 3;
        hash = 37 * hash + (this.fcl != null ? this.fcl.hashCode() : 0);
        hash = 13 * hash + (thisFo != null ? thisFo.hashCode() : 0);
        return hash;
    }

    private boolean isParentOf(FileObject folder, FileObject fo) {
        Parameters.notNull("folder", folder);  //NOI18N
        Parameters.notNull("fileObject", fo);  //NOI18N
        if (folder.isFolder()) {
            try {
                if (folder.getFileSystem() != fo.getFileSystem()) {
                    return false;
                }
            } catch (FileStateInvalidException e) {
                return false;
            }
            FileObject parent = fo.getParent();
            while (parent != null) {
                if (parent.equals(folder)) { // links are wrapper, == does not suite here!
                    return true;
                }
                parent = parent.getParent();
            }
        }
        return false;        
    }
}
