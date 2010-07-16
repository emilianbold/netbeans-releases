/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.lib.cvsclient.file.DefaultFileHandler;
import org.netbeans.lib.cvsclient.file.FileReadOnlyHandler;
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
        file = FileUtil.normalizeFile(file);
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
        file.setWritable(readOnly);
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
