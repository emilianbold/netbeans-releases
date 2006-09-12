/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * PkgCreateTestTest.java
 *
 * Created on August 2, 2006, 1:39 PM
 */

package org.netbeans.test.junit.pkgtestcreation;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
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
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(PkgCreateTestTest.class);
        return suite;
    }
    
    /**
     * Creates tests for whole package 
     */ 
    public void testCreateTestForPackage() {
        //open sample package
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        Utilities.pushCreateTestsPopup(n);
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
