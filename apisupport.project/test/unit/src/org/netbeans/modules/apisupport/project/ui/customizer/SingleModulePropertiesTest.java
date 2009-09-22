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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.EditableManifest;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectGenerator;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.CustomizerComponentFactory.PublicPackagesTableModel;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.util.Mutex.ExceptionAction;

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
        noDataDir = true;
        clearWorkDir();
        super.setUp();
    }
    
    /** Tests few basic properties to be sure that loading works. */
    public void testThatBasicPropertiesAreLoaded() throws Exception {
        NbModuleProject p = generateStandaloneModule("module1");
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
        NbModuleProject p = generateStandaloneModule("module1");
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
        EditableProperties ep = new EditableProperties(false);
        is = new FileInputStream(props.getBundleInfo().getPaths()[0]);
        try {
            ep.load(is);
        } finally {
            is.close();
        }
        ep.setProperty(LocalizedBundleInfo.NAME, "Miscellaneous");
        os = new FileOutputStream(props.getBundleInfo().getPaths()[0]);
        try {
            ep.store(os);
        } finally {
            os.close();
        }
        
        // modify project.xml
        Util.addDependency(p, "org.netbeans.modules.java.project", "1", null, false);
        ProjectManager.getDefault().saveProject(p);
        
        simulatePropertiesOpening(props, p);
        
        // check that manifest and bundle has been reloaded
        assertEquals("spec. version", "1.1", props.getSpecificationVersion());
        assertEquals("display name should be changed", "Miscellaneous", props.getBundleInfo().getDisplayName());
        assertEquals("number of dependencies", 1, props.getDependenciesListModel().getSize());
    }
    
    public void testThatPropertiesListen() throws Exception {
        NbModuleProject p = generateStandaloneModule("module1");
        SingleModuleProperties props = loadProperties(p);
        assertEquals("display name from ProjectInformation", "Testing Module",
                ProjectUtils.getInformation(p).getDisplayName());
        assertEquals("display name from LocalizedBundleInfo", "Testing Module",
                props.getBundleInfo().getDisplayName());
        
        FileObject bundleFO = FileUtil.toFileObject(props.getBundleInfo().getPaths()[0]);
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
        final NbModuleProject p = generateStandaloneModule("module1");
        FileUtil.createData(p.getSourceDirectory(), "org/example/module1/One.java");
        FileUtil.createData(p.getSourceDirectory(), "org/example/module1/resources/Two.java");
        
        // apply and save project
        boolean result = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
            public Boolean run() throws IOException {
                ProjectXMLManager pxm = new ProjectXMLManager(p);
                pxm.replacePublicPackages(Collections.singleton("org.example.module1"));
                return true;
            }
        });
        assertTrue("replace public packages", result);
        ProjectManager.getDefault().saveProject(p);
        
        SingleModuleProperties props = loadProperties(p);
        PublicPackagesTableModel pptm = props.getPublicPackagesModel();
        assertEquals("number of available public packages", 2, pptm.getRowCount());
        assertEquals("number of selected public packages", 1, pptm.getSelectedPackages().size());
    }

    @RandomlyFails // not random, cannot be run in binary dist, requires sources; XXX test against fake platform
    public void testGetPublicPackagesForNBOrg() throws Exception {
        // libs.xerces properties
        NbModuleProject libP = (NbModuleProject) ProjectManager.getDefault().findProject(nbRoot().getFileObject("libs.xerces"));
        SingleModuleProperties props = loadProperties(libP);
        PublicPackagesTableModel pptm = props.getPublicPackagesModel();
        assertEquals("number of available public packages", 38, pptm.getRowCount());
    }
    
    public void testThatProjectWithoutBundleDoesNotThrowNPE_61469() throws Exception {
        FileObject pFO = TestBase.generateStandaloneModuleDirectory(getWorkDir(), "module1");
        FileObject propsFO = FileUtil.toFileObject(new File(getWorkDir(),
                "module1/src/org/example/module1/resources/Bundle.properties"));
        propsFO.delete();
        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(pFO);
        SingleModuleProperties props = loadProperties(p);
        simulatePropertiesOpening(props, p);
    }

    public void testThatManifestFormattingIsNotMessedUp_61248() throws Exception {
        NbModuleProject p = generateStandaloneModule("module1");
        EditableManifest em = Util.loadManifest(p.getManifestFile());
        em.setAttribute(ManifestManager.OPENIDE_MODULE_REQUIRES, "\n" +
                "  org.openide.execution.ExecutionEngine,\n" +
                "  org.openide.windows.IOProvider", null);
        Util.storeManifest(p.getManifestFile(), em);
        String before = TestBase.slurp(p.getManifestFile());

        SingleModuleProperties props = loadProperties(p);
        // two lines below are ensured by CustomizerVersioning - let's simulate it
        props.setImplementationVersion("");
        props.setProvidedTokens("");
        props.storeProperties();
        ProjectManager.getDefault().saveProject(p);
        String after = TestBase.slurp(p.getManifestFile());

        assertEquals("the same content", before, after);
    }

    public void testNiceFormattingForRequiredTokensInManifest_63516() throws Exception {
        NbModuleProject p = generateStandaloneModule("module1");
        EditableManifest em = Util.loadManifest(p.getManifestFile());
        em.setAttribute(ManifestManager.OPENIDE_MODULE_REQUIRES, "\n" +
                "  org.openide.execution.ExecutionEngine,\n" +
                "  org.openide.windows.IOProvider", null);
        Util.storeManifest(p.getManifestFile(), em);

        SingleModuleProperties props = loadProperties(p);
        props.getRequiredTokenListModel().addToken("org.netbeans.api.javahelp.Help");
        // two lines below are ensured by CustomizerVersioning - let's simulate it
        props.setImplementationVersion("");
        props.setProvidedTokens("");
        props.storeProperties();
        ProjectManager.getDefault().saveProject(p);
        String real = TestBase.slurp(p.getManifestFile());
        String newline = System.getProperty("line.separator");
        String expected = "Manifest-Version: 1.0" + newline +
                "OpenIDE-Module: org.example.module1" + newline +
                "OpenIDE-Module-Layer: org/example/module1/resources/layer.xml" + newline +
                "OpenIDE-Module-Localizing-Bundle: org/example/module1/resources/Bundle.properties" + newline +
                "OpenIDE-Module-Requires: " + newline +
                "  org.netbeans.api.javahelp.Help," + newline +
                "  org.openide.execution.ExecutionEngine," + newline +
                "  org.openide.windows.IOProvider" + newline +
                "OpenIDE-Module-Specification-Version: 1.0" + newline + newline;

        assertEquals("expected content", expected, real);

        props.getRequiredTokenListModel().removeToken("org.openide.execution.ExecutionEngine");
        props.getRequiredTokenListModel().removeToken("org.netbeans.api.javahelp.Help");
        props.storeProperties();
        ProjectManager.getDefault().saveProject(p);
        real = TestBase.slurp(p.getManifestFile());
        expected = "Manifest-Version: 1.0" + newline +
                "OpenIDE-Module: org.example.module1" + newline +
                "OpenIDE-Module-Layer: org/example/module1/resources/layer.xml" + newline +
                "OpenIDE-Module-Localizing-Bundle: org/example/module1/resources/Bundle.properties" + newline +
                "OpenIDE-Module-Requires: org.openide.windows.IOProvider" + newline +
                "OpenIDE-Module-Specification-Version: 1.0" + newline + newline;

        assertEquals("expected content", expected, real);
    }
    
    public void testAvailablePublicPackages() throws Exception {
        Map<String,String> contents = new HashMap<String,String>();
        contents.put("lib/pkg/Clazz3.class", "");
        contents.put("lib/pkg2/Clazz4.class", "");
        contents.put("1.0/oldlib/Clazz5.class", ""); // #72669
        File jar = new File(getWorkDir(), "some.jar");
        createJar(jar, contents, new Manifest());
        SuiteProject sweet = generateSuite("sweet");
        File moduleDir = new File(getWorkDir(), "module");
        NbModuleProjectGenerator.createSuiteLibraryModule(
                moduleDir, "module", "Module", "module/Bundle.properties",
                sweet.getProjectDirectoryFile(), null, new File[] {jar});
        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(moduleDir));
        FileObject srcDir = p.getProjectDirectory().getFileObject("src");
        FileUtil.createData(srcDir, "pkg1/Clazz1.java");
        FileUtil.createData(srcDir, "pkg1/Clazz2.java");
        FileUtil.createData(srcDir, "pkg2/CVS/#1.20#Clazz1.java");
        FileUtil.createData(srcDir, "pkg2/Clazz1.java");
        FileUtil.createData(srcDir, "pkg2/deeper/Clazz1.java");
        FileUtil.createData(srcDir, "pkg2/deeper/and/deeper/Clazz1.java");
        FileUtil.createData(srcDir, ".broken/Clazz.java"); // #72669
        assertEquals(Arrays.asList("lib.pkg", "lib.pkg2", "pkg1", "pkg2", "pkg2.deeper", "pkg2.deeper.and.deeper"),
                new ArrayList<String>(SingleModuleProperties.getInstance(p).getAvailablePublicPackages()));
    }
    
    public void testPublicPackagesAreUpToDate_63561() throws Exception {
        SuiteProject suite1 = generateSuite("suite1");
        final NbModuleProject p = TestBase.generateSuiteComponent(suite1, "module1a");
        FileUtil.createData(p.getSourceDirectory(), "org/example/module1a/Dummy.java");
        SingleModuleProperties props = loadProperties(p);
        PublicPackagesTableModel pptm = props.getPublicPackagesModel();
        assertEquals("number of available public packages", 1, pptm.getRowCount());
        assertEquals("number of selected public packages", 0, pptm.getSelectedPackages().size());
        assertEquals("no public packages in the ModuleEntry", 0, props.getModuleList().getEntry("org.example.module1a").getPublicPackages().length);
        
        // apply and save project
        boolean result = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
            public Boolean run() throws IOException {
                ProjectXMLManager pxm = new ProjectXMLManager(p);
                pxm.replacePublicPackages(Collections.singleton("org.example.module1a"));
                return true;
            }
        });
        assertTrue("replace public packages", result);
        ProjectManager.getDefault().saveProject(p);
        
        simulatePropertiesOpening(props, p);
        
        pptm = props.getPublicPackagesModel();
        assertEquals("number of available public packages", 1, pptm.getRowCount());
        assertEquals("number of selected public packages", 1, pptm.getSelectedPackages().size());
        assertEquals("one public packages in the ModuleEntry", 1, props.getModuleList().getEntry("org.example.module1a").getPublicPackages().length);
    }
    
    /** Test that a module doesn't offer itself in its dependency list. */
    public void testThatTheModuleDoesNotOfferItself_61232() throws Exception {
        NbModuleProject p = generateStandaloneModule("module1");
        SingleModuleProperties props = loadProperties(p);
        for (ModuleDependency dependency : props.getUniverseDependencies(true)) {
            ModuleEntry me = dependency.getModuleEntry();
            assertFalse("module doesn't offer itself in its dependency list: " + p.getCodeNameBase(),
                    p.getCodeNameBase().equals(me.getCodeNameBase()));
        }
    }
    
    public void testGetAvailableFriends() throws Exception {
        // standalone
        NbModuleProject standAlone = generateStandaloneModule("module1");
        SingleModuleProperties props = loadProperties(standAlone);
        assertEquals("There are no friends for standalone module.", 0, props.getAvailableFriends().length);
        
        // suitecomponent
        SuiteProject suite1 = generateSuite("suite1");
        TestBase.generateSuiteComponent(suite1, "component1");
        NbModuleProject component2 = TestBase.generateSuiteComponent(suite1, "component2");
        TestBase.generateSuiteComponent(suite1, "component3");
        props = loadProperties(component2);
        assertEquals("There are two available friends for component2.", 2, props.getAvailableFriends().length);
    }

    @RandomlyFails // not random, cannot be run in binary dist, requires sources; XXX test against fake platform
    public void testGetAvailableFriendsForNBOrg() throws Exception {
        // netbeans.org
        Project javaProject = ProjectManager.getDefault().findProject(nbRoot().getFileObject("java.project"));
        SingleModuleProperties props = loadProperties((NbModuleProject) javaProject);
        assertTrue("There are two available friends for component2.", props.getAvailableFriends().length > 50);
    }

    public void testSimulateLocalizedBundlePackageRefactoring() throws Exception {
        NbModuleProject p = generateStandaloneModule("module1");
        SingleModuleProperties props = loadProperties(p);
        assertEquals("display name from ProjectInformation", "Testing Module",
                ProjectUtils.getInformation(p).getDisplayName());
        assertEquals("display name from LocalizedBundleInfo", "Testing Module",
                props.getBundleInfo().getDisplayName());
        
        // rename package
        FileObject pDir = p.getProjectDirectory();
        FileObject pkg = pDir.getFileObject("src/org/example/module1");
        FileLock lock = pkg.lock();
        pkg.rename(lock, "module1Renamed", null);
        lock.releaseLock();
        System.gc();    // no more random
        FileObject manifestFO = pDir.getFileObject("manifest.mf");

        // change manifest
        EditableManifest mf = Util.loadManifest(manifestFO);
        mf.setAttribute(ManifestManager.OPENIDE_MODULE_LOCALIZING_BUNDLE, "org/example/module1Renamed/resources/Bundle.properties", null);
        Util.storeManifest(manifestFO, mf);
        simulatePropertiesOpening(props, p);
        
        // make sure that properties are not damaged
        assertEquals("display name was refreshed in ProjectInformation", "Testing Module",
                ProjectUtils.getInformation(p).getDisplayName());
        assertEquals("display name was refreshed in LocalizedBundleInfo", "Testing Module",
                props.getBundleInfo().getDisplayName());
    }
    
    public void testSimulateIllLocalizedBundlePackageRefactoring_67961() throws Exception {
        NbModuleProject p = generateStandaloneModule("module1");
        SingleModuleProperties props = loadProperties(p);
        assertEquals("display name from ProjectInformation", "Testing Module",
                ProjectUtils.getInformation(p).getDisplayName());
        assertEquals("display name from LocalizedBundleInfo", "Testing Module",
                props.getBundleInfo().getDisplayName());
        
        // change manifest (will fire a change event before the package is actually renamed)
        FileObject pDir = p.getProjectDirectory();
        FileObject manifestFO = pDir.getFileObject("manifest.mf");
        EditableManifest mf = Util.loadManifest(manifestFO);
        mf.setAttribute(ManifestManager.OPENIDE_MODULE_LOCALIZING_BUNDLE, "org/example/module1Renamed/resources/Bundle.properties", null);
        Util.storeManifest(manifestFO, mf);
        
        // rename package
        FileObject pkg = pDir.getFileObject("src/org/example/module1");
        FileLock lock = pkg.lock();
        pkg.rename(lock, "module1Renamed", null);
        lock.releaseLock();
        
        simulatePropertiesOpening(props, p);
        
        // make sure that properties are not damaged
        assertEquals("display name was refreshed in ProjectInformation", "Testing Module",
                ProjectUtils.getInformation(p).getDisplayName());
        assertEquals("display name was refreshed in LocalizedBundleInfo", "Testing Module",
                props.getBundleInfo().getDisplayName());
    }
    
    public void testResolveFile() throws Exception {
        NbModuleProject p = generateStandaloneModule("module1");
        SingleModuleProperties props = loadProperties(p);
        assertTrue("manifest exist", props.evaluateFile("manifest.mf").exists());
        assertTrue("manifest exist", props.evaluateFile(props.getProjectDirectory() + "/manifest.mf").exists());
        assertTrue("manifest exist", props.evaluateFile("${basedir}/manifest.mf").exists());
        assertFalse("non-existing file", props.evaluateFile("non-existing").exists());
        assertFalse("invalid reference", props.evaluateFile("${invalid-reference}/manifest.mf").exists());
    }
    
    public void testThatFilesAreNotTouched_67249() throws Exception {
        NbModuleProject p = generateStandaloneModule("module1");
        FileUtil.createData(p.getSourceDirectory(), "org/example/module1/One.java");
        FileUtil.createData(p.getSourceDirectory(), "org/example/module1/resources/Two.java");
        SingleModuleProperties props = loadProperties(p);
        
        // times before change
        FileObject bundle = FileUtil.toFileObject(props.getBundleInfo().getPaths()[0]);
        FileObject mf = p.getManifestFile();
        long mfTime = mf.lastModified().getTime();
        long bundleTime = bundle.lastModified().getTime();
        
        // be sure we are not too fast
        Thread.sleep(2000);
        
        // select a package
        props.getPublicPackagesModel().setValueAt(Boolean.TRUE, 0, 0);
        props.storeProperties();
        
        // compare with times after change
        assertEquals("time for manifest has not changed", mfTime, mf.lastModified().getTime());
        assertEquals("time for bundle has not changed", bundleTime, bundle.lastModified().getTime());
    }
    
    public void testGetUniverseDependencies() throws Exception {
        SuiteProject suite = generateSuite("suite");
        
        NbModuleProject testPrj = generateSuiteComponent(suite, "testPrj");
        
        NbModuleProject apiPrj = generateSuiteComponent(suite, "apiPrj");
        FileUtil.createData(apiPrj.getProjectDirectory(), "src/api/Util.java");
        SingleModuleProperties apiPrjProps = SingleModulePropertiesTest.loadProperties(apiPrj);
        apiPrjProps.getPublicPackagesModel().setValueAt(Boolean.TRUE, 0, 0);
        apiPrjProps.storeProperties();
        ProjectManager.getDefault().saveProject(apiPrj);
        
        NbModuleProject friendPrj = generateSuiteComponent(suite, "friendPrj");
        FileUtil.createData(friendPrj.getProjectDirectory(), "src/friend/Karel.java");
        SingleModuleProperties friendPrjProps = SingleModulePropertiesTest.loadProperties(friendPrj);
        friendPrjProps.getPublicPackagesModel().setValueAt(Boolean.TRUE, 0, 0);
        friendPrjProps.getFriendListModel().addFriend("org.example.testPrj");
        friendPrjProps.storeProperties();
        ProjectManager.getDefault().saveProject(friendPrj);
        
        generateSuiteComponent(suite, "nonApiPrj");
        ModuleEntry apiPrjME = ModuleList.getModuleList(testPrj.getProjectDirectoryFile()).getEntry("org.example.apiPrj");
        ModuleDependency apiPrjDep = new ModuleDependency(apiPrjME);
        ModuleEntry friendPrjME = ModuleList.getModuleList(testPrj.getProjectDirectoryFile()).getEntry("org.example.friendPrj");
        ModuleDependency friendPrjDep = new ModuleDependency(friendPrjME);
        ModuleEntry nonApiPrjME = ModuleList.getModuleList(testPrj.getProjectDirectoryFile()).getEntry("org.example.nonApiPrj");
        ModuleDependency nonApiPrjDep = new ModuleDependency(nonApiPrjME);
        
        SingleModuleProperties testProps = SingleModulePropertiesTest.loadProperties(testPrj);
        Set allDeps = testProps.getUniverseDependencies(false);
        Set allDepsFilterExcluded = testProps.getUniverseDependencies(true);
        Set apiDeps = testProps.getUniverseDependencies(false, true);
        Set apiDepsFilterExcluded = testProps.getUniverseDependencies(true, true);
        
        assertTrue(allDeps.contains(apiPrjDep));
        assertTrue(allDeps.contains(friendPrjDep));
        assertTrue(allDeps.contains(nonApiPrjDep));
        
        assertTrue(allDepsFilterExcluded.contains(apiPrjDep));
        assertTrue(allDepsFilterExcluded.contains(friendPrjDep));
        assertTrue(allDepsFilterExcluded.contains(nonApiPrjDep));
        
        assertTrue(apiDeps.contains(apiPrjDep));
        assertTrue(apiDeps.contains(friendPrjDep));
        assertFalse(apiDeps.contains(nonApiPrjDep));
        
        assertTrue(apiDepsFilterExcluded.contains(apiPrjDep));
        assertTrue(apiDepsFilterExcluded.contains(friendPrjDep));
        assertFalse(apiDepsFilterExcluded.contains(nonApiPrjDep));
        
        // #72124: check that cluster include/exclude lists do not affect suite components:
        EditableProperties ep = suite.getHelper().getProperties("nbproject/platform.properties");
        ep.setProperty(SuiteProperties.ENABLED_CLUSTERS_PROPERTY, "crazy99"); // should not match any platform modules
        suite.getHelper().putProperties("nbproject/platform.properties", ep);
        ProjectManager.getDefault().saveProject(suite);
        allDepsFilterExcluded = testProps.getUniverseDependencies(true);
        assertTrue(allDepsFilterExcluded.contains(apiPrjDep));
        assertTrue(allDepsFilterExcluded.contains(friendPrjDep));
        assertTrue(allDepsFilterExcluded.contains(nonApiPrjDep));
    }
    
    public void testDefaultPackageIsNotOffered_71532() throws Exception {
        NbModuleProject p = generateStandaloneModule("module1");
        FileUtil.createData(p.getProjectDirectory(), "src/BadInDefault.java");
        FileUtil.createData(p.getProjectDirectory(), "src/org/example/module1/GoodOne.java");
        assertEquals("one non-default valid package", 1, loadProperties(p).getPublicPackagesModel().getRowCount());
    }
    
