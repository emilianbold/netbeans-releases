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

package org.netbeans.modules.j2ee.earproject;

import java.io.IOException;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import java.io.File;
import junit.framework.TestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.spi.project.support.ant.ProjectGenerator;

/**
 *
 * @author vkraemer
 */
public class EarProjectTypeTest extends TestCase {
    
    /**
     * Test of createProject method, of class org.netbeans.modules.j2ee.earproject.EarProjectType.
     */
    public void testCreateProject() {

        System.out.println("testCreateProject");
        FileObject dir[] = null;
        
        // TODO add your test code below by replacing the default call to fail.
        try {
            File f = File.createTempFile("vbktest","ZZZZZZ");
            File tmpdir = f.getParentFile();
            f.delete();
            f = new File(tmpdir,"EarProjectTypeTest.testCreatProject");
            f.mkdirs();
            dir = FileUtil.fromFile(f);
            String typeName = t.getType();
            AntProjectHelper tmp = ProjectGenerator.createProject(dir[0], typeName);
            // TODO figure out how to avoid getting a null here...
            if (null != tmp) {
                t.createProject(tmp);
//                fail("null is an invalid argument");
            }
        } catch (IOException ioe) {
            
        } catch (Throwable t) {
            t.printStackTrace();
            fail("caught an unexpected exception: "+t.getClass().toString());
        } finally {
            try {
                if (null != dir && null != dir[0]) {
                    dir[0].delete();
                }
            } catch (Throwable t) {
                System.out.println("bummer there");
            }
        }
    }

    /**
     * Test of createProject method, of class org.netbeans.modules.j2ee.earproject.EarProjectType.
     */
    public void testCreateProjectNullArg() {

        System.out.println("testCreateProjectNullArg");
        
        // TODO add your test code below by replacing the default call to fail.
        try {
            t.createProject(null);
            fail("null is an invalid argument");
        } catch (IllegalArgumentException iae) {
            
        } catch (Throwable t) {
            fail("caught an unexpected exception: "+t.getCause().toString());
        }
    }

    /**
     * Test of getType method, of class org.netbeans.modules.j2ee.earproject.EarProjectType.
     */
    public void testGetType() {

        System.out.println("testGetType");
        
        // TODO add your test code below by replacing the default call to fail.
        String tmp = t.getType();
        assertEquals(tmp,"org.netbeans.modules.j2ee.earproject"); // NOI18N
    }

    private EarProjectType t;
    
    public EarProjectTypeTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        t = new EarProjectType();
    }

}
