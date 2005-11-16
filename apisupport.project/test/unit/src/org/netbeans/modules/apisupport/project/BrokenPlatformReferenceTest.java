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
import java.util.Collections;
import java.util.jar.Manifest;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectGenerator;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.EditableProperties;
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
    /** an alternate valid install dir */
    private File install2;
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
        install2 = new File(getWorkDir(), "install2");
        makePlatform(install2);
        NbPlatform.addPlatform("install2", install2, "install2");
    }
    
    private static void makePlatform(File d) throws IOException {
        // To satisfy NbPlatform.defaultPlatformLocation and NbPlatform.isValid, and make at least one module:
        Manifest mani = new Manifest();
        mani.getMainAttributes().putValue("OpenIDE-Module", "core");
        TestBase.createJar(new File(new File(new File(d, "platform"), "core"), "core.jar"), Collections.EMPTY_MAP, mani);
        TestBase.dump(new File(new File(d, "harness"), "suite.xml"), "");
    }
    
    /** Make sure everything is working as expected when there are no breakages. */
    public void testEverythingNormal() throws Exception {
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

    /** Test that use of default platform is OK even if platform-private.properties is initially missing; must be created. */
    public void testMissingPlatformPrivatePropertiesDefaultPlatform() throws Exception {
        // Try making a standalone module w/ default platform.
        File d = new File(getWorkDir(), "standalone");
        NbModuleProjectGenerator.createStandAloneModule(d, "x", "X", null, null, NbPlatform.PLATFORM_ID_DEFAULT);
        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(d));
        open(p);
        assertEquals(Collections.singletonMap("user.properties.file", new File(user, "build.properties").getAbsolutePath()),
                Util.loadProperties(p.getProjectDirectory().getFileObject("nbproject/private/platform-private.properties")));
        // Same for suite.
        File sd = new File(getWorkDir(), "suite");
        SuiteProjectGenerator.createSuiteProject(sd, NbPlatform.PLATFORM_ID_DEFAULT);
        SuiteProject s = (SuiteProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(sd));
        open(s);
        assertEquals(Collections.singletonMap("user.properties.file", new File(user, "build.properties").getAbsolutePath()),
                Util.loadProperties(s.getProjectDirectory().getFileObject("nbproject/private/platform-private.properties")));
    }
    
    /** Test that use of default platform is still fine even if platform-private.properties is initially incorrect; must be corrected. */
    public void testIncorrectPlatformPrivatePropertiesDefaultPlatform() throws Exception {
        // Try making a standalone module w/ default platform.
        File d = new File(getWorkDir(), "standalone");
        NbModuleProjectGenerator.createStandAloneModule(d, "x", "X", null, null, NbPlatform.PLATFORM_ID_DEFAULT);
        FileObject props = FileUtil.createData(FileUtil.toFileObject(d), "nbproject/private/platform-private.properties");
        Util.storeProperties(props, new EditableProperties(Collections.singletonMap("user.properties.file", "bogus")));
        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(d));
        NbPlatform pl = p.getPlatform(true); // with fallback=false, who knows what it will be
        assertNotNull(pl);
        assertEquals(install, pl.getDestDir());
        open(p);
        assertEquals(Collections.singletonMap("user.properties.file", new File(user, "build.properties").getAbsolutePath()),
                Util.loadProperties(props));
        assertEquals(pl, p.getPlatform(true));
        assertEquals(pl, p.getPlatform(false)); // now should be corrected even w/o fallback
        // Same for suite. Check a component module too.
        File sd = new File(getWorkDir(), "suite");
        SuiteProjectGenerator.createSuiteProject(sd, NbPlatform.PLATFORM_ID_DEFAULT);
        props = FileUtil.createData(FileUtil.toFileObject(sd), "nbproject/private/platform-private.properties");
        Util.storeProperties(props, new EditableProperties(Collections.singletonMap("user.properties.file", "bogus")));
        d = new File(getWorkDir(), "suitecomp");
        NbModuleProjectGenerator.createSuiteComponentModule(d, "x", "X", null, null, sd);
        SuiteProject s = (SuiteProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(sd));
        p = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(d));
        pl = s.getPlatform(true);
        assertNotNull(pl);
        assertEquals(install, pl.getDestDir());
        assertEquals(pl, p.getPlatform(true));
        open(s);
        open(p); // just in case
        assertEquals(Collections.singletonMap("user.properties.file", new File(user, "build.properties").getAbsolutePath()),
                Util.loadProperties(props));
        assertEquals(pl, s.getPlatform(true));
        assertEquals(pl, s.getPlatform(false));
        assertEquals(pl, p.getPlatform(true));
        assertEquals(pl, p.getPlatform(false));
    }
    
    // XXX to test, for suite projects, suite component module projects, and standalone projects:
    // - return default platform if ${netbeans.dest.dir} undefined in any way or not pointing to valid platform [partly tested]
    // - OpenProjectHook fixes, or creates, platform-private.properties to point to current build.properties [in progress; need to test non-default platforms valid in new b.props]
    // - test that NbPrj.getModuleList uses a valid dest dir even if some refs are broken [should be implemented, just need test]
    // - in OPH, platform.properties is fixed to use default if no value for nbplatform.active or points to invalid platform
    // - all problems are notified to user (maybe move ModuleProperties.reportLostPlatform, and change MP.runFromTests)

    private static void open(NbModuleProject p) {
        ((NbModuleProject.OpenedHook) p.getLookup().lookup(NbModuleProject.OpenedHook.class)).projectOpened();
    }

    private void open(SuiteProject p) {
        ((SuiteProject.OpenedHook) p.getLookup().lookup(SuiteProject.OpenedHook.class)).projectOpened();
    }
    
}
