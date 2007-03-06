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

import java.awt.Component;
import org.netbeans.qa.form.*;
import org.netbeans.qa.form.visualDevelopment.*;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.*;
import org.netbeans.qa.form.ExtJellyTestCase;
import java.util.*;
import org.netbeans.jellytools.modules.form.ComponentPaletteOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 * Tests adding and removing beans into/from palette
 *
 * @author Jiri Vagner
 */
public class AddAndRemoveBeansTest  extends ExtJellyTestCase {
    public static String VISUAL_BEAN_NAME = "TestVisualBean"; // NOI18N
    public static String NONVISUAL_BEAN_NAME = "TestNonVisualBean"; // NOI18N
    public static String TESTED_BEAN_TEXT = "Lancia Lybra"; // NOI18N
    public static String TESTED_BEAN_POWER = "140"; // NOI18N

    
    /**
     * Constructor required by JUnit
     */
    public AddAndRemoveBeansTest(String testName) {
        super(testName);
    }
    
    /**
     * Method allowing to execute test directly from IDE.
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /**
     * Creates suite from particular test cases.
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        
        suite.addTest(new AddAndRemoveBeansTest("testAddingBeans")); // NOI18N
        suite.addTest(new AddAndRemoveBeansTest("testRemovingBeans")); // NOI18N
        
        return suite;
    }
    
    /**
     *  Tests "Add Bean" dialog
     */
    public void testAddingBeans() {
        addBean( VISUAL_BEAN_NAME + ".java"); // NOI18N
        addBean( NONVISUAL_BEAN_NAME + ".java"); // NOI18N
    }
    
    /**
     * Tests removing bean using Palette Manager
     */
    public void testRemovingBeans() {
        new ActionNoBlock("Tools|Palette Manager|Swing/AWT Components", null).perform(); // NOI18N
        
        PaletteManagerOperator manOp = new PaletteManagerOperator();
        JTreeOperator treeOp = manOp.treePaletteContentsTree();
        
        treeOp.clickOnPath( treeOp.findPath("Beans|" + VISUAL_BEAN_NAME,"|")); // NOI18N
        manOp.remove();
        new NbDialogOperator("Confirm").yes(); // NOI18N
        
        treeOp.clickOnPath( treeOp.findPath("Beans|" + NONVISUAL_BEAN_NAME,"|")); // NOI18N
        manOp.remove();
        new NbDialogOperator("Confirm").yes(); // NOI18N
        
        manOp.close();
    }
    
    /**
     * Tests removing beans using popup menu from palette
     */
    public void testRemovingBeansFromPalette() {
        openFile("clear_Frame.java");
        
        ComponentPaletteOperator palette = new ComponentPaletteOperator();
        palette.expandBeans();
        palette.collapseSwingContainers();
        palette.collapseSwingMenus();
        palette.collapseSwingWindows();
        palette.collapseAWT();
        palette.collapseSwingControls();
        
        JListOperator list = palette.lstComponents();
        list.clickOnItem(NONVISUAL_BEAN_NAME, new Operator.DefaultStringComparator(true, false));
        
        // TODO: I'm not able to invoke popup menu :(
        int i = list.findItemIndex(NONVISUAL_BEAN_NAME, new Operator.DefaultStringComparator(true, false));
        p(i);
        
        Component[] comps = list.getComponents();
        p(comps.length);
        for (Component comp : comps) {
            p(comp.toString());
        }
    }
    
}
