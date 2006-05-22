/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.openide.loaders;

import java.util.logging.Level;
import junit.framework.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.ref.Reference;
import java.io.IOException;
import java.util.*;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager;
import org.openide.util.datatransfer.*;
import org.openide.filesystems.*;
import org.openide.util.*;

/** Tests for internals of FolderList as there seems to be some
 * inherent problems.
 *
 * @author Jaroslav Tulach
 */
public class FolderListTest extends NbTestCase {
    private FileObject folder;
    private FolderList list;
    
    
    public FolderListTest(String testName) {
        super(testName);
    }
    
    protected Level logLevel() {
        return Level.FINE;
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        
        folder = FileUtil.createFolder(lfs.getRoot(), "folder");

        FileUtil.createData(folder, "A.txt");
        FileUtil.createData(folder, "B.txt");
        FileUtil.createData(folder, "C.txt");
        
        list = FolderList.find(folder, true);
    }

    protected void tearDown() throws Exception {
    }

    public void testComputeChildrenList() throws Exception {
        class L implements FolderListListener {
            private int cnt;
            private boolean finished;
            
            public void process(DataObject obj, List arr) {
                cnt++;
            }

            public void finished(List arr) {
                assertTrue(arr.isEmpty());
                finished = true;
            }
        }
        
        L listener = new L();       
        RequestProcessor.Task t = list.computeChildrenList(listener);
        t.waitFinished();
        
        assertEquals("Three files", 3, listener.cnt);
        assertTrue("finished", listener.finished);
    }

    public void testComputeChildrenListKeepsTheObject() throws Exception {
        class L implements FolderListListener {
            private HashSet hashes = new HashSet();
            private boolean check;

            public void process(DataObject obj, List arr) {
                if (!check) {
                    hashes.add(new Integer(obj.hashCode()));
                } else {
                    Integer i = new Integer(obj.hashCode());
                    hashes.remove(i);
                }

            }

            public void finished(List arr) {
                check = true;
            }
        }

        L listener = null;


        for(int j = 0; j < 100; j++) {
            DataObject[] objArr = list.getChildren();
            FileObject[] arr = new FileObject[objArr.length];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = objArr[i].getPrimaryFile();
            }
            for (int i = 0; i < objArr.length; i++) {
                WeakReference r = new WeakReference(objArr[i]);
                objArr[i] = null;
                assertGC(i + "-th can GC", r);
            }

            listener = new L();
            RequestProcessor.Task t = list.computeChildrenList(listener);
            t.waitFinished();

            // if GC kicks in here, the objects can go away
            for (int i = 0; i < 10; i++) {
                System.gc();
            }


            t = list.computeChildrenList(listener);
            t.waitFinished();


            if (listener.hashes.isEmpty()) {
                break;
            }

            // make some room so soft references have a place to stay
            byte[] tmp = new byte[10000 * j];
            tmp = null;
        }



        if (!listener.hashes.isEmpty()) {
            fail("There is new data object created, which is not in " + listener.hashes);
        }
        
        assertEquals("Was checking", true, listener.check);
    }

    public void testComputeChildrenListKeepsTheObjectDuringTheFirstRunAsWell() throws Exception {
        class L implements FolderListListener {
            private HashSet hashes = new HashSet();
            private boolean check;
            
            public void process(DataObject obj, List arr) {
                if (!check) {
                    hashes.add(new Integer(obj.hashCode()));
                } else {
                    Integer i = new Integer(obj.hashCode());
                    hashes.remove(i);
                }
                
            }

            public void finished(List arr) {
                check = true;
            }
        }
        
        L listener = null;

        for(int i = 0; i < 100; i++) {
            listener = new L();
            RequestProcessor.Task t = list.computeChildrenList(listener);
            t.waitFinished();

            // if GC kicks in here, the objects can go away
            for (int j = 0; j < 10; j++) {
                System.gc();
            }


            t = list.computeChildrenList(listener);
            t.waitFinished();

            if (listener.hashes.isEmpty()) {
                break;
            }

            // make some room so soft references have a place to stay
            byte[] tmp = new byte[10000 * i];
            tmp = null;
        }
        
        
        if (!listener.hashes.isEmpty()) {
            fail("There is new data object created, which is not in " + listener.hashes);
        }
        
        
        assertEquals("Was checking", true, listener.check);
    }
    
}
