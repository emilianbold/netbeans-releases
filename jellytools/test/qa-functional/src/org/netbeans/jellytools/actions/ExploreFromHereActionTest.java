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
package org.netbeans.jellytools.actions;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;

/** Test org.netbeans.jellytools.actions.ExploreFromHereAction
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class ExploreFromHereActionTest extends JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public ExploreFromHereActionTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new ExploreFromHereActionTest("testInit"));
        // Explore from here is used on web services node but to create such
        // a node you need application server installed. For now we skip these two tests.
        //suite.addTest(new ExploreFromHereActionTest("testPerformPopup"));
        //suite.addTest(new ExploreFromHereActionTest("testPerformAPI"));
        return suite;
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    // name of sample web application project
    private static final String SAMPLE_WEB_PROJECT_NAME = "SampleWebProject";  //NOI18N
    // name of sample web service
    private static final String SAMPLE_WEB_SERVICE_NAME = "SampleWebService";  //NOI18N

    /** Just to test action is still used in IDE. In constructor is accessed key
        from Bundle.properties.
     */
    public void testInit() {
        new ExploreFromHereAction();
    }
    
    /** Test performPopup */
    public void testPerformPopup() {
        // create new web application project
        
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        // "Web"
        String webLabel = Bundle.getString("org.netbeans.modules.web.core.Bundle", "Templates/JSP_Servlet");
        npwo.selectCategory(webLabel);
        // "Web Application"
        String webApplicationLabel = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.web.project.ui.wizards.Bundle", "Templates/Project/Web/emptyWeb.xml");
        npwo.selectProject(webApplicationLabel);
        npwo.next();
        NewProjectNameLocationStepOperator npnlso = new NewProjectNameLocationStepOperator();
        npnlso.txtProjectName().setText(SAMPLE_WEB_PROJECT_NAME);
        npnlso.txtProjectLocation().setText(System.getProperty("netbeans.user")); // NOI18N
        npnlso.finish();
        // wait project appear in projects view
        Node projectRootNode = new ProjectsTabOperator().getProjectRootNode(SAMPLE_WEB_PROJECT_NAME);
        // wait index.jsp is opened in editor
        EditorOperator editor = new EditorOperator("index.jsp"); // NOI18N
        // wait classpath scanning finished
        ProjectSupport.waitScanFinished();
        
        // create a web service

        // "Web Services"
        String webServicesLabel = Bundle.getString(
                "org.netbeans.modules.websvc.dev.wizard.Bundle", "Templates/WebServices");
        // "Web Service"
        String webServiceLabel = org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.websvc.dev.wizard.Bundle", "Templates/WebServices/WebService");
        NewFileWizardOperator.invoke(projectRootNode, webServicesLabel, webServiceLabel);
        NewFileNameLocationStepOperator nameStepOper = new NewFileNameLocationStepOperator();
        nameStepOper.setPackage("dummy"); // NOI18N
        nameStepOper.setObjectName(SAMPLE_WEB_SERVICE_NAME);
        nameStepOper.finish();
        // check class is opened in Editor and then close all documents
        new EditorOperator(SAMPLE_WEB_SERVICE_NAME).closeAllDocuments();

        // "Web Services"
        String webServicesNodeLabel = Bundle.getString(
                "org.netbeans.modules.websvc.core.webservices.ui.Bundle", "LBL_WebServices");
        Node wsNode = new Node(projectRootNode, webServicesNodeLabel+"|"+SAMPLE_WEB_SERVICE_NAME);
        new ExploreFromHereAction().performPopup(wsNode);
        new TopComponentOperator(SAMPLE_WEB_SERVICE_NAME).close();  // NOI18N
    }
    
    /** Test performAPI */
    public void testPerformAPI() {
        // "Web Services"
        String webServicesNodeLabel = Bundle.getString(
                "org.netbeans.modules.websvc.core.webservices.ui.Bundle", "LBL_WebServices");
        Node projectRootNode = new ProjectsTabOperator().getProjectRootNode(SAMPLE_WEB_PROJECT_NAME);
        Node wsNode = new Node(projectRootNode, webServicesNodeLabel+"|"+SAMPLE_WEB_SERVICE_NAME);
        new ExploreFromHereAction().performAPI(wsNode);
        new TopComponentOperator(SAMPLE_WEB_SERVICE_NAME).close();  // NOI18N
    }
}
