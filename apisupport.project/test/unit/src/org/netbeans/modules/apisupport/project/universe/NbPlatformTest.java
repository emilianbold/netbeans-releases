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

package org.netbeans.modules.apisupport.project.universe;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
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
        Set<NbPlatform> platforms = NbPlatform.getPlatforms();
        assertEquals("have two platforms", 2, platforms.size());
        NbPlatform def = NbPlatform.getPlatformByID(NbPlatform.PLATFORM_ID_DEFAULT);
        assertNotNull("have default platform", def);
        assertEquals("can use a special method call for that", def, NbPlatform.getDefaultPlatform());
        NbPlatform custom = NbPlatform.getPlatformByID("custom");
        assertNotNull("have custom platform", custom);
        assertNull("no such bogus platform", NbPlatform.getPlatformByID("bogus"));
        assertEquals(new HashSet<NbPlatform>(Arrays.asList(def, custom)), platforms);
        assertEquals("right default platform by dest dir", def, NbPlatform.getPlatformByDestDir(destDirF));
        assertEquals("right custom platform by dest dir", custom, NbPlatform.getPlatformByDestDir(resolveEEPFile("suite3/nbplatform")));
        assertFalse("bogus platform is not valid", NbPlatform.getPlatformByDestDir(file("nbbuild")).isValid());
        assertFalse("bogus platform is not default", NbPlatform.getPlatformByDestDir(file("nbbuild")).isDefault());
        assertEquals("right dest dir for default platform", destDirF, def.getDestDir());
        assertEquals("right dest dir for custom platform", resolveEEPFile("suite3/nbplatform"), custom.getDestDir());
        assertEquals("right name for default platform", NbPlatform.PLATFORM_ID_DEFAULT, def.getID());
        assertEquals("right name for custom platform", "custom", custom.getID());
        assertEquals("right sources for default platform", new HashSet<URL>(Arrays.asList(
            Util.urlForDir(nbRootFile()),
            Util.urlForDir(resolveEEPFile("suite2"))
        )), new HashSet<URL>(Arrays.asList(def.getSourceRoots())));
        assertEquals("right Javadoc for default platform", new HashSet<URL>(Arrays.asList(
            Util.urlForJar(apisZip)
        )), new HashSet<URL>(Arrays.asList(def.getJavadocRoots())));
        assertEquals("no sources for custom platform", Collections.emptySet(), new HashSet<URL>(Arrays.asList(custom.getSourceRoots())));
        assertEquals("no Javadoc for custom platform", Collections.emptySet(), new HashSet<URL>(Arrays.asList(custom.getJavadocRoots())));
    }
    
    public void testGetSourceLocationOfModule() throws Exception {
        NbPlatform p = NbPlatform.getDefaultPlatform();
        assertEquals("Right source location for image.jar", file("image"), p.getSourceLocationOfModule(file("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-image.jar")));
    }
    
    public void testIsPlatformDirectory() throws Exception {
        assertTrue("nbbuild/netbeans is a platform", NbPlatform.isPlatformDirectory(destDirF));
        assertFalse(TestBase.CLUSTER_PLATFORM + " is not a platform", NbPlatform.isPlatformDirectory(file("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM)));
        assertFalse("nbbuild is not a platform", NbPlatform.isPlatformDirectory(file("nbbuild")));
        assertFalse("nbbuild/build.xml is not a platform", NbPlatform.isPlatformDirectory(file("nbbuild/build.xml")));
        assertFalse("nonexistent dir is not a platform", NbPlatform.isPlatformDirectory(file("nonexistent")));
    }
    
    public void testComputeDisplayName() throws Exception {
        String name = NbPlatform.computeDisplayName(destDirF);
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
        try {
            NbPlatform.addPlatform(custom.getID(), custom.getDestDir(), "Duplicate");
            fail("should have rejected this");
        } catch (IOException x) {
            // OK
        }
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
        assertTrue("contains suite3/nbplatform", NbPlatform.contains(resolveEEPFile("suite3/nbplatform")));
        assertTrue("contains nbbuild/netbeans", NbPlatform.contains(destDirF));
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
        URL[] us = {f1.toURI().toURL(), f2.toURI().toURL()};
        String path = NbPlatform.urlsToAntPath(us);
        URL[] rus = NbPlatform.findURLs(path);
        assertEquals(path, Arrays.asList(us), Arrays.asList(rus));
    }
    
    public void testHarnessVersionDetection() throws Exception {
        NbPlatform p = NbPlatform.getDefaultPlatform();
        assertEquals("6.1 harness detected", NbPlatform.HARNESS_VERSION_61, p.getHarnessVersion());
        File testPlatform = new File(getWorkDir(), "test-platform");
        makePlatform(testPlatform);
        p = NbPlatform.getPlatformByDestDir(testPlatform);
        assertEquals("5.0 harness detected", NbPlatform.HARNESS_VERSION_50, p.getHarnessVersion());
        File defaultHarnessLocation = NbPlatform.getDefaultPlatform().getHarnessLocation();
        p = NbPlatform.addPlatform("test", testPlatform, defaultHarnessLocation, "Test");
        assertEquals("6.1 harness detected", NbPlatform.HARNESS_VERSION_61, p.getHarnessVersion());
        PropertyEvaluator eval = PropertyUtils.sequentialPropertyEvaluator(null, PropertyUtils.globalPropertyProvider());
        assertEquals(defaultHarnessLocation, FileUtil.normalizeFile(new File(eval.getProperty("nbplatform.test.harness.dir"))));
        NbPlatform.reset();
        p = NbPlatform.getPlatformByID("test");
        assertNotNull(p);
        assertEquals(testPlatform, p.getDestDir());
        assertEquals(defaultHarnessLocation, p.getHarnessLocation());
        assertEquals(NbPlatform.HARNESS_VERSION_61, p.getHarnessVersion());
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
            if (NbPlatform.PROP_SOURCE_ROOTS.equals(evt.getPropertyName())) {
                sourcesChanged = true;
            }
        }
        
    }
    
    // XXX testHarnessSelection
    
}
