/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * OutputDocumentTest.java
 * JUnit based test
 *
 * Created on March 23, 2004, 5:34 PM
 */

package org.netbeans.core.output2;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.nio.*;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TooManyListenersException;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.*;
import junit.framework.*;

/**
 *
 * @author tim
 */
public class StorageTest extends TestCase {
    
    public StorageTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(StorageTest.class);
        return suite;
    }

    Storage filemap = null;
    Storage heap = null;
    protected void setUp() throws java.lang.Exception {
        filemap = new FileMapStorage();
        heap = new HeapStorage();
    }
    
    protected void tearDown() throws Exception {
        filemap.dispose();
        heap.dispose();
    }
    
    public void testIsClosed() throws Exception {
        doTestIsClosed(heap);
        doTestIsClosed(filemap);
    }
    
    private void doTestIsClosed (Storage storage) throws Exception {
        System.out.println("testIsClosed - " + storage.getClass());
        assertTrue (storage.isClosed());
        
        String test = "Hello world";
        storage.write(ByteBuffer.wrap(test.getBytes()), true);
        
        assertFalse (storage.isClosed());
        
        storage.close();
        assertTrue (storage.isClosed());
        
        write (storage, test);
        assertFalse (storage.isClosed());
        
        storage.close();
        assertTrue (storage.isClosed());
        
    }
                        
    private int write (Storage storage, String s) throws Exception {
        ByteBuffer buf = storage.getWriteBuffer(AbstractLines.toByteIndex(s.length()));
        buf.asCharBuffer().put(s);
        buf.position (buf.position() + AbstractLines.toByteIndex(s.length()));
        int result = storage.write(buf, true);
        storage.flush();
        return result;
    }
    
    
    public void testIdenticalBehaviors() throws Exception {
        String[] s = new String[10];
        String a = "abcd";
        String b = a;
        for (int i=0; i < s.length; i++) {
            s[i] = b;
            b += a;
            int hwrite = write (heap, s[i]);
            int fwrite = write (filemap, s[i]);
            assertEquals (hwrite, fwrite);
            assertEquals(heap.isClosed(), filemap.isClosed());
            assertEquals(heap.size(), filemap.size());
            ByteBuffer hbuf = heap.getReadBuffer(hwrite, heap.size() - hwrite);
            ByteBuffer fbuf = filemap.getReadBuffer(hwrite, filemap.size() - fwrite);
        }
    }
    
    public void testFileMapStorageCanBeAsLargeAsIntegerMaxValue() {
        System.out.println("testFileMapStorageCanBeAsLargeAsIntegerMaxValue - THIS TEST WILL CREATE A 2 GIGABYTE TEMP FILE!!!!");
        if (true) {
            System.out.println("Wisely skipping this test");
            return;
        }
        char[] c = new char[16384];
        Arrays.fill (c, 'a');
        String s = new String(c);
        try {
            while (filemap.size() < Integer.MAX_VALUE) {
                 write (filemap, s);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail ("Could not create a large file - " + e.getMessage());
        }
    }
    
    public void testOutputWriterUsesHeapStorageWithLowMemoryFlagSet() throws Exception {
        System.out.println("testOutputWriterUsesHeapStorageWithLowMemoryFlagSet");
        boolean old = OutWriter.lowDiskSpace;
        OutWriter.lowDiskSpace = true;
        OutWriter ow = new OutWriter ();
        try {
            ow.println("Foo");
            assertTrue (ow.getStorage() instanceof HeapStorage);
        } finally {
            ow.dispose();
            OutWriter.lowDiskSpace = old;
        }
    }
    
}
