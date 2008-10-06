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

package org.openide.filesystems;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Enumeration;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Jaroslav Tulach, Radek Matous
 */
public class MIMESupportTest extends NbTestCase {
    private TestLookup lookup;
    public MIMESupportTest(String testName) {
        super(testName);
    }

    static {
        System.setProperty("org.openide.util.Lookup", MIMESupportTest.TestLookup.class.getName());
        assertEquals(MIMESupportTest.TestLookup.class, Lookup.getDefault().getClass());
        
    }
    
    protected @Override void setUp() throws Exception {
        lookup = (MIMESupportTest.TestLookup)Lookup.getDefault();
        lookup.init();
    }

    public void testFindMIMETypeCanBeGarbageCollected() throws IOException {
        FileObject fo = FileUtil.createData(FileUtil.createMemoryFileSystem().getRoot(), "Ahoj.bla");
        
        String expResult = "content/unknown";
        String result = FileUtil.getMIMEType(fo);
        assertEquals("some content found", expResult, result);
        
        WeakReference<FileObject> r = new WeakReference<FileObject>(fo);
        fo = null;
        assertGC("Can be GCed", r);
    }
    
    public void testBehaviourWhemLookupResultIsChanging() throws Exception {
        MIMESupportTest.TestResolver testR = new MIMESupportTest.TestResolver("a/a");
        assertTrue(Lookup.getDefault().lookupAll(MIMEResolver.class).isEmpty());
        
        FileObject fo = FileUtil.createData(FileUtil.createMemoryFileSystem().getRoot(), "mysterious.lenka");
        
        assertEquals("content/unknown",fo.getMIMEType());
        
        lookup.setLookups(testR);
        assertTrue(Lookup.getDefault().lookupAll(MIMEResolver.class).contains(testR));        
        assertEquals(testR.getMime(),fo.getMIMEType());
        
        testR = new MIMESupportTest.TestResolver("b/b");
        lookup.setLookups(testR);
        assertTrue(Lookup.getDefault().lookupAll(MIMEResolver.class).contains(testR));        
        assertEquals(testR.getMime(),fo.getMIMEType());
    }

    public void testUnreadableFiles() throws Exception {
        MIMESupportTest.TestResolver testR = new MIMESupportTest.TestResolver("a/a");
        assertTrue(Lookup.getDefault().lookupAll(MIMEResolver.class).isEmpty());
        lookup.setLookups(testR);
        assertTrue(Lookup.getDefault().lookupAll(MIMEResolver.class).contains(testR));        
        AbstractFileSystem afs = new AbstractFileSystem() {
            @Override
            public String getDisplayName() {
                return "";
            }
            @Override
            public boolean isReadOnly() {
                return false;
            }
            @Override
            protected boolean canRead(String name) {
                return !name.equals("f");
            }
        };
        afs.list = new AbstractFileSystem.List() {
            public String[] children(String f) {
                if (f.equals("")) {
                    return new String[] {"f"};
                } else {
                    return null;
                }
            }
        };
        afs.info = new AbstractFileSystem.Info() {
            public Date lastModified(String name) {
                return null;
            }
            public boolean folder(String name) {
                return name.equals("");
            }
            public boolean readOnly(String name) {
                return false;
            }
            public String mimeType(String name) {
                return null;
            }
            public long size(String name) {
                return 0;
            }
            public InputStream inputStream(String name) throws FileNotFoundException {
                throw new FileNotFoundException();
            }
            public OutputStream outputStream(String name) throws IOException {
                throw new IOException();
            }
            public void lock(String name) throws IOException {}
            public void unlock(String name) {}
            public void markUnimportant(String name) {}
        };
        afs.attr = new AbstractFileSystem.Attr() {
            public Object readAttribute(String name, String attrName) {
                return null;
            }
            public void writeAttribute(String name, String attrName, Object value) throws IOException {}
            public Enumeration<String> attributes(String name) {
                return Enumerations.empty();
            }
            public void renameAttributes(String oldName, String newName) {}
            public void deleteAttributes(String name) {}
        };
        FileObject fo = afs.findResource("f");
        assertNotNull(fo);
        assertFalse(fo.canRead());
        assertEquals("unreadable", fo.getMIMEType());
    }

