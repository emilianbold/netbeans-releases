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
package org.netbeans.modules.remotefs.versioning.spi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.remote.impl.fileoperations.FilesystemInterceptorProvider;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystem;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.filesystems.VCSFilesystemInterceptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;

/**
 *
 * @author Alexander Simon
 */
@org.openide.util.lookup.ServiceProvider(service = FilesystemInterceptorProvider.class, position = 1000)
public class FilesystemInterceptorProviderImpl extends FilesystemInterceptorProvider {

    @Override
    public FilesystemInterceptor getFilesystemInterceptor(FileSystem fs) {
        return new FilesystemInterceptorImpl(fs);
    }

    private static final class FilesystemInterceptorImpl implements FilesystemInterceptor {
        private final FileSystem fs;
        
        public FilesystemInterceptorImpl(FileSystem fs) {
            this.fs = fs;
        }

        private VCSFilesystemInterceptor getDelegate() {
            return Lookup.getDefault().lookup(VCSFilesystemInterceptor.class);
        }

        @Override
        public boolean canWrite(FileProxyI file) {
            return getDelegate().canWrite(toVCSFileProxy(file));
        }

        @Override
        public Object getAttribute(FileProxyI file, String attrName) {
            return getDelegate().getAttribute(toVCSFileProxy(file), attrName);
        }

        @Override
        public void beforeChange(FileProxyI file) {
            getDelegate().beforeChange(toVCSFileProxy(file));
        }

        @Override
        public void fileChanged(FileProxyI file) {
            getDelegate().fileChanged(toVCSFileProxy(file));
        }

        @Override
        public DeleteHandler getDeleteHandler(FileProxyI file) {
            final VCSFilesystemInterceptor.DeleteHandler deleteHandler = getDelegate().getDeleteHandler(toVCSFileProxy(file));
            return new DeleteHandler() {

                @Override
                public boolean delete(FileProxyI file) {
                    return deleteHandler.delete(toVCSFileProxy(file));
                }
            };
        }

        @Override
        public void deleteSuccess(FileProxyI file) {
            getDelegate().deleteSuccess(toVCSFileProxy(file));
        }

        @Override
        public void deletedExternally(FileProxyI file) {
            getDelegate().deletedExternally(toVCSFileProxy(file));
        }

        @Override
        public void beforeCreate(FileProxyI parent, String name, boolean isFolder) {
            getDelegate().beforeCreate(toVCSFileProxy(parent), name, isFolder);
        }

        @Override
        public void createFailure(FileProxyI parent, String name, boolean isFolder) {
            getDelegate().createFailure(toVCSFileProxy(parent), name, isFolder);
        }

        @Override
        public void createSuccess(FileProxyI fo) {
            getDelegate().createSuccess(toVCSFileProxy(fo));
        }

        @Override
        public void createdExternally(FileProxyI fo) {
            getDelegate().createdExternally(toVCSFileProxy(fo));
        }

        @Override
        public IOHandler getMoveHandler(FileProxyI from, FileProxyI to) {
            final VCSFilesystemInterceptor.IOHandler moveHandler = getDelegate().getMoveHandler(toVCSFileProxy(from), toVCSFileProxy(to));
            return new IOHandler() {

                @Override
                public void handle() throws IOException {
                    moveHandler.handle();
                }
            };
        }

        @Override
        public IOHandler getRenameHandler(FileProxyI from, String newName) {
            final VCSFilesystemInterceptor.IOHandler renameHandler = getDelegate().getRenameHandler(toVCSFileProxy(from), newName);
            return new IOHandler() {

                @Override
                public void handle() throws IOException {
                    renameHandler.handle();
                }
            };
        }

        @Override
        public void afterMove(FileProxyI from, FileProxyI to) {
            getDelegate().afterMove(toVCSFileProxy(from), toVCSFileProxy(from));
        }

        @Override
        public FilesystemInterceptorProvider.IOHandler getCopyHandler(FileObject from, FileProxyI to) {
            final VCSFilesystemInterceptor.IOHandler copyHandler = getDelegate().getCopyHandler(toVCSFileProxy(from), toVCSFileProxy(to));
            return new FilesystemInterceptorProvider.IOHandler() {

                @Override
                public void handle() throws IOException {
                    copyHandler.handle();
                }
            };
        }

        @Override
        public void beforeCopy(FileObject from, FileProxyI to) {
            getDelegate().beforeCopy(toVCSFileProxy(from), toVCSFileProxy(to));
        }

        @Override
        public void copySuccess(FileObject from, FileProxyI to) {
            getDelegate().copySuccess(toVCSFileProxy(from), toVCSFileProxy(to));
        }

        @Override
        public void copyFailure(FileObject from, FileProxyI to) {
            // Missing method in VCSFilesystemInterceptor or should be processed in FS?
        }

        @Override
        public void fileLocked(FileProxyI fo) {
            getDelegate().fileLocked(toVCSFileProxy(fo));
        }

        @Override
        public long refreshRecursively(FileProxyI dir, long lastTimeStamp, List<? super FileProxyI> children) {
            List<VCSFileProxy> res = new ArrayList<VCSFileProxy>();
            for(Object f : children) {
                res.add(toVCSFileProxy((FileProxyI)f));
            }
            return getDelegate().refreshRecursively(toVCSFileProxy(dir), lastTimeStamp, res);
        }

        @Override
        public String toString() {
            return fs.getDisplayName();
        }
        
    }

    public static VCSFileProxy toVCSFileProxy(FileObject file) {
        return VCSFileProxy.createFileProxy(file);
    }

    public static VCSFileProxy toVCSFileProxy(FileProxyI proxy) {
        FileSystem fileSystem = proxy.getFileSystem();
        VCSFileProxy res;
        if (fileSystem instanceof RemoteFileSystem) {
            res = VCSFileProxy.createFileProxy(fileSystem.getRoot());
            String[] split = proxy.getPath().split("/"); // NOI18N
            for(int i = 0; i < split.length; i++) {
                res = VCSFileProxy.createFileProxy(res, split[i]);
            }
        } else {
            res = VCSFileProxy.createFileProxy(new File(proxy.getPath()));
        }
        return res;
    }
}
