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

package org.netbeans.modules.java.j2seproject.classpath;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.platform.JavaPlatformProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.TestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;

/**
 * Tests for {@link BootClassPathImplementation}.
 * @author Tomas Zezula
 */
public class BootClassPathImplementationTest extends NbTestCase {
    
    public BootClassPathImplementationTest(String testName) {
        super(testName);
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private FileObject tests;
    private FileObject defaultPlatformBootRoot;
    private FileObject explicitPlatformBootRoot;
    private ProjectManager pm;
    private Project pp;
    private TestPlatformProvider tp;
    
    protected void setUp() throws Exception {
        super.setUp();
        scratch = TestUtil.makeScratchDir(this);
        this.defaultPlatformBootRoot = scratch.createFolder("DefaultPlatformBootRoot");
        this.explicitPlatformBootRoot = scratch.createFolder("ExplicitPlatformBootRoot");
        ClassPath defBCP = ClassPathSupport.createClassPath(new URL[]{defaultPlatformBootRoot.getURL()});
        ClassPath expBCP = ClassPathSupport.createClassPath(new URL[]{explicitPlatformBootRoot.getURL()});
        tp = new TestPlatformProvider (defBCP, expBCP);
        TestUtil.setLookup(new Object[] {
            new org.netbeans.modules.java.j2seproject.J2SEProjectType(),
            new org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation(),
            tp
        });
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        pm = null;
        TestUtil.setLookup(Lookup.EMPTY);
        super.tearDown();
    }
    
    
    private void prepareProject (String platformName) throws IOException {
        projdir = scratch.createFolder("proj");
        AntProjectHelper helper = ProjectGenerator.createProject(projdir, "org.netbeans.modules.java.j2seproject");
        pm = ProjectManager.getDefault();
        pp = pm.findProject(projdir);
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty ("platform.active",platformName);
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        sources = projdir.createFolder("src");
        tests = projdir.createFolder("test");
    }
    
    public void testBootClassPathImplementation () throws Exception {
        this.prepareProject("ExplicitPlatform");        
        FileObject file = sources.createData("a.java");
        ClassPath bootCP = ClassPath.getClassPath(file, ClassPath.BOOT);
        assertNotNull("Boot ClassPath exists",bootCP);
        FileObject[] roots = bootCP.getRoots();
        assertEquals("Boot classpath size",1, roots.length);        
        assertEquals("Boot classpath",explicitPlatformBootRoot, roots[0]);
        
        tp.setExplicitPlatformVisible(false);
        bootCP = ClassPath.getClassPath(file, ClassPath.BOOT);
        assertNotNull("Boot ClassPath exists",bootCP);
        roots = bootCP.getRoots();
        assertEquals("Boot classpath size",1, roots.length);        
        assertEquals("Boot classpath",defaultPlatformBootRoot, roots[0]);
                
        tp.setExplicitPlatformVisible(true);
        bootCP = ClassPath.getClassPath(file, ClassPath.BOOT);
        assertNotNull("Boot ClassPath exists",bootCP);
        roots = bootCP.getRoots();
        assertEquals("Boot classpath size",1, roots.length);        
        assertEquals("Boot classpath",explicitPlatformBootRoot, roots[0]);
    }        
    
    
    
    private static class TestPlatformProvider implements JavaPlatformProvider {
        
        private JavaPlatform defaultPlatform;
        private JavaPlatform explicitPlatform;
        private PropertyChangeSupport support;
        private boolean hideExplicitPlatform;
        
        public TestPlatformProvider (ClassPath defaultPlatformBootClassPath, ClassPath explicitPlatformBootClassPath) {
            this.support = new PropertyChangeSupport (this);
            this.defaultPlatform = new TestPlatform ("DefaultPlatform", defaultPlatformBootClassPath);
            this.explicitPlatform = new TestPlatform ("ExplicitPlatform", explicitPlatformBootClassPath);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            this.support.removePropertyChangeListener (listener);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            this.support.addPropertyChangeListener(listener);
        }

        public JavaPlatform[] getInstalledPlatforms()  {
            if (this.hideExplicitPlatform) {
                return new JavaPlatform[] {
                    this.defaultPlatform,
                };
            }
            else {
                return new JavaPlatform[] {
                    this.defaultPlatform,
                    this.explicitPlatform,
                };
            }
        }
       
        public JavaPlatform getDefaultPlatform () {            
            return this.defaultPlatform;
        }
        
        public void setExplicitPlatformVisible (boolean value) {
            this.hideExplicitPlatform = !value;
            this.support.firePropertyChange(PROP_INSTALLED_PLATFORMS,null,null);
        }
    }
    
    private static class TestPlatform extends JavaPlatform {
        
        private String systemName;
        private Map properties;
        private ClassPath bootClassPath;
        
        public TestPlatform (String systemName, ClassPath bootCP) {
            this.systemName = systemName;
            this.bootClassPath = bootCP;
            this.properties = Collections.singletonMap("platform.ant.name",this.systemName);
        }
        
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

        public Map getProperties() {
            return this.properties;
        }

        public List getJavadocFolders() {
            return null;
        }

        public Collection getInstallFolders() {
            return null;
        }

        public String getDisplayName() {
            return this.systemName;
        }

        public ClassPath getBootstrapLibraries() {
            return this.bootClassPath;
        }
        
    }
    
}
