/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
    
    public void testDataModelGenarationForCustomBranchingWizard() throws Exception {
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
        assertEquals(
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
    
    public void testDataModelGenarationForFileTemplateBranchingWizard() throws Exception {
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
        assertEquals(
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
        assertEquals(
                Arrays.asList(new String[] {
                    "nbproject/project.xml",
                    "src/org/example/module1/resources/Bundle.properties",
                    "src/org/example/module1/resources/layer.xml"
                }),
                Arrays.asList(cmf.getModifiedPaths()));

        cmf.run();
    }
    
    public void testDataModelGenarationForCustomSimpleWizard() throws Exception {
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
        assertEquals(
                Arrays.asList(new String[] {
                    "src/org/example/module1/DocBookVisualPanel1.form",
                    "src/org/example/module1/DocBookVisualPanel1.java",
                    "src/org/example/module1/DocBookWizardPanel1.java",
                    "src/org/example/module1/SampleAction.java"
                }),
                Arrays.asList(cmf.getCreatedPaths()));
        assertEquals("project.xml was modified",
                Arrays.asList(new String[] {"nbproject/project.xml"}),
                Arrays.asList(cmf.getModifiedPaths()));
        
        cmf.run();
    }
    
}

