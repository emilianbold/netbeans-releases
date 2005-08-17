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

package org.netbeans.modules.apisupport.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import junit.framework.TestCase;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles.Operation;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleDependency;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;

/**
 * Tests {@link CreatedModifiedFiles}.
 * @author Martin Krauskopf
 */
public class CreatedModifiedFilesTest extends LayerTestBase {

    private static final String[] HTML_CONTENT = new String[] {
        "<html>",
        "<em>i am some template</em>",
        "</html>"
    };
    
    private static final Map TOKENS_MAP = new HashMap(2);
    
    static {
        TOKENS_MAP.put("some", "a\\ nonsense");
        TOKENS_MAP.put("\\<(\\/{0,1})em\\>", "<$1strong>");
    }
    
    private static final String[] HTML_CONTENT_TOKENIZED = new String[] {
        "<html>",
        "<strong>i am a nonsense template</strong>",
        "</html>"
    };
    
    public CreatedModifiedFilesTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        TestBase.initializeBuildProperties(getWorkDir());
    }
    
    public void testCreatedModifiedFiles() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        cmf.add(cmf.bundleKeyDefaultBundle(LocalizedBundleInfo.NAME, "Much Better Name"));
        cmf.add(cmf.bundleKey("src/custom.properties", "some.property", "some value"));
        cmf.add(cmf.addLoaderSection("org/example/module1/MyExtLoader", null));
        cmf.add(cmf.createFile("src/org/example/module1/resources/template.html", createFile(HTML_CONTENT)));
        cmf.add(cmf.addLookupRegistration(
                "org.example.spi.somemodule.ProvideMe",
                "org.example.module1.ProvideMeImpl"));
        
        assertRelativePaths(
                new String[] {"src/META-INF/services/org.example.spi.somemodule.ProvideMe", "src/custom.properties", "src/org/example/module1/resources/template.html"},
                cmf.getCreatedPaths());
        assertRelativePaths(
                new String[] {"manifest.mf", "src/org/example/module1/resources/Bundle.properties"},
                cmf.getModifiedPaths());

        cmf.run();
    }
    
    public void testBundleKeyDefaultBundle() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        ProjectInformation pi = (ProjectInformation) project.getLookup().lookup(ProjectInformation.class);
        assertEquals("display name before from bundle", "Testing Module", pi.getDisplayName());
        assertEquals("display name before from project", "Testing Module", project.getBundleInfo().getDisplayName());
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        Operation op = cmf.bundleKeyDefaultBundle(LocalizedBundleInfo.NAME, "Much Better Name");
        assertRelativePath("src/org/example/module1/resources/Bundle.properties",
                op.getModifiedPaths());
        op.run();
        
        pi = (ProjectInformation) project.getLookup().lookup(ProjectInformation.class);
        assertEquals("display name after from bundle", "Much Better Name", pi.getDisplayName());
        assertEquals("display name after from project", "Much Better Name", project.getBundleInfo().getDisplayName());
    }
    
    public void testBundleKey() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        Operation op = cmf.bundleKey("src/custom.properties", "some.property", "some value");
        
        assertRelativePath("src/custom.properties", op.getCreatedPaths());
        
        cmf.add(op);
        cmf.run();
        
        EditableProperties ep = Util.loadProperties(FileUtil.toFileObject(TestBase.file(getWorkDir(), "module1/src/custom.properties")));
        assertEquals("property created", "some value", ep.getProperty("some.property"));
    }
    
    public void testAddLoaderSection() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        Operation op = cmf.addLoaderSection("org/example/module1/MyExtLoader", null);
        
        assertRelativePath("manifest.mf", op.getModifiedPaths());
        
        op.run();
        
        EditableManifest em = Util.loadManifest(FileUtil.toFileObject(TestBase.file(getWorkDir(), "module1/manifest.mf")));
        assertEquals("loader section was added", "Loader", em.getAttribute("OpenIDE-Module-Class", "org/example/module1/MyExtLoader.class"));
    }
    
    public void testAddLookupRegistration() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        Operation op = cmf.addLookupRegistration(
                "org.example.spi.somemodule.ProvideMe",
                "org.example.module1.ProvideMeImpl");
        
        assertRelativePath("src/META-INF/services/org.example.spi.somemodule.ProvideMe", op.getCreatedPaths());
        
        cmf.add(op);
        cmf.run();
    }
    
    public void testCreateFile() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        
        String templatePath = "src/org/example/module1/resources/template.html";
        Operation op = cmf.createFile(templatePath, createFile(HTML_CONTENT));
        
        assertRelativePath(templatePath, op.getCreatedPaths());
        
        cmf.add(op);
        cmf.run();
        
        assertFileContent(HTML_CONTENT, new File(getWorkDir(), "module1/" + templatePath));
    }
    
    public void testCreateBinaryFile() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        
        String templatePath = "src/org/example/module1/resources/binarytemplate.zip";
        
        File binaryFile = createBinaryFile(HTML_CONTENT);
        
        Operation op = cmf.createFile(templatePath, binaryFile.toURI().toURL());
        
        assertRelativePath(templatePath, op.getCreatedPaths());
        
        cmf.add(op);
        cmf.run();
        
        assertFileContent(binaryFile, new File(getWorkDir(), "module1/" + templatePath));
    }
    
    public void testCreateFileWithSubstitutions() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        
        String templatePath = "src/org/example/module1/resources/template.html";
        Operation op = cmf.createFileWithSubstitutions(templatePath, createFile(HTML_CONTENT), TOKENS_MAP);
        
        assertRelativePath(templatePath, op.getCreatedPaths());
        
        cmf.add(op);
        cmf.run();
        
        assertFileContent(HTML_CONTENT_TOKENIZED, new File(getWorkDir(), "module1/" + templatePath));
    }
    
    public void testAddModuleDependency() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        
        Operation op = cmf.addModuleDependency("org.apache.tools.ant.module", 3,
                new SpecificationVersion("3.9"), true);
        
        assertRelativePath("nbproject/project.xml", op.getModifiedPaths());
        
        cmf.add(op);
        cmf.run();
        
        ProjectXMLManager pxm = new ProjectXMLManager(project.getHelper());
        Set deps = pxm.getDirectDependencies(NbPlatform.getDefaultPlatform());
        assertEquals("one dependency", 1, deps.size());
        ModuleDependency antDep = (ModuleDependency) deps.toArray()[0];
        assertEquals("cnb", "org.apache.tools.ant.module", antDep.getModuleEntry().getCodeNameBase());
        assertEquals("release version", "3", antDep.getModuleEntry().getReleaseVersion());
        assertEquals("specification version", "3.9", antDep.getSpecificationVersion());
        assertTrue("compile dependeny", antDep.hasCompileDependency());
        assertFalse("implementation dependeny", antDep.hasImplementationDepedendency());
    }
    
    public void testTheSameModuleDependencyTwice() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        
        Operation op = cmf.addModuleDependency("org.apache.tools.ant.module", -1, null, false);
        
        assertRelativePath("nbproject/project.xml", op.getModifiedPaths());
        
        cmf.add(op);
        cmf.add(op);
        cmf.run();
        
        ProjectXMLManager pxm = new ProjectXMLManager(project.getHelper());
        Set deps = pxm.getDirectDependencies(NbPlatform.getDefaultPlatform());
        assertEquals("one dependency", 1, deps.size());
        ModuleDependency antDep = (ModuleDependency) deps.toArray()[0];
        assertEquals("cnb", "org.apache.tools.ant.module", antDep.getModuleEntry().getCodeNameBase());
    }
    
    public void testOrderLayerEntry() throws Exception {
        // also tested in testCreateLayerEntry where is also tested generated content
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        
        Operation op = cmf.orderLayerEntry("Loaders/text/x-java/Actions",
                "IAmSecond.instance", "IAmThird.instance");
        
        assertRelativePath("src/org/example/module1/resources/layer.xml", op.getModifiedPaths());
        
        cmf.add(op);
        cmf.run();
    }
    
    public void testCreateLayerEntry() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        Operation layerOp = cmf.createLayerEntry(
                "Menu/Tools/org-example-module1-BeepAction.instance",
                null,
                null,
                null, 
                null);
        layerOp.run();
        
        layerOp = cmf.createLayerEntry(
                "Services/org-example-module1-Module1UI.settings",
                null,
                null,
                null,
                null);
        cmf.add(layerOp);
        assertRelativePath("src/org/example/module1/resources/layer.xml", layerOp.getModifiedPaths());
        
        layerOp = cmf.orderLayerEntry("Menu/Tools",
                "org-example-module1-BeepAction.instance",
                "org-example-module1-BlareAction.instance");
        cmf.add(layerOp);
        
        layerOp = cmf.createLayerEntry(
                "Menu/Tools/org-example-module1-BlareAction.instance",
                null,
                null,
                null, 
                null);
        cmf.add(layerOp);
        
        layerOp = cmf.createLayerEntry(
                "Services/org-example-module1-Other.settings",
                createFile(HTML_CONTENT),
                null,
                null, 
                null);
        cmf.add(layerOp);

        layerOp = cmf.createLayerEntry(
                "Services/org-example-module1-Tokenized.settings",
                createFile(HTML_CONTENT),
                TOKENS_MAP,
                null,
                null);
        cmf.add(layerOp);

        layerOp = cmf.createLayerEntry(
                "Services/org-example-module1-LocalizedAndTokened.settings",
                createFile(HTML_CONTENT),
                TOKENS_MAP,
                "Some Settings",
                null);
        cmf.add(layerOp);

        assertRelativePaths(
                new String[] {"src/org/example/module1/resources/Bundle.properties", "src/org/example/module1/resources/layer.xml"},
                cmf.getModifiedPaths());
        assertRelativePaths(
                new String[] {
                    "src/org/example/module1/resources/org-example-module1-LocalizedAndTokened.xml",
                    "src/org/example/module1/resources/org-example-module1-Other.xml",
                    "src/org/example/module1/resources/org-example-module1-Tokenized.xml"
                },
                cmf.getCreatedPaths());
        cmf.run();

        assertFileContent(HTML_CONTENT_TOKENIZED, new File(getWorkDir(), "module1/src/org/example/module1/resources/org-example-module1-Tokenized.xml"));

        // check layer content
        String[] supposedContent = new String[] {
            "<filesystem>",
                    "<folder name=\"Menu\">",
                    "<folder name=\"Tools\">",
                    "<file name=\"org-example-module1-BeepAction.instance\"/>",
                    "<attr name=\"org-example-module1-BeepAction.instance/org-example-module1-BlareAction.instance\" boolvalue=\"true\"/>",
                    "<file name=\"org-example-module1-BlareAction.instance\"/>",
                    "</folder>",
                    "</folder>",
                    "<folder name=\"Services\">",
                    "<file name=\"org-example-module1-LocalizedAndTokened.settings\" url=\"org-example-module1-LocalizedAndTokened.xml\">",
                    "<attr name=\"SystemFileSystem.localizingBundle\" stringvalue=\"org.example.module1.resources.Bundle\"/>",
                    "</file>",
                    "<file name=\"org-example-module1-Module1UI.settings\"/>",
                    "<file name=\"org-example-module1-Other.settings\" url=\"org-example-module1-Other.xml\"/>",
                    "<file name=\"org-example-module1-Tokenized.settings\" url=\"org-example-module1-Tokenized.xml\"/>",
                    "</folder>",
                    "</filesystem>"
        };
        assertLayerContent(supposedContent, 
                new File(getWorkDir(), "module1/src/org/example/module1/resources/layer.xml"));
        
        // check bundle content
        EditableProperties ep = Util.loadProperties(FileUtil.toFileObject(
                TestBase.file(getWorkDir(), "module1/src/org/example/module1/resources/Bundle.properties")));
        assertEquals("localized name property", "Some Settings",
                ep.getProperty("Services/org-example-module1-LocalizedAndTokened.settings"));
        assertEquals("module name", "Testing Module", ep.getProperty("OpenIDE-Module-Name"));
    }
    
    public void testCreateLayerAttribute() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        String fqClassName = "org.example.module1.BeepAction";
        String dashedFqClassName = fqClassName.replace('.', '-');
        String layerPath = "Actions/Tools/" + dashedFqClassName + ".instance";
        
        Operation op = cmf.createLayerEntry(layerPath, null, null, null, null);
        cmf.add(op);
        
        op = cmf.createLayerAttribute(
                layerPath, "instanceClass", fqClassName);
        assertRelativePath("src/org/example/module1/resources/layer.xml", op.getModifiedPaths());
        
        cmf.add(op);
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
                    "</filesystem>"
        };
        assertLayerContent(supposedContent, 
                new File(getWorkDir(), "module1/src/org/example/module1/resources/layer.xml"));
    }
    
