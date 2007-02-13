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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package footprint;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Measure Web Project Workflow Memory footprint
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class WebProjectWorkflow extends MemoryFootprintTestCase {

    private String webproject;

    /**
     * Creates a new instance of WebProjectWorkflow
     * @param testName the name of the test
     */
    public WebProjectWorkflow(String testName) {
        super(testName);
        prefix = "Web Project Workflow |";
    }

    /**
     * Creates a new instance of WebProjectWorkflow
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public WebProjectWorkflow(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        prefix = "Web Project Workflow |";
    }
    
    public void initialize() {
        super.initialize();
        FootprintUtilities.closeAllDocuments();
        FootprintUtilities.closeMemoryToolbar();
        FootprintUtilities.closeUIGesturesToolbar();
    }
    
    public void prepare() {
    }
    
    public ComponentOperator open(){
        // Web project
        webproject = FootprintUtilities.createproject("Samples|Web", "Tomcat Servlet Example", false);
        //EditorWindowOperator ewo = new EditorWindowOperator();
        //EditorOperator eo = ewo.selectPage("index.html");
        //eo.close(false);
        //checkScanFinished();
        FootprintUtilities.waitForPendingBackgroundTasks();
        FootprintUtilities.openFile(webproject, "<default package>", "SessionExample.java", true);
        FootprintUtilities.buildproject(webproject);
        FootprintUtilities.deployProject(webproject);
        //FootprintUtilities.collapseProject(webproject);
        
        return null;
    }
    
    public void close(){
        FootprintUtilities.deleteProject(webproject);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new WebProjectWorkflow("measureMemoryFooprint"));
    }
    
}
