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
