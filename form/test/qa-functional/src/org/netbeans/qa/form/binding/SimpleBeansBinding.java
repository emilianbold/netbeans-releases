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
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jellytools.properties.Property;        
import org.netbeans.jemmy.operators.JButtonOperator;

/**
 * Beans Binding basic test
 *
 * @author Jiri Vagner
 */
public class SimpleBeansBinding extends ExtJellyTestCase {
    
    /** Constructor required by JUnit */
    public SimpleBeansBinding(String testName) {
        super(testName);
    }
    
    /* Method allowing to execute test directly from IDE. */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** Creates suite from particular test cases. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new SimpleBeansBinding("testSimpleBeansBinding")); // NOI18N
        return suite;
    }
    
    /** Tests basic beans binding features */
    public void testSimpleBeansBinding() {
        String jLabel1NodePath = "[JFrame]|jLabel1 [JLabel]";  // NOI18N
        String jLabel2NodePath = "[JFrame]|jLabel2 [JLabel]";  // NOI18N
        String actionPath = "Bind|text";  // NOI18N
        String bindSource = "jLabel2";  // NOI18N
        String bindExpression = "${text}";  // NOI18N
        
        // create frame
        String frameName = createJFrameFile();
        FormDesignerOperator designer = new FormDesignerOperator(frameName);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        
        // add two labels
        Node actNode = new Node(inspector.treeComponents(), "JFrame"); // NOI18N
        String itemPath = "Add From Palette|Swing Controls|Label"; // NOI18N
        runPopupOverNode(itemPath, actNode);
        runPopupOverNode(itemPath, actNode);

        // invoke bind dialog
        actNode = new Node(inspector.treeComponents(), jLabel1NodePath);
        Action act = new ActionNoBlock(null, actionPath);
        act.perform(actNode);
        
        // bind jlabel1.text with jlabel2.text
        BindDialogOperator bindOp = new BindDialogOperator();
        bindOp.selectBindSource(bindSource);
        bindOp.setBindExpression(bindExpression);
        bindOp.ok();
        
        // invoke bind dialog again ...
        actNode = new Node(inspector.treeComponents(), jLabel1NodePath);
        act = new ActionNoBlock(null, actionPath);
        act.perform(actNode);
        
        // ... and check the values in binding dialog
        bindOp = new BindDialogOperator();
        assertEquals(bindOp.getSelectedBindSource(), bindSource);
        assertEquals(bindOp.getBindExpression(), bindExpression);
        bindOp.ok();

        // check generated binding code
        findInCode("bindingContext.addBinding(jLabel2, \"${text}\", jLabel1, \"text\");", designer);  // NOI18N
        findInCode("bindingContext.bind();", designer);  // NOI18N

        // get values of text properties of jLabels and test them
        assertEquals(getTextValueOfLabel(inspector, jLabel1NodePath),
                getTextValueOfLabel(inspector, jLabel2NodePath));
    }
    
    /** Gets text value of jlabel component
     * @return String text 
     */
    private String getTextValueOfLabel(ComponentInspectorOperator inspector, String nodePath) {
        // invoke properties of component ...
        Node actNode = new Node(inspector.treeComponents(), nodePath);
        ActionNoBlock act = new ActionNoBlock(null, "Properties");  // NOI18N
        act.perform(actNode);

        // get value of property
        NbDialogOperator dialogOp = new NbDialogOperator("[JLabel]");  // NOI18N
        Property prop = new Property(new PropertySheetOperator(dialogOp), "text");  // NOI18N
        String result = prop.getValue();
        
        // close property dialog
        new JButtonOperator(dialogOp,"Close").push();  // NOI18N
        waitAMoment();

        return result;
    }

}
