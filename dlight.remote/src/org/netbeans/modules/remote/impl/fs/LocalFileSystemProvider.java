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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.impl.fs;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProviderImplementation;
import org.netbeans.modules.remote.support.RemoteLogger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author ak119685
 */
@ServiceProvider(service = org.netbeans.modules.remote.spi.FileSystemProviderImplementation.class, position=100)
public final class LocalFileSystemProvider implements FileSystemProviderImplementation {

    private FileSystem rootFileSystem = null;
    private Map<String, LocalFileSystem> nonRootFileSystems = new HashMap<String, LocalFileSystem>();

    @Override
    public String normalizeAbsolutePathImpl(String absPath, ExecutionEnvironment env) {
        return FileUtil.normalizePath(absPath);
    }

    @Override
    public FileObject normalizeFileObjectImpl(FileObject fileObject) {
        String normalizedPath = FileUtil.normalizePath(fileObject.getPath());
        if (normalizedPath.equals(fileObject.getPath())) {
            return fileObject;
        } else {
            return FileUtil.toFileObject(new File(normalizedPath));
        }
    }

    @Override
    public FileObject getFileObjectImpl(FileObject baseFileObject, String relativeOrAbsolutePath) {
        return baseFileObject.getFileObject(relativeOrAbsolutePath);
    }

    private FileSystem getRootFileSystem() {
        if (rootFileSystem == null) {
            FileObject fo = FileUtil.toFileObject(File.listRoots()[0]);
            try {
                rootFileSystem = fo.getFileSystem();
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return rootFileSystem;
    }

    @Override
    public FileSystem getFileSystemImpl(ExecutionEnvironment env, String root) {
        if (env.isLocal()) {
            synchronized (this) {
                if ("/".equals(root)) {
                    return getRootFileSystem();
                } else {
                    LocalFileSystem fs = nonRootFileSystems.get(root);
                    if (fs == null) {
                        fs = new LocalFileSystem();
                        try {
                            fs.setRootDirectory(new File(root));
                            nonRootFileSystems.put(root, fs);
                        } catch (PropertyVetoException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    return fs;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isMine(ExecutionEnvironment env) {
        return env.isLocal();
    }

    @Override
    public boolean isMine(FileObject fileObject) {
        try {
            FileSystem fileSystem = fileObject.getFileSystem();
            if (fileSystem instanceof LocalFileSystem) {
                return true;
            } else {
                FileSystem rootFS = getRootFileSystem();
                if (rootFS != null && rootFS.getClass() == fileSystem.getClass()) {
                    return true;
                }
            }
        } catch (FileStateInvalidException ex) {
            RemoteLogger.getInstance().log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }
        return false;
    }

    @Override
    public boolean isMine(String absoluteURL) {
        return absoluteURL.startsWith("/"); //NOI18N
    }

    @Override
    public boolean waitWrites(ExecutionEnvironment env, List<String> failedFiles) throws InterruptedException {
        return true;
    }

    @Override
    public FileObject getFileObjectImpl(String absoluteURL) {
        File file = new File(absoluteURL);
        return FileUtil.toFileObject(file);
    }

    @Override
    public String toURL(FileObject fileObject) {
        return fileObject.getPath();
    }
}
