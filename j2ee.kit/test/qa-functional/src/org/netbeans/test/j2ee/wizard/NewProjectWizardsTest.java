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
package org.netbeans.test.j2ee.wizard;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewJavaProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.j2ee.lib.Reporter;
import org.netbeans.test.j2ee.lib.RequiredFiles;
import org.netbeans.test.j2ee.lib.J2eeProjectSupport;
import org.netbeans.test.j2ee.lib.Utils;

/**
 * Test New File wizards in J2EE area. These tests are
 * part of J2EE Functional test suite. Each test checks
 * if all files were created (deployment descriptors,
 * directories for sources etc.) and if project node
 * is expanded after finishing New Project wizard.
 *
 * @author jungi
 * @see <a href="http://qa.netbeans.org/modules/j2ee/promo-f/testspec/j2ee-wizards-testspec.html">J2EE Wizards Test Specification</a>
 */
public class NewProjectWizardsTest extends JellyTestCase {
    private static final String CATEGORY_WEB = "Java Web";
    private static final String CATERGORY_JAVA_EE = "Java EE";
    
    private static final int EJB = 0;
    private static final int WEB = 1;
    private static final int J2EE_DEFAULT = 3;
    private static final int APP_CLIENT_DEFAULT = 4;
    
    private static String projectLocation = null;
    private String projectName;
    private String version;
    private Reporter reporter;

    public NewProjectWizardsTest(String testName) {
        this(testName, "5");
    }
    
