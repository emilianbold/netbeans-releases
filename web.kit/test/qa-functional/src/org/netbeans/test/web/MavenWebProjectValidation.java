/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.test.web;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import javax.swing.JTextField;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.DialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.WindowOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jindrich Sedek
 */
public class MavenWebProjectValidation extends WebProjectValidation {

    static {
        PROJECT_NAME = "WebMavenProject";
        PROJECT_LOCATION = new MavenWebProjectValidation().getWorkDirPath();
    }

    public MavenWebProjectValidation(String name) {
        super(name);
    }

    public MavenWebProjectValidation() {
        super("MavenWebProjectValidation");
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(MavenWebProjectValidation.class);
        conf = addServerTests(J2eeTestCase.Server.TOMCAT, conf,
              "testPreconditions", "testNewMavenWebProject", "testRegisterTomcat",
              "testNewJSP", "testNewJSP2", "testNewServlet", "testNewServlet2",
              "testCleanAndBuildProject", "testRunProject", "testRunJSP", "testViewServlet",
              "testRunServlet","testCreateTLD", "testCreateTagHandler",
              "testRunTag","testNewHTML", "testRunHTML",
              "testNewSegment", "testNewDocument",
              "testJSPNavigator", "testHTMLNavigator",
              "testStopServer", "testStartServer", "testBrowserSettings", "testFinish"
               /*"testJSPNavigator", "testHTMLNavigator" */);
        conf = conf.enableModules(".*").clusters(".*");
        return NbModuleSuite.create(conf);
    }

    public void testNewMavenWebProject() throws IOException {
        installJemmyQueue();
        FileUtil.createFolder(new File(PROJECT_LOCATION));
        NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
        projectWizard.selectCategory("Maven");
        projectWizard.selectProject("Maven Web Application");
        projectWizard.next();
        WizardOperator mavenWebAppWizardOperator = new WizardOperator(projectWizard.getTitle());
        Component pnComp = new JLabelOperator(mavenWebAppWizardOperator, "Project Name").getLabelFor();
        JTextFieldOperator projectName = new JTextFieldOperator((JTextField)pnComp);
        projectName.setText("");
        projectName.typeText(PROJECT_NAME);


        Component plComp = new JLabelOperator(mavenWebAppWizardOperator, "Project Location").getLabelFor();
        JTextFieldOperator projectLocation = new JTextFieldOperator((JTextField)plComp);
        projectLocation.setText("");
        projectLocation.typeText(PROJECT_LOCATION);
        mavenWebAppWizardOperator.finish();

        // wait for project creation
        sleep(5000);
        Window wnd = DialogOperator.findWindow(new ComponentChooser() {

            public boolean checkComponent(Component comp) {
                if (comp instanceof Dialog){
                    Dialog jd = (Dialog) comp;
                    String title = jd.getTitle();
                    if ((title != null) && title.contains("Message")){
                        return true;
                    }
                }
                return false;
            }

            public String getDescription() {
                return "No Maven installed message";
            }
        });
        if (wnd != null){
            new WindowOperator(wnd).close();
        }
        sleep(5000);
        ProjectSupport.waitScanFinished();
        verifyWebPagesNode("index.jsp");
        verifyWebPagesNode("WEB-INF|web.xml");
    }

    @Override
    public void testCleanAndBuildProject() {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        Util.cleanStatusBar();
        new Action(null, "Clean and Build").perform(rootNode);
        waitBuildSuccessful();
    }

    @Override
    public void testRunProject(){
        initDisplayer();
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        new Node(rootNode,"Web Pages|index.jsp").performPopupAction("Open");
        EditorOperator editor = new EditorOperator("index.jsp");
        editor.replace("<title>JSP Page</title>",
                "<title>SampleProject Index Page</title>");
        editor.insert("Running Project\n",12,1);
        new ActionNoBlock(null,"Run").perform(rootNode);
        DialogOperator dio = new DialogOperator("Select deployment server");
        JComboBoxOperator op = new JComboBoxOperator(dio);
        op.selectItem(1);
        JButtonOperator ok = new JButtonOperator(dio, "OK");
        ok.push();
        waitBuildSuccessful();
        assertDisplayerContent("<title>SampleProject Index Page</title>");
        editor.deleteLine(12);
        editor.save();
        EditorOperator.closeDiscardAll();
    }

    @Override
    public void waitBuildSuccessful() {
        waitBuildSuccessfulInActualTab();
    }

}
