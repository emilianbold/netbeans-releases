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

package org.openide.util.lookup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;

/** Test finding services from manifest.
 * @author Jesse Glick
 */
public class MetaInfServicesLookupTest extends NbTestCase {
    private Logger LOG;
    
    public MetaInfServicesLookupTest(String name) {
        super(name);
        LOG = Logger.getLogger("Test." + name);
    }
    
    private String prefix() {
        return "META-INF/services/";
    }
    
    protected Level logLevel() {
        return Level.INFO;
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
            os.closeEntry();;
        }
        os.close();
        LOG.info("done " + jar);
        return jar.toURI().toURL();
    }

    ClassLoader c1, c2, c2a, c3, c4;

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

    public void testBasicUsage() throws Exception {
        Lookup l = Lookups.metaInfServices(c2);
        Class xface = c1.loadClass("org.foo.Interface");
        List results = new ArrayList(l.lookup(new Lookup.Template(xface)).allInstances());
        assertEquals(2, results.size());
        // Note that they have to be in order:
        assertEquals("org.foo.impl.Implementation1", results.get(0).getClass().getName());
        assertEquals("org.bar.Implementation2", results.get(1).getClass().getName());
        // Make sure it does not gratuitously replace items:
        List results2 = new ArrayList(l.lookup(new Lookup.Template(xface)).allInstances());
        assertEquals(results, results2);
    }

    public void testLoaderSkew() throws Exception {
        Class xface1 = c1.loadClass("org.foo.Interface");
        Lookup l3 = Lookups.metaInfServices(c3);
        // If we cannot load Interface, there should be no impls of course... quietly!
        assertEquals(Collections.EMPTY_LIST,
                new ArrayList(l3.lookup(new Lookup.Template(xface1)).allInstances()));
        Lookup l4 = Lookups.metaInfServices(c4);
        // If we can load Interface but it is the wrong one, ignore it.
        assertEquals(Collections.EMPTY_LIST,
                new ArrayList(l4.lookup(new Lookup.Template(xface1)).allInstances()));
        // Make sure l4 is really OK - it can load from its own JARs.
        Class xface4 = c4.loadClass("org.foo.Interface");
        assertEquals(2, l4.lookup(new Lookup.Template(xface4)).allInstances().size());
    }

    public void testStability() throws Exception {
        Lookup l = Lookups.metaInfServices(c2);
        Class xface = c1.loadClass("org.foo.Interface");
        Object first = l.lookup(new Lookup.Template(xface)).allInstances().iterator().next();
        l = Lookups.metaInfServices(c2a);
        Object second = l.lookup(new Lookup.Template(xface)).allInstances().iterator().next();
        assertEquals(first, second);
    }

    public void testMaskingOfResources() throws Exception {
        Lookup l1 = Lookups.metaInfServices(c1);
        Lookup l2 = Lookups.metaInfServices(c2);
        Lookup l4 = Lookups.metaInfServices(c4);

        assertNotNull("services1.jar defines a class that implements runnable", l1.lookup(Runnable.class));
        assertNull("services2.jar does not defines a class that implements runnable", l2.lookup(Runnable.class));
        assertNull("services1.jar defines Runnable, but services2.jar masks it out", l4.lookup(Runnable.class));
    }

    public void testOrdering() throws Exception {
        Lookup l = Lookups.metaInfServices(c1);
        Class xface = c1.loadClass("java.util.Comparator");
        List results = new ArrayList(l.lookup(new Lookup.Template(xface)).allInstances());
        assertEquals(1, results.size());

        l = Lookups.metaInfServices(c2);
        xface = c2.loadClass("java.util.Comparator");
        results = new ArrayList(l.lookup(new Lookup.Template(xface)).allInstances());
        assertEquals(2, results.size());
        // Test order:
        assertEquals("org.bar.Comparator2", results.get(0).getClass().getName());
        assertEquals("org.foo.impl.Comparator1", results.get(1).getClass().getName());

        // test that items without position are always at the end
        l = Lookups.metaInfServices(c2);
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

            protected URL findResource(String name) {
                if (name.equals("META-INF/services/java.lang.Object")) {
                    counter++;
                }

                URL retValue;

                retValue = super.findResource(name);
                return retValue;
            }

            protected Enumeration findResources(String name) throws IOException {
                if (name.equals("META-INF/services/java.lang.Object")) {
                    counter++;
                }
                Enumeration retValue;

                retValue = super.findResources(name);
                return retValue;
            }
        }
        Loader loader = new Loader();
        Lookup l = Lookups.metaInfServices(loader);

        Object no = l.lookup(String.class);
        assertNull("Not found of course", no);
        assertEquals("No lookup of Object", 0, loader.counter);
    }

    public void testListenersAreNotifiedWithoutHoldingALockIssue36035() throws Exception {
        final Lookup l = Lookups.metaInfServices(c2);
        final Class xface = c1.loadClass("org.foo.Interface");
        final Lookup.Result res = l.lookup(new Lookup.Template(Object.class));

        class L implements LookupListener, Runnable {
            private Thread toInterrupt;

            public void run() {
                assertNotNull("Possible to query lookup", l.lookup(xface));
                assertEquals("and there are two items", 2, res.allInstances().size());
                toInterrupt.interrupt();
            }

            public synchronized void resultChanged(LookupEvent ev) {
                toInterrupt = Thread.currentThread();
                RequestProcessor.getDefault().post(this);
                try {
                    wait(3000);
                    fail("Should be interrupted - means it was not possible to finish query in run() method");
                } catch (InterruptedException ex) {
                    // this is what we want
                }
            }
        }
        L listener = new L();

        res.addLookupListener(listener);
        assertEquals("Nothing yet", 0, res.allInstances().size());

        assertNotNull("Interface found", l.lookup(xface));
        assertNotNull("Listener notified", listener.toInterrupt);

        assertEquals("Now two", 2, res.allInstances().size());
    }
}
