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

package org.netbeans.modules.java.j2seproject.applet;

import java.net.URL;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Collection;
import java.util.List;
import java.util.Collections;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.modules.SpecificationVersion;
import org.netbeans.junit.NbTestCase;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.java.j2seproject.J2SEProjectUtil;
import org.netbeans.modules.java.platform.JavaPlatformProvider;

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
        JavaPlatform platform1 = new TestPlatform("TP1","tp1",new Specification("j2se", new SpecificationVersion("1.4")));
        JavaPlatform platform2 = new TestPlatform("TP2","tp2",new Specification("j2se", new SpecificationVersion("1.5")));
        JavaPlatform platform3 = new TestPlatform("TP2","tp3",new Specification("j2se", new SpecificationVersion("1.5.1")));
        TestUtil.setLookup(new Object[] {
            new PlatformProviderImpl(new JavaPlatform[] {
                platform1,
                platform2,
                platform3
            }),
        });
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
        super.tearDown();
    }

    public void testgenerateHtmlFileURL () throws Exception {
        //Tests the JDK issue #6193279
        URL url = AppletSupport.generateHtmlFileURL(source,buildFolder,classesFolder,"tp1");
        String[] parts = url.toExternalForm().split("/");
        assertTrue (parts.length>4);
        assertEquals (parts[parts.length-1],"Applet.html");
        assertEquals (parts[parts.length-2],"build");
        assertEquals (parts[parts.length-3],"proj");
        assertEquals (parts[parts.length-4],"Folder%20With%20Spaces");
        url = AppletSupport.generateHtmlFileURL(source,buildFolder,classesFolder,"tp2");
        parts = url.toExternalForm().split("/");
        assertTrue (parts.length>4);
        assertEquals (parts[parts.length-1],"Applet.html");
        assertEquals (parts[parts.length-2],"build");
        assertEquals (parts[parts.length-3],"proj");
        assertEquals (parts[parts.length-4],"Folder With Spaces");
        url = AppletSupport.generateHtmlFileURL(source,buildFolder,classesFolder,null);
        parts = url.toExternalForm().split("/");
        assertTrue (parts.length>4);
        assertEquals (parts[parts.length-1],"Applet.html");
        assertEquals (parts[parts.length-2],"build");
        assertEquals (parts[parts.length-3],"proj");
        assertEquals (parts[parts.length-4],"Folder%20With%20Spaces");
        url = AppletSupport.generateHtmlFileURL(source,buildFolder,classesFolder,"tp3");
        parts = url.toExternalForm().split("/");
        assertTrue (parts.length>4);
        assertEquals (parts[parts.length-1],"Applet.html");
        assertEquals (parts[parts.length-2],"build");
        assertEquals (parts[parts.length-3],"proj");
        assertEquals (parts[parts.length-4],"Folder With Spaces");
    }

    private static class PlatformProviderImpl implements JavaPlatformProvider {
        private JavaPlatform[] platforms;

        public PlatformProviderImpl (JavaPlatform[] platforms) {
            this.platforms = platforms;
        }

        public JavaPlatform[] getInstalledPlatforms() {
            return platforms;
        }

        public JavaPlatform getDefaultPlatform() {
            return this.platforms[0];
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }

    private static class TestPlatform extends JavaPlatform {

        private String displayName;
        private Map props;
        private Specification spec;

        public TestPlatform (String displayName, String antName, Specification spec) {
            this.displayName = displayName;
            this.props = Collections.singletonMap("platform.ant.name",antName);
            this.spec = spec;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public Specification getSpecification() {
            return spec;
        }

        public Map getProperties() {
            return this.props;
        }

        public ClassPath getBootstrapLibraries() {
            return null;
        }

        public ClassPath getStandardLibraries() {
            return null;
        }

        public String getVendor() {
            return null;
        }

        public Collection getInstallFolders() {
            return null;
        }

        public FileObject findTool(String toolName) {
            return null;
        }

        public ClassPath getSourceFolders() {
            return null;
        }

        public List getJavadocFolders() {
            return null;
        }
    }
}
