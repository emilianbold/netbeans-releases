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
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
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
public class RenameComponentVariableTest extends ExtJellyTestCase {
    private String FILE_NAME = "RenameComponentVariableTestFrame"; // NOI18N
    private String JAVA_FILE_NAME = "RenameComponentVariableTestFrame"; // NOI18N    
    private String VARIABLE_OLD_NAME = "jButton1"; // NOI18N
    private String VARIABLE_NEW_NAME = "myNewButton"; // NOI18N
    private String NODE_PATH = "[JFrame]|" + VARIABLE_OLD_NAME + " [JButton]"; // NOI18N
    
    /**
     * Constructor required by JUnit
     * @param testName
     */
    public RenameComponentVariableTest(String testName) {
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
        suite.addTest(new RenameComponentVariableTest("testRefactoring")); // NOI18N
        suite.addTest(new RenameComponentVariableTest("testChangesInJavaFile")); // NOI18N
        suite.addTest(new RenameComponentVariableTest("testChangesInFormFile")); // NOI18N
        suite.addTest(new RenameComponentVariableTest("testChangesInPropertiesFile")); // NOI18N
        return suite;
    }
    
    /** Runs refactoring  */
    public void testRefactoring() {
        openFile(JAVA_FILE_NAME);
        FormDesignerOperator designer = new FormDesignerOperator(JAVA_FILE_NAME);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        Node node = new Node(inspector.treeComponents(), NODE_PATH);
        
        runNoBlockPopupOverNode("Change Variable Name ...", node); // NOI18N
        
        JDialogOperator dialog = new JDialogOperator("Rename"); // NOI18N
        new JTextFieldOperator(dialog).setText(VARIABLE_NEW_NAME);
        new JButtonOperator(dialog,"OK").clickMouse(); // NOI18N
    }
    
    /** Tests content of java file */
    public void testChangesInJavaFile() {
        openFile(JAVA_FILE_NAME);
        FormDesignerOperator designer = new FormDesignerOperator(JAVA_FILE_NAME);
        
        ArrayList<String> lines = new ArrayList<String>();

        // local variable in my own method with same name
        lines.add("JButton jButton2 = jButton1;"); // NOI18N

        // using renamed button in my own method
        lines.add("String actualButtonText = this.myNewButton.getText();"); // NOI18N
        
        // declaration of renames button
        lines.add("private javax.swing.JButton myNewButton;"); // NOI18N
        
        // renamed event handler name
        lines.add("private void myNewButtonActionPerformed("); // NOI18N
        
        // my own code inside frame contructor
        lines.add("myNewButton.setEnabled(true);"); // NOI18N
        
        // event handling code of renamed button
        lines.add("myNewButton.addActionListener(new"); // NOI18N
        lines.add("myNewButtonActionPerformed(evt);"); // NOI18N
        
        // binding code with renamed button
        lines.add("bindingContext.addBinding(myNewButton,"); // NOI18N
        
        // initialization of renamed button inside
        lines.add("myNewButton = new javax.swing.JButton()"); // NOI18N
        
        // issue 105649 - getting model from renamed button inside generated event handler method
        lines.add("ButtonModel model = myNewButton.getModel();"); // NOI18N
        
        findInCode(lines, designer);
    }
    
    /** Tests changes in form file */
    public void testChangesInFormFile() {
        String sourceFilePath = getFilePathFromDataPackage(FILE_NAME + ".form"); // NOI18N
            
        assertTrue("Old variable name \""+VARIABLE_OLD_NAME+"\" found in " + FILE_NAME + ".form file.",
                !findInFile(VARIABLE_OLD_NAME,sourceFilePath)
                ); // NOI18N
    }

    /** Test changes in property bundle file */
    public void testChangesInPropertiesFile() {
        String sourceFilePath = getFilePathFromDataPackage("Bundle.properties"); // NOI18N
        
        assertTrue("Old variable name \""+VARIABLE_OLD_NAME+"\" found in Bundle.properties file.",
                !findInFile(VARIABLE_OLD_NAME,sourceFilePath)
                ); // NOI18N
    }
}    
