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

package org.netbeans.performance.visualweb.setup;

import java.io.File;
import java.io.IOException;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.performance.visualweb.VWPUtilities;

import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.project.ui.test.ProjectSupport;

/**
 * Test suite that actually does not perform any test but sets up servers
 * for Visual Web Pack tests (and opens required test projects...)
 *
 * @author  mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class WebSetupTest extends JellyTestCase  {
    public static final String suiteName="UI Responsiveness Setup suite for Visual Web Pack";
    private RuntimeTabOperator rto = null;
    private String workdir;

    public WebSetupTest(String testName) {
        super(testName);
        workdir = System.getProperty("nbjunit.workdir");
        log("nbjunit.workdir = "  + workdir);
        System.out.println("nbjunit.workdir = "  + workdir);
        try {
            workdir = new File(workdir + "/../../../../../../../nbextra/data/").getCanonicalPath();           
            log("workdir = "  + workdir);
            System.out.println("workdir = "  + workdir);
        } catch (IOException ex) {
            System.err.println("Exception: "+ex);
        }        
    }

    public void testCloseWelcome() {
        CommonUtilities.closeWelcome();
    }

    public void testCloseAllDocuments() {
        CommonUtilities.closeAllDocuments();
    }

    public void testCloseMemoryToolbar() {
        CommonUtilities.closeMemoryToolbar();
    }
    
    public void testAddAppServer() {
        CommonUtilities.addApplicationServer();
    }    

    public void testAddTomcatServer() {
        CommonUtilities.addTomcatServer();
    }

    public void testOpenVisualWebProject() {
        
        String projectsDir = workdir + File.separator+ "VisualWebProject";
        Object prj=ProjectSupport.openProject(projectsDir);
        assertNotNull(prj);
        CommonUtilities.waitProjectTasksFinished();        
    }
    
    public void testOpenLargeVisualWebProject() {
        
        String projectsDir = workdir + File.separator+ "UltraLargeWA";
        Object prj=ProjectSupport.openProject(projectsDir);
        assertNotNull(prj);
        CommonUtilities.waitProjectTasksFinished();        
    }
    public void testOpenHugeVisualWebProject() {

        String projectsDir = workdir + File.separator+ "HugeApp";
        Object prj=ProjectSupport.openProject(projectsDir);
        assertNotNull(prj);
        CommonUtilities.waitProjectTasksFinished();
    }
    private void openProject(String projectName) {
        String projectsDir = workdir + File.separator+projectName;
        Object prj=ProjectSupport.openProject(projectsDir);
        assertNotNull(prj);
//        VWPUtilities.verifyAndResolveMissingWebServer(projectName, "GlassFish V2");
        CommonUtilities.waitProjectTasksFinished();        
    }
    
    public void testCloseTaskWindow() {
        CommonUtilities.closeTaskWindow();
    }
    
    public void setupAppServer() {
        rto = RuntimeTabOperator.invoke();
        try {
            VWPUtilities.getApplicationServerNode();
        } catch (TimeoutExpiredException ex) {
            //App server node not found -(
            installAppServer();
        }
        
        try {
            VWPUtilities.getApplicationServerNode();
        } catch (TimeoutExpiredException ex) {
            //App server node not found -(
            fail("Application Server Installation failed");
        }
        VWPUtilities.invokePTO();
    }
    
    private void installAppServer() {
        Node n = null;
        try {
            n = new Node(rto.tree(),"Servers"); // NOI18N
        } catch(TimeoutExpiredException e) {
            fail("Cannot select serves node");
        }
        n.select();
        n.callPopup().pushMenuNoBlock("Add Server"); // NOI18N
        processInstall();
    }
    
    private void processInstall() {
        WizardOperator wizard = new WizardOperator("Add Server Instance"); // NOI18N
        org.netbeans.jemmy.operators.JComboBoxOperator SrvTypeList;
        SrvTypeList = new JComboBoxOperator(wizard,0);
        SrvTypeList.selectItem("Sun Java System Application Server"); // NOI18N
        String ASPath = System.getProperty("com.sun.aas.installRoot");
        wizard.next();
        
        wizard = new WizardOperator("Add Server Instance"); // NOI18N
        JTextComponentOperator domainpath = null;
        try {
            domainpath = new JTextComponentOperator(wizard,0);
        } catch (TimeoutExpiredException tex) {
            fail("Cannot take a TextBox");
        }
        
        log("Actual path id: "+domainpath.getText());
        
        domainpath.setText(ASPath);
        log("Actual path id: "+domainpath.getText());
        
        new WizardOperator("Add Server Instance").next(); // NOI18N
        new JTextComponentOperator(new WizardOperator("Add Server Instance"),1).setText("adminadmin"); // NOI18N
        new WizardOperator("Add Server Instance").finish(); // NOI18N
        new EventTool().waitNoEvent(15000);
        
    }
    
    private void createTestProject(String ProjectName) {
        String category = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.wizards.Bundle", "Templates/Project/Web"); // Web
        String project = "Web Application"; // NOI18N
        
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.next();
        NewProjectNameLocationStepOperator wizard_location = new NewProjectNameLocationStepOperator();
        
        String directory = System.getProperty("xtest.tmpdir");
        
        wizard_location.txtProjectLocation().setText("");
        new EventTool().waitNoEvent(1000);
        wizard_location.txtProjectLocation().setText(directory);
        
        wizard_location.txtProjectName().setText("");
        new EventTool().waitNoEvent(1000);
        wizard_location.txtProjectName().typeText(ProjectName);
        
        wizard_location.next();
        
        JTableOperator frameworkselector = new JTableOperator(wizard);
        frameworkselector.selectCell(0,0);
        
        wizard_location.finish();
    }
    
}