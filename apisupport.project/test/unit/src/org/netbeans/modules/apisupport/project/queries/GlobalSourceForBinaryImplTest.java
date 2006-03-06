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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 * Test functionality of GlobalSourceForBinaryImpl.
 *
 * @author Martin Krauskopf
 */
public class GlobalSourceForBinaryImplTest extends TestBase {
    
    // Doesn't need to be precise and/or valid. Should show what actual
    // GlobalSourceForBinaryImpl works with.
    private static final String LOADERS_XML =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project xmlns=\"http://www.netbeans.org/ns/project/1\">\n" +
            "<type>org.netbeans.modules.apisupport.project</type>\n" +
            "<configuration>\n" +
            "<data xmlns=\"http://www.netbeans.org/ns/nb-module-project/2\">\n" +
            "<code-name-base>org.openide.loaders</code-name-base>\n" +
            "</data>\n" +
            "</configuration>\n" +
            "</project>\n";
    
    public GlobalSourceForBinaryImplTest(String name) {
        super(name);
    }
    
    public void testFindSourceRootForZipWithFirstLevelDepthNbBuild() throws Exception {
        File nbSrcZip = generateNbSrcZip("");
        NbPlatform.getDefaultPlatform().addSourceRoot(Util.urlForJar(nbSrcZip));
        
        URL loadersURL = Util.urlForJar(file("nbbuild/netbeans/platform7/modules/org-openide-loaders.jar"));
        URL loadersSrcURL = new URL(Util.urlForJar(nbSrcZip), "openide/loaders/src/");
        assertEquals("right results for " + loadersURL,
                Collections.singletonList(URLMapper.findFileObject(loadersSrcURL)),
                Arrays.asList(SourceForBinaryQuery.findSourceRoots(loadersURL).getRoots()));
    }
    
    public void testFindSourceRootForZipWithSecondLevelDepthNbBuild() throws Exception {
        File nbSrcZip = generateNbSrcZip("netbeans-src/");
        NbPlatform.getDefaultPlatform().addSourceRoot(Util.urlForJar(nbSrcZip));
        
        URL loadersURL = Util.urlForJar(file("nbbuild/netbeans/platform7/modules/org-openide-loaders.jar"));
        URL loadersSrcURL = new URL(Util.urlForJar(nbSrcZip), "netbeans-src/openide/loaders/src/");
        assertEquals("right results for " + loadersURL,
                Collections.singletonList(URLMapper.findFileObject(loadersSrcURL)),
                Arrays.asList(SourceForBinaryQuery.findSourceRoots(loadersURL).getRoots()));
    }
    
    // just sanity check that exception is not thrown
    public void testBehaviourWithNonZipFile() throws Exception {
        GlobalSourceForBinaryImpl.quiet = true;
        File nbSrcZip = new File(getWorkDir(), "wrong-nbsrc.zip");
        nbSrcZip.createNewFile();
        NbPlatform.getDefaultPlatform().addSourceRoot(Util.urlForJar(nbSrcZip));
        URL loadersURL = Util.urlForJar(file("nbbuild/netbeans/platform7/modules/org-openide-loaders.jar"));
        SourceForBinaryQuery.findSourceRoots(loadersURL).getRoots();
    }
    
    public void testResolveSpecialNBSrcPaths() throws Exception {
        assertResolved("testtools/modules/ext/nbjunit.jar", "xtest/nbjunit/src");
        assertResolved("testtools/modules/ext/nbjunit-ide.jar", "xtest/nbjunit/src");
        assertTrue("performance.netbeans.org checked out", file("performance").isDirectory());
        assertResolved("testtools/modules/ext/insanelib.jar", "performance/insanelib/src");
    }
    
    public void testListeningToNbPlatform() throws Exception {
        File nbSrcZip = generateNbSrcZip("");
        URL loadersURL = Util.urlForJar(file("nbbuild/netbeans/platform7/modules/org-openide-loaders.jar"));
        SourceForBinaryQuery.Result res = SourceForBinaryQuery.findSourceRoots(loadersURL);
        assertNotNull("got result", res);
        ResultChangeListener resultCL = new ResultChangeListener();
        res.addChangeListener(resultCL);
        assertFalse("not changed yet", resultCL.changed);
        assertEquals("non source root", 0, res.getRoots().length);
        NbPlatform.getDefaultPlatform().addSourceRoot(Util.urlForJar(nbSrcZip));
        assertTrue("changed yet", resultCL.changed);
        assertEquals("one source root", 1, res.getRoots().length);
        URL loadersSrcURL = new URL(Util.urlForJar(nbSrcZip), "openide/loaders/src/");
        assertEquals("right results for " + loadersURL,
                Collections.singletonList(URLMapper.findFileObject(loadersSrcURL)),
                Arrays.asList(SourceForBinaryQuery.findSourceRoots(loadersURL).getRoots()));
    }
    
    private void assertResolved(String jarInNBBuild, String dirInNBSrc) {
        File jarFile = new File(file("nbbuild/netbeans"), jarInNBBuild);
        assertEquals("right result for " + jarFile.getAbsolutePath(),
                Collections.singletonList(FileUtil.toFileObject((file(dirInNBSrc)))),
                Arrays.asList(SourceForBinaryQuery.findSourceRoots(Util.urlForJar(jarFile)).getRoots()));
    }
    
    private File generateNbSrcZip(String topLevelEntry) throws IOException {
        File zip = new File(getWorkDir(), "nbsrc.zip");
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip));
        if (topLevelEntry.length() != 0) {
            zos.putNextEntry(new ZipEntry(topLevelEntry));
        }
        zos.putNextEntry(new ZipEntry(topLevelEntry + "nbbuild/"));
        zos.putNextEntry(new ZipEntry(topLevelEntry + "nbbuild/nbproject/"));
        zos.putNextEntry(new ZipEntry(topLevelEntry + "nbbuild/nbproject/project.xml"));
        zos.putNextEntry(new ZipEntry(topLevelEntry + "openide/"));
        zos.putNextEntry(new ZipEntry(topLevelEntry + "openide/loaders/"));
        zos.putNextEntry(new ZipEntry(topLevelEntry + "openide/loaders/src/"));
        zos.putNextEntry(new ZipEntry(topLevelEntry + "openide/loaders/nbproject/"));
        ZipEntry loadersXML = new ZipEntry(topLevelEntry + "openide/loaders/nbproject/project.xml");
        zos.putNextEntry(loadersXML);
        try {
            zos.write(LOADERS_XML.getBytes());
        } finally {
            zos.close();
        }
        return zip;
    }
    
    private static final class ResultChangeListener implements ChangeListener {
        
        private boolean changed;
        
        public void stateChanged(ChangeEvent e) {
            changed = true;
        }
        
    }
    
}
