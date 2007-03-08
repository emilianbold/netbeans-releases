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

package gui.action;

import gui.window.PaletteComponentOperator;
import gui.window.WebFormDesignerOperator;

import org.netbeans.jellytools.actions.Action;

import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class ComponentAddTest extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    private PaletteComponentOperator palette;
    private WebFormDesignerOperator surface;
    private String categoryName;
    private String componentName;
    
    /**
     * Creates a new instance of ComponentAddTest
     */
    public ComponentAddTest(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    public ComponentAddTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new ComponentAddTest("testAddTableComponent","Adding Table Component"));
        suite.addTest(new ComponentAddTest("testAddButtonComponent","Adding Button Component"));
        suite.addTest(new ComponentAddTest("testAddListboxComponent","Adding Listbox Component"));
        return suite;
    }
    
    public void testAddTableComponent() {
        categoryName = "Basic"; // NOI18N
        componentName = "Table"; // NOI18N
        doMeasurement();
    }
    
    public void testAddButtonComponent() {
        categoryName = "Basic"; // NOI18N
        componentName = "Button"; // NOI18N
        doMeasurement();
    }
    
    public void testAddListboxComponent() {
        categoryName = "Basic"; // NOI18N
        componentName = "Listbox"; // NOI18N
        doMeasurement();
    }
    
    public void initialize() {
        log("::initialize");
        PaletteComponentOperator.invoke();
    }
    
    public void prepare() {
        log("::prepare");
        surface = gui.VWPUtilities.openedWebDesignerForJspFile("VisualWebProject", "Page1");
        
        palette = new PaletteComponentOperator();
        palette.getCategoryListOperator(categoryName).selectItem(componentName);
    }
    
    public ComponentOperator open() {
        log("::open");
        surface.clickOnSurface(20,20);
        return null;
    }
    
    public void close() {
        log("::close");
        surface.closeDiscard();

        //new SaveAllAction().performAPI(); // Save
        //new org.netbeans.jellytools.actions.CloseAllDocumentsAction().performAPI(); // Close;
        log(":: close passed");
    }
    
    protected void shutdown() {
        log("::shutdown");
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
}
