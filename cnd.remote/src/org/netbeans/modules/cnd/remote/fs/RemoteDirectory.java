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
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.StringTokenizer;
import java.util.concurrent.CancellationException;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.support.InvalidFileObjectSupport;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Vladimir Kvashin
 */
public class RemoteDirectory extends RemoteFileObjectBase {

    private DirectoryAttributes attrs;

    public RemoteDirectory(RemoteFileSystem fileSystem, ExecutionEnvironment execEnv, 
            FileObject parent, String remotePath, File cache) {
        super(fileSystem, execEnv, parent, remotePath, cache);
    }

    @Override
    public boolean isFolder() {
        return true;
    }

    @Override
    public boolean isData() {
        return false;
    }

    @Override
    public FileObject getFileObject(String name, String ext) {
         return getFileObject(name + '.' + ext); // NOI18N
    }

    private static enum Mode {
        EXISTENCE,
        FILE_OBJECT,
        CHILDINFO
    }

    public CndFileSystemProvider.FileInfo[] getChildInfo(String relativePath) {
        return (CndFileSystemProvider.FileInfo[]) getFileOrCheckExistence(relativePath, Mode.CHILDINFO);
    }

    public boolean exists(String relativePath) {
        Boolean result = (Boolean) getFileOrCheckExistence(relativePath, Mode.EXISTENCE);
        return (result == null) ? false : result.booleanValue();
    }

    @Override
    public FileObject getFileObject(String relativePath) {
        FileObject fo = (FileObject) getFileOrCheckExistence(relativePath, Mode.FILE_OBJECT);
        if (fo == null) {
            return InvalidFileObjectSupport.getInvalidFileObject(fileSystem, relativePath);
        }
        return fo;
    }

    public boolean canWrite(String childNameExt) throws IOException {
        synchronized (this) {
            if (attrs == null) {
                attrs = new DirectoryAttributes(CndFileUtils.createLocalFile(cache, RemoteFileSupport.FLAG_FILE_NAME));
                attrs.load();
            }
        }
        return attrs.isWritable(childNameExt);
    }

    /**
     *
     * @param relativePath
     * @param createFileObject
     * @return either FileObject or Boolean
     */
    private Object getFileOrCheckExistence(String relativePath, Mode mode) {
        if (relativePath != null && relativePath.length()  > 0 && relativePath.charAt(0) == '/') { //NOI18N
            relativePath = relativePath.substring(1);
        }
        if (relativePath.endsWith("/")) { // NOI18N
            relativePath = relativePath.substring(0,relativePath.length()-1);
        }
        try {
            File file = CndFileUtils.createLocalFile(cache, relativePath);
            if (!file.exists()) {
                int slashPos = relativePath.lastIndexOf('/');
                if (slashPos == -1) {
                    String parentRemotePath = (remotePath.length() == 0) ? "/" : remotePath; // NOI18N
                    DirectoryAttributes newAttrs = getRemoteFileSupport().ensureDirSync(cache, parentRemotePath);
                    if (newAttrs != null) {
                        attrs = newAttrs;
                    }
                } else {
                    File parentFile = file.getParentFile();
                    String parentRemotePath = remotePath + '/' + relativePath.substring(0, slashPos);
                    getRemoteFileSupport().ensureDirSync(parentFile, parentRemotePath);
                }
                
                if (!file.exists()) {
                    return null;
                }
            }

            if (mode == Mode.EXISTENCE) {
                return Boolean.valueOf(file.exists());
            } else if (mode == Mode.CHILDINFO) {
                File cacheFile = CndFileUtils.createLocalFile(cache, relativePath);
                getRemoteFileSupport().ensureDirSync(cacheFile, remotePath + '/' + relativePath);
                File[] children = file.listFiles();
                if (children == null) {
                    return new CndFileSystemProvider.FileInfo[0];
                } else {
                    CndFileSystemProvider.FileInfo[] infos = new CndFileSystemProvider.FileInfo[children.length];
                    for (int i = 0; i < children.length; i++) {
                        infos[i] = new CndFileSystemProvider.FileInfo(children[i].getAbsolutePath(), children[i].isDirectory());
                    }
                    return infos;
                }
            } else {
                CndUtils.assertTrue(mode == Mode.FILE_OBJECT);
            }

            boolean resultIsDirectory = file.isDirectory();

            StringBuilder remoteAbsPath = new StringBuilder(remotePath);
//            File cacheFile = remotePath.isEmpty()? cache : CndFileUtils.createLocalFile(cache.getPath() + '/' + remotePath);
            File cacheFile = cache;
            FileObject resultFileObject = this;
            StringTokenizer pathTokenizer = new StringTokenizer(relativePath, "/"); // NOI18N
            while (pathTokenizer.hasMoreTokens()) {
                String pathComponent = pathTokenizer.nextToken();
                remoteAbsPath.append('/').append(pathComponent);
                cacheFile = CndFileUtils.createLocalFile(cacheFile.getPath() + '/' + pathComponent);
                if (pathTokenizer.hasMoreElements() || resultIsDirectory) {
                    resultFileObject = new RemoteDirectory(fileSystem, execEnv, resultFileObject, remoteAbsPath.toString(), cacheFile);
                } else {
                    resultFileObject = new RemotePlainFile(fileSystem, execEnv, (RemoteDirectory) resultFileObject, remoteAbsPath.toString(), cacheFile);
                }
            }
            return resultFileObject;

        } catch (CancellationException ex) {
            // TODO: clear CndUtils cache
            return null;
        } catch (ConnectException ex) {
            // don't report, this just means that we aren't connected
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public FileObject[] getChildren() {
        try {
            DirectoryAttributes newAttrs = getRemoteFileSupport().ensureDirSync(cache, remotePath);
            if (newAttrs != null) {
                attrs = newAttrs;
            }
            File[] childrenFiles = cache.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return ! RemoteFileSupport.FLAG_FILE_NAME.equals(name);
                }
            });
            FileObject[] childrenFO = new FileObject[childrenFiles.length];
            for (int i = 0; i < childrenFiles.length; i++) {
                String childPath = remotePath + '/' + childrenFiles[i].getName(); //NOI18N
                if (childrenFiles[i].isDirectory()) {
                    childrenFO[i] = new RemoteDirectory(fileSystem, execEnv, this, childPath, childrenFiles[i]);
                } else {
                    childrenFO[i] = new RemotePlainFile(fileSystem, execEnv, this, childPath, childrenFiles[i]);
                }
            }
            return childrenFO;
        } catch (ConnectException ex) {
            // don't report, this just means that we aren't connected
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (CancellationException ex) {
            // never report CancellationException
        }
        return new FileObject[0];
    }

    
    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
}
