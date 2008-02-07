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
package org.netbeans.modules.ruby.platform.gems;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformManager;
import org.netbeans.api.ruby.platform.RubyTestBase;
import org.netbeans.api.ruby.platform.RubyTestBase.IFL;
import org.netbeans.api.ruby.platform.TestUtil;
import org.netbeans.junit.MockServices;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

public class GemManagerTest extends RubyTestBase {

    public GemManagerTest(final String testName) {
        super(testName);
        MockServices.setServices(IFL.class);
        TestUtil.getXTestJRubyHome();
    }

    public void testGetGemProblem() {
        RubyPlatform jruby = RubyPlatformManager.getDefaultPlatform();
        GemManager gm = jruby.getGemManager();
        assertNotNull(gm);
    }

    public void testGetRubyLibGemDir() throws Exception {
        RubyPlatform platform = RubyPlatformManager.addPlatform(setUpRubyWithGems());
        GemManager gemManager = platform.getGemManager();
        assertEquals("righ gem dir", new File(platform.getLib(), "ruby/gems/1.8"), new File(gemManager.getGemHome()));
    }
    
    public void testGetGem() throws Exception {
        RubyPlatform platform = RubyPlatformManager.addPlatform(setUpRubyWithGems());
        GemManager gemManager = platform.getGemManager();
        assertEquals("righ gem dir", new File(new File(getTestRubyHome(), "bin"), "gem").getAbsolutePath(), gemManager.getGemTool());
    }
    
    public void testGemFetching() {
        RubyPlatform jruby = RubyPlatformManager.getDefaultPlatform();
        GemManager gm = jruby.getGemManager();
        
        List<String> errors = new ArrayList<String>();
        List<Gem> available = gm.getRemoteGems(errors);
        assertNotNull("gem not null", available);
        System.out.println("available: " + available.size());
        assertTrue("no errros: " + errors, errors.isEmpty());
        
        List<Gem> installed = gm.getInstalledGems(errors);
        assertNotNull("gem not null", installed);
        System.out.println("installed: " + installed.size());
        assertTrue("no errros", errors.isEmpty());
        
        gm.reloadIfNeeded(errors);
        assertTrue("no errros", errors.isEmpty());
    }

    public void testIsValidGemHome() throws Exception {
        assertFalse("not valid", GemManager.isValidGemHome(getWorkDir()));
        assertTrue("valid", GemManager.isValidGemHome(
                new File(RubyPlatformManager.getDefaultPlatform().getInfo().getGemHome())));
        RubyPlatform platform = RubyPlatformManager.addPlatform(setUpRubyWithGems());
        assertTrue("valid", GemManager.isValidGemHome(
                new File(platform.getInfo().getGemHome())));
    }
    
    public void testGetRepositories() throws Exception {
        final RubyPlatform platform = RubyPlatformManager.getDefaultPlatform();
        GemManager gemManager = platform.getGemManager();
        List<String> paths = gemManager.getRepositories();
        assertEquals("one path element", 1, paths.size());
        assertEquals("same as Gem Home", gemManager.getGemHome(), paths.get(0));
        assertEquals("same as Gem Home", gemManager.getGemHome(), platform.getInfo().getGemPath());
    }
    
    public void testAddRemoveRepository() throws Exception {
        final RubyPlatform platform = RubyPlatformManager.getDefaultPlatform();
        GemManager gemManager = platform.getGemManager();
        String dummyRepo = getWorkDirPath() + "/a";
        gemManager.addRepository(dummyRepo);
        assertEquals("two repositories", 2, gemManager.getRepositories().size());
        assertTrue("two repositories in info's gempath", platform.getInfo().getGemPath().indexOf(File.pathSeparatorChar) != -1);
        gemManager.removeRepository(dummyRepo);
        assertEquals("one repositories", 1, gemManager.getRepositories().size());
        assertTrue("one repositories in info's gempath", platform.getInfo().getGemPath().indexOf(File.pathSeparatorChar) == -1);
    }
    
    public void testInitializeRepository() throws Exception {
        FileObject gemRepo = FileUtil.toFileObject(getWorkDir()).createFolder("gem-repo");
        GemManager.initializeRepository(gemRepo);
        GemManager.isValidGemHome(FileUtil.toFile(gemRepo));
    }
    
