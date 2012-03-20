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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.refactoring.spi;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.openide.text.NbDocumentRefactoringHack;
import org.netbeans.modules.refactoring.spi.impl.UndoableWrapper;
import org.netbeans.modules.refactoring.spi.impl.UndoableWrapper.UndoableEditDelegate;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import sun.misc.IOUtils;

/**
 * Simple backup facility can be used to backup files and implement undo For
 * instance Java Refactoring module implements undo this way:
 *
 * public Problem prepare(RefactoringElementsBag elements) { . .
 * elements.registerTransaction(new RetoucheCommit(results)); }
 *
 * where RetoucheCommit is Transaction:
 * <pre>
 * BackupFacility.Handle handle;
 * public void commit() {
 *   FileObject[] files;
 *   .
 *   .
 *   handle = BackupFacility.getDefault().backup(files);
 *   doCommit();
 * }
 * public void rollback() {
 *   //rollback all files
 *   handle.restore();
 * }
 * </pre>
 *
 * You can register your own implementation via META-INF services.
 *
 * @see Transaction
 * @see RefactoringElementImplementation#performChange
 * @see RefactoringElementImplementation#undoChange
 * @see RefactoringElementsBag#registerTransaction
 * @see RefactoringElementsBag#addFileChange
 * @see BackupFacility.Handle
 * @author Jan Becicka
 */
abstract class BackupFacility2 {
    
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.refactoring.Undo");

    private BackupFacility2() {
    }
    private static BackupFacility2 defaultInstance;

    /**
     * does backup
     *
     * @param file file(s) to backup
     * @return handle which can be used to restore files
     * @throws java.io.IOException if backup failed
     */
    public abstract Handle backup(FileObject... file) throws IOException;

    /**
     * does backup
     *
     * @param fileObjects FileObjects to backup
     * @return handle which can be used to restore files
     * @throws java.io.IOException
     */
    public final Handle backup(Collection<? extends FileObject> fileObjects) throws IOException {
        return backup(fileObjects.toArray(new FileObject[fileObjects.size()]));
    }

    /**
     * do cleanup all backup files are deleted all internal structures cleared
     * default implemntation
     */
    public abstract void clear();

    /**
     * @return default instance of this class. If there is instance of this
     * class in META-INF services -> this class is returned. Otherwise default
     * implementation is used.
     */
    public static BackupFacility2 getDefault() {
        BackupFacility2 instance = Lookup.getDefault().lookup(BackupFacility2.class);
        return (instance != null) ? instance : getDefaultInstance();
    }

