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
import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.java.j2seproject.SourceRootsTest;

public class SourcePathImplementationTest extends NbTestCase {

    public SourcePathImplementationTest(String testName) {
        super(testName);
    }

    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private ProjectManager pm;
    private AntProjectHelper helper;
    private Project pp;

    protected void setUp() throws Exception {
        super.setUp();
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        helper = J2SEProjectGenerator.createProject(FileUtil.toFile(projdir),"proj",null,null); //NOI18N
        pm = ProjectManager.getDefault();
        pp = pm.findProject(projdir);
        sources = projdir.getFileObject("src");
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        pm = null;
        super.tearDown();
    }

    public void testSourcePathImplementation () throws Exception {
        ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)pp.getLookup().lookup(ClassPathProviderImpl.class);
        ClassPath[] cps = cpProvider.getProjectClassPaths(ClassPath.SOURCE);
        ClassPath cp = cps[0];
        FileObject[] roots = cp.getRoots();
        assertNotNull ("Roots can not be null",roots);
        assertEquals ("There must be src root",1,roots.length);
        assertEquals("There must be src root",roots[0],sources);
        TestListener tl = new TestListener();
        cp.addPropertyChangeListener (tl);
        FileObject newRoot = SourceRootsTest.addSourceRoot(helper, projdir,"src.other.dir","other");
        Set events = tl.getEvents();
        assertTrue ("Classpath must fire PROP_ENTRIES and PROP_ROOTS", events.containsAll (Arrays.asList(new String[] {ClassPath.PROP_ENTRIES, ClassPath.PROP_ROOTS})));
        roots = cp.getRoots();
        assertNotNull ("Roots can not be null",roots);
        assertEquals ("There must be src root",2,roots.length);
        assertEquals("There must be src root",roots[0],sources);
        assertEquals("There must be other root",roots[1],newRoot);
        cp.removePropertyChangeListener(tl);
    }

    private static class TestListener implements PropertyChangeListener {
        private Set events = new HashSet ();

        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (propName != null) {
                this.events.add (propName);
            }
        }

        public Set getEvents () {
            return Collections.unmodifiableSet(this.events);
        }
    }
}
