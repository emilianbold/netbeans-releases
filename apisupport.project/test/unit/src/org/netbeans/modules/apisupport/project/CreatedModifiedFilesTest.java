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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles.LayerCallback;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles.Operation;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileUtil;

/**
 * Tests {@link CreatedModifiedFiles}.
 * @author Martin Krauskopf
 */
public class CreatedModifiedFilesTest extends TestBase {

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
    
    public void testCreatedModifiedFiles() throws Exception {
        NbModuleProject project = generateStandaloneModule("module1");
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        cmf.add(cmf.bundleKeyDefaultBundle(LocalizedBundleInfo.NAME, "Much Better Name"));
        cmf.add(cmf.bundleKey("src/custom.properties", "some.property", "some value"));
        cmf.add(cmf.addLoaderSection("org/example/module1/MyExtLoader"));
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
        NbModuleProject project = generateStandaloneModule("module1");
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
        NbModuleProject project = generateStandaloneModule("module1");
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        Operation op = cmf.bundleKey("src/custom.properties", "some.property", "some value");
        
        assertRelativePath("src/custom.properties", op.getCreatedPaths());
        
        cmf.add(op);
        cmf.run();
        
        EditableProperties ep = Util.loadProperties(FileUtil.toFileObject(file(getWorkDir(), "module1/src/custom.properties")));
        assertEquals("property created", "some value", ep.getProperty("some.property"));
    }
    
    public void testAddLoaderSection() throws Exception {
        NbModuleProject project = generateStandaloneModule("module1");
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        Operation op = cmf.addLoaderSection("org/example/module1/MyExtLoader");
        
        assertRelativePath("manifest.mf", op.getModifiedPaths());
        
        op.run();
        
        EditableManifest em = Util.loadManifest(FileUtil.toFileObject(file(getWorkDir(), "module1/manifest.mf")));
        assertEquals("loader section was added", "Loader", em.getAttribute("OpenIDE-Module-Class", "org/example/module1/MyExtLoader.class"));
    }
    
    public void testAddLookupRegistration() throws Exception {
        NbModuleProject project = generateStandaloneModule("module1");
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        Operation op = cmf.addLookupRegistration(
                "org.example.spi.somemodule.ProvideMe",
                "org.example.module1.ProvideMeImpl");
        
        assertRelativePath("src/META-INF/services/org.example.spi.somemodule.ProvideMe", op.getCreatedPaths());
        
        cmf.add(op);
        cmf.run();
    }
    
    public void testCreateFile() throws Exception {
        NbModuleProject project = generateStandaloneModule("module1");
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        
        String templatePath = "src/org/example/module1/resources/template.html";
        Operation op = cmf.createFile(templatePath, createFile(HTML_CONTENT));
        
        assertRelativePath(templatePath, op.getCreatedPaths());
        
        cmf.add(op);
        cmf.run();
        
        assertFileContent(HTML_CONTENT, new File(getWorkDir(), "module1/" + templatePath));
    }
    
    public void testCreateFileWithSubstitutions() throws Exception {
        NbModuleProject project = generateStandaloneModule("module1");
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        
        String templatePath = "src/org/example/module1/resources/template.html";
        Operation op = cmf.createFileWithSubstitutions(templatePath, createFile(HTML_CONTENT), TOKENS_MAP);
        
        assertRelativePath(templatePath, op.getCreatedPaths());
        
        cmf.add(op);
        cmf.run();
        
        assertFileContent(HTML_CONTENT_TOKENIZED, new File(getWorkDir(), "module1/" + templatePath));
    }
    
    public void testCreateLayerEntry() throws Exception {
        NbModuleProject project = generateStandaloneModule("module1");
        
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        LayerCallback lc = cmf.createLayerEntry(
                "Menu/Tools/org-example-module1-BeepAction.instance",
                null,
                null,
                null,
                null);
        lc.run();
        
        lc = cmf.createLayerEntry(
                "Services/org-example-module1-Module1UI.settings",
                "module1ui.settings",
                null,
                null,
                null);
        Operation layerOp = cmf.layerOperation(lc);
        cmf.add(layerOp);
        assertRelativePath("src/org/example/module1/resources/layer.xml", layerOp.getModifiedPaths());
        
        lc = cmf.createLayerEntry(
                "Menu/Tools/org-example-module1-BlareAction.instance",
                null,
                null,
                null,
                null);
        cmf.add(cmf.layerOperation(lc));
        
        assertRelativePath("src/org/example/module1/resources/layer.xml", cmf.getModifiedPaths());
        cmf.run();
        
        BufferedReader reader = new BufferedReader(new FileReader(
                new File(getWorkDir(), "module1/src/org/example/module1/resources/layer.xml")));
        String[] supposedContent = new String[] {
            "<filesystem>",
                    "<folder name=\"Menu\">",
                    "<folder name=\"Tools\">",
                    "<file name=\"org-example-module1-BeepAction.instance\"/>",
                    "<file name=\"org-example-module1-BlareAction.instance\"/>",
                    "</folder>",
                    "</folder>",
                    "<folder name=\"Services\">",
                    "<file name=\"org-example-module1-Module1UI.settings\" url=\"module1ui.settings\"/>",
                    "</folder>",
                    "</filesystem>"
        };
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
        
        assertEquals("content of layer", Arrays.asList(supposedContent), actualContent);
    }
    
    private void assertRelativePath(String expectedPath, String[] paths) {
        assertEquals("one path", 1, paths.length);
        assertEquals("created, modified paths", expectedPath, paths[0]);
    }
    
    private void assertRelativePath(String expectedPath, SortedSet paths) {
        String[] s = new String[paths.size()];
        assertRelativePath(expectedPath, (String[]) paths.toArray(s));
    }
    
    private void assertRelativePaths(String[] expectedPaths, SortedSet paths) {
        assertEquals("created, modified paths", Arrays.asList(expectedPaths).toString(), paths.toString());
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
    
    private void assertFileContent(String[] content, File file) throws IOException {
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
    
}

