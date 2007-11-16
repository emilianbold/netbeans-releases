/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.api.ruby.platform;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * @author Tor Norbye
 */
public class RubyInstallationTest extends RubyTestBase {

    public RubyInstallationTest(String testName) {
        super(testName);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
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
        assertTrue(RubyInstallation.compareGemVersions("4.4.4-platform", "4.4.4") != 0);
        assertTrue(RubyInstallation.compareGemVersions("0.10.0-ruby", "0.9.0") > 0);
        assertTrue(RubyInstallation.compareGemVersions("0.9.0-ruby", "0.10.0") < 0);
        assertTrue(RubyInstallation.compareGemVersions("0.10.0", "0.9.0-ruby") > 0);
        assertTrue(RubyInstallation.compareGemVersions("0.9.0", "0.10.0-ruby") < 0);
    }

    public void testChooseGems() throws Exception {
        RubyInstallation ri = setUpRubyInstallationWithGems();
        
        String gemLibs = ri.getRubyLibGemDir();
        File specs = new File(new File(ri.getRubyLibGemDir()), "specifications");

        // Put gems into the gemLibs dir
        String[] gemDirs = new String[]{"foo-1.0.0",
                "notagem",
                "pdf-writer-0.1.1",
                "mongrel-1.0.0-mswin",
                "bar-baz-0.3.3-ruby",
                "activerecord-1.15.1.6752",
                "activerecord-1.15.3.6752"};
        for (String gemDir : gemDirs) {
            new File(gemLibs, gemDir).mkdir();
            new File(specs, gemDir + ".gemspec").createNewFile();
        }

        // Test for 106862
        new File(gemLibs, "sqlite-2.0.1").mkdirs();
        new File(gemLibs, "sqlite3-ruby-1.2.0").mkdirs();

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

    public void testFindGemExecutableInRubyBin() throws Exception {
        RubyInstallation ri = setUpRubyInstallationWithGems();
        touch("rdebug-ide", ri.getRubyBin());
        assertNotNull(ri.findGemExecutable("rdebug-ide"));
    }

    public void testFindGemExecutableInGemRepo() throws Exception {
        RubyInstallation ri = setUpRubyInstallationWithGems();
        touch("rdebug-ide", new File(ri.getRubyLibGemDir(), "bin").getPath());
        assertNotNull(ri.findGemExecutable("rdebug-ide"));
    }

    public void testFindRDoc() throws Exception {
        RubyInstallation ri = setUpRubyInstallationWithGems();
        assertNotNull("rdoc found", ri.getRDoc());
    }

    public void testFindRDocWithSuffix() throws Exception {
        RubyInstallation ri = setUpRubyInstallation(false, "1.8.6-p110");
        assertNotNull("rdoc found", ri.getRDoc());
    }

    public void testFindGemExecutableWith_GEM_HOME() throws Exception {
        File gemRepo = new File(getWorkDir(), "gemrepo");
        File gemRepoBinF = new File(gemRepo, "bin");
        gemRepoBinF.mkdirs();
        RubyInstallation ri = setUpRubyInstallation();
        RubyInstallation.TEST_GEM_HOME = gemRepo.getAbsolutePath();
        touch("rdebug-ide", gemRepoBinF.getAbsolutePath());
        assertNotNull(ri.findGemExecutable("rdebug-ide"));
    }

    private String touch(String path, String dir) throws IOException {
        File f = new File(dir, path);
        f.createNewFile();
        return f.getAbsolutePath();
    }

    private RubyInstallation setUpRubyInstallationWithGems() throws Exception {
        return new RubyInstallation(setUpRubyWithGems().getAbsolutePath());
    }

    private RubyInstallation setUpRubyInstallation() throws Exception {
        return new RubyInstallation(setUpRuby().getAbsolutePath());
    }

    private RubyInstallation setUpRubyInstallation(final boolean withGems, final String suffix) throws Exception {
        return new RubyInstallation(setUpRuby(withGems, suffix).getAbsolutePath());
    }

}
