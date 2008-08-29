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
package org.netbeans.performance.languages.actions;


import junit.framework.Test;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.performance.utilities.CommonUtilities;

/**
 *
 * @author mkhramov@netbeans.org, mrkam@netbeans.org
 */
public class CreatePHPProject  extends org.netbeans.modules.performance.utilities.PerformanceTestCase {
    public static final String suiteName="Scripting UI Responsiveness Actions suite";
    private NewProjectNameLocationStepOperator wizard_location;
    
    public String category, project, project_name, project_type,  editor_name;
    
    public CreatePHPProject(String testName)
    {
        super(testName);        
        expectedTime = 10000;
        WAIT_AFTER_OPEN=20000;        
    }
    
    public CreatePHPProject(String testName, String performanceDataName)
    {
        super(testName,performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=20000;        
    }

    @Override
    public void initialize(){
        log("::initialize::");              
                
    }
    @Override
    public void prepare(){
        log("::prepare");
        System.out.println("::prepare");
        createProject();
    }
    
    private void createProject() {
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.next();
        wizard_location = new NewProjectNameLocationStepOperator();
        
        String directory = CommonUtilities.getTempDir() + "createdProjects";
        System.out.println(directory);
       
        wizard_location.txtProjectLocation().setText("");
                
        waitNoEvent(1000);
        wizard_location.txtProjectLocation().typeText(directory);
        System.out.println(wizard_location.txtProjectLocation().getText());
        
        project_name = project_type + "_" + System.currentTimeMillis();
        log("================= Project name="+project_name+"}");
        wizard_location.txtProjectName().setText("");
        waitNoEvent(1000);
        wizard_location.txtProjectName().typeText(project_name);
        
   
    }

    public ComponentOperator open(){
        log("::open");    
        wizard_location.finish();
        
        long oldTimeout = JemmyProperties.getCurrentTimeouts().getTimeout("ComponentOperator.WaitStateTimeout");
        JemmyProperties.getCurrentTimeouts().setTimeout("ComponentOperator.WaitStateTimeout",120000);
        System.out.println("wait wizard closed...");
        wizard_location.waitClosed();
        System.out.println("done1...");
        System.out.println("project creation dialog closed");
        waitProjectCreatingDialogClosed();
        System.out.println("done2....");

        JemmyProperties.getCurrentTimeouts().setTimeout("ComponentOperator.WaitStateTimeout",oldTimeout);        

        TopComponentOperator.findTopComponent(editor_name, 0);
        return null;
    }
    
    @Override
    public void close(){
        log("::close");

        try {
            CommonUtilities.deleteProject(project_name);
        } catch(Exception ee) {
            log("Exception during project deletion: "+ee.getMessage());
        }
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
    
    public void testCreatePhpProject() {
        category = "PHP";
        project = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.php.project.ui.wizards.Bundle", "Templates/Project/PHP/PHPProject.php");
        project_type = "PHPApplication";
        editor_name = "index.php";
        doMeasurement();        
    }
    
    public void testCreatePhpSampleProject() {
        category = "Samples|PHP";
        project = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.php.samples.Bundle", "Templates/Project/Samples/PHP/AirAlliance");
        project_type = "PHPSampleApp";
        editor_name = "index.php";        
        doMeasurement();
    }

    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.createConfiguration(CreatePHPProject.class)
            .enableModules(".*")
            .clusters(".*")
            .reuseUserDir(true)
        );    
    }
}
