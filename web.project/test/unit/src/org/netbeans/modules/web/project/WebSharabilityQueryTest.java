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

package org.netbeans.modules.web.project;

import java.io.File;
import org.netbeans.api.project.ProjectUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.web.project.test.TestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;

public class WebSharabilityQueryTest extends NbTestCase {

    public WebSharabilityQueryTest(String testName) {
        super(testName);
    }

    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private FileObject tests;
    private FileObject docRoot;
    private FileObject dist;
    private FileObject build;
    private WebProject pp;
    private AntProjectHelper helper;

    protected void setUp() throws Exception {
        super.setUp();
        
        TestUtil.setLookup(new Object[] {
            new WebProjectType(),
            new org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation()
        });
        
        File f = new File(getDataDir().getAbsolutePath(), "projects/WebApplication1");
        projdir = FileUtil.toFileObject(f);

        scratch = TestUtil.makeScratchDir(this);
        sources = projdir.getFileObject("src/java");
        tests = projdir.getFileObject("test");
        docRoot = projdir.getFileObject("web");
        dist = FileUtil.createFolder(projdir,"dist");
        build = FileUtil.createFolder(projdir,"build");
        Project p = ProjectManager.getDefault().findProject(projdir);
        assertTrue("Invalid project type",p instanceof WebProject);
        pp = (WebProject) p;
        helper = pp.getAntProjectHelper();
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        sources = null;
        tests = null;
        docRoot = null;
        pp = null;
        helper = null;
        TestUtil.setLookup(Lookup.EMPTY);
        super.tearDown();
    }

    public void testSharability () throws Exception {
        File f = FileUtil.toFile (this.sources);
        int res = SharabilityQuery.getSharability(f);
        assertEquals("Sources must be sharable", SharabilityQuery.SHARABLE, res);
        f = FileUtil.toFile (this.tests);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Tests must be sharable", SharabilityQuery.SHARABLE, res);
        f = FileUtil.toFile (this.docRoot);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Web Pages must be sharable", SharabilityQuery.SHARABLE, res);
        f = FileUtil.toFile (this.build);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Build can't be sharable", SharabilityQuery.NOT_SHARABLE, res);
        f = FileUtil.toFile (this.dist);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Dist can't be sharable", SharabilityQuery.NOT_SHARABLE, res);
        FileObject newSourceRoot = addSourceRoot(helper, projdir, "src2.dir",new File(FileUtil.toFile(scratch),"sources2"));
        ProjectUtils.getSources(pp).getSourceGroups(Sources.TYPE_GENERIC);
        f = FileUtil.toFile (newSourceRoot);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Sources2 must be sharable", SharabilityQuery.SHARABLE, res);
        FileObject newSourceRoot2 = changeSourceRoot(helper, projdir, "src2.dir", new File(FileUtil.toFile(scratch),"sources3"));
        f = FileUtil.toFile (newSourceRoot2);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Sources3 must be sharable", SharabilityQuery.SHARABLE, res);
        f = FileUtil.toFile (newSourceRoot);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Sources2 must be unknown", SharabilityQuery.UNKNOWN, res);
    }

    public static FileObject addSourceRoot (AntProjectHelper helper, FileObject projdir,
                                            String propName, File folder) throws Exception {
        if (!folder.exists()) {
            folder.mkdirs();
        }
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList nl = data.getElementsByTagNameNS (WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");
        assert nl.getLength() == 1;
        Element roots = (Element) nl.item(0);
        Document doc = roots.getOwnerDocument();
        Element root = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");
        root.setAttributeNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"id",propName);
        roots.appendChild (root);
        helper.putPrimaryConfigurationData (data,true);
        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.put (propName,folder.getAbsolutePath());
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,props);
        return FileUtil.toFileObject(folder);
    }

    public static FileObject changeSourceRoot (AntProjectHelper helper, FileObject projdir,
                                               String propName, File folder) throws Exception {
        if (!folder.exists()) {
            folder.mkdirs();
        }
        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assert props.containsKey(propName);
        props.put (propName,folder.getAbsolutePath());
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,props);
        return FileUtil.toFileObject(folder);
    }

}
