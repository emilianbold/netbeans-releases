/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import org.netbeans.modules.masterfs.filebasedfs.utils.FSException;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.openide.filesystems.*;
import org.openide.util.Utilities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;

public final class RootObj extends FileObject {
    private BaseFileObj realRoot = null;

    public RootObj(final BaseFileObj realRoot) {
        this.realRoot = realRoot;
    }

    public final String getName() {
        return "";//NOI18N
    }

    public final String getExt() {
        return "";//NOI18N
    }

    public final FileSystem getFileSystem() throws FileStateInvalidException {
        return getRealRoot().getFileSystem();
    }

    public final FileObject getParent() {
        return null;
    }

    public final boolean isFolder() {
        return true;
    }

    public final boolean isData() {
        return !isFolder();
    }

    public final Date lastModified() {
        return new Date(0);
    }

    public final boolean isRoot() {
        return true;
    }


    /* Test whether the file is valid. The file can be invalid if it has been deserialized
    * and the file no longer exists on disk; or if the file has been deleted.
    *
    * @return true if the file object is valid
    */
    public final boolean isValid() {
        return true;
    }

    public final void rename(final FileLock lock, final String name, final String ext) throws IOException {
        //throw new IOException(getPath());
        FSException.io("EXC_CannotRenameRoot", getFileSystem().getDisplayName()); // NOI18N        
    }

    public final void delete(final FileLock lock) throws IOException {
        //throw new IOException(getPath());
        FSException.io("EXC_CannotDeleteRoot", getFileSystem().getDisplayName()); // NOI18N        
    }

    public final Object getAttribute(final String attrName) {
        return null;
    }

    public final void setAttribute(final String attrName, final Object value) throws IOException {
        throw new IOException(getPath());
    }

    public final Enumeration getAttributes() {
        return new Enumeration() {
            public boolean hasMoreElements() {
                return false;
            }

            public Object nextElement() {
                return null;
            }
        };
    }

    public final void addFileChangeListener(final FileChangeListener fcl) {
    }

    public final void removeFileChangeListener(final FileChangeListener fcl) {
    }

    public final long getSize() {
        return 0;
    }

    public final InputStream getInputStream() throws FileNotFoundException {
        throw new FileNotFoundException(getPath());
    }

    public final OutputStream getOutputStream(final FileLock lock) throws IOException {
        throw new FileNotFoundException(getPath());
    }

    public final FileLock lock() throws IOException {
        //throw new IOException(getPath());
        FSException.io("EXC_CannotLockRoot"); // NOI18N
        return null;
    }

    public final void setImportant(final boolean b) {
    }

    public final FileObject[] getChildren() {
        return new FileObject[]{getRealRoot()};
    }

    public final FileObject getFileObject(final String name, final String ext) {
        FileObject retVal = null;

        if (name.equals(getRealRoot().getName())) {
            final String ext2 = getRealRoot().getExt();
            if (ext == null || ext.length() == 0) {
                retVal = (ext2 == null || ext2.length() == 0) ? getRealRoot() : null;
            } else {
                retVal = (ext.equals(ext2)) ? getRealRoot() : null;
            }
        }

        return retVal;
    }

    public final FileObject getFileObject(String relativePath) {
        final FileInfo fInfo = new FileInfo(getRealRoot().getFileName().getFile());
        FileObject retVal;

        if ((!Utilities.isWindows()) || fInfo.isUNCFolder()) {
            retVal = getRealRoot();
            if (fInfo.isUNCFolder() && relativePath.startsWith("//") || relativePath.startsWith("\\\\")) {//NOI18N
                relativePath = relativePath.substring(2);
            }
            retVal = retVal.getFileObject(relativePath);
        } else {
            retVal = super.getFileObject(relativePath);
        }

        return retVal;
    }


    public final FileObject createFolder(final String name) throws IOException {
        return realRoot.createFolder(name);
    }

    public final FileObject createData(final String name, final String ext) throws IOException {
        return realRoot.createData(name, ext);
    }

    public final boolean isReadOnly() {
        return true;
    }

    public final BaseFileObj getRealRoot() {
        return realRoot;
    }

    public String toString() {
        String retVal;
        try {
            FileSystem fileSystem = getFileSystem();
            retVal = fileSystem.getDisplayName();
        } catch (FileStateInvalidException e) {
            retVal = super.toString();
        }
        return retVal;
    }
}
