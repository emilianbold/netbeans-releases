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

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.EditableManifest;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.SingleModuleProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.util.Mutex;

/**
 * Test functionality of ModuleList.
 * @author Jesse Glick
 */
public class ModuleListTest extends TestBase {
    
    public ModuleListTest(String name) {
        super(name);
    }
    
    private File suite1, suite2, standaloneSuite3;
    
    protected void setUp() throws Exception {
        super.setUp();
        suite1 = resolveEEPFile("suite1");
        suite2 = resolveEEPFile("suite2");
        standaloneSuite3 = resolveEEPFile("suite3");
    }
    
    public void testParseProperties() throws Exception {
        File basedir = file("ant/browsetask");
        PropertyEvaluator eval = ModuleList.parseProperties(basedir, nbCVSRootFile(), false, false, "org.netbeans.modules.ant.browsetask");
        String nbdestdir = eval.getProperty("netbeans.dest.dir");
        assertNotNull(nbdestdir);
        assertEquals(file("nbbuild/netbeans"), PropertyUtils.resolveFile(basedir, nbdestdir));
        assertEquals("modules/org-netbeans-modules-ant-browsetask.jar", eval.getProperty("module.jar"));
        assertEquals(file("nbbuild/netbeans/" + TestBase.CLUSTER_JAVA), PropertyUtils.resolveFile(basedir, eval.getProperty("cluster")));
        assertNull(eval.getProperty("suite.dir"));
        basedir = file("openide/loaders");
        eval = ModuleList.parseProperties(basedir, nbCVSRootFile(), false, false, "org.openide.loaders");
        assertEquals("modules/org-openide-loaders.jar", eval.getProperty("module.jar"));
        basedir = new File(suite1, "action-project");
        eval = ModuleList.parseProperties(basedir, suite1, true, false, "org.netbeans.examples.modules.action");
        nbdestdir = eval.getProperty("netbeans.dest.dir");
        assertNotNull(nbdestdir);
        assertEquals(file("nbbuild/netbeans"), PropertyUtils.resolveFile(basedir, nbdestdir));
        assertEquals(suite1, PropertyUtils.resolveFile(basedir, eval.getProperty("suite.dir")));
        basedir = new File(suite2, "misc-project");
        eval = ModuleList.parseProperties(basedir, suite2, true, false, "org.netbeans.examples.modules.misc");
        nbdestdir = eval.getProperty("netbeans.dest.dir");
        assertNotNull(nbdestdir);
        assertEquals(file("nbbuild/netbeans"), PropertyUtils.resolveFile(basedir, nbdestdir));
        assertEquals(file(suite2, "build/cluster"), PropertyUtils.resolveFile(basedir, eval.getProperty("cluster")));
        assertEquals(suite2, PropertyUtils.resolveFile(basedir, eval.getProperty("suite.dir")));
        basedir = new File(standaloneSuite3, "dummy-project");
        eval = ModuleList.parseProperties(basedir, standaloneSuite3, false, true, "org.netbeans.examples.modules.dummy");
        nbdestdir = eval.getProperty("netbeans.dest.dir");
        assertNotNull(nbdestdir);
        assertEquals(file(standaloneSuite3, "nbplatform"), PropertyUtils.resolveFile(basedir, nbdestdir));
        assertEquals(file(standaloneSuite3, "dummy-project/build/cluster"), PropertyUtils.resolveFile(basedir, eval.getProperty("cluster")));
        assertNull(eval.getProperty("suite.dir"));
    }
    
    public void testFindModulesInSuite() throws Exception {
        assertEquals("correct modules in suite1", new HashSet(Arrays.asList(new File[] {
            file(suite1, "action-project"),
            file(suite1, "support/lib-project"),
        })), new HashSet(Arrays.asList(ModuleList.findModulesInSuite(suite1))));
        assertEquals("correct modules in suite2", new HashSet(Arrays.asList(new File[] {
            file(suite2, "misc-project"),
        })), new HashSet(Arrays.asList(ModuleList.findModulesInSuite(suite2))));
    }

