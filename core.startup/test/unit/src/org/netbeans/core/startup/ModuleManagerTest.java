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

package org.netbeans.core.startup;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.netbeans.InvalidException;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.Util;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

/** Test the module manager as well as the Module class.
 * This means creating modules from JAR as well as from "classpath"
 * (i.e. rigged-up classloader), and testing that it creates them with
 * the correct stuff; testing that the various pieces of the manifest
 * are correctly parsed and made accessible; that dependencies work
 * when things are done in various orders etc.; that problems (such as
 * missing dependencies) are accurately reported; that the classloaders
 * are capable of getting everything listed; that module installer
 * methods are called at the correct times and with modules in the correct
 * state; that changes are fired correctly; etc.
 * Note that since the design of the module manager makes no direct
 * reference to general IDE classes other than standalone APIs and a couple
 * of standalone core utilities, this entire test can (and ought to be)
 * executed in standalone mode.
 * @author Jesse Glick
 */
public class ModuleManagerTest extends SetupHid {
    
    static {
        // To match org.netbeans.Main.execute (cf. #44828):
        new URLConnection(ModuleManagerTest.class.getResource("ModuleManagerTest.class")) {
            public void connect() throws IOException {}
        }.setDefaultUseCaches(false);
        // Just annoying and misleading (tests pass despite exception):
        System.setProperty ("suppress.topological.exception", "true");
    }
    
    public ModuleManagerTest(String name) {
        super(name);
    }
    
