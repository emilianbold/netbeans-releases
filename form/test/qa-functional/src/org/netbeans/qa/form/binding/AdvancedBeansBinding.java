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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.qa.form.binding;

import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.*;
import org.netbeans.qa.form.ExtJellyTestCase;
import org.netbeans.jellytools.nodes.Node;
import java.util.*;
import org.netbeans.qa.form.BindDialogOperator;

/**
 * Beans Binding advanced test
 *
 * @author Jiri Vagner
 */
public class AdvancedBeansBinding extends ExtJellyTestCase {
    private String ACTION_PATH = "Bind|text";  // NOI18N
    private String BIND_EXPRESSION = "${text}";  // NOI18N
    private String FILENAME = "ConvertorAndValidatorTest.java";
    
    /** Constructor required by JUnit */
    public AdvancedBeansBinding(String testName) {
        super(testName);
    }
    
    /* Method allowing to execute test directly from IDE. */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** Creates suite from particular test cases. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new AdvancedBeansBinding("testUpdateMode")); // NOI18N
        suite.addTest(new AdvancedBeansBinding("testAlternateValues")); // NOI18N
        
//        suite.addTest(new AdvancedBeansBinding("testConversion")); // NOI18N
//        suite.addTest(new AdvancedBeansBinding("testValidation")); // NOI18N
        return suite;
    }
    
    /** Tests different update modes */
    public void testUpdateMode() {
        // open frame
        openFile(FILENAME);
        FormDesignerOperator designer = new FormDesignerOperator(FILENAME);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        
        // select update modes for jlabels
        selectUpdateModeForJLabel(inspector, "jLabel2", BindDialogOperator.READ_ONCE_UPDATE_MODE); // NOI18N
        selectUpdateModeForJLabel(inspector, "jLabel4", BindDialogOperator.READ_ONLY_UPDATE_MODE); // NOI18N
        selectUpdateModeForJLabel(inspector, "jLabel6", BindDialogOperator.READ_ONLY_UPDATE_MODE); // NOI18N
        selectUpdateModeForJLabel(inspector, "jLabel6", BindDialogOperator.READ_WRITE_UPDATE_MODE); // NOI18N
        
        // find generated code
        findInCode("setUpdateStrategy(javax.beans.binding.Binding.UpdateStrategy.READ_ONCE)",designer); // NOI18N
        findInCode("setUpdateStrategy(javax.beans.binding.Binding.UpdateStrategy.READ_FROM_SOURCE)", designer); // NOI18N
        
        // test values in bind dialog
        assertTrue(getSelectedUpdateModeForJLabel(inspector, "jLabel2")
                .contains(BindDialogOperator.READ_ONCE_UPDATE_MODE)); // NOI18N

        assertTrue(getSelectedUpdateModeForJLabel(inspector, "jLabel4")
                .contains(BindDialogOperator.READ_ONLY_UPDATE_MODE)); // NOI18N
        
        assertTrue(getSelectedUpdateModeForJLabel(inspector, "jLabel6")
                .contains(BindDialogOperator.READ_WRITE_UPDATE_MODE)); // NOI18N
    }

    /** Tests alternate values */
    public void testAlternateValues() {
        String nullLabelPath =  "[JFrame]|jLabel7 [JLabel]"; // NOI18N
        String incompleteLabelPath = "[JFrame]|jLabel8 [JLabel]"; // NOI18N
        String nullMsg = "null foo msg"; // NOI18N
        String incompleteMsg = "incomplete foo msg";   // NOI18N      
        
        // open frame
        openFile(FILENAME);
        FormDesignerOperator designer = new FormDesignerOperator(FILENAME);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        
        // invoke bind dialog
        Node actNode = new Node(inspector.treeComponents(), nullLabelPath);
        Action act = new ActionNoBlock(null, ACTION_PATH);
        act.perform(actNode);
        
        // set null value text
        BindDialogOperator bindOp = new BindDialogOperator();
        bindOp.selectAdvancedTab();
        bindOp.unselectIncompletePathValue();
        bindOp.selectNullValue();
        waitAMoment();
        bindOp.setNullValueText(nullMsg);
        bindOp.ok();

        // invoke bind dialog
        actNode = new Node(inspector.treeComponents(), incompleteLabelPath);
        act = new ActionNoBlock(null, ACTION_PATH);
        act.perform(actNode);
        
        // set incomplete path value text
        bindOp = new BindDialogOperator();
        bindOp.selectAdvancedTab();
        bindOp.unselectNullValue();
        bindOp.selectIncompletePathValue();
        waitAMoment();
        bindOp.setIncompletePathValueText(incompleteMsg);
        bindOp.ok();
        
        // test generated code
        findInCode("binding.setNullSourceValue(\"" + nullMsg + "\"); // NOI18N", designer); // NOI18N
        findInCode("binding.setValueForIncompleteSourcePath(\"" + incompleteMsg + "\");", designer); // NOI18N
        
        // invoke bind dialog again
        actNode = new Node(inspector.treeComponents(), incompleteLabelPath);
        act = new ActionNoBlock(null, ACTION_PATH);
        act.perform(actNode);
        
        // get incomplete path value from ui
        bindOp = new BindDialogOperator();
        bindOp.selectAdvancedTab();
        String result = bindOp.getIncompletePathValueText();
        bindOp.ok();
        
        // compare values
        assertEquals(result, incompleteMsg);
        
        // invoke bind dialog again
        actNode = new Node(inspector.treeComponents(), nullLabelPath);
        act = new ActionNoBlock(null, ACTION_PATH);
        act.perform(actNode);
        
        // get null value text from ui
        bindOp = new BindDialogOperator();
        bindOp.selectAdvancedTab();
        result = bindOp.getNullValueText();
        bindOp.ok();
        
        // compare values
        assertEquals(result, nullMsg);
    }
    
    /** Tests validation */
    public void testValidation() {
        fail("todo"); // NOI18N
    }

    /** Tests conversion */
    public void testConversion() {
        fail("todo"); // NOI18N
    }
    
    /** Select update mode for  jlabel */
    private void selectUpdateModeForJLabel(ComponentInspectorOperator inspector, String jLabelName, String mode) {
        // invoke bind dialog
        Node actNode = new Node(inspector.treeComponents(), "[JFrame]|" + jLabelName + " [JLabel]"); // NOI18N
        Action act = new ActionNoBlock(null, ACTION_PATH);
        act.perform(actNode);
        
        // select update mode
        BindDialogOperator bindOp = new BindDialogOperator();
        bindOp.selectAdvancedTab();
        bindOp.selectUpdateMode(mode);
        bindOp.ok();
    }

    /* Get selected update mode text caption for jlabel */
    private String getSelectedUpdateModeForJLabel(ComponentInspectorOperator inspector, String jLabelName) {
        // invoke bind dialog
        Node actNode = new Node(inspector.treeComponents(), "[JFrame]|" + jLabelName + " [JLabel]"); // NOI18N
        Action act = new ActionNoBlock(null, ACTION_PATH);
        act.perform(actNode);
        
        // get selected update mode
        BindDialogOperator bindOp = new BindDialogOperator();
        bindOp.selectAdvancedTab();
        String result = bindOp.getSelectedUpdateMode();
        bindOp.cancel();
        return result;
    }
}
