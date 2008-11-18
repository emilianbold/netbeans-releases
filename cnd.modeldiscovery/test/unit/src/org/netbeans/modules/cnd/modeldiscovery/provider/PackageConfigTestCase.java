/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modeldiscovery.provider;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.discovery.api.PkgConfigManager.PackageConfiguration;
import org.netbeans.modules.cnd.discovery.api.PkgConfigManager.ResolvedPath;
import org.openide.util.NbPreferences;
import org.netbeans.modules.cnd.test.BaseTestCase;

/**
 *
 * @author Alexander Simon
 */
public class PackageConfigTestCase extends BaseTestCase {

    public PackageConfigTestCase(String testName) {
        super(testName);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testGtkPackage() {
        // Test requires cygwin on Windows platform
        // Test requires package gtk+-2.0
        // I do not know how the test will work on Mac. Help me to make test working on Mac.
        // If you computer do not have a needed software, install it. It help us to find bugs.
        Logger logger = Logger.getLogger(NbPreferences.class.getName());
        logger.setLevel(Level.SEVERE);
        PkgConfigImpl pc = (PkgConfigImpl) new PkgConfigManagerImpl().getPkgConfig(null);
        basicTest(pc);
    }

    private void basicTest(PkgConfigImpl pc) {
        String packageName = "gtk+-2.0";
        pc.traceConfig(packageName, true);
        pc.traceRecursiveConfig(packageName);
        //pc.trace();
        assert pc.getPkgConfig(packageName) != null;
        String include = "gtk/gtk.h";
        ResolvedPath rp = pc.getResolvedPath(include);
        assert rp != null;
        System.out.println("Resolved include paths");
        String path = rp.getIncludePath();
        System.out.println("Include: " + include);
        System.out.println("Path:    " + path);
        StringBuilder packages = new StringBuilder();
        for (PackageConfiguration pkg : rp.getPackages()) {
            System.out.print("Package: " + pkg.getName());
            packages.append(pkg.getName() + " ");
            StringBuilder buf = new StringBuilder();
            for (String p : pkg.getIncludePaths()) {
                if (buf.length() > 0) {
                    buf.append(", ");
                }
                buf.append(p);
            }
            System.out.println("\t[" + buf.toString() + "]");
        }
        assert packages.toString().indexOf(packageName + " ") >= 0;

    }

    @Test
    public void testRemoteGtkPackage() {
        if (canTestRemote()) { //TODO: 10k vs 3
            PlatformInfo pi = PlatformInfo.getDefault(getHKey());
            PkgConfigImpl pc = new PkgConfigImpl(pi, null);
            basicTest(pc);
        }
    }
}