    /*
    public static void main(String[] args) {
        // Turn on verbose logging while developing tests:
        System.setProperty("org.netbeans.core.modules", "0");
        System.setProperty("org.openide.util.Lookup", "-");
        TestRunner.run(new NbTestSuite(ModuleManagerTest.class));
    }
     */
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    /** Load simple-module and depends-on-simple-module.
     * Make sure they can be installed and in a sane order.
     * Make sure a class from one can depend on a class from another.
     * Try to disable them too.
     */
    public void testSimpleInstallation() throws Exception {
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m2 = mgr.create(new File(jars, "depends-on-simple-module.jar"), null, false, false, false);
            Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, false, false);
            assertEquals("org.foo", m1.getCodeNameBase());
            assertEquals("org.bar", m2.getCodeNameBase());
            assertEquals(Collections.EMPTY_SET, m1.getDependencies());
            assertEquals(Dependency.create(Dependency.TYPE_MODULE, "org.foo/1"), m2.getDependencies());
            Map modulesByName = new HashMap();
            modulesByName.put(m1.getCodeNameBase(), m1);
            modulesByName.put(m2.getCodeNameBase(), m2);
            List m1m2 = Arrays.asList(new Module[] {m1, m2});
            List m2m1 = Arrays.asList(new Module[] {m2, m1});
            Map deps = Util.moduleDependencies(m1m2, modulesByName, Collections.EMPTY_MAP);
            assertNull(deps.get(m1));
            assertEquals(Collections.singletonList(m1), deps.get(m2));
            assertEquals(m2m1, Utilities.topologicalSort(m1m2, deps));
            assertEquals(m2m1, Utilities.topologicalSort(m2m1, deps));
            // Leave commented out since it has a (hopefully clean) mutation effect
            // and could affect results:
            /*
            assertEquals(Collections.EMPTY_SET, m1.getProblems());
            assertEquals(Collections.EMPTY_SET, m2.getProblems());
             */
            Set m1PlusM2 = new HashSet(); // Set<Module>
            m1PlusM2.add(m1);
            m1PlusM2.add(m2);
            List toEnable = mgr.simulateEnable(m1PlusM2);
            assertEquals("correct result of simulateEnable", Arrays.asList(new Module[] {m1, m2}), toEnable);
            mgr.enable(m1PlusM2);
            assertEquals(Arrays.asList(new String[] {
                "prepare",
                "prepare",
                "load"
            }), installer.actions);
            assertEquals(Arrays.asList(new Object[] {
                m1,
                m2,
                Arrays.asList(new Module[] {m1, m2})
            }), installer.args);
            Class somethingelse = Class.forName("org.bar.SomethingElse", true, m2.getClassLoader());
            Method somemethod = somethingelse.getMethod("message", null);
            assertEquals("hello", somemethod.invoke(somethingelse.newInstance(), null));
            installer.clear();
            List toDisable = mgr.simulateDisable(Collections.singleton(m1));
            assertEquals("correct result of simulateDisable", Arrays.asList(new Module[] {m2, m1}), toDisable);
            toDisable = mgr.simulateDisable(m1PlusM2);
            assertEquals("correct result of simulateDisable #2", Arrays.asList(new Module[] {m2, m1}), toDisable);
            mgr.disable(m1PlusM2);
            assertFalse(m1.isEnabled());
            assertFalse(m2.isEnabled());
            assertEquals(Collections.EMPTY_SET, mgr.getEnabledModules());
            assertEquals(m1PlusM2, mgr.getModules());
            assertEquals(Arrays.asList(new String[] {
                "unload",
                "dispose",
                "dispose"
            }), installer.actions);
            assertEquals(Arrays.asList(new Object[] {
                Arrays.asList(new Module[] {m2, m1}),
                m2,
                m1
            }), installer.args);
            installer.clear();
            mgr.enable(m1);
            mgr.shutDown();
            assertEquals(Arrays.asList(new String[] {
                "prepare",
                "load",
                "closing",
                "close"
            }), installer.actions);
            assertEquals(Arrays.asList(new Object[] {
                m1,
                Collections.singletonList(m1),
                Collections.singletonList(m1),
                Collections.singletonList(m1)
            }), installer.args);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    public void testInstallAutoload() throws Exception {
        // Cf. #9779, I think.
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m2 = mgr.create(new File(jars, "depends-on-simple-module.jar"), null, false, false, false);
            // m1 will be an autoload.
            Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, true, false);
            try {
                mgr.simulateEnable(new HashSet(Arrays.asList(new Module[] {m1, m2})));
                assertTrue("Should not permit you to simulate enablement of an autoload", false);
            } catch (IllegalArgumentException iae) {
                // Good. m1 should not have been passed to it.
            }
            assertEquals(Collections.EMPTY_SET, m1.getProblems());
            assertEquals(Collections.EMPTY_SET, m2.getProblems());
            List toEnable = mgr.simulateEnable(Collections.singleton(m2));
            assertEquals("correct result of simulateEnable", Arrays.asList(new Module[] {m1, m2}), toEnable);
            mgr.enable(Collections.singleton(m2));
            assertEquals(Arrays.asList(new String[] {
                "prepare",
                "prepare",
                "load",
            }), installer.actions);
            assertEquals(Arrays.asList(new Object[] {
                m1,
                m2,
                Arrays.asList(new Module[] {m1, m2}),
            }), installer.args);
            Class somethingelse = Class.forName("org.bar.SomethingElse", true, m2.getClassLoader());
            Method somemethod = somethingelse.getMethod("message", null);
            assertEquals("hello", somemethod.invoke(somethingelse.newInstance(), null));
            // Now try turning off m2 and make sure m1 goes away as well.
            assertEquals("correct result of simulateDisable", Arrays.asList(new Module[] {m2, m1}), mgr.simulateDisable(Collections.singleton(m2)));
            installer.clear();
            mgr.disable(Collections.singleton(m2));
            assertEquals(Arrays.asList(new String[] {
                "unload",
                "dispose",
                "dispose",
            }), installer.actions);
            assertEquals(Arrays.asList(new Object[] {
                Arrays.asList(new Module[] {m2, m1}),
                m2,
                m1,
            }), installer.args);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    public void testInstallEager() throws Exception {
        // Cf. #17501.
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, false, false);
            // m2 will be eager.
            Module m2 = mgr.create(new File(jars, "depends-on-simple-module.jar"), null, false, false, true);
            try {
                mgr.simulateEnable(new HashSet(Arrays.asList(new Module[] {m1, m2})));
                fail("Should not permit you to simulate enablement of an eager module");
            } catch (IllegalArgumentException iae) {
                // Good. m2 should not have been passed to it.
            }
            assertEquals(Collections.EMPTY_SET, m1.getProblems());
            assertEquals(Collections.EMPTY_SET, m2.getProblems());
            List toEnable = mgr.simulateEnable(Collections.singleton(m1));
            assertEquals("correct result of simulateEnable", Arrays.asList(new Module[] {m1, m2}), toEnable);
            mgr.enable(Collections.singleton(m1));
            assertEquals(Arrays.asList(new String[] {
                "prepare",
                "prepare",
                "load",
            }), installer.actions);
            assertEquals(Arrays.asList(new Object[] {
                m1,
                m2,
                Arrays.asList(new Module[] {m1, m2}),
            }), installer.args);
            Class somethingelse = Class.forName("org.bar.SomethingElse", true, m2.getClassLoader());
            Method somemethod = somethingelse.getMethod("message", null);
            assertEquals("hello", somemethod.invoke(somethingelse.newInstance(), null));
            // Now try turning off m1 and make sure m2 goes away quietly.
            assertEquals("correct result of simulateDisable", Arrays.asList(new Module[] {m2, m1}), mgr.simulateDisable(Collections.singleton(m1)));
            installer.clear();
            mgr.disable(Collections.singleton(m1));
            assertEquals(Arrays.asList(new String[] {
                "unload",
                "dispose",
                "dispose",
            }), installer.actions);
            assertEquals(Arrays.asList(new Object[] {
                Arrays.asList(new Module[] {m2, m1}),
                m2,
                m1,
            }), installer.args);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    public void testEagerPlusAutoload() throws Exception {
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            // m1 autoload, m2 normal, m3 eager
            Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, true, false);
            Module m2 = mgr.create(new File(jars, "depends-on-simple-module.jar"), null, false, false, false);
            Module m3 = mgr.create(new File(jars, "dep-on-dep-on-simple.jar"), null, false, false, true);
            List toEnable = mgr.simulateEnable(Collections.singleton(m2));
            assertEquals("correct result of simulateEnable", Arrays.asList(new Module[] {m1, m2, m3}), toEnable);
            mgr.enable(Collections.singleton(m2));
            assertEquals(Arrays.asList(new String[] {
                "prepare",
                "prepare",
                "prepare",
                "load",
            }), installer.actions);
            assertEquals(Arrays.asList(new Object[] {
                m1,
                m2,
                m3,
                Arrays.asList(new Module[] {m1, m2, m3}),
            }), installer.args);
            Class somethingelseagain = Class.forName("org.baz.SomethingElseAgain", true, m3.getClassLoader());
            Method somemethod = somethingelseagain.getMethod("doit", null);
            assertEquals("hello", somemethod.invoke(somethingelseagain.newInstance(), null));
            assertEquals("correct result of simulateDisable", Arrays.asList(new Module[] {m3, m2, m1}), mgr.simulateDisable(Collections.singleton(m2)));
            installer.clear();
            mgr.disable(Collections.singleton(m2));
            assertEquals(Arrays.asList(new String[] {
                "unload",
                "dispose",
                "dispose",
                "dispose",
            }), installer.actions);
            assertEquals(Arrays.asList(new Object[] {
                Arrays.asList(new Module[] {m3, m2, m1}),
                m3,
                m2,
                m1,
            }), installer.args);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    /** Test scenario from #22536: when a normal module and an eager module
     * both depend on the autoload, the eager & autoload modules should
     * always be on, regardless of the normal module.
     */
    public void testEagerPlusAutoload2() throws Exception {
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            // m1 autoload, m2 normal, m3 eager
            Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, true, false);
            Module m2 = mgr.create(new File(jars, "depends-on-simple-module.jar"), null, false, false, false);
            Module m3 = mgr.create(new File(jars, "depends-on-simple-module-2.jar"), null, false, false, true);
            assertTrue(m1.isEnabled());
            assertFalse(m2.isEnabled());
            assertTrue(m3.isEnabled());
            List toEnable = mgr.simulateEnable(Collections.singleton(m2));
            assertEquals("correct result of simulateEnable", Collections.singletonList(m2), toEnable);
            mgr.enable(Collections.singleton(m2));
            assertTrue(m1.isEnabled());
            assertTrue(m2.isEnabled());
            assertTrue(m3.isEnabled());
            List toDisable = mgr.simulateDisable(Collections.singleton(m2));
            assertEquals("correct result of simulateDisable", Collections.singletonList(m2), toDisable);
            mgr.disable(Collections.singleton(m2));
            assertTrue(m1.isEnabled());
            assertFalse(m2.isEnabled());
            assertTrue(m3.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    public void testEagerEnabledImmediately() throws Exception {
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, false, true);
            assertTrue(m1.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "depends-on-simple-module.jar"), null, false, false, true);
            assertFalse(m1.isEnabled());
            assertFalse(m2.isEnabled());
            mgr.enable(m1);
            assertTrue(m1.isEnabled());
            assertTrue(m2.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, true, false);
            Module m2 = mgr.create(new File(jars, "depends-on-simple-module.jar"), null, false, false, true);
            assertTrue(m1.isEnabled());
            assertTrue(m2.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    public void testCyclic() throws Exception {
        // Cf. #12014.
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module cyc1 = mgr.create(new File(jars, "cyclic-1.jar"), null, false, false, false);
            Module cyc2 = mgr.create(new File(jars, "cyclic-2.jar"), null, false, false, false);
            Module cycd = mgr.create(new File(jars, "depends-on-cyclic-1.jar"), null, false, false, false);
            Set circular = new HashSet(Arrays.asList(new Module[] {cyc1, cyc2, cycd}));
            assertEquals("correct result of simulateEnable", Collections.EMPTY_LIST, mgr.simulateEnable(circular));
            assertEquals("cyc1 problems include cyc2", cyc1.getDependencies(), cyc1.getProblems());
            assertEquals("cyc2 problems include cyc1", cyc2.getDependencies(), cyc2.getProblems());
            assertEquals("cycd problems include cyc1", cycd.getDependencies(), cycd.getProblems());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testBuildVersionCanBeReadOrIsDelegated() throws Exception {
        // Cf. #12014.
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module cyc1 = mgr.create(new File(jars, "cyclic-1.jar"), null, false, false, false);
            Module cyc2 = mgr.create(new File(jars, "cyclic-2.jar"), null, false, false, false);
            
            String impl1 = cyc1.getImplementationVersion ();
            String impl2 = cyc2.getImplementationVersion ();
            String bld1 = cyc1.getBuildVersion ();
            String bld2 = cyc2.getBuildVersion ();
            
            assertEquals (
                "cyc1 does not define build version and thus it is same as impl",
                impl1, bld1
            );
            
            assertEquals (
                "cyc2 does define build version",
                "this_line_is_here_due_to_yarda",
                bld2
            );
            
            assertTrue ("Impl and build versions are not same",
                !bld2.equals (impl2)
            );
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    public void testLookup() throws Exception {
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        Module m1, m2;
        try {
            m2 = mgr.create(new File(jars, "depends-on-simple-module.jar"), null, false, false, false);
            m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, false, false);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        Lookup l = mgr.getModuleLookup();
        assertNull(l.lookup(String.class));
        Object random = l.lookup(ModuleInfo.class);
        assertTrue(random == m1 || random == m2);
        random = l.lookup(Module.class);
        assertTrue(random == m1 || random == m2);
        Lookup.Result resultAll = l.lookup(new Lookup.Template(ModuleInfo.class));
        assertEquals("finding all instances works", new HashSet(Arrays.asList(new Module[] {m1, m2})), new HashSet(resultAll.allInstances()));
        Lookup.Result resultInstance2 = l.lookup(new Lookup.Template(null, null, m2));
        assertEquals("finding one specific instance works", Collections.singleton(m2), new HashSet(resultInstance2.allInstances()));
        Collection items = resultInstance2.allItems();
        assertTrue(items.size() == 1);
        Lookup.Item item = (Lookup.Item)items.iterator().next();
        assertEquals(m2, item.getInstance());
        Util.err.log("Item ID: " + item.getId());
        assertTrue("Item class is OK: " + item.getType(), item.getType().isAssignableFrom(Module.class));
        assertEquals("finding by ID works", Collections.singleton(m2), new HashSet(l.lookup(new Lookup.Template(null, item.getId(), null)).allInstances()));
        final boolean[] waiter = new boolean[] {false};
        resultAll.addLookupListener(new LookupListener() {
            public void resultChanged(LookupEvent lev) {
                Util.err.log("Got event: " + lev);
                synchronized (waiter) {
                    waiter[0] = true;
                    waiter.notify();
                }
            }
        });
        Module m3;
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            m3 = mgr.create(new File(jars, "cyclic-1.jar"), null, false, false, false);
            mgr.delete(m2);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        assertEquals("results changed", new HashSet(Arrays.asList(new Module[] {m1, m3})), new HashSet(resultAll.allInstances()));
        synchronized (waiter) {
            if (! waiter[0]) {
                waiter.wait(5000);
            }
        }
        assertTrue("got lookup changes within 5 seconds", waiter[0]);
    }
    
    /** Test that after deletion of a module, problems cache is cleared. */
    public void test14561() throws Exception {
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "depends-on-simple-module.jar"), null, false, false, false);
            Set m1AndM2 = new HashSet(Arrays.asList(new Module[] {m1, m2}));
            mgr.enable(m1AndM2);
            mgr.disable(m1AndM2);
            assertEquals(Collections.EMPTY_SET, m2.getProblems());
            mgr.delete(m1);
            assertEquals(1, m2.getProblems().size());
            assertEquals(Collections.EMPTY_LIST, mgr.simulateEnable(Collections.singleton(m2)));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    /** Test that PROP_PROBLEMS is fired reliably after unexpected problems. */
    public void test14560() throws Exception {
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        LoggedPCListener listener = new LoggedPCListener();
        mgr.addPropertyChangeListener(listener);
        mgr.mutexPrivileged().enterWriteAccess();
        Module m1, m2;
        try {
            m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, false, false);
            m1.addPropertyChangeListener(listener);
            m2 = mgr.create(new File(jars, "depends-on-simple-module.jar"), null, false, false, false);
            m2.addPropertyChangeListener(listener);
            installer.delinquents.add(m1);
            Set m1AndM2 = new HashSet(Arrays.asList(new Module[] {m1, m2}));
            try {
                mgr.enable(m1AndM2);
            } catch (InvalidException ie) {
                assertEquals(m1, ie.getModule());
            }
            assertFalse(m1.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        assertTrue("Got PROP_PROBLEMS on m1", listener.waitForChange(m1, Module.PROP_PROBLEMS));
    }
    
    // #14705: make sure package loading is tested
    public void testPackageLoading() throws Exception {
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            // Make sure all of these can be turned on:
            tryEnablingModule(mgr, "depends-on-lib-undecl.jar");
            tryEnablingModule(mgr, "depends-on-lib-unvers.jar");
            tryEnablingModule(mgr, "depends-on-lib-vers.jar");
            tryEnablingModule(mgr, "depends-on-lib-vers-partial.jar");
            // In fact it is OK to depend on pkg.somepkg[Something] even with
            // library-undecl.jar, since the classloader will define a package for you.
            //failToEnableModule(mgr, "fails-on-lib-undecl.jar");
            // These should not work:
            failToEnableModule(mgr, "fails-on-lib-unvers.jar");
            failToEnableModule(mgr, "fails-on-lib-old.jar");
            // Make sure that classloading is OK:
            Module m = mgr.create(new File(jars, "depends-on-lib-undecl.jar"), null, false, false, false);
            mgr.enable(m);
            Class c = m.getClassLoader().loadClass("org.dol.User");
            Object o = c.newInstance();
            Field f = c.getField("val");
            assertEquals(42, f.getInt(o));
            mgr.disable(m);
            mgr.delete(m);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    private void tryEnablingModule(ModuleManager mgr, String name) throws Exception {
        Module m = mgr.create(new File(jars, name), null, false, false, false);
        try {
            mgr.enable(m);
            mgr.disable(m);
        } finally {
            mgr.delete(m);
        }
    }
    
    private void failToEnableModule(ModuleManager mgr, String name) throws Exception {
        try {
            tryEnablingModule(mgr, name);
            fail("Was able to turn on " + name + " without complaint");
        } catch (InvalidException ie) {
            // Fine, expected.
        }
    }
    
    // #12549: check that loading of localized manifest attributes works.
    public void testLocalizedManifestAttributes() throws Exception {
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        Locale starting = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("en", "US"));
            File locmanijar = new File(jars, "localized-manifest.jar");
            assertTrue("test JAR exists: " + locmanijar, locmanijar.isFile()); // #50891
            Module m = mgr.create(locmanijar, null, false, false, false);
            // These are defined in the bundle:
            assertEquals("en_US display name", "Localized Manifest Module", m.getDisplayName());
            assertEquals("en_US bundle main attr", "value #1", m.getLocalizedAttribute("some-other-key"));
            assertEquals("en_US bundle sub attr", "value #2", m.getLocalizedAttribute("locmani/something.txt/other-key"));
            assertEquals("en_US bundle main attr untrans", "value #7", m.getLocalizedAttribute("other-untrans"));
            assertEquals("en_US bundle sub attr untrans", "value #8", m.getLocalizedAttribute("locmani/something.txt/other-untrans"));
            // These in the manifest itself:
            assertEquals("en_US manifest main attr", "value #3", m.getLocalizedAttribute("some-key"));
            assertEquals("en_US manifest sub attr", "value #4", m.getLocalizedAttribute("locmani/something.txt/key"));
            assertEquals("en_US manifest main attr untrans", "value #5", m.getLocalizedAttribute("untrans"));
            assertEquals("en_US manifest sub attr untrans", "value #6", m.getLocalizedAttribute("locmani/something.txt/untrans"));
            mgr.delete(m);
            // Now try it again, with a different locale this time:
            Locale.setDefault(new Locale("cs", "CZ"));
            m = mgr.create(new File(jars, "localized-manifest.jar"), null, false, false, false);
            // Note Unicode values in the bundle.
            assertEquals("cs_CZ display name", "Modul s lokalizovan\u00FDm manifestem", m.getDisplayName());
            assertEquals("cs_CZ bundle main attr", "v\u00FDznam #1", m.getLocalizedAttribute("some-other-key"));
            assertEquals("cs_CZ bundle sub attr", "v\u00FDznam #2", m.getLocalizedAttribute("locmani/something.txt/other-key"));
            // These are not translated, see that they fall back to "default" locale:
            assertEquals("cs_CZ bundle main attr untrans", "value #7", m.getLocalizedAttribute("other-untrans"));
            assertEquals("cs_CZ bundle sub attr untrans", "value #8", m.getLocalizedAttribute("locmani/something.txt/other-untrans"));
            // The manifest cannot hold non-ASCII characters.
            assertEquals("cs_CZ manifest main attr", "vyznam #3", m.getLocalizedAttribute("some-key"));
            assertEquals("cs_CZ manifest sub attr", "vyznam #4", m.getLocalizedAttribute("locmani/something.txt/key"));
            // Also not translated:
            assertEquals("cs_CZ manifest main attr untrans", "value #5", m.getLocalizedAttribute("untrans"));
            assertEquals("cs_CZ manifest sub attr untrans", "value #6", m.getLocalizedAttribute("locmani/something.txt/untrans"));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
            Locale.setDefault(starting);
        }
    }
    
    // #19698: check that it also works when the module is enabled (above, module was disabled).
    public void testLocalizedManifestAttributesWhileEnabled() throws Exception {
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        Locale starting = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("en", "US"));
            Module m = mgr.create(new File(jars, "localized-manifest.jar"), null, false, false, false);
            mgr.enable(m);
            // These are defined in the bundle:
            assertEquals("en_US display name", "Localized Manifest Module", m.getDisplayName());
            assertEquals("en_US bundle main attr", "value #1", m.getLocalizedAttribute("some-other-key"));
            assertEquals("en_US bundle sub attr", "value #2", m.getLocalizedAttribute("locmani/something.txt/other-key"));
            assertEquals("en_US bundle main attr untrans", "value #7", m.getLocalizedAttribute("other-untrans"));
            assertEquals("en_US bundle sub attr untrans", "value #8", m.getLocalizedAttribute("locmani/something.txt/other-untrans"));
            // These in the manifest itself:
            assertEquals("en_US manifest main attr", "value #3", m.getLocalizedAttribute("some-key"));
            assertEquals("en_US manifest sub attr", "value #4", m.getLocalizedAttribute("locmani/something.txt/key"));
            assertEquals("en_US manifest main attr untrans", "value #5", m.getLocalizedAttribute("untrans"));
            assertEquals("en_US manifest sub attr untrans", "value #6", m.getLocalizedAttribute("locmani/something.txt/untrans"));
            mgr.disable(m);
            mgr.delete(m);
            // Now try it again, with a different locale this time:
            Locale.setDefault(new Locale("cs", "CZ"));
            m = mgr.create(new File(jars, "localized-manifest.jar"), null, false, false, false);
            mgr.enable(m);
            // Note Unicode values in the bundle.
            assertEquals("cs_CZ display name", "Modul s lokalizovan\u00FDm manifestem", m.getDisplayName());
            assertEquals("cs_CZ bundle main attr", "v\u00FDznam #1", m.getLocalizedAttribute("some-other-key"));
            assertEquals("cs_CZ bundle sub attr", "v\u00FDznam #2", m.getLocalizedAttribute("locmani/something.txt/other-key"));
            // These are not translated, see that they fall back to "default" locale:
            assertEquals("cs_CZ bundle main attr untrans", "value #7", m.getLocalizedAttribute("other-untrans"));
            assertEquals("cs_CZ bundle sub attr untrans", "value #8", m.getLocalizedAttribute("locmani/something.txt/other-untrans"));
            // The manifest cannot hold non-ASCII characters.
            assertEquals("cs_CZ manifest main attr", "vyznam #3", m.getLocalizedAttribute("some-key"));
            assertEquals("cs_CZ manifest sub attr", "vyznam #4", m.getLocalizedAttribute("locmani/something.txt/key"));
            // Also not translated:
            assertEquals("cs_CZ manifest main attr untrans", "value #5", m.getLocalizedAttribute("untrans"));
            assertEquals("cs_CZ manifest sub attr untrans", "value #6", m.getLocalizedAttribute("locmani/something.txt/untrans"));
            mgr.disable(m);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
            Locale.setDefault(starting);
        }
    }
    
