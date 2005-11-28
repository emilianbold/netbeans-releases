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

import gui.Utilities;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;

import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;


/**
 * Test of expanding container in Component Inspector.
 *
 * @author  mmirilovic@netbeans.org
 */
public class ExpandNodesInComponentInspector extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static Node nodeToBeExpanded;
    
    /**
     * Creates a new instance of ExpandNodesInComponentInspector
     * @param testName the name of the test
     */
    public ExpandNodesInComponentInspector(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /**
     * Creates a new instance of ExpandNodesInComponentInspector
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public ExpandNodesInComponentInspector(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void initialize(){
        Utilities.openSmallFormFile();
        waitNoEvent(5000);
    }
    
    public void shutdown(){
        EditorOperator.closeDiscardAll();
    }
    
    public void prepare(){
        nodeToBeExpanded = new Node(new ComponentInspectorOperator().treeComponents(), "[JFrame]");
    }
    
    public ComponentOperator open(){
        nodeToBeExpanded.tree().clickOnPath(nodeToBeExpanded.getTreePath(), 2);
//        nodeToBeExpanded.tree().clickMouse(2);
//        nodeToBeExpanded.waitExpanded();
        nodeToBeExpanded.expand();
        return null;
    }
    
    public void close(){
        nodeToBeExpanded.collapse();
    }

    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new CloseEditor("testClosing20kBJavaFile"));
        junit.textui.TestRunner.run(new ExpandNodesInComponentInspector("measureTime"));
    }
    
}
