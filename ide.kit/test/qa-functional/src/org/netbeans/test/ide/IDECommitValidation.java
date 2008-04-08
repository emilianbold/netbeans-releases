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

package org.netbeans.test.ide;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Overall sanity check suite for IDE before commit.<br>
 * Look at IDEValidation.java for test specification and implementation.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class IDECommitValidation extends JellyTestCase {
    
    
    /** Need to be defined because of JUnit */
    public IDECommitValidation(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        if (System.getProperty("xtest.ide.blacklist") != null) {
            suite.addTest(new IDEValidation("testBlacklistedClassesHandler"));
        }
        suite.addTest(new IDEValidation("testInitGCProjects"));
        suite.addTest(new IDEValidation("testMainMenu"));
        suite.addTest(new IDEValidation("testHelp"));
        suite.addTest(new IDEValidation("testOptions"));
        suite.addTest(new IDEValidation("testNewProject"));
        // sample project must exist before testShortcuts
        suite.addTest(new IDEValidation("testShortcuts"));
        suite.addTest(new IDEValidation("testNewFile"));
        suite.addTest(new IDEValidation("testCVSLite"));
        suite.addTest(new IDEValidation("testProjectsView"));
        suite.addTest(new IDEValidation("testFilesView"));
        suite.addTest(new IDEValidation("testEditor"));
        suite.addTest(new IDEValidation("testBuildAndRun"));
        suite.addTest(new IDEValidation("testDebugging"));
        suite.addTest(new IDEValidation("testJUnit"));
        suite.addTest(new IDEValidation("testXML"));
        suite.addTest(new IDEValidation("testDb"));
        suite.addTest(new IDEValidation("testWindowSystem"));
        suite.addTest(new IDEValidation("testGCProjects"));
        // not in commit suite because it needs net connectivity
        // suite.addTest(new IDEValidation("testPlugins"));
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
        // run only selected test case
        //junit.textui.TestRunner.run(new IDEValidation("testMainMenu"));
    }
}
