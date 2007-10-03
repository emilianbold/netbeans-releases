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

package org.netbeans.modules.junit;

import org.openide.TopManager;
import junit.framework.*;
import junit.runner.*;
import java.util.*;
import java.io.*;
import junit.framework.*;

public class TestRunnerTest extends TestCase {

    public TestRunnerTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestRunnerTest.class);

        return suite;
    }
    
    /** Test of getLoader method, of class org.netbeans.modules.junit.TestRunner. */
    public void testGetLoader() {
        System.out.println("testGetLoader");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of addError method, of class org.netbeans.modules.junit.TestRunner. */
    public void testAddError() {
        System.out.println("testAddError");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of addFailure method, of class org.netbeans.modules.junit.TestRunner. */
    public void testAddFailure() {
        System.out.println("testAddFailure");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of endTest method, of class org.netbeans.modules.junit.TestRunner. */
    public void testEndTest() {
        System.out.println("testEndTest");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of startTest method, of class org.netbeans.modules.junit.TestRunner. */
    public void testStartTest() {
        System.out.println("testStartTest");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of doRun method, of class org.netbeans.modules.junit.TestRunner. */
    public void testDoRun() {
        System.out.println("testDoRun");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of runFailed method, of class org.netbeans.modules.junit.TestRunner. */
    public void testRunFailed() {
        System.out.println("testRunFailed");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of start method, of class org.netbeans.modules.junit.TestRunner. */
    public void testStart() {
        System.out.println("testStart");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
}
