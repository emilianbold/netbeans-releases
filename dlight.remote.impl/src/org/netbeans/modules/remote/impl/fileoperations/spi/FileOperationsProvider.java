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
package org.netbeans.modules.remote.impl.fileoperations.spi;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.api.extexecution.ProcessBuilder;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.SftpIOException;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.remote.impl.fs.RemoteFileObject;
import org.netbeans.modules.remote.impl.fs.RemoteFileObjectBase;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystem;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystemManager;
import org.netbeans.modules.remote.impl.fs.RemoteFileUrlMapper;
import org.netbeans.spi.extexecution.ProcessBuilderFactory;
import org.netbeans.spi.extexecution.ProcessBuilderImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander Simon
 */
abstract public class FileOperationsProvider {
    public static final String ATTRIBUTE = "FileProxyOperations"; // NOI18N
    private static final int LINK_DEPTH = 5;
        
    private static FileOperationsProvider defaultProvider;

    protected FileOperationsProvider() {
    }

    abstract public FileOperations getFileOperations(FileSystem fs);

    abstract public static class FileOperations {

        private final ExecutionEnvironment env;
        private final RemoteFileSystem fileSystem;
        private final RequestProcessor RP;
        
        private static final boolean USE_CACHE;
        static {
            String text = System.getProperty("rfs.vcs.cache");
            USE_CACHE = (text == null) ? true : Boolean.parseBoolean(text);
        }

        protected FileOperations(FileSystem fs) {
            FileObject root = fs.getRoot();
            if (root instanceof RemoteFileObject) {
                env = ((RemoteFileObject)root).getExecutionEnvironment();
                fileSystem = (RemoteFileSystem) fs;
                RP = new RequestProcessor("Refresh for "+env); //NOI18N
            } else {
                throw new IllegalArgumentException();
            }
        }

        protected String getName(FileProxyO file) {
            return PathUtilities.getBaseName(file.getPath());
        }

        protected String getDir(FileProxyO file) {
            return PathUtilities.getDirName(file.getPath());
        }
        
        protected String normalizeUnixPath(FileProxyO file) {
            String path = PathUtilities.normalizeUnixPath(file.getPath());
            // TODO resolve inconsistency of PathUtilities && FileUtils.
            if (path.isEmpty() && file.getPath().startsWith("/") || //NOI18N
                path.equals("/..")) { //NOI18N
                return "/"; //NOI18N
            }
            return path;
        }

        protected boolean isDirectory(FileProxyO file) {
            if (USE_CACHE) {
                Boolean res = fileSystem.vcsSafeIsDirectory(file.getPath());
                if (res != null) {
                    return res.booleanValue();
                }
            }
            return isDirectory(file, LINK_DEPTH);
        }
        
        private boolean isDirectory(FileProxyO file, int deep) {
            if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
                return false;
            }
            if (deep > 0) {
                deep--;
                Future<FileInfoProvider.StatInfo> stat = FileInfoProvider.stat(getExecutionEnvironment(), file.getPath());
                try {
                    FileInfoProvider.StatInfo statInfo = stat.get();
                    switch (statInfo.getFileType()) {
                        case Directory:
                            return true;
                        case SymbolicLink:
                            String linkTarget = statInfo.getLinkTarget();
                            if (linkTarget.startsWith("/")) { // NOI18N
                                return isDirectory(toFileProxy(linkTarget), deep);
                            } else {
                                String path = PathUtilities.getDirName(file.getPath())+"/"+linkTarget; // NOI18N
                                path = PathUtilities.normalizeUnixPath(path);
                                return isDirectory(toFileProxy(path), deep);
                            }
                        default:
                            return false;
                    }
                } catch (InterruptedException ex) {
                } catch (ExecutionException ex) {
                    if (notExist(ex)) {
                        return false;
                    }
                    ex.printStackTrace(System.err);
                }
            }
            return false;
        }

        protected long lastModified(FileProxyO file) {
            if (USE_CACHE) {
                Long res = fileSystem.vcsSafeLastModified(file.getPath());
                if (res != null) {
                    return res.longValue();
                }
            }
            return lastModified(file, LINK_DEPTH);
        }

