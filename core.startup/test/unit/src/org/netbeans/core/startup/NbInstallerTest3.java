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
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.junit.*;
import junit.textui.TestRunner;
import org.openide.util.NbBundle;

/** Test the NetBeans module installer implementation.
 * Broken into pieces to ensure each runs in its own VM.
 * @author Jesse Glick
 */
public class NbInstallerTest3 extends SetupHid {
    
    public NbInstallerTest3(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(NbInstallerTest3.class));
    }
    
    /** Test #21173/#23595: overriding layers by branding. */
    public void testBrandingLayerOverrides() throws Exception {
        Main.getModuleSystem ();
        final FakeEvents ev = new FakeEvents();
        org.netbeans.core.startup.NbInstaller installer = new org.netbeans.core.startup.NbInstaller(ev);
        ModuleManager mgr = new ModuleManager(installer, ev);
        installer.registerManager(mgr);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            String orig = NbBundle.getBranding();
            NbBundle.setBranding("foo");
            try {
                Module m1 = mgr.create(new File(jars, "base-layer-mod.jar"), null, false, false, false);
                assertEquals(Collections.EMPTY_SET, m1.getProblems());
                mgr.enable(m1);
                assertEquals("special contents", slurp("foo/file1.txt"));
                assertEquals(null, slurp("foo/file2.txt"));
                mgr.disable(m1);
                mgr.delete(m1);
            } finally {
                NbBundle.setBranding(orig);
            }
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
}
