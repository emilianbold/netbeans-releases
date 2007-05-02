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

package gui.action;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.test.web.performance.WebPerformanceTestCase;

/**
 * Test of opening files.
 *
 * @author  mmirilovic@netbeans.org
 */
public class OpenServletFile extends WebPerformanceTestCase {
    
    /** Node to be opened/edited */
    public static Node openNode ;
    
    /** Folder with data */
    public static String fileProject;
    
    /** Folder with data  */
    public static String filePackage;
    
    /** Name of file to open */
    public static String fileName;
    
    /** Menu item name that opens the editor */
    public static String menuItem;
    
    protected static String OPEN = "Open"; //NOI18N
    
    protected static String EDIT = "Edit"; //NOI18N
    
    /**
     * Creates a new instance of OpenFiles
     * @param testName the name of the test
     */
    public OpenServletFile(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /**
     * Creates a new instance of OpenFiles
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenServletFile(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void testOpeningServletFile(){
        WAIT_AFTER_OPEN = 6000;
       //repaintManager().setOnlyEditor(true);
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
        setJavaEditorCaretFilteringOn();
        fileProject = "TestWebProject";
        filePackage = "test";
        fileName = "TestServlet.java";
        menuItem = OPEN;
        doMeasurement();
    }
    
    public void testOpeningJavaFile(){
        WAIT_AFTER_OPEN = 6000;
        //repaintManager().setOnlyEditor(true);
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
        setJavaEditorCaretFilteringOn();
        fileProject = "TestWebProject";
        filePackage = "test";
        fileName = "Main.java";
        menuItem = OPEN;
        doMeasurement();
    }
    
    public void initialize(){
        EditorOperator.closeDiscardAll();
    }

    public void shutdown(){
        EditorOperator.closeDiscardAll();
    }
    
    public void prepare(){
        this.openNode = new Node(new ProjectsTabOperator().getProjectRootNode(fileProject),"Source Packages" + '|' +  filePackage + '|' + fileName);
        
        if (this.openNode == null) {
            fail ("Cannot find node ["+"Source Packages" + '|' +  filePackage + '|' + fileName + "] in project [" + fileProject + "]");
        }
        log("========== Open file path ="+this.openNode.getPath());
    }
    
    public ComponentOperator open(){
        JPopupMenuOperator popup =  this.openNode.callPopup();
        if (popup == null) {
            fail ("Cannot get context menu for node ["+"Source Packages" + '|' +  filePackage + '|' + fileName + "] in project [" + fileProject + "]");
        }
        log("------------------------- after popup invocation ------------");
        try {
            popup.pushMenu(this.menuItem);
        }
        catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            fail ("Cannot push menu item "+this.menuItem+" of node ["+"Source Packages" + '|' +  filePackage + '|' + fileName + "] in project [" + fileProject + "]");
        }
        log("------------------------- after open ------------");
        return new EditorOperator(this.fileName);
    }
    
    public void close(){
        //repaintManager().setOnlyEditor(false);
        repaintManager().resetRegionFilters(); // added - was missing
        if (testedComponentOperator != null) {
            ((EditorOperator)testedComponentOperator).closeDiscard();
        }
        else {
            fail ("no component to close");
        }
    }
    
}