    // There was also a bug that loc mani attrs were not recognized for classpath modules.
    public void testLocalizedManifestAttributesClasspath() throws Exception {
        File jar = new File(jars, "localized-manifest.jar");
        File ljar = new File(new File(jars, "locale"), "localized-manifest_cs.jar");
        Manifest mani;
        JarFile jf = new JarFile(jar);
        try {
            mani = jf.getManifest();
        } finally {
            jf.close();
        }
        ClassLoader l = new URLClassLoader(new URL[] {
            // Order should be irrelevant:
            jar.toURL(),
            ljar.toURL(),
        });
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        Locale starting = Locale.getDefault();
        try {
            ModuleManager mgr = new ModuleManager(installer, ev);
            mgr.mutexPrivileged().enterWriteAccess();
            try {
                Locale.setDefault(new Locale("en", "US"));
                Module m = mgr.createFixed(mani, null, l);
                // These are defined in the bundle:
                assertEquals("en_US display name", "Localized Manifest Module", m.getDisplayName());
                assertEquals("en_US bundle main attr", "value #1", m.getLocalizedAttribute("some-other-key"));
                assertEquals("en_US bundle sub attr", "value #2", m.getLocalizedAttribute("locmani/something.txt/other-key"));
                assertEquals("en_US bundle main attr untrans", "value #7", m.getLocalizedAttribute("other-untrans"));
                assertEquals("en_US bundle sub attr untrans", "value #8", m.getLocalizedAttribute("locmani/something.txt/other-untrans"));
                // These in the manifest itself:
                assertEquals("en_US manifest main attr", "value #3", m.getLocalizedAttribute("some-key"));
                assertEquals("en_US manifest sub attr", "value #4", m.getLocalizedAttribute("locmani/something.txt/key"));
                assertEquals("en_US manifest main attr untrans", "value #5", m.getLocalizedAttribute("untrans"));
                assertEquals("en_US manifest sub attr untrans", "value #6", m.getLocalizedAttribute("locmani/something.txt/untrans"));
            } finally {
                mgr.mutexPrivileged().exitWriteAccess();
            }
            // Need to start with a new manager: cannot delete classpath modules, would be a dupe
            // if we tried to make it again.
            mgr = new ModuleManager(installer, ev);
            mgr.mutexPrivileged().enterWriteAccess();
            try {
                // Now try it again, with a different locale this time:
                Locale.setDefault(new Locale("cs", "CZ"));
                Module m = mgr.createFixed(mani, null, l);
                // Note Unicode values in the bundle.
                assertEquals("cs_CZ display name", "Modul s lokalizovan\u00FDm manifestem", m.getDisplayName());
                assertEquals("cs_CZ bundle main attr", "v\u00FDznam #1", m.getLocalizedAttribute("some-other-key"));
                assertEquals("cs_CZ bundle sub attr", "v\u00FDznam #2", m.getLocalizedAttribute("locmani/something.txt/other-key"));
                // These are not translated, see that they fall back to "default" locale:
                assertEquals("cs_CZ bundle main attr untrans", "value #7", m.getLocalizedAttribute("other-untrans"));
                assertEquals("cs_CZ bundle sub attr untrans", "value #8", m.getLocalizedAttribute("locmani/something.txt/other-untrans"));
                // The manifest cannot hold non-ASCII characters.
                assertEquals("cs_CZ manifest main attr", "vyznam #3", m.getLocalizedAttribute("some-key"));
                assertEquals("cs_CZ manifest sub attr", "vyznam #4", m.getLocalizedAttribute("locmani/something.txt/key"));
                // Also not translated:
                assertEquals("cs_CZ manifest main attr untrans", "value #5", m.getLocalizedAttribute("untrans"));
                assertEquals("cs_CZ manifest sub attr untrans", "value #6", m.getLocalizedAttribute("locmani/something.txt/untrans"));
            } finally {
                mgr.mutexPrivileged().exitWriteAccess();
            }
        } finally {
            Locale.setDefault(starting);
        }
    }
    
