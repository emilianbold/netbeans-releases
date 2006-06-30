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

package org.netbeans.modules.apisupport.project;

import java.io.File;

/**
 * Test functionality of ManifestManager.
 *
 * @author Martin Krauskopf
 */
public class ManifestManagerTest extends TestBase {

    // XXX test also implementation version

    public ManifestManagerTest(String name) {
        super(name);
    }

    private File suite1, suite2;

    protected void setUp() throws Exception {
        super.setUp();
        suite1 = file(extexamplesF, "suite1");
        suite2 = file(extexamplesF, "suite2");
    }

    public void testDirectManifestFile() throws Exception {
        File basedir = new File(suite2, "misc-project");
        ManifestManager mm = ManifestManager.getInstance(new File(basedir, "manifest.mf"), false);
        assertEquals("right codeNameBase", "org.netbeans.examples.modules.misc", mm.getCodeNameBase());
        assertEquals("right release version", "1", mm.getReleaseVersion());
        assertEquals("right specification version", "1.0", mm.getSpecificationVersion());
        assertEquals("right localizing bundle", "org/netbeans/examples/modules/misc/Bundle.properties",
                mm.getLocalizingBundle());
        
        basedir = new File(suite1, "action-project");
        mm = ManifestManager.getInstance(new File(basedir, "manifest.mf"), false);
        assertEquals("right codeNameBase", "org.netbeans.examples.modules.action", mm.getCodeNameBase());
        assertNull("no release version", mm.getReleaseVersion());
    }
    
    public void testFriends() throws Exception {
        File manifest = new File(getWorkDir(), "testManifest.mf");
        String mfContent = "Manifest-Version: 1.0\n" +
                "Ant-Version: Apache Ant 1.6.5\n" +
                "Created-By: 1.4.2_10-b03 (Sun Microsystems Inc.)\n" +
                "OpenIDE-Module-Public-Packages: org.netbeans.modules.editor.hints.spi.*\n" +
                "OpenIDE-Module-Friends: org.netbeans.modules.java.hints, org.netbeans.\n" +
                " modules.j2ee.ejbcore, org.netbeans.modules.kjava.editor\n" +
                "OpenIDE-Module-Module-Dependencies: org.openide.filesystems > 6.2, org\n" +
                " .openide.util > 6.2, org.openide.modules > 6.2, org.openide.nodes > 6\n" +
                " .2, org.openide.awt > 6.2, org.openide.text > 6.2, org.openide.loader\n" +
                " s, org.netbeans.modules.editor.lib/1, org.netbeans.modules.editor.mim\n" +
                " elookup/1\n" +
                "OpenIDE-Module-Build-Version: 060123\n" +
                "OpenIDE-Module-Specification-Version: 1.10.0.1\n" +
                "OpenIDE-Module: org.netbeans.modules.editor.hints/1\n" +
                "OpenIDE-Module-Implementation-Version: 1\n" +
                "OpenIDE-Module-Localizing-Bundle: org/netbeans/modules/editor/hints/re\n" +
                " sources/Bundle.properties\n" +
                "OpenIDE-Module-Install: org/netbeans/modules/editor/hints/HintsModule.\n" +
                " class\n" +
                "OpenIDE-Module-Requires: org.openide.modules.ModuleFormat1\n";
        dump(manifest, mfContent);
        ManifestManager mm = ManifestManager.getInstance(manifest, true);
        assertEquals("one public package", 1, mm.getPublicPackages().length);
    }
}
