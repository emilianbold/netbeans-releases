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

package gui.window;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 * Open Create Tests dialog.
 *
 * @author  mmirilovic@netbeans.org
 */
public class CreateTestsDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    protected static String CREATE_JUNIT_TESTS = "Tools|Create JUnit Tests"; //NOI18N
    
    private Node createTestsNode;
    
    /**
     * Creates a new instance of CreateTestsDialog
     * 
     * @param testName the name of the test
     */
    public CreateTestsDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /**
     * Creates a new instance of CreateTestsDialog
     * 
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateTestsDialog(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void prepare() {
        this.createTestsNode = new Node(new ProjectsTabOperator().getProjectRootNode("PerformanceTestData"), gui.Utilities.SOURCE_PACKAGES + "|org.netbeans.test.performance|Main20kB.java");
        
        if (this.createTestsNode == null) {
            throw new Error ("Cannot find node [" + gui.Utilities.SOURCE_PACKAGES + "|org.netbeans.test.performance|Main20kB.java] in project [PerformanceTestData]");
        }
    }
    
    public ComponentOperator open(){
        JPopupMenuOperator popup =  this.createTestsNode.callPopup();
        if (popup == null) {
            new Error("Cannot get context menu for node [" + gui.Utilities.SOURCE_PACKAGES + "|org.netbeans.test.performance|Main20kB.java] in project [PerformanceTestData]");
        }
        
        try {
            popup.pushMenu(CREATE_JUNIT_TESTS);
        }
        catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            throw new Error ("Cannot push Create JUnit Tests on node [" + gui.Utilities.SOURCE_PACKAGES + "|org.netbeans.test.performance|Main20kB.java] in project [PerformanceTestData]");
        }
        return new NbDialogOperator("Create Tests"); //NOI18N
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new CreateTestsDialog("measureTime"));
    }
    
}
