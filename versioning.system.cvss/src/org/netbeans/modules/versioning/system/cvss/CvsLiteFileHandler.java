/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.lib.cvsclient.file.DefaultFileHandler;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;

/**
 * Cvs client library FileHandler that performs
 * operations using openide filesystems.
 *
 * <p>It writes user's data files. Folders, temporary
 * files and metadata files are written directly by
 * the cvsclient library.
 *
 * <p>It supresses FilesystemHandler event propagating
 * to cache to avoid
 *
 * @author Petr Kuzel
 */
class CvsLiteFileHandler extends DefaultFileHandler {

    protected boolean createNewFile(File file) throws IOException {
        boolean exists = file.isFile();
        if (exists) {
            return false;
        } else {
            File parent = file.getParentFile();
            FileObject fo = Utils.mkfolders(parent);
            try {
                FilesystemHandler.ignoreEvents(true);
                try {
                    fo.createData(file.getName());
                } catch (IOException e) {
                    // #69639: Try File I/O instead
                    return file.createNewFile();
                }
            } finally {
                FilesystemHandler.ignoreEvents(false);
            }
            return true;
        }
    }

    protected OutputStream createOutputStream(File file) throws IOException {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) {
            // #69639: Try File I/O instead
            return new FileOutputStream(file);
        }
        FileLock lock = fo.lock();
        OutputStream stream = fo.getOutputStream(lock);
        return new LockedOutputStream(lock, stream);
    }

    public void removeLocalFile(String pathname) throws IOException {
        File fileToDelete = new File(pathname);
        FileObject fo = FileUtil.toFileObject(fileToDelete);
        if (fo == null) {
            // #69639: Try File I/O instead
            fileToDelete.delete();
            return;
        }
        try {
            FilesystemHandler.ignoreEvents(true);
            fo.delete();
        } finally {
            FilesystemHandler.ignoreEvents(false);
        }
    }

    public void renameLocalFile(String pathname, String newName) throws IOException {
        File sourceFile = new File(pathname);
        FileObject fo = FileUtil.toFileObject(sourceFile);
        if (fo == null) {
            // #69639: Try File I/O instead
            sourceFile.renameTo(new File(sourceFile.getParentFile(), newName));
            return;
        }
        FileLock lock = null;
        try {
            lock = fo.lock();
            try {
                FilesystemHandler.ignoreEvents(true);
                fo.rename(lock, newName, null);
            } finally {
                FilesystemHandler.ignoreEvents(false);
            }
        } finally {
            if (lock != null) {
                lock.releaseLock();
            }
        }
    }

    private static class LockedOutputStream extends OutputStream {

        private final OutputStream peer;
        private final FileLock lock;

        public LockedOutputStream(FileLock lock, OutputStream peer) {
            this.lock = lock;
            this.peer = peer;
        }

        public void close() throws IOException {
            lock.releaseLock();
            try {
                FilesystemHandler.ignoreEvents(true);
                peer.close();
            } finally {
                FilesystemHandler.ignoreEvents(false);
            }
        }

        public void flush() throws IOException {
            peer.flush();
        }

        public void write(byte b[]) throws IOException {
            peer.write(b);
        }

        public void write(byte b[], int off, int len) throws IOException {
            peer.write(b, off, len);
        }

        public void write(int b) throws IOException {
            peer.write(b);
        }
    }
}
