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

import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jellytools.actions.OpenAction;

/**
 * Test of opening JSP file if Editor is opened.
 *
 * @author  mmirilovic@netbeans.org
 */
public class OpenJspFileWithOpenedEditor extends OpenJspFile {

    /**
     * Creates a new instance of OpenFiles
     * @param testName the name of the test
     */
    public OpenJspFileWithOpenedEditor(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /**
     * Creates a new instance of OpenFiles
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenJspFileWithOpenedEditor(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void testOpening20kBJSPFile(){
        WAIT_AFTER_OPEN = 3000;
        setJSPEditorCaretFilteringOn();
        fileProject = "PerformanceTestWebApplication";
        fileName = "Test.jsp";
        menuItem = OPEN;
        doMeasurement();
    }
    
    /**
     * Initialize test - open Main.java file in the Source Editor.
     */
    public void initialize(){
        super.initialize();
        new OpenAction().performAPI(new Node(new SourcePackagesNode("PerformanceTestData"),"org.netbeans.test.performance|Main.java"));
    }
    
}
