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
import java.io.IOException;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectGenerator;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.SharedClassObject;

/**
 * Check that missing or invalid *.properties files do not badly break projects.
 * More or less corresponds to issue #66404 and others.
 * @author Jesse Glick
 */
public final class BrokenPlatformReferenceTest extends NbTestCase {
    
    public BrokenPlatformReferenceTest(String name) {
        super(name);
    }

    /** a fake but valid-looking install dir; the default NB platform */
    private File install;
    /** the user dir */
    private File user;
    
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        NbPlatform.reset();
        user = new File(getWorkDir(), "user");
        user.mkdirs();
        System.setProperty("netbeans.user", user.getAbsolutePath());
        install = new File(getWorkDir(), "install");
        makePlatform(install);
        // Now set up build.properties accordingly:
        InstalledFileLocatorImpl.registerDestDir(install);
        ((Install) SharedClassObject.findObject(Install.class, true)).restored();
        assertEquals("set up run correctly", install.getAbsolutePath(), PropertyUtils.getGlobalProperties().getProperty("nbplatform.default.netbeans.dest.dir"));
    }
    
    private static void makePlatform(File d) throws IOException {
        // To satisfy NbPlatform.defaultPlatformLocation and NbPlatform.isValid:
        TestBase.dump(new File(new File(new File(d, "platform"), "core"), "core.jar"), "");
        TestBase.dump(new File(new File(d, "harness"), "suite.xml"), "");
    }
    
    public void testEverythingNormal() throws Exception {
        // Make sure everything is working as expected when there are no breakages.
        File install2 = new File(getWorkDir(), "install2");
        makePlatform(install2);
        NbPlatform.addPlatform("install2", install2, "install2");
        // Try making a standalone module w/ default platform, confirm loaded OK.
        File d = new File(getWorkDir(), "standalone");
        NbModuleProjectGenerator.createStandAloneModule(d, "x", "X", null, null, NbPlatform.PLATFORM_ID_DEFAULT);
        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(d));
        NbPlatform pl = p.getPlatform(false);
        assertNotNull(pl);
        assertEquals(install, pl.getDestDir());
        assertEquals(pl, p.getPlatform(true));
        // Same but w/ a non-default platform.
        d = new File(getWorkDir(), "standalone2");
        NbModuleProjectGenerator.createStandAloneModule(d, "x", "X", null, null, "install2");
        p = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(d));
        pl = p.getPlatform(false);
        assertNotNull(pl);
        assertEquals(install2, pl.getDestDir());
        // Same for suites.
        File sd = new File(getWorkDir(), "suite");
        SuiteProjectGenerator.createSuiteProject(sd, NbPlatform.PLATFORM_ID_DEFAULT);
        d = new File(getWorkDir(), "suitecomp");
        NbModuleProjectGenerator.createSuiteComponentModule(d, "x", "X", null, null, sd);
        SuiteProject s = (SuiteProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(sd));
        pl = s.getPlatform(false);
        assertNotNull(pl);
        assertEquals(install, pl.getDestDir());
        assertEquals(pl, s.getPlatform(true));
        p = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(d));
        assertEquals(pl, p.getPlatform(false));
        // And again w/ a non-default platform.
        sd = new File(getWorkDir(), "suite2");
        SuiteProjectGenerator.createSuiteProject(sd, "install2");
        d = new File(getWorkDir(), "suitecomp2");
        NbModuleProjectGenerator.createSuiteComponentModule(d, "x", "X", null, null, sd);
        s = (SuiteProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(sd));
        pl = s.getPlatform(false);
        assertNotNull(pl);
        assertEquals(install2, pl.getDestDir());
        p = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(d));
        assertEquals(pl, p.getPlatform(false));
    }
    
    // XXX to test, for suite projects, suite component module projects, and standalone projects:
    // - return default platform if ${netbeans.dest.dir} undefined in any way or not pointing to valid platform
    // - OpenProjectHook fixes, or creates, platform-private.properties to point to current build.properties
    // - in OPH, platform.properties is fixed to use default if no value for nbplatform.active or points to invalid platform
    // - any changes in evaluator, or build.properties, resulting in an invalid platform choice for loaded project make evaluator use default platform (?)
    // - all problems are notified to user (maybe move ModuleProperties.reportLostPlatform, and change MP.runFromTests)
    // - migrate hacks around getModuleList to work just on platform (if module not listed in own module list, too bad...)?
    
}
