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

package org.netbeans.modules.remote.spi;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.support.RemoteLogger;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 * A temporary solution until we have an official file system provider in thus module
 * @author Andrew Krasny
 * @author Vladimir Kvashin
 */
public final class FileSystemProvider {

    public interface FileSystemProblemListener {
        void problemOccurred(FileSystem fileSystem, String path);
        void recovered(FileSystem fileSystem);
    }
    // create own copy of lookup to avoid performance issues in ProxyLookup.LazyCollection.iterator()
    private static final  Collection<FileSystemProviderImplementation> ALL_PROVIDERS =
            new ArrayList<FileSystemProviderImplementation>(Lookup.getDefault().lookupAll(FileSystemProviderImplementation.class));

    private FileSystemProvider() {
    }

    public static FileSystem getFileSystem(ExecutionEnvironment env) {
        return getFileSystem(env, "/"); //NOI18N
    }

    public static ExecutionEnvironment getExecutionEnvironment(FileSystem fileSystem) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileSystem)) {
                return provider.getExecutionEnvironment(fileSystem);
            }
        }
        return ExecutionEnvironmentFactory.getLocal();
    }

    public static ExecutionEnvironment getExecutionEnvironment(FileObject fileObject) {
        try {
            return getExecutionEnvironment(fileObject.getFileSystem());
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);            
        }
        return ExecutionEnvironmentFactory.getLocal();
    }

    public static FileSystem getFileSystem(ExecutionEnvironment env, String root) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(env)) {
                return provider.getFileSystem(env, root);
            }
        }
        noProvidersWarning(env);
        return null;
    }

    public static boolean waitWrites(ExecutionEnvironment env, Collection<FileObject> filesToWait, Collection<String> failedFiles) throws InterruptedException {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(env)) {
                return provider.waitWrites(env, filesToWait, failedFiles);
            }
        }
        noProvidersWarning(env);
        return true;
    }
    
    public static boolean waitWrites(ExecutionEnvironment env, Collection<String> failedFiles) throws InterruptedException {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(env)) {
                return provider.waitWrites(env, failedFiles);
            }
        }
        noProvidersWarning(env);
        return true;
    }

    public static String normalizeAbsolutePath(String absPath, ExecutionEnvironment env) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(env)) {
                return provider.normalizeAbsolutePath(absPath, env);
            }
        }
        noProvidersWarning(env);
        return FileUtil.normalizePath(absPath); // or should it return just absPath?
    }

    public static String normalizeAbsolutePath(String absPath, FileSystem fileSystem) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileSystem)) {
                return provider.normalizeAbsolutePath(absPath, fileSystem);
            }
        }
        noProvidersWarning(fileSystem);
        return FileUtil.normalizePath(absPath); // or should it return just absPath?
    }

    /**
     * In many places, standard sequence is as follows:
     *  - convert path to absolute if need
     *  - normalize it
     *  - find file object
     * In the case of non-local file systems we should delegate it to correspondent file systems.
     */
    public static FileObject getFileObject(FileObject baseFileObject, String relativeOrAbsolutePath) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(baseFileObject)) {
                return provider.getFileObject(baseFileObject, relativeOrAbsolutePath);
            }
        }
        noProvidersWarning(baseFileObject);
        if (isAbsolute(relativeOrAbsolutePath)) {
            try {
                return baseFileObject.getFileSystem().findResource(relativeOrAbsolutePath);
            } catch (FileStateInvalidException ex) {
                return null;
            }
        } else {
            return baseFileObject.getFileObject(relativeOrAbsolutePath);
        }
    }
    
    /**
     * Just a convenient shortcut
     */
    public static FileObject getFileObject(ExecutionEnvironment env, String absPath) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(env)) {
                return provider.getFileSystem(env, "/").findResource(absPath);
            }
        }
        noProvidersWarning(env);
        return FileUtil.toFileObject(FileUtil.normalizeFile(new File(absPath)));
    }

    public static FileObject getCanonicalFileObject(FileObject fileObject) throws IOException {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileObject)) {
                return provider.getCanonicalFileObject(fileObject);
            }
        }
        noProvidersWarning(fileObject);
        return fileObject;
    }
    
    public static String getCanonicalPath(FileObject fileObject) throws IOException {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileObject)) {
                return provider.getCanonicalPath(fileObject);
            }
        }
        noProvidersWarning(fileObject);
        return fileObject.getPath();
    }

    public static String getCanonicalPath(FileSystem fileSystem, String absPath) throws IOException {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileSystem)) {
                return provider.getCanonicalPath(fileSystem, absPath);
            }
        }
        noProvidersWarning(fileSystem);
        return absPath;
    }

    public static String getCanonicalPath(ExecutionEnvironment env, String absPath) throws IOException {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(env)) {
                return provider.getCanonicalPath(env, absPath);
            }
        }
        noProvidersWarning(env);
        return absPath;
    }

    public static boolean isAbsolute(ExecutionEnvironment env,  String path) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(env)) {
                return provider.isAbsolute(path);
            }
        }
        return true; // for other file system, let us return true - or should it be false? 
    }
    
    public static boolean isAbsolute(String path) {
        if (path == null || path.length() == 0) {
            return false;
        } else if (path.charAt(0) == '/') {
            return true;
        } else if (path.charAt(0) == '\\') {
            return true;
        } else if (path.indexOf(':') == 1 && Utilities.isWindows()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * JFileChooser works in the term of files.
     * For such "perverted" files FileUtil.toFileObject won't work.
     * @param file
     * @return 
     */
    public static FileObject fileToFileObject(File file) {
        Parameters.notNull("file", file);
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(file)) {
                return provider.fileToFileObject(file);
            }
        }
        noProvidersWarning(file);
        return FileUtil.toFileObject(file);
    }

    public static FileSystem getFileSystem(URI uri) {
        Parameters.notNull("file", uri);
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(uri)) {
                return provider.getFileSystem(uri);
            }
        }
        noProvidersWarning(uri);
        return null;
    }

    public static FileObject urlToFileObject(String url) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(url)) {
                return provider.urlToFileObject(url);
            }
        }
        noProvidersWarning(url);
        return null;
    }

    public static String toUrl(FileSystem fileSystem, String absPath) {
        Parameters.notNull("fileSystem", fileSystem); //NOI18N
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileSystem)) {
                return provider.toURL(fileSystem, absPath);
            }
        }
        noProvidersWarning(fileSystem);
        return absPath;        
    }

    public static String fileObjectToUrl(FileObject fileObject) {
        Parameters.notNull("fileObject", fileObject); //NOI18N
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileObject)) {
                return provider.toURL(fileObject);
            }
        }
        noProvidersWarning(fileObject);
        try {
            return fileObject.getURL().toExternalForm();
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    public static void scheduleRefresh(FileObject fileObject) {
        Parameters.notNull("fileObject", fileObject); //NOI18N
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileObject)) {
                provider.scheduleRefresh(fileObject); 
                return;
            }
        }
        noProvidersWarning(fileObject);
    }
    
    public static void scheduleRefresh(ExecutionEnvironment env, Collection<String> paths) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(env)) {
                provider.scheduleRefresh(env, paths);
                return;
            }
        }
        noProvidersWarning(env);
    }
    
    public static void addRecursiveListener(FileChangeListener listener,  FileSystem fileSystem, String absPath) {
        addRecursiveListener(listener, fileSystem, absPath, null, null);
    }

    public static void addRecursiveListener(FileChangeListener listener,  FileSystem fileSystem, String absPath, FileFilter recurseInto, Callable<Boolean> interrupter) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileSystem)) {
                absPath = provider.normalizeAbsolutePath(absPath, fileSystem);
                provider.addRecursiveListener(listener, fileSystem, absPath, recurseInto, interrupter);
                return;
            }
        }
        noProvidersWarning(fileSystem);
    }
    
    public static void removeRecursiveListener(FileChangeListener listener, FileSystem fileSystem, String absPath) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileSystem)) {
                absPath = provider.normalizeAbsolutePath(absPath, fileSystem);
                provider.removeRecursiveListener(listener, fileSystem, absPath);
                return;
            }
        }
        noProvidersWarning(fileSystem);
    }

    public static boolean canExecute(FileObject fileObject) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileObject)) {
                return provider.canExecute(fileObject);
            }
        }
        noProvidersWarning(fileObject);
        return true;
    }
    
    public static void addFileChangeListener(FileChangeListener listener, FileSystem fileSystem, String path) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileSystem)) {
                provider.addFileChangeListener(listener, fileSystem, path);
                return;
            }
        }
        noProvidersWarning(fileSystem);
    }
    
    public static void addFileChangeListener(FileChangeListener listener, ExecutionEnvironment env, String path) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(env)) {
                provider.addFileChangeListener(listener, env, path);
                return;
            }
        }
        noProvidersWarning(env);
    }
    
    public static void addFileChangeListener(FileChangeListener listener) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            provider.addFileChangeListener(listener);
        }
    }
    
    public static void removeFileChangeListener(FileChangeListener listener) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            provider.removeFileChangeListener(listener);
        }
    }
    
    public static void addFileSystemProblemListener(FileSystemProblemListener listener, FileSystem fileSystem) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileSystem)) {
                provider.addFileSystemProblemListener(listener, fileSystem);
            }
        }
    }

    public static void removeFileSystemProblemListener(FileSystemProblemListener listener, FileSystem fileSystem) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileSystem)) {
                provider.removeFileSystemProblemListener(listener, fileSystem);
            }
        }
    }

    public static char getFileSeparatorChar(FileSystem fileSystem) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileSystem)) {
                return provider.getFileSeparatorChar();
            }
        }
        noProvidersWarning(fileSystem);
        return '/';
    }

    public static char getFileSeparatorChar(ExecutionEnvironment env) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(env)) {
                return provider.getFileSeparatorChar();
            }
        }
        noProvidersWarning(env);
        return '/';
    }
    
    private static void noProvidersWarning(Object object) {
        if (RemoteLogger.getInstance().isLoggable(Level.FINE)) {        
            if (RemoteLogger.getInstance().isLoggable(Level.FINEST)) {
                String message = "No file system providers for " + object; // NOI18N
                RemoteLogger.getInstance().log( Level.FINEST, message, new Exception(message)); //NOI18N
            } else {
                RemoteLogger.getInstance().log(Level.FINE, "No file system providers for {0}", object); //NOI18N
            }
        }
    }
}
