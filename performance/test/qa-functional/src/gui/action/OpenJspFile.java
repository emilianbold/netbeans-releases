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

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

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
    }
    
    /**
     * Creates a new instance of OpenFiles
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenJspFile(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void testOpening20kBJSPFile(){
        WAIT_AFTER_OPEN = 3000;
        setPaintFilteringForEditor();
        setXMLEditorCaretFilteringOn();
        fileProject = "PerformanceTestWebApplication";
        fileName = "Test.jsp";
        menuItem = EDIT;
        doMeasurement();
    }
    
    public void prepare(){
        this.openNode = new Node(new ProjectsTabOperator().getProjectRootNode(fileProject),"Web Pages" + '|' + fileName);
        
        if (this.openNode == null) {
            fail ("Cannot find node ["+"Web Pages" + '|' + fileName + "] in project [" + fileProject + "]");
        }
        log("========== Open file path ="+this.openNode.getPath());
    }
    
    public ComponentOperator open(){
        JPopupMenuOperator popup =  this.openNode.callPopup();
        if (popup == null) {
            fail ("Cannot get context menu for node ["+"Web Pages" + '|' +  fileName + "] in project [" + fileProject + "]");
        }
        log("------------------------- after popup invocation ------------");
        try {
            popup.pushMenu(this.menuItem);
        }
        catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            fail ("Cannot push menu item "+this.menuItem+" of node ["+"Web Pages" + '|' +  fileName + "] in project [" + fileProject + "]");
        }
        log("------------------------- after open ------------");
        return new EditorOperator(this.fileName);
    }
    
}