//    public void testReloadNetBeansModulueListSpeedHid() throws Exception {
//        long startTotal = System.currentTimeMillis();
//        SingleModuleProperties props = loadProperties(nbCVSRoot().getFileObject("apisupport/project"),
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

        public void testGetActivePlatform() throws Exception {
        ProjectManager.mutex().writeAccess(new ExceptionAction<Void>() {
            // saving of platform.properties of the project must be done under
            // PM.mutex() lock (read would in fact suffice too), otherwise
            // there is a race condition between storing properties file
            // and updating project evaluator
            public Void run() throws Exception {
                SuiteProject suite = generateSuite("suite");
                NbModuleProject module = generateSuiteComponent(suite, "module");
                File plaf = new File(getWorkDir(), "plaf");
                makePlatform(plaf, "1.13"); // 6.7 harness
                NbPlatform.addPlatform("plaf", plaf, "Test Platform");
                FileObject platformPropertiesFO = suite.getProjectDirectory().getFileObject("nbproject/platform.properties");
                EditableProperties platformProperties = Util.loadProperties(platformPropertiesFO);
                platformProperties.put("suite.dir", "${basedir}");
                platformProperties.put("nbplatform.active", "plaf");
                Util.storeProperties(platformPropertiesFO, platformProperties);
                SingleModuleProperties props = loadProperties(module);
                NbPlatform platform = props.getActivePlatform();
                assertEquals(plaf, platform.getDestDir());
                return null;
            }
        });
    }


    static SingleModuleProperties loadProperties(NbModuleProject project) throws IOException {
        return new SingleModuleProperties(project.getHelper(), project.evaluator(),
                getSuiteProvider(project), getModuleType(project),
                project.getLookup().lookup(LocalizedBundleInfo.Provider.class));
    }
    
    private static NbModuleProvider.NbModuleType getModuleType(Project p) {
        return p.getLookup().lookup(NbModuleProvider.class).getModuleType();
    }
    
    private static SuiteProvider getSuiteProvider(Project p) {
        return p.getLookup().lookup(SuiteProvider.class);
    }
    
    private static void simulatePropertiesOpening(
            final SingleModuleProperties props, final NbModuleProject p) {
        props.refresh(getModuleType(p), getSuiteProvider(p));
    }
    
}
