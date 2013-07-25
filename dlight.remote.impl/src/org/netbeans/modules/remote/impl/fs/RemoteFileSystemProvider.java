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
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import org.netbeans.modules.dlight.libs.common.DLightLibsCommonLogger;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.api.ui.FileObjectBasedFile;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.spi.FileSystemProvider.FileSystemProblemListener;
import org.netbeans.modules.remote.spi.FileSystemProviderImplementation;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Vladimir Kvashin
 */
@ServiceProvider(service=org.netbeans.modules.remote.spi.FileSystemProviderImplementation.class, position=150)
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
        if (baseFileObject instanceof RemoteFileObject) {
            ExecutionEnvironment execEnv = ((RemoteFileObject) baseFileObject).getExecutionEnvironment();
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

    /** Copy-pasted from CndPathUtilities.isPathAbsolute */
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
        return fileObject instanceof RemoteFileObject;
    }

    @Override
    public boolean isMine(FileSystem fileSystem) {
        return fileSystem instanceof RemoteFileSystem;
    }

    @Override
    public boolean isMine(URI uri) {
        return uri.getScheme().equals(RemoteFileURLStreamHandler.PROTOCOL);
    }

    @Override
    public FileObject getCanonicalFileObject(FileObject fileObject) throws IOException {
        return RemoteFileSystemUtils.getCanonicalFileObject(fileObject);
    }

    @Override
    public String getCanonicalPath(FileObject fileObject) throws IOException {
        return RemoteFileSystemUtils.getCanonicalFileObject(fileObject).getPath();
    }

    @Override
    public String getCanonicalPath(FileSystem fs, String absPath) throws IOException {
        FileObject fo = fs.findResource(absPath);
        if (fo != null) {
            try {
                return getCanonicalFileObject(fo).getPath();
            } catch (FileNotFoundException e) {
                RemoteLogger.finest(e);
            }
        }
        return PathUtilities.normalizeUnixPath(absPath);
    }

    @Override
    public String getCanonicalPath(ExecutionEnvironment env, String absPath) throws IOException {
        RemoteLogger.assertTrueInConsole(env.isRemote(), getClass().getSimpleName() + ".getCanonicalPath is called for LOCAL env: " + env); //NOI18N
        FileSystem fs = RemoteFileSystemManager.getInstance().getFileSystem(env);
        return getCanonicalPath(fs, absPath);
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
        return true;
    }

    @Override
    public boolean waitWrites(ExecutionEnvironment env, Collection<FileObject> filesToWait, Collection<String> failedFiles) throws InterruptedException {
        return true;
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        assert isMine(uri);
        return getFileSystem(getEnv(uri), ""); //NOI18N
    }

    private ExecutionEnvironment getEnv(URI uri) {
        String host = uri.getHost();
        int port = uri.getPort();
        String user = uri.getUserInfo();
        return ExecutionEnvironmentFactory.createNew(user, host, port);
    }

    @Override
    /**
     * Returns a FileOblect referenced by the provided URI.
     * Strictly speaking this is not an URL, but rather a string returned by
     * one of the toURL() methods.
     */
    public FileObject urlToFileObject(String path) {
        DLightLibsCommonLogger.assertNonUiThreadOnce(Level.INFO);

        if (!path.startsWith(RemoteFileURLStreamHandler.PROTOCOL_PREFIX)) {
            return null;
        }

        String url = path.substring(RemoteFileURLStreamHandler.PROTOCOL_PREFIX.length());
        if (url.startsWith("//")) { // NOI18N
            url = url.substring(2);
        }

        int idx = url.indexOf(":/"); // NOI18N

        String envPart;
        String pathPart;
        if (idx < 0) {
            envPart = url;
            pathPart = "/"; // NOI18N
        } else {
            envPart = url.substring(0, idx);
            pathPart = url.substring(idx + 1);
        }

        ExecutionEnvironment env = null;
        if (envPart.indexOf('@') < 0) {
            // The magic below is about getting connected environment even
            // when no user is specified ...
            RemoteLogger.assertTrueInConsole(false, "Trying to access remote file system without user name"); // NOI18N
            idx = envPart.lastIndexOf(':');
            String host = (idx < 0) ? envPart : envPart.substring(0, idx);
            env = RemoteFileSystemUtils.getExecutionEnvironment(host, 0);
        }
        if (env == null) {
            env = ExecutionEnvironmentFactory.fromUniqueID(envPart);
        }
        if (env == null) {
            throw new IllegalArgumentException("Invalid path: " + path); //NOI18N
        }

        RemoteFileSystem fs = RemoteFileSystemManager.getInstance().getFileSystem(env);
        return fs.findResource(pathPart);
    }

    @Override
    /**
     * Returns an URL-like string for the passed fileObject.
     * Later it could be passed to the urlToFileObject() method to get a 
     * FileObject. 
     */
    public String toURL(FileObject fileObject) {
        if (!(fileObject instanceof RemoteFileObject)) {
            return null;
        }

        ExecutionEnvironment env = ((RemoteFileObject) fileObject).getExecutionEnvironment();
        String path = fileObject.getPath();
        if (path == null || path.isEmpty()) {
            path = "/"; // NOI18N
        }
        return RemoteFileURLStreamHandler.PROTOCOL_PREFIX + ExecutionEnvironmentFactory.toUniqueID(env) + ':' + path;
    }

    @Override
    /**
     * Returns an URL-like string for the passed fileSystem and absPath.
     * Later it could be passed to the urlToFileObject() method to get a 
     * FileObject. 
     */
    public String toURL(FileSystem fileSystem, String absPath) {
        RemoteLogger.assertTrue(RemoteFileSystemUtils.isPathAbsolute(absPath), "Path must be absolute: " + absPath); //NOPI18N        
        if (!(fileSystem instanceof RemoteFileSystem)) {
            throw new IllegalArgumentException("File system should be an istance of " + RemoteFileSystem.class.getName()); //NOI18N
        }

        ExecutionEnvironment env = ((RemoteFileSystem) fileSystem).getExecutionEnvironment();
        return RemoteFileURLStreamHandler.PROTOCOL_PREFIX + ExecutionEnvironmentFactory.toUniqueID(env) + ':' + absPath;
    }

    @Override
    public FileObject fileToFileObject(File file) {
        if (!(file instanceof FileObjectBasedFile)) {
            return null;
        }
        return ((FileObjectBasedFile) file).getFileObject();
    }

    @Override
    public boolean isMine(File file) {
        return file instanceof FileObjectBasedFile;
    }

    @Override
    public void scheduleRefresh(FileObject fileObject) {
        if (fileObject instanceof RemoteFileObject) {
            RemoteFileObject fo = (RemoteFileObject) fileObject;
            fo.getFileSystem().getRefreshManager().scheduleRefresh(Arrays.asList(fo.getImplementor()), true);
        } else {
            RemoteLogger.getInstance().log(Level.WARNING, "Unexpected fileObject class: {0}", fileObject.getClass());
        }
    }

    @Override
    public void scheduleRefresh(ExecutionEnvironment env, Collection<String> paths) {
        RemoteFileSystem fs = RemoteFileSystemManager.getInstance().getFileSystem(env);
        fs.getRefreshManager().scheduleRefreshExistent(paths);
    }

    @Override
    public void addRecursiveListener(FileChangeListener listener, FileSystem fileSystem, String absPath) {
        addRecursiveListener(listener, fileSystem, absPath, null, null);
    }

    @Override
    public void addRecursiveListener(FileChangeListener listener, FileSystem fileSystem, String absPath,  FileFilter recurseInto, Callable<Boolean> interrupter) {
        //TODO: use interrupter & filter
        RemoteLogger.assertTrue(fileSystem instanceof RemoteFileSystem, "Unexpected file system class: " + fileSystem); // NOI18N
        FileObject fileObject = fileSystem.findResource(absPath);
        if (fileObject != null) {
            fileObject.addRecursiveListener(listener);
        }
    }

    @Override
    public void removeRecursiveListener(FileChangeListener listener, FileSystem fileSystem, String absPath) {
        RemoteLogger.assertTrue(fileSystem instanceof RemoteFileSystem, "Unexpected file system class: " + fileSystem); // NOI18N
        FileObject fileObject = fileSystem.findResource(absPath);
        if (fileObject != null) {
            fileObject.removeRecursiveListener(listener);
        }
    }

    @Override
    public void addFileChangeListener(FileChangeListener listener, FileSystem fileSystem, String path) {
        RemoteLogger.assertTrue(fileSystem instanceof RemoteFileSystem, "Unexpected file system class: " + fileSystem); // NOI18N
        ((RemoteFileSystem) fileSystem).getFactory().addFileChangeListener(path, listener);

    }

    @Override
    public void addFileChangeListener(FileChangeListener listener, ExecutionEnvironment env, String path) {
        RemoteLogger.assertTrue(env.isRemote(), "Unexpected ExecutionEnvironment: should be remote"); // NOI18N
        RemoteFileSystemManager.getInstance().getFileSystem(env).getFactory().addFileChangeListener(path, listener);
    }

    @Override
    public void addFileChangeListener(FileChangeListener listener) {
        RemoteFileSystemManager.getInstance().addFileChangeListener(listener);
    }

    @Override
    public void removeFileChangeListener(FileChangeListener listener) {
        RemoteFileSystemManager.getInstance().removeFileChangeListener(listener);
    }

    @Override
    public boolean canExecute(FileObject fileObject) {
        RemoteLogger.assertTrue(fileObject instanceof RemoteFileObject, "Unexpected file object class: " + fileObject); // NOI18N
        if (fileObject instanceof RemoteFileObject) {
            return ((RemoteFileObject) fileObject).getImplementor().canExecute();
        }
        return false;
    }

    @Override
    public char getFileSeparatorChar() {
        return '/';
    }

    @Override
    public void addFileSystemProblemListener(FileSystemProblemListener listener, FileSystem fileSystem) {
        ((RemoteFileSystem) fileSystem).addFileSystemProblemListener(listener);
    }

    @Override
    public void removeFileSystemProblemListener(FileSystemProblemListener listener, FileSystem fileSystem) {
        ((RemoteFileSystem) fileSystem).removeFileSystemProblemListener(listener);
    }
}
