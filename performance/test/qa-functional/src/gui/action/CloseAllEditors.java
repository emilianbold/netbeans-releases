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

import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

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
        SourcePackagesNode sourcePackagesNode = new SourcePackagesNode("jEdit");
        for(int i=0; i<files_path.length; i++) {
                openFileNodes[i] = new Node(sourcePackagesNode, files_path[i][0] + '|' + files_path[i][1]);
        }
    }
    
    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new CloseAllEditors("measureTime"));
    }
    
}