    public NewProjectWizardsTest(String testName, String version) {
        super(testName);
        this.version = version;
    }
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        if (projectLocation == null) {
            projectLocation = getWorkDir().getParentFile().getParentFile().getCanonicalPath();
        }
        reporter = Reporter.getReporter((NbTestCase) this);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        reporter.close();
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(NewProjectWizardsTest.class);
        return suite;
    }

    /**
     * Create EJB Module which name contains spaces
     * and default project settings.
     */
    public void testDefaultNewEJBModWizard() throws Exception {
        Utils.openOutputTab();
        projectName = "def EJB Mod" + version;
        NewProjectWizardOperator wiz
                = WizardUtils.createNewProject(CATERGORY_JAVA_EE,"EJB Module");
        NewJavaProjectNameLocationStepOperator op
                = WizardUtils.setProjectNameLocation(projectName,
                projectLocation);
        WizardUtils.setJ2eeSpecVersion(op, WizardUtils.MODULE_EJB, version);
        wiz.finish();
        checkProjectStructure(EJB);
        checkProjectNodes();
    }
    
    /**
     * Create EJB Module with default project settings.
     */
    public void testNewEJBModWizard() throws Exception {
        projectName = "BadModule" + version;
        NewProjectWizardOperator wiz
                = WizardUtils.createNewProject(CATERGORY_JAVA_EE,"EJB Module");
        NewJavaProjectNameLocationStepOperator op
                = WizardUtils.setProjectNameLocation(projectName,
                projectLocation);
        WizardUtils.setJ2eeSpecVersion(op, WizardUtils.MODULE_EJB, version);
        wiz.finish();
        checkProjectStructure(EJB);
        checkProjectNodes();
    }
    
    
    /**
     * Create Enterprise Application Client project with default project
     * settings.
     */
    public void testDefaultAppClientWizard() throws Exception {
        projectName = "App client" + version;
        NewProjectWizardOperator wiz
                = WizardUtils.createNewProject(CATERGORY_JAVA_EE,"Enterprise Application Client");
        NewJavaProjectNameLocationStepOperator op
                = WizardUtils.setProjectNameLocation(projectName,
                projectLocation);
        WizardUtils.setJ2eeSpecVersion(op, WizardUtils.MODULE_CAR, version);
        wiz.finish();
        checkProjectStructure(APP_CLIENT_DEFAULT);
        checkProjectNodes();
    }
    
    /**
     * Create Web Application which name contains spaces
     * and default project settings.
     */
    public void testDefaultNewWebModWizard() throws Exception {
        projectName = "def Web app" + version;
        NewProjectWizardOperator wiz
                = WizardUtils.createNewProject(CATEGORY_WEB,"Web Application");
        NewJavaProjectNameLocationStepOperator op
                = WizardUtils.setProjectNameLocation(projectName,
                projectLocation);
        WizardUtils.setJ2eeSpecVersion(op, WizardUtils.MODULE_WAR, version);
        wiz.finish();
        checkProjectStructure(WEB);
        checkProjectNodes();
    }
    
    /**
     * Create Enterprise Application project with default project
     * settings (ejb and web module are as well ).
     */
    public void testDefaultNewJ2eeAppWizard() throws Exception {
        projectName = "def EAR app" + version;
        NewProjectWizardOperator wiz
                = WizardUtils.createNewProject(CATERGORY_JAVA_EE,"Enterprise Application");
        NewJavaProjectNameLocationStepOperator op
                = WizardUtils.setProjectNameLocation(projectName,
                projectLocation);
        WizardUtils.setJ2eeSpecVersion(op, WizardUtils.MODULE_EAR, version);
        wiz.finish();
        checkProjectStructure(J2EE_DEFAULT);
        Node root = checkProjectNodes();
        Node modules = new Node(root, "Java EE Modules");
        modules.expand();
        String[] s = modules.getChildren();
        assertEquals("Expected: \"def_EAR_app" + version + "-ejb.jar\", was: \"" + s[1]
                + "\"", "def_EAR_app" + version + "-ejb.jar", s[1]);
        assertEquals("Expected: \"def_EAR_app" + version + "-war.war\", was: \"" + s[0]
                + "\"", "def_EAR_app" + version + "-war.war", s[0]);
    }

    private void checkProjectStructure(int prjType) {
        RequiredFiles r = null;
        switch (prjType) {
            case EJB:
                r = readRF("structures/ejbProject.str");
                break;
            case WEB:
                r = readRF("structures/webProject.str");
                break;
            case J2EE_DEFAULT:
                if(version.equals("5")) {
                    r = readRF("structures/defEAR5.str");
                } else {
                    r = readRF("structures/defEAR.str");
                }
                break;
            case APP_CLIENT_DEFAULT:
               r = readRF("structures/carProject.str");
               break;
            default:
                throw new IllegalArgumentException();
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            //do nothing
        }
        String projectPath = projectLocation + File.separatorChar + projectName;
        Set<String> l = J2eeProjectSupport.getFileSet(projectPath);
        Set<String> rf = r.getRequiredFiles();
        reporter.ref("Project: " + projectPath);
        reporter.ref("Expected: " + rf);
        reporter.ref("Real: " + l);
        if ((EJB == prjType) && ("5".equals(version))) {
            Set result = getDifference(l, rf);
            if (!result.remove("src" + File.separator + "conf" + File.separator + "ejb-jar.xml")) {
                fail("Files: " + result + " are missing in project at: " + projectPath);
            }
        } else {
            assertTrue("Files: " + getDifference(l, rf) + " are missing in project at: " + projectPath, l.containsAll(rf));
        }
        rf = r.getRequiredFiles();
        reporter.ref("Project: " + projectPath);
        reporter.ref("Expected: " + rf);
        reporter.ref("Real: " + l);
        Set s = getDifference(rf, l);
        assertTrue("Files: " + s + " are new in project: " + projectPath , s.isEmpty());
    }
    
    public void closeProjects() {
        ProjectsTabOperator pto = new ProjectsTabOperator();
        pto.getProjectRootNode("def EJB Mod").performPopupAction("Close");
        pto.getProjectRootNode("def EAR app").performPopupAction("Close");
        pto.getProjectRootNode("def Web app").performPopupAction("Close");
        if(version.contains("5")) {
            pto.getProjectRootNode("App client"+version).performPopupAction("Close");
        } else {
            pto.getProjectRootNode("BadModule").performPopupAction("Close");
        }
        new EventTool().waitNoEvent(2500);
    }
    
    private RequiredFiles readRF(String fileName) {
        RequiredFiles rf = null;
        try {
            rf = new RequiredFiles(new File(getDataDir(), fileName));
        } catch (IOException ioe) {
            ioe.printStackTrace(reporter.getLogStream());
        }
        assertNotNull(rf);
        return rf;
    }
    
    private Set getDifference(Set<String> s1, Set<String> s2) {
        Set<String> result = new HashSet<String>();
        s2.removeAll(s1);
        for (Iterator<String> i = s2.iterator(); i.hasNext();) {
            String s = i.next();
            if (s.indexOf(".LCK") < 0) {
                result.add(s);
            } else {
                reporter.log("Additional file: " + s);
            }
        }
        return result;
    }
    
    private Node checkProjectNodes() {
        Node node = new ProjectsTabOperator().getProjectRootNode(projectName);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ie) {
            //do nothing
        }
        assertTrue("Project " + projectName + " is not expanded", node.isExpanded());
        return node;
    }
}
