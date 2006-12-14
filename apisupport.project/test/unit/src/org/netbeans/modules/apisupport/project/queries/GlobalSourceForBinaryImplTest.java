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
import org.openide.filesystems.FileObject;
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
            "<data xmlns=\"http://www.netbeans.org/ns/nb-module-project/3\">\n" +
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
        
        URL loadersURL = Util.urlForJar(file("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
        URL loadersSrcURL = new URL(Util.urlForJar(nbSrcZip), "openide/loaders/src/");
        assertRoot(loadersURL, URLMapper.findFileObject(loadersSrcURL));
    }
    
    public void testFindSourceRootForZipWithSecondLevelDepthNbBuild() throws Exception {
        File nbSrcZip = generateNbSrcZip("netbeans-src/");
        NbPlatform.getDefaultPlatform().addSourceRoot(Util.urlForJar(nbSrcZip));
        
        URL loadersURL = Util.urlForJar(file("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
        URL loadersSrcURL = new URL(Util.urlForJar(nbSrcZip), "netbeans-src/openide/loaders/src/");
        assertRoot(loadersURL, URLMapper.findFileObject(loadersSrcURL));
    }
    
    // just sanity check that exception is not thrown
    public void testBehaviourWithNonZipFile() throws Exception {
        GlobalSourceForBinaryImpl.quiet = true;
        File nbSrcZip = new File(getWorkDir(), "wrong-nbsrc.zip");
        nbSrcZip.createNewFile();
        NbPlatform.getDefaultPlatform().addSourceRoot(Util.urlForJar(nbSrcZip));
        URL loadersURL = Util.urlForJar(file("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
        SourceForBinaryQuery.findSourceRoots(loadersURL).getRoots();
    }
    
    public void testResolveSpecialNBSrcPaths() throws Exception {
        String[] srcDirs = {"xtest/nbjunit/src",
                            "xtest/nbjunit/ide/src",
                            "performance/insanelib/src"};
        String[] xtestJars = {"xtest/lib/nbjunit.jar",
                            "xtest/lib/nbjunit-ide.jar",
                            "xtest/lib/insanelib.jar"};
        String[] ideJars = {"testtools/modules/org-netbeans-modules-nbjunit.jar",
                            "testtools/modules/org-netbeans-modules-nbjunit-ide.jar",
                            "testtools/modules/ext/insanelib.jar"};
        for (int i = 0; i < srcDirs.length; i++) {
            if(!file(srcDirs[i]).isDirectory()) {
                System.err.println("Skipping testResolveSpecialNBSrcPaths since "+srcDirs[i]+" is not checked out");
                continue;
            }
            assertRoot(Util.urlForJar(file(xtestJars[i])),
                    FileUtil.toFileObject(file(srcDirs[i])));
            File jarFile = new File(file("nbbuild/netbeans"), ideJars[i]);
            if (jarFile.exists()) {
                assertResolved(ideJars[i], srcDirs[i]);
            } else {
                assertEquals("no resolved root", 0,
                        SourceForBinaryQuery.findSourceRoots(Util.urlForJar(jarFile)).getRoots().length);
            }
        }
    }
    
    private void assertResolved(String jarInNBBuild, String dirInNBSrc) {
        File jarFile = new File(file("nbbuild/netbeans"), jarInNBBuild);
        assertRoot(Util.urlForJar(jarFile), FileUtil.toFileObject((file(dirInNBSrc))));
    }
    
    public void testListeningToNbPlatform() throws Exception {
        File nbSrcZip = generateNbSrcZip("");
        URL loadersURL = Util.urlForJar(file("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
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
        assertRoot(loadersURL, URLMapper.findFileObject(loadersSrcURL));
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
    
    private static void assertRoot(final URL loadersURL, final FileObject loadersSrcFO) {
        assertEquals("right results for " + loadersURL,
                Collections.singletonList(loadersSrcFO),
                Arrays.asList(SourceForBinaryQuery.findSourceRoots(loadersURL).getRoots()));
    }
    
    private static final class ResultChangeListener implements ChangeListener {
        
        private boolean changed;
        
        public void stateChanged(ChangeEvent e) {
            changed = true;
        }
        
    }
    
}
