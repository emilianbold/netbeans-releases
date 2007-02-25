/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

import gui.window.WebFormDesignerOperator;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */

public class OpenProjectFirstPage extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private Node openNode;
    private String project_name;
    
    /** Creates a new instance of OpenProjectFirstPage */
    public OpenProjectFirstPage(String testName) {
        super(testName);
        expectedTime = 18000;
        WAIT_AFTER_OPEN=20000;
    }
    
    /** Creates a new instance of OpenProjectFirstPage */
    public OpenProjectFirstPage(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 18000;
        WAIT_AFTER_OPEN=20000;
    }
    
    public void testOpenSmallProjectFirstPage() {
        project_name = "VisualWebProject";
        doMeasurement();
    }
    
    public void testOpenLargeProjectFirstPage() {
        project_name = "HugeApp";
        doMeasurement();
    }
    
//TODO remove ?    protected void openProject(String projectName) {
//        new ActionNoBlock("File|Open Project...",null).perform();
//        WizardOperator wizard = new WizardOperator("Open Project");
//        JTextComponentOperator path = new JTextComponentOperator(wizard,1);
//        
//        String buttonCaption = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "BTN_PrjChooser_ApproveButtonText");
//        JButtonOperator openButton = new JButtonOperator(wizard,buttonCaption);
//        
//        String paths = System.getProperty("xtest.tmpdir")+ java.io.File.separator +projectName;
//        path.setText(paths);
//        openButton.pushNoBlock();
//    }
    
    public void initialize(){
        log("::initialize::");
    }
    
    public void prepare(){
        log("::prepare");
//TODO remove ?        openProject(project_name);
//TODO remove ?        log("::open Project passed");
//TODO remove ?        new CloseAllDocumentsAction().performAPI();
        openNode = new Node(new ProjectsTabOperator().getProjectRootNode(project_name), gui.VWPUtilities.WEB_PAGES + '|' + "Page1.jsp");
        
        if (this.openNode == null) {
            throw new Error("Cannot find expected node ");
        }
        openNode.select();
    }
    
    public ComponentOperator open(){
        log("::open");
        JPopupMenuOperator popup =  this.openNode.callPopup();
        if (popup == null) {
            throw new Error("Cannot get context menu for node ");
        }
        log("------------------------- after popup invocation ------------");
        try {
            popup.pushMenu("Open");
        } catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            throw new Error("Cannot push menu item ");
        }
        
        return new WebFormDesignerOperator("Page1");
    }
    
    public void close(){
        log("::close");
        if(testedComponentOperator != null) { ((WebFormDesignerOperator)testedComponentOperator).close(); }
//TODO remove ?        ProjectSupport.closeProject(project_name);
//TODO remove ?        new CloseAllDocumentsAction().performAPI(); //avoid issue 68671 - editors are not closed after closing project by ProjectSupport
    }
    
    protected void shutdown() {
        log("::shutdown");
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new OpenProjectFirstPage("testOpenProjectFirstPage"));
    }
}
