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

package org.netbeans.modules.php.project.ui.actions.tests;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.project.util.PhpUnit;
import org.netbeans.modules.php.project.util.TestUtils;
import org.openide.util.Utilities;

/**
 * @author Tomas Mysik
 */
public class CreateTestsActionTest extends NbTestCase {

    public CreateTestsActionTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtils.init();
    }

    public void testRequireOnceUnix() throws Exception {
        if (!Utilities.isUnix()) {
            return;
        }
        List<Crate> crates = new LinkedList<Crate>();
        crates.add(new Crate(
                "/project1/src/MyClass.php",
                "/project1/test/MyClassTest.php",
                PhpUnit.REQUIRE_ONCE_REL_PART + "../src/MyClass.php"));
        crates.add(new Crate(
                "/project1/src/a/MyClass.php",
                "/project1/test/a/MyClassTest.php",
                PhpUnit.REQUIRE_ONCE_REL_PART + "../../src/a/MyClass.php"));
        crates.add(new Crate(
                "/project1/src/a/b/c/MyClass.php",
                "/project1/test/a/b/c/MyClassTest.php",
                PhpUnit.REQUIRE_ONCE_REL_PART + "../../../../src/a/b/c/MyClass.php"));

        verifyCrates(crates);
    }

    public void testRequireOnceWindows() throws Exception {
        if (!Utilities.isWindows()) {
            return;
        }
        List<Crate> crates = new LinkedList<Crate>();
        crates.add(new Crate(
                "C:\\project1\\src\\MyClass.php",
                "C:\\project1\\test\\MyClassTest.php",
                PhpUnit.REQUIRE_ONCE_REL_PART + "../src/MyClass.php"));
        crates.add(new Crate(
                "C:\\project1\\src\\MyClass.php",
                "D:\\project1\\test\\MyClassTest.php",
                "C:/project1/src/MyClass.php"));

        verifyCrates(crates);
    }

    private void verifyCrates(List<Crate> creates) {
        for (Crate crate : creates) {
            String requireOnce = PhpUnit.getRequireOnce(crate.testFile, crate.sourceFile);
            assertNotNull(requireOnce);
            assertEquals(crate.requireOnce, requireOnce);
        }
    }

    private static final class Crate {
        public final File sourceFile;
        public final File testFile;
        public final String requireOnce;

        public Crate(String sourceFile, String testFile, String requireOnce) {
            this.sourceFile = new File(sourceFile);
            this.testFile = new File(testFile);
            this.requireOnce = requireOnce;
        }
    }
}
