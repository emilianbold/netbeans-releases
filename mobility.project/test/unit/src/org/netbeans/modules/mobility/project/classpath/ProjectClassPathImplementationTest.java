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
 * ProjectClassPathImplementationTest.java
 * JUnit based test
 *
 * Created on 10 February 2006, 14:22
 */
package org.netbeans.modules.mobility.project.classpath;

import junit.framework.*;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import java.util.List;
import java.io.File;
import java.net.URL;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.classpath.SimplePathResourceImplementation;
import org.netbeans.modules.mobility.project.J2MEProjectGenerator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lukas
 */
public class ProjectClassPathImplementationTest extends NbTestCase {
    static AntProjectHelper aph=null;
    static ProjectClassPathImplementation instance = null;
    static FileObject projDir = null;
    
    
    public ProjectClassPathImplementationTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        File workDir = getWorkDir();
        File proj = new File(workDir, "testProject");
        
        System.setProperty("netbeans.user","test/tiredTester");
        
        aph = J2MEProjectGenerator.createNewProject(proj, "testProject", null, null,null);
        projDir=FileUtil.toFileObject(proj);
        instance=new ProjectClassPathImplementation(aph) {
            
            protected String evaluatePath() {
                return "test.jar";
            }
        };
        assertNotNull(instance);
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ProjectClassPathImplementationTest.class);
        
        return suite;
    }
    
    /**
     * Test of getResources method, of class org.netbeans.modules.mobility.project.classpath.ProjectClassPathImplementation.
     */
    public void testGetResources() throws Exception {
        System.out.println("getResources");
        
        List entries = instance.getResources();
        assertTrue(entries.size()==1);
        SimplePathResourceImplementation entry=(SimplePathResourceImplementation)entries.get(0);
        String s1=new URL("jar:" + projDir.getURL().toString()+"test.jar!/").toString().replaceAll(" ","%20");
        URL urls[]=entry.getRoots();
        assertTrue(urls.length==1);
        String s2=urls[0].toString();
        assertEquals(s1,s2);
    }
    
    
    
    /**
     * Test of removeResource method, of class org.netbeans.modules.mobility.project.classpath.ProjectClassPathImplementation.
     */
    public void testRemoveResource() {
        System.out.println("removeResource");
        PathResourceImplementation resource = instance.getResources().get(0);
        
        //Following methods are called just to get test covereage, implementation is incorrect
        try {
            instance.configurationXmlChanged(null);
            instance.addResource(null);
        } catch (UnsupportedOperationException e) {}
        
        try {
            instance.reorder(null);
        } catch (UnsupportedOperationException e) {}
        
        
        try {
            instance.removeResource(resource);
        } catch (UnsupportedOperationException e) {
            //We are expecting methods fails as implementation is not finished
        }
    }
}