    public void testNetBeansOrgEntries() throws Exception {
        long start = System.currentTimeMillis();
        ModuleList ml = ModuleList.getModuleList(file("ant/browsetask")); // should be arbitrary
        System.err.println("Time to scan netbeans.org sources: " + (System.currentTimeMillis() - start) + "msec");
        System.err.println("Directories traversed: " + ModuleList.directoriesChecked);
        System.err.println("XML files parsed: " + ModuleList.xmlFilesParsed + " in " + ModuleList.timeSpentInXmlParsing + "msec");
        ModuleEntry e = ml.getEntry("org.netbeans.modules.java.project");
        assertNotNull("have org.netbeans.modules.java.project", e);
        assertEquals("right jarLocation", file("nbbuild/netbeans/" + TestBase.CLUSTER_JAVA + "/modules/org-netbeans-modules-java-project.jar"), e.getJarLocation());
        assertTrue("in all entries", ml.getAllEntries().contains(e));
        assertEquals("right path", "java/project", e.getNetBeansOrgPath());
        assertEquals("right source location", file("java/project"), e.getSourceLocation());
        assertTrue("same by JAR", ModuleList.getKnownEntries(e.getJarLocation()).contains(e));
        assertTrue("same by other random file", ModuleList.getKnownEntries(file("nbbuild/netbeans/" + TestBase.CLUSTER_JAVA + "/config/Modules/org-netbeans-modules-java-project.xml")).contains(e));
        assertEquals("right codeNameBase", "org.netbeans.modules.java.project", e.getCodeNameBase());
        assertEquals(file("nbbuild/netbeans"), e.getDestDir());
        assertEquals("", e.getClassPathExtensions());
        assertNotNull("localized name", e.getLocalizedName());
        assertNotNull("display category", e.getCategory());
        assertNotNull("short description", e.getShortDescription());
        assertNotNull("long description", e.getLongDescription());
        assertNotNull("release version", e.getReleaseVersion());
        assertNotNull("specification version", e.getSpecificationVersion());
        assertEquals("number of public packages for " + e, new Integer(6), new Integer(e.getPublicPackages().length));
        assertFalse("not deprecated", e.isDeprecated());
        // Test something in a different cluster and dir:
        e = ml.getEntry("org.openide.filesystems");
        assertNotNull("have org.openide.filesystems", e);
        assertEquals("right jarLocation", file("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/core/org-openide-filesystems.jar"), e.getJarLocation());
        assertEquals("right source location", file("openide/fs"), e.getSourceLocation());
        assertTrue("same by JAR", ModuleList.getKnownEntries(e.getJarLocation()).contains(e));
        assertEquals("right path", "openide/fs", e.getNetBeansOrgPath());
        // Test class-path extensions:
        e = ml.getEntry("org.netbeans.libs.xerces");
        assertNotNull(e);
        assertEquals("correct CP extensions (using <binary-origin> and relative paths)",
            ":" + file("libs/xerces/external/xerces-2.8.0.jar"),
            e.getClassPathExtensions());
        /* XXX unmaintained:
        e = ml.getEntry("javax.jmi.model");
        assertNotNull(e);
        assertEquals("correct CP extensions (using <binary-origin> and property substitutions #1)",
            ":" + file("mdr/external/mof.jar"),
            e.getClassPathExtensions());
         */
        /* XXX org.netbeans.modules.css moved to "org.netbeans.modules.languages.css?
        e = ml.getEntry("org.netbeans.modules.css");
        assertNotNull(e);
        assertEquals("correct CP extensions (using <binary-origin> and property substitutions #2)",
            ":" + file("xml/external/flute.jar") + ":" + file("xml/external/sac.jar"),
            e.getClassPathExtensions());
         */
        e = ml.getEntry("org.netbeans.modules.xml.tax");
        assertNotNull(e);
        assertEquals("correct CP extensions (using runtime-relative-path)",
            ":" + file("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/ext/org-netbeans-tax.jar"),
            e.getClassPathExtensions());
        e = ml.getEntry("org.openide.util.enumerations");
        assertNotNull(e);
        assertTrue("this one is deprecated", e.isDeprecated());
        e = ml.getEntry("org.netbeans.modules.projectui");
        assertNotNull(e);
        assertNotNull(e.getProvidedTokens());
        assertTrue("There are some provided tokens", e.getProvidedTokens().length > 0);
        // XXX test that getAllEntries() also includes nonstandard modules, and so does getKnownEntries() if necessary
        // Test a nonstandard module:
        e = ml.getEntry("org.netbeans.modules.looks");
        assertNotNull(e);
        assertEquals("right path", "contrib/looks", e.getNetBeansOrgPath());
    }
    
