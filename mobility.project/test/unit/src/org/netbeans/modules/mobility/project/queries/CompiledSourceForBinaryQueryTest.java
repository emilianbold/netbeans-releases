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
 * CompiledSourceForBinaryQueryTest.java
 * JUnit based test
 *
 * Created on 14 February 2006, 10:43
 */
package org.netbeans.modules.mobility.project.queries;

import java.io.File;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import junit.framework.*;
import java.net.URL;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.MasterFileSystem;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.J2MEProjectGenerator;
import org.netbeans.modules.mobility.project.TestUtil;
import org.netbeans.modules.mobility.project.classpath.J2MEProjectClassPathExtenderTest;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lukas
 */
public class CompiledSourceForBinaryQueryTest extends NbTestCase {
    static AntProjectHelper aph=null;
    static CompiledSourceForBinaryQuery instance = null;
    static FileObject projDir = null;
    
    static
    {
        TestUtil.setLookup( new Object[] {            
        }, CompiledSourceForBinaryQueryTest.class.getClassLoader());
        assertNotNull(MasterFileSystem.settingsFactory(null));
    }
    
    public CompiledSourceForBinaryQueryTest(String testName) {
        super(testName);
        
        TestUtil.setEnv();
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
        projDir.getParent().createFolder("src2");
        instance = p.getLookup().lookup(CompiledSourceForBinaryQuery.class);
        assertNotNull(instance);
        EditableProperties ep=aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty("libs.classpath","../src2");
        aph.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,ep);
        
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(CompiledSourceForBinaryQueryTest.class);
        
        return suite;
    }
    
    /**
     * Test of findSourceRoots method, of class org.netbeans.modules.mobility.project.queries.CompiledSourceForBinaryQuery.
     */
    public void testFindSourceRoots() throws Exception {
        System.out.println("findSourceRoots");
        URL projRoot=null;
        URL binRoot=null;
        try {
            projRoot = projDir.getURL();
            binRoot=FileUtil.normalizeFile(new File(projDir.getPath()+File.separatorChar+"dist")).toURI().toURL();
        } catch (FileStateInvalidException ex) {
            ex.printStackTrace();
            fail("Can't get URL");
        }
        
        SourceForBinaryQuery.Result result = instance.findSourceRoots(projRoot);
        assertNotNull(result);        
        FileObject obj[]=result.getRoots();
        assertNotNull(obj);       
        assertTrue(obj.length==0);
        
        
        result = instance.findSourceRoots(binRoot);
        assertNotNull(result);        
        obj=result.getRoots();
        assertNotNull(obj);       
        assertTrue(obj.length>0);
        
        
        assertEquals(obj[0],projDir.getFileObject("src"));
        
        //Improve test coverage
        ChangeListener list=new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
            }
        };
        result.addChangeListener(list);
        result.removeChangeListener(null);
        EditableProperties ep=aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty("src.dir","../src2");
        aph.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,ep);
        //result.removeChangeListener(list);
    }
    
    public void testSourceLevelQueryImpl() throws Exception {
        J2MEProject p=(J2MEProject)ProjectManager.getDefault().findProject(projDir);
        SourceLevelQueryImpl simpl=p.getLookup().lookup(SourceLevelQueryImpl.class);
        String ver=simpl.getSourceLevel(null);
        assertEquals(ver,"1.3");
    }
    
}
