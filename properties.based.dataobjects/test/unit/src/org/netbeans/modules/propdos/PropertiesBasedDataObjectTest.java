/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.propdos;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Tim Boudreau
 */
public class PropertiesBasedDataObjectTest extends NbTestCase {
    private FileSystem dfs;
    private FileObject file;
    private DataObject o;
    private Properties initialProperties;
    public PropertiesBasedDataObjectTest(String x) {
        super (x);
    }

    @Before
    @Override
    public void setUp() throws IOException, InterruptedException, InvocationTargetException {
        MockLookup.setLayersAndInstances(PropertiesBasedDataObjectTest.class.getClassLoader(),
                new DFS(dfs = FileUtil.createMemoryFileSystem()), new LDR());
        file = dfs.getRoot().createData("Foo.foo");
        initialProperties = new Properties();
        for (char c='a'; c <= 'z'; c++) {
            String key = new String(new char[] { c });
            String val = new String(new char[] { Character.toUpperCase(c) });
            initialProperties.setProperty(key, val);
        }
        assertFalse (initialProperties.isEmpty());
        OutputStream out = file.getOutputStream();
        try {
            initialProperties.store(out, null);
        } finally {
            out.close();
        }
        o = DataObject.find (file);
        assertNotNull(o);
    }

    @Test
    public void testSanity() throws Exception {
        assertTrue( o instanceof Impl );

        Properties ap = new Properties();
        Properties bp = new Properties();
        assertEquals (ap, bp);
        ap.setProperty ("foo", "bar");
        assertFalse (ap.equals(bp));
        assertFalse (bp.equals(ap));
        assertTrue (bp.equals(bp));
        bp.setProperty ("foo", "baz");
        assertFalse (ap.equals(bp));
        bp.setProperty ("foo", "bar");
        assertEquals (ap, bp);
        Properties cp = new Properties (bp);
        assertEquals ("bar", cp.getProperty("foo"));

        Properties p = new Properties ();
        p.putAll(initialProperties);
        assertFalse (p.isEmpty());
        A one = new A (p);
        A two = new A (p);
        assertEquals (one, two);
        A three = new A (new Properties());
        assertNotSame (three, one);
        assertFalse (one + " should not equal " + three, three.equals(one));

        p.setProperty("foo", "bar");
        A four = new A (p);
        boolean equal = one.equals(four);
        assertFalse (equal);
    }

    @Test
    public void testAsProperties() throws Exception {
        PropertiesAdapter adap = o.getLookup().lookup(PropertiesAdapter.class);
        assertNotNull (adap);
        ObservableProperties p = adap.asProperties();
        assertNotNull (p);
        for (char c='a'; c <= 'z'; c++) {
            String key = new String(new char[] { c });
            String val = new String(new char[] { Character.toUpperCase(c) });
            assertEquals (val, p.get(key));
        }
    }

    /*
    public void testFileChanged() throws InterruptedException {
        FCL x = new FCL();
        file.addFileChangeListener(x);
        PropertiesAdapter adap = o.getLookup().lookup(PropertiesAdapter.class);
        ObservableProperties p = adap.asProperties();
        p.setProperty("foo", "mooglewhatzit");
        Date lastModified = file.lastModified();
        x.waitForChange();
        assertTrue (lastModified.before(file.lastModified()));
        assertTrue ("File not written after properties write", x.changed);
        p = adap.asProperties();
        assertEquals ("mooglewhatzit", p.get("foo"));
    }

    public void testObjectInLookupChanged() throws Exception {
        FCL l = new FCL();
        file.addFileChangeListener(l);
        Impl i = (Impl) o;
        A a = i.getLookup().lookup(A.class);
        assertNotNull (a);
        assertEquals (new A(initialProperties), a);
        i.ref();
        A nue = i.getLookup().lookup(A.class);
        assertNotSame (a, nue);
        assertEquals (a, nue);
        Reference<A> r = new WeakReference<A> (a);
        a = null;
        assertGC("Old object not collected", r);
        PropertiesAdapter adap = o.getLookup().lookup(PropertiesAdapter.class);
        ObservableProperties p = adap.asProperties();
        p.setProperty("foo", "baz");
        assertEquals ("baz", p.getProperty("foo"));
        p = o.getLookup().lookup(PropertiesAdapter.class).asProperties();
        assertEquals ("baz", p.getProperty("foo"));
        p.setProperty ("foo", "moo");
        l.waitForChange();
        assertTrue (l.changed);
        A newer = i.getLookup().lookup(A.class);
        assertFalse (new A(initialProperties).equals(newer));
        assertNotSame (nue, newer);
        assertNotSame (a, newer);
        assertEquals ("baz", p.getProperty("foo"));

        Reference<A> ref = new WeakReference<A>(newer);
        a = nue = newer = null;
        assertGC("Object not collected", ref);
        assertEquals ("baz", p.getProperty("foo"));
    }
     */

