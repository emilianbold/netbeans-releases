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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.localhistory.store;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.localhistory.utils.FileUtils;

/**
 *
 * @author tomas
 */
public class LHTestCase extends NbTestCase {

    static int DELETED = 0;
    static int TOUCHED = 1;

    
    public LHTestCase(String testName) {
        super(testName);      
    }

    @Override
    protected void setUp() throws Exception {        
        super.setUp();
        cleanUpDataFolder();
        LocalHistoryTestStore store = createStore();
        store.cleanUp(1);        
    }

    
    protected LocalHistoryTestStore createStore() {
        System.setProperty("netbeans.user", getDataDir().getAbsolutePath());  
        return new LocalHistoryTestStore(getDataDir().getAbsolutePath());
    }
    
    protected void cleanUpDataFolder() {
        File[] files = getDataDir().listFiles();
        if(files == null || files.length == 0) {
            return;
        }
        for(File file : files) {
            if(!file.getName().equals("var")) {
                FileUtils.deleteRecursively(file);   
            }
        }        
    }
    
    public static void assertFile(File file, LocalHistoryTestStore store, long ts, long storeFileLastModified, int siblings, int parentChildren, String data, int action) throws Exception {

        File storeFolder = store.getStoreFolder(file);
        String[] files = storeFolder.list();
        if (files == null || files.length == 0) {
            fail("no files in store folder for file " + file.getAbsolutePath() + " store folder " + storeFolder.getAbsolutePath());
        }
        if (files.length != siblings) {
            fail("wrong amount of files in store folder " + files.length + " instead of expected " + siblings + " : file " + file.getAbsolutePath() + " store folder " + storeFolder.getAbsolutePath());
        }

        File storeParent = store.getStoreFolder(file.getParentFile());
        files = storeParent.list();
        if (parentChildren > 0) {
            if (files == null || files.length == 0) {        
                fail("no files in store parent for file " + file.getAbsolutePath() + " store parent " + storeParent.getAbsolutePath());
            }
        }
        if (files != null && files.length != parentChildren) {
            fail("wrong amount of files in store parent " + files.length + " instead of expected " + parentChildren + " : file " + file.getAbsolutePath() + " store parent " + storeParent.getAbsolutePath());        
        }
        

        if (file.isFile()) {
            File storeFile = store.getStoreFile(file, ts);
            if (!storeFile.exists()) {
                fail("store file doesn't exist for file");
            }
            if (data != null) {
                ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(storeFile)));
                        
                ZipEntry entry;
                while ( (entry = zis.getNextEntry()) != null ) {
                    if( entry.getName().equals(storeFile.getName()) ) {
                        break;
                    }
                }   
                assertNotNull(entry);
                assertDataInStream(zis, data.getBytes());
            }
        }

        File dataFile = store.getDataFile(file);
        assertTrue(dataFile.exists());
        assertValuesInFile(dataFile, file.isFile(), action, ts, file.getAbsolutePath());
        if (storeFileLastModified != -1) {
            assertTrue(storeFileLastModified == storeFolder.lastModified());
        }
    }

    public static void assertValuesInFile(File file, boolean isFile, int action, long modified, String filePath) throws Exception {
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(new FileInputStream(file));
            boolean f = dis.readBoolean();
            int i = dis.readInt();
            long l = dis.readLong();
            int len = dis.readInt();
            StringBuffer fileName = new StringBuffer();
            while (len-- > 0) {
                char c = dis.readChar();
                fileName.append(c);
            }
            if (f != isFile) {
                fail("storeDataFile.isFile value in file " + file.getAbsolutePath() + " is " + f + " instead of " + isFile);
            }
            if (modified != l) {
                fail("storeDataFile.modified value in file " + file.getAbsolutePath() + " is " + l + " instead of " + modified);
            }
            if (action != i) {
                fail("storeDataFile.action value in file " + file.getAbsolutePath() + " is " + i + " instead of " + action);
            }
            if (!fileName.toString().equals(filePath)) {
                fail("storeDataFile.fileName value in file " + file.getAbsolutePath() + " is " + fileName + " instead of " + filePath);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (dis != null) {
                dis.close();
            }
        }
    }
        
    public static void assertEntries(StoreEntry[] entries, File[] files, String[] data) throws Exception {
        assertEquals(entries.length, files.length);
        for(int i = 0; i < files.length; i++) {                        
            boolean blContinue = false;
            for(StoreEntry entry : entries) {
                if(entry.getFile().equals(files[i])) {
                    assertDataInStream(entry.getStoreFileInputStream(), data[i].getBytes());
                    blContinue = true;                    
                    break;
                }
            }
            if(!blContinue) {
                fail("no store entry for file " + files[i]);
            }            
        }
    }

    public static void assertEntries(StoreEntry[] entries, File file, long[] ts, String[] data) throws Exception {
        assertEquals(entries.length, ts.length);
        for(int i = 0; i < ts.length; i++) {                        
            boolean blContinue = false;
            for(StoreEntry entry : entries) {
                assertEquals(entry.getFile(), file);
                if(entry.getTimestamp() == ts[i]) {
                    assertDataInStream(entry.getStoreFileInputStream(), data[i].getBytes());
                    blContinue = true;                    
                    break;
                }
            }
            if(!blContinue) {
                fail("no store entry with timestamp " + ts[i]);
            }            
        }
    }
    
    static void createFile(LocalHistoryStore store, File file, long ts, String data) throws Exception {        
        if(data != null) {
            write(file, data.getBytes());
        } else {
            file.mkdirs();
        }
        store.fileCreate(file, ts);        
    }
    
    static void changeFile(LocalHistoryStore store, File file, long ts, String data) throws Exception {        
        write(file, data.getBytes());        
        store.fileChange(file, ts);        
    }
    
    static String read(InputStream is, int length) throws Exception {        
        try {            
            byte[] buffer = new byte[length];
            int len = is.read(buffer);
            byte[] ret = new byte[len];
            for(int i = 0; i < len; i++) {
                ret[i] = buffer[i];
            }
            return new String(ret);
        } catch(Exception e) {
            throw e;
        } finally {
            if(is != null) { is.close(); };
        }
    }
    
    static void write(File file, byte[] data) throws Exception {
        BufferedOutputStream bos  = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(data);
        } catch(Exception e) {
            throw e;
        } finally {
            if(bos != null) { bos.close(); };
        }
    }

    public static void assertDataInStream(InputStream is, byte[] data) throws Exception {        
        BufferedInputStream bis = new BufferedInputStream(is);
        try {
            byte[] contents = new byte[data.length];
            int l = bis.read(contents);            
            for (int i = 0; i < contents.length; i++) {
                if(data[i] != contents[i]) {
                    fail("given stream differs with data at byte " + i + " - " + contents[i] + " instead of " + data[i]);
                }
            }
            assertTrue(bis.read() == -1);
        } catch (Exception e) {
            throw e;
        } finally {
            if (bis != null) {
                bis.close();
            }
        }
    }

    public static void assertDataInFile(File file, byte[] data) throws Exception {
        assertDataInStream(new FileInputStream(file), data);
    }    
    
}