    private static synchronized BackupFacility2 getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new DefaultImpl();
        }

        return defaultInstance;
    }

    /**
     * Handle class representing handle to file(s), which were backuped by {@link  org.netbeans.modules.refactoring.spi.BackupFacility#backup()}
     */
    public interface Handle {

        /**
         * restore file(s), which was stored by {@link  org.netbeans.modules.refactoring.spi.BackupFacility#backup()}
         *
         * @throws java.io.IOException if restore failed.
         */
        public abstract void restore() throws java.io.IOException;

        void storeChecksum() throws IOException;

        public Collection<String> checkChecksum(boolean undo) throws IOException;
    }

    private static class DefaultHandle implements Handle {

        private List<Long> handle;
        private DefaultImpl instance;

        private DefaultHandle(DefaultImpl instance, List<Long> handles) {
            this.handle = handles;
            this.instance = instance;
        }

        @Override
        public void restore() throws IOException {
            for (long l : handle) {
                instance.restore(l);
            }
        }

        @Override
        public void storeChecksum() throws IOException {
            for (long l : handle) {
                instance.storeChecksum(l);
            }
        }

        @Override
        public Collection<String> checkChecksum(boolean undo) throws IOException {
            Collection<String> result = new LinkedList<String>();
            for (long l : handle) {
                String checkChecksum = instance.checkChecksum(l, undo);
                if (checkChecksum !=null) {
                    result.add(checkChecksum);
                }
            }
            return result;

        }
    }

    private static class DefaultImpl extends BackupFacility2 {

        private long currentId = 0;
        private Map<Long, BackupEntry> map = new HashMap<Long, BackupEntry>();

        private String MD5toString(byte[] digest) {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < digest.length; i++) {
                b.append(Integer.toHexString(0xFF & digest[i]));
            }
            return b.toString();
        }

        private void storeChecksum(long l) throws IOException {
            BackupEntry backup = map.get(l);
            File f = new File(backup.path);
            FileObject fo = FileUtil.toFileObject(f);
            DataObject dob = DataObject.find(fo);
            if (dob != null) {
                CloneableEditorSupport ces = dob.getLookup().lookup(CloneableEditorSupport.class);
                final BaseDocument doc = (BaseDocument) ces.getDocument();
                if (doc !=null && doc.isAtomicLock()) {
                    //workaround to avoid deadlock
                    return;
                }
            }
            LOG.fine("Storing MD5 for " + backup.path);
            backup.checkSum = getMD5(getInputStream(backup.path));
            LOG.fine("MD5 is: " + MD5toString(backup.checkSum));
        }

        private String checkChecksum(long l, boolean undo) {

            try {
                BackupEntry backup = map.get(l);
                File f = new File(backup.path);
                FileObject fo = FileUtil.toFileObject(f);
                DataObject dob = DataObject.find(fo);
                if (dob != null) {
                    CloneableEditorSupport ces = dob.getLookup().lookup(CloneableEditorSupport.class);

                    final BaseDocument doc = (BaseDocument) ces.getDocument();
                    if (doc != null && doc.isAtomicLock()) {
                        //workaround to avoid deadlock
                        return null;
                    } else {
                        EditorCookie editor = dob.getLookup().lookup(EditorCookie.class);
                        if (editor != null  && doc!=null && editor.isModified()) {
                            UndoableEditDelegate edit = undo?NbDocumentRefactoringHack.getEditToBeUndoneOfType(editor, UndoableWrapper.UndoableEditDelegate.class):NbDocumentRefactoringHack.getEditToBeRedoneOfType(editor, UndoableWrapper.UndoableEditDelegate.class);
                            if (edit == null) {
                                try {
                                    LOG.fine("Editor Undo Different");
                                    return backup.path.toURL().getPath();
                                } catch (MalformedURLException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        }

                    }
                }

                try {
                    LOG.fine("Checking MD5 for " + backup.path);
                    byte[] ts = getMD5(getInputStream(backup.path));
                    if (!Arrays.equals(backup.checkSum, ts)) {
                        LOG.fine("MD5 check failed");
                        return backup.path.toURL().getPath();
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }

        private InputStream getInputStream(URI path) throws IOException {
            File f = new File(path);
            FileObject fo = FileUtil.toFileObject(f);
            DataObject dob = DataObject.find(fo);
            CloneableEditorSupport ces = dob.getLookup().lookup(CloneableEditorSupport.class);
            if (ces != null && ces.isModified()) {
                LOG.fine("Editor Input Stream");
                return ces.getInputStream();
            }
            LOG.fine("File Input Stream");
            return fo.getInputStream();
        }

        private class BackupEntry {

            private File file;
            private URI path;
            private byte[] checkSum;
            private boolean undo = true;

            public BackupEntry() {
            }

            public boolean isUndo() {
                return undo;
            }

            public void setUndo(boolean undo) {
                this.undo = undo;
            }
        }

        /**
         * Creates a new instance of BackupFacility
         */
        private DefaultImpl() {
        }

        @Override
        public Handle backup(FileObject... file) throws IOException {
            ArrayList<Long> list = new ArrayList<Long>();
            for (FileObject f : file) {
                list.add(backup(f));
            }
            return new DefaultHandle(this, list);
        }

        /**
         * does backup
         *
         * @param file to backup
         * @return id of backup file
         * @throws java.io.IOException if backup failed
         */
        public long backup(FileObject file) throws IOException {
            try {
                BackupEntry entry = new BackupEntry();
                entry.file = File.createTempFile("nbbackup", null); //NOI18N
                copy(file, entry.file);
                entry.path = file.getURL().toURI();
                map.put(currentId, entry);
                entry.file.deleteOnExit();
                return currentId++;
            } catch (URISyntaxException ex) {
                throw (IOException) new IOException(file.toString()).initCause(ex);
            }
        }

        private byte[] getMD5(InputStream is) throws IOException {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                try {
                    is = new DigestInputStream(is, md);
                    IOUtils.readFully(is, -1, true);
                } finally {
                    is.close();
                }
                return md.digest();
            } catch (NoSuchAlgorithmException ex) {
                throw new IOException(ex);
            }
        }

        private static java.lang.reflect.Field undoRedo;

        static {
            try {
                //obviously hack. See 108616 and 48427
                undoRedo = org.openide.text.CloneableEditorSupport.class.getDeclaredField("undoRedo"); //NOI18N
                undoRedo.setAccessible(true);
            } catch (NoSuchFieldException ex) {
                Exceptions.printStackTrace(ex);
            } catch (SecurityException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        /**
         * restore file, which was stored by backup(file)
         *
         * @param id identification of backup transaction
         * @throws java.io.IOException if restore failed.
         */
        void restore(long id) throws IOException {
            BackupEntry entry = map.get(id);
            if (entry == null) {
                throw new IllegalArgumentException("Backup with id " + id + "does not exist"); // NOI18N
            }
            File backup = File.createTempFile("nbbackup", null); //NOI18N
            backup.deleteOnExit();
            File f = new File(entry.path);
            if (createNewFile(f)) {
                backup.createNewFile();
                copy(f, backup);
            }
            FileObject fileObj = FileUtil.toFileObject(f);

            if (!tryUndoOrRedo(fileObj, entry)) {
                copy(entry.file, fileObj);
            }
            entry.file.delete();
            if (backup.exists()) {
                entry.file = backup;
            } else {
                map.remove(id);
            }
        }

        private boolean tryUndoOrRedo(final FileObject fileObj, final BackupEntry entry) throws DataObjectNotFoundException {
            DataObject dob = DataObject.find(fileObj);
            if (dob != null) {
                CloneableEditorSupport ces = dob.getLookup().lookup(CloneableEditorSupport.class);
                final org.openide.awt.UndoRedo.Manager manager;
                try {
                    manager = (org.openide.awt.UndoRedo.Manager) undoRedo.get(ces);
                    final BaseDocument doc = (BaseDocument) ces.getDocument();
                    if (doc==null) {
                        return false;
                    }
                    if (doc.isAtomicLock()) {
                        //undo already performed
                        if (entry.isUndo()) {
                            entry.setUndo(false);
                        } else {
                            entry.setUndo(true);
                        }
                    } else {
                        if ((entry.isUndo() && manager.canUndo()) || (!entry.isUndo() && manager.canRedo())) {
                            doc.runAtomic(new Runnable() {

                                @Override
                                public void run() {
                                    if (entry.isUndo()) {
                                        manager.undo();
                                        entry.setUndo(false);
                                    } else {
                                        manager.redo();
                                        entry.setUndo(true);
                                    }
                                }
                            });
                        } else {
                            return false;
                        }
                    }
                    return true;
                } catch (IllegalArgumentException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IllegalAccessException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return false;
        }

        /**
         * workaround for #93390
         */
        private boolean createNewFile(File f) throws IOException {
            if (f.exists()) {
                return true;
            }
            File parent = f.getParentFile();
            if (parent != null) {
                createNewFolder(parent);
            }
            FileUtil.createData(f);
            return false;
        }

        private void createNewFolder(File f) throws IOException {
            if (!f.exists()) {
                File parent = f.getParentFile();
                if (parent != null) {
                    createNewFolder(parent);
                }
                FileUtil.createFolder(f);
            }
        }

        private void copy(FileObject a, File b) throws IOException {
            InputStream fs = a.getInputStream();
            FileOutputStream fo = new FileOutputStream(b);
            copy(fs, fo);
        }

        private void copy(File a, File b) throws IOException {
            FileInputStream fs = new FileInputStream(a);
            FileOutputStream fo = new FileOutputStream(b);
            copy(fs, fo);
        }

        private void copy(File a, FileObject b) throws IOException {
            FileInputStream fs = new FileInputStream(a);
            OutputStream fo = b.getOutputStream();
            copy(fs, fo);
        }

        private void copy(InputStream is, OutputStream os) throws IOException {
            try {
                FileUtil.copy(is, os);
            } finally {
                is.close();
                os.close();
            }
        }

        @Override
        public void clear() {
            for (BackupEntry entry : map.values()) {
                entry.file.delete();
            }
            map.clear();
        }
    }
}
