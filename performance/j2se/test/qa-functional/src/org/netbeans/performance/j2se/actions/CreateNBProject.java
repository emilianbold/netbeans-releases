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

package org.netbeans.performance.j2se.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Test create projects
 *
 * @author  mmirilovic@netbeans.org
 */
public class CreateNBProject extends PerformanceTestCase {
    
    private NewProjectNameLocationStepOperator wizard_location;
    
    private String category, project, project_name, project_type;
    
    private int index;
    
    public static final String suiteName="UI Responsiveness J2SE Actions";    
    
    /**
     * Creates a new instance of CreateNBProject
     * @param testName the name of the test
     */
    public CreateNBProject(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=10000;
    }
    
    /**
     * Creates a new instance of CreateNBProject
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateNBProject(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=10000;
    }
    
    public void testCreateModuleProject(){
        category = Bundle.getStringTrimmed("org.netbeans.modules.apisupport.project.ui.wizard.Bundle","Templates/Project/APISupport"); //"NetBeans Plug-in Modules"
        project = Bundle.getStringTrimmed("org.netbeans.modules.apisupport.project.ui.wizard.Bundle","Templates/Project/APISupport/emptyModule"); //"Module Project"
        project_type="moduleProject";
        index=1;
        doMeasurement();
    }

    public void testCreateModuleSuiteProject(){
        category = Bundle.getStringTrimmed("org.netbeans.modules.apisupport.project.ui.wizard.Bundle","Templates/Project/APISupport"); //"NetBeans Plug-in Modules"
        project = Bundle.getStringTrimmed("org.netbeans.modules.apisupport.project.ui.wizard.Bundle","Templates/Project/APISupport/emptySuite"); //"Module Suite Project"
        project_type="moduleSuiteProject";
        index=1;
        doMeasurement();
    }

    @Override
    public void initialize(){
    }
    
    public void prepare(){
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.next();
        wizard_location = new NewProjectNameLocationStepOperator();
        
        String directory = System.getProperty("xtest.tmpdir")+"/"+"createdProjects";
        log("================= Destination directory={"+directory+"}");
        wizard_location.txtProjectLocation().setText("");
        new EventTool().waitNoEvent(1000);
        wizard_location.txtProjectLocation().setText(directory);
        
        project_name = project_type + "_" + (index++);
        log("================= Project name="+project_name+"}");
        wizard_location.txtProjectName().setText("");
        new EventTool().waitNoEvent(1000);
        wizard_location.txtProjectName().typeText(project_name);
    }
    
    public ComponentOperator open(){
        if(project_type.equalsIgnoreCase("moduleProject")){
            wizard_location.next();
        }
        wizard_location.finish();
//        ProjectSupport.waitScanFinished();
        new EventTool().waitNoEvent(1000);
        return null;
    }
/*
    public void testInitGC() {
        org.netbeans.junit.Log.enableInstances(Logger.getLogger("TIMER"),null, Level.FINEST);
    }

    public void testGC() {
        org.netbeans.junit.Log.assertInstances("GC:"); 
    }
*/
    @Override
    public void close(){
    }
    
    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new CreateNBProject("testCreateModuleProject"));
    }
}
