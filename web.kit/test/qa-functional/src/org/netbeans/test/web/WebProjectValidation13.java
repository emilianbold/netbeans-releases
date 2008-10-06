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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
import org.netbeans.jellytools.actions.NewProjectAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author  lm97939
 */
public class WebProjectValidation13 extends WebProjectValidation {
    static {
        PROJECT_NAME = "SampleProject13"; // NOI18N
        PROJECT_FOLDER = PROJECT_LOCATION+File.separator+PROJECT_NAME;
    }

    /** Need to be defined because of JUnit */
    public WebProjectValidation13(String name) {
        super(name);
    }
    
    /** Need to be defined because of JUnit */
    public WebProjectValidation13() {
        super();
    }

    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
        // run only selected test case
        //junit.textui.TestRunner.run(new MyModuleValidation("testT2"));
    }
    
    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(WebProjectValidation13.class);
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
    
    /** Test creation of web project.
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
        String sWeb = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.core.Bundle",
                "Templates/JSP_Servlet");
        projectWizard.selectCategory(sWeb);
        String sWeb_Application = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.project.ui.wizards.Bundle",
                "Templates/Project/Web/emptyWeb.xml");
        projectWizard.selectProject(sWeb_Application);
        projectWizard.next();
        NewWebProjectNameLocationStepOperator nameStep =
                new NewWebProjectNameLocationStepOperator();
        nameStep.txtProjectName().setText("");
        nameStep.txtProjectName().typeText(PROJECT_NAME);
        nameStep.txtProjectLocation().setText("");
        nameStep.txtProjectLocation().typeText(PROJECT_LOCATION);
        nameStep.next();
        NewWebProjectServerSettingsStepOperator serverStep = new NewWebProjectServerSettingsStepOperator();
        serverStep.selectServer(getServerNode(Server.ANY).getText());
        serverStep.selectJavaEEVersion(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.j2ee.common.project.ui.Bundle", "J2EESpecLevel_13"));
        serverStep.next();
        NewWebProjectSourcesStepOperator frameworkStep =  new NewWebProjectSourcesStepOperator();
        frameworkStep.finish();
        Timeouts timeouts = frameworkStep.getTimeouts().cloneThis();
        frameworkStep.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 60000);
        frameworkStep.waitClosed();
        frameworkStep.setTimeouts(timeouts);
        // wait for project creation
        sleep(5000);
        ProjectSupport.waitScanFinished();
        verifyWebPagesNode("index.jsp");
        verifyWebPagesNode("WEB-INF|web.xml");
        verifyWebPagesNode("META-INF|context.xml");
    }
    
    @Override
    protected String getTagHandlerCode() {
        return "try { JspWriter out = pageContext.getOut();\n out.print(\"TagOutput\");\n} catch (java.io.IOException e) {} \n";
    }
    
    @Override
    public void testCreateTagHandler() {
        new ActionNoBlock("File|New File", null).perform();
        WizardOperator newFileWizard = new WizardOperator("New File");
        new JComboBoxOperator(newFileWizard).selectItem(PROJECT_NAME);
        new Node(new JTreeOperator(newFileWizard), "Web").select();
        new JListOperator(newFileWizard, 1).selectItem("Tag Handler");
        newFileWizard.next();
        JTextFieldOperator txtName = new JTextFieldOperator(newFileWizard);
        // clear text field
        txtName.setText("");
        txtName.typeText("MyTag");
        JComboBoxOperator pkg = new JComboBoxOperator(newFileWizard,1);
        pkg.clearText();
        pkg.typeText("tags");
        newFileWizard.next();
        new JButtonOperator(newFileWizard).push();
        NbDialogOperator dialog = new NbDialogOperator("Browse Files");
        new Node(new JTreeOperator(dialog),"Web Pages|WEB-INF|tlds|MyTags.tld").select();
        new JButtonOperator(dialog,"Select File").push();
        newFileWizard.finish();
        // HACK
        new Node(phelper.getSourceNode(), "tags|MyTag.java");
        // check class is opened in Editor and then close it
        EditorOperator editor = new EditorOperator("MyTag.java");
        editor.replace("// TODO: code that performs other operations in doStartTag", getTagHandlerCode());
        editor.saveDocument();
        editor.close();
        ref(Util.dumpProjectView(PROJECT_NAME));
        //compareReferenceFiles();
    }
}
