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

/*
 * PkgCreateTestTest.java
 *
 * Created on August 2, 2006, 1:39 PM
 */

package org.netbeans.test.junit.pkgtestcreation;

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.junit.testcase.JunitTestCase;
import org.netbeans.test.junit.utils.Utilities;

/**
 *
 * @author ms159439
 */
public class PkgCreateTestTest extends JunitTestCase {
    
    /** path to sample files */
    private static final String TEST_PACKAGE_PATH =
            "org.netbeans.test.junit.pkgtestcreation";
    
    /** name of sample package */
    private static final String TEST_PACKAGE_NAME = TEST_PACKAGE_PATH+".test";
    
    /**
     * Creates a new instance of PkgCreateTestTest
     */
    public PkgCreateTestTest(String testName) {
        super(testName);
    }
    
    /**
     * Adds tests to suite
     * @return created suite
     */
    public static Test suite() {
//        NbTestSuite suite = new NbTestSuite(PkgCreateTestTest.class);
//        return suite;
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(PkgCreateTestTest.class).addTest(
                "testCreateTestForPackage").enableModules(".*").clusters(".*"));
    }
    
    /**
     * Creates tests for whole package 
     */ 
    public void testCreateTestForPackage() {
        //open sample package
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
//        Utilities.pushCreateTestsPopup(n);
        n.performPopupActionNoBlock("Tools|Create JUnit Tests");
        NbDialogOperator ndo = new NbDialogOperator(CREATE_TESTS_DIALOG);
        Utilities.checkAllCheckboxes(ndo);
        ndo.btOK().push();
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        //Compare TestClassTest
        ref(filter.filter(new EditorOperator(Utilities.TEST_CLASS_NAME + "Test.java").getText()));
        compareReferenceFiles();
        
        //compare the created test suite TestSuite.java
//        compareReferenceFiles(Utilities.openFile(Utilities.TEST_PACKAGES_PATH +
//                "|" + TEST_PACKAGE_NAME + "|" + "TestSuite").getPath(),
//                getGoldenFile().getParent() + "Suite.pass",
//                getWorkDirPath() + getName() + ".diff"
//                );
        
//        Utilities.deleteNode(Utilities.TEST_PACKAGES_PATH +
//                "|" + TEST_PACKAGE_NAME);
        
    }
    
}
