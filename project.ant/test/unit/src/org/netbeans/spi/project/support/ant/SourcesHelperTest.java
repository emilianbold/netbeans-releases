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

package org.netbeans.spi.project.support.ant;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test functionality of SourcesHelper.
 * @author Jesse Glick
 */
public final class SourcesHelperTest extends NbTestCase {
    
    public SourcesHelperTest(String name) {
        super(name);
    }
    
    private FileObject scratch;
    private FileObject maindir;
    private FileObject projdir;
    private FileObject src1dir;
    private FileObject src2dir;
    private FileObject src3dir;
    private FileObject src4dir;
    private FileObject builddir;
    private AntProjectHelper h;
    private Project project;
    private SourcesHelper sh;
    private FileObject proj2dir;
    private FileObject proj2src1dir;
    private FileObject proj2src2dir;
    private AntProjectHelper h2;
    private Project project2;
    private SourcesHelper sh2;
    
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setLookup(new Object[] {
            AntBasedTestUtil.testAntBasedProjectType(),
        });
        scratch = TestUtil.makeScratchDir(this);
        scratch.createData("otherfile");
        maindir = scratch.createFolder("dir");
        maindir.createData("readme");
        projdir = maindir.createFolder("projdir");
        projdir.createData("projfile");
        src1dir = projdir.createFolder("src1");
        src1dir.createData("src1file");
        src2dir = scratch.createFolder("src2");
        src2dir.createData("src2file");
        src3dir = scratch.createFolder("src3");
        src3dir.createData("src3file");
        src4dir = scratch.createFolder("src4");
        src4dir.createData("src4file");
        builddir = scratch.createFolder("build");
        builddir.createData("buildfile");
        h = ProjectGenerator.createProject(projdir, "test");
        project = ProjectManager.getDefault().findProject(projdir);
        assertNotNull("have a project", project);
        EditableProperties p = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        p.setProperty("src1.dir", "src1");
        p.setProperty("src2.dir", "../../src2");
        p.setProperty("src2a.dir", "../../src2"); // same path as src2.dir
        p.setProperty("src3.dir", FileUtil.toFile(src3dir).getAbsolutePath());
        p.setProperty("src4.dir", "..");
        p.setProperty("src5.dir", "../../nonesuch");
        p.setProperty("build.dir", "../../build");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, p);
        ProjectManager.getDefault().saveProject(project);
        sh = new SourcesHelper(h, h.getStandardPropertyEvaluator());
        sh.addPrincipalSourceRoot("${src1.dir}", "Sources #1", null, null); // inside proj dir
        sh.addPrincipalSourceRoot("${src2.dir}", "Sources #2", null, null); // outside (rel path)
        sh.addPrincipalSourceRoot("${src2a.dir}", "Sources #2a", null, null); // redundant
        sh.addPrincipalSourceRoot("${src3.dir}", "Sources #3", null, null); // outside (abs path)
        sh.addPrincipalSourceRoot("${src4.dir}", "The Whole Shebang", null, null); // above proj dir
        sh.addPrincipalSourceRoot("${src5.dir}", "None such", null, null); // does not exist on disk
        sh.addNonSourceRoot("${build.dir}");
        sh.addTypedSourceRoot("${src1.dir}", "java", "Packages #1", null, null);
        sh.addTypedSourceRoot("${src3.dir}", "java", "Packages #3", null, null);
        sh.addTypedSourceRoot("${src5.dir}", "java", "No Packages", null, null);
        sh.addTypedSourceRoot("${src2.dir}", "docroot", "Documents #2", null, null);
        sh.addTypedSourceRoot("${src2a.dir}", "docroot", "Documents #2a", null, null); // redundant
        // Separate project that has includes its project directory implicitly only.
        // Also hardcodes paths rather than using properties.
        proj2dir = scratch.createFolder("proj2dir");
        proj2dir.createData("proj2file");
        proj2src1dir = proj2dir.createFolder("src1");
        proj2src1dir.createData("proj2src1file");
        proj2src2dir = proj2dir.createFolder("src2");
        proj2src2dir.createData("proj2src2file");
        h2 = ProjectGenerator.createProject(proj2dir, "test");
        project2 = ProjectManager.getDefault().findProject(proj2dir);
        assertNotNull("have a project2", project2);
        sh2 = new SourcesHelper(h2, h2.getStandardPropertyEvaluator());
        sh2.addPrincipalSourceRoot("src1", "Sources #1", null, null);
        sh2.addPrincipalSourceRoot("src2", "Sources #2", null, null);
        sh2.addNonSourceRoot("build");
        sh2.addTypedSourceRoot("src1", "java", "Packages #1", null, null);
        sh2.addTypedSourceRoot("src2", "java", "Packages #2", null, null);
    }
    
    public void testSourcesBasic() throws Exception {
        Sources s = sh.createSources();
        // XXX test that ISE is thrown if we try to add more dirs now
        SourceGroup[] groups = s.getSourceGroups(Sources.TYPE_GENERIC);
        assertEquals("should have maindir plus src2dir plus src3dir", 3, groups.length);
        assertEquals("group #1 is src2dir", src2dir, groups[0].getRootFolder());
        assertEquals("right display name for src2dir", "Sources #2", groups[0].getDisplayName());
        assertEquals("group #2 is src3dir", src3dir, groups[1].getRootFolder());
        assertEquals("right display name for src3dir", "Sources #3", groups[1].getDisplayName());
        assertEquals("group #3 is maindir", maindir, groups[2].getRootFolder());
        assertEquals("right display name for maindir", "The Whole Shebang", groups[2].getDisplayName());
        // Now the typed source roots.
        groups = s.getSourceGroups("java");
        assertEquals("should have src1dir plus src3dir", 2, groups.length);
        assertEquals("group #1 is src1dir", src1dir, groups[0].getRootFolder());
        assertEquals("right display name for src1dir", "Packages #1", groups[0].getDisplayName());
        assertEquals("group #2 is src3dir", src3dir, groups[1].getRootFolder());
        assertEquals("right display name for src3dir", "Packages #3", groups[1].getDisplayName());
        groups = s.getSourceGroups("docroot");
        assertEquals("should have just src2dir", 1, groups.length);
        assertEquals("group #1 is src2dir", src2dir, groups[0].getRootFolder());
        assertEquals("right display name for src2dir", "Documents #2", groups[0].getDisplayName());
        groups = s.getSourceGroups("unknown");
        assertEquals("should not have any unknown dirs", 0, groups.length);
        // Test the simpler project type.
        s = sh2.createSources();
        groups = s.getSourceGroups(Sources.TYPE_GENERIC);
        assertEquals("should have just proj2dir", 1, groups.length);
        assertEquals("group #1 is proj2dir", proj2dir, groups[0].getRootFolder());
        assertEquals("right display name for proj2dir", ProjectUtils.getInformation(project2).getDisplayName(), groups[0].getDisplayName());
        groups = s.getSourceGroups("java");
        assertEquals("should have proj2src1dir plus proj2src2dir", 2, groups.length);
        assertEquals("group #1 is proj2src1dir group", proj2src1dir, groups[0].getRootFolder());
        assertEquals("right display name for src1dir", "Packages #1", groups[0].getDisplayName());
        assertEquals("group #2 is proj2src2dir group", proj2src2dir, groups[1].getRootFolder());
        assertEquals("right display name for proj2src2dir", "Packages #2", groups[1].getDisplayName());
        // XXX test also icons
    }
    
    public void testExternalRootRegistration() throws Exception {
        FileObject f = maindir.getFileObject("readme");
        assertEquals("readme not yet registered", null, FileOwnerQuery.getOwner(f));
        f = projdir.getFileObject("projfile");
        assertEquals("projfile initially OK", project, FileOwnerQuery.getOwner(f));
        sh.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        f = maindir.getFileObject("readme");
        assertEquals("readme now registered", project, FileOwnerQuery.getOwner(f));
        f = projdir.getFileObject("projfile");
        assertEquals("projfile still OK", project, FileOwnerQuery.getOwner(f));
        f = src1dir.getFileObject("src1file");
        assertEquals("src1file registered", project, FileOwnerQuery.getOwner(f));
        f = src2dir.getFileObject("src2file");
        assertEquals("src2file registered", project, FileOwnerQuery.getOwner(f));
        f = src3dir.getFileObject("src3file");
        assertEquals("src3file registered", project, FileOwnerQuery.getOwner(f));
        f = builddir.getFileObject("buildfile");
        assertEquals("buildfile registered", project, FileOwnerQuery.getOwner(f));
        f = scratch.getFileObject("otherfile");
        assertEquals("otherfile not registered", null, FileOwnerQuery.getOwner(f));
        // Test the simpler project type.
        sh2.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        f = proj2dir.getFileObject("proj2file");
        assertEquals("proj2file of course OK", project2, FileOwnerQuery.getOwner(f));
        f = proj2src1dir.getFileObject("proj2src1file");
        assertEquals("proj2src1file registered", project2, FileOwnerQuery.getOwner(f));
        f = proj2src2dir.getFileObject("proj2src2file");
        assertEquals("proj2src2file registered", project2, FileOwnerQuery.getOwner(f));
    }
    
    public void testSourceLocationChanges() throws Exception {
        Sources s = sh.createSources();
        SourceGroup[] groups = s.getSourceGroups(Sources.TYPE_GENERIC);
        assertEquals("should have maindir plus src2dir plus src3dir", 3, groups.length);
        assertEquals("group #1 is src2dir", src2dir, groups[0].getRootFolder());
        assertEquals("group #2 is src3dir", src3dir, groups[1].getRootFolder());
        assertEquals("group #3 is maindir", maindir, groups[2].getRootFolder());
        groups = s.getSourceGroups("java");
        assertEquals("should have src1dir plus src3dir", 2, groups.length);
        assertEquals("group #1 is src1dir", src1dir, groups[0].getRootFolder());
        assertEquals("right display name for src1dir", "Packages #1", groups[0].getDisplayName());
        assertEquals("group #2 is src3dir", src3dir, groups[1].getRootFolder());
        // Now change one of them.
        EditableProperties p = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        p.setProperty("src1.dir", "../../src4");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, p);
        ProjectManager.getDefault().saveProject(project);
        groups = s.getSourceGroups(Sources.TYPE_GENERIC);
        assertEquals("should have maindir plus src4dir plus src2dir plus src3dir", 4, groups.length);
        assertEquals("group #1 is src4dir", src4dir, groups[0].getRootFolder());
        assertEquals("group #2 is src2dir", src2dir, groups[1].getRootFolder());
        assertEquals("group #3 is src3dir", src3dir, groups[2].getRootFolder());
        assertEquals("group #4 is maindir", maindir, groups[3].getRootFolder());
        groups = s.getSourceGroups("java");
        assertEquals("should have src4dir plus src3dir", 2, groups.length);
        assertEquals("group #1 is src4dir", src4dir, groups[0].getRootFolder());
        assertEquals("right display name for src4dir", "Packages #1", groups[0].getDisplayName());
        assertEquals("group #2 is src3dir", src3dir, groups[1].getRootFolder());
    }
    
    public void testSourceLocationChangesFired() throws Exception {
        Sources s = sh.createSources();
        // Listen to changes.
        AntBasedTestUtil.TestCL l = new AntBasedTestUtil.TestCL();
        s.addChangeListener(l);
        // Check baseline GENERIC sources.
        SourceGroup[] groups = s.getSourceGroups(Sources.TYPE_GENERIC);
        assertEquals("should have maindir plus src2dir plus src3dir", 3, groups.length);
        assertEquals("group #1 is src2dir", src2dir, groups[0].getRootFolder());
        assertEquals("group #2 is src3dir", src3dir, groups[1].getRootFolder());
        assertEquals("group #3 is maindir", maindir, groups[2].getRootFolder());
        assertFalse("no initial changes", l.expect());
        // Now change one of them to a different dir.
        EditableProperties p = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        p.setProperty("src2.dir", "../../src4");
        p.setProperty("src2a.dir", "nonsense");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, p);
        ProjectManager.getDefault().saveProject(project);
        assertTrue("got change in GENERIC sources", l.expect());
        // Check new values.
        groups = s.getSourceGroups(Sources.TYPE_GENERIC);
        assertEquals("should have maindir plus src4dir plus src3dir", 3, groups.length);
        assertEquals("group #1 is src4dir", src4dir, groups[0].getRootFolder());
        assertEquals("group #2 is src3dir", src3dir, groups[1].getRootFolder());
        assertEquals("group #3 is maindir", maindir, groups[2].getRootFolder());
        // Check 'java' type groups also.
        groups = s.getSourceGroups("java");
        assertEquals("should have src1dir plus src3dir", 2, groups.length);
        assertEquals("group #1 is src1dir", src1dir, groups[0].getRootFolder());
        assertEquals("group #2 is src3dir", src3dir, groups[1].getRootFolder());
        assertFalse("no additional changes yet", l.expect());
        p = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        p.setProperty("src1.dir", "does-not-exist");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, p);
        ProjectManager.getDefault().saveProject(project);
        assertTrue("got change in java sources", l.expect());
        groups = s.getSourceGroups("java");
        assertEquals("should have just src3dir", 1, groups.length);
        assertEquals("group #2 is src3dir", src3dir, groups[0].getRootFolder());
        assertFalse("no further changes", l.expect());
        // #47451: should not fire changes for unrelated properties.
        p = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        p.setProperty("irrelevant", "value");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, p);
        ProjectManager.getDefault().saveProject(project);
        assertFalse("no changes fired from an unrelated property", l.expect());
    }
    
    public void testExternalRootLocationChanges() throws Exception {
        FileObject readme = maindir.getFileObject("readme");
        assertEquals("readme not yet registered", null, FileOwnerQuery.getOwner(readme));
        sh.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        assertEquals("readme still registered", project, FileOwnerQuery.getOwner(readme));
        FileObject src4file = src4dir.getFileObject("src4file");
        assertEquals("src4file not yet owned by anyone", null, FileOwnerQuery.getOwner(src4file));
        FileObject src2file = src2dir.getFileObject("src2file");
        assertEquals("src2file owned by the project", project, FileOwnerQuery.getOwner(src2file));
        // Change things around.
        EditableProperties p = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        p.setProperty("src1.dir", "../../src4"); // start to recognize this root
        p.setProperty("src2.dir", "src2"); // moved from ../../src2
        p.remove("src2a.dir"); // was also ../../src2
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, p);
        ProjectManager.getDefault().saveProject(project);
        assertEquals("src4file now owned by the project", project, FileOwnerQuery.getOwner(src4file));
        assertEquals("src2file no longer owned by the project", null, FileOwnerQuery.getOwner(src2file));
        assertEquals("readme still registered after unrelated changes", project, FileOwnerQuery.getOwner(readme));
        FileObject otherfile = scratch.getFileObject("otherfile");
        assertEquals("otherfile still not registered", null, FileOwnerQuery.getOwner(otherfile));
    }
    
    public void testSourceRootDeletion() throws Exception {
        // Cf. #40845. Need to fire a change if a root is deleted while project is open.
        Sources s = sh.createSources();
        SourceGroup[] groups = s.getSourceGroups("java");
        assertEquals("should have src1dir plus src3dir", 2, groups.length);
        assertEquals("group #1 is src1dir", src1dir, groups[0].getRootFolder());
        assertEquals("group #2 is src3dir", src3dir, groups[1].getRootFolder());
        AntBasedTestUtil.TestCL l = new AntBasedTestUtil.TestCL();
        s.addChangeListener(l);
        src3dir.delete();
        assertTrue("got a change after src3dir deleted", l.expect());
        groups = s.getSourceGroups("java");
        assertEquals("should have just src1dir", 1, groups.length);
        assertEquals("group #1 is src1dir", src1dir, groups[0].getRootFolder());
        src1dir.delete();
        assertTrue("got a change after src1dir deleted", l.expect());
        groups = s.getSourceGroups("java");
        assertEquals("should have no dirs", 0, groups.length);
        FileObject src5dir = scratch.createFolder("nonesuch");
        assertTrue("got a change after src5dir created", l.expect());
        groups = s.getSourceGroups("java");
        assertEquals("should have src15dir now", 1, groups.length);
        assertEquals("group #1 is src5dir", src5dir, groups[0].getRootFolder());
    }
    
}
