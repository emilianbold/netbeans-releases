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

import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.openide.filesystems.*;

import java.io.*;
import java.util.Date;

/**
 * @author Radek Matous
 */
public class ReplaceForSerialization extends Object implements java.io.Serializable {
    /**
     * generated Serialized Version UID
     */
    static final long serialVersionUID = -7451332135435542113L;
    private final String absolutePath;

    public ReplaceForSerialization(final File file) {
        absolutePath = file.getAbsolutePath();
    }

    public final Object readResolve() {
        final File file = new File(absolutePath);
        final FileBasedFileSystem fs = FileBasedFileSystem.getInstance(file);
        final FileObject retVal = (fs != null) ? fs.findFileObject(file) : null;
        
        
        return (retVal != null) ? retVal : new Invalid (file);
    }

    private static class Invalid extends BaseFileObj {
        protected Invalid(File file) {
            super(file);
        }

        public void delete(FileLock lock) throws IOException {
            throw new IOException(getPath()); 
        }

        boolean checkLock(FileLock lock) throws IOException {
            return false;
        }

        protected void setValid(boolean valid) {}

        public boolean isFolder() {
            return false;
        }

        public Date lastModified() {
            return null;
        }

        /* Test whether the file is valid. The file can be invalid if it has been deserialized
        * and the file no longer exists on disk; or if the file has been deleted.
        *
        * @return true if the file object is valid
        */
        public boolean isValid() {
            return false;
        }

        public InputStream getInputStream() throws FileNotFoundException {
            throw new FileNotFoundException (getPath());
        }

        public OutputStream getOutputStream(FileLock lock) throws IOException {
            throw new IOException (getPath());
        }

        public FileLock lock() throws IOException {
            throw new IOException (getPath());
        }

        public FileObject[] getChildren() {
            return new FileObject[] {};
        }

        public FileObject getFileObject(String name, String ext) {
            return null;
        }

        public FileObject createFolder(String name) throws IOException {
            throw new IOException (getPath());
        }

        public FileObject createData(String name, String ext) throws IOException {
            throw new IOException (getPath());
        }

        protected void refresh(boolean expected, boolean isFileDeletedAllowed) {
        }
    }    
}
