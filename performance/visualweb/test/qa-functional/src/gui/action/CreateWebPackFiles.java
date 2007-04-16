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

import gui.VWPUtilities;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.TimeoutExpiredException;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.NbTestSuite;

/**
 * Test create Web Pack projects
 *
 * @author  mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class CreateWebPackFiles extends org.netbeans.performance.test.utilities.PerformanceTestCase {
   
    private String doccategory, doctype, docname, docfolder, suffix, projectfolder, buildedname;
    private NewFileNameLocationStepOperator location;
    
    private int index;
    private static final String project_name = "VisualWebProject";
    
    /**
     * Creates a new instance of CreateWebPackFiles
     * @param testName the name of the test
     */
    public CreateWebPackFiles(String testName) {
        super(testName);
    }
    
    /**
     * Creates a new instance of CreateWebPackFiles
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateWebPackFiles(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CreateWebPackFiles("testCreateJSPPage","Create JSP Page"));
        suite.addTest(new CreateWebPackFiles("testCreateJSPFragment","Create JSP fragment"));
        suite.addTest(new CreateWebPackFiles("testCreateCSSTable","Create CSS Table"));
        return suite;
    }  
    
    public void testCreateJSPPage(){
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=15000;
        docname = "JSFPage"; //NOI18N
        doccategory = "Web"; //NOI18N
        doctype ="Visual Web JSF Page"; //NOI18N
	docfolder = "web";
	suffix = ".jsp";
        projectfolder = VWPUtilities.WEB_PAGES;
	doMeasurement();
    }
    
    public void testCreateJSPFragment(){
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=15000;
        docname = "JSFFragment"; //NOI18N
        doccategory = "Web"; //NOI18N
        doctype = "Visual Web JSF Page Fragment"; //NOI18N
	docfolder = "web";
	suffix = ".jspf";
        projectfolder = VWPUtilities.WEB_PAGES;
	doMeasurement();
    }
    
    public void testCreateCSSTable(){
        expectedTime = 1000;
        WAIT_AFTER_OPEN=5000;
	docname = "CSSTable"; //NOI18N
        doccategory = "XML"; //NOI18N
        doctype = "Cascading Style Sheet"; //NOI18N
	docfolder = "web" + java.io.File.separatorChar + "resources"; // NOI18N
	suffix = ".css";
        projectfolder = VWPUtilities.WEB_PAGES+"|"+"resources"; // NOI18N
	doMeasurement();
    }
    
    public ComponentOperator open(){
        log("::open::");
        location.finish();
	
        return null; //new EditorOperator(docname+"_"+(index)+suffix);
    }
    
    public void initialize(){
	log("::initialize::");
        Node projectRoot = null;
        try {
            projectRoot = new ProjectsTabOperator().getProjectRootNode(project_name);
            projectRoot.select();
            
        } catch (org.netbeans.jemmy.TimeoutExpiredException ex) {
            fail("Cannot find and select project root node");
        }
    }

    public void prepare(){
        log("::prepare::");

        NewFileWizardOperator wizard = NewFileWizardOperator.invoke();
        
        // create exactly (full match) and case sensitively comparing comparator
        Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, true);
        wizard.lstFileTypes().setComparator(comparator);
        
        wizard.selectCategory(doccategory);
        wizard.selectFileType(doctype);
	
        wizard.next();

        waitNoEvent(1000);
        location = new NewFileNameLocationStepOperator();
        buildedname = docname+"_"+System.currentTimeMillis();
        location.txtObjectName().setText(buildedname);

	JTextFieldOperator pathField = new JTextFieldOperator(wizard,2);
	pathField.setText(docfolder);
        waitNoEvent(1000);
    }

    public void close(){
        log("::close");
        cleanupTest();
        new CloseAllDocumentsAction().performAPI(); //avoid issue 68671 - editors are not closed after closing project by ProjectSupport
    }
    
    private void cleanupTest() {
        log(":: do cleanup.....");
        Node projectRootNode = new ProjectsTabOperator().getProjectRootNode(project_name);        
        Node objNode = new Node(projectRootNode,projectfolder+"|"+ buildedname+suffix);
        objNode.select();
        log(":: Document: "+buildedname+suffix);
        log(":: Selected: "+objNode.getTreePath().toString());
        
        try {
            new DeleteAction().performAPI(objNode);
            String dialogCaption = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.visualweb.navigation.Bundle", "MSG_ConfirmDeleteObjectTitle");
            new NbDialogOperator(dialogCaption).yes();
            
        } catch (TimeoutExpiredException timeoutExpiredException) {
            log("Cleanup failed because of: "+timeoutExpiredException.getMessage());
            return;
        }
 
        log(":: cleanup passed");
    }
    
    public void shutdown() {
        log("::shutdown");
        super.shutdown();
    }
    
    public static void main(String[] args) {
       junit.textui.TestRunner.run(suite()); 
    }

}
