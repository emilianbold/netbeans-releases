/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.action;

import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 * Test of opening files.
 *
 * @author  mmirilovic@netbeans.org
 */
public class OpenFormFile extends OpenFilesNoCloneableEditor {
    
    /**
     * Creates a new instance of OpenFiles
     * @param testName the name of the test
     */
    public OpenFormFile(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /**
     * Creates a new instance of OpenFiles
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenFormFile(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    
    public void testOpening20kBFormFile(){
        WAIT_AFTER_OPEN = 15000;
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "JFrame20kB.java";
        menuItem = OPEN;
        doMeasurement();
    }
    
    public ComponentOperator open(){
        JPopupMenuOperator popup =  this.openNode.callPopup();
        if (popup == null) {
            new java.lang.Error("Cannot get context menu for node [" + gui.Utilities.SOURCE_PACKAGES + '|' +  filePackage + '|' + fileName + "] in project [" + fileProject + "]");
        }
        log("------------------------- after popup invocation ------------");
        try {
            popup.pushMenu(this.menuItem);
        }
        catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            throw new Error ("Cannot push menu item "+this.menuItem+" of node [" + gui.Utilities.SOURCE_PACKAGES + '|' +  filePackage + '|' + fileName + "] in project [" + fileProject + "]");
        }
        log("------------------------- after open ------------");
        return new FormDesignerOperator("JFrame20kB");
    }

    public void close() {
        ((FormDesignerOperator)testedComponentOperator).close();
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new OpenFormFile("testOpening20kBFormFile"));
    }
    
}
