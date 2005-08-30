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

package org.netbeans.modules.apisupport.project.layers;

import java.io.File;
import java.util.Collections;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectGenerator;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectGenerator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * Test writing changes to layers.
 * @author Jesse Glick
 */
public class LayerUtilsTest extends LayerTestBase {
    
    public LayerUtilsTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        TestBase.initializeBuildProperties(getWorkDir());
    }
    
    public void testLayerHandle() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module");
        LayerUtils.LayerHandle handle = LayerUtils.layerForProject(project);
        FileObject expectedLayerXML = project.getProjectDirectory().getFileObject("src/org/example/module/resources/layer.xml");
        assertNotNull(expectedLayerXML);
        FileObject layerXML = handle.getLayerFile();
        assertNotNull("layer.xml already exists", layerXML);
        assertEquals("right layer file", expectedLayerXML, layerXML);
        FileSystem fs = handle.layer(true);
        assertEquals("initially empty", 0, fs.getRoot().getChildren().length);
        long initialSize = layerXML.getSize();
        fs.getRoot().createData("foo");
        assertEquals("not saved yet", initialSize, layerXML.getSize());
        fs = handle.layer(true);
        assertNotNull("still have in-memory mods", fs.findResource("foo"));
        fs.getRoot().createData("bar");
        handle.save();
        assertTrue("now it is saved", layerXML.getSize() > initialSize);
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.1//EN\" \"http://www.netbeans.org/dtds/filesystem-1_1.dtd\">\n" +
                "<filesystem>\n" +
                "    <file name=\"bar\"/>\n" +
                "    <file name=\"foo\"/>\n" +
                "</filesystem>\n";
        assertEquals("right contents too", xml, TestBase.slurp(layerXML));
        // XXX test that nbres: file contents work
    }
    
    public void testLayerAutoSave() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module");
        LayerUtils.LayerHandle handle = LayerUtils.layerForProject(project);
        FileSystem fs = handle.layer(true);
        handle.setAutosave(true);
        FileObject foo = fs.getRoot().createData("foo");
        FileObject layerXML = handle.getLayerFile();
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.1//EN\" \"http://www.netbeans.org/dtds/filesystem-1_1.dtd\">\n" +
                "<filesystem>\n" +
                "    <file name=\"foo\"/>\n" +
                "</filesystem>\n";
        assertEquals("saved automatically", xml, TestBase.slurp(layerXML));
        foo.setAttribute("a", Boolean.TRUE);
        xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.1//EN\" \"http://www.netbeans.org/dtds/filesystem-1_1.dtd\">\n" +
                "<filesystem>\n" +
                "    <file name=\"foo\">\n" +
                "        <attr name=\"a\" boolvalue=\"true\"/>\n" +
                "    </file>\n" +
                "</filesystem>\n";
        assertEquals("saved automatically from an attribute change too", xml, TestBase.slurp(layerXML));
    }
    
    // XXX testInitiallyInvalidLayer
    // XXX testInitiallyMissingLayer
    // XXX testGcLayerHandle
    
    public void testSystemFilesystemStandaloneProject() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module");
        LayerUtils.LayerHandle handle = LayerUtils.layerForProject(project);
        FileObject layerXML = handle.getLayerFile();
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.1//EN\" \"http://www.netbeans.org/dtds/filesystem-1_1.dtd\">\n" +
                "<filesystem>\n" +
                "    <file name=\"foo\"/>\n" +
                "</filesystem>\n";
        TestBase.dump(layerXML, xml);
        long start = System.currentTimeMillis();
        FileSystem fs = LayerUtils.getEffectiveSystemFilesystem(project);
        System.err.println("LayerUtils.getEffectiveSystemFilesystem ran in " + (System.currentTimeMillis() - start) + "msec");
        assertFalse("can write to it", fs.isReadOnly());
        assertNotNull("have stuff from the platform", fs.findResource("Menu/File"));
        assertNotNull("have stuff from my own layer", fs.findResource("foo"));
        fs.getRoot().createData("quux");
        handle.save();
        xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.1//EN\" \"http://www.netbeans.org/dtds/filesystem-1_1.dtd\">\n" +
                "<filesystem>\n" +
                "    <file name=\"foo\"/>\n" +
                "    <file name=\"quux\"/>\n" +
                "</filesystem>\n";
        assertEquals("new layer stored", xml, TestBase.slurp(layerXML));
    }
    
    public void testSystemFilesystemSuiteComponentProject() throws Exception {
        File suiteDir = new File(getWorkDir(), "testSuite");
        SuiteProjectGenerator.createSuiteProject(suiteDir, "default");
        File module1Dir = new File(suiteDir, "testModule1");
        NbModuleProjectGenerator.createSuiteComponentModule(
                module1Dir,
                "test.module1",
                "module1",
                "test/module1/resources/Bundle.properties",
                "test/module1/resources/layer.xml",
                suiteDir);
        NbModuleProject module1 = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(module1Dir));
        LayerUtils.LayerHandle handle = LayerUtils.layerForProject(module1);
        FileUtil.createData(handle.layer(true).getRoot(), "random/stuff");
        handle.save();
        File module2Dir = new File(suiteDir, "testModule2");
        NbModuleProjectGenerator.createSuiteComponentModule(
                module2Dir,
                "test.module2",
                "module2",
                "test/module2/resources/Bundle.properties",
                "test/module2/resources/layer.xml",
                suiteDir);
        NbModuleProject module2 = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(module2Dir));
        handle = LayerUtils.layerForProject(module2);
        FileObject layerXML = handle.getLayerFile();
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.1//EN\" \"http://www.netbeans.org/dtds/filesystem-1_1.dtd\">\n" +
                "<filesystem>\n" +
                "    <file name=\"existing\"/>\n" +
                "</filesystem>\n";
        TestBase.dump(layerXML, xml);
        FileSystem fs = LayerUtils.getEffectiveSystemFilesystem(module2);
        assertFalse("can write to it", fs.isReadOnly());
        assertNotNull("have stuff from the platform", fs.findResource("Menu/File"));
        assertNotNull("have stuff from my own layer", fs.findResource("existing"));
        assertNotNull("have stuff from other modules in the same suite", fs.findResource("random/stuff"));
        fs.getRoot().createData("new");
        handle.save();
        xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.1//EN\" \"http://www.netbeans.org/dtds/filesystem-1_1.dtd\">\n" +
                "<filesystem>\n" +
                "    <file name=\"existing\"/>\n" +
                "    <file name=\"new\"/>\n" +
                "</filesystem>\n";
        assertEquals("new layer stored", xml, TestBase.slurp(layerXML));
    }
    
    public void testSystemFilesystemLocalizedNames() throws Exception {
        File suiteDir = new File(getWorkDir(), "testSuite");
        SuiteProjectGenerator.createSuiteProject(suiteDir, "default");
        /* XXX too hard to unit test in practice:
        SuiteProject suite = (SuiteProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(suiteDir));
        // Make ide6 *not* be excluded, so we can test some editor actions:
        EditableProperties p = suite.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        p.setProperty("disabled.clusters", "");
        suite.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, p);
        ProjectManager.getDefault().saveProject(suite);
         */
        File module1Dir = new File(suiteDir, "testModule1");
        NbModuleProjectGenerator.createSuiteComponentModule(
                module1Dir,
                "test.module1",
                "module1",
                "test/module1/resources/Bundle.properties",
                "test/module1/resources/layer.xml",
                suiteDir);
        NbModuleProject module1 = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(module1Dir));
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(module1);
        cmf.add(cmf.createLayerEntry("foo", null, null, "Foo", null));
        cmf.run();
        File module2Dir = new File(suiteDir, "testModule2");
        NbModuleProjectGenerator.createSuiteComponentModule(
                module2Dir,
                "test.module2",
                "module2",
                "test/module2/resources/Bundle.properties",
                "test/module2/resources/layer.xml",
                suiteDir);
        NbModuleProject module2 = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(module2Dir));
        /*
        assertEquals("right disabled.clusters in suite evaluator", "", suite.getEvaluator().getProperty("disabled.clusters"));
         */
        cmf = new CreatedModifiedFiles(module2);
        cmf.add(cmf.createLayerEntry("bar", null, null, "Bar", null));
        cmf.add(cmf.createLayerEntry("test-module2-MyAction.instance", null, null, null, null));
        cmf.add(cmf.createLayerEntry("test-module2-some-action.instance", null, null, null, Collections.singletonMap("instanceClass", "test.module2.SomeAction")));
        cmf.add(cmf.createLayerEntry("test-module2-another-action.instance", null, null, null, Collections.singletonMap("instanceCreate", "newvalue:test.module2.AnotherAction")));
        cmf.add(cmf.createLayerEntry("test-module2-factory-action.instance", null, null, null, Collections.singletonMap("instanceCreate", "methodvalue:test.module2.FactoryAction.create")));
        cmf.add(cmf.createLayerEntry("sep-42.instance", null, null, null, Collections.singletonMap("instanceClass", "javax.swing.JSeparator")));
        cmf.add(cmf.createLayerEntry("link-to-standard.shadow", null, null, null, Collections.singletonMap("originalFile", "Actions/System/org-openide-actions-OpenAction.instance")));
        cmf.add(cmf.createLayerEntry("link-to-custom.shadow", null, null, null, Collections.singletonMap("originalFile", "test-module2-MyAction.instance")));
        cmf.run();
        FileSystem fs = LayerUtils.getEffectiveSystemFilesystem(module2);
        assertDisplayName(fs, "right display name for platform file", "Menu/Window/SelectDocumentNode", "Select Document in");
        assertDisplayName(fs, "label for file in suite", "foo", "Foo");
        assertDisplayName(fs, "label for file in this project", "bar", "Bar");
        assertDisplayName(fs, "right display name for well-known action", "Menu/File/org-openide-actions-SaveAction.instance", "Save");
        assertDisplayName(fs, "label for simple instance", "test-module2-MyAction.instance", "<instance of MyAction>");
        assertDisplayName(fs, "label for instanceClass", "test-module2-some-action.instance", "<instance of SomeAction>");
        assertDisplayName(fs, "label for newvalue instanceCreate", "test-module2-another-action.instance", "<instance of AnotherAction>");
        assertDisplayName(fs, "label for methodvalue instanceCreate", "test-module2-factory-action.instance", "<instance from FactoryAction.create>");
        assertDisplayName(fs, "label for menu separator", "sep-42.instance", "<separator>");
        assertDisplayName(fs, "link to standard menu item", "link-to-standard.shadow", "Open");
        assertDisplayName(fs, "link to custom menu item", "link-to-custom.shadow", "<instance of MyAction>");
        /*
        //System.err.println("items in Menu/Edit: " + java.util.Arrays.asList(fs.findResource("Menu/Edit").getChildren()));
        assertDisplayName(fs, "right display name for non-action with only menu presenter", "Menu/Edit/org-netbeans-modules-editor-MainMenuAction$FindSelectionAction.instance", "Find Selection");
         */
    }
    
    public void testSystemFilesystemNetBeansOrgProject() throws Exception {
        FileObject nbroot = FileUtil.toFileObject(new File(System.getProperty("test.nbroot")));
        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(nbroot.getFileObject("beans"));
        FileSystem fs = LayerUtils.getEffectiveSystemFilesystem(p);
        assertDisplayName(fs, "right display name for netbeans.org standard file", "Menu/Window/SelectDocumentNode", "Select Document in");
        assertNull("not loading files from extra modules", fs.findResource("Templates/Documents/docbook-article.xml"));
        p = (NbModuleProject) ProjectManager.getDefault().findProject(nbroot.getFileObject("contrib/docbook"));
        fs = LayerUtils.getEffectiveSystemFilesystem(p);
        assertDisplayName(fs, "right display name for file from extra module", "Templates/Documents/docbook-article.xml", "DocBook Article");
    }
    
    // XXX testClusterAndModuleExclusions
    // XXX testSystemFilesystemSuiteProject

    private static void assertDisplayName(FileSystem fs, String message, String path, String label) throws Exception {
        FileObject file = fs.findResource(path);
        assertNotNull("found " + path, file);
        assertEquals(message, label, DataObject.find(file).getNodeDelegate().getDisplayName());
    }
    
}
