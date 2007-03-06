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
package org.netbeans.jellytools.modules.form;

import java.awt.Component;
import javax.swing.JButton;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.AttachWindowAction;
import org.netbeans.jellytools.nodes.FormNode;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/** Test FormDesignerOperator, ComponentPaletteOperator 
 * and ComponentInspectorOperator.
 */
public class FormEditorOperatorTest extends JellyTestCase {
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new FormEditorOperatorTest("testOpen"));
        suite.addTest(new FormEditorOperatorTest("testSourceButton"));
        suite.addTest(new FormEditorOperatorTest("testEditor"));
        suite.addTest(new FormEditorOperatorTest("testDesignButton"));
        suite.addTest(new FormEditorOperatorTest("testDesign"));
        suite.addTest(new FormEditorOperatorTest("testProperties"));
        suite.addTest(new FormEditorOperatorTest("testPreviewForm"));
        suite.addTest(new FormEditorOperatorTest("testClose"));
        return(suite);
    }
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public FormEditorOperatorTest(String testName) {
        super(testName);
    }

    private static final String SAMPLE_FRAME = "JFrameSample.java";
    
    /** Print out test name. */
    public void setUp() {
        System.out.println("### "+getName()+" ###");
    }
    
    /** Opens sample JFrame. */
    public void testOpen() throws Exception {
        FormNode node = new FormNode(new SourcePackagesNode("SampleProject"),
                                    "sample1|"+SAMPLE_FRAME); // NOI18N
        node.open();
    }
    
    /** Test source toggle button. */
    public void testSourceButton() {
        new FormDesignerOperator(SAMPLE_FRAME).source();
    }
    
    /** Test editor method. */
    public void testEditor() {
        new FormDesignerOperator(SAMPLE_FRAME).editor();
    }
    
    /** Test Design toggle button. */
    public void testDesignButton() {
        new FormDesignerOperator(SAMPLE_FRAME).design();
    }
    
    /** Test design actions. */
    public void testDesign() {
        FormDesignerOperator designer = new FormDesignerOperator(SAMPLE_FRAME);
        String windowItem = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Window");
        String paletteItem = Bundle.getStringTrimmed("org.netbeans.modules.palette.Bundle", "CTL_PaletteAction");
        new Action(windowItem+"|"+paletteItem, null).perform();
        ComponentPaletteOperator palette = new ComponentPaletteOperator();
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        // attach Palette to better position because components are not visible
        // when screen resolution is too low
        palette.attachTo(new OutputOperator(), AttachWindowAction.RIGHT);
        //add something there
        palette.expandSwingControls();
        palette.selectComponent("Label"); // NOI18N
        designer.clickOnComponent(designer.fakePane().getSource());
        palette.selectComponent("Button"); // NOI18N
        designer.clickOnComponent(designer.fakePane().getSource());
        palette.selectComponent("Text Field"); // NOI18N
        designer.clickOnComponent(designer.fakePane().getSource());
        // add second button next to the first one
        Component button1 = designer.findComponent(JButton.class);
        palette.selectComponent("Button"); // NOI18N
        designer.clickOnComponent(button1);
    }
    
    /** Test setting properties of components. */
    public void testProperties() {
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        inspector.selectComponent("JFrame|jButton2"); // NOI18N
        PropertySheetOperator pso = inspector.properties();
        new Property(pso, "text").setValue("Add"); // NOI18N
        inspector.selectComponent("JFrame|jLabel1"); // NOI18N
        new Property(pso, "text").setValue("Text to be added:"); // NOI18N
        inspector.selectComponent("JFrame|jTextField1"); // NOI18N
        new Property(pso, "text").setValue("             "); // NOI18N
        inspector.selectComponent("JFrame|jButton1"); // NOI18N
        new Property(pso, "text").setValue("Close"); // NOI18N
    }
    
    /** Test preview form mode of form designer. */
    public void testPreviewForm() {
        FormDesignerOperator designer = new FormDesignerOperator(SAMPLE_FRAME);
        JFrameOperator myFrame = designer.previewForm(SAMPLE_FRAME.substring(0, SAMPLE_FRAME.indexOf('.')));
        myFrame.resize(400, 400);
        myFrame.close();
    }
    
    /** Closes java source together with form editor. */
    public void testClose() {
        new FormDesignerOperator(SAMPLE_FRAME).closeDiscard();
    }
}
