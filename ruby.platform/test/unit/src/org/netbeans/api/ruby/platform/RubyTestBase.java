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
import org.netbeans.junit.NbTestCase;
import org.openide.modules.InstalledFileLocator;

public abstract class RubyTestBase extends NbTestCase {

    public RubyTestBase(final String name) {
        super(name);
    }

    public static final class IFL extends InstalledFileLocator {
        public IFL() {}
        public @Override File locate(String relativePath, String codeNameBase, boolean localized) {
            if (relativePath.equals("ruby/debug-commons-0.9.5/classic-debug.rb")) {
                File rubydebugDir = getDirectory("rubydebug.dir", true);
                File cd = new File(rubydebugDir, "classic-debug.rb");
                assertTrue("classic-debug found in " + rubydebugDir, cd.isFile());
                return cd;
            } else if (relativePath.equals("jruby-1.0.2")) {
                return TestUtil.getXTestJRubyHome();
            } else {
                return null;
            }
        }
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
        RubyInstallation.TEST_GEM_HOME = ""; // non null but also invalid dir, will bypass $GEM_HOME lookup
        
        File home = getWorkDir();

        // Build a fake ruby structure
        File bin = new File(home, "bin");
        bin.mkdirs();
        File ruby = new File(bin, "ruby" + suffix);
        ruby.createNewFile();
        File rdoc = new File(bin, "rdoc" + suffix);
        rdoc.createNewFile();

        if (withGems) {
            // Build a fake rubygems repository
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
            File gembin = new File(gems, version + File.separator + "bin");
            gembin.mkdirs();
        }
        return ruby;
//        return new RubyInstallation(ruby.getAbsolutePath());
    }

}
