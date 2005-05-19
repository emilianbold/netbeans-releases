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

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;

import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of Closing All Documents.
 *
 * @author  mmirilovic@netbeans.org
 */
public class CloseAllEditors extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    /** Nodes represent files to be opened */
    private static Node[] openFileNodes;
    
    
    /**
     * Creates a new instance of CloseAllEditors
     * @param testName the name of the test
     */
    public CloseAllEditors(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_PREPARE=1000;
    }
    
    /**
     * Creates a new instance of CloseAllEditors
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CloseAllEditors(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_PREPARE=1000;
    }
    
    
    public void testClosingAllJavaFiles(){
        doMeasurement();
    }
    
    public void initialize(){
        EditorOperator.closeDiscardAll();
        prepareFiles();
    }

    public void shutdown(){
        EditorOperator.closeDiscardAll();
    }
    
    public void prepare(){
//        for(int i=0; i<openFileNodes.length; i++) {
//            new OpenAction().performAPI(openFileNodes[i]); // fix for mdr+java, opening all files at once causes never ending loop
//        }
        new OpenAction().performAPI(openFileNodes);
    }
    
    public ComponentOperator open(){
        new CloseAllDocumentsAction().performMenu();
        return null;
    }
    
    
    /**
     * Prepare ten selected file from jEdit project
     */
    protected void prepareFiles(){
        String[][] files_path = gui.Utilities.getTenSelectedFiles();
        
        openFileNodes = new Node[files_path.length];
            
        for(int i=0; i<files_path.length; i++) {
                openFileNodes[i] = new Node(new ProjectsTabOperator().getProjectRootNode("jEdit"), gui.Utilities.SOURCE_PACKAGES + '|' +  files_path[i][0] + '|' + files_path[i][1]);
        }
    }
    
    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new CloseAllEditors("measureTime"));
    }
    
}
