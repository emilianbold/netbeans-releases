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

package org.netbeans.modules.apisupport.project.ui.wizard.action;

import java.io.File;
import java.util.Arrays;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.CreatedModifiedFilesTest;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.modules.apisupport.project.ui.wizard.action.DataModel.Position;
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
    
    public void testDataModelGenarationForAlwaysEnabledActions() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        WizardDescriptor wd = new WizardDescriptor(new Panel[] {});
        wd.putProperty(ProjectChooserFactory.WIZARD_KEY_PROJECT, project);
        DataModel data = new DataModel(wd);
        
        // first panel data (Action Type)
        data.setAlwaysEnabled(true);
        
        // second panel data (GUI Registration)
        data.setCategory("Tools");
        // global menu item
        data.setGlobalMenuItemEnabled(true);
        data.setGMIParentMenu(new String[] {"Help", "Tutorials"});
        data.setGMIPosition(new Position("quick-start.url", "prj-import-guide.url"));
        data.setGMISeparatorBefore(true);
        data.setGMISeparatorAfter(true);
        // global toolbar button
        data.setToolbarEnabled(true);
        data.setToolbar("Edit");
        data.setToolbarPosition(new Position("org-openide-actions-FindAction.instance", null));
        // global keyboard shortcut
        data.setKeyboardShortcutEnabled(true);
        data.setKeyStroke("DA-B");
        
        // third panel data (Name, Icon, and Location)
        data.setClassName("BeepAction");
        data.setDisplayName("Beep");
        data.setPackageName("org.example.module1");
        
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        assertEquals(
                Arrays.asList(new String[] {"src/org/example/module1/BeepAction.java"}),
                Arrays.asList(cmf.getCreatedPaths()));
        assertEquals(
                Arrays.asList(new String[] {"nbproject/project.xml", "src/org/example/module1/resources/layer.xml"}),
                Arrays.asList(cmf.getModifiedPaths()));
        
        cmf.run();
        
        String[] supposedContent = new String[] {
            "<filesystem>",
                    "<folder name=\"Actions\">",
                    "<folder name=\"Tools\">",
                    "<file name=\"org-example-module1-BeepAction.instance\">",
                    "<attr name=\"instanceClass\" stringvalue=\"org.example.module1.BeepAction\"/>",
                    "</file>",
                    "</folder>",
                    "</folder>",
                    "<folder name=\"Menu\">",
                    "<folder name=\"Help\">",
                    "<folder name=\"Tutorials\">",
                    "<attr name=\"quick-start.url/org-example-module1-separatorBefore.instance\" boolvalue=\"true\"/>",
                    "<file name=\"org-example-module1-separatorBefore.instance\">",
                    "<attr name=\"instanceClass\" stringvalue=\"javax.swing.JSeparator\"/>",
                    "</file>",
                    "<attr name=\"org-example-module1-separatorBefore.instance/org-example-module1-BeepAction.shadow\" boolvalue=\"true\"/>",
                    "<file name=\"org-example-module1-BeepAction.shadow\">",
                    "<attr name=\"originalFile\" stringvalue=\"Actions/Tools/org-example-module1-BeepAction.instance\"/>",
                    "</file>",
                    "<attr name=\"org-example-module1-BeepAction.shadow/org-example-module1-separatorAfter.instance\" boolvalue=\"true\"/>",
                    "<file name=\"org-example-module1-separatorAfter.instance\">",
                    "<attr name=\"instanceClass\" stringvalue=\"javax.swing.JSeparator\"/>",
                    "</file>",
                    "<attr name=\"org-example-module1-separatorAfter.instance/prj-import-guide.url\" boolvalue=\"true\"/>",
                    "</folder>",
                    "</folder>",
                    "</folder>",
                    "<folder name=\"Shortcuts\">",
                    "<file name=\"DA-B.shadow\">",
                    "<attr name=\"originalFile\" stringvalue=\"Actions/Tools/org-example-module1-BeepAction.instance\"/>",
                    "</file>",
                    "</folder>",
                    "<folder name=\"Toolbars\">",
                    "<folder name=\"Edit\">",
                    "<attr name=\"org-openide-actions-FindAction.instance/org-example-module1-BeepAction.shadow\" boolvalue=\"true\"/>",
                    "<file name=\"org-example-module1-BeepAction.shadow\">",
                    "<attr name=\"originalFile\" stringvalue=\"Actions/Tools/org-example-module1-BeepAction.instance\"/>",
                    "</file>",
                    "</folder>",
                    "</folder>",
                    "</filesystem>"
        };
        
        CreatedModifiedFilesTest.assertLayerContent(supposedContent,
                new File(getWorkDir(), "module1/src/org/example/module1/resources/layer.xml"));
    }
    
    public void testDataModelGenarationForConditionallyEnabledActions() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        WizardDescriptor wd = new WizardDescriptor(new Panel[] {});
        wd.putProperty(ProjectChooserFactory.WIZARD_KEY_PROJECT, project);
        DataModel data = new DataModel(wd);
        
        // first panel data (Action Type)
        data.setAlwaysEnabled(false);
        data.setCookieClasses(new String[] {"OpenCookie", "EditCookie"});
        data.setMultiSelection(false);
        
        // second panel data (GUI Registration)
        data.setCategory("Tools");
        // global menu item
        data.setGlobalMenuItemEnabled(true);
        data.setGMIParentMenu(new String[] {"Help", "Tutorials"});
        data.setGMIPosition(new Position("quick-start.url", "prj-import-guide.url"));
        data.setGMISeparatorBefore(true);
        data.setGMISeparatorAfter(true);
        // global toolbar button
        data.setToolbarEnabled(true);
        data.setToolbar("Edit");
        data.setToolbarPosition(new Position("org-openide-actions-FindAction.instance", null));
        // file type context menu item
        data.setFileTypeContextEnabled(true);
        data.setFTContextType("text/x-java");
        data.setFTContextPosition(new Position(null, "OpenAction.instance"));
        data.setFTContextSeparatorBefore(false);
        data.setFTContextSeparatorAfter(true);
        // editor context menu item
        data.setEditorContextEnabled(true);
        data.setEdContextType("text/x-java");
        data.setEdContextPosition(new Position(null, "generate-goto-popup"));
        data.setEdContextSeparatorBefore(false);
        data.setEdContextSeparatorAfter(true);
        
        // third panel data (Name, Icon, and Location)
        data.setClassName("BeepAction");
        data.setDisplayName("Beep");
        data.setPackageName("org.example.module1");
        
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        assertEquals(
                Arrays.asList(new String[] {"src/org/example/module1/BeepAction.java"}),
                Arrays.asList(cmf.getCreatedPaths()));
        assertEquals(
                Arrays.asList(new String[] {"nbproject/project.xml", "src/org/example/module1/resources/layer.xml"}),
                Arrays.asList(cmf.getModifiedPaths()));

        cmf.run();
        
        String[] supposedContent = new String[] {
            "<filesystem>",
                    "<folder name=\"Actions\">",
                    "<folder name=\"Tools\">",
                    "<file name=\"org-example-module1-BeepAction.instance\">",
                    "<attr name=\"instanceClass\" stringvalue=\"org.example.module1.BeepAction\"/>",
                    "</file>",
                    "</folder>",
                    "</folder>",
                    "<folder name=\"Editors\">",
                    "<folder name=\"text\">",
                    "<folder name=\"x-java\">",
                    "<folder name=\"Popup\">",
                    "<file name=\"org-example-module1-BeepAction.shadow\">",
                    "<attr name=\"originalFile\" stringvalue=\"Actions/Tools/org-example-module1-BeepAction.instance\"/>",
                    "</file>",
                    "<attr name=\"org-example-module1-BeepAction.shadow/org-example-module1-separatorAfter.instance\" boolvalue=\"true\"/>",
                    "<file name=\"org-example-module1-separatorAfter.instance\">",
                    "<attr name=\"instanceClass\" stringvalue=\"javax.swing.JSeparator\"/>",
                    "</file>",
                    "<attr name=\"org-example-module1-separatorAfter.instance/generate-goto-popup\" boolvalue=\"true\"/>",
                    "</folder>",
                    "</folder>",
                    "</folder>",
                    "</folder>",
                    "<folder name=\"Loaders\">",
                    "<folder name=\"text\">",
                    "<folder name=\"x-java\">",
                    "<folder name=\"Actions\">",
                    "<file name=\"org-example-module1-BeepAction.shadow\">",
                    "<attr name=\"originalFile\" stringvalue=\"Actions/Tools/org-example-module1-BeepAction.instance\"/>",
                    "</file>",
                    "<attr name=\"org-example-module1-BeepAction.shadow/org-example-module1-separatorAfter.instance\" boolvalue=\"true\"/>",
                    "<file name=\"org-example-module1-separatorAfter.instance\">",
                    "<attr name=\"instanceClass\" stringvalue=\"javax.swing.JSeparator\"/>",
                    "</file>",
                    "<attr name=\"org-example-module1-separatorAfter.instance/OpenAction.instance\" boolvalue=\"true\"/>",
                    "</folder>",
                    "</folder>",
                    "</folder>",
                    "</folder>",
                    "<folder name=\"Menu\">",
                    "<folder name=\"Help\">",
                    "<folder name=\"Tutorials\">",
                    "<attr name=\"quick-start.url/org-example-module1-separatorBefore.instance\" boolvalue=\"true\"/>",
                    "<file name=\"org-example-module1-separatorBefore.instance\">",
                    "<attr name=\"instanceClass\" stringvalue=\"javax.swing.JSeparator\"/>",
                    "</file>",
                    "<attr name=\"org-example-module1-separatorBefore.instance/org-example-module1-BeepAction.shadow\" boolvalue=\"true\"/>",
                    "<file name=\"org-example-module1-BeepAction.shadow\">",
                    "<attr name=\"originalFile\" stringvalue=\"Actions/Tools/org-example-module1-BeepAction.instance\"/>",
                    "</file>",
                    "<attr name=\"org-example-module1-BeepAction.shadow/org-example-module1-separatorAfter.instance\" boolvalue=\"true\"/>",
                    "<file name=\"org-example-module1-separatorAfter.instance\">",
                    "<attr name=\"instanceClass\" stringvalue=\"javax.swing.JSeparator\"/>",
                    "</file>",
                    "<attr name=\"org-example-module1-separatorAfter.instance/prj-import-guide.url\" boolvalue=\"true\"/>",
                    "</folder>",
                    "</folder>",
                    "</folder>",
                    "<folder name=\"Toolbars\">",
                    "<folder name=\"Edit\">",
                    "<attr name=\"org-openide-actions-FindAction.instance/org-example-module1-BeepAction.shadow\" boolvalue=\"true\"/>",
                    "<file name=\"org-example-module1-BeepAction.shadow\">",
                    "<attr name=\"originalFile\" stringvalue=\"Actions/Tools/org-example-module1-BeepAction.instance\"/>",
                    "</file>",
                    "</folder>",
                    "</folder>",
                    "</filesystem>"
        };
        
        CreatedModifiedFilesTest.assertLayerContent(supposedContent,
                new File(getWorkDir(), "module1/src/org/example/module1/resources/layer.xml"));
    }
    
}

