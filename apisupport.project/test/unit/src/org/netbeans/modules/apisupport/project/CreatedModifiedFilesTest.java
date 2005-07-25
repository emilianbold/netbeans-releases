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

import java.util.Arrays;
import java.util.SortedSet;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles.Operation;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileUtil;

/**
 * Tests {@link CreatedModifiedFiles}.
 * @author Martin Krauskopf
 */
public class CreatedModifiedFilesTest extends TestBase {
    
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
    }
    
    public CreatedModifiedFilesTest(String name) {
        super(name);
    }
    
    public void testCreatedModifiedFiles() throws Exception {
        NbModuleProject project = generateStandaloneModule("module1");
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(project);
        cmf.add(cmf.bundleKeyDefaultBundle(LocalizedBundleInfo.NAME, "Much Better Name"));
        cmf.add(cmf.bundleKey("src/custom.properties", "some.property", "some value"));
        cmf.add(cmf.addLoaderSection("org/example/module1/MyExtLoader"));
        cmf.add(cmf.addLookupRegistration(
                "org.example.spi.somemodule.ProvideMe",
                "org.example.module1.ProvideMeImpl"));
        
        assertRelativePath(
                "src/META-INF/services/org.example.spi.somemodule.ProvideMe",
                cmf.getCreatedPaths());
        assertRelativePaths(
                new String[] {"manifest.mf", "src/custom.properties", "src/org/example/module1/resources/Bundle.properties"},
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
        
        assertRelativePath("src/custom.properties", op.getModifiedPaths());
        
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
}