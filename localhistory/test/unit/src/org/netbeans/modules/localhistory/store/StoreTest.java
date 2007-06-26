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
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.localhistory.utils.FileUtils;

/**
*/
public class StoreTest extends NbTestCase {
    
    private static int DELETED = 0;
    private static int TOUCHED = 1;
    
    public StoreTest(String testName) {
        super(testName);      
    }
    
    public void testFileCreate() throws Exception {

        LocalHistoryTestStore store = createStore();
        store.cleanUp(1); // cleanup imediately
        long ts = System.currentTimeMillis();
        
        // create file1 in store
        File file = new File(getDataDir(), "file1");
        createFile(store, file, ts, "data");
        // is it there?
        assertFile(file, store, ts, -1, 2, 1, "data", TOUCHED);

        File storefile = store.getStoreFile(file, ts);
        // try again
//        if(store.lastModified(file) != ts) {
//            createFile(store, file, ts, "data");
//        }
        // no changes ?
        assertFile(file, store, ts, storefile.lastModified(), 2, 1, "data", TOUCHED);

        // create file2 in store
        file = new File(getDataDir(), "file2");
        createFile(store, file, ts, "data");
        // is it there?
        assertFile(file, store, ts, -1, 2, 1, "data", TOUCHED);

        // check the whole repository
//        File mainDir = getStoreMainFolder();
//        File[] files = mainDir.listFiles();
//        assertTrue(files != null && files.length == 2);
//        files = files[0].listFiles();
//        assertTrue(files != null && files.length == 1);
//        files = files[1].listFiles();
//        assertTrue(files != null && files.length == 1);
        // create file in store
        File folder = new File(getDataDir(), "folder");
        ts = System.currentTimeMillis();
        // create folder
        createFile(store, folder, ts, null);
        // is it there?
        assertFile(folder, store, ts, -1, 1, 1, null, TOUCHED);

        file = new File(folder, "file2");
        createFile(store, file, ts, "data");
        // is it there?
        assertFile(file, store, ts, -1, 2, 2, "data", TOUCHED);
        //File parentFile = file.getParentFile();
        //checkParent(parentFile, ts, 2);
        // one more file in folder
        file = new File(folder, "file3");
        createFile(store, file, ts, "data");
        assertFile(file, store, ts, -1, 2, 2, "data", TOUCHED);
        //checkParent(parentFile, ts, 3);
        // XXX check parent journal
    }

    public void testFileChange() throws Exception {
        LocalHistoryTestStore store = createStore();
        store.cleanUp(1); // cleanup imediately
        long ts = System.currentTimeMillis();

        // create file in store
        File file = new File(getDataDir(), "file1");
        createFile(store, file, ts, "data");

        File storefile = store.getStoreFile(file, ts);
        // change file with same ts
        // XXX
//        if(store.lastModified(file) != ts) {
//            changeFile(store, file, ts, "data2");
//        }
        // check that nothing changed
        assertFile(file, store, ts, storefile.lastModified(), 2, 1, "data", TOUCHED);

        // change file with new ts
        ts = System.currentTimeMillis();
        changeFile(store, file, ts, "data2");
        // check the change
        assertFile(file, store, ts, -1, 3, 1, "data2", TOUCHED);
    }

    public void testFileDelete() throws Exception {
        LocalHistoryTestStore store = createStore();
        store.cleanUp(1); // cleanup imediately
        long ts = System.currentTimeMillis();

        // create file in store
        File file = new File(getDataDir(), "file1");
        createFile(store, file, ts, "data");

        store.fileDelete(file, ts);
        // check
        File storefile = store.getStoreFile(file, ts);
        assertFile(file, store, ts, storefile.lastModified(), 2, 1, "data", DELETED);

        file = new File(getDataDir(), "file2");
        createFile(store, file, ts, "data");

        store.fileDelete(file, ts);
        // check
        storefile = store.getStoreFile(file, ts);
        assertFile(file, store, ts, storefile.lastModified(), 2, 1, "data", DELETED);
    }

