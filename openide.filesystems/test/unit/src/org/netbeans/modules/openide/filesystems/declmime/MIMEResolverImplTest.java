/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.openide.filesystems.declmime;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.filesystems.XMLFileSystem;

public class MIMEResolverImplTest extends NbTestCase {
    List<MIMEResolver> resolvers;
    FileObject root;
           
    public MIMEResolverImplTest(String testName) {
        super(testName);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    protected void setUp() throws Exception {
        URL u = this.getClass().getResource ("code-fs.xml");        
        FileSystem fs = new XMLFileSystem(u);
        
        FileObject coderoot = fs.getRoot().getFileObject("root");
        coderoot.refresh();
        
        FileObject fos[] = coderoot.getChildren();
        resolvers = new ArrayList<MIMEResolver>();
        for (int i = 0; i<fos.length; i++) {
            resolvers.add(createResolver(fos[i]));
        }
        
        u = this.getClass().getResource ("data-fs.xml");                
        fs = new XMLFileSystem(u);
        
        root = fs.getRoot().getFileObject("root");
        root.refresh();
        FileUtil.setMIMEType("txt2", "text/plain; charset=us-ascii");        
    }
    
    private static MIMEResolver createResolver(FileObject fo) throws Exception {
        if (fo == null) throw new NullPointerException();
        return MIMEResolverImpl.forDescriptor(fo);
    }

    private String resolve(FileObject fo) {
        for (MIMEResolver r : resolvers) {
            String s = r.findMIMEType(fo);
            if (s != null) return s;
        }
        return null;
    }
    
    public void testMultithreading() throws Exception {
        
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
        
        final Object lock;
        String fail;
        
        private TestThread(Object lock) {
            this.lock = lock;
        }
        
        @Override
        public void run() {
            String s;
            FileObject fo = null;

            fo = root.getFileObject("test","txt2");
            s = resolve(fo);
            if ("mime.xml".equals(s) == false) fail = "mime rule failure: " + fo + " => " + s;            

            fo = root.getFileObject("test","txt3");
            s = resolve(fo);
            if (s != null) fail = "and-mime rule failure: " + fo + " => " + s;            
                        
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
        // see #126496
        assertEquals("NPE at XMLEntityScanner.skipChar not ignored.", null, resolve(root.getFileObject("126496-skipCharNPE", "xml")));
    }
    
    public void testIllegalXMLEncoding() {
        assertEquals("illegal-encoding.xml recognized as a XML file", "text/x-springconfig+xml", resolve(root.getFileObject("illegal-encoding", "xml")));
    }

}
