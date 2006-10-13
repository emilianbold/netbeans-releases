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

package org.netbeans.modules.openfile;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.modules.openfile.RecentFiles.HistoryItem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.Lookups;
import org.openide.windows.CloneableTopComponent;


/**
 * Tests for RecentFiles support, tests list modification policy.
 *
 * @author Dafe Simonek
 */
public class RecentFilesTest extends TestCase {
    
    public RecentFilesTest(String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(RecentFilesTest.class);
    }
    
    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testGetRecentFiles () throws Exception {
        System.out.println("Testing RecentFiles.getRecentFiles...");
        URL url = RecentFilesTest.class.getResource("resources/recent_files/");
        assertNotNull("url not found.", url);
        
        FileObject folder = URLMapper.findFileObject(url);
        FileObject[] files = folder.getChildren();
        List<EditorLikeTC> tcs = new ArrayList<EditorLikeTC>();
        
        RecentFiles.getPrefs().clear();
        RecentFiles.init();
        
        for (FileObject curFo : files) {
            EditorLikeTC curTC = new EditorLikeTC(curFo);
            tcs.add(0, curTC);
            curTC.open();
        }

        // close top components and check if they were added correctly to
        // recent files list
        for (EditorLikeTC curTC : tcs) {
            curTC.close();
        }
        int i = 0;
        List<HistoryItem> recentFiles = RecentFiles.getRecentFiles();
        assertTrue("Expected " + files.length + " recent files, got " + recentFiles.size(), files.length == recentFiles.size());
        for (FileObject fo : files) {
            assertEquals(fo, recentFiles.get(i).getFile());
            i++;
        }

        // reopen first component again and check that it was removed from
        // recent files list
        tcs.get(0).open();
        recentFiles = RecentFiles.getRecentFiles();
        assertTrue(files.length == (recentFiles.size() + 1));
        
    }
    
    public void testPersistence () throws Exception {
        System.out.println("Testing perfistence of recent files history...");
        URL url = RecentFilesTest.class.getResource("resources/recent_files/");
        assertNotNull("url not found.", url);
        
        FileObject folder = URLMapper.findFileObject(url);
        FileObject[] files = folder.getChildren();
        
        RecentFiles.getPrefs().clear();
        
        // store, load and check for equality
        for (FileObject file : files) {
            HistoryItem hItem = new HistoryItem(file, System.currentTimeMillis());
            RecentFiles.storeAdded(hItem);
            Thread.sleep(100);
        }
        List<HistoryItem> loaded = RecentFiles.load();
        assertTrue("Persistence failed, " + files.length + " stored items, " + loaded.size() + " loaded.", files.length == loaded.size());
        int i = files.length - 1;
        for (FileObject fileObject : files) {
            assertTrue("File #" + (i + 1) + " differs", fileObject.equals(loaded.get(i--).getFile()));
        }
    }

    /** Special TopComponent subclass which imitates TopComponents used for documents, editors */
    private static class EditorLikeTC extends CloneableTopComponent {
        
        public EditorLikeTC (FileObject fo) throws Exception {
            DataObject dObj = DataObject.find(fo);
            associateLookup(Lookups.singleton(dObj));
        }
        
    }

}
