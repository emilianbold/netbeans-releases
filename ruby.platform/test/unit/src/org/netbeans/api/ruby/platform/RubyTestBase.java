/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
import java.util.Properties;
import org.netbeans.api.ruby.platform.RubyPlatform.Info;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public abstract class RubyTestBase extends CslTestBase {

    private FileObject testRubyHome;

    public RubyTestBase(final String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        TestUtil.getXTestJRubyHome();
    }

    public File getTestRubyHome() {
        return FileUtil.toFile(testRubyHome);
    }

    private static File resolveFile(final String property, final boolean mandatory) {
        String path = System.getProperty(property);
        assertTrue("must set " + property, !mandatory || (path != null));
        return path == null ? null : new File(path);
    }

    static File getFile(final String property, boolean mandatory) {
        File file = resolveFile(property, mandatory);
        assertTrue(file + " is file", !mandatory || file.isFile());
        return file;

    }

    static File getDirectory(final String property, final boolean mandatory) {
        File directory = resolveFile(property, mandatory);
        assertTrue(directory + " is directory", !mandatory || directory.isDirectory());
        return directory;
    }

    protected RubyPlatform setUpPlatform() throws IOException {
        return setUpPlatform(false, "");
    }

    protected RubyPlatform setUpPlatformWithRubyGems() throws IOException {
        return setUpPlatform(true, "");
    }

    protected RubyPlatform setUpPlatform(final boolean withGems, final String suffix) throws IOException {
        return RubyPlatformManager.addPlatform(setUpRuby("Ruby", withGems, suffix));
    }

    protected RubyPlatform setUpRubinius() throws IOException {
        return RubyPlatformManager.addPlatform(setUpRuby("Rubinius", false, ""));
    }

    private File setUpRuby(final String kind, final boolean withGems, final String suffix) throws IOException {
        // Ensure that $GEM_HOME isn't picked up
        // I can't do this:
        //  System.getenv().remove("GEM_HOME");
        // because the environment variable map is unmodifiable. So instead
        // side effect to ensure that the GEM_HOME check isn't run
        GemManager.TEST_GEM_HOME = "invalid"; // non null but also invalid dir, will bypass $GEM_HOME lookup
        boolean isRubinius = kind.equals("Rubinius");

        // Build a fake ruby structure
        testRubyHome = FileUtil.createFolder(FileUtil.toFileObject(getWorkDir()), "test_ruby");

        FileObject bin = testRubyHome.createFolder("bin");
        FileObject libRuby = FileUtil.createFolder(testRubyHome, "lib");
        FileObject libRuby18 = null;
        if (!isRubinius) {
            libRuby = FileUtil.createFolder(testRubyHome, "lib/ruby");
            libRuby18 = libRuby.createFolder(RubyPlatform.DEFAULT_RUBY_RELEASE);
        }

        FileObject interpreter = isRubinius
                ? FileUtil.createData(testRubyHome, "shotgun/rubinius")
                : bin.createData("ruby" + suffix);
        String[] binaries = { "irb", "gem", "rdoc" };
        for (String binary : binaries) {
            bin.createData(binary + suffix);
        }

        Properties props = new Properties();
        props.put(Info.RUBY_KIND, kind);
        props.put(Info.RUBY_VERSION, "0.1");
        props.put(Info.RUBY_RELEASE_DATE, "2000-01-01");
        props.put(Info.RUBY_PLATFORM, "abcd");
        props.put(Info.RUBY_PLATFORM, "abcd");
        if (!isRubinius) {
            props.put(Info.RUBY_LIB_DIR, FileUtil.toFile(libRuby18).getAbsolutePath());
        }
        if (withGems) {
            assertFalse("setuping Rubinius with RubyGems is not supported yet", isRubinius);
            // Build a fake rubygems repository
            FileObject gemRepo = FileUtil.createFolder(libRuby, "gems/" + RubyPlatform.DEFAULT_RUBY_RELEASE);
            GemManager.initializeRepository(gemRepo);
            gemRepo.createFolder("bin");
            props.put(Info.GEM_HOME, FileUtil.toFile(gemRepo).getAbsolutePath());
            props.put(Info.GEM_PATH, "/tmp/a/b/c");
            props.put(Info.GEM_VERSION, "0.2");
        }

        RubyPlatformManager.TEST_RUBY_PROPS = props;
        return FileUtil.toFile(interpreter);
    }

    /**
     * Return {@link RubyPlatformManager#getDefaultPlatform default platform}
     * with customized Gem Home and empty Gem Path so it is safe to treat it in
     * read-write mode.
     */
    protected RubyPlatform getSafeJRuby() throws IOException {
        RubyPlatform jruby = RubyPlatformManager.getDefaultPlatform();
        FileObject gemRepo = FileUtil.toFileObject(getWorkDir()).createFolder("gem-repo");
        GemManager.initializeRepository(gemRepo);
        jruby.setGemHome(FileUtil.toFile(gemRepo));
        jruby.getInfo().setGemPath("");
        return jruby;
    }

    protected static void installFakeFastRubyDebugger(RubyPlatform platform) throws IOException {
        String gemplaf = platform.isJRuby() ? "java" : "";
        installFakeGem("ruby-debug-ide", "0.4.6", gemplaf, platform);
    }

    protected static void uninstallFakeGem(final String name, final String version, final String actualPlatform, final RubyPlatform platform) throws IOException {
        FileObject gemHome = platform.getGemManager().getGemHomeFO();
        String gemplaf = actualPlatform == null ? "" : "-" + actualPlatform;
        FileObject gem = gemHome.getFileObject("specifications/" + name + '-' + version + gemplaf + ".gemspec");
        gem.delete();
        platform.getGemManager().reset();
    }

    protected static void uninstallFakeGem(final String name, final String version, final RubyPlatform platform) throws IOException {
        uninstallFakeGem(name, version, null, platform);
    }

    protected static void installFakeGem(final String name, final String version, final String actualPlatform, final RubyPlatform platform) throws IOException {
        FileObject gemHome = platform.getGemManager().getGemHomeFO();
        String gemplaf = actualPlatform == null ? "" : "-" + actualPlatform;
        FileUtil.createData(gemHome, "specifications/" + name + '-' + version + gemplaf + ".gemspec");
        platform.getGemManager().reset();
    }

    protected static void installFakeGem(final String name, final String version, final RubyPlatform platform) throws IOException {
        installFakeGem(name, version, null, platform);
    }
}
