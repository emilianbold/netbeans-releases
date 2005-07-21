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

import java.net.URL;
import java.text.Collator;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.SortedSet;
import org.netbeans.modules.apisupport.project.*;

/**
 * Test functionality of {@link NbPlatform}.
 * @author Jesse Glick
 */
public class NbPlatformTest extends TestBase {
    
    public NbPlatformTest(String name) {
        super(name);
    }
    
    public void testBasicUsage() throws Exception {
        SortedSet/*<NbPlatform>*/ platforms = NbPlatform.getPlatforms();
        assertEquals("have two platforms", 2, platforms.size());
        assertTrue("Platforms order", Collator.getInstance().compare(
                ((NbPlatform) platforms.first()).getLabel(),
                ((NbPlatform) platforms.last()).getLabel()) < 0);
        NbPlatform def = NbPlatform.getPlatformByID("default");
        assertNotNull("have default platform", def);
        assertEquals("can use a special method call for that", def, NbPlatform.getDefaultPlatform());
        NbPlatform custom = NbPlatform.getPlatformByID("custom");
        assertNotNull("have custom platform", custom);
        assertNull("no such bogus platform", NbPlatform.getPlatformByID("bogus"));
        assertEquals(new HashSet(Arrays.asList(new NbPlatform[] {def, custom})), platforms);
        assertEquals("right default platform by dest dir", def, NbPlatform.getPlatformByDestDir(file("nbbuild/netbeans")));
        assertEquals("right custom platform by dest dir", custom, NbPlatform.getPlatformByDestDir(file(extexamplesF, "suite3/nbplatform")));
        assertEquals("right dest dir for default platform", file("nbbuild/netbeans"), def.getDestDir());
        assertEquals("right dest dir for custom platform", file(extexamplesF, "suite3/nbplatform"), custom.getDestDir());
        assertEquals("right name for default platform", "default", def.getID());
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
        assertEquals("Right source location for beans.jar", file("beans"), p.getSourceLocationOfModule(file("nbbuild/netbeans/ide5/modules/org-netbeans-modules-beans.jar")));
    }
    
    public void testIsPlatformDirectory() throws Exception {
        assertTrue("nbbuild/netbeans is a platform", NbPlatform.isPlatformDirectory(file("nbbuild/netbeans")));
        assertFalse("platform5 is not a platform", NbPlatform.isPlatformDirectory(file("nbbuild/netbeans/platform5")));
        assertFalse("nbbuild is not a platform", NbPlatform.isPlatformDirectory(file("nbbuild")));
        assertFalse("nbbuild/build.xml is not a platform", NbPlatform.isPlatformDirectory(file("nbbuild/build.xml")));
        assertFalse("nonexistent dir is not a platform", NbPlatform.isPlatformDirectory(file("nonexistent")));
    }
    
    public void testComputeDisplayName() throws Exception {
        String name = NbPlatform.computeDisplayName(file("nbbuild/netbeans"));
        //System.out.println("name: " + name);
        assertTrue("name mentions 'NetBeans IDE'", name.indexOf("NetBeans IDE") != -1);
    }
    
    public void testAddSourceRoot() throws Exception {
        NbPlatform def = NbPlatform.getPlatformByID("default");
        doAddSourceRoot(def, new URL("file:/nonsense/"));
    }
    
    public void testRemoveRoots() throws Exception {
        URL url = new URL("file:/nonsense/");
        NbPlatform def = NbPlatform.getPlatformByID("default");
        doAddSourceRoot(def, url);
        assertTrue("adding of new source root", Arrays.asList( // double check
                def.getSourceRoots()).toString().indexOf("nonsense") != -1);
        def.removeSourceRoots(new URL[] {url});
        assertFalse("removing of new source root", Arrays.asList( // double check
                def.getSourceRoots()).toString().indexOf("nonsense") != -1);
    }
    
    public void testMovingSourceRoots() throws Exception {
        NbPlatform def = NbPlatform.getPlatformByID("default");
        URL url = new URL("file:/nonsense/");
        doAddSourceRoot(def, url);
        
        def = NbPlatform.getPlatformByID("default");
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
        NbPlatform def = NbPlatform.getPlatformByID("default");
        doAddJavadocRoot(def, new URL("file:/nonsense/"));
    }
    
    public void testRemoveJavadocs() throws Exception {
        URL url = new URL("file:/nonsense/");
        NbPlatform def = NbPlatform.getPlatformByID("default");
        doAddJavadocRoot(def, url);
        assertTrue("adding of new javadoc", Arrays.asList( // double check
                def.getJavadocRoots()).toString().indexOf("nonsense") != -1);
        def.removeJavadocRoots(new URL[] {url});
        assertFalse("removing of new javadoc", Arrays.asList( // double check
                def.getJavadocRoots()).toString().indexOf("nonsense") != -1);
    }

    public void testMovingJavadocRoots() throws Exception {
        NbPlatform def = NbPlatform.getPlatformByID("default");
        URL url = new URL("file:/nonsense/");
        doAddJavadocRoot(def, url);

        def = NbPlatform.getPlatformByID("default");
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

    private void doAddSourceRoot(NbPlatform plaf, URL urlToAdd) throws Exception {
        plaf.addSourceRoot(urlToAdd);
        // reload platform
        NbPlatform.reset();
        plaf = NbPlatform.getPlatformByID(plaf.getID());
        assertTrue("adding of new sourceroot", Arrays.asList(
                plaf.getSourceRoots()).toString().indexOf("nonsense") != -1);
    }

    private void doAddJavadocRoot(NbPlatform plaf, URL urlToAdd) throws Exception {
        plaf.addJavadocRoot(urlToAdd);
        // reload platform
        NbPlatform.reset();
        plaf = NbPlatform.getPlatformByID(plaf.getID());
        assertTrue("adding of new javadoc", Arrays.asList(
                plaf.getJavadocRoots()).toString().indexOf("nonsense") != -1);
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
        NbPlatform def = NbPlatform.getPlatformByID("default");
        assertFalse("already used label", NbPlatform.isLabelValid(def.getLabel()));
        assertFalse("null label", NbPlatform.isLabelValid(null));
        assertTrue("valid label", NbPlatform.isLabelValid("whatever"));
    }
    
}
