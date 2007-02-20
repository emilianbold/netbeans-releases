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
 * Componentes declaration test
 *
 * @author Jiri Vagner
 */
public class GeneratedComponentsDestionationTest extends ExtJellyTestCase {
    
    /** Constructor required by JUnit */
    public GeneratedComponentsDestionationTest(String testName) {
        super(testName);
    }
    
    /** Method allowing to execute test directly from IDE. */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** Creates suite from particular test cases. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        
        suite.addTest(new GeneratedComponentsDestionationTest("testGeneratedComponentsDestionationLocal")); // NOI18N
        suite.addTest(new GeneratedComponentsDestionationTest("testGeneratedComponentsDestionationClassField")); // NOI18N
        
        return suite;
    }
    
    /** Tests generation component declaration code with properties LocalVariables=true
     * Test for issue 95518
     */
    public void testGeneratedComponentsDestionationLocal() {
        testGeneratedComponentsDestionation(true);
    }

    /**
     * Tests generation component declaration code with properties LocalVariables=false
     */
    public void testGeneratedComponentsDestionationClassField() {
        testGeneratedComponentsDestionation(false);
    }

    /**
     * Tests generation component declaration code with properties LocalVariables=false
     * 
     * @param local "Local Variables" settings 
     */
    private void testGeneratedComponentsDestionation(Boolean local) {
        OptionsOperator.invoke();
        OptionsOperator options = new OptionsOperator();
        options.switchToClassicView();
        waitAMoment();
        
        options.selectOption("Editing|GUI Builder"); // NOI18N
        waitAMoment();        

        Property property = new Property(options.getPropertySheet("Editing|GUI Builder"), "Variables Modifier"); // NOI18N
        property.setValue("private"); // NOI18N
        
        property = new Property(options.getPropertySheet("Editing|GUI Builder"), "Local Variables"); // NOI18N
        property.setValue(String.valueOf(local));
        options.close();
        waitAMoment();        
        
        String name = createJFrameFile();
        waitAMoment();        
        
        FormDesignerOperator designer = new FormDesignerOperator(name);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        Node node = new Node(inspector.treeComponents(), "JFrame"); // NOI18N
        
        runPopupOverNode("Add From Palette|Swing Controls|Label", node); // NOI18N
        waitAMoment();        
        
        String code = "private javax.swing.JLabel jLabel1";  // NOI18N
        if (local)
            missInCode(code, designer);
        else
            findInCode(code, designer);
        
        waitAMoment();        
        removeFile(name);
    }
}