    public void testExternalEntries() throws Exception {
        // Start with suite1 - should find also nb_all.
        long start = System.currentTimeMillis();
        ModuleList ml = ModuleList.getModuleList(file(suite1, "support/lib-project"));
        System.err.println("Time to scan suite + NB binaries: " + (System.currentTimeMillis() - start) + "msec");
        ModuleEntry e = ml.getEntry("org.netbeans.examples.modules.action");
        assertNotNull("action-project found", e);
        File jar = resolveEEPFile("/suite1/build/cluster/modules/org-netbeans-examples-modules-action.jar");
        assertEquals("right JAR location", jar, e.getJarLocation());
        assertTrue("in all entries", ml.getAllEntries().contains(e));
        assertNull("no nb.org path", e.getNetBeansOrgPath());
        assertEquals("right source location", file(suite1, "action-project"), e.getSourceLocation());
        assertTrue("same by JAR", ModuleList.getKnownEntries(e.getJarLocation()).contains(e));
        assertEquals("right codeNameBase", "org.netbeans.examples.modules.action", e.getCodeNameBase());
        e = ml.getEntry("org.netbeans.modules.classfile");
        assertNotNull("can find nb.org sources too (classfile module must be built)", e);
        assertEquals("correct nb.org source location", file("classfile"), e.getSourceLocation());
        assertNotNull("localized name", e.getLocalizedName());
        assertNotNull("display category", e.getCategory());
        assertNotNull("short description", e.getShortDescription());
        assertNotNull("long description", e.getLongDescription());
        assertNotNull("release version", e.getReleaseVersion());
        assertNotNull("specification version", e.getSpecificationVersion());
        assertNotNull(e.getProvidedTokens());
        assertEquals("there are no provided tokens", 0, e.getProvidedTokens().length);
        /*
        e = ml.getEntry("org.netbeans.examples.modules.misc");
        assertNotNull("can find sources from another suite (misc must have been built first)", e);
        assertEquals("correct source location", file(suite2, "misc-project"), e.getSourceLocation());
        assertEquals("number of public packages for " + e, new Integer(1), new Integer(e.getPublicPackages().length));
         */
        e = ml.getEntry("org.netbeans.libs.xerces");
        assertEquals("correct CP exts for a nb.org module (using Class-Path only)",
            ":" + file("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/ext/xerces-2.8.0.jar"),
            e.getClassPathExtensions());
        // From suite2, can only find itself, and netbeans.org modules only available in binary form.
        ml = ModuleList.getModuleList(file(suite2, "misc-project"));
        e = ml.getEntry("org.netbeans.examples.modules.misc");
        assertNotNull("can find module from my own suite", e);
        assertEquals("correct JAR location", resolveEEPFile("/suite2/build/cluster/modules/org-netbeans-examples-modules-misc.jar"), e.getJarLocation());
        assertNotNull("localized name", e.getLocalizedName());
        assertNotNull("display category", e.getCategory());
        assertNotNull("short description", e.getShortDescription());
        assertNotNull("long description", e.getLongDescription());
        assertEquals("right codeNameBase", "org.netbeans.examples.modules.misc", e.getCodeNameBase());
        assertNotNull("release version", e.getReleaseVersion());
        assertNotNull("specification version", e.getSpecificationVersion());
        assertNotNull(e.getProvidedTokens());
        assertEquals("there are no provided tokens", 0, e.getProvidedTokens().length);
        assertEquals("number of public packages for " + e, new Integer(1), new Integer(e.getPublicPackages().length));
        e = ml.getEntry("org.netbeans.libs.xerces");
        assertNotNull("can find nb.org binary module too", e);
        assertEquals("have sources for that", file("libs/xerces"), e.getSourceLocation());
        assertEquals("and correct JAR location", file("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-libs-xerces.jar"), e.getJarLocation());
        assertEquals("and correct CP exts (using Class-Path only)",
            ":" + file("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/ext/xerces-2.8.0.jar"),
            e.getClassPathExtensions());
        e = ml.getEntry("org.openide.util");
        assertNotNull(e);
        assertFalse("binary API not deprecated", e.isDeprecated());
        e = ml.getEntry("org.openide.util.enumerations");
        assertNotNull(e);
        assertTrue("this one is deprecated", e.isDeprecated());
        // From suite3, can find itself and netbeans.org modules in binary form.
        ml = ModuleList.getModuleList(file(standaloneSuite3, "dummy-project"));
        e = ml.getEntry("org.netbeans.examples.modules.dummy");
        assertNotNull("can find myself", e);
        e = ml.getEntry("org.netbeans.modules.classfile");
        assertNotNull("found (fake) nb.org module", e);
        assertNull("...without sources", e.getSourceLocation());
        assertEquals("and with a special JAR location", file(standaloneSuite3, "nbplatform/random/modules/random.jar"), e.getJarLocation());
        assertEquals("correct CP extensions (using Class-Path only, and ignoring sources completely)",
            ":" + file(standaloneSuite3, "nbplatform/random/modules/ext/stuff.jar"),
            e.getClassPathExtensions());
    }
    
