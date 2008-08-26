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

package org.netbeans.performance.visualweb.actions;

import org.netbeans.performance.visualweb.footprint.VWPFootprintUtilities;
import org.netbeans.performance.visualweb.windows.WebFormDesignerOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTableOperator;

/**
 * Test create Web Pack projects
 *
 * @author  mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class CreateWebPackProjectSBS extends org.netbeans.modules.performance.utilities.PerformanceTestCase {
    
    private NewProjectNameLocationStepOperator wizard_location;
    
    private String category, project, project_name, project_type;
    
    
    public static final String suiteName="UI Responsiveness VisualWeb Actions suite";
    /**
     * Creates a new instance of CreateWebPackProject
     * @param testName the name of the test
     */
    public CreateWebPackProjectSBS(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=20000;
    }
    
    /**
     * Creates a new instance of CreateWebPackProject
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateWebPackProjectSBS(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=20000;
    }
    
    public void testCreateWebPackProject() {
        category = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.wizards.Bundle", "Templates/Project/Web"); // Web
        project = "Web Application";
        project_type="JSFWebProject";
        
        doMeasurement();
    }
    
    public void initialize(){
        log("::initialize::");
        PerformanceCounters.initPerformanceCounters(this);
    }
    
    public void prepare(){
        log("::prepare");
        createProject();
    }
    
    private void createProject() {
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.next();
        wizard_location = new NewProjectNameLocationStepOperator();
        
        String directory = System.getProperty("nbjunit.workdir")+ java.io.File.separator + "createdProjects";
        log("================= Destination directory={"+directory+"}");
        wizard_location.txtProjectLocation().setText("");
        waitNoEvent(1000);
        wizard_location.txtProjectLocation().setText(directory);
        
        project_name = project_type + "_" + System.currentTimeMillis();
        log("================= Project name="+project_name+"}");
        wizard_location.txtProjectName().setText("");
        waitNoEvent(1000);
        wizard_location.txtProjectName().typeText(project_name);
        
        wizard_location.next();
        wizard_location.next();
        
        JTableOperator frameworkselector = new JTableOperator(wizard);
        frameworkselector.selectCell(0,0);
        
        
        
    }
    
    public ComponentOperator open(){
        log("::open");
        PerformanceCounters.addPerformanceCounter("Wait Wizard closed");        
        wizard_location.finish();
        
        long oldTimeout = JemmyProperties.getCurrentTimeouts().getTimeout("ComponentOperator.WaitStateTimeout");
        JemmyProperties.getCurrentTimeouts().setTimeout("ComponentOperator.WaitStateTimeout",120000);        
        wizard_location.waitClosed();
        PerformanceCounters.endPerformanceCounter("Wait Wizard closed");
        
        PerformanceCounters.addPerformanceCounter("Wait Project Creation Box");
        waitProjectCreatingDialogClosed();
        PerformanceCounters.endPerformanceCounter("Wait Project Creation Box");
        
        JemmyProperties.getCurrentTimeouts().setTimeout("ComponentOperator.WaitStateTimeout",oldTimeout);        

        PerformanceCounters.addPerformanceCounter("Wait document");
        WebFormDesignerOperator.findWebFormDesignerOperator("Page1");
        
        PerformanceCounters.endPerformanceCounter("Wait document");
        return null;
    }
    
    public void close(){
        log("::close");
        PerformanceCounters.reportPerformanceCounters();
        VWPFootprintUtilities.deleteProject(project_name);
    }
    private void waitProjectCreatingDialogClosed()
    {
       String dlgName = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.visualweb.project.jsf.ui.Bundle", "CAP_Opening_Projects");
       try {
           NbDialogOperator dlg = new NbDialogOperator(dlgName);
           dlg.waitClosed();
       } catch(TimeoutExpiredException tex) {
           //
       }
       
    }
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new CreateWebPackProject("testCreateWebPackProject"));
    }
    
}
