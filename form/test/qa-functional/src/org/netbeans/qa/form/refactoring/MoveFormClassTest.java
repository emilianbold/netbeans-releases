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

package org.netbeans.qa.form.refactoring;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.qa.form.*;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import java.io.File;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.Operator;


/**
 * Tests form refactoring, 3rd scenarion : Move form class into dif package
 *
 * @author Jiri Vagner
 */
public class MoveFormClassTest extends ExtJellyTestCase {
    private String CLASS_NAME = "FrameWithBundleToMove"; // NOI18N
//    private String CLASS_NAME = "ClassToMove"; // NOI18N    
    private String NEW_PACKAGE_NAME = "subdata";
    private String PACKAGE_NAME = TEST_PACKAGE_NAME + "." + NEW_PACKAGE_NAME; // NOI18N
    
    /**
     * Constructor required by JUnit
     * @param testName
     */
    public MoveFormClassTest(String testName) {
        super(testName);
    }
    
    /**
     * Method allowing to execute test directly from IDE.
     * @param args
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /**
     * Creates suite from particular test cases.
     * @return nb test suite
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new MoveFormClassTest("testRefactoring")); // NOI18N
        suite.addTest(new MoveFormClassTest("testChangesInJavaFile")); // NOI18N
        suite.addTest(new MoveFormClassTest("testChangesInPropertiesFile")); // NOI18N
        return suite;
    }

    /** Runs refactoring  */
    public void testRefactoring() {
        Node node = openFile(CLASS_NAME);

        runNoBlockPopupOverNode("Refactor|Move...", node); // NOI18N

        JDialogOperator dialog = new JDialogOperator("Move"); // NOI18N
        JComboBoxOperator combo = new JComboBoxOperator(dialog, 2);
        combo.selectItem(PACKAGE_NAME);

        new JButtonOperator(dialog,"Refactor").clickMouse();
    }
    
    /** Tests content of java file */
    public void testChangesInJavaFile() {
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(TEST_PROJECT_NAME);
        prn.select();

        waitNoEvent(45000);

        String path = "Source Packages|" + PACKAGE_NAME + "|" + CLASS_NAME + ".java"; // NOI18N
        //p(path);
        Node formnode = new Node(prn, path ); // NOI18N
        formnode.setComparator(new Operator.DefaultStringComparator(true, false));
        formnode.select();

        OpenAction openAction = new OpenAction();
        openAction.perform(formnode);

        //FormDesignerOperator designer = new FormDesignerOperator(CLASS_NAME);

        // new class package
        //findInCode("package data.subdata;", designer);
    }
    
    /** Test changes in property bundle file */
    public void testChangesInPropertiesFile() {
        String sourceFilePath = getFilePathFromDataPackage(NEW_PACKAGE_NAME
                                        + File.separator
                                        +"Bundle.properties");

        String key = "FrameWithBundleToMove.lanButton.text";
        assertTrue("Key \"" + key + "\" not found in Bundle.properties file.",
                findInFile( key, sourceFilePath)); // NOI18N
    }
}    
