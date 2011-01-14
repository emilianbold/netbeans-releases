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

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.spi.FileSystemProviderImplementation;
import org.netbeans.modules.remote.support.RemoteLogger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Vladimir Kvashin
 */
@ServiceProvider(service=org.netbeans.modules.remote.spi.FileSystemProviderImplementation.class, position=50)
public class RemoteFileSystemProvider implements FileSystemProviderImplementation {

    @Override
    public FileSystem getFileSystem(ExecutionEnvironment env, String root) {
        return RemoteFileSystemManager.getInstance().getFileSystem(env);
    }

    @Override
    public String normalizeAbsolutePath(String absPath, ExecutionEnvironment env) {
        return RemoteFileSystemManager.getInstance().getFileSystem(env).normalizeAbsolutePath(absPath);
    }

    @Override
    public FileObject getFileObject(FileObject baseFileObject, String relativeOrAbsolutePath) {
        if (baseFileObject instanceof RemoteFileObjectBase) {
            ExecutionEnvironment execEnv = ((RemoteFileObjectBase) baseFileObject).getExecutionEnvironment();
            if (isPathAbsolute(relativeOrAbsolutePath)) {
                relativeOrAbsolutePath = RemoteFileSystemManager.getInstance().getFileSystem(execEnv).normalizeAbsolutePath(relativeOrAbsolutePath);
                try {
                    return baseFileObject.getFileSystem().findResource(relativeOrAbsolutePath);
                } catch (FileStateInvalidException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                // it's RemoteDirectory responsibility to normalize in this case
                return baseFileObject.getFileObject(relativeOrAbsolutePath);
            }
        }
        return null;
    }

    /** Copy-pasted from CndPathUtilitities.isPathAbsolute */
    private static boolean isPathAbsolute(String path) {
        if (path == null || path.length() == 0) {
            return false;
        } else if (path.charAt(0) == '/') {
            return true;
        } else if (path.charAt(0) == '\\') {
            return true;
        } else if (path.indexOf(':') > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isMine(ExecutionEnvironment env) {
        return env.isRemote();
    }


    @Override
    public boolean isMine(FileObject fileObject) {
        return fileObject instanceof RemoteFileObjectBase;
    }

    @Override
    public boolean isMine(FileSystem fileSystem) {
        return fileSystem instanceof RemoteFileSystem;
    }

    @Override
    public FileObject getCanonicalFileObject(FileObject fileObject) throws IOException {
        while (fileObject instanceof RemoteLink) {
            FileObject delegate = ((RemoteLink) fileObject).getDelegate();
            if (delegate == null) {
                RemoteLogger.getInstance().log(Level.INFO, "Null delegate for remote link {0}", fileObject); //NOI18N
                break;
            } else {
                fileObject = delegate;
            }
        }
        return fileObject;
    }

    @Override
    public String getCanonicalPath(FileObject fileObject) throws IOException {
        return getCanonicalFileObject(fileObject).getPath();
    }

    public String getCanonicalPath(String absPath) throws IOException {
        //TODO:fullRemote implement (see issue #194361)
        return absPath;
    }
    
    @Override
    public ExecutionEnvironment getExecutionEnvironment(FileSystem fileSystem) {
        if (fileSystem instanceof RemoteFileSystem) {
            return ((RemoteFileSystem) fileSystem).getExecutionEnvironment();
        }
        return ExecutionEnvironmentFactory.getLocal();
    }

    @Override
    public boolean isMine(String absoluteURL) {
        return absoluteURL.startsWith(RemoteFileURLStreamHandler.PROTOCOL_PREFIX);
    }

    @Override
    public boolean waitWrites(ExecutionEnvironment env, List<String> failedFiles) throws InterruptedException {
        if (env.isRemote()) {
            return WritingQueue.getInstance(env).waitFinished(failedFiles);
        } else {
            return true;
        }
    }

    @Override
    public FileObject urlToFileObject(String path) {
        if (path.startsWith(RemoteFileURLStreamHandler.PROTOCOL_PREFIX)) {
            // path is like "rfs:,hostname:22/tmp/filename.ext"
            int port = 0;
            StringBuilder hostName = new StringBuilder();
            CharSequence remotePath = "";
            boolean insideHost = true;
            for (int i = RemoteFileURLStreamHandler.PROTOCOL_PREFIX.length(); i < path.length(); i++) {
                char c = path.charAt(i);
                if (insideHost) {
                    if (c == ':') {
                        insideHost = false;
                    } else {
                        hostName.append(c);
                    }
                } else {
                    if (Character.isDigit(c)) {
                        int digit = (int) c - (int) '0';
                        port = port * 10 + digit;
                    } else {
                        remotePath = path.subSequence(i, path.length());
                        break;
                    }
                }
            }
            if (hostName.length() == 0) {
                throw new IllegalArgumentException("Invalid path: " + path); //NOI18N
            }
            FileObject fo = null;
            RemoteFileSystem fs = null;
            ExecutionEnvironment env = RemoteFileSystemUtils.getExecutionEnvironment(hostName.toString(), 0);
            if (env != null) {
                fs = RemoteFileSystemManager.getInstance().getFileSystem(env);
                fo = fs.findResource(remotePath.toString());
            }
//            if (fo == null) {
//                fo = InvalidFileObjectSupport.getInvalidFileObject(fs, remotePath);
//            }
            return fo;
        }
        return null;
    }

    @Override
    public String toURL(FileObject fileObject) {
        if (fileObject instanceof RemoteFileObjectBase) {
            ExecutionEnvironment env =((RemoteFileObjectBase) fileObject).getExecutionEnvironment();
            return RemoteFileURLStreamHandler.PROTOCOL_PREFIX + env.getHost() + ':' + env.getSSHPort() + fileObject.getPath();
        }
        return null;
    }

    @Override
    public void addDownloadListener(FileSystemProvider.DownloadListener listener) {
        RemoteFileSystemManager.getInstance().addDownloadListener(listener);
    }

    @Override
    public void removeDownloadListener(FileSystemProvider.DownloadListener listener) {
        RemoteFileSystemManager.getInstance().removeDownloadListener(listener);
    }
}
