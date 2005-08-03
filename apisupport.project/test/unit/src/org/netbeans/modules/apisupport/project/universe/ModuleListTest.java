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

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.modules.apisupport.project.*;

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
        suite1 = file(extexamplesF, "suite1");
        suite2 = file(extexamplesF, "suite2");
        standaloneSuite3 = file(extexamplesF, "suite3");
    }
    
    public void testParseProperties() throws Exception {
        File basedir = file("ant/browsetask");
        PropertyEvaluator eval = ModuleList.parseProperties(basedir, nbrootF, false, false, "org.netbeans.modules.ant.browsetask");
        String nbdestdir = eval.getProperty("netbeans.dest.dir");
        assertNotNull(nbdestdir);
        assertEquals(file("nbbuild/netbeans"), PropertyUtils.resolveFile(basedir, nbdestdir));
        assertEquals("modules/org-netbeans-modules-ant-browsetask.jar", eval.getProperty("module.jar"));
        assertEquals(file("nbbuild/netbeans/ide6"), PropertyUtils.resolveFile(basedir, eval.getProperty("cluster")));
        assertNull(eval.getProperty("suite.dir"));
        basedir = file("openide/loaders");
        eval = ModuleList.parseProperties(basedir, nbrootF, false, false, "org.openide.loaders");
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
        // XXX might be better to have a test/perf/src/.../ModuleListPerfTest.java extending org.netbeans.performance.Benchmark
        System.err.println("Time to scan netbeans.org sources: " + (System.currentTimeMillis() - start) + "msec");
        ModuleEntry e = ml.getEntry("org.netbeans.modules.java.project");
        assertNotNull("have org.netbeans.modules.java.project", e);
        assertEquals("right jarLocation", file("nbbuild/netbeans/ide6/modules/org-netbeans-modules-java-project.jar"), e.getJarLocation());
        assertTrue("in all entries", ml.getAllEntries().contains(e));
        assertEquals("right path", "java/project", e.getNetBeansOrgPath());
        assertEquals("right source location", file("java/project"), e.getSourceLocation());
        assertTrue("same by JAR", ModuleList.getKnownEntries(e.getJarLocation()).contains(e));
        assertTrue("same by other random file", ModuleList.getKnownEntries(file("nbbuild/netbeans/ide6/config/Modules/org-netbeans-modules-java-project.xml")).contains(e));
        assertEquals("right codeNameBase", "org.netbeans.modules.java.project", e.getCodeNameBase());
        assertEquals(file("nbbuild/netbeans"), e.getDestDir());
        assertEquals("", e.getClassPathExtensions());
        assertNotNull("localized name", e.getLocalizedName());
        assertNotNull("display category", e.getCategory());
        assertNotNull("short description", e.getShortDescription());
        assertNotNull("long description", e.getLongDescription());
        assertNotNull("release version", e.getReleaseVersion());
        assertNotNull("specification version", e.getSpecificationVersion());
        assertEquals("number of public packages for " + e, new Integer(5), new Integer(e.getPublicPackages().length));
        assertFalse("not deprecated", e.isDeprecated());
        // Test something in a different cluster and dir:
        e = ml.getEntry("org.openide.filesystems");
        assertNotNull("have org.openide.filesystems", e);
        assertEquals("right jarLocation", file("nbbuild/netbeans/platform6/core/org-openide-filesystems.jar"), e.getJarLocation());
        assertEquals("right source location", file("openide/fs"), e.getSourceLocation());
        assertTrue("same by JAR", ModuleList.getKnownEntries(e.getJarLocation()).contains(e));
        assertEquals("right path", "openide/fs", e.getNetBeansOrgPath());
        // Test class-path extensions:
        e = ml.getEntry("org.netbeans.libs.xerces");
        assertNotNull(e);
        assertEquals("correct CP extensions (using <binary-origin> and relative paths)",
            ":" + file("libs/external/xerces-2.6.2.jar") + ":" + file("libs/external/xml-commons-dom-ranges-1.0.b2.jar"),
            e.getClassPathExtensions());
        e = ml.getEntry("javax.jmi.model");
        assertNotNull(e);
        assertEquals("correct CP extensions (using <binary-origin> and property substitutions #1)",
            ":" + file("mdr/external/mof.jar"),
            e.getClassPathExtensions());
        e = ml.getEntry("org.netbeans.modules.css");
        assertNotNull(e);
        assertEquals("correct CP extensions (using <binary-origin> and property substitutions #2)",
            ":" + file("xml/external/flute.jar") + ":" + file("xml/external/sac.jar"),
            e.getClassPathExtensions());
        e = ml.getEntry("org.netbeans.modules.xml.tax");
        assertNotNull(e);
        assertEquals("correct CP extensions (using runtime-relative-path)",
            ":" + file("nbbuild/netbeans/ide6/modules/autoload/ext/tax.jar"),
            e.getClassPathExtensions());
        e = ml.getEntry("org.openide.util.enumerations");
        assertNotNull(e);
        assertTrue("this one is deprecated", e.isDeprecated());
        e = ml.getEntry("org.netbeans.modules.projectui");
        assertNotNull(e);
        assertNotNull(e.getProvidedTokens());
        assertTrue("There are some provided tokens", e.getProvidedTokens().length > 0);
    }
    
    public void testExternalEntries() throws Exception {
        // Start with suite1 - should find also nb_all.
        long start = System.currentTimeMillis();
        ModuleList ml = ModuleList.getModuleList(file(suite1, "support/lib-project"));
        System.err.println("Time to scan suite + NB binaries: " + (System.currentTimeMillis() - start) + "msec");
        ModuleEntry e = ml.getEntry("org.netbeans.examples.modules.action");
        assertNotNull("action-project found", e);
        File jar = file(EEP + "/suite1/build/cluster/modules/org-netbeans-examples-modules-action.jar");
        assertEquals("right JAR location", jar, e.getJarLocation());
        assertTrue("in all entries", ml.getAllEntries().contains(e));
        assertNull("no nb.org path", e.getNetBeansOrgPath());
        assertEquals("right source location", file(suite1, "action-project"), e.getSourceLocation());
        assertTrue("same by JAR", ModuleList.getKnownEntries(e.getJarLocation()).contains(e));
        assertEquals("right codeNameBase", "org.netbeans.examples.modules.action", e.getCodeNameBase());
        e = ml.getEntry("org.netbeans.modules.beans");
        assertNotNull("can find nb.org sources too (beans module must be built)", e);
        assertEquals("correct nb.org source location", file("beans"), e.getSourceLocation());
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
            ":" + file("nbbuild/netbeans/ide6/modules/ext/xerces-2.6.2.jar") + ":" + file("nbbuild/netbeans/ide6/modules/ext/xml-commons-dom-ranges-1.0.b2.jar"),
            e.getClassPathExtensions());
        // From suite2, can only find itself, and netbeans.org modules only available in binary form.
        ml = ModuleList.getModuleList(file(suite2, "misc-project"));
        e = ml.getEntry("org.netbeans.examples.modules.misc");
        assertNotNull("can find module from my own suite", e);
        assertEquals("correct JAR location", file(EEP + "/suite2/build/cluster/modules/org-netbeans-examples-modules-misc.jar"), e.getJarLocation());
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
        assertEquals("and correct JAR location", file("nbbuild/netbeans/ide6/modules/org-netbeans-libs-xerces.jar"), e.getJarLocation());
        assertEquals("and correct CP exts (using Class-Path only)",
            ":" + file("nbbuild/netbeans/ide6/modules/ext/xerces-2.6.2.jar") + ":" + file("nbbuild/netbeans/ide6/modules/ext/xml-commons-dom-ranges-1.0.b2.jar"),
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
        e = ml.getEntry("org.netbeans.modules.beans");
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
        assertEquals(nbrootF, ModuleList.findNetBeansOrg(file("xml")));
        assertEquals(nbrootF, ModuleList.findNetBeansOrg(file("xml/tax")));
        assertEquals(nbrootF, ModuleList.findNetBeansOrg(file("xml/tax/lib")));
        assertEquals(null, ModuleList.findNetBeansOrg(file("xml/tax/lib/src")));
        assertEquals(null, ModuleList.findNetBeansOrg(File.listRoots()[0]));
    }
    
}
