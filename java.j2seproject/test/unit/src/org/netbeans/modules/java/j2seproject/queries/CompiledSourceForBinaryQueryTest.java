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

package org.netbeans.modules.java.j2seproject.queries;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
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
import org.openide.filesystems.Repository;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;

/**
 * Tests for CompiledSourceForBinaryQuery
 *
 * @author Tomas Zezula
 */
public class CompiledSourceForBinaryQueryTest extends NbTestCase {
    
    public CompiledSourceForBinaryQueryTest(java.lang.String testName) {
        super(testName);
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private FileObject buildClasses;
    private ProjectManager pm;
    private Project pp;
    AntProjectHelper helper;
    
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setLookup(new Object[] {
            new org.netbeans.modules.java.j2seproject.J2SEProjectType(),
            new org.netbeans.modules.java.project.ProjectSourceForBinaryQuery(),
            new org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation(),
        }, CompiledSourceForBinaryQuery.class.getClassLoader());
        Properties p = System.getProperties();
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        pm = null;
        TestUtil.setLookup(Lookup.EMPTY);
        super.tearDown();
    }
    
    
    private void prepareProject () throws IOException {
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        helper = ProjectGenerator.createProject(projdir, "org.netbeans.modules.java.j2seproject");
        pm = ProjectManager.getDefault();
        pp = pm.findProject(projdir);
        sources = projdir.createFolder("src");
        FileObject fo = projdir.createFolder("build");
        buildClasses = fo.createFolder("classes");
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.put ("build.dir","build");
        props.put ("build.classes.dir","${build.dir}/classes");
        props.put ("src.dir","src");        
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
    }
    
    public void testSourceForBinaryQuery() throws Exception {
        this.prepareProject();
        FileObject folder = scratch.createFolder("SomeFolder");
        SourceForBinaryQuery.Result result = SourceForBinaryQuery.findSourceRoots(folder.getURL());
        assertEquals("Non-project folder does not have any source folder", 0, result.getRoots().length);
        folder = projdir.createFolder("SomeFolderInProject");
        result = SourceForBinaryQuery.findSourceRoots(folder.getURL());
        assertEquals("Project non build folder does not have any source folder", 0, result.getRoots().length);
        result = SourceForBinaryQuery.findSourceRoots(buildClasses.getURL());
        assertEquals("Project build folder must have source folder", 1, result.getRoots().length);
        assertEquals("Project build folder must have source folder",sources,result.getRoots()[0]);
    }               
    
    
    public void testSourceForBinaryQueryListening () throws Exception {
        this.prepareProject();
        SourceForBinaryQuery.Result result = SourceForBinaryQuery.findSourceRoots(buildClasses.getURL());
        assertEquals("Project build folder must have source folder", 1, result.getRoots().length);
        assertEquals("Project build folder must have source folder",sources,result.getRoots()[0]);
        TestListener tl = new TestListener ();
        result.addChangeListener(tl);
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        FileObject sources2 = projdir.createFolder("src2");
        props.put ("src.dir","src2");        
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        assertTrue (tl.wasEvent());
        assertEquals("Project build folder must have source folder", 1, result.getRoots().length);
        assertEquals("Project build folder must have source folder",sources2,result.getRoots()[0]);
    }       
    
    private static class TestListener implements ChangeListener {
        
        private boolean gotEvent;
        
        public void stateChanged(ChangeEvent changeEvent) {
            this.gotEvent = true;
        }      
        
        public void reset () {
            this.gotEvent = false;
        }
        
        public boolean wasEvent () {
            return this.gotEvent;
        }
    }
        
}
