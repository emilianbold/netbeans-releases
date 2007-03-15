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

package org.netbeans.modules.refactoring.spi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * Simple backup facility
 * can be used to backup files and implement undo
 * For instance Java Refactoring module implements undo this way:
 *
 * public Problem prepare(RefactoringElementsBag elements) {
 * .
 * .
 *   elements.registerTransaction(new RetoucheCommit(results));
 * }
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
 * @see Transaction
 * @see RefactoringElementImplementation#performChange
 * @see RefactoringElementImplementation#undoChange
 * @see RefactoringElementsBag#registerTransaction
 * @see RefactoringElementsBag#registerFileChange
 * @see BackupFacility.Handle
 * @author Jan Becicka
 */
public abstract class BackupFacility {
    
    private BackupFacility() {
    }
    
    private static BackupFacility defaultInstance;
    
    /**
     * does beckup
     * @param file file(s) to backup
     * @return handle which can be used to restore files
     * @throws java.io.IOException if backup failed
     */
    public abstract Handle backup(FileObject... file) throws IOException;
    
    /**
     * does backup
     * @param fileObjects FileObjects to backup
     * @return handle which can be used to restore files
     * @throws java.io.IOException 
     */
    public final Handle backup(Collection<? extends FileObject> fileObjects) throws IOException {
        return backup(fileObjects.toArray(new FileObject[fileObjects.size()]));
    }
    
    /**
     * do cleanup
     * all backup files are deleted
     * all internal structures cleared
     * default implementa
     */
    public abstract void clear();
    
    /**
     * @return default instance of this class. If there is instance of this 
     * class in META-INF services -> this class is returned. Otherwise default 
     * implementation is used.
     */
    public static BackupFacility getDefault() {
        BackupFacility instance = Lookup.getDefault().lookup(BackupFacility.class);
        return (instance != null) ? instance : getDefaultInstance();
    }
    
    private static synchronized BackupFacility getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new DefaultImpl();
        }
        
        return defaultInstance;
    }
    
    /**
     * Handle class representing handle to file{s), which were backuped
     * by
     * {@link  org.netbeans.modules.refactoring.spi.BackupFacility.backup()}
     */
    public interface Handle {
        /**
         * restore file(s), which was stored by  {@link  org.netbeans.modules.refactoring.spi.BackupFacility.backup()}
         * @throws java.io.IOException if restore failed.
         */
        void restore() throws IOException;
    }
    
    private static class DefaultHandle implements Handle {
        ArrayList<Long> handle;
        DefaultImpl instance;
        private DefaultHandle(DefaultImpl instance, ArrayList<Long> handles) {
            this.handle = handles;
            this.instance = instance;
        }
        public void restore() throws IOException {
            for (long l:handle) {
                instance.restore(l);
            }
        }
    }
    
    private static class DefaultImpl extends BackupFacility {
        
        private long currentId = 0;
        private HashMap<Long, BackupEntry> map = new HashMap();
        
        private class BackupEntry {
            private File file;
            private String path;
        }
        
        /** Creates a new instance of BackupFacility */
        private DefaultImpl() {
        }
        
        public Handle backup(FileObject ... file) throws IOException {
            ArrayList<Long> list = new ArrayList();
            for (FileObject f:file) {
                list.add(backup(f));
            }
            return new DefaultHandle(this, list);
        }
        /**
         * does beckup
         * @param file to backup
         * @return id of backup file
         * @throws java.io.IOException if backup failed
         */
        public long backup(FileObject file) throws IOException {
            BackupEntry entry = new BackupEntry();
            entry.file = File.createTempFile("nbbackup", null); //NOI18N
            copy(FileUtil.toFile(file), entry.file);
            entry.path = file.getPath();
            map.put(currentId, entry);
            entry.file.deleteOnExit();
            return currentId++;
        }
        /**
         * restore file, which was stored by backup(file)
         * @param id identification of backup transaction
         * @throws java.io.IOException if restore failed.
         */
        void restore(long id) throws IOException {
            BackupEntry entry = map.get(id);
            if(entry==null) {
                throw new IllegalArgumentException("Backup with id " + id + "does not exist");
            }
            File backup = File.createTempFile("nbbackup", null); //NOI18N
            backup.deleteOnExit();
            File f = new File(entry.path);
            if (createNewFile(f)) {
                backup.createNewFile();
                copy(f,backup);
            }
            copy(entry.file,f);
            FileUtil.toFileObject(f).refresh(true);
            entry.file.delete();
            if (backup.exists()) {
                entry.file = backup;
            } else {
                map.remove(id);
            }
        }
        
        /**
         * workaround for #93390
         */
        private boolean createNewFile(File f) throws IOException {
            if (f.exists())
                return true;
            File parent = f.getParentFile();
            if (parent!=null) {
                createNewFile(parent);
            }
            f.createNewFile();
            return false;
            
        }
        
        private void copy(File a, File b) throws IOException {
            FileInputStream fs = new FileInputStream(a);
            FileOutputStream fo = new FileOutputStream(b);
            try {
                FileUtil.copy(fs, fo);
            } finally {
                fs.close();
                fo.close();
            }
        }
        
        public void clear() {
            for(BackupEntry entry: map.values()) {
                entry.file.delete();
            }
            map.clear();
        }
    }
}