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
import java.util.Enumeration;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.ui.customizer.ComponentFactory.PublicPackagesTableModel;
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
        suiteRepoFO = prepareSuiteRepo(extexamples);
        suite2FO = suiteRepoFO.getFileObject("suite2");
    }
    
    /** Tests few basic properties to be sure that loading works. */
    public void testThatBasicPropertiesAreLoaded() throws Exception {
        SingleModuleProperties props = loadProperties(suite2FO.getFileObject("misc-project"),
                "src/org/netbeans/examples/modules/misc/Bundle.properties");
        
        assertNotNull(props.getActivePlatform());
        assertEquals("four properties should is set", 4, props.getBundleProperties().size());
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
    
    private SingleModuleProperties loadProperties(FileObject dirFO, String propsRelPath) throws IOException {
        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(dirFO);
        
        SingleModuleProperties props = new SingleModuleProperties(
                p.getHelper(),
                p.evaluator(),
                (SuiteProvider) p.getLookup().lookup(SuiteProvider.class),
                false,
                propsRelPath); // NOI18N
        return props;
    }
    
    // XXX fastly copied from ProjectXMLManagerTest!!!
    private FileObject prepareSuiteRepo(FileObject what) throws Exception {
        int srcFolderLen = what.getPath().length();
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        // XXX this should be probably be using (TestBase.this.)copyFolder
        for (Enumeration en = what.getFolders(true); en.hasMoreElements(); ) {
            FileObject src = (FileObject) en.nextElement();
            if (src.getName().equals("CVS")) {
                continue;
            }
            FileObject dest = FileUtil.createFolder(workDir, src.getPath().substring(srcFolderLen));
            for (Enumeration en2 = src.getData(false); en2.hasMoreElements(); ) {
                FileObject fo = (FileObject) en2.nextElement();
                FileUtil.copyFile(fo, dest, fo.getName());
            }
        }
        return workDir;
    }
    
}