        private long lastModified(FileProxyO file, int deep) {
            if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
                return -1;
            }
            if (deep > 0) {
                deep--;
                Future<FileInfoProvider.StatInfo> stat = FileInfoProvider.stat(getExecutionEnvironment(), file.getPath());
                try {
                    FileInfoProvider.StatInfo statInfo = stat.get();
                    switch (statInfo.getFileType()) {
                        case SymbolicLink:
                            String linkTarget = statInfo.getLinkTarget();
                            if (linkTarget.startsWith("/")) { // NOI18N
                                return lastModified(toFileProxy(linkTarget), deep);
                            } else {
                                String path = PathUtilities.getDirName(file.getPath())+"/"+linkTarget; // NOI18N
                                path = PathUtilities.normalizeUnixPath(path);
                                return lastModified(toFileProxy(path), deep);
                            }
                        default:
                            return statInfo.getLastModified().getTime();
                    }
                } catch (InterruptedException ex) {
                } catch (ExecutionException ex) {
                    if (notExist(ex)) {
                        return -1;
                    }
                    ex.printStackTrace(System.err);
                }
            }
            return -1;
        }

        protected boolean isFile(FileProxyO file) {
            if (USE_CACHE) {
                Boolean res = fileSystem.vcsSafeIsFile(file.getPath());
                if (res != null) {
                    return res.booleanValue();
                }
            }
            return isFile(file, LINK_DEPTH);
        }
        
        private boolean isFile(FileProxyO file, int deep) {
            if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
                return false;
            }
            if (deep > 0) {
                deep--;
                Future<FileInfoProvider.StatInfo> stat = FileInfoProvider.stat(getExecutionEnvironment(), file.getPath());
                try {
                    FileInfoProvider.StatInfo statInfo = stat.get();
                    switch (statInfo.getFileType()) {
                        case Regular:
                            return true;
                        case SymbolicLink:
                            String linkTarget = statInfo.getLinkTarget();
                            if (linkTarget.startsWith("/")) { // NOI18N
                                return isFile(toFileProxy(linkTarget), deep);
                            } else {
                                String path = PathUtilities.getDirName(file.getPath())+"/"+linkTarget; // NOI18N
                                path = PathUtilities.normalizeUnixPath(path);
                                return isFile(toFileProxy(path), deep);
                            }
                        default:
                            return false;
                    }
                } catch (InterruptedException ex) {
                } catch (ExecutionException ex) {
                    if (notExist(ex)) {
                        return false;
                    }
                    ex.printStackTrace(System.err);
                }
            }
            return false;
        }

        private boolean notExist(ExecutionException e) {
            Throwable ex = e;
            while (ex != null) {
                if (ex instanceof SftpIOException) {
                    switch(((SftpIOException)ex).getId()) {
                        case SftpIOException.SSH_FX_NO_SUCH_FILE:
                        case SftpIOException.SSH_FX_PERMISSION_DENIED:
                        return true;
                    }
                    break;
                }
                ex = ex.getCause();
            }
            return false;
        }
        
        protected boolean canWrite(FileProxyO file) {
            return canWrite(file, LINK_DEPTH);
        }
        
        private boolean canWrite(FileProxyO file, int deep) {
            if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
                return false;
            }
            if (deep > 0) {
                deep--;
                Future<FileInfoProvider.StatInfo> stat = FileInfoProvider.stat(getExecutionEnvironment(), file.getPath());
                try {
                    FileInfoProvider.StatInfo statInfo = stat.get();
                    switch (statInfo.getFileType()) {
                        case SymbolicLink:
                            String linkTarget = statInfo.getLinkTarget();
                            if (linkTarget.startsWith("/")) { // NOI18N
                                return canWrite(toFileProxy(linkTarget), deep);
                            } else {
                                String path = PathUtilities.getDirName(file.getPath())+"/"+linkTarget; // NOI18N
                                path = PathUtilities.normalizeUnixPath(path);
                                return canWrite(toFileProxy(path), deep);
                            }
                        default:
                            return statInfo.canWrite(env);
                    }
                } catch (InterruptedException ex) {
                } catch (ExecutionException ex) {
                    if (notExist(ex)) {
                        return false;
                    }
                    ex.printStackTrace(System.err);
                }
            }
            return false;
        }

        protected FileObject getRoot() {
            RemoteFileSystem fs = RemoteFileSystemManager.getInstance().getFileSystem(env);
            return fs.getRoot();
        }
        
        protected String getPath(FileProxyO file) {
            return file.getPath();
        }

        protected URI toURI(String path, boolean folder) throws URISyntaxException {
            return RemoteFileUrlMapper.toURI(env, path, folder);
        }

        protected boolean exists(FileProxyO file) {
            if (USE_CACHE) {
                Boolean res = fileSystem.vcsSafeExists(file.getPath());
                if (res != null) {
                    return res.booleanValue();
                }
            }
            if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
                return false;
            }
            Future<FileInfoProvider.StatInfo> stat = FileInfoProvider.stat(getExecutionEnvironment(), file.getPath());
            try {
                FileInfoProvider.StatInfo statInfo = stat.get();
                return statInfo != null;
            } catch (InterruptedException ex) {
            } catch (ExecutionException ex) {
                if (notExist(ex)) {
                    return false;
                }
                System.err.println("Exception on file "+file.getPath());
                ex.printStackTrace(System.err);
            }
            return false;
        }

        protected FileObject toFileObject(FileProxyO path) {
            FileObject root = getRoot();
            return root.getFileObject(path.getPath());
        }

        protected String[] list(FileProxyO file) {
            if (isDirectory(file)) {
                Future<FileInfoProvider.StatInfo[]> stat = FileInfoProvider.ls(env, file.getPath());
                try {
                    FileInfoProvider.StatInfo[] statInfo = stat.get();
                    if (statInfo != null) {
                        String[] res = new String[statInfo.length];
                        for (int i = 0; i < statInfo.length; i++) {
                            res[i] = statInfo[i].getName();
                        }
                        return res;
                    }
                } catch (InterruptedException ex) {
                } catch (ExecutionException ex) {
                    if (notExist(ex)) {
                        return null;
                    }
                    ex.printStackTrace(System.err);
                }
            }
            return null;
        }

        protected ProcessBuilder createProcessBuilder(FileProxyO file) {
            return ProcessBuilderFactory.createProcessBuilder(new ProcessBuilderImplementationImpl(env), "RFS Process Builder"); // NOI18N
        }
        
        protected void refreshFor(FileProxyO ... files) {
            List<RemoteFileObjectBase> roots = new ArrayList<RemoteFileObjectBase>();
            for(FileProxyO f : files) {
                RemoteFileObjectBase fo = findExistingParent(f.getPath());
                if (fo != null) {
                    roots.add(fo);
                }
            }
            for(RemoteFileObjectBase fo : roots) {
                if (fo.isValid()) {
                    fo.refresh(true);
                }
            }
        }
        
        private RemoteFileObjectBase findExistingParent(String path) {
            while(true) {
                RemoteFileObject fo = RemoteFileSystemManager.getInstance().getFileSystem(env).findResource(path);
                if (fo != null) {
                    return fo.getImplementor();
                }
                path = PathUtilities.getDirName(path);
                if (path == null) {
                    return null;
                }
            }
        }

        private ExecutionEnvironment getExecutionEnvironment() {
            return env;
        }

        @Override
        public String toString() {
            return env.getDisplayName();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + (this.env != null ? this.env.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FileOperations other = (FileOperations) obj;
            if (this.env != other.env && (this.env == null || !this.env.equals(other.env))) {
                return false;
            }
            return true;
        }
        
    }

    public static FileProxyO toFileProxy(final String path) {
        return new FileProxyOImpl(path);
    }

    /**
     * Static method to obtain the provider.
     *
     * @return the provider
     */
    public static FileOperationsProvider getDefault() {
        /*
         * no need for sync synchronized access
         */
        if (defaultProvider != null) {
            return defaultProvider;
        }
        defaultProvider = Lookup.getDefault().lookup(FileOperationsProvider.class);
        return defaultProvider;
    }

    private static final class ProcessBuilderImplementationImpl implements ProcessBuilderImplementation {
        private final ExecutionEnvironment env;
        private ProcessBuilderImplementationImpl(ExecutionEnvironment env) {
            this.env = env;
        }

        @Override
        public Process createProcess(String executable, String workingDirectory, List<String> arguments, List<String> paths, Map<String, String> environment, boolean redirectErrorStream) throws IOException {
            NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(env);
            pb.setExecutable(executable).setWorkingDirectory(workingDirectory).setArguments(arguments.toArray(new String[arguments.size()]));
            MacroMap mm = MacroMap.forExecEnv(env);
            mm.putAll(environment);
            pb.getEnvironment().putAll(mm);
            for(String path : paths) {
                pb.getEnvironment().appendPathVariable("PATH", path); // NOI18N
            }
            if (redirectErrorStream) {
                pb.redirectError();
            }
            return pb.call();
        }

        @Override
        public String toString() {
            return env.getDisplayName();
        }
    }
    
    public interface FileProxyO {

        String getPath();
    }

    private static final class FileProxyOImpl implements FileProxyO {
        private final String path;
        private FileProxyOImpl(String path) {
            this.path = path;
        }
        @Override
        public String getPath() {
            return path;
        }

        @Override
        public String toString() {
            return path;
        }

        @Override
        public int hashCode() {
            return path.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FileProxyOImpl other = (FileProxyOImpl) obj;
            if ((this.path == null) ? (other.path != null) : !this.path.equals(other.path)) {
                return false;
            }
            return true;
        }
    }   
}
