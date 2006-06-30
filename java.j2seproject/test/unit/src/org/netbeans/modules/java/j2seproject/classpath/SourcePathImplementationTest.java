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

package org.netbeans.modules.java.j2seproject.classpath;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.J2SEProjectUtil;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.java.j2seproject.SourceRootsTest;
import org.openide.util.Mutex;

public class SourcePathImplementationTest extends NbTestCase {

    public SourcePathImplementationTest(String testName) {
        super(testName);
    }

    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private ProjectManager pm;
    private AntProjectHelper helper;
    private J2SEProject pp;

    protected void setUp() throws Exception {
        super.setUp();
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.4"));   //NOI18N
        helper = J2SEProjectGenerator.createProject(FileUtil.toFile(projdir),"proj",null,null); //NOI18N
        J2SEProjectGenerator.setDefaultSourceLevel(null);
        pm = ProjectManager.getDefault();
        pp = (J2SEProject) pm.findProject(projdir).getLookup().lookup(J2SEProject.class);
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
    
    public void testWSClientSupport () throws Exception {
        ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)pp.getLookup().lookup(ClassPathProviderImpl.class);
        ClassPath[] cps = cpProvider.getProjectClassPaths(ClassPath.SOURCE);
        ClassPath cp = cps[0];
        List entries = cp.entries();
        assertNotNull ("Entries can not be null", entries);
        assertEquals ("There must be 2 src entries",2, entries.size());
        assertEquals("There must be src root",((ClassPath.Entry)entries.get(0)).getRoot(),sources);
        String buildDir = (String) J2SEProjectUtil.getEvaluatedProperty(pp,"${build.dir}");
        assertNotNull ("There is no build.dir property", buildDir);
        File f = new File (new File (pp.getAntProjectHelper().resolveFile(buildDir),"generated"),"wsclient");
        URL url = f.toURI().toURL();
        if (!f.exists()) {
            url = new URL (url.toExternalForm() + "/");
        }
        assertEquals("There must be WSClient entry",((ClassPath.Entry)entries.get(1)).getURL(),url);                
        ProjectManager.mutex().writeAccess( new Mutex.ExceptionAction () {
            public Object run () throws Exception {
                EditableProperties ep = pp.getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                ep.put("build.dir","build2");   //NOI18N
                pp.getAntProjectHelper().putProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH,ep);
                ProjectManager.getDefault().saveProject(pp);
                return null;
            }
        });                
        entries = cp.entries();
        assertNotNull ("Entries can not be null", entries);
        assertEquals ("There must be 2 src entries",2, entries.size());
        assertEquals("There must be src root",((ClassPath.Entry)entries.get(0)).getRoot(),sources);
        buildDir = (String) J2SEProjectUtil.getEvaluatedProperty(pp,"${build.dir}");
        assertNotNull ("There is no build.dir property", buildDir);
        f = new File (new File (pp.getAntProjectHelper().resolveFile(buildDir),"generated"),"wsclient");
        url = f.toURI().toURL();
        if (!f.exists()) {
            url = new URL (url.toExternalForm() + "/");
        }
        assertEquals("There must be WSClient entry",((ClassPath.Entry)entries.get(1)).getURL(),url);
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
