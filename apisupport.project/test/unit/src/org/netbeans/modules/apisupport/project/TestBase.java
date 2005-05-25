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
package org.netbeans.modules.apisupport.project;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Basic setup for all the tests.
 * @author Jesse Glick
 */
abstract class TestBase extends NbTestCase {
    
    protected TestBase(String name) {
        super(name);
    }
    
    protected static String EEP = "apisupport/project/test/unit/data/example-external-projects";
    
    protected File nbrootF;
    protected FileObject nbroot;
    protected File extexamplesF;
    protected FileObject extexamples;
    protected File apisZip;
    protected void setUp() throws Exception {
        super.setUp();
        nbrootF = new File(System.getProperty("test.nbroot"));
        assertTrue("there is a dir " + nbrootF, nbrootF.isDirectory());
        assertTrue("nbbuild exists", new File(nbrootF, "nbbuild").isDirectory());
        nbroot = FileUtil.toFileObject(nbrootF);
        assertNotNull("have a file object for nbroot", nbroot);
        extexamplesF = file(nbrootF, EEP);
        assertTrue("there is a dir " + extexamplesF, extexamplesF.isDirectory());
        extexamples = FileUtil.toFileObject(extexamplesF);
        assertNotNull("have a file object for extexamples", extexamples);
        // Need to set up private locations in extexamples, as if they were opened in the IDE.
        clearWorkDir();
        // For PropertyUtils.userBuildProperties():
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        File userPropertiesFile = new File(getWorkDir(), "build.properties");
        Properties p = new Properties();
        p.setProperty("nbplatform.default.netbeans.dest.dir", file("nbbuild/netbeans").getAbsolutePath());
        p.setProperty("nbplatform.default.harness.dir", "${nbplatform.default.netbeans.dest.dir}/harness");
        p.setProperty("nbplatform.custom.netbeans.dest.dir", file(extexamplesF, "suite3/nbplatform").getAbsolutePath());
        // Nonexistent path, just for JavadocForBuiltModuleTest:
        apisZip = new File(getWorkDir(), "apis.zip");
        p.setProperty("nbplatform.default.javadoc", apisZip.getAbsolutePath());
        // Make source association work to find misc-project from its binary:
        p.setProperty("nbplatform.default.sources", nbrootF.getAbsolutePath() + ":" + file(extexamplesF, "suite2").getAbsolutePath());
        OutputStream os = new FileOutputStream(userPropertiesFile);
        try {
            p.store(os, null);
        } finally {
            os.close();
        }
        String[] suites = {
            // Suite projects:
            "suite1",
            "suite2",
            // Standalone module projects:
            "suite3/dummy-project",
        };
        for (int i = 0; i < suites.length; i++) {
            File platformPrivate = file(extexamplesF, suites[i] + "/nbproject/private/platform-private.properties");
            p = new Properties();
            p.setProperty("user.properties.file", userPropertiesFile.getAbsolutePath());
            platformPrivate.getParentFile().mkdirs();
            os = new FileOutputStream(platformPrivate);
            try {
                p.store(os, null);
            } finally {
                os.close();
            }
        }
        NbPlatform.reset();
    }
    
    protected File file(File root, String path) {
        return new File(root, path.replace('/', File.separatorChar));
    }
    
    protected File file(String path) {
        return file(nbrootF, path);
    }
    
}
