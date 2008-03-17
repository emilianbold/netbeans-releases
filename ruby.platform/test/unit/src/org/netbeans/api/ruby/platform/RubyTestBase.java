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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.netbeans.api.ruby.platform.RubyPlatform.Info;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public abstract class RubyTestBase extends NbTestCase {
    
    private FileObject testRubyHome;

    public RubyTestBase(final String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        TestUtil.getXTestJRubyHome();
        System.setProperty("netbeans.user", getWorkDirPath());
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

    protected File setUpRuby() throws Exception {
        return setUpRuby(false, "");
    }

    protected File setUpRubyWithGems() throws Exception {
        return setUpRuby(true, "");
    }
    
    protected File setUpRuby(final boolean withGems, final String suffix) throws Exception {
        // Ensure that $GEM_HOME isn't picked up
        // I can't do this:
        //  System.getenv().remove("GEM_HOME");
        // because the environment variable map is unmodifiable. So instead
        // side effect to ensure that the GEM_HOME check isn't run
        GemManager.TEST_GEM_HOME = "invalid"; // non null but also invalid dir, will bypass $GEM_HOME lookup
        
        // Build a fake ruby structure
        testRubyHome = FileUtil.createFolder(FileUtil.toFileObject(getWorkDir()), "test_ruby");
        
        FileObject bin = testRubyHome.createFolder("bin");
        FileObject libRuby = FileUtil.createFolder(testRubyHome, "lib/ruby");
        libRuby.createFolder(RubyPlatform.DEFAULT_RUBY_RELEASE);
        FileObject interpreter = bin.createData("ruby" + suffix);
        String[] binaries = { "rdoc", "gem" };
        for (String binary : binaries) {
            bin.createData(binary + suffix);
        }

        Properties props = new Properties();
        props.put(Info.RUBY_KIND, "Ruby");
        props.put(Info.RUBY_VERSION, "0.1");
        props.put(Info.RUBY_RELEASE_DATE, "2000-01-01");
        props.put(Info.RUBY_PLATFORM, "abcd");
        if (withGems) {
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

    protected static void installFakeFastRubyDebugger(RubyPlatform platform) throws IOException {
        String gemplaf = platform.isJRuby() ? "java" : "";
        installFakeGem("ruby-debug-base", RubyPlatform.RDEBUG_BASE_VERSION, gemplaf, platform);
        installFakeGem("ruby-debug-ide", RubyPlatform.RDEBUG_IDE_VERSION, gemplaf, platform);
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

    protected FileObject getTestFile(String relFilePath) {
        File wholeInputFile = new File(getDataDir(), relFilePath);
        if (!wholeInputFile.exists()) {
            NbTestCase.fail("File " + wholeInputFile + " not found.");
        }
        FileObject fo = FileUtil.toFileObject(wholeInputFile);
        assertNotNull(fo);
        return fo;
    }

    
    protected FileObject touch(final String dir, final String path) throws IOException {
        return touch(new File(dir), path);
    }

    protected FileObject touch(final File dir, final String binary) throws IOException {
        if (!dir.isDirectory()) {
            assertTrue("success to create " + dir, dir.mkdirs());
        }
        FileObject dirFO = FileUtil.toFileObject(FileUtil.normalizeFile(dir));
        return FileUtil.createData(dirFO, binary);
    }

    /** Copy-pasted from APISupport. */
    protected static String slurp(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileUtil.copy(is, baos);
            return baos.toString("UTF-8");
        } finally {
            is.close();
        }
    }
    
}
