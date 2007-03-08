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
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 * Test of opening files.
 *
 * @author  mmirilovic@netbeans.org
 */
public class OpenFiles extends org.netbeans.performance.test.utilities.PerformanceTestCase {

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
    
    protected static String OPEN = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
    
    protected static String EDIT = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Edit");
    
    /**
     * Creates a new instance of OpenFiles
     * @param testName the name of the test
     */
    public OpenFiles(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /**
     * Creates a new instance of OpenFiles
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenFiles(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void testOpening20kBJavaFile(){
        WAIT_AFTER_OPEN = 6000;
        setJavaEditorCaretFilteringOn();
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "Main20kB.java";
        menuItem = OPEN;
        doMeasurement();
    }
    
    public void testOpening20kBTxtFile(){
        WAIT_AFTER_OPEN = 1500;
        setPlainTextEditorCaretFilteringOn();
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "textfile20kB.txt";
        menuItem = OPEN;
        doMeasurement();
    }
    
    public void testOpening20kBXmlFile(){
        WAIT_AFTER_OPEN = 3000;
        setXMLEditorCaretFilteringOn();
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "xmlfile20kB.xml";
        menuItem = EDIT;
        doMeasurement();
    }
    
    protected void initialize(){
        EditorOperator.closeDiscardAll();
        repaintManager().setOnlyEditor(true);
    }

    protected void shutdown(){
        EditorOperator.closeDiscardAll();
        repaintManager().setOnlyEditor(false);
    }
    
    public void prepare(){
        this.openNode = new Node(new ProjectsTabOperator().getProjectRootNode(fileProject), gui.Utilities.SOURCE_PACKAGES + '|' +  filePackage + '|' + fileName);
        
        if (this.openNode == null) {
            throw new Error ("Cannot find node [" + openNode.getPath() + "] in project [" + fileProject + "]");
        }
        log("========== Open file path ="+this.openNode.getPath());
    }
    
    public ComponentOperator open(){
        JPopupMenuOperator popup =  this.openNode.callPopup();
        if (popup == null) {
            throw new Error ("Cannot get context menu for node ["+ openNode.getPath() + "] in project [" + fileProject + "]");
        }
        log("------------------------- after popup invocation ------------");
        
        try {
            popup.pushMenu(this.menuItem);
        }
        catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            tee.printStackTrace(getLog());
            throw new Error ("Cannot push menu item ["+this.menuItem+"] of node [" + openNode.getPath() + "] in project [" + fileProject + "]");
        }
        log("------------------------- after open ------------");
        return new EditorOperator(this.fileName);
    }
    
    public void close(){
        if (testedComponentOperator != null) {
            ((EditorOperator)testedComponentOperator).closeDiscard();
        }
        else {
            throw new Error ("no component to close");
        }
    }
    
}
