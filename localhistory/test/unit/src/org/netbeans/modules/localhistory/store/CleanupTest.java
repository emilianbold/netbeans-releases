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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;

/**
 *
 * @author tomas
 */
public class CleanupTest extends LHTestCase {

    public CleanupTest(String testName) {
        super(testName);      
    }
    
    public void xtestCleanUp() throws Exception { 
        LocalHistoryTestStore store = createStore();
        File folder = new File(getDataDir(), "datafolder");        
        folder.mkdirs();
        
        // create the files
        File file1 = new File(folder, "file1");
        File file2 = new File(folder, "file2");
        File file3 = new File(folder, "file3");
        File file4 = new File(folder, "file4");
        File file5 = new File(folder, "file5");
        File file6 = new File(folder, "file6");
        
        // lets get some history
        
        // 4 days ago
        long ts = System.currentTimeMillis() - 4 * 24 * 60 * 60 * 1000;
        createFile(store, folder, ts, null);
        createFile(store, file1, ts, "data1");
        createFile(store, file2, ts, "data2");
        createFile(store, file3, ts, "data3");
        createFile(store, file4, ts, "data4");
        store.setLabel(file1, ts, "dil");                
        store.setLabel(file2, ts, "dil2");                
        
        long tsCreateFile5 = ts; 
        createFile(store, file5, tsCreateFile5, "data5"); // this one will get deleted by cleanup
                
        // 2 days ago
        ts = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000;
        changeFile(store, file1, ts, "data1.1");
        changeFile(store, file2, ts, "data2.1");
        changeFile(store, file3, ts, "data3.1");
        changeFile(store, file4, ts, "data4.1");
        
        long tsCreateFile6 = ts;
        createFile(store, file6, tsCreateFile6, "data6");
        
        long tsLabelFile2 = ts; 
        String labelFile2 = "dil2.1";
        store.setLabel(file2, tsLabelFile2, labelFile2);  // two labels - each timestamp got one               
        
        store.setLabel(file3, ts, "dil3");                
        
        // check the files created in storage
        assertFile(file1, store, ts,    -1, 4, 2, "data1.1", TOUCHED);
        assertFile(file2, store, ts,    -1, 4, 2, "data2.1", TOUCHED);
        assertFile(file3, store, ts,    -1, 4, 2, "data3.1", TOUCHED);
        assertFile(file4, store, ts,    -1, 3, 2, "data4.1", TOUCHED);
        assertFile(file5, store, tsCreateFile5, -1, 2, 2, "data5",   TOUCHED);
//        assertFile(file6, store, tsCreateFile6, -1, 2, 2, "data6",   TOUCHED);
        
        // run clean up - time to live = 3 days 
        long ttl = 3 * 24 * 60 * 60 * 1000;
        store.cleanUp(ttl); 
        
        // check the cleaned storage
        assertFile(file1, store, ts, -1, 2, 2, "data1.1", TOUCHED);
        assertFile(file2, store, ts, -1, 3, 2, "data2.1", TOUCHED);
        assertFile(file3, store, ts, -1, 3, 2, "data3.1", TOUCHED);
        assertFile(file4, store, ts, -1, 2, 2, "data4.1", TOUCHED);
        
        // check labels for file2 - the first one should be deleted
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeLong(tsLabelFile2);
        dos.writeInt(labelFile2.length());
        dos.writeChars(labelFile2);        
        dos.flush();
        
        File labelsFile = store.getLabelsFile(file2);
        assertDataInFile(labelsFile, baos.toByteArray());
                
        // check the history for folder - should contain only 1 file created 2 days ago
        baos = new ByteArrayOutputStream();
        dos = new DataOutputStream(baos);
        dos.writeLong(tsCreateFile6);
        dos.writeInt(0);
        dos.writeChars("");                
        dos.writeInt(file6.getAbsolutePath().length());
        dos.writeChars(file6.getAbsolutePath());                
        dos.writeInt(TOUCHED);
        dos.flush();
        
        File historyFile = store.getHistoryFile(folder);
        
        assertDataInFile(historyFile, baos.toByteArray());
        dos.close();
    }                   

    public void testCleanUpAll() throws Exception {
        LocalHistoryTestStore store = createStore();
        File folder = new File(getDataDir(), "datafolder");        
        folder.mkdirs();
        
        // create the files
        File file1 = new File(folder, "file1");
        File file2 = new File(folder, "file2");        

        // CREATE HISTORY
        // 4 days ago
        long ts = System.currentTimeMillis() - 4 * 24 * 60 * 60 * 1000;
        createFile(store, file1, ts, "data1");
        createFile(store, file2, ts, "data2");        

        // check the files created in storage
        assertFile(file1, store, ts,    -1, 2, 1, "data1", TOUCHED);
        assertFile(file2, store, ts,    -1, 2, 1, "data2", TOUCHED);
                
        // run clean up - time to live = 3 days 
        long ttl = 3 * 24 * 60 * 60 * 1000;
        store.cleanUp(ttl); 

        // storage is EMPTY
        File storage = new File(new File(getDataDir(), "var"), "filehistory");
        assertEquals(1, storage.list().length);
    }
    
    
    
    public void testCleanUp() throws Exception {
        LocalHistoryTestStore store = createStore();
        File folder = new File(getDataDir(), "datafolder");        
        folder.mkdirs();
        
        // create the files
        File file1 = new File(folder, "file1");
        File file2 = new File(folder, "file2");        

        // CREATE HISTORY
        // 4 days ago
        long ts = System.currentTimeMillis() - 4 * 24 * 60 * 60 * 1000;
        createFile(store, file1, ts, "data1");
        createFile(store, file2, ts, "data2");        

        // 2 days ago
        ts = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000;
        changeFile(store, file1, ts, "data1.1");
        changeFile(store, file2, ts, "data2.1");        
        
        // check the files created in storage
        assertFile(file1, store, ts, -1, 3, 1, "data1.1", TOUCHED);
        assertFile(file2, store, ts, -1, 3, 1, "data2.1", TOUCHED);
                
        // run clean up - time to live = 3 days 
        long ttl = 3 * 24 * 60 * 60 * 1000;
        store.cleanUp(ttl); 

        // check the files after the cleanup
        // the versions data1 and data2 are to be deleted
        assertFile(file1, store, ts, -1, 2, 0, "data1.1", TOUCHED);
        assertFile(file2, store, ts, -1, 2, 0, "data2.1", TOUCHED);
        
        
        
    }    
    
    
    
}




