    // #9273: test that modules/patches/<<code-name-dashes>>/*.jar function as patches
    public void testModulePatches() throws Exception {
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m = mgr.create(new File(jars, "patchable.jar"), null, false, false, false);
            mgr.enable(m);
            Class c = m.getClassLoader().loadClass("pkg.subpkg.A");
            Field f = c.getField("val");
            Object o = c.newInstance();
            assertEquals(25, f.getInt(o));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    public void testSimpleProvReq() throws Exception {
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "prov-foo.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "req-foo.jar"), null, false, false, false);
            assertEquals(Collections.singletonList("foo"), Arrays.asList(m1.getProvides()));
            assertEquals(Collections.EMPTY_LIST, Arrays.asList(m2.getProvides()));
            assertEquals(Collections.EMPTY_SET, m1.getDependencies());
            assertEquals(Dependency.create(Dependency.TYPE_REQUIRES, "foo"), m2.getDependencies());
            Map modulesByName = new HashMap();
            modulesByName.put(m1.getCodeNameBase(), m1);
            modulesByName.put(m2.getCodeNameBase(), m2);
            Map providersOf = new HashMap();
            providersOf.put("foo", Collections.singleton(m1));
            List m1m2 = Arrays.asList(new Module[] {m1, m2});
            List m2m1 = Arrays.asList(new Module[] {m2, m1});
            Map deps = Util.moduleDependencies(m1m2, modulesByName, providersOf);
            assertNull(deps.get(m1));
            assertEquals(Collections.singletonList(m1), deps.get(m2));
            assertEquals(m2m1, Utilities.topologicalSort(m1m2, deps));
            assertEquals(m2m1, Utilities.topologicalSort(m2m1, deps));
            Set m1PlusM2 = new HashSet(); // Set<Module>
            m1PlusM2.add(m1);
            m1PlusM2.add(m2);
            List toEnable = mgr.simulateEnable(m1PlusM2);
            assertEquals("correct result of simulateEnable", Arrays.asList(new Module[] {m1, m2}), toEnable);
            toEnable = mgr.simulateEnable(Collections.singleton(m2));
            assertEquals("correct result of simulateEnable #2", Arrays.asList(new Module[] {m1, m2}), toEnable);
            mgr.enable(m1PlusM2);
            assertEquals(Arrays.asList(new String[] {
                "prepare",
                "prepare",
                "load"
            }), installer.actions);
            assertEquals(Arrays.asList(new Object[] {
                m1,
                m2,
                Arrays.asList(new Module[] {m1, m2})
            }), installer.args);
            Class testclazz = Class.forName("org.prov_foo.Clazz", true, m1.getClassLoader());
            try {
                Class.forName("org.prov_foo.Clazz", true, m2.getClassLoader());
                fail("Should not be able to access classes due to prov-req deps only");
            } catch (ClassNotFoundException cnfe) {
                // OK, good.
            }
            installer.clear();
            List toDisable = mgr.simulateDisable(Collections.singleton(m1));
            assertEquals("correct result of simulateDisable", Arrays.asList(new Module[] {m2, m1}), toDisable);
            toDisable = mgr.simulateDisable(m1PlusM2);
            assertEquals("correct result of simulateDisable #2", Arrays.asList(new Module[] {m2, m1}), toDisable);
            mgr.disable(m1PlusM2);
            assertFalse(m1.isEnabled());
            assertFalse(m2.isEnabled());
            assertEquals(Arrays.asList(new String[] {
                "unload",
                "dispose",
                "dispose"
            }), installer.actions);
            assertEquals(Arrays.asList(new Object[] {
                Arrays.asList(new Module[] {m2, m1}),
                m2,
                m1
            }), installer.args);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    public void testProvReqCycles() throws Exception {
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "prov-foo-req-bar.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "prov-bar-req-foo.jar"), null, false, false, false);
            assertEquals("m1 cannot be installed because of m2",
                Dependency.create(Dependency.TYPE_REQUIRES, "bar"),
                m1.getProblems());
            assertEquals("m2 cannot be installed because of m1",
                Dependency.create(Dependency.TYPE_REQUIRES, "foo"),
                m2.getProblems());
            assertEquals("neither m1 nor m2 can be installed",
                Collections.EMPTY_LIST,
                    mgr.simulateEnable(new HashSet(Arrays.asList(new Module[] {m1, m2}))));
            mgr.delete(m2);
            Module m3 = mgr.create(new File(jars, "prov-bar-dep-cyclic.jar"), null, false, false, false);
            assertEquals("m1 cannot be installed because of m3",
                Dependency.create(Dependency.TYPE_REQUIRES, "bar"),
                m1.getProblems());
            assertEquals("m3 cannot be installed because of m1",
                Dependency.create(Dependency.TYPE_MODULE, "prov_foo_req_bar"),
                m3.getProblems());
            assertEquals("neither m1 nor m3 can be installed",
                Collections.EMPTY_LIST,
                    mgr.simulateEnable(new HashSet(Arrays.asList(new Module[] {m1, m3}))));
            m2 = mgr.create(new File(jars, "prov-bar-req-foo.jar"), null, false, false, false);
            assertEquals("m2 cannot be installed because of m1",
                Dependency.create(Dependency.TYPE_REQUIRES, "foo"),
                m2.getProblems());
            Module m4 = mgr.create(new File(jars, "prov-foo.jar"), null, false, false, false);
            assertEquals("m2 is OK with m4 here",
                Collections.EMPTY_SET,
                m2.getProblems());
            assertEquals("m2 and m4 can be enabled together",
                Arrays.asList(new Module[] {m4, m2}),
                mgr.simulateEnable(Collections.singleton(m2)));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    public void testMultipleProvs() throws Exception {
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "prov-foo.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "prov-foo-bar.jar"), null, false, false, false);
            Module m3 = mgr.create(new File(jars, "req-foo.jar"), null, false, false, false);
            Set m123 = new HashSet(Arrays.asList(new Module[] {m1, m2, m3}));
            List toEnable = mgr.simulateEnable(Collections.singleton(m3));
            // Note order of first two items in toEnable is indeterminate.
            assertEquals("From start, turn on all providers", m123, new HashSet(toEnable));
            assertEquals("m3 last", m3, toEnable.get(2));
            assertEquals("Could request them all together too", m123, new HashSet(mgr.simulateEnable(m123)));
            List m13 = Arrays.asList(new Module[] {m1, m3});
            assertEquals("Or just m1 + m3", m13, mgr.simulateEnable(new HashSet(m13)));
            List m23 = Arrays.asList(new Module[] {m2, m3});
            assertEquals("Or just m2 + m3", m23, mgr.simulateEnable(new HashSet(m23)));
            mgr.enable(m123);
            assertTrue(m1.isEnabled());
            assertTrue(m2.isEnabled());
            assertTrue(m3.isEnabled());
            assertEquals("Can turn off one provider",
                Collections.singletonList(m1),
                mgr.simulateDisable(Collections.singleton(m1)));
            Set m12 = new HashSet(Arrays.asList(new Module[] {m1, m2}));
            assertEquals("Can't turn off both providers",
                m123,
                new HashSet(mgr.simulateDisable(m12)));
            mgr.disable(m1);
            assertFalse(m1.isEnabled());
            assertTrue(m2.isEnabled());
            assertTrue(m3.isEnabled());
            List m32 = Arrays.asList(new Module[] {m3, m2});
            assertEquals("Can't turn off last provider",
                m32,
                mgr.simulateDisable(Collections.singleton(m2)));
            mgr.disable(new HashSet(m32));
            assertFalse(m1.isEnabled());
            assertFalse(m2.isEnabled());
            assertFalse(m3.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    public void testProvReqUnsatisfiable() throws Exception {
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "prov-foo.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "prov-foo-req-bar.jar"), null, false, false, false);
            Module m3 = mgr.create(new File(jars, "req-foo.jar"), null, false, false, false);
            assertEquals(Collections.EMPTY_SET, m1.getProblems());
            assertEquals(Collections.EMPTY_SET, m3.getProblems());
            assertEquals(Dependency.create(Dependency.TYPE_REQUIRES, "bar"), m2.getProblems());
            List m13 = Arrays.asList(new Module[] {m1, m3});
            assertEquals(m13, mgr.simulateEnable(Collections.singleton(m3)));
            mgr.enable(new HashSet(m13));
            assertTrue(m1.isEnabled());
            assertFalse(m2.isEnabled());
            assertTrue(m3.isEnabled());
            Module m4 = mgr.create(new File(jars, "prov-foo-bar.jar"), null, false, false, false);
            assertEquals(Collections.EMPTY_SET, m2.getProblems());
            mgr.disable(new HashSet(m13));
            assertFalse(m3.isEnabled());
            List toEnable = mgr.simulateEnable(Collections.singleton(m3));
            assertEquals(4, toEnable.size());
            assertTrue(toEnable.get(0) != m2);
            assertTrue(toEnable.get(0) != m3);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    public void testMultipleReqs() throws Exception {
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "prov-foo.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "prov-baz.jar"), null, false, false, false);
            Module m3 = mgr.create(new File(jars, "req-foo-baz.jar"), null, false, false, false);
            Set m123 = new HashSet(Arrays.asList(new Module[] {m1, m2, m3}));
            assertEquals(m123, new HashSet(mgr.simulateEnable(Collections.singleton(m3))));
            mgr.enable(m123);
            assertEquals(Arrays.asList(new Module[] {m3, m1}), mgr.simulateDisable(Collections.singleton(m1)));
            assertEquals(Arrays.asList(new Module[] {m3, m2}), mgr.simulateDisable(Collections.singleton(m2)));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    public void testEagerReq() throws Exception {
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "prov-foo.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "prov-baz.jar"), null, false, false, false);
            Module m3 = mgr.create(new File(jars, "req-foo-baz.jar"), null, false, false, true);
            assertEquals(Collections.singletonList(m1),
                mgr.simulateEnable(Collections.singleton(m1)));
            assertEquals(Collections.singletonList(m2),
                mgr.simulateEnable(Collections.singleton(m2)));
            Set m12 = new HashSet(Arrays.asList(new Module[] {m1, m2}));
            Set m123 = new HashSet(Arrays.asList(new Module[] {m1, m2, m3}));
            assertEquals(m123, new HashSet(mgr.simulateEnable(m12)));
            mgr.enable(m12);
            assertTrue(m3.isEnabled());
            assertEquals(Arrays.asList(new Module[] {m3, m1}),
                mgr.simulateDisable(Collections.singleton(m1)));
            assertEquals(Arrays.asList(new Module[] {m3, m2}),
                mgr.simulateDisable(Collections.singleton(m2)));
            assertEquals(m123,
                new HashSet(mgr.simulateDisable(m12)));
            mgr.disable(m12);
            assertFalse(m3.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    public void testAutoloadProv() throws Exception {
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "prov-foo.jar"), null, false, true, false);
            Module m2 = mgr.create(new File(jars, "req-foo.jar"), null, false, false, false);
            assertEquals(Arrays.asList(new Module[] {m1, m2}),
                mgr.simulateEnable(Collections.singleton(m2)));
            mgr.enable(m2);
            assertTrue(m1.isEnabled());
            assertEquals(Arrays.asList(new Module[] {m2, m1}),
                mgr.simulateDisable(Collections.singleton(m2)));
            mgr.disable(m2);
            assertFalse(m1.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    public void testWeirdRecursion() throws Exception {
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            //Module m1 = mgr.create(new File(jars, "prov-foo.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "prov-bar-req-foo.jar"), null, false, true, false);
            Module m3 = mgr.create(new File(jars, "prov-foo-bar.jar"), null, false, false, false);
            Module m4 = mgr.create(new File(jars, "prov-foo-req-bar.jar"), null, false, false, true);
            assertEquals("m2 should not be enabled - m4 might ask for it but m3 already has bar",
                new HashSet(Arrays.asList(new Module[] {m3, m4})),
                new HashSet(mgr.simulateEnable(Collections.singleton(m3))));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    public void testLackOfOrderSensitivity() throws Exception {
        String[] moduleNames = new String[] {
            "simple-module.jar",
            "depends-on-simple-module.jar",
            "dep-on-dep-on-simple.jar",
            "prov-foo.jar",
            "prov-baz.jar",
            "prov-foo-bar.jar",
            "req-foo.jar",
            "req-foo-baz.jar",
            "prov-bar-req-foo.jar",
            "prov-foo-req-bar.jar",
        };
        // Never make any of the following eager:
        Set noDepsNames = new HashSet(Arrays.asList(new String[] {
            "simple-module.jar",
            "prov-foo.jar",
            "prov-baz.jar",
            "prov-foo-bar.jar",
        }));
        List freeModules = new ArrayList(Arrays.asList(moduleNames));
        int count = 100; // # of things to do in order
        Random r = new Random(count * 17 + 113);
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            int i = 0;
            while (i < count) {
                Util.err.log("testLackOfOrderSensitivity round #" + i);
                switch (r.nextInt(11)) {
                case 0:
                case 1:
                case 2:
                    Util.err.log("Add a regular module");
                    if (!freeModules.isEmpty()) {
                        String name = (String)freeModules.remove(r.nextInt(freeModules.size()));
                        mgr.create(new File(jars, name), null, false, false, false);
                        i++;
                    }
                    break;
                case 3:
                    Util.err.log("Add an autoload");
                    if (!freeModules.isEmpty()) {
                        String name = (String)freeModules.remove(r.nextInt(freeModules.size()));
                        mgr.create(new File(jars, name), null, false, true, false);
                        i++;
                    }
                    break;
                case 4:
                    Util.err.log("Add an eager module");
                    if (!freeModules.isEmpty()) {
                        String name = (String)freeModules.remove(r.nextInt(freeModules.size()));
                        if (!noDepsNames.contains(name)) {
                            Module m = mgr.create(new File(jars, name), null, false, false, true);
                            i++;
                        }
                    }
                    break;
                case 5:
                case 6:
                    Util.err.log("Remove a disabled module");
                    List disabled = new ArrayList(moduleNames.length);
                    Iterator it = mgr.getModules().iterator();
                    while (it.hasNext()) {
                        Module m = (Module)it.next();
                        if (!m.isEnabled()) {
                            disabled.add(m);
                        }
                    }
                    if (!disabled.isEmpty()) {
                        Module m = (Module)disabled.get(r.nextInt(disabled.size()));
                        mgr.delete(m);
                        freeModules.add(m.getJarFile().getName());
                        i++;
                    }
                    break;
                case 7:
                case 8:
                    Util.err.log("Enable some set of modules");
                    List candidates = new ArrayList(moduleNames.length);
                    it = mgr.getModules().iterator();
                    while (it.hasNext()) {
                        Module m = (Module)it.next();
                        if (!m.isEnabled() && !m.isAutoload() && !m.isEager() && r.nextBoolean()) {
                            candidates.add(m);
                        }
                    }
                    if (!candidates.isEmpty()) {
                        Collections.shuffle(candidates, r);
                        Set candidatesSet = new LinkedHashSet(candidates);
                        assertEquals("OrderPreservingSet works", candidates, new ArrayList(candidatesSet));
                        //dumpState(mgr);
                        //System.err.println("will try to enable: " + candidates);
                        List toEnable1 = mgr.simulateEnable(candidatesSet);
                        //System.err.println("Enabling  " + candidates + " ->\n          " + toEnable1);
                        Collections.shuffle(candidates, r);
                        List toEnable2 = mgr.simulateEnable(new LinkedHashSet(candidates));
                        Set s1 = new HashSet(toEnable1);
                        Set s2 = new HashSet(toEnable2);
                        assertEquals("Order preserved", s1, s2);
                        it = s1.iterator();
                        while (it.hasNext()) {
                            Module m = (Module)it.next();
                            if (m.isAutoload() || m.isEager()) {
                                it.remove();
                            }
                        }
                        mgr.enable(s1);
                        i++;
                    }
                    break;
                case 9:
                case 10:
                    Util.err.log("Disable some set of modules");
                    candidates = new ArrayList(moduleNames.length);
                    it = mgr.getModules().iterator();
                    while (it.hasNext()) {
                        Module m = (Module)it.next();
                        if (m.isEnabled() && !m.isAutoload() && !m.isEager() && r.nextBoolean()) {
                            candidates.add(m);
                        }
                    }
                    if (!candidates.isEmpty()) {
                        Collections.shuffle(candidates, r);
                        //dumpState(mgr);
                        List toDisable1 = mgr.simulateDisable(new LinkedHashSet(candidates));
                        //System.err.println("Disabling " + candidates + " ->\n          " + toDisable1);
                        Collections.shuffle(candidates, r);
                        //System.err.println("candidates #2: " + candidates);
                        List toDisable2 = mgr.simulateDisable(new LinkedHashSet(candidates));
                        Set s1 = new HashSet(toDisable1);
                        Set s2 = new HashSet(toDisable2);
                        assertEquals("Order preserved", s1, s2);
                        it = s1.iterator();
                        while (it.hasNext()) {
                            Module m = (Module)it.next();
                            if (m.isAutoload() || m.isEager()) {
                                it.remove();
                            }
                        }
                        mgr.disable(s1);
                        i++;
                    }
                    break;
                default:
                    throw new IllegalStateException();
                }
            }
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    /*
    private static void dumpState(ModuleManager mgr) {
        SortedSet modules = new TreeSet(Util.displayNameComparator());
        modules.addAll(mgr.getModules());
        System.err.print("State:");
        Iterator it = modules.iterator();
        while (it.hasNext()) {
            Module m = (Module)it.next();
            System.err.print(" " + m.getCodeNameBase());
            if (m.isAutoload()) {
                System.err.print(" (autoload, ");
            } else if (m.isEager()) {
                System.err.print(" (eager, ");
            } else {
                System.err.print(" (normal, ");
            }
            if (m.isEnabled()) {
                System.err.print("on)");
            } else {
                System.err.print("off)");
            }
        }
        System.err.println();
    }
     */
    
    public void testRelVersRanges() throws Exception {
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module base = mgr.create(new File(jars, "rel-ver-2.jar"), null, false, false, false);
            String[] depNames = new String[] {
                "dep-on-relvertest-1.jar", // 0
                "dep-on-relvertest-1-2.jar", // 1
                "dep-on-relvertest-2.jar", // 2
                "dep-on-relvertest-2-3.jar", // 3
                "dep-on-relvertest-2-3-late.jar", // 4
                "dep-on-relvertest-2-impl.jar", // 5
                "dep-on-relvertest-2-impl-wrong.jar", // 6
                "dep-on-relvertest-2-late.jar", // 7
                "dep-on-relvertest-3-4.jar", // 8
                "dep-on-relvertest-some.jar", // 9
            };
            Module[] deps = new Module[depNames.length];
            for (int i = 0; i < deps.length; i++) {
                deps[i] = mgr.create(new File(jars, depNames[i]), null, false, false, false);
            }
            Set all = new HashSet();
            all.add(base);
            all.addAll(Arrays.asList(deps));
            Set ok = new HashSet();
            ok.add(base);
            // 0 - too early
            ok.add(deps[1]);
            ok.add(deps[2]);
            ok.add(deps[3]);
            // 4 - too late
            ok.add(deps[5]);
            // 6 - wrong impl version
            // 7 - too late
            // 8 - too late
            // 9 - must give some rel vers, else ~ -1
            assertEquals(ok, new HashSet(mgr.simulateEnable(all)));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    public void testDisableAgainstRelVersRange() throws Exception {
        // #41449: org.openidex.util/3 disabled improperly when disable module w/ dep on org.openide.util/2-3
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module base = mgr.create(new File(jars, "rel-ver-2.jar"), null, false, true, false);
            Module dep1 = mgr.create(new File(jars, "dep-on-relvertest-2.jar"), null, false, false, false);
            Module dep2 = mgr.create(new File(jars, "dep-on-relvertest-1-2-nospec.jar"), null, false, false, false);
            Set/*<Module>*/ all = new HashSet();
            all.add(dep1);
            all.add(dep2);
            mgr.enable(all);
            all.add(base);
            assertEquals("turn on autoload w/ both deps OK", all, mgr.getEnabledModules());
            Set/*<Module>*/ dep2only = Collections.singleton(dep2);
            assertEquals("intend to disable only dep2", dep2only, new HashSet(mgr.simulateDisable(dep2only)));
            mgr.disable(dep2only);
            all.remove(dep2);
            assertEquals("removed just dep2, not autoload used by dep1", all, mgr.getEnabledModules());
            mgr.disable(Collections.singleton(dep1));
            assertEquals("now all gone", Collections.EMPTY_SET, mgr.getEnabledModules());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    /** Test #21114: after deleting a module, its JARs are released.
     * Would probably always pass on Unix, but on Windows it matters.
     */
    public void testModuleDeletion() throws Exception {
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        
        clearWorkDir();
        File jar = new File(getWorkDir(), "copy-of-simple-module.jar");
        copy(new File(jars, "simple-module.jar"), jar);
        
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m = mgr.create(jar, null, false, false, false);
            mgr.enable(m);
            Class c = m.getClassLoader().loadClass("org.foo.Something");
            URL u = m.getClassLoader().getResource("org/foo/Something.class");
            URLConnection uc = u.openConnection();
            assertTrue("using JarURLConnection", uc instanceof JarURLConnection);
            uc.connect();
            mgr.disable(m);
            mgr.delete(m);
            
            WeakReference refC = new WeakReference (c);
            WeakReference refU = new WeakReference (u);
            WeakReference refUC = new WeakReference (uc);
            
            c = null;
            u = null;
            uc = null;
            
            assertGC ("Module class can go away", refC);
            assertGC ("Module url", refU);
            assertGC ("Module connection ", refUC);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        
        assertTrue("could delete JAR file", jar.delete());
    }
    
    /** Test #20663: the context classloader is set on all threads
     * according to the system classloader.
     */
    public void testContextClassLoader() throws Exception {
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        final ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        // Make sure created threads do not die.
        final Object sleepForever = "sleepForever";
        try {
            final Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "depends-on-simple-module.jar"), null, false, false, false);
            ClassLoader l1 = mgr.getClassLoader();
            assertEquals(l1, Thread.currentThread().getContextClassLoader());
            mgr.enable(m1);
            ClassLoader l2 = mgr.getClassLoader();
            assertTrue(l1 == l2);
            assertEquals(l2, Thread.currentThread().getContextClassLoader());
            mgr.enable(m2);
            ClassLoader l3 = mgr.getClassLoader();
            assertTrue(l1 == l3);
            assertEquals(l3, Thread.currentThread().getContextClassLoader());
            mgr.disable(m2);
            ClassLoader l4 = mgr.getClassLoader();
            assertTrue(l1 != l4);
            assertEquals(l4, Thread.currentThread().getContextClassLoader());
            final Thread[] t23 = new Thread[2];
            final ClassLoader[] lx = new ClassLoader[] {new URLClassLoader(new URL[0])};
            // Make sure t1 runs to completion, though.
            final Object finishT1 = "finishT1";
            Thread t1 = new Thread("custom thread #1") {
                public void run() {
                    synchronized (finishT1) {
                        t23[0] = new Thread("custom thread #2") {
                            public void run() {
                                synchronized (sleepForever) {
                                    try {
                                        sleepForever.wait();
                                    } catch (InterruptedException ie) {
                                        throw new Error(ie.toString());
                                    }
                                }
                            }
                        };
                        t23[0].start();
                        Thread.currentThread().setContextClassLoader(lx[0]);
                        mgr.disable(m1);
                        t23[1] = new Thread("custom thread #3") {
                            public void run() {
                                synchronized (sleepForever) {
                                    try {
                                        sleepForever.wait();
                                    } catch (InterruptedException ie) {
                                        throw new Error(ie.toString());
                                    }
                                }
                            }
                        };
                        t23[1].start();
                        finishT1.notify();
                    }
                    synchronized (sleepForever) {
                        try {
                            sleepForever.wait();
                        } catch (InterruptedException ie) {
                            throw new Error(ie.toString());
                        }
                    }
                }
            };
            t1.start();
            synchronized (finishT1) {
                if (t23[1] == null) {
                    finishT1.wait();
                    assertNotNull(t23[1]);
                }
            }
            assertFalse(m1.isEnabled());
            ClassLoader l5 = mgr.getClassLoader();
            assertTrue(l1 != l5);
            assertTrue(l4 != l5);
            assertEquals(l5, Thread.currentThread().getContextClassLoader());
            // It had a special classloader when we changed modules.
            assertTrue(t1.isAlive());
            assertEquals(lx[0], t1.getContextClassLoader());
            // It was created before the special classloader.
            assertTrue(t23[0].isAlive());
            assertEquals(l5, t23[0].getContextClassLoader());
            // It was created after and should have inherited the special classloader.
            assertTrue(t23[1].isAlive());
            assertEquals(lx[0], t23[1].getContextClassLoader());
            mgr.enable(m1);
            mgr.disable(m1);
            ClassLoader l6 = mgr.getClassLoader();
            assertTrue(l1 != l6);
            assertTrue(l4 != l6);
            assertTrue(l5 != l6);
            assertEquals(l6, Thread.currentThread().getContextClassLoader());
            assertEquals(lx[0], t1.getContextClassLoader());
            assertEquals(l6, t23[0].getContextClassLoader());
            assertEquals(lx[0], t23[1].getContextClassLoader());
        } finally {
            synchronized (sleepForever) {
                sleepForever.notifyAll();
            }
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    /** Make sure classloaders do not overlap.
     * @see "#24996"
     */
    public void testDependOnTwoFixedModules() throws Exception {
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            File j1 = new File(jars, "simple-module.jar");
            File j2 = new File(jars, "depends-on-simple-module.jar");
            File j3 = new File(jars, "dep-on-two-modules.jar");
            URLClassLoader l = new URLClassLoader(new URL[] {j1.toURL(), j2.toURL()});
            Manifest mani1, mani2;
            JarFile j = new JarFile(j1);
            try {
                mani1 = j.getManifest();
            } finally {
                j.close();
            }
            j = new JarFile(j2);
            try {
                mani2 = j.getManifest();
            } finally {
                j.close();
            }
            Module m1 = mgr.createFixed(mani1, null, l);
            Module m2 = mgr.createFixed(mani2, null, l);
            Module m3 = mgr.create(j3, null, false, false, false);
            mgr.enable(new HashSet(Arrays.asList(new Module[] {m1, m2, m3})));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    /** Test exporting selected packages to clients.
     * @see "#19621"
     */
    public void testPackageExports() throws Exception {
        ModuleManager mgr = new ModuleManager(new FakeModuleInstaller(), new FakeEvents());
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "api-mod-export-all.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "uses-api-simple-dep.jar"), null, false, false, false);
            mgr.enable(m1);
            mgr.enable(m2);
            m2.getClassLoader().loadClass("usesapi.UsesPublicClass").newInstance();
            m2.getClassLoader().loadClass("usesapi.UsesImplClass").newInstance();
            mgr.disable(m2);
            mgr.disable(m1);
            mgr.delete(m2);
            mgr.delete(m1);
            m1 = mgr.create(new File(jars, "api-mod-export-none.jar"), null, false, false, false);
            m2 = mgr.create(new File(jars, "uses-api-simple-dep.jar"), null, false, false, false);
            mgr.enable(m1);
            mgr.enable(m2);
            try {
                m2.getClassLoader().loadClass("usesapi.UsesPublicClass").newInstance();
                fail();
            } catch (NoClassDefFoundError e) {}
            try {
                m2.getClassLoader().loadClass("usesapi.UsesImplClass").newInstance();
                fail();
            } catch (NoClassDefFoundError e) {}
            assertNotNull(mgr.getClassLoader().getResource("usesapi/UsesImplClass.class"));
            assertNotNull(mgr.getClassLoader().getResource("org/netbeans/api/foo/PublicClass.class"));
            assertNotNull(mgr.getClassLoader().getResource("org/netbeans/modules/foo/ImplClass.class"));
            mgr.disable(m2);
            mgr.disable(m1);
            mgr.delete(m2);
            mgr.delete(m1);
            m1 = mgr.create(new File(jars, "api-mod-export-none.jar"), null, false, false, false);
            m2 = mgr.create(new File(jars, "uses-api-spec-dep.jar"), null, false, false, false);
            mgr.enable(m1);
            mgr.enable(m2);
            try {
                m2.getClassLoader().loadClass("usesapi.UsesPublicClass").newInstance();
                fail();
            } catch (NoClassDefFoundError e) {}
            try {
                m2.getClassLoader().loadClass("usesapi.UsesImplClass").newInstance();
                fail();
            } catch (NoClassDefFoundError e) {}
            mgr.disable(m2);
            mgr.disable(m1);
            mgr.delete(m2);
            mgr.delete(m1);
            m1 = mgr.create(new File(jars, "api-mod-export-none.jar"), null, false, false, false);
            m2 = mgr.create(new File(jars, "uses-api-impl-dep.jar"), null, false, false, false);
            mgr.enable(m1);
            mgr.enable(m2);
            m2.getClassLoader().loadClass("usesapi.UsesPublicClass").newInstance();
            m2.getClassLoader().loadClass("usesapi.UsesImplClass").newInstance();
            mgr.disable(m2);
            mgr.disable(m1);
            mgr.delete(m2);
            mgr.delete(m1);
            m1 = mgr.create(new File(jars, "api-mod-export-api.jar"), null, false, false, false);
            m2 = mgr.create(new File(jars, "uses-api-simple-dep.jar"), null, false, false, false);
            assertEquals("api-mod-export-api.jar can be enabled", Collections.EMPTY_SET, m1.getProblems());
            mgr.enable(m1);
            assertEquals("uses-api-simple-dep.jar can be enabled", Collections.EMPTY_SET, m2.getProblems());
            mgr.enable(m2);
            m2.getClassLoader().loadClass("usesapi.UsesPublicClass").newInstance();
            try {
                m2.getClassLoader().loadClass("usesapi.UsesImplClass").newInstance();
                fail();
            } catch (NoClassDefFoundError e) {}
            mgr.disable(m2);
            mgr.disable(m1);
            mgr.delete(m2);
            mgr.delete(m1);
            m1 = mgr.create(new File(jars, "api-mod-export-api.jar"), null, false, false, false);
            m2 = mgr.create(new File(jars, "uses-api-spec-dep.jar"), null, false, false, false);
            mgr.enable(m1);
            mgr.enable(m2);
            m2.getClassLoader().loadClass("usesapi.UsesPublicClass").newInstance();
            try {
                m2.getClassLoader().loadClass("usesapi.UsesImplClass").newInstance();
                fail();
            } catch (NoClassDefFoundError e) {}
            mgr.disable(m2);
            mgr.disable(m1);
            mgr.delete(m2);
            mgr.delete(m1);
            m1 = mgr.create(new File(jars, "api-mod-export-api.jar"), null, false, false, false);
            m2 = mgr.create(new File(jars, "uses-api-impl-dep.jar"), null, false, false, false);
            mgr.enable(m1);
            mgr.enable(m2);
            m2.getClassLoader().loadClass("usesapi.UsesPublicClass").newInstance();
            m2.getClassLoader().loadClass("usesapi.UsesImplClass").newInstance();
            mgr.disable(m2);
            mgr.disable(m1);
            mgr.delete(m2);
            mgr.delete(m1);
            // XXX test use of .** to export packages recursively
            // XXX test misparsing of malformed export lines
            // XXX test exporting of >1 package from one module (comma-separated)
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    /** Test that package exports, and package/classloader use generally, is not
     * transitively exported from modules - that you need to declare an explicit
     * module dependency on every module from which you expect to load classes
     * or resources, even if you are already declaring a dependency on an inter-
     * mediate module which has such a dependency.
     * @see "#27853"
     */
    public void testIndirectPackageExports() throws Exception {
        ModuleManager mgr = new ModuleManager(new FakeModuleInstaller(), new FakeEvents());
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "api-mod-export-api.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "uses-and-exports-api.jar"), null, false, false, false);
            Module m3 = mgr.create(new File(jars, "uses-api-transitively.jar"), null, false, false, false);
            Module m4 = mgr.create(new File(jars, "uses-api-directly.jar"), null, false, false, false);
            assertEquals("api-mod-export-api.jar had no problems", Collections.EMPTY_SET, m1.getProblems());
            assertEquals("uses-and-exports-api.jar had no problems", Collections.EMPTY_SET, m2.getProblems());
            assertEquals("uses-api-transitively.jar had no problems", Collections.EMPTY_SET, m3.getProblems());
            assertEquals("uses-api-directly.jar had no problems", Collections.EMPTY_SET, m4.getProblems());
            mgr.enable(new HashSet(Arrays.asList(new Module[] {m1, m2, m3, m4})));
            m4.getClassLoader().loadClass("usesapitrans.UsesDirectAPI").newInstance();
            m4.getClassLoader().loadClass("usesapitrans.UsesIndirectAPI").newInstance();
            m3.getClassLoader().loadClass("usesapitrans.UsesDirectAPI").newInstance();
            try {
                m3.getClassLoader().loadClass("usesapitrans.UsesIndirectAPI").newInstance();
                fail("Should not be able to use a transitive API class with no direct dependency");
            } catch (NoClassDefFoundError e) {}
            mgr.disable(new HashSet(Arrays.asList(new Module[] {m1, m2, m3, m4})));
            mgr.delete(m4);
            mgr.delete(m3);
            mgr.delete(m2);
            mgr.delete(m1);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testPublicPackagesCanBeExportedToSelectedFriendsOnlyIssue54123 () throws Exception {
        ModuleManager mgr = new ModuleManager(new FakeModuleInstaller(), new FakeEvents());
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "api-mod-export-friend.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "uses-api-friend.jar"), null, false, false, false);
            Module m3 = mgr.create(new File(jars, "uses-and-exports-api.jar"), null, false, false, false);
            Module m4 = mgr.create(new File(jars, "uses-api-directly.jar"), null, false, false, false);
            Module m5 = mgr.create(new File(jars, "uses-api-impl-dep-for-friends.jar"), null, false, false, false);
            assertEquals("api-mod-export-api.jar had no problems", Collections.EMPTY_SET, m1.getProblems());
            assertEquals("uses-api-friend.jar had no problems", Collections.EMPTY_SET, m2.getProblems());
            assertEquals("uses-and-exports-api.jar had no problems", Collections.EMPTY_SET, m3.getProblems());
            assertEquals("uses-api-directly.jar had no problems", Collections.EMPTY_SET, m4.getProblems());
            assertEquals("uses-api-impl-dep-for-friends.jar had no problems", Collections.EMPTY_SET, m5.getProblems());
            mgr.enable(new HashSet(Arrays.asList(new Module[] {m1, m2, m3, m4, m5})));
            m2.getClassLoader().loadClass("usesapi.UsesPublicClass").newInstance();
            try {
                m2.getClassLoader().loadClass("usesapi.UsesImplClass").newInstance();
                fail ("Even friends modules cannot access implementation classes");
            } catch (NoClassDefFoundError ex) {
                // ok
            }
            
            try {
                m4.getClassLoader().loadClass("usesapi.UsesPublicClass").newInstance();
                fail ("m4 is not friend and should not be allowed to load the class");
            } catch (NoClassDefFoundError ex) {
                // ok
            }
            try {
                m4.getClassLoader().loadClass("usesapi.UsesImplClass").newInstance();
                fail ("m4 is not friend and should not be allowed to load the implementation either");
            } catch (NoClassDefFoundError ex) {
                // ok
            }
            try {
                m5.getClassLoader().loadClass("usesapi.UsesPublicClass").newInstance();
            } catch (NoClassDefFoundError e) {
                fail("m5 has an implementation dependency and has not been allowed to load the public class");
            }
            try {
                m5.getClassLoader().loadClass("usesapi.UsesImplClass").newInstance();
            } catch (NoClassDefFoundError e) {
                fail("m5 has an implementation dependency and has not been allowed to load the imlpementation class");
            }
            
            mgr.disable(new HashSet(Arrays.asList(new Module[] {m1, m2, m3, m4, m5})));
            mgr.delete(m5);
            mgr.delete(m4);
            mgr.delete(m3);
            mgr.delete(m2);
            mgr.delete(m1);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    public void testModuleInterdependencies() throws Exception {
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "depends-on-simple-module.jar"), null, false, false, false);
            Module m3 = mgr.create(new File(jars, "dep-on-dep-on-simple.jar"), null, false, false, false);
            Set m1m2 = new HashSet(Arrays.asList(new Module[] {m1, m2}));
            Set m2m3 = new HashSet(Arrays.asList(new Module[] {m2, m3}));
            assertEquals(Collections.EMPTY_SET, mgr.getModuleInterdependencies(m1, false, false));
            assertEquals(Collections.EMPTY_SET, mgr.getModuleInterdependencies(m1, false, true));
            assertEquals(Collections.singleton(m2), mgr.getModuleInterdependencies(m1, true, false));
            assertEquals(m2m3, mgr.getModuleInterdependencies(m1, true, true));
            assertEquals(Collections.singleton(m1), mgr.getModuleInterdependencies(m2, false, false));
            assertEquals(Collections.singleton(m1), mgr.getModuleInterdependencies(m2, false, true));
            assertEquals(Collections.singleton(m3), mgr.getModuleInterdependencies(m2, true, false));
            assertEquals(Collections.singleton(m3), mgr.getModuleInterdependencies(m2, true, true));
            assertEquals(Collections.singleton(m2), mgr.getModuleInterdependencies(m3, false, false));
            assertEquals(m1m2, mgr.getModuleInterdependencies(m3, false, true));
            assertEquals(Collections.EMPTY_SET, mgr.getModuleInterdependencies(m3, true, false));
            assertEquals(Collections.EMPTY_SET, mgr.getModuleInterdependencies(m3, true, true));
            m1 = mgr.create(new File(jars, "prov-foo.jar"), null, false, false, false);
            m2 = mgr.create(new File(jars, "prov-foo-bar.jar"), null, false, false, false);
            m3 = mgr.create(new File(jars, "req-foo.jar"), null, false, false, false);
            Module m4 = mgr.create(new File(jars, "prov-baz.jar"), null, false, false, false);
            Module m5 = mgr.create(new File(jars, "req-foo-baz.jar"), null, false, false, false);
            m1m2 = new HashSet(Arrays.asList(new Module[] {m1, m2}));
            assertEquals(m1m2, mgr.getModuleInterdependencies(m3, false, true));
            Set m1m2m4 = new HashSet(Arrays.asList(new Module[] {m1, m2, m4}));
            assertEquals(m1m2m4, mgr.getModuleInterdependencies(m5, false, true));
            Set m3m5 = new HashSet(Arrays.asList(new Module[] {m3, m5}));
            assertEquals(m3m5, mgr.getModuleInterdependencies(m1, true, true));
            // XXX could do more...
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testModuleClassLoaderWasNotReadyWhenTheChangeWasFiredIssue() throws Exception {
        doModuleClassLoaderWasNotReadyWhenTheChangeWasFiredIssue (1);
    }
    public void testGlobalClassLoaderWasNotReadyWhenTheChangeWasFiredIssue() throws Exception {
        doModuleClassLoaderWasNotReadyWhenTheChangeWasFiredIssue (2);
    }
    public void testModuleManagerClassLoaderWasNotReadyWhenTheChangeWasFiredIssue() throws Exception {
        doModuleClassLoaderWasNotReadyWhenTheChangeWasFiredIssue (3);
    }
    
    private void doModuleClassLoaderWasNotReadyWhenTheChangeWasFiredIssue (final int typeOfClassLoader) throws Exception {
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        final ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            final Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, false, false);
            
            class L implements java.beans.PropertyChangeListener {
                ClassLoader l;
                IllegalStateException ex;
                
                public void propertyChange (java.beans.PropertyChangeEvent event) {
                    if (m1.PROP_ENABLED.equals (event.getPropertyName ())) {
                        try {
                            this.l = get ();
                        } catch (IllegalStateException ex) {
                            this.ex = ex;
                        }
                    }
                }
                
                public ClassLoader get () {
                    switch (typeOfClassLoader) {
                        case 1: return m1.getClassLoader ();
                        case 2: return Thread.currentThread ().getContextClassLoader ();
                        case 3: return mgr.getClassLoader ();
                    }
                    fail ("Wrong type: " + typeOfClassLoader);
                    return null;
                }
            }
            L l = new L ();
            m1.addPropertyChangeListener (l);
            
            mgr.enable (m1);
            
            assertTrue ("Successfully enabled", m1.isEnabled ());
            assertEquals ("Classloader at the time of PROP_ENABLED is the same as now", l.get (), l.l);
            assertNull ("No exception thrown", l.ex);
            System.out.println("L: " + l.l);
            m1.removePropertyChangeListener (l);
            
            mgr.disable (m1);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
}
