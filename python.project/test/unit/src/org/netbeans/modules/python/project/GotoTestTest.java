/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.python.project;

import org.netbeans.modules.python.editor.PythonTestBase;
import org.netbeans.spi.gototest.TestLocator.LocationResult;

/**
 * @author Tor Norbye
 */
public class GotoTestTest extends PythonTestBase {

    private GotoTest gotoTest;

    public GotoTestTest(String testName) {
        super(testName);
    }
    private static LocationResult NONE = null;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        gotoTest = new GotoTest();
    }

    public void testGotoTestUnit() {
        LocationResult loc = gotoTest.findTest(getTestFile("testfiles/compl2.py"), -1);
        assertNotSame(NONE, loc);
        assertEquals(getTestFile("testfiles/tests/compl2_test.py"), loc.getFileObject());
    }

    public void testGotoTestUnit2() {
        LocationResult loc = gotoTest.findTested(getTestFile("testfiles/tests/compl2_test.py"), -1);
        assertNotSame(NONE, loc);
        assertEquals(getTestFile("testfiles/compl2.py"), loc.getFileObject());
    }

    public void testGotoTestUnit3() {
        LocationResult loc = gotoTest.findTest(getTestFile("testfiles/foo.py"), -1);
        assertNotSame(NONE, loc);
        assertEquals(getTestFile("testfiles/foo_test.py"), loc.getFileObject());
    }

    public void testGotoTestUnit4() {
        LocationResult loc = gotoTest.findTested(getTestFile("testfiles/foo_test.py"), -1);
        assertNotSame(NONE, loc);
        assertEquals(getTestFile("testfiles/foo.py"), loc.getFileObject());
    }

    public void testGotoTestUnit5() {
        LocationResult loc = gotoTest.findTest(getTestFile("testfiles/bar.py"), -1);
        assertNotSame(NONE, loc);
        assertEquals(getTestFile("testfiles/test_bar.py"), loc.getFileObject());
    }

    public void testGotoTestUnit6() {
        LocationResult loc = gotoTest.findTested(getTestFile("testfiles/test_bar.py"), -1);
        assertNotSame(NONE, loc);
        assertEquals(getTestFile("testfiles/bar.py"), loc.getFileObject());
    }

    public void testGotoTestUnit7() {
        LocationResult loc = gotoTest.findTest(getTestFile("testfiles/dir/baz.py"), -1);
        assertNotSame(NONE, loc);
        assertEquals(getTestFile("testfiles/tests/test_baz.py"), loc.getFileObject());
    }

    public void testGotoTestUnit8() {
        LocationResult loc = gotoTest.findTest(getTestFile("testfiles/whatever.py"), -1);
        assertNotSame(NONE, loc);
        assertEquals(getTestFile("testfiles/testwhatever.py"), loc.getFileObject());
    }

    // Not yet working
    //public void testGotoTestUnit8() {
    //    LocationResult loc = gotoTest.findTested(getTestFile("testfiles/tests/test_baz.py"), -1);
    //    assertNotSame(NONE, loc);
    //    assertEquals(getTestFile("testfiles/dir/baz.py"), loc.getFileObject());
    //}
}
