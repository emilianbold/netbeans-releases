/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.filesystems;

import java.io.*;
import java.util.*;
import java.net.URL;
import org.xml.sax.*;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.*;
import org.openide.execution.NbfsURLConnection;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem; // override java.io.FileSystem
import org.openide.util.*;
import org.openide.util.lookup.*;
import org.openide.xml.*;
import org.openide.*;
//import junit.framework.*;
import org.netbeans.junit.*;

public class MIMEResolverImplTest extends NbTestCase {

    List resolvers;
    FileObject root;
           
    public MIMEResolverImplTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        URL u = getClass().getProtectionDomain().getCodeSource().getLocation();
        u = new URL(u, "org/netbeans/core/filesystems/code-fs.xml");
        FileSystem fs = new XMLFileSystem(u);
        
        FileObject coderoot = fs.getRoot().getFileObject("root");
        coderoot.refresh();
        
        FileObject fos[] = coderoot.getChildren();
        resolvers = new ArrayList();
        for (int i = 0; i<fos.length; i++) {
            resolvers.add(createResolver(fos[i]));
        }
        
        u = getClass().getProtectionDomain().getCodeSource().getLocation();
        u = new URL(u, "org/netbeans/core/filesystems/data-fs.xml");
        fs = new XMLFileSystem(u);
        
        root = fs.getRoot().getFileObject("root");
        root.refresh();
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTest suite() {
        NbTestSuite suite = new NbTestSuite(MIMEResolverImplTest.class);
        
        return suite;
    }
    
    
    private static MIMEResolver createResolver(FileObject fo) throws Exception {
        if (fo == null) throw new NullPointerException();
        return new MIMEResolverImpl.Impl(fo);
    }

    private String resolve(FileObject fo) {
        Iterator it = resolvers.iterator();
        while (it.hasNext()) {
            MIMEResolver r = (MIMEResolver) it.next();
            String s = r.findMIMEType(fo);
            if (s != null) return s;
        }
        return null;
    }
    
    public void testDeclarativeMIME() throws Exception {
        
        Object tl1 = new Object();
        Object tl2 = new Object();
        
        TestThread t1 = new TestThread(tl1);
        TestThread t2 = new TestThread(tl2);

        // call resolver from two threads
        
        t1.start();
        t2.start();
        Thread.currentThread().join(100);
        synchronized (tl1) {tl1.notify();}
        synchronized (tl2) {tl2.notify();}

 
        t1.join(5000);
        t2.join(5000);
        
        if (t1.fail != null) fail(t1.fail);

        if (t2.fail != null) fail(t2.fail);
    }

    private class TestThread extends Thread {
        
        Object lock;
        String fail;
        
        private TestThread(Object lock) {
            this.lock = lock;
        }
        
        public void run() {
            String s;
            FileObject fo = null;
            
            fo = root.getFileObject("test","elf");
            s = resolve(fo);
            if ("magic-mask.xml".equals(s) == false) fail = "magic-mask rule failure: " + fo + " => " + s;
            
            fo = root.getFileObject("test","exe");
            s = resolve(fo);
            if ("magic.xml".equals(s) == false) fail = "magic rule failure: " + fo + " => " + s;

            fo = root.getFileObject("root","xml");
            s = resolve(fo);
            if ("root.xml".equals(s) == false) fail = "root rule failure" + fo + " => " + s;

            fo = root.getFileObject("ns","xml");
            s = resolve(fo);
            if ("ns.xml".equals(s) == false) fail = "ns rule failure"  + fo + " => " + s;

            try {
                synchronized (lock) {
                    lock.wait(5000);  // switch threads here
                }
            } catch (Exception ex) {
                //
            }
            
            fo = root.getFileObject("empty","dtd");
            s = resolve(fo);
            if (null != s) fail = "null rule failure"  + fo + " => " + s;

            fo = root.getFileObject("pid","xml");
            s = resolve(fo);
            if ("pid.xml".equals(s) == false) fail = "pid rule failure"  + fo + " => " + s;
                        
        }
    }
    
    /** See #15672.
     * @author Jesse Glick
     */
    public void testParseFailures() {
        assertEquals("build1.xml recognized as Ant script", "text/x-ant+xml", resolve(root.getFileObject("build1", "xml")));
        assertEquals("bogus.xml not recognized as anything", null, resolve(root.getFileObject("bogus", "xml")));
        assertEquals("build2.xml recognized as Ant script", "text/x-ant+xml", resolve(root.getFileObject("build2", "xml")));
    }
        
}
