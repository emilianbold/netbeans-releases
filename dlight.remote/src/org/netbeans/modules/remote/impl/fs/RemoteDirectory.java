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

package org.netbeans.modules.remote.impl.fs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.locks.Lock;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.support.RemoteLogger;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Kvashin
 */
public class RemoteDirectory extends RemoteFileObjectBase {

    public static final String FLAG_FILE_NAME = ".rfs"; // NOI18N

    private volatile DirectoryAttributes attrs;
    private Reference<DirectoryStorage> storageRef;

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

    /*package*/ boolean canWrite(String childNameExt) throws IOException {
        return getDirectoryAttrs().isWritable(childNameExt);
    }

    private DirectoryAttributes getDirectoryAttrs() throws IOException {
        DirectoryAttributes result;
        if (attrs != null) {
            result = attrs;
        } else {
            result = fileSystem.getChildrenSupport().createDirectoryAttrs(cache);
            synchronized (this) {
                if (attrs == null) {
                    attrs = result;
                } else {
                    result = attrs;
                }
            }
        }
        return result;
    }

    @Override
    public FileObject getFileObject(String relativePath) {
        if (relativePath != null && relativePath.length()  > 0 && relativePath.charAt(0) == '/') { //NOI18N
            relativePath = relativePath.substring(1);
        }
        if (relativePath.endsWith("/")) { // NOI18N
            relativePath = relativePath.substring(0,relativePath.length()-1);
        }
        int slashPos = relativePath.lastIndexOf('/');
        if (slashPos > 0) { // can't be 0 - see the check above
            // relative path contains '/' => delegate to direct parent
            String parentRemotePath = remotePath + '/' + relativePath.substring(0, slashPos);
            String childNameExt = relativePath.substring(slashPos + 1);
            FileObject parentFileObject = fileSystem.findResource(parentRemotePath);
            if (parentFileObject != null &&  parentFileObject.isFolder()) {
                return parentFileObject.getFileObject(childNameExt);
            } else {
                return null;
            }
        }
        RemoteLogger.assertTrue(slashPos == -1);
        try {
            DirectoryAttributes newAttrs = getChildrenSupport().ensureDirSync(cache, (remotePath.length() == 0) ? "/" : remotePath); // NOI18N
            if (newAttrs != null) {
                synchronized (this) {
                    attrs = newAttrs;
                }
            }
            if (!getDirectoryAttrs().exists(relativePath)) {
                return null;
            }
            File cacheFile = new File(cache, relativePath);
            if (!cacheFile.exists()) {
                return null;
            } else {
                String remoteAbsPath = remotePath + '/' + relativePath;
                if (cacheFile.isDirectory()) {
                    return fileSystem.getFactory().createRemoteDirectory(this, remoteAbsPath, cacheFile);
                } else {
                    return fileSystem.getFactory().createRemotePlainFile(this, remoteAbsPath, cacheFile, FileType.File);
                }
            }
        } catch (CancellationException ex) {
            // TODO: clear CndUtils cache
            return null;
        } catch (ConnectException ex) {
            // don't report, this just means that we aren't connected
            return null;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    public FileObject[] getChildren() {
        try {
            DirectoryAttributes newAttrs = getChildrenSupport().ensureDirSync(cache, remotePath);
            if (newAttrs != null) {
                attrs = newAttrs;
            }
            File[] childrenFiles = cache.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return ! FLAG_FILE_NAME.equals(name);
                }
            });
            FileObject[] childrenFO = new FileObject[childrenFiles.length];
            for (int i = 0; i < childrenFiles.length; i++) {
                String childPath = remotePath + '/' + childrenFiles[i].getName(); //NOI18N
                if (childrenFiles[i].isDirectory()) {
                    childrenFO[i] = fileSystem.getFactory().createRemoteDirectory(this, childPath, childrenFiles[i]);
                } else {
                    childrenFO[i] = fileSystem.getFactory().createRemotePlainFile(this, childPath, childrenFiles[i], FileType.File);
                }
            }
            return childrenFO;
        } catch (ConnectException ex) {
            // don't report, this just means that we aren't connected
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (CancellationException ex) {
            // never report CancellationException
        }
        return new FileObject[0];
    }

    private DirectoryStorage getStorage() throws IOException {
        DirectoryStorage result = null;
        synchronized (this) {
            if (storageRef != null) {
                result = storageRef.get();
            }
            if (result == null) {
                File storageFile = new File(cache, FLAG_FILE_NAME);
                result = new DirectoryStorage(storageFile);
                // 1. Should we lock by cache file?
                // 2. locking within synchronized block - potentially dangerous!
                Lock lock = RemoteFileSystem.getLock(storageFile).readLock();
                try {
                    lock.lock();
                    result.load();
                } finally {
                    lock.unlock();
                }
                storageRef = new WeakReference<DirectoryStorage>(result);
            }
        }
        return result;
    }

    private void fillStorage() {
        
    }

//    @Override
//    protected void ensureSync() throws IOException, ConnectException {
//    }

    @Override
    public FileType getType() {
        return FileType.Directory;
    }
    
    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
}
