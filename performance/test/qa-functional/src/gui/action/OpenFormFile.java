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
import org.netbeans.jellytools.modules.form.FormDesignerOperator;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

import org.netbeans.performance.test.guitracker.LoggingRepaintManager.RegionFilter;

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
    
    @Override
    protected void initialize() {
        EditorOperator.closeDiscardAll();
        // don't measure paint events from StatusLine
        repaintManager().addRegionFilter(STATUSLINE_FILTER);
    }

    @Override
    protected void shutdown() {
        EditorOperator.closeDiscardAll();
        // reset filter
        repaintManager().resetRegionFilters();
    }
    
    public ComponentOperator open(){
        JPopupMenuOperator popup =  this.openNode.callPopup();
        if (popup == null) {
            new java.lang.Error("Cannot get context menu for node [" + openNode.getPath() + "]");
        }
        log("------------------------- after popup invocation ------------");
        try {
            popup.pushMenu(this.menuItem);
        }
        catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            throw new Error ("Cannot push menu item "+this.menuItem+" of node [" + openNode.getPath() + "]");
        }
        log("------------------------- after open ------------");
        return new FormDesignerOperator("JFrame20kB");
    }

    public void close() {
//        ((FormDesignerOperator)testedComponentOperator).close();
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new OpenFormFile("testOpening20kBFormFile"));
    }
    
    private static final RegionFilter STATUSLINE_FILTER =
            new RegionFilter() {

                public boolean accept(javax.swing.JComponent c) {
                    return !c.getClass().getName().equals("org.netbeans.core.windows.view.ui.StatusLine");
                }

                public String getFilterName() {
                    return "Don't accept paints from org.netbeans.core.windows.view.ui.StatusLine";
                }
            };
    
}
