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

package org.netbeans.performance.visualweb.memory;

import org.netbeans.performance.visualweb.footprint.VWPFootprintUtilities;
import org.netbeans.performance.visualweb.windows.PaletteComponentOperator;
import org.netbeans.performance.visualweb.windows.WebFormDesignerOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.PaletteOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class StyleBuilderMem extends org.netbeans.modules.performance.utilities.MemoryFootprintTestCase {
    
    private String category, project, project_name, project_type;
    private NewProjectNameLocationStepOperator wizard_location;

    
    private PaletteComponentOperator palette;
    private WebFormDesignerOperator surface;
    private String categoryName;
    private String componentName;  
    
    private PropertySheetOperator pto;
    private Property property;
    private NbDialogOperator styleDialog;
    private String componentID;    
    
    
    public StyleBuilderMem(String testName) {
        super(testName);
        repeat_memory = 1; // Perform single test pass
        
    }
    public StyleBuilderMem(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        repeat_memory = 1; // Perform single test pass
    }
    
    @Override
    public void initialize() {


            EditorOperator.closeDiscardAll();
            ProjectsTabOperator.invoke();

            category = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.wizards.Bundle", "Templates/Project/Web"); 
            project = "Web Application";
            project_type = "JSFWebProject";
            createProject();
            log("Created project named: " + project_name);
            System.out.println("Created project named: " + project_name);
            
            long oldTimeout = JemmyProperties.getCurrentTimeouts().getTimeout("ComponentOperator.WaitStateTimeout");
            JemmyProperties.getCurrentTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 120000);

            waitProjectCreatingDialogClosed();

            JemmyProperties.getCurrentTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", oldTimeout);
            log("Project Creation dialog passed");
            System.out.println("Project Creation dialog passed");
            

            try {
                surface = WebFormDesignerOperator.findWebFormDesignerOperator("Page1");
                log("Page1 page opened");
                System.out.println("Page1 page opened");                
            } catch(Exception se) {
                surface = null;
            }

    }
    @Override
    public void prepare() {
        try {
            if (surface == null) {
                System.out.println("Null surface");
                surface = WebFormDesignerOperator.findWebFormDesignerOperator("Page1");
                log("Page1 page opened");
            }

        } catch (Exception exception) {
            fail("Unable to initialize testing component surface");
        }
        System.out.println("Invoking palette");
        PaletteOperator.invoke();
        log("Components palette invoked");
        palette = new PaletteComponentOperator();
        palette.getCategoryListOperator(categoryName).selectItem(componentName); 
        
        surface.clickOnSurface(20, 20);
        //surface.clickOnSurface();
        log("Component added to surface");
        System.out.println("Component added to surface");
        
        pto =  PropertySheetOperator.invoke();
        
        surface.clickOnSurface(25, 25);
        componentID = new Property(pto,"id").getValue();
        property = new Property(pto,"style"); // NOI18N        
        
        
    }

    @Override
    public ComponentOperator open() {
      
        for(int i=0;i<100;i++) {
            System.out.println("Attempt: "+i);
            property.openEditor();
            styleDialog = new NbDialogOperator(componentID);
            styleDialog.close();              
        }
        return null;
        
    }
    
    @Override
    public void close(){
        log("::close");

        try {
            VWPFootprintUtilities.deleteProject(project_name);
        } catch(Exception ee) {
            log("Exception during project deletion: "+ee.getMessage());
        }
    }
    
    @Override
    public void shutdown() {
       super.shutdown(); 
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
        
        JTableOperator frameworkselector = new JTableOperator(wizard);
        frameworkselector.selectCell(0,0);
        wizard_location.finish();
        
    }
    
    private void waitProjectCreatingDialogClosed() {
       String dlgName = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.visualweb.project.jsf.ui.Bundle", "CAP_Opening_Projects");
       try {
           NbDialogOperator dlg = new NbDialogOperator(dlgName);
           dlg.waitClosed();
       } catch(TimeoutExpiredException tex) {
           //
       }
       
    }
    public void testMem() {
        doMeasurement();
    }
    /** Creates suite from particular test cases. You can define order of testcases here. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new StyleBuilderMem("testMem","Memory footprint test"));
        return suite;
    }    
    /* Method allowing test execution directly from the IDE. */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());

    }       

}
