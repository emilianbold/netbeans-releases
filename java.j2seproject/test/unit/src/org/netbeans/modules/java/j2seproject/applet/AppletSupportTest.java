/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject.applet;

import java.net.URL;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.java.j2seproject.J2SEProjectUtil;







public class AppletSupportTest extends NbTestCase {

    private FileObject scratch;
    private FileObject projdir;
    private AntProjectHelper helper;
    private FileObject source;
    private FileObject buildFolder;
    private FileObject classesFolder;

    public AppletSupportTest (String name) {
        super (name);
    }

     protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setLookup(new Object[] {
            new org.netbeans.modules.java.j2seproject.J2SEProjectType(),
            new org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation()
        }, AppletSupportTest.class.getClassLoader());
        scratch = TestUtil.makeScratchDir(this);
        FileObject folderWithSpaces = scratch.createFolder("Folder With Spaces");
        projdir = folderWithSpaces.createFolder("proj");
        helper = J2SEProjectGenerator.createProject(FileUtil.toFile(projdir),"proj",null,null); //NOI18N
        Project p = ProjectManager.getDefault().findProject(projdir);

        FileObject src = projdir.getFileObject("src");
        FileObject pkg = src.createFolder("pkg");
        source = pkg.createData("Applet","java");
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String buildFolderName = (String) J2SEProjectUtil.getEvaluatedProperty (p, ep.getProperty("build.dir"));
        buildFolder = FileUtil.createFolder(projdir,buildFolderName);
        String classesFolderName = (String) J2SEProjectUtil.getEvaluatedProperty(p, ep.getProperty("build.classes.dir"));
        classesFolder = FileUtil.createFolder(projdir,classesFolderName);
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        helper = null;
        TestUtil.setLookup(Lookup.EMPTY);
        super.tearDown();
    }


    public void testgenerateHtmlFileURL () throws Exception {
        //Tests the JDK issue #6193279
        AppletSupport.workAround6193279 = false;
        URL url = AppletSupport.generateHtmlFileURL(source,buildFolder,classesFolder);
        String[] parts = url.toExternalForm().split("/");
        assertTrue (parts.length>4);
        assertEquals (parts[parts.length-1],"Applet.html");
        assertEquals (parts[parts.length-2],"build");
        assertEquals (parts[parts.length-3],"proj");
        assertEquals (parts[parts.length-4],"Folder%20With%20Spaces");
        AppletSupport.workAround6193279 = true;
        url = AppletSupport.generateHtmlFileURL(source,buildFolder,classesFolder);
        parts = url.toExternalForm().split("/");
        assertTrue (parts.length>4);
        assertEquals (parts[parts.length-1],"Applet.html");
        assertEquals (parts[parts.length-2],"build");
        assertEquals (parts[parts.length-3],"proj");
        assertEquals (parts[parts.length-4],"Folder With Spaces");
    }
}
