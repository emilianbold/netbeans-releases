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

package org.openide.util.lookup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bar.Comparator2;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.test.MockLookup;

/** Test finding services from manifest.
 * @author Jesse Glick
 */
public class MetaInfServicesLookupTest extends NbTestCase {
    private Logger LOG;
    private Map<ClassLoader,Lookup> lookups = new WeakHashMap<ClassLoader,Lookup>();
    
    public MetaInfServicesLookupTest(String name) {
        super(name);
        LOG = Logger.getLogger("Test." + name);
    }

    protected String prefix() {
        return "META-INF/services/";
    }
    
    protected Lookup createLookup(ClassLoader c) {
        return Lookups.metaInfServices(c);
    }
    
    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    private Lookup getTestedLookup(ClassLoader c) {
        MockServices.setServices();
        Lookup l = lookups.get(c);
        if (l == null) {
            l = createLookup(c);
            lookups.put(c, l);
        }
        return l;
    }

    private URL findJar(String n) throws IOException {
        LOG.info("Looking for " + n);
        File jarDir = new File(getWorkDir(), "jars");
        jarDir.mkdirs();
        File jar = new File(jarDir, n);
        if (jar.exists()) {
            return jar.toURI().toURL();
        }
        
        LOG.info("generating " + jar);
        
        URL data = MetaInfServicesLookupTest.class.getResource(n.replaceAll("\\.jar", "\\.txt"));
        assertNotNull("Data found", data);
        StringBuffer sb = new StringBuffer();
        InputStreamReader r = new InputStreamReader(data.openStream());
        for(;;) {
            int ch = r.read();
            if (ch == -1) {
                break;
            }
            sb.append((char)ch);
        }
        
        JarOutputStream os = new JarOutputStream(new FileOutputStream(jar));
        
        Pattern p = Pattern.compile(":([^:]+):([^:]*)", Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = p.matcher(sb);
        Pattern foobar = Pattern.compile("^(org\\.(foo|bar)\\..*)$", Pattern.MULTILINE);
        Set<String> names = new TreeSet<String>();
        while (m.find()) {
            assert m.groupCount() == 2;
            String entryName = prefix() + m.group(1);
            LOG.info("putting there entry: " + entryName);
            os.putNextEntry(new JarEntry(entryName));
            os.write(m.group(2).getBytes());
            os.closeEntry();
            
            Matcher fb = foobar.matcher(m.group(2));
            while (fb.find()) {
                String clazz = fb.group(1).replace('.', '/') + ".class";
                LOG.info("will copy " + clazz);
                names.add(clazz);
            }
        }
        
        for (String copy : names) {
            os.putNextEntry(new JarEntry(copy));
            LOG.info("copying " + copy);
            InputStream from = MetaInfServicesLookupTest.class.getResourceAsStream("/" + copy);
            assertNotNull(copy, from);
            for (;;) {
                int ch = from.read();
                if (ch == -1) {
                    break;
                }
                os.write(ch);
            }
            from.close();
            os.closeEntry();
        }
        os.close();
        LOG.info("done " + jar);
        return jar.toURI().toURL();
    }

    ClassLoader c1, c2, c2a, c3, c4;

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        ClassLoader app = getClass().getClassLoader().getParent();
        ClassLoader c0 = app;
        
        c1 = new URLClassLoader(new URL[] {
            findJar("services-jar-1.jar"),
        }, c0);
        c2 = new URLClassLoader(new URL[] {
            findJar("services-jar-2.jar"),
        }, c1);
        c2a = new URLClassLoader(new URL[] {
            findJar("services-jar-2.jar"),
        }, c1);
        c3 = new URLClassLoader(new URL[] { findJar("services-jar-2.jar") },
            c0
        );
        c4 = new URLClassLoader(new URL[] {
            findJar("services-jar-1.jar"),
            findJar("services-jar-2.jar"),
        }, c0);
    }

    @Override
    protected void tearDown() throws Exception {
        Set<Reference<Lookup>> weak = new HashSet<Reference<Lookup>>();
        for (Lookup l : lookups.values()) {
            weak.add(new WeakReference<Lookup>(l));
        }
        
        lookups = null;
        
        for(Reference<Lookup> ref : weak) {
            assertGC("Lookup can disappear", ref);
        }
    }

