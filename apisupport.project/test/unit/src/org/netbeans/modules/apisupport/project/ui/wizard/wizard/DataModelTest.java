/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard.wizard;

import java.util.Arrays;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.modules.project.uiapi.ProjectChooserFactory;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;

/**
 * Tests {@link DataModel}.
 *
 * @author Martin Krauskopf
 */
public class DataModelTest extends LayerTestBase {
    
    public DataModelTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        TestBase.initializeBuildProperties(getWorkDir());
    }
    
    public void testDataModelGenerationForCustomBranchingWizard() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        WizardDescriptor wd = new WizardDescriptor(new Panel[] {});
        wd.putProperty(ProjectChooserFactory.WIZARD_KEY_PROJECT, project);
        DataModel data = new DataModel(wd);
        
        // first panel data (Wizard Type)
        data.setBranching(true);
        data.setFileTemplateType(false);
        data.setNumberOfSteps(2);
        
        // second panel data (Name and Location)
        data.setClassNamePrefix("DocBook");
        data.setPackageName("org.example.module1");
        
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        assertEquals("created files",
                Arrays.asList(new String[] {
                    "src/org/example/module1/DocBookVisualPanel1.form",
                    "src/org/example/module1/DocBookVisualPanel1.java",
                    "src/org/example/module1/DocBookVisualPanel2.form",
                    "src/org/example/module1/DocBookVisualPanel2.java",
                    "src/org/example/module1/DocBookWizardIterator.java",
                    "src/org/example/module1/DocBookWizardPanel1.java",
                    "src/org/example/module1/DocBookWizardPanel2.java",
                }),
                Arrays.asList(cmf.getCreatedPaths()));
        assertEquals("project.xml was modified",
                Arrays.asList(new String[] {"nbproject/project.xml"}),
                Arrays.asList(cmf.getModifiedPaths()));
        
        cmf.run();
    }
    
    public void testDataModelGenerationForFileTemplateBranchingWizard() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        WizardDescriptor wd = new WizardDescriptor(new Panel[] {});
        wd.putProperty(ProjectChooserFactory.WIZARD_KEY_PROJECT, project);
        DataModel data = new DataModel(wd);
        
        // first panel data (Wizard Type)
        data.setBranching(true);
        data.setFileTemplateType(true);
        data.setNumberOfSteps(2);
        
        // second panel data (Name and Location)
        data.setClassNamePrefix("DocBook");
        data.setDisplayName("DocBook Document");
        data.setCategory("Templates/XML");
        data.setPackageName("org.example.module1");
        
        
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        assertEquals("created files",
                Arrays.asList(new String[] {
                    "src/org/example/module1/DocBookVisualPanel1.form",
                    "src/org/example/module1/DocBookVisualPanel1.java",
                    "src/org/example/module1/DocBookVisualPanel2.form",
                    "src/org/example/module1/DocBookVisualPanel2.java",
                    "src/org/example/module1/DocBookWizardIterator.java",
                    "src/org/example/module1/DocBookWizardPanel1.java",
                    "src/org/example/module1/DocBookWizardPanel2.java",
                    "src/org/example/module1/docBook.html"
                }),
                Arrays.asList(cmf.getCreatedPaths()));
        assertEquals("modified files",
                Arrays.asList(new String[] {
                    "nbproject/project.xml",
                    "src/org/example/module1/resources/Bundle.properties",
                    "src/org/example/module1/resources/layer.xml"
                }),
                Arrays.asList(cmf.getModifiedPaths()));

        cmf.run();
    }
    
    public void testDataModelGenerationForCustomSimpleWizard() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        WizardDescriptor wd = new WizardDescriptor(new Panel[] {});
        wd.putProperty(ProjectChooserFactory.WIZARD_KEY_PROJECT, project);
        DataModel data = new DataModel(wd);
        
        // first panel data (Wizard Type)
        data.setBranching(false);
        data.setFileTemplateType(false);
        data.setNumberOfSteps(1);
        
        // second panel data (Name and Location)
        data.setClassNamePrefix("DocBook");
        data.setPackageName("org.example.module1");
        
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        assertEquals("created files",
                Arrays.asList(new String[] {
                    "src/org/example/module1/DocBookVisualPanel1.form",
                    "src/org/example/module1/DocBookVisualPanel1.java",
                    "src/org/example/module1/DocBookWizardAction.java",
                    "src/org/example/module1/DocBookWizardPanel1.java",
                }),
                Arrays.asList(cmf.getCreatedPaths()));
        assertEquals("project.xml was modified",
                Arrays.asList(new String[] {"nbproject/project.xml"}),
                Arrays.asList(cmf.getModifiedPaths()));
        
        cmf.run();
    }
    
    public void testDataModelCMFUpdated() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        WizardDescriptor wd = new WizardDescriptor(new Panel[] {});
        wd.putProperty(ProjectChooserFactory.WIZARD_KEY_PROJECT, project);
        DataModel data = new DataModel(wd);
        data.setBranching(false);
        data.setFileTemplateType(false);
        data.setNumberOfSteps(1);
        data.setClassNamePrefix("X");
        data.setPackageName("x");
        assertEquals("initial files correct",
                Arrays.asList(new String[] {
                    "src/x/XVisualPanel1.form",
                    "src/x/XVisualPanel1.java",
                    "src/x/XWizardAction.java",
                    "src/x/XWizardPanel1.java",
                }),
                Arrays.asList(data.getCreatedModifiedFiles().getCreatedPaths()));
        data.setClassNamePrefix("Y");
        assertEquals("class name change takes effect",
                Arrays.asList(new String[] {
                    "src/x/YVisualPanel1.form",
                    "src/x/YVisualPanel1.java",
                    "src/x/YWizardAction.java",
                    "src/x/YWizardPanel1.java",
                }),
                Arrays.asList(data.getCreatedModifiedFiles().getCreatedPaths()));
        data.setPackageName("y");
        assertEquals("package change takes effect",
                Arrays.asList(new String[] {
                    "src/y/YVisualPanel1.form",
                    "src/y/YVisualPanel1.java",
                    "src/y/YWizardAction.java",
                    "src/y/YWizardPanel1.java",
                }),
                Arrays.asList(data.getCreatedModifiedFiles().getCreatedPaths()));
    }
    
}
