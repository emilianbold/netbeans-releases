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

package org.netbeans.qa.form.options;

import org.netbeans.qa.form.*;
import org.netbeans.qa.form.visualDevelopment.*;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.qa.form.ExtJellyTestCase;
import java.util.*;

/**
 * Automatic insternationalization test
 *
 * @author Jiri Vagner
 */
public class AutomaticInternationalizationTest extends ExtJellyTestCase {
    
    /** Constructor required by JUnit */
    public AutomaticInternationalizationTest(String testName) {
        super(testName);
    }
    
    /** Method allowing to execute test directly from IDE. */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** Creates suite from particular test cases. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        
        suite.addTest(new AutomaticInternationalizationTest("testAutomaticInternationalizationEnabled")); // NOI18N
        suite.addTest(new AutomaticInternationalizationTest("testAutomaticInternationalizationDisabled")); // NOI18N
        
        return suite;
    }
    
    /**
     *  Tests component code with properties Automatic Internationalization = true
     */
    public void testAutomaticInternationalizationEnabled() {
        testAutomaticInternationalization(true);
    }

    /**
     *  Tests component code with properties Automatic Internationalization = false
     */
    public void testAutomaticInternationalizationDisabled() {
        testAutomaticInternationalization(false);
    }

    /**
     * Tests component code with different value of properties Automatic Internationalization
     * 
     * @param local "Automatic Internationalization" settings 
     */
    private void testAutomaticInternationalization(Boolean enabled) {
        OptionsOperator.invoke();
        OptionsOperator options = new OptionsOperator();
        options.switchToClassicView();
        
        options.selectOption("Editing|GUI Builder"); // NOI18N

        Property property = new Property(options.getPropertySheet("Editing|GUI Builder"), "Automatic Internationalization"); // NOI18N
        property.setValue(String.valueOf( enabled ? "On" : "Off"));
        options.close();
        waitAMoment();        

        String name = createJFrameFile();        
        FormDesignerOperator designer = new FormDesignerOperator(name);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        Node node = new Node(inspector.treeComponents(), "JFrame"); // NOI18N
        
        runPopupOverNode("Add From Palette|Swing Controls|Button", node); // NOI18N
        
        String baseName = "[JFrame]"; // NOI18N
        Node dialogNode = new Node(inspector.treeComponents(), baseName);
        String[] names = dialogNode.getChildren();
        
        inspector.selectComponent("[JFrame]|jButton1");
            
        Property prop = new Property(inspector.properties(), "text"); // NOI18N
        prop.setValue("Lancia Lybra");
        
        if (enabled)
            findInCode("jButton1.setText(bundle.getString(\"MyJFrame", designer);
        else
            findInCode("jButton1.setText(\"Lancia Lybra\");", designer);
        
        removeFile(name);
    }
}