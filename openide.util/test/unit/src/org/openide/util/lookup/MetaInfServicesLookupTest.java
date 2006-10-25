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

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;

/** Test finding services from manifest.
 * @author Jesse Glick
 */
public class MetaInfServicesLookupTest extends NbTestCase {

    public MetaInfServicesLookupTest(String name) {
        super(name);
    }

    private URL findJar(String n) {
        URL u = MetaInfServicesLookupTest.class.getResource("/org/openide/util/data/" + n);
        assertNotNull("Url cannot be null: " + n, u);
        return u;
    }

    ClassLoader c1, c2, c2a, c3, c4;

    protected void setUp() throws Exception {
        c1 = new URLClassLoader(new URL[] {
            findJar("services-jar-1.jar"),
        });
        c2 = new URLClassLoader(new URL[] {
            findJar("services-jar-2.jar"),
        }, c1);
        c2a = new URLClassLoader(new URL[] {
            findJar("services-jar-2.jar"),
        }, c1);
        c3 = new URLClassLoader(new URL[] {
            findJar("services-jar-2.jar"),
        });
        c4 = new URLClassLoader(new URL[] {
            findJar("services-jar-1.jar"),
            findJar("services-jar-2.jar"),
        });
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
