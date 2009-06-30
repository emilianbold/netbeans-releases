/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.jellytools.actions;

import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewJavaProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.junit.NbTestSuite;

/** Test org.netbeans.jellytools.actions.ExploreFromHereAction
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class ExploreFromHereActionTest extends JellyTestCase {

    public static final String[] tests = new String[] {
        "testInit"
    };
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public ExploreFromHereActionTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition */
    public static Test suite() {
        /*
        TestSuite suite = new NbTestSuite();
        suite.addTest(new ExploreFromHereActionTest("testInit"));
        // Explore from here is used on web services node but to create such
        // a node you need application server installed. For now we skip these two tests.
        //suite.addTest(new ExploreFromHereActionTest("testPerformPopup"));
        //suite.addTest(new ExploreFromHereActionTest("testPerformAPI"));
        return suite;
         */
        return createModuleTest(ExploreFromHereActionTest.class, tests);
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

    @Override
    protected void setUp() throws IOException {
        openDataProjects("SampleProject");
    }
    
    
    
    /** Test performPopup */
    public void testPerformPopup() throws Exception {
        // create new web application project
        
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        // "Web"
        String webLabel = Bundle.getString("org.netbeans.modules.web.core.Bundle", "Templates/JSP_Servlet");
        npwo.selectCategory(webLabel);
        // "Web Application"
        String webApplicationLabel = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.web.project.ui.wizards.Bundle", "Templates/Project/Web/emptyWeb.xml");
        npwo.selectProject(webApplicationLabel);
        npwo.next();
        NewJavaProjectNameLocationStepOperator npnlso = new NewJavaProjectNameLocationStepOperator();
        npnlso.txtProjectName().setText(SAMPLE_WEB_PROJECT_NAME);
        npnlso.txtProjectLocation().setText(System.getProperty("netbeans.user")); // NOI18N
        npnlso.finish();
        // wait project appear in projects view
        Node projectRootNode = new ProjectsTabOperator().getProjectRootNode(SAMPLE_WEB_PROJECT_NAME);
        // wait index.jsp is opened in editor
        EditorOperator editor = new EditorOperator("index.jsp"); // NOI18N
        // wait classpath scanning finished
        try {
            Class.forName("org.netbeans.api.java.source.SourceUtils", true, Thread.currentThread().getContextClassLoader()).
                    getMethod("waitScanFinished").invoke(null);
        } catch (ClassNotFoundException x) {
            System.err.println("Warning: org.netbeans.api.java.source.SourceUtils could not be found, will not wait for scan to finish");
        }
        
        // create a web service

        // "Web Services"
        String webServicesLabel = Bundle.getString(
                "org.netbeans.modules.websvc.core.client.wizard.Bundle", "Templates/WebServices");
        // "Web Service"
        String webServiceLabel = org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.websvc.core.dev.wizard.Bundle", "Templates/WebServices/WebService.java");
        NewFileWizardOperator.invoke(projectRootNode, webServicesLabel, webServiceLabel);
        NewJavaFileNameLocationStepOperator nameStepOper = new NewJavaFileNameLocationStepOperator();
        nameStepOper.setPackage("dummy"); // NOI18N
        nameStepOper.setObjectName(SAMPLE_WEB_SERVICE_NAME);
        nameStepOper.finish();
        // check class is opened in Editor and then close all documents
        new EditorOperator(SAMPLE_WEB_SERVICE_NAME).closeAllDocuments();

        // "Web Services"
        String webServicesNodeLabel = Bundle.getString(
                "org.netbeans.modules.websvc.core.Bundle", "LBL_WebServices");
        Node wsNode = new Node(projectRootNode, webServicesNodeLabel+"|"+SAMPLE_WEB_SERVICE_NAME);
        new ExploreFromHereAction().performPopup(wsNode);
        new TopComponentOperator(SAMPLE_WEB_SERVICE_NAME).close();  // NOI18N
    }
    
    /** Test performAPI */
    public void testPerformAPI() {
        // "Web Services"
        String webServicesNodeLabel = Bundle.getString(
                "org.netbeans.modules.websvc.core.Bundle", "LBL_WebServices");
        Node projectRootNode = new ProjectsTabOperator().getProjectRootNode(SAMPLE_WEB_PROJECT_NAME);
        Node wsNode = new Node(projectRootNode, webServicesNodeLabel+"|"+SAMPLE_WEB_SERVICE_NAME);
        new ExploreFromHereAction().performAPI(wsNode);
        new TopComponentOperator(SAMPLE_WEB_SERVICE_NAME).close();  // NOI18N
    }
}
