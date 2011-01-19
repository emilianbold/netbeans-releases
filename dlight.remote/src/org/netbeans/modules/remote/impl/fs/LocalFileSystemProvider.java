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
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.spi.FileSystemProviderImplementation;
import org.netbeans.modules.remote.support.RemoteLogger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author ak119685
 */
@ServiceProvider(service = org.netbeans.modules.remote.spi.FileSystemProviderImplementation.class, position=100)
public final class LocalFileSystemProvider implements FileSystemProviderImplementation {

    private FileSystem rootFileSystem = null;
    private Map<String, LocalFileSystem> nonRootFileSystems = new HashMap<String, LocalFileSystem>();
    private final boolean isWindows = Utilities.isWindows();

    @Override
    public String normalizeAbsolutePath(String absPath, ExecutionEnvironment env) {
        return FileUtil.normalizePath(absPath);
    }

    @Override
    public FileObject getFileObject(FileObject baseFileObject, String relativeOrAbsolutePath) {
        String absPath;
        if (FileSystemProvider.isAbsolute(relativeOrAbsolutePath)) {
            absPath = relativeOrAbsolutePath;
            
        } else {
            absPath = baseFileObject.getPath() + File.separatorChar + relativeOrAbsolutePath.toString();
        }
        return FileUtil.toFileObject(new File(FileUtil.normalizePath(absPath)));
    }

    private FileSystem getRootFileSystem() {
        if (rootFileSystem == null) {
            File tmpFile = null;
            try {
                tmpFile = File.createTempFile("NetBeans", ".tmp"); //NOI18N
                tmpFile = FileUtil.normalizeFile(tmpFile);
                FileObject fo = FileUtil.toFileObject(tmpFile);
                rootFileSystem = fo.getFileSystem();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (tmpFile != null) {
                    tmpFile.delete();
                }
            }
        }
        return rootFileSystem;
    }

    @Override
    public FileSystem getFileSystem(ExecutionEnvironment env, String root) {
        if (env.isLocal()) {
            synchronized (this) {
                if ("/".equals(root) || "".equals(root)) { // NOI18N
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
    public FileObject getCanonicalFileObject(FileObject fileObject) throws IOException {
        File file = FileUtil.toFile(fileObject);
        RemoteLogger.assertTrue(file != null, "null file"); //NOI18N
        if (file == null) {
            return fileObject;
        } else {
            File canonicalFile = file.getCanonicalFile();
            if (canonicalFile.equals(file)) {
                return fileObject;
            } else {
                FileObject canonicalFileObject = FileUtil.toFileObject(canonicalFile);
                RemoteLogger.assertTrue(canonicalFileObject != null, "null canonical file"); //NOI18N
                return (canonicalFileObject == null) ? fileObject : canonicalFileObject;
            }
        }
    }

    @Override
    public String getCanonicalPath(FileObject fileObject) throws IOException {
        return getCanonicalFileObject(fileObject).getPath();
    }

    public String getCanonicalPath(FileSystem fs, String absPath) throws IOException {
        return new File(absPath).getCanonicalPath();
    }
    
    @Override
    public boolean isMine(ExecutionEnvironment env) {
        return env.isLocal();
    }

    @Override
    public ExecutionEnvironment getExecutionEnvironment(FileSystem fileSystem) {
        return ExecutionEnvironmentFactory.getLocal();
    }

    @Override
    public boolean isMine(FileObject fileObject) {
        try {
            return isMine(fileObject.getFileSystem());
        } catch (FileStateInvalidException ex) {
            RemoteLogger.getInstance().log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }
        return false;
    }

    @Override
    public boolean isMine(FileSystem fileSystem) {
        if (fileSystem instanceof LocalFileSystem) {
            return true;
        } else {
            FileSystem rootFS = getRootFileSystem();
            if (rootFS != null && rootFS.getClass() == fileSystem.getClass()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isMine(String absoluteURL) {
        if (isWindows) {
            return (absoluteURL.length() > 1) && absoluteURL.charAt(1) == ':';
        } else {
            return absoluteURL.startsWith("/"); //NOI18N
        }
    }

    @Override
    public boolean waitWrites(ExecutionEnvironment env, List<String> failedFiles) throws InterruptedException {
        return true;
    }

    @Override
    public FileObject urlToFileObject(String absoluteURL) {
        File file = new File(absoluteURL);
        return FileUtil.toFileObject(file);
    }

    @Override
    public String toURL(FileObject fileObject) {
        return fileObject.getPath();
    }
    
    @Override
    public String toURL(FileSystem fileSystem, String absPath) {
        return absPath;
    }

    @Override
    public void addDownloadListener(FileSystemProvider.DownloadListener listener) {
    }

    @Override
    public void removeDownloadListener(FileSystemProvider.DownloadListener listener) {
    }
}
