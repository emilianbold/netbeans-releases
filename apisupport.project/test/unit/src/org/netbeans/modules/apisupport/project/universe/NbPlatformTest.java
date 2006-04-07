/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.universe;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;

/**
 * Test functionality of {@link NbPlatform}.
 * @author Jesse Glick
 */
public class NbPlatformTest extends TestBase {
    
    private static final String ARTIFICIAL_DIR = "artificial/";
    
    public NbPlatformTest(String name) {
        super(name);
    }
    
    public void testBasicUsage() throws Exception {
        Set/*<NbPlatform>*/ platforms = NbPlatform.getPlatforms();
        assertEquals("have two platforms", 2, platforms.size());
        NbPlatform def = NbPlatform.getPlatformByID(NbPlatform.PLATFORM_ID_DEFAULT);
        assertNotNull("have default platform", def);
        assertEquals("can use a special method call for that", def, NbPlatform.getDefaultPlatform());
        NbPlatform custom = NbPlatform.getPlatformByID("custom");
        assertNotNull("have custom platform", custom);
        assertNull("no such bogus platform", NbPlatform.getPlatformByID("bogus"));
        assertEquals(new HashSet(Arrays.asList(new NbPlatform[] {def, custom})), platforms);
        assertEquals("right default platform by dest dir", def, NbPlatform.getPlatformByDestDir(file("nbbuild/netbeans")));
        assertEquals("right custom platform by dest dir", custom, NbPlatform.getPlatformByDestDir(file(extexamplesF, "suite3/nbplatform")));
        assertFalse("bogus platform is not valid", NbPlatform.getPlatformByDestDir(file("nbbuild")).isValid());
        assertFalse("bogus platform is not default", NbPlatform.getPlatformByDestDir(file("nbbuild")).isDefault());
        assertEquals("right dest dir for default platform", file("nbbuild/netbeans"), def.getDestDir());
        assertEquals("right dest dir for custom platform", file(extexamplesF, "suite3/nbplatform"), custom.getDestDir());
        assertEquals("right name for default platform", NbPlatform.PLATFORM_ID_DEFAULT, def.getID());
        assertEquals("right name for custom platform", "custom", custom.getID());
        assertEquals("right sources for default platform", new HashSet(Arrays.asList(new URL[] {
            Util.urlForDir(nbrootF),
            Util.urlForDir(file(extexamplesF, "suite2")),
        })), new HashSet(Arrays.asList(def.getSourceRoots())));
        assertEquals("right Javadoc for default platform", new HashSet(Arrays.asList(new URL[] {
            Util.urlForJar(apisZip),
        })), new HashSet(Arrays.asList(def.getJavadocRoots())));
        assertEquals("no sources for custom platform", Collections.EMPTY_SET, new HashSet(Arrays.asList(custom.getSourceRoots())));
        assertEquals("no Javadoc for custom platform", Collections.EMPTY_SET, new HashSet(Arrays.asList(custom.getJavadocRoots())));
    }
    
    public void testGetSourceLocationOfModule() throws Exception {
        NbPlatform p = NbPlatform.getDefaultPlatform();
        assertEquals("Right source location for beans.jar", file("beans"), p.getSourceLocationOfModule(file("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-beans.jar")));
    }
    
