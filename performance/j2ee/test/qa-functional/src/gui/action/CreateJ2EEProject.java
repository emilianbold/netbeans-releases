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

package gui.action;

import gui.Utils;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import org.netbeans.junit.ide.ProjectSupport;


/**
 * Test create projects
 *
 * @author  lmartinek@netbeans.org
 */
public class CreateJ2EEProject extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private NewProjectNameLocationStepOperator wizard_location;
    
    private String category, project, project_name;
    private boolean createSubProjects = false;
    
    /**
     * Creates a new instance of CreateJ2EEProject
     * @param testName the name of the test
     */
    public CreateJ2EEProject(String testName) {
        super(testName);
        expectedTime = 20000;
        WAIT_AFTER_OPEN=20000;
    }
    
    /**
     * Creates a new instance of CreateJ2EEProject
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateJ2EEProject(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 20000;
        WAIT_AFTER_OPEN=20000;
    }
    
    public void testCreateEnterpriseApplicationProject(){
        category = "Enterprise";
        project = "Enterprise Application";
        project_name = "MyApp";
        createSubProjects = true;
        doMeasurement();
    }
   
    public void testCreateStandaloneEnterpriseApplicationProject(){
        category = "Enterprise";
        project = "Enterprise Application";
        project_name = "MyStandaloneApp";
        createSubProjects = false;
        doMeasurement();
    }

    public void testCreateEJBModuleProject(){
        category = "Enterprise";
        project = "EJB Module";
        project_name = "MyEJBModule";
        doMeasurement();
    }
    
    public void initialize(){
    }
    
    public void prepare(){
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.next();
        wizard_location = new NewProjectNameLocationStepOperator();
        wizard_location.txtProjectLocation().setText(System.getProperty("xtest.tmpdir"));
        project_name += Utils.getTimeIndex();
        wizard_location.txtProjectName().setText(project_name);
        //project_name = wizard_location.txtProjectName().getText();
        if (project.equals("Enterprise Application")) {
            JCheckBoxOperator createEjb = new JCheckBoxOperator(wizard_location, "Ejb");
            JCheckBoxOperator createWeb = new JCheckBoxOperator(wizard_location, "Web");
            createEjb.setSelected(createSubProjects);
            createWeb.setSelected(createSubProjects);
        }
    }
    
    public ComponentOperator open(){
        wizard_location.next();
        wizard_location.finish();
        return null;
    }
    
    public void close(){
        ProjectSupport.closeProject(project_name);
        ProjectSupport.closeProject(project_name+"-EJBModule");
        ProjectSupport.closeProject(project_name+"-WebModule");
    }
    
    
}
