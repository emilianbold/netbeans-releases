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

package org.netbeans.test.web;

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.actions.NewProjectAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.operators.*;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author  lm97939
 */
public class WebProjectValidationNb36WebModule extends WebProjectValidation {
    
    static {
        PROJECT_NAME = "WebModuleNB36"; // NOI18N
        PROJECT_FOLDER = PROJECT_LOCATION+File.separator+PROJECT_NAME;
    }
    
    /** Need to be defined because of JUnit */
    public WebProjectValidationNb36WebModule(String name) {
        super(name);
    }
    
    /** Need to be defined because of JUnit */
    public WebProjectValidationNb36WebModule() {
        super();
    }
    
    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(WebProjectValidationNb36WebModule.class);
        conf = addServerTests(Server.TOMCAT, conf, 
              "testNewWebProject", "testNewJSP", "testNewJSP2", "testNewServlet", "testNewServlet2",
              "testCompileAllJSP", "testCompileJSP",
              "testCleanAndBuildProject", "testRunProject", "testRunJSP", 
              "testRunServlet", "testCreateTLD", "testCreateTagHandler", "testRunTag",
              "testNewHTML", "testRunHTML", "testNewSegment", "testNewDocument",
              "testStopServer", "testStartServer", "testFinish");
        conf = conf.enableModules(".*").clusters(".*");
        return NbModuleSuite.create(conf);
    }
    
    /** Test creation of web application.
     * - open New Project wizard from main menu (File|New Project)
     * - select Web|Web Application
     * - in the next panel type project name and project location
     * - finish the wizard
     * - wait until scanning of java files is finished
     * - check index.jsp is opened
     */
    @Override
    public void testNewWebProject() throws IOException {
        installJemmyQueue();
        new NewProjectAction().perform();
        NewProjectWizardOperator projectWizard = new NewProjectWizardOperator();
        projectWizard.selectCategory("Java Web"); // XXX use Bundle.getString instead
        projectWizard.selectProject("Web Application with Existing Sources");
        projectWizard.next();
        NewWebProjectNameLocationStepOperator
        nameStep = new NewWebProjectNameLocationStepOperator();
        nameStep.txtLocation().setText(getDataDir().getAbsolutePath()+
                File.separator+PROJECT_NAME);
        nameStep.txtProjectName().setText(PROJECT_NAME);
        nameStep.txtProjectFolder().setText(getWorkDirPath()+
                File.separator+PROJECT_NAME+"Prj");
        nameStep.next();
        NewWebProjectServerSettingsStepOperator serverStep = new NewWebProjectServerSettingsStepOperator();
        serverStep.cboServer().selectItem(0);
        serverStep.next();
        NewWebProjectSourcesStepOperator srcStep =  new NewWebProjectSourcesStepOperator();
        srcStep.finish();
        Timeouts timeouts = nameStep.getTimeouts().cloneThis();
        srcStep.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 90000);
        srcStep.waitClosed();
        srcStep.setTimeouts(timeouts);
        sleep(5000);
        // wait for project creation
        ProjectSupport.waitScanFinished();
        verifyWebPagesNode("META-INF|context.xml");
        verifyProjectNode("Configuration Files|web.xml");
    }

    /** Test new JSP wizard.
     * - open New File wizard from main menu (File|New File)
     * - select sample project as target
     * - select Web|JSP file type
     * - in the next panel type name
     * - finish the wizard
     * - check file is open in editor and close all opened documents
     */
    @Override
    public void testNewJSP() throws IOException {
        new ActionNoBlock("File|New File", null).perform();
        WizardOperator newFileWizard = new WizardOperator("New File");
        new JComboBoxOperator(newFileWizard).selectItem(PROJECT_NAME);
        new Node(new JTreeOperator(newFileWizard), "Web").select();
        new JListOperator(newFileWizard, 1).selectItem("JSP");
        newFileWizard.next();
        JTextFieldOperator txtPackageName = new JTextFieldOperator(newFileWizard);
        // clear text field
        //XXX <workaround issue='#61034 - wrond default destination'>
        txtPackageName.setText("index1");
        JTextFieldOperator txtProjectFolder = new JTextFieldOperator(
                newFileWizard, 2);
        txtProjectFolder.setText("");
        //</workaround>
        newFileWizard.finish();
        // check class is opened in Editor and then close it
        new EditorOperator("index1.jsp").close();
        ref(Util.dumpProjectView(PROJECT_NAME));
        new Node(new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME),"Web Pages|index1.jsp");
        //compareReferenceFiles();
        //compareDD();
    }

    @Override
     public void testCreateTLD() {
        new ActionNoBlock("File|New File", null).perform();
        WizardOperator newFileWizard = new WizardOperator("New File");
        new JComboBoxOperator(newFileWizard).selectItem(PROJECT_NAME);
        new Node(new JTreeOperator(newFileWizard), "Web").select();
        new JListOperator(newFileWizard, 1).selectItem("Tag Library Descriptor");
        newFileWizard.next();
        JTextFieldOperator txtName = new JTextFieldOperator(newFileWizard);
        // clear text field
        txtName.setText("");
        txtName.typeText("MyTags");
        newFileWizard.finish();
        //XXX try { Thread.currentThread().sleep(5000); } catch (InterruptedException e) {}
        //XXX HACK #48865
        new Node(new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME), "Web|WEB-INF|tlds|MyTags.tld");
        // check class is opened in Editor and then close it
        new EditorOperator("MyTags.tld").close();
        ref(Util.dumpProjectView(PROJECT_NAME));
        //compareReferenceFiles();
    }
   
    @Override
    public void testNewServlet() throws IOException {
        // create a new package
        new ActionNoBlock("File|New File", null).perform();
        // WORKAROUND
        new EventTool().waitNoEvent(1000);
        WizardOperator newFileWizard = new WizardOperator("New File");
        new JComboBoxOperator(newFileWizard).selectItem(PROJECT_NAME);
        new Node(new JTreeOperator(newFileWizard), "Web").select();
        new JListOperator(newFileWizard, 1).selectItem("Servlet");
        newFileWizard.next();
        JTextFieldOperator txtPackageName = new JTextFieldOperator(newFileWizard);
        // clear text field
        txtPackageName.setText("");
        txtPackageName.typeText("Servlet1");
        JComboBoxOperator txtPackage = new JComboBoxOperator(newFileWizard,1);
        // clear text field
        txtPackage.clearText();
        txtPackage.typeText("test1");
        newFileWizard.next();
        newFileWizard.finish();
        // check class is opened in Editor and close it
        new EditorOperator("Servlet1.java").close();
        // check the servlet is specified in web.xml
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        rootNode.setComparator(new Operator.DefaultStringComparator(true, true));
        Node webXml = new Node(rootNode, "WEB-INF|web.xml");
        new EditAction().performPopup(webXml);
        String xmlText = new EditorOperator("web.xml").getText();
        new EditorOperator("web.xml").closeAllDocuments();
        String[] content = new String[] {
            "<servlet-name>Servlet1</servlet-name>",
            "<servlet-class>test1.Servlet1</servlet-class>",
            "<url-pattern>/Servlet1</url-pattern>"
        };
        for (int i=0; i<content.length; i++) {
            assertTrue("Servlet is not correctly specifeid in web.xml." +
                    " Following line is missing in the web.xml:\n"+content[i],
                    xmlText.indexOf(content[i]) != -1);
        }
    }

    
}
