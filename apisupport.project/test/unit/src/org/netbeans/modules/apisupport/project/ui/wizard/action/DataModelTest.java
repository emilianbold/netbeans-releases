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

package org.netbeans.modules.apisupport.project.ui.wizard.action;

import java.io.File;
import java.util.Arrays;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.CreatedModifiedFilesTest;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.apisupport.project.ui.wizard.action.DataModel.Position;
import org.netbeans.modules.project.uiapi.ProjectChooserFactory;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

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

//    XXX: failing test, fix or delete
//    public void testDataModelGenarationForAlwaysEnabledActions() throws Exception {
//        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
//        LayerUtils.LayerHandle handle = LayerUtils.layerForProject(project);
//        FileObject root = handle.layer(true).getRoot();
//        FileUtil.createData(root, "Menu/Help/Tutorials/quick-start.url").setAttribute("position", 100);
//        FileUtil.createData(root, "Menu/Help/Tutorials/prj-import-guide.url").setAttribute("position", 200);
//        FileUtil.createData(root, "Toolbars/Edit/org-openide-actions-FindAction.instance").setAttribute("position", 600);
//
//        WizardDescriptor wd = new WizardDescriptor() {};
//        wd.putProperty(ProjectChooserFactory.WIZARD_KEY_PROJECT, project);
//        DataModel data = new DataModel(wd);
//
//        // first panel data (Action Type)
//        data.setAlwaysEnabled(true);
//
//        // second panel data (GUI Registration)
//        data.setCategory("Actions/Tools");
//        // global menu item
//        data.setGlobalMenuItemEnabled(true);
//        data.setGMIParentMenu("Menu/Help/Tutorials");
//        data.setGMIPosition(new Position("quick-start.url", "prj-import-guide.url"));
//        data.setGMISeparatorBefore(true);
//        data.setGMISeparatorAfter(true);
//        // global toolbar button
//        data.setToolbarEnabled(true);
//        data.setToolbar("Toolbars/Edit");
//        data.setToolbarPosition(new Position("org-openide-actions-FindAction.instance", null));
//        // global keyboard shortcut
//        data.setKeyboardShortcutEnabled(true);
//        data.setKeyStroke("DA-B");
//
//        // third panel data (Name, Icon, and Location)
//        data.setClassName("BeepAction");
//        data.setDisplayName("Beep");
//        data.setPackageName("org.example.module1");
//
//        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
//        assertEquals(
//                Arrays.asList(new String[] {"src/org/example/module1/BeepAction.java", "src/org/example/module1/Bundle.properties"}),
//                Arrays.asList(cmf.getCreatedPaths()));
//        assertEquals(
//                Arrays.asList(new String[] {"nbproject/project.xml", "src/org/example/module1/resources/layer.xml"}),
//                Arrays.asList(cmf.getModifiedPaths()));
//
//        cmf.run();
//
//        String[] supposedContent = new String[] {
//            "<filesystem>",
//                    "<folder name=\"Actions\">",
//                    "<folder name=\"Tools\">",
//                    "<file name=\"org-example-module1-BeepAction.instance\"/>",
//                    "</folder>",
//                    "</folder>",
//                    "<folder name=\"Menu\">",
//                    "<folder name=\"Help\">",
//                    "<folder name=\"Tutorials\">",
//                    "<file name=\"org-example-module1-BeepAction.shadow\">",
//                    "<attr name=\"originalFile\" stringvalue=\"Actions/Tools/org-example-module1-BeepAction.instance\"/>",
//                    "<attr name=\"position\" intvalue=\"150\"/>",
//                    "</file>",
//                    "<file name=\"org-example-module1-separatorAfter.instance\">",
//                    "<attr name=\"instanceClass\" stringvalue=\"javax.swing.JSeparator\"/>",
//                    "<attr name=\"position\" intvalue=\"175\"/>",
//                    "</file>",
//                    "<file name=\"org-example-module1-separatorBefore.instance\">",
//                    "<attr name=\"instanceClass\" stringvalue=\"javax.swing.JSeparator\"/>",
//                    "<attr name=\"position\" intvalue=\"125\"/>",
//                    "</file>",
//                    "<file name=\"prj-import-guide.url\">",
//                    "<attr name=\"position\" intvalue=\"200\"/>",
//                    "</file>",
//                    "<file name=\"quick-start.url\">",
//                    "<attr name=\"position\" intvalue=\"100\"/>",
//                    "</file>",
//                    "</folder>",
//                    "</folder>",
//                    "</folder>",
//                    "<folder name=\"Shortcuts\">",
//                    "<file name=\"DA-B.shadow\">",
//                    "<attr name=\"originalFile\" stringvalue=\"Actions/Tools/org-example-module1-BeepAction.instance\"/>",
//                    "</file>",
//                    "</folder>",
//                    "<folder name=\"Toolbars\">",
//                    "<folder name=\"Edit\">",
//                    "<file name=\"org-example-module1-BeepAction.shadow\">",
//                    "<attr name=\"originalFile\" stringvalue=\"Actions/Tools/org-example-module1-BeepAction.instance\"/>",
//                    "<attr name=\"position\" intvalue=\"700\"/>",
//                    "</file>",
//                    "<file name=\"org-openide-actions-FindAction.instance\">",
//                    "<attr name=\"position\" intvalue=\"600\"/>",
//                    "</file>",
//                    "</folder>",
//                    "</folder>",
//                    "</filesystem>"
//        };
//
//        CreatedModifiedFilesTest.assertLayerContent(supposedContent,
//                new File(getWorkDir(), "module1/src/org/example/module1/resources/layer.xml"));
//    }

