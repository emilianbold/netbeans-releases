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

package golden;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbTestCase;

/** Example of golden file approach.
 * For each test case method there should exist <methodName>.pass file
 * containing expected output (AKA golden file). It resides in package
 * data.goldenfiles.GoldenTest1.
 */
public class GoldenTest1 extends NbTestCase {


    public GoldenTest1(String testName) {
        super(testName);
    }

    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite(GoldenTest1.class));
    }
    
    /** Uses golden files approach. Messages are written to the reference file
     * by the ref() method. At the end tearDown() method satisfy comparing with
     * golden file.<br>
     * Of course, you can also use fail() and assert() methods within the test to indicate
     * a test failure.
     */
    public void testPart1() throws Exception {
        // print path where log and ref files will be stored
        System.out.println("WORKDIR="+getWorkDirPath());
        ref("Output to ref file called testPart1.ref");
        log("message to log file testPart1.log");
        log("mySpecialLog.log", "message to log file mySpecialLog.log");
        // here write body of the test
        // ....
        // compares created testPart1.ref to testPart1.pass
        compareReferenceFiles();
    }
    
    
    /** This method should fail because contents of reference file and
     * golden file will differ.
     */
    public void testPart2() throws Exception {
        ref("Output to ref file called testPart2.ref");
        log("message to log file testPart2.log");
        // here write body of the test
        // ....
        log("Finished.");
        // compares created testPart2.ref to testPart2.pass
        compareReferenceFiles();
    }
    
}
