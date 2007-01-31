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
package org.netbeans.modules.localhistory.store;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Stupka
 * 
 * // XXX override for folder and to be deleted 
 * // XXX status or isDeleted
 * 
 */
public abstract class StoreEntry {
            
    private final File file;
    private final File storeFile;
    private final long ts;
    private final String label;
    private final Date date;    
    private String mimeType = null;
      
    public static StoreEntry createStoreEntry(File file, File storeFile, long ts, String label) {
        return new DefaultStoreEntry(file, storeFile, ts, label);
    }

    public static StoreEntry createDeletedStoreEntry(File file, long ts) {
        return new DeletedStoreEntry(file, ts);
    }

    public static StoreEntry createFakeStoreEntry(File file, long ts) {
        return new FakeStoreEntry(file, ts);
    }
    
    private StoreEntry(File file, File storeFile, long ts, String label) {
        this.file = file;
        this.storeFile = storeFile;
        this.ts = ts;
        this.label = label;
        this.date = new Date(ts);
    }    
    
    public File getStoreFile() {
        return storeFile;
    }

    public File getFile() {
        return file;
    }
    
    public long getTimestamp() {
        return ts;
    }
    
    public String getLabel() {
        return label != null ? label : "";
    }
    
    public Date getDate() {
        return date;
    }
    
    public boolean representsFile() {
        return storeFile.isFile();
    }
        
    public String getMIMEType() {
        if(mimeType == null) {
            FileObject fo = FileUtil.toFileObject(getFile());
            if(fo != null) {
                mimeType = fo.getMIMEType();   
            } else {
                mimeType = "content/unknown";
            }                
        }        
        return mimeType;
    }    
    
    // XXX compresion level etc.
    static OutputStream createStoreFileOutputSteam(File storeFile) throws FileNotFoundException, IOException {
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(storeFile)));
        ZipEntry entry = new ZipEntry(storeFile.getName());
        zos.putNextEntry(entry);
        return zos;
    }
        
    abstract OutputStream getStoreFileOutputStream() throws FileNotFoundException, IOException;
    public abstract InputStream getStoreFileInputStream() throws FileNotFoundException, IOException;    
    
    private static class DefaultStoreEntry extends StoreEntry { 
        
        private DefaultStoreEntry(File file, File storeFile, long ts, String label) {
            super(file, storeFile, ts, label);
        }    
        
        OutputStream getStoreFileOutputStream() throws FileNotFoundException, IOException {
            return createStoreFileOutputSteam(getStoreFile());
        }

        public InputStream getStoreFileInputStream() throws FileNotFoundException, IOException {
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(getStoreFile())));
            ZipEntry entry;
            while ( (entry = zis.getNextEntry()) != null ) {
                if( entry.getName().equals(getStoreFile().getName()) ) {
                    return zis;
                }
            }
            throw new FileNotFoundException();        
        }                    
    }
    
    private static class DeletedStoreEntry extends StoreEntry {  
        public DeletedStoreEntry(File file, long ts) {
            super(file, null, ts, "");
        } 
        
        OutputStream getStoreFileOutputStream() throws FileNotFoundException, IOException {
            throwNoStoreEntry();
            return null;
        }

        public InputStream getStoreFileInputStream() throws FileNotFoundException, IOException {
            throwNoStoreEntry();
            return null;            
        }            
        
        private void throwNoStoreEntry() throws FileNotFoundException {
            throw new FileNotFoundException("There is no store entry for file " + getFile() + " and timestamp " + getTimestamp());
        }
        
    }

    private static class FakeStoreEntry extends StoreEntry {
        
        public FakeStoreEntry(File file, long ts) {
            super(file, file, ts, "");
        }  

        OutputStream getStoreFileOutputStream() throws FileNotFoundException, IOException {
            throw new FileNotFoundException("There is no OutputStream for this for file " + getFile());
        }

        public InputStream getStoreFileInputStream() throws FileNotFoundException, IOException {
            return new FileInputStream(getFile());            
        }            
    }    
}
