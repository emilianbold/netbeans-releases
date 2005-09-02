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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.EditableManifest;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleTypeProvider;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.ui.customizer.ComponentFactory.PublicPackagesTableModel;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;

// XXX mkrauskopf: don't use libs/xerces for testing purposes of apisupport
// since it could fail with a new version of xerces lib! Generate or create some
// testing modules in apisupport testing data section instead.

/**
 * Tests {@link SingleModuleProperties}. Actually also for some classes which
 * SingleModuleProperties utilizes - which doesn't mean they shouldn't be tested
 * individually :)
 *
 * @author Martin Krauskopf
 */
public class SingleModulePropertiesTest extends TestBase {
    
    public SingleModulePropertiesTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
    }
    
    /** Tests few basic properties to be sure that loading works. */
    public void testThatBasicPropertiesAreLoaded() throws Exception {
        NbModuleProject p = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        SingleModuleProperties props = loadProperties(p);
        assertNotNull(props.getActivePlatform());
        assertNotNull("loading bundle info", props.getBundleInfo());
        assertEquals("display name", "Testing Module", props.getBundleInfo().getDisplayName());
        assertEquals("cnb", "org.example.module1", props.getCodeNameBase());
        assertNull("no impl. version", props.getImplementationVersion());
        assertTrue("jar file", props.getJarFile().endsWith("org-example-module1.jar"));
        assertEquals("major release version", null, props.getMajorReleaseVersion());
        assertEquals("spec. version", "1.0", props.getSpecificationVersion());
    }
    
    public void testThatPropertiesAreRefreshed() throws Exception {
        NbModuleProject p = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        SingleModuleProperties props = loadProperties(p);
        assertEquals("spec. version", "1.0", props.getSpecificationVersion());
        assertEquals("display name", "Testing Module", props.getBundleInfo().getDisplayName());
        assertEquals("number of dependencies", 0, props.getDependenciesListModel().getSize());
        
        // silently change manifest
        InputStream is = new FileInputStream(props.getManifestFile());
        EditableManifest em = new EditableManifest();
        try {
            em = new EditableManifest(is);
        } finally {
            is.close();
        }
        em.setAttribute(ManifestManager.OPENIDE_MODULE_SPECIFICATION_VERSION, "1.1", null);
        OutputStream os = new FileOutputStream(props.getManifestFile());
        try {
            em.write(os);
        } finally {
            os.close();
        }
        
        // silently change bundle
        EditableProperties ep = new EditableProperties();
        is = new FileInputStream(props.getBundleInfo().getPath());
        try {
            ep.load(is);
        } finally {
            is.close();
        }
        ep.setProperty(LocalizedBundleInfo.NAME, "Miscellaneous");
        os = new FileOutputStream(props.getBundleInfo().getPath());
        try {
            ep.store(os);
        } finally {
            os.close();
        }
        
        // modify project.xml
        final ProjectXMLManager pxm = new ProjectXMLManager(p.getHelper());
        ModuleEntry me = p.getModuleList().getEntry(
                "org.netbeans.modules.java.project");
        final ModuleDependency md = new ModuleDependency(me, "1", null, false, true);
        Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
            public Object run() throws IOException {
                pxm.addDependency(md);
                return Boolean.TRUE;
            }
        });
        assertTrue("adding dependencies", result.booleanValue());
        ProjectManager.getDefault().saveProject(p);
        
        // simple reload
        props.refresh(getModuleType(p), getSuiteProvider(p));
        
        // check that manifest and bundle has been reloaded
        assertEquals("spec. version", "1.1", props.getSpecificationVersion());
        assertEquals("display name should be changed", "Miscellaneous", props.getBundleInfo().getDisplayName());
        assertEquals("number of dependencies", 1, props.getDependenciesListModel().getSize());
    }
    
    public void testThatPropertiesListen() throws Exception {
        NbModuleProject p = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        SingleModuleProperties props = loadProperties(p);
        assertEquals("display name from ProjectInformation", "Testing Module",
                ProjectUtils.getInformation(p).getDisplayName());
        assertEquals("display name from LocalizedBundleInfo", "Testing Module",
                props.getBundleInfo().getDisplayName());
        
        FileObject bundleFO = FileUtil.toFileObject(new File(props.getBundleInfo().getPath()));
        EditableProperties bundleEP = Util.loadProperties(bundleFO);
        bundleEP.setProperty(LocalizedBundleInfo.NAME, "Miscellaneous");
        // let's fire a change
        Util.storeProperties(bundleFO, bundleEP);
        
        // display name should be refreshed
        assertEquals("display name was refreshed in ProjectInformation", "Miscellaneous",
                ProjectUtils.getInformation(p).getDisplayName());
        assertEquals("display name was refreshed in LocalizedBundleInfo", "Miscellaneous",
                props.getBundleInfo().getDisplayName());
    }
    
    public void testGetPublicPackages() throws Exception {
        final NbModuleProject p = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        FileUtil.createData(p.getSourceDirectory(), "org/example/module1/One.java");
        FileUtil.createData(p.getSourceDirectory(), "org/example/module1/resources/Two.java");
        
        // apply and save project
        Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
            public Object run() throws IOException {
                ProjectXMLManager pxm = new ProjectXMLManager(p.getHelper());
                String[] newPP = new String[] { "org.example.module1" };
                pxm.replacePublicPackages(newPP);
                return Boolean.TRUE;
            }
        });
        assertTrue("replace public packages", result.booleanValue());
        ProjectManager.getDefault().saveProject(p);
        
        SingleModuleProperties props = loadProperties(p);
        PublicPackagesTableModel pptm = props.getPublicPackagesModel();
        assertEquals("number of available public packages", 2, pptm.getRowCount());
        assertEquals("number of selected public packages", 1, pptm.getSelectedPackages().length);
        
        // libs/xerces properties
        NbModuleProject libP = (NbModuleProject) ProjectManager.getDefault().findProject(nbroot.getFileObject("libs/xerces"));
        props = loadProperties(libP);
        pptm = props.getPublicPackagesModel();
        assertEquals("number of available public packages", 38, pptm.getRowCount());
        assertEquals("number of selected public packages", 38, pptm.getSelectedPackages().length);
    }
    
    public void testThatProjectWithoutBundleDoesNotThrowNPE_61469() throws Exception {
        FileObject pFO = TestBase.generateStandaloneModuleDirectory(getWorkDir(), "module1");
        FileObject propsFO = FileUtil.toFileObject(new File(getWorkDir(),
                "module1/src/org/example/module1/resources/Bundle.properties"));
        propsFO.delete();
        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(pFO);
        SingleModuleProperties props = loadProperties(p);
        props.refresh(getModuleType(p), getSuiteProvider(p));
    }
    
    public void testThatManifestFormattingIsNotMessedUp_61248() throws Exception {
        NbModuleProject p = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        EditableManifest em = Util.loadManifest(p.getManifestFile());
        em.setAttribute(ManifestManager.OPENIDE_MODULE_REQUIRES, "\n" +
                "  org.openide.execution.ExecutionEngine,\n" +
                "  org.openide.windows.IOProvider", null);
        Util.storeManifest(p.getManifestFile(), em);
        String before = TestBase.slurp(p.getManifestFile());
        
        SingleModuleProperties props = loadProperties(p);
        // two lines bellow are ensured by CustomizerVersioning - let's simulate it
        props.setImplementationVersion("");
        props.setProvidedTokens("");
        props.storeProperties();
        ProjectManager.getDefault().saveProject(p);
        String after = TestBase.slurp(p.getManifestFile());
        
        assertEquals("the same content", before, after);
    }
    
    public void testNiceFormattingForRequiredTokensInManifest_63516() throws Exception {
        NbModuleProject p = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        EditableManifest em = Util.loadManifest(p.getManifestFile());
        em.setAttribute(ManifestManager.OPENIDE_MODULE_REQUIRES, "\n" +
                "  org.openide.execution.ExecutionEngine,\n" +
                "  org.openide.windows.IOProvider", null);
        Util.storeManifest(p.getManifestFile(), em);
        
        SingleModuleProperties props = loadProperties(p);
        props.getRequiredTokenListModel().addToken("org.netbeans.api.javahelp.Help");
        // two lines bellow are ensured by CustomizerVersioning - let's simulate it
        props.setImplementationVersion("");
        props.setProvidedTokens("");
        props.storeProperties();
        ProjectManager.getDefault().saveProject(p);
        String real = TestBase.slurp(p.getManifestFile());
        String expected = "Manifest-Version: 1.0\n" +
                "OpenIDE-Module: org.example.module1\n" +
                "OpenIDE-Module-Layer: org/example/module1/resources/layer.xml\n" +
                "OpenIDE-Module-Localizing-Bundle: org/example/module1/resources/Bundle.properties\n" +
                "OpenIDE-Module-Requires: \n" +
                "  org.netbeans.api.javahelp.Help,\n" +
                "  org.openide.execution.ExecutionEngine,\n" +
                "  org.openide.windows.IOProvider\n" +
                "OpenIDE-Module-Specification-Version: 1.0\n\n";
        
        assertEquals("expected content", expected, real);
        
        props.getRequiredTokenListModel().removeToken("org.openide.execution.ExecutionEngine");
        props.getRequiredTokenListModel().removeToken("org.netbeans.api.javahelp.Help");
        props.storeProperties();
        ProjectManager.getDefault().saveProject(p);
        real = TestBase.slurp(p.getManifestFile());
        expected = "Manifest-Version: 1.0\n" +
                "OpenIDE-Module: org.example.module1\n" +
                "OpenIDE-Module-Layer: org/example/module1/resources/layer.xml\n" +
                "OpenIDE-Module-Localizing-Bundle: org/example/module1/resources/Bundle.properties\n" +
                "OpenIDE-Module-Requires: org.openide.windows.IOProvider\n" +
                "OpenIDE-Module-Specification-Version: 1.0\n\n";
        
        assertEquals("expected content", expected, real);
    }
    
    public void testaddNonEmptyPackages() throws Exception {
        FileObject srcDir = FileUtil.toFileObject(getWorkDir()).createFolder("src");
        FileUtil.createData(srcDir, "pkg1/Clazz1.java");
        FileUtil.createData(srcDir, "pkg1/Clazz2.java");
        FileUtil.createData(srcDir, "pkg2/CVS/#1.20#Clazz1.java");
        FileUtil.createData(srcDir, "pkg2/Clazz1.java");
        FileUtil.createData(srcDir, "pkg2/deeper/Clazz1.java");
        FileUtil.createData(srcDir, "pkg2/deeper/and/deeper/Clazz1.java");
        Set packages = new HashSet();
        SingleModuleProperties.addNonEmptyPackages(packages, srcDir, "java", false);
        assertEquals("two packages", 4, packages.size());
        assertTrue("pkg1", packages.remove("pkg1"));
        assertTrue("pkg2", packages.remove("pkg2"));
        assertTrue("pkg2.deeper", packages.remove("pkg2.deeper"));
        assertTrue("pkg2.deeper.and.deeper", packages.remove("pkg2.deeper.and.deeper"));
    }
    
