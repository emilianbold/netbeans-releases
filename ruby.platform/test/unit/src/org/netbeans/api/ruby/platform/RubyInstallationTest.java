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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.ruby.platform;

import java.io.File;
import java.util.Set;
import org.netbeans.junit.NbTestCase;

/**
 * @author Tor Norbye
 */
public class RubyInstallationTest extends NbTestCase {

    public RubyInstallationTest(String testName) {
        super(testName);
    }

    public void testCompareGemVersions() {
        assertTrue(RubyInstallation.compareGemVersions("1.0.0", "0.9.9") > 0);
        assertTrue(RubyInstallation.compareGemVersions("0.4.0", "0.3.0") > 0);
        assertTrue(RubyInstallation.compareGemVersions("0.4.0", "0.3.9") > 0);
        assertTrue(RubyInstallation.compareGemVersions("0.0.2", "0.0.1") > 0);
        assertTrue(RubyInstallation.compareGemVersions("0.10.0", "0.9.0") > 0);
        assertTrue(RubyInstallation.compareGemVersions("0.9.0", "0.10.0") < 0);
        assertTrue(RubyInstallation.compareGemVersions("1.0.0", "4.9.9") < 0);
        assertTrue(RubyInstallation.compareGemVersions("0.3.0", "0.4.0") < 0);
        assertTrue(RubyInstallation.compareGemVersions("0.3.9", "0.4.0") < 0);
        assertTrue(RubyInstallation.compareGemVersions("0.0.1", "0.0.2") < 0);
        assertTrue(RubyInstallation.compareGemVersions("4.4.4", "4.4.4") == 0);
    }

    public void testChooseGems() throws Exception {
        File home = getWorkDir();

        // Build a fake ruby structure
        File bin = new File(home, "bin");
        bin.mkdirs();
        File ruby = new File(bin, "ruby");
        ruby.createNewFile();

        File lib = new File(home, "lib");
        File rubyLib = new File(lib, "ruby");
        File gems = new File(rubyLib, "gems");
        String version = "1.8";
        File ruby18Libs = new File(rubyLib, version);
        ruby18Libs.mkdirs();
        File gemLibs = new File(gems, version + File.separator + "gems");
        gemLibs.mkdirs();
        File specs = new File(gems, version + File.separator + "specifications");
        specs.mkdirs();

        // Put gems into the gemLibs dir
        String[] gemDirs = new String[]{"foo-1.0.0", "notagem", "pdf-writer-0.1.1", "mongrel-1.0.0-mswin", "bar-baz-0.3.3-ruby",
           "activerecord-1.15.1.6752", "activerecord-1.15.3.6752",};
        for (String gemDir : gemDirs) {
            new File(gemLibs, gemDir).mkdir();
            new File(specs, gemDir + ".gemspec").createNewFile();
        }
        
        // Test for 106862
        new File(gemLibs, "sqlite-2.0.1").mkdirs();
        new File(gemLibs, "sqlite3-ruby-1.2.0").mkdirs();

        RubyInstallation ri = new RubyInstallation(ruby.getAbsolutePath());

        // Now introspect on the structure
        Set<String> installedGems = ri.getInstalledGems();
        assertTrue(installedGems.contains("foo"));
        assertTrue(installedGems.contains("pdf-writer"));
        assertTrue(installedGems.contains("mongrel"));
        assertTrue(installedGems.contains("bar-baz"));
        assertTrue(installedGems.contains("activerecord"));
        assertFalse(installedGems.contains("notagem"));
        assertFalse(installedGems.contains("whatever"));
        assertFalse(installedGems.contains("sqlite"));
        assertFalse(installedGems.contains("sqlite3-ruby"));

        assertEquals("1.0.0", ri.getVersion("foo"));
        assertEquals(null, ri.getVersion("notagem"));
        assertEquals(null, ri.getVersion("nosuchgem"));
        assertEquals(null, ri.getVersion("sqlite"));
        assertEquals(null, ri.getVersion("sqlite3-ruby"));
        assertEquals("1.0.0", ri.getVersion("mongrel"));
        assertEquals("0.3.3", ri.getVersion("bar-baz"));
        assertEquals("0.1.1", ri.getVersion("pdf-writer"));
        assertEquals("1.15.3.6752", ri.getVersion("activerecord"));
    }
}
