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
import org.openide.util.test.MockChangeListener;
import org.openide.util.test.MockLookup;
import org.openide.util.test.MockPropertyChangeListener;

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
        MockLookup.setInstances(AntBasedTestUtil.testAntBasedProjectType());
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
        MockChangeListener l = new MockChangeListener();
        s.addChangeListener(l);
        // Check baseline GENERIC sources.
        SourceGroup[] groups = s.getSourceGroups(Sources.TYPE_GENERIC);
        assertEquals("should have maindir plus src2dir plus src3dir", 3, groups.length);
        assertEquals("group #1 is src2dir", src2dir, groups[0].getRootFolder());
        assertEquals("group #2 is src3dir", src3dir, groups[1].getRootFolder());
        assertEquals("group #3 is maindir", maindir, groups[2].getRootFolder());
        l.assertNoEvents();
        // Now change one of them to a different dir.
        EditableProperties p = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        p.setProperty("src2.dir", "../../src4");
        p.setProperty("src2a.dir", "nonsense");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, p);
        ProjectManager.getDefault().saveProject(project);
        l.msg("got change in GENERIC sources").assertEvent();
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
        l.assertNoEvents();
        p = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        p.setProperty("src1.dir", "does-not-exist");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, p);
        ProjectManager.getDefault().saveProject(project);
        l.msg("got change in java sources").assertEvent();
        groups = s.getSourceGroups("java");
        assertEquals("should have just src3dir", 1, groups.length);
        assertEquals("group #2 is src3dir", src3dir, groups[0].getRootFolder());
        l.assertNoEvents();
        // #47451: should not fire changes for unrelated properties.
        p = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        p.setProperty("irrelevant", "value");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, p);
        ProjectManager.getDefault().saveProject(project);
        l.msg("no changes fired from an unrelated property").assertNoEvents();
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
        MockChangeListener l = new MockChangeListener();
        s.addChangeListener(l);
        src3dir.delete();
        l.msg("got a change after src3dir deleted").assertEvent();
        groups = s.getSourceGroups("java");
        assertEquals("should have just src1dir", 1, groups.length);
        assertEquals("group #1 is src1dir", src1dir, groups[0].getRootFolder());
        src1dir.delete();
        l.msg("got a change after src1dir deleted").assertEvent();
        groups = s.getSourceGroups("java");
        assertEquals("should have no dirs", 0, groups.length);
        FileObject src5dir = scratch.createFolder("nonesuch");
        l.msg("got a change after src5dir created").assertEvent();
        groups = s.getSourceGroups("java");
        assertEquals("should have src15dir now", 1, groups.length);
        assertEquals("group #1 is src5dir", src5dir, groups[0].getRootFolder());
    }

    public void testIncludesExcludes() throws Exception {
        // <editor-fold desc="initial setup">
        scratch = TestUtil.makeScratchDir(this); // have our own setup
        maindir = scratch.createFolder("dir");
        projdir = maindir.createFolder("proj-dir");
        src1dir = projdir.createFolder("src1");
        src2dir = scratch.createFolder("src2");
        src3dir = projdir.createFolder("src3");
        src4dir = scratch.createFolder("src4");
        // </editor-fold>
        // <editor-fold desc="create files in group #1">
        FileUtil.createData(src1dir, "com/sun/tools/javac/Main.java");
        FileUtil.createData(src1dir, "com/sun/tools/internal/ws/processor/model/java/JavaArrayType.java");
        FileUtil.createData(src1dir, "sun/tools/javac/Main.java");
        FileUtil.createData(src1dir, "sunw/io/Serializable.java");
        FileUtil.createData(src1dir, "java/lang/Byte.java");
        FileUtil.createData(src1dir, "java/text/resources/Messages.properties");
        FileUtil.createData(src1dir, "java/text/resources/Messages_zh.properties");
        FileUtil.createData(src1dir, "java/text/resources/Messages_zh_TW.properties");
        FileUtil.createData(src1dir, "java/text/resources/x_y/z.properties");
        // </editor-fold>
        // <editor-fold desc="create files in group #2">
        FileUtil.createData(src2dir, "java/lang/Class.java");
        FileUtil.createData(src2dir, "javax/swing/JComponent.java");
        FileUtil.createData(src2dir, "javax/lang/Foo.java");
        FileUtil.createData(src2dir, "com/sun/java/swing/plaf/motif/MotifSplitPaneUI.java");
        FileUtil.createData(src2dir, "com/sun/org/apache/xerces/internal/parsers/AbstractDOMParser.java");
        FileUtil.createData(src2dir, "org/omg/CORBA/Any.java");
        FileUtil.createData(src2dir, "javax/swing/doc-files/groupLayout.1.gif");
        FileUtil.createData(src2dir, "javax/swing/plaf/resources/foo.gif");
        FileUtil.createData(src2dir, "javax/swing/resources/bar.gif");
        FileUtil.createData(src2dir, "docs/html/index.html");
        // </editor-fold>
        // <editor-fold desc="create files in group #3">
        FileUtil.createData(src3dir, "java/lang/Class.java");
        FileUtil.createData(src3dir, "java/util/Compat$Clazz.java");
        FileUtil.createData(src3dir, "javax/swing/JComponent.java");
        FileUtil.createData(src3dir, "com/sun/java/swing/plaf/motif/MotifSplitPaneUI.java");
        FileUtil.createData(src3dir, "README");
        FileUtil.createData(src3dir, "README.html");
        FileUtil.createData(src3dir, "whatever.xml");
        // </editor-fold>
        // <editor-fold desc="create files in group #4">
        FileUtil.createData(src4dir, "java/lang/Class.java");
        // </editor-fold>
        // <editor-fold desc="other setup #1">
        h = ProjectGenerator.createProject(projdir, "test");
        project = ProjectManager.getDefault().findProject(projdir);
        EditableProperties p = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        p.setProperty("src1.dir", "src1");
        p.setProperty("src2.dir", "../../src2");
        p.setProperty("src3.dir", "src3");
        p.setProperty("src4.dir", "../../src4");
        // </editor-fold>
        // <editor-fold desc="includes & excludes">
        p.setProperty("src1.excludes", " sun/,sunw\\, **\\internal/** ${undefined} ,  **/resources/*_*.properties ");
        p.setProperty("src2.includes", "**/swing/,com/sun/org/apache/,org/omg,docs/html/index.html");
        p.setProperty("src2.excludes", "**/doc-files/ **/swing/**/resources/");
        p.setProperty("src3.includes", "javax/swing/,com/sun/java/swing/,README,**/*$*.java,**/*.xml");
        // </editor-fold>
        // <editor-fold desc="other setup #2">
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, p);
        ProjectManager.getDefault().saveProject(project);
        sh = new SourcesHelper(h, h.getStandardPropertyEvaluator());
        sh.addPrincipalSourceRoot("${src1.dir}", "${src1.includes}", "${src1.excludes}", "Sources #1", null, null);
        sh.addPrincipalSourceRoot("${src2.dir}", "${src2.includes}", "${src2.excludes}", "Sources #2", null, null);
        sh.addPrincipalSourceRoot("${src3.dir}", "${src3.includes}", null, "Sources #3", null, null);
        sh.addPrincipalSourceRoot("${src4.dir}", "**", "", "Sources #4", null, null);
        sh.addTypedSourceRoot("${src1.dir}", "${src1.includes}", "${src1.excludes}", "java", "Packages #1", null, null);
        sh.addTypedSourceRoot("${src2.dir}", "${src2.includes}", "${src2.excludes}", "java", "Packages #2", null, null);
        sh.addTypedSourceRoot("${src3.dir}", "${src3.includes}", null, "java", "Packages #3", null, null);
        sh.addTypedSourceRoot("${src4.dir}", "**", "", "java", "Packages #4", null, null);
        Sources s = sh.createSources();
        SourceGroup[] groups = s.getSourceGroups("java");
        SourceGroup g1 = groups[0];
        assertEquals("Packages #1", g1.getDisplayName());
        SourceGroup g2 = groups[1];
        assertEquals("Packages #2", g2.getDisplayName());
        SourceGroup g3 = groups[2];
        assertEquals("Packages #3", g3.getDisplayName());
        SourceGroup g4 = groups[3];
        assertEquals("Packages #4", g4.getDisplayName());
        // </editor-fold>
        // <editor-fold desc="testing group #1">
        assertIncluded("not excluded despite sun/ infix", g1, "com/sun/tools/javac/Main.java");
        assertExcluded("internal infix", g1, "com/sun/tools/internal/ws/processor/model/java/JavaArrayType.java");
        assertExcluded("the whole folder is suppressed", g1, "com/sun/tools/internal");
        assertExcluded("sun/ prefix", g1, "sun/tools/javac/Main.java");
        assertExcluded("the whole folder is suppressed", g1, "sun");
        assertExcluded("sunw/ prefix even with \\", g1, "sunw/io/Serializable.java");
        assertExcluded("the whole folder is suppressed", g1, "sunw");
        assertIncluded("why not?", g1, "java/lang/Byte.java");
        assertIncluded("no _", g1, "java/text/resources/Messages.properties");
        assertExcluded("has _", g1, "java/text/resources/Messages_zh.properties");
        assertExcluded("has _ twice", g1, "java/text/resources/Messages_zh_TW.properties");
        assertIncluded("* does not match /", g1, "java/text/resources/x_y/z.properties");
        // </editor-fold>
        // <editor-fold desc="testing group #2">
        assertExcluded("not explicitly included", g2, "java/lang/Class.java");
        assertExcluded("nothing in java.lang.** is", g2, "java/lang");
        assertExcluded("nothing in java.** is", g2, "java");
        assertIncluded("explicitly included", g2, "javax/swing/JComponent.java");
        assertExcluded("but that does not apply to other children", g2, "javax/lang/Foo.java");
        assertIncluded("also explicitly included", g2, "com/sun/java/swing/plaf/motif/MotifSplitPaneUI.java");
        assertIncluded("not excluded as internal", g2, "com/sun/org/apache/xerces/internal/parsers/AbstractDOMParser.java");
        assertExcluded("dir includes do not work without /", g2, "org/omg/CORBA/Any.java");
        assertExcluded("dir includes do not work without / even for folder itself", g2, "org/omg");
        assertExcluded("nothing in org included, in fact", g2, "org");
        assertExcluded("doc-files excluded", g2, "javax/swing/doc-files/groupLayout.1.gif");
        assertExcluded("whole doc-files excluded", g2, "javax/swing/doc-files");
        assertExcluded("resources excluded with intermediate plaf", g2, "javax/swing/plaf/resources/foo.gif");
        assertExcluded("whole resources excluded with intermediate plaf", g2, "javax/swing/plaf/resources");
        assertExcluded("/**/ can match /", g2, "javax/swing/resources/bar.gif");
        assertExcluded("/**/ can match / on whole dir", g2, "javax/swing/resources");
        // </editor-fold>
        // <editor-fold desc="testing group #3">
        assertExcluded("no reason to include", g3, "java/lang/Class.java");
        assertExcluded("java.lang not there", g3, "java/lang");
        assertIncluded("has a $", g3, "java/util/Compat$Clazz.java");
        assertIncluded("explicitly included", g3, "javax/swing/JComponent.java");
        assertIncluded("explicitly included", g3, "com/sun/java/swing/plaf/motif/MotifSplitPaneUI.java");
        assertIncluded("explicitly included", g3, "README");
        assertExcluded("did not include file w/ ext", g3, "README.html");
        assertIncluded("**/ can match null prefix", g3, "whatever.xml");
        // </editor-fold>
        // <editor-fold desc="testing group #4">
        assertIncluded("everything included", g4, "java/lang/Class.java");
        // </editor-fold>
        // <editor-fold desc="testing external roots">
        sh.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        FileObject projdir2 = maindir.createFolder("proj-dir2");
        ProjectGenerator.createProject(projdir2, "test");
        Project prj2 = ProjectManager.getDefault().findProject(projdir2);
        FileObject docfiles = src2dir.getFileObject("javax/swing/doc-files");
        assertNotNull(docfiles);
        FileOwnerQuery.markExternalOwner(docfiles, prj2, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        assertOwner(null, g2, "java/lang/Class.java");
        assertOwner(project, g2, "javax/swing/JComponent.java");
        assertOwner(project, g2, "javax/swing");
        assertOwner(null, g2, "javax");
        assertOwner(project, g2, "com/sun/java/swing/plaf/motif/MotifSplitPaneUI.java");
        assertOwner(project, g2, "com/sun/java/swing/plaf/motif");
        assertOwner(project, g2, "com/sun/java/swing/plaf");
        assertOwner(project, g2, "com/sun/java/swing");
        assertOwner(null, g2, "com/sun/java");
        assertOwner(null, g2, "com/sun");
        assertOwner(null, g2, "com");
        assertOwner(null, g2, "org/omg/CORBA/Any.java");
        assertOwner(prj2, g2, "javax/swing/doc-files/groupLayout.1.gif");
        assertOwner(prj2, g2, "javax/swing/doc-files");
        assertOwner(project, g2, "com/sun/java/swing");
        assertOwner(project, g2, "docs/html/index.html");
        assertOwner(project, g2, "docs/html");
        assertOwner(null, g2, "docs");
        p = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        p.setProperty("src2.includes", "com/sun/org/apache/,org/omg,docs/html/index.html");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, p);
        assertOwner(null, g2, "javax/swing/JComponent.java");
        p = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        p.setProperty("src2.includes", "**/swing/,com/sun/org/apache/,org/omg,docs/html/index.html");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, p);
        assertOwner(project, g2, "javax/swing/JComponent.java");
        assertOwner(project, g4, "java/lang/Class.java");
        assertOwner(project, g4, "java/lang");
        assertOwner(project, g4, "java");
        assertOwner(project, g4, "");
        // </editor-fold>
        // <editor-fold desc="testing change firing">
        MockPropertyChangeListener l = new MockPropertyChangeListener();
        g2.addPropertyChangeListener(l);
        p = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        p.setProperty("src2.excludes", "**/doc-files/");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, p);
        l.assertEvents(SourceGroup.PROP_CONTAINERSHIP);
        assertExcluded("doc-files still excluded", g2, "javax/swing/doc-files/groupLayout.1.gif");
        assertIncluded("resources now included", g2, "javax/swing/plaf/resources/foo.gif");
        p = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        p.setProperty("src2.includes", "**");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, p);
        l.assertEvents(SourceGroup.PROP_CONTAINERSHIP);
        assertIncluded("may as well be included", g2, "java/lang/Class.java");
        p = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        p.setProperty("src2.includes", "**/swing/");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, p);
        l.assertEvents(SourceGroup.PROP_CONTAINERSHIP);
        assertExcluded("excluded again", g2, "java/lang/Class.java");
        p = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        p.setProperty("irrelevant", "value");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, p);
        l.assertEvents();
        // </editor-fold>
        // <editor-fold desc="testing misc">
        try {
            FileObject f = src2dir.getFileObject("java/lang/Class.java");
            assertNotNull(f);
            g1.contains(f);
            fail("wrong root");
        } catch (IllegalArgumentException e) {/* good */}
        SourceGroup[] ggroups = s.getSourceGroups(Sources.TYPE_GENERIC);
        SourceGroup gg1 = ggroups[0];
        assertEquals("Sources #2", gg1.getDisplayName());
        assertIncluded("explicitly included", gg1, "javax/swing/JComponent.java");
        assertExcluded("but that does not apply to other children", gg1, "javax/lang/Foo.java");
        // </editor-fold>
    }
    private static void assertIncluded(String message, SourceGroup g, String resource) {
        FileObject f = g.getRootFolder().getFileObject(resource);
        assertNotNull(resource, f);
        assertTrue(message, g.contains(f));
        int slash = resource.lastIndexOf('/');
        if (slash != -1) {
            String parent = resource.substring(0, slash);
            assertIncluded("parent " + parent + " of " + resource + " must also be contained by definition", g, parent);
        } else if (resource.length() > 0) {
            assertIncluded("root folder always contained by definition", g, "");
        }
    }
    private static void assertExcluded(String message, SourceGroup g, String resource) {
        FileObject f = g.getRootFolder().getFileObject(resource);
        assertNotNull(resource, f);
        assertFalse(message, g.contains(f));
    }
    private static void assertOwner(Project owner, SourceGroup g, String resource) {
        FileObject f = g.getRootFolder().getFileObject(resource);
        assertNotNull(resource, f);
        assertEquals(owner, FileOwnerQuery.getOwner(f));
    }
    
}
