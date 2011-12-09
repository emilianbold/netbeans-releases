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
package org.netbeans.modules.remote.impl.fileoperations;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.api.extexecution.ProcessBuilder;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider;
import org.netbeans.modules.remote.impl.fs.RemoteFileObjectBase;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystem;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystemManager;
import org.netbeans.spi.extexecution.ProcessBuilderFactory;
import org.netbeans.spi.extexecution.ProcessBuilderImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;

/**
 *
 * @author Alexander Simon
 */
abstract public class FileOperationsProvider {
    public static final String ATTRIBUTE = "FileProxyOperations"; // NOI18N
        
    private static FileOperationsProvider defaultProvider;

    protected FileOperationsProvider() {
    }

    abstract public FileOperations getFileOperations(FileSystem fs);

    abstract public static class FileOperations {

        private final ExecutionEnvironment env;

        protected FileOperations(FileSystem fs) {
            FileObject root = fs.getRoot();
            if (root instanceof RemoteFileObjectBase) {
                env = ((RemoteFileObjectBase)root).getExecutionEnvironment();
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
            if (path.isEmpty() && file.getPath().startsWith("/") ||
                path.equals("/..")) {
                return "/";
            }
            return path;
        }

        protected boolean isDirectory(FileProxyO file) {
            return isDirectory(file, 5);
        }
        
        private boolean isDirectory(FileProxyO file, int deep) {
            if (deep > 0) {
                deep--;
                Future<FileInfoProvider.StatInfo> stat = FileInfoProvider.stat(getExecutionEnvironment(), file.getPath());
                try {
                    FileInfoProvider.StatInfo statInfo = stat.get();
                    switch (statInfo.getFileType()) {
                        case Directory:
                            return true;
                        case SymbolicLink:
                            // TODO support links
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

        protected boolean isFile(FileProxyO file) {
            return isFile(file, 5);
        }
        
        protected boolean isFile(FileProxyO file, int deep) {
            if (deep > 0) {
                deep--;
                Future<FileInfoProvider.StatInfo> stat = FileInfoProvider.stat(getExecutionEnvironment(), file.getPath());
                try {
                    FileInfoProvider.StatInfo statInfo = stat.get();
                    switch (statInfo.getFileType()) {
                        case Regular:
                            return true;
                        case SymbolicLink:
                            // TODO support links
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

        private boolean notExist(ExecutionException ex) {
            // TODO refactor code.
            if (ex.getCause()  != null && ex.getCause().getCause() instanceof FileNotFoundException) {
                return true;
            }
            return false;
        }
        
        protected boolean canWrite(FileProxyO file) {
            Future<FileInfoProvider.StatInfo> stat = FileInfoProvider.stat(getExecutionEnvironment(), file.getPath());
            try {
                FileInfoProvider.StatInfo statInfo = stat.get();
                if (statInfo.isDirectory()) {
                    return statInfo.canWrite(env);
                } else {
                    String dirName = PathUtilities.getDirName(file.getPath());
                    if (dirName != null) {
                        return canWrite(toFileProxy(dirName));
                    }
                }
            } catch (InterruptedException ex) {
            } catch (ExecutionException ex) {
                if (notExist(ex)) {
                    return false;
                }
                ex.printStackTrace(System.err);
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

        protected boolean exists(FileProxyO file) {
            Future<FileInfoProvider.StatInfo> stat = FileInfoProvider.stat(getExecutionEnvironment(), file.getPath());
            try {
                FileInfoProvider.StatInfo statInfo = stat.get();
                return statInfo != null;
            } catch (InterruptedException ex) {
            } catch (ExecutionException ex) {
                if (notExist(ex)) {
                    return false;
                }
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

        private ExecutionEnvironment getExecutionEnvironment() {
            return env;
        }

        @Override
        public String toString() {
            return env.getDisplayName();
        }
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
            pb.getEnvironment().putAll(environment);
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

    public static FileProxyO toFileProxy(final String path) {
        return new FileProxyO() {

            @Override
            public String getPath() {
                return path;
            }

            @Override
            public String toString() {
                return path;
            }
            
        };
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
}
