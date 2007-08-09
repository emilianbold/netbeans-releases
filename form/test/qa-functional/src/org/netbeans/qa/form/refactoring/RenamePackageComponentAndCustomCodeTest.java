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
import org.netbeans.jemmy.operators.JDialogOperator;
import java.util.ArrayList;
import org.netbeans.jellytools.actions.CompileAction;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;


/**
 * Tests form refactoring : Refactoring custom component name, custom code and package name
 *
 * @author Jiri Vagner
 */
public class RenamePackageComponentAndCustomCodeTest extends ExtJellyTestCase {
    private String FORM_NAME = "CustomComponentForm"; // NOI18N
    private String OLD_COMPONENT_NAME = "CustomButton"; // NOI18N    
    private String NEW_COMPONENT_NAME = OLD_COMPONENT_NAME + "Renamed"; // NOI18N    
    private String OLD_PACKAGE_NAME = "data.components"; // NOI18N
    private String NEW_PACKAGE_NAME = "data.renamedcomponents"; // NOI18N
    
    /**
     * Constructor required by JUnit
     * @param testName
     */
    public RenamePackageComponentAndCustomCodeTest(String testName) {
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
        suite.addTest(new RenamePackageComponentAndCustomCodeTest("testRefactoringComponentName")); // NOI18N
        suite.addTest(new RenamePackageComponentAndCustomCodeTest("testChangesInJavaFile")); // NOI18N
        return suite;
    }

    /** Runs refactoring  */
    public void testRefactoringComponentName() {
        Node compNode = getProjectFileNode(OLD_COMPONENT_NAME, OLD_PACKAGE_NAME);

        // custom component rename
        runNoBlockPopupOverNode("Refactor|Rename...", compNode); // NOI18N
        JDialogOperator dialog = new JDialogOperator("Rename"); // NOI18N
        new JTextFieldOperator(dialog).typeText(NEW_COMPONENT_NAME);
        new JButtonOperator(dialog,"Refactor").clickMouse(); // NOI18N
        waitNoEvent(1000);
        
        // custom component package rename
        Node node = getProjectFileNode(OLD_PACKAGE_NAME, true);
        runNoBlockPopupOverNode("Rename...", node); // NOI18N
        
        // rename dialog ...
        dialog = new JDialogOperator("Rename"); // NOI18N
        new JTextFieldOperator(dialog).typeText(NEW_PACKAGE_NAME);
        new JButtonOperator(dialog,"OK").clickMouse(); // NOI18N
        
        // ... refactoring dialog
        dialog = new JDialogOperator("Rename"); // NOI18N
        new JButtonOperator(dialog,"Refactor").clickMouse(); // NOI18N
        waitNoEvent(45000);
        
        // compiling component to avoid load form error
        compNode = getProjectFileNode(NEW_COMPONENT_NAME, NEW_PACKAGE_NAME);
        new CompileAction().perform(compNode);
    }
    
    /** Tests content of java file */
    public void testChangesInJavaFile() {
        openFile(FORM_NAME);
        FormDesignerOperator designer = new FormDesignerOperator(FORM_NAME);
        
        ArrayList<String> lines = new ArrayList<String>();
        
        // custom components refatoring
        lines.add("customButton1 = new data.renamedcomponents.CustomButtonRenamed();"); // NOI18N

        // custom code refactoring
        lines.add("jButton1 = data.renamedcomponents.CustomButtonRenamed.createButton();"); // NOI18N

        // custom component field refactoring
        lines.add("private data.renamedcomponents.CustomButtonRenamed customButton1;"); // NOI18N
        
        findInCode(lines, designer);
    }
}    