    public void testIsPlatformDirectory() throws Exception {
        assertTrue("nbbuild/netbeans is a platform", NbPlatform.isPlatformDirectory(file("nbbuild/netbeans")));
        assertFalse(TestBase.CLUSTER_PLATFORM + " is not a platform", NbPlatform.isPlatformDirectory(file("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM)));
        assertFalse("nbbuild is not a platform", NbPlatform.isPlatformDirectory(file("nbbuild")));
        assertFalse("nbbuild/build.xml is not a platform", NbPlatform.isPlatformDirectory(file("nbbuild/build.xml")));
        assertFalse("nonexistent dir is not a platform", NbPlatform.isPlatformDirectory(file("nonexistent")));
    }
    
    public void testComputeDisplayName() throws Exception {
        String name = NbPlatform.computeDisplayName(file("nbbuild/netbeans"));
        //System.out.println("name: " + name);
        assertTrue("name '" + name + "' mentions 'NetBeans IDE'", name.indexOf("NetBeans IDE") != -1);
    }
    
    public void testAddSourceRoot() throws Exception {
        NbPlatform def = NbPlatform.getPlatformByID(NbPlatform.PLATFORM_ID_DEFAULT);
        doAddSourceRoot(def, ARTIFICIAL_DIR);
    }
    
    public void testRemoveRoots() throws Exception {
        NbPlatform def = NbPlatform.getPlatformByID(NbPlatform.PLATFORM_ID_DEFAULT);
        URL url = doAddSourceRoot(def, ARTIFICIAL_DIR);
        assertTrue("adding of new source root", Arrays.asList( // double check
                def.getSourceRoots()).toString().indexOf(ARTIFICIAL_DIR) != -1);
        def.removeSourceRoots(new URL[] {url});
        assertFalse("removing of new source root", Arrays.asList( // double check
                def.getSourceRoots()).toString().indexOf(ARTIFICIAL_DIR) != -1);
    }
    
    public void testMovingSourceRoots() throws Exception {
        NbPlatform def = NbPlatform.getPlatformByID(NbPlatform.PLATFORM_ID_DEFAULT);
        URL url = doAddSourceRoot(def, ARTIFICIAL_DIR);
        
        def = NbPlatform.getPlatformByID(NbPlatform.PLATFORM_ID_DEFAULT);
        URL[] srcRoot = def.getSourceRoots();
        assertEquals("new url should be the last one", url, srcRoot[srcRoot.length - 1]);
        def.moveSourceRootUp(srcRoot.length - 1);
        srcRoot = def.getSourceRoots();
        assertEquals("new url should be moved up", url, srcRoot[srcRoot.length - 2]);
        
        URL first = srcRoot[0];
        def.moveSourceRootDown(0);
        srcRoot = def.getSourceRoots();
        assertEquals("first url should be moved to the second position", first, srcRoot[1]);
    }
    
    public void testAddJavadoc() throws Exception {
        NbPlatform def = NbPlatform.getPlatformByID(NbPlatform.PLATFORM_ID_DEFAULT);
        doAddJavadocRoot(def, ARTIFICIAL_DIR);
    }
    
    public void testRemoveJavadocs() throws Exception {
        NbPlatform def = NbPlatform.getPlatformByID(NbPlatform.PLATFORM_ID_DEFAULT);
        URL url = doAddJavadocRoot(def, ARTIFICIAL_DIR);
        assertTrue("adding of new javadoc", Arrays.asList( // double check
                def.getJavadocRoots()).toString().indexOf(ARTIFICIAL_DIR) != -1);
        def.removeJavadocRoots(new URL[] {url});
        assertFalse("removing of new javadoc", Arrays.asList( // double check
                def.getJavadocRoots()).toString().indexOf(ARTIFICIAL_DIR) != -1);
    }
    
    public void testMovingJavadocRoots() throws Exception {
        NbPlatform def = NbPlatform.getPlatformByID(NbPlatform.PLATFORM_ID_DEFAULT);
        URL url = doAddJavadocRoot(def, ARTIFICIAL_DIR);
        
        def = NbPlatform.getPlatformByID(NbPlatform.PLATFORM_ID_DEFAULT);
        URL[] jdRoot = def.getJavadocRoots();
        assertEquals("new url should be the last one", url, jdRoot[jdRoot.length - 1]);
        def.moveJavadocRootUp(jdRoot.length - 1);
        jdRoot = def.getJavadocRoots();
        assertEquals("new url should be moved up", url, jdRoot[jdRoot.length - 2]);
        
        URL first = jdRoot[0];
        def.moveJavadocRootDown(0);
        jdRoot = def.getJavadocRoots();
        assertEquals("first url should be moved to the second position", first, jdRoot[1]);
    }
    
    /** Add and tests URL and returns it. */
    private URL doAddSourceRoot(NbPlatform plaf, String urlInWorkDir) throws Exception {
        URL urlToAdd = new URL(getWorkDir().toURI().toURL(), urlInWorkDir);
        plaf.addSourceRoot(urlToAdd);
        // reload platform
        NbPlatform.reset();
        plaf = NbPlatform.getPlatformByID(plaf.getID());
        assertTrue("adding of new sourceroot", Arrays.asList(
                plaf.getSourceRoots()).toString().indexOf(urlInWorkDir) != -1);
        return urlToAdd;
    }
    
    private URL doAddJavadocRoot(NbPlatform plaf, String urlInWorkDir) throws Exception {
        URL urlToAdd = new URL(getWorkDir().toURI().toURL(), urlInWorkDir);
        plaf.addJavadocRoot(urlToAdd);
        // reload platform
        NbPlatform.reset();
        plaf = NbPlatform.getPlatformByID(plaf.getID());
        assertTrue("adding of new javadoc", Arrays.asList(
                plaf.getJavadocRoots()).toString().indexOf(urlInWorkDir) != -1);
        return urlToAdd;
    }
    
    public void testRemovePlatform() throws Exception {
        assertEquals("have two platforms", 2, NbPlatform.getPlatforms().size());
        NbPlatform custom = NbPlatform.getPlatformByID("custom");
        assertNotNull("have custom platform", custom);
        NbPlatform.removePlatform(custom);
        assertEquals("custom platform was deleted platforms", 1, NbPlatform.getPlatforms().size());
        assertNull("custom platform was deleted", NbPlatform.getPlatformByID("custom"));
    }
    
    public void testAddPlatform() throws Exception {
        assertEquals("have two platforms", 2, NbPlatform.getPlatforms().size());
        NbPlatform custom = NbPlatform.getPlatformByID("custom");
        assertNotNull("have custom platform", custom);
        NbPlatform.removePlatform(custom);
        assertEquals("custom platform was deleted platforms", 1, NbPlatform.getPlatforms().size());
        assertNull("custom platform was deleted", NbPlatform.getPlatformByID("custom"));
        NbPlatform.addPlatform(custom.getID(), custom.getDestDir(), "Some Label");
        NbPlatform.addPlatform(custom.getID() + 1, custom.getDestDir(), "Some Label 1");
        assertEquals("have two platforms", 3, NbPlatform.getPlatforms().size());
        assertNotNull("custom platform was added", NbPlatform.getPlatformByID("custom"));
        assertNotNull("custom platform was added", NbPlatform.getPlatformByID("custom1"));
        NbPlatform.reset();
        assertEquals("custom label", "Some Label 1", NbPlatform.getPlatformByID("custom1").getLabel());
    }
    
    public void testIsLabelValid() throws Exception {
        NbPlatform def = NbPlatform.getPlatformByID(NbPlatform.PLATFORM_ID_DEFAULT);
        assertFalse("already used label", NbPlatform.isLabelValid(def.getLabel()));
        assertFalse("null label", NbPlatform.isLabelValid(null));
        assertTrue("valid label", NbPlatform.isLabelValid("whatever"));
    }
    
    public void testIsSupportedPlatform() throws Exception {
        NbPlatform def = NbPlatform.getPlatformByID(NbPlatform.PLATFORM_ID_DEFAULT);
        assertTrue("platform supported", NbPlatform.isSupportedPlatform(def.getDestDir()));
    }
    
    public void testContains() throws Exception {
        assertTrue("contains suite3/nbplatform", NbPlatform.contains(file(extexamplesF, "suite3/nbplatform")));
        assertTrue("contains nbbuild/netbeans", NbPlatform.contains(file("nbbuild/netbeans")));
        assertFalse("doesn't contains whatever/platform", NbPlatform.contains(file("whatever/platform")));
    }
    
    public void testSourceRootsPersistence() throws Exception {
        NbPlatform def = NbPlatform.getPlatformByID(NbPlatform.PLATFORM_ID_DEFAULT);
        assertTrue("platform supported", NbPlatform.isSupportedPlatform(def.getDestDir()));
        File f1 = new File(getWorkDir(),"f1");
        File f2 = new File(getWorkDir(),"f2");
        if (!f1.exists()) {
            assertTrue(f1.mkdir());
        }
        if (!f2.exists()) {
            assertTrue(f2.mkdir());
        }
        URL[] us = new URL[] {f1.toURI().toURL(),f2.toURI().toURL()};
        String path = def.urlsToAntPath(us);
        URL[] rus = def.findURLs(path);
        assertEquals(us.length, rus.length);
        for (int i = 0; i < us.length; i++) {
            assertEquals(path, us[i].toExternalForm(), rus[i].toExternalForm());
        }
    }
    
    public void testHarnessVersionDetection() throws Exception {
        NbPlatform p = NbPlatform.getDefaultPlatform();
        assertEquals("5.0u2 harness detected", NbPlatform.HARNESS_VERSION_50u2, p.getHarnessVersion());
        File testPlatform = new File(getWorkDir(), "test-platform");
        makePlatform(testPlatform);
        p = NbPlatform.getPlatformByDestDir(testPlatform);
        assertEquals("5.0 harness detected", NbPlatform.HARNESS_VERSION_50, p.getHarnessVersion());
        File defaultHarnessLocation = NbPlatform.getDefaultPlatform().getHarnessLocation();
        p = NbPlatform.addPlatform("test", testPlatform, defaultHarnessLocation, "Test");
        assertEquals("5.0u2 harness detected", NbPlatform.HARNESS_VERSION_50u2, p.getHarnessVersion());
        PropertyEvaluator eval = PropertyUtils.sequentialPropertyEvaluator(null, new PropertyProvider[] {PropertyUtils.globalPropertyProvider()});
        assertEquals(defaultHarnessLocation, FileUtil.normalizeFile(new File(eval.getProperty("nbplatform.test.harness.dir"))));
        NbPlatform.reset();
        p = NbPlatform.getPlatformByID("test");
        assertNotNull(p);
        assertEquals(testPlatform, p.getDestDir());
        assertEquals(defaultHarnessLocation, p.getHarnessLocation());
        assertEquals(NbPlatform.HARNESS_VERSION_50u2, p.getHarnessVersion());
    }
    
    public void testSourceRootChangeFiring() throws Exception {
        NbPlatform p = NbPlatform.getDefaultPlatform();
        SourceRootsPCL pcl = new SourceRootsPCL();
        p.addPropertyChangeListener(pcl);
        assertFalse("source roots has not changed yet (sanity check)", pcl.sourcesChanged);
        doAddSourceRoot(p, ARTIFICIAL_DIR);
        assertTrue("source roots has changed", pcl.sourcesChanged);
    }
    
    private static final class SourceRootsPCL implements PropertyChangeListener {
        
        boolean sourcesChanged;
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == NbPlatform.PROP_SOURCE_ROOTS) {
                sourcesChanged = true;
            }
        }
        
    }
    
    // XXX testHarnessSelection
    
}
