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

package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.lib.cvsclient.file.DefaultFileHandler;
import org.netbeans.lib.cvsclient.file.FileReadOnlyHandler;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.util.Utilities;

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
class CvsLiteFileHandler extends DefaultFileHandler implements FileReadOnlyHandler {

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
        OutputStream stream = null;
        try {
            stream = fo.getOutputStream(lock);
        } catch (IOException e) {
            lock.releaseLock();
            throw e;
        }
        return new LockedOutputStream(lock, stream);
    }

    public void removeLocalFile(String pathname) throws IOException {
        File fileToDelete = new File(pathname);
        fileToDelete = FileUtil.normalizeFile(fileToDelete);
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

    public void setFileReadOnly(File file, boolean readOnly) throws IOException {
        String [] command = new String[3];
        // TODO: update for JDK 6
        if (Utilities.isWindows()) {
            command[0] = "attrib";
            command[1] = readOnly ? "+R" : "-R";
        } else {
            command[0] = "chmod";
            command[1] = readOnly ? "u-w" : "u+w";
        }
        command[2] = file.getAbsolutePath();
        try {
            Runtime.getRuntime().exec(command);
        } catch (Exception e) {
            // probably does not work, ignore
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
