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

package org.netbeans.modules.cnd.remote.fs;

import java.io.File;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.support.InvalidFileObjectSupport;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.EnvUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Vladimir Kvashin
 */
@ServiceProvider(service=CndFileSystemProvider.class)
public class RemoteFileSystemsProvider extends CndFileSystemProvider {

    private static final String PROTOCOL_PREFIX = "rfs:"; // NOI18N
    
   /** just to speed it up, since Utilities.isWindows will get string property, test equals, etc */
   private static final boolean isWindows = Utilities.isWindows();

    @Override
    protected boolean isMine(CharSequence path) {
        String prefix = CndUtils.getIncludeFileBase();
        if (isWindows) {
            path = path.toString().replace('\\', '/');
        }
        if (pathStartsWith(path, prefix)) {
            return true;
        }
        return false;
    }

    @Override
    protected FileObject toFileObjectImpl(File file) {
        return filePathToFileObject(file.getAbsolutePath());
    }

    @Override
    protected File toFileImpl(FileObject fileObject) {
        return (fileObject instanceof RemoteFileObjectBase) ? ((RemoteFileObjectBase) fileObject).cache : null ;
    }

    @Override
    protected FileObject toFileObjectImpl(CharSequence path) {
        if (CharSequenceUtils.startsWith(path, PROTOCOL_PREFIX)) {
            // path is like "rfs:,hostname:22/tmp/filename.ext"
            int port = 0;
            StringBuilder hostName = new StringBuilder();
            CharSequence remotePath = null;
            boolean insideHost = true;
            for (int i = PROTOCOL_PREFIX.length(); i < path.length(); i++) {
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
            if (remotePath == null || hostName.length() == 0) {
                throw new IllegalArgumentException("Invalid path: " + path); //NOI18N
            }
            FileObject fo = null;
            RemoteFileSystem fs = null;
            ExecutionEnvironment env = getExecutionEnvironment(hostName.toString(), 0);
            if (env != null) {
                fs = RemoteFileSystemManager.getInstance().get(env);
                fo = fs.findResource(remotePath.toString());
            }
            if (fo == null) {
                fo = InvalidFileObjectSupport.getInvalidFileObject(fs, remotePath);
            }
            return fo;
        } else {
            return filePathToFileObject(path);
        }
    }

    @Override
    protected CharSequence toPathImpl(FileObject fileObject) {
        if (fileObject instanceof RemoteFileObjectBase) {
            ExecutionEnvironment env =((RemoteFileObjectBase) fileObject).getExecutionEnvironment();
            return PROTOCOL_PREFIX + env.getHost() + ':' + env.getSSHPort() + fileObject.getPath();
        }
        return null;
    }

    @Override
    protected FileInfo[] getChildInfoImpl(CharSequence path) {
        FileSystemAndString p = getFileSystemAndRemotePath(path);
        if (p != null) {
            CndUtils.assertNotNull(p.fileSystem, "null file system"); //NOI18N
            CndUtils.assertNotNull(p.remotePath, "null remote path"); //NOI18N
            p.fileSystem.getChildInfo(p.remotePath.toString());
        }
        return null;
    }

    @Override
    protected Boolean existsImpl(CharSequence path) {
        FileSystemAndString p = getFileSystemAndRemotePath(path);
        if (p != null) {
            CndUtils.assertNotNull(p.fileSystem, "null file system"); //NOI18N
            CndUtils.assertNotNull(p.remotePath, "null remote path"); //NOI18N
            return p.fileSystem.exists(p.remotePath.toString());
        }
        return null;
    }

    private FileObject filePathToFileObject(CharSequence path) {
        FileSystemAndString p = getFileSystemAndRemotePath(path);
        if (p != null) {
            CndUtils.assertNotNull(p.fileSystem, "null file system"); //NOI18N
            CndUtils.assertNotNull(p.remotePath, "null remote path"); //NOI18N
            FileObject fo = p.fileSystem.findResource(p.remotePath.toString());
            if (fo == null) {
                fo = InvalidFileObjectSupport.getInvalidFileObject(p.fileSystem, p.remotePath);
            }
            return fo;
        }
        return null;
    }

    private FileSystemAndString getFileSystemAndRemotePath(CharSequence path) {
        String prefix = CndUtils.getIncludeFileBase();
        if (prefix != null) {
            if (isWindows) {
                path = path.toString().replace('\\', '/');
            }
            if (pathStartsWith(path, prefix)) {
                CharSequence rest = path.subSequence(prefix.length(), path.length());
                int slashPos = CharSequenceUtils.indexOf(rest, "/"); // NOI18N
                if (slashPos >= 0) {
                    String hostName = rest.subSequence(0, slashPos).toString();
                    CharSequence remotePath = rest.subSequence(slashPos + 1, rest.length());
                    ExecutionEnvironment env = getExecutionEnvironment(hostName, 0);
                    RemoteFileSystem fs = null;
                    FileObject fo = null;
                    if (env != null) {
                        fs = RemoteFileSystemManager.getInstance().get(env);
                        if (fs != null) {
                            return new FileSystemAndString(fs, remotePath);
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean pathStartsWith(CharSequence path, CharSequence prefix) {
        if (CndFileUtils.isSystemCaseSensitive()) {
            return CharSequenceUtils.startsWith(path, prefix);
        } else {
            return CharSequenceUtils.startsWithIgnoreCase(path, prefix);
        }
    }

    @Override
    protected String getCaseInsensitivePathImpl(CharSequence path) {
        String prefix = CndUtils.getIncludeFileBase();
        if (Utilities.isWindows()) {
            path = path.toString().replace('\\', '/');
        }
        if (pathStartsWith(path, prefix)) {
            CharSequence start = path.subSequence(0, prefix.length());
            CharSequence rest = path.subSequence(prefix.length(), path.length());
            return start +
                    (CndFileUtils.isSystemCaseSensitive() ? rest.toString() : RemoteFileSupport.fixCaseSensitivePathIfNeeded(rest.toString()));
        }
        return null;
    }

    private ExecutionEnvironment getExecutionEnvironment(String hostName, int port) {
        ExecutionEnvironment result = null;
        for(ExecutionEnvironment env : ServerList.getEnvironments()) {
            if (hostName.equals(EnvUtils.toHostID(env))) {
                if (port == 0 || port == env.getSSHPort()) {
                    result = env;
                    if (ConnectionManager.getInstance().isConnectedTo(env)) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    private static class FileSystemAndString {
        public final RemoteFileSystem fileSystem;
        public final CharSequence remotePath;
        public FileSystemAndString(RemoteFileSystem first, CharSequence second) {
            this.fileSystem = first;
            this.remotePath = second;
        }
    }
}
