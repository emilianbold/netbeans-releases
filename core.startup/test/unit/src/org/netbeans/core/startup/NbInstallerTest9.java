/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup;

import java.io.*;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.netbeans.Module;
import org.netbeans.ModuleInstaller;
import org.netbeans.ModuleManager;
import org.netbeans.junit.*;
import junit.textui.TestRunner;

/** Test the NetBeans module installer implementation.
 * Broken into pieces to ensure each runs in its own VM.
 * @author Jesse Glick
 */
public class NbInstallerTest9 extends SetupHid {
    
    public NbInstallerTest9(String name) {
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
        System.setProperty("netbeans.suppress.sysprop.warning", "true");
        TestRunner.run(new NbTestSuite(NbInstallerTest9.class));
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        File workdir = getWorkDir();
        String[] jarnames = new String[] {
            "little-manifest.jar",
            "medium-manifest.jar",
            "big-manifest.jar",
        };
        for (int i = 0; i < jarnames.length; i++) {
            copy(new File(jars, jarnames[i]), new File(workdir, jarnames[i]));
        }
    }
    
    /** Test #26786/#28755: manifest caching can be buggy.
     */
    public void testManifestCaching() throws Exception {
        File workdir = getWorkDir();
        System.setProperty("netbeans.user", workdir.getAbsolutePath());
        FakeEvents ev = new FakeEvents();
        NbInstaller inst = new org.netbeans.core.startup.NbInstaller(ev);
        ModuleManager mgr = new ModuleManager(inst, ev);
        inst.registerManager(mgr);
        
        File littleJar = new File(workdir, "little-manifest.jar");
        Module m1 = mgr.create(littleJar, null, false, false, false);
        
        //inst.loadManifest(littleJar).write(System.out);
        assertEquals(getManifest(littleJar), inst.loadManifest(littleJar));
        File mediumJar = new File(workdir, "medium-manifest.jar");
        Module m2 = mgr.create(mediumJar, null, false, false, false);

        assertEquals(getManifest(mediumJar), inst.loadManifest(mediumJar));
        File bigJar = new File(workdir, "big-manifest.jar");
        Module m3 = mgr.create(bigJar, null, false, false, false);
        assertEquals(getManifest(bigJar), inst.loadManifest(bigJar));
        
        // enable them
        try {
            mgr.mutexPrivileged().enterWriteAccess();
            
            mgr.enable(new HashSet (Arrays.asList (new Object[] { m1, m2, m3})));
            
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        
        // trigger cache saving - this is sort of a hack, there is no API to do it
        inst.load(Collections.EMPTY_LIST);
        File allManifestsDat = new File(new File(new File(workdir, "var"), "cache"), "all-manifests.dat");
        assertTrue("File " + allManifestsDat + " exists", allManifestsDat.isFile());
        // Create a new NbInstaller, since otherwise it turns off caching...
        inst = new org.netbeans.core.startup.NbInstaller(new FakeEvents());
        
        assertEquals(getManifest(littleJar), inst.loadManifest(littleJar));
        assertEquals(getManifest(mediumJar), inst.loadManifest(mediumJar));
        assertEquals(getManifest(bigJar), inst.loadManifest(bigJar));
        
        int i1 = inst.getIndex(littleJar);
        int i2 = inst.getIndex(mediumJar);
        int i3 = inst.getIndex(bigJar);

        int[] data = { -1, -1, -1 };
        
        data[i1] = 1;
        data[i2] = 2;
        data[i3] = 3;
        
        int all = data[0] + data[1] + data[2];
        assertEquals ("Each of the jar files has a unique id", 6, all);
    }
    
    static Manifest getManifest(File jar) throws IOException {
        JarFile jf = new JarFile(jar);
        try {
            return jf.getManifest();
        } finally {
            jf.close();
        }
    }
    
}
