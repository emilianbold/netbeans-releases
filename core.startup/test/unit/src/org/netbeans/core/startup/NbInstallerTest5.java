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

package org.netbeans.core.startup;

import java.io.File;
import java.util.Collections;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.junit.*;
import junit.textui.TestRunner;
import org.openide.filesystems.Repository;

/** Test the NetBeans module installer implementation.
 * Broken into pieces to ensure each runs in its own VM.
 * @author Jesse Glick
 */
public class NbInstallerTest5 extends SetupHid {

    public NbInstallerTest5(String name) {
        super(name);
    }

    public static void main(String[] args) {
        //System.setProperty("mlfs.DEBUG", "true");
        TestRunner.run(new NbTestSuite(NbInstallerTest5.class));
    }
    
    /** Test #21173/#23609: overriding layers by module dependencies.
     * Version 2: modules loaded piece by piece.
     * Exercises different logic in XMLFileSystem as well as ModuleLayeredFileSystem.
     */
    public void testDependencyLayerOverrides2() throws Exception {
        Main.getModuleSystem (); // init module system
        System.err.println("Module Info->"+org.openide.util.Lookup.getDefault()
                .lookup(org.openide.modules.ModuleInfo.class)); // TEMP
        final FakeEvents ev = new FakeEvents();
        org.netbeans.core.startup.NbInstaller installer = new org.netbeans.core.startup.NbInstaller(ev);
        ModuleManager mgr = new ModuleManager(installer, ev);
        installer.registerManager(mgr);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "base-layer-mod.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "override-layer-mod.jar"), null, false, false, false);
            
            assertEquals(Collections.EMPTY_SET, m2.getProblems());
            assertEquals(null, slurp("foo/file1.txt"));
            assertEquals(null, slurp("foo/file3.txt"));
            assertEquals(null, slurp("foo/file4.txt"));
            mgr.enable(m1);
            assertEquals("base contents", slurp("foo/file1.txt"));
            assertEquals("base contents", slurp("foo/file3.txt"));
            assertEquals("base contents", slurp("foo/file4.txt"));
            assertEquals("someval", Repository.getDefault().getDefaultFileSystem().findResource("foo/file5.txt").getAttribute("myattr"));
            mgr.enable(m2);
            assertEquals("base contents", slurp("foo/file1.txt"));
            assertEquals(null, slurp("foo/file4.txt"));
            assertEquals("customized contents", slurp("foo/file3.txt"));
            assertEquals("someotherval", Repository.getDefault().getDefaultFileSystem().findResource("foo/file5.txt").getAttribute("myattr"));
            mgr.disable(m2);
            assertEquals("base contents", slurp("foo/file3.txt"));
            mgr.disable(m1);
            assertEquals(null, slurp("foo/file3.txt"));
            mgr.delete(m2);
            mgr.delete(m1);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

}
