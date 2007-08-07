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

import java.util.ArrayList;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.qa.form.*;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Tests form refactoring, 1st scenarion : Rename component variable
 * and tests value and access rights of inherited properties
 *
 * @author Jiri Vagner
 */
public class RenameFormClassTest extends ExtJellyTestCase {
    private String CLASS_OLD_NAME = "FrameWithBundle"; // NOI18N
    private String CLASS_NEW_NAME = CLASS_OLD_NAME + "Renamed"; // NOI18N
    
    /**
     * Constructor required by JUnit
     * @param testName
     */
    public RenameFormClassTest(String testName) {
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
        suite.addTest(new RenameFormClassTest("testRefactoring")); // NOI18N
        suite.addTest(new RenameFormClassTest("testChangesInJavaFile")); // NOI18N
        suite.addTest(new RenameFormClassTest("testChangesInPropertiesFile")); // NOI18N
        return suite;
    }

    /** Runs refactoring  */
    public void testRefactoring() {
        Node node = openFile(CLASS_OLD_NAME);
        runNoBlockPopupOverNode("Refactor|Rename...", node); // NOI18N

        JDialogOperator dialog = new JDialogOperator("Rename"); // NOI18N
        new JTextFieldOperator(dialog).setText(CLASS_NEW_NAME);
        new JButtonOperator(dialog,"Refactor").clickMouse(); // NOI18N
        waitAMoment();
        waitAMoment();        
    }
    
    /** Tests content of java file */
    public void testChangesInJavaFile() {
        openFile(CLASS_NEW_NAME);
        FormDesignerOperator designer = new FormDesignerOperator(CLASS_OLD_NAME);
        
        ArrayList<String> lines = new ArrayList<String>();

        // new class name
        lines.add("public class FrameWithBundleRenamed"); // NOI18N

        // new class constructor name
        lines.add("public FrameWithBundleRenamed()"); // NOI18N
        
        // new key name
        lines.add("bundle.getString(\"FrameWithBundleRenamed.lanciaButton.text\")"); // NOI18N
        
        findInCode(lines, designer);
    }
    
    /** Test changes in property bundle file */
    public void testChangesInPropertiesFile() {
        String sourceFilePath = getFilePathFromDataPackage("Bundle.properties");
        //p(sourceFilePath);
        
        assertTrue("New class name \""+CLASS_NEW_NAME+"\" not found in Bundle.properties file.",
                findInFile(CLASS_NEW_NAME,sourceFilePath)); // NOI18N
    }
}    
