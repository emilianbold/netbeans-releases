/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.settings;

import java.io.IOException;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import junit.textui.TestRunner;

import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;

/** JUnit tests
 *
 * @author  Jan Pokorsky
 */
public final class ScheduledRequestTest extends NbTestCase {
    FileSystem fs;
    
    /** Creates a new instance of ScheduledRequestTest */
    public ScheduledRequestTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(ScheduledRequestTest.class));
        System.exit(0);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        LocalFileSystem lfs = new LocalFileSystem();
        clearWorkDir();
        lfs.setRootDirectory(this.getWorkDir());
        fs = lfs;
    }
    
    public void testSchedule() throws Exception {
        FSA toRun = new FSA();
        FileObject fo = fs.getRoot();
        ScheduledRequest sr = new ScheduledRequest(fo, toRun);
        Object obj1 = new Object();
        sr.schedule(obj1);
        assertNotNull("none file lock", sr.getFileLock());
        for (int i = 0; i < 2 && !toRun.finished; i++) {
            Thread.sleep(2500);    
        }
        assertTrue("scheduled request was not performed yet", toRun.finished);        
        assertNull("file is still locked", sr.getFileLock());
    }
    
    public void testCancel() throws Exception {
        FSA toRun = new FSA();
        FileObject fo = fs.getRoot();
        ScheduledRequest sr = new ScheduledRequest(fo, toRun);
        Object obj1 = new Object();
        sr.schedule(obj1);
        assertNotNull("none file lock", sr.getFileLock());
        sr.cancel();
        assertNull("file lock", sr.getFileLock());
        Thread.sleep(2500);
        assertTrue("scheduled request was performed", !toRun.finished);
        
        Object obj2 = new Object();
        sr.schedule(obj2);
        assertNotNull("none file lock", sr.getFileLock());
        Thread.sleep(2500);
        assertNull("file lock", sr.getFileLock());
        assertTrue("scheduled request was not performed yet", toRun.finished);
    }
    
    public void testForceToFinish() throws Exception {
        FSA toRun = new FSA();
        FileObject fo = fs.getRoot();
        ScheduledRequest sr = new ScheduledRequest(fo, toRun);
        Object obj1 = new Object();
        sr.schedule(obj1);
        assertNotNull("none file lock", sr.getFileLock());
        sr.forceToFinish();
        assertTrue("scheduled request was not performed yet", toRun.finished);
        assertNull("file lock", sr.getFileLock());
    }
    
    public void testRunAndWait() throws Exception {
        FSA toRun = new FSA();
        FileObject fo = fs.getRoot();
        ScheduledRequest sr = new ScheduledRequest(fo, toRun);
        sr.runAndWait();
        assertTrue("scheduled request was not performed yet", toRun.finished);
        assertNull("file lock", sr.getFileLock());
    }
    
    private static class FSA implements org.openide.filesystems.FileSystem.AtomicAction {
        boolean finished = false;
        public void run() throws IOException {
            finished = true;
        }
        
    }
}