    private void assertFile(File file, LocalHistoryTestStore store, long ts, long storeFileLastModified, int siblings, int parentChildren, String data, int action) throws Exception {

        File storeFolder = store.getStoreFolder(file);
        String[] files = storeFolder.list();
        if (files == null || files.length == 0) {
            fail("no files in store folder for file " + file.getAbsolutePath() + " store folder " + storeFolder.getAbsolutePath());
        }
        if (files.length != siblings) {
            fail("wrong files amount of files in store folder for file " + file.getAbsolutePath() + " store folder " + storeFolder.getAbsolutePath());
        }

        File storeParent = store.getStoreFolder(file.getParentFile());
        files = storeParent.list();
        if (files == null || files.length == 0) {
            fail("no files in store parent for file " + file.getAbsolutePath() + " store parent " + storeParent.getAbsolutePath());
        }
        if (files.length != parentChildren) {
            fail("wrong files amount of files in store parent for file " + file.getAbsolutePath() + " store parent " + storeParent.getAbsolutePath());
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

    private void assertValuesInFile(File file, boolean isFile, int action, long modified, String filePath) throws Exception {
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
            ;
        }
    }
    
    public void testGetDeletedFiles() throws Exception {
        LocalHistoryTestStore store = createStore();
        store.cleanUp(1); 
        
        cleanUpDataFolder();        
        File folder = getDataDir();        
        folder.mkdirs();
        
        // create the files
        File file1 = new File(folder, "file1");
        File file2 = new File(folder, "file2");
        File file3 = new File(folder, "file3");
        File file4 = new File(folder, "file4");
        
        createFile(store, file1, System.currentTimeMillis(), "data1");
        createFile(store, file2, System.currentTimeMillis(), "data2");
        createFile(store, file3, System.currentTimeMillis(), "data3");
        createFile(store, file4, System.currentTimeMillis(), "data4");
        
        // touch the files 
        changeFile(store, file1, System.currentTimeMillis(), "data1.1");
        changeFile(store, file2, System.currentTimeMillis(), "data2.1");
        changeFile(store, file3, System.currentTimeMillis(), "data3.1");
        changeFile(store, file4, System.currentTimeMillis(), "data4.1");
        
        // delete one of them
        store.fileDelete(file2, System.currentTimeMillis());        
        StoreEntry[] entries = store.getDeletedFiles(folder);
        assertEntries(entries, new File[] { file2 }, new String[] { "data2.1" } );
        
        // delete one of them
        store.fileDelete(file3, System.currentTimeMillis());        
        entries = store.getDeletedFiles(folder);
        assertEntries(entries, new File[] { file2, file3 }, new String[] { "data2.1", "data3.1" } );
        
        // delete without entry - only via .delete()
        file4.delete();
        entries = store.getDeletedFiles(folder);
        assertEntries(entries, new File[] { file2, file3, file4 }, new String[] { "data2.1", "data3.1", "data4.1" } );
        
    }

    public void testGetStoreEntry() throws Exception { 
        LocalHistoryTestStore store = createStore();
        store.cleanUp(1); 
            
        cleanUpDataFolder();
        
        File folder = getDataDir();        
        folder.mkdirs();        
        File file1 = new File(folder, "file1");
        File file2 = new File(folder, "file2");
        
        createFile(store, file1, System.currentTimeMillis(), "data1");
        createFile(store, file2, System.currentTimeMillis(), "data2");
        
        // change the file 
        changeFile(store, file1, System.currentTimeMillis(), "data1.1");
        // rewrite the file
        write(file1, "data1.2".getBytes());                
        assertDataInFile(file1, "data1.2".getBytes());
        
        // get the files last state
        StoreEntry entry = store.getStoreEntry(file1, System.currentTimeMillis());
        assertNotNull(entry);
        assertDataInStream(entry.getStoreFileInputStream(), "data1".getBytes());        
    }   
    
    
    
    public void testGetFolderState() throws Exception {        
        LocalHistoryTestStore store = createStore();
        store.cleanUp(1); // cleanup imediately
        
        cleanUpDataFolder();
                
        // check for deleted root folder
        File folder = new File(getDataDir(), "datafolder");
        setupFirstFolderToRevert(store, folder);        
        
        File[] files = folder.listFiles();              
        
        assertEquals(files.length, 7);  //   fileNotInStorage 
                                        //   fileUnchanged 
                                        //   fileChangedAfterRevert 
                                        // X fileDeletedAfterRevert 
                                        //   fileDeletedBeforeRevert
                                        //   fileUndeletedBeforeRevert 
                                        //   fileCreatedToLate 
                                        //   folderCreatedAfterRevert
        
        store.fileDelete(folder, System.currentTimeMillis());
        Thread.sleep(1000); // give me some time
        long revertToTS = System.currentTimeMillis();
        
        StoreEntry[] entries = store.getFolderState(folder, files , revertToTS);
        assertEquals(entries.length, 0);  // all are deleted
        
        
        store.cleanUp(1); 
        cleanUpDataFolder();                
        folder = new File(getDataDir(), "datafolder");
        revertToTS = setupFirstFolderToRevert(store, folder);        
        files = folder.listFiles(); 
        files = folder.listFiles();              
        assertEquals(files.length, 7);  //   fileNotInStorage 
                                        //   fileUnchanged 
                                        //   fileChangedAfterRevert 
                                        // X fileDeletedAfterRevert 
                                        //   fileDeletedBeforeRevert
                                        //   fileUndeletedBeforeRevert 
                                        //   fileCreatedToLate 
        
                                        // X  folderDeletedAfterRevert
                                        //    folderCreatedAfterRevert    
        
       
        entries = store.getFolderState(folder, files , revertToTS);

        for(StoreEntry se : entries) {
            System.out.println(se.getFile().getName());
        }    
        
        assertEquals(entries.length, 8);    
        //   * returned, X as to be deleted
        //   fileNotInStorage             - 
        //*   fileUnchanged                - *
        //*   fileChangedAfterRevert       - * previous revision
        //*   fileDeletedAfterRevert       - *
        //*   fileUndeletedBeforeRevert    - *
        // X fileCreatedAfterRevert       - * X
        //* X fileDeletedBeforeRevert      - * X
        
        //*   folderDeletedAfterRevert     - *
        //   folderCreatedAfterRevert     - * X
        
        Map<String, StoreEntry> entriesMap = new HashMap<String, StoreEntry>();
        for(StoreEntry se : entries) {
            entriesMap.put(se.getFile().getName(), se);
        }        
        assertNull(entriesMap.get("fileNotInStorage"));        
        
        assertNotNull(entriesMap.get("fileUnchanged"));                
        assertNotNull(entriesMap.get("fileChangedAfterRevert"));       
        assertNotNull(entriesMap.get("fileDeletedAfterRevert"));       
        assertNotNull(entriesMap.get("fileUndeletedBeforeRevert"));    
        assertNotNull(entriesMap.get("fileCreatedAfterRevert"));       
        assertNotNull(entriesMap.get("fileDeletedBeforeRevert"));      
        assertNotNull(entriesMap.get("folderDeletedAfterRevert"));      
        assertNotNull(entriesMap.get("folderCreatedAfterRevert"));      
        
        assertNotNull(entriesMap.get("fileUnchanged").getStoreFile());                
        assertNotNull(entriesMap.get("fileChangedAfterRevert").getStoreFile());       
        assertNotNull(entriesMap.get("fileDeletedAfterRevert").getStoreFile());       
        assertNotNull(entriesMap.get("fileUndeletedBeforeRevert").getStoreFile());         
        assertNotNull(entriesMap.get("folderDeletedAfterRevert").getStoreFile());      
        assertNull(entriesMap.get("fileCreatedAfterRevert").getStoreFile());
        assertNull(entriesMap.get("fileDeletedBeforeRevert").getStoreFile());
        assertNull(entriesMap.get("folderCreatedAfterRevert").getStoreFile());
        
        String strStore = read(entriesMap.get("fileChangedAfterRevert").getStoreFileInputStream(), 1024);
        String strFile = read(new FileInputStream(entriesMap.get("fileChangedAfterRevert").getFile()), 1024);
        assertNotSame(strFile, strStore);
    }     
    
    private long setupFirstFolderToRevert(LocalHistoryStore store, File folder) throws Exception {
        
        File fileNotInStorage = new File(folder, "fileNotInStorage");
        File fileUnchanged = new File(folder, "fileUnchanged");        
        File fileChangedAfterRevert = new File(folder, "fileChangedAfterRevert");        
        File fileDeletedAfterRevert = new File(folder, "fileDeletedAfterRevert");
        File fileDeletedBeforeRevert = new File(folder, "fileDeletedBeforeRevert");
        File fileUndeletedBeforeRevert = new File(folder, "fileUndeletedBeforeRevert");
        File fileCreatedAfterRevert = new File(folder, "fileCreatedAfterRevert");
        
        File folderDeletedAfterRevert = new File(folder, "folderDeletedAfterRevert");
        File folderCreatedAfterRevert = new File(folder, "folderCreatedAfterRevert");        
        
        createFile(store, folder, System.currentTimeMillis(), null);        
        write(fileNotInStorage, "fileNotInStorage".getBytes());        
        createFile(store, fileUnchanged, System.currentTimeMillis(), "fileUnchanged");
        createFile(store, fileChangedAfterRevert, System.currentTimeMillis(), "fileChangedAfterRevert BEFORE change");
        createFile(store, fileDeletedAfterRevert, System.currentTimeMillis(), "fileDeletedAfterRevert BEFORE delete");
        createFile(store, fileDeletedBeforeRevert, System.currentTimeMillis(), "fileDeletedBeforeRevert BEFORE delete");
        createFile(store, fileUndeletedBeforeRevert, System.currentTimeMillis(), "fileUndeletedBeforeRevert");
        
        createFile(store, folderDeletedAfterRevert, System.currentTimeMillis(), null);
        
                
        
        fileDeletedBeforeRevert.delete();
        store.fileDelete(fileDeletedBeforeRevert, System.currentTimeMillis());                                
        
        fileUndeletedBeforeRevert.delete();
        store.fileDelete(fileUndeletedBeforeRevert, System.currentTimeMillis());
        createFile(store, fileUndeletedBeforeRevert, System.currentTimeMillis(), "fileUndeletedBeforeRevert BEFORE revert");
        
        // REVERT
        Thread.sleep(1000); // give me some time
        long revertToTS = System.currentTimeMillis();
        Thread.sleep(1000); // give me some time
        // REVERT
        
        changeFile(store, fileChangedAfterRevert, System.currentTimeMillis(), "fileChanged AFTER change");
                
        fileDeletedAfterRevert.delete();
        store.fileDelete(fileDeletedAfterRevert, System.currentTimeMillis());        
        
        createFile(store, fileDeletedBeforeRevert, System.currentTimeMillis(), "fileDeletedBeforeRevert after delete");
        
        createFile(store, fileCreatedAfterRevert, System.currentTimeMillis(), "fileCreatedAfterRevert");
        
        folderDeletedAfterRevert.delete();
        store.fileDelete(folderDeletedAfterRevert, System.currentTimeMillis());
        
        createFile(store, folderCreatedAfterRevert, System.currentTimeMillis(), null);
        
                
        // check datadir
        assertTrue(folder.exists());
        assertTrue(fileNotInStorage.exists());
        assertTrue(fileUnchanged.exists());
        assertTrue(fileChangedAfterRevert.exists());
        assertTrue(!fileDeletedAfterRevert.exists());
        assertTrue(fileDeletedBeforeRevert.exists());
        assertTrue(fileCreatedAfterRevert.exists());
        assertTrue(!folderDeletedAfterRevert.exists());
        assertTrue(folderCreatedAfterRevert.exists());
        
        File[] files = folder.listFiles();              
        assertEquals(files.length, 7);  //   fileNotInStorage 
                                        //   fileUnchanged 
                                        //   fileChangedAfterRevert 
                                        // X fileDeletedAfterRevert 
                                        //   fileDeletedBeforeRevert     
                                        //   fileUndeletedBeforeRevert 
                                        //   fileCreatedAfterRevert 
                                        //   folderCreatedAfterRevert    
        
        return revertToTS;
    }     
   
    private void assertEntries(StoreEntry[] entries, File[] files, String[] data) throws Exception {
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

    public void testCleanUp() throws Exception { 
        LocalHistoryTestStore store = createStore();
        store.cleanUp(1); 
            
        cleanUpDataFolder();
        
        // XXX 

    }   
    
    public void testGetStoreEntries() throws Exception { 
        LocalHistoryTestStore store = createStore();
        store.cleanUp(1); 
            
        cleanUpDataFolder();
        
        // XXX 

    }   

    public void testDeleteEntry() throws Exception { 
        LocalHistoryTestStore store = createStore();
        store.cleanUp(1); 
            
        cleanUpDataFolder();
        
        // XXX 
        
    }   

    public void testSetLabel() throws Exception { 
        LocalHistoryTestStore store = createStore();
        store.cleanUp(1); 
        
        
        
        cleanUpDataFolder();
        
        // XXX 
        
    }   

    public void testFileDeleteFromMove() throws Exception { 
        LocalHistoryTestStore store = createStore();
        store.cleanUp(1); 
        
        
        
        cleanUpDataFolder();
        
        // XXX 
        
    }       
    
    public void testFileCreateFromMove() throws Exception { 
        LocalHistoryTestStore store = createStore();
        store.cleanUp(1); 
        
        cleanUpDataFolder();
        
        // XXX 
        
    }       

    public void testMove() throws Exception { 
        LocalHistoryTestStore store = createStore();
        store.cleanUp(1); 
        
        cleanUpDataFolder();
        
        // XXX 
        
    }   

    public void testLocalHistoryStoreTested() throws Exception {         
        Set<String> notTestedMethods = new HashSet<String>(2);
        notTestedMethods.add("addPropertyChangeListener");
        notTestedMethods.add("removePropertyChangeListener");
                        
        Method[] methods = LocalHistoryStore.class.getMethods();
        for(Method method : methods) {
            if(!notTestedMethods.contains(method.getName())) {
                try {
                    this.getClass().getMethod("test" + method.getName().substring(0, 1).toUpperCase() + method.getName().substring(1), new Class[] {});
                } catch(NoSuchMethodException e) {
                    fail("Missing test for method " + method.getName());
                }                
            }            
        }        
    }
    
    private LocalHistoryTestStore createStore() {
        return new LocalHistoryTestStore(getDataDir().getAbsolutePath());
    }
    
    private void cleanUpDataFolder() {
        FileUtils.deleteRecursively(getDataDir());
    }
 

    private void createFile(LocalHistoryStore store, File file, long ts, String data) throws Exception {        
        if(data != null) {
            write(file, data.getBytes());
        } else {
            file.mkdirs();
        }
        store.fileCreate(file, ts);        
    }
    
    private void changeFile(LocalHistoryStore store, File file, long ts, String data) throws Exception {        
        write(file, data.getBytes());        
        store.fileChange(file, ts);        
    }
    
    private String read(InputStream is, int length) throws Exception {        
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
    
    private void write(File file, byte[] data) throws Exception {
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

    private void assertDataInStream(InputStream is, byte[] data) throws Exception {        
        BufferedInputStream bis = new BufferedInputStream(is);
        try {
            byte[] contents = new byte[data.length];
            int l = bis.read(contents);
            assertEquals(l, data.length);
            for (int i = 0; i < contents.length; i++) {
                assertEquals(contents[i], data[i]);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (bis != null) {
                bis.close();
            }
        }
    }

    private void assertDataInFile(File file, byte[] data) throws Exception {
        assertDataInStream(new FileInputStream(file), data);
    }
    
}
