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

package org.netbeans.modules.java.j2seproject;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.TestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

/**
 * Tests J2SESources
 * Tests if SourceForBinaryQuery works fine on external build folder.
 *
 * @author Tomas Zezula
 */
public class J2SESourcesTest extends NbTestCase {

    public J2SESourcesTest(String testName) {
        super(testName);
    }

    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private FileObject build;
    private FileObject classes;
    private ProjectManager pm;
    private J2SEProject pp;
    private AntProjectHelper helper;

    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setLookup(new Object[] {
            new org.netbeans.modules.java.j2seproject.J2SEProjectType(),
            new org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation()
        });
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");        
        helper = J2SEProjectGenerator.createProject(FileUtil.toFile(projdir),"proj",null,null); //NOI18N        
        sources = this.getFileObject(projdir, "src");
        build = this.getFileObject (scratch, "build");
        classes = this.getFileObject(build,"classes");
        File f = FileUtil.normalizeFile (FileUtil.toFile(build));
        String path = f.getAbsolutePath ();
//#47657: SourcesHelper.remarkExternalRoots () does not work on deleted folders
// To reproduce it uncomment following line
//        build.delete();
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty(J2SEProjectProperties.BUILD_DIR, path);
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        pm = ProjectManager.getDefault();
        Project p = pm.findProject(projdir);
        assertTrue("Invalid project type",p instanceof J2SEProject);
        pp = (J2SEProject) p;
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        sources = null;
        build = null;
        classes = null;
        pm = null;
        pp = null;
        helper = null;
        TestUtil.setLookup(Lookup.EMPTY);
        super.tearDown();
    }

    public void testSourceRoots () throws Exception {        
        FileObject[] roots = SourceForBinaryQuery.findSourceRoots(classes.getURL()).getRoots();
        assertEquals("There should be 1 src root",1,roots.length);
        assertEquals("Invalid src root", sources, roots[0]);               
    }
    
    private static FileObject getFileObject (FileObject parent, String name) throws IOException {
        FileObject result = parent.getFileObject(name);
        if (result == null) {
            result = parent.createFolder(name);
        }
        return result;
    }   
    

    private static void addSourceRoot (AntProjectHelper helper, FileObject sourceFolder, String propName) throws Exception {
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList nl = data.getElementsByTagNameNS (J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");
        assert nl.getLength() == 1;
        Element roots = (Element) nl.item(0);
        Document doc = roots.getOwnerDocument();
        Element root = doc.createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");
        root.setAttributeNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"id",propName);
        roots.appendChild (root);
        helper.putPrimaryConfigurationData (data,true);
        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
        File f = FileUtil.normalizeFile(FileUtil.toFile(sourceFolder));
        props.put (propName, f.getAbsolutePath());
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,props);
    }


}
