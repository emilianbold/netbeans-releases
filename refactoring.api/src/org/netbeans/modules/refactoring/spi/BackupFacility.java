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
import java.util.HashMap;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Simple backup facility
 * can be used to backup files and implement undo
 * @author Jan Becicka
 */
public class BackupFacility {
    
    private static BackupFacility instance;
    private long currentId = 0;
    private HashMap<Long, BackupEntry> map = new HashMap();
    
    private class BackupEntry {
        private File file;
        private String path;
    }
    
    /** Creates a new instance of BackupFacility */
    private BackupFacility() {
    }
    
    /**
     * @return singletone instance
     */
    public static BackupFacility getDefault() {
        if (instance==null) {
            instance = new BackupFacility();
        }
        return instance;
    }
    
    /**
     * does beckup
     * @param file to backup
     * @return id of backup file
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
     */
    public void restore(long id) throws IOException {
        BackupEntry entry = map.get(id);
        if(entry==null) {
            throw new IllegalArgumentException("Backup with id " + id + "does not exist");
        }
        File backup = File.createTempFile("nbbackup", null); //NOI18N
        backup.deleteOnExit();
        File f = new File(entry.path);
        if (!f.exists()) {
            f.createNewFile();
        } else {
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
    
    /**
     * do cleanup
     * all backup files are deleted
     * all internal structures cleared
     */ 
    public void clear() {
        for(BackupEntry entry: map.values()) {
            entry.file.delete();
        }
        map.clear();
    }
}
