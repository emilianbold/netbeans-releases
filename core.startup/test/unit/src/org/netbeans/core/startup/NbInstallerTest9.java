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
import java.io.IOException;
import java.util.Collections;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import junit.textui.TestRunner;
import org.netbeans.ModuleInstaller;
import org.netbeans.core.startup.SetupHid.FakeEvents;
import org.netbeans.junit.NbTestSuite;

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
        ModuleInstaller inst = new org.netbeans.core.startup.NbInstaller(new FakeEvents());
        File littleJar = new File(workdir, "little-manifest.jar");
        //inst.loadManifest(littleJar).write(System.out);
        assertEquals(getManifest(littleJar), inst.loadManifest(littleJar));
        File mediumJar = new File(workdir, "medium-manifest.jar");
        assertEquals(getManifest(mediumJar), inst.loadManifest(mediumJar));
        File bigJar = new File(workdir, "big-manifest.jar");
        assertEquals(getManifest(bigJar), inst.loadManifest(bigJar));
        // trigger cache saving - this is sort of a hack, there is no API to do it
        inst.load(Collections.EMPTY_LIST);
        File allManifestsDat = new File(new File(new File(workdir, "var"), "cache"), "all-manifests.dat");
        assertTrue("File " + allManifestsDat + " exists", allManifestsDat.isFile());
        // Create a new NbInstaller, since otherwise it turns off caching...
        inst = new org.netbeans.core.startup.NbInstaller(new FakeEvents());
        assertEquals(getManifest(littleJar), inst.loadManifest(littleJar));
        assertEquals(getManifest(mediumJar), inst.loadManifest(mediumJar));
        assertEquals(getManifest(bigJar), inst.loadManifest(bigJar));
    }
    
    private static Manifest getManifest(File jar) throws IOException {
        JarFile jf = new JarFile(jar);
        try {
            return jf.getManifest();
        } finally {
            jf.close();
        }
    }
    
}