    public void testBasicUsage() throws Exception {
        Lookup l = getTestedLookup(c2);
        Class xface = c1.loadClass("org.foo.Interface");
        List results = new ArrayList(l.lookup(new Lookup.Template(xface)).allInstances());
        assertEquals("Two items in result: " + results, 2, results.size());
        // Note that they have to be in order:
        assertEquals("org.foo.impl.Implementation1", results.get(0).getClass().getName());
        assertEquals("org.bar.Implementation2", results.get(1).getClass().getName());
        // Make sure it does not gratuitously replace items:
        List results2 = new ArrayList(l.lookup(new Lookup.Template(xface)).allInstances());
        assertEquals(results, results2);
    }

    public void testLoaderSkew() throws Exception {
        Class xface1 = c1.loadClass("org.foo.Interface");
        Lookup l3 = getTestedLookup(c3);
        // If we cannot load Interface, there should be no impls of course... quietly!
        assertEquals(Collections.EMPTY_LIST,
                new ArrayList(l3.lookup(new Lookup.Template(xface1)).allInstances()));
        Lookup l4 = getTestedLookup(c4);
        // If we can load Interface but it is the wrong one, ignore it.
        assertEquals(Collections.EMPTY_LIST,
                new ArrayList(l4.lookup(new Lookup.Template(xface1)).allInstances()));
        // Make sure l4 is really OK - it can load from its own JARs.
        Class xface4 = c4.loadClass("org.foo.Interface");
        assertEquals(2, l4.lookup(new Lookup.Template(xface4)).allInstances().size());
    }

    public void testStability() throws Exception {
        Lookup l = getTestedLookup(c2);
        Class xface = c1.loadClass("org.foo.Interface");
        Object first = l.lookup(new Lookup.Template(xface)).allInstances().iterator().next();
        l = getTestedLookup(c2a);
        Object second = l.lookup(new Lookup.Template(xface)).allInstances().iterator().next();
        assertEquals(first, second);
    }

    public void testMaskingOfResources() throws Exception {
        Lookup l1 = getTestedLookup(c1);
        Lookup l2 = getTestedLookup(c2);
        Lookup l4 = getTestedLookup(c4);

        assertNotNull("services1.jar defines a class that implements runnable", l1.lookup(Runnable.class));
        assertNull("services2.jar does not defines a class that implements runnable", l2.lookup(Runnable.class));
        assertNull("services1.jar defines Runnable, but services2.jar masks it out", l4.lookup(Runnable.class));
    }

    public void testOrdering() throws Exception {
        Lookup l = getTestedLookup(c1);
        Class xface = c1.loadClass("java.util.Comparator");
        List results = new ArrayList(l.lookup(new Lookup.Template(xface)).allInstances());
        assertEquals(1, results.size());

        l = getTestedLookup(c2);
        xface = c2.loadClass("java.util.Comparator");
        results = new ArrayList(l.lookup(new Lookup.Template(xface)).allInstances());
        assertEquals(2, results.size());
        // Test order:
        assertEquals("org.bar.Comparator2", results.get(0).getClass().getName());
        assertEquals("org.foo.impl.Comparator1", results.get(1).getClass().getName());

        // test that items without position are always at the end
        l = getTestedLookup(c2);
        xface = c2.loadClass("java.util.Iterator");
        results = new ArrayList(l.lookup(new Lookup.Template(xface)).allInstances());
        assertEquals(2, results.size());
        // Test order:
        assertEquals("org.bar.Iterator2", results.get(0).getClass().getName());
        assertEquals("org.foo.impl.Iterator1", results.get(1).getClass().getName());
    }

    public void testNoCallToGetResourceForObjectIssue65124() throws Exception {
        class Loader extends ClassLoader {
            private int counter;

            @Override
            protected URL findResource(String name) {
                if (name.equals(prefix() + "java.lang.Object")) {
                    counter++;
                }

                URL retValue;

                retValue = super.findResource(name);
                return retValue;
            }

            @Override
            protected Enumeration findResources(String name) throws IOException {
                if (name.equals(prefix() + "java.lang.Object")) {
                    counter++;
                }
                Enumeration retValue;

                retValue = super.findResources(name);
                return retValue;
            }
        }
        Loader loader = new Loader();
        Lookup l = getTestedLookup(loader);

        Object no = l.lookup(String.class);
        assertNull("Not found of course", no);
        assertEquals("No lookup of Object", 0, loader.counter);
    }

