/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.versioning.spi;

import java.io.File;
import java.util.prefs.Preferences;
import junit.framework.TestCase;
import org.netbeans.modules.versioning.spi.testvcs.TestVCS;

/**
 * Versioning SPI unit tests.
 *
 * @author Maros Sandor
 */
public class VersioningSupportTest extends TestCase {
    
    private File dataRootDir;

    public VersioningSupportTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        dataRootDir = new File(System.getProperty("data.root.dir"));
    }
    
    public void testGetPreferences() {
        Preferences prefs = VersioningSupport.getPreferences();
        assertNotNull(prefs);
        prefs.putBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, true);
        assertTrue(prefs.getBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, false));
        prefs.putBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, false);
        assertFalse(prefs.getBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, true));
    }

    public void testGetOwner() {
        File aRoot = File.listRoots()[0];
        assertNull(VersioningSupport.getOwner(aRoot));
        aRoot = dataRootDir;
        assertNull(VersioningSupport.getOwner(aRoot));
        aRoot = new File(dataRootDir, "workdir");
        assertNull(VersioningSupport.getOwner(aRoot));
        aRoot = new File(dataRootDir, "workdir/root-test-versioned/a.txt");
        assertTrue(VersioningSupport.getOwner(aRoot) instanceof TestVCS);
        aRoot = new File(dataRootDir, "workdir/root-test-versioned");
        assertTrue(VersioningSupport.getOwner(aRoot) instanceof TestVCS);
        aRoot = new File(dataRootDir, "workdir/root-test-versioned/b-test-versioned");
        assertTrue(VersioningSupport.getOwner(aRoot) instanceof TestVCS);
        aRoot = new File(dataRootDir, "workdir/root-test-versioned/nonexistent-file");
        assertTrue(VersioningSupport.getOwner(aRoot) instanceof TestVCS);
    }

    public void testFlat() {
        File aRoot = File.listRoots()[0];
        assertFalse(VersioningSupport.isFlat(aRoot));
        File file = VersioningSupport.getFlat(aRoot.getAbsolutePath());
        assertTrue(VersioningSupport.isFlat(file));
    }
}
