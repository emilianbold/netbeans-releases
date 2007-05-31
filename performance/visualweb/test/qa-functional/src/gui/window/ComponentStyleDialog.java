/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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


package gui.window;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;

import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class ComponentStyleDialog extends JSFComponentOptionsDialog {
    
    private PropertySheetOperator pto;
    private Property property;
    private NbDialogOperator styleDialog;
    private String componentID;
    
    /**
     * @param testName
     */
    public ComponentStyleDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=3000;
        categoryName = "Basic"; // NOI18N
        componentName = "Table"; // NOI18N
        addPoint = new java.awt.Point(50,50);
    }
    
    /**
     * @param testName
     * @param performanceDataName
     */
    public ComponentStyleDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=3000;
        categoryName = "Basic"; // NOI18N
        componentName = "Button"; // NOI18N
        addPoint = new java.awt.Point(50,50);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new ComponentStyleDialog("testButtonStyleDialog","Button Style Dialog Open test"));
        suite.addTest(new ComponentStyleDialog("testTableStyleDialog","Table Style Dialog Open test"));
        suite.addTest(new ComponentStyleDialog("testLisbBoxStyleDialog","Listbox Style Dialog Open test"));
        return suite;
    }
    
    public void testButtonStyleDialog() {
        categoryName = "Basic"; // NOI18N
        componentName = "Button"; // NOI18N
        doMeasurement();
    }
    
    public void testTableStyleDialog() {
        categoryName = "Basic"; // NOI18N
        componentName = "Table"; // NOI18N
        doMeasurement();
    }
    
    public void testLisbBoxStyleDialog() {
        categoryName = "Basic"; // NOI18N
        componentName = "Listbox"; // NOI18N
        doMeasurement();
    }
    
    public void initialize() {
        super.initialize();
        pto =  new PropertySheetOperator("Page1").invoke();
        surface.clickOnSurface(addPoint.x+5, addPoint.y+5);
        componentID = new Property(pto,"id").getValue();
        property = new Property(pto,"style"); // NOI18N
    }
    
    public ComponentOperator open() {
        log(":: open");
        property.openEditor();
        styleDialog = new NbDialogOperator(componentID);
        return null;
    }
    
    public void close() {
        log(":: close");
        styleDialog.close();
        super.close();
    }
    
    protected void shutdown() {
        super.shutdown();
        pto.close();
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
