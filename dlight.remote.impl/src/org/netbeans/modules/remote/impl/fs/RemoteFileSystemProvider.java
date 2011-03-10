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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.api.ui.FileObjectBasedFile;
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
    public String normalizeAbsolutePath(String absPath, FileSystem fileSystem) {
        RemoteLogger.assertTrue(fileSystem instanceof RemoteFileSystem); // see isMine(FileSystem)
        if ((fileSystem instanceof RemoteFileSystem)) { // paranoidal check
            return ((RemoteFileSystem) fileSystem).normalizeAbsolutePath(absPath);
        }
        return absPath;
    }

    @Override
    public boolean isAbsolute(String path) {
        return path.startsWith("/"); //NOI18N
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
        return RemoteFileSystemUtils.getCanonicalFileObject(fileObject);
    }

    @Override
    public String getCanonicalPath(FileObject fileObject) throws IOException {
        return RemoteFileSystemUtils.getCanonicalFileObject(fileObject).getPath();
    }

    public String getCanonicalPath(FileSystem fs, String absPath) throws IOException {
        FileObject fo = fs.findResource(absPath);
        return (fo == null) ? null : getCanonicalFileObject(fo).getPath();
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
    public boolean waitWrites(ExecutionEnvironment env, Collection<String> failedFiles) throws InterruptedException {
        if (env.isRemote()) {
            return WritingQueue.getInstance(env).waitFinished(failedFiles);
        } else {
            return true;
        }
    }
    
    @Override
    public boolean waitWrites(ExecutionEnvironment env, Collection<FileObject> filesToWait, Collection<String> failedFiles) throws InterruptedException {
        if (env.isRemote()) {
            return WritingQueue.getInstance(env).waitFinished(filesToWait, failedFiles);
        } else {
            return true;
        }
    }    

    @Override
    public FileObject urlToFileObject(String path) {
        if (path.startsWith(RemoteFileURLStreamHandler.PROTOCOL_PREFIX)) {
            // path is like "rfs:hostname:22/tmp/filename.ext"
            // or           "rfs:username@hostname:22/tmp/filename.ext"
            int port = 0;
            StringBuilder hostName = new StringBuilder();
            String userName = null;
            CharSequence remotePath = "";
            boolean insideHostOrUser = true;
            for (int i = RemoteFileURLStreamHandler.PROTOCOL_PREFIX.length(); i < path.length(); i++) {
                char c = path.charAt(i);
                if (insideHostOrUser) {
                    if (c == '@') {
                        userName = hostName.toString(); // it was user, not host
                        hostName = new StringBuilder();
                    } else if (c == ':') {
                        insideHostOrUser = false;
                    } else {
                        hostName.append(c);
                    }
                } else {
                    if (Character.isDigit(c)) {
                        int digit = (int) c - (int) '0';
                        port = port * 10 + digit;
                    } else {
                        remotePath = path.subSequence(i + 1, path.length());
                        break;
                    }
                }
            }
            if (hostName.length() == 0) {
                throw new IllegalArgumentException("Invalid path: " + path); //NOI18N
            }
            if (port == 0) {
                port = 22;
            }
            FileObject fo = null;
            RemoteFileSystem fs = null;
            ExecutionEnvironment env;
            if (userName == null) {
                RemoteLogger.assertTrueInConsole(false, "Trying to access remote file system without user name");
                env = RemoteFileSystemUtils.getExecutionEnvironment(hostName.toString(), 0);
                if (env == null) {
                    env = ExecutionEnvironmentFactory.createNew(System.getProperty("user.name"), hostName.toString());
                }
            } else {
                env = ExecutionEnvironmentFactory.createNew(userName, hostName.toString(), port);
            }
            fs = RemoteFileSystemManager.getInstance().getFileSystem(env);
            fo = fs.findResource(remotePath.toString());
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
            return getUrlPrefix(env) + fileObject.getPath();
        }
        return null;
    }
    
    private String getUrlPrefix(ExecutionEnvironment env) {
        return RemoteFileURLStreamHandler.PROTOCOL_PREFIX + env.getUser() + '@' + env.getHost() + ':' + env.getSSHPort();
    }

    @Override
    public String toURL(FileSystem fileSystem, String absPath) {
        RemoteLogger.assertTrue(RemoteFileSystemUtils.isPathAbsolute(absPath), "Path must be absolute: " + absPath); //NOPI18N        
        if (fileSystem instanceof RemoteFileSystem) {
            ExecutionEnvironment env =((RemoteFileSystem) fileSystem).getExecutionEnvironment();
            return getUrlPrefix(env) + absPath;
        } else {
            throw new IllegalArgumentException("File system should be an istance of " + RemoteFileSystem.class.getName()); //NOI18N
        }
    }

    public FileObject fileToFileObject(File file) {
        if (file instanceof FileObjectBasedFile) {
            return ((FileObjectBasedFile) file).getFileObject();
        }
        return null;
    }

    public boolean isMine(File file) {
        return file instanceof FileObjectBasedFile;
    }
    
    @Override
    public void addDownloadListener(FileSystemProvider.DownloadListener listener) {
        RemoteFileSystemManager.getInstance().addDownloadListener(listener);
    }

    @Override
    public void removeDownloadListener(FileSystemProvider.DownloadListener listener) {
        RemoteFileSystemManager.getInstance().removeDownloadListener(listener);
    }

    @Override
    public void scheduleRefresh(FileObject fileObject) {
        if (fileObject instanceof RemoteFileObjectBase) {
            RemoteFileObjectBase fo = (RemoteFileObjectBase) fileObject;
            fo.getFileSystem().getRefreshManager().scheduleRefresh(Arrays.asList(fo));
        } else {
            RemoteLogger.getInstance().log(Level.WARNING, "Unexpected fileObject class: {0}", fileObject.getClass());
        }
    }

    @Override
    public void scheduleRefresh(ExecutionEnvironment env, Collection<String> paths) {
        RemoteFileSystem fs = RemoteFileSystemManager.getInstance().getFileSystem(env);
        fs.scheduleRefreshExistent(paths);
    }    
}
