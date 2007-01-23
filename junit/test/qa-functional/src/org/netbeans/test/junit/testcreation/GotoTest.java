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

package org.netbeans.test.junit.testcreation;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.junit.utils.Utilities;

/**
 * Tests "Goto Test" action
 * @author Max Sauer
 */
public class GotoTest extends NbTestCase {
    private static final String TEST_PACKAGE_NAME =
            "org.netbeans.test.junit.testresults.test";
    
    /** Creates a new instance of GotoTest */
    public GotoTest(String testName) {
        super(testName);
    }
    
    /**
     * Adds tests to suite
     * @return created suite
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(GotoTest.class);
        return suite;
    }
    
    /**
     * Test selecting appropriate test from menu
     */
    public void selectTest() {
        //open sample class
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME+ "|" + Utilities.TEST_CLASS_NAME);
        
        JMenuBarOperator jbo = new JMenuBarOperator(
                MainWindowOperator.getDefault().getJMenuBar());
        String[] sf = {Bundle.getStringTrimmed("org.netbeans.core.Bundle",
                "Menu/Window"),
                Bundle.getStringTrimmed("org.netbeans.modules.junit.Bundle",
                "LBL_Action_GoToTest")};
        jbo.pushMenu(sf);
        EditorOperator eo = new EditorOperator(""); //TODO: finish
        
        
    }
}
