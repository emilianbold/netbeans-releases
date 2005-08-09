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

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.OpenAction;

/**
 * Test of opening files if Editor is already opened.
 * OpenFiles is used as a base for tests of opening files
 * when editor is already opened.
 *
 * @author  mmirilovic@netbeans.org
 */
public class OpenFormFileWithOpenedEditor extends OpenFormFile {
    
    /**
     * Creates a new instance of OpenFilesWithOpenedEditor
     * @param testName the name of the test
     */
    public OpenFormFileWithOpenedEditor(String testName) {
        super(testName);
    }
    
    /**
     * Creates a new instance of OpenFilesWithOpenedEditor
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenFormFileWithOpenedEditor(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }

    public void testOpening20kBFormFile(){
        super.testOpening20kBFormFile();
        WAIT_AFTER_OPEN = 9000;
    }
    
    /**
     * Initialize test - open Main.java file in the Source Editor.
     */
    public void initialize(){
        super.initialize();
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().getProjectRootNode("PerformanceTestData"), gui.Utilities.SOURCE_PACKAGES + "|org.netbeans.test.performance|Main.java"));
    }
 
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new OpenFormFileWithOpenedEditor("testOpening20kBFormFile"));
    }
    
}
