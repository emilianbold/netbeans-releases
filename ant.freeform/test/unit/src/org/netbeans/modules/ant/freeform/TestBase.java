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

package org.netbeans.modules.ant.freeform;

import java.io.File;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Base class for tests.
 * @author Jesse Glick
 */
public abstract class TestBase extends NbTestCase {
    
    static {
        TestBase.class.getClassLoader().setDefaultAssertionStatus(true);
    }
    
    protected TestBase(String name) {
        super(name);
    }
    
    protected File egdir;
    protected FileObject egdirFO;
    protected FreeformProject simple;
    protected FreeformProject extsrcroot;
    protected FileObject myAppJava;
    protected FileObject specialTaskJava;
    protected FileObject buildProperties;
    
    protected void setUp() throws Exception {
        super.setUp();
        egdir = FileUtil.normalizeFile(new File(System.getProperty("test.eg.dir")));
        assertTrue("example dir exists", egdir.exists());
        egdirFO = FileUtil.toFileObject(egdir);
        assertNotNull("have FileObject for " + egdir);
        FileObject projdir = egdirFO.getFileObject("simple");
        assertNotNull("found projdir", projdir);
        Project _simple = ProjectManager.getDefault().findProject(projdir);
        assertNotNull("have a project", _simple);
        simple = (FreeformProject) _simple;
        myAppJava = projdir.getFileObject("src/org/foo/myapp/MyApp.java");
        assertNotNull("found MyApp.java", myAppJava);
        specialTaskJava = projdir.getFileObject("antsrc/org/foo/ant/SpecialTask.java");
        assertNotNull("found SpecialTask.java", specialTaskJava);
        buildProperties = projdir.getFileObject("build.properties");
        assertNotNull("found build.properties", buildProperties);
        projdir = egdirFO.getFileObject("extsrcroot/proj");
        assertNotNull("found projdir", projdir);
        Project _extsrcroot = ProjectManager.getDefault().findProject(projdir);
        assertNotNull("have a project", _extsrcroot);
        extsrcroot = (FreeformProject) _extsrcroot;
    }
    
    /** ChangeListener for tests. */
    protected static final class TestCL implements ChangeListener {
        private int changed = 0;
        public TestCL() {}
        public synchronized void stateChanged(ChangeEvent changeEvent) {
            changed++;
        }
        /** Return count of change events since last call. Resets count. */
        public synchronized int changeCount() {
            int x = changed;
            changed = 0;
            return x;
        }
    }
    
}
