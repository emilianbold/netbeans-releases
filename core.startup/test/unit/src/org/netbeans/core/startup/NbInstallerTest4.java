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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.openide.filesystems.Repository;

/** Test the NetBeans module installer implementation.
 * Broken into pieces to ensure each runs in its own VM.
 * @author Jesse Glick
 */
public class NbInstallerTest4 extends SetupHid {

    /*
    static {
        // Turn on verbose logging while developing tests:
        System.setProperty("org.netbeans.core.modules", "0");
        System.setProperty("org.netbeans.core.projects", "0");
    }
     */
    
    public NbInstallerTest4(String name) {
        super(name);
    }
    
    /** Test #21173/#23609: overriding layers by module dependencies.
     * Version 1: all modules loaded together.
     */
    public void testDependencyLayerOverrides1() throws Exception {
        Main.getModuleSystem (); // init module system
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
            Set m1m2 = new HashSet(Arrays.asList(new Module[] {m1, m2}));
            mgr.enable(m1m2);
            assertEquals("base contents", slurp("foo/file1.txt"));
            assertEquals("customized contents", slurp("foo/file3.txt"));
            assertEquals(null, slurp("foo/file4.txt"));
            assertEquals("someotherval", Repository.getDefault().getDefaultFileSystem().findResource("foo/file5.txt").getAttribute("myattr"));
            mgr.disable(m1m2);
            assertEquals(null, slurp("foo/file1.txt"));
            mgr.delete(m2);
            mgr.delete(m1);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

}
