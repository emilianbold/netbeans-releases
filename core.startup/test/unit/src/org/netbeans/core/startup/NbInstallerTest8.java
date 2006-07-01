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

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.core.startup.ModuleHistory;
import org.netbeans.junit.*;
import junit.textui.TestRunner;
import org.netbeans.core.startup.layers.SessionManager;
import org.openide.filesystems.FileSystem;

/** Test the NetBeans module installer implementation.
 * Broken into pieces to ensure each runs in its own VM.
 * @author Jesse Glick
 */
public class NbInstallerTest8 extends SetupHid {
    
    public NbInstallerTest8(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        // Turn on verbose logging while developing tests:
        System.setProperty("org.netbeans.core.modules", "0");
        // In case run standalone, need a work dir.
        if (System.getProperty("nbjunit.workdir") == null) {
            // Hope java.io.tmpdir is set...
            System.setProperty("nbjunit.workdir", System.getProperty("java.io.tmpdir"));
        }
        TestRunner.run(new NbTestSuite(NbInstallerTest8.class));
    }
    
    private static File home, user, homeMod, userMod;
    private File moduleJar;
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("org.netbeans.core.modules.NbInstaller.noAutoDeps", "true");
        // leave NO_COMPAT_AUTO_TRANSITIVE_DEPS=false
        moduleJar = new File(jars, "look-for-myself.jar");
    }
    
    /** Test #28465: Lookup<ModuleInfo> should be ready soon, even while
     * modules are still loading. The ModuleInfo need not claim to be enabled
     * during this time, but it must exist.
     */
    public void testEarlyModuleInfoLookup() throws Exception {
        // Ought to load these modules:
        ModuleManager mgr = Main.getModuleSystem().getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m = mgr.get("lookformyself");
            assertNull(m);
            m = mgr.create(moduleJar, new ModuleHistory(moduleJar.getAbsolutePath()), false, false, false);
            assertEquals("look-for-myself.jar can be enabled", Collections.EMPTY_SET, m.getProblems());
            mgr.enable(m);
            Class c = m.getClassLoader().loadClass("lookformyself.Loder");
            Method meth = c.getMethod("foundNow", null);
            assertTrue("ModuleInfo is found after startup", ((Boolean)meth.invoke(null, null)).booleanValue());
            Field f = c.getField("foundEarly");
            assertTrue("ModuleInfo is found during dataloader section initialization", ((Boolean)f.get(null)).booleanValue());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
}