    public void testCanGarbageCollectClasses() throws Exception {
        class Loader extends ClassLoader {
            public Loader() {
                super(Loader.class.getClassLoader().getParent());
            }

            @Override
            protected URL findResource(String name) {
                if (name.equals(prefix() + "java.lang.Runnable")) {
                    return Loader.class.getResource("MetaInfServicesLookupTestRunnable.txt");
                }

                URL retValue;

                retValue = super.findResource(name);
                return retValue;
            }

            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                if (name.equals("org.openide.util.lookup.MetaInfServicesLookupTestRunnable")) {
                    try {
                        InputStream is = getClass().getResourceAsStream("MetaInfServicesLookupTestRunnable.class");
                        byte[] arr = new byte[is.available()];
                        int read = is.read(arr);
                        assertEquals("Fully read", arr.length, read);
                        return defineClass(name, arr, 0, arr.length);
                    } catch (IOException ex) {
                        throw new ClassNotFoundException("Cannot load", ex);
                    }
                }
                throw new ClassNotFoundException();
            }



            @Override
            protected Enumeration findResources(String name) throws IOException {
                if (name.equals(prefix() + "java.lang.Runnable")) {
                    return Collections.enumeration(Collections.singleton(findResource(name)));
                }
                return super.findResources(name);
            }
        }
        Loader loader = new Loader();
        Lookup l = getTestedLookup(loader);


        Object no = l.lookup(Runnable.class);
        assertNotNull("Found of course", no);
        assertEquals("The right name", "MetaInfServicesLookupTestRunnable", no.getClass().getSimpleName());
        if (no.getClass().getClassLoader() != loader) {
            fail("Wrong classloader: " + no.getClass().getClassLoader());
        }

        WeakReference<Object> ref = new WeakReference<Object>(no.getClass());
        loader = null;
        no = null;
        l = null;
        lookups.clear();
        MockLookup.setInstances();
        Thread.currentThread().setContextClassLoader(null);
        assertGC("Class can be garbage collected", ref);
    }

    public void testSuperTypes() throws Exception {
        doTestSuperTypes(createLookup(c2));
        doTestSuperTypes(new ProxyLookup(createLookup(c2)));
    }
    private void doTestSuperTypes(Lookup l) throws Exception {
        final Class<?> xface = c1.loadClass("org.foo.Interface");
        final Lookup.Result<Object> res = l.lookupResult(Object.class);
        assertEquals("Nothing yet", 0, res.allInstances().size());
        final AtomicBoolean event = new AtomicBoolean();
        final Thread here = Thread.currentThread();
        res.addLookupListener(new LookupListener() {
            public void resultChanged(LookupEvent ev) {
                if (Thread.currentThread() == here) {
                    event.set(true);
                }
            }
        });
        assertNotNull("Interface found", l.lookup(xface));
        assertFalse(event.get());
        MetaInfServicesLookup.RP.post(new Runnable() {public void run() {}}).waitFinished();
        assertEquals("Now two", 2, res.allInstances().size());
    }
    
    public void testWrongOrderAsInIssue100320() throws Exception {
        ClassLoader app = getClass().getClassLoader().getParent();
        ClassLoader c0 = app;
        ClassLoader ctmp = new URLClassLoader(new URL[] {
            findJar("problem100320.jar"),
        }, c0);
        Lookup lookup = Lookups.metaInfServices(ctmp, prefix());

        Collection<?> colAWT = lookup.lookupAll(IOException.class);
        assertEquals("There is enough objects to switch to InheritanceTree", 12, colAWT.size());
        
        
        List<?> col1 = new ArrayList<Object>(lookup.lookupAll(Comparator.class));
        assertEquals("Two", 2, col1.size());
        Collection<?> col2 = lookup.lookupAll(ctmp.loadClass(Comparator2.class.getName()));
        assertEquals("One", 1, col2.size());
        List<?> col3 = new ArrayList<Object>(lookup.lookupAll(Comparator.class));
        assertEquals("Two2", 2, col3.size());
        
        Iterator<?> it1 = col1.iterator();
        Iterator<?> it3 = col3.iterator();
        if (
            it1.next() != it3.next() || 
            it1.next() != it3.next() 
        ) {
            fail("Collections are different:\nFirst: " + col1 + "\nLast:  " + col3);
        }
    }
}
