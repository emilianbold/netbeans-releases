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
        long start = System.currentTimeMillis();
        ModuleListParser p = new ModuleListParser(properties, ParseProjectXml.TYPE_NB_ORG, null);
        System.err.println("Scanned " + nball + " sources in " + (System.currentTimeMillis() - start) + "msec");
        ModuleListParser.Entry e = p.findByCodeNameBase("org.netbeans.modules.beans");
        assertNotNull(e);
        assertEquals("org.netbeans.modules.beans", e.getCnb());
        assertEquals(file(build, "foodir/modules/org-netbeans-modules-beans.jar"), e.getJar());
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.netbeans.libs.xerces");
        assertNotNull("found module in a subdir", e);
        assertEquals("org.netbeans.libs.xerces", e.getCnb());
        assertEquals("unknown module put in extra cluster by default", file(build, "extra/modules/org-netbeans-libs-xerces.jar"), e.getJar());
        assertEquals("correct CP extensions (using <binary-origin> and relative paths)",
            Collections.singletonList(file(nball, "libs/external/xerces-2.8.0.jar")),
            Arrays.asList(e.getClassPathExtensions()));
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
        e = p.findByCodeNameBase("org.netbeans.swing.tabcontrol");
        assertNotNull("found module in a subsubdir", e);
        e = p.findByCodeNameBase("org.netbeans.core.startup");
        assertNotNull(e);
        assertEquals("org.netbeans.core.startup", e.getCnb());
        assertEquals("handling special JAR names correctly", file(build, "bardir/core/core.jar"), e.getJar());
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.netbeans.modules.xml.tax");
        assertNotNull("found xml/tax", e);
        assertEquals("org.netbeans.modules.xml.tax", e.getCnb());
        assertEquals(file(build, "extra/modules/org-netbeans-modules-xml-tax.jar"), e.getJar());
        assertEquals("correct CP extensions (using runtime-relative-path)", Arrays.asList(new File[] {
            file(build, "extra/modules/ext/org-netbeans-tax.jar"),
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
        properties.put("suite.dir", filePath(nball, "apisupport/project/test/unit/data/example-external-projects/suite1"));
        long start = System.currentTimeMillis();
        ModuleListParser p = new ModuleListParser(properties, ParseProjectXml.TYPE_SUITE, fakeproj);
        System.err.println("Scanned " + nball + " binaries in " + (System.currentTimeMillis() - start) + "msec");
        ModuleListParser.Entry e = p.findByCodeNameBase("org.netbeans.examples.modules.action");
        assertNotNull("found myself", e);
        assertEquals("org.netbeans.examples.modules.action", e.getCnb());
        assertEquals(file(nball, "apisupport/project/test/unit/data/example-external-projects/suite1/build/cluster/modules/org-netbeans-examples-modules-action.jar"), e.getJar());
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.netbeans.examples.modules.lib");
        assertNotNull("found sister project in suite", e);
        assertEquals("org.netbeans.examples.modules.lib", e.getCnb());
        assertEquals(file(nball, "apisupport/project/test/unit/data/example-external-projects/suite1/build/cluster/modules/org-netbeans-examples-modules-lib.jar"), e.getJar());
        File jar = file(nball, "nbbuild/netbeans/ide8/modules/org-netbeans-libs-xerces.jar");
        assertTrue("Build all-libs/xerces first!", jar.isFile());
        e = p.findByCodeNameBase("org.netbeans.libs.xerces");
        assertNotNull("found netbeans.org module by its binary", e);
        assertEquals("org.netbeans.libs.xerces", e.getCnb());
        assertEquals(jar, e.getJar());
        assertEquals("correct CP extensions (using Class-Path header in manifest)",
                Collections.singletonList(file(nball, "nbbuild/netbeans/ide8/modules/ext/xerces-2.8.0.jar")),
                Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.openide.loaders");
        assertNotNull(e);
        assertEquals("org.openide.loaders", e.getCnb());
        assertEquals(file(nball, "nbbuild/netbeans/platform7/modules/org-openide-loaders.jar"), e.getJar());
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.netbeans.bootstrap");
        assertNotNull(e);
        assertEquals("org.netbeans.bootstrap", e.getCnb());
        assertEquals(file(nball, "nbbuild/netbeans/platform7/lib/boot.jar"), e.getJar());
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(e.getClassPathExtensions()));
        jar = file(nball, "nbbuild/netbeans/ide8/modules/org-netbeans-modules-xml-tax.jar");
        assertTrue("Build all-xml/tax first!", jar.isFile());
        e = p.findByCodeNameBase("org.netbeans.modules.xml.tax");
        assertNotNull(e);
        assertEquals("org.netbeans.modules.xml.tax", e.getCnb());
        assertEquals(jar, e.getJar());
        assertEquals(Arrays.asList(new File[] {
            file(nball, "nbbuild/netbeans/ide8/modules/ext/org-netbeans-tax.jar"),
        }), Arrays.asList(e.getClassPathExtensions()));
    }
    
    public void testScanSourcesAndBinariesForExternalStandaloneModule() throws Exception {
        Hashtable properties = new Hashtable();
        properties.put("netbeans.dest.dir", filePath(nball, "apisupport/project/test/unit/data/example-external-projects/suite3/nbplatform"));
        properties.put("basedir", filePath(nball, "apisupport/project/test/unit/data/example-external-projects/suite3/dummy-project"));
        properties.put("project", filePath(nball, "apisupport/project/test/unit/data/example-external-projects/suite3/dummy-project"));
        ModuleListParser p = new ModuleListParser(properties, ParseProjectXml.TYPE_STANDALONE, null);
        ModuleListParser.Entry e = p.findByCodeNameBase("org.netbeans.examples.modules.dummy");
        assertNotNull("found myself", e);
        assertEquals("org.netbeans.examples.modules.dummy", e.getCnb());
        assertEquals(file(nball, "apisupport/project/test/unit/data/example-external-projects/suite3/dummy-project/build/cluster/modules/org-netbeans-examples-modules-dummy.jar"), e.getJar());
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.netbeans.modules.classfile");
        assertNotNull("found (fake) netbeans.org module by its binary", e);
        assertEquals("org.netbeans.modules.classfile", e.getCnb());
    }
    
}
