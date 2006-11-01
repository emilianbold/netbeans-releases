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
        TestBase.initializeBuildProperties(getWorkDir(), getDataDir());
    }
    
    public void testDataModelGenarationForAlwaysEnabledActions() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        WizardDescriptor wd = new WizardDescriptor(new Panel[] {});
        wd.putProperty(ProjectChooserFactory.WIZARD_KEY_PROJECT, project);
        DataModel data = new DataModel(wd);
        
        // first panel data (Action Type)
        data.setAlwaysEnabled(true);
        
        // second panel data (GUI Registration)
        data.setCategory("Actions/Tools");
        // global menu item
        data.setGlobalMenuItemEnabled(true);
        data.setGMIParentMenu("Menu/Help/Tutorials");
        data.setGMIPosition(new Position("quick-start.url", "prj-import-guide.url"));
        data.setGMISeparatorBefore(true);
        data.setGMISeparatorAfter(true);
        // global toolbar button
        data.setToolbarEnabled(true);
        data.setToolbar("Toolbars/Edit");
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
                Arrays.asList(new String[] {"src/org/example/module1/BeepAction.java", "src/org/example/module1/Bundle.properties"}),
                Arrays.asList(cmf.getCreatedPaths()));
        assertEquals(
                Arrays.asList(new String[] {"nbproject/project.xml", "src/org/example/module1/resources/layer.xml"}),
                Arrays.asList(cmf.getModifiedPaths()));
        
        cmf.run();
        
        String[] supposedContent = new String[] {
            "<filesystem>",
                    "<folder name=\"Actions\">",
                    "<folder name=\"Tools\">",
                    "<file name=\"org-example-module1-BeepAction.instance\"/>",
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
        data.setCookieClasses(new String[] {DataModel.PREDEFINED_COOKIE_CLASSES[1], DataModel.PREDEFINED_COOKIE_CLASSES[2]});
        data.setMultiSelection(false);
        
        // second panel data (GUI Registration)
        data.setCategory("Actions/Tools");
        // global menu item
        data.setGlobalMenuItemEnabled(true);
        data.setGMIParentMenu("Menu/Help/Tutorials");
        data.setGMIPosition(new Position("quick-start.url", "prj-import-guide.url"));
        data.setGMISeparatorBefore(true);
        data.setGMISeparatorAfter(true);
        // global toolbar button
        data.setToolbarEnabled(true);
        data.setToolbar("Toolbars/Edit");
        data.setToolbarPosition(new Position("org-openide-actions-FindAction.instance", null));
        // file type context menu item
        data.setFileTypeContextEnabled(true);
        data.setFTContextType("Loaders/text/x-java/Actions");
        data.setFTContextPosition(new Position(null, "OpenAction.instance"));
        data.setFTContextSeparatorBefore(false);
        data.setFTContextSeparatorAfter(true);
        // editor context menu item
        data.setEditorContextEnabled(true);
        data.setEdContextType("Editors/text/x-java/Popup");
        data.setEdContextPosition(new Position(null, "generate-goto-popup"));
        data.setEdContextSeparatorBefore(false);
        data.setEdContextSeparatorAfter(true);
        
        // third panel data (Name, Icon, and Location)
        data.setClassName("BeepAction");
        data.setDisplayName("Beep");
        data.setPackageName("org.example.module1");
        
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        assertEquals("new files",
                Arrays.asList(new String[] {"src/org/example/module1/BeepAction.java", "src/org/example/module1/Bundle.properties"}),
                Arrays.asList(cmf.getCreatedPaths()));
        assertEquals("modified files",
                Arrays.asList(new String[] {"nbproject/project.xml", "src/org/example/module1/resources/layer.xml"}),
                Arrays.asList(cmf.getModifiedPaths()));

        cmf.run();
        
        String[] supposedContent = new String[] {
            "<filesystem>",
                    "<folder name=\"Actions\">",
                    "<folder name=\"Tools\">",
                    "<file name=\"org-example-module1-BeepAction.instance\"/>",
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

