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

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Test create Web Pack projects
 *
 * @author  mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class CreateWebPackFiles extends org.netbeans.performance.test.utilities.PerformanceTestCase {
   
    private String doccategory, doctype, docname, docfolder, suffix, projectfolder;
    private NewFileNameLocationStepOperator location;
    
    private int index;
    private static final String project_name = "VisualWebProject";
    /**
     * Creates a new instance of CreateWebPackFiles
     * @param testName the name of the test
     */
    public CreateWebPackFiles(String testName) {
        super(testName);
        expectedTime = 18000;
        WAIT_AFTER_OPEN=20000;
    }
    
    /**
     * Creates a new instance of CreateWebPackFiles
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateWebPackFiles(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=20000;
    }
    
    public void testCreateJSPPage(){
        docname = "JSFPage"; //NOI18N
        doccategory = "Visual Web"; //NOI18N
        doctype ="Page"; //NOI18N
	docfolder = "web";
	suffix = "";
        index = 1;
        projectfolder = "Web Pages";
	doMeasurement();
    }
    
    public void testCreateJSPFragment(){
        docname = "JSFFragment"; //NOI18N
        doccategory = "Visual Web"; //NOI18N
        doctype = "Page Fragment"; //NOI18N
	docfolder = "web";
	suffix = "";
        index = 1;
        projectfolder = "Web Pages";
	doMeasurement();
    }
    
    public void testCreateCSSTable(){
	docname = "CSSTable"; //NOI18N
        doccategory = "Other"; //NOI18N
        doctype = "Cascading Style Sheet"; //NOI18N
	docfolder = "web" + java.io.File.separatorChar + "resources";
	suffix = ".css";
        index = 1;
        projectfolder = "Web Pages"+"|"+"resources";
	doMeasurement();
    }
    
    public ComponentOperator open(){
        log("::open::");
        location.finish();
	
        return null; //new EditorOperator(docname+"_"+(index)+suffix);
    }
    
    public void initialize(){
	log("::initialize::");
        new ProjectsTabOperator().getProjectRootNode(project_name).select();
    }

    public void prepare(){
        log("::prepare::");
        //new NewFileAction().perform();

        NewFileWizardOperator wizard = NewFileWizardOperator.invoke();
        
        wizard.selectCategory(doccategory);
        wizard.selectFileType(doctype);
	
        wizard.next();

        waitNoEvent(1000);
        location = new NewFileNameLocationStepOperator();
        location.txtObjectName().setText(docname+"_"+(index));

	JTextFieldOperator pathField = new JTextFieldOperator(wizard,2);
	pathField.setText(docfolder);
        waitNoEvent(1000);
    }

    public void close(){
        log("::close");
        cleanupTest();
	index++;
        new CloseAllDocumentsAction().performAPI(); //avoid issue 68671 - editors are not closed after closing project by ProjectSupport
    }
    
    private void cleanupTest() {
        log(":: do cleanup.....");
        Node projectRoot = new ProjectsTabOperator().getProjectRootNode(project_name);        
        Node objNode = new Node(projectRoot.tree(),projectfolder+"|"+ docname+"_"+(index)+suffix);
        objNode.select();
        log(":: Document: "+docname+"_"+(index)+suffix);
        log(":: Selected: "+objNode.getTreePath().toString());
        
        objNode.performPopupAction("Delete");
        String dialogName = org.netbeans.jellytools.Bundle.getString("com.sun.rave.navigation.Bundle", "MSG_ConfirmDeleteObjectTitle");
        
        new NbDialogOperator(dialogName).ok();
        log(":: cleanup passed");
        
    }
    protected void shutdown() {
        log("::shutdown");
        super.shutdown();

    }

}
