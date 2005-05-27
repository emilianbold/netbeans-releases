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

package org.netbeans.spi.java.project.classpath.support;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.project.support.ant.AntBasedTestUtil;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.TestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * Tests for {@link ProjectClassPathImplementation}.
 * @author Tomas Zezula
 */
public class ProjectClassPathImplementationTest extends NbTestCase {
    
    private static final String PROP_NAME_1 = "classpath1"; //NOI18N
    private static final String PROP_NAME_2 = "classpath2"; //NOI18N
    
    public ProjectClassPathImplementationTest(String testName) {
        super(testName);
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private FileObject[] cpRoots1;
    private FileObject[] cpRoots2;
    private AntProjectHelper helper;
    private PropertyEvaluator evaluator;
    
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setLookup(new Object[] {
            new org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation(),
            AntBasedTestUtil.testAntBasedProjectType(),
        });
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        cpRoots1 = null;
        cpRoots2 = null;
        helper = null;
        evaluator = null;
        TestUtil.setLookup(Lookup.EMPTY);
        super.tearDown();
    }
    
    
    private void prepareProject () throws IOException {
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj"); //NOI18N
        cpRoots1 = new FileObject[2];
        cpRoots1[0] = scratch.createFolder("cpRoot1"); //NOI18N
        cpRoots1[1] = scratch.createFolder("cpRoot2"); //NOI18N
        cpRoots2 = new FileObject[2];
        cpRoots2[0] = scratch.createFolder("cpRoot3"); //NOI18N
        cpRoots2[1] = scratch.createFolder("cpRoot4"); //NOI18N
        helper = ProjectGenerator.createProject(projdir, "test"); //NOI18N                
        evaluator = helper.getStandardPropertyEvaluator();
        setClassPath(new String[] {PROP_NAME_1, PROP_NAME_2}, new FileObject[][] {cpRoots1, cpRoots2});
    }
    
    public void testBootClassPathImplementation () throws Exception {
        prepareProject();
        ClassPathImplementation cpImpl = ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                FileUtil.toFile(helper.getProjectDirectory()), evaluator, new String[] {PROP_NAME_1, PROP_NAME_2});
        ClassPath cp = ClassPathFactory.createClassPath(cpImpl);
        FileObject[] fo = cp.getRoots();
        List expected = new ArrayList ();
        expected.addAll(Arrays.asList(cpRoots1));
        expected.addAll(Arrays.asList(cpRoots2));
        assertEquals ("Wrong ClassPath roots",expected, Arrays.asList(fo));   //NOI18N
        cpRoots1 = new FileObject[] {cpRoots1[0]};
        setClassPath(new String[] {PROP_NAME_1}, new FileObject[][]{cpRoots1});
        fo = cp.getRoots();
        expected = new ArrayList ();
        expected.addAll(Arrays.asList(cpRoots1));
        expected.addAll(Arrays.asList(cpRoots2));
        assertEquals ("Wrong ClassPath roots",expected, Arrays.asList(fo));   //NOI18N
        cpRoots2 = new FileObject[] {cpRoots2[0]};
        setClassPath(new String[] {PROP_NAME_2}, new FileObject[][]{cpRoots2});
        fo = cp.getRoots();
        expected = new ArrayList ();
        expected.addAll(Arrays.asList(cpRoots1));
        expected.addAll(Arrays.asList(cpRoots2));
        assertEquals ("Wrong ClassPath roots",expected, Arrays.asList(fo));   //NOI18N
    }        
    
    // XXX should test that changes are actually fired when appropriate
    
    private void setClassPath (String[] propNames, FileObject[][] cpRoots) {
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        for (int i=0; i< propNames.length; i++) {
            props.setProperty (propNames[i],toPath(cpRoots[i]));
        }                
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
    }
    
    
    private static String toPath (FileObject[] cpRoots) {
        StringBuffer result = new StringBuffer ();
        for (int i=0; i<cpRoots.length; i++) {
            if (i>0) {
                result.append(':'); //NOI18N
            }
            File f = FileUtil.toFile (cpRoots[i]);
            result.append (f.getAbsolutePath());
        }
        return result.toString();
    }
    
}