    @Test
    public void testOnDelete() throws Exception {
        FCL l = new FCL();
        file.addFileChangeListener(l);
        o.delete();
        l.waitForChange();
        assertTrue (((Impl)o).deleted);
    }

    private final class FCL extends FileChangeAdapter {
        private volatile boolean changed;
        @Override
        public void fileChanged(FileEvent fe) {
            changed = true;
            synchronized(this) {
                notifyAll();
            }
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            fileChanged(fe);
        }

        public void waitForChange() throws InterruptedException {
            int ct = 0;
            while (!changed && ct++ < 20) {
                Thread.sleep(200);
                synchronized (this) {
                    wait (100);
                }
            }
        }
    }

    public final class LDR extends MultiFileLoader {
        LDR() {
            super (Impl.class.getName());
        }

        @Override
        protected FileObject findPrimaryFile(FileObject fo) {
            return fo;
        }

        @Override
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            if ("foo".equals(primaryFile.getExt())) {
                try {
                    return new Impl(primaryFile, this);
                } catch (Exception ex) {
                    throw new RuntimeException (ex);
                }
            }
            return null;
        }

        @Override
        protected Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return ((Impl) obj).entry();
        }

        @Override
        protected Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return null;
        }

    }

    public static final class A{
        private final Properties props;
        A (Properties props) {
            this.props = new Properties();
            this.props.putAll(props);
        }

        Properties props() {
            return props;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (A.class != obj.getClass()) {
                return false;
            }
            final A other = (A) obj;
            if (this.props != other.props && (this.props == null || !this.props.equals(other.props))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + (this.props != null ? this.props.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return super.toString() + "[" + props + "]";
        }
    }

    public class Impl extends PropertiesBasedDataObject<A> {
        boolean deleted;
        public Impl(FileObject fo, MultiFileLoader ldr) throws Exception {
            super(fo, ldr, A.class);
        }

        public void onDelete(FileObject parentFolder) throws Exception {
            deleted = true;
        }

        public A createFrom(ObservableProperties properties) {
            return new A(properties);
        }

        public void ref() {
            super.refreshObject();
        }

        public Entry entry() {
            return new E();
        }

        private final class E extends MultiDataObject.Entry {
            E() {
                super (getPrimaryFile());
            }

            @Override
            public FileObject copy(FileObject f, String suffix) throws IOException {
                return f;
            }

            @Override
            public FileObject rename(String name) throws IOException {
                return getPrimaryFile();
            }

            @Override
            public FileObject move(FileObject f, String suffix) throws IOException {
                return f;
            }

            @Override
            public void delete() throws IOException {
                getPrimaryFile().delete();
            }

            @Override
            public FileObject createFromTemplate(FileObject f, String name) throws IOException {
                return f;
            }

        }

    }

    private static final class DFS extends Repository {

        DFS(FileSystem fs) {
            super(fs);
        }
    }

/*
    @Test
    public void testPropertyChanged() {
    }

    @Test
    public void testType() {
    }

    @Test
    public void testCreateFrom() {
    }

    @Test
    public void testGetLookup() {
    }

    @Test
    public void testIsCopyAllowed() {
    }

    @Test
    public void testIsMoveAllowed() {
    }

    @Test
    public void testIsRenameAllowed() {
    }

    @Test
    public void testGetPropertiesAsPropertySet() {
    }

    @Test
    public void testRefreshObject() {
    }
 */ 

}