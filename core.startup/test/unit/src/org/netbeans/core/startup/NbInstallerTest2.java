/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup;

import java.io.File;
import java.util.Collections;
import java.util.Locale;
import org.netbeans.Module;
import org.netbeans.junit.*;
import junit.textui.TestRunner;
import org.openide.filesystems.Repository;

/** Test the NetBeans module installer implementation.
 * Broken into pieces to ensure each runs in its own VM.
 * @author Jesse Glick
 */
public class NbInstallerTest2 extends SetupHid {
    
    public NbInstallerTest2(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(NbInstallerTest2.class));
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("org.netbeans.core.modules.NbInstaller.noAutoDeps", "true");
    }
    
    /** Test #21173/#23595: overriding layers by localization. */
    public void testLocLayerOverrides() throws Exception {
        Main.getModuleSystem (); // init module system
        final FakeEvents ev = new FakeEvents();
        org.netbeans.core.startup.NbInstaller installer = new org.netbeans.core.startup.NbInstaller(ev);
        org.netbeans.ModuleManager mgr = new org.netbeans.ModuleManager(installer, ev);
        installer.registerManager(mgr);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Locale orig = Locale.getDefault();
            Locale.setDefault(new Locale("cs", "CZ"));
            try {
                Module m1 = mgr.create(new File(jars, "base-layer-mod.jar"), null, false, false, false);
                assertEquals(Collections.EMPTY_SET, m1.getProblems());
                assertEquals(null, slurp("foo/file1.txt"));
                mgr.enable(m1);
                assertEquals("prekladany obsah", slurp("foo/file1.txt"));
                assertEquals("base contents", slurp("foo/file2.txt"));
                assertEquals("someval", Repository.getDefault().getDefaultFileSystem().findResource("foo/file5.txt").getAttribute("myattr"));
                mgr.disable(m1);
                mgr.delete(m1);
            } finally {
                Locale.setDefault(orig);
            }
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

}