//    XXX: failing test, fix or delete
//    public void testDataModelGenarationForConditionallyEnabledActions() throws Exception {
//        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
//        WizardDescriptor wd = new WizardDescriptor() {};
//        wd.putProperty(ProjectChooserFactory.WIZARD_KEY_PROJECT, project);
//        DataModel data = new DataModel(wd);
//
//        // first panel data (Action Type)
//        data.setAlwaysEnabled(false);
//        data.setCookieClasses(new String[] {DataModel.PREDEFINED_COOKIE_CLASSES[1], DataModel.PREDEFINED_COOKIE_CLASSES[2]});
//        data.setMultiSelection(false);
//
//        // second panel data (GUI Registration)
//        data.setCategory("Actions/Tools");
//        // global menu item
//        data.setGlobalMenuItemEnabled(true);
//        data.setGMIParentMenu("Menu/Help/Tutorials");
//        data.setGMIPosition(new Position("quick-start.url", "prj-import-guide.url"));
//        data.setGMISeparatorBefore(true);
//        data.setGMISeparatorAfter(true);
//        // global toolbar button
//        data.setToolbarEnabled(true);
//        data.setToolbar("Toolbars/Edit");
//        data.setToolbarPosition(new Position("org-openide-actions-FindAction.instance", null));
//        // file type context menu item
//        data.setFileTypeContextEnabled(true);
//        data.setFTContextType("Loaders/text/x-java/Actions");
//        data.setFTContextPosition(new Position(null, "OpenAction.instance"));
//        data.setFTContextSeparatorBefore(false);
//        data.setFTContextSeparatorAfter(true);
//        // editor context menu item
//        data.setEditorContextEnabled(true);
//        data.setEdContextType("Editors/text/x-java/Popup");
//        data.setEdContextPosition(new Position(null, "generate-goto-popup"));
//        data.setEdContextSeparatorBefore(false);
//        data.setEdContextSeparatorAfter(true);
//
//        // third panel data (Name, Icon, and Location)
//        data.setClassName("BeepAction");
//        data.setDisplayName("Beep");
//        data.setPackageName("org.example.module1");
//
//        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
//        assertEquals("new files",
//                Arrays.asList(new String[] {"src/org/example/module1/BeepAction.java", "src/org/example/module1/Bundle.properties"}),
//                Arrays.asList(cmf.getCreatedPaths()));
//        assertEquals("modified files",
//                Arrays.asList(new String[] {"nbproject/project.xml", "src/org/example/module1/resources/layer.xml"}),
//                Arrays.asList(cmf.getModifiedPaths()));
//
//        cmf.run();
//
//        String[] supposedContent = new String[] {
//            "<filesystem>",
//                    "<folder name=\"Actions\">",
//                    "<folder name=\"Tools\">",
//                    "<file name=\"org-example-module1-BeepAction.instance\"/>",
//                    "</folder>",
//                    "</folder>",
//                    "<folder name=\"Editors\">",
//                    "<folder name=\"text\">",
//                    "<folder name=\"x-java\">",
//                    "<folder name=\"Popup\">",
//                    "<attr name=\"org-example-module1-BeepAction.shadow/generate-goto-popup\" boolvalue=\"true\"/>",
//                    "<attr name=\"org-example-module1-BeepAction.shadow/org-example-module1-separatorAfter.instance\" boolvalue=\"true\"/>",
//                    "<attr name=\"org-example-module1-separatorAfter.instance/generate-goto-popup\" boolvalue=\"true\"/>",
//                    "<file name=\"org-example-module1-BeepAction.shadow\">",
//                    "<attr name=\"originalFile\" stringvalue=\"Actions/Tools/org-example-module1-BeepAction.instance\"/>",
//                    "</file>",
//                    "<file name=\"org-example-module1-separatorAfter.instance\">",
//                    "<attr name=\"instanceClass\" stringvalue=\"javax.swing.JSeparator\"/>",
//                    "</file>",
//                    "</folder>",
//                    "</folder>",
//                    "</folder>",
//                    "</folder>",
//                    "<folder name=\"Loaders\">",
//                    "<folder name=\"text\">",
//                    "<folder name=\"x-java\">",
//                    "<folder name=\"Actions\">",
//                    "<attr name=\"org-example-module1-BeepAction.shadow/OpenAction.instance\" boolvalue=\"true\"/>",
//                    "<attr name=\"org-example-module1-BeepAction.shadow/org-example-module1-separatorAfter.instance\" boolvalue=\"true\"/>",
//                    "<attr name=\"org-example-module1-separatorAfter.instance/OpenAction.instance\" boolvalue=\"true\"/>",
//                    "<file name=\"org-example-module1-BeepAction.shadow\">",
//                    "<attr name=\"originalFile\" stringvalue=\"Actions/Tools/org-example-module1-BeepAction.instance\"/>",
//                    "</file>",
//                    "<file name=\"org-example-module1-separatorAfter.instance\">",
//                    "<attr name=\"instanceClass\" stringvalue=\"javax.swing.JSeparator\"/>",
//                    "</file>",
//                    "</folder>",
//                    "</folder>",
//                    "</folder>",
//                    "</folder>",
//                    "<folder name=\"Menu\">",
//                    "<folder name=\"Help\">",
//                    "<folder name=\"Tutorials\">",
//                    "<attr name=\"org-example-module1-BeepAction.shadow/org-example-module1-separatorAfter.instance\" boolvalue=\"true\"/>",
//                    "<attr name=\"org-example-module1-BeepAction.shadow/prj-import-guide.url\" boolvalue=\"true\"/>",
//                    "<attr name=\"org-example-module1-separatorAfter.instance/prj-import-guide.url\" boolvalue=\"true\"/>",
//                    "<attr name=\"org-example-module1-separatorBefore.instance/org-example-module1-BeepAction.shadow\" boolvalue=\"true\"/>",
//                    "<attr name=\"quick-start.url/org-example-module1-BeepAction.shadow\" boolvalue=\"true\"/>",
//                    "<attr name=\"quick-start.url/org-example-module1-separatorBefore.instance\" boolvalue=\"true\"/>",
//                    "<file name=\"org-example-module1-BeepAction.shadow\">",
//                    "<attr name=\"originalFile\" stringvalue=\"Actions/Tools/org-example-module1-BeepAction.instance\"/>",
//                    "</file>",
//                    "<file name=\"org-example-module1-separatorAfter.instance\">",
//                    "<attr name=\"instanceClass\" stringvalue=\"javax.swing.JSeparator\"/>",
//                    "</file>",
//                    "<file name=\"org-example-module1-separatorBefore.instance\">",
//                    "<attr name=\"instanceClass\" stringvalue=\"javax.swing.JSeparator\"/>",
//                    "</file>",
//                    "</folder>",
//                    "</folder>",
//                    "</folder>",
//                    "<folder name=\"Toolbars\">",
//                    "<folder name=\"Edit\">",
//                    "<attr name=\"org-openide-actions-FindAction.instance/org-example-module1-BeepAction.shadow\" boolvalue=\"true\"/>",
//                    "<file name=\"org-example-module1-BeepAction.shadow\">",
//                    "<attr name=\"originalFile\" stringvalue=\"Actions/Tools/org-example-module1-BeepAction.instance\"/>",
//                    "</file>",
//                    "</folder>",
//                    "</folder>",
//                    "</filesystem>"
//        };
//
//        CreatedModifiedFilesTest.assertLayerContent(supposedContent,
//                new File(getWorkDir(), "module1/src/org/example/module1/resources/layer.xml"));
//    }
    
}