//    TODO: mkleint
//    public void testCreateLayerSubtree() throws Exception {
//    }
    
    public static void assertRelativePath(String expectedPath, String[] paths) {
        TestCase.assertEquals("one path", 1, paths.length);
        TestCase.assertEquals("created, modified paths", expectedPath, paths[0]);
    }
    
    public static void assertRelativePath(String expectedPath, SortedSet paths) {
        String[] s = new String[paths.size()];
        assertRelativePath(expectedPath, (String[]) paths.toArray(s));
    }
    
    public static void assertRelativePaths(String[] expectedPaths, String[] paths) {
        TestCase.assertEquals("created, modified paths", Arrays.asList(expectedPaths), Arrays.asList(paths));
    }
    
    private URL createFile(String[] content) throws IOException {
        File myTemplate = getWorkDir().createTempFile("myTemplate", "html");
        OutputStream myTemplateOS = new FileOutputStream(myTemplate);
        PrintWriter pw = new PrintWriter(myTemplateOS);
        try {
            for (int i = 0; i < content.length; i++) {
                pw.println(content[i]);
            }
        } finally {
            pw.close();
        }
        return myTemplate.toURI().toURL();
    }
    
    private File createBinaryFile(String[] content) throws IOException {
        File myTemplate = getWorkDir().createTempFile("myTemplate", "zip");
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(myTemplate));
        ZipEntry entry = new ZipEntry("a/b/c/d.txt");
        zos.putNextEntry(entry);
        
        try {
            for (int i = 0; i  < content.length; i++) {
                zos.write(content[i].getBytes());
            }
            
        } finally {
            zos.close();
        }
        return myTemplate;
    }
    
    private void assertFileContent(String[] content, File file) throws IOException {
        assertTrue("file exist and is a regular file", file.isFile());
        BufferedReader br = new BufferedReader(new FileReader(file));
        try {
            for (int i = 0; i < content.length; i++) {
                assertEquals("file content", content[i], br.readLine());
            }
            assertNull(br.readLine());
        } finally {
            br.close();
        }
    }
    
    private void assertFileContent(File f1, File f2) throws IOException {
        InputStream is = new FileInputStream(f1);
        InputStream is2 = new FileInputStream(f2);
        
        try {
            byte[] content = new byte[is.available()];
            is.read(content);
            
            byte[] content2 = new byte[is2.available()];
            is2.read(content2);
            
            for (int i = 0; i < content.length; i++) {
                assertEquals("file content", content[i], content2[i]);
            }
        } finally {
            is.close();
            is2.close();
        }
    }
    
    public static void assertLayerContent(final String[] supposedContent,
            final File layerF) throws IOException, FileNotFoundException {
        BufferedReader reader = new BufferedReader(new FileReader(layerF));
        List actualContent = new ArrayList();
        boolean fsElementReached = false;
        String line;
        
        try {
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!fsElementReached && line.equals(supposedContent[0])) {
                    fsElementReached = true;
                    actualContent.add(line);
                    continue;
                }
                if (fsElementReached) {
                    actualContent.add(line);
                }
            }
        } finally {
            reader.close();
        }
        
        assertEquals("content of layer", Arrays.asList(supposedContent).toString(), actualContent.toString());
    }
    
}

