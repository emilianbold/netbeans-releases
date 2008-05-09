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

package org.netbeans.core.startup;

//import junit.framework.*;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.junit.*;
import java.util.*;
import org.openide.util.*;
import org.openide.modules.*;
import java.io.*;
import java.net.URI;
import java.util.logging.Level;
import org.netbeans.Stamps;
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
    private File ud;
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
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        
        ud = new File(getWorkDir(), "ud");
        ud.mkdirs();
        System.setProperty("netbeans.user", ud.getPath());
        
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
            assertEquals(Collections.emptySet(), list.readInitial());
            Set<Module> modules = new HashSet<Module>();
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
            mgr.disable(new HashSet<Module>(Arrays.asList(m1, m2)));
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
        
        assertCache();
    }
    
    private void assertCache() throws Exception {
        Stamps.getModulesJARs().flush(0);
        Stamps.getModulesJARs().shutdown();
        
        File f = new File(new File(new File(System.getProperty("netbeans.user"), "var"), "cache"), "all-modules.dat");
        assertTrue("Cache exists", f.exists());

        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        FileObject mf = lfs.findResource(modulesfolder.getPath());
        assertNotNull("config folder exits", mf);
        
        CountingSecurityManager.initialize(new File(lfs.getRootDirectory(), "Modules").getPath());
        
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr2 = new ModuleManager(installer, ev);
        assertNotNull(mf);
        ModuleList list2 = new ModuleList(mgr2, mf, ev);
        mgr2.mutexPrivileged().enterWriteAccess();
        CharSequence log = Log.enable("org.netbeans.core.startup.ModuleList", Level.FINEST);
        try {
            list2.readInitial();
        } finally {
            mgr2.mutexPrivileged().exitWriteAccess();
        }
        if (log.toString().indexOf("no cache") >= 0) {
            fail("Everything shall be read from cache:\n" + log);
        }
        if (log.toString().indexOf("Reading cache") < 0) {
            fail("Cache shall be read:\n" + log);
        }

        Set<String> moduleNew = cnbs(mgr2.getModules());
        Set<String> moduleOld = cnbs(mgr.getModules());
        
        assertEquals("Same set of modules:", moduleOld, moduleNew);
        
        CountingSecurityManager.assertCounts("Do not access the module config files", 0);
    }
    private static Set<String> cnbs(Set<Module> modules) {
        TreeSet<String> set = new TreeSet<String>();
        for (Module m : modules) {
            set.add(m.getCodeNameBase());
        }
        return set;
    }
    
    /** Check that adding a new module via XML, as Auto Update does, works.
     * Written to help test #27106.
     */
    public void testAddNewModuleViaXML() throws Exception {
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            assertEquals(Collections.emptySet(), list.readInitial());
            assertEquals(Collections.emptySet(), mgr.getModules());
            list.trigger(Collections.<Module>emptySet());
            assertEquals(Collections.emptySet(), mgr.getModules());
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
