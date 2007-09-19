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
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.qa.form.BindDialogOperator;

/**
 * Beans Binding advanced tests
 *
 * @author Jiri Vagner
 */
public class AdvancedBeansBinding extends ExtJellyTestCase {
    private String ACTION_PATH = "Bind|text";  // NOI18N
    private String BIND_EXPRESSION = "${text}";  // NOI18N
    private String FILENAME = "ConvertorAndValidatorTest.java"; // NOI18N
    private String VALIDATOR_NAME = "loginLengthValidator";  // NOI18N
    private String CONVERTOR_NAME = "bool2FaceConverter";  // NOI18N    
    
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
        suite.addTest(new AdvancedBeansBinding("testCompileComponents")); // NOI18N
        suite.addTest(new AdvancedBeansBinding("testUpdateMode")); // NOI18N
        suite.addTest(new AdvancedBeansBinding("testAlternateValues")); // NOI18N
        suite.addTest(new AdvancedBeansBinding("testConversion")); // NOI18N
        suite.addTest(new AdvancedBeansBinding("testValidation")); // NOI18N
        return suite;
    }

    public void testCompileComponents() throws Exception {
        super.setUp();

        Node beanNode = openFile(CONVERTOR_NAME);
        CompileAction action = new CompileAction();
        action.perform(beanNode);
        
        beanNode = openFile(VALIDATOR_NAME);
        action = new CompileAction();
        action.perform(beanNode);
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
        //findInCode("setUpdateStrategy(javax.beans.binding.Binding.UpdateStrategy.READ_ONCE)",designer); // NOI18N
        //findInCode("setUpdateStrategy(javax.beans.binding.Binding.UpdateStrategy.READ_FROM_SOURCE)", designer); // NOI18N
        
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
        
        JDialogOperator bindOp = new JDialogOperator("Bind");  // NOI18N
        JTabbedPaneOperator tabOp = new JTabbedPaneOperator(bindOp);
        tabOp.selectPage("Advanced");

        // null checkbox
        JCheckBoxOperator checkBoxOp = new JCheckBoxOperator(tabOp, 0);
        checkBoxOp.changeSelection(true);
        
        // incomplet path checkbox
        checkBoxOp = new JCheckBoxOperator(tabOp, 1);
        checkBoxOp.changeSelection(true);
        
        // incomplete value settings
        new JButtonOperator(tabOp,6).pushNoBlock();
        NbDialogOperator valueOp = new NbDialogOperator("Incomplete Path Value");  // NOI18N
        new JTextAreaOperator(valueOp,0).setText(incompleteMsg);
        new JButtonOperator(valueOp, "OK").push();  // NOI18N
        
        // null value settings
        new JButtonOperator(tabOp,7).pushNoBlock();
        valueOp = new NbDialogOperator("Null Value");  // NOI18N
        new JTextAreaOperator(valueOp,0).setText(nullMsg);
        new JButtonOperator(valueOp, "OK").push();  // NOI18N

        // closing bind dialog
        new JButtonOperator(bindOp,"OK").push();  // NOI18N

        // test generated code
        findInCode("binding.setSourceNullValue(\"" + nullMsg + "\");", designer); // NOI18N
        findInCode("binding.setSourceUnreadableValue(\"" + incompleteMsg + "\");", designer); // NOI18N
       
        // invoke bind dialog again and check values
        actNode = new Node(inspector.treeComponents(), nullLabelPath);
        act = new ActionNoBlock(null, ACTION_PATH);
        act.perform(actNode);
        
        bindOp = new JDialogOperator("Bind");  // NOI18N
        tabOp = new JTabbedPaneOperator(bindOp);
        tabOp.selectPage("Advanced");  // NOI18N

        // get incomplete path value
        new JButtonOperator(tabOp,6).pushNoBlock();
        valueOp = new NbDialogOperator("Incomplete Path Value");  // NOI18N
        String incomleteValue =  new JTextAreaOperator(valueOp,0).getText();
        new JButtonOperator(valueOp, "OK").push();  // NOI18N
        
        // get null value
        new JButtonOperator(tabOp,7).pushNoBlock();
        valueOp = new NbDialogOperator("Null Value");  // NOI18N
        String nullValue =  new JTextAreaOperator(valueOp,0).getText();
        new JButtonOperator(valueOp, "OK").push();  // NOI18N

        // closing bind dialog
        new JButtonOperator(bindOp,"OK").push();  // NOI18N

        // compare values
        assertEquals(incomleteValue, incompleteMsg);
        assertEquals(nullValue, nullMsg);
    }
    
    /** Tests validation */
    public void testValidation() {

        // open frame
        openFile(FILENAME);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        Node actNode = new Node(inspector.treeComponents(), "[JFrame]|jLabel12 [JLabel]"); // NOI18N
        Action act = new ActionNoBlock(null, ACTION_PATH);
        act.perform(actNode);
        
        // set Face2Bool converter from list
        BindDialogOperator bindOp = new BindDialogOperator();
        bindOp.selectAdvancedTab();
        bindOp.selectValidator(VALIDATOR_NAME);
        bindOp.ok();
        
        // find code in source file
        FormDesignerOperator designer = new FormDesignerOperator(FILENAME);
        findInCode("binding.setValidator(" + VALIDATOR_NAME + ");", designer);  // NOI18N
        
        // open bind dialog again and check selected
        act.perform(actNode);
        bindOp = new BindDialogOperator();
        bindOp.selectAdvancedTab();
        String selected = bindOp.getValidator();
        bindOp.ok();

        // test name
        assertEquals(selected, VALIDATOR_NAME);
    }

    /** Tests conversion */
    public void testConversion() {
        
        // open frame
        openFile(FILENAME);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        Node actNode = new Node(inspector.treeComponents(), "[JFrame]|jLabel9 [JLabel]"); // NOI18N
        Action act = new ActionNoBlock(null, ACTION_PATH);
        act.perform(actNode);
        
        // set Face2Bool converter from list
        BindDialogOperator bindOp = new BindDialogOperator();
        bindOp.selectAdvancedTab();
        bindOp.selectConverter(CONVERTOR_NAME);
        bindOp.ok();
        
        // find code in source file
        FormDesignerOperator designer = new FormDesignerOperator(FILENAME);
        findInCode("binding.setConverter("+CONVERTOR_NAME+");", designer);  // NOI18N

        
        // open bind dialog again and check selected convertor
        act.perform(actNode);
        bindOp = new BindDialogOperator();
        bindOp.selectAdvancedTab();
        String selectedConvertor = bindOp.getSelectedConverter();
        bindOp.ok();

        // test convertor name
        assertEquals(selectedConvertor, CONVERTOR_NAME);
        
        
//        actNode = new Node(inspector.treeComponents(), "[JFrame]|jLabel10 [JLabel]"); // NOI18N
//        act = new ActionNoBlock(null, ACTION_PATH);
//        act.perform(actNode);
        
// It does not work because of issue #115239
//        // set Face2Bool converter using "..." button
//        bindOp = new BindDialogOperator();
//        bindOp.selectAdvancedTab();
//        
//        new JButtonOperator(bindOp.tbdPane(), 2).pushNoBlock();
//        JDialogOperator dialog = new JDialogOperator("Converter");
//        new JComboBoxOperator(dialog, 0).selectItem(2);
//        
//        JTextFieldOperator textOp = new JTextFieldOperator(dialog, 0);
//        textOp.clearText();
//        textOp.setText("new Bool2FaceConverter()");
//        
//        new JButtonOperator(dialog,"OK").push();
//
//        bindOp = new BindDialogOperator();
//        bindOp.ok();
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
