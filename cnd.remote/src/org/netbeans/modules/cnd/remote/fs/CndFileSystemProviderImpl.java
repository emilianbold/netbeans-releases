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
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.EnvUtils;
import org.netbeans.modules.remote.spi.FileSystemCacheProvider;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Vladimir Kvashin
 */
@ServiceProvider(service=CndFileSystemProvider.class)
public class CndFileSystemProviderImpl extends CndFileSystemProvider implements FileSystemProvider.DownloadListener {

   /** just to speed it up, since Utilities.isWindows will get string property, test equals, etc */
   private static final boolean isWindows = Utilities.isWindows();
   private String cachePrefix;

    public CndFileSystemProviderImpl() {
        FileSystemProvider.addDownloadListener(this);
    }

    @Override
    protected FileObject toFileObjectImpl(CharSequence absPath) {
        FileSystemAndString p = getFileSystemAndRemotePath(absPath);
        if (p == null) {
            return FileSystemProvider.urlToFileObject(absPath.toString());
        } else {
            return p.getFileObject();
        }
    }

    @Override
    protected CharSequence fileObjectToUrlImpl(FileObject fileObject) {
        return FileSystemProvider.fileObjectToUrl(fileObject);
    }

    @Override
    protected FileObject urlToFileObjectImpl(CharSequence url) {
        // That's legacy: an url can be a path to RFS cache file.
        FileSystemAndString p = getFileSystemAndRemotePath(url);
        if (p == null) {
            return FileSystemProvider.urlToFileObject(url.toString());
        } else {
            return p.getFileObject();
        }
    }

    @Override
    protected FileInfo[] getChildInfoImpl(CharSequence path) {
        FileSystemAndString p = getFileSystemAndRemotePath(path);
        if (p != null) {
            CndUtils.assertNotNull(p.fileSystem, "null file system"); //NOI18N
            CndUtils.assertNotNull(p.remotePath, "null remote path"); //NOI18N
            FileObject dirFO = p.getFileObject();
            if (dirFO == null) {
                return new FileInfo[0];
            }
            FileObject[] children = dirFO.getChildren();
            FileInfo[] result = new FileInfo[children.length];
            for (int i = 0; i < children.length; i++) {
                result[i] = new FileInfo(path.toString() + '/' + children[i].getNameExt(), children[i].isFolder());
            }
            return result;
        }
        return null;
    }

    @Override
    protected Boolean canReadImpl(CharSequence path) {
        FileSystemAndString p = getFileSystemAndRemotePath(path);
        if (p != null) {
            CndUtils.assertNotNull(p.fileSystem, "null file system"); //NOI18N
            CndUtils.assertNotNull(p.remotePath, "null remote path"); //NOI18N
            FileObject fo = p.getFileObject();
            return (fo != null && fo.isValid() && fo.canRead());
        }
        return null;
    }

    @Override
    protected Boolean existsImpl(CharSequence path) {
        FileSystemAndString p = getFileSystemAndRemotePath(path);
        if (p != null) {
            CndUtils.assertNotNull(p.fileSystem, "null file system"); //NOI18N
            CndUtils.assertNotNull(p.remotePath, "null remote path"); //NOI18N
            FileObject fo = p.getFileObject();
            return (fo != null && fo.isValid());
        }
        return null;
    }

    private FileSystemAndString getFileSystemAndRemotePath(CharSequence path) {
        String prefix = getPrefix();
        if (prefix != null) {
            if (isWindows) {
                path = path.toString().replace('\\', '/');
            }
            if (pathStartsWith(path, prefix)) {
                CharSequence rest = path.subSequence(prefix.length(), path.length());
                int slashPos = CharSequenceUtils.indexOf(rest, "/"); // NOI18N
                if (slashPos >= 0) {
                    String hostID = rest.subSequence(0, slashPos).toString();
                    CharSequence remotePath = rest.subSequence(slashPos + 1, rest.length());
                    ExecutionEnvironment env = getExecutionEnvironmentByHostID(hostID);
                    if (env != null) {
                        FileSystem fs = FileSystemProvider.getFileSystem(env);
                        return new FileSystemAndString(fs, remotePath);
                    }
                }
            }
        }
        return null;
    }

    private String getPrefix() {
        synchronized (this) {
            if (cachePrefix == null) {
                // XXX: FullRemote
                String prefix = new File(FileSystemCacheProvider.getCacheRoot(ExecutionEnvironmentFactory.getLocal())).getParent();
                prefix= prefix.replace("\\", "/"); //NOI18N
                if (!prefix.endsWith("/")) { //NOI18N
                    prefix += '/';
                }
                cachePrefix = prefix;
            }
        }
        return cachePrefix;
    }

    private boolean pathStartsWith(CharSequence path, CharSequence prefix) {
        if (CndFileUtils.isSystemCaseSensitive()) {
            return CharSequenceUtils.startsWith(path, prefix);
        } else {
            return CharSequenceUtils.startsWithIgnoreCase(path, prefix);
        }
    }

    private static ExecutionEnvironment getExecutionEnvironmentByHostID(String hostID) {
        ExecutionEnvironment result = null;
        for(ExecutionEnvironment env : ServerList.getEnvironments()) {
            if (hostID.equals(EnvUtils.toHostID(env))) {
                result = env;
                if (ConnectionManager.getInstance().isConnectedTo(env)) {
                    break;
                }
            }
        }
        return result;
    }


    @Override
    protected String getCaseInsensitivePathImpl(CharSequence path) {
//        String prefix = CndUtils.getIncludeFileBase();
        if (Utilities.isWindows()) {
            path = path.toString().replace('\\', '/');
        }
        return path.toString();
//        if (pathStartsWith(path, prefix)) {
//            CharSequence start = path.subSequence(0, prefix.length());
//            CharSequence rest = path.subSequence(prefix.length(), path.length());
//            return start + rest.toString(); // RemoteFileSupport.fixCaseSensitivePathIfNeeded(rest.toString());
//        }
//        return null;
    }

    @Override
    public void postConnectDownloadFinished(ExecutionEnvironment env) {
        RemoteCodeModelUtils.scheduleReparse(env);
    }

    private static class FileSystemAndString {

        public final FileSystem fileSystem;
        public final CharSequence remotePath;

        public FileSystemAndString(FileSystem fs, CharSequence path) {
            this.fileSystem = fs;
            this.remotePath = path;
        }

        public FileObject getFileObject() {
            return fileSystem.findResource(remotePath.toString());
        }
    }
}
