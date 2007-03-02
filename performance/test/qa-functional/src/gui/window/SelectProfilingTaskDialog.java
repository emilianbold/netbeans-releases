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

package gui.window;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Open Select Profiling Task dialog.
 *
 * @author  mmirilovic@netbeans.org
 */
public class SelectProfilingTaskDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    protected static String PROFILE_MAIN_PROJECT = Bundle.getStringTrimmed("org.netbeans.modules.profiler.actions.Bundle","LBL_ProfileProjectActionPopup"); //Profile Project
    
    protected static String DIALOG_TITLE = Bundle.getStringTrimmed("org.netbeans.modules.profiler.actions.Bundle","ProfilingSupport_ProfileClassDialogCaption"); //Select Profiling Task
    
    private Node projectNode;
    
    /**
     * Creates a new instance of CreateTestsDialog
     * 
     * @param testName the name of the test
     */
    public SelectProfilingTaskDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /**
     * Creates a new instance of CreateTestsDialog
     * 
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public SelectProfilingTaskDialog(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void prepare() {
        this.projectNode = new ProjectsTabOperator().getProjectRootNode("PerformanceTestData");
        
        if (this.projectNode == null) {
            throw new Error ("Cannot find project [PerformanceTestData]");
        }
    }
    
    public ComponentOperator open(){
        // invoke Tools|Create JUnit Tests from the popup menu
        projectNode.performPopupActionNoBlock(PROFILE_MAIN_PROJECT);
        return new NbDialogOperator(DIALOG_TITLE);
    }

    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new SelectProfilingTaskDialog("measureTime"));
    }
    
}
