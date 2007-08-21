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
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JMenuOperator;
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
    
    private static final String TEST_PACKAGE_PACKAGEGOTO_NAME =
            "org.netbeans.test.junit.go";
    
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
     * Test selecting appropriate test from Main menu
     */
    public void testSelectTestFromMainMenu() {
        //open sample class
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME+ "|" + Utilities.TEST_CLASS_NAME);
        
        EditorOperator eos = new EditorOperator(Utilities.TEST_CLASS_NAME);
        
        JMenuBarOperator jbo = new JMenuBarOperator(
                MainWindowOperator.getDefault().getJMenuBar());
        String[] sf = {Bundle.getStringTrimmed("org.netbeans.core.Bundle",
                "Menu/GoTo"),
                Bundle.getStringTrimmed("org.netbeans.modules.junit.Bundle",
                "LBL_Action_GoToTest")};
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);        
        jbo.pushMenu(sf[0]);
        JMenuItemOperator jmio = new JMenuItemOperator(new JMenuOperator(jbo, sf[0]).getItem(0));
        //Check if goto test is enabled inside menu
        assertTrue("Goto Test disabled when invoked from Editor!", jmio.isEnabled());
        jbo.pushMenu(sf);
        //Operator for opened TestClassTest
        EditorOperator eo = new EditorOperator(Utilities.TEST_CLASS_NAME + "Test");
        assertTrue("Test for \"" + TEST_PACKAGE_NAME +
                Utilities.TEST_CLASS_NAME + "\" not opened!", eo.isVisible());
        eo.close(false);
        eos.close(false);
    }
    
    /**
     * Test selecting appropriate test from Edior's context menu
     */
    public void testSelectTestFromExplorer() {
        //select sample class in explorer
        Node pn = new ProjectsTabOperator().getProjectRootNode(
                Utilities.TEST_PROJECT_NAME);
        pn.select();
        Node n = new Node(pn, Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME+ "|" + Utilities.TEST_CLASS_NAME);
        n.select();
        
        JMenuBarOperator jbo = new JMenuBarOperator(
                MainWindowOperator.getDefault().getJMenuBar());
        
        String[] sf = {Bundle.getStringTrimmed("org.netbeans.core.Bundle",
                "Menu/GoTo"),
                Bundle.getStringTrimmed("org.netbeans.modules.junit.Bundle",
                "LBL_Action_GoToTest")};
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);        
        jbo.pushMenu(sf[0]);
        JMenuItemOperator jmio = new JMenuItemOperator(new JMenuOperator(jbo, sf[0]).getItem(0));
        //Check if goto test is enabled inside menu
        assertTrue("Goto Test disabled when invoked from Explorer, over a class node!" +
                "see: http://www.netbeans.org/issues/show_bug.cgi?id=88599",
                jmio.isEnabled());
        jbo.pushMenu(sf);
        EditorOperator eot = new EditorOperator(Utilities.TEST_CLASS_NAME);
        assertTrue("Test for \"" + TEST_PACKAGE_NAME +
                Utilities.TEST_CLASS_NAME + "\" not opened!", eot.isVisible());
        eot.close(false);
    }
    
    /**
     * Tests selecting of suite test when invoking GoTo Test for a java package
     */
    public void testSelectTestFromExplorerPackage() {
        //select sample class in explorer
        Node pn = new ProjectsTabOperator().getProjectRootNode(
                Utilities.TEST_PROJECT_NAME);
        pn.select();
        Node n = new Node(pn, Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_PACKAGEGOTO_NAME);
        n.select(); //select the 'go' package from test project 
        
        JMenuBarOperator jbo = new JMenuBarOperator(
                MainWindowOperator.getDefault().getJMenuBar());
        
        String[] sf = {Bundle.getStringTrimmed("org.netbeans.core.Bundle",
                "Menu/GoTo"),
                Bundle.getStringTrimmed("org.netbeans.modules.junit.Bundle",
                "LBL_Action_GoToTest")};
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);        
        jbo.pushMenu(sf[0]);
        JMenuItemOperator jmio = new JMenuItemOperator(new JMenuOperator(jbo, sf[0]).getItem(0));
        //Check if goto test is enabled inside menu
        assertTrue("Goto Test disabled when invoked from Explorer, over a package node!" +
                "see: http://www.netbeans.org/issues/show_bug.cgi?id=88599",
                jmio.isEnabled());
        jbo.pushMenu(sf);
        Utilities.takeANap(3000);
        EditorOperator eot = new EditorOperator("GoSuite"); //test suite for the package
        assertTrue("Test suite for \"" + TEST_PACKAGE_PACKAGEGOTO_NAME +
                 "\" (GoSuite.java) not opened!", eot.isVisible());
        eot.close(false);
    }
    
    

}
