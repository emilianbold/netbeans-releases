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

package org.netbeans.qa.form.beans;

import org.netbeans.jellytools.actions.CompileAction;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.qa.form.*;

/**
 * Tests creating Bean Forms from visual and non-visual JavaBeans superclasses
 * and tests value and access rights of inherited properties
 *
 * @author Jiri Vagner
 */
public class AddBeanForms extends AddAndRemoveBeansTest {
    
    /**
     * Constructor required by JUnit
     * @param testName
     */
    public AddBeanForms(String testName) {
        super(testName);
        //this.DELETE_FILES = false;
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
        
        suite.addTest(new AddBeanForms("testCompileBeanClasses")); // NOI18N
        suite.addTest(new AddBeanForms("testAddingBeanFormWithVisualBeanSuperclass")); // NOI18N
        suite.addTest(new AddBeanForms("testAddingBeanFormWithNonVisualBeanSuperclass")); // NOI18N        
        
        return suite;
    }

    /** Compiling beans components */
    public void testCompileBeanClasses() {
        Node beanNode = openFile(VISUAL_BEAN_NAME);
        CompileAction action = new CompileAction();
        action.perform(beanNode);

        beanNode = openFile(NONVISUAL_BEAN_NAME);
        action = new CompileAction();
        action.perform(beanNode);
   }
    
    /** Test adding Bean Form with visual bean superclass */
    public void testAddingBeanFormWithVisualBeanSuperclass() {
        String name = createBeanFormFile(VISUAL_BEAN_NAME);

        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        inspector.selectComponent(VISUAL_BEAN_NAME);
        Property prop = new Property(inspector.properties(), "text"); // NOI18N
        assertEquals("Text property of component " + name + " was not set correctly.",
            prop.getValue(), TESTED_BEAN_TEXT); // NOI18N

        removeFile(name);
    }

    /** Test adding Bean Form with non-visual bean superclass */
    public void testAddingBeanFormWithNonVisualBeanSuperclass() {
        String name = createBeanFormFile(NONVISUAL_BEAN_NAME);


        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        inspector.selectComponent(NONVISUAL_BEAN_NAME);
        
        Property prop = new Property(inspector.properties(), "power"); // NOI18N
        assertEquals("Text property of component " + name + " was not set correctly.",
            prop.getValue(), this.TESTED_BEAN_POWER); // NOI18N
        assertEquals("Property of component " + name + " is read-only.",
            prop.isEnabled(), true); // NOI18N

        prop = new Property(inspector.properties(), "carName"); // NOI18N
        assertEquals("Text property of component " + name + " was not set correctly.",
            prop.getValue(), TESTED_BEAN_TEXT); // NOI18N
        assertEquals("Property of component " + name + " is not read-only.",
            prop.isEnabled(), false); // NOI18N

        removeFile(name);
    }
}
