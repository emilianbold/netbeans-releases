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

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.TopComponentOperator;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 * Test of opening files, where CloneableEditor isn't super class of their editor.
 *
 * @author  mmirilovic@netbeans.org
 */
public class OpenFilesNoCloneableEditor extends OpenFiles {

    private String compName;

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
        fileName = "Bundle20kB.properties";
        compName = "Bundle20kB.properties";
        menuItem = OPEN;
        doMeasurement();
    }

    public void testOpening20kBPictureFile(){
        WAIT_AFTER_OPEN = 2000;
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "splash.gif";
        compName = "splash.gif";
        menuItem = OPEN;
        doMeasurement();
    }
        
    @Override
    protected void initialize() {
        EditorOperator.closeDiscardAll();
    }
    
    @Override
    protected void shutdown(){
        EditorOperator.closeDiscardAll();
    }
    
    public ComponentOperator open(){
        JPopupMenuOperator popup =  this.openNode.callPopup();
        if (popup == null) {
            throw new Error ("Cannot get context menu for node [" + openNode.getPath() + "] in project [" + fileProject + "]");
        }
        log("------------------------- after popup invocation ------------");
        
        try {
            popup.pushMenu(this.menuItem);
        }
        catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            tee.printStackTrace(getLog());
            throw new Error ("Cannot push menu item ["+this.menuItem+"] of node [" + openNode.getPath() + "] in project [" + fileProject + "]");
        }
        log("------------------------- after open ------------");
        return new TopComponentOperator(compName);
    }
    
    public void close(){
/*        if (testedComponentOperator != null) {
            ((TopComponentOperator)testedComponentOperator).close();
        }
        else {
            throw new Error ("no component to close");
        }
*/    }
}
