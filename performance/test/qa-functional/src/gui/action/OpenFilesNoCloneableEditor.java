/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.action;

import java.io.File;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 * Test of opening files, where CloneableEditor isn't super class of their editor.
 *
 * @author  mmirilovic@netbeans.org
 */
public class OpenFilesNoCloneableEditor extends OpenFiles {
    
    /**
     * Creates a new instance of OpenFilesNoCloneableEditor
     * @param testName the name of the test
     */
    public OpenFilesNoCloneableEditor(String testName) {
        super(testName);
    }
    
    /**
     * Creates a new instance of OpenFilesNoCloneableEditor
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenFilesNoCloneableEditor(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }
    
    public void testOpening20kBPropertiesFile(){
        WAIT_AFTER_OPEN = 2500;
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "Bundle.properties";
        menuItem = OPEN;
        doMeasurement();
    }

    public void testOpening20kBPictureFile(){
        WAIT_AFTER_OPEN = 2000;
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "splash.gif";
        menuItem = OPEN;
        doMeasurement();
    }
        
    public ComponentOperator open(){
        JPopupMenuOperator popup =  this.openNode.callPopup();
        if (popup == null) {
            fail ("Cannot get context menu for node ["+"Source Packages" + '|' +  filePackage + '|' + fileName + "] in project [" + fileProject + "]");
        }
        log("------------------------- after popup invocation ------------");
        try {
            popup.pushMenu(this.menuItem);
        }
        catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            fail ("Cannot push menu item "+this.menuItem+" of node ["+"Source Packages" + '|' +  filePackage + '|' + fileName + "] in project [" + fileProject + "]");
        }
        log("------------------------- after open ------------");
        return new TopComponentOperator(fileName);
    }
    
    public void close(){
        if (testedComponentOperator != null) {
            ((TopComponentOperator)testedComponentOperator).close();
        }
        else {
            fail ("no component to close");
        }
    }
    
}
