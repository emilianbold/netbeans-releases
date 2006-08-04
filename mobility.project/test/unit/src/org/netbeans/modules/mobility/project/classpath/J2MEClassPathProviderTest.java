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

/*
 * J2MEClassPathProviderTest.java
 * JUnit based test
 *
 * Created on 09 February 2006, 16:58
 */
package org.netbeans.modules.mobility.project.classpath;

import java.io.File;
import java.util.List;
import junit.framework.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.mobility.project.J2MEProjectGenerator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lukas
 */
public class J2MEClassPathProviderTest extends NbTestCase {
    static AntProjectHelper aph=null;
    static J2MEClassPathProvider instance = null;
    static FileObject projDir = null;
    
    public J2MEClassPathProviderTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        File workDir = getWorkDir();
        File proj = new File(workDir, "testProject");
        
        System.setProperty("netbeans.user","test/tiredTester");
        
        aph = J2MEProjectGenerator.createNewProject(proj, "testProject", null, null,null);
        projDir=FileUtil.toFileObject(proj);
        Project p=ProjectManager.getDefault().findProject(projDir);
        assertNotNull(p);
        instance = p.getLookup().lookup(J2MEClassPathProvider.class);
        assertNotNull(instance);
        EditableProperties ep=aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty("libs.classpath","build");
        ep.setProperty("platform.active","test");
        ep.setProperty("platforms.test.home","home");
        ep.setProperty("platform.bootclasspath","${platform.home}/bin");
        aph.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,ep);
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(J2MEClassPathProviderTest.class);
        
        return suite;
    }
    
    /**
     * Test of findClassPath method, of class org.netbeans.modules.mobility.project.classpath.J2MEClassPathProvider.
     */
    public void testFindClassPath() throws Exception {
        List entries=null;
        System.out.println("findClassPath");
        FileObject file=projDir.getFileObject("src").createData("Test.java");
        
        ClassPath result = instance.findClassPath(file, ClassPath.BOOT);
        entries=result.entries();
        assertTrue(entries.size()==1);
        ClassPath.Entry entry=(ClassPath.Entry)entries.get(0);
        String s1=(projDir.getURL().toString()+"home/bin/").replaceAll(" ","%20");
        String s2=entry.getURL().toString();
        assertEquals(s1,s2);
        
        result = instance.findClassPath(file, ClassPath.COMPILE);
        entries=result.entries();
        assertTrue(entries.size()==1);
        entry=(ClassPath.Entry)entries.get(0);
        s1=(projDir.getURL().toString()+"build/").replaceAll(" ","%20");
        s2=entry.getURL().toString();
        assertEquals(s1,s2);
        
        result = instance.findClassPath(file, ClassPath.EXECUTE);
        entries=result.entries();
        assertTrue(entries.size()==1);
        entry=(ClassPath.Entry)entries.get(0);
        s1=(projDir.getURL().toString()+"build/compiled/").replaceAll(" ","%20");
        s2=entry.getURL().toString();
        assertEquals(s1,s2);
        
        result = instance.findClassPath(file, ClassPath.SOURCE);
        entries=result.entries();
        assertTrue(entries.size()==1);
        entry=(ClassPath.Entry)entries.get(0);
        s1=(projDir.getURL().toString()+"src/").replaceAll(" ","%20");
        s2=entry.getURL().toString();
        assertTrue(s1.startsWith(s2));
    }
}
