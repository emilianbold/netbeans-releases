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

//import junit.framework.*;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.core.startup.ModuleHistory;
import org.netbeans.core.startup.ModuleList;
import org.netbeans.junit.*;
import junit.textui.TestRunner;
import java.util.*;
import java.lang.reflect.Method;
import org.openide.util.*;
import org.openide.modules.*;
import java.io.*;
import java.net.URI;
import org.openide.filesystems.*;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/** Test the functions of the module list, i.e. finding modules on
 * disk and installing them, and writing out state as needed.
 * @author Jesse Glick
 */
public class ModuleListTest extends SetupHid {
    
    static {
        System.setProperty("org.openide.util.Lookup", L.class.getName());
    }
    public static final class L extends ProxyLookup {
        public L() {
            super(new Lookup[] {
                Lookups.singleton(new IFL()),
            });
        }
    }
    
    private static final File JARS = new File(URI.create(ModuleListTest.class.getResource("jars").toExternalForm()));
    private static final String PREFIX = "wherever/";
    
    private static final class IFL extends InstalledFileLocator {
        public IFL() {}
        public File locate(String relativePath, String codeNameBase, boolean localized) {
            if (relativePath.startsWith(PREFIX)) {
                File f = new File(JARS, relativePath.substring(PREFIX.length()).replace('/', File.separatorChar));
                if (f.exists()) {
                    return f;
                }
            }
            return null;
        }
    }
    
    public ModuleListTest(String name) {
        super(name);
    }
    
    private ModuleManager mgr;
    private org.netbeans.core.startup.ModuleList list;
    private FileObject modulesfolder;
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        mgr = new ModuleManager(installer, ev);
        File dir = getWorkDir();
        File modulesdir = new File(dir, "Modules");
        if (! modulesdir.mkdir()) throw new IOException("Making " + modulesdir);
        LocalFileSystem fs = new LocalFileSystem();
        fs.setRootDirectory(dir);
        modulesfolder = fs.findResource("Modules");
        assertNotNull(modulesfolder);
        list = new ModuleList(mgr, modulesfolder, ev);
    }
    
    private Module makeModule(String jarName) throws Exception {
        File f = new File(JARS, jarName);
        Module m = mgr.create(f, new ModuleHistory(PREFIX + jarName), false, false, false);
        return m;
    }
    
    /** Load simple-module and depends-on-simple-module.
     * Make sure they can be installed and in a sane order.
     * Make sure a class from one can depend on a class from another.
     */
    public void testScanAndTwiddle() throws Exception {
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            // XXX try to read an actual initial list too...
            assertEquals(Collections.EMPTY_SET, list.readInitial());
            Set modules = new HashSet();
            modules.add(makeModule("simple-module.jar"));
            modules.add(makeModule("depends-on-simple-module.jar"));
            list.trigger(modules);
            assertEquals(modules, mgr.getEnabledModules());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        FileObject[] xml = modulesfolder.getChildren();
        assertEquals(2, xml.length);
        FileObject foo, bar;
        if (xml[0].getPath().equals("Modules/org-foo.xml")) {
            assertEquals("Modules/org-bar.xml", xml[1].getPath());
            foo = xml[0];
            bar = xml[1];
        } else {
            assertEquals("Modules/org-bar.xml", xml[0].getPath());
            assertEquals("Modules/org-foo.xml", xml[1].getPath());
            foo = xml[1];
            bar = xml[0];
        }
        assertFile(FileUtil.toFile(foo), new File(JARS, "org-foo.xml"));
        assertFile(FileUtil.toFile(bar), new File(JARS, "org-bar.xml"));
        // Checking that changes in memory will rewrite XML:
        LoggedFileListener listener = new LoggedFileListener();
        modulesfolder.addFileChangeListener(listener);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.get("org.foo");
            assertNotNull(m1);
            Module m2 = mgr.get("org.bar");
            assertNotNull(m2);
            mgr.disable(new HashSet(Arrays.asList(new Module[] {m1, m2})));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        // We expect it to have marked both as disabled now:
        listener.waitForChange("Modules/org-foo.xml");
        listener.waitForChange("Modules/org-bar.xml");
        assertFile(new File(JARS, "org-foo_disabled.xml"), FileUtil.toFile(foo));
        assertFile(new File(JARS, "org-bar_disabled.xml"), FileUtil.toFile(bar));
        // Check that changes in disk are parsed and applied (#13921)
        LoggedPCListener listener2 = new LoggedPCListener();
        Module m1 = mgr.get("org.foo");
        m1.addPropertyChangeListener(listener2);
        copy(new File(JARS, "org-foo.xml"), foo);
        /* Does not seem to refresh reliably enough:
        copy(new File(JARS, "org-foo.xml"), FileUtil.toFile(foo));
        foo.refresh();
         */
        // The change ought to be noticed by filesystems, picked up by
        // ModuleList, parsed, and result in org.foo being turned back on.
        listener2.waitForChange(m1, Module.PROP_ENABLED);
        assertTrue("m1 is enabled now", m1.isEnabled());
    }
    
    /** Check that adding a new module via XML, as Auto Update does, works.
     * Written to help test #27106.
     */
    public void testAddNewModuleViaXML() throws Exception {
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            assertEquals(Collections.EMPTY_SET, list.readInitial());
            assertEquals(Collections.EMPTY_SET, mgr.getModules());
            list.trigger(Collections.EMPTY_SET);
            assertEquals(Collections.EMPTY_SET, mgr.getModules());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        LoggedPCListener listener = new LoggedPCListener();
        mgr.addPropertyChangeListener(listener);
        modulesfolder.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                // XXX this will require that there be an appropriate InstalledFileLocator in Lookup
                FileObject fooxml = modulesfolder.createData("org-foo", "xml");
                copy(new File(JARS, "org-foo.xml"), fooxml);
            }
        });
        assertTrue("PROP_MODULES fired", listener.waitForChange(mgr, ModuleManager.PROP_MODULES));
        mgr.mutexPrivileged().enterReadAccess();
        try {
            Set modules = mgr.getEnabledModules();
            assertEquals(1, modules.size());
            Module m = (Module)modules.iterator().next();
            assertEquals("org.foo", m.getCodeNameBase());
            assertTrue(m.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitReadAccess();
        }
    }
    
    // XXX try to read a nonempty initial list
    
    // XXX would be nice to also have test which uses a layer
    // to install and remove the Modules/ entries in a MFS
    // and check that layer-driven events are enough to cause
    // complex installations & uninstallations
    
}
