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

package org.netbeans.nbbuild;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import junit.framework.TestCase;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;

/**
 * Test {@link ModuleListParser}.
 * @author Jesse Glick
 */
public class ModuleListParserTest extends TestCase {
    
    public ModuleListParserTest(String name) {
        super(name);
    }
    
    private File nball;
    
    private File file(File root, String relpath) {
        return new File(root, relpath.replace('/', File.separatorChar));
    }
    
    private String filePath(File root, String relpath) {
        return file(root, relpath).getAbsolutePath();
    }

    protected void setUp() throws Exception {
        super.setUp();
        String prop = System.getProperty("nb_all");
        assertNotNull("${nb_all} defined", prop);
        nball = new File(prop);
    }
    
    public void testScanSourcesInNetBeansOrg() throws Exception {
        Hashtable properties = new Hashtable();
        properties.put("nb_all", nball.getAbsolutePath());
        File build = file(nball, "build");
        properties.put("netbeans.dest.dir", build.getAbsolutePath());
        properties.put("nb.cluster.foo", "beans,clazz");
        properties.put("nb.cluster.foo.dir", "foodir");
        properties.put("nb.cluster.bar", "core/startup");
        properties.put("nb.cluster.bar.dir", "bardir");
        ModuleListParser p = new ModuleListParser(properties, ParseProjectXml.TYPE_NB_ORG, null);
        ModuleListParser.Entry e = p.findByCodeNameBase("org.netbeans.modules.beans");
        assertNotNull(e);
        assertEquals("org.netbeans.modules.beans", e.getCnb());
        assertEquals(file(build, "foodir/modules/org-netbeans-modules-beans.jar"), e.getJar());
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.netbeans.libs.xerces");
        assertNotNull("found module in a subdir", e);
        assertEquals("org.netbeans.libs.xerces", e.getCnb());
        assertEquals("unknown module put in extra cluster by default", file(build, "extra/modules/org-netbeans-libs-xerces.jar"), e.getJar());
        assertEquals("correct CP extensions (using <binary-origin> and relative paths)", Arrays.asList(new File[] {
            file(nball, "libs/external/xerces-2.6.2.jar"),
            file(nball, "libs/external/xml-commons-dom-ranges-1.0.b2.jar"),
        }), Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("javax.jmi.model");
        assertNotNull(e);
        assertEquals("correct CP extensions (using <binary-origin> and property substitutions #1)", Arrays.asList(new File[] {
            file(nball, "mdr/external/mof.jar"),
        }), Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.netbeans.modules.css");
        assertNotNull(e);
        assertEquals("correct CP extensions (using <binary-origin> and property substitutions #2)", Arrays.asList(new File[] {
            file(nball, "xml/external/flute.jar"),
            file(nball, "xml/external/sac.jar"),
        }), Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.netbeans.modules.javanavigation");
        assertNotNull("found module in a subsubdir", e);
        e = p.findByCodeNameBase("org.netbeans.core.startup");
        assertNotNull(e);
        assertEquals("org.netbeans.core.startup", e.getCnb());
        assertEquals("handling special JAR names correctly", file(build, "bardir/core/core.jar"), e.getJar());
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.netbeans.modules.xml.tax");
        assertNotNull("found xml/tax", e);
        assertEquals("org.netbeans.modules.xml.tax", e.getCnb());
        assertEquals(file(build, "extra/modules/autoload/xml-tax.jar"), e.getJar());
        assertEquals("correct CP extensions (using runtime-relative-path)", Arrays.asList(new File[] {
            file(build, "extra/modules/autoload/ext/tax.jar"),
        }), Arrays.asList(e.getClassPathExtensions()));
    }
    
    public void testScanSourcesAndBinariesForExternalSuite() throws Exception {
        Project fakeproj = new Project();
        fakeproj.addBuildListener(new BuildListener() {
            public void messageLogged(BuildEvent buildEvent) {
                if (buildEvent.getPriority() <= Project.MSG_VERBOSE) {
                    System.err.println(buildEvent.getMessage());
                }
            }
            public void taskStarted(BuildEvent buildEvent) {}
            public void taskFinished(BuildEvent buildEvent) {}
            public void targetStarted(BuildEvent buildEvent) {}
            public void targetFinished(BuildEvent buildEvent) {}
            public void buildStarted(BuildEvent buildEvent) {}
            public void buildFinished(BuildEvent buildEvent) {}
        });
        Hashtable properties = new Hashtable();
        properties.put("netbeans.dest.dir", filePath(nball, "nbbuild/netbeans"));
        properties.put("basedir", filePath(nball, "apisupport/project/test/unit/data/example-external-projects/suite1/action-project"));
        properties.put("suite.dir", "..");
        ModuleListParser p = new ModuleListParser(properties, ParseProjectXml.TYPE_SUITE, fakeproj);
        ModuleListParser.Entry e = p.findByCodeNameBase("org.netbeans.examples.modules.action");
        assertNotNull("found myself", e);
        assertEquals("org.netbeans.examples.modules.action", e.getCnb());
        assertEquals(file(nball, "nbbuild/netbeans/devel/modules/org-netbeans-examples-modules-action.jar"), e.getJar());
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.netbeans.examples.modules.lib");
        assertNotNull("found sister project in suite", e);
        assertEquals("org.netbeans.examples.modules.lib", e.getCnb());
        assertEquals(file(nball, "nbbuild/netbeans/devel/modules/org-netbeans-examples-modules-lib.jar"), e.getJar());
        File jar = file(nball, "nbbuild/netbeans/ide5/modules/org-netbeans-libs-xerces.jar");
        assertTrue("Build all-libs/xerces first!", jar.isFile());
        e = p.findByCodeNameBase("org.netbeans.libs.xerces");
        assertNotNull("found netbeans.org module by its binary", e);
        assertEquals("org.netbeans.libs.xerces", e.getCnb());
        assertEquals(jar, e.getJar());
        assertEquals("correct CP extensions (using Class-Path header in manifest)", Arrays.asList(new File[] {
            file(nball, "nbbuild/netbeans/ide5/modules/ext/xerces-2.6.2.jar"),
            file(nball, "nbbuild/netbeans/ide5/modules/ext/xml-commons-dom-ranges-1.0.b2.jar"),
        }), Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.openide.loaders");
        assertNotNull(e);
        assertEquals("org.openide.loaders", e.getCnb());
        assertEquals(file(nball, "nbbuild/netbeans/platform5/modules/org-openide-loaders.jar"), e.getJar());
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.netbeans.bootstrap");
        assertNotNull(e);
        assertEquals("org.netbeans.bootstrap", e.getCnb());
        assertEquals(file(nball, "nbbuild/netbeans/platform5/lib/boot.jar"), e.getJar());
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(e.getClassPathExtensions()));
        jar = file(nball, "nbbuild/netbeans/ide5/modules/autoload/xml-tax.jar");
        assertTrue("Build all-xml/tax first!", jar.isFile());
        e = p.findByCodeNameBase("org.netbeans.modules.xml.tax");
        assertNotNull(e);
        assertEquals("org.netbeans.modules.xml.tax", e.getCnb());
        assertEquals(jar, e.getJar());
        assertEquals(Arrays.asList(new File[] {
            file(nball, "nbbuild/netbeans/ide5/modules/autoload/ext/tax.jar"),
        }), Arrays.asList(e.getClassPathExtensions()));
    }
    
    public void testScanSourcesAndBinariesForExternalStandaloneModule() throws Exception {
        Hashtable properties = new Hashtable();
        properties.put("netbeans.dest.dir", filePath(nball, "apisupport/project/test/unit/data/example-external-projects/suite3/nbplatform"));
        properties.put("basedir", filePath(nball, "apisupport/project/test/unit/data/example-external-projects/suite3/dummy-project"));
        ModuleListParser p = new ModuleListParser(properties, ParseProjectXml.TYPE_STANDALONE, null);
        ModuleListParser.Entry e = p.findByCodeNameBase("org.netbeans.examples.modules.dummy");
        assertNotNull("found myself", e);
        assertEquals("org.netbeans.examples.modules.dummy", e.getCnb());
        assertEquals(file(nball, "apisupport/project/test/unit/data/example-external-projects/suite3/nbplatform/devel/modules/org-netbeans-examples-modules-dummy.jar"), e.getJar());
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.netbeans.modules.beans");
        assertNotNull("found (fake) netbeans.org module by its binary", e);
        assertEquals("org.netbeans.modules.beans", e.getCnb());
    }
    
}