    private class TestResolver extends MIMEResolver {
        private String mime;
        private TestResolver(String mime) {            
            this.mime = mime;
        }
        
        public String findMIMEType(FileObject fo) {
            if (fo.canRead()) {
                return mime;
            } else {
                return "unreadable";
            }
        }        
        
        private String getMime() {
            return mime;
        }
    }
    
    public void testDeclarativeMIMEResolvers() throws Exception {
        FileObject resolver = FileUtil.createData(Repository.getDefault().getDefaultFileSystem().getRoot(), "Services/MIMEResolver/r.xml");
        resolver.setAttribute("position", 2);
        OutputStream os = resolver.getOutputStream();
        PrintStream ps = new PrintStream(os);
        ps.println("<!DOCTYPE MIME-resolver PUBLIC '-//NetBeans//DTD MIME Resolver 1.0//EN' 'http://www.netbeans.org/dtds/mime-resolver-1_0.dtd'>");
        ps.println("<MIME-resolver>");
        ps.println(" <file>");
        ps.println("  <ext name='foo'/>");
        ps.println("  <resolver mime='text/x-foo'/>");
        ps.println(" </file>");
        ps.println("</MIME-resolver>");
        os.close();
        FileObject foo = FileUtil.createMemoryFileSystem().getRoot().createData("x.foo");
        assertEquals("text/x-foo", foo.getMIMEType());
        // Test changing a resolver:
        os = resolver.getOutputStream();
        ps = new PrintStream(os);
        ps.println("<!DOCTYPE MIME-resolver PUBLIC '-//NetBeans//DTD MIME Resolver 1.0//EN' 'http://www.netbeans.org/dtds/mime-resolver-1_0.dtd'>");
        ps.println("<MIME-resolver>");
        ps.println(" <file>");
        ps.println("  <ext name='foo'/>");
        ps.println("  <resolver mime='text/x-foo2'/>");
        ps.println(" </file>");
        ps.println("</MIME-resolver>");
        os.close();
        foo = FileUtil.createMemoryFileSystem().getRoot().createData("x2.foo");
        assertEquals("text/x-foo2", foo.getMIMEType());
        // Test adding a resolver:
        resolver = FileUtil.createData(Repository.getDefault().getDefaultFileSystem().getRoot(), "Services/MIMEResolver/r2.xml");
        resolver.setAttribute("position", 1);
        os = resolver.getOutputStream();
        ps = new PrintStream(os);
        ps.println("<!DOCTYPE MIME-resolver PUBLIC '-//NetBeans//DTD MIME Resolver 1.0//EN' 'http://www.netbeans.org/dtds/mime-resolver-1_0.dtd'>");
        ps.println("<MIME-resolver>");
        ps.println(" <file>");
        ps.println("  <ext name='foo'/>");
        ps.println("  <resolver mime='text/x-foo3'/>");
        ps.println(" </file>");
        ps.println("</MIME-resolver>");
        os.close();
        foo = FileUtil.createMemoryFileSystem().getRoot().createData("x3.foo");
        assertEquals("text/x-foo3", foo.getMIMEType());
        // Test removing a resolver:
        resolver.delete();
        foo = FileUtil.createMemoryFileSystem().getRoot().createData("x4.foo");
        assertEquals("text/x-foo2", foo.getMIMEType());
    }

    public static class TestLookup extends ProxyLookup {
        public TestLookup() {
            super();
            init();
        }
        
        private void init() {
            setLookups(new Lookup[] {});
        }
        
        private void setLookups(Object instance) {
            setLookups(new Lookup[] {getInstanceLookup(instance)});
        }
        
        private Lookup getInstanceLookup(final Object instance) {
            InstanceContent instanceContent = new InstanceContent();
            instanceContent.add(instance);
            Lookup instanceLookup = new AbstractLookup(instanceContent);
            return instanceLookup;
        }        
    }    
    
}
