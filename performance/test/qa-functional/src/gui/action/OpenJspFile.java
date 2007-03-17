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
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.modules.web.nodes.WebPagesNode;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of opening JSP file
 *
 * @author  mmirilovic@netbeans.org
 */
public class OpenJspFile extends OpenFiles {

    /**
     * Creates a new instance of OpenFiles
     * @param testName the name of the test
     */
    public OpenJspFile(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        HEURISTIC_FACTOR = -1; // disable heuristics, wait for all attempts same time
    }
    
    /**
     * Creates a new instance of OpenFiles
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenJspFile(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        HEURISTIC_FACTOR = -1; // disable heuristics, wait for all attempts same time
    }
    
    public void testOpening20kBJSPFile(){
        WAIT_AFTER_OPEN = 7000;
        setJSPEditorCaretFilteringOn();
        fileProject = "PerformanceTestWebApplication";
        fileName = "Test.jsp";
        menuItem = OPEN;
        doMeasurement();
    }
    
    public void prepare(){
        this.openNode = new Node(new WebPagesNode(fileProject),fileName);
        this.openNode.select();
        log("========== Open file path ="+this.openNode.getPath());
    }
    
    public ComponentOperator open(){
        new OpenAction().performPopup(this.openNode);
        return new EditorOperator(this.fileName);
    }
    
    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new OpenJspFile("testOpening20kBJSPFile"));
    }
    
}
