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

package org.netbeans.performance.enterprise.actions;

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.jemmy.util.Dumper;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.enterprise.MeasureEnterpriseSetupTest;
import org.netbeans.performance.enterprise.setup.EnterpriseSetup;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test create BPELmodule
 *
 * @author  rashid@netbeans.org, mrkam@netbeans.org
 */
public class CreateBPELmoduleTest extends PerformanceTestCase {
    
    private NewProjectNameLocationStepOperator wizard_location;
    
    private String category, project, project_name, project_type;
    
    private int index;
    
    /**
     * Creates a new instance of CreateBPELmodule
     * @param testName the name of the test
     */
    public CreateBPELmoduleTest(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    /**
     * Creates a new instance of CreateProject
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateBPELmoduleTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(EnterpriseSetup.class)
             .addTest(CreateBPELmoduleTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testCreateBPELModule() {
        super.measureTime();
    }

    @Override
    public void initialize(){
        category = Bundle.getStringTrimmed("org.netbeans.modules.bpel.project.Bundle", "OpenIDE-Module-Display-Category"); // "SOA"
        project = Bundle.getStringTrimmed("org.netbeans.modules.bpel.project.wizards.Bundle", "LBL_BPEL_Wizard_Title"); // "BPEL Module"
        project_type="BPELModule";
        index=1;
        
        runGC(2);
        
        MainWindowOperator.getDefault().maximize();
    }
    
    public void prepare(){
        NewProjectWizardOperator wizard;
        for(int attempt = 1; ; attempt++) {
            log("Attempt " + attempt + " to open New Project Wizard");
            new EventTool().waitNoEvent(3000);
            Timeouts old_timeouts = JemmyProperties.getCurrentTimeouts().cloneThis();
            try {                
                JemmyProperties.setCurrentTimeout("JMenuOperator.PushMenuTimeout", 150000);
                JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 150000);
                wizard = NewProjectWizardOperator.invoke();
                break;
            } catch (RuntimeException exc) {
                if (attempt < 5) {
                    log("Attempt failed with exception: " + exc);
                    exc.printStackTrace(getLog());
                    continue;
                }
                Dumper.dumpAll(getLog("dump.xml"));
                throw exc;
            } finally {
                JemmyProperties.setCurrentTimeouts(old_timeouts);
            }
        }   
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.move(0, 0);    
        new EventTool().waitNoEvent(1000);
        wizard.next();
        wizard_location = new NewProjectNameLocationStepOperator();
        
        String directory = CommonUtilities.getTempDir() + "createdProjects";
        log("================= Destination directory={"+directory+"}");
      //  wizard_location.txtProjectLocation().setText("");
        new EventTool().waitNoEvent(1000);
        wizard_location.txtProjectLocation().clearText();
        wizard_location.txtProjectLocation().typeText(directory);
        
        project_name = project_type + "_" + (index++);
        log("================= Project name="+project_name+"}");
        wizard_location.txtProjectName().clearText();
        new EventTool().waitNoEvent(1000);
        wizard_location.txtProjectName().typeText(project_name);
    }
    
    public ComponentOperator open(){
        wizard_location.finish();
        return null;
    }
    
    @Override
    public void close(){
        closeAllModal(); // This is necessary in case open failed
//        ProjectSupport.closeProject(project_name);
        runGC(1);
    }

}