    public void testNewlyAddedModule() throws Exception {
        // XXX make new module, call refresh, check that things work
        // (partially tested already by NbModuleProjectGeneratorTest.testCreateSuiteComponentModule)
    }
    
    public void testFindNetBeansOrg() throws Exception {
        assertEquals(nbCVSRootFile(), ModuleList.findNetBeansOrg(file("xml")));
        assertEquals(nbCVSRootFile(), ModuleList.findNetBeansOrg(file("xml/tax")));
        assertEquals(nbCVSRootFile(), ModuleList.findNetBeansOrg(file("xml/tax/lib")));
        assertEquals(null, ModuleList.findNetBeansOrg(file("xml/tax/lib/src")));
        assertEquals(null, ModuleList.findNetBeansOrg(File.listRoots()[0]));
    }
    
    public void testRefreshSuiteModuleList() throws Exception {
        SuiteProject suite1 = generateSuite("suite1");
        final NbModuleProject p = TestBase.generateSuiteComponent(suite1, "module1a");
        ModuleList ml = ModuleList.getModuleList(
                p.getProjectDirectoryFile(),
                NbPlatform.getDefaultPlatform().getDestDir());
        assertNotNull("module1a is in the suite1's module list", ml.getEntry("org.example.module1a"));
        assertEquals("no public packages in the ModuleEntry", 0, ml.getEntry("org.example.module1a").getPublicPackages().length);
        
        // added package must be reflected in the refreshed list (63561)
        Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
            public Object run() throws IOException {
                ProjectXMLManager pxm = new ProjectXMLManager(p);
                String[] newPP = new String[] { "org.example.module1a" };
                pxm.replacePublicPackages(newPP);
                return Boolean.TRUE;
            }
        });
        assertTrue("replace public packages", result.booleanValue());
        ProjectManager.getDefault().saveProject(p);
        
        ModuleList.refreshSuiteModuleList(suite1.getProjectDirectoryFile());
        ml = ModuleList.getModuleList(p.getProjectDirectoryFile(),
                NbPlatform.getDefaultPlatform().getDestDir());
        assertEquals("one public packages in the refreshed ModuleEntry", 1, ml.getEntry("org.example.module1a").getPublicPackages().length);
    }
    
    public void testSpecVersionBaseSourceEntries() throws Exception { // #72463
        SuiteProject suite = generateSuite("suite");
        NbModuleProject p = TestBase.generateSuiteComponent(suite, "module");
        ModuleList ml = ModuleList.getModuleList(p.getProjectDirectoryFile());
        ModuleEntry e = ml.getEntry("org.example.module");
        assertNotNull("have entry", e);
        assertEquals("right initial spec vers from manifest", "1.0", e.getSpecificationVersion());
        EditableProperties ep = p.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty(SingleModuleProperties.SPEC_VERSION_BASE, "1.1.0");
        p.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        EditableManifest em = Util.loadManifest(p.getManifestFile());
        em.removeAttribute(ManifestManager.OPENIDE_MODULE_SPECIFICATION_VERSION, null);
        Util.storeManifest(p.getManifestFile(), em);
        ProjectManager.getDefault().saveProject(p);
        assertEquals("right spec.version.base", "1.1", e.getSpecificationVersion());
        ep.setProperty(SingleModuleProperties.SPEC_VERSION_BASE, "1.2.0");
        p.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveProject(p);
        assertEquals("right modified spec.version.base", "1.2", e.getSpecificationVersion());
    }
    
}
