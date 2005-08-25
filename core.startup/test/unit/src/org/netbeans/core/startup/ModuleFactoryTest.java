/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Nokia. Portions Copyright 2005 Nokia. All Rights Reserved.
 */
package org.netbeans.core.startup;

import java.io.File;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.netbeans.DuplicateException;
import org.netbeans.Events;
import org.netbeans.InvalidException;
import org.netbeans.JarClassLoader;
import org.netbeans.Module;
import org.netbeans.ModuleFactory;
import org.netbeans.ModuleManager;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * These tests verify that the module manager behaves basically the
 * same way with ModuleFactory as without it.
 * @author David Strupl
 */
public class ModuleFactoryTest extends ModuleManagerTest {
    
    static {
        System.setProperty("org.openide.util.Lookup", L.class.getName());
        assertTrue(Lookup.getDefault() instanceof L);
    }
    
    /** Creates a new instance of ModuleFactoryTest */
    public ModuleFactoryTest(String name) {
        super(name);
    }
    
    public static int numberOfStandard = 0;
    public static int numberOfFixed = 0;
    public static boolean testingParentClassloaders = false;
    public static boolean testingDummyModule = false;
    
    public void testFactoryCreatesOurModules() throws Exception {
        // clear the counters before the test!
        numberOfStandard = 0;
        numberOfFixed = 0;
        
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
        assertEquals("Number of standard modules created by our factory ", 1, numberOfStandard);
        assertEquals("Number of fixed modules created by our factory ", 2, numberOfFixed);
    }

    public void testDummyModule() throws Exception {
        ModuleManager mgr = null;
        try {
            testingDummyModule = true;
            FakeModuleInstaller installer = new FakeModuleInstaller();
            FakeEvents ev = new FakeEvents();
            mgr = new ModuleManager(installer, ev);
            mgr.mutexPrivileged().enterWriteAccess();
            
            File j1 = new File(jars, "simple-module.jar");
            Module m1 = mgr.create(j1, null, false, false, false);
            mgr.enable(new HashSet(Arrays.asList(new Module[] {m1})));
            boolean res = false;
            try {
                m1.getClassLoader().loadClass("java.lang.String");
            } catch (ClassNotFoundException x) {
                res = true;
            }
            assertTrue("dummy module with no-op classloader should not find any classes", res);
        } finally {
            testingDummyModule = false;
            if (mgr != null) {
                mgr.mutexPrivileged().exitWriteAccess();
            }
        }
    }

    public void testRemoveBaseClassLoader() throws Exception {
        ModuleManager mgr = null;
        try {
            testingParentClassloaders = true;
            FakeModuleInstaller installer = new FakeModuleInstaller();
            FakeEvents ev = new FakeEvents();
            mgr = new ModuleManager(installer, ev);
            mgr.mutexPrivileged().enterWriteAccess();
            File j1 = new File(jars, "simple-module.jar");
            Module m1 = mgr.create(j1, null, false, false, false);
            mgr.enable(new HashSet(Arrays.asList(new Module[] {m1})));
            boolean res = false;
            try {
                mgr.getClassLoader().loadClass("java.lang.String");
            } catch (ClassNotFoundException x) {
                res = true;
            }
            assertTrue("When removing the base classloader not even JDK classes should be found", res);
        } finally {
            testingParentClassloaders = false;
            if (mgr != null) {
                mgr.mutexPrivileged().exitWriteAccess();
            }
        }
    }
    
    public static final class L extends ProxyLookup {
        public L() {
            super(new Lookup[] {
                Lookups.fixed(new Object[] {
                    new MyModuleFactory()
                }),
            });
        }
    }

    public static final class MyModuleFactory extends ModuleFactory {
        public Module create(File jar, Object history, boolean reloadable, boolean autoload, boolean eager, ModuleManager mgr, Events ev) throws IOException, DuplicateException {
            if (testingDummyModule || testingParentClassloaders) {
                return new DummyModule(mgr, ev, history, reloadable, autoload, eager);
            }
            numberOfStandard++;
            return super.create(jar, history, reloadable, autoload, eager, mgr, ev);
        }
        
        public Module createFixed(Manifest mani, Object history, ClassLoader loader, ModuleManager mgr, Events ev) throws InvalidException, DuplicateException {
            numberOfFixed++;
            return super.createFixed(mani, history, loader, mgr, ev);
        }
        
        public boolean removeBaseClassLoader() {
            if (testingParentClassloaders) {
                return true;
            }
            return super.removeBaseClassLoader();
        }
        public ClassLoader getClasspathDelegateClassLoader(ModuleManager mgr, ClassLoader del) {
            if (testingParentClassloaders) {
                return new NoOpClassLoader();
            }
            return del;
        }
    }
    
    private static final class DummyModule extends Module {
        public DummyModule(ModuleManager mgr, Events ev, Object history, boolean reloadable, boolean autoload, boolean eager) throws IOException {
            super(mgr, ev, history, reloadable, autoload, eager);
            String ms = "OpenIDE-Module: boom\n\n";
            StringBufferInputStream sbis = new StringBufferInputStream(ms);
            manifest = new Manifest(sbis);
            parseManifest();
        }
        public List getAllJars() {
            return new ArrayList();
        }
        public void setReloadable(boolean r) {
        }
        public void reload() throws IOException {
        }
        protected void classLoaderUp(Set parents) throws IOException {
            classloader = new JarClassLoader(getAllJars(), new ClassLoader[] { new NoOpClassLoader() } ); // new NoOpClassLoader()
        }
        protected void classLoaderDown() {
        }
        protected void cleanup() {
        }
        protected void destroy() {
        }
        public boolean isFixed() {
            return true;
        }
        public Object getLocalizedAttribute(String attr) {
            return null;
        }
    }
    
    private static final class NoOpClassLoader extends ClassLoader {
        protected Class loadClass(String name, boolean resolve) 
	throws ClassNotFoundException {
            if ("java.lang.String".equals(name)) {
                throw new ClassNotFoundException("NoOpClassLoader cannot load " + name);
            }
            return super.loadClass(name, resolve);
        }
    }
}
