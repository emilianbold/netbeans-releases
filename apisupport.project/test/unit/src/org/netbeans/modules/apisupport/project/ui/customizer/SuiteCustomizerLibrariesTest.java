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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.apisupport.project.EditableManifest;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.modules.Dependency;
import org.openide.modules.SpecificationVersion;

/**
 * Tests module dependencies in a suite.
 * @author Jesse Glick
 */
public class SuiteCustomizerLibrariesTest extends NbTestCase {
    
    public SuiteCustomizerLibrariesTest(String name) {
        super(name);
    }
    
    private NbPlatform platform;
    private SuiteProject suite;

    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        // PLATFORM SETUP
        TestBase.initializeBuildProperties(getWorkDir());
        File install = new File(getWorkDir(), "install");
        TestBase.makePlatform(install);
        // MODULE foo
        Manifest mani = new Manifest();
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE, "foo/1");
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE_SPECIFICATION_VERSION, "1.0");
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE_IMPLEMENTATION_VERSION, "foo-1");
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE_LOCALIZING_BUNDLE, "foo/Bundle.properties");
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE_PROVIDES, "tok1, tok1a");
        Map/*<String,String>*/ contents = new HashMap();
        contents.put("foo/Bundle.properties", "OpenIDE-Module-Name=Foo Module");
        TestBase.createJar(new File(new File(new File(install, "somecluster"), "modules"), "foo.jar"), contents, mani);
        // MODULE bar
        mani = new Manifest();
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE, "bar");
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE_REQUIRES, "tok1");
        TestBase.createJar(new File(new File(new File(install, "somecluster"), "modules"), "bar.jar"), Collections.EMPTY_MAP, mani);
        // MODULE baz
        mani = new Manifest();
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE, "baz");
        mani.getMainAttributes().putValue("OpenIDE-Module-Module-Dependencies", "foo/1 > 1.0");
        mani.getMainAttributes().putValue("OpenIDE-Module-Requires", "org.openide.modules.ModuleFormat1, org.openide.modules.os.Windows");
        TestBase.createJar(new File(new File(new File(install, "anothercluster"), "modules"), "baz.jar"), Collections.EMPTY_MAP, mani);
        platform = NbPlatform.addPlatform("custom", install, "custom");
        // SUITE setup
        suite = TestBase.generateSuite(getWorkDir(), "suite", "custom");
        // MODULE org.example.module1
        NbModuleProject module = TestBase.generateSuiteComponent(suite, "module1");
        EditableManifest em = Util.loadManifest(module.getManifestFile());
        em.setAttribute(ManifestManager.OPENIDE_MODULE, "org.example.module1/2", null);
        em.setAttribute(ManifestManager.OPENIDE_MODULE_SPECIFICATION_VERSION, "2.0", null);
        em.setAttribute(ManifestManager.OPENIDE_MODULE_PROVIDES, "tok2", null);
        Util.storeManifest(module.getManifestFile(), em);
        LocalizedBundleInfo lbinfo = ((LocalizedBundleInfo.Provider) module.getLookup().lookup(LocalizedBundleInfo.Provider.class)).getLocalizedBundleInfo();
        lbinfo.setDisplayName("Module One");
        lbinfo.store();
        // MODULE org.example.module2
        module = TestBase.generateSuiteComponent(suite, "module2");
        em = Util.loadManifest(module.getManifestFile());
        em.removeAttribute(ManifestManager.OPENIDE_MODULE_SPECIFICATION_VERSION, null);
        em.setAttribute(ManifestManager.OPENIDE_MODULE_REQUIRES, "tok2", null);
        Util.storeManifest(module.getManifestFile(), em);
        lbinfo = ((LocalizedBundleInfo.Provider) module.getLookup().lookup(LocalizedBundleInfo.Provider.class)).getLocalizedBundleInfo();
        lbinfo.setDisplayName("Module Two");
        lbinfo.store();
        // MODULE org.example.module3
        module = TestBase.generateSuiteComponent(suite, "module3");
        Util.addDependency(module, "org.example.module2");
        Util.addDependency(module, "bar");
        lbinfo = ((LocalizedBundleInfo.Provider) module.getLookup().lookup(LocalizedBundleInfo.Provider.class)).getLocalizedBundleInfo();
        lbinfo.setDisplayName("Module Three");
        lbinfo.store();
        ProjectManager.getDefault().saveAllProjects();
    }
    
    public void testUniverseModules() throws Exception { // #65924
        Set/*<UniverseModule>*/ modules = SuiteCustomizerLibraries.loadUniverseModules(platform.getModules(), SuiteUtils.getSubProjects(suite));
        Map/*<String,UniverseModule>*/ modulesByName = new HashMap();
        for (Iterator it = modules.iterator(); it.hasNext(); ) {
            SuiteCustomizerLibraries.UniverseModule m = (SuiteCustomizerLibraries.UniverseModule) it.next();
            modulesByName.put(m.getCodeNameBase(), m);
        }
        assertEquals(modules.size(), modulesByName.size());
        SuiteCustomizerLibraries.UniverseModule m = (SuiteCustomizerLibraries.UniverseModule) modulesByName.get("core");
        assertNotNull(m);
        // core.jar is just a dummy JAR, nothing interesting to test
        m = (SuiteCustomizerLibraries.UniverseModule) modulesByName.get("foo");
        assertNotNull(m);
        assertEquals("somecluster", m.getCluster());
        assertEquals("Foo Module", m.getDisplayName());
        assertEquals(1, m.getReleaseVersion());
        assertEquals(new SpecificationVersion("1.0"), m.getSpecificationVersion());
        assertEquals("foo-1", m.getImplementationVersion());
        assertEquals(new HashSet(Arrays.asList(new String[] {"tok1", "tok1a"})), m.getProvidedTokens());
        assertEquals(Collections.EMPTY_SET, m.getRequiredTokens());
        assertEquals(Collections.EMPTY_SET, m.getModuleDependencies());
        m = (SuiteCustomizerLibraries.UniverseModule) modulesByName.get("bar");
        assertNotNull(m);
        assertEquals(Collections.EMPTY_SET, m.getProvidedTokens());
        assertEquals(Collections.singleton("tok1"), m.getRequiredTokens());
        m = (SuiteCustomizerLibraries.UniverseModule) modulesByName.get("baz");
        assertNotNull(m);
        assertEquals(Dependency.create(Dependency.TYPE_MODULE, "foo/1 > 1.0"), m.getModuleDependencies());
        m = (SuiteCustomizerLibraries.UniverseModule) modulesByName.get("org.example.module1");
        assertNotNull(m);
        assertNull(m.getCluster());
        assertEquals("Module One", m.getDisplayName());
        assertEquals(2, m.getReleaseVersion());
        assertEquals(new SpecificationVersion("2.0"), m.getSpecificationVersion());
        assertNull(m.getImplementationVersion());
        assertEquals(Collections.singleton("tok2"), m.getProvidedTokens());
        assertEquals(Collections.EMPTY_SET, m.getRequiredTokens());
        assertEquals(Collections.EMPTY_SET, m.getModuleDependencies());
        m = (SuiteCustomizerLibraries.UniverseModule) modulesByName.get("org.example.module2");
        assertNotNull(m);
        assertEquals(-1, m.getReleaseVersion());
        assertNull(m.getSpecificationVersion());
        assertNull(m.getImplementationVersion());
        assertEquals(Collections.EMPTY_SET, m.getProvidedTokens());
        assertEquals(Collections.singleton("tok2"), m.getRequiredTokens());
        m = (SuiteCustomizerLibraries.UniverseModule) modulesByName.get("org.example.module3");
        assertNotNull(m);
        assertEquals(Dependency.create(Dependency.TYPE_MODULE, "org.example.module2, bar"), m.getModuleDependencies());
    }
    
    public void testDependencyWarnings() throws Exception { // #65924
        Set/*<UniverseModule>*/ modules = SuiteCustomizerLibraries.loadUniverseModules(platform.getModules(), SuiteUtils.getSubProjects(suite));
        Set/*<String>*/ bothClusters = new HashSet(Arrays.asList(new String[] {"somecluster", "anothercluster"}));
        assertEquals(null, join(SuiteCustomizerLibraries.findWarning(modules, bothClusters, Collections.EMPTY_SET)));
        assertEquals("[ERR_platform_excluded_dep, baz, anothercluster, Foo Module, somecluster]",
                join(SuiteCustomizerLibraries.findWarning(modules, Collections.singleton("anothercluster"), Collections.EMPTY_SET)));
        assertNull(join(SuiteCustomizerLibraries.findWarning(modules, Collections.singleton("somecluster"), Collections.EMPTY_SET)));
        assertEquals("[ERR_suite_excluded_dep, Module Three, bar, somecluster]",
                join(SuiteCustomizerLibraries.findWarning(modules, Collections.EMPTY_SET, Collections.EMPTY_SET)));
        assertEquals("[ERR_platform_only_excluded_providers, tok1, bar, somecluster, Foo Module, somecluster]",
                join(SuiteCustomizerLibraries.findWarning(modules, bothClusters, Collections.singleton("foo"))));
        // XXX much more could be tested; check coverage results
    }
    
    private static String join(String[] elements) {
        if (elements != null) {
            return Arrays.asList(elements).toString();
        } else {
            return null;
        }
    }
    
}
