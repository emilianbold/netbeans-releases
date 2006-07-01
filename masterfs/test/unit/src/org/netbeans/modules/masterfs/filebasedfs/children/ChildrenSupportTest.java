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

package org.netbeans.modules.masterfs.filebasedfs.children;

import junit.framework.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileName;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;

import java.io.File;
import java.util.*;

/**
 *
 * @author Radek Matous
 */
public class ChildrenSupportTest extends NbTestCase {
    ChildrenSupport folderItem;
    File testFile;

    /*Just for testRefresh*/
    private File fbase;
    private File removed1;
    private File removed2;
    private File added1;
    private File added2;
    private FileNaming folderName;

    public ChildrenSupportTest(String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
        testFile = getWorkDir().getParentFile();
        folderName = NamingFactory.fromFile(testFile);
        folderItem = new ChildrenSupport ();
        
        if (getName().startsWith("testRefresh")) {
            fbase = new File (testFile, "testrefresh");
            removed1 = new File (fbase, "removed1/");
            removed2 = new File (fbase, "removed2/");
            added1 = new File (fbase, "added1/");
            added2 = new File (fbase, "added2/");  
            
            assertTrue (testFile.exists());
            assertTrue(testFile.isDirectory());
            if (!fbase.exists()) assertTrue (fbase.getAbsoluteFile().mkdirs());
            if (!removed1.exists())assertTrue (removed1.mkdir());
            if (!removed2.exists())assertTrue (removed2.mkdir());
            if (added1.exists()) assertTrue (added1.delete());
            if (added2.exists()) assertTrue (added2.delete());        
        }
    }


    /**
     * Test of getFolderName method, of class org.netbeans.modules.masterfs.children.FolderPathItems.
     */
    public void testGetFolderItem() throws Exception {
        assertEquals(testFile, folderName.getFile());
    }


    /**
     * Test of getChildren method, of class org.netbeans.modules.masterfs.children.FolderPathItems.
     */
    public void testGetChildrenPathItems() throws Exception{
        Set childItems = folderItem.getChildren(folderName, true);
        List lst = Arrays.asList(testFile.listFiles());
        Iterator it = childItems.iterator();
        while (it.hasNext()) {
            FileNaming pi = (FileNaming)it.next();
            File f = pi.getFile();
            assertTrue (lst.contains(f));
        }
    }

    /**
     * Test of getFileName method, of class org.netbeans.modules.masterfs.children.FolderPathItems.
     */
    public void testGetChildItem() throws Exception{
        folderItem.getChildren(folderName, true);
        List lst = Arrays.asList(testFile.listFiles());
        Iterator it = lst.iterator();
        while (it.hasNext()) {
            File f = (File)it.next();
            FileNaming item = folderItem.getChild(f.getName(), folderName,false);
            assertNotNull(item);
            assertTrue (item.getFile().equals(f));
        }
    }

    public void testRefresh () throws Exception {
        FileNaming fpiName = NamingFactory.fromFile(fbase);
        ChildrenSupport fpi = new ChildrenSupport();
        fpi.getChildren(fpiName, true);
        assertTrue (removed1.delete());
        assertTrue (removed2.delete());
        assertTrue (added1.mkdirs());
        assertTrue (added2.mkdirs());

        List added = Arrays.asList(new String[] {"added1", "added2"});
        List removed = Arrays.asList(new String[] {"removed1", "removed2"});
        
        Map changes = fpi.refresh(fpiName);
        Iterator it = changes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            FileNaming pItem = (FileNaming)entry.getKey();
            Integer type = (Integer)entry.getValue();
            if (type == ChildrenCache.ADDED_CHILD) {
                assertTrue (added.contains(pItem.getName()));
            }
            if (type == ChildrenCache.REMOVED_CHILD) {
                assertTrue (removed.contains(pItem.getName()));
            }            
        }
        assertTrue (changes.size() == 4);        
    }        
    
    public void testRefresh2 () throws Exception {
        FileNaming fpiName = NamingFactory.fromFile(fbase);
        ChildrenSupport fpi = new ChildrenSupport();
        fpi.getChild("removed1", fpiName, false);
        assertTrue (removed1.delete());
        assertTrue (removed2.delete());
        assertTrue (added1.mkdirs());
        assertTrue (added2.mkdirs());
        
        Map changes = fpi.refresh(fpiName);
        Iterator it = changes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            FileNaming pItem = (FileNaming)entry.getKey();
            Integer type = (Integer)entry.getValue();
            assertEquals("removed1", pItem.getName());
        }
        assertTrue (changes.size() == 1);        
    }        
    
}
