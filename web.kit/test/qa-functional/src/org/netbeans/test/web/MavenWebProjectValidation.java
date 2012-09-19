/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.test.web;

import java.awt.Component;
import java.io.IOException;
import javax.swing.JTextField;
import junit.framework.Test;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author Jindrich Sedek
 */
public class MavenWebProjectValidation extends WebProjectValidation {

    @SuppressWarnings("hiding")
    public static final String[] TESTS = {
        "testNewMavenWebProject",
        "testNewJSP", "testNewJSP2", "testNewServlet", "testNewServlet2",
        "testCleanAndBuildProject", "testRunProject", "testRunJSP", "testViewServlet",
        "testRunServlet", "testCreateTLD", "testCreateTagHandler",
        "testRunTag", "testNewHTML", "testRunHTML",
        "testNewSegment", "testNewDocument",
        "testFinish"
    };

    public MavenWebProjectValidation(String name) {
        super(name);
        PROJECT_NAME = "WebMavenProject";
    }

    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, MavenWebProjectValidation.class, TESTS);
    }

    @Override
    protected String getEEVersion() {
        return JAVA_EE_5;
    }

    public void testNewMavenWebProject() throws IOException {
        NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
        projectWizard.selectCategory("Maven");
        projectWizard.selectProject("Web Application");
        projectWizard.next();
        WizardOperator mavenWebAppWizardOperator = new WizardOperator(projectWizard.getTitle());
        Component pnComp = new JLabelOperator(mavenWebAppWizardOperator, "Project Name").getLabelFor();
        JTextFieldOperator projectName = new JTextFieldOperator((JTextField) pnComp);
        projectName.setText(PROJECT_NAME);

        Component plComp = new JLabelOperator(mavenWebAppWizardOperator, "Project Location").getLabelFor();
        JTextFieldOperator projectLocation = new JTextFieldOperator((JTextField) plComp);
        projectLocation.setText(PROJECT_LOCATION);
        mavenWebAppWizardOperator.next();
        NewWebProjectServerSettingsStepOperator serverStep = new NewWebProjectServerSettingsStepOperator();
        // uncomment when bug 204278 fixed
        //serverStep.selectJavaEEVersion(getEEVersion());
        if (JAVA_EE_5.equals(getEEVersion())) {
            serverStep.cboJavaEEVersion().selectItem("1.5");
        }
        serverStep.finish();
        // need to increase time to wait for project node
        long oldTimeout = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 120000);
        try {
            verifyWebPagesNode("index.jsp");
        } finally {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", oldTimeout);
        }
        waitScanFinished();
    }

    @Override
    public void testCleanAndBuildProject() {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        Util.cleanStatusBar();
        new Action(null, "Clean and Build").perform(rootNode);
        waitBuildSuccessful();
    }

    @Override
    public void testRunProject() {
        initDisplayer();
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        new Node(rootNode, "Web Pages|index.jsp").performPopupAction("Open");
        EditorOperator editor = new EditorOperator("index.jsp");
        editor.replace("<title>JSP Page</title>", "<title>SampleProject Index Page</title>");
        editor.insert("Running Project\n", 12, 1);
        new Action(null, "Run").perform(rootNode);
        waitBuildSuccessful();
        assertDisplayerContent("<title>SampleProject Index Page</title>");
        editor.deleteLine(12);
        editor.save();
        EditorOperator.closeDiscardAll();
    }

    @Override
    public void waitBuildSuccessful() {
        OutputTabOperator console = new OutputTabOperator(PROJECT_NAME);
        console.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 180000);
        console.waitText("BUILD SUCCESS");
    }
}
