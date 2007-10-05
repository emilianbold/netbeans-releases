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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.xtest.plugin.ide.test;

import java.io.File;
import java.util.Hashtable;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.xtest.pe.xmlbeans.UnitTestSuite;
import org.netbeans.xtest.pe.xmlbeans.UnitTestCase;

/** Check test results of XTestErrorManagerTest. At the end delete those results
 * because they contain errors.
 */
public class XTestErrorManagerCheckTest extends NbTestCase {
    
    /** Need to be defined because of JUnit */
    public XTestErrorManagerCheckTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new XTestErrorManagerCheckTest("testInformational"));
        suite.addTest(new XTestErrorManagerCheckTest("testException"));
        suite.addTest(new XTestErrorManagerCheckTest("testError"));
        suite.addTest(new XTestErrorManagerCheckTest("testErrorInTest"));
        suite.addTest(new XTestErrorManagerCheckTest("testErrorInTestExceptionRemembered"));
        suite.addTest(new XTestErrorManagerCheckTest("testErrorWithFail"));
        suite.addTest(new XTestErrorManagerCheckTest("testDeleteSuite"));
        return suite;
    }
    
    private static Hashtable unitTestCases;
    private static File suiteResultsFile = new File(
            new File(System.getProperty("xtest.workdir")),
            "xmlresults/suites/TEST-org.netbeans.xtest.plugin.ide.test.XTestErrorManagerTest.xml"
    );
    private UnitTestCase unitTestCase;
    
    /** Load XTestErrorManagerTest results from xml file. */
    public void setUp() throws Exception {
        System.out.println("########  "+getName()+"  #######");
        if(unitTestCases == null) {
            unitTestCases = new Hashtable();
            UnitTestSuite unitTestSuite = UnitTestSuite.loadFromFile(suiteResultsFile);
            for(int i=0;i<unitTestSuite.xmlel_UnitTestCase.length;i++) {
                unitTestCases.put(unitTestSuite.xmlel_UnitTestCase[i].getName(), unitTestSuite.xmlel_UnitTestCase[i]);
            }
        }
        unitTestCase = (UnitTestCase)unitTestCases.get(getName());
    }
    
    /** XTestErrorManagerTest.testInformational should pass. */
    public void testInformational() {
        assertEquals("INFORMATIONAL messages should not be caugth by XTest.", "pass", unitTestCase.getResult());
    }
    
    /** XTestErrorManagerTest.testException should be error. */
    public void testException() {
        assertEquals("EXCEPTION severity mesagges should be caugth by XTest.", "error", unitTestCase.getResult());
    }
    
    /** XTestErrorManagerTest.testError should be error. */
    public void testError() {
        assertEquals("ERROR severity mesagges should be caugth by XTest.", "error", unitTestCase.getResult());
    }
    
    /** XTestErrorManagerTest.testErrorInTest should report error from test. */
    public void testErrorInTest() {
        assertEquals("Error in test should be reported.", "error", unitTestCase.getResult());
        assertEquals("Error in test should precede severe message from IDE.", "/ by zero", unitTestCase.getMessage());
    }
    
    /** XTestErrorManagerTest.testErrorInTestExceptionRemembered should be error. */
    public void testErrorInTestExceptionRemembered() {
        assertEquals("Exception from IDE should be remembered if cannot be reported in previous tast case.", "error", unitTestCase.getResult());
        assertEquals("Wrong message.", "testErrorInTest", unitTestCase.getMessage());
    }
    
    /** XTestErrorManagerTest.testErrorWithFail should be error. */
    public void testErrorWithFail() {
        assertEquals("Exception from IDE should precede failure in test.", "error", unitTestCase.getResult());
        assertEquals("First exception message should be reported.", "testErrorWithFail1", unitTestCase.getMessage());
    }
    
    /** Delete XTestErrorManagerTest results because they contain wanted errors. */
    public void testDeleteSuite() {
        assertTrue("Cannot delete suite file "+suiteResultsFile, suiteResultsFile.delete());
    }
}