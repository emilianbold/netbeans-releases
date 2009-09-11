/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import javax.swing.event.EventListenerList;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;

/**
 *
 * @author Vladimir Kvashin
 */
public abstract class RemoteFileObjectBase extends FileObject {

    protected final RemoteFileSystem fileSystem;
    protected final ExecutionEnvironment execEnv;
    protected final String remotePath;
    protected final File cache;
    private volatile EventListenerList eventSupport;
    protected final String nameExt;

    public RemoteFileObjectBase(RemoteFileSystem fileSystem, ExecutionEnvironment execEnv,
            String remotePath, File cache) {
        assert execEnv.isRemote();
        assert cache.exists();
        this.fileSystem = fileSystem;
        this.execEnv = execEnv;
        this.remotePath = remotePath;
        this.cache = cache;        
        int slashPos = remotePath.lastIndexOf('/');
        nameExt = (slashPos < 0) ? "" : remotePath.substring(slashPos + 1);
    }

    public ExecutionEnvironment getExecutionEnvironment() {
        return execEnv;
    }

    private synchronized EventListenerList getEventSupport() {
        if (eventSupport == null) {
            eventSupport = new EventListenerList();
        }
        return eventSupport;
    }

    @Override
    public void addFileChangeListener(FileChangeListener fcl) {
        getEventSupport().add(FileChangeListener.class, fcl);
    }

    @Override
    public FileObject createData(String name, String ext) throws IOException {
        throw new ReadOnlyException();
    }

    @Override
    public FileObject createFolder(String name) throws IOException {
        throw new ReadOnlyException();
    }

    @Override
    public void delete(FileLock lock) throws IOException {
        throw new ReadOnlyException();
    }

    @Override
    public Object getAttribute(String attrName) {
        if (attrName.equals("java.io.File")) { // NOI18N
            return cache;
        }
        return null;
    }


    @Override
    public Enumeration<String> getAttributes() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public String getExt() {
        int pointPos = nameExt.lastIndexOf('.');
        return (pointPos < 0) ? "" : nameExt.substring(pointPos + 1);
    }

    @Override
    public FileSystem getFileSystem() throws FileStateInvalidException {
        return fileSystem;
    }

    protected RemoteFileSupport getRemoteFileSupport() {
        return fileSystem.getRemoteFileSupport();
    }

    @Override
    public String getName() {
        int pointPos = nameExt.lastIndexOf('.');
        return (pointPos < 0) ? nameExt : nameExt.substring(0, pointPos);
    }

    @Override
    public OutputStream getOutputStream(FileLock lock) throws IOException {
        throw new ReadOnlyException();
    }

    @Override
    abstract public FileObject getFileObject(String relativePath);
    
    @Override
    public FileObject getParent() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public long getSize() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    // unfortunately warnings supression does not work due to a Java bug
    // http://bugs.sun.com/view_bug.do?bug_id=6460147
    @SuppressWarnings("deprecation")
    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public boolean canWrite() {
        return false;
    }
    
    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public boolean isValid() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public boolean isVirtual() {
        return ! cache.exists();
    }

    @Override
    public Date lastModified() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public FileLock lock() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public void removeFileChangeListener(FileChangeListener fcl) {
        getEventSupport().remove(FileChangeListener.class, fcl);
    }

    @Override
    public void rename(FileLock lock, String name, String ext) throws IOException {
        throw new ReadOnlyException();
    }

    @Override
    public void setAttribute(String attrName, Object value) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    // unfortunately warnings supression does not work due to a Java bug
    // http://bugs.sun.com/view_bug.do?bug_id=6460147
    @SuppressWarnings("deprecation")
    @Override
    public void setImportant(boolean b) {
        // Deprecated. Noithing to do.
    }

    private static class ReadOnlyException extends IOException {
        public ReadOnlyException() {
            super("The remote file system is read-only"); //NOI18N
        }
    }

}
