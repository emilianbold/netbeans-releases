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

package org.netbeans.modules.apisupport.project;

import java.io.File;

/**
 * Test functionality of ManifestManager.
 *
 * @author Martin Krauskopf
 */
public class ManifestManagerTest extends TestBase {
    
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
}
