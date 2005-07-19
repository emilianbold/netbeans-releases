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

import java.io.IOException;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.ui.customizer.ComponentFactory.PublicPackagesTableModel;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

// XXX mkrauskopf: don't use libs/xerces for testing purposes of apisupport
// since it could fail with a new version of xerces lib! Generate or create some
// testing modules in apisupport testing data section instead.

/**
 * Tests {@link SingleModuleProperties}. Actually also for some classes which
 * SuiteProperties utilizes - which doesn't mean they shouldn't be tested
 * individually :)
 *
 * @author Martin Krauskopf
 */
public class SingleModulePropertiesTest extends TestBase {
    
    private FileObject suiteRepoFO;
    private FileObject suite2FO;
    
    public SingleModulePropertiesTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
        suiteRepoFO = FileUtil.toFileObject(copyFolder(extexamplesF));
        suite2FO = suiteRepoFO.getFileObject("suite2");
    }
    
    /** Tests few basic properties to be sure that loading works. */
    public void testThatBasicPropertiesAreLoaded() throws Exception {
        SingleModuleProperties props = loadProperties(suite2FO.getFileObject("misc-project"),
                "src/org/netbeans/examples/modules/misc/Bundle.properties");
        
        assertNotNull(props.getActivePlatform());
        assertNotNull("loading bundle info", props.getBundleInfo());
        assertEquals("cnb", "org.netbeans.examples.modules.misc", props.getCodeNameBase());
        assertNull("no impl. version", props.getImplementationVersion());
        assertTrue("jar file", props.getJarFile().endsWith("org-netbeans-examples-modules-misc.jar"));
        assertEquals("major release version", "1", props.getMajorReleaseVersion());
        assertEquals("spec. version", "1.0", props.getSpecificationVersion());
        assertTrue("suite directory", props.getSuiteDirectory().endsWith("suite2"));
    }
    
    public void testGetPublicPackages() throws Exception {
        // misc-project properties
        SingleModuleProperties props = loadProperties(suite2FO.getFileObject("misc-project"),
                "src/org/netbeans/examples/modules/misc/Bundle.properties");
        PublicPackagesTableModel pptm = props.getPublicPackagesModel();
        assertEquals("number of available public packages", 1, pptm.getRowCount());
        assertEquals("number of selected public packages", 1, pptm.getSelectedPackages().length);
        
        // libs/xerces properties
        props = loadProperties(nbroot.getFileObject("libs/xerces"),
                "src/org/netbeans/libs/xerces/Bundle.properties");
        pptm = props.getPublicPackagesModel();
        assertEquals("number of available public packages", 38, pptm.getRowCount());
        assertEquals("number of selected public packages", 38, pptm.getSelectedPackages().length);
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
    
    private SingleModuleProperties loadProperties(FileObject dirFO, String propsRelPath) throws IOException {
//        long start = System.currentTimeMillis();
        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(dirFO);
//        System.err.println("Loading of project " + FileUtil.toFile(dirFO).getAbsolutePath() + ": " + (System.currentTimeMillis() - start) + "msec");
        FileObject bundleF0 = FileUtil.toFileObject(
                file(FileUtil.toFile(dirFO), propsRelPath));
//        start = System.currentTimeMillis();
        LocalizedBundleInfo locInfo = LocalizedBundleInfo.load(bundleF0);
        SingleModuleProperties props = new SingleModuleProperties(
                p.getHelper(),
                p.evaluator(),
                (SuiteProvider) p.getLookup().lookup(SuiteProvider.class),
                false,
                locInfo);
//        System.err.println("Loading of properties: " + (System.currentTimeMillis() - start) + "msec");
        return props;
    }
}
