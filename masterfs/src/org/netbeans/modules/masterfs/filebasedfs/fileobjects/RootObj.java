/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
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
        throw new IOException(getPath());
    }

    public final FileObject createData(final String name, final String ext) throws IOException {
        throw new IOException(getPath());
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