    public void testGetVersionForPlatform() throws IOException {
        final RubyPlatform platform = RubyPlatformManager.getDefaultPlatform();
        GemManager gemManager = platform.getGemManager();
        RubyPlatform jruby = RubyPlatformManager.getDefaultPlatform();
        FileObject gemRepo = FileUtil.toFileObject(getWorkDir()).createFolder("gem-repo");
        GemManager.initializeRepository(gemRepo);
        jruby.setGemHome(FileUtil.toFile(gemRepo));
        String version = Utilities.isWindows() ? "0.9.3" : "0.1.10";
        installFakeGem("ruby-debug-base", version, platform);
        assertEquals("native fast debugger available", version, gemManager.getVersion("ruby-debug-base"));
        assertNull("no jruby fast debugger available", gemManager.getVersionForPlatform("ruby-debug-base"));
        uninstallFakeGem("ruby-debug-base", version, platform);
        installFakeGem("ruby-debug-base", version, "java", platform);
        assertEquals("no jruby fast debugger available", version, gemManager.getVersionForPlatform("ruby-debug-base"));
    }
    
    public void testCompareGemVersions() {
        assertTrue(GemManager.compareGemVersions("1.0.0", "0.9.9") > 0);
        assertTrue(GemManager.compareGemVersions("0.4.0", "0.3.0") > 0);
        assertTrue(GemManager.compareGemVersions("0.4.0", "0.3.9") > 0);
        assertTrue(GemManager.compareGemVersions("0.0.2", "0.0.1") > 0);
        assertTrue(GemManager.compareGemVersions("0.10.0", "0.9.0") > 0);
        assertTrue(GemManager.compareGemVersions("0.9.0", "0.10.0") < 0);
        assertTrue(GemManager.compareGemVersions("1.0.0", "4.9.9") < 0);
        assertTrue(GemManager.compareGemVersions("0.3.0", "0.4.0") < 0);
        assertTrue(GemManager.compareGemVersions("0.3.9", "0.4.0") < 0);
        assertTrue(GemManager.compareGemVersions("0.0.1", "0.0.2") < 0);
        assertTrue(GemManager.compareGemVersions("4.4.4", "4.4.4") == 0);
        assertTrue(GemManager.compareGemVersions("4.4.4-platform", "4.4.4") != 0);
        assertTrue(GemManager.compareGemVersions("0.10.0-ruby", "0.9.0") > 0);
        assertTrue(GemManager.compareGemVersions("0.9.0-ruby", "0.10.0") < 0);
        assertTrue(GemManager.compareGemVersions("0.10.0", "0.9.0-ruby") > 0);
        assertTrue(GemManager.compareGemVersions("0.9.0", "0.10.0-ruby") < 0);
    }

    public void testChooseGems() throws Exception {
        RubyPlatform platform = RubyPlatformManager.addPlatform(setUpRubyWithGems());
        GemManager gemManager = platform.getGemManager();
        
        String gemLibs = gemManager.getGemHome();
        File specs = new File(new File(gemManager.getGemHome()), "specifications");

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
        Set<String> installedGems = gemManager.getInstalledGemsFiles();
        assertTrue(installedGems.contains("foo"));
        assertTrue(installedGems.contains("pdf-writer"));
        assertTrue(installedGems.contains("mongrel"));
        assertTrue(installedGems.contains("bar-baz"));
        assertTrue(installedGems.contains("activerecord"));
        assertFalse(installedGems.contains("notagem"));
        assertFalse(installedGems.contains("whatever"));
        assertFalse(installedGems.contains("sqlite"));
        assertFalse(installedGems.contains("sqlite3-ruby"));

        assertEquals("1.0.0", gemManager.getVersion("foo"));
        assertEquals(null, gemManager.getVersion("notagem"));
        assertEquals(null, gemManager.getVersion("nosuchgem"));
        assertEquals(null, gemManager.getVersion("sqlite"));
        assertEquals(null, gemManager.getVersion("sqlite3-ruby"));
        assertEquals("1.0.0", gemManager.getVersion("mongrel"));
        assertEquals("0.3.3", gemManager.getVersion("bar-baz"));
        assertEquals("0.1.1", gemManager.getVersion("pdf-writer"));
        assertEquals("1.15.3.6752", gemManager.getVersion("activerecord"));
    }

    // XXX
//    public void testFindGemExecutableWith_GEM_HOME() throws Exception {
//        File gemRepo = new File(getWorkDir(), "gemrepo");
//        File gemRepoBinF = new File(gemRepo, "bin");
//        gemRepoBinF.mkdirs();
//        RubyPlatform platform = RubyPlatformManager.addPlatform(setUpRuby(), "ruby");
//        GemManager.TEST_GEM_HOME = gemRepo.getAbsolutePath();
//        touch("rdebug-ide", gemRepoBinF.getAbsolutePath());
//        assertNotNull(platform.getGemManager().findGemExecutable("rdebug-ide"));
//    }

}
