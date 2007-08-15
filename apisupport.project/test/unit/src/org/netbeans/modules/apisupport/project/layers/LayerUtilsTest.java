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

package org.netbeans.modules.apisupport.project.layers;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectGenerator;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectGenerator;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
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
        TestBase.initializeBuildProperties(getWorkDir(), getDataDir());
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
        SuiteProjectGenerator.createSuiteProject(suiteDir, NbPlatform.PLATFORM_ID_DEFAULT);
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
        SuiteProjectGenerator.createSuiteProject(suiteDir, NbPlatform.PLATFORM_ID_DEFAULT);
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
        cmf = new CreatedModifiedFiles(module2);
        cmf.add(cmf.createLayerEntry("bar", null, null, "Bar", null));
        cmf.add(cmf.createLayerEntry("test-module2-MyAction.instance", null, null, null, null));
        cmf.add(cmf.createLayerEntry("test-module2-some-action.instance", null, null, null, Collections.<String,Object>singletonMap("instanceClass", "test.module2.SomeAction")));
        cmf.add(cmf.createLayerEntry("test-module2-another-action.instance", null, null, null, Collections.<String,Object>singletonMap("instanceCreate", "newvalue:test.module2.AnotherAction")));
        cmf.add(cmf.createLayerEntry("test-module2-factory-action.instance", null, null, null, Collections.<String,Object>singletonMap("instanceCreate", "methodvalue:test.module2.FactoryAction.create")));
        cmf.add(cmf.createLayerEntry("sep-42.instance", null, null, null, Collections.<String,Object>singletonMap("instanceClass", "javax.swing.JSeparator")));
        cmf.add(cmf.createLayerEntry("link-to-standard.shadow", null, null, null, Collections.<String,Object>singletonMap("originalFile", "Actions/System/org-openide-actions-OpenAction.instance")));
        cmf.add(cmf.createLayerEntry("link-to-custom.shadow", null, null, null, Collections.<String,Object>singletonMap("originalFile", "test-module2-MyAction.instance")));
        File dummyDir = new File(getWorkDir(), "dummy");
        dummyDir.mkdir();
        cmf.add(cmf.createLayerEntry("link-to-url.shadow", null, null, null, Collections.<String,Object>singletonMap("originalFile", dummyDir.toURI().toURL())));
        cmf.run();
        FileSystem fs = LayerUtils.getEffectiveSystemFilesystem(module2);
        assertDisplayName(fs, "right display name for platform file", "Menu/RunProject", "Run");
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
        DataObject.find(fs.findResource("link-to-url.shadow")).getNodeDelegate().getDisplayName(); // #65665
        /* XXX too hard to unit test in practice, since we will get a CNFE trying to load a class from editor here:
        //System.err.println("items in Menu/Edit: " + java.util.Arrays.asList(fs.findResource("Menu/Edit").getChildren()));
        assertDisplayName(fs, "right display name for non-action with only menu presenter", "Menu/Edit/org-netbeans-modules-editor-MainMenuAction$FindSelectionAction.instance", "Find Selection");
         */
    }
    
    public void testSystemFilesystemLocalizedNamesI18N() throws Exception {
        Locale orig = Locale.getDefault();
        try {
            Locale.setDefault(Locale.JAPAN);
            File platformDir = new File(getWorkDir(), "testPlatform");
            Manifest mf = new Manifest();
            mf.getMainAttributes().putValue("OpenIDE-Module", "platform.module");
            mf.getMainAttributes().putValue("OpenIDE-Module-Layer", "platform/module/layer.xml");
            Map/*<String,String>*/ contents = new HashMap();
            contents.put("platform/module/Bundle.properties", "folder/file=English");
            contents.put("platform/module/layer.xml", "<filesystem><folder name=\"folder\"><file name=\"file\"><attr name=\"SystemFileSystem.localizingBundle\" stringvalue=\"platform.module.Bundle\"/></file></folder></filesystem>");
            TestBase.createJar(new File(platformDir, "cluster/modules/platform-module.jar".replace('/', File.separatorChar)), contents, mf);
            mf = new Manifest();
            contents = new HashMap();
            contents.put("platform/module/Bundle_ja.properties", "folder/file=Japanese");
            TestBase.createJar(new File(platformDir, "cluster/modules/locale/platform-module_ja.jar".replace('/', File.separatorChar)), contents, mf);
            // To satisfy NbPlatform.isValid:
            TestBase.createJar(new File(new File(new File(platformDir, "platform"), "core"), "core.jar"), Collections.EMPTY_MAP, new Manifest());
            NbPlatform.addPlatform("testplatform", platformDir, "Test Platform");
            File suiteDir = new File(getWorkDir(), "testSuite");
            SuiteProjectGenerator.createSuiteProject(suiteDir, "testplatform");
            File moduleDir = new File(suiteDir, "testModule");
            NbModuleProjectGenerator.createSuiteComponentModule(
                    moduleDir,
                    "test.module",
                    "module",
                    "test/module/resources/Bundle.properties",
                    "test/module/resources/layer.xml",
                    suiteDir);
            NbModuleProject module = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(moduleDir));
            FileSystem fs = LayerUtils.getEffectiveSystemFilesystem(module);
            assertDisplayName(fs, "#64779: localized platform filename", "folder/file", "Japanese");
        } finally {
            Locale.setDefault(orig);
        }
    }
    
    public void testSystemFilesystemNetBeansOrgProject() throws Exception {
        FileObject nbcvsroot = FileUtil.toFileObject(new File(System.getProperty("test.nbcvsroot")));
        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(nbcvsroot.getFileObject("beans"));
        FileSystem fs = LayerUtils.getEffectiveSystemFilesystem(p);
        assertDisplayName(fs, "right display name for netbeans.org standard file", "Menu/RunProject", "Run");
        assertNull("not loading files from extra modules", fs.findResource("Templates/Documents/docbook-article.xml"));
        FileObject docbook = nbcvsroot.getFileObject("contrib/docbook");
        if (docbook == null) {
            System.err.println("Skipping part of testSystemFilesystemNetBeansOrgProject since contrib is not checked out");
            return;
        }
        p = (NbModuleProject) ProjectManager.getDefault().findProject(docbook);
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
    
    public void testMasks() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module");
        FileSystem fs = LayerUtils.getEffectiveSystemFilesystem(project);
        Set/*<String>*/ optionInstanceNames = new HashSet();
        FileObject toolsMenu = fs.findResource("Menu/Tools");
        assertNotNull(toolsMenu);
        FileObject[] kids = toolsMenu.getChildren();
        for (int i = 0; i < kids.length; i++) {
            String name = kids[i].getNameExt();
            if (name.indexOf("Options") != -1) {
                optionInstanceNames.add(name);
            }
        }
        assertEquals("#63295: masks work",
                new HashSet(Arrays.asList(new String[] {
            "org-netbeans-modules-options-OptionsWindowAction.shadow",
            // org-netbeans-core-actions-OptionsAction.instance should be masked
        })), optionInstanceNames);
        assertNotNull("system FS has xml/catalog", fs.findResource("Services/Hidden/CatalogProvider/org-netbeans-modules-xml-catalog-impl-XCatalogProvider.instance"));
        assertNull("but one entry hidden by apisupport/project", fs.findResource("Services/Hidden/org-netbeans-modules-xml-catalog-impl-SystemCatalogProvider.instance"));
    }
    
}
