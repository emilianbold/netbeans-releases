/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remote.impl.fileoperations.spi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.nio.channels.InterruptedByTimeoutException;
import java.util.concurrent.ExecutionException;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider;
import org.netbeans.modules.remote.impl.fs.RemoteFileObject;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystem;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystemTransport;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystemUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileSystem;

/**
 * Static methods that are need for RemoteVcsSupportImpl
 * @author vkvashin
 */
public class RemoteVcsSupportUtil {

    private RemoteVcsSupportUtil() {        
    }
    
    private static final boolean USE_CACHE;
    static {
        String text = System.getProperty("rfs.vcs.cache");
        USE_CACHE = (text == null) ? true : Boolean.parseBoolean(text);
    }
    
    public static boolean isSymbolicLink(FileSystem fileSystem, String path) {
        if (fileSystem instanceof RemoteFileSystem) {
            RemoteFileSystem rfs = (RemoteFileSystem) fileSystem;
            if (USE_CACHE) {
                Boolean res = rfs.vcsSafeIsSymbolicLink(path);
                if (res != null) {
                    return res.booleanValue();
                }
            }            
            final ExecutionEnvironment env = rfs.getExecutionEnvironment();
            if (!ConnectionManager.getInstance().isConnectedTo(env)) {
                return false;
            }
            try {
                FileInfoProvider.StatInfo statInfo = RemoteFileSystemTransport.lstat(env, path);
                return statInfo.isLink();
            } catch (InterruptedException ex) {
            } catch (ExecutionException ex) {
                if (RemoteFileSystemUtils.isFileNotFoundException(ex)) {
                    return false;
                }
                ex.printStackTrace(System.err);
            }
            return false;
            
        } else {
            return false;
        }
    }
    
//    /** returns fully resolved canonical path or NULL if this is not a symbolic link */
//    private static String resolveLink(ExecutionEnvironment env, String path) {
//        try {
//            FileInfoProvider.StatInfo statInfo = RemoteFileSystemTransport.stat(env, path);
//            if (statInfo.isLink()) {
//                String target = statInfo.getLinkTarget();
//                if (!target.startsWith("/")) { //NOI18N
//                    target = PathUtilities.normalizeUnixPath(path + "/" + target);
//                }
//                String nextTarget = resolveLink(env, target);
//                return (nextTarget == null) ? target : nextTarget;
//            } else {
//                return null;
//            }
//        } catch (InterruptedException ex) {
//        } catch (ExecutionException ex) {
//            if (RemoteFileSystemUtils.isFileNotFoundException(ex)) {
//                return path; // TODO: think over whether this is correct
//            }
//            ex.printStackTrace(System.err);
//        }        
//    }

    /** returns fully resolved canonical path or NULL if this is not a symbolic link */
    public static String getCanonicalPath(FileSystem fileSystem, String path) throws IOException {
        if (fileSystem instanceof RemoteFileSystem) {
            return getCanonicalPathImpl((RemoteFileSystem) fileSystem, path);
        } else {
            return null;
        }
    }
    
    /** returns fully resolved canonical path or NULL if this is not a symbolic link */
    private static String getCanonicalPathImpl(RemoteFileSystem fs, String path) throws IOException {
        Boolean isLink = fs.vcsSafeCanonicalPathDiffers(path);
        if (isLink != null && !isLink.booleanValue()) {
            return null;
        }
        ExecutionEnvironment env = fs.getExecutionEnvironment();
        if (!ConnectionManager.getInstance().isConnectedTo(env)) {
            throw new ConnectException(env.getDisplayName() + " not connected"); // NOI18N
        }
        try {
            FileInfoProvider.StatInfo statInfo = RemoteFileSystemTransport.lstat(env, path);
            if (statInfo.isLink()) {
                String target = statInfo.getLinkTarget();
                if (!target.startsWith("/")) { //NOI18N
                    target = PathUtilities.normalizeUnixPath(path + "/" + target); // NOI18N
                }
                String nextTarget = getCanonicalPathImpl(fs, target);
                return (nextTarget == null) ? target : nextTarget;
            } else {
                return null;
            }
        } catch (InterruptedException ex) {
            throw new InterruptedIOException();
        } catch (ExecutionException ex) {
            if (RemoteFileSystemUtils.isFileNotFoundException(ex)) {
                final FileNotFoundException fnfe = new FileNotFoundException();
                fnfe.initCause(ex);
                throw fnfe; // TODO: think over whether this is correct
            }
            throw new IOException(ex);
        }
    }
    
    public static boolean canReadImpl(RemoteFileSystem fileSystem, String path) {        
        try {
            ExecutionEnvironment env = fileSystem.getExecutionEnvironment();
            FileInfoProvider.StatInfo statInfo = RemoteFileSystemTransport.stat(env, path);
            return statInfo.canRead(env);
        } catch (InterruptedException ex) {
            return false; // TODO: is this correct?
        } catch (ExecutionException ex) {
            return false; // TODO: is this correct?
        }    
    }
    
    public static boolean canRead(FileSystem fileSystem, String path) {
        if (fileSystem instanceof RemoteFileSystem) {
            return canReadImpl((RemoteFileSystem) fileSystem, path);
        } else {
            return false;
        }        
    }
    
    public static long getSizeImpl(RemoteFileSystem fileSystem, String path) {
        try {
            ExecutionEnvironment env = fileSystem.getExecutionEnvironment();
            FileInfoProvider.StatInfo statInfo = RemoteFileSystemTransport.stat(env, path);
            return statInfo.getSize();
        } catch (InterruptedException ex) {
            return 0; // TODO: is this correct?
        } catch (ExecutionException ex) {
            return 0; // TODO: is this correct?
        }
    }

    public static long getSize(FileSystem fileSystem, String path) {
        if (fileSystem instanceof RemoteFileSystem) {
            return getSizeImpl((RemoteFileSystem) fileSystem, path);
        } else {
            return 0; // TODO: should it be -1?
        }
    }
}