//    public void testReloadNetBeansModulueListSpeedHid() throws Exception {
//        long startTotal = System.currentTimeMillis();
//        SingleModuleProperties props = loadProperties(nbroot.getFileObject("apisupport/project"),
//                "src/org/netbeans/modules/apisupport/project/Bundle.properties");
//        long start = System.currentTimeMillis();
//        props.reloadModuleListInfo();
//        System.err.println("Reloading of module list: " + (System.currentTimeMillis() - start) + "msec");
//        System.err.println("Total time: " + (System.currentTimeMillis() - startTotal) + "msec");
//    }
//
//    public void testReloadBinaryModulueListSpeedHid() throws Exception {
//        long startTotal = System.currentTimeMillis();
//        SingleModuleProperties props = loadProperties(suite2FO.getFileObject("misc-project"),
//                "src/org/netbeans/examples/modules/misc/Bundle.properties");
//        long start = System.currentTimeMillis();
//        props.reloadModuleListInfo();
//        System.err.println("Time to reload module list: " + (System.currentTimeMillis() - start) + "msec");
//        System.err.println("Total time: " + (System.currentTimeMillis() - startTotal) + "msec");
//    }
    
    private static SingleModuleProperties loadProperties(NbModuleProject project) throws IOException {
        return new SingleModuleProperties(project.getHelper(), project.evaluator(),
                getSuiteProvider(project), getModuleType(project), project.getBundleInfo());
    }
    
    private static NbModuleTypeProvider.NbModuleType getModuleType(Project p) {
        NbModuleTypeProvider nmtp = (NbModuleTypeProvider) p.getLookup().lookup(NbModuleTypeProvider.class);
        return nmtp.getModuleType();
    }
    
    private static SuiteProvider getSuiteProvider(Project p) {
        return (SuiteProvider) p.getLookup().lookup(SuiteProvider.class);
    }
    
}
