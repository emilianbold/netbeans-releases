/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

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
            return new Date(0L);
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

        public void refresh(final boolean expected, boolean fire) {
    }    
}
}
