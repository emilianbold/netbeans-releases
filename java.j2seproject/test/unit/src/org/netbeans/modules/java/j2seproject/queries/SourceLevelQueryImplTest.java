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

package org.netbeans.modules.java.j2seproject.queries;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.platform.JavaPlatformProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.TestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;

/**
 * Tests for SourceLevelQueryImpl
 *
 * @author David Konecny
 */
public class SourceLevelQueryImplTest extends NbTestCase {
    
    public SourceLevelQueryImplTest(String testName) {
        super(testName);
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private FileObject tests;
    private ProjectManager pm;
    private Project pp;
    
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setLookup(new Object[] {
            new org.netbeans.modules.java.j2seproject.J2SEProjectType(),
            new org.netbeans.modules.java.project.ProjectSourceLevelQueryImpl(),
            new org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation(),
            new TestPlatformProvider ()
        });
        Properties p = System.getProperties();
        if (p.getProperty ("netbeans.user") == null) {
            p.put("netbeans.user", FileUtil.toFile(TestUtil.makeScratchDir(this)).getAbsolutePath());
        }
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        pm = null;
        super.tearDown();
    }
    
    
    private void prepareProject (String platformName) throws IOException {
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        AntProjectHelper helper = ProjectGenerator.createProject(projdir, "org.netbeans.modules.java.j2seproject");
        pm = ProjectManager.getDefault();
        pp = pm.findProject(projdir);
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("javac.source", "${def}");
        props.setProperty ("platform.active",platformName);
        props.setProperty("def", "1.2.3.4");
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        props = PropertyUtils.getGlobalProperties();
        props.put("default.javac.source","4.3.2.1");
        PropertyUtils.putGlobalProperties(props);
        sources = projdir.createFolder("src");
        tests = projdir.createFolder("test");
    }
    
    public void testGetSourceLevelWithValidPlatform() throws Exception {
        this.prepareProject("TestPlatform");
        FileObject file = scratch.createData("some.java");
        String sl = SourceLevelQuery.getSourceLevel(file);
        assertEquals("Non-project Java file does not have any source level", null, sl);
        file = sources.createData("a.java");
        sl = SourceLevelQuery.getSourceLevel(file);
        assertEquals("Project's Java file must have project's source", "1.2.3.4", sl);        
    }
    
    public void testGetSourceLevelWithBrokenPlatform() throws Exception {
        this.prepareProject("BrokenPlatform");
        FileObject file = scratch.createData("some.java");
        String sl = SourceLevelQuery.getSourceLevel(file);
        assertEquals("Non-project Java file does not have any source level", null, sl);
        file = sources.createData("a.java");
        sl = SourceLevelQuery.getSourceLevel(file);
        assertEquals("Project's Java file must have project's source", "4.3.2.1", sl);        
    }
    
    
    
    private static class TestPlatformProvider implements JavaPlatformProvider {
        
        private JavaPlatform platform;
        
        public void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
        }

        public void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
        }

        public JavaPlatform[] getInstalledPlatforms()  {
            return new JavaPlatform[] {
                getDefaultPlatform()
            };
        }

        public JavaPlatform getDefaultPlatform()  {
            if (this.platform == null) {
                this.platform = new TestPlatform ();
            }
            return this.platform;
        }                                
    }
    
    private static class TestPlatform extends JavaPlatform {
        
        public FileObject findTool(String toolName) {
            return null;
        }

        public String getVendor() {
            return "me";    
        }

        public ClassPath getStandardLibraries() {
            return null;
        }

        public Specification getSpecification() {
            return new Specification ("j2se", new SpecificationVersion ("1.5"));
        }

        public ClassPath getSourceFolders() {
            return null;
        }

        public java.util.Map getProperties() {
            return Collections.singletonMap("platform.ant.name","TestPlatform");
        }

        public java.util.List getJavadocFolders() {
            return null;
        }

        public java.util.Collection getInstallFolders() {
            return null;
        }

        public String getDisplayName() {
            return "TestPlatform";
        }

        public ClassPath getBootstrapLibraries() {
            return null;
        }
        
    }
    
}
