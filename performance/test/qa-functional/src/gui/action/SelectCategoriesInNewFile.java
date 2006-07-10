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

package gui.action;

import org.netbeans.jellytools.NewFileWizardOperator;

import org.netbeans.jemmy.operators.ComponentOperator;


/**
 * Test of expanding nodes in the New File Wizard tree.
 *
 * @author  mmirilovic@netbeans.org
 */
public class SelectCategoriesInNewFile extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    /** Category name */
    private static String category;

    /** Jelly Operator for New Wizard */
    private static NewFileWizardOperator newFile;

    /**
     * Creates a new instance of SelectCategoriesInNewFile
     * @param testName the name of the test
     */
    public SelectCategoriesInNewFile(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /**
     * Creates a new instance of SelectCategoriesInNewFile
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public SelectCategoriesInNewFile(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    
    public void testSelectGUIForms(){
        testCategory("org.netbeans.modules.form.resources.Bundle", "Templates/GUIForms");
    }
    
    public void testSelectXML(){
        testCategory("org.netbeans.api.xml.resources.Bundle", "Templates/XML");
    }
    
    public void testSelectOther(){
        testCategory("org.netbeans.modules.favorites.Bundle", "Templates/Other");
    }
    
    
    protected void testCategory(String bundle, String key) {
        category = org.netbeans.jellytools.Bundle.getStringTrimmed(bundle,key);
        doMeasurement();
    }
   
    protected void initialize(){
    }
    
    public void prepare(){
        newFile = NewFileWizardOperator.invoke();
    }
    
    public ComponentOperator open(){
        newFile.selectCategory(category);
        return null;
    }
    
    public void close(){
        newFile.cancel();        
    }
    
}
