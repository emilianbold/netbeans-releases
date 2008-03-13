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

package gui.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.junit.ide.ProjectSupport;


/**
 * Test create CreateCompositeApplication
 *
 * @author  rashid@netbeans.org
 */
public class CreateCompositeApplication extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private NewProjectNameLocationStepOperator wizard_location;
    
    private String category, project, project_name;
    
    private int index;
    
    /**
     * Creates a new instance of CreateCompositeApplication
     * @param testName the name of the test
     */
    public CreateCompositeApplication(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=10000;
    }
    
    /**
     * Creates a new instance of CreateCompositeApplication
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateCompositeApplication(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=10000;
    }
    
    public void initialize(){
        category = Bundle.getStringTrimmed("org.netbeans.modules.bpel.project.wizards.Bundle","Templates/Project/SOA"); // "Service Oriented Architecture"
//        project = Bundle.getStringTrimmed("org.netbeans.modules.bpel.project.wizards.Bundle","Templates/Project/SOA/emptyBpelpro.xml"); // "Composite Application"
        project = "Composite Application"; // NOI18N
        index=1;
        
        MainWindowOperator.getDefault().maximize();
    }
    
    public void prepare(){
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.next();
        wizard_location = new NewProjectNameLocationStepOperator();
        
        String directory = System.getProperty("xtest.tmpdir")+java.io.File.separator+"createdProjects";
        log("================= Destination directory={"+directory+"}");
      //  wizard_location.txtProjectLocation().setText("");
        new EventTool().waitNoEvent(1000);
        wizard_location.txtProjectLocation().setText(directory);
        
        project_name = "CompositeApp_" + (index++);
        log("================= Project name="+project_name+"}");
        wizard_location.txtProjectName().setText("");
        new EventTool().waitNoEvent(1000);
        wizard_location.txtProjectName().typeText(project_name);
    }
    
    public ComponentOperator open(){
        wizard_location.finish();
        return null;
    }
    
    public void close(){
        ProjectSupport.closeProject(project_name);
//        new CloseAllDocumentsAction().performAPI(); //avoid issue 68671 - editors are not closed after closing project by ProjectSupport
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new CreateCompositeApplication("measureTime"));
    }
}
