/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
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
import junit.framework.*;

public class MIMEResolverImplTest extends TestCase {

    List resolvers = new ArrayList();
           
    public MIMEResolverImplTest(java.lang.String testName) throws Exception {
        super(testName);

        
        URL u = getClass().getProtectionDomain().getCodeSource().getLocation();
        u = new URL(u, "org/netbeans/core/filesystems/code-fs.xml");
        FileSystem fs = new XMLFileSystem(u);
        
        FileObject root = fs.getRoot().getFileObject("root");
        root.refresh();
        
        FileObject fos[] = root.getChildren();
        for (int i = 0; i<fos.length; i++) {
            resolvers.add(createResolver(fos[i]));
        }
        
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(MIMEResolverImplTest.class);
        
        return suite;
    }
    
    
    private MIMEResolver createResolver(FileObject fo) throws Exception {
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
        
        URL u = getClass().getProtectionDomain().getCodeSource().getLocation();
        u = new URL(u, "org/netbeans/core/filesystems/data-fs.xml");
        FileSystem fs = new XMLFileSystem(u);
        
        Object tl1 = new Object();
        Object tl2 = new Object();
        
        FileObject root = fs.getRoot().getFileObject("root");
        root.refresh();
        
        TestThread t1 = new TestThread(root, tl1);
        TestThread t2 = new TestThread(root, tl2);

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
        FileObject root;
        String fail;
        
        private TestThread(FileObject root, Object lock) {
            this.lock = lock;
            this.root = root;
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
        
}
